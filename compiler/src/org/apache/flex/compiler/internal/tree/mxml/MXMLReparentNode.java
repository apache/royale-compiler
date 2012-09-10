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

package org.apache.flex.compiler.internal.tree.mxml;

import org.apache.flex.compiler.internal.mxml.MXMLDialect;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.mxml.IMXMLLanguageConstants;
import org.apache.flex.compiler.mxml.MXMLTagAttributeData;
import org.apache.flex.compiler.mxml.MXMLTagData;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.MXMLEmptyAttributeProblem;
import org.apache.flex.compiler.problems.MXMLIncludeInAndExcludeFromProblem;
import org.apache.flex.compiler.problems.MXMLRequiredAttributeProblem;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLReparentNode;

import static org.apache.flex.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Implementation of the {@link IMXMLReparentNode} interface.
 */
class MXMLReparentNode extends MXMLNodeBase implements IMXMLReparentNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLReparentNode(NodeBase parent)
    {
        super(parent);
    }

    private String target;

    private String[] includeIn;

    private String[] excludeFrom;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLReparentID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.REPARENT;
    }

    @Override
    public String[] getIncludeIn()
    {
        return includeIn;
    }

    @Override
    public String[] getExcludeFrom()
    {
        return excludeFrom;
    }

    @Override
    public String getTarget()
    {
        return target;
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, MXMLTagData tag,
                                               MXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_TARGET))
            target = processTargetAttribute(builder, attribute);

        else if (attribute.isSpecialAttribute(ATTRIBUTE_INCLUDE_IN))
            includeIn = processIncludeInOrExcludeFromAttribute(builder, attribute);

        else if (attribute.isSpecialAttribute(ATTRIBUTE_EXCLUDE_FROM))
            excludeFrom = processIncludeInOrExcludeFromAttribute(builder, attribute);

        else
            super.processTagSpecificAttribute(builder, tag, attribute, info);
    }

    private String processTargetAttribute(MXMLTreeBuilder builder,
                                          MXMLTagAttributeData attribute)
    {
        MXMLDialect mxmlDialect = builder.getMXMLDialect();

        // Falcon trims this attribute even though the old compiler didn't.
        String value = attribute.getRawValue();
        value = mxmlDialect.trim(value);

        return value;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder,
                                          MXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        if (target == null)
        {
            ICompilerProblem problem = new MXMLRequiredAttributeProblem(tag, ATTRIBUTE_TARGET);
            builder.addProblem(problem);
        }
        else if (target.isEmpty())
        {
            ICompilerProblem problem = new MXMLEmptyAttributeProblem(tag.getTagAttributeData(ATTRIBUTE_TARGET));
            builder.addProblem(problem);
        }

        // If both includeIn and excludeFrom are specified, set both to null.
        if (includeIn != null && excludeFrom != null)
        {
            ICompilerProblem problem = new MXMLIncludeInAndExcludeFromProblem(tag);
            builder.addProblem(problem);
            includeIn = null;
            excludeFrom = null;
        }

        super.initializationComplete(builder, tag, info);
    }
}
