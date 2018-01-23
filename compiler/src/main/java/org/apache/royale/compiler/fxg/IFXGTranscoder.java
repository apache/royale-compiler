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

package org.apache.royale.compiler.fxg;

import java.util.Collection;
import java.util.Map;

import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.fxg.flex.FXGSymbolClass;
import org.apache.royale.compiler.fxg.resources.IFXGResourceResolver;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.tags.ITag;

/**
 * Simple interface for a transcoder on an FXG DOM.
 */
public interface IFXGTranscoder
{
    /**
     * Establishes the ResourceResolver implementation used to locate and load
     * resources such as embedded images for BitmapGraphic nodes.
     * 
     * @param resolver fxg resource resolver
     */
    void setResourceResolver(IFXGResourceResolver resolver);

    /**
     * Traverses an FXG DOM to generate a SWF graphics primitives, with the root
     * DefineSpriteTag associated with a generated ActionScript symbol class if
     * any.
     * 
     * @param node the root node of an FXG DOM.
     * @param packageName the package name of the generated symbol class.
     * @param className the class name of the generated symbol class.
     * @param extraTags any extra tags that are associated with tags in this FXG. 
     * The unit calling transcoder should add these to the frame after their keys are added.
     * @param problems problem collection used to collect problems occurred within this method
     * @return an FXGSymbolClass context that includes generated sources and
     * associated DefineSpriteTags.
     */
    FXGSymbolClass transcode(IFXGNode node, String packageName, String className, Map<ITag, ITag> extraTags, Collection<ICompilerProblem> problems);
    
    /**
     * Returns a list of types the FXG processed by this transcoder depends
     * on.
     */
    ITypeDefinition[] getDependencies();
}
