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

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyContainerNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Implementation of the {@link IMXMLModelPropertyContainerNode} interface.
 */
abstract class MXMLModelPropertyContainerNodeBase extends MXMLNodeBase implements IMXMLModelPropertyContainerNode
{
    private static final IMXMLModelPropertyNode[] NO_PROPERTY_NODES = new IMXMLModelPropertyNode[0];

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLModelPropertyContainerNodeBase(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The short name of the tag that created this node.
     */
    private String name;

    /**
     * The children of this node.
     */
    private IMXMLModelPropertyNode[] propertyNodes = NO_PROPERTY_NODES;

    /**
     * The names of the properties to be set on this node. If this node has more
     * than one child {@link IMXMLModelPropertyNode} with the same name, the
     * property name appears only once in this list.
     */
    private List<String> propertyNameList;

    /**
     * A map mapping a property name to the child property nodes with that name.
     */
    private ListMultimap<String, IMXMLModelPropertyNode> propertyMultimap;

    /**
     * If this node has the same name as a sibling, then this field stores that
     * it is the ith one, in attribute-then-child-tag order. If it is the only
     * one, its index is NO_INDEX.
     */
    private int index = NO_INDEX;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getChildCount()
    {
        return propertyNodes.length;
    }

    @Override
    public IASNode getChild(int i)
    {
        return propertyNodes[i];
    }

    @Override
    public IMXMLModelPropertyNode[] getPropertyNodes()
    {
        return propertyNodes;
    }

    @Override
    public String[] getPropertyNames()
    {
        return propertyNameList != null ?
                propertyNameList.toArray(new String[0]) :
                new String[0];
    }

    @Override
    public IMXMLModelPropertyNode[] getPropertyNodes(String propertyName)
    {
        return propertyMultimap != null ?
                propertyMultimap.get(propertyName).toArray(new IMXMLModelPropertyNode[0]) :
                new IMXMLModelPropertyNode[0];
    }

    @Override
    public int getIndex()
    {
        return index;
    }

    void setIndex(int index)
    {
        this.index = index;
    }

    @Override
    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return new MXMLNodeInfo(builder);
    }

    protected void initializeFromAttribute(MXMLTreeBuilder builder,
                                           IMXMLTagAttributeData attribute)
    {
        /*
         * For Model, namespaces are just ignored, so we can just use the short
         * name, rather than turning this into a qname. AFAIK, this is special
         * case for Model tag
         */
        name = attribute.getShortName();

        setLocation(attribute);
    }

    @Override
    protected void initializeFromTag(MXMLTreeBuilder builder, IMXMLTagData tag)
    {
        name = tag.getShortName(); // see note in initializeFromAttribute about namespaces.
        super.initializeFromTag(builder, tag);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.getPrefix() != null)
        {
            // TODO Report a problem because a prefix means nothing.
        }

        MXMLModelPropertyNode propertyNode = new MXMLModelPropertyNode(this);
        propertyNode.initializeFromAttribute(builder, attribute);
        info.addChildNode(propertyNode);
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag,
                                   MXMLNodeInfo info)
    {
        if (childTag.getPrefix() != null)
        {
            // TODO Report a problem because a prefix means nothing.
        }

        MXMLModelPropertyNode propertyNode = new MXMLModelPropertyNode(this);
        propertyNode.initializeFromTag(builder, childTag);
        info.addChildNode(propertyNode);
    }

    void setPropertyNodes(IMXMLModelPropertyNode[] propertyNodes)
    {
        this.propertyNodes = propertyNodes;

        for (IMXMLModelPropertyNode propertyNode : propertyNodes)
        {
            String propertyName = propertyNode.getName();

            if (propertyMultimap == null)
                propertyMultimap = ArrayListMultimap.create();
            if (propertyNameList == null)
                propertyNameList = new ArrayList<String>();

            int n = propertyMultimap.get(propertyName).size();
            if (n == 0)
                propertyNameList.add(propertyName);
            propertyMultimap.put(propertyName, propertyNode);
        }

        if (propertyNameList != null)
        {
            for (String propertyName : propertyNameList)
            {
                List<IMXMLModelPropertyNode> list = propertyMultimap.get(propertyName);
                if (list.size() > 1)
                {
                    int i = 0;
                    for (IMXMLModelPropertyNode node : list)
                    {
                        ((MXMLModelPropertyNode)node).setIndex(i++);
                    }
                }
            }
        }
    }

    /**
     * For debugging only. Builds a string such as
     * <code>"spark.components.Button" id="b1"</code> from the qualified name of
     * the class reference for the node and its id, if present.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        int index = getIndex();
        if (index != NO_INDEX)
        {
            sb.append(' ');
            sb.append('[');
            sb.append(index);
            sb.append(']');
        }

        return true;
    }
}
