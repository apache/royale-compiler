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
import java.util.Iterator;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;

import static org.apache.royale.abc.ABCConstants.*;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.InterfaceNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.problems.AbstractOutsideClassProblem;
import org.apache.royale.compiler.problems.AmbiguousReferenceProblem;
import org.apache.royale.compiler.problems.CannotExtendClassProblem;
import org.apache.royale.compiler.problems.ConstructorInInterfaceProblem;
import org.apache.royale.compiler.problems.DuplicateInterfaceDefinitionProblem;
import org.apache.royale.compiler.problems.FinalOutsideClassProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InterfaceBindablePropertyProblem;
import org.apache.royale.compiler.problems.InterfaceMethodWithBodyProblem;
import org.apache.royale.compiler.problems.InvalidOverrideProblem;
import org.apache.royale.compiler.problems.NamespaceInInterfaceProblem;
import org.apache.royale.compiler.problems.NativeUsedInInterfaceProblem;
import org.apache.royale.compiler.problems.StaticOutsideClassProblem;
import org.apache.royale.compiler.problems.SyntaxProblem;
import org.apache.royale.compiler.problems.UnknownInterfaceProblem;
import org.apache.royale.compiler.problems.BadAccessInterfaceMemberProblem;
import org.apache.royale.compiler.problems.InterfaceNamespaceAttributeProblem;
import org.apache.royale.compiler.problems.VarInInterfaceProblem;
import org.apache.royale.compiler.problems.VirtualOutsideClassProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.utils.ArrayLikeUtil;

/**
 *  The InterfaceDirectiveProcessor translates an InterfaceNode AST
 *  and children into an interface declaration and definition in the
 *  ABC and the init script, respectively.
 */
public class InterfaceDirectiveProcessor extends DirectiveProcessor
{
    /**
     *  The interface's syntax node.
     */
    InterfaceNode interfaceNode;

    /**
     *  The interface's lexical scope.
     */
    LexicalScope interfaceScope;

    /**
     *  The active ABC emitter.
     */
    IABCVisitor emitter;

    /**
     *  The name of the interface.
     */
    Name interfaceName;

    /** The interface' instance traits */
    ITraitsVisitor itraits;
    
    /** The AET visitor implementing this interface. */
    IClassVisitor cv;
    /** The interface' AET ClassInfo. */
    ClassInfo cinfo = new ClassInfo();
    /** The interface' AET InstanceInfo. */
    InstanceInfo iinfo = new InstanceInfo();


