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

package org.apache.royale.compiler.internal.codegen.js.jx;

import java.util.*;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.BindableVarInfo;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.ImplicitBindableImplementation;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.SetterNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.problems.UnknownTypeProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

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
        JSRoyaleDocEmitter doc = (JSRoyaleDocEmitter) getEmitter()
        .getDocEmitter();

	    if (!getEmitter().getModel().isExterns && !getEmitter().getModel().suppressExports)
	    {
			boolean isInterface = tnode instanceof IInterfaceNode;
			boolean isDynamic = tnode instanceof IClassNode && tnode.hasModifier(ASModifier.DYNAMIC);
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
		    writeToken(JSRoyaleEmitterTokens.ROYALE_CLASS_INFO);
		    writeToken(ASEmitterTokens.EQUAL);
		    writeToken(ASEmitterTokens.BLOCK_OPEN);
		
		    // names: [{ name: '', qName: '', kind:'interface|class' }]
		    write(JSRoyaleEmitterTokens.NAMES);
		    writeToken(ASEmitterTokens.COLON);
		    write(ASEmitterTokens.SQUARE_OPEN);
		    writeToken(ASEmitterTokens.BLOCK_OPEN);
		    write(JSRoyaleEmitterTokens.NAME);
		    writeToken(ASEmitterTokens.COLON);
		    write(ASEmitterTokens.SINGLE_QUOTE);
		    write(tnode.getName());
		    write(ASEmitterTokens.SINGLE_QUOTE);
		    writeToken(ASEmitterTokens.COMMA);
		    write(JSRoyaleEmitterTokens.QNAME);
		    writeToken(ASEmitterTokens.COLON);
		    write(ASEmitterTokens.SINGLE_QUOTE);
		    write(getEmitter().formatQualifiedName(tnode.getQualifiedName()));
		    write(ASEmitterTokens.SINGLE_QUOTE);
			writeToken(ASEmitterTokens.COMMA);
			write(JSRoyaleEmitterTokens.ROYALE_CLASS_INFO_KIND);
			writeToken(ASEmitterTokens.COLON);
			write(ASEmitterTokens.SINGLE_QUOTE);
			if (isInterface) write(JSRoyaleEmitterTokens.ROYALE_CLASS_INFO_INTERFACE_KIND);
			else write(JSRoyaleEmitterTokens.ROYALE_CLASS_INFO_CLASS_KIND);
			//writeToken(ASEmitterTokens.SINGLE_QUOTE);
			
			if (isDynamic) {
				//only add the 'isDynamic' tag when it is needed
				write(ASEmitterTokens.SINGLE_QUOTE);
				writeToken(ASEmitterTokens.COMMA);
				write(JSRoyaleEmitterTokens.ROYALE_CLASS_INFO_IS_DYNAMIC);
				writeToken(ASEmitterTokens.COLON);
				write(ASEmitterTokens.TRUE);
			} else {
				writeToken(ASEmitterTokens.SINGLE_QUOTE);
			}
			
		    write(ASEmitterTokens.BLOCK_CLOSE);
		    write(ASEmitterTokens.SQUARE_CLOSE);
	
		    IExpressionNode[] enodes;
		    if (tnode instanceof IClassNode)
		        enodes = ((IClassNode) tnode).getImplementedInterfaceNodes();
		    else {
				enodes = ((IInterfaceNode) tnode).getExtendedInterfaceNodes();
			}
	
	
			boolean needsIEventDispatcher = tnode instanceof IClassNode
					&& ((IClassDefinition) tnode.getDefinition()).needsEventDispatcher(getProject())
					&& getModel().getImplicitBindableImplementation() == ImplicitBindableImplementation.IMPLEMENTS;
	
			//we can remove the mapping from the model for ImplicitBindableImplementation now
			if (tnode.getDefinition() instanceof IClassDefinition)
					getModel().unregisterImplicitBindableImplementation(
							(IClassDefinition) tnode.getDefinition());
	
		    if (enodes.length > 0 || needsIEventDispatcher)
		    {
		        writeToken(ASEmitterTokens.COMMA);
		
		        // interfaces: [a.IC, a.ID]
		        write(JSRoyaleEmitterTokens.INTERFACES);
		        writeToken(ASEmitterTokens.COLON);
		        write(ASEmitterTokens.SQUARE_OPEN);
				if (needsIEventDispatcher) {
					//add IEventDispatcher interface to implemented interfaces list
					write(getEmitter().formatQualifiedName(BindableEmitter.DISPATCHER_INTERFACE_QNAME));
					if (enodes.length > 0)
						writeToken(ASEmitterTokens.COMMA);
				}
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

		    if (needsIEventDispatcher) {
				JSRoyaleEmitter fjs = (JSRoyaleEmitter)getEmitter();
				fjs.getBindableEmitter().emitBindableInterfaceMethods(((IClassDefinition) tnode.getDefinition()));
			}

		    collectReflectionData(tnode);
		    IMetaTagNode[] metadata = null;
		    IMetaTagsNode metadataTags = tnode.getMetaTags();
		    if (metadataTags != null)
		    	metadata = metadataTags.getAllTags();
	
			String typeName = getEmitter().formatQualifiedName(tnode.getQualifiedName());
	
			emitReflectionData(
					typeName,
					reflectionKind,
					varData,
					accessorData,
					methodData,
					metadata);
			
		    if (!isInterface) {
		    	emitReflectionRegisterInitialStaticFields(typeName, (ClassDefinition) tnode.getDefinition());
			}
		   
		    emitExportProperties(typeName, exportProperties, exportSymbols);
	    }
    }

    public enum ReflectionKind{
		CLASS,
		INTERFACE
	}
    
    public class VariableData
    {
    	public String name;
		public String customNS = null;
    	public String type;
		public Boolean isStatic = false;
    	public IMetaTagNode[] metaData;
    }
    
    public class MethodData
    {
    	public String name;
		public String customNS = null;
    	public String type;
		public Boolean isStatic = false;
    	public String declaredBy;
		public IParameterNode [] parameters;
    	public IMetaTagNode[] metaData;
    }

	public class AccessorData extends MethodData
	{
		public String access;
	}

	private ArrayList<VariableData> varData;
    private ArrayList<AccessorData> accessorData;
    private ArrayList<MethodData> methodData;
	private ReflectionKind reflectionKind;
    private ArrayList<String> exportProperties;
    private ArrayList<String> exportSymbols;
    
    public void collectReflectionData(ITypeNode tnode)
    {
    	JSRoyaleEmitter fjs = (JSRoyaleEmitter)getEmitter();
    	exportProperties = new ArrayList<String>();
    	exportSymbols = new ArrayList<String>();
		ICompilerProject project = getWalker().getProject();
    	Set<String> exportMetadata = Collections.<String> emptySet();
    	if (project instanceof RoyaleJSProject)
    	{
    		RoyaleJSProject fjsp = ((RoyaleJSProject)project);
    		if (fjsp.config != null)
    			exportMetadata = fjsp.config.getCompilerKeepCodeWithMetadata();
    	}
    	varData = new ArrayList<VariableData>();
    	accessorData = new ArrayList<AccessorData>();
    	methodData = new ArrayList<MethodData>();
    	/*
	     * Reflection
	     *
	     * @return {Object.<string, Function>}
	     */
        IDefinitionNode[] dnodes;
		String name;
		//bindables:
		HashMap<String, BindableVarInfo> bindableVars = getModel().getBindableVars();
        boolean isInterface = tnode instanceof IInterfaceNode;
	    if (!isInterface)
	        dnodes = ((IClassNode) tnode).getAllMemberNodes();
	    else
	        dnodes = ((IInterfaceNode) tnode).getAllMemberDefinitionNodes();
		reflectionKind = isInterface ? ReflectionKind.INTERFACE : ReflectionKind.CLASS;

        for (IDefinitionNode dnode : dnodes)
        {
            ModifiersSet modifierSet = dnode.getDefinition().getModifiers();
            boolean isStatic = (modifierSet != null && modifierSet
                    .hasModifier(ASModifier.STATIC));
            if ((dnode.getNodeID() == ASTNodeID.VariableID ||
            		dnode.getNodeID() == ASTNodeID.BindableVariableID))
            {
            	IVariableNode varNode = (IVariableNode)dnode;
                String ns = varNode.getNamespace();
				boolean isConst = varNode.isConst();
				if (isConst) {
					//todo consider outputting consts, none output for now
					continue;
				}
				//explicit exclusion from reflection data:
				if (getModel().suppressedExportNodes.contains(varNode)) {
					continue;
				}
				String altNS = null;
				if (ns != null &&  EmitterUtils.isCustomNamespace(ns)) {
					altNS = ((INamespaceDefinition)(((VariableNode) varNode).getNamespaceNode().resolve(getProject()))).getURI();
				}
                if (isInterface || (ns != null && ns.equals(IASKeywordConstants.PUBLIC )) || altNS != null)
                {
                	name = varNode.getName();

					IMetaTagsNode metaData = varNode.getMetaTags();
					//first deal with 'Bindable' upgrades to getters/setters
					if (!isInterface && bindableVars.containsKey(name)
							&& bindableVars.get(name).namespace.equals(IASKeywordConstants.PUBLIC)) {

						AccessorData bindableAccessor = new AccessorData();
						bindableAccessor.customNS = altNS;
						bindableAccessor.name = name;
						bindableAccessor.access = "readwrite";
						bindableAccessor.type = bindableVars.get(name).type;
						bindableAccessor.declaredBy = fjs.formatQualifiedName(tnode.getQualifiedName(), true);
						bindableAccessor.isStatic = isStatic;
						//attribute the metadata from the var definition to the Bindable Accessor implementation
						if (metaData != null)
						{
							IMetaTagNode[] tags = metaData.getAllTags();
							if (tags.length > 0)
								bindableAccessor.metaData = tags;
						}
						accessorData.add(bindableAccessor);
						//skip processing this varNode as a variable, it has now be added as an accessor
						continue;
					}


                	VariableData data = new VariableData();
                	varData.add(data);
                	data.name = name;
                	data.customNS = altNS;
					data.isStatic = isStatic;
					String qualifiedTypeName =	varNode.getVariableTypeNode().resolveType(getProject()).getQualifiedName();
					data.type = fjs.formatQualifiedName(qualifiedTypeName, true);

            	    if (metaData != null)
            	    {
            	    	IMetaTagNode[] tags = metaData.getAllTags();
            	    	if (tags.length > 0)
            	    	{
            	    		data.metaData = tags;
            	    		for (IMetaTagNode tag : tags)
            	    		{
            	    			String tagName =  tag.getTagName();
            	    			if (exportMetadata.contains(tagName))
            	    			{
            	    				if (data.isStatic)
            	    					exportSymbols.add(data.name);
            	    				else
                	    				exportProperties.add(data.name);
            	    			}
            	    		}
            	    	}
            	    }
                }
            }
        }

        if (getModel().hasStaticBindableVars()) {
			//we have an implicit implementation of a static event dispatcher
			//so add the 'staticEventDispatcher' accessor to the reflection data
			AccessorData staticEventDispatcher = new AccessorData();
			staticEventDispatcher.name = BindableEmitter.STATIC_DISPATCHER_GETTER;
			staticEventDispatcher.access = "readonly";
			staticEventDispatcher.type = fjs.formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME, true);
			staticEventDispatcher.declaredBy = fjs.formatQualifiedName(tnode.getQualifiedName(), true);
			staticEventDispatcher.isStatic = true;
			accessorData.add(staticEventDispatcher);
		}
     
	    HashMap<String, AccessorData> instanceAccessorMap = new HashMap<String, AccessorData>();
		HashMap<String, AccessorData> staticAccessorMap = new HashMap<String, AccessorData>();
        for (IDefinitionNode dnode : dnodes)
        {
            ModifiersSet modifierSet = dnode.getDefinition().getModifiers();
            boolean isStatic = (modifierSet != null && modifierSet
                    .hasModifier(ASModifier.STATIC));

			HashMap<String, AccessorData> accessorMap = isStatic ? staticAccessorMap : instanceAccessorMap;
            if ((dnode.getNodeID() == ASTNodeID.GetterID ||
            		dnode.getNodeID() == ASTNodeID.SetterID))
            {
            	IFunctionNode fnNode = (IFunctionNode)dnode;
                String ns = fnNode.getNamespace();
                boolean suppressed = getModel().suppressedExportNodes.contains(fnNode);
	
				String altNS = null;
				if (ns != null &&  EmitterUtils.isCustomNamespace(ns)) {
					altNS = ((INamespaceDefinition)(((FunctionNode) fnNode).getNamespaceNode().resolve(getProject()))).getURI();
				}

                if (isInterface || (ns != null && ns.equals(IASKeywordConstants.PUBLIC)) || altNS != null)
                {
					String accessorName = fnNode.getName();
					String nameKey = altNS!=null? altNS+accessorName : accessorName;
                	AccessorData data = accessorMap.get(nameKey);
					if (data == null) {
						if (suppressed) continue;
						data = new AccessorData();
					} else {
						if (suppressed) {
							accessorData.remove(data);
							accessorMap.remove(nameKey);
							continue;
						}
					}
                	data.name = accessorName;
					data.customNS = altNS;
                	if (!accessorData.contains(data)) accessorData.add(data);
            	    if (dnode.getNodeID() == ASTNodeID.GetterID) {
						data.type = fnNode.getReturnTypeNode().resolveType(getProject()).getQualifiedName();
						if (data.access == null) {
							data.access = "readonly";
						} else data.access = "readwrite";
					}
            	    else {
						data.type = ((SetterNode)fnNode).getVariableTypeNode().resolveType(getProject()).getQualifiedName();
						if (data.access == null) {
							data.access = "writeonly";
						} else data.access = "readwrite";
					}
                	accessorMap.put(nameKey, data);
            	    data.type = fjs.formatQualifiedName(data.type, true);
            	    IClassNode declarer = (IClassNode)fnNode.getAncestorOfType(IClassNode.class);
            	    String declarant = fjs.formatQualifiedName(tnode.getQualifiedName(), true);
            	    if (declarer != null)
            	    	declarant = fjs.formatQualifiedName(declarer.getQualifiedName(), true);
            	    data.declaredBy = declarant;
					data.isStatic = isStatic;
            	    IMetaTagsNode metaData = fnNode.getMetaTags();
            	    if (metaData != null)
            	    {
            	    	IMetaTagNode[] tags = metaData.getAllTags();
            	    	if (tags.length > 0)
            	    	{
            	    		data.metaData = tags;
        	    			/* accessors don't need exportProp since they are referenced via the defineProp data structure
            	    		for (IMetaTagNode tag : tags)
            	    		{
            	    			String tagName =  tag.getTagName();
            	    			if (exportMetadata.contains(tagName))
            	    			{
            	    				if (data.isStatic)
            	    					exportSymbols.add(data.name);
            	    				else
                	    				exportProperties.add(data.name);
            	    			}
            	    		}
            	    		*/
            	    	}
            	    }
                }
            }
        }
        for (IDefinitionNode dnode : dnodes)
        {
            ModifiersSet modifierSet = dnode.getDefinition().getModifiers();
            boolean isStatic = (modifierSet != null && modifierSet
                    .hasModifier(ASModifier.STATIC));
            if (dnode.getNodeID() == ASTNodeID.FunctionID )
            {
            	IFunctionNode fnNode = (IFunctionNode)dnode;
                String ns = fnNode.getNamespace();
	
				String altNS = null;
				if (ns != null && EmitterUtils.isCustomNamespace(ns)) {
					altNS = ((INamespaceDefinition)(((FunctionNode) fnNode).getNamespaceNode().resolve(getProject()))).getURI();
				}
                
                if (isInterface || (ns != null && ns.equals(IASKeywordConstants.PUBLIC)) || altNS != null)
                {
                	if (getModel().suppressedExportNodes.contains(fnNode)) continue;
                	MethodData data = new MethodData();
					data.isStatic = isStatic;
                	methodData.add(data);
                	data.name = fnNode.getName();
                	data.customNS = altNS;
					String qualifiedTypeName =	fnNode.getReturnType();
					if (!(qualifiedTypeName.equals("") || qualifiedTypeName.equals("void"))) {
							qualifiedTypeName = fnNode.getReturnTypeNode().resolveType(getProject()).getQualifiedName();
					}
					data.type = fjs.formatQualifiedName(qualifiedTypeName, true);
            	    ITypeNode declarer;
            	    if (isInterface)
            	    	declarer = (IInterfaceNode)fnNode.getAncestorOfType(IInterfaceNode.class);
            	    else
            	    	declarer = (IClassNode)fnNode.getAncestorOfType(IClassNode.class);
            	    String declarant = fjs.formatQualifiedName(tnode.getQualifiedName(), true);
            	    if (declarer != null)
            	    	declarant = fjs.formatQualifiedName(declarer.getQualifiedName(), true);
            	    data.declaredBy = declarant;
            	    IMetaTagsNode metaData = fnNode.getMetaTags();
            	    if (metaData != null)
            	    {
            	    	IMetaTagNode[] tags = metaData.getAllTags();
            	    	if (tags.length > 0)
            	    	{
            	    		data.metaData = tags;
            	    		for (IMetaTagNode tag : tags)
            	    		{
            	    			String tagName =  tag.getTagName();
            	    			if (exportMetadata.contains(tagName))
            	    			{
            	    				if (data.isStatic)
            	    					exportSymbols.add(data.name);
            	    				else
                	    				exportProperties.add(data.name);
            	    			}
            	    		}
            	    	}
            	    }
					IParameterNode[] paramNodes = fnNode.getParameterNodes();
					if (paramNodes != null) {
						data.parameters = paramNodes;
					}
				}
            }
        }
    }




    private void emitReflectionDataStart(String typeName) {
		JSRoyaleDocEmitter doc = (JSRoyaleDocEmitter) getEmitter()
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

		// a.B.prototype.ROYALE_REFLECTION_INFO = function() {
		write(typeName);
		write(ASEmitterTokens.MEMBER_ACCESS);
		write(JSEmitterTokens.PROTOTYPE);
		write(ASEmitterTokens.MEMBER_ACCESS);
		writeToken(JSRoyaleEmitterTokens.ROYALE_REFLECTION_INFO);
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
	}

	private void emitReflectionDataEnd(String typeName, boolean hasContent) {
		JSGoogConfiguration config = ((RoyaleJSProject)getWalker().getProject()).config;
		
		if (hasContent) writeNewline();
		// close return object
		write(ASEmitterTokens.BLOCK_CLOSE);
		write(ASEmitterTokens.SEMICOLON);

		// close function
		indentPop();
		writeNewline();
		write(ASEmitterTokens.BLOCK_CLOSE);
		writeNewline(ASEmitterTokens.SEMICOLON);
		
		if (config == null) return;
		//add compiletime descriptor flags
		//allow dead-code elimination if reflection is not used
		//doc emitter-ish:
		writeNewline("/**");
		writeNewline(" * @const");
		writeNewline(" * @type {number}");
		writeNewline(" */");
		
		//{typeName}.prototype.ROYALE_COMPILE_FLAGS = {int value here};
		write(typeName);
		write(ASEmitterTokens.MEMBER_ACCESS);
		write(JSEmitterTokens.PROTOTYPE);
		write(ASEmitterTokens.MEMBER_ACCESS);
		writeToken(JSRoyaleEmitterTokens.ROYALE_REFLECTION_INFO_COMPILE_TIME_FLAGS);
		writeToken(ASEmitterTokens.EQUAL);
		//
		write(String.valueOf(config.getReflectionFlags()));
		writeNewline(ASEmitterTokens.SEMICOLON);
	}
    
    public void emitReflectionData(
			String typeName,
			ReflectionKind outputType,
    		List<VariableData> varData,
    		List<AccessorData> accessorData,
    		List<MethodData> methodData,
    		IMetaTagNode[] metaData
    		)
    {

		emitReflectionDataStart(typeName);
		boolean indented = false;
		boolean continueContent = false;
		int count;
		if (outputType == ReflectionKind.CLASS) {
			
			if (varData.size() > 0) {
				indentPush();
				writeNewline();
				indented = true;
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

				count = 0;
				for (VariableData var : varData) {
					if (count > 0)
						write(ASEmitterTokens.COMMA);
					writeNewline();
					count++;
					// varname: { type: typename
					write(ASEmitterTokens.SINGLE_QUOTE);
					//prefix static var names with |
					if (var.isStatic) {
					    write("|");
                    }
					if (var.customNS != null) {
						write(var.customNS);
						write("::");
					}
					write(var.name);
					write(ASEmitterTokens.SINGLE_QUOTE);
					writeToken(ASEmitterTokens.COLON);
					writeToken(ASEmitterTokens.BLOCK_OPEN);
					write("type");
					writeToken(ASEmitterTokens.COLON);
					write(ASEmitterTokens.SINGLE_QUOTE);
					write(var.type);
					write(ASEmitterTokens.SINGLE_QUOTE);
					
					//provide a get_set function that works in release build with public vars
					writeToken(ASEmitterTokens.COMMA);
					write(JSRoyaleEmitterTokens.ROYALE_REFLECTION_INFO_GET_SET);
					writeToken(ASEmitterTokens.COLON);
					writeToken(ASEmitterTokens.FUNCTION);
					boolean valueIsUntyped = var.type.equals("*");
					if (valueIsUntyped) {
						//give the function a local name because a self-reference argument will be used to signify that
						//it is not being used as a setter (because 'undefined' is a valid possible value to set)
						write("f");
					}
					write(ASEmitterTokens.PAREN_OPEN);
					
					if (!var.isStatic) {
						//instance type parameter
						writeToken("/** " + typeName + " */");
						write("inst");
						writeToken(ASEmitterTokens.COMMA);
					}
					//any type for value
					write("/** * */ v");
					writeToken(ASEmitterTokens.PAREN_CLOSE);
					write(ASEmitterTokens.BLOCK_OPEN);
					String getterSetter;
					String varName;
					if (var.customNS != null) {
						varName = JSRoyaleEmitter.formatNamespacedProperty(var.customNS, var.name, false);
					} else varName = var.name;
					
					String field = var.isStatic ? typeName + "." + varName : "inst." + varName;
					if (valueIsUntyped) {
						//to avoid setting when type is '*' set the 'value' param to the function being called, which
						//causes a 'getter only' result
						//In the case of no parameter or literal undefined being passed, it will be treated as the value
						//of undefined to be assigned to the variable field
						getterSetter = "return v !== f ? "+ field + " = v : " + field + ";";
					} else {
						getterSetter = "return v !== undefined ? " + field + " = v : " + field + ";";
					}
					write(getterSetter);
					write(ASEmitterTokens.BLOCK_CLOSE);
					
					IMetaTagNode[] tags = var.metaData;
					if (tags != null) {
						//writeToken(ASEmitterTokens.COMMA);
						writeMetaData(tags, true, false);
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
				continueContent = true;
			
			}
		}
	    
	 

		if (accessorData.size() > 0) {
			if (continueContent) {
				write(ASEmitterTokens.COMMA);
				writeNewline();
			}
			// accessors: function() {
			if (!indented) {
				indentPush();
				writeNewline();
				indented = true;
			}
			
			
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
			for (AccessorData accessor : accessorData)
			{
				if (count > 0)
					write(ASEmitterTokens.COMMA);
				writeNewline();
				count++;
				// accessorname: { type: typename
				write(ASEmitterTokens.SINGLE_QUOTE);
				//prefix static accessor names with |
				if (accessor.isStatic) {
					write("|");
				}
				if (accessor.customNS != null) {
					write(accessor.customNS);
					write("::");
				}
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
				write("access");
				writeToken(ASEmitterTokens.COLON);
				write(ASEmitterTokens.SINGLE_QUOTE);
				write(accessor.access);
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
					//writeToken(ASEmitterTokens.COMMA);
					writeMetaData(tags, true, false);
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
			continueContent = true;
		}

	 
		if (methodData.size() > 0) {
			if (continueContent){
				write(ASEmitterTokens.COMMA);
				writeNewline();
			}
			
			if (!indented) {
				indentPush();
				writeNewline();
				indented = true;
			}
			
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
				//prefix static method names with |
				if (method.isStatic) {
					write("|");
				}
				if (method.customNS != null) {
					write(method.customNS);
					write("::");
				}
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

				IParameterNode[] params = method.parameters;
				//only output params if there are any
				if (params!=null && params.length > 0) {
					writeToken(ASEmitterTokens.COMMA);
					writeParameters(params);
				}
				IMetaTagNode[] metas = method.metaData;
				if (metas != null)
				{
					writeMetaData(metas, true, false);
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
			continueContent = true;
		}
		
    	if (metaData != null && metaData.length > 0)
    	{
    	    writeMetaData(metaData, continueContent, continueContent);
    	}
	    if (indented)
	    	indentPop();
		emitReflectionDataEnd(typeName, indented);
    }
    

	private void writeParameters(IParameterNode[] params)
	{
		// parameters: function() {
		write("parameters");
		writeToken(ASEmitterTokens.COLON);
		writeToken(ASEmitterTokens.FUNCTION);
		write(ASEmitterTokens.PAREN_OPEN);
		writeToken(ASEmitterTokens.PAREN_CLOSE);
		writeToken(ASEmitterTokens.BLOCK_OPEN);
		// return [ array of parameter definitions ]
		writeToken(ASEmitterTokens.RETURN);
		writeToken(ASEmitterTokens.SQUARE_OPEN);

		int len = params.length;
		for (int i = 0; i < len ; i++) {
			IParameterDefinition parameterDefinition = (IParameterDefinition) params[i].getDefinition();

			write(ASEmitterTokens.SINGLE_QUOTE);
			ITypeDefinition pd = parameterDefinition.resolveType(getProject());
			if (pd == null)
			{
				UnknownTypeProblem problem = new UnknownTypeProblem(parameterDefinition.getNode(), parameterDefinition.getQualifiedName());
				getProject().getProblems().add(problem);
				write("not found");
			}
			else
				write(pd.getQualifiedName());
			write(ASEmitterTokens.SINGLE_QUOTE);

			write(ASEmitterTokens.COMMA);
			write(ASEmitterTokens.SPACE);
			writeToken(parameterDefinition.hasDefaultValue() ? ASEmitterTokens.TRUE :  ASEmitterTokens.FALSE);
			
			if (i < len-1) write(ASEmitterTokens.COMMA);
		}

		// close array of parameter definitions
		write(ASEmitterTokens.SQUARE_CLOSE);
		writeToken(ASEmitterTokens.SEMICOLON);
		// close function
		write(ASEmitterTokens.BLOCK_CLOSE);
	}
	
	private ArrayList<IMetaTagNode> getAllowedMetadata(IMetaTagNode[] tags) {
		JSGoogConfiguration config = ((RoyaleJSProject)getWalker().getProject()).config;
		Set<String> allowedNames = config.getCompilerKeepAs3Metadata();
		
		ArrayList<IMetaTagNode> filteredTags = new ArrayList<IMetaTagNode>(tags.length);
		for (IMetaTagNode tag : tags)
		{
			if (allowedNames.contains(tag.getTagName())) filteredTags.add(tag);
		}
		return filteredTags;
	}
	
	private void writeAllowedMetadata(ArrayList<IMetaTagNode> filteredTags ) {
		int count = 0;
		int len = filteredTags.size();
		
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
		
		for (IMetaTagNode tag : filteredTags)
		{
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
					write(formatJSStringValue(arg.getValue()));
					write(ASEmitterTokens.SINGLE_QUOTE);
					write(ASEmitterTokens.SPACE);
					write(ASEmitterTokens.BLOCK_CLOSE);
				}
				// close array of args
				write(ASEmitterTokens.SPACE);
				write(ASEmitterTokens.SQUARE_CLOSE);
			}
			// close metadata object
			write(ASEmitterTokens.SPACE);
			write(ASEmitterTokens.BLOCK_CLOSE);
			if (count > 0 && count < len)
			{
				writeToken(ASEmitterTokens.COMMA);
			}
		}
		// close array of metadatas
		write(ASEmitterTokens.SPACE);
		write(ASEmitterTokens.SQUARE_CLOSE);
		writeToken(ASEmitterTokens.SEMICOLON);
		// close function
		write(ASEmitterTokens.BLOCK_CLOSE);
	}
    
    private void writeMetaData(IMetaTagNode[] tags, boolean prefixComma, boolean prefixNewline)
    {
		ArrayList<IMetaTagNode> filteredTags = getAllowedMetadata(tags);
		if (filteredTags.size() == 0) {
			//nothing to write
			return;
		}
		if (prefixNewline) {
			if (prefixComma) {
				write(ASEmitterTokens.COMMA);
			}
			writeNewline();
		} else {
			if (prefixComma) {
				writeToken(ASEmitterTokens.COMMA);
			}
		}
		writeAllowedMetadata(filteredTags);
    }

    private String formatJSStringValue(String value) {
		//todo: check other possible metadata values for any need for js string escaping etc
    	value = value.replace("'","\\'");
    	return value;
	}
	
	public void emitReflectionRegisterInitialStaticFields(String typeName, IClassDefinition classDef) {
		//this is only output if the default initializers are enabled (otherwise runtime reflection results are not reliable)
		//local config check here (instead of call site check) - in case this needs to change in the future:
		JSGoogConfiguration config = ((RoyaleJSProject)getWalker().getProject()).config;
		if (config == null || !config.getJsDefaultInitializers()) return;
		
		boolean needsStaticsList = false;
		Collection<IDefinitionSet> defs = classDef.getContainedScope().getAllLocalDefinitionSets();
		for (IDefinitionSet set : defs) {
			for (int i = 0, l = set.getSize(); i < l; ++i) {
				IDefinition d = set.getDefinition(i);
				if (d.isStatic()) {
					needsStaticsList = true;
					break;
				}
			}
			if (needsStaticsList) break;
		}
		if (needsStaticsList) {
			//support for reflection on static classes: supports ability to distinguish between initial fields and dynamic fields
			//this is excluded via dead-code-elimination if never used
			//doc emitter-ish:
			writeNewline("/**");
			writeNewline(" * Provide reflection support for distinguishing dynamic fields on class object (static)");
			writeNewline(" * @const");
			writeNewline(" * @type {Array<string>}");
			writeNewline(" */");
			
			//{typeName}.prototype.ROYALE_INITIAL_STATICS = Object.keys({typeName});
			write(typeName);
			write(ASEmitterTokens.MEMBER_ACCESS);
			write(JSEmitterTokens.PROTOTYPE);
			write(ASEmitterTokens.MEMBER_ACCESS);
			writeToken(JSRoyaleEmitterTokens.ROYALE_REFLECTION_INFO_INITIAL_STATICS);
			writeToken(ASEmitterTokens.EQUAL);
			write("Object.keys");
			write(ASEmitterTokens.PAREN_OPEN);
			write(typeName);
			write(ASEmitterTokens.PAREN_CLOSE);
			write(ASEmitterTokens.SEMICOLON);
			writeNewline();
		}
	}
    
    public void emitExportProperties(String typeName, ArrayList<String> exportProperties, ArrayList<String> exportSymbols)
    {
    	for (String prop : exportSymbols)
    	{
    		write(JSRoyaleEmitterTokens.GOOG_EXPORT_SYMBOL);
    		write(ASEmitterTokens.PAREN_OPEN);
    		write(ASEmitterTokens.SINGLE_QUOTE);
    		write(typeName);
    		write(ASEmitterTokens.MEMBER_ACCESS);
    		write(prop);
    		write(ASEmitterTokens.SINGLE_QUOTE);
    		write(ASEmitterTokens.COMMA);
    		write(ASEmitterTokens.SPACE);
    		write(typeName);
    		write(ASEmitterTokens.MEMBER_ACCESS);
    		write(prop);
    		write(ASEmitterTokens.PAREN_CLOSE);
    		writeNewline(ASEmitterTokens.SEMICOLON);
    	}
    	for (String prop : exportProperties)
    	{
    		write(JSRoyaleEmitterTokens.GOOG_EXPORT_PROPERTY);
    		write(ASEmitterTokens.PAREN_OPEN);
    		write(typeName);
    		write(ASEmitterTokens.MEMBER_ACCESS);
    		write(JSEmitterTokens.PROTOTYPE);
    		write(ASEmitterTokens.COMMA);
    		write(ASEmitterTokens.SPACE);
    		write(ASEmitterTokens.SINGLE_QUOTE);
    		write(prop);
    		write(ASEmitterTokens.SINGLE_QUOTE);
    		write(ASEmitterTokens.COMMA);
    		write(ASEmitterTokens.SPACE);
    		write(typeName);
    		write(ASEmitterTokens.MEMBER_ACCESS);
    		write(JSEmitterTokens.PROTOTYPE);
    		write(ASEmitterTokens.MEMBER_ACCESS);
    		write(prop);
    		write(ASEmitterTokens.PAREN_CLOSE);
    		writeNewline(ASEmitterTokens.SEMICOLON);
    	}
    }
}
