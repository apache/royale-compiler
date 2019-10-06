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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.IDefinitionPriority;
import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.css.CSSFunctionCallPropertyValue;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.internal.css.semantics.CSSSemanticAnalyzer;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.graph.LinkReportWriter;
import org.apache.royale.compiler.internal.projects.ASProject;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.units.requests.ABCBytesRequestResult;
import org.apache.royale.compiler.internal.units.requests.FileScopeRequestResultBase;
import org.apache.royale.compiler.internal.units.requests.RequestMaker;
import org.apache.royale.compiler.internal.units.requests.SyntaxTreeRequestResult;
import org.apache.royale.compiler.mxml.IXMLNameResolver;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem2;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.IInvisibleCompilationUnit;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.compiler.units.requests.IFileScopeRequestResult;
import org.apache.royale.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.royale.compiler.units.requests.IRequest;
import org.apache.royale.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.compiler.workspaces.IWorkspaceProfilingDelegate;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.StringEncoder;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Abstract class used to share implementation of some ICompilationUnit methods
 * across multiple concrete implementation classes.
 * <p>
 * This class converts calls to the getXXXRequest methods to calls into
 * handleXXXRequest methods. Subclasses can just implement the handleXXXRequest
 * methods without having to worry about creating an IRequest implementation.
 */
public abstract class CompilationUnitBase implements ICompilationUnit
{
    protected static final String DEFAULT_DO_ABC_TAG_NAME = "script";

    private static class InvisibleCompilationUnitRef extends FinalizableWeakReference<InvisibleCompilationUnit>
    {
        private final CompilationUnitBase delegate;

        /**
         * @param referent
         * @param queue
         */
        protected InvisibleCompilationUnitRef(CompilationUnitBase delegate, InvisibleCompilationUnit referent, FinalizableReferenceQueue queue)
        {
            super(referent, queue);
            this.delegate = delegate;
        }

        @Override
        public void finalizeReferent()
        {
            CompilerProject project = delegate.getProject();
            // if the project is null, the delegate CU has
            // already been manually removed by a client, so
            // nothing more to do.
            if (project == null)
                return;

            // when the InvisibleCompilationUnit get's gc'd, make sure
            // we also remove the CU which isn't pointing too.
            project.removeCompilationUnit(delegate);
        }
    }

    /**
     * Semantic analyze a collection of CSS model objects, and return a
     * {@link CSSCompilationSession} that contains the resolved symbols.
     * <ol>
     * <li>Resolve type selectors to {@link IClassDefinition} definitions.</li>
     * <li>Find all the dependencies introduced by {@code ClassReference()} and
     * {@code Embed()} property values.</li>
     * </ol>
     * 
     * @param cssCompilationSession A {@code CSSCompilationSession} object that
     * contains resolved symbols from all the CSS models in {@code cssDocuments}
     * collection.
     * @param xmlNameResolver Resolve type selectors to definitions.
     * @param cssDocuments A list of CSS model objects.
     * @param problems Compiler problem collection.
     */
    protected void updateStyleCompilationUnitDependencies(
            final CSSCompilationSession cssCompilationSession,
            final IXMLNameResolver xmlNameResolver,
            final Iterable<ICSSDocument> cssDocuments,
            final Collection<ICompilerProblem> problems)
    {
        final Set<IClassDefinition> classReferences = new LinkedHashSet<IClassDefinition>();
        final Set<EmbedCompilationUnit> dependentEmbedCompilationUnits = new LinkedHashSet<EmbedCompilationUnit>();

        for (final ICSSDocument cssDocument : cssDocuments)
        {
            final boolean isFlex3CSS = ((IRoyaleProject)project).getCSSManager().isFlex3CSS();
            final ImmutableMap<ICSSSelector, String> resolvedSelectors =
                    CSSSemanticAnalyzer.resolveSelectors(xmlNameResolver, cssDocument, problems, (IRoyaleProject) project, isFlex3CSS);
            
            // Store resolved type selectors required by CSS code generation.
            cssCompilationSession.resolvedSelectors.putAll(resolvedSelectors);

            // Store resolved embed compilation units required by CSS code generation.
            for (final ICSSRule cssRule : cssDocument.getRules())
            {
            	if (project instanceof IRoyaleProject && !((IRoyaleProject)project).isPlatformRule(cssRule))
            		continue;
                final Map<CSSFunctionCallPropertyValue, EmbedCompilationUnit> resolvedEmbedProperties =
                        new HashMap<CSSFunctionCallPropertyValue, EmbedCompilationUnit>();
                CSSSemanticAnalyzer.resolveDependencies(
                        resolvedEmbedProperties,
                        cssRule,
                        project,
                        classReferences,
                        dependentEmbedCompilationUnits,
                        problems);
                cssCompilationSession.resolvedEmbedProperties.putAll(resolvedEmbedProperties);
            }
        }

        // Convert from "IClassDefinition" to "ICompilationUnit".
        final Function<IClassDefinition, ICompilationUnit> findCompilationUnitForClass = new Function<IClassDefinition, ICompilationUnit>()
        {
            @Override
            public ICompilationUnit apply(final IClassDefinition classDefinition)
            {
                return project.getScope().getCompilationUnitForDefinition(classDefinition);
            }
        };
        final Collection<ICompilationUnit> classReferenceCompilationUnits =
                transform(classReferences, findCompilationUnitForClass);

        // "The 'IClassDefinition' was resolved from the project, so there should be a compilation 
        // unit for each IClassDefinition definition. If there's an exception for a null compilation
        // unit being added to the ImmutableSet, there's probably a bug in 
        // CSSSemanticAnalyzer.resolveDependencies().

        // Merge dependencies from ClassReference() and Embed().
        final ImmutableSet<ICompilationUnit> dependencies = new ImmutableSet.Builder<ICompilationUnit>()
                .addAll(classReferenceCompilationUnits)
                .addAll(dependentEmbedCompilationUnits)
                .build();
        for (final ICompilationUnit dependee : dependencies)
        {
            project.getDependencyGraph().addDependency(this, dependee, DependencyType.EXPRESSION);
        }
    }

