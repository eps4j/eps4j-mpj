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

import java.util.List;
import java.util.Random;

import org.eps4j.specs.msg.IJob;
import org.eps4j.specs.proc.IJobSelector;

public final class RandomJobSelector<J extends IJob> implements IJobSelector<J> {

	private final Random rnd;
	
	public RandomJobSelector(long seed) {
		rnd = new Random(seed);
	}

	@Override
	public int selectJob(List<J> jobList, int rank) {
		return rnd.nextInt(jobList.size());
	}
}
