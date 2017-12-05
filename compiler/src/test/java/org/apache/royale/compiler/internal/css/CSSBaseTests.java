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

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSProperty;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.internal.tree.mxml.MXMLNodeBaseTests;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleNode;
import org.junit.Ignore;

import com.google.common.collect.ImmutableList;

@Ignore
public class CSSBaseTests extends MXMLNodeBaseTests {
	
	private static final String EOL = "\n\t\t";
	
	private String getPrefix()
	{
		return "<fx:Object xmlns:fx='http://ns.adobe.com/mxml/2009'>\n" +
	           "    <fx:Style>" + EOL + "@namespace custom \"library://ns.apache.org/royale/test\";" + EOL + 
		       "        ";
	}
			
    private String getPostfix()
    {
    	return EOL +
		       "    </fx:Style>" + EOL +
		       "</fx:Object>";
    }
	

	public ICSSDocument getCSSNodeBase(String code) {
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

        IMXMLFileNode fileNode = super.getMXMLFileNode(getPrefix() + code + getPostfix());
		IMXMLStyleNode styleNode = (IMXMLStyleNode) findFirstDescendantOfType(fileNode, IMXMLStyleNode.class);
		
		assertNotNull("styleNode", styleNode );		
					
		return styleNode.getCSSDocument(problems);
	}
	
	protected List<ICSSProperty> getCSSProperties(String code) {
		ICSSDocument doc = getCSSNodeBase(code);
		
		ImmutableList<ICSSRule> rules = doc.getRules();
		List<ICSSProperty> properties = new ArrayList<ICSSProperty>();
		for (ICSSRule icssRule : rules) {
			properties.addAll( icssRule.getProperties() );
		}
		assertThat("properties", properties,  not( (List<ICSSProperty>) null) );

		return properties;
	}
	
	protected List<ICSSSelector> getCSSSelectors(String code) {
		ImmutableList<ICSSRule> rules =  getCSSNodeBase( code ).getRules();
		assertThat("rules", rules,  not( (ImmutableList<ICSSRule>) null) );
		List<ICSSSelector> selectors = new ArrayList<ICSSSelector>();
		for (ICSSRule icssRule : rules) {
			selectors.addAll( icssRule.getSelectorGroup() );
		}
		assertThat("selectors", selectors,  not( (List<ICSSSelector>) null) );
		return selectors;
	}

}