    /**
     *  Create an InterfaceDirectiveProcessor and set up the basic AET structures.
     *  @param in - the InterfaceNode.
     *  @param enclosing_scope - the lexical scope that encloses this interface
     *    declaration.  Either the global scope or a package scope.
     *  @param emitter - the active ABC emitter.
     */
    InterfaceDirectiveProcessor(InterfaceNode in, LexicalScope enclosing_scope, IABCVisitor emitter)
    {
        super(enclosing_scope.getProblems());

        this.interfaceNode   = in;
        this.emitter = emitter;
        
        this.interfaceScope = enclosing_scope.pushFrame();

        //  Create the class level structures.
        InterfaceDefinition interfDef = interfaceNode.getDefinition();
        this.interfaceName = interfDef.getMName(interfaceScope.getProject());
        
        iinfo.name = this.interfaceName;
        
        //  Check for a duplicate interface name.
        switch ( SemanticUtils.getMultiDefinitionType(interfaceNode.getDefinition(), interfaceScope.getProject()))
        {
            case AMBIGUOUS:
                this.interfaceScope.addProblem(new DuplicateInterfaceDefinitionProblem(in, this.interfaceName.getBaseName()));
                break;
            case NONE:
                break;
            default:
                assert false;       // I don't think interfaces can have other type of multiple definitions
        }
        
        if (this.interfaceName != null)
        {
            SemanticUtils.checkScopedToDefaultNamespaceProblem(this.interfaceScope, in, interfDef, this.interfaceName.getBaseName());
        }
        
        // Check for circular definition by iterating over all parent interfaces.    
        Iterator<IInterfaceDefinition> ifaces = interfDef.interfaceIterator(interfaceScope.getProject(), false, interfaceScope.getProblems());
        while (ifaces.hasNext()) {
            ifaces.next();
          
        }
        // check that args are valid
      //  checkArguments(interfDef, interfaceScope.getProject(), interfaceScope.getProblems());

        //  Interfaces can't have a superclass.
        iinfo.superName = null;

        //  Add implmented interfaces.
        IExpressionNode[] raw_interfaces = this.interfaceNode.getExtendedInterfaceNodes();
        iinfo.interfaceNames = new Name[raw_interfaces.length];

        for ( int i = 0; i < raw_interfaces.length; i++)
        {
            IExpressionNode extendedInterface = raw_interfaces[i];
            IDefinition extendedDefinition = extendedInterface.resolve(interfaceScope.getProject());
            
            if ( extendedDefinition instanceof IInterfaceDefinition ) 
            {
                Name interfaceName = ((DefinitionBase)extendedDefinition).getMName(interfaceScope.getProject());
                iinfo.interfaceNames[i] = interfaceName;
            }
            else if ( extendedDefinition instanceof ClassDefinition )
            {
                this.interfaceScope.addProblem(new CannotExtendClassProblem(extendedInterface, extendedDefinition.getBaseName()));
            }
            else if ( extendedDefinition instanceof AmbiguousDefinition )
            {
                if ( extendedInterface instanceof IIdentifierNode )
                {
                    this.interfaceScope.addProblem(new AmbiguousReferenceProblem(extendedInterface, ((IIdentifierNode)extendedInterface).getName()));
                }
                else
                {
                    //  Parser let something weird through.
                    this.interfaceScope.addProblem(new AmbiguousReferenceProblem(extendedInterface, ""));
                }
            }
            else if ( extendedDefinition != null )
            {
                this.interfaceScope.addProblem(new UnknownInterfaceProblem(extendedInterface, extendedDefinition.getBaseName()));
            }
            else
            {
                if ( extendedInterface instanceof IIdentifierNode )
                {
                    this.interfaceScope.addProblem(new UnknownInterfaceProblem(extendedInterface, ((IIdentifierNode)extendedInterface).getName()));
                }
                else
                {
                    //  Parser let something weird through.
                    this.interfaceScope.addProblem(new UnknownInterfaceProblem(extendedInterface, ""));
                }
            }
            
            // Report a problem if the interface is deprecated
            // and the reference to it is not within a deprecated API.
            if ( extendedDefinition != null && extendedDefinition.isDeprecated())
            {
                if (!SemanticUtils.hasDeprecatedAncestor(extendedInterface))
                {
                    ICompilerProblem problem = SemanticUtils.createDeprecationProblem(extendedDefinition, extendedInterface);
                    this.interfaceScope.addProblem(problem);
                }
            }
        }
        
        // Set the flags corresponding to 'final' and 'dynamic'.
        if (interfDef.isFinal())
            iinfo.flags |= ABCConstants.CLASS_FLAG_final;
        if (!interfDef.isDynamic())
            iinfo.flags |= ABCConstants.CLASS_FLAG_sealed;
        iinfo.flags |= ABCConstants.CLASS_FLAG_interface;
        
        this.cv = emitter.visitClass(iinfo, cinfo);
        cv.visit();
        
        this.itraits = cv.visitInstanceTraits();
        
        //  Define the interface in the init script.
        InstructionList setup_insns = this.interfaceScope.getGlobalScope().getInitInstructions();
        setup_insns.addInstruction(OP_getscopeobject, 0);

        //  Interfaces don't have a base class.
        setup_insns.addInstruction(OP_pushnull);

        setup_insns.addInstruction(OP_newclass, cinfo);
        setup_insns.addInstruction(OP_initproperty, interfaceName);
        
        ITraitVisitor tv = this.interfaceScope.getGlobalScope().traitsVisitor.visitClassTrait(TRAIT_Class, interfaceName, 0, cinfo);
        if (ArrayLikeUtil.definitionIsArrayLike(interfDef)) {
            ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
            boolean valid = ArrayLikeUtil.validateArrayLikeDefinition(interfDef, this.interfaceScope.getProject(), problems);
            if (!valid){
                this.interfaceScope.getProject().getProblems().addAll(problems);
            }
        }
        this.interfaceScope.processMetadata(tv, interfDef.getAllMetaTags());
        tv.visitEnd();
    }

