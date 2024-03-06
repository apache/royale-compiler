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

/**
 * Problem generated when a manifest file is invalid.
 */
public final class ManifestParsingProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "Manifest: Error loading namespace ${uri}: ${text}.";

    
    public ManifestParsingProblem(String text, String uri, String manifestFileName)
    {
        super(manifestFileName);
        this.text = text;
        this.uri = uri;
    }

    public String text;
    public String uri;
}
