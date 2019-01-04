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

package org.apache.royale.compiler.internal.projects;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;

import org.apache.royale.compiler.internal.caches.CacheStoreKeyBase;
import org.apache.royale.compiler.internal.caches.SWFCache;
import org.apache.royale.compiler.internal.units.ResourceBundleCompilationUnit;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.problems.DuplicateSourceFileProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swc.ISWCManager;
import org.apache.royale.swc.ISWCScript;
import org.apache.royale.swc.SWCManager;
import org.apache.royale.swf.ITagContainer;
import org.apache.royale.utils.FileID;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Manages the library path of a {@link ASProject}. It has a list of SWC file
 * handles discovered on the library path. The manager doesn't keep SWC models,
 * because the {@link SWCManager} keep them as {@link SoftReference}.
 * {@link SWCManager} is responsible for providing a {@link ISWC} model from a
 * file.
 */
public final class LibraryPathManager
{
    public static final String SWC_EXT = "swc";
    public static final String ANE_EXT = "ane";

    // SWC file extension
    public static final String DOT_SWC_EXT = "." + SWC_EXT;

    // ANE file extension
    public static final String DOT_ANE_EXT = "." + ANE_EXT;

    // A filename filter that only accepts SWC files and ANE files.
    private static final FilenameFilter SWC_FILTER = FileFilterUtils.or(FileFilterUtils.suffixFileFilter(DOT_SWC_EXT, IOCase.INSENSITIVE), FileFilterUtils.suffixFileFilter(DOT_ANE_EXT, IOCase.INSENSITIVE));
    
    public static boolean isSWCFile(File file)
    {
        return SWC_FILTER.accept(file.getParentFile(), file.getName());
    }
    
    /**
     * Find all the SWC files on the library path. The given library path
     * entries can be a SWC file or a directory containing SWC files. Note that
     * it does <b>NOT</b> read the directories recursively.
     * 
     * Returns FileID objects so we can be sure that we have not accidenly made a case sensitive comparison
     */
    public static Set<FileID> discoverSWCFilePaths(final File[] libraryPath)
    {
        final Set<FileID> swcFiles = new LinkedHashSet<FileID>();

        for (final File path : libraryPath)
        {
            if (path.isDirectory())
            {
                final File[] swcsInFolder = path.listFiles(SWC_FILTER);
                for (final File file : swcsInFolder)
                    swcFiles.add( new FileID(file));
            }
            else
            {
                 swcFiles.add(new FileID(path));
            }
        }
        return swcFiles;
    }
    
    /**
     * like discoverSWCFilePaths, but returns File instead of FileID. Only needed for extrenal clients
     * who don't / shouldn't have access to FileID
     * @param libraryPath An array of {@code File} objects.
     */
    public static Set<File> discoverSWCFilePathsAsFiles(final File[] libraryPath)
    {
        Set<File> ret = new HashSet<File>();
        
        Set<FileID> fileIds = discoverSWCFilePaths( libraryPath);
        for (FileID f : fileIds)
        {
            ret.add(f.getFile());
        }
        return ret;
    }

    /**
     * Create a library path manager for a given project.
     * 
     * @param flashProject flash project
     */
    public LibraryPathManager(final ASProject flashProject)
    {
        checkNotNull(flashProject, "Flash project cannot be null.");
        this.flashProject = flashProject;
        this.libraryFilePaths = new LinkedHashMap<String, String>();
    }

    /** Owner project. */
    private final ASProject flashProject;

    /**
     * The bidirectional map stores normalized paths and source paths of the SWC
     * files on library path.
     * <p>
     * The key is the SWC file path; The value is the source file directory.
     */
    private final Map<String, String> libraryFilePaths;

    /**
     * Create {@code SWCCompilationUnit} objects from the given SWC files. These
     * compilation units will be added to the project.
     * 
     * @param swcFilePaths an array of SWC file paths
     * @return new compilation units from the given SWC file paths
     */
    private List<ICompilationUnit> computeUnitsToAdd(final Collection<String> swcFilePaths)
    {
        int order = 0;
        final List<ICompilationUnit> result = new LinkedList<ICompilationUnit>();
        final ISWCManager swcManager = flashProject.getWorkspace().getSWCManager();
        for (final String swcFilePath : swcFilePaths)
        {
            // it is possible for the SWC to not exist on disk, if this method
            // is being called as part of a SWC file file removal invalidation.
            File swcFile = new File(swcFilePath);
            final ISWC swc = swcManager.get(swcFile);
            computeUnitsToAdd(swc, order, result);
            order++;
        }

        return result;
    }

