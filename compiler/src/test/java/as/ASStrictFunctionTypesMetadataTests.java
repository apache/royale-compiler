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

public class ASStrictFunctionTypesMetadataTests extends ASFeatureTestsBase
{
    @Test
    public void testWrongParameterType1_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"Number\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(Number):void to an unrelated type function(String):void.\n");
    }

    @Test
    public void testWrongParameterType1_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"Number\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testWrongParameterType2_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Boolean\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(String, Boolean):void to an unrelated type function(String, Number):void.\n");
    }

	@Test
    public void testWrongParameterType2_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Boolean\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean,...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean,...Array\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testWrongReturnType_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"Number\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function():Number to an unrelated type function():String.\n");
    }

	@Test
    public void testWrongReturnType_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"Number\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"\")]",
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignStrictTypeToRegular_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignStrictTypeToRegular_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"var a:Function;",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignRegularTypeToStrict_assignVariableToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String\")]",
			"var a:Function;",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type Function to an unrelated type function(String):void.\n");
    }

	@Test
    public void testAssignRegularTypeToStrict_assignVariableToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String\")]",
			"var a:Function;",
			"var b:Function;",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testWrongParameterType1_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String\")]",
			"var a:Function;",
			"function b(x:Number):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(Number):void to an unrelated type function(String):void.\n");
    }

	@Test
    public void testWrongParameterType1_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String\")]",
			"var a:Function;",
			"function b(x:Number):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testWrongParameterType2_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number\")]",
			"var a:Function;",
			"function b(x:String, y:Boolean):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(String, Boolean):void to an unrelated type function(String, Number):void.\n");
    }

	@Test
    public void testWrongParameterType2_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number\")]",
			"var a:Function;",
			"function b(x:String, y:Boolean):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, y:Number, z:Boolean):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, y:Number, z:Boolean):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, ...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, ...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, y:Number, ...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, y:Number, ...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, y:Number, z:Boolean, ...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var a:Function;",
			"function b(x:String, y:Number, z:Boolean, ...rest):void {}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testWrongReturnType_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"function b():Number {return 0;}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function():Number to an unrelated type function():String.\n");
    }

	@Test
    public void testWrongReturnType_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"function b():Number {return 0;}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"function b():String {return null;}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var a:Function;",
			"function b():String {return null;}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_assignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"\")]",
			"var a:Function;",
			"function b():String {return null;}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_assignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(returns=\"void\",params=\"\")]",
			"var a:Function;",
			"function b():String {return null;}",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignFunctionToVariable_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"var a:Function;",
			"function b(x:String):Number { return 0; }",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignFunctionToVariable_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"var a:Function;",
			"function b(x:String):Number { return 0; }",
			"a = b;"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testWrongParameterType1_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"Number\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(Number):void to an unrelated type function(String):void.\n");
    }

	@Test
    public void testWrongParameterType1_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"Number\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testWrongParameterType2_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"y\",returns=\"void\",params=\"String,Number\")]",
			"function a(x:String, y:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Boolean\")]",
			"var b:Function;",
			"a(\"hi\", b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(String, Boolean):void to an unrelated type function(String, Number):void.\n");
    }

	@Test
    public void testWrongParameterType2_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"y\",returns=\"void\",params=\"String,Number\")]",
			"function a(x:String, y:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Boolean\")]",
			"var b:Function;",
			"a(\"hi\", b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"yx\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean,...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"yx\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"void\",params=\"String,Number,Boolean,...Array\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testWrongReturnType_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"Number\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function():Number to an unrelated type function():String.\n");
    }

	@Test
    public void testWrongReturnType_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"Number\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"\")]",
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignStrictTypeToRegular_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignStrictTypeToRegular_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function a(x:Function):void {}",
			"[RoyaleFunctionType(returns=\"String\",params=\"\")]",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testAssignRegularTypeToStrict_passVariableToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String\")]",
			"function a(x:Function):void {}",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type Function to an unrelated type function(String):void.\n");
    }

	@Test
    public void testAssignRegularTypeToStrict_passVariableToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String\")]",
			"function a(x:Function):void {}",
			"var b:Function;",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testWrongParameterType1_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String\")]",
			"function a(x:Function):void {}",
			"function b(x:Number):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(Number):void to an unrelated type function(String):void.\n");
    }

	@Test
    public void testWrongParameterType1_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String\")]",
			"function a(x:Function):void {}",
			"function b(x:Number):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testWrongParameterType2_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Boolean):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function(String, Boolean):void to an unrelated type function(String, Number):void.\n");
    }

	@Test
    public void testWrongParameterType2_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Boolean):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Number, z:Boolean):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectParameterTypes_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Number, z:Boolean):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes1_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, ...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes2_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, ...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Number, ...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes3_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Number, ...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Number, z:Boolean, ...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testRestParameterInsteadOfCorrectParameterTypes4_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"String,Number,Boolean\")]",
			"function a(x:Function):void {}",
			"function b(x:String, y:Number, z:Boolean, ...rest):void {}",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false,false,false, options);
    }

	@Test
    public void testWrongReturnType_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"function b():Number { return 0; }",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Implicit coercion of a value of type function():Number to an unrelated type function():String.\n");
    }

	@Test
    public void testWrongReturnType_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"function b():Number { return 0; }",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"function b():String { return null; }",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testCorrectReturnType_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"String\",params=\"\")]",
			"function a(x:Function):void {}",
			"function b():String { return null; }",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_passFunctionToParameter_withAllowStrictFunctionTypesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"\")]",
			"function a(x:Function):void {}",
			"function b():String { return null; }",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=true"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }

	@Test
    public void testIgnoreReturnTypeForVoid_passFunctionToParameter_withAllowStrictFunctionTypesDisabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"[RoyaleFunctionType(paramName=\"x\",returns=\"void\",params=\"\")]",
			"function a(x:Function):void {}",
			"function b():String { return null; }",
			"a(b);"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-strict-function-types=false"
        };
        compileAndExpectNoErrors(source, false, false, false, options);
    }
}