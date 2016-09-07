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
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.LiteralNode;
import org.apache.flex.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.flex.compiler.internal.tree.as.XMLLiteralNode;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;

public class LiteralEmitter extends JSSubEmitter implements
        ISubEmitter<ILiteralNode>
{

    public LiteralEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ILiteralNode node)
    {
        boolean isWritten = false;

        String newlineReplacement = "\\\\n";
        String s = node.getValue(true);
        if (!(node instanceof RegExpLiteralNode))
        {
            if (node.getLiteralType() == LiteralType.XML)
            {
            	newlineReplacement = "\\\\\n";
            	XMLLiteralNode xmlNode = (XMLLiteralNode)node;
            	if (xmlNode.getContentsNode().getChildCount() == 1)
            	{
	            	if (s.contains("'"))
	            		s = "\"" + s + "\"";
	            	else
	            		s = "'" + s + "'";
            	}
            	else
            	{
        			StringBuilder sb = new StringBuilder();
            		// probably contains {initializers}
            		int n = xmlNode.getContentsNode().getChildCount();
            		for (int i = 0; i < n; i++)
            		{
            			if (i > 0)
            				sb.append(" + ");
            			IASNode child = xmlNode.getContentsNode().getChild(i);
            			if (child instanceof LiteralNode)
            			{
            				s = ((LiteralNode)child).getValue(true);
        	            	if (s.contains("'"))
        	            		sb.append("\"" + s + "\"");
        	            	else
        	            		sb.append("'" + s + "'");
            			}
            			else if (child instanceof IdentifierNode)
            			{
            				s = getEmitter().stringifyNode(child);
            				sb.append(s);
            			}
            		}
            		s = sb.toString();
            	}
                char c = s.charAt(0);
                if (c == '"')
                {
                    s = s.substring(1, s.length() - 1);
                    s = s.replace("\"", "\\\"");
                    s = "\"" + s + "\"";
                }
                s = "new XML( " + s + ")";
            }
            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
            //s = "\'" + s.replaceAll("\'", "\\\\\'") + "\'";
            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", newlineReplacement);
            if (node.getLiteralType() == LiteralType.STRING)
            {
            	char c = s.charAt(0);
            	if (c == '"')
            	{
            		s = s.substring(1, s.length() - 1);
            		s = s.replace("\"", "\\\"");
            		s = "\"" + s + "\"";
            	}
            	else if (c == '\'')
            	{
            		s = s.substring(1, s.length() - 1);
            		s = s.replace("'", "\\'");            		
            		s = "'" + s + "'";
            	}
            	s = s.replace("\u2028", "\\u2028");
            	s = s.replace("\u2029", "\\u2029");
            }

        }

        if (!isWritten)
        {
			startMapping(node);
            write(s);
			endMapping(node);
        }
    }
}
