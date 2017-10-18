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

import java.util.Set;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IStyleDefinition;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.MXMLInvalidStyleProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLClassReferenceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * {@code MXMLStyleSpecifierNode} represents an MXML style attribute or style
 * child tag.
 * <p>
 * It has a single child, which is an {@code MXMLInstanceNode} specifying the
 * style value.
 */
class MXMLStyleSpecifierNode extends MXMLPropertySpecifierNode implements IMXMLStyleSpecifierNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLStyleSpecifierNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLStyleSpecifierID;
    }

    @Override
    protected void initializeFromAttribute(MXMLTreeBuilder builder, IMXMLTagAttributeData attribute, MXMLNodeInfo info)
    {
        super.initializeFromAttribute(builder, attribute, info);
        validateStyle(builder, attribute);
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag, MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);
        validateStyle(builder, tag);
    }

    /**
     * Validate style.
     * 
     * @see MXMLInvalidStyleProblem
     */
    private void validateStyle(MXMLTreeBuilder builder, ISourceLocation source)
    {
        final IDefinition definition = getDefinition();
        if (definition instanceof IStyleDefinition)
        {
            final IStyleDefinition styleTag = (IStyleDefinition)definition;
            final Set<String> applicableThemes = ImmutableSet.copyOf(styleTag.getThemes());
            if (!applicableThemes.isEmpty())
            {
                final Set<String> themeNames = ImmutableSet.copyOf(builder.getProject().getThemeNames());
                final boolean isStyleValid = !Sets.intersection(themeNames, applicableThemes).isEmpty();
                if (!isStyleValid)
                {
                    final String componentType;
                    if (getParent() instanceof IMXMLClassReferenceNode)
                        componentType = ((IMXMLClassReferenceNode)getParent()).getClassReference(builder.getProject()).getBaseName();
                    else
                        componentType = "";
                    final MXMLInvalidStyleProblem problem = new MXMLInvalidStyleProblem(
                            source,
                            this.getName(),
                            componentType,
                            styleTag.getThemes());
                    builder.addProblem(problem);
                }
            }
        }
    }
}
