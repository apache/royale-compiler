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

import static org.apache.flex.abc.ABCConstants.OP_add;
import static org.apache.flex.abc.ABCConstants.OP_getlocal0;
import static org.apache.flex.abc.ABCConstants.OP_pushscope;
import static org.apache.flex.abc.ABCConstants.OP_returnvoid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.flex.abc.ABCConstants;
import org.apache.flex.abc.instructionlist.InstructionList;
import org.apache.flex.abc.semantics.MethodBodyInfo;
import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.visitors.IMethodBodyVisitor;
import org.apache.flex.abc.visitors.IMethodVisitor;
import org.apache.flex.abc.visitors.IScriptVisitor;
import org.apache.flex.compiler.definitions.references.IReference;
import org.apache.flex.compiler.exceptions.BURMAbortException;
import org.apache.flex.compiler.exceptions.CodegenInterruptedException;
import org.apache.flex.compiler.exceptions.MissingBuiltinException;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.ParameterDefinition;
import org.apache.flex.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.flex.compiler.internal.embedding.EmbedData;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.testing.NodesToXMLStringFormatter;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.flex.compiler.problems.CodegenInternalProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.MissingBuiltinProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.ICompilationUnit.Operation;
import org.apache.flex.compiler.units.requests.IABCBytesRequestResult;

/**
 * ABCGenerator is the public interface to the code generator.
 */

/**
 * JSGenerator is modeled after from ABCGenerator and called by
 * JSCompilationUnit. JSGenerator owns the JSSharedData singleton. Ideally
 * JSGenerator and ABCGenerator should be derived from the same base class, i.e.
 * Generator. Some of the code in JSGenerator and ABCGenerator could be shared
 * if Generator used a burm factory in generateInstructions(). ABCGenerator
 * creates and uses a CmcEmitter, while JSGenerator uses a CmcJSEmitter. This
 * implementation is part of FalconJS. For more details on FalconJS see
 * org.apache.flex.compiler.JSDriver
 */
public class JSGenerator
{
    public ICompilationUnit m_compilationUnit = null;
    private Boolean m_needsSecondPass = false;
    private CmcJSEmitter m_cmcJSEmitter = null;
    private JSGeneratingReducer m_burm = null;
    private ICompilationUnit.Operation m_buildPhase = Operation.GET_ABC_BYTES;
    private JSEmitter m_emitter = null;

    public JSGenerator()
    {
        m_cmcJSEmitter = JSSharedData.backend.createCmcJSEmitter();
        m_cmcJSEmitter.reducer = JSSharedData.backend.createReducer();
        m_burm = m_cmcJSEmitter.reducer;
    }

    // If JSEmitter.needsSecondPass() returns true, JSGenerator.generate() will return null during scanning, 
    // which will result in JSCompilationUnit::handleSemanticProblemsRequest not caching any abcBytes for 
    // handleABCBytesRequest. The net result is that JSGenerator.generate() will be called again in handleABCBytesRequest. 
    // This mechanic will ensure selective two-pass compilation. 
    public Boolean needsSecondPass()
    {
        return m_needsSecondPass;
    }

    /*
     * There are currently a lot of problems with the DependencyGraph: -
     * ImportNode::resolveRefs() has not been implemented. - import and
     * expression dependencies are not being recognized. If
     * useOwnDependencyGraph() returns true we use our own (old) DependencyGraph
     * implementation that used to drive the while loop in
     * JSGenerator.generate() using nextJS. - JSDriver does a second pass -
     * JSGlobalDirectiveProcessor::processImportDirective() registers imported
     * classes.
     */
    public static Boolean useOwnDependencyGraph()
    {
        // return true;
        return false;
    }

