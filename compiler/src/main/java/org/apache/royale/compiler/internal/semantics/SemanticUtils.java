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

package org.apache.royale.compiler.internal.semantics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition.FunctionClassification;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IDeprecationInfo;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.exceptions.MissingBuiltinException;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.as.codegen.Binding;
import org.apache.royale.compiler.internal.as.codegen.LexicalScope;
import org.apache.royale.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.royale.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.GetterDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.PackageDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.SetterDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.ASScopeBase;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.scopes.ScopeView;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.BaseTypedDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorAssignmentNode;
import org.apache.royale.compiler.internal.tree.as.BlockNode;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.IterationFlowNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.TypedExpressionNode;
import org.apache.royale.compiler.internal.tree.as.UnaryOperatorAtNode;
import org.apache.royale.compiler.internal.tree.as.UnaryOperatorNodeBase;
import org.apache.royale.compiler.internal.tree.as.VariableExpressionNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.internal.tree.as.WithNode;
import org.apache.royale.compiler.problems.AmbiguousReferenceProblem;
import org.apache.royale.compiler.problems.BaseClassIsFinalProblem;
import org.apache.royale.compiler.problems.CannotExtendInterfaceProblem;
import org.apache.royale.compiler.problems.CircularTypeReferenceProblem;
import org.apache.royale.compiler.problems.DeprecatedAPIProblem;
import org.apache.royale.compiler.problems.DeprecatedAPIWithMessageProblem;
import org.apache.royale.compiler.problems.DeprecatedAPIWithReplacementProblem;
import org.apache.royale.compiler.problems.DeprecatedAPIWithSinceAndReplacementProblem;
import org.apache.royale.compiler.problems.DeprecatedAPIWithSinceProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.ParameterHasNoTypeDeclarationProblem;
import org.apache.royale.compiler.problems.ReturnValueHasNoTypeDeclarationProblem;
import org.apache.royale.compiler.problems.ScopedToDefaultNamespaceProblem;
import org.apache.royale.compiler.problems.UnknownSuperclassProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.INumericLiteralNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;

/**
 *  SemanticUtils is a set of utility routines to provide information
 *  about AS3 constructs' semantics.
 *  
 *  Many of these routines come in pairs; a static form that accepts 
 *  an ICompilerProject parameter for name resolution context, and
 *  an instance form that uses the project provided at construction.
 */
public class SemanticUtils
{
    /**
     * Enum that describes different kinds of multiple definitions
     */
    public enum MultiDefinitionType {
        /** The definition is not multiply defined at all */
        NONE,
        
        /** The definition is ambiguous in a way that violates the language spec */
        AMBIGUOUS,
        
        /** The definition is multiply defined, but in a way that is legal and unambiguous  ECMAScript */ 
        MULTIPLE,
        
        /** The definition is also a parameter definition, which is legal
         * The only reason SHADOWS_PARAM and MULTIPLE are different is that the old compiler did not detect shadows
         */
        SHADOWS_PARAM
        }
    /**
     *  ICompilerProject for the current project,
     *  provided by the constructor.
     */
    private final ICompilerProject project;

    /**
     *  Construct a new SemanticUtils object.
     *  @param project - an ICompilerProject for the current project.
     */
    public SemanticUtils(ICompilerProject project)
    {
        this.project = project;
    }

    /**
     * Check if the node is for {@code super(this)} but not
     * {@code super(this).call()}. The incoming tree shape is:
     * 
     * <pre>
     * MemberAccessExpressionID(FunctionCallID(SuperID(void), ContainerID(IdentifierID(void))), qname)
     * </pre>
     * 
     * @return "0" if the node represents {@code super(this)};
     * {@link Integer#MAX_VALUE} otherwise.
     */
    public static int isSuperThisForFieldAccess(IASNode n)
    {
        // super(this).callSomething() is handled by another pattern that emits
        // "callsuper" instruction.
        if (n.getParent() instanceof FunctionCallNode)
            return Integer.MAX_VALUE;

        // The following unguarded casts are safe, because this is a cost 
        // function and the pattern matcher has checked all the node IDs.
        final MemberAccessExpressionNode memberAccessNode = (MemberAccessExpressionNode)n;
        final FunctionCallNode callNode = (FunctionCallNode)memberAccessNode.getLeftOperandNode();
        final IdentifierNode idNode = (IdentifierNode)callNode.getArgumentsNode().getChild(0);

        if (idNode.getName().equals(IASKeywordConstants.THIS))
            return 1;
        else
            return Integer.MAX_VALUE;
    }

    /**
     * Check that a implicit conversion from actual to expected type is valid.
     * 
     * @param expected the expected type.
     * @param actual the actual type.
     * @param project an ICompilerProject for the current project.
     * @return true if there is a valid implicit type conversion from actual to
     * expected, false otherwise.
     */
    private static boolean isValidTypeConversion(IDefinition expected, IDefinition actual, ICompilerProject project)
    {
        if  (
                actual == null ||
                expected == null ||
                actual.equals(getBuiltinType(BuiltinType.ANY_TYPE, project)) ||
                expected.equals(getBuiltinType(BuiltinType.ANY_TYPE, project)) ||
                isInstanceOf(actual, expected, project) ||
                isBuiltin(actual, BuiltinType.NULL, project) ||
                isBuiltin(expected, BuiltinType.BOOLEAN, project) ||
                isBuiltin(expected, BuiltinType.VOID, project)
                )
            {
                return true;
            }
            // else if ( isVectorSplat(expected) && isVector(actual) ) // This is allowed by the AVM.
            else if (
                isBuiltin(expected, BuiltinType.STRING, project) &&
                (isBuiltin(actual, BuiltinType.XML, project) || isBuiltin(actual, BuiltinType.XMLLIST, project))
            )
            {
                // allow this, e4x values are string values transparently (i.e. without having to call toString()/toXMLString()).
                return true;
            }

            //  Types are unrelated, unless they're both numeric.
            return isNumericType(expected, project) && isNumericType(actual, project);
    }

    /**
     *  Check that implicit conversion done as part of an op= style assignment is valid.
     *  @param lhsType - the type of the lvalue (and one operand of the implicit binary operator).
     *  @param rhsType - the type of the result of the implicit binary operation.
     *  @param opcode - the operator of the implicit binary operator.
     *  @param project an ICompilerProject for the current project.
     *  @return true if there is a valid implicit binary operation that can assign
     *    its result to lhsType.
     */
    public static boolean isValidImplicitOpAssignment(IDefinition lhsType, IDefinition rhsType, final int opcode, ICompilerProject project, final boolean compareNormalized)
    {
        boolean result = isValidTypeConversion(lhsType, rhsType, project, compareNormalized);

        if ( ! result )
        {
            switch (opcode)
            {
                case ABCConstants.OP_add:
                    //  Special cases for +=:
                    //  anything can be added to String
                    //  XML can be added to XMLList
                    result = 
                        isBuiltin(lhsType, BuiltinType.STRING, project ) ||
                        (isBuiltin(lhsType, BuiltinType.XMLLIST, project) && isBuiltin(rhsType, BuiltinType.XML, project));
                    break;
            }
        }

        return result;
    }
    
    /**
     * If the specified {@link IDefinition} is a public or internal definition,
     * then construct a qualified name to lookup a definition with the same name
     * in the {@link ICompilerProject}. The {@link IDefinition} that can be
     * found within the {@link ICompilerProject} is the "normalized"
     * {@link IDefinition}.
     * <p>
     * This method asserts that is only called with definitions that are not
     * local variables, parameters, or class members.
     * <p>
     * This method is necessary because
     * {@link org.apache.royale.compiler.units.IInvisibleCompilationUnit}s create the
     * possibility that we will be processing a file whose definitions are not
     * in the {@link ASProjectScope} in the {@link ICompilerProject}. This in
     * turn creates the possibility there are two or more {@link IDefinition}s
     * floating around for the same class, variable, or function. One the
     * {@link IDefinition}, the "normalized" one, should be registered with the
     * {@link ASProjectScope} in the {@link ICompilerProject}. The other
     * {@link IDefinition}s are from
     * {@link org.apache.royale.compiler.units.IInvisibleCompilationUnit}s. When doing
     * semantic analysis if we don't "normalize" {@link IDefinition}s in some
     * cases, we'll get spurious errors because the semantic analysis code
     * generally compares {@link IDefinition}s by identity ( which is faster )
     * rather than by name. This method should only be called when doing
     * semantic analysis of an
     * {@link org.apache.royale.compiler.units.IInvisibleCompilationUnit}.
     * 
     * @param def The {@link IDefinition} whose corresponding normalized
     * definition should be returned.
     * @param project The {@link ICompilerProject} in which to find normalized
     * {@link IDefinition}s.
     * @return The normalized {@link IDefinition} ( which may be the same as the
     * specified {@link IDefinition} ) or null.
     */
    private static IDefinition normalizeDefinition(IDefinition def, ICompilerProject project)
    {
        assert !(def.getContainingScope() instanceof FunctionScope);
        assert !(def.getContainingScope() instanceof ScopeView);
        assert !(def.getContainingScope() instanceof TypeScope);
        assert !(def instanceof ParameterDefinition);

        INamespaceReference qualifier = def.getNamespaceReference();
        if (!qualifier.isPublicOrInternalNamespace())
            return def;
        INamespaceDefinition qualifierDef = (INamespaceDefinition)qualifier;
        IResolvedQualifiersReference refToDefinition =
                ReferenceFactory.resolvedQualifierQualifiedReference(project.getWorkspace(), qualifierDef, def.getBaseName());
        return refToDefinition.resolve(project);
    }
    
    /**
     * Check that a implicit conversion from actual to expected type is valid.
     * 
     * @param expected the expected type.
     * @param actual the actual type.
     * @param project an ICompilerProject for the current project.
     * @param normalizeDefinitions If true, we'll try normalizing both
     * {@link IDefinition}s before returning false.  See the java doc for
     * {@code normalizeDefinition(IDefinition, ICompilerProject)} for more info.
     * @return true if there is a valid implicit type conversion from actual to
     * expected, false otherwise.
     */
    public static boolean isValidTypeConversion(IDefinition expected, IDefinition actual, ICompilerProject project, boolean normalizeDefinitions)
    {
        if (isValidTypeConversion(expected, actual, project))
            return true;
        if (!normalizeDefinitions)
            return false;
        final IDefinition normalizedExpected = normalizeDefinition(expected, project);
        if (normalizedExpected == null)
            return false;
        final IDefinition normalizedActual = normalizeDefinition(actual, project);
        if (normalizedActual == null)
            return false;
        if ((normalizedExpected == expected) && (normalizedActual == actual))
            return false;
        return isValidTypeConversion(normalizedExpected, normalizedActual, project);
    }

    /**
     * Helper method to get the node to report a problem with a Function.  This will get the name node,
     * if it exists, otherwise it will return the function node.
     */
    public static IASNode getFunctionProblemNode(IFunctionNode iNode)
    {
        IASNode result = iNode;
        IExpressionNode nameExpression = iNode.getNameExpressionNode();
        if( nameExpression != null )
            result = nameExpression;
        return result;
    }

    /**
     * Helper method to determine if an IdentifierNode refers to the Class it is contained
     * in from a context where we are generating code that may be run during the cinit method.
     *
     * This is important because cinit code must refer to the class via getlocal0 instead of a
     * named lookup like findprop, getprop.  This is because the Class property is initialized with the
     * result of the cinit, but since we are potentially in the cinit, the property will not have been initialized.
     *
     * class C {
     *     static var a = C;  // C won't have been initialized yet, so we need to use getlocal0
     * }
     *
     * The above class generates code like:
     *
     * findprop C
     * newclass C // this runs the cinit, and leaves the result on the stack, at this point 'C' is undefined
     * setprop  C // this will init the property
     *
     * @param id  the identifier node that we are resolving
     * @param def the definition the identifier node resolved to
     * @return    true if this is a reference to the Class that is being initialized, and needs special treatment.
     */
    public static boolean isRefToClassBeingInited (IdentifierNode id, IDefinition def)
    {
        boolean refToClassBeingInited = false;
        if( def instanceof IClassDefinition)
        {
            ASScope scope = id.getASScope();
            IDefinition containingDef = null;

            while( scope != null )
            {
                if( containingDef == null )
                    containingDef = scope.getDefinition();

                if( scope instanceof ScopeView)
                {
                    if (((ScopeView)scope).isInstanceScope() )
                    {
                        // stop looking, we're in an instance method, so we're ok
                        break;
                    }
                    else if( ((ScopeView)scope).isStaticScope() && def == scope.getDefinition() )
                    {
                        // we're in static code, and are referencing the class we are in
                        refToClassBeingInited = true;
                        break;
                    }
                }

                scope = scope.getContainingScope();
            }
        }
        return refToClassBeingInited;
    }

