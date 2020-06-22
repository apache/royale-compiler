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

package org.apache.royale.compiler.internal.codegen.mxml.royale;


import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.OneOperandInstruction;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.IMappingEmitter;
import org.apache.royale.compiler.codegen.mxml.royale.IMXMLRoyaleEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.as.codegen.InstructionListNode;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.databinding.BindingDatabase;
import org.apache.royale.compiler.internal.codegen.databinding.BindingInfo;
import org.apache.royale.compiler.internal.codegen.databinding.FunctionWatcherInfo;
import org.apache.royale.compiler.internal.codegen.databinding.PropertyWatcherInfo;
import org.apache.royale.compiler.internal.codegen.databinding.StaticPropertyWatcherInfo;
import org.apache.royale.compiler.internal.codegen.databinding.WatcherInfoBase;
import org.apache.royale.compiler.internal.codegen.databinding.WatcherInfoBase.WatcherType;
import org.apache.royale.compiler.internal.codegen.databinding.XMLWatcherInfo;
import org.apache.royale.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.PropertyNodes;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.BindableVarInfo;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.codegen.mxml.MXMLEmitter;
import org.apache.royale.compiler.internal.codegen.mxml.MXMLEmitterTokens;
import org.apache.royale.compiler.internal.driver.js.royale.JSCSSCompilationSession;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.targets.ITargetAttributes;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLDocumentNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLFileNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLFunctionNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLBindingNode;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.problems.FileNotFoundProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.tree.mxml.*;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.utils.DefinitionUtils;
import org.apache.royale.compiler.utils.NativeUtils;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;
import org.apache.royale.swc.ISWC;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.debugging.sourcemap.FilePosition;

/**
 * @author Erik de Bruin
 */
