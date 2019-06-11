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

import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.internal.as.codegen.CodeGeneratorManager;
import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.Multiname;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.fxg.flex.FXGSymbolClass;
import org.apache.royale.compiler.fxg.flex.FlexFXG2SWFTranscoder;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.PackageDefinition;
import org.apache.royale.compiler.internal.fxg.resources.FXGFileResolver;
import org.apache.royale.compiler.internal.fxg.sax.FXGSAXParser;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority.BasePriority;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.FXGFileScope;
import org.apache.royale.compiler.internal.scopes.PackageScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.targets.TagSorter;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.internal.units.requests.ASFileScopeRequestResult;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.collect.Iterables;

/**
 * This is a Compilation Unit which handles FXG compilation
 */
public class FXGCompilationUnit extends CompilationUnitBase
{
    public static String fxgBaseClassName =  "spark.core.SpriteVisualElement";

    private String qname;
    
    private static final String SUB_SYSTEM = "FXGCompilationUnit";   // used for error reporting
    
    /**
     * A {@code IFileSpecification} implementation from {@code String}.
     */
    private class GeneratedSourceFileSpecfication implements IFileSpecification
    {
        public GeneratedSourceFileSpecfication(String name, String content)
        {
            this.reader = new StringReader(content);
            String alias = null;
            ICompilerProject project = FXGCompilationUnit.this.getProject();
            if (project instanceof RoyaleProject)
            {
            	alias = ((RoyaleProject)project).getSwfDebugfileAlias();
            }
            if (alias != null)
            	this.name = alias + "/" + name;
            else
            	this.name = FilenameNormalization.normalize(name);
        }

        private final StringReader reader;
        private final String name;

        @Override
        public String getPath()
        {
            return name;
        }

        @Override
        public Reader createReader() throws FileNotFoundException
        {
            return reader;
        }

        @Override
        public long getLastModified()
        {
            return 0;
        }

        @Override
        public boolean isOpenDocument()
        {
            return false;
        }

		@Override
		public void setLastModified(long fileDate) {
			// TODO Auto-generated method stub
			
		}
    }

