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

package org.apache.royale.compiler.clients;

import java.io.File;
import java.util.Arrays;

import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.CannotOpen;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
import org.apache.royale.utils.FilenameNormalization;

public class PlayerglobalcConfiguration extends Configuration
{
    private File asdocRoot;
    private File asRoot;

    public PlayerglobalcConfiguration()
    {
    }

    public File getAsRoot()
    {
        return asRoot;
    }

    @Config
    @Mapping("as-root")
    public void setASRoot(ConfigurationValue cfgval, String filename) throws CannotOpen
    {
        setASRoot(new File(FilenameNormalization.normalize(getOutputPath(cfgval, filename))));
    }

    public void setASRoot(File file)
    {
        this.asRoot = file;
    }

    public File getASDocRoot()
    {
        return asdocRoot;
    }

    @Config
    @Mapping("asdoc-root")
    public void setASDocRoot(ConfigurationValue cfgval, String filename) throws ConfigurationException
    {
		assertThatAllPathsAreDirectories(Arrays.asList(filename), cfgval);
        this.asdocRoot = new File(FilenameNormalization.normalize(filename));
    }
}
