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

package org.apache.royale.compiler.internal.codegen.typedefs;

import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.filespecs.IFileSpecification;

// used to generate errors
public class DummyNode extends SourceLocation implements IASNode
{
    /**
     * Get the opcode of this node
     *
     * @return the opcode - this is one of the constants defined in ASTConstants
     */
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.UnknownID;
    }
    
    /**
     * Determine whether the offset fits within this node.
     *
     * @param offset the offset to test
     * @return true if the offset is contained within this node
     */
    public boolean contains(int offset)
    {
        return false;
    }
    
    /**
     * Get the nearest ancestor of this node that has the specified type.
     *
     * @param nodeType the node type for which to search
     * @return the nearest ancestor that has the specified type (null if no such
     * node exists)
     */
    public IASNode getAncestorOfType(Class<? extends IASNode> nodeType)
    {
        return null;
    }
    
    /**
     * Get a particular child of this node
     *
     * @param i the child's index
     * @return the specified child
     */
    public IASNode getChild(int i)
    {
        return null;
    }
    
    /**
     * Get the number of children
     *
     * @return the number of children
     */
    public int getChildCount()
    {
        return 0;
    }
    
    /**
     * Gets the child node that contains the offset
     *
     * @param offset an offest
     * @return an IASNode or null
     */
    public IASNode getContainingNode(int offset)
    {
        return null;
    }
    
    /**
     * Gets the containing scope for this node
     *
     * @return the current {@link IScopedNode}
     */
    public IScopedNode getContainingScope()
    {
        return null;
    }
    
    /**
     * Get package name that applies to this node. If this node doesn't reside
     * inside a package definition, this method will return null.
     *
     * @return String containing fully-qualified package name
     */
    public String getPackageName()
    {
        return null;
    }
    
    /**
     * Get the parent of this node
     *
     * @return the parent of this node
     */
    public IASNode getParent()
    {
        return null;
    }
    
    /**
     * Get the {@link IFileSpecification} that produced this node
     *
     * @return the source of this node
     */
    public IFileSpecification getFileSpecification()
    {
        return null;
    }
    
    /**
     * Gets the local offset where the node starts, including any extra items that may
     * change the appearance of the node's start. These can include namespaces,
     * keywords, modifiers, etc
     *
     * @return the start of the span
     */
    public int getSpanningStart()
    {
        return 0;
    }
    
    /**
     * Get the first node that succeeds the offset (i.e. which starts after the
     * offset).
     *
     * @param offset the offset for which to search
     * @return the most general node that starts after the offset (or null, if
     * the offset isn't contained within this node)
     */
    public IASNode getSucceedingNode(int offset)
    {
        return null;
    }
    
    /**
     * Determines if this node is a terminal node, meaning it cannot have
     * children
     *
     * @return true if this node is terminal
     */
    public boolean isTerminal()
    {
        return true;
    }
}
