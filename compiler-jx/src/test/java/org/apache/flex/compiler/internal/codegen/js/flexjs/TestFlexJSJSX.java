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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.test.ASTestBase;
import org.apache.flex.compiler.tree.as.IFunctionNode;

import org.junit.Test;

public class TestFlexJSJSX extends ASTestBase
{
    @Test
    public void testJSXMetadataWithoutXMLLiterals()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n}");
    }

    @Test
    public void testSimpleSelfClosingHTMLTag()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null);\n}");
    }

    @Test
    public void testSimpleSelfClosingHTMLTagWithTrailingSpace()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div />}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null);\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithMultipleAttributes()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\" className=\"bar\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 'foo', className: 'bar' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSingleQuoteAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\'foo\'/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSingleQuoteInAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"'\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: '\\\'' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithDashInAttributeName()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div data-prop=\"foo\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'data-prop': 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSpaceInAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div className=\"foo bar\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { className: 'foo bar' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSpaceInSingleQuoteAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div className=\'foo bar\'/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { className: 'foo bar' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttributeContainingLiteral()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id={2}/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 2 });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttributeContainingExpression()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id={2 + 2}/>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 2 + 2 });\n}");
    }

    @Test
    public void testSimpleOpenAndCloseHTMLTag()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>Foo</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildBracesContainingLiteral()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>{2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 2);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildBracesContainingExpression()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>{2 + 2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 2 + 2);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextAndBraces()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>Foo {2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo ', 2);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildBracesAndText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>{2} Foo</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 2, ' Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\"></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 'foo' });\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithMultipleAttributes()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\" className=\"bar\"></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 'foo', className: 'bar' });\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithAttributeAndChildText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\">Foo</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', { id: 'foo' }, 'Foo');\n}");
    }

    @Test
    public void testNestedHTMLTags()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div><button/></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null));\n}");
    }

    @Test
    public void testImportedClass()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {\n  import flash.events.EventDispatcher;\n  return <EventDispatcher/>;\n}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  return React.createElement(flash.events.EventDispatcher, null);\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
