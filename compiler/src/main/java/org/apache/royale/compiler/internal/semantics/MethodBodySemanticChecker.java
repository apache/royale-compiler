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

import static org.apache.royale.abc.ABCConstants.OP_findproperty;
import static org.apache.royale.abc.ABCConstants.OP_findpropstrict;
import static org.apache.royale.abc.ABCConstants.OP_getlex;
import static org.apache.royale.abc.ABCConstants.OP_getouterscope;
import static org.apache.royale.abc.ABCConstants.OP_getscopeobject;
import static org.apache.royale.abc.ABCConstants.OP_newactivation;
import static org.apache.royale.abc.ABCConstants.OP_newcatch;
import static org.apache.royale.abc.ABCConstants.OP_newclass;
import static org.apache.royale.abc.ABCConstants.OP_newfunction;
import static org.apache.royale.abc.ABCConstants.OP_popscope;
import static org.apache.royale.abc.ABCConstants.OP_pushscope;
import static org.apache.royale.abc.ABCConstants.OP_pushwith;
import static org.apache.royale.abc.ABCConstants.OP_returnvalue;
import static org.apache.royale.abc.ABCConstants.OP_returnvoid;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.ECMASupport;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IGetterDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.as.codegen.*;
import org.apache.royale.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.problems.*;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.ICompoundAssignmentNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.INumericLiteralNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IReturnNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ITypedExpressionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.ClassTraitsDefinition;
import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.GetterDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.SetterDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils.MultiDefinitionType;
import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorLogicalAndNode;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorLogicalOrNode;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LanguageIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.ModifierNode;
import org.apache.royale.compiler.internal.tree.as.ModifiersContainerNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.NumericLiteralNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.ParameterNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.tree.as.TernaryOperatorNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.internal.tree.as.VectorLiteralNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLDocumentNode;

/**
 *  The MethodBodySemanticChecker contains the logic that checks method body semantics.
 */
public class MethodBodySemanticChecker
{
    /**
     *  The current LexicalScope for this compilation.
     */
    private LexicalScope currentScope;

    /**
     *  The current ICompilerProject for this compilation.
     */
    private final ICompilerProject project;

    /**
     *  Semantic utilities, which know how to interface with name resolution
     */
    private final SemanticUtils utils;


    /**
     *  Current state of diagnostics regarding "super" 
     */
    private enum SuperState { Invalid, Initial, Armed };

    /**
     *  Current state of diagnostics regarding "super" 
     */
    private SuperState superState = SuperState.Invalid;

    /**
     *  Construct a new MethodBodySemanticChecker from the current lexical scope.
     */
    public MethodBodySemanticChecker(LexicalScope current_scope)
    {
        this.currentScope = current_scope;
        this.project = currentScope.getProject();
        this.utils = new SemanticUtils(this.project);
    }

    /**
     * Do a semantic analysis of all the arguments of a function
     *
     * @param funcNode is the function to be analyzed
     */
    public void checkFunctionDecl(IFunctionNode funcNode )
    {
        IParameterNode[] paramNodes = funcNode.getParameterNodes();
        for (IParameterNode paramNode : paramNodes)
        {
            IDefinition paramDef = paramNode.getDefinition();

            ITypeDefinition paramTypeDef = ((IVariableDefinition)paramDef).resolveType(project);
            if (!SemanticUtils.isType(paramTypeDef) )
            {
                IExpressionNode typeExpression = paramNode.getVariableTypeNode();
                String typeName =  paramDef.getTypeAsDisplayString();
                addTypeProblem(typeExpression, paramTypeDef, typeName, true);
            }
        }
    }

    /**
     * Semantic analysis of a function declared inside another function.  This is only for function declarations,
     * and not function expressions.
     * @param funcNode  The node of the nested function
     */
    public void checkNestedFunctionDecl(IFunctionNode funcNode)
    {
        IFunctionDefinition funcDef = funcNode.getDefinition();
        List<IDefinition> defs = SemanticUtils.findPotentialFunctionConflicts(currentScope.getProject(), funcDef);

        // Check for potential dups - functions can be redeclared and won't be ambiguous, but in strict mode
        // we want to issue an error.
        // Don't need to worry about getter/setter pairs as you can't declare nested getter/setters
        if( defs.size() > 1 )
        {
            ICompilerProblem problem = new DuplicateFunctionDefinitionProblem(funcNode, funcDef.getBaseName());
            this.currentScope.addProblem(problem);
        }
    }

    /**
     *  Convenience method to add a problem.
     *  @param problem - the problem to add.
     */
    private void addProblem(ICompilerProblem problem)
    {
        //  Some of the "figure out this error scenario"
        //  methods pass in null to mean "ignore."
        if ( problem != null )
            this.currentScope.addProblem(problem);
    }

    /**
     *  Perform semantic checks on an assignment.
     */
    public void checkAssignment(IASNode iNode, Binding binding)
    {
        checkLValue(iNode, binding);

        if ( SemanticUtils.isUnprotectedAssignmentInConditional(iNode) )
            addProblem(new AssignmentInConditionalProblem(SemanticUtils.getNthChild(iNode, 0)));

        //  Check the assignment's type logic and values.
        ITypeDefinition leftType = null;
        if ( binding.getDefinition() != null )
        {
            IDefinition leftDef = binding.getDefinition();
            leftType = binding.getDefinition().resolveType(project);
            
            IASNode rightNode = SemanticUtils.getNthChild(iNode, 1);
       
            checkImplicitConversion(rightNode, leftType, null);
            checkAssignmentValue(leftDef, rightNode);
        }
    }
    
    /**
     * Checks that the value (RHS) is appropriate, given the type of the LHS
     * @param leftDefinition is the definition of the variable on the LHS
     * @param rightNode is the tree node for the RHS of the assignment
     */
    public void checkAssignmentValue( IDefinition leftDefinition, IASNode rightNode)
    {
        ITypeDefinition leftType = leftDefinition.resolveType(project);
        
        if (rightNode instanceof IExpressionNode)
        {
            IDefinition rightType = ((IExpressionNode)rightNode).resolveType(project);
            final boolean leftIsNumericOrBoolean = SemanticUtils.isNumericTypeOrBoolean(leftType, project);   
            final boolean rightIsNull =  SemanticUtils.isBuiltin(rightType, BuiltinType.NULL, project);
            
            if (leftIsNumericOrBoolean && rightIsNull)
            {
                final boolean leftIsConstant = leftDefinition instanceof IConstantDefinition;
                addProblem(leftIsConstant ?
                        new IncompatibleDefaultValueOfTypeNullProblem(rightNode, leftType.getBaseName()) :
                        new NullUsedWhereOtherExpectedProblem(rightNode, leftType.getBaseName()));
            }
        }
    }
    
    /**
     * Perform semantic checks on an initialization
     */
    public void checkInitialization(IASNode iNode, Binding binding)
    {
        //  Check the assignment's type logic.
        if ( binding.getDefinition() != null )
            checkImplicitConversion(SemanticUtils.getNthChild(iNode, 2), binding.getDefinition().resolveType(project), null);
    }
    
    /**
     *  Perform semantic checks on an x[i] = rvalue assignment expression.
     */
    public void checkAssignToBracketExpr(IASNode iNode)
    {
    }

    /**
     *  Check a binary operator.
     *  @param iNode - the operator node.
     *  @param opcode - the opcode.
     */
    public void checkBinaryOperator(IASNode iNode, int opcode)
    {
        final IASNode left = ((IBinaryOperatorNode)iNode).getLeftOperandNode();
        final IASNode right = ((IBinaryOperatorNode)iNode).getRightOperandNode();
        checkBinaryOperator(iNode, left, right, opcode);
    }

    /**
     *  Check a (possibly implicit) binary operator.
     *  @param root - the operator node.
     *  @param left - the left-hand operand.
     *  @param right - the right-hand operand.
     *  @param opcode - the opcode.
     */
    public void checkBinaryOperator(IASNode root, IASNode left, IASNode right, final int opcode)
    {
        switch(opcode)
        {
            case ABCConstants.OP_multiply:
            case ABCConstants.OP_divide:
            case ABCConstants.OP_modulo:
            case ABCConstants.OP_subtract:
            case ABCConstants.OP_lshift:
            case ABCConstants.OP_rshift:
            case ABCConstants.OP_urshift:
            case ABCConstants.OP_bitand:
            case ABCConstants.OP_bitor:
            case ABCConstants.OP_bitxor:
                checkImplicitConversion(left, utils.numberType(), null);
                checkImplicitConversion(right, utils.numberType(), null);
                break;

            case ABCConstants.OP_istypelate:
            case ABCConstants.OP_astypelate:
                checkTypeCheckImplicitConversion(right);
                break;
            case ABCConstants.OP_equals:
            case ABCConstants.OP_strictequals:
            case ABCConstants.OP_lessthan:
            case ABCConstants.OP_lessequals:
            case ABCConstants.OP_greaterthan:
            case ABCConstants.OP_greaterequals:

                if ((left instanceof IExpressionNode) && (right instanceof IExpressionNode))
                {
                    checkComparison((IExpressionNode)left, (IExpressionNode)right);
                }
                break;
            case ABCConstants.OP_instanceof:
                addProblem(new InstanceOfProblem(root));
                break;
        }
    }

    /**
     *  Check an implicit conversion.
     *  @param iNode - the expression being checked.
     *  @param expected_type - the type to convert to.
     */
    
    public void checkImplicitConversion(IASNode iNode, IDefinition expected_type, FunctionDefinition func)
    {
        if (iNode instanceof BinaryOperatorLogicalOrNode ||
             iNode instanceof BinaryOperatorLogicalAndNode ||
             iNode instanceof TernaryOperatorNode)
        {
            // For these logical nodes, just check both sides with a recursive call.
            // Note that we need to recurse, because this may be a tree of binary logical nodes
            final IExpressionNode leftOp = ((IBinaryOperatorNode)iNode).getLeftOperandNode();
            checkImplicitConversion(leftOp, expected_type, null);
            final IExpressionNode rightOp = ((IBinaryOperatorNode)iNode).getRightOperandNode();
            checkImplicitConversion(rightOp, expected_type, null);
        }

        else if (iNode instanceof ExpressionNodeBase)
        {
            checkImplicitConversion(iNode, ((ExpressionNodeBase)iNode).resolveType(project), expected_type, func);
        }
    }

    /**
     * Check an implicit conversion in an 'is' or 'as' binop
     * @param iNode - the expression being checked.
     */
    public void checkTypeCheckImplicitConversion(IASNode iNode)
    {
        if (!(iNode instanceof ExpressionNodeBase))
            return;

        final IDefinition actual_type = ((ExpressionNodeBase)iNode).resolveType(project);
        // expected type is always of type CLASS;
        final IDefinition expected_type = utils.getBuiltinType(BuiltinType.CLASS);

        if (!SemanticUtils.isValidTypeConversion(expected_type, actual_type, project, currentScope.getInInvisibleCompilationUnit()))
        {
            addProblem(new ImplicitTypeCheckCoercionToUnrelatedTypeProblem(iNode, actual_type.getQualifiedName(), expected_type.getQualifiedName()));
        }
        else
        {
            SpecialValue value = getSpecialValue((IExpressionNode)iNode);
            if (value == SpecialValue.UNDEFINED) // test for undefined
            {
                addProblem(new ImplicitTypeCheckCoercionToUnrelatedTypeProblem(iNode, IASLanguageConstants.UNDEFINED, expected_type.getQualifiedName()));                
            }
            else if (iNode instanceof LiteralNode && IASKeywordConstants.NULL.equals(((LiteralNode)iNode).getValue())) // test for null
            {
                addProblem(new ImplicitTypeCheckCoercionToUnrelatedTypeProblem(iNode, IASKeywordConstants.NULL, expected_type.getQualifiedName()));
            }
        }
    }

    enum SpecialValue { NONE, NAN, UNDEFINED }
    SpecialValue getSpecialValue(IExpressionNode node)
    {
        SpecialValue ret = SpecialValue.NONE;
        IDefinition def = node.resolve(project);
        
        if (def instanceof IConstantDefinition)
        {
            // it's easy to find the word "undefined", just compare to the
            // singleton definition
            if (def == project.getUndefinedValue())
            {
                ret = SpecialValue.UNDEFINED;
            }
            else
            {
                Object initialValue = ((ConstantDefinition) def).resolveValueFrom(project, (NodeBase)node);
                if (initialValue != null)
                {
                    if (initialValue instanceof Double)
                    {
                        Double d = (Double)initialValue;
                        if (ECMASupport.isNan(d))
                            ret = SpecialValue.NAN;
                    }
                }
            }
        }
        return ret;
    }
    
    /**
     * determine if it is reasonable to compare two operands of perhaps different types.
     * Create a CompilerProblem if not.
     * 
     */
    private void checkComparison(IExpressionNode leftNode, IExpressionNode rightNode)
    {
        SpecialValue leftValue = getSpecialValue(leftNode);
        SpecialValue rightValue = getSpecialValue(rightNode);
        
        if (leftValue == SpecialValue.NAN || rightValue == SpecialValue.NAN)
        {
           addProblem( new IllogicalComparionWithNaNProblem(leftNode));
        }
        
       
        
        IDefinition left_type = leftNode.resolveType(project);
        IDefinition right_type = rightNode.resolveType(project);
        
        if (left_type==null || right_type==null)
        {
            return; // if we can't resolve both side, some other check will catch it.
        }
        
       
        final IDefinition anyType = project.getBuiltinType(BuiltinType.ANY_TYPE);
        if (rightValue == SpecialValue.UNDEFINED && !left_type.equals(anyType))
        {
           addProblem(new IllogicalComparisonWithUndefinedProblem(leftNode));
        }
        
        if (leftValue == SpecialValue.UNDEFINED && !right_type.equals(anyType))
        {
           addProblem(new IllogicalComparisonWithUndefinedProblem(rightNode));
        }
        // Harmless pre-optimizition. If both types are the same they must be comparable.
        // (but only after we have checked special value cases above.
        if (left_type.equals(right_type))
        {
            return;
        }
        
        final boolean leftIsNumeric = SemanticUtils.isNumericType(left_type, project);
        final boolean rightIsNumeric = SemanticUtils.isNumericType(right_type, project);
        final boolean leftIsNull =  SemanticUtils.isBuiltin(left_type, BuiltinType.NULL, project);
        final boolean rightIsNull =  SemanticUtils.isBuiltin(right_type, BuiltinType.NULL, project);
        
        // Interfaces can be compared against any Object
        if ((left_type instanceof IInterfaceDefinition && !rightIsNumeric) ||
                ( right_type instanceof IInterfaceDefinition && !leftIsNumeric))
        {
            return;
        }
        
         
        boolean isBad = false;
        
        // Numeric types can never be null
        if ((leftIsNumeric&&rightIsNull) || (rightIsNumeric&&leftIsNull))
        {
            isBad = true;
        }
        
        // If all the speical cases have passed, the try isValidTypeConversion in both directions.
        if (!isBad)
        {
            if (SemanticUtils.isValidTypeConversion(left_type, right_type, project, this.currentScope.getInInvisibleCompilationUnit()))
                return;
            if (SemanticUtils.isValidTypeConversion(right_type, left_type, project, this.currentScope.getInInvisibleCompilationUnit()))
                return;
        }
        
        addProblem(new ComparisonBetweenUnrelatedTypesProblem(leftNode, left_type.getBaseName(), right_type.getBaseName()));
    }

