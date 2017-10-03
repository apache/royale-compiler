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
 * JUnit tests for {@link CSSNumberPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSNumberPropertyValueTests extends CSSPropertyValueTests {
	
	private List<CSSNumberPropertyValue> getCSSNumberPropertyValues(String code) {
		List<ICSSPropertyValue> propertyValues = getCSSPropertyValues(code);
		List<CSSNumberPropertyValue> stringPropertyValues = new ArrayList<CSSNumberPropertyValue>();
		for (ICSSPropertyValue icssPropertyValue : propertyValues) {
			if(icssPropertyValue instanceof CSSNumberPropertyValue)
				stringPropertyValues.add( (CSSNumberPropertyValue) icssPropertyValue );
		}
		return stringPropertyValues;		
	}
	
	@Test
	public void CSSNumberPropertyValue_number()
	{
		String code = "	fontSize: 10; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(10) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_pixel_number()
	{
		String code = "	fontSize: 10px; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "px" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(10) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_em_number()
	{
		String code = "	fontSize: 10em; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "em" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(10) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_percentage_number()
	{
		String code = "	fontSize: 10%; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "%" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(10) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_negative_number()
	{
		String code = "	left: -10; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(-10) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_positive_number()
	{
		String code = "	left: +10; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(10) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_decimal_number()
	{
		String code = "	left: .31; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()()" , numberPropertyValue.getUnit(), is( "" ) );
		assertThat("numberPropertyValue.getNumber()()" , numberPropertyValue.getNumber(), is( (Number) new Float(.31) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_positive_decimal_number()
	{
		String code = "	left: +10.31; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(10.31) ) );
	}
	
	@Test
	public void CSSNumberPropertyValue_negative_decimal_number()
	{
		String code = "	left: -10.31; ";
		
		List<CSSNumberPropertyValue> numberProperties = getCSSNumberPropertyValues(code);
		assertThat("numberProperties.size()" , numberProperties.size(), is(1) );	
		
		CSSNumberPropertyValue numberPropertyValue = numberProperties.get(0);
		assertThat("numberPropertyValue.getOperator()" , numberPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("numberPropertyValue.getUnit()" , numberPropertyValue.getUnit(), is( "" ) );
		assertThat("numberPropertyValue.getNumber()" , numberPropertyValue.getNumber(), is( (Number) new Float(-10.31) ) );
	}
	

}
