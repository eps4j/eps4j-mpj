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
package org.eps4j;

import static org.testng.Assert.assertEquals;

import org.chocosolver.pf4cs.SetUpException;
import org.eps4j.EPSCmd.ArgsEPS;
import org.eps4j.specs.IFactoryEPS;
import org.eps4j.specs.IProcessEPS;
import org.testng.annotations.Test;

public class TestArgsEPS {


	public final static class FakeFactory implements IFactoryEPS {

		@Override
		public IProcessEPS buildMaster() {
			return null;
		}

		@Override
		public IProcessEPS buildForeman() {
			return null;
		}

		@Override
		public IProcessEPS buildWorker(int rank, int nranks) {
			return null;
		}
	}
	
	private final static String FACTORY_CLASSNAME = FakeFactory.class.getName();
	public TestArgsEPS() {}


	private void assertArgs(ArgsEPS args, int factArgs, int procArgs) {
		assertEquals(factArgs, args.getFactoryArgs().length, "Factory Args");
		assertEquals(procArgs, args.getProcessArgs().length, "Process Args");
	}

	private void assertEmpty(ArgsEPS args) {
		assertArgs(args, 0, 0);
	}


	@Test(timeOut = 60000)
	public void testEmptyArgs() throws SetUpException {
		final ArgsEPS args = new ArgsEPS(1);
		args.setUp(FACTORY_CLASSNAME,"--", "foo", "--", "--");
		assertEmpty(args);
		args.tearDown();
		
		args.setUp(FACTORY_CLASSNAME,"--", "foo");
		assertEmpty(args);
		args.tearDown();

		args.setUp(FACTORY_CLASSNAME,"--", "foo", "bar", "--");
		assertEmpty(args);
		args.tearDown();

		args.setUp(FACTORY_CLASSNAME, "--", "--", "--", "foo", "bar");
		assertEmpty(args);
		args.tearDown();

	}

	@Test(timeOut = 60000)
	public void testArgs() throws SetUpException {
		final ArgsEPS args = new ArgsEPS(1);
		args.setUp(FACTORY_CLASSNAME, "class", "factory_arg", "--", "master", "--", "foreman", "--");
		assertArgs(args, 2, 1);;
		args.tearDown();

		args.setUp(FACTORY_CLASSNAME, "--", "master", "--", "foreman", "--");
		assertArgs(args, 0, 1);;
		args.tearDown();

		args.setUp(FACTORY_CLASSNAME, "class", "factory1", "factory2");
		assertArgs(args, 3, 0);;
		args.tearDown();

		args.setUp(FACTORY_CLASSNAME, "--", "master", "--", "foreman", "--", "worker");
		assertArgs(args, 0, 1);;
		args.tearDown();

	}
	
}
