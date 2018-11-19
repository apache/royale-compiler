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

package org.apache.royale.compiler.internal.tree.mxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.targets.ITargetAttributes;
import org.apache.royale.compiler.internal.targets.TargetAttributesMap;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.units.MXMLCompilationUnit;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLInstructionData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLTextData.TextType;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLConstructorHasParametersProblem;
import org.apache.royale.compiler.problems.MXMLContentAfterRootTagProblem;
import org.apache.royale.compiler.problems.MXMLContentBeforeRootTagProblem;
import org.apache.royale.compiler.problems.MXMLFinalClassProblem;
import org.apache.royale.compiler.problems.MXMLMissingRootTagProblem;
import org.apache.royale.compiler.problems.MXMLMultipleRootTagsProblem;
import org.apache.royale.compiler.problems.MXMLNotAClassProblem;
import org.apache.royale.compiler.problems.MXMLUnresolvedTagProblem;
import org.apache.royale.compiler.problems.MXMLXMLProcessingInstructionLocationProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleNode;

public class MXMLFileNode extends MXMLNodeBase implements IMXMLFileNode, IScopedNode, IFileNode, IFileNodeAccumulator
{
    // TODO Make this class package internal rather than public.

    public MXMLFileNode()
    {
        super(null);
        styleNodes = new ArrayList<IMXMLStyleNode>();
        importNodes = new LinkedList<IImportNode>();
        embedNodes = new LinkedList<IEmbedResolver>();
        requiredResourceBundles = new HashSet<String>();
    }

    private RoyaleProject project;

    private String qname;

    private MXMLFileScope fileScope;

    /**
     * The node corresponding to the root tag of the file. If the root tag is
     * missing, cannot be resolved, or resolves to something other than a
     * non-final class, the documentNode will be null. If it exists, the
     * documentNode is the one and only child of this file node. If the
     * documentNode does not exist, then this file node has no children.
     */
    private MXMLDocumentNode documentNode;

    private long includeTreeLastModified;

    private final List<IImportNode> importNodes;

    private final List<IEmbedResolver> embedNodes;

    private final Set<String> requiredResourceBundles;

    // Translates offset between local and absolute.
    private OffsetLookup offsetLookup;

    private final List<IMXMLStyleNode> styleNodes;

    private ITargetAttributes targetAttributes;

    private CSSCompilationSession cssCompilationSession;

    void initialize(MXMLTreeBuilder builder)
    {
        IFileSpecification fileSpec = builder.getFileSpecification();

        project = builder.getProject();
        qname = builder.getQName();
        fileScope = builder.getFileScope();

        // When parsing the MXML for AST construction, ad-hoc IncludeHandler are created in
        // order to please the MXML lexer and to keep the AST node offsets in-sync with
        // the scope tree node offsets. As a result, the MXML AST nodes does not have a 
        // usable OffsetLookup after tree building. That's why we need to provide the real
        // OffsetLookup from the scope building.
        final OffsetLookup offsetLookup = fileScope.getOffsetLookup();
        assert offsetLookup != null : "Expected offset lookup on MXMLFileScope.";
        this.offsetLookup = offsetLookup;

        setLocation(builder);

        // Add implicit import nodes for ActionScript.
        for (String importStr : ASFileScope.getImplicitImportsForAS())
        {
            ImportNode implicitImportNode = new MXMLImplicitImportNode(project, importStr);
            addImportNode(implicitImportNode);
        }

        // Add implicit import nodes for MXML.
        for (IImportNode implicitImportNode : project.getImplicitImportNodesForMXML(builder.getMXMLDialect()))
        {
            addImportNode(implicitImportNode);
        }

        includeTreeLastModified = fileSpec.getLastModified();

        processUnits(builder);
    }

    private void processUnits(MXMLTreeBuilder builder)
    {
        IMXMLData mxmlData = builder.getMXMLData();
        
        // add problems that were encountered while parsing the data
        for (ICompilerProblem problem : mxmlData.getProblems())
        {
            builder.addProblem(problem);
        }

        if (mxmlData.getNumUnits() == 0)
            return;

        boolean foundRootTag = false;
        IMXMLTextData asDoc = null;

        // Walk the top-level units of the MXMLData.
        for (IMXMLUnitData unit = mxmlData.getUnit(0); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit instanceof IMXMLInstructionData)
            {
                if (unit.getStart() > 0)
                {
                    ICompilerProblem problem = new MXMLXMLProcessingInstructionLocationProblem(unit);
                    builder.addProblem(problem);
                }
            }
            else if (unit instanceof IMXMLTagData)
            {
                if (!foundRootTag)
                {
                    foundRootTag = true;
                    processRootTag(builder, (IMXMLTagData)unit, asDoc);
                }
                else
                {
                    ICompilerProblem problem = new MXMLMultipleRootTagsProblem(unit);
                    builder.addProblem(problem);
                }
            }
            else if (unit instanceof IMXMLTextData)
            {
                IMXMLTextData textData = (IMXMLTextData)unit;
                if (textData.getTextType().equals(TextType.ASDOC))
                    asDoc = textData;
                if (!builder.getMXMLDialect().isWhitespace(textData.getCompilableText()))
                {
                    if (documentNode == null)
                    {
                        ICompilerProblem problem = new MXMLContentBeforeRootTagProblem(unit);
                        builder.addProblem(problem);
                    }
                    else
                    {
                        ICompilerProblem problem = new MXMLContentAfterRootTagProblem(unit);
                        builder.addProblem(problem);
                    }
                }
            }
        }

