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
 * Problem generated when an element used in an FXG document cannot be resolved 
 * for the specified FXG version.
 */
public final class FXGUnknownElementInVersionProblem extends FXGProblem
{
    public static final String DESCRIPTION = "Element name '${element}' is unknown in FXG Version '${version}'";
    public static final int errorCode = 1404;
    public final double version;
    public final String element;
    
    public FXGUnknownElementInVersionProblem(String filePath, int line, int column, String element, double version)
    {
        super(filePath, line, column);
        this.element = element;
        this.version = version;
    }
}
