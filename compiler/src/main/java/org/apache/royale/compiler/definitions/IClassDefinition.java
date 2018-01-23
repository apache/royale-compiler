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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.utils.Version;

/**
 * A definition representing a <code>class</code> declaration.
 * <p>
 * An <code>IClassDefinition</code is created from an <code>IClassNode</code>.
 * <p>
 * For example, the class declaration
 * <pre>
 * public class B extends A
 * {
 * }</pre>
 * creates a class definition whose base name is <code>"B"</code>,
 * whose namespace reference is to the <code>public</code> namespace,
 * and whose type reference is <code>null</code>.
 * It has an <code>IReference</code> named "A" to the class that it extends.
 * <p>
 * A class definition is contained within a file scope or a package scope,
 * and contains a class scope.
 * The members of the class are represented by definitions in the class scope.
 */
public interface IClassDefinition extends ITypeDefinition
{
    /**
     * Determines the type of class
     */
    static enum ClassClassification
    {
        /**
         * A class contained with a package
         */
        PACKAGE_MEMBER,

        /**
         * A class contained within a file, outside a package
         */
        FILE_MEMBER,

        /**
         * A synthetic class representing a parameterized class instance, eg.
         * Vector.&lt;String&gt;.
         */
        PARAMETERIZED_CLASS_INSTANCE,
    }
    
    /**
     * The class definition for the <code>*</code> "type".
     */
    static final IClassDefinition ANY_TYPE = ClassDefinition.getAnyTypeClassDefinition();

    /**
     * The type of the <code>null</code> value.
     */
    static final IClassDefinition NULL = ClassDefinition.getNullClassDefinition();

    /**
     * The type of the <code>undefined</code> value.
     */
    static final IClassDefinition UNDEFINED = ClassDefinition.getUndefinedClassDefinition();

    /**
     * The type definition for <code>void</code>.
     */
    static final IClassDefinition VOID = ClassDefinition.getVoidClassDefinition();

    /**
     * Returns the classification of this ActionScript class
     * 
     * @return the {@link ClassClassification} object
     */
    ClassClassification getClassClassification();

    /**
     * Gets a reference to the base class for this class.
     * 
     * @return An {@link IReference} referring to the base class.
     */
    IReference getBaseClassReference();

    /**
     * Finds the definition of the base class for this class.
     * <p>
     * Note that <code>Object</code> does not have a base class, so this method
     * returns <code>null</code> if called on the class definition for
     * <code>Object</code>.
     * <p>
     * It will also return <code>null</code> in the following error cases:
     * <ul>
     * <li>the base class does not exist in the project;</li>
     * <li>the base class is actually an interface;</li>
     * <li>the base class is final;</li>
     * <li>the base class is the same as this class.</li>
     * </ul>
     * <p>
     * Note: {@link SemanticUtils#resolveBaseClass} is similar, but actually
     * reports the various error cases as problems, and returns
     * <code>Object</code> instead of <code>null</code> in the error cases so
     * that the code generator gives the class a valid superclass.
     * 
     * @return An {@link IClassDefinition} or <code>null</code>.
     */
    IClassDefinition resolveBaseClass(ICompilerProject project);

    /**
     * Converts this class's {@link IReference} for its base class
     * to a human-readable String.
     * <p>
     * If this class does not have a base class reference,
     * this method returns the empty string.
     * <p>
     * This method should only be used for displaying the base class,
     * and not for making semantic decisions.
     * 
     * @return The result of calling {@link IReference#getDisplayString}()
     * on the {@link IReference} representing this class's base class.
     */
    String getBaseClassAsDisplayString();

    /**
     * Get the ancestral stack for this class, from this class all the way to
     * Object.
     * 
     * @return ancestral stack, starting with this class
     */
    IClassDefinition[] resolveAncestry(ICompilerProject project);

    /**
     * Returns {@link IReference} objects that will resolve to any interface this
     * class directly implements. This does not walk of the inheritance chain.
     * 
     * @return An array of interface {@link IReference} objects, or an empty array.
     */
    IReference[] getImplementedInterfaceReferences();