    /**
     *  Check a compound assignment.
     *  @param iNode - the node at the root of the assignment subtree.
     *  @param lvalue - the resolved lvalue (which is also an implicit rvalue).
     *  @param opcode - the opcode of the implied binary operator.
     */
    public void checkCompoundAssignment(IASNode iNode, Binding lvalue, final int opcode)
    {
        ICompoundAssignmentNode compoundNode = (ICompoundAssignmentNode)iNode;
        IBinaryOperatorNode  binop = (IBinaryOperatorNode)iNode;

        checkLValue(iNode, lvalue);

        if ( SemanticUtils.isUnprotectedAssignmentInConditional(iNode) )
            addProblem(new AssignmentInConditionalProblem(binop.getLeftOperandNode()));

        //  Check the implicit binary operator.
        checkBinaryOperator(iNode, binop.getLeftOperandNode(), binop.getRightOperandNode(), opcode);

        //  Check the assignment's types are compatible.
        if ( lvalue.getDefinition() != null )
        {
            //  Do own checks, then call common logic to emit diagnostics.
            ITypeDefinition lhsType = lvalue.getDefinition().resolveType(this.project);
            ITypeDefinition compoundType = compoundNode.resolveTypeOfRValue(this.project);

            if ( ! SemanticUtils.isValidImplicitOpAssignment(lhsType, compoundType, opcode, this.project, this.currentScope.getInInvisibleCompilationUnit()) )
            {
                checkImplicitConversion(binop.getRightOperandNode(), lhsType, null);
            }
            else if ( opcode == ABCConstants.OP_iffalse || opcode == ABCConstants.OP_iftrue )
            {
                //  check the RHS type of a logical operation on its own;
                //  this is rather strange behavior, but it replicates ASC's logic.
                checkImplicitConversion(binop.getRightOperandNode(), lhsType, null);
            }
        }
    }

    /**
     *  Check an implicit conversion.
     *  @param actual_type   - the type of the expression being checked.
     *  @param expected_type - the type to convert to.
     */
    private void checkImplicitConversion(IASNode iNode, IDefinition actual_type, IDefinition expected_type, FunctionDefinition func)
    {
        if ( !SemanticUtils.isValidTypeConversion(expected_type, actual_type, this.project, this.currentScope.getInInvisibleCompilationUnit()) )
        {
            if (project.isValidTypeConversion(iNode, actual_type, expected_type, func))
                return;
            
            // If we're assigning to a class, this will generate an "Illegal assignment to class" error,
            // so we don't need another error for implicit coercion
            if( !(expected_type instanceof ClassTraitsDefinition) )
            {
                if ( utils.isInstanceOf(expected_type, actual_type) )
                {
                    addProblem(new ImplicitCoercionToSubtypeProblem(iNode, actual_type.getQualifiedName(), expected_type.getQualifiedName()));
                }
                else
                {
                    addProblem(new ImplicitCoercionToUnrelatedTypeProblem(iNode, actual_type.getQualifiedName(), expected_type.getQualifiedName()));
                }
            }
        }
    }

    /**
     * Check if we are allowed to declare a bindable variable at this location.
     */
    public void checkBindableVariableDeclaration(IASNode iNode, IDefinition d)
    {

        assert d != null;

        if( !(d.getParent() instanceof IClassDefinition))
        {
            this.currentScope.addProblem(new LocalBindablePropertyProblem(iNode));
        }
        
        checkVariableDeclaration(iNode);
    }
    
    /**
     * Check a constant value used in a non-initializer context.
     */
    public void checkConstantValue(IASNode iNode)
    {
        // Check for a node resolving to a deprecated constant definition.
        if (iNode instanceof IExpressionNode)
        {
            IDefinition definition = ((IExpressionNode)iNode).resolve(project);
            checkDeprecated(iNode, definition);
        }
    }

    /**
     *  Check that a synthetic super() call is allowed by the class' superclass.
     */
    public void checkDefaultSuperCall(IASNode iNode)
    {
        //  This occurs in some error cases.
        if ( iNode == null )
            return;

        ClassNode enclosing_class = (ClassNode) iNode.getAncestorOfType(ClassNode.class);

        if ( enclosing_class != null )
        {
            IClassDefinition super_def = enclosing_class.getDefinition().resolveBaseClass(project);
            if (super_def != null)
            {
                IFunctionDefinition ctor = super_def.getConstructor();

                if (ctor instanceof FunctionDefinition)
                {
                    FunctionDefinition func = (FunctionDefinition)ctor;
                    if (func.getParameters() != null && func.getParameters().length != 0)
                    {
                        ParameterDefinition first_param = func.getParameters()[0];

                        if ( !first_param.hasDefaultValue() && ! first_param.isRest() )
                        {
                            if ( enclosing_class.getDefinition().getConstructor().isImplicit()) {
                                //in this case the Error reporting site should point to the class node,
                                //because there is no 'real' constructor node to reference in the source code
                                addProblem(new NoDefaultConstructorInBaseClassProblem(enclosing_class, super_def.getBaseName()));
                            }
                            else addProblem(new NoDefaultConstructorInBaseClassProblem(iNode, super_def.getBaseName()));
                        }
                    }
                }
            }
        }
    }
    
    /**
     *  Perform semantic checks on a delete expression.
     */
    public void checkDeleteExpr(IASNode iNode, Binding binding)
    {
        IDefinition def = binding.getDefinition();

        if (def != null)
        {
            // If we can resolve to a definition, check to be sure we are not trying
            // to delete a non-dynamic properly
            if (!(utils.hasDynamicBase(binding) || SemanticUtils.isInWith(iNode)))
                addProblem(new AttemptToDeleteFixedPropertyProblem(iNode, binding.getName()));
        }
        else if (SemanticUtils.hasBaseNode(binding) && !utils.hasDynamicBase(binding))
        {
            // If we are trying to delete a member of a class, but the member doesn't exists,
            // The log the problem for that
            addProblem(new AccessUndefinedMemberProblem(
                    roundUpUsualSuspects(binding, iNode),
                    binding.getName().getBaseName(),
                    utils.getTypeOfBase(binding.getNode())));
        }
        else
        {
            // This checked knows about packages and undefined properties,
            // could use the more specific  
            //     addProblem(accessUndefinedProperty(binding, roundUpUsualSuspects(binding, iNode)));
            checkLValue(iNode, binding);
        }
    }

    /**
     *  Check a super() or super(a,b,c) call.
     */
    public void checkExplicitSuperCall(IASNode iNode, Vector<? extends Object> args)
    {
        LanguageIdentifierNode super_node = (LanguageIdentifierNode)((IFunctionCallNode)iNode).getNameNode();

        //  Check that this super() call is in a constructor.
        if ( !SemanticUtils.isInConstructor(iNode) )
        {
            addProblem(new InvalidSuperStatementProblem(iNode));
        }
        else
        {
            //  Check that this super call does not follow a construct
            //  that invalidates it.
            if ( this.superState != SuperState.Initial )
                addProblem(new ExtraneousSuperStatementProblem(iNode));
            else
                this.superState = SuperState.Armed;
        }

        //  Check parameters if possible.
        ClassDefinition super_def = (ClassDefinition) super_node.resolveType(project);

        if (super_def != null)
        {
            IFunctionDefinition ctor = super_def.getConstructor();

            if (ctor instanceof FunctionDefinition)
            {
                checkFormalsVsActuals(super_node, (FunctionDefinition)ctor, args);
            }
        }
    }

    /**
     *  Check that formal and actual parameters correspond and are compatible.
     */
    private void checkFormalsVsActuals(IASNode iNode, FunctionDefinition func, Vector<? extends Object> actuals)
    {

        //  If the call is through a function variable then we don't know much about it.
        //  If we get a setter function assume a getter is what was meant since calling a setter
        //  directly is not legal and resolving the name for a getter/setter could
        //  return either. Code generation does the right thing so changing this check
        //  to just return for setters as well.
        if ( func instanceof GetterDefinition || func instanceof SetterDefinition)
            return;

        //  Check the formal parameter definitions, and ensure we have
        //  a corresponding number of actual parameters.
        ParameterDefinition[] formals = func.getParameters();

        if ( formals == null )
            return;
        
        boolean last_is_rest   = formals.length > 0 && formals[formals.length - 1].isRest();

        int required_count = 0;

        if ( actuals.size() > formals.length && !last_is_rest )
        {
        	if (project.isParameterCountMismatchAllowed(func, formals.length, actuals.size()))
        		return;
            addProblem(new TooManyFunctionParametersProblem(iNode, formals.length));
        }

        //  Compute the number of required parameters.
        for ( int i = 0; i < formals.length; i++ )
        {
            if ( formals[i].hasDefaultValue() || formals[i].isRest() )
                break;

            required_count++;
        }

        if ( actuals.size() < required_count )
        {
            addProblem(new TooFewFunctionParametersProblem(iNode, required_count));
        }

        //  Check that the actuals are compatible with the formals.
        IASNode actuals_container = null;
        if( iNode instanceof FunctionCallNode )
            actuals_container = ((FunctionCallNode)iNode).getArgumentsNode();

        if ( actuals_container != null )
        {
            for ( int i = 0; i < actuals_container.getChildCount() && i < formals.length; i++ )
            {
                if ( !formals[i].isRest() )
                    checkImplicitConversion( actuals_container.getChild(i), formals[i].resolveType(project), func );
            }
        }
    }

    /**
     *  Check a function body's overall characteristics.
     */
    public void checkFunctionBody(IASNode iNode)
    {
        if ( iNode instanceof FunctionNode )
        {
            FunctionNode func = (FunctionNode)iNode;

            if (SemanticUtils.isFunctionClosure(func))
            {
                for (IASNode thisNode : findThisIdentifierNodes(func))
                {
                    addProblem(new ThisUsedInClosureProblem(thisNode));
                }
            }

            IDefinition def = func.getDefinition();

            if ( project.getAllowAbstractClasses()
                    && def.isAbstract()
                    && SemanticUtils.canBeAbstract(iNode, currentScope.getProject()))
            {
                if ( func.hasBody() )
                {
                    addProblem(new AbstractMethodWithBodyProblem(SemanticUtils.getFunctionProblemNode(func)));
                }
            }
            else if ( !( def.hasModifier(ASModifier.NATIVE ) || def.hasModifier(ASModifier.DYNAMIC) || func.isConstructor() ) )
            {
                if ( !func.hasBody() )
                {
                    addProblem(new FunctionWithoutBodyProblem(SemanticUtils.getFunctionProblemNode(func)));
                }
            }
        }
    }

    private static List<IASNode> findThisIdentifierNodes(IASNode iNode)
    {
        List<IASNode> result = new ArrayList<IASNode>();
        for(int i = 0, count = iNode.getChildCount(); i < count; i++)
        {
            IASNode child = iNode.getChild(i);
            if(SemanticUtils.isThisKeyword(child))
            {
                result.add(child);
            }
            else if(!child.isTerminal() && !(child instanceof IFunctionNode))
            {
                result.addAll(findThisIdentifierNodes(child));
            }
        }
        return result;
    }

    public void checkNativeMethod(IASNode iNode)
    {
        if( iNode instanceof FunctionNode )
        {
            if ( ((FunctionNode)iNode).hasBody() )
            {
                addProblem(new NativeMethodWithBodyProblem(iNode));
            }
        }
    }

    private IInterfaceDefinition iEventDispatcher(){
        String iEventDispatcherPackage = BindableHelper.NAME_IEVENT_DISPATCHER.getQualifiers().getSingleQualifier().getName();
        String iEventDispatcherName = BindableHelper.NAME_IEVENT_DISPATCHER.getBaseName();
        IDefinition iEventDispatcher =  ((ASProjectScope)(project.getScope())).findDefinitionByName(iEventDispatcherPackage + "." + iEventDispatcherName);
        if (iEventDispatcher instanceof IInterfaceDefinition) return (IInterfaceDefinition) iEventDispatcher;
        return null;
    }

