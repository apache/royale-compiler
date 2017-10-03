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

import com.google.common.collect.ImmutableList;

/**
 * JUnit tests for {@link CSSArrayPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSArrayPropertyValueTests extends CSSPropertyValueTests {
		
	private List<CSSArrayPropertyValue> getCSSArrayPropertyValues(String code) {
		List<ICSSPropertyValue> propertyValues = getCSSPropertyValues(code);
		List<CSSArrayPropertyValue> stringPropertyValues = new ArrayList<CSSArrayPropertyValue>();
		for (ICSSPropertyValue icssPropertyValue : propertyValues) {
			if(icssPropertyValue instanceof CSSArrayPropertyValue)
				stringPropertyValues.add( (CSSArrayPropertyValue) icssPropertyValue );
		}
		return stringPropertyValues;		
	}
	
	@Test
	public void CSSArrayPropertyValue_quoted_text()
	{
		String code = "	fillColor: #FFFFFF, #CCCCCC, #FFFFFF, #EEEEEE;";
		
		List<CSSArrayPropertyValue> arrayPropertyValues = getCSSArrayPropertyValues(code);
		assertThat("arrayPropertyValues.size()" , arrayPropertyValues.size(), is(1) );	
		
		CSSArrayPropertyValue arrayPropertyValue = arrayPropertyValues.get(0);
		assertThat("colorPropertyValue.getOperator()" , arrayPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("arrayPropertyValue.getElements().size()" , arrayPropertyValue.getElements().size(), is( 4) );
		
		ImmutableList<? extends ICSSPropertyValue> elements = arrayPropertyValue.getElements();	
		assertThat("element 0" , ((CSSColorPropertyValue)elements.get(0)).getText() , is( "#FFFFFF"  ) );
		assertThat("element 1" , ((CSSColorPropertyValue)elements.get(1)).getText() , is( "#CCCCCC"  ) );
		assertThat("element 2" , ((CSSColorPropertyValue)elements.get(2)).getText() , is( "#FFFFFF"  ) );
		assertThat("element 3" , ((CSSColorPropertyValue)elements.get(3)).getText() , is( "#EEEEEE"  ) );
	}
	
	@Test
	public void CSSArrayPropertyValue_quoted_text1()
	{
		String code = "	fillColor: #FFFFFF, 'String', Red, 0, Embed('image.gif'), bold; ";
		
		List<CSSArrayPropertyValue> arrayPropertyValues = getCSSArrayPropertyValues(code);
		assertThat("arrayPropertyValues.size()" , arrayPropertyValues.size(), is(1) );	
		
		CSSArrayPropertyValue arrayPropertyValue = arrayPropertyValues.get(0);
		assertThat("arrayPropertyValue.getOperator()" , arrayPropertyValue.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("arrayPropertyValue.getElements().size()" , arrayPropertyValue.getElements().size(), is( 6 ) );

		ImmutableList<? extends ICSSPropertyValue> elements = arrayPropertyValue.getElements();	
		assertThat("element 0" , ((CSSColorPropertyValue)elements.get(0)).getText() , is( "#FFFFFF"  ) );
		assertThat("element 1" , ((CSSStringPropertyValue)elements.get(1)).getValue() , is( "String"  ) );
		assertThat("element 2" , ((CSSColorPropertyValue)elements.get(2)).getText() , is( "Red"  ) );
		assertThat("element 3" , ((CSSNumberPropertyValue)elements.get(3)).getNumber() , is( (Number) new Float(0) ) );
		assertThat("element 4" , ((CSSFunctionCallPropertyValue)elements.get(4)).name , is( CSSFunctionCallPropertyValue.EMBED  ) );
		assertThat("element 5" , ((CSSKeywordPropertyValue)elements.get(5)).getKeyword() , is( "bold" ) );

	}
	

}
