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

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.mxml.MXMLDialect.TextParsingFlags;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;

/**
 * Implementation of the {@link IMXMLModelPropertyNode} interface.
 */
class MXMLModelPropertyNode extends MXMLModelPropertyContainerNodeBase implements IMXMLModelPropertyNode
{
    private static final EnumSet<TextParsingFlags> FLAGS = EnumSet.of(
            TextParsingFlags.ALLOW_ARRAY,
            TextParsingFlags.ALLOW_BINDING,
            TextParsingFlags.ALLOW_COMPILER_DIRECTIVE,
            TextParsingFlags.ALLOW_ESCAPED_COMPILER_DIRECTIVE);

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLModelPropertyNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The instance node representing the value of the property.
     */
    private MXMLInstanceNode instanceNode = null;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLModelPropertyID;
    }

    @Override
    public int getChildCount()
    {
        return instanceNode != null ? 1 : super.getChildCount();
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 && instanceNode != null ? instanceNode : super.getChild(i);
    }

    @Override
    public boolean hasLeafValue()
    {
        return instanceNode != null;
    }

    @Override
    public IMXMLInstanceNode getInstanceNode()
    {
        return instanceNode;
    }

    @Override
    protected void initializeFromAttribute(MXMLTreeBuilder builder,
                                           IMXMLTagAttributeData attribute)
    {
        super.initializeFromAttribute(builder, attribute);

        Collection<ICompilerProblem> problems = builder.getProblems();
        ISourceFragment[] fragments = attribute.getValueFragments(problems);
        ISourceLocation location = attribute.getValueLocation();
        instanceNode = createInstanceNode(builder, fragments, location);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        MXMLModelPropertyNode propertyNode = new MXMLModelPropertyNode(this);
        propertyNode.initializeFromAttribute(builder, attribute);
        info.addChildNode(propertyNode);
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        Collection<ICompilerProblem> problems = builder.getProblems();
        ISourceFragment[] fragments = text.getFragments(problems);
        MXMLInstanceNode instanceNode = createInstanceNode(builder, fragments, text);
        info.addChildNode(instanceNode);
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder,
                                          IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        List<IMXMLNode> childNodeList = info.getChildNodeList();
        if (childNodeList.size() == 1 && childNodeList.get(0) instanceof IMXMLInstanceNode)
        {
            instanceNode = (MXMLInstanceNode)childNodeList.get(0);
        }
        else
        {
            setPropertyNodes(childNodeList.toArray(new IMXMLModelPropertyNode[0]));
        }

        super.initializationComplete(builder, tag, info);
    }

    private MXMLInstanceNode createInstanceNode(MXMLTreeBuilder builder,
                                                ISourceFragment[] fragments,
                                                ISourceLocation location)
    {
        ITypeDefinition anyType = ClassDefinition.getAnyTypeClassDefinition();
        MXMLClassDefinitionNode classNode =
                (MXMLClassDefinitionNode)getClassDefinitionNode();
        return builder.createInstanceNode(this, anyType, fragments, location, FLAGS, classNode);
    }
}
