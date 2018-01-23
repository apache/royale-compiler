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

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Interface for the code generator.  Different code generation targets
 * (e.g. ABC bytecode or JavaScript) implement these methods to emit
 * suitable object code for their target.
 */
public interface ICodeGenerator
{
    /**
     * Generate an ABC file equivalent to the input syntax tree.
     *
     * @param synthetic_name_prefix Prefix to prepend to all synthetic names
     * @param root_node the root of the syntax tree.
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve references to definitions.
     * @throws InterruptedException
     * @return An ABCBytesRequestResult that can provide the generated code
     */
    ABCBytesRequestResult generate(String synthetic_name_prefix, IASNode root_node,
                                   ICompilerProject project) throws InterruptedException;

    /**
     * Generate an ABC file equivalent to the input syntax tree.
     *
     * @param executorService {@link ExecutorService} used by the code generator
     * when parallel code generation is enabled.
     * @param useParallelCodegen If true, some method bodies are generated in
     * background threads. If false all method bodies are generated on the
     * calling thread.
     * @param synthetic_name_prefix Prefix to prepend to all synthetic names
     * @param root_node the root of the syntax tree.
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve references to definitions.
     * @param inInvisibleCompilationUnit Indicates whether or not we are
     * generating code for an
     * {@link org.apache.royale.compiler.units.IInvisibleCompilationUnit}.
     * @param encodedDebugFiles - a mapping between the absolute path of a file, and the
     *        encoded path that is used by OP_debugfile
     * @throws InterruptedException
     * @return An ABCBytesRequestResult that can provide the generated code
     */
    ABCBytesRequestResult generate(ExecutorService executorService, boolean useParallelCodegen,
                                   String synthetic_name_prefix, IASNode root_node,
                                   ICompilerProject project, boolean inInvisibleCompilationUnit,
                                   Map<String, String> encodedDebugFiles)
        throws InterruptedException;

    /**
     * Translate an AST into ABC instructions.
     * @param subtree - the CM subtree.
     * @param goal_state - the desired goal state.
     *   One of the nonterminal states in  CmcEmitter,
     *   or 0 if you're feeling lucky and are willing
     *   to accept whatever instruction sequence the
     *   BURM decides is optimal.
     * @param scope - the active lexical scope.
     * @return a list of ABC instructions.
     */
    InstructionList generateInstructions (IASNode subtree, int goal_state, LexicalScope scope);

    /**
     * Generate code for a function declaration, and put its initialization code
     * on the relevant instruction list.
     * <p>
     * @post if instance_init_insns is not null then the method will
     * have been processed as and marked as a constructor.
     *
     * @param func the function declaration node.
     * @param enclosing_scope the lexical scope in which the function was
     * defined.
     * @param instance_init_insns a list of instance initialization instructions
     * collected outside a constructor body that must be included in the
     * constructor.
     * @return {@link MethodInfo} created for the function.
     */
    MethodInfo generateFunction(FunctionNode func, LexicalScope enclosing_scope,
                                InstructionList instance_init_insns, Name alternateName);

    /**
     * Generate code for a function declaration, using a background thread
     * provided by the specified {@link ExecutorService}.
     *
     * @param executorService {@link ExecutorService} used to do work in other
     * threads.
     * @param func the function declaration node.
     * @param enclosing_scope the lexical scope in which the function was
     * defined.
     * @return {@link GenerateFunctionInParallelResult} which can be used to
     * wait for code generation of the specified function to complete and to
     * extract the {@link MethodInfo} created for the specified function. The
     * {@link MethodInfo} may be extracted immediately after this method
     * completes ( you don't have to wait for code generation of the specified
     * function complete ).
     */
    GenerateFunctionInParallelResult generateFunctionInParallel(ExecutorService executorService,
                                                                FunctionNode func,
                                                                LexicalScope enclosing_scope);

