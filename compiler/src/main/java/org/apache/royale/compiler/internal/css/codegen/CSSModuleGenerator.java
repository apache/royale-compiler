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

import java.util.Collections;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.abc.ClassGeneratorHelper;
import org.apache.royale.compiler.internal.css.codegen.CSSEmitter;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * This class generates the "main" class and "style data" class for a CSS module
 * SWF.
 * 
 * <pre>
 * package
 * {
 * 
 * [ExcludeClass]
 * public class CSSModule2Main extends StyleModuleBase
 * {
 * }
 * 
 * public class CSSModule2_StyleData
 * {
 *     public static var factoryFunctions:Object;
 *     public static var inheritingStyles:String;
 *     public static var data:Array
 * }
 * 
 * }
 * </pre>
 */
public class CSSModuleGenerator
{

    /**
     * Generate CSS module main class.
     * 
     * @param emitter ABC emitter.
     * @param project Compiler project.
     * @param mainClassName Name of the main class.
     */
    public static void generateMainClass(final IABCVisitor emitter,
                                         final ICompilerProject project,
                                         final Name mainClassName)
    {
        final IDefinition styleModuleBaseDefinition = project.resolveQNameToDefinition("StyleModuleBase");
        if (styleModuleBaseDefinition instanceof ClassDefinition)
        {
            final ClassGeneratorHelper helper = new ClassGeneratorHelper(
                    project,
                    emitter,
                    mainClassName,
                    (ClassDefinition)styleModuleBaseDefinition,
                    ClassGeneratorHelper.returnVoid());
            helper.finishScript();
        }
        else
        {
            throw new IllegalStateException("Unable to resolve 'StyleModuleBase' for CSS module.");
        }
    }

    /**
     * Generating CSS module style data class.
     * 
     * @param abcEmitter ABC emitter.
     * @param project Compiler project.
     * @param cssDocument CSS model.
     * @throws Exception CSS code generation exception.
     */
    public static void generateStyleDataClass(final IABCVisitor abcEmitter,
                                              final RoyaleProject project,
                                              final ICSSDocument cssDocument,
                                              final CSSCompilationSession cssCompilationSession,
                                              final Name stylesDataClassName)
                                              throws Exception
    {
        final CSSReducer reducer = new CSSReducer(project, cssDocument, abcEmitter, cssCompilationSession, true, 0);
        final CSSEmitter cssEmitter = new CSSEmitter(reducer);
        cssEmitter.burm(cssDocument);
        
        final InstructionList cinit = reducer.getClassInitializationInstructions();
        cinit.addInstruction(ABCConstants.OP_returnvoid);
        final ClassGeneratorHelper helper = new ClassGeneratorHelper(
                project, 
                abcEmitter, 
                stylesDataClassName, 
                (ClassDefinition)project.resolveQNameToDefinition(IASLanguageConstants.Object), 
                Collections.<Name>emptyList(), 
                Collections.<Name>emptyList(), 
                ClassGeneratorHelper.returnVoid(), 
                cinit, 
                false);
        reducer.visitClassTraits(helper.getCTraitsVisitor());
        helper.finishScript();
    }
}
