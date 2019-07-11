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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.clients.ExternCConfiguration.ExcludedMember;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.FunctionUtils;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;

public class FunctionReference extends BaseReference {
    private boolean isStatic;
    private Node paramNode;
    private Set<String> imports = new HashSet<String>();
    private List<ParameterReference> parameters;

    public File getFile(File asSourceRoot) {
        String packageName = "";

        return new File(asSourceRoot, packageName + File.separator + getQualifiedName() + ".as");
    }

    private FunctionReference getContext() {
        return this;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean isStatic) {
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

    public FunctionReference(ReferenceModel model, Node node, String qualifiedName, JSDocInfo comment) {
        super(model, node, qualifiedName, comment);
        this.paramNode = node.getChildAtIndex(1);

        addParameterReferences();
    }

    private void addParameterReferences() {

        parameters = new ArrayList<ParameterReference>();

        if (paramNode != null) {

            final boolean isDocumented = comment.getParameterCount() > 0;
            List<String> parameterNames = null;

            if (isDocumented) {
                parameterNames = Lists.newArrayList(comment.getParameterNames());
            }

            for (Node param : paramNode.children()) {
                ParameterReference parameterReference;

                if ((parameterNames != null) && parameterNames.contains(param.getString())) {
                    final String qualifiedName = FunctionUtils.toParameterType(this, param.getString());
                    parameterReference = new ParameterReference(getModel(), param, qualifiedName);
                } else {
                    parameterReference = new ParameterReference(getModel(), param);
                }

                parameters.add(parameterReference);
            }
        }
    }

    @Override
    public void emit(StringBuilder sb) {
        String packageName = "";

        sb.append("package ");
        sb.append(packageName).append(" ");
        sb.append("{\n");
        sb.append("\n");

        printImports(sb);

        emitComment(sb);

        ExcludedMember excluded = isExcluded();
        if (excluded != null) {
            excluded.print(sb);
        }

        String staticValue = (isStatic) ? "static " : "";

        String publicModifier = "public ";
        String braces;

        String returns = "";
        if (!transformReturnString().equals("void")) {
            returns = " return null;";
        }

        braces = " { " + returns + " }";

        sb.append(indent);
        sb.append(publicModifier);
        sb.append(staticValue);
        sb.append("function ");
        sb.append(getQualifiedName());
        sb.append(toParameterString());
        sb.append(":");
        sb.append(transformReturnString());
        sb.append(braces);
        sb.append("\n");

        sb.append("}\n"); // package
    }

    private void printImports(final StringBuilder sb) {
        if (imports.size() > 0) {
            for (String anImport : imports) {
                sb.append("import ").append(anImport).append(";\n");
            }
            sb.append("\n");
        }
    }

    public String transformReturnString() {
        return FunctionUtils.toReturnString(getContext());
    }

    private String toParameterString() {
        return FunctionUtils.toParameterString(getContext(), getComment(), paramNode, outputJS);
    }

    public boolean isOverride() {
        return getComment().isOverride();
    }

    @Override
    protected void emitCommentBody(StringBuilder sb) {
        emitFunctionCommentBody(sb);
    }

    public void addImport(ClassReference reference) {
        if (reference != null) {
            imports.add(reference.getQualifiedName());
        }
    }

    public List<ParameterReference> getParameters() {
        return parameters;
    }

    public boolean hasImport(String qualifiedName) {
        return imports.contains(qualifiedName);
    }
}
