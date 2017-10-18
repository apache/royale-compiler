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
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFactoryNode;

/**
 * This AST node represents the instance of <code>mx.core.ClassFactory</code>
 * that the compiler implicitly creates as the value for a property of type
 * <code>mx.core.IFactory</code>.
 * <p>
 * An {@code IMXMLFactoryNode} has exactly one child, which is always an
 * {@code IMXMLClassNode} specifying the <code>generator</code> class for the
 * <code>ClassFactory</code> (i.e., the class from which the factory creates
 * instances). Gordon Smith
 */
class MXMLFactoryNode extends MXMLInstanceNode implements IMXMLFactoryNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLFactoryNode(NodeBase parent)
    {
        super(parent);
    }

    private MXMLClassNode classNode;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLFactoryID;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? classNode : null;
    }

    @Override
    public int getChildCount()
    {
        return classNode != null ? 1 : 0;
    }

    @Override
    protected void initializeFromTag(MXMLTreeBuilder builder, IMXMLTagData tag)
    {
        initialize(builder);

        super.initializeFromTag(builder, tag);
    }

    protected void initializeFromFragments(MXMLTreeBuilder builder,
                                           ISourceLocation location,
                                           ISourceFragment[] fragments)
    {
        initialize(builder);

        setLocation(location);

        // The source fragments are a class name (possibly a complex one
        // like Vector.<Vector.<int>>. This is the class from which
        // we're making a ClassFactory. Create a child MXMLClassNode from it.
        classNode = new MXMLClassNode(this);
        classNode.initializeFromFragments(builder, location, fragments);
    }

    private void initialize(MXMLTreeBuilder builder)
    {
        // This node represents an instance of mx.core.ClassFactory.
        RoyaleProject project = builder.getProject();
        String qname = project.getClassFactoryClass();
        setClassReference(project, qname);

        // Add an expression dependency on that class.
        builder.addExpressionDependency(qname);
    }

    @Override
    public IMXMLClassNode getClassNode()
    {
        return classNode;
    }
}
