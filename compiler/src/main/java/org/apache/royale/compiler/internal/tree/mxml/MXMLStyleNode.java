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

import org.antlr.runtime.ANTLRStringStream;

import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.internal.caches.CSSDocumentCache;
import org.apache.royale.compiler.internal.css.CSSDocument;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDualContentProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

class MXMLStyleNode extends MXMLNodeBase implements IMXMLStyleNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLStyleNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * This field caches parsed CSS model in this node for
     * {@link #getCSSDocument(Collection)}.
     */
    private ICSSDocument cssDocument;

    /**
     * All CSS text in this node will be aggregated here. The CSS text can
     * either be text of {@code <fx:Style>} node or included by
     * {@code <fx:Style src="styles.css" />}.
     */
    private String cssText;

    private int cssContentStart = -1;
    private int cssCharPos = -1;
    
    /**
     * path of included css.
     * {@code <fx:Style src="styles.css" />}.
     */
    private String cssSourcePath = null;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLStyleID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.STYLE;
    }

    @Override
    public int getContentStart()
    {
        return cssContentStart;
    }

    @Override
    public ICSSDocument getCSSDocument(final Collection<ICompilerProblem> problems)
    {
        if (cssDocument == null)
        {
            if (cssText != null && !cssText.isEmpty() && !cssText.trim().isEmpty())
            {
                boolean strictFlexCSS = false;
                IMXMLFileNode fileNode = getFileNode();
                if (fileNode != null)
                {
                    ICompilerProject project = fileNode.getCompilerProject();
                    if (project != null)
                    {
                        strictFlexCSS = project.getStrictFlexCSS();
                    }
                }
                ANTLRStringStream stream = new ANTLRStringStream(cssText);
                stream.name = cssSourcePath != null ? cssSourcePath : getSourcePath();
                stream.setLine(getLine());
                stream.setCharPositionInLine(cssCharPos);
                cssDocument = CSSDocument.parse(stream, strictFlexCSS, problems);
                if (cssDocument == null)
                {
                    cssDocument = CSSDocumentCache.EMPTY_CSS_DOCUMENT;
                }
            }
            else
            {
                cssDocument = CSSDocumentCache.EMPTY_CSS_DOCUMENT;
            }
                          
        }

        return cssDocument;
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
            // Resolve the attribute value to a normalized path.
            // Doing so makes this compilation unit dependent on that file.
            final String sourcePath = resolveSourceAttributePath(builder, attribute, info);
            if (sourcePath != null)
            {
                cssText = builder.readExternalFile(attribute, sourcePath);
                cssCharPos = -1;
                cssContentStart = -1;
                cssSourcePath = sourcePath;
            }
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, IMXMLTagData tag,
                                                 IMXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        info.hasDualContent = true;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        if (info.hasSourceAttribute && info.hasDualContent)
        {
            ICompilerProblem problem = new MXMLDualContentProblem(tag, tag.getShortName());
            builder.addProblem(problem);
            return;
        }

        // If <fx:Style> tag has a valid 'source' attribute, the text of the node is ignored.
        if (cssText == null)
        {
            cssText = tag.getCompilableText();
            cssCharPos = tag.getEndColumn() + 1;
            cssContentStart = tag.getEnd();
        }

        // Register this "style" node with the root "MXMLFileNode".
        builder.getFileNode().getStyleNodes().add(this);
    }
}
