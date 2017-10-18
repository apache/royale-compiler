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

package org.apache.royale.compiler.internal.definitions;

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IMetadataDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * This is the abstract base class for definitions in the symbol table that that
 * are declared by metadata tags on other definitions.
 */
public abstract class MetadataDefinitionBase extends DefinitionBase implements IMetadataDefinition
{
    public MetadataDefinitionBase(String name, String tagName, IClassDefinition decoratedDefinition)
    {
        super(name);
        this.decoratedDefinition = decoratedDefinition;
        setNamespaceReference(NamespaceDefinition.getPublicNamespaceDefinition());
        this.tagName = tagName;
    }

    private IDefinition decoratedDefinition;

    private final String tagName;

    private IMetaTagAttribute[] attributes;

    @Override
    public IDefinition getDecoratedDefinition()
    {
        return decoratedDefinition;
    }

    public IMetaTagsNode getTagNode()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStart()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getEnd()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     */
    public IDefinitionNode getDecoratedDefinitionNode()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     */
    public String getTagName()
    {
        return tagName;
    }

    /**
     */
    public IMetaTagAttribute[] getAttributes()
    {
        return attributes;
    }

    void setAttributes(IMetaTagAttribute[] attributes)
    {
        this.attributes = attributes;
    }

    public String getAttributeValue(String key)
    {
        if (attributes != null)
        {
            for (IMetaTagAttribute attribute : attributes)
            {
                if (attribute.getKey().equals(key))
                    return attribute.getValue();
            }
        }
        return null;
    }

    @Override
    public boolean matches(DefinitionBase definition)
    {
        if (this == definition)
            return true;
        if (getClass() != definition.getClass())
            return false;
        assert definition instanceof MetadataDefinitionBase;
        MetadataDefinitionBase other = (MetadataDefinitionBase)definition;
        assert tagName.equals(other.tagName);
        if (!getBaseName().equals(other.getBaseName()))
            return false;
        assert decoratedDefinition instanceof DefinitionBase;
        assert other.decoratedDefinition instanceof DefinitionBase;
        if (!((DefinitionBase)decoratedDefinition).matches((DefinitionBase)other.decoratedDefinition))
            return false;
        return super.matches(definition);
    }

    @Override
    public String getDeprecatedSince()
    {
        return getAttributeValue(IMetaAttributeConstants.NAME_DEPRECATED_METADATA_SINCE);
    }

    @Override
    public String getDeprecatedReplacement()
    {
        return getAttributeValue(IMetaAttributeConstants.NAME_DEPRECATED_METADATA_REPLACEMENT);
    }

    @Override
    public String getDeprecatedMessage()
    {
        return getAttributeValue(IMetaAttributeConstants.NAME_DEPRECATED_METADATA_MESSAGE);
    }
}
