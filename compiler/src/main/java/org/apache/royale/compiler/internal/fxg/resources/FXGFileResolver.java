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

package org.apache.royale.compiler.internal.fxg.resources;

import org.apache.royale.compiler.fxg.resources.IFXGResourceResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Simple implementation of ResourceResolver to locate and load local files
 * from a specified path. This path may be resolved relative from an
 * additionally configured root path.
 */
public class FXGFileResolver implements IFXGResourceResolver
{
    private String rootPath;

    public FXGFileResolver(String dir)
    {
        rootPath = dir;
    }
    
    public FXGFileResolver()
    {        
    }

    @Override
    public String getRootPath()
    {
        return rootPath;
    }

    @Override
    public void setRootPath(String dir)
    {
        rootPath = dir;
    }

    @Override
    public String resolve(String path)
    {
        File file = new File(path);
        if (!file.isAbsolute())
            file = new File(rootPath, path);

        return file.getAbsolutePath();
    }
    
    @Override
    public InputStream openStream(String path) throws IOException
    {
        File file = new File(path);
        if (!file.isAbsolute())
            file = new File(rootPath, path);

        FileInputStream fis = new FileInputStream(file);
        return fis;
    }

    @Override
    public InputStream openStream(URL url) throws IOException
    {
        return url.openStream();
    }
}