    /**
     * Generate an ABC file equivalent to the input syntax tree.
     * 
     * @param synthetic_name_prefix Prefix to prepend to all synthetic names
     * @param root_node the root of the syntax tree.
     * @param project {@link ICompilerProject} whose symbol table is used to
     * resolve references to definitions.
     * @throws InterruptedException
     */
    @SuppressWarnings("nls")
    public ABCBytesRequestResult generate(String synthetic_name_prefix, IASNode root_node, ICompilerProject project) throws InterruptedException
    {
        m_needsSecondPass = false;
        m_emitter = JSSharedData.backend.createEmitter(m_buildPhase, project);
        m_emitter.visit(ABCConstants.VERSION_ABC_MAJOR_FP10, ABCConstants.VERSION_ABC_MINOR_FP10);

        IScriptVisitor sv = m_emitter.visitScript();
        sv.visit();
        MethodInfo init_method = new MethodInfo();
        sv.visitInit(init_method);

        MethodBodyInfo init_body = new MethodBodyInfo();
        init_body.setMethodInfo(init_method);

        IMethodVisitor mv = m_emitter.visitMethod(init_method);
        IMethodBodyVisitor mbv = mv.visitBody(init_body);
        mbv.visit();

        //  Set up the global lexical scope.
        final LexicalScope global_scope = new GlobalLexicalScope(project, null, synthetic_name_prefix, m_emitter);
        global_scope.traitsVisitor = sv.visitTraits();
        global_scope.setMethodInfo(init_method);
        global_scope.methodBodyVisitor = mbv;

        //  Process global directives.
        GlobalDirectiveProcessor top_level_processor = JSSharedData.backend.createGlobalDirectiveProcessor(this, global_scope, m_emitter);
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

        byte[] generatedBytes = IABCBytesRequestResult.ZEROBYTES;

        if (!fatal_error_encountered)
        {
            //  Initialize the init script.
            InstructionList script_init_insns = new InstructionList();

            script_init_insns.addInstruction(OP_getlocal0);
            script_init_insns.addInstruction(OP_pushscope);

            script_init_insns.addAll(global_scope.getInitInstructions());
            script_init_insns.addAll(top_level_processor.directiveInsns);

            if (script_init_insns.canFallThrough() || script_init_insns.hasPendingLabels())
                script_init_insns.addInstruction(OP_returnvoid);

            //  Allocate temps beginning with register 1,
            //  register 0 is reserved for "this" global.
            global_scope.initializeTempRegisters(1);

            mbv.visitInstructionList(script_init_insns);
            mbv.visitEnd();
            mv.visitEnd();
            sv.visitEnd();

            try
            {
                generatedBytes = m_emitter.emit();
            }
            catch (Throwable cant_generate)
            {
                global_scope.addProblem(new CodegenInternalProblem(root_node, cant_generate));
            }
        }

        ICompilerProblem[] problemsArray = global_scope.getProblems().toArray(IABCBytesRequestResult.ZEROPROBLEMS);
        return new ABCBytesRequestResult(generatedBytes, problemsArray, Collections.<EmbedData> emptySet());
    }

    /**
     * Translate an AST into ABC instructions.
     * 
     * @param subtree - the CM subtree.
     * @param goal_state - the desired goal state. One of the nonterminal states
     * in CmcJSEmitter, or 0 if you're feeling lucky and are willing to accept
     * whatever instruction sequence the BURM decides is optimal.
     * @param scope - the active lexical scope.
     * @return a list of ABC instructions.
     */
    public InstructionList generateInstructions(IASNode subtree, int goal_state, LexicalScope scope)
    {
        return generateInstructions(subtree, goal_state, scope, null);
    }

