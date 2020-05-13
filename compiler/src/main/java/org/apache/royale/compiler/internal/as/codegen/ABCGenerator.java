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

import static org.apache.royale.abc.ABCConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.IVisitor;
import org.apache.royale.compiler.embedding.IEmbedData;
import org.apache.royale.compiler.exceptions.BURMAbortException;
import org.apache.royale.compiler.exceptions.CodegenInterruptedException;
import org.apache.royale.compiler.exceptions.MissingBuiltinException;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnitFactory;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.problems.CodegenInternalProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MissingBuiltinProblem;
import org.apache.royale.compiler.problems.NonConstantParamInitializerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import com.google.common.util.concurrent.Futures;
import org.apache.royale.utils.ASTUtil;

/**
 * ABCGenerator is the public interface to the code generator.
 */
public class ABCGenerator implements ICodeGenerator
{
    private static boolean DEFINITION_NORMALIZATION_DISABLED = false;
    
    @Override
    public ABCBytesRequestResult generate (String synthetic_name_prefix, IASNode root_node, ICompilerProject project) throws InterruptedException
    {
        return generate(null, false, synthetic_name_prefix, root_node, project, DEFINITION_NORMALIZATION_DISABLED, Collections.<String, String>emptyMap());
    }

    @Override
	public ABCBytesRequestResult generate(ExecutorService executorService, boolean useParallelCodegen,
	                                      String synthetic_name_prefix, IASNode root_node,
	                                      ICompilerProject project, boolean inInvisibleCompilationUnit,
	                                      Map<String, String> encodedDebugFiles)
        throws InterruptedException
	{
        //  Set up the global lexical scope.
        final GlobalLexicalScope global_scope = new GlobalLexicalScope(
            project, this, synthetic_name_prefix, inInvisibleCompilationUnit, useParallelCodegen, encodedDebugFiles);
        
	    final ABCEmitter emitter = (ABCEmitter)global_scope.getEmitter();

        // CG targets the latest version - these ABCs can be postprocessed to downgrade to previous versions
        emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);

        IScriptVisitor sv = emitter.visitScript();
        sv.visit();
        MethodInfo init_method = new MethodInfo();
        sv.visitInit(init_method);
        
        MethodBodyInfo init_body = new MethodBodyInfo();
        init_body.setMethodInfo(init_method);
        
        IMethodVisitor mv = emitter.visitMethod(init_method);
        mv.visit();
        IMethodBodyVisitor mbv = mv.visitBody(init_body);
        mbv.visit();
        
        global_scope.traitsVisitor = sv.visitTraits();
        global_scope.setMethodInfo(init_method);
        global_scope.methodBodyVisitor = mbv;
        global_scope.setInitialControlFlowRegionNode(root_node);

        //  Process global directives.
        GlobalDirectiveProcessor top_level_processor = new GlobalDirectiveProcessor(executorService, useParallelCodegen, global_scope, emitter);
        boolean fatal_error_encountered = false;
        try
        {
            top_level_processor.traverse(root_node);
        }
        catch (MissingBuiltinException e)
        {
            global_scope.addProblem(new MissingBuiltinProblem(root_node, e.getBuiltinName()));
            fatal_error_encountered = true;
        }
        catch (CodegenInterruptedException e)
        {
            //  Unwrap the InterruptedException and rethrow it.
            throw e.getException();
        }

        top_level_processor.finish();
        
        byte[] generatedBytes = IABCBytesRequestResult.ZEROBYTES;
        
