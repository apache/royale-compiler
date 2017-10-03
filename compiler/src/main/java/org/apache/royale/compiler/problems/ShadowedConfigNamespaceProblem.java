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
 * Diagnostic emitted when a normal namespace is given the same name as a config namespace
 */
public class ShadowedConfigNamespaceProblem extends ParserProblem
{
    public static final String DESCRIPTION = "Namespace ${namespace} conflicts with a configuration namespace.";

    public static final int errorCode = 1211;

    public final String namespace;

    public ShadowedConfigNamespaceProblem (ISourceLocation site, String namespace)
    {
        super(site);
        this.namespace = namespace;
    }
}
