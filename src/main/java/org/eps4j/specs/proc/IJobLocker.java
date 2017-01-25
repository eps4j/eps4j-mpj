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

import org.eps4j.specs.msg.IJob;


/**
 * an interface for objects controlling the job locking (for heuristic and portfolio purposes)
 */
public interface IJobLocker<J extends IJob> {

  /**
   * the JobSelector can be asked to return if a worker locks a job.
   * A locked job is not available for the other workers
   *
   * @param the next job assigned to the worker
   * @param rank of the worker
   * @return <code>true</code> if the worker locks the job.
   */
  boolean lockJob(J job, int rank);
  
}