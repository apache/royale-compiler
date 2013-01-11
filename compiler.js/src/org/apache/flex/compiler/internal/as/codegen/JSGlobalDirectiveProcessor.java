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

import org.apache.flex.abc.ABCConstants;
import org.apache.flex.abc.instructionlist.InstructionList;
import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.visitors.IABCVisitor;
import org.apache.flex.abc.visitors.IClassVisitor;
import org.apache.flex.abc.visitors.ITraitVisitor;
import org.apache.flex.abc.visitors.ITraitsVisitor;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.tree.as.ClassNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.ImportNode;
import org.apache.flex.compiler.internal.tree.as.InterfaceNode;
import org.apache.flex.compiler.internal.tree.as.PackageNode;
import org.apache.flex.compiler.internal.tree.as.VariableNode;
import org.apache.flex.compiler.problems.DuplicateFunctionDefinitionProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;

/**
 * A GlobalDirectiveProcessor translates directives at global scope into ABC.
 * JSGlobalDirectiveProcessor is derived from GlobalDirectiveProcessor and adds
 * workarounds necessary for FalconJS. Ideally FalconJS should use
 * GlobalDirectiveProcessor and retire JSGlobalDirectiveProcessor. This
 * implementation is part of FalconJS. For more details on FalconJS see
 * org.apache.flex.compiler.JSDriver
 */
public class JSGlobalDirectiveProcessor extends GlobalDirectiveProcessor
{
    JSGenerator m_generator;

    private String m_packageName = null;

    /**
     * @param current_scope - the scope to use. It may be created a priori by
     * the caller, so it's not created by nesting an enclosing scope.
     * @param emitter - the ABC emitter.
     */
    public JSGlobalDirectiveProcessor(JSGenerator generator, LexicalScope current_scope, IABCVisitor emitter)
    {
        super(current_scope, emitter);
        m_generator = generator;
    }

