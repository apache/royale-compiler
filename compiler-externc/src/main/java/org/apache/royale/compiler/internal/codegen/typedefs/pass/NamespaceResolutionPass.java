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
import org.apache.royale.compiler.internal.codegen.typedefs.DummyNode;
import org.apache.royale.compiler.problems.VariableHasNoTypeDeclarationProblem;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.StaticSourceFile;

public class NamespaceResolutionPass extends AbstractCompilerPass
{

    public NamespaceResolutionPass(ReferenceModel model,
            AbstractCompiler compiler)
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
            //System.out.println("-------------------------------------");
            //System.out.println(child.toStringTree());
            JSDocInfo comment = null;

            if (child.isVar())
            {
                Node name = child.getFirstChild();
                comment = child.getJSDocInfo();
                if (comment != null && comment.isConstant())
                {
                    if (name.getFirstChild() != null
                            && name.getFirstChild().isObjectLit())
                    {
                        // * @const
                        // var chrome = {}:
                        // print(child);
                        model.addClass(child, name.getQualifiedName());
                    }
                }
                else if (comment != null && comment.hasEnumParameterType())
                {
                    model.addEnum(child, name.getQualifiedName());
                }
            }
            else if (child.isExprResult())
            {
                Node first = child.getFirstChild();
                comment = first.getJSDocInfo();
                if (first.isQualifiedName())
                {
                    //print(name.getQualifiedName());
                }
                else if (first.isAssign())
                {
                    comment = first.getJSDocInfo();

                    Node firstAssignChild = first.getFirstChild();
                    Node lastAssignChild = first.getLastChild();
                    if (lastAssignChild.isObjectLit())
                    {
                        if (comment == null)
                        {
                        	StaticSourceFile ssf = first.getStaticSourceFile();
                        	String source = getSourceCode(ssf, first.getLineno());
                        	DummyNode node = new DummyNode();
                        	String externName = getSourceFileName(ssf.getName(), model);
                        	node.setSourcePath(externName);
                        	node.setLine(first.getLineno());
                        	VariableHasNoTypeDeclarationProblem problem = new VariableHasNoTypeDeclarationProblem(node, source);
                        	model.problems.add(problem);
                        	return;
                        }
                        if (comment.getType() != null)
                        {
                            //print("Class "
                            //        + firstAssignChild.getQualifiedName());
                            model.addClass(first,
                                    firstAssignChild.getQualifiedName());
                        }
                        else if (comment.isConstant())
                        {
                            //print("Package "
                            //        + firstAssignChild.getQualifiedName());
                            model.addClass(first,
                                    firstAssignChild.getQualifiedName());
                        }
                        else if (comment.hasEnumParameterType())
                        {
                            err(first);
                            model.addEnum(first,
                                    firstAssignChild.getQualifiedName());
                        }
                        else
                        {
                            err("Unhandled expression result:");
                            err(child);
                        }
                    }
                }
            }
        }
    }
}
