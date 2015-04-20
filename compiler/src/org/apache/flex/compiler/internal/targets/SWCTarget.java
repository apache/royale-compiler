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

package org.apache.flex.compiler.internal.targets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.common.DependencyTypeSet;
import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.common.VersionInfo;
import org.apache.flex.compiler.common.XMLName;
import org.apache.flex.compiler.constants.IMetaAttributeConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.flex.compiler.definitions.references.ReferenceFactory;
import org.apache.flex.compiler.exceptions.BuildCanceledException;
import org.apache.flex.compiler.filespecs.IBinaryFileSpecification;
import org.apache.flex.compiler.internal.config.QNameNormalization;
import org.apache.flex.compiler.internal.filespecs.SWCFileSpecification;
import org.apache.flex.compiler.internal.projects.DependencyGraph;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.LibraryPathManager;
import org.apache.flex.compiler.internal.projects.ResourceBundleSourceFileHandler;
import org.apache.flex.compiler.internal.projects.SourcePathManager;
import org.apache.flex.compiler.internal.units.ResourceBundleCompilationUnit;
import org.apache.flex.compiler.problems.DuplicateScriptProblem;
import org.apache.flex.compiler.problems.FileNotFoundProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.NoCompilationUnitForDefinitionProblem;
import org.apache.flex.compiler.problems.NoSourceForClassInNamespaceProblem;
import org.apache.flex.compiler.problems.NoSourceForClassProblem;
import org.apache.flex.compiler.targets.ISWCTarget;
import org.apache.flex.compiler.targets.ISWFTarget;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetReport;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.ICompilationUnit.UnitType;
import org.apache.flex.compiler.units.requests.IFileScopeRequestResult;
import org.apache.flex.swc.ISWC;
import org.apache.flex.swc.ISWCFileEntry;
import org.apache.flex.swc.ISWCLibrary;
import org.apache.flex.swc.ISWCManager;
import org.apache.flex.swc.ISWCVersion;
import org.apache.flex.swc.SWC;
import org.apache.flex.swc.SWCComponent;
import org.apache.flex.swc.SWCLibrary;
import org.apache.flex.swc.SWCScript;
import org.apache.flex.swf.ISWF;
import org.apache.flex.utils.DAByteArrayOutputStream;
import org.apache.flex.utils.FileID;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Compilation target for SWC library.
 */
public class SWCTarget extends Target implements ISWCTarget
{
    private static final String LIBRARY_SWF = "library.swf";

    public SWCTarget(final FlexProject project, ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor)
    {
        super(project, targetSettings, progressMonitor);
        swc = new SWC(targetSettings.getOutput());
        flexProject = project;
    }

    private final SWC swc;
    private final FlexProject flexProject;
    private ILibrarySWFTarget librarySWFTarget;
    
    private RootedCompilationUnits rootedCompilationUnits;

    @Override
    public ISWC build(final Collection<ICompilerProblem> problems)
    {
        buildStarted();
        try
        {
            Iterable<ICompilerProblem> fatalProblems = getFatalProblems();
            if (!Iterables.isEmpty(fatalProblems))
            {
                Iterables.addAll(problems, fatalProblems);
                return null;
            }

            setVersionInfo();
            final HashSet<IDefinition> definitions = new HashSet<IDefinition>();
            buildLibrarySWF(definitions, problems);
            addComponents(definitions);
            addFileEntriesToSWC(problems);

            return swc;
        }
        catch (BuildCanceledException bce)
        {
            return null;
        }
        catch (InterruptedException ie)
        {
            return null;
        }
        finally
        {
            buildFinished();
        }
    }
    
    /**
     * Sets version information to be written into the <versions> tag in catalog.xml.
     */
    private void setVersionInfo()
    {
        ISWCVersion swcVersion = swc.getVersion();
        swcVersion.setSWCVersion(VersionInfo.getLibVersion());
        if (flexProject.isFlex())
        {
            swcVersion.setFlexVersion(VersionInfo.getFlexVersion());
            swcVersion.setFlexBuild(VersionInfo.getBuild());
            swcVersion.setFlexMinSupportedVersion(targetSettings.getFlexMinimumSupportedVersion());
        }
        swcVersion.setCompilerName(VersionInfo.getCompilerName());
        swcVersion.setCompilerVersion(VersionInfo.getCompilerVersion());
        swcVersion.setCompilerBuild(VersionInfo.getCompilerBuild());
    }
    
