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

package org.apache.royale.compiler.internal.scopes;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ScopedDefinitionBase;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.SWCFileScopeProvider.SWCFileScope;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;
import org.apache.royale.compiler.scopes.IFileScope;
import org.apache.royale.utils.FastStack;

/**
 * IASScope implementation for FileNodes. Conceptually, the file scope contains
 * all top-level definitions in the file. Any top-level definitions that don't
 * reside in packages are in the scope itself (see ASScope). This scope's
 * implementations of findDefinitionByName, findAllDefinitionsByName, and
 * findAllDefinitions also look for definitions in package scopes within the
 * file. That way, we use the versions of these classes in the (possibly
 * unsaved) file, rather than the versions that are stored in the project scope.
 */
public class ASFileScope extends ASScope implements IFileScope
{
    private static Collection<String> IMPLICIT_IMPORTS_FOR_AS =
        Collections.singleton(IASLanguageConstants.Vector_qname);
    
    /**
     * Returns the implicit imports for an ActionScript file.
     * <p>
     * For AS3, there is a single implicit import, <code>"__AS3__.vec.Vector"</code>.
     * 
     * @return A collection of Strings for the implicit imports.
     */
    public static Collection<String> getImplicitImportsForAS()
    {
        return IMPLICIT_IMPORTS_FOR_AS;
    }
    
    /**
     * the path of the file this file scope represents
     */
    protected String filePath;

    /**
     * A flag indicating whether this is an editable file (AS/MXML) or not
     * (SWC).
     */
    protected Boolean isEditableFile;

    private final NamespaceDefinition.IFilePrivateNamespaceDefinition filePrivateNamespace;

    private final IFileSpecificationGetter fileSpecGetter;

    protected WeakReference<ICompilationUnit> compilationUnitReference;
    
    /**
     * The file scope needs to keep track or the parsed function bodies to pin the
     * FileNode until those bodies are discarded again.  If the file node is not pinned
     * when function bodies are parsed, the FileNode can be gc'd, and reparsed, which
     * will then reconnect the new FileNode to this FileScope.  This leaves the scopes
     * in a bad state, as the parse/discard function bodies modify the scope state
     * Found in CMP-2238.
     */
    private ConcurrentHashMap<FunctionNode, Object> parsedFunctionBodies;

    /**
     * Lookup filename and local offset from the absolute offset.
     */
    private OffsetLookup offsetLookup;

    /**
     * Constructor
     * 
     * @param fileSpecGetter {@link IFileSpecificationGetter} that is used to
     * open files associated with this {@link ASFileScope}.
     * @param filePath Normalized absolute name of the file for which this
     * {@link ASFileScope} is being created.
     */
    public ASFileScope(IFileSpecificationGetter fileSpecGetter, String filePath)
    {
        super(null);
        this.filePath = filePath;
        String uri = "FilePrivateNS";
        filePrivateNamespace = NamespaceDefinition.createFilePrivateNamespaceDefinition(uri);
        this.fileSpecGetter = fileSpecGetter;
        this.compilationUnitReference = null;
        addImplicitImportsForAS();
        this.parsedFunctionBodies = new ConcurrentHashMap<FunctionNode, Object>();
    }

    /**
     * Constructor
     * 
     * @param fileNode file node whose scope this represents
     */
    public ASFileScope(FileNode fileNode)
    {
        super(null, fileNode);
        filePath = fileNode.getSourcePath();
        final String uri = "FilePrivateNS:" + FilenameUtils.getBaseName(filePath);
        filePrivateNamespace = NamespaceDefinition.createFilePrivateNamespaceDefinition(uri);
        fileSpecGetter = fileNode.getFileSpecificaitonGetter();
		this.compilationUnitReference = null;
        addImplicitImportsForAS();
        this.parsedFunctionBodies = new ConcurrentHashMap<FunctionNode, Object>();
    }
 
