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

import static org.apache.royale.compiler.common.ISourceLocation.UNKNOWN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.common.NodeReference;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IDocumentableDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.definitions.metadata.IDeprecationInfo;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.INamespaceResolvedReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.common.Counter;
import org.apache.royale.compiler.internal.definitions.metadata.DeprecationInfo;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.ASScopeBase;
import org.apache.royale.compiler.internal.scopes.SWCFileScopeProvider.SWCFileScope;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * This class is the abstract base class for all ActionScript definitions in the
 * symbol table.
 * <p>
 * After definitions are added to the symbol table, they should always be
 * accessed through the read-only definition interfaces (which are
 * <code>IDefinition</code> and its subinterfaces) rather than through these
 * implementation classes.
 * <p>
 * Note that this class also implements {@link IDefinitionSet} so that a
 * scope's map can point to a single definition acting as it own
 * definition-set-of-size-1.
 */
public abstract class DefinitionBase implements IDocumentableDefinition, IDefinitionSet
{
    // Mask constants for bitflags in the 'flags' field,
    // a short value that we use in place of multiple boolean fields.
    protected static final short FLAG_CAST_FUNCTION = 1 << 0;
    protected static final short FLAG_CONSTRUCTOR = 1 << 1;
    protected static final short FLAG_DYNAMIC = 1 << 2;
    protected static final short FLAG_FINAL = 1 << 3;
    protected static final short FLAG_IMPLICIT = 1 << 4;
    protected static final short FLAG_NATIVE = 1 << 5;
    protected static final short FLAG_OVERRIDE = 1 << 6;
    protected static final short FLAG_REST = 1 << 7;
    protected static final short FLAG_STATIC = 1 << 8;
    protected static final short FLAG_DEFAULT = 1 << 9;
    protected static final short FLAG_CONTINGENT = 1 << 10;
    protected static final short FLAG_GENERATED_EMBED_CLASS = 1 << 11;
    protected static final short FLAG_HAS_INIT = 1 << 12;
    protected static final short FLAG_DEPRECATED = 1 << 13;
    protected static final short FLAG_DECLARED_IN_CONTROL_FLOW = 1 << 14;

    private static boolean performanceCachingEnabled = false;

    public static boolean getPerformanceCachingEnabled()
    {
        return performanceCachingEnabled;
    }

    public static void setPerformanceCachingEnabled(boolean enable)
    {
        performanceCachingEnabled = enable;
    }

    /**
     * Constructor.
     * 
     * @param name The "constructor name" of the definition.
     * This is currently in an inconsistent form; for example,
     * for the <code>flash.display.Sprite</code> class definition
     * constructed from source code it is <code>"Sprite"</code>
     * but for the same definition constructed from ABC it is
     * <code>"flash.display.Sprite"</code>.
     * The "constructor name" will be converted to a "storage name"
     * and stored in the <code>storageName</code> field.
     */
    // TODO Fix construction of definitions so that
    // the storage name is always passed in.
    public DefinitionBase(String name)
    {
        assert name != null;
        this.storageName = toStorageName(name);
        
        if (Counter.COUNT_DEFINITIONS)
            countDefinitions();
    }

    // The scope that contains this definition.
    // This field is set when the addDefinition() method of ASScope
    // calls the package-private setContainingScope() method of this class.
    private IASScope containingScope;

    // Bit flags for this definition, such as whether it is implicit.
    protected short flags;

    // The namespace reference for this definition.
    // For most definitions this will be non-null, even if no namespace was specified
    // in the source code. For example, if the namespace is omitted on a member of a class,
    // "internal" is implied; therefore this field will be a reference to the internal
    // namespace for the package containing the class. However, some types of definitions,
    // such as a for a function parameter, do have a null namespace reference.
    private INamespaceResolvedReference namespaceReference;

    // The name stored for this definition. See getStorageName() for details.
    private final String storageName;

    // The type reference for this definition. See getTypeReference() for details.
    // Note that this is an IReference, which is basically a reference-by-name
    // to a definition, which can resolve to different definitions in different projects.
    private IReference typeReference;

    // metaTags is never null. As an optimization, it re-uses a singleton empty array when
    // it has nothing in it
    private IMetaTag[] metaTags = singletonEmptyMetaTags;

    protected static final IMetaTag[] singletonEmptyMetaTags = new IMetaTag[0];

    // Hold a reference to the node this definition came from
    // (NodeReference only holds onto the node weakly, so we don't have to worry about leaks).
    protected NodeReference nodeRef = NodeReference.noReference;

    private int absoluteNameStart = 0;
    private int absoluteNameEnd = 0;
    
    private IDefinition parentDef = null;

    /**
     * Called by {@code MXMLScopeBuilder} when building definitions from
     * {@code MXMLData}. Creates a nodeless {@code NodeReference} for this
     * definition that keeps track of the file and offset for the MXML tag that
     * produced this definition. Later, after the MXML tree is produced, this
     * {@code NodeReference} can be used to locate the appropriate tree node.
     * 
     * @param tag The {@code MXMLTagData} that is producing this definition.
     */
    public void setLocation(IMXMLTagData tag)
    {
        nodeRef = new NodeReference(tag.getSource(), tag.getAbsoluteStart());
    }

    /**
     * Attaches a weak reference to the source node that produced this
     * definition, and copies over enough source-location information to
     * re-associate the node if it is garbage-collected.
     * 
     * @param node The {@link IDefinitionNode} that is producing
     * this definition.
     */
    public void setNode(IDefinitionNode node)
    {
        if (node == null)
        {
            nodeRef = NodeReference.noReference;

            absoluteNameStart = UNKNOWN;
            absoluteNameEnd = UNKNOWN;
        }
        else
        {
            nodeRef = new NodeReference(node);

            IExpressionNode nameNode = node.getNameExpressionNode();
            if (nameNode != null)
            {
                absoluteNameStart = nameNode.getAbsoluteStart();
                absoluteNameEnd = nameNode.getAbsoluteEnd();
            }
            else
            {
                absoluteNameStart = UNKNOWN;
                absoluteNameEnd = UNKNOWN;
            }
        }
    }

    /**
     * This method is used to set the name location when there is no name node
     * from which it can be determined.
     */
    public void setNameLocation(int absoluteNameStart, int absoluteNameEnd)
    {
        this.absoluteNameStart = absoluteNameStart;
        this.absoluteNameEnd = absoluteNameEnd;
    }

