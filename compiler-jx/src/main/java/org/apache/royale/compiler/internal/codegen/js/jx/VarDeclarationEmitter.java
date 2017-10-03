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
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.royale.compiler.internal.tree.as.DynamicAccessNode;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IEmbedNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

public class VarDeclarationEmitter extends JSSubEmitter implements
        ISubEmitter<IVariableNode>
{

    public VarDeclarationEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IVariableNode node)
    {
        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) getEmitter();

        getModel().getVars().add(node);
        
        if (!(node instanceof ChainedVariableNode) && !node.isConst())
        {
            fjs.emitMemberKeyword(node);
        }

        IExpressionNode variableTypeNode = node.getVariableTypeNode();
        boolean hasVariableType = variableTypeNode.getLine() >= 0;
        if(hasVariableType)
        {
            startMapping(variableTypeNode,
                    variableTypeNode.getLine(),
                    variableTypeNode.getColumn() - 1); //include the :
        }
        else
        {
            //the result of getVariableTypeNode() may not have a line and
            //column. this can happen when the type is omitted in the code, and
            //the compiler generates a node for type *.
            //in this case, put it at the end of the name expression.
            IExpressionNode nameExpressionNode = node.getNameExpressionNode();
            startMapping(variableTypeNode, nameExpressionNode.getLine(),
                    nameExpressionNode.getColumn() + nameExpressionNode.getAbsoluteEnd() - nameExpressionNode.getAbsoluteStart());
        }
        IExpressionNode avnode = node.getAssignedValueNode();
        IDefinition avdef = null;
        if (avnode != null)
        {
        	avdef = avnode.resolveType(getWalker().getProject());
            String opcode = avnode.getNodeID().getParaphrase();
            if (opcode != "AnonymousFunction")
            {
                fjs.getDocEmitter().emitVarDoc(node, avdef, getWalker().getProject());
            }
        }
        else
        {
            fjs.getDocEmitter().emitVarDoc(node, null, getWalker().getProject());
        }
        endMapping(variableTypeNode);

        if (!(node instanceof ChainedVariableNode) && node.isConst())
        {
            fjs.emitMemberKeyword(node);
        }

        fjs.emitDeclarationName(node);
        if (avnode != null && !(avnode instanceof IEmbedNode))
        {
            if (hasVariableType)
            {
                startMapping(node, node.getVariableTypeNode());
            }
            else
            {
                startMapping(node, node.getNameExpressionNode());
            }
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            endMapping(node);
            boolean varIsNumber = (variableTypeNode.getNodeID() == ASTNodeID.IdentifierID && 
            		  (((IdentifierNode)variableTypeNode).getName().equals(IASLanguageConstants.Number) ||
            		   ((IdentifierNode)variableTypeNode).getName().equals(IASLanguageConstants._int) ||
            		   ((IdentifierNode)variableTypeNode).getName().equals(IASLanguageConstants.uint)));
            boolean valIsNumber = (avdef != null && (avdef.getQualifiedName().equals(IASLanguageConstants.Number) ||
            										 avdef.getQualifiedName().equals(IASLanguageConstants._int) ||
            										 avdef.getQualifiedName().equals(IASLanguageConstants.uint)));
            if (varIsNumber && !valIsNumber && (avdef == null || avdef.getQualifiedName().equals(IASLanguageConstants.ANY_TYPE)))
            {
        		if (avnode.getNodeID() == ASTNodeID.FunctionCallID)
        		{
	            	IExpressionNode fnNameNode = ((FunctionCallNode)avnode).getNameNode();
	            	if (fnNameNode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
	            	{
	            		MemberAccessExpressionNode mae = (MemberAccessExpressionNode)fnNameNode;
	            		IExpressionNode rightNode = mae.getRightOperandNode();
	            		valIsNumber = rightNode.getNodeID() == ASTNodeID.IdentifierID && 
	            				((IdentifierNode)rightNode).getName().equals("length") &&
	            				fjs.isXMLList(mae);
	            	}
        		}
        		else if (avnode.getNodeID() == ASTNodeID.ArrayIndexExpressionID)
        		{
        			DynamicAccessNode dyn = (DynamicAccessNode)avnode;
        			IDefinition leftDef = dyn.getLeftOperandNode().resolveType(getProject());
        			IDefinition rightDef = dyn.getRightOperandNode().resolveType(getProject());
        			// numeric indexing?
        			if (rightDef.getQualifiedName().equals(IASLanguageConstants.Number))
        			{
        				IMetaTag[] metas = leftDef.getAllMetaTags();
        				for (IMetaTag meta : metas)
        				{
        					if (meta.getTagName().equals("ArrayElementType"))
        					{
        						IMetaTagAttribute[] attrs = meta.getAllAttributes();
        						for (IMetaTagAttribute attr : attrs)
        						{
        							String t = attr.getValue();
            						if (t.equals(IASLanguageConstants.Number))
            							valIsNumber = true;
        						}
        					}
        				}
        			}
        		}
            }
            String coercion = "";
            if (varIsNumber && !valIsNumber)
            	coercion = "Number(";
            if (variableTypeNode.getNodeID() == ASTNodeID.IdentifierID &&
                	((IdentifierNode)variableTypeNode).getName().equals(IASLanguageConstants.String) &&
                	(avdef == null || (!avdef.getQualifiedName().equals(IASLanguageConstants.String) &&
                			            !avdef.getQualifiedName().equals(IASLanguageConstants.Null))))
                	coercion = "org.apache.royale.utils.Language.string(";
            write(coercion);
            fjs.emitAssignedValue(avnode);
            if (coercion.length() > 0)
              	write(")");
        }
        if (avnode == null)
        {
            IDefinition typedef = null;
            IExpressionNode enode = node.getVariableTypeNode();//getAssignedValueNode();
            if (enode != null)
                typedef = enode.resolveType(getWalker().getProject());
            if (typedef != null)
            {
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
                if (node.getParent() != null &&
                        node.getParent().getParent() != null &&
                        node.getParent().getParent().getNodeID() != ASTNodeID.Op_InID)
                {
                    String defName = typedef.getQualifiedName();
                    if (defName.equals("int") || defName.equals("uint"))
                    {
                        write(ASEmitterTokens.SPACE);
                        writeToken(ASEmitterTokens.EQUAL);
                        write("0");
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
            }
        }

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    startMapping(node, node.getChild(i - 1));
                    writeToken(ASEmitterTokens.COMMA);
                    endMapping(node);
                    fjs.emitVarDeclaration((IVariableNode) child);
                }
            }
        }
    }

}
