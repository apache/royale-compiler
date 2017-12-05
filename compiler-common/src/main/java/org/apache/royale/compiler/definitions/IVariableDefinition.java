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
import org.apache.royale.compiler.tree.as.IVariableNode;

/**
 * A definition representing a <code>var</code> declaration.
 * <p>
 * An <code>IVariableDefinition</code is created from an <code>IVariableNode</code>
 * for which <code>isConst()</code> is <code>false</code>.
 * <p>
 * For example, the declaration
 * <pre>
 * public var i:int = 1;
 * </pre>
 * creates a variable definition whose base name is <code>"i"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is named <code>"int"</code>.
 * <p>
 * A variable definition is contained within a file scope, a package scope,
 * a class scope, an interface scope, or a function scope;
 * it does not contain a scope.
 */
public interface IVariableDefinition extends IDocumentableDefinition
{
    /**
     * Variable classifications (local, argument, class member, interface
     * member, and package member)
     */
    static enum VariableClassification
    {
        /**
         * Local variable contained within a non-visible scope
         */
        LOCAL,

        /**
         * A parameter in a function scope
         */
        PARAMETER,

        /**
         * A variable that is a member of a class
         */
        CLASS_MEMBER,

        /**
         * A variable that is a member of an interface
         */
        INTERFACE_MEMBER,

        /**
         * A variable that is a member of a package
         */
        PACKAGE_MEMBER,

        /**
         * A variable that is a member of a file
         */
        FILE_MEMBER,

        /**
         * A variable that represents the id attribute of an MXML id
         */
        MXML_ID
    }

    /**
     * Get the classification for this variable (local, parameter, class member,
     * etc)
     * 
     * @return variable classification
     */
    VariableClassification getVariableClassification();

    /**
     * Returns the {@link IVariableNode} from which this definition was created,
     * if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    IVariableNode getVariableNode();

    /**
     * Attempt to resolve the initial value assigned to this variable. This may
     * cause the AST for the definition to get reloaded, so could be slow.
     * 
     * @param project the Project to resolve things in
     * @return The constant value of the initial value, if one can be
     * determined, or null if it can't be determined. This will be a String,
     * int, double, boolean, or Namespace depending on what the initial value
     * was. The value could also be
     * org.apache.royale.abc.ABCConstants.UNDEFINED_VALUE if the initial
     * value was the undefined constant value Callers will need to use
     * instanceof to see what type the value is.
     */
    Object resolveInitialValue(ICompilerProject project);

    /**
     * Check whether the variable is a skin part. Both optional and required
     * skin parts are checked for.
     * 
     * @return true if a skin part
     */
    boolean isSkinPart();

    /**
     * Check whether the variable is a required skin part. Only required skin
     * parts are checked for.
     * 
     * @return true if a required skin part. false if not a skin part or
     * optional
     */
    boolean isRequiredSkinPart();

    /**
     * If this {@code IVariableDefinition} is of type <code>Array</code> and has
     * <code>[ArrayElementType("...")]</code> metadata, this method returns the
     * specified type for the array elements. Otherwise, it returns
     * <code>null</code>.
     */
    String getArrayElementType(ICompilerProject project);

    /**
     * If this {@code IVariableDefinition} is of type
     * <code>IDeferredInstance</code> and has <code>[InstanceType("...")}</code>
     * metadata, this method returns the specified type for the deferred
     * instance. Otherwise, it returns <code>null</code>.
     */
    String getInstanceType(ICompilerProject project);

    /**
     * If this {@code IVariableDefinition} has
     * <code>[PercentProxy(...)] metadata,
     * this method returns the specified property name.
     * Otherwise, it returns <code>null</code>
     */
    String getPercentProxy(ICompilerProject project);

    /**
     * If this {@code IVariableDefinition} has <code>[RichTextContent]</code>
     * metadata, this method returns <code>true</code> Otherwise, it returns
     * <code>false</code>
     */
    boolean hasRichTextContent(ICompilerProject project);

    /**
     * If this {@code IVariableDefinition} has <code>[CollapseWhiteSpace]</code>
     * metadata, this method returns <code>true</code> Otherwise, it returns
     * <code>false</code>
     */
    boolean hasCollapseWhiteSpace(ICompilerProject project);

    /**
     * Returns <code>true</code> if this {@code IVariableDefinition} has
     * <code>[Inspectable(...)]</code> metadata that specifies
     * <code>format="Color"</code> Otherwise, returns <code>false</code>.
     */
    boolean isColor(ICompilerProject project);
}
