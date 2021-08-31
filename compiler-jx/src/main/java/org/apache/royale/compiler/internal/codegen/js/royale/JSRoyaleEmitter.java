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
import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleEmitter;
import org.apache.royale.compiler.codegen.js.goog.IJSGoogDocEmitter;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.definitions.references.INamespaceResolvedReference;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSessionModel.ImplicitBindableImplementation;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.jx.AccessorEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.AsIsEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.BinaryOperatorEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.BindableEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ClassEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.DefinePropertyFunctionEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.FieldEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ForEachEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.FunctionCallEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.IdentifierEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.InterfaceEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.LiteralEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.MemberAccessEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.MethodEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ObjectDefinePropertyEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.PackageHeaderEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.SelfReferenceEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.SuperCallEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.VarDeclarationEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.BinaryOperatorEmitter.DatePropertiesGetters;
import org.apache.royale.compiler.internal.codegen.js.jx.BinaryOperatorEmitter.DatePropertiesSetters;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyaleEmitter;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.embedding.EmbedMIMEType;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.problems.EmbedUnableToReadSourceProblem;
import org.apache.royale.compiler.problems.FilePrivateItemsWithMainVarWarningProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.utils.ASNodeUtils;

import com.google.common.base.Joiner;
import org.apache.royale.compiler.utils.NativeUtils;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Concrete implementation of the 'Royale' JavaScript production.
 *
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSRoyaleEmitter extends JSGoogEmitter implements IJSRoyaleEmitter
{

    private JSRoyaleDocEmitter docEmitter = null;

    private PackageHeaderEmitter packageHeaderEmitter;
    public PackageFooterEmitter packageFooterEmitter;

    private BindableEmitter bindableEmitter;

    private ClassEmitter classEmitter;
    private InterfaceEmitter interfaceEmitter;

    private FieldEmitter fieldEmitter;
    public VarDeclarationEmitter varDeclarationEmitter;
    public AccessorEmitter accessorEmitter;
    public MethodEmitter methodEmitter;

    private FunctionCallEmitter functionCallEmitter;
    private SuperCallEmitter superCallEmitter;
    private ForEachEmitter forEachEmitter;
    private MemberAccessEmitter memberAccessEmitter;
    private BinaryOperatorEmitter binaryOperatorEmitter;
    private IdentifierEmitter identifierEmitter;
    private LiteralEmitter literalEmitter;

    private AsIsEmitter asIsEmitter;
    private SelfReferenceEmitter selfReferenceEmitter;
    private ObjectDefinePropertyEmitter objectDefinePropertyEmitter;
    private DefinePropertyFunctionEmitter definePropertyFunctionEmitter;

    public ArrayList<String> usedNames = new ArrayList<String>();
    public ArrayList<String> staticUsedNames = new ArrayList<String>();
    private boolean needNamespace;
    
    private Set<IFunctionNode> emittingHoistedNodes = new HashSet<IFunctionNode>();

    @Override
    public String postProcess(String output)
    {
        output = super.postProcess(output);

    	String[] lines = output.split("\n");
    	ArrayList<String> finalLines = new ArrayList<String>();
        boolean foundLanguage = false;
        boolean foundXML = false;
        boolean foundNamespace = false;
        boolean sawRequires = false;
    	boolean stillSearching = true;
        int addIndex = -1;
        int provideIndex = -1;
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
                        provideIndex = addIndex = i + 1;
                    }
                }
	            int c = line.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	            if (c != -1)
	            {
                    // we found other requires, so we'll just add Language at
                    // the end of the list
                    addIndex = -1;
	                int c2 = line.indexOf(")");
	                String s = line.substring(c + 14, c2 - 1);
                    if (s.equals(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken()))
                    {
                        foundLanguage = true;
                    }
                    else if (s.equals(IASLanguageConstants.XML))
                    {
                        foundXML = true;
                    }
                    else if (s.equals(IASLanguageConstants.Namespace))
                    {
                        foundNamespace = true;
                    }
	    			sawRequires = true;
	    			/*
	    			if (!usedNames.contains(s))
                    {
                        removeLineFromMappings(i);
                        continue;
                    }
                    */
	    		}
	    		else if (sawRequires || i == len - 1)
                {
                    stillSearching = false;

                    //when we emitted the requires based on the imports, we may
                    //not have known if Language was needed yet because the
                    //imports are at the beginning of the file. other code,
                    //later in the file, may require Language.
                    ICompilerProject project = getWalker().getProject();
                    if (project instanceof RoyaleJSProject)
                    {
                        RoyaleJSProject royaleProject = (RoyaleJSProject) project;
                        boolean needLanguage = getModel().needLanguage;
                        if (needLanguage && !foundLanguage)
                        {
                            StringBuilder appendString = new StringBuilder();
                            appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken());
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
                            appendString.append(ASEmitterTokens.SEMICOLON.getToken());
                            if(addIndex != -1)
                            {
                                // if we didn't find other requires, this index
                                // points to the line after goog.provide
                                finalLines.add(addIndex, appendString.toString());
                                addLineToMappings(addIndex);
                            }
                            else
                            {
                                finalLines.add(appendString.toString());
                                addLineToMappings(i);
                            }
                        }
                        boolean needXML = royaleProject.needXML;
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
                            if(addIndex != -1)
                            {
                                // if we didn't find other requires, this index
                                // points to the line after goog.provide
                                finalLines.add(addIndex, appendString.toString());
                                addLineToMappings(addIndex);
                            }
                            else
                            {
                                finalLines.add(appendString.toString());
                                addLineToMappings(i);
                            }
                        }
                        if (needNamespace && !foundNamespace)
                        {
                            StringBuilder appendString = new StringBuilder();
                            appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(IASLanguageConstants.Namespace);
                            appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                            appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
                            appendString.append(ASEmitterTokens.SEMICOLON.getToken());
                            if(addIndex != -1)
                            {
                                // if we didn't find other requires, this index
                                // points to the line after goog.provide
                                finalLines.add(addIndex, appendString.toString());
                                addLineToMappings(addIndex);
                            }
                            else
                            {
                                finalLines.add(appendString.toString());
                                addLineToMappings(i);
                            }
                        }
                    }
                }
    		}
    		finalLines.add(line);
    	}
		if (staticUsedNames.size() > 0)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(JSGoogEmitterTokens.ROYALE_STATIC_DEPENDENCY_LIST.getToken());
			boolean firstDependency = true;
			for (String staticName : staticUsedNames)
			{
				if (!firstDependency)
					sb.append(",");
				firstDependency = false;
				sb.append(staticName);
			}
			sb.append("*/");
            finalLines.add(provideIndex, sb.toString());
            addLineToMappings(provideIndex);
		}

    	return Joiner.on("\n").join(finalLines);
    }

    public BindableEmitter getBindableEmitter()
    {
        return bindableEmitter;
    }

    public FieldEmitter getFieldEmitter()
    {
        return fieldEmitter;
    }

    public ClassEmitter getClassEmitter()
    {
        return classEmitter;
    }

    public AccessorEmitter getAccessorEmitter()
    {
        return accessorEmitter;
    }

    public PackageFooterEmitter getPackageFooterEmitter()
    {
        return packageFooterEmitter;
    }

    // TODO (mschmalle) Fix; this is not using the backend doc strategy for replacement
    @Override
    public IJSGoogDocEmitter getDocEmitter()
    {
        if (docEmitter == null)
            docEmitter = new JSRoyaleDocEmitter(this);
        return docEmitter;
    }

    public JSRoyaleEmitter(FilterWriter out)
    {
        super(out);

        packageHeaderEmitter = new PackageHeaderEmitter(this);
        packageFooterEmitter = new PackageFooterEmitter(this);

        bindableEmitter = new BindableEmitter(this);

        classEmitter = new ClassEmitter(this);
        interfaceEmitter = new InterfaceEmitter(this);

        fieldEmitter = new FieldEmitter(this);
        varDeclarationEmitter = new VarDeclarationEmitter(this);
        accessorEmitter = new AccessorEmitter(this);
        methodEmitter = new MethodEmitter(this);

        functionCallEmitter = new FunctionCallEmitter(this);
        superCallEmitter = new SuperCallEmitter(this);
        forEachEmitter = new ForEachEmitter(this);
        memberAccessEmitter = new MemberAccessEmitter(this);
        binaryOperatorEmitter = new BinaryOperatorEmitter(this);
        identifierEmitter = new IdentifierEmitter(this);
        literalEmitter = new LiteralEmitter(this);

        asIsEmitter = new AsIsEmitter(this);
        selfReferenceEmitter = new SelfReferenceEmitter(this);
        objectDefinePropertyEmitter = new ObjectDefinePropertyEmitter(this);
        definePropertyFunctionEmitter = new DefinePropertyFunctionEmitter(this);

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
    public void emitLocalNamedFunction(IFunctionNode node)
    {
        IFunctionNode parentFnNode = (IFunctionNode) node.getAncestorOfType(IFunctionNode.class);
        if (parentFnNode == null || isEmittingHoistedNodes(parentFnNode))
    	{
            // emit named functions only when allowed
    		super.emitLocalNamedFunction(node);
        }
    }

    public Boolean isEmittingHoistedNodes(IFunctionNode node)
    {
        return emittingHoistedNodes.contains(node);
    }

    protected void setEmittingHoistedNodes(IFunctionNode node, boolean emit)
    {
        if (emit)
        {
            emittingHoistedNodes.add(node);
        }
        else
        {
            emittingHoistedNodes.remove(node);
        }
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        setEmittingHoistedNodes(node, true);
    	super.emitFunctionBlockHeader(node);
    	if (node.isConstructor())
    	{
            IClassNode cnode = (IClassNode) node.getAncestorOfType(IClassNode.class);
            if (cnode.getDefinition().needsEventDispatcher(getWalker().getProject())) {
                //add whatever variant of the implementation is necessary inside the constructor
                if (getModel().getImplicitBindableImplementation() == ImplicitBindableImplementation.IMPLEMENTS)
                    bindableEmitter.emitBindableImplementsConstructorCode();
                else if (getModel().getImplicitBindableImplementation() == ImplicitBindableImplementation.EXTENDS)
                    bindableEmitter.emitBindableExtendsConstructorCode(cnode.getDefinition().getQualifiedName(),false);
            }
            emitComplexInitializers(cnode);
    	}

        emitHoistedFunctionsCodeBlock(node);

        if (!getModel().isExterns)
            emitHoistedVariablesCodeBlock(node);

        setEmittingHoistedNodes(node, false);
    }

    protected void emitHoistedFunctionsCodeBlock(IFunctionNode node)
    {
        if (!node.containsLocalFunctions())
        {
            return;
        }
        List<IFunctionNode> localFns = node.getLocalFunctions();
        int n = localFns.size();
        for (int i = 0; i < n; i++)
        {
            IFunctionNode localFn = localFns.get(i);
            // named functions need to be declared at the top level to
            // comply with JS strict mode.
            // anonymous functions can be emitted inline, so we'll do that.
            if (localFn.getName().length() > 0)
            {
                getWalker().walk(localFn);
                write(ASEmitterTokens.SEMICOLON);
                this.writeNewline();
            }
        }
    }

    protected void emitHoistedVariablesCodeBlock(IFunctionNode node)
    {
        boolean defaultInitializers = false;
        ICompilerProject project = getWalker().getProject();
        if(project instanceof RoyaleJSProject)
        {
            RoyaleJSProject fjsProject = (RoyaleJSProject) project; 
            if(fjsProject.config != null)
            {
                defaultInitializers = fjsProject.config.getJsDefaultInitializers();
            }
        }
        Collection<IDefinition> localDefs = node.getScopedNode().getScope().getAllLocalDefinitions();
        for (IDefinition localDef : localDefs)
        {
            if (localDef instanceof IVariableDefinition)
            {
                IVariableDefinition varDef = (IVariableDefinition) localDef;
                IVariableNode varNode = varDef.getVariableNode();
                if (varNode == null)
                {
                    //no associated node is possible for implicit variables,
                    //like "arguments"
                    continue;
                }
                if (varNode instanceof ChainedVariableNode)
                {
                    //these will be handled from the first variable in the chain
                    continue;
                }
                if (EmitterUtils.needsDefaultValue(varNode, defaultInitializers, getWalker().getProject()))
                {
                    emitVarDeclaration(varNode);
                    write(ASEmitterTokens.SEMICOLON);
                    writeNewline();
                }
                else
                {
                    //when the first varible in a chain doesn't need a default
                    //value, we need to manually check the rest of the chain
                    int len = varNode.getChildCount();
                    for(int i = 0; i < len; i++)
                    {
                        IASNode child = varNode.getChild(i);
                        if(child instanceof ChainedVariableNode)
                        {
                            ChainedVariableNode childVarNode = (ChainedVariableNode) child;
                            if (EmitterUtils.needsDefaultValue(childVarNode, defaultInitializers, getWalker().getProject()))
                            {
                                writeToken(ASEmitterTokens.VAR);
                                emitVarDeclaration(childVarNode);
                                write(ASEmitterTokens.SEMICOLON);
                                writeNewline();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
		IFunctionNode parentFnNode = (IFunctionNode) node.getAncestorOfType(IFunctionNode.class);
        IFunctionNode localFnNode = node.getFunctionNode();
    	if (parentFnNode == null || isEmittingHoistedNodes(parentFnNode))
    	{
            // emit named functions only when allowed
    		super.emitFunctionObject(node);
        }
        else if(localFnNode.getName().length() == 0)
        {
            // anonymous functions are allowed to be inline
    		super.emitFunctionObject(node);
        }
    	else
    	{
            List<IFunctionNode> localFns = parentFnNode.getLocalFunctions();
            int i = localFns.indexOf(node.getFunctionNode());
            if (i < 0)
            	System.out.println("missing index for " + node.toString());
            else
            {
            	IFunctionNode localFn = localFns.get(i);
            	IExpressionNode idNode = localFn.getNameExpressionNode();
            	String fnName = "";
            	if (idNode != null && idNode.getNodeID() == ASTNodeID.IdentifierID)
            		fnName = ((IdentifierNode)idNode).getName();
            	if (!fnName.isEmpty())
            		write(((IdentifierNode)idNode).getName());
            	else
            		write("__localFn" + Integer.toString(i) + "__");
            }
    	}
    }

    @Override
    public void emitNamespace(INamespaceNode node)
    {
    	needNamespace = true;
    	if (node.getContainingScope().getScope() instanceof PackageScope) {
            startMapping(node);
            write(formatQualifiedName(node.getQualifiedName()));
            endMapping(node);
        } else if (node.getContainingScope().getScope() instanceof FunctionScope){
            writeToken(ASEmitterTokens.VAR);
            write("/** @type {"+IASLanguageConstants.Namespace+"} */");
            startMapping(node);
            write(node.getName());
            endMapping(node);
        } else {
    	    //class member - static scope
            if (!(node.getDefinition().getParent() instanceof IClassDefinition)) {
                startMapping(node);
                write(node.getQualifiedName());
                endMapping(node);
            } else {
                write(node.getDefinition().getParent().getQualifiedName());
                write(ASEmitterTokens.MEMBER_ACCESS);
                startMapping(node);
                write(node.getName());
                endMapping(node);
            }
        }
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        writeToken(ASEmitterTokens.NEW);
        startMapping(node);
        write(IASLanguageConstants.Namespace);
        endMapping(node);
        write(ASEmitterTokens.PAREN_OPEN);
        if (!staticUsedNames.contains(IASLanguageConstants.Namespace))
        	staticUsedNames.add(IASLanguageConstants.Namespace);
        IExpressionNode uriNode = node.getNamespaceURINode();
        if (uriNode == null)
        {
            startMapping(node);
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(node.getName());
            write(ASEmitterTokens.SINGLE_QUOTE);
            endMapping(node);
        }
        else
        	getWalker().walk(uriNode);
        write(ASEmitterTokens.PAREN_CLOSE);
    }
    
    @Override
    public void emitUseNamespace(IUseNamespaceNode node)
    {
        write("//use namespace ");
        getWalker().walk(node.getTargetNamespaceNode());
    }

    public boolean isCustomNamespace(FunctionNode node)
    {
		INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
		if (ns != null)
		{
            String nsName = node.getNamespace();
            if (!(nsName == IASKeywordConstants.PRIVATE ||
                nsName == IASKeywordConstants.PROTECTED ||
                nsName == IASKeywordConstants.INTERNAL ||
                nsName == INamespaceConstants.AS3URI ||
                nsName == IASKeywordConstants.PUBLIC))
            {
            	return true;
            }
		}
		return false;
    }

    public boolean isCustomNamespace(FunctionDefinition def)
    {
		INamespaceDefinition nsDef = def.getNamespaceReference().resolveNamespaceReference(getWalker().getProject());
		String uri = nsDef.getURI();
		if (!def.getNamespaceReference().isLanguageNamespace() && !uri.equals(INamespaceConstants.AS3URI) &&
						!nsDef.getBaseName().equals(ASEmitterTokens.PRIVATE.getToken()))
			return true;
		return false;
    }

    @Override
    public void emitMemberName(IDefinitionNode node)
    {
        ICompilerProject project = getWalker().getProject();
        if (node.getNodeID() == ASTNodeID.FunctionID)
        {
            FunctionNode fn = (FunctionNode)node;
            if (isCustomNamespace(fn))
            {
                INamespaceDecorationNode ns = ((FunctionNode)node).getActualNamespaceNode();
                INamespaceDefinition nsDef = (INamespaceDefinition)ns.resolve(project);
                formatQualifiedName(nsDef.getQualifiedName()); // register with used names
                String s = nsDef.getURI();
                write(formatNamespacedProperty(s, node.getName(), true));
                return;
            }
        }
        String qname = node.getName();
        IDefinition nodeDef = node.getDefinition();
        if (nodeDef != null && !nodeDef.isStatic() && nodeDef.isPrivate() && project.getAllowPrivateNameConflicts())
            qname = formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);
        write(qname);
    }

    public static String formatNamespacedProperty(String s, String propName, boolean access) {
    	//closure compiler choked on this syntax so stop using bracket notation for now
    	//if (access) return "[\"" + s + "::" + propName + "\"]";
    	//return "\"" + s + "::" + propName + "\"";
    	s = s.replace(":", "_");
    	s = s.replace(".", "_");
    	s = s.replace("/", "$");
    	s += "__" + propName;
    	if (access)
    		s = ASEmitterTokens.MEMBER_ACCESS.getToken() + s;
    	return s;
	}

	@Override
    public String formatQualifiedName(String name)
    {
        return formatQualifiedName(name, false);
    }

    public MXMLRoyaleEmitter mxmlEmitter = null;

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
        	if (getModel().inStaticInitializer)
        		if (!staticUsedNames.contains(name) && !NativeUtils.isJSNative(name)
        				&& isGoogProvided(name) && (getModel().getCurrentClass() == null || !getModel().getCurrentClass().getQualifiedName().equals(name))
        				&& (getModel().primaryDefinitionQName == null
        					|| !getModel().primaryDefinitionQName.equals(name)))
        			staticUsedNames.add(name);
    		
    		if (!usedNames.contains(name) && isGoogProvided(name))
    			usedNames.add(name);
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
        else if (name.equals(IASLanguageConstants.Vector) || name.equals("__AS3__.vec.Vector")) {
            result = JSRoyaleEmitterTokens.VECTOR.getToken();
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
        String mainClassName = null;
        Boolean mainClassNameisVar = false;
        IASNode firstInternalContent = null;
        for (int i = 0; i < nodeCount; i++)
        {
            IASNode pnode = fileNode.getChild(i);
            
            if (pnode instanceof IPackageNode)
            {
                IScopedNode snode = ((IPackageNode)pnode).getScopedNode();
                int snodeCount = snode.getChildCount();
                for (int j = 0; j < snodeCount; j++)
                {
                    //there can only be one externally visible definition of either class, namespace, variable or function
                    //and package scope does not permit other class level access modifiers, otherwise a compiler error has already occurred.
                    //So mainClassName is derived from the first instance of any of these.
                    IASNode cnode = snode.getChild(j);
                    if (cnode instanceof IClassNode)
                    {
                        mainClassName = ((IClassNode)cnode).getQualifiedName();
                        break;
                    }
                    else if (cnode instanceof IFunctionNode)
                    {
                        mainClassName = ((IFunctionNode)cnode).getQualifiedName();
                        break;
                    }
                    else if (cnode instanceof INamespaceNode)
                    {
                        mainClassName = ((INamespaceNode)cnode).getQualifiedName();
                        break;
                    }
                    else if (cnode instanceof IVariableNode)
                    {
                        mainClassName = ((IVariableNode)cnode).getQualifiedName();
                        mainClassNameisVar = true;
                        break;
                    }
                }
            }
            else if (pnode instanceof IClassNode)
            {
                String className = ((IClassNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
                if (firstInternalContent == null) firstInternalContent = pnode;
            }
            else if (pnode instanceof IInterfaceNode)
            {
                String className = ((IInterfaceNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
                if (firstInternalContent == null) firstInternalContent = pnode;
            }
            else if (pnode instanceof IFunctionNode)
            {
                String className = ((IFunctionNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
                if (firstInternalContent == null) firstInternalContent = pnode;
            }
            else if (pnode instanceof INamespaceNode)
            {
                String className = ((INamespaceNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
                if (firstInternalContent == null) firstInternalContent = pnode;
            }
            else if (pnode instanceof IVariableNode)
            {
                String className = ((IVariableNode)pnode).getQualifiedName();
                getModel().getInternalClasses().put(className, mainClassName + "." + className);
                if (firstInternalContent == null) firstInternalContent = pnode;
            }
        }
        if (mainClassNameisVar && firstInternalContent != null) {
            getProblems().add(new FilePrivateItemsWithMainVarWarningProblem(firstInternalContent));
        }
        packageHeaderEmitter.emit(definition);
        
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        packageHeaderEmitter.emitContents(definition);
        usedNames.clear();
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        packageFooterEmitter.emit(definition);
    }

    //--------------------------------------------------------------------------
    // Class
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        classEmitter.emit(node);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        interfaceEmitter.emit(node);
    }

    @Override
    public void emitField(IVariableNode node)
    {
        fieldEmitter.emit(node);
    }

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
        varDeclarationEmitter.emit(node);
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
        accessorEmitter.emit(node);
    }

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        accessorEmitter.emitGet(node);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        accessorEmitter.emitSet(node);
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        methodEmitter.emit(node);
    }

    public void emitComplexInitializers(IClassNode node)
    {
    	classEmitter.emitComplexInitializers(node);
    }

    //--------------------------------------------------------------------------
    // Statements
    //--------------------------------------------------------------------------

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        functionCallEmitter.emit(node);
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        forEachEmitter.emit(node);
    }

    //--------------------------------------------------------------------------
    // Expressions
    //--------------------------------------------------------------------------

    @Override
    public void emitSuperCall(IASNode node, String type)
    {
        superCallEmitter.emit(node, type);
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        memberAccessEmitter.emit(node);
    }

    @Override
    public void emitArguments(IContainerNode node)
    {
        IContainerNode newNode = node;
        int len = node.getChildCount();
    	if (len == 2)
    	{
            ICompilerProject project = getWalker().getProject();;
    		IFunctionCallNode fcNode = (IFunctionCallNode) node.getParent();
    		IExpressionNode nameNode = fcNode.getNameNode();
            IDefinition def = nameNode.resolve(project);
        	if (def != null && def.getBaseName().equals("insertAt"))
        	{
        		if (def.getParent() != null &&
            		def.getParent().getQualifiedName().equals("Array"))
        		{
            		if (nameNode instanceof MemberAccessExpressionNode)
            		{
                        newNode = EmitterUtils.insertArgumentsAt(node, 1, new NumericLiteralNode("0"));
            		}
    			}
    		}
    	}
        if (len == 1)
        {
            ICompilerProject project = getWalker().getProject();;
            IFunctionCallNode fcNode = (IFunctionCallNode) node.getParent();
            IExpressionNode nameNode = fcNode.getNameNode();
            IDefinition def = nameNode.resolve(project);
            if (def != null && def.getBaseName().equals("removeAt"))
            {
                if (def.getParent() != null &&
                        def.getParent().getQualifiedName().equals("Array"))
                {
                    if (nameNode instanceof MemberAccessExpressionNode)
                    {
                        newNode = EmitterUtils.insertArgumentsAfter(node, new NumericLiteralNode("1"));
                    }
                }
            }
            else if (def != null && def.getBaseName().equals("parseInt"))
            {
                IDefinition parentDef = def.getParent();
                if (parentDef == null)
                {
                    if (nameNode instanceof IdentifierNode)
                    {
                        //see FLEX-35283
                        LiteralNode appendedArgument = new NumericLiteralNode("undefined");
                        appendedArgument.setSynthetic(true);
                        newNode = EmitterUtils.insertArgumentsAfter(node, appendedArgument);
                    }
                }
            }
        }
        super.emitArguments(newNode);
    }

    @Override
    public void emitE4XFilter(IMemberAccessExpressionNode node)
    {
    	getWalker().walk(node.getLeftOperandNode());
    	getModel().inE4xFilter = true;
    	String s = stringifyNode(node.getRightOperandNode());
    	if (s.startsWith("(") && s.endsWith(")"))
    		s = s.substring(1, s.length() - 1);
        if (s.contains("this")) {
            //it *could* be in some string content, but most likely is an actual 'this' reference
            //so always bind to 'this' to correctly resolve the external references for the instance scope of the original code.
            write(".filter((function(/** @type {XML} */ node){return (");
            write(s);
            write(")})");
            write(".bind(this))");
        } else {
            write(".filter(function(/** @type {XML} */ node){return (");
            write(s);
            write(")})");
        }
    	getModel().inE4xFilter = false;
    }
    
    @Override
    public  void emitE4XDefaultNamespaceDirective(IDefaultXMLNamespaceNode node){
        //leave a comment at the original code location
        write("// e4x default namespace active: ");
        getWalker().walk(node.getKeywordNode());
        write(" = ");
        getWalker().walk(node.getExpressionNode());
        //register the namespace for the current function scope (and only for function scopes (observed behavior) )
        if (node.getContainingScope().getScope() instanceof FunctionScope) {
            getModel().registerDefaultXMLNamespace(((FunctionScope)node.getContainingScope().getScope()), node.getExpressionNode());
        }
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        binaryOperatorEmitter.emit(node);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        identifierEmitter.emit(node);
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        literalEmitter.emit(node);
    }

    @Override
    public void emitEmbed(IEmbedNode node)
    {
    	// if the embed is text/plain, return the actual text from the file.
    	// this assumes the variable being initialized is of type String.
    	// Embed node seems to not have location, so use parent.
        EmbedData data = new EmbedData(node.getParent().getSourcePath(), null);
        boolean hadError = false;
        for (IMetaTagAttribute attribute : node.getAttributes())
        {
            String key = attribute.getKey();
            String value = attribute.getValue();
            if (data.addAttribute((CompilerProject) project, node.getParent(), key, value, getProblems()))
            {
                hadError = true;
            }
        }
        if (hadError)
        {
        	write("");
        	return;
        }
    	String source = (String) data.getAttribute(EmbedAttribute.SOURCE);
    	EmbedMIMEType mimeType = (EmbedMIMEType) data.getAttribute(EmbedAttribute.MIME_TYPE);
        if (mimeType != null && mimeType.toString().equals(EmbedMIMEType.TEXT.toString()) && source != null)
        {
            File file = new File(FilenameNormalization.normalize(source));
    		try {
    	        String newlineReplacement = "\\\\n";
				String s = FileUtils.readFileToString(file);
	            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
	            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
	            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
	            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
	            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
	            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
	            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
	            s = s.replaceAll("\"", "\\\\\"");
	            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
	            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
	            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
	            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
	            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
	            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
	            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", newlineReplacement);
				write("\"" + s + "\"");
			} catch (IOException e) {
	            getProblems().add(new EmbedUnableToReadSourceProblem(e, file.getPath()));
			}
        }
    }


    //--------------------------------------------------------------------------
    // Specific
    //--------------------------------------------------------------------------

    public void emitIsAs(IExpressionNode node, IExpressionNode left, IExpressionNode right, ASTNodeID id, boolean coercion)
    {
        asIsEmitter.emitIsAs(node, left, right, id, coercion);
    }

    @Override
    protected void emitSelfReference(IFunctionNode node)
    {
        selfReferenceEmitter.emit(node);
    }

    @Override
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        objectDefinePropertyEmitter.emit(node);
    }

    @Override
    public void emitDefinePropertyFunction(IAccessorNode node)
    {
        definePropertyFunctionEmitter.emit(node);
    }

    public String stringifyDefineProperties(IClassDefinition cdef)
    {
    	setBufferWrite(true);
    	accessorEmitter.emit(cdef);
        String result = getBuilder().toString();
        getBuilder().setLength(0);
        setBufferWrite(false);
        return result;
    }

    @Override
	public void emitClosureStart()
    {
        if (getDocEmitter() instanceof JSRoyaleDocEmitter && ((JSRoyaleDocEmitter) getDocEmitter()).getSuppressClosure()) return;
        ICompilerProject project = getWalker().getProject();
        if (project instanceof RoyaleJSProject)
        	((RoyaleJSProject)project).needLanguage = true;
        getModel().needLanguage = true;
        write(JSRoyaleEmitterTokens.CLOSURE_FUNCTION_NAME);
        write(ASEmitterTokens.PAREN_OPEN);
    }

    @Override
	public void emitClosureEnd(IASNode node, IDefinition nodeDef)
    {
        if (getDocEmitter() instanceof JSRoyaleDocEmitter && ((JSRoyaleDocEmitter) getDocEmitter()).getSuppressClosure()) return;
    	write(ASEmitterTokens.COMMA);
    	write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.SINGLE_QUOTE);
        
        IIdentifierNode identifierNode = null;
    	if (node.getNodeID() == ASTNodeID.IdentifierID)
    	{
            identifierNode = (IIdentifierNode) node;
    	}
        else if (node.getNodeID() == ASTNodeID.MemberAccessExpressionID)
        {
            identifierNode = getChainedIdentifierNode(node);
        }
        if (identifierNode == null)
        {
            System.out.println("unexpected node in emitClosureEnd");
        }
        else
        {
            if (!node.equals(identifierNode))
            {
                nodeDef = identifierNode.resolve(getWalker().getProject());
            }

            if (nodeDef instanceof FunctionDefinition &&
        			isCustomNamespace((FunctionDefinition)nodeDef))
        	{
            	String ns = ((INamespaceResolvedReference)((FunctionDefinition)nodeDef).getNamespaceReference()).resolveAETNamespace(getWalker().getProject()).getName();
            	write(ns + "::");
            }

            String qname = nodeDef.getBaseName();
            if (nodeDef != null && !nodeDef.isStatic() && (!(nodeDef instanceof IParameterDefinition)) && nodeDef.isPrivate() && getWalker().getProject().getAllowPrivateNameConflicts())
                qname = formatPrivateName(nodeDef.getParent().getQualifiedName(), qname);
    		write(qname);
        }
    	write(ASEmitterTokens.SINGLE_QUOTE);
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitStatement(IASNode node)
    {
    	if (node instanceof IFunctionNode)
    	{
            IFunctionNode fnode = (IFunctionNode) node;
            if(fnode.getName().length() > 0)
            {
                // don't emit named local functions as statements, if they have
                // a name. these functions are emitted as part of the function
                // block header because that is required by JS strict mode.
                // anonymous functions (functions without a name) are allowed.
                return;
            }
    	}
    	super.emitStatement(node);
    }

    private IIdentifierNode getChainedIdentifierNode(IASNode node)
    {
    	while (node.getNodeID() == ASTNodeID.MemberAccessExpressionID)
    	{
    		node = ((IMemberAccessExpressionNode)node).getRightOperandNode();
    	}
        if (node.getNodeID() == ASTNodeID.IdentifierID)
        {
            return (IIdentifierNode) node;
        }
        return null;
    }

    @Override
    public void emitUnaryOperator(IUnaryOperatorNode node)
    {
        if (node.getNodeID() == ASTNodeID.Op_DeleteID)
        {
        	if (node.getChild(0).getNodeID() == ASTNodeID.ArrayIndexExpressionID)
        	{
        		if (node.getChild(0).getChild(0).getNodeID() == ASTNodeID.MemberAccessExpressionID)
        		{
        			MemberAccessExpressionNode obj = (MemberAccessExpressionNode)(node.getChild(0).getChild(0));
        			if (isXMLList(obj))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);

        	            getWalker().walk(obj);
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".removeChildAt(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;
        			}
        		}
        		else if (node.getChild(0).getChild(0).getNodeID() == ASTNodeID.IdentifierID)
        		{
        			if (isXMLish((IdentifierNode)(node.getChild(0).getChild(0))))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);

        	            getWalker().walk(node.getChild(0).getChild(0));
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".removeChild(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;
        			}
        			else if (isProxy((IdentifierNode)(node.getChild(0).getChild(0))))
        			{
        		        if (ASNodeUtils.hasParenOpen(node))
        		            write(ASEmitterTokens.PAREN_OPEN);

        	            getWalker().walk(node.getChild(0).getChild(0));
        	            DynamicAccessNode dan = (DynamicAccessNode)(node.getChild(0));
        	            IASNode indexNode = dan.getChild(1);
        	            write(".deleteProperty(");
        	            getWalker().walk(indexNode);
        	            write(")");
        		        if (ASNodeUtils.hasParenClose(node))
        		            write(ASEmitterTokens.PAREN_CLOSE);
        		        return;
        			}
        		}

        	}
        	else if (node.getChild(0).getNodeID() == ASTNodeID.MemberAccessExpressionID)
    		{
    			MemberAccessExpressionNode obj = (MemberAccessExpressionNode)(node.getChild(0));
    			if (isXMLList(obj))
    			{
    		        if (ASNodeUtils.hasParenOpen(node))
    		            write(ASEmitterTokens.PAREN_OPEN);

    		        IExpressionNode rightNode = obj.getRightOperandNode();
    	            String s = stringifyNode(obj.getLeftOperandNode());
    	            boolean avoidMethodAccess = (rightNode instanceof IDynamicAccessNode && ((IDynamicAccessNode) rightNode).getLeftOperandNode().getNodeID() == ASTNodeID.Op_AtID);
    	            boolean needsQuotes = rightNode.getNodeID() != ASTNodeID.Op_AtID
                             && !avoidMethodAccess;
    	            write(s);
    	            write(".removeChild(");
    	            if (needsQuotes)
    	            	write("'");
    	            else
    	            	if (!avoidMethodAccess) write(s + ".");
    	            s = stringifyNode(rightNode);
    	            write(s);
    	            if (needsQuotes)
    	            	write("'");
    	            write(")");
    		        if (ASNodeUtils.hasParenClose(node))
    		            write(ASEmitterTokens.PAREN_CLOSE);
    		        return;
    			}
    		}

        }
        else if (node.getNodeID() == ASTNodeID.Op_AtID)
        {
        	IASNode op = node.getOperandNode();
        	if (op != null)
        	{
        		if (EmitterUtils.writeE4xFilterNode(getWalker().getProject(), getModel(), node))
        			write("node.");
            	write("attribute(");
            	if (op instanceof INamespaceAccessExpressionNode) {
            	    //parent.@ns::attributeName
                    write("XML.createAttributeQName(");
                    write("new QName(");
                    getWalker().walk(((INamespaceAccessExpressionNode) op).getLeftOperandNode());
                    write(",'");
                    getWalker().walk(((INamespaceAccessExpressionNode) op).getRightOperandNode());
                    write("')");
                    write(")");
                } else {
            	    if (getModel().defaultXMLNamespaceActive
                            && op instanceof IIdentifierNode
                            && node.getContainingScope().getScope() instanceof FunctionScope
                            && getModel().getDefaultXMLNamespace((FunctionScope)(node.getContainingScope().getScope())) !=null) {
            	        //apply default namespace to this attributes query
                        //parent.@attributeName (with default xml namespace set in scope)
                        write("XML.swfCompatibleQuery(");
            	        write("new QName(");
                        getWalker().walk(getModel().getDefaultXMLNamespace((FunctionScope)(node.getContainingScope().getScope())));
                        write(",'");
                        getWalker().walk(node.getOperandNode());
                        write("')");
                        write(", true"); //attribute flag
                        write(")");
                    } else {
            	        //default namespace is ''
            	        write("'");
                        getWalker().walk(node.getOperandNode());
                        write("'");
                    }
                }
            	write(")");
        	}
        	else if (node.getParent().getNodeID() == ASTNodeID.ArrayIndexExpressionID)
        	{
        		DynamicAccessNode parentNode = (DynamicAccessNode)node.getParent();
        		if (EmitterUtils.writeE4xFilterNode(getWalker().getProject(), getModel(), node))
        			write("node.");
        		if (node.getParent().getParent() != null
                        && node.getParent().getParent().getParent() != null
                        && node.getParent().getParent().getParent().getNodeID() == ASTNodeID.Op_DeleteID) {
        		    write ("'@' + ");
        		    //example : delete myXML.@[MyConst];
        		    //output myXML.removeChild('@' + MyConst) <-- variant is handled in emulation class
                    getWalker().walk(parentNode.getRightOperandNode());
                    return;
                }
            	write("attribute(");
        		getWalker().walk(parentNode.getRightOperandNode());
            	write(")");
        	}
        	
        	return;
        }

        super.emitUnaryOperator(node);

    }

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * We want to know not just whether the node is of type XML,
     * but whether it is a property of a property of type XML.
     * For example, this.foo might be XML or XMLList, but since
     * 'this' isn't also XML, we return false.  That's because
     * assignment to this.foo shouldn't use setChild() but
     * just do an assignment.
     * @param obj
     * @return
     */
    public boolean isXMLList(IMemberAccessExpressionNode obj)
    {
		return EmitterUtils.isXMLList(obj, getWalker().getProject());
    }

    public boolean isLeftNodeXMLish(IExpressionNode leftNode)
    {
    	return EmitterUtils.isLeftNodeXMLish(leftNode, getWalker().getProject());
    }

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isProxy(IExpressionNode obj)
    {
		RoyaleProject project = (RoyaleProject)getWalker().getProject();
		// See if it is Proxy
		ITypeDefinition leftDef = obj.resolveType(project);
		if (leftDef == null)
		{
			if (obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
			{
				IExpressionNode leftNode = ((MemberAccessExpressionNode)obj).getLeftOperandNode();
				leftDef = leftNode.resolveType(project);
				if (leftDef != null && leftDef.isInstanceOf(project.getProxyBaseClass(), project))
					return true;
				while (leftNode.getNodeID() == ASTNodeID.MemberAccessExpressionID)
				{
					// walk up chain looking for a proxy
					leftNode = ((MemberAccessExpressionNode)leftNode).getLeftOperandNode();
					leftDef = leftNode.resolveType(project);
					if (leftDef != null && leftDef.isInstanceOf(project.getProxyBaseClass(), project))
						return true;
				}
			}
			return false;
		}
		return leftDef.isInstanceOf(project.getProxyBaseClass(), project);
    }

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isDateProperty(IExpressionNode obj, boolean writeAccess)
    {
		RoyaleProject project = (RoyaleProject)getWalker().getProject();
		if (obj.getNodeID() == ASTNodeID.MemberAccessExpressionID)
		{
			IDefinition leftDef;
			IExpressionNode leftNode = ((MemberAccessExpressionNode)obj).getLeftOperandNode();
			IExpressionNode rightNode = ((MemberAccessExpressionNode)obj).getRightOperandNode();
			leftDef = leftNode.resolveType(project);
			IDefinition rightDef = rightNode.resolve(project);
			if (leftDef != null && leftDef.getQualifiedName().equals("Date"))
			{
				if (rightDef instanceof AccessorDefinition)
					return true;
				else if (rightDef instanceof VariableDefinition)
					return true;
				else if (rightDef == null && rightNode.getNodeID() == ASTNodeID.IdentifierID)
				{
					if (writeAccess)
					{
			            DatePropertiesSetters propSetter = DatePropertiesSetters.valueOf(((IIdentifierNode)rightNode).getName().toUpperCase());
			            if (propSetter != null) return true;
					}
					else
					{
			            DatePropertiesGetters propGetter = DatePropertiesGetters.valueOf(((IIdentifierNode)rightNode).getName().toUpperCase());
			            if (propGetter != null) return true;
					}
				}
			}
		}
		return false;
	}

    /**
     * resolveType on an XML expression returns null
     * (see IdentiferNode.resolveType).
     * So, we have to walk the tree ourselves and resolve
     * individual pieces.
     * @param obj
     * @return
     */
    public boolean isXMLish(IExpressionNode obj)
    {
		return EmitterUtils.isXMLish(obj, getWalker().getProject());
    }

    public MemberAccessExpressionNode getLastMAEInChain(MemberAccessExpressionNode node)
    {
    	while (node.getRightOperandNode() instanceof MemberAccessExpressionNode)
    		node = (MemberAccessExpressionNode)node.getRightOperandNode();
    	return node;
    }

    @Override
    public void emitLabelStatement(LabeledStatementNode node)
    {
    	BlockNode innerBlock = node.getLabeledStatement();
    	if (innerBlock.getChildCount() == 1 && innerBlock.getChild(0).getNodeID() == ASTNodeID.ForEachLoopID)
    	{
    		getWalker().walk(node.getLabeledStatement());
    		return; // for each emitter will emit label in the right spot
    	}
    	super.emitLabelStatement(node);
    }
    
    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        ICompilerProject project = getWalker().getProject();
        if (project instanceof RoyaleJSProject)
        {
        	String vectorClassName = ((RoyaleJSProject)project).config == null ? null : ((RoyaleJSProject)project).config.getJsVectorEmulationClass();
        	if (vectorClassName != null)
        	{
        	    if (!vectorClassName.equals("Array"))
        		    write(vectorClassName);
        		return;
        	}
        }
        Boolean written = false;
        if (node instanceof TypedExpressionNode) {
            startMapping(node);
            write(ASEmitterTokens.PAREN_OPEN);
            write(JSRoyaleEmitterTokens.SYNTH_VECTOR);
            write(ASEmitterTokens.PAREN_OPEN);
            write(ASEmitterTokens.SINGLE_QUOTE);
            //the element type of the Vector:
            write(formatQualifiedName(node.getTypeNode().resolve(project).getQualifiedName()));
            write(ASEmitterTokens.SINGLE_QUOTE);
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.PAREN_CLOSE);
            endMapping(node);
            written = true;
        }

        if (!written) {
            write(JSRoyaleEmitterTokens.VECTOR);
        }
        if (getModel().inStaticInitializer)
        {
        	if (!staticUsedNames.contains(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken()))
        		staticUsedNames.add(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken());
        }
        if (project instanceof RoyaleJSProject)
        	((RoyaleJSProject)project).needLanguage = true;
        getModel().needLanguage = true;
    }

    @Override
    public void emitAssignmentCoercion(IExpressionNode assignedNode, IDefinition definition)
    {
        super.emitAssignmentCoercion(assignedNode, definition);
        if (getModel().inStaticInitializer)
        {
        	if (!staticUsedNames.contains(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken()))
        		staticUsedNames.add(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken());
        }

    }
    
	boolean isGoogProvided(String className)
	{
        ICompilerProject project = getWalker().getProject();
		return ((RoyaleJSProject)project).isGoogProvided(className);
	}
	
    public void processBindableSupport(IClassDefinition type,ArrayList<String> requiresList) {
        ICompilerProject royaleProject = getWalker().getProject();
        boolean needsBindableSupport = type.needsEventDispatcher(royaleProject);
        if (needsBindableSupport) {
            
            IClassDefinition bindableClassDef = type;
            ClassDefinition objectClassDefinition = (ClassDefinition)royaleProject.getBuiltinType(
                    IASLanguageConstants.BuiltinType.OBJECT);
            
            if (bindableClassDef.resolveBaseClass(royaleProject).equals(objectClassDefinition)) {
                //keep the decision in the model for later
                getModel().registerImplicitBindableImplementation( bindableClassDef,
                        ImplicitBindableImplementation.EXTENDS);
                // add the requiresList support for extending the dispatcher class
                if (!requiresList.contains(formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME))) {
                    requiresList.add(formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME));
                }
            } else {
                //keep the decision in the model for later
                getModel().registerImplicitBindableImplementation( bindableClassDef,
                        ImplicitBindableImplementation.IMPLEMENTS);
                //add the requiresList support for implementing IEventDispatcher
                if (!requiresList.contains(formatQualifiedName(BindableEmitter.DISPATCHER_INTERFACE_QNAME))) {
                    requiresList.add(formatQualifiedName(BindableEmitter.DISPATCHER_INTERFACE_QNAME));
                }
                if (!requiresList.contains(formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME))) {
                    requiresList.add(formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME));
                }
            }
        }
        
        if (!needsBindableSupport) {
            //we still need to check for static-only bindable requirements. If it was also instance-bindable,
            //then the static-only requirements have already been met above
            needsBindableSupport = ((IClassDefinition) type).needsStaticEventDispatcher(royaleProject);
            //static-only bindable *only* requires the Dispatcher class, not the interface
            if (needsBindableSupport
                    && !requiresList.contains(formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME))) {
                requiresList.add(formatQualifiedName(BindableEmitter.DISPATCHER_CLASS_QNAME));
            }
        }
    }

}
