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

package org.apache.royale.compiler.definitions;

import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.metadata.IStyleTagNode;

/**
 * Represents style metadata decorating a class definition,
 * such as <code>Style(name="color", type="uint", format="color", inherit="yes")]</code>.
 * <p>
 * Use {@link #getBaseName}() to get the style's name (e.g., <code>"color"</code>.
 */
public interface IStyleDefinition extends IMetadataDefinition
{
    /**
     * Returns the {@link IStyleTagNode} from which this definition was created,
     * if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    @Override
    IStyleTagNode getNode();

    /**
     * Gets the value of this style's <code>arrayType</code> attribute.
     * <p>
     * When the <code>type</code> attribute is <code>"Array</code>,
     * the <code>arrayType</code> attribute specifies the type
     * of the array elements.
     * 
     * @return The value of the <code>arrayType</code> attribute as a String,
     * or <code>null</code>.
     */
    String getArrayType();

    /**
     * Resolves the type specified by the <code>arrayType</code> attribute
     * to a class or interface definitions.
     * <p>
     * When the value of the <code>type</code> attribute is <code>"Array"</code>,
     * the <code>arrayType</code> attribute specifies the type
     * of the array elements.
     * 
     * @param project The {@link ICompilerProject} within which references
     * should be resolved.
     * @return An {@link ITypeDefinition} for the resolved array type,
     * or <code>null</code>.
     */
    ITypeDefinition resolveArrayType(ICompilerProject project);

    /**
     * Gets the possible values of this style, as specified by
     * its <code>enumeration</code> attribute.
     * <p>
     * When the value of the <code>type</code> attribute is <code>"String"</code>,
     * the <code>enumeration</code> attribute specifies the allowed String values.
     * 
     * @return The comma-separated values in the <code>enumeration</code>
     * attribute as an array of Strings, or an empty array.
     */
    String[] getEnumeration();

    /**
     * Gets the value of this style's <code>format</code> attribute.
     * 
     * @return The value of the <code>format</code> attribute as a String,
     * or <code>null</code>.
     */
    String getFormat();

    /**
     * Determines whether this style represents a color.
     * 
     * @return <code>true</code> if the value of the <code>format</code>
     * attribute is <code>"Color"</code>.
     */
    boolean isColor();

    /**
     * Gets the value of this style's <code>inherit</code> attribute.
     * 
     * @return The value of the <code>inherit</code> attribute as a String,
     * or <code>null</code>.
     */
    String getInherit();
    
    /**
     * Determines whether this style is inheriting.
     * 
     * @return <code>true</code> if the value of the <code>inherit</code>
     * attribute is <code>"yes"</code>.
     */
    boolean isInheriting();

    /**
     * Gets the value of this style's <code>states</code> attribute.
     * 
     * @return The comma-separated values in the <code>states</code> attribute
     * as an array of Strings, or an empty array.
     */
    String[] getStates();

    /**
     * Gets the value of this style's <code>themes</code> attribute.
     * 
     * @return The comma-separated values in the <code>themes</code> attribute
     * as an array of Strings, or an empty array.
     */
    String[] getThemes();

    /**
     * Gets the value of this style's <code>minValue</code> attribute.
     * 
     * @return The value of the <code>minValue</code> attribute as a String,
     * or <code>null</code>.
     */
    String getMinValue();

    /**
     * Gets the value of this style's <code>minValueExclusive</code> attribute.
     * 
     * @return The value of the <code>minValueExclusive</code> attribute as a String,
     * or <code>null</code>.
     */
    String getMinValueExclusive();

    /**
     * Gets the value of this style's <code>maxValue</code> attribute.
     * 
     * @return The value of the <code>maxValue</code> attribute as a String,
     * or <code>null</code>.
     */
    String getMaxValue();

    /**
     * Gets the value of this style's <code>maxValueExclusive</code> attribute.
     * 
     * @return The value of the <code>maxValueExclusive</code> attribute as a String,
     * or <code>null</code>.
     */
    String getMaxValueExclusive();
}
