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

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chocosolver.pf4cs.SetUpException;
import org.eps4j.EPSComm;
import org.eps4j.proc.ForemanProcess;
import org.eps4j.proc.MasterProcess;
import org.eps4j.proc.WorkerProcess;
import org.eps4j.specs.IFactoryEPS;
import org.eps4j.specs.IProcessEPS;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import mpi.MPI;


class MockupOptSolver extends MockupSolver {

	public MockupOptSolver(Random rnd, int maxTime) {
		super(rnd, maxTime);
	}
	
	
	
	@Override
	public Bounds solve(Bounds bounds) {
		sleep();
		return bounds.reduce(rnd);
	}

}

class MockupSolver {

	protected Random rnd;
	private int maxTime = 0;

	public MockupSolver(Random rnd, int maxTime) {
		super();
		this.rnd = rnd;
		this.maxTime = maxTime;
	}

	public final void sleep() {
		if(maxTime > 0) {
			try {
				Thread.sleep(rnd.nextInt(maxTime)+ 1);
			} catch (InterruptedException e) {
				EPSComm.LOGGER.log(Level.SEVERE, "Thread Insomnia !", e);
			}
		}
	}

	public Bounds solve(Bounds bounds) {
		sleep();
		return bounds;
	}
}

public class MockupFactory implements IFactoryEPS {

	public final String CMD = "java [-jar myCmd.jar| -cp myCmd.jar MyCmd.class] ";

	public final static Logger LOGGER= Logger.getLogger(MockupFactory.class.getPackage().getName());

	@Option(name="-n",aliases={"--problems"},usage="Number of subproblems.")
	private int n=1;
	
	@Option(name="-b",aliases={"--betters"},usage="Number of betters (from the master).")
	private int b=1;

	@Option(name="-lb",aliases={"--lowerBound"},usage="Lower bound on objective (<=0).")
	private int lowerBound=Integer.MIN_VALUE;
	
	@Option(name="-ub",aliases={"--upperBound"},usage="Upper bound on objective (>=0).")
	private int upperBound=Integer.MAX_VALUE;
	
	@Option(name="-td",aliases={"--maxDTime"},usage="Maximum time (ms) for generating a subproblem.")
	private int maxDTime;

	@Option(name="-ts",aliases={"--maxSTime"},usage="Maximum time (ms) for solving a subproblem.")
	private int maxSTime;

	@Option(name="-tb",aliases={"--maxBTime"},usage="Maximum time (ms) for obtaining a better.")
	private int maxBTime;
	
	@Option(name="-sc",aliases={"--scale"},usage="scale of the solve() method for optimization.")
	private double solvingScale = 1;
	
	@Option(name="-s",aliases={"--seed"},usage="Random Seed.")
	private long seed = 0;

	@Option(name="-a",aliases={"--all"},usage="solve all subproblems (enumeration problem).")
	private boolean solveAll;

	private final CmdLineParser parser;

	/**
	 * the mode of this Command Line
	 */
	public MockupFactory() {
		super();
		parser = new CmdLineParser(this);
	}

	@Override
	public void setUp(String... args) throws SetUpException {
		try {
			// parse the arguments.
			parser.parseArgument(args);
			checkData();
		} catch( CmdLineException e ) {
			LOGGER.log(Level.SEVERE,"cmd...[FAIL]",e);
			throw new SetUpException("Invalid Command Line Arguments : "+Arrays.toString(args));
		}
	}

	/**
	 * check the validity of the command line
	 * @throws CmdLineException
	 */
	protected void checkData() throws CmdLineException {
		if(n < 0) {
			throw new CmdLineException(this.parser,"Invalid number of subproblems", null);
		}
		if(b < 0) {
			throw new CmdLineException(this.parser,"Invalid number of betters", null);
		}
		if(maxDTime < 0 || maxSTime < 0 || maxBTime < 0) {
			throw new CmdLineException(this.parser,"Negative time is not allowed", null);
		}
	}

	final MockupSolver buildSolver(int maxTime) {
		final Random rnd = new Random(seed + MPI.COMM_WORLD.Rank());
		return solveAll ? new MockupSolver(rnd, maxTime) : new MockupOptSolver(rnd, maxTime);

	}
	
	@Override
	public IProcessEPS buildMaster() {
		return new MasterProcess<>(new Master(n, b, new Bounds(lowerBound, upperBound), buildSolver(maxDTime), buildSolver(maxBTime)));
	}

	@Override
	public IProcessEPS buildForeman() {
		return new ForemanProcess<>(new Foreman(new Bounds(lowerBound, upperBound)));
	}

	@Override
	public IProcessEPS buildWorker(int rank, int nranks) {
		return new WorkerProcess<>(new Worker(buildSolver(maxSTime), buildSolver(maxBTime)));
	}

}
