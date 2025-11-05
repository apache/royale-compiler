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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.problems.CSSInvalidNumberProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Implementation for number CSS property values. The number can also have a
 * unit string. For example: <code>12px; 12em; 12%</code>
 */
public class CSSNumberPropertyValue extends CSSPropertyValue
{
    /**
     * Match a signed real number.
     * 
     * <pre>
     * +100
     * -3.14
     * .25
     * 10.00
     * </pre>
     */
    private static final Pattern PATTERN = Pattern.compile(
            "[\\+\\-]?((\\d+(\\.\\d+)?)|(\\.\\d+))");

    protected CSSNumberPropertyValue(final String numberWithUnit,
                                     final CommonTree tree,
                                     final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.PROPERTY_VALUE);
        assert numberWithUnit != null : "Number can't be null.";

        final Matcher matcher = PATTERN.matcher(numberWithUnit);
        String numberWithoutUnit = null;
        if (matcher.find())
        {
            numberWithoutUnit = matcher.group();
            float parsedFloat = Float.parseFloat(numberWithoutUnit);
            if (Float.isInfinite(parsedFloat) || Float.isNaN(parsedFloat))
            {
                this.number = null;
                this.unit = null;
            }
            else
            {
                this.number = parsedFloat;
                this.unit = numberWithUnit.substring(matcher.end());
            }
        }
        else
        {
            numberWithoutUnit = "";
            this.number = null;
            this.unit = null;
        }
        if (this.number == null)
        {
            ISourceLocation sourceLocation = new SourceLocation(tokenStream.getSourceName(), UNKNOWN, UNKNOWN, tree.getLine(), tree.getCharPositionInLine());
            problems.add(new CSSInvalidNumberProblem(sourceLocation, numberWithoutUnit));
        }
        this.raw = numberWithUnit;
    }

    private final String raw;
    private final Number number;
    private final String unit;

    private final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

    public List<ICompilerProblem> getProblems()
    {
        return problems;
    }

    /**
     * @return The number value.
     */
    public Number getNumber()
    {
        return number;
    }

    /**
     * @return Unit for the number; Or {@code null} if this number doesn't have
     * a unit.
     */
    public String getUnit()
    {
        return unit;
    }

    @Override
    public String toString()
    {
        return raw;
    }
}