    private CompilerProject project;
    private final String absoluteFilename;
    private List<IDefinition> definitionPromises;
    private final DefinitionPriority definitionPriority;
    protected final AtomicReference<IRequest<ISyntaxTreeRequestResult, ICompilationUnit>> syntaxTreeRequest;
    protected final AtomicReference<IRequest<IFileScopeRequestResult, ICompilationUnit>> fileScopeRequest;
    protected final AtomicReference<IRequest<IABCBytesRequestResult, ICompilationUnit>> abc;
    private final AtomicReference<IRequest<ISWFTagsRequestResult, ICompilationUnit>> tags;
    protected final AtomicReference<IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit>> problems;
    private final Set<ICompilationUnit> embedCompilationUnits;
    private IFileSpecificationGetter fileSpecificationGetter;
    private InvisibleCompilationUnitRef invisibleCURef;
    private final AtomicInteger state;
    
    /**
     * Cached name of this compilation unit. Must be set in constructor
     * 
     * It would be nice if this could be final, but it can't because of the multiplicity of constructors in this class
     */
    private String name;
    
    /**
     * Tracks whether or not the syntax tree must be built before the file scope
     * for this {@link ICompilationUnit} can be built. This flag is needed to
     * make incremental building of projects containing files on the source list
     * work.
     */
    private final boolean scopeBuiltFromSyntaxTree;

    private static final RequestMaker<ISyntaxTreeRequestResult, ICompilationUnit, CompilationUnitBase> syntaxTreeRequestMaker =
        new RequestMaker<ISyntaxTreeRequestResult, ICompilationUnit, CompilationUnitBase>()
        {
            @Override
            protected Callable<ISyntaxTreeRequestResult> getCallable(final CompilationUnitBase u)
            {
                return new Callable<ISyntaxTreeRequestResult>()
                {
                    @Override
                    public ISyntaxTreeRequestResult call() throws InterruptedException
                    {
                        return u.processSyntaxTreeRequest();
                    }
                };
            }

            @Override
            protected ISyntaxTreeRequestResult getResultForThrowable(CompilationUnitBase u, Throwable throwable)
            {
                final ICompilerProblem prob = new InternalCompilerProblem2(
                    u.getRootFileSpecification().getPath(), throwable, "syntax tree request");
                return new SyntaxTreeRequestResult(u.getRootFileSpecification().getLastModified(), Collections.singleton(prob));
            }
        };
        
    private static final RequestMaker<IFileScopeRequestResult, ICompilationUnit, CompilationUnitBase> fileScopeRequestMaker =
        new RequestMaker<IFileScopeRequestResult, ICompilationUnit, CompilationUnitBase>()
        {
            @Override
            protected Callable<IFileScopeRequestResult> getCallable(final CompilationUnitBase u)
            {
                return new Callable<IFileScopeRequestResult>()
                {
                    @Override
                    public IFileScopeRequestResult call() throws InterruptedException
                    {
                        return u.processFileScopeRequest();
                    }
                };
            }

            @Override
            protected IFileScopeRequestResult getResultForThrowable(CompilationUnitBase u, Throwable throwable)
            {
                final ICompilerProblem prob = new InternalCompilerProblem2(
                    u.getRootFileSpecification().getPath(), throwable, "file scope builder");
                return new FileScopeRequestResultBase(Collections.singleton(prob), null);
            }
        };

