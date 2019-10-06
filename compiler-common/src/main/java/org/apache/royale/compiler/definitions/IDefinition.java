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

import java.util.List;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.definitions.metadata.IDeprecationInfo;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;

/**
 * The base class for all definitions.
 * <p>
 * A definition is something in source code that can be referred to by name:
 * a package, namespace, class, interface, function, getter, setter,
 * parameter, variable, constant, event, style, or effect.
 * <p>
 * <p>
 * Each definition has
 * <ul>
 * <li>a source location;</li>
 * <li>an optional list of metadata annotations;</li>
 * <li>a namespace reference indicating its visibility,
 * such as <code>public</code>;</li>
 * <li>a set of flags indicating modifiers such as <code>static</code>
 * or <code>override</code>;</li>
 * <li>a base, or unqualified, name;</li>
 * <li>a type reference (which may be <code>null</code>
 * for some types of definitions.</li>
 * </ul>
 * Specific types of definitions of course store additional information.
 * For example, a class definition stores a reference to the class it extends
 * and references to the interfaces that it implements.
 * <p>
 * Definitions refer to other definitions indirectly, by name,
 * using an <code>IReference</code>. See the <code>references</code>
 * subpackage for an explanation of this design.
 */
public interface IDefinition
{
    /**
     * Gets the local source path of the entire definition (not just the name).
     * It is normalized.
     * 
     * @return An <code>String<code> for the path.
     */
    String getSourcePath();

    /**
     * Gets the local starting offset of the entire definition (not just the
     * name). It is zero-based.
     * 
     * @return An <code>int</code> for the offset.
     */
    int getStart();

    /**
     * Gets the local ending offset of this entire definition (not just the
     * name). It is zero-based.
     * 
     * @return An <code>int</code> for the offset.
     */
    int getEnd();

    /**
     * Gets the local line number of this entire definition (not just the name).
     * It is zero-based.
     * 
     * @return An <code>int</code> for the line number.
     */
    int getLine();

    /**
     * Gets the local column number of the entire definition (not just the
     * name). It is zero-based.
     * 
     * @return An <code>int</code> for the column number.
     */
    int getColumn();

    /**
     * Gets the absolute starting offset of the entire definition (not just the
     * name). It is zero-based.
     * 
     * @return An <code>int</code> for the offset.
     */
    int getAbsoluteStart();

    /**
     * Gets the absolute ending offset of the entire definition (not just the
     * name). It is zero-based.
     * 
     * @return An <code>int</code> for the offset.
     */
    int getAbsoluteEnd();

    /**
     * Gets the base (i.e., unqualified) name of this definition.
     * <p>
     * For example, the base name of the class definition
     * for the class <code>flash.display.Sprite</code>
     * is <code>"Sprite"</code>.
     * 
     * @return The base name as a String.
     */
    String getBaseName();

    /**
     * Gets the fully-qualified package name for this definition.
     * <p>
     * For example, the backage name of the class definition
     * for the class <code>flash.display.Sprite</code>
     * is <code>"flash.display"</code>
     * 
     * @return The fully-qualified package name as a String.
     */
    String getPackageName();

    /**
     * Gets the fully-qualified name of this definition.
     * <p>
     * For example, the fully-qualified name of the class definition
     * for the class <code>flash.display.Sprite</code> is
     * <code>"flash.display.Sprite"</code>.
     * 
     * @return The fully-qualified name as a String.
     */
    String getQualifiedName();

    /**
     * Gets the local starting offset of the name of this definition. It is
     * zero-based.
     * 
     * @return An <code>int</code> for the offset.
     */
    int getNameStart();

    /**
     * Gets the local ending offset of the name of this definition. It is
     * zero-based.
     * 
     * @return An <code>int</code> for the offset.
     */
    int getNameEnd();

    /**
     * Gets the local line number of the name of this definition. It is
     * zero-based.
     * 
     * @return An <code>int</code> for the line number.
     */
    int getNameLine();

    /**
     * Gets the local column number of the name of this definition. It is
     * zero-based.
     * 
     * @return An <code>int</code> for the column number.
     */
    int getNameColumn();

    /**
     * Gets the scope in which this definition exists.
     * 
     * @return An IASScope for the containing scope, or <code>null</code> if
     * this definition has not yet been added to a scope.
     */
    IASScope getContainingScope();

    /**
     * Gets the parent definition of this definition.
     * 
     * @return An IDefinition for the parent, or <code>null</code> if there is
     * no parent.
     */
    IDefinition getParent();

