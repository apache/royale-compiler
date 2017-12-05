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

import java.util.List;

import org.apache.royale.compiler.css.ICSSCombinator;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSSelector;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * JUnit tests for {@link CSSSelector}.
 * 
 * @author Gordon Smith
 */
public class CSSSelectorTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";
	
    private String getPostfix()
    {
    	return " { " + EOL + 
		    "	fontWeight:bold; " + EOL + 
		    "} ";
    }
    
	@Override
	public ICSSDocument getCSSNodeBase(String code) {
		return super.getCSSNodeBase(code + getPostfix());
	}
	
	private ICSSSelector getCSSFirstSelector(String code) {		
		return getCSSSelectors(code).get(0);
	}
	
	@Test
	public void CSSSelectorTests_selector1()
	{
		String code = " custom|VBox "; 
		
		List<ICSSSelector> selectors = getCSSSelectors(code);
		assertThat("selectors.size()" , selectors.size(), is(1) );	
		
		CSSSelector selector = (CSSSelector) selectors.get(0);
		assertThat("selector" , selector.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector.getCombinator()" , selector.getCombinator() , is( (ICSSCombinator) null ) );
		assertThat("selector.getConditions().size()" , selector.getConditions().size(), is( 0 ) );
		assertThat("selector.getCSSSyntax()" , selector.getCSSSyntax(), is( "custom|VBox" ) );		
		assertThat("selector.getElementName()" , selector.getElementName(), is( "VBox" ) );
		assertThat("selector.getNamespacePrefix()" , selector.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector.isAdvanced()" , selector.isAdvanced(), is( true ) );
	}
	
	@Test
	public void CSSSelectorTests_duplicate_selector()
	{
		String code = " custom|VBox, custom|VBox "; 
		
		List<ICSSSelector> selectors = getCSSSelectors(code);
		assertThat("selectors.size()" , selectors.size(), is(2) );	
		
		CSSSelector selector1 = (CSSSelector) selectors.get(0);
		assertThat("selector1.getOperator()" , selector1.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector.getCombinator()" , selector1.getCombinator() , is( (ICSSCombinator) null ) );
		assertThat("selector1.getConditions().size()" , selector1.getConditions().size(), is( 0 ) );
		assertThat("selector1.getCSSSyntax()" , selector1.getCSSSyntax(), is( "custom|VBox" ) );		
		assertThat("selector1.getElementName()" , selector1.getElementName(), is( "VBox" ) );
		assertThat("selector1.getNamespacePrefix()" , selector1.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector1.isAdvanced()" , selector1.isAdvanced(), is( true ) );
		
		CSSSelector selector2 = (CSSSelector) selectors.get(0);
		assertThat("selector2.getOperator()" , selector2.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector2.getCombinator()" , selector2.getCombinator() , is( (ICSSCombinator) null ) );
		assertThat("selector2.getConditions().size()" , selector2.getConditions().size(), is( 0 ) );
		assertThat("selector2.getCSSSyntax()" , selector2.getCSSSyntax(), is( "custom|VBox" ) );		
		assertThat("selector2.getElementName()" , selector2.getElementName(), is( "VBox" ) );
		assertThat("selector2.getNamespacePrefix()" , selector2.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector2.isAdvanced()" , selector2.isAdvanced(), is( true ) );
	}
	
	@Test
	public void CSSSelectorTests_selector_combination()
	{
		String code = " custom|VBox .test"; 
		
		List<ICSSSelector> selectors = getCSSSelectors(code);
		assertThat("selectors.size()" , selectors.size(), is(1) );	
		
		CSSSelector selector = (CSSSelector) selectors.get(0);
		assertThat("selector.getOperator()" , selector.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector.getCombinator()" , selector.getCombinator() , not( (ICSSCombinator) null ) );
		assertThat("selector.getConditions().size()" , selector.getConditions().size(), is( 1 ) );
		assertThat("selector.getCSSSyntax()" , selector.getCSSSyntax(), is( "custom|VBox .test" ) );		
		assertThat("selector.getElementName()" , selector.getElementName(), is( (String) null ) );
		assertThat("selector.getNamespacePrefix()" , selector.getNamespacePrefix(), is( (String) null ) );
		assertThat("selector.isAdvanced()" , selector.isAdvanced(), is( true ) );
	}
	
	@Test
	public void CSSSelectorTests_selector_conditions()
	{
		String code = " custom|Button.rounded#main:up"; 
		
		List<ICSSSelector> selectors = getCSSSelectors(code);
		assertThat("selectors.size()" , selectors.size(), is(1) );	
		
		CSSSelector selector = (CSSSelector) selectors.get(0);
		assertThat("selector.getOperator()" , selector.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector.getCombinator()" , selector.getCombinator() , is( (ICSSCombinator) null ) );
		assertThat("selector.getConditions().size()" , selector.getConditions().size(), is( 3 ) );
		assertThat("selector.getCSSSyntax()" , selector.getCSSSyntax(), is( "custom|Button.rounded#main:up" ) );		
		assertThat("selector.getElementName()" , selector.getElementName(), is( "Button" ) );
		assertThat("selector.getNamespacePrefix()" , selector.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector.isAdvanced()" , selector.isAdvanced(), is( true ) );

	}
	
	@Test
	public void CSSSelectorTests_selector_combinator()
	{
		String code = " custom|VBox custom|Label .test"; 
		
		List<ICSSSelector> selectors = getCSSSelectors(code);
		assertThat("selectors.size()" , selectors.size(), is(1) );	
		
		CSSSelector selector = (CSSSelector) selectors.get(0);
		assertThat("selector.getOperator()" , selector.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector.getCombinator()" , selector.getCombinator() , not( (ICSSCombinator) null ) );
		assertThat("selector.getConditions().size()" , selector.getConditions().size(), is( 1 ) );
		assertThat("selector.getCSSSyntax()" , selector.getCSSSyntax(), is( "custom|VBox custom|Label .test" ) );		
		assertThat("selector.getElementName()" , selector.getElementName(), is( (String) null ) );
		assertThat("selector.getNamespacePrefix()" , selector.getNamespacePrefix(), is( (String) null ) );
		assertThat("selector.isAdvanced()" , selector.isAdvanced(), is( true ) );

	}
	
	@Test
	public void CSSSelectorTests_selector_combinator_conditions()
	{
		String code = " custom|VBox , custom|HBox custom|Button.rounded#main:up "; 
		
		List<ICSSSelector> selectors = getCSSSelectors(code);
		assertThat("selectors.size()" , selectors.size(), is(2) );	
		
		CSSSelector selector1 = (CSSSelector) selectors.get(0);
		assertThat("selector1.getOperator()" , selector1.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector1.getCombinator()" , selector1.getCombinator() , is( (ICSSCombinator) null ) );
		assertThat("selector1.getConditions().size()" , selector1.getConditions().size(), is( 0 ) );
		assertThat("selector1.getCSSSyntax()" , selector1.getCSSSyntax(), is( "custom|VBox" ) );		
		assertThat("selector1.getElementName()" , selector1.getElementName(), is( "VBox") );
		assertThat("selector1.getNamespacePrefix()" , selector1.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector1.isAdvanced()" , selector1.isAdvanced(), is( true ) );
		
		CSSSelector selector2 = (CSSSelector) selectors.get(1);
		assertThat("selector2.getOperator()" , selector1.getOperator(), is( CSSModelTreeType.SELECTOR ) );
		assertThat("selector2.getCombinator()" , selector2.getCombinator() , not( (ICSSCombinator) null ) );
		assertThat("selector2.getConditions().size()" , selector2.getConditions().size(), is( 3 ) );
		assertThat("selector2.getCSSSyntax()" , selector2.getCSSSyntax(), is( "custom|HBox custom|Button.rounded#main:up" ) );		
		assertThat("selector2.getElementName()" , selector2.getElementName(), is( "Button" ) );
		assertThat("selector2.getNamespacePrefix()" , selector2.getNamespacePrefix(), is( "custom") );
		assertThat("selector2.isAdvanced()" , selector2.isAdvanced(), is( true ) );
	}
	
	
	@Test
	public void CSSSelectorTests_static_getCombinedSelectorList()
	{
		ImmutableList<ICSSSelector> combinedSelectors1 = CSSSelector.getCombinedSelectorList( getCSSFirstSelector("custom|Label .test #Name") );
		assertThat("combinedSelectors1.size()" , combinedSelectors1.size(), is( 3 ) );
		ImmutableList<ICSSSelector> combinedSelectors2 = CSSSelector.getCombinedSelectorList( getCSSFirstSelector("#Name") );
		assertThat("combinedSelectors2.size()" , combinedSelectors2.size(), is( 1 ) );
		ImmutableList<ICSSSelector> combinedSelectors3 = CSSSelector.getCombinedSelectorList( getCSSFirstSelector("custom|Label") );
		assertThat("combinedSelectors3.size()" , combinedSelectors3.size(), is( 1 ) );
		ImmutableList<ICSSSelector> combinedSelectors4 = CSSSelector.getCombinedSelectorList( null );
		assertThat("combinedSelectors4.size()" , combinedSelectors4.size(), is( 0 ) );
	}


}