    private static final RequestMaker<IABCBytesRequestResult, ICompilationUnit, CompilationUnitBase> abcBytesRequestMaker =
        new RequestMaker<IABCBytesRequestResult, ICompilationUnit, CompilationUnitBase>()
        {
            @Override
            protected Callable<IABCBytesRequestResult> getCallable(final CompilationUnitBase u)
            {
                return new Callable<IABCBytesRequestResult>()
                {
                    @Override
                    public IABCBytesRequestResult call() throws Exception
                    {
                        return u.processABCBytesRequest();
                    }
                };
            }

            @Override
            protected IABCBytesRequestResult getResultForThrowable(CompilationUnitBase u, Throwable throwable)
            {
                return new ABCBytesRequestResult(new ICompilerProblem[] {
                        new InternalCompilerProblem2(u.getAbsoluteFilename(), throwable, "ABC generator")});
            }
        };


    private static final RequestMaker<ISWFTagsRequestResult, ICompilationUnit, CompilationUnitBase> swfTagsRequestMaker =
        new RequestMaker<ISWFTagsRequestResult, ICompilationUnit, CompilationUnitBase>()
        {
            @Override
            protected Callable<ISWFTagsRequestResult> getCallable(final CompilationUnitBase u)
            {
                return new Callable<ISWFTagsRequestResult>()
               {
                   @Override
                public ISWFTagsRequestResult call() throws Exception
                   {
                       return u.processSWFTTagsRequest();
                   }
               };
           }

            @Override
            protected ISWFTagsRequestResult getResultForThrowable(CompilationUnitBase u, Throwable throwable)
            {
                final ICompilerProblem prob = new InternalCompilerProblem2(
                    u.getRootFileSpecification().getPath(), throwable, "SWFTags");
                return new ISWFTagsRequestResult()
                {
                    @Override
                    public ICompilerProblem[] getProblems()
                    {
                        return new ICompilerProblem[] {prob};
                    }

                    @Override
                    public boolean addToFrame(SWFFrame f)
                    {
                        return false;
                    }

                    @Override
                    public String getDoABCTagName()
                    {
                        return "";
                    }
                    
                    @Override
                    public DoABCTag getDoABCTag()
                    {
                        return null;
                    }
                };
            }
        };

    private static final RequestMaker<IOutgoingDependenciesRequestResult, ICompilationUnit, CompilationUnitBase> outgoingDependenciesRequestMaker =
        new RequestMaker<IOutgoingDependenciesRequestResult, ICompilationUnit, CompilationUnitBase>()
        {
            @Override
            protected Callable<IOutgoingDependenciesRequestResult> getCallable(final CompilationUnitBase u)
            {
                return new Callable<IOutgoingDependenciesRequestResult>()
                {
                    @Override
                    public IOutgoingDependenciesRequestResult call() throws Exception
                    {
                        return u.processOutgoingDependenciesRequest();
                    }
                };
            }

            @Override
            protected IOutgoingDependenciesRequestResult getResultForThrowable(CompilationUnitBase u, Throwable throwable)
            {
                final ICompilerProblem prob = new InternalCompilerProblem2(
                    u.getRootFileSpecification().getPath(), throwable, "outgoing dependency");
                return new IOutgoingDependenciesRequestResult()
                {
                    @Override
                    public ICompilerProblem[] getProblems()
                    {
                        return new ICompilerProblem[] {prob};
                    }
                };
            }
        };
      
    /**
     * Master constructor. Other accesible constructors end up funneling down to
     * this one.
     * 
     * @param project
     * @param path
     * @param basePriority
     * @param doInitDefinitionPromises - if true will "complete" the
     * construction by allocating an empty list of definitions, and also
     * initialize the name field. If false, caller must initialize
     * this.definitionPromises and this.name
     * @param scopeBuiltFromSyntaxTree If true, the request that builds the file
     * scope requires that the request that builds the syntax tree be completed
     * first.
     */
    private CompilationUnitBase(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, boolean doInitDefinitionPromises, boolean scopeBuiltFromSyntaxTree)
    {
        this.project = project;
        this.absoluteFilename = FilenameNormalization.normalize(path);
        definitionPriority = new DefinitionPriority(basePriority, 0);
        syntaxTreeRequest = new AtomicReference<IRequest<ISyntaxTreeRequestResult, ICompilationUnit>>();
        fileScopeRequest = new AtomicReference<IRequest<IFileScopeRequestResult, ICompilationUnit>>();
        abc = new AtomicReference<IRequest<IABCBytesRequestResult, ICompilationUnit>>();
        tags = new AtomicReference<IRequest<ISWFTagsRequestResult, ICompilationUnit>>();
        problems = new AtomicReference<IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit>>();
        embedCompilationUnits = new HashSet<ICompilationUnit>();
        fileSpecificationGetter = project.getWorkspace();
        state = new AtomicInteger();
        if (doInitDefinitionPromises) 
        {
            definitionPromises = Collections.emptyList();
            name = computeName();              
        }
        this.scopeBuiltFromSyntaxTree = scopeBuiltFromSyntaxTree;
    }
    