    /**
     *  Check a function call.
     */
    public void checkFunctionCall(IASNode iNode, Binding method_binding, Vector<? extends Object>actuals)
    {
        //  Skip synthetic calls.
        if ( method_binding.getName() == null )
            return;

        //  Check for calls to attributes.
        if ( method_binding.getName().isAttributeName() )
        {
            addProblem(new AttributesAreNotCallableProblem(roundUpUsualSuspects(method_binding, iNode)));
        }

        IDefinition def = method_binding.getDefinition();
        if (def == null) {
            IDefinition defCheck = SemanticUtils.getDefinitionOfUnderlyingType(iNode,true, project);
            if (defCheck instanceof IClassDefinition) {
                //if we have the bindable IEventDispatcher implementation either at this level or via ancestry, then we should allow those IEventDispatcher methods
                boolean assumeBindableIEventDispatcher = false;
                IClassDefinition.IClassIterator classIterator = ((IClassDefinition) defCheck).classIterator(project, true);
                while (classIterator.hasNext())
                {
                    defCheck = classIterator.next();
                    if (((IClassDefinition)defCheck).needsEventDispatcher(project)) {
                        assumeBindableIEventDispatcher = true;
                        break;
                    }
                }
                if (assumeBindableIEventDispatcher) {
                    IInterfaceDefinition iEventDispatcher =  iEventDispatcher();
                    IDefinitionSet bindingMethodCheck = iEventDispatcher.getContainedScope().getLocalDefinitionSetByName( method_binding.getName().getBaseName());
                    if (bindingMethodCheck!=null && bindingMethodCheck.getSize() == 1) {
                        def = bindingMethodCheck.getDefinition(0);
                    }
                }
            }
        }

        if ( def == null && utils.definitionCanBeAnalyzed(method_binding) )
        {
            if ( utils.isInaccessible(iNode, method_binding) )
            {
            	if (!method_binding.getName().getBaseName().equals("toString"))
	                addProblem(new InaccessibleMethodReferenceProblem( 
	                    roundUpUsualSuspects(method_binding, iNode), 
	                    method_binding.getName().getBaseName(),
	                    utils.getTypeOfStem(iNode)
	                ));
            }
            else if ( SemanticUtils.hasExplicitStem(iNode) && utils.hasUnderlyingType(iNode) )
            {

                addProblem(new StrictUndefinedMethodProblem( 
                    roundUpUsualSuspects(method_binding, iNode), 
                    method_binding.getName().getBaseName(),
                    utils.getTypeOfStem(iNode)
                ));
            }
            else
            {
                addProblem(new CallUndefinedMethodProblem( 
                    roundUpUsualSuspects(method_binding, iNode), 
                    method_binding.getName().getBaseName()
                ));
            }
        }
        else if ( def instanceof AccessorDefinition )
        {
            AccessorDefinition accessorDef = (AccessorDefinition)def;
            IDefinition accessorType = accessorDef.resolveType(project);
            if (accessorType != null && // Null here means the ANY_TYPE
                    (accessorType.equals(project.getBuiltinType(BuiltinType.NUMBER)) ||
                    accessorType.equals(project.getBuiltinType(BuiltinType.BOOLEAN)) ||
                    accessorType.equals(project.getBuiltinType(BuiltinType.INT)) ||
                    accessorType.equals(project.getBuiltinType(BuiltinType.UINT)) ||
                    accessorType.equals(project.getBuiltinType(BuiltinType.STRING))))
            {
                addProblem(new CallNonFunctionProblem(iNode, method_binding.getName().getBaseName()));
            }
        }
        else if ( def instanceof FunctionDefinition )
        {
            FunctionDefinition func = (FunctionDefinition)def;
            checkFormalsVsActuals(iNode, func, actuals);
        }
        else if ( def instanceof VariableDefinition )
        {
            VariableDefinition varDef = (VariableDefinition)def;
            IDefinition varType = varDef.resolveType(project);
            if (varType != null && // Null here means the ANY_TYPE
                    (varType.equals(project.getBuiltinType(BuiltinType.NUMBER)) ||
                    varType.equals(project.getBuiltinType(BuiltinType.BOOLEAN)) ||
                    varType.equals(project.getBuiltinType(BuiltinType.INT)) ||
                    varType.equals(project.getBuiltinType(BuiltinType.UINT)) ||
                    varType.equals(project.getBuiltinType(BuiltinType.STRING))))
            {
                addProblem(new CallNonFunctionProblem(iNode, method_binding.getName().getBaseName()));
            }
        }
        else if ( def == project.getBuiltinType(BuiltinType.ARRAY) )
        {
            // Warn about calling Array as a function because developers
            // may not understand that this always creates a new array.
            // The warning is different when there is one argument
            // of type Array, Object, or *; in that case developers
            // may think they are downcasting.
            boolean downcast = false;
            if (actuals.size() == 1)
            {
                IExpressionNode argument = ((IFunctionCallNode)iNode).getArgumentNodes()[0];
                IDefinition argumentType = argument.resolveType(project);
                
                if (argumentType == null || // Null here means the ANY_TYPE
                    argumentType.equals(project.getBuiltinType(BuiltinType.ARRAY)) ||
                    argumentType.equals(project.getBuiltinType(BuiltinType.OBJECT)) ||
                    argumentType.equals(project.getBuiltinType(BuiltinType.ANY_TYPE)))
                {
                     downcast = true;
                }
            }
            if (downcast)
                addProblem(new ArrayDowncastProblem(iNode));
            else
                addProblem(new ArrayCastProblem(iNode));
        }
        else if (def != null &&
                 // If def is an AmbiguousDefinition,
                 // getQualifiedName() will throw an exception.
                 !AmbiguousDefinition.isAmbiguous(def) &&
                 def.getQualifiedName().equals(IASLanguageConstants.Date))
        {
            if (actuals.size() > 0)
                addProblem(new DateCastProblem(iNode));
        }
        else if ( def instanceof ITypeDefinition )
        {
            // We've already handled the special cases of Array(...) and Date(...)
            // For other cast-like calls, there should be one and only one parameter.
            switch ( actuals.size() )
            {
                case 0:
                {
                        addProblem(new TooFewFunctionParametersProblem(iNode, 1));
                    break;
                    }
                case 1:
                {
                    //  Correct number of parameters.
                        break;
                }
                default:
                {
                    addProblem(new TooManyFunctionParametersProblem(iNode, 1));
                    break;
            }
        }
        }
        checkReference(method_binding);
    }

    /**
     *  Check a function definition.
     *  @param iNode - the top-level definition node.
     *  @param def - the function's definition.
     */
    public void checkFunctionDefinition(IFunctionNode iNode, FunctionDefinition def )
    {
        SemanticUtils.checkReturnValueHasNoTypeDeclaration(this.currentScope, iNode, def);
        SemanticUtils.checkParametersHaveNoTypeDeclaration(this.currentScope, iNode, def);
        
        if (SemanticUtils.isInFunction(iNode))
        {
            if (iNode instanceof BaseDefinitionNode)
                checkForNamespaceInFunction((BaseDefinitionNode)iNode, currentScope);
        }
        ParameterDefinition[] formals = def.getParameters();

        if ( formals == null )
            return;

        boolean found_optional = false;

        for ( int i = 0; i < formals.length; i++ )
        {
            //  Check the structure of the formals; 
            //  required parameters, then optionals,
            //  then the rest parameter, if present.
            if ( formals[i].hasDefaultValue() )
            {
                found_optional = true;
                if( def instanceof ISetterDefinition )
                {
                    addProblem(new SetterCannotHaveOptionalProblem(formals[i].getNode()));
                }
            }
            else if ( formals[i].isRest() )
            {
                if ( i != formals.length -1 )
                    addProblem(new RestParameterMustBeLastProblem(formals[i].getNode()));
                // TODO: Add a check for kError_InvalidRestDecl once CMP-279 is fixed.
            }
            else
            {
                if ( found_optional )
                {
                    addProblem(new RequiredParameterAfterOptionalProblem(formals[i].getNode()));
                }
            }

        }

        //  Check the return type.
        TypeDefinitionBase return_type = (TypeDefinitionBase)def.resolveReturnType(project);
        if ( return_type == null )
        {
            //  Error already emitted.
        }
        // Setter return type can only be 'void' or '*'
        else if( def instanceof ISetterDefinition &&
                // Error says return type must be void, but ASC allows '*' as well
                (return_type != project.getBuiltinType(BuiltinType.VOID) &&
                 return_type != project.getBuiltinType(BuiltinType.ANY_TYPE)) )
        {
            addProblem(new BadSetterReturnTypeProblem(iNode.getReturnTypeNode()));
        }

        if( def instanceof IAccessorDefinition )
        {
            IAccessorDefinition accessorDef = (IAccessorDefinition)def;
            ITypeDefinition thisType = accessorDef.resolveType(project);
            IAccessorDefinition other = null;
            if( (other = accessorDef.resolveCorrespondingAccessor(project)) != null)
            {
                ITypeDefinition otherType = other.resolveType(project);
                
                IDefinition anyType = project.getBuiltinType(BuiltinType.ANY_TYPE);

                if( otherType != thisType
                        // Don't complain if one of the types is '*'
                        && otherType != anyType && thisType != anyType )
                {
                    addProblem(new AccessorTypesMustMatchProblem(getAccessorTypeNode(iNode)));
                }
            }
            if( def instanceof IGetterDefinition )
            {
                if (formals.length > 0 )
                {
                    addProblem(new GetterCannotHaveParametersProblem(formals[0].getNode()));
                }
                
                if (SemanticUtils.isBuiltin(thisType, BuiltinType.VOID, project))
                {
                    addProblem( new GetterMustNotBeVoidProblem(iNode.getReturnTypeNode()));
                }
            }
       
            if( def instanceof ISetterDefinition )
            {
                if( formals.length != 1 )
                {
                    addProblem(new SetterMustHaveOneParameterProblem(iNode.getNameExpressionNode()));
                }
            }
        }

        checkNamespaceOfDefinition(iNode, def, project);
    }

    /**
     * Helper to get the node to report a problem with the "type" of a getter/setter.  Will return the
     * return type node for a setter, or the first parameter node for a getter, or the name node of the
     * accessor if the return type or parameter does not exist
     */
    private IASNode getAccessorTypeNode(IFunctionNode iNode)
    {
        IASNode result = iNode.getNameExpressionNode();
        if( iNode.isSetter() )
        {
            IExpressionNode returnType = iNode.getReturnTypeNode();
            if( returnType != null )
                result = returnType;
        }
        else if( iNode.isGetter() )
        {
            IParameterNode[] params = iNode.getParameterNodes();
            if( params != null && params.length > 0)
                result = params[0];
        }
        return result;
    }

    /**
     *  Check a simple name reference.
     */
    public void checkSimpleName(IASNode iNode, Binding binding)
    {
        if ( SemanticUtils.isThisKeyword(iNode) )
        {
           LanguageIdentifierNode.Context context = ((LanguageIdentifierNode)iNode).getContext();

           if ( context == LanguageIdentifierNode.Context.STATIC_CONTEXT || context == LanguageIdentifierNode.Context.PACKAGE_CONTEXT)
           {
               addProblem(new ThisUsedInStaticFunctionProblem(iNode));
           }
        }
        else if ( SemanticUtils.isArgumentsReference(binding) )
        {
            FunctionDefinition functionDef = SemanticUtils.getFunctionDefinition(iNode);
            if (functionDef != null)
            {
                ParameterDefinition[] parameters = functionDef.getParameters();
                for (ParameterDefinition param : parameters)
                {
                    if (param.isRest())
                    {
                        addProblem(new RestParamAndArgumentsUsedTogetherProblem(iNode));
                        break;
                    }
                }
            }
        }

        if( binding.getDefinition() == null && isPackageReference(binding) )
        {
            // A simple name only counts as a package reference if it did not resolve to anything
            addProblem(new PackageCannotBeUsedAsValueProblem(iNode, binding.getName().getBaseName()));
        }
    }

    /**
     * Translate a {@link PooledValue} into a {@link BuiltinType}.
     * @param value The {@link PooledValue} to translate.
     * @return The {@link BuiltinType} for the specified {@link PooledValue}.
     */
    private static BuiltinType getBuiltinTypeOfPooledValue(PooledValue value)
    {
        switch ( value.getKind() )
        {
            case ABCConstants.CONSTANT_Int:
                return BuiltinType.INT;
            case ABCConstants.CONSTANT_UInt:
                return BuiltinType.UINT;
            case ABCConstants.CONSTANT_Double:
                return BuiltinType.NUMBER;
            case ABCConstants.CONSTANT_Utf8:
                return BuiltinType.STRING;
            case ABCConstants.CONSTANT_True:
            case ABCConstants.CONSTANT_False:
                return BuiltinType.BOOLEAN;
            case ABCConstants.CONSTANT_Undefined:
                return BuiltinType.VOID;
            case ABCConstants.CONSTANT_Null:
                return BuiltinType.NULL;
            case ABCConstants.CONSTANT_Namespace:
            case ABCConstants.CONSTANT_PrivateNs:
            case ABCConstants.CONSTANT_PackageNs:
            case ABCConstants.CONSTANT_PackageInternalNs:
            case ABCConstants.CONSTANT_ProtectedNs:
            case ABCConstants.CONSTANT_ExplicitNamespace:
            case ABCConstants.CONSTANT_StaticProtectedNs:
                return BuiltinType.NAMESPACE;
            default:
                assert false : "Unknown default value kind " + value.getKind();
        }
        return BuiltinType.ANY_TYPE;
    }
    
    /**
     *  Translate a {@link PooledValue} into an {@link IDefinition} type.
     *  @return the {@link IDefinition} for the value's type.
     */
    private IDefinition getTypeOfPooledValue(PooledValue value)
    {
        BuiltinType builtinType = getBuiltinTypeOfPooledValue(value);

        return utils.getBuiltinType(builtinType);
    }

