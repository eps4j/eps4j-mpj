/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.eps4j.proc;

import java.io.Serializable;

import org.eps4j.specs.msg.IBetter;
import org.eps4j.specs.msg.IJob;

public final class JobMsg<J extends IJob, B extends IBetter> implements Serializable {

    private static final long serialVersionUID = 4574258919140892287L;

    /** A job defines a subproblem */
    public final J job;

    /** A better contains useful information about the search process */
    public final B better;

    /** The next cutoff of a parallel restart strategy */
    public final int limit;

    public JobMsg(J job, B better) {
        this(job, better, -1);
    }

    public JobMsg(J job, B better, int limit) {
        super();
        this.job = job;
        this.better = better;
        this.limit = limit;
    }

    public J getJob() {
        return job;
    }

    public B getBetter() {
        return better;
    }


    public int getLimit() {
        return limit;
    }


    @Override
    public String toString() {
        return "[job=" + job + ", better=" + better + ", limit=" + limit + "]";
    }

}