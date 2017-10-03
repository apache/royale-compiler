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

package org.apache.royale.compiler.utils;

public class NodeJSUtils
{
    /**
     * Converts the name of a node module with dashes into a version in camel
     * case so that it can be a valid identifier.
     */
    public static String convertFromDashesToCamelCase(String moduleNameWithDashes)
    {
        String camelCaseModule = moduleNameWithDashes;
        int moduleIndex = camelCaseModule.indexOf("-");
        while (moduleIndex != -1 && moduleIndex < camelCaseModule.length() - 1)
        {
            camelCaseModule = camelCaseModule.substring(0, moduleIndex)
                    + camelCaseModule.substring(moduleIndex + 1, moduleIndex + 2).toUpperCase()
                    + camelCaseModule.substring(moduleIndex + 2);
            moduleIndex = camelCaseModule.indexOf("-");
        }
        return camelCaseModule;
    }
}
