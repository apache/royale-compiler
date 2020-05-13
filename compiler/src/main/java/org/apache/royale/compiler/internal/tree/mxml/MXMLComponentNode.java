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
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLSemanticProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLComponentNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

class MXMLComponentNode extends MXMLFactoryNode implements IMXMLComponentNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLComponentNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The name of the class as specified by the <code>className</code>
     * attribute. This may be <code>null</code>.
     */
    private String className;

    /**
     * The class-defining node which is the sole child of this node.
     */
    private MXMLClassDefinitionNode containedClassDefinitionNode;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLComponentID;
    }

    @Override
    public String getName()
    {
        IClassDefinition classDefinition = getContainedClassDefinition();
        // The classDefinition can be null when Component tag is partially written
        // i.e., with no component inside it
        // e.g. <fx:Component></fx:Component>
        return classDefinition != null ? classDefinition.getBaseName() : "";
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
        if (attribute.isSpecialAttribute(ATTRIBUTE_CLASS_NAME))
        {
            if (className == null)
            {
                // TODO Check that the class name is a valid AS identifier
                // and doesn't have conflicts with other names.
                String rawValue = attribute.getRawValue();
                if (rawValue != null)
                    className = attribute.getMXMLDialect().trim(rawValue);
            }
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
                return;
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

            // Find the ClassDefinition that was created for the component class.
            MXMLFileScope fileScope = builder.getFileScope();
            ClassDefinition fxComponentClassDefinition =
                    fileScope.getClassDefinitionForComponentTag(tag);

            assert fxComponentClassDefinition != null : "MXMLScopeBuilder failed to build a class for an fx:Component";

            // attach scope with the component class definition node.
            TypeScope componentClassScope = (TypeScope)fxComponentClassDefinition.getContainedScope();
            containedClassDefinitionNode.setScope(componentClassScope); // TODO Move this logic to initializeFromTag().

            // Connect node to definitions and vice versa.
            containedClassDefinitionNode.setClassReference(project, tagDefinition); // TODO Move this logic to initializeFromTag().
            containedClassDefinitionNode.setClassDefinition(fxComponentClassDefinition); // TODO Move this logic to initializeFromTag().

            int nameStart = fxComponentClassDefinition.getNameStart();
            int nameEnd = fxComponentClassDefinition.getNameEnd();
            fxComponentClassDefinition.setNode(containedClassDefinitionNode);
            // TODO The above call is setting nameStart and nameEnd to -1
            // because the MXML class definition node doesn't have a name expression node.
            // We need to reset the correct nameStart and nameEnd.
            fxComponentClassDefinition.setNameLocation(nameStart, nameEnd);

            containedClassDefinitionNode.initializeFromTag(builder, childTag);
        }
        if (!handled)
        {
            super.processChildTag(builder, tag, childTag, info);
        }
    }

    @Override
    public String getClassName()
    {
        return className;
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