    /**
     * Adds the appropriate implicit imports for ActionScript.
     */
    private void addImplicitImportsForAS()
    {
        for (String implicitImport : ASFileScope.getImplicitImportsForAS())
        {
            addImport(implicitImport);
        }       
        
    }

    /**
     * Determine whether or not this is an editable file. This will check the
     * attached FileNode to see if it represents a SWC file (as opposed to an AS
     * or MXML file). If so, then we know not to prefer in-file definitions to
     * those in the project scope.
     * 
     * @return true if the file is editable
     */
    protected boolean isEditableFile()
    {
        if (isEditableFile == null)
        {
            boolean isEditable = true;
            if (filePath != null &&
                    filePath.endsWith(".swc"))
                isEditable = false;
            isEditableFile = isEditable;
        }
        return isEditableFile.booleanValue();
    }

    /**
     * For debugging only.
     */
    @Override
    protected String toStringHeader()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toStringHeader());

        if (filePath != null)
        {
            sb.append(" for \"");
            sb.append(filePath);
            sb.append("\"");
        }

        return sb.toString();
    }

    /**
     * Determine if p is the name of a package - at File Scope it checks
     * declared package names in addition to package names introduced via import
     * 
     * @param p the string to test
     * @return true it p is a known package name
     */
    @Override
    public boolean isPackageName(String p)
    {
        // Check the imports
        if (super.isPackageName(p))
            return true;

        // Just get the def set and iterate over the result looking for a package.
        // Common cases are the string isn't found, or the def set will contain
        // 1 definition so iterating over it isn't a problem.
        IDefinitionSet defSet = getLocalDefinitionSetByName(p);
        if (defSet != null)
        {
            int n = defSet.getSize();
            for (int i = 0; i < n; i++)
            {
                IDefinition def = defSet.getDefinition(i);
                if (def instanceof IPackageDefinition)
                {
                    IPackageDefinition packageDef = (IPackageDefinition)def;
                    if (p.equals(packageDef.getBaseName()))
                        return true;
                }
            }
        }

        return false;
    }

    @Override
    protected boolean namespaceSetSameAsContainingScopeNamespaceSet()
    {
        return false;
    }

    public NamespaceDefinition.ILanguageNamespaceDefinition getFilePrivateNamespaceReference()
    {
        return filePrivateNamespace;
    }

    @Override
    public IWorkspace getWorkspace()
    {
        return fileSpecGetter.getWorkspace();
    }

    /**
     * Resolve the source file path of the given definition QName. The QName
     * must be defined within this file scope.
     * <p>
     * If this {@code ASFileScope} comes from an ActionScript file, return the
     * ActionScript file path. File scopes coming from ABC or SWC files will
     * override this method to locate their source file path.
     * 
     * @param qName QName of a definition
     * @param project resolve the source path within this project
     * @return Source file path of this {@code ASFileScope}.
     */
    @Override
    public String getContainingSourcePath(final String qName, final ICompilerProject project)
    {
        return filePath;
    }

    @Override
    public String getContainingPath()
    {
        return filePath;
    }

    @Override
    public void collectExternallyVisibleDefinitions(Collection<IDefinition> defs, boolean includePrivateDefinitions)
    {
        FastStack<ASScope> scopesToVisit = new FastStack<ASScope>();
        scopesToVisit.push(this);
        while (scopesToVisit.size() > 0)
        {
            ASScope currentScope = scopesToVisit.pop();
            for (IDefinitionSet defSet : currentScope.getAllLocalDefinitionSets())
            {
                int n = defSet.getSize();
                for (int i = 0; i < n; i++)
                {
                    IDefinition def = defSet.getDefinition(i);
                    if (def instanceof IPackageDefinition)
                    {
                        IPackageDefinition packageDef = (IPackageDefinition)def;
                        ASScope packageScope = (ASScope)packageDef.getContainedScope();
                        scopesToVisit.push(packageScope);
                    }
                    else if ((def.getContainingScope() == currentScope) &&
                             (includePrivateDefinitions || def.getNamespaceReference().isPublicOrInternalNamespace()))
                    {
                        defs.add(def);
                    }
                }
            }
        }
    }

    @Override
    public void addImplicitOpenNamespaces(CompilerProject compilerProject, Set<INamespaceDefinition> result)
    {
        result.add(getFilePrivateNamespaceReference());
        compilerProject.addGlobalUsedNamespacesToNamespaceSet(result);
    }

    /**
     * Re-parse the file this scope was built for.
     * 
     * @return The root {@link IASNode} of the syntax tree.
     */
    public IASNode reparseFile() throws InterruptedException
    {
        return getCompilationUnit().getSyntaxTreeRequest().get().getAST();
    }

    /**
     * Set the {@code OffsetLookup} utility.
     * 
     * @param offsetLookup offset lookup utility
     */
    public void setOffsetLookup(OffsetLookup offsetLookup)
    {
        assert offsetLookup != null : "Offset lookup can't be null";
        this.offsetLookup = offsetLookup;
    }

    /**
     * @return Offset lookup utility for this file scope.
     */
    public OffsetLookup getOffsetLookup()
    {
        return offsetLookup;
    }

    @Override
    public final ScopedDefinitionBase getContainingDefinition()
    {
        return null;
    }

    @Override
    public boolean isSWC()
    {
        return false;
    }

    /**
     * Get the IASNode this FileScope came from.  This will reparse the file if necessary
     * to rebuild the IASNode.
     * If this FileScope did not come from a source file, this will return null.
     * @return  The IASNode that produced this FileScope, or null if this Scope did not come from
     *          a source file
     */
    public IASNode getNode()
    {
        IASNode node = null;
            try
            {
                    node = reparseFile();
            }
            catch (InterruptedException ie)
            {
                node = null;
            }

        return node;
    }

    /**
     * Set the {@link ICompilationUnit} related to this {@link ASFileScope}.
     * This class is overridden by SWCFileScope which will never set the
     * project reference, as SWC scopes are shared across projects.
     * 
     * @param compilationUnit A compilation unit.
     * @return true if the {@link ICompilationUnit} was stored, false if not
     * stored.  This happens when trying to store a SWCCompilationUnit.
     */
    public boolean setCompilationUnit(ICompilationUnit compilationUnit)
    {
        if (compilationUnit == null)
        {
            compilationUnitReference = null;
            return true;
        }

        compilationUnitReference = new WeakReference<ICompilationUnit>(compilationUnit);
        return true;
    }

    /**
     * Get the {@link ICompilationUnit} related to this {@link ASFileScope}.
     * SWCFileScope will override this class.
     * 
     * Note that this class should only ever be called from ASProjectScope or CompilerProject.
     * Clients should only ever use ASProjectScope.getCompilationUnitForScope()
     * 
     * @return the {@link ICompilationUnit}.  May be null when a {@link SWCFileScope}
     */
    public ICompilationUnit getCompilationUnit()
    {
        if (compilationUnitReference == null)
            return null;

        return compilationUnitReference.get();
    }

    /**
     * Add a function node the the collection of function nodes which
     * have been parsed
     * 
     * @param functionNode the FunctionNode to add
     */
    public void addParsedFunctionBodies(FunctionNode functionNode)
    {
        parsedFunctionBodies.put(functionNode, Object.class);
    }

    /**
     * Remove a function node the the collection of function nodes which
     * have been parsed, when the body is discarded
     * 
     * @param functionNode the FunctionNode to add
     */
    public void removeParsedFunctionBodies(FunctionNode functionNode)
    {
        parsedFunctionBodies.remove(functionNode);
    }

    public void addDependencyOnBuiltinType(ICompilerProject project, IASLanguageConstants.BuiltinType builtinType,
                                           DependencyType dependencyType)
    {
        ASScopeCache cache = ((CompilerProject)project).getCacheForScope(this);

        cache.addDependencyOnBuiltinType(builtinType, dependencyType);
    }
}
