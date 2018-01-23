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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.types.Commandline;

import org.apache.royale.compiler.ant.FlexTask;
import org.apache.royale.compiler.ant.config.ConfigBoolean;
import org.apache.royale.compiler.ant.config.ConfigString;
import org.apache.royale.compiler.ant.config.ConfigVariable;
import org.apache.royale.compiler.ant.config.NestedAttributeElement;
import org.apache.royale.compiler.ant.config.IOptionSource;
import org.apache.royale.compiler.ant.config.OptionSpec;

/**
 * Supports the nested &lt;fonts&gt; tag.
 */
public final class Fonts implements IOptionSource, DynamicConfigurator
{
    /*
     * Use this defintion of LANGUAGE_RANGE if you want to allow users to set the
     * compiler.fonts.languages.language-range by using a nested element named
     * languages.language-range:
     *
     * private static OptionSpec LANGUAGE_RANGE = new OptionSpec("compiler.fonts.languages.language-range", "languages.language-range");
     *
     * Note that using this will no longer allow users to set the option by
     * using a language-range nested element.
     */
    private static final OptionSpec LANGUAGE_RANGE =
    	new OptionSpec("compiler.fonts.languages.language-range");
    
    private static final OptionSpec MANAGERS =
    	new OptionSpec("compiler.fonts.managers");

    public Fonts()
    {
        this(null);
    }

    public Fonts(FlexTask task)
    {
        attribs = new ConfigVariable[]
        {
            new ConfigBoolean(new OptionSpec("compiler.fonts.flash-type")),
            new ConfigBoolean(new OptionSpec("compiler.fonts.advanced-anti-aliasing")),
            new ConfigString(new OptionSpec("compiler.fonts.local-fonts-snapshot")),
            new ConfigString(new OptionSpec("compiler.fonts.max-cached-fonts")),
            new ConfigString(new OptionSpec("compiler.fonts.max-glyphs-per-face"))
        };

        nestedAttribs = new ArrayList<NestedAttributeElement>();
        this.task = task;
    }
    
    private final ConfigVariable[] attribs;

    private final ArrayList<NestedAttributeElement> nestedAttribs;
    private final FlexTask task;

    /*=======================================================================*
     *  Attributes                                                           *
     *=======================================================================*/

    public void setDynamicAttribute(String name, String value)
    {
        ConfigVariable var = null;

        for (int i = 0; i < attribs.length && var == null; i++)
        {
            if (attribs[i].matches(name))
                var = attribs[i];
        }

        if (var != null)
        {
            var.set(value);
        }
        else
        {
            throw new BuildException("The <font> type doesn't support the \""
                                     + name + "\" attribute.");
        }
    }

    /*=======================================================================*
     *  Nested Elements                                                      *
     *=======================================================================*/

    public Object createDynamicElement(String name)
    {
        if (LANGUAGE_RANGE.matches(name))
        {
            NestedAttributeElement e = new NestedAttributeElement(new String[] { "lang", "range" }, LANGUAGE_RANGE, task);
            nestedAttribs.add(e);
            return e;
        }
        else
        {
            throw new BuildException("Invalid element: " + name);
        }
    }

    public NestedAttributeElement createManager()
    {
        NestedAttributeElement e = new NestedAttributeElement("class", MANAGERS, task);
        nestedAttribs.add(e);
        return e;
    }

    /*=======================================================================*
     *  IOptionSource interface                                               *
     *=======================================================================*/

    public void addToCommandline(Commandline cmdline)
    {
        for (int i = 0; i < attribs.length; i++)
        {
            attribs[i].addToCommandline(cmdline);
        }

        Iterator<NestedAttributeElement> it = nestedAttribs.iterator();

        while (it.hasNext())
        {
            ((IOptionSource)it.next()).addToCommandline(cmdline);
        }
    }
}
