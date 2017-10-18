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

package org.apache.royale.compiler.tree.mxml;

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

/**
 * This AST node represents an MXML <code>&lt;Script&gt;</code> tag.
 * <p>
 * An {@link IMXMLScriptNode} has N child nodes representing the ActionScript
 * within the tag. For example, if the tag is
 * 
 * <pre>
 *   &lt;fx:Script&gt;
 *   <![CDATA[
 *     import spark.components.Button;
 *     private var b:Button;
 *   ]]>
 *   &lt;/fx:Script&gt;
 * then there will be two child nodes,
 * an {@link IImportNode} and an {@link IVariableNode}.
 * 
 * This interface extends the marker interface {@link IBlockNode} so that
 * code-assist features can find script nodes when looking for "blocks"
 */
public interface IMXMLScriptNode extends IMXMLNode, IBlockNode
{
    /**
     * Gets the parsed ActionScript code for &lt;Script&gt; tag.
     * 
     * @return An array of {@link IASNode} objects representing ActionScript
     * code.
     */
    IASNode[] getASNodes();
}