    /**
     * Helper method called by
     * {@link #checkInitialValue(IVariableNode, Binding, PooledValue)}.
     * <p>
     * Conversion rules:
     * <table>
     * <tr>
     * <td></td>
     * <td>*</td>
     * <td>Object</td>
     * <td>void</td>
     * <td>Array</td>
     * <td>XML</td>
     * <td>Function</td>
     * <td>Class</td>
     * <td>String</td>
     * <td>Boolean</td>
     * <td>Number</td>
     * <td>uint</td>
     * <td>int</td>
     * </tr>
     * <tr>
     * <td>CONSTANT_Int</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>Error/transform to null</td> <!-- Array -->
     * <td>Error/transform to null</td> <!-- XML -->
     * <td>Error/transform to null</td> <!-- Function -->
     * <td>Error/transform to null</td> <!-- Class -->
     * <td>Error/coerce to string</td> <!-- String -->
     * <td>Error/coerce to Boolean</td> <!-- Boolean -->
     * <td>No Error</td> <!-- Number -->
     * <td>If negative error and coerce otherwise no error</td> <!-- uint -->
     * <td>-</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_UInt</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>Error/transform to null</td> <!-- Array -->
     * <td>Error/transform to null</td> <!-- XML -->
     * <td>Error/transform to null</td> <!-- Function -->
     * <td>Error/transform to null</td> <!-- Class -->
     * <td>Error/coerce to string</td> <!-- String -->
     * <td>Error/coerce to Boolean</td> <!-- Boolean -->
     * <td>No Error</td> <!-- Number -->
     * <td>-</td> <!-- uint -->
     * <td>Error/coerce if not an integer in int range</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_Double</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>Error/transform to null</td> <!-- Array -->
     * <td>Error/transform to null</td> <!-- XML -->
     * <td>Error/transform to null</td> <!-- Function -->
     * <td>Error/transform to null</td> <!-- Class -->
     * <td>Error/coerce to string</td> <!-- String -->
     * <td>Error/coerce to Boolean</td> <!-- Boolean -->
     * <td>-</td> <!-- Number -->
     * <td>Error/coerce if not a positive error in uint range</td> <!-- uint -->
     * <td>Error/coerce if not an integer in int range</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_Utf8</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>Error/transform to null</td> <!-- Array -->
     * <td>Error/transform to null</td> <!-- XML -->
     * <td>Error/transform to null</td> <!-- Function -->
     * <td>Error/transform to null</td> <!-- Class -->
     * <td>-</td> <!-- String -->
     * <td>Error/coerce to Boolean</td> <!-- Boolean -->
     * <td>Error/coerce to Number</td> <!-- Number -->
     * <td>Error/coerce to uint</td> <!-- uint -->
     * <td>Error/coerce to int</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_True</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>Error/transform to null</td> <!-- Array -->
     * <td>Error/transform to null</td> <!-- XML -->
     * <td>Error/transform to null</td> <!-- Function -->
     * <td>Error/transform to null</td> <!-- Class -->
     * <td>Error/coerce to string</td> <!-- String -->
     * <td>-</td> <!-- Boolean -->
     * <td>Error/coerce to Number</td> <!-- Number -->
     * <td>Error/coerce to uint</td> <!-- uint -->
     * <td>Error/coerce to int</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_False</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>Error/transform to null</td> <!-- Array -->
     * <td>Error/transform to null</td> <!-- XML -->
     * <td>Error/transform to null</td> <!-- Function -->
     * <td>Error/transform to null</td> <!-- Class -->
     * <td>Error/coerce to string</td> <!-- String -->
     * <td>-</td> <!-- Boolean -->
     * <td>Error/coerce to Number</td> <!-- Number -->
     * <td>Error/coerce to uint</td> <!-- uint -->
     * <td>Error/coerce to int</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_Undefined</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>No error</td> <!-- Array -->
     * <td>No error</td> <!-- XML -->
     * <td>No error</td> <!-- Function -->
     * <td>No error</td> <!-- Class -->
     * <td>No error</td> <!-- String -->
     * <td>No error</td> <!-- Boolean -->
     * <td>No error</td> <!-- Number -->
     * <td>No error</td> <!-- uint -->
     * <td>No error</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_Null</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>No error</td> <!-- Array -->
     * <td>No error</td> <!-- XML -->
     * <td>No error</td> <!-- Function -->
     * <td>No error</td> <!-- Class -->
     * <td>No error</td> <!-- String -->
     * <td>Error/coerce to Boolean</td> <!-- Boolean -->
     * <td>Error/coerce to Number</td> <!-- Number -->
     * <td>Error/coerce to uint</td> <!-- uint -->
     * <td>Error/coerce to int</td> <!-- int -->
     * </tr>
     * <tr>
     * <td>CONSTANT_Namespace, CONSTANT_*Ns</td>
     * <td>No error</td> <!-- * -->
     * <td>No error</td> <!-- Object -->
     * <td>No error</td> <!-- void -->
     * <td>Error/transform to null</td> <!-- Array -->
     * <td>Error/transform to null</td> <!-- XML -->
     * <td>Error/transform to null</td> <!-- Function -->
     * <td>Error/transform to null</td> <!-- Class -->
     * <td>Error/transform to null</td> <!-- String -->
     * <td>Error/transform to true</td> <!-- Boolean -->
     * <td>Error/transform to 0</td> <!-- Number -->
     * <td>Error/transform to 0</td> <!-- uint -->
     * <td>Error/transform to 0</td> <!-- int -->
     * </tr>
     * <tr>
     * </table>
     * 
     * @param initial_value_location The {@link ISourceLocation} for the
     * variable's initializer expression.
     * @param desired_type {@link IDefinition} that the initial value's type
     * should be compatible with.
     * @param initial_value {@link PooledValue} containing the constant
     * initializer for the variable.
     * @return A {@link PooledValue} whose type is compatible with desired_type.
     */
    private PooledValue checkInitialValue(ISourceLocation initial_value_location, IDefinition desired_type, PooledValue initial_value)
    {
        IDefinition value_type = getTypeOfPooledValue(initial_value);
        if  (desired_type == null ||
             desired_type.equals(value_type) ||
             utils.isInstanceOf(value_type, desired_type))
        {
            return initial_value;
        }
        else if (utils.isBuiltin(desired_type, BuiltinType.OBJECT) ||
                 utils.isBuiltin(desired_type, BuiltinType.ANY_TYPE))
        {
            return initial_value;
        }
        else if (utils.isBuiltin(value_type, BuiltinType.VOID))
        {
            return initial_value;
        }
        else if (
            utils.isBuiltin(desired_type, BuiltinType.ARRAY) ||
            utils.isBuiltin(desired_type, BuiltinType.XML) ||
            utils.isBuiltin(desired_type, BuiltinType.FUNCTION) ||
            utils.isBuiltin(desired_type, BuiltinType.CLASS)
            )
        {
            if (utils.isBuiltin(value_type, BuiltinType.NULL))
                return initial_value;
            addProblem(new IncompatibleInitializerTypeProblem(initial_value_location, value_type.getBaseName(), desired_type.getBaseName(), "null"));
            // transform initial_value to null.
            return new PooledValue(ABCConstants.NULL_VALUE);
        }
        else if (utils.isBuiltin(desired_type, BuiltinType.STRING))
        {
            assert !(utils.isBuiltin(value_type, BuiltinType.STRING));
            if (utils.isBuiltin(value_type, BuiltinType.NULL))
                return initial_value;
            String initial_value_string = ECMASupport.toString(initial_value.getValue());
            String initial_value_quoted = "\"" + initial_value_string + "\"";
            addProblem(new IncompatibleInitializerTypeProblem(initial_value_location, value_type.getBaseName(), desired_type.getBaseName(), initial_value_quoted));
            // transform initial_value to a string constant.
            return new PooledValue(initial_value_string);
        }
        else if (utils.isBuiltin(desired_type, BuiltinType.BOOLEAN))
        {
            assert !(utils.isBuiltin(value_type, BuiltinType.BOOLEAN));
            // transform initial_value to a boolean constant.
            boolean initial_value_boolean = ECMASupport.toBoolean(initial_value.getValue());
            // tell the programmer about the transformation we did.
            addProblem(new IncompatibleInitializerTypeProblem(initial_value_location, value_type.getBaseName(), desired_type.getBaseName(), String.valueOf(initial_value_boolean)));
            return new PooledValue(initial_value_boolean);
                
        }
        else if (utils.isBuiltin(desired_type, BuiltinType.NUMBER))
        {
            if (utils.isBuiltin(value_type, BuiltinType.INT) ||
                utils.isBuiltin(value_type, BuiltinType.UINT) ||
                utils.isBuiltin(value_type, BuiltinType.NUMBER))
                return initial_value;
            Number initial_value_numeric = ECMASupport.toNumeric(initial_value.getValue());
            double initial_value_double = initial_value_numeric != null ? initial_value_numeric.doubleValue() : 0;
            addProblem(new IncompatibleInitializerTypeProblem(initial_value_location, value_type.getBaseName(), desired_type.getBaseName(), String.valueOf(initial_value_double)));
            return new PooledValue(initial_value_double);
        }
        else if (utils.isBuiltin(desired_type, BuiltinType.UINT))
        {
            return checkInitialUIntValue(initial_value_location, desired_type, initial_value, value_type);
        }
        else if (utils.isBuiltin(desired_type, BuiltinType.INT))
        {
            return checkInitialIntValue(initial_value_location, desired_type, initial_value, value_type);
        }
        
        addProblem(new IncompatibleInitializerTypeProblem(initial_value_location, value_type.getBaseName(), desired_type.getBaseName(), "null"));
        return new PooledValue(ABCConstants.NULL_VALUE);
    }

    /**
     * Helper method called by
     * {@link #checkInitialValue(ISourceLocation, IDefinition, PooledValue)}.
     * 
     * @param initial_value_location The {@link ISourceLocation} for the
     * variable's initializer expression.
     * @param desired_type {@link IDefinition} that the initial value's type
     * should be compatible with. This should always by the {@link IDefinition}
     * for int. This is passed in as a convenience.
     * @param initial_value {@link PooledValue} containing the constant
     * initializer for the variable.
     * @param value_type {@link IDefinition} for the type of value contained by
     * the specified {@link PooledValue}.
     * @return A {@link PooledValue} whose type is compatible with desired_type.
     */
    private PooledValue checkInitialIntValue(ISourceLocation initial_value_location, IDefinition desired_type, PooledValue initial_value, IDefinition value_type)
    {
        assert !(utils.isBuiltin(value_type, BuiltinType.INT));
        if (utils.isBuiltin(value_type, BuiltinType.UINT))
        {
            long initial_value_long = initial_value.getLongValue();
            if (initial_value_long > Integer.MAX_VALUE)
                return addIntOutOfRangeProblem(initial_value_location, desired_type, initial_value_long);
            else
                return initial_value;
        }
        else if (utils.isBuiltin(value_type, BuiltinType.NUMBER))
        {
            double initial_value_double = initial_value.getDoubleValue();
            double initial_value_rounded = ECMASupport.toInteger(initial_value_double);
            int initial_value_int = ECMASupport.toInt32(initial_value_double);
            if (initial_value_rounded != initial_value_double)
            {
                addProblem(new InitializerValueNotAnIntegerProblem(
                        initial_value_location,
                        desired_type.getBaseName(),
                        String.valueOf(initial_value_double), String.valueOf(initial_value_int)));
                return new PooledValue(initial_value_int);
            }
            else if ((initial_value_rounded < Integer.MIN_VALUE) || (initial_value_rounded > Integer.MAX_VALUE))
            {
                return addIntOutOfRangeProblem(initial_value_location, desired_type, initial_value_rounded);
            }
            else
            {
                // transform the number that happens to be an integer into
                // an integer.
                return new PooledValue(initial_value_int);
            }
        }
        else
        {
            Number initial_value_numeric = ECMASupport.toNumeric(initial_value.getValue());
            double initial_value_double = initial_value_numeric != null ? initial_value_numeric.doubleValue() : 0;
            int initial_value_int = ECMASupport.toInt32(initial_value_double);
            addProblem(new IncompatibleInitializerTypeProblem(initial_value_location, value_type.getBaseName(), desired_type.getBaseName(), String.valueOf(initial_value_int)));
            return new PooledValue(initial_value_int);
        }
    }

    /**
     * Helper method called by
     * {@link #checkInitialValue(ISourceLocation, IDefinition, PooledValue)}.
     * 
     * @param initial_value_location The {@link ISourceLocation} for the
     * variable's initializer expression.
     * @param desired_type {@link IDefinition} that the initial value's type
     * should be compatible with. This should always by the {@link IDefinition}
     * for uint. This is passed in as a convenience.
     * @param initial_value {@link PooledValue} containing the constant
     * initializer for the variable.
     * @param value_type {@link IDefinition} for the type of value contained by
     * the specified {@link PooledValue}.
     * @return A {@link PooledValue} whose type is compatible with desired_type.
     */
    private PooledValue checkInitialUIntValue(ISourceLocation initial_value_location, IDefinition desired_type, PooledValue initial_value, IDefinition value_type)
    {
        assert !(utils.isBuiltin(value_type, BuiltinType.UINT));
        if (utils.isBuiltin(value_type, BuiltinType.INT))
        {
            int initial_value_int = initial_value.getIntegerValue();
            
            if (initial_value_int < 0)
                return addUIntOutOfRangeProblem(initial_value_location, desired_type, initial_value_int);
            else
                return initial_value;
        }
        else if (utils.isBuiltin(value_type, BuiltinType.NUMBER))
        {
            double initial_value_double = initial_value.getDoubleValue();
            double initial_value_int = ECMASupport.toInteger(initial_value_double);
            long initial_value_long = ECMASupport.toUInt32(initial_value_int);
            if (initial_value_double != initial_value_int)
            {
                addProblem(new InitializerValueNotAnIntegerProblem(
                        initial_value_location,
                        desired_type.getBaseName(),
                        String.valueOf(initial_value_double), String.valueOf(initial_value_long)));
                return new PooledValue(initial_value_long);
            }
            else if ((initial_value_int < 0) || (initial_value_int > 0xFFFFFFFFL))
            {
                return addUIntOutOfRangeProblem(initial_value_location, desired_type, initial_value_int);
            }
            else
            {
                // transform the number that happens to be an unsigned integer into
                // an unsigned integer.
                return new PooledValue(initial_value_long);
            }
        }
        else
        {
            Number initial_value_numeric = ECMASupport.toNumeric(initial_value.getValue());
            double initial_value_double = initial_value_numeric != null ? initial_value_numeric.doubleValue() : 0;
            long initial_value_long = ECMASupport.toUInt32(initial_value_double);
            addProblem(new IncompatibleInitializerTypeProblem(initial_value_location, value_type.getBaseName(), desired_type.getBaseName(), String.valueOf(initial_value_long)));
            return new PooledValue(initial_value_long);
        }
    }
    
    private PooledValue addUIntOutOfRangeProblem(ISourceLocation initial_value_location, IDefinition required_type, double initial_value_int)
    {
        long initial_value_long = ECMASupport.toUInt32(initial_value_int);
        addProblem(new InitializerValueOutOfRangeProblem(
                initial_value_location, 
                required_type.getBaseName(),
                String.valueOf((long)initial_value_int),
                "0",
                String.valueOf(0xFFFFFFFFL),
                String.valueOf(initial_value_long)));
        return new PooledValue(initial_value_long);
    }

