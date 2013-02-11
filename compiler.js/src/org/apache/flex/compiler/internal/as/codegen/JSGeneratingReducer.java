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

package org.apache.flex.compiler.internal.as.codegen;

import static org.apache.flex.abc.ABCConstants.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.flex.abc.ABCConstants;
import org.apache.flex.abc.instructionlist.InstructionList;
import org.apache.flex.abc.semantics.ECMASupport;
import org.apache.flex.abc.semantics.Instruction;
import org.apache.flex.abc.semantics.MethodBodyInfo;
import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.abc.semantics.Nsset;
import org.apache.flex.abc.semantics.PooledValue;
import org.apache.flex.abc.visitors.IMethodBodyVisitor;
import org.apache.flex.abc.visitors.IMethodVisitor;
import org.apache.flex.abc.visitors.ITraitsVisitor;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IAppliedVectorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IGetterDefinition;
import org.apache.flex.compiler.definitions.IInterfaceDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.IParameterDefinition;
import org.apache.flex.compiler.definitions.ISetterDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.definitions.references.INamespaceReference;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.exceptions.DuplicateLabelException;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.flex.compiler.internal.definitions.AppliedVectorDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.DefinitionBase;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.GetterDefinition;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.definitions.MemberedDefinition;
import org.apache.flex.compiler.internal.definitions.NamespaceDefinition;
import org.apache.flex.compiler.internal.definitions.PackageDefinition;
import org.apache.flex.compiler.internal.definitions.ParameterDefinition;
import org.apache.flex.compiler.internal.definitions.SetterDefinition;
import org.apache.flex.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.flex.compiler.internal.definitions.VariableDefinition;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.AccessValue;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.ClassificationValue;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.SearchScopeValue;
import org.apache.flex.compiler.internal.legacy.ASScopeUtils;
import org.apache.flex.compiler.internal.legacy.MemberedDefinitionUtils;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.scopes.ASFileScope;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.scopes.ASScopeBase;
import org.apache.flex.compiler.internal.scopes.CatchScope;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.flex.compiler.internal.tree.as.BaseVariableNode;
import org.apache.flex.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.FunctionObjectNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.NamespaceNode;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.internal.tree.as.ParameterNode;
import org.apache.flex.compiler.internal.tree.as.SwitchNode;
import org.apache.flex.compiler.internal.tree.as.VariableExpressionNode;
import org.apache.flex.compiler.internal.tree.as.VariableNode;
import org.apache.flex.compiler.problems.CodegenInternalProblem;
import org.apache.flex.compiler.problems.CodegenProblem;
import org.apache.flex.compiler.problems.CompilerProblem;
import org.apache.flex.compiler.problems.DuplicateLabelProblem;
import org.apache.flex.compiler.problems.DuplicateNamespaceDefinitionProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.InvalidLvalueProblem;
import org.apache.flex.compiler.problems.InvalidOverrideProblem;
import org.apache.flex.compiler.problems.PackageCannotBeUsedAsValueProblem;
import org.apache.flex.compiler.problems.UnknownNamespaceProblem;
import org.apache.flex.compiler.problems.VoidTypeProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.ICatchNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IImportNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.INumericLiteralNode;
import org.apache.flex.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IRegExpLiteralNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ITryNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.as.IWhileLoopNode;
import org.apache.flex.compiler.tree.as.IWithNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.ICompilationUnit.Operation;

/**
 * JSGeneratingReducer is modeled after from ABCGeneratingReducer and called by
 * the generated CmcJSEmitter (see cmc-js.jbg). This is the "meat" of FalconJS.
 * This implementation is part of FalconJS. For more details on FalconJS see
 * org.apache.flex.compiler.JSDriver
 */
@SuppressWarnings("nls")
public class JSGeneratingReducer
{
    /**
     * A very high cost, used to trap errors but otherwise well outside normal
     * bounds.
     */
    static final int ERROR_TRAP = 268435456;

    /**
     * A struct for the decoded pieces of a catch block.
     */
    public class CatchPrototype
    {
        Name catchType;
        Name catchVarName;
        String catchBody;
    }

    /**
     * ConditionalFragment holds the disparate elements of a conditional
     * statement fragment.
     */
    public class ConditionalFragment
    {
        IASNode site;
        String condition;
        String statement;

        // private Label statementLabel = null;

        /**
         * Construct a ConditionalFragment with a single statement.
         */
        ConditionalFragment(IASNode site, String condition, String statement)
        {
            this.site = site;
            this.condition = condition;
            this.statement = statement;
        }

        /**
         * Construct a ConditionalFragment with a list of statements, coalescing
         * them into a composite statement.
         */
        ConditionalFragment(IASNode site, String condition, Vector<String> statements)
        {
            this.site = site;
            this.condition = condition;
            this.statement = createInstruction(site);

            for (String stmt : statements)
            {
                this.statement += stmt;
            }
        }

        /**
         * @return the label of the fragment's condition, or its statement label
         * if it's the default alternative.
         */
        /*
         * Label getLabel() { if ( !this.isUnconditionalAlternative() ) return
         * this.condition.getLabel(); else return getStatementLabel(); }
         */

        /**
         * Cache and return the label of this conditional fragment's statement
         * so it can be retrieved after the statement's been added to the
         * overall conditional statement and this.statement is invalidated.
         */
        /*
         * Label getStatementLabel() { if ( null == this.statementLabel )
         * this.statementLabel = this.statement.getLabel(); return
         * this.statementLabel; }
         */

        /**
         * @return true if this is an unconditional alternative, i.e. a default
         * case or the last else in an if/else if/else.
         */
        boolean isUnconditionalAlternative()
        {
            return null == this.condition;
        }
    }

    /**
     * The RuntimeMultiname class holds the components of the various types of
     * runtime multiname, and puts them together in the right order to generate
     * the names and instruction sequences to jamble up the AVM. Only some
     * permutations of the compile-time/runtime qualifier::name possibilities
     * are valid: Name::expression generates MultinameL expression::expression
     * generates RTQnameL expression::name generates RTQname (name::name is a
     * plain Multiname)
     */
    public class RuntimeMultiname
    {
        /**
         * The compile-time qualifier of a MultinameL. compileTimeName isn't
         * valid (this would be a compile-time constant Multiname).
         */
        Name compileTimeQualifier;

        /**
         * The runtime qualifier of a RTQname or RTQNameL. Compile-time and
         * runtime names are valid.
         */
        String runtimeQualifier;

        /**
         * The compile-time constant name of a RTQName. compileTimeQualifier
         * isn't valid (this would be a compile-time constant Multiname).
         */
        Name compileTimeName;

        /**
         * The runtime name of a MultinameL or RTQnameL.
         */
        String runtimeName;

        /**
         * Construct a RTQnameL-type runtime name.
         * 
         * @param qualifier - the runtime qualifier.
         * @param name - the runtime name.
         */
        RuntimeMultiname(String qualifier, String runtime_name)
        {
            this.runtimeQualifier = qualifier;
            this.runtimeName = runtime_name;
        }

        /**
         * Construct a RTQname-type runtime name.
         * 
         * @param qualifier - the runtime qualifier.
         * @param name - the compile-time name.
         */
        RuntimeMultiname(String qualifier, Name name)
        {
            this.runtimeQualifier = qualifier;
            this.compileTimeName = name;
        }

        /**
         * Construct a MultinameL-type runtime name.
         * 
         * @param qualifier - the Multiname. Note this is the only kind of
         * runtime name that receives a qualifying name.
         * @param nameL - the runtime expression that yields the base name.
         */
        RuntimeMultiname(Name qualifier, String nameL)
        {
            assert (qualifier.getKind() == CONSTANT_MultinameL || qualifier.getKind() == CONSTANT_MultinameLA) : "Bad qualifier kind " + qualifier.getKind() + " on name " + qualifier.toString();
            this.compileTimeQualifier = qualifier;
            this.runtimeName = nameL;
        }

        /**
         * Assemble the ingredients into an expression.
         * 
         * @param opcode - one of OP_getproperty or OP_setproperty.
         * @param rhs - the value of a setproperty call. Passed to this routine
         * because there may be name-specific instructions before and after the
         * value.
         */
        private String generateGetOrSet(IASNode iNode, int opcode, String rhs)
        {
            //  TODO: Optimize InstructionList size.
            String result = createInstruction(iNode);

            //  Note: numerous microoptimization opportunities here
            //  to avoid storing values in temps by creative use of
            //  OP_dup and OP_swap, especially in getproperty gen.

            if (this.compileTimeQualifier != null && this.runtimeName != null)
            {
                result += runtimeName;
                if (rhs != null)
                    result += rhs;

                /*
                 * // Generate MultinameL type code. Binding name_temp =
                 * currentScope.allocateTemp(); result.addAll(this.runtimeName);
                 * result.addInstruction(OP_dup);
                 * result.addInstruction(name_temp.setlocal()); //
                 * findprop(MultinameL) consumes a name from the value stack.
                 * result.addInstruction(OP_findpropstrict,
                 * this.compileTimeQualifier);
                 * result.addInstruction(name_temp.getlocal()); if ( rhs != null
                 * ) result.addAll(rhs); // get/setprop(MultinameL) consumes a
                 * name from the value stack. result.addInstruction(opcode,
                 * this.compileTimeQualifier);
                 */

            }
            else if (this.runtimeQualifier != null && this.runtimeName != null)
            {
                /*
                 * // Generate RTQnameL type code. Binding name_temp =
                 * currentScope.allocateTemp(); Binding qual_temp =
                 * currentScope.allocateTemp(); Name rtqnl = new
                 * Name(CONSTANT_RTQnameL, null, null);
                 * result.addAll(getRuntimeName(iNode));
                 * result.addInstruction(name_temp.setlocal());
                 * result.addAll(getRuntimeQualifier(iNode));
                 * result.addInstruction(OP_dup);
                 * result.addInstruction(qual_temp.setlocal());
                 * result.addInstruction(name_temp.getlocal()); //
                 * findprop(RTQNameL) consumes namespace and name from the value
                 * stack. result.addInstruction(OP_findpropstrict, rtqnl);
                 * result.addInstruction(qual_temp.getlocal());
                 * result.addInstruction(name_temp.getlocal()); if ( rhs != null
                 * ) result.addAll(rhs); // get/setprop(RTQNameL) consumes
                 * namespace and name from the value stack.
                 * result.addInstruction(opcode, rtqnl);
                 * result.addAll(currentScope.releaseTemp(name_temp));
                 */

                result += getRuntimeName(iNode);
                if (rhs != null)
                    result += rhs;
            }
            else
            {
                //  Last valid combination generates a RTQname.
                assert (this.runtimeQualifier != null && this.compileTimeName != null) : "Unknown runtime name configuration: " + this.toString();

                result += getRuntimeName(iNode);
                if (rhs != null)
                    result += rhs;

                /*
                 * Name rtqn = new Name(CONSTANT_RTQname, null,
                 * this.compileTimeName.getBaseName());
                 * result.addAll(getRuntimeQualifier(iNode));
                 * result.addInstruction(OP_dup); // findprop(RTQName) consumes
                 * a namespace from the value stack.
                 * result.addInstruction(OP_findpropstrict, rtqn);
                 * result.addInstruction(OP_swap); if ( rhs != null )
                 * result.addAll(rhs); // get/setprop(RTQName) consumes a
                 * namespace from the value stack. result.addInstruction(opcode,
                 * rtqn);
                 */
            }

            return result;
        }

        /**
         * Generate the InstructionList that expresses this RuntimeName's
         * qualifier.
         * 
         * @param iNode - an IASNode to contribute debug info to the result
         * InstructionList.
         * @return the runtime qualifier setup instructions.
         */
        public String getRuntimeQualifier(IASNode iNode)
        {
            assert (hasRuntimeQualifier());
            String result = createInstruction(iNode);

            result += this.runtimeQualifier;

            /*
             * // Ensure the last instruction is an OP_coerce to Namespace.
             * Instruction last = result.lastElement(); if ( last.getOpcode() !=
             * OP_coerce || last.getOperandCount() == 0 || !
             * namespaceType.equals(last.getOperand(0)) ) {
             * result.addInstruction(OP_coerce, namespaceType); }
             */
            return result;
        }

        /**
         * @return true if this RuntimeName has a runtime qualifier.
         */
        public boolean hasRuntimeQualifier()
        {
            return this.runtimeQualifier != null;
        }

        /**
         * @return true if this RuntimeName has a runtime name.
         */
        public boolean hasRuntimeName()
        {
            return this.runtimeName != null;
        }

        /**
         * Generate the InstructionList that expresses this RuntimeName's name.
         * 
         * @param iNode - an IASNode to contribute debug info to the result
         * InstructionList.
         * @return the runtime name setup instructions.
         */
        public String getRuntimeName(IASNode iNode)
        {
            assert (hasRuntimeName());

            String result = createInstructionList(iNode);

            /*
             * result.addAll(replicate(this.runtimeName)); if (
             * result.lastElement().getOpcode() != OP_coerce_s)
             * result.addInstruction(OP_coerce_s);
             */

            result += this.runtimeName;

            return result;
        }

        /**
         * Generate the runtime name appropriate to this qualifier/name
         * combination.
         * 
         * @return the runtime name appropriate to this qualifier/name
         * combination.
         */
        public Name generateName()
        {
            Name result;

            if (this.compileTimeQualifier != null)
            {
                result = this.compileTimeQualifier;
            }
            else if (this.runtimeQualifier != null && this.runtimeName != null)
            {
                result = new Name(CONSTANT_RTQnameL, null, null);
            }
            else
            {
                //  Last valid combination generates a RTQname.
                assert (this.runtimeQualifier != null && this.compileTimeName != null) : "Unknown runtime name configuration: " + this.toString();

                result = new Name(CONSTANT_RTQname, null, this.compileTimeName.getBaseName());
            }

            return result;
        }

        /**
         * Convenience method generates an assignment
         * 
         * @param value - the value to set.
         * @return the instruction sequence to set a the given multiname's
         * value.
         */
        String generateAssignment(IASNode iNode, String value)
        {
            return generateGetOrSet(iNode, OP_setproperty, value);
        }

        /**
         * Convenience method generates an r-value.
         * 
         * @return the instruction sequence to look up the given multiname.
         */
        String generateRvalue(IASNode iNode)
        {
            return generateGetOrSet(iNode, OP_getproperty, null);
        }

        /**
         * Diagnostic toString() method, used primarily to debug the assertion
         * in generate().
         */
        @Override
        public String toString()
        {
            return "\n\t" + compileTimeQualifier + ",\n\t" + runtimeQualifier + ",\n\t" + compileTimeName + ",\n\t" + runtimeName;
        }
    }

    /**
     * The current LexicalScope. Set by the caller, changes during reduction
     * when a scoped construct such as an embedded function or a with statement
     * is encountered.
     */
    private LexicalScope currentScope;

    /**
     * Strings sent by the caller to be added to a function definition. Usually
     * these are field initialization expressions in a constructor.
     */
    // private String instanceInitializers;

    /**
     * A shared name for the Namespace type.
     */
    public static final Name namespaceType = new Name("Namespace");

    /**
     * A shared name for the XML type.
     */
    public static final Name xmlType = new Name("XML");

    /**
     * A shared name for the XMLList type.
     */
    public static final Name xmlListType = new Name("XMLList");

    /**
     * A shared name for the RegExp type.
     */
    public static final Name regexType = new Name("RegExp");

    /**
     * A shared name for the void type.
     */
    public static final Name voidType = new Name("void");

    /**
     * Generate code to push a numeric constant.
     */
    public static void pushNumericConstant(long value, String result_list)
    {
        // do nothing
        // result_list.pushNumericConstant(value);
    }

    /**
     * Active labeled control-flow region's substatements. Populated by the
     * labeledStmt Prologue, and winnowed by the labeledStmt reduction's
     * epilogue.
     */
    private Set<IASNode> labeledNodes = new HashSet<IASNode>();

    /**
     * @return the currently active collection of problems.
     */
    public Collection<ICompilerProblem> getProblems()
    {
        return currentScope.getProblems();
    }

    /**
     * Generate a binary operator.
     * 
     * @param l - the left-hand operand.
     * @param r - the right-hand operand.
     * @param opcode - the operator's opcode.
     * @return the combined String sequence with the operator appended.
     */
    String binaryOp(IASNode iNode, String l, String r, int opcode)
    {
        checkBinaryOp(iNode, opcode);

        switch (opcode)
        {
            //  Binary logical operators.
            case OP_istypelate:
                return reduce_istypeExprLate(iNode, l, r);
            case OP_astypelate:
                return reduce_astypeExprLate(iNode, l, r);
            case OP_instanceof:
                return reduce_instanceofExpr(iNode, l, r);
            case OP_in:
                return reduce_inExpr(iNode, l, r);
        }

        final String operator = opToString(iNode, opcode);
        String result = createInstruction(iNode, l.length() + r.length() + 1);
        result += binaryOp(l.toString(), r.toString(), operator);
        return result;

        /*
         * String result = createInstruction(iNode, l.size() + r.size() + 1);
         * result.addAll(l); result.addAll(r); result.addInstruction(opcode);
         * return result;
         */
    }

    public void checkBinaryOp(IASNode iNode, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkBinaryOperator(iNode, opcode);
    }

    public String conditionalJump(IASNode iNode, String l, String r, int opcode)
    {
        return binaryOp(iNode, l, r, opcode);
    }

    String binaryOp(String l, String r, String operator)
    {
        String result = "(" + stripTabs(stripNS(l.toString())) + " " + operator + " " + stripTabs(stripNS(r.toString())) + ")";
        return result;
    }

    /**
     * Resolve a dotted name, e.g., foo.bar.baz
     */
    Binding dottedName(IASNode iNode, String qualifiers, String base_name)
    {
        if (iNode instanceof IdentifierNode)
        {
            return currentScope.resolveName((IdentifierNode)iNode);
        }
        else if (iNode instanceof ExpressionNodeBase)
        {
            ExpressionNodeBase expr = (ExpressionNodeBase)iNode;
            ICompilerProject project = currentScope.getProject();
            Name n = expr.getMName(project);

            if (n == null)
            {
                currentScope.addProblem(new CodegenInternalProblem(iNode, "Unable to resove member name: " + iNode.toString()));
                n = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, qualifiers)), base_name);
            }

            return currentScope.getBinding(iNode, n, expr.resolve(project));
        }

        //else
        currentScope.addProblem(new CodegenInternalProblem(iNode, "Unable to resove to a dotted name: " + iNode.toString()));
        return new Binding(iNode, new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, qualifiers)), base_name), null);
    }

    /**
     * Resolve a dotted name, e.g., foo.bar.baz, where the whole dotted name is
     * a package this is an error, and a diagnostic will be emitted
     */
    Binding errorPackageName(IASNode iNode, String qualifiers, String base_name)
    {
        currentScope.addProblem(new PackageCannotBeUsedAsValueProblem(iNode, qualifiers + "." + base_name));
        return new Binding(iNode, new Name(qualifiers + "." + base_name), null);
    }

    /**
     * Common routine used by reductions that need an empty list.
     * 
     * @param iNode - the current AST node.
     */
    public String createInstruction(IASNode iNode)
    {
        return createInstruction(iNode, 0);
    }

    /**
     * Common routine used by reductions that need an empty list.
     * 
     * @param iNode - the current AST node.
     * @param capacity - requested capacity of the new list. May be adjusted to
     * accomodate debug Strings.
     */
    public String createInstruction(IASNode iNode, int capacity)
    {
        String result = "";

        String file_name = iNode.getSourcePath();

        if (file_name == null)
        {
            //  Fall back on the file specification.
            //  This may also be null (or assert if it's feeling moody),
            //  in which case the file name remains null and file/line
            //  processing noops.
            try
            {
                IFileSpecification fs = iNode.getFileSpecification();
                if (fs != null)
                    file_name = fs.getPath();
            }
            catch (Throwable no_fs)
            {
                //  No file specification available, probably
                //  because this node is some kind of syntho.
            }
        }

        //  Note: getLine() uses zero-based counting.
        int line_num = iNode.getLine() + 1;

        //  Adjust the capacity requirement if debug
        //  Strings are to be emitted.
        if (currentScope.emitFile(file_name))
            capacity++;
        if (currentScope.emitLine(line_num))
            capacity++;

        //  If the required capacity is less than three
        //  Strings, the String can hold 
        //  them organically.  Specifying a capacity to
        //  the String ctor causes it to allocate
        //  a separate ArrayList.
        if (capacity > 3)
            result = new String(/* capacity */);
        else
            result = new String();

        if (currentScope.emitFile(file_name))
        {
            // result.addInstruction(OP_debugfile, file_name);
            currentScope.setDebugFile(file_name);
        }

        if (currentScope.emitLine(line_num))
        {
            // result.addInstruction(OP_debugline, line_num);
            currentScope.setDebugLine(line_num);
        }

        return result;
    }

    /**
     * Error trap.
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
     * Error trap.
     */
    public String error_reduce_Op_AssignId(IASNode iNode, String non_lvalue, String rvalue)
    {
        currentScope.addProblem(new InvalidLvalueProblem(iNode));
        return createInstruction(iNode);
    }

    /**
     * Generate access to a named entity.
     * 
     * @param name - the entity's name.
     * @return an instruction sequence to access the entity.
     */
    String generateAccess(IASNode iNode, Binding name)
    {
        StringBuilder result = new StringBuilder();
        generateAccess(iNode, name, result);
        return result.toString();
    }

    /**
     * Generate access to a named entity.
     * 
     * @param name - the entity's name.
     * @param result - the instruction sequence to generate into.
     */
    void generateAccess(IASNode iNode, Binding binding, StringBuilder result)
    {
        if (binding.isLocal())
        {
            final String name = bindingToString(iNode, binding, false, false);
            result.append(name);
        }
        else
        {
            assert (binding.getName() != null) : "non-local Binding " + binding + " must have a name";
            currentScope.getMethodBodySemanticChecker().checkGetProperty(binding);
            // generateAccess( iNode, binding.getName(), result);
            final String name = bindingToString(iNode, binding, true, true);
            result.append(name);
        }
    }

    /**
     * Generate access to a named entity.
     * 
     * @param name - the entity's name.
     * @param result - the instruction sequence to generate into.
     */
    void generateAccess(IASNode iNode, Name name, StringBuilder result)
    {
        final String str = nameToString(iNode, name, true, true);
        result.append(str);

        /*
         * if ( name.isTypeName() ) { generateAccess(name.getTypeNameBase(),
         * result); generateTypeNameParameter(name.getTypeNameParameter(),
         * result); result.addInstruction(OP_applytype, 1); } else {
         * result.addInstruction(OP_findpropstrict, name);
         * result.addInstruction(OP_getproperty, name); }
         */
    }

    /**
     * Generate code to assign to a named entity.
     * 
     * @param target - the entity to be assigned to.
     * @param rvalue - the value to assign.
     * @return an String sequence that stores the given rvalue in the target.
     */
    String generateAssignment(IASNode iNode, Binding target, String rvalue)
    {
        return generateAssignment(iNode, target, rvalue, false);
    }

    /**
     * Generate code to assign to a named entity.
     * 
     * @param target - the entity to be assigned to.
     * @param rvalue - the value to assign.
     * @param need_value - when true, leave a DUP of the rvalue on the stack.
     * @return an String sequence that stores the given rvalue in the target,
     * and leaves a DUP of the rvalue on the stack if need_value is set.
     */
    String generateAssignment(IASNode iNode, Binding target, String rvalue, boolean need_value)
    {
        String result = createInstruction(iNode, rvalue.length() + 4);

        result += rvalue;

        /*
         * result.addAll(rvalue); if ( need_value )
         * result.addInstruction(OP_dup); if ( target.isLocal() ) { if (
         * target.getDefinition() != null ) { ITypeDefinition type =
         * target.getDefinition
         * ().resolveType(currentScope.getDefinitionCache()); if ( type != null
         * && type != ClassDefinition.getAnyTypeClassDefinition() ) {
         * result.addInstruction(OP_coerce,
         * ((DefinitionBase)type).getMName(currentScope.getProject())); } }
         * result.addInstruction(target.setlocal()); } else {
         * result.addInstruction(OP_findproperty, target.getName());
         * result.addInstruction(OP_swap); result.addInstruction(OP_setproperty,
         * target.getName()); }
         */
        return result;
    }

    /**
     * Generate a catch block.
     * 
     * @param catch_proto - the catch block's prototype.
     */
    /*
     * String generateCatchBlock( Label try_start, Label try_end, CatchPrototype
     * catch_proto) { String scope_reinit =
     * currentScope.getFlowManager().getScopeStackReinit(); String current_catch
     * = new String(catch_proto.catchBody.size() + scope_reinit.size() + 15); //
     * Common prologue code. current_catch.addInstruction(OP_getlocal0);
     * current_catch.addInstruction(OP_pushscope); if(
     * currentScope.needsActivation() ) { // Restore the activation object.
     * current_catch
     * .addInstruction(currentScope.getActivationStorage().getlocal());
     * current_catch.addInstruction(OP_pushscope); } // Re-establish enclosing
     * exception scopes. current_catch.addAll(scope_reinit); int handler_number
     * = currentScope.getMethodBodyVisitor().visitException(try_start, try_end,
     * current_catch.getLabel(), catch_proto.catchType,
     * catch_proto.catchVarName); Binding exception_storage =
     * currentScope.getFlowManager().getFinallyContext().getExceptionStorage();
     * current_catch.addInstruction(OP_newcatch, handler_number);
     * current_catch.addInstruction(OP_dup); if ( exception_storage != null ) {
     * current_catch.addInstruction(exception_storage.setlocal());
     * current_catch.addInstruction(OP_dup); }
     * current_catch.addInstruction(OP_pushscope);
     * current_catch.addInstruction(OP_swap);
     * current_catch.addInstruction(OP_setslot, 1);
     * current_catch.addAll(catch_proto.catchBody); if (
     * current_catch.canFallThrough() || current_catch.hasPendingLabels() ) {
     * current_catch.addInstruction(OP_popscope); } return current_catch; }
     */

    /**
     * Generate a compound assignment statement.
     */
    String generateCompoundAssignment(IASNode iNode, Binding operand, String expr, int opcode, boolean need_value)
    {
        String result = createInstruction(iNode, expr.length() + (operand.isLocal() ? 4 : 8));
        /*
         * if ( operand.isLocal() ) { result.addInstruction(operand.getlocal());
         * result.addAll(expr); result.addInstruction(opcode); if ( need_value )
         * result.addInstruction(OP_dup);
         * result.addInstruction(operand.setlocal()); } else { Binding
         * value_temp = null; result.addInstruction(OP_findpropstrict,
         * operand.getName()); result.addInstruction(OP_dup);
         * result.addInstruction(OP_getproperty, operand.getName());
         * result.addAll(expr); result.addInstruction(opcode); if ( need_value )
         * { value_temp = currentScope.allocateTemp();
         * result.addInstruction(OP_dup);
         * result.addInstruction(value_temp.setlocal()); }
         * result.addInstruction(OP_setproperty, operand.getName()); if (
         * need_value ) { result.addInstruction(value_temp.getlocal());
         * result.addInstruction(value_temp.kill()); } }
         */
        final String op = opToString(iNode, opcode);
        final String methodName = bindingToString(iNode, operand, true, true);
        if (methodName.contains("."))
        {
            final String stem = methodName.substring(0, methodName.lastIndexOf("."));
            final String member = getBasenameFromBinding(operand);
            result += resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, member), op + "=", expr, false, true);
        }
        else
        {
            result += binaryOp(methodName, expr, op + "=");
        }

        if (need_value)
            result = "(" + result + ")";
        else
            result += ";" + endl();

        return result;
    }

    String generateCompoundBracketAssignment(IASNode iNode, String stem, String index, String expr, int opcode, boolean need_value)
    {
        String result = createInstruction(iNode, stem.length() * 2 + index.length() + expr.length() + 11);

        /*
         * // Although ASC evaluates the stem twice, it only evaluates the index
         * once. // TODO: Inspect the index expression for side effects so the
         * temp can be // elided in most cases. Binding index_temp =
         * currentScope.allocateTemp(); result.addAll(index);
         * result.addInstruction(index_temp.setlocal());
         * result.addAll(replicate(stem));
         * result.addInstruction(index_temp.getlocal());
         * result.addInstruction(OP_getproperty); result.addAll(expr);
         * result.addInstruction(opcode); Binding value_temp =
         * currentScope.allocateTemp();
         * result.addInstruction(value_temp.setlocal()); result.addAll(stem);
         * result.addInstruction(index_temp.getlocal());
         * result.addInstruction(value_temp.getlocal());
         * result.addInstruction(OP_setproperty); if ( need_value )
         * result.addInstruction(value_temp.getlocal());
         * result.addAll(currentScope.releaseTemp(value_temp));
         * result.addAll(currentScope.releaseTemp(index_temp));
         */

        // String result = binaryOp(stem + "[" + index + "]", expr, opcode + "="); 
        final String op = opToString(iNode, opcode);
        result += resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, index), op + "=", expr, true, true);
        if (need_value)
            result = "(" + result + ")";
        else
            result += ";" + endl();
        return result;
    }

    String generateCompoundMemberAssignment(IASNode iNode, String stem, Binding member, String expr, int fetch_opcode, int assign_opcode, boolean need_value)
    {
        String result = createInstruction(iNode, stem.length() * 2 + expr.length() + 5);

        /*
         * // TODO: Depending on the resolution of ASC-4159 and // the
         * corresponding Falcon backwards compatibility // issue, cache the stem
         * expression in a local to avoid // multiple evaluations.
         * result.addAll(replicate(stem)); result.addInstruction(fetch_opcode,
         * member.getName()); result.addAll(expr);
         * result.addInstruction(assign_opcode); if ( need_value ) {
         * result.addInstruction(OP_dup); } result.addAll(stem);
         * result.addInstruction(OP_swap); result.addInstruction(OP_setproperty,
         * member.getName());
         */

        // String result = binaryOp(stem + "." + getBasenameFromBinding( member ), expr, opcode + "="); 
        final String op = opToString(iNode, assign_opcode);
        result += resolveSetterName(iNode, stem, member, op + "=", expr, false, true);
        if (need_value)
            result = "(" + result + ")";
        else
            result += ";" + endl();
        return result;
    }

    String generateCompoundAssignmentToRuntimeName(IASNode iNode, RuntimeMultiname name, String expr, int opcode, boolean need_value)
    {
        String rhs = opToString(iNode, opcode) + "= " + expr;

        return name.generateAssignment(iNode, rhs);
    }

    /**
     * Generate a compound logical assignment expression to a named lvalue.
     * 
     * @param iNode - the assignment operator (root of the subtree).
     * @param lvalue - the lvalue's name.
     * @param expr - the expression to assign.
     * @param is_and - true if the expression is &amp;&amp;=, false if it's ||=.
     * @param need_value - true if the expression's not used in a void context.
     */
    String generateCompoundLogicalAssignment(IASNode iNode, Binding lvalue, String expr, boolean is_and, boolean need_value)
    {
        String result = createInstructionList(iNode);

        /*
         * Label tail = new Label(); int failure_test = is_and? OP_iffalse :
         * OP_iftrue; if ( lvalue.isLocal() ) { // Fetch and test the current
         * value. result.addInstruction(lvalue.getlocal()); // The current value
         * may not be the result value, // but for now assume it is. if (
         * need_value ) result.addInstruction( OP_dup); result.addInstruction(
         * failure_test, tail ); // Test succeeded: reset the value, but first
         * // pop the value speculatively dup'd above. if ( need_value )
         * result.addInstruction(OP_pop); result.addAll(expr); if ( need_value )
         * result.addInstruction(OP_dup);
         * result.addInstruction(lvalue.setlocal()); } else { // Fetch,
         * speculatively dup, and test the current value.
         * result.addInstruction(OP_findpropstrict, lvalue.getName());
         * result.addInstruction(OP_getproperty, lvalue.getName()); if (
         * need_value ) result.addInstruction(OP_dup);
         * result.addInstruction(failure_test, tail); if ( need_value )
         * result.addInstruction(OP_pop); result.addAll(expr); if ( need_value )
         * { result.addInstruction(OP_dup); }
         * result.addInstruction(OP_findpropstrict, lvalue.getName());
         * result.addInstruction(OP_swap); result.addInstruction(OP_setproperty,
         * lvalue.getName()); } result.labelNext(tail);
         */

        final String lvalStr = bindingToString(iNode, lvalue, true, true);
        result += lvalStr + " = (" + lvalStr + ") ";
        if (is_and)
            result += "&& ";
        else
            result += "|| ";
        result += "(" + expr + ");\n";

        return result;
    }

    /**
     * Generate compound logical assignment to a runtime name, e.g., n::x ||=
     * foo;
     * 
     * @param iNode - the root of the assignment subtree.
     * @param name - the runtime name.
     * @param expr - the second operand of the implied binary expression.
     * @param is_and - true if the result is set to the second operand iff the
     * first operand is true.
     * @param need_value - true if the value of the assignment is required.
     */
    String generateCompoundLogicalRuntimeNameAssignment(IASNode iNode, RuntimeMultiname name, String expr, boolean is_and, boolean need_value)
    {
        return (is_and) ?
                String.format("%s &&= %s", name.generateRvalue(iNode), expr)
                :
                String.format("%s ||= %s", name.generateRvalue(iNode), expr);
    }

    /**
     * Generate a compound logical assignment expression to a a[i] type lvalue.
     * 
     * @param iNode - the assignment operator (root of the subtree).
     * @param stem - the expression that generates the lvalue's stem, e.g., a in
     * a[i]
     * @param index - the index expression.
     * @param expr - the expression to assign.
     * @param is_and - true if the expression is &amp;&amp;=, false if it's ||=.
     * @param need_value - true if the expression's not used in a void context.
     */
    String generateCompoundLogicalBracketAssignment(IASNode iNode, String stem, String index, String expr, boolean is_and, boolean need_value)
    {
        String result = createInstructionList(iNode);

        /*
         * String result = createInstructionList(iNode, stem.size() * 2 +
         * index.size() + expr.size() + 11 ); Label tail = new Label(); int
         * failure_test = is_and? OP_iffalse : OP_iftrue; // Although ASC
         * evaluates the stem twice, it only evaluates the index once. // TODO:
         * Inspect the index expression for side effects so the temp can be //
         * elided in most cases. Binding index_temp =
         * currentScope.allocateTemp(); result.addAll(index);
         * result.addInstruction(index_temp.setlocal());
         * result.addAll(replicate(stem));
         * result.addInstruction(index_temp.getlocal()); result.addAll(stem);
         * result.addInstruction(index_temp.getlocal());
         * result.addInstruction(OP_getproperty); // Assume this is the result.
         * result.addInstruction(OP_dup); result.addInstruction(failure_test,
         * tail); // Pop the speculative result and assign the correct one.
         * result.addInstruction(OP_pop); result.addAll(expr);
         * result.labelNext(tail); Binding value_temp = null; if ( need_value )
         * { value_temp = currentScope.allocateTemp();
         * result.addInstruction(OP_dup);
         * result.addInstruction(value_temp.setlocal()); }
         * result.addInstruction(OP_setproperty); if ( need_value ) {
         * result.addInstruction(value_temp.getlocal());
         * result.addAll(currentScope.releaseTemp(value_temp)); }
         * result.addAll(currentScope.releaseTemp(index_temp));
         */

        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "logical bracket assignment"));
        return result;
    }

    /**
     * Generate a compound logical assignment expression to a foo.bar type
     * lvalue
     * 
     * @param iNode - the assignment operator (root of the subtree).
     * @param stem - the expression that generates the lvalue's stem, e.g., a in
     * a[i]
     * @param index - the index expression.
     * @param expr - the expression to assign.
     * @param is_and - true if the expression is &amp;&amp;=, false if it's ||=.
     * @param need_value - true if the expression's not used in a void context.
     */
    String generateCompoundLogicalMemberAssignment(IASNode iNode, String stem, Binding member, String expr, int fetch_opcode, boolean is_and, boolean need_value)
    {
        String result = createInstructionList(iNode);

        /*
         * Label tail = new Label(); int failure_test = is_and? OP_iffalse :
         * OP_iftrue; result.addAll(replicate(stem)); result.addAll(stem);
         * result.addInstruction(OP_getproperty, member.getName()); // Assume
         * this is the result. result.addInstruction(OP_dup);
         * result.addInstruction(failure_test, tail);
         * result.addInstruction(OP_pop); result.addAll(expr);
         * result.labelNext(tail); Binding value_temp = null; if ( need_value )
         * { value_temp = currentScope.allocateTemp();
         * result.addInstruction(OP_dup);
         * result.addInstruction(value_temp.setlocal()); }
         * result.addInstruction(OP_setproperty, member.getName()); if (
         * need_value ) { result.addInstruction(value_temp.getlocal());
         * result.addAll(currentScope.releaseTemp(value_temp)); }
         */

        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "logical member assignment"));
        return result;

    }

    /**
     * Generate a for/in or for each/in loop.
     * 
     * @param iNode - the for/in or for/each node.
     * @param opcode - OP_nextname or OP_nextvalue.
     * @param it - the name of the iterator variable, e.g., v in for (v in foo)
     * {f(v);}.
     * @param base - the source of names/values, e.g., foo in for (v in foo)
     * {f(v);}.
     * @param body - the loop body, e.g., {f(v);} in for (v in foo) {f(v);}.
     */
    private String generateForKeyOrValueLoop(IASNode iNode, int opcode, Binding it, String base, String body)
    {
        /*
         * String result = new String(body.size() + base.size() + 15); Label
         * test = new Label(); Label loop = new Label(); // Set up the object
         * and index registers. LexicalScope.Hasnext2Wrapper hasnext =
         * currentScope.hasnext2(); result.addInstruction(OP_pushbyte, 0);
         * result.addInstruction(hasnext.index_temp.setlocal());
         * result.addAll(base); result.addInstruction(OP_coerce_a);
         * result.addInstruction(hasnext.stem_temp.setlocal()); // Go to the
         * loop test. result.addInstruction(OP_jump, test); // Top of loop
         * processing. result.addInstruction(OP_label);
         * result.labelCurrent(loop); String nextvalue = new String(3);
         * result.addInstruction(hasnext.stem_temp.getlocal());
         * result.addInstruction(hasnext.index_temp.getlocal());
         * nextvalue.addInstruction(opcode);
         * result.addAll(generateAssignment(iNode, it, nextvalue));
         * result.addAll(body); // Loop test. // It needs its own list so the
         * continue target // can be properly resolved; this label is always //
         * attached to the head of an String. String test_list = new String(1);
         * test_list.addInstruction(hasnext.String);
         * test_list.labelCurrent(test);
         * currentScope.getFlowManager().resolveContinueLabel(test_list);
         * test_list.addInstruction(OP_iftrue, loop); result.addAll(test_list);
         * result.addAll(hasnext.release());
         */

        String result = new String();
        return result;
    }

    /**
     * generateFunctionBody() wrapper suitable for calling from the BURM. See
     * JBurg ENHRQ <N> : the grammar that accepts the BURM's parameters to a
     * JBurg.Reduction routine doesn't grok function calls, so the BURM cannot
     * call generateFunctionBody(body, name.getName());
     */
    String generateFunctionBody(IASNode iNode, String function_body, Binding return_type)
    {
        return generateFunctionBody(iNode, function_body, return_type != null ? return_type.getName() : null);
    }

    /**
     * Generate boilerplate function prolog/epilog code.
     * 
     * @param block - the actual CFG.
     * @param return_type - the function's return type. Not presently used.
     */
    String generateFunctionBody(IASNode iNode, String function_body, Name return_type)
    {
        currentScope.getMethodInfo().setReturnType(return_type);
        String result = "";
        final String methodName = getMethodName();
        final Boolean isAnonymousFunction = isAnonymousFunction(methodName);
        final Boolean isCtor = methodName != null && methodName.equals(getCurrentClassName());

        if (return_type != null)
            usedTypes.add(getBasenameFromName(return_type));

        // AJH removed use of 'self'
        /*
         * !isAnonymousFunction is necessary. AS and JS are different in the way
         * "this" is being treated in anonymous functions. i.e. public function
         * whoIsThis() : void { const localFunction : Function = funciton():void
         * { this.callMe(); }; } In this example this.callMe() refers to the
         * callMe method of the class. In JS "this" refers to the local
         * function! By supressing "var self = this;" for anonymous functions
         * we'll emulate the AS behavior. The JS result then looks like this:
         * tests.Test.whoIsThis.prototype = function() { var self = this; const
         * localFunction : Function = funciton():void { // NOT EMITTED: var self
         * = this; self.callMe(); }; };
         */
        //if (!JSSharedData.m_useSelfParameter && !isAnonymousFunction &&
        //        /* TODO: !needsSelfParameter(createFullClassName(false)) && */function_body.contains(JSSharedData.THIS))
        //{
            // final Binding fullClassName = makeBinding(createFullClassName(false));
            // this.registerLocalVariable(currentScope, makeBinding("self"), fullClassName, fullClassName);

        //    final Binding fullClassName = new Binding(iNode, makeName(createFullClassName(false)), null);
        //    this.registerLocalVariable(currentScope, new Binding(iNode, makeName("self"), null), fullClassName, fullClassName);
        //    result += indentBlock("/** @type {" + createFullClassName(true) + "} */" + endl(), 1);
        //    result += indentBlock("var " + JSSharedData.THIS + " = this;" + endl(), 1);
        //}

        //  Constructor-specific processing: add the instance initializers,
        //  add a constructsuper call if none exists.
        if (haveAPrioriInstructions() && !isAnonymousFunction)
        {
            result += indentBlock(aPrioriInstructions, 1);

            //  If this is a constructor and there's no explicit
            //  super() call, synthesize one.
            //  Note that this may be a semantic error if the 
            //  superclass' constructor needs arguments.
            /*
             * if ( isCtor && !block.contains("super(") ) { // Call the
             * superclass' constructor after the instance // init instructions;
             * this doesn't seem like an abstractly // correct sequence, but
             * it's what ASC does. result += "" // TODO: //
             * result.addInstruction(OP_getlocal0); //
             * result.addInstruction(OP_constructsuper, 0); }
             */
        }

        if (!function_body.isEmpty())
        	result += indentBlock(function_body, 1);

        // popIndent();
        // result += indent() + "}";
        return result;
    }

    private IFunctionNode getFunctionNodeFromNode(IASNode iNode)
    {
        IFunctionNode fn = null;
        if (iNode instanceof FunctionObjectNode)
        {
            FunctionObjectNode afn = (FunctionObjectNode)iNode;
            fn = afn.getFunctionNode();
        }
        else if (iNode instanceof IFunctionNode)
        {
            fn = (IFunctionNode)iNode;
        }

        if (fn == null)
            currentScope.addProblem(new JSInternalCompilerProblem(iNode, "cannot extract function node"));

        return fn;
    }

    private IFunctionDefinition getFunctionDefinitionFromNode(IASNode iNode)
    {
        final IFunctionNode fn = getFunctionNodeFromNode(iNode);
        return fn.getDefinition();
    }

    /**
     * Generate a named nested function.
     * 
     * @param func_name - the function's name.
     * @param return_type - the function's return type.
     * @param function_body - the body of the function.
     * @pre the function's lexical scope must be at the top of the lexical scope
     * stack, with the declaring function's lexical scope under it.
     * @post the nested function's MethodInfo is filled in, the function body is
     * attached to its MethodBodyInfo, and the declaring function's
     * initialization sequence gets code to declare the function.
     */
    private String generateNestedFunction(IASNode iNode, Binding func_name, Name return_type, String function_body)
    {
        final Boolean isAnonymousFunction = func_name == null && iNode instanceof FunctionObjectNode;

        // only the outer function of a nested function should be converted to 
        final Boolean convertLocalVarsToMembers = !isAnonymousFunction && convertLocalVarsToMembers(iNode);

        String result = "";
        if (!isAnonymousFunction)
        {
            currentScope.setFunctionName(getBasenameFromBinding(func_name));
            result = generateFunctionBody(iNode, function_body, new Binding(iNode, return_type, null));
        }

        currentScope.generateNestedFunction(null);
        //  Pull the nested function's MethodInfo out of its scope before we pop it.
        MethodInfo nested_method_info = currentScope.getMethodInfo();

        currentScope = currentScope.popFrame();

        //  Intiialize the nested function; add a variable 
        //  to the containing function scope and add 
        //  newfunction/setproperty logic to the containing
        //  function's hoisted initialization instructions.
        if (!isAnonymousFunction && !convertLocalVarsToMembers)
            currentScope.makeVariable(func_name);

        /*
         * String init_insns = currentScope.getHoistedInitInstructions();
         * init_insns.addInstruction(OP_findproperty, func_name); //
         * Optimization: declare the function at global scope // (in a utility
         * namespace) and only new it once.
         * init_insns.addInstruction(OP_newfunction, nested_method_info);
         * init_insns.addInstruction(OP_setproperty, func_name);
         */

        {
            final FunctionNode fn = (FunctionNode)getFunctionNodeFromNode(iNode);

            // Workaround for Falcon bug.
            // In theory, just using 
            //     MethodInfo mi = currentScope.methodInfo;
            // should work. But in 307387 the parameter names and types are empty.
            // currentScope.methodInfo does contain the default values, because
            // reduce_optionalParameter got called
            if (!nested_method_info.hasParamNames())
            {
                MethodInfo miRepaired = JSGenerator.createMethodInfo(this, getEmitter(), currentScope, fn);
                final Vector<PooledValue> defaultValues = nested_method_info.getDefaultValues();
                for (PooledValue pv : defaultValues)
                {
                    miRepaired.addDefaultValue(pv);
                }
                nested_method_info = miRepaired;
            }

            String[] chunk = null;
            try
            {
                // chunk = emitParameters(currentScope.getProject(), fn.getDefinition());
                chunk = emitParameters(nested_method_info);
            }
            catch (Exception e)
            {

            }

            final String a_priori_insns = chunk[0];
            final String params = chunk[1];
            final String baseName = getBasenameFromBinding(func_name);

            // String result = generateFunction((IFunctionNode)__p, null, n, block, return_type );
            result = "";

            /*
             * internal compiler error generated with optimize enabled compiling
             * as3_enumerate.fla and fails to release the JS file
             * http://watsonexp.corp.adobe.com/#bug=3047880 We have to emit
             * something, otherwise you'd get:
             * @type {function(, , )} as that happened in as3_enumerate
             * MainTimeline. In the original MainTimeline.as change() is a local
             * function with untyped parameters: function change(identifier,
             * object, correct) which gets promoted to a method via
             * [ConvertLocalVarsToMembers] of frame0(). It seems that there are
             * two bugs: 1. JSDoc comments for functions with untyped parameters
             * should use "*" instead:
             * @type {function(*, *, *)} 2. JSDoc comments for promoted local
             * functions should be omitted. The change below addresses #2.
             */

            // JSDoc
            if (!convertLocalVarsToMembers)
                result += getJSDocForFunction(nested_method_info);

            /*
             * @@@ if( !isAnonymousFunction && convertLocalVarsToMembers ) {
             * result += getCurrentFullClassName() + ".prototype." + baseName +
             * " = "; }
             */

            // support for [ConvertLocalVarsToMembers]
            if (convertLocalVarsToMembers)
            {
                result += "\n";
                if (a_priori_insns != null && !a_priori_insns.isEmpty())
                    result += a_priori_insns + "\n";

                if (!JSSharedData.m_useSelfParameter &&
                        /*
                         * TODO: !needsSelfParameter(createFullClassName(false))
                         * &&
                         */function_body.contains(JSSharedData.THIS) &&
                        !function_body.contains("var " + JSSharedData.THIS))
                {
                    // final Binding fullClassName = makeBinding(createFullClassName(false));
                    // this.registerLocalVariable(currentScope, makeBinding("self"), fullClassName, fullClassName);

                    final Binding fullClassName = new Binding(iNode, makeName(createFullClassName(false)), null);
                    this.registerLocalVariable(currentScope, new Binding(iNode, makeName("self"), null), fullClassName, fullClassName);
                    result += indentBlock("/** @type {" + createFullClassName(true) + "} */" + endl(), 1);
                    result += indentBlock("var " + JSSharedData.THIS + " = this;" + endl(), 1);
                }

                result += function_body;

                final JSEmitter emitter = getEmitter();
                final FunctionNode func = (FunctionNode)iNode;
                final ITraitsVisitor itraits = emitter.getCurrentClassVisitor().visitInstanceTraits();
                itraits.visitMethodTrait(DirectiveProcessor.functionTraitKind(func, TRAIT_Method), getNameFromBinding(func_name), 0, nested_method_info);

                IMethodVisitor mv = emitter.visitMethod(nested_method_info);
                mv.visit();

                MethodBodyInfo mbi = new MethodBodyInfo();
                mbi.setMethodInfo(nested_method_info);

                IMethodBodyVisitor mbv = mv.visitBody(mbi);
                mbv.visit();

                InstructionList insns = new InstructionList();
                insns.addInstruction(JSSharedData.OP_JS, result);

                mbv.visitInstructionList(insns);

                mbv.visitEnd();
                mv.visitEnd();

                result = "";

                /*
                 * internal compiler error generated with optimize enabled
                 * compiling as3_enumerate.fla and fails to release the JS file
                 * We have to emit something, otherwise you'd get:
                 * @type {function(, , )} as that happened in as3_enumerate
                 * MainTimeline. In the original MainTimeline.as change() is a
                 * local function with untyped parameters: function
                 * change(identifier, object, correct) which gets promoted to a
                 * method via [ConvertLocalVarsToMembers] of frame0(). It seems
                 * that there are two bugs: 1. JSDoc comments for functions with
                 * untyped parameters should use "*" instead:
                 * @type {function(*, *, *)} 2. JSDoc comments for promoted
                 * local functions should be omitted. The change below addresses
                 * #2.
                 */
                // result += getJSDocForFunction(nested_method_info);

                this.registerConvertedMember(baseName);

                // result += "\nvar " + baseName + " /* : Function */ = " + JSSharedData.THIS + "." + baseName + ";\n";
            }
            else
            {
                if (isAnonymousFunction)
                    result += "function";
                else
                    result += "\nfunction " + baseName;

                result += "(" + params + ")";

                final Name retType = nested_method_info.getReturnType();
                if (retType != null)
                    result += " /* : " + getBasenameFromName(retType) + " */";
                else
                    result += " /* : void */";

                result += indent() + "\n";
                result += indent() + "{\n";
                if (a_priori_insns != null && !a_priori_insns.isEmpty())
                    result += indentBlock(a_priori_insns, 1) + "\n";
                result += indentBlock(function_body, 1);
                result += indent() + "}";
                if (!isAnonymousFunction)
                    result += "\n";
            }
        }

        return result;
    }

    /**
     * Generate a try/catch/finally (or try/finally) compound statement.
     * 
     * @param try_stmt - the body of the try block.
     * @param catch_blocks - associated catch blocks. May be null if no catch
     * blocks are present.
     * @param finally_stmt - the body of the finally block.
     */
    String generateTryCatchFinally(IASNode iNode, String try_stmt, Vector<CatchPrototype> catch_blocks, String finally_stmt)
    {
        /*
         * // TODO: Optimize these String sizes. String normal_flow_fixup = new
         * String(); String catch_insns = new String(); String final_catch = new
         * String(); String finally_insns = new String(); String final_throw =
         * new String(); ExceptionHandlingContext finally_context =
         * currentScope.getFlowManager().getFinallyContext(); Label
         * final_catch_target = final_catch.getLabel(); // We need a local to
         * store the caught exception. Binding exception_storage =
         * finally_context.getExceptionStorage(); Collection<Label>
         * pending_normal_control_flow = try_stmt.stripPendingLabels(); if (
         * try_stmt.canFallThrough() || pending_normal_control_flow != null ) {
         * normal_flow_fixup.addInstruction(OP_jump,
         * finally_context.finallyBlock); } else { // Extend the region past a
         * terminating // throw statement to give the AVM a // little buffer to
         * figure out its // exception-handling regions.
         * normal_flow_fixup.addInstruction(OP_nop); } Label try_start =
         * try_stmt.getLabel(); Label try_end =
         * normal_flow_fixup.getLastLabel(); Label finally_region_end = null; if
         * ( null == catch_blocks ) { finally_region_end = try_end; } else { for
         * ( CatchPrototype catch_proto: catch_blocks ) { String catch_body =
         * generateCatchBlock(try_start, try_end, catch_proto); boolean
         * is_last_catch = catch_proto.equals(catch_blocks.lastElement()); if (
         * catch_body.canFallThrough() ) { // Signal the finally block that this
         * execution succeeded. catch_body.addInstruction(OP_pushbyte, 0);
         * catch_body.addInstruction(OP_coerce_a);
         * catch_body.addInstruction(finally_context
         * .finallyReturnStorage.setlocal()); catch_body.addInstruction(OP_jump,
         * finally_context.finallyBlock); } else if ( is_last_catch ) { //
         * Extend the region past a terminating throw // insn to give the AVM a
         * little buffer. catch_body.addInstruction(OP_nop); } if (
         * is_last_catch ) finally_region_end = catch_body.getLastLabel();
         * catch_insns.addAll(catch_body); } } // Set up the exception handler
         * for the finally block.
         * currentScope.getMethodBodyVisitor().visitException(try_start,
         * finally_region_end, final_catch_target, null, null); // The final
         * catch block only needs to save the // caught exception for a rethrow.
         * final_catch.addInstruction(OP_getlocal0);
         * final_catch.addInstruction(OP_pushscope); if(
         * currentScope.needsActivation() ) { // Restore the activation object
         * final_catch
         * .addInstruction(currentScope.getActivationStorage().getlocal());
         * final_catch.addInstruction(OP_pushscope); }
         * final_catch.addAll(currentScope
         * .getFlowManager().getScopeStackReinit());
         * final_catch.addInstruction(exception_storage.setlocal()); // Signal
         * the finally epilog that this execution failed // and should rethrow.
         * final_catch.addInstruction(OP_pushbyte,
         * currentScope.getFlowManager().getFinallyAlternativesSize() + 1);
         * final_catch.addInstruction(OP_coerce_a);
         * final_catch.addInstruction(finally_context
         * .finallyReturnStorage.setlocal()); // falls through //
         * final_catch.addInstruction(OP_jump, finally_head);
         * finally_insns.addInstruction
         * (finally_context.finallyReturnStorage.getlocal());
         * finally_insns.addInstruction(OP_convert_i);
         * finally_insns.addInstruction
         * (finally_context.finallyReturnStorage.kill());
         * finally_insns.addAll(currentScope
         * .getFlowManager().getFinallySwitch()); // Label the start of the
         * final set of Strings. if (!finally_stmt.isEmpty()) {
         * finally_stmt.labelFirst(finally_context.finallyBlock); } else // This
         * is just an expedient for this degenerate finally.
         * finally_insns.labelFirst(finally_context.finallyBlock);
         * final_throw.addInstruction(exception_storage.getlocal());
         * final_throw.labelCurrent(finally_context.finallyDoRethrow);
         * final_throw.addInstruction(OP_throw); // Assemble the statement.
         * String result = new String(); // Initialize the finally return
         * storage location to appease // legacy verifiers. // TODO: This could
         * travel on the value stack. // See:
         * http://bugs.adobe.com/jira/browse/CMP-104
         * result.addInstruction(OP_pushbyte, 0);
         * result.addInstruction(OP_coerce_a);
         * result.addInstruction(finally_context
         * .finallyReturnStorage.setlocal()); result.addAll(try_stmt); // Send
         * all "next statement" type control flow into the finally block.
         * result.addAllPendingLabels(pending_normal_control_flow);
         * result.addAll(normal_flow_fixup); result.addAll(catch_insns);
         * result.addAll(final_catch); result.addAll(finally_stmt);
         * result.addAll(finally_insns); result.addAll(final_throw); for (
         * ExceptionHandlingContext.FinallyReturn retblock:
         * finally_context.finallyReturns )
         * result.addAll(retblock.getStrings()); // TODO: Need more analysis of
         * how this temp travels through // the system before it can be safely
         * released. For now // just leak it.
         * //result.addAll(currentScope.releaseTemp(exception_storage)); //
         * TODO: Removing a hanging kill exposed a latent bug // in
         * hasPendingLabels() and end-of-routine processing. // Give the CG a
         * harmless String that will generate // a returnvoid if this is the
         * last statement in the routine. result.addInstruction(OP_nop); //
         * Fallthrough out of the finally block.
         * result.labelNext(finally_context.finallyDoFallthrough);
         */

        // statement = Pattern tryCatchFinallyStmt
        String result = "";

        //  Do nothing if the try statement's empty.
        if (try_stmt.length() == 0)
            return result;

        result = indent() + "try" + endl();
        result += indent() + "{" + endl();
        result += indentBlock(try_stmt, 1);
        result += indent() + "}" + endl();

        if (catch_blocks != null)
        {
            for (CatchPrototype catch_proto : (Vector<CatchPrototype>)catch_blocks)
            {
                final Binding nameBinding = new Binding(iNode, catch_proto.catchVarName, null);
                Binding typeBinding;
                if (catch_proto.catchType == null)
                    typeBinding = new Binding(iNode, makeName("*"), null);
                else
                    typeBinding = new Binding(iNode, catch_proto.catchType, null);

                registerLocalVariable(currentScope, nameBinding, typeBinding, typeBinding);

                result += indent() + "catch(";
                if (catch_proto.catchType != null)
                {
                    final StringBuilder sb = new StringBuilder();
                    if (!nameToJSDocType(iNode, typeBinding.getName(), sb))
                        m_needsSecondPass = true;
                    result += "/** @type {" + sb.toString() + "}*/ ";
                }
                result += getBasenameFromName(catch_proto.catchVarName);
                if (catch_proto.catchType != null)
                    result += " /* : " + getBasenameFromBinding(typeBinding) + "*/";
                result += ")" + endl();
                result += indent() + "{" + endl();

                if (catch_proto.catchBody.length() > 0)
                    result += indentBlock(catch_proto.catchBody, 1);

                // boolean is_last_catch = catch_proto.equals(catch_blocks.lastElement());
                //if( !is_last_catch && canFallThrough(catch_body) )
                //    catch_body.addInstruction(OP_jump, catch_tail);

                result += indent() + "}" + endl();
            }
        }

        if (finally_stmt != null && finally_stmt.length() != 0)
        {
            result += indent() + "finally" + endl();
            result += indent() + "{" + endl();
            result += indentBlock(finally_stmt, 1);
            result += indent() + "}" + endl();
        }

        return result;
    }

    public String reduce_typeNameParameter(IASNode iNode, Binding param_name)
    {
        StringBuilder result = new StringBuilder();

        generateTypeNameParameter(iNode, param_name.getName(), result);

        return result.toString();
    }

    /**
     * Generate the String sequence that designates the parameter of a
     * parameterized type, e.g., String in Vector.&lt;String&gt; or * in
     * Vector.&lt;*&gt;.
     * 
     * @param name - the type parameter's name. May be null in the * case.
     * @param result - the String sequence to generate into.
     */
    void generateTypeNameParameter(IASNode iNode, Name param_name, StringBuilder result)
    {
        if (param_name == null || param_name.couldBeAnyType())
            result.append("null");
        else
            generateAccess(iNode, param_name, result);
    }

    /**
     * Find the specified break target.
     * 
     * @param criterion - the search criterion.
     * @throws UnknownControlFlowTargetException if the criterion is not on the
     * control-flow stack.
     */
    /*
     * private String getBreakTarget(Object criterion) throws
     * UnknownControlFlowTargetException { return
     * currentScope.getFlowManager().getBreakTarget(criterion); }
     */

    /**
     * Find the specified continue label.
     * 
     * @param criterion - the search criterion.
     * @throws UnknownControlFlowTargetException if the criterion is not on the
     * control-flow stack.
     */
    /*
     * private String getContinueLabel(Object criterion) throws
     * UnknownControlFlowTargetException { return
     * currentScope.getFlowManager().getContinueLabel(criterion); }
     */

    /**
     * @return the double content of a numeric literal.
     * @param iNode - the literal node.
     */
    Double getDoubleContent(IASNode iNode)
    {
        return new Double(((INumericLiteralNode)iNode).getNumericValue().toNumber());
    }

    /**
     * @return the name of an identifier.
     * @param iNode - the IIdentifier node.
     */
    String getIdentifierContent(IASNode iNode)
    {
        return ((IIdentifierNode)iNode).getName();
    }

    /**
     * @return the int content of a numeric literal.
     * @param iNode - the literal node.
     */
    Integer getIntegerContent(IASNode iNode)
    {
        return new Integer(((INumericLiteralNode)iNode).getNumericValue().toInt32());
    }

    /**
     * @return always zero.
     * @param iNode - the literal node.
     */
    Integer getIntegerZeroContent(IASNode iNode)
    {
        return 0;
    }

    /**
     * @return always zero.
     * @param iNode - the literal node.
     */
    Long getIntegerZeroContentAsLong(IASNode iNode)
    {
        return 0L;
    }

    /**
     * @return a node cast to MXMLEventSpecifierNode type.
     * @param iNode - the MXMLEventSpecifierNode node.
     */
    IMXMLEventSpecifierNode getMXMLEventSpecifierContent(IASNode iNode)
    {
        return (IMXMLEventSpecifierNode)iNode;
    }

    /**
     * Find all active exception handling blocks or scopes, and set up finally
     * return sequences and/or popscopes.
     * 
     * @param original - the original control-flow sequence.
     * @param criterion - the search criterion that identifies the boundary of
     * the region to be exited.
     * @return a control-flow sequence that is either the original sequence or
     * the start of a trampoline of finally blocks, popscopes, and whatever else
     * is necessary to exit the control-flow region.
     * @throws UnknownControlFlowTargetException if the criterion is not on the
     * control-flow stack.
     */
    /*
     * private String getNonLocalControlFlow(String original, Object criterion)
     * throws UnknownControlFlowTargetException { return
     * currentScope.getFlowManager().getNonLocalControlFlow(original,
     * criterion); }
     */

    /**
     * @return a parameter's definition.
     * @param iNode - the parameter node.
     */
    IDefinition getParameterContent(IASNode iNode)
    {
        return (((IParameterNode)iNode).getDefinition());
    }

    /**
     * @return the string content of a literal.
     * @param iNode - the literal node.
     */
    String getStringLiteralContent(IASNode iNode)
    {
        // Literals.
        // Pattern stringLiteral
        // LiteralStringID(void);
        String s = ((ILiteralNode)iNode).getValue();
        if (s == null || s.length() == 0)
            return "''";

        s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
        s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
        s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
        s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
        s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
        s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
        s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
        s = "\"" + s.replaceAll("\"", "\\\\\"") + "\"";
        s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
        s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
        s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
        s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
        s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
        s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
        s = s.replaceAll("__NEWLINE_PLACEHOLDER__", "\\\\n");

        // Note: we don't try to preserve \ u NNNN or \ x NN escape codes, since the actual Unicode chars can appear
        // inside the literal just fine (unless they translate to one of the above chars, e.g. CR, in which case
        // the above substitutions will occur.

        return s;
    }

    /**
     * @return the uint content of a numeric literal.
     * @param iNode - the literal node.
     */
    Long getUintContent(IASNode iNode)
    {
        return new Long(((INumericLiteralNode)iNode).getNumericValue().toUint32());
    }

    /**
     * @return the Boolean content of a BOOLEAN literal.
     * @param iNode - the literal node.
     */
    boolean getBooleanContent(IASNode iNode)
    {
        return SemanticUtils.getBooleanContent(iNode);
    }

    /**
     * @return true if a priori Strings are present.
     */
    /*
     * private boolean haveinstanceInitializers() { return instanceInitializers
     * != null; }
     */

    /*
     * *******************************
     * ** Cost/Decision Functions ** *******************************
     */

    /**
     * Explore a MemberAccessNode and decide if its stem is a reference to a
     * package.
     */
    int isDottedName(IASNode n)
    {
        int result = Integer.MAX_VALUE;

        if (n instanceof MemberAccessExpressionNode)
        {
            MemberAccessExpressionNode ma = (MemberAccessExpressionNode)n;

            if (ma.stemIsPackage())
                // This needs to be greater than the value returned from isPackageName,
                // so that isPackageName wins
                result = 2;
        }

        return result;
    }

    /**
     * Explore a MemberAccessNode and decide if it is a reference to a package.
     */
    int isPackageName(IASNode n)
    {
        int result = Integer.MAX_VALUE;

        if (n instanceof MemberAccessExpressionNode)
        {
            MemberAccessExpressionNode ma = (MemberAccessExpressionNode)n;

            if (ma.isPackageReference())
                // This needs to be less than the value returned from isDottedName,
                // so that isPackageName wins
                result = 1;
        }

        return result;
    }

    /**
     * Check if the given AST (the root AST of a looping construct) is in the
     * set of active labeled control-flow contexts. If it is, then its
     * control-flow context is being managed by the labeledStatement production
     * that's reducing the loop's LabeledStatementNode, and the loop should use
     * that context so that the labeled context's continue label is resolved to
     * the correct top-of-loop location.
     * 
     * @param loop_node - the AST at the root of the loop.
     * @return true if loop_node is known to be the substatment of a labeled
     * statement.
     */
    private boolean isLabeledSubstatement(IASNode loop_node)
    {
        return labeledNodes.contains(loop_node);
    }

    /**
     * Get the definition associated with a node's qualifier and decide if the
     * qualifier is a compile-time constant.
     * 
     * @param iNode - the node to check.
     * @pre - the node has an IdentifierNode 0th child.
     * @return an attractive cost if the child has a known namespace, i.e., it's
     * a compile-time constant qualifier.
     */
    int qualifierIsCompileTimeConstant(IASNode iNode)
    {
        IdentifierNode qualifier = (IdentifierNode)SemanticUtils.getNthChild(iNode, 0);
        IDefinition def = qualifier.resolve(currentScope.getProject());

        int result = def instanceof NamespaceDefinition ? 1 : Integer.MAX_VALUE;
        return result;
    }

    /**
     * Get the definition associated with a node's qualifier and decide if the
     * qualifier is an interface name.
     * 
     * @param iNode - the node to check.
     * @pre - the node has an IdentifierNode 0th child.
     * @return an attractive cost if the child is an interface.
     */
    int qualifierIsInterface(IASNode iNode)
    {
        IdentifierNode qualifier = (IdentifierNode)SemanticUtils.getNthChild(iNode, 0);
        IDefinition def = qualifier.resolve(currentScope.getProject());

        int result = def instanceof InterfaceDefinition ? 1 : Integer.MAX_VALUE;
        return result;

    }

    /**
     * @return a feasible cost if a node has a compile-time constant defintion.
     */
    int isCompileTimeConstant(IASNode iNode)
    {
        if (SemanticUtils.transformNameToConstantValue(iNode, currentScope.getProject()) != null)
            return 1;
        else
            return Integer.MAX_VALUE;
    }

    /**
     * @return a feasible cost if the function call node is a call to a constant
     * function.
     */
    int isCompileTimeConstantFunction(IASNode iNode)
    {
        //if ( SemanticUtils.isConstantFunction(currentScope.getProject(), iNode)  )
        //    return 1;
        // else
        return Integer.MAX_VALUE;
    }

    /**
     * @return a feasible cost if a parameterized type's base and parameter
     * types are resolved types, ERROR_TRAP if not.
     */
    int parameterTypeIsConstant(IASNode iNode)
    {
        return Math.max(
                isKnownType(SemanticUtils.getNthChild(iNode, 0)),
                isKnownType(SemanticUtils.getNthChild(iNode, 1))
                );
    }

    /**
     * @return a feasible cost if the type's parameter is a resolved type,
     * ERROR_TRAP if not.
     */
    int isKnownType(IASNode iNode)
    {
        boolean isConstant = false;

        if (iNode instanceof IExpressionNode)
        {
            IExpressionNode qualifier = (IExpressionNode)iNode;
            isConstant = qualifier.resolve(currentScope.getProject()) instanceof ITypeDefinition;
        }

        return isConstant ? 1 : ERROR_TRAP;
    }

    public Object transform_constant_function_to_value(IASNode iNode, Binding method, Vector<Object> constant_args)
    {
        assert false : "attempting to reduce a non-constant function as a constant.  " +
                       "isCompileTimeConstantFunction Should have guarded against this";

        return null;
    }

    /**
     * Check a Binding to decide if its referent is statically known to be a
     * Namespace.
     * 
     * @param b - the Binding to check.
     * @return true if the given Binding refers to a namespace.
     */
    /*
     * private boolean isNamespace(Binding b) { return b.getDefinition()
     * instanceof NamespaceDefinition; }
     */

    /**
     * Check a Binding to see if it's (often incorrectly) defined as the
     * ANY_TYPE.
     * 
     * @param b - the Binding to check.
     * @return true if the Binding's definition is the ANY_TYPE definition.
     */
    /*
     * private boolean isAnyType(Binding b) { return b.getDefinition() ==
     * ClassDefinition.getAnyTypeClassDefinition(); }
     */

    /**
     * @return a feasible cost (1) if the node is for 'new Array()'
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
     * @return a feasible cost (1) if the node is for 'new Object()'
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
     * ************************
     * ** Reduction prologues ************************
     */

    public void prologue_anonymousFunction(IASNode iNode)
    {
        //  TODO: The current scope may not 
        //  need an activation if the anonymous
        //  function doesn't access any of the
        //  current scope's locals.
        currentScope = currentScope.pushFrame();
        currentScope.declareAnonymousFunction();
        prologue_function(iNode);

        // Register arguments as local Variables.
        final Binding b_name = new Binding(iNode, new Name("arguments"), null);
        final Binding b_type = new Binding(iNode, new Name("Array"), null);
        registerLocalVariable(currentScope, b_name, b_type, b_type);
    }

    public void prologue_functionObject(IASNode iNode)
    {
        //  TODO: The current scope may not 
        //  need an activation if the anonymous
        //  function doesn't access any of the
        //  current scope's locals.
        currentScope = currentScope.pushFrame();
        final FunctionNode innerFunctionNode = ((FunctionObjectNode)iNode).getFunctionNode();
        currentScope.declareFunctionObject(innerFunctionNode.getName());
        prologue_function(iNode);

        // Register arguments as local Variables.
        final Binding b_name = new Binding(iNode, new Name("arguments"), null);
        final Binding b_type = new Binding(iNode, new Name("Array"), null);
        registerLocalVariable(currentScope, b_name, b_type, b_type);
    }

    public void prologue_blockStmt(IASNode iNode)
    {
    }

    public void prologue_blockStmt_to_finally_clause(IASNode iNode)
    {
        // workaround for Falcon bug.
        // java.lang.ClassCastException: org.apache.flex.compiler.internal.as.codegen.ControlFlowContext cannot be cast to org.apache.flex.compiler.internal.as.codegen.ExceptionHandlingContext
        try
        {
            currentScope.getFlowManager().startFinallyContext(iNode);
        }
        catch (ClassCastException e)
        {

        }
    }

    public void prologue_catchBlock(IASNode iNode)
    {
        // workaround for Falcon bug.
        // java.lang.ClassCastException: org.apache.flex.compiler.internal.as.codegen.ControlFlowContext cannot be cast to org.apache.flex.compiler.internal.as.codegen.ExceptionHandlingContext
        try
        {
            currentScope.getFlowManager().startCatchContext((ICatchNode)iNode);
        }
        catch (ClassCastException e)
        {

        }
    }

    public void prologue_countedForStmt(IASNode iNode)
    {
        if (!isLabeledSubstatement(iNode))
            currentScope.getFlowManager().startLoopControlFlowContext(((IForLoopNode)iNode).getStatementContentsNode());
    }

    public void prologue_doStmt(IASNode iNode)
    {
        if (!isLabeledSubstatement(iNode))
            currentScope.getFlowManager().startLoopControlFlowContext(((IWhileLoopNode)iNode).getStatementContentsNode());
    }

    public void prologue_forEachStmt(IASNode iNode)
    {
        if (!isLabeledSubstatement(iNode))
            currentScope.getFlowManager().startLoopControlFlowContext(((IForLoopNode)iNode).getStatementContentsNode());
    }

    public void prologue_forInStmt(IASNode iNode)
    {
        if (!isLabeledSubstatement(iNode))
            currentScope.getFlowManager().startLoopControlFlowContext(((IForLoopNode)iNode).getStatementContentsNode());
    }

    void prologue_function(IASNode n)
    {
        FunctionDefinition func = (FunctionDefinition)getFunctionDefinitionFromNode(n);

        IFunctionNode funcNode = null;

        if (n instanceof IFunctionNode)
            funcNode = (IFunctionNode)n;
        else if (n instanceof FunctionObjectNode)
            funcNode = ((FunctionObjectNode)n).getFunctionNode();

        if (funcNode != null)
            func = (FunctionDefinition)funcNode.getDefinition();
        assert (func != null) : n + " has no definition.";

        // workaroudn for Falcon bug CMP-???
        // setLocalASScope() throws "Local scope already set.".
        // 
        try
        {
            currentScope.setLocalASScope(func.getContainedScope());
        }

        catch (AssertionError e)
        {

        }
        currentScope.resetDebugInfo();

        currentScope.getMethodBodySemanticChecker().checkFunctionDefinition(funcNode, func);

        for (int i = 0; i < n.getChildCount(); i++)
            scanFunctionBodyForActivations(n.getChild(i));
    }

    public void prologue_labeledStmt(IASNode iNode)
    {
        // Since the Prologue code runs before the subtrees have been reduced, 
        // we can't get the label string via the non_resolving_identifier reduction.
        // NonResolvingIdentifierNode label = (NonResolvingIdentifierNode)JSGeneratingReducer.getNthChild(iNode, 0);
        try
        {
            currentScope.getFlowManager().startLabeledStatementControlFlowContext((LabeledStatementNode)iNode);
        }
        catch (DuplicateLabelException dup_label)
        {
            currentScope.addProblem(new DuplicateLabelProblem(iNode));
        }

        //  If the label applies to a particular statement,
        //  not to a block, then add that statement to the set
        //  of labeled nodes so that the statement's reduction 
        //  can use this labeled control flow context.
        if (iNode.getChildCount() == 2)
            labeledNodes.add(SemanticUtils.getNthChild(iNode, 1));
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
        //  Always establish a flow context, even if the statement
        //  was labeled; control flow can't branch backwards via
        //  continue.
        currentScope.getFlowManager().startSwitchContext((SwitchNode)iNode);
    }

    public void prologue_tryCatchFinallyStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startExceptionContext((ITryNode)iNode);
        currentScope.getFlowManager().getFinallyContext().setHasFinallyBlock(true);
    }

    public void prologue_tryCatchStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startExceptionContext((ITryNode)iNode);
    }

    public void prologue_tryFinallyStmt(IASNode iNode)
    {
        currentScope.getFlowManager().startExceptionContext((ITryNode)iNode);
        currentScope.getFlowManager().getFinallyContext().setHasFinallyBlock(true);
    }

    public void prologue_typedFunction_to_statement(IASNode iNode)
    {
        //  Note: A named nested function does 
        //  always need an activation record.
        currentScope = currentScope.pushFrame();
        currentScope.declareNestedFunction();
        prologue_function(iNode);
    }

    public void prologue_typelessFunction_to_statement(IASNode iNode)
    {
        //  Note: A named nested function does 
        //  always need an activation record.
        currentScope = currentScope.pushFrame();
        currentScope.declareNestedFunction();
        prologue_function(iNode);
    }

    public void prologue_typeof(IASNode iNode)
    {
        //  ABC-specific processing not relevant for source-to-source translation.
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
     * *************************
     * ** Reduction actions ** *************************
     */
    public String reduce_functionObject(IASNode iNode, IASNode function_name, String plist, Binding return_type, String function_body)
    {
        return reduce_anonymousFunction(iNode, function_body);
    }

    public String reduce_anonymousFunction(IASNode iNode, String function_body)
    {
        /*
         * currentScope.generateNestedFunction(function_body); String result =
         * createInstruction(iNode, 1); result.addInstruction(OP_newfunction,
         * currentScope.methodInfo);
         */

        currentScope.generateNestedFunction(null);
        String result = createInstruction(iNode, 1);

        MethodInfo mi = currentScope.getMethodInfo();

        // Workaround for Falcon bug.
        // In theory, just using 
        //     MethodInfo mi = currentScope.methodInfo;
        // should work. But in 307387 the parameter names and types are empty.
        // currentScope.methodInfo does contain the default values, because
        // reduce_optionalParameter got called
        {
            final FunctionNode fn = (FunctionNode)getFunctionNodeFromNode(iNode);
            if (!mi.hasParamNames())
            {
                MethodInfo miRepaired = JSGenerator.createMethodInfo(this, (JSEmitter)currentScope.getEmitter(), currentScope, fn);
                final Vector<PooledValue> defaultValues = mi.getDefaultValues();
                for (PooledValue pv : defaultValues)
                {
                    miRepaired.addDefaultValue(pv);
                }
                mi = miRepaired;
            }

            String[] chunk = null;
            try
            {
                // chunk = emitParameters(currentScope.getProject(), fn.getDefinition());
                chunk = emitParameters(mi);
            }
            catch (Exception e)
            {

            }

            final String a_priori_insns = chunk[0];
            final String params = chunk[1];

            // String result = generateFunction((IFunctionNode)iNode, null, n, block, return_type );

            // JSDoc
            result += getJSDocForFunction(mi);
            result += "function(" + params + ")";

            final Name retType = mi.getReturnType();
            if (retType != null)
                result += " /* : " + getBasenameFromName(retType) + " */";
            else
                result += " /* : void */";

            result += indent() + "\n";
            result += indent() + "{\n";
            if (a_priori_insns != null && !a_priori_insns.isEmpty())
                result += indentBlock(a_priori_insns, 1) + "\n";
            result += indentBlock(function_body, 1);
            result += indent() + "}";
        }

        currentScope = currentScope.popFrame();
        return result;
    }

    public String reduce_arrayIndexExpr(IASNode iNode, String stem, boolean is_super, String index)
    {
        // TODO: handle is_super
        if (is_super)
            currentScope.getMethodBodySemanticChecker().checkSuperAccess(iNode);

        //  No semantic restrictions on fetching an array value.

        String result = createInstruction(iNode, stem.length() + index.length() + 1);

        String member = stripTabs(index);

        // NOTE: Sometimes the part in brackets ("member") is a member of the LHS type, and sometimes it's not.
        // E.g. in instance["foo"] foo is a member name; but in instance[foo], foo is an expression evaluating
        // to a string, and that string is the member name. This distinction is handled by resolveGetterName()
        // and resolveSetterName(). They may ignore useBrackets in certain cases due to this.
        result += resolveGetterName(iNode, stem, makeMemberBinding(iNode, stem, member), true);

        return result;
    }

    public String reduce_arrayLiteral(IASNode iNode, Vector<String> elements)
    {
        //  No semantic restrictions on array literals.

        //  TODO: Investigate optimizing.
        String result = createInstruction(iNode);

        /*
         * for ( String element: elements ) result.addAll(element);
         * result.addInstruction(OP_newarray, elements.size());
         */

        result += "[";
        boolean comma = false;
        for (String element : elements)
        {
            if (comma)
                result += ", ";
            else
                comma = true;
            result += stripTabs(element);
        }

        result += "]";
        return result;
    }

    public String reduce_assignToBracketExpr_to_expression(IASNode iNode, String stem, String index, String r, boolean is_super)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignToBracketExpr(iNode);

        /*
         * Binding local = currentScope.allocateTemp(); String result =
         * createInstruction(iNode, stem.size() + index.size() + r.size() + 5);
         * result.addAll(stem); result.addAll(index); result.addAll(r);
         * result.addInstruction(OP_dup); result.addInstruction(local.setlocal()
         * ); result.addInstruction(OP_setproperty);
         * result.addInstruction(local.getlocal() );
         * result.addAll(currentScope.releaseTemp(local));
         */

        // void_expression = Pattern assignToBracketExpr, expression
        // Assignment to a[i] type lvalue.

        // trim quotes
        String member = stripTabs(index);

        String result = createInstruction(iNode, stem.length() + index.length() + r.length() + 5);
        final Binding memberBinding = makeMemberBinding(iNode, stem, member);
        final String setter = resolveSetterName(iNode, stem, memberBinding, "=", stripTabs(r), true, true);
        final String getter = resolveGetterName(iNode, stem, memberBinding, true);
        if (getter.equals(stem + "[" + member + "]"))
            result += "(" + setter + ")";
        else
            result += "(" + setter + ", " + getter + ")";

        return result;
    }

    public String reduce_assignToBracketExpr_to_void_expression(IASNode iNode, String stem, String index, String r, boolean is_super)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignToBracketExpr(iNode);

        /*
         * String result = createInstruction(iNode, stem.size() + index.size() +
         * r.size() + 1); result.addAll(stem); result.addAll(index);
         * result.addAll(r); result.addInstruction(OP_setproperty);
         */

        String result = createInstruction(iNode, stem.length() + index.length() + r.length() + 1);

        // void_expression = Pattern assignToBracketExpr, void_expression
        // Assignment to a[i] type lvalue.

        // String result = indent() + stem + "[" + stripTabs(index) + "] = " + stripTabs(r) + ";" + endl();

        // trim quotes
        final String member = stripTabs(index);
        result += indent() + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, member), "=", stripTabs(r), true, true) + ";" + endl();
        return result;
    }

    public String reduce_assignToMemberExpr_to_expression(IASNode iNode, String stem, Binding member, String r)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, member);

        /*
         * Binding local = currentScope.allocateTemp(); String result =
         * createInstruction(iNode, stem.size() + r.size() + 5);
         * result.addAll(stem); result.addAll(r); result.addInstruction(OP_dup);
         * result.addInstruction(local.setlocal() );
         * result.addInstruction(OP_setproperty, member.getName());
         * result.addInstruction(local.getlocal() );
         * result.addAll(currentScope.releaseTemp(local));
         */

        String result = createInstruction(iNode, stem.length() + r.length() + 5);
        // Assignment to a more general lvalue
        // Pattern assignToMemberExpr, expression
        // Op_AssignId(MemberAccessExpressionID(expression stem, name member), expression r);
        // String result = indent() + stem + "." + getBasenameFromBinding(member) + " = " + stripTabs(r) + ";" + endl();

        result += "(" + resolveSetterName(iNode, stem, member, "=", stripTabs(r), false, true) + ", " + resolveGetterName(iNode, stem, member, false) + ")";
        return result;
    }

    public String reduce_assignToMemberExpr_to_void_expression(IASNode iNode, String stem, Binding member, String r)
    {
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, member);

        /*
         * String result = createInstruction(iNode, stem.size() + r.size() + 1);
         * result.addAll(stem); result.addAll(r);
         * result.addInstruction(OP_setproperty, member.getName());
         */

        String result = createInstruction(iNode, stem.length() + r.length() + 1);
        // Assignment to a more general lvalue
        // Pattern assignToMemberExpr, void_expression
        // Op_AssignId(MemberAccessExpressionID(expression stem, name member), expression r);

        result += indent();
        result += resolveSetterName(iNode, stem, member, "=", stripTabs(r), false, true) + ";" + endl();
        return result;
    }

    public String reduce_assignToDescendantsExpr(IASNode iNode, String stem, Binding member, String r, boolean need_value)
    {
        //  Note: This code doesn't assign to descendants!
        //  While one might think it should, it does in fact
        //  mirror the bytecode ASC emitted.
        currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, member);

        /*
         * Binding local = null; String result = createInstruction(iNode,
         * stem.size() + r.size() + 5); result.addAll(stem); result.addAll(r);
         * if ( need_value ) { local = currentScope.allocateTemp();
         * result.addInstruction(OP_dup); result.addInstruction(local.setlocal()
         * ); } result.addInstruction(OP_setproperty, member.getName()); if (
         * need_value ) { result.addInstruction(local.getlocal() );
         * result.addAll(currentScope.releaseTemp(local)); }
         */

        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "E4X (ECMA-357)"));
        String result = createInstruction(iNode, stem.length() + r.length() + 5);
        return result;
    }

    public String reduce_assignToNameExpr_to_void_expression(IASNode iNode, Binding lvalue, String r)
    {
        if (!isSyntheticBinding(lvalue))
            currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, lvalue);

        // TODO: return generateAssignment(iNode, lvalue, r);

        // Pattern assignToNameExpr, void_expression
        // Op_AssignId(name lval, expression r);

        String name = bindingToString(iNode, lvalue, true, true);
        if (name.contains("."))
            name = name.substring(0, name.lastIndexOf("."));
        else
            name = "";
        return indent() + resolveSetterName(iNode, name, lvalue, "=", stripTabs(r), false, true) + ";" + endl();
    }

    public String reduce_assignToNameExpr_to_expression(IASNode iNode, Binding lvalue, String r)
    {
        if (!isSyntheticBinding(lvalue))
            currentScope.getMethodBodySemanticChecker().checkAssignment(iNode, lvalue);
        // TODO: return generateAssignment(iNode, lvalue, r, true);

        // Pattern assignToNameExpr, expression
        // Op_AssignId(name lval, expression r);

        final String name = bindingToString(iNode, lvalue, true, true);
        String stem = "";
        if (name.contains("."))
            stem = name.substring(0, name.lastIndexOf("."));
        return "(" + resolveSetterName(iNode, stem, lvalue, "=", stripTabs(r), false, true) + ", " + name + ")";
    }

    public String reduce_assignToQualifiedMemberExpr(IASNode iNode, String stem, Binding qualifier, Binding member, String rhs, boolean need_value)
    {
        String result = createInstruction(iNode);
        result += "(" + resolveSetterName(iNode, stem, member, "=", stripTabs(rhs), false, true) + ", " + resolveGetterName(iNode, stem, member, false) + ")";
        return result;
    }

    public String reduce_assignToQualifiedRuntimeMemberExpr(IASNode iNode, String stem, Binding qualifier, String runtime_member_selector, String rhs, boolean need_value)
    {
        String result = createInstruction(iNode);

        final Binding member = qualifier;
        result += "(" + resolveSetterName(iNode, stem, member, "=", stripTabs(rhs), false, true) + ", " + resolveGetterName(iNode, stem, member, false) + ")";
        return result;
    }

    public String reduce_assignToQualifiedAttributeExpr(IASNode iNode, String stem, Binding qualifier, Binding member, String rhs, boolean need_value)
    {
        String result = createInstruction(iNode);

        result += "(" + resolveSetterName(iNode, stem, member, "=", stripTabs(rhs), false, true) + ", " + resolveGetterName(iNode, stem, member, false) + ")";
        return result;
    }

    public String reduce_assignToQualifiedRuntimeAttributeExpr(IASNode iNode, String stem, Binding qualifier, String runtime_member_selector, String rhs, boolean need_value)
    {
        String result = createInstruction(iNode);

        final Binding member = qualifier;
        result += "(" + resolveSetterName(iNode, stem, member, "=", stripTabs(rhs), false, true) + ", " + resolveGetterName(iNode, stem, member, false) + ")";
        return result;
    }

    public String reduce_assignToRuntimeNameExpr(IASNode iNode, RuntimeMultiname lval, String r, final boolean need_value)
    {
        return lval.generateAssignment(iNode, r);
    }

    public String reduce_assignToUnqualifiedRuntimeAttributeExpr(IASNode iNode, String stem, String rt_attr, String rhs, boolean need_value)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "assign to unqualified runtime attribute"));

        String result = createInstruction(iNode);
        return result;
    }

    public Binding reduce_attributeName(IASNode iNode, Binding attr_name)
    {
        // do nothing - just return the contained name
        // which will already be set up correctly for an attribute name
        return attr_name;
    }

    public String reduce_blockStmt_to_finally_clause(IASNode iNode, Vector<String> stmts)
    {
        String result = createInstruction(iNode);

        for (String stmt : stmts)
            result += stmt;

        // workaround for Falcon bug.
        // java.lang.ClassCastException: org.apache.flex.compiler.internal.as.codegen.ControlFlowContext cannot be cast to org.apache.flex.compiler.internal.as.codegen.ExceptionHandlingContext
        try
        {
            currentScope.getFlowManager().endFinallyContext();
        }
        catch (ClassCastException e)
        {

        }

        return result;

    }

    public String reduce_blockStmt_to_statement(IASNode iNode, Vector<String> stmts)
    {
        //  TODO: See if optimizing this is useful.
        //  Iterating twice over the statements
        //  might cost more than it's worth.
        String result = createInstruction(iNode);

        for (String stmt : stmts)
        {
            //  Some statement-level reductions return null.
            if (stmt != null)
            {
                result += stmt;
            }
        }

        return result;
    }

    public Boolean reduce_booleanLiteral(IASNode iNode)
    {
        if ("true".equals(removeQuotes(getStringLiteralContent(iNode))))
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    public String reduce_breakStmt(IASNode iNode)
    {
        /*
         * try { return getNonLocalControlFlow(
         * getBreakTarget(ControlFlowContextManager.FIND_LAST_CONTEXT),
         * ControlFlowContextManager.FIND_LAST_CONTEXT ); } catch (
         * UnknownControlFlowTargetException no_target ) {
         * currentScope.getProblems().add(new UnknownBreakTargetProblem(iNode));
         * return createInstruction(iNode); }
         */
        // statement = Pattern breakStmt
        String result = createInstruction(iNode);
        result += indent() + "break;" + endl();
        return result;

    }

    public String reduce_by_concatenation(IASNode iNode, String first, String second)
    {
        return first + "." + second;
    }

    public CatchPrototype reduce_catchBlockTyped(IASNode iNode, Binding var_name, Binding exception, String action)
    {
        CatchPrototype result = new CatchPrototype();
        result.catchVarName = var_name.getName();
        result.catchType = exception.getName();
        result.catchBody = action;

        // workaround for Falcon bug.
        // java.lang.ClassCastException: org.apache.flex.compiler.internal.as.codegen.ControlFlowContext cannot be cast to org.apache.flex.compiler.internal.as.codegen.ExceptionHandlingContext
        try
        {
            currentScope.getFlowManager().startCatchContext((ICatchNode)iNode);
        }
        catch (ClassCastException e)
        {

        }
        return result;
    }

    public CatchPrototype reduce_catchBlockUntyped(IASNode iNode, Binding var_name, String action)
    {
        CatchPrototype result = new CatchPrototype();
        result.catchVarName = var_name.getName();
        result.catchType = null;
        result.catchBody = action;

        // workaround for Falcon bug.
        // java.lang.ClassCastException: org.apache.flex.compiler.internal.as.codegen.ControlFlowContext cannot be cast to org.apache.flex.compiler.internal.as.codegen.ExceptionHandlingContext
        try
        {
            currentScope.getFlowManager().endCatchContext();
        }
        catch (ClassCastException e)
        {

        }
        return result;
    }

    public String reduce_commaExpr(IASNode iNode, String payload_expr, Vector<String> exprs)
    {
        //  TODO: Investigate optimizing this.
        String result = createInstruction(iNode);

        // Pattern commaExpr
        // Op_CommaID(expression payload_expr, void_expression exprs+);

        //  The payload expression is the last expression in the
        //  comma list.  It was inverted by IASNodeAdapter into
        //  the 0th position to work around limitations of the
        //  BURG's n-ary operand processor.

        Boolean comma = false;
        result += "(";
        for (String other_expr : exprs)
        {
            if (comma)
                result += ", ";
            else
                comma = true;
            // CMP-???: remove trailing LF
            if (other_expr.charAt(other_expr.length() - 1) == '\n')
                other_expr = other_expr.substring(0, other_expr.length() - 1);

            // CMP-???: remove trailing ";"
            if (other_expr.charAt(other_expr.length() - 1) == ';')
                other_expr = other_expr.substring(0, other_expr.length() - 1);

            result += stripTabs(other_expr);
        }

        if (comma)
            result += ", ";
        result += payload_expr;
        result += ")";
        return result;
    }

    public ConditionalFragment reduce_conditionalFragment(IASNode iNode, String condition, Vector<String> consequents)
    {
        // return new ConditionalFragment(iNode, condition, consequents);

        if (condition == null)
            return reduce_switchDefault(iNode, consequents);

        return reduce_switchCase(iNode, condition, consequents);

    }

    public ConditionalFragment reduce_constantConditionalFragment(IASNode iNode, Object condition, Vector<String> consequents)
    {
        return reduce_conditionalFragment(iNode, condition.toString(), consequents);
    }

    public String reduce_constantStringConcatenation(IASNode iNode, String l, String r)
    {
        // return l + r;

        // Concatenate two string literals
        assert l.endsWith("\"");
        assert r.startsWith("\"");

        return l.substring(0, l.length() - 1) + r.substring(1);
    }

    /**
     * reduce a concatenation of a string and a constant value to a string
     * constant. The constant value will be converted to a string according to
     * the ECMA spec, and then will be concatenated with the string.
     * 
     * @param iNode the node
     * @param l the left string
     * @param r the right value
     * @return the concatenated string
     */
    public String reduce_constantStringConcatenation(IASNode iNode, String l, Object r)
    {
        return reduce_constantStringConcatenation(iNode, l, ECMASupport.toString(r));
    }

    /**
     * reduce a concatenation of a constant value and a string to a string
     * constant. The constant value will be converted to a string according to
     * the ECMA spec, and then will be concatenated with the string.
     * 
     * @param iNode the node
     * @param l the left value
     * @param r the right string
     * @return the concatenated string
     */
    public String reduce_constantStringConcatenation(IASNode iNode, Object l, String r)
    {
        return reduce_constantStringConcatenation(iNode, ECMASupport.toString(l), r);
    }

    /**
     * reduce addition of two constant values. If either of the constants is a
     * String, then string concatenation will be performed. OTherwise the
     * constants will be converted to Numbers, according to the ECMA spec, and
     * added
     * 
     * @param iNode the node
     * @param l the left value
     * @param r the right value
     * @return the result of adding the two values
     */
    public Object reduce_constantAddition(IASNode iNode, Object l, Object r)
    {
        checkBinaryOp(iNode, OP_add);
        if (l instanceof String)
            return reduce_constantStringConcatenation(iNode, (String)l, r);
        else if (r instanceof String)
            return reduce_constantStringConcatenation(iNode, l, (String)r);
        else
            return ECMASupport.toNumeric(l).doubleValue() + ECMASupport.toNumeric(r).doubleValue();
    }

    public String reduce_continueStmt(IASNode iNode)
    {
        /*
         * try { return getNonLocalControlFlow(
         * getContinueLabel(ControlFlowContextManager.FIND_LAST_CONTEXT),
         * ControlFlowContextManager.FIND_LAST_CONTEXT ); } catch (
         * UnknownControlFlowTargetException no_target ) {
         * currentScope.getProblems().add(new
         * UnknownContinueTargetProblem(iNode)); return
         * createInstruction(iNode); }
         */

        // statement = Pattern continueStmt
        String result = createInstruction(iNode);
        result += indent() + "continue;" + endl();
        return result;
    }

    public String reduce_countedForStmt(IASNode iNode, String init, String test_insns, String incr, String body)
    {
        String result = createInstruction(iNode, init.length() + test_insns.length() + incr.length() + body.length() + 5);

        /*
         * // Initialize counters, jump to test result.addAll(init);
         * result.addInstruction(OP_jump, test_insns.getLabel()); // Loop body
         * String loop_body = new String(); Label loop_head = new Label();
         * loop_body.addInstruction(OP_label);
         * loop_body.labelCurrent(loop_head); loop_body.addAll(body); // Set the
         * continue target on the correct String. if ( !incr.isEmpty() )
         * currentScope.getFlowManager().resolveContinueLabel(incr); else
         * currentScope.getFlowManager().resolveContinueLabel(loop_body);
         * result.addAll(loop_body); // Now add the loop counter increments. //
         * The continue context needs to be set // first for String hygiene.
         * result.addAll(incr); // Add the test; a successful test branches //
         * back to the loop head. The test generation // rules guarantee this
         * last String is a // branch String that needs a target.
         * test_insns.lastElement().setTarget(loop_head);
         * result.addAll(test_insns); if ( ! isLabeledSubstatement(iNode) )
         * currentScope.getFlowManager().finishControlFlowContext(result);
         */

        {
            // Pattern countedForStmt
            // ForLoopID(ContainerID( void_expression init, conditionalJump test_insns, void_expression incr ), statement body );

            // Initializer clause (1st clause)
            // (init may be "" if clause was empty in AS)
            init = removeSuffixIfPresent(init, ";\n"); // strip final ; so we can easily munge any other ;s below
            init = init.replace("\n", "");
            init = stripTabs(init);

            // Deal with multiple var declarations in first clause
            int indexOfVar = init.indexOf("var ");
            if (indexOfVar != -1)
            {
                String initRest = init.substring(indexOfVar + 3);
                if (initRest.contains("var "))
                {
                    // Multiple vars are present: 'init' currently contains a semicolon-separated series of var declarations.
                    // That is not valid JS nor AS: we want a comma-separated series with only ONE var keyword at the start.
                    // TODO: We still leave in extraneous @type JSDoc comments... does this cause a problem for Closure?
                    initRest = initRest.replace(";", ","); // change semicolon separators into commas
                    initRest = initRest.replaceAll("\\bvar ", " "); // remove all later var keywords
                    init = init.substring(0, indexOfVar + 3) + initRest;
                }
            }

            init = init + ";"; // restore final ; removed in first step (or was never present, if init was originally "")

            // Test clause (2nd clause)
            String test = "";
            if (test_insns != null)
            {
                // Workaround for Falcon bug.
                // FJS-9: for (count = 0; ; count++)
                if (test_insns.equals("== null"))
                    test = ";";
                else
                    test = test_insns + ";";
            }

            // Increment clause (3rd clause)
            // (incr may be "" if clause was empty in AS)
            incr = removeSuffixIfPresent(incr, ";\n");
            incr = stripTabs(incr);
            // last clause, so leave it with no trailing ;

            assert init.endsWith(";");
            assert test.endsWith(";");

            result += indent() + "for(" + init + " " + test + " " + incr + ")" + endl();
            result += indent() + "{" + endl();
            // pushIndent();
            if (body.length() > 0)
                result += indentBlock(body, 1);
            // popIndent();
            result += indent() + "}" + endl();
        }

        return result;
    }

    public String reduce_defaultXMLNamespace(IASNode iNode, String ns_expr)
    {
        String result = createInstruction(iNode);
        /*
         * // TODO: This could be optimized for the string constant case, // but
         * ASC doesn't do so, which means it's future and probably // also not
         * well tested in the AVM. result.addAll(ns_expr);
         * result.addInstruction(OP_dxnslate);
         */

        currentScope.setSetsDxns();

        result += ns_expr;

        return result;
    }

    public String reduce_deleteBracketExpr(IASNode iNode, String stem, String index)
    {
        // ASC doesn't check bracket expressions, and it's difficult
        // to see what could usefully be done in any case.
        // currentScope.getMethodBodySemanticChecker().checkDeleteExpr(iNode);
        String result = createInstruction(iNode, stem.length() + index.length() + 1);

        /*
         * result.addAll(stem); result.addAll(index);
         * result.addInstruction(OP_deleteproperty);
         */

        // Delete expressions.
        // Pattern deleteBracketExpr
        // String stem
        // String index

        if (isCurrentFramework())
            result += "delete(" + stem + "[" + index + "])";
        else
            result += "adobe.deleteProperty(" + JSSharedData.THIS + ", " + stem + ", " + index + ")";

        return result;
    }

    public String reduce_deleteAtBracketExpr(IASNode iNode, String stem, String index)
    {
        // TODO: added for http://bugs.adobe.com/jira/browse/CMP-1511
        throw new UnsupportedOperationException();
    }

    public String reduce_deleteDescendantsExpr(IASNode iNode, String stem, Binding field)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "E4X (ECMA-357)"));

        // TODO: Investigate semantics of this.
        // This special case should get a warning or some indication it's a tautology.

        String result = createInstruction(iNode, stem.length() + 1);
        /*
         * result.addAll(stem); result.addInstruction(OP_pop);
         * result.addInstruction(OP_pushtrue);
         */
        return result;
    }

    public String reduce_deleteMemberExpr(IASNode iNode, String stem, Binding field)
    {
        currentScope.getMethodBodySemanticChecker().checkDeleteExpr(iNode, field);

        String result = createInstruction(iNode, stem.length() + 1);
        /*
         * result.addAll(stem); result.addInstruction(OP_deleteproperty,
         * field.getName());
         */

        // Delete expressions.
        // Pattern deleteMemberExpr
        // String stem
        // Binding field

        if (isCurrentFramework())
            result += "delete(" + stem + "." + getBasenameFromBinding(field) + ")";
        else
            result += "adobe.deleteProperty(" + JSSharedData.THIS + ", " + stem + ", \"" + getBasenameFromBinding(field) + "\")";
        return result;
    }

    public String reduce_deleteRuntimeNameExpr(IASNode iNode, String stem, RuntimeMultiname rt_name)
    {
        //  TODO: Any relevant semantic checks?
        String result = createInstruction(iNode);

        result += "delete ";
        result += stem;

        // TODO: inject adobe.deleteProperty() ?
        if (rt_name.hasRuntimeQualifier())
            result += rt_name.getRuntimeQualifier(iNode);
        if (rt_name.hasRuntimeName())
            result += rt_name.getRuntimeName(iNode);
        // result.addInstruction(OP_deleteproperty, rt_name.generateName());
        return result;
    }

    public String reduce_deleteNameExpr(IASNode iNode, Binding n)
    {
        currentScope.getMethodBodySemanticChecker().checkDeleteExpr(iNode, n);

        String result = createInstruction(iNode, 2);
        /*
         * result.addInstruction(OP_findproperty, n.getName());
         * result.addInstruction(OP_deleteproperty, n.getName());
         */

        // Delete expressions.
        // Pattern deleteNameExpr
        // Binding n
        if (isCurrentFramework())
            result += "delete(" + JSSharedData.THIS + "." + getBasenameFromBinding(n) + ")";
        else
            result += "adobe.deleteProperty(" + JSSharedData.THIS + ", \"" + getBasenameFromBinding(n) + "\")";

        return result;
    }

    public String reduce_deleteExprExprExpr(IASNode iNode, String expr)
    {
        return "delete(" + expr + ")";
    }

    public String reduce_doStmt(IASNode iNode, String body, String cond)
    {
        String result = createInstruction(iNode, cond.length() + body.length() + 1);

        /*
         * currentScope.getFlowManager().resolveContinueLabel(cond); if (
         * body.firstElement().getOpcode() != OP_label )
         * result.addInstruction(OP_label); result.addAll(body);
         * result.addAll(cond); result.addInstruction(OP_iftrue,
         * result.getLabel()); if ( ! isLabeledSubstatement(iNode) )
         * currentScope.getFlowManager().finishControlFlowContext(result);
         */

        // Pattern doStmt
        // DoWhileLoopID(statement body, expression cond);

        result += indent() + "do" + endl();
        result += indent() + "{" + endl();
        result += indentBlock(body, 1);
        result += indent() + "} while(" + cond + ");" + endl();
        return result;
    }

    public String reduce_e4xFilter(IASNode iNode, String stem, String filter)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "E4X (ECMA-357)"));
        String result = createInstruction(iNode);

        /*
         * LexicalScope.Hasnext2Wrapper hasnext = currentScope.hasnext2(); //
         * Cache each value of e.employee. Binding value_temp =
         * currentScope.allocateTemp(); // Cache the result XMLList. Binding
         * result_temp = currentScope.allocateTemp(); // Emit the stem
         * expression and cache // the stem object in a local register.
         * result.addAll(stem); result.addInstruction(OP_checkfilter);
         * result.addInstruction(OP_coerce_a);
         * result.addInstruction(hasnext.stem_temp.setlocal() ); // Initialize
         * the index register. result.addInstruction(OP_pushbyte, 0);
         * result.addInstruction(hasnext.index_temp.setlocal()); // Create the
         * result XMLList. result.addInstruction(OP_findpropstrict,
         * xmlListType); result.addInstruction(OP_getproperty, xmlListType);
         * result.addInstruction(OP_pushstring, "");
         * result.addInstruction(OP_construct, 1);
         * result.addInstruction(result_temp.setlocal() ); // Jump to the loop
         * test. Label test = new Label(); result.addInstruction(OP_jump, test);
         * // Top of the loop: put an AET Label on // the ABC OP_Label. Label
         * loop = new Label(); result.labelNext(loop);
         * result.addInstruction(OP_label); // Get the next value and cache it.
         * result.addInstruction(hasnext.stem_temp.getlocal());
         * result.addInstruction(hasnext.index_temp.getlocal());
         * result.addInstruction(OP_nextvalue); result.addInstruction(OP_dup);
         * result.addInstruction(value_temp.setlocal() ); // See ECMA-357 11.2.4
         * result.addInstruction(OP_pushwith); result.addAll(filter); Label
         * no_match = new Label(); result.addInstruction(OP_iffalse, no_match);
         * result.addInstruction(result_temp.getlocal() );
         * result.addInstruction(hasnext.index_temp.getlocal() );
         * result.addInstruction(value_temp.getlocal() );
         * result.addInstruction(OP_setproperty); result.labelNext(no_match); //
         * See ECMA-357 11.2.4 result.addInstruction(OP_popscope);
         * result.labelNext(test); result.addInstruction(hasnext.String);
         * result.addInstruction(OP_iftrue, loop);
         * result.addInstruction(result_temp.getlocal());
         * result.addAll(hasnext.release());
         * result.addAll(currentScope.releaseTemp(value_temp));
         * result.addAll(currentScope.releaseTemp(result_temp));
         */
        return result;
    }

    public String reduce_embed(IASNode iNode)
    {/*
      * String name = ""; try { name =
      * ((EmbedNode)iNode).getName(currentScope.getProject(), getProblems()); }
      * catch (InterruptedException e) { // Throw a RuntimeException here rather
      * than the InterruptedException // so that we don't need to modify the
      * throws clause of all calling functions. //
      * ABCGenerator.handleBurmError() will catch the RuntimeException, check //
      * that the cause is a InterruptedException and respond accordingly. throw
      * new RuntimeException(e); } Name embedName = new Name(name);
      */
        String result = createInstruction(iNode);
        /*
         * result.addInstruction(OP_findproperty, embedName);
         * result.addInstruction(OP_getproperty, embedName);
         */

        return result;
    }

    /*
     * FOR EACH gives you access to the element in the array. FOR IN gives you
     * access to the index of the array.
     */

    public String reduce_forKeyValueStmt(IASNode iNode, Binding it, String base, String body, int opcode)
    {
        String result = generateForKeyOrValueLoop(iNode, opcode, it, base, body);
        result += reduce_forLoop(iNode, it, base, body, false);
        return result;
    }

    public String reduce_forKeyValueArrayStmt(IASNode iNode, String stem, String index, String base, String body, int opcode, boolean is_super)
    {
        throw new IllegalStateException("Not implemented: for each(a[i] in ...)");
    }

    public String reduce_forKeyValueMemberStmt(IASNode iNode, String stem, Binding member, String base, String body, int opcode, boolean is_super)
    {
        throw new IllegalStateException("Not implemented: for each(x.y in ...)");
    }

    private IDefinition getDefinitionForNode(IASNode iNode, Boolean toRight)
    {
        // This is based on an idea Peter came up with...
        try
        {
            IASNode node = iNode;
            while (node != null)
            {
                if (node instanceof IBinaryOperatorNode && !(node instanceof MemberAccessExpressionNode))
                {
                    if (toRight)
                        node = ((IBinaryOperatorNode)node).getRightOperandNode();
                    else
                        node = ((IBinaryOperatorNode)node).getLeftOperandNode();
                }
                else if (node instanceof IFunctionCallNode)
                    node = ((IFunctionCallNode)node).getNameNode();
                else if (node instanceof IDynamicAccessNode)
                    node = ((IDynamicAccessNode)node).getLeftOperandNode();
                else if (node instanceof IUnaryOperatorNode)
                    node = ((IUnaryOperatorNode)node).getOperandNode();
                // return new Binding(iNode,makeName(member),((IUnaryOperatorNode)node).getDefinition());
                else if (node instanceof IForLoopNode)
                    node = ((IForLoopNode)node).getChild(0).getChild(0);
                else if (node instanceof IVariableNode)
                {
                    if (toRight)
                        node = ((IVariableNode)node).getAssignedValueNode();
                    else
                        node = ((IVariableNode)node).getVariableTypeNode();
                }
                else if (node instanceof IExpressionNode)
                {
                    IDefinition def = ((IExpressionNode)node).resolve(currentScope.getProject());
                    if (def instanceof VariableDefinition)
                    {
                        final VariableDefinition variable = (VariableDefinition)def;
                        def = variable.resolveType(currentScope.getProject());
                    }
                    else if (def instanceof FunctionDefinition)
                    {
                        final FunctionDefinition functionDef = (FunctionDefinition)def;
                        final IReference typeRef = functionDef.getReturnTypeReference();
                        if (typeRef != null)
                            def = typeRef.resolve(currentScope.getProject(), (ASScope)getScopeFromNode(iNode), DependencyType.INHERITANCE, false);
                    }
                    else if (def instanceof IGetterDefinition)
                    {
                        // return new Binding(iNode,makeName(member),leftDef);
                        final ITypeDefinition returnType = ((IGetterDefinition)def).resolveReturnType(currentScope.getProject());
                        def = m_sharedData.getDefinition(returnType.getQualifiedName());
                    }

                    if (def != null && def instanceof ClassDefinition)
                    {
                        return def;
                        /*
                         * IDefinition memberDef = getDefinitionForMember(
                         * iNode, def, removeQuotes(member),
                         * ClassificationValue.MEMBERS_AND_TYPES ); if(
                         * memberDef != null ) return new
                         * Binding(iNode,makeName(member),memberDef);
                         */
                    }
                    node = null;
                }
                else
                {
                    node = null;
                }
            }
        }
        catch (Exception e)
        {
            // getDefinitionForNode(iNode,toRight);

            // getDefinition() sometimes crashes, e.g. when looking at a cast to an interface in some cases,
            // FunctionDefinition.getParameters() returns null and ExpressionNodeBase.determineIfFunction() chokes on it
            printWarning(iNode, "getDefinitionForNode() failed for" + iNode);
        }
        return null;
    }

    private Boolean isDataClassDefinition(IDefinition def)
    {
        if (def != null && def instanceof IClassDefinition)
        {
            IClassDefinition classDef = (IClassDefinition)def;
            Iterator<IClassDefinition> superClassIterator = classDef.classIterator(currentScope.getProject(), true);

            while (superClassIterator.hasNext())
            {
                final IClassDefinition superClass = superClassIterator.next();
                if (superClass.getMetaTagByName("DataClass") != null)
                    return true;
            }
        }
        return false;
    }

    private Boolean isProxyDefinition(IDefinition def)
    {
        if (def != null && def instanceof IClassDefinition)
        {
            IClassDefinition classDef = (IClassDefinition)def;
            Iterator<IClassDefinition> superClassIterator = classDef.classIterator(currentScope.getProject(), true);

            while (superClassIterator.hasNext())
            {
                final IClassDefinition superClass = superClassIterator.next();
                final String superClassName = superClass.getQualifiedName();

                if (superClassName.equals("flash.utils.Proxy"))
                    return true;
            }
        }
        return false;
    }

    /*
     * FOR EACH gives you access to the element in the array. FOR IN gives you
     * access to the index of the array.
     */
    private String reduce_forLoop(IASNode iNode, Binding it, String base, String body, Boolean isForInLoop)
    {
        final Boolean convertLocalVarsToMembers = convertLocalVarsToMembers(iNode);

        if (convertLocalVarsToMembers)
        {
            // support for [ConvertLocalVarsToMembers]
            // reduce_forEachStmt() registers variables as members if convertLocalVarsToMembers(iNode) 
            // is true via switching to the class scope.
            LexicalScope scope = currentScope.getEnclosingFrame();
            Boolean resetTraitsVisitor = false;
            if (scope.traitsVisitor == null)
            {
                final JSEmitter emitter = (JSEmitter)currentScope.getEmitter();
                scope.traitsVisitor = emitter.getCurrentClassVisitor().visitInstanceTraits();
                resetTraitsVisitor = true;
            }

            scope.makeVariable(it);

            if (resetTraitsVisitor)
                scope.traitsVisitor = null;

            this.registerConvertedMember(getBasenameFromBinding(it));
        }

        final IDefinition def = getDefinitionForNode(iNode, true);
        final String container = base; // bindingToString(__p, base,true,true);
        // final ASDefinitionCache cache = currentScope.getDefinitionCache();
        final String baseName = getBasenameFromBinding(it);
        final Boolean isFramework = getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME);

        String result = "";
        if (isFramework || isDataClassDefinition(def))
        {
            result += indent() + "for(" + getVarSnippet(iNode, baseName) + " in ";
            result += container;
            result += ")" + endl();
            result += "{" + endl();
            if (!isForInLoop)
            {
                final IDefinition itDef = it.getDefinition();
                if (itDef != null &&
                    itDef.resolveType(currentScope.getProject()) == currentScope.getProject().getBuiltinType(BuiltinType.STRING))
                {
                    result += indentBlock(baseName + " = " + container + "[" + baseName + "].toString();", 1);
                }
                else
                    result += indentBlock(baseName + " = " + container + "[" + baseName + "];", 1);
            }
            result += indentBlock(body, 1);
            result += "}" + endl();
        }
        /*
         * TODO: This code does not work because the body may contain 'break',
         * 'continue', or 'return' statements. For now we cannot else if(def ==
         * null || def == cache.getBuiltinType(BuiltinType.ANY_TYPE) || def ==
         * cache.getBuiltinType(BuiltinType.OBJECT) ) { final String
         * forLoopFunction = isForInLoop ? "forInLoop" : "forEachLoop"; result
         * += indent() + "adobe." + forLoopFunction + "( " + container + ",\n";
         * result += indentBlock( "function(" + baseName + ")\n", 2 ); result +=
         * indentBlock( "{" + endl(), 2 ); result += indentBlock( body, 3 );
         * result += indentBlock( "});" + endl(), 2 ); }
         */
        else if (isProxyDefinition(def))
        {
            final String typeComment = isForInLoop ? "/** @type {string} */\n" : "/** @type {object} */\n";
            final String nextFunction = isForInLoop ? "nextName" : "nextValue";
            final String nextName = baseName + "_next";
            result += "\n";
            result += "// Inlining for..in expression for Proxy object.\n";
            result += "/** @type {number} */\n";
            result += "var " + nextName + " = " + container + ".nextNameIndex(0);\n";
            result += "while(" + nextName + " > 0)\n";
            result += "{\n";
            result += indentBlock(typeComment, 1);
            result += indentBlock("var " + baseName + " = " + container + "." + nextFunction + "(" + nextName + ");\n", 1);
            result += indentBlock(body, 1);
            result += indentBlock(nextName + " = " + container + ".nextNameIndex(" + nextName + ");\n", 1);
            result += "}\n";
        }
        else
        {
            result += indent() + "for(" + getVarSnippet(iNode, baseName) + " in ";
            if (getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME))
                result += container;
            else
                result += "adobe.getOwnProperties(" + container + ")";

            result += ")" + endl();
            result += "{" + endl();
            if (!isForInLoop)
            {
                final IDefinition itDef = it.getDefinition();
                if (itDef != null &&
                    itDef.resolveType(currentScope.getProject()) == currentScope.getProject().getBuiltinType(BuiltinType.STRING))
                    result += indentBlock(baseName + " = " + container + "[" + baseName + "].toString();", 1);
                else
                    result += indentBlock(baseName + " = " + container + "[" + baseName + "];", 1);
            }
            result += indentBlock(body, 1);
            result += "}" + endl();
        }

        return result;
    }

    public String reduce_forInArrayStmt(IASNode iNode, String base, String stem, String index, String body)
    {
        throw new IllegalStateException("Not implemented: for (a[i] in ...)");
    }

    public String reduce_forInMemberStmt(IASNode iNode, String base, String stem, String index, String body)
    {
        throw new IllegalStateException("Not implemented: for (x.y in ...)");
    }

    public String reduce_forVarDeclInStmt(IASNode __p, String single_decl, String base, String body, int opNextvalue)
    {
        throw new IllegalStateException("Not implemented: for (var x = 100 in ...)");
    }

    public String reduce_functionAsBracketExpr(IASNode iNode, String stem, String index, Vector<String> args)
    {
        //  TODO: Investigate optimizing this.
        String result = createInstruction(iNode);

        // Primary expressions.
        // expression = Pattern functionAsBracketExpr : 4
        result += indent();
        String extraParams = "";
        final String name = index; //removeQuotes(index);

        final Binding memberBinding = makeMemberBinding(iNode, stem, index);

        final IDefinition def = memberBinding.getDefinition();

        // constant propagation for initialized members
        if (!m_hasSideEffects && hasSideEffects(def))
            m_hasSideEffects = true;

        // DataClass classes must not declare methods.
        if (isDataClass(currentScope.getProject(), def))
            currentScope.addProblem(new JSDataClassMethodError(iNode));

        // dynamic classes need to call adobe.callProperty
        if (!isCurrentFramework() && (def == null || def.isDynamic()))
        {
            registerAccessedPropertyName(iNode, name);
            result += indent() + "adobe.callProperty( this, " + stem + ", " + name + ", [";
            result += collectCallParameters(iNode, stem, memberBinding, args, JSSharedData.m_useSelfParameter, extraParams);
            result += "])";
        }
        else
        {
            // result += getRootName() + ".";
            if (m_convertToStatic)
            {
                String owner = getTypeOfInstance(iNode, stripNS(stem));
                if (owner.length() == 0 || isDataType(owner) || isBrowserDataType(owner) || owner.equals(JSSharedData.JS_FRAMEWORK_NAME))
                    result += stripNS(stem);
                else
                {
                    owner = findClassWithMethod(owner, name);
                    result += createFullName(owner, true);
                }
                result += "." + name;
            }
            else
            {
                result += stripNS(stem);
                if (!stem.equals(JSSharedData.THIS + "." + JSSharedData._SUPER))
                    result += "[" + name + "]";
            }

            result += "(";
            result += collectCallParameters(iNode, stem, memberBinding, args, JSSharedData.m_useSelfParameter, extraParams);
            result += ")";
        }
        return result;
    }

    public String reduce_functionAsMemberExpr(IASNode iNode, String stem, Binding method_name, Vector<String> args)
    {

        currentScope.getMethodBodySemanticChecker().checkFunctionCall(iNode, method_name, args);

        //  TODO: Investigate optimizing this.
        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); for ( String arg: args) result.addAll(arg);
         * result.addInstruction(OP_callproperty, new Object[]
         * {method_name.getName(), args.size() } );
         */

        // Primary expressions.
        // Pattern functionAsMemberExpr, expression
        // FunctionCallID(MemberAccessExpressionID(expression stem, name method_name), expressionList args);

        final String basename = getBasenameFromBinding(method_name);

        IDefinition def = method_name.getDefinition();

        // items from Vectors trigger adobe.getProperty() 
        if (def == null && iNode instanceof IFunctionCallNode)
        {
            final Binding b = this.makeMemberBinding(iNode, stem, basename);
            def = b.getDefinition();
        }

        // constant propagation for initialized members
        if (!m_hasSideEffects && hasSideEffects(def))
            m_hasSideEffects = true;

        // DataClass classes must not declare methods.
        if (isDataClass(currentScope.getProject(), def))
            currentScope.addProblem(new JSDataClassMethodError(iNode));

        // dynamic classes need to call adobe.callProperty
        if (!isCurrentFramework() && (def == null || def.isDynamic()))
        {
            registerAccessedPropertyName(iNode, basename);
            result += indent() + "adobe.callProperty( this, " + stem + ", \"" + basename + "\", [";
            result += collectCallParameters(iNode, "", method_name, args, JSSharedData.m_useSelfParameter, "");
            result += "])";
        }
        else
        {
            result += indent();

            // result += getRootName() + ".";
            if (m_convertToStatic)
            {
                String owner = getTypeOfInstance(iNode, stripNS(stem));
                if (owner.length() == 0 || isDataType(owner) || isBrowserDataType(owner) || owner.equals(JSSharedData.JS_FRAMEWORK_NAME))
                    result += stripNS(stem);
                else
                {
                    owner = findClassWithMethod(owner, basename);
                    result += createFullName(owner, true);
                }
                result += "." + basename;
            }
            else
            {
                result += stripNS(stem);
                if (!stem.equals(JSSharedData.THIS + "." + JSSharedData._SUPER))
                    result += "." + basename;
            }
            result += "(";
            result += collectCallParameters(iNode, stem, method_name, args, JSSharedData.m_useSelfParameter, "");
            result += ")";
        }
        return result;
    }

    public String reduce_functionAsRandomExpr(IASNode iNode, String random_expr, Vector<String> args)
    {
        //  TODO: Investigate optimizing this.
        String result = createInstruction(iNode);

        // constant propagation for initialized members
        m_hasSideEffects = true;

        // random_expr can be just a comment (see reduce_parameterizedName_expression)
        // i.e. in fl.motion.MatrixTransformer3D you'll find this line of code:
        // var vec3D:Vector.<Number> = Vector.<Number>(getRawDataVector(mat)); 
        final Boolean isComment = random_expr.startsWith("/*") && random_expr.endsWith("*/");

        if (!isComment)
            result += "(" + random_expr + ")";

        result += "(";
        result += collectCallParameters(iNode, "", makeBinding(iNode, random_expr), args, JSSharedData.m_useSelfParameter, "");
        result += ")";

        if (isComment)
            result += " " + random_expr;
        return result;
    }

    public String reduce_functionCallExpr_to_expression(IASNode iNode, Binding method_name, Vector<String> args)
    {
        return reduce_functionCall_common(iNode, method_name, args, true);
    }

    public String reduce_functionCallExpr_to_void_expression(IASNode iNode, Binding method_name, Vector<String> args)
    {
        return reduce_functionCall_common(iNode, method_name, args, false);
    }

    private String reduce_functionCall_common(IASNode iNode, Binding method_name, Vector<String> args, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkFunctionCall(iNode, method_name, args);
        String result = createInstruction(iNode);

        // Primary expressions.
        // Pattern functionCallExpr, expression
        // FunctionCallID(name method_name, expressionList args);

        // Sometimes, what looks syntactically like a function call is actally a type-cast expression. And
        // sometimes, a cast expression is actually a 'new' operator in disguise. Either way, we shouldn't
        // generate function call syntax in JS, since in JS that syntax *always* means a functon call.
        result = handleCastExpr(iNode, method_name, args);
        if (result != null)
            return result;

        String extraParams = "";
        String mname = bindingToString(iNode, method_name, true, true);

        // dynamic classes need to call adobe.callProperty
        final IDefinition def = method_name.getDefinition();

        // constant propagation for initialized members
        if (!m_hasSideEffects && hasSideEffects(def))
            m_hasSideEffects = true;

        if (mname.contains(".") && !isCurrentFramework() && (def == null || def.isDynamic()))
        {
            final String stem = mname.substring(0, mname.lastIndexOf("."));
            final String basename = mname.substring(mname.lastIndexOf(".") + 1);
            registerAccessedPropertyName(iNode, basename);
            result = indent() + "adobe.callProperty( this, " + stem + ", \"" + basename + "\", [";
            result += collectCallParameters(iNode, "", method_name, args, JSSharedData.m_useSelfParameter, "");
            result += "])";
        }
        else
        {
            if (JSSharedData.m_useClosureLib)
            {
                String fname = "";
                if (currentScope != null &&
                    currentScope.getMethodInfo() != null &&
                    currentScope.getMethodInfo().getMethodName() != null)
                {
                    fname = currentScope.getMethodInfo().getMethodName();
                }

                if (mname.equals(JSSharedData.THIS + "." + JSSharedData.CLASS_NAME + ".superClass_"))
                {
                    if (!fname.isEmpty())
                    {
                        mname += "." + fname + ".call";
                        extraParams = JSSharedData.THIS;
                    }
                }
                else if (mname.equals("goog.base"))
                {
                    if (!fname.isEmpty())
                        extraParams = "this, \"" + fname + "\"";
                    else
                        extraParams = "this, undefined";
                }
                else if (mname.equals("adobe.base"))
                {
                    printWarning(iNode, "Hand-written adobe.base() calls are not supported");
                    assert false;
                }
            }

            result = indent() + mname + "(";
            result += collectCallParameters(iNode, "", method_name, args, JSSharedData.m_useSelfParameter, extraParams);
            result += ")";
        }

        if (!need_result)
            result += ";" + endl();

        return result;
    }

    public String reduce_functionCallOfSuperclassMethod_to_expression(IASNode iNode, String stem, Binding method_name, Vector<String> args)
    {
        //  TODO: Investigate optimizing this.
        String result = createInstruction(iNode);

        /*
         * result.addInstruction(OP_getlocal0); for ( String arg: args)
         * result.addAll(arg); result.addInstruction(OP_callsuper, new Object[]
         * {method_name.getName(), args.size() } );
         */

        // Note: if method_name == null then this is a super() call inside a constructor.
        // If method_name != null then this is a super.foo() call (which can be anywhere).

        // Could this be a ctor super() call where the superclass is Object? We don't want to emit anything
        // for that no-op case since goog.base() chokes when the super is Object.
        if (method_name == null && args.size() == 0)
        {
            if (getEmitter().getCurrentSuperClassFullName().equals("Object"))
                return "";
        }

        // constant propagation for initialized members
        if (!m_hasSideEffects && method_name != null)
        {
            final IDefinition def = method_name.getDefinition();
            if (!m_hasSideEffects && hasSideEffects(def))
                m_hasSideEffects = true;
        }

        // goog.base() doesn't support the construct A() { super.B() } - need to use custom adobe.base() instead
        boolean needAdobeBase = false;
        String invokingMethodName = null;
        IClassDefinition invokingMethodClass = null;
        if (method_name != null)
        {
            String invokingMethodRawName = getCurrentMethodName(); //method we're looking at, containing the super.foo() call
            IFunctionDefinition enclosingDef = getCurrentMethod(iNode);
            invokingMethodName = mungeMethodName(invokingMethodRawName, enclosingDef, false);

            String superMethodName = getBasenameFromBinding(method_name); // method being invoked via super, e.g. "foo" in super.foo()
            if (!invokingMethodName.equals(superMethodName))
            {
                needAdobeBase = true;
                invokingMethodClass = (IClassDefinition)enclosingDef.getParent();
            }
        }

        String extraParams = "";
        Boolean addSelfParameter = JSSharedData.m_useSelfParameter;
        String _name;
        if (JSSharedData.m_useClosureLib)
        {
            if (!needAdobeBase)
            {
                _name = "goog.base";
                extraParams = "this";
                if (method_name != null)
                    extraParams += ", \"" + getBasenameFromBinding(method_name) + "\"";
            }
            else
            {
                _name = "adobe.base";
                extraParams = "this";
                extraParams += ", " + getSuperMethodReference(invokingMethodClass, getBasenameFromBinding(method_name));
            }
        }
        else
        {
            if (m_convertToStatic)
            {
                String fullClassName = createFullClassName(false);
                if (m_sharedData.hasClass(makeName(fullClassName)))
                {
                    fullClassName = getCurrentSuperClassFullName();
                    _name = fullClassName;
                }
                else
                    _name = createFullClassName(true) + "." + JSSharedData._SUPER;

                if (this.m_functionName.length() > 0)
                    _name += "." + this.m_functionName;
            }
            else
            {
                final Boolean isFramework = getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME);
                final String thisName = isFramework ? "this" : JSSharedData.THIS;
                _name = "goog.base(this, \"" + invokingMethodName + "\", ";
            }
        }

        result += indent() + _name;
        result += collectCallParameters(iNode, "", method_name, args, addSelfParameter, extraParams);

        result += ")";
        return result;
    }

    public String reduce_functionCallOfSuperclassMethod_to_void_expression(IASNode iNode, String stem, Binding method_name, Vector<String> args)
    {
        String result = createInstruction(iNode);
        /*
         * result.addInstruction(OP_getlocal0); for ( String arg: args)
         * result.addAll(arg); result.addInstruction(OP_callsupervoid, new
         * Object[] {method_name.getName(), args.size() } );
         */

        // void_expression = Pattern functionCallOfSuperclassMethod : 1
        // Binding method_name, Vector<InstructionList> args
        result += reduce_functionCallOfSuperclassMethod_to_expression(iNode, stem, method_name, args) + ";" + endl();

        return result;
    }

    public String reduce_groupedVoidExpression(IASNode iNode, Vector<String> contents)
    {
        String result = createInstruction(iNode);

        for (String expr : contents)
            result += expr;

        return result;
    }

    public String reduce_ifElseIf(IASNode iNode, String test, String then, Vector<ConditionalFragment> if_elseif)
    {
        //  TODO: Experiment with optimizing this.
        //  Iterating twice over the arms of the if
        //  might cost more than it helps.
        String result = createInstruction(iNode);

        /*
         * boolean has_else = !if_elseif.isEmpty(); // Label to the
         * "next statement." Label tail = new Label(); // TODO: Use
         * conditionalJump style tests. result.addAll(test); if ( has_else )
         * result.addInstruction(OP_iffalse,
         * if_elseif.firstElement().getLabel()); else
         * result.addInstruction(OP_iffalse, tail); result.addAll(then); if (
         * has_else ) { result.addInstruction(OP_jump, tail); for ( int i = 0; i
         * < if_elseif.size() - 1; i++ ) { ConditionalFragment alternative =
         * if_elseif.elementAt(i); if (
         * !alternative.isUnconditionalAlternative() ) { // Add the test and
         * conditional jump to the next alternative.
         * result.addAll(alternative.condition); ConditionalFragment
         * next_alternative = if_elseif.elementAt(i+1);
         * result.addInstruction(OP_iffalse, next_alternative.getLabel()); }
         * result.addAll(alternative.statement); result.addInstruction(OP_jump,
         * tail); } ConditionalFragment last_clause = if_elseif.lastElement();
         * if ( !last_clause.isUnconditionalAlternative() ) {
         * result.addAll(last_clause.condition);
         * result.addInstruction(OP_iffalse, tail);
         * result.addAll(last_clause.statement); } else {
         * result.addAll(last_clause.statement); } } result.labelNext(tail);
         */
        // statement = Pattern ifElseIf

        result += indent() + "if (" + (test) + ")" + endl();
        result += indent() + "{" + endl();
        result += indentBlock(then, 1);
        result += indent() + "}" + endl();

        // Vector<ConditionalFragment> if_elseif = (Vector<ConditionalFragment>) raw_if_elseif;
        boolean has_else = !if_elseif.isEmpty();
        if (has_else)
        {
            for (int i = 0; i < if_elseif.size(); i++)
            {
                result += indent() + "else";
                ConditionalFragment alternative = if_elseif.elementAt(i);
                if (!alternative.isUnconditionalAlternative())
                {
                    result += " if (" + alternative.condition + ")";
                }
                result += endl();
                result += indent() + "{" + endl();
                result += indentBlock(alternative.statement, 1);
                result += indent() + "}" + endl();
            }
        }
        return result;
    }

    public String reduce_importDirective(IASNode iNode)
    {
        currentScope.getMethodBodySemanticChecker().checkImportDirective(((IImportNode)iNode));
        return createInstruction(iNode);
    }

    public String reduce_labeledBreakStmt(IASNode iNode)
    {
        final String target = ((IdentifierNode)SemanticUtils.getNthChild(iNode, 0)).getName();
        return "break " + target + ";\n";
    }

    public String reduce_labeledContinueStmt(IASNode iNode)
    {
        final String target = ((IdentifierNode)SemanticUtils.getNthChild(iNode, 0)).getName();
        return "continue " + target + ";\n";
    }

    public String reduce_labeledStmt(IASNode iNode, String label, Vector<String> substatements)
    {
        String result = createInstruction(iNode);

        // statement = Pattern labeledStmt

        result += label + ": ";
        result += "{\n";
        String subs = "";
        for (String substatement : substatements)
            subs += substatement;
        result += indentBlock(subs, 1);
        result += "};\n";

        // currentScope.getFlowManager().finishControlFlowContext(result);
        // if ( iNode.getChildCount() == 2 )
        //    labeledNodes.remove(JSGeneratingReducer.getNthChild(iNode, 1));
        return result;
    }

    public String reduce_gotoStmt(IASNode iNode)
    {
        String result = "";
        assert false : "implement me!!";
        return result;
    }

    public String reduce_labeledStmt(IASNode iNode, String label, String substatement)
    {
        String result = "";
        assert false : "fix me!!!";
        return result;
    }

    public ConditionalFragment reduce_lastElse(IASNode iNode, String else_clause)
    {
        return new ConditionalFragment(iNode, null, else_clause);
    }

    public String reduce_logicalAndExpr(IASNode iNode, String l, String r)
    {
        String result = createInstruction(iNode, l.length() + r.length() + 3);

        // Pattern logicalAndExpr
        // Op_LogicalAndID(expression l, expression r);
        result += binaryOp(l, r, "&&");

        return result;
    }

    public String reduce_logicalNotExpr(IASNode iNode, String expr)
    {
        String result = createInstruction(iNode, expr.length() + 1);

        // Pattern logicalNotExpr
        // Op_LogicalNotID(expression expr);
        result += "(!" + stripTabs(stripNS(expr)) + ")";
        return result;
    }

    public String reduce_logicalOrExpr(IASNode iNode, String l, String r)
    {
        String result = createInstruction(iNode, l.length() + r.length() + 3);

        // Pattern logicalOrExpr
        // Op_LogicalOrID(expression l, expression r);
        result += binaryOp(l, r, "||");

        return result;
    }

    public String reduce_memberAccessExpr(IASNode iNode, String stem, Binding member, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkMemberAccess(iNode, member, opcode);

        String result = createInstruction(iNode, stem.length() + 1);

        // items from Vectors trigger adobe.getProperty() 
        IDefinition def = member.getDefinition();
        if (def == null && iNode instanceof IExpressionNode)
        {
            def = ((IExpressionNode)iNode).resolve(currentScope.getProject());
            if (def != null)
            {
                def = def.getParent();
                if (def != null && def instanceof ClassDefinition)
                {
                    final String basename = getBasenameFromBinding(member);
                    IDefinition memberDef = getDefinitionForMember(iNode, def, removeQuotes(basename), ClassificationValue.MEMBERS_AND_TYPES);
                    if (memberDef != null)
                        member = new Binding(iNode, member.getName(), memberDef);
                }
            }
        }

        // Primary expressions.
        // Pattern memberAccessExpr
        // MemberAccessExpressionID(expression stem, name member);
        result += resolveGetterName(iNode, stem, member, false);

        return result;
    }

    public String reduce_qualifiedMemberAccessExpr(IASNode iNode, String stem, Binding qualifier, Binding member, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkMemberAccess(iNode, member, opcode);
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "qualified member access"));

        String result = createInstruction(iNode);
        return result;
    }

    public String reduce_qualifiedAttributeRuntimeMemberExpr(IASNode iNode, String stem, Binding qualifier, String runtime_member_selector, int opcode)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "qualified attribute runtime member access"));

        String result = createInstruction(iNode);
        return result;
    }

    public String reduce_qualifiedMemberRuntimeNameExpr(IASNode iNode, String stem, Binding qualifier, String runtime_member_selector)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "qualified member runtime member access"));

        String result = createInstructionList(iNode);
        return result;
    }

    public String reduce_qualifiedAttributeExpr(IASNode iNode, String stem, Binding qualifier, Binding member, int opcode)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "qualified attribute expression"));

        String result = createInstruction(iNode);
        return result;
    }

    public String reduce_unqualifiedAttributeExpr(IASNode iNode, String stem, String rt_attr, int opcode)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "unqualified attribute expression"));

        String result = createInstruction(iNode);
        return result;
    }

    public String reduce_runtimeAttributeExp(IASNode iNode, String rt_attr)
    {
        // TODO: Unsupported operation. See also reduce_unqualifiedAttributeExpr().
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "unqualified attribute expression"));
        String result = createInstruction(iNode);
        return result;
    }

    public String reduce_mxmlEventSpecifier(IASNode iNode, Vector<String> stmts)
    {
        //  TODO: Investigate optimizing.
        String body = createInstruction(iNode);

        /*
         * // An MXMLEventSpecifierNode can have N child nodes which are
         * statements. // We need to codegen all of them as an effective method
         * body. 
         */
    	for ( String stmt: stmts ) 
    		body += stmt; 
    	return generateFunctionBody(iNode, body, LexicalScope.anyType);

        // function = Pattern mxmlEventSpecifier
        // ArgumentID(Vector<String> stmts)

        // An MXMLEventSpecifierNode can have N child nodes which are statements.
        // We need to codegen all of them as an effective method body.
        // for ( String stmt: stmts )
        //    body += stmt;

        // The effective method they're inside of has a single parameter.
        // Its name is "event" (which is discovered from the ParameterDefinition
        // that the event node creates).
        // Its type, such as flash.events.MouseEvent, has already been determined
        // when we created the MethodInfo, so we access it there
        // rather than creatinga another equivalent Name.

        // TODO:
        /*
         * currentScope.makeParameter(#MXMLEventSpecifierID#.
         * getEventParameterDefinition(),
         * currentScope.methodInfo.getParamTypes().get(0)); return
         * generateFunctionBody(body, null);
         */

        // return null;
    }

    public Binding reduce_namespaceAccess(IASNode iNode, IASNode qualifier, Binding qualified_name)
    {
        currentScope.getMethodBodySemanticChecker().checkQualifier(qualifier);
        //  All the qualifying syntax nodes are just concrete.
        return qualified_name;
    }

    public RuntimeMultiname reduce_namespaceAccess_to_runtime_name(IASNode iNode, Binding qualified_name)
    {
        String qualifier = createInstruction(iNode);
        qualifier += generateAccess(iNode, reduce_simpleName(iNode.getChild(0)));
        // qualifier.addInstruction(OP_coerce, namespaceType);
        return new RuntimeMultiname(qualifier.toString(), qualified_name.getName());
    }

    public String reduce_namespaceAsName_to_expression(IASNode iNode)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "namespace as name"));

        String result = createInstruction(iNode);
        return result;
    }

    public Name reduce_namespaceAsName_to_multinameL(IASNode iNode)
    {
        return reduce_namespaceAsName_to_multinameL(iNode, false);
    }

    public Name reduce_namespaceAsName_to_multinameL(IASNode iNode, final boolean is_attribute)
    {
        IdentifierNode qualifier = (IdentifierNode)iNode;
        NamespaceDefinition ns = (NamespaceDefinition)qualifier.resolve(currentScope.getProject());

        int name_kind = is_attribute ? CONSTANT_MultinameLA : CONSTANT_MultinameL;
        return new Name(name_kind, new Nsset(ns.resolveAETNamespace(currentScope.getProject())), null);
    }

    public Binding reduce_namespaceAsName_to_name(IASNode iNode)
    {
        return currentScope.resolveName((org.apache.flex.compiler.internal.tree.as.IdentifierNode)iNode);
    }

    public String reduce_namespaceDeclaration(IASNode iNode, Binding ns_name)
    {
        //  Get the AET Namespace from the namespace's definition.
        NamespaceDefinition def = ((NamespaceNode)iNode).getDefinition();
        return reduce_namespaceDeclarationConstantInitializer(iNode, ns_name, def.resolveAETNamespace(currentScope.getProject()));
    }

    public String reduce_namespaceDeclarationConstantInitializer(IASNode iNode, Binding ns_name, String uri)
    {
        // Make a new CONSTANT_Namespace with the given URI - all user defined namespaces initialized with a
        // String literal become CONSTANT_Namespaces
        final Namespace ns = new Namespace(CONSTANT_Namespace, uri);
        return reduce_namespaceDeclarationConstantInitializer(iNode, ns_name, ns);
    }

    /**
     * Does the work for the various reduce_namespaceDecl methods. Creates a new
     * property, with an initial value of the Namespace passed in.
     */
    public String reduce_namespaceDeclarationConstantInitializer(IASNode iNode, Binding ns_name, Namespace initializer)
    {
        NamespaceDefinition def = ((NamespaceNode)iNode).getDefinition();

        switch (SemanticUtils.getMultiDefinitionType(def, currentScope.getProject()))
        {
            case AMBIGUOUS:
                currentScope.addProblem(new DuplicateNamespaceDefinitionProblem(iNode));
                break;
            case NONE:
                break;
            default:
                assert false; // I don't think namespaces can have other type of multiple definitions
        }

        if (initializer == null)
        {
            // Can't declare a namespace if we don't know what its initial value is
            getProblems().add(new InvalidOverrideProblem(iNode));
            return null;
        }

        currentScope.makeNamespace(ns_name, initializer);
        /*
         * if ( ns_name.isLocal() ) { // Locals have no initializer, so the
         * variable // needs explicit initialization code.
         * currentScope.getHoistedInitInstructions
         * ().addInstruction(OP_pushnamespace, initializer);
         * currentScope.getHoistedInitInstructions
         * ().addInstruction(ns_name.setlocal()); }
         */

        // workaround for Falcon bug, see checkintests/as3/AbcDecoder/accessSpecifiers.as
        String uri = initializer.getName();
        uri = removeQuotes(uri);
        m_sharedData.registerNamespace(uri, def);

        return null;
    }

    public String reduce_namespaceDeclarationInitializer(IASNode iNode, Binding ns_name, Binding second_ns)
    {
        String uri;

        if (second_ns.getDefinition() instanceof NamespaceDefinition)
        {
            NamespaceDefinition ns_def = (NamespaceDefinition)second_ns.getDefinition();
            uri = ns_def.resolveAETNamespace(currentScope.getProject()).getName();
        }
        else
        {
            getProblems().add(new InvalidOverrideProblem(iNode));
            return null;
        }

        return reduce_namespaceDeclarationConstantInitializer(iNode, ns_name, uri);
    }

    public RuntimeMultiname reduce_namespaceMultinameL(IASNode iNode, IASNode qualifier_node, String expr)
    {
        Name qualifier = reduce_namespaceAsName_to_multinameL(qualifier_node);
        return new RuntimeMultiname(qualifier, expr);
    }

    public RuntimeMultiname reduce_namespaceRTQName(IASNode iNode, String qualifier, Binding qualified_name)
    {
        return new RuntimeMultiname(qualifier, qualified_name.getName());
    }

    public RuntimeMultiname reduce_namespaceRTQNameL(IASNode iNode, String qualifier, String expr)
    {
        return new RuntimeMultiname(qualifier, expr);
    }

    public String reduce_neqExpr(IASNode iNode, String l, String r)
    {
        /*
         * String result = binaryOp(iNode, l, r, OP_equals);
         * result.addInstruction(OP_not);
         */

        // Binary logical operators.
        // Pattern neqExpr
        // Op_NotEqualID(expression l, expression r) ;
        String result = binaryOp(l, r, "!=");
        return result;
    }

    public Binding reduce_nameToTypeName(Binding name, boolean check_name)
    {
        if (check_name)
        {
            currentScope.getMethodBodySemanticChecker().checkTypeName(name);
        }
        return name;
    }

    public String reduce_newMemberProperty(IASNode iNode, String stem, Binding member, Vector<String> args)
    {
        String result = createInstruction(iNode);

        result += "new ";
        result += stem;
        result += ".";
        result += member.getName().getBaseName();
        result += "(";

        for (int i = 0; i < args.size(); i++)
        {
            if (i > 0)
                result += ",";
            result += args.get(i);
        }

        result += ")";

        return result;
    }

    public String reduce_newAsRandomExpr(IASNode iNode, String random_expr, Vector<String> args)
    {
        //  TODO: Investigate optimizing this.
        String result = createInstruction(iNode);

        /*
         * result.addAll(random_expr); for ( String arg: args)
         * result.addAll(arg); result.addInstruction(OP_construct, args.size()
         * );
         */

        // expression = Pattern newAsRandomExpr : 30
        // String random_expr, Vector<String> args

        // TODO: add test case to Test.as
        result += generateNewCall(iNode, makeBinding(iNode, random_expr), args);

        return result;
    }

    /**
     * Reduces <code>new Array()</code>.
     */
    public String reduce_newEmptyArray(IASNode iNode)
    {
        return "[]";
    }

    /**
     * Reduces <code>new Object()</code>.
     */
    public String reduce_newEmptyObject(IASNode iNode)
    {
        return "{}";
    }

    public String reduce_newExpr(IASNode iNode, Binding class_binding, Vector<String> args)
    {
        // currentScope.getMethodBodySemanticChecker().checkNewExpr(iNode, class_binding, args);

        //  TODO: Investigate optimizing this.

        String result = createInstruction(iNode);

        // Primary expressions.
        // Pattern newExpr
        // FunctionCallID(new_keyword xnew, name class_name, expressionList args);

        //  TODO: Investigate optimizing this.
        final Name class_name = class_binding.getName();

        if (class_name != null && class_name.isTypeName())
        {
            // generateAccess(class_name, result);

            // result.addInstruction(OP_construct, args.size());

            // support for vectors.
            if (isVectorTypeName(class_name))
            {
                Boolean comma = false;
                result = "new Array(";
                for (String arg : args)
                {
                    if (comma)
                        result += ", ";
                    else
                        comma = true;
                    result += arg;
                }
                result += ")";
                result += " /* of " + class_name.getTypeNameParameter().getBaseName() + " */";
            }
            else
            {
                currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "new " + getBasenameFromName(class_name)));
            }
        }
        else
        {
            result = generateNewCall(iNode, class_binding, args);
        }
        return result;
    }

    public String reduce_newVectorLiteral(IASNode iNode, String literal)
    {
        return literal;
    }

    public String reduce_nilExpr_to_conditionalJump(IASNode iNode)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_jump);
         */

        // Pattern nilExpr, conditionalJump
        // NilID(void);
        result += "== null";
        return result;
    }

    public String reduce_nilExpr_to_expression(IASNode iNode)
    {
        String result = createInstruction(iNode, 1);
        /*
         * result.addInstruction(OP_pushundefined);
         */
        return result;
    }

    public Object reduce_nullLiteral_to_constant_value(IASNode iNode)
    {
        return null;
    }

    public String reduce_nullLiteral_to_object_literal(IASNode iNode)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushnull);
         */

        // Pattern nullLiteral
        // object_constant = nullLiteral
        result += "null";

        return result;
    }

    public String reduce_objectLiteral(IASNode iNode, Vector<String> elements)
    {
        //  TODO: Investigate optimizing.
        String result = createInstruction(iNode);

        /*
         * for ( String element: elements ) { result.addAll(element); }
         * result.addInstruction(OP_newobject, elements.size());
         */

        // Pattern objectLiteral
        // ObjectLiteralExpressionID(ContainerID(object_literal_element elements*));

        result += "{";
        boolean comma = false;
        for (String element : (Vector<String>)elements)
        {
            if (comma)
                result += ", ";
            else
                comma = true;
            result += element;
        }
        result += "}";
        return result;
    }

    public String reduce_objectLiteralElement(IASNode iNode, String id, String value)
    {
        String result = createInstruction(iNode, value.length() + 1);

        /*
         * result.addAll(id); // TODO: Push type analysis up through the CG, //
         * so that string constants don't go through // convert_s. See
         * http://bugs.adobe.com/jira/browse/CMP-118
         * result.addInstruction(OP_convert_s); result.addAll(value);
         */

        // Pattern objectLiteralElement
        // ObjectLiteralValuePairID(expression id, expression value);

        final IObjectLiteralValuePairNode node = (IObjectLiteralValuePairNode)iNode;
        final IExpressionNode valueNode = node.getValueNode();
        final IDefinition valueDef = valueNode.resolve(currentScope.getProject());

        result += id;
        result += ": " + injectCreateProxy(iNode, valueDef, value);

        //  TODO: Push type analysis up through the CG,
        //  so that string constants don't go through
        //  convert_s.  See http://bugs.adobe.com/jira/browse/CMP-118
        return result;
    }

    public String reduce_optionalParameter(IASNode iNode, Name param_name, Binding param_type, Object default_value)
    {
        /*
         * currentScope.makeParameter(getParameterContent(iNode),
         * param_type.getName()); currentScope.addDefaultValue(default_value);
         */

        // Pattern optionalParameter
        // ArgumentID(name param_name, name param_type, Object default_value);  

        if (param_type.getName() != null)
            usedTypes.add(getBasenameFromBinding(param_type));
        // this.makeLocalVariable(currentScope, param_name, param_type, param_type);
        this.makeParameter(iNode, ((org.apache.flex.compiler.internal.tree.as.ParameterNode)iNode), currentScope, param_name, param_type, param_type);

        // currentScope.addDefaultValue(default_value);
        if (default_value == null)
            default_value = ABCConstants.NULL_VALUE;
        currentScope.getMethodInfo().addDefaultValue(new PooledValue(default_value));

        return null;
    }

    public Binding reduce_parameterizedTypeName(IASNode iNode, Binding base, Binding param)
    {
        Name param_name = param.getName();
        if (param_name.couldBeAnyType())
            param_name = null;
        return new Binding(iNode, new Name(base.getName(), param_name), null);
    }

    public String reduce_parameterizedTypeExpression(IASNode iNode, String base, String param)
    {
        String result = createInstruction(iNode);

        /*
         * result.addAll(base); generateTypeNameParameter(param.getName(),
         * result); result.addInstruction(OP_applytype, 1);
         */

        // expression = Pattern parameterizedName : 1
        // Binding base, Binding param

        // We get here due to AS code that looks like this:  (note: no "new" operator)
        //     Vector.<String>( ["foo", "bar"] )
        // The resulting emitted JS will look like this:
        //     (adobe.arrayToVector)( ["foo", "bar"] )
        // (which is a function call, despite the extra parens)
        result += "adobe.arrayToVector";

        return result;
    }

    public String reduce_plist(IASNode iNode, Vector<String> pdecl)
    {
        //  Parameter processing is done by side effect
        //  in the parameter reduction.
        return null;
    }

    public String reduce_postDecBracketExpr(IASNode iNode, String stem, String index, boolean need_result)
    {
        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); Binding stem_tmp = currentScope.allocateTemp();
         * result.addInstruction(OP_dup);
         * result.addInstruction(stem_tmp.setlocal() ); result.addAll(index);
         * result.addInstruction(OP_dup); // Get the initial value, and dup a
         * copy // onto the stack as the value of this expression. Binding
         * index_tmp = currentScope.allocateTemp();
         * result.addInstruction(index_tmp.setlocal() );
         * result.addInstruction(OP_getproperty); result.addInstruction(OP_dup);
         * result.addInstruction(OP_decrement); Binding result_tmp =
         * currentScope.allocateTemp();
         * result.addInstruction(result_tmp.setlocal() );
         * result.addInstruction(stem_tmp.getlocal() );
         * result.addInstruction(index_tmp.getlocal() );
         * result.addInstruction(result_tmp.getlocal() );
         * result.addInstruction(OP_setproperty);
         * result.addAll(currentScope.releaseTemp(stem_tmp));
         * result.addAll(currentScope.releaseTemp(index_tmp));
         * result.addAll(currentScope.releaseTemp(result_tmp));
         */

        // prefix unary operator.
        // Pattern postDecBracketExpr
        // Op_PostDecrID(ArrayIndexExpressionID(expression stem, expression index));
        // String result = indent() + "(" + stem + "[" + index + "]--" + ")";
        result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, index), "-=", "1", true, true) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_postDecMemberExpr(IASNode iNode, String stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false);

        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); result.addInstruction(OP_dup);
         * result.addInstruction(OP_getproperty, field.getName()); // Save a
         * copy of the original value to use as the value of this expression.
         * result.addInstruction(OP_dup); Binding orig_tmp =
         * currentScope.allocateTemp();
         * result.addInstruction(orig_tmp.setlocal() );
         * result.addInstruction(OP_decrement);
         * result.addInstruction(OP_setproperty, field.getName());
         * result.addInstruction(orig_tmp.getlocal() );
         * result.addAll(currentScope.releaseTemp(orig_tmp));
         */

        // prefix unary operator.
        // Pattern postDecMemberExpr
        // Op_PostDecrID(MemberAccessExpressionID(expression stem, name field));
        // String result = indent() + "(" + stem + "." + getBasenameFromBinding(field) + "--" + ")";
        result += "(" + resolveSetterName(iNode, stem, field, "-=", "1", false, true) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_postDecNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false);

        String result = createInstruction(iNode);

        /*
         * if ( unary.isLocal() ) { result.addInstruction(unary.getlocal());
         * result.addInstruction(unary.declocal()); } else { Name n =
         * unary.getName(); result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_getproperty,n);
         * result.addInstruction(OP_dup); result.addInstruction(OP_decrement);
         * result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_swap);
         * result.addInstruction(OP_setproperty,n); }
         */

        // postfix unary operator.
        // Pattern postDecNameExpr
        // Op_PostDecrID(name unary);
        // final String result = indent() + "(" + bindingToString(iNode,unary, true, true) + "--" + ")";

        final String methodName = bindingToString(iNode, unary, true, true);
        if (methodName.contains("."))
        {
            final String stem = methodName.substring(0, methodName.lastIndexOf("."));
            final String member = getBasenameFromBinding(unary);
            result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, member), "-=", "1", false, true) + ")";
        }
        else
        {
            result += indent() + "(" + methodName + "--" + ")";
        }

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_postIncBracketExpr(IASNode iNode, String stem, String index, boolean need_result)
    {
        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); Binding stem_tmp = currentScope.allocateTemp();
         * result.addInstruction(OP_dup);
         * result.addInstruction(stem_tmp.setlocal() ); result.addAll(index);
         * result.addInstruction(OP_dup); // Get the initial value, and dup a
         * copy // onto the stack as the value of this expression. Binding
         * index_tmp = currentScope.allocateTemp();
         * result.addInstruction(index_tmp.setlocal() );
         * result.addInstruction(OP_getproperty); result.addInstruction(OP_dup);
         * result.addInstruction(OP_increment); Binding result_tmp =
         * currentScope.allocateTemp();
         * result.addInstruction(result_tmp.setlocal());
         * result.addInstruction(stem_tmp.getlocal() );
         * result.addInstruction(index_tmp.getlocal() );
         * result.addInstruction(result_tmp.getlocal() );
         * result.addInstruction(OP_setproperty);
         * result.addAll(currentScope.releaseTemp(stem_tmp));
         * result.addAll(currentScope.releaseTemp(index_tmp));
         * result.addAll(currentScope.releaseTemp(result_tmp));
         */

        // prefix unary operator.
        // Pattern postIncBracketExpr
        // Op_PostIncrID(ArrayIndexExpressionID(expression stem, expression index));
        // String result = indent() + "(" + stem + "[" + index + "]++" + ")";
        result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, index), "+=", "1", true, true) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_postIncMemberExpr(IASNode iNode, String stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true);

        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); result.addInstruction(OP_dup);
         * result.addInstruction(OP_getproperty, field.getName()); // Save a
         * copy of the original value to use as the value of this expression.
         * result.addInstruction(OP_dup); Binding orig_tmp =
         * currentScope.allocateTemp();
         * result.addInstruction(orig_tmp.setlocal() );
         * result.addInstruction(OP_increment);
         * result.addInstruction(OP_setproperty, field.getName());
         * result.addInstruction(orig_tmp.getlocal() );
         * result.addAll(currentScope.releaseTemp(orig_tmp));
         */

        // prefix unary operator.
        // Pattern postIncMemberExpr
        // Op_PostIncrID(MemberAccessExpressionID(expression stem, name field));

        result += "(" + resolveSetterName(iNode, stem, field, "+=", "1", false, true) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_postIncNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true);

        String result = createInstruction(iNode);

        /*
         * if ( unary.isLocal() ) { result.addInstruction(unary.getlocal());
         * result.addInstruction(unary.inclocal()); } else { Name n =
         * unary.getName(); result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_getproperty,n);
         * result.addInstruction(OP_dup); result.addInstruction(OP_increment);
         * result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_swap);
         * result.addInstruction(OP_setproperty,n); }
         */

        // postfix unary operator.
        // Pattern postIncNameExpr
        // Op_PostIncrID(name unary);

        final String methodName = bindingToString(iNode, unary, true, true);
        if (methodName.contains("."))
        {
            final String stem = methodName.substring(0, methodName.lastIndexOf("."));
            final String member = getBasenameFromBinding(unary);
            result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, member), "+=", "1", false, true) + ")";
        }
        else
        {
            result += indent() + "(" + methodName + "++" + ")";
        }

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_preDecBracketExpr(IASNode iNode, String stem, String index, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false);

        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); Binding stem_tmp = currentScope.allocateTemp();
         * result.addInstruction(OP_dup);
         * result.addInstruction(stem_tmp.setlocal() ); result.addAll(index);
         * result.addInstruction(OP_dup); // Get the initial value. Binding
         * index_tmp = currentScope.allocateTemp();
         * result.addInstruction(index_tmp.setlocal());
         * result.addInstruction(OP_getproperty); // Increment, and dup a copy
         * onto // the stack as the value of this expression.
         * result.addInstruction(OP_decrement); result.addInstruction(OP_dup);
         * Binding result_tmp = currentScope.allocateTemp();
         * result.addInstruction(result_tmp.setlocal());
         * result.addInstruction(stem_tmp.getlocal() );
         * result.addInstruction(index_tmp.getlocal() );
         * result.addInstruction(result_tmp.getlocal() );
         * result.addInstruction(OP_setproperty);
         * result.addAll(currentScope.releaseTemp(stem_tmp));
         * result.addAll(currentScope.releaseTemp(index_tmp));
         * result.addAll(currentScope.releaseTemp(result_tmp));
         */

        // prefix unary operator.
        // Pattern preDecBracketExpr
        // Op_PreDecrID(ArrayIndexExpressionID(expression stem, expression index));
        // String result = indent() + "(" + "--" + stem + "[" + index + "]" + ")";
        result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, index), "-=", "1", true, false) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_preDecMemberExpr(IASNode iNode, String stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false);

        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); result.addInstruction(OP_dup);
         * result.addInstruction(OP_getproperty, field.getName());
         * result.addInstruction(OP_decrement); // Save a copy of the result to
         * use as the value of this expression. result.addInstruction(OP_dup);
         * Binding result_tmp = currentScope.allocateTemp();
         * result.addInstruction(result_tmp.setlocal() );
         * result.addInstruction(OP_setproperty, field.getName());
         * result.addInstruction(result_tmp.getlocal() );
         * result.addAll(currentScope.releaseTemp(result_tmp));
         */

        // prefix unary operator.
        // Pattern preDecMemberExpr
        // Op_PreDecrID(MemberAccessExpressionID(expression stem, name field));
        // String result = indent() + "(" + "--" + stem + "." + getBasenameFromBinding(field) + ")";
        result += "(" + resolveSetterName(iNode, stem, field, "-=", "1", false, false) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_preDecNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, false);

        String result = createInstruction(iNode);

        /*
         * if ( unary.isLocal() ) { result.addInstruction(unary.declocal());
         * result.addInstruction(unary.getlocal()); } else { Name n =
         * unary.getName(); result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_getproperty,n);
         * result.addInstruction(OP_decrement); result.addInstruction(OP_dup);
         * result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_swap);
         * result.addInstruction(OP_setproperty,n); }
         */

        // prefix unary operator.
        // Pattern preDecNameExpr
        // Op_PreDecrID(name unary);
        // String result = indent() + "(" + "--" + bindingToString(iNode,unary, true, true) + ")";

        final String methodName = bindingToString(iNode, unary, true, true);
        if (methodName.contains("."))
        {
            final String stem = methodName.substring(0, methodName.lastIndexOf("."));
            final String member = getBasenameFromBinding(unary);
            result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, member), "-=", "1", false, false) + ")";
        }
        else
        {
            result += indent() + "(" + "--" + methodName + ")";
        }

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_preIncBracketExpr(IASNode iNode, String stem, String index, boolean need_result)
    {
        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); Binding stem_tmp = currentScope.allocateTemp();
         * result.addInstruction(OP_dup);
         * result.addInstruction(stem_tmp.setlocal() ); result.addAll(index);
         * result.addInstruction(OP_dup); // Get the initial value. Binding
         * index_tmp = currentScope.allocateTemp();
         * result.addInstruction(index_tmp.setlocal() );
         * result.addInstruction(OP_getproperty); // Increment, and dup a copy
         * onto // the stack as the value of this expression.
         * result.addInstruction(OP_increment); result.addInstruction(OP_dup);
         * Binding result_tmp = currentScope.allocateTemp();
         * result.addInstruction(result_tmp.setlocal() );
         * result.addInstruction(stem_tmp.getlocal() );
         * result.addInstruction(index_tmp.getlocal());
         * result.addInstruction(result_tmp.getlocal());
         * result.addInstruction(OP_setproperty);
         * result.addAll(currentScope.releaseTemp(stem_tmp));
         * result.addAll(currentScope.releaseTemp(index_tmp));
         * result.addAll(currentScope.releaseTemp(result_tmp));
         */

        // prefix unary operator.
        // Pattern preIncBracketExpr
        // Op_PreIncrID(ArrayIndexExpressionID(expression stem, expression index));
        // String result = indent() + "(" + "++" + stem + "[" + index + "]" + ")";
        result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, index), "+=", "1", true, false) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_preIncMemberExpr(IASNode iNode, String stem, Binding field, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true);

        String result = createInstruction(iNode);

        /*
         * result.addAll(stem); result.addInstruction(OP_dup);
         * result.addInstruction(OP_getproperty, field.getName());
         * result.addInstruction(OP_increment); // Save a copy of the result to
         * use as the value of this expression. result.addInstruction(OP_dup);
         * Binding result_tmp = currentScope.allocateTemp();
         * result.addInstruction(result_tmp.setlocal());
         * result.addInstruction(OP_setproperty, field.getName());
         * result.addInstruction(result_tmp.getlocal());
         * result.addAll(currentScope.releaseTemp(result_tmp));
         */

        // prefix unary operator.
        // Pattern preIncMemberExpr
        // Op_PreIncrID(MemberAccessExpressionID(expression stem, name field));
        // String result = indent() + "(" + "++" + stem + "." + getBasenameFromBinding(field) + ")";
        result += "(" + resolveSetterName(iNode, stem, field, "+=", "1", false, false) + ")";

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_preIncNameExpr(IASNode iNode, Binding unary, boolean need_result)
    {
        currentScope.getMethodBodySemanticChecker().checkIncDec(iNode, true);

        String result = createInstruction(iNode);

        /*
         * if ( unary.isLocal() ) { result.addInstruction(unary.inclocal());
         * result.addInstruction(unary.getlocal()); } else { Name n =
         * unary.getName(); result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_getproperty,n);
         * result.addInstruction(OP_increment); result.addInstruction(OP_dup);
         * result.addInstruction(OP_findpropstrict,n);
         * result.addInstruction(OP_swap);
         * result.addInstruction(OP_setproperty,n); }
         */

        // prefix unary operator.
        // Pattern preIncNameExpr
        // Op_PreIncrID(name unary);
        // String result = indent() + "(" + "++" + bindingToString(iNode,unary, true, true) + ")";

        final String methodName = bindingToString(iNode, unary, true, true);
        if (methodName.contains("."))
        {
            final String stem = methodName.substring(0, methodName.lastIndexOf("."));
            final String member = getBasenameFromBinding(unary);
            result += "(" + resolveSetterName(iNode, stem, makeMemberBinding(iNode, stem, member), "+=", "1", false, false) + ")";
        }
        else
        {
            result += indent() + "(" + "++" + methodName + ")";
        }

        if (!need_result)
            result = transform_expression_to_void_expression(iNode, result);

        return result;
    }

    public String reduce_regexLiteral(IASNode iNode)
    {
        String result = createInstruction(iNode, 4);

        /*
         * result.addInstruction(OP_findpropstrict, regexType);
         * result.addInstruction(OP_getproperty, regexType);
         * result.addInstruction(OP_pushstring, getStringLiteralContent(iNode));
         * result.addInstruction(OP_construct, 1);
         */

        final IRegExpLiteralNode regExNode = (IRegExpLiteralNode)iNode;
        final String flags = regExNode.getFlagString();

        // object_constant = Pattern regexLiteral
        if (flags != null && !flags.isEmpty())
            result += "new RegExp(" + getStringLiteralContent(iNode) + ", \"" + flags + "\")";
        else
            result += "new RegExp(" + getStringLiteralContent(iNode) + ")";
        return result;
    }

    public String reduce_requiredParameter(IASNode iNode, Name param_name, Binding param_type)
    {
        /*
         * currentScope.makeParameter(getParameterContent(iNode),
         * param_type.getName());
         */
        // Pattern requiredParameter
        // ArgumentID(Binding param_name, Binding param_type);

        // currentScope.makeParameter(#ArgumentID#, param_type.getName());
        this.makeParameter(iNode, ((org.apache.flex.compiler.internal.tree.as.ParameterNode)iNode), currentScope, param_name, param_type, param_type);
        return null;
    }

    public String reduce_restParameter(IASNode iNode, Name param_name, Binding param_type)
    {
        /*
         * currentScope.makeParameter(getParameterContent(iNode),
         * param_type.getName());
         */
        // parameter = Pattern restParameter
        // ArgumentID(Binding param_name, Binding param_type);

        // currentScope.makeParameter((((org.apache.flex.compiler.as.tree.IParameterNode)iNode).getDefinition()), param_type.getName());
        this.makeParameter(iNode, ((ParameterNode)iNode), currentScope, param_name, param_type, param_type);
        return null;
    }

    public String reduce_returnValue(IASNode iNode, String value)
    {
        // workaround for falcon bug
        // Date to class triggers
        // Implicit coercion of a value of type Date to an unrelated type Class.
        // currentScope.getMethodBodySemanticChecker().checkReturnValue(iNode);

        String result = createInstruction(iNode, value.length() + 1);

        /*
         * result.addAll(value); result.addInstruction(OP_returnvalue); try {
         * return getNonLocalControlFlow(result,
         * ControlFlowContextManager.FIND_ALL_CONTEXTS); } catch (
         * UnknownControlFlowTargetException cant_return ) {
         * currentScope.getProblems().add(new UnexpectedReturnProblem(iNode));
         * return createInstruction(iNode); }
         */

        // Pattern returnValue
        // ReturnStatementID(void_expression no_value);

        String fname = "";
        if (currentScope != null &&
            currentScope.getMethodInfo() != null &&
            currentScope.getMethodInfo().getMethodName() != null)
        {
            fname = currentScope.getMethodInfo().getMethodName();
        }

        result += indent() + "return ";

        if ((value.startsWith("goog.base") && !value.startsWith("goog.base(this")) || (value.startsWith("adobe.base") && !value.startsWith("adobe.base(this")))
        {
            assert false : "FalconJS should never generate 'goog.base(' or 'adobe.base(' not followed by 'this'";

            /*
             * if( !fname.isEmpty() ) { final String getter =
             * resolveGetterName(iNode, JSSharedData.THIS,
             * makeMemberBinding(iNode, JSSharedData.THIS, fname), false); if(
             * getter.startsWith(JSSharedData.THIS + "." +
             * JSSharedData.GETTER_PREFIX)) fname = JSSharedData.GETTER_PREFIX +
             * fname; } result += "goog.base(this"; if( !fname.isEmpty() )
             * result += ", \"" + fname + "\""; if(!params.isEmpty()) { result
             * += ", " + params; } result += ");" + endl();
             */
        }
        else
        {
            // Symbol support
            if (fname.equals(JSSharedData.SYMBOL_GET_LINKAGE))
            {
                String _className = getCurrentClassName();
                final IDefinition def = getDefinitionForName(iNode, makeName(_className));
                if (def != null && def instanceof ClassDefinition)
                {
                    if (isSymbolClass(currentScope.getProject(), def))
                    {
                        // TODO: check whether value is derived from MovieClip.
                        _className = createFullNameFromDefinition(currentScope.getProject(), def);
                        this.m_sharedData.registerSymbol(value, _className);
                    }
                }
            }

            result += stripNS(value) + ";" + endl();
        }
        return result;
    }

    public String reduce_returnVoidSideEffect(IASNode iNode, String expr)
    {
        return String.format("%sreturn void(%s);%s", indent(), expr, endl());
    }

    public String reduce_returnVoid(IASNode iNode)
    {
        String result = createInstruction(iNode, 1);
        currentScope.getMethodBodySemanticChecker().checkReturnVoid(iNode);

        /*
         * result.addInstruction(OP_returnvoid); try { return
         * getNonLocalControlFlow(result,
         * ControlFlowContextManager.FIND_ALL_CONTEXTS); } catch (
         * UnknownControlFlowTargetException cant_return ) {
         * currentScope.getProblems().add(new UnexpectedReturnProblem(iNode));
         * return createInstruction(iNode); }
         */

        // Pattern returnVoid
        // ReturnStatementID(void);

        result += indent() + "return;" + endl();
        return result;
    }

    public String reduce_returnVoidValue(IASNode iNode, String no_value)
    {
        String result = createInstruction(iNode, no_value.length() + 1);
        currentScope.getMethodBodySemanticChecker().checkReturnVoid(iNode);

        /*
         * // The void expression might have side effects.
         * result.addAll(no_value); result.addInstruction(OP_returnvoid); try {
         * return getNonLocalControlFlow(result,
         * ControlFlowContextManager.FIND_ALL_CONTEXTS); } catch (
         * UnknownControlFlowTargetException cant_return ) {
         * currentScope.getProblems().add(new UnexpectedReturnProblem(iNode));
         * return createInstruction(iNode); }
         */

        // Pattern returnVoidValue
        // ReturnStatementID(void_expression no_value);

        if (!no_value.isEmpty())
            currentScope.addProblem(new JSInternalCompilerProblem(iNode, "value is not empty"));

        // just weird, this might be a bug.
        result += indent() + "return;";
        return result;
    }

    public String reduce_runtimeNameExpression(IASNode iNode, String expr)
    {
        return expr;
    }

    public Binding reduce_simpleName(IASNode iNode)
    {
        /*
         * return currentScope.resolveName((IdentifierNode)iNode);
         */

        try
        {
            Binding b;

            // decompiling builtin.abc
            if (((IdentifierNode)iNode).getName().equals("..."))
                b = makeBinding(iNode, "arguments");
            else
            {
                b = currentScope.resolveName((IdentifierNode)iNode);
            }
            return b;
        }

        catch (NullPointerException e)
        {
            throw e;
        }
    }

    public Name reduce_declName(IASNode iNode)
    {
        return reduce_simpleName(iNode).getName();
    }

    public String reduce_strictneqExpr(IASNode iNode, String l, String r)
    {
        /*
         * String result = binaryOp(iNode, l, r, OP_strictequals);
         * result.addInstruction(OP_not);
         */

        // Binary logical operators.
        // Pattern strictneqExpr
        // Op_StrictNotEqualID(expression l, expression r);
        String result = binaryOp(l, r, "!==");
        return result;
    }

    public Binding reduce_superAccess(IASNode iNode, Binding qualified_name)
    {
        /*
         * // TODO: in-flight SEW changes superseded by bugfix, uncomment when
         * this routine is available. //
         * currentScope.getMethodBodySemanticChecker().checkSuperAccess(iNode,
         * qualified_name); // The super qualifier is just concrete syntax.
         * return qualified_name;
         */

        // Pattern superAccess
        // ArgumentID(Binding qualified_name);

        final String baseName = getBasenameFromBinding(qualified_name);

        //  The super qualifier is just concrete syntax.
        // final String superName = JSSharedData._SUPER + "." + baseName;
        // final Name name = new Name( qualified_name.getName().getQualifiers(), superName );

        String superName;
        if (JSSharedData.m_useClosureLib)
        {
            // name of method containing the super.foo assignment
            String invokingMethodRawName = getCurrentMethodName();
            IFunctionDefinition enclosingDef = getCurrentMethod(iNode);
            String invokingMethodName = mungeMethodName(invokingMethodRawName, enclosingDef, false);

            // name of method being invoked via super, e.g. "set_foo" for "super.foo = bar"
            final String superMethodName;

            // Note: getDefinitionForName() isn't aware of the "super." prefix, so it can be unreliable; prefer def already in binding if exists
            IFunctionDefinition def = (IFunctionDefinition)qualified_name.getDefinition();
            if (def == null)
                def = (IFunctionDefinition)getDefinitionForName(iNode, qualified_name.getName());

            // Note: we use get_ prefix even if def is a setter since we won't know until later whether we're in an
            // lvalue; resolveSetterName() will fix this up later if needed.
            superMethodName = mungeMethodName(baseName, def, true);

            boolean needAdobeBase = !invokingMethodName.equals(superMethodName);

            // NOTE: in the ISetterDefinition case, this string is later munged by resolveSetterName() to add one last
            // arg, the RHS value; see its call to convertSuperAccessToSetter().
            if (!needAdobeBase)
                superName = "goog.base(this, \"" + superMethodName + "\")";
            else
            {
                IClassDefinition invokingMethodClass = (IClassDefinition)enclosingDef.getParent();

                superName = "adobe.base(this, " + getSuperMethodReference(invokingMethodClass, baseName) + ")";
            }
        }
        else
        {
            superName = JSSharedData.THIS + "." + JSSharedData._SUPER;
        }

        final Name name = new Name(qualified_name.getName().getQualifiers(), superName);
        return new Binding(iNode, name, null);
    }

    public String reduce_superStandalone(IASNode iNode)
    {
        return JSSharedData.THIS + "." + JSSharedData._SUPER;
    }

    public String reduce_superCallExpr(IASNode iNode, Vector<String> args)
    {
        /*
         * currentScope.getMethodBodySemanticChecker().checkExplicitSuperCall(iNode
         * , args); // TODO: Investigate optimizing this. String result =
         * createInstruction(iNode); // Special case first part: don't look for
         * the "super" name, // push "this" on the stack.
         * result.addInstruction(OP_getlocal0); for ( String arg: args)
         * result.addAll(arg); // Special case second part: call constructsuper.
         * result.addInstruction(OP_constructsuper, args.size() );
         */

    				// using call() instead of goog.base()
        return ""; // reduce_functionCallOfSuperclassMethod_to_expression(iNode, null, null, args) + ";" + endl();
    }

    public String reduce_switchStmt(IASNode iNode, String switch_expr, Vector<ConditionalFragment> cases)
    {
        //  TODO: Optimize String size.
        String result = createInstruction(iNode);

        /*
         * Label default_case_label = null; Label switch_tail = null; // Get the
         * switch value and save it in a temp. Binding switch_temp =
         * currentScope.allocateTemp(); result.addAll(switch_expr);
         * result.addInstruction(switch_temp.setlocal()); // First, jump to the
         * switch table. if ( cases.size() > 0 ) {
         * result.addInstruction(OP_jump, cases.elementAt(0).getLabel()); } for
         * ( ConditionalFragment current_case: cases ) { // Ensure the case
         * begins with a OP_label String // so we can branch back to it. if (
         * current_case.statement.isEmpty() ||
         * !(current_case.statement.firstElement().getOpcode() == OP_label ) ) {
         * String labeled_list = createInstruction(iNode);
         * labeled_list.addInstruction(OP_label);
         * labeled_list.addAll(current_case.statement); current_case.statement =
         * labeled_list; } if ( current_case.isUnconditionalAlternative() ) { //
         * Note: Verified that the parser rejects duplicate default
         * alternatives. assert ( null == default_case_label);
         * default_case_label = current_case.statement.getLabel(); } // else
         * TODO: Check for duplicate cases. current_case.getStatementLabel(); //
         * Touch the label so it will be properly recorded.
         * result.addAll(current_case.statement); } // Jump to the tail.
         * result.addInstruction(switch_temp.kill()); switch_tail = new Label();
         * result.addInstruction(OP_jump, switch_tail); // Emit the
         * "switch table" as if/else statements. for ( ConditionalFragment
         * current_case: cases ) { if (
         * !current_case.isUnconditionalAlternative() ) {
         * result.addAll(current_case.condition);
         * result.addInstruction(switch_temp.getlocal());
         * result.addInstruction(OP_ifstricteq, current_case.getStatementLabel()
         * ); } } // Emit the jump to the default case. if ( default_case_label
         * != null ) { result.addInstruction(switch_temp.kill());
         * result.addInstruction(OP_jump, default_case_label); } // Note the
         * temp has already been killed // since we don't want it reused in a
         * control // flow region that might confuse the Verifier.
         * result.addAll(currentScope.releaseTemp(switch_temp)); // No continue
         * here, and thus there is no need // to check for previous labeled flow
         * contexts.
         * currentScope.getFlowManager().finishControlFlowContext(result); if (
         * switch_tail != null ) result.labelNext(switch_tail);
         */

        // statement = Pattern switchStmt

        result += indent() + "switch(" + switch_expr + ")" + endl();
        result += "{" + endl();

        String casesBlock = "";

        //  Emit the switch cases.
        // Vector<ConditionalFragment> cases = (Vector<ConditionalFragment>) raw_cases;
        for (ConditionalFragment current_case : cases)
        {
            if (current_case.isUnconditionalAlternative())
                casesBlock += "default";
            else
            {
                casesBlock += "case ";
                casesBlock += current_case.condition;
            }
            casesBlock += ": " + endl();
            if (current_case.statement.length() > 0)
            {
                casesBlock += "{" + endl();
                casesBlock += indentBlock(current_case.statement, 1); // Touch the label so it will be properly recorded.
                casesBlock += "}" + endl();
            }
        }

        result += indentBlock(casesBlock, 1);
        result += "}" + endl();
        return result;
    }

    public String reduce_ternaryExpr(IASNode iNode, String test, String when_true, String when_false)
    {
        String result = createInstruction(iNode, test.length() + when_true.length() + when_false.length() + 2);

        /*
         * Label tail = new Label(); result.addAll(test);
         * result.addInstruction(OP_iffalse, when_false.getLabel());
         * result.addAll(when_true); result.addInstruction(OP_jump, tail);
         * result.addAll(when_false); result.labelNext(tail);
         */

        // Pattern ternaryExpr
        // TernaryExpressionID(expression test, expression when_true, expression when_false);

        result += "(" + test + " ? " + stripTabs(stripNS(when_true)) + " : " + stripTabs(stripNS(when_false)) + ")";

        return result;
    }

    public String reduce_throwStmt(IASNode iNode, String tossable)
    {
        String result = createInstruction(iNode, tossable.length() + 1);

        /*
         * result.addAll(tossable); result.addInstruction(OP_throw);
         */

        // statement = Pattern throwStmt
        result += indent() + "throw " + tossable + ";" + endl();

        return result;
    }

    public String reduce_tryCatchFinallyStmt(IASNode iNode, String try_stmt, String finally_stmt, Vector<CatchPrototype> catch_blocks)
    {
        String result = generateTryCatchFinally(iNode, try_stmt, catch_blocks, finally_stmt);
        // workaround for Falcon bug.
        // java.lang.ClassCastException: org.apache.flex.compiler.internal.as.codegen.ControlFlowContext cannot be cast to org.apache.flex.compiler.internal.as.codegen.ExceptionHandlingContext
        try
        {
            currentScope.getFlowManager().finishExceptionContext();
        }
        catch (ClassCastException e)
        {

        }

        return result;
    }

    public String reduce_tryCatchStmt(IASNode iNode, String try_stmt, Vector<CatchPrototype> catch_blocks)
    {
        /*
         * // TODO: Optimize. String result = createInstruction(iNode); if (
         * try_stmt.isEmpty() ) { // TODO: The catch clause gen may have // side
         * effects, so we can't skip this. try_stmt = createInstruction(iNode);
         * try_stmt.addInstruction(OP_nop); } Label catch_tail = new Label();
         * result.addAll(try_stmt); result.addInstruction(OP_jump, catch_tail);
         * // Get labels for the start and end of the try block. Label try_start
         * = result.getLabel(); Label try_end = result.getLastLabel(); for (
         * CatchPrototype catch_proto: catch_blocks ) { boolean is_last_catch =
         * catch_proto.equals(catch_blocks.lastElement()); String catch_body =
         * generateCatchBlock(try_start, try_end, catch_proto); if(
         * !is_last_catch && catch_body.canFallThrough() )
         * catch_body.addInstruction(OP_jump, catch_tail);
         * result.addAll(catch_body); }
         * currentScope.getFlowManager().finishExceptionContext();
         * result.labelNext(catch_tail);
         */

        String result = reduce_tryCatchFinallyStmt(iNode, try_stmt, null, catch_blocks);
        return result;
    }

    public String reduce_tryFinallyStmt(IASNode iNode, String try_stmt, String finally_stmt)
    {
        /*
         * String result = generateTryCatchFinally(try_stmt, null,
         * finally_stmt);
         * currentScope.getFlowManager().finishExceptionContext();
         */

        // statement = Pattern tryFinallyStmt
        String result = reduce_tryCatchFinallyStmt(iNode, try_stmt, finally_stmt, null);
        return result;
    }

    public String reduce_typedFunction_to_statement(IASNode iNode, String plist, Binding return_type, String block)
    {
        /*
         * Binding nestedFunctionName =
         * currentScope.resolveName((IdentifierNode)
         * JSGeneratingReducer.getNthChild(iNode, 0));
         * this.generateNestedFunction(nestedFunctionName,
         * return_type.getName(), block); return createInstruction(iNode);
         */

        // statement = Pattern typedFunction : 0
        // ArgumentID( Binding n, String plist, Binding return_type , String block );

        Binding nestedFunctionName = currentScope.resolveName((IdentifierNode)SemanticUtils.getNthChild(iNode, 0));
        return this.generateNestedFunction(iNode, nestedFunctionName, return_type.getName(), block);
    }

    public String reduce_typedVariableDecl(IASNode iNode, Name var_name, Binding var_type, Vector<String> chained_decls)
    {
        BaseVariableNode var_node = (BaseVariableNode)iNode;
        Binding var = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
        currentScope.makeVariable(var, var_type.getName(), ((BaseDefinitionNode)iNode).getMetaInfos());

        /*
         * String result = createInstruction(iNode); if ( var_name.isLocal() ) {
         * // Add initializer code to the init Strings. String init_insns =
         * currentScope.getHoistedInitInstructions(); ASDefinitionCache cache =
         * currentScope.getDefinitionCache(); IDefinition type_def =
         * var_type.getDefinition(); if ( type_def ==
         * cache.getBuiltinType(BuiltinType.INT) || type_def ==
         * cache.getBuiltinType(BuiltinType.UINT) ) {
         * init_insns.addInstruction(OP_pushbyte, 0);
         * init_insns.addInstruction(var_name.setlocal()); } else if ( type_def
         * == cache.getBuiltinType(BuiltinType.BOOLEAN) ) {
         * init_insns.addInstruction(OP_pushfalse);
         * init_insns.addInstruction(var_name.setlocal()); } else if ( type_def
         * == cache.getBuiltinType(BuiltinType.NUMBER) ) {
         * init_insns.addInstruction(OP_pushdouble, Double.NaN);
         * init_insns.addInstruction(var_name.setlocal()); } else if ( type_def
         * == cache.getBuiltinType(BuiltinType.ANY_TYPE) ) {
         * init_insns.addInstruction(OP_pushundefined);
         * init_insns.addInstruction(var_name.setlocal()); } else if (
         * type_def.resolveType
         * (cache).isInstanceOf(cache.getBuiltinType(BuiltinType
         * .OBJECT).resolveType(cache), cache) ) {
         * init_insns.addInstruction(OP_pushnull);
         * init_insns.addInstruction(var_name.setlocal()); } else {
         * init_insns.addInstruction(OP_pushundefined);
         * init_insns.addInstruction(var_name.setlocal()); } } for ( String
         * decl: chained_decls ) result.addAll(decl); return result;
         */

        // In AS, uninitialized variables have a type-specific default value (not always undefined)
        String var_initializer = null;
        String defaultInitializer = JSEmitter.getDefaultInitializerForVariable(var_type.getName());
        if (!defaultInitializer.equals("undefined")) // don't emit anything if JS would default to the right value anyway
        {
            // Don't emit anything for member vars - JSEmitter has already put the right initializer on the prototype
            IDefinition varDef = var_node.getDefinition();
            if (varDef == null || isLocalVariable((IVariableDefinition)varDef))
                var_initializer = defaultInitializer;
        }

        return reduce_variableDecl(iNode, var_name, var_type, var_initializer, chained_decls);
    }

    public String reduce_typedVariableDeclWithInitializer(IASNode iNode, Name var_name, Binding var_type, String var_initializer, Vector<String> chained_decls)
    {
        BaseVariableNode var_node = (BaseVariableNode)iNode;
        Binding var = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
        currentScope.makeVariable(var, var_type.getName(), ((BaseDefinitionNode)iNode).getMetaInfos());

        /*
         * String result = generateAssignment(iNode, var_name, var_initializer);
         * for ( String decl: chained_decls ) result.addAll(decl); return
         * result;
         */

        return reduce_variableDecl(iNode, var_name, var_type, var_initializer, chained_decls);
    }

    public String reduce_typedVariableDeclWithConstantInitializer(IASNode iNode, Name var_name, Binding var_type, Object var_initializer, Vector<String> chained_decls)
    {
        return reduce_typedVariableDeclWithInitializer(iNode, var_name, var_type, var_initializer.toString(), chained_decls);
    }

    public String reduce_typedBindableVariableDecl(IASNode iNode, Name var_name, Binding var_type, Vector<String> chained_decls)
    {
        BaseVariableNode vn = (BaseVariableNode)iNode;
        currentScope.getMethodBodySemanticChecker().checkBindableVariableDeclaration(iNode, vn.getDefinition());
        String result = createInstruction(iNode);
        Binding var = currentScope.resolveName((IdentifierNode)vn.getNameExpressionNode());
        currentScope.makeBindableVariable(var, var_type.getName(), vn.getMetaInfos());
        for (String decl : chained_decls)
            result += decl;
        return result;
    }

    public String reduce_typedBindableVariableDeclWithInitializer(IASNode iNode, Name var_name, Binding var_type, String var_initializer, Vector<String> chained_decls)
    {
        // constant propagation for initialized members
        m_varInitValue = var_initializer;

        BaseVariableNode vn = (BaseVariableNode)iNode;

        currentScope.getMethodBodySemanticChecker().checkBindableVariableDeclaration(iNode, vn.getDefinition());
        Binding var = currentScope.resolveName((IdentifierNode)vn.getNameExpressionNode());
        currentScope.makeBindableVariable(var, var_type.getName(), vn.getMetaInfos());
        String result = generateAssignment(iNode, new Binding(iNode, BindableHelper.getBackingPropertyName(var_name), vn.getDefinition()), var_initializer);
        for (String decl : chained_decls)
            result += decl;
        return result;
    }

    public Binding reduce_typedVariableExpression(IASNode iNode, Name var_name, Binding var_type)
    {
        /*
         * currentScope.makeVariable(var_name, var_type.getName(), null); return
         * var_name;
         */

        // name = Pattern typedVariableExpression
        VariableExpressionNode var_expr_node = (VariableExpressionNode)iNode;
        BaseVariableNode var_node = var_expr_node.getTargetVariable();
        String varType = bindingToString(iNode, var_type, false, true);
        usedTypes.add(removeRootName(varType));
        this.makeVariable(var_node, currentScope, var_name, var_type, null);
        return currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
    }

    public String reduce_typelessFunction(IASNode iNode, String plist, String block)
    {
        /*
         * Binding nestedFunctionName =
         * currentScope.resolveName((IdentifierNode)
         * JSGeneratingReducer.getNthChild(iNode, 0));
         * this.generateNestedFunction(nestedFunctionName, null, block); return
         * createInstruction(iNode);
         */

        // statement = Pattern typelessFunction : 0
        // ArgumentID( Binding n, String plist, String block );

        Binding nestedFunctionName = currentScope.resolveName((IdentifierNode)SemanticUtils.getNthChild(iNode, 0));
        return this.generateNestedFunction(iNode, nestedFunctionName, null, block);
    }

    private String getTypeOfString(ITypeDefinition typezo)
    {
        ICompilerProject project = currentScope.getProject();

        String result = null;
        if (typezo != null)
        {
            if (typezo == project.getBuiltinType(BuiltinType.STRING))
            {
                result = "\"string\"";
            }
            else if (typezo == project.getBuiltinType(BuiltinType.NUMBER) ||
                     typezo == project.getBuiltinType(BuiltinType.INT) ||
                     typezo == project.getBuiltinType(BuiltinType.UINT))
            {
                result = "\"number\"";
            }
            else if (typezo == project.getBuiltinType(BuiltinType.BOOLEAN))
            {
                result = "\"boolean\"";
            }
        }
        return result;
    }

    public String reduce_typeof_name(IASNode iNode, Binding binding)
    {
        String result = null;

        final IDefinition def = binding.getDefinition();
        if (def != null)
        {
            final ITypeDefinition typezo = def.resolveType(currentScope.getProject());
            result = getTypeOfString(typezo);
        }

        if (result == null)
        {
            /*
             * result = generateAccess(binding);
             * result.addInstruction(OP_typeof);
             */

            result = "typeof(" + bindingToString(iNode, binding, true, true) + ")";
        }

        return result;
    }

    public String reduce_typeof_expr(IASNode iNode, String expr)
    {
        return String.format("typeof(%s)", expr);
    }

    public String reduce_variableExpression(IASNode iNode, Vector<String> decls)
    {
        String result = createInstruction(iNode);

        /*
         * for ( String var_decl: decls ) result.addAll(var_decl);
         */

        // void_expression = Pattern variableExpression
        // ArgumentID(Vector<String> decls)
        for (String var_decl : decls)
            result += var_decl;
        return result;
    }

    public String reduce_useNamespaceDirective(IASNode iNode, Binding ns_name)
    {
        currentScope.getMethodBodySemanticChecker().checkUseNamespaceDirective(iNode, ns_name);
        String result = createInstruction(iNode);

        final JSEmitter emitter = getEmitter();
        final IDefinition def = ns_name.getDefinition();
        if (def != null)
            emitter.visitUseNamespace(def.getQualifiedName());
        else
            emitter.visitUseNamespace(getBasenameFromBinding(ns_name));

        return result;
    }

    public String reduce_typeNameParameterAsType(IASNode iNode, Binding type_param)
    {
        return type_param.getName().getBaseName();
    }

    public String reduce_vectorLiteral(IASNode iNode, String type_param, Vector<String> elements)
    {
        currentScope.getMethodBodySemanticChecker().checkVectorLiteral(iNode, null);
        String result = createInstruction(iNode);

        result += "new Array()";

        return result;
    }

    public String reduce_vectorLiteralAsExpression(IASNode iNode, String type, Vector<String> elements)
    {
        return "new Array()";
    }

    public String reduce_voidExpr_to_expression(IASNode iNode)
    {
        String il = createInstructionList(iNode);
        // il.addInstruction(OP_pushundefined);
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
        /*
         * return UNDEFINED_VALUE;
         */

        // object_literal = Pattern void0Literal
        // ArgumentID()
        return "undefined";
    }

    public String reduce_void0Literal_to_object_literal(IASNode iNode)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushundefined);
         */

        // object_literal = Pattern void0Literal
        // ArgumentID()
        result += "undefined";
        return result;
    }

    public String reduce_instructionListExpression(IASNode iNode)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "instruction list expression"));
        // return ((InstructionListNode)iNode).getInstructions();
        return "";
    }

    public String reduce_void0Operator(IASNode iNode)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushundefined);
         */

        // object_literal = Pattern voidOperator
        // ArgumentID()
        result += "undefined";

        return result;
    }

    public Object reduce_voidOperator_to_constant_value(IASNode iNode, Object expr)
    {
        /*
         * return UNDEFINED_VALUE;
         */

        // constant_value =  Pattern voidOperator
        // ArgumentID()
        String result = createInstruction(iNode, 1);

        result += "undefined";
        return result;
    }

    public String reduce_voidOperator_to_expression(IASNode iNode, String expr)
    {
        // String result = createInstruction(iNode, 1);

        /*
         * result.addAll(expr); result.addInstruction(OP_pop);
         * result.addInstruction(OP_pushundefined); return result;
         */

        // object_literal = Pattern voidOperator
        // String expr

        // be careful, typeof(null) returns "object".
        // We have to return undefined here.
        return "undefined";
    }

    public String reduce_whileStmt(IASNode iNode, String cond, String body)
    {
        String result = createInstruction(iNode, cond.length() + body.length() + 5);

        /*
         * currentScope.getFlowManager().resolveContinueLabel(cond); if(
         * !isAnonymousFunction && !convertLocalVarsToMembers )
         * currentScope.makeVariable(new Binding(func_name,null)); // Jump to
         * the test. result.addInstruction(OP_jump, cond.getLabel()); // Create
         * an emitter-time label, and attach // it to an OP_label String for the
         * // backwards branch. result.addInstruction(OP_label); Label loop =
         * result.getLastLabel(); result.addAll(body); result.addAll(cond);
         * result.addInstruction(OP_iftrue, loop); if ( !
         * isLabeledSubstatement(iNode) )
         * currentScope.getFlowManager().finishControlFlowContext(result);
         */

        // Pattern whileStmt
        // WhileLoopID(expression cond, statement body);    
        result += indent() + "while(" + cond + ")" + endl();
        result += indent() + "{" + endl();
        // pushIndent();
        result += indentBlock(body, 1);
        // popIndent();
        result += indent() + "}" + endl();
        return result;
    }

    public String reduce_withStmt(IASNode iNode, String new_scope, String body)
    {
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "'with' statements"));
        return "";

        /*
         * // statement = Pattern withStmt String result = indent() + "with(" +
         * new_scope + ")" + endl(); result += indent() + "{" + endl(); result
         * += indentBlock( body, 1 ); result += indent() + "}" + endl();
         */

        /*
         * String result = createInstruction(iNode);
         * currentScope.getMethodBodySemanticChecker
         * ().popWithScope(currentScope); result.addAll(new_scope); if (
         * currentScope.getFlowManager().hasWithStorage()) {
         * result.addInstruction(OP_dup);
         * result.addInstruction(currentScope.getFlowManager
         * ().getWithStorage().setlocal()); }
         * result.addInstruction(OP_pushwith); result.addAll(body);
         * result.addInstruction(OP_popscope);
         * currentScope.getFlowManager().finishWithContext(result); return
         * result;
         */
    }

    /**
     * The enumeration of states that the code generator finds interesting in an
     * XML literal. There are states here than the reducer, below, doesn't
     * consider especially interesting; see computeXMLContentStateMatrix() for
     * their usage.
     */
    // private enum XMLContentState { TagStart, TagLiteral, TagName, Attr, Value, TagEnd, ContentLiteral, ContentExpression };

    public String reduce_XMLContent(IASNode iNode, Vector<String> exprs)
    {
        String result = createInstruction(iNode);

        /*
         * result.addInstruction(OP_findpropstrict, xmlType);
         * result.addInstruction(OP_getproperty, xmlType); // Add all the
         * arguments up into a single String. XMLContentState[] content_state =
         * computeXMLContentStateMatrix(exprs);
         * result.addAll(exprs.elementAt(0)); for ( int i = 1; i < exprs.size();
         * i++ ) { result.addAll(exprs.elementAt(i)); if ( content_state[i] ==
         * XMLContentState.TagName ) { // Parser elides whitespace after tag
         * name expressions. result.addInstruction(OP_pushstring, " ");
         * result.addInstruction(OP_add); } else if ( content_state[i] ==
         * XMLContentState.Attr ) { // Parser elides whitespace before attribute
         * name expressions. if ( content_state[i-1] != XMLContentState.TagName
         * ) { result.addInstruction(OP_pushstring, " ");
         * result.addInstruction(OP_swap); result.addInstruction(OP_add); } }
         * else if ( content_state[i] == XMLContentState.Value ) { if (
         * content_state[i-1] == XMLContentState.Attr ) { // Parser elided this
         * token. result.addInstruction(OP_pushstring, "=");
         * result.addInstruction(OP_swap); result.addInstruction(OP_add); }
         * result.addInstruction(OP_esc_xattr); // The attribute will need quote
         * marks for the AVM's XML parser. result.addInstruction(OP_pushstring,
         * "\""); result.addInstruction(OP_swap); result.addInstruction(OP_add);
         * result.addInstruction(OP_pushstring, "\"");
         * result.addInstruction(OP_add); } else if ( content_state[i] ==
         * XMLContentState.ContentExpression )
         * result.addInstruction(OP_esc_xelem); result.addInstruction(OP_add); }
         * result.addInstruction(OP_construct, 1); return result;
         */

        // expression = Pattern XMLContent: 
        // String literal

        // TODO: check implementation
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "E4X (ECMA-357)"));
        return result;
    }

    public String reduce_XMLList(IASNode iNode, Vector<String> exprs)
    {
        String result = createInstruction(iNode);
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "E4X (ECMA-357)"));
        return result;
    }

    public String reduce_XMLListConst(IASNode iNode, Vector<String> elements)
    {
        String result = createInstruction(iNode);
        currentScope.addProblem(new JSUnsupportedFeatureProblem(iNode, "E4X (ECMA-357)"));
        return result;
    }

    /**
     * Clone an String.
     * 
     * @return the clone, appropriately cast.
     * @note clone() always returns Object.
     */
    String replicate(String prototype)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(prototype);

        return sb.toString();
    }

    /**
     * Set this reducer's list of a priori Strings; typically these are field
     * initializers passed in to a constructor routine.
     */
    public void setInstanceInitializers(String insns)
    {
        // this.instanceInitializers = insns; 
    }

    public void setInstanceInitializers(InstructionList insns)
    {
        // this.instanceInitializers = instructionListToString(insns, false); 
    }

    /**
     * Set this reducer's initial LexicalScope.
     */
    public void setCurrentscope(LexicalScope scope)
    {
        this.currentScope = scope;
    }

    /*
     * ******************************
     * ** Transformation routines ** ******************************
     */

    public String transform_boolean_constant(IASNode iNode, Boolean boolean_constant)
    {
        String result = createInstruction(iNode, 1);

        // Literals.
        // Pattern booleanLiteral
        // LiteralBooleanID(void);

        if (Boolean.TRUE.equals(boolean_constant))
            result += "true";
        else
            result += "false";

        return result;
    }

    public String transform_double_constant(IASNode iNode, Double double_constant)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushdouble, double_constant);
         */

        // expression = double_constant
        result += double_constant.toString();
        if (result.length() > 2 && result.charAt(result.length() - 1) == '0' && result.charAt(result.length() - 2) == '.')
            result = result.replace(".0", "");
        return result;
    }

    public String transform_numeric_constant(IASNode iNode, Number numeric_constant)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushdouble, double_constant);
         */
        if (numeric_constant instanceof Integer)
            return transform_integer_constant(iNode, (Integer)numeric_constant);
        if (numeric_constant instanceof Double)
            return transform_double_constant(iNode, (Double)numeric_constant);
        return result;
    }

    public String transform_constant_value(IASNode iNode, Object constant_value)
    {
        if (constant_value instanceof Boolean)
        {
            return transform_boolean_constant(iNode, (Boolean)constant_value);
        }
        if (constant_value instanceof Number)
        {
            return transform_numeric_constant(iNode, (Number)constant_value);
        }
        if (constant_value instanceof String)
        {
            return transform_string_constant(iNode, (String)constant_value);
        }

        String result = null;
        if (constant_value instanceof Namespace)
        {
            // Doesn't look like FlaconJS has the equivalent of push namespace...
            //result.addInstruction(OP_pushnamespace, (Namespace)constant_value);
        }
        else if (constant_value == ABCConstants.NULL_VALUE)
        {
            result = "null";
        }
        else if (constant_value == ABCConstants.UNDEFINED_VALUE)
        {
            result = "undefined";
        }
        else
        {
            assert false : "unknown constant type";
        }

        return result;
    }

    /**
     * transform a string_constant to a constant_value - essentially a no-op,
     * but need a reduction so we can assign it a cost
     */
    public Object transform_string_constant_to_constant(IASNode iNode, String string_constant)
    {
        return string_constant;
    }

    /**
     * transform a boolean_constant to a constant_value - essentially a no-op,
     * but need a reduction so we can assign it a cost
     */
    public Object transform_boolean_constant_to_constant(IASNode iNode, Boolean boolean_constant)
    {
        return boolean_constant;
    }

    /**
     * transform a numeric_constant to a constant_value - essentially a no-op,
     * but need a reduction so we can assign it a cost
     */
    public Object transform_numeric_constant_to_constant(IASNode iNode, Number numeric_constant)
    {
        return numeric_constant;
    }

    public String transform_expression_to_conditionalJump(IASNode iNode, String expression)
    {
        String result = createInstruction(iNode, expression.length() + 1);

        /*
         * result.addAll(expression); result.addInstruction(OP_iftrue);
         */

        // conditionalJump = expression
        // String result = indent() +"if" + expression;
        result += expression;
        return result;
    }

    public Object transform_expression_to_constant_value(IASNode iNode, String expression)
    {
        //  return null - something higher up will report any appropriate diagnostics.
        return null;
    }

    public String transform_expression_to_void_expression(IASNode iNode, String expression)
    {
        String result = createInstruction(iNode, expression.length() + 1);

        /*
         * result.addAll(expression); result.addInstruction(OP_pop);
         */

        // void_expression = expression
        result += expression.toString() + ";" + endl();
        return result;
    }

    public String transform_integer_constant(IASNode iNode, Integer integer_constant)
    {
        // long value = integer_constant.longValue();

        String result = createInstruction(iNode, 1);

        /*
         * // Choose an String. // Could also be done at label name, but there's
         * // not a lot of value in doing so. result.pushNumericConstant(value);
         */

        // expression = integer_constant
        result += integer_constant.longValue();

        // this confuses addDynamicCastToValue()
        // if( integer_constant >= 16 )
        //    result += " /* 0x" + Long.toHexString(integer_constant).toUpperCase() + " */";

        return result;
    }

    public Object transform_name_to_constant_value(IASNode iNode)
    {
        return SemanticUtils.transformNameToConstantValue(iNode, currentScope.getProject());
    }

    public String transform_name_to_expression(IASNode iNode, Binding name)
    {
        /*
         * return generateAccess(name);
         */

        // expression = name
        return bindingToString(iNode, name, true, true);
    }

    public String transform_non_resolving_identifier(IASNode iNode, String non_resolving_identifier)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushstring, non_resolving_identifier);
         */

        // Pattern nonResolvingIdentifier, expression
        // NonResolvingIdentifierID(void);

        result += non_resolving_identifier;

        return result;
    }

    public String transform_runtime_name_expression(IASNode iNode, RuntimeMultiname runtime_name_expression)
    {
        return runtime_name_expression.generateRvalue(iNode);
    }

    public String transform_string_constant(IASNode iNode, String string_constant)
    {
        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushstring, string_constant);
         */

        // expression = string_constant
        result += string_constant;

        return result;
    }

    public String transform_uint_constant(IASNode iNode, Long uint_constant)
    {
        long value = uint_constant.longValue();

        String result = createInstruction(iNode, 1);

        /*
         * result.addInstruction(OP_pushuint, value);
         */

        // expression = uint_constant
        // ArgumentID(Long uint_constant)
        result += value;
        if (uint_constant >= 16)
            result += " /* 0x" + Long.toHexString(uint_constant).toUpperCase() + " */";
        return result;
    }

    /**
     * Generate a unary operator.
     * 
     * @param operand - the operand expression.
     * @param opcode - the operator's opcode.
     * @return an String that applies the operator to the operand.
     */
    String unaryOp(IASNode iNode, String operand, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkUnaryOperator(iNode, opcode);
        String result = createInstruction(iNode, operand.length() + 1);

        /*
         * result.addAll(operand); result.addInstruction(opcode);
         */

        switch (opcode)
        {
            case OP_negate:
                result += reduce_unaryMinusExpr(iNode, operand);
                break;
            case OP_convert_d:
                result += reduce_unaryPlusExpr(iNode, operand);
                break;
            case OP_typeof:
                result += reduce_typeofExpr(iNode, operand);
                break;
            case OP_bitnot:
                result += reduce_bitNotExpr(iNode, operand);
                break;
            case OP_unplus:
                result += reduce_unaryPlusExpr(iNode, operand);
                break;
            default:
                currentScope.addProblem(new JSInternalCompilerProblem(iNode, "Unknown unary op '" + opcode + "'"));
        }

        return result;
    }

    void checkUnaryOp(IASNode iNode, int opcode)
    {
        currentScope.getMethodBodySemanticChecker().checkUnaryOperator(iNode, opcode);
    }

    /**
     * Walk a syntax tree and note any construct that will cause the ABC
     * function to need an activation record.
     */
    void scanFunctionBodyForActivations(IASNode n)
    {
        if (n != null)
        {
            switch (n.getNodeID())
            {
                case WithID:
                case CatchID:
                case AnonymousFunctionID:
                case FinallyID:
                case FunctionID:
                {
                    currentScope.setNeedsActivation();
                    break;
                }
                default:
                {
                    for (int i = 0; i < n.getChildCount(); i++)
                        scanFunctionBodyForActivations(n.getChild(i));
                }
            }
        }
    }

    /**
     * Is this expression a string literal?
     * 
     * @return true if the expression is a string literal, i.e., it has one
     * pushstring String.
     */
    /*
     * private static boolean isStringLiteral(String insns) { return
     * insns.lastElement().getOpcode() == OP_pushstring; }
     */

    /**
     * Extract the string literal from an String.
     * 
     * @param insns - the String of interest.
     * @return the string literal from insns' final (and presumably only
     * executable) String.
     */
    /*
     * private static String getStringLiteral(String insns) { assert
     * isStringLiteral(insns); return
     * insns.lastElement().getOperand(0).toString(); }
     */

    /**
     * Work around the linear presentation of XML initializer elements from the
     * parser by doing ad-hoc pattern matching.
     * 
     * @param exprs - a Vector of expressions.
     * @return A state transition matrix to generate code for the elements.
     * @see ECMA-357 11.1.4 "XML Initialiser" Expressions may be used to compute
     * parts of an XML initialiser. Expressions are delimited by curly braces
     * and may appear inside tags or element content. Inside a tag, expressions
     * may be used to compute a tag name, attribute name, or attribute value.
     * Inside an element, expressions may be used to compute element content.
     */
    /*
     * private XMLContentState[] computeXMLContentStateMatrix( Vector<String>
     * exprs) { XMLContentState[] result = new XMLContentState[exprs.size()]; //
     * TagStart, TagLiteral, TagName, Attr, Value, TagEnd, ContentLiteral,
     * ContentExpression // First element must be a string or the parser
     * wouldn't have put us here. // But it may be a simple < or </ literal or a
     * <foo literal; the former // imply that the next expression is going to be
     * a TagName, the latter just // is a TagLiteral that puts us into the
     * middle of things. assert isStringLiteral(exprs.elementAt(0)) :
     * exprs.elementAt(0); String first_element =
     * getStringLiteral(exprs.elementAt(0)); if ( first_element.equals("<") ||
     * first_element.equals("</") ) result[0] = XMLContentState.TagStart; else
     * result[0] = XMLContentState.TagLiteral; for ( int i = 1; i <
     * exprs.size(); i++ ) { String insns = exprs.elementAt(i); if (
     * isStringLiteral(insns) ) { switch ( result[i-1] ) { case TagStart: case
     * TagLiteral: case TagName: case Attr: case Value: { if (
     * getStringLiteral(insns).matches(".*>") ) result[i] =
     * XMLContentState.TagEnd; else result[i] = XMLContentState.TagLiteral;
     * break; } case TagEnd: case ContentLiteral: case ContentExpression: if (
     * getStringLiteral(insns).matches("<.*") ) result[i] =
     * XMLContentState.TagStart; else result[i] =
     * XMLContentState.ContentLiteral; } } else { // Now the fun starts. // The
     * most important thing to do here is to get the // Value, and
     * ContentExpression states right; // these are the entities that require
     * special Strings. switch ( result[i-1] ) { case TagStart: result[i] =
     * XMLContentState.TagName; break; // An expression following a tag name or
     * // an attribute value should be an attribute name. case TagName: case
     * Value: result[i] = XMLContentState.Attr; break; // An expression
     * following an attribute name // should be an attribute value. case Attr:
     * result[i] = XMLContentState.Value; break; case TagEnd: case
     * ContentLiteral: case ContentExpression: result[i] =
     * XMLContentState.ContentExpression; break; case TagLiteral: { String
     * literal = getStringLiteral(exprs.elementAt(i-1)); if (
     * literal.endsWith("=") ) result[i] = XMLContentState.Value; else result[i]
     * = XMLContentState.Attr; break; } default: assert false: "Unhandled case "
     * + result[i-1]; } } } return result; }
     */

    // =======================================================================================   

    public String reduce_untypedParameterInitializer(IASNode iNode, Binding param_name, String param_initializer)
    {
        // Pattern untypedParameterInitializer
        // ArgumentID(name param_name, expression param_initializer);

        this.makeParameter(iNode, ((org.apache.flex.compiler.internal.tree.as.ParameterNode)iNode), currentScope, param_name, null, null);
        return null;
    }

    public String reduce_typedParameterInitializer(IASNode iNode, Binding param_name, Binding param_type, String param_initializer)
    {
        // Pattern typedParameterInitializer
        // ArgumentID(name param_name, name param_type, expression param_initializer);

        if (param_type.getName() != null)
            usedTypes.add(getBasenameFromBinding(param_type));
        this.makeParameter(iNode, ((org.apache.flex.compiler.internal.tree.as.ParameterNode)iNode), currentScope, param_name, param_type, param_type);

        return null;
    }

    private static String removeSuffixIfPresent(String str, String suffix)
    {
        if (str.endsWith(suffix))
            return str.substring(0, str.length() - suffix.length());
        return str;
    }

    /*
     * General form of switch statement: expand into if/elseif.
     */

    // BlockID(switchCases cases+);

    public ConditionalFragment reduce_switchCase(IASNode iNode, String case_expr, Vector<String> case_actions)
    {
        // conditionalElements = Pattern switchCase
        // String case_expr, Vector<String> case_actions
        return new ConditionalFragment(iNode, case_expr, case_actions);
    }

    public ConditionalFragment reduce_switchDefault(IASNode iNode, String case_actions)
    {
        // conditionalElements = Pattern switchDefault
        return new ConditionalFragment(iNode, null, case_actions);
    }

    public ConditionalFragment reduce_switchDefault(IASNode iNode, java.util.Vector<String> case_actions)
    {
        // conditionalElements = Pattern switchDefault
        String s = "";
        for (int i = 0; i < case_actions.size(); i++)
            s += case_actions.get(i);

        return new ConditionalFragment(iNode, null, s);
    }

    public void prologue_labeledStmt(IASNode p, int iRule) throws java.lang.Exception
    {
        // statement = Pattern labeledStmt
    }

    public void prologue_withStmt(IASNode p, int iRule) throws java.lang.Exception
    {
        // statement = Pattern withStmt
    }

    /*
     * ********************
     * ** Declarations ** ********************
     */

    private String reduce_variableDecl(IASNode iNode, Name var_name, Binding var_type, String var_initializer, Vector<String> chained_decls)
    {
        // m_numMemberAsUint = num; needs to
        var_initializer = addDynamicCastToValue(iNode, var_initializer);

        // constant propagation for initialized members
        m_varInitValue = var_initializer;

        String result = "";
        final Boolean isStaticInit = currentScope.traitsVisitor == null;

        // CMP-???: {PackageInternalNs:"tests"}::n
        Name vname = var_name;
        Namespace ns = vname.getQualifiers().length() > 0 ? null : vname.getSingleQualifier();
        if (ns == null || ns.getKind() == CONSTANT_PackageInternalNs)
            vname = new Name(new Nsset(new Namespace(CONSTANT_PackageNs)), getBasenameFromName(var_name));

        if (!isStaticInit)
        {
            // support for [ConvertLocalVarsToMembers]
            // reduce_variableDecl() registers variables as members if convertLocalVarsToMembers(__p) 
            // is true via switching to the class scope.
            LexicalScope scope = currentScope;
            Boolean resetTraitsVisitor = false;
            if (convertLocalVarsToMembers(iNode))
            {
                scope = scope.getEnclosingFrame();
                if (scope.traitsVisitor == null)
                {
                    final JSEmitter emitter = (JSEmitter)currentScope.getEmitter();
                    scope.traitsVisitor = emitter.getCurrentClassVisitor().visitInstanceTraits();
                    resetTraitsVisitor = true;
                }

                this.registerConvertedMember(getBasenameFromName(var_name));
            }
            BaseVariableNode var_node = (BaseVariableNode)iNode;
            Binding var = scope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
            if (var_type != null && var_type.getName() != null)
                scope.makeVariable(var, var_type.getName(), ((BaseDefinitionNode)iNode).getMetaInfos());
            else
                scope.makeVariable(var);

            // support for [ConvertLocalVarsToMembers]
            if (resetTraitsVisitor)
                scope.traitsVisitor = null;
        }

        final VariableNode var = (VariableNode)iNode;
        final boolean isStaticName = var.hasModifier(ASModifier.STATIC);
        final String varName = nameToString(iNode, var_name, false, isStaticName);
        final boolean isMember = var.getDefinition().getParent() instanceof ClassDefinition;

        // add explicit dependencies.
        // regression, see shell.as: var playerType:String = Capabilities.playerType;
        // regression requires add explicit dependencies, see WSDK's Deferred.ctor, 
        //	  which references static Activity.current. Without modification Falcon does not 
        //	  recognize that Deferred is dependent on Activity. 
        if (var_initializer != null && !var_initializer.isEmpty())
        {
            for (IDefinition def : m_referencedDefinitions)
            {
                final String fullName = createFullNameFromDefinition(currentScope.getProject(), def);
                if (var_initializer.contains(fullName) && def.getParent() instanceof ClassDefinition)
                {
                    final ClassDefinition addTo = (ClassDefinition)def.getParent();
                    addDependency(def, addTo, DependencyType.INHERITANCE);
                    // m_sharedData.addDependency(def, addTo);
                    break;
                }
            }
        }

        if (isMember)
        {
            result += (isStaticInit ? "" : (indent() + indent())) + varName;
        }
        else
        {
            if (var_type != null && var_type.getName() != null)
            {
                final StringBuilder sb = new StringBuilder();
                final Boolean foundDefinition = nameToJSDocType(var_type, sb);
                /*
                 * IDefinition typeDef = var_type.getDefinition(); if( typeDef
                 * == null ) typeDef = getDefinitionForName(iNode,
                 * var_type.getName() ); final StringBuilder sb = new
                 * StringBuilder(); Boolean foundDefinition = false; if( typeDef
                 * == null ) foundDefinition = nameToJSDocType( var_type, sb );
                 * else foundDefinition = nameToJSDocType( typeDef, sb );
                 */
                if (!foundDefinition)
                    m_needsSecondPass = true;

                result += indent() + "/** @type {" + sb.toString() + "} */\n";
            }
            result += indent() + getVarSnippet(iNode, varName);
        }

//        if (var_type != null && var_type.getName() != null)
//            result += " /* : " + getBasenameFromBinding(var_type) + " */";

        if (var_initializer != null && !var_initializer.isEmpty())
            result += " = " + var_initializer;

        result += ";" + endl();

        for (String decl : chained_decls)
        {
            result += decl;
        }
        return result;
    }

    private boolean isLocalVariable(IVariableDefinition def)
    {
        return def.getVariableClassification() == IVariableDefinition.VariableClassification.LOCAL;
    }

    public String emptyList(IASNode iNode)
    {
        {
            // Pattern nilExpr
            // NilID(void);
            return new String("");
        }
    }

    public String reduce_unaryMinusExpr(IASNode iNode, String e)
    {
        {
            // postfix unary operator.
            // Pattern unaryMinusExpr
            // Op_SubtractID(expression e);
            String result = "(-" + stripNS(e) + ")";
            return result;
        }
    }

    public String reduce_unaryPlusExpr(IASNode iNode, String e)
    {
        {
            // Pattern unaryPlusExpr
            String result = "(+" + stripNS(e) + ")";
            return result;
        }
    }

    public String reduce_typeofExpr(IASNode iNode, String expr)
    {
        String result = null;

        // Pattern typeofExpr
        // Op_TypeOfID(expression expr);

        // e15_6_2, e15_4_2_2_2	
        // see makeMemberBinding()
        if (iNode instanceof IUnaryOperatorNode)
        {
            final IASNode exprNode = ((IUnaryOperatorNode)iNode).getOperandNode();
            if (exprNode instanceof FunctionCallNode)
            {
                final FunctionCallNode fcn = (FunctionCallNode)exprNode;
                final IDefinition def = fcn.resolveType(currentScope.getProject());
                if (def != null && def instanceof ITypeDefinition)
                {
                    final ITypeDefinition type = (ITypeDefinition)def;
                    result = getTypeOfString(type);
                }
            }
        }

        if (result == null)
            result = "typeof(" + stripNS(expr) + ")";
        return result;
    }

    public String reduce_bitNotExpr(IASNode iNode, String unary)
    {
        {
            // Pattern bitNotExpr
            // Op_BitwiseNotID(expression unary);
            String result = "~(" + stripNS(unary) + ")";
            return result;
        }
    }

    /*
     * Miscellaneous binary expressions.
     */

    public String reduce_istypeExprLate(IASNode iNode, String expr, String typename)
    {
        {
            // Pattern istypeExprLate
            // Op_IsID(expression expr, expression typename);

            // do not use stripNS, because 'org.w3c.dom.Document.createElementNS(adobe.root.browser.SVGUtils.ns, "g")' then becomes 'ns, "g")'.
            // String result = stripNS(expr) + " instanceof " + stripNS(typename);
            String result = reduce_instanceofExpr(iNode, expr, typename);
            return result;
        }
    }

    public String reduce_astypeExprLate(IASNode iNode, String expr, String typename)
    {
        {
            // Pattern astypeExprLate
            // Op_AsID(expression expr, expression typename);

            String result;

            // Don't emit dynamicCastAs() call when the typename on the RHS is one of our fake classes standing in for
            // 'native' browser or JS APIs, or when we're inside the core adobe.* code.
            boolean suppress = true; // AJH disabled getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME) ||
                                // isExternalClass(currentScope.getProject(), getDefinitionForName(iNode, makeName(typename)));

            /*
             * // As with handleCastExpr(), we don't handle conversion to
             * int/uint primitives yet - FJS-54 if( !suppress &&
             * (typename.equals("int") || typename.equals("uint")) ) {
             * printWarning(iNode,
             * "Conversion to "+typename+" will be ignored!"); suppress = true;
             * }
             */

            if (suppress)
                result = expr + " /* as " + stripNS(typename) + " */";
            else
            {
                if (typename.equals("int"))
                    typename = JSSharedData.FRAMEWORK_CLASS + ".IntClass";
                else if (typename.equals("uint"))
                    typename = JSSharedData.FRAMEWORK_CLASS + ".UIntClass";
                result = "adobe.dynamicCastAs(" + stripNS(expr) + ", " + stripNS(typename) + ")";
            }
            return result;
        }
    }

    public String reduce_inExpr(IASNode iNode, String needle, String haystack)
    {
        // Pattern inExpr
        // Op_InID(expression needle, expression haystack);
        String result;

        final boolean isFramework = getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME);
        if (isFramework)
            result = "(" + stripNS(needle) + " in " + stripNS(haystack) + ")";
        else
        {
            // TODO: static analysis of haystack.
            // Get IDefinition of haystack and check against Proxy's IDefinition.
            // If haystack is not a Proxy we can simply emit:
            // result = "(" + stripNS(needle) + " in " + stripNS(haystack) + ")";
            result = "adobe.inProperty(self, " + stripNS(needle) + ", " + stripNS(haystack) + ")";
        }
        return result;
    }

    private Boolean isDerivedFromInterface(IDefinition def, IInterfaceDefinition interf, Boolean includingDef)
    {
        if (def == interf)
            return includingDef;

        if (def != null)
        {
            IClassDefinition classDef = null;
            if (def instanceof IClassDefinition)
                classDef = (IClassDefinition)def;
            else if (def instanceof ITypeDefinition)
            {
                final ITypeDefinition type = (ITypeDefinition)def;
                def = type.resolveType(currentScope.getProject());
            }

            if (classDef != null)
            {
                Iterator<IClassDefinition> superClassIterator = classDef.classIterator(currentScope.getProject(), true);
                while (superClassIterator.hasNext())
                {
                    final IClassDefinition superClass = superClassIterator.next();
                    if (superClass == interf)
                        return true;

                    Iterator<IInterfaceDefinition> interfaceIterator = classDef.interfaceIterator(currentScope.getProject());
                    while (interfaceIterator.hasNext())
                    {
                        final IInterfaceDefinition implementedInterface = interfaceIterator.next();
                        if (implementedInterface == interf)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    public String reduce_instanceofExpr(IASNode iNode, String expr, String typename)
    {
        final Boolean optimize = true;
        String result;

        // Pattern instanceofExpr
        // Op_InstanceOfID(expression expr, expression typename);
        final boolean isFramework = getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME);

        if (optimize)
        {
            boolean useNative = isFramework;
            final String warnString = "is/instanceof expression is a constant: " + expr + " instanceof " + typename;

            // As with handleCastExpr(), we don't handle conversion to int/uint primitives yet - FJS-54
            boolean suppress = false;
            if (!isFramework && (typename.equals("int") || typename.equals("uint")))
            {
                printWarning(iNode, warnString);
                suppress = true;
            }
            else if (!useNative)
            {
                // optimization for "instanceof"
                IDefinition typeDef = m_sharedData.getDefinition(typename);
                if (typeDef == null)
                    typeDef = getDefinitionForNode(iNode, true);

                if (typeDef != null)
                {
                    final Boolean simpleOptimization = true;
                    if (simpleOptimization)
                    {
                        if (!(typeDef instanceof IInterfaceDefinition))
                            useNative = true;
                    }
                    else
                    {
                        IDefinition exprDef = m_sharedData.getDefinition(expr);
                        if (exprDef == null)
                            exprDef = getDefinitionForNode(iNode, false);

                        ICompilerProject project = currentScope.getProject();
                        if (!(typeDef instanceof IInterfaceDefinition))
                        {
                            if (typeDef instanceof IClassDefinition &&
                                 exprDef instanceof IClassDefinition &&
                                 typeDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.OBJECT) &&
                                 typeDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE) &&
                                 typeDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.CLASS) &&
                                 exprDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.OBJECT) &&
                                 exprDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE) &&
                                 exprDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.CLASS))
                            {
                                final IClassDefinition classDef = (IClassDefinition)typeDef;
                                Iterator<IClassDefinition> superClassIterator = classDef.classIterator(currentScope.getProject(), true);
                                while (superClassIterator.hasNext())
                                {
                                    final IClassDefinition superClass = superClassIterator.next();
                                    if (superClass == exprDef)
                                        return "true" + " /* " + warnString + " */";
                                }

                                return "false" + " /* " + warnString + " */";
                            }
                            useNative = true;
                        }
                        else
                        {
                            final IInterfaceDefinition interf = (IInterfaceDefinition)typeDef;
                            final Boolean isDerived = isDerivedFromInterface(exprDef, interf, true);

                            // If the rhs is not an Object or "*" then we can evaluate instanceof statically
                            // and we can use the result of isDerivedFromInterface() as a constant.
                            if (exprDef != null && interf != null &&
                                 exprDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.OBJECT) &&
                                 exprDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE) &&
                                 exprDef != project.getBuiltinType(IASLanguageConstants.BuiltinType.CLASS))
                            {
                                if (isDerived)
                                    return "true" + " /* " + warnString + " */";
                                else
                                    return "false" + " /* " + warnString + " */";
                            }

                            useNative = isDerived;
                        }
                    }
                }
            }

            if (useNative)
                result = "(" + stripNS(expr) + " instanceof " + stripNS(typename) + ")";
            else if (suppress)
                result = "false" + " /* " + warnString + " */";
            else
                result = "adobe.instanceOf(" + stripNS(expr) + ", " + stripNS(typename) + ")";
        }
        else
        {
            // As with handleCastExpr(), we don't handle conversion to int/uint primitives yet - FJS-54
            boolean suppress = false;
            if (!isFramework && (typename.equals("int") || typename.equals("uint")))
            {
                printWarning(iNode, "is/instanceof " + typename + " will always be false!");
                suppress = true;
            }

            if (isFramework)
                result = "(" + stripNS(expr) + " instanceof " + stripNS(typename) + ")";
            else if (suppress)
                result = "false";
            else
                result = JSSharedData.JS_FRAMEWORK_NAME + "." + "instanceOf(" + stripNS(expr) + ", " + stripNS(typename) + ")";
        }

        return result;
    }

    public Binding makeMemberBinding(IASNode iNode, String stem, String member)
    {
        /*
         * if( stem.equals(JSSharedData.THIS) && m_functionNode != null ) {
         * final String currentFullClassName = getCurrentFullClassName(); final
         * IDefinition classDef =
         * m_sharedData.getDefinition(currentFullClassName); if( classDef !=
         * null ) { IDefinition memberDef = getDefinitionForMember( iNode,
         * classDef, removeQuotes(member), ClassificationValue.MEMBERS_AND_TYPES
         * ); if( memberDef != null ) return new
         * Binding(iNode,makeName(member),memberDef); } }
         */

        // This is based on an idea Peter came up with...
        try
        {
            IASNode node = iNode;
            while (node != null)
            {
                if (node instanceof IBinaryOperatorNode && !(node instanceof MemberAccessExpressionNode))
                {
                    node = ((IBinaryOperatorNode)node).getLeftOperandNode();
                }
                else if (node instanceof IFunctionCallNode)
                    node = ((IFunctionCallNode)node).getNameNode();
                else if (node instanceof IDynamicAccessNode)
                    node = ((IDynamicAccessNode)node).getLeftOperandNode();
                else if (node instanceof IUnaryOperatorNode)
                    node = ((IUnaryOperatorNode)node).getOperandNode();
                // return new Binding(iNode,makeName(member),((IUnaryOperatorNode)node).getDefinition());
                else if (node instanceof IExpressionNode)
                {
                    IDefinition leftDef = ((IExpressionNode)node).resolve(currentScope.getProject());
                    if (leftDef instanceof VariableDefinition)
                    {
                        final VariableDefinition variable = (VariableDefinition)leftDef;
                        leftDef = variable.resolveType(currentScope.getProject());

                        // items from Vectors trigger adobe.getProperty() 
                        if (leftDef instanceof AppliedVectorDefinition)
                        {
                            final AppliedVectorDefinition vn = (AppliedVectorDefinition)leftDef;
                            leftDef = vn.resolveElementType(currentScope.getProject());
                            if (leftDef != null && leftDef instanceof ClassDefinition)
                                return new Binding(iNode, makeName(member), leftDef);
                        }
                    }
                    else if (leftDef instanceof FunctionDefinition)
                    {
                        final FunctionDefinition functionDef = (FunctionDefinition)leftDef;
                        final IReference typeRef = functionDef.getReturnTypeReference();

                        // Tamarin's ecma3/Expressions/e11_2_3_1.as triggers ClassCastException
                        // http://watsonexp.corp.adobe.com/#bug=3026796
                        if (typeRef != null)
                            leftDef = typeRef.resolve(currentScope.getProject(), (ASScope)getScopeFromNode(iNode), DependencyType.INHERITANCE, false);
                    }
                    else if (leftDef instanceof IGetterDefinition)
                    {
                        return new Binding(iNode, makeName(member), leftDef);
                        // final ITypeDefinition returnType = ((IGetterDefinition)leftDef).resolveReturnType(currentScope.getDefinitionCache());
                        // leftDef = m_sharedData.getDefinition(returnType.getQualifiedName());
                    }

                    if (leftDef != null && leftDef instanceof ClassDefinition)
                    {
                        IDefinition memberDef = getDefinitionForMember(iNode, leftDef, removeQuotes(member), ClassificationValue.MEMBERS_AND_TYPES);
                        if (memberDef != null)
                            return new Binding(iNode, makeName(member), memberDef);
                    }
                    node = null;
                }
                else
                {
                    node = null;
                }
            }
        }
        catch (Exception e)
        {
            // getDefinition() sometimes crashes, e.g. when looking at a cast to an interface in some cases,
            // FunctionDefinition.getParameters() returns null and ExpressionNodeBase.determineIfFunction() chokes on it
            printWarning(iNode, "makeMemberBinding() failed for member-access expression " + ((IMemberAccessExpressionNode)iNode).getDisplayString());
        }

        Binding b = makeBinding(iNode, member);
        return b;
    }

    private String handleCastExpr(IASNode iNode, Binding method_name, Vector<String> args)
    {
        // if( !JSSharedData.m_useClosureLib )
        {
            String className = bindingToString(iNode, method_name, false, true);
            className = removeRootName(className);

            boolean noOpCast = false; // If true, the function-call syntax is entirely stripped out
            boolean injectMissingNew = false; // Else, if true, we inject a 'new' operator
            // If both false, we keep the function-call syntax unchanged

            IDefinition def = getDefinitionForName(iNode, makeName(className));
            if (def != null)
            {
                // Cast to a built-in ActionScript language type?
                if (isDataType(def))
                {
                    // Casts to a built-in data type fall into several categories: conversions, and effective no-ops
                    //     Case A: cast to built-in object type (e.g. Function, Date)
                    //     Case B: conversion to built-in object (implicit 'new'), which browser will also grok
                    //             given the same cast-like syntax (e.g. Boolean, RegExp, Array)
                    //     Case C: conversion to built-in object (implicit 'new'), which requires an explicit
                    //             'new' in browser (e.g. Error, XML, XMLList)
                    //     Case D: conversion to primitive type (e.g. int), which requires generating special-case
                    //             code (e.g. Math.floor() in the case of int)
                    final String typeName = def.getBaseName();
                    if (isComplexDataType(typeName))
                    {
                        if (isErrorDataType(typeName) || typeName.equals("XML") || typeName.equals("XMLList"))
                            // Case C: casts (e.g. from String) implicitly new up an instance in AS, but not in JS - must inject a 'new' keyword
                            // TODO: need to do the same for subclasses of Error too! - FJS-71
                            injectMissingNew = true;
                        else if (typeName.equals("Array") || typeName.equals("RegExp"))
                            // Case B: casts (e.g. from String) implicitly new up an instance in both AS and JS
                            // safe to emit cast-like code (e.g. function call syntax)
                            noOpCast = false;
                        else
                            // Case A: just regular cast-to-object operations, like the !isDataType() case
                            noOpCast = true;
                    }
                    else
                    {
                        if (typeName.equals("Number") || typeName.equals("Boolean") || typeName.equals("String"))
                        {
                            // safe to emit cast-like code (e.g. function call syntax), so leave both flags false
                        }
                        else if (typeName.equals("IntClass"))
                        {
                            // According to Peter Math.floor() gives incorrect results for negative numbers.
                            // He suggests using ~~ instead.
                            // (Fixes FJS-54 in handleCastExpr())

                            // int(undefined) = 0
                            // int(null) = 0
                            // int(1.6) = 1
                            // int(-1) = -1
                            // int(-1.2) = -1
                            // int(-1.6) = -1

                            return "~~(" + args.get(0) + ") /* Cast to " + className + " */";
                        }
                        else if (typeName.equals("UIntClass"))
                        {
                            // (Fixes FJS-54 in handleCastExpr())

                            // uint(undefined) = 0
                            // uint(null) = 0
                            // uint(1.6) = 1
                            // uint(1.2) = 1
                            // uint(-1.2) = 1
                            // uint(-1) = 4294967295
                            // uint(-1.2) = 4294967295
                            // uint(-1.6) = 4294967295

                            // ((~~(-1.6)) < 0) ? (4294967296+(~~(-1.6))) : (~~(-1.6))

                            return "(((~~(" + args.get(0) + ")) < 0 ) ? (4294967296+(~~(" + args.get(0) + "))) : (~~(" + args.get(0) + ")) /* Cast to " + className + " */)";
                        }
                        else
                        {
                            // TODO: (remaining cases -- Null, *, void -- should never be encountered)
                            printWarning(iNode, "Cast to " + typeName + " will be ignored!");
                            noOpCast = true;
                        }
                    }

                }
                else
                {
                    // Casts to a user-defined type are *always* a simple cast (like Case A above)
                    boolean isType = (def instanceof IClassDefinition) || (def instanceof IInterfaceDefinition);
                    if (isType)
                    {
                        if (args.size() == 1)
                            noOpCast = true;
                        else
                            injectMissingNew = true; // TODO: is this case ever valid AS code?
                    }
                }
            }

            // prevent java.lang.ArrayIndexOutOfBoundsException,
            // see falconjs_cs6: Tamarin's ecma3/Date/e15_9_2_2_1.as triggers ArrayIndexOutOfBoundsException
            // http://watsonexp.corp.adobe.com/#bug=3022808
            if (noOpCast && args.size() == 1)
            {
                return args.get(0) + " /* Cast to " + className + " */";
            }
            else if (injectMissingNew)
            {
                return "/* Injecting missing new */ " + generateNewCall(iNode, method_name, args);
            }
        }
        return null;
    }

    /**
     * Returns a "direct" reference to the version of superMethodName defined by
     * the closest ancestor of invokingMethodClass (but not the version defined
     * by invokingMethodClass itself, if it has one; this mirrors the semantics
     * of super.foo() calls). A "direct" reference means a static-like reference
     * to what is actually an instance method. E.g. if the defining ancestor
     * class is Foo and the method name if bar, then the direct reference is
     * "Foo.prototype.bar"
     * 
     * @param invokingMethodClass Class whose code contains the super() call.
     * @param superMethodNameUnmunged Raw AS name of super method being invoked;
     * NO get_/set_ prefix.
     */
    public String getSuperMethodReference(IClassDefinition invokingMethodClass, String superMethodNameUnmunged)
    {
        assert !superMethodNameUnmunged.startsWith("get_");
        assert !superMethodNameUnmunged.startsWith("set_");

        IClassDefinition superClass = invokingMethodClass.resolveBaseClass(currentScope.getProject());

        // note: context must be invokingMethodClass, not superClass, so we have the right set of namespaces open for search
        ASDefinitionFilter filter = new ASDefinitionFilter(
                ClassificationValue.MEMBERS_AND_TYPES, SearchScopeValue.INHERITED_MEMBERS,
                AccessValue.ALL, invokingMethodClass);
        IDefinition superMethod = MemberedDefinitionUtils.getMemberByName(
                superClass, currentScope.getProject(), superMethodNameUnmunged, filter);

        if (superMethod instanceof IFunctionDefinition)
        {
            IFunctionDefinition method = (IFunctionDefinition)superMethod;
            IClassDefinition definingClass = (IClassDefinition)method.getParent();
            String definingClassReference = definitionToString(currentScope.getProject(), definingClass);

            String superMethodNameMunged = mungeMethodName(superMethodNameUnmunged, method, true);
            return definingClassReference + ".prototype." + superMethodNameMunged;

        }
        else
        {
            m_sharedData.stdout("##### Unable to generate super method reference #####");
            return "pf_unknown";
        }

        // Note: an alternative impl would be to do a search for the function def using invokingMethodClass as the
        // membereddef scope, and then if the resulting IFunctionDefinition's parent is invokingMethodClass, calling IFunctionDefinition.resolveOverriddenFunction().
    }

    /*
     * Expression lists.
     */

    public Vector<String> reduce_exprList(IASNode iNode, Vector<String> args)
    {
        // Expression lists.
        // Pattern exprList
        // ContainerID(expression args+);   
        return args;
    }

    /**
     * Convert ActionScript function name to JavaScript function name (with
     * get_/set_ prefix as needed).
     * 
     * @param forceGetForSetters If true, a get_ prefix is used when
     * 'definition' is a setter. Use this if you can expect the result to later
     * be passed to resolveSetterName() (which replaces get_ with set_) if the
     * expression is ultimately being used as an lvalue.
     */
    private String mungeMethodName(String asName, IFunctionDefinition definition, boolean forceGetForSetters)
    {
        if (definition instanceof IGetterDefinition)
            return JSSharedData.GETTER_PREFIX + asName;
        else if (definition instanceof ISetterDefinition)
        {
            String prefix = forceGetForSetters ? JSSharedData.GETTER_PREFIX : JSSharedData.SETTER_PREFIX;
            return prefix + asName;
        }
        else
            return asName;
    }

    //  TODO: The parser should differentiate various constants 
    //  (and parse their values, or prepare them for parsing), 
    //  see http://bugs.adobe.com/jira/browse/CMP-54
    public Long reduce_numericLiteral(IASNode iNode)
    {
        {
            // Literals.
            // Pattern numericLiteral
            // LiteralNumberID(void);
            return Long.decode(((org.apache.flex.compiler.tree.as.ILiteralNode)iNode).getValue());
        }
    }

    public int isIntegerConstant(IASNode iNode)
    {
        //  Horribly inefficient, but a robust expedient.
        try
        {
            Long.decode(((org.apache.flex.compiler.internal.tree.as.LiteralNode)iNode).getValue());
        }
        catch (NumberFormatException ex)
        {
            return Integer.MAX_VALUE;
        }
        return 0;
    }

    public Long reduce_uintLiteral(IASNode iNode)
    {
        {
            // uint_constant = Pattern uintLiteral
            // ArgumentID()
            final Long uint_constant = new Long(((org.apache.flex.compiler.tree.as.INumericLiteralNode)iNode).getNumericValue().toUint32());
            return uint_constant;
        }
    }

    public String toString()
    {
        if (m_functionName.length() > 0)
            return "JSGeneratingReducer: " + this.createFullName(this.m_functionName, false);

        return "JSGeneratingReducer: " + this.createFullClassName(false);
    }

    private static String[] emitParameters(List<String> paramNames,
                                             Vector<Name> paramTypes,
                                             Vector<PooledValue> defaultValues,
                                             Boolean needsRest) throws Exception
    {
        String code = "";

        String a_priori_insns = "";
        Boolean emitComma = false;

        // CMP-355: paramNames is always empty
        /*
         * if( paramNames.isEmpty() ) { for (Trait t : traits) { if( t.getKind()
         * != TRAIT_Var ) throw new
         * IllegalArgumentException("Unknown parameter trait kind " +
         * t.getKind()); Name name = t.getNameAttr("name"); Namespace ns =
         * name.getSingleQualifier(); if( ns.getKind() ==
         * CONSTANT_PackageInternalNs ) { paramNames.add(
         * getBasenameFromName(name) ); } } }
         */

        int nthParam = 0;
        final int defaultsStartAt = paramTypes.size() - defaultValues.size();
        final int restStartsAt = needsRest ? paramNames.size() - 1 : paramNames.size();
        final String varPrefix = JSSharedData.DEFAULT_PARAM_PREFIX;
        final String indent = "        ";
        for (String argName : paramNames)
        {
            if (nthParam == restStartsAt)
            {
                // param is rest argument

                // JavaScript only knows arguments
                if (!argName.equals("arguments"))
                {
                    a_priori_insns += "var " + argName + " = arguments;\n";
                }

                // now remove the parameters from arguments that we already got.
                final int argsLen = paramNames.size();
                if (argsLen > 1)
                {
                    // Argument is an Object and not an Array. These lines implement shift().                        
                    a_priori_insns += indent + "var _args = [];\n";
                    a_priori_insns += indent + "for( var i = 0; i < (arguments.length - " + (argsLen - 1) + "); i++)\n";
                    a_priori_insns += indent + "{\n";
                    a_priori_insns += indent + "    _args.push(arguments[i+" + (argsLen - 1) + "]);\n";
                    a_priori_insns += indent + "}\n";
                    a_priori_insns += indent + "arguments = _args;\n";
                }
            }
            else
            {
                if (emitComma)
                    code += ", ";
                else
                    emitComma = true;

                final Name nextParam = paramTypes.elementAt(nthParam);
                final String argType = nextParam == null ? "*" : getBasenameFromName(nextParam);
                if (nthParam < defaultsStartAt)
                {
                    // param without default value
                    code += argName; // + " /* : " + argType + " */";
                }
                else
                {
                    // param with default value
                    String defaultVal = "undefined";
                    PooledValue val = defaultValues.elementAt(nthParam - defaultsStartAt);
                    if (val != null)
                    {
                        switch (val.getKind())
                        {
                            case ABCConstants.CONSTANT_Int:
                                defaultVal = val.getIntegerValue().toString();
                                break;
                            case ABCConstants.CONSTANT_UInt:
                                defaultVal = val.getLongValue().toString();
                                break;
                            case ABCConstants.CONSTANT_Double:
                                defaultVal = val.getDoubleValue().toString();
                                break;
                            case ABCConstants.CONSTANT_Utf8:
                            {
                                defaultVal = val.getStringValue();
                                if (!defaultVal.startsWith("\"") && !defaultVal.startsWith("\'"))
                                    defaultVal = "\"" + defaultVal;
                                if (!defaultVal.endsWith("\"") && !defaultVal.endsWith("\'"))
                                    defaultVal += "\"";
                            }
                                break;
                            case ABCConstants.CONSTANT_True:
                                defaultVal = "true";
                                break;
                            case ABCConstants.CONSTANT_False:
                                defaultVal = "false";
                                break;
                            case ABCConstants.CONSTANT_Undefined:
                                defaultVal = "undefined";
                                break;
                            case ABCConstants.CONSTANT_Null:
                                defaultVal = "null";
                                break;
                            default:
                            {
                                final Namespace ns = val.getNamespaceValue();
                                if (ns != null)
                                    defaultVal = ns.getName() + " /* (namespace) */";
                            }
                                break;
                        }
                    }

                    code += varPrefix + argName; // + " /* : " + argType + " = " + defaultVal + " */";

                    // feed a_priori_insns
                    a_priori_insns += indent + "/** @type {" + argType + "} */\n";
                    a_priori_insns += indent + "var " + argName + " = " + defaultVal + ";\n";
                    a_priori_insns += indent + "if (typeof(" + varPrefix + argName + ") != 'undefined') ";
                    a_priori_insns += "{ " + argName + " = " + varPrefix + argName + "; }\n";
                }
            }

            nthParam++;
        }

        String[] ret = new String[] {a_priori_insns, code};

        return ret;
    }

    public static String[] emitParameters(MethodInfo mi) throws Exception
    {
        Vector<PooledValue> defaultValues = mi.getDefaultValues();
        Vector<Name> paramTypes = mi.getParamTypes();
        List<String> paramNames = mi.getParamNames();
        final Boolean needsRest = (mi.getFlags() & ABCConstants.NEED_REST) > 0;

        return emitParameters(paramNames, paramTypes, defaultValues, needsRest);
    }

    private static PooledValue createPooledValue(ICompilerProject project, String value, TypeDefinitionBase type)
    {
        if (value.equals("null"))
            return new PooledValue(ABCConstants.CONSTANT_Null, null);

        if (value.equals("undefined"))
            return new PooledValue(ABCConstants.CONSTANT_Undefined, ABCConstants.UNDEFINED_VALUE);

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.STRING))
        {
            if (value.startsWith("\"") && value.endsWith("\""))
                return new PooledValue(ABCConstants.CONSTANT_Utf8, value);
        }

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.INT))
        {
            if (value.startsWith("0x"))
                return new PooledValue(ABCConstants.CONSTANT_Int, Integer.parseInt(value.substring(2), 16));
            else
                return new PooledValue(ABCConstants.CONSTANT_Int, Integer.parseInt(value));
        }

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.UINT))
        {
            if (value.startsWith("0x"))
                return new PooledValue(ABCConstants.CONSTANT_UInt, Long.parseLong(value.substring(2), 16));
            else
                return new PooledValue(ABCConstants.CONSTANT_UInt, Long.parseLong(value));
        }

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.NUMBER))
            return new PooledValue(ABCConstants.CONSTANT_Double, Double.parseDouble(value));

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.BOOLEAN))
        {
            final Boolean bool = Boolean.parseBoolean(value);
            if (bool)
                return new PooledValue(ABCConstants.CONSTANT_True, true);
            else
                return new PooledValue(ABCConstants.CONSTANT_False, false);
        }

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.Undefined))
            return new PooledValue(ABCConstants.CONSTANT_Undefined, ABCConstants.UNDEFINED_VALUE);

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.NULL))
            return new PooledValue(ABCConstants.CONSTANT_Null, null);

        if (type == project.getBuiltinType(IASLanguageConstants.BuiltinType.NAMESPACE))
            return new PooledValue(ABCConstants.CONSTANT_Namespace, null);

        // Note that I am returning CONSTANT_Undefined with value if I can't figure out what value is.
        JSSharedData.instance.stderr("Warning: cannot create PooledValue for '" + value + "' of type " + type.getBaseName());
        return new PooledValue(ABCConstants.CONSTANT_Undefined, value);
    }

    public static String[] emitParameters(ICompilerProject project, FunctionDefinition fdef, MethodInfo mi) throws Exception
    {
        Vector<PooledValue> defaultValues = mi.getDefaultValues();
        Vector<Name> paramTypes = mi.getParamTypes();
        List<String> paramNames = mi.getParamNames();
        Boolean needsRest = (mi.getFlags() & ABCConstants.NEED_REST) > 0;

        // workaround for Falcon bugs.
        // mi.defaultValues and funcDef.getParameters() can get out of sync and are not 
        // corrected in Abc/JSGenerator::createMethodInfo.

        // is mi.hasOptional() reliable?
        if (!paramNames.isEmpty() &&
             (defaultValues == null || defaultValues.isEmpty()) &&
             fdef != null &&
             fdef.getParameters().length > 0)
        {
            defaultValues = new Vector<PooledValue>();

            needsRest = false;
            final ParameterDefinition[] args = fdef.getParameters();
            paramTypes = new Vector<Name>();
            paramNames = new ArrayList<String>();

            if (args.length > 0)
            {
                for (ParameterDefinition arg : args)
                {
                    TypeDefinitionBase arg_type = arg.resolveType(project);
                    Name type_name = arg_type != null ? arg_type.getMName(project) : null;

                    if (arg.hasDefaultValue())
                    {
                        final String defaultVal = arg.resolveDefaultValue(project).toString();
                        final PooledValue pv = createPooledValue(project, defaultVal, arg.resolveType(project));
                        defaultValues.add(pv);
                    }

                    if (arg.isRest())
                    {
                        needsRest = true;
                        paramNames.add(arg.getBaseName());
                    }
                    else
                    {
                        paramTypes.add(type_name);
                        paramNames.add(arg.getBaseName());
                    }
                }
            }
        }

        return emitParameters(paramNames, paramTypes, defaultValues, needsRest);
    }

    /**
     * Prints a warning message to the console, including file name and line
     * number
     */
    private static void printWarning(IASNode iNode, String message)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("Warning: " + message + "\n");
        if (iNode != null)
            sb.append("    " + iNode.getSourcePath() + ": " + (iNode.getLine() + 1) + "\n");
        else
            sb.append("    (unknown location in code)\n");
        sb.append("\n");
        JSSharedData.instance.stderr(sb.toString());
    }

    /** Exception thrown due to an unexpected, internal FalconJS error */
    /*
     * public static class FalconJSError extends RuntimeException { // TODO:
     * require passing an IASNode so we can indicate which line of AS code
     * triggered the internal error public FalconJSError(String message) {
     * super(message); } }
     */
    /**
     * Convenience method generates an r-value.
     * 
     * @return the instruction sequence to look up the given multiname.
     */
    /*
     * private String generateRvalue() { return generate(OP_getproperty, null);
     * }
     */

    /**
     * Convenience method generates an assignment
     * 
     * @param value - the value to set.
     * @return the instruction sequence to set a the given multiname's value.
     */
    /*
     * private String generateAssignment(String value) { return
     * generate(OP_setproperty, value); }
     */

    /**
     * Get the definition associated with a node and decide if the qualifier is
     * a compile-time constant.
     * 
     * @param iNode - the node to check.
     * @pre - the node is an IdentifierNode and has a child.
     * @return true if the child has a known namespace, i.e., it's a
     * compile-time constant qualifier.
     */
    int isMultiname(IASNode iNode)
    {
        IdentifierNode qualifier = (IdentifierNode)SemanticUtils.getNthChild(iNode, 0);
        IDefinition def = qualifier.resolve(currentScope.getProject());

        int result = def instanceof NamespaceDefinition ? 1 : Integer.MAX_VALUE;
        return result;
    }

    private String getMethodName()
    {
        String methodName = null;
        final MethodInfo mi = currentScope.getMethodInfo();
        if (mi != null)
            methodName = mi.getMethodName();
        else if (m_functionNode != null)
            methodName = m_functionNode.getName();
        return methodName;
    }

    private Boolean isAnonymousFunction(String methodName)
    {
        return methodName != null && methodName.endsWith(":anonymous");
    }

    /**
     * Converts a definition to a String
     * 
     * @param def
     * @return name of def, or null
     */
    public static String definitionToString(ICompilerProject project, IDefinition def)
    {
        if (def != null)
        {
            // register this definition as a used extern if necessary.
            if (def.getMetaTagByName(JSSharedData.EXTERN_METATAG) != null)
            {
                JSSharedData.instance.registerExtern(def);
                JSSharedData.instance.registerUsedExtern(def.getQualifiedName());
            }

            final String baseName = def.getBaseName();

            // doesn't work, check for InternalNamespaceDefinition instead. 
            if (def instanceof GetterDefinition || def instanceof SetterDefinition)
            {
                // Note: setter defs are given a get_ prefix here; resolveSetterName() will turn this into set_ later if
                // the def is really being used in an assignment context. But often, when we look up a property def we'll
                // get the setter def even if it's being used in a get context, and thus the get_ prefix will remain.
                return makeGetterCall(project, def, JSSharedData.THIS, baseName);
            }
            else if (isPackageFunction(def))
            {
                return createFullNameFromDefinition(project, def);
            }
            /*
             * Used to be not necessary, because we interface for external
             * classes:
             * goog.provide("adobe.root.org.w3c.dom.events.EventTarget");
             * adobe.root.org.w3c.dom.events.EventTarget = typeof(EventTarget)
             * == "undefined" ? function() {} : EventTarget; But we don't emit
             * externs anymore. So this code is necessary:
             */
            else if (JSSharedData.instance.isExtern(def.getQualifiedName()))
            {
                return JSSharedData.instance.getExternName(def.getQualifiedName());
            }
            else if (def instanceof ClassDefinition || def instanceof InterfaceDefinition)
            {
                return createFullNameFromDefinition(project, def);
            }
            else if (def.getContainingScope() instanceof CatchScope)
            {
                return baseName;
            }
            if (isLocalName(def))
            {
                return baseName;
            }
            else if (def instanceof ParameterDefinition)
            {
            	return baseName;
            }
            else if (isGlobalName(def))
            {
                return baseName;
            }
            else
            {
                final Boolean isStatic = m_convertToStatic || def.isStatic();
                if (isStatic)
                    return createFullNameFromDefinition(project, def);

                return "this" + "." + baseName;
            }
        }
        return null;
    }

    public String nameToString(IASNode iNode, IDefinition def, Name name)
    {
        if (def == null)
            currentScope.addProblem(new JSInternalCompilerProblem(iNode, "no definition for " + name.toString()));

        // see FJS-44, ambiguous_shortname testcase
        if (def instanceof AmbiguousDefinition)
        {
            m_needsSecondPass = true;

            String unambiguousName = name.getBaseName();
            /*
             * if( unambiguousName != null ) { final Nsset nsset =
             * name.getQualifiers(); if( nsset != null && nsset.length() > 0 )
             * unambiguousName = nsset.iterator().next().getName() + "." +
             * unambiguousName; }
             */
            return unambiguousName + " /* WARNING: ambiguous name */";
        }

        final String baseName = def.getBaseName();

        // Simple name mappings: Table, base names
        if (m_nameMap.containsKey(baseName))
        {
            return m_nameMap.get(baseName);
        }

        // filter out the strings
        if (isLongNumberString(baseName) || isStringWithQuotes(baseName) || baseName.contains(" "))
            return baseName;

        // see action_superAccess
        if (baseName.startsWith(JSSharedData._SUPER + "."))
        {
            return JSSharedData.THIS + "." + baseName;
        }

        // resolving the name...
        final String str = definitionToString(currentScope.getProject(), def);
        if (str != null && str.length() > 0)
        {
            // support for [ConvertLocalVarsToMembers]
            if (convertLocalVarsToMembers(iNode) &&
                 !str.startsWith(JSSharedData.THIS + ".") &&
                 (def instanceof VariableDefinition || def instanceof FunctionDefinition) &&
                 !(def instanceof ParameterDefinition))
            {
                Boolean addThis = true;

                if (def instanceof FunctionDefinition)
                {
                    final IFunctionDefinition fdef = (FunctionDefinition)def;
                    if (fdef.isStatic())
                        addThis = false;
                    else if (fdef.getParent() == null || !(fdef.getParent() instanceof ClassDefinition))
                        addThis = false;
                }

                if (addThis)
                {
                    this.registerConvertedMember(str);
                    return JSSharedData.THIS + "." + str;
                }
            }
            return str;
        }

        final String packageName = getPackageNameFromName(iNode, name, false, false);
        if (packageName != null && !packageName.isEmpty())
        {
            final String fullName = packageName + "." + baseName;
            if (m_sharedData.getVarType(fullName) != null)
                return fullName;
            return fullName;
        }

        // support for vectors
        if (name.getBaseName() == null && name.getTypeNameBase().getBaseName().equals("Vector"))
            return baseName;

        return "/* unknown */ " + baseName;
    }

    public String nameToString(IASNode iNode, Name name, Boolean withThis, Boolean resolve)
    {
        IDefinition def = getDefinitionForName(iNode, name);
        final String str = nameToString(iNode, def, name);

        return str;
        /*
         * final String baseName = getBasenameFromName(name); // Simple name
         * mappings: Table, base names if( m_nameMap.containsKey(baseName) ) {
         * return m_nameMap.get(baseName); } // filter out the strings if(
         * isLongNumberString(baseName) || isStringWithQuotes(baseName) ||
         * baseName.contains(" ")) return baseName; // see action_superAccess
         * if( baseName.startsWith( JSSharedData._SUPER + "." ) ) { return
         * JSSharedData.THIS + "." + baseName; } // resolving the name...
         * IDefinition def = getDefinitionForName( iNode, name ); final String
         * str = definitionToString(currentScope.getProject(), def); if( str !=
         * null && str.length() > 0 ) { // support for
         * [ConvertLocalVarsToMembers] if( convertLocalVarsToMembers(iNode) &&
         * !str.startsWith(JSSharedData.THIS + ".") && def instanceof
         * VariableDefinition && !(def instanceof ParameterDefinition) ) {
         * return JSSharedData.THIS + "." + str; } return str; } final String
         * packageName = getPackageNameFromName(name, false, false); if(
         * packageName != null && !packageName.isEmpty() ) { final String
         * fullName = packageName + "." + baseName; if(
         * m_sharedData.getVarType(fullName) != null ) return fullName; return
         * fullName; } // support for vectors if( name.getBaseName() == null &&
         * isVectorTypeName(name)) return baseName; return "/ * unknown * / " +
         * baseName;"
         */
    }

    public static boolean isVectorTypeName(Name name)
    {
        return name.getTypeNameBase().getBaseName().equals("Vector");
    }

    public static Boolean isPackageFunction(IDefinition def)
    {
        if (def instanceof FunctionDefinition)
        {
            /*
             * If a definition comes from a SWC then the parent always seems to
             * be null for package functions. In those cases we look for the
             * containing scope and test against ASFileScope. In the case of
             * getDefinitionByName from flash.swc we'll see a SWCFileScope.
             */
            if (def.getContainingScope() instanceof ASFileScope)
                return true;

            // VALIDATE: currentScope.methodInfo != null &&
            if (def.getParent() != null)
            {
                IDefinition parentDef = def.getParent();
                if (parentDef instanceof PackageDefinition)
                    return true;
            }
        }

        return false;
    }

    private String opToString(IASNode iNode, int op)
    {
        switch (op)
        {
            //  Binary logical operators.
            case OP_equals:
                return "==";
            case OP_strictequals:
                return "===";
            case OP_greaterthan:
                return ">";
            case OP_greaterequals:
                return ">=";
            case OP_lessthan:
                return "<";
            case OP_lessequals:
                return "<=";

                // Binary branching constructs
            case OP_ifeq:
                return "==";
            case OP_ifne:
                return "!=";
            case OP_ifstricteq:
                return "===";
            case OP_ifstrictne:
                return "!==";
            case OP_ifgt:
                return ">";
            case OP_ifge:
                return ">=";
            case OP_iflt:
                return "<";
            case OP_ifle:
                return "<=";

                //  Binary arithmetic operators
            case OP_add:
                return "+";
            case OP_subtract:
                return "-";
            case OP_divide:
                return "/";
            case OP_multiply:
                return "*";
            case OP_modulo:
                return "%";
            case OP_bitor:
                return "|";
            case OP_bitand:
                return "&";
            case OP_bitxor:
                return "^";
            case OP_lshift:
                return "<<";
            case OP_rshift:
                return ">>";
            case OP_urshift:
                return ">>>";

                // 
            case OP_in:
                return "in";
        }
        currentScope.addProblem(new JSInternalCompilerProblem(iNode, "Unknown op '" + op + "'"));
        return "";
    }

    private final static String THIS_SNIPPET = JSSharedData.THIS + "."; // support for [ConvertLocalVarsToMembers]
    private final static String JS_THIS_SNIPPET = JSSharedData.JS_THIS + "."; // support for [ConvertLocalVarsToMembers]

    private final static String VAR_SNIPPET = "var "; // support for [ConvertLocalVarsToMembers]
    private final static Map<String, String> m_typeMap = createTypeMap();

    private String aPrioriInstructions;
    private String m_functionName = "";
    private int m_indent = 0;
    private Map<String, String> m_nameMap = new HashMap<String, String>();

    public Set<String> usedTypes = new TreeSet<String>();
    public Set<String> usedClasses = new TreeSet<String>();
    public static Boolean m_convertToStatic = JSSharedData.m_useSelfParameter && false;
    private ICompilationUnit.Operation m_buildPhase = Operation.INVALIDATE_CU;
    private JSSharedData m_sharedData;

    private Boolean m_needsSecondPass = false;
    private FunctionNode m_functionNode = null; // support for [ConvertLocalVarsToMembers]
    private Set<String> m_convertedMembers = new HashSet<String>(); // support for [ConvertLocalVarsToMembers]
    private DefinitionBase m_currentVariable = null; // constant propagation for initialized members
    private Boolean m_hasSideEffects = false; // constant propagation for initialized members
    private String m_varInitValue = null; // constant propagation for initialized members

    private ClassDefinition m_currentClassDefinition = null;
    private Set<IDefinition> m_referencedDefinitions = new HashSet<IDefinition>();

    public JSSharedData getSharedData()
    {
        return m_sharedData;
    }

    public void setClassDefinition(ICompilerProject project, ClassDefinition classDef)
    {
        m_currentClassDefinition = classDef;

        // poor man's type inference.
        if (m_currentClassDefinition != null && project != null)
        {
            JSSharedData.instance.registerMethods(project, m_currentClassDefinition);
        }

    }

    private static Map<String, String> createTypeMap()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("String", "string");
        map.put("uint", "number");
        map.put("int", "number");
        map.put("Number", "number");
        map.put("Boolean", "boolean");
        map.put("Class", "function(...)");
        map.put("Function", "function(...)");
        return map;
    }

    private static Boolean vectorNameToJSDocType(Name name, StringBuilder sb)
    {
        final String baseName = name.getBaseName();
        if (baseName == null && name.getTypeNameBase().getBaseName().equals("Vector"))
        {
            final String param = getBasenameFromName(name);
            sb.append(param);
            return true;
        }

        if (baseName.equals("..."))
        {
            sb.append("Array");
            return true;
        }

        return false;
    }

    private static Boolean nameToJSDocType(IDefinition type, StringBuilder sb)
    {
        if (type == null)
        {
            sb.append("*");
            return false;
        }

        String _type = type.getBaseName();
        final String packageName = type.getPackageName();
        if (packageName != null && !packageName.isEmpty())
        {
            if (type.getQualifiedName().startsWith("__AS3__.vec.Vector$"))
            {
                if (m_typeMap.containsKey(_type))
                    sb.append("Array.<" + m_typeMap.get(_type) + ">");
                else
                    sb.append("Array.<" + _type + ">");
            }
            else
            {
                _type = JSSharedData.ROOT_NAME + packageName + "." + _type;
                if (_type.contains(":"))
                    _type = _type.substring(_type.indexOf(":") + 1);
            }
        }
        else
        {
            final String mapped = m_typeMap.get(_type);
            if (mapped != null)
                _type = mapped;
        }

        sb.append(_type);
        return true;
    }

    private Boolean nameToJSDocType(Binding binding, StringBuilder sb)
    {
        IDefinition def = binding.getDefinition();
        if (def != null)
            return nameToJSDocType(def, sb);

        final Name name = getNameFromBinding(binding);
        return nameToJSDocType(currentScope.getProject(), name, sb);
    }

    public static Boolean nameToJSDocType(ICompilerProject project, Name name, StringBuilder sb)
    {
        if (project == null || name == null)
        {
            sb.append("*");
            return false;
        }

        if (vectorNameToJSDocType(name, sb))
            return true;

        final IDefinition typeDef = getDefinitionForName(project, name);

        // see FJS-31 & FJS-63
        if (typeDef == null || typeDef instanceof AmbiguousDefinition)
        {
            String packageName = "";
            final String baseName = getBasenameFromName(name);
            final Nsset nsset = name.getQualifiers();
            if (nsset != null && nsset.length() > 0)
            {
                packageName = nsset.iterator().next().getName();
                if (packageName.contains(":"))
                    packageName = packageName.substring(packageName.indexOf(":") + 1);
                if (packageName.contains(":"))
                    packageName = packageName.replaceAll(":", ".");
            }
            if (!packageName.isEmpty())
                sb.append(packageName + "." + baseName);
            else
                sb.append(baseName);
            return false;
        }

        return nameToJSDocType(typeDef, sb);
    }

    private Boolean nameToJSDocType(IASNode iNode, Name name, StringBuilder sb)
    {
        if (iNode == null || name == null)
        {
            sb.append("Object");
            return false;
        }

        if (vectorNameToJSDocType(name, sb))
            return true;

        final IDefinition typeDef = getDefinitionForName(iNode, name);
        return nameToJSDocType(typeDef, sb);
    }

    // If JSEmitter.needsSecondPass() returns true, JSGenerator.generate() will return null during scanning, 
    // which will result in JSCompilationUnit::handleSemanticProblemsRequest not caching any abcBytes for 
    // handleABCBytesRequest. The net result is that JSGenerator.generate() will be called again in handleABCBytesRequest. 
    // This mechanic will ensure selective two-pass compilation. 
    public Boolean needsSecondPass()
    {
        return m_needsSecondPass;
    }

    public void setNeedsSecondPass()
    {
        m_needsSecondPass = true;
    }

    public static String now()
    {
        final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    public static String getTimeStampString()
    {
        if (JSSharedData.OUTPUT_TIMESTAMPS)
            return "CROSS-COMPILED BY " + JSSharedData.COMPILER_NAME + " (" + JSSharedData.COMPILER_VERSION + ") ON " + JSGeneratingReducer.now() + "\n";
        else
            return "CROSS-COMPILED BY " + JSSharedData.COMPILER_NAME + "\n";
    }

    private static Boolean checkConstant(int c1, int c2, String name)
    {
        if (c1 != c2)
            throw JSSharedData.backend.createException("Constants differ: CmcEmitter." + name + "CmcJSEmitter." + name + "(" + c1 + " != " + c2 + ")");
        return true;
    }

    public static Boolean validate()
    {
        checkConstant(CmcEmitter.__expression_NT, CmcJSEmitter.__expression_NT, "__expression_NT");
        checkConstant(CmcEmitter.__boolean_literal_NT, CmcJSEmitter.__boolean_literal_NT, "__boolean_literal_NT");
        checkConstant(CmcEmitter.__comparison_expression_NT, CmcJSEmitter.__comparison_expression_NT, "__comparison_expression_NT");
        checkConstant(CmcEmitter.__void_expression_NT, CmcJSEmitter.__void_expression_NT, "__void_expression_NT");
        checkConstant(CmcEmitter.__function_NT, CmcJSEmitter.__function_NT, "__function_NT");
        checkConstant(CmcEmitter.__parameter_NT, CmcJSEmitter.__parameter_NT, "__parameter_NT");
        checkConstant(CmcEmitter.__return_type_name_NT, CmcJSEmitter.__return_type_name_NT, "__return_type_name_NT");
        checkConstant(CmcEmitter.__catch_block_NT, CmcJSEmitter.__catch_block_NT, "__catch_block_NT");
        // checkConstant(CmcEmitter.__multinameL_NT, CmcJSEmitter.__multinameL_NT, "__multinameL_NT" ); 
        checkConstant(CmcEmitter.__runtime_name_expression_NT, CmcJSEmitter.__runtime_name_expression_NT, "__runtime_name_expression_NT");
        checkConstant(CmcEmitter.__parameterList_NT, CmcJSEmitter.__parameterList_NT, "__parameterList_NT");
        checkConstant(CmcEmitter.__name_NT, CmcJSEmitter.__name_NT, "__name_NT");
        checkConstant(CmcEmitter.__object_literal_NT, CmcJSEmitter.__object_literal_NT, "__object_literal_NT");
        checkConstant(CmcEmitter.__double_constant_NT, CmcJSEmitter.__double_constant_NT, "__double_constant_NT");
        checkConstant(CmcEmitter.__vector_literal_NT, CmcJSEmitter.__vector_literal_NT, "__vector_literal_NT");
        checkConstant(CmcEmitter.__finally_clause_NT, CmcJSEmitter.__finally_clause_NT, "__finally_clause_NT");
        checkConstant(CmcEmitter.__array_literal_NT, CmcJSEmitter.__array_literal_NT, "__array_literal_NT");
        checkConstant(CmcEmitter.__e4x_literal_NT, CmcJSEmitter.__e4x_literal_NT, "__e4x_literal_NT");
        checkConstant(CmcEmitter.__var_decl_NT, CmcJSEmitter.__var_decl_NT, "__var_decl_NT");
        checkConstant(CmcEmitter.__qualifiedNamePart_NT, CmcJSEmitter.__qualifiedNamePart_NT, "__qualifiedNamePart_NT");
        checkConstant(CmcEmitter.__statement_NT, CmcJSEmitter.__statement_NT, "__statement_NT");
        checkConstant(CmcEmitter.__boolean_constant_NT, CmcJSEmitter.__boolean_constant_NT, "__boolean_constant_NT");
        checkConstant(CmcEmitter.__object_literal_element_NT, CmcJSEmitter.__object_literal_element_NT, "__object_literal_element_NT");
        checkConstant(CmcEmitter.__uint_constant_NT, CmcJSEmitter.__uint_constant_NT, "__uint_constant_NT");
        checkConstant(CmcEmitter.__constant_value_NT, CmcJSEmitter.__constant_value_NT, "__constant_value_NT");
        checkConstant(CmcEmitter.__new_type_name_NT, CmcJSEmitter.__new_type_name_NT, "__new_type_name_NT");
        checkConstant(CmcEmitter.__non_resolving_identifier_NT, CmcJSEmitter.__non_resolving_identifier_NT, "__non_resolving_identifier_NT");
        checkConstant(CmcEmitter.__integer_constant_NT, CmcJSEmitter.__integer_constant_NT, "__integer_constant_NT");
        checkConstant(CmcEmitter.__string_constant_NT, CmcJSEmitter.__string_constant_NT, "__string_constant_NT");
        checkConstant(CmcEmitter.__type_name_NT, CmcJSEmitter.__type_name_NT, "__type_name_NT");
        checkConstant(CmcEmitter.__conditionalJump_NT, CmcJSEmitter.__conditionalJump_NT, "__conditionalJump_NT");
        checkConstant(CmcEmitter.__dottedNamePart_NT, CmcJSEmitter.__dottedNamePart_NT, "__dottedNamePart_NT");
        checkConstant(CmcEmitter.__literal_NT, CmcJSEmitter.__literal_NT, "__literal_NT"); // = 28;         
        checkConstant(CmcEmitter.__conditionalElements_NT, CmcJSEmitter.__conditionalElements_NT, "__conditionalElements_NT");

        // really only checking whether the number of constants has increased:
        checkConstant(CmcEmitter.nStates, 39, "nStates");

        return true;
    }

    public JSGeneratingReducer(JSSharedData sharedData)
    {
        m_sharedData = sharedData;

        m_nameMap.put(JSSharedData.JS_FRAMEWORK_NAME, JSSharedData.JS_FRAMEWORK_NAME);

        m_nameMap.put("NULL", "null");
        m_nameMap.put("_NEW", "new");
        m_nameMap.put("DELETE", "delete");
        m_nameMap.put("EMPTY_STRING", "''");
        // m_nameMap.put( JSSharedData.THIS, "this" );
        // m_nameMap.put( "TRACE", JSSharedData.ROOT_NAME + "flash.utils.trace" );
        // m_nameMap.put( "trace", JSSharedData.ROOT_NAME + "flash.utils.trace" );

        if (JSSharedData.m_useClosureLib)
        {
            m_nameMap.put("super", JSSharedData.THIS + "." + JSSharedData.CLASS_NAME + ".superClass_");
            m_nameMap.put(JSSharedData.THIS + "." + JSSharedData.CLASS_NAME + ".superClass_", JSSharedData.THIS + "." + JSSharedData.CLASS_NAME + ".superClass_");
            m_nameMap.put("goog.base", "goog.base");
            m_nameMap.put("adobe.base", "adobe.base");
            // m_nameMap.put( JSSharedData.JS_FRAMEWORK_NAME, "/** @type {object} */ " + JSSharedData.JS_FRAMEWORK_NAME);
        }
        else
        {
            m_nameMap.put("super", JSSharedData.THIS + "." + JSSharedData._SUPER);
            m_nameMap.put(JSSharedData.THIS + "." + JSSharedData._SUPER, JSSharedData.THIS + "." + JSSharedData._SUPER);
        }
        m_nameMap.put("prototype", "prototype");
        m_nameMap.put("...", "Array");
        m_nameMap.put("this", JSSharedData.JS_THIS);
        m_nameMap.put(JSSharedData.THIS, JSSharedData.THIS);
        m_nameMap.put("_CLASS", "_CLASS");

        // operators
        m_nameMap.put("new", "new");
        m_nameMap.put("delete", "delete");

        // nameToString(null) must return JSSharedData.THIS
        m_nameMap.put(null, JSSharedData.THIS);

        // special values
        m_nameMap.put("null", "null");
        m_nameMap.put("undefined", "undefined");
        m_nameMap.put("NaN", "NaN");
        m_nameMap.put("Infinity", "Infinity");

        // isNumberDataType
        m_nameMap.put("Number", "Number");
        m_nameMap.put("int", "IntClass");
        m_nameMap.put("uint", "UIntClass");

        // isPrimitveDataType
        m_nameMap.put("Boolean", "Boolean");
        m_nameMap.put("String", "String");
        m_nameMap.put("*", "*");
        m_nameMap.put("void", "void");

        // isComplexDataType
        m_nameMap.put("Object", "Object");
        m_nameMap.put("Array", "Array");
        m_nameMap.put("Date", "Date");
        m_nameMap.put("Error", "Error");
        m_nameMap.put("Function", "Function");
        m_nameMap.put("Class", "Function"); // JS's version of Class is just the the ctor Function
        m_nameMap.put("RegExp", "RegExp");
        m_nameMap.put("XML", "XML");
        // m_nameMap.put("XMLList", "XMLList");

        // isReservedDataType
        m_nameMap.put("MathGlue", "Math");
        m_nameMap.put("$", "$");
        m_nameMap.put("jQuery", "jQuery");
        m_nameMap.put("JQuery", "jQuery");
        m_nameMap.put("JSError", "Error");
        m_nameMap.put("JQEvent", "jQuery.Event");
    }

    /**
     * Active labeled control-flow region's substatements. Populated by the
     * labeledStmt Prologue, and winnowed by the labeledStmt reduction's
     * epilogue.
     */
    // private Set<IASNode> labeledNodes = new HashSet<IASNode>();

    /**
     * Check if the given AST (the root AST of a looping construct) is in the
     * set of active labeled control-flow contexts. If it is, then its
     * control-flow context is being managed by the labeledStatement production
     * that's reducing the loop's LabeledStatementNode, and the loop should use
     * that context so that the labeled context's continue label is resolved to
     * the correct top-of-loop location.
     * 
     * @param loop_node - the AST at the root of the loop.
     * @return true if loop_node is known to be the substatment of a labeled
     * statement.
     */

    /*
     * private boolean isLabeledSubstatement(IASNode loop_node) { return
     * labeledNodes.contains(loop_node); }
     */
    public Boolean inCodeGen()
    {
        return this.getBuildPhase() == Operation.GET_ABC_BYTES;
    }

    public void reset()
    {
        usedTypes.clear();
        m_functionName = "";
        m_indent = 0;
        currentScope = null;
    }

    public void setBuildPhase(Operation op)
    {
        m_buildPhase = op;
    }

    public Operation getBuildPhase()
    {
        return m_buildPhase;
    }

    public static String instructionListToString(InstructionList instructionList, Boolean initVarCodeOnly)
    {
        String str = "";
        if (instructionList != null)
        {
            List<Instruction> il = instructionList.getInstructions();
            for (int i = 0; i < il.size() && !il.get(i).isBranch(); i++)
            {
                final Instruction insn = il.get(i);
                if (insn.getOpcode() == JSSharedData.OP_JS)
                {
                    String instruction = (String)insn.getOperand(0);

                    // we are only interested in the instruction that initialize variables with a value.
                    if (!initVarCodeOnly || instruction.contains("="))
                        str += instruction;
                }
            }
        }
        return str;
    }

    /*
     * Instructions to be inserted into a function prologue; used when
     * generating initialization procedures such as constructors.
     */

    public void setAprioriinstructions(InstructionList setting)
    {
        // TODO
        if (setting != null)
            this.aPrioriInstructions = instructionListToString(setting, false);
        else
            this.aPrioriInstructions = null;
    }

    public LexicalScope getCurrentscope()
    {
        return (this.currentScope);
    }

    /**
     * setFunctionNode() is called by JSGenerator::generateInstructions() when
     * generating functions and members. setFunctionNode() sets
     * convertLocalVarsToMembers(iNode) to true if the function carries a
     * [ConvertLocalVarsToMembers] metadata tag. support for
     * [ConvertLocalVarsToMembers]
     */
    public void setFunctionNode(IASNode node)
    {
        m_functionNode = null;
        if (node instanceof FunctionNode)
            m_functionNode = (FunctionNode)node;
    }

    /**
     * getVarSnippet() either returns "var " or an empty string dependent on
     * convertLocalVarsToMembers(iNode). support for [ConvertLocalVarsToMembers]
     */
    private String getVarSnippet(IASNode iNode, String varName)
    {
        if (convertLocalVarsToMembers(iNode))
        {
            if (varName.startsWith(THIS_SNIPPET))
                return varName;
            if (varName.startsWith(JS_THIS_SNIPPET))
                return varName;
            return THIS_SNIPPET + varName;
        }
        return VAR_SNIPPET + varName;
    }

    private void registerMember(IASNode iNode, IDefinition name, IDefinition type)
    {
        final String fullName = name.getQualifiedName();
        final String _type = type.getQualifiedName();
        if (!m_sharedData.hasVarName(fullName))
            m_sharedData.registerVarType(fullName, _type);
    }

    private IDefinition getDefinitionForMember(IASNode iNode, IDefinition def, String member, ClassificationValue what)
    {
        if (what == null)
            what = ClassificationValue.MEMBERS_AND_TYPES;

        // weird bug: m_rect = m_data.rect;
        // JSGeneratingReducer.getDefinitionForMember() returns null, because m_data is of type VariableDefinition in Mandelbrot.
        // That must be a bug in Falcon...
        if (def != null && def instanceof VariableDefinition)
        {
            VariableDefinition vdef = (VariableDefinition)def;
            IDefinition resolvedType = vdef.resolveType(currentScope.getProject());
            if (resolvedType != null)
                def = getClassDefinition(currentScope.getProject(), vdef, createFullNameFromDefinition(currentScope.getProject(), resolvedType));
            else
                def = getClassDefinition(currentScope.getProject(), vdef, vdef.getTypeAsDisplayString());
            if (def == null)
                return null;
        }

        if (def == null)
            currentScope.addProblem(new JSInternalCompilerProblem(iNode, "cannot find definition for member"));

        final String _class = def.getBaseName();
        if (_class.equals("Object"))
            return def;

        // TODO: feed m_sharedData with var types for Object, Array, Date, Error, Function, RegExp, XML, and XMLList.
        if (isDataType(_class))
            return null;

        if (def != null && def instanceof MemberedDefinition)
        {
            member = removeParentheses(member);

            MemberedDefinition mdef = (MemberedDefinition)def;
            ASDefinitionFilter filter = new ASDefinitionFilter(
                    ClassificationValue.MEMBERS_AND_TYPES, SearchScopeValue.ALL_SCOPES,
                    AccessValue.ALL, def);

            // IDefinition[] memberDefs = mdef.getAllMembersByName(currentScope.getProject(), member, filter);
            // def = memberDefs[0];
            def = MemberedDefinitionUtils.getMemberByName(mdef, currentScope.getProject(), member, filter);

            // see Test::resolveSetter unit test:
            // this.instanceXetterThis.instanceXetterThis = this;
            // should be resolved to self.get_instanceXetterThis().set_instanceXetterThis(self);
            if (def == null)
            {
                if (member.startsWith(JSSharedData.GETTER_PREFIX))
                {
                    member = member.substring(JSSharedData.GETTER_PREFIX.length());
                    def = MemberedDefinitionUtils.getMemberByName(mdef, currentScope.getProject(), member, filter);
                }
                else if (member.startsWith(JSSharedData.SETTER_PREFIX))
                {
                    member = member.substring(JSSharedData.SETTER_PREFIX.length());
                    def = MemberedDefinitionUtils.getMemberByName(mdef, currentScope.getProject(), member, filter);
                }
            }

            if (def != null)
            {
                if (what == ClassificationValue.SETTERS && def instanceof IGetterDefinition)
                {
                    final IGetterDefinition getterDef = (IGetterDefinition)def;
                    filter = new ASDefinitionFilter(ClassificationValue.SETTERS, SearchScopeValue.ALL_SCOPES, AccessValue.ALL, mdef);
                    def = MemberedDefinitionUtils.getMemberByName(
                            mdef, currentScope.getProject(), member, filter);

                    // work around CMP-851
                    if (def == null)
                        def = getterDef.resolveSetter(currentScope.getProject());

                    return def;
                }
                else if (what == ClassificationValue.GETTERS && def instanceof ISetterDefinition)
                {
                    final ISetterDefinition setterDef = (ISetterDefinition)def;
                    filter = new ASDefinitionFilter(ClassificationValue.GETTERS, SearchScopeValue.ALL_SCOPES, AccessValue.ALL, mdef);
                    def = MemberedDefinitionUtils.getMemberByName(
                            mdef, currentScope.getProject(), member, filter);

                    // work around CMP-851
                    if (def == null)
                        def = setterDef.resolveGetter(currentScope.getProject());

                    return def;
                }
            }

            if (def != null)
                return def;
        }

        // If JSEmitter.needsSecondPass() returns true, JSGenerator.generate() will return null during scanning, 
        // which will result in JSCompilationUnit::handleSemanticProblemsRequest not caching any abcBytes for 
        // handleABCBytesRequest. The net result is that JSGenerator.generate() will be called again in handleABCBytesRequest. 
        // This mechanic will ensure selective two-pass compilation. 
        m_needsSecondPass = true;

        return null;
    }

    /*
     * private String getTypeForMember( IASNode iNode, String _class, String
     * member ) { if( _class.equals("Object") ) return "Object"; // TODO: feed
     * m_sharedData with var types for Object, Array, Date, Error, Function,
     * RegExp, XML, and XMLList. if( isDataType(_class) ) return ""; IDefinition
     * def = getDefinitionForName( iNode, makeName(_class) ); if( def != null )
     * { if( def instanceof MemberedDefinition ) { MemberedDefinition mdef =
     * (MemberedDefinition) def; ASDefinitionFilter filter = new
     * ASDefinitionFilter(ClassificationValue.MEMBERS_AND_TYPES,
     * SearchScopeValue.ALL_SCOPES, AccessValue.ALL, def); def =
     * mdef.getMemberByName(currentScope.getProject(), member, filter); if( def
     * != null ) { if( def instanceof GetterDefinition || def instanceof
     * SetterDefinition ) return "Function"; return def.getType(); } } }
     * if(m_sharedData.hasVarName( _class + "." + member )) return
     * m_sharedData.getVarType( _class + "." + member ); if(
     * m_sharedData.hasClass(makeName(_class)) ) return getTypeForMember(iNode,
     * getFullNameFromName(m_sharedData.getSuperClass(makeName(_class)), false),
     * member); // If JSEmitter.needsSecondPass() returns true,
     * JSGenerator.generate() will return null during scanning, // which will
     * result in JSCompilationUnit::handleSemanticProblemsRequest not caching
     * any abcBytes for // handleABCBytesRequest. The net result is that
     * JSGenerator.generate() will be called again in handleABCBytesRequest. //
     * This mechanic will ensure selective two-pass compilation.
     * m_needsSecondPass = true; return ""; } private String extractPackageName(
     * String _class ) { if( _class.lastIndexOf( '.' ) > 0 ) return
     * _class.substring( 0, _class.lastIndexOf( '.' ) ); return ""; } private
     * String extractClassName( String _class ) { if( _class.lastIndexOf( '.' )
     * > 0 ) return _class.substring( _class.lastIndexOf( '.' ) + 1 ); return
     * _class; }
     */

    private static boolean isStringWithQuotes(String s)
    {
        return s.startsWith("\"") && s.endsWith("\"");
    }

    private static boolean isLongNumberString(String s)
    {
        try
        {
            Long.parseLong(s);
        }

        catch (NumberFormatException nfe)
        {
            return false;
        }

        return true;
    }

    /**
     * Common routine used by reductions that need an empty list.
     * 
     * @param iNode - the current AST node.
     */
    public String createInstructionList(IASNode iNode)
    {
        return new String();
    }

    private static String removeQuotes(String s)
    {
        return s.replaceAll("\\\"", "");
    }

    private static String removeParentheses(String s)
    {
        while (s.lastIndexOf(")") == s.length() - 1)
        {
            if (s.indexOf("(") == 0)
                s = s.substring(1, s.lastIndexOf(')'));
            else if (s.endsWith("()"))
                return s.substring(0, s.length() - 2);
            else
                return s;
        }
        return s;
    }

    private String stripNS(String s)
    {
        if (s == null)
            return "null";

        if (s.indexOf("::") >= 0)
        {
            if (s.indexOf("{}::") >= 0)
                s = s.replaceFirst("\\{\\}::", "");
            else
            {
                s = s.replaceFirst("\\{", "");
                s = s.replaceFirst("\\}::", ".");
            }
        }
        return s;
    }

    private String stripTabs(String s)
    {
        if (s == null)
            return "null";

        while (s.length() > 0 && s.charAt(0) == '\t')
            s = s.substring(1);

        return s;
    }

    private void registerLocalVariable(LexicalScope scope, Binding b_name, Binding b_type, Binding b_returnType)
    {
        // Name name = getNameFromBinding(b_name);
        Name type = getNameFromBinding(b_type);
        if (scope == null)
            return;

        // LexicalScope.makeVariable
        scope.makeVariable(b_name, type, null); // makeLocalVariable
    }

    private void makeParameter(IASNode iNode, ParameterNode node, LexicalScope scope, Binding b_name, Binding b_type, Binding b_returnType)
    {
        // currentScope.makeParameter(#ArgumentID#);
        // currentScope.makeParameter(node.getDefinition(),b_type.getName());
        currentScope.makeParameter(getParameterContent(node), b_type.getName());
        registerLocalVariable(scope, b_name, b_type, b_returnType);
    }

    private void makeParameter(IASNode iNode, ParameterNode node, LexicalScope scope, Name name, Binding b_type, Binding b_returnType)
    {
        makeParameter(iNode, node, scope, currentScope.getBinding(node.getDefinition()), b_type, b_returnType);
    }

    private void makeVariable(BaseVariableNode var_node, LexicalScope scope, Name b_name, Binding b_type, Binding b_returnType)
    {
        // Name name = getNameFromBinding( b_name );
        Name type = null;
        // Name returnType = null;

        if (b_type != null)
            type = getNameFromBinding(b_type);

        // if( b_returnType != null )
        //    returnType = getNameFromBinding( b_returnType );

        if (scope == null)
            return;

        // LexicalScope.makeVariable
        Binding var = currentScope.resolveName((IdentifierNode)var_node.getNameExpressionNode());
        scope.makeVariable(var, type, null);

        this.registerMember(var_node, var_node.getDefinition(), b_returnType.getDefinition());

        /*
         * Name _name = new Name( new Nsset( new
         * Namespace(CONSTANT_PackageNs,createFullClassName(false))),
         * getBasenameFromName(name) ); if( returnType != null ) { String _type
         * = returnType.toString(); if( _type.startsWith("{}::") ) { _type =
         * _type.replaceFirst( "\\{\\}::", "" ); if(
         * getCurrentPackageName().length() > 0 && !isDataType(_type) ) _type =
         * getCurrentPackageName() + "." + _type; } this.registerMember(iNode,
         * _name.toString(), _type); } else this.registerMember(iNode,
         * _name.toString(), "*");
         */
    }

    public static Name makeName(String name)
    {
        String nameSpace = "";
        String baseName = name;

        if (!isStringWithQuotes(baseName) && !baseName.contains(" ") && !baseName.endsWith("()") && !baseName.endsWith("]"))
        {
            baseName = removeParentheses(baseName);
            if (name.indexOf(".") > 0)
            {
                nameSpace = baseName.substring(0, baseName.lastIndexOf("."));
                baseName = baseName.substring(baseName.lastIndexOf(".") + 1);
            }
        }
        Name _name = new Name(new Nsset(new Namespace(CONSTANT_PackageNs, nameSpace)), baseName);
        return _name;
    }

    private Binding makeBinding(IASNode node, String name)
    {
        IDefinition def = null;

        if (name.startsWith("\"") && name.endsWith("\""))
        {
            return new Binding(node, makeName(name), null);
        }
        else if (!name.startsWith(JSSharedData.THIS + "."))
        {
            if (name.contains("."))
            {
                for (String part : name.split("\\."))
                {
                    if (def == null)
                        def = getDefinitionForName(node, makeName(part));
                    else
                        def = getDefinitionForMember(node, def, part, ClassificationValue.MEMBERS_AND_TYPES);
                }
            }
            else
            {
                def = this.getDefinitionForName(node, makeName(removeQuotes(name)));
            }
        }

        return new Binding(node, makeName(name), def);
    }

    private IDefinition getDefinitionForInstance(IASNode iNode, String instanceName, ClassificationValue what)
    {
        if (what == null)
            what = ClassificationValue.MEMBERS_AND_TYPES;

        if (isStringWithQuotes(instanceName))
            return getDefinitionForName(iNode, makeName("String"));

        if (instanceName.equals(JSSharedData.THIS))
        {
            if (m_currentClassDefinition != null)
                return m_currentClassDefinition;
            return getDefinitionForName(iNode, makeName(createFullClassName(false)));
        }

        if (instanceName.equals(JSSharedData.JS_THIS))
        {
            if (isAnonymousFunction(getMethodName()))
                return m_functionNode.getDefinition();
            return m_currentClassDefinition;
        }

        if (isDataType(instanceName))
            return getDefinitionForName(iNode, makeName(instanceName));

        if (instanceName.equals("[]"))
            return getDefinitionForName(iNode, makeName("Array"));

        if (instanceName.equals("{}"))
            return getDefinitionForName(iNode, makeName("Object"));

        Name typeOfInstance = null;
        if (instanceName.equals(JSSharedData.THIS + "." + JSSharedData._SUPER))
        {
            typeOfInstance = makeName(createFullClassName(false));
            if (m_sharedData.hasClass(typeOfInstance))
                typeOfInstance = m_sharedData.getSuperClass(typeOfInstance);
            return getDefinitionForName(iNode, (typeOfInstance));
        }

        instanceName = removeRootName(instanceName);

        // remove "/* unknown */" marker
        if (instanceName.contains("unknown"))
            instanceName = instanceName.replaceFirst("\\/\\* unknown \\*\\/ ", "");

        instanceName = removeParentheses(instanceName);

        // trim function name 
        if (instanceName.contains("("))
        {
            if (!instanceName.endsWith(")"))
                return null;

            final String tmp = instanceName.substring(0, instanceName.indexOf('('));
            // IDefinition def = getDefinitionForName( iNode, makeName(tmp) );
            IDefinition def = getDefinitionForInstance(iNode, tmp, what);
            if (def != null && def instanceof FunctionDefinition)
            {
                FunctionDefinition fdef = (FunctionDefinition)def;
                def = getDefinitionForName(iNode, makeName(fdef.getReturnTypeAsDisplayString()));
                return def;
            }
        }

        // trim index access literals
        if (instanceName.contains("["))
            instanceName = instanceName.substring(0, instanceName.indexOf('['));

        // trim trailing comments
        if (instanceName.contains("/*") && instanceName.contains("*/"))
            instanceName = instanceName.substring(0, instanceName.indexOf("/*"));

        if (!instanceName.contains("."))
        {
            IDefinition def = getDefinitionForName(iNode, makeName(instanceName));
            /*
             * NON-SENSE, but kind of cool: if( def == null &&
             * (instanceName.startsWith(JSSharedData.GETTER_PREFIX) ||
             * instanceName.startsWith(JSSharedData.SETTER_PREFIX)) ) { def =
             * getDefinitionForName( __p, makeName(instanceName.substring(4)) );
             * if( def != null && def instanceof GetterDefinition ) return
             * "Function"; }
             */

            if (def != null)
            {
                if (def instanceof ClassDefinition)
                {
                    return def;

                    /*
                     * final ClassDefinition classDef = (ClassDefinition)def;
                     * IDefinition parent = def.getParent(); return
                     * JSSharedData.ROOT_NAME +
                     * m_sharedData.getPackageNameForClassName
                     * (classDef.getName(), getBaseClassAsStrign(classDef)) +
                     * "." + def.getName();
                     */
                }
                else if (def instanceof VariableDefinition)
                {
                    final VariableDefinition variable = (VariableDefinition)def;
                    IDefinition typeDef = variable.resolveType(currentScope.getProject());
                    if (typeDef != null)
                        return typeDef;
                    return def;
                }
                else if (def.getTypeAsDisplayString() != null && !def.getTypeAsDisplayString().isEmpty())
                {
                    IDefinition typeDef = getDefinitionForName(iNode, makeName(def.getTypeAsDisplayString()));
                    if (typeDef != null)
                        return typeDef;
                    return def;
                    /*
                     * if( def != null ) { IDefinition parent = def.getParent();
                     * if( parent == null ) return def.getName(); return
                     * parent.getName() + "." + def.getName(); }
                     */
                }
                else
                {
                    return def;
                }
            }
        }
        else
        {
            // String type = m_sharedData.getVarType(instanceName);
            // if( type != null )
            //    return getDefinitionForName( iNode, makeName(type));

            IDefinition obj = null;
            String name = instanceName;
            if (name.startsWith(JSSharedData.THIS + "."))
            {
                name = instanceName.replaceFirst(JSSharedData.THIS + ".", "");
                obj = getDefinitionForName(iNode, makeName(createFullClassName(false)));
            }
            String prop = "";
            IDefinition next = null;
            while (name.length() > 0)
            {
                final int nextDot = name.indexOf(".");
                if (nextDot < 0)
                {
                    prop = name;
                    name = "";
                }
                else
                {
                    prop = name.substring(0, nextDot);
                    name = name.substring(nextDot + 1);
                }

                // if( isDataType(next) )
                //    return next;

                if (obj != null)
                {
                    if (obj instanceof GetterDefinition)
                    {
                        next = getDefinitionForName(iNode, makeName(((GetterDefinition)obj).getReturnTypeAsDisplayString()));
                        if (next == null)
                        {
                            // throw new Error( "next is null" );
                            next = getDefinitionForName(iNode, makeName("Object"));
                        }
                        obj = next;
                    }

                    if (name.isEmpty())
                        next = getDefinitionForMember(iNode, obj, prop, what);
                    else
                        next = getDefinitionForMember(iNode, obj, prop, ClassificationValue.MEMBERS_AND_TYPES);

                    if (next == null)
                    {
                        // i.e. com.wizhelp.fzlib.ZStream.next_in
                        next = null;
                    }

                    if (next == null || isDataType(next.getBaseName()))
                        return next;
                }
                else
                {
                    // next = getDefinitionForInstance(iNode, prop, what );     
                    next = getDefinitionForInstance(iNode, prop, what);
                    // if(next == null)
                    //    return null;  
                }

                obj = next;
            }
            return obj;
        }

        return null;
    }

    private String getTypeOfInstance(IASNode iNode, String instanceName)
    {
        IDefinition def = getDefinitionForInstance(iNode, instanceName, ClassificationValue.MEMBERS_AND_TYPES);
        if (def != null)
        {
            if (def instanceof GetterDefinition || def instanceof SetterDefinition)
                return "Function";
            if (def.getTypeAsDisplayString() != null && !def.getTypeAsDisplayString().isEmpty())
                return def.getTypeAsDisplayString();
            return def.getBaseName();
        }

        return "";
    }

    /**
     * createFullNameFromDefinition
     */

    public static String createFullNameFromDefinition(ICompilerProject project, IDefinition def)
    {
        String name = "";

        if (def instanceof ClassDefinition)
        {
            String packageName = def.getPackageName();
            if (!packageName.isEmpty())
                name = packageName + ".";
            name += def.getBaseName();
        }
        else
        {
            final String externName = JSSharedData.instance.getExternName(def.getQualifiedName());
            if (externName != null)
            {
                return externName;
            }

            String fullName = "";
            List<String> parts = new ArrayList<String>();
            IDefinition parent = def.getParent();
            if (parent == null)
                name = def.getQualifiedName();
            else
            {
                while (parent != null)
                {
                    if (parent.getBaseName() != null && !parent.getBaseName().isEmpty())
                        parts.add(0, parent.getBaseName());

                    if (!fullName.isEmpty())
                        fullName += ".";
                    fullName += parent.getBaseName();

                    parent = parent.getParent();
                }

                for (String part : parts)
                {
                    if (name.isEmpty() && !part.equals(JSSharedData.JS_FRAMEWORK_NAME))
                        name = JSSharedData.ROOT_NAME;

                    name += part + ".";
                }
                name += def.getBaseName();
            }
        }
        return name;
    }

    private IDefinition getClassDefinition(ICompilerProject project, IDefinition definitionContext, String className)
    {
        // TODO: convert to ICompilerProblem?
        if (!className.contains(".") && !isDataType(className) && m_sharedData.getDefinition(className) == null)
        {
            final IASScope scope = definitionContext != null ? definitionContext.getContainingScope() : project.getScope();
            final IDefinition def = getDefinitionForNameByScope(project, scope, makeName(className));
            if (!isExternalClass(project, def))
            {
                if (def != null && !def.getQualifiedName().equals(className))
                    m_sharedData.stdout("Warning: Suspicious class name: did you mean '" + def.getQualifiedName() + "' instead of '" + className + "'?");
                else
                    m_sharedData.stdout("Warning: Suspicious class name: " + className);
            }
        }

        ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES, SearchScopeValue.ALL_SCOPES, AccessValue.ALL, definitionContext);
        // final Multiname multiname = Multiname.crackDottedQName(currentScope.getProject(), className);
        IDefinition classDef = ASScopeUtils.findDefinitionByName(project.getScope(), project, className, filter);
        // if( classDef == null )
        //    classDef = definitionContext.getContainingScope().findDefinitionByName(className, filter, currentScope.getDefinitionCache() );

        return classDef;
    }

    /*
     * private IDefinition getBaseClass(ClassDefinition classDef) {
     * ClassDefinition superclassDefinition = null; String name =
     * classDef.getBaseClass(); if( name != null ) {
     * Collection<ICompilerProblem> problems = currentScope.getProblems();
     * superclassDefinition = classDef.resolveBaseClass(
     * currentScope.getProject(), new RecursionGuard(), problems); if (
     * superclassDefinition == null ) { // Repair by making the class extend
     * Object; the CG's caller // is responsible for picking up the problem and
     * treating the // result of this code generation with appropriate caution.
     * superclassDefinition =
     * (ClassDefinition)currentScope.getProject().getBuiltinType
     * (IASLanguageConstants.BuiltinType.OBJECT); } } return
     * superclassDefinition; } private String
     * getBaseClassAsString(ClassDefinition classDef) { final IDefinition
     * superclassName = getBaseClass(classDef); if( superclassName == null )
     * return null; String name = superclassName.getQualifiedName(); return
     * name; }
     */

    private Boolean hasInterface(Iterator<IInterfaceDefinition> interfaceIterator, List<String> interfaceNames)
    {
        while (interfaceIterator.hasNext())
        {
            final IInterfaceDefinition interfaceDef = interfaceIterator.next();
            final String extendedInterface = interfaceDef.getQualifiedName();
            for (String interfaceName : interfaceNames)
            {
                if (extendedInterface.equals(interfaceName) || extendedInterface.equals("public." + interfaceName))
                    return true;
            }
        }
        return false;
    }

    public Boolean hasInterface(ICompilerProject project, IDefinition def, List<String> interfaceNames)
    {
        if (def != null)
        {
            if (def instanceof IClassDefinition)
            {
                IClassDefinition classDef = (IClassDefinition)def;
                final Iterator<IClassDefinition> superClassIterator = classDef.classIterator(currentScope.getProject(), true);
                while (superClassIterator.hasNext())
                {
                    classDef = superClassIterator.next();

                    // Workaround for Falcon bug
                    if (isDataType(classDef))
                        return false;

                    final Iterator<IInterfaceDefinition> interfaceIterator = classDef.interfaceIterator(currentScope.getProject());
                    if (hasInterface(interfaceIterator, interfaceNames))
                        return true;
                }
            }
            else if (def instanceof IInterfaceDefinition)
            {
                // Workaround for Falcon bug
                if (isDataType(def))
                    return false;

                final IInterfaceDefinition interfaceDef = (IInterfaceDefinition)def;
                final Iterator<IInterfaceDefinition> interfaceIterator = interfaceDef.interfaceIterator(currentScope.getProject(), true);
                if (hasInterface(interfaceIterator, interfaceNames))
                    return true;
            }

        }
        return false;
    }

    /**
     * isExternalClass
     */

    public Boolean isExternalClass(ICompilerProject project, IDefinition def)
    {
        if (def == null)
            return false;

        // we will eventually switch over to using [Extern] 
        // instead of deriving from IExtern.
        final IMetaTag extern = def.getMetaTagByName(JSSharedData.EXTERN_METATAG);
        if (extern != null)
            return true;

        final List<String> interfaceNames = new ArrayList<String>();
        interfaceNames.add("browser" + "." + JSSharedData.EXTERN_INTERFACE_NAME);
        interfaceNames.add("browser" + "." + JSSharedData.FRAMEWORK_INTERFACE_NAME);
        return hasInterface(project, def, interfaceNames);
    }

    /**
     * isDataClass
     */

    public Boolean isDataClass(ICompilerProject project, IDefinition def)
    {
        if (def == null)
            return false;

        final IMetaTag extern = def.getMetaTagByName(JSSharedData.DATACLASS_METATAG);
        if (extern != null)
            return true;

        def = getSuperClass(project, def);
        if (def != null && def != currentScope.getProject().getBuiltinType(BuiltinType.OBJECT))
            return isDataClass(project, def);

        return false;
    }

    /**
     * isSymbolClass
     */

    public Boolean isSymbolClass(ICompilerProject project, IDefinition def)
    {
        final List<String> interfaceNames = new ArrayList<String>();
        interfaceNames.add(JSSharedData.SYMBOL_INTERFACE_NAME);
        return hasInterface(project, def, interfaceNames);
    }

    /**
     * makeSetterCall
     */

    private String makeSetterCall(IASNode iNode, IDefinition def,
              String instanceName, Binding member, String memberName,
              String op, String value, Boolean isPostOperator)
    {
        final String selfParam = JSSharedData.m_useSelfParameter ? instanceName + ", " : "";
        String name = instanceName;
        if (def != null)
        {
            final Boolean isStatic = m_convertToStatic || def.isStatic();
            if (isStatic)
            {
                name = createFullNameFromDefinition(currentScope.getProject(), def);
                name = name.substring(0, name.lastIndexOf("."));
            }
        }

        String getterName = "";
        String result = "";
        // if (def == null || !(def instanceof SetterDefinition))
        if (false) // AJH disable this clause for now
        {
            String self = JSSharedData.THIS;
            final String currentFullClassName = getCurrentFullClassName();
            if (currentFullClassName == null || currentFullClassName.isEmpty())
                self = "null";

            // could be a member.

            /*
             * !convertLocalVarsToMembers(iNode) fixes bug related to injecting
             * adobe.setProperty and "+=" operator. In order to reproduce the
             * bug you need: 1. a dynamic class 2. a method marked with
             * ConvertLocalVarsToMembers 3. two local variables, i.e. A and B.
             * 4. a plus equal assignment in an expression using A and B, i.e.:
             * if ((A += B) == 15) The bug was: if( (self.A +=
             * adobe.getProperty(self, self, "A") + self.B)) == 15)) The correct
             * result is: if(((adobe.setProperty(self, self, "A",
             * adobe.getProperty(self, self, "A") + self.B)) == 15))
             */
            if (def != null &&
                  def instanceof VariableDefinition &&
                  (instanceName.equals(JSSharedData.THIS) || instanceName.equals(JSSharedData.JS_THIS)) &&
                  !convertLocalVarsToMembers(iNode))
            {
                result = instanceName + "." + removeQuotes(memberName) + " " + op + " (";
            }
            else
            {
                registerAccessedPropertyName(iNode, memberName);
                result = JSSharedData.JS_FRAMEWORK_NAME + ".setProperty(" + self + ", " + instanceName + ", " + memberName + ", ";
            }
        }
        else
            result = name + "." + JSSharedData.SETTER_PREFIX + removeQuotes(memberName) + "(";

        if (op.length() >= 2 && op.endsWith("="))
        {
            getterName = resolveGetterName(iNode, instanceName, member, false);

            // This very simple test case messed up our set/getProperty injections:
            // const array : Object = {item:123};
            // const num : uint = array.item++;
            if (isPostOperator && value.equals("1") && getterName.startsWith(JSSharedData.JS_FRAMEWORK_NAME + ".getProperty"))
            {
                if (op.equals("+="))
                    result = getterName.replaceFirst(JSSharedData.JS_FRAMEWORK_NAME + ".getProperty", JSSharedData.JS_FRAMEWORK_NAME + ".postIncProperty");
                else if (op.equals("-="))
                    result = getterName.replaceFirst(JSSharedData.JS_FRAMEWORK_NAME + ".getProperty", JSSharedData.JS_FRAMEWORK_NAME + ".postDecProperty");
                else
                    result += getterName + " " + op.substring(0, op.lastIndexOf("=")) + " " + selfParam + value;
            }
            else
            {
                result += getterName + " " + op.substring(0, op.lastIndexOf("=")) + " " + selfParam + value + ")";
            }
        }
        else
        {
            result += selfParam + value + ")";
        }

        if (!isPostOperator && !getterName.isEmpty())
        {
            // adobe.setProperty now returns the value.
            if (result.startsWith(JSSharedData.JS_FRAMEWORK_NAME + ".setProperty"))
                result += "";
            else
                result = "(" + result + ", " + getterName + ")";
        }

        return result;
    }

    private String createStringFromBinding(IASNode iNode, Binding b)
    {
        final String baseName = getBasenameFromBinding(b);
        final IDefinition def = b.getDefinition();
        if (def != null && def.getBaseName().equals(baseName))
        {
            IDefinition parent = def.getParent();
            if (parent == null || !(parent instanceof ClassDefinition))
            {
                if (def.isStatic())
                    return createFullNameFromDefinition(currentScope.getProject(), def);

                if (def instanceof VariableDefinition)
                    return def.getBaseName();

                return def.getQualifiedName();
            }
        }

        // if( def != null && !(def instanceof VariableDefinition) )
        final Name name = getNameFromBinding(b);
        {
            if (name.getQualifiers() != null && name.getQualifiers().length() == 1)
            {
            	final Namespace sq = name.getSingleQualifier();
                final String packageName = sq.getName();
                if (packageName.isEmpty())
                    return baseName;

                // AJH added to prevent private vars from having namespaces
                if (sq.getKind() == ABCConstants.CONSTANT_PrivateNs)
                	return baseName;
                
                final INamespaceDefinition ns = m_sharedData.getNamespace(packageName);
                if (ns != null)
                    return baseName; //  + " /* NS=" + ns.getName() + " */";

                if (packageName.startsWith("http:"))
                    return baseName; // + " /* NS=" + packageName + " */";

                return packageName + "." + name.getBaseName();
            }
        }
        return baseName; //  getBasenameFromBinding(b);
    }

    /**
     * getSetterDefinition
     * 
     * @param iNode
     * @param stem
     * @param member
     * @return
     */
    private IDefinition getSetterDefinition(IASNode iNode, String stem, Binding member)
    {
        // TODO: duplicates / very similar to code in resolveGetterName()
        IDefinition def = member.getDefinition();

        if (def == null || !(def instanceof ISetterDefinition))
        {
            final String baseName = removeQuotes(getBasenameFromBinding(member));
            if (def instanceof IGetterDefinition)
            {
                // def = getDefinitionForInstance(iNode, createFullNameFromDefinition(currentScope.project,def), ClassificationValue.SETTERS );
                def = getDefinitionForMember(iNode, def.getParent(), baseName, ClassificationValue.SETTERS);
            }

            if (def == null)
            {
                String packageName = "";
                if (stem != null && !stem.isEmpty())
                    packageName += removeRootName(stem) + ".";

                final String methodName = packageName + baseName;
                final String typeOfInstance = getTypeOfInstance(iNode, methodName);
                if (typeOfInstance != null && typeOfInstance.equals("Function"))
                {
                    def = getDefinitionForInstance(iNode, methodName, ClassificationValue.SETTERS);
                }
            }
        }

        if (def instanceof ISetterDefinition && !isDataType(def.getParent()) && !isExternalClass(currentScope.getProject(), def.getParent()) && !isVectorLength((ISetterDefinition)def))
        {
            return def;
        }

        return null;
    }

    private boolean isAlphaNumString(String s)
    {
        for (int i = 0; i < s.length(); ++i)
        {
            final int c = s.codePointAt(i);
            final boolean isAlphaNumChar = (c >= 'a' && c <= 'z') ||
                                             (c >= '0' && c <= '9') ||
                                             (c >= 'A' && c <= 'Z') ||
                                             (c == '$') ||
                                             (c == '_');
            if (!isAlphaNumChar)
                return false;
        }

        return true;
    }

    /*
     * Fixes bug that ChrisB pointed out: const numAsUint : uint = num; needs to
     * be emitted as: var numAsUint = adobe.dynamicCastAs(num, UIntClass); Same
     * with uint/int members and initializers. The fixes went into
     * reduce_variableDecl() and resolveSetterName(). They now call
     * addDynamicCastToValue().
     */
    private String addDynamicCastToValue(IASNode iNode, String value)
    {
        // m_numMemberAsUint = num; needs to be converted.
        // Tamarin tests trigger NPE in JSGeneratingReducer.addDynamicCastToValue()
        if (getCurrentClassName() != null &&
              !getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME) &&
              value != null &&
              !value.startsWith("adobe.dynamicCastAs("))
        {
            final IDefinition lhs = getDefinitionForNode(iNode, false);
            if (lhs != null)
            {
                final IDefinition rhs = getDefinitionForNode(iNode, true);
                if (lhs == currentScope.getProject().getBuiltinType(BuiltinType.INT) &&
                      rhs != currentScope.getProject().getBuiltinType(BuiltinType.INT))
                {
                    // http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/int.html
                    // AS3, int: The range of values represented by the int class is -2,147,483,648 (-2^31) to 2,147,483,647 (2^31-1).
                    // http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Integer.html
                    // Java, Integer: MIN_VALUE=-2^31, MAX_VALUE=2^31-1
                    Boolean needsDynamicCast = false;
                    try
                    {
                        @SuppressWarnings("unused")
                        Integer num;
                        if (value.startsWith("0x"))
                            num = Integer.parseInt(value.substring(2), 16);
                        else
                            num = Integer.parseInt(value);
                    }
                    catch (Exception e)
                    {
                        if (!value.equals("NaN"))
                            needsDynamicCast = true;
                    }
                    if (needsDynamicCast)
                        value = "adobe.dynamicCastAs(" + value + ", IntClass)"; // "~~(" + value + " / * Coerce to int * /)";
                }
                else if (lhs == currentScope.getProject().getBuiltinType(BuiltinType.UINT) &&
                           rhs != currentScope.getProject().getBuiltinType(BuiltinType.UINT))
                {
                    // http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/uint.html
                    // AS3, uint: The range of values represented by the uint class is 0 to 4,294,967,295 (2^32-1)
                    // http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Long.html
                    // Java, Long: MIN_VALUE=-2^63, MAX_VALUE=2^63-1
                    Boolean needsDynamicCast = false;
                    try
                    {
                        Long num;
                        if (value.startsWith("0x"))
                            num = Long.parseLong(value.substring(2), 16);
                        else
                            num = Long.parseLong(value);
                        if (num < 0 || num > 4294967295L)
                            needsDynamicCast = true;
                    }
                    catch (Exception e)
                    {
                        if (!value.equals("NaN"))
                            needsDynamicCast = true;
                    }
                    if (needsDynamicCast)
                        value = "adobe.dynamicCastAs(" + value + ", UIntClass)"; // "(((~~(" + value + ")) < 0 ) ? (4294967296+(~~(" + value + "))) : (~~(" + value +")) / * Coerce to uint * /)";
                }
            }
        }
        return value;
    }

    /**
     * resolveSetterName
     * 
     * @param iNode
     * @param instanceName
     * @param member
     * @param op
     * @param value
     * @param useBrackets
     * @param isPostOperator
     * @return
     */
    private String resolveSetterName(IASNode iNode, String instanceName, Binding member, String op, String value,
                                          Boolean useBrackets, Boolean isPostOperator)
    {
        // constant propagation for initialized members
        m_hasSideEffects = true;

        IDefinition def = member.getDefinition();
        final String baseNameWithQuotes = getBasenameFromBinding(member);
        final String baseName = removeQuotes(baseNameWithQuotes);

        // In instance.foo, foo is a member of instance's type
        // In instance["foo"], foo is a member of instance's type
        // In instance[foo], foo is an expression evaluating to a string, and the *string* is a member of instance's type
        // NOTE: see similar fix for getters in resolveGetterName()
        // TODO: warn if !baseNameIsMember and instanceName isn't an Array or Vector -- runtime name lookup can't
        //       handle get_/set_ prefixing properly. (Also need such a warning for "in" keyword and "for each" loops).
        boolean baseNameIsMember = !useBrackets || !baseName.equals(baseNameWithQuotes);

        // see reduce_superAccess
        if ( /* instanceName.equals("goog") && */(baseName.startsWith("goog.base(") || baseName.startsWith("adobe.base(")))
        {
            assert !useBrackets;
            return convertSuperAccessToSetter(baseNameWithQuotes, value);
        }

        /*
         * BUG: Found via fl.motion.Animator.setTimeClassic() WRONG:
         * self._target.get_transform().matrix = targetMatrix; CORRECT:
         * self._target.get_transform().set_matrix( targetMatrix );
         */

        // m_numMemberAsUint = num; needs to
        value = addDynamicCastToValue(iNode, value);

        if (baseNameIsMember)
        {
            final IDefinition setterDef = getSetterDefinition(iNode, instanceName, member);
            if (setterDef != null)
            {
                final String setterCall = makeSetterCall(iNode, setterDef, instanceName, member, baseName, op, value, isPostOperator);
                return addStaticInitCall(setterDef, setterCall, false);
            }

            // e15_6_3_1_4, Trying to verify that Boolean.prototype is read-only.
            if (baseName.equals("prototype") && isDataType(instanceName))
            {
                return "throw new Error(\"ReferenceError: Error #1074\") /* FOR: " + instanceName + "." + baseName + " " + op + " " + value + " */";
            }
        }

        String ret = "";

        // optimization for post/pre and inc/dec
        String preOpAndValue = "";
        String postOpAndValue = " " + op + " " + value;
        if (value.equals("1"))
        {
            if (op.equals("+="))
            {
                if (isPostOperator)
                    postOpAndValue = "++";
                else
                {
                    preOpAndValue = "++";
                    postOpAndValue = "";
                }
            }
            else if (op.equals("-="))
            {
                if (isPostOperator)
                    postOpAndValue = "--";
                else
                {
                    preOpAndValue = "--";
                    postOpAndValue = "";
                }
            }
        }

        String memberName = createStringFromBinding(iNode, member);

        if (instanceName.isEmpty())
            ret = preOpAndValue + memberName + postOpAndValue;
        else
        {
            if (needsRuntimeLookup(iNode, instanceName, member))
            {
                String quote = "";
                if (baseNameIsMember && isAlphaNumString(baseNameWithQuotes) && !memberName.startsWith("\""))
                    quote = "\"";
                else
                {
                    // this will ensure that converted members are being resolved correctly.
                    final String mappedName = m_nameMap.get(member);
                    if (mappedName != null && !mappedName.isEmpty())
                        memberName = mappedName;
                }
                ret = makeSetterCall(iNode, def, instanceName, member, quote + memberName + quote, op, value, isPostOperator);

                /*
                 * String self = JSSharedData.THIS; final String
                 * currentFullClassName = getCurrentFullClassName(); if(
                 * currentFullClassName == null ||
                 * currentFullClassName.isEmpty() ) self = "null";
                 * registerAccessedPropertyName(iNode,memberName); ret =
                 * JSSharedData.JS_FRAMEWORK_NAME + ".setProperty(" + self +
                 * ", " + preOpAndValue + instanceName + ", " + quote +
                 * memberName + quote + ", " + value + ")";
                 */
            }
            else if (useBrackets)
                ret = preOpAndValue + instanceName + "[" + memberName + "]" + postOpAndValue;
            else
                ret = preOpAndValue + instanceName + "." + memberName + postOpAndValue;
        }

        return addStaticInitCall(def, ret, false);
    }

    /**
     * Convert a super getter invocation to a super setter invocation, now that
     * we know we're in an assignment context.
     */
    private String convertSuperAccessToSetter(String baseNameWithQuotes, String value)
    {
        // Method name is passed to goog.base() as a string, so don't removeQuotes() in this case (the code
        // came from reduce_superAccess(), which is guaranteed to not emit any quotes but the needed ones...)
        String superSetter = baseNameWithQuotes;

        // Convert get_ prefix created by reduce_superAccess() to set_ prefix
        // TODO: this string manipulation is inherently fragile... it might even break in some edge cases
        if (baseNameWithQuotes.startsWith("adobe.base("))
        {
            int firstGet = baseNameWithQuotes.indexOf("get_");
            if (firstGet != -1)
            {
                int secondGet = baseNameWithQuotes.indexOf("get_", firstGet + 1);
                int getToReplace = (secondGet != -1) ? secondGet : firstGet;

                superSetter = superSetter.substring(0, getToReplace) + "set_" + superSetter.substring(getToReplace + 4);
            }
        }
        else
        // startsWith("goog.base(")
        {
            if (superSetter.contains("\"get_"))
                superSetter = superSetter.replaceFirst("\"get_", "\"set_");
        }

        // The base() call now needs an extra argument: the RHS value to pass to the setter
        superSetter = superSetter.replaceFirst("\\)", ", " + value + "\\)");

        return superSetter;
    }

    /**
     * makeGetterCall
     */
    private static String makeGetterCall(ICompilerProject project, IDefinition def, String instanceName, String memberName)
    {
        final String selfParam = JSSharedData.m_useSelfParameter ? instanceName + ", " : "";
        final Boolean isStatic = m_convertToStatic || def.isStatic();
        String name = instanceName;
        if (isStatic)
        {
            name = createFullNameFromDefinition(project, def);
            name = name.substring(0, name.lastIndexOf("."));
        }
        return name + "." + JSSharedData.GETTER_PREFIX + removeQuotes(memberName) + "(" + selfParam + ")";
    }

    /**
     * needsRuntimeLookup
     */

    private Boolean needsRuntimeLookup(IASNode iNode, String instanceName, Binding member)
    {
        if (isCurrentFramework())
            return false;

        // final boolean instanceIsFramework =  instanceName.startsWith(JSSharedData.JS_FRAMEWORK_NAME + "."); 
        // if( instanceIsFramework )
        //    return false;

        if (instanceName.equals(JSSharedData.JS_THIS) && isAnonymousFunction(getMethodName()))
            return false;

        // Object["getOwnPropertyDescriptor"];
        if (isDataType(instanceName))
            return false;

        /*
         * Name x = m_currentClassDefinition.getMName(currentScope.project);
         * Nsset nsset = x.getQualifiers(); nsset.add(); x = new Name(nsset,
         * instanceName); IDefinition instanceDef = getDefinitionForName(iNode,
         * x );
         */
        IDefinition instanceDef = getDefinitionForName(iNode, makeName(instanceName));

        if (instanceDef == null &&
              instanceName.equals(JSSharedData.THIS) &&
              m_currentClassDefinition != null &&
              !isAnonymousFunction(getMethodName()))
        {
            instanceDef = m_currentClassDefinition;
        }

        if (instanceDef == null)
            instanceDef = getDefinitionForInstance(iNode, instanceName, ClassificationValue.MEMBERS_AND_TYPES);

        if (instanceDef != null)
        {
            if (instanceDef instanceof VariableDefinition)
            {
                final VariableDefinition varDef = (VariableDefinition)instanceDef;
                final IReference typeRef = varDef.getTypeReference();
                if (typeRef == null)
                    return true;

                final ASScope scope = (ASScope)getScopeFromNode(iNode);
                instanceDef = typeRef.resolve(currentScope.getProject(), scope, DependencyType.INHERITANCE, false);
                if (instanceDef == null)
                    instanceDef = getDefinitionForInstance(iNode, typeRef.getName(), ClassificationValue.CLASSES_AND_INTERFACES);

                // Vectors are an exception
                if (instanceDef != null && instanceDef instanceof AppliedVectorDefinition)
                    return false;
            }
            else if (instanceDef instanceof IFunctionDefinition)
            {
                final IFunctionDefinition functionDef = (IFunctionDefinition)instanceDef;
                instanceDef = functionDef.resolveReturnType(currentScope.getProject());
                if (instanceDef == null)
                    return true;
            }

            // Once you stick something into an Array it becomes typeless:
            // const arr : Array = [obj];
            // obj[0].a = 1;
            // unless you have a Vector!
            if (instanceName.endsWith("]"))
                return true;

            // Objects and Arrays are tricky.
            // For example, in this case we really do want the lookup:
            // const obj : Object = new MyClass();
            // obj.a = 1;
            //if (instanceDef == currentScope.getProject().getBuiltinType(BuiltinType.OBJECT))
            //    return true;

            // Same with "*"
            // For example, in this case we really do want the lookup:
            // const obj : * = new MyClass();
            // obj.a = 1;
            //if (instanceDef == currentScope.getProject().getBuiltinType(BuiltinType.ANY_TYPE))
            //    return true;

            if (instanceDef instanceof IClassDefinition || instanceDef instanceof IInterfaceDefinition)
            {
                final String baseName = getBasenameFromBinding(member);

                if (isDataType(instanceDef))
                    return false;

                if (isExternalClass(currentScope.getProject(), instanceDef))
                    return false;

                if (isDataClass(currentScope.getProject(), instanceDef))
                    return false;

                // workaround for Falcon bug.
                // We never find prototype members, but they are always provided through Object.
                if (baseName != null && baseName.equals("prototype"))
                    return false;

                if (instanceDef.isDynamic())
                {
                    instanceDef = getDefinitionForMember(iNode, instanceDef, baseName, ClassificationValue.MEMBERS_AND_TYPES);
                    if (instanceDef != null)
                        return false;
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private String addStaticInitCall(IDefinition def, String code, Boolean provideReturnValue)
    {
        if (def != null)
        {
            // support for class inits.
            // If this is a static function and the owner class has a class init then call __static_init()
            final IDefinition fdef = def;
            if (fdef.getParent() != null && fdef.getParent() instanceof ClassDefinition)
            {
                // Static initializer is generated but not called for classes without explicit constructor
                // http://watsonexp.corp.adobe.com/#bug=2984348
                final ClassDefinition cdef = (ClassDefinition)fdef.getParent();
                if (fdef.isStatic() || cdef.getConstructor() == null)
                {
                    final String fullName = JSGeneratingReducer.createFullNameFromDefinition(currentScope.getProject(), cdef);
                    if (!fullName.equals(JSSharedData.JS_FRAMEWORK_NAME))
                    {
                        if (m_sharedData.hasClassInit(fullName))
                        {
                            final Boolean sameClass = fdef.isStatic() && cdef == m_currentClassDefinition /*
                                                                                                           * &&
                                                                                                           * cdef
                                                                                                           * .
                                                                                                           * getConstructor
                                                                                                           * (
                                                                                                           * )
                                                                                                           * !=
                                                                                                           * null
                                                                                                           */;
                            if (!sameClass)
                            {
                                // constant propagation for initialized members
                                m_hasSideEffects = true;

                                // TODO: If the current function is not a static function and a member of the same class 
                                // and we have a ctor, then we know that __staticInit() has been called, because the ctor calls init.
                                final String pre = "(" + fullName + "." + JSSharedData.STATIC_INIT + "(), ";
                                final String post = ")";
                                return pre + code + post;
                            }
                        }
                        else if (!m_hasSideEffects && fdef.isStatic())
                        {
                            m_hasSideEffects = true;
                            m_needsSecondPass = true;

                            /*
                             * final ASDefinitionFilter memberFilter = new
                             * ASDefinitionFilter
                             * (ClassificationValue.MEMBERS_AND_TYPES,
                             * SearchScopeValue.INHERITED_MEMBERS,
                             * AccessValue.ALL, cdef); final IDefinition[]
                             * members =
                             * cdef.getAllMembers(currentScope.getProject(),
                             * memberFilter); for( IDefinition member: members )
                             * { if( member.isStatic() ) { m_hasSideEffects =
                             * true; } }
                             */
                        }
                    }
                }
            }
        }
        return code;
    }

    /**
     * resolveGetterName
     */

    private String resolveGetterName(IASNode iNode, String instanceName, Binding member, Boolean useBrackets)
    {
        // constant propagation for initialized members
        m_hasSideEffects = true;

        IDefinition def = null;
        final String baseNameWithQuotes = getBasenameFromBinding(member);
        final String baseName = removeQuotes(baseNameWithQuotes);

        // In instance.foo, foo is a member of instance's type
        // In instance["foo"], foo is a member of instance's type
        // In instance[foo], foo is an expression evaluating to a string, and the *string* is a member of instance's type
        // NOTE: see similar fix for setters in resolveSetterName()
        // TODO: warn if !baseNameIsMember and instanceName isn't an Array or Vector -- runtime name lookup can't
        //       handle get_/set_ prefixing properly. (Also need such a warning for "in" keyword and "for each" loops).
        final boolean baseNameIsMember = !useBrackets || !baseName.equals(baseNameWithQuotes);

        if (baseNameIsMember)
        {
            // TODO: duplicates / very similar to code in getSetterDefinition()
            /**
             * RE: baseName.contains(".") In
             * fl.motion.AnimatorBase.processCurrentFrame() there is this code,
             * that caused problems: anim.targetParent[anim.targetName] == null
             * where anim: AnimatorBase targetParent: getter returning
             * DisplayObjectContainer targetName: getter returning string
             * FalconJS 314157.0 generated this incorrect code:
             * (anim.get_targetParent().get_anim.get_targetName()() == null) The
             * correct code is: (anim.get_targetParent()[anim.get_targetName()]
             * == null)
             */
            // TODO: push some of this code into a getGetterDefinition() to mirror resolveSetterName() better?
            if (!baseName.contains("."))
            {
                def = member.getDefinition();
            }

            if (def != null && def instanceof ISetterDefinition)
            {
                // def = getDefinitionForInstance(iNode, createFullNameFromDefinition(def), ClassificationValue.GETTERS );
                def = getDefinitionForMember(iNode, def.getParent(), baseName, ClassificationValue.GETTERS);
            }

            if (def != null &&
                  def instanceof IGetterDefinition &&
                  !isDataType(def.getParent()) &&
                  !isExternalClass(currentScope.getProject(), def.getParent()) &&
                  !isVectorLength((IGetterDefinition)def))
            {
                return addStaticInitCall(def, makeGetterCall(currentScope.getProject(), def, instanceName, baseName), true);
            }

            // add explicit dependencies.
            if (def != null && def.getParent() != null && def != m_currentClassDefinition &&
                     !isDataType(def.getParent()) && !isExternalClass(currentScope.getProject(), def.getParent()))
            {
                m_referencedDefinitions.add(def);
            }
        }

        String ret = "";
        String memberName = createStringFromBinding(iNode, member);

        if (instanceName.isEmpty())
            ret = memberName;
        else
        {
            if (needsRuntimeLookup(iNode, instanceName, member))
            {
                String quote = "";
                if (baseNameIsMember && isAlphaNumString(baseNameWithQuotes) && !memberName.startsWith("\""))
                    quote = "\"";
                else
                {
                    // this will ensure that converted members are being resolved correctly.
                    final String mappedName = m_nameMap.get(member);
                    if (mappedName != null && !mappedName.isEmpty())
                        memberName = mappedName;
                }

                String self = JSSharedData.THIS;
                final String currentFullClassName = getCurrentFullClassName();
                if (currentFullClassName == null || currentFullClassName.isEmpty())
                    self = "null";

                registerAccessedPropertyName(iNode, memberName);
                ret = JSSharedData.JS_FRAMEWORK_NAME + ".getProperty(" + self + ", " + instanceName + ", " + quote + memberName + quote + ")";
            }
            else if (useBrackets)
                ret = instanceName + "[" + memberName + "]";
            else
                ret = instanceName + "." + memberName;
        }

        /*
         * doesn't work yet and reports too many false negatives. if(
         * member.getDefinition() == null && !instanceName.equals("arguments")
         * && !instanceName.equals(JSSharedData.JS_FRAMEWORK_NAME) &&
         * instanceName != null && !instanceName.isEmpty() &&
         * !getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME)) {
         * instanceName = removeParentheses( instanceName ); String
         * typeOfInstance = ""; def = getDefinitionForInstance(iNode,
         * instanceName, ClassificationValue.MEMBERS_AND_TYPES); if( def != null
         * ) { if( def instanceof GetterDefinition || def instanceof
         * SetterDefinition ) typeOfInstance = "Function"; else { if(
         * def.getType() != null && !def.getType().isEmpty() ) def =
         * getDefinitionForInstance(iNode, def.getType() ,
         * ClassificationValue.MEMBERS_AND_TYPES); if( def != null )
         * typeOfInstance =
         * createFullNameFromDefinition(currentScope.project,def); } } if(
         * typeOfInstance == null ) typeOfInstance = ""; if(
         * !isDataType(typeOfInstance) && !isBrowserDataType(typeOfInstance) &&
         * !isReservedDataType(typeOfInstance) ) { ret +=
         * " / * WARNING: cannot resolve type for lval. This expression might call a getter function. "
         * + "I=" + instanceName + ", M=" + memberName + ", T=" + typeOfInstance
         * + " * / "; } }
         */

        return addStaticInitCall(def, ret, true);
    }

    /**
     * Returns true if the given accrssor is the getter or setter for
     * Vector.length. We use this to avoid FJS-27: Vector is translated to JS
     * Array, where length is a plain old field and thus shouldn't have a
     * get_/set_ prefix.
     */
    private boolean isVectorLength(IAccessorDefinition def)
    {
        // Is accessor for a property called "length"?
        if (def.getBaseName().equals("length"))
        {
            // Is "length" property a member of Vector?
            IDefinition classContainingProp = def.getParent();
            boolean isClassVector = classContainingProp instanceof IAppliedVectorDefinition; // Vector of Objects
            if (!isClassVector)
            {
                // Vector of int/uint has a different definition under the hood (though this may be a bug)
                String className = classContainingProp.getBaseName();
                isClassVector = className.startsWith("Vector$");
            }
            return isClassVector;
        }
        return false;
    }

    /*
     * private Boolean isTopLevelClass( String name ) { return name.equals(
     * "Math" ) || name.equals( "Date" ) ; } private Boolean isTopLevelFunction(
     * String name ) { return name.equals( "parseInt" ) || name.equals(
     * "parseFloat" ) || name.equals( "setInterval" ) || name.equals(
     * "clearInterval" ) || name.equals( "setTimeout" ) || name.equals(
     * "clearTimeout" ) || name.equals( "isNaN" ) || name.equals( "isFinite" )
     * || name.equals( "escape" ) || name.equals( "unescape" ) || name.equals(
     * "decodeURIComponent" ) || name.equals( "encodeURIComponent" ); } private
     * Boolean isLocal( Name name ) { LexicalScope scope = currentScope; while(
     * scope != null && scope.traitsVisitor instanceof JSTraits ) { JSTraits
     * traits = (JSTraits)scope.traitsVisitor; if( traits.hasLocalVariable(
     * getBasenameFromName(name) ) ) return true; scope = scope.enclosingFrame;
     * } return false; }
     */

    private Name getNameFromBinding(Binding binding)
    {
        if (binding == null)
            throw JSSharedData.backend.createException("invalid binding");

        /*
         * this in anonymous functions and ctor functions. If code in anonymous
         * functions or ctor functions use "this" explicitly then we should emit
         * "this" and not "self". Furthermore anonymous functions should not
         * declare "var self = this;" at the begin of the function. See
         * https://zerowing.corp.adobe.com/display/htmldevtools/FalconJS+Bugs
         */
        if (binding.getName() == null)
        {
            final IDefinition def = binding.getDefinition();
            if (def != null)
            {
                /*
                 * ICompilerProject project = currentScope.getProject();
                 * ASDefinitionCache cache = currentScope.getDefinitionCache();
                 * Name defName = def.getMName(project,
                 * currentScope.getProblems());
                 */
                return makeName(def.getQualifiedName());
            }

            final Boolean isAnonymousFunction = isAnonymousFunction(getMethodName());
            if (isAnonymousFunction)
                return makeName(JSSharedData.JS_THIS);
            else
                return makeName(JSSharedData.THIS);
        }

        // Bindings can have Definitions.
        // REVISIT: Doesn't work yet. Interfers with nameToString().
        /*
         * final IDefinition def = binding.getDefinition(); if( def != null ) {
         * return makeName(
         * createFullNameFromDefinition(currentScope.project,def) ); }
         */

        //if( getBasenameFromBinding(binding).equals("...") )
        //    throw new Error( "Invalid binding: ellipsis has ot been converted to Array." );

        // TODO: cleanup
        // if( binding.getName().getQualifiers().length() > 1 )
        //    throw new Error( "Invalid binding: name has more than one qualifier." );

        return binding.getName();
    }

    public static String getBasenameFromName(Name name)
    {
        if (name == null)
            return JSSharedData.JS_THIS;

        // support for vectors.
        final String baseName = name.getBaseName();
        if (baseName == null && isVectorTypeName(name))
        {
            String param = name.getTypeNameParameter().getBaseName();
            if (m_typeMap.containsKey(param))
                return "Array.<" + m_typeMap.get(param) + ">";
            return "Array.<" + param + ">";
        }

        if (baseName.equals("..."))
            return "Array";
        return baseName;
    }

    public static String getBasenameFromBinding(Binding binding)
    {
        if (binding == null)
            return JSSharedData.THIS;
        return getBasenameFromName(binding.getName());
    }

    private String bindingToString(IASNode iNode, Binding binding, Boolean withThis, Boolean resolve)
    {
        final Name name = getNameFromBinding(binding);

        IDefinition def = binding.getDefinition();
        if (def == null)
            def = getDefinitionForName(iNode, name);
        if (def == null)
        {
            // see superGetter_setterDef testcase
            // The basename can be something like goog.base(...)
            final String baseName = getBasenameFromName(name);
            if (baseName.contains("(") && baseName.contains(")"))
                return baseName;

            final Boolean isAnonymousFunction = isAnonymousFunction(getMethodName());
            if (isAnonymousFunction)
                return JSSharedData.JS_THIS;
            else
                return JSSharedData.THIS;
        }

        return nameToString(iNode, def, name);

        /*
         * final Name name = getNameFromBinding( binding ); if( name == null )
         * return JSSharedData.THIS; return nameToString(iNode, name, withThis,
         * resolve);
         */
    }

    private static INamespaceDefinition getNamespaceDefinition(ASScope asScope, Name name)
    {
        if (name.getQualifiers() == null || name.getQualifiers().length() != 1)
            return null;

        final Namespace sq = name.getSingleQualifier();
        if (sq.getKind() != ABCConstants.CONSTANT_Namespace)
            return null;

        final INamespaceDefinition ns = JSSharedData.instance.getNamespace(sq.getName());

        // Workaround for Falcon bug: Namespace dependencies are not correctly handled.
        /*
         * if( ns == null && sq.getName().startsWith( "http://" ) ) {
         * m_needsSecondPass = true; }
         */
        return ns;
    }

    public static IDefinition getDefinitionForVectorName(ICompilerProject project, Name name)
    {
        IDefinition def = null;
        if (name.getTypeNameBase() != null)
        {
            // Vector
            final Nsset nsset = name.getTypeNameBase().getQualifiers();
            if (nsset != null)
            {
                final IDefinition definitionContext = null;
                final ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES, SearchScopeValue.ALL_SCOPES, AccessValue.ALL, definitionContext);

                for (Namespace ns : nsset)
                {
                    String defParam = name.getTypeNameParameter().getBaseName();
                    String defName = ns.getName() + ".Vector$";
                    if (defParam == null)
                        defName += "object";
                    else if (defParam.equals("Number"))
                        defName += "double";
                    else if (defParam.equals("int"))
                        defName += "int";
                    else if (defParam.equals("uint"))
                        defName += "uint";
                    else
                        defName += "object";
                    def = ASScopeUtils.findDefinitionByName(project.getScope(), project, defName, filter);
                    if (def != null)
                        break;
                }
            }
        }
        return def;
    }

    /**
     * Warning: this may return an AmbiguousName if name has a package
     * qualifier. TODO: should probably fix this, since it's bug-prone (see
     * FJS-44 & FJS-62)
     */
    public static IDefinition getDefinitionForNameByScope(ICompilerProject project, IASScope scope, Name name)
    {
        // Tamarin's ecma3/Exceptions/global_001_rt.as triggers NPE
        // http://watsonexp.corp.adobe.com/#bug=3026511
        if (name == null)
            return null;

        IDefinition def = JSSharedData.instance.getDefinition(name);
        if (def != null)
            return def;

        if (name.getTypeNameBase() != null)
            return getDefinitionForVectorName(project, name);

        final String filePrivateNS = "FilePrivateNS:";

        final String baseName = getBasenameFromName(name);
        final IDefinition definitionContext = null;
        final ASDefinitionFilter filterClassAndInterfaces = new ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES, SearchScopeValue.ALL_SCOPES, AccessValue.ALL, definitionContext);
        final Nsset nsset = name.getQualifiers();
        for (Namespace ns : nsset)
        {
            String defName = ns.getName();

            // find definition 
            if (ns.getKind() == CONSTANT_PrivateNs && defName.startsWith(filePrivateNS))
            {
                // find the def that is the owner of filePrivateNS
                defName = defName.substring(filePrivateNS.length());
                def = ASScopeUtils.findDefinitionByName(scope, project, defName, filterClassAndInterfaces);

                final ASDefinitionFilter filterPrivate = new ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES, SearchScopeValue.CONTAINING_SCOPES, AccessValue.ALL, def);
                def = ASScopeUtils.findDefinitionByName(scope, project, name.getBaseName(), filterPrivate);

                if (def == null)
                    def = AmbiguousDefinition.get();
            }
            else if (ns.getKind() == CONSTANT_PrivateNs)
            {
                final ASDefinitionFilter filterPrivate = new ASDefinitionFilter(ClassificationValue.VARIABLES_AND_FUNCTIONS, SearchScopeValue.CONTAINING_SCOPES, AccessValue.ALL, definitionContext);
                defName += "." + name.getBaseName();
                def = ASScopeUtils.findDefinitionByName(scope, project, defName, filterPrivate);
                if (def == null && scope instanceof ASScopeBase)
                {
                    final ASScope asScope = (ASScope)scope;
                    List<IDefinition> definitions = new ArrayList<IDefinition>(1);
                    Set<INamespaceDefinition> namespaceSet = null;
                    namespaceSet = new HashSet<INamespaceDefinition>(1);
                    INamespaceDefinition namespaceRef = NamespaceDefinition.createNamespaceDefinition(ns);
                	namespaceSet.add(namespaceRef);
                	asScope.getLocalProperty(project, definitions, name.getBaseName(), namespaceSet);
                	if (definitions.size() == 1)
                		def = definitions.get(0);
                }
            }
            else
            {
                if (defName.isEmpty())
                {
                    def = ASScopeUtils.findDefinitionByName(scope, project, baseName, filterClassAndInterfaces);

                    // this doesn't work but it should
                    if (def == null)
                    {
                        // final ASDefinitionFilter filterAll = new ASDefinitionFilter(ClassificationValue.ALL, SearchScopeValue.ALL_SCOPES, AccessValue.ALL, def);
                        // def = scope.findDefinitionByName(baseName, filterAll, cache );

                        // workaround for Falcon bug, because the code above is not finding any local variables.
                        if (def == null && scope instanceof ASScope)
                        {
                            final ASScope asScope = (ASScope)scope;
                            final INamespaceDefinition ins = getNamespaceDefinition(asScope, name);
                            String _name = removeParentheses(baseName);

                            // TODO: revisit
                            if (_name.contains("."))
                                _name = _name.substring(_name.lastIndexOf(".") + 1);

                            if (ins != null)
                                def = asScope.findPropertyQualified(project, ins, _name, DependencyType.INHERITANCE);
                            else
                                def = asScope.findProperty(project, _name, DependencyType.INHERITANCE);
                        }
                    }
                }
                else
                {
                    defName += "." + name.getBaseName();
                    def = ASScopeUtils.findDefinitionByName(scope, project, defName, filterClassAndInterfaces);
                    // workaround for Falcon bug, because the code above is not finding any local variables.
                    if (def == null && scope instanceof ASScope)
                    {
                        final ASScope asScope = (ASScope)scope;
                        final INamespaceDefinition ins = getNamespaceDefinition(asScope, name);
                        String _name = removeParentheses(baseName);

                        // TODO: revisit
                        if (_name.contains("."))
                            _name = _name.substring(_name.lastIndexOf(".") + 1);

                        if (ins != null)
                            def = asScope.findPropertyQualified(project, ins, _name, DependencyType.INHERITANCE);
                        else
                            def = asScope.findProperty(project, _name, DependencyType.INHERITANCE);
                    }
                }
            }
            if (def != null)
                break;
        }

        /*
         * if( scope instanceof ASScope ) { final ASScope asScope =
         * (ASScope)scope; final INamespaceDefinition ins =
         * getNamespaceDefinition(asScope, name); String _name =
         * removeParentheses(getBasenameFromName(name)); // TODO: revisit if(
         * _name.contains(".") ) _name = _name.substring( _name.lastIndexOf(".")
         * + 1 ); if( ins != null ) def = asScope.findPropertyQualified(project,
         * ins, _name, DependencyType.INHERITANCE ); else def =
         * asScope.findProperty(project, _name, DependencyType.INHERITANCE ); }
         * else if( scope instanceof ASProjectScope ) { final ASProjectScope
         * projectScope = (ASProjectScope)scope; final IDefinition
         * definitionContext = null; final ASDefinitionFilter filter = new
         * ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES,
         * SearchScopeValue.ALL_SCOPES, AccessValue.ALL, definitionContext);
         * final Nsset nsset = name.getQualifiers(); for( Namespace ns: nsset )
         * { String defName = ns.getName(); // find definition if( ns.getKind()
         * == CONSTANT_PrivateNs && defName.startsWith(filePrivateNS) ) { //
         * find the def that is the owner of filePrivateNS defName =
         * defName.substring( filePrivateNS.length() ); def =
         * projectScope.findDefinitionByName(defName, filter, cache ); final
         * ASDefinitionFilter filterPrivate = new
         * ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES,
         * SearchScopeValue.CONTAINING_SCOPES, AccessValue.ALL, def); def =
         * projectScope.findDefinitionByName(name.getBaseName(), filterPrivate,
         * cache ); if( def == null ) def = AmbiguousDefinition.get(); } else {
         * if( !defName.isEmpty()) defName += "."; defName +=
         * name.getBaseName(); def = projectScope.findDefinitionByName(defName,
         * filter, cache ); } if( def != null ) break; } }
         */

        // search for name in the currentScope
        /*
         * if( def == null && currentScope != null && currentScope.traitsVisitor
         * != null && currentScope.traitsVisitor.getTraits() != null ) { for(
         * Trait t : currentScope.traitsVisitor.getTraits() ) { Name n =
         * t.getNameAttr("name"); if( getBasenameFromName(name).equals(
         * getBasenameFromName(n)) ) { } } }
         */

        if (def instanceof AmbiguousDefinition)
        {
            final String fullName = getBasenameFromName(name);

            IDefinition foundDef = JSSharedData.instance.getDefinition(fullName);
            if (foundDef == null)
                foundDef = JSSharedData.instance.getDefinition(name);
            if (foundDef != null)
                def = foundDef;
        }

        return def;
    }

    public static IASScope getScopeFromNode(IASNode iNode)
    {
        NodeBase id = (NodeBase)iNode;

        // protected ASScope scope = id.getASScope();
        // This is the implementation from NodeBase::getASScope:

        IScopedNode scopeNode = id.getScopeNode(); // id.getContainingScope();
        IASScope scope = scopeNode != null ? scopeNode.getScope() : null;
        // If the ScopedNode had a null scope, keep looking up the tree until we
        // find one with a non-null scope.
        // TODO: Is it a bug that an IScopedNode returns null for it's scope?
        // TODO: this seems like a leftover from block scoping - for example, a
        // TODO: ForLoopNode is an IScopedNode, but it doesn't really have a scope
        while (scope == null && scopeNode != null)
        {
            scopeNode = scopeNode.getContainingScope();
            scope = scopeNode != null ? scopeNode.getScope() : null;
        }

        return scope;
    }

    public IDefinition getDefinitionForName(IASNode iNode, Name name)
    {
        final IASScope scope = getScopeFromNode(iNode);
        return getDefinitionForNameByScope(currentScope.getProject(), scope, name);
    }

    public static IDefinition getDefinitionForName(ICompilerProject project, Name name)
    {
        final IASScope scope = project.getScope();
        return getDefinitionForNameByScope(project, scope, name);
    }

    private static Boolean isLocalName(IDefinition def)
    {
        if (!isGlobalName(def)
              && (!(def instanceof ClassDefinition) && !(def.getParent() instanceof ClassDefinition))
              && (!(def instanceof InterfaceDefinition) && !(def.getParent() instanceof InterfaceDefinition)))
        {
            return true;
        }
        return false;
    }

    private static Boolean isGlobalName(IDefinition def)
    {
        IDefinition parent = def.getParent();

        return parent == null;
    }

    private String getPackageNameFromName(IASNode iNode, Name name, Boolean withRoot, Boolean useQualifiers)
    {
        if (name == null)
            return "";

        IDefinition def = JSSharedData.instance.getDefinition(name);
        if (def == null)
        {
            def = getDefinitionForNameByScope(currentScope.getProject(), currentScope.getProject().getScope(), name);
            if (def != null)
                JSSharedData.instance.registerDefinition(def);
        }

        if (def == null)
        {
            JSSharedData.instance.getDefinition(name);
            currentScope.addProblem(new JSInternalCompilerProblem(iNode, "cannot find definition for " + name.toString()));
        }

        String packageName = "";
        if (!JSSharedData.instance.isExtern(def.getQualifiedName()))
        {
            if (withRoot)
                packageName += JSSharedData.ROOT_NAME;
            packageName += def.getPackageName();
        }

        /*
         * Namespace ns = null; if( name.getQualifiers() != null ) { if(
         * name.getQualifiers().length() == 1 ) { ns =
         * name.getSingleQualifier(); } else if( useQualifiers ) { for(
         * Namespace qualifier: name.getQualifiers() ) { if( qualifier.getKind()
         * == ABCConstants.CONSTANT_PackageNs ) { ns = qualifier; break; } } } }
         * if( ns == null ) return ""; // {PackageNs:"tests"}::TestBase if(
         * ns.getKind() == ABCConstants.CONSTANT_Namespace || ns.getKind() ==
         * ABCConstants.CONSTANT_PrivateNs ) return ""; String packageName =
         * ns.getName(); // File-private namespaces are not valid identifiers
         * unless we strip out the ":" if
         * (packageName.startsWith("FilePrivateNS:")) { packageName =
         * packageName.replaceAll(":", "_"); } if( withRoot &&
         * !packageName.isEmpty() && !JSSharedData.ROOT_NAME.isEmpty() &&
         * !JSSharedData.instance.isExtern(name) ) packageName =
         * JSSharedData.ROOT_NAME + packageName;
         */
        return packageName;
    }

    /*
     * public String getFullNameFromName( Name name, Boolean withRoot, Boolean
     * useQualifiers ) { if( name == null ) return null; String packageName =
     * getPackageNameFromName(name,withRoot,useQualifiers); if( packageName ==
     * null || packageName.isEmpty() ) return getBasenameFromName(name); return
     * packageName + "." + getBasenameFromName(name); } public String
     * getFullNameFromName( Name name, Boolean withRoot ) { return
     * getFullNameFromName(name, withRoot, false); }
     */
    public JSEmitter getEmitter()
    {
        if (currentScope != null)
        {
            final JSEmitter emitter = (JSEmitter)currentScope.getEmitter();
            return emitter;
        }
        return null;
    }

    public String getCurrentClassName()
    {
        final JSEmitter emitter = getEmitter();
        if (emitter != null)
            return emitter.getCurrentClassName();
        return null;
    }

    public String getCurrentSuperClassName()
    {
        final JSEmitter emitter = getEmitter();
        if (emitter != null)
            return emitter.getCurrentSuperClassName();
        return null;
    }

    public String getCurrentSuperClassFullName()
    {
        final JSEmitter emitter = getEmitter();
        if (emitter != null)
            return emitter.getCurrentSuperClassFullName();
        return null;
    }

    public String getCurrentPackageName()
    {
        final JSEmitter emitter = getEmitter();
        if (emitter != null)
            return emitter.getCurrentPackageName();
        return null;
    }

    public String getCurrentFullClassName()
    {
        final JSEmitter emitter = getEmitter();
        if (emitter != null)
        {
            final String packageName = getCurrentPackageName();
            if (packageName != null && !packageName.isEmpty())
                return getCurrentPackageName() + "." + emitter.getCurrentClassName();
            return emitter.getCurrentClassName();
        }
        return null;
    }

    public String getCurrentMethodName()
    {
        return currentScope.getMethodInfo().getMethodName();
    }

    public Boolean isCurrentFramework()
    {
        final String currentFullClassName = getCurrentFullClassName();
        return currentFullClassName != null && getCurrentFullClassName().equals(JSSharedData.JS_FRAMEWORK_NAME);
    }

    public static String removeRootName(String fromThisName)
    {
        if (JSSharedData.ROOT_NAME.length() > 0)
            return fromThisName.replaceFirst(JSSharedData.ROOT_NAME, "");
        return fromThisName;
    }

    public static IFunctionDefinition getCurrentMethod(IASNode currentNode)
    {
        // TODO: would it be cleaner/faster to walk up the LexicalScopes instead?
        IASNode node = currentNode;
        while (node != null && !(node instanceof IFunctionNode))
        {
            node = node.getParent();
        }
        if (node != null)
            return ((IFunctionNode)node).getDefinition();
        else
            return null;
    }

    private String createFullClassName(Boolean withRoot)
    {
        String s = "";
        if (withRoot && !JSSharedData.ROOT_NAME.isEmpty() &&
                  !getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME))
        {
            s = JSSharedData.ROOT_NAME;

            // remove trailing dot
            if (s.endsWith("."))
                s = s.substring(0, s.length() - 1);
        }
        String packageName = getCurrentPackageName();
        if (packageName != null && packageName.length() > 0)
        {
            if (s.length() > 0)
                s += ".";
            s += packageName;
        }

        final String className = getCurrentClassName();
        if (className != null && className.length() > 0)
        {
            if (s.length() > 0)
                s += ".";
            s += className;
        }
        return s;
    }

    private String createFullName(String tail, Boolean withRoot)
    {
        String s = createFullClassName(withRoot);
        if (s.length() > 0)
            s += ".";
        s += tail;
        return s;
    }

    private String indent()
    {
        String s = "";
        for (int i = 0; i < m_indent; ++i)
            s += "    ";
        return s;
    }

    public static String indentBlock(String block, int indentBy)
    {
        String s = "";
        String[] parts = block.split("\n");
        for (String part : parts)
        {
            for (int i = 0; i < indentBy; ++i)
                s += "    ";

            s += part + "\n";
        }
        return s;
    }

    private String endl()
    {
        return "\n";
    }

    // Turns out that other Errors like EvalError
    // behave like Error, see handleCast
    private static boolean isErrorDataType(String typeName)
    {
        return typeName.equals("Error") ||
                  typeName.equals("ArgumentError") ||
                  typeName.equals("DefinitionError") ||
                  typeName.equals("EvalError") ||
                  typeName.equals("RangeError") ||
                  typeName.equals("ReferenceError") ||
                  typeName.equals("SecurityError") ||
                  typeName.equals("SyntaxError") ||
                  typeName.equals("TypeError") ||
                  typeName.equals("UninitializedError") ||
                  typeName.equals("URIError") ||
                  typeName.equals("VerifyError");
    }

    private static boolean isComplexDataType(String typeName)
    {
        // Object, Array, Date, Error, Function, RegExp, XML, and XMLList.
        return typeName.equals("Object") ||
                  typeName.equals("Array") ||
                  typeName.equals("Date") ||
                  typeName.equals("Function") ||
                  typeName.equals("Class") ||
                  typeName.equals("RegExp") ||
                  typeName.equals("XML") ||
                  typeName.equals("XMLList") ||
                  typeName.equals("Math") ||
                  isErrorDataType(typeName);
    }

    private static boolean isNumberDataType(String typeName)
    {
        //  int, Number, uint
        return typeName.equals("int") ||
                  typeName.equals("Number") ||
                  typeName.equals("uint");
    }

    private static boolean isPrimitveDataType(String typeName)
    {
        // Boolean, int, Null, Number, String, uint, and void.
        return isNumberDataType(typeName) ||
                  typeName.equals("Boolean") ||
                  typeName.equals("Null") ||
                  typeName.equals("IntClass") ||
                  typeName.equals("UIntClass") ||
                  typeName.equals("String") ||
                  typeName.equals("*") ||
                  typeName.equals("void");
    }

    public static boolean isReservedDataType(String typeName)
    {
        // Boolean, int, Null, Number, String, uint, and void.
        return typeName.startsWith("com.jquery") ||
                  typeName.equals("browser.MathGlue") ||
                  typeName.equals("JQuery") ||
                  typeName.equals("browser.JSError") ||
                  typeName.equals("browser.JQEvent") ||
                  typeName.equals("JQEvent") ||
                  typeName.equals("jQuery.Event");
    }

    public static boolean isBrowserDataType(String qualifiedClassName)
    {
        return qualifiedClassName.startsWith("org.w3c.dom") ||
                  qualifiedClassName.startsWith("com.jquery.") ||
                  qualifiedClassName.startsWith("jQuery.") ||
                  qualifiedClassName.startsWith("goog.");
    }

    /*
     * private boolean needsSelfParameter( IASNode iNode, String className ) {
     * if( !JSSharedData.m_useSelfParameter ) return false; if(
     * className.length() == 0 ) return true; if(
     * className.equals(JSSharedData.JS_FRAMEWORK_NAME) ) return false; if(
     * className.endsWith(".JSSharedData._SUPER") ) return true; final String
     * typeName = getTypeOfInstance(iNode, className); if( typeName.length() ==
     * 0 ) return false; if( typeName.equals( "Function" ) ) return true; if(
     * typeName.equals(JSSharedData.JS_FRAMEWORK_NAME) ) return false; if(
     * isDataType( typeName ) ) return false; if( isBrowserDataType( typeName )
     * ) return false; if( isReservedDataType( typeName ) ) return false; return
     * true; }
     */

    public static boolean isDataType(String typeName)
    {
        return isPrimitveDataType(typeName) || isComplexDataType(typeName) || isReservedDataType(typeName);
    }

    public static boolean isDataType(IDefinition def)
    {
        // workaround for Falcon bug CMP-984
        // DefinitionBase::getContainingToplevelDefinition() runs into an NPE in test case source_level_function    
        try
        {
            final String packageName = def.getPackageName();
            if (!packageName.isEmpty())
                return false;
        }

        catch (NullPointerException npe)
        {
            return false;
        }

        // java.lang.AssertionError: This method should not be called until we have a containing scope.!
        catch (AssertionError e)
        {
            return false;
        }

        final String typeName = def.getBaseName();
        return isPrimitveDataType(typeName) || isComplexDataType(typeName) || isReservedDataType(typeName);
    }

    private IDefinition getSuperClass(ICompilerProject project, IDefinition def)
    {
        if (def instanceof ClassDefinition)
        {
            final ClassDefinition classDef = (ClassDefinition)def;
            return classDef.resolveBaseClass(project);
        }

        return null;
    }

    private String findClassWithMethod(String owner, String methodName)
    {
        String _class = owner;
        while (_class != null && !isDataType(_class))
        {
            String name = makeName(_class + "." + methodName).toString();
            if (m_sharedData.hasVarName(name))
            {
                owner = _class;
                // TODO: revisit
                if (m_buildPhase.equals("CODEGEN") && owner.equals("void"))
                    return owner;
                return owner;
            }

            _class = getBasenameFromName(m_sharedData.getSuperClass(makeName(_class)));
        }

        // TODO: revisit
        if (m_buildPhase.equals("CODEGEN"))
            return owner;

        return owner;
    }

    private String generateNewCall(IASNode node, Binding class_name, Vector<String> args)
    {
        String _class = "";
        final Name _className = class_name.getName();
        final String baseName = getBasenameFromBinding(class_name);

        /*
         * TODO: e15_4_2_2_2, ecma3/Array/e15_4_2_2_2.abc : expected failure:
         * (new Array(new Number(1073741823))).length = 1 failed! expected:
         * 1073741823 reason: JavaScript returns 'typeof(new Number()) =
         * "object"' ecma3/Array/e15_4_2_2_2.abc : expected failure: (new
         * Array(new Number(0))).length = 1 failed! expected: 0 reason:
         * JavaScript returns 'typeof(new Number()) = "object"'
         * ecma3/Array/e15_4_2_2_2.abc : expected failure: (new Array(new
         * Number(1000))).length = 1 failed! expected: 1000 reason: JavaScript
         * returns 'typeof(new Number()) = "object"'
         */

        if (isDataType(baseName))
        {
            // "new int(0)" needs to be translated to "new Number(0)" 
            if (baseName.equals("int"))
                _class = "Number";
            else if (baseName.equals("uint"))
                _class = "Number";

        }
        else
        {
            final IDefinition def = getDefinitionForName(node, _className);

            // constant propagation for initialized members
            if (!m_hasSideEffects && hasSideEffects(def))
                m_hasSideEffects = true;

            if (def instanceof ClassDefinition)
            {
                // Note: because getDefinitionForName() doesn't handle package qualfiers, ambiguous shortnames
                // wind up in the catch-all "def != null" case below.
                _class = createFullNameFromDefinition(currentScope.getProject(), def);
                if (isExternalClass(currentScope.getProject(), def))
                {
                    // Invalid JavaScript code is generated for new DOMParser ()
                    // http://watsonexp.corp.adobe.com/#bug=2985815
                    final IMetaTag extern = def.getMetaTagByName(JSSharedData.EXTERN_METATAG);
                    if (extern != null &&
                          extern.getAttributeValue("name") != null &&
                          !extern.getAttributeValue("name").isEmpty())
                    {
                        _class = extern.getAttributeValue("name");
                    }
                    else
                    {
                        _class = baseName;
                    }
                }
            }
            else if (def instanceof IGetterDefinition || def instanceof ISetterDefinition)
            {
                // Property (of type Class or Function) instead of literal type name - resolve into getter call
                _class = "(" + bindingToString(node, class_name, true, true) + ")";
            }
            else if (def instanceof IFunctionDefinition)
            {
                // Function call - "_className" was the original compiled function call expression
                _class = baseName;
            }
            else if (def != null)
            {
                // Identifier needing "self." prefix, OR qualified classname where shortname is ambiguous (def is
                // AmbiguousDefinition) - add "self." prefix and/or resolve into fully-qualified name.
                _class = bindingToString(node, class_name, true, true);
            }
            else
            {
                // If we couldn't resolve a def or got one not of the above types, chances are baseName is
                // actually not a name but an expression... so wrap it in parens to preserve predence vs. 'new'
                // operator, and then just use the string verbatim.
                _class = baseName;
            }
        }

        // TODO: do we ever hit this case other than when !isDataType()? (e.g. could this go in an 'else'?)
        if (_class.isEmpty())
            _class = baseName;

        String result = "";

        // final Boolean useNew =  getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME) || isDataType(_class) || !m_sharedData.hasClass(removeRootName(_class)); /*|| !needsSelfParameter(_class)*/;
        final Boolean useNew = JSSharedData.m_useClosureLib || getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME) || isDataType(_class); // || !m_sharedData.hasClass(removeRootName(_class)); /*|| !needsSelfParameter(_class)*/;

        if (useNew)
            result += "(new " + _class + "(";
        else
        {
            result += JSSharedData.JS_FRAMEWORK_NAME + ".newObject(" + _class + ", [";
        }

        result += collectCallParameters(node, "", null, args, false, "");

        if (useNew)
        {
            result += ")"; // closes ctor parameters
            result += ")"; // closes (new ...)
        }
        else
        {
            result += "]"; // closes ctor parameters
            result += ")"; // closes newObject(...)
        }

        // Symbol support
        if (m_sharedData.hasSymbols() && !_class.isEmpty())
        {
            final String symbolName = m_sharedData.getSymbol(_class);
            if (symbolName != null && !symbolName.isEmpty())
                result = symbolName + "." + JSSharedData.SYMBOL_INSTANCE + "." + JSSharedData.SYMBOL_INIT + "(" + result + ")";
        }

        /*
         * if( JSSharedData.m_useClosureLib && getCurrentClassName() != null &&
         * !getCurrentClassName().equals(JSSharedData.JS_FRAMEWORK_NAME) &&
         * !isDataType(_class)) { result = "adobe.addClassInfo(" + result + ", "
         * + _class + ")"; }
         */
        return result;
    }

    private Boolean isConvertedFunction(IDefinition def)
    {
        if (def instanceof FunctionDefinition)
        {
            if (this.m_convertedMembers.contains(def.getBaseName()))
                return true;

            final IDefinition parent = def.getParent();
            if (parent != null && parent instanceof FunctionDefinition)
            {
                final IMetaTag metaTag = parent.getMetaTagByName(JSSharedData.CONVERT_LOCAL_VARS_TO_MEMBERS);
                if (metaTag != null)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private String injectCreateProxy(IASNode node, IDefinition argDef, String arg)
    {
        if (isCurrentFramework())
            return arg;

        if (argDef == null)
        {
            // see test case createProxyForArrayItems():
            // const arr : Array = [new Test()];
            // callWithFunctionParameter( arr[0].createProxyForArrayItems );
            /*
             * if( arg.startsWith("adobe.getProperty(" )) arg =
             * arg.replaceFirst("getProperty", "getPropertyAsProxy" );
             */
            /*
             * if( node instanceof IFunctionCallNode ) { final IFunctionCallNode
             * fnode = (IFunctionCallNode)node; final IExpressionNode[] pnode =
             * fnode.getArguments(); for( IExpressionNode anode: pnode ) {
             * IDefinition def = anode.getDefinition(); if(def == null && anode
             * instanceof IMemberAccessExpressionNode ) {
             * IMemberAccessExpressionNode man =
             * (IMemberAccessExpressionNode)anode; def =
             * getDefinitionForNode(man.getLeft(),false); def = null; Object
             * result = JSGenerator.generateParameterExpression(man.getLeft(),
             * currentScope.getProject()); result = null; } } }
             */
            return arg;
        }

        // tamarin_ecma3_Array_e15_4_2_2_2()
        final String argType = argDef.getTypeAsDisplayString();
        if (!argType.equals("Function"))
        {
            if (arg.contains("new Number(") && argDef == currentScope.getProject().getBuiltinType(BuiltinType.NUMBER))
                arg = "(" + arg + ").valueOf()";
            else if (arg.contains("new String(") && argDef == currentScope.getProject().getBuiltinType(BuiltinType.STRING))
                arg = "(" + arg + ").toString()";
            return arg;
        }

        final String argWithoutThis = arg.replaceFirst(JSSharedData.THIS + ".", "");

        IDefinition def = null;
        String _stem = null;
        String _member = null;
        if (arg.startsWith("[") || arg.endsWith("]"))
        {
            /*
             * Array params need to be ignored. Handles this test case: private
             * function callFuncWithArrayParam( arr : Array ) : void { const
             * localFunction = function():void { const num : uint = 13;
             * funcWithArrayParam( [num] ); } }
             */
        }
        else if (arg.startsWith("/** @type {function("))
        {
            /*
             * Function params need to be ignored. i.e.:
             * pgt.PagedView.moveBackward(); dl.then(
             * function(gotToThePage:Boolean):void { ... } );
             */

        }
        else if (arg.startsWith("adobe.createProxy("))
        {
            // adobe.createProxy() is currently a public method,
            // which we need to exclude from proxying.
        }
        else if (arg.startsWith("adobe.getProperty("))
        {
            // see test case createProxyForArrayItems():
            // const arr : Array = [new Test()];
            // callWithFunctionParameter( arr[0].createProxyForArrayItems );

            arg = arg.replaceFirst("getProperty", "getPropertyAsProxy");
        }
        else if (arg.equals(JSSharedData.JS_THIS))
        {
            // we need to exclude "this" in anonymous functions from proxying.
        }
        else if (argWithoutThis.contains("."))
        {
            _stem = argWithoutThis.substring(0, argWithoutThis.lastIndexOf("."));
            _member = argWithoutThis.substring(argWithoutThis.lastIndexOf(".") + 1);
            def = this.getDefinitionForName(node, makeName(_stem));
            if (def instanceof ClassDefinition)
                _stem = createFullNameFromDefinition(currentScope.getProject(), def);
        }
        else
        {
            def = this.getDefinitionForName(node, makeName(argWithoutThis));

            if (_stem == null && _member == null)
            {
                // FalconJS needs to inject adobe.createProxy() for source-level functions, which get promoted to methods.
                // (source level-functions and anonymous functions are usually local and should not be proxy'ed. But promoted SLFs are obviously an exception.
                if (def == null || convertLocalVarsToMembers(node) || isConvertedFunction(def) || (!isLocalName(def) && !isGlobalName(def)))
                {
                    _stem = JSSharedData.THIS;
                    _member = argWithoutThis;
                }
            }
        }

        if (_stem != null && !_stem.equals(JSSharedData.JS_FRAMEWORK_NAME) && _member != null && !_member.contains("("))
        {
            String type = this.getTypeOfInstance(node, argWithoutThis);
            if (type.equals("Function"))
            {
                // we need to check for vectors, because vectors get translated to arrays.
                // See tests/testceases/vectors. We now correctly identify foo.length as a function.
                // But we don't want to inject createProxy(), because Array.length is native.
                IDefinition idef = getDefinitionForInstance(node, _stem, ClassificationValue.CLASSES);
                if (idef == null || !(idef instanceof AppliedVectorDefinition))
                {
                    idef = getDefinitionForInstance(node, _stem + "." + _member, ClassificationValue.MEMBERS_AND_TYPES);
                    if (idef == null || !isExternalClass(currentScope.getProject(), idef.getParent()))
                    {
                        if (def instanceof GetterDefinition)
                            arg = resolveGetterName(node, _stem, makeMemberBinding(node, _stem, _member), false);
                        else if (!isDataType(def))
                        {
                            // arg = JSSharedData.JS_FRAMEWORK_NAME + ".createProxy(" + nameToString(node, makeName(_stem), true, true) + ",\"" + _member + "\")";
                            if (!_stem.equals(JSSharedData.THIS) && !_stem.contains("("))
                            {
                                _stem = nameToString(node, makeName(_stem), true, true);
                            }
                            arg = JSSharedData.JS_FRAMEWORK_NAME + ".createProxy(" + _stem + ", " + _stem + "." + _member + ")";
                        }
                    }
                }
            }
        }

        return arg;
    }

    private String collectCallParameters(IASNode node, String stem, Binding method_name, Vector<String> args, Boolean addSelfParameter, String extraParams)
    {
        String result = "";

        final String basename = getBasenameFromBinding(method_name);

        // FalconJS needs to inject adobe.createProxy() for source-level functions, which get promoted to methods.
        // (source level-functions and anonymous functions are usually local and should not be proxy'ed. But promoted SLFs are obviously an exception.
        // final Boolean convertLocalVarsToMembers = convertLocalVarsToMembers(node);

        if (args.size() == 2 && stem.equals(JSSharedData.JS_FRAMEWORK_NAME) && basename.equals("createProxy"))
        {
            result = args.get(0) + ", " + args.get(1);
            return result;
        }

        // TypeError: Result of expression 'self.m_displayObject' [undefined] is not an object.
        // if( m_buildPhase.equals("CODEGEN") && stem.contains("sprite.get_graphics") && method_name != null && method_name.toString().contains("beginFill"))
        //      getTypeOfInstance(stem);

        // avmplus.test.acceptance.shell.AddTestCase
        // TypeError: Result of expression '_self.getTestCaseResult' [undefined] is not a function.     

        if (addSelfParameter)
            result = JSSharedData.THIS;
        /*
         * if( addSelfParameter && needsSelfParameter(node,
         * createFullClassName(false)) ) { final String methodName = method_name
         * != null ? bindingToString(node, method_name,false,true) : ""; if(
         * !methodName.equals( "new" ) && !methodName.equals( "delete" ) ) { if(
         * stem.length() > 0 ) { if( needsSelfParameter(node, stem) ) { if(
         * stem.startsWith( "self.JSSharedData._SUPER" ) ) result =
         * stem.replaceFirst( "self.JSSharedData._SUPER", "self" ); else result
         * = stem; } } else { if( needsSelfParameter(node, methodName) ||
         * method_name.toString().equals("{}::super") ) result =
         * JSSharedData.THIS; else { String s = bindingToString(node,
         * method_name,true,true); if( s.startsWith(JSSharedData.THIS +".")) s =
         * s.replaceFirst( JSSharedData.THIS, createFullClassName(false) ); else
         * s = removeRootName(methodName); // s = makeName(s).toString(); s =
         * getTypeForMember( node, extractPackageName(s), extractClassName(s) );
         * if( s.length() > 0 ) result = JSSharedData.THIS; } } } }
         */

        if (args.size() == 0 && extraParams.isEmpty())
            return result;

        // using types from arg nodes.
        if (!(node instanceof IFunctionCallNode))
            currentScope.addProblem(new JSInternalCompilerProblem(node, "this node is not a function node"));
        final IFunctionCallNode fnode = (IFunctionCallNode)node;
        final IDefinition calledDef = fnode.resolveCalledExpression(currentScope.getProject());
        final List<IDefinition> argDefs = new ArrayList<IDefinition>();
        /*
         * if( calledDef instanceof VariableDefinition ) calledDef =
         * calledDef.resolveType(currentScope.getDefinitionCache()); if(
         * !(calledDef instanceof IFunctionDefinition) )
         * currentScope.getProblems().add( new JSInternalCompilerProblem(iNode,
         * "Can't get called function of " + node.toString() ); final
         * IFunctionDefinition fdef = (IFunctionDefinition)calledDef; for(
         * IParameterDefinition param: fdef.getParameters() ) { IDefinition def
         * = null; IVariableNode vnode = param.getVariableNode(); if( vnode !=
         * null ) def = vnode.getDefinition(); if( def == null ) { final String
         * type = param.getType(); if(
         * type.equals(IASLanguageConstants.ANY_TYPE) ) def =
         * currentScope.getDefinitionCache
         * ().getBuiltinType(BuiltinType.ANY_TYPE); else if(
         * type.equals(IASLanguageConstants.Array) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.ARRAY);
         * else if( type.equals(IASLanguageConstants.Boolean) ) def =
         * currentScope
         * .getDefinitionCache().getBuiltinType(BuiltinType.BOOLEAN); else if(
         * type.equals(IASLanguageConstants.Class) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.CLASS);
         * else if( type.equals(IASLanguageConstants.Function) ) def =
         * currentScope
         * .getDefinitionCache().getBuiltinType(BuiltinType.FUNCTION); else if(
         * type.equals(IASLanguageConstants._int) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.INT);
         * else if( type.equals(IASLanguageConstants.Namespace) ) def =
         * currentScope
         * .getDefinitionCache().getBuiltinType(BuiltinType.NAMESPACE); else if(
         * type.equals(IASLanguageConstants.Null) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.NULL);
         * else if( type.equals(IASLanguageConstants.Number) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.NUMBER);
         * else if( type.equals(IASLanguageConstants.Object) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.OBJECT);
         * else if( type.equals(IASLanguageConstants.QName) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.QNAME);
         * else if( type.equals(IASLanguageConstants.RegExp) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.REGEXP);
         * else if( type.equals(IASLanguageConstants.String) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.STRING);
         * else if( type.equals(IASLanguageConstants.Undefined) ) def =
         * currentScope
         * .getDefinitionCache().getBuiltinType(BuiltinType.UNDEFINED); else if(
         * type.equals(IASLanguageConstants.uint) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.UINT);
         * else if( type.equals(IASLanguageConstants.Vector) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.VECTOR);
         * else if( type.equals(IASLanguageConstants.void_) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.VOID);
         * else if( type.equals(IASLanguageConstants.XML) ) def =
         * currentScope.getDefinitionCache().getBuiltinType(BuiltinType.XML);
         * else if( type.equals(IASLanguageConstants.XMLList) ) def =
         * currentScope
         * .getDefinitionCache().getBuiltinType(BuiltinType.XMLLIST); else def =
         * null; } if( def != null ) def =
         * def.resolveType(currentScope.getDefinitionCache()); if( def == null )
         * currentScope.getProblems().add( new JSInternalCompilerProblem(iNode,
         * "Can't resolve type of parameter for " + node.toString() );
         * argDefs.add(def); }
         */

        IParameterDefinition[] params = null;
        if (calledDef instanceof IFunctionDefinition)
        {
            final IFunctionDefinition fdef = (IFunctionDefinition)calledDef;
            params = fdef.getParameters();
        }

        // poor man's type inference.
        if (calledDef == null)
        {
            final Set<IFunctionDefinition> methods = m_sharedData.getMethods(currentScope.getProject(), basename);
            if (methods.size() == 1)
                params = methods.iterator().next().getParameters();
        }

        int nthArg = 0;
        final IExpressionNode[] pnode = fnode.getArgumentNodes();
        for (IExpressionNode anode : pnode)
        {
            IDefinition def = anode.resolve(currentScope.getProject());
            if (def == null && params != null && nthArg < params.length)
            {
                final IVariableNode vnode = params[nthArg].getVariableNode();
                if (vnode != null)
                {
                    def = vnode.getDefinition();
                }
            }

            /*
             * class method registered as eventListener gets global when class
             * instance is in an array If def is null we were not able to
             * identify the signature of this parameter. Our second attempt is
             * trying to infer the type of the parameter by its value.
             */
            if (def == null && anode instanceof IMemberAccessExpressionNode)
            {
                final IMemberAccessExpressionNode mnode = (IMemberAccessExpressionNode)anode;
                final IASNode right = mnode.getRightOperandNode();
                if (right != null && right instanceof IIdentifierNode)
                {
                    final IIdentifierNode inode = (IIdentifierNode)right;
                    final Set<IFunctionDefinition> methods = m_sharedData.getMethods(currentScope.getProject(), inode.getName());
                    if (methods != null && methods.size() == 1)
                        def = methods.iterator().next();
                }
            }

            argDefs.add(def);
            ++nthArg;
        }

        nthArg = 0;
        boolean comma = result.length() > 0;

        if (!extraParams.isEmpty())
        {
            if (comma)
                result += ", ";
            else
                comma = true;
            result += extraParams;
        }

        for (String arg : args)
        {
            if (comma)
                result += ", ";
            else
                comma = true;

            arg = injectCreateProxy(node, argDefs.get(nthArg), stripTabs(arg));
            result += arg;
            ++nthArg;
        }

        return result;
    }

    /**
     * Transform a list of qualified name parts into a name. Resolving the name
     * may yield a QName or a multiname, depending on how it was resolved. TODO:
     * To be replaced by annotations on reference ASTs.
     * 
     * @return the resolved name.
     */
    /*
     * Name resolveName(Vector<? extends Object> qualifiers ) { if ( 1 ==
     * qualifiers.size() ) { // No qualifiers, just the base name. return
     * currentScope.resolveName(qualifiers.firstElement().toString()); } else {
     * StringBuffer buf = new StringBuffer(); for ( int i = 0; i <
     * qualifiers.size() - 1; i++ ) { if ( buf.length() > 0 ) buf.append('.');
     * buf.append(qualifiers.elementAt(i).toString()); } String base_name =
     * (String)qualifiers.lastElement(); return new Name(new Nsset(new
     * Namespace(CONSTANT_PackageNs, buf.toString())), base_name); } }
     */
    /*
     * public String generateFunctionBody(String function_body) { return
     * generateFunctionBody(function_body,null); }
     */

    /**
     * Generate boilerplate function prolog/epilog code.
     * 
     * @param block - the actual CFG.
     * @param return_type - the function's return type. Not presently used.
     */
    public String generateFunctionBody(IASNode iNode, String function_body, Binding returnType, Boolean uuu)
    {
        final Name return_type = returnType == null ? null : returnType.getName();
        final MethodInfo mi = currentScope.getMethodInfo();
        final String methodName = getMethodName();
        final Boolean isAnonymousFunction = isAnonymousFunction(methodName);
        final Boolean isCtor = methodName != null && methodName.equals(getCurrentClassName());

        if (mi != null && return_type != null && mi.getReturnType() == null)
        {
            mi.setReturnType(return_type);
        }

        if (return_type != null)
            usedTypes.add(getBasenameFromBinding(returnType));

        String result = ""; // indent() + "{" + endl();

        /*
         * !isAnonymousFunction is necessary. AS and JS are different in the way
         * "this" is being treated in anonymous functions. i.e. public function
         * whoIsThis() : void { const localFunction : Function = funciton():void
         * { this.callMe(); }; } In this example this.callMe() refers to the
         * callMe method of the class. In JS "this" refers to the local
         * function! By supressing "var self = this;" for anonymous functions
         * we'll emulate the AS behavior. The JS result then looks like this:
         * tests.Test.whoIsThis.prototype = function() { var self = this; const
         * localFunction : Function = funciton():void { // NOT EMITTED: var self
         * = this; self.callMe(); }; };
         */
        if (!JSSharedData.m_useSelfParameter && !isAnonymousFunction &&
                  /* TODO: !needsSelfParameter(createFullClassName(false)) && */function_body.contains(JSSharedData.THIS))
        {
            // final Binding fullClassName = makeBinding(createFullClassName(false));
            // this.registerLocalVariable(currentScope, makeBinding("self"), fullClassName, fullClassName);

            final Binding fullClassName = new Binding(iNode, makeName(createFullClassName(false)), null);
            this.registerLocalVariable(currentScope, new Binding(iNode, makeName("self"), null), fullClassName, fullClassName);
            result += indentBlock("/** @type {" + createFullClassName(true) + "} */" + endl(), 1);
            result += indentBlock("var " + JSSharedData.THIS + " = this;" + endl(), 1);
        }

        //  Constructor-specific processing: add the instance initializers,
        //  add a constructsuper call if none exists.
        if (haveAPrioriInstructions() && !isAnonymousFunction)
        {
            result += indentBlock(aPrioriInstructions, 1);

            //  If this is a constructor and there's no explicit
            //  super() call, synthesize one.
            //  Note that this may be a semantic error if the 
            //  superclass' constructor needs arguments.
            /*
             * if ( isCtor && !block.contains("super(") ) { // Call the
             * superclass' constructor after the instance // init instructions;
             * this doesn't seem like an abstractly // correct sequence, but
             * it's what ASC does. result += "" // TODO: //
             * result.addInstruction(OP_getlocal0); //
             * result.addInstruction(OP_constructsuper, 0); }
             */
        }

        // http://livedocs.adobe.com/flex/3/html/help.html?content=basic_as_2.html
        // "If you define the constructor, but omit the call to super(), Flex automatically 
        // calls super() at the beginning of your constructor."
        if (isCtor)
        {
            if (function_body.indexOf(JSSharedData.m_superCalledMarker) >= 0)
                function_body = function_body.replaceAll(JSSharedData.m_superCalledMarker, "");
            else if (!function_body.contains("goog.base(this")) // we're only looking for super(), so ignore adobe.base() case
            {
                String derivedFrom = getCurrentSuperClassName();
                if (derivedFrom != null && !isDataType(derivedFrom) && !derivedFrom.equals("Globals"))
                {
                    String superCall = indent();
                    if (JSSharedData.m_useClosureLib)
                    {
                        // superCall += "goog.base(" + JSSharedData.ROOT_NAME + getCurrentFullClassName() + " /*generateFunctionBody*/";
                        // superCall += ""goog.base(this"" + JSSharedData.THIS + " /*generateFunctionBody isCtor*/";
                        superCall += "goog.base(this";
                    }
                    else
                    {
                        if (m_convertToStatic)
                        {
                            String fullClassName = createFullClassName(false);
                            if (m_sharedData.hasClass(makeName(fullClassName)))
                            {
                                fullClassName = getCurrentSuperClassFullName();
                                superCall += createFullName(fullClassName, true) + ".init(";
                            }
                            else
                                superCall += createFullClassName(true) + "." + JSSharedData._SUPER + ".init(";
                        }
                        else
                            superCall += JSSharedData.THIS + "." + JSSharedData._SUPER + "(";
                        if (JSSharedData.m_useSelfParameter)
                            superCall += JSSharedData.THIS;
                    }
                    superCall += "); /* Call to super() was missing in ctor! */" + endl();
                    function_body = superCall + function_body;
                }
            }
        }

        result += indentBlock(function_body, 1);

        if (isCtor)
        {
            if (!result.contains(JSSharedData.THIS))
            {
                result += indentBlock("/** @type {" + createFullClassName(true) + "} */" + endl(), 1);
                result += indentBlock("var " + JSSharedData.THIS + " = this;" + endl(), 1);
            }
            result += indentBlock("return " + JSSharedData.THIS + ";" + endl(), 1);
        }

        // popIndent();
        // result += indent() + "}";
        return result;
    }

    private boolean haveAPrioriInstructions()
    {
        return aPrioriInstructions != null && !aPrioriInstructions.isEmpty();
    }

    /*
     * public JSTraits createTraitsVisitor() { return new JSTraits(); }
     */

    public String reduce_getModifier(IASNode iNode)
    {
        // Pattern getModifier
        // KeywordGetID(void);

        return null;
    }

    public String reduce_setModifier(IASNode iNode)
    {
        // Pattern setModifier
        // KeywordSetID(void);

        return null;
    }

    /*
     * ******************
     * ** Declarations ** ******************
     */

    public void prologue_typelessFunction(IASNode p, int iRule) throws java.lang.Exception
    {
        //  Note: A named nested function does 
        //  always need an activation record.
        currentScope.setNeedsActivation();
        currentScope = currentScope.pushFrame();
        currentScope.declareNestedFunction();
    }

    public void prologue_typedFunction_to_statemen(IASNode p, int iRule) throws java.lang.Exception
    {
        //  Note: A named nested function does 
        //  always need an activation record.
        currentScope.setNeedsActivation();
        currentScope = currentScope.pushFrame();
        currentScope.declareNestedFunction();
    }

    private Boolean addDependency(IDefinition fromDef, IDefinition toDef, DependencyType dt)
    {
        if (JSGeneratingReducer.isReservedDataType(fromDef.getBaseName()))
            return false;
        if (JSGeneratingReducer.isReservedDataType(toDef.getBaseName()))
            return false;

        final CompilerProject compilerProject = (CompilerProject)currentScope.getProject();
        final ASProjectScope projectScope = compilerProject.getScope();

        final ICompilationUnit fromCU = projectScope.getCompilationUnitForDefinition(fromDef);
        if (fromCU == null)
            return false;

        final ICompilationUnit toCU = projectScope.getCompilationUnitForDefinition(toDef);
        if (toCU == null)
            return false;

        if (fromCU == toCU)
            return false;

        // sharedData.verboseMessage( "Adding dependency: " + fromDef.getName() + " to " + toDef.getName() );
        compilerProject.addDependency(fromCU, toCU, dt);

        return true;
    }

    private String getJSDocForFunction(MethodInfo mi)
    {
        String s = "/** @type {function(";
        Vector<PooledValue> defaultValues = mi.getDefaultValues();
        Vector<Name> paramTypes = mi.getParamTypes();
        List<String> paramNames = mi.getParamNames();

        int nthParam = 0;
        final int defaultsStartAt = paramTypes.size() - defaultValues.size();
        final Boolean needsRest = (mi.getFlags() & ABCConstants.NEED_REST) > 0;
        final int restStartsAt = needsRest ? paramNames.size() - 1 : paramNames.size();
        Boolean emitComma = false;

        for (Name nextParam : paramTypes)
        {
            if (emitComma)
                s += ", ";
            else
                emitComma = true;

            if (nthParam == restStartsAt)
            {
                // param is rest argument
                s += "...";
            }
            else if (nextParam == null)
            {
                /*
                 * internal compiler error generated with optimize enabled
                 * compiling as3_enumerate.fla and fails to release the JS file
                 * http://watsonexp.corp.adobe.com/#bug=3047880 We have to emit
                 * something, otherwise you'd get:
                 * @type {function(, , )} as that happened in as3_enumerate
                 * MainTimeline. In the original MainTimeline.as change() is a
                 * local function with untyped parameters: function
                 * change(identifier, object, correct) which gets promoted to a
                 * method via [ConvertLocalVarsToMembers] of frame0(). It seems
                 * that there are two bugs: 1. JSDoc comments for functions with
                 * untyped parameters should use "*" instead:
                 * @type {function(*, *, *)} 2. JSDoc comments for promoted
                 * local functions should be omitted. The change below addresses
                 * #1.
                 */

                // param is untyped argument
                s += "*";
                this.m_needsSecondPass = true;
            }
            else
            {
                final IDefinition paramDef = getDefinitionForName(currentScope.getProject(), nextParam);
                if (paramDef == null)
                {
                    this.m_needsSecondPass = true;
                }

                final StringBuilder sb = new StringBuilder();
                if (!nameToJSDocType(currentScope.getProject(), nextParam, sb))
                    m_needsSecondPass = true;
                final String argType = sb.toString();
                if (nthParam < defaultsStartAt)
                {
                    // param without default value
                    s += argType;
                }
                else
                {
                    // param with default value
                    s += argType + "=";
                }
            }

            nthParam++;
        }

        if (mi.getReturnType() != null && !getBasenameFromName(mi.getReturnType()).equals("void"))
        {
            final StringBuilder sb = new StringBuilder();
            if (!nameToJSDocType(currentScope.getProject(), mi.getReturnType(), sb))
                m_needsSecondPass = true;
            s += " : " + sb.toString();
        }

        s += ")} */ ";
        return s;
    }

    private Boolean isSyntheticBinding(Binding b)
    {
        final String baseName = getBasenameFromBinding(b);

        if (!isAlphaNumString(baseName))
            return true;

        return false;
    }

    private Boolean convertLocalVarsToMembers(IASNode iNode)
    {
        if (iNode != null && m_functionNode != null && !isAnonymousFunction(getMethodName()))
        {
            final IDefinition def = m_functionNode.getDefinition();
            final IMetaTag metaTag = def.getMetaTagByName(JSSharedData.CONVERT_LOCAL_VARS_TO_MEMBERS);
            if (metaTag != null)
            {
                int countFunctionNodeParents = 0;
                IASNode parent = iNode.getParent();
                while (parent != null)
                {
                    if (parent instanceof IFunctionNode)
                    {
                        if (countFunctionNodeParents > 0)
                            return false;
                        ++countFunctionNodeParents;
                    }
                    else if (parent instanceof IClassNode)
                    {
                        return true;
                    }
                    parent = parent.getParent();
                }
                return true;
            }
        }
        return false;
    }

    public void startDeclareVariable(DefinitionBase varDef)
    {
        m_currentVariable = varDef;
        m_hasSideEffects = false;
        m_varInitValue = null;
    }

    public void endDeclareVariable(DefinitionBase varDef)
    {
        m_currentVariable = null;
    }

    private Boolean hasSideEffects(IDefinition def)
    {
        if (def != null &&
              !isExternalClass(currentScope.getProject(), def) &&
              !isDataType(def)
        /* !isDataClass(currentScope.project, def) */)
        {
            return true;
        }

        return false;
    }

    public String getVariableInitializer(DefinitionBase varDef)
    {
        if (varDef != m_currentVariable)
            currentScope.addProblem(new JSInternalCompilerProblem(varDef.getNode(), "unknown variable definition"));

        if (m_hasSideEffects)
            return null;

        return m_varInitValue;
    }

    // called by JSGenerator::generateFunction
    public void startFunction(FunctionNode func)
    {
        // DataClass classes must not declare methods.
        if (isDataClass(currentScope.getProject(), this.m_currentClassDefinition))
            currentScope.addProblem(new JSDataClassMethodError(func));

        final Boolean convertLocalVarsToMembers = convertLocalVarsToMembers(func);
        if (convertLocalVarsToMembers)
        {
            final String func_name = func.getName();
            registerConvertedMember(func_name);
        }
    }

    // called by JSGenerator::generateFunction
    public void endFunction(FunctionNode func)
    {
        if (!m_convertedMembers.isEmpty())
        {
            for (String name : m_convertedMembers)
            {
                m_nameMap.remove(name);
            }
            m_convertedMembers.clear();
        }
    }

    public void registerConvertedMember(String name)
    {
        if (!m_convertedMembers.contains(name) && !m_nameMap.containsKey(name))
        {
            m_convertedMembers.add(name);
            m_nameMap.put(name, JSSharedData.THIS + "." + name);
        }
    }

    public void registerAccessedPropertyName(IASNode iNode, String name)
    {
        m_sharedData.registerAccessedPropertyName(name);
        if (inCodeGen())
        {
            if (JSSharedData.usingAdvancedOptimizations() && !name.startsWith("\""))
                currentScope.addProblem(new JSWarnClosureAdvancedOptimizationsProblem(iNode));
            else if (JSSharedData.WARN_PERFORMANCE_LOSS || JSSharedData.WARN_RUNTIME_NAME_LOOKUP)
                currentScope.addProblem(new JSWarnRuntimeNameLookupProblem(iNode));
        }
        else
        {
            this.m_needsSecondPass = true;
        }
    }

    public void warnClassInitPerformance(IASNode iNode)
    {
        if (JSSharedData.WARN_PERFORMANCE_LOSS || JSSharedData.WARN_CLASS_INIT)
            currentScope.addProblem(new JSWarnClassInitProblem(iNode));
    }

    /**
     * Problem generated when JSSharedData.WARN_PERFORMANCE_LOSS is set and we
     * emit adobe.get/set/callProperty
     */
    public class JSWarnRuntimeNameLookupProblem extends CodegenProblem
    {
        public static final String DESCRIPTION = "PERFORMANCE: Forced to emit expensive runtime name lookup. Consider using strict types instead of generic objects and avoiding dynamic classes.";

        public static final int errCode = -1;

        public JSWarnRuntimeNameLookupProblem(IASNode site)
        {
            super(site);
        }
    }

    /**
     * Problem generated when JSSharedData.WARN_PERFORMANCE_LOSS is set and we
     * encounter any class init code.
     */
    public class JSWarnClassInitProblem extends CodegenProblem
    {
        public static final String DESCRIPTION = "PERFORMANCE: Avoid loose class initialization statements. Consider using singletons instead.";

        public static final int errCode = -1;

        public JSWarnClassInitProblem(IASNode site)
        {
            super(site);
        }
    }

    /**
     * Problem generated when JSSharedData.CLOSURE_compilation_level is set to
     * "ADVANCED_OPTIMIZATIONS" and we emit adobe.get/set/callProperty
     */
    public class JSWarnClosureAdvancedOptimizationsProblem extends CodegenProblem
    {
        public static final String DESCRIPTION = "WARNING: Optimizing with ADVANCED_OPTIMIZATIONS will fail because of accessing properties via unknown strings.";

        public static final int errCode = -1;

        public JSWarnClosureAdvancedOptimizationsProblem(IASNode site)
        {
            super(site);
        }
    }

    /**
     * Problem generated when JSSharedData.WARN_PERFORMANCE_LOSS is set and we
     * emit adobe.get/set/callProperty
     */
    public class JSDataClassMethodError extends CodegenProblem
    {
        public static final String DESCRIPTION = "ERROR: DataClass classes must not declare methods.";

        public static final int errCode = -1;

        public JSDataClassMethodError(IASNode site)
        {
            super(site);
        }
    }

    /**
     * {@link CompilerProblem} subclass for files in the source list that are
     * not supported by the project.
     */
    public class JSUnsupportedFeatureProblem extends CodegenProblem
    {
        public static final String DESCRIPTION = "Unsupported feature: ${feature}."; //$NON-NLS-1$

        public JSUnsupportedFeatureProblem(IASNode site, String featureName)
        {
            super(site);
            feature = featureName;
        }

        public final String feature;
    }

    /**
     * {@link CompilerProblem} subclass for files in the source list that are
     * not supported by the project.
     */
    public class JSInternalCompilerProblem extends CodegenProblem
    {
        public static final String DESCRIPTION = "Internal compiler error: ${message}."; //$NON-NLS-1$

        public JSInternalCompilerProblem(IASNode site, String errorMessage)
        {
            super(site);
            message = errorMessage;
        }

        public final String message;
    }

    /**
     * @return OP_unplus or OP_convert_d depending on if float support is
     * enabled or not
     */
    int op_unplus()
    {
        // This does not seem to matter for JSGeneratingReducer - each opcode has the same effect
        return OP_unplus;
    }

    public String reduce_arrayIndexExpr_to_mxmlDataBindingSetter(IASNode __p,
            String stem, String index, boolean b)
    {
        assert false : "Data binding not supported!";
        return null;
    }

    public String reduceName_to_mxmlDataBindingSetter(IASNode __p, Binding name)
    {
        assert false : "Data binding not supported!";
        return null;
    }

    public String reduceRuntimeName_to_mxmlDataBindingSetter(IASNode __p,
            RuntimeMultiname runtime_name_expression)
    {
        assert false : "Data binding not supported!";
        return null;
    }

    public String reduce_memberAccessExpr_to_mxmlDataBindingSetter(IASNode __p,
            String stem, Binding member)
    {
        assert false : "Data binding not supported!";
        return null;
    }

    public String reduce_qualifiedMemberAccessExpr_to_mxmlDataBindingSetter(
            IASNode __p, String stem, Binding qualifier, Binding member)
    {
        assert false : "Data binding not supported!";
        return null;
    }

    public String reduce_qualifiedMemberRuntimeNameExpr_to_mxmlDataBindingSetter(
            IASNode __p, String stem, Binding qualifier, String runtime_member)
    {
        assert false : "Data binding not supported!";
        return null;
    }

    public Binding reduce_parameterizedName(IASNode node, Binding base, Binding param)
    {
        return null;
    }

    public String reduce_vectorLiteral(IASNode node, Binding type_param, Vector<String> elements)
    {
        return null;
    }
}
