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

package org.apache.flex.compiler.internal.tree.as;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;

/**
 * A LabeledStatementNode contains an ActionScript label and its associated
 * statements, and provides a specific node type to facilitate recognition in
 * the syntax tree.
 */
// TODO Add interface for this class.
public class LabeledStatementNode extends FixedChildrenNode
{
    /**
     * Constructor.
     * 
     * @param labelIdentifierNode The node representing the label.
     */
    public LabeledStatementNode(NonResolvingIdentifierNode labelIdentifierNode)
    {
        this.labelIdentifierNode = labelIdentifierNode;
        labeledStatement = new BlockNode();
    }

    private final NonResolvingIdentifierNode labelIdentifierNode;
    
    private final BlockNode labeledStatement;
    
    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.LabledStatementID;
    }

    @Override
    public int getChildCount()
    {
        return 2;
    }

    @Override
    public IASNode getChild(int i)
    {
        switch (i)
        {
            case 0:
                return labelIdentifierNode;
                
            case 1:
                return labeledStatement;
        }
        
        return null;
    }

    //
    // Other methods
    //

    /**
     * @return The name of the label in this labeled statement node.
     */
    // TODO Add to interface.
    public String getLabel()
    {
        return labelIdentifierNode.getName();
    }

    /**
     * @return The tree node for the statement this labeled statement labels.
     */
    // TODO Add to interfaced.
    public BlockNode getLabeledStatement()
    {
        return labeledStatement;
    }
}