    /**
     * Helper method used by mxml databinding codegen to emit an anonymous
     * function used by an mxml data binding destination function. Example:
     * <p>
     * If the expression node is a.b.c, this method will generate a funtion
     * whose source would look something like this:
     *
     * <pre>
     * function (arg:*):void { a.b.c = arg; }
     * </pre>
     *
     * @param mi - the MethodInfo describing the signature
     * @param setterExpression {@link IExpressionNode} that is the destination
     * expression of a mxml data binding.
     * @param enclosing_scope {@link LexicalScope} for the class initializer
     * that encloses the function being generated.
     */
    void generateMXMLDataBindingSetterFunction(MethodInfo mi, IExpressionNode setterExpression,
                                               LexicalScope enclosing_scope);

    /**
     * Helper method used by databinding codegen to emit an anonymous function
     * based on a list of {@link IExpressionNode}'s. This method emits a
     * function that contains code that evaluates each expression in the list
     * and adds the expressions together with {@link org.apache.royale.abc.ABCConstants#OP_add}.
     *
     * @param mi - the MethodInfo describing the signature
     * @param nodes - a {@link List} of {@link IExpressionNode}'s to be
     * codegen'd.
     * @param enclosing_scope {@link LexicalScope} for the class initializer
     * that encloses the function being generated.
     */
    void generateMXMLDataBindingGetterFunction(MethodInfo mi, List<IExpressionNode> nodes,
                                               LexicalScope enclosing_scope);

    /**
     * Creates a MethodInfo specifying the signature of a method
     * declared by a FunctionNode.
     * @param func - A FunctionNode representing a method declaration.
     * @return The MethodInfo specifying the signature of the method.
     */
    MethodInfo createMethodInfo (LexicalScope scope, FunctionNode func, Name alternate_name);

    /**
     * Creates a MethodInfo specifying the signature of a method
     * declared by a FunctionNode, and adds in the information for any
     * default argument values.
     * 
     * @param func - A FunctionNode representing a method declaration.
     * @return The MethodInfo specifying the signature of the method.
     */
    MethodInfo createMethodInfoWithDefaultArgumentValues(LexicalScope scope, FunctionNode func);

    /**
     * Helper method used by <code>generateFunction()</code> (and also by
     * <code>generateEventHandler()</code> in MXMLDocumentDirectiveProcessor).
     * @param mi - the MethodInfo describing the signature
     * @param node - the FunctionNode or MXMLEventSpecifierNode.
     * @param enclosing_scope - the lexical scope
     *    in which the handler method is autogenerated.
     * @param instance_init_insns - a list of instance
     *    initialization instructions collected outside
     *    a constructor body that must be included in the
     *    constructor.
     * @post if instance_init_insns is not null then the
     *    method will have been processed as and marked
     *    as a constructor.
     */
    void generateMethodBodyForFunction(MethodInfo mi, IASNode node,
                                       LexicalScope enclosing_scope,
                                       InstructionList instance_init_insns);

    /**
     * Helper method to expose the constant folding code to clients outside of the burm, such
     * as org.apache.royale.compiler.internal.as.definitions.ConstantDefinition.
     * @param subtree   the tree to generate a constant value for
     * @param project   the project to use to evaluate the tree
     * @return          the constant value for the subtree, or null if a constant value can't be determined
     */
    IConstantValue generateConstantValue(IASNode subtree, ICompilerProject project);

    /**
     *  Reduce an AST to its equivalent ABC structures.
     *  @param subtree - the root of the AST subtree.
     *    May be null, in which case this routine returns null.
     *  @param scope - the active LexicalScope.
     *  @param goal - the BURM's goal state.  One of the CmcEmitter.__foo_NT constants.
     *  @return the result of reducing the subtree to the desired goal state,
     *    or null if the input subtree was null.
     *  @throws Exception from the BURM if the computation didn't succeed or was interrupted.
     */
    Object reduceSubtree(IASNode subtree, LexicalScope scope, int goal) throws Exception;
    
    /**
     * Represents the result of {@link #generateConstantValue}(}.
     * <p>
     * In addition to producing the constant value itself,
     * the constant reduction process can also produce compiler problems.
     */
    static interface IConstantValue
    {
        /**
         * The value produced by the constant reduction process.
         */
        Object getValue();
        
        /**
         * The compiler problems produced by the constant reduction process.
         */
        Collection<ICompilerProblem> getProblems();
    }
}
