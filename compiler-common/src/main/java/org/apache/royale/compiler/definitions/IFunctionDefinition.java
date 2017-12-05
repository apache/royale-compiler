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

import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IFunctionNode;

/**
 * A definition representing a <code>function</code> declaration.
 * <p>
 * An <code>IFunctionDefinition</code is created from an <code>IFunctionNode</code>.
 * <p>
 * For example, the function declaration
 * <pre>public function f(i:int):int;</pre>
 * creates a function definition whose base name is <code>"f"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is named <code>"Function"</code>.
 * It has an <code>IReference</code> named <code>"int"</code>
 * representing its return type.
 * It also owns one parameter definition named <code>"i"</code>.
 * <p>
 * A function definition is contained within a file scope, a package scope,
 * a class scope, an interface scope, or a function scope;
 * it contains a function scope.
 */
public interface IFunctionDefinition extends IScopedDefinition
{
    /**
     * Function classifications (local, class member, interface member, and
     * package member)
     */
    static enum FunctionClassification
    {
        /**
         * Local function contained with a non-visible scope
         */
        LOCAL,
        
        /**
         * A function that is a member of a class
         */
        CLASS_MEMBER,
        
        /**
         * A function that is a member of a interface
         */
        INTERFACE_MEMBER,
        
        /**
         * A function that is a member of a package
         */
        PACKAGE_MEMBER,
        
        /**
         * A function that is a member of a file
         */
        FILE_MEMBER
    }

    /**
     * Get the classification for this function (local, argument, class member,
     * etc)
     * 
     * @return function classification
     */
    FunctionClassification getFunctionClassification();

    /**
     * Get the parameters of this function as an array of
     * {@link IParameterDefinition} elements
     * 
     * @return the parameters of this function
     */
    IParameterDefinition[] getParameters();

    /**
     * Returns <code>true</code> if the function has required parameters.
     */
    boolean hasRequiredParameters();
    
    /**
     * Gets a reference to the return type for this function.
     * <p>
     * For example, for the function <code>private function f(i:int):String</code>,
     * this is a reference to the <code>String</code> class.
     * 
     * @return An {@link IReference} to a class or interface.
     */
    IReference getReturnTypeReference();

    /**
     * Resolve this function's return type in the given scope and find its
     * definition
     * 
     * @param project the {@link ICompilerProject} in which to resolve
     * references
     * @return an {@link ITypeDefinition} representing this function's return
     * type, or null
     */
    ITypeDefinition resolveReturnType(ICompilerProject project);

    /**
     * Converts this definition's {@link IReference} for its return type
     * to a human-readable String.
     * <p>
     * If the definition does not have a return type reference,
     * this method returns the empty string.
     * <p>
     * This method should only be used for displaying return types in problems,
     * and not for making semantic decisions.
     * <p>
     * Note: 
     * 
     * @return The result of calling {@link IReference#getDisplayString}()
     * on the {@link IReference} representing this definition's return type.
     */
    // TODO Builder is currently using this functionality in inconsistent ways;
    // sometimes it interprets empty string to as <code>void</code> and sometimes as <code>*</code>.
    String getReturnTypeAsDisplayString();

    /**
     * Is this a constructor?
     * 
     * @return true if the member is a constructor
     */
    boolean isConstructor();

    /**
     * Is this a cast function?
     * 
     * @return true if the member is a cast function
     */
    boolean isCastFunction();

    /**
     * Is this function marked as <code>inline</code>?
     * 
     * @return <code>true</code> if the function is <code>inline</code>.
     */
    boolean isInline();

    /**
     * Can this function be inlined
     * 
     * @return true if function can be inlined
     */
    boolean inlineFunction();

    /**
     * Is this function actually overriding a function in a base class
     * 
     * @return true if overriding Note that for valid code, isOverride and
     * overridesAncestor are the same, but overridesAncestor can return true
     * even if the override keyword is missing. Conversely, isOverride can
     * return true, even if there is no matching function in a parent interface
     */
    boolean overridesAncestor(ICompilerProject project);

    /**
     * Finds the definition of the IFunctionDefinition that we are overriding
     * 
     * @return an {@link IFunctionDefinition} or null
     */
    IFunctionDefinition resolveOverriddenFunction(ICompilerProject project);

    /**
     * Determines whether this function is an implementation of a function from
     * an implements Interface
     * 
     * @return true if this is an implementation
     */
    boolean isImplementation(ICompilerProject project);

    /**
     * Finds the definition of the IFunctionDefinition that defines this
     * function
     * 
     * @return the parent {@link IFunctionDefinition} or null
     */
    IFunctionDefinition resolveImplementedFunction(ICompilerProject project);

    /**
     * Returns the {@link IFunctionNode} from which this definition was created,
     * if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     */
    IFunctionNode getFunctionNode();
}
