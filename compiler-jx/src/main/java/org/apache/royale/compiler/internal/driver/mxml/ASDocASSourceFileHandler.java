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

package org.apache.royale.compiler.internal.driver.mxml;

import org.apache.royale.compiler.internal.driver.js.goog.ASDocConfiguration;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.units.ASCompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Implementation of ISourceFileHandler that constructs
 * {@link ASCompilationUnit}'s. MXMLSourceFileHandler is the SourceFileHandler
 * that provides JSCompilationUnit for *.mxml files. JSDriver registers
 * MXMLSourceFileHandler at RoyaleApplicationProject. This implementation is part
 * of RoyaleJS. For more details on RoyaleJS see
 * org.apache.royale.compiler.JSDriver
 */
public final class ASDocASSourceFileHandler implements ISourceFileHandler
{

    public static final String EXTENSION = "as"; //$NON-NLS-1$
    public static final ASDocASSourceFileHandler INSTANCE = new ASDocASSourceFileHandler();

    private ASDocASSourceFileHandler()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getExtensions()
    {
        return new String[] { EXTENSION };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompilationUnit createCompilationUnit(CompilerProject proj,
            String path, DefinitionPriority.BasePriority basePriority,
            int order, String qname, String locale)
    {
        return new ASCompilationUnit(proj, path, basePriority, order, qname);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needCompilationUnit(CompilerProject project, String path,
            String qname, String locale)
    {
    	RoyaleJSProject fproject = (RoyaleJSProject) project;
    	ASDocConfiguration config = (ASDocConfiguration) fproject.configurator.getConfiguration();
        path = path.replace("\\", "/");
    	return !(config.getExcludeSources().contains(path));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canCreateInvisibleCompilationUnit()
    {
        return false;
    }
}
