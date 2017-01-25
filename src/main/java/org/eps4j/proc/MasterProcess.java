/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.eps4j.proc;

import static org.eps4j.EPSComm.FOREMAN;
import static org.eps4j.EPSComm.TAG_BETTER;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.chocosolver.pf4cs.SetUpException;
import org.eps4j.EPSComm;
import org.eps4j.exception.EPSRuntimeException;
import org.eps4j.specs.IProcessEPS;
import org.eps4j.specs.actors.IMasterEPS;
import org.eps4j.specs.msg.IBetter;
import org.eps4j.specs.msg.IJob;
import org.eps4j.specs.proc.IBetterObserver;
import org.eps4j.specs.proc.IJobObserver;
import org.eps4j.specs.proc.IMasterProgress;


public class MasterProcess<J extends IJob, B extends IBetter> implements IProcessEPS, IJobObserver<J, B>, IBetterObserver<B>, IMasterProgress {


    private final static IJob POISON_PILL = new IJob() {private static final long serialVersionUID = 1L;};

    //Beware: throws exception when adding null object (null cannot be locked!)
    private final BlockingQueue<IJob> jobList = new LinkedBlockingQueue<>();

    private final ThreadGroup masterGroup = new ThreadGroup("Master");

    private final IMasterEPS<J, B> master;

    private int jobCount;

    private int betterCount;

    private volatile B better;

    private volatile B collectBetter;

    private final ReentrantLock betterLock = new ReentrantLock();

    private final Condition sendBetter = betterLock.newCondition();

    /**
     * Written by the master process thread, but read by the other threads. 
     */
    private volatile boolean isStopped;

    public MasterProcess(IMasterEPS<J,B> master) {
        super();
        this.master = master;
        master.setMasterProgress(this);
    }

    @Override
    public boolean isStopped() {
        return isStopped;
    }

    @Override
    public void setUp(String... args) throws SetUpException {
        isStopped = false;
        jobCount = 0;
        betterCount = 0;
        master.setUp(args);
    }

    @Override
    public void notifyBetter(B better) {
        betterLock.lock(); 
        try {
            this.better = better;
            sendBetter.signal();
        }finally {
            betterLock.unlock();
        }
    }


    @Override
    public void notifyJob(J job, B better) {
        this.better = better;
        jobList.add(job);
    }


    private final class DecompositionThread extends Thread {

        public DecompositionThread() {
            super(masterGroup, "Decomposition");
        }

        @Override
        public void run() {
            collectBetter = master.decompose(MasterProcess.this, EPSComm.workers());
            jobList.add(POISON_PILL); 
        }
    }


    private final void sendCollectBetter() throws IOException {
        EPSComm.sendCollect(FOREMAN, collectBetter);
    }

    private final void recordBetter(B better) throws IOException, ClassNotFoundException {
        if(better == null) {
            EPSComm.LOGGER.log(Level.CONFIG, "<{0}> decomposition is interrupted by the foreman", EPSComm.rank());
            isStopped = true; 
        } else {
            // FIXME possible race condition once again !?
            master.notifyBetter(better);
            // The master update of internal data must be synchronized, because it is running simultaneously
        }
    }

    private final void awakeOnNewJob() throws ClassNotFoundException, IOException, InterruptedException {
        IJob current;
        while( !isStopped && (current = jobList.take() ) != POISON_PILL) {
            jobCount++;
            EPSComm.sendGive(FOREMAN, current, better);
            final B better = (B) EPSComm.recv(FOREMAN, TAG_BETTER);
            recordBetter(better);
        }
        if(! isStopped) {
            EPSComm.sendPoisonGive(FOREMAN);
        }
    }

    @Override
    public void decompose() throws ClassNotFoundException, IOException {
        final Thread decthread = new DecompositionThread();
        decthread.start();
        try {
            awakeOnNewJob();
            decthread.join();
        } catch (InterruptedException e) {
            EPSComm.LOGGER.log(Level.INFO, "Decomposition [INTERRUPTED]", e);
            jobList.clear();
            throw new EPSRuntimeException("Master decomposition interrupted.");
        }
        sendCollectBetter(); 
    }


    private class DiversificationThread extends Thread {

        public DiversificationThread() {
            super(masterGroup, "Diversification");
        }

        @Override
        public void run() {
            collectBetter = master.diversify(MasterProcess.this);
            notifyBetter(null);
        }
    }

    @Override
    public void solve() throws ClassNotFoundException, IOException {
        if(!isStopped) {
            final Thread divthread = new DiversificationThread();
            divthread.start();
            betterLock.lock(); 
            try{
                while(true) {
                    sendBetter.await();
                    if(better != null) {
                        betterCount++;
                        final B better = EPSComm.sendrecvBetter(this.better);
                        recordBetter(better);
                    } else {
                        break;
                    }
                }
                divthread.join();
            } catch (InterruptedException e) {
                EPSComm.LOGGER.log(Level.INFO, "Diversification [INTERRUPTED]", e);
                throw new EPSRuntimeException("Master diversification interrupted.");
            } finally {
                betterLock.unlock();
            }
        }
        sendCollectBetter();	
    }

    @Override
    public void tearDown() {
        isStopped = true;
        EPSComm.LOGGER.log(Level.INFO, "\nd M_GIVE {0,number,#}\nd M_BETTER {1,number,#}\nd M_QUEUE {1,number,#}", new Object[] {jobCount, betterCount, jobList.size()});
        master.tearDown();
    }

}
