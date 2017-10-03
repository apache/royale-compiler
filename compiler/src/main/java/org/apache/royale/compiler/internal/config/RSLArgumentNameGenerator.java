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

package org.apache.royale.compiler.internal.config;

/**
 * Class that generates argument names for the 
 * -runtime-shared-library-path option.
 */
public class RSLArgumentNameGenerator
{
    /**
     * Converts a zero based index into an argument name.
     * @param argrumentNumber zero-based index of argument
     * @return name of argument
     */
    public static String getArgumentName(int argrumentNumber)
    {
        String argumentName = null;

        if (argrumentNumber == 0)
        {
            argumentName = "path-element";
        }
        else
        {
            argrumentNumber = (argrumentNumber + 1) % 2;
            if (argrumentNumber == 0)
                argumentName = "rsl-url";
            else
                argumentName = "policy-file-url";
        }
        return argumentName;
    }
    
}
