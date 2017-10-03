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
import org.apache.royale.compiler.internal.codegen.as.TestPackage;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * This class tests the production of 'goog' JavaScript for AS package.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogPackage extends TestPackage
{
    @Override
    @Test
    public void testPackage_Simple()
    {
        IFileNode node = compileAS("package{}");
        asBlockWalker.visitFile(node);
        assertOut("");
    }

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
        // does JS need a implicit constructor function? ... always?
        // All class nodes in AST get either an implicit or explicit constructor
        // this is an implicit and the way I have the before/after handler working
        // with block disallows implicit blocks from getting { }

        // (erikdebruin) the constuctor IS the class definition, in 'goog' JS,
        //               therefor we need to write out implicit constructors 
        //               (if I understand the term correctly)

        IFileNode node = compileAS("package {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('A');\n\n/**\n * @constructor\n */\nA = function() {\n};");
    }

    @Override
    @Test
    public void testPackageQualified_Class()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('foo.bar.baz.A');\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n};");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBody()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){}}}");
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('foo.bar.baz.A');\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n};");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBodyMethodContents()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){if (a){for (var i:Object in obj){doit();}}}}}");
        asBlockWalker.visitFile(node);
        assertOut("goog.provide('foo.bar.baz.A');\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n\tvar self = this;\n\tif (a) {\n\t\tfor (var /** @type {Object} */ i in obj) {\n\t\t\tdoit();\n\t\t}\n\t}\n};");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
