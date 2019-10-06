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
 * Problem generated when a class does not implement an abstract method from one
 * of its superclasses.
 */
public final class UnimplementedAbstractMethodProblem extends CodegenProblem
{
    public static final String DESCRIPTION =
        "Method ${methodName} in ${ABSTRACT} ${CLASS} ${abstractClassName} not implemented by ${CLASS} ${className}";

    public static final int errorCode = 1044;

    public UnimplementedAbstractMethodProblem(IDefinition site, String methodName, String abstractClassName, String className)
    {
        super(site);
        this.methodName = methodName;
        this.abstractClassName = abstractClassName;
        this.className = className;
    }
    
    public final String methodName;
    public final String abstractClassName;
    public final String className;

    // Prevent these from being localized.
    public final String ABSTRACT = "abstract";
    public final String CLASS = "class";
}
