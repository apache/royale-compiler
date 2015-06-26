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

import java.util.HashMap;

import org.apache.flex.compiler.internal.codegen.externals.reference.BaseReference;
import org.apache.flex.compiler.internal.codegen.externals.reference.ConstantReference;
import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel;

import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.UnionType;

public class JSTypeUtils
{
    public static String toParamTypeString(BaseReference reference,
            String paramName)
    {
        String type = "Object";

        JSTypeExpression paramType = reference.getComment().getParameterType(
                paramName);

        if (paramType != null)
        {
            JSType jsType = JSTypeUtils.toParamJsType(reference.getModel(),
                    paramType);
            //System.err.println(jsType);

            if (jsType != null)
            {
                type = jsType.toString();

                if (jsType.isFunctionType())
                {
                    return "Function /* " + type + " */";
                }
                else if (jsType.isRecordType())
                {
                    return "Object /* " + type + " */";
                }
                else
                {
                    if (type.indexOf("Array<") == 0)
                    {
                        return "Array";
                    }
                    else if (type.indexOf("Object<") == 0)
                    {
                        return "Object";
                    }
                }

            }
            else
            {
                return "Object"; // TemplateType
            }
        }

        type = transformParamType(type);

        return type;
    }

    private static JSType toParamJsType(ReferenceModel model,
            JSTypeExpression typeExpression)
    {
        JSType jsType = model.evaluate(typeExpression);

        if (jsType.isUnionType())
        {
            UnionType ut = (UnionType) jsType;
            JSType jsType2 = ut.restrictByNotNullOrUndefined();

            //System.err.println(jsType2);

            if (!jsType2.isUnionType())
                jsType = jsType2;
        }

        return jsType;
    }

    public static String toConstantTypeString(ConstantReference reference)
    {
        JSTypeExpression typeExpression = reference.getComment().getType();
        JSType jsType = reference.getModel().evaluate(typeExpression);
        String type = jsType.toString();
        type = transformParamType(type);
        return type;
    }

    public static String toReturnTypeString(BaseReference reference)
    {
        String type = "void";

        JSTypeExpression returnType = reference.getComment().getReturnType();
        if (returnType != null)
        {

            JSType jsType = JSTypeUtils.toReturnJsType(reference.getModel(),
                    returnType);
            //System.err.println(jsType);

            if (jsType != null)
            {
                if (jsType.isRecordType())
                    return "Object";

                type = jsType.toString();

                if (type.indexOf("Array<") == 0)
                {
                    return "Array";
                }
                else if (type.indexOf("Object<") == 0)
                {
                    return "Object";
                }
            }
            else
            {
                return "Object"; // TemplateType
            }
        }

        type = transformReturnType(type);

        return type;
    }

    private static JSType toReturnJsType(ReferenceModel model,
            JSTypeExpression typeExpression)
    {
        JSType jsType = model.evaluate(typeExpression);

        if (jsType.isUnionType())
        {
            UnionType ut = (UnionType) jsType;
            JSType jsType2 = ut.restrictByNotNullOrUndefined();

            if (!jsType2.isUnionType())
                jsType = jsType2;
        }

        return jsType;
    }

    public static String toFieldString(BaseReference reference)
    {
        String type = "Object";

        JSTypeExpression ttype = reference.getComment().getType();

        if (ttype != null)
        {
            JSType jsType = JSTypeUtils.toTypeJsType(reference.getModel(),
                    ttype);
            //System.err.println(jsType);

            if (jsType != null)
            {
                if (jsType.isUnionType())
                {
                    UnionType ut = (UnionType) jsType;
                    JSType jsType2 = ut.restrictByNotNullOrUndefined();

                    if (!jsType2.isUnionType())
                        jsType = jsType2;
                }

                type = jsType.toString();

                if (jsType.isFunctionType())
                {
                    return "Function /* " + type + " */";
                }
                else
                {
                    if (type.indexOf("Array<") == 0)
                    {
                        return "Array";
                    }
                    else if (type.indexOf("Object<") == 0)
                    {
                        return "Object";
                    }
                }
            }
            else
            {
                return "Object"; // TemplateType
            }
        }

        type = transformType(type);

        return type;
    }

    public static JSType toTypeJsType(ReferenceModel model,
            JSTypeExpression typeExpression)
    {
        JSType jsType = model.evaluate(typeExpression);

        if (jsType.isUnionType())
        {
            UnionType ut = (UnionType) jsType;
            JSType jsType2 = ut.restrictByNotNullOrUndefined();

            if (!jsType2.isUnionType())
                jsType = jsType2;
        }

        return jsType;
    }

    // XXX These are NOT for returned types
    private static String transformParamType(String type)
    {
        if (type.indexOf("|") != -1)
            return "Object";

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("?", "Object /* ? */");
        map.put("*", "*");
        map.put("string", "String");
        map.put("number", "Number");
        map.put("boolean", "Boolean");
        map.put("undefined", "Object /* undefined */");
        map.put("null", "Object /* null */");

        if (map.containsKey(type))
            return map.get(type);

        return type;
    }

    private static String transformReturnType(String type)
    {
        if (type.indexOf("|") != -1)
            return "Object";

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("?", "Object /* ? */");
        map.put("*", "*");
        map.put("string", "String");
        map.put("number", "Number");
        map.put("boolean", "Boolean");
        map.put("undefined", "Object /* undefined */");
        map.put("null", "void /* null */");

        if (map.containsKey(type))
            return map.get(type);

        return type;
    }

    // XXX shouldn't be public
    public static String transformType(String type)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("?", "Object /* ? */");
        map.put("*", "*");
        map.put("string", "String");
        map.put("number", "Number");
        map.put("boolean", "Boolean");
        map.put("undefined", "Object /* undefined */");
        map.put("null", "Object /* null */");

        if (map.containsKey(type))
            return map.get(type);

        return type;
    }

}