    /**
     *  Determine if an IDefinition is a specific builtin type.
     *  @param def - the IDefinition of interest.
     *  @param builtin - the builtin type.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if def is the desired builtin type.
     */
    public static boolean isBuiltin(IDefinition def, BuiltinType builtin, ICompilerProject project)
    {
        return def != null && def.equals(project.getBuiltinType(builtin));
    }

    /**
     * Find potential conflict for a FunctionDefinition. In some contexts it is ok to redeclare a Function (global, or
     * inside another function body).  For those cases the function will not be reported as ambiguous.  However, in strict
     * mode we do want to issue a diagnostic that the function has been redeclared - this method is to find the redlarations
     * even though those redecls don't make references ambiguous.
     * @param project   The project to resolve things in
     * @param funcDef   The function definition to find potential conlicts for
     * @return          A List of Definitions from the same scope that have the same name as the function definition, including
     *                  the function definition (so if there are no conflicts, the list should have 1 item).
     */
    public static List<IDefinition> findPotentialFunctionConflicts (ICompilerProject project, IFunctionDefinition funcDef)
    {
        ASScope scope = (ASScope)funcDef.getContainingScope();
        INamespaceDefinition qualifier = funcDef.getNamespaceReference().resolveNamespaceReference(project);
        Set<INamespaceDefinition> namespaceSet = Collections.singleton(qualifier);
        return scope.getPropertiesByNameForMemberAccess((CompilerProject) project, funcDef.getBaseName(), namespaceSet);
    }

    /**
     * Is the binding a potential reference to
     * the 'arguments' object
     * @param b         the Binding to check
     * @return          true if the reference could be a reference to 'arguments'
     */
    public static boolean isArgumentsReference (Binding b)
    {
        Name name = b.getName();
        if (name != null && IASLanguageConstants.arguments.equals(name.getBaseName()))
        {
            // the name is "arguments", but make sure it's not a user defined definition
            // by checking whether the def is implicit
            IDefinition definition = b.getDefinition();
            if (definition != null)
            {
                return definition.isImplicit();
            }
            else
            {
                // if there's no definition, as can happen when referencing "arguments" from
                // within a 'with', then fall back to checking on the node.
                IASNode node = b.getNode();
                return node instanceof IdentifierNode && !((IdentifierNode) node).isMemberRef();
            }
        }

        return false;
    }

    /**
     *  Determine if an IDefinition is a specific builtin type.
     *  @param def - the IDefinition of interest.
     *  @param builtin - the builtin type.
     *  @return true if def is the desired builtin type.
     */
    public boolean isBuiltin(IDefinition def, BuiltinType builtin)
    {
        return isBuiltin(def, builtin, this.project);
    }

    /**
     *  @return true if the given definition is a numeric type.
     *  @param def - the IDefinition of interest.
     *  @param project - an ICompilerProject for the current project.
     */
    public static boolean isNumericType(IDefinition def, ICompilerProject project)
    {
        return isBuiltin(def, BuiltinType.INT, project) ||
               isBuiltin(def, BuiltinType.UINT, project) ||
               isBuiltin(def, BuiltinType.NUMBER, project);
    }

    /**
     *  @return true if the given definition is a numeric or boolean type.
     *  @param def - the IDefinition of interest.
     *  @param project - an ICompilerProject for the current project.
     */
    public static boolean isNumericTypeOrBoolean(IDefinition def, ICompilerProject project)
    {
        return isNumericType(def, project) ||
            isBuiltin(def, BuiltinType.BOOLEAN, project);
    }
    
    /**
     *  @return true if the given definition is a numeric type.
     *  @param def - the IDefinition of interest.
     *  @see #isNumericType(IDefinition, ICompilerProject)
     */
    public boolean isNumericType(IDefinition def)
    {
        return isNumericType(def, this.project);
    }

    /**
     *  Determine if one IDefinition is a subclass of another.
     *  @param maybe_derived The first definition.
     *  @param maybe_base The second definition.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if  maybe_derived is an instance of maybe_base.
     */
    public static boolean isInstanceOf(IDefinition maybe_derived, IDefinition maybe_base, ICompilerProject project)
    {
        if (!(maybe_base instanceof ITypeDefinition))
            return false;
        if (!(maybe_derived instanceof ITypeDefinition))
            return false;
        
        ITypeDefinition base    = (ITypeDefinition) maybe_base;
        ITypeDefinition derived = (ITypeDefinition) maybe_derived;

        if (derived.isInstanceOf(base, project))
            return true;
        
        
        //  Consider null to be an "instanceof" any Object subtype.
        boolean base_is_object = isInstanceOf(base, getBuiltinType(BuiltinType.OBJECT, project), project);
        if (base_is_object && derived.equals(getBuiltinType(BuiltinType.NULL, project)))
            return true;
        
        
        if (AppliedVectorDefinition.vectorInstanceOfCheck(base, derived))
            return true;
        
        return false;
        
    }

    /**
     *  Determine if one IDefinition is a subclass of another.
     *  @param maybe_derived The first definition.
     *  @param maybe_base The second definition.
     *  @return true if  maybe_derived is an instance of maybe_base.
     *  @see #isInstanceOf(IDefinition, IDefinition, ICompilerProject)
     */
    public boolean isInstanceOf(IDefinition maybe_derived, IDefinition maybe_base)
    {
        return isInstanceOf(maybe_derived, maybe_base, this.project);
    }

    /**
     *  Fetch a builtin type.
     *  @param builtin - the BuiltinType identifier of the type.
     *  @param project - an ICompilerProject for the current project.
     *  @return the specified builtin type.
     */
    public static IDefinition getBuiltinType(BuiltinType builtin, ICompilerProject project)
    {
        return project.getBuiltinType(builtin);
    }

    /**
     *  Fetch a builtin type.
     *  @param builtin - the BuiltinType identifier of the type.
     *  @return the specified builtin type.
     */
    public IDefinition getBuiltinType(BuiltinType builtin)
    {
        return getBuiltinType(builtin, this.project);
    }

    /**
     *  Get the current number type.
     *  @param project - an ICompilerProject for the current project.
     *  @return the current number type; currently hardcoded
     *    to BuiltinTypes.NUMBER.
     */
    public static IDefinition numberType(ICompilerProject project)
    {
        return getBuiltinType(BuiltinType.NUMBER, project);
    }

    /**
     *  Get the current number type.
     *  @return the current number type; currently hardcoded
     *    to BuiltinTypes.NUMBER.
     *  @see #numberType(ICompilerProject)
     */
    public IDefinition numberType()
    {
        return numberType(this.project);
    }

    /**
     *  Given the definition of a getter or setter, resolve its 
     *  corresponding accessor (e.g., the setter for a getter).
     *  @param def - the definition of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the corresponding accessor, or null if 
     *    there is no corresponding accessor.
     *  @throws IllegalArgumentException if the definition is
     *    not for a getter or setter.
     */
    public static IDefinition resolveCorrespondingAccessor(IDefinition def, ICompilerProject project)
    {
        if ( def instanceof GetterDefinition )
            return ((GetterDefinition)def).resolveCorrespondingAccessor(project);
        else if ( def instanceof SetterDefinition )
            return ((SetterDefinition)def).resolveCorrespondingAccessor(project);
        else
            throw new IllegalArgumentException("Definition " + def + " was not a getter or setter.");
    }

    /**
     *  Given the definition of a getter or setter, resolve its 
     *  corresponding accessor (e.g., the setter for a getter).
     *  @param def - the definition of interest.
     *  @return the corresponding accessor, or null if 
     *    there is no corresponding accessor.
     *  @throws IllegalArgumentException if the definition is
     *    not for a getter or setter.
     *  @see #resolveCorrespondingAccessor(IDefinition, ICompilerProject)
     */
    public IDefinition resolveCorrespondingAccessor(IDefinition def)
    {
        return resolveCorrespondingAccessor(def, this.project);
    }

    /**
     *  Is the given node in a with scope?
     *  @param iNode - the node of interest.
     *  @return the node's own answer if it's an
     *    ExpressionNodeBase; otherwise return true 
     *    if the node has a WithNode ancestor.
     */
    public static boolean isInWith(IASNode iNode)
    {
        if ( iNode instanceof ExpressionNodeBase )
        {
            return ((ExpressionNodeBase)iNode).inWith();
        }
        else
        {
            return iNode.getAncestorOfType(WithNode.class) != null;
        }
    }

    /**
     *  Is the given node in a filter expression?
     *  @param iNode - the node of interest.
     *  @return the node's own answer if it's an
     *    ExpressionNodeBase; otherwise return false.
     */
    public static boolean isInFilter(IASNode iNode)
    {
        if ( iNode instanceof ExpressionNodeBase )
        {
            return ((ExpressionNodeBase)iNode).inFilter();
        }
        else
        {
            return false;
        }
    }

    /**
     * Check if a nested function can have it's init instructions hoisted to the top of the
     * function it is contained in.  This is not possible when the function is inside some control-flow,
     * as the OP_newfunction must be executed only when control flows there (and with whatever scope stack is in effect
     * if we're in a with/catch scope).
     * @param iNode  The node to check
     * @return       true if it is ok to hoist the init instructions for the node to the top of the function
     */
    public static boolean canNestedFunctionBeHoisted(IASNode iNode)
    {
        IASNode n = iNode.getParent();
        // Walk up the parent chain looking for anything other than block nodes,
        // until we get to the containing function - we don't have to look past the containing
        // function because any control flow around it should have been handled when setting up the outer function
        while( n != null && !(n instanceof FunctionNode) )
        {
            if( !(n instanceof BlockNode) )
                return false;

            n = n.getParent();
        }
        return true;
    }

    /**
     *  Is this definition a member of a class?
     *  @param def - the definition of interest.
     *  @return true if the definition's parent is a class definition.
     */
    public static boolean isMemberDefinition(IDefinition def)
    {
        return def != null && def.getParent() instanceof ClassDefinition;
    }

    /**
     *  Is this definition a member of a package?
     *  @param def - the definition of interest.
     *  @return true if the definition's parent is a package definition.
     */
    public static boolean isPackageDefinition(IDefinition def)
    {
        return def != null && def.getParent() instanceof PackageDefinition;
    }

    /**
     *  Is the definition going to be a method binding in the ABC?
     *  method bindings can be called with findprop/callprop instead of having to
     *  use op_call.
     *
     *  A method biniding is a member function, or a global function defined in a package
     *
     *  @param def - the definition of interest.
     *  @return true if the definition is a method binding.
     */
    public static boolean isMethodBinding(IDefinition def)
    {
        return def instanceof IFunctionDefinition &&
                (isMemberDefinition(def) || isPackageDefinition(def));
    }

    /**
     *  Is this definition read-only?
     *  @param def - the definition of interest.
     */
    public boolean isReadOnlyDefinition(IDefinition def)
    {
        boolean result = false;

        if ( def instanceof GetterDefinition )
        {
        	IDefinition otherDef = resolveCorrespondingAccessor(def);
            if (otherDef == null) return true;
            if (otherDef.getNamespaceReference().getBaseName() != def.getNamespaceReference().getBaseName())
            	return true;
        }
        else if ( def instanceof ConstantDefinition )
        {
            result = true;
        }

        return result;
    }

    /**
     *  Is this Binding's definition statically analyzable?
     *  @param binding - the Binding of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if the Binding has no definition and
     *    it is in a context that implies its definition
     *    is statically resolvable.
     */
    public static boolean definitionCanBeAnalyzed(Binding binding, ICompilerProject project)
    {
        assert(binding.getNode()) != null;

        IASNode node = binding.getNode();

        if ( node instanceof ExpressionNodeBase )
        {
            //  Ensure we're not in a with scope or part of a filter expression.
            final ExpressionNodeBase expressionNode = (ExpressionNodeBase)node;
            if (expressionNode.inWith() || expressionNode.inFilter())
                return false;
        }

        //  Attribute names aren't statically knowable.
        if ( binding.getName() != null && binding.getName().isAttributeName() )
            return false;

        return !hasDynamicBase(binding, project);
    }

    /**
     *  Is this Binding's definition statically analyzable?
     *  @param binding - the Binding of interest.
     *  @return true if the Binding has no definition and
     *    it is in a context that implies its definition
     *    is statically resolvable.
     *  @see #definitionCanBeAnalyzed(Binding, ICompilerProject)
     */
    public boolean definitionCanBeAnalyzed(Binding binding)
    {
        return definitionCanBeAnalyzed(binding, this.project);
    }

