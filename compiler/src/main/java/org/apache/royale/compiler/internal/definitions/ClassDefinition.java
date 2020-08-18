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

import static org.apache.royale.compiler.common.DependencyType.EXPRESSION;
import static org.apache.royale.compiler.common.DependencyType.INHERITANCE;
import static org.apache.royale.compiler.common.DependencyType.SIGNATURE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IEffectDefinition;
import org.apache.royale.compiler.definitions.IEventDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IGetterDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.definitions.IStyleDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.config.QNameNormalization;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnitFactory;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.problems.DuplicateSkinStateProblem;
import org.apache.royale.compiler.problems.EmbedMultipleMetaTagsProblem;
import org.apache.royale.compiler.problems.HostComponentClassNotFoundProblem;
import org.apache.royale.compiler.problems.HostComponentMustHaveTypeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MissingSkinPartProblem;
import org.apache.royale.compiler.problems.MissingSkinStateProblem;
import org.apache.royale.compiler.problems.OnlyOneHostComponentAllowedProblem;
import org.apache.royale.compiler.problems.SkinPartsMustBePublicProblem;
import org.apache.royale.compiler.problems.UnimplementedAbstractMethodProblem;
import org.apache.royale.compiler.problems.WrongSkinPartProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.metadata.IEventTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IStyleTagNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Each instance of this class represents the definition of an ActionScript
 * class in the symbol table.
 * <p>
 * After a class definition is in the symbol table, it should always be accessed
 * through the read-only <code>IClassDefinition</code> interface.
 */
public class ClassDefinition extends ClassDefinitionBase implements IClassDefinition
{
    // The ClassDefinitions for *, Null, Undefined, and void are implicit;
    // unlike for, say, Object, these are not read from any SWC.
    // They must be created by the compiler itself.
    private static final ClassDefinition ANY_TYPE;
    private static final ClassDefinition NULL;
    private static final ClassDefinition UNDEFINED;
    private static final ClassDefinition VOID;

    // Sentinel value used internally by this class to indicate that
    // the ABC class definition does not have [RemoteClass] meta-data.
    private static final String NO_REMOTE_CLASS_ALIAS = "";

    private static final String HOST_COMPONENT = "hostComponent";
    
    // this gets replaced in some projects
    public static String Event = IASLanguageConstants.Event;

    private static ClassDefinition makeImplicitClassDefinition(String name)
    {
        ClassDefinition def = new ClassDefinition(name, NamespaceDefinition.getPublicNamespaceDefinition());
        def.setPublic();
        def.setFinal();
        def.setImplicit();
        ASScope scope = new TypeScope(null, def);
        scope.setContainingDefinition(def);
        def.setContainedScope(scope);
        return def;
    }

    /**
     * Private class that bundles up information extracted from [Frame]
     * meta-data tags.
     */
    private static class FrameInformation
    {
        IResolvedQualifiersReference factoryClass;
        Collection<IResolvedQualifiersReference> extraClasses;
    }

    static
    {
        ANY_TYPE = makeImplicitClassDefinition(IASLanguageConstants.ANY_TYPE);
        ANY_TYPE.setDynamic();

        NULL = makeImplicitClassDefinition(IASLanguageConstants.Null);
        UNDEFINED = makeImplicitClassDefinition(IASLanguageConstants.Undefined);
        VOID = makeImplicitClassDefinition(IASLanguageConstants.void_);
    }

    public static IClassDefinition getAnyTypeClassDefinition()
    {
        return ANY_TYPE;
    }

    public static IClassDefinition getNullClassDefinition()
    {
        return NULL;
    }

    public static IClassDefinition getUndefinedClassDefinition()
    {
        return UNDEFINED;
    }

    public static IClassDefinition getVoidClassDefinition()
    {
        return VOID;
    }

    private static String getGeneratedURIPrefix(INamespaceReference namespaceRef)
    {
        if (namespaceRef instanceof NamespaceDefinition.INamespaceWithPackageName)
            return ((NamespaceDefinition.INamespaceWithPackageName)namespaceRef).getGeneratedURIPrefix();
        return "";
    }

    /**
     * Constructs a new class definition with an auto generated protected
     * namespace.
     * 
     * @param name Base name of the class definition, eg: DisplayObject, not
     * flash.display.DisplayObject.
     * @param namespaceRef {@link INamespaceReference} qualifier. Usually this
     * will be a {@code INamespaceDefinition.IPublicNamespaceDefinition} whose
     * URI is a package name like flash.display.
     */
    public ClassDefinition(String name, INamespaceReference namespaceRef)
    {
        this(name, namespaceRef, NamespaceDefinition.createProtectedNamespaceDefinition(getGeneratedURIPrefix(namespaceRef) + name));
    }

    /**
     * Constructs a new class definition with an explicit protected namespace.
     * The protected namespace is used to qualify protected instance members of
     * the ABC class.
     * 
     * @param name Base name of the class definition, eg: DisplayObject, not
     * flash.display.DisplayObject.
     * @param namespaceRef {@link INamespaceReference} qualifier. Usually this
     * will be a {@code INamespaceDefinition.IPublicNamespaceDefinition} whose
     * URI is a package name like flash.display.
     * @param protectedNamespace Protected namespace that will be used to
     * qualifier protected instance members of the ABC class.
     */
    public ClassDefinition(String name, INamespaceReference namespaceRef,
                           NamespaceDefinition.IProtectedNamespaceDefinition protectedNamespace)
    {
        super(name);
        assert name.indexOf('.') == -1 : "name must not be a qualified name!";

        setNamespaceReference(namespaceRef);

        String generatedURI = getGeneratedURIPrefix(namespaceRef) + name;
        privateNamespaceReference =
                NamespaceDefinition.createPrivateNamespaceDefinition(generatedURI);
        protectedNamespaceReference = protectedNamespace;
        staticProtectedNamespaceReference =
                NamespaceDefinition.createStaticProtectedNamespaceDefinition(generatedURI);

        contingentDefinitions = new LinkedList<IDefinition>();

        eventDefinitions = new AtomicReference<Map<String, IEventDefinition>>();
        styleDefinitions = new AtomicReference<Map<String, IStyleDefinition>>();
        effectDefinitions = new AtomicReference<Map<String, IEffectDefinition>>();

        skinStates = new AtomicReference<String[]>();
        skinParts = new AtomicReference<IMetaTag[]>();

        stateNames = new HashSet<String>();

        frameInformation = new AtomicReference<FrameInformation>();

        remoteClassAlias = new AtomicReference<String>();
    }

