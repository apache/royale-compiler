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
 * JUnit tests for {@link CSSColorPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSColorPropertyValueTests extends CSSPropertyValueTests {
	
	private List<CSSColorPropertyValue> getCSSColorPropertyValues(String code) {
		List<ICSSPropertyValue> propertyValues = getCSSPropertyValues(code);
		List<CSSColorPropertyValue> colorPropertyValues = new ArrayList<CSSColorPropertyValue>();
		for (ICSSPropertyValue icssPropertyValue : propertyValues) {
			if(icssPropertyValue instanceof CSSColorPropertyValue)
				colorPropertyValues.add( (CSSColorPropertyValue) icssPropertyValue );
		}
		return colorPropertyValues;		
	}
	
	@Test
	public void CSSColorPropertyValue_textColor()
	{
		String code = "	color: blue; ";
		
		List<CSSColorPropertyValue> colorProperties = getCSSColorPropertyValues(code);
		assertThat("colorProperties.size()" , colorProperties.size(), is(1) );	
		
		CSSColorPropertyValue colorPropertyValue = colorProperties.get(0);
		assertThat("colorPropertyValue.getOperator()" , colorPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("colorPropertyValue.getText()" , colorPropertyValue.getText(), is( "blue" ) );
		assertThat("colorPropertyValue.getColorAsInt()" , colorPropertyValue.getColorAsInt(), is( 255 ) );
	}
	
	@Test
	public void CSSColorPropertyValue_wrong_textColor()
	{
		String code = "	color: xred; ";
		
		List<CSSColorPropertyValue> colorProperties = getCSSColorPropertyValues(code);
		assertThat("colorProperties.size()" , colorProperties.size(), is(0) );		
	}
	
	@Test
	public void CSSColorPropertyValue_textColor_upperCase()
	{
		String code = "	color: RED; ";
		
		List<CSSColorPropertyValue> colorProperties = getCSSColorPropertyValues(code);
		assertThat("colorProperties.size()" , colorProperties.size(), is(1) );	
		
		CSSColorPropertyValue colorPropertyValue = colorProperties.get(0);
		assertThat("colorPropertyValue.getOperator()" , colorPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("colorPropertyValue.getText()" , colorPropertyValue.getText(), is( "RED" ) );
		assertThat("colorPropertyValue.getColorAsInt()" , colorPropertyValue.getColorAsInt(), is( 16711680 ) );
	}
	
	@Test
	public void CSSColorPropertyValue_hexColor()
	{
		String code = "	color: #FF0000; ";
		
		List<CSSColorPropertyValue> colorProperties = getCSSColorPropertyValues(code);
		assertThat("colorProperties.size()" , colorProperties.size(), is(1) );	
		
		CSSColorPropertyValue colorPropertyValue = colorProperties.get(0);
		assertThat("colorPropertyValue.getOperator()" , colorPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("colorPropertyValue.getText()" , colorPropertyValue.getText(), is( "#FF0000" ) );
		assertThat("colorPropertyValue.getColorAsInt()" , colorPropertyValue.getColorAsInt(), is( 16711680 ) );
	}
	
	@Test
	public void CSSColorPropertyValue_intColor()
	{
		String code = "	color: 0xFF0000; ";
		
		List<CSSColorPropertyValue> colorProperties = getCSSColorPropertyValues(code);
		assertThat("colorProperties.size()" , colorProperties.size(), is(0) );	
	}
	
	@Test
	public void CSSColorPropertyValue_wrong_hexColor()
	{
		String code = "	color: FF0000; ";

		List<CSSColorPropertyValue> colorProperties = getCSSColorPropertyValues(code);
		assertThat("colorProperties.size()" , colorProperties.size(), is(0) );	
	}
	

	// not sure you can expect exception since compiler catches everything
	@Test(expected = NumberFormatException.class)
	public void CSSColorPropertyValue_wrong_hexColor2()
	{
		String code = "	color: #FF00FG; ";

		errorFilters = new String[1];
		errorFilters[0] = "Unexpected exception 'java.lang.NumberFormatException";
		List<CSSColorPropertyValue> colorProperties = getCSSColorPropertyValues(code);
		assertThat("colorProperties.size()" , colorProperties.size(), is(0) );	
	}

}
