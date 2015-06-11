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

import org.apache.flex.compiler.internal.codegen.externals.ExternalsClientConfig.ExcludedMemeber;
import org.apache.flex.compiler.internal.codegen.externals.utils.FunctionUtils;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;

public class MethodReference extends MemberReference
{

    private boolean isStatic;
    private MethodReference override;
    private Node paramNode;

    private MethodReference getContext()
    {
        return override == null ? this : override;
    }

    public boolean isStatic()
    {
        return isStatic;
    }

    public void setStatic(boolean isStatic)
    {
        this.isStatic = isStatic;
    }

    public MethodReference(ReferenceModel model, ClassReference classReference,
            Node node, String name, JSDocInfo comment, boolean isStatic)
    {
        super(model, classReference, node, name, comment);
        this.isStatic = isStatic;
        this.paramNode = node.getLastChild().getChildAtIndex(1);
    }

    @Override
    public void emit(StringBuilder sb)
    {
        if (isOverride())
            return;

        if (getClassReference().hasSuperMethod(getQualifiedName()))
            return;

        printComment(sb);

        ExcludedMemeber excluded = isExcluded();
        if (excluded != null)
        {
            excluded.print(sb);
        }

        String staticValue = (isStatic) ? "static " : "";
        if (getClassReference().isInterface())
            staticValue = "";

        String isOverride = "";
        //        if (TagUtils.hasTags(this, "override"))
        //        {
        //            isOverride = "override ";
        //            if (getClassReference().isMethodOverrideFromInterface(this))
        //            {
        //                override = getClassReference().getMethodOverrideFromInterface(
        //                        this);
        //
        //                isOverride = "";
        //            }
        //        }

        String publicModifier = "";
        String braces = "";
        String returns = "";

        if (!transformReturnString().equals("void"))
        {
            returns = " return null;";
        }

        if (!getClassReference().isInterface())
        {
            publicModifier = "public ";
            braces = " { " + returns + " }";
        }

        sb.append("    ");
        sb.append(publicModifier);
        sb.append(isOverride);
        sb.append(staticValue);
        sb.append("function ");
        sb.append(getQualifiedName());
        sb.append(toPrameterString());
        sb.append(":");
        sb.append(transformReturnString());
        sb.append(braces);
        sb.append("\n");

        override = null;
    }

    private String transformReturnString()
    {
        return FunctionUtils.transformReturnString(getContext(), getComment());
    }

    private String toPrameterString()
    {
        return FunctionUtils.toPrameterString(getContext(), getComment(),
                paramNode);
    }

    public boolean isOverride()
    {
        return getComment().isOverride();
    }

}
