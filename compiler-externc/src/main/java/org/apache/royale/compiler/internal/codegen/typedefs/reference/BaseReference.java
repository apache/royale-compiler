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
import java.util.Set;

import org.apache.royale.compiler.clients.ExternCConfiguration.ExcludedMember;
import org.apache.royale.compiler.clients.ExternCConfiguration.ReadOnlyMember;
import org.apache.royale.compiler.clients.ExternCConfiguration.TrueConstant;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Marker;
import com.google.javascript.rhino.JSDocInfo.StringPosition;
import com.google.javascript.rhino.JSDocInfo.TypePosition;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType.Nullability;

public abstract class BaseReference implements Comparable< BaseReference >
{
    private String qualifiedName;

    protected JSDocInfo comment;

    private File currentFile;

    private Node node;

    private ReferenceModel model;

    protected boolean outputJS;

    protected String indent = "    ";

    public File getCurrentFile()
    {
        return currentFile;
    }

    public void setCurrentFile(File currentFile)
    {
        this.currentFile = currentFile;
    }

    public String getBaseName()
    {
        return qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    }

    public String getPackageName()
    {
        int end = qualifiedName.lastIndexOf('.');
        if (end == -1)
            return "";
        return qualifiedName.substring(0, end);
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public final boolean isQualifiedName()
    {
        return qualifiedName.indexOf('.') != -1;
    }

    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }

    public void setComment(JSDocInfo comment)
    {
        this.comment = comment;
    }

    public JSDocInfo getComment()
    {
        return comment;
    }

    public ReferenceModel getModel()
    {
        return model;
    }

    public BaseReference(ReferenceModel model, Node node, String qualifiedName, JSDocInfo comment)
    {
        this.model = model;
        this.node = node;
        this.qualifiedName = qualifiedName;
        this.comment = comment;
        outputJS = model.getConfiguration().getJsRoot() != null;
    }

    public ExcludedMember isExcluded()
    {
        return null;
    }
    
    public ReadOnlyMember isReadOnly()
    {
    	return null;
    }

    public abstract void emit(StringBuilder sb);

    public void emitComment(StringBuilder sb)
    {
        sb.append(indent);
        sb.append("/**\n");
        emitCommentBody(sb);
        sb.append(indent);
        sb.append(" */\n");
    }

    protected void emitCommentBody(StringBuilder sb)
    {
        emitBlockDescription(sb);
        emitSee(sb);
        emitSeeSourceFileName(sb);
    }

    protected void emitBlockDescription(StringBuilder sb)
    {
        String blockDescription = getComment().getBlockDescription();
        if (blockDescription != null)
        {
            sb.append(indent);
            sb.append(" * ");
            sb.append(blockDescription.replaceAll("\\n", "\n" + indent + " * "));
            sb.append("\n ").append(indent).append("*\n");
        }
    }

    protected void emitSee(StringBuilder sb)
    {
        for (Marker marker : getComment().getMarkers())
        {
            StringPosition name = marker.getAnnotation();
            TypePosition typePosition = marker.getType();
            StringPosition descriptionPosition = marker.getDescription();
            StringBuilder desc = new StringBuilder();

            // XXX Figure out how to toString() a TypePosition Node for markers
            // XXX Figure out how to get a @param name form the Marker
            if (!name.getItem().equals("see"))
                continue;

            desc.append(name.getItem());
            desc.append(" ");

            if (typePosition != null)
            {
                //desc.append(typePosition.getItem().getString());
                //desc.append(" ");
            }

            if (descriptionPosition != null)
            {
                desc.append(descriptionPosition.getItem());
                desc.append(" ");
            }

            sb.append(indent);
            sb.append(" * @").append(desc.toString()).append("\n");
        }
    }

    protected void emitSeeSourceFileName(StringBuilder sb)
    {
        sb.append(indent);
        sb.append(" * @see ").append(getNode().getSourceFileName()).append("\n");
    }

    protected void emitFunctionCommentBody(StringBuilder sb)
    {
        emitBlockDescription(sb);
        emitParams(sb);
        emitSee(sb);
        emitSeeSourceFileName(sb);
        emitReturns(sb);
    }

