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

package org.apache.royale.compiler.driver;

import java.io.File;
import java.io.FilterWriter;

import org.apache.royale.compiler.clients.MXMLJSC;
import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * The backend strategy for the {@link MXMLJSC} javascript compiler.
 * 
 * @author Michael Schmalle
 */
public interface IBackend
{

    /**
     * Returns the instance that is used to manage what type of
     * {@link ICompilationUnit} is created during parsing.
     * 
     * @return The implemented {@link ISourceFileHandler}.
     */
    ISourceFileHandler getSourceFileHandlerInstance();

    /**
     * Returns the {@link File} extension used when saving compiled code.
     */
    String getOutputExtension();

    /**
     * Creates a {@link Configurator} for the specific compile session.
     */
    Configurator createConfigurator();

    IDocEmitter createDocEmitter(IASEmitter emitter);

    IASEmitter createEmitter(FilterWriter writer);

    IMXMLEmitter createMXMLEmitter(FilterWriter writer);

}
