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

package org.apache.royale.compiler.ant.types;

import java.io.File;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

import org.apache.royale.compiler.ant.config.IOptionSource;
import org.apache.royale.compiler.ant.config.OptionSpec;

/**
 * Adds support for setting Flex configuration options using Ant FileSets.  For example:
 * <code>
 *     &lt;library-path dir="${FLEX_HOME}/libs" includes="royale.swc" append="true"/&gt;
 * </code>
 */
public class FlexFileSet extends FileSet implements IOptionSource
{
    public FlexFileSet()
    {
        this(false);
    }

    public FlexFileSet(OptionSpec spec)
    {
        this(spec, false);
    }

    public FlexFileSet(boolean dirs)
    {
        spec = null;
        includeDirs = dirs;
        append = false;
    }

    public FlexFileSet(OptionSpec spec, boolean dirs)
    {
        this.spec = spec; 
        includeDirs = dirs;
        append = false;
    }

    protected final OptionSpec spec;
    protected final boolean includeDirs;

    protected boolean append;

    public void setAppend(boolean value)
    {
        append = value;
    }

    public void addToCommandline(Commandline cmdline)
    {
        if (hasSelectors() || hasPatterns())
        {
            DirectoryScanner scanner = getDirectoryScanner(getProject());

            if (includeDirs)
                addFiles(scanner.getBasedir(), scanner.getIncludedDirectories(), cmdline);

            addFiles(scanner.getBasedir(), scanner.getIncludedFiles(), cmdline);
        }
        else if (spec != null)
        {
            cmdline.createArgument().setValue("-" + spec.getFullName() + equalString());
        }
    }

    protected void addFiles(File base, String[] files, Commandline cmdline)
    {
        FileUtils utils = FileUtils.getFileUtils();

        if (spec == null)
        {
            for (int i = 0; i < files.length; i++)
            {
                cmdline.createArgument().setValue(utils.resolveFile(base, files[i]).getAbsolutePath());
            }
        }
        else
        {
            for (int i = 0; i < files.length; i++)
            {
                cmdline.createArgument().setValue("-" + spec.getFullName() + equalString() +
                                                  utils.resolveFile(base, files[i]).getAbsolutePath());
            }
        }
    }

    protected String equalString()
    {
        return append ? "+=" : "=";
    }
}
