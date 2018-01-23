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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Factory that can create {@link ICompilationUnit}'s
 * given File's.
 */
public class SourceCompilationUnitFactory
{
    public SourceCompilationUnitFactory(CompilerProject project)
    {
        this.project = project;
        handlerMap = new HashMap<String, ISourceFileHandler>();
    }

    private final CompilerProject project;
    private final Map<String, ISourceFileHandler> handlerMap;

    /**
     * Registers a new handler with the factory.
     * 
     * @param handler Handler to register.
     */
    public void addHandler(ISourceFileHandler handler)
    {
        String[] extensions = handler.getExtensions();
        for (String extension : extensions)
            handlerMap.put(extension.toLowerCase().intern(), handler);
    }

    private ISourceFileHandler getHandler(File file)
    {
        final String extension = FilenameUtils.getExtension(file.getName());
        return handlerMap.get(extension);
    }

    /**
     * Determines if there is a registered handler for the file type associated
     * with the specified file extension ( without the dot eg:
     * {@code as}, not {@code .as} ).
     * 
     * @param fileExtensionWithoutDot The file extension to check without a dot, for
     * example {@code mxml}.
     * @return true if there is a registered handler for the file type associated
     * with the specified file extension, false otherwise.
     */
    public boolean canCreateCompilationUnitForFileType(String fileExtensionWithoutDot)
    {
        return handlerMap.containsKey(fileExtensionWithoutDot);
    }
    
    /**
     * Determines if there is a registered handler that can handle the specified
     * File.
     * 
     * @param file A file.
     * @return true if a {@link ICompilationUnit} can be constructed for the
     * specified File.
     */
    public boolean canCreateCompilationUnit(File file)
    {
        return getHandler(file) != null;
    }
    
    /**
     * Determines if a {@link ICompilationUnit} is needed for the specified
     * file, qualified name, and locale.
     * <p>
     * This method will return false for resource bundles when the project does has
     * an empty locale list.
     * @return true if a {@link ICompilationUnit} is needed, false otherwise.
     */
    public boolean needCompilationUnit(File file, String qname, String locale)
    {
        final ISourceFileHandler handler = getHandler(file);
        if (handler == null)
            return false;
        return handler.needCompilationUnit(project, file.getPath(), qname, locale);
    }

    /**
     * Constructs an {@link ICompilationUnit} that can process the specified
     * file, if there is a registered handler that can handle the specified
     * file.
     * @param file A file.
     * @param basePriority {@code DefinitionPriority.BasePriority} used to determine
     * if definitions defined by the new {@link ICompilationUnit} shadow
     * definitions defined by other {@link ICompilationUnit}s.
     * @param order The index of the entry in the source path or source list
     * that is causing this method to be called.
     * @param locale locale of the file if the file is locale dependent or 
     * <code>null</code> if the file is not locale dependent.
     * @return New {@link ICompilationUnit} or null if there is no handler that
     * can handle the specified file or the handler decides to return null.
     */
    public ICompilationUnit createCompilationUnit(File file,
                                                  DefinitionPriority.BasePriority basePriority,
                                                  int order,
                                                  String qname, 
                                                  String locale)
    {
        final ISourceFileHandler handler = getHandler(file);
        if (handler == null)
            return null;
        if (!needCompilationUnit(file, qname, locale))
            return null;
        return handler.createCompilationUnit(project, file.getPath(), basePriority, order, qname, locale);
    }

    /**
     * Utility method to get an array of all file extensions for which there
     * is a handler.
     * 
     * @return array of handled extensions
     */
    public String[] getHandledFileExtensions()
    {
        return handlerMap.keySet().toArray(new String[0]);
    }
    
    /**
     * Determines if an invisible {@link ICompilationUnit} can be created for
     * the specified file.
     * 
     * @param file A file.
     * @return true if an invisible {@link ICompilationUnit} can be created for
     * the specified file, false otherwise.
     */
    public boolean canCreateInvisibleCompilationUnit(File file)
    {
        final ISourceFileHandler handler = getHandler(file);
        if (handler == null)
            return false;
        return handler.canCreateInvisibleCompilationUnit();
    }
}
