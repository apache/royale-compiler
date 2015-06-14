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

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;

public class FunctionUtils
{
    public static String transformReturnString(BaseReference reference,
            JSDocInfo comment)
    {
        StringBuilder sb = new StringBuilder();
        ImmutableList<String> names = comment.getTemplateTypeNames();
        if (names.size() > 0)
        {
            sb.append("Object");
        }
        else
        {
            String type = JSTypeUtils.toReturnTypeString(reference);
            if (type.indexOf("|") != -1 || type.indexOf('?') != -1)
                type = "*";

            if (type.indexOf("|") != -1)
                type = "Object /* TODO " + type + "*/";

            sb.append(type);
            return sb.toString();
        }

        return sb.toString();
    }

    public static String toPrameterString(BaseReference reference,
            JSDocInfo comment, Node paramNode)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");

        if (paramNode != null)
        {
            int index = 0;
            int len = comment.getParameterCount();
            //int childCount = paramNode.getChildCount();
            if (len == 0)
            {
                len = paramNode.getChildCount();
                // Missing JSDocInf @param tags
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
            //            else if (len != childCount)
            //            {
            //                // XXX Match up existing @param tags with parameters
            //                if (childCount > 0)
            //                {
            //                    for (Node param : paramNode.children())
            //                    {
            //                        sb.append(param.getString() + ":Object");
            //                        if (index < childCount - 1)
            //                            sb.append(", ");
            //                        index++;
            //                    }
            //                }
            //            }
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

    public static String toParameter(BaseReference reference,
            JSDocInfo comment, String paramName, JSTypeExpression parameterType)
    {
        StringBuilder sb = new StringBuilder();

        if (parameterType == null)
        {
            sb.append(paramName);
            sb.append(":");
            sb.append("Object /* TODO is this correct? */");
            return sb.toString();
        }

        //JSTypeExpression parameterType = comment.getParameterType(paramName);

        ImmutableList<String> names = comment.getTemplateTypeNames();
        if (names.size() > 0)
        {
            sb.append(paramName);
            sb.append(":");
            sb.append("Object");
        }
        else
        {
            if (parameterType.isVarArgs())
            {
                sb.append("...rest");
            }
            else
            {
                String paramType = JSTypeUtils.toParamTypeString(reference,
                        paramName);

                sb.append(paramName);
                sb.append(":");
                sb.append(paramType);

                if (paramType.indexOf("|") != -1)
                    paramType = "Object /* TODO " + paramType + "*/";

                if (parameterType.isOptionalArg())
                {
                    sb.append(" = null");
                }
            }
        }

        return sb.toString();
    }
}
