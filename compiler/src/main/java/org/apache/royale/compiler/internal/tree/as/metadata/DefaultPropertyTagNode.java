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

package org.apache.royale.compiler.internal.tree.as.metadata;

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.metadata.IDefaultPropertyTagNode;

/**
 * Implementation of {@link IDefaultPropertyTagNode}.
 */
public class DefaultPropertyTagNode extends MetaTagNode implements IDefaultPropertyTagNode
{
    /**
     * Constructor.
     */
    public DefaultPropertyTagNode()
    {
        super(IMetaAttributeConstants.ATTRIBUTE_DEFAULTPROPERTY);
    }

    private IdentifierNode propertyNameNode;

    public void setPropertyNameNode(IdentifierNode propertyNameNode)
    {
        this.propertyNameNode = propertyNameNode;
        propertyNameNode.setParent(this);
        addToMap(SINGLE_VALUE, propertyNameNode.getName());
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(propertyNameNode, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 1;
    }

    @Override
    public IVariableDefinition resolveProperty(ICompilerProject project)
    {
        if (propertyNameNode == null)
            return null;
        
        IDefinition definition = propertyNameNode.resolve(project);
        if (!(definition instanceof IVariableDefinition))
            return null;
        
        return (IVariableDefinition)definition;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DefaultPropertyTagNode)
        {
            if (!equals(((DefaultPropertyTagNode)obj).propertyNameNode, propertyNameNode))
                return false;
        }
        else
        {
            return false;
        }

        return super.equals(obj);
    }
}
