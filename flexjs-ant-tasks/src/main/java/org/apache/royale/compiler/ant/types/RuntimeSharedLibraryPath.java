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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.types.Commandline;

import org.apache.royale.compiler.ant.config.IOptionSource;
import org.apache.royale.compiler.ant.config.OptionSpec;

/**
 * Supports the nested &lt;runtime-shared-library-path&gt; tag.
 */
public final class RuntimeSharedLibraryPath implements IOptionSource, DynamicConfigurator
{
    private static final String RUNTIME_SHARED_LIBRARY_PATH = "-runtime-shared-library-path";
    private static final String PATH_ELEMENT = "path-element";

    private static final OptionSpec URL = new OptionSpec("url");

    public RuntimeSharedLibraryPath()
    {
    }

    private String pathElement;
    private List<URLElement> urlElements = new ArrayList<URLElement>();

    public void addToCommandline(Commandline cmdline)
    {
        cmdline.createArgument().setValue(RUNTIME_SHARED_LIBRARY_PATH);
        cmdline.createArgument().setValue(pathElement);

        Iterator<URLElement> it = urlElements.iterator();

        while (it.hasNext())
        {
            ((IOptionSource)it.next()).addToCommandline(cmdline);
        }
    }

    public Object createDynamicElement(String name)
    {
        URLElement result;

        if (URL.matches(name))
        {
            result = new URLElement();
            urlElements.add(result);
        }
        else
        {
            throw new BuildException("Invalid element: " + name);
        }

        return result;
    }

    public void setDynamicAttribute(String name, String value)
    {
        if (name.equals(PATH_ELEMENT))
        {
            pathElement = value;
        }
        else
        {
            throw new BuildException("The <rutime-shared-library-path> type doesn't support the \"" +
                                     name + "\" attribute.");
        }
    }
}
