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

package org.apache.royale.compiler.internal.as.codegen;

import jburg.burg.emitlangs.EmitLang;
import jburg.burg.inode.InodeAdapter;

/**
 *  IASNodeAdapter generates method calls into
 *  a BURM to access properties of an IASNode.
 */
public class IASNodeAdapter implements InodeAdapter
{
	/**
	 *  @return true if the adapter can handle this type of inode.
	 */
	public boolean accept(String inodeClassName)
	{
        return "IASNode".equals(inodeClassName);
    }

    /**
     *  Generate an expression to fetch a node's child count.
     *  @param node_path - the code generator's path to the node.
     *  @param emitter - the compiler-compiler's code emitter.
     *  @return said expression.
     */
	public String genGetArity(String node_path, EmitLang emitter)
	{
		return emitter.genCallMethod("SemanticUtils", "getChildCount", new String[] { node_path } );
	}

	/**
	 *  Generate an expression to fetch a node's Nth child.
	 *  @param node_path - the code generator's path to the node.
	 *  @param index - the index expression. 
	 *  @param emitter - the compiler-compiler's code emitter.
     *  @return said expression.
	 */
	public String genGetNthChild(String node_path, String index, EmitLang emitter)
	{
		return emitter.genCallMethod("SemanticUtils", "getNthChild", new String[] { node_path, index } );
	}

	/**
	 *  Generate an expression to fetch a node's node ID.
	 *  @param node_path - the code generator's path to the node.
     *  @param emitter - the compiler-compiler's code emitter.
     *  @return said expression.
	 */
	public String genGetOperator(String node_path, EmitLang emitter)
	{
		return emitter.genCallMethod(node_path, "getNodeID", null);
	}
}