    @Override
    public IASScope getContainingScope()
    {
        return containingScope;
    }

    public void setContainingScope(IASScope scope)
    {
        containingScope = scope;
    }

    @Override
    public IDefinition getParent()
    {
    	if (getPerformanceCachingEnabled() && parentDef != null)
    		return parentDef;
    	
        IASScope scope = getContainingScope();

        // Walk up the scope chain until we find a scope that has
        // a containing definition.  The following types of scope do
        // not have an immediate containing definition:
        // CatchScope's
        // WithScope's
        // FunctionScope's for MXML event handler's.
        while ((scope != null) && (scope.getDefinition() == null))
            scope = scope.getContainingScope();

        IDefinition result = scope != null ? scope.getDefinition() : null;
        if (getPerformanceCachingEnabled())
            parentDef = result;
        return result;
    }

    @Override
    public IDefinition getAncestorOfType(Class<? extends IDefinition> ancestorType)
    {
        IDefinition definition = getParent();
        while (definition != null && !(ancestorType.isInstance(definition)))
            definition = definition.getParent();
        return definition;
    }
    
    // TODO Eliminate this method and its overrides
    // by constructing the definition with its storage name.
    protected String toStorageName(String name)
    {
        // Most constructors pass in either a base name or a qname,
        // which we convert to a base name.
        // However, this method is overridin in DefinitionPromise,
        // PackageDefinition, and AppliedVectorDefinition to NOT
        // reduce the constructor name to a base name.
        int i = name.lastIndexOf('.');
        return i == -1 ? name : name.substring(i + 1);
    }
    
    /**
     *  Gets the name that is actually stored for this definition.
     *  <p>
     *  This is not a public API, and is not in the {@link IDefinition}
     *  interface, because different types of definitions store different
     *  types of names. However, the storage name of a definition does
     *  not depend on whether it came from source code or from ABC.
     *  <p>
     *  For class, interface, function, variable, constant, and namespace
     *  definitions -- whose base name and the qualified name can differ
     *  when the definition is at package scope -- the storage name is the
     *  base name (e.g., <code>"Sprite"</code>,
     *  not <code>"flash.display.Sprite"</code>).
     *  <p>
     *  In the case of vector types, the storage name is a base name
     *  like <code>"Vector.&lt;Sprite&gt;"</code>
     *  (not <code>"Vector.&lt;flash.display.Sprite&gt;"</code>),
     *  which is considered a base name despite technically having a dot.
     *  <p>
     *  For parameter definitions -- which cannot be at package scope -
     *  and for event, style, and effect definitions -- which as metadata
     *  don't live in any scope -- there is no distinction between the base
     *  name and the qualified name; the storage name is the same as both
     *  of these. E.g., <code>i</code> for a parameter definition or
     *  <code>"click"</code> for an event definition.
     *  <p>
     *  For package definitions, the storage name is the package name
     *  (e.g., <code>"flash.display"</code>), which is considered
     *  both the base name and the qualified name of a package definition.
     *  <p>
     *  Finally, for definition promises, the storage name is a dotted,
     *  fully-qualified name (e.g., <code>"flash.display.Sprite"</code>,
     *  not <code>"Sprite"</code>).
     */
    protected final String getStorageName()
    {
        return storageName;
    }

    /**
     */
    public IFileSpecification getFileSpecification()
    {
        return nodeRef.getFileSpecification();
    }

    @Override
    public String getSourcePath()
    {
        IFileSpecification fileSpec = getFileSpecification();
        return fileSpec != null ? fileSpec.getPath() : null;
    }

    private OffsetLookup getOffsetLookup()
    {
        final ASFileScope fileScope = getFileScope();
        if (fileScope == null)
            return null;

        return fileScope.getOffsetLookup();
    }

    @Override
    public int getStart()
    {
        // Because the starting offset is what is used
        // connect a node to a definition,
        // we can't just call getStart() on getNode();
        // because this leads to deadlock.
        // Instead, we have to use the starting offset
        // stored in the NodeReference.

        if (nodeRef == null)
            return UNKNOWN;

        final int absoluteStart = nodeRef.getAbsoluteStart();
        if (absoluteStart == UNKNOWN)
            return UNKNOWN;

        final OffsetLookup offsetLookup = getOffsetLookup();
        if (offsetLookup == null)
            return absoluteStart;

        return offsetLookup.getLocalOffset(absoluteStart);
    }

    @Override
    public int getEnd()
    {
        final IDefinitionNode node = getNode();
        if (node == null)
            return UNKNOWN;

        return node.getEnd();
    }

    @Override
    public int getLine()
    {
        final IDefinitionNode node = getNode();
        if (node == null)
            return UNKNOWN;

        return node.getLine();
    }

    @Override
    public int getColumn()
    {
        final IDefinitionNode node = getNode();
        if (node == null)
            return UNKNOWN;

        return node.getColumn();
    }

    @Override
    public int getAbsoluteStart()
    {
        // Because the starting offset is what is used
        // connect a node to a definition,
        // we can't just call getAbsoluteStart() on getNode();
        // because this leads to deadlock.
        // Instead, we have to use the starting offset
        // stored in the NodeReference.

        if (nodeRef == null)
            return UNKNOWN;

        return nodeRef.getAbsoluteStart();
    }

    @Override
    public int getAbsoluteEnd()
    {
        final IDefinitionNode node = getNode();
        if (node == null)
            return UNKNOWN;

        return node.getAbsoluteEnd();
    }

    private IExpressionNode getNameNode()
    {
        IDefinitionNode node = getNode();
        if (node == null)
            return null;

        return node.getNameExpressionNode();
    }

    @Override
    public int getNameStart()
    {
        if (absoluteNameStart == UNKNOWN)
            return UNKNOWN;

        OffsetLookup offsetLookup = getOffsetLookup();
        if (offsetLookup == null)
            return absoluteNameStart;

        return offsetLookup.getLocalOffset(absoluteNameStart);
    }

    @Override
    public int getNameEnd()
    {
        if (absoluteNameEnd == UNKNOWN)
            return UNKNOWN;

        OffsetLookup offsetLookup = getOffsetLookup();
        if (offsetLookup == null)
            return absoluteNameEnd;

        return offsetLookup.getLocalOffset(absoluteNameEnd);
    }

