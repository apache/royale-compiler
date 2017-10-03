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

package org.apache.flex.compiler.internal.codegen.js.jx;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel.BindableVarInfo;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.projects.RoyaleProject;
import org.apache.flex.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagsNode;

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
            write(node.getName());
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
            		// assume this is a call to static method in the class
            		// otherwise it would be a memberaccessexpression?
            		IDefinition d = (IDefinition)fcn.getNameNode().resolve(getProject());
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
                startMapping(vnode);
	            write(vnodeString);
                endMapping(vnode);
	        }
	        else if (ndef.isStatic() && EmitterUtils.needsStaticInitializer(vnodeString, className))
	        {
	        	hasComplexStaticInitializers = true;
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
            boolean defaultInitializers = false;
            ICompilerProject project = getProject();
            if(project instanceof RoyaleProject)
            {
                RoyaleProject fjsProject = (RoyaleProject) project;
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
                }
                else if (!defName.equals("*"))
                {
                    //type * is meant to default to undefined, so it
                    //doesn't need to be initialized, but everything
                    //else should default to null
                    write(ASEmitterTokens.SPACE);
                    writeToken(ASEmitterTokens.EQUAL);
                    write(IASKeywordConstants.NULL);
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
                write(className
                        + ASEmitterTokens.MEMBER_ACCESS.getToken());
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
