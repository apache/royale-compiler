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

package org.apache.flex.compiler.internal.codegen.externals.utils;

import org.apache.flex.compiler.internal.codegen.externals.reference.BaseReference;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;

public class FunctionUtils
{
    public static String toReturnString(BaseReference reference,
            JSDocInfo comment)
    {
        final StringBuilder sb = new StringBuilder();

        String returnType = null;

        if (hasTemplate(reference))
        {
            returnType = "Object";
        }
        else
        {
            returnType = JSTypeUtils.toReturnTypeString(reference);
            //            if (returnType.indexOf("|") != -1 || returnType.indexOf('?') != -1)
            //                returnType = "*";
            //
            //            if (returnType.indexOf("|") != -1)
            //                returnType = "Object /* TODO " + returnType + "*/";
        }

        sb.append(returnType);

        return sb.toString();
    }

    public static String toPrameterString(BaseReference reference,
            JSDocInfo comment, Node paramNode)
    {
        final StringBuilder sb = new StringBuilder();

        sb.append("(");

        if (paramNode != null)
        {
            int index = 0;
            int len = comment.getParameterCount();
            if (len == 0)
            {
                // Missing JSDocInf @param tags, so instead of using the @param tags
                // we use the actual Node list from the AST
                len = paramNode.getChildCount();
                if (len > 0)
                {
                    for (Node param : paramNode.children())
                    {
                        sb.append(param.getString() + ":Object");
                        if (index < len - 1)
                            sb.append(", ");
                        index++;
                    }
                }
            }
            else
            {
                for (String paramName : comment.getParameterNames())
                {
                    sb.append(toParameter(reference, comment, paramName,
                            comment.getParameterType(paramName)));

                    if (index < len - 1)
                        sb.append(", ");

                    index++;
                }
            }
        }

        sb.append(")");

        return sb.toString();
    }

    private static String toParameter(BaseReference reference,
            JSDocInfo comment, String paramName, JSTypeExpression parameterType)
    {
        final StringBuilder sb = new StringBuilder();

        String paramType = null;

        if (parameterType.isVarArgs())
        {
            sb.append("..." + paramName);
        }
        else
        {
            if (hasTemplate(reference))
            {
                paramType = "Object";
            }
            else
            {
                paramType = JSTypeUtils.toParamTypeString(reference, paramName);
            }

            sb.append(paramName);
            sb.append(":");
            sb.append(paramType);

            if (parameterType.isOptionalArg())
            {
                sb.append(" = ");
                sb.append(toDefaultParameterValue(paramType));
            }
        }

        return sb.toString();
    }

    private static String toDefaultParameterValue(String paramType)
    {
        if (paramType.equals("Function"))
            return "null";
        else if (paramType.equals("Number"))
            return "0";
        else if (paramType.equals("String"))
            return "''";
        else if (paramType.equals("Boolean"))
            return "false";
        return "null";
    }

    private static boolean hasTemplate(BaseReference reference)
    {
        return reference.getComment().getTemplateTypeNames().size() > 0;
    }

}