    /**
     * Translate an AST into ABC instructions.
     * 
     * @param subtree - the CM subtree.
     * @param goal_state - the desired goal state. One of the nonterminal states
     * in CmcJSEmitter, or 0 if you're feeling lucky and are willing to accept
     * whatever instruction sequence the BURM decides is optimal.
     * @param scope - the active lexical scope.
     * @param instance_init_insns - a list of instance initialization
     * instructions collected outside a constructor body that must be included
     * in the constructor.
     * @post if instance_init_insns is not null then the method will have been
     * processed as and marked as a constructor.
     * @return a list of ABC instructions.
     */
    public InstructionList generateInstructions(IASNode subtree, int goal_state, LexicalScope scope, InstructionList instance_init_insns)
    {
        m_burm.setCurrentscope(scope);
        m_burm.setInstanceInitializers(instance_init_insns);
        m_burm.setAprioriinstructions(instance_init_insns);
        m_burm.setFunctionNode(subtree);

        InstructionList list = new InstructionList();

        try
        {
            m_cmcJSEmitter.burm(subtree, goal_state);

            // TODO: cmcJSEmitter.getResult() now returns a String, which needs to be wrapped into an InstructionList
            // return ((InstructionList)cmcJSEmitter.getResult());
            list.addInstruction(JSSharedData.OP_JS, m_cmcJSEmitter.getResult());

            // If JSEmitter.needsSecondPass() returns true, JSGenerator.generate() will return null during scanning, 
            // which will result in JSCompilationUnit::handleSemanticProblemsRequest not caching any abcBytes for 
            // handleABCBytesRequest. The net result is that JSGenerator.generate() will be called again in handleABCBytesRequest. 
            // This mechanic will ensure selective two-pass compilation. 
            if (m_burm.needsSecondPass())
            {
                m_needsSecondPass = true;
            }
        }
        catch (Exception cant_reduce)
        {
            handleBurmError(m_cmcJSEmitter, subtree, cant_reduce, scope);
        }

        return list;
    }

    /**
     * Generate code for a function declaration, and put its initialization code
     * on the relevant instruction list.
     * 
     * @param func - the function declaration node.
     * @param enclosing_scope - the lexical scope in which the function was
     * defined.
     * @param a_priori_insns - instructions generated by an enclosing subsystem
     * that should be included in the function (e.g., a constructor needs a
     * priori instructions to initialize instance vars).
     */
    public MethodInfo generateFunction(FunctionNode func, LexicalScope enclosing_scope, InstructionList a_priori_insns)
    {
        m_burm.setCurrentscope(enclosing_scope);
        MethodInfo mi = createMethodInfo(m_burm, m_emitter, enclosing_scope, func);
        if (mi.isNative())
        {
            generateNativeMethod(func, mi, enclosing_scope);
        }
        else
        {
            /*
             * Now done in JSEmitter: // support for class inits. // If this is
             * a static function and the owner class has a class init then call
             * __static_init() final IDefinition fdef = func.getDefinition();
             * if( fdef.isStatic() && fdef.getParent() != null &&
             * fdef.getParent() instanceof ClassDefinition ) { final IDefinition
             * cdef = fdef.getParent(); final String fullName =
             * JSGeneratingReducer
             * .createFullNameFromDefinition(enclosing_scope.getProject(),cdef);
             * if( JSSharedData.instance.hasClassInit(fullName) ) { final String
             * callInit = fullName + "." + JSSharedData.STATIC_INIT + "();\n";
             * if( a_priori_insns == null ) a_priori_insns = new
             * InstructionList(); a_priori_insns.addInstruction(
             * JSSharedData.OP_JS, callInit ); } }
             */
            m_burm.startFunction(func);
            mi = generateMethodBodyForFunction(mi, func, enclosing_scope, a_priori_insns);
            m_burm.endFunction(func);
        }

        return mi;
    }

    /**
     * Helper method used by <code>generateFunction()</code>.
     * 
     * @param func - the function declaration node.
     * @param mi - the MethodInfo describing the signature
     * @param enclosing_scope - the lexical scope in which the handler method is
     * autogenerated.
     */
    static void generateNativeMethod(FunctionNode func, MethodInfo mi,
                                     LexicalScope enclosing_scope)
    {
        enclosing_scope.getMethodBodySemanticChecker().checkNativeMethod(func);

        // don't need to create a new scope, so just use the enclosing scope
        // to get a handle to the emitter
        IMethodVisitor mv = enclosing_scope.getEmitter().visitMethod(mi);

        // Just visit the method info.  Do NOT generate a body
        // for native methods
        mv.visit();

        // func.getReturnType() returns a short name string.
        // But we need a real name. ctors don't have return types.
        if (!func.isConstructor())
        {
            final ASScope scope = (ASScope)JSGeneratingReducer.getScopeFromNode(func);
            final FunctionDefinition fdef = func.getDefinition();
            final IReference ref = fdef.getReturnTypeReference();
            final Name returnTypeName = ref.getMName(enclosing_scope.getProject(), scope);
            mi.setReturnType(returnTypeName);
        }

        // For non native methods, the return type is set by the burm,
        // but for native types, as the burm isn't run, we need to set
        // the return type here.
        // String returnType = func.getReturnType();
        // mi.setReturnType(new Name(returnType));

        mv.visitEnd();
    }

