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
 * Problem generated when a node does not support a child node.
 */
public final class FXGChildNodeNotSupportedProblem extends FXGProblem
{
    public static final String DESCRIPTION = "Child node is not supported by node '${parent}'.";
    
    public static final int errorCode = 1365;
    public final String parent;
    
    public FXGChildNodeNotSupportedProblem(String filePath, int line, int column, String parent)
    {
        super(filePath, line, column);
        this.parent = parent;
    }
}