    /**
     * Get the definitions of the implemented interfaces. This only returns the
     * interfaces explicitly implemented by the current class, not those that
     * are implemented by base classes or those that are extended by other
     * interfaces.
     * 
     * @return implemented interface definitions
     */
    IInterfaceDefinition[] resolveImplementedInterfaces(ICompilerProject project);

    /**
     * Returns the names of any interfaces that this class directly references.
     * This does not walk up the inheritance chain, rather only looks at what is
     * directly defined on the class
     * 
     * @return an array of interface names, or an empty array
     */
    String[] getImplementedInterfacesAsDisplayStrings();


    /**
     * Determine if this class needs to add an implicit 'implements
     * flash.events.IEventDispatcher' due to the class, or some of its members
     * being marked bindable. If this class is marked bindable, or if it has
     * members that are marked bindable then this class will need to implement
     * IEventDispatcher if no baseclass already implements IEventDispatcher, and
     * no implemented interface extends IEventDispatcher.
     * <p>
     *
     * @param project The project to use to resolve interfaces and base classes
     * @return true if this class needs to add IEventDispatcher to its interface
     * list, and should implement the IEventDispatcher methods.
     */
    public boolean needsEventDispatcher(ICompilerProject project);

    /**
     * Determine if this class needs a static event dispatcher added to it. This
     * is neccessary if it has any static properties that are bindable.
     *
     * @param project Project to use to resolve things.
     * @return true, if we need to codegen a static event dispatcher method
     */
    public boolean needsStaticEventDispatcher(ICompilerProject project);

    /**
     * Creates an iterator for enumerating the superclasses of this class.
     * 
     * @param project The {@link ICompilerProject} within which references
     * should be resolved.
     * @param includeThis A flag indicating whether the enumeration should start
     * with this class rather than with its superclass.
     * @return An {@link IClassIterator} that iterates over
     * {@code IClassDefinition} objects.
     */
    IClassIterator classIterator(ICompilerProject project, boolean includeThis);

    /**
     * Creates an iterator for enumerating all of the interfaces that this class
     * implements. The enumeration includes not just the interfaces that the
     * class directly implements, but the ones that they extend (and the ones
     * those extend, etc.) and the interfaces implemented by its superclasses.
     * 
     * @param project The {@link ICompilerProject} within which references
     * should be resolved
     * @return An iterator that iterates over {@code IInterfaceDefinition}
     * objects.
     */
    Iterator<IInterfaceDefinition> interfaceIterator(ICompilerProject project);

    /**
     * Gets the constructor function for this class.
     * 
     * @return An {@code IFunctionDefinition} representing the constructor.
     */
    IFunctionDefinition getConstructor();

    /**
     * Gets an event definition for an event declared on this class with a
     * particular name such as <code>"click"</code>.
     * <p>
     * If there is no such event, this method returns <code>null</code>.
     * <p>
     * This method does not find events defined on superclasses.
     * 
     * @param name The name of the event.
     * @return An {@code IEventDefinition} object.
     */
    IEventDefinition getEventDefinition(IWorkspace w, String name);

    /**
     * Gets the event definitions for events declared on this class.
     * <p>
     * If there are no events declared, this method returns an empty array.
     * <p>
     * This method does not find events defined on superclasses.
     * 
     * @return An array of {@code IEventDefinition} objects.
     */
    IEventDefinition[] getEventDefinitions(IWorkspace w);

    /**
     * Finds the event definitions for events declared on this class or any of
     * its superclasses.
     * <p>
     * If there are no events declared, this method returns an empty array.
     * <p>
     * The event definitions returned are guaranteed to have unique event names.
     * If an event is declared with the same name on a superclass and a
     * subclass, the event on the subclass overrides the one on the superclass.
     * 
     * @param project An {@code ICompilerProject} object used to determine the
     * superclass chain.
     * @return An array of {@code IEventDefinition} objects.
     */
    IEventDefinition[] findEventDefinitions(ICompilerProject project);

