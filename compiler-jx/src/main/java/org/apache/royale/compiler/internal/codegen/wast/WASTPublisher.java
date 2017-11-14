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

package org.apache.royale.compiler.internal.codegen.wast;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.codegen.wast.IWASTPublisher;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.internal.projects.RoyaleWASTProject;

public class WASTPublisher implements IWASTPublisher {

    public WASTPublisher(RoyaleWASTProject project, Configuration config)
    {
        this.project = project;
        this.configuration = config;
    }

    protected Configuration configuration;

    protected File outputFolder;
    protected File outputParentFolder;

    protected RoyaleWASTProject project;

	@Override
    public File getOutputFolder()
    {
        outputFolder = new File(getOutputFilePath());
        if (!outputFolder.isDirectory()) {
            outputFolder = outputFolder.getParentFile();
        }

        outputParentFolder = outputFolder;

        setupOutputFolder();

        return outputFolder;
    }

    protected void setupOutputFolder()
    {
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
    }

    private String getOutputFilePath()
    {
        if (configuration.getOutput() == null)
        {
            final String extension = "." + project.getBackend().getOutputExtension();
            return FilenameUtils.removeExtension(configuration.getTargetFile())
                    .concat(extension);
        }
        else {
            return configuration.getOutput();
        }
    }

	@Override
    public boolean publish(ProblemQuery problems) throws IOException
    {
        System.out
                .println("The project has been successfully compiled and optimized.");
        return true;
    }

}
