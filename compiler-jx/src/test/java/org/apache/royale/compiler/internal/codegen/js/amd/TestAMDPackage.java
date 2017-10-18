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

import java.io.IOException;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.TestPackage;
import org.apache.royale.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * This class tests the production of AMD JavaScript for AS package.
 * 
 * @author Michael Schmalle
 */
public class TestAMDPackage extends TestPackage
{

    @Override
    @Test
    public void testPackage_Simple()
    {
        IFileNode node = compileAS("package{}");
        asBlockWalker.visitFile(node);
        assertOut("");
    }

    @Override
    @Test
    public void testPackage_SimpleName()
    {
        IFileNode node = compileAS("package foo {}");
        asBlockWalker.visitFile(node);
        assertOut("");
    }

    @Override
    @Test
    public void testPackage_Name()
    {
        IFileNode node = compileAS("package foo.bar.baz {}");
        asBlockWalker.visitFile(node);
        assertOut("");
    }

    @Override
    @Test
    public void testPackageSimple_Class()
    {
        IFileNode node = compileAS("package {public class A{}}");
        asBlockWalker.visitFile(node);
        //assertOut("");
    }

    // XXX (mschmalle) ?
    @Test
    public void testPackageSimple_TestA() throws IOException
    {
    }

    @Override
    @Test
    public void testPackageQualified_Class()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{}}");
        asBlockWalker.visitFile(node);
        //assertOut("");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBody()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){}}}");
        asBlockWalker.visitFile(node);
        //assertOut("");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBodyMethodContents()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){if (a){for (var i:Object in obj){doit();}}}}}");
        asBlockWalker.visitFile(node);
        //assertOut("");
    }

    //@Test
    public void testMethod()
    {
        IFunctionNode node = getMethod("function foo(){}");
        asBlockWalker.visitFunction(node);
        assertOut("A.prototype.foo = function() {\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new AMDBackend();
    }

    protected IFileNode getFile(String code)
    {
        IFileNode node = compileAS(code);
        return node;
    }
}
