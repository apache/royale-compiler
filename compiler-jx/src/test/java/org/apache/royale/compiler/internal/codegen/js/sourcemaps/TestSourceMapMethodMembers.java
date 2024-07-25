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
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

public class TestSourceMapMethodMembers extends SourceMapTestBase
{
    @Test
    public void testMethod()
    {
        IFunctionNode node = getMethod("function foo(){}");
        asBlockWalker.visitFunction(node);
        //RoyaleTest_A.prototype.foo = function() {\n}
        assertMapping(node, 0, 9, 0, 0, 0, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 9, 0, 23, 0, 26); // foo
        assertMapping(node, 0, 0, 0, 26, 0, 37); // = function
        assertMapping(node, 0, 12, 0, 37, 0, 38); // (
        assertMapping(node, 0, 13, 0, 38, 0, 39); // )
        assertMapping(node, 0, 14, 0, 40, 0, 41); // {
        assertMapping(node, 0, 15, 1, 0, 1, 1);   // }
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
