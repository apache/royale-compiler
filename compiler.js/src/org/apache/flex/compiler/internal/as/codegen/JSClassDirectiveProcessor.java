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

import org.apache.flex.abc.ABCConstants;
import org.apache.flex.abc.visitors.IABCVisitor;
import org.apache.flex.abc.visitors.ITraitVisitor;
import org.apache.flex.abc.visitors.ITraitsVisitor;
import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.abc.semantics.Trait;
import org.apache.flex.abc.instructionlist.InstructionList;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.internal.as.codegen.ICodeGenerator.IConstantValue;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.DefinitionBase;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.NamespaceDefinition;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.internal.tree.as.ClassNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.NamespaceNode;
import org.apache.flex.compiler.internal.tree.as.VariableNode;
import org.apache.flex.compiler.problems.StaticNamespaceDefinitionProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.ICommonClassNode;

/**
 * A ClassDirectiveProcessor generates an ABC class from a ClassNode and its
 * contents. JSClassDirectiveProcessor is derived from ClassDirectiveProcessor
 * and adds workarounds necessary for FalconJS. Ideally FalconJS should use
 * ClassDirectiveProcessor and retire JSClassDirectiveProcessor. This
 * implementation is part of FalconJS. For more details on FalconJS see
 * org.apache.flex.compiler.JSDriver.
 */
@SuppressWarnings("nls")
public class JSClassDirectiveProcessor extends ClassDirectiveProcessor
{
    JSGenerator m_generator;

    /**
     * Instructions to place in the class' initializer. Note that these are part
     * of the class itself, as opposed to the above instructions which create
     * the class at the global scope.
     */
    InstructionList looseInsns = new InstructionList();

    /**
     * Constructor. Initializes the ClassDirectiveProcessor and its associated
     * AET data structures.
     * 
     * @param c - the class' AST.
     * @param enclosing_scope - the immediately enclosing lexical scope.
     * @param emitter - the active ABC emitter.
     */
    public JSClassDirectiveProcessor(JSGenerator generator, ClassNode c, LexicalScope enclosing_scope, IABCVisitor emitter)
    {
        this(generator, c, addDefinition(enclosing_scope.getProject(), c.getDefinition()), enclosing_scope, emitter);
    }

    private static ClassDefinition addDefinition(ICompilerProject project, ClassDefinition cdef)
    {
        JSSharedData.instance.registerDefinition(cdef);
        return cdef;
    }

    /**
     * Constructor. Initializes the ClassDirectiveProcessor and its associated
     * AET data structures.
     * 
     * @param class_definition - the class' definition
     * @param enclosing_scope - the immediately enclosing lexical scope.
     * @param emitter - the active ABC emitter.
     */
    public JSClassDirectiveProcessor(JSGenerator generator, IASNode node, ClassDefinition class_definition, LexicalScope enclosing_scope, IABCVisitor emitter)
    {
        super((ICommonClassNode)node, class_definition, enclosing_scope, emitter);
        m_generator = generator;

        /*
         * // add explicit dependencies. final ICompilerProject project =
         * classScope.getProject(); final ClassDefinition superclassDefinition =
         * class_definition.resolveBaseClass( new ASDefinitionCache(project),
         * new RecursionGuard(), classScope.getProblems()); if(
         * superclassDefinition != null )
         * JSSharedData.instance.addDependency(class_definition,
         * superclassDefinition);
         */

        generator.getReducer().setClassDefinition(enclosing_scope.getProject(), class_definition);
    }