    /**
     * Get line number for the name of this definition, if it can be determined.
     * 
     * @return the line number, or -1 if we couldn't figure it out
     */
    @Override
    public int getNameLine()
    {
        final IExpressionNode nameNode = getNameNode();
        if (nameNode == null)
            return UNKNOWN;

        return nameNode.getLine();
    }

    /**
     * Get column number for the name of this definition, if it can be
     * determined.
     * 
     * @return the column number, or -1 if we couldn't figure it out
     */
    @Override
    public int getNameColumn()
    {
        final IExpressionNode nameNode = getNameNode();
        if (nameNode == null)
            return UNKNOWN;

        return nameNode.getColumn();
    }

    @Override
    public String getContainingFilePath()
    {
        ASFileScope fileScope = getFileScope();
        if (fileScope == null)
            return null;
        return fileScope.getContainingPath();
    }

    @Override
    public String getContainingSourceFilePath(final ICompilerProject project)
    {
        // If node reference is set, find the source file name from
        // offset lookup
        if (nodeRef != NodeReference.noReference)
        {
            final ASFileScope fileScope = getFileScope();
            if (fileScope != null && fileScope.getOffsetLookup() != null)
            {
                return fileScope.getOffsetLookup().getFilename(getAbsoluteStart());
            }
        }

        ASScope containingScope = (ASScope)getContainingScope();
        if (containingScope != null)
            return containingScope.getContainingSourcePath(getQualifiedName(), project);
        return null;
    }

    private static String namespaceReferenceToPackageName(INamespaceReference nsRef)
    {
        if (nsRef instanceof NamespaceDefinition.INamespaceWithPackageName)
            return ((NamespaceDefinition.INamespaceWithPackageName)nsRef).getNamespacePackageName();
        return null;
    }

    /**
     * 
     * @param definition is the definition whose containing top level definition we want
     * @return the top level deinition, or null if none exists.
     */
    private static DefinitionBase getContainingToplevelDefinition(DefinitionBase definition)
    {
        ASScope currentContainingScope = definition.getContainingASScope();
        if (currentContainingScope == null)
        {
            // With some synthetic definitions you can't find a containint top level definition.
            // This happens with Vector<T>, for example. In this case, return null
            return null;
        }
        DefinitionBase currentDefinition = definition;

        IScopedDefinition containingDefinition = currentContainingScope.getContainingDefinition();
        while (containingDefinition != null)
        {
            currentDefinition = (DefinitionBase)containingDefinition;
            currentContainingScope = currentDefinition.getContainingASScope();
            
            if (currentContainingScope == null)
            {
                // See comment above
                return null;
            }
            
            containingDefinition = currentContainingScope.getContainingDefinition();
        }
        assert currentDefinition != null;
        return currentDefinition;
    }

    @Override
    public String getPackageName()
    {
        // Ugghhh!!  This method is supposed to return the fully qualified name
        // of the package that contains this definition.  The method works even
        // if this definition is not a top level definition.

        // Sub-classes that will never have a containing scope ( like DefinitionPromise's )
        // should overload this method.
        assert getContainingScope() != null : "This method should not be called until we have a containing scope.!";

        // If we get here, we have a containing scope, so we can just walk the scope
        // chain until we get to a definition that is a direct child of a package or file
        // scope.

        DefinitionBase containingToplevelDefinition = getContainingToplevelDefinition(this);
        if (containingToplevelDefinition == null)
        {
            // If there is no containing top level definition they return the top level package.
            // In the case of member functions of the Vector<T> class, this will be correct
            return "";
        }

        assert (!(containingToplevelDefinition instanceof FunctionDefinition)) || (!((FunctionDefinition)containingToplevelDefinition).isConstructor()) : "Constructors should always be contained by class definitions!";

        INamespaceReference toplevelDefinitionNSRef =
                containingToplevelDefinition.getNamespaceReference();

        assert toplevelDefinitionNSRef != null;
        String packageNameFromNSRef = namespaceReferenceToPackageName(toplevelDefinitionNSRef);
        if (packageNameFromNSRef != null)
            return packageNameFromNSRef;

        assert getBaseName().indexOf('.') == -1;
        return "";
    }

    @Override
    public String getBaseName()
    {
        // Most definitions store their base name, so this base method
        // simply returns it. However, a few subclasses override this.
        return storageName;
    }

    @Override
    public String getQualifiedName()
    {
        // For top-level definitions, report the package name concatenated
        // with the base name. If it's not in a package (e.g. a file private
        // definition), then the package name will be "".
        if (isTopLevelDefinition())
        {
            String packageName = getPackageName();
            if (packageName != null && !packageName.isEmpty())
                return packageName + "." + storageName;
        }
        
        return storageName;
    }

    /**
     * Is this definition a toplevel definition
     * 
     * @return true if this definition is declared at file scope, or package
     * scope.
     */
    public boolean isTopLevelDefinition()
    {
        return getParent() instanceof IPackageDefinition || getParent() == null;
    }

    @Override
    public boolean isDynamic()
    {
        return (flags & FLAG_DYNAMIC) != 0;
    }

    public void setDynamic()
    {
        flags |= FLAG_DYNAMIC;
    }

    @Override
    public boolean isFinal()
    {
        return (flags & FLAG_FINAL) != 0;
    }

    public void setFinal()
    {
        flags |= FLAG_FINAL;
    }

    @Override
    public boolean isNative()
    {
        return (flags & FLAG_NATIVE) != 0;
    }

    public void setNative()
    {
        flags |= FLAG_NATIVE;
    }

    @Override
    public boolean isOverride()
    {
        return (flags & FLAG_OVERRIDE) != 0;
    }

    public void setOverride()
    {
        flags |= FLAG_OVERRIDE;
    }

    public void unsetOverride()
    {
        if (isOverride())
            flags -= FLAG_OVERRIDE;
    }

    @Override
    public boolean isStatic()
    {
        return (flags & FLAG_STATIC) != 0;
    }

    public void setStatic()
    {
        flags |= FLAG_STATIC;
    }

    // instead of increasing the size of "flags" from a short to int, I added
    // a boolean variable instead. feel free to change this, if desired. -JT
    private boolean abstractFlag = false;

    @Override
    public boolean isAbstract()
    {
        return abstractFlag;
    }

