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

package org.apache.flex.compiler.internal.codegen.mxml.flexjs;


import java.io.File;
import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.mxml.flexjs.IMXMLFlexJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.databinding.BindingDatabase;
import org.apache.flex.compiler.internal.codegen.databinding.BindingInfo;
import org.apache.flex.compiler.internal.codegen.databinding.FunctionWatcherInfo;
import org.apache.flex.compiler.internal.codegen.databinding.PropertyWatcherInfo;
import org.apache.flex.compiler.internal.codegen.databinding.StaticPropertyWatcherInfo;
import org.apache.flex.compiler.internal.codegen.databinding.WatcherInfoBase;
import org.apache.flex.compiler.internal.codegen.databinding.WatcherInfoBase.WatcherType;
import org.apache.flex.compiler.internal.codegen.databinding.XMLWatcherInfo;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel.PropertyNodes;
import org.apache.flex.compiler.internal.codegen.js.JSSessionModel.BindableVarInfo;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitter;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.codegen.mxml.MXMLEmitter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.targets.ITargetAttributes;
import org.apache.flex.compiler.internal.tree.as.FunctionCallNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.mxml.MXMLDocumentNode;
import org.apache.flex.compiler.internal.tree.mxml.MXMLFileNode;
import org.apache.flex.compiler.mxml.IMXMLLanguageConstants;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.*;
import org.apache.flex.compiler.tree.metadata.IMetaTagNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagsNode;
import org.apache.flex.compiler.tree.mxml.*;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.NativeUtils;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;
import org.apache.flex.swc.ISWC;

import com.google.common.base.Joiner;

/**
 * @author Erik de Bruin
 */
