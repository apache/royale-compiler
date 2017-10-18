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
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of AMD JavaScript for the test project, class
 * A.
 * 
 * @author Michael Schmalle
 */
@Ignore
public class TestAMDClassA extends AMDTestBase
{
    // !!! the errors have to do with how I change 'this' on member expressions

    //--------------------------------------------------------------------------
    // Class A
    //--------------------------------------------------------------------------

    @Test
    public void test_field_msg()
    {
        IVariableNode vnode = findField("_msg", classNode);
        asBlockWalker.visitVariable(vnode);
        assertOut("_msg$1: {\n\tvalue:0,\n\twritable:true\n}");
    }

    @Test
    public void test_constructor()
    {
        IFunctionNode vnode = findFunction("A", classNode);
        asBlockWalker.visitFunction(vnode);
        assertOut("function A(msg) {\n\tthis.msg = msg;\n}");
    }

    @Test
    public void test_get_msg()
    {
        IGetterNode node = findGetter("msg", classNode);
        asBlockWalker.visitGetter(node);
        assertOut("msg: {\n\tget: function msg$get() {\n\t\treturn String(this._msg$1);\n\t},"
                + "\n\tset: function msg$set(value) {\n\t\tthis._msg$1 = parseInt(value, 10);\n\t}\n}");
    }

    @Test
    public void test_set_msg()
    {
        ISetterNode node = findSetter("msg", classNode);
        asBlockWalker.visitSetter(node);
        assertOut("msg: {\n\tget: function msg$get() {\n\t\treturn String(this._msg$1);\n\t},"
                + "\n\tset: function msg$set(value) {\n\t\tthis._msg$1 = parseInt(value, 10);\n\t}\n}");
    }

    @Test
    public void test_secret()
    {
        IFunctionNode vnode = findFunction("secret", classNode);
        asBlockWalker.visitFunction(vnode);
        assertOut("secret$1: function secret(n) {\n\treturn this.msg + n;\n}");
    }

    @Test
    public void test_foo()
    {
        IFunctionNode vnode = findFunction("foo", classNode);
        asBlockWalker.visitFunction(vnode);
        assertOut("foo: function foo(x) {\n\treturn this.secret$1(A.bar(x));\n}");
    }

    @Test
    public void test_baz()
    {
        IFunctionNode vnode = findFunction("baz", classNode);
        asBlockWalker.visitFunction(vnode);
        assertOut("baz: function baz() {\n\tvar tmp = AS3.bind(this, "
                + "\"secret$1\");\n\treturn tmp(\"-bound\");\n}");
    }

    @Test
    public void test_bar()
    {
        IFunctionNode vnode = findFunction("bar", classNode);
        asBlockWalker.visitFunction(vnode);
        assertOut("bar: function bar(x) {\n\treturn x + 1;\n}");
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
        return "com.acme.A";
    }
}

/*

--------------------------------------------------------------------------------
02-07-2013
Current Production of visitFile()
--------------------------------------------------------------------------------

define(["exports", "runtime/AS3", "classes/I", "classes/String", "classes/parseInt", "classes/trace"], function($exports, AS3, I, String, parseInt, trace) {
    "use strict"; 
    AS3.compilationUnit($exports, function($primaryDeclaration){
        function A(msg) {
            this.msg = msg;
        }
        $primaryDeclaration(AS3.class_({
            package_: "com.acme",
            class_: "A",
            implements_: [
                I
            ],
            members: {
                constructor: A,
                _msg$1: {
                    value:0,
                    writable:true
                },
                msg: {
                    get: function msg$get() {
                        return String(this._msg$1);
                    },
                    set: function msg$set(value) {
                        this._msg$1 = parseInt(value, 10);
                    }
                },
                secret$1: function secret(n) {
                    return this.msg + n;
                },
                foo: function foo(x) {
                    return this.secret$1(A.bar(x));
                },
                baz: function baz() {
                    var tmp = AS3.bind(this, "secret$1");
                    return tmp("-bound");
                }
            },
            staticMembers: {
                bar: function bar(x) {
                    return x + 1;
                }
            }
        }));
        trace("Class A is initialized!");
    });
});

*/
