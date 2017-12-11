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

package org.apache.royale.compiler.workspaces;

import java.util.Collection;
import java.util.Map;

import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Listener interface to allow clients of {@link IWorkspace} to be notified when
 * definitions visible across compilation unit boundaries are invalidated in a
 * workspace. Clients are notified by definition name ( current String
 * containing dotted qualified names ).
 */
public interface IInvalidationListener
{
    /**
     * Utility class to store information about
     * each invalidated definition
     */
    static final class InvalidatedDefinition
    {
        private final String qName;
        private final String filename;

        public InvalidatedDefinition(String qName, String filename)
        {
            this.qName = qName;
            this.filename = filename;
        }

        public String getQName()
        {
            return qName;
        }

        public String getFilename()
        {
            return filename;
        }

        /**
         * For debugging only.
         */
        @Override
        public String toString()
        {
            return filename + ":" + qName;
        }
    }
    
    /**
     * Called before any compilation units have been cleaned, but after the
     * complete set of compilation units to be cleaned has been computed.
     * 
     * @param changedDefinitions Map from {@link ICompilerProject} to a
     * collection of InvalidatedDefinitions for global definitions that are
     * defined in {@link ICompilationUnit}'s that are about to be cleaned.
     */
    void definitionsChanged(Map<ICompilerProject, Collection<InvalidatedDefinition>> changedDefinitions);
}
