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

package org.apache.flex.compiler.internal.units;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.flex.compiler.clients.ASC;
import org.apache.flex.compiler.common.IMetaInfo;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.filespecs.FileSpecification;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.as.codegen.CodeGeneratorManager;
import org.apache.flex.compiler.internal.parsing.as.ASParser;
import org.apache.flex.compiler.internal.parsing.as.ASToken;
import org.apache.flex.compiler.internal.parsing.as.DeferFunctionBody;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.projects.DefinitionPriority;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.scopes.ASFileScope;
import org.apache.flex.compiler.internal.semantics.PostProcessStep;
import org.apache.flex.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.flex.compiler.internal.tree.as.ClassNode;
import org.apache.flex.compiler.internal.tree.as.FileNode;
import org.apache.flex.compiler.internal.tree.as.FullNameNode;
import org.apache.flex.compiler.internal.tree.as.IdentifierNode;
import org.apache.flex.compiler.internal.tree.as.ImportNode;
import org.apache.flex.compiler.internal.tree.as.PackageNode;
import org.apache.flex.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.flex.compiler.internal.units.requests.ASFileScopeRequestResult;
import org.apache.flex.compiler.internal.units.requests.SWFTagsRequestResult;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFileNodeAccumulator;
import org.apache.flex.compiler.tree.as.IImportNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.requests.IABCBytesRequestResult;
import org.apache.flex.compiler.units.requests.IFileScopeRequestResult;
import org.apache.flex.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.flex.compiler.units.requests.IRequest;
import org.apache.flex.compiler.units.requests.ISWFTagsRequestResult;
import org.apache.flex.compiler.units.requests.ISyntaxTreeRequestResult;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class ASCompilationUnit extends CompilationUnitBase
{
    /**
     * Implementation of {@link ISyntaxTreeRequestResult} that has the added feature
     * of being able to transform the reference to the syntax tree to a weak reference.
     */
    private static class ASSyntaxTreeRequestResult implements ISyntaxTreeRequestResult
    {
        private static class HardToWeakRef<T> extends WeakReference<T>
        {
            /**
             * Constructs a reference to the specified object.
             * @param referent Object to refer to.
             */
            public HardToWeakRef(T referent)
            {
                super(referent);
                this.referent = referent;
            }
            
            private T referent;
            
            public void makeWeak()
            {
                // No lock here because we are writing a single
                // reference variable that is not a long or double.
                referent = null;
            }

            @Override
            public T get()
            {
                // Overriding this method just to make
                // sure the java compiler or vm does not
                // optimize away the member variable that holds
                // strong reference.
                
                // No lock here because we are reading a single reference
                // variable that is not a long or double.
                //
                // We read the member variable into a local so we can return it
                // without worrying about the another thread bashing
                // the member variable.
                T referent = this.referent;
                if (referent != null)
                    return referent;
                return super.get();
            }
            
            
        }

        ASSyntaxTreeRequestResult(ASCompilationUnit owner, IRequest<ISyntaxTreeRequestResult, ICompilationUnit> syntaxTreeRequest, IASNode ast, ImmutableSet<String> includedFiles, long lastModified, Collection<ICompilerProblem> problems)
        {
            ownerRef = new WeakReference<ASCompilationUnit>(owner);
            this.syntaxTreeRequest = syntaxTreeRequest;
            astRef = new HardToWeakRef<IASNode>(ast);
            this.includedFiles = includedFiles;
            this.problems = problems.toArray(new ICompilerProblem[problems.size()]);
            this.lastModified = lastModified;
        }

        private final WeakReference<ASCompilationUnit> ownerRef;
        private final IRequest<ISyntaxTreeRequestResult, ICompilationUnit> syntaxTreeRequest;
        private final HardToWeakRef<IASNode> astRef;
        private final ImmutableSet<String> includedFiles;
        private final long lastModified;
        private final ICompilerProblem[] problems;

        @Override
        public ICompilerProblem[] getProblems()
        {
            return problems;
        }

        @Override
        public IASNode getAST() throws InterruptedException
        {
            // This method needs to return the syntax tree
            // for this compilation unit.
            //
            // Since we might have allowed the syntax tree
            // to be GC'd we may have to repase the file.
            
            // First see if we still have the AST..
            IASNode result = astRef.get();
            if (result != null)
                return result;
            
            // We allowed the syntax tree to be gc'd.
            // Now we have to get hold of our owning
            // compilation unit to tell it to reparse the file.
            ASCompilationUnit owner = ownerRef.get();
            // If our owning compilation unit has been gc'd
            // then we are just a stale result object.  Just bail.
            if (owner == null)
                return null;
            // The reference to our owner is still good.
            // Use compare and set to atomically update our
            // owner's reference to us.  We don't care if it
            // ends up being null or a pointing to someone else.
            owner.syntaxTreeRequest.compareAndSet(syntaxTreeRequest, null);
            // Now ask our owner for the syntax tree.
            return owner.getSyntaxTreeRequest().get().getAST();
        }
        
        /**
         * Called by the {@link ASCompilationUnit} to make the reference to the syntax
         * tree held by this class a weak reference.
         */
        public void dropASTRef()
        {
            astRef.makeWeak();
        }
        
        @Override
        public Set<String> getRequiredResourceBundles() throws InterruptedException
        {
            IASNode tree = getAST();
            
            if(tree instanceof IFileNodeAccumulator)
            {
                return ((IFileNodeAccumulator)tree).getRequiredResourceBundles();
            }
            
            return Collections.emptySet();
        }

        @Override
        public ImmutableSet<String> getIncludedFiles()
        {
            return includedFiles;
        }

        @Override
        public long getLastModified()
        {
            return lastModified;
        }
    }
    
    /**
     * Create a main compilation unit for ASC client. This factory method will
     * setup the included files specified by {@code -in} option onto the
     * tokenizer.
     * <p>
     * Using this factory method so that we don't have to expose
     * {@code ASCompilationUnit#setIncludedFiles(List)}, because it is specific
     * to {@link ASC} only.
     * 
     * @param project Compiler project.
     * @param mainFile Main source file.
     * @param asc ASC client instance.
     * @return Main ActionScript compilation unit.
     */
    public static ASCompilationUnit createMainCompilationUnitForASC(
            final CompilerProject project,
            final IFileSpecification mainFile,
            final ASC asc)
    {
        assert project != null : "Expecting project.";
        assert mainFile != null : "Expecting main file.";
        assert asc != null : "Expecting ASC client.";

        final ASCompilationUnit mainCompilationUnit = new ASCompilationUnit(
                project,
                mainFile.getPath(),
                DefinitionPriority.BasePriority.SOURCE_LIST);
        mainCompilationUnit.includedFiles.addAll(asc.getIncludeFilenames());
        return mainCompilationUnit;
    }

    public ASCompilationUnit(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority)
    {
        this(project, path, basePriority, 0);
    }

    public ASCompilationUnit(CompilerProject project, String path, DefinitionPriority.BasePriority basePriority, int order)
    {
        this(project, path, basePriority, order, null);
    }

    public ASCompilationUnit(CompilerProject project, String path,
                             DefinitionPriority.BasePriority basePriority,
                             int order,
                             String qname)
    {
        super(project, path, basePriority, qname);
        this.qname = qname;
        ((DefinitionPriority) getDefinitionPriority()).setOrder(order);
    }

    // The fully-qualified name of the one externally-visible definition
    // expected to be found in this compilation unit, or null if none is expected.
    // This qname is determined from the name of the file
    // and the file's location relative to the source path.
    private final String qname;
    
    /**
     * This field is specific to {@link ASC} client. It's a list of files
     * included by {@code -in} option.
     */
    private final List<String> includedFiles = new ArrayList<String>();
    
    @Override
    public UnitType getCompilationUnitType()
    {
        return UnitType.AS_UNIT;
    }

    /**
     * Creates the FileNode to be returned by the syntax tree request
     * 
     * @param specification the {@link IFileSpecification} for the given file
     * @return a {@link FileNode}
     */
    protected FileNode createFileNode(IFileSpecification specification)
    {
        // Only defer function body if the compilation unit is from an actual AS 
        // file, and the compilation unit is not "invisible" (currently not 
        // open in IDE). "isInvisible" means the compilation unit is invisible 
        // to semantic analyzer. It, however, is "visible" to the user in the 
        // IDE.
        final DeferFunctionBody deferFunctionBody;
        if(!isInvisible() && specification instanceof FileSpecification)
            deferFunctionBody = DeferFunctionBody.ENABLED;
        else
            deferFunctionBody = DeferFunctionBody.DISABLED;

        final IASProject flashProject;
        if(getProject() instanceof IASProject)
            flashProject = (IASProject)getProject();
        else
            flashProject = null;
        
        // Parse the AS file into an AST and build a symbol table for it.
        return ASParser.parseFile(
                specification,
                getFileSpecificationGetter(),
                EnumSet.of(PostProcessStep.CALCULATE_OFFSETS),
                this.getProject().getProjectConfigVariables(),
                true,
                this.getProject().isAssetEmbeddingSupported(),
                includedFiles,
                deferFunctionBody,
                flashProject,
                this);
    }

    @Override
    protected ISyntaxTreeRequestResult handleSyntaxTreeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_SYNTAX_TREE);
        try
        {
            IRequest<ISyntaxTreeRequestResult, ICompilationUnit> syntaxTreeRequest = this.syntaxTreeRequest.get();
            
            boolean isBindable = false;
            PackageNode pkg = null;
            ClassNode classNode = null;
            IMetaInfo[] metaInfos = null;
            final FileNode ast = createFileNode(getRootFileSpecification());
            if (this.getProject() instanceof FlexProject)
            {
                IASNode child = ast.getChild(0);
                if (child instanceof PackageNode)
                {
                    pkg = (PackageNode)child;
                    IDefinitionNode[] memberNodes = pkg.getAllMemberDefinitionNodes();
                    if (memberNodes[0] instanceof ClassNode)
                    {
                        classNode = (ClassNode)memberNodes[0];
                        memberNodes = classNode.getAllMemberNodes();
                        metaInfos = pkg.getMetaInfos();
                        for (IMetaInfo metaInfo : metaInfos)
                        {
                            String name = metaInfo.getTagName();
                            if (name.equals("Bindable"))
                            {
                                isBindable = true;
                                break;
                            }
                        }
                    }
                    if (!isBindable)
                    {
                        for (IDefinitionNode memberNode : memberNodes)
                        {
                            if (memberNode instanceof BaseDefinitionNode)
                            {
                                BaseDefinitionNode bdn = (BaseDefinitionNode)memberNode;
                                metaInfos = bdn.getMetaInfos();
                                for (IMetaInfo metaInfo : metaInfos)
                                {
                                    String name = metaInfo.getTagName();
                                    if (name.equals("Bindable"))
                                    {
                                        isBindable = true;
                                        break;
                                    }
                                }
                                if (isBindable)
                                    break;
                            }
                        }
                    }
                    if (isBindable)
                    {
                        IExpressionNode baseNode = classNode.getBaseClassNode();
                        Collection<IImportNode> importNodes = new ArrayList<IImportNode>();
                        ast.getAllImportNodes(importNodes);
                        if (baseNode == null)
                        {
                            // bindable class extends Object, must switch to
                            // extend EventDispatcher
                            IdentifierNode baseClassNode = new IdentifierNode("EventDispatcher");
                            baseClassNode.setParent(classNode);
                            classNode.setBaseClass(baseClassNode);
                            IdentifierNode flash = new IdentifierNode("flash");
                            IdentifierNode events = new IdentifierNode("events");
                            FullNameNode flashDotEvents = new FullNameNode(flash, 
                                    new ASToken(ASToken.TOKEN_OPERATOR_MEMBER_ACCESS, 0, 0, 0, 0, "."), events);
                            FullNameNode fullNameNode = new FullNameNode(flashDotEvents,
                                    new ASToken(ASToken.TOKEN_OPERATOR_MEMBER_ACCESS, 0, 0, 0, 0, "."), 
                                    baseClassNode);
                            ImportNode importNode = new ImportNode(fullNameNode);
                            ScopedBlockNode sbn = (ScopedBlockNode)pkg.getChild(1);
                            sbn.addChild(importNode, 0);
                        }
                    }
                }
            }
            

            IRequest<IFileScopeRequestResult, ICompilationUnit> fileScopeRequest = this.fileScopeRequest.get();
            if ((fileScopeRequest != null) && (fileScopeRequest.isDone()))
            {
                ast.reconnectDefinitions((ASFileScope)fileScopeRequest.get().getScopes()[0]);
            }
            else
            {
                getProject().clearScopeCacheForCompilationUnit(this);
                ast.runPostProcess(EnumSet.of(PostProcessStep.POPULATE_SCOPE));
                if (isBindable)
                    pkg.getASScope().addImport("flash.events.EventDispatcher");
            }
            final ImmutableSet<String> includedFiles = ast.getIncludeHandler().getIncludedFiles();
            addScopeToProjectScope(new ASFileScope[] { ast.getFileScope() });
            ast.parseRequiredFunctionBodies();
            final Collection<ICompilerProblem> problemCollection = ast.getProblems();
            ASSyntaxTreeRequestResult result = new ASSyntaxTreeRequestResult(this, syntaxTreeRequest, ast, includedFiles, ast.getIncludeTreeLastModified(), problemCollection);
            getProject().getWorkspace().addIncludedFilesToCompilationUnit(this, result.getIncludedFiles());
            return result;
        }
        finally
        {
            stopProfile(Operation.GET_SYNTAX_TREE);
        }
    }
    
    @Override
    protected IFileScopeRequestResult handleFileScopeRequest() throws InterruptedException
    {
        startProfile(Operation.GET_FILESCOPE);

        // Get the AST dig out the symbol table.
        final FileNode ast = (FileNode)getSyntaxTreeRequest().get().getAST();
        final IASScope scope = ast.getScope();
        assert scope instanceof ASFileScope : "Expect ASFileScope as the top-level scope, but found " + scope.getClass();
        
        IFileSpecification rootSource = getRootFileSpecification();
        
        final ASFileScopeRequestResult result =
            new ASFileScopeRequestResult(getDefinitionPromises(), getDefinitionPriority(),
                Collections.<ICompilerProblem>emptyList(), (ASFileScope)scope, rootSource);
        stopProfile(Operation.GET_FILESCOPE);

        return result;
    }

    @Override
    protected IABCBytesRequestResult handleABCBytesRequest() throws InterruptedException
    {
        final ISyntaxTreeRequestResult fsr = getSyntaxTreeRequest().get();
        final IASNode rootNode = fsr.getAST();
        final CompilerProject project = getProject();

        startProfile(Operation.GET_ABC_BYTES);
        IABCBytesRequestResult result = CodeGeneratorManager.getCodeGenerator().generate(project.getWorkspace().getExecutorService(),
                project.getUseParallelCodeGeneration(),
                this.getFilenameNoPath(),
                rootNode,
                this.getProject(),
                this.isInvisible(),
                this.getEncodedDebugFiles());
        stopProfile(Operation.GET_ABC_BYTES);

        return result;
    }

    private static Comparator<IDefinition> SCRIPT_NAME_DEFINITION_COMPARATOR =
        new Comparator<IDefinition>()
        {

            @Override
            public int compare(IDefinition arg0, IDefinition arg1)
            {
                int result = arg0.getAbsoluteStart() - arg1.getAbsoluteEnd();
                if (result != 0)
                    return result;
                result = arg0.getQualifiedName().compareTo(arg1.getQualifiedName());
                return result;
            }
        
        };
    
    @Override
    protected ISWFTagsRequestResult handleSWFTagsRequest() throws InterruptedException
    {
        final IABCBytesRequestResult abc = getABCBytesRequest().get();

        startProfile(Operation.GET_SWF_TAGS);

        try
        {
            final String tagName;
            if (Strings.isNullOrEmpty(qname))
            {
                final IFileScopeRequestResult fileScopeRR = getFileScopeRequest().get();

                final Collection<IDefinition> externallyVisibleDefinitions =
                        fileScopeRR.getExternallyVisibleDefinitions();
                if (!externallyVisibleDefinitions.isEmpty())
                {
                    ArrayList<IDefinition> sortedDefinitions =
                            new ArrayList<IDefinition>(externallyVisibleDefinitions.size());
                    Iterables.addAll(sortedDefinitions, externallyVisibleDefinitions);
                    Collections.sort(sortedDefinitions, SCRIPT_NAME_DEFINITION_COMPARATOR);
                    tagName = sortedDefinitions.get(0).getQualifiedName().replace('.', '/');
                }
                else
                {
                    tagName = getName();
                }
            }
            else
            {
                tagName = qname.replace('.', '/');
            }

            return new SWFTagsRequestResult(abc.getABCBytes(), tagName, abc.getEmbeds());
        }
        finally
        {
            stopProfile(Operation.GET_SWF_TAGS);
        }
    }

    @Override
    protected IOutgoingDependenciesRequestResult handleOutgoingDependenciesRequest () throws InterruptedException
    {
        final ISyntaxTreeRequestResult fsr = getSyntaxTreeRequest().get();
        final FileNode fn = (FileNode)fsr.getAST();

        startParsingImports(fn);

        startProfile(Operation.GET_SEMANTIC_PROBLEMS);

        Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

        updateEmbedCompilationUnitDependencies(fn.getEmbedNodes(), problems);

        getABCBytesRequest().get();

        IOutgoingDependenciesRequestResult result = new IOutgoingDependenciesRequestResult()
        {
            @Override
            public ICompilerProblem[] getProblems()
            {
                return IOutgoingDependenciesRequestResult.NO_PROBLEMS;
            }
        };
        stopProfile(Operation.GET_SEMANTIC_PROBLEMS);

        return result;
    }

    @Override
    protected void removeAST()
    {
        // This is purely an optimization.  If we don't remove the tree we could have that is not
        // the end of the world, we'll just pin the tree for longer than we'd like.
        // We are attempting to remove all hard references to the AST after codege and
        // semantic analysis are complete.
        IRequest<ISyntaxTreeRequestResult, ICompilationUnit> syntaxTreeRequest = this.syntaxTreeRequest.get();
        boolean canRemoveAST = operationsCompleted(EnumSet.of(ICompilationUnit.Operation.GET_SEMANTIC_PROBLEMS, ICompilationUnit.Operation.GET_ABC_BYTES));
        if (canRemoveAST)
        {
            try
            {
                assert syntaxTreeRequest != null;
                ((ASSyntaxTreeRequestResult)syntaxTreeRequest.get()).dropASTRef();
            }
            catch (InterruptedException e)
            {
                assert false : "Syntax should have already been built";
            }
        }
    }

    /**
     * TODO: Replace this with proper API call on CompilationUnit to get the
     * root class name.
     * 
     * @param node node to search for class node
     * @return root class name
     */
    private ClassNode findFirstClassNode(IASNode node)
    {
        ClassNode classNode = null;
        if (node instanceof ClassNode)
        {
            classNode = (ClassNode)node;
        }
        else
        {
            for (int i = 0; i < node.getChildCount(); i++)
            {
                classNode = findFirstClassNode(node.getChild(i));
                if (classNode != null)
                {
                    break;
                }
            }
        }
        return classNode;
    }

    /**
     * Get the root class name if defined.
     * 
     * @return root class name
     */
    public String getRootClassName() throws InterruptedException
    {
        final IASNode fileNode = getSyntaxTreeRequest().get().getAST();
        final ClassNode classNode = findFirstClassNode(fileNode);
        final String rootClassName = classNode.getName();
        return rootClassName;
    }
}
