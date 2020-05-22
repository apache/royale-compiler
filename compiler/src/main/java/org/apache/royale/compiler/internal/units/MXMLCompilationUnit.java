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

package org.apache.royale.compiler.internal.units;


import static com.google.common.collect.Collections2.transform;

import java.util.Collection;
import java.util.HashSet;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.as.codegen.CodeGeneratorManager;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.parsing.as.OffsetCue;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLScopeBuilder;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.tree.mxml.MXMLDocumentNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLFileNode;
import org.apache.royale.compiler.internal.tree.mxml.MXMLTreeBuilder;
import org.apache.royale.compiler.internal.units.requests.ASFileScopeRequestResult;
import org.apache.royale.compiler.internal.units.requests.SWFTagsRequestResult;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLDataManager;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class MXMLCompilationUnit extends CompilationUnitBase
{
    public MXMLCompilationUnit(CompilerProject project, String path,
                               DefinitionPriority.BasePriority basePriority,
                               int order,
                               String qname)
    {
        super(project, path, basePriority, qname);
        this.qname = qname;
        ((DefinitionPriority)getDefinitionPriority()).setOrder(order);
    }

    // The fully-qualified name of the one externally-visible definition
    // expected to be found in this compilation unit, or null if none is expected.
    // This qname is determined from the name of the file
    // and the file's location relative to the source path.
    private final String qname;

    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.MXML_UNIT;
    }

    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        // Fulfill other requests before profiling this request.       
        final IFileScopeRequestResult fileScopeRequestResult = getFileScopeRequest().get();
        final MXMLFileScope fileScope = (MXMLFileScope)fileScopeRequestResult.getScopes()[0];
        
        startProfile(Operation.GET_SYNTAX_TREE);
        
        final IMXMLData mxmlData = getMXMLData();
        
        final Collection<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        
        // Create an MXMLTreeBuilder to store all the contextual information
        // that we need to build an MXML tree.
        final MXMLTreeBuilder builder =
            new MXMLTreeBuilder(this, getFileSpecificationGetter(),  qname, mxmlData, fileScope, problems);

        // Use the MXMLTreeBuilder to build an MXMLFileNode (the root of an MXML AST)
        // from the MXMLData (the MXML DOM) and the MXMLFileScope.
        final IMXMLFileNode fileNode = builder.build();

        try
        {
            // TODO This belongs in MXMLDocumentNode.
            MXMLDocumentNode documentNode = (MXMLDocumentNode)fileNode.getDocumentNode();
            if (documentNode != null)
            {
                ClassDefinition mainClassDefinition = fileScope.getMainClassDefinition();
                if (mainClassDefinition != null)
                {
                    TypeScope mainClassScope = (TypeScope)mainClassDefinition.getContainedScope();
                    documentNode.setScope(mainClassScope);
                    if (documentNode.getHasDataBindings()) {
                        mainClassDefinition.setRoyaleBindings();
                    }
                }
            }
            
            // Start CSS semantic analysis. 
            final Function<IMXMLStyleNode, ICSSDocument> parseMXMLStyleNode = new Function<IMXMLStyleNode, ICSSDocument>()
            {
                @Override
                public ICSSDocument apply(IMXMLStyleNode mxmlStyleNode)
                {
                    // This method will trigger the CSS parser to parse the CSS fragment.
                    return mxmlStyleNode.getCSSDocument(problems);
                }
            };
            final Collection<ICSSDocument> cssDocumentList =
                    transform(fileNode.getStyleNodes(), parseMXMLStyleNode);

            // This method will resolve dependencies introduced by the CSS fragment, and add the
            // dependee's to the dependency graph. This is done at the last step in MXML tree 
            // building phase. 
            // - It can't be done in MXML semantic analysis, because MXML code generation doesn't
            //   depend on MXML semantic analysis; 
            // - It can't be done in MXML code generation either, because the "problems" in that phase are 
            //   generated from inside ABCGenerator.
            updateStyleCompilationUnitDependencies(
                    fileNode.getCSSCompilationSession(), 
                    fileScope,
                    cssDocumentList, 
                    problems);
            
            fileNode.getCSSCompilationSession().cssDocuments.addAll(cssDocumentList);
        }
        catch (Exception e)
        {
            //something went wrong, so log it.  
            problems.add(new UnexpectedExceptionProblem(e));
        }
        finally
        {
            stopProfile(Operation.GET_SYNTAX_TREE);
        }

        getProject().addToASTCache(fileNode);
        return new SyntaxTreeRequestResult(fileNode, ImmutableSet.<String>copyOf(fileScope.getSourceDependencies()), fileNode.getIncludeTreeLastModified(), problems);
    }

    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_FILESCOPE);
        try
        {
            final IMXMLData mxmlData = getMXMLData();
                    
            final MXMLScopeBuilder scopeBuilder = new MXMLScopeBuilder(this, getFileSpecificationGetter(), mxmlData, qname, getAbsoluteFilename());
            MXMLFileScope fileScope = scopeBuilder.build();
            final ImmutableList<OffsetCue> offsetCueList = scopeBuilder.getIncludeHandler().getOffsetCueList();
            final OffsetLookup offsetLookup = new OffsetLookup(offsetCueList);
            fileScope.setOffsetLookup(offsetLookup);
            final Collection<ICompilerProblem> problemCollection = scopeBuilder.getProblems();
            final IFileSpecification rootFileSpec = getRootFileSpecification();

            getProject().getWorkspace().addIncludedFilesToCompilationUnit(this, fileScope.getSourceDependencies());

            return new ASFileScopeRequestResult(getDefinitionPromises(), getDefinitionPriority(), problemCollection, fileScope, rootFileSpec);
        }
        finally
        {
            stopProfile(Operation.GET_FILESCOPE);
        }
    }
    
    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        // Fulfill other requests before profiling this request.
        final ISyntaxTreeRequestResult syntaxTreeRequestResult = getSyntaxTreeRequest().get();
        final MXMLFileNode fileNode = (MXMLFileNode)syntaxTreeRequestResult.getAST();
        final CompilerProject project = getProject();
        
        startProfile(Operation.GET_ABC_BYTES);
        try
        {
            IABCBytesRequestResult result = CodeGeneratorManager.getCodeGenerator().generate(
                project.getWorkspace().getExecutorService(),
                project.getUseParallelCodeGeneration(),
                getFilenameNoPath(),
                fileNode,
                getProject(),
                isInvisible(),
                getEncodedDebugFiles());
            return result;
        }
        finally
        {
            stopProfile(Operation.GET_ABC_BYTES);
        }
    }

    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        // Fulfill other requests before profiling this request.
        final IABCBytesRequestResult abc = getABCBytesRequest().get();

        startProfile(Operation.GET_SWF_TAGS);

        try
        {
            return new SWFTagsRequestResult(abc.getABCBytes(), qname, abc.getEmbeds());
        }
        finally
        {
            stopProfile(Operation.GET_SWF_TAGS);
        }
    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        // Fulfill other requests before profiling this request.
        final ISyntaxTreeRequestResult syntaxTreeRequestResult = getSyntaxTreeRequest().get();
        final MXMLFileNode fileNode = (MXMLFileNode)syntaxTreeRequestResult.getAST();

        startParsingImports(fileNode);

        startProfile(Operation.GET_SEMANTIC_PROBLEMS);
        try
        {
            /* do the codegen now, because we don't discover SDK databinding dependencies
             * until codegen. Long term we probably want to go in this direction anyway,
             * since semantic analysis and codegen may get folded together for other reasons.
             */
            getABCBytesRequest().get(); 
            Collection<ICompilerProblem> problems = new HashSet<ICompilerProblem>();

            updateEmbedCompilationUnitDependencies(fileNode.getEmbedNodes(), problems);
            
            getABCBytesRequest().get();

            // Resolve all references to definitions.
            //fileNode.resolveRefs(problems, getProject());

            return new IOutgoingDependenciesRequestResult()
            {
                @Override
                public ICompilerProblem[] getProblems()
                {
                    return IOutgoingDependenciesRequestResult.NO_PROBLEMS;
                }
            };
        }
        finally
        {
            stopProfile(Operation.GET_SEMANTIC_PROBLEMS);
        }
    }
    
    private IMXMLData getMXMLData()
    {
        // Get the DOM-like MXMLData for the file.
        // If its not already in the Workspace's MXMLDataManager,
        // the MXML file will be parsed.
        final IMXMLDataManager mxmlDataManager = getProject().getWorkspace().getMXMLDataManager();
        final IFileSpecification rootFileSpec = getRootFileSpecification();
        final IMXMLData mxmlData = mxmlDataManager.get(rootFileSpec);
        return mxmlData;
    }
    
    @Override
    public RoyaleProject getProject()
    {
        return (RoyaleProject)super.getProject();
    }
}
