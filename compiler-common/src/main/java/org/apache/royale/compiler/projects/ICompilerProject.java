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

package org.apache.royale.compiler.projects;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.targets.ISWFTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * All IProject implementations aggregate a project scope containing global
 * definitions, a set of CompilationUnits, and a graph of dependencies between
 * the CompilationUnits.
 */
public interface ICompilerProject
{
    /**
     * @return The workspace in which this project lives.
     */
    IWorkspace getWorkspace();
    
    /**
     * @return The project scope for the project. This scope object contains all
     * the global definitions in the project.
     */
    IASScope getScope();

    /**
     * @return All the compilation units in the project.
     */
    Collection<ICompilationUnit> getCompilationUnits();

    /**
     * Get all compilation units from the filename
     * 
     * @param filename path to source file
     */
    Collection<ICompilationUnit> getCompilationUnits(String filename);

    /**
     * Get all compilation units from which the filename is included.
     * <p>
     * The returned collection does not include invisible compilation units.
     * 
     * @param filename path to source file
     */
    Collection<ICompilationUnit> getIncludingCompilationUnits(String filename);

    /**
     * Removes all data created during a compile
     */
    void clean();

    /**
     * Called before a project is deleted.  This method will remove all compilation
     * units associated with the project from the workspace, along with the project
     * itself from the workspace.  Once this method has been called, the project will
     * no longer be in a valid state.
     *
     * SWC caches which are shared across projects will not be invalidated.
     */
    void delete();

    /**
     * Create a SWF target
     * 
     * @param targetSettings The settings to use for this target
     * @param progressMonitor to collect progress information, can be <code>null</code>
     * @return The newly created ISWFTarget
     * @throws InterruptedException
     */
    ISWFTarget createSWFTarget(ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor) throws InterruptedException;

    /**
     * Given a set of roots node in the dependency graph, compute a list of
     * {@link ICompilationUnit}'s that are reachable from the specified roots.
     * The returned list of {@link ICompilationUnit}'s is in the order in which
     * the {@link ICompilationUnit}'s should be added to a SWF.
     * 
     * @param roots The roots of the dependency graph that is walked to compute
     * which CompilationUnits to include in the return value.
     * @return A list of {@link ICompilationUnit}'s reachable from the specified roots
     * in the order the {@link ICompilationUnit}'s should be added to a SWF.
     */
    List<ICompilationUnit> getReachableCompilationUnitsInSWFOrder(Collection<ICompilationUnit> roots);

    /**
     * Resolve a QName such as {@code mx.controls.Button} to a class definition.
     * 
     * @param qName Qualified name.
     * @return The {@link IDefinition} corresponds to the given QName; Null if
     * the name can't be resolved.
     */
    IDefinition resolveQNameToDefinition(final String qName);

    /**
     * Resolve a QName such as {@code mx.controls.Button} to the containing
     * ICompilationUnit
     * 
     * @param qName Qualified name.
     * @return The {@link ICompilationUnit} corresponds to the given QName; Null if
     * the name can't be resolved.
     */
    ICompilationUnit resolveQNameToCompilationUnit(final String qName);
    
    /**
     * Helper method to get some of the built-in types (*, String, Number, etc).  Looks right
     * in the project scope, so won't find any intervening definitions that could hide a built-in.
     */
    ITypeDefinition getBuiltinType(IASLanguageConstants.BuiltinType type);
    
    /**
     * Helper method to get the only built-in value (undefined).  Looks right
     * in the project scope, so won't find any intervening definitions that could hide a built-in.
     * @return the definition for the constant whose qname is "undefined".
     * Note that if we add more built-in values we will need to change this api to take an enum,
     * just like getBuiltinType().
     */
    IDefinition getUndefinedValue();

    /**
     * Test whether the project supports the embedding of assets.
     * 
     * @return true if embedding is supported, false otherwise
     */
    boolean isAssetEmbeddingSupported();
    
    /**
     * Enables or disables parallel code generation of method bodies.
     * 
     * @param useParallelCodeGeneration If true, method bodies will be generated
     * in parallel.
     */
    void setUseParallelCodeGeneration(boolean useParallelCodeGeneration);
    
