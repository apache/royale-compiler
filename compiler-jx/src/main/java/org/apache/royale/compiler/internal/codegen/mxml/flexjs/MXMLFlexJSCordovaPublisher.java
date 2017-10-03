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

package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import org.apache.commons.io.FilenameUtils;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.internal.projects.RoyaleProject;

import java.io.File;
import java.io.IOException;

public class MXMLRoyaleCordovaPublisher extends MXMLRoyalePublisher
{
    public MXMLRoyaleCordovaPublisher(Configuration config, RoyaleProject project)
    {
        super(project, config);
    }

    @Override
    public boolean publish(ProblemQuery problems) throws IOException
    {
		createCordovaProjectIfNeeded();
		//loadCordovaPlatformsIfNeeded();
		
    	if (super.publish(problems))
    	{
    		//loadCordovaPlugins();
    	}
    	
    	return true;
    }
    
    private void createCordovaProjectIfNeeded()
    {
        // The "intermediate" is the "js-debug" output.
        final File intermediateDir = outputFolder;
        final String projectName = FilenameUtils.getBaseName(configuration.getTargetFile());

        // The "release" is the "js-release" directory.
        File releaseDir = new File(outputParentFolder, FLEXJS_RELEASE_DIR_NAME);

    }
}
