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
import org.apache.royale.compiler.tree.metadata.IExcludeTagNode;

/**
 * Implementation of {@link IExcludeTagNode}.
 */
public class ExcludeTagNode extends MetaTagNode implements IExcludeTagNode
{
    /**
     * Constructor.
     */
    public ExcludeTagNode()
    {
        super(IMetaAttributeConstants.ATTRIBUTE_EXCLUDE);
    }

    private IdentifierNode nameNode;

    public void setNameNode(IdentifierNode nameNode)
    {
        this.nameNode = nameNode;
        nameNode.setParent(this);
        addToMap(IMetaAttributeConstants.NAME_EXCLUDE_NAME, nameNode.getName());
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(nameNode, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 1;
    }

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        if (nameNode == null)
            return null;
        
        return nameNode.resolve(project);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ExcludeTagNode)
        {
            if (!equals(((ExcludeTagNode)obj).nameNode, nameNode))
                return false;
        }
        else
        {
            return false;
        }

        return super.equals(obj);
    }
}
