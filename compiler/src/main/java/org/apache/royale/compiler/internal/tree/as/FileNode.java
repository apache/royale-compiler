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

package org.apache.royale.compiler.internal.tree.as;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.common.RecursionGuard;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.parsing.as.IncludeHandler;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.targets.ITargetAttributes;
import org.apache.royale.compiler.internal.targets.TargetAttributesMetadata;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.utils.FilenameNormalization;

/**
 * ActionScript parse tree node representing a file
 */
public class FileNode extends ScopedBlockNode implements Serializable, IFileNode, IFileNodeAccumulator
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Private constructor.
     * 
     * @param fileSpecGetter The {@link IFileSpecificationGetter} to be
     * used to open included files while parsing the file for this file node.
     * @param fileSpec The {@link IFileSpecification} of the file
     * for this file node.
     */
    private FileNode(IFileSpecificationGetter fileSpecGetter, IFileSpecification fileSpec)
    {
        super();

        this.fileSpecGetter = fileSpecGetter;
        this.fileSpec = fileSpec;
        
        setSourcePath(fileSpec.getPath());
        
        includeHandler = new IncludeHandler(fileSpecGetter);
        embedNodes = new LinkedList<IEmbedResolver>();
        requiredResourceBundles = new HashSet<String>();
        deferredFunctionNodes = new HashSet<FunctionNode>();
        
        // Create the implicit import nodes.
        // (Currently there is only one, for __AS3__.vec.Vector.)
        // These are not considered children of this file node,
        // but they have this file node as their parent.
        implicitImportNodes = new LinkedList<IImportNode>();
        for (String implicitImport : ASFileScope.getImplicitImportsForAS())
        {
            ImportNode implicitImportNode = ImportNode.buildImportNode(implicitImport);
            implicitImportNode.setParent(this);
            implicitImportNodes.add(implicitImportNode);
        }
       
        // Initialize the list of all import nodes to the implicit import nodes.
        // The parser will add the explicit import nodes within the file.
        importNodes = new LinkedList<IImportNode>();
        for (IImportNode implicitImportNode : implicitImportNodes)
        {
            importNodes.add(implicitImportNode);
        }
    }
        
    /**
     * Constructor for a {@link FileNode} that has a source file
     * associated with it.
     * 
     * @param fileSpecGetter The {@link IFileSpecificationGetter} to be
     * used to open included files while parsing the file for this file node.
     * @param pathName The path name of the file for this file node.
     */
    public FileNode(IFileSpecificationGetter fileSpecGetter, String pathName)
    {
        this(fileSpecGetter, fileSpecGetter.getFileSpecification(pathName));
    }

    /**
     * Constructor for a {@link FileNode} that does not have a source file
     * associated with it.
     * 
     * @param fileSpecGetter The {@link IFileSpecificationGetter} to be
     * used to open included files while parsing this file node.
     */
    public FileNode(IFileSpecificationGetter fileSpecGetter)
    {
        this(fileSpecGetter, FilenameNormalization.normalize(""));
    }

    private IFileSpecification fileSpec;

    private final IFileSpecificationGetter fileSpecGetter;

    private Collection<ICompilerProblem> problems;

    private final IncludeHandler includeHandler;
    
    /**
     * {@code StreamingASTokenizer} and {@code IncludeHandler} generates data
     * needed to do offset lookup. The data is encapsulated in an
     * {@code OffsetLookup} object. It is parked on this {@code FileNode} so
     * that it can be later passed into the {@code ASFileScope}.
     */
    private OffsetLookup offsetLookup;

    /**
     * A list of the implicit imports for each ActionScript file.
     * Currently there is only one, for the __AS3__vec.Vector class.
     */
    private final List<IImportNode> implicitImportNodes;

    /**
     * A list of every import node (both implicit and explicit) within this file.
     * It is initialized to the one implicit import node for __AS3__.vec.Vector.
     * Then as importDirective() in ASParser builds each import node,
     * it adds it to this list.
     */
    private final List<IImportNode> importNodes;

    private final List<IEmbedResolver> embedNodes;

    private ITargetAttributes targetAttributes;

    private final Set<String> requiredResourceBundles;

    /**
     * A convenient collection of all {@code FunctionNode}s whose body nodes
     * are deferred.
     */
    private final Set<FunctionNode> deferredFunctionNodes;
    
    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.FileID;
    }
    
    @Override
    public IFileSpecification getFileSpecification()
    {
        return fileSpec;
    }

    @Override
    public IWorkspace getWorkspace()
    {
        return fileSpecGetter.getWorkspace();
    }
    
    @Override
    public void setParent(NodeBase parent)
    {
        super.setParent(parent);
        
        if (parent != null)
            connectedToProjectScope();
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {
            assert scope == null;
            // A FileNode creates a parentless ASFileScope,
            // which then gets passed down as the current scope.
            initializeScope(scope);
            scope = getASScope();
        }

        if (set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            assert scope != null;
            assert scope instanceof ASFileScope;
            reconnectScope(scope);
        }

        super.analyze(set, scope, problems);
    }
    
    @Override
    public void collectImportNodes(Collection<IImportNode> importNodes)
    {
        // Collect the explicit import nodes.
        super.collectImportNodes(importNodes);
        
        collectImplicitImportNodes(importNodes);
    }

    /**
     * Collect only the nodes for the implicit Imports.
     * @param importNodes   The Collection to add the implicit import nodes to
     */
    void collectImplicitImportNodes(Collection<IImportNode> importNodes)
    {
        // Add the implicit import nodes.
        importNodes.addAll(implicitImportNodes);
    }
    
    //
    // IFileNode implementations
    //
    
    @Override
    public OffsetLookup getOffsetLookup()
    {
        return this.offsetLookup;
    }

    @Override
    public boolean hasIncludes()
    {
        return getFileScope().getOffsetLookup().hasIncludes();
    }

    @Override
    public long getIncludeTreeLastModified()
    {
        return getIncludeHandler().getLastModified();
    }
    
    @Override
    public IDefinitionNode[] getTopLevelDefinitionNodes(boolean includeDefinitionsOutsideOfPackage,
                                                        boolean includeNonPublicDefinitions)
    {
        List<IDefinitionNode> list = new ArrayList<IDefinitionNode>();
        
        // Check children of this FileNode that might be definition nodes.
        int n = getChildCount();
        for (int i = 0; i < n; i++)
        {
            IASNode child = getChild(i);
            
            if (child instanceof IPackageNode)
            {
                IScopedNode packageBodyNode = ((IPackageNode)child).getScopedNode();

                // Check children of the package body node that might be definition nodes.
                int m = packageBodyNode.getChildCount();
                for (int j = 0; j < m; j++)
                {
                    IASNode packageBodyChild = packageBodyNode.getChild(j);
                    addIfDefinitionNode(packageBodyChild, includeNonPublicDefinitions, list);
                }
            }
            else if (includeDefinitionsOutsideOfPackage)
            {
                addIfDefinitionNode(child, includeNonPublicDefinitions, list);
            }
        }
        
        return list.toArray(new IDefinitionNode[0]);
    }
    
    @Override
    public IDefinition[] getTopLevelDefinitions(boolean includeDefinitionsOutsideOfPackage,
                                                boolean includeNonPublicDefinitions)
    {
        IDefinitionNode[] nodes = getTopLevelDefinitionNodes(
            includeDefinitionsOutsideOfPackage, includeNonPublicDefinitions);
        int n = nodes.length;
        
        IDefinition[] definitions = new IDefinition[n];
        for (int i = 0; i < n; i++)
        {
            definitions[i] = nodes[i].getDefinition();
        }
        return definitions;
    }
    
    @Override
    public ITargetAttributes getTargetAttributes(ICompilerProject project)
    {
        if (targetAttributes == null)
        {
            final IDefinitionNode[] definitionNodes = getTopLevelDefinitionNodes(false, false);
            if (definitionNodes.length == 1 && definitionNodes[0] instanceof IClassNode)
            {
                final IClassNode classNode = (IClassNode)definitionNodes[0];
                final IMetaTagNode[] metaTagSWF = classNode.getMetaTagNodesByName("SWF");
                if (metaTagSWF != null && metaTagSWF.length > 0)
                {
                    if (metaTagSWF.length > 1)
                        throw new IllegalStateException("Only allow one [SWF] metadata tag.");
                    targetAttributes = new TargetAttributesMetadata(metaTagSWF[0]);
                }
            }
        }
        
        return targetAttributes;
    }

    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }

    //
    // IFileNodeAccumulator implementations
    //
    
    @Override
    public void addImportNode(IImportNode node)
    {
        // don't add imports inside a function body to the list of import
        // nodes, as the import node list shouldn't be modified once the
        // FileNode has been created, but deferred function body parsing
        // happens later, so skip this case.
        if (node.getAncestorOfType(FunctionNode.class) != null)
            return;

        importNodes.add(node);
    }

    @Override
    public List<IImportNode> getImportNodes()
    {
        return importNodes;
    }
    
    @Override
    public void addEmbedNode(IEmbedResolver node)
    {
        embedNodes.add(node);
    }

    @Override
    public List<IEmbedResolver> getEmbedNodes()
    {
        return embedNodes;
    }

    @Override
    public void addRequiredResourceBundle(String bundleName)
    {
        requiredResourceBundles.add(bundleName);
    }

    @Override
    public Set<String> getRequiredResourceBundles()
    {
        return requiredResourceBundles;
    }
    

    @Override
    public void addDeferredFunctionNode(FunctionNode functionNode)
    {
        assert functionNode != null : "Function node can't be null.";
        this.deferredFunctionNodes.add(functionNode);
    }

    @Override
    public synchronized void populateFunctionNodes()
    {
        for (final FunctionNode fn : deferredFunctionNodes)
        {
            fn.parseFunctionBody(problems);
        }
    }

    /**
     * Run through all the deferredFunctionNodes, and if their containing
     * scope isn't file, package or class, it needs to be parsed
     */
    public synchronized void parseRequiredFunctionBodies()
    {
        Iterator<FunctionNode> iter = deferredFunctionNodes.iterator();

        while (iter.hasNext())
        {
            FunctionNode fn = iter.next();

            // don't parse, but do remove from the deferred list functions
            // which are inside a disabled config block, as they can't be
            // parsed properly later, and they are never needed.
            if (isInsideDisabledConfigBlock(fn))
            {
                iter.remove();
            	continue;
            }

            IASNode functionParent = fn.getParent();
            if (functionParent == null || functionParent instanceof FileNode)
                continue;

            if (functionParent instanceof ScopedBlockNode || functionParent instanceof ContainerNode)
                functionParent = functionParent.getParent();

            // This shouldn't really ever be null, but for some reason it
            // is, so guard against it for now.
            if (functionParent == null)
                continue;

            switch (functionParent.getNodeID())
            {
                case ClassID:
                case PackageID:
                    break;
                default:
                    fn.parseFunctionBody(problems);
                    iter.remove();
            }
        }
    }

    private static boolean isInsideDisabledConfigBlock(FunctionNode fn)
    {
        ConfigConditionBlockNode configBlock = (ConfigConditionBlockNode)fn.getAncestorOfType(ConfigConditionBlockNode.class);
        if (configBlock != null && configBlock.getChildCount() == 0)
            return true;

        return false;
    }

    //
    // Other methods
    //
    
    /**
     * Gets the {@link IFileSpecificationGetter} for that was used to open
     * included files when this {@link FileNode} was parsed.
     * 
     * @return The {@link IFileSpecificationGetter} for that was used to open
     * included files when this {@link FileNode} was parsed.
     */
    // TODO Fix type in name
    public IFileSpecificationGetter getFileSpecificaitonGetter()
    {
        return fileSpecGetter;
    }

    public void setOffsetLookup(final OffsetLookup offsetLookup)
    {
        assert offsetLookup != null : "OffsetLookup can't be null.";
        this.offsetLookup = offsetLookup;
    }

    /**
     * Gets the {@link IncludeHandler} for this file node. The
     * {@link IncludeHandler} can be used to get a list of files included by
     * this file.
     * 
     * @return The {@link IncludeHandler} for this file node.
     */
    public IncludeHandler getIncludeHandler()
    {
        return this.includeHandler;
    }

    /**
     * Retrieve the temporary enclosing scope (which is used to make an included
     * file look like it's inside the scope where the include statement lives.
     * See Project.connectToIncluder for details.
     * 
     * @return the temporary enclosing scope
     */
    // TODO Does this need 'synchronized'?
    public synchronized IASScope getTemporaryEnclosingScope(RecursionGuard scopeGuard)
    {
        // Leaving this method here until we deal with includes properly.
        // If we find the old connected scopes thing will work for real, then
        // we can use version control to bring back all the code that was here.
        // In the mean time nobody has a "connected" scope and thus
        // nobody will have a temporary enclosing scope.
        return null;
    }

    public void processAST(EnumSet<PostProcessStep> features)
    {
        //START DEBUG HERE FOR SCOPE POPULATION
        Collection<ICompilerProblem> problems = runPostProcess(features);
        if (problems != null)
        {
            if (this.problems == null)
                this.problems = new ArrayList<ICompilerProblem>();
            this.problems.addAll(problems);
        }
    }

    public void reconnectDefinitions(ASFileScope fileScope)
    {
        this.scope = fileScope;
        Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        analyze(EnumSet.of(PostProcessStep.RECONNECT_DEFINITIONS), scope, problems);
    }

    protected void initializeScope(ASScope containingScope)
    {
        //containing scope will always be null here
        final ASFileScope fileScope = new ASFileScope(this);
        if (offsetLookup != null)
            fileScope.setOffsetLookup(offsetLookup);
        scope = fileScope;
    }

    public void setProblems(Collection<ICompilerProblem> problems)
    {
        if (this.problems != null)
            this.problems.addAll(problems);
        else
            this.problems = problems;
    }

    public void addProblem(ICompilerProblem problem)
    {
        if (problems == null)
            problems = new ArrayList<ICompilerProblem>();
        
        problems.add(problem);
    }
    
    /*
     * Helper method for geAllToplevelDefinitionNodes()
     */
    private void addIfDefinitionNode(IASNode node, boolean includeNonPublicDefinitions,
                                     List<IDefinitionNode> list)
    {
        if (!(node instanceof IDefinitionNode))
            return;

        IDefinitionNode definitionNode = (IDefinitionNode)node;
        
        if (!includeNonPublicDefinitions)
        {
            String namespace = definitionNode.getNamespace();
            if (!INamespaceConstants.public_.equals(namespace))
                return;
        }
        
        list.add(definitionNode);
    }
}
