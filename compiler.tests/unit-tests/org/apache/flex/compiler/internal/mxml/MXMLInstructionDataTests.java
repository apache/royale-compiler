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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.mxml.IMXMLInstructionData;
import org.apache.flex.compiler.mxml.IMXMLUnitData;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLInstructionData}.
 * 
 * @author Gordon Smith
 */
public class MXMLInstructionDataTests extends MXMLUnitDataTests
{
	private IMXMLInstructionData getMXMLInstructionData(String[] code)
	{
		IMXMLUnitData unitData = getMXMLUnitData(code);
		assertThat("instanceof", unitData instanceof IMXMLInstructionData, is(true));
		return (IMXMLInstructionData)unitData;
	}

	@Test
	public void MXMLInstructionData_empty1()
	{
		String[] code = new String[]
		{
			"<??>"
		};
		IMXMLInstructionData instructionData = getMXMLInstructionData(code);
		assertThat("getInstructionText", instructionData.getInstructionText(), is(code[0]));
	}
	
	@Test
	public void MXMLInstructionData_minimal()
	{
		String[] code = new String[]
		{
			"<?xml?>"
		};
		IMXMLInstructionData instructionData = getMXMLInstructionData(code);
		assertThat("getInstructionText", instructionData.getInstructionText(), is(code[0]));
	}
	
	@Test
	public void MXMLInstructionData_typical()
	{
		String[] code = new String[]
		{
			"<?xml version='1.0' encoding='utf-8'?>"
		};
		IMXMLInstructionData instructionData = getMXMLInstructionData(code);
		assertThat("getInstructionText", instructionData.getInstructionText(), is(code[0]));
	}
	

}