    /**
     *  Is this definition write-only?
     *  @param def - the definition of interest.
     *  @return true if the definition is a set 
     *    that does not have a corresponding get.
     */
    public boolean isWriteOnlyDefinition(IDefinition def)
    {
        return def instanceof SetterDefinition && resolveCorrespondingAccessor(def) == null;
    }

    /**
     *  Check an ExpressionNodeBase's base expression (if any)
     *  to determine if it's a dynamic expression, which
     *  means we don't know much about its definition.
     *  @param binding - the binding whose base node is to be checked.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if node's base expression is dynamic.
     */
    public static boolean hasDynamicBase(Binding binding, ICompilerProject project)
    {
        ExpressionNodeBase base = getBaseNode(binding);
        if (base != null && base.isDynamicExpression(project))
        	return true;
        // the JS version of XML is not currently dynamic so special case it here.
        if (base != null && base.getNodeID() == ASTNodeID.IdentifierID && isXMLish(base.resolveType(project), project))
        	return true;
        return false;
    }

    /**
     *  Check an ExpressionNodeBase's base expression (if any)
     *  to determine if it's a dynamic expression, which
     *  means we don't know much about its definition.
     *  @param binding - the binding whose base node is to be checked.
     *  @return true if node's base expression is dynamic.
     *  @see #hasDynamicBase(Binding, ICompilerProject)
     */
    public boolean hasDynamicBase(Binding binding)
    {
        return hasDynamicBase(binding, this.project);
    }
    
    /**
     *  Get an ExpressionNodeBase's base node.
     *  @return the node's base expression, or null
     *    if the Binding does not wrap an ExpressionNodeBase.
     */
    public static ExpressionNodeBase getBaseNode(Binding binding)
    {
        if ( binding.getNode() instanceof ExpressionNodeBase )
            return ((ExpressionNodeBase)binding.getNode()).getBaseExpression();
        else
            return null;
    }
    

    /**
     *  Get the definition associated with a IASNode.
     *  @param iNode - the node whose definition is desired.
     *  @return the node's definition, or null if the node
     *    is of an unknown type or has no definition.
     */
    public IDefinition getDefinition(IASNode iNode)
    {
        return getDefinition(iNode, project);
    }

    /**
     *  Get the definition associated with a IASNode.
     *  @param iNode - the node whose definition is desired.
     *  @param project - an ICompilerProject for the current project.
     *  @return the node's definition, or null if the node
     *    is of an unknown type or has no definition.
     *    
     *    TODO: don't require a ASDefinition cache, base on ICompilerProject
     */
    public static IDefinition getDefinition(IASNode iNode, ICompilerProject project)
    {
        if ( iNode instanceof ExpressionNodeBase )
        {
            return ((ExpressionNodeBase)iNode).resolve(project);
        }
        else if ( iNode instanceof BaseTypedDefinitionNode )
        {
            return ((BaseTypedDefinitionNode)iNode).getDefinition();
        }
        else if ( iNode instanceof NamespaceNode )
        {
            return ((NamespaceNode)iNode).getDefinition();
        }

        return null;
    }
    
    public static IDefinition resolveType(IASNode iNode, ICompilerProject project)
    {
        if ( iNode instanceof ExpressionNodeBase )
        {
            return ((ExpressionNodeBase)iNode).resolveType(project);
        }
        else if ( iNode instanceof BaseTypedDefinitionNode )
        {
            return ((BaseTypedDefinitionNode)iNode).getDefinition().resolveType(project);
        }

        return null;
    }

    public IDefinition resolveType(IASNode iNode)
    {
        return resolveType(iNode, this.project);
    }

    /**
     * Resolve the type of the underlying expression in a UnaryOperatorNode.
     *
     * for '++i', this would return the type of 'i' as opposed to the result of the ++
     *
     * @param iNode  The UnaryOperatorNode you want the underlying type for
     *  @param project - an ICompilerProject for the current project.
     * @return       The type of the underlying expression, or null if the node passed in isn't
     *               a UnaryOperatorNode
     */
    public static IDefinition resolveUnaryExprType(IASNode iNode, ICompilerProject project)
    {
        if ( iNode instanceof UnaryOperatorNodeBase )
        {
            return ((UnaryOperatorNodeBase)iNode).getOperandNode().resolveType(project);
        }

        return null;
    }

    /**
     * Resolving the type of an expression on an XMLish object always returns *,
     * but this function returns the actual resolved type, if available.
     * 
     * @param iNode The node you want to resolve
     * @param project an ICompilerProject for the current project.
     * @return The type of the underlying expression, or null.
     */
    public static IDefinition resolveTypeXML(IExpressionNode iNode, ICompilerProject project)
    {
        IDefinition def = resolveXML(iNode, project);
        if(def == null)
        {
            return null;
        }
        if (def instanceof IFunctionDefinition)
        {
            IFunctionDefinition functionDef = (IFunctionDefinition) def;
            return functionDef.resolveReturnType(project);
        }
        return def.resolveType(project);
    }

    /**
     * Resolving an expression on an XMLish object always returns null, but this
     * function returns the actual resolved definition, if available.
     * 
     * @param iNode The node you want to resolve
     * @param project an ICompilerProject for the current project.
     * @return The definition of the underlying expression, or null.
     */
    public static IDefinition resolveXML(IExpressionNode iNode, ICompilerProject project)
    {
        if (iNode instanceof IFunctionCallNode)
        {
            IFunctionCallNode functionCall = (IFunctionCallNode) iNode;
            IExpressionNode nameNode = functionCall.getNameNode();
            if (nameNode instanceof IMemberAccessExpressionNode)
            {
                IMemberAccessExpressionNode memberAccess = (IMemberAccessExpressionNode) nameNode;
                nameNode = memberAccess.getRightOperandNode();
            }
            if (nameNode instanceof IIdentifierNode)
            {
                IIdentifierNode identifierNode = (IIdentifierNode) nameNode;
                IDefinition resolvedDef = identifierNode.resolve(project);
                if (resolvedDef != null && isXMLish(resolvedDef.getParent(), project))
                {
                    if (resolvedDef instanceof IFunctionDefinition
                            && !(resolvedDef instanceof IAccessorDefinition))
                    {
                        //method call on XML or XMLList instance
                        return resolvedDef;
                    }
                }
            }
            return null;
        }

        if (iNode instanceof IIdentifierNode)
        {
            IIdentifierNode identifierNode = (IIdentifierNode) iNode;
            IDefinition resolvedDef = identifierNode.resolve(project);
            if (resolvedDef != null && isXMLish(resolvedDef.getParent(), project))
            {
                if (resolvedDef.isPrivate() || resolvedDef.isProtected())
                {
                    //private/protected member inside the XML or XMLList class
                    return resolvedDef;
                }
            }
        }
        if (iNode instanceof IMemberAccessExpressionNode)
        {
            IMemberAccessExpressionNode memberAccess = (IMemberAccessExpressionNode) iNode;
            IExpressionNode nameNode = memberAccess.getRightOperandNode();
            return resolveXML(nameNode, project);
        }
        return null;
    }

    /**
     * Determine if the definition passed in is one of the XML types (XML or
     * XMLList) These classes are unrelated, but behave in similar manners.
     * 
     * @param def the {@link IDefinition} to check
     * @param project the {@link ICompilerProject} in which to look up types
     * @return true if definition is the built-in XML or XMLList type.
     */
    public static boolean isXMLish(IDefinition def, ICompilerProject project)
    {
        IDefinition xmlDef = project.getBuiltinType(IASLanguageConstants.BuiltinType.XML);
        IDefinition xmlListDef = project.getBuiltinType(IASLanguageConstants.BuiltinType.XMLLIST);
        return (xmlDef != null && def == xmlDef) ||
               (xmlListDef != null && def == xmlListDef);
    }
    
    /**
     * Determines if an expression is a toString() function call.
     */
    public static boolean isToStringFunctionCall(IExpressionNode node, ICompilerProject project)
    {
        if(!(node instanceof IFunctionCallNode))
        {
            return false;
        }
        IFunctionCallNode functionCall = (IFunctionCallNode) node;
    	IExpressionNode nameExpression = functionCall.getNameNode();
    	if (nameExpression instanceof IMemberAccessExpressionNode)
    	{
    		IMemberAccessExpressionNode memberAccess = (IMemberAccessExpressionNode) nameExpression;
    		IExpressionNode rightNode = memberAccess.getRightOperandNode();
            if (rightNode instanceof IIdentifierNode)
            {
                IIdentifierNode rightIdentifier = (IIdentifierNode) rightNode;
                return rightIdentifier.getName().equals("toString");
            }
    	}
    	return false;
    }

    /**
     *  Get the type of a node's base expression as a string.
     *  @param node the node whose base's type is desired.
     *  @return the type string, or null if none found.
     */
    public String getTypeOfBase(IASNode node)
    {
        return getTypeOfBase(node, project);
    }
    
    /**
     *  Get the type of a node's base expression as a string.
     *  @param node the node whose base's type is desired.
     *  @param project an ICompilerProject for the current project.
     *  @return the type string, or null if none found.
     */
    public static String getTypeOfBase(IASNode node, ICompilerProject project)
    {
        if (!(node instanceof ExpressionNodeBase))
            return null;
        
        ExpressionNodeBase base = ((ExpressionNodeBase)node).getBaseExpression();
        if (base == null)
            return null;
        
        IDefinition def = base.resolve(project);
        if (def == null)
            return null;
        
        return def.getTypeAsDisplayString();
    }
    
    /**
     *  Get the type of an expression's stem.
     *  @param iNode - the node of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the type of the node's underlying reference base as a string.
     *  @pre hasUnderlyingType(iNode, cache) must be true.
     */
    public static String getTypeOfStem(IASNode iNode, ICompilerProject project)
    {
        IDefinition type_def = getDefinitionOfUnderlyingType(iNode, ASSERT_ON_UNKNOWN_INODE, project);
        return ( type_def != null )?  type_def.getBaseName(): "?";
    }
    
    /**
     *  Get the type of an expression's stem.
     *  @param node - the node of interest.
     *  @return the type of the node's underlying reference base as a string.
     *  @see #getTypeOfStem(IASNode, ICompilerProject)
     */

    public String getTypeOfStem(IASNode node)
    {
        return getTypeOfStem(node, this.project);
    }

    /**
     *  Get the definition of the explicit or implicit type underlying
     *  an iNode's reference(s).
     *  @param iNode - the inode of interest.
     *  @param allow_unknown_iNode - tolerate a node type that doesn't
     *    have a procedure for extracting its underlying scope.
     *  @param project - an ICompilerProject for the current project.
     *  @return the definition of the type that underlies the node's references,
     *    or null if no underlying scope is available.
     */
    public static IDefinition getDefinitionOfUnderlyingType(IASNode iNode, final boolean allow_unknown_iNode, ICompilerProject project)
    {
        IDefinition result;

        if ( iNode instanceof MemberAccessExpressionNode )
        {
            MemberAccessExpressionNode member_node = (MemberAccessExpressionNode) iNode;
            result = member_node.getLeftOperandNode().resolveType(project);
        }
        else if ( iNode instanceof FunctionCallNode && ((FunctionCallNode)iNode).getNameNode() instanceof MemberAccessExpressionNode )
        {
            MemberAccessExpressionNode member_node = (MemberAccessExpressionNode) ((FunctionCallNode)iNode).getNameNode();
            result = member_node.getLeftOperandNode().resolveType(project);
        }
        else if ( isInInstanceFunction(iNode, project) )
        {
            result = getEnclosingFunctionDefinition(iNode, project).getAncestorOfType(ClassDefinition.class);
        }
        else if ( isInStaticClassFunction(iNode, project) )
        {
            result = getEnclosingFunctionDefinition(iNode, project).getAncestorOfType(ClassDefinition.class);
        }
        else
        {
            assert(allow_unknown_iNode): "Unknown iNode:" + iNode;
            result = null;
        }

        return result;
    }

    /**
     *  Get the definition of the explicit or implicit type underlying
     *  an iNode's reference(s).
     *  @param iNode - the inode of interest.
     *  @param allow_unknown_iNode - tolerate a node type that doesn't
     *    have a procedure for extracting its underlying scope.
     *  @return the definition of the type that underlies the node's references,
     *    or null if no underlying scope is available.
     *  @see #getDefinitionOfUnderlyingType(IASNode, boolean, ICompilerProject)
     */
    public IDefinition getDefinitionOfUnderlyingType(IASNode iNode, final boolean allow_unknown_iNode)
    {
        return getDefinitionOfUnderlyingType(iNode, allow_unknown_iNode, this.project);
    }

