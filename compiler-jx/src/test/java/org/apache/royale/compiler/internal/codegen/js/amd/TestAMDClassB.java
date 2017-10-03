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

package org.apache.royale.compiler.internal.codegen.js.amd;

import org.apache.royale.compiler.internal.test.AMDTestBase;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of AMD JavaScript for the test project, class
 * B.
 * 
 * @author Michael Schmalle
 */
@Ignore
public class TestAMDClassB extends AMDTestBase
{
    //--------------------------------------------------------------------------
    // Class B
    //--------------------------------------------------------------------------
    
    // XXX (mschmalle) () get back to this when more work is done
    @Test
    public void test_nowPlusOne()
    {
//        IFunctionNode vnode = findFunction("nowPlusOne", classNode);
//        asBlockWalker.visitFunction(vnode);
//        assertOut("nowPlusOne: function nowPlusOne() {\n\treturn new Date(B.now.getTime() + 60 * 60 * 1000);\n}");
    }

    @Test
    public void test_constructor()
    {
        IFunctionNode vnode = findFunction("B", classNode);
        asBlockWalker.visitFunction(vnode);
        assertOut("function B(msg, count) {\n\tthis.barfoo = (A._ || A._$get()).bar(3);"
                + "\n\tSuper.call(this, msg);\n\tthis.count = count;\n\ttrace(\"now: \" + B.now);\n}");
    }

    @Test
    public void test_count()
    {
        IVariableNode node = findField("count", classNode);
        asBlockWalker.visitVariable(node);
        assertOut("count: {\n\tvalue:0,\n\twritable:true\n}");
    }

    @Test
    public void test_override_foo()
    {
        IFunctionNode vnode = findFunction("foo", classNode);
        asBlockWalker.visitFunction(vnode);
        assertOut("foo: function foo(x) {\n\treturn this.foo$2(x + 2) + \"-sub\";\n}");
    }

    @Test
    public void test_now()
    {
        IVariableNode node = findField("now", classNode);
        asBlockWalker.visitVariable(node);
        assertOut("B.now = new Date()");
    }

    @Test
    public void test_file()
    {
        // not going to put this test production in until everything is concrete and agreed upon
        asBlockWalker.visitFile(fileNode);
        //assertOut("");
    }

    @Override
    protected String getTypeUnderTest()
    {
        return "com.acme.B";
    }

}
