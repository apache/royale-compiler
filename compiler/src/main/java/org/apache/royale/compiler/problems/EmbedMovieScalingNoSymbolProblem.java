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

package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.common.ISourceLocation;

/**
 * This problem gets created when a scaling grid is used on an embedded movie
 * asset, but no symbol has been specified, so it is not clear when tag needs
 * to be scaled, as the whole swf is read in as a byte array asset.
 */
public final class EmbedMovieScalingNoSymbolProblem extends CompilerProblem
{
    public static final String DESCRIPTION = "Specify a '${SYMBOL}' when scaling an embedded movie";

    public static final int errorCode = 1343;
    public EmbedMovieScalingNoSymbolProblem(ISourceLocation site)
    {
        super(site);
    }
    
    // Prevent these from being localized.
    public final String SYMBOL = "symbol";
}