public class MXMLFlexJSEmitter extends MXMLEmitter implements
        IMXMLFlexJSEmitter
{

	// the instances in a container
    private ArrayList<MXMLDescriptorSpecifier> currentInstances;
    private ArrayList<MXMLDescriptorSpecifier> currentPropertySpecifiers;
    private ArrayList<MXMLDescriptorSpecifier> descriptorTree;
    private MXMLDescriptorSpecifier propertiesTree;
    private MXMLDescriptorSpecifier currentStateOverrides;
    private ArrayList<MXMLEventSpecifier> events;
    // all instances in the current document or subdocument
    private ArrayList<MXMLDescriptorSpecifier> instances;
    // all instances in the document AND its subdocuments
    private ArrayList<MXMLDescriptorSpecifier> allInstances = new ArrayList<MXMLDescriptorSpecifier>();
    private ArrayList<MXMLScriptSpecifier> scripts;
    //private ArrayList<MXMLStyleSpecifier> styles;
    private IClassDefinition classDefinition;
    private IClassDefinition documentDefinition;
    private ArrayList<String> usedNames = new ArrayList<String>();
    private ArrayList<IMXMLMetadataNode> metadataNodes = new ArrayList<IMXMLMetadataNode>();
    
    private int eventCounter;
    private int idCounter;
    private int bindingCounter;

    private boolean inMXMLContent;
    private boolean inStatesOverride;
    private boolean makingSimpleArray;
    
    private StringBuilder subDocuments = new StringBuilder();
    private ArrayList<String> subDocumentNames = new ArrayList<String>();
    private String interfaceList;
    
    /**
     * This keeps track of the entries in our temporary array of 
     * DeferredInstanceFromFunction objects that we CG to help with
     * State override CG.
     * 
     * Keys are Instance nodes,
     * values are the array index where the deferred instance is:
     * 
     *  deferred instance = local3[ nodeToIndexMap.get(an instance) ]
     */
    protected Map<IMXMLNode, Integer> nodeToIndexMap;
    
    public MXMLFlexJSEmitter(FilterWriter out)
    {
        super(out);
    }

    @Override
    public String postProcess(String output)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
        ArrayList<String> asEmitterUsedNames = ((JSFlexJSEmitter)asEmitter).usedNames;
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        String currentClassName = fjs.getModel().getCurrentClass().getQualifiedName();
        for (String usedName : asEmitterUsedNames) {
            //remove any internal component that has been registered with the other emitter's usedNames
            if (usedName.startsWith(currentClassName+".") && subDocumentNames.contains(usedName.substring(currentClassName.length()+1))) {
                asEmitterUsedNames.remove(usedName);
            }
        }
        System.out.println(currentClassName + " as: " + asEmitterUsedNames.toString());
        System.out.println(currentClassName + " mxml: " + usedNames.toString());
        usedNames.addAll(asEmitterUsedNames);

        boolean foundXML = false;
    	String[] lines = output.split("\n");
    	ArrayList<String> finalLines = new ArrayList<String>();
    	int endRequires = -1;
    	int provideIndex = -1;
    	boolean sawRequires = false;
    	boolean depsAdded = false;
    	boolean stillSearching = true;
        ArrayList<String> namesToAdd = new ArrayList<String>();
        ArrayList<String> foundRequires = new ArrayList<String>();
        int i = 0;
    	for (String line : lines)
    	{
    		if (stillSearching)
    		{
    			String token = JSGoogEmitterTokens.FLEXJS_DEPENDENCY_LIST.getToken();
	            int c = line.indexOf(token);
	            if (c > -1)
	            {
	                int c2 = line.indexOf("*/");
	                String s = line.substring(c + token.length(), c2);
	                String[] reqs = s.split(",");
	                for (String req : reqs)
	                {
	                	if (usedNames.contains(req) && !foundRequires.contains(req))
	                		foundRequires.add(req);
	                }
	    			sawRequires = true;
	    			endRequires = i;
	    		}
	    		else if (sawRequires)
	    		{
	    	    	// append info() structure if main CU
	    	        ICompilerProject project = getMXMLWalker().getProject();
    	            FlexJSProject flexJSProject = null;
	    	        if (project instanceof FlexJSProject)
	    	            flexJSProject = (FlexJSProject) project;
	    	        
	    			stillSearching = false;
                    for (String usedName :usedNames) {
                        if (!foundRequires.contains(usedName)) {
                            if (usedName.equals(classDefinition.getQualifiedName())) continue;
                            if (((JSFlexJSEmitter) asEmitter).getModel().isInternalClass(usedName)) continue;
                            if (subDocumentNames.contains(usedName)) continue;
                            if (flexJSProject != null && flexJSProject.isExternalLinkage(flexJSProject.resolveQNameToCompilationUnit(usedName)))
                            	continue;
                            namesToAdd.add(usedName);
                        }
                    }

                    for (String nameToAdd : namesToAdd) {
                        //System.out.println("adding late requires:"+nameToAdd);
                    	if (!foundRequires.contains(nameToAdd))
                    		foundRequires.add(nameToAdd);
                    }
	    		}
	    		else if (line.indexOf(JSGoogEmitterTokens.GOOG_PROVIDE.getToken()) != -1)
	    		{
	    			provideIndex = i;
	    		}
    		}
    		finalLines.add(line);
    		i++;
    	}
        boolean needXML = ((FlexJSProject)(((IMXMLBlockWalker) getMXMLWalker()).getProject())).needXML;
        if (needXML && !foundRequires.contains(IASLanguageConstants.XML))
        {
    		foundRequires.add(IASLanguageConstants.XML);
    		depsAdded = true;
        }
    	// append info() structure if main CU
        ICompilerProject project = getMXMLWalker().getProject();
        if (project instanceof FlexJSProject)
        {
            FlexJSProject flexJSProject = (FlexJSProject) project;
        	if (flexJSProject.mainCU != null)
        	{
	            String mainDef = null;
				try {
					mainDef = flexJSProject.mainCU.getQualifiedNames().get(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            String thisDef = documentDefinition.getQualifiedName();
	            if (mainDef != null && mainDef.equals(thisDef))
	            {
	            	String infoInject = "\n\n" + thisDef + ".prototype.info = function() {\n" +
					"  return { ";
	            	String sep = "";
	            	Set<String> mixins = flexJSProject.mixinClassNames;
	            	if (mixins.size() > 0)
	            	{
		            	String mixinInject = "mixins: [";
		            	boolean firstOne = true;
		            	for (String mixin : mixins)
		            	{
		            		if (!firstOne)
		            			mixinInject += ", "; 
		            		mixinInject += mixin;
		            		firstOne = false;
		                    foundRequires.add(mixin);
		                    depsAdded = true;
	                        //addLineToMappings(finalLines.size());
		            	}
		            	mixinInject += "]";
		            	infoInject += mixinInject;
		            	sep = ",\n";
	                    //addLineToMappings(finalLines.size());	            	
	            	}
	            	boolean isMX = false;
	            	List<ISWC> swcs = flexJSProject.getLibraries();
	            	for (ISWC swc : swcs)
	            	{
	            		if (swc.getSWCFile().getName().equalsIgnoreCase("MX.swc"))
	            		{
	            			isMX = true;
	            			break;
	            		}
	            	}
	            	if (isMX)
	            	{
	            		MXMLDocumentNode mxmlDoc = (MXMLDocumentNode)documentDefinition.getNode();
	            		if (mxmlDoc != null)
	            		{
	            			MXMLFileNode mxmlFile = (MXMLFileNode)mxmlDoc.getParent();
	            			if (mxmlFile != null)
	            			{
	            				ITargetAttributes attrs = mxmlFile.getTargetAttributes(flexJSProject);
	            				if (attrs != null && attrs.getUsePreloader() != null)
	            				{
	            					String preloaderInject = sep + IMXMLLanguageConstants.ATTRIBUTE_USE_PRELOADER + ": ";
	            					preloaderInject += attrs.getUsePreloader() == Boolean.TRUE ? "true" : "false";
	        		            	sep = ",\n";
	        		            	infoInject += preloaderInject;
	            				}
	            			}
	            		}
	            	}
	            	infoInject += "}};";
                    finalLines.add(infoInject);
	            }
            }
        }
        if (foundRequires.size() > 0)
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append(JSGoogEmitterTokens.FLEXJS_DEPENDENCY_LIST.getToken());
            sb.append(Joiner.on(",").join(foundRequires));
            sb.append("*/\n");
            if (endRequires == -1)
            	finalLines.add(provideIndex + 1, sb.toString());
            else
            	finalLines.set(endRequires, sb.toString());

        }
    	return Joiner.on("\n").join(finalLines);
    }
    
    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitDeclarations(IMXMLDeclarationsNode node)
    {
    	inMXMLContent = true;
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("i");

        MXMLDescriptorSpecifier currentPropertySpecifier = new MXMLDescriptorSpecifier();
        currentPropertySpecifier.isProperty = true;
        currentPropertySpecifier.name = "mxmlContent";
        currentPropertySpecifier.parent = currentInstance;
        descriptorTree.add(currentPropertySpecifier);
        moveDown(false, currentInstance, currentPropertySpecifier);
    	super.emitDeclarations(node);
        moveUp(false, false);
    	inMXMLContent = false;
    }
    
    @Override
    public void emitDocument(IMXMLDocumentNode node)
    {
        descriptorTree = new ArrayList<MXMLDescriptorSpecifier>();
        propertiesTree = new MXMLDescriptorSpecifier();

        events = new ArrayList<MXMLEventSpecifier>();
        instances = new ArrayList<MXMLDescriptorSpecifier>();
        scripts = new ArrayList<MXMLScriptSpecifier>();
        //styles = new ArrayList<MXMLStyleSpecifier>();

        currentInstances = new ArrayList<MXMLDescriptorSpecifier>();
        currentStateOverrides = new MXMLDescriptorSpecifier();
        currentPropertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();

        eventCounter = 0;
        idCounter = 0;
        bindingCounter = 0;
        
        // visit MXML
        IClassDefinition cdef = node.getClassDefinition();
        classDefinition = cdef;
        documentDefinition = cdef;

        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        fjs.getModel().setCurrentClass(cdef);

        // visit tags
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i));
        }

        String cname = node.getFileNode().getName();

        emitHeader(node);

        emitClassDeclStart(cname, node.getBaseClassName(), false);

        emitComplexInitializers(node);

        emitPropertyDecls();
        
        emitClassDeclEnd(cname, node.getBaseClassName());

     //   emitMetaData(cdef);

        write(subDocuments.toString());
        writeNewline();

        emitScripts();

        fjs.getBindableEmitter().emit(cdef);
        fjs.getAccessorEmitter().emit(cdef);

        emitEvents(cname);
        emitComplexStaticInitializers(node);
        emitPropertyGetterSetters(cname);

        emitMXMLDescriptorFuncs(cname);

        emitBindingData(cname, cdef);

        emitMetaData(cdef);
    }

    public void emitSubDocument(IMXMLComponentNode node)
    {
        ArrayList<MXMLDescriptorSpecifier> oldDescriptorTree;
        MXMLDescriptorSpecifier oldPropertiesTree;
        ArrayList<MXMLEventSpecifier> oldEvents;
        ArrayList<MXMLScriptSpecifier> oldScripts;
        ArrayList<MXMLDescriptorSpecifier> oldCurrentInstances;
        ArrayList<MXMLDescriptorSpecifier> oldInstances;
        ArrayList<MXMLDescriptorSpecifier> oldCurrentPropertySpecifiers;
        int oldEventCounter;
        int oldIdCounter;
        boolean oldInMXMLContent;
        
        oldDescriptorTree = descriptorTree;
        descriptorTree = new ArrayList<MXMLDescriptorSpecifier>();
        oldPropertiesTree = propertiesTree;
        propertiesTree = new MXMLDescriptorSpecifier();

        oldInMXMLContent = inMXMLContent;
        inMXMLContent = false;
        oldEvents = events;
        events = new ArrayList<MXMLEventSpecifier>();
        oldInstances = instances;
        instances = new ArrayList<MXMLDescriptorSpecifier>();
        oldScripts = scripts;
        scripts = new ArrayList<MXMLScriptSpecifier>();
        //styles = new ArrayList<MXMLStyleSpecifier>();

        oldCurrentInstances = currentInstances;
        currentInstances = new ArrayList<MXMLDescriptorSpecifier>();
        oldCurrentPropertySpecifiers = currentPropertySpecifiers;
        currentPropertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();

        oldEventCounter = eventCounter;
        eventCounter = 0;
        oldIdCounter = idCounter;
        idCounter = 0;

        // visit MXML
        IClassDefinition oldClassDef = classDefinition;
        IClassDefinition cdef = node.getContainedClassDefinition();
        classDefinition = cdef;
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();
        ((JSFlexJSEmitter) asEmitter).getModel().pushClass(cdef);
        
        IASNode classNode = node.getContainedClassDefinitionNode();
        String cname = cdef.getQualifiedName();
        String baseClassName = cdef.getBaseClassAsDisplayString();
        subDocumentNames.add(cname);

        // visit tags
        final int len = classNode.getChildCount();
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(classNode.getChild(i));
        }

        ((JSFlexJSEmitter) asEmitter).mxmlEmitter = this;

        emitClassDeclStart(cname, baseClassName, false);

        emitComplexInitializers(classNode);
        
        emitPropertyDecls();
        
        emitClassDeclEnd(cname, baseClassName);

        emitMetaData(cdef);

        emitScripts();

        emitEvents(cname);

        emitPropertyGetterSetters(cname);

        emitMXMLDescriptorFuncs(cname);

        emitBindingData(cname, cdef);

        write(((JSFlexJSEmitter) asEmitter).stringifyDefineProperties(cdef));


        descriptorTree = oldDescriptorTree;
        propertiesTree = oldPropertiesTree;
        events = oldEvents;
        scripts = oldScripts;
        currentInstances = oldCurrentInstances;
        allInstances.addAll(instances);
        instances = oldInstances;
        currentPropertySpecifiers = oldCurrentPropertySpecifiers;
        eventCounter = oldEventCounter;
        idCounter = oldIdCounter;
        inMXMLContent = oldInMXMLContent;
        classDefinition = oldClassDef;
        ((JSFlexJSEmitter) asEmitter).getModel().popClass();
        ((JSFlexJSEmitter) asEmitter).mxmlEmitter = null;

    }

    @Override
    public void emitMetadata(IMXMLMetadataNode node)
    {
        metadataNodes.add(node);
    }

    //--------------------------------------------------------------------------

    protected void emitClassDeclStart(String cname, String baseClassName,
            boolean indent)
    {
        writeNewline();
        writeNewline("/**");
        writeNewline(" * @constructor");
        writeNewline(" * @extends {" + formatQualifiedName(baseClassName) + "}");
        writeNewline(" */");
        writeToken(formatQualifiedName(cname));
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        if (indent)
            indentPush();
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);
        write(formatQualifiedName(cname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSGoogEmitterTokens.GOOG_BASE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.THIS);
        writeToken(ASEmitterTokens.COMMA);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(JSGoogEmitterTokens.GOOG_CONSTRUCTOR);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);        
    }

    //--------------------------------------------------------------------------

    protected void emitClassDeclEnd(String cname, String baseClassName)
    {
        writeNewline();
        writeNewline("/**");
        writeNewline(" * @private");
        writeNewline(" * @type {Array}");
        writeNewline(" */");
        writeNewline("this.mxmldd;");

        // top level is 'mxmlContent', skip it...
        if (currentStateOverrides.propertySpecifiers.size() > 0)
        {
            MXMLDescriptorSpecifier root = currentStateOverrides;
            root.isTopNode = true;
    
	        writeNewline("/**");
	        writeNewline(" * @export");
	        writeNewline(" * @type {Array}");
	        writeNewline(" */");
	        writeNewline("this.mxmlsd = " + ASEmitterTokens.SQUARE_OPEN.getToken());
	        indentPush();
	        write(root.outputStateDescriptors());
	        write("null");
	        write(ASEmitterTokens.SQUARE_CLOSE);
	        indentPop();
	        writeNewline(ASEmitterTokens.SEMICOLON);
        }
        
        writeNewline();
        writeNewline("/**");
        writeNewline(" * @private");
        writeNewline(" * @type {Array}");
        writeNewline(" */");

        indentPop();
        writeNewline("this.mxmldp;");

        if (propertiesTree.propertySpecifiers.size() > 0 ||
                propertiesTree.eventSpecifiers.size() > 0)
        {
            indentPush();
            writeNewline();
            writeNewline("this.generateMXMLAttributes");
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.SQUARE_OPEN);
    
            MXMLDescriptorSpecifier root = propertiesTree;
            root.isTopNode = true;
            writeNewline(root.output(true));
    
            write(ASEmitterTokens.SQUARE_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
            indentPop();
            writeNewline();
        }

        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        write(JSGoogEmitterTokens.GOOG_INHERITS);
        write(ASEmitterTokens.PAREN_OPEN);
        write(formatQualifiedName(cname));
        writeToken(ASEmitterTokens.COMMA);
        write(formatQualifiedName(baseClassName));
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
        writeNewline();
        writeNewline();
    }

    //--------------------------------------------------------------------------

    protected void emitMetaData(IClassDefinition cdef)
    {
        String cname = cdef.getQualifiedName();
        
        writeNewline("/**");
        writeNewline(" * Metadata");
        writeNewline(" *");
        writeNewline(" * @type {Object.<string, Array.<Object>>}");
        writeNewline(" */");
        write(formatQualifiedName(cname) + ".prototype.FLEXJS_CLASS_INFO = { names: [{ name: '");
        write(cdef.getBaseName());
        write("', qName: '");
        write(formatQualifiedName(cname));
        write("'");
        writeToken(ASEmitterTokens.COMMA);
        write(JSFlexJSEmitterTokens.FLEXJS_CLASS_INFO_KIND);
        writeToken(ASEmitterTokens.COLON);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(JSFlexJSEmitterTokens.FLEXJS_CLASS_INFO_CLASS_KIND);
        writeToken(ASEmitterTokens.SINGLE_QUOTE);
        write(" }]");
        if (interfaceList != null)
        {
        	write(", interfaces: [");
        	write(interfaceList);
        	write("]");
        }
        write(" };");
        
	    writeNewline();
	    writeNewline();
	    writeNewline();
        writeNewline("/**");
	    writeNewline(" * Prevent renaming of class. Needed for reflection.");
        writeNewline(" */");
	    write(JSFlexJSEmitterTokens.GOOG_EXPORT_SYMBOL);
	    write(ASEmitterTokens.PAREN_OPEN);
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(formatQualifiedName(cname));
	    write(ASEmitterTokens.SINGLE_QUOTE);
	    write(ASEmitterTokens.COMMA);
	    write(ASEmitterTokens.SPACE);
	    write(formatQualifiedName(cname));
	    write(ASEmitterTokens.PAREN_CLOSE);
	    write(ASEmitterTokens.SEMICOLON);

        emitReflectionData(cdef);
        writeNewline();
        writeNewline();
        
    }
    
    private void emitReflectionData(IClassDefinition cdef)
    {
        JSFlexJSEmitter asEmitter = (JSFlexJSEmitter)((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
        FlexJSProject fjs = (FlexJSProject) getMXMLWalker().getProject();
    	ArrayList<String> exportProperties = new ArrayList<String>();
    	ArrayList<String> exportSymbols = new ArrayList<String>();
    	Set<String> exportMetadata = Collections.<String> emptySet();
    	if (fjs.config != null)
    		exportMetadata = fjs.config.getCompilerKeepCodeWithMetadata();
        ArrayList<PackageFooterEmitter.VariableData> varData = new ArrayList<PackageFooterEmitter.VariableData>();
        // vars can only come from script blocks?
        List<IVariableNode> vars = asEmitter.getModel().getVars();
        for (IVariableNode varNode : vars)
        {
            String ns = varNode.getNamespace();
            if (ns == IASKeywordConstants.PUBLIC && !varNode.isConst())
            {
            	PackageFooterEmitter.VariableData data = asEmitter.packageFooterEmitter.new VariableData();
            	varData.add(data);
            	data.name = varNode.getName();
                data.isStatic = varNode.hasModifier(ASModifier.STATIC);
                String qualifiedTypeName =	varNode.getVariableTypeNode().resolveType(getMXMLWalker().getProject()).getQualifiedName();
        	    data.type = (qualifiedTypeName);
        	    IMetaTagsNode metaData = varNode.getMetaTags();
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

        ArrayList<PackageFooterEmitter.AccessorData> accessorData = new ArrayList<PackageFooterEmitter.AccessorData>();
        HashMap<String, PropertyNodes> accessors = asEmitter.getModel().getPropertyMap();
        //instance accessors
        collectAccessors(accessors,accessorData,cdef);
        accessors = asEmitter.getModel().getStaticPropertyMap();
        //static accessors
        collectAccessors(accessors,accessorData,cdef);

        //additional bindables
        HashMap<String, BindableVarInfo> bindableVars = asEmitter.getModel().getBindableVars();
        for (String varName : bindableVars.keySet())
        {
            BindableVarInfo bindableVarInfo = bindableVars.get(varName);

            String ns = bindableVarInfo.namespace;
            if (ns == IASKeywordConstants.PUBLIC)
            {
                PackageFooterEmitter.AccessorData data = asEmitter.packageFooterEmitter.new AccessorData();
                accessorData.add(data);
                data.name = varName;
                data.isStatic = bindableVarInfo.isStatic;
                data.type = bindableVarInfo.type;
                data.declaredBy = cdef.getQualifiedName();
                data.access = "readwrite";
                if (bindableVarInfo.metaTags != null) {
                    if (bindableVarInfo.metaTags.length > 0)
                        data.metaData = bindableVarInfo.metaTags;
                }
            }
        }


        for (MXMLDescriptorSpecifier instance : instances)
        {
            if (!instance.id.startsWith(MXMLFlexJSEmitterTokens.ID_PREFIX
                    .getToken()))
            {
	        	PackageFooterEmitter.AccessorData data = asEmitter.packageFooterEmitter.new AccessorData();
	        	accessorData.add(data);
	        	data.name = instance.id;
	        	data.type = instance.name;
                data.access = "readwrite";
	    	    data.declaredBy = cdef.getQualifiedName();
            }	        	
        }
        ArrayList<PackageFooterEmitter.MethodData> methodData = new ArrayList<PackageFooterEmitter.MethodData>();
        List<IFunctionNode> methods = asEmitter.getModel().getMethods();




        for (IFunctionNode methodNode : methods)
        {
            String ns = methodNode.getNamespace();
            if (ns == IASKeywordConstants.PUBLIC)
            {
            	PackageFooterEmitter.MethodData data = asEmitter.packageFooterEmitter.new MethodData();
            	methodData.add(data);
            	data.name = methodNode.getName();
                String qualifiedTypeName =	methodNode.getReturnType();
                if (!(qualifiedTypeName.equals("") || qualifiedTypeName.equals("void"))) {
                    qualifiedTypeName = methodNode.getReturnTypeNode().resolveType(fjs).getQualifiedName();;
                }
                data.type = qualifiedTypeName;
        	    data.declaredBy = cdef.getQualifiedName();
                data.isStatic = methodNode.hasModifier(ASModifier.STATIC);
                IParameterNode[] paramNodes = methodNode.getParameterNodes();
                if (paramNodes != null && paramNodes.length > 0) {
                    data.parameters = paramNodes;
                }
        	    IMetaTagsNode metaData = methodNode.getMetaTags();
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

        if (cdef.getConstructor()==null) {
            //add a constructor description for the reflection data
            PackageFooterEmitter.MethodData data = asEmitter.packageFooterEmitter.new MethodData();
            methodData.add(data);
            data.name = cdef.getBaseName();
            data.type = "";
            data.isStatic = false;
            data.declaredBy = cdef.getQualifiedName();
        }


        for (MXMLEventSpecifier event : events)
        {
        	PackageFooterEmitter.MethodData data = asEmitter.packageFooterEmitter.new MethodData();
        	methodData.add(data);
        	data.name = event.eventHandler;
        	data.type = ASEmitterTokens.VOID.getToken();
    	    data.declaredBy = cdef.getQualifiedName();
        }
        ArrayList<IMetaTagNode> metadataTagNodes = new ArrayList<IMetaTagNode>();
        for (IMXMLMetadataNode metadataTag : metadataNodes)
        {
        	IMetaTagNode[] tags = metadataTag.getMetaTagNodes();
        	for (IMetaTagNode tag : tags)
        	{
        		metadataTagNodes.add(tag);
        	}
        }
        IMetaTagNode[] metaDataTags = new IMetaTagNode[metadataTagNodes.size()];

        asEmitter.packageFooterEmitter.emitReflectionData(
                formatQualifiedName(cdef.getQualifiedName()),
                PackageFooterEmitter.ReflectionKind.CLASS,
                varData,
        		accessorData,
                methodData,
                metadataTagNodes.toArray(metaDataTags));
        asEmitter.packageFooterEmitter.emitExportProperties(
                formatQualifiedName(cdef.getQualifiedName()),
                exportProperties,
                exportSymbols);
    }

    private void collectAccessors(HashMap<String, PropertyNodes> accessors, ArrayList<PackageFooterEmitter.AccessorData> accessorData,IClassDefinition cdef ) {
        JSFlexJSEmitter asEmitter = (JSFlexJSEmitter)((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
        FlexJSProject fjs = (FlexJSProject) getMXMLWalker().getProject();

        for (String propName : accessors.keySet())
        {
            PropertyNodes p = accessors.get(propName);

            IFunctionNode accessorNode = p.getter;
            if (accessorNode == null)
                accessorNode = p.setter;
            String ns = accessorNode.getNamespace();
            if (ns == IASKeywordConstants.PUBLIC)
            {
                PackageFooterEmitter.AccessorData data = asEmitter.packageFooterEmitter.new AccessorData();
                accessorData.add(data);
                data.name = accessorNode.getName();

                data.isStatic = accessorNode.hasModifier(ASModifier.STATIC);
                if (p.getter != null)
                {
                    data.type = p.getter.getReturnTypeNode().resolveType(fjs).getQualifiedName();
                    if (p.setter !=null) {
                        data.access = "readwrite";
                    } else data.access = "readonly";
                }
                else
                {
                    data.type = p.setter.getVariableTypeNode().resolveType(fjs).getQualifiedName();
                    data.access = "writeonly";
                }

                data.declaredBy = (cdef.getQualifiedName());
                IMetaTagsNode metaData = accessorNode.getMetaTags();
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

    //--------------------------------------------------------------------------

    protected void emitPropertyDecls()
    {
        for (MXMLDescriptorSpecifier instance : instances)
        {
            writeNewline();
            writeNewline("/**");
            writeNewline(" * @private");
            writeNewline(" * @type {" + instance.name + "}");
            writeNewline(" */");
            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(instance.id + "_");
            writeNewline(ASEmitterTokens.SEMICOLON);
        }
    }

    //--------------------------------------------------------------------------

    protected void emitBindingData(String cname, IClassDefinition cdef)
    {
        BindingDatabase bd = BindingDatabase.bindingMap.get(cdef);
        if (bd == null)
            return;
        if (bd.getBindingInfo().isEmpty())
            return;

        outputBindingInfoAsData(cname, bd);
    }

    private void outputBindingInfoAsData(String cname, BindingDatabase bindingDataBase)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
        .getASEmitter();

        writeNewline("/**");
        writeNewline(" * @export");
        writeNewline(" */");
        writeNewline(formatQualifiedName(cname)
                + ".prototype._bindings = [");
        
        Set<BindingInfo> bindingInfo = bindingDataBase.getBindingInfo();
        writeNewline(bindingInfo.size() + ","); // number of bindings
        
        for (BindingInfo bi : bindingInfo)
        {
            String s;
            s = bi.getSourceString();
            if (s == null && bi.isSourceSimplePublicProperty())
                s = getSourceStringFromGetter(bi.getExpressionNodesForGetter());
            if (s == null || s.length() == 0)
            {
                List<IExpressionNode> getterNodes = bi.getExpressionNodesForGetter();
                StringBuilder sb = new StringBuilder();
                sb.append("function() { return ");
                int n = getterNodes.size();
                for (int i = 0; i < n; i++)
                {
                	IExpressionNode getterNode = getterNodes.get(i);
                	if (getterNode.getNodeID() == ASTNodeID.LiteralStringID)
                	{
                		sb.append(ASEmitterTokens.DOUBLE_QUOTE.getToken());
                		sb.append(asEmitter.stringifyNode(getterNode));
                		sb.append(ASEmitterTokens.DOUBLE_QUOTE.getToken());
                	}
                	else
                		sb.append(asEmitter.stringifyNode(getterNode));
                    if (i < n - 1)
                    	sb.append(ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.PLUS.getToken() + ASEmitterTokens.SPACE.getToken());
                }
                sb.append("; },");
                writeNewline(sb.toString());
            }
            else if (s.contains("."))
            {
            	if (bi.classDef != null)
            	{
	                String[] parts = s.split("\\.");
	                write(ASEmitterTokens.SQUARE_OPEN.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() +
	                        bi.classDef.getQualifiedName() + ASEmitterTokens.DOUBLE_QUOTE.getToken());
	                usedNames.add(bi.classDef.getQualifiedName());
	                int n = parts.length;
	                for (int i = 1; i < n; i++)
	                {
	                    String part = parts[i];
	                    write(", " +  ASEmitterTokens.DOUBLE_QUOTE.getToken() + part + ASEmitterTokens.DOUBLE_QUOTE.getToken());
	                }
	                writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());            		
            	}
            	else
            	{
	                String[] parts = s.split("\\.");
	                write(ASEmitterTokens.SQUARE_OPEN.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() +
	                        parts[0] + ASEmitterTokens.DOUBLE_QUOTE.getToken());
	                int n = parts.length;
	                for (int i = 1; i < n; i++)
	                {
	                    String part = parts[i];
	                    write(", " +  ASEmitterTokens.DOUBLE_QUOTE.getToken() + part + ASEmitterTokens.DOUBLE_QUOTE.getToken());
	                }
	                writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
            	}
            }
            else
                writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + s + 
                        ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
            
            IExpressionNode destNode = bi.getExpressionNodeForDestination();
            if (destNode != null)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(generateSetterFunction(destNode));
                writeNewline(sb.toString() + ASEmitterTokens.COMMA.getToken());
            }
            else
                writeNewline(ASEmitterTokens.NULL.getToken() + ASEmitterTokens.COMMA.getToken());
            
            s = bi.getDestinationString();
            if (s == null)
            {
                writeNewline(ASEmitterTokens.NULL.getToken() + ASEmitterTokens.COMMA.getToken());            	
            }
            else if (s.contains("."))
            {
                String[] parts = s.split("\\.");
                write(ASEmitterTokens.SQUARE_OPEN.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() + 
                        parts[0] + ASEmitterTokens.DOUBLE_QUOTE.getToken());
                int n = parts.length;
                for (int i = 1; i < n; i++)
                {
                    String part = parts[i];
                    write(", " + ASEmitterTokens.DOUBLE_QUOTE.getToken() + part + ASEmitterTokens.DOUBLE_QUOTE.getToken());
                }
                writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
            }
            else
                writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + s +
                        ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
        }
        Set<Entry<Object, WatcherInfoBase>> watcherChains = bindingDataBase.getWatcherChains();

        if (watcherChains != null)
        {
            int count = watcherChains.size();
            for (Entry<Object, WatcherInfoBase> entry : watcherChains)
            {
                count--;
                WatcherInfoBase watcherInfoBase = entry.getValue();
                encodeWatcher(watcherInfoBase);
                if (count > 0) writeNewline(ASEmitterTokens.COMMA);
            }
        }

        writeNewline( ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.SEMICOLON.getToken());
    }

    private String generateSetterFunction(IExpressionNode destNode) {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
        	.getASEmitter();
		String body = asEmitter.stringifyNode(destNode);
        
		StringBuilder sb = new StringBuilder();
		sb.append("function (value) { ");
		int lastGet = body.lastIndexOf("get_");
		int lastDot = body.lastIndexOf(".");
		if (lastDot == lastGet - 1)
		{
			String object = body.substring(0, lastDot);
			String getter = body.substring(lastDot);
			String setter = getter.replace("get_", "set_");
			setter = setter.replace("()", "(value)");
			body = object + setter;
			sb.append(body);
		}
		else
		{
			sb.append(body);
			sb.append(" = value;");
		}
		sb.append(";}");
		return sb.toString();
	}

	private void encodeWatcher(WatcherInfoBase watcherInfoBase)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
        .getASEmitter();

        writeNewline(watcherInfoBase.getIndex() + ASEmitterTokens.COMMA.getToken());
        WatcherType type = watcherInfoBase.getType();
        if (type == WatcherType.FUNCTION)
        {
            writeNewline("0" + ASEmitterTokens.COMMA.getToken());

            FunctionWatcherInfo functionWatcherInfo = (FunctionWatcherInfo)watcherInfoBase;
           
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + functionWatcherInfo.getFunctionName() + 
                    ASEmitterTokens.DOUBLE_QUOTE.getToken());
            IExpressionNode params[] = functionWatcherInfo.params;
            StringBuilder sb = new StringBuilder();
            sb.append("function() { return [");
            boolean firstone = true;
            for (IExpressionNode param : params)
            {
                if (firstone)
                    firstone = false;
                sb.append(ASEmitterTokens.COMMA.getToken());
                sb.append(asEmitter.stringifyNode(param));   
            }
            sb.append("]; },");
            outputEventNames(functionWatcherInfo.getEventNames());
            outputBindings(functionWatcherInfo.getBindings());
        }
        else if ((type == WatcherType.STATIC_PROPERTY) || (type == WatcherType.PROPERTY))
        {
            writeNewline((type == WatcherType.STATIC_PROPERTY ? "1" : "2") + 
                    ASEmitterTokens.COMMA.getToken());

            PropertyWatcherInfo propertyWatcherInfo = (PropertyWatcherInfo)watcherInfoBase;
           
            boolean makeStaticWatcher = (watcherInfoBase.getType() == WatcherType.STATIC_PROPERTY);
            
            // round up the getter function for the watcher, or null if we don't need one
            MethodInfo propertyGetterFunction = null;
            if (watcherInfoBase.isRoot && !makeStaticWatcher)
            {
                // TODO: figure out what this looks like
                // propertyGetterFunction = this.propertyGetter;
                // assert propertyGetterFunction != null;
            }
            else if (watcherInfoBase.isRoot && makeStaticWatcher)
            {
                 // TODO: implement getter func for static watcher.
            }
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + propertyWatcherInfo.getPropertyName() +
                    ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
            outputEventNames(propertyWatcherInfo.getEventNames());
            outputBindings(propertyWatcherInfo.getBindings());
            if (propertyGetterFunction == null)
                writeNewline("null" + ASEmitterTokens.COMMA.getToken()); // null is valid
            if (type == WatcherType.STATIC_PROPERTY)
            {
                StaticPropertyWatcherInfo pwinfo = (StaticPropertyWatcherInfo)watcherInfoBase;
                Name classMName = pwinfo.getContainingClass(getMXMLWalker().getProject());
                writeNewline(nameToString(classMName)+ ASEmitterTokens.COMMA.getToken());
            }
        }
        else if (type == WatcherType.XML)
        {
            writeNewline("3" + ASEmitterTokens.COMMA.getToken());

            XMLWatcherInfo xmlWatcherInfo = (XMLWatcherInfo)watcherInfoBase;
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + xmlWatcherInfo.getPropertyName() +
                    ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
            outputBindings(xmlWatcherInfo.getBindings());
        }
        else assert false;     

        // then recurse into children
        Set<Entry<Object, WatcherInfoBase>> children = watcherInfoBase.getChildren();
        if (children != null)
        {
            writeNewline(ASEmitterTokens.SQUARE_OPEN.getToken());
            for ( Entry<Object, WatcherInfoBase> ent : children)
            {
                encodeWatcher(ent.getValue());
                writeNewline(ASEmitterTokens.COMMA);
            }
            write("null" + ASEmitterTokens.SQUARE_CLOSE.getToken() );
        }
        else
        {
            write("null" );
        }
    }
    
    private String getSourceStringFromMemberAccessExpressionNode(MemberAccessExpressionNode node)
    {
        String s = "";
        
        IExpressionNode left = node.getLeftOperandNode();
        if (left instanceof FunctionCallNode) //  probably a cast
        {
            IASNode child = ((FunctionCallNode)left).getArgumentsNode().getChild(0);
            if (child instanceof IdentifierNode)
                s = getSourceStringFromIdentifierNode((IdentifierNode)child);
            else if (child instanceof MemberAccessExpressionNode)
                s = getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)child);
        }
        else if (left instanceof MemberAccessExpressionNode)
            s = getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)left);
        else if (left instanceof IdentifierNode)
            s = getSourceStringFromIdentifierNode((IdentifierNode)left);
        else
            System.out.println("expected binding member access left node" + node.toString());
        s += ".";
        
        IExpressionNode right = node.getRightOperandNode();
        if (right instanceof FunctionCallNode) //  probably a cast
        {
            IASNode child = ((FunctionCallNode)right).getArgumentsNode().getChild(0);
            if (child instanceof IdentifierNode)
                s += getSourceStringFromIdentifierNode((IdentifierNode)child);
            else if (child instanceof MemberAccessExpressionNode)
                s += getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)child);
        }
        else if (right instanceof MemberAccessExpressionNode)
            s += getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)right);
        else if (right instanceof IdentifierNode)
            s += getSourceStringFromIdentifierNode((IdentifierNode)right);
        else
            System.out.println("expected binding member access right node" + node.toString());
        
        return s;
    }
    
    private String getSourceStringFromIdentifierNode(IdentifierNode node)
    {
        return node.getName();
    }
    
    private String getSourceStringFromGetter(List<IExpressionNode> nodes)
    {
        String s = "";
        IExpressionNode node = nodes.get(0);
        if (node instanceof MemberAccessExpressionNode)
        {
            s = getSourceStringFromMemberAccessExpressionNode((MemberAccessExpressionNode)node);
        }
        else if (node instanceof IdentifierNode)
        {
            s = ((IdentifierNode)node).getName();
        }
        return s;
    }
    
    private void outputEventNames(List<String> events)
    {
        if (events.size() > 1)
        {
            int n = events.size();
            write(ASEmitterTokens.SQUARE_OPEN.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() +
                    events.get(0) + ASEmitterTokens.DOUBLE_QUOTE.getToken());
            for (int i = 1; i < n; i++)
            {
                String event = events.get(i);
                write(ASEmitterTokens.COMMA.getToken() + ASEmitterTokens.DOUBLE_QUOTE.getToken() + 
                        event + ASEmitterTokens.DOUBLE_QUOTE.getToken());
            }
            writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
        }
        else if (events.size() == 1)
            writeNewline(ASEmitterTokens.DOUBLE_QUOTE.getToken() + events.get(0) +
                    ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
        else
            writeNewline("null" + ASEmitterTokens.COMMA.getToken());
    }
    
    private void outputBindings(List<BindingInfo> bindings)
    {
        if (bindings.size() > 1)
        {
            int n = bindings.size();
            write(ASEmitterTokens.SQUARE_OPEN.getToken() + bindings.get(0).getIndex());
            for (int i = 1; i < n; i++)
            {
                BindingInfo binding = bindings.get(i);
                write(ASEmitterTokens.COMMA.getToken() + binding.getIndex());
            }
            writeNewline(ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.COMMA.getToken());
        }
        else if (bindings.size() == 1)
            writeNewline(bindings.get(0).getIndex() + ASEmitterTokens.COMMA.getToken());
        else
            writeNewline("null" + ASEmitterTokens.COMMA.getToken());
        
    }

    //--------------------------------------------------------------------------    

    protected void emitScripts()
    {
        for (MXMLScriptSpecifier script : scripts)
        {
            String output = script.output();

            if (!output.equals(""))
            {
                writeNewline(output);
            }
        }
    }

    //--------------------------------------------------------------------------    

    protected void emitEvents(String cname)
    {
        for (MXMLEventSpecifier event : events)
        {
            writeNewline("/**");
            writeNewline(" * @export");
            writeNewline(" * @param {" + formatQualifiedName(event.type) + "} event");
            writeNewline(" */");
            writeNewline(formatQualifiedName(cname)
                    + ".prototype." + event.eventHandler + " = function(event)");
            writeNewline(ASEmitterTokens.BLOCK_OPEN, true);

            writeNewline(event.value + ASEmitterTokens.SEMICOLON.getToken(),
                    false);

            write(ASEmitterTokens.BLOCK_CLOSE);
            writeNewline(";");
            writeNewline();
            writeNewline();
        }
    }

    //--------------------------------------------------------------------------    

    protected void emitPropertyGetterSetters(String cname)
    {
    	int n = 0;
        for (MXMLDescriptorSpecifier instance : instances)
        {
            if (!instance.id.startsWith(MXMLFlexJSEmitterTokens.ID_PREFIX
                    .getToken()))
            {
            	n++;
            }
        }
    	if (n == 0 && descriptorTree.size() == 0)
    		return;
    	
    	String formattedCName = formatQualifiedName(cname);
    	
    	write("Object.defineProperties(");
    	write(formattedCName);
    	writeNewline(".prototype, /** @lends {" + formattedCName + ".prototype} */ {");
        indentPush();
        int i = 0;
        for (MXMLDescriptorSpecifier instance : instances)
        {
            if (!instance.id.startsWith(MXMLFlexJSEmitterTokens.ID_PREFIX
                    .getToken()))
            {
                indentPush();
                writeNewline("/** @export */");
                writeNewline(instance.id + ": {");
                writeNewline("/** @this {" + formattedCName + "} */");
                indentPush();
                writeNewline("get: function() {");
                indentPop();
                writeNewline("return this." + instance.id + "_;");
                writeNewline("},");
                writeNewline("/** @this {" + formattedCName + "} */");
                indentPush();
                writeNewline("set: function(value) {");
                indentPush();
                writeNewline("if (value != this." + instance.id + "_) {");
                writeNewline("this." + instance.id + "_ = value;");
                write("this.dispatchEvent(org.apache.flex.events.ValueChangeEvent.createUpdateEvent(this, '");
                indentPop();
                writeNewline(instance.id + "', null, value));");
                indentPop();
                writeNewline("}");
                indentPop();
                writeNewline("}");
                if (i < n - 1 || descriptorTree.size() > 0)
                	writeNewline("},");
                else
                {
                    indentPop();
                    writeNewline("}");
                }
                i++;
            }
        }
        if (descriptorTree.size() == 0)
        	writeNewline("});");
    }

    //--------------------------------------------------------------------------    

    protected void emitMXMLDescriptorFuncs(String cname)
    {
        // top level is 'mxmlContent', skip it...
        if (descriptorTree.size() > 0)
        {
            FlexJSProject project = (FlexJSProject) getMXMLWalker().getProject();
            project.needLanguage = true;
            MXMLDescriptorSpecifier root = descriptorTree.get(0);
            root.isTopNode = false;
    
            indentPush();
            writeNewline("'MXMLDescriptor': {");
            writeNewline("/** @this {" + formatQualifiedName(cname) + "} */");
            indentPush();
            writeNewline("get: function() {");
            indentPush();
            writeNewline("{");
            writeNewline("if (this.mxmldd == undefined)");
            indentPush();
            writeNewline("{");
            writeNewline("/** @type {Array} */");
            writeNewline("var arr = " + formatQualifiedName(cname) + ".superClass_.get__MXMLDescriptor.apply(this);");
            writeNewline("/** @type {Array} */");
            indentPop();
            indentPop();
            writeNewline("var data = [");
    
            writeNewline(root.output(true));
    
            indentPush();
            writeNewline("];");
            indentPush();
            writeNewline("");
            indentPush();
            writeNewline("if (arr)");
            indentPop();
            writeNewline("this.mxmldd = arr.concat(data);");
            indentPush();
            writeNewline("else");
            indentPop();
            indentPop();
            writeNewline("this.mxmldd = data;");
            writeNewline("}");
            indentPop();
            writeNewline("return this.mxmldd;");
            writeNewline("}");
            indentPop();
            writeNewline("}");
            indentPop();
            writeNewline("}");
        	writeNewline("});");
        }
   
    }

    //--------------------------------------------------------------------------    

    private HashMap<IMXMLEventSpecifierNode, String> eventHandlerNameMap = new HashMap<IMXMLEventSpecifierNode, String>();
    
    @Override
    public void emitEventSpecifier(IMXMLEventSpecifierNode node)
    {
    	if (isStateDependent(node) && !inStatesOverride)
    		return;
    	
        IDefinition cdef = node.getDefinition();

        MXMLDescriptorSpecifier currentDescriptor = getCurrentDescriptor("i");

        MXMLEventSpecifier eventSpecifier = new MXMLEventSpecifier();
        eventSpecifier.eventHandler = MXMLFlexJSEmitterTokens.EVENT_PREFIX
                .getToken() + eventCounter++;
        eventSpecifier.name = cdef.getBaseName();
        eventSpecifier.type = node.getEventParameterDefinition()
                .getTypeAsDisplayString();

        eventHandlerNameMap.put(node, eventSpecifier.eventHandler);
        
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        StringBuilder sb = null;
        int len = node.getChildCount();
        if (len > 0)
        {
            sb = new StringBuilder();
            for (int i = 0; i < len; i++)
            {
                sb.append(getIndent((i > 0) ? 1 : 0)
                        + asEmitter.stringifyNode(node.getChild(i)));
                if (i < len - 1)
                {
                    sb.append(ASEmitterTokens.SEMICOLON.getToken());
                    sb.append(ASEmitterTokens.NEW_LINE.getToken());
                }
            }
        }
        eventSpecifier.value = sb.toString();

	    if (currentDescriptor != null)
	        currentDescriptor.eventSpecifiers.add(eventSpecifier);
	    else if (!inStatesOverride) // in theory, if no currentdescriptor must be top tag event
	        propertiesTree.eventSpecifiers.add(eventSpecifier);
        events.add(eventSpecifier);
    }

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
        if (isStateDependent(node) && !inStatesOverride)
            return;
        
        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) getMXMLWalker()
                        .getProject());

        MXMLDescriptorSpecifier currentPropertySpecifier = getCurrentDescriptor("ps");

        String id = node.getID();
        if (id == null)
            id = node.getEffectiveID();
        if (id == null)
            id = MXMLFlexJSEmitterTokens.ID_PREFIX.getToken() + idCounter++;

        MXMLDescriptorSpecifier currentInstance = new MXMLDescriptorSpecifier();
        currentInstance.isProperty = false;
        currentInstance.id = id;
        currentInstance.name = formatQualifiedName(cdef.getQualifiedName());
        currentInstance.parent = currentPropertySpecifier;

        if (currentPropertySpecifier != null)
            currentPropertySpecifier.propertySpecifiers.add(currentInstance);
        else if (inMXMLContent)
            descriptorTree.add(currentInstance);
        else
        {
            currentInstance.parent = propertiesTree;
            propertiesTree.propertySpecifiers.add(currentInstance);
        }

        instances.add(currentInstance);

        IMXMLPropertySpecifierNode[] pnodes = node.getPropertySpecifierNodes();
        if (pnodes != null)
        {
            moveDown(false, currentInstance, null);

            for (IMXMLPropertySpecifierNode pnode : pnodes)
            {
                getMXMLWalker().walk(pnode); // Property Specifier
            }

            moveUp(false, true);
        }
        else if (node instanceof IMXMLStateNode)
        {
            IMXMLStateNode stateNode = (IMXMLStateNode)node;
            String name = stateNode.getStateName();
            if (name != null)
            {
                MXMLDescriptorSpecifier stateName = new MXMLDescriptorSpecifier();
                stateName.isProperty = true;
                stateName.id = id;
                stateName.name = "name";
                stateName.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + name + ASEmitterTokens.SINGLE_QUOTE.getToken();
                stateName.parent = currentInstance;
                currentInstance.propertySpecifiers.add(stateName);
            }
            MXMLDescriptorSpecifier overrides = new MXMLDescriptorSpecifier();
            overrides.isProperty = true;
            overrides.hasArray = true;
            overrides.id = id;
            overrides.name = "overrides";
            overrides.parent = currentInstance;
            currentInstance.propertySpecifiers.add(overrides);
            moveDown(false, null, overrides);

            IMXMLClassDefinitionNode classDefinitionNode = stateNode.getClassDefinitionNode();
            List<IMXMLNode> snodes = classDefinitionNode.getNodesDependentOnState(stateNode.getStateName());
            if (snodes != null)
            {
                for (int i=snodes.size()-1; i>=0; --i)
                {
                    IMXMLNode inode = snodes.get(i);
                    if (inode.getNodeID() == ASTNodeID.MXMLInstanceID)
                    {
                        emitInstanceOverride((IMXMLInstanceNode)inode);
                    }
                }
                // Next process the non-instance overrides dependent on this state.
                // Each one will generate code to push an IOverride instance.
                for (IMXMLNode anode : snodes)
                {
                    switch (anode.getNodeID())
                    {
                        case MXMLPropertySpecifierID:
                        {
                            emitPropertyOverride((IMXMLPropertySpecifierNode)anode);
                            break;
                        }
                        case MXMLStyleSpecifierID:
                        {
                            emitStyleOverride((IMXMLStyleSpecifierNode)anode);
                            break;
                        }
                        case MXMLEventSpecifierID:
                        {
                            emitEventOverride((IMXMLEventSpecifierNode)anode);
                            break;
                        }
                        default:
                        {
                            break;
                        }
                    }
                }
            }
            
            moveUp(false, false);
        }

        IMXMLEventSpecifierNode[] enodes = node.getEventSpecifierNodes();
        if (enodes != null)
        {
            moveDown(false, currentInstance, null);

            for (IMXMLEventSpecifierNode enode : enodes)
            {
                getMXMLWalker().walk(enode); // Event Specifier
            }

            moveUp(false, true);
        }
    }

    public void emitPropertyOverride(IMXMLPropertySpecifierNode propertyNode)
    {
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name propertyOverride = project.getPropertyOverrideClassName();
        emitPropertyOrStyleOverride(propertyOverride, propertyNode);
    }
    
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetStyle
     * with its <code>target</code>, <code>name</code>,
     * and <code>value</code> properties set.
     */
    void emitStyleOverride(IMXMLStyleSpecifierNode styleNode)
    {
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name styleOverride = project.getStyleOverrideClassName();
        emitPropertyOrStyleOverride(styleOverride, styleNode);
    }
    
    void emitPropertyOrStyleOverride(Name overrideName, IMXMLPropertySpecifierNode propertyOrStyleNode)
    {
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        IASNode parentNode = propertyOrStyleNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    null;
        
        String name = propertyOrStyleNode.getName();        
        
        boolean valueIsDataBound = isDataBindingNode(propertyOrStyleNode.getChild(0));
        IMXMLInstanceNode propertyOrStyleValueNode = propertyOrStyleNode.getInstanceNode();
        
        MXMLDescriptorSpecifier setProp = new MXMLDescriptorSpecifier();
        setProp.isProperty = false;
        setProp.name = formatQualifiedName(nameToString(overrideName));
        setProp.parent = currentInstance;
        currentInstance.propertySpecifiers.add(setProp);
        
        if (id != null)
        {
	            // Set its 'target' property to the id of the object
	            // whose property or style this override will set.
	        MXMLDescriptorSpecifier target = new MXMLDescriptorSpecifier();
	        target.isProperty = true;
	        target.name = "target";
	        target.parent = setProp;
	        target.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + id + ASEmitterTokens.SINGLE_QUOTE.getToken();
	        setProp.propertySpecifiers.add(target);
        }
        
            // Set its 'name' property to the name of the property or style.
        MXMLDescriptorSpecifier pname = new MXMLDescriptorSpecifier();
        pname.isProperty = true;
        pname.name = "name";
        pname.parent = setProp;
        pname.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + name + ASEmitterTokens.SINGLE_QUOTE.getToken();
        setProp.propertySpecifiers.add(pname);

        if (!valueIsDataBound)
        {
	            // Set its 'value' property to the value of the property or style.
	        MXMLDescriptorSpecifier value = new MXMLDescriptorSpecifier();
	        value.isProperty = true;
	        value.name = "value";
	        value.parent = setProp;
	        setProp.propertySpecifiers.add(value);
	        moveDown(false, null, value);
	        getMXMLWalker().walk(propertyOrStyleValueNode); // instance node
	        moveUp(false, false);
        }
        else
        {
            String overrideID = MXMLFlexJSEmitterTokens.BINDING_PREFIX.getToken() + bindingCounter++;
	        setProp.id = overrideID;
	        instances.add(setProp);
	        BindingDatabase bd = BindingDatabase.bindingMap.get(classDefinition);
	        Set<BindingInfo> bindingInfo = bd.getBindingInfo();
	        IMXMLDataBindingNode bindingNode = (IMXMLDataBindingNode)propertyOrStyleNode.getChild(0);
	        for (BindingInfo bi : bindingInfo)
	        {
	        	if (bi.node == bindingNode)
	        	{
	                bi.setDestinationString(overrideID + ".value");
	                break;
	        	}
	        }
        }
    }
        
    /**
     * Generates instructions in the current context
     * to create an instance of mx.states.SetEventHandler
     * with its <code>target</code>, <code>name</code>,
     * and <code>handlerFunction</code> properties set.
     */
    void emitEventOverride(IMXMLEventSpecifierNode eventNode)
    {
        inStatesOverride = true;
        
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name eventOverride = project.getEventOverrideClassName();
        
        IASNode parentNode = eventNode.getParent();
        String id = parentNode instanceof IMXMLInstanceNode ?
                    ((IMXMLInstanceNode)parentNode).getEffectiveID() :
                    "";
        
        String name = MXMLEventSpecifier.getJSEventName(eventNode.getName());
        
        String eventHandler = eventHandlerNameMap.get(eventNode);
        if (eventHandler == null)
        {
        	emitEventSpecifier(eventNode);
        	eventHandler = eventHandlerNameMap.get(eventNode);
        }

        MXMLDescriptorSpecifier setEvent = new MXMLDescriptorSpecifier();
        setEvent.isProperty = false;
        setEvent.name = formatQualifiedName(nameToString(eventOverride));
        setEvent.parent = currentInstance;
        currentInstance.propertySpecifiers.add(setEvent);
        // Set its 'target' property to the id of the object
        // whose event this override will set.
        MXMLDescriptorSpecifier target = new MXMLDescriptorSpecifier();
        target.isProperty = true;
        target.name = "target";
        target.parent = setEvent;
        target.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + id + ASEmitterTokens.SINGLE_QUOTE.getToken();
        setEvent.propertySpecifiers.add(target);

        // Set its 'name' property to the name of the event.
        MXMLDescriptorSpecifier pname = new MXMLDescriptorSpecifier();
        pname.isProperty = true;
        pname.name = "name";
        pname.parent = setEvent;
        pname.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + name + ASEmitterTokens.SINGLE_QUOTE.getToken();
        setEvent.propertySpecifiers.add(pname);
        
        // Set its 'handlerFunction' property to the autogenerated event handler.
        MXMLDescriptorSpecifier handler = new MXMLDescriptorSpecifier();
        handler.isProperty = true;
        handler.name = "handlerFunction";
        handler.parent = setEvent;
        handler.value = JSFlexJSEmitterTokens.CLOSURE_FUNCTION_NAME.getToken() + ASEmitterTokens.PAREN_OPEN.getToken() + 
        		ASEmitterTokens.THIS.getToken() + ASEmitterTokens.MEMBER_ACCESS.getToken() + eventHandler +
        		ASEmitterTokens.COMMA.getToken() + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.THIS.getToken() +
        		ASEmitterTokens.COMMA.getToken() + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.SINGLE_QUOTE.getToken() +
        			eventHandler + ASEmitterTokens.SINGLE_QUOTE.getToken() +
        		ASEmitterTokens.PAREN_CLOSE.getToken();
        setEvent.propertySpecifiers.add(handler);
        
        inStatesOverride = false;
    }

    public void emitInstanceOverride(IMXMLInstanceNode instanceNode)
    {
        inStatesOverride = true;
        
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        FlexProject project = (FlexProject) getMXMLWalker().getProject();
        Name instanceOverrideName = project.getInstanceOverrideClassName();

        MXMLDescriptorSpecifier overrideInstances = getCurrentDescriptor("so");
        int index = overrideInstances.propertySpecifiers.size();
        if (nodeToIndexMap == null)
        	nodeToIndexMap = new HashMap<IMXMLNode, Integer>();
        if (nodeToIndexMap.containsKey(instanceNode))
        {
        	index = nodeToIndexMap.get(instanceNode);
        }
        else
        {
        	nodeToIndexMap.put(instanceNode, index);
            MXMLDescriptorSpecifier itemsDesc = new MXMLDescriptorSpecifier();
            itemsDesc.isProperty = true;
            itemsDesc.hasArray = true;
            itemsDesc.name = "itemsDescriptor";
            itemsDesc.parent = overrideInstances;
            overrideInstances.propertySpecifiers.add(itemsDesc);
            boolean oldInMXMLContent = inMXMLContent;
            moveDown(false, null, itemsDesc);
            inMXMLContent = true;
            getMXMLWalker().walk(instanceNode); // instance node
            inMXMLContent = oldInMXMLContent;
            moveUp(false, false);
        }

        MXMLDescriptorSpecifier addItems = new MXMLDescriptorSpecifier();
        addItems.isProperty = false;
        addItems.name = formatQualifiedName(nameToString(instanceOverrideName));
        addItems.parent = currentInstance;
        currentInstance.propertySpecifiers.add(addItems);
        MXMLDescriptorSpecifier itemsDescIndex = new MXMLDescriptorSpecifier();
        itemsDescIndex.isProperty = true;
        itemsDescIndex.hasArray = true;
        itemsDescIndex.name = "itemsDescriptorIndex";
        itemsDescIndex.parent = addItems;
        itemsDescIndex.value = Integer.toString(index);
        addItems.propertySpecifiers.add(itemsDescIndex);
        
        //-----------------------------------------------------------------------------
        // Second property set: maybe set destination and propertyName
        
        // get the property specifier node for the property the instanceNode represents
        IMXMLPropertySpecifierNode propertySpecifier = (IMXMLPropertySpecifierNode) 
            instanceNode.getAncestorOfType( IMXMLPropertySpecifierNode.class);
    
        if (propertySpecifier == null)
        {
           assert false;        // I think this indicates an invalid tree...
        }
        else
        {
            // Check the parent - if it's an instance then we want to use these
            // nodes to get our property values from. If not, then it's the root
            // and we don't need to specify destination
            
            IASNode parent = propertySpecifier.getParent();
            if (parent instanceof IMXMLInstanceNode)
            {
               IMXMLInstanceNode parentInstance = (IMXMLInstanceNode)parent;
               String parentId = parentInstance.getEffectiveID();
               assert parentId != null;
               String propName = propertySpecifier.getName();
               
               MXMLDescriptorSpecifier dest = new MXMLDescriptorSpecifier();
               dest.isProperty = true;
               dest.name = "destination";
               dest.parent = addItems;
               dest.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + parentId + ASEmitterTokens.SINGLE_QUOTE.getToken();
               addItems.propertySpecifiers.add(dest);

               MXMLDescriptorSpecifier prop = new MXMLDescriptorSpecifier();
               prop.isProperty = true;
               prop.name = "propertyName";
               prop.parent = addItems;
               prop.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + propName + ASEmitterTokens.SINGLE_QUOTE.getToken();
               addItems.propertySpecifiers.add(prop);
            }
        }  
        
        //---------------------------------------------------------------
        // Third property set: position and relativeTo
        String positionPropertyValue = null;
        String relativeToPropertyValue = null;
       
        // look to see if we have any sibling nodes that are not state dependent
        // that come BEFORE us
        IASNode instanceParent = instanceNode.getParent();
        IASNode prevStatelessSibling=null;
        for (int i=0; i< instanceParent.getChildCount(); ++i)
        {
            IASNode sib = instanceParent.getChild(i);
            assert sib instanceof IMXMLInstanceNode;    // surely our siblings are also instances?
           
            // stop looking for previous nodes when we find ourself
            if (sib == instanceNode)
                break;

            if (sib instanceof IMXMLInstanceNode && !isStateDependent(sib))
            {
                prevStatelessSibling = sib;
            }
        }
        
        if (prevStatelessSibling == null) {
            positionPropertyValue = "first";        // TODO: these should be named constants
        }
        else {
            positionPropertyValue = "after";
            relativeToPropertyValue = ((IMXMLInstanceNode)prevStatelessSibling).getEffectiveID();
        }
       
        MXMLDescriptorSpecifier pos = new MXMLDescriptorSpecifier();
        pos.isProperty = true;
        pos.name = "position";
        pos.parent = addItems;
        pos.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + positionPropertyValue + ASEmitterTokens.SINGLE_QUOTE.getToken();
        addItems.propertySpecifiers.add(pos);
        
        if (relativeToPropertyValue != null)
        {
            MXMLDescriptorSpecifier rel = new MXMLDescriptorSpecifier();
            rel.isProperty = true;
            rel.name = "relativeTo";
            rel.parent = addItems;
            rel.value = ASEmitterTokens.SINGLE_QUOTE.getToken() + relativeToPropertyValue + ASEmitterTokens.SINGLE_QUOTE.getToken();
            addItems.propertySpecifiers.add(rel);
        }
        
        inStatesOverride = false;
    }

    private String nameToString(Name name)
    {
        String s;
        Namespace ns = name.getSingleQualifier();
        s = ns.getName();
        if (s != "") s = s + ASEmitterTokens.MEMBER_ACCESS.getToken() + name.getBaseName();
        else s = name.getBaseName();
        return s;
    }
    /**
     * Determines whether a node is state-dependent.
     * TODO: we should move to IMXMLNode
     */
    protected boolean isStateDependent(IASNode node)
    {
        if (node instanceof IMXMLSpecifierNode)
        {
            String suffix = ((IMXMLSpecifierNode)node).getSuffix();
            return suffix != null && suffix.length() > 0;
        }
        else if (isStateDependentInstance(node))
            return true;
        return false;
    }
    
    /**
     * Determines whether the geven node is an instance node, as is state dependent
     */
    protected boolean isStateDependentInstance(IASNode node)
    {
        if (node instanceof IMXMLInstanceNode)
        {
            String[] includeIn = ((IMXMLInstanceNode)node).getIncludeIn();
            String[] excludeFrom = ((IMXMLInstanceNode)node).getExcludeFrom();
            return includeIn != null || excludeFrom != null;
        }
        return false;
    }
    
    /**
     * Is a give node a "databinding node"?
     */
    public static boolean isDataBindingNode(IASNode node)
    {
        return node instanceof IMXMLDataBindingNode;
    }
    
    protected static boolean isDataboundProp(IMXMLPropertySpecifierNode propertyNode)
    {
        boolean ret = propertyNode.getChildCount() > 0 && isDataBindingNode(propertyNode.getInstanceNode());
        
        // Sanity check that we based our conclusion about databinding on the correct node.
        // (code assumes only one child if databinding)
        int n = propertyNode.getChildCount();
        for (int i = 0; i < n; i++)
        {
            boolean db = isDataBindingNode(propertyNode.getChild(i));
            assert db == ret;
        }
        
        return ret;
    }

    @Override
    public void emitPropertySpecifier(IMXMLPropertySpecifierNode node)
    {
        if (isDataboundProp(node))
            return;
        
        if (isStateDependent(node))
            return;
        
        IDefinition cdef = node.getDefinition();

        IASNode cnode = node.getChild(0);

        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("i");

        MXMLDescriptorSpecifier currentPropertySpecifier = new MXMLDescriptorSpecifier();
        currentPropertySpecifier.isProperty = true;
        currentPropertySpecifier.name = cdef != null ? cdef.getQualifiedName() : node.getName();
        currentPropertySpecifier.parent = currentInstance;

        boolean oldInMXMLContent = inMXMLContent;
        boolean reusingDescriptor = false;
        if (currentPropertySpecifier.name.equals("mxmlContent"))
        {
            inMXMLContent = true;
            ArrayList<MXMLDescriptorSpecifier> specList = 
            	(currentInstance == null) ? descriptorTree : currentInstance.propertySpecifiers;
            for (MXMLDescriptorSpecifier ds : specList)
            {
            	if (ds.name.equals("mxmlContent"))
            	{
            		currentPropertySpecifier = ds;
            		reusingDescriptor = true;
            		break;
            	}
            }
        }
        
        if (currentInstance != null)
        {
        	// we end up here for children of tags
        	if (!reusingDescriptor)
        		currentInstance.propertySpecifiers.add(currentPropertySpecifier);
        }
        else if (inMXMLContent)
        {
        	// we end up here for top tags?
        	if (!reusingDescriptor)
        		descriptorTree.add(currentPropertySpecifier);
        }
        else
        {
            currentPropertySpecifier.parent = propertiesTree;
            propertiesTree.propertySpecifiers.add(currentPropertySpecifier);
        }

        boolean valueIsArray = cnode != null && cnode instanceof IMXMLArrayNode;
        boolean valueIsObject = cnode != null && cnode instanceof IMXMLObjectNode;

        currentPropertySpecifier.hasArray = valueIsArray;
        currentPropertySpecifier.hasObject = valueIsObject;

        moveDown(valueIsArray || valueIsObject, null, currentPropertySpecifier);

        getMXMLWalker().walk(cnode); // Array or Instance

        moveUp(valueIsArray || valueIsObject, false);
        
        inMXMLContent = oldInMXMLContent;
    }

    @Override
    public void emitScript(IMXMLScriptNode node)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        String nl = ASEmitterTokens.NEW_LINE.getToken();

        StringBuilder sb = null;
        MXMLScriptSpecifier scriptSpecifier = null;

        int len = node.getChildCount();
        if (len > 0)
        {
            for (int i = 0; i < len; i++)
            {
                IASNode cnode = node.getChild(i);
                if (cnode.getNodeID() == ASTNodeID.VariableID) {
                    ((JSFlexJSEmitter) asEmitter).getModel().getVars().add((IVariableNode) cnode);
                } else {
                    if (cnode.getNodeID() == ASTNodeID.BindableVariableID) {
                        IVariableNode variableNode = (IVariableNode) cnode;
                        BindableVarInfo bindableVarInfo = new BindableVarInfo();
                        bindableVarInfo.isStatic = variableNode.hasModifier(ASModifier.STATIC);;
                        bindableVarInfo.namespace = variableNode.getNamespace();
                        IMetaTagsNode metaTags = variableNode.getMetaTags();
                        if (metaTags != null) {
                            IMetaTagNode[] tags = metaTags.getAllTags();
                            if (tags.length > 0)
                                bindableVarInfo.metaTags = tags;
                        }

                        bindableVarInfo.type = variableNode.getVariableTypeNode().resolveType(getMXMLWalker().getProject()).getQualifiedName();
                        ((JSFlexJSEmitter) asEmitter).getModel().getBindableVars().put(variableNode.getName(), bindableVarInfo);
                    }
                }

                if (!(cnode instanceof IImportNode))
                {
                    sb = new StringBuilder();
                    scriptSpecifier = new MXMLScriptSpecifier();

                    sb.append(asEmitter.stringifyNode(cnode));

                    sb.append(ASEmitterTokens.SEMICOLON.getToken());

                    if (i == len - 1)
                        indentPop();

                    sb.append(nl);
                    sb.append(nl);

                    scriptSpecifier.fragment = sb.toString();

                    scripts.add(scriptSpecifier);
                }
            }
        }
    }

    @Override
    public void emitStyleSpecifier(IMXMLStyleSpecifierNode node)
    {
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitObject(IMXMLObjectNode node)
    {
        final int len = node.getChildCount();
    	if (!makingSimpleArray)
    	{
            MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
            if (ps.hasObject)
            {
            	emitInstance(node);
            	return;
            }
            for (int i = 0; i < len; i++)
            {
                getMXMLWalker().walk(node.getChild(i)); // props in object
            }    		
    	}
    	else
    	{
            MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
            if (ps.value == null)
            	ps.value = "";
            ps.value += "{";	
            for (int i = 0; i < len; i++)
            {
                IMXMLPropertySpecifierNode propName = (IMXMLPropertySpecifierNode)node.getChild(i);
                ps.value += propName.getName() + ": ";	                
                getMXMLWalker().walk(propName.getChild(0));
                if (i < len - 1)
                    ps.value += ", ";	                                	
            }    		
            ps.value += "}";	
    	}
    }
    
    @Override
    public void emitArray(IMXMLArrayNode node)
    {
        moveDown(false, null, null);

        boolean isSimple = true;
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            final IASNode child = node.getChild(i);
            ASTNodeID nodeID = child.getNodeID();
            if (nodeID == ASTNodeID.MXMLArrayID || nodeID == ASTNodeID.MXMLInstanceID || nodeID == ASTNodeID.MXMLStateID)
            {
                isSimple = false;
                break;
            }
        }
        boolean oldMakingSimpleArray = makingSimpleArray;
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        if (isSimple)
        {
        	makingSimpleArray = true;
        	ps.value = ASEmitterTokens.SQUARE_OPEN.getToken();
        }
        for (int i = 0; i < len; i++)
        {
            getMXMLWalker().walk(node.getChild(i)); // Instance
            if (isSimple && i < len - 1)
            	ps.value += ASEmitterTokens.COMMA.getToken();
        }
        if (isSimple)
        {
        	ps.value += ASEmitterTokens.SQUARE_CLOSE.getToken();        	
        }
        makingSimpleArray = oldMakingSimpleArray;

        moveUp(false, false);
    }

    @Override
    public void emitString(IMXMLStringNode node)
    {
        getCurrentDescriptor("ps").valueNeedsQuotes = true;

        emitAttributeValue(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitLiteral(IMXMLLiteralNode node)
    {
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        if (ps.value == null) // might be non-null if makingSimpleArray
        	ps.value = "";

        if (ps.valueNeedsQuotes)
            ps.value += ASEmitterTokens.SINGLE_QUOTE.getToken();

        String s = node.getValue().toString();
        if (ps.valueNeedsQuotes)
        {
            // escape all single quotes found within the string
            s = s.replace(ASEmitterTokens.SINGLE_QUOTE.getToken(), 
                    "\\" + ASEmitterTokens.SINGLE_QUOTE.getToken());
        }
        s = s.replace("\r\n", "\\n");
        s = s.replace("\n", "\\n");
        ps.value += s;
        
        if (ps.valueNeedsQuotes)
            ps.value += ASEmitterTokens.SINGLE_QUOTE.getToken();
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitFactory(IMXMLFactoryNode node)
    {
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = "new " + formatQualifiedName("org.apache.flex.core.ClassFactory") + "(";

        IASNode cnode = node.getChild(0);
        if (cnode instanceof IMXMLClassNode)
        {
            ps.value += formatQualifiedName(((IMXMLClassNode)cnode).getValue(getMXMLWalker().getProject()).getQualifiedName());
        }
        ps.value += ")";
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitComponent(IMXMLComponentNode node)
    {
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = "new " + formatQualifiedName("org.apache.flex.core.ClassFactory") + "(";

        ps.value += formatQualifiedName(documentDefinition.getQualifiedName()) + ".";
        ps.value += formatQualifiedName(node.getName());
        ps.value += ")";
        
        setBufferWrite(true);
        emitSubDocument(node);
        subDocuments.append(getBuilder().toString());
        getBuilder().setLength(0);
        setBufferWrite(false);
    }

    @Override
    protected void setBufferWrite(boolean value)
    {
    	super.setBufferWrite(value);
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
        ((JSFlexJSEmitter)asEmitter).setBufferWrite(value);
    }
    
    //--------------------------------------------------------------------------
    //    JS output
    //--------------------------------------------------------------------------
    
    private void emitHeader(IMXMLDocumentNode node)
    {
        String cname = node.getFileNode().getName();
        String bcname = node.getBaseClassName();

        FlexJSProject project = (FlexJSProject) getMXMLWalker().getProject();
        List<File> sourcePaths = project.getSourcePath();
        String sourceName = node.getSourcePath();
        for (File sourcePath : sourcePaths)
        {
            if (sourceName.startsWith(sourcePath.getAbsolutePath())) 
            {
            	sourceName = sourceName.substring(sourcePath.getAbsolutePath().length() + 1);        	
            }
        }
        writeNewline("/**");
        writeNewline(" * Generated by Apache Flex Cross-Compiler from " + sourceName);
        writeNewline(" * " + cname);
        writeNewline(" *");
        writeNewline(" * @fileoverview");
        writeNewline(" *");
        writeNewline(" * @suppress {checkTypes|accessControls}");
        writeNewline(" */");
        writeNewline();
        
        ArrayList<String> writtenInstances = new ArrayList<String>();
        emitHeaderLine(cname, true); // provide
        for (String subDocumentName : subDocumentNames)
        {
            emitHeaderLine(subDocumentName, true);
            writtenInstances.add(formatQualifiedName(subDocumentName));
        }
        writeNewline();
        emitHeaderLine(bcname);
        writtenInstances.add(formatQualifiedName(cname)); // make sure we don't add ourselves
        writtenInstances.add(formatQualifiedName(bcname)); // make sure we don't add the baseclass twice
        allInstances.addAll(0, instances);
        
        ASProjectScope projectScope = (ASProjectScope) project.getScope();
        IDefinition cdef = node.getDefinition();
        ICompilationUnit cu = projectScope
                .getCompilationUnitForDefinition(cdef);
        ArrayList<String> deps = project.getRequires(cu);
        for (MXMLDescriptorSpecifier instance : allInstances)
        {
            String name = instance.name;
            deps.add(name);
        }

        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();
        if (fjs.getModel().hasStaticBindableVars()) {
            //we need to add EventDispatcher
            if (deps.indexOf(BindableEmitter.DISPATCHER_CLASS_QNAME) == -1)
                deps.add(BindableEmitter.DISPATCHER_CLASS_QNAME);
            if (usedNames.indexOf(BindableEmitter.DISPATCHER_CLASS_QNAME) == -1)
                usedNames.add(BindableEmitter.DISPATCHER_CLASS_QNAME);
        }

        if (interfaceList != null)
        {
        	String[] interfaces = interfaceList.split(", ");
        	for (String iface : interfaces)
        	{
        		while (deps.contains(iface))
        			deps.remove(iface);
        		usedNames.add(iface);
                if (writtenInstances.indexOf(iface) == -1)
                {
                    emitHeaderLine(iface);
                    writtenInstances.add(iface);
                }
        	}
        }
        boolean firstDependency = true;
        boolean depsAdded = false;
    	StringBuilder sb = new StringBuilder();
    	sb.append(JSGoogEmitterTokens.FLEXJS_DEPENDENCY_LIST.getToken());
        if (deps != null)
        {
        	Collections.sort(deps);
            for (String imp : deps)
            {
                if (imp.indexOf(JSGoogEmitterTokens.AS3.getToken()) != -1)
                    continue;
    
                if (imp.equals(cname))
                    continue;
    
                if (imp.equals("mx.binding.Binding"))
                    continue;
                if (imp.equals("mx.binding.BindingManager"))
                    continue;
                if (imp.equals("mx.binding.FunctionReturnWatcher"))
                    continue;
                if (imp.equals("mx.binding.PropertyWatcher"))
                    continue;
                if (imp.equals("mx.binding.StaticPropertyWatcher"))
                    continue;
                if (imp.equals("mx.binding.XMLWatcher"))
                    continue;
                if (imp.equals("mx.events.PropertyChangeEvent"))
                    continue;
                if (imp.equals("mx.events.PropertyChangeEventKind"))
                    continue;
                if (imp.equals("mx.core.DeferredInstanceFromFunction"))
                    continue;
    
                if (NativeUtils.isNative(imp))
                    continue;
    
                String formatted = formatQualifiedName(imp, false);
                if (writtenInstances.indexOf(formatted) == -1)
                {
            		if (!firstDependency)
            			sb.append(",");
                    sb.append(formatted);
                    writtenInstances.add(formatted);
                    firstDependency = false;
                    depsAdded = true;
                }
            }
        }

        // erikdebruin: Add missing language feature support, like the 'is' and 
        //              'as' operators. We don't need to worry about requiring
        //              this in every project: ADVANCED_OPTIMISATIONS will NOT
        //              include any of the code if it is not used in the project.
        if (project.mainCU != null &&
                cu.getName().equals(project.mainCU.getName()))
        {
            if (project instanceof FlexJSProject)
            {
            	if (((FlexJSProject)project).needLanguage)
            	{
            		if (!firstDependency)
            			sb.append(",");
            		sb.append(JSFlexJSEmitterTokens.LANGUAGE_QNAME.getToken());
                    depsAdded = true;
            	}
            }
        }
        sb.append("*/\n");
        if (depsAdded)
        {
        	write(sb.toString());
        }
        writeNewline();
        writeNewline();
    }

    private void emitHeaderLine(String qname)
    {
        emitHeaderLine(qname, false);
    }

    private void emitHeaderLine(String qname, boolean isProvide)
    {
        write((isProvide) ? JSGoogEmitterTokens.GOOG_PROVIDE
                : JSGoogEmitterTokens.GOOG_REQUIRE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(formatQualifiedName(qname, false));
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.SEMICOLON);
    }

    //--------------------------------------------------------------------------
    //    Utils
    //--------------------------------------------------------------------------

    @Override
    protected void emitAttributeValue(IASNode node)
    {
        IMXMLLiteralNode cnode = (IMXMLLiteralNode) node.getChild(0);

        if (cnode.getValue() != null)
            getMXMLWalker().walk((IASNode) cnode); // Literal
    }

    private MXMLDescriptorSpecifier getCurrentDescriptor(String type)
    {
        MXMLDescriptorSpecifier currentDescriptor = null;

        int index;

        if (type.equals("i"))
        {
            index = currentInstances.size() - 1;
            if (index > -1)
                currentDescriptor = currentInstances.get(index);
        }
        else if (type.equals("so"))
        {
            return currentStateOverrides;
        }
        else
        {
            index = currentPropertySpecifiers.size() - 1;
            if (index > -1)
                currentDescriptor = currentPropertySpecifiers.get(index);
        }

        return currentDescriptor;
    }

    protected void moveDown(boolean byPass,
            MXMLDescriptorSpecifier currentInstance,
            MXMLDescriptorSpecifier currentPropertySpecifier)
    {
        if (!byPass)
        {
            if (currentInstance != null)
                currentInstances.add(currentInstance);
        }

        if (currentPropertySpecifier != null)
            currentPropertySpecifiers.add(currentPropertySpecifier);
    }

    protected void moveUp(boolean byPass, boolean isInstance)
    {
        if (!byPass)
        {
            int index;

            if (isInstance)
            {
                index = currentInstances.size() - 1;
                if (index > -1)
                    currentInstances.remove(index);
            }
            else
            {
                index = currentPropertySpecifiers.size() - 1;
                if (index > -1)
                    currentPropertySpecifiers.remove(index);
            }
        }
    }

    public String formatQualifiedName(String name)
    {
    	return formatQualifiedName(name, true);
    }
    
    protected String formatQualifiedName(String name, boolean useName)
    {
    	/*
    	if (name.contains("goog.") || name.startsWith("Vector."))
    		return name;
    	name = name.replaceAll("\\.", "_");
    	*/
    	if (subDocumentNames.contains(name))
    		return documentDefinition.getQualifiedName() + "." + name;
        if (NativeUtils.isJSNative(name)) return name;
		if (useName && !usedNames.contains(name))
			usedNames.add(name);
     	return name;
    }

    private void emitComplexInitializers(IASNode node)
    {
    	int n = node.getChildCount();
    	for (int i = 0; i < n; i++)
    	{
    		IASNode child = node.getChild(i);
    		if (child.getNodeID() == ASTNodeID.MXMLScriptID)
    		{
    			int m = child.getChildCount();
    			for (int j = 0; j < m; j++)
    			{
    				IASNode schild = child.getChild(j);
    				ASTNodeID schildID = schild.getNodeID();
    				if (schildID == ASTNodeID.VariableID ||
    						schildID == ASTNodeID.BindableVariableID)
    				{
    					IVariableNode varnode = (IVariableNode)schild;
    			        IExpressionNode vnode = varnode.getAssignedValueNode();


                        if (vnode != null && (!EmitterUtils.isScalar(vnode)))
    			        {
    	                    writeNewline();
    	                    write(ASEmitterTokens.THIS);
    	                    write(ASEmitterTokens.MEMBER_ACCESS);
    	                    write(varnode.getName());
    	                    if (schildID == ASTNodeID.BindableVariableID && !varnode.isConst())
    	                    	write("_"); // use backing variable
    	                    write(ASEmitterTokens.SPACE);
    	                    writeToken(ASEmitterTokens.EQUAL);
    	                    JSFlexJSEmitter fjs = (JSFlexJSEmitter) ((IMXMLBlockWalker) getMXMLWalker())
    	                    .getASEmitter();
    	                    fjs.getWalker().walk(vnode);
    	                    write(ASEmitterTokens.SEMICOLON);

    			        }
    				}
    			}
    		}
    	}
    }

    public void emitComplexStaticInitializers(IASNode node){
        JSFlexJSEmitter fjs = (JSFlexJSEmitter) ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        if (!fjs.getFieldEmitter().hasComplexStaticInitializers) return;
        int n = node.getChildCount();
        boolean sawOutput = false;
        for (int i = 0; i < n; i++)
        {
            IASNode child = node.getChild(i);
            if (child.getNodeID() == ASTNodeID.MXMLScriptID)
            {
                int m = child.getChildCount();
                for (int j = 0; j < m; j++)
                {
                    IASNode schild = child.getChild(j);
                    ASTNodeID schildID = schild.getNodeID();
                    if (schildID == ASTNodeID.VariableID ||
                            schildID == ASTNodeID.BindableVariableID)
                    {
                        sawOutput = fjs.getFieldEmitter().emitFieldInitializer((IVariableNode) schild) || sawOutput;
                    }
                }
            }
        }

        if (sawOutput) {
            writeNewline();
            writeNewline();
        }
    }
    
    @Override
    public void emitImplements(IMXMLImplementsNode node)
    {
    	StringBuilder list = new StringBuilder();
    	boolean needsComma = false;
        IIdentifierNode[] interfaces = node.getInterfaceNodes();
        for (IIdentifierNode iface : interfaces)
        {
        	if (needsComma)
        		list.append(", ");
        	list.append(iface.getName());
        	needsComma = true;
        }
        //System.out.println("mxml implements "+list);
        interfaceList = list.toString();
    }
    
}
