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
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

public class AddMemberPass extends AbstractCompilerPass
{

    public AddMemberPass(ReferenceModel model, AbstractCompiler compiler)
    {
        super(model, compiler);
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent)
    {
        if (n.getParent() == null || n.isScript() || n.isSyntheticBlock())
            return;

        JSDocInfo jsDoc = n.getJSDocInfo(); //NodeUtil.getBestJSDocInfo(n);
        if (jsDoc != null)
        {
            if (n.isVar())
            {
                visitVar(t, n);
            }
            else if (n.isFunction())
            {
                visitFunction(t, n);
            }
            else if (n.isAssign())
            {
                if (n.getFirstChild().isGetProp()
                        && n.getLastChild().isFunction())
                {
                    // instance or static method
                    visitMethod(t, n);
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
            else if (n.isGetProp())
            {
                visitGetProp(t, n);
            }
        }
    }

    /*
    ASSIGN 48 [jsdoc_info: JSDocInfo] [source_file: [w3c_dom1]] [length: 38]
            GETPROP 48 [source_file: [w3c_dom1]] [length: 34]
                NAME DOMException 48 [source_file: [w3c_dom1]] [length: 12]
                STRING HIERARCHY_REQUEST_ERR 48 [source_file: [w3c_dom1]] [length: 21]
            NUMBER 3.0 48 [source_file: [w3c_dom1]] [length: 1]
    */

    // Instance Field (prototype)
    // GETPROP 2026 [jsdoc_info: JSDocInfo] [source_file: [es3]] [length: 27]
    //        GETPROP 2026 [source_file: [es3]] [length: 16]
    //            NAME RegExp 2026 [source_file: [es3]] [length: 6]
    //            STRING prototype 2026 [source_file: [es3]] [length: 9]
    //        STRING ignoreCase 2026 [source_file: [es3]] [length: 10]

    @SuppressWarnings("unused")
    private void visitInstanceField(NodeTraversal t, Node n)
    {
        Node className = n.getFirstChild().getFirstChild();
        Node name = n.getLastChild();
        model.addField(n, className.getString(), name.getString());
    }

    // Static Field
    // GETPROP 1994 [jsdoc_info: JSDocInfo] [source_file: [es3]] [length: 9]
    //       NAME RegExp 1994 [source_file: [es3]] [length: 6]
    //        STRING $6 1994 [source_file: [es3]] [length: 2]

    @SuppressWarnings("unused")
    private void visitStaticField(NodeTraversal t, Node n)
    {
        Node className = n.getFirstChild();
        Node name = n.getLastChild();
        model.addStaticField(n, className.getString(), name.getString());

    }

    /*
    
    Instance
    
    ASSIGN 194 [jsdoc_info: JSDocInfo] [source_file: [es3]] [length: 44]
        GETPROP 194 [source_file: [es3]] [length: 28]
            GETPROP 194 [source_file: [es3]] [length: 16]
                NAME Object 194 [source_file: [es3]] [length: 6]
                STRING prototype 194 [source_file: [es3]] [length: 9]
            STRING constructor 194 [source_file: [es3]] [length: 11]
        FUNCTION  194 [source_file: [es3]] [length: 13]
            NAME  194 [source_file: [es3]] [length: 13]
            PARAM_LIST 194 [source_file: [es3]] [length: 2]
            BLOCK 194 [source_file: [es3]] [length: 2]
    
    Static
    
    ASSIGN 770 [jsdoc_info: JSDocInfo] [source_file: [es3]] [length: 52]
        GETPROP 770 [source_file: [es3]] [length: 10]
            NAME Array 770 [source_file: [es3]] [length: 5]
            STRING some 770 [source_file: [es3]] [length: 4]    
    
    
    */

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
        //log(n.toStringTree());
        log(n.getQualifiedName());

        String qName = n.getQualifiedName();
        // Port.prototype.name

        // chrome.runtime.lastError.message
        int protoType = qName.indexOf(".prototype");
        if (protoType != -1)
        {
            String className = qName.substring(0, protoType);
            String memberName = qName.substring(protoType + 11, qName.length());
            //log("Prototype:: className [" + className
            //        + "] memberName [" + memberName + "]");
            model.addField(n, className, memberName);
        }
        else
        {
            String className = qName.substring(0, qName.lastIndexOf("."));
            String memberName = qName.substring(qName.lastIndexOf(".") + 1,
                    qName.length());
            //log("className [" + className + "] memberName ["
            //        + memberName + "]");
            model.addStaticField(n, className, memberName);
        }

    }

    /*
    
    FUNCTION Arguments 34 [jsdoc_info: JSDocInfo] [source_file: [test]] [length: 23]
        NAME Arguments 34 [source_file: [test]] [length: 9]
        PARAM_LIST 34 [source_file: [test]] [length: 2]
        BLOCK 34 [source_file: [test]] [length: 2]
    
    */

    @SuppressWarnings("unused")
    private void visitFunction(NodeTraversal t, Node n)
    {

        JSDocInfo jsDoc = n.getJSDocInfo();

        if (jsDoc != null)
        {
            if (jsDoc.isConstructor())
            {
                //System.out.println("---------------------------------------------");
                Node name = n.getChildAtIndex(0);
                //System.out.println("Class " + name.getString());

                Node paramList = n.getChildAtIndex(1);

                JSTypeExpression returnType = jsDoc.getReturnType();
                if (returnType != null)
                {
                    JSType jsReturnType = returnType.evaluate(null,
                            compiler.getTypeRegistry());
                    //System.out.println("Returns: " + jsReturnType);
                }

                Node block = n.getChildAtIndex(2);
                //System.out.println(">>>>>>--------------------------------------");
                //System.out.println(n.toStringTree());
            }
            else
            {
                // XX Global function parseInt(num, base)
                //System.out.println(n.toStringTree());
            }
        }
    }

    private void visitVar(NodeTraversal t, Node n)
    {
        JSDocInfo jsDoc = n.getJSDocInfo();

        if (jsDoc != null)
        {
            if (jsDoc.isConstant())
            {
                Node first = n.getFirstChild();
                if (first.isName())
                {
                    Node second = first.getFirstChild();
                    if (second != null && second.isObjectLit())
                    {
                        // * @const
                        // var Math = {};
                        //System.out.println("Final Class "
                        //       + n.getFirstChild().getString());
                        //System.out.println(n.toStringTree());
                    }
                    else
                    {
                        // * @const
                        // var Infinity;
                        //System.out.println("var "
                        //        + n.getFirstChild().getString());
                        //System.out.println(n.toStringTree());
                    }
                }
            }
            else if (jsDoc.isConstructor())
            {
            }
        }
    }

    @Override
    public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n,
            Node parent)
    {
        return true;
    }

}
