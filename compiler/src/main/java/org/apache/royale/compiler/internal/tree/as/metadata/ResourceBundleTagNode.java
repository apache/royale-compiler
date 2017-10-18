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
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.definitions.metadata.ResourceBundleMetaTag;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.metadata.IResourceBundleTagNode;

/**
 * Represents an ResourceBundle metadata tag, of the form
 * [ResourceBundle("bundlename")]
 */
public class ResourceBundleTagNode extends MetaTagNode implements IResourceBundleTagNode
{
    /**
     * Constructor.
     */
    public ResourceBundleTagNode()
    {
        super(IMetaAttributeConstants.ATTRIBUTE_RESOURCEBUNDLE);
    }

    private IdentifierNode bundleNameNode;

    /**
     * Sets the resource bundle name node
     * 
     * @param name resource bundle name node
     */
    public void setBundleNameNode(IdentifierNode name)
    {
        bundleNameNode = name;
        bundleNameNode.setParent(this);
        addToMap(SINGLE_VALUE, name.getName());

        IFileNode fileNode = (IFileNode)getAncestorOfType(IFileNode.class);
        if (fileNode instanceof IFileNodeAccumulator)
            ((IFileNodeAccumulator)fileNode).addRequiredResourceBundle(getBundleName());
    }

    /**
     * Returns the string representation of the resource bundle name as found in
     * metadata
     * 
     * @return resource bundle name.
     */
    private String getBundleName()
    {
        return bundleNameNode != null ? bundleNameNode.getName() : "";
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(bundleNameNode, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 1;
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ResourceBundleTagID;
    }

    @Override
    public IMetaTag buildMetaTag(IFileSpecification containingFileSpec, IDefinition definition)
    {
        String name = getTagName();
        MetaTag metaTag = new ResourceBundleMetaTag(definition, name, getAllAttributes(), getBundleName());
        metaTag.setLocation(containingFileSpec, getAbsoluteStart(), getAbsoluteEnd(), getLine(), getColumn());
        return metaTag;
    }
}
