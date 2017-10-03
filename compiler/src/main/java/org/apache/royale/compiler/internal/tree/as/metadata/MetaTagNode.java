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

package org.apache.royale.compiler.internal.tree.as.metadata;

import java.util.List;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.TreeNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.utils.CheapArray;

/**
 * MetaAttributeNode is a generic node holding info for one metadata attribute.
 * The name of the attribute is the meta name ("Inspectable", "Bindable",
 * "Style", etc.) (not the name of any "name" attribute within the tag)
 */
public abstract class MetaTagNode extends TreeNode implements IMetaTagNode
{
    private static final IMetaTagAttribute[] EMPTY_ARRAY = new IMetaTagAttribute[0];
    static final String NAME = "name";
    static final String VALUE = "value";
    static final String ATTR_PAIR = "AttrPair";

    private static MetaTagValue[] emptyAttrArray = new MetaTagValue[0];

    /**
     * Meta attribute comparator used to assist MetaAttributesNode.equals. Only
     * used in freeze/thaw unit tests.
     */
    public static class MetaComparator implements java.util.Comparator<Object>
    {
        @Override
        public int compare(Object o1, Object o2)
        {
            if (o1 instanceof MetaTagNode && o2 instanceof MetaTagNode)
            {
                return ((MetaTagNode)o1).toString().compareTo(((MetaTagNode)o2).toString());
            }
            else if (o1 instanceof MetaTagValue && o2 instanceof MetaTagValue)
            {
                return ((MetaTagValue)o1).toString().compareTo(((MetaTagValue)o2).toString());
            }
            return 0;
        }
    }

    protected String tagName;
    private Object metaAttrs;

    /**
     * Constructor
     * 
     * @param name name of attribute (e.g. Event or IconFile)
     */
    public MetaTagNode(String name)
    {
        if (name != null)
            tagName = name;
        
        metaAttrs = CheapArray.create(4);
    }

    /**
     * Copy constructor
     * 
     * @param other attribute to copy
     */
    public MetaTagNode(MetaTagNode other)
    {
        this.metaAttrs = other.metaAttrs;
        this.tagName = other.tagName;
    }

    public void addToMap(String name, String value)
    {
        if (!(metaAttrs instanceof List))
            metaAttrs = CheapArray.create(2);
        if (name == null || name.equals(IMetaTagNode.SINGLE_VALUE))
            CheapArray.add(new MetaTagValue(value), metaAttrs);
        else
            CheapArray.add(new MetaTagValue(name, value), metaAttrs);

    }

    @Override
    protected void optimizeChildren(Object newChildren)
    {
        metaAttrs = CheapArray.optimize(metaAttrs, emptyAttrArray);
        super.optimizeChildren(newChildren);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 0;
    }

    @Override
    public IDefinitionNode getDecoratedDefinitionNode()
    {
        IASNode parent = getParent();
        if (parent instanceof MetaTagsNode)
        {
            return ((MetaTagsNode)parent).getDecoratedDefinition();
        }
        return null;
    }

    public String getValue(String key)
    {
        IMetaTagAttribute attribute = getAttribute(key);
        if (attribute != null)
            return attribute.getValue();
        return "";
    }

    @Override
    public IMetaTagAttribute getAttribute(String key)
    {
        int size = CheapArray.size(metaAttrs);
        for (int i = 0; i < size; i++)
        {
            Object object = CheapArray.get(i, metaAttrs);
            if (object instanceof IMetaTagAttribute)
            {
                if (key == SINGLE_VALUE && !((IMetaTagAttribute)object).hasKey())
                {
                    //just return the first one we find
                    return ((IMetaTagAttribute)object);
                }
                
                String tagKey = ((IMetaTagAttribute)object).getKey();
                if (tagKey!= null && tagKey.compareTo(key)==0)
                {
                    return ((IMetaTagAttribute)object);
                }
            }
        }
        return null;
    }

    @Override
    public String getAttributeValue(String key)
    {
        IMetaTagAttribute attribute = getAttribute(key);
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public IMetaTagAttribute[] getAllAttributes()
    {
        return (IMetaTagAttribute[])CheapArray.toArray(metaAttrs, EMPTY_ARRAY);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof MetaTagNode)
        {
            if (getAbsoluteStart() != ((MetaTagNode)obj).getAbsoluteStart())
                return false;

            if (getAbsoluteEnd() != ((MetaTagNode)obj).getAbsoluteEnd())
                return false;
            return obj.toString().compareTo(this.toString()) == 0;
        }
        return super.equals(obj);
    }

    /**
     * For debugging only. Builds a string such as
     * <code>Event(name="click", type="flash.events.MouseEvent")</code> from the
     * metadata.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('[');
        sb.append(getTagName());
        sb.append('(');

        int size = CheapArray.size(metaAttrs);
        for (int i = 0; i < size; i++)
        {
            String key = ((IMetaTagAttribute)CheapArray.get(i, metaAttrs)).getKey();
            String value = ((IMetaTagAttribute)CheapArray.get(i, metaAttrs)).getValue();

            if (key == null)
            {
                sb.append('"');
                sb.append(value);
                sb.append('"');
            }
            else
            {
                sb.append(key);
                sb.append('=');
                sb.append('"');
                sb.append(value);
                sb.append('"');
                if (i + 1 < size)
                {
                    sb.append(',');
                    sb.append(' ');
                }
            }
        }

        sb.append(')');
        sb.append(']');

        return true;
    }

    @Override
    public String getTagName()
    {
        return tagName;
    }

    protected boolean equals(IdentifierNode left, IdentifierNode right)
    {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;
        if (right == null)
            return false;

        if (left.getName().compareTo(right.getName()) != 0)
            return false;

        if (left.getAbsoluteStart() != right.getAbsoluteStart())
            return false;

        if (left.getAbsoluteEnd() != right.getAbsoluteEnd())
            return false;

        return true;
    }

    public String getDefaultValue(String name, String defaultValue)
    {
        return getValue(defaultValue);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MetaTagID;
    }

    public IMetaTag buildMetaTag(IFileSpecification containingFileSpec, IDefinition definition)
    {
        String name = getTagName();
        MetaTag metaTag = new MetaTag(definition, name, getAllAttributes());
        metaTag.setLocation(containingFileSpec, getAbsoluteStart(), getAbsoluteEnd(), getLine(), getColumn());
        return metaTag;
    }
}
