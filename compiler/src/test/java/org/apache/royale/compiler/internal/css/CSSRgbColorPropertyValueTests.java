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
 * JUnit tests for {@link CSSRgbColorPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSRgbColorPropertyValueTests extends CSSPropertyValueTests {
	
	private List<CSSRgbColorPropertyValue> getCSSRgbPropertyValues(String code) {
		List<ICSSPropertyValue> propertyValues = getCSSPropertyValues(code);
		List<CSSRgbColorPropertyValue> colorPropertyValues = new ArrayList<CSSRgbColorPropertyValue>();
		for (ICSSPropertyValue icssPropertyValue : propertyValues) {
			if(icssPropertyValue instanceof CSSRgbColorPropertyValue)
				colorPropertyValues.add( (CSSRgbColorPropertyValue) icssPropertyValue );
		}
		return colorPropertyValues;		
	}
	
	@Test
	public void CSSColorPropertyValue_textColor()
	{
		String code = "	color: rgb(100%, 0%, 0%); ";
		
		List<CSSRgbColorPropertyValue> rgbColorProperties = getCSSRgbPropertyValues(code);
		assertThat("rgbColorProperties.size()" , rgbColorProperties.size(), is(1) );	
		
		CSSRgbColorPropertyValue rgbColorPropertyValue = rgbColorProperties.get(0);
		assertThat("rgbColorPropertyValue.getOperator()" , rgbColorPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("rgbColorPropertyValue.getRawRgb()" , rgbColorPropertyValue.getRawRgb(), is( "rgb(100%, 0%, 0%)" ) );
		assertThat("rgbColorPropertyValue.getColorAsInt()" , rgbColorPropertyValue.getColorAsInt(), is(  Integer.parseInt("ff0000", 16) ) );
	}
	
	@Test
	public void CSSColorPropertyValue_textColor2()
	{
		String code = "	color: rgb(100%, 255, 100%); ";
		
		List<CSSRgbColorPropertyValue> rgbColorProperties = getCSSRgbPropertyValues(code);
		assertThat("rgbColorProperties.size()" , rgbColorProperties.size(), is(1) );	
		
		CSSRgbColorPropertyValue rgbColorPropertyValue = rgbColorProperties.get(0);
		assertThat("rgbColorPropertyValue.getOperator()" , rgbColorPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("rgbColorPropertyValue.getRawRgb()" , rgbColorPropertyValue.getRawRgb(), is( "rgb(100%, 255, 100%)" ) );
		assertThat("rgbColorPropertyValue.getColorAsInt()" , rgbColorPropertyValue.getColorAsInt(), is( Integer.parseInt("ffffff",16) ) );
	}
	
	@Test
	public void CSSColorPropertyValue_textColor3()
	{
		String code = "	color: rgb(100%, , 100%); ";
		
		errorFilters = new String[2];
		errorFilters[0] = "missing STRING at ';'";
		errorFilters[1] = "no viable alternative at character";
		List<CSSRgbColorPropertyValue> rgbColorProperties = getCSSRgbPropertyValues(code);
		assertThat("rgbColorProperties.size()" , rgbColorProperties.size(), is(0) );	
	}
	
	@Test
	public void CSSRgbPropertyValue_getRgbValues() {
		
		assertThat(CSSRgbColorPropertyValue.getIntValue( "rgb(100%, 255, 0)" ), 
				is( Integer.parseInt("ffff00", 16) ));

		assertThat(CSSRgbColorPropertyValue.getIntValue( "rgb(100%, 100%, 100%)" ), 
				is( Integer.parseInt("ffffff", 16) ));
		
		assertThat(CSSRgbColorPropertyValue.getIntValue( "rgb(100%, 255, 100%)" ), 
				is( Integer.parseInt("ffffff", 16) ));
		 	
		assertThat(CSSRgbColorPropertyValue.getIntValue( "rgb(100%, 2%, 100%)" ), 
				is( Integer.parseInt("ff05ff", 16) ));

		assertThat(CSSRgbColorPropertyValue.getIntValue( "rgb(256, 0, 0)" ), 
				is( Integer.parseInt("000000", 16) ));
		
		assertThat(CSSRgbColorPropertyValue.getIntValue( "rgb(254.5, 99.5%, 0)" ), 
				is( Integer.parseInt("fefc00", 16) ));
		
	}

}
