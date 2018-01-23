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

import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;

/**
 * This problem gets created when the sampling rate of an embedded MP3 asset
 * is not supported.  Only 11025, 22050 and 44100 are supported.
 */
public final class EmbedUnsupportedSamplingRateProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "The frequency ${frequency} is not supported in file ${filename}";

    public static final int errorCode = 1362;
    public EmbedUnsupportedSamplingRateProblem(EmbedData data, int frequency)
    {
        super();
        this.frequency = frequency;
        this.filename = (String)data.getAttribute(EmbedAttribute.SOURCE);
    }

    public final int frequency;
    public final String filename;
}
