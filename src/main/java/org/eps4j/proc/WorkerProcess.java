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

import static org.eps4j.EPSComm.FOREMAN;
import static org.eps4j.EPSComm.TAG_GIVE;

import java.io.IOException;

import org.chocosolver.pf4cs.SetUpException;
import org.eps4j.EPSComm;
import org.eps4j.exception.EPSException;
import org.eps4j.specs.IProcessEPS;
import org.eps4j.specs.actors.IWorkerEPS;
import org.eps4j.specs.msg.IBetter;
import org.eps4j.specs.msg.IJob;

public class WorkerProcess<J extends IJob, B extends IBetter> implements IProcessEPS {

	private final IWorkerEPS<J,B> worker;

	public WorkerProcess(IWorkerEPS<J,B> worker) {
		super();
		this.worker = worker;
	}

	@Override
	public void setUp(String... args) throws SetUpException {
		worker.setUp(args);
	}

	@Override
	public void decompose() throws IOException {
		// Init. connection
		EPSComm.sendPoisonCollect(FOREMAN); 
	}


	@Override
	public void solve() throws ClassNotFoundException, IOException, EPSException {
		boolean isRunning = true;
		while(isRunning) {
			final JobMsg<J,B> msg = (JobMsg<J,B>) EPSComm.recv(FOREMAN, TAG_GIVE);
			if(msg == null) {
				isRunning = false;
			} else {
				worker.recordBetter(msg.getBetter());
				final IBetter better = worker.execute(msg.getJob(), msg.getLimit());
				if(better != null) {
					EPSComm.sendCollect(FOREMAN, better);
				} else {
					throw new EPSException("failed to execute " + msg);
				}
			}

		}
	}

	@Override
	public void tearDown() {
		worker.tearDown();
	}

}
