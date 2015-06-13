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
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.Node;

public class AddMemberPass extends AbstractCompilerPass
{

    public AddMemberPass(ReferenceModel model, AbstractCompiler compiler)
    {
        super(model, compiler);
    }

    @Override
    public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n,
            Node parent)
    {
        return n.isBlock() || n.isScript();
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent)
    {
        for (Node child : n.children())
        {
            //log(child);

            if (child.isExprResult())
            {
                Node first = child.getFirstChild();

                if (first.isVar())
                {
                    // visitVar(t, n);
                }
                else if (first.isFunction())
                {
                    // visitFunction(t, n);
                }
                else if (first.isAssign())
                {
                    if (first.getFirstChild().isGetProp()
                            && first.getLastChild().isFunction())
                    {
                        // instance or static method
                        visitMethod(t, first);
                    }
                    else
                    {
                        // DOMException.INDEX_SIZE_ERR = 1;
                        // The first child of the assign is the GetProp node,
                        // if later you need the value, either change this or check
                        // for a parent assign node when creating the FieldReference
                        // which the value would be n.getLastChild()
                        // XXX visitStaticField(t, n);
                        //System.err.println(n.toStringTree());
                    }
                }
                else if (first.isGetProp())
                {
                    visitGetProp(t, first);
                }
            }

        }

        //        JSDocInfo jsDoc = n.getJSDocInfo();
        //        if (jsDoc != null)
        //        {
        //            if (n.isVar())
        //            {
        //                // visitVar(t, n);
        //            }
        //            else if (n.isFunction())
        //            {
        //                // visitFunction(t, n);
        //            }
        //            else if (n.isAssign())
        //            {
        //                if (n.getFirstChild().isGetProp()
        //                        && n.getLastChild().isFunction())
        //                {
        //                    // instance or static method
        //                    visitMethod(t, n);
        //                }
        //                else
        //                {
        //                    // DOMException.INDEX_SIZE_ERR = 1;
        //                    // The first child of the assign is the GetProp node,
        //                    // if later you need the value, either change this or check
        //                    // for a parent assign node when creating the FieldReference
        //                    // which the value would be n.getLastChild()
        //                    // XXX visitStaticField(t, n);
        //                    //System.err.println(n.toStringTree());
        //                }
        //            }
        //            else if (n.isGetProp())
        //            {
        //                visitGetProp(t, n);
        //            }
        //        }
    }

    // n == ASSIGN
    private void visitMethod(NodeTraversal t, Node n)
    {
        String qName = n.getFirstChild().getQualifiedName();

        if (n.getFirstChild().isGetProp())
        {
            int protoType = qName.indexOf(".prototype");
            if (protoType != -1)
            {
                String className = qName.substring(0, protoType);
                String memberName = qName.substring(protoType + 11,
                        qName.length());
                //log("Prototype:: className [" + className
                //        + "] memberName [" + memberName + "]");
                model.addMethod(n, className, memberName);
            }
            else
            {
                String className = qName.substring(0, qName.lastIndexOf("."));
                String memberName = qName.substring(qName.lastIndexOf(".") + 1,
                        qName.length());
                //log("className [" + className + "] memberName ["
                //        + memberName + "]");
                model.addStaticMethod(n, className, memberName);
            }
        }
        else if (n.getFirstChild().isName())
        {
            log(n);
        }
    }

    private void visitGetProp(NodeTraversal t, Node n)
    {
        String qualifiedName = n.getQualifiedName();

        log("visitGetProp [" + qualifiedName + "]");

        int protoType = qualifiedName.indexOf(".prototype");
        if (protoType != -1)
        {
            String className = qualifiedName.substring(0, protoType);
            String memberName = qualifiedName.substring(protoType + 11,
                    qualifiedName.length());
            //log("Prototype:: className [" + className
            //        + "] memberName [" + memberName + "]");
            model.addField(n, className, memberName);
        }
        else
        {
            String className = qualifiedName.substring(0,
                    qualifiedName.lastIndexOf("."));
            String memberName = qualifiedName.substring(
                    qualifiedName.lastIndexOf(".") + 1, qualifiedName.length());
            //log("className [" + className + "] memberName ["
            //        + memberName + "]");
            model.addStaticField(n, className, memberName);
        }
    }

}