    /**
     * This is the most generic constructor
     * 
     * @param project
     * @param path
     * @param basePriority
     * @param qnamesOfDefinitions is a collection with all the definition promises for the compilation unit
     */
    protected CompilationUnitBase(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, Collection<String> qnamesOfDefinitions)
    {
        this(project, path, basePriority, false, qnamesOfDefinitions.isEmpty());
        definitionPromises = createDefinitionPromisesFromQnames(qnamesOfDefinitions, this);
        name = computeName();       // now that the definitions are set, we can cache the name
    }
    
    
    /**
     * Use this constructor if derived class has no definition promises
     */
    protected CompilationUnitBase(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, boolean scopeBuiltFromSytaxTree)
    {
        this(project, path, basePriority, true, scopeBuiltFromSytaxTree);
    }
    
    /**
     * Use this constructor if the derived class has at most one definition promise
     * 
     * @param project
     * @param path
     * @param basePriority
     * @param qnameOfDefinition is the definition promise, or null
     */
    protected CompilationUnitBase(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, String qnameOfDefinition)
    {
        this(project, path, basePriority, qnameOfDefinition == null ? Collections.<String>emptyList() : Collections.<String>singletonList(qnameOfDefinition));
    }
    
   
    private static List<IDefinition> createDefinitionPromisesFromQnames(Collection<String> qnames, ICompilationUnit cu)
    {
        List<IDefinition> ret = new ArrayList<IDefinition>();
        for (String qname : qnames)
        {
            if (qname != null)  // some ctors pass in null qname string - it makes things easier. Just ignore them
                ret.add(ASProjectScope.createDefinitionPromise(qname, cu));
        }
        return ret;
    }
    
    /**
     * Helper method that atomically updates the state of this compilation unit
     * to reflect that the specified operation is now complete.
     * @param operation The operation that is now complete.
     */
    private final void operationComplete(ICompilationUnit.Operation operation)
    {
        boolean done = false;
        do
        {
            int currentState = state.get();
            int newState = currentState | operation.mask;
            done = state.compareAndSet(currentState, newState);
        } while (!done);
    }
    
    /**
     * Atomically checks the state of this compilation unit to determine
     * if all the specified operations are complete.
     * @param operations The set of operation to check.
     * @return true if all the specified operations are complete, false otherwise.
     */
    protected final boolean operationsCompleted(EnumSet<ICompilationUnit.Operation> operations)
    {
        int currentState = state.get();
        for (ICompilationUnit.Operation operation : operations)
        {
            if ((currentState & operation.mask) == 0)
                return false;
        }
        return true;
    }

    /**
     * @return List of definition promises
     */
    @Override
    public List<IDefinition> getDefinitionPromises()
    {
        return definitionPromises;
    }

    @Override
    public CompilerProject getProject()
    {
        return project;
    }

    /**
     * Builds a {@link ISyntaxTreeRequestResult}.  In many cases the {@link ISyntaxTreeRequestResult}
     * is built by parsing a source file into a syntax tree.
     * <p>
     * Called by this class from potentially any thread. This class guarantees
     * that this method will only be entered from one thread at a time for a
     * given instance of this class, so implementations do not need to make this
     * a synchronized method.
     *
     * @return The {@link ISyntaxTreeRequestResult} for this compilation unit.
     * @throws InterruptedException
     */
    protected abstract ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException;
    
    /**
     * Builds a {@link IFileScopeRequestResult}. In many cases the
     * {@link IFileScopeRequestResult} is built by walking an AST produced by a parser.
     * <p>
     * Called by this class from potentially any thread. This class guarantees
     * that this method will only be entered from one thread at a time for a
     * given instance of this class, so implementations do not need to make this
     * a synchronized method.
     * 
     * @return The {@link IFileScopeRequestResult} for this compilation unit.
     */
    protected abstract IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException;

    /**
     * Builds a IABCBytesRequestResult. In many cases the IABCBytesRequestResult
     * is built running a code generator over an AST produced by a parser.
     * <p>
     * Called by this class from potentially any thread. This class guarantees
     * that this method will only be entered from one thread at a time for a
     * given instance of this class, so implementations do not need to make this
     * a synchronized method.
     * 
     * @return The IABCBytesRequestResult for this compilation unit.
     */
    protected abstract IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException;

    /**
     * Builds a ISWFTagsRequestResult. In many cases the ISWFTagsRequestResult is built
     * getting the IABCBytesRequestResult and wrapping its abc bytes in a
     * DoABCTag.
     * <p>
     * Called by this class from potentially any thread. This class guarantees
     * that this method will only be entered from one thread at a time for a
     * given instance of this class, so implementations do not need to make this
     * a synchronized method.
     * 
     * @return The ISWFTagsRequestResult for this compilation unit.
     */
    protected abstract ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException;

