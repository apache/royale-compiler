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

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.InterfaceNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLDocumentNode;
import org.apache.royale.compiler.problems.AbstractOutsideClassProblem;
import org.apache.royale.compiler.problems.DynamicNotOnClassProblem;
import org.apache.royale.compiler.problems.EmbedOnlyOnClassesAndVarsProblem;
import org.apache.royale.compiler.problems.FinalOutsideClassProblem;
import org.apache.royale.compiler.problems.GlobalBindablePropertyProblem;
import org.apache.royale.compiler.problems.InterfaceModifierProblem;
import org.apache.royale.compiler.problems.NativeNotOnFunctionProblem;
import org.apache.royale.compiler.problems.NativeVariableProblem;
import org.apache.royale.compiler.problems.OverrideOutsideClassProblem;
import org.apache.royale.compiler.problems.StaticOutsideClassProblem;
import org.apache.royale.compiler.problems.SyntaxProblem;
import org.apache.royale.compiler.problems.VirtualOutsideClassProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.utils.ASTUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * A GlobalDirectiveProcessor translates directives at
 * global scope into ABC.
 */
class GlobalDirectiveProcessor extends DirectiveProcessor
{
    /** Instructions to implement directives as they're encountered. */
    InstructionList directiveInsns = new InstructionList();
    
    /** The top of the lexical scope chain. */
    protected LexicalScope currentScope;
    
    /** The AET emitter generating code for this script. */
    protected IABCVisitor emitter;

    /** 
     *  Set when processing a package, which inplies some different
     *  strategies for declaring traits.
     */
    protected boolean processingPackage = false;
    
    private final List<GenerateFunctionInParallelResult> parallelCodeGenList;
    
    /**
     * {@link ExecutorService} used to generation function
     * bodies in background threads.
     */
    private final ExecutorService executorService;
    
    /**
     * Flag to enabling or disabling use of background threads
     * to generate function bodies.
     */
    private final boolean useParallelCodeGen;

    /**
     * @param current_scope the scope to use. It may be created a priori by the
     * caller, so it's not created by nesting an enclosing scope.
     * @param emitter the ABC emitter.
     */
    GlobalDirectiveProcessor(LexicalScope current_scope, IABCVisitor emitter)
    {
        this(null, false, current_scope, emitter);
    }
    
    /**
     * @param executorService {@link ExecutorService} used to schedule
     * generation of function bodies on background threads. This may be null if
     * useParallelCodeGen is false.
     * @param useParallelCodeGen Flag to enabling or disabling use of background
     * threads to generate function bodies.
     * @param current_scope the scope to use. It may be created a priori by the
     * caller, so it's not created by nesting an enclosing scope.
     * @param emitter the ABC emitter.
     */
    GlobalDirectiveProcessor(ExecutorService executorService, boolean useParallelCodeGen, LexicalScope current_scope, IABCVisitor emitter)
    {
        super(current_scope.getProblems());
        assert (!useParallelCodeGen) || (executorService != null) : "Parallel code generation requires access to an ExecutorService";
        
        this.currentScope = current_scope;
        this.emitter = emitter;
        this.parallelCodeGenList = new LinkedList<GenerateFunctionInParallelResult>();
        this.executorService = executorService;
        this.useParallelCodeGen = useParallelCodeGen;
    }
    
    /**
     * Start generation of the specified function in a background thread if
     * parallel code generation is enabled, otherwise generate code for the
     * specified function in the calling thread
     * 
     * @param f Function to generate code for.
     * @return {@link MethodInfo} for the specified function.
     */
    private MethodInfo startFunctionGeneration(FunctionNode f)
    {
        if (this.useParallelCodeGen)
        {
            assert this.executorService != null : "Parallel codegen requires an ExecutorService!";
            final GenerateFunctionInParallelResult parallelCodeGen =
                currentScope.getGenerator().generateFunctionInParallel(this.executorService, f, this.currentScope);
            
            this.parallelCodeGenList.add(parallelCodeGen);
            
            return parallelCodeGen.getMethodInfo();
        }
        else
        {
            f.parseFunctionBody(currentScope.getProblems());
            ASTUtil.processFunctionNode(f, currentScope.getProject());
            return currentScope.getGenerator().generateFunction(f, this.currentScope, null, null);
        }
    }
    
