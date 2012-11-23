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

import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.visitors.IABCVisitor;
import org.apache.flex.abc.visitors.IMethodVisitor;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.references.INamespaceReference;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.InterfaceNode;
import org.apache.flex.compiler.problems.BadAccessInterfaceMemberProblem;
import org.apache.flex.compiler.problems.InterfaceNamespaceAttributeProblem;

/**
 * The InterfaceDirectiveProcessor translates an InterfaceNode AST and children
 * into an interface declaration and definition in the ABC and the init script,
 * respectively.
 */
public class JSInterfaceDirectiveProcessor extends InterfaceDirectiveProcessor
{
    private JSGenerator m_generator;

    /**
     * Create an InterfaceDirectiveProcessor and set up the basic AET
     * structures.
     * 
     * @param in - the InterfaceNode.
     * @param enclosing_scope - the lexical scope that encloses this interface
     * declaration. Either the global scope or a package scope.
     * @param emitter - the active ABC emitter.
     */
    public JSInterfaceDirectiveProcessor(JSGenerator jsGenerator, InterfaceNode in, LexicalScope enclosing_scope, IABCVisitor emitter)
    {
        super(in, addDefinition(enclosing_scope, in.getDefinition()), emitter);
        m_generator = jsGenerator;
    }

    private static LexicalScope addDefinition(LexicalScope enclosing_scope, InterfaceDefinition idef)
    {
        JSSharedData.instance.registerDefinition(idef);
        return enclosing_scope;
    }

    /**
     * Declare a function.
     */
    @Override
    void declareFunction(FunctionNode func)
    {
        verifyFunctionModifiers(func);

        INamespaceReference ns_ref = func.getDefinition().getNamespaceReference();

        if (ns_ref instanceof INamespaceDefinition.IInterfaceNamespaceDefinition)
        {
            //  Allowed, continue
        }
        else if (ns_ref instanceof INamespaceDefinition.ILanguageNamespaceDefinition)
        {
            interfaceScope.addProblem(new BadAccessInterfaceMemberProblem(func));
        }
        else
        // if ( ns_ref instanceof UserDefinedNamespaceDefinition )
        {
            interfaceScope.addProblem(new InterfaceNamespaceAttributeProblem(func));
        }

        // workaround for Falcon bug.
        // InterfaceDirectiveProcessor currently does not record return type and default values of optional parameters.
        // In order to do so InterfaceDirectiveProcessor needs to do what ClassDirectiveProcessor::declareFunction
        // is doing, which is letting the ABCGenerator kick off CmcEmitter.burm() etc.
        // This is the stack trace for the JS backend implementation:
        //      JSGenerator.generateInstructions() 
        //      JSGenerator.generateMethodBody()  
        //      JSGenerator.generateFunction()
        //      JSClassDirectiveProcessor.declareFunction() 
        // The workaround below achieves the same by directly calling JSGenerator.generateFunction(), which is robust 
        // enough to handle situations where there is no IMethodBodyVisitor registered.

        // MethodInfo mi = m_generator.createMethodInfo(this.interfaceScope, func);
        MethodInfo mi = m_generator.generateFunction(func, this.interfaceScope, null);

        IMethodVisitor mv = this.emitter.visitMethod(mi);
        mv.visit();
        mv.visitEnd();

        FunctionDefinition funcDef = func.getDefinition();
        Name funcName = funcDef.getMName(interfaceScope.getProject());
        if (mi.getReturnType() == null && funcDef.getReturnTypeReference() != null)
            mi.setReturnType(funcDef.getReturnTypeReference().getMName(interfaceScope.getProject(), funcDef.getContainedScope()));

        itraits.visitMethodTrait(functionTraitKind(func, TRAIT_Method), funcName, 0, mi);
    }

}
