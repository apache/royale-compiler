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

package org.apache.flex.compiler.internal.codegen.externals.pass;

import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

public class CollectTypesPass implements CompilerPass, Callback
{
    protected AbstractCompiler compiler;
    private ReferenceModel model;

    public CollectTypesPass(ReferenceModel model, AbstractCompiler compiler)
    {
        this.model = model;
        this.compiler = compiler;
    }

    @Override
    public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n,
            Node parent)
    {
        return true;
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent)
    {
        JSDocInfo jsDoc = n.getJSDocInfo();
        if (jsDoc != null)
        {
            if (n.isVar())
            {
                visitVar(t, n, jsDoc);
            }
            else if (n.isFunction())
            {
                visitFunction(t, n, jsDoc);
            }
            else if (n.isAssign())
            {
                if (n.getFirstChild().isGetProp()
                        && n.getChildAtIndex(1).isFunction())
                {
                    // instance or static method
                    visitMethod(t, n, jsDoc);
                }
            }

        }

    }

    private void visitVar(NodeTraversal t, Node n, JSDocInfo jsDoc)
    {

        Node first = n.getFirstChild();
        if (first.isName())
        {
            Node second = first.getFirstChild();
            if (second != null && second.isObjectLit())
            {
                if (jsDoc.isConstant())
                {
                    // * @const
                    // var Math = {};
                    model.addFinalClass(n, n.getFirstChild().getQualifiedName());
                }
            }
            else if (jsDoc.isConstructor())
            {
                /*
                 VAR 241 [jsdoc_info: JSDocInfo] [source_file: [es5]] [length: 29]
                    NAME JSONType 241 [source_file: [es5]] [length: 8]
                        FUNCTION  241 [source_file: [es5]] [length: 13]
                            NAME  241 [source_file: [es5]] [length: 13]
                            PARAM_LIST 241 [source_file: [es5]] [length: 2]
                            BLOCK 241 [source_file: [es5]] [length: 2]
                 */
                // * @constructor
                // var JSONType = function() {};
                Node name = n.getFirstChild();
                if (name.getFirstChild().isFunction())
                {
                    model.addClass(n, name.getString());
                }
                //System.err.println(n.toStringTree());
            }
            else
            {
                if (jsDoc.isConstant())
                {
                    // * @const
                    // var Infinity;
                    model.addConstant(n, n.getFirstChild().getQualifiedName());
                }
                else if (jsDoc.getTypedefType() != null)
                {
                    // * @typedef {{prp(foo)}}
                    // var MyStrcut;
                    JSTypeExpression typedefType = jsDoc.getTypedefType();
                    System.out.println("@typedef "
                            + n.getFirstChild().getString());
                    System.out.println(typedefType);

                    JSType jsReturnType = typedefType.evaluate(null,
                            compiler.getTypeRegistry());
                    if (jsReturnType.isRecordType())
                    {
                        // property map of JSType
                    }

                    model.addClass(n, n.getFirstChild().getQualifiedName());

                    //System.out.println("   : " + jsReturnType);
                }
            }
        }

    }

    private void visitFunction(NodeTraversal t, Node n, JSDocInfo jsDoc)
    {
        if (jsDoc.isConstructor())
        {
            Node name = n.getChildAtIndex(0);
            //System.out.println("Class " + name.getString());
            //System.out.println(n.toStringTree());

            model.addClass(n, name.getQualifiedName());
        }
        else if (jsDoc.isInterface())
        {
            Node name = n.getChildAtIndex(0);
            //System.out.println("Interface " + name.getString());

            model.addInterface(n, name.getQualifiedName());
        }
        else
        {
            // XX Global function parseInt(num, base)
            //System.out.println(n.toStringTree());
            Node name = n.getChildAtIndex(0);
            //System.out.println("Function " + name.getString());

            model.addFunction(n, name.getQualifiedName());
        }
    }

    private void visitMethod(NodeTraversal t, Node n, JSDocInfo jsDoc)
    {

    }

    @Override
    public void process(Node externs, Node root)
    {
        NodeTraversal.traverseRoots(compiler, this, externs, root);
    }

}
