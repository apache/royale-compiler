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

package org.apache.royale.compiler.internal.as.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import java.util.Stack;

import static org.apache.royale.abc.ABCConstants.*;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.ECMASupport;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.InstructionFactory;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.NoOperandsInstruction;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceResolvedReference;
import org.apache.royale.compiler.exceptions.CodegenInterruptedException;
import org.apache.royale.compiler.exceptions.DuplicateLabelException;
import org.apache.royale.compiler.exceptions.UnknownControlFlowTargetException;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.BaseVariableNode;
import org.apache.royale.compiler.internal.tree.as.VariableExpressionNode;
import org.apache.royale.compiler.problems.AmbiguousGotoTargetProblem;
import org.apache.royale.compiler.problems.AnyNamespaceCannotBeQualifierProblem;
import org.apache.royale.compiler.problems.CallNonFunctionProblem;
import org.apache.royale.compiler.problems.CodegenInternalProblem;
import org.apache.royale.compiler.problems.DuplicateLabelProblem;
import org.apache.royale.compiler.problems.DuplicateNamespaceDefinitionProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InvalidLvalueProblem;
import org.apache.royale.compiler.problems.InvalidNamespaceInitializerProblem;
import org.apache.royale.compiler.problems.MultipleSwitchDefaultsProblem;
import org.apache.royale.compiler.problems.NonConstantParamInitializerProblem;
import org.apache.royale.compiler.problems.PackageCannotBeUsedAsValueProblem;
import org.apache.royale.compiler.problems.UnexpectedReturnProblem;
import org.apache.royale.compiler.problems.UnknownGotoTargetProblem;
import org.apache.royale.compiler.problems.UnknownNamespaceProblem;
import org.apache.royale.compiler.problems.UnknownBreakTargetProblem;
import org.apache.royale.compiler.problems.UnknownContinueTargetProblem;
import org.apache.royale.compiler.problems.VoidTypeProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.ICatchNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.ITypedExpressionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IWhileLoopNode;
import org.apache.royale.compiler.tree.as.IWithNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.royale.compiler.internal.as.codegen.LexicalScope.NestingState;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.GetterDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.SetterDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorInNode;
import org.apache.royale.compiler.internal.tree.as.DynamicAccessNode;
import org.apache.royale.compiler.internal.tree.as.EmbedNode;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.ForLoopNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.FunctionObjectNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceNode;
import org.apache.royale.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.royale.compiler.internal.tree.as.SwitchNode;
import org.apache.royale.compiler.internal.tree.as.TernaryOperatorNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;

/*
 * Notes on changing the reducer:
 * 
 * - Do not add OP_finddefinition, OP_finddefstrict or OP_getlex instructions directly.  Instead use
 *   LexicalScope.findProperty() and LexicalScope.getPropertyValue(), as these methods will
 *   ensure the correct instructions are added when inlining a function body where the scope
 *   chain can not be depended on.
 */
public class ABCGeneratingReducer
{
    /**
     *  A struct for the decoded pieces of a catch block.
     */
    public static class CatchPrototype
    {
        Name catchType;
        Name catchVarName;
        InstructionList catchBody;
    }

    /**
     *  ConditionalFragment holds the disparate elements of a conditional statement fragment.
     */
    public class ConditionalFragment
    {
        IASNode site;
        /**
         * condition is non-null when the conditional expression is not a constant.
         * condition is null if an unconditional fragement, or if the fragement is
         * a constant, in which case constantCondition is non-null.
         */
        InstructionList condition;
        /**
         * constantCondition is non-null when the conditional expression is a constant.
         * constantCondition is null if an unconditional fragement, or if the fragement is
         * a non-constant, in which case condition is non-null.
         */
        Object constantCondition;
        InstructionList statement;

        private Label statementLabel = null;

        /**
         *  Construct a ConditionalFragment with a single statement.
         */
        ConditionalFragment( IASNode site, InstructionList condition, InstructionList statement)
        {
            this.site = site;
            this.condition = condition;
            this.constantCondition = null;
            this.statement = statement;
        }

        /**
         *  Construct a ConditionalFragment with a list of statements,
         *  coalescing them into a composite statement.
         */
        ConditionalFragment( IASNode site, InstructionList condition, Vector<InstructionList> statements)
        {
            this(site, condition, null, statements);
        }

        /**
         * Construct a constant conditional ConditionalFragment with a list of
         * statements, coalescing them into a composite statement.
         */
        ConditionalFragment(IASNode site, Object constantConditional, Vector<InstructionList> statements)
        {
            this(site, null, constantConditional, statements);
        }

        /**
         * Private constructor called by public constructors
         * @param site
         * @param condition
         * @param constantCondition
         * @param statements
         */
        private ConditionalFragment(IASNode site, InstructionList condition, Object constantCondition, Vector<InstructionList> statements)
        {
            assert (condition == null || constantCondition == null) : "condition and constantCondition can't both be non-null";
            this.site = site;
            this.condition = condition;
            this.constantCondition = constantCondition;
            this.statement = createInstructionList(site);

            for (InstructionList stmt : statements)
            {
                this.statement.addAll(stmt);
            }
        }

        /**
         *  @return the label of the fragment's condition, or
         *    its statement label if it's the default alternative.
         */
        Label getLabel()
        {
            if ( !this.isUnconditionalAlternative() )
            {
                if ( this.condition != null )
                    return this.condition.getLabel();
                else
                    return getStatementLabel();
            }
            else
            {
                return getStatementLabel();
            }
        }

        /**
         *  Cache and return the label of this conditional fragment's
         *  statement so it can be retrieved after the statement's been
         *  added to the overall conditional statement and this.statement
         *  is invalidated.
         */
        Label getStatementLabel()
        {
            if ( null == this.statementLabel )
                this.statementLabel = this.statement.getLabel();

            return this.statementLabel;
        }

        /**
         *  @return true if this is an unconditional alternative,
         *    i.e. a default case or the last else in an if/else if/else.
         */
        boolean isUnconditionalAlternative()
        {
            return (null == this.condition) && (null == this.constantCondition);
        }

        void convertConstantConditionToInstruction()
        {
            if ((constantCondition == null && condition != null)  || condition == null)
                return;

            condition = transform_constant_value(site, constantCondition);
            constantCondition = null;
        }

        /**
         * @return true if this is a constant conditonal fragement i.e. case 7:
         */
        boolean isConstantConditional()
        {
            return this.constantCondition != null;
        }
    }

    /**
     *  The RuntimeMultiname class holds the components of 
     *  the various types of runtime multiname, and puts them
     *  together in the right order to generate the names and
     *  instruction sequences to jamble up the AVM.
     *
     *  Only some permutations of the compile-time/runtime
     *  qualifier::name possibilities are valid:
     *  Name::expression generates MultinameL
     *  expression::expression generates RTQnameL
     *  expression::name generates RTQname
     *  (name::name is a plain Multiname)
     */
    class RuntimeMultiname
    {
        /**
         *  The compile-time qualifier of a MultinameL.
         *  compileTimeName isn't valid (this would
         *  be a compile-time constant Multiname).
         */
        Name compileTimeQualifier;

        /**
         *  The runtime qualifier of a RTQname or RTQNameL.
         *  Compile-time and runtime names are valid.
         */
        InstructionList runtimeQualifier;

        /**
         *  The compile-time constant name of a RTQName.
         *  compileTimeQualifier isn't valid (this would
         *  be a compile-time constant Multiname).
         */
        Name compileTimeName;

        /**
         *  The runtime name of a MultinameL or RTQnameL.
         */
        InstructionList runtimeName;

        /**
         *  Construct a RTQnameL-type runtime name.
         *  @param qualifier - the runtime qualifier.
         *  @param name - the runtime name.
         */
        RuntimeMultiname(InstructionList qualifier, InstructionList runtime_name)
        {
            this.runtimeQualifier = qualifier;
            this.runtimeName  = runtime_name;
        }

        /**
         *  Construct a RTQname-type runtime name.
         *  @param qualifier - the runtime qualifier.
         *  @param name - the compile-time name.
         */
        RuntimeMultiname(InstructionList qualifier, Name name)
        {
            this.runtimeQualifier = qualifier;
            this.compileTimeName  = name;
        }

        /**
         *  Construct a MultinameL-type runtime name.
         *  @param qualifier - the Multiname.  Note this
         *    is the only kind of runtime name that receives
         *    a qualifying name.
         *  @param nameL - the runtime expression that 
         *    yields the base name.
         */
        RuntimeMultiname(Name qualifier, InstructionList nameL)
        {
            assert(qualifier.getKind() == CONSTANT_MultinameL || qualifier.getKind() == CONSTANT_MultinameLA): "Bad qualifier kind " + qualifier.getKind() + " on name " + qualifier.toString();
            this.compileTimeQualifier = qualifier;
            this.runtimeName = nameL;
        }

        /**
         *  Assemble the ingredients into an expression.
         *  @param opcode - one of OP_getproperty or OP_setproperty.
         *  @param rhs - the value of a setproperty call.  Passed to
         *    this routine because there may be name-specific 
         *    instructions before and after the value.
         */
        InstructionList generateGetOrSet(IASNode iNode, int opcode, InstructionList rhs)
        {
            InstructionList result = createInstructionList(iNode);

            //  Note: numerous microoptimization opportunities here
            //  to avoid storing values in temps by creative use of
            //  OP_dup and OP_swap, especially in getproperty gen.

            if ( this.compileTimeQualifier != null && this.runtimeName != null )
            {
                //  Generate MultinameL type code.
                Binding name_temp = currentScope.allocateTemp();

                result.addAll(this.runtimeName);
                result.addInstruction(OP_dup);
                result.addInstruction(name_temp.setlocal());
                //  findprop(MultinameL) consumes a name from the value stack.
                result.addInstruction(OP_findpropstrict, this.compileTimeQualifier);
                result.addInstruction(name_temp.getlocal());
                if ( rhs != null )
                    result.addAll(rhs);

                //  get/setprop(MultinameL) consumes a name from the value stack.
                result.addInstruction(opcode, this.compileTimeQualifier);
            }
            else if ( this.runtimeQualifier != null && this.runtimeName != null )
            {
                //  Generate RTQnameL type code.
                Binding name_temp = currentScope.allocateTemp();
                Binding qual_temp = currentScope.allocateTemp();

                Name rtqnl = new Name(CONSTANT_RTQnameL, null, null);

                result.addAll(getRuntimeName(iNode));
                result.addInstruction(name_temp.setlocal());

                result.addAll(getRuntimeQualifier(iNode));
                result.addInstruction(OP_dup);
                result.addInstruction(qual_temp.setlocal());

                result.addInstruction(name_temp.getlocal());
                //  findprop(RTQNameL) consumes namespace and name from the value stack.
                result.addInstruction(OP_findpropstrict, rtqnl);
                result.addInstruction(qual_temp.getlocal());
                result.addInstruction(name_temp.getlocal());
                if ( rhs != null )
                    result.addAll(rhs);

                //  get/setprop(RTQNameL) consumes namespace and name from the value stack.
                result.addInstruction(opcode, rtqnl);

                currentScope.releaseTemp(name_temp);
                currentScope.releaseTemp(qual_temp);
            }
            else
            {
                //  Last valid combination generates a RTQname.
                assert(this.runtimeQualifier != null && this.compileTimeName != null) : "Unknown runtime name configuration: " + this.toString();

                Name rtqn = new Name(CONSTANT_RTQname, null, this.compileTimeName.getBaseName());

                result.addAll(getRuntimeQualifier(iNode));
                result.addInstruction(OP_dup);
                //  findprop(RTQName) consumes a namespace from the value stack.
                result.addInstruction(OP_findpropstrict, rtqn);
                result.addInstruction(OP_swap);
                if ( rhs != null )
                    result.addAll(rhs);

                //  get/setprop(RTQName) consumes a namespace from the value stack.
                result.addInstruction(opcode, rtqn);
            }

            return result;
        }

        /**
         *  Generate the InstructionList that expresses this RuntimeName's qualifier.
         *  @param iNode - an IASNode to contribute debug info to the result InstructionList.
         *  @return the runtime qualifier setup instructions.
         */
        public InstructionList getRuntimeQualifier(IASNode iNode)
        {
            assert(hasRuntimeQualifier());
            InstructionList result = createInstructionList(iNode);

            result.addAll(replicate(this.runtimeQualifier));

            //  Ensure the last instruction is an OP_coerce to Namespace.
            Instruction last = result.lastElement();

            if  (
                    last.getOpcode() != OP_coerce ||
                    last.getOperandCount() == 0 ||
                    ! namespaceType.equals(last.getOperand(0))
                )
            {
                result.addInstruction(OP_coerce, namespaceType);
            }

            return result;
        }

        /**
         *  @return true if this RuntimeName has a runtime qualifier.
         */
        public boolean hasRuntimeQualifier()
        {
            return this.runtimeQualifier != null;
        }

        /**
         *  @return true if this RuntimeName has a runtime name.
         */
        public boolean hasRuntimeName()
        {
            return this.runtimeName != null;
        }

        /**
         *  Generate the InstructionList that expresses this RuntimeName's name.
         *  @param iNode - an IASNode to contribute debug info to the result InstructionList.
         *  @return the runtime name setup instructions.
         */
        public InstructionList getRuntimeName(IASNode iNode)
        {
            assert(hasRuntimeName());

            InstructionList result = createInstructionList(iNode);

            result.addAll(replicate(this.runtimeName));
            if ( result.lastElement().getOpcode() != OP_coerce_s)
                result.addInstruction(OP_coerce_s);
            return result;
        }

        /**
         *  Generate the runtime name appropriate to this qualifier/name combination.
         *  @return the runtime name appropriate to this qualifier/name combination.
         */
        public Name generateName()
        {
            Name result;

            if ( this.compileTimeQualifier != null )
            {
                result = this.compileTimeQualifier;
            }
            else if ( this.runtimeQualifier != null && this.runtimeName != null )
            {
                result = new Name(CONSTANT_RTQnameL, null, null);
            }
            else
            {
                //  Last valid combination generates a RTQname.
                assert(this.runtimeQualifier != null && this.compileTimeName != null) : "Unknown runtime name configuration: " + this.toString();

                result = new Name(CONSTANT_RTQname, null, this.compileTimeName.getBaseName());
            }

            return result;
        }

        /**
         *  Convenience method generates an assignment
         *  @param value - the value to set.
         *  @return the instruction sequence to set a
         *    the given multiname's value.
         */
        InstructionList generateAssignment(IASNode iNode, InstructionList value)
        {
            return generateGetOrSet(iNode, getAssignmentOpcode(iNode), value);
        }

        /**
         *  Deduce the correct assignment opcode for a property.
         *  @param iNode - the root of the reference to the property.
         *  @return OP_initproperty if the property is const, OP_setproperty if not.
         */
        int getAssignmentOpcode(IASNode iNode)
        {
            if ( SemanticUtils.isConst(iNode, currentScope.getProject()) && ABCGeneratingReducer.this.inInitializingContext(iNode) )
                return OP_initproperty;
            else
                return OP_setproperty;
        }

        /**
         *  Convenience method generates an r-value.
         *  @return the instruction sequence to look up
         *    the given multiname.
         */
        InstructionList generateRvalue(IASNode iNode)
        {
            return generateGetOrSet(iNode, OP_getproperty, null);
        }

        /**
         *  Diagnostic toString() method, 
         *  used primarily to debug the assertion in generate().
         */
        @Override
            public String toString()
            {
                return  "\n\t" + compileTimeQualifier + ",\n\t" + runtimeQualifier + ",\n\t" + compileTimeName + ",\n\t" + runtimeName;
            }
    }

    /**
     *  ForKeyValueLoopState holds the state of a for/in or for each/in loop:
     *  the Hasnext2Wrapper that tracks the state of the locals required to
     *  support the hasnext2 instruction, and the Labels that mark the top
     *  of the loop and the test.
     */
    private class ForKeyValueLoopState
    {
        /**
         *  Label for the loop-test logic.
         */
        Label test = new Label();
 
        /**
         *  Label for the top of the loop body.
         */
        Label loop = new Label();

        /**
         *  Hasnext2 management structure.
         */
        LexicalScope.Hasnext2Wrapper hasnext = currentScope.hasnext2();

        /**
         *  Generate the loop prologue code.
         *  @return loop prologue code.
         */
        InstructionList generatePrologue(IASNode iNode, InstructionList base)
        {
            InstructionList result = createInstructionList(iNode);

            //  Set up the object and index registers.
            result.addInstruction(OP_pushbyte, 0);
            result.addInstruction(hasnext.index_temp.setlocal());

            result.addAll(base);
            result.addInstruction(hasnext.stem_temp.setlocal());

            //  Go to the loop test.
            result.addInstruction(OP_jump, test);

            //  Top of loop processing.
            result.addInstruction(OP_label);
            result.labelCurrent(loop);
            return result;
        }

        /**
         *  Generate the instruction sequence that
         *  fetches the next key or value.
         *  @return the instruction sequence that
         *    fetches the next key or value.
         */
        InstructionList generateKeyOrValue(int opcode)
        {
            InstructionList result = new InstructionList();
            result.addInstruction(hasnext.stem_temp.getlocal());
            result.addInstruction(hasnext.index_temp.getlocal());
            result.addInstruction(opcode);
            return result;
        }

        /**
         *  Generate the loop epilogue code.
         *  @return loop epilogue code.
         */
        InstructionList generateEpilogue()
        {
            InstructionList result = new InstructionList();
            result.addInstruction(hasnext.instruction);
            result.labelCurrent(test);
            currentScope.getFlowManager().resolveContinueLabel(result);
            result.addInstruction(OP_iftrue, loop);
            hasnext.release();
            return result;
        }
    }

    /**
     *  The current LexicalScope.  Set by the caller,
     *  changes during reduction when a scoped construct
     *  such as an embedded function or a with statement
     *  is encountered.
     */
    private LexicalScope currentScope;

    /**
     *  Instructions sent by the caller to be added to
     *  a function definition.  Usually these are field
     *  initialization expressions in a constructor.
     */
    private InstructionList instanceInitializers;

    /**
     *  "Mini scope" regions, i.e., blocks of the method body.
     */
    Stack<InstructionList> miniScopes = new Stack<InstructionList>();

    /**
     *  A shared name for the Namespace type.
     */
    public static final Name namespaceType = new Name("Namespace");

    /**
     *  A shared name for the XML type.
     */
    public static final Name xmlType = new Name("XML");

    /**
     *  A shared name for the XMLList type.
     */
    public static final Name xmlListType = new Name("XMLList");

    /**
     *  A shared name for the RegExp type.
     */
    public static final Name regexType = new Name("RegExp");

    /**
     *  A shared name for the void type.
     */
    public static final Name voidType = new Name("void");

    /**
     *  Generate code to push a numeric constant.
     */
    public static void pushNumericConstant(long value, InstructionList result_list)
    {
        result_list.pushNumericConstant(value);
    }

    /**
     * Generate code to push a numeric value onto the stack.  This will take into account the type
     * of the expression that generated the number - e.g. a numeric value produced from a const of type 'int'
     * will produce at worst a pushint instruction
     *
     * Used by the constant folding routines so we don't lose type info when all we have is a Number.
     *
     * @param result_list   IL to add the instruction.
     * @param value         the numeric value to push
     * @param type          the type of the expression that produced the value.
     */
    public void pushNumericConstant(InstructionList result_list, Number value, IDefinition type)
    {
        ICompilerProject project = currentScope.getProject();
        if( type == project.getBuiltinType(BuiltinType.INT) )
        {
            int val = ECMASupport.toInt32(value);
            if( (byte)val == val )
            {
                result_list.addInstruction(OP_pushbyte, val);
            }
            else if( (short)val == val )
            {
                result_list.addInstruction(OP_pushshort, val);
            }
            else
            {
                result_list.addInstruction(OP_pushint, Integer.valueOf(val));
            }
        }
        else if( type == project.getBuiltinType(BuiltinType.UINT) )
        {
            long uval = ECMASupport.toUInt32(value.doubleValue());
            if ((uval & 0x7F) == uval) { // Pushbyte sign extends
                result_list.addInstruction(OP_pushbyte, (int)uval);
            }
            else if ((uval & 0x7FFF) == uval) { // Pushshort sign extends
                result_list.addInstruction(OP_pushshort, (int)uval);
            }
            else {
                result_list.addInstruction(OP_pushuint, Long.valueOf(uval));
            }
        }
        else
        {
            double dval = value.doubleValue();
            if( ECMASupport.isNan(dval) )
            {
                result_list.addInstruction(OP_pushnan);
            }
            else if (!Double.isInfinite(dval) && (dval == ECMASupport.toInt32(value)) )
            {
                // distinguish pos/neg 0
                // java treats -0 and 0 as equal, but divide by -0 results in NEG INFINITY, whereas pos
                // 0 results in POS INFINITY
                // positive 0 can be encoded with a pushbyte, but neg zero requires a pushdouble
                if( dval == 0.0 && 1.0/dval == Double.NEGATIVE_INFINITY )
                    result_list.addInstruction(OP_pushdouble, dval);
                else
                    // Integer
                    pushNumericConstant(result_list, ECMASupport.toInt32(value), project.getBuiltinType(BuiltinType.INT));
            }
            else if( Double.isNaN(dval) )
            {
                result_list.addInstruction(OP_pushnan);
            }
            else
            {
                result_list.addInstruction(OP_pushdouble, value.doubleValue());
            }
        }
    }

    /**
     *  @return the currently active collection of problems.
     */
    public Collection<ICompilerProblem> getProblems()
    {
        return currentScope.getProblems();
    }

    /**
     *  Generate a binary operator.
     *  @param l - the left-hand operand.
     *  @param r - the right-hand operand.
     *  @param opcode - the operator's opcode.
     *  @return the combined instruction sequence with the operator appended.
     */
    InstructionList binaryOp(IASNode iNode, InstructionList l, InstructionList r, int opcode)
    {
        checkBinaryOp(iNode, opcode);
        return binaryOperatorBody(iNode, l, r, InstructionFactory.getInstruction(opcode));
    }

    /**
     *  Generate a conditional jump operator.
     *  @param l - the left-hand operand.
     *  @param r - the right-hand operand.
     *  @param opcode - the operator's opcode.
     *  @return the combined instruction sequence with the operator appended.
     *    The target is filled in by a downstream reduction.
     */
    InstructionList conditionalJump(IASNode iNode, InstructionList l, InstructionList r, int opcode)
    {
        checkBinaryOp(iNode, opcode);
        return binaryOperatorBody(iNode, l, r, InstructionFactory.getTargetableInstruction(opcode));
    }

    /**
     *  Generate a binary operator.
     *  @param l - the left-hand operand.
     *  @param r - the right-hand operand.
     *  @param operator - the operator, as an Instruction.
     *  @return the combined instruction sequence with the operator appended.
     */
    InstructionList binaryOperatorBody(IASNode iNode, InstructionList l, InstructionList r, Instruction operator)
    {
        InstructionList result = createInstructionList(iNode, l.size() + r.size() + 1);
        result.addAll(l);
        result.addAll(r);
        result.addInstruction(operator);
        return result;
    }

    /**
     *  Perform semantic checks on a binary operator.
     */
    public void checkBinaryOp (IASNode iNode, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkBinaryOperator(iNode, opcode);
    }