    @Override
    public ISWFTarget getLibrarySWFTarget() throws InterruptedException
    {
        if (librarySWFTarget == null)
        {
            Target.RootedCompilationUnits rootedCompilationUnits = getRootedCompilationUnits();
            if (flexProject.isFlex())
            {
                librarySWFTarget =
                    new FlexLibrarySWFTarget(flexProject, targetSettings, rootedCompilationUnits.getUnits());                
            }
            else
            {
                librarySWFTarget = new LibrarySWFTarget(flexProject, targetSettings,
                        rootedCompilationUnits.getUnits());                
            }
        }
        assert librarySWFTarget != null;
        return librarySWFTarget;
    }

    @Override
    public TargetType getTargetType()
    {
        return TargetType.SWC;
    }

    /**
     * Creating {@code <component>} tags in catalog.xml.
     * 
     * @param definitions All definitions to be linked into the target.
     */
    private void addComponents(final HashSet<IDefinition> definitions)
    {
        final Set<String> includedNamespaces = ImmutableSet.copyOf(targetSettings.getIncludeNamespaces());
        
        for (final IDefinition def : definitions)
        {
            final String qName = def.getQualifiedName();
            
            final Collection<XMLName> tagNames = flexProject.getTagNamesForClass(qName);
            for (XMLName tagName : tagNames)
            {
                if (includeComponent(tagName, includedNamespaces))
                {
                    final SWCComponent component = new SWCComponent();
                    component.setName(tagName.getName());
                    component.setURI(tagName.getXMLNamespace());
                    component.setQName(qName);
                    swc.addComponent(component);                    
                }
            }
        }
    }

    /**
     * Test if a component should be included in a SWC's component list.
     * <p>
     * The rule for including a component in a SWC's component list is that
     * the component must be in one of the SWC's included namespaces
     * (meaning those specified by -include-namespace, not -namespace)
     * and either the component's lookupOnly flag is false
     * or the option -include-lookup-only is true.
     * 
     * @param tagName the tag name of the component to check.
     * @param includedNamespaces the namespaces included in this SWC using the
     * -include-namespaces option.
     * @return true if component should be included, false otherwise.
     */
    private boolean includeComponent(XMLName tagName, Set<String> includedNamespaces)
    {
        if (includedNamespaces.contains(tagName.getXMLNamespace()))
        {
            return !flexProject.isManifestComponentLookupOnly(tagName) ||
                   targetSettings.isIncludeLookupOnlyEnabled();
        }
        
        return false;
     }

    /**
     * Add asset files to the SWC model.
     * 
     * @param problems Problem collection.
     */
    private void addFileEntriesToSWC(Collection<ICompilerProblem> problems)
    {
        // -include
        Map<String, FileEntryValue> fileEntries = computeIncludedFiles();
        for (Entry<String, FileEntryValue> entry : fileEntries.entrySet())
        {
            processFileEntry(entry, problems);
        }
        
        // -include-libraries
        Map<String, ISWCFileEntry> includeLibraryFiles = getIncludedLibrariesFiles();
        for (ISWCFileEntry fileEntry : includeLibraryFiles.values())
        {
            swc.addFile(fileEntry);
        }

        // [IconFile(...)] metadata
        Map<String, FileEntryValue> iconFilesMap = computeIconFiles();
        for (Entry<String, FileEntryValue> entry : iconFilesMap.entrySet())
        {
            processFileEntry(entry, problems);
        }
    }
    
    /**
     * Helper method used by addFileEntriesToSWC() to process one file.
     */
    private void processFileEntry(Entry<String, FileEntryValue> entry,
                                  Collection<ICompilerProblem> problems)
    {
        String path = entry.getKey();
        IBinaryFileSpecification fileSpec = entry.getValue().getFileSpec();
        ISourceLocation sourceLocation = entry.getValue().getSourceLocation();
        
        byte[] contents = getContents(fileSpec);
        
        if (contents != null)
        {
            swc.addFile(path, fileSpec.getLastModified(), contents);
        }
        else    
        {
            final ICompilerProblem problem =
                sourceLocation != null ?
                new FileNotFoundProblem(sourceLocation, fileSpec.getPath()) :
                new FileNotFoundProblem(fileSpec.getPath());
            problems.add(problem);
        }
    }
    