    /**
     * Finish the class' definition.
     */
    @Override
    void finishClassDefinition()
    {
        //  Create the class' constructor function.
        if (this.ctorFunction != null /* || !iinitInsns.isEmpty() */)
        {
            MethodInfo mi = m_generator.generateFunction(this.ctorFunction, classScope, this.iinitInsns);
            if (mi != null)
                this.iinfo.iInit = mi;

            this.ctorFunction = null;
            this.iinitInsns = new InstructionList();
        }

        // clear cinitInsns if there are no side effects
        // by initializing the static members directly.
        final String fullName = JSGeneratingReducer.createFullNameFromDefinition(classScope.getProject(), classDefinition);
        if (!JSSharedData.instance.hasClassInit(fullName))
            cinitInsns = new InstructionList();

        // support for class inits 
        // loose statement are now collected in looseInsns
        if (!this.looseInsns.isEmpty())
            cinitInsns.addAll(looseInsns);

        // base class injects ABC if not empty and then NPEs.
        // save our insns and give it an empty list then restore
        InstructionList cinitHack = cinitInsns;
        cinitInsns = new InstructionList();
        super.finishClassDefinition();
        cinitInsns = cinitHack;

        m_generator.getReducer().setClassDefinition(null, null);
    }

    /**
     * Declare a function. TODO: static vs. instance.
     */
    @Override
    void declareFunction(FunctionNode func)
    {
        func.parseFunctionBody(classScope.getProblems());

        boolean is_constructor = func.isConstructor();

        functionSemanticChecks(func);

        //  Save the constructor function until
        //  we've seen all the instance variables
        //  that might need initialization.
        if (is_constructor)
        {
            this.ctorFunction = func;
        }
        else
        {
            MethodInfo mi = m_generator.generateFunction(func, classScope, null);
            ITraitVisitor tv;

            if (mi != null)
            {
                FunctionDefinition funcDef = func.getDefinition();
                Name funcName = funcDef.getMName(classScope.getProject());

                if (func.hasModifier(ASModifier.STATIC))
                    tv = ctraits.visitMethodTrait(functionTraitKind(func, TRAIT_Method), funcName, 0, mi);
                else
                {
                    tv = itraits.visitMethodTrait(functionTraitKind(func, TRAIT_Method), funcName, 0, mi);
                    if (funcDef.getNamespaceReference() instanceof NamespaceDefinition.IProtectedNamespaceDefinition)
                        this.iinfo.flags |= ABCConstants.CLASS_FLAG_protected;
                }

                this.classScope.processMetadata(tv, funcDef.getAllMetaTags());

                if (func.hasModifier(ASModifier.FINAL))
                    tv.visitAttribute(Trait.TRAIT_FINAL, Boolean.TRUE);
                if (func.hasModifier(ASModifier.OVERRIDE))
                    tv.visitAttribute(Trait.TRAIT_OVERRIDE, Boolean.TRUE);
            }
        }
    }

