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

package org.apache.royale.compiler.internal.definitions;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.ScopeView;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IExpressionNode;

public class ConstantDefinition extends VariableDefinition implements IConstantDefinition
{
    public ConstantDefinition(String name)
    {
        super(name);
    }

    /**
     * Construct a ConstantDefinition with a constant value - for use when
     * building defs from an ABC.
     * 
     * @param name the name of the definition
     * @param value the value of the constant
     */
    public ConstantDefinition(String name, Object value)
    {
        super(name, value);
    }

    /**
     * For debugging only. Produces a string such as
     * <code>public const N:int</code>.
     */
    @Override
    public void buildInnerString(StringBuilder sb)
    {
        sb.append(getNamespaceReferenceAsString());
        sb.append(' ');

        sb.append(IASKeywordConstants.CONST);
        sb.append(' ');

        sb.append(getBaseName());

        String type = getTypeAsDisplayString();
        if (!type.isEmpty())
        {
            sb.append(':');
            sb.append(type);
        }
    }

    @Override
    public Object resolveValue(ICompilerProject project)
    {
        // Fail fast if we statically know we won't have a value
        // constants in control flow always fail to constant eval
        if( declaredInControlFlow() )
            return null;

        // Fastest way out for Constants that came from ABCs
        if (initValue != null)
            return resolveUndefined(project, initValue);

        // TODO: possible optimizations
        // 1. cache result on a per-project basis (ASScopeCache)
        // 2. Copy the init expr out of the original tree, so we don't have to
        //    reparse the whole file if the AST is collected.
        return ((CompilerProject)project).getCacheForScope(getContainingASScope()).getConstantValue(this);
    }

    /**
     * Try to calculate the constant value for this Constant Definition when
     * referenced from the passed in Node. If the passed in node is a Forward
     * reference to this ConstantDefinition then we will return null, if it is
     * not a forward reference this method will return the same as resolveValue
     * above.
     * 
     * @param project project to use to resolve the initializer
     * @param fromNode the node that is referencing this constant
     * @return the constant value of this definition, or null if one can't be
     * determined, or if this is a forward reference, as determined by the
     * fromNode.
     */
    public Object resolveValueFrom(ICompilerProject project, NodeBase fromNode)
    {
        // Fail fast if we statically know we won't have a value
        // constants in control flow always fail to constant eval
        if(declaredInControlFlow())
            return null;

        // fast path for values from ABC
        if (initValue != null)
            return resolveUndefined(project, initValue);

        if (fromNode != null && fromNode.getFileScope() == this.getFileScope())
        {
            // Declared in the same file, figure out if we're a forward reference.
            if (fromNode.getAbsoluteStart() <= this.getAbsoluteStart())
            {
                // We can reference a static property from an instance context even if the
                // static property occurs later in the file - this is because we know the
                // static initializer will have run by the time we get to the instance
                if( !isReferenceToStaticFromInstanceScope(fromNode) )
                    return null;
            }
            // Not a forward reference, but are we referencing ourselves from our own initializer?
            else
            {
                IExpressionNode initNode = this.getInitExpression();
                if (initNode != null)
                {
                    // We are looking up ourselves from inside our own initializer
                    // contains returns false if the start positions match, so just check that here
                    if (initNode.getAbsoluteStart() == fromNode.getAbsoluteStart() ||
                        initNode.contains(fromNode.getAbsoluteStart()))
                        return null;
                }
            }
        }
        return resolveValue(project);
    }

    public Object resolveValueImpl(ICompilerProject project)
    {
        // Fail fast if we statically know we won't have a value
        // constants in control flow always fail to constant eval
        if( declaredInControlFlow() )
            return null;

        Object value = super.resolveInitialValue(project);

        value = resolveUndefined(project, value);
        return value;
    }

    /**
     * This mimics ASCs strange behavior with UNDEFINED constants. This is due
     * to the ABC file not actually having enough information to differentiate
     * btwn no initializer, and "undefined" was the initializer
     */
    private Object resolveUndefined(ICompilerProject project, Object value)
    {
        if (value == ABCConstants.UNDEFINED_VALUE &&
            resolveType(project) != ClassDefinition.getAnyTypeClassDefinition())
        {
            // If we are a type that can't hold undefined, then return null
            // so that this constant won't participate in constant folding.
            // This is because the ABC format does not differentiate between
            // no initializer, and the intializer was the "undefined" value.
            // In old-ASC there were bugs that made it appear to work this way most of the time,
            // so we're replicating that here.
            value = null;
        }
        return value;
    }

    /**
     * Determine if the reference from 'fromNode' is a reference to a static property from an instance
     * context of the same class.  If it is, then we can allow the forward reference.
     * @param fromNode  the Node making the reference
     * @return          true if 'fromNode' is from an instance context, and this definition is a static property
     *                  of the same class
     */
    private boolean isReferenceToStaticFromInstanceScope (NodeBase fromNode)
    {
        // Only have to check if this Definition is static
        if( this.isStatic() )
        {
            // Get the class this definition is in
            IClassDefinition containingClass = (IClassDefinition)this.getAncestorOfType(IClassDefinition.class);

            // Grab the scope the fromNode uses to resolve itself, and walk
            // up the containing scopes looking for an instance scope
            ASScope fromScope = fromNode.getASScope();
            while( fromScope != null )
            {
                if( fromScope instanceof ScopeView )
                {
                    // return true if we hit an instance scope, and it's for the same
                    // class as this definition is in
                    return ((ScopeView)fromScope).isInstanceScope()
                            && fromScope.getDefinition() == containingClass;
                }
                fromScope = fromScope.getContainingScope();
            }
        }
        return false;
    }
}
