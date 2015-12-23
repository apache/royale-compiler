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
        assertOutWithMetadata("/**\n * A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('A');\n\n\n\n/**\n * @constructor\n */\nA = function() {\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nA.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'A'}] };\n");
    }

    @Override
    @Test
    public void testPackageQualified_Class()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nfoo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBody()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){}}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nfoo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n");
    }

    @Override
    @Test
    public void testPackageQualified_ClassBodyMethodContents()
    {
        IFileNode node = compileAS("package foo.bar.baz {public class A{public function A(){if (a){for (var i:Object in obj){doit();}}}}}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n * foo.bar.baz.A\n *\n * @fileoverview\n *\n * @suppress {checkTypes|accessControls}\n */\n\ngoog.provide('foo.bar.baz.A');\n\n\n\n/**\n * @constructor\n */\nfoo.bar.baz.A = function() {\n  if (a) {\n    for (var /** @type {Object} */ i in obj) {\n      doit();\n    }\n  }\n};\n\n\n/**\n * Metadata\n *\n * @type {Object.<string, Array.<Object>>}\n */\nfoo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n");
    }

    @Test
    public void testPackageQualified_ClassAndInternalClass()
    {
        IFileNode node = compileAS("package foo.bar.baz {\n" + 
        							  "public class A {\n" +
        							  "public function A(){\n" +
        							      "var internalClass:InternalClass = new InternalClass();\n" +
        							  "}}}\n" +
        							  "class InternalClass {\n" +
        							      "public function InternalClass(){}\n" +
        							  "}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
        		  " * foo.bar.baz.A\n" +
        		  " *\n" +
        		  " * @fileoverview\n" +
        		  " *\n" +
        		  " * @suppress {checkTypes|accessControls}\n" +
        		  " */\n" +
        		  "\n" +
        		  "goog.provide('foo.bar.baz.A');\n" +
        		  "\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @constructor\n" +
        		  " */\n" +
        		  "foo.bar.baz.A = function() {\n" +
        		  "  var /** @type {foo.bar.baz.A.InternalClass} */ internalClass = new foo.bar.baz.A.InternalClass();\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n/" +
        		  "**\n" +
        		  " * Metadata\n" +
        		  " *\n" +
        		  " * @type {Object.<string, Array.<Object>>}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n" +
        		  "\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @constructor\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass = function() {\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * Metadata\n" +
        		  " *\n" +
        		  " * @type {Object.<string, Array.<Object>>}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'foo.bar.baz.A.InternalClass'}] };\n");
    }

    @Test
    public void testPackageQualified_ClassAndInternalClassMethods()
    {
        IFileNode node = compileAS("package foo.bar.baz {\n" + 
        							  "public class A {\n" +
        							  "public function A(){\n" +
        							      "var internalClass:InternalClass = new InternalClass();\n" +
        							      "var myString:String = InternalClass.someString;\n" +
        							      "myString = InternalClass.someStaticFunction();\n" +
        							      "myString = internalClass.someMethod();\n" +
        							  "}}}\n" +
        							  "class InternalClass {\n" +
        							      "public function InternalClass(){\n" +
        							      "}\n" +
       							          "public static var someString:String = \"foo\";\n" +
    							          "public static function someStaticFunction():String { return \"bar\";}\n" +
    							          "public function someMethod():String { return \"baz\";}\n" +
        							  "}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
        		  " * foo.bar.baz.A\n" +
        		  " *\n" +
        		  " * @fileoverview\n" +
        		  " *\n" +
        		  " * @suppress {checkTypes|accessControls}\n" +
        		  " */\n" +
        		  "\n" +
        		  "goog.provide('foo.bar.baz.A');\n" +
        		  "\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @constructor\n" +
        		  " */\n" +
        		  "foo.bar.baz.A = function() {\n" +
        		  "  var /** @type {foo.bar.baz.A.InternalClass} */ internalClass = new foo.bar.baz.A.InternalClass();\n" +
        		  "  var /** @type {string} */ myString = foo.bar.baz.A.InternalClass.someString;\n" +
        		  "  myString = foo.bar.baz.A.InternalClass.someStaticFunction();\n" +
        		  "  myString = internalClass.someMethod();\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * Metadata\n" +
        		  " *\n" +
        		  " * @type {Object.<string, Array.<Object>>}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n" +
        		  "\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @constructor\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass = function() {\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @export\n" +
        		  " * @type {string}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass.someString = \"foo\";\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @export\n" +
        		  " * @return {string}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass.someStaticFunction = function() {\n" +
        		  "  return \"bar\";\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @export\n" +
        		  " * @return {string}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass.prototype.someMethod = function() {\n" +
        		  "  return \"baz\";\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * Metadata\n" +
        		  " *\n" +
        		  " * @type {Object.<string, Array.<Object>>}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'foo.bar.baz.A.InternalClass'}] };\n");
    }
    
    @Test
    public void testPackageQualified_ClassAndInternalGettersAndSetters()
    {
        IFileNode node = compileAS("package foo.bar.baz {\n" + 
        							  "public class A {\n" +
        							  "public function A(){\n" +
        							      "var internalClass:InternalClass = new InternalClass();\n" +
        							      "myString = internalClass.someString;\n" +
        							      "internalClass.someString = myString;\n" +
        							  "}\n" +
        							  "public function get myString():String {\n" +
        							  "    return null;\n" +
        							  "}\n" +
        							  "public function set myString(value:String):void {}\n" +
        							  "}}\n" +
        							  "class InternalClass {\n" +
        							      "public function InternalClass(){\n" +
        							      "}\n" +
       							          "public function get someString():String {\n" +
       							          "    return null;\n" +
       							          "}\n" +
    							          "public function set someString(value:String):void {}\n" +
        							  "}");
        asBlockWalker.visitFile(node);
        assertOutWithMetadata("/**\n" +
        		  " * foo.bar.baz.A\n" +
        		  " *\n" +
        		  " * @fileoverview\n" +
        		  " *\n" +
        		  " * @suppress {checkTypes|accessControls}\n" +
        		  " */\n" +
        		  "\n" +
        		  "goog.provide('foo.bar.baz.A');\n" +
        		  "\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @constructor\n" +
        		  " */\n" +
        		  "foo.bar.baz.A = function() {\n" +
        		  "  var /** @type {foo.bar.baz.A.InternalClass} */ internalClass = new foo.bar.baz.A.InternalClass();\n" +
        		  "  this.myString = internalClass.someString;\n" +
        		  "  internalClass.someString = this.myString;\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n" +
        		  "Object.defineProperties(foo.bar.baz.A.prototype, /** @lends {foo.bar.baz.A.prototype} */ {\n" +
                  "/** @export */\n" +
                  "myString: {\n" +
                  "get: /** @this {foo.bar.baz.A} */ function() {\n" +
                  "  return null;\n" +
                  "},\n" +
                  "set: /** @this {foo.bar.baz.A} */ function(value) {\n" +
                  "}}}\n" +
                  ");\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * Metadata\n" +
        		  " *\n" +
        		  " * @type {Object.<string, Array.<Object>>}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'foo.bar.baz.A'}] };\n" +
        		  "\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * @constructor\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass = function() {\n" +
        		  "};\n" +
        		  "\n" +
        		  "\n" +
        		  "Object.defineProperties(foo.bar.baz.A.InternalClass.prototype, /** @lends {foo.bar.baz.A.InternalClass.prototype} */ {\n" +
                  "/** @export */\n" +
                  "someString: {\n" +
                  "get: /** @this {foo.bar.baz.A.InternalClass} */ function() {\n" +
                  "  return null;\n" +
                  "},\n" +
                  "set: /** @this {foo.bar.baz.A.InternalClass} */ function(value) {\n" +
                  "}}}\n" +
                  ");\n" +
        		  "\n" +
        		  "\n" +
        		  "/**\n" +
        		  " * Metadata\n" +
        		  " *\n" +
        		  " * @type {Object.<string, Array.<Object>>}\n" +
        		  " */\n" +
        		  "foo.bar.baz.A.InternalClass.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'foo.bar.baz.A.InternalClass'}] };\n");
    }
    
    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
