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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.problems.EmbedTypeNotEmbeddableProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLUnrecognizedCompilerDirectiveProblem;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;

/**
 * Utility class that handles parsing MXML compiler directives (@ functions) and
 * constructing corresponding MXMLInstanceNode's for all supported directives.
 */
class MXMLCompilerDirectiveParser
{
    /**
     * Parse a string containing potentially containing a compiler directive and
     * return a corresponding MXMLInstanceNode if a supported directive is
     * contained within the string
     * 
     * @param builder The object helping to build the MXML AST.
     * @param parent The parent node.
     * @param location The source location of the compiler directive.
     * @param text The text of the compiler directive.
     * @param type The class or interface associated with the compiler directive.
     * @return MXMLInstanceNode or null if no at function contained within text
     */
    public static MXMLInstanceNode parse(MXMLTreeBuilder builder, IMXMLNode parent,
                                         ISourceLocation location, String text, ITypeDefinition type)
    {
        String atFunctionName = getAtFunctionName(text);
        if (atFunctionName == null)
            return null;

        String trimmedText = builder.getMXMLDialect().trim(text);
        return parseAtFunction(builder, parent, location, atFunctionName, trimmedText, type);
    }

    private static String getAtFunctionName(String value)
    {
        value = value.trim();

        if (value.length() > 1 && value.charAt(0) == '@')
        {
            int openParen = value.indexOf('(');

            // A function must have an open paren and a close paren after the open paren.
            if (openParen > 1 && value.indexOf(')') > openParen)
            {
                return value.substring(1, openParen);
            }
        }

        return null;
    }

    private static MXMLCompilerDirectiveNodeBase parseAtFunction(
            MXMLTreeBuilder builder, IMXMLNode parent, ISourceLocation location,
            String functionName, String text, ITypeDefinition type)
    {
        MXMLCompilerDirectiveNodeBase result = null;
        if (IMetaAttributeConstants.ATTRIBUTE_EMBED.equals(functionName))
        {
            RoyaleProject project = builder.getProject();
            ITypeDefinition stringType = (ITypeDefinition)project.getBuiltinType(BuiltinType.STRING);
            ITypeDefinition classType = (ITypeDefinition)project.getBuiltinType(BuiltinType.CLASS);
            ITypeDefinition anyType = (ITypeDefinition)project.getBuiltinType(BuiltinType.ANY_TYPE);

            // @Embed requires that lvalue accept String, Class.
            if (stringType.isInstanceOf(type, project) || classType.isInstanceOf(type, project) ||
                type == anyType)
            {
                result = new MXMLEmbedNode((MXMLNodeBase)parent);
                result.initializeFromText(builder, text, location);
            }
            else
            {
                ICompilerProblem problem = new EmbedTypeNotEmbeddableProblem(location, type.getBaseName());
                builder.addProblem(problem);
            }
        }
        else if (IMetaAttributeConstants.ATTRIBUTE_RESOURCE.equals(functionName))
        {
            result = new MXMLResourceNode((NodeBase)parent, type);
            result.initializeFromText(builder, text, location);
        }
        //        else if ("ContextRoot".equals(functionName))
        //        {
        //        }
        //        else if ("Clear".equals(functionName))
        //        {
        //        }
        else
        {
            builder.addProblem(new MXMLUnrecognizedCompilerDirectiveProblem(location, functionName));
        }

        if (result != null)
            result.setSourceLocation(location);

        return result;
    }
}
