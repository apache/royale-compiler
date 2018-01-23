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

package org.apache.royale.compiler.internal.targets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.SourcePathManager;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.NoCompilationUnitForDefinitionProblem;
import org.apache.royale.compiler.problems.NoSourceForClassInNamespaceProblem;
import org.apache.royale.compiler.problems.NoSourceForClassProblem;
import org.apache.royale.compiler.targets.IJSTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class RoyaleSWCTarget extends JSTarget implements IJSTarget
{
    protected ICompilationUnit mainCU;
    protected RootedCompilationUnits rootedCompilationUnits;

    /**
     * Initialize a JS target with the owner project and root compilation units.
     * 
     * @param project the owner project
     */
    public RoyaleSWCTarget(RoyaleJSProject project, ITargetSettings targetSettings,
            ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
        royaleProject = (RoyaleJSProject)project;
    }

    private RoyaleJSProject royaleProject;
    
    @Override
    protected Target.RootedCompilationUnits computeRootedCompilationUnits() throws InterruptedException
    {
        final Set<ICompilationUnit> rootCompilationUnits = new HashSet<ICompilationUnit>();

        final Collection<File> includedSourceFiles = targetSettings.getIncludeSources();
        final Set<String> includeClassNameSet = ImmutableSet.copyOf(targetSettings.getIncludeClasses());
        final Set<String> includedNamespaces = ImmutableSet.copyOf(targetSettings.getIncludeNamespaces());

        final ArrayList<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        
        // Select definitions according to configurations.

        // include-namespace
        final Collection<ICompilationUnit> includeNamespaceUnits = 
            getCompilationUnitsForIncludedNamespaces(includedNamespaces, problems); 
        rootCompilationUnits.addAll(includeNamespaceUnits);
        
        // include-class + include-namespace
        rootCompilationUnits.addAll(getCompilationUnitsFromClassNames(null, includeClassNameSet, problems));

        // include-source
        for (final File includedSourceFileName : includedSourceFiles)
        {
            // Get all the compilation units in the project that reference the specified file.
            Collection<ICompilationUnit> compilationUnitsForFile = project.getWorkspace().getCompilationUnits(includedSourceFileName.getAbsolutePath(), project);
            
            // For compilation units with that differ by qname, choose the compilation that
            // appears first on the source-path.
            if (compilationUnitsForFile.size() > 1)
            {
                compilationUnitsForFile = filterUnitsBasedOnSourcePath(compilationUnitsForFile);
            }
            
            for (ICompilationUnit cu : compilationUnitsForFile)
            {
                // IFilter out any compilation unit in the list where the specified file is not the root
                // source file compiled by the compilation unit.
                if (cu.getAbsoluteFilename().equals(includedSourceFileName.getAbsolutePath()))
                    rootCompilationUnits.add(cu);
            }
        }

        //Add compilation units for included resource bundles
        for (ICompilationUnit rbCompUnit : getIncludedResourceBundlesCompilationUnits(problems))
            rootCompilationUnits.add(rbCompUnit);

        // -include and -include-libraries
        rootCompilationUnits.addAll(getIncludesCompilationUnits());
        rootCompilationUnits.addAll(getIncludeLibrariesCompilationUnits());
        
        return new Target.RootedCompilationUnits(rootCompilationUnits, problems);
    }

    /**
     * For compilation units with the same absolute source path, filter based on
     * the source path. The compilation unit found on the highest priority
     * source path wins. The rest of the compilation units with qnames are
     * discared. If a unit is not on the source path or does not have a qname or
     * more than one qname, then let it thru the filter.
     * 
     * @param compilationUnitsForFile list of compilation units to filter.
     * @return filtered compilation units.
     * @throws InterruptedException
     */
    private Collection<ICompilationUnit> filterUnitsBasedOnSourcePath(Collection<ICompilationUnit> compilationUnitsForFile) throws InterruptedException
    {
        List<ICompilationUnit> sourcePathUnits = new ArrayList<ICompilationUnit>(compilationUnitsForFile);
        boolean foundHighestPriorityUnit = false;
        for (File sourcePath : royaleProject.getSourcePath())
        {
            for (ICompilationUnit unit : sourcePathUnits)
            {
                // We only care about filtering units on the source path
                // that follow the single definition rule.
                UnitType unitType = unit.getCompilationUnitType();
                if (unitType == UnitType.AS_UNIT || unitType == UnitType.FXG_UNIT ||
                    unitType == UnitType.MXML_UNIT || unitType == UnitType.CSS_UNIT)
                {
                    Collection<String> qnames = unit.getQualifiedNames();
                    if (qnames.size() > 1)
                        continue;
                    
                    String unitQname = qnames.isEmpty() ? "" : qnames.iterator().next();
                    String computedQname = SourcePathManager.computeQName(sourcePath, new File(unit.getAbsoluteFilename()));
                    
                    if (unitQname.equals(computedQname))
                    {
                        // We found a unit on the source path. Only keep the 
                        // first unit found on the source path and remove the 
                        // others.
                        if (foundHighestPriorityUnit)
                            compilationUnitsForFile.remove(unit);
                        
                        foundHighestPriorityUnit = true;
                        break; // should only be one compilation unit on a source path
                    }
                }
            }
        }
        
        return compilationUnitsForFile;
    }

    /**
     * Get the compilation units for the given included namespaces. Also perform error
     * checking.
     * 
     * @param namespaces the namespaces included in this swc target.
     * @param problems A collection where detected problems are added.
     * @return A collection of compilation units.
     * @throws InterruptedException 
     */
    private Collection<ICompilationUnit> getCompilationUnitsForIncludedNamespaces(
            Collection<String> namespaces,
            Collection<ICompilerProblem> problems) throws InterruptedException
    {
        final Collection<ICompilationUnit> allUnits = new HashSet<ICompilationUnit>();
        
        for (String namespace : namespaces)
        {
            // For each namespace get the set of classes. 
            // From the classes get the the compilation units.
            // Validate the compilation units are resolved to source
            // files unless there are lookupOnly entries.
            final Collection<String> includeNamespaceQualifiedNames =
                royaleProject.getQualifiedClassNamesForManifestNamespaces(
                        Collections.singleton(namespace));
            final Collection<ICompilationUnit> units = 
                getCompilationUnitsFromClassNames(namespace, includeNamespaceQualifiedNames, problems);
            validateIncludeNamespaceEntries(namespace, units, problems);
            allUnits.addAll(units);
        }
        return allUnits;
    }
    
    /**
     * Validate that the manifest entries in the included namespaces resolve to
     * source files, not classes from other SWCs. The exception is for entries
     * that are "lookupOnly".
     * 
     * @param namespace The target namespace.
     * @param units The compilation units found in that namespace.
     * @param problems detected problems are added to this list.
     * @throws InterruptedException 
     */
    private void validateIncludeNamespaceEntries(String namespace, 
            Collection<ICompilationUnit> units, 
            Collection<ICompilerProblem> problems) throws InterruptedException
    {
        for (ICompilationUnit unit : units)
        {
            List<String> classNames = unit.getQualifiedNames();
            String className = classNames.get(classNames.size() - 1);
            Collection<XMLName> xmlNames = royaleProject.getTagNamesForClass(className);
            for (XMLName xmlName : xmlNames)
            {
                if (namespace.equals(xmlName.getXMLNamespace()))
                {
                    if (!royaleProject.isManifestComponentLookupOnly(xmlName) && 
                        unit.getCompilationUnitType() == UnitType.SWC_UNIT)
                    {
                        problems.add(new NoSourceForClassInNamespaceProblem(namespace, className));
                    }
                    break;
                }
            }
        }
    }

    /**
     * Return a collection of compilation units for a collection of class names. 
     *
     * @param namespace the namespace of the classes. Null if there is no namespace.
     * @param classNames
     * @param problems detected problems are added to this list.
     * @return a collection of compilation units.
     */
    private Collection<ICompilationUnit> getCompilationUnitsFromClassNames(String namespace,
            Collection<String> classNames,
            final Collection<ICompilerProblem> problems)
    {
        Collection<String> compilableClassNames = new ArrayList<String>();
        for (String className : classNames)
        {
            Collection<XMLName> tagNames = royaleProject.getTagNamesForClass(className);
            boolean okToAdd = true;
            for (XMLName tagName : tagNames)
            {
                if (royaleProject.isManifestComponentLookupOnly(tagName))
                    okToAdd = false;
            }
            if (okToAdd)
                compilableClassNames.add(className);
        }
        
        // Class names are turned into references and then info compilation units.
        final Iterable<IResolvedQualifiersReference> references = 
            Iterables.transform(compilableClassNames, new Function<String, IResolvedQualifiersReference>()
                {
                    @Override
                    public IResolvedQualifiersReference apply(String qualifiedName)
                    {
                        return ReferenceFactory.packageQualifiedReference(project.getWorkspace(), qualifiedName, true);
                    }
                });

        Collection<ICompilationUnit> units = new LinkedList<ICompilationUnit>();
        for (IResolvedQualifiersReference reference : references)
        {
            IDefinition def = reference.resolve(royaleProject);
            if (def == null)
            {
                if (namespace == null)
                    problems.add(new NoSourceForClassProblem(reference.getDisplayString()));
                else
                    problems.add(new NoSourceForClassInNamespaceProblem(namespace, reference.getDisplayString()));
            }
            else
            {
                ICompilationUnit defCU = project.getScope().getCompilationUnitForDefinition(def);
                if (defCU == null)
                    problems.add(new NoCompilationUnitForDefinitionProblem(def.getBaseName()));
                else
                    units.add(defCU);
            }
        }

        return units;
    }


}
