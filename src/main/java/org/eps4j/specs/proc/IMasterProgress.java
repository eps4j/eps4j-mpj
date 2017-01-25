/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.eps4j.specs.proc;

/**
 * Callback mechanism used by the master process to notify the master EPS. 
 * @author Arnaud Malapert
 *
 */
public interface IMasterProgress {

    /**   
     * Indicates whether the foreman requested that the search be stopped. 
     * @return true if the user has requested that the search is stopped.
     */
    boolean isStopped();

}