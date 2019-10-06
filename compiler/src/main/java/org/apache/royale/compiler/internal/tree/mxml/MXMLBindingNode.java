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

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLEmptyAttributeProblem;
import org.apache.royale.compiler.problems.MXMLRequiredAttributeProblem;
import org.apache.royale.compiler.problems.MXMLSameBindingSourceAndDestinationProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingAttributeNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Implementation of the {@code IMXMLBindingNode} interface.
 */
public class MXMLBindingNode extends MXMLNodeBase implements IMXMLBindingNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLBindingNode(NodeBase parent)
    {
        super(parent);
    }

    private IMXMLBindingAttributeNode[] children = null;

    private MXMLBindingAttributeNode sourceAttributeNode = null;

    private MXMLBindingAttributeNode destinationAttributeNode = null;

    private boolean twoWay = false;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLBindingID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.BINDING;
    }

    @Override
    public int getChildCount()
    {
        return children != null ? children.length : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        return children != null ? children[i] : null;
    }

    @Override
    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return new MXMLNodeInfo(builder);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_SOURCE))
        {
            sourceAttributeNode = new MXMLBindingAttributeNode(this);
            sourceAttributeNode.initializeFromAttribute(builder, attribute);
            info.addChildNode(sourceAttributeNode);
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_DESTINATION))
        {
            destinationAttributeNode = new MXMLBindingAttributeNode(this);
            destinationAttributeNode.initializeFromAttribute(builder, attribute);
            info.addChildNode(destinationAttributeNode);
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_TWO_WAY))
        {
            String value = attribute.getMXMLDialect().trim(attribute.getRawValue());
            if (value.equals(IASLanguageConstants.TRUE))
                twoWay = true;
            else if (value.equals(IASLanguageConstants.FALSE))
                twoWay = false;
            else
            {
                // TODO Report a problem;
            }
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        children = info.getChildNodeList().toArray(new IMXMLBindingAttributeNode[0]);

        // Add an expression dependency on mx.binding.BindingManager.
        RoyaleProject project = builder.getProject();
        builder.addExpressionDependency(project.getBindingManagerClass());

        // Do some semantic checks.

        IMXMLTagAttributeData sourceAttribute = tag.getTagAttributeData(ATTRIBUTE_SOURCE);
        IMXMLTagAttributeData destinationAttribute = tag.getTagAttributeData(ATTRIBUTE_DESTINATION);

        String trimmedSourceValue = null;
        String trimmedDestinationValue = null;

        if (sourceAttribute == null)
        {
            // 'source' attribute is required
            ICompilerProblem problem = new MXMLRequiredAttributeProblem(tag, ATTRIBUTE_SOURCE);
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
        else
        {
            trimmedSourceValue = builder.getMXMLDialect().trim(sourceAttribute.getRawValue());
            if (trimmedSourceValue.isEmpty())
            {
                // 'source' attribute value cannot be empty
                ICompilerProblem problem = new MXMLEmptyAttributeProblem(sourceAttribute);
                builder.addProblem(problem);
                markInvalidForCodeGen();
            }
        }

        if (destinationAttribute == null)
        {
            // 'destination' attribute is required
            ICompilerProblem problem = new MXMLRequiredAttributeProblem(tag, ATTRIBUTE_DESTINATION);
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
        else
        {
            trimmedDestinationValue = builder.getMXMLDialect().trim(destinationAttribute.getRawValue());
            if (trimmedDestinationValue.isEmpty())
            {
                // 'destination' attribute value cannot be empty
                ICompilerProblem problem = new MXMLEmptyAttributeProblem(destinationAttribute);
                builder.addProblem(problem);
                markInvalidForCodeGen();
            }
        }

        if (trimmedSourceValue != null && !trimmedSourceValue.isEmpty() &&
            trimmedDestinationValue != null && !trimmedDestinationValue.isEmpty() &&
            trimmedSourceValue.equals(trimmedDestinationValue))
        {
            // 'source' and 'destination' values cannot be the same
            // TODO "a.b" and "a . b" won't be detected as the same.
            // Should we compare tokens instead?
            ICompilerProblem problem = new MXMLSameBindingSourceAndDestinationProblem(tag);
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
    }

    @Override
    public IMXMLBindingAttributeNode getSourceAttributeNode()
    {
        return sourceAttributeNode;
    }

    @Override
    public IMXMLBindingAttributeNode getDestinationAttributeNode()
    {
        return destinationAttributeNode;
    }

    @Override
    public boolean getTwoWay()
    {
        return twoWay;
    }

    /**
     * For debugging only. Builds a string such as <code>twoWay="true"</code>
     * from the attribute name.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append("twoWay");
        sb.append('=');
        sb.append('"');
        sb.append(twoWay);
        sb.append('"');

        return true;
    }

    /**
     * Set the destination attribute node - used when creating synthetic
     * MXMLBindingNodes
     */
    void setDestinationAttributeNode(MXMLBindingAttributeNode dest)
    {
        this.destinationAttributeNode = dest;
    }

    /**
     * Set the source attribute node - used when creating synthetic
     * MXMLBindingNodes
     */
    void setSourceAttributeNode(MXMLBindingAttributeNode src)
    {
        this.sourceAttributeNode = src;
    }
}
