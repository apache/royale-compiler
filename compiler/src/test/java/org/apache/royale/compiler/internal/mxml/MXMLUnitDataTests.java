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

import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.junit.Ignore;

/**
 * Base class for JUnit tests of {@link MXMLUnitData} subclasses.
 * <p>
 * Each test tests a tiny portion of MXML that contains only a single unit.
 * 
 * @author Gordon Smith
 */
@Ignore
public class MXMLUnitDataTests extends MXMLDataTests
{
    protected IMXMLUnitData getMXMLUnitData(String[] code)
    {
    	String mxml = getMXML(code);
    	IMXMLData mxmlData = getMXMLData(mxml);
    	assertThat("mxmlData", mxmlData, is(notNullValue()));
		assertThat("getFileSpecification, getPath", mxmlData.getFileSpecification().getPath(), is(mxmlData.getPath()));
		assertThat("getNumUnits", mxmlData.getNumUnits(), is(1));
		assertThat("getEnd", mxmlData.getEnd(), is(mxml.length()));
		assertThat("getRootTag", mxmlData.getRootTag(), is(nullValue()));
		assertThat("getMXMLDialect", mxmlData.getMXMLDialect(), is(MXMLDialect.DEFAULT));
		assertThat("getProblems", mxmlData.getProblems().size(), is(0));
		return mxmlData.getUnit(0);
    }
}