    /** Manifest constant, getDefinitionOfUnderlyingType asserts if this is passed */
    public static final boolean ASSERT_ON_UNKNOWN_INODE = false;
    /** Manifest constant, getDefinitionOfUnderlyingType tolerates unknown tree shapes if this is passed */
    public static final boolean ALLOW_UNKNOWN_INODE = true;

    /**
     *  Does the given inode have an underlying type that could
     *  resolve its references?
     *  @param iNode - the inode of interest.
     *  @param project - an ICompilerProject for the current project.
     */
    public static boolean hasUnderlyingType(IASNode iNode, ICompilerProject project)
    {
        return getDefinitionOfUnderlyingType(iNode, ALLOW_UNKNOWN_INODE, project) != null;
    }

    /**
     *  Does the given inode have an underlying type that could
     *  resolve its references?
     *  @param iNode - the inode of interest.
     *  @see #hasUnderlyingType(IASNode, ICompilerProject)
     */
    public boolean hasUnderlyingType(IASNode iNode)
    {
        return hasUnderlyingType(iNode, this.project);
    }

    /**
     *  Does the given node have an explicit stem reference, e.g., foo.bar?
     *  @param iNode - the node of interest.
     *  @return true if the node has an explicit stem (the foo in foo.bar).
     *  @see #hasUnderlyingType which checks for explicit or implicit contexts for resolution.
     */
    public static boolean hasExplicitStem(IASNode iNode)
    {
        return  ( iNode instanceof MemberAccessExpressionNode ) ||
                ( iNode instanceof FunctionCallNode && ((FunctionCallNode)iNode).getNameNode() instanceof MemberAccessExpressionNode );
    }

    /**
     *  Does this binding have a base node?
     *  @param binding - the Binding of interest.
     *  @return true if the Binding's i-node has a base node.
     */
    public static boolean hasBaseNode(Binding binding)
    {
        return getBaseNode(binding) != null;
    }

    /**
     *  Is this node enclosed in a function definition?
     *  @param iNode - the node of interest.
     *  @return true if the node is in a function definition.
     */
    public static boolean isInFunction(IASNode iNode)
    {
        return iNode.getAncestorOfType(FunctionNode.class) != null;
    }

    /**
     * Is this node declaring a property in a class, but also nested inside
     * another statement.
     * @param iNode the node
     * @param def   the
     */
    public static boolean isNestedClassProperty(IASNode iNode, VariableDefinition def)
    {
        if(def != null && def.getParent() instanceof IClassDefinition)
        {
            return def.declaredInControlFlow();
        }
        return false;
    }

    /**
     *  Is this container an implicit container?
     *  @param iNode - the container of interest.
     *  @return true if the container is implicit.
     */
    public static boolean isImplicitContainer(IASNode iNode)
    {
        //  TODO: The result of CMP-1004 goes here.
        return
            ( iNode instanceof IContainerNode ) &&
            false;
          
    }
    
   
    
    /**
     *  Is the given multiply defined, or ambiguous?
     *  @param def - the definition of interest.
     *  @param project - the active compiler project.
     *  @return An enum telling the nature of any discovered multple definitions
     */
    public static MultiDefinitionType getMultiDefinitionType(IDefinition def, ICompilerProject project)
    {
        // A null definition cannot be ambiguous.
        if (def == null)
           return MultiDefinitionType.NONE;
        
        // Look for other definitions with the same base name
        // and the same namespace as the specified definition.
        IDefinition found = findPropertyQualified(def, project);
        
        if (AmbiguousDefinition.isAmbiguous(found))
            return MultiDefinitionType.AMBIGUOUS;
      
        // if we found something, andit's not us, and it's not null, then there are multi deinitions.
        if (def != found && found != null && !isGetterSetterPair(def, found, project))
        {
            return found instanceof IParameterDefinition ? MultiDefinitionType.SHADOWS_PARAM :
                    MultiDefinitionType.MULTIPLE;
        }
        
        return MultiDefinitionType.NONE ;
    }


    /**
     * Determine if a given binding is ambiguous
     * @param b  the binding to check
     * @return   true if the binding refers to more than 1 definiton
     */
    public static boolean isAmbiguousReference(Binding b)
    {
        return AmbiguousDefinition.isAmbiguous(b.getDefinition());
    }

    /**
     *  Does this node represent the "this" keyword?
     *  @param iNode - the node of interest.
     *  @return true if the node represents "this"
     */
    public static boolean isThisKeyword(IASNode iNode)
    {
        return 
            (iNode instanceof ILanguageIdentifierNode) &&
            (((ILanguageIdentifierNode)iNode).getKind() == LanguageIdentifierKind.THIS);
    }

    /**
     *  Is this function node contained within another function node?
     *  @param iNode - the node of interest.
     *  @return true if the function node is a closure
     */
    public static boolean isFunctionClosure(IFunctionNode functionNode)
    {
        IScopedNode containingScope = functionNode.getContainingScope();
        if (containingScope == null)
        {
            return false;
        }
        return containingScope.getParent() instanceof IFunctionNode;
    }

    /**
     *  Is the given node in a class with a base class definition?
     *  @param iNode - the node of interest.
     *  @return true if the node is in a class with a base class.
     *  @see #hasBaseClassDefinition(IASNode, ICompilerProject)
     */
    public boolean hasBaseClassDefinition(IASNode iNode)
    {
        return hasBaseClassDefinition(iNode, this.project);
    }

    /**
     *  Is the given node in a class with a base class definition?
     *  @param iNode - the node of interest.
     *  @param project - the active compiler project.
     *  @return true if the node is in a class with a base class.
     */
    public static boolean hasBaseClassDefinition(IASNode iNode, ICompilerProject project)
    {
        return getBaseClassDefinition(iNode, project) != null;
    }

    /**
     *  Fetch the definition of the given node's
     *    class' base class.
     *  @param iNode - the node of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the definition of the node's class' base class,
     *    or null if it not in a class or its class 
     *    has no base class.
     */
    public static IDefinition getBaseClassDefinition(IASNode iNode, ICompilerProject project)
    {
        IDefinition result = null;
        IDefinition member_def = getDefinition(iNode, project);

        if ( member_def != null )
        {
            ASScope super_scope = getSuperClassScope(project, member_def);
            if( super_scope != null )
            {
                result = getPropertyQualified(
                    super_scope,
                    getNamespaceInClassContext(member_def, project),
                    member_def.getBaseName(),
                    project
                );
            }
        }

        return result;
    }

    /**
     *  Resolve a definition's namespace, and if its parent is a class, normalize
     *  that namespace against the parent class' superclass' protected namespace.
     *  @param def - the definition of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the definition's class' superclass' namespace if the definition is
     *    in a class and its namespace is the same as the enclosing class' superclass'
     *    protected namespace; otherwise, return the definition's namespace.
     */
    public static INamespaceDefinition getNamespaceInClassContext(IDefinition def, ICompilerProject project)
    {
        INamespaceDefinition namespace = def.resolveNamespace(project);

        IDefinition parent = def.getParent();

        if ( namespace != null && parent instanceof ClassDefinition )
        {
            // Iterate over the superclasses of this method's class.
            ClassDefinition cls  = (ClassDefinition)parent;
            ClassDefinition base = (ClassDefinition)cls.resolveBaseClass(project);

            if (base != null)
            {
                // Adjust the namespace if this is the protected namespace
                INamespaceDefinition protectedNS = cls.getProtectedNamespaceReference().resolveNamespaceReference(project);
                if (namespace.equals(protectedNS))
                    namespace = base.getProtectedNamespaceReference().resolveNamespaceReference(project);
            }
        }

        return namespace;
    }
    
    /**
     * Get the super class scope for the definition passed in.  This will return an ASScope
     * for the super class of whatever class declared the member passed in.
     * @param project       The project to resolve references in
     * @param member_def    The member to find a super class for
     * @return              The scope of the base class of the class the member_def is declared in.
     *                      This will return null if the member_def was not declared in a class.
     */
    public static ASScope getSuperClassScope(ICompilerProject project, IDefinition member_def)
    {
        IASScope containingScope = member_def.getContainingScope();
        if( containingScope instanceof ScopeView )
        {
            return ((ScopeView) containingScope).resolveSuperScope(project);
        }
        return null;
    }

    /**
     *  Find a member definition, given its name.
     *  @param iNode - the node that forms the reference.
     *    Should be a MemberAccessExpressionNode to get any useful result.
     *  @param member_name - the name of the member of interest.
     *  @param match_static_to_static - when true, only accept definitions 
     */
    public static IDefinition findMemberByName(IASNode iNode, String member_name,  boolean match_static_to_static, ICompilerProject project)
    {
        //  Find a scope to work with.
        IASScope starting_scope = null;

        //  Is this a static reference, e.g., ClassName.staticField?
        boolean is_static_reference = false;

        if ( iNode instanceof MemberAccessExpressionNode )
        {
            TypeDefinitionBase type = (TypeDefinitionBase)((MemberAccessExpressionNode)iNode).getLeftOperandNode().resolveType(project);

            if ( type != null )
            {
                starting_scope = ((TypeDefinitionBase)((MemberAccessExpressionNode)iNode).getLeftOperandNode().resolveType(project)).getContainedScope();
                is_static_reference = ((MemberAccessExpressionNode)iNode).getLeftOperandNode().resolve(project) instanceof ClassDefinition;
            }
        }
        else if ( iNode instanceof FunctionCallNode && ((FunctionCallNode)iNode).getNameNode() instanceof MemberAccessExpressionNode )
        {
            MemberAccessExpressionNode member_node = (MemberAccessExpressionNode) ((FunctionCallNode)iNode).getNameNode();
            
            TypeDefinitionBase type = (TypeDefinitionBase)member_node.getLeftOperandNode().resolveType(project);
            if ( type != null )
            {
                starting_scope = type.getContainedScope();
                is_static_reference = member_node.getLeftOperandNode().resolve(project) instanceof ClassDefinition;
            }
        }
        else if ( isInInstanceFunction(iNode, project) )
        {
            starting_scope = getClassScope(iNode, project);
            is_static_reference = false;
        }
        else if ( isInStaticClassFunction(iNode, project) )
        {
            starting_scope = getClassScope(iNode, project);
            is_static_reference = true;
        }

        //  Did we find a scope to work with?
        if ( starting_scope != null )
            return findMemberByName(starting_scope, member_name, match_static_to_static, is_static_reference, project);
        else
            return null;
    }

    /**
     *  Get the ASScope of the class that contains an inode.
     *  @param iNode - the inode of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the ASScope (holding both static and instance definitions)
     *    of the class enclosing the inode, or null if no such scope is found.
     */
    public static ASScope getClassScope(IASNode iNode, ICompilerProject project)
    {
        if ( isInInstanceFunction(iNode, project) || isInStaticClassFunction(iNode, project) )
        {
            ClassDefinition class_def = (ClassDefinition) getEnclosingFunctionDefinition(iNode, project).getAncestorOfType(ClassDefinition.class);
            if ( class_def != null )
                return class_def.getContainedScope();
        }

        return null;
    }

    /**
     *  Get the ASScope of the class that contains an inode.
     *  @param iNode - the inode of interest.
     *  @return the ASScope (holding both static and instance definitions)
     *    of the class enclosing the inode, or null if no such scope is found.
     *  @see #getClassScope(IASNode, ICompilerProject)
     */
    public ASScope getClassScope(IASNode iNode)
    {
        return getClassScope(iNode, this.project);
    }

    /**
     *  Search a scope and its enclosing scopes for a member whose name is known.
     *  @param scope - the innermost scope to search.
     *  @param member_name - the (string) name of interest.
     *  @param match_static_to_static - if set, then only return static definitions if is_static_reference is set.
     *  @param is_static_reference - caller sets this if the reference occurs in a context that is known to be static.
     *  @param project - an ICompilerProject for the current project.
     */
    public static IDefinition findMemberByName(IASScope scope, String member_name,  boolean match_static_to_static, boolean is_static_reference, ICompilerProject project)
    {
        if ( scope == null )
            return null;
        
        IDefinition result = null;

        //  A definition by the right name but in the wrong scope
        //  (e.g., an instance def found while looking for a static def).
        IDefinition possible_result = null;

        for ( IDefinition def: getPropertiesByNameForMemberAccess(scope, member_name, project) )
        {
            //  Check for matching scope: static references should only
            //  match static definitions, and vice versa.
            //  Note: the diagnostic could be improved by doing a subsequent
            //  check for definitions with matching names and conflicting scopes
            //  (i.e., a static reference to an instance field).
            if ( is_static_reference )
            {
                if ( def.isStatic() )
                {
                    result = def;
                    break;
                }
                else if ( match_static_to_static && possible_result != null )
                {
                    possible_result = def;
                    //  Keep looking for a static definition as a better match.
                    continue;
                }
            }
            else 
            {
                if ( !def.isStatic() )
                {
                    result = def;
                    break;
                }
            }
        }

        return result != null? result: possible_result;
    }

