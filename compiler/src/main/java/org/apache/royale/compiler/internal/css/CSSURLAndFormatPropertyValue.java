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

package org.apache.royale.compiler.internal.css;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.apache.royale.compiler.css.ICSSDocument;

/**
 * @author aharui
 *
 */
public class CSSURLAndFormatPropertyValue extends CSSFunctionCallPropertyValue
{
    /**
     * Initialize a {@link CSSURLAndFormatPropertyValue}.
     * 
     * @param name Function name.
     * @param rawArguments Raw argument string with parentheses and quotes.
     * @param tree AST.
     * @param tokenStream Token stream.
     */
    public CSSURLAndFormatPropertyValue(final String name,
                                        final String rawArguments,
                                        final String format,
                                        final CommonTree tree,
                                        final TokenStream tokenStream)
    {
        super(name, rawArguments, tree, tokenStream);
        this.format = format;
    }

    private String format;
    
    /**
     * Generate CSS code fragment for this model object. This is used by
     * recursively generating a CSS document from a {@link ICSSDocument} for
     * debugging and testing purposes.
     */
    @Override
    public String toString()
    {
        if (format == null)
            return super.toString();
        return super.toString() + " " + format;
    }

}
