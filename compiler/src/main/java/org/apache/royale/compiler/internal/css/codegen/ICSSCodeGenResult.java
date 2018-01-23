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

package org.apache.royale.compiler.internal.css.codegen;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.visitors.ITraitsVisitor;

/**
 * Interface to results of the CSS code generator.
 */
public interface ICSSCodeGenResult
{
    /**
     * @return The {@link InstructionList} containing instructions for
     * initializing the CSS data structures. This method does not append
     * {@code returnvoid} instructions to the end of the instruction list.
     */
    InstructionList getClassInitializationInstructions();
    
    /**
     * Add generated data slots for generated data structures to the specified
     * {@link ITraitsVisitor}.
     * 
     * @param classTraitsVisitor {@link ITraitsVisitor} to which traits for the
     * css data structures are added.
     */
    void visitClassTraits(ITraitsVisitor classTraitsVisitor);
}
