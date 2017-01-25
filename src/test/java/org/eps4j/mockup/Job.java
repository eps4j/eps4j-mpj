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

import org.eps4j.specs.msg.IJob;

class Job implements IJob {

	private static final long serialVersionUID = 4574258919140892287L;
	public int id;

	public Job(int id) {
		super();
		this.id = id;
	}


	public int getID() {
		return id;
	}

	@Override
	public String toString() {
		return "Job:"+String.valueOf(id);
	}
}