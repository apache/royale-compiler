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

package org.apache.royale.compiler.internal.codegen.typedefs.utils;

import org.apache.royale.compiler.internal.codegen.typedefs.reference.BaseReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.MemberReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;

import com.google.common.base.Strings;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;

public class FunctionUtils
{
    /**
     * Compute the type of a function or method parameter.
     * 
     * @param reference The FunctionReference or MethodReference the parameter belongs to
     * @param name The name of the parameter
     * @return the type of a function or method parameter
     */
    public static String toParameterType(final BaseReference reference, final String name)
    {

        String parameterType;
        if (FunctionUtils.hasTemplate(reference) && FunctionUtils.containsTemplate(reference, name))
        {
            parameterType = "Object";
        }
        else
        {
            parameterType = JSTypeUtils.toParamTypeString(reference, name);
        }

        return parameterType;
    }

    public static String toReturnString(BaseReference reference)
    {
        final StringBuilder sb = new StringBuilder();

        String returnType;

        if (hasTemplate(reference))
        {
            returnType = JSTypeUtils.toReturnTypeString(reference);
            if (containsTemplate(reference, returnType))
            	returnType = "*";
            else if (returnType.equals("RESULT"))
            	returnType = "Object";
        }
        else
        {
            returnType = JSTypeUtils.toReturnTypeString(reference);
        }

        sb.append(returnType);

        return sb.toString();
    }

    public static String toParameterString(BaseReference reference, JSDocInfo comment, Node paramNode, boolean outputJS)
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
                    	String name = param.getString();
                    	int c = name.indexOf("$jscomp$");
                    	if (c != -1)
                    		name = name.substring(0, c);
                        sb.append(name);
                        if (!outputJS)
                        	sb.append(":Object");
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
                    sb.append(toParameter(reference, comment, paramName, comment.getParameterType(paramName), outputJS));

                    if (index < len - 1)
                        sb.append(", ");

                    index++;
                }
            }
        }

        sb.append(")");

        return sb.toString();
    }

    /**
     * Check we can import the given type into the given package.
     *
     * @param model The containing reference model
     * @param node The containing node
     * @param typeName The type we want check
     * @param packageName The current package
     * @return true if we can import the given type into the given package
     */
    public static boolean canBeImported(final ReferenceModel model, final Node node, final String typeName,
            final String packageName)
    {
        boolean canImport = false;

        if (model != null && node != null && !Strings.isNullOrEmpty(typeName))
        {
            final ClassReference reference = new ClassReference(null, node, typeName);

            final int lastDotPosition = typeName.lastIndexOf(".");

            // Can import when the type to import does not belong to the current package.
            canImport = lastDotPosition > -1 && !typeName.substring(0, lastDotPosition).equals(packageName);

            // And is not excluded.
            canImport &= model.isExcludedClass(reference) == null;
        }

        return canImport;
    }

    public static String toParameter(BaseReference reference, JSDocInfo comment, String paramName,
            JSTypeExpression parameterType, boolean outputJS)
    {
        final StringBuilder sb = new StringBuilder();

        String paramType;

        if (parameterType == null)
        {
        	System.out.println("no parameter type for " + paramName + " " + reference.getQualifiedName());
            paramType = "Object";
            if (outputJS)
            	sb.append(paramName);
        }
        else if (parameterType.isVarArgs())
        {
        	if (outputJS)
        		sb.append("var_").append(paramName);
        	else
        		sb.append("...").append(paramName);
        }
        else
        {
            paramType = JSTypeUtils.toParamTypeString(reference, paramName);
            if (hasTemplate(reference) && containsTemplate(reference, paramType))
            {
                paramType = "Object";
            }

            if (outputJS && parameterType.isOptionalArg())
            {
            	sb.append("opt_");
            	sb.append(paramName);            	
            }
            else
            	sb.append(paramName);
            if (!outputJS)
            {
                sb.append(":");
                sb.append(paramType);            	
	            if (parameterType.isOptionalArg())
	            {
	                sb.append(" = ");
	                sb.append(toDefaultParameterValue(paramType));
	            }
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

    public static boolean hasTemplate(BaseReference reference)
    {
        if(reference instanceof MemberReference)
        {
            MemberReference memberRef = (MemberReference) reference;
            if(memberRef.getClassReference().getComment().getTemplateTypeNames().size() > 0)
            {
                return true;
            }
        }
        return reference.getComment().getTemplateTypeNames().size() > 0;
    }
    
    public static boolean containsTemplate(BaseReference reference, String name)
    {
        if(reference instanceof MemberReference)
        {
            MemberReference memberRef = (MemberReference) reference;
            if(commentContainsTemplate(memberRef.getClassReference().getComment(), name))
            {
                return true;
            }
        }
        return commentContainsTemplate(reference.getComment(), name);
    }
    
    private static boolean commentContainsTemplate(JSDocInfo comment, String name)
    {
        for (String template : comment.getTemplateTypeNames())
        {
            if (name.contains("<" + template + ">"))
                return true;
            if (name.equals(template))
                return true;
        }
        return false;
    }

}
