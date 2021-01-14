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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.MXMLUnresolvedTagProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;

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
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
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
            RoyaleProject project = builder.getProject();
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
                MXMLUnresolvedTagProblem problem = new MXMLUnresolvedTagProblem(childTag);
                builder.addProblem(problem);
                return;
            }
        }
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
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
