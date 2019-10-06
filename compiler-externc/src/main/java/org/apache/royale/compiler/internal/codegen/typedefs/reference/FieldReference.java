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

import java.util.Collection;

import org.apache.royale.compiler.clients.ExternCConfiguration.ExcludedMember;
import org.apache.royale.compiler.clients.ExternCConfiguration.ReadOnlyMember;
import org.apache.royale.compiler.clients.ExternCConfiguration.TrueConstant;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.FunctionUtils;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.JSTypeUtils;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Marker;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSType.Nullability;

public class FieldReference extends MemberReference
{

    private boolean isStatic;
    private boolean isConst;
    private String overrideStringType;
    private Node constantValueNode;
    private String constantValue;

    public boolean isStatic()
    {
        return isStatic;
    }

    public void setStatic(boolean isStatic)
    {
        this.isStatic = isStatic;
    }

    public boolean isConst()
    {
        return isConst;
    }

    public void setConst(boolean isConst)
    {
        this.isConst = isConst;
    }

    public void setOverrideStringType(String overrideStringType)
    {
        this.overrideStringType = overrideStringType;
    }

    public void setConstantValueNode(Node constantValueNode)
    {
        this.constantValueNode = constantValueNode;
    }

    public String toTypeAnnotationString()
    {
        JSType jsType = null;
        if (getComment() != null && getComment().getType() != null)
        {
            jsType = getModel().evaluate(getComment().getType());
        }
        return jsType != null ? jsType.toAnnotationString(Nullability.EXPLICIT) : "Object";
    }

    public FieldReference(ReferenceModel model, ClassReference classReference, Node node, String name,
            JSDocInfo comment, boolean isStatic)
    {
        super(model, classReference, node, name, comment);
        Collection<Marker> markers = comment.getMarkers();
        for (Marker marker : markers)
        {
        	if (marker.getAnnotation().getItem().equals("const"))
        		this.isConst = true;
        }
        TrueConstant constant = isTrueConstant();
        if (constant != null)
        	constantValue = constant.getValue();
        this.isStatic = isStatic;
    }

    @Override
    public void emit(StringBuilder sb)
    {
        // XXX HACK TEMP!
        if (getComment().isConstructor())
            return;

        // Window has a global var Window that conflicts with the constructor.
        if (getQualifiedName().equals(getClassReference().getQualifiedName()))
            return;

        if (getClassReference().hasSuperField(getQualifiedName()))
            return;

        emitComment(sb);

        ExcludedMember excluded = isExcluded();
        if (excluded != null)
        {
            excluded.print(sb);
            return; // XXX (mschmalle) accessors are not treated right, need to exclude get/set
        }
        
        ReadOnlyMember readOnly = isReadOnly();

        if (!getClassReference().isInterface() && !getComment().isOverride()
                && !getClassReference().isPropertyInterfaceImplementation(getBaseName())
                && (null == readOnly))
        {
        	if (isConst && constantValue == null)
        		emitAccessor(sb, true); // const is used for readOnly as well.  If there is an initial value assume it is const
        	else
        		emitVar(sb);
        }
        else
        {
            emitAccessor(sb, (null != readOnly));
        }
    }

    private void emitAccessor(StringBuilder sb, boolean isReadOnly)
    {
        boolean isInterface = getClassReference().isInterface();

        String staticValue = (isStatic) ? "static " : "";
        String isPublic = isInterface ? "" : "public ";
        String getBody = isInterface ? "" : "{ return null; }";
        String setBody = isInterface ? "" : "{}";

        String type = toTypeString();
        if (type.contains("|") || type.contains("?"))
            type = "*";

        if (outputJS)
        {
        	sb.append(getClassReference().getPackageName());
        	sb.append(".");
        	sb.append(getClassReference().getBaseName());
        	sb.append(".");
        	if (!isStatic)
        		sb.append("prototype.");
        	sb.append(getBaseName());
        	sb.append(";\n");
        	return;
        }
        // getter
        sb.append(indent);
        sb.append(isPublic);
        sb.append(staticValue);
        sb.append("function get ");
        sb.append(getBaseName());
        sb.append("():");
        sb.append(type);
        sb.append(getBody);
        sb.append(";\n");

        if (!isReadOnly)
        {
	        // setter
	        sb.append(indent);
	        sb.append(isPublic);
	        sb.append(staticValue);
	        sb.append("function set ");
	        sb.append(getBaseName());
	        sb.append("(value:");
	        sb.append(type);
	        sb.append("):void");
	        sb.append(setBody);
	        sb.append(";\n");
        }
    }

    private void emitVar(StringBuilder sb)
    {
        String staticValue = (isStatic) ? "static " : "";
        String constVarValue = (isConst) ? "const " : "var ";

        String type = toTypeString();
        if (type.contains("|") || type.contains("?"))
            type = "*";

        if (outputJS)
        {
        	sb.append(getClassReference().getPackageName());
        	sb.append(".");
        	sb.append(getClassReference().getBaseName());
        	sb.append(".");
        	if (!isStatic)
        		sb.append("prototype.");
        	sb.append(getBaseName());
        	sb.append(";\n");
        	return;
        }
        
        sb.append(indent);
        sb.append("public ");
        sb.append(staticValue);
        sb.append(constVarValue);
        sb.append(getQualifiedName());
        sb.append(":");
        sb.append(type);
        if (isConst)
        {
            emitConstValue(sb);
        }
        sb.append(";\n");
    }

    private void emitConstValue(StringBuilder sb)
    {
        sb.append(" = ");
        if (constantValueNode != null)
        	sb.append(toConstValue(constantValueNode));
        else
        	sb.append(constantValue);
    }

    private String toConstValue(Node node)
    {
    	String typeString = toTypeString();
        if (typeString.equals("Number"))
            return Integer.toString(getClassReference().getEnumConstant());
        if (node == null)
        {
        	if (typeString.equals("Number"))
        		return "NaN";
        	return "null";
        }
        if (node.isString())
            return "'" + node.getString() + "'";
        return "undefined /* TODO type not set */";
    }

    public String toTypeString()
    {
        if (overrideStringType != null)
            return overrideStringType;
        String typeString = JSTypeUtils.toFieldTypeString(this);
        if (FunctionUtils.hasTemplate(this)
                && FunctionUtils.containsTemplate(this, typeString))
        {
            return "Object";
        }
        return typeString;
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
        	if (outputJS)
        	{
                sb.append(indent);
                sb.append(" * @type ");
                sb.append("{");
                sb.append(mapBackToJS(getModel().evaluate(type).toAnnotationString(Nullability.EXPLICIT), false));
                sb.append("} ");
                sb.append("\n");
        	}
        	else
        	{
                sb.append(indent);
                sb.append(" * @see JSType - ");
                sb.append("[");
                sb.append(getModel().evaluate(type).toAnnotationString(Nullability.EXPLICIT));
                sb.append("] ");
                String description = getComment().getReturnDescription();
                if (description != null)
                    sb.append(description);
                sb.append("\n");        		
        	}
        }
    }

}
