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

package org.apache.royale.compiler.internal.codegen.typedefs.pass;

import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.JSDocInfo;
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
        return n.isRoot() || n.isNormalBlock() || n.isScript();
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
                    JSDocInfo jsDocInfo = first.getJSDocInfo();
                    if (jsDocInfo != null && jsDocInfo.hasTypedefType())
                    {
                        // this is a typedef, and not a member. it was
                        // already handled during the collect types pass.
                        continue;
                    }
                    else if(jsDocInfo != null 
                            && (jsDocInfo.getParameterCount() > 0
                                || jsDocInfo.getReturnType() != null))
                    {
                        // instance or static method that isn't declared as a
                        // function, but has @param or @returns
                        visitMethodFromJSDoc(t, first);
                    }
                    else
                    {
                        visitGetProp(t, first);
                    }
                }
            }
        }
    }

    // n == ASSIGN
    private void visitMethod(NodeTraversal t, Node n)
    {
        String qName = n.getFirstChild().getQualifiedName();

        if (n.getFirstChild().isGetProp())
        {
            int protoType = qName.indexOf(".prototype.");
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
            err("visitMethod() non impl");
            log(n);
        }
    }
    private void visitMethodFromJSDoc(NodeTraversal t, Node n)
    {
        String qName = n.getQualifiedName();

        if (n.isGetProp())
        {
            int protoType = qName.indexOf(".prototype.");
            if (protoType != -1)
            {
                String className = qName.substring(0, protoType);
                String memberName = qName.substring(protoType + 11,
                        qName.length());
                model.addMethod(n, className, memberName);
            }
            else
            {
                String className = qName.substring(0, qName.lastIndexOf("."));
                String memberName = qName.substring(qName.lastIndexOf(".") + 1,
                        qName.length());
                model.addStaticMethod(n, className, memberName);
            }
        }
        else if (n.isName())
        {
            err("visitMethod() non impl");
            log(n);
        }
    }

    private void visitGetProp(NodeTraversal t, Node n)
    {
        String qualifiedName = n.getQualifiedName();

        log("visitGetProp [" + qualifiedName + "]");

        int protoType = qualifiedName.indexOf(".prototype.");
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
