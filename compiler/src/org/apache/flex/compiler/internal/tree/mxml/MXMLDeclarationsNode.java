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

package org.apache.flex.compiler.internal.tree.mxml;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.scopes.MXMLFileScope;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.mxml.IMXMLLanguageConstants;
import org.apache.flex.compiler.mxml.MXMLTagData;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;

class MXMLDeclarationsNode extends MXMLNodeBase implements IMXMLDeclarationsNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLDeclarationsNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The child nodes.
     */
    private IMXMLInstanceNode[] declarationInstances;

    @Override
    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return new MXMLNodeInfo(builder);
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, MXMLTagData tag,
                                   MXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        MXMLFileScope fileScope = builder.getFileScope();

        if (fileScope.isComponentTag(childTag))
        {
            MXMLComponentNode componentNode = new MXMLComponentNode(this);
            componentNode.initializeFromTag(builder, childTag);
            info.addChildNode(componentNode);
        }
        else if (fileScope.isModelTag(childTag))
        {
            MXMLModelNode modelNode = new MXMLModelNode(this);
            modelNode.initializeFromTag(builder, childTag);
            info.addChildNode(modelNode);
        }
        else
        {
            FlexProject project = builder.getProject();
            IDefinition definition = builder.getFileScope().resolveTagToDefinition(childTag);
            if (definition instanceof ClassDefinition)
            {
                MXMLInstanceNode instanceNode = MXMLInstanceNode.createInstanceNode(
                        builder, definition.getQualifiedName(), this);
                instanceNode.setClassReference(project, (IClassDefinition)definition); // TODO Move this logic to initializeFromTag().
                instanceNode.initializeFromTag(builder, childTag);
                info.addChildNode(instanceNode);
            }
            else
            {
                super.processChildTag(builder, tag, childTag, info);
            }
        }
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, MXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        declarationInstances = info.getChildNodeList().toArray(new MXMLInstanceNode[0]);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLDeclarationsID;
    }

    @Override
    public IASNode getChild(int i)
    {
        return declarationInstances != null ? declarationInstances[i] : null;
    }

    @Override
    public int getChildCount()
    {
        return declarationInstances != null ? declarationInstances.length : 0;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.DECLARATIONS;
    }

    @Override
    public IMXMLInstanceNode[] getDeclarationInstanceNodes()
    {
        return declarationInstances;
    }

    void setDeclarationInstanceNodes(IMXMLInstanceNode[] declarationInstances)
    {
        this.declarationInstances = declarationInstances;
    }
}