    public void setAbstract()
    {
        abstractFlag = true;
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        // Note: These get checked in decreasing order of frequency of use.

        if (modifier == ASModifier.OVERRIDE)
        {
            return (flags & FLAG_OVERRIDE) != 0;
        }
        else if (modifier == ASModifier.STATIC)
        {
            return (flags & FLAG_STATIC) != 0;
        }
        else if (modifier == ASModifier.FINAL)
        {
            return (flags & FLAG_FINAL) != 0;
        }
        else if (modifier == ASModifier.DYNAMIC)
        {
            return (flags & FLAG_DYNAMIC) != 0;
        }
        else if (modifier == ASModifier.NATIVE)
        {
            return (flags & FLAG_NATIVE) != 0;
        }
        else if (modifier == ASModifier.VIRTUAL)
        {
            // Ignore "virtual" modifier.
            return false;
        }
        else if (modifier == ASModifier.ABSTRACT)
        {
            return abstractFlag;
        }
        else
        {
            assert false : "Unknown modifier: " + modifier;
            return false;
        }
    }

    @Override
    public ModifiersSet getModifiers()
    {
        ModifiersSet result = new ModifiersSet();
        if ((flags & FLAG_OVERRIDE) != 0)
            result.addModifier(ASModifier.OVERRIDE);
        if ((flags & FLAG_STATIC) != 0)
            result.addModifier(ASModifier.STATIC);
        if ((flags & FLAG_FINAL) != 0)
            result.addModifier(ASModifier.FINAL);
        if ((flags & FLAG_DYNAMIC) != 0)
            result.addModifier(ASModifier.DYNAMIC);
        if ((flags & FLAG_NATIVE) != 0)
            result.addModifier(ASModifier.NATIVE);
        if (abstractFlag)
            result.addModifier(ASModifier.ABSTRACT);
        return result;
    }

    public void setModifier(ASModifier modifier)
    {
        // Note: These get checked in decreasing order of frequency of use.

        if (modifier == ASModifier.OVERRIDE)
            flags |= FLAG_OVERRIDE;

        else if (modifier == ASModifier.STATIC)
            flags |= FLAG_STATIC;

        else if (modifier == ASModifier.FINAL)
            flags |= FLAG_FINAL;

        else if (modifier == ASModifier.DYNAMIC)
            flags |= FLAG_DYNAMIC;

        else if (modifier == ASModifier.NATIVE)
            flags |= FLAG_NATIVE;

        else if (modifier == ASModifier.ABSTRACT)
            setAbstract();

        else
            assert false;
    }

    @Override
    public boolean isPublic()
    {
        return namespaceReference instanceof INamespaceDefinition.IPublicNamespaceDefinition;
    }

    @Override
    public boolean isPrivate()
    {
        return namespaceReference instanceof INamespaceDefinition.IPrivateNamespaceDefinition;
    }

    @Override
    public boolean isProtected()
    {
        return namespaceReference instanceof INamespaceDefinition.IProtectedNamespaceDefinition
                || namespaceReference instanceof INamespaceDefinition.IStaticProtectedNamespaceDefinition;
    }

    @Override
    public boolean isInternal()
    {
        return namespaceReference instanceof INamespaceDefinition.IInternalNamespaceDefinition;
    }

    @Override
    public boolean hasNamespace(INamespaceReference namespace, ICompilerProject project)
    {
        // If the namespaces being compared are language namespaces
        // (which have singleton NamespaceReferences)
        // then we can just compare the NamespaceReferences.
        if (namespace.isLanguageNamespace() &&
            this.namespaceReference.isLanguageNamespace())
        {
            return namespace == this.namespaceReference;
        }

        // Otherwise, the namespaces are equivalent if they resolve to
        // equivalent namespaceReference definitions (i.e., two with the same URI).
        INamespaceDefinition ns1 = namespace.resolveNamespaceReference(project);
        INamespaceDefinition ns2 = this.namespaceReference.resolveNamespaceReference(project);
        return ((ns1 != null && ns2 != null) ? ns1.equals(ns2) : false);
    }

    @Override
    public INamespaceReference getNamespaceReference()
    {
        return namespaceReference;
    }

    public void setNamespaceReference(INamespaceReference value)
    {
        namespaceReference = (INamespaceResolvedReference)value;

        NamespaceDefinition.setContainingDefinitionOfReference(value, this);
    }

    public void setPublic()
    {
        setNamespaceReference(NamespaceDefinition.getPublicNamespaceDefinition());
    }

    /**
     * Utility to mark a definition as bindable. This method should only ever be
     * called during construction or initialization of a definition.
     */
    public void setBindable()
    {
        MetaTag bindableMetaTag = new MetaTag(this, IMetaAttributeConstants.ATTRIBUTE_BINDABLE, new IMetaTagAttribute[0]);
        addMetaTag(bindableMetaTag);
    }

    @Override
    public INamespaceDefinition resolveNamespace(ICompilerProject project)
    {
        return namespaceReference != null ? namespaceReference.resolveNamespaceReference(project) : null;
    }

    @Override
    public String getTypeAsDisplayString()
    {
        return typeReference != null ? typeReference.getDisplayString() : "";
    }

    @Override
    public IReference getTypeReference()
    {
        return typeReference;
    }

    /**
     * Sets the type reference for this definition.
     * 
     * @param typeReference An [@Link IReference} to a class or interface.
     */
    public void setTypeReference(IReference typeReference)
    {
        this.typeReference = typeReference;
    }

    /**
     * Gets the {@link DependencyType} that should be used when resolving the
     * type of this definition.
     * <p>
     * This method is intended to be overridden by sub-classes.
     * 
     * @return The {@link DependencyType} that should be used when resolving the
     * type of this variable definition
     */
    protected DependencyType getTypeDependencyType()
    {
        return DependencyType.SIGNATURE;
    }

    @Override
    public TypeDefinitionBase resolveType(ICompilerProject project)
    {
        DependencyType dt = getTypeDependencyType();
        return resolveType(typeReference, project, dt);
    }

    @Override
    public boolean isImplicit()
    {
        return (flags & FLAG_IMPLICIT) != 0;
    }

    public void setImplicit()
    {
        flags |= FLAG_IMPLICIT;
    }

    @Override
    public IMetaTag[] getAllMetaTags()
    {
        return metaTags;
    }