    /**
     * Declare a variable. TODO: static vs. instance.
     */
    @Override
    void declareVariable(VariableNode var)
    {
        verifyVariableModifiers(var);

        DefinitionBase varDef = var.getDefinition();
        m_generator.getReducer().startDeclareVariable(varDef);

        boolean is_static = var.hasModifier(ASModifier.STATIC);
        boolean is_const = SemanticUtils.isConst(var, classScope.getProject());
        // simple initializers for public/protected vars go right on prototype.
        // the rest (all private vars), all "complex" initializers (like array) get
        // initialized in the constructor
        boolean needs_constructor_init = true;
        
        //  generateConstantValue() returns null if no constant value
        //  can be generated, and null is the correct value for "no value."
        IConstantValue constantValue =  m_generator.generateConstantValue(var.getAssignedValueNode(), this.classScope.getProject());

        //  initializer is null if no constant value
        //  can be generated, and null is the correct value for "no value."
        Object initializer = constantValue != null ? constantValue.getValue() : null;

        ITraitVisitor tv = declareVariable(var, varDef, is_static, is_const, initializer);

        this.classScope.processMetadata(tv, varDef.getAllMetaTags());

        //  Generate variable initializers and append them to the 
        //  proper initialization list.
        if (var.getChildCount() > 1)
        {
            //  We need to put the correct traits visitor on the class'
            //  LexicalScope; the BURM may encounter variable declarations
            //  chained onto this one, and it will need the traits visitor to declare them.

            //  Save the scope's current traits visitor (which should be null)
            //  and restore it 
            ITraitsVisitor saved_traits_visitor = this.classScope.traitsVisitor;
            assert (saved_traits_visitor == null);
            try
            {
                // the following line causes duplicate Traits.
                // JSEmitter::emitTraits works around duplicate Traits by checking against
                // a visitedTraits set.
                this.classScope.traitsVisitor = (is_static) ? ctraits : itraits;
                this.classScope.resetDebugInfo();
                InstructionList init_expression = m_generator.generateInstructions(var, CmcJSEmitter.__statement_NT, this.classScope);
                if (init_expression != null && !init_expression.isEmpty())
                {
                    // final JSEmitter emitter = (JSEmitter)this.classScope.getEmitter();
                    final String str = JSGeneratingReducer.instructionListToString(init_expression, true);

                    if (str.contains(" = "))
                    {
                        final String varInit = m_generator.getReducer().getVariableInitializer(varDef);
                        if (varInit != null && !varInit.isEmpty())
                        {
                            // set the value of the slot trait.
                            final String varName = varDef.getBaseName();
                            for (Trait t : this.classScope.traitsVisitor.getTraits())
                            {
                                final byte kind = t.getKind();
                                if (kind == TRAIT_Const || kind == TRAIT_Var)
                                {
                                	boolean is_private = false;
                                    final Name name = t.getNameAttr(Trait.TRAIT_NAME);
                                    Namespace ns = name.getSingleQualifier();
                                    if (ns.getKind() == CONSTANT_PrivateNs)
                                    	is_private = true;
                                    if (name.getBaseName().equals(varName))
                                    {
                                        t.setAttr(Trait.SLOT_VALUE, varInit);
                                        if (!is_private)
                                        	needs_constructor_init = false;
                                        break;
                                    }
                                }
                            }

                        }

                        if (is_static)
                        {
                            // see finishClassDefinition.
                            // We clear cinitInsns only if there are no side effects
                            // by initializing the static members directly.
                            // If varInit is null, or varInit is isEmpty() 
                            // then we have side effects. 
                            if (!init_expression.isEmpty())
                                registerClassInit(var);

                            cinitInsns.addAll(init_expression);
                        }
                        else if (needs_constructor_init)
                            iinitInsns.addAll(init_expression);
                    }
                }
            }
            finally
            {
                this.classScope.traitsVisitor = saved_traits_visitor;
            }
        }

        m_generator.getReducer().endDeclareVariable(varDef);
    }

    /**
     * Ignore modifier nodes that are in the AST, but processed as attributes of
     * the definition nodes. Other loose directives are processed as statements
     * and added to the class' static init method.
     */
    @Override
    void processDirective(IASNode n)
    {
        switch (n.getNodeID())
        {

            case StaticID:
            case FinalID:
            case OverrideID:
            case UseID:
                break;

            case NamespaceID:
            {
                NamespaceNode ns = (NamespaceNode)n;

                if (ns.hasModifier(ASModifier.STATIC))
                {
                    this.classScope.addProblem(new StaticNamespaceDefinitionProblem(ns));
                }
                else
                {
                    try
                    {
                        this.classScope.traitsVisitor = itraits;
                        m_generator.generateInstructions(n, CmcEmitter.__statement_NT, this.classScope);
                        // assert(stmt_insns == null);
                    }
                    finally
                    {
                        this.classScope.traitsVisitor = null;
                    }
                }
                break;
            }
            default:
            {
                // support for class inits.
                // loose statement are now collected in looseInsns
                //  Handle a loose statement.
                InstructionList stmt_insns = m_generator.generateInstructions(n, CmcJSEmitter.__statement_NT, this.classScope);
                if (stmt_insns != null)
                {
                    if (looseInsns.size() == 0)
                        registerClassInit(n);

                    looseInsns.addAll(stmt_insns);
                }
                break;
            }
        }
    }

    private void registerClassInit(IASNode node)
    {
        final String fullName = JSGeneratingReducer.createFullNameFromDefinition(classScope.getProject(), classDefinition);
        if (!fullName.equals(JSSharedData.JS_FRAMEWORK_NAME))
        {
            JSSharedData.instance.registerClassInit(fullName);
            m_generator.getReducer().warnClassInitPerformance(node);
            m_generator.getReducer().setNeedsSecondPass();
        }
    }
}
