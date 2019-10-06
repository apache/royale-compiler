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
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.ILiteralContainerNode;

public class LiteralContainerEmitter extends JSSubEmitter implements
        ISubEmitter<ILiteralContainerNode>
{
    public LiteralContainerEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ILiteralContainerNode node)
    {
        final IContainerNode cnode = node.getContentsNode();
        final IContainerNode.ContainerType type = cnode.getContainerType();
        String preFix = null;
        String postFix = null;
        boolean isXMLList = node.getNodeID() == ASTNodeID.XMLListContentID;

        if (type == IContainerNode.ContainerType.BRACES)
        {
            preFix = ASEmitterTokens.BLOCK_OPEN.getToken();
            postFix = ASEmitterTokens.BLOCK_CLOSE.getToken();
        }
        else if (type == IContainerNode.ContainerType.BRACKETS)
        {
            preFix = ASEmitterTokens.SQUARE_OPEN.getToken();
            postFix = ASEmitterTokens.SQUARE_CLOSE.getToken();
        }
        else if (type == IContainerNode.ContainerType.IMPLICIT)
        {
            // nothing to write, move along
        }
        else if (type == IContainerNode.ContainerType.PARENTHESIS)
        {
            preFix = ASEmitterTokens.PAREN_OPEN.getToken();
            postFix = ASEmitterTokens.PAREN_CLOSE.getToken();
        }

        if (preFix != null)
        {
            startMapping(node);
            write(preFix);
            endMapping(node);
        }

        if (isXMLList)
        {
        	write("new XMLList(\"");
        }
        final int len = cnode.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = cnode.getChild(i);
            if (isXMLList)
            {
            	if (child instanceof LiteralNode)
            	{
            		String value = ((LiteralNode)child).getValue(true);
            		value = value.replace("\"", "\\\"");
            		value = value.replace("\r", "");
            		value = value.replace("\n", "\\n");
            		// skip the wrapping empty nodes.  XMLList
            		// is expecting illegal xml (a sequence of children nodes
            		// and will wrap it
            		// in containing nodes
            		if (value.contentEquals("<>"))
            			continue;
            		else if (value.contentEquals("</>"))
            			continue;
            		write(value);
            	}
            }
            else
            {
	            getWalker().walk(child);
	            if (i < len - 1)
	            {
	                //we're mapping the comma to the literal container, but we fill
	                //the space between the current child and the next because we
	                //don't know exactly where the comma appears in ActionScript
	                startMapping(node, child);
	                writeToken(ASEmitterTokens.COMMA);
	                endMapping(node);
	            }
            }
        }
        if (isXMLList)
        	write("\")");
        
        if (postFix != null)
        {
            startMapping(node, node.getEndLine(), node.getEndColumn() - postFix.length());
            write(postFix);
            endMapping(node);
        }
    }
}
