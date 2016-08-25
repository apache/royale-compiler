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

    @Test
    public void ASVariableTests_localVarSameNameAsPrivateMethodError()
    {
        // all tests can assume that flash.display.Sprite
        // flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "private function isVertical():Boolean { return false; }",
        };
        String[] testCode = new String[]
        {
            // this threw an exception when the generated code
            // tried to call the value of the local var.
            // mxmlc will generate a call to the method
            // without require a this.isVertical to reference
            // the instance method.
            "var isVertical:Boolean = isVertical();",
            "assertEqual('null', isVertical, false);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndExpectErrors(source, false, false, false, new String[0],
                "Call to isVertical is not a function.\n");
    }

    @Test
    public void ASVariableTests_constIsClassCastFunction()
    {
        // all tests can assume that flash.display.Sprite
        // flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "private const innerClass:InnerClass = null;",
        };
        String[] testCode = new String[]
        {
            "if (false) { var test:Object = innerClass('foo')};",
        };
        String[] extraCode = new String[]
        {
            "class InnerClass",
            "{",
            "    public function InnerClass(obj:Object)",
            "    {",
            "    }",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extraCode);
        compileAndRun(source);
    }

    @Test
    public void ASVariableTests_localVarSameNameAsPrivateMethod()
    {
        // all tests can assume that flash.display.Sprite
        // flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "private function isVertical():Boolean { return false; }",
        };
        String[] testCode = new String[]
        {
            // this threw an exception when the generated code
            // tried to call the value of the local var.
            // mxmlc will generate a call to the method
            // without require a this.isVertical to reference
            // the instance method.
            "var isVertical:Boolean = isVertical();",
            "assertEqual('null', isVertical, false);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source, false, false, false, new String[]{ "-compiler.mxml.compatibility-version=4.6.0" } );
    }

    /*
    public void ASVariableTests_VectorInitializer()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
            "public static var arr:Array = Array('foo', 'bar', 'baz');",
            "public static var foo:Vector.<String> = new Vector.<String>('foo', 'bar', 'baz');",
            "public static var bar:Vector.<String> = Vector.<String>('foo', 'bar', 'baz');",
        };
        String[] testCode = new String[]
        {
            "assertEqual('length', foo.length, 3, 4);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }
    */
}