    /**
     * Returns the contents of a binary file as an array of bytes,
     * or null if the file cannot be read.
     */
    private byte[] getContents(IBinaryFileSpecification fileSpec)
    {
        byte[] contents = null;
        
        try
        {
            final DAByteArrayOutputStream buffer = new DAByteArrayOutputStream();
            final InputStream fileInputStream = fileSpec.createInputStream();
            IOUtils.copy(fileInputStream, buffer);
            IOUtils.closeQuietly(buffer);
            IOUtils.closeQuietly(fileInputStream);
            contents = buffer.getDirectByteArray();
        }
        catch (IOException e)
        {
        }
        
        return contents;
    }
    
    /**
     * Build "library.swf" model and add to the SWC model as a
     * {@link ISWCLibrary}.
     * 
     * @param definitionsToBuild After this method returns, all the definitions
     * to be build into the target will be stored on this collection. It is
     * needed to compute a set of URLs for {@code <component>} tags in
     * catalog.xml file.
     * @param problems Compiler problems.
     * @return the ISWF for the library
     * @throws InterruptedException Compilation terminated.
     */
    private ISWF buildLibrarySWF(final Collection<IDefinition> definitionsToBuild,
            final Collection<ICompilerProblem> problems)
            throws InterruptedException
    {
        getLibrarySWFTarget();
        Iterables.addAll(problems, getRootedCompilationUnits().getProblems());
        
        final LinkageChecker externalLinkageChecker = new LinkageChecker(
                flexProject, targetSettings);
        ((Target)librarySWFTarget).setLinkageChecker(externalLinkageChecker);
        setLinkageChecker(externalLinkageChecker);
        final ISWF defaultLibrarySWF = librarySWFTarget.build(problems);
        
        // make default library model
        final ISWCLibrary defaultLibrary = new SWCLibrary(LIBRARY_SWF, defaultLibrarySWF);
        swc.addLibrary(defaultLibrary);
    
        // Deal with all the cu's that don't need to be added via addScript
        // save all the add script ones in cuToWrite for the next phase.
        Set<ICompilationUnit> cuToWrite = new HashSet<ICompilationUnit>();
        for (final ICompilationUnit cu : librarySWFTarget.getCompilationUnits())
        {
            if (isLinkageExternal(cu, targetSettings))
            {
                    // don't do anything with these
            }
            //Resource bundles processed uniquely
            else if (cu instanceof ResourceBundleCompilationUnit)
            {
                assert project instanceof FlexProject;
                processResourceBundle((FlexProject)project, (ResourceBundleCompilationUnit)cu,
                            swc, problems);
            }
            else
            {
                // everyone else goes in this list for next step             
                cuToWrite.add(cu);
            }
        }
        
        // remove duplicates and log resulting problems, then add to library
        filterCompilationUnits(cuToWrite, problems);   
        for (final ICompilationUnit cu : cuToWrite)
        {
            defaultLibrary.addScript(createScript(cu, definitionsToBuild));
        }
        
        // Add the generated root class and its dependencies to the list of 
        // scripts.
        if (librarySWFTarget.getRootClassName() != null)
        {
            final SWCScript script = new SWCScript();
            script.setName(librarySWFTarget.getRootClassName());
            script.addDefinition(librarySWFTarget.getRootClassName());
            // This class is synthetic and is supposed to be globally
            // unique, so nobody should care what the time stamp is.
            // If we set the time stamp to the current time, then
            // every SWC we make will be slightly different from all other
            // SWCs.
            script.setLastModified(1);

            // add baseclass as an inheritance dependency
            script.addDependency(librarySWFTarget.getBaseClassQName(), DependencyType.INHERITANCE);
            defaultLibrary.addScript(script);
        }
        
        if (librarySWFTarget.getASMetadataNames() != null)
        {
            for (String name : librarySWFTarget.getASMetadataNames())
            {
                defaultLibrary.addNameToKeepAS3MetadataSet(name);
            }
        }

        return defaultLibrarySWF;
    }