        if ( !fatal_error_encountered )
        {
            //  Initialize the init script.
            InstructionList script_init_insns = new InstructionList();

            script_init_insns.addInstruction(OP_getlocal0);
            script_init_insns.addInstruction(OP_pushscope);

            script_init_insns.addAll(global_scope.getInitInstructions());
            script_init_insns.addAll(top_level_processor.directiveInsns);

            if ( script_init_insns.canFallThrough() || script_init_insns.hasPendingLabels() )
                script_init_insns.addInstruction(OP_returnvoid);

            //  Allocate temps beginning with register 1,
            //  register 0 is reserved for "this" global.
            global_scope.initializeTempRegisters(1);

            // Make any vistEnd method calls
            // that were deferred.
            // callVisitEnds must be called on the same thread
            // that original called ABCGenerator.generate ( this method ).
            global_scope.callVisitEnds();
            
            mbv.visitInstructionList(script_init_insns);
            mbv.visitEnd();
            mv.visitEnd();
            global_scope.traitsVisitor.visitEnd();
            sv.visitEnd();

            try
            {
                generatedBytes = emitter.emit();
            } 
            catch ( Exception e )
            {
                global_scope.addProblem(new CodegenInternalProblem(root_node, e));
            }
        }

        Set<EmbedData> embeds = global_scope.getEmbeds();
        EmbedCompilationUnitFactory.collectEmbedDatas(project, (IFileNodeAccumulator)root_node, embeds, global_scope.getProblems());

