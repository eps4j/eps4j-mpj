/**
 * This file is part of eps4j-core, http://github.com/eps4j/eps4j-core
 *
 * Copyright (c) 2017, Arnaud Malapert, Université Nice Sophia Antipolis. All rights reserved.
 *
 * Licensed under the BSD 3-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.eps4j;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class HelloTest{

    public HelloTest() {}

    @Test(timeOut = 60000)
    public void test1() {
        assertEquals(2, 2);
    }
    
    @Test(timeOut = 60000)
    public void test10() {
        assertEquals(10, 10);
    }

    public static void main(String[] args) {
        HelloWorld.main(args);
    }
}
