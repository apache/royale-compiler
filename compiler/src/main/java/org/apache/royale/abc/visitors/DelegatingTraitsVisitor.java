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

package org.apache.royale.abc.visitors;

import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Traits;

/**
 * this class just passes all calls through to the delegate - used as a base
 * class when you want to modify the behavior of some of the methods.
 */
public class DelegatingTraitsVisitor implements ITraitsVisitor
{
    public DelegatingTraitsVisitor(ITraitsVisitor delegate)
    {
        this.delegate = delegate;
    }

    private ITraitsVisitor delegate;

    @Override
    public void visit()
    {
        delegate.visit();
    }

    @Override
    public void visitEnd()
    {
        delegate.visitEnd();
    }

    @Override
    public Traits getTraits()
    {
        return delegate.getTraits();
    }

    @Override
    public ITraitVisitor visitSlotTrait(int kind, Name name, int slotID, Name slotType, Object slotValue)
    {
        return delegate.visitSlotTrait(kind, name, slotID, slotType, slotValue);
    }

    @Override
    public ITraitVisitor visitClassTrait(int kind, Name name, int slotID, ClassInfo clazz)
    {
        return delegate.visitClassTrait(kind, name, slotID, clazz);
    }

    @Override
    public ITraitVisitor visitMethodTrait(int kind, Name name, int dispID, MethodInfo method)
    {
        return delegate.visitMethodTrait(kind, name, dispID, method);
    }
}