    protected String mapBackToJS(String t, boolean optional)
    {
    	// remove all whitespace
    	t = t.replace(" ", "");
    	if (t.contains("{String}")) 
    		t = t.replace("{String}", "{string}");
    	if (t.contains("{Number}")) 
    		t = t.replace("{Number}", "{number}");
    	if (t.contains("{Boolean}")) 
    		t = t.replace("{Boolean}", "{boolean}");
    	if (t.contains("{object")) 
    		t = t.replace("{object}", "{Object}");
    	if (t.contains("(String|")) 
    		t = t.replace("(String|", "(string|");
    	if (t.contains("(Number|")) 
    		t = t.replace("(Number|", "(number|");
    	if (t.contains("(Boolean|")) 
    		t = t.replace("(Boolean|", "(boolean|");
    	if (t.contains("(object|")) 
    		t = t.replace("(object|", "(Object|");
    	if (t.contains("|String|")) 
    		t = t.replace("|String|", "|string|");
    	if (t.contains("|Number|")) 
    		t = t.replace("|Number|", "|number|");
    	if (t.contains("|Boolean|")) 
    		t = t.replace("|Boolean|", "|boolean|");
    	if (t.contains("|object|")) 
    		t = t.replace("|object|", "|Object|");
    	if (t.contains("|String)")) 
    		t = t.replace("|String)", "|string)");
    	if (t.contains("|Number)")) 
    		t = t.replace("|Number)", "|number)");
    	if (t.contains("|Boolean)")) 
    		t = t.replace("|Boolean)", "|boolean)");
    	if (t.contains("|object)")) 
    		t = t.replace("|object)", "|Object)");
    	if (optional)
    	{
    		// try to strip out undefined and null and replace with =
    		if (t.contains("null|"))
    			t = t.replace("null|", "");
    		if (t.contains("|null"))
    			t = t.replace("|null", "");
    		if (t.contains("undefined|"))
    			t = t.replace("undefined|", "");
    		if (t.contains("|undefined"))
    			t = t.replace("|undefined", "");
    		// strip off wrapping parens if not needed
    		if (!t.contains("|") && t.startsWith("(") && t.endsWith(")"))
    			t = t.substring(1, t.length() - 1);
    		t = t + "=";
    	}
    	return t;
    }
    
    protected void emitParams(StringBuilder sb)
    {
        Set<String> parameterNames = getComment().getParameterNames();
        for (String paramName : parameterNames)
        {
            JSTypeExpression parameterType = getComment().getParameterType(paramName);
            String description = getComment().getDescriptionForParameter(paramName);

            sb.append(indent);
            sb.append(" * @param ");

            boolean optional = parameterType != null && parameterType.isOptionalArg();
            if (outputJS && parameterType != null)
            {
                sb.append("{");
                sb.append(mapBackToJS(getModel().evaluate(parameterType).toAnnotationString(Nullability.EXPLICIT), optional));
                sb.append("}");
                sb.append(" ");            	
            }
            
            if (outputJS && optional)
            {
            	sb.append("opt_");            	
            	sb.append(paramName);            	
            }
            else
            	sb.append(paramName);
            sb.append(" ");

            if (!outputJS && parameterType != null)
            {
                sb.append("[");
                sb.append(getModel().evaluate(parameterType).toAnnotationString(Nullability.EXPLICIT));
                sb.append("]");
                sb.append(" ");
            }
            if (description != null)
                sb.append(description);
            sb.append("\n");
        }
    }

    protected void emitReturns(StringBuilder sb)
    {
        if (getComment().hasReturnType())
        {
            JSTypeExpression returnType = getComment().getReturnType();
            if (returnType != null)
            {
                sb.append(indent);
                sb.append(" * @returns ");
                sb.append("{");
                if (outputJS)
                    sb.append(mapBackToJS(getModel().evaluate(returnType).toAnnotationString(Nullability.EXPLICIT), false));
                else
                	sb.append(getModel().evaluate(returnType).toAnnotationString(Nullability.EXPLICIT));
                sb.append("} ");
                String description = getComment().getReturnDescription();
                if (description != null)
                    sb.append(description);
                sb.append("\n");
            }
        }
    }

	public TrueConstant isTrueConstant() {
		return null;
	}
	
	@Override
    public int compareTo(BaseReference o) {
        return this.getBaseName().compareTo(o.getBaseName());
    }
}
