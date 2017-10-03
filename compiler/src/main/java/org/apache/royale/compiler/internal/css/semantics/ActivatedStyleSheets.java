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

package org.apache.royale.compiler.internal.css.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.internal.projects.LibraryPathManager;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A container for all activated CSS models. It can sort the CSS models by
 * priority.
 * <ol>
 * <li>CSS files from {@code defaults-css-files} configuration option.</li>
 * <li>"defaults.css" in SWC libraries on the library paths.</li>
 * <li>Theme CSS and SWC.</li>
 * </ol>
 */
public class ActivatedStyleSheets
{
    public ActivatedStyleSheets()
    {
        defaults = new ArrayList<ICSSDocument>();
        themes = new ArrayList<ICSSDocument>();
        libraries = new HashMap<ICSSDocument, String>();
        comparator = new Comparator<ICSSDocument>()
        {
            /**
             * Sort CSS model in their SWC filenames' alphabetic order. If we
             * need to sort in the define order of library path,
             * {@link LibraryPathManager} need to provide the comparator.
             */
            @Override
            public int compare(ICSSDocument o1, ICSSDocument o2)
            {
                final String swcFile1 = libraries.get(o1);
                final String swcFile2 = libraries.get(o2);
                return swcFile1.compareTo(swcFile2);
            }
        };
    }

    /**
     * CSS models from {@code defaults-css-files} option.
     */
    private final List<ICSSDocument> defaults;

    /**
     * CSS models from theme files.
     */
    private final List<ICSSDocument> themes;

    /**
     * CSS models from "defaults.css" files in SWC libraries.
     */
    private final Map<ICSSDocument, String> libraries;

    /**
     * Sort {@link #libraries} on their paths by alphabetical order.
     */
    private final Comparator<ICSSDocument> comparator;

    /**
     * Activate a default CSS model.
     * 
     * @param css CSS model.
     */
    public void addDefaultCSS(final ICSSDocument css)
    {
        assert css != null : "defaults css can't be null";
        defaults.add(css);
    }

    /**
     * Activate a theme CSS model.
     * 
     * @param css CSS model.
     */
    public void addThemeCSS(final ICSSDocument css)
    {
        assert css != null : "theme css can't be null";
        themes.add(css);
    }

    /**
     * Activate a library CSS model.
     * 
     * @param css CSS model.
     * @param path Filename of SWC library that contains the CSS.
     */
    public void addLibraryCSS(final ICSSDocument css, final String path)
    {
        assert css != null : "librarycss can't be null";
        libraries.put(css, FilenameNormalization.normalize(path));
    }

    /**
     * @return A list of all activated CSS model sorted by precedence.
     */
    public List<ICSSDocument> sort()
    {
        // Sort CSS models from SWC libraries by library path order.
        final List<ICSSDocument> librariesSorted = new ArrayList<ICSSDocument>(libraries.keySet());
        Collections.sort(librariesSorted, comparator);

        final ImmutableList.Builder<ICSSDocument> builder = new ImmutableList.Builder<ICSSDocument>();
        builder.addAll(defaults);
        builder.addAll(librariesSorted);
        builder.addAll(themes);
        return builder.build();
    }

    /**
     * @return All activated CSS models.
     */
    public Set<ICSSDocument> all()
    {
        final ImmutableSet.Builder<ICSSDocument> builder = new ImmutableSet.Builder<ICSSDocument>();
        builder.addAll(defaults);
        builder.addAll(libraries.keySet());
        builder.addAll(themes);
        return builder.build();
    }
}