    /**
     *  Is the given binding an inaccessible reference?
     *  @param iNode - the iNode that anchors the reference.
     *  @param member - the Binding.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if the binding has no definition, but
     *    a search for the member by name in the iNode's 
     *    underlying type scopes finds something.
     */
    public static boolean isInaccessible(IASNode iNode, Binding member, ICompilerProject project)
    {
        assert(member.getName() != null);
        return member.getDefinition() == null && findMemberByName(iNode, member.getName().getBaseName(), true, project) != null;
    }
    
    /**
     *  Is the given binding an inaccessible reference?
     *  @param iNode - the iNode that anchors the reference.
     *  @param member - the Binding.
     *  @return true if the binding has no definition, but
     *    a search for the member by name in the iNode's 
     *    underlying type scopes finds something.
     *  @see #isInaccessible(IASNode, Binding, ICompilerProject)
     */
    public boolean isInaccessible(IASNode iNode, Binding member)
    {
        return isInaccessible(iNode, member, this.project);
    }


    /**
     *  Fetch the definiton of the given node's
     *    class' base class.
     *  @param iNode - the node of interest.
     *  @return the definition of the node's class' base class,
     *    or null if it not in a class or its class 
     *    has no base class.
     *  @see #getBaseClassDefinition(IASNode, ICompilerProject)
     */
    public IDefinition getBaseClassDefinition(IASNode iNode)
    {
        return getBaseClassDefinition(iNode, this.project);
    }

    /**
     *  Find a property in a defintion's containing scope.
     *  @param member_def - the definition of interest.
     *  @param project - the active compiler project.
     *  @return the definition found in the containing scope, or null if not found.
     */
    public static IDefinition findPropertyQualified(IDefinition member_def, ICompilerProject project)
    {
        ASScope containingScope = (ASScope)member_def.getContainingScope();
        return findPropertyQualified(containingScope, member_def, project);
    }

    /**
     *  Find a property in a defintion's containing scope.
     *  @param member_def - the definition of interest.
     *  @return the definition found in the containing scope, or null if not found.
     *  @see #findPropertyQualified(IDefinition, ICompilerProject)
     */
    public IDefinition findPropertyQualified(IDefinition member_def)
    {
        return findPropertyQualified(member_def, this.project);
    }

    /**
     *  Find a property in a (presumably) enclosing defintion's scope.
     *  @param enclosing_def - the definition to query.
     *  @param member_def - the prototype definition of the member desired.
     *  @param project - the active compiler project.
     *  @return the definition found in the enclosing scope, or null if not found.
     */
    public static IDefinition findPropertyQualified(IScopedDefinition enclosing_def, IDefinition member_def, ICompilerProject project)
    {
        return findPropertyQualified((ASScope)enclosing_def.getContainedScope(), member_def, project);
    }

    public static IDefinition getPropertyQualified(IScopedDefinition enclosing_def, IDefinition member_def, ICompilerProject project)
    {
        return getPropertyQualified((ASScope)enclosing_def.getContainedScope(), member_def, project);
    }

    /**
     *  Find a property in a (presumably) enclosing defintion's scope.
     *  @param enclosing_def - the definition to query.
     *  @param member_def - the prototype definition of the member desired.
     *  @return the definition found in the enclosing scope, or null if not found.
     *  @see #findPropertyQualified(IScopedDefinition, IDefinition, ICompilerProject)
     */
    public IDefinition findPropertyQualified(IScopedDefinition enclosing_def, IDefinition member_def)
    {
        return findPropertyQualified(enclosing_def, member_def, this.project);
    }

    /**
     *  Find a qualified property in a scope.
     *  @param scope - the scope to query.
     *  @param member_def - the prototype definition of the member desired.
     *  @param project - the active compiler project.
     *  @return the definition found in the enclosing scope, or null if not found.
     */
    public static IDefinition findPropertyQualified(ASScope scope, IDefinition member_def, ICompilerProject project)
    {
        IDefinition result = null; 

        if ( scope != null && member_def != null && member_def.getNamespaceReference() != null )
        {
            INamespaceDefinition namespace =
                member_def.getNamespaceReference().resolveNamespaceReference(project);
            if (namespace != null)
            {
                String baseName = member_def.getBaseName();

                result = scope.findPropertyQualified(
                        project, namespace, baseName, DependencyType.EXPRESSION);
            }
        }

        return result;
    }

    /**
     * Get a qualified property in a scope - this will look for properties with the same qualified name as member_def
     * in the scope passed in.
     * @param scope         the scope to look for the property in
     * @param member_def    the prototype definition of the member desired
     * @param project       the active compiler project
     * @return              the definition found in the passed in scope ,or null if not found
     */
    public static IDefinition getPropertyQualified(ASScope scope, IDefinition member_def, ICompilerProject project)
    {
        IDefinition result = null;

        if ( scope != null && member_def != null && member_def.getNamespaceReference() != null )
        {
            INamespaceDefinition namespace =
                    member_def.getNamespaceReference().resolveNamespaceReference(project);

            if (namespace != null)
            {
                result = getPropertyQualified( scope, namespace, member_def.getBaseName(), project);
            }
        }

        return result;
    }

    /**
     * Get a qualified property in a scope, given the namespace and base name to search for.
     * @param scope         the scope to look for the property in
     * @param namespace     the namespace of the member desired
     * @param baseName      the base name of the member desired
     * @param project       the active compiler project
     * @return              the definition found in the passed in scope ,or null if not found
     */
    public static IDefinition getPropertyQualified(ASScope scope, INamespaceDefinition namespace, String baseName, ICompilerProject project)
    {
        IDefinition result = null;

        if ( scope != null && namespace != null && baseName != null )
        {
            result = scope.getPropertyByNameForMemberAccess(
                (CompilerProject)project,
                baseName,
                Collections.singleton(namespace)
            );
        }

        return result;
    }

    /**
     *  Find a qualified property by name in a scope.
     *  @param scope - the scope to query.
     *  @param member_name - the name of the desired member.
     *  @param project - the active compiler project.
     *  @return the definition found in the scope, or null if not found.
     */
    public static IDefinition findProperty(ASScope scope, String member_name, ICompilerProject project, boolean lookForStatics)
    {
        IDefinition result = null; 

        if ( scope != null )
        {
            result = scope.findProperty(
                project,
                member_name,
                DependencyType.EXPRESSION,
                false
            );
        }

        return result;
    }

    /**
     *  Find the definition of the function that encloses the given node, if any.
     *  @param iNode - the inode of interest. 
     *  @param project - an ICompilerProject for the current project.
     *  @return the definition of any function that encloses the inode, or null
     *    if not found (or if the function is undefined for some reason).
     */
    public static FunctionDefinition getEnclosingFunctionDefinition(IASNode iNode, ICompilerProject project)
    {
        IASNode func_node = iNode.getAncestorOfType(FunctionNode.class);

        if ( func_node != null )
        {
            return  (FunctionDefinition) getDefinition(func_node, project);
        }

        return null;
    }

    /**
     *  Find the definition of the function that encloses the given node, if any.
     *  @param iNode - the inode of interest. 
     *  @return the definition of any function that encloses the inode, or null
     *    if not found (or if the function is undefined for some reason).
     *  @see #getEnclosingFunctionDefinition(IASNode, ICompilerProject)
     */
    public FunctionDefinition getEnclosingFunctionDefinition(IASNode iNode)
    {
        return getEnclosingFunctionDefinition(iNode, this.project);
    }

    /**
     *  Is the given inode in an instance function?
     *  @param iNode - the inode of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if the inode has an enclosing function
     *    and that function is a non-static class member.
     */
    public static boolean isInInstanceFunction(IASNode iNode, ICompilerProject project)
    {
        FunctionDefinition func_def = getEnclosingFunctionDefinition(iNode, project);
        return func_def != null && func_def.getFunctionClassification() == FunctionClassification.CLASS_MEMBER && !func_def.isStatic();
    }

    
    /**
     *  Is the given inode in an instance function?
     *  @param iNode - the inode of interest.
     *  @return true if the inode has an enclosing function
     *    and that function is a non-static class member.
     *  @see #isInInstanceFunction(IASNode, ICompilerProject)
     */
    public boolean isInInstanceFunction(IASNode iNode)
    {
        return isInInstanceFunction(iNode, this.project);
    }

    /**
     *  Is the given inode in a static function?
     *  @param iNode - the inode of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if the inode has an enclosing function
     *    and that function is a static class member.
     */
    public static boolean isInStaticClassFunction(IASNode iNode, ICompilerProject project)
    {
        FunctionDefinition func_def = getEnclosingFunctionDefinition(iNode, project);
        return func_def != null && func_def.getFunctionClassification() == FunctionClassification.CLASS_MEMBER && func_def.isStatic();
    }


    /**
     *  Is the given Binding inaccessible, i.e., known to 
     *  an underlying type scope but not defined here
     *  due to access restrictions?
     *  @param scope - the innermost underlying type scope.
     *  @param b - the Binding of interest
     *  @param project - an ICompilerProject for the current project.
     */
    private static boolean isInaccessible(IASScope scope, Binding b, ICompilerProject project)
    {
        return b.getDefinition() == null && findMemberByName(scope, b.getName().getBaseName(), true, false, project) != null;
    }

    /**
     *  Is the given Binding inaccessible, i.e., known to 
     *  an underlying type scope but not defined here
     *  due to access restrictions?
     *  @param containingScope - the innermost underlying type scope.
     *  @param b - the Binding of interest
=     */
    public boolean isInaccessible(ASScope containingScope, Binding b)
    {
        return isInaccessible(containingScope,  b, this.project);
    }

    /**
     *  Get the name of the class enclosing an instance function in which 
     *  some problematic construct resides.
     *  @param iNode - the problematic construct.
     *  @param project - an ICompilerProject for the current project.
     *  @return the name of the enclosing class.
     */
    public static String getEnclosingClassName(IASNode iNode, ICompilerProject project)
    {
        assert(isInInstanceFunction(iNode, project));  //  assert precondition.

        IDefinition class_def = getEnclosingFunctionDefinition(iNode, project).getAncestorOfType(ClassDefinition.class);
        return class_def.getBaseName();
    }

    /**
     *  Get the name of the class enclosing an instance function in which 
     *  some problematic construct resides.
     *  @param iNode - the problematic construct.
     *  @return the name of the enclosing class.
     *  @see #getEnclosingClassName(IASNode, ICompilerProject)
     */
    public String getEnclosingClassName(IASNode iNode)
    {
        return getEnclosingClassName(iNode, this.project);
    }

    /**
     *  Fetch the collection of definitions in the given scope or its underlying scopes
     *  that match the given name.
     *  @param scope - the innermost scope to search.
     *  @param member_name - the (string) name of interest.
     *  @param project - an ICompilerProject for the current project.
     */
    public static Collection<IDefinition> getPropertiesByNameForMemberAccess(IASScope scope, String member_name, ICompilerProject project)
    {
        assert(scope != null);  //  check precondition.
        return ((ASScope)scope).getPropertiesByNameForMemberAccess(
            (CompilerProject)project, 
            member_name, 
            ASScopeBase.allNamespacesSet
        );
    }

    /**
     *  Get an abstract string description of a node, suitable for a diagnostic.
     *  @return the best guess we can come up with for a node's user-friendly string representation.
     */
    public static String getDiagnosticString(IASNode iNode)
    {
        String content = null;

        switch ( iNode.getNodeID() )
        {
            case IdentifierID:
            case NamespaceIdentifierID:
            case NonResolvingIdentifierID:
            case QualifiedNameExpressionID:
                content = ((IdentifierNode)iNode).getName();
                break;

            case FunctionCallID:
            {
                FunctionCallNode func = (FunctionCallNode)iNode;
                
                if ( func.getNameNode() != null )
                {
                    // return to avoid quoting the result twice.
                    return getDiagnosticString(func.getNameNode());
                }
                else
                {
                    content = iNode.getNodeID().getParaphrase();
                }
                
                break;
            }

            case LiteralArrayID:
            case LiteralBooleanID:
            case LiteralNumberID:
            case LiteralObjectID:
            case LiteralRegexID:
            case LiteralStringID:
            case LiteralXMLID:
            case LiteralID:
                content = ((LiteralNode)iNode).getValue();
                break;

            case LiteralNullID:
                content = "null";
                break;

            case LiteralVoidID:
                content = "void";
                break;

            case MemberAccessExpressionID:
                content = ((MemberAccessExpressionNode)iNode).getDisplayString();
                break;

            default:
                content = iNode.getNodeID().getParaphrase();
        }

        return  "'" + content + "'";
    }

