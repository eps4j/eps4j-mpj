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
package org.eps4j.mockup;

import java.io.IOException;

import org.eps4j.EPSComm;
import org.eps4j.exception.EPSRuntimeException;
import org.eps4j.specs.actors.IWorkerEPS;

class Worker implements IWorkerEPS<Job, Bounds> {

	private final MockupSolver jobSolver;
	private final MockupSolver betterSolver;

	private volatile Bounds bounds;
	
	public Worker(MockupSolver jobSolver, MockupSolver betterSolver) {
		super();
		this.jobSolver = jobSolver;
		this.betterSolver = betterSolver;
	}


	@Override
	public void recordBetter(Bounds better) {
		bounds = better;
	}

	@Override
	public Bounds execute(Job job, int limit) {
		bounds = betterSolver.solve(bounds);
		try {
			final Bounds newBounds = (Bounds) EPSComm.sendrecvBetter(bounds);
			if(newBounds != null) {
				bounds = jobSolver.solve(bounds);
			}
		} catch (ClassNotFoundException | IOException e) {
			throw new EPSRuntimeException("did not receive a better message.", e);
		} 
		return bounds;
	}


	@Override
	public void tearDown() {
		bounds = null;
	}


}