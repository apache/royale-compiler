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

import java.io.File;

import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel.ExcludedMemeber;
import org.apache.flex.compiler.internal.codegen.externals.utils.FunctionUtils;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;

public class FunctionReference extends BaseReference
{
    private boolean isStatic;
    private FunctionReference override;
    private Node paramNode;

    public File getFile(File asSourceRoot)
    {
        String packageName = "";

        return new File(asSourceRoot, packageName + File.separator
                + getQualifiedName() + ".as");
    }

    private FunctionReference getContext()
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

    /*
    FUNCTION [node] Scope:Global
        NAME
        PARAM_LIST
            NAME
            NAME
        BLOCK
    */

    public FunctionReference(ReferenceModel model, Node node,
            String qualifiedName, JSDocInfo comment)
    {
        super(model, node, qualifiedName, comment);
        this.paramNode = node.getChildAtIndex(1);
    }

    @Override
    public void emit(StringBuilder sb)
    {
        String packageName = "";

        sb.append("package ");
        sb.append(packageName + " ");
        sb.append("{\n");
        sb.append("\n");

        printImports();

        printComment(sb);

        ExcludedMemeber excluded = isExcluded();
        if (excluded != null)
        {
            excluded.print(sb);
        }

        String staticValue = (isStatic) ? "static " : "";

        String publicModifier = "public ";
        String braces = "";

        String returns = "";
        if (!transformReturnString().equals("void"))
        {
            returns = " return null;";
        }

        braces = " { " + returns + " }";

        sb.append("    ");
        sb.append(publicModifier);
        sb.append(staticValue);
        sb.append("function ");
        sb.append(getQualifiedName());
        sb.append(toPrameterString());
        sb.append(":");
        sb.append(transformReturnString());
        sb.append(braces);
        sb.append("\n");

        sb.append("}\n"); // package
    }

    private void printImports()
    {
        // TODO Auto-generated method stub

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
