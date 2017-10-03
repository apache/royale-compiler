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

import java.io.Reader;
import java.util.Collection;

import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * A IFXGParser parses an InputStream for an FXG document and builds a custom
 * DOM. Custom FXGNodes can be registered to represent specific elements and
 * elements can also be marked as skipped prior to parsing .
 */
public interface IFXGParser
{
    /**
     * Parses an FXG document reader to produce an IFXGNode based DOM.
     * 
     * @param reader - Reader of the document to be parsed.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the root IFXGNode of the DOM
     */
    IFXGNode parse(Reader reader, Collection<ICompilerProblem> problems);

    /**
     * Parses an FXG document reader to produce an IFXGNode based DOM.
     *
     * @param reader - Reader of the document to be parsed.
     * @param documentPath - the path of the FXG document which can be useful
     * for error reporting.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the root IFXGNode of the DOM
     */
    IFXGNode parse(Reader reader, String documentPath, Collection<ICompilerProblem> problems);
}
