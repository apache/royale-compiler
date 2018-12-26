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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.clients.ExternCConfiguration.ExcludedMember;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.FunctionUtils;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSType.Nullability;

public class MethodReference extends MemberReference
{

    private boolean isStatic;
    private MethodReference override;
    private Node paramNode;

    private List<ParameterReference> parameters;

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

    public List<ParameterReference> getParameters()
    {
        return parameters;
    }

    public Set<String> getParameterNames()
    {
        return getComment().getParameterNames();
    }

    public String toReturnTypeAnnotationString()
    {
        JSType jsType = getModel().evaluate(getComment().getReturnType());
        return jsType.toAnnotationString(Nullability.EXPLICIT);
    }

    public MethodReference(ReferenceModel model, ClassReference classReference, Node node, String name,
            JSDocInfo comment, boolean isStatic)
    {
        super(model, classReference, node, name, comment);
        this.isStatic = isStatic;

        if (node.isFunction())
        {
            this.paramNode = node.getChildAtIndex(1);
        }
        else if (node.getLastChild().isFunction())
        {
            this.paramNode = node.getLastChild().getChildAtIndex(1);
        }

        addParameterReferences();
    }

    private void addParameterReferences()
    {

        parameters = new ArrayList<ParameterReference>();

        if (paramNode != null)
        {

            final boolean isDocumented = comment.getParameterCount() > 0;
            List<String> parameterNames = null;

            if (isDocumented)
            {
                parameterNames = Lists.newArrayList(comment.getParameterNames());
            }

            for (Node param : paramNode.children())
            {
                ParameterReference parameterReference;

                if ((parameterNames != null) && parameterNames.contains(param.getString()))
                {
                    final String qualifiedName = FunctionUtils.toParameterType(this, param.getString());
                    parameterReference = new ParameterReference(getModel(), param, qualifiedName);
                }
                else
                {
                    parameterReference = new ParameterReference(getModel(), param);
                }

                parameters.add(parameterReference);
            }
        }
        else if(comment.getParameterCount() > 0 || comment.getReturnType() != null)
        {
            for (int i = 0; i < comment.getParameterCount(); i++)
            {
                String parameterName = comment.getParameterNameAt(i);
                String qualifiedName = FunctionUtils.toParameterType(this, parameterName);
                ParameterReference parameterReference = new ParameterReference(getModel(), parameterName, qualifiedName);
                parameters.add(parameterReference);
            }
        }
        else
        {
            System.out.println(getQualifiedName() + " parameters not found! " + " " + comment.getParameterCount());
        }
    }

    @Override
    public void emit(StringBuilder sb)
    {
    	String className = getClassReference().getBaseName();
    	
        // XXX HACK TEMP!
        if (getComment().isConstructor() && !getBaseName().equals(className))
            return;

        if (isConstructor())
        {
            emitConstructor(sb);
            return;
        }

        String qName = getQualifiedName();
        // skip overrides since they have to have the same signature as the super method
        if (getClassReference().hasSuperMethod(qName))
        	return;

        emitComment(sb);

        ExcludedMember excluded = isExcluded();
        if (excluded != null)
        {
            excluded.print(sb);
        }

        emitCode(sb);

        override = null;
    }

    public void emitCode(StringBuilder sb)
    {
        String staticValue = (isStatic) ? "static " : "";
        if (getClassReference().isInterface())
            staticValue = "";

        String isOverride = "";

        if (!getClassReference().isInterface())
        {
            MethodReference overrideFromInterface = getClassReference().getMethodOverrideFromInterface(this);
            if (/*isOverride() && */overrideFromInterface != null)
            {
                override = overrideFromInterface;
            }
        }

        String qName = getQualifiedName();
        
        String publicModifier = "";
        String braces = "";
        String returns = "";

        String returnString = transformReturnString();
        if (!returnString.equals("void"))
        {
        	if (returnString.equals("Number"))
        		returns = "return 0;";
        	else if (returnString.equals("String"))
        		returns = "return '';";
        	else
        		returns = " return null;";
        }

        if (!getClassReference().isInterface())
        {
            publicModifier = "public ";
            braces = " { " + returns + " }";
        }
        
        if (getClassReference().hasSuperMethod(qName))
        {
        	isOverride = "override ";
        }

    	if (outputJS)
    	{
        	sb.append(getClassReference().getPackageName());
        	sb.append(".");
        	sb.append(getClassReference().getBaseName());
        	sb.append(".");
        	if (!isStatic)
        		sb.append("prototype.");
        	sb.append(qName);    		
        	sb.append(" = function "); 		
            sb.append(toParameterString());
            sb.append(braces);
            sb.append("\n");
            return;
    	}
    	
        sb.append(indent);
        sb.append(publicModifier);
        sb.append(isOverride);
        sb.append(staticValue);
        sb.append("function ");
        sb.append(getQualifiedName());
        sb.append(toParameterString());
        sb.append(":");
        sb.append(transformReturnString());
        sb.append(braces);
        sb.append("\n");
    }

    private void emitConstructor(StringBuilder sb)
    {
    	if (!outputJS)
    		emitComment(sb);

    	if (outputJS)
    	{
        	sb.append(getClassReference().getPackageName());
        	sb.append(".");
        	sb.append(getBaseName());    		
        	sb.append(" = function "); 		
            sb.append(toParameterString());
            sb.append(" {}\n");
            return;
    	}
    	
        sb.append(indent);
        sb.append("public function ");
        sb.append(getBaseName());
        if (!getBaseName().equals("Object"))
        {
            sb.append(toParameterString());
            sb.append(" {\n");
            sb.append(indent);
            emitSuperCall(sb);
            sb.append(indent);
            sb.append("}");
        }
        else
        {
            sb.append("() {}");
        }

        sb.append("\n");
    }

    private void emitSuperCall(StringBuilder sb)
    {

        sb.append(indent);
        sb.append("super(");

        ClassReference superClass = getClassReference().getSuperClass();
        if (superClass != null && !superClass.getBaseName().equals("Object"))
        {
            MethodReference constructor = superClass.getConstructor();
            Set<String> parameterNames = constructor.getParameterNames();
            int len = parameterNames.size();
            for (int i = 0; i < len; i++)
            {
                sb.append("null");
                if (i < len - 1)
                    sb.append(", ");
            }
        }

        sb.append(");\n");
    }

    public boolean isConstructor()
    {
        return getComment().isConstructor();
    }

    public String transformReturnString()
    {
        return FunctionUtils.toReturnString(getContext());
    }

    private String toParameterString()
    {
        if (paramNode != null)
        {
            return FunctionUtils.toParameterString(getContext(), getContext().getComment(), paramNode, outputJS);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int len = comment.getParameterCount();
        for (int i = 0; i < len; i++)
        {
            String parameterName = comment.getParameterNameAt(i);
            JSTypeExpression parameterType = comment.getParameterType(parameterName);
            sb.append(FunctionUtils.toParameter(getContext(), comment, parameterName, parameterType, outputJS));
            if (i < len - 1)
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean isOverride()
    {
        return getComment().isOverride();
    }

    @Override
    protected void emitCommentBody(StringBuilder sb)
    {
        emitFunctionCommentBody(sb);
    }
}
