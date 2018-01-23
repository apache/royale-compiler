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

package org.apache.royale.compiler.mxml;

import java.util.Collection;

import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents a block of some form of text found in MXML content.
 */
public interface IMXMLTextData extends IMXMLUnitData
{
    /**
     * Represents different kinds of text elements.
     */
    enum TextType
    {
        /**
         * Text
         */
        TEXT,

        /**
         * Whitespace found in the MXML
         */
        WHITESPACE,

        /**
         * A CDATA block
         */
        CDATA,

        /**
         * A comment block
         */
        COMMENT,

        /**
         * An ASDoc comment block
         */
        ASDOC
    }

    /**
     * Gets the type of the text.
     * 
     * @return A {@link TextType} value.
     */
    TextType getTextType();

    /**
     * Gets the content of the text.
     * 
     * @return The content as a String.
     */
    String getContent();

    /**
     * Gets this unit's compilable text as a String.
     * <p>
     * Comments have no compilable text. The compilable text of a CDATA unit is
     * the text between the <![CDATA[ and the ]]>.
     * 
     * @return This unit's compilable text.
     */
    String getCompilableText();

    /**
     * Gets the start of this unit's compilable text.
     * 
     * @return The start of the compilable text
     */
    int getCompilableTextStart();

    /**
     * Gets the end of this unit's compilable text.
     * 
     * @return The end of the compilable text
     */
    int getCompilableTextEnd();

    /**
     * Gets the line of this unit's compilable text.
     * 
     * @return The line of the compilable text
     */
    int getCompilableTextLine();

    /**
     * Gets the column of this unit's compilable text.
     * 
     * @return The column of the compilable text
     */
    int getCompilableTextColumn();

    /**
     * Gets the source fragments that make up this text.
     * 
     * @param problems A collection of compiler problems to add problems to.
     * @return An array of source fragments.
     */
    ISourceFragment[] getFragments(Collection<ICompilerProblem> problems);
}
