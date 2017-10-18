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

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLIncompatibleVectorElementProblem;
import org.apache.royale.compiler.problems.MXMLInvalidVectorFixedAttributeProblem;
import org.apache.royale.compiler.problems.MXMLInvalidVectorTypeAttributeProblem;
import org.apache.royale.compiler.problems.MXMLMissingVectorTypeAttributeProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.AppliedVectorDefinitionFactory;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLVectorNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

class MXMLVectorNode extends MXMLInstanceNode implements IMXMLVectorNode
{
    private static final String DOT_LESS_THAN_ESCAPED = ".&lt;";
    private static final String GREATER_THAN_ESCAPED = "&gt;";

    /**
     * This AST node represents an MXML &lt;Vector&gt; tag.
     */
    MXMLVectorNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The type of the Vector's elements, as determined by the <code>type</code>
     * attribute.
     */
    private ITypeDefinition type;

    /**
     * Whether the Vector is fixed in size or not, as determined by the
     * <code>fixed</code> attribute.
     */
    private Boolean fixed = false;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLVectorID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.Vector;
    }

    @Override
    public IClassDefinition getClassReference(ICompilerProject project)
    {
        // The classReference set onto AppliedVectorDefinition
        // is to the bare  __AS3__.vec.Vector class,
        // which is what the <Vector> tag maps to.
        // But we really want it to be the Vector.<T> class
        // specified by the type="T" attribute.
        ASProjectScope projectScope = (ASProjectScope)project.getScope();
        return projectScope.newVectorClass(type);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        String value = attribute.getRawValue();

        if (attribute.isSpecialAttribute(ATTRIBUTE_TYPE))
        {
            RoyaleProject project = builder.getProject();
            IDefinition def = resolveElementType(value, project);

            if (def instanceof ITypeDefinition)
            {
                type = (ITypeDefinition)def;
            }
            else
            {
                ICompilerProblem problem = new MXMLInvalidVectorTypeAttributeProblem(attribute);
                builder.addProblem(problem);
            }
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_FIXED))
        {
            // TODO Improve this when we implement type recognition for text values.
            value = attribute.getMXMLDialect().trim(value);
            if (value.equals(IASLanguageConstants.TRUE))
            {
                fixed = true;
            }
            else if (value.equals(IASLanguageConstants.FALSE))
            {
                fixed = false;
            }
            else
            {
                ICompilerProblem problem = new MXMLInvalidVectorFixedAttributeProblem(attribute);
                builder.addProblem(problem);
            }
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    /**
     * Helper method to convert the type string into an IDefinition. This method
     * will handle nested vectors, like a Vector of Vectors of Vectors of Foo.
     */
    private IDefinition resolveElementType(String value, RoyaleProject project)
    {
        IDefinition def = null;
        int dotLessIdx = value.indexOf(DOT_LESS_THAN_ESCAPED);
        if (dotLessIdx != -1)
        {
            int endIdx = value.lastIndexOf(GREATER_THAN_ESCAPED);
            if (endIdx != -1)
            {
                // We have something that looks like a parameterized type - pull apart the base name
                // and type arg, and try to resolve them to a Vector.
                // When we add real generic support, we'll need to revisit this code

                // Resolve the element type - it could be yet another Vector, so call this method again
                // e.g. Vector.<Vector.<Vector.<Vector.<Turtles>>>>
                String typeArg = value.substring(dotLessIdx + 5, endIdx);
                IDefinition typeParam = resolveElementType(typeArg, project);

                String baseName = value.substring(0, dotLessIdx);

                IDefinition baseType = null;
                // Get the base type
                if (baseName.equals(IASLanguageConstants.Vector))
                    baseType = project.getBuiltinType(IASLanguageConstants.BuiltinType.VECTOR);
                else
                    baseType = project.getScope().findDefinitionByName(baseName);

                if (baseType instanceof ITypeDefinition && typeParam instanceof ITypeDefinition)
                {
                    IDefinition vectorType = project.getBuiltinType(IASLanguageConstants.BuiltinType.VECTOR);

                    if (baseType == vectorType) //only vectors are parameterizable today
                    {
                        def = AppliedVectorDefinitionFactory.newVector(project, (ITypeDefinition)typeParam);
                    }
                }
            }
        }

        if (def == null)
            // Not a Vector, so just have the project resolve the type
            def = project.getScope().findDefinitionByName(value);

        return def;
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        RoyaleProject project = builder.getProject();
        String tagName = builder.getFileScope().resolveTagToQualifiedName(childTag);
        IDefinition definition = project.getScope().findDefinitionByName(tagName);
        if (definition instanceof ClassDefinition)
        {
            // Check whether the child tag is compatible with the 'type' attribute.
            // Have to get the type from the child tag, in case it's another Vector type,
            // which won't have a simple string type.
            MXMLInstanceNode instanceNode =
                    MXMLInstanceNode.createInstanceNode(builder, tagName, this);
            instanceNode.setClassReference(project, (ClassDefinition)definition); // TODO Move this logic to initializeFromTag().
            instanceNode.initializeFromTag(builder, childTag);
            info.addChildNode(instanceNode);
            if (!instanceNode.getClassReference(project).isInstanceOf(type, project))
            {
                ICompilerProblem problem = new MXMLIncompatibleVectorElementProblem(childTag);
                builder.addProblem(problem);
            }
        }
        else
        {
            super.processChildTag(builder, tag, childTag, info);
        }
    }

    void initializeDefaultProperty(MXMLTreeBuilder builder, IVariableDefinition defaultPropertyDefinition,
            List<IMXMLUnitData> contentUnits)
    {
        RoyaleProject project = builder.getProject();

        ITypeDefinition typeDef = defaultPropertyDefinition.resolveType(project);
        
        String typeName = typeDef.getQualifiedName();
        typeName = typeName.replace(".<", DOT_LESS_THAN_ESCAPED);
        typeName = typeName.replace(">", GREATER_THAN_ESCAPED);
        type = (ITypeDefinition)resolveElementType(typeName, project);
        
        // Set the location of the implicit array node
        // to span the tags that specify the default property value.
        setLocation(builder, contentUnits);

        setClassReference(project, IASLanguageConstants.Vector);

        List<IMXMLNode> children = new ArrayList<IMXMLNode>();
        for (IMXMLUnitData unit : contentUnits)
        {
            if (unit instanceof IMXMLTagData)
            {
                IMXMLTagData tag = (IMXMLTagData)unit;
                // While it is normally illegal to put
                // script tags in an array, a default property
                // tag sequence can contain script nodes which need
                // to be children of the implicit array node to
                // keep the tree in file offset order.
                // See: http://bugs.adobe.com/jira/browse/CMP-955
                if (builder.getFileScope().isScriptTag(tag))
                {
                    MXMLScriptNode scriptNode = new MXMLScriptNode(this);
                    scriptNode.initializeFromTag(builder, tag);
                    children.add(scriptNode);
                }
                else
                {
                    IDefinition definition = builder.getFileScope().resolveTagToDefinition(tag);
                    if (definition instanceof IClassDefinition)
                    {
                        MXMLInstanceNode childNode = MXMLInstanceNode.createInstanceNode(
                                builder, definition.getQualifiedName(), this);
                        childNode.setClassReference(project, (ClassDefinition)definition); // TODO Move this logic to initializeFromTag().
                        childNode.initializeFromTag(builder, tag);
                        children.add(childNode);
                    }
                }
            }
        }
        setChildren(children.toArray(new IMXMLNode[0]));
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        setChildren(info.getChildNodeList().toArray(new MXMLInstanceNode[0]));

        // 'type' is a required attribute, and it makes this MXML compilation unit
        // dependent on the compilation unit for the class or interface specified by 'type'.
        if (type != null)
        {
            RoyaleProject project = builder.getProject();

            ITypeDefinition t = type;
            // for Vectors we want to create a dependency on the element type - we don't
            // need a dependency on Vector itself, as it's builtin
            while (t instanceof AppliedVectorDefinition)
            {
                t = ((AppliedVectorDefinition)t).resolveElementType(project);
            }

            String qname = t.getQualifiedName();
            builder.addDependency(qname, DependencyType.EXPRESSION);
            if (getID() != null)
                builder.addDependency(qname, DependencyType.SIGNATURE);
        }
        else
        {
            ICompilerProblem problem = new MXMLMissingVectorTypeAttributeProblem(tag);
            builder.addProblem(problem);
        }
    }

    @Override
    public ITypeDefinition getType()
    {
        return type;
    }

    @Override
    public boolean getFixed()
    {
        return fixed;
    }
}