    private final NamespaceDefinition.ILanguageNamespaceDefinition privateNamespaceReference;
    private final NamespaceDefinition.ILanguageNamespaceDefinition protectedNamespaceReference;
    private final NamespaceDefinition.ILanguageNamespaceDefinition staticProtectedNamespaceReference;

    // A reference to the class that this class extends.
    private IReference baseClassReference;

    // References to the interfaces that this class implements.
    private IReference[] interfaceReferences;

    // The definition for the constructor of this class.
    // This can be stored as a definition rather than as a reference
    // because the definition is within this class
    // rather than in a different class.
    private IFunctionDefinition constructor;

    // This map has keys which are event names (such as "click")
    // and values which are EventDefinitions.
    // It contains event definitions created from [Event(...)] metadata
    // on this class, but not from such metadata on superclasses.
    // It is lazily created so that if no one asks for the events of a class,
    // the EventDefinitions don't get created; for example, if we are simply
    // compiling ActionScript files, their event metadata is irrelevant.
    // We keep an atomic reference to it because multiple threads
    // need to be able to lazily update this ClassDefinition after it
    // is in a scope.
    private AtomicReference<Map<String, IEventDefinition>> eventDefinitions;

    // This map has keys which are style names (such as "fontSize")
    // and values which are StyleDefinitions.
    // It contains style definitions created from [Style(...)] metadata
    // on this class, but not from such metadata on superclasses.
    // It is lazily created so that if no one asks for the styles of a class,
    // the StyleDefinitions don't get created; for example, if we are simply
    // compiling ActionScript files, their style metadata is irrelevant.
    // We keep an atomic reference to it because multiple threads
    // need to be able to lazily update this ClassDefinition after it
    // is in a scope.
    private final AtomicReference<Map<String, IStyleDefinition>> styleDefinitions;

    private final AtomicReference<Map<String, IEffectDefinition>> effectDefinitions;

    private final AtomicReference<String[]> skinStates;

    private final AtomicReference<IMetaTag[]> skinParts;

    private Set<String> stateNames;

    private final AtomicReference<FrameInformation> frameInformation;

    // Remote class alias for this class.  Set from [RemoteClass] meta-data.
    private final AtomicReference<String> remoteClassAlias;

    /**
     * If this ClassDefinition is for an MXML class, this field is a set of
     * implicit imports determined by the MXML instance tags that are used
     * inside the class.
     */
    private Set<String> implicitImports;

    private List<IDefinition> contingentDefinitions;

    @Override
    public ClassClassification getClassClassification()
    {
        if (inPackageNamespace())
            return ClassClassification.PACKAGE_MEMBER;
        return ClassClassification.FILE_MEMBER;
    }

    @Override
    public IFunctionDefinition getConstructor()
    {
        return constructor;
    }

