/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.eps4j.mockup;

import org.eps4j.specs.actors.IMasterEPS;
import org.eps4j.specs.proc.IBetterObserver;
import org.eps4j.specs.proc.IJobObserver;
import org.eps4j.specs.proc.IMasterProgress;

class Master implements IMasterEPS<Job, Bounds> {

    private final int n;
    private final int b;

    private final MockupSolver decompSolver;
    private final MockupSolver betterSolver;

    private IMasterProgress masterProgress;

    private final Bounds bounds;


    public Master(int n, int b, Bounds bounds, MockupSolver decompSolver, MockupSolver betterSolver) {
        super();
        this.n = n;
        this.b = b;
        this.bounds = bounds;
        this.decompSolver = decompSolver;
        this.betterSolver = betterSolver;
    }

    @Override
    public void setMasterProgress(IMasterProgress masterProgress) {
        this.masterProgress = masterProgress;
    }
    
    
    @Override
    public void tearDown() {}


    @Override
    public Bounds decompose(IJobObserver<Job, Bounds> jobObserver, int workers) {
        for (int i = 1; i <= n; i++) {
            if(masterProgress.isStopped()) {
                break;
            }
            decompSolver.sleep();
            jobObserver.notifyJob(new Job(i), bounds);
        }
        return bounds;
    }

    @Override
    public Bounds diversify(IBetterObserver<Bounds> betterObserver) {
        for (int i = 1; i <= b; i++) {
            if(masterProgress.isStopped()) {
                break;
            } 
            bounds.recordBounds(betterSolver.solve(bounds));
            betterObserver.notifyBetter(bounds);
        }
        return bounds;
    }

    @Override
    public void notifyBetter(Bounds helper) {
        bounds.recordBounds((Bounds) helper);
    }




}