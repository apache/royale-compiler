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

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;

/**
 * Utility class for writing a function into an {@link IABCVisitor}.
 */
public class FunctionGeneratorHelper
{
    /**
     * Adds the specified {@link MethodInfo} with the specified
     * {@link InstructionList} as its body to the specified {@link IABCVisitor}
     * <p>
     * After calling the method, the specified {@link MethodInfo} and
     * {@link InstructionList} must not be modified.
     * 
     * @param abcVisitor {@link IABCVisitor} to write the function/method into.
     * @param mi {@link MethodInfo} for the function/method
     * @param functionBody {@link InstructionList} for the body of the
     * function/method.
     */
    public static void generateFunction(IABCVisitor abcVisitor, MethodInfo mi, InstructionList functionBody)
    {
        IMethodVisitor methodVisitor = abcVisitor.visitMethod(mi);
        methodVisitor.visit();
        MethodBodyInfo methodBodyInfo = new MethodBodyInfo();
        methodBodyInfo.setMethodInfo(mi);
        IMethodBodyVisitor methodBodyVisitor = methodVisitor.visitBody(methodBodyInfo);
        methodBodyVisitor.visit();
        methodBodyVisitor.visitInstructionList(functionBody);
        methodBodyVisitor.visitEnd();
        methodVisitor.visitEnd();
    }
}
