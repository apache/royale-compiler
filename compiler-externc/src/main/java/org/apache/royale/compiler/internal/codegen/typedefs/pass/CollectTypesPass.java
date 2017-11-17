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

public class CollectTypesPass extends AbstractCompilerPass
{
    public CollectTypesPass(ReferenceModel model, AbstractCompiler compiler)
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
            if (child.isVar())
            {
                visitVar(child);
            }
            else if (child.isFunction())
            {
                visitFunction(child);
            }
            else if (child.isExprResult())
            {
                visitExprResult(child);
            }
        }
    }

    private void visitExprResult(Node child)
    {
        JSDocInfo comment = null;

        Node container = child.getFirstChild();
        if (container.isAssign())
        {
            comment = container.getJSDocInfo();

            Node left = container.getFirstChild();
            Node right = container.getLastChild();

            if (left.isName() && right.isFunction())
            {
                if (comment.isConstructor() || comment.isInterface())
                {
                    // Foo = function () {};
                    model.addClass(container, left.getString());
                }

            }
            else if (left.isGetProp() && right.isFunction())
            {
                boolean isConstructor = comment != null
                        && (comment.isConstructor() || comment.isInterface());
                // foo.bar.Baz = function () {};
                if (isConstructor)
                {
                    model.addClass(container, left.getQualifiedName());
                }
            }
        }
        else
        {
            comment = container.getJSDocInfo();
            boolean isConstructor = comment != null
                    && (comment.getTypedefType() != null);
            if (isConstructor)
            {
                model.addTypeDef(container, container.getQualifiedName());
            }
        }
    }

    private void visitFunction(Node child)
    {
        JSDocInfo comment = child.getJSDocInfo();

        boolean isConstructor = comment != null
                && (comment.isConstructor() || comment.isInterface());

        if (isConstructor)
        {
            // function Goo () {};
            model.addClass(child, child.getFirstChild().getString());
        }
        else
        {
            model.addFunction(child, child.getFirstChild().getString());
        }
    }

    private void visitVar(Node child)
    {
        JSDocInfo comment = child.getJSDocInfo();

        Node first = child.getFirstChild();
        if (first.isName())
        {
            Node subFirst = first.getFirstChild();
            if (subFirst != null && subFirst.isObjectLit())
            {
                if (comment.hasEnumParameterType())
                {

                }
                else
                {
                    //System.out.println(first.getFirstChild().toStringTree());
                    //log("Encountered namespace [" + first.getQualifiedName() + "]");
                    model.addNamespace(child, first.getQualifiedName());
                }
            }
            else if (subFirst != null && subFirst.isFunction())
            {
                boolean isConstructor = comment != null
                        && (comment.isConstructor() || comment.isInterface());
                // foo.bar.Baz = function () {};
                if (isConstructor)
                {
                    model.addClass(child, first.getString());
                }
            }
            else
            {
                boolean isConstructor = comment != null
                        && (comment.getTypedefType() != null);
                // * @typedef
                // var foo;
                if (isConstructor)
                {
                    // model.addClass(child, first.getString());
                    model.addTypeDef(child, first.getString());
                }
                else if (comment != null && comment.isConstant())
                {
                    //System.out.println(child.toStringTree());
                    model.addConstant(child, first.getString());
                }
            }
        }
    }
}
