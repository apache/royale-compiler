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
import org.apache.royale.compiler.internal.parsing.TokenBase;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.QualifiedNameExpressionNode;
import org.apache.royale.compiler.tree.metadata.IInspectableTagNode;
import org.apache.royale.utils.CheapArray;

/**
 * Implementation of {@link IInspectableTagNode}.
 */
public class InspectableTagNode extends MetaTagNode implements IInspectableTagNode
{
    private static final String[] EMPTY_ARRAY = new String[0];

    /**
     * Constructor.
     */
    public InspectableTagNode()
    {
        super(IMetaAttributeConstants.ATTRIBUTE_INSPECTABLE);
        values = CheapArray.create(5);
    }

    private IdentifierNode variable;

    private IdentifierNode arrayType;

    private Object values;
    
    private IdentifierNode type;

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addChildInOrder(variable, fillInOffsets);
        addChildInOrder(arrayType, fillInOffsets);
        addChildInOrder(type, fillInOffsets);
    }

    @Override
    protected int getInitialChildCount()
    {
        return 0;
    }

    @Override
    public void normalize(boolean fillInOffsets)
    {
        super.normalize(fillInOffsets);
        values = CheapArray.optimize(values, EMPTY_ARRAY);
    }

    @Override
    public String[] getAllowedValues()
    {
        return (String[])CheapArray.toArray(values, EMPTY_ARRAY);
    }

    public void addAllowedValue(String value)
    {
        CheapArray.add(value, values);
        String string = getValue(IMetaAttributeConstants.NAME_INSPECTABLE_ENUMERATION);
        if (string.length() > 0)
            string += ",";
        string += value;
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_ENUMERATION, value);
    }

    public void parseValues(String valueString)
    {
        if (valueString != null)
        {
            String[] strings = valueString.split(",");
            for (int i = 0; i < strings.length; i++)
            {
                CheapArray.add(strings[i].trim(), values);
            }
        }
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_ENUMERATION, valueString);
    }

    public void setCategory(String category)
    {
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_CATEGORY, category);
    }

    public String getCategory()
    {
        return getValue(IMetaAttributeConstants.NAME_INSPECTABLE_CATEGORY);
    }

    public String getDefaultValue()
    {
        return getValue(IMetaAttributeConstants.NAME_INSPECTABLE_DEFAULT_VALUE);
    }

    public void setDefaultValue(String value)
    {
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_DEFAULT_VALUE, value);
    }

    public void setEnvironment(String environment)
    {
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_ENVIRONMENT, environment);
    }

    @Override
    public String getEnvironment()
    {
        return getValue(IMetaAttributeConstants.NAME_INSPECTABLE_ENVIRONMENT);
    }

    @Override
    public String getFormat()
    {
        return getValue(IMetaAttributeConstants.NAME_INSPECTABLE_FORMAT);
    }

    public void setFormat(String format)
    {
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_FORMAT, format);
    }

    @Override
    public String getName()
    {
        return getValue(IMetaAttributeConstants.NAME_INSPECTABLE_NAME);
    }

    public void setName(String name)
    {
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_NAME, name);
    }

    private String getTypeName()
    {
        return type != null ? type.getName() : "";
    }

    public void setType(Token type)
    {
        this.type = new QualifiedNameExpressionNode((TokenBase)type);
        this.type.setParent(this);
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_TYPE, getTypeName());
    }

    @Override
    public String getVariable()
    {
        return variable != null ? variable.getName() : "";
    }

    public void setVariable(IdentifierNode variable)
    {
        this.variable = variable;
        this.variable.setParent(this);
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_VARIABLE, getVariable());
    }

    public void setVerbose(String verbose)
    {
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_VERBOSE, verbose);
    }

    @Override
    public String getVerbose()
    {
        return getValue(IMetaAttributeConstants.NAME_INSPECTABLE_VERBOSE);
    }

    public void setArrayType(IdentifierNode arrayType)
    {
        this.arrayType = arrayType;
        this.arrayType.setParent(this);
        addToMap(IMetaAttributeConstants.NAME_INSPECTABLE_ARRAYTYPE, getArrayType());
    }

    @Override
    public String getArrayType()
    {
        return arrayType != null ? arrayType.getName() : "";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof InspectableTagNode)
        {
            if (!equals(((InspectableTagNode)obj).arrayType, arrayType))
                return false;
            if (!equals(((InspectableTagNode)obj).type, type))
                return false;
            if (!equals(((InspectableTagNode)obj).variable, variable))
                return false;
        }
        else
        {
            return false;
        }
        return super.equals(obj);
    }
}