    /**
     * Builds a IOutgoingDependenciesRequestResult. In many cases the
     * IOutgoingDependenciesRequestResult is built getting the walking an AST.
     * <p>
     * Called by this class from potentially any thread. This class guarantees
     * that this method will only be entered from one thread at a time for a
     * given instance of this class, so implementations do not need to make this
     * a synchronized method.
     * 
     * @return The IOutgoingDependenciesRequestResult for this compilation unit.
     */
    protected abstract IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException;

    @Override
    public IRequest<ISyntaxTreeRequestResult, ICompilationUnit> getSyntaxTreeRequest()
    {
        return syntaxTreeRequestMaker.getRequest(this, syntaxTreeRequest, project.getWorkspace(), scopeBuiltFromSyntaxTree);
    }

    @Override
    public IRequest<IFileScopeRequestResult, ICompilationUnit> getFileScopeRequest()
    {
        return fileScopeRequestMaker.getRequest(this, fileScopeRequest, project.getWorkspace(), true);
    }

    @Override
    public IRequest<IABCBytesRequestResult, ICompilationUnit> getABCBytesRequest()
    {
        return abcBytesRequestMaker.getRequest(this, abc, project.getWorkspace(), false);
    }

    @Override
    public IRequest<ISWFTagsRequestResult, ICompilationUnit> getSWFTagsRequest()
    {
        return swfTagsRequestMaker.getRequest(this, tags, project.getWorkspace(), false);
    }

    @Override
    public IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit> getOutgoingDependenciesRequest()
    {
        return outgoingDependenciesRequestMaker.getRequest(this, problems, project.getWorkspace(), false);
    }

    private Collection<IDefinition> getAllDefinitions() throws InterruptedException
    {
        Collection<IDefinition> definitions;
        if (definitionPromises.isEmpty())
        {
            IFileScopeRequestResult fileScopeRequestResult = getFileScopeRequest().get();
            definitions = fileScopeRequestResult.getExternallyVisibleDefinitions();
        }
        else
        {
            definitions = definitionPromises;
        }

        return definitions;
    }

    @Override
    public List<String> getShortNames() throws InterruptedException
    {
        Collection<IDefinition> definitions = getAllDefinitions();

        List<String> shortNames = new ArrayList<String>(definitions.size());
        for (IDefinition definition : definitions)
        {
            shortNames.add(definition.getBaseName());
        }

        return shortNames;
    }

    @Override
    public List<String> getQualifiedNames() throws InterruptedException
    {
        Collection<IDefinition> definitions = getAllDefinitions();

        List<String> qualifiedNames = new ArrayList<String>(definitions.size());
        for (IDefinition definition : definitions)
        {
            qualifiedNames.add(definition.getQualifiedName());
        }

        return qualifiedNames;
    }

    @Override
    public final String getAbsoluteFilename()
    {
        return absoluteFilename;
    }

    protected final String getFilenameNoPath()
    {
        return FilenameUtils.getName(absoluteFilename);
    }

    protected IFileSpecification getRootFileSpecification()
    {
        final String fileName = getAbsoluteFilename();
        return fileSpecificationGetter.getFileSpecification(fileName);
    }
    
    protected IFileSpecificationGetter getFileSpecificationGetter()
    {
        return fileSpecificationGetter;
    }
    
    @Override
    public boolean clean(Map<ICompilerProject, Set<File>> invalidatedSWCFiles, Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate, final boolean clearFileScope)
    {
        IWorkspaceProfilingDelegate profilingDelegate = project.getWorkspace().getProfilingDelegate();

        if (profilingDelegate != null)
            profilingDelegate.operationStarted(this, Operation.INVALIDATE_CU);

        project.removeDependencies(Collections.<ICompilationUnit>singletonList(this));

        if (clearFileScope)
        {
            clearIncludedFilesFromWorkspace();

            // If this compilation unit is invisible don't try to
            // remove definitions from the project scope.
            if (!isInvisible())
                project.getScope().removeCompilationUnits(Collections.<ICompilationUnit>singletonList(this));

            fileScopeRequest.set(null);

            // If we are are invalidating the file scope,
            // we should also invalidate the ast.
            syntaxTreeRequest.set(null);

            project.removeAnyUnfoundDependencies(this);
        }

        abc.set(null);
        tags.set(null);
        problems.set(null);
        embedCompilationUnits.clear();

        project.resetScopeCacheForCompilationUnit(this);

        if (clearFileScope)
            updateDefinitions(cusToUpdate);

        // delegate to a virtual method that sub-classes can override to
        // do additional cleaning.
        handleClean(clearFileScope, invalidatedSWCFiles);

        if (profilingDelegate != null)
            profilingDelegate.operationCompleted(this, Operation.INVALIDATE_CU);

        return true;
    }

