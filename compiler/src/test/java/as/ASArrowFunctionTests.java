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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class ASArrowFunctionTests extends ASFeatureTestsBase
{
    @Test
    public void testArrowFunctionNotAllowedError_withAllowArrowFunctionsDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            // error because arrow functions have not been enabled
			"var f:Function = (name:String) => 'Hi, ' + name;"
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=false"
        };
        compileAndExpectErrors(source, false,false,false, options,
            "'=>' is not allowed here\n");
    }

    @Test
    public void testCallArrowFunctionNoBraces_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var f:Function = (name:String) => 'Hi, ' + name;",
			"assertEqual('arrow function call', f('Royale'), 'Hi, Royale');",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndRun(source, false, false, false, options);
    }

    @Test
    public void testCallArrowFunctionWithBracesAndReturnType_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var f:Function = (name:String):String => {",
			"    var result:String = 'Hi, ' + name;\n",
			"    return result;\n",
			"}",
			"assertEqual('arrow function call', f('Royale'), 'Hi, Royale');",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndRun(source, false, false, false, options);
    }

    @Test
    public void testCallArrowFunctionWithBracesNoReturn_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var f:Function = (name:String) => {",
			"    'Hi, ' + name;\n",
			"}",
			"assertEqual('arrow function call', f('Royale'), undefined);",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndRun(source, false, false, false, options);
    }

    @Test
    public void testArrowFunctionThis_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var self:Object = this;",
			"var f:Function = () => this;",
			"assertEqual('arrow function this value', this, self);",
            // ensure that it's the class 'this' and not global 'this'
            "var f2:Function = (function():Object {return this;});",
			"assertEqual('arrow function this value', this !== f2(), true);",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndRun(source, false, false, false, options);
    }

    @Test
    public void testArrowFunctionNoBracesVoidReturn_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var f:Function = ():void => 123.4;",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndExpectErrors(source, false,false,false, options,
                "A return value is not allowed because the return type of this function is 'void'.\n");
    }

    @Test
    public void testArrowFunctionWithReturnKeyword_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            // error because statements aren't allowed
			"var f:Function = () => return 'Royale';",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndExpectErrors(source, false,false,false, options,
                "'return' is not allowed here\n" +
                "return value for function '' has no type declaration.\n" +
                "Function does not have a body.\n" + 
                "Access of possibly undefined property return.\n");
    }

    @Test
    public void testArrowFunctionNewLineBeforeArrow_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var f:Function = ()\n",
            "    => this;",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndExpectErrors(source, false,false,false, options,
            "'=>' is not allowed here\n");
    }

    @Test
    public void testArrowFunctionWithOperatorOfHigherPrecendent_NoParentheses_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var f:Function;\n",
            "f = f || () => this;",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndExpectErrors(source, false,false,false, options,
            "'=>' is not allowed here\n");
    }

    @Test
    public void testArrowFunctionWithOperatorOfHigherPrecendent_WithParentheses_withAllowArrowFunctionsEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
			"var f:Function;\n",
            "f = f || (() => this);\n",
			"assertEqual('arrow function operator precedence', f(), this);",
        };
        String[] extra = new String[]
        {
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-arrow-functions=true"
        };
        compileAndRun(source, false, false, false, options);
    }
}
