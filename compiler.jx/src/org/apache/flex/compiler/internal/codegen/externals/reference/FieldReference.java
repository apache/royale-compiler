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

package org.apache.flex.compiler.internal.codegen.externals.reference;

import org.apache.flex.compiler.clients.ExternCConfiguration.ExcludedMemeber;
import org.apache.flex.compiler.internal.codegen.externals.utils.FunctionUtils;
import org.apache.flex.compiler.internal.codegen.externals.utils.JSTypeUtils;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

public class FieldReference extends MemberReference
{

    private boolean isStatic;
    private String overrideStringType;

    public boolean isStatic()
    {
        return isStatic;
    }

    public void setStatic(boolean isStatic)
    {
        this.isStatic = isStatic;
    }

    public void setOverrideStringType(String overrideStringType)
    {
        this.overrideStringType = overrideStringType;
    }

    public String toTypeAnnotationString()
    {
        JSType jsType = getComment().getType().evaluate(null,
                getModel().getCompiler().getTypeRegistry());
        return jsType.toAnnotationString();
    }

    public FieldReference(ReferenceModel model, ClassReference classReference,
            Node node, String name, JSDocInfo comment, boolean isStatic)
    {
        super(model, classReference, node, name, comment);
        this.isStatic = isStatic;
    }

    @Override
    public void emit(StringBuilder sb)
    {
        // Window has a global var Window that conflicts with the constructor.
        if (getQualifiedName().equals(getClassReference().getQualifiedName()))
            return;

        if (getClassReference().hasSuperField(getQualifiedName()))
            return;

        printComment(sb);

        ExcludedMemeber excluded = isExcluded();
        if (excluded != null)
        {
            excluded.print(sb);
        }

        if (!getClassReference().isInterface() && !getComment().isOverride())
        //&& !getClassReference().isPropertyInterfaceImplementation(this))
        {
            printVar(sb);
        }
        else
        {
            printAccessor(sb);
        }

    }

    private void printAccessor(StringBuilder sb)
    {
        String staticValue = "";//(isStatic) ? "static " : "";

        String isPublic = getClassReference().isInterface() ? "" : "public ";

        sb.append("    " + isPublic + staticValue + "function get "
                + getQualifiedName() + "():" + toReturnString() + ";\n");
        sb.append("    " + isPublic + staticValue + "function set "
                + getQualifiedName() + "(" + toPrameterString() + "):void"
                + ";\n");
    }

    private void printVar(StringBuilder sb)
    {
        String staticValue = (isStatic) ? "static " : "";

        String type = toTypeString();
        if (type.indexOf("|") != -1 || type.indexOf("?") != -1)
            type = "*";

        sb.append("    public " + staticValue + "var " + getQualifiedName()
                + ":" + type + ";\n");
    }

    private String toTypeString()
    {
        if (overrideStringType != null)
            return overrideStringType;
        return JSTypeUtils.toFieldString(this);
    }

    private String toReturnString()
    {
        return toPrameterString().replace("value:", "");
    }

    private String toPrameterString()
    {
        return FunctionUtils.toParameter(this, getComment(), "value",
                getComment().getType());
    }

    @Override
    protected void emitCommentBody(StringBuilder sb)
    {
        emitBlockDescription(sb);
        emitType(sb);
        emitSee(sb);
        emitSeeSourceFileName(sb);
    }

    private void emitType(StringBuilder sb)
    {
        JSTypeExpression type = getComment().getType();
        if (type != null)
        {
            sb.append("     * @see JSType - ");
            sb.append("[");
            sb.append(type.evaluate(null,
                    getModel().getCompiler().getTypeRegistry()).toAnnotationString());
            sb.append("] ");
            String description = getComment().getReturnDescription();
            if (description != null)
                sb.append(description);
            sb.append("\n");
        }

    }

}
