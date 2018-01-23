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

package org.apache.royale.compiler.internal.abc;

import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.scopes.IASScope;

/**
 * Implementation of the {@linkplain IScriptVisitor}. It builds the script-level
 * scope object.
 */
class ScriptDefinitionBuilder implements IScriptVisitor
{
    /**
     * Create a ScriptDefinitionBuilder. The definitions will be added to the
     * given scope object.
     * 
     * @param scope the scope contained by the script
     */
    protected ScriptDefinitionBuilder(final ABCScopeBuilder owner, final IASScope scope)
    {
        this.scope = scope;
        this.scopeBuilder = owner;
    }

    private final IASScope scope;
    private final ABCScopeBuilder scopeBuilder;

    @Override
    public void visit()
    {
    }

    @Override
    public void visitEnd()
    {
    }

    @Override
    public ITraitsVisitor visitTraits()
    {
        final ITraitsVisitor visitor = new ScopedDefinitionTraitsVisitor(scopeBuilder,
                                                                        scope,
                                                                        false);
        return visitor;
    }

    @Override
    public void visitInit(MethodInfo methodInfo)
    {
    }

}
