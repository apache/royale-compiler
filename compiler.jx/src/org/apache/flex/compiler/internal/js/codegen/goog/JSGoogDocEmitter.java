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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.internal.js.codegen.JSDocEmitter;
import org.apache.flex.compiler.internal.js.codegen.JSSharedData;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.js.codegen.IJSEmitter;
import org.apache.flex.compiler.js.codegen.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

public class JSGoogDocEmitter extends JSDocEmitter implements IJSGoogDocEmitter
{

    public JSGoogDocEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }
    
    @Override
    public void emitFieldDoc(IVariableNode node)
    {
        begin();
        
        String ns = node.getNamespace();
        if (ns == "private")
        {
        	emitPrivate(node);
        }
        else if (ns == "protected")
        {	
        	emitProtected(node);
        }
        
        if (node.isConst())
        	emitConst(node);
        
        emitType(node);
        
        end();
    }
    
    @Override
    public void emitMethodDoc(IFunctionNode node, ICompilerProject project)
    {
        IClassNode cnode = (IClassNode) node.getAncestorOfType(IClassNode.class);
        IClassDefinition classDefinition = cnode.getDefinition();

        if (node instanceof IFunctionNode)
        {
            boolean hasDoc = false;

            if (node.isConstructor())
            {
                begin();
                hasDoc = true;
                
                write(" * @constructor\n");
                
                IClassDefinition parent = (IClassDefinition) node.getDefinition().getParent();
                IClassDefinition superClass = parent.resolveBaseClass(project);
            	String qname = superClass.getQualifiedName();
            	
                if (superClass != null && !qname.equals("Object"))
                    emitExtends(superClass);
                
            	IReference[] interfaceReferences = classDefinition.getImplementedInterfaceReferences();
                if (interfaceReferences.length > 0)
                {
                    for (IReference reference : interfaceReferences)
                    {
                    	emitImplements(reference);
                    }
                }
            }
            else
            {
                // @this
                if (containsThisReference(node))
                {
                    begin();
                    hasDoc = true;

                    emitThis(classDefinition);
                }

                // @param
                IParameterNode[] parameters = node.getParameterNodes();
                for (IParameterNode pnode : parameters)
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    emitParam(pnode);
                }

                // @return
                String returnType = node.getReturnType();
                if (returnType != "" && returnType != "void")
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    emitReturn(node);
                }

                // @override
                Boolean override = node.hasModifier(ASModifier.OVERRIDE);
                if (override)
                {
                    if (!hasDoc)
                    {
                        begin();
                        hasDoc = true;
                    }

                    emitOverride(node);
                }
            }
            
            if (hasDoc)
                end();
        }
    }

    @Override
    public void emitVarDoc(IVariableNode node)
    {
        if (!node.isConst())
        {	
        	emitTypeShort(node);
        }
        else
        {
        	write("\n"); // TODO (erikdebruin) check if this is needed
        	begin();
        	emitConst(node);
        	emitType(node);
        	end();
        }
    }
    
    @Override
    public void emitConst(IVariableNode node)
    {
        write(" * @const\n");
    }

    @Override
    public void emitDefine(IVariableNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitDeprecated(IASNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitEnum(IClassNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitExtends(IClassDefinition superDefinition)
    {
        write(" * @extends {" + superDefinition.getQualifiedName() + "}\n");
    }

    @Override
    public void emitImplements(IReference reference)
    {
    	// TODO (erikdebruin) we need to get the fully qualified name...
        write(" * @implements {" + reference.getName() + "}\n");
    }

    @Override
    public void emitInheritDoc(IClassNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitLicense(IClassNode node)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void emitOverride(IFunctionNode node)
    {
        write(" * @override\n");
    }

    @Override
    public void emitParam(IParameterNode node)
    {
    	String postfix = (node.getDefaultValue() == null) ? "" : "=";
    	
    	String paramType = "";
    	if (node.isRest())
    		paramType = "...";
    	else
    		paramType = convertASTypeToJS(node.getVariableType());
    	
        write(" * @param {" + paramType + postfix + "} " + node.getName() + "\n");
    }

    @Override
    public void emitPrivate(IASNode node)
    {
        write(" * @private\n");
    }

    @Override
    public void emitProtected(IASNode node)
    {
        write(" * @protected\n");
    }

    @Override
    public void emitReturn(IFunctionNode node)
    {
        String rtype = node.getReturnType();
        if (rtype != null)
            write(" * @return {" + convertASTypeToJS(rtype) + "}\n");
    }

    @Override
    public void emitThis(ITypeDefinition type)
    {
        write(" * @this {" + type.getQualifiedName() + "}\n");
    }

    @Override
    public void emitType(IASNode node)
    {
        String type = ((IVariableNode) node).getVariableType(); 
        write(" * @type {" + convertASTypeToJS(type) + "}\n");
    }

    public void emitTypeShort(IASNode node)
    {
        String type = ((IVariableNode) node).getVariableType(); 
        write("/** @type {" + convertASTypeToJS(type) + "} */ ");
    }

    @Override
    public void emitTypedef(IASNode node)
    {
        // TODO Auto-generated method stub

    }

    //--------------------------------------------------------------------------

    public void emmitPackageHeader(IPackageNode node)
    {
        begin();
        write(" * " + JSSharedData.getTimeStampString());
        end();
    }

    //--------------------------------------------------------------------------

    private boolean containsThisReference(IASNode node)
    {
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            final IASNode child = node.getChild(i);
            if (child.getChildCount() > 0)
            {
                return containsThisReference(child);
            }
            else
            {
                if (SemanticUtils.isThisKeyword(child))
                    return true;
            }
        }

        return false;
    }

    private String convertASTypeToJS(String name)
    {
    	String result = name;
    	
    	if (name.equals(""))
    		result = "*";
    	else if (name.equals("Boolean") || name.equals("String") || name.equals("Number"))
    		result = result.toLowerCase();
    	else if (name.equals("int") || name.equals("uint"))
    		result = "number";
    	else if (name.matches("Vector.<.*>"))
    		// TODO (erikdebruin) will this work with nested Vector declarations?
        	result = name.replace("Vector", "Array");
    	
        return result;
    }

}
