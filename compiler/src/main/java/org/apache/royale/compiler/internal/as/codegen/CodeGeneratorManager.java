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

package org.apache.royale.compiler.internal.as.codegen;

/**
 * Class to manage access to the ICodeGenerator.
 *
 * Allows clients to register a different factory if they want to provide an alternate implementation.
 */
public class CodeGeneratorManager
{
    // default to ABCGenerator
    private static ICodeGeneratorFactory factory = ABCGenerator.getABCGeneratorFactory();

    /**
     * @return an instance of the code generator
     */
    public static ICodeGenerator getCodeGenerator ()
    {
        return factory.get();
    }

    /**
     * Make this class use the passed in factory to obtain references to the code generator
     * @param f  the factory to use to get the code generator
     */
    public static void setFactory (ICodeGeneratorFactory f)
    {
        factory = f;
    }
}
