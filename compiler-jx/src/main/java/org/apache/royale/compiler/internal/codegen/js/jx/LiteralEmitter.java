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

package org.apache.royale.compiler.internal.codegen.js.jx;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.royale.compiler.internal.tree.as.XMLLiteralNode;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.utils.JSXUtil;

public class LiteralEmitter extends JSSubEmitter implements
        ISubEmitter<ILiteralNode>
{
    private final List<String> JSX_ATTRIBUTES_NO_RENAME = Arrays.asList("ref", "key");

    public LiteralEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ILiteralNode node)
    {
        boolean isWritten = false;

        String newlineReplacement = "\\\\n";
        String s = node.getValue(true);
        if (!(node instanceof RegExpLiteralNode))
        {
            boolean withEscapedSingleQuote = false;
            if (node.getLiteralType() == LiteralType.XML)
            {
                boolean jsx = false;
                IFunctionNode functionNode = (IFunctionNode) node
                        .getAncestorOfType(IFunctionNode.class);
                if (functionNode != null)
                {
                    IMetaInfo[] metaInfos = functionNode.getMetaInfos();
                    for (IMetaInfo metaInfo : metaInfos)
                    {
                        if (metaInfo.getTagName().equals(JSRoyaleEmitterTokens.JSX.getToken()))
                        {
                            jsx = true;
                            break;
                        }
                    }
                }
                if (node instanceof XMLLiteralNode)
                {
                    XMLLiteralNode xmlNode = (XMLLiteralNode) node;
	                if (jsx)
	                {
	                    emitJSX(xmlNode);
	                    return;
	                }
	                else
	                {
	                    newlineReplacement = "\\\\\n";
	                    if (xmlNode.getContentsNode().getChildCount() == 1)
	                    {
	                        if (s.contains("'"))
	                            s = "\"" + s + "\"";
	                        else
	                            s = "'" + s.trim() + "'";
	                    }
	                    else
	                    {
	                        StringBuilder sb = new StringBuilder();
	                        // probably contains {initializers}
	                        boolean inAttribute = false;
	                        int n = xmlNode.getContentsNode().getChildCount();
	                        for (int i = 0; i < n; i++)
	                        {
	                            if (i > 0)
	                                sb.append(" + ");
	                            IASNode child = xmlNode.getContentsNode().getChild(i);
	                            if (child instanceof LiteralNode)
	                            {
	                                s = ((LiteralNode)child).getValue(true);
	                                s = s.replace("\n", "");
                                    if (s.contains("'")) {
                                        s = s.replace("'","__ESC_SNGLE_QUOT_PLACEHOLDER__");
                                        withEscapedSingleQuote = true;
                                    }

                                    sb.append("'" + s + "'");
	                            }
	                            else
	                            {
	                                s = getEmitter().stringifyNode(child);
	                                if (inAttribute)
	                                {
	                                    sb.append("'\"' + ");
	
	                                    sb.append(s);
	
	                                    sb.append(" + '\"'");
	                                }
	                                else
	                                    sb.append(s);
	                            }
	                            inAttribute = s.endsWith("=");
	                        }
	                        s = sb.toString();
	                    }
	                    char c = s.charAt(0);
	                    if (c == '"')
	                    {
	                        s = s.substring(1, s.length() - 1).trim();
	                        s = s.replace("\"", "__QUOTE_PLACEHOLDER__");
	                        s = "\"" + s + "\"";
	                    }
	                    // use formatQualifiedName to get XML in the usedNames dependencies
                        if (getModel().defaultXMLNamespaceActive &&
                                xmlNode.getContainingScope().getScope() instanceof FunctionScope &&
                                getModel().getDefaultXMLNamespace((FunctionScope)(xmlNode.getContainingScope().getScope())) != null) {
                                    s = getEmitter().formatQualifiedName("XML")
                                    + ASEmitterTokens.MEMBER_ACCESS.getToken()
                                    + "constructWithDefaultXmlNS"
                                    + "("
                                    + s
                                    //we need to append the default ns arg:
                                    +","
                                    + getEmitter().stringifyNode(getModel().getDefaultXMLNamespace((FunctionScope)(xmlNode.getContainingScope().getScope())))
                                    +")";
                            
                        }
	                    else {
		                    newlineReplacement = "\\\\n";
	                        s = s.replaceAll("\r", "");
	                    	s = "new " + getEmitter().formatQualifiedName("XML") + "( " + s + ")";
	                    }
	                }
                }
                else
                {
                	s = node.getValue();
                    if (s.contains("'"))
                        s = "\"" + s + "\"";
                    else
                        s = "'" + s + "'";
                    s = "new " + getEmitter().formatQualifiedName("XML") + "( " + s + ")";
                }
            }
            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
            //s = "\'" + s.replaceAll("\'", "\\\\\'") + "\'";
            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
            if (withEscapedSingleQuote) s = s.replaceAll("__ESC_SNGLE_QUOT_PLACEHOLDER__", "\\\\'");
            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", newlineReplacement);
            if (node.getLiteralType() == LiteralType.STRING && s.length() > 0)
            {
                char c = s.charAt(0);
                if (c == '"')
                {
                    s = s.substring(1, s.length() - 1);
                    s = s.replace("\"", "\\\"");
                    s = "\"" + s + "\"";
                }
                else if (c == '\'')
                {
                    s = s.substring(1, s.length() - 1);
                    s = s.replace("'", "\\'");
                    s = "'" + s + "'";
                }
                s = s.replace("\u00A0", "\\u00A0");
                s = s.replace("\u2028", "\\u2028");
                s = s.replace("\u2029", "\\u2029");
                s = s.replace("\u3000", "\\u3000");
            }

        }
        else
        {
            s = s.replaceAll("\\\\\\\\", "__ESCAPE__ESCAPE__PLACEHOLDER__");
            s = s.replaceAll("\\\\/", "__SLASH_PLACEHOLDER__");
            s = s.replaceAll("\\\\([^u])", "__ESCAPE_PLACEHOLDER__$1");
            s = s.replaceAll("\"", "__DOUBLE_QUOTE_PLACEHOLDER__");
            //s = s.replaceAll("\\\\s", "__S_PLACEHOLDER__");
            //s = s.replaceAll("\\\\w", "__W_PLACEHOLDER__");
            //s = s.replaceAll("\\\\\\.", "__DOT_PLACEHOLDER__");
            s = StringEscapeUtils.escapeJava(s);
            //s = s.replaceAll("__S_PLACEHOLDER__", "\\\\s");
            //s = s.replaceAll("__W_PLACEHOLDER__", "\\\\w");
            //s = s.replaceAll("__DOT_PLACEHOLDER__", "\\\\.");
            s = s.replaceAll("__DOUBLE_QUOTE_PLACEHOLDER__", "\"");
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\");
            s = s.replaceAll("__SLASH_PLACEHOLDER__", "\\\\/");
            s = s.replaceAll("__ESCAPE__ESCAPE__PLACEHOLDER__", "\\\\\\\\");
            s = s.replaceAll("\\\\n", "\\\\u000a");
            s = s.replaceAll("\\\\r", "\\\\u000d");
            s = s.replaceAll("\\\\t", "\\\\u0009");        	
            s = s.replaceAll(" ", "\\\\u0020");
        }

        if (!isWritten)
        {
            startMapping(node);
            write(s);
            endMapping(node);
        }
    }

    private void emitJSX(XMLLiteralNode node)
    {
        RoyaleJSProject project = (RoyaleJSProject) getProject();
        String jsxFactory = project.config.getJsxFactory();
        int childCount = node.getContentsNode().getChildCount();
        Stack<String> elementStack = new Stack<String>();
        String elementName = null;
        boolean endsWithAttribute = false;
        boolean afterOpenTag = false;
        for (int i = 0; i < childCount; i++)
        {
            IASNode child = node.getContentsNode().getChild(i);
            if (child instanceof ILiteralNode)
            {
                ILiteralNode literalChild = (ILiteralNode) child;
                if (literalChild.getLiteralType() != LiteralType.XML)
                {
                    //inside {} syntax. emit normally.
                    if (afterOpenTag)
                    {
                        writeToken(ASEmitterTokens.COMMA);
                    }
                    getEmitter().getWalker().walk(literalChild);
                    continue;
                }
                String value = literalChild.getValue(true);
                while (value.length() > 0)
                {
                    int nextTagStartIndex = value.indexOf("<");
                    int nextTagEndIndex = value.indexOf(">");
                    boolean selfClosing = false;
                    boolean startsWithAttribute = false;
                    if (nextTagEndIndex > 0
                            && value.charAt(nextTagEndIndex - 1) == '/')
                    {
                        selfClosing = true;
                    }
                    if (endsWithAttribute)
                    {
                        //we'll fall back into attribute parsing below
                        endsWithAttribute = false;
                        startsWithAttribute = true;
                        elementName = elementStack.peek();
                    }
                    else if (nextTagStartIndex == 0)
                    {
                        //assume that the name ends at the end of the open tag
                        int endNameIndex = nextTagEndIndex;
                        if (endNameIndex == -1)
                        {
                            //literal ends with an attribute that uses {} syntax
                            endNameIndex = value.length() - 1;
                        }
                        int attributeIndex = -1;
                        for (int j = 0, count = value.length(); j < count; j++)
                        {
                            int charAt = value.charAt(j);
                            if (charAt == ' ' || charAt == '\t'
                                    || charAt == '\r' || charAt == '\n')
                            {
                                attributeIndex = j;
                                break;
                            }
                        }
                        if (attributeIndex > 0 && attributeIndex < endNameIndex)
                        {
                            //if there are attributes, the name does not end at
                            //the end of the open tag
                            endNameIndex = attributeIndex;
                        }
                        elementName = value.substring(1, endNameIndex);
                        if (elementName.endsWith("/"))
                        {
                            elementName = elementName.substring(0, elementName.length() - 1);
                        }
                        if (elementName.startsWith("/"))
                        {
                            //the close tag of the current element
                            elementName = elementName.substring(1);
                            elementName = getElementNameToEmit(elementName, node);
                            if (elementStack.size() > 0)
                            {
                                indentPop();
                            }
                            write(ASEmitterTokens.PAREN_CLOSE);
                            String topOfStack = elementStack.pop();
                            assert topOfStack.equals(elementName);
                            value = value.substring(nextTagEndIndex + 1);
                            continue;
                        }
                        else
                        {
                            //the open tag of a new element
                            if (elementStack.size() > 0)
                            {
                                indentPush();
                                writeNewline(ASEmitterTokens.COMMA);
                            }
                            elementName = getElementNameToEmit(elementName, node);
                            elementStack.push(elementName);
                            write(jsxFactory);
                            write(ASEmitterTokens.PAREN_OPEN);
                            write(elementName);
                            value = value.substring(endNameIndex);
                            value = value.replaceAll("^\\s+", "");
                            //we changed the string, so find it again
                            nextTagEndIndex = value.indexOf(">");
                            afterOpenTag = false;
                        }
                    }
                    else
                    {
                        //we're inside an element's open and closing tags
                        if (nextTagStartIndex == -1)
                        {
                            //literal ends with an attribute that uses {} syntax
                            nextTagStartIndex = value.length();
                        }
                        String elementText = value.substring(0, nextTagStartIndex);
                        int oldLength = elementText.length();
                        //remove carriage return completely
                        elementText = elementText.replaceAll("\r", "");
                        //remove whitespace after new line
                        elementText = elementText.replaceAll("\n\\s+", "\n");
                        //remove whitespace before new line
                        elementText = elementText.replaceAll("\\s+\n", "\n");
                        //remove new lines
                        elementText = elementText.replaceAll("\n", "");
                        //if we've removed new lines, do not end with whitespace
                        //unless it's right before the start of {}
                        if (oldLength != elementText.length()
                                && nextTagStartIndex != value.length())
                        {
                            elementText = elementText.replaceAll("\\s+$", "");
                        }
                        //if there's nothing but whitespace between tags, ignore
                        //it, unless it's right before the start of {}
                        if (!elementText.matches("^\\s*$")
                                || nextTagStartIndex == value.length())
                        {
                            writeToken(ASEmitterTokens.COMMA);
                            emitJSXText(elementText);
                        }
                        value = value.substring(nextTagStartIndex);
                        continue;
                    }
                    //parse the tag's attributes
                    if (nextTagEndIndex == -1)
                    {
                        //literal ends with an attribute that uses {} syntax
                        endsWithAttribute = true;
                        nextTagEndIndex = value.length() - 1;
                    }
                    int attributesEndIndex = (selfClosing && nextTagEndIndex > 0) ? nextTagEndIndex - 1 : nextTagEndIndex;
                    String attributes = value.substring(0, attributesEndIndex);
                    emitJSXAttributes(attributes, startsWithAttribute, endsWithAttribute, elementStack.peek().startsWith("'"));
                    if (selfClosing)
                    {
                        //end of open tag, including attributes
                        write(ASEmitterTokens.PAREN_CLOSE);
                        if (elementStack.size() > 0)
                        {
                            indentPop();
                        }
                        String topOfStack = elementStack.pop();
                        assert topOfStack.equals(elementName);
                    }
                    value = value.substring(nextTagEndIndex + 1);
                    if (!endsWithAttribute)
                    {
                        //don't trim here because we want to preserve whitespace
                        //inside the open and close tags
                        afterOpenTag = true;
                    }
                }
            }
            else
            {
                if (!endsWithAttribute || afterOpenTag)
                {
                    writeToken(ASEmitterTokens.COMMA);
                }
                //not a literal, and inside {} syntax. emit normally.
                getEmitter().getWalker().walk(child);
            }
        }
    }

    private void emitJSXAttributes(String value, boolean startsWithAttribute, boolean endsWithAttribute, boolean isHTML)
    {
        int attributeCount = 0;
        while (true)
        {
            int charCount = value.length();
            if (charCount == 0)
            {
                break;
            }
            int endAttributeNameIndex = value.indexOf("=");
            if (endAttributeNameIndex == -1)
            {
                endAttributeNameIndex = value.length();
            }
            String attributeName = value.substring(0, endAttributeNameIndex);
            writeToken(ASEmitterTokens.COMMA);
            if (!startsWithAttribute && attributeCount == 0)
            {
                writeToken(ASEmitterTokens.BLOCK_OPEN);
            }
            attributeName = attributeName.trim();
            //if it's HTML, we don't want anything to be renamed by Closure
            //compiler. if the attribute is a special built-in value, like "ref"
            //or "key", we never want it to be renamed.
            if (isHTML || JSX_ATTRIBUTES_NO_RENAME.contains(attributeName))
            {
                emitJSXText(attributeName);
            }
            else
            {
                write(attributeName);
            }
            writeToken(ASEmitterTokens.COLON);
            attributeCount++;
            if ((endAttributeNameIndex + 1) >= charCount)
            {
                //literal ends with an attribute that uses {} syntax
                break;
            }
            int quoteChar = value.charAt(endAttributeNameIndex + 1);
            int startAttributeValueIndex = endAttributeNameIndex + 2;
            if (startAttributeValueIndex > charCount)
            {
                startAttributeValueIndex = charCount;
            }
            int endAttributeValueIndex = value.indexOf(quoteChar, startAttributeValueIndex);
            if (endAttributeValueIndex == -1 || endAttributeValueIndex > charCount)
            {
                endAttributeValueIndex = charCount;
            }
            if (endAttributeValueIndex > 0)
            {
                String attributeValue = value.substring(startAttributeValueIndex, endAttributeValueIndex);
                emitJSXText(attributeValue);
                value = value.substring(endAttributeValueIndex + 1);
                value = value.replaceAll("^\\s+", "");
            }
        }
        if (!endsWithAttribute)
        {
            if (!startsWithAttribute && attributeCount == 0)
            {
                writeToken(ASEmitterTokens.COMMA);
                write(ASEmitterTokens.NULL);
            }
            else
            {
                write(ASEmitterTokens.SPACE);
                write(ASEmitterTokens.BLOCK_CLOSE);
            }
        }
    }

    private void emitJSXText(String elementText)
    {
        write(ASEmitterTokens.SINGLE_QUOTE);
        elementText = elementText.replaceAll("'", "\\\\\'");
        write(elementText);
        write(ASEmitterTokens.SINGLE_QUOTE);
    }

    private String getElementNameToEmit(String elementName, IASNode node)
    {
        String qualifiedTypeName = JSXUtil.getQualifiedTypeForElementName(elementName, node, getProject());
        if (qualifiedTypeName != null)
        {
            return getEmitter().formatQualifiedName(qualifiedTypeName);
        }
        //it's a basic HTML tag
        return ASEmitterTokens.SINGLE_QUOTE.getToken() + elementName + ASEmitterTokens.SINGLE_QUOTE.getToken();
    }
}
