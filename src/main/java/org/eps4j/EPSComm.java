/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.eps4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eps4j.proc.JobMsg;
import org.eps4j.specs.msg.IBetter;
import org.eps4j.specs.msg.IJob;

import mpi.MPI;
import mpi.Status;
/**
 * 
 * http://ocw.mit.edu/courses/earth-atmospheric-and-planetary-sciences/12-950-parallel-programming-for-multicore-machines-using-openmp-and-mpi-january-iap-2010/
 * 
 * https://www.open-mpi.org/faq/?category=java
 * http://users.dsic.upv.es/~jroman/preprints/ompi-java.pdf
 * http://blogs.cisco.com/performance/java-bindings-for-open-mpi
 * 
 * http://www.mcs.anl.gov/research/projects/mpi/usingmpi/
 * http://stackoverflow.com/questions/17829463/java-task-distribution-and-collection-on-a-grid
 * @author Arnaud Malapert
 *
 */
public final class EPSComm {

    public static final int MASTER = 0;
    public static final int FOREMAN = 1;

    public static final int TAG_GIVE = 1;
    public static final int TAG_COLLECT = 2;
    public static final int TAG_BETTER = 3;

    static {
        // http://stackoverflow.com/questions/14903978/cant-configure-logging-for-executable-jar
        final String logFile = System.getProperty("java.util.logging.config.file");
        if(logFile == null){
            try {
                LogManager.getLogManager().readConfiguration(EPSComm.class.getClassLoader().getResourceAsStream("logging.properties"));
            } catch (SecurityException e) {
                System.err.println("Cannot initialize properly loggers");
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Cannot initialize properly loggers");
                e.printStackTrace();
            }
        }                
    }
    // Now retrieve the logger with proper default settings
    public static final Logger LOGGER =  Logger.getLogger(EPSComm.class.getName());

    // private final static int BUF_SIZE = 4096;
    private final static int BUF_SIZE = 1 << 15;
    
    private final static ByteArrayOutputStream bos = new ByteArrayOutputStream(BUF_SIZE);
    
    private static byte[] buf = new byte[BUF_SIZE];

    private EPSComm() {
        super();
    }

    public static int rank() {
        return MPI.COMM_WORLD.Rank();
    }

    public static int workers() {
        return MPI.COMM_WORLD.Size() - 2;
    }

    public static String[] initMPI(String... args) {
        final String[] trimmedArgs = MPI.Init(args);
        EPSComm.LOGGER.log(Level.CONFIG, "<{0}>", rank());
        if(rank() == MASTER) {
            EPSComm.LOGGER.log(Level.INFO, "\nc WORKERS {0}", workers());
        }
        return trimmedArgs;
    }

    public static void finalizeMPI() {
        barrier();
        EPSComm.LOGGER.log(Level.CONFIG, "<{0}>", rank());
        MPI.Finalize();
    }

    private static int barrierCount = 0;

    public static void barrier() {
        LOGGER.log(Level.CONFIG, "<{0}> #{1}", new Object[] { rank(), ++barrierCount });
        MPI.COMM_WORLD.Barrier();
    }

    private static String getTagName(int tag) {
        switch (tag) {
        case TAG_GIVE:
            return "GIVE";
        case TAG_COLLECT:
            return "COLLECT";
        case TAG_BETTER:
            return "BETTER";
        default:
            return String.valueOf(tag);
        }
    }

    private static byte[] serialize(Serializable object) throws IOException {
        bos.reset();
        final ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        bos.flush();
        return bos.toByteArray();
    }

    private static Object deserialize(byte[] buf, int len) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream bis = new ByteArrayInputStream(buf, 0, len);
        final ObjectInputStream ois = new ObjectInputStream(bis);
        final Object object = ois.readObject();
        ois.close();
        bis.close();
        return object;
    }

    private static void sendNull(int dest, int tag) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "<{0}> => <{1}> [{2}]", new Object[] { rank(), dest, getTagName(tag) });
        }
        MPI.COMM_WORLD.Send(buf, 0, 0, MPI.BYTE, dest, tag);
    }

    private static void recvNull(int source, int tag) {
        MPI.COMM_WORLD.Recv(buf, 0, 0, MPI.BYTE, source, tag);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "<{0}> <= <{1}> [{2}]", new Object[] { rank(), source, getTagName(tag) });
        }
    }

    public static void sendPoisonGive(int dest) throws IOException {
        sendNull(dest, TAG_GIVE);
    }

    public static void sendPoisonBetter(int dest) throws IOException {
        sendNull(dest, TAG_BETTER);
    }

    public static void sendPoisonCollect(int dest) throws IOException {
        sendNull(dest, TAG_COLLECT);
    }

    public static void send(int dest, int tag, Serializable object) throws IOException {
        // Log start
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "<{0}> =>  <{1}> : {2} [{3}]",
                    new Object[] { rank(), dest, object, getTagName(tag) });
        }
        // Send
        byte[] obuf = serialize(object);
        MPI.COMM_WORLD.Send(obuf, 0, obuf.length, MPI.BYTE, dest, tag);
    }

    private static Status probe(int source, int tag) {
        // Log start
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "<{0}> <=  <{1}> [{2}]", new Object[] { rank(), source, getTagName(tag) });
        }
        // Probe
        final Status status = MPI.COMM_WORLD.Probe(source, tag);
        return status;
    }

    public static Object recv(int source, int tag) throws ClassNotFoundException, IOException {
        // Probe
        final Status status = probe(source, tag);
        return recv(status);
    }

    public static Object recv(Status status) throws IOException, ClassNotFoundException {
        final int nbytes = status.Get_count(MPI.BYTE);
        if (nbytes > 0) {
            // Check buffer capacity
            if (buf.length < nbytes) {
                buf = new byte[2 * nbytes];
            }
            // Recv
            status = MPI.COMM_WORLD.Recv(buf, 0, nbytes, MPI.BYTE, status.source, status.tag);
            Object obj = deserialize(buf, nbytes);
            // Log
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "<{0}> <=  <{1}> : {2} [{3}]",
                        new Object[] { rank(), status.source, obj, getTagName(status.tag) });
            }
            return obj;
        } else {
            recvNull(status.source, status.tag);
            return null;
        }
    }

    public static <B extends IBetter> B sendrecvBetter(B better) throws IOException, ClassNotFoundException {
        send(FOREMAN, TAG_BETTER, better);
        return (B) recv(FOREMAN, TAG_BETTER);
    }

    public static void sendBetter(final int dest, final IBetter better) throws IOException {
        send(dest, TAG_BETTER, better);
    }

    public static void sendCollect(final int dest, final IBetter better) throws IOException {
        send(dest, TAG_COLLECT, better);
    }

    public static <J extends IJob, B extends IBetter> void sendGive(final int dest, final J job, final B better) throws IOException {
        send(dest, TAG_GIVE, new JobMsg<>(job, better));
    }

    public static <J extends IJob, B extends IBetter> void sendGive(final int dest, final J job, final B better, final int restartLimit) throws IOException {
        send(dest, TAG_GIVE, new JobMsg<>(job, better, restartLimit));
    }

    public static Status iprobeGive(int src) {
        return MPI.COMM_WORLD.Iprobe(src, TAG_GIVE);
    }

    public static Status iprobeBetter() {
        return MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE, TAG_BETTER);
    }

    public static Status iprobeCollect() {
        return MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE, TAG_COLLECT);
    }

}