    protected void addMetaTag(IMetaTag metaTag)
    {
        IMetaTag[] newMetaTags = new IMetaTag[metaTags.length + 1];
        System.arraycopy(metaTags, 0, newMetaTags, 0, metaTags.length);
        newMetaTags[metaTags.length] = metaTag;
        setMetaTags(newMetaTags);
    }

    public void setMetaTags(IMetaTag[] newMetaTags)
    {
        if (newMetaTags == null)
        {
            metaTags = singletonEmptyMetaTags;
        }
        else
        {
            this.metaTags = newMetaTags;
            
            // Use a flag bit to keep track of whether there is a [Deprecated] tag.
            for (IMetaTag metaTag : metaTags)
            {
                String tagName = metaTag.getTagName();
                if (tagName.equals(IMetaAttributeConstants.ATTRIBUTE_DEPRECATED))
                    setDeprecated();
            }
        }
    }

    @Override
    public IMetaTag[] getMetaTagsByName(String name)
    {
        // We should never return null from this function - callers do not expect it.
        // So allocate an empty array here, even if metaTags is null
        List<IMetaTag> list = new ArrayList<IMetaTag>();

        for (IMetaTag tag : metaTags)
        {
            if (tag.getTagName().equals(name))
                list.add(tag);
        }

        return list.toArray(new IMetaTag[0]);
    }

    @Override
    public boolean hasMetaTagByName(String name)
    {
        return getMetaTagByName(name) != null;
    }

    @Override
    public IMetaTag getMetaTagByName(String name)
    {
        for (IMetaTag tag : metaTags)
        {
            if (tag.getTagName().equals(name))
                return tag;
        }
        return null;
    }

    @Override
    public IASDocComment getExplicitSourceComment()
    {
        IDefinitionNode node = getNode();
        if (node instanceof IDocumentableDefinitionNode)
            return ((IDocumentableDefinitionNode)node).getASDocComment();
        return null;
    }

    @Override
    public boolean hasExplicitComment()
    {
        IDefinitionNode node = getNode();

        if (node instanceof IDocumentableDefinitionNode)
            return ((IDocumentableDefinitionNode)node).hasExplicitComment();

        return false;
    }

    public ASFileScope getFileScope()
    {
        ASScope s = getContainingASScope();
        if (s == null)
            return null;
        return s.getFileScope();
    }

    @Override
    public IDefinitionNode getNode()
    {
        // If this definition didn't come from source, return null.
        if (nodeRef == NodeReference.noReference)
            return null;

        ASScope containingScope = getContainingASScope();
        if (containingScope == null)
            return null;

        ASFileScope fileScope = containingScope.getFileScope();
        if (fileScope == null)
            return null;

        IASNode node = nodeRef.getNode(fileScope.getWorkspace(), getContainingASScope());
        if (!(node instanceof IDefinitionNode))
            return null;

        return (IDefinitionNode)node;
    }

    /**
     * Static version of {@code resolveType()} to help sub-classes implement
     * their overrides of {@link #resolveType(String, ICompilerProject, DependencyType)}.
     * 
     * @param context {@link DefinitionBase} relative to which the resolve should
     * be done.
     */
    protected static TypeDefinitionBase resolveType(DefinitionBase context, String typeName, ICompilerProject project, DependencyType dt)
    {
        // TODO - should probably return the Definition for "*" once we have an easy way to get that
        // TODO - definition - this works for now to prevent NPEs
        if (typeName == null)
            return null;

        CompilerProject compilerProject = (CompilerProject)project;
        ASScope containingScope = (ASScope)(context.containingScope);
        // TODO at some point this method should take some sort of name object.
        if (containingScope != null)
        {
            int lastIndexOfDot = typeName.lastIndexOf('.');
            IDefinition foundDefinition = null;
            if (lastIndexOfDot != -1)
            {
                String unqualifiedName = typeName.substring(lastIndexOfDot + 1);
                String packageName = typeName.substring(0, lastIndexOfDot);
                INamespaceDefinition packageNS = ((CompilerProject)project).getWorkspace().getPackageNamespaceDefinitionCache().get(packageName, false);
                foundDefinition = containingScope.findPropertyQualified(compilerProject, packageNS, unqualifiedName, dt, true);
            }
            else
            {
                foundDefinition = containingScope.findProperty(compilerProject, typeName, dt, true);
            }

            assert (foundDefinition == null) || foundDefinition.isInProject(project);
            if (foundDefinition instanceof TypeDefinitionBase)
                return (TypeDefinitionBase)foundDefinition;
        }

        return null;
    }

    protected static TypeDefinitionBase resolveType(DefinitionBase context, IReference typeRef, ICompilerProject project, DependencyType dt)
    {
        // No type means '*'
        if (typeRef == null)
            return (TypeDefinitionBase)project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE);