        Set<IEmbedData> iembeds = new HashSet<IEmbedData>();
        for (EmbedData embed : embeds)
            iembeds.add(embed);
        ICompilerProblem[] problemsArray = global_scope.getProblems().toArray(IABCBytesRequestResult.ZEROPROBLEMS);
        return new ABCBytesRequestResult(generatedBytes, problemsArray, iembeds);
	}

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
	public InstructionList generateInstructions (IASNode subtree, int goal_state, LexicalScope scope)
	{
	    return generateInstructions(subtree, goal_state, scope, null);
	}

    /**
	 * Translate an AST into ABC instructions.
	 * @param subtree - the CM subtree.
	 * @param goal_state - the desired goal state.
	 *   One of the nonterminal states in  CmcEmitter,
	 *   or 0 if you're feeling lucky and are willing
	 *   to accept whatever instruction sequence the
	 *   BURM decides is optimal.
	 * @param scope - the active lexical scope.
     * @param instance_init_insns - a list of instance
     *    initialization instructions collected outside
     *    a constructor body that must be included in the
     *    constructor.
     * @post if instance_init_insns is not null then the
     *    method will have been processed as and marked
     *    as a constructor.
	 * @return a list of ABC instructions.
	 */
	public InstructionList generateInstructions(IASNode subtree, int goal_state, LexicalScope scope, InstructionList instance_init_insns)
	{
	    CmcEmitter burm = new CmcEmitter();
        burm.reducer = new ABCGeneratingReducer();
	    burm.reducer.setCurrentscope(scope);
	    burm.reducer.setInstanceInitializers(instance_init_insns);

		try {
			burm.burm(subtree, goal_state);
			return ((InstructionList)burm.getResult());
		} 
		catch ( Exception cant_reduce) {
			handleBurmError(burm, subtree, cant_reduce, scope);
			return new InstructionList();
		}
	}
	

    /**
     * Generate code for a function declaration, and put its initialization code
     * on the relevant instruction list.
     * <p>
     * Post condition: if instance_init_insns is not null then the method will
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
    public MethodInfo generateFunction (FunctionNode func, LexicalScope enclosing_scope, InstructionList instance_init_insns, Name alternate_name)
    {
        MethodInfo mi = createMethodInfo(enclosing_scope, func, alternate_name);
        if (mi.isNative())
        {
            generateNativeMethod(func, mi, enclosing_scope);
        }
        else
        {
            generateMethodBodyForFunction(mi, func, enclosing_scope, instance_init_insns);
        }

        func.discardFunctionBody();
        return mi;
    }
    
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
    public GenerateFunctionInParallelResult generateFunctionInParallel (ExecutorService executorService, FunctionNode func, LexicalScope enclosing_scope)
    {
        MethodInfo mi = createMethodInfo(enclosing_scope, func, null);
        if (mi.isNative())
        {
            generateNativeMethod(func, mi, enclosing_scope);
            return new GenerateFunctionInParallelResult(Futures.immediateFuture(null), mi, Collections.<IVisitor>emptyList());
        }
        GenerateFunctionRunnable runnable = new GenerateFunctionRunnable(mi, func, enclosing_scope);
        Future<?> future = executorService.submit(runnable);
        return new GenerateFunctionInParallelResult(future, mi, runnable.getDeferredVisitEndsList());
    }
    
    /**
     * Helper method used by <code>generateFunction()</code>.
     * @param func - the function declaration node.
     * @param mi - the MethodInfo describing the signature
     * @param enclosing_scope - the lexical scope
     *    in which the handler method is autogenerated.
     */
    void generateNativeMethod(FunctionNode func, MethodInfo mi,
                                     LexicalScope enclosing_scope)
    {
        enclosing_scope.getMethodBodySemanticChecker().checkNativeMethod(func);

        // don't need to create a new scope, so just use the enclosing scope
        // to get a handle to the emitter
        IMethodVisitor mv = enclosing_scope.getEmitter().visitMethod(mi);

        // Just visit the method info.  Do NOT generate a body
        // for native methods
        mv.visit();

        // For non native methods, the return type is set by the burm,
        // but for native types, as the burm isn't run, we need to set
        // the return type here.
        String returnType = func.getReturnType();
        mi.setReturnType(new Name(returnType));
        
        mv.visitEnd();
    }

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
    public void generateMethodBodyForFunction(MethodInfo mi, IASNode node,
            LexicalScope enclosing_scope,
            InstructionList instance_init_insns)
    {
        List<IVisitor> deferredVisitEnds = new LinkedList<IVisitor>();
        generateMethodBodyForFunction(deferredVisitEnds, mi, node, enclosing_scope, instance_init_insns);
        for (IVisitor v : deferredVisitEnds)
            v.visitEnd();
    }
    
    private void generateMethodBodyForFunction(List<IVisitor> deferredVisitEnds,
            MethodInfo mi, IASNode node,
            LexicalScope enclosing_scope,
            InstructionList instance_init_insns)
    {
        assert node != null;
        final boolean is_constructor =  SemanticUtils.isInConstructor(node);
        //  Set up a lexical scope for this function.
        LexicalScope function_scope = enclosing_scope.pushFrame();

        //  If instance_init_insns is not null, then the new
        //  scope needs to assume ownership of the initializers'
        //  data, such as hasnext2 instruction initializers.
        if ( instance_init_insns != null )
        {
            function_scope.transferInitializerData();
        }
        
        IMethodVisitor mv = function_scope.getEmitter().visitMethod(mi);
        mv.visit();
        
        MethodBodyInfo mbi = new MethodBodyInfo();
        mbi.setMethodInfo(mi);
        
        IMethodBodyVisitor mbv = mv.visitBody(mbi);
        mbv.visit();
        
        function_scope.methodBodyVisitor = mbv;
        function_scope.traitsVisitor = mbv.visitTraits();
        function_scope.setMethodInfo(mi);
        
        if ( is_constructor )
            function_scope.getMethodBodySemanticChecker().enterConstructor();
        
        InstructionList insns = null;
         
        if (node instanceof IFunctionNode)
            function_scope.setInitialControlFlowRegionNode(((IFunctionNode)node).getScopedNode());
        else
            function_scope.setInitialControlFlowRegionNode((IASNode)node);
        // If we are passed a single node, generate its instructions
        insns = generateInstructions((IASNode)node, CmcEmitter.__function_NT, function_scope, instance_init_insns);
         
        if ( is_constructor )
            function_scope.getMethodBodySemanticChecker().leaveConstructor();
         
        assert insns != null;
        
        
        // Make any vistEnd method calls
        // that were deferred.
        // callVisitEnds must be called on the same thread
        // that started code generation.
        function_scope.addVisitEndsToList(deferredVisitEnds);
        
        mbv.visitInstructionList(insns);
        deferredVisitEnds.add(function_scope.traitsVisitor);
        deferredVisitEnds.add(mbv);
        deferredVisitEnds.add(mv);

        //  Perform semantic checks that require the control flow graph.
        function_scope.getMethodBodySemanticChecker().checkControlFlow( node, mi, mbi );
    }
    
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
    public void generateMXMLDataBindingSetterFunction (MethodInfo mi, IExpressionNode setterExpression, LexicalScope enclosing_scope)
    {
        IMethodVisitor methodVisitor = enclosing_scope.getEmitter().visitMethod(mi);
        methodVisitor.visit();
        MethodBodyInfo methodBodyInfo = new MethodBodyInfo();
        methodBodyInfo.setMethodInfo(mi);
        IMethodBodyVisitor methodBodyVisitor = methodVisitor.visitBody(methodBodyInfo);
        methodBodyVisitor.visit();
        
        //  Set up a lexical scope for this function.
        LexicalScope function_scope = enclosing_scope.pushFrame();
        
        function_scope.methodBodyVisitor = methodBodyVisitor;
        function_scope.traitsVisitor = methodBodyVisitor.visitTraits();
        function_scope.setMethodInfo(mi);
        

        InstructionList functionBody;
        if (setterExpression instanceof InstructionListNode)
            functionBody = ((InstructionListNode)setterExpression).getInstructions();
        else
            functionBody = generateInstructions(setterExpression, CmcEmitter.__mxml_data_binding_setter_expression_NT, function_scope, null);
        
        functionBody.addInstruction(OP_returnvoid);
        
        methodBodyVisitor.visitInstructionList(functionBody);
        methodBodyVisitor.visitEnd();
        methodVisitor.visitEnd();
    }
    
    /**
     * Helper method used by databinding codegen to emit an anonymous function
     * based on a list of {@link IExpressionNode}'s. This method emits a
     * function that contains code that evaluates each expression in the list
     * and adds the expressions together with {@link ABCConstants#OP_add}.
     * 
     * @param mi - the MethodInfo describing the signature
     * @param nodes - a {@link List} of {@link IExpressionNode}'s to be
     * codegen'd.
     * @param enclosing_scope {@link LexicalScope} for the class initializer
     * that encloses the function being generated.
     */
     public void generateMXMLDataBindingGetterFunction (MethodInfo mi, List<IExpressionNode> nodes,
                                                        LexicalScope enclosing_scope)
     {
         IMethodVisitor methodVisitor = enclosing_scope.getEmitter().visitMethod(mi);
         methodVisitor.visit();
         MethodBodyInfo methodBodyInfo = new MethodBodyInfo();
         methodBodyInfo.setMethodInfo(mi);
         IMethodBodyVisitor methodBodyVisitor = methodVisitor.visitBody(methodBodyInfo);
         methodBodyVisitor.visit();
         
         //  Set up a lexical scope for this function.
         LexicalScope function_scope = enclosing_scope.pushFrame();
         
         function_scope.methodBodyVisitor = methodBodyVisitor;
         function_scope.traitsVisitor = methodBodyVisitor.visitTraits();
         function_scope.setMethodInfo(mi);

         InstructionList functionBody = null;
         // for a list of nodes, generate all their instructions and add the results together.
         // typically we are doing this to concatenate strings
         for (IExpressionNode expressionNode : nodes)
         {
             InstructionList instructionsForExpression = generateInstructions(expressionNode, CmcEmitter.__expression_NT, function_scope, null);
             if (functionBody == null)
             {
                 // First one in the list makes a new IL and puts
                 // instructions into it
                 functionBody = instructionsForExpression;
             }
             else
             {
                 // successive children generate into the same IL, then add the results
                 functionBody.addAll(instructionsForExpression);
                 functionBody.addInstruction(OP_add);
             }
         }
         
         functionBody.addInstruction(OP_returnvalue);
         
         methodBodyVisitor.visitInstructionList(functionBody);
         function_scope.traitsVisitor.visitEnd();
         methodBodyVisitor.visitEnd();
         methodVisitor.visitEnd();
    }
    /**
     * Creates a MethodInfo specifying the signature of a method
     * declared by a FunctionNode.
     * @param func - A FunctionNode representing a method declaration.
     * @return The MethodInfo specifying the signature of the method.
     */
    @Override
    public MethodInfo createMethodInfo (LexicalScope scope, FunctionNode func, Name alternate_name)
    {	
        return createMethodInfoWithOptionalDefaultArgumentValues(scope, func, false, alternate_name);
    }
    
    /**
     **
     * Creates a MethodInfo specifying the signature of a method
     * declared by a FunctionNode, and adds in the information for any
     * default argument values.
     * 
     * @param func - A FunctionNode representing a method declaration.
     * @return The MethodInfo specifying the signature of the method.
     * 
     * Will generate a compiler problem is the default value is bad
     */
    @Override
    public MethodInfo createMethodInfoWithDefaultArgumentValues (LexicalScope scope, FunctionNode func)
    {   
        return createMethodInfoWithOptionalDefaultArgumentValues(scope, func, true, null);
    }
    
    private static MethodInfo createMethodInfoWithOptionalDefaultArgumentValues(LexicalScope scope, FunctionNode func, 
                                        boolean addDefalutValues, Name alternate_name)
    {
        MethodInfo mi = new MethodInfo();
        mi.setMethodName(alternate_name != null ? alternate_name.getBaseName() : func.getName());

        FunctionDefinition funcDef = func.getDefinition();
        //  Marshal the function's arguments.
        ParameterDefinition[] args = funcDef.getParameters();
        List<String> param_names = new ArrayList<String>();

        ICompilerProject project = scope.getProject();
        if ( args.length > 0 )
        {
        	Vector<Name> method_args = new Vector<Name>();
        	for ( ParameterDefinition arg: args)
        	{
                TypeDefinitionBase arg_type = (TypeDefinitionBase)arg.resolveType(project);
                Name type_name = arg_type != null ? arg_type.getMName(project) : null;

        		if ( arg.isRest() )
                {
        		    mi.setFlags( (byte)(mi.getFlags() | ABCConstants.NEED_REST) );
                }
        		else
        		{
                    method_args.add(type_name);
                    param_names.add(arg.getBaseName());
        	    }
        	
        		// If appropriate, tell the MethodInfo about the default parameter value
        		if (addDefalutValues && arg.hasDefaultValue())
        		{
        		    Object initValue = arg.resolveInitialValue(project);
        		    
        		    // init value might resolve to null, if the source code is bad
        		    if (initValue == null)
        		    {
        		        IParameterNode paramNode = arg.getNode();
        		        scope.addProblem(new NonConstantParamInitializerProblem(paramNode.getAssignedValueNode()));
        	            // re-write non-constant expression to undefined, so resulting ABC will pass the verifier.
        	            initValue = ABCConstants.UNDEFINED_VALUE;
        		    }
        		    mi.addDefaultValue(new PooledValue(initValue));
        		}
            }
        	mi.setParamTypes(method_args);
            mi.setParamNames(param_names);
        }

        // check for native modifier
        if ( func.getDefinition().isNative() )
        {
            mi.setFlags( (byte)(mi.getFlags() | ABCConstants.NATIVE) );
        }

        // The return type will be set by the BURM.
        
        return mi;
    }

    /**
     * Helper method to expose the constant folding code to clients outside of the burm, such
     * as org.apache.royale.compiler.internal.as.definitions.ConstantDefinition.
     * @param subtree   the tree to generate a constant value for
     * @param project   the project to use to evaluate the tree
     * @return          the constant value for the subtree, or null if a constant value can't be determined
     */
    @Override
    public IConstantValue generateConstantValue (IASNode subtree, ICompilerProject project)
    {
        IConstantValue result = null;

        LexicalScope scope = new GlobalLexicalScope(project, this);

        if ( subtree != null )
        {
            try
            {
                Object value = reduceSubtree(subtree, scope, CmcEmitter.__constant_value_NT);
                result = new ConstantValue(value, scope.getProblems());
            }
            catch ( Exception cant_reduce)
            {
                // Can't generate a constant value, just return null
            	// Do not add a problem for this exception as it could be a non-constant
            	// initializer like: var foo = new Date();
            }
        }

        return result;
    }

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
    public Object reduceSubtree (IASNode subtree, LexicalScope scope, int goal)
    throws Exception
    {
        CmcEmitter burm = new CmcEmitter();
        burm.reducer = new ABCGeneratingReducer();
        burm.reducer.setCurrentscope(scope);

        burm.burm(subtree, CmcEmitter.__constant_value_NT);
        return burm.getResult();
    }

	/**
	 * Handle an error from a BURM: emit diagnostics and bump the error count.
	 * @param n - the subtree that was to be reduced.
	 * @param ex - the exception.
	 */
    private void handleBurmError(CmcEmitter burm, IASNode n, Exception ex, LexicalScope scope)
	{
	    if ( ex instanceof CodegenInterruptedException )
	    {
	        // If the exception is an InterruptedException, do nothing, as not
	        // a real error.  The incremental flow kicked in and interrupted
	        // the current work, so just throw away the current work and carry
	        // on our merry way.
	        // No problem should be reported in this case.
	        scope.getProblems().clear();
	        return;
	    }
	    else if ( ! ( ex instanceof BURMAbortException ) )
        {
            scope.addProblem( new CodegenInternalProblem(n, ex) );
        }
	    DumpBURMState.dump(burm, n);
	}

    /**
     * Implementation of {@link Runnable} that is used to code generate a
     * function on a background thread.
     */
    private class GenerateFunctionRunnable implements Runnable
    {
        
        GenerateFunctionRunnable(MethodInfo methodInfo, FunctionNode func, LexicalScope enclosing_scope)
        {
            this.methodInfo = methodInfo;
            this.functionNode = func;
            this.enclosingScope = enclosing_scope;
            this.deferredVisitEnds = new LinkedList<IVisitor>();
        }
        
        private final MethodInfo methodInfo;
        private final FunctionNode functionNode;
        private final LexicalScope enclosingScope;
        private final List<IVisitor> deferredVisitEnds;
        
        @Override
        public void run()
        {
            assert !methodInfo.isNative() : "Native methods should be handled in the main thread and not be dispatched to a background thread!";
            functionNode.parseFunctionBody(enclosingScope.getProblems());
            ASTUtil.processFunctionNode(functionNode, enclosingScope.getProject());
            generateMethodBodyForFunction(deferredVisitEnds, methodInfo, functionNode, enclosingScope, null);
        }
        
        public List<IVisitor> getDeferredVisitEndsList()
        {
            return deferredVisitEnds;
        }
    }

    /**
     * Get an ICodeGeneratorFactory that will always return the same ABCGenerator instance
     */
    public static ICodeGeneratorFactory getABCGeneratorFactory()
    {
        return new ICodeGeneratorFactory()
        {
            public ICodeGenerator get ()
            {
                return new ABCGenerator();
            }
        };
    }
    
    /**
     * Represents the result of {@link #generateConstantValue}(}.
     * <p>
     * In addition to producing the constant value itself,
     * the constant reduction process can also produce compiler problems.
     */
    public static final class ConstantValue implements IConstantValue
    {
        public ConstantValue(Object value, Collection<ICompilerProblem> problems)
        {
            this.value = value;
            this.problems = problems;
        }
        
        private final Object value;
        private final Collection<ICompilerProblem> problems;
        
        @Override
        public Object getValue()
        {
            return value;
        }

        @Override
        public Collection<ICompilerProblem> getProblems()
        {
            return problems;
        }
    }
}