    /**
     * removes compilation units from a list if they are shaddowed by other ones
     * that try to generate the same script. Logs a problem in this case
     * 
     * @param cuToWrite
     * @param problems
     * @throws InterruptedException
     */
    private void filterCompilationUnits(Set<ICompilationUnit> cuToWrite, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        // group cu's by script name.
        Map<String, Set<ICompilationUnit>> cuMap = new HashMap<String, Set<ICompilationUnit>>();
        
        Comparator<ICompilationUnit> comparator = new Comparator<ICompilationUnit>()   {
            @Override
            public int compare(ICompilationUnit arg0, ICompilationUnit arg1) {
                // inverted sort on name will give the behavior of the old compiler:
                // last path in alphabetical order wins
                return -arg0.getName().compareTo(arg1.getName()); }};
                
        for (ICompilationUnit cu : cuToWrite)
        {
            String name = cu.getSWFTagsRequest().get().getDoABCTagName();
            Set<ICompilationUnit> l = cuMap.get(name);
            if (l == null)
            {
                l = new TreeSet<ICompilationUnit>(comparator);
                cuMap.put(name, l);
            }
            l.add(cu);
        }

        // now that we have grouped CUs based on script name, we can look for dupes
        for (Collection<ICompilationUnit> cus : cuMap.values())
        {
            assert !cus.isEmpty();

            Iterator<ICompilationUnit> it = cus.iterator();

            ICompilationUnit cuKept = it.next(); // we are keeping the first one
            ICompilationUnit cuDumped = null;
            boolean somoneHasVisibleDefinitions = hasExternallyVisibleDefinitions(cuKept);

            while (it.hasNext()) // remove all the dupes from the list
            {
                cuDumped = it.next(); // Remember the last dupe to report it
                cuToWrite.remove(cuDumped);
                somoneHasVisibleDefinitions |= hasExternallyVisibleDefinitions(cuDumped);
            }

            // report a problem for dupe, unless none of the shadows has anything in it.
            if (cuDumped != null && somoneHasVisibleDefinitions)
            {
                problems.add(new DuplicateScriptProblem(cuKept.getAbsoluteFilename(), cuDumped.getAbsoluteFilename()));
            }
        }
    }
    
    private boolean hasExternallyVisibleDefinitions(ICompilationUnit cu) throws InterruptedException
    {
        final IFileScopeRequestResult r = cu.getFileScopeRequest().get();
        Collection<IDefinition> vis = r.getExternallyVisibleDefinitions();
        return !vis.isEmpty(); 
    }

    /**
     * Create a new script based on the compilation unit.
     * 
     * @param cu
     * @param definitionsToBuild
     * @return
     * @throws InterruptedException
     */
    private SWCScript createScript(ICompilationUnit cu, 
            Collection<IDefinition> definitionsToBuild) throws InterruptedException
    {
        // Create a script model per compilation unit.
        final SWCScript script = new SWCScript();
        script.setName(cu.getSWFTagsRequest().get().getDoABCTagName());

        // Add all the externally visible definitions to the script model.
        final IFileScopeRequestResult fsResult = cu.getFileScopeRequest().get();
        for (final IDefinition def : fsResult.getExternallyVisibleDefinitions())
        {
            script.addDefinition(def.getQualifiedName());
            script.setLastModified(cu.getSyntaxTreeRequest().get().getLastModified());
            
            if (definitionsToBuild != null)
                definitionsToBuild.add(def);
        }
        final DependencyGraph dependencyGraph =
            flexProject.getDependencyGraph();
        Set<ICompilationUnit> directDependencies = 
            dependencyGraph.getDirectDependencies(cu);
        for (ICompilationUnit directDependency : directDependencies)
        {
            final Map<String, DependencyTypeSet> dependenciesMap =
                dependencyGraph.getDependencySet(cu, directDependency);
            for (Map.Entry<String, DependencyTypeSet> dependencyEntry : dependenciesMap.entrySet())
            {
                for (DependencyType type : dependencyEntry.getValue())
                    script.addDependency(dependencyEntry.getKey(), type);
            }
            
        }

        return script;
    }
    
