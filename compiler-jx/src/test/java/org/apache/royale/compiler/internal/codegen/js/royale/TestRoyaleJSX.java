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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IFunctionNode;

import org.junit.Test;

public class TestRoyaleJSX extends ASTestBase
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
        project.config = new JSGoogConfiguration();
        super.setUp();
    }

    @Test
    public void testJSXMetadataWithoutXMLLiterals()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n}");
    }

    @Test
    public void testSimpleSelfClosingHTMLTag()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null);\n}");
    }

    @Test
    public void testSimpleSelfClosingHTMLTagWithTrailingSpace()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div />}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null);\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttributeAfterNewLine()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div\nid=\"foo\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttributeAfterCarriageReturnNewLine()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div\r\nid=\"foo\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttributeAfterTab()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div\tid=\"foo\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithMultipleAttributes()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\" className=\"bar\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo', 'className': 'bar' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSingleQuoteAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\'foo\'/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSingleQuoteInAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"'\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': '\\\'' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithDashInAttributeName()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div data-prop=\"foo\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'data-prop': 'foo' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSpaceInAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div className=\"foo bar\"/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'className': 'foo bar' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithSpaceInSingleQuoteAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div className=\'foo bar\'/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'className': 'foo bar' });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttributeContainingLiteral()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id={2}/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 2 });\n}");
    }

    @Test
    public void testSelfClosingHTMLTagWithAttributeContainingExpression()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id={2 + 2}/>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 2 + 2 });\n}");
    }

    @Test
    public void testSimpleOpenAndCloseHTMLTag()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>Foo</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextOnNewLine()
    {
        //in JSX, new lines are removed
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\nFoo\n</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextWithMultipleSpacesBetween()
    {
        //in JSX, spaces are only removed after a new line, so these are kept!
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>Foo   bar</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo   bar');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextWithMultipleSpacesBeforeAndAfter()
    {
        //in JSX, spaces are only removed after a new line, so these are kept!
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>   Foo   </div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, '   Foo   ');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextOnNewLineCRLF()
    {
        //in JSX, new lines are removed
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\r\nFoo\r\n</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextOnNewLineWithTabIndent()
    {
        //in JSX, whitespace is removed after a new line, so the tab isn't kept
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\n\tFoo\n</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextOnNewLineWithSpaceIndent()
    {
        //in JSX, whitespace is removed after a new line, so the spaces aren't kept
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\n    Foo\n</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildBracesContainingLiteral()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>{2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 2);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildBracesContainingExpression()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>{2 + 2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 2 + 2);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildTextAndBraces()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>Foo {2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 'Foo ', 2);\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithChildBracesAndText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>{2} Foo</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null, 2, ' Foo');\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\"></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo' });\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithMultipleAttributes()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\" className=\"bar\"></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo', 'className': 'bar' });\n}");
    }

    @Test
    public void testOpenAndCloseHTMLTagWithAttributeAndChildText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div id=\"foo\">Foo</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', { 'id': 'foo' }, 'Foo');\n}");
    }

    @Test
    public void testNestedHTMLTags()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div><button/></div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null));\n}");
    }

    @Test
    public void testNestedHTMLTagsWithWhitespace()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>  \t<button/>   \t   </div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null));\n}");
    }

    @Test
    public void testNestedHTMLTagsWithWhitespaceAndText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\t<button/>   Hello</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null), '   Hello');\n}");
    }

    @Test
    public void testNestedHTMLTagsWithChildBracesWhitespaceAndText()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\t<button/>   Hello   {2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null), '   Hello   ', 2);\n}");
    }

    @Test
    public void testNestedHTMLTagsWithChildBracesWhitespaceAndText2()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\t<button></button>   Hello   {2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null), '   Hello   ', 2);\n}");
    }

    @Test
    public void testNestedHTMLTagsWithChildBracesAndWhitespace()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\t<button/>   {2}</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null), '   ', 2);\n}");
    }

    @Test
    public void testNestedHTMLTagsOnDifferentLines()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\n<button/>\n</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null));\n}");
    }

    @Test
    public void testNestedHTMLTagsOnDifferentLinesWithCRLF()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {return <div>\r\n\t<button/>\r\n</div>}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement('div', null,\n    React.createElement('button', null));\n}");
    }

    @Test
    public void testImportedClass()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {\n  import custom.TestImplementation;\n  return <TestImplementation/>;\n}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement(custom.TestImplementation, null);\n}");
    }

    @Test
    public void tesClassWithAttribute()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {\n  import custom.TestImplementation;\n  return <TestImplementation id=\"hello\"/>;\n}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement(custom.TestImplementation, { id: 'hello' });\n}");
    }

    @Test
    public void tesClassWithRef()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {\n  import custom.TestImplementation;\n  return <TestImplementation ref=\"hello\"/>;\n}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement(custom.TestImplementation, { 'ref': 'hello' });\n}");
    }

    @Test
    public void tesClassWithKey()
    {
        IFunctionNode node = getMethod("[JSX]\nfunction foo() {\n  import custom.TestImplementation;\n  return <TestImplementation key=\"hello\"/>;\n}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  return React.createElement(custom.TestImplementation, { 'key': 'hello' });\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
