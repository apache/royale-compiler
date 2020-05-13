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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.abc.ABCScopeBuilder;
import org.apache.royale.compiler.internal.as.codegen.CodeGeneratorManager;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.embedding.IEmbedData;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.embedding.transcoders.SkinTranscoder;
import org.apache.royale.compiler.internal.embedding.transcoders.TranscoderBase;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASFileScopeProvider;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.internal.units.requests.ABCFileScopeRequestResult;
import org.apache.royale.compiler.internal.units.requests.EmbedFileScopeRequestResult;
import org.apache.royale.compiler.internal.units.requests.SWFTagsRequestResult;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InvalidABCByteCodeProblem;
import org.apache.royale.compiler.problems.NoScopesInABCCompilationUnitProblem;
import org.apache.royale.compiler.problems.UnknownSuperclassProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swc.ISWCFileEntry;

/**
 * This is a Compilation Unit which handles transcoding and embedding of embed
 * meta data.
 */
public class EmbedCompilationUnit extends CompilationUnitBase
{
    private static String getSourcePath(EmbedData data)
    {
        ISWCFileEntry swcFile = data.getSWCSource();
        if (swcFile != null)
            return swcFile.getContainingSWCPath();
        else
            return (String)data.getAttribute(EmbedAttribute.SOURCE);
    }

    public EmbedCompilationUnit(CompilerProject project, EmbedData data)
    {
        super(project, getSourcePath(data), DefinitionPriority.BasePriority.SOURCE_LIST, true);
        this.embedData = data;
    }

