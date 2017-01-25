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
package org.eps4j.specs.actors;

import org.chocosolver.pf4cs.IUpDown;
import org.eps4j.specs.msg.IBetter;
import org.eps4j.specs.msg.IJob;

/**
 *
 * @param <B> Better concrete class
 * @param <J> job concrete class
 */
public interface IForemanEPS<B extends IBetter, J extends IJob> extends IUpDown {

        
	boolean hasEnded();

	/**
	 * @return <code>true</code> if job is finished.
	 */ 
	boolean recordCollect(J job, B better);

	void recordCollect(B better);

	void recordBetter(J job, B better);

	void recordBetter(B better);

	B getBetter();

}