    /**
     *  Get the set of open namespaces for the given node.  
     *  @param iNode - the node of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the open namespaces at this node as a Nsset,
     *    or null if the node doesn't have a containing scope.
     */
    public static Nsset getOpenNamespaces(IASNode iNode, ICompilerProject project)
    {
        ASScope scope = getASScope(iNode);
        if (scope == null)
            return null;

        Set<INamespaceDefinition> namespaceSet = scope.getNamespaceSet(project);
        Nsset nsSet = convertSetINamespaceToNsset(namespaceSet);
        return nsSet;        
    }
    
    /**
     *  Get the set of open namespaces for the given node,
     *  as appropriate for <code>super[...]</code> access.
     *  @param iNode - the node of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the open namespaces at this node as a Nsset,
     *    or null if the node doesn't have a containing scope.
     */
    public static Nsset getOpenNamespacesForSuper(IASNode iNode, ICompilerProject project,
                                                  IDefinition superDef)
    {
        ASScope scope = getASScope(iNode);
        if (scope == null)
            return null;

        Set<INamespaceDefinition> namespaceSet = scope.getNamespaceSetForSuper(project, superDef);
        Nsset nsSet = convertSetINamespaceToNsset(namespaceSet);
        return nsSet;        
    }
    

    /**
     *  Convert a set of INamespaceDefinition objects into a Nsset.
     *  @param namespaceSet - the set of INamespaceDefinition objects.
     *  @return the namespaces as a Nsset.
     */
    public static Nsset convertSetINamespaceToNsset(Set<INamespaceDefinition> namespaceSet)
    {
        ArrayList<Namespace> ns_set = new ArrayList<Namespace>(namespaceSet.size());

        for (INamespaceDefinition namespace : namespaceSet)
        {
            Namespace aetNamespace = ((NamespaceDefinition)namespace).getAETNamespace();
            ns_set.add(aetNamespace);
        }

        return new Nsset(ns_set);
    }

    /**
     *  Get the given node's containing scope.
     *  (Convenience method that hides a downcast.)
     *  @param iNode - the node of interest.
     *  @return the node's scope, or null if not available.
     */
    public static ASScope getASScope(IASNode iNode)
    {
        return ((NodeBase)iNode).getASScope();
    }

    /**
     *  Is the given definition const?
     *  @param iNode - the root of the definition.
     *  @param project - an ICompilerProject for the current project.
     *  @return true if iNode can be resolved to a const definition.
     */
    public static boolean isConst(IASNode iNode, ICompilerProject project)
    {
        return isConstDefinition(getDefinition(iNode, project));
    }

    /**
     *  Is the given definition const?
     *  @param iNode - the root of the definition.
     *  @return true if iNode can be resolved to a const definition.
     *  @see #isConst(IASNode iNode,  ICompilerProject cache)
     */
    public boolean isConst(IASNode iNode)
    {
        return isConst(iNode, this.project);
    }

    /**
     *  Is the given definition a constant?
     *  @param def - the definition of interest.
     *  @return true if the definition is of a const.
     */
    public static boolean isConstDefinition(IDefinition def)
    {
        return def instanceof IConstantDefinition;
    }

    /**
     *  Is the given node in a constructor?
     *  @param iNode - the i-node of interest.
     *  @return true if the node is in a constructor function.
     */
    @SuppressWarnings("unchecked")
    public static boolean isInConstructor(IASNode iNode)
    {
        FunctionNode fnode = (FunctionNode) searchUpForType(iNode, FunctionNode.class);
        return fnode != null && fnode.isConstructor();
    }

    /**
     *  Is the given i-node inside a variable declaration?
     *  @param iNode - the i-node of interest.
     *  @return true if the node has a VariableNode or VariableExpressionNode parent.
     */
    @SuppressWarnings("unchecked")
    public static boolean isInVariableDeclaration(IASNode iNode)
    {
        return searchUpForType(iNode, VariableNode.class, VariableExpressionNode.class) != null;
    }

    /**
     *  Search a node's parent chain for an ancestor of any of a group of classes.
     *  @param iNode - the node of interest.
     *  @param desired - the group of classes desired.
     *  @return the first node in the node and its ancestors that matches a desired class.
     */
    private static IASNode searchUpForType(IASNode iNode, Class<? extends IASNode> ... desired)
    {
        if ( iNode == null )
            return null;

        IASNode found = null;
        for ( Class<? extends IASNode> candidate: desired)
        {
            if ( candidate.isInstance(iNode) )
                found = iNode;
            else
                found = iNode.getAncestorOfType(candidate);

            if ( found != null )
                break;
        }

        return found;
    }

    /**
     *  Fetch the most appropriate part of a type name for diagnostics.
     *  @return the type part of a parameterized type, or the input node.
     */
    public IASNode getPotentiallyParameterizedType(IASNode iNode)
    {
        IASNode result = iNode;

        if ( iNode instanceof TypedExpressionNode )
        {
            result = ((TypedExpressionNode)iNode).getTypeNode();
        }

        return result;
        
    }

    /**
     *  Get the Nth child of a node per the BURM's semantics.
     */
	public static IASNode getNthChild( IASNode node, int index)
	{
        IASNode result = null;
        
        switch( node.getNodeID() )
        {
            case BindableVariableID:
            case VariableID:
            {
                IVariableNode var = (IVariableNode) node;
                switch( index )
                {
                    case 0:
                    {
                        result = var.getNameExpressionNode();
                        break;
                    }
                    default:
                    {
                        //  We have to go hunt among the children
                        //  for the nodes that are relevant to the BURM.
                        int lastFoundChildPos = 0;

                        result = var.getVariableTypeNode();
                        if ( result != null )
                        {
                            lastFoundChildPos++;
                            if ( index == lastFoundChildPos)
                                break;
                        }

                        result = var.getAssignedValueNode();
                        if ( result != null )
                        {
                            lastFoundChildPos++;
                            if ( index == lastFoundChildPos)
                                break;
                        }

                        //  Look for chained variable declarations.
                        int needle = 0;
                        while ( needle < node.getChildCount() && lastFoundChildPos < index )
                        {
                            if ( node.getChild(needle) instanceof IVariableNode )
                                lastFoundChildPos++;
                            if ( lastFoundChildPos < index )
                                needle++;
                        }

                        assert(lastFoundChildPos == index): "getNthChild() failed, should have been constrained by getChildCount(node)";
                        return ( node.getChild(needle));
                    }
                    
                }
                break;
            }
            case FunctionID:
            case GetterID:
            case SetterID:
            {
                FunctionNode func = (FunctionNode) node;
                switch( index )
                {
                    case 0:
                        result = func.getNameExpressionNode();
                        break;
                    case 1:
                        result = func.getParametersContainerNode();
                        break;
                    case 2:
                        result = func.getReturnTypeNode();
                        if ( result != null )
                            break;
                    case 3:
                        assert (func.hasBeenParsed()) : "getScopedNode() called on a function before the body has been parsed";
                        result = func.getScopedNode();
                        break;
                }
                break;
            }
            case Op_CommaID:
            {
                switch( index )
                {
                    case 0:
                    {
                        result = node.getChild(node.getChildCount()-1);
                        break;
                    }
                    default:
                        result = node.getChild(index - 1);
                    
                }
                break;
            }
            case TryID:
			{
                ITryNode tryNode = (ITryNode) node;
				switch( index )
				{
					case 0:
					{
						result = tryNode.getStatementContentsNode();
						break;
					}
					case 1:
					{
						if  ( tryNode.getFinallyNode() != null )
                        {
                            result = tryNode.getFinallyNode();
                        }
                        else 
                        {
                            assert ( tryNode.getCatchNodeCount() > 0 );
                            result = tryNode.getCatchNode(0);
                        }
                        break;
					}
					default:
					{
                        //  Note: If the try has a contents and finally nodes,
                        //  they are presented to the CG by getNthChild() as child
                        //  nodes 0 and 1 before the n-ary tail of catch nodes.
                        if (tryNode.getStatementContentsNode() != null)
                            index--;
                        if (tryNode.getFinallyNode() != null)
                            index--;
                        result = tryNode.getCatchNode(index);
                    }
				}
				break;
			}
            case NamespaceID:
            {
                // Skip over MetaTagsNode and NamespaceIdentifierNode
                final NamespaceNode nsNode = (NamespaceNode)node;
                final IASNode nsName = nsNode.getNameExpressionNode();
                final IASNode nsURI = nsNode.getNamespaceURINode();
                switch(index)
                {
                    case 0:
                        if(nsName != null)
                            result = nsName;
                        else 
                            result = nsURI;
                        break;
                    case 1:
                        if(nsName != null && nsURI != null)
                            result = nsURI;
                        else 
                            throw new ArrayIndexOutOfBoundsException(index);
                        break;
			default:
                        throw new ArrayIndexOutOfBoundsException(index);
                }
                break;
            }
            default:
			{
				result = node.getChild(index);
				break;
			}
		}

        assert(result != null): String.format("getNthChild(%s,%d) null result", node.getNodeID(), index);
        return result;
    }

    /**
     *  Get the child count for nodes whose
     *  CodeModel-centric child count isn't 
     *  reliable.
     */
    public static int getChildCount(IASNode node)
    {
        int result = 0;
        
        switch( node.getNodeID() )
        {
            case BindableVariableID:
            case VariableID:
            {
                IVariableNode var = (IVariableNode) node;

                assert ( var.getNameExpressionNode() != null ): "Variable has null name expression.";
                result = 1;

                if ( var.getVariableTypeNode() != null )
                    result++;

                if ( var.getAssignedValueNode() != null )
                    result++;

                //  Scan for chained declarations.
                for ( int i = 0; i < node.getChildCount(); i++ )
                {
                    if ( node.getChild(i) instanceof IVariableNode )
                        result++;
                }
                break;
            }
            case FunctionID:
            case GetterID:
            case SetterID:
            {
                FunctionNode func = (FunctionNode)node;
                assert ( func.getNameExpressionNode() != null ): "Function has null name expression";
                assert ( func.getParametersContainerNode() != null ): "Function has null parameters";
                result = 2;

                if ( func.getReturnTypeNode() != null )
                    result++;

                if ( func.getScopedNode() != null )
                    result++;
            }
            break;
            case NamespaceID:
            {
                final NamespaceNode nsNode = (NamespaceNode)node;
                assert nsNode.getNameExpressionNode() != null : "'name' node is required";
                result = 1;
                if (nsNode.getNamespaceURINode() != null)
                    result = 2;
                break;
            }
            default:
                result = node.getChildCount();
        }
        return( result);
   	}

    /**
     *  @return a parameter's definition.
     *  @param iNode - the parameter node.
     */
    public static IDefinition getParameterContent(IASNode iNode)
    {
        return ((( IParameterNode )iNode).getDefinition());
    }
    
    /**
     *  @return the string content of a literal.
     *  @param iNode - the literal node.
     */
    public static String getStringLiteralContent(IASNode iNode)
    {
        return (( ILiteralNode )iNode).getValue();
    }

    /**
     *  @return the uint content of a numeric literal.
     *  @param iNode - the literal node.
     */
    public static Long getUintContent(IASNode iNode)
    {
        return Long.valueOf((( INumericLiteralNode )iNode).getNumericValue().toUint32());
    }

    /**
     *  @return the double content of a numeric literal.
     *  @param iNode - the literal node.
     */
    public static Double getDoubleContent(IASNode iNode)
    {
        return Double.valueOf((( INumericLiteralNode )iNode).getNumericValue().toNumber());
    }

    /**
     *  @return the boolean content of a literal.
     *  @param iNode - the literal node.
     */
    public static Boolean getBooleanContent(IASNode iNode)
    {
        if ( "true".equalsIgnoreCase(getStringLiteralContent(iNode)) )
        {
            return Boolean.TRUE;
        }
        else
            return Boolean.FALSE;
    }

