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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.royale.abc.ABCConstants.OP_finddef;
import static org.apache.royale.abc.ABCConstants.OP_getproperty;
import static org.apache.royale.abc.ABCConstants.OP_getlocal0;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.InstructionFactory;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.scopes.ScopeView;
import org.apache.royale.compiler.internal.semantics.MethodBodySemanticChecker;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * Lexical scope which is constructed when inlining a function.  This
 * allows us to modify the lexical scope of the calling function, and if
 * the inline fails unwind any changes which were made to the scope.
 */
public class InlineFunctionLexicalScope extends LexicalScope
{
    /**
     * The maximum number of exressions that can be contained
     * in a function body to inline.
     */
    public static final int MAX_EXPR_IN_BODY = 50;

    /**
     * The scope with which this inlined function is contained
     */
    private final IASScope containingScope;

    /**
     * The Binding which contains the "this" of the function being inlined
     */
    private final Binding containingClassBinding;

    /**
     * Return call site to jump back to after the inline
     */
    private final Label inlinedFunctionCallSiteLabel;

    /**
     * Collection of problems generating during the inline.  Stored here
     * rather than on the global scope, so that any problems created while
     * attempting an inline will not be displayed to the user.
     */
    private final Collection<ICompilerProblem> problems;


    protected InlineFunctionLexicalScope(LexicalScope enclosingFrame, IASScope containingScope, boolean storeClassBinding, FunctionNode functionNode)
    {
        super(enclosingFrame, true);
        this.containingScope = containingScope;
        this.inlinedFunctionCallSiteLabel = new Label();
        // set the problems and methodBodySemanticChecker, so we don't create
        // any problems when trying to inline a function
        this.problems = new ArrayList<ICompilerProblem>();
        this.methodBodySemanticChecker = new MethodBodySemanticChecker(this);

        // these members should be initialized from the containing scope, as they'll be merged back
        // if the inline is successful
        this.containingClassBinding = storeClassBinding ? allocateTemp() : null;

        IScopedNode body = functionNode.getScopedNode();
        FunctionDefinition functionDefinition = (FunctionDefinition)functionNode.getDefinition();

        setInitialControlFlowRegionNode(body);
        setLocalASScope(functionDefinition.getContainedScope());
        resetDebugInfo();

        // Set the current file name - the function body will always start with an OP_debugfile
        // so we never need emit another one unless the method body spans multiple files
        setDebugFile(SemanticUtils.getFileName(functionNode));
    }

    public void assignActualsToFormals(FunctionDefinition functionDef, InstructionList result)
    {
        // The same parameter name may occur more than
        // once in the parameter list. In this case, ECMA says the last one wins.
        // So in the loop below will go from last parameter to first, and skip any 
        // that we have already seen
        ParameterDefinition[] params = functionDef.getParameters();
        Set<String> uniqueParamNames = new HashSet<String>();
        for (int i = params.length - 1; i >= 0; i--)
        {
            String paramName = params[i].getBaseName();
            Binding paramBinding = getBinding(params[i]);
            if (paramBinding != null && !uniqueParamNames.contains(paramName))
            {
                result.addInstruction(paramBinding.setlocal());
                uniqueParamNames.add(paramName);
            }
        }
    }

    public void mergeIntoContainingScope(InstructionList result)
    {
        Collection<Binding> localBindings = getLocalBindings();
        if (!localBindings.isEmpty())
        {
            ArrayList<Binding> inlinedBindings = new ArrayList<Binding>(localBindings.size());
            for (Binding localBinding : localBindings)
            {
                inlinedBindings.add(localBinding);
                releaseTemp(localBinding);
            }

            getEnclosingFrame().addInlinedBindings(inlinedBindings);
        }

        getEnclosingFrame().addHasNexts(getHasNexts());

        getEnclosingFrame().mergeTemps(this);
    }

    @Override
    public boolean insideInlineFunction()
    {
        return true;
    }

    @Override
    protected Binding getThisBinding(IdentifierNode id)
    {
        if (containingClassBinding != null)
            return containingClassBinding;
        else
            return super.getThisBinding(id);
    }

    @Override
    public InstructionList findProperty(Name name, IDefinition def, boolean useStrict)
    {
        // if def is null, that means the property couldn't be resolved, so we can't
        // inline this, so just bail to the super call which will cause the inline
        // to fail.
        if (def == null)
            return super.findProperty(name, def, useStrict);

        InstructionList result = new InstructionList(def.isStatic() ? 2 : 1);

        if (def instanceof IClassDefinition)
        {
            result.addInstruction(OP_finddef, name);
        }
        else if (def.isStatic())
        {
            DefinitionBase staticContainingScope = (DefinitionBase)def.getContainingScope().getDefinition();
            Name containingScopeName = staticContainingScope.getMName(getProject());
            result.addInstruction(OP_finddef, containingScopeName);
            result.addInstruction(OP_getproperty, containingScopeName);
        }
        else if (((DefinitionBase)def).isTopLevelDefinition())
        {
            result.addInstruction(OP_finddef, name);
        }
        else if (containingScope instanceof ScopeView)
        {
            // Check if the property is a member of the ViewScope.  If so, then
            // return the containing class register
            ScopeView scopeView = (ScopeView)containingScope;
            final String baseName = name.getBaseName();
            IDefinition propertyDef = scopeView.getPropertyFromDef(getProject(), scopeView.getDefinition(), baseName, false);

            if (propertyDef != null)
                result.addInstruction(getContainingClass());
        }

        if (!result.isEmpty())
            return result;

        // couldn't statically resolve the definition, so
        // just emit the normal findprop, and the inlining check
        // AbcGeneratingReducer.checkInlinedInstructions() will
        // catch this problem and react accordingly
        return super.findProperty(name, def, useStrict);
    }

    @Override
    public InstructionList getPropertyValue(Name name, IDefinition def)
    {
        InstructionList findPropResult = findProperty(name, def, true);

        InstructionList result = new InstructionList(findPropResult.size() + 1);
        result.addAll(findPropResult);
        result.addInstruction(OP_getproperty, name);
        return result;
    }

    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return this.problems;
    }

    @Override
    InstructionList finishMethodDeclaration(final boolean hasBody, String source_path)
    {
        assert false : "finishMethodDeclaration() should never be called on an inline function";
        return null;
    }

    private Instruction getContainingClass()
    {
        if (containingClassBinding != null)
        {
            return containingClassBinding.getlocal();
        }
        else
        {
            return InstructionFactory.getInstruction(OP_getlocal0);
        }
    }

    public Instruction setContainingClass()
    {
        assert (containingClassBinding != null) : "don't set the class when there is no class binding, as local0 can be used";
        return containingClassBinding.setlocal();
    }

    /**
     * Returns the label to jump back to when returning
     * out of an inlined function
     * 
     * @return label, or null if not inlining a function
     */
    public Label getInlinedFunctionCallSiteLabel()
    {
        return inlinedFunctionCallSiteLabel;
    }
}