    /**
     * This method is used to update the Workspace
     * includeFilesToIncludingCompilationUnitMapping map when a file get's cleaned.  It
     * goes to great pains to not reparse the file, as if the file hasn't been parsed,
     * there shouldn't be any files related to this compilation unit in the map.
     */
    public void clearIncludedFilesFromWorkspace()
    {
        try
        {
            IRequest<ISyntaxTreeRequestResult, ICompilationUnit> req = syntaxTreeRequest.get();
            if (req != null && req.isDone())
                project.getWorkspace().removeIncludedFilesToCompilationUnit(this, req.get().getIncludedFiles());
        }
        catch (InterruptedException e)
        {
            // this should never happen, as req is only ever called when it's already done.
        }        
    }

    /**
     * This method is overriden by base classes to
     * clean additional processing results not cleaned by
     * the clean method above.
     * @param invalidatedSWCFiles
     */
    protected void handleClean(boolean clearFileScope, Map<ICompilerProject, Set<File>> invalidatedSWCFiles)
    {

    }

    private void updateDefinitions(Map<ICompilerProject, Set<ICompilationUnit>> cusToUpdate)
    {
        // If this compilation unit is the delegate
        // of an invisible compilation unit, then do
        // *not* add any definition to the project
        // symbol table.
        if (isInvisible())
            return;
        
        // Add back in any definition promises or FileScopes depending how the
        // input file was added to the project
        if (definitionPromises.isEmpty()) // no definitionPromises, so parse file
        {
            Set<ICompilationUnit> cus = cusToUpdate.get(getProject());
            if (cus == null)
            {
                cus = new HashSet<ICompilationUnit>();
                cusToUpdate.put(getProject(), cus);
            }
            cus.add(this);
        }
        else // add definition promises
        {
            for (IDefinition definitionPromise : definitionPromises)
            {
                if (definitionPromise instanceof DefinitionPromise)
                    ((DefinitionPromise)definitionPromise).clean();
                
                project.getScope().addDefinition(definitionPromise);
            }
        }
    }

    protected final void startProfile(Operation operation)
    {
        IWorkspaceProfilingDelegate profilingDelegate = project.getWorkspace().getProfilingDelegate();

        if (profilingDelegate == null)
            return;

        profilingDelegate.operationStarted(this, operation);
    }

    protected final void stopProfile(Operation operation)
    {
        IWorkspaceProfilingDelegate profilingDelegate = project.getWorkspace().getProfilingDelegate();

        if (profilingDelegate == null)
            return;

        profilingDelegate.operationCompleted(this, operation);
    }

    private ISyntaxTreeRequestResult processSyntaxTreeRequest() throws InterruptedException
    {
        ISyntaxTreeRequestResult result = handleSyntaxTreeRequest();
        IASNode ast = result.getAST();
        verifyAST(ast);
        operationComplete(ICompilationUnit.Operation.GET_SYNTAX_TREE);
        return result;
    }
    
    protected void verifyAST(IASNode ast)
    {
        if (ast != null)
            assert ((NodeBase)ast).verify() : "AST failed verification";
    }

    protected final void addScopeToProjectScope(ASFileScope[] scopes)
    {
        ASProjectScope projectScope = project.getScope();
        for (ASFileScope scope : scopes)
        {
            assert scope.verify() : "Scope failed verification";
            projectScope.addScopeForCompilationUnit(this, scope);
        }
    }

    private IFileScopeRequestResult processFileScopeRequest() throws InterruptedException
    {
        FileScopeRequestResultBase result = (FileScopeRequestResultBase)handleFileScopeRequest();

        // add the scopes for this compilation unit
        ASFileScope[] scopes = result.getFileScopes();
        addScopeToProjectScope(scopes);

        operationComplete(ICompilationUnit.Operation.GET_FILESCOPE);
        return result;
    }

    private IABCBytesRequestResult processABCBytesRequest() throws InterruptedException
    {
        IABCBytesRequestResult result = handleABCBytesRequest();
        operationComplete(ICompilationUnit.Operation.GET_ABC_BYTES);
        removeAST();

        return result;
    }

    private ISWFTagsRequestResult processSWFTTagsRequest() throws InterruptedException
    {
        ISWFTagsRequestResult result = handleSWFTagsRequest();
        operationComplete(ICompilationUnit.Operation.GET_SWF_TAGS);
        return result;
    }

    private IOutgoingDependenciesRequestResult processOutgoingDependenciesRequest () throws InterruptedException
    {
        IOutgoingDependenciesRequestResult result = handleOutgoingDependenciesRequest();
        operationComplete(ICompilationUnit.Operation.GET_SEMANTIC_PROBLEMS);

        removeAST();

        return result;
    }

