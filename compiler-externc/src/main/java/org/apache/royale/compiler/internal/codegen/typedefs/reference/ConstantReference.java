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

package org.apache.royale.compiler.internal.codegen.typedefs.reference;

import java.io.File;
import java.util.HashMap;

import org.apache.royale.compiler.internal.codegen.typedefs.utils.JSTypeUtils;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

public class ConstantReference extends BaseReference
{

    @SuppressWarnings("unused")
    private JSType type;

    public ConstantReference(ReferenceModel model, Node node,
            String qualifiedName, JSDocInfo comment)
    {
        super(model, node, qualifiedName, node.getJSDocInfo());

        /*
        VAR 70 [jsdoc_info: JSDocInfo]
            NAME self  [is_constant_var: 1]
        */
    }

    public ConstantReference(ReferenceModel model, Node node,
            String qualifiedName, JSDocInfo comment, JSType type)
    {
        super(model, node, qualifiedName, node.getJSDocInfo());
        this.type = type;
    }

    public File getFile(File asSourceRoot)
    {
        String packageName = "";

        return new File(asSourceRoot, packageName + File.separator
                + getQualifiedName() + ".as");
    }

    @Override
    public void emit(StringBuilder sb)
    {
        sb.append("package ");

        sb.append("{\n");
        sb.append("\n");

        String type = JSTypeUtils.toConstantTypeString(this);
        String value = resolveValue(type);

        if (getQualifiedName().equals("undefined"))
        {
            sb.append(indent);
            sb.append("public const undefined:* = void 0;\n");
        }
        else if (getQualifiedName().equals("NaN"))
        {
            sb.append(indent);
            sb.append("public const NaN:Number = 0/0;\n");
        }
        else if (getQualifiedName().equals("Infinity"))
        {
            sb.append(indent);
            sb.append("public const Infinity:Number = 1/0;\n");
        }
        else
        {
            sb.append(indent);
            sb.append("public const " + getQualifiedName() + ":" + type + " = "
                    + value + ";\n");
        }

        sb.append("}\n"); // package
    }

    private String resolveValue(String type)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Number", "0");
        map.put("undefined", "0");
        map.put("Boolean", "false");

        if (map.containsKey(type))
            return map.get(type);

        return type.equals("*") ? "undefined" : "null";
    }

}
