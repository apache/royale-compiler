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

import org.apache.royale.compiler.filespecs.IFileSpecification;
import com.google.common.base.Function;

/**
 * Utilities for theme-related operations.
 */
public class ThemeUtilities
{
    private ThemeUtilities()
    {

    }

    /**
     * Get the theme name from a theme file path. For example:
     * <ul>
     * <li>{@code C:\workspace\project\themes\MyTheme.swc} - theme name is
     * "MyTheme"</li>
     * <li>{@code path/to/foo-1.0.0.css} - theme name is "foo"</li>
     * <li>{@code Spark.css} - theme name is "Spark"</li>
     * </ul>
     * 
     * @param filename Path of the theme file.
     * @return Theme name.
     */
    public static String getThemeName(final String filename)
    {
        if (filename == null)
            return null;

        final String baseName = FilenameUtils.getBaseName(filename);
        final int dashPosition = baseName.indexOf('-');
        if (dashPosition == -1)
            return baseName;
        else
            return baseName.substring(0, dashPosition);
    }
    
    /**
     * A function to get theme name from a theme file specification.
     */
    public static final Function<IFileSpecification, String> THEME_FILE_TO_NAME = new Function<IFileSpecification, String>()
    {
        @Override
        public String apply(IFileSpecification filespec)
        {
            return getThemeName(filespec.getPath());
        }
    };
}
