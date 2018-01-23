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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ResourceBundleNoBundleParameterProblem;
import org.apache.royale.compiler.problems.ResourceBundleNoKeyParameterProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.mxml.IMXMLResourceNode;

/**
 * Node to represent the MXML @Resource compiler directive
 */
class MXMLResourceNode extends MXMLCompilerDirectiveNodeBase implements IMXMLResourceNode
{
    /**
     * Constructor.
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     * @param type The type of the value being set with this directive.
     */
    MXMLResourceNode(NodeBase parent, ITypeDefinition type)
    {
        super(parent);
        this.type = type;
    }

    /**
     * Bundle name which is the value of the 'bundle' parameter in the
     * associated @Resource compiler directive
     */
    private String bundleName;

    /**
     * Key which is the value of the 'key' parameter in the associated @Resource
     * compiler directive
     */
    private String key;

    /**
     * Type of the identifier which its value is set with this compiler
     * directive.
     */
    private ITypeDefinition type;

    @Override
    public String getBundleName()
    {
        return bundleName;
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public ITypeDefinition getType()
    {
        return type;
    }

    @Override
    public void initializeFromText(MXMLTreeBuilder builder,
                                   String text, ISourceLocation location)
    {
        parseTextAndSetAttributes(builder, text, location,
                                  IMetaAttributeConstants.ATTRIBUTE_RESOURCE);

        setSourceLocation(location);

        for (IMetaTagAttribute attr : attributes)
        {
            if (IMetaAttributeConstants.NAME_RESOURCE_BUNDLE.equals(attr.getKey()))
                bundleName = attr.getValue();
            else if (IMetaAttributeConstants.NAME_RESOURCE_KEY.equals(attr.getKey()))
                key = attr.getValue();
        }

        if (key != null && bundleName == null)
        {
            //If the bundle name isn't specified, use the class name.
            bundleName = ResourceBundleUtils.convertBundleNameToColonSyntax(
                    getClassDefinitionNode().getQualifiedName());
        }

        if (bundleName != null)
        {
            IFileNode fileNode = builder.getFileNode();
            if (fileNode instanceof IFileNodeAccumulator)
            {
                ((IFileNodeAccumulator)fileNode).addRequiredResourceBundle(bundleName);
            }
        }
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLResourceID;
    }

    @Override
    public String getName()
    {
        return "Resource";
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        if (key == null)
        {
            ICompilerProblem problem = new ResourceBundleNoKeyParameterProblem(this);
            builder.addProblem(problem);
        }

        if (bundleName == null)
        {
            ICompilerProblem problem = new ResourceBundleNoBundleParameterProblem(this);
            builder.addProblem(problem);
        }
    }
}
