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
 * JUnit tests for {@link CSSKeywordPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSKeywordPropertyValueTests extends CSSPropertyValueTests {
	
	private List<CSSKeywordPropertyValue> getCSSKeywordPropertyValues(String code) {
		List<ICSSPropertyValue> propertyValues = getCSSPropertyValues(code);
		List<CSSKeywordPropertyValue> stringPropertyValues = new ArrayList<CSSKeywordPropertyValue>();
		for (ICSSPropertyValue icssPropertyValue : propertyValues) {
			if(icssPropertyValue instanceof CSSKeywordPropertyValue)
				stringPropertyValues.add( (CSSKeywordPropertyValue) icssPropertyValue );
		}
		return stringPropertyValues;		
	}
	
	@Test
	public void CSSKeywordPropertyValue_keyword()
	{
		String code = "	color: keyWord; ";
		
		List<CSSKeywordPropertyValue> keywordProperties = getCSSKeywordPropertyValues(code);
		assertThat("keywordProperties.size()" , keywordProperties.size(), is(1) );	
		
		CSSKeywordPropertyValue keywordPropertyValue = keywordProperties.get(0);
		assertThat("keywordPropertyValue.getOperator()" , keywordPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("keywordPropertyValue.getValue()" , keywordPropertyValue.getKeyword(), is( "keyWord" ) );
	}
	
	@Test
	public void CSSKeywordPropertyValue_color_keyword()
	{
		String code = "	color: orange; ";
		
		List<CSSKeywordPropertyValue> keywordProperties = getCSSKeywordPropertyValues(code);
		assertThat("keywordProperties.size()" , keywordProperties.size(), is(0) );	
	}

}
