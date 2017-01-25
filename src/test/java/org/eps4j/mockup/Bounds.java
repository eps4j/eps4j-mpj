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

import java.util.Random;

import org.eps4j.EPSComm;
import org.eps4j.specs.msg.IBetter;

public class Bounds implements IBetter {

	private static final long serialVersionUID = 7888703471367058216L;

	//TODO immutable ?
	private int lowerBound;

	private int upperBound;

	public Bounds(int lowerBound, int upperBound) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public boolean isSolved() {
		return lowerBound >= upperBound;
	}

	public final int getLowerBound() {
		return lowerBound;
	}

	public final int getUpperBound() {
		return upperBound;
	}


	public synchronized void recordLowerBound(int lowerBound) {
		if(lowerBound > this.lowerBound) {
			this.lowerBound = lowerBound;
		}
	}

	public synchronized void recordUpperBound(int upperBound) {
		if(upperBound < this.upperBound) {
			this.upperBound = upperBound;
		}
	}

	public void recordBounds(Bounds bounds) {
		if(bounds != null) {
			recordLowerBound(bounds.lowerBound);
			recordUpperBound(bounds.upperBound);
		} else {
			EPSComm.LOGGER.severe("null bounds ignored");
		}
	}
	
	public final static int decrease(Random rnd, int value, int scale) {
		if(value == 0) return 0;
		else {
			final int play = value/scale + value % scale;
			return value-play + rnd.nextInt(play);
		}
	}
	
	public static Bounds decrease(Random rnd, Bounds bounds, int scale) {
		int lb = bounds.getLowerBound(); 
		if(lb == Integer.MIN_VALUE) lb++;
		return new Bounds(
				-decrease(rnd, -lb, scale),
				decrease(rnd, bounds.getUpperBound(), scale)
		);
	}
	public Bounds reduce(Random rnd) {
		return decrease(rnd, this, 1);
	}

	@Override
	public String toString() {
		return "Bounds [" + lowerBound + ", " + upperBound + "]";
	}
	
	

}