    /**
     * Gets a style definition for a style declared on this class with a
     * particular name such as <code>"fontSize"</code>.
     * <p>
     * If there is no such style, this method returns <code>null</code>.
     * <p>
     * This method does not find styles defined on superclasses.
     * 
     * @param name The name of the style.
     * @return An {@code IStyleDefinition} object.
     */
    IStyleDefinition getStyleDefinition(IWorkspace workspace, String name);

    /**
     * Gets the style definitions for styles declared on this class.
     * <p>
     * If there are no styles declared, this method returns an empty array.
     * <p>
     * This method does not find styles defined on superclasses.
     * 
     * @return An array of {@code IStyleDefinition} objects.
     */
    IStyleDefinition[] getStyleDefinitions(IWorkspace w);

    /**
     * Finds the style definitions for styles declared on this class or any of
     * its superclasses.
     * <p>
     * If there are no styles declared, this method returns an empty array.
     * <p>
     * The style definitions returned are guaranteed to have unique style names.
     * If a style is declared with the same name on a superclass and a subclass,
     * the style on the subclass overrides the one on the superclass.
     * 
     * @param project An {@code ICompilerProject} object used to determine the
     * superclass chain.
     * @return An array of {@code IStyleDefinition} objects.
     */
    IStyleDefinition[] findStyleDefinitions(ICompilerProject project);

    /**
     * Gets an effect definition for an effect declared on this class with a
     * particular name such as <code>"moveEffect"</code>.
     * <p>
     * If there is no such effect, this method returns <code>null</code>.
     * <p>
     * This method does not find effects defined on superclasses.
     * 
     * @param name The name of the effect.
     * @return An {@code IEffectDefinition} object.
     */
    IEffectDefinition getEffectDefinition(IWorkspace w, String name);

    /**
     * Gets the effect definitions for effects declared on this class.
     * <p>
     * If there are no effects declared, this method returns an empty array.
     * <p>
     * This method does not find effects defined on superclasses.
     * 
     * @return An array of {@code IEffectDefinition} objects.
     */
    IEffectDefinition[] getEffectDefinitions(IWorkspace w);

    /**
     * Finds the effect definitions for effect declared on this class or any of
     * its superclasses.
     * <p>
     * If there are no effects declared, this method returns an empty array.
     * <p>
     * The effect definitions returned are guaranteed to have unique effect
     * names. If an effect is declared with the same name on a superclass and a
     * subclass, the effect on the subclass overrides the one on the superclass.
     * 
     * @param project An {@code ICompilerProject} object used to determine the
     * superclass chain.
     * @return An array of {@code IEffectDefinition} objects.
     */
    IEffectDefinition[] findEffectDefinitions(ICompilerProject project);

    /**
     * Gets the name of the default property for this class.
     */
    String getDefaultPropertyName(ICompilerProject project);

    /**
     * Returns the classes that are specified as replacing the given class. This
     * replacement information comes from the [Alternative] metadata found on
     * classes in the Flex language
     * 
     * @param project the associated project
     * @param version the {@link Version} we are working against. If the
     * version is less than the "since" metaData, the replacement class will not
     * be returned
     * @return an array of IClassDefinition objects. An empty array if there are
     * no replacements
     */
    IClassDefinition[] getAlternativeClasses(ICompilerProject project, Version version);

    /**
     * Finds all metadata with a specified tag name.
     * <p>
     * If the tag name is in the set
     * {@code IMetaAttributeConstants.NON_INHERITING_METATAGS}, then only this
     * class is searched; otherwise the superclass chain is searched as well.
     * 
     * @param name The tag name of the desired metadata, such as
     * <code>"Event"</code>.
     * @param project An {@code ICompilerProject} object, for resolving the
     * superclass chain.
     * @return An array of {@code IMetaTag} objects. If there is no metadata with
     * the specified tag name, the result is an empty array, not
     * <code>null</code>.
     */
    IMetaTag[] findMetaTagsByName(String name, ICompilerProject project);

