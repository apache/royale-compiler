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

package org.apache.royale.compiler.css;

import java.io.File;
import java.util.Collection;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Manages CSS source file and inline CSS fragments in a project.
 */
public interface ICSSManager
{

    /**
     * @return All the CSS models coming from style modules in the project.
     */
    Collection<ICSSDocument> getCSSFromStyleModules();

    /**
     * @return All the CSS models coming from defaults.css in SWC libraries.
     */
    Collection<ICSSDocument> getCSSFromSWCDefaultStyle();

    /**
     * @param problems Problems loading and parsing theme styles.
     * @return All the CSS models coming from themes.
     */
    Collection<ICSSDocument> getCSSFromThemes(Collection<ICompilerProblem> problems);

    /**
     * Find all the compilation units that the given {@code definitions} depend
     * on in the given {@code cssDocument}.
     * <p>
     * For example, given the following CSS and
     * <code>definition=[A, B, C]</code>
     * 
     * <pre>
     * A { x : ClassReference("M"); }
     * B { x : ClassReference("L"); }
     * P { x : ClassReference("N"); }
     * R { x : Embed("logo.jpg"); }
     * </pre>
     * 
     * The result is a collection:
     * <code>[ CompilationUnit(M), CompilationUnit(L), EmbedCompilationUnit(logo.jpg) ]</code>
     * <br>
     * "M" and "L" are dependencies of "A" and "B". Since "P" is not in the
     * definition list, "N" is not selected. On the other hand, the
     * {@link EmbedCompilationUnit}'s will be created for each "Embed()"
     * property and included in this discovery process.
     * 
     * @param session CSS compilation session data.
     * @param cssDocument A CSS document model.
     * @param definitions A collection of externally visible definitions.
     * @param problems Problems collection.
     */
    Collection<ICompilationUnit> getDependentCompilationUnitsFromCSS(
            CSSCompilationSession session,
            ICSSDocument cssDocument,
            Collection<IDefinition> definitions,
            Collection<ICompilerProblem> problems);

    /**
     * Get the model for "defaults.css" in the given SWC file. If the CSS is not
     * found, return null.
     * 
     * @param swcFile SWC file.
     * @return Model for defaults CSS or null.
     */
    ICSSDocument getDefaultCSS(File swcFile);

    /**
     * Get a CSS model from the CSS cache.
     * 
     * @param cssFilename Normalized path of a CSS file.
     * @return CSS model object, or null if CSS doesn't exist.
     */
    ICSSDocument getCSS(String cssFilename);

    /**
     * In {@code compatibility-version=3} mode, the CSS selectors will be
     * emitted "as-is". They will not be resolved to QNames of the components
     * definitions.
     * <p>
     * For example:
     * 
     * <pre>
     * Button { font-size:12; }
     * 
     * .foo { font-weight:bold; }
     * </pre>
     * 
     * will be compiled as:
     * 
     * <pre>
     * object["Button"] = function() {
     *     this.fontSize = 12; 
     * };
     * 
     * object[".foo"] = function() {
     *     this.fontWeight = "bold";
     * };
     * </pre>
     * 
     * @return True if CSS is compiled in Flex 3 mode.
     */
    boolean isFlex3CSS();
}