    /**
     * Iterate through all imports within a IFileNodeAccumulator and start parsing any
     * non-wildcard imports.  This will help speed discovery of dependencies to
     * allow better thread utilization.
     * 
     * @param fna IFileNodeAccumulator
     */
    protected void startParsingImports(IFileNodeAccumulator fna)
    {
        ASProjectScope projectScope = getProject().getScope();
        List<IImportNode> importNodes = fna.getImportNodes();
        Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();
        for (IImportNode importNode : importNodes)
        {
            if (!importNode.isWildcardImport())
            {
                String importName = importNode.getImportName();
                int index = importName.lastIndexOf('.');
                if (index != -1)
                {
                    String className = importName.substring(index + 1);
                    compilationUnits.addAll(projectScope.getCompilationUnitsByDefinitionName(className));
                }
            }
        }

        // Now that we have the compilation units from the imports,
        // start a parallel build on them.
        for (ICompilationUnit compilationUnit : compilationUnits)
        {
            compilationUnit.startBuildAsync(TargetType.SWF);
        }
    }

    /**
     * Iterate through all specified embeds, adding the
     * EmbedCompilationUnit dependencies, and removing any EmbedCompilationUnits
     * which no longer exist
     * <p>
     * This function adds an empty definition dependency to the {@link DependencyGraph},
     * so it will not be printed to the link-report
     * @throws InterruptedException 
     */
    protected void updateEmbedCompilationUnitDependencies(List<IEmbedResolver> embedNodes, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        Set<ICompilationUnit> previousEmbedCompilationUnits = new HashSet<ICompilationUnit>(embedCompilationUnits);
        embedCompilationUnits.clear();

        CompilerProject project = getProject();

        for (IEmbedResolver embedNode : embedNodes)
        {
            ICompilationUnit cu = embedNode.resolveCompilationUnit(project);
            if (cu != null)
            {
                // if the cu has already been part of this CU, nothing to do
                if (!previousEmbedCompilationUnits.remove(cu))
                {
                    // this dependency targets the embedNode's (import etc) data qname
                    project.addDependency(this, cu, DependencyType.EXPRESSION, cu.getQualifiedNames().get(0));
                }

                embedCompilationUnits.add(cu);
            }            
        }

        // remove any CUs which are no longer referenced in this file
        project.removeCompilationUnits(previousEmbedCompilationUnits);
    }

    @Override
    public Collection<String> getEmbeddedFilenames()
    {
        if (embedCompilationUnits.isEmpty())
            return Collections.emptySet();

        Set<String> filenames = new HashSet<String>();
        for (ICompilationUnit cu : embedCompilationUnits)
        {
            EmbedData data = ((EmbedCompilationUnit)cu).getEmbedData();
            String sourcePath = (String)data.getAttribute(EmbedAttribute.SOURCE);

            // the source attribute can be null or empty if the file doesn't exist
            // or wasn't specified.
            if (sourcePath != null && !sourcePath.isEmpty())
                filenames.add(sourcePath);
        }

        return filenames;
    }

    protected void removeAST()
    {
        // don't do anything by default
    }

    @Override
    public String getName()
    {
       assert name.equals(computeName());       // verify that our cache is valid, but only if asserts are enabled
       return name;
    }
   
    /**
     * Puts together the compilation unit's name.
     * Normally would be cached, as it can be slow.
     */
    private String computeName()
    {
        String absoluteFileName = getAbsoluteFilename();
        final String filename = FilenameUtils.getName(absoluteFileName).replace('.', '_');
        if (definitionPromises.isEmpty())
        {
        	// conditional compilation units may not have definitionpromises
            String encodedName = StringEncoder.stringToHashCodeString(absoluteFileName);
            encodedName +=  ":" + filename;
            return encodedName;
        }
        // we used to use the absolute path, but it would be different
        // on different machines and we want builds to be binary reproducible.
        // So we will use the first definition's QName as that should be unique
        IDefinition def0 = getDefinitionPromises().get(0);
        final String encodedQName = StringEncoder.stringToHashCodeString(def0.getQualifiedName());
        String encodedName = encodedQName + ":" + filename;

        try
        {
            List<String> qualifiedNames = getQualifiedNames();
            Collections.sort(qualifiedNames, new LinkReportWriter.QNameComparator());
            return encodedName + ":" + Joiner.on(' ').join(qualifiedNames);
        }
        catch (InterruptedException e)
        {
        }
        assert false : "Should not get here, because we should be able to compute qnames for def promises without getting interrupted!";
        return null;
    }

    /**
     * @return a string, which is helpful when inspecting in a debugger.
     */
    @Override
    public String toString()
    {
        return getAbsoluteFilename();
    }

    @Override
    public IDefinitionPriority getDefinitionPriority()
    {
        return definitionPriority;
    }

    @Override
    public void clearProject()
    {
        project = null;
    }
    
