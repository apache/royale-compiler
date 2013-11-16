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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogPackage;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSPackage extends TestGoogPackage
{
    @Override
    public void setUp()
    {
    	project = new FlexJSProject(workspace);
        super.setUp();
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
        assertOut("/**\n * A\n *\n * @fileoverview\n *\n * @suppress {checkTypes}\n */\n\ngoog.provide('A');\n\n\n\n/**\n * @constructor\n */\nA = function() {\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nA.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'A'}] };\n");
    }

    @Override
    @Test
    public void testPackageQualified_Class()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOut("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nfoo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBody()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){}}}");
        asBlockWalker.visitFile(node);
        assertOut("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nfoo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBodyMethodContents()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){if (a){for (var i:Object in obj){doit();}}}}}");
        asBlockWalker.visitFile(node);
        assertOut("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n  if (a) {\n    for (var /** @type {Object} */ i in obj) {\n      doit();\n    }\n  }\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nfoo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
