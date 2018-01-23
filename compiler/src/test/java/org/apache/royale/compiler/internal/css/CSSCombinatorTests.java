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

import org.apache.royale.compiler.css.CombinatorType;
import org.apache.royale.compiler.css.ICSSCombinator;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSSelector;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSCombinator}.
 * 
 * @author Gordon Smith
 */
public class CSSCombinatorTests extends CSSBaseTests {
		
    private String getPostfix()
    {
    	return " { } ";
    }
    
	@Override
	public ICSSDocument getCSSNodeBase(String code) {
		return super.getCSSNodeBase(code + getPostfix());
	}
	
	
	protected CSSCombinator getCSSCombinator(String code) {
		List<ICSSSelector> selectors =  getCSSSelectors( code );
		assertThat("selectors", selectors,  not( (List<ICSSSelector>) null) );
		assertThat("selectors.size()", selectors.size(),  is( 1 ) );

		ICSSSelector selector = selectors.get(0);
		assertThat("selector", selector,  not( (ICSSSelector) null) );

		return (CSSCombinator) selector.getCombinator();
	}

	
	@Test
	public void CSSSelectorConditionTests_descendant_combinator1()
	{
		String code = " custom|VBox custom|Label "; 
		
		CSSCombinator combinator = getCSSCombinator(code);
		//TODO why CSSCombinator doesn't extend CSSNodeBase?? 
		assertThat("combinator.getOperator()" , combinator.getOperator(), is( CSSModelTreeType.COMBINATOR ) );
		assertThat("combinator.getCombinatorType()" , combinator.getCombinatorType(), is(CombinatorType.DESCENDANT) );
		assertThat("combinator.getSelector()" , combinator.getSelector(), not( (ICSSSelector) null) );
		
		ICSSSelector selector = combinator.getSelector();
		assertThat("selector.getElementName()" , selector.getElementName(), is( "VBox" ) );
		assertThat("selector.getNamespacePrefix()" , selector.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector.getCombinator()" , selector.getCombinator(), is( (ICSSCombinator) null) );
	}
	
	@Test
	public void CSSSelectorConditionTests_descendant_combinator2()
	{
		String code = " custom|VBox custom|HBox custom|Label"; 
		
		CSSCombinator combinator = getCSSCombinator(code);
		assertThat("combinator.getOperator()" , combinator.getOperator(), is( CSSModelTreeType.COMBINATOR ) );
		assertThat("combinator.getCombinatorType()" , combinator.getCombinatorType(), is(CombinatorType.DESCENDANT) );
		assertThat("combinator.getSelector()" , combinator.getSelector(), not( (ICSSSelector) null) );
		
		ICSSSelector selector1 = combinator.getSelector();
		assertThat("selector1.getElementName()" , selector1.getElementName(), is( "HBox" ) );
		assertThat("selector1.getNamespacePrefix()" , selector1.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector1.getCombinator()" , selector1.getCombinator(), not( (ICSSCombinator) null) );
		assertThat("selector1.getCombinator().getSelector()" , selector1.getCombinator().getSelector(), not( (ICSSSelector) null) );
		

		ICSSSelector selector2 = selector1.getCombinator().getSelector();
		assertThat("selector2.getElementName()" , selector2.getElementName(), is( "VBox" ) );
		assertThat("selector2.getNamespacePrefix()" , selector2.getNamespacePrefix(), is( "custom" ) );
		assertThat("selector2.getCombinator()" , selector2.getCombinator(), is( (ICSSCombinator) null) );
		

	}
	

}