    /**
     *  Finish processing an interface declaration.
     */
    void finishInterfaceDefinition()
    {
        this.itraits.visitEnd();
        this.cv.visitEnd();
    }


    /**
     * Translate a VaribleNode AST into ABC.
     * Interfaces can't have vars in them, so just emit a diagnostic.
     * @param var - the variable's AST.
     */
    @Override
    void declareVariable(VariableNode var)
    {
        interfaceScope.addProblem(new VarInInterfaceProblem(var.getNameExpressionNode()));
    }

    /**
     * Declare a function.
     */
    @Override
    void declareFunction(FunctionNode func)
    {
        func.parseFunctionBody(interfaceScope.getProblems());
        functionSemanticChecks(func);

        FunctionDefinition func_def = func.getDefinition();

        final String interfaceBaseName = interfaceNode.getShortName();

        // ignore a constructor in an interface.
        // functionSemanticChecks() will already have issued a diagnostic.
        if (func_def.getBaseName().equals(interfaceBaseName))
        {
            return;
        }

        // make the method info, including any default vaules of parameters. Will do semantic checks
        // on the default values, too.
        MethodInfo mi = interfaceScope.getGenerator().createMethodInfoWithDefaultArgumentValues(this.interfaceScope, func);

        ICompilerProject project = interfaceScope.getProject();

        ExpressionNodeBase return_type_expr = (ExpressionNodeBase)func.getReturnTypeNode();
        if ( return_type_expr != null )
        {
            Name return_type_name = return_type_expr.getMName(project);
            mi.setReturnType(return_type_name);
        }

        IMethodVisitor mv = this.emitter.visitMethod(mi);
        mv.visit();
        mv.visitEnd();

        Name funcName = func_def.getMName(project);

        ITraitVisitor tv = itraits.visitMethodTrait(functionTraitKind(func, TRAIT_Method), funcName, 0, mi);
        IMetaInfo[] metaTags = func_def.getAllMetaTags();
        if (metaTags != null && metaTags.length > 0)
        {
            interfaceScope.processMetadata(tv, metaTags);
        }
        
    }

    /**
     * This method performs the semantic analysis of a function declared in a class.
     * @param func  the FunctionNode to semantically analyze
     */
    void functionSemanticChecks(FunctionNode func)
    {
        final FunctionDefinition func_def = func.getDefinition();
        final String interfaceBaseName = interfaceNode.getShortName();

        // Check for constructor and log a problem.  If it is a constructor,
        // don't bother doing anymore checking
        if (func_def.getBaseName().equals(interfaceBaseName))
        {
            interfaceScope.addProblem(new ConstructorInInterfaceProblem(func));
            return;
        }

        // check the modifiers
        verifyFunctionModifiers(func);

        // Make sure the function doesn't have a namespace
        verifyFunctionNamespace(func, func_def);

        // Warn if there is no return type
        SemanticUtils.checkReturnValueHasNoTypeDeclaration(interfaceScope, func, func_def);
        // Warn if there are any missing parameter types
        SemanticUtils.checkParametersHaveNoTypeDeclaration(interfaceScope, func, func_def);

        // Interface methods can't have a body
        if( func.hasBody() )
        {
            interfaceScope.addProblem(new InterfaceMethodWithBodyProblem(SemanticUtils.getFunctionProblemNode(func)));
        }

        ICompilerProject project = interfaceScope.getProject();

        // Do some semantic checking on the function
        interfaceScope.getMethodBodySemanticChecker().checkFunctionDecl(func);

        //  Ensure the return type is defined.
        IDefinition return_type = func_def.resolveReturnType(project);

        if ( !SemanticUtils.isType(return_type) )
        {
            interfaceScope.getMethodBodySemanticChecker().addTypeProblem(func.getReturnTypeNode(), return_type, func_def.getReturnTypeAsDisplayString(), true);
        }

        Name funcName = func_def.getMName(project);

        if (funcName != null)
        {
            interfaceScope.getMethodBodySemanticChecker().checkInterfaceFunctionForConflictingDefinitions(func, func_def);
        }

    }
        /**
        * Verify that interface function does not have a namespace on it. If it does, print the appropriate error
        * Note that "appropriate" means what the old compiler did: internal, public, private are special cased.
        *
        * @param func is the function node do be analyzed
        * @param func_def is the definition for func
        */
    private void verifyFunctionNamespace(FunctionNode func, FunctionDefinition func_def)
    {
        INamespaceDecorationNode nsNode = func.getActualNamespaceNode();
        
        // if we have no "actual" node, then there is no namespace in front of our function
        if (nsNode != null)
        {
            boolean isLanguateNS = false;
            
            // We need a special check for "internal", because tree building has already munged innternal to 
            // make it just look like the default. But here we need to know what it really is
            if (INamespaceConstants.internal_.equals(nsNode.getName()))
            {
                isLanguateNS = true;
            }
            
            else
            {
                // If it's not "internal", then we can use our normal check to find out what it is
                INamespaceReference ns_ref = func_def.getNamespaceReference();
                if ( ns_ref instanceof INamespaceDefinition.ILanguageNamespaceDefinition )
                {
                    isLanguateNS = true;
                }
                
            }
            
            // generate the appropriate error
            if (isLanguateNS)
            {
                interfaceScope.addProblem(new BadAccessInterfaceMemberProblem(func));
            }
            else 
            {
                interfaceScope.addProblem(new InterfaceNamespaceAttributeProblem(func));
            }
        }
    }

