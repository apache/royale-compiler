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

package org.apache.royale.compiler.internal.tree.as;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFunctionObjectNode;

/**
 * An anonymous function (e.g. function() {...}) is an expression, but acts like
 * a function definition where scopes are concerned. We'll implement this hybrid
 * construct by wrapping a regular FunctionNode in an ExpressionNodeBase-based
 * wrapper.
 */
public class FunctionObjectNode extends ExpressionNodeBase implements IFunctionObjectNode
{
    /**
     * Constructor.
     */
    public FunctionObjectNode(FunctionNode functionNode)
    {
        if (functionNode == null)
            throw new NullPointerException("function node can't be null");

        this.functionNode = functionNode;
        this.functionNode.parent = this;
    }

    /**
     * Function node in the wrapper.
     */
    private final FunctionNode functionNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        if (functionNode.getName().isEmpty())
            return ASTNodeID.AnonymousFunctionID;
        else
            return ASTNodeID.FunctionObjectID;
    }

    @Override
    public int getChildCount()
    {
        return 1;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? functionNode : null;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (functionNode != null)
            functionNode.setParent(this);
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        EnumSet<PostProcessStep> stepsToRunOnChildren;
        if (set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            stepsToRunOnChildren = EnumSet.copyOf(set);
            stepsToRunOnChildren.remove(PostProcessStep.RECONNECT_DEFINITIONS);
            stepsToRunOnChildren.add(PostProcessStep.POPULATE_SCOPE);
        }
        else
        {
            stepsToRunOnChildren = set;
        }
        
        super.analyze(stepsToRunOnChildren, scope, problems);
    }
    
    //
    // ExpressionNodeBase overrides
    //

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        return project.getBuiltinType(IASLanguageConstants.BuiltinType.FUNCTION);
    }

    @Override
    protected FunctionObjectNode copy()
    {
        // Can't copy the entire method; this would require code
        // to copy any syntax tree, instead of just expressions.
        // This should be OK because we can't constant evaluate
        // function expressions anyway.
        return null;
    }

    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return true;
    }
    
    //
    // IFunctionObjectNode implementations
    //

    @Override
    public FunctionNode getFunctionNode()
    {
        return functionNode;
    }
}
