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
public class ASExpressionTests extends ASFeatureTestsBase
{
    @Test
    public void ASExpressionTests_simpleTernary()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        	"public static const bool1:Boolean = false",
            "public static const foo:String = bool1 ? 'foo' : ''",
        };
        String[] testCode = new String[]
        {
            "assertEqual('empty string', foo, '');",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASExpressionTests_nestedTernary()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        	"public static const bool1:Boolean = false",
        	"public static const bool2:Boolean = true",
            "public static const foo:String = (bool1 ? bool2 : !bool2) ? 'foo' : ''",
        };
        String[] testCode = new String[]
        {
            "assertEqual('empty string', foo, '');",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASExpressionTests_returnNestedTernary()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        	"public static function returner():Number {",
        	"    var bool1:Boolean = false",
        	"    var now:Date = new Date()",
        	"    if (now.getFullYear() < 2016)",
        	"        bool1 = true",
        	"    var bool2:Boolean = true",
        	"    var now2:Date = new Date()",
        	"    if (now2.getFullYear() < 2016)",
        	"        bool2 = false",
        	"    return (bool1 ? !bool2 : bool2) ? 1 : 0",
        	"}",
            "public static var foo:Number = returner()",
        };
        String[] testCode = new String[]
        {
            "assertEqual('static number', foo, 1);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }
}
