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

import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  Base class of all fatal errors.
 */
public abstract class FatalProblem extends CompilerProblem
{
    /**
     *  super-constructor uses the source position information
     *  in an IASNode at or near the fatal error site as 
     *  paramaters to the CompilerProblem super-constructor.
     *  @param site - an IASNode at or near the fatal problem site.
     */
    protected FatalProblem(IASNode site)
    {
        super(site);
    }

    /**
     * Constructor for when we have no location information.
     */
    protected FatalProblem()
    {
        super();
    }
 }
