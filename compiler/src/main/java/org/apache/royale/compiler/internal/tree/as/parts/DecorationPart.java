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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.internal.tree.as.ModifiersContainerNode;
import org.apache.royale.compiler.internal.tree.as.metadata.MetaTagsNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.utils.CheapArray;

public class DecorationPart implements IDecorationPart
{
    private static final Object[] EMPTY_ARRAY = new Object[0];
    
    private byte modId;
    private byte metaId;
    private byte namespaceId;
    private byte commentId;

    private Object store;

    public DecorationPart()
    {
        store = CheapArray.create(2);
        metaId = -1;
        modId = -1;
        namespaceId = -1;
        commentId = -1;
    }

    @Override
    public void compact()
    {
        if (store instanceof List)
            store = CheapArray.optimize(store, EMPTY_ARRAY);
    }

    protected byte insert(Object object)
    {
        if (object == null)
            return -1;
        byte id = (byte)CheapArray.size(store);
        if (!(store instanceof List))
        {
            ArrayList<Object> list = new ArrayList<Object>(((Object[])store).length);
            list.addAll(Arrays.asList((Object[])store));
            int size = list.size();
            for (int i = 0; i < size; i++)
            {
                Object entry = list.get(i);
                if (entry instanceof INamespaceDecorationNode)
                {
                    namespaceId = (byte)i;
                }
                else if (entry instanceof MetaTagsNode)
                {
                    metaId = (byte)i;
                }
                else if (entry instanceof IASDocComment)
                {
                    commentId = (byte)i;
                }
                else if (entry instanceof ModifiersContainerNode)
                {
                    modId = (byte)i;
                }
                else
                {
                    insertUnknownObject(entry, i);
                }
            }
            store = list;
        }
        CheapArray.add(object, store);
        return id;
    }

    protected Object getFromStore(byte index)
    {
        if (index >= 0)
        {
            return CheapArray.get(index, store);
        }
        return null;
    }

    protected void insertUnknownObject(Object entry, int offset)
    {
        //do nothing
    }

    @Override
    public void setASDocComment(IASDocComment ref)
    {
        commentId = insert(ref);
    }

    @Override
    public IASDocComment getASDocComment()
    {
        if (commentId >= 0)
        {
            Object object = CheapArray.get(commentId, store);
            if (object instanceof IASDocComment)
                return (IASDocComment)object;
        }
        return null;
    }

    @Override
    public void setMetadata(MetaTagsNode meta)
    {
        metaId = insert(meta);
    }

    @Override
    public MetaTagsNode getMetadata()
    {
        if (metaId >= 0)
        {
            Object object = CheapArray.get(metaId, store);
            if (object instanceof MetaTagsNode)
                return (MetaTagsNode)object;
        }
        return null;
    }

    @Override
    public void setModifiers(ModifiersContainerNode set)
    {
        modId = insert(set);
    }

    @Override
    public ModifiersContainerNode getModifiers()
    {
        if (modId >= 0)
        {
            Object object = CheapArray.get(modId, store);
            if (object instanceof ModifiersContainerNode)
                return (ModifiersContainerNode)object;
        }
        return null;
    }

    @Override
    public void setNamespace(INamespaceDecorationNode ns)
    {
        namespaceId = insert(ns);
    }

    @Override
    public INamespaceDecorationNode getNamespace()
    {
        if (namespaceId >= 0)
        {
            Object object = CheapArray.get(namespaceId, store);
            if (object instanceof INamespaceDecorationNode)
                return (INamespaceDecorationNode)object;
        }
        return null;
    }
}
