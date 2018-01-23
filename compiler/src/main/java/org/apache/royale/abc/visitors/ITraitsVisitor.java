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
 * An ITraitsVisitor defines the individual trait entries of a traits collection.
 */
public interface ITraitsVisitor extends IVisitor
{
    /**
     * Slot ID 0 means "let the AVM pick a slot ID."
     */
    static final int RUNTIME_SLOT = 0;

    /**
     * disp_id ID 0 means "let the AVM pick a disp_id."
     */
    static final int RUNTIME_DISP_ID = 0;

    /**
     * Begin visiting traits. Calling the visitXTrait methods is undefined
     * before visit() is called.
     */
    void visit();

    /**
     * @return the visitor's traits (optional), or null.
     */
    Traits getTraits();

    /**
     * Define a slot trait.
     * 
     * @return a ITraitsVisitor, used to visit the trait's metadata.
     */
    ITraitVisitor visitSlotTrait(int kind, Name name, int slotID, Name slotType, Object slotValue);

    /**
     * Define a class trait.
     * 
     * @return a ITraitsVisitor, used to visit the trait's metadata.
     */
    ITraitVisitor visitClassTrait(int kind, Name name, int slotID, ClassInfo clazz);

    /**
     * Define a method trait.
     * 
     * @return a ITraitsVisitor, used to visit the trait's metadata.
     */
    ITraitVisitor visitMethodTrait(int kind, Name name, int dispID, MethodInfo method);
}