    /**
     * Declare a function.
     */
    @Override
    void declareFunction(FunctionNode f)
    {
        verifyFunctionModifiers(f);

        final MethodInfo mi = startFunctionGeneration(f);
        if ( mi != null )
        {
            FunctionDefinition funcDef = f.getDefinition();
            Name funcName = funcDef.getMName(this.currentScope.getProject());
            
            SemanticUtils.checkScopedToDefaultNamespaceProblem(this.currentScope, f, funcDef, null);
            

            boolean conflictsWithOtherDefinition = false;
            if ( funcName == null )
            {
                //  getMName() emitted a diagnostic, 
                //  repair and continue.
                funcName = new Name("<invalid>");
            }
            else
            {
                conflictsWithOtherDefinition = currentScope.getMethodBodySemanticChecker().checkFunctionForConflictingDefinitions(f, funcDef);
            }
       
            ITraitVisitor tv = null;

            int traitKind = this.processingPackage?
                DirectiveProcessor.functionTraitKind(f, ABCConstants.TRAIT_Method):
                DirectiveProcessor.functionTraitKind(f, ABCConstants.TRAIT_Var);

            if (! this.currentScope.traitsVisitor.getTraits().containsTrait(traitKind, funcName) )
            {
                this.currentScope.declareVariableName(funcName);

                if ( ! this.processingPackage )
                {
                    if ( f.isGetter() || f.isSetter() )
                    {
                        tv = this.currentScope.traitsVisitor.visitMethodTrait(
                            traitKind, 
                            funcName, 
                            ITraitsVisitor.RUNTIME_DISP_ID,
                            mi);
                        assert tv != null : "visitMethodTrait should never return null!";
                    }
                    else
                    {
                        tv = this.currentScope.traitsVisitor.visitSlotTrait(
                            traitKind,
                            funcName,
                            ITraitsVisitor.RUNTIME_SLOT,
                            LexicalScope.anyType,
                            LexicalScope.noInitializer);
                        assert tv != null : "visitSlotTrait should never return null!";
    
                        this.currentScope.getInitInstructions().addInstruction(ABCConstants.OP_getglobalscope);
                        this.currentScope.getInitInstructions().addInstruction(ABCConstants.OP_newfunction, mi);
                        this.currentScope.getInitInstructions().addInstruction(ABCConstants.OP_setproperty, funcName);
                    }
                    
    
                }
                else
                {
                    tv = this.currentScope.traitsVisitor.visitMethodTrait(traitKind, funcName, 0, mi);
                    assert tv != null : "visitMethodTrait should never return null!";
                }
                
                if ( tv != null )
                {
                    this.currentScope.processMetadata(tv, funcDef.getAllMetaTags());
                    tv.visitEnd();
                }
            }
            else if (!conflictsWithOtherDefinition)
            {
                // Duplicate that is not a "conflict" - must be a global, where dupes are "allowed" as 
                // per ECMA
                // In strict mode (only) we issue a warning for this. Which is the behavior of the old compiler
                // (as well as being the "right" thing to do)
                
                // But - our simple criteria for "conflicts with other definitions" will give a false positive for
                // Getter/setter pairs, so only emit the warning if this is not the case.
                //
                // Updater: the warning is detected elsewhere, so all we are doing here is
                // generating code to create the new function as per ECMAS

                ICompilerProject project = currentScope.getProject();
                List<IDefinition> defs = SemanticUtils.findPotentialFunctionConflicts(project, funcDef);

                if (!SemanticUtils.isGetterSetterPair(defs, project))
                {
                    // This is a new funciton, so generate code for it
                    //  Add initialization logic to the init instructions.
                    if ( ! ( this.processingPackage || f.isGetter() || f.isSetter() ) )
                    {
                        this.currentScope.getInitInstructions().addInstruction(ABCConstants.OP_getglobalscope);
                        this.currentScope.getInitInstructions().addInstruction(ABCConstants.OP_newfunction, mi);
                        this.currentScope.getInitInstructions().addInstruction(ABCConstants.OP_setproperty, funcName);
                    }
                }
            }
        }
    }
    
    /**
     * validate the modifiers used on a function decl.
     */
    protected void verifyFunctionModifiers(FunctionNode f)
    {
        ModifiersSet modifiersSet = f.getModifiers();
        if (modifiersSet == null)
            return;

        ASModifier[] modifiers = modifiersSet.getAllModifiers();
        IExpressionNode site = f.getNameExpressionNode();
        for (ASModifier modifier : modifiers)
        {
            verifyModifier(site, modifier);
        }
        currentScope.getMethodBodySemanticChecker().checkForDuplicateModifiers(f);
    }

    /**
     * validate the modifiers used on a var decl
     */
    protected void verifyVariableModifiers(VariableNode v)
    {
        ModifiersSet modifiersSet = v.getModifiers();
        if (modifiersSet == null)
            return;

        ASModifier[] modifiers = modifiersSet.getAllModifiers();
        IExpressionNode site = v.getNameExpressionNode();
        for (ASModifier modifier : modifiers)
        {
            // native on a variable generates a different error
            if (modifier == ASModifier.NATIVE)
            {
                currentScope.addProblem(new NativeVariableProblem(site));
            }
            else if( modifier == ASModifier.DYNAMIC )
            {
                currentScope.addProblem(new DynamicNotOnClassProblem(site));
            }
            else
            {
                verifyModifier(site, modifier);
            }
        }
        currentScope.getMethodBodySemanticChecker().checkForDuplicateModifiers(v);
    }

