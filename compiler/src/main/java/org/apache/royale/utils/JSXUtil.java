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

package org.apache.royale.utils;

import java.util.List;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.tree.as.XMLLiteralNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * Utilities for parsing XML in functions with [JSX] metadata.
 */
public class JSXUtil
{
    public static boolean hasJSXMetadata(IFunctionNode node)
    {
        for (IMetaInfo metaInfo : node.getMetaInfos())
        {
            if (metaInfo.getTagName().equals("JSX"))
            {
                return true;
            }
        }
        return false;
    }

    public static void findQualifiedNamesInXMLLiteral(XMLLiteralNode node, ICompilerProject project, List<String> qualifiedNames)
    {
        int childCount = node.getContentsNode().getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            IASNode child = node.getContentsNode().getChild(i);
            if (child instanceof ILiteralNode)
            {
                ILiteralNode literalChild = (ILiteralNode) child;
                if (literalChild.getLiteralType() == ILiteralNode.LiteralType.XML)
                {
                    findQualifiedNamesInXMLLiteralChild(literalChild, project, qualifiedNames);
                }
            }
        }
    }

    private static void findQualifiedNamesInXMLLiteralChild(ILiteralNode node, ICompilerProject project, List<String> qualifiedNames)
    {
        String value = node.getValue();
        while (true)
        {
            int index = value.indexOf("<");
            if (index == -1)
            {
                break;
            }
            value = value.substring(index + 1);
            index = value.indexOf(">");
            int nextWhitespaceIndex = -1;
            for (int i = 0, count = value.length(); i < count; i++)
            {
                int charAt = value.charAt(i);
                if (charAt == ' ' || charAt == '\t' || charAt == '\r'
                        || charAt == '\n')
                {
                    nextWhitespaceIndex = i;
                    break;
                }
            }
            if (nextWhitespaceIndex == -1)
            {
                nextWhitespaceIndex = value.length();
            }
            if (index == -1 || index > nextWhitespaceIndex)
            {
                index = nextWhitespaceIndex;
            }
            String elementName = value.substring(0, index);
            if (elementName.endsWith("/"))
            {
                //strip the / from self-closing tags
                elementName = elementName.substring(0, elementName.length() - 1);
            }
            //ignore end tags
            if (!elementName.startsWith("/"))
            {
                String qualifiedElementName = getQualifiedTypeForElementName(elementName, node, project);
                if (qualifiedElementName != null)
                {
                    qualifiedNames.add(qualifiedElementName);
                }
            }
            value = value.substring(index + 1);
        }
    }

    /**
     * Finds the qualified type name for an <element> in JSX. Returns null if
     * the element name is a basic HTML tag.
     */
    public static String getQualifiedTypeForElementName(String elementName, IASNode node, ICompilerProject project)
    {
        String firstChar = elementName.substring(0, 1);
        boolean isHTMLTag = firstChar.toLowerCase().equals(firstChar);
        if (isHTMLTag)
        {
            return null;
        }
        IScopedNode scopedNode = node.getContainingScope();
        IASScope scope = scopedNode.getScope();
        if (scope instanceof ASScope)
        {
            ASScope asScope = (ASScope) scope;
            IDefinition definition = asScope.findProperty(project, elementName, DependencyType.EXPRESSION);
            if (definition != null)
            {
                return definition.getQualifiedName();
            }
        }
        return elementName;
    }
}
