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

package org.apache.royale.compiler.internal.tree.as.parts;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.internal.tree.as.ModifiersContainerNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;

public class SparseDecorationPart implements IDecorationPart
{
    public static final SparseDecorationPart EMPTY_DECORATION_PART = new SparseDecorationPart();

    private SparseDecorationPart()
    {
    }

    @Override
    public void setASDocComment(IASDocComment ref)
    {
    }

    @Override
    public void setMetadata(MetaTagsNode meta)
    {
    }

    @Override
    public void setModifiers(ModifiersContainerNode set)
    {
    }

    @Override
    public void setNamespace(INamespaceDecorationNode ns)
    {
    }

    @Override
    public void compact()
    {
    }

    @Override
    public IASDocComment getASDocComment()
    {
        return null;
    }

    @Override
    public MetaTagsNode getMetadata()
    {
        return null;
    }

    @Override
    public ModifiersContainerNode getModifiers()
    {
        return null;
    }

    @Override
    public INamespaceDecorationNode getNamespace()
    {
        return null;
    }
}
