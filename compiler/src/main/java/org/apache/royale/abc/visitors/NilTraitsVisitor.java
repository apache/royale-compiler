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
 * An ITraitsVisitor that ignores its input as far as possible.
 */
public class NilTraitsVisitor implements ITraitsVisitor
{
    @Override
    public void visit()
    {
    }

    @Override
    public void visitEnd()
    {
    }

    @Override
    public Traits getTraits()
    {
        return null;
    }
    
    @Override
    public ITraitVisitor visitClassTrait(int kind, Name name, int slotId, ClassInfo clazz)
    {
        return NilVisitors.NIL_TRAIT_VISITOR;
    }

    @Override
    public ITraitVisitor visitMethodTrait(int kind, Name name, int dispId, MethodInfo method)
    {
        return NilVisitors.NIL_TRAIT_VISITOR;
    }

    @Override
    public ITraitVisitor visitSlotTrait(int kind, Name name, int slotId, Name slotType, Object slotValue)
    {
        return NilVisitors.NIL_TRAIT_VISITOR;
    }
}
