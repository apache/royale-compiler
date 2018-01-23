/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.codegen.js.sourcemaps;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.test.SourceMapTestBase;
import org.apache.royale.compiler.tree.as.IVariableNode;

import org.junit.Test;

public class TestSourceMapGlobalClasses extends SourceMapTestBase
{
    @Test
    public void testArrayConstCaseInsensitive()
    {
        IVariableNode node = getVariable("var a:Number = Array.CASEINSENSITIVE");
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 1
        assertMapping(node, 0, 15, 0, 30, 0, 31);    // 1
    }

    @Test
    public void testArrayConstNumeric()
    {
        IVariableNode node = getVariable("var a:Number = Array.NUMERIC");
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 16
        assertMapping(node, 0, 15, 0, 30, 0, 32);    // 16
    }

    @Test
    public void testIntConstMaxValue()
    {
        IVariableNode node = getVariable("var a:Number = int.MAX_VALUE");
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 2147483648
        assertMapping(node, 0, 15, 0, 30, 0, 40);    // 2147483648
    }

    @Test
    public void testIntConstMinValue()
    {
        IVariableNode node = getVariable("var a:Number = int.MIN_VALUE");
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = -2147483648
        assertMapping(node, 0, 15, 0, 30, 0, 41);    // -2147483648
    }

    @Test
    public void testUintConstMaxValue()
    {
        IVariableNode node = getVariable("var a:Number = uint.MAX_VALUE");
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 4294967295
        assertMapping(node, 0, 15, 0, 30, 0, 40);    // 4294967295
    }

    @Test
    public void testUintConstMinValue()
    {
        IVariableNode node = getVariable("var a:Number = uint.MIN_VALUE");
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 0
        assertMapping(node, 0, 15, 0, 30, 0, 31);    // 0
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