    /**
     * Helper method used by <code>generateFunction()</code> (and also by
     * <code>generateEventHandler()</code> in MXMLDocumentDirectiveProcessor).
     * 
     * @param mi - the MethodInfo describing the signature
     * @param node - the FunctionNode or MXMLEventSpecifierNode. may be null
     * when generating method bodies for purely synthetic functions, such as
     * theIEventDispatcher methods that [Bindable] introduces.
     * @param enclosing_scope - the lexical scope in which the handler method is
     * autogenerated.
     * @param a_priori_insns - instructions generated by an enclosing subsystem
     * that should be included in the function (e.g., a constructor needs a
     * priori instructions to initialize instance vars).
     */
    MethodInfo generateMethodBodyForFunction(MethodInfo mi, IASNode node,
            LexicalScope enclosing_scope,
            InstructionList a_priori_insns)
    {
        return generateMethodBody(mi, node, enclosing_scope, a_priori_insns, CmcEmitter.__function_NT, null);
    }

    /**
     * Helper methods used by databinding codegen to emit anonymous functions
     * based on an expression node.
     * 
     * @param mi - the MethodInfo describing the signature
     * @param node - the expression node whose code will start the function. may
     * be null when generating method bodies for purely synthetic functions,
     * such as theIEventDispatcher methods that [Bindable] introduces.
     * @param enclosing_scope
     * @param insns_to_append - typically some massaging of the TOS and a return
     * function
     */
    public MethodInfo generateFunctionFromExpression(MethodInfo mi, IASNode node,
            LexicalScope enclosing_scope,
            InstructionList insns_to_append)
    {
        return generateMethodBody(mi, node, enclosing_scope, null, CmcEmitter.__expression_NT, insns_to_append);
    }

    /**
     * Helper methods used by databinding codegen to emit anonymous functions
     * based on an expression node.
     * 
     * @param mi - the MethodInfo describing the signature
     * @param nodes - a list of expression nodes whose code will start the
     * function. May have the following values: null when generating method
     * bodies for purely synthetic functions, such as theIEventDispatcher
     * methods that [Bindable] introduces. an IASNode for an expression that
     * will be code-gen'd a List of IASNodes. This is a special case where we
     * code-gen the sum of all the expressions
     * @param enclosing_scope
     * @param insns_to_append - typically some massaging of the TOS and a return
     * function
     */

    public MethodInfo generateFunctionFromExpressions(MethodInfo mi, List<? extends IASNode> nodes,
             LexicalScope enclosing_scope,
             InstructionList insns_to_append)
    {
        return generateMethodBody(mi, nodes, enclosing_scope, null, CmcEmitter.__expression_NT, insns_to_append);
    }