    /**
     * Gets an ancestor definition of this definition.
     * <p>
     * This method walks up the chain of parent definitions and returns the
     * first one having the specified type.
     * 
     * @param ancestorType A Class specifying the desired type of ancestor.
     * @return An IDefinition for the ancestor.
     */
    IDefinition getAncestorOfType(Class<? extends IDefinition> ancestorType);

    /**
     * Gets the file path in which this definition is defined.
     * 
     * @return A String for the file path.
     */
    String getContainingFilePath();

    /**
     * Gets the source file path in which this definition is defined.
     * <p>
     * For definitions from a source file (like *.as), the result is the same as
     * {@link #getContainingFilePath()}.
     * <p>
     * If the definition is from a SWC library, and the library source path was
     * set, the result is the file path in the source directory. If the source
     * path was not set, the result is null.
     * 
     * @param project the containing project
     * @return A String for the file path.
     */
    String getContainingSourceFilePath(ICompilerProject project);

    /**
     * Is this definition marked as <code>dynamic</code>?
     * 
     * @return <code>true</code> if the definition is <code>dynamic</code>.
     */
    boolean isDynamic();

    /**
     * Is this definition marked as <code>final</code>?
     * 
     * @return <code>true</code> if the definition is <code>final</code>.
     */
    boolean isFinal();

    /**
     * Is this definition marked as <code>native</code>?
     * 
     * @return <code>true</code> if the definition is <code>native</code>.
     */
    boolean isNative();

    /**
     * Is this definition marked as <code>override</code>?
     * 
     * @return <code>true</code> if the definition is <code>override</code>.
     */
    boolean isOverride();

    /**
     * Is this definition marked as <code>static</code>?
     * 
     * @return <code>true</code> if the definition is <code>static</code>.
     */
    boolean isStatic();

    /**
     * Is this definition marked as <code>abstract</code>?
     * 
     * @return <code>true</code> if the definition is <code>abstract</code>.
     */
    boolean isAbstract();

    /**
     * Determines whether the specified modifier is present on this definition.
     * See {@link ASModifier} for the list of modifiers.
     * 
     * @return <code>true</code> if the modifier is present.
     */
    boolean hasModifier(ASModifier modifier);

    ModifiersSet getModifiers();

    /**
     * Determines whether the specified namespace is present on this definition.
     * <p>
     * Namespaces include <code>public</code>, <code>private</code>,
     * <code>protected</code>, and <code>internal</code>, plus any custom
     * namespaces available in the definition's scope.
     * <p>
     * The namespace is specified by an INamespaceReference, which can represent
     * not only a custom namespace like <code>ns1</code> but also
     * <code>ns1::ns2</code>, <code>(ns1::ns2)::ns3</code>, etc.
     * 
     * @param namespace An INamespaceReference specifying the namespace to check
     * for.
     * @return <code>true</code> if the namespace is present.
     */
    boolean hasNamespace(INamespaceReference namespace, ICompilerProject project);

    /**
     * Gets the namespace that this definition belongs to.
     * 
     * @return An INamespaceReference representing the namespace. For some
     * built-in namespaces, you can:
     * <code>== NamespaceDefinition.getPublic()</code>,
     * <code>instanceof IPublicNamespaceDefinition</code>,
     * <code>instanceof IProtectedNamespaceDefinition</code>, or For a custom
     * namespace, the INamespaceReference will represent an expression of the
     * form <code>ns1</code>, <code>ns1::ns2</code>,
     * <code>(ns1::ns2)::ns3</code>, etc.
     */
    INamespaceReference getNamespaceReference();

    /**
     * Resolves the namespace specified for this definition to its
     * INamespaceDefinition. Built-in namespaces will return <code>null</code>.
     * 
     * @return An {@link INamespaceDefinition} or <code>null</code>.
     */
    INamespaceDefinition resolveNamespace(ICompilerProject project);
    
    /**
     * Determines whether this definition is in a public namespace.
     * 
     * @return <code>true</code> if it is.
     */
    boolean isPublic();
    
    /**
     * Determines whether this definition is in a private namespace.
     * 
     * @return <code>true</code> if it is.
     */
    boolean isPrivate();
    
    /**
     * Determines whether this definition is in a protected namespace.
     * 
     * @return <code>true</code> if it is.
     */
    boolean isProtected();
    
    /**
     * Determines whether this definition is in an internal namespace.
     * 
     * @return <code>true</code> if it is.
     */
    boolean isInternal();
    
