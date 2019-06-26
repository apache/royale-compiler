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

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.asdoc.IASDocTag;
import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleASDocEmitter;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleEmitter;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyaleASDocEmitter;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
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
public class JSRoyaleASDocDITAEmitter extends JSGoogEmitter implements IJSRoyaleEmitter, IJSRoyaleASDocEmitter
{

    @Override
    public String postProcess(String output)
    {
    	return output;
    }

    public JSRoyaleASDocDITAEmitter(FilterWriter out)
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
        writeNewline("\",");
        indentPush();
        if (asDoc != null)
        	writeASDoc(asDoc);
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
    	int c = name.lastIndexOf(".");
    	if (c != -1)
    		name = name.substring(0, c) + ":" + name.substring(c + 1);
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
        IClassDefinition def = node.getDefinition();
        write("<apiClassifier id=\"");
        write(formatQualifiedName(node.getQualifiedName()));
        write("\"><apiName>");
        write(node.getName());
        write("</apiName>");
        
        String linkText = "";
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        String shortdesc = null;
        if (asDoc != null)
        {
        	asDoc.compile(false);
            shortdesc = makeShortDescription(asDoc);
            write(shortdesc);
            linkText = writeASDoc(asDoc);
        }
        else
        	write("<shortdesc/>");        
        
    	write("<apiClassifierDetail>");
    	write("<apiClassifierDef>");
    	if (def.isPublic())
        	write("<apiAccess value=\"public\"/>");
    	write("<apiStatic/>");
    	IExpressionNode interfaceNodes[] = node.getImplementedInterfaceNodes();
        for (IExpressionNode base : interfaceNodes)
        {
        	write("<apiBaseInterface>");
        	write(formatQualifiedName(base.resolve(getWalker().getProject()).getQualifiedName()));
        	write("</apiBaseInterface>");
        }
    	write("<apiBaseClassifier>");
    	IExpressionNode baseNode = node.getBaseClassExpressionNode();
    	if (baseNode == null)
    		write("Object");
    	else
    		write(formatQualifiedName(baseNode.resolve(getWalker().getProject()).getQualifiedName()));
    	write("</apiBaseClassifier>");
    	write("</apiClassifierDef>");
    	if (asDoc != null)
    	{
        	writeAPIDesc(asDoc);
    	}
    	write("</apiClassifierDetail>");
        write(linkText);

        final IDefinitionNode[] members = node.getAllMemberNodes();
        for (IDefinitionNode mnode : members)
        {
        	getWalker().walk(mnode);
        }
        IMetaTagNode[] metas = node.getMetaTagNodesByName("Event");
        for (IMetaTagNode mnode : metas)
        {
        	writeEventTagNode(mnode, def);
        }
        
        write("</apiClassifier>");
        addToIndex(node.getDefinition(), asDoc);
    	RoyaleASDocProject project = (RoyaleASDocProject)getWalker().getProject();
    	RoyaleASDocProject.ASDocRecord record = project.new ASDocRecord();
    	record.definition = node.getDefinition();
    	if (asDoc != null)
    		record.description = makeShortDescription(asDoc);
    	else
    		record.description = "";
        ((RoyaleASDocProject)getWalker().getProject()).classes.put(formatQualifiedName(node.getQualifiedName()), record);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        IInterfaceDefinition def = node.getDefinition();
        write("<apiClassifier id=\"");
        write(formatQualifiedName(node.getQualifiedName()));
        write("\"><apiName>");
        write(node.getName());
        write("</apiName>");
        
        String linkText = "";
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        String shortdesc = null;
        if (asDoc != null)
        {
        	asDoc.compile(false);
            shortdesc = makeShortDescription(asDoc);
            write(shortdesc);
            linkText = writeASDoc(asDoc);
        }
        else
        	write("<shortdesc/>");        
        
