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
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDualContentProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

class MXMLStringNode extends MXMLExpressionNodeBase implements IMXMLStringNode
{
    private static final String DEFAULT = null;

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLStringNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLStringID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.String;
    }

    @Override
    public String getValue()
    {
        assert getExpressionNode() instanceof MXMLLiteralNode : "getValue() shouldn't be getting called on a non-literal MXMLStringNode";

        MXMLLiteralNode literalNode = (MXMLLiteralNode)getExpressionNode();
        return (String)literalNode.getValue();
    }

    @Override
    public ExpressionType getExpressionType()
    {
        return ExpressionType.STRING;
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
            String sourcePath = resolveSourceAttributePath(builder, attribute, info);
            if (sourcePath != null)
            {
                String text = builder.readExternalFile(attribute, sourcePath);
                if (text != null)
                {
                    ISourceLocation location = attribute.getValueLocation();
                    SourceFragment fragment = new SourceFragment(text, text,
                            location.getStart(), location.getLine(), location.getColumn());

                    info.addSourceFragments(sourcePath, new SourceFragment[] {fragment});
                }
            }
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
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

        NodeBase expressionNode = createExpressionNodeFromFragments(builder, tag, info, DEFAULT);
        setExpressionNode(expressionNode);

        super.initializationComplete(builder, tag, info);
    }
}