    private PooledValue addIntOutOfRangeProblem(ISourceLocation initial_value_location, IDefinition required_type, double initial_value_double)
    {
        int initial_value_int = ECMASupport.toInt32(initial_value_double);
        addProblem(new InitializerValueOutOfRangeProblem(
                initial_value_location, 
                required_type.getBaseName(),
                String.valueOf((long)initial_value_double),
                String.valueOf(Integer.MIN_VALUE),
                String.valueOf(Integer.MAX_VALUE),
                String.valueOf(initial_value_int)));
        return new PooledValue(initial_value_int);
    }


    /**
     *  Check a getproperty operation.
     *  @param binding - the Binding which is being fetched.
     */
    public void checkGetProperty(Binding binding)
    {
        Name name = binding.getName();
        assert name != null;

        if ( utils.isWriteOnlyDefinition(binding.getDefinition()) )
        {
            addProblem(new PropertyIsWriteOnlyProblem(
                binding.getNode(),
                name.getBaseName()
            ));
        }
        
        switch ( name.getKind() )
        {
            case ABCConstants.CONSTANT_QnameA:
            case ABCConstants.CONSTANT_MultinameA:
            case ABCConstants.CONSTANT_MultinameLA:
            case ABCConstants.CONSTANT_RTQnameA:
            case ABCConstants.CONSTANT_RTQnameLA:
                {
                    //  TODO: Attribute checks
                    break;
                }
            case ABCConstants.CONSTANT_TypeName:
                {
                    //  TODO: Type name checks
                    break;
                }
            case ABCConstants.CONSTANT_RTQname:
            case ABCConstants.CONSTANT_RTQnameL:
            case ABCConstants.CONSTANT_MultinameL:
                {
                    //  Not much to be done with these.
                }
            default:
                {
                    if ( binding.getDefinition() == null && SemanticUtils.definitionCanBeAnalyzed(binding, this.project) )
                    {
                        addProblem(accessUndefinedProperty(binding, binding.getNode()));
                    }
                    checkReference(binding);
                }
        }
    }

    /**
     * Check for possible problems with a reference for an r-value.
     * This will find 2 kinds of problems:
     *  1. Ambiguous references
     *  2. References to deprecated symbols
     *
     * @param b the binding to check
     */
    public void checkReference(Binding b)
    {
        checkReference(b, false);
    }

    /**
     * Check for possible problems with a reference.
     * This will find 2 kinds of problems:
     *  1. Ambiguous references
     *  2. References to deprecated symbols
     *
     * @param b the binding to check
     * @param forLValue Whether the binding is for an l-value.
     */
    public void checkReference(Binding b, boolean forLValue)
    {
        if (b != null)
        {
            IDefinition definition = b.getDefinition();
            
            if (definition != null)
            {
                // Since the BURM doesn't currently understand whether a Binding
                // is for an l-value or an r-value, a Binding for an l-value
                // may contain a getter and a Binding for an r-value may contain
                // a setter. If that's the case, we need to find the corresponding
                // accessor.
                if (forLValue && definition instanceof IGetterDefinition ||
                    !forLValue && definition instanceof ISetterDefinition)
                {
                     definition = ((IAccessorDefinition)definition).resolveCorrespondingAccessor(project);
                }
                
                IASNode node = b.getNode();

                checkAmbiguousReference(b);
                checkDeprecated(node, definition);
            }
        }
    }

    /**
     * Log a problem if the binding is an ambiguous reference
     * @param b the binding to check
     */
    private void checkAmbiguousReference(Binding b)
    {
        if( SemanticUtils.isAmbiguousReference(b) )
        {
            addProblem(new AmbiguousReferenceProblem(b.getNode(), b.getName().getBaseName()));
        }
    }

    /**
     * Log a problem if the definition passed in has been marked deprecated
     * and the node passed in is not within a deprecated API.
     * 
     * @param site  the Node to use to obtain location info for any problems
     * @param def   the definition to check
     */
    private void checkDeprecated(IASNode site, IDefinition def)
    {
        if (def != null && def.isDeprecated())
        {
            if (!SemanticUtils.hasDeprecatedAncestor(site))
            {
                ICompilerProblem problem = SemanticUtils.createDeprecationProblem(def, site);
                addProblem(problem);
            }
        }
    }

    /**
     * Check the initial value of an {@link IVariableNode}. The specified
     * {@link IVariableNode} could be:
     * <ul>
     * <li>an {@link IParameterNode} for a function parameter.</li>
     * <li>an {@link IVariableNode} for a var or const in any scope.</li>
     * </ul>
     * This method will create {@link ICompilerProblem}s if the specified
     * initial value is not the correct type and will return an initializer
     * value that is the correct type that was created by apply the AS3 coercion
     * rules.
     * 
     * @param iNode The {@link IVariableNode} whose initializer value should be
     * checked.
     * @param type A {@link Binding} for the specified {@link IVariableNode}'s
     * type annotation.
     * @param initial_value A {@link PooledValue} that contains the constant
     * initializer value computed by the constant propagation code.
     * @return A {@link PooledValue} with the correct type to initialize the
     * specified {@link IVariableNode}. This can be the same as the specified
     * {@link PooledValue} if it was already of the correct type.
     */
    public PooledValue checkInitialValue(IVariableNode iNode, Binding type, PooledValue initial_value)
    {
        assert initial_value != null : "Caller's should generate a compiler problem when a default_value is missing and *not* call this method!";
        ISourceLocation assignedValueExpressionLocation = iNode.getAssignedValueNode();
        IDefinition target_type = type.getDefinition();
        
        return checkInitialValue(assignedValueExpressionLocation, target_type, initial_value);
        
    }

    /**
     *  Analyze an undefined binding and create an appropriate problem.
     *  @return the problem that best fits this error condition.
     */
    private ICompilerProblem accessUndefinedProperty(Binding b, IASNode iNode)
    {
        ICompilerProblem problem;
        
        String unknown_name = null;

        if ( b.getName() != null )
            unknown_name = b.getName().getBaseName();
        else if (SemanticUtils.isThisKeyword(iNode))
            unknown_name = "this";
        else
            return null;

        if ( b.getNode() instanceof MemberAccessExpressionNode )
        {
            MemberAccessExpressionNode maex = (MemberAccessExpressionNode)b.getNode();

            if ( maex.stemIsPackage() && maex.getLeftOperandNode() instanceof IdentifierNode )
            {
                return new AccessUndefinedPropertyInPackageProblem(
                    iNode, 
                    unknown_name,
                    ((IdentifierNode)maex.getLeftOperandNode()).getName()
                );
            }
        }
        else if ((problem = isMissingMember(b.getNode())) != null)
        {
            return problem;
        }
        else if ( utils.isInInstanceFunction(iNode) && utils.isInaccessible((ASScope)utils.getEnclosingFunctionDefinition(iNode).getContainingScope(), b) )
        {
            return new InaccessiblePropertyReferenceProblem(
                    iNode, 
                    b.getName().getBaseName(), 
                    utils.getEnclosingClassName(iNode)
                );
        }

        while (iNode.getSourcePath().contains("compiler-jx") &&
        		iNode.getSourcePath().contains("config.as"))
		{
        	iNode = iNode.getParent();
		}
        	
        return new AccessUndefinedPropertyProblem(iNode, unknown_name);
    }
    
    public ICompilerProblem isMissingMember(IASNode iNode)
    {
        if (iNode instanceof IdentifierNode && iNode.getParent() instanceof MemberAccessExpressionNode)
        {
            MemberAccessExpressionNode mae = (MemberAccessExpressionNode)(iNode.getParent());
            if (iNode == mae.getRightOperandNode())
            {
                ITypeDefinition leftDef = mae.getLeftOperandNode().resolveType(project);
                if (!leftDef.isDynamic())
                    return new AccessUndefinedMemberProblem(iNode, ((IdentifierNode)iNode).getName(), leftDef.getQualifiedName());
            }
        }
        return null;
    }
    
