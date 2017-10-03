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
 * Problem generated when the supplied attribute is not supported by the node.
 */
public final class FXGInvalidNodeAttributeProblem extends FXGProblem
{   
    public static final String DESCRIPTION = "Attribute '${attribute}' not supported by node '${node}'.";
    public static final int errorCode = 1384;
    public final String attribute;
    public final String node;
    
    public FXGInvalidNodeAttributeProblem(String filePath, int line, int column, String attribute, String node)
    {
        super(filePath, line, column);
        this.node = node;
        this.attribute = attribute;
    }
}