    @Override
    void declareFunction(FunctionNode f)
    {
        //  No modifiers allowed at global scope.
        // super.verifyModifiers(f);

        // MISSING in GlobalDirectiveProcessor
        MethodInfo mi = m_generator.generateFunction(f, currentScope, null);
        if (mi != null)
        {
            FunctionDefinition funcDef = f.getDefinition();
            JSSharedData.instance.registerDefinition(funcDef);

            // TODO: generalize excluding functions using metadata annotations.
            final String packageName = funcDef.getPackageName();
            if (!packageName.equals("com.jquery"))
            {
                final IClassVisitor cv = ((JSEmitter)emitter).visitPackage(m_packageName);
                cv.visit();
                this.currentScope.traitsVisitor = cv.visitClassTraits();
                cv.visitEnd();
                /*
                 * InstanceInfo iinfo = new InstanceInfo(); iinfo.name =
                 * JSGeneratingReducer.makeName(packageName); IClassVisitor cv =
                 * emitter.visitClass(iinfo, new ClassInfo()); cv.visit();
                 * cv.visitInstanceTraits(); ITraitsVisitor ctraits =
                 * cv.visitClassTraits(); final Name funcName =
                 * funcDef.getMName(currentScope.getProject(),
                 * currentScope.getProblems()); final ITraitVisitor tv =
                 * ctraits.visitMethodTrait(TRAIT_Method, funcName, 0, mi);
                 * tv.visitAttribute(Trait.TRAIT_FINAL, Boolean.TRUE);
                 */
                /*
                 * final String key = packageName; ICompilationUnit cu =
                 * m_generator.m_compilationUnit; ICompilationUnit registeredCU
                 * = JSSharedData.instance.getCompilationUnit(key); if( cu !=
                 * registeredCU ) { // make the current cu dependent on the
                 * registered cu. if( registeredCU != null ) { final
                 * CompilerProject compilerProject =
                 * (CompilerProject)cu.getProject();
                 * compilerProject.addDependency(cu, registeredCU,
                 * DependencyType.INHERITANCE); } else {
                 * JSSharedData.instance.registerCompilationUnit(key, cu); } }
                 */
            }

            Name funcName = funcDef.getMName(this.currentScope.getProject());

            if (funcName == null)
            {
                //  getMName() emitted a diagnostic, 
                //  repair and continue.
                funcName = new Name("<invalid>");
            }

            ITraitVisitor tv = null;

            int traitKind = this.processingPackage ?
                    DirectiveProcessor.functionTraitKind(f, ABCConstants.TRAIT_Method) :
                    DirectiveProcessor.functionTraitKind(f, ABCConstants.TRAIT_Var);

            if (!this.currentScope.traitsVisitor.getTraits().containsTrait(traitKind, funcName))

            {
                this.currentScope.declareVariableName(funcName);

                if (!this.processingPackage)
                {
                    // Functions at the global scope create a var of type '*'
                    // TODO: this should be typed as 'Function' if strict mode is on
                    if (f.isGetter() || f.isSetter())
                    {
                        tv = this.currentScope.traitsVisitor.visitMethodTrait(
                                DirectiveProcessor.functionTraitKind(f, ABCConstants.TRAIT_Var),
                                funcName,
                                ITraitsVisitor.RUNTIME_DISP_ID,
                                mi);
                    }
                    else
                    {
                        tv = this.currentScope.traitsVisitor.visitSlotTrait(
                                DirectiveProcessor.functionTraitKind(f, ABCConstants.TRAIT_Var),
                                funcName,
                                ITraitsVisitor.RUNTIME_SLOT,
                                LexicalScope.anyType,
                                LexicalScope.noInitializer);

                        /*
                         * this.currentScope.getInitInstructions().addInstruction
                         * (ABCConstants.OP_getglobalscope);
                         * this.currentScope.getInitInstructions
                         * ().addInstruction(ABCConstants.OP_newfunction, mi);
                         * this
                         * .currentScope.getInitInstructions().addInstruction
                         * (ABCConstants.OP_setproperty, funcName);
                         */
                    }
                }
                else
                {
                    tv = this.currentScope.traitsVisitor.visitMethodTrait(DirectiveProcessor.functionTraitKind(f, ABCConstants.TRAIT_Method), funcName, 0, mi);
                }

                if (tv != null)
                {
                    this.currentScope.processMetadata(tv, funcDef.getAllMetaTags());
                }
            }
            else
            {
                ICompilerProblem problem = new DuplicateFunctionDefinitionProblem(f, funcName.getBaseName());
                this.currentScope.addProblem(problem);
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
        JSClassDirectiveProcessor cp = JSSharedData.backend.createClassDirectiveProcessor(m_generator, c, this.currentScope, this.emitter);
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
        InterfaceDirectiveProcessor ip = JSSharedData.backend.createInterfaceDirectiveProcessor(m_generator, interface_ast, this.currentScope, this.emitter);
        ip.traverse(interface_ast.getScopedNode());
        ip.finishInterfaceDefinition();
    }

    /**
     * Process a random directive, which at the global level is probably a loose
     * instruction.
     */
    @Override
    void processDirective(IASNode n)
    {
        // Use nodes have no effect
        /*
         * if(n.getNodeID() == ASTNodeID.UseID ) { if(
         * !JSSharedData.GENERATE_EMBED_WRAPPER ) return; }
         */

        // workaround for Falcon bug.
        // DirectiveProcessor is observing NamespaceIdentifierID instead of NamespaceID
        if (n.getNodeID() == ASTNodeID.NamespaceID)
        {
            try
            {
                final IClassVisitor cv = ((JSEmitter)emitter).visitPackage(m_packageName);
                cv.visit();
                this.currentScope.traitsVisitor = cv.visitClassTraits();
                cv.visitEnd();

                m_generator.generateInstructions(n, CmcEmitter.__statement_NT, this.currentScope);
                // assert(stmt_insns == null);
            }
            finally
            {
                // this.currentScope.traitsVisitor = null;
            }
            return;
        }

        //  Handle a loose statement.
        InstructionList stmt_insns = m_generator.generateInstructions(n, CmcJSEmitter.__statement_NT, currentScope);
        if (stmt_insns != null)
            directiveInsns.addAll(stmt_insns);
    }

    @Override
    void declarePackage(PackageNode p)
    {
        m_packageName = p.getName();
        ((JSEmitter)emitter).visitPackage(m_packageName);
        JSSharedData.instance.registerPackage(m_packageName);
        super.declarePackage(p);
    }

    /**
     * Declare a variable.
     */
    @Override
    void declareVariable(VariableNode var)
    {
        super.declareVariable(var);
    }

    /**
     * Translate a ImportNode AST into ABC. Subclasses should override this if
     * they can process imports.
     * 
     * @param imp - the import's AST.
     */
    @Override
    void processImportDirective(ImportNode imp)
    {
        String importName = imp.getImportName();
        if (!importName.contains("."))
        {
            final IASScope scope = JSGeneratingReducer.getScopeFromNode(imp);
            final IDefinition def = ((ASScope)scope).findProperty(currentScope.getProject(), imp.getImportName(), DependencyType.INHERITANCE);
            if (def == null)
            {
                importName = null;
            }
            else
            {
                // workaround for Falcon bug.
                // Falcon does not always recognize dependencies to package functions provided by SWCs.
                // In this workaround we explicitly set a EXPRESSION dependency. 
                /*
                 * if( def != null ) { final ICompilationUnit fromCU =
                 * m_generator.m_compilationUnit; final CompilerProject
                 * compilerProject = (CompilerProject)currentScope.project;
                 * final ASProjectScope projectScope =
                 * compilerProject.getScope(); final ICompilationUnit toCU =
                 * projectScope.getCompilationUnitForDefinition(def); if( fromCU
                 * != toCU ) { // sharedData.verboseMessage(
                 * "Adding dependency: " + className );
                 * compilerProject.addDependency(fromCU, toCU,
                 * DependencyGraph.DependencyType.EXPRESSION); } }
                 */

                // skip imports from the same package.
                if (def.getPackageName().equals(m_packageName))
                    importName = null;
                else
                    importName = JSGeneratingReducer.definitionToString(currentScope.getProject(), def);
            }
        }

        if (importName != null)
        {
            JSEmitter emitter = (JSEmitter)this.currentScope.getEmitter();
            emitter.visitImport(importName, imp.getImportKind());
        }
    }
    
    /**
     * Declare an MXML document.
     */
    @Override
    void declareMXMLDocument(IMXMLDocumentNode d)
    {
        verifySkinning((ClassDefinition)d.getDefinition());
        try
        {
            MXMLClassDirectiveProcessor dp;
        	dp = new JSMXMLClassDirectiveProcessor(d, this.currentScope, this.emitter);
            dp.processMainClassDefinitionNode(d);
            dp.finishClassDefinition();
        }
        catch (Error e)
        {
        	System.out.print(e);
        }
    }

}
