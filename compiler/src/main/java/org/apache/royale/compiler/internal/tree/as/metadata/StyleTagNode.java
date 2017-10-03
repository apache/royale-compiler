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

import antlr.Token;

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.QualifiedNameExpressionNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.metadata.IStyleTagNode;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.utils.CheapArray;

public final class StyleTagNode extends BaseDefinitionMetaTagNode implements IStyleTagNode
{
    private IdentifierNode arrayType;
    private IdentifierNode type;
    private Object values;
    private Object states;

    private static final String[] EMPTY_LIST = new String[0];

    public StyleTagNode()
    {
        super(IMetaAttributeConstants.ATTRIBUTE_STYLE);
        init();
    }

    private void init()
    {
        values = CheapArray.create(3);
        states = CheapArray.create(3);
    }

    @Override
    public void normalize(boolean fillInOffsets)
    {
        super.normalize(fillInOffsets);
        values = CheapArray.optimize(values, EMPTY_LIST);
        states = CheapArray.optimize(states, EMPTY_LIST);
    }

    public void setArrayType(IdentifierNode arrayType)
    {
        this.arrayType = arrayType;
        this.arrayType.setParent(this);
        addToMap(IMetaAttributeConstants.NAME_STYLE_ARRAYTYPE, getArrayTypeName());
    }

    public void setType(Token type)
    {
        this.type = new QualifiedNameExpressionNode((TokenBase)type);
        this.type.setParent(this);
        addToMap(IMetaAttributeConstants.NAME_STYLE_TYPE, getTypeName());
    }

    public void setIsInheritable(String doesInherit)
    {
        addToMap(IMetaAttributeConstants.NAME_STYLE_INHERIT, doesInherit);
    }

    public void setFormat(String format)
    {
        addToMap(IMetaAttributeConstants.NAME_STYLE_FORMAT, format);
    }

    public void addValue(String value)
    {
        addToList(IMetaAttributeConstants.NAME_STYLE_ENUMERATION, value, values);
    }

    public void addState(String state)
    {
        addToList(IMetaAttributeConstants.NAME_STYLE_STATES, state, states);
    }

    private void addToList(String key, String value, Object array)
    {
        CheapArray.add(value, array);
        String string = getValue(key);
        if (string.length() > 0)
            string += ",";
        string += value;
        addToMap(key, value);
    }

    public void parseValues(String valueString)
    {
        parseList(IMetaAttributeConstants.NAME_STYLE_ENUMERATION, valueString, values);
    }

    public void parseStates(String statesString)
    {
        parseList(IMetaAttributeConstants.NAME_STYLE_STATES, statesString, states);
    }

    private void parseList(String key, String value, Object list)
    {
        if (value != null)
        {
            String[] strings = value.split(",");
            for (int i = 0; i < strings.length; i++)
            {
                CheapArray.add(strings[i].trim(), list);
            }
        }
        addToMap(key, value);
    }

    private String getArrayTypeName()
    {
        if (arrayType != null)
            return arrayType.getName();
        return "";
    }

    public IdentifierNode getArrayTypeNode()
    {
        return arrayType;
    }

    @Override
    public String getFormat()
    {
        return getValue(IMetaAttributeConstants.NAME_STYLE_FORMAT);
    }

    @Override
    public String getTypeName()
    {
        if (type != null)
            return type.getName();
        return "";
    }

    @Override
    public boolean hasThemes()
    {
        return !(getValue(IMetaAttributeConstants.NAME_STYLE_THEME).isEmpty());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof StyleTagNode)
        {
            StyleTagNode other = (StyleTagNode)obj;
            
            if (!equals(nameNode, other.nameNode))
                return false;

            if (!equals(type, other.type))
                return false;

            if (!equals(arrayType, other.arrayType))
                return false;
            
            if (other.getAbsoluteStart() != this.getAbsoluteStart())
                return false;
            
            if (other.getAbsoluteEnd() != this.getAbsoluteEnd())
                return false;

            if (getFormat().compareTo(other.getFormat()) != 0)
                return false;

            if (other.getValue(IMetaAttributeConstants.NAME_STYLE_ENUMERATION).compareTo(getValue(IMetaAttributeConstants.NAME_STYLE_ENUMERATION)) != 0)
                return false;

            if (other.getValue(IMetaAttributeConstants.NAME_STYLE_STATES).compareTo(getValue(IMetaAttributeConstants.NAME_STYLE_STATES)) != 0)
                return false;

            return true;

        }
        return false;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(nameNode, fillInOffsets);
        addChildInOrder(arrayType, fillInOffsets);
        addChildInOrder(type, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 0;
    }

    @Override
    public IDefinition getDefinition()
    {
        ICommonClassNode decoratedClassNode = (ICommonClassNode)getAncestorOfType(ICommonClassNode.class);
        if (decoratedClassNode == null)
            return null;
        IWorkspace workspace = getWorkspace();
        IClassDefinition decoratedClassDefinition = decoratedClassNode.getDefinition();
        assert decoratedClassDefinition != null;
        return decoratedClassDefinition.getStyleDefinition(workspace, getName());
    }
}
