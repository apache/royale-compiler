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

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.projects.ICompilerProject;

public class ConfigExpressionNode extends NamespaceAccessExpressionNode
{
    /**
     * Create {@code ConfigExpressionNode} from its two children.
     * 
     * @param left config namespace
     * @param operator {@code ::} operator
     * @param right config variable
     */
    public ConfigExpressionNode(IdentifierNode left, ASToken operator, IdentifierNode right)
    {
        super(left, operator, right);
        left.setParent(this);
        right.setParent(this);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected ConfigExpressionNode(ConfigExpressionNode other)
    {
        super(other);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    // TODO Remove unnecessary override.
    public void normalize(boolean fillInOffsets)
    {
        super.normalize(fillInOffsets);
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected ConfigExpressionNode copy()
    {
        return new ConfigExpressionNode(this);
    }
    
    //
    // Other methods
    //

    public IdentifierNode getConfigNamespaceNode()
    {
        return (IdentifierNode)leftOperandNode;
    }

    public String getConfigNamespace()
    {
        return leftOperandNode instanceof IdentifierNode ? ((IdentifierNode)leftOperandNode).getName() : null;
    }

    public IdentifierNode getConfigValueNode()
    {
        return (IdentifierNode)rightOperandNode;
    }

    public String getConfigValue()
    {
        return rightOperandNode instanceof IdentifierNode ? ((IdentifierNode)rightOperandNode).getName() : null;
    }

    public Object resolveConfigValue(ICompilerProject project)
    {
        Object value = null;
        
        IDefinition definition = resolve(project);
        if (definition instanceof ConstantDefinition)
            value = ((ConstantDefinition)definition).resolveValue(project);
        
        return value;
    }
}
