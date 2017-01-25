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
package org.eps4j.specs.proc;

import java.util.List;

import org.eps4j.specs.msg.IJob;


/**
 * an interface for objects controlling the selection of a job (for heuristic purposes)
 */
public interface IJobSelector<J extends IJob> {

  /**
   * the JobSelector can be asked to return a job for a worker
   *
   * @param jobList non empty and unmodifiable list of jobs
   * @param rank of the worker
   * @return the index of a job that must be executed by a worker.
   */
  int selectJob(List<J> jobList, int rank);
  
}
