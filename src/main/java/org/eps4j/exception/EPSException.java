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
package org.eps4j.exception;

import mpi.MPI;

public final class EPSException extends Exception {

	private static final long serialVersionUID = 1054257488847222620L;

	public final static String format(String message) {
		return String.format("<%d> %s", MPI.COMM_WORLD.Rank(), message);
	}
	
	public EPSException() {
		super();
	}

	public EPSException(String message) {
		super(format(message));
	}

	public EPSException(Throwable cause) {
		super(format(""), cause);
	}

	public EPSException(String message, Throwable cause) {
		super(format(message), cause);
	}

	public EPSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(format(message), cause, enableSuppression, writableStackTrace);
	}

}