    /**
     *  @return the name of an identifier.
     *  @param iNode - the IIdentifier node.
     */
    public static String getIdentifierContent(IASNode iNode)
    {
        return (( IIdentifierNode )iNode).getName();
    }

    /**
     *  @return the int content of a numeric literal.
     *  @param iNode - the literal node.
     */
    public static Integer getIntegerContent(IASNode iNode)
    {
        return Integer.valueOf((( INumericLiteralNode )iNode).getNumericValue().toInt32());
    }

    /**
     *  @return a node cast to MXMLEventSpecifierNode type.
     *  @param iNode - the MXMLEventSpecifierNode node.
     */
    public static IMXMLEventSpecifierNode getMXMLEventSpecifierContent(IASNode iNode)
    {
        return (IMXMLEventSpecifierNode)iNode;
    }

    /**
     *  Explore a MemberAccessNode and decide if
     *  its stem is a referenceto a package.
     *  @return a "feasible" cost of this node
     *    is of the form foo.bar, where foo is a package.
     */
    public static int isDottedName(IASNode n)
    {
        if ( n instanceof MemberAccessExpressionNode && ((MemberAccessExpressionNode)n).stemIsPackage() )
            return 1;

        //  Return "unfeasible" cost.
        return Integer.MAX_VALUE;
    }
    
    /**
     *  Get the definition associated with a node's qualifier
     *  and decide if the qualifier is a compile-time constant.
     *  @param iNode - the node to check.
     *  @pre - the node has an IdentifierNode 0th child.
     *  @return an attractive cost if the child has a known namespace, i.e.,
     *    it's a compile-time constant qualifier.
     */
    public static int qualifierIsCompileTimeConstant(IASNode iNode, ICompilerProject project)
    {
        IdentifierNode qualifier = (IdentifierNode) SemanticUtils.getNthChild(iNode, 0);
        IDefinition def = qualifier.resolve(project);

        if ( def instanceof NamespaceDefinition )
            return 1;
        else
            return Integer.MAX_VALUE;
    }

    /**
     *  Get the definition associated with a node's qualifier
     *  and decide if the qualifier is a compile-time constant.
     *  @param iNode - the node to check.
     *  @pre - the node has an IdentifierNode 0th child.
     *  @return an attractive cost if the child has a known namespace, i.e.,
     *    it's a compile-time constant qualifier.
     *  @see #qualifierIsCompileTimeConstant(IASNode, ICompilerProject)
     */
    public int qualifierIsCompileTimeConstant(IASNode iNode)
    {
        return qualifierIsCompileTimeConstant(iNode, this.project);
    }

    /**
     *  Get the definition associated with a node's qualifier
     *  and decide if the qualifier is an interface name.
     *  @param iNode - the node to check.
     *  @pre - the node has an IdentifierNode 0th child.
     *  @return an attractive cost if the child is an interface.
     */
    public static int qualifierIsInterface(IASNode iNode, ICompilerProject project)
    {
        IdentifierNode qualifier = (IdentifierNode) SemanticUtils.getNthChild(iNode, 0);
        IDefinition def = qualifier.resolve(project);

        if ( def instanceof InterfaceDefinition )
            return 1;
        else 
            return Integer.MAX_VALUE;
    }

    /**
     *  Get the definition associated with a node's qualifier
     *  and decide if the qualifier is an interface name.
     *  @param iNode - the node to check.
     *  @pre - the node has an IdentifierNode 0th child.
     *  @return an attractive cost if the child is an interface.
     *  @see #qualifierIsInterface(IASNode, ICompilerProject)
     */
    public  int qualifierIsInterface(IASNode iNode)
    {
        return qualifierIsInterface(iNode, this.project);
    }
    
    /**
     *  Transform a leaf AST into a compile-time constant;
     *  note that more complex constant folding is done by
     *  a series of rules in the BURM.  This routine looks
     *  at "terminal" constants, e.g., 1 or foo where foo
     *  is a namespace.
     *  @param iNode - the i-node of interest.
     *  @param project - an ICompilerProject for the current project.
     *  @return the constant value, if there is one.
     */
    public static Object transformNameToConstantValue(IASNode iNode, ICompilerProject project)
    {
        Object result = null;
        if ( iNode instanceof ExpressionNodeBase )
        {
            IASNode parentNode = iNode.getParent();
            if (parentNode instanceof BaseTypedDefinitionNode)
            {
                BaseTypedDefinitionNode parentDefinitionNode = (BaseTypedDefinitionNode)parentNode;
                if (parentDefinitionNode.getNameExpressionNode() == iNode)
                    return null;
                if (parentDefinitionNode.getTypeNode() == iNode)
                    return null;
            }
            else if (parentNode instanceof IterationFlowNode)
            {
                IterationFlowNode parentIterationFlowNode = (IterationFlowNode)parentNode;
                if (parentIterationFlowNode.getLabelNode() == iNode)
                    return null;
            }
            //  Try for a compile-time constant value.
            IDefinition val = ((ExpressionNodeBase)iNode).resolve(project);

            if ( val instanceof NamespaceDefinition )
            {
                NamespaceDefinition ns = (NamespaceDefinition)val;
                result = ns.resolveAETNamespace(project);
            }
            else if( val instanceof ConstantDefinition )
            {
                ConstantDefinition cd = (ConstantDefinition)val;
                result = cd.resolveValueFrom(project, (NodeBase)iNode);
            }
        }

        return result;
    }

    /**
     *  Transform a leaf AST into a compile-time constant;
     *  note that more complex constant folding is done by
     *  a series of rules in the BURM.  This routine looks
     *  at "terminal" constants, e.g., 1 or foo where foo
     *  is a namespace.
     *  @param iNode - the i-node of interest.
     *  @return the constant value, if there is one.
     *  @see #transformNameToConstantValue(IASNode, ICompilerProject)
     */
    public Object transformNameToConstantValue(IASNode iNode)
    {
        return transformNameToConstantValue(iNode, this.project);
    }

    /**
     * Check for identifiers that don't have a namespace quailifier. Generates problem if found.
     * 
     * @param scope is the scope where problems will be added
     * @param node is the definition node for the defintion we are checking
     * @param definition is the dfintion we are checking
     * @param className is the class in which the identifier lives, or null if not in a class. Used for error reporting
     */
    public static void checkScopedToDefaultNamespaceProblem(LexicalScope scope, BaseDefinitionNode node, IDefinition definition, String className)
    {
        if (node == null)       // if we don't have a node, it means we didn't come from source code, so there is no point
                                // in doing a semantic check
            return;
        INamespaceReference nsref = definition.getNamespaceReference();
        INamespaceDecorationNode dec = node.getNamespaceNode();
        if ((dec ==null) &&(nsref instanceof INamespaceDefinition.IInternalNamespaceDefinition))
        {
            String type = "declaration";        // should never see this string...
            if (definition instanceof IFunctionDefinition)
                type = "function";
            else if (definition instanceof IClassDefinition)
                type = "class";
            else if (definition instanceof IInterfaceDefinition)
                type = "interface";
            String identifierName = type + " '" + definition.getBaseName() + "'";
            scope.addProblem( new ScopedToDefaultNamespaceProblem( node, identifierName, className));
        }
    }
    
    /**
     * Checks that a given function's parameters have a type, and logs a problem if not
     * 
     * @param scope is the scope where problems are to be logged
     * @param node is the function node that is being checked (used for location reporting)
     * @param func is the definition of the function to be checked.
     */
    public static void checkParametersHaveNoTypeDeclaration(LexicalScope scope, IFunctionNode node, IFunctionDefinition func)
    {
        for (IParameterNode paramNode : node.getParameterNodes())
        {
            IExpressionNode paramType = paramNode.getVariableTypeNode();

            // check for parameter type declaration
            if (paramType == null)
            {
                scope.addProblem(new ParameterHasNoTypeDeclarationProblem(paramNode, paramNode.getName(), node.getName()));
            }
            else if(paramType.getAbsoluteStart() == -1 && paramType.getAbsoluteEnd() == -1
                    && paramType instanceof ILanguageIdentifierNode)
            {
                ILanguageIdentifierNode identifier = (ILanguageIdentifierNode) paramType;
                if (identifier.getKind().equals(LanguageIdentifierKind.ANY_TYPE))
                {
                    scope.addProblem(new ParameterHasNoTypeDeclarationProblem(paramNode, paramNode.getName(), node.getName()));
                }
            }
        }
    }
    
    /**
     * Checks that a given function definition has a return type, and logs a problem if not
     * 
     * @param scope is the scope where problems are to be logged
     * @param node is the function node that is being checked (used for location reporting(
     * @param func is the definition of the function to be checked.
     */
    public static void checkReturnValueHasNoTypeDeclaration(LexicalScope scope, IFunctionNode node, IFunctionDefinition func)
    {
        // constructors aren't supposed to have return types, so skip this check for them
        if (node.isConstructor())
            return;

        // Compiler synthesized nodes may not report return types as we expect. For example
        // the class generated for embedding has an implicit return type node, which causes our 
        // check to think the "user code" has no return type. So skip these kinds of nodes....
        if (node.getStart() == node.getEnd())
            return;
        
        IExpressionNode returnType = node.getReturnTypeNode();
        
        // check for return type declaration
        if (returnType == null)       
        {
            // if none found, derive best source location for problem report
            IASNode sourceLoc = node;
            IExpressionNode nameNode = node.getNameExpressionNode();
            if (nameNode != null)
                sourceLoc = nameNode;
            scope.addProblem(new ReturnValueHasNoTypeDeclarationProblem(sourceLoc, func.getBaseName()));
        }
    }
    
    /**
     * Determines if a list of definitions consists of a matched getter/setter pair.
     * 
     * @param defs The list of definitions to analyze.
     * @param project The context in which they are analyzed.
     * @return <code>true</code> if the definitions are a getter/setter pair.
     */
    public static boolean isGetterSetterPair(List<IDefinition> defs, ICompilerProject project)
    {
        // Must be a pair to be a getter setter pair. A pair plus others doesn't count.
        if (defs.size() != 2)
            return false;
        
        IDefinition def0 = defs.get(0);
        IDefinition def1 = defs.get(1);
        return isGetterSetterPair(def0, def1, project);
    }
    
    /**
     * Determines if a pair of definitions consists of a matched getter/setter pair.
     * 
     * @param def0 The first definitions to analyze.
     * @param def1 The first definitions to analyze.
     * @param project The context in which they are analyzed.
     * @return <code>true</code> if the definitions are a getter/setter pair.
     */
    public static boolean isGetterSetterPair(IDefinition def0, IDefinition def1, ICompilerProject project)
    {
        if (!(def0 instanceof IAccessorDefinition))
            return false;
      
        if (!(def1 instanceof IAccessorDefinition))
            return false;
        
        return ((IAccessorDefinition)def0).resolveCorrespondingAccessor(project) == def1;
    }
    
    /**
     * This is similar to the <code>resolveBaseClass()</code> method of <code>IClassDefinition</code>,
     * except it reports problems if the base class doesn't exist,
     * if it isn't actually a class, or if it is final;
     * it also returns the class definition for <code>Object</code> in these error cases.
     */
    public static ClassDefinition resolveBaseClass(ICommonClassNode classNode,
                                                   ClassDefinition classDefinition,
                                                   ICompilerProject project,
                                                   Collection<ICompilerProblem> problems)
    {
        IReference baseClassReference = classDefinition.getBaseClassReference();
        
        // Object has no base class.
        if (baseClassReference == null)
            return null;
        
        ITypeDefinition superclassDefinition = classDefinition.resolveType(
            baseClassReference, project, DependencyType.INHERITANCE);
        
        if (superclassDefinition == null)
        {
            IASNode problemNode = getBaseClassProblemNode(classNode, classDefinition);
            String baseClassReferenceName = baseClassReference.getName();

            //  The base class reference might be ambiguous.
            IDefinition foundDefinition = baseClassReference.resolve(project, (ASScope)classDefinition.getContainingScope(), DependencyType.INHERITANCE, true);
            if ( AmbiguousDefinition.isAmbiguous(foundDefinition))
                problems.add(new AmbiguousReferenceProblem(problemNode, baseClassReferenceName));
            else
                problems.add(new UnknownSuperclassProblem(problemNode, baseClassReferenceName));
            
            // Repair by making the class extend Object.
            superclassDefinition = getObjectDefinition(project);
        }
        else if (superclassDefinition instanceof IInterfaceDefinition)
        {
            IASNode problemNode = getBaseClassProblemNode(classNode, classDefinition);
            problems.add(new CannotExtendInterfaceProblem(problemNode));
            
            // Repair by making the class extend Object.
            superclassDefinition = getObjectDefinition(project);
        }
        else if (superclassDefinition.isFinal())
        {
            IASNode problemNode = getBaseClassProblemNode(classNode, classDefinition);
            problems.add(new BaseClassIsFinalProblem(problemNode));
            
            // Repair by making the class extend Object.
            superclassDefinition = getObjectDefinition(project);
        }
        else if (superclassDefinition == classDefinition)
        {
            problems.add(new CircularTypeReferenceProblem(classDefinition, classDefinition.getQualifiedName()));
            
            // Repair by making the class extend Object.
            superclassDefinition = getObjectDefinition(project);
        }
        
        // Report a problem if the superclass is deprecated
        // and the reference to it is not within a deprecated API.
        if (superclassDefinition != null && superclassDefinition.isDeprecated())
        {
            IASNode problemNode = getBaseClassProblemNode(classNode, classDefinition);
            if (!SemanticUtils.hasDeprecatedAncestor(problemNode))
            {
                ICompilerProblem problem = SemanticUtils.createDeprecationProblem(superclassDefinition, problemNode);
                problems.add(problem);
            }
        }

        return (ClassDefinition)superclassDefinition;
    }
    
