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

package org.apache.royale.compiler.internal.mxml;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLDataManager;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.StringUtils;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Test;

public class MXMLDataTests
{
	private ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

	protected String getMXML(String[] code)
    {
        return StringUtils.join(code, "\n");
    }
	
	protected IMXMLData getMXMLData(String mxml)
	{
		File tempMXMLFile = null;
		try
		{
			tempMXMLFile = File.createTempFile(getClass().getSimpleName(), ".mxml",
					new File(testAdapter.getTempDir()));
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
	
	/**
	 * Tests that an empty MXML file produces a non-null MXMLData with 0 units.
	 */
	@Test
	public void MXMLData_empty()
	{
		String[] code = new String[]
		{
			""
		};
		IMXMLData mxmlData = getMXMLData(getMXML(code));
		assertThat("mxmlData", mxmlData, is(notNullValue()));
		assertThat("getFileSpecification, getPath", mxmlData.getFileSpecification().getPath(), is(mxmlData.getPath()));
		assertThat("getNumUnits", mxmlData.getNumUnits(), is(0));
		assertThat("getEnd", mxmlData.getEnd(), is(0));
		assertThat("getRootTag", mxmlData.getRootTag(), is(nullValue()));
		assertThat("getMXMLDialect", mxmlData.getMXMLDialect(), is(MXMLDialect.DEFAULT));
		assertThat("getProblems", mxmlData.getProblems().size(), is(0));
	}
}
