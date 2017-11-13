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

package org.apache.royale.compiler.internal.test;

import java.io.File;
import java.util.List;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.driver.wast.IWASTBackend;
import org.apache.royale.compiler.internal.driver.wast.WASTBackend;
import org.apache.royale.compiler.internal.projects.RoyaleWASTProject;
import org.apache.royale.utils.FilenameNormalization;

/**
 * This class tests the production of WebAssembly Text Format output for for AS package.
 * 
 * @author Erik de Bruin
 */
public class WASTTestBase extends ASTestBase {

    protected RoyaleWASTProject project;

    @Override
    public void setUp()
    {
        super.setUp();

        asEmitter = backend.createEmitter(writer);
        asBlockWalker = ((IWASTBackend) backend).createWalker(project, errors, asEmitter);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(tempDir);
    }
    
    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/" + env.FPVER + "/playerglobal.swc")));
    }

    @Override
    protected IBackend createBackend()
    {
        return new WASTBackend();
    }

}
