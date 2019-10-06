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

package org.apache.royale.compiler.internal.codegen.js.jx;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.BindableVarInfo;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * Static or member variables of a class. For local variables in a function, see
 * VarDeclarationEmitter. For accessors, see AccessorEmitter.
 */
public class FieldEmitter extends JSSubEmitter implements
        ISubEmitter<IVariableNode>
{
    public FieldEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    public boolean hasComplexStaticInitializers = false;
    
    @Override
    public void emit(IVariableNode node)
    {
        IDefinition definition = EmitterUtils.getClassDefinition(node);

        IDefinition def = null;
        IExpressionNode enode = node.getVariableTypeNode();//getAssignedValueNode();
        if (enode != null)
        {
            def = enode.resolveType(getProject());
        }

        // TODO (mschmalle)
        if (getEmitter().getDocEmitter() instanceof IJSGoogDocEmitter)
        {
            ((IJSGoogDocEmitter) getEmitter().getDocEmitter()).emitFieldDoc(node, def, getProject());
        }

        IDefinition ndef = node.getDefinition();

        String className = null;
        String root = "";
        IVariableDefinition.VariableClassification classification = node.getVariableClassification();
        boolean isPackageOrFileMember = classification == IVariableDefinition.VariableClassification.PACKAGE_MEMBER ||
                classification == IVariableDefinition.VariableClassification.FILE_MEMBER;
        if (isPackageOrFileMember)
        {
        	className = getEmitter().formatQualifiedName(node.getQualifiedName());
            write(className);
        }
        else
        {
            ModifiersSet modifierSet = ndef.getModifiers();
            if (modifierSet != null && !modifierSet.hasModifier(ASModifier.STATIC))
            {
                root = JSEmitterTokens.PROTOTYPE.getToken();
                root += ASEmitterTokens.MEMBER_ACCESS.getToken();
            }

            if (definition == null)
                definition = ndef.getContainingScope().getDefinition();

            startMapping(node.getNameExpressionNode());
            className = getEmitter().formatQualifiedName(definition.getQualifiedName());
            write(className
                    + ASEmitterTokens.MEMBER_ACCESS.getToken() + root);
            String qname = node.getName();
            IDefinition nodeDef = node.getDefinition();
            if (nodeDef != null && !nodeDef.isStatic() && nodeDef.isPrivate() && getProject().getAllowPrivateNameConflicts())
        			qname = getEmitter().formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);
            write(qname);
            endMapping(node.getNameExpressionNode());
        }

        if (node.getNodeID() == ASTNodeID.BindableVariableID && !node.isConst())
        {
            // add an underscore to convert this var to be the
            // backing var for the get/set pair that will be generated later.
            write("_");
        }
        IExpressionNode vnode = node.getAssignedValueNode();
        if (vnode != null)
        {
        	getModel().inStaticInitializer = ndef.isStatic();
            String vnodeString = getEmitter().stringifyNode(vnode);
            if (ndef.isStatic() && vnode instanceof FunctionCallNode)
            {
                FunctionCallNode fcn = (FunctionCallNode)vnode;
            	if (fcn.getNameNode() instanceof IdentifierNode)
            	{
            		IDefinition d = fcn.getNameNode().resolve(getProject());
            		// assume this is a call to static method in the class
            		// otherwise it would be a memberaccessexpression?
            		if (d instanceof FunctionDefinition)
            		{
            			FunctionDefinition fd = (FunctionDefinition)d;
                		IASNode m = fd.getNode();
                		if (m != null)
                		{
    	            		// re-emit it to collect static initializer class references in usedNames
    	            		getEmitter().stringifyNode(m);
                		}
                    }
                    //it could also be a constructor
                    else if (d instanceof IClassDefinition)
                    {
                        IClassDefinition classDef = (IClassDefinition) d;
                        IFunctionDefinition constructorDef = classDef.getConstructor();
                        if (constructorDef != null)
                        {
                            IASNode m = constructorDef.getNode();
                            if (m != null)
                            {
                                // re-emit it to collect static initializer class references in usedNames
                                getEmitter().stringifyNode(m);
                            }
                        }
                    }
            	}
            }
        	getModel().inStaticInitializer = false;
        	if ((ndef.isStatic() && !EmitterUtils.needsStaticInitializer(vnodeString, className)) ||
        			(!ndef.isStatic() && EmitterUtils.isScalar(vnode)) ||
        			isPackageOrFileMember)
	        {
                IExpressionNode beforeNode = node.getVariableTypeNode();
                if (beforeNode.getAbsoluteStart() == -1)
                {
                    beforeNode = node.getNameExpressionNode();
                }
	            startMapping(node, beforeNode);
	            write(ASEmitterTokens.SPACE);
	            writeToken(ASEmitterTokens.EQUAL);
	            endMapping(node);
                getEmitter().emitAssignmentCoercion(vnode, node.getVariableTypeNode().resolve(getProject()));
	        }
	        else if (ndef.isStatic() && EmitterUtils.needsStaticInitializer(vnodeString, className))
	        {
	        	hasComplexStaticInitializers = true;
	        }
	        
	        if (!isPackageOrFileMember  && !ndef.isStatic() && !EmitterUtils.isScalar(vnode)
                    && getProject() instanceof RoyaleJSProject
                    && ((RoyaleJSProject) getProject()).config != null
                    && ((RoyaleJSProject) getProject()).config.getJsDefaultInitializers()
            )
	        {
	            //this value will actually be initialized inside the constructor.
                //but if default initializers is set, we define it on the prototype with null value first.
	            //Why?: this needs to be defined on the prototype to support reflection
                //otherwise the constructor initializers will create the new property value on 'this' and
                //there is no runtime clue to separate what is 'dynamic' and what is 'inherited'
                //these clues throughout the prototype chain are important for runtime identification
                //of dynamic fields.
                //runtime checks will only work accurately using this technique if the entire inheritance chain
                //for the reflection target is compiled with default js initializers, because it permits
                //inspection of the prototype chain to determine all the sealed members, and isolate them
                //from whatever else is defined as 'own' properties on the instance (which can be assumed to be
                // 'dynamic' properties).
                write(ASEmitterTokens.SPACE);
                writeToken(ASEmitterTokens.EQUAL);
                write(ASEmitterTokens.NULL);
            }
        }
        if (vnode == null && def != null)
        {
            String defName = def.getQualifiedName();
        	if (defName.equals("int") || defName.equals("uint"))
        	{
                write(ASEmitterTokens.SPACE);
                writeToken(ASEmitterTokens.EQUAL);
                write("0");
            }
            else
            {
                boolean defaultInitializers = false;
                ICompilerProject project = getProject();
                if(project instanceof RoyaleJSProject)
                {
                    RoyaleJSProject fjsProject = (RoyaleJSProject) project;
                    if(fjsProject.config != null)
                    {
                        defaultInitializers = fjsProject.config.getJsDefaultInitializers();
                    }
                }
                if (defaultInitializers)
                {
                    if (defName.equals("Number"))
                    {
                        write(ASEmitterTokens.SPACE);
                        writeToken(ASEmitterTokens.EQUAL);
                        write(IASKeywordConstants.NA_N);
                    }
                    else if (defName.equals("Boolean"))
                    {
                        write(ASEmitterTokens.SPACE);
                        writeToken(ASEmitterTokens.EQUAL);
                        write(IASKeywordConstants.FALSE);
                        
                    } else if (defName.equals("*")) {
                        //setting the value to *undefined* is needed  to create the field
                        //on the prototype - this is important for reflection purposes
                        write(ASEmitterTokens.SPACE);
                        writeToken(ASEmitterTokens.EQUAL);
                        write(ASEmitterTokens.UNDEFINED);
                    }
                    else
                    {
                        //everything else should default to null
                        write(ASEmitterTokens.SPACE);
                        writeToken(ASEmitterTokens.EQUAL);
                        write(IASKeywordConstants.NULL);
                    }
                }
            }
        }

        if (!(node instanceof ChainedVariableNode))
        {
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    writeNewline(ASEmitterTokens.SEMICOLON);
                    writeNewline();
                    getEmitter().emitField((IVariableNode) child);
                }
            }
        }
        if (node.getNodeID() == ASTNodeID.BindableVariableID && !node.isConst())
        {
            if (getModel().getBindableVars().get(node.getName()) == null) {
                BindableVarInfo bindableVarInfo = new BindableVarInfo();
                bindableVarInfo.isStatic = node.hasModifier(ASModifier.STATIC);;
                bindableVarInfo.namespace = node.getNamespace();
                bindableVarInfo.type = def.getQualifiedName();
                getModel().getBindableVars().put(node.getName(), bindableVarInfo);
                IMetaTagsNode metaTags = node.getMetaTags();
                if (metaTags != null) {
                    IMetaTagNode[] tags = metaTags.getAllTags();
                    if (tags.length > 0)
                        bindableVarInfo.metaTags = tags;
                }
            }
        }
    }

    public boolean emitFieldInitializer(IVariableNode node)
    {
        IDefinition definition = EmitterUtils.getClassDefinition(node);

        IDefinition ndef = node.getDefinition();
        String className = null;

        IVariableDefinition.VariableClassification classification = node.getVariableClassification();
        boolean isPackageOrFileMember = classification == IVariableDefinition.VariableClassification.PACKAGE_MEMBER ||
                classification == IVariableDefinition.VariableClassification.FILE_MEMBER;
        IExpressionNode vnode = node.getAssignedValueNode();
        if (vnode != null)
        {
            String vnodeString = getEmitter().stringifyNode(vnode);
            if (definition == null)
                definition = ndef.getContainingScope().getDefinition();
            className = getEmitter().formatQualifiedName(definition.getQualifiedName());
        	if (ndef.isStatic() && EmitterUtils.needsStaticInitializer(vnodeString, className) && !isPackageOrFileMember)
	        {
                writeNewline();
                write(className);
                write(ASEmitterTokens.MEMBER_ACCESS.getToken());
                write(node.getName());
	
	            if (node.getNodeID() == ASTNodeID.BindableVariableID && !node.isConst())
	            {
	                // add an underscore to convert this var to be the
	                // backing var for the get/set pair that will be generated later.
	                write("_");
	            }
	            write(ASEmitterTokens.SPACE);
	            writeToken(ASEmitterTokens.EQUAL);
	            write(vnodeString);
	            write(ASEmitterTokens.SEMICOLON);
                return true;
            }
        }

        return false;
    }
}
