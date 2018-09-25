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
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NumericLiteralNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;

public class DynamicAccessEmitter extends JSSubEmitter implements
        ISubEmitter<IDynamicAccessNode>
{
    public DynamicAccessEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IDynamicAccessNode node)
    {
        IExpressionNode leftOperandNode = node.getLeftOperandNode();
        getWalker().walk(leftOperandNode);
        if (leftOperandNode.getNodeID() == ASTNodeID.Op_AtID)
        	return;

        IExpressionNode rightOperandNode = node.getRightOperandNode();
        IJSEmitter ijs = getEmitter();
    	JSRoyaleEmitter fjs = (ijs instanceof JSRoyaleEmitter) ? 
    							(JSRoyaleEmitter)ijs : null;
    	if (fjs != null)
    	{
	    	boolean isXML = false;
	    	if (leftOperandNode instanceof MemberAccessExpressionNode)
	    		isXML = fjs.isLeftNodeXMLish((MemberAccessExpressionNode)leftOperandNode);
	    	else if (leftOperandNode instanceof IExpressionNode)
	    		isXML = fjs.isXML((IExpressionNode)leftOperandNode);
	    	if (isXML)
	    	{
	    		ITypeDefinition type = rightOperandNode.resolveType(getProject());
				if (!type.isInstanceOf("int", getProject()) && !type.isInstanceOf("uint", getProject()) && !type.isInstanceOf("Number", getProject()) )
				{
					String field = fjs.stringifyNode(rightOperandNode);
					if (field.startsWith("\"@"))
					{
						field = field.replace("@", "");
						write(".attribute(" + field + ")");
					}
					else
						write(".child(" + field + ")");
					return;
				}    		
	    	}
    	}
    	
        startMapping(node, leftOperandNode);
        write(ASEmitterTokens.SQUARE_OPEN);
        endMapping(node);

        getWalker().walk(rightOperandNode);

        startMapping(node, rightOperandNode);
        write(ASEmitterTokens.SQUARE_CLOSE);
        endMapping(node);
    }
}