    /**
     * General method body maker. see the public documentation, above, for more
     * into
     */
    MethodInfo generateMethodBody(MethodInfo mi, Object node,
                                         LexicalScope enclosing_scope,
                                         InstructionList a_priori_insns,
                                         int goal_state,
                                         InstructionList insns_to_append)
    {
        //  Set up a lexical scope for this function.
        LexicalScope function_scope = enclosing_scope.pushFrame();

        IMethodVisitor mv = function_scope.getEmitter().visitMethod(mi);
        mv.visit();

        MethodBodyInfo mbi = new MethodBodyInfo();
        mbi.setMethodInfo(mi);

        IMethodBodyVisitor mbv = mv.visitBody(mbi);
        mbv.visit();

        function_scope.methodBodyVisitor = mbv;
        function_scope.traitsVisitor = mbv.visitTraits();
        function_scope.setMethodInfo(mi);

        InstructionList insns = null;
        if (node == null)
        {
            // node may be null when generating method bodies for purely synthetic functions, such as the
            // IEventDispatcher methods that [Bindable] introduces.
            insns = new InstructionList();
        }
        else if (node instanceof IASNode)
        {
            // If we are passed a single node, generate its instructions
            insns = generateInstructions((IASNode)node, goal_state, function_scope, a_priori_insns);
        }
        else if (node instanceof List<?>)
        {
            List<?> nodes = (List<?>)node;

            // for a list of nodes, generate all their instructions and add the results together.
            // typically we are doing this to concatenate strings
            for (int nodeIndex = 0; nodeIndex < nodes.size(); ++nodeIndex)
            {
                IASNode n = (IASNode)nodes.get(nodeIndex);
                if (nodeIndex == 0)
                {
                    // First one in the list makes a new IL and puts
                    // instructions into it
                    insns = generateInstructions(n, goal_state, function_scope, a_priori_insns);
                }
                else
                {
                    // successive children generate into the same IS, then add the results
                    insns.addAll(generateInstructions(n, goal_state, function_scope, a_priori_insns));
                    insns.addInstruction(OP_add);
                }
            }
        }
        else
        {
            assert false; // Illegal type passed as node parameter
        }
        assert insns != null;

        // If caller passed in instructions to get after the BURM-generated stuff,
        // add them to the instruction stream
        if (insns_to_append != null)
        {
            insns.addAll(insns_to_append);
        }

        mbv.visitInstructionList(insns);

        if (function_scope.needsActivation())
        {
            mi.setFlags((byte)(mi.getFlags() | ABCConstants.NEED_ACTIVATION));
        }

        mbv.visitEnd();
        mv.visitEnd();

        return mi;
    }

    /**
     * Creates a MethodInfo specifying the signature of a method declared by a
     * FunctionNode.
     * 
     * @param func - A FunctionNode representing a method declaration.
     * @return The MethodInfo specifying the signature of the method.
     */
    public static MethodInfo createMethodInfo(JSGeneratingReducer burm, JSEmitter emitter, LexicalScope scope, FunctionNode func)
    {
        MethodInfo mi = new MethodInfo();
        //  FIXME: FunctionNode.getQualifiedName() has
        //  preconditions that need to be understood!
        mi.setMethodName(func.getName());

        FunctionDefinition funcDef = func.getDefinition();
        //  Marshal the function's arguments.
        ParameterDefinition[] args = funcDef.getParameters();
        List<String> param_names = new ArrayList<String>();

        ICompilerProject project = scope.getProject();
        if (args.length > 0)
        {
            Vector<Name> method_args = new Vector<Name>();
            for (ParameterDefinition arg : args)
            {
                TypeDefinitionBase arg_type = arg.resolveType(project);
                Name type_name = arg_type != null ? arg_type.getMName(project) : null;

                if (arg.isRest())
                {
                    mi.setFlags((byte)(mi.getFlags() | ABCConstants.NEED_REST));
                    param_names.add(arg.getBaseName());
                }
                else
                {
                    method_args.add(type_name);
                    param_names.add(arg.getBaseName());
                }
            }
            mi.setParamTypes(method_args);
            mi.setParamNames(param_names);
        }

        // check for native modifier
        if (func.getDefinition().isNative())
        {
            mi.setFlags((byte)(mi.getFlags() | ABCConstants.NATIVE));
        }

        // The return type will be set by the BURM.

        // Falcon's IMethodVisitor only records a fraction of the FunctionDefinition.
        // For that reason we are registering every MethodInfo with its corresponding FunctionDefinition at the JSEmitter.
        emitter.visitFunctionDefinition(mi, funcDef);

        return mi;
    }

    // called by JSInterfaceDirectiveProcessor
    public MethodInfo createMethodInfo(LexicalScope scope, FunctionNode func)
    {
        return JSGenerator.createMethodInfo(m_burm, m_emitter, scope, func);
    }