        if (!foundRootTag)
        {
            ICompilerProblem problem = new MXMLMissingRootTagProblem(builder.getPath());
            builder.addProblem(problem);
        }
    }

    private void processRootTag(MXMLTreeBuilder builder, IMXMLTagData rootTag, IMXMLTextData asDoc)
    {
        ClassDefinition fileDef = fileScope.getMainClassDefinition();
        assert fileDef != null;

        IDefinition tagDef = builder.getFileScope().resolveTagToDefinition(rootTag);

        // Report a problem if the root tag doesn't map to a definition.
        if (tagDef == null)
        {
            ICompilerProblem problem = new MXMLUnresolvedTagProblem(rootTag);
            builder.addProblem(problem);
            return;
        }

        // Report a problem if that definition isn't for a class.
        if (!(tagDef instanceof IClassDefinition))
        {
            ICompilerProblem problem = new MXMLNotAClassProblem(rootTag, tagDef.getQualifiedName());
            builder.addProblem(problem);
            return;
        }

        IClassDefinition tagDefinition = (IClassDefinition)tagDef;

        // Report a problem if that class is final.
        if (tagDefinition != null && tagDefinition.isFinal())
        {
            ICompilerProblem problem = new MXMLFinalClassProblem(rootTag, tagDef.getQualifiedName());
            builder.addProblem(problem);
            return;
        }

        // Report a problem is that class's constructor has required parameters.
        IFunctionDefinition constructor = tagDefinition.getConstructor();
        if (constructor != null && constructor.hasRequiredParameters())
        {
            ICompilerProblem problem = new MXMLConstructorHasParametersProblem(rootTag, tagDef.getQualifiedName());
            builder.addProblem(problem);
            return;
        }

        documentNode = new MXMLDocumentNode(this);
        documentNode.setClassReference(project, tagDefinition);
        documentNode.setClassDefinition(fileDef);
        documentNode.initializeFromTag(builder, rootTag);
        
        if (asDoc != null)
        {
            IASDocComment asDocComment = builder.getWorkspace().getASDocDelegate().createASDocComment(asDoc, tagDefinition);
            documentNode.setASDocComment(asDocComment);
        }

        fileDef.setNode(documentNode);
        // TODO setNode() sets the nameStart and nameEnd to -1
        // Fix setNode() to set the values properly
        // From MXMLScopeBuilder - CM clients expect the name start of the root class definition to be at 0.
        if (fileDef.getNameStart() == -1 && fileDef.getNameEnd() == -1)
            fileDef.setNameLocation(0, 0);
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? documentNode : null;
    }

    @Override
    public int getChildCount()
    {
        return documentNode != null ? 1 : 0;
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLFileID;
    }

    @Override
    public String getName()
    {
        return qname;
    }

    @Override
    public IMXMLDocumentNode getDocumentNode()
    {
        return documentNode;
    }

    @Override
    public IASScope getScope()
    {
        return fileScope;
    }

    @Override
    public void getAllImports(Collection<String> imports)
    {
        for (IImportNode node : importNodes)
        {
            imports.add(node.getImportName());
        }
    }

    @Override
    public void getAllImportNodes(Collection<IImportNode> imports)
    {
        imports.addAll(importNodes);
    }

    @Override
    public IDefinition[] getTopLevelDefinitions(boolean includeDefinitionsOutsideOfPackage,
                                                boolean includeNonPublicDefinitions)
    {
        return documentNode != null ?
                new IDefinition[] {documentNode.getClassDefinition()} :
                new IDefinition[] {};
    }

    @Override
    public IDefinitionNode[] getTopLevelDefinitionNodes(boolean includeDefinitionsOutsideOfPackage,
                                                        boolean includeNonPublicDefinitions)
    {
        return documentNode != null ?
                new IDefinitionNode[] {documentNode} :
                new IDefinitionNode[] {};
    }

    @Override
    public boolean hasIncludes()
    {
        return fileScope.getOffsetLookup().hasIncludes();
    }

    public IClassDefinition[] getLibraryDefinitions()
    {
        return ((MXMLFileScope)getScope()).getLibraryDefinitions();
    }

    @Override
    public IFileSpecification getFileSpecification()
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("MXMLFileNode waiting for lock in getFileSpecification");
    	IFileSpecification fs = fileScope.getWorkspace().getFileSpecification(fileScope.getContainingPath());
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
    		System.out.println("MXMLFileNode done with lock in getFileSpecification");
    	return fs;
    }

    @Override
    public ICompilerProject getCompilerProject()
    {
        return project;
    }

    /**
     * For debugging only. Builds a string such as <code>"C:\test.mxml"</code>
     * from the path of the file node.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getSourcePath());
        sb.append('"');

        return true;
    }

    /**
     * @param lastModified
     */
    protected void updateIncludeTreeLastModified(long lastModified)
    {
        this.includeTreeLastModified = Math.max(lastModified, this.includeTreeLastModified);

    }

    /**
     * @return A time stamp of the last modification time this filenode's
     * include tree
     */
    @Override
    public long getIncludeTreeLastModified()
    {
        return this.includeTreeLastModified;
    }

    // TODO: this method should should be merged with FileNode.addImportNode()
    //       if the two MXMLFileNodes are merged
    @Override
    public void addImportNode(IImportNode node)
    {
        importNodes.add(node);
    }

    @Override
    public List<IImportNode> getImportNodes()
    {
        return importNodes;
    }

    // TODO: this method should should be merged with FileNode.addEmbedNode()
    //       if the two MXMLFileNodes are merged
    @Override
    public void addEmbedNode(IEmbedResolver node)
    {
        embedNodes.add(node);
    }

    // TODO: this method should should be merged with FileNode.getEmbedNodes()
    //       if the two MXMLFileNodes are merged
    @Override
    public List<IEmbedResolver> getEmbedNodes()
    {
        return embedNodes;
    }

    // TODO: this method should should be merged with FileNode.addRequiredResourceBundle()
    //       if the two MXMLFileNodes are merged
    @Override
    public void addRequiredResourceBundle(String bundleName)
    {
        requiredResourceBundles.add(bundleName);
    }

    // TODO: this method should should be merged with FileNode.getRequiredResourceBundles()
    //       if the two MXMLFileNodes are merged
    @Override
    public Set<String> getRequiredResourceBundles()
    {
        return requiredResourceBundles;
    }

    @Override
    public OffsetLookup getOffsetLookup()
    {
        return this.offsetLookup;
    }

    private void setLocation(MXMLTreeBuilder builder)
    {
        String sourcePath = builder.getPath();

        MXMLCompilationUnit cu = builder.getCompilationUnit();
        IMXMLData mxmlData = builder.getMXMLData();

        // Find the extents of the file node by finding the last bit
        // of the MXMLData. Note that this assumes that MXMLData
        // represents trailing white space (which it currently does).
        // Note also that we can't just look at the file system,
        // because the MXMLFileNode might actually backed by an
        // in-memory document, not the original file.
        final String absoluteFilename = cu.getAbsoluteFilename();
        final int mxmlDataEnd = mxmlData.getEnd();
        // Convert from a local offset to an absolute offset
        final int[] possibleAbsoluteOffsets =
                offsetLookup.getAbsoluteOffset(absoluteFilename, mxmlDataEnd);
        assert possibleAbsoluteOffsets.length == 1 : "Expected only 1 match.";
        final int absoluteEnd = possibleAbsoluteOffsets[0];
        setLocation(sourcePath, 0, absoluteEnd, 1, 1);
    }

    /**
     * Assuming {@code MXMLStyleNode} is always a direct child of
     * {@link MXMLDocumentNode}.
     */
    @Override
    public List<IMXMLStyleNode> getStyleNodes()
    {
        return styleNodes;
    }

    @Override
    public ITargetAttributes getTargetAttributes(final ICompilerProject project)
    {
        if (targetAttributes == null && documentNode != null)
            targetAttributes = new TargetAttributesMap(documentNode.rootAttributes);
        return targetAttributes;
    }

    /**
     * Get the CSS semantic information.
     * 
     * @return CSS semantic information.
     */
    @Override
    public CSSCompilationSession getCSSCompilationSession()
    {
        if (cssCompilationSession == null)
        {
        	synchronized (this)
        	{
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_NODE) == CompilerDiagnosticsConstants.FILE_NODE)
            		System.out.println("MXMLFileNode waiting for lock in getCSSCompilationSession");
                if (cssCompilationSession == null)
                	cssCompilationSession = project.getCSSCompilationSession();        		
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_NODE) == CompilerDiagnosticsConstants.FILE_NODE)
            		System.out.println("MXMLFileNode done with lock in getCSSCompilationSession");
        	}
        }
        return cssCompilationSession;
    }

    @Override
    public void addDeferredFunctionNode(FunctionNode functionNode)
    {
        throw new UnsupportedOperationException("Functions should never be deferred in MXML document.");
    }

    @Override
    public void populateFunctionNodes()
    {
        // Do nothing. Function bodies are never deferred for non-AS documents.
    }
    
    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return Collections.emptyList();
    }
}
