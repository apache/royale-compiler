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

import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDualContentProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelRootNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Implementation of the {@code IMXMLModelNode} interface.
 */
class MXMLModelNode extends MXMLInstanceNode implements IMXMLModelNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLModelNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The sole child node.
     */
    private MXMLModelRootNode rootNode;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLModelID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.MODEL;
    }

    @Override
    public int getChildCount()
    {
        return rootNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? rootNode : null;
    }

    @Override
    public IMXMLModelRootNode getRootNode()
    {
        return rootNode;
    }

    @Override
    protected void initializeFromTag(MXMLTreeBuilder builder, IMXMLTagData tag)
    {
        RoyaleProject project = builder.getProject();
        String qname = project.getModelClass();
        setClassReference(project, qname);

        super.initializeFromTag(builder, tag);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_SOURCE))
        {
            // Resolve the attribute value to a normalized path.
            // Doing so makes this compilation unit dependent on that file.
            String sourcePath = resolveSourceAttributePath(builder, attribute, info);
            if (sourcePath != null)
            {
                IMXMLData mxmlData = builder.getExternalMXMLData(attribute, sourcePath);
                if (mxmlData != null)
                {
                    IMXMLTagData rootTag = mxmlData.getRootTag();

                    rootNode = new MXMLModelRootNode(this);
                    rootNode.initializeFromTag(builder, rootTag);
                }
            }
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag, MXMLNodeInfo info)
    {
        info.hasDualContent = true;

        if (rootNode == null)
        {
            rootNode = new MXMLModelRootNode(this);
            rootNode.initializeFromTag(builder, childTag);
        }
        else
        {
            // TODO Report a problem if more than one root node.
        }
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        if (info.hasSourceAttribute && info.hasDualContent)
        {
            ICompilerProblem problem = new MXMLDualContentProblem(tag, tag.getShortName());
            builder.addProblem(problem);
        }
    }
}
