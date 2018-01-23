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
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLEmptyAttributeProblem;
import org.apache.royale.compiler.problems.MXMLInvalidDefinitionNameProblem;
import org.apache.royale.compiler.problems.MXMLRequiredAttributeProblem;
import org.apache.royale.compiler.problems.MXMLSemanticProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDefinitionNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * MXML syntax tree node for &lt;fx:Definition&gt; tags.
 */
class MXMLDefinitionNode extends MXMLNodeBase implements IMXMLDefinitionNode
{
    /**
     * @param parent
     */
    MXMLDefinitionNode(NodeBase parent)
    {
        super(parent);
    }

    private String definitionName;

    private MXMLClassDefinitionNode containedClassDefinitionNode;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLDefinitionID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.DEFINITION;
    }

    @Override
    public int getChildCount()
    {
        return containedClassDefinitionNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? containedClassDefinitionNode : null;
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_NAME))
        {
            if (definitionName == null)
            {
                String rawValue = attribute.getRawValue();
                if (rawValue != null)
                    definitionName = attribute.getMXMLDialect().trim(rawValue);
            }

            // TODO Report problems the definition name is set twice or is
            // set to a name that is not a legal xml tag name.
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        boolean handled = false;
        if (containedClassDefinitionNode == null)
        {
            containedClassDefinitionNode = new MXMLClassDefinitionNode(this);
            handled = true;

            // check also that there is only one child tag and that
            // there are no text units around the child tag
            RoyaleProject project = builder.getProject();
            IDefinition tagDef = builder.getFileScope().resolveTagToDefinition(childTag);

            // Check that the root tag mapped to a definition.
            if (tagDef == null)
            {
                // TODO Add a problem subclass for this.
                ICompilerProblem problem = new MXMLSemanticProblem(childTag);
                builder.addProblem(problem);
            }

            // Check that the definition is for a class.
            if (!(tagDef instanceof IClassDefinition))
            {
                // TODO Add a problem subclass for this.
                ICompilerProblem problem = new MXMLSemanticProblem(childTag);
                builder.addProblem(problem);
            }

            IClassDefinition tagDefinition = (IClassDefinition)tagDef;

            // Check that the class is not final.
            if (tagDefinition.isFinal())
            {
                // TODO Add a problem subclass for this.
                ICompilerProblem problem = new MXMLSemanticProblem(childTag);
                builder.addProblem(problem);
            }
            
            MXMLFileScope fileScope = builder.getFileScope();
            ClassDefinition definitionTagClass =
                    fileScope.getClassDefinitionForDefinitionTag(tag);

            assert tagDefinition == definitionTagClass.resolveBaseClass(project);

            containedClassDefinitionNode.setClassReference(project, tagDefinition); // TODO Move this logic to initializeFromTag().
            containedClassDefinitionNode.setClassDefinition(definitionTagClass); // TODO Move this logic to initializeFromTag().
            containedClassDefinitionNode.initializeFromTag(builder, childTag);

            int nameStart = definitionTagClass.getNameStart();
            int nameEnd = definitionTagClass.getNameEnd();
            definitionTagClass.setNode(containedClassDefinitionNode);
            // TODO The above call is setting nameStart and nameEnd to -1
            // because the MXML class definition node doesn't have a name expression node.
            // We need to reset the correct nameStart and nameEnd.
            definitionTagClass.setNameLocation(nameStart, nameEnd);
        }
        if (!handled)
        {
            super.processChildTag(builder, tag, childTag, info);
        }
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag, MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        if (definitionName == null)
        {
            ICompilerProblem problem = new MXMLRequiredAttributeProblem(tag, ATTRIBUTE_NAME);
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
        else if (definitionName.isEmpty())
        {
            ICompilerProblem problem = new MXMLEmptyAttributeProblem(tag.getTagAttributeData(ATTRIBUTE_NAME));
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
        else if (!isValidXMLTagName(definitionName))
        {
            ICompilerProblem problem = new MXMLInvalidDefinitionNameProblem(tag.getTagAttributeData(ATTRIBUTE_NAME), definitionName);
            builder.addProblem(problem);
            markInvalidForCodeGen();
        }
    }

    @Override
    public String getDefinitionName()
    {
        return definitionName;
    }

    @Override
    public IMXMLClassDefinitionNode getContainedClassDefinitionNode()
    {
        return containedClassDefinitionNode;
    }

    @Override
    public IClassDefinition getContainedClassDefinition()
    {
        return containedClassDefinitionNode != null ?
                containedClassDefinitionNode.getClassDefinition() :
                null;
    }
}
