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

import org.apache.royale.compiler.css.ICSSNamespaceDefinition;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSNamespaceDefinition}.
 * 
 * @author Gordon Smith
 */
public class CSSNamespaceDefinitionTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";

	protected List<ICSSNamespaceDefinition> getCSSNamespaceDefinition(String code) {
		return getCSSNodeBase(code).getAtNamespaces();
	}
	
	@Test
	public void CSSNamespaceDefinitionTests_namespace()
	{
		String code = 
				" @namespace s \"library://ns.adobe.com/flex/spark\";";
		
		List<ICSSNamespaceDefinition> namespaces = getCSSNamespaceDefinition(code);
		assertThat("namespaces.size()" , namespaces.size(), is(2) );	
		
		CSSNamespaceDefinition namespace = (CSSNamespaceDefinition) namespaces.get(1);
		assertThat("namespace.getOperator()" , namespace.getOperator(), is( CSSModelTreeType.NAMESPACE_DEFINITION ) );
		assertThat("namespace.getPrefix()" , namespace.getPrefix(), is( "s" ) );
		assertThat("namespace.getURI()" , namespace.getURI(), is( "library://ns.adobe.com/flex/spark" ) );
	}
	
	@Test
	public void CSSNamespaceDefinitionTests_namespace1()
	{
		String code = 
				" @namespace s ;";
		
		errorFilters = new String[1];
		errorFilters[0] = "missing STRING at ';'";
		List<ICSSNamespaceDefinition> namespaces = getCSSNamespaceDefinition(code);
		assertThat("namespaces.size()" , namespaces.size(), is(2) );	
		
		CSSNamespaceDefinition namespace = (CSSNamespaceDefinition) namespaces.get(1);
		assertThat("namespace.getOperator()" , namespace.getOperator(), is( CSSModelTreeType.NAMESPACE_DEFINITION ) );
		assertThat("namespace.getPrefix()" , namespace.getPrefix(), is( "s" ) );
		//TODO why is it "missing STRING"?
		assertThat("namespace.getURI()" , namespace.getURI(), is( "missing STRING" ) );
	}
	
	@Test
	public void CSSNamespaceDefinitionTests_namespace2()
	{
		String code = 
				" @namespace ;";
		
		errorFilters = new String[1];
		errorFilters[0] = "missing STRING at ';'";
		List<ICSSNamespaceDefinition> namespaces = getCSSNamespaceDefinition(code);
		assertThat("namespaces.size()" , namespaces.size(), is(2) );	
		
		CSSNamespaceDefinition namespace = (CSSNamespaceDefinition) namespaces.get(1);
		assertThat("namespace.getOperator()" , namespace.getOperator(), is( CSSModelTreeType.NAMESPACE_DEFINITION ) );
		assertThat("namespace.getPrefix()" , namespace.getPrefix(), is( (String)null ) );
		//TODO why is it "missing STRING"?
		assertThat("namespace.getURI()" , namespace.getURI(), is( "missing STRING" ) );
	}
	
	@Test
	public void CSSNamespaceDefinitionTests_duplicate_namespace()
	{
		String code = 
				" @namespace s \"library://ns.adobe.com/flex/spark\";" + EOL +
				" @namespace s \"library://ns.adobe.com/flex/spark\";";
		
		List<ICSSNamespaceDefinition> namespaces = getCSSNamespaceDefinition(code);
		assertThat("namespaces.size()" , namespaces.size(), is(3) );	
		
		CSSNamespaceDefinition namespace1 = (CSSNamespaceDefinition) namespaces.get(1);
		assertThat("namespace1.getOperator()" , namespace1.getOperator(), is( CSSModelTreeType.NAMESPACE_DEFINITION ) );
		assertThat("namespace1.getPrefix()" , namespace1.getPrefix(), is( "s" ) );
		assertThat("namespace1.getURI()" , namespace1.getURI(), is( "library://ns.adobe.com/flex/spark" ) );
		
		CSSNamespaceDefinition namespace2 = (CSSNamespaceDefinition) namespaces.get(2);
		assertThat("namespace2.getOperator()" , namespace2.getOperator(), is( CSSModelTreeType.NAMESPACE_DEFINITION ) );
		assertThat("namespace2.getPrefix()" , namespace2.getPrefix(), is( "s" ) );
		assertThat("namespace2.getURI()" , namespace2.getURI(), is( "library://ns.adobe.com/flex/spark" ) );
	}
	
	@Test
	public void CSSNamespaceDefinitionTests_two_namespaces()
	{
		String code = 
				" @namespace s \"library://ns.adobe.com/flex/spark\";" + EOL +
				" @namespace mx \"library://ns.adobe.com/flex/mx\";";
		
		List<ICSSNamespaceDefinition> namespaces = getCSSNamespaceDefinition(code);
		assertThat("namespaces.size()" , namespaces.size(), is(3) );	
		
		CSSNamespaceDefinition namespace1 = (CSSNamespaceDefinition) namespaces.get(1);
		assertThat("namespace1.getOperator()" , namespace1.getOperator(), is( CSSModelTreeType.NAMESPACE_DEFINITION ) );
		assertThat("namespace1.getPrefix()" , namespace1.getPrefix(), is( "s" ) );
		assertThat("namespace1.getURI()" , namespace1.getURI(), is( "library://ns.adobe.com/flex/spark" ) );
		
		CSSNamespaceDefinition namespace2 = (CSSNamespaceDefinition) namespaces.get(2);
		assertThat("namespace2.getOperator()" , namespace2.getOperator(), is( CSSModelTreeType.NAMESPACE_DEFINITION ) );
		assertThat("namespace1.getPrefix()" , namespace2.getPrefix(), is( "mx" ) );
		assertThat("namespace1.getURI()" , namespace2.getURI(), is( "library://ns.adobe.com/flex/mx" ) );
	}
	
	

}
