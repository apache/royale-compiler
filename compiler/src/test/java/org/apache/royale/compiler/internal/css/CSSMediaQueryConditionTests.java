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
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSMediaQueryCondition;
import org.apache.royale.compiler.css.ICSSPropertyValue;
import org.apache.royale.compiler.css.ICSSRule;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * JUnit tests for {@link CSSMediaQueryCondition}.
 * 
 * @author Gordon Smith
 */
public class CSSMediaQueryConditionTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";
	
    private String getPostfix()
    {
    	return 	"{ " + EOL + 
    			"	custom|VBox { } " + EOL +
				"}"; 
    }
    
	@Override
	public ICSSDocument getCSSNodeBase(String code) {
		return super.getCSSNodeBase(code + getPostfix());
	}
	
	protected List<ICSSMediaQueryCondition> getCSSMediaQueryCondition(String code) {
		ImmutableList<ICSSRule> rules =  getCSSNodeBase( code ).getRules();
		assertThat("rules", rules,  not( (ImmutableList<ICSSRule>) null) );
		List<ICSSMediaQueryCondition> mediaQueryCondition = new ArrayList<ICSSMediaQueryCondition>();
		for (ICSSRule icssRule : rules) {
			mediaQueryCondition.addAll( icssRule.getMediaQueryConditions() );
		}
		assertThat("mediaQueryCondition", mediaQueryCondition,  not( (List<ICSSMediaQueryCondition>) null) );
		return mediaQueryCondition;
	}
	
	@Test
	public void CSSMediaQueryCondition_media1()
	{
		String code = "@media all";
		
		List<ICSSMediaQueryCondition> mediaQueryConditions = getCSSMediaQueryCondition(code);
		assertThat("mediaQueryConditions.size()" , mediaQueryConditions.size(), is(1) );	
		
		ICSSMediaQueryCondition mediaQueryCondition = mediaQueryConditions.get(0);
		assertThat("mediaQueryCondition.getOperator()" , mediaQueryCondition.getOperator(), is( CSSModelTreeType.MEDIA_QUERY_CONDITION ) );
		assertThat("mediaQueryCondition.getKey()" , mediaQueryCondition.getKey(), is( (String) null ) );
		assertThat("mediaQueryCondition.getValue()" , mediaQueryCondition.getValue(), not( (ICSSPropertyValue) null ) );
		assertTrue( mediaQueryCondition.getValue() instanceof CSSKeywordPropertyValue );
		
		CSSKeywordPropertyValue property = (CSSKeywordPropertyValue) mediaQueryCondition.getValue();
		assertThat("property.getKey()" , property.getKeyword(), is( "all" ) );

	}
	
	@Test
	public void CSSMediaQueryCondition_media_combined()
	{
		String code = "@media (application-dpi: 240) and (os-platform: \"Android\") ";
		
		List<ICSSMediaQueryCondition> mediaQueryConditions = getCSSMediaQueryCondition(code);
		assertThat("mediaQueryConditions.size()" , mediaQueryConditions.size(), is(2) );	
		
		ICSSMediaQueryCondition mediaQueryCondition1 = mediaQueryConditions.get(0);
		assertThat("mediaQueryCondition1.getOperator()" , mediaQueryCondition1.getOperator(), is( CSSModelTreeType.MEDIA_QUERY_CONDITION ) );
		assertThat("mediaQueryCondition1.getKey()" , mediaQueryCondition1.getKey(), is( (String) null ) );
		assertThat("mediaQueryCondition1.getValue()" , mediaQueryCondition1.getValue(), not( (ICSSPropertyValue) null ) );
		assertTrue( mediaQueryCondition1.getValue() instanceof CSSKeywordPropertyValue );
		
		CSSKeywordPropertyValue property1 = (CSSKeywordPropertyValue) mediaQueryCondition1.getValue();
		assertThat("property1.getOperator()" , property1.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("property1.getKey()" , property1.getKeyword(), is( "(application-dpi: 240)" ) );
		
		ICSSMediaQueryCondition mediaQueryCondition2 = mediaQueryConditions.get(0);
		assertThat("mediaQueryCondition2.getOperator()" , mediaQueryCondition2.getOperator(), is( CSSModelTreeType.MEDIA_QUERY_CONDITION ) );
		assertThat("mediaQueryCondition2.getKey()" , mediaQueryCondition2.getKey(), is( (String) null ) );
		assertThat("mediaQueryCondition2.getValue()" , mediaQueryCondition2.getValue(), not( (ICSSPropertyValue) null ) );
		assertTrue( mediaQueryCondition2.getValue() instanceof CSSKeywordPropertyValue );
		
		CSSKeywordPropertyValue property2 = (CSSKeywordPropertyValue) mediaQueryCondition2.getValue();
		assertThat("property2.getOperator()" , property2.getOperator(), is( CSSModelTreeType.PROPERTY_VALUE ) );
		assertThat("property2.getKey()" , property2.getKeyword(), is( "(application-dpi: 240)" ) );

	}


}
