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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Commandline;

import org.apache.royale.compiler.ant.config.IOptionSource;
import org.apache.royale.compiler.ant.config.OptionSpec;

/**
 * Implements &lt;default-size&gt;.
 */
public class DefaultSize implements IOptionSource
{
    public static final OptionSpec DEFAULT_SIZE = new OptionSpec("default-size");

    private int width = -1;
    private int height = -1;

    public void setWidth(int val)
    {
        if (val <= 0)
            throw new BuildException("width attribute must be a positive integer!");

        width = val;
    }

    public void setHeight(int val)
    {
        if (val <= 0)
            throw new BuildException("height attribute must be a positive integer!");

        height = val;
    }

    public void addToCommandline(Commandline cmdline)
    {
        if (width == -1)
        {
            throw new BuildException("width attribute must be set!");
        }
        else if (height == -1)
        {
            throw new BuildException("height attribute must be set!");
        }
        else
        {
            cmdline.createArgument().setValue("-" + DEFAULT_SIZE.getFullName());
            cmdline.createArgument().setValue(String.valueOf(width));
            cmdline.createArgument().setValue(String.valueOf(height));
        }
    }
}
