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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.flex.compiler.asdoc.IASDocTag;
import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSASDocEmitter;
import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.IParameterDefinition;
import org.apache.flex.compiler.definitions.metadata.IDeprecationInfo;
import org.apache.flex.compiler.definitions.references.INamespaceReference;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSASDocEmitter;
import org.apache.flex.compiler.internal.definitions.AccessorDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.EventDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.definitions.VariableDefinition;
import org.apache.flex.compiler.internal.projects.FlexJSASDocProject;
import org.apache.flex.compiler.internal.tree.as.metadata.EventTagNode;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.INamespaceNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagNode;
import org.apache.flex.compiler.utils.NativeUtils;

/**
 * Concrete implementation of the 'FlexJS' JavaScript production.
 *
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSFlexJSASDocEmitter extends JSGoogEmitter implements IJSFlexJSEmitter, IJSFlexJSASDocEmitter
{

	private boolean firstMember = true;
	
    @Override
    public String postProcess(String output)
    {
    	return output;
    }

    public JSFlexJSASDocEmitter(FilterWriter out)
    {
        super(out);
    }

    @Override
    protected void writeIndent()
    {
        write(JSFlexJSEmitterTokens.INDENT);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
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
        	writeASDoc(asDoc);
        }
        indentPop();
        writeNewline("}");
    }


    @Override
    public String formatQualifiedName(String name)
    {
        return formatQualifiedName(name, false);
    }

    public MXMLFlexJSASDocEmitter mxmlEmitter = null;

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
	        	getWalker().walk(pnode);
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
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        writeNewline("{ \"type\": \"class\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        indentPush();
        write("  \"baseClassname\": \"");
        write(formatQualifiedName(node.getBaseClassName()));
        writeNewline("\"");
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc);
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
    	FlexJSASDocProject project = (FlexJSASDocProject)getWalker().getProject();
    	FlexJSASDocProject.ASDocRecord record = project.new ASDocRecord();
    	record.definition = node.getDefinition();
    	if (asDoc != null)
    		record.description = makeShortDescription(asDoc.getDescription());
    	else
    		record.description = "";
        ((FlexJSASDocProject)getWalker().getProject()).classes.put(formatQualifiedName(node.getQualifiedName()), record);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        writeNewline("{ \"type\": \"interface\",");
        write("  \"qname\": \"");
        write(formatQualifiedName(node.getQualifiedName()));
        writeNewline("\",");
        String bases[] = node.getExtendedInterfaces();
        if (bases.length > 0)
        {
        	writeNewline(",");
        	writeNewline("extends: [");
            boolean firstBase = true;
	        for (String base : bases)
	        {
	        	if (!firstBase)
	        		writeNewline(", ");
	        	firstBase = false;
	        	write("\"" + base + "\"");
	        	write(base);
	        }
        	writeNewline("]");
        }
        indentPush();
        if (asDoc != null)
        	writeASDoc(asDoc);
        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        if (members.length > 0)
        {
        	writeNewline(",");
        	writeNewline("members: [");
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
    	FlexJSASDocProject project = (FlexJSASDocProject)getWalker().getProject();
    	FlexJSASDocProject.ASDocRecord record = project.new ASDocRecord();
    	record.definition = node.getDefinition();
    	if (asDoc != null)
    		record.description = makeShortDescription(asDoc.getDescription());
    	else
    		record.description = "";
        ((FlexJSASDocProject)getWalker().getProject()).classes.put(formatQualifiedName(node.getQualifiedName()), record);
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
        write(formatQualifiedName(node.getReturnType()));
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
        	writeASDoc(asDoc);
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
        write(formatQualifiedName(node.getParameterNodes()[0].getQualifiedName()));
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
        	writeASDoc(asDoc);
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
        writeDefinitionAttributes(node.getDefinition());
        indentPush();
        if (asDoc != null)
        {
            writeNewline(",");
        	writeASDoc(asDoc);
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
        	writeASDoc(asDoc);
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
        	writeASDoc(asDoc);
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
        	writeASDoc(asDoc);
            writeNewline(",");
        }
        write("  \"return\": \"");
        write(formatQualifiedName(node.getReturnType()));
        writeNewline("\",");
        write("  \"params\": [");
        boolean firstParam = true;
    	IParameterDefinition params[] = node.getDefinition().getParameters();
    	for (IParameterDefinition param : params)
    	{
    		if (!firstParam)
    			writeNewline(",");
    		write("{ \"name\": \"");
    		write(param.getBaseName());
    		write("\", \"type\": \"");
            write(formatQualifiedName(param.getTypeAsDisplayString()));
            writeNewline("\"}");    		
    	}
    	write("]");
        indentPop();
        write("}");
        addToIndex(node.getDefinition(), asDoc);
    }
    
    public void writeASDoc(ASDocComment asDoc)
    {
    	FlexJSASDocProject project = (FlexJSASDocProject)getWalker().getProject();
    	List<String> tagList = project.tags;
    	asDoc.compile();
        write("  \"description\": \"");
    	write(asDoc.getDescription());
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
    					write(value.getDescription());
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
        if (def.isBindable())
        {
        	writeNewline(",");
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
        if (def.isOverride())
        {
        	writeNewline(",");
            write("  \"override\": true");
        }
        if (def.isStatic())
        {
        	writeNewline(",");
            write("  \"static\": true");
        }
        if (def.isDynamic())
        {
        	writeNewline(",");
            write("  \"dynamic\": true");
        }
        if (def.isFinal())
        {
        	writeNewline(",");
            write("  \"final\": true");
        }
        if (def.isDeprecated())
        {
        	writeNewline(",");
        	IDeprecationInfo dep = def.getDeprecationInfo();
            writeNewline("  \"deprecated\": {");
            indentPush();
            write("  \"message\":  \"");
            write(dep.getMessage());
            writeNewline("\",");
            write("  \"replacement\":  \"");
            write(dep.getReplacement());
            writeNewline("\",");
            write("  \"since\":  \"");
            write(dep.getSince());
            writeNewline("\"}");
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
        	writeASDoc(asDoc);
        }
        indentPop();
        write("}");
        addToIndex(evt.getDefinition(), asDoc);
    }
    
    private void addToIndex(IDefinition def, ASDocComment asDoc)
    {
    	FlexJSASDocProject project = (FlexJSASDocProject)getWalker().getProject();
    	List<FlexJSASDocProject.ASDocRecord> list = project.index.get(def.getBaseName());
    	if (list == null)
    	{
    		list = new ArrayList<FlexJSASDocProject.ASDocRecord>();
    		project.index.put(def.getBaseName(), list);
    	}
    	FlexJSASDocProject.ASDocRecord record = project.new ASDocRecord();
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

    public void outputIndex(File outputFolder, FlexJSASDocProject project) throws IOException
    {
	    final File indexFile = new File(outputFolder, "index.json");
	    FileWriter out = new FileWriter(indexFile);
		out.write("{  \"index\": [");
	    System.out.println("Compiling file: " + indexFile);
    	Set<String> keys = project.index.keySet();
    	List<String> keyList = new ArrayList<String>(keys);
    	Collections.sort(keyList);
    	boolean firstLine = true;
    	for (String key : keyList)
    	{
        	List<FlexJSASDocProject.ASDocRecord> list = project.index.get(key);
        	for (FlexJSASDocProject.ASDocRecord record : list)
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

    public void outputClasses(File outputFolder, FlexJSASDocProject project) throws IOException
    {
	    final File indexFile = new File(outputFolder, "classes.json");
	    FileWriter out = new FileWriter(indexFile);
		out.write("{  \"classes\": [");
	    System.out.println("Compiling file: " + indexFile);
    	Set<String> keys = project.classes.keySet();
    	List<String> keyList = new ArrayList<String>(keys);
    	Collections.sort(keyList);
    	boolean firstLine = true;
    	for (String key : keyList)
    	{
    		if (!firstLine)
    			out.write(",\n");
    		firstLine = false;
        	FlexJSASDocProject.ASDocRecord record = project.classes.get(key);
        	out.write("{ \"name\": \"");
        	out.write(key);
        	out.write("\",\n");
        	out.write("  \"description\": \"");
        	out.write(record.description);
        	out.write("\"}");
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
    
    public void outputTags(File outputFolder, FlexJSASDocProject project) throws IOException
    {
	    final File indexFile = new File(outputFolder, "tags.json");
	    FileWriter out = new FileWriter(indexFile);
		out.write("{  \"tags\": [");
	    System.out.println("Compiling file: " + indexFile);
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
