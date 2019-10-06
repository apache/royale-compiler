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
 * JUnit tests for {@link CSSStringPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSStringPropertyValueTests extends CSSPropertyValueTests {
		
	private List<CSSStringPropertyValue> getCSSStringPropertyValues(String code) {
		List<ICSSPropertyValue> propertyValues = getCSSPropertyValues(code);
		List<CSSStringPropertyValue> stringPropertyValues = new ArrayList<CSSStringPropertyValue>();
		for (ICSSPropertyValue icssPropertyValue : propertyValues) {
			if(icssPropertyValue instanceof CSSStringPropertyValue)
				stringPropertyValues.add( (CSSStringPropertyValue) icssPropertyValue );
		}
		return stringPropertyValues;		
	}
	
	@Test
	public void CSSStringPropertyValue_doubleQuoted_text()
	{
		String code = "	fontFamily: \"Verdana\"; ";
		
		List<CSSStringPropertyValue> stringProperties = getCSSStringPropertyValues(code);
		assertThat("stringProperties.size()" , stringProperties.size(), is(1) );	
		
		CSSStringPropertyValue stringPropertyValue = stringProperties.get(0);
		assertThat("stringPropertyValue" , stringPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("stringPropertyValue.getValue()" , stringPropertyValue.getValue(), is( "Verdana" ) );
	}
	
	@Test
	public void CSSStringPropertyValue_singleQuoted_text()
	{
		String code = "	fontFamily: 'Verdana'; ";
		
		List<CSSStringPropertyValue> stringProperties = getCSSStringPropertyValues(code);
		assertThat("stringProperties.size()" , stringProperties.size(), is(1) );	
		
		CSSStringPropertyValue stringPropertyValue = stringProperties.get(0);
		assertThat("stringPropertyValue" , stringPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("stringPropertyValue.getValue()" , stringPropertyValue.getValue(), is( "Verdana" ) );
	}
	
	@Test
	public void CSSStringPropertyValue_doubleQuoted_text2()
	{
		String code = "	fontFamily: \"Ver'dana\"; ";
		
		errorFilters = new String[3];
		errorFilters[0] = "mismatched character '<EOF>' expecting set null";
		errorFilters[1] = "unexpected token '<EOF>' expecting CSS property name";
		errorFilters[2] = "mismatched input '<EOF>' expecting BLOCK_END";
		List<CSSStringPropertyValue> stringProperties = getCSSStringPropertyValues(code);
		assertThat("stringProperties.size()" , stringProperties.size(), is(0) );	
	}

}
