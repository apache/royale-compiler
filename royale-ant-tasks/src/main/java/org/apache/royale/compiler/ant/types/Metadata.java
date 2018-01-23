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
import org.apache.tools.ant.DynamicElement;
import org.apache.tools.ant.types.Commandline;

import org.apache.royale.compiler.ant.FlexTask;
import org.apache.royale.compiler.ant.config.ConfigString;
import org.apache.royale.compiler.ant.config.NestedAttributeElement;
import org.apache.royale.compiler.ant.config.IOptionSource;
import org.apache.royale.compiler.ant.config.OptionSpec;

/**
 * Supports the nested &lt;metadata&gt; tag.
 */
public final class Metadata implements IOptionSource, DynamicElement
{
    private static final OptionSpec LOCALIZED_DESCRIPTION = new OptionSpec("metadata.localized-description");
    private static final OptionSpec LOCALIZED_TITLE = new OptionSpec("metadata.localized-title");

    private static final OptionSpec CONTRIBUTOR = new OptionSpec("metadata.contributor");
    private static final OptionSpec CREATOR = new OptionSpec("metadata.creator");
    private static final OptionSpec LANGUAGE = new OptionSpec("metadata.language");
    private static final OptionSpec PUBLISHER = new OptionSpec("metadata.publisher");

    public Metadata()
    {
        this(null);
    }

    public Metadata(FlexTask task)
    {
        date = new ConfigString(new OptionSpec("metadata", "date"));
        description = new ConfigString(new OptionSpec("metadata", "description"));
        title = new ConfigString(new OptionSpec("metadata", "title"));

        nestedAttribs = new ArrayList<NestedAttributeElement>();
        this.task = task;
    }

    private final ConfigString date;
    private final ConfigString description;
    private final ConfigString title;

    private final ArrayList<NestedAttributeElement> nestedAttribs;
    private final FlexTask task;
    
    /*=======================================================================*
     *  Attributes                                                           *
     *=======================================================================*/

    public void setDate(String value)
    {
        date.set(value);
    }

    public void setDescription(String value)
    {
        description.set(value);
    }

    public void setTitle(String value)
    {
        title.set(value);
    }

    /*=======================================================================*
     *  Nested Elements
     *=======================================================================*/

    public NestedAttributeElement createContributor()
    {
        return createElem("name", CONTRIBUTOR);
    }

    public NestedAttributeElement createCreator()
    {
        return createElem("name", CREATOR);
    }

    public NestedAttributeElement createLanguage()
    {
        return createElem("code", LANGUAGE);
    }

    public NestedAttributeElement createPublisher()
    {
        return createElem("name", PUBLISHER);
    }

    public Object createDynamicElement(String name)
    {
        /*
         * Name is checked against getAlias() because both of these options
         * have prefixes. We don't want to allow something like:
         *
         * <metadata>
         *   <metadata.localized-title title="foo" lang="en" />
         * </metadata>
         */
        if (LOCALIZED_DESCRIPTION.matches(name))
        {
            return createElem(new String[] { "text", "lang" }, LOCALIZED_DESCRIPTION);
        }
        else if (LOCALIZED_TITLE.matches(name))
        {
            return createElem(new String[] { "title", "lang" }, LOCALIZED_TITLE);
        }
        else
        {
            throw new BuildException("Invalid element: " + name);
        }
    }

    private NestedAttributeElement createElem(String attrib, OptionSpec spec)
    {
        NestedAttributeElement e = new NestedAttributeElement(attrib, spec, task);
        nestedAttribs.add(e);
        return e;
    }

    private NestedAttributeElement createElem(String[] attribs, OptionSpec spec)
    {
        NestedAttributeElement e = new NestedAttributeElement(attribs, spec, task);
        nestedAttribs.add(e);
        return e;
    }

    /*=======================================================================*
     *  IOptionSource interface                                               *
     *=======================================================================*/

    public void addToCommandline(Commandline cmdline)
    {
        date.addToCommandline(cmdline);
        description.addToCommandline(cmdline);
        title.addToCommandline(cmdline);

        Iterator<NestedAttributeElement> it = nestedAttribs.iterator();

        while (it.hasNext())
        {
            ((IOptionSource) it.next()).addToCommandline(cmdline);
        }
    }
}
