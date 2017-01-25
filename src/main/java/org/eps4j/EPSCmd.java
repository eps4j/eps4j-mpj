/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.eps4j;

import static org.eps4j.EPSComm.FOREMAN;
import static org.eps4j.EPSComm.MASTER;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

import org.chocosolver.pf4cs.IUpDown;
import org.chocosolver.pf4cs.SetUpException;
import org.eps4j.exception.EPSException;
import org.eps4j.specs.IArgsEPS;
import org.eps4j.specs.IFactoryEPS;
import org.eps4j.specs.IProcessEPS;

import mpi.MPI;


public class EPSCmd {

    private static final IProcessEPS buildProcess(IFactoryEPS factory) {
        final int rank = EPSComm.rank();
        switch (rank) {
        case MASTER : return factory.buildMaster();
        case FOREMAN : return factory.buildForeman();
        default: return factory.buildWorker(rank-1,EPSComm.workers());
        }
    }

    public final static void doMain(IArgsEPS args) throws EPSException, SetUpException, ClassNotFoundException, IOException {
        final long stime = System.currentTimeMillis();
        final int me = EPSComm.rank();
        if(MPI.COMM_WORLD.Size() < 3) {
            throw new EPSException("Not enough process");
        }	
        // get factory
        final IFactoryEPS factory = args.getFactory();
        EPSComm.LOGGER.log(Level.CONFIG, "<{0}> {1} [FACTORY]", new Object[] {me, factory});
        factory.setUp(args.getFactoryArgs());
        // get process
        final IProcessEPS process = buildProcess(factory);
        // run process
        EPSComm.LOGGER.log(Level.CONFIG, "<{0}> [INIT]", me);
        process.setUp(args.getProcessArgs());
        EPSComm.LOGGER.log(Level.CONFIG, "<{0}> [DECOMP]", me);
        process.decompose();
        EPSComm.LOGGER.log(Level.CONFIG, "<{0}> [SOLVE]", me);
        process.solve();
        EPSComm.LOGGER.log(Level.CONFIG, "<{0}> [EXIT]", me);
        process.tearDown();	
        factory.tearDown();
        // log runtime
        final long etime = System.currentTimeMillis();
        EPSComm.LOGGER.log(Level.CONFIG, "\nd WCTIME_{0,number,#} {1,number,#}", new Object[]{me, etime-stime});
        if(me == FOREMAN) {
            EPSComm.LOGGER.log(Level.INFO, "\nd RUNTIME {0,number,#}", etime-stime);
        }
    }


    /** 
     * @param args MPI_ARGS FACTORY FACTORY_ARGS -- MASTER_ARGS -- FOREMAN_ARGS -- WORKER_ARGS 
     */
    public static void doMain(String... args) throws EPSException, ClassNotFoundException, SetUpException, IOException {
        //Eclipse Run Configuration http://stackoverflow.com/a/36311242
        String[] trimmedArgs = EPSComm.initMPI(args);
        ArgsEPS argsEPS = new ArgsEPS();
        argsEPS.setUp(trimmedArgs);
        EPSComm.LOGGER.log(Level.FINE, "<{0}> {1}", new Object[] {EPSComm.rank(), argsEPS});

        doMain(argsEPS);
        argsEPS.tearDown();
        EPSComm.finalizeMPI();

    }
    
    public static void main(String[] args) throws ClassNotFoundException, EPSException, SetUpException, IOException {
        doMain(args);
    }

    public static class ArgsEPS implements IArgsEPS, IUpDown {

        private int argsIdx ;

        private String factoryClassName;

        private String[] factoryArgs;

        private String[] processArgs;

        private IFactoryEPS factory;

        public ArgsEPS(int argsIdx) {
            super();
            this.argsIdx = argsIdx;
        }

        public ArgsEPS() {
            this(-1);
        }

        private static int getArgIdx() {
            switch (EPSComm.rank()) {
            case MASTER : return 0;
            case FOREMAN : return 1;
            default: return 2;
            }
        }

        private final static int nextSepIndex(String[] args, int from) {
            while(from < args.length) {
                if(args[from].equals("--")) {
                    return from;
                }
                from++;
            }
            return args.length;
        }

        public final void setUp(String... args) throws SetUpException {
            if(argsIdx < 0) {
                argsIdx = getArgIdx();
            }
            if(args.length == 0) {
                throw new SetUpException("No process factory provided");
            }

            // set factory class
            factoryClassName = args[0];
            try {
                factory = (IFactoryEPS) Class.forName (factoryClassName).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new SetUpException("EPS Factory not found: "+factoryClassName, e);
            }
            // find and copy factory args
            int from = 1;
            int to = nextSepIndex(args, from);
            factoryArgs = Arrays.copyOfRange(args,from, to);
            // find and copy process args
            for (int i = 0; i <= argsIdx; i++) {
                from = to;
                to = nextSepIndex(args, from+1);
            }
            processArgs = from < to ? Arrays.copyOfRange(args, from+1, to) : new String[0];	
        }

        public final String getFactoryClassName() {
            return factoryClassName;
        }


        @Override
        public IFactoryEPS getFactory() {
            return factory;
        }

        public final String[] getFactoryArgs() {
            return factoryArgs;
        }

        public final String[] getProcessArgs() {
            return processArgs;
        }

        @Override
        public void tearDown() {
            factoryClassName = null;
            factory = null;
            factoryArgs = null;
            processArgs = null;
        }


        @Override
        public String toString() {
            return "ArgsEPS [factoryClassName=" + factoryClassName + ", factoryArgs=" + Arrays.toString(factoryArgs)
            + ", processArgs=" + Arrays.toString(processArgs) + "]";
        }
    }
    
}