    /**
     * Process a resource bundle represented by the specified compilation unit.
     * 
     * @param project active project
     * @param cu resource bundle compilation unit to process
     * @param swc target swc
     * @param problems problems collection
     */
    private void processResourceBundle(FlexProject project, ResourceBundleCompilationUnit cu,
            SWC swc, Collection<ICompilerProblem> problems)
    {
        Collection<String> locales = null;
        if (cu.getLocale() == null)
        {
            //Create a file entry for each locale since this compilation unit is not locale specific
            locales = project.getLocales();
        }
        else
        {
            //This compilation unit is for a locale specific file, 
            //therefore create a file entry for only the locale comp unit depends on
            locales = Collections.singletonList(cu.getLocale());
        }

        byte[] fileContent = cu.getFileContent(problems); //get file content

        if (fileContent == null)
            return; //Is this the right thing to do here?

        for (String locale : locales)
        {
            //Build the destination path. Destination path for a .properties file 
            //is such as locale/{locale}/foo/bar/x.properties

            StringBuilder sb = new StringBuilder();

            sb.append(ResourceBundleCompilationUnit.LOCALE);
            sb.append(File.separator);
            sb.append(locale);
            sb.append(File.separator);
            sb.append(QNameNormalization.normalize(cu.getBundleNameInColonSyntax()).replace('.', File.separatorChar));
            sb.append(FilenameUtils.EXTENSION_SEPARATOR_STR);
            sb.append(ResourceBundleSourceFileHandler.EXTENSION);

            swc.addFile(sb.toString(), cu.getFileLastModified(), fileContent);
        }
    }

    @Override
    public boolean addToZipOutputStream(ZipOutputStream output, Collection<ICompilerProblem> problemCollection)
    {
        throw new UnsupportedOperationException();
    }

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
    