    private final EmbedData embedData;

    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.EMBED_UNIT;
    }

    @Override
    public String getName()
    {
        return embedData.getQName();
    }

    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_SYNTAX_TREE);
        try
        {
            getProject().clearScopeCacheForCompilationUnit(this);
            TranscoderBase transcoder = (TranscoderBase)embedData.getTranscoder();
            if (transcoder instanceof SkinTranscoder)
            {
                List<ICompilerProblem> noProblems = Collections.emptyList();
                return new SyntaxTreeRequestResult(getRootFileSpecification().getLastModified(), noProblems);
            }
            else
            {
                Collection<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
                FileNode fileNode = transcoder.buildAST(problems, getAbsoluteFilename());
                final ASFileScope fileScope = fileNode.getFileScope();
                addScopeToProjectScope(new ASFileScope[] { fileScope });
                markClassAsEmbed(fileScope);

                return new SyntaxTreeRequestResult(fileNode, fileNode.getIncludeHandler().getIncludedFiles(), getRootFileSpecification().getLastModified(), problems);
            }
        }
        finally
        {
            stopProfile(Operation.GET_SYNTAX_TREE);
        }
    }
    
    @Override
    protected void verifyAST(IASNode ast)
    {
        // Don't try to verify ASTs produced by an EmbedCompilationUnit
        // because none of the nodes have source location info.
    }

    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        TranscoderBase transcoder = (TranscoderBase)embedData.getTranscoder();
        // SkinTranscoder generates ABC directly rather than an AST, so handle differently
        // Eventually all transcoders should be updated to go directly to ABC
        if (transcoder instanceof SkinTranscoder)
        {
            List<IASScope> scopeList = null;
            List<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
            IFileSpecification rootSource = getRootFileSpecification();
            try
            {
                IABCBytesRequestResult abcResult = getABCBytesRequest().get();

                startProfile(Operation.GET_FILESCOPE);

                String path = rootSource.getPath();
                ABCScopeBuilder abcScopeBuilder = new ABCScopeBuilder(
                        getProject().getWorkspace(),
                        abcResult.getABCBytes(),
                        path,
                        ASFileScopeProvider.getInstance());
                scopeList = abcScopeBuilder.build();
                if (scopeList.isEmpty())
                {
                    problems.add(new NoScopesInABCCompilationUnitProblem(path));
                }
                for (IASScope scope : scopeList)
                    markClassAsEmbed((ASFileScope)scope);

                return new ABCFileScopeRequestResult(problems, scopeList);
            }
            catch (Exception e)
            {
                ICompilerProblem problem = new InvalidABCByteCodeProblem(rootSource.getPath());
                problems.add(problem);
                return new ABCFileScopeRequestResult(problems, Collections.<IASScope> emptyList());
            }
            finally
            {
                stopProfile(Operation.GET_FILESCOPE);
            }
        }
        else
        {
            ISyntaxTreeRequestResult syntaxTreeResult = getSyntaxTreeRequest().get();
            FileNode rootNode = (FileNode)syntaxTreeResult.getAST();
            if (rootNode == null)
            {
                return new EmbedFileScopeRequestResult(null);
            }

            startProfile(Operation.GET_FILESCOPE);
            try
            {
                final IASScope fileScope = rootNode.getScope();
                assert fileScope instanceof ASFileScope : "Expect ASFileScope as the top-level scope, but found " + fileScope.getClass();
                return new EmbedFileScopeRequestResult((ASFileScope)fileScope);
            }
            finally
            {
                stopProfile(Operation.GET_FILESCOPE);
            }
        }
    }

    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        TranscoderBase transcoder = (TranscoderBase)embedData.getTranscoder();
        // SkinTranscoder generates ABC directly rather than an AST, so handle differently
        // Eventually all transcoders should be updated to go directly to ABC
        if (transcoder instanceof SkinTranscoder)
        {
            startProfile(Operation.GET_ABC_BYTES);
            try
            {
                List<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
                byte[] bytes = transcoder.buildABC(getProject(), problems);
                ICompilerProblem[] problemsArray = problems.toArray(new ICompilerProblem[problems.size()]);
                Set<IEmbedData> iembeds = new HashSet<IEmbedData>();
                iembeds.add(embedData);
                return new ABCBytesRequestResult(bytes, problemsArray, iembeds);
            }
            finally
            {
                stopProfile(Operation.GET_ABC_BYTES);
            }
        }
        else
        {

            ISyntaxTreeRequestResult syntaxTreeResult = getSyntaxTreeRequest().get();
            IASNode rootNode = syntaxTreeResult.getAST();
            if (rootNode == null)
            {
                return new ABCBytesRequestResult(syntaxTreeResult.getProblems());
            }

            startProfile(Operation.GET_ABC_BYTES);
            try
            {
                return CodeGeneratorManager.getCodeGenerator().generate(getFilenameNoPath(), rootNode, getProject());
            }
            finally
            {
                stopProfile(Operation.GET_ABC_BYTES);
            }
        }
    }

    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        final IABCBytesRequestResult abc = getABCBytesRequest().get();

        startProfile(Operation.GET_SWF_TAGS);

        try
        {
            String tagName;
            String qname = getName();
            if (qname == null || "".equals(qname))
                tagName = DEFAULT_DO_ABC_TAG_NAME;
            else
                tagName = qname.replace('.', '/');

            Collection<IEmbedData> embeds = abc.getEmbeds();
            if (embeds.size() == 0)
                embeds = Collections.singleton((IEmbedData)embedData);
            return new SWFTagsRequestResult(abc.getABCBytes(), tagName, embeds);
        }
        finally
        {
            stopProfile(Operation.GET_SWF_TAGS);
        }
    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        startProfile(Operation.GET_SEMANTIC_PROBLEMS);

        try
        {
            Collection<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
            analyze(problems);

            IOutgoingDependenciesRequestResult result = new IOutgoingDependenciesRequestResult()
            {
                @Override
                public ICompilerProblem[] getProblems()
                {
                    return IOutgoingDependenciesRequestResult.NO_PROBLEMS;
                }
            };

            return result;
        }
        finally
        {
            stopProfile(Operation.GET_SEMANTIC_PROBLEMS);
        }
    }

    public EmbedData getEmbedData()
    {
        return embedData;
    }

    private void analyze(Collection<ICompilerProblem> problems)
    {
        // if this class extends another, add a dependency on this compilation unit to the class that it extends
        if (embedData.generatedClassExtendsAnother())
        {
            ASProjectScope projectScope = getProject().getScope();
            IDefinition[] baseDefs = projectScope.findAllDefinitionsByName(Multiname.crackDottedQName(getProject(), embedData.getTranscoder().getBaseClassQName()));
            if(baseDefs == null || baseDefs.length == 0)
            {
                IDefinition[] subDefs = projectScope.findAllDefinitionsByName(Multiname.crackDottedQName(getProject(), embedData.getQName()));
                ICompilerProblem problem = new UnknownSuperclassProblem(
                    subDefs[0], embedData.getTranscoder().getBaseClassQName());
                problems.add(problem);
            }
            else
            {
                ICompilationUnit referencedCU = projectScope.getCompilationUnitForScope(baseDefs[0].getContainingScope());
                getProject().addDependency(this, referencedCU, DependencyType.INHERITANCE, baseDefs[0].getQualifiedName());
            }
        }
    }
    
    /**
     * Marks all the externally visible classes in the scope
     * as being embed classes.
     */
    private void markClassAsEmbed(ASFileScope fileScope)
    {
        // Mark all the clases as being embed classes.
        Collection<IDefinition> definitions = new LinkedList<IDefinition>();
        fileScope.collectExternallyVisibleDefinitions(definitions, false);
        for (IDefinition definition : definitions)
        {
            if (definition instanceof ClassDefinition)
            {
                ClassDefinition classDefinition = (ClassDefinition)definition;
                classDefinition.setGeneratedEmbedClass();
                classDefinition.setExcludedClass();
            }
        }
    }
}