    private static IASNode getBaseClassProblemNode(ICommonClassNode classNode, ClassDefinition classDefinition)
    {
        // If we have a source node for 'class A extends B', return the node for B.
        if (classNode instanceof IClassNode)
            return ((IClassNode)classNode).getBaseClassExpressionNode();
        
        // Otherwise, try to get the class node from the definition.
        return classDefinition.getNode();
    }
    
    private static IClassDefinition getObjectDefinition(ICompilerProject project)
    {
        IClassDefinition objectDefinition = (IClassDefinition)project.getBuiltinType(
            IASLanguageConstants.BuiltinType.OBJECT);
        
        if (objectDefinition == null)
        {
            // Abort codegen with a more comprehensible diagnostic.
            throw new MissingBuiltinException(IASLanguageConstants.Object);
        }
        
        return objectDefinition;
    }
        
    public static boolean isValidImport(IImportNode importNode, ICompilerProject project,
                                        boolean inInvisibleCompilationUnit)
    {
        String importName = importNode.getImportName();
        importName = project.getActualPackageName(importName);
        
        // First check whether the import name matches
        // a definition in the project scope.
        ASProjectScope projectScope = (ASProjectScope)project.getScope();
        if (projectScope.isValidImport(importName))
            return true;
        
        // For an invisible compilation unit, which doesn't contribute
        // definitions to the project scope, we must also check
        // whether it matches an externally visible definition
        // in the file.
        if (inInvisibleCompilationUnit)
        {
            IFileNode fileNode = (IFileNode)importNode.getAncestorOfType(IFileNode.class);
            IDefinition[] externallyVisibleDefinitionsInThisFile =
                fileNode.getTopLevelDefinitions(false, true);
            for (IDefinition d : externallyVisibleDefinitionsInThisFile)
            {
                String qName = d.getQualifiedName();
                if (qName.equals(importName))
                    return true;
                
                String wildcardQName = ImportNode.makeWildcardName(qName);
                if (wildcardQName.equals(importName))
                    return true;
            }
        }
        
        return false;
    }

    /**
     * Determine if a given IDefinition is the definition of a type (class or interface)
     * @param d     the definition to check
     * @return      true if the Definition represents a Type
     */
    public static boolean isType(IDefinition d)
    {
        return d instanceof ITypeDefinition;
    }
    

    /**
     * Check if {@code node} is in an E4X filter expression.
     * 
     * @param node AST node to be checked.
     * @return True if the node is in an E4X filter expression without crossing
     * a block.
     */
    public static boolean isE4XWildcardProperty(final IdentifierNode node)
    {
        assert node != null : "node can't be null";
        IASNode blockOrFilterNode = node.getParent();
        
        // "*::bar", "foo::*", "foo.*", "foo.(@* == 10) are not E4X wildcard properties
        if (blockOrFilterNode instanceof MemberAccessExpressionNode ||
            blockOrFilterNode instanceof UnaryOperatorAtNode)
            return false;
        
        while (blockOrFilterNode != null &&
               blockOrFilterNode.getNodeID() != ASTNodeID.BlockID &&
               blockOrFilterNode.getNodeID() != ASTNodeID.E4XFilterID)
        {
            blockOrFilterNode = blockOrFilterNode.getParent();
        }

        if (blockOrFilterNode != null && blockOrFilterNode.getNodeID() == ASTNodeID.E4XFilterID)
            return true;
        else
            return false;
    }

    /**
     *  Decide if an assignment node is in a context that merits a warning.
     *  @return true if an assignment has a path to a parent's conditional
     *  expression that contains only Boolean operators.
     */
    public static boolean isUnprotectedAssignmentInConditional(final IASNode node)
    {
        IASNode current = node;
        IASNode parent  = current.getParent();

        while ( parent != null )
        {
            switch ( parent.getNodeID() )
            {
                case Op_LogicalAndID:
                case Op_LogicalOrID:
                case Op_LogicalNotID:
                case ContainerID:
                case ConditionalID:
                    current = parent;
                    parent  = current.getParent();
                    break;
                
                case WhileLoopID:
                    return current == getNthChild(parent,0);

                case DoWhileLoopID:
                    return current == getNthChild(parent,1);

                case IfStatementID:
                    return current == getNthChild(parent,0);

                default:
                    return false;
            }
        }

        return false;
    }

    /**
     *  Downcast an IDefinition and get an AET name from it.
     *  @param def - the definition.
     *  @param project - the active project.
     *  @return the definition's AET name.
     */
    public static Name getAETName(IDefinition def, ICompilerProject project)
    {
        return ((DefinitionBase)def).getMName(project);
    }

    /**
     *  Downcast an IDefinition and get an AET name from it.
     *  @param def - the definition.
     *  @return the definition's AET name.
     *  @see #getAETName(IDefinition, ICompilerProject) to which this delegates.
     */
    public Name getAETName(IDefinition def)
    {
        return ((DefinitionBase)def).getMName(this.project);
    }

    /**
     * Determines whether the specified node has an ancestor
     * which is a definition node with <code>[Deprecated]</code> metadata.
     * <p>
     * The node itself is not checked for such metadata.
     * 
     * @param node The node whose ancestors should be checked.
     * @return <code>true</code> if an ancestor is deprecated.
     */
    public static boolean hasDeprecatedAncestor(IASNode node)
    {
        for (IASNode ancestor = node.getParent();
             ancestor != null;
             ancestor = ancestor.getParent())
        {
            if (ancestor instanceof IDefinitionNode)
            {
                IDefinition definition = ((IDefinitionNode)ancestor).getDefinition();
                if (definition.isDeprecated())
                    return true;
            }
        }
        
        return false;
    }

    /**
     * Uses the <code>[Deprecated]</code> metadata of the specified definition
     * to create one of five different deprecation problems, depending on
     * the presence of <code>message</code>, <code>since</code>, and
     * <code>replacement</code> attributes in the metadata.
     * 
     * @param definition A deprecated definition.
     * @param site The node referring to the deprecated definition,
     * which determines the problem location.
     * @return An {@link ICompilerProblem}.
     */
    public static ICompilerProblem createDeprecationProblem(IDefinition definition, IASNode site)
    {
        ICompilerProblem problem = null;

        IDeprecationInfo deprecationInfo = definition.getDeprecationInfo();

        String message = deprecationInfo.getMessage();
        if (message != null)
        {
            problem = new DeprecatedAPIWithMessageProblem(site, message);
        }
        else
        {
            String name = definition.getBaseName();
            String since = deprecationInfo.getSince();
            String replacement = deprecationInfo.getReplacement();

            if (since == null && replacement == null)
                problem = new DeprecatedAPIProblem(site, name);
            
            else if (since != null && replacement == null)
                problem = new DeprecatedAPIWithSinceProblem(site, name, since);
            
            else if (since == null && replacement != null)
                problem = new DeprecatedAPIWithReplacementProblem(site, name, replacement);
            
            else if (since != null && replacement != null)
                problem = new DeprecatedAPIWithSinceAndReplacementProblem(site, name, since, replacement);
        }
        
        return problem;
    }

    /**
     *  Locate a FunctionDefinition that encloses the given i-node.
     *  @param iNode - the i-node of interest.
     *  @return the nearest enclosing function definition, or null.
     */
    public static FunctionDefinition getFunctionDefinition(IASNode iNode)
    {
        FunctionNode func_node;
        
        if ( iNode instanceof FunctionNode )
            func_node = (FunctionNode) iNode;
        else
            func_node = (FunctionNode) iNode.getAncestorOfType(FunctionNode.class);

        if ( func_node != null )
            return func_node.getDefinition();
        else
            return null;
    }

    /**
     *  Can the subject be abstract?
     *  @param iNode - an AST node in (or at the border of) the function.
     *  @return true if the definition is allowed to be abstract
     */
    public static boolean canBeAbstract(IASNode iNode, ICompilerProject project)
    {
        if(iNode instanceof IClassNode)
        {
            IClassNode classNode = (IClassNode) iNode;
            IClassDefinition classDef = classNode.getDefinition();
            return !classDef.isFinal();
        }
        else if(iNode instanceof IFunctionNode)
        {
            IFunctionNode funcNode = (IFunctionNode) iNode;
            IFunctionDefinition funcDef = funcNode.getDefinition();
            IDefinition parentDef = funcDef.getParent();
            if (parentDef instanceof IClassDefinition)
            {
                return parentDef.isAbstract()
                        && !funcDef.isStatic()
                        && !funcDef.isFinal()
                        && !funcDef.isConstructor()
                        && !(funcDef instanceof IAccessorDefinition);
            }
            return false;
        }
        return false;
    }

    /**
     *  Does the subject function require a non-void return value?
     *  @param iNode - an AST node in (or at the border of) the function.
     *  @return true if the function's definition is known, the definition's
     *    return type is known (and the function is not a constructor),
     *    and the return type is not void or *
     */
    public static boolean functionMustReturnValue(IASNode iNode, boolean allowAbstract, ICompilerProject project)
    {
        FunctionDefinition func_def = SemanticUtils.getFunctionDefinition(iNode);

        if ( func_def != null )
        {
            IDefinition return_type = func_def.resolveReturnType(project);
            //  The function must return a value unless its return type is void or *,
            //  or if it's a constructor (its definition lies).
            return !( 
                return_type == null ||
                return_type.equals(ClassDefinition.getVoidClassDefinition()) ||
                return_type.equals(ClassDefinition.getAnyTypeClassDefinition()) ||
                func_def.isConstructor() ||
                (allowAbstract && canBeAbstract(iNode, project) && func_def.isAbstract())
            );
        }
        else
        {
            //  Nothing known about this function.
            return false;
        }
    }

    /**
     * Calculate the type of the RHS of an assignment.  Works with either BinaryOperatorAssignmentNodes
     * or VariableNodes
     * @param project   the active project
     * @param iNode     the node representing the assignment
     * @return          the type of the RHS, or null if one couldn't be determined.
     */
    public static ITypeDefinition resolveRHSTypeOfAssignment (ICompilerProject project, IASNode iNode)
    {
        ITypeDefinition srcType = null;
        if( iNode instanceof BinaryOperatorAssignmentNode)
        {
            if ( iNode.getChildCount() > 1 && iNode.getChild(1) instanceof IExpressionNode)
            {
                IExpressionNode rhs = (IExpressionNode)iNode.getChild(1);
                srcType = rhs.resolveType(project);
            }
        }
        else if(iNode instanceof IVariableNode)
        {
            IExpressionNode rhs = ((IVariableNode)iNode).getAssignedValueNode();
            srcType = rhs != null ? rhs.resolveType(project) : null;
        }
        return srcType;
    }

    /**
     * Helper method to get the file path from an IASNode
     * @param iNode the node you want the file path for
     * @return A String representing the file path for the file the node came from
     */
    public static String getFileName(IASNode iNode)
    {
        String file_name = iNode.getSourcePath();

        if ( file_name == null )
        {
            //  Fall back on the file specification.
            //  This may also be null (or assert if it's feeling moody),
            //  in which case the file name remains null and file/line
            //  processing noops.
            try
            {
                IFileSpecification fs = iNode.getFileSpecification();
                if ( fs != null)
                    file_name = fs.getPath();
            }
            catch (Exception e)
            {
                //  No file specification available, probably
                //  because this node is some kind of syntho.
            }
        }
        return file_name;
    }
}