    /**
     * Gets a reference to the type of this definition.
     * <p>
     * For a constant, variable, parameter, getter, or setter definition,
     * the type reference is determined by the type annotation.
     * For example, for a variable declaration like <code>var i:int</code>,
     * the type reference is a reference to the <code>int</code> class.
     * (For a setter, the relevant type annotation is on the parameter,
     * not the return type, which is supposed to be <code>void</code>.)
     * If the type annotation is missing, the reference is <code>null</code>.
     * <p>
     * For a function definition, the type reference is always
     * to the <code>Function</code> class. You must use the
     * <code>getReturnTypeReference()</code> method of
     * <code>FunctionDefinition</code> to get the function's return type.
     * <p>
     * For a rest parameter, the type reference is always
     * to the <code>Array</code> class.
     * <p>
     * For a package, class, or interface definition,
     * the type reference is <code>null</code>.
     * <p>
     * For style and event definitions, the type reference
     * is determined by the <code>type</code> attribute in the metadata.
     * 
     * @return An {@link IReference} to a class or interface.
     */
    IReference getTypeReference();

    /**
     * Resolves the type of this definition to an {@link ITypeDefinition}.
     * 
     * @param project The {@link ICompilerProject} within which references are
     * to be resolved.
     * @return An {@link ITypeDefinition} or <code>null</code>
     */
    ITypeDefinition resolveType(ICompilerProject project);

    /**
     * Converts this definition's type reference to a human-readable String.
     * <p>
     * If the definition does not have a type reference,
     * this method returns the empty string.
     * <p>
     * This method should only be used for displaying types in problems,
     * and not for making semantic decisions.
     * 
     * @return The result of calling {@link IReference#getDisplayString}()
     * on the {@link IReference} returned by {@link #getTypeReference}().
     */
    String getTypeAsDisplayString();

    /**
     * Is this definition an implicit definition that doesn't actually appear in
     * the source file? Examples include <code>this</code>, <code>super</code>,
     * default constructors, and cast functions.
     */
    boolean isImplicit();

    /**
     * Gets all the {@link IMetaTagNode} objects that match the given name
     * 
     * @param name the name to match, such as Event, Style, IconFile, etc
     * @return an array of {@link IMetaTagNode} objects, or empty array (never
     * null)
     */
    IMetaTag[] getMetaTagsByName(String name);

    /**
     * Returns all the {@link IMetaTagNode} objects as an array
     * 
     * @return an array of objects, or an empty array
     */
    IMetaTag[] getAllMetaTags();

    /**
     * Determines if a specific {@link IMetaTagNode} exists in this collection
     * 
     * @param name the name of the tag
     * @return true if it exists
     */
    boolean hasMetaTagByName(String name);

    /**
     * Returns the first {@link IMetaTagNode} matching the given name
     * 
     * @param name the name to search for
     * @return an {@link IMetaTagNode} or null
     */
    IMetaTag getMetaTagByName(String name);

    /**
     * Returns the {@link IDefinitionNode} from which this definition was
     * created, if the definition came from the AST for a source file.
     * <p>
     * This method may require the AST to be reloaded or regenerated and
     * therefore may be slow.
     * <p>
     * More specific definition interfaces such as {@link IClassDefinition}
     * redeclare this method to return a more specific node interface such as
     * IClassNode.
     */
    IDefinitionNode getNode();

    /**
     * Whether this definition was specified as "Bindable" in its metadata
     * 
     * @return true if this definition is Bindable.
     */
    boolean isBindable();

    /**
     * @return a collection of Bindable event names, if any were specified in
     * the metadata
     */
    List<String> getBindableEventNames();

    /**
     * @return true if this definition has metadata [Bindable(style="true")]
     */
    boolean isBindableStyle();

    /**
     * @return true if contingent definition
     */
    boolean isContingent();

    /**
     * @return <code>true</code> if this definition is for an auto-generated
     * embed class (i.e., one created for a <code>var</code> with
     * <code>[Embed(...)]</code> metadata).
     */
    boolean isGeneratedEmbedClass();

    /**
     * Check if the contingent definition is needed
     * 
     * @param project
     * @return true is the contingent definition is needed
     */
    boolean isContingentNeeded(ICompilerProject project);
    
    /**
     * Checks if this definition is deprecated.
     * 
     * @return <code>true</code> if it has <code>[Deprecated]</code> metadata.
     */
    boolean isDeprecated();

    /**
     * Gets the information in the definition's <code>[Deprecated]</code> metadata.
     * 
     * @return An {@link IDeprecationInfo} object.
     */
    IDeprecationInfo getDeprecationInfo();
    
    /**
     * Debugging method that can be used to assert that a definition is a specified project.
     * @param project
     * @return true if this definition is in the specified project.
     */
    boolean isInProject(ICompilerProject project);
}