public class MXMLRoyaleEmitter extends MXMLEmitter implements
        IMXMLRoyaleEmitter, IMappingEmitter
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
    private ArrayList<IMXMLScriptNode> scripts;
    //private ArrayList<MXMLStyleSpecifier> styles;
    private IClassDefinition classDefinition;
    private IClassDefinition documentDefinition;
    private ArrayList<String> usedNames = new ArrayList<String>();
    private ArrayList<String> staticUsedNames = new ArrayList<String>();
    private ArrayList<IMXMLMetadataNode> metadataNodes = new ArrayList<IMXMLMetadataNode>();
    // separately track all fx:Declarations that are primitive types (fx:String, fx:Array)
    private ArrayList<IMXMLInstanceNode> primitiveDeclarationNodes = new ArrayList<IMXMLInstanceNode>();

    private int eventCounter;
    private int idCounter;
    private int bindingCounter;

    private boolean inMXMLContent;
    private IMXMLInstanceNode overrideInstanceToEmit;
    private Stack<IMXMLStateNode> inStatesOverride = new Stack<IMXMLStateNode>();
    private boolean makingSimpleArray;
    private boolean inStaticInitializer;

    private StringBuilder subDocuments = new StringBuilder();
    private ArrayList<String> subDocumentNames = new ArrayList<String>();
    private String interfaceList;
    private boolean emitExports = true;

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

    private SourceMapMapping lastMapping;

    private List<SourceMapMapping> sourceMapMappings;

    public List<SourceMapMapping> getSourceMapMappings()
    {
        return sourceMapMappings;
    }

    public MXMLRoyaleEmitter(FilterWriter out)
    {
        super(out);
        sourceMapMappings = new ArrayList<SourceMapMapping>();
    }

    @Override
    public String postProcess(String output)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
        ArrayList<String> asEmitterUsedNames = ((JSRoyaleEmitter)asEmitter).usedNames;
        JSRoyaleEmitter fjs = (JSRoyaleEmitter)asEmitter;

        String currentClassName = fjs.getModel().getCurrentClass().getQualifiedName();
        ArrayList<String> removals = new ArrayList<String>();
        for (String usedName : asEmitterUsedNames) {
            //remove any internal component that has been registered with the other emitter's usedNames
            if (usedName.startsWith(currentClassName+".") && subDocumentNames.contains(usedName.substring(currentClassName.length()+1))) {
                removals.add(usedName);
            }
        }
        for (String usedName : removals)
        {
        	asEmitterUsedNames.remove(usedName);
        }
        RoyaleJSProject fjp = (RoyaleJSProject) getMXMLWalker().getProject();
        if (fjp.config == null || fjp.config.isVerbose())
        {
            System.out.println(currentClassName + " as: " + asEmitterUsedNames.toString());
            System.out.println(currentClassName + " mxml: " + usedNames.toString());
        }
        usedNames.addAll(asEmitterUsedNames);
        
        ArrayList<String> asStaticEmitterUsedNames = ((JSRoyaleEmitter)asEmitter).staticUsedNames;
        removals = new ArrayList<String>();
        for (String usedName : asStaticEmitterUsedNames) {
            //remove any internal component that has been registered with the other emitter's usedNames
            if (usedName.startsWith(currentClassName+".") && subDocumentNames.contains(usedName.substring(currentClassName.length()+1))) {
                removals.add(usedName);
            }
        }
        for (String usedName : removals)
        {
        	asStaticEmitterUsedNames.remove(usedName);
        }
        if (fjp.config == null || fjp.config.isVerbose())
        {
            System.out.println(currentClassName + " as: " + asStaticEmitterUsedNames.toString());
            System.out.println(currentClassName + " mxml: " + staticUsedNames.toString());
        }
        staticUsedNames.addAll(asStaticEmitterUsedNames);
        

        boolean foundXML = false;
    	String[] lines = output.split("\n");
    	ArrayList<String> finalLines = new ArrayList<String>();
    	int endRequires = -1;
    	boolean sawRequires = false;
    	boolean stillSearching = true;
        int provideIndex = -1;
        ArrayList<String> namesToAdd = new ArrayList<String>();
        ArrayList<String> foundRequires = new ArrayList<String>();
        int len = lines.length;
        for (int i = 0; i < len; i++)
        {
            String line = lines[i];
    		if (stillSearching)
    		{
                if (provideIndex == -1 || !sawRequires)
                {
                    int c = line.indexOf(JSGoogEmitterTokens.GOOG_PROVIDE.getToken());
                    if (c != -1)
                    {
                        // if zero requires are found, require Language after the
                        // call to goog.provide
                        provideIndex = i + 1;
                    }
                }
	            int c = line.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	            if (c > -1)
	            {
	                int c2 = line.indexOf(")");
	                String s = line.substring(c + 14, c2 - 1);
                    if (s.equals(IASLanguageConstants.XML))
                    {
                        foundXML = true;
                    }
	    			sawRequires = true;
                    foundRequires.add(s);
	    			if (!usedNames.contains(s))
                    {
                        removeLineFromMappings(i);
                        continue;
                    }
	    		}
	    		else if (sawRequires)
	    		{
	    	    	// append info() structure if main CU
	    	        ICompilerProject project = getMXMLWalker().getProject();
    	            RoyaleJSProject royaleProject = null;
	    	        if (project instanceof RoyaleJSProject)
	    	            royaleProject = (RoyaleJSProject) project;

	    			stillSearching = false;
                    for (String usedName :usedNames) {
                        if (!foundRequires.contains(usedName)) {
                            if (usedName.equals(classDefinition.getQualifiedName())) continue;
                            if (((JSRoyaleEmitter) asEmitter).getModel().isInternalClass(usedName)) continue;
                            if (subDocumentNames.contains(usedName)) continue;
                            if (royaleProject != null)
                            {
                                if (!isGoogProvided(usedName))
                                {
                                    continue;
                                }
                            	ICompilationUnit cu = royaleProject.resolveQNameToCompilationUnit(usedName);
                                if (cu == null)
                                {
                                    System.out.println("didn't find CompilationUnit for " + usedName);
                                }
                            }
                            namesToAdd.add(usedName);
                        }
                    }

                    for (String nameToAdd : namesToAdd) {
                        finalLines.add(createRequireLine(nameToAdd,false));
                        addLineToMappings(i);
                    }

	    			endRequires = finalLines.size();
	    		}
    		}
    		finalLines.add(line);
    	}
        boolean needXML = ((RoyaleJSProject)(((IMXMLBlockWalker) getMXMLWalker()).getProject())).needXML;
        if (needXML && !foundXML)
        {
            StringBuilder appendString = new StringBuilder();
            appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
            appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
            appendString.append(IASLanguageConstants.XML);
            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
            appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
            appendString.append(ASEmitterTokens.SEMICOLON.getToken());
            finalLines.add(endRequires, appendString.toString());
            addLineToMappings(endRequires);
            endRequires++;
        }
    	// append info() structure if main CU
        ICompilerProject project = getMXMLWalker().getProject();
        if (project instanceof RoyaleJSProject)
        {
            RoyaleJSProject royaleProject = (RoyaleJSProject) project;
        	if (royaleProject.mainCU != null)
        	{
	            String mainDef = null;
				try {
					mainDef = royaleProject.mainCU.getQualifiedNames().get(0);
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
	            	Set<String> mixins = royaleProject.mixinClassNames;
	            	if (mixins.size() > 0)
	            	{
		            	String mixinInject = "\"mixins\": [";
		            	boolean firstOne = true;
		            	for (String mixin : mixins)
		            	{
                            if (!isGoogProvided(mixin))
                            {
                                continue;
                            }
                            if (!firstOne)
                            {
                                mixinInject += ", ";
                            }
		            		mixinInject += mixin;
		            		firstOne = false;
		                    StringBuilder appendString = new StringBuilder();
		                    appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
		                    appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		                    appendString.append(mixin);
		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		                    appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
		                    appendString.append(ASEmitterTokens.SEMICOLON.getToken());
	                        finalLines.add(endRequires, appendString.toString());
	                        addLineToMappings(endRequires);
                            endRequires++;
		            	}
		            	mixinInject += "]";
		            	infoInject += mixinInject;
		            	sep = ",\n";
	            	}
	            	Map<String, String> aliases = royaleProject.remoteClassAliasMap;
	            	if (aliases != null && aliases.size() > 0)
	            	{
		            	String aliasInject = sep + "\"remoteClassAliases\": {";
		            	boolean firstOne = true;
		            	for (String className : aliases.keySet())
		            	{
                            if (!isGoogProvided(className))
                            {
                                continue;
                            }
                            if (!firstOne)
                            {
                                aliasInject += ", ";
                            }
		            		aliasInject += "\"" + className + "\": ";
		            		String alias = aliases.get(className);
		            		aliasInject += "\"" + alias + "\"";
		            		firstOne = false;
		                    StringBuilder appendString = new StringBuilder();
		                    appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
		                    appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		                    appendString.append(className);
		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		                    appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
		                    appendString.append(ASEmitterTokens.SEMICOLON.getToken());
	                        finalLines.add(endRequires, appendString.toString());
	                        addLineToMappings(endRequires);
                            endRequires++;
		            	}
		            	aliasInject += "}";
		            	infoInject += aliasInject;
		            	sep = ",\n";
	            	}
	                Collection<String> locales = royaleProject.getLocales();
	                if (locales.size() > 0)
	                {
		            	String localeInject = sep + "\"compiledLocales\": [";
		            	boolean firstOne = true;
		            	String[] localeNames = new String[locales.size()];
		            	locales.toArray(localeNames);
		            	for (String locale : localeNames)
		            	{
                            if (!firstOne)
                            {
                                localeInject += ", ";
                            }
		            		localeInject += "\"" + locale + "\"";
		            		firstOne = false;
		            	}
		            	localeInject += "]";
		            	infoInject += localeInject;
		            	sep = ",\n";

	                }
	                List<String> bundles = royaleProject.compiledResourceBundleNames;
	                if (bundles.size() > 0)
	                {
		            	String bundleInject = sep + "\"compiledResourceBundleNames\": [";
		            	boolean firstOne = true;
		            	for (String bundle : bundles)
		            	{
                            if (!firstOne)
                            {
                                bundleInject += ", ";
                            }
		            		bundleInject += "\"" + bundle + "\"";
		            		firstOne = false;
		            	}
		            	bundleInject += "]";
		            	infoInject += bundleInject;
		            	sep = ",\n";

	                }
	                List<String> bundleClasses = royaleProject.compiledResourceBundleClasses;
	                if (bundles.size() > 0)
	                {
		            	for (String bundleClass : bundleClasses)
		            	{
		                    StringBuilder appendString = new StringBuilder();
		                    appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
		                    appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		                    appendString.append(bundleClass);
		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		                    appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
		                    appendString.append(ASEmitterTokens.SEMICOLON.getToken());
	                        finalLines.add(endRequires, appendString.toString());
	                        addLineToMappings(endRequires);
                            endRequires++;
		            	}
	                }
	            	boolean isMX = false;
	            	List<ISWC> swcs = royaleProject.getLibraries();
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
	            				ITargetAttributes attrs = mxmlFile.getTargetAttributes(royaleProject);
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
	            	String contextRoot = royaleProject.getServciesContextRoot();
	            	if (contextRoot != null)
	            	{
    					String contextInject = sep + "\"contextRoot\"" + ": ";
    					contextInject += "'" + contextRoot.trim() + "'";
		            	sep = ",\n";
		            	infoInject += contextInject;
	            	}
	            	String servicesPath = royaleProject.getServicesXMLPath();
	            	if (servicesPath != null)
	            	{
	            		File servicesFile = new File(servicesPath);
	            		if (!servicesFile.exists())
	            		{
	            			FileNotFoundProblem prob = new FileNotFoundProblem(servicesPath);
	            			royaleProject.getProblems().add(prob);
	            		}
	            		else
	            		{
	            			// should use XML parser to skip over comments
	            			// but this will work for now
	            	        List<String> fileLines = null;
	            			try {
	            				fileLines = Files.readLines(new File(servicesPath), Charset.forName("utf8"));
	            			} catch (IOException e) {
	            				// TODO Auto-generated catch block
	            				e.printStackTrace();
	            			}
	            			StringBuffer sb = new StringBuffer();
	            			boolean inComment = false;
	            			boolean inChannels = false;
	            			for (String s : fileLines)
	            			{
	            	    		s = s.trim();
	            	    		if (s.contains("<!--"))
	            	    		{
	            	    			if (!s.contains("-->"))
	            	    				inComment = true;
	            	    			continue;
	            	    		}
	            	    		if (inComment)
	            	    		{
	            	    			if (s.contains("-->"))
	            	    				inComment = false;
	            	    			continue;
	            	    		}
	            				if (s.contains("service-include"))
	            				{
	            					int c = s.indexOf("file-path");
	            					c = s.indexOf("\"", c);
	            					int c2 = s.indexOf("\"", c + 1);
	            					String filePath = s.substring(c + 1, c2);
	            					File subFile = new File(servicesFile.getParentFile(), filePath);
	    	            	        List<String> subfileLines;
	    	            			try {
	    	            				subfileLines = Files.readLines(subFile, Charset.forName("utf8"));
		    	            			s = getSubFileContent(subfileLines);
		    	            			sb.append(s);
	    	            			} catch (IOException e) {
	    	            				// TODO Auto-generated catch block
	    	            				e.printStackTrace();
	    	            			}
	            				}
	            				else 
	            				{
	            					sb.append(s + " ");
	            					if (s.contains("<channel-definition"))
	            						inChannels = true;
	            					if (s.contains("<endpoint"))
	            						inChannels = false;
	            					if (inChannels && s.contains("class"))
	            					{
		            					int c = s.indexOf("class");
		            					c = s.indexOf("\"", c);
		            					int c2 = s.indexOf("\"", c + 1);
		            					String className = s.substring(c + 1, c2);
		    		                    StringBuilder appendString = new StringBuilder();
		    		                    appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
		    		                    appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
		    		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		    		                    appendString.append(className);
		    		                    appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
		    		                    appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
		    		                    appendString.append(ASEmitterTokens.SEMICOLON.getToken());
		    	                        finalLines.add(endRequires, appendString.toString());
		    	                        addLineToMappings(endRequires);
		                                endRequires++;
	            					}
	            				}
	            			}
        					String servicesInject = sep + "\"servicesConfig\"" + ": ";
        					servicesInject += "'" + sb.toString().trim() + "'";
    		            	sep = ",\n";
    		            	infoInject += servicesInject;

	            		}
	            	}
	            	infoInject += "}};";
                    finalLines.add(infoInject);
                    int newLineIndex = 0;
                    while((newLineIndex = infoInject.indexOf('\n', newLineIndex)) != -1)
                    {
                        addLineToMappings(finalLines.size());
                        newLineIndex++;
                    }

                    String cssInject = "\n\n" + thisDef + ".prototype.cssData = [";
                    JSCSSCompilationSession cssSession = (JSCSSCompilationSession) royaleProject.getCSSCompilationSession();
                    String s = cssSession.getEncodedCSS();
                    if (s != null)
                    {
                        int reqidx = s.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                        if (reqidx != -1)
                        {
                            String cssRequires = s.substring(reqidx);
                            s = s.substring(0, reqidx - 1);
                            String[] cssRequireLines = cssRequires.split("\n");
                            for(String require : cssRequireLines)
                            {
                                finalLines.add(endRequires, require);
                                addLineToMappings(endRequires);
                                endRequires++;
                            }
                        }
                        cssInject += s;
                        finalLines.add(cssInject);
                        newLineIndex = 0;
                        while((newLineIndex = cssInject.indexOf('\n', newLineIndex)) != -1)
                        {
                            addLineToMappings(finalLines.size());
                            newLineIndex++;
                        }
                    }
	            }
            }
        }
		if (staticUsedNames.size() > 0)
		{
			if (staticUsedNames.size() > 1 ||
					!staticUsedNames.get(0).equals(currentClassName))
			{
				StringBuilder sb = new StringBuilder();
				sb.append(JSGoogEmitterTokens.ROYALE_STATIC_DEPENDENCY_LIST.getToken());
				boolean firstDependency = true;
				for (String staticName : staticUsedNames)
				{
					if (currentClassName.equals(staticName))
						continue;
					
					if (!firstDependency)
						sb.append(",");
					firstDependency = false;
					sb.append(staticName);
				}
				sb.append("*/");
				finalLines.add(provideIndex, sb.toString());
	            addLineToMappings(provideIndex);
			}
		}

    	return Joiner.on("\n").join(finalLines);
    }

    private String getSubFileContent(List<String> subfileLines) {
    	StringBuffer sb = new StringBuffer();
    	for (String s : subfileLines)
    	{
    		s = s.trim();
    		if (s.startsWith("<?xml"))
    			continue;
    		else
    		{
    			sb.append(s + " ");
    		}
    	}
    	return sb.toString();
	}

	public void startMapping(ISourceLocation node)
    {
        startMapping(node, node.getLine(), node.getColumn());
    }

    public void startMapping(ISourceLocation node, int line, int column)
    {
        if (isBufferWrite())
        {
            return;
        }
        if (lastMapping != null)
        {
            FilePosition sourceStartPosition = lastMapping.sourceStartPosition;
            throw new IllegalStateException("Cannot start new mapping when another mapping is already started. "
                    + "Previous mapping at Line " + sourceStartPosition.getLine()
                    + " and Column " + sourceStartPosition.getColumn()
                    + " in file " + lastMapping.sourcePath);
        }

        String sourcePath = node.getSourcePath();
        if (sourcePath == null)
        {
            //if the source path is null, this node may have been generated by
            //the compiler automatically. for example, an untyped variable will
            //have a node for the * type.
            if (node instanceof IASNode)
            {
                IASNode parentNode = ((IASNode) node).getParent();
                if (parentNode != null)
                {
                    //try the parent node
                    startMapping(parentNode, line, column);
                    return;
                }
            }
        }

        SourceMapMapping mapping = new SourceMapMapping();
        mapping.sourcePath = sourcePath;
        mapping.sourceStartPosition = new FilePosition(line, column);
        mapping.destStartPosition = new FilePosition(getCurrentLine(), getCurrentColumn());
        lastMapping = mapping;
    }

    public void startMapping(ISourceLocation node, ISourceLocation afterNode)
    {
        startMapping(node, afterNode.getEndLine(), afterNode.getEndColumn());
    }

    public void endMapping(ISourceLocation node)
    {
        if (isBufferWrite())
        {
            return;
        }
        if (lastMapping == null)
        {
            throw new IllegalStateException("Cannot end mapping when a mapping has not been started");
        }

        lastMapping.destEndPosition = new FilePosition(getCurrentLine(), getCurrentColumn());
        sourceMapMappings.add(lastMapping);
        lastMapping = null;
    }

    /**
     * Adjusts the line numbers saved in the source map when a line should be
     * added during post processing.
     *
     * @param lineIndex
     */
    protected void addLineToMappings(int lineIndex)
    {
        for (SourceMapMapping mapping : sourceMapMappings)
        {
            FilePosition destStartPosition = mapping.destStartPosition;
            int startLine = destStartPosition.getLine();
            if(startLine > lineIndex)
            {
                mapping.destStartPosition = new FilePosition(startLine + 1, destStartPosition.getColumn());
                FilePosition destEndPosition = mapping.destEndPosition;
                mapping.destEndPosition = new FilePosition(destEndPosition.getLine() + 1, destEndPosition.getColumn());
            }
        }
    }

    /**
     * Adjusts the line numbers saved in the source map when a line should be
     * removed during post processing.
     *
     * @param lineIndex
     */
    protected void removeLineFromMappings(int lineIndex)
    {
        for (SourceMapMapping mapping : sourceMapMappings)
        {
            FilePosition destStartPosition = mapping.destStartPosition;
            int startLine = destStartPosition.getLine();
            if(startLine > lineIndex)
            {
                mapping.destStartPosition = new FilePosition(startLine - 1, destStartPosition.getColumn());
                FilePosition destEndPosition = mapping.destEndPosition;
                mapping.destEndPosition = new FilePosition(destEndPosition.getLine() - 1, destEndPosition.getColumn());
            }
        }
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSRoyaleEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitDeclarations(IMXMLDeclarationsNode node)
    {
    	inMXMLContent = true;
        boolean reusingDescriptor = false;

        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("i");

        MXMLDescriptorSpecifier currentPropertySpecifier = new MXMLDescriptorSpecifier();
        currentPropertySpecifier.isProperty = true;
        currentPropertySpecifier.name = "mxmlContent";
        currentPropertySpecifier.parent = currentInstance;
        if (currentInstance == null)
        {
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
        if (!reusingDescriptor)
        	descriptorTree.add(currentPropertySpecifier);
        moveDown(false, currentInstance, currentPropertySpecifier);
    	super.emitDeclarations(node);
        moveUp(false, false);
    	inMXMLContent = false;
    }

    @Override
    public void emitDocument(IMXMLDocumentNode node)
    {
        RoyaleJSProject fjp = (RoyaleJSProject) getMXMLWalker().getProject();
    	if (fjp.config != null)
    		emitExports = fjp.config.getExportPublicSymbols();

        descriptorTree = new ArrayList<MXMLDescriptorSpecifier>();
        propertiesTree = new MXMLDescriptorSpecifier();

        events = new ArrayList<MXMLEventSpecifier>();
        instances = new ArrayList<MXMLDescriptorSpecifier>();
        scripts = new ArrayList<IMXMLScriptNode>();
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
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();

        fjs.setBuilder(getBuilder());
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

        emitDeclarationVariables();

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

        emitSourceMapDirective(node);
    }

    public void emitDeclarationVariables()
    {
    	for (IMXMLInstanceNode node : primitiveDeclarationNodes)
    	{
    		String id = node.getEffectiveID();
            writeNewline();
            writeNewline("/**");
            writeNewline(" * @export");
            writeNewline(" * @type {" + JSGoogDocEmitter.convertASTypeToJSType(formatQualifiedName(node.getName()), "") + "}");
            writeNewline(" */");
            String cname = node.getFileNode().getName();
            write(cname);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(JSEmitterTokens.PROTOTYPE);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(id);
            writeNewline(ASEmitterTokens.SEMICOLON);

    	}
    }

    public void emitSubDocument(IMXMLComponentNode node)
    {
        ArrayList<MXMLDescriptorSpecifier> oldDescriptorTree;
        MXMLDescriptorSpecifier oldPropertiesTree;
        MXMLDescriptorSpecifier oldStateOverrides;
        ArrayList<MXMLEventSpecifier> oldEvents;
        ArrayList<IMXMLScriptNode> oldScripts;
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
        scripts = new ArrayList<IMXMLScriptNode>();
        //styles = new ArrayList<MXMLStyleSpecifier>();

        oldCurrentInstances = currentInstances;
        currentInstances = new ArrayList<MXMLDescriptorSpecifier>();
        oldCurrentPropertySpecifiers = currentPropertySpecifiers;
        currentPropertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();
        oldStateOverrides = currentStateOverrides;
        currentStateOverrides = new MXMLDescriptorSpecifier();
        
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
        ((JSRoyaleEmitter) asEmitter).getModel().pushClass(cdef);

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

        ((JSRoyaleEmitter) asEmitter).mxmlEmitter = this;

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

        write(((JSRoyaleEmitter) asEmitter).stringifyDefineProperties(cdef));


        descriptorTree = oldDescriptorTree;
        propertiesTree = oldPropertiesTree;
        currentStateOverrides = oldStateOverrides;
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
        ((JSRoyaleEmitter) asEmitter).getModel().popClass();
        ((JSRoyaleEmitter) asEmitter).mxmlEmitter = null;

    }

    @Override
    public void emitMetadata(IMXMLMetadataNode node)
    {
        metadataNodes.add(node);
    }

    public void emitSourceMapDirective(ITypeNode node)
    {
        IMXMLBlockWalker walker = (IMXMLBlockWalker) getMXMLWalker();
        IJSEmitter jsEmitter = (IJSEmitter) walker.getASEmitter();
        jsEmitter.emitSourceMapDirective(node);
    }

    //--------------------------------------------------------------------------

    protected void emitClassDeclStart(String cname, String baseClassName,
            boolean indent)
    {
        writeNewline();
        writeNewline("/**");
        writeNewline(" * @constructor");
        writeNewline(" * @extends {" + formatQualifiedName(baseClassName) + "}");
        if (interfaceList != null && interfaceList.length() > 0)
        {
        	String[] interfaces = interfaceList.split(",");
        	for (String iface : interfaces)
        	{
        		writeNewline(" * @implements {" + formatQualifiedName(iface.trim()) + "}");
        	}
        }
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

            collectExportedNames(root);

	        writeNewline("/**");
	        if (emitExports)
	        	writeNewline(" * @export");
	        writeNewline(" * @type {Array}");
	        writeNewline(" */");
	        writeNewline("this.mxmlsd = " + ASEmitterTokens.SQUARE_OPEN.getToken());
	        indentPush();
	        write(root.outputStateDescriptors(false));
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
            write("this.generateMXMLAttributes");
            write(ASEmitterTokens.PAREN_OPEN);
            indentPush();
            writeNewline(ASEmitterTokens.SQUARE_OPEN);

            MXMLDescriptorSpecifier root = propertiesTree;
            root.isTopNode = true;
            for(int i = 0; i < getCurrentIndent(); i++)
            {
                root.indentPush();
            }
            write(root.output(true));
            indentPop();
            writeNewline();

            collectExportedNames(root);

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
        write(formatQualifiedName(cname) + ".prototype.ROYALE_CLASS_INFO = { names: [{ name: '");
        write(cdef.getBaseName());
        write("', qName: '");
        write(formatQualifiedName(cname));
        write("'");
        writeToken(ASEmitterTokens.COMMA);
        write(JSRoyaleEmitterTokens.ROYALE_CLASS_INFO_KIND);
        writeToken(ASEmitterTokens.COLON);
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(JSRoyaleEmitterTokens.ROYALE_CLASS_INFO_CLASS_KIND);
        writeToken(ASEmitterTokens.SINGLE_QUOTE);
        write(" }]");
        if (interfaceList != null)
        {
        	write(", interfaces: [");
        	write(interfaceList);
        	write("]");
        }
        write(" };");

        emitReflectionData(cdef);
        writeNewline();
        writeNewline();

    }

    private void emitReflectionData(IClassDefinition cdef)
    {
        JSRoyaleEmitter asEmitter = (JSRoyaleEmitter)((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
        RoyaleJSProject fjs = (RoyaleJSProject) getMXMLWalker().getProject();
    	ArrayList<String> exportProperties = new ArrayList<String>();
    	ArrayList<String> exportSymbols = new ArrayList<String>();
    	Set<String> exportMetadata = Collections.<String> emptySet();
    	if (fjs.config != null)
    		exportMetadata = fjs.config.getCompilerKeepCodeWithMetadata();
        ArrayList<PackageFooterEmitter.VariableData> varData = new ArrayList<PackageFooterEmitter.VariableData>();
        // vars can only come from script blocks and decls?
        for (IMXMLInstanceNode declNode : primitiveDeclarationNodes)
        {
        	PackageFooterEmitter.VariableData data = asEmitter.packageFooterEmitter.new VariableData();
        	varData.add(data);
        	data.name = declNode.getEffectiveID();
            data.isStatic = false;
            String qualifiedTypeName = declNode.getName();
    	    data.type = (qualifiedTypeName);
        }
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
            if (instance.id != null)
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
        
        ArrayList<IMetaTagNode> metadataTagNodes = new ArrayList<IMetaTagNode>();
        for (IMXMLMetadataNode metadataTag : metadataNodes)
        {
        	IMetaTagNode[] tags = metadataTag.getMetaTagNodes();
        	//tags (MetaTagNodes) can be null if the parent node is empty (or content is commented out)
        	if (tags != null) {
                for (IMetaTagNode tag : tags)
                {
                    metadataTagNodes.add(tag);
                }
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
	
		asEmitter.packageFooterEmitter.emitReflectionRegisterInitialStaticFields(
				formatQualifiedName(cdef.getQualifiedName()),
				cdef);
        
        asEmitter.packageFooterEmitter.emitExportProperties(
                formatQualifiedName(cdef.getQualifiedName()),
                exportProperties,
                exportSymbols);
    }

    private void collectAccessors(HashMap<String, PropertyNodes> accessors, ArrayList<PackageFooterEmitter.AccessorData> accessorData,IClassDefinition cdef ) {
        JSRoyaleEmitter asEmitter = (JSRoyaleEmitter)((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
        RoyaleJSProject fjs = (RoyaleJSProject) getMXMLWalker().getProject();

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
			String id = instance.id != null ? instance.id : instance.effectiveId;
			if (id != null) { //it seems id can be null, for example with a generated Object for Operations via RemoteObject
				writeNewline();
				writeNewline("/**");
				writeNewline(" * @private");
				writeNewline(" * @type {" + instance.name + "}");
				writeNewline(" */");
				write(ASEmitterTokens.THIS);
				write(ASEmitterTokens.MEMBER_ACCESS);

				if (!id.startsWith(MXMLRoyaleEmitterTokens.ID_PREFIX.getToken())) id += "_";
				write(id);
				writeNewline(ASEmitterTokens.SEMICOLON);
			}
        }
    }

    //--------------------------------------------------------------------------

    protected void emitBindingData(String cname, IClassDefinition cdef)
    {
		IRoyaleProject project = (IRoyaleProject)(walker.getProject());
		BindingDatabase bd = project.getBindingMap().get(cdef);
        if (bd == null)
            return;
        if (bd.getBindingInfo().isEmpty())
            return;

        inStaticInitializer = true;
        outputBindingInfoAsData(cname, bd);
        inStaticInitializer = false;
    }

    private void outputBindingInfoAsData(String cname, BindingDatabase bindingDataBase)
    {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                .getASEmitter();
        
        writeNewline("/**");
        writeNewline(" * @export"); // must export or else GCC will remove it
        writeNewline(" */");
        writeNewline(formatQualifiedName(cname)
                + ".prototype._bindings = [");

		if (bindingDataBase.getHasAncestorBindings()) {
			//reference the ancestor binding data (which may in turn reference its owner's ancestor's bindings etc)
			writeNewline(formatQualifiedName(bindingDataBase.getNearestAncestorWithBindings()) +
					 ".prototype._bindings,");
		}

        Set<BindingInfo> bindingInfo = bindingDataBase.getBindingInfo();
        writeNewline(bindingInfo.size() + ","); // number of bindings
        boolean hadOutput = false;
        for (BindingInfo bi : bindingInfo)
        {
            if (hadOutput) writeNewline(ASEmitterTokens.COMMA.getToken());
            hadOutput = true;
            String s;
            IMXMLNode node = bi.node;
            if (node instanceof IMXMLSingleDataBindingNode)
            {
            	IMXMLSingleDataBindingNode sbdn = (IMXMLSingleDataBindingNode)node;
            	RoyaleJSProject project = (RoyaleJSProject)getMXMLWalker().getProject();
            	IDefinition bdef = sbdn.getExpressionNode().resolve(project);
            	if (bdef != null)
            	{
	            	//IDefinition cdef = bdef.getParent();
	            	project.addExportedName(/*cdef.getQualifiedName() + "." + */bdef.getBaseName());
            	}
            }
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
	                String qname = bi.classDef.getQualifiedName();
	                if (!usedNames.contains(qname))
	                	usedNames.add(qname);
	                if (!staticUsedNames.contains(qname))
	                	staticUsedNames.add(qname);
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
            s = bi.getDestinationString();
            if (destNode != null && s == null)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(generateSetterFunction(bi, destNode));
                writeNewline(sb.toString() + ASEmitterTokens.COMMA.getToken());
            }
            else
                writeNewline(ASEmitterTokens.NULL.getToken() + ASEmitterTokens.COMMA.getToken());

            if (s == null)
            {
                write(ASEmitterTokens.NULL.getToken());
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
                write(ASEmitterTokens.SQUARE_CLOSE.getToken());
            }
            else
                write(ASEmitterTokens.DOUBLE_QUOTE.getToken() + s +
                        ASEmitterTokens.DOUBLE_QUOTE.getToken());
            
        }
        Set<Entry<Object, WatcherInfoBase>> watcherChains = bindingDataBase.getWatcherChains();
        
        if (watcherChains != null)
        {
            int count = watcherChains.size();
            if (hadOutput) {
                if (count > 0) writeNewline(ASEmitterTokens.COMMA);
                else writeNewline();
            }
            for (Entry<Object, WatcherInfoBase> entry : watcherChains)
            {
                count--;
                WatcherInfoBase watcherInfoBase = entry.getValue();
                encodeWatcher(watcherInfoBase);
                if (count > 0) writeNewline(ASEmitterTokens.COMMA);
            }
        } else {
            if (hadOutput) writeNewline();
        }

        writeNewline( ASEmitterTokens.SQUARE_CLOSE.getToken() + ASEmitterTokens.SEMICOLON.getToken());
    }

    private String generateSetterFunction(BindingInfo bi, IExpressionNode destNode) {
        IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
        	.getASEmitter();
		StringBuilder sb = new StringBuilder();
		sb.append("function (value) { ");
		if (destNode instanceof InstructionListNode)
		{
			sb.append(generateDestExpression(bi));
		}
		else
		{
			String body = asEmitter.stringifyNode(destNode);
			sb.append(body);
			sb.append(" = value;");
		}
		sb.append("}");
		return sb.toString();
	}
    
    String generateDestExpression(BindingInfo bi)
    {
    	StringBuilder sb = new StringBuilder();
    	MXMLBindingNode node = (MXMLBindingNode)bi.node;
    	IMXMLBindingAttributeNode destNode = node.getDestinationAttributeNode();
    	Stack<IASNode> nodeStack = new Stack<IASNode>();
    	nodeStack.push(node);
    	IASNode parentNode = node.getParent();
    	while (!(parentNode instanceof IMXMLInstanceNode))
    	{
    		nodeStack.push(parentNode);
    		parentNode = parentNode.getParent();
    	}
    	boolean isXML = parentNode instanceof IMXMLXMLNode;
    	boolean isXMLList = parentNode instanceof IMXMLXMLListNode;
    	String effectiveID = ((IMXMLInstanceNode)parentNode).getEffectiveID();
    	sb.append("this.");
    	sb.append(effectiveID);
    	// at least for one XMLList case, we could not trust
    	// the nodestack as children were only binding nodes
    	// and not preceding XML nodes
    	if (isXMLList)
    	{
    		// re-interpret the instruction list.
    		// it would not be a surprise of the non-XMLList cases will
    		// eventually require this code path
    		InstructionListNode ilNode = (InstructionListNode)destNode.getExpressionNode();
    		InstructionList il = ilNode.getInstructions();
    		ArrayList<Instruction> abcs = il.getInstructions();
    		// the first indexed access accesses an XMLList, the next ones
    		// access an XML object
    		boolean indexedAccess = false;
    		int n = abcs.size();
    		for (int i = 0; i < n; i++)
    		{
    			Instruction inst = abcs.get(i);
    			int opCode = inst.getOpcode();
    			if (opCode == ABCConstants.OP_getlocal0)
    			{
    				if (i > 0)
    					System.out.println("unexpected getLocal0 in binding expression");
    			}
    			else if (opCode == ABCConstants.OP_getproperty)
    			{
    				OneOperandInstruction getProp = (OneOperandInstruction)inst;
    				Name propName = (Name)getProp.getOperand(0);
    				if (i == 0)
    					System.out.println("unexpected opcode in binding expression");
    				else if (i == 1 && !propName.getBaseName().contentEquals(effectiveID))
    					System.out.println("unexpected effectiveID in binding expression");
    				else if (i > 1)
    				{
    					try
    				    {
    				        Integer.parseInt(propName.getBaseName());
    				        if (indexedAccess)
    				        	sb.append(".children()");
    			    		sb.append("[" + propName.getBaseName() + "]" );
    				        indexedAccess = true;
    				    } catch (NumberFormatException ex)
    				    {
    				    	sb.append(".elements(" + propName.getBaseName() + ")" );
    				    }
    				}
    			}
    			else if (opCode == ABCConstants.OP_getlocal1)
    			{
    				if (i != n - 3)
    					System.out.println("unexpected getLocal1 in binding expression");    				
    			}
    			else if (opCode == ABCConstants.OP_setproperty)
    			{
    				if (i != n - 2)
    					System.out.println("unexpected setProperty in binding expression");    				
    				OneOperandInstruction setProp = (OneOperandInstruction)inst;
    				Name propName = (Name)setProp.getOperand(0);
    				if (!propName.getBaseName().contentEquals(destNode.getName()))
    					System.out.println("unexpected setProperty name in binding expression");    				
    				break; // exit loop
    			}
    		}    		
	    	sb.append(".setAttribute('" + destNode.getName() + "', value);" );
    	}
    	else
    	{
	    	while (nodeStack.size() > 0)
	    	{
	    		IASNode childNode = nodeStack.pop();
	    		int n = parentNode.getChildCount();
	    		int i = 0;
	    		for (; i < n; i++)
	    		{
	    			if (childNode == parentNode.getChild(i))
	    				break;
	    		}
	    		assert i < n;
	    		sb.append("[" + new Integer(i).toString() + "]" );
	    		parentNode = childNode;
	    	}
	    	if (isXML)
		    	sb.append(".setAttribute('" + destNode.getName() + "', value);" );
	    	else
	    		sb.append("." + destNode.getName() + " = value;");
    	}
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
                    ASEmitterTokens.DOUBLE_QUOTE.getToken() + ASEmitterTokens.COMMA.getToken());
            IExpressionNode params[] = functionWatcherInfo.params;
            StringBuilder sb = new StringBuilder();
            sb.append("function() { return [");
            boolean firstone = true;
            for (IExpressionNode param : params)
            {
                if (!firstone)
                {
                    sb.append(ASEmitterTokens.COMMA.getToken());
                }
                firstone = false;
                sb.append(asEmitter.stringifyNode(param));
            }
            sb.append("]; },");
            writeNewline(sb.toString());
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
            StringBuilder propertyGetterFunction = null;
            if (watcherInfoBase.isRoot && !makeStaticWatcher)
            {
                // TODO: figure out what this looks like
                // propertyGetterFunction = this.propertyGetter;
                // assert propertyGetterFunction != null;
                StringBuilder sb = new StringBuilder();
                sb.append("function() { return this.");
                RoyaleJSProject fjp = (RoyaleJSProject) getMXMLWalker().getProject();
                String propName = propertyWatcherInfo.getPropertyName();
                IDefinitionSet defSet = this.classDefinition.getContainedScope().getLocalDefinitionSetByName(propName);
                if (defSet != null)
                {
	                IDefinition rootDef = defSet.getDefinition(0);
	                if (rootDef != null)
	                {
	                	if (rootDef.isPrivate())
	                	{
	                		sb.append(((JSRoyaleEmitter)asEmitter).formatPrivateName(this.classDefinition.getQualifiedName(), 
	                				propName));
	                    	sb.append("; }");
	                    	propertyGetterFunction = sb;
	                	}
	                	// might need for public as well.
	                }
                }
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
            else
            	writeNewline(propertyGetterFunction.toString() + ASEmitterTokens.COMMA.getToken());
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
        for (IMXMLScriptNode node : scripts)
        {
            IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                    .getASEmitter();

            int len = node.getChildCount();
            if (len > 0)
            {
                for (int i = 0; i < len; i++)
                {
                    IASNode cnode = node.getChild(i);
                    if (cnode.getNodeID() == ASTNodeID.VariableID) {
                        ((JSRoyaleEmitter) asEmitter).getModel().getVars().add((IVariableNode) cnode);
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
                            ((JSRoyaleEmitter) asEmitter).getModel().getBindableVars().put(variableNode.getName(), bindableVarInfo);
                        }
                    }

                    if (!(cnode instanceof IImportNode))
                    {
                        asEmitter.getWalker().walk(cnode);
                        write(ASEmitterTokens.SEMICOLON.getToken());

                        if (i == len - 1)
                            indentPop();

                        writeNewline();
                        writeNewline();
                        writeNewline();
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    protected void emitEvents(String cname)
    {
        for (MXMLEventSpecifier event : events)
        {
            writeNewline("/**");
	        if (emitExports)
	        	writeNewline(" * @export");
            writeNewline(" * @param {" + formatQualifiedName(event.type) + "} event");
            writeNewline(" */");
            writeNewline(formatQualifiedName(cname)
                    + ".prototype." + event.eventHandler + " = function(event)");
            writeNewline(ASEmitterTokens.BLOCK_OPEN, true);


            IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                    .getASEmitter();

            IMXMLEventSpecifierNode node = event.node;
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                if (i > 0)
                {
                    writeNewline();
                }
                IASNode cnode = node.getChild(i);
                asEmitter.getWalker().walk(cnode);
                write(ASEmitterTokens.SEMICOLON);
            }

            indentPop();
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);
            writeNewline();
            writeNewline();
        }
    }

    //--------------------------------------------------------------------------
    private boolean skippedDefineProps;

    protected void emitPropertyGetterSetters(String cname)
    {
    	int n = 0;
        for (MXMLDescriptorSpecifier instance : instances)
        {
            if (instance.id != null || instance.hasLocalId)
            {
            	n++;
            }
        }
    	if (n == 0 && (descriptorTree.size() == 0 ||
    			       descriptorTree.size() == 1 && descriptorTree.get(0).propertySpecifiers.size() == 0))
    	{
    		skippedDefineProps = true;
    		return;
    	}

    	String formattedCName = formatQualifiedName(cname);

    	write("Object.defineProperties(");
    	write(formattedCName);
    	writeNewline(".prototype, /** @lends {" + formattedCName + ".prototype} */ {");
        indentPush();
        int i = 0;
        for (MXMLDescriptorSpecifier instance : instances)
        {
        	String instanceId = instance.id;
        	if (instanceId == null && instance.hasLocalId ){
        		instanceId = instance.effectiveId;
			}
            if (instanceId != null)
            {
                indentPush();
    	        writeNewline("/** @export */");
                writeNewline(instanceId + ": {");
                writeNewline("/** @this {" + formattedCName + "} */");
                indentPush();
                writeNewline("get: function() {");
                indentPop();
                writeNewline("return this." + instanceId + "_;");
                writeNewline("},");
                writeNewline("/** @this {" + formattedCName + "} */");
                indentPush();
                writeNewline("set: function(value) {");
                indentPush();
                writeNewline("if (value != this." + instanceId + "_) {");
                writeNewline("this." + instanceId + "_ = value;");
                write("this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(this, '");
                indentPop();
                writeNewline(instanceId + "', null, value));");
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
            RoyaleJSProject project = (RoyaleJSProject) getMXMLWalker().getProject();
            project.needLanguage = true;
            MXMLDescriptorSpecifier root = descriptorTree.get(0);
            if (root.propertySpecifiers.size() == 0 && skippedDefineProps)
            	return; // all declarations were primitives
            root.isTopNode = false;

            collectExportedNames(root);

            indentPush();
            writeNewline("'MXMLDescriptor': {");
            writeNewline("/** @this {" + formatQualifiedName(cname) + "} */");
            indentPush();
            writeNewline("get: function() {");
            writeNewline("if (this.mxmldd == undefined)");
            indentPush();
            writeNewline("{");
            writeNewline("/** @type {Array} */");
            writeNewline("var arr = " + formatQualifiedName(cname) + ".superClass_.get__MXMLDescriptor.apply(this);");
            writeNewline("/** @type {Array} */");
            indentPush();
            writeNewline("var data = [");

            for(int i = 0; i < getCurrentIndent(); i++)
            {
                root.indentPush();
            }
            write(root.output(true));
            indentPop();
            writeNewline();

            writeNewline("];");
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
            indentPop();
            writeNewline("}");
            indentPop();
            writeNewline("}");
            indentPop();
        	writeNewline("});");
        }

    }

    private void collectExportedNames(MXMLDescriptorSpecifier descriptor)
    {
        ICompilerProject project = getMXMLWalker().getProject();
        RoyaleJSProject royaleProject = null;
        if (project instanceof RoyaleJSProject)
        {
            royaleProject = (RoyaleJSProject) project;
            String name = descriptor.name;
            if (name == null)
            	name = this.classDefinition.getQualifiedName();
            for (MXMLDescriptorSpecifier prop : descriptor.propertySpecifiers)
            {
            	String propName = prop.name;
            	royaleProject.addExportedName(/*name + "." + */propName);
            	if (prop.propertySpecifiers.size() > 0)
            	{
                    collectExportedNames(prop.propertySpecifiers.get(0));
            	}
            }
            if (descriptor.childrenSpecifier != null)
            {
                for (MXMLDescriptorSpecifier prop : descriptor.childrenSpecifier.propertySpecifiers)
                {
                	collectExportedNames(prop);
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    private HashMap<IMXMLEventSpecifierNode, String> eventHandlerNameMap = new HashMap<IMXMLEventSpecifierNode, String>();

    @Override
    public void emitEventSpecifier(IMXMLEventSpecifierNode node)
    {
    	IMXMLStateNode currentState = null;
    	if (!inStatesOverride.empty())
    		currentState = inStatesOverride.peek();
        if (isStateDependent(node, currentState, true))
            return;
    		
        IDefinition cdef = node.getDefinition();

        MXMLDescriptorSpecifier currentDescriptor = getCurrentDescriptor("i");

        MXMLEventSpecifier eventSpecifier = new MXMLEventSpecifier();

		IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker()).getASEmitter();
		JSRoyaleEmitter fjs = (JSRoyaleEmitter)asEmitter;

		IClassDefinition currentClass = fjs.getModel().getCurrentClass();
		//naming needs to avoid conflicts with ancestors - using delta from object which is
		//a) short and b)provides a 'unique' (not zero risk, but very low risk) option
        String nameBase = EmitterUtils.getClassDepthNameBase(MXMLRoyaleEmitterTokens.EVENT_PREFIX
				.getToken(), currentClass, getMXMLWalker().getProject());
        eventSpecifier.eventHandler = nameBase + eventCounter++;
        eventSpecifier.name = cdef.getBaseName();
        eventSpecifier.type = node.getEventParameterDefinition()
                .getTypeAsDisplayString();

        eventHandlerNameMap.put(node, eventSpecifier.eventHandler);

        //save the node for emitting later in emitEvents()
        //previously, we stringified the node and saved that instead of the
        //node, but source maps don't work when you stringify a node too early -JT
        eventSpecifier.node = node;

	    if (currentDescriptor != null)
	        currentDescriptor.eventSpecifiers.add(eventSpecifier);
	    else if (inStatesOverride.empty()) // in theory, if no currentdescriptor must be top tag event
	        propertiesTree.eventSpecifiers.add(eventSpecifier);
        events.add(eventSpecifier);
    }

    @Override
    public void emitInstance(IMXMLInstanceNode node)
    {
    	IMXMLStateNode currentState = null;
    	if (!inStatesOverride.empty())
    		currentState = inStatesOverride.peek();
        if (overrideInstanceToEmit != node && isStateDependent(node, currentState, false))
            return;

        ASTNodeID nodeID = node.getNodeID();
    	if ((nodeID == ASTNodeID.MXMLXMLID || nodeID == ASTNodeID.MXMLXMLListID) &&
    			node.getParent().getNodeID() == ASTNodeID.MXMLDeclarationsID)
    	{
    		primitiveDeclarationNodes.add(node);
    		return;
    	}

        IClassDefinition cdef = node
                .getClassReference((ICompilerProject) getMXMLWalker()
                        .getProject());

        MXMLDescriptorSpecifier currentPropertySpecifier = getCurrentDescriptor("ps");
    	if (nodeID == ASTNodeID.MXMLFunctionID)
    	{
            RoyaleJSProject project = (RoyaleJSProject) getMXMLWalker().getProject();
            project.needLanguage = true;
            MXMLFunctionNode fnode = ((MXMLFunctionNode)node);
            IFunctionDefinition fdef = fnode.getValue(project);
            IExpressionNode fexpNode = (IExpressionNode)fnode.getExpressionNode();
            String fnName = fdef.getBaseName();
            IASEmitter asEmitter = ((IMXMLBlockWalker) getMXMLWalker())
                    .getASEmitter();
            if (fdef.isPrivate() && project.getAllowPrivateNameConflicts())
            	fnName = ((JSRoyaleEmitter)asEmitter).formatPrivateName(fdef.getParent().getQualifiedName(), fdef.getBaseName());
            String fNodeString = ((JSRoyaleEmitter)asEmitter).stringifyNode(fexpNode);
    		currentPropertySpecifier.value = fNodeString; 
    		return;
    	}

        String effectiveId = null;
        String id = node.getID();
        if (id == null)
        {
        	effectiveId = node.getEffectiveID();
        	if (effectiveId == null)
        		effectiveId = node.getClassDefinitionNode().getGeneratedID(node);
        }

        MXMLDescriptorSpecifier currentInstance = new MXMLDescriptorSpecifier();
        currentInstance.isProperty = false;
        currentInstance.id = id;
        currentInstance.hasLocalId = node.getLocalID() != null;
        currentInstance.effectiveId = effectiveId;
        currentInstance.name = formatQualifiedName(cdef.getQualifiedName());
        currentInstance.parent = currentPropertySpecifier;

        if (currentPropertySpecifier != null)
            currentPropertySpecifier.propertySpecifiers.add(currentInstance);
        else if (inMXMLContent)
            descriptorTree.add(currentInstance);
        else
        {
        	// we get here if a instance is a child of a top-level tag
        	// and there is no default property.  If there are other
        	// ways to get here, then the code will need adjusting.
        	
        	// this code assumes that the children will have an id
        	// and will just create properties with the children's id
        	// on the class.
        	MXMLDescriptorSpecifier prop = new MXMLDescriptorSpecifier();
        	prop.isProperty = true;
        	prop.name = id;
        	prop.parent = propertiesTree;
        	propertiesTree.propertySpecifiers.add(prop);
            currentInstance.parent = prop;
            prop.propertySpecifiers.add(currentInstance);
        }

        addInstanceIfNeeded(instances, currentInstance);

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
            	inStatesOverride.push(stateNode);
                for (int i=0; i<snodes.size(); i++)
                {
                    IMXMLNode inode = snodes.get(i);
                    if (inode.getNodeID() == ASTNodeID.MXMLInstanceID)
                    {
                        emitInstanceOverride((IMXMLInstanceNode)inode, stateNode);
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
            	inStatesOverride.pop();
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

    private void addInstanceIfNeeded(
			ArrayList<MXMLDescriptorSpecifier> instances2,
			MXMLDescriptorSpecifier currentInstance) {
    	for (MXMLDescriptorSpecifier instance : instances2)
    		if (instance.id != null && currentInstance.id != null && instance.id.equals(currentInstance.id))
    			return;
        instances.add(currentInstance);

	}

	public void emitPropertyOverride(IMXMLPropertySpecifierNode propertyNode)
    {
        RoyaleProject project = (RoyaleProject) getMXMLWalker().getProject();
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
        RoyaleProject project = (RoyaleProject) getMXMLWalker().getProject();
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
            String overrideID = MXMLRoyaleEmitterTokens.BINDING_PREFIX.getToken() + bindingCounter++;
	        setProp.id = overrideID;
	        instances.add(setProp);
			IRoyaleProject project = (IRoyaleProject)(walker.getProject());
			BindingDatabase bd = project.getBindingMap().get(classDefinition);
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
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        RoyaleProject project = (RoyaleProject) getMXMLWalker().getProject();
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
        handler.value = JSRoyaleEmitterTokens.CLOSURE_FUNCTION_NAME.getToken() + ASEmitterTokens.PAREN_OPEN.getToken() +
        		ASEmitterTokens.THIS.getToken() + ASEmitterTokens.MEMBER_ACCESS.getToken() + eventHandler +
        		ASEmitterTokens.COMMA.getToken() + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.THIS.getToken() +
        		ASEmitterTokens.COMMA.getToken() + ASEmitterTokens.SPACE.getToken() + ASEmitterTokens.SINGLE_QUOTE.getToken() +
        			eventHandler + ASEmitterTokens.SINGLE_QUOTE.getToken() +
        		ASEmitterTokens.PAREN_CLOSE.getToken();
        setEvent.propertySpecifiers.add(handler);
    }

    public void emitInstanceOverride(IMXMLInstanceNode instanceNode, IMXMLStateNode state)
    {
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("ps");
        RoyaleProject project = (RoyaleProject) getMXMLWalker().getProject();
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
            overrideInstanceToEmit = instanceNode;
            getMXMLWalker().walk(instanceNode); // instance node
            overrideInstanceToEmit = null;
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

            if (sib instanceof IMXMLInstanceNode && !isStateDependent(sib, state, true))
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
     * Determines whether a string matches a state's name or group name.
     */
    protected boolean inStateOrStateGroup(String name, IMXMLStateNode state)
    {
    	if (state == null) return false;
    	
    	if (name.contentEquals(state.getStateName()))
    		return true;
    	
    	String[] groups = state.getStateGroups();
    	if (groups != null)
    	{
        	for (String s : groups)
        	{
        		if (name.contentEquals(s))
        			return true;
        	}
    	}
    	return false;
    }
    
    /**
     * Determines whether a node is state-dependent.
     * TODO: we should move to IMXMLNode
     */
    protected boolean isStateDependent(IASNode node, IMXMLStateNode currentState, boolean includeGroups)
    {
        if (node instanceof IMXMLSpecifierNode)
        {
            String suffix = ((IMXMLSpecifierNode)node).getSuffix();
            return suffix != null && suffix.length() > 0 && !inStateOrStateGroup(suffix, currentState);
        }
        else if (isStateDependentInstance(node, currentState, includeGroups))
            return true;
        return false;
    }

    /**
     * Determines whether the geven node is an instance node, as is state dependent
     */
    protected boolean isStateDependentInstance(IASNode node, IMXMLStateNode currentState, boolean includeGroups)
    {
        if (node instanceof IMXMLInstanceNode)
        {
            String[] includeIn = ((IMXMLInstanceNode)node).getIncludeIn();
            String[] excludeFrom = ((IMXMLInstanceNode)node).getExcludeFrom();
            if (includeGroups)
            {
	            if (includeIn != null && currentState != null)
	               for (String s : includeIn)
	                   if (inStateOrStateGroup(s, currentState)) return false;
	            if (excludeFrom != null && currentState != null)
	            {
	               for (String s : excludeFrom)
	                   if (inStateOrStateGroup(s, currentState)) return true;
	               return false;
	            }
            }
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

        if (isStateDependent(node, null, true))
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

        moveDown(false, null, currentPropertySpecifier);

        getMXMLWalker().walk(cnode); // Array or Instance

        moveUp(false, false);

        inMXMLContent = oldInMXMLContent;
    }

    @Override
    public void emitScript(IMXMLScriptNode node)
    {
        //save the script for emitting later in emitScripts()
        //previously, we stringified the node and saved that instead of the
        //node, but source maps don't work when you stringify a node too early -JT
        scripts.add(node);
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
            if (ps.hasObject || ps.parent == null) //('ps.parent == null' was added to allow a top level fx:Object definition, they were not being output without that)
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
    	if (node.getParent().getNodeID() == ASTNodeID.MXMLDeclarationsID)
    	{
    		primitiveDeclarationNodes.add(node);
    		return;
    	}

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

    }

    @Override
    public void emitString(IMXMLStringNode node)
    {
    	if (node.getParent().getNodeID() == ASTNodeID.MXMLDeclarationsID)
    	{
    		primitiveDeclarationNodes.add(node);
    		return;
    	}
        getCurrentDescriptor("ps").valueNeedsQuotes = true;

        emitAttributeValue(node);
    }
    
    @Override
    public void emitMXMLClass(IMXMLClassNode node)
    {   
    	RoyaleJSProject project = (RoyaleJSProject)getMXMLWalker().getProject();
    	ITypeDefinition cdef = node.getValue(project);
    	String qname = formatQualifiedName(cdef.getQualifiedName());
    	ICompilationUnit classCU = project.resolveQNameToCompilationUnit(qname);
    	ICompilationUnit cu = project.resolveQNameToCompilationUnit(classDefinition.getQualifiedName());
    	project.addDependency(cu, classCU, DependencyType.EXPRESSION, qname);
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = qname;
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
        IASNode cnode = node.getChild(0);
    	ITypeDefinition type = ((IMXMLClassNode)cnode).getValue(getMXMLWalker().getProject());
    	if (type == null) return;

        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = "new " + formatQualifiedName("org.apache.royale.core.ClassFactory") + "(";

        if (cnode instanceof IMXMLClassNode)
        {
            ps.value += formatQualifiedName(type.getQualifiedName());
        }
        ps.value += ")";
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitComponent(IMXMLComponentNode node)
    {
        MXMLDescriptorSpecifier ps = getCurrentDescriptor("ps");
        ps.value = "new " + formatQualifiedName("org.apache.royale.core.ClassFactory") + "(";

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
        ((JSRoyaleEmitter)asEmitter).setBufferWrite(value);
    }

    //--------------------------------------------------------------------------
    //    JS output
    //--------------------------------------------------------------------------

    private void emitHeader(IMXMLDocumentNode node)
    {
        String cname = node.getFileNode().getName();
        String bcname = node.getBaseClassName();

        RoyaleJSProject project = (RoyaleJSProject) getMXMLWalker().getProject();
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
        writeNewline(" * Generated by Apache Royale Compiler from " + sourceName.replace('\\', '/'));
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
        for (MXMLDescriptorSpecifier instance : allInstances)
        {
            String name = instance.name;
            if (writtenInstances.indexOf(name) == -1)
            {
                emitHeaderLine(name);
                writtenInstances.add(name);
            }
        }
        ASProjectScope projectScope = (ASProjectScope) project.getScope();
        IDefinition cdef = node.getDefinition();
        ICompilationUnit cu = projectScope
                .getCompilationUnitForDefinition(cdef);
        ArrayList<String> deps = project.getRequires(cu);

        // TODO (mschmalle) will remove this cast as more things get abstracted
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) ((IMXMLBlockWalker) getMXMLWalker())
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
        		deps.add(iface);
        		usedNames.add(iface);
        	}
        }
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
                    emitHeaderLine(imp);
                    writtenInstances.add(formatted);
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
            if (project instanceof RoyaleJSProject)
            {
            	if (((RoyaleJSProject)project).needLanguage)
            		emitHeaderLine(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken());
            }
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

    private String createRequireLine(String qname, boolean isProvide) {
        StringBuilder createHeader = new StringBuilder();
        createHeader.append(isProvide ? JSGoogEmitterTokens.GOOG_PROVIDE.getToken() : JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
        createHeader.append(ASEmitterTokens.PAREN_OPEN.getToken());
        createHeader.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
        createHeader.append(qname);
        createHeader.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
        createHeader.append(ASEmitterTokens.PAREN_CLOSE.getToken());
        createHeader.append(ASEmitterTokens.SEMICOLON.getToken());
        return createHeader.toString();
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
        if (inStaticInitializer)
        {
            if (!staticUsedNames.contains(name) && !NativeUtils.isJSNative(name) && isGoogProvided(name))
            {
                staticUsedNames.add(name);
            }
        }

        if (useName && !usedNames.contains(name) && isGoogProvided(name))
        {
            usedNames.add(name);
        }
     	return name;
    }

    @SuppressWarnings("incomplete-switch")
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
    	                    IDefinition varDef = varnode.getDefinition();
    	                    if (varDef.isStatic())
    	                    	continue;
    	                    writeNewline();
    	                    write(ASEmitterTokens.THIS);
    	                    write(ASEmitterTokens.MEMBER_ACCESS);
    	                    JSRoyaleEmitter fjs = (JSRoyaleEmitter) ((IMXMLBlockWalker) getMXMLWalker())
    	                    .getASEmitter();
    	                    
    	                    ICompilerProject project = getMXMLWalker().getProject();
    	                    String qname = varnode.getName();
	                		if (varDef != null && varDef.isPrivate() && project.getAllowPrivateNameConflicts())
	                			qname = fjs.formatPrivateName(varDef.getParent().getQualifiedName(), qname);
	                        if (EmitterUtils.isCustomNamespace(varnode.getNamespace())) {
	                            INamespaceDecorationNode ns = ((VariableNode) varnode).getNamespaceNode();
	                            INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
	                            fjs.formatQualifiedName(nsDef.getQualifiedName()); // register with used names
	                            String s = nsDef.getURI();
	                            write(JSRoyaleEmitter.formatNamespacedProperty(s, qname, false));
	                        }
	                        else write(qname);
    	                    if (schildID == ASTNodeID.BindableVariableID && !varnode.isConst())
    	                    	write("_"); // use backing variable
    	                    write(ASEmitterTokens.SPACE);
    	                    writeToken(ASEmitterTokens.EQUAL);
                            fjs.emitAssignmentCoercion(vnode, varnode.getVariableTypeNode().resolve(getMXMLWalker().getProject()));
    	                    write(ASEmitterTokens.SEMICOLON);

    			        }
    				}
    			}
    		}
    	}
    	n = primitiveDeclarationNodes.size();
    	for (int i = 0; i < n; i++)
    	{
    		IMXMLInstanceNode declNode = primitiveDeclarationNodes.get(i);
    		ASTNodeID nodeId = declNode.getNodeID();
    		String varname;

    		switch (nodeId)
    		{
	    		case MXMLStringID:
	    		{
	    			IMXMLStringNode stringNode = (IMXMLStringNode)declNode;
	    			varname = stringNode.getEffectiveID();
	                writeNewline();
	                write(ASEmitterTokens.THIS);
	                write(ASEmitterTokens.MEMBER_ACCESS);
	                write(varname);
	                write(ASEmitterTokens.SPACE);
	                writeToken(ASEmitterTokens.EQUAL);
	                IMXMLLiteralNode valueNode = (IMXMLLiteralNode)(stringNode.getExpressionNode());
	                Object value = valueNode.getValue();
	                write(objectToString(value));
	                write(ASEmitterTokens.SEMICOLON);
	                break;
	    		}
				case MXMLArrayID:
				{
					IMXMLArrayNode arrayNode = (IMXMLArrayNode)declNode;
					varname = arrayNode.getEffectiveID();
		            writeNewline();
		            write(ASEmitterTokens.THIS);
		            write(ASEmitterTokens.MEMBER_ACCESS);
		            write(varname);
		            write(ASEmitterTokens.SPACE);
		            writeToken(ASEmitterTokens.EQUAL);
		            write("[");
		            int m = arrayNode.getChildCount();
	            	boolean firstOne = true;
		            for (int j = 0; j < m; j++)
		            {
			            IMXMLInstanceNode valueNode = (IMXMLInstanceNode)(arrayNode.getChild(j));
		            	if (firstOne)
		            		firstOne = false;
		            	else
		            		writeToken(",");
			            write(instanceToString(valueNode));
		            }
		            write("]");
		            write(ASEmitterTokens.SEMICOLON);
		            break;
				}
				case MXMLXMLID:
				{
					IMXMLXMLNode xmlNode = (IMXMLXMLNode)declNode;
					String valueString = xmlNode.getXMLString();
					if (valueString != null)
					{
						varname = xmlNode.getEffectiveID();
			            writeNewline();
			            write(ASEmitterTokens.THIS);
			            write(ASEmitterTokens.MEMBER_ACCESS);
			            write(varname);
			            write(ASEmitterTokens.SPACE);
			            writeToken(ASEmitterTokens.EQUAL);
			            write("new XML('");
				        write(StringEscapeUtils.escapeJavaScript(valueString));
			            write("')");
			            write(ASEmitterTokens.SEMICOLON);
					}
		            break;
				}
				case MXMLXMLListID:
				{
					IMXMLXMLListNode xmlNode = (IMXMLXMLListNode)declNode;
					String valueString = xmlNode.getXMLString();
					if (valueString != null)
					{
						varname = xmlNode.getEffectiveID();
			            writeNewline();
			            write(ASEmitterTokens.THIS);
			            write(ASEmitterTokens.MEMBER_ACCESS);
			            write(varname);
			            write(ASEmitterTokens.SPACE);
			            writeToken(ASEmitterTokens.EQUAL);
			            write("new XMLList('");
				        write(StringEscapeUtils.escapeJavaScript(valueString));
			            write("')");
			            write(ASEmitterTokens.SEMICOLON);
					}
		            break;
				}
    		}
    	}
    }

    private String objectToString(Object value)
    {
    	if (value instanceof String)
    	{
    		String s = (String)value;
    		s = StringEscapeUtils.escapeJavaScript(s);
    		return "'" + s + "'";
    	}
    	return "";
    }

    private String instanceToString(IMXMLInstanceNode instanceNode)
    {
    	if (instanceNode instanceof IMXMLStringNode)
    	{
    		IMXMLStringNode stringNode = (IMXMLStringNode)instanceNode;
    		IASNode vNode = stringNode.getExpressionNode();
    		if (vNode instanceof IMXMLLiteralNode)
    		{
	            IMXMLLiteralNode valueNode = (IMXMLLiteralNode)vNode;
	            Object value = valueNode.getValue();
	            return objectToString(value);
    		}
    		else
    			return "''";
    	}
    	return "";
    }

    public void emitComplexStaticInitializers(IASNode node){
        JSRoyaleEmitter fjs = (JSRoyaleEmitter) ((IMXMLBlockWalker) getMXMLWalker())
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
    
	boolean isGoogProvided(String className)
	{
        ICompilerProject project = getMXMLWalker().getProject();
		return ((RoyaleJSProject)project).isGoogProvided(className);
	}

	@Override
	public void emitRemoteObjectMethod(IMXMLRemoteObjectMethodNode node) {
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("i");
        String propName = null;
        int l = node.getChildCount();
        for (int k = 0; k < l; k++)
        {
        	IASNode child = node.getChild(k);
        	if (child.getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
        	{
        		IMXMLPropertySpecifierNode propNode = (IMXMLPropertySpecifierNode)child;
        		if (propNode.getName().equals("name"))
        		{
        			// assume StringNode with LiteralNode
        			IMXMLStringNode literalNode = (IMXMLStringNode)propNode.getChild(0);
        			propName = literalNode.getValue();
        			break;
        		}
        	}
        }
    	MXMLDescriptorSpecifier propertySpecifier = new MXMLDescriptorSpecifier();
    	propertySpecifier.isProperty = true;
    	propertySpecifier.name = propName;
    	propertySpecifier.parent = currentInstance;
    	currentInstance.propertySpecifiers.add(propertySpecifier);
        moveDown(false, null, propertySpecifier);

		emitInstance(node);

		moveUp(false, false);
    	// build out the argument list if any
    	int n = node.getChildCount();
    	for (int i = 0; i < n; i++)
    	{
    		IASNode childNode = node.getChild(i);
    		if (childNode.getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
    		{
    			IMXMLPropertySpecifierNode propNode = (IMXMLPropertySpecifierNode)childNode;
    			if (propNode.getName().equals("arguments"))
    			{
    				ArrayList<String> argList = new ArrayList<String>();
    				childNode = propNode.getChild(0); // this is an MXMLObjectNode
    				n = childNode.getChildCount();
    				for (i = 0; i < n; i++)
    				{
    					IASNode argNode = childNode.getChild(i);
    					propNode = (IMXMLPropertySpecifierNode)argNode;
    					argList.add(propNode.getName());
    				}
    				if (argList.size() > 0)
    				{
    					StringBuilder list = new StringBuilder();
    					list.append("[");
    					int m = argList.size();
    					for (int j = 0; j < m; j++)
    					{
    						if (j > 0)
    							list.append(",");
    						list.append("'" + argList.get(j) + "'");
    					}
    					list.append("]");

    			        MXMLDescriptorSpecifier operationInstance = propertySpecifier.propertySpecifiers.get(0);

    			        MXMLDescriptorSpecifier argListSpecifier = new MXMLDescriptorSpecifier();
    			        argListSpecifier.isProperty = true;
    			        argListSpecifier.name = "argumentNames";
    			        argListSpecifier.parent = operationInstance;
    			        argListSpecifier.value = list.toString();
				        if (operationInstance != null)
				        	operationInstance.propertySpecifiers.add(argListSpecifier);
    				}
    				break;
    			}
    		}
    	}
	}

	@Override
	public void emitRemoteObject(IMXMLRemoteObjectNode node) {
		emitInstance(node);
		// now search for Operations, and add an Object that contains them
		int n = node.getChildCount();
		MXMLDescriptorSpecifier objectSpecifier = null;
		MXMLDescriptorSpecifier propertySpecifier = null;
		for (int i = 0; i < n; i++)
		{
			IASNode child = node.getChild(i);
			if (child.getNodeID() == ASTNodeID.MXMLRemoteObjectMethodID)
			{
		        MXMLDescriptorSpecifier currentPropertySpecifier = getCurrentDescriptor("ps");
		        MXMLDescriptorSpecifier currentInstance =
		        	currentPropertySpecifier.propertySpecifiers.get(currentPropertySpecifier.propertySpecifiers.size() - 1);

		        if (objectSpecifier == null)
		        {
		        	propertySpecifier = new MXMLDescriptorSpecifier();
		        	propertySpecifier.isProperty = true;
		        	propertySpecifier.name = "operations";
		        	propertySpecifier.parent = currentInstance;

			        if (currentInstance != null)
			        	currentInstance.propertySpecifiers.add(propertySpecifier);
			        objectSpecifier = new MXMLDescriptorSpecifier();
			        objectSpecifier.isProperty = false;
			        objectSpecifier.name = formatQualifiedName(IASLanguageConstants.Object);
			        objectSpecifier.parent = propertySpecifier;
			        propertySpecifier.propertySpecifiers.add(objectSpecifier);
			        instances.add(objectSpecifier);
		        }
	            moveDown(false, objectSpecifier, null);
                getMXMLWalker().walk(child); // RemoteObjectMethod
	            moveUp(false, true);
			}
		}
	}
	
	@Override
	public void emitWebServiceMethod(IMXMLWebServiceOperationNode node) {
        MXMLDescriptorSpecifier currentInstance = getCurrentDescriptor("i");
        String propName = null;
        int l = node.getChildCount();
        for (int k = 0; k < l; k++)
        {
        	IASNode child = node.getChild(k);
        	if (child.getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
        	{
        		IMXMLPropertySpecifierNode propNode = (IMXMLPropertySpecifierNode)child;
        		if (propNode.getName().equals("name"))
        		{
        			// assume StringNode with LiteralNode
        			IMXMLStringNode literalNode = (IMXMLStringNode)propNode.getChild(0);
        			propName = literalNode.getValue();
        			break;
        		}
        	}
        }
    	MXMLDescriptorSpecifier propertySpecifier = new MXMLDescriptorSpecifier();
    	propertySpecifier.isProperty = true;
    	propertySpecifier.name = propName;
    	propertySpecifier.parent = currentInstance;
    	currentInstance.propertySpecifiers.add(propertySpecifier);
        moveDown(false, null, propertySpecifier);

		emitInstance(node);

		moveUp(false, false);
    	// build out the argument list if any
    	int n = node.getChildCount();
    	for (int i = 0; i < n; i++)
    	{
    		IASNode childNode = node.getChild(i);
    		if (childNode.getNodeID() == ASTNodeID.MXMLPropertySpecifierID)
    		{
    			IMXMLPropertySpecifierNode propNode = (IMXMLPropertySpecifierNode)childNode;
    			if (propNode.getName().equals("arguments"))
    			{
    				ArrayList<String> argList = new ArrayList<String>();
    				childNode = propNode.getChild(0); // this is an MXMLObjectNode
    				n = childNode.getChildCount();
    				for (i = 0; i < n; i++)
    				{
    					IASNode argNode = childNode.getChild(i);
    					propNode = (IMXMLPropertySpecifierNode)argNode;
    					argList.add(propNode.getName());
    				}
    				if (argList.size() > 0)
    				{
    					StringBuilder list = new StringBuilder();
    					list.append("[");
    					int m = argList.size();
    					for (int j = 0; j < m; j++)
    					{
    						if (j > 0)
    							list.append(",");
    						list.append("'" + argList.get(j) + "'");
    					}
    					list.append("]");

    			        MXMLDescriptorSpecifier operationInstance = propertySpecifier.propertySpecifiers.get(0);

    			        MXMLDescriptorSpecifier argListSpecifier = new MXMLDescriptorSpecifier();
    			        argListSpecifier.isProperty = true;
    			        argListSpecifier.name = "argumentNames";
    			        argListSpecifier.parent = operationInstance;
    			        argListSpecifier.value = list.toString();
				        if (operationInstance != null)
				        	operationInstance.propertySpecifiers.add(argListSpecifier);
    				}
    				break;
    			}
    		}
    	}
	}

	@Override
	public void emitWebService(IMXMLWebServiceNode node) {
		emitInstance(node);
		// now search for Operations, and add an Object that contains them
		int n = node.getChildCount();
		MXMLDescriptorSpecifier objectSpecifier = null;
		MXMLDescriptorSpecifier propertySpecifier = null;
		for (int i = 0; i < n; i++)
		{
			IASNode child = node.getChild(i);
			if (child.getNodeID() == ASTNodeID.MXMLWebServiceOperationID)
			{
		        MXMLDescriptorSpecifier currentPropertySpecifier = getCurrentDescriptor("ps");
		        MXMLDescriptorSpecifier currentInstance =
		        	currentPropertySpecifier.propertySpecifiers.get(currentPropertySpecifier.propertySpecifiers.size() - 1);

		        if (objectSpecifier == null)
		        {
		        	propertySpecifier = new MXMLDescriptorSpecifier();
		        	propertySpecifier.isProperty = true;
		        	propertySpecifier.name = "operations";
		        	propertySpecifier.parent = currentInstance;

			        if (currentInstance != null)
			        	currentInstance.propertySpecifiers.add(propertySpecifier);
			        objectSpecifier = new MXMLDescriptorSpecifier();
			        objectSpecifier.isProperty = false;
			        objectSpecifier.name = formatQualifiedName(IASLanguageConstants.Object);
			        objectSpecifier.parent = propertySpecifier;
			        propertySpecifier.propertySpecifiers.add(objectSpecifier);
			        instances.add(objectSpecifier);
		        }
	            moveDown(false, objectSpecifier, null);
                getMXMLWalker().walk(child); // RemoteObjectMethod
	            moveUp(false, true);
			}
		}
	}
}
