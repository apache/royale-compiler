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

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.junit.Test;

/**
 * This class tests the production of 'AMD' JavaScript output.
 * <p>
 * Note; this is a complete prototype more used in figuring out where
 * abstraction and indirection is needed concerning the AS -> JS translations.
 * 
 * @author Michael Schmalle
 */
public class TestAMDEmitter extends ASTestBase
{
    // TODO (mschmalle) these tests were all ignored... ?
    
    @Test
    public void testSimple()
    {
//        String code = "package com.example.components {"
//                + "import org.apache.royale.html.staticControls.TextButton;"
//                + "public class MyTextButton extends TextButton {"
//                + "public function MyTextButton() {if (foo() != 42) { bar(); } }"
//                + "private var _privateVar:String = \"do \";"
//                + "public var publicProperty:Number = 100;"
//                + "public function myFunction(value: String): String{"
//                + "return \"Don't \" + _privateVar + value; }";
//        IFileNode node = compileAS(code);
//        asBlockWalker.visitFile(node);
//        assertOut("package com.example.components {\n\tpublic class MyTextButton extends TextButton {\n\t\tcom.example.components.MyTextButton = function() {\n\t\t\tif (foo() != 42) {\n\t\t\t\tbar();\n\t\t\t}\n\t\t}\n\t\tprivate var _privateVar:String = \"do \";\n\t\tpublic var publicProperty:Number = 100;\n\t\tcom.example.components.MyTextButton.prototype.myFunction = function(value) {\n\t\t\treturn \"Don't \" + _privateVar + value;\n\t\t}\n\t}\n}");
    }

    @Test
    public void testSimpleMethod()
    {
//        IFunctionNode node = getMethod("function method1():void{\n}");
//        asBlockWalker.visitFunction(node);
//        assertOut("A.prototype.method1 = function() {\n}");
    }

    @Test
    public void testSimpleParameterReturnType()
    {
//        IFunctionNode node = getMethod("function method1(bar:int):int{\n}");
//        asBlockWalker.visitFunction(node);
//        assertOut("A.prototype.method1 = function(bar) {\n}");
    }

    @Test
    public void testSimpleMultipleParameter()
    {
//        IFunctionNode node = getMethod("function method1(bar:int, baz:String, goo:A):void{\n}");
//        asBlockWalker.visitFunction(node);
//        assertOut("A.prototype.method1 = function(bar, baz, goo) {\n}");
    }

    @Test
    public void testDefaultParameter()
    {
//        /*
//         foo.bar.A.method1 = function(p1, p2, p3, p4) {
//            if (arguments.length < 4) {
//                if (arguments.length < 3) {
//                    p3 = 3;
//                }
//                p4 = 4;
//            }
//            return p1 + p2 + p3 + p4;
//         }
//         */
//        IFunctionNode node = getMethod("function method1(p1:int, p2:int, p3:int = 3, p4:int = 4):int{return p1 + p2 + p3 + p4;}");
//        asBlockWalker.visitFunction(node);
//        assertOut("A.prototype.method1 = function(p1, p2, p3, p4) {\n\tif (arguments.length < 4) "
//                + "{\n\t\tif (arguments.length < 3) {\n\t\t\tp3 = 3;\n\t\t}\n\t\tp4 = 4;\n\t}"
//                + "\n\treturn p1 + p2 + p3 + p4;\n}");
    }

    @Test
    public void testDefaultParameter_Body()
    {
//        /*
//        foo.bar.A.method1 = function(bar, bax) {
//            if (arguments.length < 2) {
//                if (arguments.length < 1) {
//                    bar = 42;
//                }
//                bax = 4;
//            }
//        }
//        */
//        IFunctionNode node = getMethod("function method1(bar:int = 42, bax:int = 4):void{if (a) foo();}");
//        asBlockWalker.visitFunction(node);
//        assertOut("A.prototype.method1 = function(bar, bax) {\n\tif (arguments.length < 2) {\n\t\t"
//                + "if (arguments.length < 1) {\n\t\t\tbar = 42;\n\t\t}\n\t\tbax = 4;\n\t}\n\t"
//                + "if (a)\n\t\tfoo();\n}");
    }

    @Test
    public void testDefaultParameter_NoBody_Alternate()
    {
//        /*
//        foo.bar.A.method1 = function(bar, bax) {
//            if (arguments.length < 2) {
//                if (arguments.length < 1) {
//                    bar = 42;
//                }
//                bax = 4;
//            }
//        }
//        */
//        IFunctionNode node = getMethod("function method1(bar:int = 42, bax:int = 4):void{\n}");
//        asBlockWalker.visitFunction(node);
//        assertOut("A.prototype.method1 = function(bar, bax) {\n\tif (arguments.length < 2) {\n\t\t"
//                + "if (arguments.length < 1) {\n\t\t\tbar = 42;\n\t\t}\n\t\tbax = 4;\n\t}\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new AMDBackend();
    }
}