    /**
     *  Resolve a dotted name, e.g., foo.bar.baz
     */
    Binding dottedName(IASNode iNode, String qualifiers, String base_name)
    {
        if ( iNode instanceof IdentifierNode )
        {
            return currentScope.resolveName((IdentifierNode)iNode);
        }
        else if ( iNode instanceof ExpressionNodeBase )
        {
            ExpressionNodeBase expr = (ExpressionNodeBase) iNode;
            Name n = expr.getMName(currentScope.getProject());

            if ( n == null )
            {
                currentScope.addProblem(new CodegenInternalProblem(iNode, "Unable to resove member name: " + iNode.toString()));
                n = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, qualifiers)), base_name);
            }

            return currentScope.getBinding(iNode, n, expr.resolve(currentScope.getProject()));
        }

        //else
            currentScope.addProblem(new CodegenInternalProblem(iNode, "Unable to resove to a dotted name: " + iNode.toString()));
            return new Binding(iNode, new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, qualifiers)), base_name), null);
    }

    /**
     *  Resolve a dotted name, e.g., foo.bar.baz, where the whole dotted name is a package
     *  this is an error, and a diagnostic will be emitted
     */
    Binding errorPackageName(IASNode iNode, String qualifiers, String base_name)
    {
        currentScope.addProblem(new PackageCannotBeUsedAsValueProblem(iNode, "'" + qualifiers + "." + base_name + "'"));
        return new Binding(iNode, new Name(qualifiers + "." + base_name), null);
    }

    private void generateAssignmentOp(IASNode site, Binding target, InstructionList result)
    {
        boolean inlined = generateInlineSetterAccess(target, result, true);

        if (!inlined)
            result.addInstruction(getAssignmentOpcode(site, target), target.getName());
    }

    /**
     * Deduce the correct assignment opcode for a Binding.
     * 
     * @param b - the Binding of interest.
     * @return the appropriate opcode to assign to the entity represented by the
     * Binding.
     */
    int getAssignmentOpcode(IASNode site, Binding b)
    {
        if (b.getDefinition() instanceof IConstantDefinition && this.inInitializingContext(site))
            return OP_initproperty;
        else if (b.isSuperQualified())
            return OP_setsuper;
        else
            return OP_setproperty;
    }

    /**
     *  Is the generator in an initializing context?
     *  @param iNode - the i-node of interest.
     *  @return true if the generator is in a context where it should
     *    generate initializer calls, i.e., OP_initproperty to set properties.
     */
    boolean inInitializingContext(IASNode iNode)
    {
        return SemanticUtils.isInVariableDeclaration(iNode) || SemanticUtils.isInConstructor(iNode);
    }


    /**
     *  Common routine used by reductions that
     *  need an empty list.
     *  @param iNode - the current AST node.
     */
    public InstructionList createInstructionList(IASNode iNode)
    {
        return createInstructionList(iNode, 0);
    }

    /**
     *  Common routine used by reductions that
     *  need an empty list.
     *  @param iNode - the current AST node.
     *  @param capacity - requested capacity of the new list.
     *    May be adjusted to accomodate debug instructions.
     */
    public InstructionList createInstructionList(IASNode iNode, int capacity)
    {
        DebugInfoInstructionList result;

        String file_name = SemanticUtils.getFileName(iNode);

        //  Note: getLine() uses zero-based counting.
        int line_num = iNode.getLine() + 1;

        //  Adjust the capacity requirement if debug
        //  instructions are to be emitted.
        if ( currentScope.emitFile(file_name) )
            capacity++;
        if ( currentScope.emitLine(line_num) )
            capacity++;

        //  If the required capacity is less than three
        //  instructions, the InstructionList can hold 
        //  them organically.  Specifying a capacity to
        //  the InstructionList ctor causes it to allocate
        //  a separate ArrayList.
        if ( capacity > 3 )
            result = new DebugInfoInstructionList(capacity);
        else
            result = new DebugInfoInstructionList();

        if ( currentScope.emitFile(file_name) )
        {
            currentScope.setDebugFile(file_name);
            result.addInstruction(OP_debugfile, currentScope.getDebugFile());
        }

        if ( currentScope.emitLine(line_num) )
        {
            // Set the line number in the instruction list - it will
            // emit or not emit the debugline depending on how it's used
            result.setDebugLine(line_num);
            currentScope.setDebugLine(line_num);
        }

        return result;
    }

    /**
     * Instruction list to help with emitting OP_debuglines.
     *
     * When given a line number, it will emit an OP_debugline when addInstruction is called with an
     * executable instruction.
     *
     * If addAll is called, then the debugline will not be emitted as the IL passed in should already have
     * any necessary debuglines if it contains executable instrucitons.
     *
     */
    private static class DebugInfoInstructionList extends InstructionList
    {
        /**
         * Constant for we don't have a line number
         */
        public static final int NO_LINE = -1;
        /**
         * The line number to use for an op_debugline when an executable instruction is added
         */
        private int line_num = NO_LINE;

        DebugInfoInstructionList (int capacity)
        {
            super(capacity);
        }
        DebugInfoInstructionList ()
        {
            super();
        }

        /**
         * Set the line number for this IL
         * @param line_num  the line number
         */
        void setDebugLine(int line_num)
        {
            this.line_num = line_num;
        }

        /**
         * Add the instruction to the IL.  If the instruction is
         * an exectuable instruction, and we have not emitted the line number yet,
         * then this will insert an OP_debugline with the line number before the instruction is
         * added.
         * @param insn  the instruction to be added.
         * @return      the added instruction
         */
        public Instruction addInstruction(Instruction insn)
        {

            if( line_num != NO_LINE && insn.isExecutable() )
            {
                // add the debugline instruction first

                // reset the line number
                // do this before adding the instruction so
                // we don't infinitely recurse
                int line = line_num;
                line_num = NO_LINE;

                addInstruction(OP_debugline, line);
            }
            return super.addInstruction(insn);
        }
    }

    /**
     *  Error trap.
     */
    public Binding error_namespaceAccess(IASNode iNode, IASNode raw_qualifier, Binding qualified_name)
    {
        String qualifier = ((IdentifierNode)raw_qualifier).getName();

        //  TODO: In some circumstances, the namespace qualifier
        //  may be an invalid attribute on a declaration.
        currentScope.addProblem(new UnknownNamespaceProblem(raw_qualifier, qualifier));

        return qualified_name;
    }

    /**
     *  Error trap.
     */
    public InstructionList error_reduce_Op_AssignId(IASNode iNode, InstructionList non_lvalue, InstructionList rvalue)
    {
        currentScope.addProblem(new InvalidLvalueProblem(iNode));
        InstructionList result = createInstructionList(iNode, 1);
        // Since we are reducing to an expression, make sure the instruction
        // list we return, produces a value on the operand stack.
        result.addInstruction(ABCConstants.OP_pushundefined);
        return result;
    }

    /**
     *  Generate access to a named entity.
     *  @param name - the entity's name.
     *  @return an instruction sequence to access the entity.
     */
    InstructionList generateAccess(Binding name)
    {
        return generateAccess(name, AccessType.Strict);
    }

    /**
     *  Enumerate possible access types to a named entity;
     *  Lenient is used in typeof expressions to access
     *  variables referred to by simple name and for the
     *  member references of member access expressions.
     *  All other expressions use strict access.
     */
    private enum AccessType { Strict, Lenient }

    /**
     *  Generate access to a named entity.
     *  @param name - the entity's name.
     *  @param accessType - one of Lenient or Strict.
     *  @return an instruction sequence to access the entity.
     */
    InstructionList generateAccess(Binding name, AccessType accessType)
    {
        InstructionList result = new InstructionList(2);
        generateAccess(name, accessType, result);
        return result;
    }

    /**
     *  Generate access to a named entity.
     *  @param name - the entity's name.
     *  @param result - the instruction sequence to generate into.
     */
    void generateAccess(Binding binding, InstructionList result)
    {
        generateAccess(binding, AccessType.Strict, result);
    }

    /**
     *  Generate access to a named entity.
     *  @param name - the entity's name.
     *  @param accessType - one of Lenient or Strict.
     *  @param result - the instruction sequence to generate into.
     */
    void generateAccess(Binding binding, AccessType accessType, InstructionList result)
    {
        if (binding.isLocal())
        {
            result.addInstruction(binding.getlocal());
        }
        else
        {
            assert (binding.getName() != null) : "non-local Binding " + binding + " must have a name";
            currentScope.getMethodBodySemanticChecker().checkGetProperty(binding);

            boolean inlined = generateInlineGetterAccess(binding, result, false);

            if (!inlined)
                generateAccess(binding, binding.isSuperQualified(), accessType, result);
        }
    }

    /**
     *  Generate access to a named entity.
     *  @param binding - the entity's binding.
     *  @param is_super - set if the name was explicitly qualified with "super."
     *  @param accessType - one of Lenient or Strict.
     *  @param result - the instruction sequence to generate into.
     */
    void generateAccess(Binding binding, final boolean is_super, AccessType accessType, InstructionList result)
    {
        final Name name = binding.getName();
        assert (name != null) : "binding must have a name when calling generateAccess()";

        final IASNode node = binding.getNode();
        if ( name.isTypeName() )
        {
            // a type names node should always be a ITypedExpressionNode
            ITypedExpressionNode typeNode = (ITypedExpressionNode)node;
            IExpressionNode collectionNode = typeNode.getCollectionNode();
            IDefinition collectionDefinition = SemanticUtils.getDefinition(collectionNode, currentScope.getProject());
            Binding collectionBinding = currentScope.getBinding(collectionNode, name.getTypeNameBase(), collectionDefinition);
            generateAccess(collectionBinding, is_super, AccessType.Strict, result);

            IExpressionNode typeTypeNode = typeNode.getTypeNode();
            IDefinition typeDefinition = SemanticUtils.getDefinition(typeTypeNode, currentScope.getProject());
            Binding typeBinding = currentScope.getBinding(typeTypeNode, name.getTypeNameParameter(), typeDefinition);
            generateTypeNameParameter(typeBinding, result);

            result.addInstruction(OP_applytype, 1);
        }
        else
        {
            // Test whether we're refering to the class being initialized, for example: 
            // public class C {
            //     private static var v:Vector.<C> = new Vector.<C>();
            // }
            // as in this case we need to do a getlocal0 as the class
            // hasn't been initialized yet
            boolean useLocal0 = false;
            if (node instanceof IdentifierNode)
            {
                IdentifierNode id = (IdentifierNode)node;
                IDefinition def = id.resolve(currentScope.getProject());
                if (SemanticUtils.isRefToClassBeingInited(id, def) && !currentScope.insideInlineFunction())
                    useLocal0 = true;
            }

            // TODO: use getslot when we can.
            // TODO: we can at least do this when we're accessing something in an activation object            
            if (useLocal0)
            {
                result.addInstruction(OP_getlocal0);
            }
            else if (is_super)
            {
                result.addAll(currentScope.findProperty(binding, true));
                result.addInstruction(OP_getsuper, name);
            }
            else if (accessType == AccessType.Lenient)
            {
                result.addAll(currentScope.findProperty(binding, false));
                result.addInstruction(OP_getproperty, name);
            }
            else
            {
                result.addAll(currentScope.getPropertyValue(binding));
            }
        }
    }

    private boolean generateInlineGetterAccess(Binding binding, InstructionList result, boolean isQualified)
    {
        IDefinition def = binding.getDefinition();
        if (!(def instanceof AccessorDefinition && currentScope.getMethodBodySemanticChecker().canGetterBeInlined((AccessorDefinition)def)))
            return false;

        AccessorDefinition accessorDefinition = (AccessorDefinition)def;
        if (accessorDefinition instanceof SetterDefinition)
            accessorDefinition = accessorDefinition.resolveCorrespondingAccessor(currentScope.getProject());

        assert (accessorDefinition != null) : "generateInlineGetterAccess() called with no getter definition";

        FunctionNode functionNode = (FunctionNode)accessorDefinition.getFunctionNode();
        return inlineFunction(accessorDefinition, functionNode, result, isQualified);
    }

    private boolean generateInlineSetterAccess(Binding binding, InstructionList result, boolean isQualified)
    {
        IDefinition def = binding.getDefinition();
        if (!(def instanceof AccessorDefinition && currentScope.getMethodBodySemanticChecker().canSetterBeInlined((AccessorDefinition)def)))
            return false;

        AccessorDefinition accessorDefinition = (AccessorDefinition)def;
        if (accessorDefinition instanceof GetterDefinition)
            accessorDefinition = accessorDefinition.resolveCorrespondingAccessor(currentScope.getProject());

        assert (accessorDefinition != null) : "generateInlineSetterAccess() called with no setter definition";

        FunctionNode functionNode = (FunctionNode)accessorDefinition.getFunctionNode();
        return inlineFunction(accessorDefinition, functionNode, result, isQualified);
    }

    private boolean generateInlineFunctionCall(Binding binding, InstructionList result, boolean isQualified, Vector<InstructionList> args)
    {
        IDefinition def = binding.getDefinition();
        if (!(def instanceof FunctionDefinition && (!(def instanceof IAccessorDefinition)) && currentScope.getMethodBodySemanticChecker().canFunctionBeInlined((FunctionDefinition)def)))
            return false;

        FunctionDefinition functionDef = (FunctionDefinition)binding.getDefinition();
        FunctionNode functionNode = (FunctionNode)functionDef.getFunctionNode();

        InstructionList insn = createInstructionList(functionNode);
        for (InstructionList arg: args)
            insn.addAll(arg);

        if (inlineFunction(functionDef, functionNode, insn, isQualified))
        {
            result.addAll(insn);
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean inlineFunction(FunctionDefinition functionDef, FunctionNode functionNode, InstructionList result, boolean isQualified)
    {
        try
        {
            InlineFunctionLexicalScope inlineFunctionScope = currentScope.pushInlineFunctionFrame(functionDef.getContainingScope(), isQualified, functionNode);
            currentScope = inlineFunctionScope;

            // generate the instructions for the body of the function
            IScopedNode body = functionNode.getScopedNode();
            InstructionList insns = inlineFunctionScope.getGenerator().generateInstructions(body, CmcEmitter.__statement_NT, inlineFunctionScope);

            currentScope = currentScope.popFrame();

            final ICompilerProject project = currentScope.getProject();
            if (currentScope.getMethodBodySemanticChecker().functionBodyHasNonInlineableInstructions(insns, functionDef.isInline(), functionDef.getBaseName()))
            {
                return false;
            }
            else
            {
                inlineFunctionScope.assignActualsToFormals(functionDef, result);

                if (isQualified)
                    result.addInstruction(inlineFunctionScope.setContainingClass());

                result.addAll(insns);

                // test for a fallthrough when the return type is non-void, as we
                // need coerce undefined to the return type to mimic returnvoid behavior
                if (!(functionDef instanceof SetterDefinition) && result.canFallThrough() || result.hasPendingLabels())
                {
                    TypeDefinitionBase returnType = (TypeDefinitionBase)functionDef.resolveReturnType(project);
                    if (returnType != currentScope.getProject().getBuiltinType(BuiltinType.VOID) &&
                        returnType != currentScope.getProject().getBuiltinType(BuiltinType.ANY_TYPE))
                    {
                        result.addInstruction(OP_pushundefined);
                        result.addInstruction(OP_coerce, returnType.getMName(project));
                    }
                }

                result.labelNext(inlineFunctionScope.getInlinedFunctionCallSiteLabel());
                inlineFunctionScope.mergeIntoContainingScope(result);
            }
        }
        finally
        {
            functionNode.discardFunctionBody();
        }

        return true;
    }

    /**
     *  Generate code to assign to a named entity.
     *  @param target - the entity to be assigned to.
     *  @param rvalue - the value to assign.
     *  @return an instruction sequence that stores
     *    the given rvalue in the target.
     *  
     */
    InstructionList generateAssignment(IASNode iNode, Binding target, InstructionList rvalue)
    {
        return generateAssignment(iNode, target, rvalue, false);
    }

    /**
     *  Generate code to assign to a named entity.
     *  @param target - the entity to be assigned to.
     *  @param rvalue - the value to assign.
     *  @param need_value - when true, 
     *    leave a DUP of the rvalue on the stack.
     *  @return an instruction sequence that stores
     *    the given rvalue in the target, and leaves
     *    a DUP of the rvalue on the stack if need_value is set.
     *  
     */
    InstructionList generateAssignment(IASNode iNode, Binding target, InstructionList rvalue, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode, rvalue.size() + 4);

        //  We may need to know if the RHS is in a local
        //  for some micro-optimizations; the rvalue instruction
        //  list is about to be invalidated, so speculatively
        //  determine that information here.
        int rhsOpcode = rvalue.lastElement().getOpcode();
        final boolean rhsIsLocal = 
            rhsOpcode == OP_getlocal || 
            rhsOpcode == OP_getlocal0 || 
            rhsOpcode == OP_getlocal1 || 
            rhsOpcode == OP_getlocal2 || 
            rhsOpcode == OP_getlocal3;

        result.addAll(rvalue);
        if ( need_value )
            result.addInstruction(OP_dup);

        // when assigning to a local or slot, we need to generate the type coerce and
        // runtime illegal write to const checks, so combine in this if block.
        if ( target.isLocal() || target.slotIdIsSet() )
        {
            if ( target.getDefinition() != null )
            {
                ITypeDefinition tgtType = target.getDefinition().resolveType(currentScope.getProject());
                ITypeDefinition srcType = null;

                srcType = SemanticUtils.resolveRHSTypeOfAssignment(currentScope.getProject(), iNode);

                if (
                    tgtType != null &&
                    tgtType != ClassDefinition.getAnyTypeClassDefinition() &&
                    !(srcType == tgtType && rhsIsLocal)
                )
                {
                    coerce(result, tgtType);
                }
            }

            //  If the target is a const and this assignment occurs outside the const's declaration,
            //  emulate the AVM's behavior by compiling in a throw statement.
            //  Note that this behavior differs from ASC's until ASC-4376 is fixed.
            //  We need to keep this runtime check, as in strict mode this is a semantic error, but
            //  in non-strict mode it needs to be caught at run time.
            if  (
                    SemanticUtils.isConstDefinition(target.getDefinition())  && 
                    !SemanticUtils.isInVariableDeclaration(iNode) && 
                    target.getName() != null
                )
            {
                result.addInstruction(OP_pushstring, "Illegal write to local const " + target.getName().getBaseName());
                result.addInstruction(OP_throw);
            }
            else if (target.slotIdIsSet())
            {
                if (currentScope.needsActivation())
                {
                    // Restore the activation object.
                    result.addInstruction(currentScope.getActivationStorage().getlocal());
                    result.addInstruction(OP_swap);
                }

                result.addInstruction(OP_setslot, target.getSlotId());
            }
            else
            {
                result.addInstruction(target.setlocal());
            }
        }
        else
        {
            boolean inlined = generateInlineSetterAccess(target, result, false);

            if (!inlined)
            {
                IDefinition def = target.getDefinition();
                if (def instanceof GetterDefinition)
                {
                    boolean isSuper = target.isSuperQualified();
                    SetterDefinition setter = (SetterDefinition)((GetterDefinition)def).
                                resolveCorrespondingAccessor(currentScope.getProject());
                    target = currentScope.getBinding(setter);   
                    if (isSuper)
                        target.setSuperQualified(true);
                }
                result.addAll(currentScope.findProperty(target, false));
                result.addInstruction(OP_swap);
                result.addInstruction(getAssignmentOpcode(iNode, target), target.getName());
            }
        }
        return result;
    }

    /**
     *  Generate a catch block.
     *  @param catch_proto - the catch block's prototype.
     */
    InstructionList generateCatchBlock( Label try_start, Label try_end, CatchPrototype catch_proto)
    {
        InstructionList scope_reinit = currentScope.getFlowManager().getScopeStackReinit();
        InstructionList current_catch = new InstructionList(catch_proto.catchBody.size() + scope_reinit.size() + 15);

        //  Common prologue code.
        if ( currentScope.needsThis() )
        {
            current_catch.addInstruction(OP_getlocal0);
            current_catch.addInstruction(OP_pushscope);
        }

        if( currentScope.needsActivation() )
        {
            //  Restore the activation object.
            current_catch.addInstruction(currentScope.getActivationStorage().getlocal());
            current_catch.addInstruction(OP_pushscope);
        }

        //  Re-establish enclosing exception scopes.
        current_catch.addAll(scope_reinit);

        int handler_number = currentScope.getMethodBodyVisitor().visitException(try_start, try_end, current_catch.getLabel(), catch_proto.catchType, catch_proto.catchVarName);
        Binding exception_storage = currentScope.getFlowManager().getFinallyContext().getExceptionStorage();

        current_catch.addInstruction(OP_newcatch, handler_number);
        current_catch.addInstruction(OP_dup);
        if ( exception_storage != null )
        {
            current_catch.addInstruction(exception_storage.setlocal());
            current_catch.addInstruction(OP_dup);
        }
        current_catch.addInstruction(OP_pushscope);
        current_catch.addInstruction(OP_swap);
        current_catch.addInstruction(OP_setslot, 1);

        current_catch.addAll(catch_proto.catchBody);

        if ( current_catch.canFallThrough() || current_catch.hasPendingLabels() )
        {
            current_catch.addInstruction(OP_popscope);
        }

        return current_catch;
    }

    /**
     *  Generate a compound assignment statement.
     */
    InstructionList generateCompoundAssignment(IASNode iNode, Binding lvalue, InstructionList expr, int opcode, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode, expr.size() + (lvalue.isLocal()? 4 : 8));

        currentScope.getMethodBodySemanticChecker().checkCompoundAssignment(iNode, lvalue, opcode);

        if ( lvalue.isLocal() )
        {
            result.addInstruction(lvalue.getlocal());
            result.addAll(expr);
            result.addInstruction(opcode);
            if ( need_value )
                result.addInstruction(OP_dup);

            // Coerce to the right type if the local has a type anno
            if ( lvalue.getDefinition() != null )
            {
                ITypeDefinition type = lvalue.getDefinition().resolveType(currentScope.getProject());

                if ( type != ClassDefinition.getAnyTypeClassDefinition() )
                {
                    coerce(result, type);
                }
            }

            result.addInstruction(lvalue.setlocal());
        }
        else
        {
            result.addAll(currentScope.findProperty(lvalue, true));
            result.addInstruction(OP_dup);
            result.addInstruction(OP_getproperty, lvalue.getName());
            result.addAll(expr);
            result.addInstruction(opcode);

            Binding value_temp = null;
            if ( need_value )
            {
                value_temp = currentScope.allocateTemp();
                result.addInstruction(OP_dup);
                result.addInstruction(value_temp.setlocal());
            }
            result.addInstruction(getAssignmentOpcode(iNode, lvalue), lvalue.getName());
            if ( need_value )
            {
                result.addInstruction(value_temp.getlocal());
                currentScope.releaseTemp(value_temp);
            }
        }

        return result;
    }

    InstructionList generateCompoundBracketAssignment(IASNode iNode, InstructionList stem, InstructionList index, InstructionList expr, int opcode, boolean need_value)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IBinaryOperatorNode)iNode).getLeftOperandNode();
        InstructionList result = createInstructionList(iNode, stem.size() * 2 + index.size() + expr.size() + 11 );

        //  Although ASC evaluates the stem twice, it only evaluates the index once.
        //  TODO: Inspect the index expression for side effects so the temp can be
        //  elided in most cases.
        Binding index_temp = currentScope.allocateTemp();
        result.addAll(index);
        result.addInstruction(index_temp.setlocal());

        result.addAll(replicate(stem));
        result.addInstruction(index_temp.getlocal());
        result.addInstruction(arrayAccess(arrayIndexNode, OP_getproperty));
        result.addAll(expr);
        result.addInstruction(opcode);

        Binding value_temp = currentScope.allocateTemp();
        result.addInstruction(value_temp.setlocal());

        result.addAll(stem);
        result.addInstruction(index_temp.getlocal());
        result.addInstruction(value_temp.getlocal());
        result.addInstruction(arrayAccess(arrayIndexNode, OP_setproperty));

        if ( need_value )
            result.addInstruction(value_temp.getlocal());

        currentScope.releaseTemp(value_temp);
        currentScope.releaseTemp(index_temp);

        return result;
    }

    InstructionList generateCompoundAssignmentToRuntimeName(IASNode iNode, RuntimeMultiname name, InstructionList expr, int opcode, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(name.generateRvalue(iNode));
        result.addAll(expr);
        result.addInstruction(opcode);

        if ( need_value )
        {
            //  TODO: In many cases this temp can be avoided by more sophisticated analysis.
            //  TODO: See also comments in RuntimeMultiname.generateGetOrSet().
            Binding temp = currentScope.allocateTemp();
            result.addInstruction(temp.setlocal());
            InstructionList temp_rhs = new InstructionList();
            temp_rhs.addInstruction(temp.getlocal());
            result.addAll(name.generateAssignment(iNode, temp_rhs));
            result.addInstruction(temp.getlocal());
            currentScope.releaseTemp(temp);
        }
        else
        {
            result = name.generateAssignment(iNode, result);
        }

        return result;
    }

    InstructionList generateCompoundMemberAssignment(IASNode iNode, InstructionList stem, Binding member, InstructionList expr, int fetch_opcode, int assign_opcode, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode, stem.size() * 2 + expr.size() + 5 );
        currentScope.getMethodBodySemanticChecker().checkCompoundAssignment(iNode, member, assign_opcode);

        //  TODO: Depending on the resolution of ASC-4159 and 
        //  the corresponding Royale backwards compatibility
        //  issue, cache the stem expression in a local to avoid
        //  multiple evaluations.
        result.addAll(replicate(stem));
        result.addInstruction(fetch_opcode, member.getName());

        result.addAll(expr);
        result.addInstruction(assign_opcode);

        if ( need_value )
        {
            result.addInstruction(OP_dup);
        }

        result.addAll(stem);
        result.addInstruction(OP_swap);
        result.addInstruction(getAssignmentOpcode(iNode, member), member.getName());

        return result;
    }

    /**
     *  Generate a compound logical assignment expression to a named lvalue.
     *  @param iNode - the assignment operator (root of the subtree).
     *  @param lvalue - the lvalue's name.
     *  @param expr - the expression to assign.
     *  @param is_and - true if the expression is &amp;&amp;=, false if it's ||=.
     *  @param need_value - true if the expression's not used in a void context.
     */
    InstructionList generateCompoundLogicalAssignment(IASNode iNode, Binding lvalue, InstructionList expr, boolean is_and, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);
        int failure_test = is_and? OP_iffalse : OP_iftrue;

        currentScope.getMethodBodySemanticChecker().checkCompoundAssignment(iNode, lvalue, failure_test);

        Label tail = new Label();

        if ( lvalue.isLocal() )
        {
            //  Fetch and test the current value.
            result.addInstruction(lvalue.getlocal());
            //  The current value may not be the result value,
            //  but for now assume it is.
            if ( need_value )
                result.addInstruction( OP_dup);

            result.addInstruction( failure_test, tail );

            //  Test succeeded: reset the value, but first
            //  pop the value speculatively dup'd above.
            if ( need_value )
                result.addInstruction(OP_pop);

            result.addAll(expr);

            if ( need_value )
                result.addInstruction(OP_dup);

            result.addInstruction(lvalue.setlocal());
        }
        else
        {
            //  Fetch, speculatively dup, and test the current value.
            result.addAll(currentScope.getPropertyValue(lvalue));

            if ( need_value )
                result.addInstruction(OP_dup);

            result.addInstruction(failure_test, tail);

            if ( need_value )
                result.addInstruction(OP_pop);

            result.addAll(expr);

            if ( need_value )
            {
                result.addInstruction(OP_dup);
            }

            result.addAll(currentScope.findProperty(lvalue, true));
            result.addInstruction(OP_swap);
            result.addInstruction(getAssignmentOpcode(iNode, lvalue), lvalue.getName());

        }

        result.labelNext(tail);
        return result;
    }

    /**
     *  Generate compound logical assignment to a runtime name, e.g., n::x ||= foo;
     *  @param iNode - the root of the assignment subtree.
     *  @param name - the runtime name.
     *  @param expr - the second operand of the implied binary expression.
     *  @param is_and - true if the result is set to the second operand iff the first operand is true.
     *  @param need_value - true if the value of the assignment is required.
     */
    InstructionList generateCompoundLogicalRuntimeNameAssignment(IASNode iNode, RuntimeMultiname name, InstructionList expr, boolean is_and, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);

        Label tail = new Label();
        int failure_test = is_and? OP_iffalse : OP_iftrue;

        Binding rhs_temp = null;
        InstructionList rhs_fetch = null;

        //  Fetch, speculatively dup, and test the current value.
        result.addAll(name.generateRvalue(iNode));

        if ( need_value )
            result.addInstruction(OP_dup);

        result.addInstruction(failure_test, tail);

        if ( need_value )
        {
            //  Clear the speculative dup.
            result.addInstruction(OP_pop);
        }

        result.addAll(expr);

        if ( need_value )
        {
            //  The runtime multiname instruction sequence
            //  doesn't allow the rhs expression to travel
            //  on the stack.
            rhs_temp = currentScope.allocateTemp();
            rhs_fetch = createInstructionList(iNode);

            result.addInstruction(OP_dup);
            result.addInstruction(rhs_temp.setlocal());
            rhs_fetch.addInstruction(rhs_temp.getlocal());

            result.addAll(name.generateAssignment(iNode, rhs_fetch));
            currentScope.releaseTemp(rhs_temp);
        }
        else
        {
            result = (name.generateAssignment(iNode, result));
        }

        result.labelNext(tail);
        return result;
    }

    /**
     *  Generate a compound logical assignment expression to a a[i] type lvalue.
     *  @param iNode - the assignment operator (root of the subtree).
     *  @param stem - the expression that generates the lvalue's stem, e.g., a in a[i]
     *  @param index - the index expression.
     *  @param expr - the expression to assign.
     *  @param is_and - true if the expression is &amp;&amp;=, false if it's ||=.
     *  @param need_value - true if the expression's not used in a void context.
     */
    InstructionList generateCompoundLogicalBracketAssignment(IASNode iNode, InstructionList stem, InstructionList index, InstructionList expr, boolean is_and, boolean need_value)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IBinaryOperatorNode)iNode).getLeftOperandNode();
        
        InstructionList result = createInstructionList(iNode, stem.size() * 2 + index.size() + expr.size() + 11 );

        Label tail = new Label();
        int failure_test = is_and? OP_iffalse : OP_iftrue;

        //  Although ASC evaluates the stem twice, it only evaluates the index once.
        //  TODO: Inspect the index expression for side effects so the temp can be
        //  elided in most cases.
        Binding index_temp = currentScope.allocateTemp();
        result.addAll(index);
        result.addInstruction(index_temp.setlocal());

        result.addAll(replicate(stem));
        result.addInstruction(index_temp.getlocal());
        result.addAll(stem);
        result.addInstruction(index_temp.getlocal());
        result.addInstruction(arrayAccess(arrayIndexNode, OP_getproperty));
        //  Assume this is the result.
        result.addInstruction(OP_dup);
        result.addInstruction(failure_test, tail);

        //  Pop the speculative result and assign the correct one.
        result.addInstruction(OP_pop);
        result.addAll(expr);

        result.labelNext(tail);
        Binding value_temp = null;
        if ( need_value )
        {
            value_temp = currentScope.allocateTemp();
            result.addInstruction(OP_dup);
            result.addInstruction(value_temp.setlocal());
        }
        result.addInstruction(arrayAccess(arrayIndexNode, OP_setproperty));
        if ( need_value )
        {
            result.addInstruction(value_temp.getlocal());
            currentScope.releaseTemp(value_temp);
        }

        currentScope.releaseTemp(index_temp);

        return result;
    }

    /**
     *  Generate a compound logical assignment expression to a foo.bar type lvalue
     *  @param iNode - the assignment operator (root of the subtree).
     *  @param stem - the expression that generates the lvalue's stem, e.g., a in a[i]
     *  @param index - the index expression.
     *  @param expr - the expression to assign.
     *  @param is_and - true if the expression is &amp;&amp;=, false if it's ||=.
     *  @param need_value - true if the expression's not used in a void context.
     */
    InstructionList generateCompoundLogicalMemberAssignment(IASNode iNode, InstructionList stem, Binding member, InstructionList expr, int fetch_opcode, boolean is_and, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);
        int failure_test = is_and? OP_iffalse : OP_iftrue;
        currentScope.getMethodBodySemanticChecker().checkCompoundAssignment(iNode, member, failure_test);

        Label tail = new Label();

        result.addAll(replicate(stem));
        result.addAll(stem);
        result.addInstruction(OP_getproperty, member.getName());
        //  Assume this is the result.
        result.addInstruction(OP_dup);
        result.addInstruction(failure_test, tail);

        result.addInstruction(OP_pop);
        result.addAll(expr);

        result.labelNext(tail);
        Binding value_temp = null;

        if ( need_value )
        {
            value_temp = currentScope.allocateTemp();
            result.addInstruction(OP_dup);
            result.addInstruction(value_temp.setlocal());
        }

        result.addInstruction(getAssignmentOpcode(iNode, member), member.getName());

        if ( need_value )
        {
            result.addInstruction(value_temp.getlocal());
            currentScope.releaseTemp(value_temp);
        }

        return result;

    }

    /**
     *  generateFunctionBody() wrapper suitable for calling from the BURM.
     *  See JBurg ENHRQ <N> : the grammar that accepts the BURM's parameters
     *  to a JBurg.Reduction routine doesn't grok function calls, so the BURM
     *  cannot call generateFunctionBody(body, name.getName());
     */
    InstructionList generateFunctionBody(IASNode iNode, InstructionList function_body, Binding return_type)
    {
        return generateFunctionBody(iNode, function_body, return_type != null? return_type.getName(): null);
    }

    /**
     *  Generate boilerplate function prolog/epilog code.
     *  @param block - the actual CFG.
     *  @param return_type - the function's return type.
     *  @return the function body.
     */
    InstructionList generateFunctionBody(IASNode iNode, InstructionList function_body, Name return_type)
    {
        currentScope.getMethodBodySemanticChecker().checkFunctionBody(iNode);

        currentScope.getMethodInfo().setReturnType(return_type);
        InstructionList result = currentScope.finishMethodDeclaration( !function_body.isEmpty() || haveInstanceInitializers(), SemanticUtils.getFileName(iNode));

        //  Constructor-specific processing: add the instance initializers,
        //  add a constructsuper call if none exists.
        if ( haveInstanceInitializers() && currentScope.getNestingState() == NestingState.NotNested)
        {
            result.addAll(this.instanceInitializers);

            //  If this is a constructor and there's no explicit
            //  super() call, synthesize one.
            //  Note that this may be a semantic error if the 
            //  superclass' constructor needs arguments.
            if ( !function_body.hasSuchInstruction(OP_constructsuper) )
            {
                currentScope.getMethodBodySemanticChecker().checkDefaultSuperCall(iNode);
                //  Call the superclass' constructor after the instance
                //  init instructions; this doesn't seem like an abstractly
                //  correct sequence, but it's what ASC does.
                result.addInstruction(OP_getlocal0);
                result.addInstruction(OP_constructsuper, 0);
            }
        }

        result.addAll(function_body);

        //  Epilog code.
        if ( result.canFallThrough() || result.hasPendingLabels() )
        {
            //  Synthesize a returnvoid instruction, using the
            //  single-purpose returnvoid so the caller can
            //  search for it.

            //  If, at some point, the MBSC walks all functions' CFGs,
            //  then all returnvoid processing can be centralized and
            //  this specialized Instruction will be unnecessary.
            result.addInstruction(synthesizedReturnVoid);
        }

        return result;
    }

    /**
     *  This returnvoid instruction is used when the reducer injects a returnvoid at
     *  the end of a method body; the caller (with access to the MethodBodyInfo) can
     *  create a control flow graph and search it to emit a diagnostic as appropriate.
     */
    public static final Instruction synthesizedReturnVoid = new NoOperandsInstruction(OP_returnvoid);

    /**
     *  Generate a named nested function.
     *
     *  This will generate init instructions either in controlFlow?Sensitive?Destination, or in the current scopes hoisted init instructions.
     *  Most of the time the instuctions will go in the hoisted init instructions, but if the nested function is declared
     *  in a 'with' or 'catch' block then the init instructions need to go into the normal controlFlow?Sensitive?Destination.
     *
     *  @param iNode - the function node for the nested function.
     *  @param controlFlowSensitiveDestination - the instruction list to add instructions to
     *  @param func_name - the function's name.
     *  @param return_type - the function's return type.
     *  @param function_body - the body of the function.
     *  @pre the function's lexical scope must be at the
     *    top of the lexical scope stack, with the declaring
     *    function's lexical scope under it.
     *  @post the nested function's MethodInfo is filled in,
     *    the function body is attached to its MethodBodyInfo,
     *    and the declaring function's initialization sequence
     *    gets code to declare the function.
     */
    private void generateNestedFunction(IASNode iNode, InstructionList controlFlowSensitiveDestination, Binding func_name, Name return_type, InstructionList function_body)
    {
        currentScope.setFunctionName(func_name.getName().getBaseName());
        currentScope.generateNestedFunction(generateFunctionBody(iNode, function_body, return_type));
        //  Pull the nested function's MethodInfo out of its scope before we pop it.
        MethodInfo nested_method_info = currentScope.getMethodInfo();

        currentScope = currentScope.popFrame();

        //  Initialize the nested function; add a variable 
        //  to the containing function scope and add 
        //  newfunction/setproperty logic to the containing
        //  function's hoisted initialization instructions.
        currentScope.makeVariable(func_name);

        InstructionList init_insns = SemanticUtils.canNestedFunctionBeHoisted(iNode) ? currentScope.getHoistedInitInstructions() : controlFlowSensitiveDestination;

        //  The containing function must be marked needsActivation() so the
        //  binding cannot be local.
        assert(!func_name.isLocal());
        init_insns.addInstruction(OP_findproperty, func_name.getName());
        init_insns.addInstruction(OP_newfunction, nested_method_info);
        init_insns.addInstruction(OP_setproperty, func_name.getName());
    }

    /**
     *  Generate a try/catch/finally (or try/finally) compound statement.
     *  @param try_stmt - the body of the try block.
     *  @param catch_blocks - associated catch blocks.
     *    May be null if no catch blocks are present.
     *  @param finally_stmt - the body of the finally block.
     */
    InstructionList generateTryCatchFinally ( InstructionList try_stmt, Vector<CatchPrototype> catch_blocks, InstructionList finally_stmt)
    {
        InstructionList normal_flow_fixup = new InstructionList();
        InstructionList catch_insns = new InstructionList();
        InstructionList final_catch = new InstructionList();
        InstructionList finally_insns = new InstructionList();
        InstructionList final_throw = new InstructionList();

        ExceptionHandlingContext finally_context = currentScope.getFlowManager().getFinallyContext();

        Label final_catch_target = final_catch.getLabel();

        //  We need a local to store the caught exception.
        Binding exception_storage = finally_context.getExceptionStorage();

        Collection<Label> pending_normal_control_flow = try_stmt.stripPendingLabels();

        if ( try_stmt.canFallThrough() || pending_normal_control_flow != null )
        {
            normal_flow_fixup.addInstruction(OP_jump, finally_context.finallyBlock);
        }
        else
        {
            //  Extend the region past a terminating
            //  throw statement to give the AVM a
            //  little buffer to figure out its
            //  exception-handling regions.
            normal_flow_fixup.addInstruction(OP_nop);
        }

        Label try_start = try_stmt.getLabel();
        Label try_end   = normal_flow_fixup.getLastLabel();

        Label finally_region_end = null;

        if ( null == catch_blocks )
        {
            finally_region_end = try_end;
        }
        else
        {
            for ( CatchPrototype catch_proto: catch_blocks )
            {

                InstructionList catch_body = generateCatchBlock(try_start, try_end, catch_proto);

                boolean is_last_catch = catch_proto.equals(catch_blocks.lastElement());

                if ( catch_body.canFallThrough() )
                {
                    //  Signal the finally block that this execution succeeded.
                    catch_body.addInstruction(OP_pushbyte, 0);
                    catch_body.addInstruction(finally_context.finallyReturnStorage.setlocal());
                    catch_body.addInstruction(OP_jump, finally_context.finallyBlock);
                }
                else if ( is_last_catch )
                {
                    //  Extend the region past a terminating throw
                    //  insn to give the AVM a little buffer.
                    catch_body.addInstruction(OP_nop);
                }

                if ( is_last_catch )
                    finally_region_end = catch_body.getLastLabel();

                catch_insns.addAll(catch_body);
            }
        }

        //  Set up the exception handler for the finally block.
        currentScope.getMethodBodyVisitor().visitException(try_start, finally_region_end, final_catch_target, null, null);

        //  The final catch block only needs to save the
        //  caught exception for a rethrow.
        if ( currentScope.needsThis() )
        {
            final_catch.addInstruction(OP_getlocal0);
            final_catch.addInstruction(OP_pushscope);
        }

        if( currentScope.needsActivation() )
        {
            //  Restore the activation object
            final_catch.addInstruction(currentScope.getActivationStorage().getlocal());
            final_catch.addInstruction(OP_pushscope);
        }

        final_catch.addAll(currentScope.getFlowManager().getScopeStackReinit());
        final_catch.addInstruction(exception_storage.setlocal());
        //  Signal the finally epilog that this execution failed
        //  and should rethrow.
        final_catch.addInstruction(OP_pushbyte, currentScope.getFlowManager().getFinallyAlternativesSize() + 1);
        final_catch.addInstruction(finally_context.finallyReturnStorage.setlocal());

        //  falls through
        // final_catch.addInstruction(OP_jump,  finally_head);

        finally_insns.addInstruction(finally_context.finallyReturnStorage.getlocal());
        finally_insns.addInstruction(OP_convert_i);
        finally_insns.addAll(currentScope.getFlowManager().getFinallySwitch());

        //  Label the start of the final set of instructions.
        if (!finally_stmt.isEmpty())
        {
            finally_stmt.labelFirst(finally_context.finallyBlock);
        }
        else
            //  This is just an expedient for this degenerate finally.
            finally_insns.labelFirst(finally_context.finallyBlock);

        final_throw.addInstruction(exception_storage.getlocal());
        final_throw.labelCurrent(finally_context.finallyDoRethrow);
        final_throw.addInstruction(OP_throw);

        //  Assemble the statement.
        InstructionList result = new InstructionList();

        result.addInstruction(OP_pushbyte, 0);
        result.addInstruction(finally_context.finallyReturnStorage.setlocal());

        result.addAll(try_stmt);

        //  Send all "next statement" type control flow into the finally block.
        result.addAllPendingLabels(pending_normal_control_flow);

        result.addAll(normal_flow_fixup);

        result.addAll(catch_insns);
        result.addAll(final_catch);

        result.addAll(finally_stmt);
        result.addAll(finally_insns);
        result.addAll(final_throw);

        for ( ExceptionHandlingContext.FinallyReturn retblock: finally_context.finallyReturns )
            result.addAll(retblock.getInstructions());

        //  TODO: Need more analysis of how this temp travels through
        //  the system before it can be safely released.  For now
        //  just leak it.
        //  currentScope.releaseTemp(exception_storage);

        //  TODO: Removing a hanging kill exposed a latent bug 
        //  in hasPendingLabels() and end-of-routine processing.
        //  Give the CG a harmless instruction that will generate
        //  a returnvoid if this is the last statement in the routine.
        result.addInstruction(OP_nop);

        //  Fallthrough out of the finally block.
        result.labelNext(finally_context.finallyDoFallthrough);

        return result;
    }

    /**
     * Reduce a name to an instruction list that can be used in a parameterized type expression.
     * This is different from the normal name -> expression because '*' maps to pushnull.
     * @param iNode         the node
     * @param param_name    the name to reduce
     * @return              IL with instructions to push the appropriate value on the stack
     */
    public InstructionList reduce_typeNameParameter(IASNode iNode, Binding param_name)
    {
        InstructionList result = createInstructionList(iNode);

        generateTypeNameParameter(param_name, result);

        return result;
    }

    /**
     *  Generate the instruction sequence that designates
     *  the parameter of a parameterized type, e.g.,
     *  String in Vector.&lt;String&gt; or * in Vector.&lt;*&gt;.
     *  @param param_node - the type parameter's node.
     *  @param param_name - the type parameter's name.
     *    May be null in the * case.
     *  @param result - the instruction sequence to generate into.
     */
    private void generateTypeNameParameter(Binding param, InstructionList result)
    {
        if ( param == null || param.getName() == null || param.getName().couldBeAnyType() )
            result.addInstruction(OP_pushnull);
        else
            generateAccess(param, false, AccessType.Strict, result);
    }

    /**
     * @return A
     * {@link ControlFlowContextManager.ControlFlowContextSearchCriteria} that
     * will find the control context that a break statement without a label in
     * the current control flow context refers to. This should always be a
     * control flow context for any of:
     * <ul>
     * <li>a labeled statement</li>
     * <li>a loop</li>
     * <li>a switch statement</li>
     * </ul>
     */
    private ControlFlowContextManager.ControlFlowContextSearchCriteria getBreakCriteria()
    {
        return ControlFlowContextManager.breakWithOutLabelCriteria;
    }
    
    /**
     * @return A
     * {@link ControlFlowContextManager.ControlFlowContextSearchCriteria} that
     * will find the control context that a continue statement without a label
     * in the current control flow context refers to. This should always be a
     * control flow context for a loop.
     */
    private ControlFlowContextManager.ControlFlowContextSearchCriteria getContinueCriteria()
    {
        return ControlFlowContextManager.continueWithOutLabelCriteria;
    }
    
    /**
     * @param Label name in a break statement with a label.
     * @return A
     * {@link ControlFlowContextManager.ControlFlowContextSearchCriteria} that
     * will find the control context that a break statement with a label in the
     * current control flow context refers to. This should always be a control
     * flow context for labeled statement.
     */
    private ControlFlowContextManager.ControlFlowContextSearchCriteria getBreakCriteria(String label)
    {
        return currentScope.getFlowManager().breakWithLabelCriteria(label);
    }
    
    /**
     * @param Label name in a continue statement with a label.
     * @return A
     * {@link ControlFlowContextManager.ControlFlowContextSearchCriteria} that
     * will find the control context that a continue statement with a label
     * in the current control flow context refers to. This should always be a
     * control flow context for a loop.
     */
    private ControlFlowContextManager.ControlFlowContextSearchCriteria getContinueCriteria(String label)
    {
        return currentScope.getFlowManager().continueWithLabelCriteria(label);
    }
    
    /**
     * @param Label name in a goto statement.
     * @return A
     * {@link ControlFlowContextManager.ControlFlowContextSearchCriteria} that
     * will find the control context that a goto statement in the
     * current control flow context refers to. This should always be a control
     * flow context for labeled statement.
     */
    private ControlFlowContextManager.ControlFlowContextSearchCriteria getGotoCriteria(String label)
    {
        return currentScope.getFlowManager().gotoLabelCriteria(label, false);
    }
    
    /**
     * Constructs a sequence of code that will jump to the proper location in the
     * control flow context matched by the criteria object.  The returned code will
     * run finally blocks and adjust the scope stack if needed.
     * @param criteria Criteria object that matches the control flow context and specifies the location
     * in that context to which the generated code will jump to.
     * @return An {@link InstructionList} that will jump to the proper location in the
     * control flow context matched by the criteria object
     * @throws UnknownControlFlowTargetException
     */
    private InstructionList getBranchTarget(ControlFlowContextManager.ControlFlowContextSearchCriteria criteria)
        throws UnknownControlFlowTargetException
    {
        return currentScope.getFlowManager().getBranchTarget(criteria);
    }

    /**
     *  @return the double content of a numeric literal.
     *  @param iNode - the literal node.
     */
    Double getDoubleContent(IASNode iNode)
    {
        return SemanticUtils.getDoubleContent(iNode);
    }

    /**
     *  @return the name of an identifier.
     *  @param iNode - the IIdentifier node.
     */
    String getIdentifierContent(IASNode iNode)
    {
        return SemanticUtils.getIdentifierContent(iNode);
    }

    /**
     *  @return the int content of a numeric literal.
     *  @param iNode - the literal node.
     */
    Integer getIntegerContent(IASNode iNode)
    {
        return SemanticUtils.getIntegerContent(iNode);
    }
    
    /**
     *  @return always zero.
     *  @param iNode - the literal node.
     */
    Integer getIntegerZeroContent(IASNode iNode)
    {
        return 0;
    }    
    
    /**
     *  @return always zero.
     *  @param iNode - the literal node.
     */
    Long getIntegerZeroContentAsLong(IASNode iNode)
    {
        return 0L;
    }

    /**
     *  @return a node cast to MXMLEventSpecifierNode type.
     *  @param iNode - the MXMLEventSpecifierNode node.
     */
    IMXMLEventSpecifierNode getMXMLEventSpecifierContent(IASNode iNode)
    {
        return SemanticUtils.getMXMLEventSpecifierContent(iNode);
    }

    /**
     *  Find all active exception handling blocks or scopes,
     *  and set up finally return sequences and/or popscopes.
     *  @param original - the original control-flow sequence.
     *  @param criterion - the search criterion that identifies
     *    the boundary of the region to be exited.
     *  @return a control-flow sequence that is either the
     *    original sequence or the start of a trampoline of
     *    finally blocks, popscopes, and whatever else is 
     *    necessary to exit the control-flow region.
     *  @throws UnknownControlFlowTargetException if the criterion is not on the control-flow stack.
     */
    private InstructionList getNonLocalControlFlow(InstructionList original, ControlFlowContextManager.ControlFlowContextSearchCriteria criterion)
        throws UnknownControlFlowTargetException
    {
        return currentScope.getFlowManager().getNonLocalControlFlow(original, criterion);
    }
    
    /**
     *  Find all active exception handling blocks or scopes,
     *  and set up finally return sequences and/or popscopes.
     *  @param criterion - the search criterion that identifies
     *    the boundary of the region to be exited and the target
     *    in the found region to jump to.
     *  @return a control-flow sequence that is either the
     *    original sequence or the start of a trampoline of
     *    finally blocks, popscopes, and whatever else is 
     *    necessary to exit the control-flow region.
     *  @throws UnknownControlFlowTargetException if the criterion is not on the control-flow stack.
     */
    private InstructionList getNonLocalControlFlow(ControlFlowContextManager.ControlFlowContextSearchCriteria criterion)
        throws UnknownControlFlowTargetException
    {
        return currentScope.getFlowManager().getNonLocalControlFlow(getBranchTarget(criterion), criterion);
    }

    /**
     *  @return a parameter's definition.
     *  @param iNode - the parameter node.
     */
    IDefinition getParameterContent(IASNode iNode)
    {
        return SemanticUtils.getParameterContent(iNode);
    }
    
    /**
     *  @return the string content of a literal.
     *  @param iNode - the literal node.
     */
    String getStringLiteralContent(IASNode iNode)
    {
        return SemanticUtils.getStringLiteralContent(iNode);
    }

    /**
     *  @return the uint content of a numeric literal.
     *  @param iNode - the literal node.
     */
    Long getUintContent(IASNode iNode)
    {
        return SemanticUtils.getUintContent(iNode);
    }
    
    /**
     *  @return the Boolean content of a BOOLEAN literal.
     *  @param iNode - the literal node.
     */
    boolean getBooleanContent(IASNode iNode)
    {
        return SemanticUtils.getBooleanContent(iNode);
    }

    /**
     *  @return true if instance initialization instructions are present.
     */
    private boolean haveInstanceInitializers()
    {
        return instanceInitializers != null;
    }

    /* 
     * 
     *  *******************************
     *  **  Cost/Decision Functions  **
     *  *******************************
     *
     */

    /**
     *  Explore a MemberAccessNode and 
     *  decide if its stem is a reference
     *  to a package.
     *
     *  This method will always return a result greater than what isPackageName will return,
     *  as package name must "win" over dotted name.
     */
    int isDottedName(IASNode n)
    {
        int result = Integer.MAX_VALUE;

        if ( n instanceof MemberAccessExpressionNode )
        {
            MemberAccessExpressionNode ma = (MemberAccessExpressionNode)n;

            if(ma.stemIsPackage())
                // This needs to be greater than the value returned from isPackageName,
                // so that isPackageName wins
                result = 2;
        }

        return result;
    }

    /**
     *  Explore a MemberAccessNode and
     *  decide if it is a reference
     *  to a package.
     *
     *  This method will always return a result less than what isDottedName will return,
     *  as package name must "win" over dotted name.
     */
    int isPackageName(IASNode n)
    {
        int result = Integer.MAX_VALUE;

        if ( n instanceof MemberAccessExpressionNode )
        {
            MemberAccessExpressionNode ma = (MemberAccessExpressionNode)n;

            if(ma.isPackageReference())
                // This needs to be less than the value returned from isDottedName,
                // so that isPackageName wins
                result = 1;
        }

        return result;
    }

    /**
     *  Get the definition associated with a node's qualifier
     *  and decide if the qualifier is a compile-time constant.
     *  @param iNode - the node to check.
     *  @pre - the node has an IdentifierNode 0th child.
     *  @return an attractive cost if the child has a known namespace, i.e.,
     *    it's a compile-time constant qualifier.
     */
    int qualifierIsCompileTimeConstant(IASNode iNode)
    {
        IdentifierNode qualifier = (IdentifierNode)SemanticUtils.getNthChild(iNode, 0);
        IDefinition def = qualifier.resolve(currentScope.getProject());

        int result = def instanceof NamespaceDefinition ? 1 : Integer.MAX_VALUE;
        return result;
    }

    /**
     *  @return a feasible cost if a node has a compile-time constant defintion.
     */
    int isCompileTimeConstant(IASNode iNode)
    {
        if ( SemanticUtils.transformNameToConstantValue(iNode, currentScope.getProject()) != null )
            return 1;
        else
            return Integer.MAX_VALUE;
    }

    /**
     *  Check a Binding to decide if its referent 
     *  is statically known to be a Namespace. 
     *  @param b - the Binding to check.
     *  @return true if the given Binding refers to a namespace.
     */
    private boolean isNamespace(Binding b)
    {
        return b.getDefinition() instanceof NamespaceDefinition;
    }

    /**
     *  Check a Binding to see if it's defined as the ANY namespace.
     *  @param b - the Binding to check.
     *  @return true if the Binding's definition is the ANY namespace definition.
     */
    private boolean isAnyNamespace(Binding b)
    {
        return b.getDefinition().equals(NamespaceDefinition.getAnyNamespaceReference());
    }
    
    /**
     *  @return a feasible cost (1) if the node is for 'new Array()'
     */
    int isEmptyArrayConstructor(IASNode iNode)
    {
        ICompilerProject project = currentScope.getProject();
        IIdentifierNode identifierNode = (IIdentifierNode)SemanticUtils.getNthChild(iNode, 1);
        if (identifierNode.resolve(project) == project.getBuiltinType(BuiltinType.ARRAY))
            return 1;

        return Integer.MAX_VALUE;
    }

    /**
     *  @return a feasible cost (1) if the node is for 'new Object()'
     */
    int isEmptyObjectConstructor(IASNode iNode)
    {
        ICompilerProject project = currentScope.getProject();
        IIdentifierNode identifierNode = (IIdentifierNode)SemanticUtils.getNthChild(iNode, 1);
        if (identifierNode.resolve(project) == project.getBuiltinType(BuiltinType.OBJECT))
            return 1;

        return Integer.MAX_VALUE;
    }

    /*
     *  ************************
     *  **  Reduction prologues
     *  ************************
     */

    public void prologue_anonymousFunction(IASNode iNode)
    {
        //  TODO: The current scope may not 
        //  need an activation if the anonymous
        //  function doesn't access any of the
        //  current scope's locals.
        currentScope = currentScope.pushFrame();
        currentScope.declareAnonymousFunction();
        currentScope.setInitialControlFlowRegionNode(((FunctionObjectNode)iNode).getFunctionNode().getScopedNode());
    }
    
    public void prologue_functionObject(IASNode iNode)
    {
        final FunctionNode innerFunctionNode = ((FunctionObjectNode)iNode).getFunctionNode();
        currentScope = currentScope.pushFrame();
        currentScope.declareFunctionObject(innerFunctionNode.getName());
        currentScope.setInitialControlFlowRegionNode(innerFunctionNode.getScopedNode());
        prologue_function(iNode);
    }

    public void prologue_blockStmt(IASNode iNode)
    {
        //  Note: don't create an InstructionList with
        //  line number information here; this IL is
        //  a pure placeholder.
        this.miniScopes.push(new InstructionList());
    }

    public void prologue_blockStmt_to_finally_clause(IASNode iNode)
    {
        currentScope.getFlowManager().startFinallyContext(iNode);
    }

    public void prologue_catchBlock(IASNode iNode)
    {
        currentScope.getFlowManager().startCatchContext((ICatchNode)iNode);
    }

    private void loop_prologue_common(IASNode loopNode, IASNode loopContents)
    {
        currentScope.getFlowManager().startLoopControlFlowContext(loopContents);
    }
    
    public void prologue_countedForStmt(IASNode iNode)
    {
        loop_prologue_common(iNode, ((IForLoopNode)iNode).getStatementContentsNode());
    }

    public void prologue_doStmt(IASNode iNode)
    {
        loop_prologue_common(iNode, ((IWhileLoopNode)iNode).getStatementContentsNode());
    }

    public void prologue_forEachStmt(IASNode iNode)
    {
        loop_prologue_common(iNode, ((IForLoopNode)iNode).getStatementContentsNode());
    }

    public void prologue_forInStmt(IASNode iNode)
    {
        loop_prologue_common(iNode, ((IForLoopNode)iNode).getStatementContentsNode());
    }

    void prologue_function(IASNode n)
    {
        FunctionDefinition func = null;

        IFunctionNode funcNode = null;
        
        if ( n instanceof IFunctionNode )
            funcNode = (IFunctionNode)n;
        else if( n instanceof FunctionObjectNode )
            funcNode = ((FunctionObjectNode)n).getFunctionNode();

        // Add any parse problems which might have been encountered while lazily parsing the function
        if( funcNode instanceof FunctionNode )
        {
            Collection<ICompilerProblem> parseProblems = ((FunctionNode) funcNode).getParsingProblems();
            for( ICompilerProblem problem : parseProblems )
                currentScope.addProblem(problem);
        }

        assert funcNode != null : n + " has no FunctionNode child";
        func = (FunctionDefinition)funcNode.getDefinition();
        assert(func != null): n + " has no definition.";

        currentScope.setLocalASScope(func.getContainedScope());
        currentScope.resetDebugInfo();

        // Set the current file name - the function body will always start with an OP_debugfile
        // so we never need emit another one unless the method body spans multiple files
        currentScope.setDebugFile(SemanticUtils.getFileName(n));

        currentScope.getMethodBodySemanticChecker().checkFunctionDefinition(funcNode, func);
        for ( int i = 0; i < funcNode.getChildCount(); i++ )
            scanFunctionBodyForActivations(funcNode.getChild(i));
    }

    public void prologue_labeledStmt(IASNode iNode)
    {
        // Since the Prologue code runs before the subtrees have been reduced, 
        // we can't get the label string via the non_resolving_identifier reduction.
        try
        {
            currentScope.getFlowManager().startLabeledStatementControlFlowContext((LabeledStatementNode) iNode);
        }
        catch ( DuplicateLabelException dup_label )
        {
            currentScope.addProblem(new DuplicateLabelProblem(iNode));
        }
    }

    public void prologue_mxmlEventSpecifier(IASNode iNode)
    {
        // The method that implements an event specifier has a single parameter.
        // Its name is "event" (which is discovered from the ParameterDefinition
        // that the event node creates).
        // Its type, such as flash.events.MouseEvent, has already been determined
        // when we created the MethodInfo, so we access it there
        // rather than creatinga another equivalent Name.
        currentScope.makeParameter(
            getMXMLEventSpecifierContent(iNode).getEventParameterDefinition(),
            currentScope.getMethodInfo().getParamTypes().get(0));

    }

    public void prologue_switchStmt(IASNode iNode)
    {
        // Establish a switch context so that breaks inside the switch statement will work.
        currentScope.getFlowManager().startSwitchContext((SwitchNode)iNode);
    }

    public void prologue_tryCatchFinallyStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startExceptionContext((ITryNode) iNode);
        currentScope.getFlowManager().getFinallyContext().setHasFinallyBlock(true);
    }

    public void prologue_tryCatchStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startExceptionContext((ITryNode) iNode);
    }

    public void prologue_tryFinallyStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startExceptionContext((ITryNode) iNode);
        currentScope.getFlowManager().getFinallyContext().setHasFinallyBlock(true);
    }

    public void prologue_typedFunction_to_statement(IASNode iNode)
    {
        //  Note: A named nested function does 
        //  always need an activation record.
        currentScope = currentScope.pushFrame();
        currentScope.declareNestedFunction();
        currentScope.setInitialControlFlowRegionNode(((IFunctionNode)iNode).getScopedNode());
        prologue_function(iNode);
    }

    public void prologue_typelessFunction_to_statement(IASNode iNode)
    {
        //  Note: A named nested function does 
        //  always need an activation record.
        currentScope = currentScope.pushFrame();
        currentScope.declareNestedFunction();
        currentScope.setInitialControlFlowRegionNode(((IFunctionNode)iNode).getScopedNode());
        prologue_function(iNode);
    }

    /**
     *  Count the number of typeof levels active.
     */
    private int typeofCount = 0;

    /**
     *  Before reducing the operands of a typeof,
     *  we need to increment the typeof counter.
     *  It will be decremented by the reduction's
     *  epilogue.
     */
    public void prologue_typeof(IASNode iNode)
    {
        this.typeofCount++;
    }

    public void prologue_whileStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startLoopControlFlowContext(((IWhileLoopNode)iNode).getStatementContentsNode());
    }

    public void prologue_withStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startWithContext(((IWithNode)iNode).getStatementContentsNode());
    }

    /*
     *  *************************
     *  **  Reduction actions  **
     *  *************************
     */
    

    public InstructionList reduce_anonymousFunction(IASNode iNode, InstructionList function_body)
    {
        InstructionList result = createInstructionList(iNode, 1);

        currentScope.generateNestedFunction(function_body);
        result.addInstruction(OP_newfunction, currentScope.getMethodInfo());

        currentScope = currentScope.popFrame();
        return result;
    }
    
    public InstructionList reduce_functionObject(IASNode iNode, IASNode function_name, InstructionList plist, Binding return_binding, InstructionList function_body)
    {
        InstructionList result = createInstructionList(iNode);

        Binding nestedFunctionName = currentScope.resolveName((IdentifierNode)function_name);
        Name return_type;

        if ( return_binding != null ) 
            return_type = return_binding.getName();
        else
            return_type = null;

        currentScope.generateNestedFunction(generateFunctionBody(iNode, function_body, return_type));
        //  Pull the nested function's MethodInfo out of its scope before we pop it.
        MethodInfo nested_method_info = currentScope.getMethodInfo();
        currentScope = currentScope.popFrame();

        //  Create a wrapper routine that returns the named
        //  routine wrapped in a closure; it's an anonymous
        //  function with an activation record.
        currentScope = currentScope.pushFrame();
        currentScope.declareAnonymousFunction();
        currentScope.setInitialControlFlowRegionNode(((FunctionObjectNode)iNode).getFunctionNode().getScopedNode());
        currentScope.setNeedsActivation();

        currentScope.makeVariable(
                nestedFunctionName, 
                SemanticUtils.getAETName(currentScope.getProject().getBuiltinType(BuiltinType.FUNCTION), currentScope.getProject()),
                null, // no meta tags
                null, // no initializer
                LexicalScope.VariableMutability.Constant
            );

        InstructionList wrapper_insns = createInstructionList(iNode);

        wrapper_insns.addInstruction(OP_newfunction, nested_method_info);
        wrapper_insns.addInstruction(OP_dup);
        wrapper_insns.addInstruction(OP_findproperty, nestedFunctionName.getName());
        wrapper_insns.addInstruction(OP_swap);
        wrapper_insns.addInstruction(OP_setslot, 1);
        wrapper_insns.addInstruction(OP_returnvalue);
        currentScope.generateNestedFunction(generateFunctionBody(iNode, wrapper_insns, LexicalScope.anyType));

        MethodInfo wrapper_method_info = currentScope.getMethodInfo();
        currentScope = currentScope.popFrame();

        result.addInstruction(OP_newfunction, wrapper_method_info);
        //  Doesn't matter much what we use as "this" --
        //  the store acts on the activation.
        result.addInstruction(OP_getlocal0);
        result.addInstruction(OP_call, 0);

        return result;
    
    }

    public InstructionList reduce_arrayIndexExpr(IASNode iNode, InstructionList stem, boolean is_super, InstructionList index)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)iNode;

        if ( is_super )
            currentScope.getMethodBodySemanticChecker().checkSuperAccess(arrayIndexNode);

        InstructionList result;

        if ( !is_super )
        {
            result = createInstructionList(arrayIndexNode, stem.size() + index.size() + 1);
            result.addAll(stem);
        }
        else
        {
            result = createInstructionList(arrayIndexNode, index.size() + 2);
            //  Use "this" as the stem.
            result.addInstruction(OP_getlocal0);
        }
        result.addAll(index);

        result.addInstruction(arrayAccess(arrayIndexNode, is_super? OP_getsuper: OP_getproperty));

        return result;
    }

    public InstructionList reduce_arrayLiteral(IASNode iNode, Vector<InstructionList> elements)
    {
        //  No semantic restrictions on array literals.

        //  TODO: Investigate optimizing.
        InstructionList result = createInstructionList(iNode);

        for ( InstructionList element: elements )
            result.addAll(element);

        result.addInstruction(OP_newarray, elements.size());
        return result;
    }

    public InstructionList reduce_assignToBracketExpr_to_expression(IASNode iNode, InstructionList stem, InstructionList index, InstructionList r, boolean is_super)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IBinaryOperatorNode)iNode).getLeftOperandNode();
        
        currentScope.getMethodBodySemanticChecker().checkAssignToBracketExpr(iNode);

        Binding local = currentScope.allocateTemp();

        InstructionList result = createInstructionList(iNode, (is_super ? 1:stem.size()) + index.size() + r.size() + 5);
        if( is_super )
            result.addInstruction(OP_getlocal0);
        else
            result.addAll(stem);
        
        result.addAll(index);
        result.addAll(r);
        result.addInstruction(OP_dup);
        result.addInstruction(local.setlocal() );
        result.addInstruction(arrayAccess(arrayIndexNode, is_super ? OP_setsuper : OP_setproperty));
        result.addInstruction(local.getlocal() );

        currentScope.releaseTemp(local);

        return result;
    }

    public InstructionList reduce_assignToBracketExpr_to_void_expression(IASNode iNode, InstructionList stem, InstructionList index, InstructionList r, boolean is_super)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IBinaryOperatorNode)iNode).getLeftOperandNode();
        
        /*
        ITypeDefinition destinationType = arrayIndexNode.resolveType(currentScope.getProject());
        IExpressionNode valueNode = ((IBinaryOperatorNode)iNode).getRightOperandNode();
        
        IDefinition type_def = valueNode.resolveType(currentScope.getProject()); //SemanticUtils.getDefinitionOfUnderlyingType(, 
                           // true, currentScope.getProject());
        boolean need_coerce = !type_def.equals(destinationType) || (valueNode instanceof TernaryOperatorNode);
        */

        currentScope.getMethodBodySemanticChecker().checkAssignToBracketExpr(iNode);
        
        int num_ins = /*need_coerce ? 2 :*/ 1;

        InstructionList result = createInstructionList(iNode, (is_super ? num_ins : stem.size()) + index.size() + r.size() + num_ins);
        if( is_super )
            result.addInstruction(OP_getlocal0);
        else
            result.addAll(stem);
        result.addAll(index);
        result.addAll(r);
        /*
        if (need_coerce)
            coerce(result, destinationType);
        */
        result.addInstruction(arrayAccess(arrayIndexNode, is_super ? OP_setsuper : OP_setproperty));

        return result;
    }

    public InstructionList reduce_assignToMemberExpr_to_expression(IASNode iNode, InstructionList stem, Binding member, InstructionList r)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, member);

        Binding local = currentScope.allocateTemp();

        InstructionList result = createInstructionList(iNode, stem.size() + r.size() + 5);
        result.addAll(stem);
        result.addAll(r);
        result.addInstruction(OP_dup);
        result.addInstruction(local.setlocal() );
        generateAssignmentOp(iNode, member, result);
        result.addInstruction(local.getlocal() );

        currentScope.releaseTemp(local);

        return result;
    }

    public InstructionList reduce_assignToMemberExpr_to_void_expression(IASNode iNode, InstructionList stem, Binding member, InstructionList r)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, member);

        InstructionList result = createInstructionList(iNode, stem.size() + r.size() + 1);
        result.addAll(stem);
        result.addAll(r);

        generateAssignmentOp(iNode, member, result);

        return result;
    }

    public InstructionList reduce_assignToDescendantsExpr(IASNode iNode, InstructionList stem, Binding member, InstructionList r, boolean need_value)
    {
        //  Note: This code doesn't assign to descendants!
        //  While one might think it should, it does in fact
        //  mirror the bytecode ASC emitted.
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, member);

        Binding local = null;

        InstructionList result = createInstructionList(iNode, stem.size() + r.size() + 5);
        result.addAll(stem);
        result.addAll(r);
        if ( need_value )
        {
            local = currentScope.allocateTemp();
            result.addInstruction(OP_dup);
            result.addInstruction(local.setlocal() );
        }

        generateAssignmentOp(iNode, member, result);

        if ( need_value )
        {
            result.addInstruction(local.getlocal());
            currentScope.releaseTemp(local);
        }

        return result;
    }

    public InstructionList reduce_assignToNameExpr_to_void_expression(IASNode iNode, Binding lvalue, InstructionList r)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, lvalue);
        return generateAssignment(iNode, lvalue, r);
    }

    public InstructionList reduce_assignToNameExpr_to_expression(IASNode iNode, Binding lvalue, InstructionList r)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, lvalue);
        return generateAssignment(iNode, lvalue, r, true);
    }

    public InstructionList reduce_assignToQualifiedMemberExpr(IASNode iNode, InstructionList stem, Binding qualifier, Binding member, InstructionList rhs, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);

        if ( !isNamespace(qualifier) )
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
        }
        //  else the qualifier's namespace is already present in the name.
        result.addAll(rhs);

        Binding value_temp = null;
        if ( need_value )
        {
            value_temp = currentScope.allocateTemp();
            result.addInstruction(OP_dup);
            result.addInstruction(value_temp.setlocal());
        }

        generateAssignmentOp(iNode, member, result);

        if ( need_value )
        {
            result.addInstruction(value_temp.getlocal());
            currentScope.releaseTemp(value_temp);
        }

        return result;
    }

    public InstructionList reduce_assignToQualifiedRuntimeMemberExpr(IASNode iNode, InstructionList stem, Binding qualifier, InstructionList runtime_member_selector, InstructionList rhs, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        if ( isNamespace(qualifier) )
        {
            result.addAll(runtime_member_selector);
            //  Extract the URI from the namespace and use it to construct a qualified name.
            NamespaceDefinition ns_def = (NamespaceDefinition)qualifier.getDefinition();
            Name qualified_name = new Name(CONSTANT_MultinameL, new Nsset(ns_def.resolveAETNamespace(currentScope.getProject())), null);
            result.addAll(rhs);
            result.addInstruction(OP_setproperty, qualified_name);
        }
        else
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
            result.addAll(runtime_member_selector);
            result.addAll(rhs);

            Binding value_temp = null;
            if ( need_value )
            {
                value_temp = currentScope.allocateTemp();
                result.addInstruction(OP_dup);
                result.addInstruction(value_temp.setlocal());
            }
            result.addInstruction(OP_setproperty, new Name(CONSTANT_RTQnameL, null, null));

            if ( need_value )
            {
                result.addInstruction(value_temp.getlocal());
                currentScope.releaseTemp(value_temp);
            }
        }
        return result;
    }

    public InstructionList reduce_assignToQualifiedAttributeExpr(IASNode iNode, InstructionList stem, Binding qualifier, Binding member, InstructionList rhs, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);

        if ( !isNamespace(qualifier) )
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
        }
        //  else the qualifier's namespace is already present in the name.

        result.addAll(rhs);

        Binding value_temp = null;
        if ( need_value )
        {
            value_temp = currentScope.allocateTemp();
            result.addInstruction(OP_dup);
            result.addInstruction(value_temp.setlocal());
        }

        generateAssignmentOp(iNode, member, result);

        if ( need_value )
        {
            result.addInstruction(value_temp.getlocal());
            currentScope.releaseTemp(value_temp);
        }

        return result;
    }

    public InstructionList reduce_assignToQualifiedRuntimeAttributeExpr(IASNode iNode, InstructionList stem, Binding qualifier, InstructionList runtime_member_selector, InstructionList rhs, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);

        if ( isAnyNamespace(qualifier) )
        {
            IASNode site = qualifier.getNode() != null? qualifier.getNode(): iNode;

            currentScope.addProblem(new AnyNamespaceCannotBeQualifierProblem(site));
        }
        else if ( ! isNamespace(qualifier) )
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
            result.addAll(runtime_member_selector);
            result.addInstruction(OP_coerce_s);
            result.addAll(rhs);
            Binding value_temp = null;

            if ( need_value )
            {
                value_temp = currentScope.allocateTemp();
                result.addInstruction(OP_dup);
                result.addInstruction(value_temp.setlocal());
            }

            result.addInstruction(OP_setproperty, new Name(CONSTANT_RTQnameLA, null, null));

                if ( need_value )
                {
                    result.addInstruction(value_temp.getlocal());
                    currentScope.releaseTemp(value_temp);
                }

        }
        else
        {
            result.addAll(runtime_member_selector);
            //  Extract the URI from the namespace and use it to construct a qualified name.
            NamespaceDefinition ns_def = (NamespaceDefinition)qualifier.getDefinition();
            Name qualified_name = new Name(CONSTANT_MultinameLA, new Nsset(ns_def.resolveAETNamespace(currentScope.getProject())), null);
            result.addAll(rhs);

            Binding value_temp = null;
            if ( need_value )
            {
                value_temp = currentScope.allocateTemp();
                result.addInstruction(OP_dup);
                result.addInstruction(value_temp.setlocal());
            }

            result.addInstruction(OP_setproperty, qualified_name);

            if ( need_value )
            {
                result.addInstruction(value_temp.getlocal());
                currentScope.releaseTemp(value_temp);
            }
        }
        return result;
    }


    public InstructionList reduce_assignToRuntimeNameExpr(IASNode iNode, RuntimeMultiname lval, InstructionList r, final boolean need_value)
    {
        InstructionList result;

        if ( need_value )
        {
            //  TODO: In many cases this temp can be avoided by more sophisticated analysis.
            //  TODO: See also comments in RuntimeMultiname.generateGetOrSet().
            Binding temp = currentScope.allocateTemp();
            result = createInstructionList(iNode);
            result.addAll(r);
            result.addInstruction(temp.setlocal());
            InstructionList temp_rhs = new InstructionList();
            temp_rhs.addInstruction(temp.getlocal());
            result.addAll(lval.generateAssignment(iNode, temp_rhs));
            result.addInstruction(temp.getlocal());
            currentScope.releaseTemp(temp);
        }
        else
        {
            result = lval.generateAssignment(iNode, r);
        }

        return result;
    }

    public InstructionList reduce_assignToUnqualifiedRuntimeAttributeExpr(IASNode iNode, InstructionList stem, InstructionList rt_attr, InstructionList rhs, boolean need_value)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);
        result.addAll(rt_attr);
        result.addAll(rhs);

        Binding value_temp = null;
        if ( need_value )
        {
            value_temp = currentScope.allocateTemp();
            result.addInstruction(OP_dup);
            result.addInstruction(value_temp.setlocal());
        }

        result.addInstruction(OP_setproperty, new Name(CONSTANT_MultinameLA, new Nsset(new Namespace(CONSTANT_PackageNs)), null));

        if ( need_value )
        {
            result.addInstruction(value_temp.getlocal());
            currentScope.releaseTemp(value_temp);
        }

        return result;
    }

    public Binding reduce_attributeName(IASNode iNode, Binding attr_name)
    {
        // do nothing - just return the contained name
        // which will already be set up correctly for an attribute name
        return attr_name;
    }

    public InstructionList reduce_blockStmt_to_finally_clause(IASNode iNode, Vector<InstructionList> stmts)
    {
        InstructionList result = createInstructionList(iNode);

        for ( InstructionList stmt: stmts )
            result.addAll(stmt);

        currentScope.getFlowManager().endFinallyContext();
        return result;

    }

    public InstructionList reduce_blockStmt_to_statement(IASNode iNode, Vector<InstructionList> stmts)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(this.miniScopes.pop());

        for ( InstructionList stmt: stmts )
        {
            //  Some statement-level reductions return null.
            if ( stmt != null )
            {
                result.addAll(stmt);
            }
        }

        return result;
    }

    public Boolean reduce_booleanLiteral(IASNode iNode)
    {
        return SemanticUtils.getBooleanContent(iNode);
    }

    public InstructionList reduce_breakStmt(IASNode iNode)
    {
        try
        {
            ControlFlowContextManager.ControlFlowContextSearchCriteria criterion = getBreakCriteria();
            return getNonLocalControlFlow(criterion);
        }
        catch ( UnknownControlFlowTargetException no_target )
        {
            currentScope.addProblem(new UnknownBreakTargetProblem(iNode));
            return createInstructionList(iNode);
        }

    }

    public String reduce_by_concatenation(IASNode iNode, String first, String second)
    {
        return first + "." + second;
    }

    public CatchPrototype reduce_catchBlockTyped(IASNode iNode, Binding var_name, Binding exception, InstructionList action)
    {
        CatchPrototype result = new CatchPrototype();
        result.catchVarName = var_name.getName();
        result.catchType = exception.getName();
        result.catchBody = action;

        currentScope.getFlowManager().endCatchContext();
        return result;
    }

    public CatchPrototype reduce_catchBlockUntyped(IASNode iNode, Binding var_name, InstructionList action)
    {
        CatchPrototype result = new CatchPrototype();
        result.catchVarName = var_name.getName();
        result.catchType = null;
        result.catchBody = action;

        currentScope.getFlowManager().endCatchContext();
        return result;
    }

    public InstructionList reduce_commaExpr(IASNode iNode, InstructionList payload_expr, Vector<InstructionList> exprs)
    {
        //  TODO: Investigate optimizing this.
        InstructionList result = createInstructionList(iNode);

        for ( InstructionList other_expr: exprs )
            result.addAll(other_expr);

        //  The payload expression is the last expression in the
        //  comma list.  It was inverted by IASNodeAdapter into
        //  the 0th position to work around limitations of the
        //  BURG's n-ary operand processor.
        result.addAll(payload_expr);

        return result;
    }

    public ConditionalFragment reduce_conditionalFragment(IASNode iNode, InstructionList condition, Vector<InstructionList> consequents)
    {
        return new ConditionalFragment(iNode, condition, consequents);
    }

    public ConditionalFragment reduce_constantConditionalFragment(IASNode iNode, Object constantCondition, Vector<InstructionList> consequents)
    {
        return new ConditionalFragment(iNode, constantCondition, consequents);
    }

    /**
     * reduce a concatenation of two strings to a string constant
     * @param iNode  the node
     * @param l      the left string
     * @param r      the right string
     * @return       the concatenated string
     */
    public String reduce_constantStringConcatenation(IASNode iNode, String l, String r)
    {
        return l + r;
    }

    /**
     * reduce a concatenation of a string and a constant value to a string constant.  The constant
     * value will be converted to a string according to the ECMA spec, and then will be concatenated with the
     * string.
     * @param iNode  the node
     * @param l      the left string
     * @param r      the right value
     * @return       the concatenated string
     */
    public String reduce_constantStringConcatenation(IASNode iNode, String l, Object r)
    {
        return reduce_constantStringConcatenation(iNode, l, ECMASupport.toString(r));
    }

    /**
     * reduce a concatenation of a constant value and a string to a string constant.  The constant
     * value will be converted to a string according to the ECMA spec, and then will be concatenated with the
     * string.
     * @param iNode  the node
     * @param l      the left value
     * @param r      the right string
     * @return       the concatenated string
     */
    public String reduce_constantStringConcatenation(IASNode iNode, Object l, String r)
    {
        return reduce_constantStringConcatenation(iNode, ECMASupport.toString(l), r);
    }

    /**
     * reduce addition of two constant values.  If either of the constants is a String, then string
     * concatenation will be performed.  OTherwise the constants will be converted to Numbers, according to the
     * ECMA spec, and added
     * @param iNode     the node
     * @param l         the left value
     * @param r         the right value
     * @return          the result of adding the two values
     */
    public Object reduce_constantAddition(IASNode iNode, Object l, Object r)
    {
        checkBinaryOp(iNode, OP_add);
        if( l instanceof String )
            return reduce_constantStringConcatenation(iNode, (String)l, r);
        else if( r instanceof String )
            return reduce_constantStringConcatenation(iNode, l, (String)r);
        else
            return ECMASupport.toNumeric(l).doubleValue() + ECMASupport.toNumeric(r).doubleValue();
    }

    public InstructionList reduce_continueStmt(IASNode iNode)
    {
        try
        {
            ControlFlowContextManager.ControlFlowContextSearchCriteria criterion = getContinueCriteria();
            return getNonLocalControlFlow(criterion);
        }
        catch ( UnknownControlFlowTargetException no_target )
        {
            currentScope.addProblem(new UnknownContinueTargetProblem(iNode));
            return createInstructionList(iNode);
        }
    }

    public InstructionList reduce_countedForStmt(IASNode iNode, InstructionList init, InstructionList test_insns, InstructionList incr, InstructionList body)
    {
        InstructionList test_insns_with_debug_ops = ensureInstructionListHasDebugInfo(test_insns, init);
        InstructionList result = createInstructionList(iNode, init.size() + test_insns_with_debug_ops.size() + incr.size() + body.size() + 5);

        //  Initialize counters, jump to test
        result.addAll(init);

        result.addInstruction(OP_jump, test_insns_with_debug_ops.getLabel());

        //  Loop body
        InstructionList loop_body = new InstructionList();
        Label loop_head = new Label();
        loop_body.addInstruction(OP_label);
        loop_body.labelCurrent(loop_head);
        loop_body.addAll(body);

        //  Set the continue target on the correct instruction.
        if ( !incr.isEmpty() )
            currentScope.getFlowManager().resolveContinueLabel(incr);
        else
            currentScope.getFlowManager().resolveContinueLabel(loop_body);

        result.addAll(loop_body);

        //  Now add the loop counter increments.
        //  The continue context needs to be set
        //  first for InstructionList hygiene.
        result.addAll(incr);

        //  Add the test; a successful test branches
        //  back to the loop head.  The test generation
        //  rules guarantee this last instruction is a
        //  branch instruction that needs a target.
        test_insns_with_debug_ops.lastElement().setTarget(loop_head);
        result.addAll(test_insns_with_debug_ops);

        currentScope.getFlowManager().finishLoopControlFlowContext(result);

        return result;
    }

    /**
     * Check that the dest InstructionList contains debug information.  If not, a new InstructionList
     * will be returned with the debug information extracted from the src InstructionLevel.
     * 
     * @param dest the InstructionList to ensure has debug information
     * @param src the InstructionList from which to obtain debug information if needed
     * @return the resulting InstructionList with debug information
     */
    private static InstructionList ensureInstructionListHasDebugInfo(final InstructionList dest, final InstructionList src)
    {
        // the condition has some debug info, so nothing to do here.
        if (dest.hasSuchInstruction(OP_debugline))
            return dest;

        // If we don't have any debug info on the condition IL, get it from the init IL.
        String debugFile = null;
        int debugLine = LexicalScope.DEBUG_LINE_UNKNOWN;
        final ArrayList<Instruction> srcInstructions = src.getInstructions();
        for (int i = 0; i < srcInstructions.size(); i++)
        {
            final Instruction insn = srcInstructions.get(i);
            if (insn.getOpcode() == OP_debugfile)
            {
                debugFile = (String)insn.getOperand(0);
            }
            else if (insn.getOpcode() == OP_debugline)
            {
                debugLine = insn.getImmediate();
                break;
            }
        }

        InstructionList result = new DebugInfoInstructionList(dest.size() + 2);
        if (debugFile != null)
            result.addInstruction(OP_debugfile, debugFile);

        if (debugLine != LexicalScope.DEBUG_LINE_UNKNOWN)
            result.addInstruction(OP_debugline, debugLine);

        result.addAll(dest);

        return result;
    }

    public InstructionList reduce_defaultXMLNamespace(IASNode iNode, InstructionList ns_expr)
    {
        InstructionList result = createInstructionList(iNode);
        //  TODO: This could be optimized for the string constant case,
        //  but ASC doesn't do so, which means it's future and probably
        //  also not well tested in the AVM.
        result.addAll(ns_expr);
        result.addInstruction(OP_dxnslate);
        currentScope.setSetsDxns();
        return result;
    }

    /**
     * Reduce expression like:<br>
     * {@code delete o[p]}
     * 
     * @param iNode Tree node for the {@code delete} statement.
     * @param stem Instructions for creating a {@code DynamicAccessNode}.
     * @param index Instructions for initializing the index expression.
     * @return Instructions for executing a {@code delete} statement.
     */
    public InstructionList reduce_deleteBracketExpr(IASNode iNode, InstructionList stem, InstructionList index)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IUnaryOperatorNode)iNode).getOperandNode();
        
        // ASC doesn't check bracket expressions, and it's difficult
        // to see what could usefully be done in any case.
        // currentScope.getMethodBodySemanticChecker().checkDeleteExpr(iNode);
        InstructionList result = createInstructionList(iNode, stem.size() + index.size() + 1);
        result.addAll(stem);
        result.addAll(index);
        result.addInstruction(arrayAccess(arrayIndexNode, OP_deleteproperty));
        return result;
    }

    /**
     * Reduce E4X expression like:<br>
     * {@code delete x.@[foo]}
     * 
     * @param iNode Tree node for the {@code delete} statement.
     * @param stem Instructions for creating a
     * {@code MemberAccessExpressionNode}.
     * @param index Instructions for initializing the array index expression.
     * @return Instructions for executing a {@code delete} statement.
     */
    public InstructionList reduce_deleteAtBracketExpr(IASNode iNode, InstructionList stem, InstructionList index)
    {
        final InstructionList result = createInstructionList(iNode, stem.size() + index.size() + 1);
        result.addAll(stem);
        result.addAll(index);
        // The namespace is ignored by AVM. We choose to use the default 
        // namespace at the current scope.
        final Namespace ns = ((INamespaceResolvedReference)NamespaceDefinition
                .getDefaultNamespaceDefinition(currentScope.getLocalASScope()))
                .resolveAETNamespace(currentScope.getProject());
        final Name multinameLA = new Name(
                CONSTANT_MultinameLA,
                new Nsset(ns),
                null);
        result.addInstruction(OP_deleteproperty, multinameLA);
        return result;
    }

    public InstructionList reduce_deleteDescendantsExpr(IASNode iNode, InstructionList stem, Binding field)
    {
        // TODO: Investigate semantics of this.
        // This special case should get a warning or some indication it's a tautology.

        InstructionList result = createInstructionList(iNode, stem.size() + 1);
        result.addAll(stem);
        result.addInstruction(OP_pop);
        result.addInstruction(OP_pushtrue);
        return result;
    }

    public InstructionList reduce_deleteExprExprExpr(IASNode iNode, InstructionList expr)
    {
        //  The expression must be run to allow for side effects.
        InstructionList result = createInstructionList(iNode, expr.size() + 2);
        result.addInstruction(OP_getlocal0);
        result.addAll(expr);
        result.addInstruction(arrayAccess(iNode, OP_deleteproperty));
        return result;
    }

    public InstructionList reduce_deleteMemberExpr(IASNode iNode, InstructionList stem, Binding field)
    {
        currentScope.getMethodBodySemanticChecker().checkDeleteExpr(iNode, field);

        InstructionList result = createInstructionList(iNode, stem.size() + 1);
        result.addAll(stem);
        result.addInstruction(OP_deleteproperty, field.getName());
        return result;
    }

    public InstructionList reduce_deleteRuntimeNameExpr(IASNode iNode, InstructionList stem, RuntimeMultiname rt_name)
    {
        //  TODO: Any relevant semantic checks?

        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);
        if ( rt_name.hasRuntimeQualifier() )
            result.addAll(rt_name.getRuntimeQualifier(iNode));
        if ( rt_name.hasRuntimeName() )
            result.addAll(rt_name.getRuntimeName(iNode));
        result.addInstruction(OP_deleteproperty, rt_name.generateName());
        return result;
    }

    public InstructionList reduce_deleteNameExpr(IASNode iNode, Binding n)
    {
        currentScope.getMethodBodySemanticChecker().checkDeleteExpr(iNode, n);

        InstructionList result = createInstructionList(iNode, 2);
        result.addAll(currentScope.findProperty(n, false));
        result.addInstruction(OP_deleteproperty, n.getName());
        return result;
    }

    public InstructionList reduce_doStmt(IASNode iNode, InstructionList body, InstructionList cond)
    {
        InstructionList result = createInstructionList(iNode, cond.size() + body.size() + 1);
        currentScope.getFlowManager().resolveContinueLabel(cond);

        if ( body.isEmpty() || body.firstElement().getOpcode() != OP_label ) 
            result.addInstruction(OP_label);

        result.addAll(body);
        result.addAll(cond);
        result.addInstruction(OP_iftrue, result.getLabel());

        currentScope.getFlowManager().finishLoopControlFlowContext(result);

        return result;
    }

    public InstructionList reduce_e4xFilter(IASNode iNode, InstructionList stem, InstructionList filter)
    {
        InstructionList result = createInstructionList(iNode);

        LexicalScope.Hasnext2Wrapper hasnext = currentScope.hasnext2();

        //  Cache each value of e.employee.
        Binding value_temp = currentScope.allocateTemp();
        //  Cache the result XMLList.
        Binding result_temp = currentScope.allocateTemp();

        //  Emit the stem expression and cache 
        //  the stem object in a local register.
        result.addAll(stem);
        result.addInstruction(OP_checkfilter);
        result.addInstruction(hasnext.stem_temp.setlocal() );

        //  Initialize the index register.
        result.addInstruction(OP_pushbyte, 0);
        result.addInstruction(hasnext.index_temp.setlocal());

        //  Create the result XMLList.
        result.addAll(currentScope.getPropertyValue(xmlListType, currentScope.getProject().getBuiltinType(BuiltinType.XMLLIST)));
        result.addInstruction(OP_pushstring, "");
        result.addInstruction(OP_construct, 1);
        result.addInstruction(result_temp.setlocal());

        //  Jump to the loop test.
        Label test = new Label();
        result.addInstruction(OP_jump, test);

        //  Top of the loop: put an AET Label on
        //  the ABC OP_Label.
        Label loop = new Label();
        result.labelNext(loop);
        result.addInstruction(OP_label);
        //  Get the next value and cache it.
        result.addInstruction(hasnext.stem_temp.getlocal());
        result.addInstruction(hasnext.index_temp.getlocal());
        result.addInstruction(OP_nextvalue);
        result.addInstruction(OP_dup);
        result.addInstruction(value_temp.setlocal() );
        //  See ECMA-357 11.2.4
        result.addInstruction(OP_pushwith);

        result.addAll(filter);

        Label no_match = new Label();
        result.addInstruction(OP_iffalse, no_match);

        result.addInstruction(result_temp.getlocal() );
        result.addInstruction(hasnext.index_temp.getlocal() );
        result.addInstruction(value_temp.getlocal() );

        result.addInstruction(arrayAccess(iNode, OP_setproperty));

        result.labelNext(no_match);
        //  See ECMA-357 11.2.4
        result.addInstruction(OP_popscope);
        result.labelNext(test);
        result.addInstruction(hasnext.instruction);
        result.addInstruction(OP_iftrue, loop);

        result.addInstruction(result_temp.getlocal());

        hasnext.release();
        currentScope.releaseTemp(value_temp);
        currentScope.releaseTemp(result_temp);

        return result;
    }

    public InstructionList reduce_embed(IASNode iNode)
    {
        String name = "";
        try
        {
            name = ((EmbedNode)iNode).getName(currentScope.getProject(), getProblems());
        }
        catch (InterruptedException e)
        {
            throw new CodegenInterruptedException(e);
        }

        // the parent of an embed node is always a variable
        final VariableNode variableNode = (VariableNode)iNode.getParent();
        final IVariableDefinition variableDef = (IVariableDefinition)variableNode.getDefinition();
        final ITypeDefinition variableType = variableDef.resolveType(currentScope.getProject());
        final String typeName = variableType.getQualifiedName();
        
        InstructionList result;
        if ("Class".equals(typeName))
        {
            result = createInstructionList(iNode);
            Name embedName = new Name(name);
            result.addInstruction(OP_getlex, embedName);
        }
        else if ("String".equals(typeName))
        {
            result = createInstructionList(iNode);
            result.addInstruction(OP_pushstring, name);
        }
        else
        {
            assert false : "this problem should have been caught already at EmbedNode construction time";
            result = null;
        }

        return result;
    }

    public InstructionList reduce_forKeyValueStmt(IASNode iNode, Binding it, InstructionList base, InstructionList body, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkLValue(iNode, it);

        InstructionList result = createInstructionList(iNode, body.size() + base.size() + 15);

        ForKeyValueLoopState fkv = new ForKeyValueLoopState();

        result.addAll(fkv.generatePrologue(iNode, base));

        result.addAll(
            generateAssignment(
                iNode,
                it,
                fkv.generateKeyOrValue(opcode)
            )
        );

        result.addAll(body);

        result.addAll(fkv.generateEpilogue());

        currentScope.getFlowManager().finishLoopControlFlowContext(result);

        return result;
    }

    /**
     * Reduce {@code for(it in base)} or {@code for each(it in base)} loop with
     * variable initializer.
     * 
     * @param iNode {@code ForLoopNode}
     * @param single_decl Instructions to initialize a loop variable.
     * @param base Instructions to create a loop base.
     * @param body For loop body instructions.
     * @param opcode Use {@code OP_nextname} for "for" loops; use
     * {@code OP_nextvalue} for "for each" loops.
     * @return Instructions generated for the "for" loop node.
     */
    public InstructionList reduce_forVarDeclInStmt(IASNode iNode, InstructionList single_decl, InstructionList base, InstructionList body, int opcode)
    {
        final InstructionList result = createInstructionList(iNode);
        result.addAll(single_decl);

        final VariableNode var_node = findIteratorVariable((ForLoopNode)iNode);
        final Binding it = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());

        currentScope.getMethodBodySemanticChecker().checkLValue(iNode, it);

        final ForKeyValueLoopState fkv = new ForKeyValueLoopState();
        result.addAll(fkv.generatePrologue(iNode, base));
        result.addAll(generateAssignment(iNode, it, fkv.generateKeyOrValue(opcode)));
        result.addAll(body);
        result.addAll(fkv.generateEpilogue());
        currentScope.getFlowManager().finishLoopControlFlowContext(result);
        return result;
    }

    /**
     * Find the iterator {@code VariableNode} in a {@code ForLoopNode}.
     * 
     * <pre>
     * for (var it : String = "hello" in array) {...}
     * for each (var it : String = "hello" in array) {...}
     * </pre>
     * 
     * @param for_loop_node "for" loop node.
     * @return {@code VariableNode} of the iterator variable.
     */
    private static VariableNode findIteratorVariable(final ForLoopNode for_loop_node)
    {
        assert for_loop_node != null : "'for' loop node can't be null";

        final IASNode conditionalStatement = for_loop_node.getConditionalsContainerNode().getChild(0);
        final BinaryOperatorInNode in_node = (BinaryOperatorInNode)conditionalStatement;
        final VariableExpressionNode var_expr_node = (VariableExpressionNode)in_node.getLeftOperandNode();
        final VariableNode var_node = (VariableNode)var_expr_node.getChild(0);
        return var_node;
    }

    /**
     * Reduce statement like:<br>
     * {@code for (a[it] in base) body;}
     * 
     * @param iNode Tree node for the for-in loop.
     * @param stem Instructions for creating a {@code DynamicAccessNode}.
     * @param index Instructions for initializing the index expression.
     * @param base Instructions for initializing the base object of the <code>in</code> expression.
     * @param body Instructions for initializing the body of the for-loop.
     * @param opcode Opcode for <code>nextname</code> or ,code>nextvalue</code> instruction.
     * @param is_super Flag indicating whether the stem of the array index expression is <code>super</code>.
     * @return Instructions for executing the {@code for-in} statement.
     */
    public InstructionList reduce_forKeyValueArrayStmt(IASNode iNode, InstructionList stem, InstructionList index, InstructionList base, InstructionList body, int opcode, boolean is_super)
    {
        IContainerNode conditionalStatementsNode = ((IForLoopNode)iNode).getConditionalsContainerNode();
        IBinaryOperatorNode inNode = (IBinaryOperatorNode)SemanticUtils.getNthChild(conditionalStatementsNode, 0);
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)inNode.getLeftOperandNode();
        
        ForKeyValueLoopState fkv = new ForKeyValueLoopState();

        InstructionList result = createInstructionList(iNode, body.size() + base.size() + 15);

        result.addAll(fkv.generatePrologue(iNode, base));

        if( is_super )
            result.addInstruction(OP_getlocal0);
        else
            result.addAll(stem);

        result.addAll(index);
        result.addAll(fkv.generateKeyOrValue(opcode));

        result.addInstruction(arrayAccess(arrayIndexNode, is_super ? OP_setsuper : OP_setproperty));

        result.addAll(body);

        result.addAll(fkv.generateEpilogue());

        currentScope.getFlowManager().finishLoopControlFlowContext(result);
        return result;
    }

    public InstructionList reduce_forKeyValueMemberStmt(IASNode iNode, InstructionList stem, Binding member, InstructionList base, InstructionList body, int opcode, boolean is_super)
    {
        currentScope.getMethodBodySemanticChecker().checkLValue(iNode, member);

        ForKeyValueLoopState fkv = new ForKeyValueLoopState();

        InstructionList result = createInstructionList(iNode, body.size() + base.size() + 15);

        result.addAll(fkv.generatePrologue(iNode, base));

        if( is_super )
            result.addInstruction(OP_getlocal0);
        else
            result.addAll(stem);

        result.addAll(fkv.generateKeyOrValue(opcode));

        generateAssignmentOp(iNode, member, result);

        result.addAll(body);

        result.addAll(fkv.generateEpilogue());

        currentScope.getFlowManager().finishLoopControlFlowContext(result);
        return result;
    }

    /**
     * Reduce expression like:<br>
     * {@code a[i]()}
     * 
     * @param iNode Tree node for the function call.
     * @param stem Instructions for creating a {@code DynamicAccessNode}.
     * @param index Instructions for initializing the index expression.
     * @return Instructions for executing a {@code function call} statement.
     */
    public InstructionList reduce_functionAsBracketExpr(IASNode iNode, InstructionList stem, InstructionList index, Vector<InstructionList> args)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IFunctionCallNode)iNode).getNameNode();
        
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        result.addInstruction(OP_dup);
        result.addAll(index);
        result.addInstruction(arrayAccess(arrayIndexNode, OP_getproperty));
        result.addInstruction(OP_swap);

        for ( InstructionList arg: args)
            result.addAll(arg);
        result.addInstruction(OP_call, args.size() );  //  note: args.size() is an immediate operand to OP_call
        return result;
    }

    public InstructionList reduce_functionAsMemberExpr(IASNode iNode, InstructionList stem, Binding method_name, Vector<InstructionList> args)
    {
        //  Perform general function call checks and specific member checks
        currentScope.getMethodBodySemanticChecker().checkFunctionCall(iNode, method_name, args);

        //  TODO: Investigate optimizing this.
        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);

        boolean inlined = generateInlineFunctionCall(method_name, result, true, args);

        if (!inlined)
        {
            for (InstructionList arg: args)
                result.addAll(arg);

            result.addInstruction(OP_callproperty, new Object[] {method_name.getName(), args.size() } );
        }

        return result;
    }

    public InstructionList reduce_functionAsRandomExpr(IASNode iNode, InstructionList random_expr, Vector<InstructionList> args)
    {
        //  TODO: Investigate optimizing this.
        InstructionList result = createInstructionList(iNode);

        result.addAll(random_expr);

        // TODO: currentScope.getThis() or sth similar
        result.addInstruction(OP_getlocal0);

        for( InstructionList arg: args )
            result.addAll(arg);

        result.addInstruction(OP_call, args.size());

        return result;
    }

    public InstructionList reduce_functionCallExpr_to_expression(IASNode iNode, Binding method_name, Vector<InstructionList> args)
    {
        return reduce_functionCall_common(iNode, method_name, args, true);
    }

    public InstructionList reduce_functionCallExpr_to_void_expression(IASNode iNode, Binding method_name, Vector<InstructionList> args)
    {
        return reduce_functionCall_common(iNode, method_name, args, false);
    }

    private InstructionList reduce_functionCall_common(IASNode iNode, Binding method_name, Vector<InstructionList> args, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkFunctionCall(iNode, method_name, args);

        InstructionList result = createInstructionList(iNode);

        if (method_name.isLocal())
        {
            result.addInstruction(method_name.getlocal());
            result.addInstruction(OP_getlocal0);
            for (InstructionList arg : args)
                result.addAll(arg);

            result.addInstruction(OP_call, args.size());
            if (!need_result)
            {
                result.addInstruction(OP_pop);
            }
        }
        else if ( SemanticUtils.isMethodBinding(method_name.getDefinition()) )
        {
            //  A call to a class' method.
            boolean inlined = generateInlineFunctionCall(method_name, result, false, args);

            if (!inlined)
            {
                result.addAll(currentScope.findProperty(method_name, true));
                for (InstructionList arg : args)
                    result.addAll(arg);

                int opcode = need_result ? OP_callproperty : OP_callpropvoid;
                result.addInstruction(opcode, new Object[] {method_name.getName(), args.size()});
            }
        }
        else if ( method_name.getDefinition() != null )
        {
            //  We're in some context where the method can be found lexically.
            assert ( ! method_name.getName().isRuntimeName() );

            boolean inlined = generateInlineGetterAccess(method_name, result, false);
            
            if (!inlined)
                result.addAll(currentScope.getPropertyValue(method_name));
            
            result.addInstruction(OP_getglobalscope);
            for (InstructionList arg : args)
                result.addAll(arg);

            result.addInstruction(OP_call, args.size());
            

            if ( ! need_result )
                result.addInstruction(OP_pop);
        }
        else
        {
            //  Probably in a with or filter block.
            result.addInstruction(OP_findpropstrict, method_name.getName());
            for (InstructionList arg : args)
                result.addAll(arg);

            int opcode = need_result ? OP_callproperty : OP_callpropvoid;
            result.addInstruction(opcode, new Object[] {method_name.getName(), args.size()});

        }

        return result;
    }

    public InstructionList reduce_functionCallOfSuperclassMethod_to_expression(IASNode iNode, InstructionList stem, Binding method_name, Vector<InstructionList> args)
    {
        currentScope.getMethodBodySemanticChecker().checkSuperAccess(iNode);
        if (method_name.getDefinition() instanceof AccessorDefinition)
        	getProblems().add(new CallNonFunctionProblem(iNode, method_name.getName().getBaseName()));

        InstructionList result = createInstructionList(iNode);

        if ( stem != null )
            result.addAll(stem);
        else
            //  Implicit stem is "this".
            result.addInstruction(OP_getlocal0);

        for ( InstructionList arg: args)
            result.addAll(arg);
        result.addInstruction(OP_callsuper, new Object[] {method_name.getName(), args.size() } );
        return result;
    }

    public InstructionList reduce_functionCallOfSuperclassMethod_to_void_expression(IASNode iNode, InstructionList stem, Binding method_name, Vector<InstructionList> args)
    {
        currentScope.getMethodBodySemanticChecker().checkSuperAccess(iNode);
        if (method_name.getDefinition() instanceof AccessorDefinition)
        	getProblems().add(new CallNonFunctionProblem(iNode, method_name.getName().getBaseName()));

        InstructionList result = createInstructionList(iNode);

        if ( stem != null )
            result.addAll(stem);
        else
            //  Implicit stem is "this"
            result.addInstruction(OP_getlocal0);

        for ( InstructionList arg: args)
            result.addAll(arg);
        result.addInstruction(OP_callsupervoid, new Object[] {method_name.getName(), args.size() } );
        return result;
    }

    public InstructionList reduce_groupedVoidExpression(IASNode iNode, Vector<InstructionList> contents)
    {
        InstructionList result = createInstructionList(iNode);

        for ( InstructionList expr: contents )
            result.addAll(expr);

        return result;
    }

    public InstructionList reduce_ifElseIf(IASNode iNode, InstructionList test, InstructionList then, Vector<ConditionalFragment> if_elseif)
    {
        // need to convert any constant values back
        // to a conditional expression when not generating
        // a lookup switch.
        // TODO: CMP-1762 make use of any constant values to optimize away
        // constant if/else ifs.
        convertConstantValueConditionsToInstructions(if_elseif);
        
        //  TODO: Experiment with optimizing this.
        //  Iterating twice over the arms of the if
        //  might cost more than it helps.
        InstructionList result = createInstructionList(iNode);

        boolean has_else = !if_elseif.isEmpty();

        //  Label to the "next statement."
        Label tail = null;

        //  TODO: Use conditionalJump style tests.
        result.addAll(test);

        if ( has_else )
            result.addInstruction(OP_iffalse, if_elseif.firstElement().getLabel());
        else
            tail = addBranch(result, tail, OP_iffalse);

        result.addAll(then);

        if ( has_else )
        {
            tail = addInterstitialControlFlow(result, tail);

            for ( int i = 0; i < if_elseif.size() - 1; i++ )
            {
                ConditionalFragment alternative = if_elseif.elementAt(i);

                if ( !alternative.isUnconditionalAlternative() )
                {
                    //  Add the test and conditional jump to the next alternative.
                    result.addAll(alternative.condition);
                    ConditionalFragment next_alternative = if_elseif.elementAt(i+1);
                    result.addInstruction(OP_iffalse, next_alternative.getLabel());
                }

                result.addAll(alternative.statement);
                tail = addInterstitialControlFlow(result, tail);
            }

            ConditionalFragment last_clause = if_elseif.lastElement();

            if ( !last_clause.isUnconditionalAlternative() )
            {
                result.addAll(last_clause.condition);
                tail = addBranch(result, tail, OP_iffalse);
                result.addAll(last_clause.statement);
            }
            else
            {
                result.addAll(last_clause.statement);
            }
        }

        if ( tail != null )
            result.labelNext(tail);
        return result;
    }

    /**
     *  Add a branch instruction to an InstructionList.
     *  @param insns - the InstructionList.
     *  @param previousLabel - any existing label that targets the jump destination.
     *  @param opcode - the opcode of the branch (e.g., OP_jump, OP_iffalse, etc.)
     *  @return a label that targets the jump destination; if previousLabel was
     *    not null, then previousLabel, otherwise a new Label.  The caller is
     *    responsible for saving this label and applying it appropriately at
     *    the destination point.
     */
    private Label addBranch(InstructionList insns, Label previousLabel, int opcode)
    {
        Label result = previousLabel != null? previousLabel: new Label();
        insns.addInstruction(opcode, result);
        return result;
    }

    /**
     *  Add jumps from one bit of a control-flow statement to the next,
     *  if they're necessary.
     *  @param insns - the InstructionList that's building up the statement.
     *  @param previousLabel - any existing Label that targets the next part of
     *    the statement.  May be null if this is the first interstitial jump.
     *  @return previousLabel if previousLabel was not null;
     *    null if previousLabel was null and no jump is necessary; 
     *    otherwise a new Label that the caller must use to label the next
     *    part of the statement.
     */
    private Label addInterstitialControlFlow(InstructionList insns, Label previousLabel)
    {
        Label result;

        if ( insns.canFallThrough() )
        {
            result = addBranch(insns, previousLabel, OP_jump);
        }
        else if ( insns.hasPendingLabels() )
        {
            //  TODO: InstructionList needs a way to "alias"
            //  labels; with such a facility, this jump could be elided.
            result = addBranch(insns, previousLabel, OP_jump);
        }
        else
        {
            result = previousLabel;
        }

        return result;
    }

    public InstructionList reduce_importDirective(IASNode iNode)
    {
        currentScope.getMethodBodySemanticChecker().checkImportDirective(((IImportNode)iNode));
        return createInstructionList(iNode);
    }

    public InstructionList reduce_labeledBreakStmt(IASNode iNode)
    {
        String target = ((IdentifierNode)SemanticUtils.getNthChild(iNode, 0)).getName();
        try
        {
            ControlFlowContextManager.ControlFlowContextSearchCriteria criterion = getBreakCriteria(target);
            return getNonLocalControlFlow(criterion);
        }
        catch ( UnknownControlFlowTargetException no_target )
        {
            currentScope.addProblem(new UnknownBreakTargetProblem(SemanticUtils.getNthChild(iNode, 0)));
            return createInstructionList(iNode);
        }
    }

    public InstructionList reduce_labeledContinueStmt(IASNode iNode)
    {
        String target = ((IdentifierNode)SemanticUtils.getNthChild(iNode, 0)).getName();
        try
        {
            ControlFlowContextManager.ControlFlowContextSearchCriteria criterion = getContinueCriteria(target);
            return getNonLocalControlFlow(criterion);
        }
        catch ( UnknownControlFlowTargetException no_target )
        {
            currentScope.addProblem(new UnknownContinueTargetProblem(SemanticUtils.getNthChild(iNode, 0)));
            return createInstructionList(iNode);
        }
    }
    
    public InstructionList reduce_gotoStmt(IASNode iNode)
    {
        IdentifierNode labelIdentifierNode = (IdentifierNode)SemanticUtils.getNthChild(iNode, 0);
        String target = labelIdentifierNode.getName();
        try
        {
            ControlFlowContextManager.ControlFlowContextSearchCriteria criterion = getGotoCriteria(target);
            return getNonLocalControlFlow(criterion);
        }
        catch ( UnknownControlFlowTargetException no_target )
        {
            Collection<LabeledStatementNode> gotoLabels = currentScope.getFlowManager().getGotoLabels(target);
            if (gotoLabels.isEmpty())
                currentScope.addProblem(new UnknownGotoTargetProblem(labelIdentifierNode));
            else
                currentScope.addProblem(new AmbiguousGotoTargetProblem(labelIdentifierNode, gotoLabels));
            return createInstructionList(iNode);
        }
    }

    public InstructionList reduce_labeledStmt(IASNode iNode, String label, InstructionList substatement)
    {
        InstructionList result = createInstructionList(iNode, 1);
        Label gotoLabel = currentScope.getFlowManager().getGotoLabel(label);
        // Since duplicate labels are invisible to goto statements,
        // we can fail to find a goto context for a label.
        if (gotoLabel != null)
        {
            result.labelNext(gotoLabel);
            result.addInstruction(OP_label);
        }
        
        result.addAll(substatement);

        currentScope.getFlowManager().finishLabeledStatementControlFlowContext(result);
        return result;
    }

    public ConditionalFragment reduce_lastElse(IASNode iNode, InstructionList else_clause)
    {
        return new ConditionalFragment(iNode, null, else_clause);
    }

    public InstructionList reduce_logicalAndExpr(IASNode iNode, InstructionList l, InstructionList r)
    {
        InstructionList result = createInstructionList(iNode, l.size() + r.size() + 3);
        Label tail = new Label();

        result.addAll(l);
        result.addInstruction(OP_dup);
        result.addInstruction(OP_iffalse, tail);
        result.addInstruction(OP_pop);
        result.addAll(r);
        result.labelNext(tail);
        return result;
    }

    public InstructionList reduce_logicalNotExpr(IASNode iNode, InstructionList expr)
    {
        InstructionList result = createInstructionList(iNode, expr.size() + 1);
        result.addAll(expr);
        result.addInstruction(OP_not);
        return result;
    }

    public InstructionList reduce_logicalOrExpr(IASNode iNode, InstructionList l, InstructionList r)
    {
        InstructionList result = createInstructionList(iNode, l.size() + r.size() + 3);
        Label tail = new Label();

        result.addAll(l);
        result.addInstruction(OP_dup);
        result.addInstruction(OP_iftrue, tail);
        result.addInstruction(OP_pop);
        result.addAll(r);
        result.labelNext(tail);
        return result;
    }

    public InstructionList reduce_memberAccessExpr(IASNode iNode, InstructionList stem, Binding member, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkMemberAccess(iNode, member, opcode);
        InstructionList result = createInstructionList(iNode, stem.size() + 1);

        result.addAll(stem);

        boolean inlined = generateInlineGetterAccess(member, result, true);

        if (!inlined)
            result.addInstruction(opcode, member.getName());

        return result;
    }

    public InstructionList reduce_qualifiedMemberAccessExpr(IASNode iNode, InstructionList stem, Binding qualifier, Binding member, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkMemberAccess(iNode, member, opcode);
        InstructionList result = createInstructionList(iNode);
        Name member_name = member.getName();

        result.addAll(stem);
        if ( isNamespace(qualifier) )
        {
            result.addInstruction(opcode, member_name);
        }
        else
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
            result.addInstruction(opcode, member_name);
        }
        return result;
    }

    public InstructionList reduce_qualifiedAttributeRuntimeMemberExpr(IASNode iNode, InstructionList stem, Binding qualifier, InstructionList runtime_member_selector, int opcode)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        if ( isNamespace(qualifier) )
        {
            result.addAll(runtime_member_selector);
            //  Extract the URI from the namespace and use it to construct a qualified name.
            NamespaceDefinition ns_def = (NamespaceDefinition)qualifier.getDefinition();
            Name qualified_name = new Name(CONSTANT_MultinameLA, new Nsset(ns_def.resolveAETNamespace(currentScope.getProject())), null);
            result.addInstruction(opcode, qualified_name);
        }
        else
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
            result.addAll(runtime_member_selector);
            result.addInstruction(OP_coerce_s);
            result.addInstruction(opcode, new Name(CONSTANT_RTQnameLA, null, null));
        }
        return result;
    }

    public InstructionList reduce_qualifiedMemberRuntimeNameExpr(IASNode iNode, InstructionList stem, Binding qualifier, InstructionList runtime_member_selector)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        if ( isNamespace(qualifier) )
        {
            result.addAll(runtime_member_selector);
            //  Extract the URI from the namespace and use it to construct a qualified name.
            NamespaceDefinition ns_def = (NamespaceDefinition)qualifier.getDefinition();
            Name qualified_name = new Name(CONSTANT_MultinameL, new Nsset(ns_def.resolveAETNamespace(currentScope.getProject())), null);
            result.addInstruction(OP_getproperty, qualified_name);
        }
        else
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
            result.addAll(runtime_member_selector);
            result.addInstruction(OP_coerce_s);
            result.addInstruction(OP_getproperty, new Name(CONSTANT_RTQnameL, null, null));
        }
        return result;
    }

    public InstructionList reduce_qualifiedAttributeExpr(IASNode iNode, InstructionList stem, Binding qualifier, Binding member, int opcode)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        if ( !isNamespace(qualifier) )
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
            result.addInstruction(opcode, member.getName());
        }
        else
        {
            // the qualifier's namespace is already present in the name.
            result.addInstruction(opcode, member.getName());
        }

        return result;
    }

    public InstructionList reduce_unqualifiedAttributeExpr(IASNode iNode, InstructionList stem, InstructionList rt_attr, int opcode)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);
        result.addAll(rt_attr);
        result.addInstruction(opcode, new Name(CONSTANT_MultinameLA, new Nsset(new Namespace(CONSTANT_PackageNs)), null));

        return result;
    }
    
    /**
     * Reduce runtime attribute expression, such as {@code @[exp]}.
     * 
     * @param iNode Node for {@code @[...]}. It is a
     * {@code ArrayIndexExpressionID(Op_AtID, ...)}.
     * @param rt_attr Instructions generated for the runtime name expression.
     * @return Instructions to get the value of an attribute described with a
     * runtime name.
     */
    public InstructionList reduce_runtimeAttributeExp(IASNode iNode, InstructionList rt_attr)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(rt_attr);
        final Nsset qualifiers = SemanticUtils.getOpenNamespaces(iNode, currentScope.getProject());
        result.addInstruction(OP_getproperty, new Name(CONSTANT_MultinameLA, qualifiers, null));
        return result;
    }

    public InstructionList reduce_mxmlEventSpecifier(IASNode iNode, Vector<InstructionList> stmts)
    {
        //  TODO: Investigate optimizing.
        InstructionList body = createInstructionList(iNode);

        // An MXMLEventSpecifierNode can have N child nodes which are statements.
        // We need to codegen all of them as an effective method body.
        for ( InstructionList stmt: stmts )
            body.addAll(stmt);

        return generateFunctionBody(iNode, body, LexicalScope.anyType);
    }

    public Binding reduce_namespaceAccess(IASNode iNode, IASNode qualifier, Binding qualified_name)
    {
        currentScope.getMethodBodySemanticChecker().checkQualifier(qualifier);
        //  All the qualifying syntax nodes are just concrete.
        return qualified_name;
    }

    public RuntimeMultiname reduce_namespaceAccess_to_runtime_name(IASNode iNode, Binding qualified_name)
    {
        InstructionList qualifier = createInstructionList(iNode);
        generateAccess(reduce_simpleName(iNode.getChild(0)), qualifier);
        qualifier.addInstruction(OP_coerce, namespaceType);
        return new RuntimeMultiname(qualifier, qualified_name.getName());
    }

    public InstructionList reduce_namespaceAsName_to_expression(IASNode iNode)
    {
        InstructionList result = createInstructionList(iNode);
        Binding b = reduce_simpleName(iNode);
        generateAccess(b, result);
        result.addInstruction(OP_coerce, namespaceType);
        return result;
    }

    public Name reduce_namespaceAsName_to_multinameL(IASNode iNode)
    {
        return reduce_namespaceAsName_to_multinameL(iNode, false);
    }

    public Name reduce_namespaceAsName_to_multinameL(IASNode iNode, final boolean is_attribute)
    {
        IdentifierNode qualifier = (IdentifierNode)iNode;
        NamespaceDefinition ns = (NamespaceDefinition) qualifier.resolve(currentScope.getProject());

        int name_kind = is_attribute? CONSTANT_MultinameLA: CONSTANT_MultinameL;
        return new Name(name_kind, new Nsset( ns.resolveAETNamespace(currentScope.getProject())), null);
    }


    public Binding reduce_namespaceAsName_to_name(IASNode iNode)
    {
        //  Note: unresolved namespaces are trapped by a bespoke rule.
        return currentScope.resolveName((org.apache.royale.compiler.internal.tree.as.IdentifierNode)iNode);
    }

    public InstructionList reduce_namespaceDeclaration(IASNode iNode, Binding ns_name)
    {
        //  Get the AET Namespace from the namespace's definition.
        NamespaceDefinition def = ((NamespaceNode)iNode).getDefinition();
        return reduce_namespaceDeclarationConstantInitializer(iNode, ns_name, def.resolveAETNamespace(currentScope.getProject()));
    }

    public InstructionList reduce_namespaceDeclarationConstantInitializer(IASNode iNode, Binding ns_name, String uri)
    {
        // Make a new CONSTANT_Namespace with the given URI - all user defined namespaces initialized with a
        // String literal become CONSTANT_Namespaces
        return reduce_namespaceDeclarationConstantInitializer(iNode, ns_name, new Namespace(CONSTANT_Namespace, uri));
    }

    /**
     * Does the work for the various reduce_namespaceDecl methods.  Creates a new property, with an initial value
     * of the Namespace passed in.
     */
    public InstructionList reduce_namespaceDeclarationConstantInitializer(IASNode iNode, Binding ns_name, Namespace initializer)
    {
        currentScope.getMethodBodySemanticChecker().checkNamespaceDeclaration(iNode, ns_name);

        NamespaceDefinition def = ((NamespaceNode)iNode).getDefinition();

        switch (SemanticUtils.getMultiDefinitionType(def, currentScope.getProject()))
        {
            case AMBIGUOUS:
                currentScope.addProblem(new DuplicateNamespaceDefinitionProblem(iNode));
                break;
            case NONE:
                break;
            default:
                assert false;       // I don't think namespaces can have other type of multiple definitions
        }
        
        if( initializer == null )
        {
            // Can't declare a namespace if we don't know what its initial value is
            getProblems().add( new InvalidNamespaceInitializerProblem(iNode) );
            return null;
        }

        currentScope.makeNamespace(ns_name, initializer);
        if ( ns_name.isLocal() )
        {
            //  Locals have no initializer, so the variable
            //  needs explicit initialization code.
            currentScope.getHoistedInitInstructions().addInstruction(OP_pushnamespace, initializer);
            currentScope.getHoistedInitInstructions().addInstruction(ns_name.setlocal());

        }
        return null;
    }

    public InstructionList reduce_namespaceDeclarationInitializer(IASNode iNode, Binding ns_name, Binding second_ns)
    {
        Namespace ns_init = null;

        if ( second_ns.getDefinition() instanceof NamespaceDefinition )
        {
            NamespaceDefinition ns_def = (NamespaceDefinition)second_ns.getDefinition();
            // Grab the AET namespace from the initializer
            ns_init = ns_def.resolveAETNamespace(currentScope.getProject());
        }

        return reduce_namespaceDeclarationConstantInitializer(iNode, ns_name, ns_init);
    }

    public RuntimeMultiname reduce_namespaceMultinameL(IASNode iNode, IASNode qualifier_node, InstructionList expr)
    {
        currentScope.getMethodBodySemanticChecker().checkQualifier(qualifier_node);
        Name qualifier = reduce_namespaceAsName_to_multinameL(qualifier_node);
        return new RuntimeMultiname(qualifier, expr);
    }

    public RuntimeMultiname reduce_namespaceRTQName(IASNode iNode, InstructionList qualifier, Binding qualified_name)
    {
        return new RuntimeMultiname(qualifier, qualified_name.getName());
    }

    public RuntimeMultiname reduce_namespaceRTQNameL(IASNode iNode, InstructionList qualifier, InstructionList expr)
    {
        return new RuntimeMultiname(qualifier, expr);
    }

    public InstructionList reduce_neqExpr(IASNode iNode, InstructionList l, InstructionList r)
    {
        InstructionList result = binaryOp(iNode, l, r, OP_equals);
        result.addInstruction(OP_not);
        return result;
    }

    public Binding reduce_nameToTypeName(Binding name, boolean check_name)
    {
        if ( check_name )
            currentScope.getMethodBodySemanticChecker().checkTypeName(name);
        return name;
    }

    public InstructionList reduce_newMemberProperty(IASNode iNode, InstructionList stem, Binding member, Vector<InstructionList> args)
    {
        currentScope.getMethodBodySemanticChecker().checkNewExpr(iNode);

        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);

        for ( InstructionList arg: args)
            result.addAll(arg);

        result.addInstruction(OP_constructprop, new Object[] { member.getName(), args.size() } );

        return result;
    }

    public InstructionList reduce_newAsRandomExpr(IASNode iNode, InstructionList random_expr, Vector<InstructionList> args)
    {
        currentScope.getMethodBodySemanticChecker().checkNewExpr(iNode);

        InstructionList result = createInstructionList(iNode);

        result.addAll(random_expr);

        for ( InstructionList arg: args)
            result.addAll(arg);

        result.addInstruction(OP_construct, args.size() );

        return result;
    }

    public InstructionList reduce_newEmptyArray(IASNode iNode)
    {
        // Codegen 'new Array()' efficiently like an array literal
        // rather than like a constructor call. That is,
        //   newarray [0]
        // rather than
        //   findpropstrict :Array
        //   constructprop :Array, (0)
        InstructionList result = createInstructionList(iNode);
        result.addInstruction(OP_newarray, 0);
        return result;
    }

    public InstructionList reduce_newEmptyObject(IASNode iNode)
    {
        // Codegen 'new Object()' efficiently like an object literal
        // rather than like a constructor call. That is,
        //   newobject {0}
        // rather than
        //   findpropstrict :Object
        //   constructprop :Object, (0)
        InstructionList result = createInstructionList(iNode);
        result.addInstruction(OP_newobject, 0);
        return result;
    }

    public InstructionList reduce_newExpr(IASNode iNode, Binding class_binding, Vector<InstructionList> args)
    {
        currentScope.getMethodBodySemanticChecker().checkNewExpr(iNode, class_binding, args);

        //  TODO: Investigate optimizing this.
        InstructionList result = createInstructionList(iNode);

        Name class_name = class_binding.getName();

        if ( class_binding.isLocal() || class_name.isTypeName() )
        {
            generateAccess(class_binding, result);

            for ( InstructionList arg: args)
                result.addAll(arg);

            result.addInstruction(OP_construct, args.size());
        }
        else
        {
            result.addAll(currentScope.findProperty(class_binding, true));

            for ( InstructionList arg: args)
                result.addAll(arg);

            result.addInstruction(OP_constructprop, new Object[] { class_name, args.size() } );
        }

        return result;
    }

    public InstructionList reduce_newVectorLiteral(IASNode iNode, InstructionList literal)
    {
        return literal;
    }

    public InstructionList reduce_nilExpr_to_conditionalJump(IASNode iNode)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addInstruction(InstructionFactory.getTargetableInstruction(OP_jump));
        return result;
    }

    public InstructionList reduce_nilExpr_to_expression(IASNode iNode)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addInstruction(OP_pushundefined);
        return result;
    }

    public Object reduce_nullLiteral_to_constant_value(IASNode iNode)
    {
        return ABCConstants.NULL_VALUE;
    }

    public InstructionList reduce_nullLiteral_to_object_literal(IASNode iNode)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addInstruction(OP_pushnull);
        return result;
    }

    public InstructionList reduce_objectLiteral(IASNode iNode, Vector<InstructionList> elements)
    {
        //  TODO: Investigate optimizing.
        InstructionList result = createInstructionList(iNode);

        for ( InstructionList element: elements )
        {
            result.addAll(element);
        }

        result.addInstruction(OP_newobject, elements.size());

        return result;
    }

    public InstructionList reduce_objectLiteralElement(IASNode iNode, InstructionList id, InstructionList value)
    {
        InstructionList result = createInstructionList(iNode, value.size() + 1);
        result.addAll(id);
        //  TODO: Push type analysis up through the CG,
        //  so that string constants don't go through
        //  convert_s.  See http://bugs.adobe.com/jira/browse/CMP-118
        result.addInstruction(OP_convert_s);
        result.addAll(value);
        return result;
    }

    public InstructionList reduce_optionalParameter(IASNode iNode, Name param_name, Binding param_type, Object raw_default_value)
    {
        IParameterNode parameter_node = (IParameterNode)iNode;
        PooledValue transformed_default_value;
        // raw_default_value will be null if the parameter's default value was not a constant expression.
        if( raw_default_value == null )
        {
            currentScope.addProblem(new NonConstantParamInitializerProblem(parameter_node.getAssignedValueNode()));
            // re-write non-constant expression to undefined, so resulting ABC will pass the verifier.
            transformed_default_value = new PooledValue(ABCConstants.UNDEFINED_VALUE);
        }
        else
        {
            PooledValue default_value = new PooledValue(raw_default_value);
            transformed_default_value =
                currentScope.getMethodBodySemanticChecker().checkInitialValue(parameter_node, param_type, default_value);
        }
        currentScope.makeParameter(getParameterContent(iNode), param_type.getName());
        currentScope.addDefaultValue(transformed_default_value);
        return null;
    }

    public Binding reduce_parameterizedName(IASNode iNode, Binding base, Binding param)
    {
        Name param_name = param.getName();

        return new Binding(iNode, new Name(base.getName(), param_name), null);
    }

    public InstructionList reduce_parameterizedTypeExpression(IASNode iNode, InstructionList base, InstructionList param)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(base);
        result.addAll(param);
        result.addInstruction(OP_applytype, 1);

        return result;
    }

    public InstructionList reduce_plist(IASNode iNode, Vector<InstructionList> pdecl)
    {
        //  Parameter processing is done by side effect
        //  in the parameter reduction.
        return null;
    }

    public InstructionList reduce_postDecBracketExpr(IASNode iNode, InstructionList stem, InstructionList index, boolean need_result)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IUnaryOperatorNode)iNode).getOperandNode();

        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false);
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        Binding stem_tmp = currentScope.allocateTemp();
        result.addInstruction(OP_dup);
        result.addInstruction(stem_tmp.setlocal() );

        result.addAll(index);
        result.addInstruction(OP_dup);

        //  Get the initial value, and dup a copy
        //  onto the stack as the value of this expression.
        Binding index_tmp = currentScope.allocateTemp();
        result.addInstruction(index_tmp.setlocal() );
        result.addInstruction(arrayAccess(arrayIndexNode, OP_getproperty));
        result.addInstruction(op_unplus());
        
        if( need_result )
            result.addInstruction(OP_dup);

        result.addInstruction(OP_decrement);
        Binding result_tmp = currentScope.allocateTemp();
        result.addInstruction(result_tmp.setlocal() );

        result.addInstruction(stem_tmp.getlocal() );
        result.addInstruction(index_tmp.getlocal() );
        result.addInstruction(result_tmp.getlocal() );
        result.addInstruction(arrayAccess(arrayIndexNode, OP_setproperty));

        currentScope.releaseTemp(stem_tmp);
        currentScope.releaseTemp(index_tmp);
        currentScope.releaseTemp(result_tmp);

        return result;
    }


    public InstructionList reduce_postDecMemberExpr(IASNode iNode, InstructionList stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false, field);
        
        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);
        result.addInstruction(OP_dup);
        result.addInstruction(OP_getproperty, field.getName());
        result.addInstruction(op_unplus());

        //  Save a copy of the original value to use as the value of this expression.
        Binding orig_tmp = null;
        if( need_result )
        {
            result.addInstruction(OP_dup);
            orig_tmp = currentScope.allocateTemp();
            result.addInstruction(orig_tmp.setlocal() );
        }

        result.addInstruction(OP_decrement);

        generateAssignmentOp(iNode, field, result);

        if( need_result )
        {
            result.addInstruction(orig_tmp.getlocal() );
            currentScope.releaseTemp(orig_tmp);
        }

        return result;
    }

    public InstructionList reduce_postDecNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false, unary);

        InstructionList result = createInstructionList(iNode);

        if ( unary.isLocal() ) {
            ICompilerProject project = currentScope.getProject();

            IDefinition inType = SemanticUtils.resolveUnaryExprType(iNode, project);

            IDefinition intType = project.getBuiltinType(BuiltinType.INT);
            IDefinition numberType = project.getBuiltinType(BuiltinType.NUMBER);
            if( inType == intType)
            {
                // Decrementing a local, typed as int
                // we can use declocal_i since we know the result will be stored in an int
                if( need_result )
                    result.addInstruction(unary.getlocal());
                result.addInstruction(unary.declocal_i());
            }
            else if( inType == numberType)
            {
                // Decrementing a local, typed as Number
                // we can use declocal since we know the result will be stored in a Number
                if( need_result )
                    result.addInstruction(unary.getlocal());
                result.addInstruction(unary.declocal());
            }
            else
            {
                result.addInstruction(unary.getlocal());
                result.addInstruction(op_unplus());
                if( need_result )
                    result.addInstruction(OP_dup);
                result.addInstruction(OP_decrement);
                coerce(result, inType);
                result.addInstruction(unary.setlocal());
            }
        }
        else
        {
            Name n = unary.getName();
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_getproperty,n);
            result.addInstruction(op_unplus());
            if( need_result )
                result.addInstruction(OP_dup);
            result.addInstruction(OP_decrement);
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_swap);
            result.addInstruction(OP_setproperty,n);
        }

        return result;
    }

    public InstructionList reduce_postIncBracketExpr(IASNode iNode, InstructionList stem, InstructionList index, boolean need_result)
    {        
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IUnaryOperatorNode)iNode).getOperandNode();

        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true);
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        result.addInstruction(OP_dup);
        Binding stem_tmp = currentScope.allocateTemp();
        result.addInstruction(stem_tmp.setlocal() );

        result.addAll(index);
        result.addInstruction(OP_dup);

        //  Get the initial value, and dup a copy
        //  onto the stack as the value of this expression.
        Binding index_tmp = currentScope.allocateTemp();
        result.addInstruction(index_tmp.setlocal() );
        result.addInstruction(arrayAccess(arrayIndexNode, OP_getproperty));
        result.addInstruction(op_unplus());
        
        if( need_result )
            result.addInstruction(OP_dup);

        result.addInstruction(OP_increment);
        Binding result_tmp = currentScope.allocateTemp();
        result.addInstruction(result_tmp.setlocal());

        result.addInstruction(stem_tmp.getlocal() );
        result.addInstruction(index_tmp.getlocal() );
        result.addInstruction(result_tmp.getlocal() );
        result.addInstruction(arrayAccess(arrayIndexNode, OP_setproperty));

        currentScope.releaseTemp(stem_tmp);
        currentScope.releaseTemp(index_tmp);
        currentScope.releaseTemp(result_tmp);

        return result;
    }

    public InstructionList reduce_postIncMemberExpr(IASNode iNode, InstructionList stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true, field);

        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);
        result.addInstruction(OP_dup);
        result.addInstruction(OP_getproperty, field.getName());
        result.addInstruction(op_unplus());

        //  Save a copy of the original value to use as the value of this expression.
        Binding orig_tmp = null;
        if( need_result )
        {
            result.addInstruction(OP_dup);
            orig_tmp = currentScope.allocateTemp();
            result.addInstruction(orig_tmp.setlocal() );
        }
        
        result.addInstruction(OP_increment);
        result.addInstruction(OP_setproperty, field.getName());

        if( need_result )
        {
            result.addInstruction(orig_tmp.getlocal() );
            currentScope.releaseTemp(orig_tmp);
        }

        return result;
    }

    public InstructionList reduce_postIncNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true, unary);

        InstructionList result = createInstructionList(iNode);

        if ( unary.isLocal() ) {
            ICompilerProject project = currentScope.getProject();

            IDefinition inType = SemanticUtils.resolveUnaryExprType(iNode, project);

            IDefinition intType = project.getBuiltinType(BuiltinType.INT);
            IDefinition numberType = project.getBuiltinType(BuiltinType.NUMBER);
            if( inType == intType)
            {
                // Incrementing a local, typed as int
                // we can use inclocal_i since we know the result will be stored in an int
                if( need_result )
                    result.addInstruction(unary.getlocal());
                result.addInstruction(unary.inclocal_i());
            }
            else if( inType == numberType)
            {
                // Incrementing a local, typed as Number
                // we can use inclocal since we know the result will be stored in a Number
                if( need_result )
                    result.addInstruction(unary.getlocal());
                result.addInstruction(unary.inclocal());
            }
            else
            {
                result.addInstruction(unary.getlocal());
                result.addInstruction(op_unplus());
                if( need_result )
                    result.addInstruction(OP_dup);
                result.addInstruction(OP_increment);
                coerce(result, inType);
                result.addInstruction(unary.setlocal());
            }
        }
        else
        {
            Name n = unary.getName();
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_getproperty,n);
            result.addInstruction(op_unplus());
            if( need_result )
                result.addInstruction(OP_dup);
            result.addInstruction(OP_increment);
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_swap);
            result.addInstruction(OP_setproperty,n);
        }

        return result;
    }

    public InstructionList reduce_preDecBracketExpr(IASNode iNode, InstructionList stem, InstructionList index, boolean need_result)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IUnaryOperatorNode)iNode).getOperandNode();

        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false);
        
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        Binding stem_tmp = currentScope.allocateTemp();
        result.addInstruction(OP_dup);
        result.addInstruction(stem_tmp.setlocal() );

        result.addAll(index);
        result.addInstruction(OP_dup);

        //  Get the initial value.
        Binding index_tmp = currentScope.allocateTemp();
        result.addInstruction(index_tmp.setlocal());
        result.addInstruction(arrayAccess(arrayIndexNode, OP_getproperty));

        //  Increment, and dup a copy onto 
        //  the stack as the value of this expression.
        result.addInstruction(OP_decrement);
        if( need_result )
            result.addInstruction(OP_dup);
        Binding result_tmp = currentScope.allocateTemp();
        result.addInstruction(result_tmp.setlocal());

        result.addInstruction(stem_tmp.getlocal() );
        result.addInstruction(index_tmp.getlocal() );
        result.addInstruction(result_tmp.getlocal() );
        result.addInstruction(arrayAccess(arrayIndexNode, OP_setproperty));

        currentScope.releaseTemp(stem_tmp);
        currentScope.releaseTemp(index_tmp);
        currentScope.releaseTemp(result_tmp);

        return result;
    }

    public InstructionList reduce_preDecMemberExpr(IASNode iNode, InstructionList stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false, field);
        
        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);
        result.addInstruction(OP_dup);
        result.addInstruction(OP_getproperty, field.getName());
        result.addInstruction(OP_decrement);

        Binding result_tmp = null;
        //  Save a copy of the result to use as the value of this expression.
        if( need_result )
        {
            result.addInstruction(OP_dup);
            result_tmp = currentScope.allocateTemp();
            result.addInstruction(result_tmp.setlocal() );
        }

        result.addInstruction(OP_setproperty, field.getName());

        if( need_result )
        {
            result.addInstruction(result_tmp.getlocal() );
            currentScope.releaseTemp(result_tmp);
        }
        
        return result;
    }

    public InstructionList reduce_preDecNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false, unary);

        InstructionList result = createInstructionList(iNode);

        if ( unary.isLocal() ) {
            ICompilerProject project = currentScope.getProject();

            IDefinition inType = SemanticUtils.resolveUnaryExprType(iNode, project);

            IDefinition intType = project.getBuiltinType(BuiltinType.INT);
            IDefinition numberType = project.getBuiltinType(BuiltinType.NUMBER);
            if( inType == intType)
            {
                // Decrementing a local, typed as int
                // we can use declocal_i since we know the result will be stored in an int
                result.addInstruction(unary.declocal_i());
                if( need_result )
                    result.addInstruction(unary.getlocal());
            }
            else if( inType == numberType)
            {
                // Decrementing a local, typed as Number
                // we can use declocal since we know the result will be stored in a Number
                result.addInstruction(unary.declocal());
                if( need_result )
                    result.addInstruction(unary.getlocal());
            }
            else
            {
                result.addInstruction(unary.getlocal());
                result.addInstruction(OP_decrement);
                if( need_result )
                    result.addInstruction(OP_dup);
                coerce(result, inType);
                result.addInstruction(unary.setlocal());
            }
        }
        else
        {
            Name n = unary.getName();
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_getproperty,n);
            result.addInstruction(OP_decrement);
            if( need_result )
                result.addInstruction(OP_dup);
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_swap);
            result.addInstruction(OP_setproperty,n);
        }

        return result;
    }

    public InstructionList reduce_preIncBracketExpr(IASNode iNode, InstructionList stem, InstructionList index, boolean need_result)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)((IUnaryOperatorNode)iNode).getOperandNode();

        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true);
        InstructionList result = createInstructionList(iNode);

        result.addAll(stem);
        Binding stem_tmp = currentScope.allocateTemp();
        result.addInstruction(OP_dup);
        result.addInstruction(stem_tmp.setlocal() );

        result.addAll(index);
        result.addInstruction(OP_dup);

        //  Get the initial value.
        Binding index_tmp = currentScope.allocateTemp();
        result.addInstruction(index_tmp.setlocal() );
        result.addInstruction(arrayAccess(arrayIndexNode, OP_getproperty));

        //  Increment, and dup a copy onto 
        //  the stack as the value of this expression.
        result.addInstruction(OP_increment);
        if( need_result )
            result.addInstruction(OP_dup);
        Binding result_tmp = currentScope.allocateTemp();
        result.addInstruction(result_tmp.setlocal() );

        result.addInstruction(stem_tmp.getlocal() );
        result.addInstruction(index_tmp.getlocal());
        result.addInstruction(result_tmp.getlocal());
        result.addInstruction(arrayAccess(arrayIndexNode, OP_setproperty));

        currentScope.releaseTemp(stem_tmp);
        currentScope.releaseTemp(index_tmp);
        currentScope.releaseTemp(result_tmp);

        return result;
    }

    public InstructionList reduce_preIncMemberExpr(IASNode iNode, InstructionList stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true, field);

        InstructionList result = createInstructionList(iNode);
        result.addAll(stem);
        result.addInstruction(OP_dup);
        result.addInstruction(OP_getproperty, field.getName());
        result.addInstruction(OP_increment);

        Binding result_tmp = null;
        //  Save a copy of the result to use as the value of this expression.
        if( need_result )
        {
            result.addInstruction(OP_dup);
            result_tmp = currentScope.allocateTemp();
            result.addInstruction(result_tmp.setlocal());
        }
        
        result.addInstruction(OP_setproperty, field.getName());

        if( need_result )
        {
            result.addInstruction(result_tmp.getlocal());
            currentScope.releaseTemp(result_tmp);
        }

        return result;
    }

    public InstructionList reduce_preIncNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true, unary);
        
        InstructionList result = createInstructionList(iNode);

        if ( unary.isLocal() ) {
            ICompilerProject project = currentScope.getProject();

            IDefinition inType = SemanticUtils.resolveUnaryExprType(iNode, project);

            IDefinition intType = project.getBuiltinType(BuiltinType.INT);
            IDefinition numberType = project.getBuiltinType(BuiltinType.NUMBER);
            if( inType == intType)
            {
                // Incrementing a local, typed as int
                // we can use inclocal_i since we know the result will be stored in an int
                result.addInstruction(unary.inclocal_i());
                if( need_result )
                    result.addInstruction(unary.getlocal());
            }
            else if( inType == numberType)
            {
                // Incrementing a local, typed as Number
                // we can use inclocal since we know the result will be stored in a Number
                result.addInstruction(unary.inclocal());
                if( need_result )
                    result.addInstruction(unary.getlocal());
            }
            else
            {
                result.addInstruction(unary.getlocal());
                result.addInstruction(OP_increment);
                if( need_result )
                    result.addInstruction(OP_dup);
                coerce(result, inType);
                result.addInstruction(unary.setlocal());
            }
        }
        else
        {
            Name n = unary.getName();
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_getproperty,n);
            result.addInstruction(OP_increment);
            if( need_result )
                result.addInstruction(OP_dup);
            result.addAll(currentScope.findProperty(unary, true));
            result.addInstruction(OP_swap);
            result.addInstruction(OP_setproperty,n);
        }

        return result;
    }

    /**
     * Add instructions to the instructionlist passed in to coerce the top value of the stack
     * to the given type.
     *
     * This will generate a generic OP_coerce, or one of the more specific coercion/conversion opcodes
     * such as convert_d for Number, coerce_s for String, etc.
     *
     * Matches ASCs behavior.
     *
     * @param il        the InstructionList to add the instruction to
     * @param toType    the type you want to coerce the top stack value to
     */
    public void coerce(InstructionList il, IDefinition toType )
    {
        if ( toType == null )
            return;

        assert toType instanceof ITypeDefinition;

        ICompilerProject project = currentScope.getProject();
        
        if( toType == project.getBuiltinType(BuiltinType.ANY_TYPE) )
        {
            il.addInstruction(OP_coerce_a);
        }
        else if ( toType == project.getBuiltinType(BuiltinType.STRING) )
        {
            il.addInstruction(OP_coerce_s);
        }
        else if ( toType == project.getBuiltinType(BuiltinType.BOOLEAN) )
        {
            il.addInstruction(OP_convert_b);
        }
        else if( toType == project.getBuiltinType(BuiltinType.NUMBER) )
        {
            il.addInstruction(OP_convert_d);
        }
        else if( toType == project.getBuiltinType(BuiltinType.INT) )
        {
            il.addInstruction(OP_convert_i);
        }
        else if( toType == project.getBuiltinType(BuiltinType.UINT) )
        {
            il.addInstruction(OP_convert_u);
        }
        else
        {
            il.addInstruction(OP_coerce, ((DefinitionBase)toType).getMName(project));
        }
    }
    public InstructionList reduce_regexLiteral(IASNode iNode)
    {
        String flags = ((RegExpLiteralNode) iNode).getFlagString();
        
        // If flags are defined, additional pushstring instruction is needed
        InstructionList result = createInstructionList(iNode, (flags.isEmpty()) ? 4 : 5);

        result.addAll(currentScope.getPropertyValue(regexType, currentScope.getProject().getBuiltinType(BuiltinType.REGEXP)));
        result.addInstruction(OP_pushstring, getStringLiteralContent(iNode));
        
        if (!flags.isEmpty())
        {
            result.addInstruction(OP_pushstring, flags);
            result.addInstruction(OP_construct, 2);
        }
        else
        {
            result.addInstruction(OP_construct, 1);
        }
        
        return result;
    }

    public InstructionList reduce_requiredParameter(IASNode iNode, Name param_name, Binding param_type)
    {
        currentScope.makeParameter(getParameterContent(iNode), param_type.getName());
        return null;
    }

    public InstructionList reduce_restParameter(IASNode iNode, Name param_name, Binding param_type)
    {
        currentScope.getMethodBodySemanticChecker().checkRestParameter(iNode, param_type);
        currentScope.makeParameter(getParameterContent(iNode), param_type.getName());
        return null;
    }

    public InstructionList reduce_returnVoidSideEffect(IASNode iNode, InstructionList value)
	{
        currentScope.getMethodBodySemanticChecker().checkReturnValue(iNode);

		//  Evaluate the expression for possible side effects.
		InstructionList result = createInstructionList(iNode);
		result.addAll(value);
		result.addInstruction(OP_returnvoid);
		return result;
	}

    public InstructionList reduce_returnValue(IASNode iNode, InstructionList value)
    {
        currentScope.getMethodBodySemanticChecker().checkReturnValue(iNode);
        return reduce_returnWithSideEffects(iNode, value, OP_returnvalue);
    }

    public InstructionList reduce_returnVoidValue(IASNode iNode, InstructionList no_value)
    {
        currentScope.getMethodBodySemanticChecker().checkReturnVoid(iNode);
        return reduce_returnWithSideEffects(iNode, no_value, OP_returnvoid);
    }
    

    /**
     *  Reduce a return statement that may have side effects
     *  (either mutation or reading volatile state).
     *  @param iNode - the root IASNode of the return statement.
     *  @param value - the value to be returned, or just computed.
     *  @param opcode - OP_returnvalue or OP_returnvoid.
     */
    private InstructionList reduce_returnWithSideEffects(IASNode iNode, InstructionList value, int opcode)
    {
        //  Create two InstructionLists; the first, result list
        //  goes in the body of the try block.  It computes the
        //  result value and stores it in a temp -- this allows
        //  the control-flow unwinding code to pop scopes, etc.,
        //  with no effect on the AS3 program's observable computation.
        //  The second IL is a control-flow stub that fetches the
        //  return value from the temp and returns it; this stub
        //  goes to the control-flow manager to be massaged into
        //  the control-flow unwinding code required by the current
        //  control flow contexts, if any.
        //  HOWEVER: as an optimization, the temp and the stub IL
        //  are only used when the flow manager says they're needed.
        InstructionList result = createInstructionList(iNode);
        result.addAll(value);

        Binding result_temp = null;
        boolean need_temp = currentScope.getFlowManager().hasNontrivialFlowCharacteristics();

        if ( need_temp )
        {
            result_temp = currentScope.allocateTemp();
            result.addInstruction(result_temp.setlocal());
        }

        InstructionList controlFlowStub = null;
        FunctionDefinition function_def = SemanticUtils.getFunctionDefinition(iNode);
        if ( need_temp )
        {
            controlFlowStub = createInstructionList(iNode);
            controlFlowStub.addInstruction(result_temp.getlocal());
            addRelevantReturnOpcode(function_def, opcode, controlFlowStub);
        }
        else
        {
            addRelevantReturnOpcode(function_def, opcode, result);
        }

        try
        {
            if ( need_temp )
            {
                //  The controlFlowStub may be replaced by code that
                //  branches to some context unwinding.
                result.addAll(getNonLocalControlFlow(controlFlowStub, ControlFlowContextManager.FIND_ALL_CONTEXTS));
            }
            else
            {
                //  Ensure we get back the same IL as sent in,
                //  or the ABC will be incorrect.
                InstructionList prev_result = result;
                result = getNonLocalControlFlow(result, ControlFlowContextManager.FIND_ALL_CONTEXTS);
                assert result == prev_result;
            }
        }
        catch ( UnknownControlFlowTargetException cant_return )
        {
            currentScope.addProblem(new UnexpectedReturnProblem(iNode));
            return createInstructionList(iNode);
        }

        return result;
    }

    public InstructionList reduce_returnVoid(IASNode iNode)
    {
        InstructionList result = createInstructionList(iNode, 1);
        currentScope.getMethodBodySemanticChecker().checkReturnVoid(iNode);
        FunctionDefinition functionDef = SemanticUtils.getFunctionDefinition(iNode);
        addRelevantReturnOpcode(functionDef, OP_returnvoid, result);

        try
        {
            return getNonLocalControlFlow(result, ControlFlowContextManager.FIND_ALL_CONTEXTS);
        }
        catch ( UnknownControlFlowTargetException cant_return )
        {
            currentScope.addProblem(new UnexpectedReturnProblem(iNode));
            return createInstructionList(iNode);
        }

    }

    /**
     * When inlining a method, a jump opcode must be emitted rather than a return
     * 
     * @param functionDef the function definition from which we're returning
     * @param opcode the original opcode which may be translated when inlining a function
     * @param result the InstructionList onto which the instruction will be added
     */
    private void addRelevantReturnOpcode(FunctionDefinition functionDef, int opcode, InstructionList result)
    {
        if (currentScope.insideInlineFunction())
        {
            Label inlinedFunctionCallSiteLabel = ((InlineFunctionLexicalScope)currentScope).getInlinedFunctionCallSiteLabel();
            switch (opcode)
            {
                case OP_returnvoid:
                {
                    // returnvoid pushes undefined onto the stack, so mimic that behavior
                    result.addInstruction(OP_pushundefined);

                    // if the function returns a type, undefined is coerced to that type
                    if (functionDef != null)
                    {
                        TypeDefinitionBase returnType = (TypeDefinitionBase)functionDef.resolveReturnType(currentScope.getProject());
                        if (returnType != null &&
                            !returnType.equals(ClassDefinition.getVoidClassDefinition()) &&
                            !returnType.equals(ClassDefinition.getAnyTypeClassDefinition()))
                        {
                            result.addInstruction(OP_coerce, returnType.getMName(currentScope.getProject()));
                        }
                    }

                    result.addInstruction(OP_jump, inlinedFunctionCallSiteLabel);
                    break;
                }
                case OP_returnvalue:
                    result.addInstruction(OP_jump, inlinedFunctionCallSiteLabel);
                    break;
                default:
                    result.addInstruction(opcode);
            }
        }
        else
        {
            result.addInstruction(opcode);
        }
    }

    public InstructionList reduce_runtimeNameExpression(IASNode iNode, InstructionList expr)
    {
        return expr;
    }

    public Binding reduce_simpleName(IASNode iNode)
    {
        final Binding result;
        final IdentifierNode identifier = (IdentifierNode)iNode;
        if (identifier.getName().equals(IASLanguageConstants.ANY_TYPE) && SemanticUtils.isE4XWildcardProperty(identifier))
        {
            // TODO: This is a specific fix for CMP-1731. CMP-696 should be able
            // to cover this use case in a more generic and robust way. Revisit
            // this implementation when CMP-696 is fixed.
            final ICompilerProject project = currentScope.getProject();
            final Nsset qualifiers = SemanticUtils.getOpenNamespaces(iNode, project);
            final Name name = new Name(CONSTANT_Multiname, qualifiers, null);
            result = new Binding(iNode, name, identifier.resolve(project));
        }
        else
        {
            result = currentScope.resolveName(identifier);
            currentScope.getMethodBodySemanticChecker().checkSimpleName(iNode, result);
        }
        return result;
    }

    public Name reduce_declName(IASNode iNode )
    {
        // We are the name of a declaration, get the containing DefinitionNode and grab the
        // name from there
        BaseDefinitionNode bdn = (BaseDefinitionNode)iNode.getAncestorOfType(BaseDefinitionNode.class);
        DefinitionBase db = bdn.getDefinition();
        Name n = db.getMName(currentScope.getProject());
        return n;
    }
    
    public InstructionList reduce_strictneqExpr(IASNode iNode, InstructionList l, InstructionList r)
    {
        InstructionList result = binaryOp(iNode, l, r, OP_strictequals);
        result.addInstruction(OP_not);
        return result;
    }

    public Binding reduce_superAccess(IASNode iNode, Binding qualified_name)
    {
        currentScope.getMethodBodySemanticChecker().checkSuperAccess(iNode);
        qualified_name.setSuperQualified(true);
        return qualified_name;
    }

    public InstructionList reduce_superCallExpr(IASNode iNode, Vector<InstructionList> args)
    {
        currentScope.getMethodBodySemanticChecker().checkExplicitSuperCall(iNode, args);
        //  TODO: Investigate optimizing this.
        InstructionList result = createInstructionList(iNode);
        //  Special case first part: don't look for the "super" name,
        //  push "this" on the stack.
        result.addInstruction(OP_getlocal0);
        for ( InstructionList arg: args)
            result.addAll(arg);
        // Special case second part: call constructsuper.
        result.addInstruction(OP_constructsuper, args.size() );
        return result;
    }

    private static class LookupSwitchInfo
    {
        public int minCase = Integer.MAX_VALUE;
        public int maxCase = Integer.MIN_VALUE;
        public ConditionalFragment defaultCase;
    }

    public InstructionList reduce_switchStmt(IASNode iNode, InstructionList switch_expr, Vector<ConditionalFragment> cases)
    {
        if (cases.size() == 0)
        {
            return reduce_trivial_switchStmt(iNode, switch_expr);
        }
        else
        {
            IExpressionNode conditional = ((SwitchNode)iNode).getConditionalNode();
            LookupSwitchInfo lookupSwitchInfo = getLookupSwitchInfo(conditional, cases);

            if (lookupSwitchInfo != null)
                return reduce_lookup_switchStmt(iNode, switch_expr, cases, lookupSwitchInfo);
            else
                return reduce_ifelse_switchStmt(iNode, switch_expr, cases);
        }
    }

    /**
     * Inspect the switch cases to determine if a lookup switch can be used.  Any criteria
     * for using a lookup switch or if/else chain should be adjusted here.
     * 
     * @param conditional
     * @param cases
     * @return LookupSwitchInfo if a lookup switch can be used, otherwise null
     */
    private LookupSwitchInfo getLookupSwitchInfo(IExpressionNode conditional, Vector<ConditionalFragment> cases)
    {
        // only generate a lookup switch if the conditional type is of type int or uint
        IDefinition conditionalType = conditional.resolveType(currentScope.getProject());
        if (!SemanticUtils.isBuiltin(conditionalType, BuiltinType.INT, currentScope.getProject()) &&
            !SemanticUtils.isBuiltin(conditionalType, BuiltinType.UINT, currentScope.getProject()))
            return null;

        LookupSwitchInfo lookupSwitchInfo = new LookupSwitchInfo();

        boolean hasConditionalCases = false;
        for (ConditionalFragment currentCase : cases)
        {
            if (currentCase.isUnconditionalAlternative())
            {
                lookupSwitchInfo.defaultCase = currentCase;
                continue;
            }

            if (currentCase.constantCondition == null)
                return null;

            assert (SemanticUtils.isBuiltin(conditionalType, BuiltinType.INT, currentScope.getProject()) ||
                    SemanticUtils.isBuiltin(conditionalType, BuiltinType.UINT, currentScope.getProject())) : "only switching on type int or uint supported";

            long value;
            if (SemanticUtils.isBuiltin(conditionalType, BuiltinType.UINT, currentScope.getProject()))
                value = ECMASupport.toUInt32(currentCase.constantCondition);
            else
                value = ECMASupport.toInt32(currentCase.constantCondition);

            // We could potentially use longs here, but for now just fallback to an if/else
            // jump in the case where a value is outside the integeger range unless we find
            // real world code where it makes sense to handle longs too.
            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE)
                return null;

            lookupSwitchInfo.minCase = Math.min((int)value, lookupSwitchInfo.minCase);
            lookupSwitchInfo.maxCase = Math.max((int)value, lookupSwitchInfo.maxCase);
            hasConditionalCases = true;
        }

        // if there are no conditional cases, don't generate lookup switch
        if (!hasConditionalCases)
            return null;

        int range = lookupSwitchInfo.maxCase - lookupSwitchInfo.minCase;

        // if the range is greater than 20 and if less than 50% ranges dense, then use if/else
        if ((range > 20) && (range < cases.size() * 2))
            return null;

        // limit the size of the jump table to cut down on compile time
        // memory building up the table, and avoid any potential player
        // bugs with massive lookup switches
        if (range > (2 ^ 16))
            return null;

        return lookupSwitchInfo;
    }

    private InstructionList reduce_lookup_switchStmt(IASNode iNode, InstructionList switch_expr, Vector<ConditionalFragment> cases, LookupSwitchInfo lookupSwitchInfo)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(switch_expr);
        result.addInstruction(OP_convert_i);

        // save some space in the case table, by not generating entries
        // for any values between 0 and the min case value
        int caseOffset = 0;
        if (lookupSwitchInfo.minCase > 0)
        {
            caseOffset = lookupSwitchInfo.minCase;
            lookupSwitchInfo.minCase = 0;
            lookupSwitchInfo.maxCase = lookupSwitchInfo.maxCase - caseOffset;
            result.addInstruction(OP_pushint, Integer.valueOf(caseOffset));
            result.addInstruction(OP_subtract_i);
        }
        // if we have negative values in the case expression, need
        // to offset from zero
        else if (lookupSwitchInfo.minCase < 0)
        {
            caseOffset = lookupSwitchInfo.minCase;
            lookupSwitchInfo.minCase = 0;
            lookupSwitchInfo.maxCase = lookupSwitchInfo.maxCase - caseOffset;
            result.addInstruction(OP_pushint, new Integer(lookupSwitchInfo.minCase + 1));
            result.addInstruction(OP_add_i);
        }

        Label switchTail = null;
        Label defaultLabel;

        if (lookupSwitchInfo.defaultCase != null)
            defaultLabel = lookupSwitchInfo.defaultCase.getLabel();
        else
            defaultLabel = switchTail = new Label();

        // generate the case table, maxCase+2, as count from zero
        // and add one for the default label at the end
        Label[] caseLabels = new Label[lookupSwitchInfo.maxCase + 2];

        // pre-fill the case label table with the default label.
        Arrays.fill(caseLabels, defaultLabel);

        // fill in specific labels for any specified values
        for (ConditionalFragment current_case : cases)
        {
            if (current_case.isUnconditionalAlternative())
                continue;

            Object caseValue = current_case.constantCondition;
            // a constant of value 90000 was in a SWC as a double even though
            // the type of the constant was int
            if (caseValue instanceof Double)
                caseValue = new Integer(((Double)caseValue).intValue());
            assert (caseValue instanceof Integer) : "reduce_lookup_switchStmt called on non integer case value";
            final int index = (Integer)caseValue - caseOffset;
            // if there is already a non-default value for this
            // index, ignore it, as only the first case counts
            if (caseLabels[index] != defaultLabel)
                continue;

            caseLabels[index] = current_case.getLabel();
        }

        result.addInstruction(OP_lookupswitch, caseLabels);

        Label default_case_label = null;
        for (ConditionalFragment current_case : cases)
        {
            if (current_case.isUnconditionalAlternative())
            {
                //  The parser rejects duplicate default alternatives
                //  in some situations, but not in others.
                if (default_case_label == null)
                    default_case_label = current_case.statement.getLabel();
                else
                    currentScope.addProblem(new MultipleSwitchDefaultsProblem(current_case.site));
            }

            result.addAll(current_case.statement);
        }

        switchTail = addInterstitialControlFlow(result, switchTail);

        //  No continue here, and thus there is no need
        //  to check for previous labeled flow contexts.
        currentScope.getFlowManager().finishSwitchControlFlowContext(result);

        if ( switchTail != null )
            result.labelNext(switchTail);

        return result;
    }

    private InstructionList reduce_ifelse_switchStmt(IASNode iNode, InstructionList switch_expr, Vector<ConditionalFragment> cases)
    {
        assert (cases.size() > 0) : "reduce_ifelse_switchStmt called on switch stmt with no cases";

        // need to convert any constant values back
        // to a conditional expression when not generating
        // a lookup switch
        convertConstantValueConditionsToInstructions(cases);

        //  TODO: Optimize InstructionList size.
        InstructionList result = createInstructionList(iNode);
        Label default_case_label = null;
        Label switch_tail = null;

        //  Get the switch value and save it in a temp.
        Binding switch_temp = currentScope.allocateTemp();
        result.addAll(switch_expr);
        result.addInstruction(switch_temp.setlocal());

        //  First, jump to the switch table.
        if ( ! cases.elementAt(0).isUnconditionalAlternative() || cases.size() == 1 )
            result.addInstruction(OP_jump, cases.elementAt(0).getLabel());
        else
            result.addInstruction(OP_jump, cases.elementAt(1).getLabel());


        for ( ConditionalFragment current_case: cases )
        {
            //  Ensure the case begins with a OP_label instruction
            //  so we can branch back to it.
            if ( current_case.statement.isEmpty() || current_case.statement.firstElement().getOpcode() != OP_label )
            {
                InstructionList labeled_list = createInstructionList(iNode);
                labeled_list.addInstruction(OP_label);
                labeled_list.addAll(current_case.statement);
                current_case.statement = labeled_list;
            }

            if ( current_case.isUnconditionalAlternative() )
            {
                //  The parser rejects duplicate default alternatives
                //  in some situations, but not in others.
                if ( default_case_label == null )
                    default_case_label = current_case.statement.getLabel();
                else
                    currentScope.addProblem(new MultipleSwitchDefaultsProblem(current_case.site));
            }
            // no need to check for duplicate cases, as the first case is always chosen in this situation

            current_case.getStatementLabel();  // Touch the label so it will be properly recorded.
            result.addAll(current_case.statement);
        }

        //  Jump to the tail.
        switch_tail = addInterstitialControlFlow(result, null);

        //  Emit the "switch table" as if/else statements.
        for ( ConditionalFragment current_case: cases )
        {
            if ( !current_case.isUnconditionalAlternative() )
            {
                result.addAll(current_case.condition);
                result.addInstruction(switch_temp.getlocal());
                result.addInstruction(OP_ifstricteq, current_case.getStatementLabel() );
            }
        }

        //  Emit the jump to the default case.
        if ( default_case_label != null )
        {
            result.addInstruction(OP_jump, default_case_label);
        }

        currentScope.releaseTemp(switch_temp);

        //  No continue here, and thus there is no need
        //  to check for previous labeled flow contexts.
        currentScope.getFlowManager().finishSwitchControlFlowContext(result);

        if ( switch_tail != null )
            result.labelNext(switch_tail);

        return result;
    }

    private InstructionList reduce_trivial_switchStmt(IASNode iNode, InstructionList switch_expr)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(switch_expr);
        //  Balance the stack!
        result.addInstruction(OP_pop);
        currentScope.getFlowManager().finishSwitchControlFlowContext(result);
        return result;
    }

    /**
     * If the optimized constant case of conditional fragements are not being used, need to go through
     * all cases and convert any constant conditionals back to a conditional instruction list for codegen.
     * 
     * @param cases
     */
    private void convertConstantValueConditionsToInstructions(Vector<ConditionalFragment> cases)
    {
        for (ConditionalFragment current_case : cases)
        {
            current_case.convertConstantConditionToInstruction();

            if (current_case.isConstantConditional())
            {
                current_case.condition = transform_constant_value(current_case.site, current_case.constantCondition);
            }
        }
    }

    public InstructionList reduce_ternaryExpr(IASNode iNode, InstructionList test, InstructionList when_true, InstructionList when_false)
    {
        // AIR (and not FlashPlayer) requires a coerce_a at the end of each clause if the results will be
        // assigned to a Dictionary.
        IBinaryOperatorNode binaryNode = (IBinaryOperatorNode)iNode.getAncestorOfType(IBinaryOperatorNode.class);
        boolean isBracketAssign = binaryNode != null && binaryNode.getOperator() == OperatorType.ASSIGNMENT
            && binaryNode.getLeftOperandNode() instanceof DynamicAccessNode;
        ITypeDefinition destinationType = null;
        if (isBracketAssign)
            destinationType = binaryNode.getLeftOperandNode().resolveType(currentScope.getProject());
        TernaryOperatorNode ternaryNode = (TernaryOperatorNode)iNode;
        
        IExpressionNode valueNode;        
        IDefinition type_def; //= valueNode.resolveType(currentScope.getProject()); //SemanticUtils.getDefinitionOfUnderlyingType(, 
                           // true, currentScope.getProject());
        boolean need_coerce = false;
        
        InstructionList result = createInstructionList(iNode, test.size() + when_true.size() + when_false.size() + 2);
        Label tail = new Label();

        result.addAll(test);
        result.addInstruction(OP_iffalse, when_false.getLabel());
        result.addAll(when_true);
        valueNode = ternaryNode.getLeftOperandNode();
        type_def = valueNode.resolveType(currentScope.getProject());
        need_coerce = type_def != null && !type_def.equals(destinationType);
        if (need_coerce && isBracketAssign)
            coerce(result, destinationType);
        result.addInstruction(OP_jump, tail);
        result.addAll(when_false);
        valueNode = ternaryNode.getRightOperandNode();
        type_def = valueNode.resolveType(currentScope.getProject());
        need_coerce = type_def != null && !type_def.equals(destinationType);
        if (need_coerce && isBracketAssign)
            coerce(result, destinationType);
        result.labelNext(tail);

        return result;
    }

    public InstructionList reduce_throwStmt(IASNode iNode, InstructionList tossable)
    {
        currentScope.getMethodBodySemanticChecker().checkThrow(iNode);

        InstructionList result = createInstructionList(iNode, tossable.size() + 1);
        result.addAll(tossable);
        result.addInstruction(OP_throw);
        return result;
    }

    public InstructionList reduce_tryCatchFinallyStmt(IASNode iNode, InstructionList try_stmt, InstructionList finally_stmt, Vector<CatchPrototype> catch_blocks)
    {
        InstructionList result = generateTryCatchFinally(try_stmt, catch_blocks, finally_stmt);
        currentScope.getFlowManager().finishExceptionContext();
        return result;
    }

    public InstructionList reduce_tryCatchStmt(IASNode iNode, InstructionList try_stmt, Vector<CatchPrototype> catch_blocks)
    {
        //  TODO: Optimize.
        InstructionList result = createInstructionList(iNode);

        if ( try_stmt.isEmpty() )
        {
            //  TODO: The catch clause gen may have
            //  side effects, so we can't skip this.
            try_stmt = createInstructionList(iNode);
            try_stmt.addInstruction(OP_nop);
        }

        Label catch_tail = new Label();

        result.addAll(try_stmt);
        result.addInstruction(OP_jump, catch_tail);

        //  Get labels for the start and end of the try block.
        Label try_start = result.getLabel();
        Label try_end   = result.getLastLabel();

        for ( CatchPrototype catch_proto: catch_blocks )
        {
            boolean is_last_catch = catch_proto.equals(catch_blocks.lastElement());

            InstructionList catch_body = generateCatchBlock(try_start, try_end, catch_proto);

            if( !is_last_catch && catch_body.canFallThrough() )
                catch_body.addInstruction(OP_jump, catch_tail);

            result.addAll(catch_body);
        }

        currentScope.getFlowManager().finishExceptionContext();

        result.labelNext(catch_tail);
        return result;
    }

    public InstructionList reduce_tryFinallyStmt(IASNode iNode, InstructionList try_stmt, InstructionList finally_stmt)
    {
        InstructionList result = generateTryCatchFinally(try_stmt, null, finally_stmt);
        currentScope.getFlowManager().finishExceptionContext();
        return result;
    }

    public InstructionList reduce_typedFunction_to_statement(IASNode iNode, InstructionList plist, Binding return_type, InstructionList block)
    {
        Binding nestedFunctionName = currentScope.resolveName((IdentifierNode)SemanticUtils.getNthChild(iNode, 0));

        InstructionList result = createInstructionList(iNode);

        this.generateNestedFunction(
            iNode,
            this.miniScopes.empty()? result: this.miniScopes.peek(),
            nestedFunctionName,
            return_type.getName(),
            block
        );

        currentScope.getMethodBodySemanticChecker().checkNestedFunctionDecl((IFunctionNode)iNode);

        return result;
    }

    public InstructionList reduce_typedVariableDecl(IASNode iNode, Name var_name, Binding var_type, Vector<InstructionList> chained_decls)
    {
        BaseVariableNode var_node = (BaseVariableNode)iNode;
        currentScope.getMethodBodySemanticChecker().checkVariableDeclaration(iNode);

        // Must check for this before we call makeVariable, but need to emit the instructions after
        // makeVariable so that Bindings are set up correctly with the right local register indexes.
        boolean needsHoistedInitInsns = currentScope.needsHoistedInitInsns(var_name, false);

        Binding var = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
        currentScope.makeVariable(var, var_type.getName(), ((BaseDefinitionNode)iNode).getMetaInfos());

        // Now that makeVariable is done, emit the hoisted instructions if we needed them.
        if(needsHoistedInitInsns)
            addHoistedInstructionsForVarDecl(var_name, var_type);

        InstructionList result = createInstructionList(iNode);


        for ( InstructionList decl: chained_decls )
            result.addAll(decl);

        return result;
    }

    /**
     * Helper method to add hoisted init instructions for a given variable.
     *
     * This is neccessary when local vars live in registers, and they are referenced before they are inited.
     * If we don't emit the init insns at the top of the method, then the locals could have the wrong values/types
     * when they're referenced resulting in incorrect behavior at runtime
     * @param var_name  the name of the local var
     * @param var_type  the type of the local var
     */
    private void addHoistedInstructionsForVarDecl (Name var_name, Binding var_type)
    {
        Binding var_binding = currentScope.getLocalBinding(var_name);
        if ( var_binding != null && var_binding.isLocal() )
        {
            //  Add initializer code to the init instructions.
            InstructionList init_insns = currentScope.getHoistedInitInstructions();

            ICompilerProject project = currentScope.getProject();

            IDefinition type_def = var_type.getDefinition();

            if  (
                    type_def == project.getBuiltinType(BuiltinType.INT) ||
                    type_def == project.getBuiltinType(BuiltinType.UINT)
                )
            {
                init_insns.addInstruction(OP_pushbyte, 0);
                init_insns.addInstruction(var_binding.setlocal());
            }
            else if ( type_def == project.getBuiltinType(BuiltinType.BOOLEAN) )
            {
                init_insns.addInstruction(OP_pushfalse);
                init_insns.addInstruction(var_binding.setlocal());
            }
            else if ( type_def == project.getBuiltinType(BuiltinType.NUMBER) )
            {
                init_insns.addInstruction(OP_pushnan);
                init_insns.addInstruction(var_binding.setlocal());
            }
            else if ( type_def == project.getBuiltinType(BuiltinType.ANY_TYPE) )
            {
                init_insns.addInstruction(OP_pushundefined);
                init_insns.addInstruction(var_binding.setlocal());
            }
            else if (
                        type_def instanceof ITypeDefinition &&
                        ((ITypeDefinition)type_def).isInstanceOf((ITypeDefinition)project.getBuiltinType(BuiltinType.OBJECT), project)
                )
            {
                init_insns.addInstruction(OP_pushnull);
                init_insns.addInstruction(var_binding.setlocal());
            }
            else
            {
                init_insns.addInstruction(OP_pushundefined);
                init_insns.addInstruction(var_binding.setlocal());
            }
        }
    }

    public InstructionList reduce_typedVariableDeclWithInitializer(IASNode iNode, Name var_name, Binding var_type, InstructionList var_initializer, Vector<InstructionList> chained_decls)
    {
        BaseVariableNode var_node = (BaseVariableNode)iNode;

        currentScope.getMethodBodySemanticChecker().checkVariableDeclaration(iNode);

        // Must check for this before we call makeVariable, but need to emit the instructions after
        // makeVariable so that Bindings are set up correctly with the right local register indexes.
        boolean refedBeforeInited = currentScope.needsHoistedInitInsns(var_name, true);
        
        Binding var = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
        currentScope.makeVariable(var, var_type.getName(), ((BaseDefinitionNode)iNode).getMetaInfos());

        // If the binding has already been referenced, then we need to emit some hoisted init instructions
        // so that the initial value will be correct.
        if( refedBeforeInited )
            addHoistedInstructionsForVarDecl(var_name, var_type);

        currentScope.getMethodBodySemanticChecker().checkInitialization(iNode, var);
        InstructionList result = generateAssignment(iNode, var, var_initializer);
        for ( InstructionList decl: chained_decls )
            result.addAll(decl);

        return result;
    }

    public InstructionList reduce_typedVariableDeclWithConstantInitializer(IASNode iNode, Name var_name, Binding var_type, Object constant_var_initializer, Vector<InstructionList> chained_decls)
    {
        BaseVariableNode var_node = (BaseVariableNode)iNode;
        PooledValue transformed_constant_initializer =
            currentScope.getMethodBodySemanticChecker().checkInitialValue(var_node, var_type, new PooledValue(constant_var_initializer));

        // if this definition isn't a const, then just use the normal variable initialization, as
        // we only initialize the const slots and not var slots
        if (!SemanticUtils.isConst(iNode, currentScope.getProject()))
        {
            InstructionList var_initializer = transform_pooled_value(iNode, transformed_constant_initializer);
            return reduce_typedVariableDeclWithInitializer(iNode, var_name, var_type, var_initializer, chained_decls);
        }

        currentScope.getMethodBodySemanticChecker().checkVariableDeclaration(iNode);
        InstructionList result = createInstructionList(iNode);
        Binding var = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
        currentScope.makeVariable(var, var_type.getName(), ((BaseDefinitionNode)iNode).getMetaInfos(), transformed_constant_initializer.getValue());

        //  If the variable is in a local, then it needs to be initialized at the start of the method.
        if ( var.isLocal() )
        {
            InstructionList var_initializer = transform_pooled_value(iNode, transformed_constant_initializer);
            currentScope.getHoistedInitInstructions().addAll(reduce_typedVariableDeclWithInitializer(iNode, var_name, var_type, var_initializer, new Vector<InstructionList>()));
        }

        for ( InstructionList decl: chained_decls )
            result.addAll(decl);

        return result;
    }

    public InstructionList reduce_typedBindableVariableDecl(IASNode iNode, Name name, Binding var_type, Vector<InstructionList> chained_decls)
    {
        BaseVariableNode vn = (BaseVariableNode)iNode;
        currentScope.getMethodBodySemanticChecker().checkBindableVariableDeclaration(iNode, vn.getDefinition());
        InstructionList result = createInstructionList(iNode);
        Binding var = currentScope.resolveName((IdentifierNode)vn.getNameExpressionNode());
        currentScope.makeBindableVariable(var, var_type.getName(), vn.getMetaInfos());
        for ( InstructionList decl: chained_decls )
             result.addAll(decl);

        return result;
    }
    public InstructionList reduce_typedBindableVariableDeclWithInitializer(IASNode iNode, Name var_name, Binding var_type, InstructionList var_initializer, Vector<InstructionList> chained_decls)
    {
        BaseVariableNode vn = (BaseVariableNode)iNode;
        currentScope.getMethodBodySemanticChecker().checkBindableVariableDeclaration(iNode, vn.getDefinition());
        Binding var = currentScope.resolveName((IdentifierNode)vn.getNameExpressionNode());
        currentScope.makeBindableVariable(var, var_type.getName(), vn.getMetaInfos());
        // pass in null as definition so the assignment goes to the backing var and not the setter
        // maybe we'll have to get the actual var def someday.
        InstructionList result = generateAssignment(iNode, new Binding(iNode, BindableHelper.getBackingPropertyName(var_name), null), var_initializer);
        for ( InstructionList decl: chained_decls )
             result.addAll(decl);

        return result;
    }
    public Binding reduce_typedVariableExpression(IASNode iNode, Name var_name, Binding var_type)
    {
        VariableExpressionNode var_expr_node = (VariableExpressionNode)iNode;
        BaseVariableNode var_node = (BaseVariableNode) var_expr_node.getTargetVariable();
        currentScope.getMethodBodySemanticChecker().checkVariableDeclaration(SemanticUtils.getNthChild(iNode, 0));
        Binding var = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
        currentScope.makeVariable(var, var_type.getName(), var_node.getMetaInfos());

        return var;
    }

    public InstructionList reduce_typelessFunction(IASNode iNode, InstructionList plist, InstructionList block)
    {
        Binding nestedFunctionName = currentScope.resolveName((IdentifierNode)SemanticUtils.getNthChild(iNode, 0));

        InstructionList result = createInstructionList(iNode);

        this.generateNestedFunction(
            iNode,
            this.miniScopes.empty()? result: this.miniScopes.peek(),
            nestedFunctionName,
            null,
            block
        );

        currentScope.getMethodBodySemanticChecker().checkNestedFunctionDecl((IFunctionNode)iNode);

        return result;
    }

    public InstructionList reduce_typeof_expr(IASNode iNode, InstructionList operand)
    {
        InstructionList result = unaryOp(iNode, operand, OP_typeof);
        this.typeofCount--;
        return result;
    }

    public InstructionList reduce_typeof_name(IASNode iNode, Binding binding)
    {
        InstructionList result = null;

        if ( binding.getDefinition() != null )
        {
            ITypeDefinition typezo = binding.getDefinition().resolveType(currentScope.getProject());
            
            ICompilerProject project = currentScope.getProject();

            if ( typezo == project.getBuiltinType(BuiltinType.STRING) )
            {
                result = createInstructionList(iNode);
                result.addInstruction(OP_pushstring, "string");
            }
            else if ( 
                typezo == project.getBuiltinType(BuiltinType.NUMBER) ||
                typezo == project.getBuiltinType(BuiltinType.INT) ||
                typezo == project.getBuiltinType(BuiltinType.UINT) 
            )
            {
                result = createInstructionList(iNode);
                result.addInstruction(OP_pushstring, "number");
            }
            else if ( typezo == project.getBuiltinType(BuiltinType.BOOLEAN) )
            {
                result = createInstructionList(iNode);
                result.addInstruction(OP_pushstring, "boolean");
            }
        }


        if ( result == null )
        {
            result = createInstructionList(iNode);
            generateAccess(binding, AccessType.Lenient, result);
            result.addInstruction(OP_typeof);
        }

        this.typeofCount--;
        return result;
    }

    public InstructionList reduce_useNamespaceDirective(IASNode iNode, Binding ns_name)
    {
        currentScope.getMethodBodySemanticChecker().checkUseNamespaceDirective(iNode, ns_name);
        return createInstructionList(iNode);
    }

    public InstructionList reduce_variableExpression(IASNode iNode, Vector<InstructionList> decls)
    {
        InstructionList result = createInstructionList(iNode);

        for ( InstructionList var_decl: decls )
            result.addAll(var_decl);
        return result;
    }

    public InstructionList reduce_vectorLiteral(IASNode iNode, Binding type_param, Vector<InstructionList> elements)
    {
        currentScope.getMethodBodySemanticChecker().checkVectorLiteral(iNode, type_param);
        InstructionList result = createInstructionList(iNode);

        Nsset ns_set = new Nsset(new Namespace(CONSTANT_PackageNs, IASLanguageConstants.Vector_impl_package));
        Name vector_name = new Name(CONSTANT_Qname, ns_set, IASLanguageConstants.Vector);

        result.addAll(currentScope.getPropertyValue(vector_name, currentScope.getProject().getBuiltinType(BuiltinType.VECTOR)));
        generateTypeNameParameter(type_param, result);
        result.addInstruction(OP_applytype, 1);
        result.pushNumericConstant(elements.size());
        result.addInstruction(OP_construct, 1);

        //  Share this instruction; there may be many elements.
        Instruction setter = arrayAccess(iNode, OP_setproperty);

        for ( int i = 0; i < elements.size(); i++ )
        {
            result.addInstruction(OP_dup);
            result.pushNumericConstant(i);
            result.addAll(elements.elementAt(i));
            result.addInstruction(setter);
        }

        return result;
    }

    public InstructionList reduce_voidExpr_to_expression(IASNode iNode)
    {
        InstructionList il = createInstructionList(iNode);
        il.addInstruction(OP_pushundefined);
        return il;
    }

    public Binding reduce_voidExpr_to_return_type_name(IASNode node)
    {
        return new Binding(node, voidType, null);
    }

    public Binding reduce_voidExpr_to_type_name(IASNode node)
    {
        getProblems().add(new VoidTypeProblem(node));
        return new Binding(node, voidType, null);
    }

    public Object reduce_void0Literal_to_constant_value(IASNode iNode)
    {
        return UNDEFINED_VALUE;
    }

    public InstructionList reduce_void0Literal_to_object_literal(IASNode iNode)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addInstruction(OP_pushundefined);
        return result;
    }

    public InstructionList reduce_instructionListExpression(IASNode iNode)
    {
        return ((InstructionListNode)iNode).getInstructions();
    }

    public InstructionList reduce_void0Operator(IASNode iNode)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addInstruction(OP_pushundefined);
        return result;
    }

    public Object reduce_voidOperator_to_constant_value(IASNode iNode, Object constant_value)
    {
        return UNDEFINED_VALUE;
    }

    public InstructionList reduce_voidOperator_to_expression(IASNode iNode, InstructionList expr)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addAll(expr);
        result.addInstruction(OP_pop);
        result.addInstruction(OP_pushundefined);
        return result;
    }

    public InstructionList reduce_whileStmt(IASNode iNode, InstructionList cond, InstructionList body)
    {
        InstructionList result = createInstructionList(iNode, cond.size() + body.size() + 5);
        currentScope.getFlowManager().resolveContinueLabel(cond);

        //  Jump to the test.
        result.addInstruction(OP_jump, cond.getLabel());

        //  Create an emitter-time label, and attach
        //  it to an OP_label instruction for the
        //  backwards branch.
        result.addInstruction(OP_label);
        Label loop = result.getLastLabel();
        result.addAll(body);

        result.addAll(cond);
        result.addInstruction(OP_iftrue, loop);

        currentScope.getFlowManager().finishLoopControlFlowContext(result);

        return result;
    }

    public InstructionList reduce_withStmt(IASNode iNode, InstructionList new_scope, InstructionList body)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(new_scope);

        if ( currentScope.getFlowManager().hasWithStorage())
        {
            result.addInstruction(OP_dup);
            result.addInstruction(currentScope.getFlowManager().getWithStorage().setlocal());
        }

        result.addInstruction(OP_pushwith);
        result.addAll(body);
        result.addInstruction(OP_popscope);

        currentScope.getFlowManager().finishWithContext(result);
        return result;
    }

    /**
     *  The enumeration of states that the code generator finds interesting in an XML literal.
     *  There are states here than the reducer, below, doesn't consider especially interesting;
     *  see computeXMLContentStateMatrix() for their usage.
     */
    private enum XMLContentState { TagStart, TagLiteral, TagName, Attr, ValueNeedsEquals, Value, TagEnd, ContentLiteral, ContentExpression };

    @SuppressWarnings("incomplete-switch")
	public InstructionList reduce_XMLContent(IASNode iNode, Vector<InstructionList> exprs)
    {
        InstructionList result = createInstructionList(iNode);

        result.addAll(currentScope.getPropertyValue(xmlType, currentScope.getProject().getBuiltinType(BuiltinType.XML)));

        //  Add all the arguments up into a single String.

        XMLContentState[] content_state = computeXMLContentStateMatrix(exprs);

        result.addAll(exprs.elementAt(0));

        for ( int i = 1; i < exprs.size(); i++ )
        {
            result.addAll(exprs.elementAt(i));

            switch ( content_state[i] )
            {
                case TagName:
                    {
                        //  Parser elides whitespace after tag name expressions.
                        result.addInstruction(OP_pushstring, " ");
                        result.addInstruction(OP_add);
                    }
                    break;
                case Attr:
                    {
                        //  Parser elides whitespace before attribute name expressions.
                        if ( content_state[i-1] != XMLContentState.TagName )
                        {
                            result.addInstruction(OP_pushstring, " ");
                            result.addInstruction(OP_swap);
                            result.addInstruction(OP_add);
                        }
                    }
                    break;
                case ValueNeedsEquals:
                    {
                        if ( content_state[i-1] == XMLContentState.Attr )
                        {
                            //  Parser elided this token.
                            result.addInstruction(OP_pushstring, "=");
                            result.addInstruction(OP_swap);
                            result.addInstruction(OP_add);
                        }
                    }
                    //  falls through
                case Value:
                    {
                        result.addInstruction(OP_esc_xattr);

                        //  The attribute will need quote marks for the AVM's XML parser.
                        result.addInstruction(OP_pushstring, "\"");
                        result.addInstruction(OP_swap);
                        result.addInstruction(OP_add);
                        result.addInstruction(OP_pushstring, "\"");
                        result.addInstruction(OP_add);
                    }
                    break;
                case ContentExpression:
                    result.addInstruction(OP_esc_xelem);
            }

            result.addInstruction(OP_add);
        }

        result.addInstruction(OP_construct, 1);
        return result;
    }

    public InstructionList reduce_XMLList(IASNode iNode, Vector<InstructionList> exprs)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(currentScope.getPropertyValue(xmlListType, currentScope.getProject().getBuiltinType(BuiltinType.XMLLIST)));
        //  The first and last elements are the <> and </> brackets.
        //  An empty XMLList <></> should be reduced by the constant
        //  case, below, so we know we have at least one element.
        result.addAll(exprs.get(1));
        for ( int i = 2; i < exprs.size() - 1; i++ )
        {
            result.addAll(exprs.get(i));
            result.addInstruction(OP_add);
        }
        result.addInstruction(OP_construct, 1);
        return result;
    }

    public InstructionList reduce_XMLListConst(IASNode iNode, Vector<String> elements)
    {
        InstructionList result = createInstructionList(iNode);
        result.addAll(currentScope.getPropertyValue(xmlListType, currentScope.getProject().getBuiltinType(BuiltinType.XMLLIST)));
        StringBuilder buffer = new StringBuilder();
        //  The first and last elements are the <> and </> brackets.
        for ( int i = 1; i < elements.size() -1; i++ )
            buffer.append(elements.elementAt(i));
        result.addInstruction(OP_pushstring, buffer.toString());
        result.addInstruction(OP_construct, 1);
        return result;
    }

    /**
     *  Clone an InstructionList.
     *  @return the clone, appropriately cast.
     *  @note clone() always returns Object.
     */
    InstructionList replicate(InstructionList prototype)
    {
        return (InstructionList)prototype.clone();
    }

    /**
     *  Set this reducer's list of a priori instructions;
     *  typically these are field initializers passed in to
     *  a constructor routine.
     */
    public void setInstanceInitializers(InstructionList insns)
    {
        this.instanceInitializers = insns; 
    }

    /**
     *  Set this reducer's initial LexicalScope.
     */
    public void setCurrentscope(LexicalScope scope)
    {
        this.currentScope = scope;
    }

    /*
     *  ******************************
     *  **  Transformation routines **
     *  ******************************
     */

    public InstructionList transform_boolean_constant(IASNode iNode, Boolean boolean_constant)
    {
        InstructionList result = createInstructionList(iNode, 1);

        if ( Boolean.TRUE.equals(boolean_constant) )
            result.addInstruction(OP_pushtrue);
        else
            result.addInstruction(OP_pushfalse);

        return result;
    }

    public InstructionList transform_double_constant(IASNode iNode, Double double_constant)
    {
        InstructionList result = createInstructionList(iNode, 1);
        pushNumericConstant(result, double_constant, SemanticUtils.resolveType(iNode, currentScope.getProject()));
        return result;
    }

    public InstructionList transform_numeric_constant(IASNode iNode, Number numeric_constant)
    {
        if( numeric_constant instanceof Integer)
            return transform_integer_constant(iNode, (Integer)numeric_constant);
        if( numeric_constant instanceof Long )
            return transform_long_constant(iNode, (Long)numeric_constant);
        if( numeric_constant instanceof Double)
            return transform_double_constant(iNode, (Double)numeric_constant);
        return createInstructionList(iNode, 1);
    }

    /**
     * Transform any PooledValue into an expression, so we can constant fold all sorts of expressions.
     * @param iNode            the node that generated the constant_value
     * @param pooled_value     the pooled value
     * @return                 an InstructionList that contains instructions to push the pooled_value onto the stack.
     */
    public InstructionList transform_pooled_value(IASNode iNode, PooledValue pooled_value )
    {
        return transform_constant_value(iNode, pooled_value.getValue());
    }

    /**
     * Transform any constant_value into an expression, so we can constant fold all sorts of expressions.
     * @param iNode            the node that generated the constant_value
     * @param constant_value   the constant value
     * @return                 an InstructionList that contains instructions to push the constant_value onto the stack.
     */
    public InstructionList transform_constant_value(IASNode iNode, Object constant_value )
    {
        assert (!(constant_value instanceof PooledValue)) : "transform_constant_value should not be called with a PooledValue";

        if( constant_value instanceof Boolean )
        {
            return transform_boolean_constant(iNode, (Boolean)constant_value);
        }
        if( constant_value instanceof Number )
        {
            return transform_numeric_constant(iNode, (Number)constant_value);
        }
        if( constant_value instanceof String )
        {
            return transform_string_constant(iNode, (String)constant_value);
        }

        InstructionList result = createInstructionList(iNode, 1);
        if( constant_value instanceof Namespace)
        {
            result.addInstruction(OP_pushnamespace, constant_value);
        }
        else if( constant_value == ABCConstants.NULL_VALUE )
        {
            result.addInstruction(OP_pushnull);
        }
        else if( constant_value == ABCConstants.UNDEFINED_VALUE )
        {
            result.addInstruction(OP_pushundefined);
        }
        else
        {
            assert false : "unknown constant type";
        }

        return result;
    }
    /**
     *  transform a string_constant to a constant_value - essentially a no-op, but need a reduction
     *  so we can assign it a cost
     */
    public Object transform_string_constant_to_constant(IASNode iNode, String string_constant)
    {
        return string_constant;
    }

    /**
     *  transform a boolean_constant to a constant_value - essentially a no-op, but need a reduction
     *  so we can assign it a cost
     */
    public Object transform_boolean_constant_to_constant(IASNode iNode, Boolean boolean_constant)
    {
        return boolean_constant;
    }

    /**
     *  transform a numeric_constant to a constant_value - essentially a no-op, but need a reduction
     *  so we can assign it a cost
     */
    public Object transform_numeric_constant_to_constant(IASNode iNode, Number numeric_constant)
    {
        return numeric_constant;
    }

    public InstructionList transform_expression_to_conditionalJump(IASNode iNode, InstructionList expression)
    {
        InstructionList result = createInstructionList(iNode, expression.size() + 1);
        result.addAll(expression);
        result.addInstruction(InstructionFactory.getTargetableInstruction(OP_iftrue));
        return result;
    }

    public Object transform_expression_to_constant_value(IASNode iNode, InstructionList expression)
    {
        //  return null - something higher up will report any appropriate diagnostics.
        return null;
    }

    public InstructionList transform_expression_to_void_expression(IASNode iNode, InstructionList expression)
    {
        InstructionList result = createInstructionList(iNode, expression.size() + 1);
        result.addAll(expression);
        result.addInstruction(OP_pop);
        return result;
    }

    public InstructionList transform_integer_constant(IASNode iNode, Integer integer_constant)
    {
        InstructionList result = createInstructionList(iNode, 1);

        pushNumericConstant(result, integer_constant, SemanticUtils.resolveType(iNode, currentScope.getProject()));
        return result;
    }

    public InstructionList transform_long_constant(IASNode iNode, Long long_constant)
    {
        InstructionList result = createInstructionList(iNode, 1);

        pushNumericConstant(result, long_constant, SemanticUtils.resolveType(iNode, currentScope.getProject()));
        return result;
    }

    public Object transform_name_to_constant_value(IASNode iNode)
    {
        currentScope.getMethodBodySemanticChecker().checkConstantValue(iNode);
        
        return SemanticUtils.transformNameToConstantValue(iNode, currentScope.getProject());
    }

    public InstructionList transform_name_to_expression(IASNode iNode, Binding name)
    {

        return generateAccess(name, determineAccessType(iNode));
    }

    public InstructionList transform_non_resolving_identifier(IASNode iNode, String non_resolving_identifier)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addInstruction(OP_pushstring, non_resolving_identifier);
        return result;
    }

    public InstructionList transform_runtime_name_expression(IASNode iNode, RuntimeMultiname runtime_name_expression)
    {
        return runtime_name_expression.generateRvalue(iNode);
    }

    public InstructionList transform_string_constant(IASNode iNode, String string_constant)
    {
        InstructionList result = createInstructionList(iNode, 1);
        result.addInstruction(OP_pushstring, string_constant);
        return result;
    }

    public InstructionList transform_uint_constant(IASNode iNode, Long uint_constant)
    {
        InstructionList result = createInstructionList(iNode, 1);
        pushNumericConstant(result, uint_constant, SemanticUtils.resolveType(iNode, currentScope.getProject()));
        return result;
    }

    /**
     *  Generate a unary operator.
     *  @param operand - the operand expression.
     *  @param opcode - the operator's opcode.
     *  @return an InstructionList that applies the operator to the operand.
     */
    InstructionList unaryOp(IASNode iNode, InstructionList operand, int opcode)
    {
        checkUnaryOp(iNode, opcode);
        InstructionList result = createInstructionList(iNode, operand.size() + 1);
        result.addAll(operand);
        result.addInstruction(opcode);
        return result;
    }

    public void checkUnaryOp (IASNode iNode, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkUnaryOperator(iNode, opcode);
    }

    /**
     *  Walk a syntax tree and note any construct that 
     *  will cause the ABC function to need an activation record.
     */
    void scanFunctionBodyForActivations(IASNode n)
    {
        if ( n != null )
        {
            switch ( n.getNodeID() )
            {
                case WithID:
                case AnonymousFunctionID:
                case FunctionID:
                {
                    currentScope.setNeedsActivation();
                    break;
                }
                default:
                {
                    // Note: There is no need to create an activation
                    // for catch or finally.  See ActivationSanityTest
                    // for examples which prove this.

                    for ( int i = 0; i < n.getChildCount(); i++ )
                        scanFunctionBodyForActivations(n.getChild(i));
                }
            }
        }
    }

    /**
     *  Is this expression a string literal?
     *  @return true if the expression is a string literal, i.e.,
     *  it has one pushstring instruction.
     */
    public static boolean isStringLiteral(InstructionList insns)
    {
        return insns.lastElement().getOpcode() == OP_pushstring;
    }

    /**
     *  Extract the string literal from an InstructionList.
     *  @param insns - the InstructionList of interest.
     *  @return the string literal from insns' final
     *  (and presumably only executable) instruction.
     */
    public static String getStringLiteral(InstructionList insns)
    {
        assert isStringLiteral(insns);
        return insns.lastElement().getOperand(0).toString();
    }

    /**
     *  Work around the linear presentation of XML initializer elements from the parser
     *  by doing ad-hoc pattern matching.
     *  @param exprs - a Vector of expressions.
     *  @return A state transition matrix to generate code for the elements.
     *  @see "ECMA-357 11.1.4 : XML Initialiser"
     *  Expressions may be used to compute parts of an XML initialiser.
     *  Expressions are delimited by curly braces and may appear inside tags or element content.
     *  Inside a tag, expressions may be used to compute a tag name, attribute name, or attribute value.
     *  Inside an element, expressions may be used to compute element content.
     */
    private XMLContentState[] computeXMLContentStateMatrix( Vector<InstructionList> exprs)
    {
        XMLContentState[] result = new XMLContentState[exprs.size()];

        //  First element must be a string or the parser wouldn't have put us here.
        //  But it may be a simple < or </ literal or a <foo literal; the former 
        //  imply that the next expression is going to be a TagName, the latter just
        //  is a TagLiteral that puts us into the middle of things.

        assert isStringLiteral(exprs.elementAt(0)) : exprs.elementAt(0);
        String first_element = getStringLiteral(exprs.elementAt(0));

        if ( first_element.equals("<") || first_element.equals("</") )
            result[0] = XMLContentState.TagStart;
        else
            result[0] = XMLContentState.TagLiteral;

        for ( int i = 1; i < exprs.size(); i++ )
        {
            InstructionList insns = exprs.elementAt(i);
            if ( isStringLiteral(insns) )
            {
                switch ( result[i-1] )
                {
                    case TagStart:
                    case TagLiteral:
                    case TagName:
                    case Attr:
                    case Value:
                    case ValueNeedsEquals:
                    {
                        if ( getStringLiteral(insns).matches(".*>") )
                            result[i] = XMLContentState.TagEnd;
                        else
                            result[i] = XMLContentState.TagLiteral;
                        break;
                    }
                    case TagEnd:
                    case ContentLiteral:
                    case ContentExpression:
                        if ( getStringLiteral(insns).matches("<.*") )
                            result[i] = XMLContentState.TagStart;
                        else
                            result[i] = XMLContentState.ContentLiteral;
                }
            }
            else
            {
                //  Now the fun starts.
                //  The most important thing to do here is to get the
                //  Value, and ContentExpression states right;
                //  these are the entities that require special instructions.
                switch ( result[i-1] )
                {
                    case TagStart:
                        result[i] = XMLContentState.TagName;
                        break;
                    //  An expression following a tag name or
                    //  an attribute value should be an attribute name.
                    case TagName:
                    case Value:
                        result[i] = XMLContentState.Attr;
                        break;
                    //  An expression following an attribute name
                    //  should be an attribute value.
                    case Attr:
                        result[i] = XMLContentState.ValueNeedsEquals;
                        break;
                    case TagEnd:
                    case ContentLiteral:
                    case ContentExpression:
                        result[i] = XMLContentState.ContentExpression;
                        break;
                    case TagLiteral:
                    {
                        //  The literal is free-form input and needs
                        //  to be analyzed.
                        switch ( getXMLLiteralContentState(getStringLiteral(exprs.elementAt(i-1)) ) )
                        {
                            case TagStart:
                                result[i] = XMLContentState.TagName;
                                break;
                            case Attr:
                                //  Since Attr here implies we saw
                                //  a '=' character, the XML string
                                //  won't need one appended.
                                result[i] = XMLContentState.Value;
                                break;
                            case Value: 
                                result[i] = XMLContentState.Attr;
                                break;
                            default:
                                result[i] = XMLContentState.ContentExpression;
                        }
                        break;
                    }
                    default:
                        assert false: "Unhandled case " + result[i-1];
                }
                
            }
        }

        return result;
    }

    /**
     *  Analyze an XML literal and see what state its last character implies.
     *  @return the XMLContentState corresponding to the significance 
     *    of the last literal; e.g., "foo=" returns Attr, "foo" returns Value.
     */
    private XMLContentState getXMLLiteralContentState(String literal)
    {
        char lastChar = literal.charAt(literal.length()-1);

        if ( lastChar == '<' )
        {
            return XMLContentState.TagStart;
        }
        else if ( lastChar ==  '=' )
        {
            return XMLContentState.Attr;
        }

        return XMLContentState.ContentExpression;
    }

    /**
     * @return return the opcode to use to convert the top stack value to a numeric
     * or not
     */
    int op_unplus ()
    {
        return OP_convert_d;
    }

    /**
     * Reduce a array index expression ( a[x] ) in an MXML data binding
     * destination to: a[x] = firstParam
     */
    public InstructionList reduce_arrayIndexExpr_to_mxmlDataBindingSetter(IASNode iNode, InstructionList stem, InstructionList index, boolean isSuper)
    {
        IDynamicAccessNode arrayIndexNode = (IDynamicAccessNode)iNode;
        
        int stemInstructionCount = isSuper ? 1 : stem.size();
        InstructionList result =
            createInstructionList(arrayIndexNode, stemInstructionCount + index.size() + 3 + 1); // +1 for the returnvoid
        
        if (isSuper)
            result.addInstruction(OP_getlocal0);
        else
            result.addAll(stem);
        result.addAll(index);
        result.addInstruction(OP_getlocal1);
        int opCode = isSuper ? OP_setsuper : OP_setproperty;
        result.addInstruction(arrayAccess(arrayIndexNode, opCode));
        return result;
    }

    /**
     * Reduce a member access expression ( a.b ) in an MXML data binding
     * destination to: a.b = firstParam
     */
    public InstructionList reduce_memberAccessExpr_to_mxmlDataBindingSetter(IASNode memberAccessExpr, InstructionList stem, Binding member)
    {
        currentScope.getMethodBodySemanticChecker().checkLValue(memberAccessExpr, member);
        InstructionList result =
            createInstructionList(memberAccessExpr, stem.size() + 2 + 1); // +1 for the returnvoid
        result.addAll(stem);
        result.addInstruction(OP_getlocal1);
        result.addInstruction(OP_setproperty, member.getName());
        return result;
    }

    /**
     * Reduce a qualified member access expression ( a.ns::b ) in an MXML data binding
     * destination to: a.ns::b = firstParam
     */
    public InstructionList reduce_qualifiedMemberAccessExpr_to_mxmlDataBindingSetter(IASNode qualifiedMemberAccessExpr, InstructionList stem, Binding qualifier, Binding member)
    {
        InstructionList result =
            createInstructionList(qualifiedMemberAccessExpr);
        
        //  We may need to work around CMP-751 by recreating 
        //  the correct (trivial) qualifier for base.*::foo.
        Name member_name = member.getName();

        result.addAll(stem);

        if ( !isNamespace(qualifier) )
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
        }
        //  else the qualifier's namespace is already present in the name.
        result.addInstruction(OP_getlocal1);
        result.addInstruction(OP_setproperty, member_name);

        return result;
    }

    /**
     * Reduce a qualified member access expression ( a.ns::["b"] ) in an MXML data binding
     * destination to: a.ns::["b"] = firstParam
     */
    public InstructionList reduce_qualifiedMemberRuntimeNameExpr_to_mxmlDataBindingSetter(IASNode qualifiedMemberRuntimeExpr, InstructionList stem, Binding qualifier, InstructionList runtime_member)
    {
        InstructionList result = createInstructionList(qualifiedMemberRuntimeExpr);

        result.addAll(stem);
        if ( isNamespace(qualifier) )
        {
            result.addAll(runtime_member);
            //  Extract the URI from the namespace and use it to construct a qualified name.
            NamespaceDefinition ns_def = (NamespaceDefinition)qualifier.getDefinition();
            Name qualified_name = new Name(CONSTANT_MultinameL, new Nsset(ns_def.resolveAETNamespace(currentScope.getProject())), null);
            result.addInstruction(OP_getlocal1);
            result.addInstruction(OP_setproperty, qualified_name);
        }
        else
        {
            generateAccess(qualifier, result);
            //  Verifier insists on this.
            result.addInstruction(OP_coerce, namespaceType);
            result.addAll(runtime_member);
            result.addInstruction(OP_getlocal1);
            result.addInstruction(OP_setproperty, new Name(CONSTANT_RTQnameL, null, null));
        }
        return result;
    }

    /**
     * Reduce a runtime name ( nsVar::x ) in an MXML data binding
     * destination to: nsVar::x = firstParam
     * <p>
     * This reduction is used when the qualifer is not a compiler time constant namespace.
     */
    public InstructionList reduceRuntimeName_to_mxmlDataBindingSetter(IASNode runtimeNameNode, RuntimeMultiname runtime_name_expression)
    {
        InstructionList rhs = new InstructionList(1);
        rhs.addInstruction(OP_getlocal1);
        return runtime_name_expression.generateGetOrSet(runtimeNameNode, OP_setproperty, rhs);
    }

    /**
     * Reduce a compile time constant name ( x ) in an MXML data binding
     * destination to: x = firstParam.
     */
    public InstructionList reduceName_to_mxmlDataBindingSetter(IASNode nameNode, Binding name)
    {
        currentScope.getMethodBodySemanticChecker().checkLValue(nameNode, name);
        InstructionList result = new InstructionList(3 + 1); // +1 because of the returnvoid
        result.addAll(currentScope.findProperty(name, true));
        result.addInstruction(ABCConstants.OP_getlocal1);
        result.addInstruction(OP_setproperty, name.getName());
        return result;
    }
    
    /**
     *  Synthesize an array access instruction 
     *  with the correct runtime multiname.
     *  <p>
     *  This handles <code>whatever[...]</code>,
     *  including <code>this[...]</code> and <code>super[...]</code>.
     *  
     *  @param iNode - an IDynamicAccessNode from the construct
     *    that needs an array access instruction.
     *  @param opcode - the opcode of the instruction desired.
     *  @return an instruction with the specified opcode
     *    and the set of open namespaces at the given node.
     */
    private Instruction arrayAccess(IASNode iNode, int opcode)
    {
        ICompilerProject project = currentScope.getProject();

        // Determine if iNode represents super[...] (either as an l-value or an r-value).
        // If so, the namespace set will be slightly different; it will have
        // ProtectedNs(thisClass) instead of ProtectedNs(superClass>.
        IDefinition superDef = null;
        if (iNode instanceof IDynamicAccessNode)
        {
            IExpressionNode arrayNode = ((IDynamicAccessNode)iNode).getLeftOperandNode();
            if (arrayNode instanceof ILanguageIdentifierNode &&
                ((ILanguageIdentifierNode)arrayNode).getKind() == LanguageIdentifierKind.SUPER)
            {
                IIdentifierNode superNode = (IIdentifierNode)arrayNode;
                superDef = superNode.resolveType(project);
            }
        }

        Nsset nsSet = superDef != null ?
                      SemanticUtils.getOpenNamespacesForSuper(iNode, project, superDef) :
                      SemanticUtils.getOpenNamespaces(iNode, project);
        
        Name name = new Name(ABCConstants.CONSTANT_MultinameL, nsSet, null);
        
        return InstructionFactory.getInstruction(opcode, name);
    }

    /**
     *  Determine the proper access type for a name.
     *  @param iNode - the name's AST.
     *  @return AccessType.Lenient if the node is a unqualified
     *    name or the member reference leaf of a member access
     *    node, AccessType.Strict in all other cases.
     */
    private AccessType determineAccessType(IASNode iNode)
    {
        if ( iNode.getParent() instanceof MemberAccessExpressionNode)
        {
            MemberAccessExpressionNode maen = (MemberAccessExpressionNode)iNode.getParent();
            if ( !maen.isMemberReference(iNode) )
            {
                return AccessType.Strict;
            }
        }

        return (this.typeofCount > 0)?
            AccessType.Lenient:
            AccessType.Strict;
    }
}
