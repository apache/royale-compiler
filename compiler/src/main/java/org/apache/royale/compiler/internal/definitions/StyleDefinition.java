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

package org.apache.royale.compiler.internal.definitions;

import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IStyleDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.metadata.IStyleTagNode;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Instances of this class represent definitions of MXML styles in the symbol
 * table.
 * <p>
 * MXML styles are defined by <code>[Style]</code> metadata on ActionScript
 * class declarations.
 * <p>
 * After a style definition is in the symbol table, it should always be accessed
 * through the read-only <code>IStyleDefinition</code> interface.
 */
public class StyleDefinition extends MetadataDefinitionBase implements IStyleDefinition
{
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public StyleDefinition(String name, IClassDefinition decoratedDefinition)
    {
        super(name, IMetaAttributeConstants.ATTRIBUTE_STYLE, decoratedDefinition);
    }

    private String arrayType;
    private String[] enumeration = EMPTY_STRING_ARRAY;
    private String format;
    private String inherit;
    private String[] states = EMPTY_STRING_ARRAY;
    private String[] themes = EMPTY_STRING_ARRAY;
    private String minValue;
    private String minValueExclusive;
    private String maxValue;
    private String maxValueExclusive;

    @Override
    public IStyleTagNode getNode()
    {
        return (IStyleTagNode)super.getNode();
    }

    @Override
    public String getArrayType()
    {
        return arrayType;
    }

    public void setArrayType(String arrayType)
    {
        this.arrayType = arrayType;
    }

    @Override
    public ITypeDefinition resolveArrayType(ICompilerProject project)
    {
        assert false;// Hitting this assertion means you probably need to implement more style metadata
        return null;
    }

    @Override
    public String[] getEnumeration()
    {
        return enumeration;
    }

    /**
     * Sets the value of the <code>enumeration</code> attribute.
     * 
     * @param enumeration The enumerated values as an array of Strings.
     */
    public void setEnumeration(String[] enumeration)
    {
        assert enumeration != null;
        this.enumeration = enumeration;
    }

    @Override
    public String getFormat()
    {
        return format;
    }

    /**
     * Sets the value of the <code>format</code> attribute.
     * 
     * @param format The value as a String.
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    @Override
    public boolean isColor()
    {
        return format.equals(IMetaAttributeConstants.VALUE_STYLE_FORMAT_COLOR);
    }

    @Override
    public String getInherit()
    {
        return inherit;
    }

    /**
     * Sets the value of the <code>inherit</code> attribute.
     * 
     * @param inherit The value as a String.
     */
    public void setInherit(String inherit)
    {
        this.inherit = inherit;
    }

    @Override
    public boolean isInheriting()
    {
        return inherit != null && inherit.equals(IMetaAttributeConstants.VALUE_STYLE_INHERIT_YES);
    }

    @Override
    public String[] getStates()
    {
        return states;
    }

    /**
     * Sets the value of the <code>states</code> attribute.
     * 
     * @param states The state names as an array of Strings.
     */
    public void setStates(String[] states)
    {
        this.states = states;
    }

    @Override
    public String[] getThemes()
    {
        return themes;
    }

    /**
     * Sets a list of theme names separated by comma or spaces.
     * 
     * @param themes Comma-separated or space-separated theme names.
     */
    // TODO Should this do string-splitting? setEnumeration() and setStates() don't.
    public void setThemes(final String themes)
    {
        if (themes == null)
        {
            this.themes = new String[0];
        }
        else
        {
            final Iterable<String> split = Splitter
                    .onPattern("[,\\s]") // comma or space
                    .trimResults()
                    .omitEmptyStrings()
                    .split(themes);
            this.themes = Iterables.toArray(split, String.class);
        }
    }

    @Override
    public String getMinValue()
    {
        return minValue;
    }

    /**
     * Sets the value of the <code>minValue</code> attribute.
     * 
     * @param minValue The value as a String.
     */
    public void setMinValue(String minValue)
    {
        this.minValue = minValue;
    }

    @Override
    public String getMinValueExclusive()
    {
        return minValueExclusive;
    }

    /**
     * Sets the value of the <code>minValueExclusive</code> attribute.
     * 
     * @param minValueExclusive The value as a String.
     */
    public void setMinValueExclusive(String minValueExclusive)
    {
        this.minValueExclusive = minValueExclusive;
    }

    @Override
    public String getMaxValue()
    {
        return maxValue;
    }

    /**
     * Sets the value of the <code>maxValue</code> attribute.
     * 
     * @param maxValue The value as a String.
     */
    public void setMaxValue(String maxValue)
    {
        this.maxValue = maxValue;
    }

    @Override
    public String getMaxValueExclusive()
    {
        return maxValueExclusive;
    }

    /**
     * Sets the value of the <code>maxValueExclusive</code> attribute.
     * 
     * @param maxValueExclusive The value as a String.
     */
    public void setMaxValueExclusive(String maxValueExclusive)
    {
        this.maxValueExclusive = maxValueExclusive;
    }
}
