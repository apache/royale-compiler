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

import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;

/**
 * This AST node represents the instance of
 * <code>mx.core.DeferredInstanceFromClass</code> or
 * <code>mx.core.DeferredInstanceFromFunction</code> that the compiler
 * implicitly creates as the value for a property of type
 * <code>mx.core.IDeferredInstance</code>.
 * <p>
 * An {@code IMXMLDeferredInstanceNode} has exactly one child, which can be
 * either an {@code IMXMLClassNode} (in the case of an
 * <code>DeferredInstanceFromClass</code>) or an {@code IMXMLInstanceNode} (in
 * the case of an <code>DeferredInstanceFromFunction</code)). Gordon Smith
 */
class MXMLDeferredInstanceNode extends MXMLInstanceNode implements IMXMLDeferredInstanceNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLDeferredInstanceNode(NodeBase parent)
    {
        super(parent);
    }

    private IMXMLInstanceNode childNode;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLDeferredInstanceID;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? childNode : null;
    }

    @Override
    public int getChildCount()
    {
        return childNode == null ? 0 : 1;
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        RoyaleProject project = builder.getProject();

        // Check whether the child tag is an instance tag.
        IDefinition definition = builder.getFileScope().resolveTagToDefinition(childTag);
        if (definition instanceof ClassDefinition)
        {
            // This node should generate an instance of mx.core.DeferredInstanceFromFunction.
            String qname = project.getDeferredInstanceFromFunctionClass();
            setClassReference(project, qname);
            builder.addExpressionDependency(qname);

            childNode = MXMLInstanceNode.createInstanceNode(
                    builder, definition.getQualifiedName(), this);
            ((MXMLInstanceNode)childNode).setClassReference(project, (ClassDefinition)definition); // TODO Move this logic to initializeFromTag().
            ((MXMLInstanceNode)childNode).initializeFromTag(builder, childTag);
        }
    }

    void initializeDefaultProperty(MXMLTreeBuilder builder, IVariableDefinition defaultPropertyDefinition,
                                   List<IMXMLUnitData> contentUnits)
    {
        RoyaleProject project = builder.getProject();

        assert (contentUnits.isEmpty()) ||
                (!builder.getFileScope().isScriptTag(contentUnits.get(0))) : "Script tags should not start a default property!";

        assert (contentUnits.isEmpty()) ||
                (!builder.getFileScope().isScriptTag(contentUnits.get(contentUnits.size() - 1))) : "Trailing script tags should be removed from default property content units!";

        // Set the location of the implicit deferred instance node
        // to span the tags that specify the default property value.
        setLocation(builder, contentUnits);

        setClassReference(project, project.getDeferredInstanceFromFunctionClass());

        // Determine whether the the default property is being set to a single Array tag.
        boolean isSingleArrayTag = false;
        int n = contentUnits.size();
        if (n == 1 && contentUnits.get(0) instanceof IMXMLTagData)
        {
            IMXMLTagData tag = (IMXMLTagData)contentUnits.get(0);
            IDefinition definition = builder.getFileScope().resolveTagToDefinition(tag);
            isSingleArrayTag = definition.getQualifiedName().equals(IASLanguageConstants.Array);
        }

        String instanceType = defaultPropertyDefinition.getInstanceType(project);
        if (IASLanguageConstants.Array.equals(instanceType) && !isSingleArrayTag)
        {
            // Create an implicit array node.
            childNode = new MXMLArrayNode(this);
            ((MXMLArrayNode)childNode).initializeDefaultProperty(
                    builder, defaultPropertyDefinition, contentUnits);
        }
        else if ((n == 1) && (!isSingleArrayTag))
        {
            IMXMLTagData tag = (IMXMLTagData)contentUnits.get(0);
            IDefinition type = builder.getFileScope().resolveTagToDefinition(tag);
            if (type instanceof ClassDefinition)
            {
                IClassDefinition tagClass = (IClassDefinition)type;
                MXMLInstanceNode childInstanceNode = MXMLInstanceNode.createInstanceNode(builder, type.getQualifiedName(), this);
                childInstanceNode.setClassReference(project, tagClass);
                childNode = childInstanceNode;
                childInstanceNode.initializeFromTag(builder, tag);
            }
        }
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        if (childNode == null)
        {
            ISourceLocation location = info.getSourceLocation();
            if (location == null)
                location = tag.getLocationOfChildUnits();

            ISourceFragment[] sourceFragments = info.getSourceFragments();

            initializeFromFragments(builder, location, sourceFragments);
        }
    }

    public void initializeFromFragments(MXMLTreeBuilder builder,
                                        ISourceLocation location,
                                        ISourceFragment[] fragments)
    {
        // This node represents an instance of mx.core.DeferredInstanceFromClass.
        RoyaleProject project = builder.getProject();
        String qname = project.getDeferredInstanceFromClassClass();
        setClassReference(project, qname);

        // Add an expression dependency on that class.
        builder.addExpressionDependency(qname);

        // The source fragments are a class name (possibly a complex one
        // like Vector.<Vector.<int>>. This is the class from which
        // we're making a DeferredInstanceFromClass object.
        // Create a child MXMLClassNode from it.
        childNode = new MXMLClassNode(this);
        ((MXMLClassNode)childNode).setClassReference(project, childNode.getName()); // TODO Move this logic to initializeFromText().
        ((MXMLClassNode)childNode).initializeFromFragments(builder, location, fragments);
    }

    @Override
    public IMXMLClassNode getClassNode()
    {
        return childNode instanceof IMXMLClassNode ? (IMXMLClassNode)childNode : null;
    }

    @Override
    public IMXMLInstanceNode getInstanceNode()
    {
        // Note that IMXMLClassNode extends IMXMLInstanceNode.
        return !(childNode instanceof IMXMLClassNode) ? childNode : null;
    }
}
