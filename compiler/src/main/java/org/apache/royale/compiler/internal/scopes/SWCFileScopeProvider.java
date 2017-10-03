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

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.definitions.IDocumentableDefinition;
import org.apache.royale.compiler.internal.abc.ABCScopeBuilder;
import org.apache.royale.compiler.internal.projects.ASProject;
import org.apache.royale.compiler.internal.projects.LibraryPathManager;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IFileScopeProvider;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;

import com.google.common.base.Strings;

/**
 * Creates {@code ASFileScope} objects for byte code in SWC library. The result
 * is used by {@link ABCScopeBuilder}.
 */
public final class SWCFileScopeProvider implements IFileScopeProvider
{

    /**
     * This class is able to resolve the source file path of a given QName
     * defined in a SWC library, if the source directory is set for that SWC
     * library.
     */
    public static final class SWCFileScope extends ASFileScope
    {
        public SWCFileScope(IWorkspace workspace, String filePath)
        {
            super(workspace, filePath);
        }

        @Override
        public String getContainingSourcePath(final String qName, final ICompilerProject project)
        {
            assert !Strings.isNullOrEmpty(qName) : "Expected QName.";
            assert project != null;

            // Only ASProject and its descendents support
            // source attachments to SWCs.
            if (!(project instanceof ASProject))
                return null;

            final ASProject flashProject = (ASProject)project;
            final String swcFilePath = filePath;

            String attachedSourceDirectory = flashProject.getAttachedSourceDirectory(swcFilePath);
            if (attachedSourceDirectory == null)
                return null;
            return LibraryPathManager.getAttachedSourceFilename(attachedSourceDirectory, qName);
        }

        public IASDocComment getComment(ICompilerProject project, IDocumentableDefinition def)
        {
            if (!(project instanceof ASProject))
                return null;
            ASProject flashProject = (ASProject)project;
            return flashProject.getASDocBundleDelegate().getComment(def, getContainingPath());
        }

        @Override
        public boolean isSWC()
        {
            return true;
        }

        @Override
        public boolean setCompilationUnit(ICompilationUnit compilationUnit)
        {
            assert (compilationUnit == null || compilationUnit instanceof SWCCompilationUnit) : "non SWCCompilationUnit passed to SWCFileScopeProvider";
            return false;
        }

        @Override
        public ICompilationUnit getCompilationUnit()
        {
            return null;
        }
    }
    
    private static final SWCFileScopeProvider instance = new SWCFileScopeProvider();

    /**
     * @return Singleton object of this class.
     */
    public static SWCFileScopeProvider getInstance()
    {
        return instance;
    }

    /**
     * Hide constructor for singleton class.
     */
    private SWCFileScopeProvider()
    {
    }

    @Override
    public ASFileScope createFileScope(IWorkspace workspace, String filePath)
    {
        return new SWCFileScope(workspace, filePath);
    }
}
