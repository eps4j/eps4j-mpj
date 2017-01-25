/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
/**
*  This file is part of eps4j.
*
*  Copyright (c) 2017, Arnaud Malapert (Université Côte d’Azur, CNRS, I3S, France)
*  All rights reserved.
*
*  This software may be modified and distributed under the terms
*  of the BSD license.  See the LICENSE file for details.
 */
package org.eps4j.proc;

import static org.eps4j.EPSComm.MASTER;
import static org.eps4j.EPSComm.iprobeGive;
import static org.eps4j.EPSComm.recv;
import static org.eps4j.EPSComm.sendBetter;
import static org.eps4j.EPSComm.sendGive;
import static org.eps4j.EPSComm.sendPoisonBetter;
import static org.eps4j.EPSComm.sendPoisonGive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.chocosolver.cutoffseq.ICutoffStrategy;
import org.chocosolver.cutoffseq.LubyCutoffStrategy;
import org.chocosolver.pf4cs.SetUpException;
import org.eps4j.EPSComm;
import org.eps4j.exception.EPSException;
import org.eps4j.specs.IProcessEPS;
import org.eps4j.specs.actors.IForemanEPS;
import org.eps4j.specs.msg.IBetter;
import org.eps4j.specs.msg.IJob;
import org.eps4j.specs.proc.IJobLocker;
import org.eps4j.specs.proc.IJobSelector;

import mpi.Status;

public class ForemanProcess<J extends IJob, B extends IBetter> implements IProcessEPS {
   
    //TODO Ensure that a subproblem sent to a worker is not discarded by the so-far best solution ?
    private final IForemanEPS<B, J> foreman;

    private final IJobSelector<J> jobSelector;

    private final IJobLocker<J> jobLocker; 
    
    private final ICutoffStrategy parallelRestart; 
    
    private final HashMap<Integer, J> runningJobs = new HashMap<>();

    //TODO Should I use a list or a set ?
    private final List<J> pendingJobs = new ArrayList<>();

    private final Set<J> unlockedJobs = new HashSet<>();

    private int betterCount;

    private int giveCount;

    private int npills;

    
    public ForemanProcess(IForemanEPS<B, J> foreman, IJobSelector<J> jobSelector, IJobLocker<J> jobLocker, ICutoffStrategy parallelRestart) {
        super();
        this.foreman = foreman;
        this.jobSelector = jobSelector;
        this.jobLocker = jobLocker;
        this.parallelRestart = parallelRestart;
    }

    public ForemanProcess(IForemanEPS<B, J> foreman) {
	this(foreman, new FifoJobSelector<>(), (job, rank) -> true, new LubyCutoffStrategy(100));
    }

    @Override
    public void setUp(String... args) throws SetUpException {
	foreman.setUp(args);
	npills = EPSComm.workers()+ 2;
    }

    public boolean hasEnded() {
	return foreman.hasEnded();
    }

    @Override
    public void tearDown() {
	EPSComm.LOGGER.log(Level.INFO, "\nd F_GIVE {0,number,#}\nd F_BETTER {1,number,#}\nd F_QUEUE {2,number,#}", 
		new Object[]{giveCount, betterCount, pendingJobs.size()});
	runningJobs.clear();
	pendingJobs.clear();
	unlockedJobs.clear();
	foreman.tearDown();
    }

    @Override
    public void decompose() throws ClassNotFoundException, IOException, EPSException {
	boolean isMasterAlive = true;
	while(isMasterAlive) {
	    do {
		isMasterAlive = irecvGiveSendBetter();
		irecvsendBetter();
	    } while(isStarving() && isMasterAlive);
	    irecvCollectSendGive();
	}		
    }

    @Override
    public void solve() throws ClassNotFoundException, IOException, EPSException {
	while( npills > 0) {
	    irecvCollectSendGive();
	    irecvsendBetter();	
	}
    }

    public boolean isStarving() {
	return pendingJobs.isEmpty();
    }

    private boolean irecvGiveSendBetter() throws ClassNotFoundException, IOException {
	final Status status = iprobeGive(MASTER);
	if(status != null) {
	    final JobMsg<J,B> msg = (JobMsg<J,B>) recv(status);
	    if( msg == null) {
		// end of decomposition
		return false;
	    } else {
		pendingJobs.add(msg.getJob());
		foreman.recordBetter(msg.getBetter());
		if(hasEnded() ) {
		    sendPoisonBetter(MASTER);	
		    return false;
		} else {
		    // send latest information
		    sendBetter(MASTER, foreman.getBetter());
		}
	    }
	} 
	return true;
    }

    private void sendJob(int dest) throws IOException {
	if( !hasEnded() && !isStarving() ) {
	    // send new job
	    final int idx = jobSelector.selectJob(Collections.unmodifiableList(pendingJobs), dest);
	    J job;
	    if( jobLocker.lockJob(pendingJobs.get(idx), dest) ) {
		// the worker locks the job
		job = pendingJobs.remove(idx);
	    } else {
		// other workers still can execute the job
		job = pendingJobs.get(idx);
		unlockedJobs.add(job);
	    }
	    runningJobs.put(dest, job);
	    sendGive(dest, job, foreman.getBetter(), parallelRestart.getNextCutoff());	
	} else {
	    // kill worker
	    npills--;
	    sendPoisonGive(dest);
	}
    }
    private void irecvCollectSendGive() throws ClassNotFoundException, IOException, EPSException {
	final Status status = EPSComm.iprobeCollect();
	if(status != null) {
	    final B better = (B) recv(status);
	    if(better == null) {
		if(runningJobs.containsKey(status.source)) {
		    throw new EPSException(" did not receive better from <" + status.source + ">");
		} else {
		    // worker registration (first message)
		    sendJob(status.source);
		}
	    } else {
		if(status.source == MASTER) {
		    foreman.recordCollect(better);
		    npills--;
		} else {
		    final J job = runningJobs.remove(status.source);
		    final boolean done = foreman.recordCollect(job, better);
		    if(done) {
			if(unlockedJobs.remove(job)) {
			    EPSComm.LOGGER.log(Level.INFO, "<{0}> Job is not pending anymore:\n {1}", new Object[]{EPSComm.rank(), job});
			    pendingJobs.remove(job);
			}  
		    } else {			
			if( !unlockedJobs.remove(job)) {
			    EPSComm.LOGGER.log(Level.INFO, "<{0}> Job is re-enqueued:\n {1}", new Object[]{EPSComm.rank(), job});
			    pendingJobs.add(job);
			}
		    } 
		    sendJob(status.source);
		    giveCount++;
		}
	    }
	}
    }

    private void irecvsendBetter() throws ClassNotFoundException, IOException, EPSException {
	Status status = EPSComm.iprobeBetter();
	if(status != null) {
	    final B better = (B) recv(status);
	    if(better == null) {
		throw new EPSException("did not receive the message from <" + status.source + ">");
	    } else {
		betterCount++;
		foreman.recordBetter(better);
		if(hasEnded() ) {
		    sendPoisonBetter(status.source);
		} else {
		    sendBetter(status.source, foreman.getBetter());	
		}
	    }			
	}
    }
}