    @Override
    public RootedCompilationUnits getRootedCompilationUnits() throws InterruptedException
    {
        if (this.rootedCompilationUnits == null)
            rootedCompilationUnits = computeRootedCompilationUnits();
        assert rootedCompilationUnits != null;
        return rootedCompilationUnits;
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
        for (File sourcePath : flexProject.getSourcePath())
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
                flexProject.getQualifiedClassNamesForManifestNamespaces(
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
            Collection<XMLName> xmlNames = flexProject.getTagNamesForClass(className);
            for (XMLName xmlName : xmlNames)
            {
                if (namespace.equals(xmlName.getXMLNamespace()))
                {
                    if (!flexProject.isManifestComponentLookupOnly(xmlName) && 
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
            Collection<XMLName> tagNames = flexProject.getTagNamesForClass(className);
            boolean okToAdd = true;
            for (XMLName tagName : tagNames)
            {
                if (flexProject.isManifestComponentLookupOnly(tagName))
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
            IDefinition def = reference.resolve(flexProject);
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

    /**
     * @return Expand {@code -include-files} items into normalized file paths.
     */
    private Map<String, FileEntryValue> computeIncludedFiles()
    {
        Map<String, FileEntryValue> includedFiles = new HashMap<String, FileEntryValue>();
        for (Entry<String, File> entry : targetSettings.getIncludeFiles().entrySet())
        {
            String filename = entry.getKey();
            if (filename.contains("*"))
            {
                Collection<File> files = getFiles(entry.getValue());
                String basename = entry.getValue().getAbsolutePath();
                for (File file : files)
                {
                    String path = file.getAbsolutePath();
                    IBinaryFileSpecification fileSpec = project.getWorkspace().getLatestBinaryFileSpecification(path);

                    if (filename != null && fileSpec != null)
                    {
                        String relativePath = path.substring(basename.length() + 1);
                        relativePath = filename.replace("*", relativePath);
                        FileEntryValue value = new FileEntryValue(fileSpec, null);
                        includedFiles.put(relativePath, value);
                    }                    
                }
            }
            else
            {
                String path = entry.getValue().getAbsolutePath();
                IBinaryFileSpecification fileSpec = project.getWorkspace().getLatestBinaryFileSpecification(path);
    
                if (filename != null && fileSpec != null)
                {
                    FileEntryValue value = new FileEntryValue(fileSpec, null);
                    includedFiles.put(filename, value);
                }
            }
        }

        return includedFiles;
    }
    
    private Collection<File> getFiles(File file)
    {
        String filename = file.getAbsolutePath();
        int c = filename.lastIndexOf("/");
        if (c != -1)
            filename = filename.substring(0, c);
        Collection<File> files = FileUtils.listFiles(new File(
                filename), null, true);
        return files;
    }
    /**
     * The collection of files from all of the libraries found on the 
     * -include-libraries path.
     * 
     * @return collection of absolute path names of SWC files.
     */
    private Map<String, ISWCFileEntry> getIncludedLibrariesFiles()
    {
        Map<String, ISWCFileEntry> files = new HashMap<String, ISWCFileEntry>();
        
        // Find all of the libraries on the -include-library path
        Set<FileID> swcs = LibraryPathManager.discoverSWCFilePaths(
                targetSettings.getIncludeLibraries().toArray(new File[0]));
        
        // For each library, get a compilation unit for every class in the
        // library.
        ISWCManager swcManager = project.getWorkspace().getSWCManager();
        for (FileID swcPath : swcs)
        {
            ISWC swc = swcManager.get(swcPath.getFile());
            Map<String, ISWCFileEntry> swcFiles = swc.getFiles();
            for (Map.Entry<String, ISWCFileEntry> file : swcFiles.entrySet())
            {
                ISWCFileEntry currentFileEntry = files.get(file.getKey());
                if (currentFileEntry == null ||
                   (file.getValue().getLastModified() > currentFileEntry.getLastModified()))
                {
                    files.put(file.getKey(), file.getValue());
                }
            }
        }  
        
        return files;
    }
    
    /**
     * Loops over all externally visible definitions in all compilation units
     * for this target, looking for class definitions with [IconFile(...)] metadata.
     * <p>
     * Suppose we find [IconFile("MyComponent.png")] on the class foo.bar.MyComponent.
     * Then the returned map will have an entry whose key is "foo/bar/MyComponent"
     * and whose value is a file specification that can be used to read the contents
     * of this file. A file with the same contents will get created at foo/bar/MyComponent
     * inside the SWC.
     */
    private Map<String, FileEntryValue> computeIconFiles()
    {
        Map<String, FileEntryValue> iconFiles = new HashMap<String, FileEntryValue>();

        try
        {
            Collection<ICompilationUnit> compilationUnits = librarySWFTarget.getCompilationUnits();
            for (IDefinition definition : getAllExternallyVisibleDefinitions(compilationUnits))
            {
                if (!(definition instanceof IClassDefinition))
                    continue;

                IClassDefinition classDefinition = (IClassDefinition)definition;
                
                IMetaTag iconFileMetaTag = classDefinition.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ICON_FILE);
                
                String iconFilePath = classDefinition.getIconFile();
                if (iconFilePath == null)
                    continue;

                String packageName = classDefinition.getPackageName();
                String key = packageName.replaceAll("\\.", "/") + "/" + iconFilePath;

                ICompilationUnit cu = project.getScope().getCompilationUnitForDefinition(classDefinition);
                IBinaryFileSpecification fileSpec = null;
                if (cu.getCompilationUnitType() == UnitType.SWC_UNIT)
                {
                    ISWC swc = project.getWorkspace().getSWCManager().get(new File(cu.getAbsoluteFilename()));
                    ISWCFileEntry swcFileEntry = swc.getFile(key);
                    fileSpec = new SWCFileSpecification(key, swcFileEntry);
                }
                else
                {
                    String sourcePath = classDefinition.getSourcePath();
                    iconFilePath = FilenameUtils.getFullPath(sourcePath) + iconFilePath;
                    fileSpec = project.getWorkspace().getLatestBinaryFileSpecification(iconFilePath);
                }

                FileEntryValue value = new FileEntryValue(fileSpec, iconFileMetaTag);
                iconFiles.put(key, value);
            }
        }
        catch (InterruptedException e)
        {
        }
        
        return iconFiles;
    }

    @Override
    protected ITargetReport computeTargetReport() throws InterruptedException
    {
        assert librarySWFTarget != null : "Must call build before getting the target report!!";
        return librarySWFTarget.getTargetReport();
    }
    
    private static class FileEntryValue
    {
        FileEntryValue(IBinaryFileSpecification fileSpec, ISourceLocation sourceLocation)
        {
            this.fileSpec = fileSpec;
            this.sourceLocation = sourceLocation;
        }
        
        private IBinaryFileSpecification fileSpec;
        private ISourceLocation sourceLocation;
        
        IBinaryFileSpecification getFileSpec()
        {
            return fileSpec;
        }
        
        ISourceLocation getSourceLocation()
        {
            return sourceLocation;
        }
    }
}