    /**
     *  Ensure a super-qualified access is in an instance method.
     */
    public void checkSuperAccess(IASNode iNode)
    {
        IDefinition def = utils.getDefinition(iNode);
        
        //  If def isn't null, then we know this is a valid expression.
        //  if def is null, allow super.foo if the reference is in an
        //  instance function.
        if ( def == null && !utils.isInInstanceFunction(iNode) )
        {
            addProblem(new InvalidSuperExpressionProblem(iNode));
        }
        
        // For super.f(...), check whether f is deprecated.
        if (iNode instanceof IFunctionCallNode)
        {
            IExpressionNode nameNode = ((IFunctionCallNode)iNode).getNameNode();
            IExpressionNode site = ((IMemberAccessExpressionNode)nameNode).getRightOperandNode();
            def = ((IFunctionCallNode)iNode).resolveCalledExpression(project);
            checkDeprecated(site, def);
            if (def == null || def.isAbstract())
            {
            	String methodName = null;
            	if (nameNode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
            	{
            		MemberAccessExpressionNode mae = (MemberAccessExpressionNode)nameNode;
            		IExpressionNode rightNode = mae.getRightOperandNode();
            		if (rightNode.getNodeID() == ASTNodeID.IdentifierID)
            			methodName = ((IdentifierNode)rightNode).getName();
            	}
                assert methodName != null;
                addProblem(new CallUndefinedMethodProblem( 
                        iNode, methodName
                    ));
            }
        }

        if ( this.superState == SuperState.Initial )
            this.superState = SuperState.Armed;
    }

    private boolean isPackageReference(Binding b)
    {
        IASNode n = b.getNode();
        return n instanceof ExpressionNodeBase && ((ExpressionNodeBase)n).isPackageReference();
    }

    /**
     *  Check foo++ and foo-- expressions.
     */
    public void checkIncDec(IASNode iNode, boolean is_incr)
    {
        IASNode operand = ((IUnaryOperatorNode)iNode).getOperandNode();

        checkImplicitConversion(operand, utils.numberType(), null);

        IDefinition def = utils.getDefinition(operand);
        
        if ( utils.isReadOnlyDefinition(def) )
        {
            if ( is_incr )
                addProblem(new InvalidIncrementOperandProblem(iNode));
            else
                addProblem(new InvalidDecrementOperandProblem(iNode));
        }
        else if ( def instanceof SetterDefinition || def instanceof GetterDefinition )
        {
            // No error, just avoid the else if ( def instanceof IFunctionDefinition ) below
        }
        else if ( def instanceof IFunctionDefinition ||  def instanceof ClassDefinition )
        {
            if ( is_incr )
                addProblem(new InvalidIncrementOperandProblem(iNode));
            else
                addProblem(new InvalidDecrementOperandProblem(iNode));
        }
        else if ( SemanticUtils.isConstDefinition(def) )
        {
            //  TODO: See CMP-1177; enhanced error message would be appropriate.
            if ( is_incr )
                addProblem(new InvalidIncrementOperandProblem(iNode));
            else
                addProblem(new InvalidDecrementOperandProblem(iNode));
        }
    }

    /**
     *  Check foo++ and foo-- expressions.
     *  @param iNode  the inc/dec Node
     *  @param is_incr whether this is an increment or decrement operation
     *  @param binding the binding for the name we are incrementing/decrementing
     */
    public void checkIncDec(IASNode iNode, boolean is_incr, Binding binding)
    {
        checkIncDec(iNode, is_incr);
        if ( binding.getName() != null )
        {
            checkGetProperty(binding);
        }
        else
        {
            addProblem(
                is_incr?
                    new IncrementMustBeReferenceProblem(iNode):
                    new DecrementMustBeReferenceProblem(iNode)
            );
        }
    }

    /**
     *  Perform semantic checks on an lvalue,
     *  i.e., the target of an assignment
     *  or other storage-mutating operation.
     */
    public void checkLValue(IASNode iNode, Binding binding)
    {
        IDefinition def = binding.getDefinition();

        if ( SemanticUtils.isThisKeyword(binding.getNode()) )
        {
            addProblem(new AssignToNonReferenceValueProblem(binding.getNode()));
        }
        else if ( def == null && !utils.hasDynamicBase(binding) && !SemanticUtils.isInWith(binding.getNode()))
        {
            addProblem(accessUndefinedProperty(binding, roundUpUsualSuspects(binding, iNode)));
        }
        else if ( def instanceof IConstantDefinition )
        {
            addProblem(new AssignToConstProblem(iNode));
        }
        else if ( utils.isReadOnlyDefinition(def) )
        {
            addProblem(new AssignToReadOnlyPropertyProblem(iNode, binding.getName().getBaseName()));
        }
        else if ( def instanceof SetterDefinition || def instanceof GetterDefinition)
        {
            // No error, just avoid the else if ( def instanceof IFunctionDefinition ) below
        }
        else if ( def instanceof IFunctionDefinition )
        {
            addProblem(new AssignToFunctionProblem(iNode, binding.getName().getBaseName()));
        }
        else if ( def instanceof ClassDefinition )
        {
            addProblem(new IllegalAssignmentToClassProblem(
                roundUpUsualSuspects(binding, iNode),
                binding.getName().getBaseName()
            ));
        }

        checkReference(binding, true);
    }

    /**
     *  Check a member access expression.
     */
    public void checkMemberAccess(IASNode iNode, Binding member, int opcode)
    {
        //  Don't check synthetic bindings.
        IASNode member_node = member.getNode();

        if ( member_node == null )
            return;

        IDefinition def = utils.getDefinition(member_node);

        if ( def == null && utils.definitionCanBeAnalyzed(member) )
        {
            // if it is foo.mx_internal::someProp, just say it passes
            if (member_node.getParent() instanceof NamespaceAccessExpressionNode)
                return;
            
            if ( utils.isInaccessible(iNode, member) )
            {
                addProblem(new InaccessiblePropertyReferenceProblem(
                    member_node, 
                    member.getName().getBaseName(), 
                    utils.getTypeOfStem(iNode))
                );
            }
            else
            {
                ICompilerProblem p = null;
                // Look for the special case of "something." (empty right hand side).
                // Return a less perplexing error for this case.
                if (iNode instanceof IMemberAccessExpressionNode)
                {
                    IMemberAccessExpressionNode maen = (IMemberAccessExpressionNode)iNode;
                    IExpressionNode right = maen.getRightOperandNode();
                    if (right instanceof IIdentifierNode)
                    {
                        if (((IIdentifierNode)right).getName().isEmpty())
                        {
                            p = new MissingPropertyNameProblem(right);
                        }
                    }
                }
                if (p== null)
                {
                    // If right side not blank, then use normal undefined member
                    p = new AccessUndefinedMemberProblem(
                            member_node, 
                            member.getName().getBaseName(), 
                            utils.getTypeOfStem(iNode));
                }
                addProblem(p);
            }
        }
        else if ( utils.isWriteOnlyDefinition(member.getDefinition()) )
        {
            addProblem(new PropertyIsWriteOnlyProblem(
                member_node,
                member.getName().getBaseName()
            ));
        }
        else
        {
            checkReference(member);
        }

    }
    
    /**
     * Check a "new expression".
     * 
     * @param call_node {@link FunctionCallNode} that has the "new" call.
     */
    @SuppressWarnings("incomplete-switch")
	public void checkNewExpr(IASNode call_node)
    {
        final ExpressionNodeBase name = ((FunctionCallNode)call_node).getNameNode();
        if (name instanceof MemberAccessExpressionNode)
        {
            final MemberAccessExpressionNode func_name = (MemberAccessExpressionNode)name;
            final IDefinition def = func_name.resolve(project);
            if ( def instanceof InterfaceDefinition )
            {
                addProblem(new InterfaceCannotBeInstantiatedProblem(call_node));
            }
            else if ( def instanceof ClassDefinition )
            {
                IClassDefinition classDef = (IClassDefinition) def;
                checkPrivateConstructorNewExpr(call_node, null, classDef, classDef.getConstructor());
            }
            else if ( def instanceof GetterDefinition )
            {
                // pass
            }
            else if (def instanceof FunctionDefinition)
            {
                final FunctionDefinition func_def = (FunctionDefinition)def;
                switch (func_def.getFunctionClassification())
                {
                    case CLASS_MEMBER:
                    case INTERFACE_MEMBER:
                        addProblem(new MethodCannotBeConstructorProblem(call_node));
                        break;
                }

                if (func_def.isConstructor())
                {
                    IDefinition class_def = func_def.getParent();
                    checkPrivateConstructorNewExpr(call_node, null, class_def, func_def);
                }
            }
            else if (def == null)
            {
            	addProblem(new UnresolvedClassReferenceProblem(call_node, func_name.getDisplayString()));
            }
        }
    }

    /**
     *  Check a new expression.
     */
    public void checkNewExpr(IASNode iNode, Binding class_binding, Vector<? extends Object> args)
    {
        IDefinition def = class_binding.getDefinition();

        if ( def == null && utils.definitionCanBeAnalyzed(class_binding) && !(class_binding.getName().isTypeName()) )
        {
            // Note: don't have to check accessability because
            // AS3 mandates constructors be public.

            addProblem(new CallUndefinedMethodProblem(
                roundUpUsualSuspects(class_binding, iNode),
                class_binding.getName().getBaseName()
            ));
        }
        else if ( def instanceof InterfaceDefinition )
        {
            addProblem(new InterfaceCannotBeInstantiatedProblem(
                roundUpUsualSuspects(class_binding, iNode)
            ));
        }
        else if ( def instanceof ClassDefinition )
        {
            ClassDefinition class_def = (ClassDefinition)def;

            if ( project.getAllowAbstractClasses() && class_def.isAbstract() )
            {
                addProblem(new AbstractClassCannotBeInstantiatedProblem(
                    roundUpUsualSuspects(class_binding, iNode)
                ));
            }

            IFunctionDefinition ctor = class_def.getConstructor();

            if ( ctor instanceof FunctionDefinition )
            {
                FunctionDefinition func_def = (FunctionDefinition)ctor;
                checkFormalsVsActuals(iNode, func_def, args);
                
                checkPrivateConstructorNewExpr(iNode, class_binding, class_def, func_def);
            }
        }
        else if ( def instanceof GetterDefinition )
        {
        }
        else if ( def instanceof FunctionDefinition )
        {
            FunctionDefinition func_def = (FunctionDefinition) def;

            IFunctionDefinition.FunctionClassification func_type = func_def.getFunctionClassification();

            if (func_def.isConstructor())
            {
                IDefinition class_def = func_def.getParent();
                checkPrivateConstructorNewExpr(iNode, class_binding, class_def, func_def);
            }
            else if ( func_type.equals(IFunctionDefinition.FunctionClassification.CLASS_MEMBER) || func_type.equals(IFunctionDefinition.FunctionClassification.INTERFACE_MEMBER) )
            {
                addProblem(new MethodCannotBeConstructorProblem(
                    roundUpUsualSuspects(class_binding, iNode)
                ));
            }
        }
        else if ( def instanceof IVariableDefinition)
        {
            if (class_binding.isLocal())
            {
                // Note: previously, local variable bindings were not checked at
                // all, but actually, variables of most types cannot be used with a
                // "new" expression -JT

                ITypeDefinition typeDef = def.resolveType(project);
                if (typeDef != null
                        && !SemanticUtils.isBuiltin(typeDef, BuiltinType.CLASS, project)
                        && !SemanticUtils.isBuiltin(typeDef, BuiltinType.FUNCTION, project)
                        && !SemanticUtils.isBuiltin(typeDef, BuiltinType.OBJECT, project)
                        && !SemanticUtils.isBuiltin(typeDef, BuiltinType.ANY_TYPE, project))
                {
                    addProblem(new CallUndefinedMethodProblem(
                        roundUpUsualSuspects(class_binding, iNode),
                        class_binding.getName().getBaseName()
                    ));
                }
            }
        }

        checkReference(class_binding);
    }

    private void checkPrivateConstructorNewExpr(IASNode iNode, Binding class_binding, IDefinition classDef, IFunctionDefinition funcDef)
    {
        if (!project.getAllowPrivateConstructors() || !funcDef.isPrivate())
        {
            return;
        }

        IScopedNode enclosingScope = iNode.getContainingScope();
        
        if (enclosingScope != null)
        {
            boolean needsProblem = true;
            IASScope currentScope = enclosingScope.getScope();
            while (currentScope != null)
            {
                IDefinition currentDef = currentScope.getDefinition();
                if (currentDef instanceof IClassDefinition)
                {
                    needsProblem = !classDef.equals(currentScope.getDefinition());
                    break;
                }
                currentScope = currentScope.getContainingScope();
            }
            if(needsProblem)
            {
                if(class_binding != null)
                {
                    addProblem(new InaccessibleConstructorReferenceProblem(
                        roundUpUsualSuspects(class_binding, iNode), classDef.getQualifiedName()
                    ));
                }
                else
                {
                    addProblem(new InaccessibleConstructorReferenceProblem(
                        iNode, classDef.getQualifiedName()
                    ));
                }
            }
        }
    }

    /**
     *  Check a return expression that returns a value.
     *  @param iNode - the return statement.  
     *    Known to have a child 0 expression.
     */
    public void checkReturnValue(IASNode iNode)
    {
        FunctionDefinition func_def = SemanticUtils.getFunctionDefinition(iNode);
        
        if ( func_def != null )
        {
            try
            {
                IExpressionNode returnExpression = ((IReturnNode)iNode).getReturnValueNode();
                IDefinition return_type = func_def.resolveReturnType(project);

                //  void has its own special type logic.
                if ( ClassDefinition.getVoidClassDefinition().equals(return_type) )
                {
                    IDefinition value_type = ((ExpressionNodeBase)returnExpression).resolveType(project);
                    if ( value_type != null && ! value_type.equals(ClassDefinition.getVoidClassDefinition()) )
                        addProblem(new ReturnValueMustBeUndefinedProblem(returnExpression));
                }
                else if ( func_def.isConstructor() )
                {
                    addProblem(new ReturnValueInConstructorProblem(returnExpression));
                }
                else
                {
                    checkImplicitConversion(returnExpression, return_type, null);
                }
            }
            catch ( Exception namerezo_problem )
            {
                //  Ignore until CMP-869 is fixed.
            }
        }
        else if ( isInPackageDefinition(iNode) )
        {
            addProblem(new ReturnCannotBeUsedInPackageProblem(iNode));
        }
        else if ( isInClassDefinition(iNode) )
        {
            addProblem(new ReturnCannotBeUsedInStaticProblem(iNode));
        }
        else if ( iNode.getAncestorOfType(MXMLDocumentNode.class) != null )
        {
            //  TODO: Remove this once CMP-874 is resolved.
        }
        else
        {
            addProblem(new ReturnCannotBeUsedInGlobalProblem(iNode));
        }

        if ( this.superState == SuperState.Initial )
            this.superState = SuperState.Armed;
    }
  
    /**
     *  Check a return expression that returns void.
     *  @param iNode - the return statement.  
     */
    public void checkReturnVoid(IASNode iNode)
    {
        if ( SemanticUtils.isInFunction(iNode) )
        {
            if ( SemanticUtils.functionMustReturnValue(iNode, currentScope.getProject().getAllowAbstractClasses(), this.project) )
                addProblem(new ReturnMustReturnValueProblem(iNode));
        }
        else if ( isInClassDefinition(iNode) )
        {
            addProblem(new ReturnCannotBeUsedInStaticProblem(iNode));
        }
        else if ( isInPackageDefinition(iNode) )
        {
            addProblem(new ReturnCannotBeUsedInPackageProblem(iNode));
        }
        else if ( iNode.getAncestorOfType(MXMLDocumentNode.class) != null )
        {
            //  TODO: Remove this once CMP-874 is resolved.
        }
        else
        {
            addProblem(new ReturnCannotBeUsedInGlobalProblem(iNode));
        }

        if ( this.superState == SuperState.Initial )
            this.superState = SuperState.Armed;
    }

    /**
     *  Perform semantic checks that require flow-aware analysis.
     *  @param iNode - an AST within a function.
     *  @param mi - the function's MethodInfo.
     *  @param mbi - the function's MethodBodyInfo.
     */
    public void checkControlFlow(IASNode iNode, MethodInfo mi, MethodBodyInfo mbi)
    {
        InstructionList il = mbi.getInstructionList();

        //  Walk the control flow graph if there are any issues that
        //  require it.
        if  ( 
                il.size() > 0 &&
                il.lastElement() == ABCGeneratingReducer.synthesizedReturnVoid && 
                SemanticUtils.functionMustReturnValue(iNode,
                    currentScope.getProject().getAllowAbstractClasses(),
                    this.project)
            )
        {
            for ( IBasicBlock b: mbi.getCfg().blocksInControlFlowOrder() )
            {
                if ( b.size() > 0 )
                {
                    //  If at some point it's necessary to walk the CFG regardless,
                    //  then this check can be for any OP_returnvoid and the alternate
                    //  problem injection site in checkReturnVoid can be removed.
                    if ( b.get(b.size()-1) == ABCGeneratingReducer.synthesizedReturnVoid )
                    {
                        addProblem(new ReturnMustReturnValueProblem(iNode));
                        break;
                    }
                }
            }
        }
    }

    /**
     *  Check a throw statement.
     */
    public void checkThrow(IASNode iNode)
    {
        if ( this.superState == SuperState.Initial )
            this.superState = SuperState.Armed;
    }

    /**
     *  Check an import directive.
     *  @param importNode - the import node.
     */
    public void checkImportDirective(IImportNode importNode)
    {
        IASNode site = importNode.getImportNameNode();
        
        String importAlias = importNode.getImportAlias();
        if (importAlias != null && !project.getAllowImportAliases())
        {
            //import aliases need to be enabled
            addProblem(new SyntaxProblem(importNode, "="));
        }
        
        if (!SemanticUtils.isValidImport(importNode, project, currentScope.getInInvisibleCompilationUnit()))
        {
            String importName = importNode.getImportName();
            if (importNode.isWildcardImport())
                addProblem(new UnknownWildcardImportProblem(site, importName));
            else
                addProblem(new UnknownImportProblem(site, importName));
        }
        
        // Check if a deprecated definition is being imported.
        if (!importNode.isWildcardImport())
        {
            IDefinition importedDefinition = importNode.resolveImport(project);
            checkDeprecated(site, importedDefinition);
        }
    }

    /**
     *  Check a use namespace directive.
     *  @param iNode - the use namespace node.
     *  @param ns_name - the namespace's Binding.
     */
    public void checkUseNamespaceDirective(IASNode iNode, Binding ns_name)
    {
        if ( ns_name.getDefinition() == null && ns_name.getName() != null )
        {
            // For error reporting, let's get the fully qualified name of the namespace, if possible
            String fullNamespaceName=null;
            IASNode n = ns_name.getNode();
            if (n instanceof IIdentifierNode)
            {
                // In cases I've seen we go through this path
                fullNamespaceName = ((IIdentifierNode)n).getName();
            }
            else
            {
                // This is purely defensive programming - if for some reason we can't get the full name,
                // go back to this older code that gets the short name.
                fullNamespaceName = ns_name.getName().getBaseName();
            }
           
            addProblem(
                new UnknownNamespaceProblem (
                    roundUpUsualSuspects(ns_name, iNode),
                    fullNamespaceName
                )
            );
        }
        
        checkReference(ns_name);
    }

    /**
     *  Check a unary operator.
     *  @param iNode - the operator's i-node.
     *  @param opcode - the corresponding opcode.
     */
    public void checkUnaryOperator(IASNode iNode, int opcode)
    {
        switch(opcode)
        {
            case ABCConstants.OP_negate:
            case ABCConstants.OP_convert_d:
            case ABCConstants.OP_bitnot:
                checkImplicitConversion(((IUnaryOperatorNode)iNode).getOperandNode(), utils.numberType(), null);
                break;
        }
    }

    /**
     *  @return true if the i-node is in a class definition.
     */
    private boolean isInClassDefinition(IASNode iNode)
    {
        return iNode.getAncestorOfType(ClassNode.class) != null;
    }

    /**
     *  @return true if the i-node is in a package definition.
     */
    private boolean isInPackageDefinition(IASNode iNode)
    {
        return iNode.getAncestorOfType(PackageNode.class) != null;
    }

    /**
     * Ensure that a definition does not have the same modifier more than once
     */
    public void checkForDuplicateModifiers(BaseDefinitionNode bdn)
    {
        ModifiersContainerNode mcn = bdn.getModifiersContainer();
        ModifiersSet modifierSet = bdn.getModifiers();
        if( mcn != null && modifierSet != null )
        {
            int modifierNodeCount = mcn.getChildCount();
            if( modifierNodeCount > modifierSet.getAllModifiers().length )
            {
                ModifiersSet tempSet = new ModifiersSet();
                // More children than modifiers - must have dups
                for( int i = 0; i < modifierNodeCount; ++i )
                {
                    IASNode node = mcn.getChild(i);
                    if( node instanceof ModifierNode )
                    {
                        ModifierNode modNode = (ModifierNode)node;
                        if( tempSet.hasModifier(modNode.getModifier()) )
                        {
                            currentScope.addProblem(new DuplicateAttributeProblem(modNode, modNode.getModifierString()));
                        }
                        tempSet.addModifier(modNode);
                    }
                }
            }
        }
    }
    
    /**
     * Looks for namespace prefixes on definitions inside of functions. 
     * Example:
     *  <code><pre>
     *  function foo() : void {
     *      private var bar:int;// this will give "Access modifier not allowed..."
     *      ns var baz:int;     // this will give "Namespace override not allowed ..."
     *  }
     *  </pre></code>
     *  
     *  This function must only be called for nodes that are in fact withing a function.
     */
    private void checkForNamespaceInFunction(BaseDefinitionNode node, LexicalScope scope)
    {
        assert SemanticUtils.isInFunction(node);
        DefinitionBase def = node.getDefinition();
        INamespaceReference nsRef = def.getNamespaceReference();
        if (!(nsRef instanceof NamespaceDefinition.IInternalNamespaceDefinition) &&
                !(nsRef instanceof NamespaceDefinition.IFilePrivateNamespaceDefinition))
        {
            IASNode site = node.getNamespaceNode();
            if (site == null)
                site = node;
            boolean isAccessor = nsRef instanceof NamespaceDefinition.ILanguageNamespaceDefinition;
           
            currentScope.addProblem(new NamespaceOverrideInsideFunctionProblem(site, isAccessor));
        }
    }

    /**
     *  Check a variable declaration.
     */
    public void checkVariableDeclaration(IASNode iNode)
    {
        VariableNode var = (VariableNode)iNode;
        ModifiersSet modifiersSet = var.getModifiers();

        boolean isInFunction = SemanticUtils.isInFunction(iNode);
         
        if (isInFunction)
        {
            checkForNamespaceInFunction(var, currentScope);
        }
        
        //  Variable decls inside methods can't have attributes other than namespaces.
        if ( isInFunction && modifiersSet != null)
        {
            IASNode site = var.getNameExpressionNode();
            for ( ASModifier modifier : modifiersSet.getAllModifiers() )
            {
                if( modifier == ASModifier.NATIVE )
                {
                    currentScope.addProblem(new NativeVariableProblem(site));
                }
                else if (modifier == ASModifier.DYNAMIC )
                {
                    currentScope.addProblem(new DynamicNotOnClassProblem(site));
                }
                else if( modifier == ASModifier.FINAL )
                {
                    currentScope.addProblem(new FinalOutsideClassProblem(site));
                }
                else if( modifier == ASModifier.OVERRIDE )
                {
                    currentScope.addProblem(new InvalidOverrideProblem(site));
                }
                else if( modifier == ASModifier.VIRTUAL )
                {
                    currentScope.addProblem(new VirtualOutsideClassProblem(site));
                }
                else if( modifier == ASModifier.STATIC )
                {
                    currentScope.addProblem(new StaticOutsideClassProblem(site));
                }
                else if( modifier == ASModifier.ABSTRACT )
                {
                    if(currentScope.getProject().getAllowAbstractClasses())
                    {
                        currentScope.addProblem(new AbstractOutsideClassProblem(site));
                    }
                }
            }
        }

        //  Check for ambiguity.
        IDefinition def = utils.getDefinition(var);
        checkVariableForConflictingDefinitions(iNode, (IVariableDefinition)def);

        checkNamespaceOfDefinition(var, def, project);
        
        /////////////////////////////////////
        // Check for a type on the variable declaration
        String type = var.getTypeName();
       
        if (type.isEmpty())     // empty string means no declaration at all (not *)
        {
            // don't check things that didn't come from source. They tend to give false negatives
            if (var.getStart() != var.getEnd())
            {
                // get a node that has the best display location for problem
                IASNode location = var;
                IExpressionNode nameExpression = var.getNameExpressionNode();
                if (nameExpression != null)
                    location = nameExpression;
                this.currentScope.addProblem( new VariableHasNoTypeDeclarationProblem(location, var.getShortName()));
            }
        }

        ExpressionNodeBase typeNode = var.getTypeNode();
        IExpressionNode currentTypeNode = typeNode;
        IDefinition vectorType = project.getBuiltinType(IASLanguageConstants.BuiltinType.VECTOR);
        while (currentTypeNode instanceof ITypedExpressionNode)
        {
            ITypedExpressionNode typedNode = (ITypedExpressionNode) currentTypeNode;
            IExpressionNode collectionNode = typedNode.getCollectionNode();
            IDefinition collectionType = collectionNode.resolve(project);
            if (!vectorType.equals(collectionType))
            {
                currentScope.addProblem(new TypeParametersWithNonParameterizedTypeProblem(collectionNode));
            }
            //keep checking while there are nested types
            currentTypeNode = typedNode.getTypeNode();
        }

        // check if thise is an assignment in this declaration.
        // If so, do checks on that
        final ExpressionNodeBase rightNode = var.getAssignedValueNode();
        if( var.isConst() && rightNode == null )
        {
            addProblem(new ConstNotInitializedProblem(var, var.getName()));
        }

        // if there is an initializer, check that the value is reasonable 
        if (rightNode != null)
        {
            checkAssignmentValue(def, rightNode);
        }

        if(def instanceof VariableDefinition)
        {
            if(SemanticUtils.isNestedClassProperty(iNode, (VariableDefinition)def))
            {
                // TODO: Issue a better, mor specific diagnostic
                // TODO: once we are allowed to add new error strings.
                addProblem(new BURMDiagnosticNotAllowedHereProblem(iNode));
            }
        }
    }

    /**
     *  Verify that a named type exists.
     *  @param typename - the name of the type.
     */
    public void checkTypeName(Binding typename)
    {
        if ( typename.getNode() == null || typename.getName() == null )
        {
            //  Some kind of synthetic Binding, ignore.
        }
        else
        {
            IDefinition typeDef = utils.getDefinition(typename.getNode());
            if ( !SemanticUtils.isType(typeDef) )
            {
                Name name = typename.getName();
                while ( name != null && name.isTypeName() && name.getTypeNameParameter() != null )
                    name = name.getTypeNameParameter();

                if ( name != null )
                {
                    if ( !name.isTypeName() )
                    {
                        addTypeProblem(typename.getNode(), typeDef, name.getBaseName(), false);
                    }
                    else
                    {
                        //  Render as best able.
                        addTypeProblem(typename.getNode(), typeDef, name.toString(), false);
                    }
                }
            }
        }
        
        checkReference(typename);
    }

    /**
     *  Check a class field declaration.
     */
    public void checkClassField(VariableNode var)
    {
        checkVariableDeclaration(var);

        //  Check the variable's type.
        IASNode typeNode = var.getTypeNode();

        IDefinition typeDef = utils.getDefinition(typeNode);
        if ( !SemanticUtils.isType(typeDef))
        {
            IASNode problematicType = utils.getPotentiallyParameterizedType(typeNode);

            String typeDesc;

            if ( problematicType instanceof IdentifierNode )
            {
                typeDesc = ((IdentifierNode)problematicType).getName();
            }
            else
            {
                typeDesc = var.getTypeName();
            }

            addTypeProblem(problematicType, typeDef, typeDesc, true);
        }
    }

    /**
     * Add the appropriate Problem when a type annotation did not resolve to a Type
     * @param typeNode  the Node to use for location info for the problem
     * @param typeDef   The IDefinition the type annotation resolved to
     * @param typeDesc  A String to use as the description of the type annotation in the diagnostic
     * @param reportAmbiguousReference A flag indicating whether an AmbiguousReferenceProblem
     * should be reported, if the <code>typeDef</code> is ambiguous.
     */
    public void addTypeProblem (IASNode typeNode, IDefinition typeDef, String typeDesc, boolean reportAmbiguousReference)
    {
        if( AmbiguousDefinition.isAmbiguous(typeDef) )
        {
            if (reportAmbiguousReference)
                addProblem(new AmbiguousReferenceProblem(typeNode, typeDesc));
        }
        else
        {
            addProblem(new UnknownTypeProblem(typeNode, typeDesc));
        }
    }

    /**
     * Check the qualifier of a qualified name ('a' in 'a::foo')
     * @param iNode     the node representing the qualifier
     */
    public void checkQualifier(IASNode iNode)
    {
        IDefinition qualifier = utils.getDefinition(iNode);
        
        // If a qualifier is used in a context hwere it is not valid, then the qualifier will resolve
        // to the CM Implicit namespace.
        // This happens for code like:  private::foo
        // when the code is outside of a class (so there is no private).
        if( qualifier == NamespaceDefinition.getCodeModelImplicitDefinitionNamespace() )
        {
            INamespaceDecorationNode nsNode = (INamespaceDecorationNode)iNode;
            String nsString = nsNode.getName();
            if( nsString == IASKeywordConstants.PUBLIC )
            {
                addProblem(new InvalidPublicNamespaceProblem(nsNode) );
            }
            else if( nsString == IASKeywordConstants.PROTECTED )
            {
                addProblem(new InvalidProtectedNamespaceProblem(nsNode) );
            }
            else if( nsString == IASKeywordConstants.PRIVATE )
            {
                addProblem(new InvalidPrivateNamespaceProblem(nsNode));
            }
        }
        
        checkDeprecated(iNode, qualifier);
    }

    /**
     *  Check a namespace declaration.
     */
    public void checkNamespaceDeclaration(IASNode iNode, Binding ns_name)
    {
        NamespaceNode nsNode = (NamespaceNode)iNode;

        IDefinition def = utils.getDefinition(nsNode);

        checkNamespaceOfDefinition(nsNode, def, project);
        SemanticUtils.checkScopedToDefaultNamespaceProblem(currentScope, nsNode, def, null);
        
        if (SemanticUtils.isInFunction(iNode))
        {
            if (iNode instanceof BaseDefinitionNode)
                checkForNamespaceInFunction((BaseDefinitionNode)iNode, currentScope);
        }
        
        // Check whether the namespace is being initialized to a deprecated namespace.
        IExpressionNode namespaceInitialValueNode = nsNode.getNamespaceURINode();
        if (namespaceInitialValueNode != null)
        {
            IDefinition namespaceInitialvalueDefinition = namespaceInitialValueNode.resolve(project);
            checkDeprecated(namespaceInitialValueNode, namespaceInitialvalueDefinition);
        }
    }

    /**
     * Check that the namespace of the definition is valid
     */
    public void checkNamespaceOfDefinition(IASNode iNode, IDefinition def, ICompilerProject project)
    {
        INamespaceReference nsRef = def.getNamespaceReference();
        
        // If it's not a language namespace, then it is only valid if the def is declared in a class
        if( !nsRef.isLanguageNamespace() && !(def.getParent() instanceof IClassDefinition) )
        {
            // if in function, we will already generate the error that no overrides at all are allowed
            if (!SemanticUtils.isInFunction(iNode))
                addProblem(new InvalidNamespaceProblem(iNode instanceof BaseDefinitionNode ?
                                                    ((BaseDefinitionNode)iNode).getNamespaceNode() :
                                                    iNode));
        }

        if( nsRef == NamespaceDefinition.getCodeModelImplicitDefinitionNamespace()
                // constructors are left in the CMImplicit namespace so that FB continues to work right
                && !isConstructor(def) )
        {
            // This should only happen if an invalid access namespace was specified
            // e.g. private outside of a class
            BaseDefinitionNode bdn = iNode instanceof BaseDefinitionNode ? (BaseDefinitionNode)iNode : null;
            if( bdn != null )
            {
                INamespaceDecorationNode nsNode = bdn.getNamespaceNode();
                if( nsNode != null )
                {
                    String nsString = nsNode.getName();
                    if( nsString == IASKeywordConstants.PUBLIC )
                    {
                        addProblem(new InvalidPublicNamespaceAttrProblem(nsNode) );
                    }
                    else if( nsString == IASKeywordConstants.PROTECTED )
                    {
                        addProblem(new InvalidProtectedNamespaceAttrProblem(nsNode) );
                    }
                    else if( nsString == IASKeywordConstants.PRIVATE )
                    {
                        addProblem(new InvalidPrivateNamespaceAttrProblem(nsNode));
                    }
                }
            }
        }
        
        IASNode nsNode = iNode instanceof BaseDefinitionNode ?
                ((BaseDefinitionNode)iNode).getNamespaceNode() :
                iNode;

        INamespaceDefinition nsDef = nsRef.resolveNamespaceReference(project);
        
        if (nsRef.resolveNamespaceReference(project) == null)
            addProblem(new UnresolvedNamespaceProblem(nsNode));
        
        checkDeprecated(nsNode, nsDef);
    }

    private boolean isConstructor(IDefinition d)
    {
        if( d instanceof IFunctionDefinition && ((IFunctionDefinition)d).isConstructor() )
            return true;
        return false;
    }
    /**
     *  Check a Vector literal.
     */
    public void checkVectorLiteral(IASNode iNode, Binding type_param)
    {
        IDefinition type_def = type_param.getDefinition();
        IContainerNode contentsNode = ((VectorLiteralNode)iNode).getContentsNode();
        if ( type_def != null )
        {
            boolean isNumericTypeOrBoolean = SemanticUtils.isNumericTypeOrBoolean(type_def, project);
            String typeName = type_def.getBaseName();
            boolean isInt = SemanticUtils.isBuiltin(type_def, BuiltinType.INT, project);
            boolean isUint = SemanticUtils.isBuiltin(type_def, BuiltinType.UINT, project);

            for ( int i = 0; i < contentsNode.getChildCount(); i++ )
            {
                IASNode literal_element = contentsNode.getChild(i);

                if (isNumericTypeOrBoolean)
                {
                    // check for loss of precision
                    if ( literal_element instanceof NumericLiteralNode )
                    {
                        INumericLiteralNode.INumericValue numeric = ((NumericLiteralNode)literal_element).getNumericValue();
                        if ( (isInt && ( numeric.toNumber() != numeric.toInt32())) ||
                             (isUint && ( numeric.toNumber() != numeric.toUint32()))  )
                        {
                            addProblem(new LossyConversionProblem(literal_element, typeName));
                        }
                    }

                    // check for null values where they don't make sense
                    if (literal_element instanceof IExpressionNode)
                    {
                        IDefinition elementType = ((IExpressionNode)literal_element).resolveType(project);
                        boolean elementIsNull = SemanticUtils.isBuiltin(elementType, BuiltinType.NULL, project);
                        if (elementIsNull)
                        {
                            addProblem( new NullUsedWhereOtherExpectedProblem(literal_element, typeName));
                        }
                    }
                }
                checkImplicitConversion(literal_element, type_def, null);
            }
        }
        return;
    }

    /**
     *  Find at least some AS3 context for a diagnostic.
     *  @return the i-node from the problematic Binding, or the nearest
     *    known i-node if the Binding has no i-node (i.e., it's synthetic).
     */
    private IASNode roundUpUsualSuspects(Binding offender, IASNode bystander)
    {
        if (offender.getNode() != null)
            return offender.getNode();
        else
            return bystander;

    }

    /**
     *  Check a rest parameter declaration (...rest)
     *  @param iNode - the ParameterNode for the rest decl.
     *  @param param_type - the type of the rest param
     */
    public void checkRestParameter(IASNode iNode, Binding param_type)
    {
        IDefinition type = param_type.getDefinition();
        if( !utils.isBuiltin(type, BuiltinType.ARRAY) && !utils.isBuiltin(type, BuiltinType.ANY_TYPE) )
            addProblem(new InvalidRestParameterDeclarationProblem(((ParameterNode)iNode).getTypeNode()));
    }


    /**
     * Check for other definitions that might conflict with the function passed in.  This method will issue
     * a diagnostic if there are any definitions with the same name in the same declaring scope.  It will
     * not find conflicts in base classes - if a method conflicts in something in a base class some sort of
     * illegal override error will already have been issued.
     *
     * @param iNode     The Node that produced the function definition.  Used for location info for any diagnostics.
     * @param funcDef   The FunctionDefinition of the function to check
     */
    public boolean checkFunctionForConflictingDefinitions (IASNode iNode, FunctionDefinition funcDef)
    {
        boolean foundConflict = false;
        // Only have to check for dups in the current class if this is a class method
        // If a base class has a conflict some sort of illegal override error will have been generated already
        // if this is a global or nested function we don't have to check the base class as there won't be one
        switch( SemanticUtils.getMultiDefinitionType(funcDef, project))
        {
            case AMBIGUOUS:
                String namespaceName = getNamespaceStringFromDef(funcDef);
                addProblem(new ConflictingNameInNamespaceProblem(iNode, funcDef.getBaseName(), namespaceName));
                foundConflict = true;
                break;
            case NONE:
                break;
            case MULTIPLE:
                addProblem(new DuplicateFunctionDefinitionProblem(iNode, funcDef.getBaseName()));
                break;
            default:;
              assert false;     
        }
        
        // getter or setter with same name as a package gets a warning/
        // we don't return true, however, as it isn't "really" a multiple definition
        if (funcDef instanceof IAccessorDefinition)
        {
            ASScope cs = (ASScope)funcDef.getContainingScope();
            if (cs.isPackageName(funcDef.getBaseName()))
            {
                IFunctionNode funcNode = (IFunctionNode)iNode;
                IASNode funcNameNode = funcNode.getNameExpressionNode();
                addProblem(new DefinitionShadowedByPackageNameProblem(funcNameNode));
            }
        }
        return foundConflict;
    }

    /**
     * Check for other definitions that might conflict with the function passed in.  This method is for interface
     * functions, so it will check for dups in the current interface, and also look for any conflicts in base interfaces.
     *
     * @param iNode     The node that produced the function definition.  Used for location info for any diagnostics.
     * @param funcDef   The FunctionDefinition of the interface function to check
     */
    public void checkInterfaceFunctionForConflictingDefinitions (IASNode iNode, FunctionDefinition funcDef)
    {
        // Look for conflicts in this interface
        checkFunctionForConflictingDefinitions(iNode, funcDef);

        // Look for methods from base interfaces we may be overriding.
        List<IFunctionDefinition> conflicts = funcDef.resolveOverridenInterfaceFunctions(project);
        if( conflicts.size() > 0 )
        {
            for( IFunctionDefinition overriden : conflicts )
            {
            	if ((overriden instanceof SetterDefinition &&
            		funcDef instanceof GetterDefinition) ||
            		(overriden instanceof GetterDefinition &&
            		funcDef instanceof SetterDefinition))
            		continue;
                addProblem(new InterfaceMethodOverrideProblem(iNode, funcDef.getBaseName(), overriden.getParent().getBaseName()));
            }
        }

    }

    /**
     * Check for other definitions that might conflict with the variable passed in.  This method will check for conflicts
     * in the declaring scope of the variable, and if it is declared in a class it will check for conflicts in the base classes.
     *
     * @param iNode     The node that produced the variable definition.  Used for location info for any diagnostics.
     * @param varDef   The VariableDefinition of the variable to check
     */
    public void checkVariableForConflictingDefinitions( IASNode iNode, IVariableDefinition varDef )
    {
        MultiDefinitionType ambiguity = SemanticUtils.getMultiDefinitionType(varDef, project);
        if (ambiguity != MultiDefinitionType.NONE)
        {
            final String varName = varDef.getBaseName();
            ICompilerProblem problem = null;
            if (ambiguity == MultiDefinitionType.AMBIGUOUS)
            {
                problem =  new ConflictingNameInNamespaceProblem(iNode, varName, getNamespaceStringFromDef(varDef));
            }
            else 
            {
                IVariableNode varNode = (IVariableNode)iNode;
                IASNode varNameNode = varNode.getNameExpressionNode();
                if (ambiguity == MultiDefinitionType.MULTIPLE)
                    problem = new DuplicateVariableDefinitionProblem(varNameNode, varName);
                else if (ambiguity == MultiDefinitionType.SHADOWS_PARAM)
                    problem = new VariableDefinitionDuplicatesParameterProblem(varNameNode, varName);
            }
            
            assert problem != null;
            if (problem != null)
                addProblem(problem);
        }

        if (!varDef.isStatic() && SemanticUtils.hasBaseClassDefinition(iNode, project))
        {
            addProblem(new ConflictingInheritedNameInNamespaceProblem(iNode, varDef.getBaseName(), getNamespaceStringFromDef(varDef) ));
        }
            
        // Now look to see if the variable name is also a package name
        ASScope cs = (ASScope)varDef.getContainingScope();
        if (cs.isPackageName(varDef.getBaseName()))
        {
            IVariableNode varNode = (IVariableNode)iNode;
            IASNode varNameNode = varNode.getNameExpressionNode();
            addProblem(new DefinitionShadowedByPackageNameProblem(varNameNode));
        }
    }

    private String getNamespaceStringFromDef (IDefinition funcDef)
    {
        String namespaceName = null;
        INamespaceDefinition n = funcDef.resolveNamespace(project);
        if (n != null)
        {
            namespaceName = n.getBaseName();
        }
        return namespaceName;
    }

    /**
     *  Signal the semantic checker that its caller is entering a constructor:
     *  initialize constructor-specific state variables.
     */
    public void enterConstructor()
    {
        assert this.superState == SuperState.Invalid: String.format("Unexpected super() tracking state %s", this.superState);
        this.superState = SuperState.Initial;
    }

    /**
     *  Signal the semantic checker that its caller is leaving a constructor:
     *  reset constructor-specific state variables.
     */
    public void leaveConstructor()
    {
        assert this.superState != SuperState.Invalid: String.format("Unexpected super() tracking state %s", this.superState);
        this.superState = SuperState.Invalid;
    }

    /**
     * Test whether the getter for the specified accessor can be inlined
     * 
     * @param accessor The accessor to test whether the getter can be inlined
     * @return true if the getter can be inlined
     */
    public boolean canGetterBeInlined(AccessorDefinition accessor)
    {
        if (accessor instanceof SetterDefinition)
            accessor = accessor.resolveCorrespondingAccessor(currentScope.getProject());

        if (accessor == null)
            return false;

        return canFunctionBeInlined(accessor);
    }

    /**
     * Test whether the setter for the specified accessor can be inlined
     * 
     * @param accessor The accessor to test whether the setter can be inlined
     * @return true if the setter can be inlined
     */
    public boolean canSetterBeInlined(AccessorDefinition accessor)
    {
        if (accessor instanceof GetterDefinition)
            accessor = accessor.resolveCorrespondingAccessor(currentScope.getProject());

        if (accessor == null)
            return false;

        return canFunctionBeInlined(accessor);
    }

    /**
     * Test whether the specified function can be inlined.  If the function
     * can't be inlined, a problem will be if the user explicity requested
     * the function be inlined, rather than the compiler trying to
     * inline it optimistically.
     * 
     * @param function The function to test whether it can be inlined
     * @return true if the function can be inlined
     */
    public boolean canFunctionBeInlined(FunctionDefinition function)
    {
        if (!currentScope.getProject().isInliningEnabled())
            return false;

        // only report a problem when a function can't be inlined if the
        // function has been explicitly marked as inline
        final boolean reportInlineProblems = function.isInline();

        // don't support inlining functions inside inlined functions
        if (currentScope.insideInlineFunction())
        {
            if (reportInlineProblems)
                currentScope.addProblem(new InlineNestedInliningNotSupportedProblem(function.getBaseName()));

            return false;
        }

        // can't inline the function if we don't have a node for it
        FunctionNode functionNode = (FunctionNode)function.getFunctionNode();
        if (functionNode == null)
        {
            if (reportInlineProblems)
                currentScope.addProblem(new InlineNoSourceProblem(function.getBaseName()));

            return false;
        }

        if (!function.inlineFunction())
        {
            if (reportInlineProblems)
                currentScope.addProblem(new InlineFunctionNotFinalStaticOrGlobalProblem(functionNode, function.getBaseName()));

            return false;
        }

        // Pass in a new collection for the compiler problems, as we don't care
        // about any problems parsing the body, as they will be reported when
        // parsing the non-inlined version of the function.
        functionNode.parseFunctionBody(new ArrayList<ICompilerProblem>());

        // If we meet all the requirements for an inlined method, parse and scan the
        // body to make sure there isn't any constructs we can't inline.
        final ScopedBlockNode functionBody = functionNode.getScopedNode();
        if (functionBodyHasNonInlineableNodes(functionBody, reportInlineProblems, function.getBaseName(), new AtomicInteger()))
        {
            functionNode.discardFunctionBody();
            return false;
        }

        return true;
    }

    /**
     * Check for any constructs we know we can't inline, or if the function is too large
     * 
     * @param n the node to test
     * @param reportInlineProblems whether or not to report inline problems
     * @param functionName the name of the function being inlined
     * @param exprCount a running count of the expressions in the body
     * @return true if the function contains un-inlineable nodes, false if
     * the function body can be inlined
     */
    public boolean functionBodyHasNonInlineableNodes(IASNode n, boolean reportInlineProblems, String functionName, AtomicInteger exprCount)
    {
        if (n == null)
            return false;

        if (n instanceof ExpressionNodeBase)
        {
            exprCount.getAndIncrement();

            if (exprCount.get() > InlineFunctionLexicalScope.MAX_EXPR_IN_BODY)
            {
                if (reportInlineProblems)
                    currentScope.addProblem(new InlineFunctionTooLargeProblem(functionName, exprCount.get(), InlineFunctionLexicalScope.MAX_EXPR_IN_BODY));

                return true;
            }
        }

        switch (n.getNodeID())
        {
            case AnonymousFunctionID:
            case CatchID:
            case FinallyID:
            case FunctionID:
            case FunctionObjectID:
            case TryID:
            case WithID:
            {
                if (reportInlineProblems)
                    currentScope.addProblem(new InlineUnsupportedNodeProblem(n, functionName));

                return true;
            }
            default:
            {
                for (int i = 0; i < n.getChildCount(); i++)
                {
                    if (functionBodyHasNonInlineableNodes(n.getChild(i), reportInlineProblems, functionName, exprCount))
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * Iterate through the Instructions which are to be inlined and
     * ensure the can all be inlined.  Any Instruction which makes
     * use of the scope chain will cause the inline to fail.
     * 
     * @param insns inlined instructions
     * @param reportInlineProblems whether or not to report inline problems
     * @param functionName the name of the function being inlined
     * @return true if all instructions are OK to be inlined
     */
    public boolean functionBodyHasNonInlineableInstructions(InstructionList insns, boolean reportInlineProblems, String functionName)
    {
        // if there are no instructions in the function body, then something went wrong (bad AS)
        // doing codegen, so we can't inline this function
        if (insns.isEmpty())
            return true;

        for (Instruction insn : insns.getInstructions())
        {
            switch (insn.getOpcode())
            {
                case OP_pushwith:
                case OP_popscope:
                case OP_pushscope:
                case OP_newfunction:
                case OP_returnvoid:
                case OP_returnvalue:
                case OP_newactivation:
                case OP_newclass:
                case OP_newcatch:
                case OP_findpropstrict:
                case OP_findproperty:
                case OP_getlex:
                case OP_getscopeobject:
                case OP_getouterscope:
                {
                    if (reportInlineProblems)
                        currentScope.addProblem(new InlineUnsupportedInstructionProblem(functionName));

                    return true;
                }
            }
        }

        return false;
    }
}