    /**
     * validate the modifiers used on a class decl
     */
    protected void verifyClassModifiers(ClassNode c)
    {
        IExpressionNode site = c.getNameExpressionNode();

        ModifiersSet modifiersSet = c.getModifiers();
        if (modifiersSet != null)
        {
            ASModifier[] modifiers = modifiersSet.getAllModifiers();
            for (ASModifier modifier : modifiers)
            {
                // final, dynamic, and abstract allowed on a class
                if( modifier == ASModifier.FINAL || modifier == ASModifier.DYNAMIC || modifier == ASModifier.ABSTRACT)
                {
                    continue;
                }
                // native generates different error for class/interface
                else if (modifier == ASModifier.NATIVE)
                {
                    currentScope.addProblem(new NativeNotOnFunctionProblem(site) );
                }
                else
                {
                    verifyModifier(site, modifier);
                }
            }
            currentScope.getMethodBodySemanticChecker().checkForDuplicateModifiers(c);
        }

        IDefinition classDef = c.getDefinition();
        if (classDef.isAbstract())
        {
            if (currentScope.getProject().getAllowAbstractClasses())
            {
                if(!SemanticUtils.canBeAbstract(c, currentScope.getProject()))
                {
                    currentScope.addProblem(new AbstractOutsideClassProblem(site) );
                }
            }
            else
            {
                currentScope.addProblem(new SyntaxProblem(site, IASKeywordConstants.ABSTRACT));
            }
        }
    }

    /**
     * validate the skinning data used on a class decl
     */
    protected void verifySkinning(ClassDefinition classDefinition)
    {
        // call these skinPart/skinState methods to collect any problems
        // with the metadata.
        classDefinition.getSkinParts(currentScope.getProblems());
        classDefinition.getSkinStates(currentScope.getProblems());

        classDefinition.verifyHostComponent((CompilerProject)currentScope.getProject(), currentScope.getProblems());
    }

    /**
     * Validate the modifiers used on an interface decl
     */
    protected void verifyInterfaceModifiers(InterfaceNode i)
    {
        ModifiersSet modifiersSet = i.getModifiers();
        if (modifiersSet == null)
            return;

        ASModifier[] modifiers = modifiersSet.getAllModifiers();
        IExpressionNode site = i.getNameExpressionNode();
        for (ASModifier modifier : modifiers)
        {
            // final generates a different error for an interface
            if( modifier == ASModifier.FINAL || modifier == ASModifier.DYNAMIC)
            {
                currentScope.addProblem(new InterfaceModifierProblem(site, modifier.toString()));
            }
            // native generates different error for class/interface
            else if (modifier == ASModifier.NATIVE)
            {
                currentScope.addProblem(new NativeNotOnFunctionProblem(site) );
                //  ASC also emits this for good measure.
                currentScope.addProblem(new InterfaceModifierProblem(site, modifier.toString()));
            }
            else
            {
                verifyModifier(site, modifier);
            }
        }
        currentScope.getMethodBodySemanticChecker().checkForDuplicateModifiers(i);
    }

    /**
     * verify a modifier used in global scope - issues errors that are common for all different decls
     * at this scope.
     * @param site      location to use if a problem is reported
     * @param modifier  the modifier to check
     */
    protected void verifyModifier(IASNode site, ASModifier modifier)
    {
        if( modifier == ASModifier.STATIC )
            currentScope.addProblem(new StaticOutsideClassProblem(site));
        else if( modifier == ASModifier.FINAL )
            currentScope.addProblem(new FinalOutsideClassProblem(site));
        else if( modifier == ASModifier.OVERRIDE )
            currentScope.addProblem(new OverrideOutsideClassProblem(site));
        else if( modifier == ASModifier.VIRTUAL )
            currentScope.addProblem(new VirtualOutsideClassProblem(site));
        else if( modifier == ASModifier.ABSTRACT )
        {
            if(currentScope.getProject().getAllowAbstractClasses())
            {
                currentScope.addProblem(new AbstractOutsideClassProblem(site));
            }
            else
            {
                currentScope.addProblem(new SyntaxProblem(site, IASKeywordConstants.ABSTRACT));
            }
        }
    }

