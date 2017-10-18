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

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.mxml.StateDefinition;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IStateDefinition;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLSemanticProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStateNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

class MXMLStateNode extends MXMLInstanceNode implements IMXMLStateNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLStateNode(NodeBase parent)
    {
        super(parent);
    }

    private IStateDefinition stateDefinition;

    private String[] stateGroups;

    private String basedOn;

    @Override
    public String getStateName()
    {
        return stateDefinition.getBaseName();
    }

    @Override
    public int getNameStart()
    {
        return stateDefinition.getNameStart();
    }

    @Override
    public int getNameEnd()
    {
        return stateDefinition.getNameEnd();
    }

    @Override
    public int getNameAbsoluteStart()
    {
        return stateDefinition.getNode() != null ? stateDefinition.getNode().getNameAbsoluteStart() : getNameStart();
    }

    @Override
    public int getNameAbsoluteEnd()
    {
        return stateDefinition.getNode() != null ? stateDefinition.getNode().getNameAbsoluteEnd() : getNameEnd();
    }

    @Override
    public String getNamespace()
    {
        return INamespaceConstants.public_;
    }

    @Override
    public IExpressionNode getNameExpressionNode()
    {
        return null;
    }

    @Override
    public String[] getStateGroups()
    {
        return stateGroups != null ? stateGroups : new String[0];
    }

    @Override
    public String getBasedOn()
    {
        return basedOn;
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLStateID;
    }

    @Override
    public IMetaTagsNode getMetaTags()
    {
        return null;
    }
    
    @Override
    public IMetaInfo[] getMetaInfos()
    {
        return new IMetaInfo[0];
    }

    @Override
    public IDefinition getDefinition()
    {
        return stateDefinition;
    }

    @Override
    public String getQualifiedName()
    {
        return getName(); //What is the qualified name for this?
    }

    @Override
    public String getShortName()
    {
        return this.getStateName();
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        return stateDefinition.hasModifier(modifier);
    }

    @Override
    public boolean hasNamespace(String namespace)
    {
        return namespace.equals(INamespaceConstants.public_);
    }

    @Override
    public boolean isImplicit()
    {
        return stateDefinition.isImplicit();
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        String value = attribute.getRawValue();

        if (attribute.isSpecialAttribute(ATTRIBUTE_NAME))
        {

            stateDefinition = new StateDefinition(this, this.getASScope(),
                    value, attribute.getValueStart(), attribute.getValueEnd());
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_STATE_GROUPS))
        {
            stateGroups = attribute.getMXMLDialect().splitAndTrim(value);
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected String[] processIncludeInOrExcludeFromAttribute(MXMLTreeBuilder builder,
                                                              IMXMLTagAttributeData attribute)
    {
        // TODO Report the correct problem.
        ICompilerProblem problem = new MXMLSemanticProblem(attribute);
        builder.addProblem(problem);
        return null;
    }

    @Override
    protected String processItemCreationPolicyAttribute(MXMLTreeBuilder builder,
                                                        IMXMLTagAttributeData attribute)
    {
        // TODO Report the correct problem.
        ICompilerProblem problem = new MXMLSemanticProblem(attribute);
        builder.addProblem(problem);
        return null;
    }

    @Override
    protected String processItemDestructionPolicyAttribute(MXMLTreeBuilder builder,
                                                           IMXMLTagAttributeData attribute)
    {
        // TODO Report the correct problem.
        ICompilerProblem problem = new MXMLSemanticProblem(attribute);
        builder.addProblem(problem);
        return null;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        // Keep track of all States nodes on the class definition node,
        // for later state processing.
        ((MXMLClassDefinitionNode)getClassDefinitionNode()).addStateNode(this);
    }

    /**
     * For debugging only. Builds a string such as <code>"normal"</code> from
     * the state name.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getStateName());
        sb.append('"');

        return true;
    }
}
