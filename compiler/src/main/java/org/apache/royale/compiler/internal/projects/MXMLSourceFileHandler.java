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

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.internal.units.MXMLCompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Implementation of ISourceFileHandler that can constructs
 * {@link MXMLSourceFileHandler}'s.
 */
public final class MXMLSourceFileHandler implements ISourceFileHandler
{
    public static final String EXTENSION = "mxml";
    
    public static final MXMLSourceFileHandler INSTANCE = new MXMLSourceFileHandler();
    
    private MXMLSourceFileHandler()
    {
    }
    
    @Override
    public String[] getExtensions()
    {
        return new String[] { EXTENSION };
    }

    @Override
    public ICompilationUnit createCompilationUnit(CompilerProject project,
                                                  String path,
                                                  DefinitionPriority.BasePriority basePriority,
                                                  int order,
                                                  String qname, 
                                                  String locale)
    {
        // MXML files on the source path have a non-null qname,
        // but those in the source list have a null qname.
        if (qname == null)
            qname = FilenameUtils.getBaseName(path);
                
        return new MXMLCompilationUnit(project, path, basePriority, order, qname);
    }

    @Override
    public boolean needCompilationUnit(CompilerProject project, String path, String qname, String locale)
    {
        return true;
    }

    @Override
    public boolean canCreateInvisibleCompilationUnit()
    {
        return true;
    }
}
