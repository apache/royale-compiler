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

package org.apache.royale.compiler.internal.config;

import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Normalizes QNames.
 */
public class QNameNormalization
{
    /**
     * Normalize a full name to {@code foo.bar.Name} form.
     * 
     * @param name full name
     * @return Dot-separated QName.
     */
    public static String normalize(final String name)
    {
        return normalize(name, ".");
    }

    /**
     * Normalize a full name to QName with the given separator character.
     * 
     * @param name full name
     * @param separator separator character between tokens in the name
     * @return Normalized name with the given separator character.
     */
    public static String normalize(final String name, final String separator)
    {
        if (name == null)
            return "";
        // split on "\" "/" "." ":" " "
        return name.replaceAll("[\\.:\\\\/\\s]", separator);
    }

    /**
     * Normalize a collection of names to dot-separated QNames.
     * 
     * @return Dot-separated QNames.
     */
    public static Collection<String> normalize(final Collection<String> names)
    {
        if (names == null)
            return Collections.emptySet();
        
        return Collections2.transform(names, new Function<String, String>()
        {
            @Override
            public String apply(String name)
            {
                return normalize(name);
            }
        });
    }
}
