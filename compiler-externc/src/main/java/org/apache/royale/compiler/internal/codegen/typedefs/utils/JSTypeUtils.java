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

import java.util.HashMap;

import org.apache.royale.compiler.internal.codegen.typedefs.reference.BaseReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ConstantReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;

import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.NamedType;
import com.google.javascript.rhino.jstype.TemplatizedType;
import com.google.javascript.rhino.jstype.UnionType;
import com.google.javascript.rhino.jstype.JSType.Nullability;

public class JSTypeUtils
{
    public static String toClassTypeString(ClassReference reference)
    {
        String type = getJsType(reference.getModel(), reference.getComment().getBaseType()).toString();
        return type;
    }

    public static String toParamTypeString(BaseReference reference, String paramName)
    {
        JSTypeExpression expression = reference.getComment().getParameterType(paramName);
        if (expression == null)
            return "Object";

        //most, if not all, types defined with @typedef don't actually exist in
        //JS, so they cannot be instantiated. if a function uses one of these
        //types for a parameter, that function will be impossible to call!
        //we need to fall back to Object instead.
        JSType jsType = getJsType(reference.getModel(), expression);
        if(jsType instanceof NamedType)
        {
            ClassReference typeDef = reference.getModel().getTypeDefReference(jsType.getDisplayName());
            if(typeDef != null)
            {
                return "Object";
            }
        }

        String type = toTypeExpressionString(reference, expression);
        type = transformType(type);

        return type;
    }

    public static String toReturnTypeString(BaseReference reference)
    {
        JSTypeExpression expression = reference.getComment().getReturnType();
        if (expression == null)
            return "void";

        String type = toTypeExpressionString(reference, expression);
        type = transformType(type);

        return type;
    }

    public static String toFieldTypeString(BaseReference reference)
    {
        JSTypeExpression expression = reference.getComment().getType();
        if (expression == null)
            return "Object";

        String type = toTypeExpressionString(reference, expression);
        type = transformType(type);

        return type;
    }

    public static String toEnumTypeString(BaseReference reference)
    {
        JSTypeExpression enumParameterType = reference.getComment().getEnumParameterType();
        String overrideStringType = transformType(reference.getModel().evaluate(enumParameterType).toAnnotationString(Nullability.EXPLICIT));

        return overrideStringType;
    }

    public static String toConstantTypeString(ConstantReference reference)
    {
        JSTypeExpression expression = reference.getComment().getType();
        if (expression == null)
            return "Object";

        String type = toTypeExpressionString(reference, expression);
        type = transformType(type);

        return type;
    }

    //--------------------------------------------------------------------------

    public static String transformType(String type)
    {
        // a comment in the type might contain |, so strip the comment first
        String typeWithoutComment = type;
        int startIndex = typeWithoutComment.indexOf(" /*");
        if (startIndex != -1)
        {
            int endIndex = typeWithoutComment.indexOf("*/", startIndex);
            if (endIndex != -1)
            {
                typeWithoutComment = typeWithoutComment.substring(0, startIndex) +
                        typeWithoutComment.substring(endIndex + 2);
            }
        }
        // XXX This is an error but, needs to be reduced in @param union
        if (typeWithoutComment.indexOf("|") != -1)
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

    private static String toTypeExpressionString(BaseReference reference, JSTypeExpression expression)
    {
        JSType jsType = getJsType(reference.getModel(), expression);
        String type = toTypeString(jsType);
        return type;
    }

    private static String toTypeString(JSType jsType)
    {
        String type = jsType.toString();

        if (jsType.isFunctionType())
        {
            return "Function /* " + type + " */";
        }
        else if (jsType.isRecordType())
        {
            return "Object /* " + type + " */";
        }
        else if (type.equals("None"))
        {
            return "* /* " + type + " */";
        }
        else if (jsType.isTemplatizedType())
        {
        	return ((TemplatizedType)jsType).getReferencedType().toString();
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

        return type;
    }

    private static JSType getJsType(ReferenceModel model, JSTypeExpression typeExpression)
    {
        JSType jsType = model.evaluate(typeExpression);

        if (jsType.isUnionType())
        {
            UnionType ut = (UnionType) jsType;
            JSType jsType2 = ut.restrictByNotNullOrUndefined();

            if (!jsType2.isUnionType())
                jsType = jsType2;
        }

        ClassReference typeDef = model.getTypeDefReference(jsType.getDisplayName());
        if (typeDef != null)
        {
            JSTypeExpression typeDefTypeExpression = typeDef.getComment().getTypedefType();
            if (typeDefTypeExpression != null)
            {
                JSType typeDefJSType = getJsType(model, typeDefTypeExpression);
                if (typeDefJSType.isFunctionType())
                {
                    // I'm not sure what the implications would be when
                    // returning the JSType for any typedef, so I'm limiting it
                    // to function typedefs for now. this should be
                    // revisited eventually. -JT
                    jsType = typeDefJSType;
                }
            }
        }

        return jsType;
    }
}
