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

package org.apache.flex.compiler.internal.codegen.js.jx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSDocEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.SetterNode;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagsNode;

public class PackageFooterEmitter extends JSSubEmitter implements
        ISubEmitter<IPackageDefinition>
{

    public PackageFooterEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IPackageDefinition definition)
    {
        IASScope containedScope = definition.getContainedScope();
        ITypeDefinition type = EmitterUtils.findType(containedScope
                .getAllLocalDefinitions());
        if (type == null)
            return;

        getEmitter().emitSourceMapDirective(type.getNode());
    }

    public void emitClassInfo(ITypeNode tnode)
    {
        JSFlexJSDocEmitter doc = (JSFlexJSDocEmitter) getEmitter()
        .getDocEmitter();

	    /*
	     * Metadata
	     * 
	     * @type {Object.<string, Array.<Object>>}
	     */
	    writeNewline();
	    writeNewline();
	    writeNewline();
	    doc.begin();
	    writeNewline(" * Metadata");
	    writeNewline(" *");
	    writeNewline(" * @type {Object.<string, Array.<Object>>}");
	    doc.end();
	
	    // a.B.prototype.AFJS_CLASS_INFO = {  };
	    write(getEmitter().formatQualifiedName(tnode.getQualifiedName()));
	    write(ASEmitterTokens.MEMBER_ACCESS);
	    write(JSEmitterTokens.PROTOTYPE);
	    write(ASEmitterTokens.MEMBER_ACCESS);
	    writeToken(JSFlexJSEmitterTokens.FLEXJS_CLASS_INFO);
	    writeToken(ASEmitterTokens.EQUAL);
	    writeToken(ASEmitterTokens.BLOCK_OPEN);
	
	    // names: [{ name: '', qName: '' }]
	    write(JSFlexJSEmitterTokens.NAMES);
	    writeToken(ASEmitterTokens.COLON);
	    write(ASEmitterTokens.SQUARE_OPEN);
	    writeToken(ASEmitterTokens.BLOCK_OPEN);
	    write(JSFlexJSEmitterTokens.NAME);
	    writeToken(ASEmitterTokens.COLON);
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(tnode.getName());
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    writeToken(ASEmitterTokens.COMMA);
	    write(JSFlexJSEmitterTokens.QNAME);
	    writeToken(ASEmitterTokens.COLON);
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(getEmitter().formatQualifiedName(tnode.getQualifiedName()));
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    write(ASEmitterTokens.SQUARE_CLOSE);
	
	    IExpressionNode[] enodes;
	    if (tnode instanceof IClassNode)
	        enodes = ((IClassNode) tnode).getImplementedInterfaceNodes();
	    else
	        enodes = ((IInterfaceNode) tnode).getExtendedInterfaceNodes();
	
	    if (enodes.length > 0)
	    {
	        writeToken(ASEmitterTokens.COMMA);
	
	        // interfaces: [a.IC, a.ID]
	        write(JSFlexJSEmitterTokens.INTERFACES);
	        writeToken(ASEmitterTokens.COLON);
	        write(ASEmitterTokens.SQUARE_OPEN);
	        int i = 0;
	        for (IExpressionNode enode : enodes)
	        {
	        	IDefinition edef = enode.resolve(getProject());
	        	if (edef == null)
	        		continue;
	            write(getEmitter().formatQualifiedName(
	                    edef.getQualifiedName()));
	            if (i < enodes.length - 1)
	                writeToken(ASEmitterTokens.COMMA);
	            i++;
	        }
	        write(ASEmitterTokens.SQUARE_CLOSE);
	    }
	    write(ASEmitterTokens.SPACE);
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    write(ASEmitterTokens.SEMICOLON);

        if (!(tnode instanceof IInterfaceNode))
        {
		    writeNewline();
		    writeNewline();
		    writeNewline();
		    doc.begin();
		    writeNewline(" * Prevent renaming of class. Needed for reflection.");
		    doc.end();
		    write(JSFlexJSEmitterTokens.GOOG_EXPORT_SYMBOL);
		    write(ASEmitterTokens.PAREN_OPEN);
		    write(ASEmitterTokens.SINGLE_QUOTE);
		    write(getEmitter().formatQualifiedName(tnode.getQualifiedName()));
		    write(ASEmitterTokens.SINGLE_QUOTE);
		    write(ASEmitterTokens.COMMA);
		    write(ASEmitterTokens.SPACE);
		    write(getEmitter().formatQualifiedName(tnode.getQualifiedName()));
		    write(ASEmitterTokens.PAREN_CLOSE);
		    write(ASEmitterTokens.SEMICOLON);
        }

	    collectReflectionData(tnode);
	    IMetaTagNode[] metadata = null;
	    IMetaTagsNode metadataTags = tnode.getMetaTags();
	    if (metadataTags != null)
	    	metadata = metadataTags.getAllTags();
	    emitReflectionData(getEmitter().formatQualifiedName(tnode.getQualifiedName()),
	    		varData,
	    		accessorData,
	    		methodData,
	    		metadata);
    }
    
    public class VariableData
    {
    	public String name;
    	public String type;
    	public IMetaTagNode[] metaData;
    }
    
    private ArrayList<VariableData> varData;
    
    public class MethodData
    {
    	public String name;
    	public String type;
    	public String declaredBy;
    	public IMetaTagNode[] metaData;
    }
    
    private ArrayList<MethodData> accessorData;
    private ArrayList<MethodData> methodData;
    
    public void collectReflectionData(ITypeNode tnode)
    {
    	varData = new ArrayList<VariableData>();
    	accessorData = new ArrayList<MethodData>();
    	methodData = new ArrayList<MethodData>();
    	/*
	     * Reflection
	     * 
	     * @return {Object.<string, Function>}
	     */
        IDefinitionNode[] dnodes;
        boolean isInterface = tnode instanceof IInterfaceNode;
	    if (!isInterface)
	        dnodes = ((IClassNode) tnode).getAllMemberNodes();
	    else
	        dnodes = ((IInterfaceNode) tnode).getAllMemberDefinitionNodes();
	    
        for (IDefinitionNode dnode : dnodes)
        {
            ModifiersSet modifierSet = dnode.getDefinition().getModifiers();
            boolean isStatic = (modifierSet != null && modifierSet
                    .hasModifier(ASModifier.STATIC));
            if (!isStatic && (dnode.getNodeID() == ASTNodeID.VariableID ||
            		dnode.getNodeID() == ASTNodeID.BindableVariableID))
            {
            	IVariableNode varNode = (IVariableNode)dnode;
                String ns = varNode.getNamespace();
                if (ns == IASKeywordConstants.PUBLIC || isInterface)
                {
                	VariableData data = new VariableData();
                	varData.add(data);
                	data.name = varNode.getName();
            	    data.type = getEmitter().formatQualifiedName(varNode.getVariableType());
            	    IMetaTagsNode metaData = varNode.getMetaTags();
            	    if (metaData != null)
            	    {
            	    	IMetaTagNode[] tags = metaData.getAllTags();
            	    	if (tags.length > 0)
            	    		data.metaData = tags;
            	    }
                }
            }
        }
        
	    HashMap<String, MethodData> accessorMap = new HashMap<String, MethodData>();
        for (IDefinitionNode dnode : dnodes)
        {
            ModifiersSet modifierSet = dnode.getDefinition().getModifiers();
            boolean isStatic = (modifierSet != null && modifierSet
                    .hasModifier(ASModifier.STATIC));
            if (!isStatic && (dnode.getNodeID() == ASTNodeID.GetterID ||
            		dnode.getNodeID() == ASTNodeID.SetterID))
            {
            	IFunctionNode fnNode = (IFunctionNode)dnode;
                String ns = fnNode.getNamespace();
                if (ns == IASKeywordConstants.PUBLIC || isInterface)
                {
                	MethodData data = new MethodData();
                	data.name = fnNode.getName();
                	if (accessorMap.containsKey(data.name)) continue;
                	accessorData.add(data);
            	    if (dnode.getNodeID() == ASTNodeID.GetterID)
            	    	data.type = fnNode.getReturnType();
            	    else
            	    	data.type = ((SetterNode)fnNode).getVariableType();
                	accessorMap.put(data.name, data);
            	    data.type = getEmitter().formatQualifiedName(data.type);
            	    IClassNode declarer = (IClassNode)fnNode.getAncestorOfType(IClassNode.class);
            	    String declarant = getEmitter().formatQualifiedName(tnode.getQualifiedName());
            	    if (declarer != null)
            	    	declarant = getEmitter().formatQualifiedName(declarer.getQualifiedName());
            	    data.declaredBy = declarant;
            	    IMetaTagsNode metaData = fnNode.getMetaTags();
            	    if (metaData != null)
            	    {
            	    	IMetaTagNode[] tags = metaData.getAllTags();
            	    	if (tags.length > 0)
                    		data.metaData = tags;
            	    }
                }
            }
        }
        for (IDefinitionNode dnode : dnodes)
        {
            ModifiersSet modifierSet = dnode.getDefinition().getModifiers();
            boolean isStatic = (modifierSet != null && modifierSet
                    .hasModifier(ASModifier.STATIC));
            if (dnode.getNodeID() == ASTNodeID.FunctionID && !isStatic)
            {
            	IFunctionNode fnNode = (IFunctionNode)dnode;
                String ns = fnNode.getNamespace();
                if (ns == IASKeywordConstants.PUBLIC || isInterface)
                {
                	MethodData data = new MethodData();
                	methodData.add(data);
                	data.name = fnNode.getName();
            	    data.type = getEmitter().formatQualifiedName(fnNode.getReturnType());
            	    ITypeNode declarer;
            	    if (isInterface)
            	    	declarer = (IInterfaceNode)fnNode.getAncestorOfType(IInterfaceNode.class);
            	    else
            	    	declarer = (IClassNode)fnNode.getAncestorOfType(IClassNode.class);
            	    String declarant = getEmitter().formatQualifiedName(tnode.getQualifiedName());
            	    if (declarer != null)
            	    	declarant = getEmitter().formatQualifiedName(declarer.getQualifiedName());
            	    data.declaredBy = declarant;
            	    IMetaTagsNode metaData = fnNode.getMetaTags();
            	    if (metaData != null)
            	    {
            	    	IMetaTagNode[] tags = metaData.getAllTags();
            	    	if (tags.length > 0)
            	    		data.metaData = tags;
            	    }
                }
            }
        }
    }
    
    public void emitReflectionData(String typeName,
    		List<VariableData> varData,
    		List<MethodData> accessorData,
    		List<MethodData> methodData,
    		IMetaTagNode[] metaData
    		)
    {
        JSFlexJSDocEmitter doc = (JSFlexJSDocEmitter) getEmitter()
        .getDocEmitter();
	    /*
	     * Reflection
	     * 
	     * @return {Object.<string, Function>}
	     */

	    writeNewline();
	    writeNewline();
	    writeNewline();
	    writeNewline();
	    doc.begin();
	    writeNewline(" * Reflection");
	    writeNewline(" *");
	    writeNewline(" * @return {Object.<string, Function>}");
	    doc.end();
	
	    // a.B.prototype.FLEXJS_REFLECTION_INFO = function() {
	    write(typeName);
	    write(ASEmitterTokens.MEMBER_ACCESS);
	    write(JSEmitterTokens.PROTOTYPE);
	    write(ASEmitterTokens.MEMBER_ACCESS);
	    writeToken(JSFlexJSEmitterTokens.FLEXJS_REFLECTION_INFO);
	    writeToken(ASEmitterTokens.EQUAL);
	    writeToken(ASEmitterTokens.FUNCTION);
	    write(ASEmitterTokens.PAREN_OPEN);
	    writeToken(ASEmitterTokens.PAREN_CLOSE);
	    write(ASEmitterTokens.BLOCK_OPEN);
	
	    indentPush();
	    writeNewline();
	    // return {
	    writeToken(ASEmitterTokens.RETURN);
	    write(ASEmitterTokens.BLOCK_OPEN);
	    indentPush();
	    writeNewline();
	    // variables: function() {
	    write("variables");
	    writeToken(ASEmitterTokens.COLON);
	    writeToken(ASEmitterTokens.FUNCTION);
	    write(ASEmitterTokens.PAREN_OPEN);
	    writeToken(ASEmitterTokens.PAREN_CLOSE);
	    write(ASEmitterTokens.BLOCK_OPEN);
	    indentPush();
	    writeNewline();
	    // return {
	    writeToken(ASEmitterTokens.RETURN);
	    write(ASEmitterTokens.BLOCK_OPEN);
	    indentPush();
	    
	    int count = 0;
        for (VariableData var : varData)
        {
        	if (count > 0)
        		write(ASEmitterTokens.COMMA);
    		writeNewline();
        	count++;
        	// varname: { type: typename
    	    write(ASEmitterTokens.SINGLE_QUOTE);
        	write(var.name);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    writeToken(ASEmitterTokens.COLON);
    	    writeToken(ASEmitterTokens.BLOCK_OPEN);
    	    write("type");
    	    writeToken(ASEmitterTokens.COLON);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    write(var.type);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
	    	IMetaTagNode[] tags = var.metaData;
	    	if (tags != null)
	    	{
        		writeToken(ASEmitterTokens.COMMA);
        	    writeMetaData(tags);
	    	}            	    	
    	    // close object
    	    write(ASEmitterTokens.BLOCK_CLOSE);
        }
	    indentPop();
	    writeNewline();
	    write(ASEmitterTokens.BLOCK_CLOSE);
    	write(ASEmitterTokens.SEMICOLON);
	    indentPop();
	    writeNewline();
	    // close variable function
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    write(ASEmitterTokens.COMMA);
	    writeNewline();
	
	    
	    // accessors: function() {
	    write("accessors");
	    writeToken(ASEmitterTokens.COLON);
	    writeToken(ASEmitterTokens.FUNCTION);
	    write(ASEmitterTokens.PAREN_OPEN);
	    writeToken(ASEmitterTokens.PAREN_CLOSE);
	    write(ASEmitterTokens.BLOCK_OPEN);
	    indentPush();
	    writeNewline();
	    // return {
	    writeToken(ASEmitterTokens.RETURN);
	    write(ASEmitterTokens.BLOCK_OPEN);
	    indentPush();
	    
	    count = 0;
        for (MethodData accessor : accessorData)
        {
        	if (count > 0)
        		write(ASEmitterTokens.COMMA);
    		writeNewline();
        	count++;
        	// accessorname: { type: typename
    	    write(ASEmitterTokens.SINGLE_QUOTE);
        	write(accessor.name);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    writeToken(ASEmitterTokens.COLON);
    	    writeToken(ASEmitterTokens.BLOCK_OPEN);
    	    write("type");
    	    writeToken(ASEmitterTokens.COLON);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    write(accessor.type);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    		writeToken(ASEmitterTokens.COMMA);
    	    write("declaredBy");
    	    writeToken(ASEmitterTokens.COLON);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    write(accessor.declaredBy);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
	    	IMetaTagNode[] tags = accessor.metaData;
	    	if (tags != null)
	    	{
        		writeToken(ASEmitterTokens.COMMA);
        	    writeMetaData(tags);
	    	}            	    	
    	    // close object
    	    write(ASEmitterTokens.BLOCK_CLOSE);
        }
	    indentPop();
	    writeNewline();
	    write(ASEmitterTokens.BLOCK_CLOSE);
    	write(ASEmitterTokens.SEMICOLON);
	    indentPop();
	    writeNewline();
	    // close accessor function
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    write(ASEmitterTokens.COMMA);
	    writeNewline();
	
	    
	    // methods: function() {
	    write("methods");
	    writeToken(ASEmitterTokens.COLON);
	    writeToken(ASEmitterTokens.FUNCTION);
	    write(ASEmitterTokens.PAREN_OPEN);
	    writeToken(ASEmitterTokens.PAREN_CLOSE);
	    write(ASEmitterTokens.BLOCK_OPEN);
	    indentPush();
	    writeNewline();
	    // return {
	    writeToken(ASEmitterTokens.RETURN);
	    write(ASEmitterTokens.BLOCK_OPEN);
	    indentPush();
	    
	    count = 0;
        for (MethodData method : methodData)
        {
        	if (count > 0)
        		write(ASEmitterTokens.COMMA);
        	writeNewline();
        	count++;
        	// methodname: { type: typename
    	    write(ASEmitterTokens.SINGLE_QUOTE);
        	write(method.name);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    writeToken(ASEmitterTokens.COLON);
    	    writeToken(ASEmitterTokens.BLOCK_OPEN);
    	    write("type");
    	    writeToken(ASEmitterTokens.COLON);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    write(method.type);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    		writeToken(ASEmitterTokens.COMMA);
    	    write("declaredBy");
    	    writeToken(ASEmitterTokens.COLON);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    write(method.declaredBy);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
	    	IMetaTagNode[] tags = method.metaData;
	    	if (tags != null)
	    	{
        		writeToken(ASEmitterTokens.COMMA);
        	    writeMetaData(tags);
	    	}            	    	
    	    // close object
    	    write(ASEmitterTokens.BLOCK_CLOSE);
        }
        // close return
	    indentPop();
	    writeNewline();
	    write(ASEmitterTokens.BLOCK_CLOSE);
    	write(ASEmitterTokens.SEMICOLON);
	    indentPop();
	    writeNewline();
	    // close method function
	    write(ASEmitterTokens.BLOCK_CLOSE);

    	if (metaData != null && metaData.length > 0)
    	{
    		write(ASEmitterTokens.COMMA);
    	    writeNewline();
    	    writeMetaData(metaData);
    	}            	    	
	    
	    indentPop();
	    writeNewline();
	    // close return object
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    write(ASEmitterTokens.SEMICOLON);
	    
	    // close function
	    indentPop();
	    writeNewline();
	    write(ASEmitterTokens.BLOCK_CLOSE);
	    writeNewline(ASEmitterTokens.SEMICOLON);
    }
    
    private void writeMetaData(IMetaTagNode[] tags)
    {
    	JSGoogConfiguration config = ((FlexJSProject)getWalker().getProject()).config;
    	Set<String> allowedNames = config.getCompilerKeepAs3Metadata();
    	
	    // metadata: function() {
		write("metadata");
	    writeToken(ASEmitterTokens.COLON);
	    writeToken(ASEmitterTokens.FUNCTION);
	    write(ASEmitterTokens.PAREN_OPEN);
	    writeToken(ASEmitterTokens.PAREN_CLOSE);
	    writeToken(ASEmitterTokens.BLOCK_OPEN);
	    // return [ array of metadata tags ]
	    writeToken(ASEmitterTokens.RETURN);
	    writeToken(ASEmitterTokens.SQUARE_OPEN);

	    int count = 0;
	    for (int i = 0; i < tags.length; i++)
	    {
	    	IMetaTagNode tag = tags[i];
	    	if (count > 0)
	    	{
        		writeToken(ASEmitterTokens.COMMA);
	    	}
	    	if (!allowedNames.contains(tag.getTagName()))
	    		continue;
	    	count++;
    	    // { name: <tag name>
    	    writeToken(ASEmitterTokens.BLOCK_OPEN);
    	    write("name");
    	    writeToken(ASEmitterTokens.COLON);
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    write(tag.getTagName());
    	    write(ASEmitterTokens.SINGLE_QUOTE);
    	    IMetaTagAttribute[] args = tag.getAllAttributes();
    	    if (args.length > 0)
    	    {
        		writeToken(ASEmitterTokens.COMMA);
        	    
        	    // args: [
        	    write("args");
        	    writeToken(ASEmitterTokens.COLON);
        	    writeToken(ASEmitterTokens.SQUARE_OPEN);
        	    
        	    for (int j = 0; j < args.length; j++)
        	    {
        	    	if (j > 0)
        	    	{
                		writeToken(ASEmitterTokens.COMMA);
        	    	}
        	    	// { key: key, value: value }
        	    	IMetaTagAttribute arg = args[j];
            	    writeToken(ASEmitterTokens.BLOCK_OPEN);
            	    write("key");
            	    writeToken(ASEmitterTokens.COLON);
            	    write(ASEmitterTokens.SINGLE_QUOTE);
            	    String key = arg.getKey();
            	    write(key == null ? "" : key);
            	    write(ASEmitterTokens.SINGLE_QUOTE);
            		writeToken(ASEmitterTokens.COMMA);
            	    write("value");
            	    writeToken(ASEmitterTokens.COLON);
            	    write(ASEmitterTokens.SINGLE_QUOTE);
            	    write(arg.getValue());
            	    write(ASEmitterTokens.SINGLE_QUOTE);
            	    write(ASEmitterTokens.BLOCK_CLOSE);
        	    }
        	    // close array of args
        	    write(ASEmitterTokens.SQUARE_CLOSE);
    	    }
    	    // close metadata object
    	    write(ASEmitterTokens.BLOCK_CLOSE);
	    }
	    // close array of metadatas
	    write(ASEmitterTokens.SQUARE_CLOSE);
	    writeToken(ASEmitterTokens.SEMICOLON);
	    // close function
	    write(ASEmitterTokens.BLOCK_CLOSE);
    }
}