    /**
     * Validate that the modifiers used on a function are allowed
     */
    protected void verifyFunctionModifiers(FunctionNode f)
    {
        IExpressionNode site = f.getNameExpressionNode();

        ModifiersSet modifiersSet = f.getModifiers();
        if (modifiersSet != null)
        {
            ASModifier[] modifiers = modifiersSet.getAllModifiers();
            for (ASModifier modifier : modifiers)
            {
                if( modifier == ASModifier.STATIC )
                {
                    this.interfaceScope.addProblem(new StaticOutsideClassProblem(site));
                }
                else if ( modifier == ASModifier.OVERRIDE )
                {
                    interfaceScope.addProblem(new InvalidOverrideProblem(site));
                }
                else if( modifier == ASModifier.FINAL )
                {
                    interfaceScope.addProblem(new FinalOutsideClassProblem(site));
                }
                else if( modifier == ASModifier.NATIVE )
                {
                    interfaceScope.addProblem(new NativeUsedInInterfaceProblem(site));
                }
                else if( modifier == ASModifier.VIRTUAL )
                {
                    interfaceScope.addProblem(new VirtualOutsideClassProblem(site));
                }
                else if ( modifier == ASModifier.DYNAMIC )
                {
                    //  Allow this and continue.
                }
            }
            interfaceScope.getMethodBodySemanticChecker().checkForDuplicateModifiers(f);
        }

        IDefinition functionDef = f.getDefinition();
        if (functionDef.isAbstract())
        {
            if (interfaceScope.getProject().getAllowAbstractClasses())
            {
                interfaceScope.addProblem(new AbstractOutsideClassProblem(site));
            }
            else
            {
                interfaceScope.addProblem(new SyntaxProblem(site, IASKeywordConstants.ABSTRACT));
            }
        }
    }

    /**
     * Process a namespace identifier.
     */
    @Override
    void processNamespaceIdentifierDirective(NamespaceIdentifierNode ns)
    {
        interfaceScope.addProblem(new NamespaceInInterfaceProblem(ns));
    }

    /**
     * Process an import directive.
     */
    @Override
    void processImportDirective(ImportNode imp)
    {
        // Run the BURM, but for the purpose of semantic checking not code generation.
        interfaceScope.getGenerator().generateInstructions(imp, CmcEmitter.__statement_NT, this.interfaceScope);
    }
    
    /**
     * 
     */
    @Override
    void processDirective(IASNode n)
    {
        switch ( n.getNodeID() )
        {
            case NamespaceID:
                interfaceScope.addProblem(new NamespaceInInterfaceProblem(n));
                break;
            default:
                super.processDirective(n);
        }
    }


    @Override
    void declareBindableVariable(VariableNode varNode)
    {
        interfaceScope.addProblem(new InterfaceBindablePropertyProblem(varNode));
    }
}
