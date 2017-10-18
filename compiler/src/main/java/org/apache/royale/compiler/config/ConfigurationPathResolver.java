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

package org.apache.royale.compiler.config;

import java.io.File;

import org.apache.royale.compiler.common.IPathResolver;
import org.apache.royale.utils.FilenameNormalization;

/**
 * A path resolver for configuration options.
 * 
 * Paths in configuration options will be resolved as follows:
 * 
 *  1. Paths in configuration files will be resolved relative to 
 *  the configuration file.
 *  2. Paths in the command line will be resolved relative to the
 *  where the compiler was launched.
 */
public class ConfigurationPathResolver implements IPathResolver
{
    /**
     * Constructor.
     * 
     * @param rootDirectory the root directory of compilation. May
     * not be null.
     * 
     * @throws NullPointerException if rootDirectory is null.
     */
    public ConfigurationPathResolver(String rootDirectory)
    {
        this.rootDirectory = rootDirectory;
        
        if (rootDirectory == null)
            throw new NullPointerException("rootDirectory may not be null");
        
    }

    private String rootDirectory;
    
    @Override
    public File resolve(String path)
    {
        return tryResolve(rootDirectory, path);
    }
    
    /**
     * Try to resolve a file in the given folder.
     * 
     * @param parent parent folder
     * @param name file name
     * @return File object that may or may not exist.
     */
    private File tryResolve(final String parent, final String name)
    {
        final File file = new File(name);
        if (file.isAbsolute())
            return FilenameNormalization.normalize(file);

        return FilenameNormalization.normalize(new File(parent, name));
    }
    
}
