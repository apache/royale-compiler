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

import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.internal.as.codegen.MXMLClassDirectiveProcessor;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLPrivateTagLocationProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import com.google.common.collect.ImmutableSet;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * {@code MXMLDocumentNode} represents the root MXML tag in an MXML document.
 */
public class MXMLDocumentNode extends MXMLClassDefinitionNode implements IMXMLDocumentNode
{
    // TODO Make this class package internal rather than public.

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    public MXMLDocumentNode(NodeBase parent)
    {
        super(parent);
    }

    public MXMLClassDirectiveProcessor cdp;
    
    /**
     * A map of special attribute values.
     */
    protected final Map<String, String> rootAttributes = new HashMap<String, String>();

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLDocumentID;
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        MXMLFileScope fileScope = builder.getFileScope();

        MXMLNodeBase childNode = null;

        if (fileScope.isLibraryTag(childTag))
        {
            childNode = new MXMLLibraryNode(this);
        }
        else if (fileScope.isModelTag(childTag))
        {
            childNode = new MXMLModelNode(this);
        }
        else if (fileScope.isPrivateTag(childTag))
        {
            // A <Private> tag must be the last child tag.
            if (childTag.getNextSibling(true) != null)
            {
                ICompilerProblem problem = new MXMLPrivateTagLocationProblem(childTag);
                builder.addProblem(problem);
            }
            else
            {
                childNode = new MXMLPrivateNode(this);
            }
        }
        else
        {
            super.processChildTag(builder, tag, childTag, info);
        }

        if (childNode != null)
        {
            childNode.initializeFromTag(builder, childTag);
            info.addChildNode(childNode);
        }
    }

    /**
     * A set of special attributes to be skipped for attribute processing. The
     * "implement" special attribute is not in the set, because we still want it
     * to be processed by the regular attribute processing logic.
     */
    private static ImmutableSet<String> SKIP_ATTRIBUTES = ImmutableSet.of(
            ATTRIBUTE_EXCLUDE_FROM,
            ATTRIBUTE_INCLUDE_IN,
            ATTRIBUTE_ITEM_CREATION_POLICY,
            ATTRIBUTE_FRAME_RATE,
            ATTRIBUTE_PAGE_TITLE,
            ATTRIBUTE_PRELOADER,
            ATTRIBUTE_RSL,
            ATTRIBUTE_RUNTIME_DPI_PROVIDER,
            ATTRIBUTE_SCRIPT_RECURSION_LIMIT,
            ATTRIBUTE_SCRIPT_TIME_LIMIT,
            ATTRIBUTE_SPLASH_SCREEN_IMAGE,
            ATTRIBUTE_THEME,
            ATTRIBUTE_USE_DIRECT_BLIT,
            ATTRIBUTE_USE_GPU,
            ATTRIBUTE_USE_PRELOADER,
            LIBRARY); // TODO Why is LIBRARY here?

    /**
     * Add all attributes to {@link #rootAttributes}. If the attribute is not a
     * known special attribute, delegate the process to the base class.
     * <p>
     * Note that this method might not be triggered if there's only namespace
     * attributes on the root tag.
     */
    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag, IMXMLTagAttributeData attribute, MXMLNodeInfo info)
    {
        final String name = attribute.getName();
        rootAttributes.put(name, attribute.getRawValue());
        if (!SKIP_ATTRIBUTES.contains(name))
            super.processTagSpecificAttribute(builder, tag, attribute, info);
    }
}
