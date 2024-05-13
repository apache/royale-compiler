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

package org.apache.royale.compiler.internal.css;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.css.FontFaceSourceType;
import org.apache.royale.compiler.css.ICSSFontFace;
import org.apache.royale.compiler.css.ICSSProperty;
import org.apache.royale.compiler.css.ICSSPropertyValue;
import org.apache.royale.compiler.problems.CSSRequiredDescriptorProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

import com.google.common.collect.ImmutableList;

/**
 * Implementation for {@code @font-face} statement DOM.
 */
public class CSSFontFace extends CSSNodeBase implements ICSSFontFace
{

    /**
     * Construct a {@code CSSFontFace} from a list of properties. The parser
     * doesn't validate if the properties are acceptable by the
     * {@code @font-face} statement, so that we don't need to update the grammar
     * when new properties are added to {@code @font-face} statement.
     * 
     * @param properties key value pairs inside the {@code @font-face} block.
     */
    protected CSSFontFace(final List<CSSProperty> properties,
                          final CommonTree tree,
                          final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.FONT_FACE);

        assert properties != null : "Properties can't be null for @font-face.";

        ICSSPropertyValue srcValue = null;
        ICSSPropertyValue fontFamilyValue = null;
        ICSSPropertyValue fontStyleValue = null;
        ICSSPropertyValue fontWeightValue = null;
        ICSSPropertyValue embedAsCFFValue = null;
        ICSSPropertyValue advancedAAValue = null;

        if (properties == null)
        {
            this.propertyList = ImmutableList.of();
        }
        else
        {
            this.propertyList = new ImmutableList.Builder<ICSSProperty>().addAll(properties).build();
        }

        if (properties != null)
        {
            // don't use children.add() or addAll() here because JBurg doesn't
            // expect the properties to be part of the tree
            for (final CSSProperty property : properties)
            {
                property.setParent(this);
                final String name = property.getName();
                final ICSSPropertyValue value = property.getValue();
                if (name.equals("src"))
                {
                    sources.add(value);
                    srcValue = value;
                }
                else if (name.equals("fontFamily"))
                {
                    fontFamilyValue = value;
                }
                else if (name.equals("fontStyle"))
                {
                    fontStyleValue = value;
                }
                else if (name.equals("fontWeight"))
                {
                    fontWeightValue = value;
                }
                else if (name.equals("embedAsCFF"))
                {
                    embedAsCFFValue = value;
                }
                else if (name.equals("advancedAntiAliasing"))
                {
                    advancedAAValue = value;
                }
                else
                {
                    // Ignore unknown properties.
                }
            }
        }

        if (srcValue == null)
        {
            source = null;
            ISourceLocation sourceLocation = new SourceLocation(tokenStream.getSourceName(), UNKNOWN, UNKNOWN, tree.getLine(), tree.getCharPositionInLine());
            problems.add(new CSSRequiredDescriptorProblem(sourceLocation, "@font-face", "src"));
        }
        else
        {
            if (srcValue instanceof CSSArrayPropertyValue)
                source = (CSSFunctionCallPropertyValue)(srcValue).getNthChild(0);
            else
                source = (CSSFunctionCallPropertyValue)srcValue;
        }

        if (fontFamilyValue == null)
        {
            fontFamily = null;
            ISourceLocation sourceLocation = new SourceLocation(tokenStream.getSourceName(), UNKNOWN, UNKNOWN, tree.getLine(), tree.getCharPositionInLine());
            problems.add(new CSSRequiredDescriptorProblem(sourceLocation, "@font-face", "fontFamily"));
        }
        else
        {
            fontFamily = fontFamilyValue.toString();
        }

        fontStyle = fontStyleValue == null ? "normal" : fontStyleValue.toString();
        fontWeight = fontWeightValue == null ? "normal" : fontWeightValue.toString();
        isEmbedAsCFF = embedAsCFFValue == null || embedAsCFFValue.toString().equalsIgnoreCase("true");
        isAdvancedAntiAliasing = advancedAAValue == null || advancedAAValue.toString().equalsIgnoreCase("true");
        
    }

    private final CSSFunctionCallPropertyValue source;
    private final ArrayList<ICSSPropertyValue> sources = new ArrayList<ICSSPropertyValue>();
    private final String fontFamily;
    private final String fontStyle;
    private final String fontWeight;
    private final boolean isEmbedAsCFF;
    private final boolean isAdvancedAntiAliasing;
    private final ImmutableList<ICSSProperty> propertyList;

    private final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

    public List<ICompilerProblem> getProblems()
    {
        return problems;
    }

    public ArrayList<ICSSPropertyValue> getSources()
    {
        return sources;
    }
    
    @Override
    public FontFaceSourceType getSourceType()
    {
        return Enum.valueOf(FontFaceSourceType.class, source.name.toUpperCase());
    }

    @Override
    public String getSourceValue()
    {
        String sourceValue = null;
        if(FontFaceSourceType.URL.equals(getSourceType()) 
                || FontFaceSourceType.LOCAL.equals(getSourceType()))
            sourceValue = CSSFunctionCallPropertyValue.getSingleArgumentFromRaw(source.rawArguments);
        return sourceValue;
    }

    @Override
    public String getFontFamily()
    {
        return fontFamily;
    }

    @Override
    public String getFontStyle()
    {
        return fontStyle;
    }

    @Override
    public String getFontWeight()
    {
        return fontWeight;
    }
    
    public boolean getEmbedAsCFF()
    {
        return isEmbedAsCFF;
    }

    @Override
    public ImmutableList<ICSSProperty> getProperties()
    {
        return propertyList;
    }

    @Override
    public boolean getAdvancedAntiAliasing()
    {
        return isAdvancedAntiAliasing;
    }

    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();
        result.append("@font-face {\n");
        result.append("    src : ")
                .append(source)
                .append(";\n");
        result.append("    fontFamily : ").append(fontFamily).append(";\n");
        result.append("    embedAsCFF : ").append(isEmbedAsCFF);
        result.append("    advancedAntiAliasing : ")
                .append(isAdvancedAntiAliasing)
                .append(";\n");
        result.append("    fontStyle : ").append(fontStyle).append(";\n");
        result.append("    fontWeight : ").append(fontWeight).append(";\n");
        result.append("}\n");
        return result.toString();
    }
}
