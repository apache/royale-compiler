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

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.css.ConditionType;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.css.ICSSSelectorCondition;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSSelectorCondition}.
 * 
 * @author Gordon Smith
 */
public class CSSSelectorConditionTests extends CSSBaseTests {
		
    private String getPostfix()
    {
    	return " { } ";
    }
    
	@Override
	public ICSSDocument getCSSNodeBase(String code) {
		return super.getCSSNodeBase(code + getPostfix());
	}
	
	
	protected List<ICSSSelectorCondition> getCSSSelectorConditions(String code) {
		List<ICSSSelector> selectors =  getCSSSelectors( code );
		assertThat("selectors", selectors,  not( (List<ICSSSelector>) null) );
		List<ICSSSelectorCondition> conditions =  new ArrayList<ICSSSelectorCondition>();

		for (ICSSSelector icssSelector : selectors) {
			conditions.addAll( icssSelector.getConditions() );
		}
		assertThat("conditions", conditions,  not( (List<ICSSSelectorCondition>) null) );
		return conditions;
	}

	
	@Test
	public void CSSSelectorConditionTests_pseudo_condtion()
	{
		String code = " custom|Button:up "; 
		
		List<ICSSSelectorCondition> conditions = getCSSSelectorConditions(code);
		assertThat("conditions.size()" , conditions.size(), is(1) );	
		
		CSSSelectorCondition condition = (CSSSelectorCondition) conditions.get(0);
		assertThat("condition.getOperator()" , condition.getOperator(), is( CSSModelTreeType.SELECTOR_CONDITION ) );
		assertThat("condition.getConditionType()" , condition.getConditionType() , is( ConditionType.PSEUDO ) );
		assertThat("condition.getValue()" , condition.getValue(), is( "up" ) );
	}
	
	@Test
	public void CSSSelectorConditionTests_class_condtion()
	{
		String code = " custom|Button.rounded "; 
		
		List<ICSSSelectorCondition> conditions = getCSSSelectorConditions(code);
		assertThat("conditions.size()" , conditions.size(), is(1) );	
		
		CSSSelectorCondition condition = (CSSSelectorCondition) conditions.get(0);
		assertThat("condition.getOperator()" , condition.getOperator(), is( CSSModelTreeType.SELECTOR_CONDITION ) );
		assertThat("condition.getConditionType()" , condition.getConditionType() , is( ConditionType.CLASS ) );
		assertThat("condition.getValue()" , condition.getValue(), is( "rounded" ) );
	}
	
	@Test
	public void CSSSelectorConditionTests_id_condtion()
	{
		String code = " custom|Button#main "; 
		
		List<ICSSSelectorCondition> conditions = getCSSSelectorConditions(code);
		assertThat("conditions.size()" , conditions.size(), is(1) );	
		
		CSSSelectorCondition condition = (CSSSelectorCondition) conditions.get(0);
		assertThat("condition.getOperator()" , condition.getOperator(), is( CSSModelTreeType.SELECTOR_CONDITION ) );
		assertThat("condition.getConditionType()" , condition.getConditionType() , is( ConditionType.ID ) );
		assertThat("condition.getValue())" , condition.getValue(), is( "main" ) );
	}
	
	@Test
	public void CSSSelectorConditionTests_combined_condtions()
	{
		String code = " custom|Button.rounded#main:up "; 
		
		List<ICSSSelectorCondition> conditions = getCSSSelectorConditions(code);
		assertThat("conditions.size()" , conditions.size(), is(3) );	
		
		CSSSelectorCondition condition1 = (CSSSelectorCondition) conditions.get(0);
		assertThat("condition1.getOperator()" , condition1.getOperator(), is( CSSModelTreeType.SELECTOR_CONDITION ) );
		assertThat("condition1.getConditionType()" , condition1.getConditionType() , is( ConditionType.CLASS ) );
		assertThat("condition1.getValue()" , condition1.getValue(), is( "rounded" ) );
		
		CSSSelectorCondition condition2 = (CSSSelectorCondition) conditions.get(1);
		assertThat("condition2.getOperator()" , condition2.getOperator(), is( CSSModelTreeType.SELECTOR_CONDITION ) );
		assertThat("condition2.getConditionType()" , condition2.getConditionType() , is( ConditionType.ID ) );
		assertThat("condition2.getValue()" , condition2.getValue(), is( "main" ) );
		
		CSSSelectorCondition condition3 = (CSSSelectorCondition) conditions.get(2);
		assertThat("condition3.getOperator()" , condition3.getOperator(), is( CSSModelTreeType.SELECTOR_CONDITION ) );
		assertThat("condition3.getConditionType()" , condition3.getConditionType() , is( ConditionType.PSEUDO ) );
		assertThat("condition3.getValue()" , condition3.getValue(), is( "up" ) );
	}
	

}
