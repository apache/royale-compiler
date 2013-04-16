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

package org.apache.flex.compiler.internal.mxml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.flex.compiler.filespecs.FileSpecification;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.mxml.IMXMLData;
import org.apache.flex.compiler.mxml.IMXMLDataManager;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.StringUtils;
import org.junit.Ignore;

@Ignore
public class MXMLDataTests
{
	protected String getMXML(String[] code)
    {
        return StringUtils.join(code, "\n");
    }
	
	protected IMXMLData getMXMLData(String mxml)
	{
		String tempDir = FilenameNormalization.normalize("temp"); // ensure this exists
		
		File tempMXMLFile = null;
		try
		{
			tempMXMLFile = File.createTempFile(getClass().getSimpleName(), ".mxml", new File(tempDir));
			tempMXMLFile.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(tempMXMLFile));
		    out.write(mxml);
		    out.close();
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}

		IFileSpecification fileSpec = new FileSpecification(tempMXMLFile.getPath());
		
		Workspace workspace = new Workspace();
		IMXMLDataManager mxmlDataManager = workspace.getMXMLDataManager();
		IMXMLData mxmlData = mxmlDataManager.get(fileSpec);
		return mxmlData;
	}
}
