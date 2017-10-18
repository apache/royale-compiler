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

package org.apache.royale.compiler.internal.testing;

import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * Formats a tree of IASNodes into an XML string.
 * 
 * @note External tools depend on this XML format.
 */
public class NodesToXMLStringFormatter
{
	StringBuffer buffer;
	int level = 0;
	boolean escapeDescriptions;
	
	/**
	 *  Construct a NodeFormatter and generate a string
	 *  representation of the input tree.
	 *  @param n - the AST tree to be formatted.
	 *  @param escape_descriptions - when true, encode
	 *    descriptive text as UTF-8.
	 */
	public NodesToXMLStringFormatter(IASNode n, boolean escape_descriptions)
	{
		this.buffer = new StringBuffer();
		this.escapeDescriptions = escape_descriptions;
		traverse(n);
	}
	
	/**
     *  Construct a NodeFormatter and generate a string
     *  representation of the input tree, with the node
     *  descriptions encoded as UTF-8.
     *  @param n - the AST tree to be formatted.
     */
	public NodesToXMLStringFormatter(IASNode n)
	{
	    this(n, true);
	}
	
	void traverse(IASNode n) {
        if ( n == null )
            return;

		String node_name = n.getNodeID().toString();
		boolean has_kids = n.getChildCount() > 0 ;
		startLine();
		
		buffer.append("<");
		buffer.append(node_name);
		buffer.append(" location=\"");
		buffer.append(n.getLine());
		buffer.append(",");
		buffer.append(n.getColumn());
		buffer.append("\"");
		buffer.append(" description=\"");
		
		String node_description = ((NodeBase)n).getInnerString();
		if (node_description.startsWith("\"") && node_description.endsWith("\""))
		    node_description = node_description.substring(1, node_description.length() - 2);
		
		if ( this.escapeDescriptions )
		{
			try
			{
			    node_description = java.net.URLEncoder.encode(node_description, "UTF-8");
			}
			catch ( Exception cant_encode )
			{
			    node_description = "";
			}
		}
		buffer.append(node_description);
		buffer.append("\"");
		if ( has_kids)
		{
			buffer.append(">\n");
            level++;
			
			for ( int i = 0; i < n.getChildCount(); i++ )
				traverse(n.getChild(i));
			level--;
			
			startLine();
			buffer.append("</" + node_name + ">");
		}
		else
		{
			buffer.append("/>");
		}
		
		buffer.append("\n");
		
	}
	
	@Override
	public String toString() {
		return ( buffer != null )?
			buffer.toString()
		:
			"-nothing-";
	}
	
	private void startLine()
	{
	    for ( int i = 0; i < this.level; i++)
            buffer.append("  ");
	}
}
