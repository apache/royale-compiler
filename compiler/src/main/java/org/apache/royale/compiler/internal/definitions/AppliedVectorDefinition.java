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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IEffectDefinition;
import org.apache.royale.compiler.definitions.IEventDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IStyleDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IAppliedVectorDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.TypedExpressionNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Definition representing a Vectorized type. It is built from a specific type,
 * and gains the properties and functions of Array.
 */
public final class AppliedVectorDefinition extends ClassDefinitionBase implements IAppliedVectorDefinition
{
    /**
     * A Name representing the base __AS3__.vec.Vector class.
     */
    private static final Name VECTOR_NAME;

    static
    {
        Nsset nsSet = new Nsset(new Namespace(ABCConstants.CONSTANT_PackageNs, IASLanguageConstants.Vector_impl_package));
        VECTOR_NAME = new Name(ABCConstants.CONSTANT_Qname, nsSet, IASLanguageConstants.Vector);
    }

    /*
     * Builds a string of the form "Vector.<Sprite>" or "Vector.<flash.display.Sprite>",
     * depending on the useFullyQualifiedNotation flag.
     */
    private static String getName(ITypeDefinition elementType, boolean useFullyQualifiedNotation)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(IASLanguageConstants.Vector);
        builder.append(".<");
        if (useFullyQualifiedNotation)
            builder.append(elementType.getQualifiedName());
        else
            builder.append(elementType.getBaseName());
        builder.append(">");
        return builder.toString();
    }

    /**
     * Helper method to copy a parameter - will use the name from the info
     * argument if provided, otherwise the name will be whatever the original
     * parameters name was - this lets us give more descriptive names to some of
     * the Vector parameters than they have in the .abc file.
     */
    private static ParameterDefinition copyParameter(ICompilerProject project, ParameterDefinition orig, VectorInformation.ArgumentInfo info)
    {
        ParameterDefinition newParam = new ParameterDefinition(info != null ? info.getName() : orig.getBaseName());
        newParam.setNamespaceReference(orig.getNamespaceReference());
        newParam.setTypeReference(orig.getTypeReference());
        if (orig.isRest())
            newParam.setRest();
        if (orig.hasDefaultValue())
            newParam.setDefaultValue(orig.resolveDefaultValue(project));
        return newParam;
    }

    /**
     * Constructor.
     * <p>
     * Don't call this constructor from anywhere other than the ASProjectScope.
     * 
     * @param project The compiler project
     * @param baseClass The base class for the vector type, such as {@code Vector$int}.
     * @param elementType The definition for the element type.
     */
    public AppliedVectorDefinition(ICompilerProject project, IClassDefinition baseClass,
                                   ITypeDefinition elementType)
    {
        // Set the 'storageName' field to a String like "Vector.<Sprite>".
        super(AppliedVectorDefinition.getName(elementType, false));
       
        this.project = project;
        this.baseClass = baseClass;
        this.elementType = elementType;
        
        setContainedScope(new TypeScope(null, this));
        setModifier(ASModifier.FINAL);
        setImplicit();
        setNamespaceReference(NamespaceDefinition.getPublicNamespaceDefinition());
    }

    private ICompilerProject project;

    /*
     *  The base class will be Vector$int, Vector$uint, Vector$Number, or Vector$Object.
     */
    private IClassDefinition baseClass;
    
    /*
     * The element type can be any class or interface, including another vector type
     * in a nested case like Vector.<Vector.<int>>.
     */
    private ITypeDefinition elementType;

    @Override
    protected String toStorageName(String name)
    {
        return name;
    }
    
    @Override
    public IReference getBaseClassReference()
    {
        return null;
    }

    @Override
    protected TypeDefinitionBase resolveType(String typeName,
                                             ICompilerProject project, DependencyType dt)
    {
        return resolveType((DefinitionBase)baseClass, typeName, project, dt);
    }

    @Override
    public final String getQualifiedName()
    {
        return AppliedVectorDefinition.getName(elementType, true);
    }

    @Override
    public final String getBaseName()
    {
        return AppliedVectorDefinition.getName(elementType, false);
    }

    @Override
    public final String getPackageName()
    {
        // Base class version of this method relies
        // on this definition having a namespace reference
        // which we don't have.  So we just overload this
        // method to return the empty string which is the package
        // name clients of CM want for instances of Vector.<>.
        return "";
    }

    @Override
    public boolean isImplicit()
    {
        return true;
    }

    @Override
    public IClassDefinition[] resolveAncestry(ICompilerProject project)
    {
        return new IClassDefinition[] {baseClass};
    }

    public String getBaseClassName()
    {
        return baseClass.getBaseName();
    }

    @Override
    public ClassClassification getClassClassification()
    {
        return ClassClassification.PARAMETERIZED_CLASS_INSTANCE;
    }

    @Override
    public String[] getImplementedInterfacesAsDisplayStrings()
    {
        return new String[0];
    }

    @Override
    public IReference[] getImplementedInterfaceReferences()
    {
        return new IReference[0];
    }

    public IExpressionNode[] getImplementedInterfaceNodes()
    {
        return new IExpressionNode[0];
    }

    @Override
    public IMetaTag[] getMetaTagsByName(String name)
    {
        return new IMetaTag[0];
    }

    @Override
    public ITypeDefinition resolveElementType(ICompilerProject project)
    {
        return elementType;
    }

    /**
     * Returns the expression that represents the value of this vector
     * 
     * @return an expression that represents the value of this vector
     */
    public ExpressionNodeBase toExpression()
    {
        if (elementType instanceof AppliedVectorDefinition)
        {
            return new TypedExpressionNode(new IdentifierNode(IASLanguageConstants.Vector), ((AppliedVectorDefinition)elementType).toExpression());
        }
        return new IdentifierNode(elementType.getBaseName());
    }

    /**
     * Returns a typed expression that represents the signature of this Vector
     * 
     * @return a {@link TypedExpressionNode}
     */
    public TypedExpressionNode toTypedExpression()
    {
        if (elementType instanceof AppliedVectorDefinition)
        {
            return new TypedExpressionNode(new IdentifierNode(IASLanguageConstants.Vector), ((AppliedVectorDefinition)elementType).toTypedExpression());
        }
        return new TypedExpressionNode(new IdentifierNode(IASLanguageConstants.Vector), new IdentifierNode(elementType.getBaseName()));
    }

    @Override
    public String getBaseClassAsDisplayString()
    {
        return baseClass.getQualifiedName();
    }

    @Override
    public IClassDefinition resolveBaseClass(ICompilerProject project)
    {
        return baseClass;
    }

    public String getDefaultPropertyName()
    {
        return null;
    }

    @Override
    public INamespaceDefinition getProtectedNamespaceReference()
    {
        return null;
    }

    @Override
    public INamespaceDefinition getStaticProtectedNamespaceReference()
    {
        return null;
    }

    @Override
    public IEventDefinition getEventDefinition(IWorkspace w, String name)
    {
        return null;
    }

    @Override
    public IEventDefinition[] getEventDefinitions(IWorkspace w)
    {
        return new IEventDefinition[0];
    }

    @Override
    public IEventDefinition[] findEventDefinitions(ICompilerProject project)
    {
        return new IEventDefinition[0];
    }

    @Override
    public IStyleDefinition getStyleDefinition(IWorkspace w, String name)
    {
        return null;
    }

    @Override
    public IStyleDefinition[] getStyleDefinitions(IWorkspace w)
    {
        return new IStyleDefinition[0];
    }

    @Override
    public IStyleDefinition[] findStyleDefinitions(ICompilerProject project)
    {
        return new IStyleDefinition[0];
    }

    @Override
    public IEffectDefinition getEffectDefinition(IWorkspace w, String name)
    {
        return null;
    }

    @Override
    public IEffectDefinition[] getEffectDefinitions(IWorkspace w)
    {
        return new IEffectDefinition[0];
    }

    @Override
    public IEffectDefinition[] findEffectDefinitions(ICompilerProject project)
    {
        return new IEffectDefinition[0];
    }

    @Override
    public String[] getSkinStates(Collection<ICompilerProblem> problems)
    {
        return new String[0];
    }

    @Override
    public String[] findSkinStates(ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        return getSkinStates(problems);
    }

    @Override
    public IMetaTag[] getSkinParts(Collection<ICompilerProblem> problems)
    {
        return new IMetaTag[0];
    }

    @Override
    public IMetaTag[] findSkinParts(ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        return getSkinParts(problems);
    }

    @Override
    public Set<String> getStateNames()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<String> findStateNames(ICompilerProject project)
    {
        return getStateNames();
    }

    @Override
    public Name getMName(ICompilerProject project)
    {
        // The Name for a type such as Vector.<T> has kind CONSTANT_TypeName.
        // It gets constructed by passing two Names, the first representing
        //the  __AS3__.vec.Vector base type and the second representing the T type.
        Name typeName = ((DefinitionBase)elementType).getMName(project);
        return new Name(VECTOR_NAME, typeName);
    }

    /**
     * Copy down definitions from Vector$object into the instantiated class as
     * we go, modify to the parameter and return types to match what is actually
     * expected (Vector$object becomes Vector.<T>, etc).
     * 
     * @param project Project we are in
     */
    public void adjustVectorMethods(ICompilerProject project)
    {
        VectorInformation vecInfo = VectorInformation.getInformation();
        ASScope scope = getContainedScope();

        // Copy the methods from the base class, so they report themselves as belonging
        // to the instantiated Vector class instead of Vector$object, etc.
        IASScope baseClassScope = baseClass.getContainedScope();
        for (IDefinition defaultDef : baseClassScope.getAllLocalDefinitions())
        {
            String name = defaultDef.getBaseName();
            VectorInformation.FunctionInfo info = vecInfo.getFunctionInfo(name);
            if (defaultDef instanceof FunctionDefinition)
            {
                FunctionDefinition defaultFunc = (FunctionDefinition)defaultDef;
                // Override the base class definition with one that has the correct signature

                FunctionDefinition newDef;
                if (defaultDef instanceof GetterDefinition)
                {
                    newDef = new GetterDefinition(name);
                    newDef.setReturnTypeReference(defaultFunc.getReturnTypeReference());
                    newDef.setTypeReference(defaultFunc.getTypeReference());
                }
                else if (defaultDef instanceof SetterDefinition)
                {
                    newDef = new SetterDefinition(name);
                    newDef.setReturnTypeReference(defaultFunc.getReturnTypeReference());
                    newDef.setTypeReference(defaultFunc.getTypeReference());
                }
                else
                {
                    newDef = new FunctionDefinition(name);
                    newDef.setReturnTypeReference(defaultFunc.getReturnTypeReference());
                }

                ASScope newScope = new FunctionScope(scope);
                newDef.setContainedScope(newScope);

                newDef.setNamespaceReference(defaultDef.getNamespaceReference());

                if (info != null)
                {
                    if (info.returnIsTypeOfCollection())
                        newDef.setReturnTypeReference(ReferenceFactory.resolvedReference(elementType));
                    else if (info.returnIsVector())
                        newDef.setReturnTypeReference(ReferenceFactory.resolvedReference(this));
                }
                ParameterDefinition[] params = defaultFunc.getParameters();

                if (params != null)
                {
                    VectorInformation.ArgumentInfo[] args = info != null ? info.getArgumentInfo() : null;
                    ParameterDefinition[] newParams = new ParameterDefinition[params.length];
                    for (int i = 0, l = params.length; i < l; ++i)
                    {
                        if (args != null && i < args.length)
                        {
                            newParams[i] = copyParameter(project, params[i], args[i]);
                            if (args[i].returnIsVector())
                                newParams[i].setTypeReference(ReferenceFactory.resolvedReference(this));
                            else if (args[i].returnIsTypeOfCollection())
                                newParams[i].setTypeReference(ReferenceFactory.resolvedReference(elementType));
                        }
                        else
                        {
                            newParams[i] = copyParameter(project, params[i], null);
                        }
                        newScope.addDefinition(newParams[i]);

                    }
                    newDef.setParameters(newParams);
                }
                scope.addDefinition(newDef);
            }
        }
    }

    /**
     * Gets the {@link ICompilerProject} that this
     * {@link AppliedVectorDefinition} was created for.
     * 
     * @return The {@link ICompilerProject} that this
     * {@link AppliedVectorDefinition} was created for
     */
    public ICompilerProject getProject()
    {
        return project;
    }

    @Override
    public String getDefaultPropertyName(ICompilerProject project)
    {
        // TODO Eliminate this method stub. AppliedVectorDefinition should not be implementing IAppliedVectorDefinition.
        return null;
    }

    private static AppliedVectorDefinition toVectorNodeOrContainingVectorNode(IDefinition def)
    {
        assert def != null;
        if (def instanceof AppliedVectorDefinition)
            return (AppliedVectorDefinition)def;

        ASScope containingScope = (ASScope)def.getContainingScope();
        if (containingScope == null)
            return null;
        IDefinition containingDefinition = containingScope.getDefinition();
        if (containingDefinition instanceof AppliedVectorDefinition)
            return (AppliedVectorDefinition)containingDefinition;
        IDefinition containingClassDefiniton = containingScope.getContainingClass();
        if (containingClassDefiniton instanceof AppliedVectorDefinition)
            return (AppliedVectorDefinition)containingClassDefiniton;
        return null;
    }

    /**
     * Determines if the specified definition is a vector instantiation or a
     * contained by a vector instantiation.
     * 
     * @param def {@link IDefinition} to check.
     * @return true if the specified definition is a vector instantiation or is
     * contained by a vector instantiation.
     */
    public static ICompilerProject getProjectIfVectorInsantiation(IDefinition def)
    {
        AppliedVectorDefinition vectorNode = toVectorNodeOrContainingVectorNode(def);
        if (vectorNode != null)
            return vectorNode.getProject();
        return null;
    }

    public static boolean isVectorScope(ASScope scope)
    {
        // Check if this scope is a Vector scope, or if it is contained in a Vector scope
        // this is possible because we now copy the methods down to the insantiated vector
        // instead of finding them in the Vector$object baseclass.  
        return scope.getDefinition() instanceof AppliedVectorDefinition
               || scope.getContainingClass() instanceof AppliedVectorDefinition;
    }

    @Override
    public IFunctionDefinition getConstructor()
    {
        assert false : "This should never get called.";
        return null;
    }
    
    
    @Override
    public IClassDefinition resolveHostComponent(ICompilerProject project)
    {
        return null;
    }

    /**
     * Helper method that implements special instanceOf checks for Vector types.
     * <p>
     * This method aliases the Vector$XXXX where XXXX is double, uint, or int to
     * Vector.<XXXXX>.
     * 
     * @param base {@link ITypeDefinition} for the base type in the instanceOf
     * check.
     * @param derived {@link ITypeDefinition} for the dervied type in the
     * instanceOf check.
     * @return true if the derived type should be considered and instance of the
     * base type.
     */
    public static boolean vectorInstanceOfCheck(ITypeDefinition base, ITypeDefinition derived)
    {
        if (!(base instanceof AppliedVectorDefinition))
            return false;

        final AppliedVectorDefinition vectorInstance = (AppliedVectorDefinition)base;
        if (vectorInstance.baseClass != derived)
            return false;
        assert ((INamespaceDefinition)vectorInstance.baseClass.getNamespaceReference()).getURI().equals(IASLanguageConstants.Vector_impl_package);
        return !(IASLanguageConstants.Vector_object.equals(derived.getBaseName()));
    }

    @Override
    public boolean isInProject(ICompilerProject project)
    {
        return project == this.project;
    }
    
    /**
     * Returns the class definition for Vector$object, Vector$int, Vector$uint, or Vector$double.
     */
    public static IClassDefinition lookupVectorImplClass(ICompilerProject project, String baseName)
    {
        // Only the base name (e.g., "Vector$object") is passed in.
        // But the developer may have also defined something with the same
        // base name, so we need to make sure that the definition we find
        // also has the correct package, namely "__AS3__.vec".
        
        INamespaceDefinition vectorImplPackage = ((CompilerProject)project).getWorkspace().getPackageNamespaceDefinitionCache().get(IASLanguageConstants.Vector_impl_package, true);
        
        IResolvedQualifiersReference vectorReference = ReferenceFactory.resolvedQualifierQualifiedReference(project.getWorkspace(), vectorImplPackage, baseName);
        IDefinition vectorIDefinition = vectorReference.resolve(project);
        assert vectorIDefinition instanceof IClassDefinition :
            "Unable to find: " + vectorReference.getDisplayString();
        return (IClassDefinition) vectorIDefinition;
    }
    
    /**
     * Updates the base class of this {@link AppliedVectorDefinition} if the
     * vector base name of the base class matches the base name of the vector
     * implementation class that has changed.
     * 
     * @param changedVectorImplClass base name of the vector implementation
     * class that has changed.
     */
    public void updateBaseClass(String changedVectorImplClass)
    {
        final String baseClassBaseName = baseClass.getBaseName();
        if (baseClassBaseName.equals(changedVectorImplClass))
            baseClass = lookupVectorImplClass(project, baseClassBaseName);
    }
}
