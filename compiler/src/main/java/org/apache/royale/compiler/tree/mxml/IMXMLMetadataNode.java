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

import org.apache.royale.compiler.tree.metadata.IMetaTagNode;

/**
 * This AST node represents an MXML <code>&lt;Metadata&gt;</code> tag.
 * <p>
 * An {@link IMXMLMetadataNode} has N child nodes; each one is an
 * {@link IMetaTagNode} representing one metadata declaration inside the tag.
 */
public interface IMXMLMetadataNode extends IMXMLNode
{
    /**
     * Gets the nodes representing the metadata declarations inside the
     * <code>fx:Metadata</code> tag. These are the children of this node.
     * 
     * @return An array of {@link IMetaTagNode} objects.
     */
    IMetaTagNode[] getMetaTagNodes();
}