    protected void setConstructor(IFunctionDefinition constructor)
    {
        this.constructor = constructor;

        if(this.constructor.isPrivate()
                && getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_PRIVATE_CONSTRUCTOR) == null)
        {
            // ensures that the constructor remains private when compiled into
            // a library because the metadata is how private constructors are
            // stored in the bytecode
            MetaTag privateMetaTag = new MetaTag(this, IMetaAttributeConstants.ATTRIBUTE_PRIVATE_CONSTRUCTOR, new IMetaTagAttribute[0]);
            addMetaTag(privateMetaTag);
        }
    }

    @Override
    public String getBaseClassAsDisplayString()
    {
        return baseClassReference != null ? baseClassReference.getDisplayString() : "";
    }

    @Override
    public IReference getBaseClassReference()
    {
        return baseClassReference;
    }

    /**
     * Sets a reference to the base class for this class.
     * 
     * @param baseClassReference An {@link IReference} referring to the base
     * class.
     */
    public void setBaseClassReference(IReference baseClassReference)
    {
        if (baseClassReference == null)
        {
            // The baseClassReference of Object is null.
            // The baseClassReference of other classes is a reference to Object,
            // until setBaseClass() gets called (which it may never be
            // in the case of a class declaration without an extends clause).
            boolean isObject = getBaseName().equals(IASLanguageConstants.Object) &&
                               getNamespaceReference().isPublicOrInternalNamespace() &&
                               ((INamespaceDefinition)getNamespaceReference()).getURI().isEmpty();
            if (!isObject)
                baseClassReference = ReferenceFactory.builtinReference(BuiltinType.OBJECT);
        }

        this.baseClassReference = baseClassReference;
    }

    @Override
    public IClassDefinition resolveBaseClass(ICompilerProject project)
    {
        // Object has no base class.
        if (baseClassReference == null)
            return null;

        // Resolve the base class reference within the project,
        // and establish an inheritance dependency.
        ITypeDefinition typeDefinition =
                resolveType(baseClassReference, project, INHERITANCE);

        if (typeDefinition == null)
            return null;

        // A class cannot extend an interface.
        if (!(typeDefinition instanceof IClassDefinition))
            return null;

        // The base class cannot be final.
        if (((IClassDefinition)typeDefinition).isFinal())
            return null;

        // A class cannot extend itself.
        if (typeDefinition == this)
            return null;

        return (IClassDefinition)typeDefinition;
    }

    @Override
    public String[] getImplementedInterfacesAsDisplayStrings()
    {
        if (interfaceReferences == null)
            return new String[0];

        int n = interfaceReferences.length;
        String[] interfaces = new String[n];
        for (int i = 0; i < n; i++)
            interfaces[i] = interfaceReferences[i].getDisplayString();

        return interfaces;
    }

    public void setImplementedInterfaceReferences(IReference[] interfaceReferences)
    {
        this.interfaceReferences = interfaceReferences;
    }

    @Override
    public IReference[] getImplementedInterfaceReferences()
    {
        if (interfaceReferences == null)
            return new IReference[0];

        return interfaceReferences;
    }

    @Override
    public IEventDefinition getEventDefinition(IWorkspace w, String name)
    {
        if (eventDefinitions.get() == null)
            buildEventDefinitions(w);

        return eventDefinitions.get().get(name);
    }

    @Override
    public IEventDefinition[] getEventDefinitions(IWorkspace w)
    {
        if (eventDefinitions.get() == null)
            buildEventDefinitions(w);

        return eventDefinitions.get().values().toArray(new IEventDefinition[0]);
    }

    /*
     * Lazily creates a map of event names to event definitions from the event
     * metadata declared on this class.
     */
    private void buildEventDefinitions(IWorkspace w)
    {
        Map<String, IEventDefinition> map = new HashMap<String, IEventDefinition>();

        Map<String, IEventTagNode> availableTagNodes = new HashMap<String, IEventTagNode>();

        // required because of CMP-2168 : can't get the event definition from the metatag.
        if (getNode() != null && getNode().getMetaTags() != null)
        {
            IMetaTagNode[] allNodes = this.getNode().getMetaTags().getTagsByName(IMetaAttributeConstants.ATTRIBUTE_EVENT);
            //                        eventDefinition.setNode((IDefinitionNode)metaTag.getTagNode());
            for (IMetaTagNode iMetaTagNode : allNodes)
            {
                IEventTagNode eventTagNode = (IEventTagNode)iMetaTagNode;
                availableTagNodes.put(eventTagNode.getName(), (IEventTagNode)iMetaTagNode);
            }
        }
        
        IMetaTag[] metaTags = getAllMetaTags();
        if (metaTags != null)
        {
            for (IMetaTag metaTag : metaTags)
            {
                if (metaTag.getTagName().equals(IMetaAttributeConstants.ATTRIBUTE_EVENT))
                {
                    // Most event metadata looks like this:
                    // [Event(name="click", type="flash.events.MouseEvent")]
                    String name = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_EVENT_NAME);
                    String type = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_EVENT_TYPE);

                    // However,
                    // [Event(name="click")]
                    // and
                    // [Event("click")]
                    // are also allowed.
                    if (name == null)
                    {
                        IMetaTagAttribute[] attrs = metaTag.getAllAttributes();
                        if (attrs != null && attrs.length > 0)
                        {
                            name = attrs[0].getValue();
                        }
                    }
                    if (type == null)
                        type = ClassDefinition.Event;

                    if (name != null)
                    {
                        EventDefinition eventDefinition = new EventDefinition(name, this);

                        // This scope is necessary for resolving the event's type.
                        eventDefinition.setContainingScope(getContainingScope());

                        eventDefinition.setTypeReference(ReferenceFactory.packageQualifiedReference(w, type));

                        if (availableTagNodes.containsKey(name))
                        {
                            eventDefinition.setNode(availableTagNodes.get(name));
                        }
                        map.put(name, eventDefinition);
                    }
                }
            }
        }

        // Let the first thread that builds the map atomically set it.
        eventDefinitions.compareAndSet(null, map);
    }

    @Override
    public IEventDefinition[] findEventDefinitions(ICompilerProject project)
    {
        IWorkspace workspace = project.getWorkspace();

        Map<String, IEventDefinition> map = new HashMap<String, IEventDefinition>();

        // Iterate over this class and its superclasses.
        for (IClassDefinition c : classIterable(project, true))
        {
            for (IEventDefinition eventTag : c.getEventDefinitions(workspace))
            {
                String eventName = eventTag.getBaseName();

                // By checking whether the event is already in the map,
                // we can make sure that an event definition with a particular name
                // on a subclass overrides ones with the same name on superclasses.
                if (!map.containsKey(eventName))
                    map.put(eventName, eventTag);
            }
        }

        return map.values().toArray(new IEventDefinition[0]);
    }

    @Override
    public IStyleDefinition getStyleDefinition(IWorkspace workspace, String name)
    {
        if (styleDefinitions.get() == null)
            buildStyleDefinitions(workspace);

        return styleDefinitions.get().get(name);
    }

    @Override
    public IStyleDefinition[] getStyleDefinitions(IWorkspace w)
    {
        if (styleDefinitions.get() == null)
            buildStyleDefinitions(w);

        return styleDefinitions.get().values().toArray(new IStyleDefinition[0]);
    }

    /*
     * Lazily creates a map of style names to style definitions from the style
     * metadata declared on this class.
     */
    private void buildStyleDefinitions(IWorkspace workspace)
    {
        Map<String, IStyleDefinition> styleMap = new HashMap<String, IStyleDefinition>();

        for (IMetaTag metaTag : getAllMetaTags())
        {
            if (metaTag.getTagName().equals(IMetaAttributeConstants.ATTRIBUTE_STYLE))
            {
                String name = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_NAME);
                String type = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_TYPE);
                String format = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_FORMAT);
                String inherit = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_INHERIT);
                String themes = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_THEME);
                String minValue = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_MIN_VALUE);
                String minValueExclusive = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_MIN_VALUE_EXCLUSIVE);
                String maxValue = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_MAX_VALUE);
                String maxValueExclusive = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_MAX_VALUE_EXCLUSIVE);
                String enumeration = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_ENUMERATION);
                String states = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_STATES);
                String arrayTypeName = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_STYLE_ARRAYTYPE);

                if (name == null) // If there is no Name, we can't create a definition, nor should we try
                    continue;
                if (type == null) // missing type is perfectly legal. so let's patch
                                  // it up to something that will resolve correctly for us.
                    type = "*";

                StyleDefinition styleDefinition = new StyleDefinition(name, this);

                // This scope is necessary for resolving the style's type.
                styleDefinition.setContainingScope(getContainingScope());

                styleDefinition.setTypeReference(ReferenceFactory.packageQualifiedReference(workspace, type));
                styleDefinition.setFormat(format);
                styleDefinition.setInherit(inherit);
                styleDefinition.setThemes(themes);
                styleDefinition.setMinValue(minValue);
                styleDefinition.setMinValueExclusive(minValueExclusive);
                styleDefinition.setMaxValue(maxValue);
                styleDefinition.setMaxValueExclusive(maxValueExclusive);
                styleDefinition.setArrayType(arrayTypeName);
                if (states != null)
                {
                    final Iterable<String> statesIterable = Splitter.on(",")
                                    .omitEmptyStrings()
                                    .trimResults()
                                    .split(states);
                    final String[] statesArray = Iterables.toArray(statesIterable, String.class);
                    styleDefinition.setStates(statesArray);
                }
                if (enumeration != null)
                {
                    // enumerations are comma seprated, with no spaces
                    String[] split = enumeration.split(",");
                    styleDefinition.setEnumeration(split);
                }
                IMetaTagNode styleTagNode = metaTag.getTagNode();
                if (styleTagNode != null && styleTagNode instanceof IStyleTagNode)
                {
                    styleDefinition.setNode((IStyleTagNode)styleTagNode);
                }
                styleMap.put(name, styleDefinition);
            }
        }

        // Let the first thread that builds the map atomically set it.
        styleDefinitions.compareAndSet(null, styleMap);
    }

    @Override
    public IStyleDefinition[] findStyleDefinitions(ICompilerProject project)
    {
        IWorkspace workspace = project.getWorkspace();

        Map<String, IStyleDefinition> map = new HashMap<String, IStyleDefinition>();

        // Iterate over this class and its superclasses.
        for (IClassDefinition c : classIterable(project, true))
        {
            for (IStyleDefinition styleTag : c.getStyleDefinitions(workspace))
            {
                String styleName = styleTag.getBaseName();

                // By checking whether the style is already in the map,
                // we can make sure that a style definition with a particular name
                // on a subclass overrides ones with the same name on superclasses.
                if (!map.containsKey(styleName))
                    map.put(styleName, styleTag);
            }
        }

        return map.values().toArray(new IStyleDefinition[0]);
    }

    @Override
    public IEffectDefinition getEffectDefinition(IWorkspace w, String name)
    {
        if (effectDefinitions.get() == null)
            buildEffectDefinitions(w);

        return effectDefinitions.get().get(name);
    }

    @Override
    public IEffectDefinition[] getEffectDefinitions(IWorkspace w)
    {
        if (effectDefinitions.get() == null)
            buildEffectDefinitions(w);

        return effectDefinitions.get().values().toArray(new IEffectDefinition[0]);
    }

    /*
     * Lazily creates a map of effect names to effect definitions from the
     * effect metadata declared on this class.
     */
    private void buildEffectDefinitions(IWorkspace workspace)
    {
        Map<String, IEffectDefinition> effectMap = new HashMap<String, IEffectDefinition>();

        for (IMetaTag metaTag : getAllMetaTags())
        {
            if (metaTag.getTagName().equals(IMetaAttributeConstants.ATTRIBUTE_EFFECT))
            {
                String name = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_EFFECT_NAME);
                String eventName = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_EFFECT_EVENT);

                EffectDefinition effectDefinition = new EffectDefinition(name, this);
                effectDefinition.setEvent(eventName);
                // Set the effect definition containing scope, so that clients
                // can find which file scope contains the effect definition.
                effectDefinition.setContainingScope(getContainingScope());

                effectMap.put(name, effectDefinition);
            }
        }

        // Let the first thread that builds the map atomically set it.
        effectDefinitions.compareAndSet(null, effectMap);
    }

    @Override
    public IEffectDefinition[] findEffectDefinitions(ICompilerProject project)
    {
        IWorkspace workspace = project.getWorkspace();

        Map<String, IEffectDefinition> map = new HashMap<String, IEffectDefinition>();

        // Iterate over this class and its superclasses.
        for (IClassDefinition c : classIterable(project, true))
        {
            for (IEffectDefinition effectTag : c.getEffectDefinitions(workspace))
            {
                String effectName = effectTag.getBaseName();

                // By checking whether the effect is already in the map,
                // we can make sure that an effect definition with a particular name
                // on a subclass overrides ones with the same name on superclasses.
                if (!map.containsKey(effectName))
                    map.put(effectName, effectTag);
            }
        }

        return map.values().toArray(new IEffectDefinition[0]);
    }

    /*
     * Lazily creates a array of skin state names from the skin state metadata
     * declared on this class.
     */
    private void buildSkinStates(Collection<ICompilerProblem> problems)
    {
        IMetaTag[] tags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_SKIN_STATE);

        Set<String> states = new HashSet<String>();

        for (IMetaTag tag : tags)
        {
            IMetaTagAttribute[] attributes = tag.getAllAttributes();
            // old compiler only ever looked at first value, and ignored
            // anything else, so match that behavior here.
            if (attributes.length >= 1)
            {
                String state = attributes[0].getValue();
                if (states.contains(state))
                {
                    ICompilerProblem problem = new DuplicateSkinStateProblem(tag, state);
                    problems.add(problem);
                }
                states.add(state);
            }
        }

        // Let the first thread that builds the array atomically set it.
        skinStates.compareAndSet(null, states.toArray(new String[states.size()]));
    }

    @Override
    public String[] getSkinStates(Collection<ICompilerProblem> problems)
    {
        if (skinStates.get() == null)
            buildSkinStates(problems);

        return skinStates.get();
    }

    @Override
    public String[] findSkinStates(ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        Set<String> states = new HashSet<String>();

        // Iterate over the superclasses first, but don't worry about duplicates
        // yet. The superclasses will find their own duplicates!
        for (IClassDefinition c : classIterable(project, false))
        {
            for (String state : c.getSkinStates(problems))
            {
                states.add(state);
            }
        }
        // Then, add the states from this class and check for duplicates that
        // specifically come from this class
        for (String state : getSkinStates(problems))
        {
            if (states.contains(state))
            {
                ICompilerProblem problem = new DuplicateSkinStateProblem(this, state);
                problems.add(problem);
            }

            states.add(state);
        }

        return states.toArray(new String[states.size()]);
    }

    /**
     * Get any embedded data decorating the class definition
     * 
     * @param project Project to resolve against
     * @param problems Any problems resolving the embedded data
     * @return EmbedData or null if no embedded data, or a problem resolving
     * embedded data
     */
    public EmbedData getEmbeddedAsset(CompilerProject project, Collection<ICompilerProblem> problems)
    {
        IMetaTag[] embedMetaTags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_EMBED);
        if (embedMetaTags == null || embedMetaTags.length == 0)
            return null;

        if (embedMetaTags.length > 1)
        {
            problems.add(new EmbedMultipleMetaTagsProblem(getNode()));
            return null;
        }

        String containingSourceFilename = getFileSpecification().getPath();
        ISourceLocation location = embedMetaTags[0];
        IMetaTagAttribute[] attributes = embedMetaTags[0].getAllAttributes();

        return EmbedCompilationUnitFactory.getEmbedData(project, getQualifiedName(), containingSourceFilename, location, attributes, problems);
    }

    private Collection<VariableDefinition> getAllLocalVariables()
    {
        Collection<IDefinitionSet> allDefinitions = getContainedScope().getAllLocalDefinitionSets();
        List<VariableDefinition> variables = new LinkedList<VariableDefinition>();

        for (IDefinitionSet defSet : allDefinitions)
        {
            for (int i = 0; i < defSet.getSize(); ++i)
            {
                IDefinition definition = defSet.getDefinition(i);
                if (definition instanceof VariableDefinition)
                    variables.add((VariableDefinition)definition);
            }
        }

        return variables;
    }

    private Collection<GetterDefinition> getAllLocalGetters()
    {
        Collection<IDefinitionSet> allDefinitions = getContainedScope().getAllLocalDefinitionSets();
        List<GetterDefinition> getters = new LinkedList<GetterDefinition>();

        for (IDefinitionSet defSet : allDefinitions)
        {
            for (int i = 0; i < defSet.getSize(); ++i)
            {
                IDefinition definition = defSet.getDefinition(i);
                if (definition instanceof GetterDefinition)
                    getters.add((GetterDefinition)definition);
            }
        }

        return getters;
    }

    /*
     * Lazily creates a array of skin parts from the skin part metadata declared
     * on this class.
     */
    private void buildSkinParts(Collection<ICompilerProblem> problems)
    {
        Collection<VariableDefinition> variables = getAllLocalVariables();

        List<IMetaTag> parts = new LinkedList<IMetaTag>();
        for (VariableDefinition variable : variables)
        {
            IMetaTag skinPart = variable.getSkinPart();
            if (skinPart != null)
                parts.add(skinPart);
        }

        Collection<GetterDefinition> getters = getAllLocalGetters();
        for (GetterDefinition getter : getters)
        {
            IMetaTag skinPart = getter.getSkinPart();
            if (skinPart != null)
                parts.add(skinPart);
        }

        for (IMetaTag part : parts)
        {
            IDefinition definition = part.getDecoratedDefinition();
            if (!definition.getNamespaceReference().equals(NamespaceDefinition.getPublicNamespaceDefinition()))
            {
                ICompilerProblem problem = new SkinPartsMustBePublicProblem(definition);
                problems.add(problem);
            }
        }

        // Let the first thread that builds the array atomically set it.
        skinParts.compareAndSet(null, parts.toArray(new IMetaTag[parts.size()]));
    }

    @Override
    public IMetaTag[] getSkinParts(Collection<ICompilerProblem> problems)
    {
        if (skinParts.get() == null)
            buildSkinParts(problems);

        return skinParts.get();
    }

    @Override
    public IMetaTag[] findSkinParts(ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        Map<String, IMetaTag> map = new HashMap<String, IMetaTag>();

        // Iterate over this class and its superclasses.
        for (IClassDefinition c : classIterable(project, true))
        {
            for (IMetaTag skinPart : c.getSkinParts(problems))
            {
                String variableName = skinPart.getDecoratedDefinition().getBaseName();

                // By checking whether the variable is already in the map,
                // we can make sure that a variable definition with a particular name
                // on a subclass overrides ones with the same name on superclasses.
                if (!map.containsKey(variableName))
                    map.put(variableName, skinPart);
            }
        }

        return map.values().toArray(new IMetaTag[map.size()]);
    }

    /**
     * Add a state name to the class
     * 
     * @param stateName The name to add
     */
    public void addStateName(String stateName)
    {
        stateNames.add(stateName);
    }

    @Override
    public Set<String> getStateNames()
    {
        return stateNames;
    }

    @Override
    public Set<String> findStateNames(ICompilerProject project)
    {
        Set<String> set = new HashSet<String>();

        // Iterate over this class and its superclasses.
        for (IClassDefinition c : classIterable(project, true))
        {
            set.addAll(c.getStateNames());
        }

        return set;
    }

    public List<IDefinition> getContingentDefinitions()
    {
        return contingentDefinitions;
    }

    /**
     * Build any contingent definitions that may be required for the class such
     * as hostComponent. These definitions will only be resolved against or
     * codegend if a superclass does not implement the definition already. Note
     * that this should only be called when constructring classes derived from
     * AS or MXML. It is not needed when creating ABC sourced classes.
     */
    public void buildContingentDefinitions()
    {
        // for now, only host components need to be created
        IDefinition definition = buildHostComponentMember();
        if (definition != null)
            contingentDefinitions.add(definition);
    }

    /**
     * If there is host component meta data on the class, run semantic checks on
     * it.
     * 
     * @param project The compiler project.
     * @param problems The collection of compiler problems to which this method should add problems.
     */
    public void verifyHostComponent(CompilerProject project, Collection<ICompilerProblem> problems)
    {
        IMetaTag[] metaTags = getHostComponentMetaData();
        if (metaTags.length == 0)
            return;

        if (metaTags.length > 1)
        {
            ICompilerProblem problem = new OnlyOneHostComponentAllowedProblem(metaTags[1]);
            problems.add(problem);
        }

        String hostComponentClassName = getHostComponentClassName(metaTags[0]);
        if (hostComponentClassName == null)
        {
            ICompilerProblem problem = new HostComponentMustHaveTypeProblem(metaTags[0]);
            problems.add(problem);
            // without a host component name, there aren't any more checks we
            // can do, so just bail out.
            return;
        }

        IResolvedQualifiersReference hostComponentRef = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), hostComponentClassName);
        ICompilationUnit referencingCU = project.getScope().getCompilationUnitForDefinition(this);
        IDefinition hostComponentDef = hostComponentRef.resolve(project, referencingCU, SIGNATURE);
        if (!(hostComponentDef instanceof IClassDefinition))
        {
            ICompilerProblem problem = new HostComponentClassNotFoundProblem(metaTags[0], hostComponentClassName);
            problems.add(problem);
            // without a host component class definition, there aren't any more checks we
            // can do, so just bail out.
            return;
        }

        IClassDefinition hostComponentClassDef = (IClassDefinition)hostComponentDef;
        verifySkinParts(hostComponentClassDef, project, problems);
        verifySkinStates(hostComponentClassDef, project, problems);
    }

    private void verifySkinParts(IClassDefinition hostComponentDef, ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        IMetaTag[] skinParts = hostComponentDef.findSkinParts(project, problems);

        for (IMetaTag skinPart : skinParts)
        {
            IVariableDefinition hostSkinPartDef = (IVariableDefinition)skinPart.getDecoratedDefinition();

            String hostStringPartName = hostSkinPartDef.getBaseName();
            IDefinition skinPartDef = getContainedScope().getPropertyFromDef(project, this, hostStringPartName, false);
            if (skinPartDef instanceof ISetterDefinition)
                skinPartDef = ((ISetterDefinition)skinPartDef).resolveGetter(project);

            // the skinPart definition needs to be either a variable or getter
            if (!((skinPartDef instanceof IVariableDefinition) || (skinPartDef instanceof IGetterDefinition)))
                skinPartDef = null;

            ITypeDefinition skinPartTypeDef = null;
            if (skinPartDef != null)
                skinPartTypeDef = skinPartDef.resolveType(project);

            if (skinPartTypeDef == null && hostSkinPartDef.isRequiredSkinPart())
            {
                ICompilerProblem problem = new MissingSkinPartProblem(skinPart, hostComponentDef.getBaseName());
                problems.add(problem);
            }
            else if (skinPartTypeDef != null && !skinPartTypeDef.isInstanceOf(hostSkinPartDef.resolveType(project), project))
            {
                ICompilerProblem problem = new WrongSkinPartProblem(skinPart, skinPartTypeDef, hostSkinPartDef.resolveType(project));
                problems.add(problem);

            }
        }
    }

    private void verifySkinStates(IClassDefinition hostComponentDef, ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        Set<String> stateNames = findStateNames(project);

        String[] skinStates = hostComponentDef.findSkinStates(project, problems);
        for (String skinStateName : skinStates)
        {
            if (!stateNames.contains(skinStateName))
                problems.add(new MissingSkinStateProblem(getFileSpecification().getPath(), skinStateName));
        }
    }

    private VariableDefinition buildHostComponentMember()
    {
        // if there's no host component meta data, no need to create any contingent member
        IMetaTag[] metaTags = getHostComponentMetaData();
        if (metaTags.length == 0)
            return null;

        String hostComponentClassName = getHostComponentClassName(metaTags[0]);
        if (hostComponentClassName == null)
            return null;

        ASScope containedScope = getContainedScope();
        VariableDefinition hostComponentDef = new VariableDefinition(HOST_COMPONENT);
        hostComponentDef.setPublic();
        hostComponentDef.setBindable();
        hostComponentDef.setImplicit();
        hostComponentDef.setContingent();

        IReference typeRef = ReferenceFactory.packageQualifiedReference(containedScope.getWorkspace(), hostComponentClassName);
        hostComponentDef.setTypeReference(typeRef);

        containedScope.addDefinition(hostComponentDef);

        return hostComponentDef;
    }

    public VariableDefinition buildOuterDocumentMember(IReference outerClass)
    {
        ASScope containedScope = getContainedScope();
        VariableDefinition outerDocumentDef = new VariableDefinition(IMXMLLanguageConstants.PROPERTY_OUTER_DOCUMENT);
        outerDocumentDef.setPublic();
        outerDocumentDef.setBindable();
        outerDocumentDef.setImplicit();
        outerDocumentDef.setTypeReference(outerClass);

        containedScope.addDefinition(outerDocumentDef);

        // the outer document isn't really a contingent definition, as the user
        // should never define it, and we should always create one, but making
        // use of the contingent variable is useful as we can create it here
        // and then resolve it during codegen and create a problem if
        // we resolved to anything but the contingent definition outerDocument.
        outerDocumentDef.setContingent();
        contingentDefinitions.add(outerDocumentDef);

        return outerDocumentDef;
    }

    private IMetaTag[] getHostComponentMetaData()
    {
        return getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_HOST_COMPONENT);
    }

    private static String getHostComponentClassName(IMetaTag metaTag)
    {
        assert IMetaAttributeConstants.ATTRIBUTE_HOST_COMPONENT.equals(metaTag.getTagName());
        String hostComponentClassName = metaTag.getValue();
        // no component name, so don't create a contingent member
        if (hostComponentClassName == null || hostComponentClassName.isEmpty())
            return null;

        return hostComponentClassName;
    }

    private void buildFrameInformation(IWorkspace w)
    {
        FrameInformation result = new FrameInformation();
        IMetaTag[] frameTags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_FRAME);

        result.extraClasses = Collections.emptySet();
        for (IMetaTag frameTag : frameTags)
        {
            String frameTagFactoryClass = frameTag.getAttributeValue(IMetaAttributeConstants.NAME_FRAME_FACTORY_CLASS);
            if (frameTagFactoryClass != null)
                result.factoryClass = ReferenceFactory.packageQualifiedReference(w, QNameNormalization.normalize(frameTagFactoryClass));
            String frameTagExtraClass = frameTag.getAttributeValue(IMetaAttributeConstants.NAME_FRAME_EXTRA_CLASS);
            if (frameTagExtraClass != null)
            {
                if (result.extraClasses.size() == 0)
                    result.extraClasses = new LinkedList<IResolvedQualifiersReference>();
                result.extraClasses.add(ReferenceFactory.packageQualifiedReference(w, QNameNormalization.normalize(frameTagExtraClass)));
            }
        }
        frameInformation.compareAndSet(null, result);
    }

    private IResolvedQualifiersReference getFactoryClass(IWorkspace w)
    {
        if (frameInformation.get() == null)
            buildFrameInformation(w);
        return frameInformation.get().factoryClass;
    }

    public boolean hasOwnFactoryClass(IWorkspace w)
    {
        return getFactoryClass(w) != null;
    }

    private void buildRemoteClassAlias()
    {
        IMetaTag[] remoteClassTags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_REMOTECLASS);
        String remoteClassAlias = NO_REMOTE_CLASS_ALIAS;
        if (remoteClassTags.length != 0)
        {
            // Reading the source for the Flex 4.5.X compiler, it seems that
            // you can only have one alias for each class and that the last alias
            // wins.
            IMetaTag lastRemoteClassTag = remoteClassTags[remoteClassTags.length - 1];
            remoteClassAlias = lastRemoteClassTag.getAttributeValue(IMetaAttributeConstants.NAME_REMOTECLASS_ALIAS);
            // Goofy logic copied from flex2.compiler.as3.SyntaxTreeEvaluator.
            // Original code references a bug number 159983 in an unknown bug base.
            if (remoteClassAlias == null)
                remoteClassAlias = "<" + getQualifiedName();
        }

        this.remoteClassAlias.compareAndSet(null, remoteClassAlias);
    }

    /**
     * Get's the remote class alias for this class.
     * 
     * @return The remote class alias for this class.
     */
    public String getRemoteClassAlias()
    {
        if (remoteClassAlias.get() == null)
            buildRemoteClassAlias();
        String result = remoteClassAlias.get();
        if (result == NO_REMOTE_CLASS_ALIAS)
            return null;
        return result;
    }

    public ClassDefinition resolveInheritedFactoryClass(ICompilerProject project)
    {
        for (IClassDefinition c : classIterable(project, true))
        {
            assert c instanceof ClassDefinition : "IClassDefinition " + c.getBaseName() + "is not a ClassDefinition!";
            ClassDefinition classDef = (ClassDefinition)c;
            IResolvedQualifiersReference factoryRef = classDef.getFactoryClass(project.getWorkspace());
            if (factoryRef != null)
            {
                IDefinition factoryDef = factoryRef.resolve(project, (ASScope)this.getContainingScope(), EXPRESSION, true);
                if (factoryDef instanceof ClassDefinition)
                    return (ClassDefinition)factoryDef;
            }
        }
        return null;
    }

    public Collection<IDefinition> resolveExtraClasses(ICompilerProject project)
    {
        if (frameInformation.get() == null)
            buildFrameInformation(project.getWorkspace());
        LinkedList<IDefinition> result = new LinkedList<IDefinition>();
        for (IResolvedQualifiersReference extraClassRef : frameInformation.get().extraClasses)
        {
            IDefinition extraClassDef =
                    extraClassRef.resolve(project, (ASScope)this.getContainingScope(), EXPRESSION, true);
            if (extraClassDef != null)
                result.add(extraClassDef);
        }
        return ImmutableList.<IDefinition> copyOf(result);
    }

    @Override
    public String getDefaultPropertyName(ICompilerProject project)
    {
        // Look for [DefaultProperty("foo")] on this class its superclasses.
        for (IClassDefinition c : classIterable(project, true))
        {
            IMetaTag defaultPropertyMetaData = c.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_DEFAULTPROPERTY);
            if (defaultPropertyMetaData != null)
            {
                IMetaTagAttribute attrs[] = defaultPropertyMetaData.getAllAttributes();
                if ((attrs.length == 1) && (!attrs[0].hasKey()))
                    return attrs[0].getValue();
            }
        }
        return null;
    }

    @Override
    public void setContainedScope(IASScope value)
    {
        super.setContainedScope(value);
        ((DefinitionBase)privateNamespaceReference).setContainingScope(value);
    }

    /**
     * Gets the {@link INamespaceReference} that resolves to the private
     * namespace for this {@link IClassDefinition}.
     * 
     * @return The {@link INamespaceReference} that resolves to the private
     * namespace for this {@link IClassDefinition}.
     */
    public NamespaceDefinition.ILanguageNamespaceDefinition getPrivateNamespaceReference()
    {
        return privateNamespaceReference;
    }

    /**
     * Gets the {@link INamespaceReference} that resolves to the protected
     * namespace for this {@link IClassDefinition}.
     * 
     * @return The {@link INamespaceReference} that resolves to the protected
     * namespace for this {@link IClassDefinition}.
     */
    @Override
    public NamespaceDefinition.ILanguageNamespaceDefinition getProtectedNamespaceReference()
    {
        return protectedNamespaceReference;
    }

    /**
     * Gets the {@link INamespaceReference} that resolves to the static
     * protected namespace for this {@link IClassDefinition}.
     * 
     * @return The {@link INamespaceReference} that resolves to the static
     * protected namespace for this {@link IClassDefinition}.
     */
    @Override
    public NamespaceDefinition.ILanguageNamespaceDefinition getStaticProtectedNamespaceReference()
    {
        return staticProtectedNamespaceReference;
    }

    @Override
    public Name getMName(ICompilerProject project)
    {
        // "*" isn't referenced by name in ABC - it's usually
        // just index 0, which is what AET does for a null name.
        if (getBaseName() == IASLanguageConstants.ANY_TYPE)
            return null;

        return super.getMName(project);
    }

    @Override
    public ASFileScope getFileScope()
    {
        if (isImplicit())
            return null;
        else
            return super.getFileScope();
    }

    /**
     * Adds a new node to the list of nodes representing the implicit imports
     * created by MXML tags.
     */
    public void addImplicitImport(String qname)
    {
        if (implicitImports == null)
            implicitImports = new HashSet<String>();

        implicitImports.add(qname);
    }

    /**
     * Gets the implicit imports created by MXML tags, for use by CodeModel.
     * 
     * @return A list of {@code MXMLImplicitImportNode} objects.
     */
    public String[] getImplicitImports()
    {
        return implicitImports != null ?
                implicitImports.toArray(new String[0]) :
                new String[0];
    }

    /**
     * Adds implicit variable definitions for "this" and "super" to this class
     * definition's contained scope. These variable definitions are for code
     * model compatibility and are not codegen'd.
     */
    public void setupThisAndSuper()
    {
        // Create an implicit VariableDefinition for "this".
        VariableDefinition thisDef = new VariableDefinition(IASKeywordConstants.THIS);

        ASScope containedScope = getContainedScope();
        IWorkspace workspace = containedScope.getWorkspace();

        // this is a lexical ref for codemodel backwards compat
        thisDef.setTypeReference(ReferenceFactory.lexicalReference(workspace, getBaseName()));
        thisDef.setImplicit();
        thisDef.setNamespaceReference(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());

        // Create an implicit VariableDefinition for "super". 
        VariableDefinition superDef = new VariableDefinition(IASKeywordConstants.SUPER);
        IReference baseClassRef = getBaseClassReference();
        if (baseClassRef == null)
        {
            baseClassRef = ReferenceFactory.builtinReference(BuiltinType.OBJECT);
        }
        superDef.setTypeReference(baseClassRef);
        superDef.setImplicit();
        superDef.setNamespaceReference(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());

        // Add these definitions to the class scope.
        containedScope.addDefinition(thisDef);
        containedScope.addDefinition(superDef);
    }

    /**
     * Mark this class as an excluded class by adding [ExcludeClass] meta data.
     */
    public void setExcludedClass()
    {
        MetaTag excludeClassMetaTag = new MetaTag(this, IMetaAttributeConstants.ATTRIBUTE_EXCLUDECLASS, new IMetaTagAttribute[0]);
        addMetaTag(excludeClassMetaTag);
    }


    /**
     * Mark this class as being generated with mxml bindings [RoyaleBindings] meta data.
     */
    public void setRoyaleBindings()
    {
        if (!hasMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_BINDINGS)) {
            MetaTag bindingsMarker = new MetaTag(this, IMetaAttributeConstants.ATTRIBUTE_BINDINGS, new IMetaTagAttribute[0]);
            addMetaTag(bindingsMarker);
        }
    }

    /**
     * For debugging only. Produces a string such as
     * <code>public class B extends A implements I1, I2</code>.
     */
    @Override
    protected void buildInnerString(StringBuilder sb)
    {
        sb.append(getNamespaceReferenceAsString());
        sb.append(' ');

        sb.append(IASKeywordConstants.CLASS);
        sb.append(' ');

        sb.append(getBaseName());
        sb.append(' ');

        if (baseClassReference != null)
        {
            sb.append(IASKeywordConstants.EXTENDS);
            sb.append(' ');

            String baseClassName = getBaseClassAsDisplayString();
            sb.append(baseClassName.isEmpty() ? IASLanguageConstants.Object : baseClassName);
        }

        String[] implementedInterfaces = getImplementedInterfacesAsDisplayStrings();
        int n = implementedInterfaces.length;
        if (n > 0)
        {
            sb.append(' ');
            sb.append(IASKeywordConstants.IMPLEMENTS);
            sb.append(' ');

            for (int i = 0; i < n; i++)
            {
                sb.append(implementedInterfaces[i]);
                if (i < n - 1)
                {
                    sb.append(',');
                    sb.append(' ');
                }
            }
        }
    }

    public boolean getOwnNeedsProtected()
    {
        return ((TypeScope)getContainedScope()).getNeedsProtected();
    }

    @Override
    public IClassDefinition resolveHostComponent(ICompilerProject project)
    {
        for (IClassDefinition c : classIterable(project, true))
        {
            if (!(c instanceof ClassDefinition))
                continue;
            
            ClassDefinition classDef = (ClassDefinition)c;
            IMetaTag[] hostComponentMetaData = classDef.getHostComponentMetaData();
            if (hostComponentMetaData.length < 1)
                continue;
            String hostComponentName = getHostComponentClassName(hostComponentMetaData[0]);
            if (hostComponentName == null)
                return null;
            IResolvedQualifiersReference hostComponentRef = ReferenceFactory.packageQualifiedReference(project.getWorkspace(), hostComponentName);
            IDefinition hostComponentDef = hostComponentRef.resolve(project);
            if (!(hostComponentDef instanceof IClassDefinition))
                return null;
            return (IClassDefinition)hostComponentDef;
        }
        return null;
    }

    @Override
    public boolean isInProject(ICompilerProject project)
    {
        if (!isImplicit())
            return super.isInProject(project);
        // The implicit definitions are shared amoungst all projects.
        if (this == NULL)
            return true;
        if (this == VOID)
            return true;
        if (this == ANY_TYPE)
            return true;
        if (this == UNDEFINED)
            return true;
        // Some implicit class definition we have never seen before.
        return false;
    }

    /**
     * Method to find all the abstract methods declared in this class, and
     * validate that the concrete class definition passed in implements those
     * methods, and that they are implemented with compatible signatures
     * 
     * @param cls the class definition to check
     * @param problems a list of problems to report errors to
     */
    public void validateClassImplementsAllMethods(ICompilerProject project, ClassDefinition cls, Collection<ICompilerProblem> problems)
    {
        ASScope classScope = cls.getContainedScope();

        for (IDefinitionSet defSet : this.getContainedScope().getAllLocalDefinitionSets())
        {
            for (int i = 0, l = defSet.getSize(); i < l; ++i)
            {
                IDefinition def = defSet.getDefinition(i);
                if (def instanceof FunctionDefinition && !(def instanceof IAccessorDefinition))
                {
                    FunctionDefinition abstractMethod = (FunctionDefinition)def;

                    // Skip any implicit methods added for CM compat
                    if (abstractMethod.isImplicit())
                        continue;

                    // Skip the constructor method of the interface.
                    if (abstractMethod.getBaseName().equals(getBaseName()))
                        continue;

                    // Skip methods that are static
                    if (abstractMethod.isStatic())
                        continue;

                    // Skip methods that aren't abstract
                    if (!abstractMethod.isAbstract())
                        continue;

                    INamespaceDefinition ns = abstractMethod.resolveNamespace(project);
                    if(ns instanceof INamespaceDefinition.IProtectedNamespaceDefinition)
                    {
                        ns = cls.getProtectedNamespaceReference();
                    }

                    IDefinition c = classScope.getQualifiedPropertyFromDef(project, cls, abstractMethod.getBaseName(), ns, false);
                    if (c == null || c.isAbstract())
                    {
                        // Error, didn't implement the method
                        problems.add(new UnimplementedAbstractMethodProblem(cls,
                                abstractMethod.getBaseName(),
                                this.getBaseName(),
                                cls.getBaseName()));
                    }
                }
            }
        }
    }
}
