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

package as;

import org.junit.Test;

/**
 * Feature tests for AS Namespaces.
 */
public class ASVariableTests extends ASFeatureTestsBase
{
    @Test
    public void ASVariableTests_stringInitializeEmptyString()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "public static const foo:String = ''",
        };
        String[] testCode = new String[]
        {
            "assertEqual('empty string', foo, '');",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASVariableTests_stringInitializeNull()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "public static const foo:String = null",
        };
        String[] testCode = new String[]
        {
            "assertEqual('null', foo, null);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASVariableTests_stringInitializeUndefined()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "public static const foo:String = undefined",
        };
        String[] testCode = new String[]
        {
            "assertEqual('null', foo, null);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASVariableTests_AnyInitializeUndefined()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "public static const foo:* = undefined",
        };
        String[] testCode = new String[]
        {
            "assertEqual('null', foo, undefined);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

}
