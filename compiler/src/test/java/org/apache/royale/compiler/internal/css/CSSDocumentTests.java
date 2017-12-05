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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.royale.compiler.css.ICSSNamespaceDefinition;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSDocument}.
 * 
 * @author Gordon Smith
 */
public class CSSDocumentTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";
	
	@Test
	public void CSSDocumentTests_namespaces()
	{
		String code = 	     
				" @namespace s \"library://ns.adobe.com/flex/spark\";" + EOL +
				" @namespace mx \"library://ns.adobe.com/flex/mx\";";
		
		CSSDocument cssDoc = (CSSDocument) getCSSNodeBase(code);
		assertThat("cssDoc" , cssDoc, not( (CSSDocument) null ) );	
		assertThat("cssDoc" , cssDoc.getOperator(), is( CSSModelTreeType.DOCUMENT ) );
		
		assertThat("cssDoc.getAtNamespaces().size()" , cssDoc.getAtNamespaces().size(), is( 3 ) );
		CSSNamespaceDefinition defaultNamespaceDefinition = (CSSNamespaceDefinition) cssDoc.getDefaultNamespaceDefinition();
		assertNull("defaultNamespaceDefinition is null" , defaultNamespaceDefinition );
		assertThat("cssDoc.getNamespaceDefinition(\"s\")" , cssDoc.getNamespaceDefinition("s"), not( (ICSSNamespaceDefinition) null ) );
		assertThat("cssDoc.getNamespaceDefinition(\"mxm\")" , cssDoc.getNamespaceDefinition("mxm"), is( (ICSSNamespaceDefinition) null ) );

	}
	
	@Test
	public void CSSDocumentTests_defaultnamespaces()
	{
		String code = " @namespace \"library://ns.adobe.com/flex/spark\"; ";
		
		CSSDocument cssDoc = (CSSDocument) getCSSNodeBase(code);
		assertThat("cssDoc" , cssDoc, not( (CSSDocument) null ) );	
		assertThat("cssDoc.getOperator()" , cssDoc.getOperator(), is( CSSModelTreeType.DOCUMENT ) );
		
		assertThat("cssDoc.getAtNamespaces().size()" , cssDoc.getAtNamespaces().size(), is( 2 ) );
		CSSNamespaceDefinition defaultNamespaceDefinition = (CSSNamespaceDefinition) cssDoc.getDefaultNamespaceDefinition();
		assertNotNull("defaultNamespaceDefinition not null" , defaultNamespaceDefinition );
		assertThat("defaultNamespaceDefinition.getPrefix()" , defaultNamespaceDefinition.getPrefix(), is( (String) null ) );
		assertThat("defaultNamespaceDefinition.getURI()" , defaultNamespaceDefinition.getURI(), is( "library://ns.adobe.com/flex/spark" ) );
		
		CSSTypedNode namespaceList = (CSSTypedNode) cssDoc.children.get(0);
		assertThat("namespaceList.getOperator()", namespaceList.getOperator(), is( CSSModelTreeType.NAMESPACE_LIST ) );
		assertThat("namespaceList.children.size()", namespaceList.children.size(), is( 2 ) );
		CSSTypedNode fontFaceList = (CSSTypedNode) cssDoc.children.get(1);
		assertThat("fontFaceList.getOperator()", fontFaceList.getOperator(), is( CSSModelTreeType.FONT_FACE_LIST ) );
		assertThat("fontFaceList.children.size()", fontFaceList.children.size(), is( 0 ) );
		CSSTypedNode ruleList = (CSSTypedNode) cssDoc.children.get(2);
		assertThat("ruleList.getOperator()", ruleList.getOperator(), is( CSSModelTreeType.RULE_LIST ) );
		assertThat("ruleList.children.size()", ruleList.children.size(), is( 0 ) );
	}
	
	@Test
	public void CSSDocumentTests_fontfaces()
	{
		String code = 	     
				"@font-face { " + EOL +
				" 	src: url('font.ttf'); " + EOL +
				"	fontFamily: 'font'; " + EOL +
				"}" + EOL +
				"@font-face { " + EOL +
				" 	src: url('font1.ttf'); " + EOL +
				"	fontFamily: 'font1'; " + EOL +
				"}";
		
		CSSDocument cssDoc = (CSSDocument) getCSSNodeBase(code);
		assertThat("cssDoc" , cssDoc, not( (CSSDocument) null ) );	
		assertThat("cssDoc.getOperator()" , cssDoc.getOperator(), is( CSSModelTreeType.DOCUMENT ) );
		assertThat("cssDoc.getFontFaces().size())" , cssDoc.getFontFaces().size(), is( 2 ) );

		CSSTypedNode namespaceList = (CSSTypedNode) cssDoc.children.get(0);
		assertThat("namespaceList.getOperator()", namespaceList.getOperator(), is( CSSModelTreeType.NAMESPACE_LIST ) );
		assertThat("namespaceList.children.size()", namespaceList.children.size(), is( 1 ) );
		CSSTypedNode fontFaceList = (CSSTypedNode) cssDoc.children.get(1);
		assertThat("fontFaceList.getOperator()", fontFaceList.getOperator(), is( CSSModelTreeType.FONT_FACE_LIST ) );
		assertThat("fontFaceList.children.size()", fontFaceList.children.size(), is( 2 ) );
		CSSTypedNode ruleList = (CSSTypedNode) cssDoc.children.get(2);
		assertThat("ruleList.getOperator()", ruleList.getOperator(), is( CSSModelTreeType.RULE_LIST ) );
		assertThat("ruleList.children.size()", ruleList.children.size(), is( 0 ) );
	}
	
	@Test
	public void CSSDocumentTests_rules()
	{
		String code = 	     
				"custom|Label { " + EOL +
				"	fontFamily: 'font'; " + EOL +
				"}" + EOL +
				"custom|Label.test { " + EOL +
				"	fontFamily: 'font1'; " + EOL +
				"}";
		
		CSSDocument cssDoc = (CSSDocument) getCSSNodeBase(code);
		assertThat("cssDoc" , cssDoc, not( (CSSDocument) null ) );	
		assertThat("cssDoc.getOperator()" , cssDoc.getOperator(), is( CSSModelTreeType.DOCUMENT ) );
		assertThat("cssDoc.getRules().size())" , cssDoc.getRules().size(), is( 2 ) );
		
		CSSTypedNode namespaceList = (CSSTypedNode) cssDoc.children.get(0);
		assertThat("namespaceList.getOperator()", namespaceList.getOperator(), is( CSSModelTreeType.NAMESPACE_LIST ) );
		assertThat("namespaceList.children.size()", namespaceList.children.size(), is( 1 ) );
		CSSTypedNode fontFaceList = (CSSTypedNode) cssDoc.children.get(1);
		assertThat("fontFaceList.getOperator()", fontFaceList.getOperator(), is( CSSModelTreeType.FONT_FACE_LIST ) );
		assertThat("fontFaceList.children.size()", fontFaceList.children.size(), is( 0 ) );
		CSSTypedNode ruleList = (CSSTypedNode) cssDoc.children.get(2);
		assertThat("ruleList.getOperator()", ruleList.getOperator(), is( CSSModelTreeType.RULE_LIST ) );
		assertThat("ruleList.children.size()", ruleList.children.size(), is( 2 ) );

	}
	
	@Test
	public void CSSDocumentTests_combination_namespace_fontface_rules()
	{
		String code =
				" " + EOL +
				"@font-face { " + EOL +
				" 	src: url('font.ttf'); " + EOL +
				"	fontFamily: 'font'; " + EOL +
				"}" + EOL +
				"@font-face { " + EOL +
				" 	src: url('font1.ttf'); " + EOL +
				"	fontFamily: 'font1'; " + EOL +
				"}" + EOL +
				" " + EOL +
				"custom|Label { " + EOL +
				"	fontFamily: 'font'; " + EOL +
				"}" + EOL +
				"custom|Label.test { " + EOL +
				"	fontFamily: 'font1'; " + EOL +
				"}";
		
		CSSDocument cssDoc = (CSSDocument) getCSSNodeBase(code);
		assertThat("cssDoc" , cssDoc, not( (CSSDocument) null ) );	
		assertThat("cssDoc.getOperator()" , cssDoc.getOperator(), is( CSSModelTreeType.DOCUMENT ) );
		
		assertThat("cssDoc.getAtNamespaces().size()" , cssDoc.getAtNamespaces().size(), is( 1 ) );
		CSSNamespaceDefinition defaultNamespaceDefinition = (CSSNamespaceDefinition) cssDoc.getDefaultNamespaceDefinition();
		assertNull("defaultNamespaceDefinition not null" , defaultNamespaceDefinition );
		
		assertThat("cssDoc.getRules().size())" , cssDoc.getRules().size(), is( 2 ) );
		assertThat("cssDoc.getFontFaces().size())" , cssDoc.getFontFaces().size(), is( 2 ) );
		
		CSSTypedNode namespaceList = (CSSTypedNode) cssDoc.children.get(0);
		assertThat("namespaceList.getOperator()", namespaceList.getOperator(), is( CSSModelTreeType.NAMESPACE_LIST ) );
		assertThat("namespaceList.children.size()", namespaceList.children.size(), is( 1 ) );
		CSSTypedNode fontFaceList = (CSSTypedNode) cssDoc.children.get(1);
		assertThat("fontFaceList.getOperator()", fontFaceList.getOperator(), is( CSSModelTreeType.FONT_FACE_LIST ) );
		assertThat("fontFaceList.children.size()", fontFaceList.children.size(), is( 2 ) );
		CSSTypedNode ruleList = (CSSTypedNode) cssDoc.children.get(2);
		assertThat("ruleList.getOperator()", ruleList.getOperator(), is( CSSModelTreeType.RULE_LIST ) );
		assertThat("ruleList.children.size()", ruleList.children.size(), is( 2 ) );
	}
	

}