    /**
     * Gets the set of compilation units depended on directly
     * by the specified compilation unit.
     * 
     * @param cu The {@link ICompilationUnit} whose set of direct dependencies
     * will be returned.
     * @return A new set of compilation units that the specified
     * compilation unit directly depends on.
     */
    Set<ICompilationUnit> getDirectDependencies(ICompilationUnit cu);

    /**
     * Gets the set of compilation units that directly depend on the specified compilation unit.
     * 
     * @param cu An {@link ICompilationUnit}.
     * @param types A {@link DependencyTypeSet} of dependency types used to filter
     * the returned set of compilation units.
     * @return A new set of compilation units that directly depend on the
     * specified compilation unit.
     */
    Set<ICompilationUnit> getDirectReverseDependencies(ICompilationUnit cu, DependencyTypeSet types);

	/**
     * Test whether the project supports function inlining
     * 
     * @return true if inlining enabled, false otherwise.
     */
    boolean isInliningEnabled();

    /**
     * Override this to permit package aliasing on imports and elsewhere
     * 
     * @param packageName The imported class
     */
    String getActualPackageName(String packageName);

    /**
     * Override this to do try harder to disambiguate between two ambiguous definitions
     * 
     * @param scope The current scope.
     * @param name Definition name.
     * @param def1 One possibility.
     * @param def2 The other possibility.
     * @return null if still ambiguous or else def1 or def2.
     */
    IDefinition doubleCheckAmbiguousDefinition(IASScope scope, String name, IDefinition def1, IDefinition def2);


    /**
     * @return All the problems collected so far.  
     * Add new ones here if you don't have another place to add them.
     */
    Collection<ICompilerProblem> getProblems();

    /**
     * @param problems All the problems collected so far.  
     * Set this to the main collection so parts of the compiler
     * can add a problem if they don't have another place to add them.
     */
    void setProblems(Collection<ICompilerProblem> problems);
    
    /**
     * @param functionDefinition 
     * @param overrideDefinition The definition overriding the base definition.  
     * @param baseDefinition The definition being overridden.  
     * @return True if compatible (default is if they are the same)
     */
    boolean isCompatibleOverrideReturnType(IFunctionDefinition functionDefinition, ITypeDefinition overrideDefinition, ITypeDefinition baseDefinition);
    
    /**
     * @param node The node being converted.
     * @param actualDefinition The actual definition.  
     * @param expectedDefinition The expected definition.
     * @param func The function being called.
     * @return True if valid
     */
    boolean isValidTypeConversion(IASNode node, IDefinition actualDefinition, IDefinition expectedDefinition, IFunctionDefinition func);

    /**
     * @param functionDefinition 
     * @param overrideDefinition The definition overriding the base definition.  
     * @param baseDefinition The definition being overridden.
     * @param zero-based index of parameter in list  
     * @return True if compatible (default is if they are the same)
     */
	boolean isCompatibleOverrideParameterType(
			IFunctionDefinition functionDefinition, ITypeDefinition type1,
			ITypeDefinition type2, int i);

    /**
     * @param functionDefinition 
     * @param formalCount The number of formal parameters.  
     * @param actualCount The number of actual parameters used in the call.
     * @return True if parameter count mismatch is allowed (because the transpiler will generate different code);
     */
	boolean isParameterCountMismatchAllowed(
			IFunctionDefinition functionDefinition, int formalCount,
			int actualCount);
	
    /**
     * @return True if a subclass can have a private API with the same name as a private API in its base classes.
     */
     boolean getAllowPrivateNameConflicts();
     
    /**
     * @return True if import aliases are allowed.
     */
     boolean getAllowImportAliases();
     
     /**
      * @return True if abstract classes are allowed.
      */
     boolean getAllowAbstractClasses();
     
     /**
      * @return True if private constructors are allowed.
      */
     boolean getAllowPrivateConstructors();
     
     /**
      * @return True if strict identifier naming is enforced.
      */
     boolean getStrictIdentifierNames();

}