        IDefinition foundDefinition = typeRef.resolve(project, (ASScope)context.containingScope, dt, true);
        assert (foundDefinition == null) || foundDefinition.isInProject(project);
        return foundDefinition instanceof TypeDefinitionBase ? (TypeDefinitionBase)foundDefinition : null;
    }

    /**
     * This helper method resolves type references in definitions (i.e., Strings
     * such as "int", "Sprite", or "flash.events.IEventDispatcher" that are
     * stored as references to a class or interface) to the definition of the
     * specified type.
     * <p>
     * It is called by
     * <ul>
     * <li><code>resolveType()</code> in DefinitionBase to resolve a type
     * annotation such as the "int" on a declaration like
     * <code>public var foo:int</code>;</li>
     * <li><code>resolveReturnType()</code> in FunctionDefinition to resolve the
     * return type of a function declaration;</li>
     * <li><code>resolveBaseClass()</code> in ClassDefinition to resolve
     * <code>extends</code> clause of a class declaration;</li>
     * <li><code>resolveImplementedInterfaces()</code> in ClassDefinition to
     * resolve the <code>implements</code> clause of a class declaration;</li>
     * <li><code>resolveTypeParameters()</code> in ParameterizedClassDefinition
     * to resolve the type of a Vector;</li>
     * <li><code>resolveExtendedInterfaces()</code> in InterfaceDefinition to
     * resolve the <code>extends</code> clause of an interface declaration.</li>
     * </ul>
     * 
     * @param typeName The name of the type to resolve.
     * @param project The compiler project.
     * @param dt The type of dependency to be created.
     * @return The definition of the resolved type.
     */
    protected TypeDefinitionBase resolveType(String typeName, ICompilerProject project, DependencyType dt)
    {
        return resolveType(this, typeName, project, dt);
    }

    public TypeDefinitionBase resolveType(IReference typeRef, ICompilerProject project, DependencyType dt)
    {
        return resolveType(this, typeRef, project, dt);
    }

    protected String getLocationString()
    {
        StringBuilder sb = new StringBuilder();

        int start = getStart();
        int end = getEnd();
        if (start != -1 && end != -1)
        {
            sb.append(start);
            sb.append('-');
            sb.append(end);
        }

        sb.append(' ');

        String path = getContainingFilePath();
        if (path != null)
            sb.append(path);

        return sb.toString();
    }

    /**
     * Generate an AET Name object for this definition, and log any Problems
     * encountered while constructing the Name, to the list of problems passed
     * in.
     * 
     * @param project the Project to use when resolving the Definitions
     * Namespace
     * @return An AET Name object that represents the qualified name of this
     * definition. Will return null if errors are encountered while constructing
     * the name (such as, the Namespace of the Definition is unresolved).
     */
    public Name getMName(ICompilerProject project)
    {
        Name name = null;

        Namespace qual = null;
        if (namespaceReference != null)
        {
            qual = namespaceReference.resolveAETNamespace(project);
        }
        else
        {
            assert false : "All definitions should have a namespaceReference qualifier.";
            ASScope scope = getContainingASScope();
            if (scope != null)
                qual = ((NamespaceDefinition)NamespaceDefinition.getDefaultNamespaceDefinition(scope)).getAETNamespace();
        }

        // if we can't figure out the namespaceReference, we can't generate a valid name
        if (qual != null)
            name = new Name(ABCConstants.CONSTANT_Qname, new Nsset(qual), getBaseName());
        return name;
    }

    protected final ASScope getContainingASScope()
    {
        IASScope s = getContainingScope();
        ASScope scope = s instanceof ASScope ? (ASScope)s : null;
        return scope;
    }

    protected String getNamespaceReferenceAsString()
    {
        INamespaceReference namespaceReference = getNamespaceReference();
        return namespaceReference != null ?
                namespaceReference.toString() :
                INamespaceConstants.internal_;
    }

    /**
     * 
     * @return false if and only if we can reject the match due to one or the other or both
     * being vectors.
     */
    private boolean checkVectorMatch(DefinitionBase node)
    {
        
        
        IASScope thisScope = getContainingScope();
        IASScope nodeScope = node.getContainingScope();
        if (thisScope != null && nodeScope != null)
            
        {
            IScopedDefinition thisDef = thisScope.getDefinition();
            IScopedDefinition nodeDef = nodeScope.getDefinition();
            
            if (thisDef instanceof AppliedVectorDefinition && nodeDef instanceof AppliedVectorDefinition)
            {
                String thisElementQName = ((AppliedVectorDefinition)thisDef).getQualifiedName();
                String nodeElementQName = ((AppliedVectorDefinition)nodeDef).getQualifiedName();
                if (thisElementQName==null && nodeElementQName==null)
                    return true;            // I don't know if this can happen, but it does then they "match"
            
                if (thisElementQName == null)
                    return false;           

                return thisElementQName.equals(nodeElementQName);
            }
        }
        
        return true;        // we could not reject this match due to vector specific issues
    }
    public boolean matches(DefinitionBase node)
    {
        if (node == null)
            return false;

        if (node.getClass() != getClass())
            return false;

        if (node == this)
            return true;

        INamespaceReference namespace = getNamespaceReference();
        if (namespace == null && node.getNamespaceReference() != null)
            return false;
        if (namespace != null && namespace.getBaseName().compareTo(node.getNamespaceReference().getBaseName()) != 0)
        {
            return false;
        }

        if (node.getBaseName().compareTo(getBaseName()) != 0)
            return false;
        
        if (!checkVectorMatch(node))
            return false;
        String packageName = node.getPackageName();
        String packageName2 = getPackageName();
        if (packageName == null && packageName2 != null)
            return false;

        if (packageName != null && packageName2 != null && packageName.compareTo(packageName2) != 0)
            return false;

        ASScope leftScope = node.getContainingASScope();
        if (leftScope != null)
        {
            leftScope = leftScope.getFileScope();
        }
        ASScope rightScope = getContainingASScope();
        if (rightScope != null)
        {
            rightScope = rightScope.getFileScope();
        }

        if (leftScope instanceof SWCFileScope || rightScope instanceof SWCFileScope)
        {
            return true; //we can't verify path because we might be a definition from a library
        }
        else if (leftScope instanceof ASFileScope && rightScope instanceof ASFileScope)
        {
            if (((ASFileScope)leftScope).getContainingPath().compareTo(((ASFileScope)rightScope).getContainingPath()) != 0)
            {
                return false;
            }
        }
        else
        {
            //For normal case, just compare the full containing file path
            //Note that for some synthetic definitions (like Vector), this path will be null
            String nodePath = node.getContainingFilePath();
            String thisPath = this.getContainingFilePath();
            
            if (nodePath == null)
            {
                if (thisPath != null) return false;
            }
            else
                if (nodePath.compareTo(thisPath) != 0)
                    return false;
        }
        if (node.getTypeAsDisplayString().compareTo(getTypeAsDisplayString()) != 0)
            return false;

        return true;
    }

    /**
     * Whether this definition was specified as "Bindable" in its metadata
     */
    private boolean isBindableLocally()
    {
        return getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_BINDABLE) != null;
    }

    /**
     * Whether this definition was specified as "Bindable" in its metadata, or
     * inherits bindability from the containing class
     * 
     * @return true if this definition is Bindable.
     */
    @Override
    public boolean isBindable()
    {
        // A variable is considered bindable if it is marked bindable,
        // or if it is a public var in a class marked bindable
        boolean bindable = isBindableLocally();
        if (!bindable)
        {
           bindable = isClassBindable();
        }
        return bindable;
    }

    public boolean isClassBindable() {
        boolean classBindable = false;
        IDefinition containing = getParent();
        if (containing instanceof IClassDefinition
                && this.getNamespaceReference() == NamespaceDefinition.getPublicNamespaceDefinition())
        {
            DefinitionBase containingBase = (DefinitionBase)containing;
            classBindable = containingBase.isBindableLocally();
        }
        return classBindable;
    }

    /**
     * @return a collection of Bindable event names, if any were specified in
     * the metadata, as well as any inherited from the class
     */
    @Override
    public List<String> getBindableEventNames()
    {
        // First get any from this definition
        // Use a Set<> temporarily, so we can do an easy union
        Set<String> eventNames = new HashSet<String>(getBindableEventNamesLocally());

        // Now "or" in any from the containing class
        IDefinition containing = getParent();
        if (containing instanceof IClassDefinition
                && this.getNamespaceReference() == NamespaceDefinition.getPublicNamespaceDefinition())
        {
            DefinitionBase containingBase = (DefinitionBase)containing;
            eventNames.addAll(containingBase.getBindableEventNamesLocally());
        }

        // Convert to list, as per the return type in IDefinition
        return new ArrayList<String>(eventNames);
    }

    /**
     * @return a collection of Bindable event names, if any were specified in
     * the metadata
     */
    private List<String> getBindableEventNamesLocally()
    {
        IMetaTag[] bindableTags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_BINDABLE);

        if (bindableTags != null)
        {
            List<String> events = new ArrayList<String>();
            for (IMetaTag bindableTag : bindableTags)
            {
                String eventName = bindableTag.getAttributeValue(IMetaAttributeConstants.NAME_BINDABLE_EVENT);
                if (eventName == null && bindableTag.getAllAttributes().length == 1)
                {
                    Boolean isStyle = false;
                    
                    eventName = bindableTag.getAllAttributes()[0].getKey();
                    if (eventName != null && eventName.equals("style"))
                        isStyle = true;
                    eventName = bindableTag.getAllAttributes()[0].getValue();
                    if (isStyle && eventName.equals("true"))
                        eventName = "isStyle"; // hack: fake event name "isStyle"
                }
                if (eventName != null)
                    events.add(eventName);
            }
            return events;
        }
        return Collections.emptyList();
    }

    /**
     * @return a collection of Bindable event names, if any were specified in
     * the metadata
     */
    @Override
    public boolean isBindableStyle()
    {
        IMetaTag[] bindableTags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_BINDABLE);

        if (bindableTags != null)
        {
            for (IMetaTag bindableTag : bindableTags)
            {
                String style = bindableTag.getAttributeValue(IMetaAttributeConstants.NAME_BINDABLE_STYLE);
                if (style != null && IASLanguageConstants.TRUE.equals(style))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isContingent()
    {
        return (flags & FLAG_CONTINGENT) != 0;
    }

    /**
     * Marks the definition as contingent. Meaning that it's added to the symbol
     * table as a placeholder, but codegen will decide whether or not it needs
     * to actually be created.
     */
    protected void setContingent()
    {
        flags |= FLAG_CONTINGENT;
    }

    @Override
    public boolean isGeneratedEmbedClass()
    {
        return (flags & FLAG_GENERATED_EMBED_CLASS) != 0;
    }

    /**
     * Marks a class definition as being auto-generated for an embedded asset
     * (i.e., one created for a <code>var</code> with <code>[Embed(...)]</code>
     * metadata).
     * <p>
     * Adding such a class to the project scope in the middle of a compilation
     * will not cause the <code>validImports</code> set to be rebuilt.
     */
    public void setGeneratedEmbedClass()
    {
        flags |= FLAG_GENERATED_EMBED_CLASS;
    }

    @Override
    public boolean isContingentNeeded(ICompilerProject project)
    {
        return ((ASScopeBase)getContainingScope()).isContingentDefinitionNeeded(project, this);
    }

    protected IMetaTag getSkinPart()
    {
        return getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_SKIN_PART);
    }

    protected boolean isRequiredSkinPart(IMetaTag skinPart)
    {
        // if the required value is not set, it's not required, as skin parts are
        // optional by default.
        String isRequired = skinPart.getAttributeValue(IMetaAttributeConstants.NAME_SKIN_PART_REQUIRED);
        if (isRequired == null)
            return false;

        if (!isRequired.equals(IMetaAttributeConstants.VALUE_SKIN_PART_REQUIRED_TRUE))
            return false;

        return true;
    }

    /**
     * Returns the type specified by <code>[ArrayElementType("...")]</code>
     * metadata on a variable/getter/setter of type Array. </p> This method is
     * in DefinitionBase because it is common to VariableDefinition,
     * GetterDefinition, and SetterDefinition.
     */
    public String getArrayElementType(ICompilerProject project)
    {
        if (getTypeAsDisplayString().equals(IASLanguageConstants.Array)
                || IASLanguageConstants.Array.equals(getInstanceType(project)))
        {
            return getPropertyMetaTagValue(
                    (RoyaleProject)project, IMetaAttributeConstants.ATTRIBUTE_ARRAYELEMENTTYPE);
        }

        return null;
    }

    /**
     * Returns the type specified by <code>[InstanceType("...")]</code> metadata
     * on a variable/getter/setter of type mx.core.IDeferredInstance.
     * <p>
     * This method is in DefinitionBase because it is common to
     * VariableDefinition, GetterDefinition, and SetterDefinition.
     */
    public String getInstanceType(ICompilerProject project)
    {
        if (getTypeAsDisplayString().equals(((RoyaleProject)project).getDeferredInstanceInterface()))
        {
            return getPropertyMetaTagValue(
                    (RoyaleProject)project, IMetaAttributeConstants.ATTRIBUTE_INSTANCETYPE);
        }

        return null;
    }

    /**
     * Returns the property name specified by <code>[PercentProxy("...")]</code>
     * metadata on a variable/getter/setter. </p> This method is in
     * DefinitionBase because it is common to VariableDefinition,
     * GetterDefinition, and SetterDefinition.
     */
    public String getPercentProxy(ICompilerProject project)
    {
        IMetaTag metaTag = getPropertyMetaTag(
                (RoyaleProject)project, IMetaAttributeConstants.ATTRIBUTE_PERCENT_PROXY);

        return metaTag != null ? metaTag.getValue() : null;
    }

    /**
     * Returns <code>true</code> if there is <code>[RichTextContent]</code>
     * metadata on a variable/getter/setter.
     * <p>
     * This method is in DefinitionBase because it is common to
     * VariableDefinition, GetterDefinition, and SetterDefinition.
     */
    public boolean hasRichTextContent(ICompilerProject project)
    {
        IMetaTag metaTag = getPropertyMetaTag(
                (RoyaleProject)project, IMetaAttributeConstants.ATTRIBUTE_RICHTEXTCONTENT);

        return metaTag != null;
    }

    /**
     * Returns <code>true</code> if there is <code>[CollapseWhiteSpace]</code>
     * metadata on a variable/getter/setter.
     * <p>
     * This method is in DefinitionBase because it is common to
     * VariableDefinition, GetterDefinition, and SetterDefinition.
     */
    public boolean hasCollapseWhiteSpace(ICompilerProject project)
    {
        IMetaTag metaTag = getPropertyMetaTag(
                (RoyaleProject)project, IMetaAttributeConstants.ATTRIBUTE_COLLAPSEWHITESPACE);

        return metaTag != null;
    }

    /**
     * Returns <code>true</code> if there is <code>[Inspectable(...)]</code>
     * metadata on a vairable/getter/setter that specifies
     * <code>format="Color"</code>.
     * <p>
     * This method is in DefinitionBase because it is common to
     * VariableDefinition, GetterDefinition, and SetterDefinition.
     */
    public boolean isColor(ICompilerProject project)
    {
        IMetaTag metaTag = getPropertyMetaTag(
                (RoyaleProject)project, IMetaAttributeConstants.ATTRIBUTE_INSPECTABLE);

        if (metaTag == null)
            return false;

        String format = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_INSPECTABLE_FORMAT);
        return format.equals(IMetaAttributeConstants.VALUE_INSPECTABLE_FORMAT_COLOR);
    }

    private IMetaTag getPropertyMetaTag(RoyaleProject project, String metadataName)
    {
        // Look for metadata with the specified name.
        IMetaTag metaTag = getMetaTagByName(metadataName);

        // If we don't find it, and we're a getter or setter,
        // look on the corresponding setter or getter.
        if (metaTag == null && this instanceof IAccessorDefinition)
        {
            IAccessorDefinition correspondingAccessor =
                    ((IAccessorDefinition)this).resolveCorrespondingAccessor(project);
            if (correspondingAccessor != null)
                metaTag = correspondingAccessor.getMetaTagByName(metadataName);
        }

        return metaTag;
    }

    /**
     * This helper method is called by {@code getArrayElementType()} and
     * {@code getInstanceType()}.
     */
    private String getPropertyMetaTagValue(RoyaleProject project, String metadataName)
    {
        // Look for metadata with the specified name.
        IMetaTag metaTag = getPropertyMetaTag(project, metadataName);

        // If we found the metadata, return the type that it specifies.
        // There should be one keyless attribute, and we want its value.
        return metaTag != null ? metaTag.getValue() : null;
    }

    /**
     * Determines if this definition is in a package namespaceReference. This is here, so
     * that the various sub-classes can call it from get*Classification()
     * methods.
     * 
     * @return true, if the namespaceReference for this definition is some kind of
     * package namespaceReference.
     */
    protected boolean inPackageNamespace()
    {
        INamespaceReference nsRef = getNamespaceReference();
        assert nsRef != null : "All definitions should have a namespaceReference reference!";
        if (nsRef.isPublicOrInternalNamespace())
            return true;
        return false;
    }
    
    @Override
    public boolean isDeprecated()
    {
        return (flags & FLAG_DEPRECATED) != 0;
    }
    
    /**
     * Sets a bitflag to indicate that this definition has [Deprecated] metadata.
     * Checking this flag is faster than walking through the metadata multiple times.
     */
    private void setDeprecated()
    {
        flags |= FLAG_DEPRECATED;
    }

    @Override
    public IDeprecationInfo getDeprecationInfo()
    {
        for (IMetaTag metaTag : metaTags)
        {
            if (metaTag.getTagName().equals(IMetaAttributeConstants.ATTRIBUTE_DEPRECATED))
            {
                String replacement = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_DEPRECATED_REPLACEMENT);
                String since = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_DEPRECATED_SINCE);
                String message = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_DEPRECATED_MESSAGE);
                return new DeprecationInfo(replacement, since, message);
            }
        }

        return null;
    }

    /**
     * Used only in asserts.
     */
    public boolean verify()
    {
        // Verify the name.
        assert getBaseName() != null : "Definition has null name";

        // TODO: Verify the source location eventually.
        //      assert getSourcePath() != null : "Definition has null source path";
        //      assert getStart() != UNKNOWN : "Definition has unknown start";
        //      assert getEnd() != UNKNOWN : "Definition has unknown end";
        //      assert getLine() != UNKNOWN : "Definition has unknown line";
        //      assert getColumn() != UNKNOWN : "Definition has unknown column";

        return true;
    }

    /**
     * Used only for debugging.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        buildString(sb, false);
        return sb.toString();
    }

    /**
     * Used only for debugging, as part of {@link #toString()}.
     */
    public void buildString(StringBuilder sb, boolean withLocation)
    {
        buildInnerString(sb);

        if (withLocation)
        {
            sb.append(' ');
            sb.append(getLocationString());
        }
    }

    /**
     * Used only for debugging, as part of {@link #toString()}.
     */
    protected void buildInnerString(StringBuilder sb)
    {
    }
    
    /**
     * Counts various types of definitions that are created,
     * as well as the total number of definitions.
     */
    private void countDefinitions()
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("DefinitionBase incrementing Counter for " + getClass().getSimpleName());
        Counter counter = Counter.getInstance();
        counter.incrementCount(getClass().getSimpleName());
        counter.incrementCount("definitions");
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("DefinitionBase done incrementing Counter for " + getClass().getSimpleName());
    }
    
    //
    // IDefinitionSet methods
    //
    
    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    @Override
    public int getMaxSize()
    {
        return 1;
    }

    @Override
    public IDefinition getDefinition(int i)
    {
        assert i == 0;
        return this;
    }

    @Override
    public boolean isInProject(ICompilerProject project)
    {
        final ICompilationUnit compilationUnit = ((ASProjectScope)project.getScope()).getCompilationUnitForDefinition(this);
        if (compilationUnit != null)
        {
            if (compilationUnit.getProject() == project)
                return true;
            else
                return false;
        }
        if (AppliedVectorDefinition.isVectorScope(getContainingASScope()))
        {
            IDefinition parentDef = getParent();
            return parentDef.isInProject(project);
        }
        return false;
    }
}
