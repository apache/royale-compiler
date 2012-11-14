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

package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.css.ICSSNamespaceDefinition;
import org.apache.flex.compiler.css.ICSSRule;
import org.apache.flex.compiler.internal.caches.CSSDocumentCache;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleNode;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * JUnit tests for {@link MXMLStyleNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLStyleNodeTests extends MXMLNodeBaseTests
{
	private static String PREFIX =
	    "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*'>\n\t";
				
	private static String POSTFIX =
		"\n</d:Sprite>";
	    
	private static String EOL = "\n\t";
	
    @Override
    protected IMXMLFileNode getMXMLFileNode(String code)
    {
    	return super.getMXMLFileNode(PREFIX + code + POSTFIX);
    }
    
	private IMXMLStyleNode getMXMLStyleNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLStyleNode node = (IMXMLStyleNode)findFirstDescendantOfType(fileNode, IMXMLStyleNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLStyleID));
		assertThat("getName", node.getName(), is("Style"));
		return node;
	}
	
	@Test
	public void MXMLStyleNode_empty1()
	{
		String code = "<fx:Style/>";
		IMXMLStyleNode node = getMXMLStyleNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		
		ICSSDocument cssDoc = node.getCSSDocument(null);
		assertThat(cssDoc, is(CSSDocumentCache.EMPTY_CSS_DOCUMENT));
	}
	
	@Test
	public void MXMLStyleNode_empty2()
	{
		String code = "<fx:Style></fx:Style>";
		IMXMLStyleNode node = getMXMLStyleNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		
		ICSSDocument cssDoc = node.getCSSDocument(null);
		assertThat(cssDoc, is(CSSDocumentCache.EMPTY_CSS_DOCUMENT));
	}
	
	@Test
	public void MXMLStyleNode_empty3()
	{
		String code = "<fx:Style/> \t\r\n<fx:Style/>";
		IMXMLStyleNode node = getMXMLStyleNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		
		ICSSDocument cssDoc = node.getCSSDocument(null);
		assertThat(cssDoc, is(CSSDocumentCache.EMPTY_CSS_DOCUMENT));
	}
	
	@Test
	public void MXMLStyleNode_two_rules()
	{
		String code =
			"<fx:Style>" + EOL +
			"    Button { font-size: 20; color: red }" + EOL +
			"    CheckBox { font-size: 16 }" + EOL +
			"</fx:Style>";
		IMXMLStyleNode node = getMXMLStyleNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		ICSSDocument css = node.getCSSDocument(null);
		ImmutableList<ICSSRule> rules = css.getRules();
		assertThat("rules", rules.size(), is(2));
		assertThat("rule 0 name", rules.get(0).getSelectorGroup().get(0).getElementName(), is("Button"));
		assertThat("rule 1 name", rules.get(1).getSelectorGroup().get(0).getElementName(), is("CheckBox"));
	}
	
	@Test
	public void MXMLStyleNode_two_same_namespaces()
	{
		String code =
			"<fx:Style>" + EOL +
			"    @namespace \"library://ns.adobe.com/flex/mx\";" + EOL +
			"    @namespace \"library://ns.adobe.com/flex/mx\";" + EOL +
			"</fx:Style>";
		IMXMLStyleNode node = getMXMLStyleNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		ICSSDocument css = node.getCSSDocument(null);
		ImmutableList<ICSSNamespaceDefinition> namespaces = css.getAtNamespaces();
		assertThat("namespaces", namespaces.size(), is(2));
		assertThat("namespace 0 prefix ", namespaces.get(0).getPrefix(), is((String)null));
		assertThat("namespace 0 uri", namespaces.get(0).getURI(), is("library://ns.adobe.com/flex/mx"));
		assertThat("namespace 1 prefix", namespaces.get(1).getPrefix(), is((String)null));
		assertThat("namespace 1 uri", namespaces.get(1).getURI(), is("library://ns.adobe.com/flex/mx"));
	}
}