    /**
     * Create {@code SWCCompilationUnit} objects from the given ISWC. These
     * compilation units will be added to the project.
     * 
     * @param swc ISWC 
     * @param order The oder of the SWC in the project
     * @param cus New compilation units from the given SWC file paths
     */
    private void computeUnitsToAdd(ISWC swc, int order, List<ICompilationUnit> cus)
    {
        for (final ISWCLibrary library : swc.getLibraries())
        {
            for (final ISWCScript script : library.getScripts())
            {
                // Multiple definition in a script share the same compilation unit 
                // with the same ABC byte code block.
                final List<String> qnames = new ArrayList<String>(script.getDefinitions().size());
                for (final String definitionQName : script.getDefinitions())
                {
                    qnames.add(definitionQName.replace(":", "."));
                }

                final ICompilationUnit cu = new SWCCompilationUnit(
                        flashProject, swc, library, script, qnames, order);
                cus.add(cu);
            }
        }
        
        //If the project's locale is empty, then don't try to create ResourceBundleCompilationUnits 
        // because no resource bundles won't go into swf or swc anyways.
        if (flashProject instanceof IRoyaleProject && 
                ((IRoyaleProject)flashProject).getLocales().size() > 0)
        {   
            //Create compilation units for all the .properties files in the swc.
            for (final ISWCFileEntry entry : swc.getFiles().values())
            {
                if (FilenameUtils.getExtension(entry.getPath()).equals(ResourceBundleSourceFileHandler.EXTENSION))
                {
                    final ICompilationUnit cu = createResourceBundleCompilationUnit(swc, entry);
                    if (cu != null)
                    {
                        cus.add(cu);
                    }
                }
            }
        }
    }

    /**
     * Set the library path. It will populate {@link SWCManager} with SWC files
     * found on the library path. It also creates {@link SWCCompilationUnit}
     * objects for the discovered SWC files, and add these compilation units to
     * the project.
     * <p>
     * It is optimized to do nothing when the new library path is same as the
     * existing setting.
     * 
     * See setLibrarySourcePath() as to why discoverSWCFilePaths() just ignores
     * invalid entries in the path
     * 
     * @param libraryPath the {@code File} on the library path can be a SWC file
     * or a directory containing SWC files.
     */
    public void setLibraryPath(final File libraryPath[])
    {
        assert libraryPath != null : "Library path cannot be null";

        final Set<FileID> swcFilePaths = discoverSWCFilePaths(libraryPath);

        // compute SWC files to add and remove
        final Collection<String> swcFilesToAdd = new LinkedHashSet<String>();
        final Collection<String> swcFilesToRemove = new LinkedHashSet<String>();
        
        // Now we can convert the FileID's to Strings, since we have eliminated dupes that use
        // mixed case. From here on, we don't have to worry about upper/lower case.
        Set<String> swcFilePathStrings = new LinkedHashSet<String>();
        for (FileID f : swcFilePaths)
            swcFilePathStrings.add(f.getFile().getAbsolutePath());
        computeAddRemoveSet(swcFilePathStrings, swcFilesToAdd, swcFilesToRemove);

        // update the library path map
        libraryFilePaths.clear();
        for (final FileID path : swcFilePaths)
        {
            libraryFilePaths.put(path.getFile().getAbsolutePath(), null);
        }

        // apply changes to the project
        updateLibraryPath(swcFilesToAdd, swcFilesToRemove);
    }

    /**
     * Set the source path of a SWC library.
     * 
     * @param library SWC file path
     * @param sourceDir source directory
     * 
     * Note if we are passed a bad path here, we just ignore it. This happens often when
     * Projects have stale/strange lib source paths.
     * We COULD try to log a problem, but Bruce and Chris discussed this, and it seems like
     * overkill, as this only affects code hinting.
     */
    public void setLibrarySourcePath(final File library, final File sourceDir)
    {
        if (library==null) return;
        final String swcPath = FilenameNormalization.normalize(library.getPath());

        if (sourceDir == null)
        {
            // clear source path
            libraryFilePaths.put(swcPath, null);
        }
        else
        {
            if (!sourceDir.exists() || !sourceDir.isDirectory() || !libraryFilePaths.containsKey(swcPath) )
                return;
            // set source path 
            final String sourcePath = FilenameNormalization.normalize(sourceDir.getPath());
            libraryFilePaths.put(swcPath, sourcePath);
        }
    }

   
    
