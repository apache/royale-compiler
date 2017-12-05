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

package org.apache.royale.compiler.internal.css;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.css.ICSSPropertyValue;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSFunctionCallPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSFunctionCallPropertyValueTests extends CSSPropertyValueTests {
		
	private List<CSSFunctionCallPropertyValue> getCSSFunctionCallPropertyValues(String code) {
		List<ICSSPropertyValue> propertyValues = getCSSPropertyValues(code);
		List<CSSFunctionCallPropertyValue> stringPropertyValues = new ArrayList<CSSFunctionCallPropertyValue>();
		for (ICSSPropertyValue icssPropertyValue : propertyValues) {
			if(icssPropertyValue instanceof CSSFunctionCallPropertyValue)
				stringPropertyValues.add( (CSSFunctionCallPropertyValue) icssPropertyValue );
		}
		return stringPropertyValues;		
	}
	
	@Test
	public void CSSFunctionCallPropertyValue_embed()
	{
		String code = "	overSkin: Embed(\"image.gif\");";

		errorFilters = new String[2];
		errorFilters[0] = "missing STRING at ';'";
		errorFilters[1] = "image.gif";

		List<CSSFunctionCallPropertyValue> functionCallProperties = getCSSFunctionCallPropertyValues(code);
		assertThat("functionCallProperties.size()" , functionCallProperties.size(), is(1) );	
		
		CSSFunctionCallPropertyValue functionCallPropertyValue = functionCallProperties.get(0);
		assertThat("functionCallPropertyValue.getOperator()" , functionCallPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("functionCallPropertyValue.name" , functionCallPropertyValue.name, is( CSSFunctionCallPropertyValue.EMBED ) );
		assertThat("functionCallPropertyValue.rawArguments" , functionCallPropertyValue.rawArguments, is( "\"image.gif\"" ) );
	}	
	
	@Test
	public void CSSFunctionCallPropertyValue_propertyReference()
	{
		String code = "	color: PropertyReference('colorValue');";
		
		List<CSSFunctionCallPropertyValue> functionCallProperties = getCSSFunctionCallPropertyValues(code);
		assertThat("functionCallProperties.size()" , functionCallProperties.size(), is(1) );	
		
		CSSFunctionCallPropertyValue functionCallPropertyValue = functionCallProperties.get(0);
		assertThat("functionCallPropertyValue.getOperator()" , functionCallPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("functionCallPropertyValue.name" , functionCallPropertyValue.name, is( CSSFunctionCallPropertyValue.PROPERTY_REFERENCE ) );
		assertThat("functionCallPropertyValue.rawArguments" , functionCallPropertyValue.rawArguments, is( "'colorValue'" ) );
	}	
	
	@Test
	public void CSSFunctionCallPropertyValue_classReference()
	{
		String code = "	overSkin: ClassReference('custom.TestInstance');";
		
		List<CSSFunctionCallPropertyValue> functionCallProperties = getCSSFunctionCallPropertyValues(code);
		assertThat("functionCallProperties.size()" , functionCallProperties.size(), is(1) );	
		
		CSSFunctionCallPropertyValue functionCallPropertyValue = functionCallProperties.get(0);
		assertThat("functionCallPropertyValue.getOperator()" , functionCallPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("functionCallPropertyValue.name" , functionCallPropertyValue.name, is( CSSFunctionCallPropertyValue.CLASS_REFERENCE ) );
		assertThat("functionCallPropertyValue.rawArguments" , functionCallPropertyValue.rawArguments, is( "'custom.TestInstance'" ) );
	}
	
	@Test
	public void CSSFunctionCallPropertyValue_static_getSingleArgumentFromRaw() {
		assertThat("", CSSFunctionCallPropertyValue.getSingleArgumentFromRaw("\"Test\""), is("Test") );
		assertThat("", CSSFunctionCallPropertyValue.getSingleArgumentFromRaw("'Test'"), is("Test") );
		assertThat("", CSSFunctionCallPropertyValue.getSingleArgumentFromRaw("'Test"), is("'Test") );
		assertThat("", CSSFunctionCallPropertyValue.getSingleArgumentFromRaw("'Test\""), is("'Test\"") );
		assertThat("", CSSFunctionCallPropertyValue.getSingleArgumentFromRaw("Test"), is("Test") );
	}

}