    /**
     * Declare a class.
     */
    @Override
    void declareClass(ClassNode c)
    {
        verifyClassModifiers(c);
        verifySkinning(c.getDefinition());
        currentScope.getMethodBodySemanticChecker().checkNamespaceOfDefinition(c, c.getDefinition(), currentScope.getProject());
        if (c.getDefinition().getConstructor().isImplicit()) {
            //check that the implicit super call is to a default constructor
            //otherwise this class needs to have an explicit constructor with an explicit super call
            //checks for problem:  Error: No default constructor found in base class {base class}
            currentScope.getMethodBodySemanticChecker().checkDefaultSuperCall(c.getDefinition().getConstructor().getNode());
        }
        ClassDirectiveProcessor cp = new ClassDirectiveProcessor(c, this.currentScope, this.emitter);
        cp.traverse(c.getScopedNode());
        cp.finishClassDefinition();
    }

    /**
     * Declare an interface.
     */
    @Override
    void declareInterface(InterfaceNode interface_ast)
    {
        verifyInterfaceModifiers(interface_ast);
        currentScope.getMethodBodySemanticChecker().checkNamespaceOfDefinition(interface_ast, interface_ast.getDefinition(), currentScope.getProject());
        InterfaceDirectiveProcessor ip = new InterfaceDirectiveProcessor(interface_ast, this.currentScope, this.emitter);
        ip.traverse(interface_ast.getScopedNode());
        ip.finishInterfaceDefinition();
    }
    
    /**
     * "Declare" a package.
     */
    @Override
    void declarePackage(PackageNode p)
    {
        try
        {
            this.processingPackage = true;
            traverse(p.getScopedNode());
        }
        finally
        {
            this.processingPackage = false;
        }
    }
    
    /**
     * Declare a variable.
     */
    @Override
    void declareVariable(VariableNode var)
    {
        verifyVariableModifiers(var);

        if (var.getMetaTags() != null && var.getMetaTags().hasTagByName(IMetaAttributeConstants.ATTRIBUTE_EMBED))
        {
            currentScope.addProblem(new EmbedOnlyOnClassesAndVarsProblem(var));
        }

        DefinitionBase varDef = var.getDefinition();
        SemanticUtils.checkScopedToDefaultNamespaceProblem(this.currentScope, var, varDef, null);
        if ( var.hasModifier(ASModifier.STATIC))
        {
            
            ICompilerProject project = this.currentScope.getProject();

            Name var_name = varDef.getMName(project);

            TypeDefinitionBase typeDef = (TypeDefinitionBase)varDef.resolveType(project);
            Name var_type = typeDef != null ? typeDef.getMName(project) : null;
            
            //  It's not necessary to check for duplicates
            //  in the traits because that is a semantic error
            //  in this context.
            ITraitVisitor tv = this.currentScope.traitsVisitor.visitSlotTrait(ABCConstants.TRAIT_Const, var_name, ITraitsVisitor.RUNTIME_SLOT, var_type, LexicalScope.noInitializer);
            this.currentScope.declareVariableName(var_name);
            this.currentScope.processMetadata(tv, varDef.getAllMetaTags());
            tv.visitEnd();
        }
        //  Run the BURM to process any initialization instructions.
        processDirective(var);
    }

    /**
     * Declare a bindable variable.
     */
    @Override
    void declareBindableVariable(VariableNode var)
    {
        currentScope.addProblem(new GlobalBindablePropertyProblem(var));
    }

    /**
     * Declare an MXML document.
     */
    @Override
    void declareMXMLDocument(IMXMLDocumentNode d)
    {
        verifySkinning((ClassDefinition)d.getDefinition());
        MXMLClassDirectiveProcessor dp = new MXMLClassDirectiveProcessor(d, this.currentScope, this.emitter);
        ((MXMLDocumentNode)d).cdp = dp;
        dp.processMainClassDefinitionNode(d);
        dp.finishClassDefinition();
    }

    /**
     * Process a namespace directive.
     */
    @Override
    void processNamespaceIdentifierDirective(NamespaceIdentifierNode ns)
    {
        traverse(ns);
    }
    
    /**
     * Process a random directive, which at the global level
     * is probably a loose instruction.
     */
    @Override
    void processDirective(IASNode n)
    {
        //  Handle a loose statement.
        InstructionList stmt_insns = currentScope.getGenerator().generateInstructions(n, CmcEmitter.__statement_NT, currentScope);
        if ( stmt_insns != null )
            directiveInsns.addAll(stmt_insns);
    }
    
    /**
     * Block until all function generation is complete and flush all ABC data to
     * the {@link IABCVisitor} we are generating code into.
     * 
     * @throws InterruptedException
     */
    void finish() throws InterruptedException
    {
        try
        {
            for (GenerateFunctionInParallelResult parallelCodeGen : this.parallelCodeGenList)
            {
                parallelCodeGen.finish();
            }
        }
        catch (ExecutionException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