    /**
     * Find the directory were a given library might live.
     * Normally used for finding source attachments.
     * @param libraryFilename name of a library we want to find
     * @return a String with the full path to the folder.
     */
    public String getAttachedSourceDirectory(String libraryFilename)
    {
        final String swcPath = FilenameNormalization.normalize(libraryFilename);
        final String attachedSourceDirectory = libraryFilePaths.get(swcPath);
        return attachedSourceDirectory;
    }
    
    /**
     * Finds a source file in the  source directory 
     * for the specified qualified name.
     * @param sourceDirectory  Path to directory where source attachment directory should be exist.
     * @param qualifiedName Dotted qualified name to find a source file for.
     * @return Absolute file name of for specified dotted qualified name in the specified library file.
     */
    public static String getAttachedSourceFilename(String sourceDirectory, String qualifiedName)
    {
        if (sourceDirectory == null)
            return null;
        // Infer source file path based on QName using Flex source path convention.
        final String relativePath = qualifiedName.replace('.', File.separatorChar);
        
   
        // Hard code to known list of extensions. If we need to get this from project we could
        // always pass in the extension list, but that seems unnecessary. We DON'T want to force callers
        // to pass in a project
        String[] knownExtensions = { "mxml", "as", "fxg" };
        for (String ext : knownExtensions)
        {
            final File f = new File(sourceDirectory, relativePath.concat(".").concat(ext));
            // TODO: there is a potential bug here on case sensitive file systems
            // as on the fs there could be foo.AS, but we only check the existence
            // of foo.as, as getHandledFileExtensions() returns lower cases strings.
            // Also, if there is foo.AS and foo.as, which one do we choose?
            if (f.exists())
                return FilenameNormalization.normalize(f).getAbsolutePath();
        }
        return null;
    }
    
    /**
     * Get source path for a SWC library.
     * 
     * @param library SWC file
     * @return Directory that contains the source for a SWC library; Null if the
     * source path hasn't been set.
     */
    public File getLibrarySourcePath(final File library)
    {
        final String swcPath = FilenameNormalization.normalize(library.getPath());
        final String sourcePath = libraryFilePaths.get(swcPath);
        if (sourcePath == null)
            return null;
        else
            return new File(sourcePath);
    }

    /**
     * Compute the set of libraries to add and remove.<br>
     * Remove all the existing libraries that are in the new library path.<br>
     * Add all the new library path that are not in the existing library path.
     * 
     * @param swcFilePaths Set of swcs to load. The iterator will iterate over
     * the swcs in priority order.
     * @param swcFilesToAdd SWC files to be added
     * @param swcFilesToRemove SWD files to be removed
     */
    private void computeAddRemoveSet(final Set<String> swcFilePaths,
                                     final Collection<String> swcFilesToAdd,
                                     final Collection<String> swcFilesToRemove)
    {
        // only add a SWC to the swcFilesToAdd if it exists in on disk. swcFilePaths
        // can have files which don't exist yet, as a library project we depend on
        // may not have been compiled yet.
        for (String swcPath : swcFilePaths)
        {
            File f = new File(swcPath);
            if (f.exists())
                swcFilesToAdd.add(swcPath);
        }

        Iterator<String> swcFilePathsIter = swcFilePaths.iterator();
        boolean swcOrderDiffers = false;
        
        // if an existing swc is not in the newPath, it needs to be removed.
        for (final String existingSWCFilePath : libraryFilePaths.keySet())
        {
            /*
             * If the order of the SWCs is different then add and remove all 
             * SWCs in the list after and including that SWC.
             */
            String newSWCFilePath = null;
            
            if (!swcOrderDiffers)
            {
                if (!swcFilePathsIter.hasNext())
                {
                    // we can run past the swcFilePaths when removing the last
                    // element from the library path
                    swcOrderDiffers = true;
                }
                else
                {
                    newSWCFilePath = swcFilePathsIter.next();
                    
                    if (existingSWCFilePath.equals(newSWCFilePath))
                    {
                        swcFilesToAdd.remove(existingSWCFilePath);
                        continue;
                    }
                    else 
                    {
                        swcOrderDiffers = true;
                    }
                }
            }

            /*
             * If the SWC order is detected as being different, then
             * always remove existing SWCs. Remove SWCs from the "add"
             * list that are no longer needed.
             */
            if (swcOrderDiffers)
            {
                swcFilesToRemove.add(existingSWCFilePath);                

                if (!swcFilesToAdd.contains(existingSWCFilePath))
                    swcFilesToAdd.remove(existingSWCFilePath);
            }
        }
    }

