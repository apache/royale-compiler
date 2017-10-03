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

package org.apache.royale.compiler.ant.types;

import java.io.File;

import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.util.FileUtils;

import org.apache.royale.compiler.ant.config.OptionSpec;

public class FlexSWCFileSet extends FlexFileSet 
{
	public FlexSWCFileSet(OptionSpec spec, boolean dirs)
	{
		super(spec, dirs);
	}

	// Only accept directories, and *.swc files
    protected void addFiles(File base, String[] files, Commandline cmdline)
    {
        FileUtils utils = FileUtils.getFileUtils();
 
        for (int i = 0; i < files.length; i++)
        {
            File f = utils.resolveFile(base, files[i]);
            String absolutePath = f.getAbsolutePath();

            if (f.isFile() && !absolutePath.endsWith(".swc"))
            	continue;

            if (spec != null)
                cmdline.createArgument().setValue("-" + spec.getFullName() + equalString() + absolutePath);
             else
                cmdline.createArgument().setValue(absolutePath);
        }    
    }
}
