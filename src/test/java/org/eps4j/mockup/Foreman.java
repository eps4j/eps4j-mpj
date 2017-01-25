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

import org.eps4j.EPSComm;
import org.eps4j.specs.actors.IForemanEPS;
import org.eps4j.specs.msg.IBetter;
import org.eps4j.specs.msg.IJob;

public class Foreman implements IForemanEPS<IBetter, IJob> {

	private Bounds bounds;
	
	public Foreman(Bounds bounds) {
		super();
		this.bounds = bounds;
	}

	@Override
	public boolean hasEnded() {
		return bounds.isSolved();
	}


	@Override
	public boolean recordCollect(IJob job, IBetter better) {
		bounds.recordBounds((Bounds) better);
		return true;
	}
	
	@Override
	public void recordCollect(IBetter better) {
		bounds.recordBounds((Bounds) better);
	}
	
	@Override
	public void recordBetter(IBetter better) {
		bounds.recordBounds((Bounds) better);
	}

	@Override
	public void recordBetter(IJob job, IBetter better) {
		bounds.recordBounds((Bounds) better);	
	}

	@Override
	public IBetter getBetter() {
		return bounds;
	}

	@Override
	public void tearDown() {
		EPSComm.LOGGER.info(bounds.toString());
	}
}