    /**
     * Add and remove SWC file on the {@link ASProject}. This method will add
     * and remove the corresponding {@link ISWC} object as well.
     * 
     * @param swcFilesToAdd
     * @param swcFilesToRemove
     * @param isExternal
     */
    private void updateLibraryPath(final Collection<String> swcFilesToAdd,
                                   final Collection<String> swcFilesToRemove)
    {
        final List<ICompilationUnit> unitsToAdd =
                computeUnitsToAdd(swcFilesToAdd);
        final List<ICompilationUnit> unitsToRemove =
                computeUnitsToRemove(swcFilesToRemove);
        assert unitsToAdd != null;
        assert unitsToRemove != null;
        // update the project
        flashProject.updateCompilationUnitsForPathChange(
                unitsToRemove,
                unitsToAdd);
    }

    /**
     * Find all the compilation units related to the given SWC file paths. These
     * compilation units will be removed from the project. This method also
     * removes the {@code ISWC} objects from the {@link SWCManager}.
     * 
     * @param swcFilesToRemove SWC file paths
     * @return compilation units associated with the SWC files
     */
    @SuppressWarnings("incomplete-switch")
	private List<ICompilationUnit> computeUnitsToRemove(final Collection<String> swcFilesToRemove)
    {
        final List<ICompilationUnit> unitsToRemove;
        unitsToRemove = new ArrayList<ICompilationUnit>();
        for (final String swcFilePath : swcFilesToRemove)
        {
            final Collection<ICompilationUnit> compilationUnits = flashProject.getCompilationUnits(swcFilePath);

            // only add SWC compilation units.  It's possible to get other compilation unit types here when
            // looking up by filename, as am EmbedCompilationUnit can have a SWC filename
            List<ICompilationUnit> swcCompilationUnits = new ArrayList<ICompilationUnit>(compilationUnits.size());
            for (ICompilationUnit cu : compilationUnits)
            {
                switch (cu.getCompilationUnitType())
                {
                    case SWC_UNIT:
                    case RESOURCE_UNIT:
                        swcCompilationUnits.add(cu);
                        break;
                }
            }
            unitsToRemove.addAll(swcCompilationUnits);
        }
        return unitsToRemove;
    }

    /**
     * Get {@link ISWC} objects for the SWC files on the library path.
     * {@code LibraryPathManager} does not keep a list of all the SWC model.
     * 
     * @return A list of {@link ISWC}'s on the library path.
     */
    protected ImmutableList<ISWC> getLibrarySWCs()
    {
        final ISWCManager swcManager = flashProject.getWorkspace().getSWCManager();
        final ImmutableList.Builder<ISWC> builder = new ImmutableList.Builder<ISWC>();
        for (final String libraryPath : libraryFilePaths.keySet())
        {
            final File swcFile = new File(libraryPath);
            if (swcFile.exists())
            {
                final ISWC swc = swcManager.get(swcFile);
                builder.add(swc); // ImmutableList throws exception on null element.
            }
        }

        return builder.build();
    }
    
    /**
     * Invalidate a collection of SWC files on the library path.
     * 
     * @param swcFiles SWC files on the library path
     * @return true if a library referenced by the project was invalidated
     */
    protected boolean invalidate(Collection<File> swcFiles)
    {
        Set<String> swcFilePaths = new HashSet<String>(swcFiles.size());
        for (File swcFile : swcFiles)
        {
            if (swcFile != null)
            {
                String swcFilePath = FilenameNormalization.normalize(swcFile.getPath());
                if (libraryFilePaths.containsKey(swcFilePath))
                {
                    swcFilePaths.add(swcFilePath);
                }
            }
        }

        if (swcFilePaths.isEmpty())
            return false;

        updateLibraryPath(swcFilePaths, swcFilePaths);
        return true;
    }

