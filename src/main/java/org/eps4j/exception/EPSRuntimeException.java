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

public class EPSRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 3001051573301253562L;

	public EPSRuntimeException() {}

	public EPSRuntimeException(String message) {
		super(EPSException.format(message));
	}

	public EPSRuntimeException(Throwable cause) {
		super(cause);
	}

	public EPSRuntimeException(String message, Throwable cause) {
		super(EPSException.format(message), cause);
	}

	public EPSRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(EPSException.format(message), cause, enableSuppression, writableStackTrace);
	}

}
