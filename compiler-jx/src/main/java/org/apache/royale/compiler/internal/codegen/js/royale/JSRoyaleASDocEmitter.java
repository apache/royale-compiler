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

package org.apache.royale.compiler.internal.codegen.js.royale;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.apache.royale.compiler.asdoc.IASDocTag;
import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleASDocEmitter;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleEmitter;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.metadata.IDeprecationInfo;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyaleASDocEmitter;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.EventDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleASDocProject;
import org.apache.royale.compiler.internal.tree.as.metadata.EventTagNode;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.utils.NativeUtils;

/**
 * Concrete implementation of the 'Royale' JavaScript production.
 *
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSRoyaleASDocEmitter extends JSGoogEmitter implements IJSRoyaleEmitter, IJSRoyaleASDocEmitter
{

	private boolean firstMember = true;
	
    @Override
    public String postProcess(String output)
    {
    	return output;
    }

    public JSRoyaleASDocEmitter(FilterWriter out)
    {
        super(out);
    }

    @Override
    protected void writeIndent()
    {
        write(JSRoyaleEmitterTokens.INDENT);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSRoyaleEmitterTokens.INDENT.getToken());
        return sb.toString();
    }
    
    @Override
    public void emitNamespace(INamespaceNode node)
    {
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
        writeNewline("{ \"type\": \"namespace\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\"");
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        indentPop();
        writeNewline("}");
    }


    @Override
    public String formatQualifiedName(String name)
    {
        return formatQualifiedName(name, false);
    }

    public MXMLRoyaleASDocEmitter mxmlEmitter = null;

    public String formatQualifiedName(String name, boolean isDoc)
    {
    	if (mxmlEmitter != null)
    		name = mxmlEmitter.formatQualifiedName(name);
        /*
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        */
    	if (getModel().isInternalClass(name))
    		return getModel().getInternalClasses().get(name);
        if (NativeUtils.isJSNative(name)) return name;
    	if (name.startsWith("window."))
    		name = name.substring(7);
    	else if (!isDoc)
    	{
    	}
        return name;
    }

    public String convertASTypeToJS(String name)
    {
        String result = name;

        if (name.equals(""))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants.Class))
            result = IASLanguageConstants.Object;
        else if (name.equals(IASLanguageConstants._int)
                || name.equals(IASLanguageConstants.uint))
            result = IASLanguageConstants.Number;

        boolean isBuiltinFunction = name.matches("Vector\\.<.*>");
        if (isBuiltinFunction)
        {
        	result = IASLanguageConstants.Array;
        }
        return result;
    }

    //--------------------------------------------------------------------------
    // Package Level
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
    	IPackageNode packageNode = definition.getNode();
    	IFileNode fileNode = (IFileNode) packageNode.getAncestorOfType(IFileNode.class);
        int nodeCount = fileNode.getChildCount();
        for (int i = 0; i < nodeCount; i++)
        {
	        IASNode pnode = fileNode.getChild(i);

	        if (pnode instanceof IPackageNode)
	        {
	        }
	        else if (pnode instanceof IClassNode)
	        {
	        	//getWalker().walk(pnode); don't emit internal classes outside of a package
	        }
	        else if (pnode instanceof IInterfaceNode)
	        {
	        	getWalker().walk(pnode);
	        }
            else if (pnode instanceof IFunctionNode)
            {
	        	getWalker().walk(pnode);
            }
            else if (pnode instanceof INamespaceNode)
            {
	        	getWalker().walk(pnode);
            }
            else if (pnode instanceof IVariableNode)
            {
	        	getWalker().walk(pnode);
            }
        }
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
    }

    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
    	if (!node.getDefinition().isPublic())
    		return;
    	
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        writeNewline("{ \"type\": \"class\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        indentPush();
        write("  \"baseClassname\": \"");
        IExpressionNode baseNode = node.getBaseClassExpressionNode();
        if (baseNode != null)
        {
            IDefinition baseDef = baseNode.resolve(getWalker().getProject());
        	write(formatQualifiedName(baseDef.getQualifiedName()));
        }
        else
        	write(formatQualifiedName(node.getBaseClassName()));
        writeNewline("\"");
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        final IDefinitionNode[] members = node.getAllMemberNodes();
        if (members.length > 0)
        {
        	writeNewline(",");
        	writeNewline("\"members\": [");
        	indentPush();
        	indentPush();
        }
        firstMember = true;
        for (IDefinitionNode mnode : members)
        {
        	getWalker().walk(mnode);
        }
        if (members.length > 0)
        {
            indentPop();
            indentPop();
        	writeNewline("]");
        }
        IMetaTagNode[] metas = node.getMetaTagNodesByName("Event");
        if (metas.length > 0)
        {
        	writeNewline(",");
        	writeNewline("\"events\": [");
        	indentPush();
        	indentPush();
        }
        firstMember = true;
        for (IMetaTagNode mnode : metas)
        {
        	writeEventTagNode(mnode);
        }
        if (metas.length > 0)
        {
            indentPop();
            indentPop();
        	writeNewline("]");
        }
        
        indentPop();
        writeNewline("}");
        addToIndex(node.getDefinition(), asDoc);
    	RoyaleASDocProject project = (RoyaleASDocProject)getWalker().getProject();
    	RoyaleASDocProject.ASDocRecord record = project.new ASDocRecord();
    	record.definition = node.getDefinition();
    	if (asDoc != null)
    		record.description = makeShortDescription(asDoc.getDescription());
    	else
    		record.description = "";
        ((RoyaleASDocProject)getWalker().getProject()).classes.put(formatQualifiedName(node.getQualifiedName()), record);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        writeNewline("{ \"type\": \"interface\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        write("\"");
        IExpressionNode bases[] = node.getExtendedInterfaceNodes();
        if (bases.length > 0)
        {
            writeNewline(",");
            writeNewline("\"baseInterfaceNames\": [");
            boolean firstBase = true;
            int n = bases.length;
	        for (int i = 0; i < n; i++)
	        {
	        	if (!firstBase)
	        		writeNewline(", ");
	        	firstBase = false;
	        	IDefinition baseDef = bases[i].resolve(getWalker().getProject());
	        	write("\"" + formatQualifiedName(baseDef.getQualifiedName()) + "\"");
	        }
        	writeNewline("]");
        }
        indentPush();
        if (asDoc != null)
        {
        	writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        if (members.length > 0)
        {
        	writeNewline(",");
        	writeNewline("\"members\": [");
        }
        firstMember = true;
        for (IDefinitionNode mnode : members)
        {
        	getWalker().walk(mnode);
        }
        if (members.length > 0)
        {
        	writeNewline("]");
        }
        indentPop();
        writeNewline("}");
        addToIndex(node.getDefinition(), asDoc);
    	RoyaleASDocProject project = (RoyaleASDocProject)getWalker().getProject();
    	RoyaleASDocProject.ASDocRecord record = project.new ASDocRecord();
    	record.definition = node.getDefinition();
    	if (asDoc != null)
    		record.description = makeShortDescription(asDoc.getDescription());
    	else
    		record.description = "";
        ((RoyaleASDocProject)getWalker().getProject()).classes.put(formatQualifiedName(node.getQualifiedName()), record);
    }

    private ArrayList<String> accessors = new ArrayList<String>();
    
    @Override
    public void emitGetAccessor(IGetterNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	String name = node.getName();
        if (accessors.contains(name)) return;
        accessors.add(name);
    	if (!firstMember)
    		writeNewline(",");
    	firstMember = false;
        writeNewline("{ \"type\": \"accessor\",");
    	IAccessorDefinition def = (IAccessorDefinition)node.getDefinition();
    	IAccessorDefinition otherDef = (IAccessorDefinition)def.resolveCorrespondingAccessor(getWalker().getProject());
    	IAccessorNode otherNode = null;
    	if (otherDef != null)
    	{
        	otherNode = (IAccessorNode)otherDef.getNode();
            writeNewline("  \"access\": \"read-write\",");
    	}
    	else
            writeNewline("  \"access\": \"read-only\",");
        write("  \"return\": \"");
        write(formatQualifiedName(node.getReturnTypeNode().resolveType(getWalker().getProject()).getQualifiedName()));
        writeNewline("\",");
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc == null || asDoc.commentNoEnd().contains("@private"))
        {
        	if (otherNode != null)
        		asDoc = (ASDocComment) otherNode.getASDocComment();        		
        }
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        writeDefinitionAttributes(def);
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        indentPop();
        write("}");
        addToIndex(node.getDefinition(), asDoc);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	String name = node.getName();
        if (accessors.contains(name)) return;
        accessors.add(name);
    	if (!firstMember)
    		writeNewline(",");
    	firstMember = false;
        writeNewline("{ \"type\": \"accessor\",");
    	IAccessorDefinition def = (IAccessorDefinition)node.getDefinition();
    	IAccessorDefinition otherDef = (IAccessorDefinition)def.resolveCorrespondingAccessor(getWalker().getProject());
    	IAccessorNode otherNode = null;
    	if (otherDef != null)
    	{
        	otherNode = (IAccessorNode)otherDef.getNode();
            writeNewline("  \"access\": \"read-write\",");
    	}
    	else
            writeNewline("  \"access\": \"write-only\",");
        write("  \"return\": \"");
        write(formatQualifiedName(node.getParameterNodes()[0].getVariableTypeNode().resolveType(getWalker().getProject()).getQualifiedName()));
        writeNewline("\",");
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc == null || asDoc.commentNoEnd().contains("@private"))
        {
        	if (otherNode != null)
        		asDoc = (ASDocComment) otherNode.getASDocComment();        		
        }
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        writeDefinitionAttributes(def);
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        indentPop();
        write("}");
        addToIndex(node.getDefinition(), asDoc);
    }
    
    @Override
    public void emitField(IVariableNode node)
    {
    	if (node.getDefinition().isPrivate()) return;
    	
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
    	if (!firstMember)
    		writeNewline(",");
    	firstMember = false;
        writeNewline("{ \"type\": \"field\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        write("  \"return\": \"");
        write(formatQualifiedName(node.getVariableTypeNode().resolveType(getWalker().getProject()).getQualifiedName()));
        writeNewline("\",");
        writeDefinitionAttributes(node.getDefinition());
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        indentPop();
        write("}");
        addToIndex(node.getDefinition(), asDoc);
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
    	if (!firstMember)
    		writeNewline(",");
    	firstMember = false;
        writeNewline("{ \"type\": \"variable\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        writeDefinitionAttributes(node.getDefinition());
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        indentPop();
        write("}");
        addToIndex(node.getDefinition(), asDoc);
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	String name = node.getName();
        if (accessors.contains(name)) return;
    	if (!firstMember)
    		writeNewline(",");
    	firstMember = false;
        
        accessors.add(name);
        writeNewline("{ \"type\": \"accessor\",");
    	IAccessorDefinition def = (IAccessorDefinition)node.getDefinition();
    	IAccessorDefinition otherDef = (IAccessorDefinition)def.resolveCorrespondingAccessor(getWalker().getProject());
    	IAccessorNode otherNode = null;
    	if (otherDef != null)
    	{
        	otherNode = (IAccessorNode)otherDef.getNode();
            writeNewline("  \"access\": \"read-write\",");
    	}
    	else
            writeNewline("  \"access\": \"read-only\",");
        write("  \"return\": \"");
        write(formatQualifiedName(node.isGetter() ? node.getReturnType() : node.getParameterNodes()[0].getQualifiedName()));
        writeNewline("\",");
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc == null || asDoc.commentNoEnd().contains("@private"))
        {
        	if (otherNode != null)
        		asDoc = (ASDocComment) otherNode.getASDocComment();        		
        }
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        writeDefinitionAttributes(def);
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        indentPop();
        write("}");
        addToIndex(node.getDefinition(), asDoc);
    }
    
    @Override
    public void emitMethod(IFunctionNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
    	if (!firstMember)
    		writeNewline(",");
    	firstMember = false;

        writeNewline("{ \"type\": \"method\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        writeDefinitionAttributes(node.getDefinition());
        indentPush();
        writeNewline(",");
        if (asDoc != null)
        {
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
            writeNewline(",");
        }
        write("  \"return\": \"");
        if (node.getReturnType().equals("void"))
        	write("void");
        else if (node.getReturnTypeNode() != null)
        	write(formatQualifiedName(node.getReturnTypeNode().resolveType(getWalker().getProject()).getQualifiedName()));
        writeNewline("\",");
        write("  \"params\": [");
        boolean firstParam = true;
    	IParameterDefinition params[] = node.getDefinition().getParameters();
    	for (IParameterDefinition param : params)
    	{
    		if (!firstParam)
    			writeNewline(",");
    		firstParam = false;
    		write("{ \"name\": \"");
    		write(param.getBaseName());
    		write("\", \"type\": \"");
    		if (param.getTypeReference() != null)
    			write(formatQualifiedName(param.getTypeReference().resolve(getWalker().getProject(), 
    					node.getContainingScope().getScope(), DependencyType.SIGNATURE, false).getQualifiedName()));
            write("\"}");    		
    	}
    	write("]");
        indentPop();
        writeNewline("}");
        addToIndex(node.getDefinition(), asDoc);
    }
    
    public void writeASDoc(ASDocComment asDoc, RoyaleASDocProject project)
    {
    	List<String> tagList = project.tags;
    	asDoc.compile();
        write("  \"description\": \"");
        String d = asDoc.getDescription();
		d = d.replace("\t", " ");
		d = d.replace("\\\"", "&quot;");
		d = d.replace("\\", "\\\\");
    	write(d);
		write("\"");
    	Map<String, List<IASDocTag>> tags = asDoc.getTags();
    	if (tags != null)
    	{
    		writeNewline(",");
    		writeNewline("\"tags\": [");
    		indentPush();
    		indentPush();
    		boolean firstTag = true;
    		Set<String> tagNames = tags.keySet();
    		for (String tagName : tagNames)
    		{
    			if (!firstTag)
    				writeNewline(",");
    			firstTag = false;
    			write("{  \"tagName\": \"");
                        tagName = tagName.trim();
    			write(tagName);
    			if (!tagList.contains(tagName))
    				tagList.add(tagName);
    			writeNewline("\",");
    			write("   \"values\": [");
        		indentPush();
        		indentPush();
    			List<IASDocTag> values = tags.get(tagName);
    			if (values != null)
    			{
    				boolean firstOne = true;
    				for (IASDocTag value : values)
    				{
    					if (!firstOne) write(", ");
    					firstOne = false;
    					write("\"");
    					d = value.getDescription().trim();
    					d = d.replace("\t", " ");
    					d = d.replace("\\\"", "&quot;");
    					d = d.replace("\\", "\\\\");
    					write(d);
    					write("\"");
    				}
    			}
    			write("]}");
        		indentPop();
        		indentPop();
    		}
    		write("  ]");
    		indentPop();
    		indentPop();
    	}
    }
           
    public void writeDefinitionAttributes(IDefinition def)
    {
        write("  \"namespace\": ");
        if (def.isProtected())
        	write("\"protected\"");
        else if (def.isInternal())
        	write("\"internal\"");
        else if (def.isPublic())
        	write("\"public\"");
        else 
        {
        	INamespaceReference nsRef = def.getNamespaceReference();
        	write("\"" + nsRef.getBaseName() + "\"");
        }
    	writeNewline(",");
        if (def.isBindable())
        {
        	List<String> events = def.getBindableEventNames();
            write("  \"bindable\": [");
            boolean firstEvent = true;
            for (String event : events)
            {
            	if (!firstEvent)
            		write(",");
            	firstEvent = false;
            	write("\"" + event + "\"");
            }
            write("]");
        }
        else
            write("  \"bindable\": []");
    	writeNewline(",");
        write("  \"details\": [");
        String sep = "";
        if (def.isOverride())
        {
            write(sep + "\"override\"");
            sep = ",";
        }
        if (def.isStatic())
        {
            write(sep + "\"static\"");
            sep = ",";
        }
        if (def.isDynamic())
        {
            write(sep + "\"dynamic\"");
            sep = ",";
        }
        if (def.isFinal())
        {
            write(sep + "\"final\"");
        }
        write("]");
    	writeNewline(",");
        if (def.isDeprecated())
        {
        	IDeprecationInfo dep = def.getDeprecationInfo();
            writeNewline("  \"deprecated\": {");
            indentPush();
            String comma = "";
            String msg = dep.getMessage();
            if (msg != null)
            {
	            write("  \"message\":  \"");
	            write(msg);
	            write("\"");
	            comma = ",";
            }
            String replace = dep.getReplacement();
            if (replace != null)
            {
            	writeNewline(comma);
            	write("  \"replacement\":  \"");
            	write(replace);
	            write("\"");
	            comma = ",";
            }
            String since = dep.getSince();
            if (since != null)
            {
	            writeNewline("\",");
	            write("  \"since\":  \"");
	            write(since);
	            write("\"");
	            comma = ",";
            }
            writeNewline("}");
        }
        else
        {
            write("  \"deprecated\": {}");
        }
    }
    
    public void writeEventTagNode(IMetaTagNode node)
    {
    	EventTagNode evt = (EventTagNode)node;
        ASDocComment asDoc = (ASDocComment) evt.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
    	if (!firstMember)
    		writeNewline(",");
    	firstMember = false;
        write("{ \"qname\": \"");
        write(formatQualifiedName(evt.getValue("name")));
        writeNewline("\",");
        write("  \"type\": \"");
        write(evt.getValue("type"));
        writeNewline("\"");
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc, (RoyaleASDocProject)getWalker().getProject());
        }
        indentPop();
        write("}");
        addToIndex(evt.getDefinition(), asDoc);
    }
    
    private void addToIndex(IDefinition def, ASDocComment asDoc)
    {
    	RoyaleASDocProject project = (RoyaleASDocProject)getWalker().getProject();
    	List<RoyaleASDocProject.ASDocRecord> list = project.index.get(def.getBaseName());
    	if (list == null)
    	{
    		list = new ArrayList<RoyaleASDocProject.ASDocRecord>();
    		project.index.put(def.getBaseName(), list);
    	}
    	RoyaleASDocProject.ASDocRecord record = project.new ASDocRecord();
    	record.definition = def;
    	if (asDoc != null)
    		record.description = makeShortDescription(asDoc.getDescription());
    	else
    		record.description = "";
    	list.add(record);
    }
    
    private String makeShortDescription(String description)
    {
    	int c = description.indexOf(".");
    	if (c != -1)
    		return description.substring(0, c + 1);
    	return description;
    }
    
    private String getMiddle(RoyaleASDocProject project)
    {
    	Map<String, String> defs = project.config.getCompilerDefine();
    	String swf = defs.get("COMPILE::SWF");
    	String middle = "";
    	if (swf != null)
    	{
        	if (swf.equals("true"))
        		middle = ".swf";
        	else
        		middle = ".js";
    	}
    	return middle;
    }

    public void outputIndex(File outputFolder, RoyaleASDocProject project) throws IOException
    {
		final File indexFile = new File(outputFolder, "index" + getMiddle(project) + ".json");
		BufferedWriter out = new BufferedWriter(new FileWriter(indexFile));
		out.write("{  \"index\": [");
		if (project.config.isVerbose())
		{
			System.out.println("Compiling file: " + indexFile);
		}
    	Set<String> keys = project.index.keySet();
    	List<String> keyList = new ArrayList<String>(keys);
    	Collections.sort(keyList);
    	boolean firstLine = true;
    	for (String key : keyList)
    	{
        	List<RoyaleASDocProject.ASDocRecord> list = project.index.get(key);
        	for (RoyaleASDocProject.ASDocRecord record : list)
        	{
        		if (!firstLine)
        			out.write(",\n");
        		firstLine = false;
	        	out.write("{ \"name\": \"");
	        	out.write(key);
	        	out.write("\",\n");
	        	out.write("  \"type\": ");
	        	if (record.definition instanceof ClassDefinition)
	        		out.write("\"Class\",\n");
	        	else if (record.definition instanceof InterfaceDefinition)
	        		out.write("\"Interface\",\n");
	        	else if (record.definition instanceof EventDefinition)
	        		out.write("\"Event\",\n");
	        	else if (record.definition instanceof AccessorDefinition)
	        	{
	        		out.write("\"Property\",\n");
	        		out.write("  \"class\": \"");
	        		out.write(formatQualifiedName(record.definition.getParent().getQualifiedName()));
	        		out.write("\",\n");
	        	}
	        	else if (record.definition instanceof VariableDefinition)
	        	{
	        		out.write("\"Property\",\n");
	        		out.write("  \"class\": \"");
	        		out.write(formatQualifiedName(record.definition.getParent().getQualifiedName()));
	        		out.write("\",\n");
	        	}
	        	else if (record.definition instanceof FunctionDefinition)
	        	{
	        		out.write("\"Method\",\n");
	        		out.write("  \"class\": \"");
	        		out.write(formatQualifiedName(record.definition.getParent().getQualifiedName()));
	        		out.write("\",\n");
	        	}
	        	out.write("  \"description\": \"");
	        	out.write(record.description);
	        	out.write("\"}");
        	}        	
    	}
		out.write("]}");
        try {
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public boolean hasCommentaryTags(ASDocComment asDoc)
    {
    	Map<String, List<IASDocTag>> tags = asDoc.getTags();
    	if (tags != null)
    	{
    		Set<String> tagNames = tags.keySet();
    		for (String tagName : tagNames)
    		{
    			if (tagName.equals("flexcomponent")) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    private void writeCommentaryValues(BufferedWriter commentaryWriter, List<IASDocTag> values) throws IOException
    {
    	HashMap<String, String> map = new HashMap<String, String>();
		boolean firstOne = true;
		for (IASDocTag value : values) {
		    String description = value.getDescription().trim();
			if (map.containsKey(description)) {
				continue;
			}
			
			if (!firstOne) commentaryWriter.write(", ");
			firstOne = false;
			commentaryWriter.write("\"");
			commentaryWriter.write(description);
			commentaryWriter.write("\"");
			
			map.put(description, "true");
		}
    }
    
    public void writeCommentaryFile(BufferedWriter commentaryWriter, String qname, ASDocComment asDoc) throws IOException
    {
    	//asDoc.compile();
    	Map<String, List<IASDocTag>> tags = asDoc.getTags();
    	
    	if (tags != null)
    	{
    		Set<String> tagNames = tags.keySet();
    		
    		commentaryWriter.write("{");
    		
    		commentaryWriter.write("\"className\": \"");
    		commentaryWriter.write(qname);
    		commentaryWriter.write("\", ");
    		
    		commentaryWriter.write("\"description\": \"");
    		commentaryWriter.write(asDoc.getDescription());
			commentaryWriter.write("\", ");
    		
    		boolean wroteCommentary = false;
    		boolean wroteExample = false;
    		boolean firstTag = true;
    		
    		for (String tagName : tagNames)
    		{
    			tagName = tagName.trim();
    			List<IASDocTag> values = tags.get(tagName);
    			
    			if (tagName.equals("flexcomponent")) {
					if (!firstTag) commentaryWriter.write(", ");
					firstTag = false;
    			    commentaryWriter.write("\"flexcomponent\": ");
    			    commentaryWriter.write("[");
    			    if (values != null) {
    			        writeCommentaryValues(commentaryWriter, values);
    			    }
    			    commentaryWriter.write("]");
    			}
    			else if (tagName.equals("flexdocurl")) {
    				if (!firstTag) commentaryWriter.write(", ");
    				firstTag = false;
    			    commentaryWriter.write("\"flexdocurl\": ");
    			    commentaryWriter.write("[");
    			    if (values != null) {
    			        writeCommentaryValues(commentaryWriter, values);
    			    }
    			    commentaryWriter.write("]");
    			}
    			else if (tagName.equals("commentary")) {
    				if (!firstTag) commentaryWriter.write(", ");
    				firstTag = false;
    			    commentaryWriter.write("\"commentary\": ");
    			    commentaryWriter.write("[");
    			    if (values != null) {
    			    	writeCommentaryValues(commentaryWriter, values);
    			    }
    			    commentaryWriter.write("]");
    			    wroteCommentary = true;
    			}
    			else if (tagName.equals("example")) {
    				if (!firstTag) commentaryWriter.write(", ");
    				firstTag = false;
    			    commentaryWriter.write("\"example\": ");
    			    commentaryWriter.write("[");
    			    if (values != null) {
    			    	writeCommentaryValues(commentaryWriter, values);
    			    }
    			    commentaryWriter.write("]");
    			    wroteExample = true;
    			}
    		}
    		
    		if (!wroteCommentary) {
    			commentaryWriter.write(", ");
    			commentaryWriter.write("\"commentary\": []");
    		}
    		
    		if (!wroteExample) {
    			commentaryWriter.write(", ");
    			commentaryWriter.write("\"example\": []");
    		}
    		
    		commentaryWriter.write("}");
    	}
    }

    public void outputClasses(File outputFolder, RoyaleASDocProject project) throws IOException
    {
		final File indexFile = new File(outputFolder, "classes" + getMiddle(project) + ".json");
		BufferedWriter out = new BufferedWriter(new FileWriter(indexFile));
        out.write("{  \"classes\": [");
		if (project.config.isVerbose())
		{
			System.out.println("Compiling file: " + indexFile);
		}
    	Set<String> keys = project.classes.keySet();
    	List<String> keyList = new ArrayList<String>(keys);
    	Collections.sort(keyList);
    	boolean firstLine = true;
    	
    	List<ASDocComment> commentaryList = new ArrayList<ASDocComment>();
    	List<String> qnameList = new ArrayList<String>();
    	    	
    	for (String key : keyList)
    	{
    		if (!firstLine)
    			out.write(",\n");
    		firstLine = false;
        	RoyaleASDocProject.ASDocRecord record = project.classes.get(key);
        	out.write("{ \"name\": \"");
        	out.write(key);
        	out.write("\",\n");
        	DefinitionBase def = (DefinitionBase)record.definition;
            ASDocComment asDoc = (ASDocComment) def.getExplicitSourceComment();
            if (asDoc != null)
            {
            	setBufferWrite(true);
            	StringBuilder sb = new StringBuilder();
            	setBuilder(sb);
            	writeASDoc(asDoc, project);
            	setBufferWrite(false);
            	out.write(sb.toString());
            	
            	if (hasCommentaryTags(asDoc)) {
            		qnameList.add(key);
            		commentaryList.add(asDoc);
            	}
            }
            else
            {
	        	out.write("  \"description\": \"");
	        	out.write(record.description);
	        	out.write("\"");
            }
        	out.write("}");
    	}
    	
		out.write("]}");
        try {
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    final File listFile = new File(outputFolder, "classlist" + getMiddle(project) + ".json");
	    out = new BufferedWriter(new FileWriter(listFile));
		out.write("{  \"classnames\": [");
	    if (project.config.isVerbose())
		{
			System.out.println("Compiling file: " + listFile);
		}
    	firstLine = true;
    	
    	for (String key : keyList)
    	{
    		if (!firstLine)
    			out.write(",\n");
    		firstLine = false;
        	out.write("\"");
        	out.write(key);
        	out.write("\"");
    	}
    	
		out.write("]}");
        try {
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (commentaryList.size() > 0) {    	
			final File commentaryFile = new File(outputFolder, "commentary" + getMiddle(project) + ".json");
			BufferedWriter commentaryWriter = new BufferedWriter(new FileWriter(commentaryFile));
			commentaryWriter.write("{ \"list\": [");
			if (project.config.isVerbose())
			{
				System.out.println("Building commentary comparison file: "+commentaryFile);
			}
			
			firstLine = true;
			int index = 0;
			
			for (ASDocComment asDoc : commentaryList) {
			    if (!firstLine) commentaryWriter.write(",\n");
			    firstLine = false;
			    String qname = qnameList.get(index);
			    writeCommentaryFile(commentaryWriter, qname, asDoc);
			    index++;
			}
		
			commentaryWriter.write("]}");
			try {
				commentaryWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				commentaryWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    public void outputTags(File outputFolder, RoyaleASDocProject project) throws IOException
    {
		final File indexFile = new File(outputFolder, "tags" + getMiddle(project) + ".json");
		BufferedWriter out = new BufferedWriter(new FileWriter(indexFile));
		out.write("{  \"tags\": [");
		if (project.config.isVerbose())
		{
			System.out.println("Compiling file: " + indexFile);
		}
    	Collections.sort(project.tags);
    	boolean firstLine = true;
    	for (String tag : project.tags)
    	{
    		if (!firstLine)
    			out.write(",\n");
    		firstLine = false;
        	out.write("\"");
        	out.write(tag);
        	out.write("\"");
    	}
		out.write("]}");
        try {
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
