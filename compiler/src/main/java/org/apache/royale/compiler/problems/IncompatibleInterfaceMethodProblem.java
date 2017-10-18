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

import org.apache.royale.compiler.definitions.IDefinition;

/**
 * Problem generated when a method in a class has an incompatible signature with a matching method from an interface
 * that it implements.
 */
public final class IncompatibleInterfaceMethodProblem extends CodegenProblem
{
    public static final String DESCRIPTION =
        "${INTERFACE} method ${methodName} in ${INTERFACE} ${interfaceName} is implemented with an incompatible signature in ${CLASS} ${className}";

    public static final int errorCode = 1144;

    public IncompatibleInterfaceMethodProblem(IDefinition site, String methodName, String interfaceName, String className)
    {
        super(site);
        this.methodName = methodName;
        this.interfaceName = interfaceName;
        this.className = className;
    }
    
    public String methodName;
    public String interfaceName;
    public String className;
    
    // Prevent these from being localized.
    public final String INTERFACE = "interface";
    public final String CLASS = "class";
}
