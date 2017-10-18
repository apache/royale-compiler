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

package org.apache.royale.abc.visitors;

import org.apache.royale.abc.semantics.MethodInfo;

/**
 * An IScriptVisitor generates a visitor for the script's traits, and records the
 * script's init method.
 */
public interface IScriptVisitor extends IVisitor
{
    /**
     * Begin visiting the script.
     */
    void visit();

    /**
     * Define the script's traits.
     * 
     * @return the ITraitsVisitor that actually defines the traits.
     */
    ITraitsVisitor visitTraits();

    /**
     * Declare the script's init routine.
     * 
     * @param methodInfo - the MethodInfo of the init routine.
     */
    void visitInit(MethodInfo methodInfo);
}