    /**
     * Invalidate a SWC on the library path.
     * 
     * @param swc ISWC to invalidate
     */
    protected void invalidate(ISWC swc)
    {
        String swcFilename = swc.getSWCFile().getAbsolutePath();
        final Collection<ICompilationUnit> unitsToRemove = flashProject.getCompilationUnits(swcFilename);

        final List<ICompilationUnit> unitsToAdd = new LinkedList<ICompilationUnit>();
        computeUnitsToAdd(swc, 0, unitsToAdd);

        // update any depending projects on this library change
        flashProject.getWorkspace().swcChanged(unitsToRemove, unitsToAdd, new Runnable()
        { 
            @Override
            public final void run()
            {
                // update the project
                flashProject.updateCompilationUnitsForPathChange(unitsToRemove, unitsToAdd);
            }
        });
    }

    /**
     * Search the library path for file entries matching the filename
     * @param filename Filename to search for
     * @return ISWCFileEntry or null if not found
     */
    public ISWCFileEntry getFileEntryFromLibraryPath(String filename)
    {
        ImmutableList<ISWC> swcs = getLibrarySWCs();

        ISWCFileEntry fileEntry = null;
        for (ISWC swc : swcs)
        {
            fileEntry = swc.getFile(filename);
            if (fileEntry != null)
                break;
        }

        return fileEntry;
    }

    /**
     * Computes the qualified name of a properties file that is in a swc and 
     * creates a compilation unit for that.
     * 
     * Example: For a properties file located at
     * "locale/en_US/foo/bar/core.properties" in a swc, the qualified name is
     * computed as "foo.bar.core".
     * 
     * @param swc swc that contains the properties file
     * @param fileEntry file entry that points to the properties file in the swc
     * @return {@link ResourceBundleCompilationUnit} for the specified swc file entry.
     */
    private ICompilationUnit createResourceBundleCompilationUnit(ISWC swc, ISWCFileEntry fileEntry)
    {
        String path = FilenameUtils.separatorsToSystem(fileEntry.getPath());
        String[] segments = Iterables.toArray(Splitter.on(File.separator).split(path), String.class);
        
        //the pattern used in SWC files to store resource bundles is 'locale/{locale}/..", 
        //therefore the segment count needs to be at least 2.
        if (segments.length > 2 && ResourceBundleCompilationUnit.LOCALE.equals(segments[0])) 
        {   
            StringBuilder qName = new StringBuilder();
            for(int i=2; i<segments.length-1; i++)
            {
                qName.append(segments[i]);
                qName.append('.');
            }
            qName.append(FilenameUtils.getBaseName(segments[segments.length-1]));
            
            return new ResourceBundleCompilationUnit(
                    flashProject, fileEntry, qName.toString(), segments[1]);
        }
        
        return null;
    }

    /**
     * Add {@link ICompilerProblem}'s found in the current library path to the
     * specified collection.
     * <p>
     * These problems are with the library path itself, not with the libraries
     * themselves. For example if a SWC has been deleted and no longer exists.
     * {@link DuplicateSourceFileProblem} problems.
     */
    void collectProblems(Collection<ICompilerProblem> problems)
    {
        final ISWCManager swcManager = flashProject.getWorkspace().getSWCManager();
        for (String swcPath : libraryFilePaths.keySet())
        {
            final File swcFile = new File(swcPath);
            ISWC swc = swcManager.get(swcFile);
            problems.addAll(swc.getProblems());
            
            for (ISWCLibrary library : swc.getLibraries())
            {
                final CacheStoreKeyBase key = SWFCache.createKey(swc, library.getPath());
                final ITagContainer tags = ((SWFCache)swcManager.getSWFCache()).get(key);
                problems.addAll(tags.getProblems());
            }
        }
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        for (String libraryFilePath : libraryFilePaths.keySet().toArray(new String[0]))
        {
            sb.append(libraryFilePath);
            sb.append('\n');
        }
        
        return sb.toString();
    }
}