    @Override
    public void waitForBuildFinish(final Collection<ICompilerProblem> problems, TargetType targetType) throws InterruptedException
    {
        assert problems != null : "Expected 'problems'. Do not ignore problems.";
        Collections.addAll(problems, getSyntaxTreeRequest().get().getProblems());
        Collections.addAll(problems, getFileScopeRequest().get().getProblems());
        Collections.addAll(problems, getOutgoingDependenciesRequest().get().getProblems());
        if (targetType == null)
        {
        	ICompilerProblem[] probs = getABCBytesRequest().get().getProblems();
        	for (ICompilerProblem prob : probs)
        	{
        		if (!(prob instanceof InternalCompilerProblem2))
        		{
        			problems.add(prob);
        		}
        	}
        }
        else
        {
        	Collections.addAll(problems, getABCBytesRequest().get().getProblems());
        	Collections.addAll(problems, getSWFTagsRequest().get().getProblems());
        }
    }

    @Override
    public void startBuildAsync(TargetType targetType)
    {
        boolean onlyDoOutgoing = false;

        if (!onlyDoOutgoing)
        {
            getSyntaxTreeRequest();
            getFileScopeRequest();
        }

        getOutgoingDependenciesRequest();

        if (!onlyDoOutgoing)
        {
            getABCBytesRequest();
            getSWFTagsRequest();
        }
    }

    /*
     * Sets the {@link IFileSpecificationGetter} used by this
     * {@link ICompilationUnit} to open files.
     * <p>
     * This method should be called very shortly after the constructor and
     * before any other methods on this class or its sub-classes are called
     * (except {@link #makeInvisible(InvisibleCompilationUnit)}).
     * The {@link IFileSpecificationGetter}
     * should really be a constructor argument, but that would require updating
     * all the sub-classes of this class and the factory class that constructs
     * {@link ICompilationUnit}'s.
     * 
     * @param getter The {@link IFileSpecificationGetter} used by this
     * {@link ICompilationUnit} to open files.
     */
    public void setFileSpecificationGetter(IFileSpecificationGetter getter)
    {
        fileSpecificationGetter = getter;
    }
    
    /**
     * Marks this {@link ICompilationUnit} as the delegate of an
     * {@link IInvisibleCompilationUnit}, which will prevent this
     * {@link ICompilationUnit} from contributing any symbols to the containing
     * {@link ICompilerProject}'s {@link ASProjectScope}.
     * <p>
     * This method should be called very shortly after the constructor and
     * before any other methods on this class or its sub-classes are called (
     * except {@link #setFileSpecificationGetter(IFileSpecificationGetter)} ).
     * <p>
     * This method must only be called zero or one time on each
     * {@link ICompilationUnit}.
     */
    public void makeInvisible(InvisibleCompilationUnit invisibleCU)
    {
        assert invisibleCURef == null : "makeInvisible should called exactly once or not at all";
        invisibleCURef = new InvisibleCompilationUnitRef(this, invisibleCU, getProject().getWorkspace().getInvisibleCompilationUnitReferenceQueue());
    }

    @Override
    public final boolean isInvisible()
    {
        return invisibleCURef != null;
    }

    protected Map<String, String> getEncodedDebugFiles() throws InterruptedException
    {
        assert (this instanceof ASCompilationUnit || this instanceof MXMLCompilationUnit) : "getEncodedDebugFiles should only be called by AS or MXML compilation units";

        // only file on the source path are encoded, and only ASProject's have
        // source path, so bail if it's not an ASProject
        if (!(getProject() instanceof ASProject))
            return Collections.<String, String>emptyMap();

        ASProject asProject = (ASProject)getProject();

        // bail if the file isn't on the source path
        File sourceFile = new File(getAbsoluteFilename());
        if (!asProject.isFileOnSourcePath(sourceFile))
            return Collections.<String, String>emptyMap();

        // as we're only dealing with AS or MXML compilation units on the source path,
        // there should only ever be one definition, so just grab the first def.
        IDefinition def = Iterables.getOnlyElement(getAllDefinitions());
        String packagePath = def.getPackageName().replace('.', File.separatorChar);
        String filenameNoPath = getFilenameNoPath();
        String rootPath;
        if (packagePath.isEmpty())
            rootPath = getAbsoluteFilename().replace(File.separatorChar + filenameNoPath, "");
        else
            rootPath = getAbsoluteFilename().replace(File.separatorChar + packagePath + File.separatorChar + filenameNoPath, "");
        if (asProject instanceof RoyaleProject)
        {
        	String swfDebugfileAlias = ((RoyaleProject)asProject).getSwfDebugfileAlias();
        	if (swfDebugfileAlias != null)
        	{
        		rootPath = swfDebugfileAlias;
        		packagePath = packagePath.replace('\\', '/');
        	}
        }

        String encodedPath = rootPath + ';' + packagePath + ';' + getFilenameNoPath();

        Map<String, String> encodedDebugFiles = new HashMap<String, String>(1);
        encodedDebugFiles.put(getAbsoluteFilename(), encodedPath);
        return encodedDebugFiles;
    }
}
