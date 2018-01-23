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

import java.util.List;

import org.apache.royale.compiler.css.ICSSRule;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSRule}.
 * 
 * @author Gordon Smith
 */
public class CSSRuleTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";
	
	
	protected List<ICSSRule> getCSSRules(String code) {
		return getCSSNodeBase( code ).getRules();
	}
	
	@Test
	public void CSSRulesTests_properties()
	{
		String code = 	     
			    " custom|VBox { " + EOL + 
			    "	fontWeight:bold; " + EOL + 
			    "} ";
		
		List<ICSSRule> rules = getCSSRules(code);
		assertThat("rules.size()" , rules.size(), is(1) );	
		
		CSSRule rule = (CSSRule) rules.get(0);
		assertThat("rule.getOperator()" , rule.getOperator(), is( CSSModelTreeType.RULE ) );
		assertThat("rule.getMediaQueryConditions().size()" , rule.getMediaQueryConditions().size(), is( 0 ) );
		assertThat("rule.getProperties().size()" , rule.getProperties().size(), is( 1 ) );
		assertThat("rule.getSelectorGroup().size()" , rule.getSelectorGroup().size(), is( 1 ) );
		
		CSSTypedNode selectorGroup = (CSSTypedNode) rule.children.get(0);
		assertThat("selectorGroup.getOperator()", selectorGroup.getOperator(), is( CSSModelTreeType.SELECTOR_GROUP ) );
		assertThat("selectorGroup.children.size()", selectorGroup.children.size(), is( 1 ) );
		CSSTypedNode mediaQuery = (CSSTypedNode) rule.children.get(1);
		assertThat("mediaQuery.getOperator()", mediaQuery.getOperator(), is( CSSModelTreeType.MEDIA_QUERY ) );
		assertThat("mediaQuery.children.size()", mediaQuery.children.size(), is( 0 ) );
		CSSTypedNode propertyList = (CSSTypedNode) rule.children.get(2);
		assertThat("propertyList.getOperator()", propertyList.getOperator(), is( CSSModelTreeType.PROPERTY_LIST ) );
		assertThat("propertyList.children.size()", propertyList.children.size(), is( 1 ) );
	}
	
	@Test
	public void CSSRulesTests_multiple_properties()
	{
		String code = 	     
			    " custom|VBox { " + EOL + 
			    "	fontWeight:bold; " + EOL +
			    "	fontSize:16; " + EOL +
			    "} ";
		
		List<ICSSRule> rules = getCSSRules(code);
		assertThat("rules.size()" , rules.size(), is(1) );	
		
		CSSRule rule = (CSSRule) rules.get(0);
		assertThat("rule.getOperator()" , rule.getOperator(), is( CSSModelTreeType.RULE ) );
		assertThat("rule.getMediaQueryConditions().size()" , rule.getMediaQueryConditions().size(), is( 0 ) );
		assertThat("rule.getProperties().size()" , rule.getProperties().size(), is( 2 ) );
		assertThat("rule.getSelectorGroup().size()" , rule.getSelectorGroup().size(), is( 1 ) );
		
		CSSTypedNode selectorGroup = (CSSTypedNode) rule.children.get(0);
		assertThat("selectorGroup.getOperator()", selectorGroup.getOperator(), is( CSSModelTreeType.SELECTOR_GROUP ) );
		assertThat("selectorGroup.children.size()", selectorGroup.children.size(), is( 1 ) );
		CSSTypedNode mediaQuery = (CSSTypedNode) rule.children.get(1);
		assertThat("mediaQuery.getOperator()", mediaQuery.getOperator(), is( CSSModelTreeType.MEDIA_QUERY ) );
		assertThat("mediaQuery.children.size()", mediaQuery.children.size(), is( 0 ) );
		CSSTypedNode propertyList = (CSSTypedNode) rule.children.get(2);
		assertThat("propertyList.getOperator()", propertyList.getOperator(), is( CSSModelTreeType.PROPERTY_LIST ) );
		assertThat("propertyList.children.size()", propertyList.children.size(), is( 2 ) );
	}
	
	@Test
	public void CSSRulesTests_selecterGroup1()
	{
		String code = 	     
				" custom|HBox .rounded custom|Label.big, " + EOL +
			    " custom|VBox .rounded custom|Label.small { " + EOL + 
			    "	fontWeight:bold; " + EOL + 
			    "} ";
		
		List<ICSSRule> rules = getCSSRules(code);
		assertThat("rules.size()" , rules.size(), is(1) );	
		
		CSSRule rule = (CSSRule) rules.get(0);
		assertThat("rule.getOperator()" , rule.getOperator(), is( CSSModelTreeType.RULE ) );
		assertThat("rule.getMediaQueryConditions().size()" , rule.getMediaQueryConditions().size(), is( 0 ) );
		assertThat("rule.getProperties().size()" , rule.getProperties().size(), is( 1 ) );
		assertThat("rule.getSelectorGroup().size()" , rule.getSelectorGroup().size(), is( 2 ) );
		
		CSSTypedNode selectorGroup = (CSSTypedNode) rule.children.get(0);
		assertThat("selectorGroup.getOperator()", selectorGroup.getOperator(), is( CSSModelTreeType.SELECTOR_GROUP ) );
		assertThat("selectorGroup.children.size()", selectorGroup.children.size(), is( 2 ) );
		CSSTypedNode mediaQuery = (CSSTypedNode) rule.children.get(1);
		assertThat("mediaQuery.getOperator()", mediaQuery.getOperator(), is( CSSModelTreeType.MEDIA_QUERY ) );
		assertThat("mediaQuery.children.size()", mediaQuery.children.size(), is( 0 ) );
		CSSTypedNode propertyList = (CSSTypedNode) rule.children.get(2);
		assertThat("propertyList.getOperator()", propertyList.getOperator(), is( CSSModelTreeType.PROPERTY_LIST ) );
		assertThat("propertyList.children.size()", propertyList.children.size(), is( 1 ) );
	}
	
	@Test
	public void CSSRulesTests_selecterGroup2()
	{
		String code = 	     
				" custom|HBox, .rounded, custom|Label.big, " + EOL +
			    " custom|VBox, .rounded, custom|Label.small { " + EOL + 
			    "	fontWeight:bold; " + EOL + 
			    "} ";
		
		List<ICSSRule> rules = getCSSRules(code);
		assertThat("rules.size()" , rules.size(), is(1) );	
		
		CSSRule rule = (CSSRule) rules.get(0);
		assertThat("rule.getOperator()" , rule.getOperator(), is( CSSModelTreeType.RULE ) );
		assertThat("rule.getMediaQueryConditions().size()" , rule.getMediaQueryConditions().size(), is( 0 ) );
		assertThat("rule.getProperties().size()" , rule.getProperties().size(), is( 1 ) );
		assertThat("rule.getSelectorGroup().size()" , rule.getSelectorGroup().size(), is( 6 ) );
		
		CSSTypedNode selectorGroup = (CSSTypedNode) rule.children.get(0);
		assertThat("selectorGroup.getOperator()", selectorGroup.getOperator(), is( CSSModelTreeType.SELECTOR_GROUP ) );
		assertThat("selectorGroup.children.size()", selectorGroup.children.size(), is( 6 ) );
		CSSTypedNode mediaQuery = (CSSTypedNode) rule.children.get(1);
		assertThat("mediaQuery.getOperator()", mediaQuery.getOperator(), is( CSSModelTreeType.MEDIA_QUERY ) );
		assertThat("mediaQuery.children.size()", mediaQuery.children.size(), is( 0 ) );
		CSSTypedNode propertyList = (CSSTypedNode) rule.children.get(2);
		assertThat("propertyList.getOperator()", propertyList.getOperator(), is( CSSModelTreeType.PROPERTY_LIST ) );
		assertThat("propertyList.children.size()", propertyList.children.size(), is( 1 ) );

	}
	
	@Test
	public void CSSRulesTests_mediaQueryConditioselectorGroup()
	{
		
		String code = 
				"@media all and (application-dpi: 240) and (os-platform: \"Android\") { " + EOL +
			    "		custom|Label { fontWeight:bold; } " + EOL +
			    "}";
		
		List<ICSSRule> rules = getCSSRules(code);
		assertThat("rules.size()" , rules.size(), is(1) );	
		
		CSSRule rule = (CSSRule) rules.get(0);
		assertThat("rule.getOperator()" , rule.getOperator(), is( CSSModelTreeType.RULE ) );
		assertThat("rule.getMediaQueryConditions().size()" , rule.getMediaQueryConditions().size(), is( 3 ) );
		assertThat("rule.getProperties().size()" , rule.getProperties().size(), is( 1 ) );
		assertThat("rule.getSelectorGroup().size()" , rule.getSelectorGroup().size(), is( 1 ) );
		
		CSSTypedNode selectorGroup = (CSSTypedNode) rule.children.get(0);
		assertThat("selectorGroup.getOperator()", selectorGroup.getOperator(), is( CSSModelTreeType.SELECTOR_GROUP ) );
		assertThat("selectorGroup.children.size()", selectorGroup.children.size(), is( 1 ) );
		CSSTypedNode mediaQuery = (CSSTypedNode) rule.children.get(1);
		assertThat("mediaQuery.getOperator()", mediaQuery.getOperator(), is( CSSModelTreeType.MEDIA_QUERY ) );
		assertThat("mediaQuery.children.size()", mediaQuery.children.size(), is( 3 ) );
		CSSTypedNode propertyList = (CSSTypedNode) rule.children.get(2);
		assertThat("propertyList.getOperator()", propertyList.getOperator(), is( CSSModelTreeType.PROPERTY_LIST ) );
		assertThat("propertyList.children.size()", propertyList.children.size(), is( 1 ) );
	}
	
	@Test
	public void CSSRulesTests_mediaQueryConditiomediaQuery()
	{
		
		String code = 
				"@media all { " + EOL +
			    "		custom|Label { fontWeight:bold; } " + EOL +
			    "}";
		
		List<ICSSRule> rules = getCSSRules(code);
		assertThat("rules.size()" , rules.size(), is(1) );	
		
		CSSRule rule = (CSSRule) rules.get(0);
		assertThat("rule.getOperator()" , rule.getOperator(), is( CSSModelTreeType.RULE ) );
		assertThat("rule.getMediaQueryConditions().size()" , rule.getMediaQueryConditions().size(), is( 1 ) );
		assertThat("rule.getProperties().size()" , rule.getProperties().size(), is( 1 ) );
		assertThat("rule.getSelectorGroup().size()" , rule.getSelectorGroup().size(), is( 1 ) );
		
		CSSTypedNode selectorGroup = (CSSTypedNode) rule.children.get(0);
		assertThat("selectorGroup.getOperator()", selectorGroup.getOperator(), is( CSSModelTreeType.SELECTOR_GROUP ) );
		assertThat("selectorGroup.children.size()", selectorGroup.children.size(), is( 1 ) );
		CSSTypedNode mediaQuery = (CSSTypedNode) rule.children.get(1);
		assertThat("mediaQuery.getOperator()", mediaQuery.getOperator(), is( CSSModelTreeType.MEDIA_QUERY ) );
		assertThat("mediaQuery.children.size()", mediaQuery.children.size(), is( 1 ) );
		CSSTypedNode propertyList = (CSSTypedNode) rule.children.get(2);
		assertThat("propertyList.getOperator()", propertyList.getOperator(), is( CSSModelTreeType.PROPERTY_LIST ) );
		assertThat("propertyList.children.size()", propertyList.children.size(), is( 1 ) );

	}

}