    public FXGCompilationUnit(CompilerProject project, String path, BasePriority basePriority, String qname)
    {
        super(project, path, basePriority, qname);
        this.qname = qname;
    }

    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.FXG_UNIT;
    }

    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_SYNTAX_TREE);
        
        getProject().clearScopeCacheForCompilationUnit(this);
        
        final Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        IFXGNode fxgroot = null; 
        try
        {
            fxgroot = new FXGSAXParser().parse(getRootFileSpecification().createReader(), qname, problems);               
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            problems.add(new InternalCompilerProblem2(getAbsoluteFilename(), ex, SUB_SYSTEM));
        }
        finally
        {
            stopProfile(Operation.GET_SYNTAX_TREE);
        }
        
        return new FXGSyntaxTreeRequestResult(fxgroot, getRootFileSpecification().getLastModified(), problems);
    }
    
    /**
     * Syntax Tree request result for FXG files. FXG files are not represented 
     * by a traditional AST, they use their own tree and this class allows 
     * {@link FXGCompilationUnit} to cache an FXG tree result.
     */
    private static class FXGSyntaxTreeRequestResult extends SyntaxTreeRequestResult {

        private IFXGNode rootNode;
        
        public FXGSyntaxTreeRequestResult(IFXGNode rootNode, long lastModified, Collection<ICompilerProblem> problems)
        {
            super(lastModified, problems);
            this.rootNode = rootNode;
        }
        
        /**
         * Returns the root node for the FXG file handled 
         * by this compilation unit
         * 
         * @return root node
         */
        public IFXGNode getRootNode()
        {
            return rootNode;
        }
       
    }
    
    /**
     * This function will create a temporary filscope. Please note that this will not be the filescope that is used in the actual AST
     */
    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_FILESCOPE);
        try
        {
            List<ICompilerProblem> noProblems = Collections.emptyList();
            IFileSpecification rootFileSpec = getRootFileSpecification();
            FXGFileScope fileScope = createFileScope();

            return new ASFileScopeRequestResult(getDefinitionPromises(), getDefinitionPriority(), noProblems, fileScope, rootFileSpec);
        }
        finally
        {
            stopProfile(Operation.GET_FILESCOPE);
        }
    }
    
    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        startProfile(Operation.GET_SEMANTIC_PROBLEMS);
        
        final Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        Map<ITag, ITag> extraTags = new HashMap<ITag, ITag>();
        FXGSymbolClass symbolClass = null;
        FileNode fileNode = null;
        ASProjectScope projectScope = getProject().getScope();
        
        try
        {
            FXGSyntaxTreeRequestResult syntaxTreeResult = (FXGSyntaxTreeRequestResult)getSyntaxTreeRequest().get();
            IFXGNode tree = syntaxTreeResult.getRootNode();

            FlexFXG2SWFTranscoder transcoder = new FlexFXG2SWFTranscoder(getProject());           
            transcoder.setResourceResolver(new FXGFileResolver(FilenameUtils.getFullPath(getRootFileSpecification().getPath())));          

            symbolClass = transcoder.transcode(tree, 
                    Multiname.getPackageNameForQName(qname), Multiname.getBaseNameForQName(qname), extraTags, problems);

            
            //Add dependencies to the classes required by the FXG processed by this compilation unit
            for (ITypeDefinition definition : transcoder.getDependencies())
            {
                getProject().addDependency(this, projectScope.getCompilationUnitForDefinition(definition),
                        DependencyType.EXPRESSION, definition.getQualifiedName());
            }

            StringBuilder sb = new StringBuilder(symbolClass.getGeneratedSource());
            if (symbolClass.getAdditionalSymbolClasses() != null)
            {
                for (FXGSymbolClass symbol : symbolClass.getAdditionalSymbolClasses())
                {
                    sb.append(symbol.getGeneratedSource());
                }
            }

            IFileSpecification virtualSymbolSource = new GeneratedSourceFileSpecfication(qname, sb.toString());
            fileNode = ASParser.parseFile(virtualSymbolSource, getProject().getWorkspace());
            fileNode.runPostProcess(EnumSet.of(PostProcessStep.POPULATE_SCOPE));

            projectScope.addScopeForCompilationUnit(this, fileNode.getFileScope());
            
            updateEmbedCompilationUnitDependencies(fileNode.getEmbedNodes(), problems);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            problems.add(new InternalCompilerProblem2(getAbsoluteFilename(), e, SUB_SYSTEM));
        }
        finally
        {
            stopProfile(Operation.GET_SEMANTIC_PROBLEMS);
        }
        
        return new FXGOutgoingDependenciesResult(fileNode, symbolClass, extraTags, problems);
    }
    
    /**
     * Semantic problems result for FXG files.
     */
    private static class FXGOutgoingDependenciesResult implements IOutgoingDependenciesRequestResult
    {

        private Collection<ICompilerProblem> problems;
        protected FileNode fileNode;
        protected FXGSymbolClass symbolClass;
        protected Map<ITag, ITag> extraTags;
        
        public FXGOutgoingDependenciesResult (FileNode fileNode, FXGSymbolClass symbolClass,
                                              Map<ITag, ITag> extraTags, Collection<ICompilerProblem> problems)
        {
            this.problems = problems;
            this.fileNode = fileNode;
            this.symbolClass = symbolClass;
            this.extraTags = extraTags;
        }
        
        @Override
        public ICompilerProblem[] getProblems()
        {
            return problems.toArray(new ICompilerProblem[0]);
        }
       
    }
    
    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        FXGOutgoingDependenciesResult semanticResults = (FXGOutgoingDependenciesResult) getOutgoingDependenciesRequest().get();
        
        startProfile(Operation.GET_ABC_BYTES);
        try
        {
            byte abc[] = CodeGeneratorManager.getCodeGenerator().generate(getFilenameNoPath(), semanticResults.fileNode, getProject()).getABCBytes();

            return new ABCBytesRequestResult(abc);
        }
        finally
        {
            stopProfile(Operation.GET_ABC_BYTES);
        }
    }
    
    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        FXGOutgoingDependenciesResult semanticResults = (FXGOutgoingDependenciesResult) getOutgoingDependenciesRequest().get();
        IABCBytesRequestResult byteResult = getABCBytesRequest().get();
        
        startProfile(Operation.GET_SWF_TAGS);
        
        final FXGSymbolClass symbolClass = semanticResults.symbolClass;
        final Map<ITag, ITag> extraTags = semanticResults.extraTags;
        final Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();       
        final DoABCTag abcTag = new DoABCTag();
        
        try
        {   
            abcTag.setName(qname);
            abcTag.setABCData(byteResult.getABCBytes());
        }
        catch (Exception e)
        {
            ICompilerProblem problem = new InternalCompilerProblem2(getRootFileSpecification().getPath(), e, SUB_SYSTEM);
            problems.add(problem);
        }
        finally
        {
            stopProfile(Operation.GET_SWF_TAGS);
        }
        
        return new ISWFTagsRequestResult()
        {
            @Override
            public boolean addToFrame(SWFFrame frame)
            {   
                ICharacterTag symbolTag = symbolClass.getSymbol();
                
                List<ITag> symbolTags = TagSorter.sortFullGraph(Collections.singletonList((ITag)symbolTag));
                
                for (ITag tag : symbolTags)
                {
                    frame.addTag(tag);
                    if (extraTags.containsKey(tag))
                    {
                        frame.addTag(extraTags.get(tag));
                    }
                }
                
                if (symbolClass.getAdditionalSymbolClasses() != null )
                {
                    for (FXGSymbolClass symbol : symbolClass.getAdditionalSymbolClasses())
                    {
                        frame.defineSymbol(symbol.getSymbol(), symbol.getQualifiedClassName());
                    }
                }

                frame.addTag(abcTag);
                frame.defineSymbol(symbolClass.getSymbol(), qname);
                
                return true;
            }
            
            @Override
            public ICompilerProblem[] getProblems()
            {
                return problems.toArray(new ICompilerProblem[0]);
            }

            @Override
            public String getDoABCTagName()
            {
                return abcTag.getName();
            }
            
            @Override
            public DoABCTag getDoABCTag()
            {
                return abcTag;
            }
        };
    }
    
    /**
     * Creates an empty FileScope for purposes of definition reporting
     * 
     * @return a {@link FXGFileScope} with a class definition for the FXG source
     */
    private FXGFileScope createFileScope()
    {
        FXGFileScope fileScope = new FXGFileScope(this, getAbsoluteFilename());

        String packageName = Multiname.getPackageNameForQName(qname);
        PackageScope packageScope = new PackageScope(fileScope, packageName);
        packageScope.setContainingScope(fileScope);

        PackageDefinition packageDefinition = new PackageDefinition(packageName);
        packageDefinition.setContainedScope(packageScope);

        fileScope.addDefinition(packageDefinition);

        Multiname mname = Multiname.crackDottedQName(getProject(), qname);
        INamespaceDefinition packageNS = Iterables.getOnlyElement(mname.getNamespaceSet());

        ClassDefinition classDefinition = new ClassDefinition(mname.getBaseName(), (INamespaceReference)packageNS);
        IReference baseClass = ReferenceFactory.packageQualifiedReference(getProject().getWorkspace(), fxgBaseClassName);
        classDefinition.setBaseClassReference(baseClass);

        TypeScope classScope = new TypeScope(packageScope, classDefinition);
        classScope.setContainingDefinition(classDefinition);
        classDefinition.setContainedScope(classScope);
        classDefinition.setupThisAndSuper();

        packageScope.addDefinition(classDefinition);

        return fileScope;
    }
}
