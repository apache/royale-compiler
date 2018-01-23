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
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.mxml.IMXMLCompilerDirectiveNodeBase;

/**
 * This abstract class should be extended by any node which is constructed from
 * a compiler directive function, such as <code>@Embed(...)</code>,
 * <code>@Resource(...)</code>, and <code>@Clear()</code>.
 */
abstract class MXMLCompilerDirectiveNodeBase extends MXMLInstanceNode implements IMXMLCompilerDirectiveNodeBase
{
    /**
     * Constructor.
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLCompilerDirectiveNodeBase(NodeBase parent)
    {
        super(parent);
    }

    protected IMetaTagAttribute[] attributes;

    @Override
    public IMetaTagAttribute[] getAttributes()
    {
        return attributes;
    }

    public abstract void initializeFromText(MXMLTreeBuilder builder, String text, ISourceLocation location);

    protected void parseTextAndSetAttributes(MXMLTreeBuilder builder, String text, ISourceLocation location, String attributeName)
    {
        MetaTagsNode metaTags = ASParser.parseAtFunction(builder.getWorkspace(), text,
                location.getSourcePath(), location.getAbsoluteStart(), location.getLine(), location.getColumn(), builder.getProblems());
        if (metaTags == null)
            return;

        IMetaTagNode embedMetaData = metaTags.getTagByName(attributeName);
        assert (embedMetaData != null) : "no meta data, but MXMLCompilerDirectiveNodeBase constructed";
        attributes = embedMetaData.getAllAttributes();
    }
}
