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

package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.TestGlobalConstants;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestGoogGlobalConstants extends TestGlobalConstants
{
    @Override
    @Test
    public void testInfinity()
    {
        IVariableNode node = getField("public var a:Number = Infinity;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @type {number}\n */\nRoyaleTest_A.prototype.a = Infinity");
    }

    @Override
    @Test
    public void testNegativeInfinity()
    {
        IVariableNode node = getField("public var a:Number = -Infinity;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @type {number}\n */\nRoyaleTest_A.prototype.a = -Infinity");
    }

    @Override
    @Test
    public void testNaN()
    {
        IVariableNode node = getField("public var a:Number = NaN;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @type {number}\n */\nRoyaleTest_A.prototype.a = NaN");
    }

    @Override
    @Test
    public void testUndefined()
    {
        IVariableNode node = getField("public var a:* = undefined;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @type {*}\n */\nRoyaleTest_A.prototype.a = undefined");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