    /**
     * Gets the skin state names declared on this class.
     * <p>
     * If there are no skin states declared, this method returns an empty array.
     * <p>
     * This method does not find skin states defined on superclasses.
     * 
     * @param problems
     * @return An array of {@code String} objects.
     */
    String[] getSkinStates(Collection<ICompilerProblem> problems);

    /**
     * Finds the skin state names declared on this class or any of its
     * superclasses.
     * <p>
     * If there are no skin states declared, this method returns an empty array.
     * <p>
     * The state names returned are guaranteed to have unique names with no
     * duplicates.
     * 
     * @param project An {@code ICompilerProject} object used to determine the
     * superclass chain.
     * @param problems
     * @return An array of {@code String} objects.
     */
    String[] findSkinStates(ICompilerProject project, Collection<ICompilerProblem> problems);

    /**
     * Gets the skin parts declared on this class.
     * <p>
     * If there are no skin parts declared, this method returns an empty array.
     * <p>
     * This method does not find skin parts defined on superclasses.
     * 
     * @param problems
     * @return An array of {@code IMetaTag} objects.
     */
    IMetaTag[] getSkinParts(Collection<ICompilerProblem> problems);

    /**
     * Finds the skin parts declared on this class or any of its superclasses.
     * <p>
     * If there are no skin parts declared, this method returns an empty array.
     * <p>
     * The skin parts returned are guaranteed to have unique names. If a skin
     * part is declared with the same name on a superclass and a subclass, the
     * skin part on the subclass overrides the one on the superclass.
     * 
     * @param project An {@code ICompilerProject} object used to determine the
     * superclass chain.
     * @param problems
     * @return An array of {@code IVariableDefinition} objects.
     */
    IMetaTag[] findSkinParts(ICompilerProject project, Collection<ICompilerProblem> problems);

    /**
     * Gets the state names declared on this class.
     * <p>
     * If there are no states declared, this method returns an empty set.
     * <p>
     * This method does not find states defined on superclasses.
     * 
     * @return A set of {@code String} objects.
     */
    Set<String> getStateNames();

    /**
     * Finds the state names declared on this class or any of its superclasses.
     * <p>
     * If there are no states declared, this method returns an empty set.
     * 
     * @param project An {@code ICompilerProject} object used to determine the
     * superclass chain.
     * @return A set of {@code String} objects.
     */
    Set<String> findStateNames(ICompilerProject project);
    
    /**
     * For a Flex component class, gets the path to the icon file representing
     * the component, as specified by <code>[IconFile(...)]<code> metadata.
     * 
     * @return The path as a String, or <code>null</code> if there is no such metadata.
     */
    String getIconFile();
    
    /**
     * Finds the {@link IClassDefinition} for the host component class, declared with <code>[HostComponent(...)]<code> metadata on
     * this class or one of its superclasses.
     * <p>
     * If there is no <code>[HostComponent(...)]<code> metadata on this class or any of its superclasses, this method returns null.
     * @param project An {@code ICompilerProject} object used to determine the
     * superclass chain and to resolve the class referenced in the <code>[HostComponent(...)]<code> metadata.
     * @return The {@link IClassDefinition} for the host component class.
     */
    IClassDefinition resolveHostComponent(ICompilerProject project);

    /**
     * This interface represents what the <code>classIterator</code> method
     * returns. In addition to being an {@link Iterator} of
     * {@link IClassDefinition} objects, it detects loops in the superclass
     * chain.
     */
    static interface IClassIterator extends Iterator<IClassDefinition>
    {
        /**
         * Reports whether the iterator found a loop. Call this after the
         * iteration is complete.
         * 
         * @return <code>true</code> if the iterator encountered a loop and
         * <code>false</code> otherwise
         */
        public boolean foundLoop();
    }
}
