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

package org.apache.flex.compiler.internal.css;

import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.css.ICSSProperty;
import org.apache.flex.compiler.css.ICSSPropertyValue;
import org.apache.flex.compiler.css.ICSSRule;

import com.google.common.collect.ImmutableList;

/**
 * JUnit tests for {@link CSSPropertyValue}.
 * 
 * @author Gordon Smith
 */
public class CSSPropertyValueTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";
	
	protected String getPrefix()
	{
		return "s|Label {" + EOL;
	}
			
    protected String getPostfix()
    {
    	return EOL +
		       "}";
    }

	@Override
	public ICSSDocument getCSSNodeBase(String code) {
		return super.getCSSNodeBase(getPrefix()  + code + getPostfix());
	}

	private List<ICSSProperty> getCSSProperties(String code) {
		ICSSDocument doc = getCSSNodeBase(code);
		
		ImmutableList<ICSSRule> rules = doc.getRules();
		List<ICSSProperty> properties = new ArrayList<ICSSProperty>();
		for (ICSSRule icssRule : rules) {
			properties.addAll( icssRule.getProperties() );
		}
		
		return properties;
	}
	
	protected List<ICSSPropertyValue> getCSSPropertyValues(String code) {
		List<ICSSProperty> properties = getCSSProperties(code);
		
		List<ICSSPropertyValue> propertyValues = new ArrayList<ICSSPropertyValue>();
		
		for (ICSSProperty icssProperty : properties) {
			propertyValues.add( icssProperty.getValue() );
		}
		
		return propertyValues;
	}

}