    	write("<apiClassifierDetail>");
    	write("<apiClassifierDef>");
    	write("<apiInterface/>");
    	if (def.isPublic())
        	write("<apiAccess value=\"public\"/>");
    	write("<apiStatic/>");
    	IExpressionNode baseNodes[] = node.getExtendedInterfaceNodes();
        for (IExpressionNode base : baseNodes)
        {
        	write("<apiBaseInterface>");
        	write(formatQualifiedName(base.resolve(getWalker().getProject()).getQualifiedName()));
        	write("</apiBaseInterface>");
        }
    	write("<apiBaseClassifier/>");
    	write("</apiClassifierDef>");
    	if (asDoc != null)
    	{
        	writeAPIDesc(asDoc);
    	}
    	write("</apiClassifierDetail>");

        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        for (IDefinitionNode mnode : members)
        {
        	getWalker().walk(mnode);
        }
        write(linkText);
        write("</apiClassifier>");
        addToIndex(node.getDefinition(), asDoc);
    	RoyaleASDocProject project = (RoyaleASDocProject)getWalker().getProject();
    	RoyaleASDocProject.ASDocRecord record = project.new ASDocRecord();
    	record.definition = node.getDefinition();
    	if (asDoc != null)
    		record.description = makeShortDescription(asDoc);
    	else
    		record.description = "";
        ((RoyaleASDocProject)getWalker().getProject()).classes.put(formatQualifiedName(node.getQualifiedName()), record);
    }

    private ArrayList<String> accessors = new ArrayList<String>();
    
    @Override
    public void emitGetAccessor(IGetterNode node)
    {
    	IDefinition def = node.getDefinition();
    	boolean isInterface = def.getParent() instanceof InterfaceDefinition;
    	if (node.getDefinition().isPrivate()) return;

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
        
    	String name = node.getName();
        if (accessors.contains(name)) return;
        accessors.add(name);
        write("<apiValue id=\"");
        if (isInterface)
        {
            write(formatQualifiedName(def.getParent().getQualifiedName()));
            write(":");        	
        }
        write(formatQualifiedName(def.getParent().getQualifiedName()));
        write(":");
        write(formatQualifiedName(node.getQualifiedName()));
        write(":get\"><apiName>");
        write(node.getName());
        write("</apiName>");
        String shortdesc = null;
        String linkText = "";
        if (asDoc != null)
        {
        	asDoc.compile(false);
            shortdesc = makeShortDescription(asDoc);
            write(shortdesc);
            linkText = writeASDoc(asDoc);
        }
        else
        	write("<shortdesc/>");        
        
    	write("<apiValueDetail>");
    	write("<apiValueDef>");
    	write("<apiProperty/>");
    	if (def.isPublic() || isInterface)
        	write("<apiAccess value=\"public\"/>");
    	else if (def.isProtected())
        	write("<apiAccess value=\"protected\"/>");
    	if (def.isStatic())
        	write("<apiStatic/>");
    	write("<apiDynamic/>");
    	write("<apiValueAccess value=\"read\"/>");
    	write("<apiType value=\"");
    	write(formatQualifiedName(def.resolveType(getWalker().getProject()).getQualifiedName()));
    	write("\"/>");
    	write("</apiValueDef>");
    	if (asDoc != null)
    	{
        	writeAPIDesc(asDoc);
    	}
    	write("</apiValueDetail>");
    	write(linkText);
    	write("</apiValue>");        	
        addToIndex(node.getDefinition(), asDoc);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
    	IDefinition def = node.getDefinition();
    	boolean isInterface = def.getParent() instanceof InterfaceDefinition;
    	if (node.getDefinition().isPrivate()) return;

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
        
    	String name = node.getName();
        if (accessors.contains(name)) return;
        accessors.add(name);
        write("<apiValue id=\"");
        if (isInterface)
        {
            write(formatQualifiedName(def.getParent().getQualifiedName()));
            write(":");        	
        }
        write(formatQualifiedName(def.getParent().getQualifiedName()));
        write(":");
        write(formatQualifiedName(node.getQualifiedName()));
        write(":set\"><apiName>");
        write(node.getName());
        write("</apiName>");
        String shortdesc = null;
        String linkText = "";
        if (asDoc != null)
        {
        	asDoc.compile(false);
            shortdesc = makeShortDescription(asDoc);
            write(shortdesc);
            linkText = writeASDoc(asDoc);
        }
        else
        	write("<shortdesc/>");        
        
    	write("<apiValueDetail>");
    	write("<apiValueDef>");
    	writeAPIProperties(def);
    	if (def.isPublic() || def.getParent() instanceof InterfaceDefinition)
        	write("<apiAccess value=\"public\"/>");
    	else if (def.isProtected())
        	write("<apiAccess value=\"protected\"/>");
    	if (def.isStatic())
        	write("<apiStatic/>");
    	write("<apiDynamic/>");
    	write("<apiValueAccess value=\"write\"/>");
    	write("<apiValueClassifier>");
    	write(formatQualifiedName(def.resolveType(getWalker().getProject()).getQualifiedName()));
    	write("</apiValueClassifier>");
    	write("</apiValueDef>");
    	if (asDoc != null)
    	{
    		writeAPIDesc(asDoc);
    	}
    	write("</apiValueDetail>");
    	write(linkText);
    	write("</apiValue>");        	

        addToIndex(node.getDefinition(), asDoc);
    }
    
    @Override
    public void emitField(IVariableNode node)
    {
    	IDefinition def = node.getDefinition();
    	if (node.getDefinition().isPrivate()) return;
    	
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
        write("<apiValue id=\"");
        write(formatQualifiedName(def.getParent().getQualifiedName()));
        write(":");
        write(formatQualifiedName(node.getQualifiedName()));
        write("\"><apiName>");
        write(node.getName());
        write("</apiName>");
        String shortdesc = null;
        String linkText = "";
        if (asDoc != null)
        {
        	asDoc.compile(false);
            shortdesc = makeShortDescription(asDoc);
            write(shortdesc);
            linkText = writeASDoc(asDoc);
        }
        else
        	write("<shortdesc/>");        
        
    	write("<apiValueDetail>");
    	write("<apiValueDef>");
    	writeAPIProperties(def);
    	if (def.isPublic() || def.getParent() instanceof InterfaceDefinition)
        	write("<apiAccess value=\"public\"/>");
    	else if (def.isProtected())
        	write("<apiAccess value=\"protected\"/>");
    	if (def.isStatic())
        	write("<apiStatic/>");
    	write("<apiDynamic/>");
    	write("<apiValueClassifier>");
    	write(formatQualifiedName(def.resolveType(getWalker().getProject()).getQualifiedName()));
    	write("</apiValueClassifier>");
    	write("</apiValueDef>");
    	if (asDoc != null)
    	{
    		writeAPIDesc(asDoc);
    	}
    	write("</apiValueDetail>");
    	write(linkText);
    	write("</apiValue>"); 
        addToIndex(node.getDefinition(), asDoc);
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
    	IDefinition def = node.getDefinition();
    	if (node.getDefinition().isPrivate()) return;

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
        write("<apiValue id=\"");
        write(formatQualifiedName(node.getQualifiedName()));
        write("\"><apiName>");
        write(node.getName());
        write("</apiName>");
        String shortdesc = null;
        if (asDoc != null)
        {
        	asDoc.compile(false);
            shortdesc = makeShortDescription(asDoc);
            write(shortdesc);
            writeASDoc(asDoc);
        }
        else
        	write("<shortdesc/>");        
        
    	write("<apiValueDetail>");
    	write("<apiValueDef>");
    	writeAPIProperties(def);
    	if (def.isPublic() || def.getParent() instanceof InterfaceDefinition)
        	write("<apiAccess value=\"public\"/>");
    	else if (def.isProtected())
        	write("<apiAccess value=\"protected\"/>");
    	if (def.isStatic())
        	write("<apiStatic/>");
    	write("<apiDynamic/>");
    	write("<apiType value=\"");
    	write(formatQualifiedName(def.resolveType(getWalker().getProject()).getQualifiedName()));
    	write("\"/>");
    	write("</apiValueDef>");
    	if (asDoc != null)
    	{
        	writeAPIDesc(asDoc);
    	}
    	write("</apiValueDetail>");
    	write("</apiValue>"); 
        addToIndex(node.getDefinition(), asDoc);
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
    	if (node.getDefinition().isPrivate()) return;

    	String name = node.getName();
        if (accessors.contains(name)) return;
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
        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc == null || asDoc.commentNoEnd().contains("@private"))
        {
        	if (otherNode != null)
        		asDoc = (ASDocComment) otherNode.getASDocComment();        		
        }
        write("<apiValue id=\"");
        write(formatQualifiedName(node.getQualifiedName()));
        write(":set\"><apiName>");
        write(node.getName());
        write("</apiName>");
        String shortdesc = null;
        if (asDoc != null)
        {
        	asDoc.compile(false);
            shortdesc = makeShortDescription(asDoc);
            write(shortdesc);
            writeASDoc(asDoc);
        }
        else
        	write("<shortdesc/>");        
        
    	write("<apiValueDetail>");
    	write("<apiValueDef>");
    	writeAPIProperties(def);
    	if (def.isPublic() || def.getParent() instanceof InterfaceDefinition)
        	write("<apiAccess value=\"public\"/>");
    	else if (def.isProtected())
        	write("<apiAccess value=\"protected\"/>");
    	if (def.isStatic())
        	write("<apiStatic/>");
    	write("<apiDynamic/>");
    	if (otherDef != null)
    	{
        	otherNode = (IAccessorNode)otherDef.getNode();
        	write("<apiValueAccess value=\"read-write\"/>");
    	}
    	else
            writeNewline("  \"access\": \"read\",");
    	write("<apiType value=\"");
    	write(formatQualifiedName(def.resolveType(getWalker().getProject()).getQualifiedName()));
    	write("\"/>");
    	write("</apiValueDef>");
    	if (asDoc != null)
    	{
        	writeAPIDesc(asDoc);
    	}
    	write("</apiValueDetail>");
    	write("</apiValue>");     
        addToIndex(node.getDefinition(), asDoc);
    }
    
    @Override
    public void emitMethod(IFunctionNode node)
    {
    	IFunctionDefinition def = node.getDefinition();
    	if (def.isPrivate()) return;

        ASDocComment asDoc = (ASDocComment) node.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
        
        if (node.isConstructor())
        {
            write("<apiConstructor id=\"");
            write(formatQualifiedName(node.getQualifiedName()));
            write(":");
            write(node.getName());
            write("\"><apiName>");
            write(node.getName());
            write("</apiName>");
            String shortdesc = null;
            if (asDoc != null)
            {
            	asDoc.compile(false);
                shortdesc = makeShortDescription(asDoc);
                write(shortdesc);
                writeASDoc(asDoc);
            }
            else
            	write("<shortdesc/>");        
            
        	write("<apiConstructorDetail>");
        	write("<apiConstructorDef>");
            write("<apiAccess value=\"public\"/>");
        	write("</apiConstructorDef>");
        	if (asDoc != null)
        	{
            	writeAPIDesc(asDoc);
        	}
        	write("</apiConstructorDetail>");
        	write("</apiConstructor>");
        }
        else
        {
            write("<apiOperation id=\"");
            write(formatQualifiedName(def.getParent().getQualifiedName()));
            write(":");
            write(formatQualifiedName(node.getQualifiedName()));
            write("\"><apiName>");
            write(node.getName());
            write("</apiName>");
            String shortdesc = null;
            if (asDoc != null)
            {
            	asDoc.compile(false);
                shortdesc = makeShortDescription(asDoc);
                write(shortdesc);
                writeASDoc(asDoc);
            }
            else
            	write("<shortdesc/>");        
            
        	write("<apiOperationDetail>");
        	write("<apiOperationDef>");
        	if (def.isPublic() || def.getParent() instanceof InterfaceDefinition)
            	write("<apiAccess value=\"public\"/>");
        	else if (def.isProtected())
            	write("<apiAccess value=\"protected\"/>");
        	if (def.isStatic())
            	write("<apiStatic/>");
        	write("<apiReturn><apiType value=\"");
        	write(def.getReturnTypeAsDisplayString());
        	write("\"/></apiReturn>");
        	IParameterDefinition params[] = def.getParameters();
        	write("<apiParam>");
        	for (IParameterDefinition param : params)
        	{
        		write("<apiItemName>");
        		write(param.getBaseName());
        		write("</apiItemName><apiType value=\"");
        		write(param.getTypeAsDisplayString());
        		write("\">");
        		if (param.hasDefaultValue())
        		{
        			write("<apiData>");
        			write(param.getNode().getDefaultValue());
        			write("</apiData>");
        		}
        		write("</apiParam>");
        	}
        	write("</apiOperationDef>");
        	if (asDoc != null)
        	{
            	writeAPIDesc(asDoc);
        	}
        	write("</apiOperationDetail>");
        	write("</apiOperation>");        	
        }
        addToIndex(node.getDefinition(), asDoc);
    }
    
    public String writeASDoc(ASDocComment asDoc)
    {
    	StringBuilder linkText = new StringBuilder();
    	RoyaleASDocProject project = (RoyaleASDocProject)getWalker().getProject();
    	List<String> tagList = project.tags;
    	Map<String, List<IASDocTag>> tags = asDoc.getTags();
    	if (tags != null)
    	{
    		write("<prolog><asMetadata><apiVersion>");
			List<IASDocTag> values = tags.get("langversion");
			if (values != null)
			{
				for (IASDocTag value : values)
				{
					write("<apiLanguage version=\"");
					write(value.getDescription());
					write("\"/>");
				}
			}
			values = tags.get("playerversion");
			if (values != null)
			{
				for (IASDocTag value : values)
				{
					write("<apiPlatform description=\"\" name=\"");
					String desc = value.getDescription();
					String parts[] = desc.split(" ");
					write(parts[0]);
					write("\" version=\"");
					write(parts[1]);
					write("\"/>");
				}
			}
			values = tags.get("productversion");
			if (values != null)
			{
				for (IASDocTag value : values)
				{
					write("<apiTool description=\"\" name=\"");
					String desc = value.getDescription();
					String parts[] = desc.split(" ");
					write(parts[0]);
					write("\" version=\"");
					write(parts[1]);
					write("\"/>");
				}
			}
			values = tags.get("see");
			if (values != null)
			{
				linkText.append("<related-links>");
				for (IASDocTag value : values)
				{
					linkText.append("<link href=\"");
					String desc = value.getDescription();
					String parts[] = desc.split("#");
					String fileName = parts[0];
					int c = fileName.lastIndexOf(".");
					if (c == -1)
					{
						c = fileName.length();
					}
					linkText.append(fileName.substring(0, c));
					if (parts.length == 1)
					{
						linkText.append(".xml");
					}
					else
					{
						linkText.append(".xml#");
						linkText.append(fileName.substring(c + 1));
						linkText.append("/");
						linkText.append(parts[1]);						
					}
					linkText.append("\"/>");
					linkText.append("<linktext>");
					linkText.append(parts[0]);
					if (parts.length > 1)
					{
						linkText.append(".");
						linkText.append(parts[1]);
					}
					linkText.append("</linktext>");
					linkText.append("</link>");
				}
				linkText.append("</related-links>");
			}
    		write("</apiVersion></asMetadata>");
    		boolean needHeader = true;
    		boolean needFooter = false;
    		Set<String> tagNames = tags.keySet();
    		for (String tagName : tagNames)
    		{
    			if (!tagList.contains(tagName))
    				tagList.add(tagName);
    			if (!(tagName.equals("see") ||
      				  tagName.equals("copy") ||
    				  tagName.equals("productversion") ||
    				  tagName.equals("langversion") ||
    				  tagName.equals("playerversion")))
    			{
    				if (needHeader)
    				{
    					write("<asCustoms>");
    					needFooter = true;
    				}
    				values = tags.get(tagName);
    				if (values != null)
    				{
	    				write("<" + tagName + ">");
	    				for (IASDocTag value : values)
	    				{
	    					write(value.getDescription());
	    				}
	    				write("</" + tagName + ">");
    				}
    				else
	    				write("<" + tagName + "/>");
    			}
    		}
    		if (needFooter)
				write("</asCustoms>");
    		write("</prolog>");
    	}
    	else
    		write("<prolog/>");
    	return linkText.toString();
    }
    
    public void writeEventTagNode(IMetaTagNode node, IClassDefinition classDef)
    {
    	EventTagNode evt = (EventTagNode)node;
        ASDocComment asDoc = (ASDocComment) evt.getASDocComment();
        if (asDoc != null && asDoc.commentNoEnd().contains("@private"))
        	return;
        write("<adobeApiEvent id=\"");
        write(formatQualifiedName(classDef.getQualifiedName()));
        write("_");
        write(evt.getValue("type"));
        write("_");
        write(evt.getValue("name"));
        writeNewline("\">");
        write("<apiName>");
        write(evt.getValue("name"));
        write("</apiName>");
        if (asDoc != null)
        {
        	asDoc.compile(false);
        	write(makeShortDescription(asDoc));
        	writeASDoc(asDoc);
        }
    	write("<adobeApiEventDetail>");
    	write("<adobeApiEventDef>");
    	write("<adobeApiEventClassifier>");
        write(evt.getValue("type"));
    	write("</adobeApiEventClassifier>");
    	write("<apiGeneratedEvent>");
    	write("</adobeApiEventDef>");
    	if (asDoc != null)
    	{
        	writeAPIDesc(asDoc);
    	}
    	write("</adobeApiEventDetail>");
    	write("</adobeApiEvent>");
        	
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
    		record.description = makeShortDescription(asDoc);
    	else
    		record.description = "";
    	list.add(record);
    }
    
    private String makeShortDescription(ASDocComment asDoc)
    {
    	String description = asDoc.getDescription();
    	if (description == null || description.length() == 0)
    		return "<shortdesc/>";
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("<shortdesc");
    	IASDocTag copyTag = asDoc.getTag("copy");
    	if (copyTag != null)
    	{
    		sb.append(" conref=\"");
    		sb.append(copyTag.getDescription());
    		sb.append("\">\n");
    	}
    	else
    	{
    		sb.append(">");
        	int c = description.indexOf(".");
        	if (c != -1)
        		sb.append(description.substring(0, c + 1));
        	else
        		sb.append(description);
    	}
    	sb.append("</shortdesc>");    		
    	return sb.toString();
    }
    
    private void writeAPIDesc(ASDocComment asDoc)
    {
       	write("<apiDesc");
    	IASDocTag copyTag = asDoc.getTag("copy");
    	if (copyTag != null)
    	{
    		write(" conref=\"");
    		write(copyTag.getDescription());
    		write("\">\n");
    	}
    	else
    	{
    		write(">");
        	write(asDoc.getDescription());
    	}
    	write("</apiDesc>");

    }

    public void writeAPIProperties(IDefinition def)
    {
    	IMetaTag propTag = def.getMetaTagByName("Bindable");
    	if (propTag == null)
    	{
        	write("<apiProperty/>");
        	return;
    	}
    	write("<apiProperty isBindable=\"true\"");
    	String value = propTag.getValue();
    	if (value != null)
    	{
    		write(" name=\"");
    		write(value);
    		write("\"");
    	}
    	write("/>");
    }
    
    public void outputIndex(File outputFolder, RoyaleASDocProject project) throws IOException
    {
		final File indexFile = new File(outputFolder, "index.json");
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

    public void outputClasses(File outputFolder, RoyaleASDocProject project) throws IOException
    {
		final File indexFile = new File(outputFolder, "classes.json");
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
    	for (String key : keyList)
    	{
    		if (!firstLine)
    			out.write(",\n");
    		firstLine = false;
        	RoyaleASDocProject.ASDocRecord record = project.classes.get(key);
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
    
    public void outputTags(File outputFolder, RoyaleASDocProject project) throws IOException
    {
		final File indexFile = new File(outputFolder, "tags.json");
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
