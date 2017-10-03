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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * This class tests the production of valid ActionScript3 code for Package
 * production.
 * 
 * @author Michael Schmalle
 */
public class TestPackage extends ASTestBase
{
    //--------------------------------------------------------------------------
    // Package
    //--------------------------------------------------------------------------

    @Test
    public void testPackage_Simple()
    {
        IFileNode node = compileAS("package{}");
        asBlockWalker.visitFile(node);
        assertOut("package {\n}");
    }

    @Test
    public void testPackage_SimpleName()
    {
        IFileNode node = compileAS("package foo {}");
        asBlockWalker.visitFile(node);
        assertOut("package foo {\n}");
    }

    @Test
    public void testPackage_Name()
    {
        IFileNode node = compileAS("package foo.bar.baz {}");
        asBlockWalker.visitFile(node);
        assertOut("package foo.bar.baz {\n}");
    }

    @Test
    public void testPackageSimple_Class()
    {
        IFileNode node = compileAS("package {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOut("package {\n\tpublic class A {\n\t}\n}");
    }

    @Test
    public void testPackageQualified_Class()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOut("package foo.bar.baz {\n\tpublic class A {\n\t}\n}");
    }

    @Test
    public void testPackageQualified_ClassBody()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){}}}");
        asBlockWalker.visitFile(node);
        assertOut("package foo.bar.baz {\n\tpublic class A {\n\t\tpublic function A() {\n\t\t}\n\t}\n}");
    }

    @Test
    public void testPackageQualified_ClassBodyMethodContents()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){if (a){for each(var i:Object in obj){doit();}}}}}");
        asBlockWalker.visitFile(node);
        assertOut("package foo.bar.baz {\n\tpublic class A {\n\t\tpublic function A() {\n\t\t\t"
                + "if (a) {\n\t\t\t\tfor each (var i:Object in obj) {\n\t\t\t\t\tdoit();\n\t\t\t\t}\n\t\t\t}\n\t\t}\n\t}\n}");
    }

    // TODO (mschmalle) implement Import unit tests for as
    @Test
    public void testPackage_Import()
    {
//        IFileNode node = compileAS("package{import foo.bar.Baz;}");
//        asBlockWalker.visitFile(node);
//        assertOut("package {\nimport foo.bar.Baz;}");
    }
}