    /**
     * Helper method to expose the constant folding code to clients outside of
     * the burm, such as
     * org.apache.flex.compiler.internal.as.definitions.ConstantDefinition.
     * 
     * @param subtree the tree to generate a constant value for
     * @param project the project to use to evaluate the tree
     * @return the constant value for the subtree, or null if a constant value
     * can't be determined
     */
    public Object generateConstantValue(IASNode subtree, ICompilerProject project)
    {
        Object result = null;

        LexicalScope scope = new GlobalLexicalScope(project, null, GlobalLexicalScope.EMPTY_NAME_PREFIX, m_emitter);

        if (subtree != null)
        {
            try
            {
                result = reduceSubtree(subtree, scope, CmcJSEmitter.__constant_value_NT);
            }
            catch (Exception cant_reduce)
            {
                // Can't generate a constant value, just return null
            }
        }

        return result;
    }

    /**
     * Reduce an AST to its equivalent ABC structures.
     * 
     * @param subtree - the root of the AST subtree. May be null, in which case
     * this routine returns null.
     * @param scope - the active LexicalScope.
     * @param goal - the BURM's goal state. One of the CmcEmitter.__foo_NT
     * constants.
     * @return the result of reducing the subtree to the desired goal state, or
     * null if the input subtree was null.
     * @throws Exception from the BURM if the computation didn't succeed or was
     * interrupted.
     */
    public Object reduceSubtree(IASNode subtree, LexicalScope scope, int goal)
            throws Exception
    {
        CmcJSEmitter burm = m_cmcJSEmitter;
        burm.reducer = this.m_burm;
        burm.reducer.setCurrentscope(scope);

        burm.burm(subtree, CmcEmitter.__constant_value_NT);
        return burm.getResult();
    }

    /**
     * Handle an error from a BURM: emit diagnostics and bump the error count.
     * 
     * @param n - the subtree that was to be reduced.
     * @param ex - the exception.
     */
    @SuppressWarnings("nls")
    private static void handleBurmError(CmcJSEmitter burm, IASNode n, Exception ex, LexicalScope scope)
    {
        if (ex instanceof CodegenInterruptedException)
        {
            // If the exception is an InterruptedException, do nothing, as not
            // a real error.  The incremental flow kicked in and interrupted
            // the current work, so just throw away the current work and carry
            // on our merry way.
            // No problem should be reported in this case.
            scope.getProblems().clear();
            return;
        }
        else if (!(ex instanceof BURMAbortException))
        {
            scope.addProblem(new CodegenInternalProblem(n, ex));
        }

        java.io.PrintWriter dumper;

        String dump_dir = System.getenv("JBURG_DUMP_DIR");
        if (dump_dir != null)
        {
            try
            {
                String dump_file = dump_dir + "/failedBurm-" + Integer.toString(dumpCount++) + ".xml";
                dumper = new java.io.PrintWriter(new java.io.FileWriter(dump_file));
                dumper.println("<?xml version=\"1.0\"?>");
                dumper.println("<BurmDump date=\"" + new Date().toString() + "\">");
                burm.dump(dumper);
                dumper.println("<AST>");
                dumper.println(new NodesToXMLStringFormatter(n).toString());
                dumper.println("</AST>");
                dumper.println("</BurmDump>");
                dumper.flush();
                dumper.close();
            }
            catch (Exception e)
            {
                JSSharedData.instance.stderr("Unable to dump due to: " + e.toString());
                try
                {
                    JSSharedData.instance.stderr(new NodesToXMLStringFormatter(n).toString());
                }
                catch (Exception cantformat)
                {
                    //  Probably an error in the AST itself, diagnosed above.
                }
            }
        }
    }

    /**
     * Number of diagnostic dumps emitted by this compiler; used to generate
     * unique dump file names.
     */
    static int dumpCount = 0;

    public String toString()
    {
        return "JSGenerator: " + m_compilationUnit.toString();
    }

    public void setBuildPhase(ICompilationUnit.Operation op)
    {
        m_buildPhase = op;
        m_burm.setBuildPhase(op);
    }

    public JSGeneratingReducer getReducer()
    {
        return m_burm;
    }

}
