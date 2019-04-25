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

public class ASPrivateConstructorTests extends ASFeatureTestsBase
{
    @Test
	public void testConstructorMustBePublicProblem_withPrivateConstructor_andAllowPrivateConstructorsDisabled()
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
			"class A {",
            //error because private constructors have not been enabled
			"private function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=false"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public\n");
	}

    @Test
	public void testConstructorMustBePublicProblem_withProtectedConstructor_andAllowPrivateConstructorsDisabled()
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
			"class A {",
            //error because protected constructors are not allowed
			"protected function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=false"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public\n");
	}

    @Test
	public void testConstructorMustBePublicProblem_withInternalConstructor_andAllowPrivateConstructorsDisabled()
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
			"class A {",
            //error because internal constructors are not allowed
			"internal function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=false"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public\n");
	}

    @Test
	public void testConstructorMustBePublicProblem_withMetadata_andAllowPrivateConstructorsDisabled()
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
			//error because private constructors have not been enabled
			"[RoyalePrivateConstructor]",
			"class A {",
			"public function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=false"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public\n");
    }

    @Test
	public void testConstructorMustBePublicProblemAndInaccessibleConstructorReferenceProblem_withPrivateConstructor_andAllowPrivateConstructorsDisabled()
    {
        String[] imports = new String[]
        {
		};
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            //no error because the constructor cannot be private
            "new A()"
        };
        String[] extra = new String[]
        {
			"class A {",
            //error because private constructors have not been enabled
			"private function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=false"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public\n");
	}

    @Test
	public void testConstructorMustBePublicProblemAndInaccessibleConstructorReferenceProblem_withMetadata_andAllowPrivateConstructorsDisabled()
    {
        String[] imports = new String[]
        {
		};
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            //no error because the constructor cannot be private
            "new A()"
        };
        String[] extra = new String[]
        {
            "[RoyalePrivateConstructor]",
			"class A {",
            //error because private constructors have not been enabled
			"private function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=false"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public\n");
	}
    
    @Test
	public void testNoConstructorMustBePublicOrPrivateProblem_withPrivateConstructor_andAllowPrivateConstructorsEnabled()
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
			"class A {",
			"private function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
	}

    @Test
	public void testConstructorMustBePublicOrPrivateProblem_withProtectedConstructor_andAllowPrivateConstructorsEnabled()
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
			"class A {",
            //error because protected constructors are not allowed
			"protected function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public or private\n");
	}

    @Test
	public void testConstructorMustBePublicOrPrivateProblem_withInternalConstructor_andAllowPrivateConstructorsEnabled()
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
			"class A {",
            //error because internal constructors are not allowed
			"internal function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "A constructor can only be declared public or private\n");
	}

    @Test
	public void testNoConstructorMustBePublicOrPrivateProblem_withMetadata_andAllowPrivateConstructorsEnabled()
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
			//error because private constructors have not been enabled
			"[RoyalePrivateConstructor]",
			"class A {",
			"public function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
    }

    @Test
	public void testInaccessibleConstructorReferenceProblem_withPrivateConstructor_andAllowPrivateConstructorsEnabled()
    {
        String[] imports = new String[]
        {
		};
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            //error because the constructor is private
            "new A()"
        };
        String[] extra = new String[]
        {
			"class A {",
			"private function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Attempted access of inaccessible constructor through a reference with static type A.\n");
	}

    @Test
	public void testInaccessibleConstructorReferenceProblem_withMetadata_andAllowPrivateConstructorsEnabled()
    {
        String[] imports = new String[]
        {
		};
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
            //error because the constructor is private
            "new A()"
        };
        String[] extra = new String[]
        {
			"[RoyalePrivateConstructor]",
			"class A {",
			"private function A() {",
			"}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Attempted access of inaccessible constructor through a reference with static type A.\n");
	}

    @Test
	public void testInaccessibleConstructorReferenceProblem_withPrivateConstructor_inFilePrivateArea_andAllowPrivateConstructorsEnabled()
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
			"class A {",
			"private function A() {",
			"}",
            "}",
            //error because the constructor is private
            "new A()"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Attempted access of inaccessible constructor through a reference with static type A.\n");
	}

    @Test
	public void testInaccessibleConstructorReferenceProblem_withMetadata_inFilePrivateArea_andAllowPrivateConstructorsEnabled()
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
			"[RoyalePrivateConstructor]",
			"class A {",
			"private function A() {",
			"}",
            "}",
            //error because the constructor is private
            "new A()"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        compileAndExpectErrors(source, false, false, false, options, "Attempted access of inaccessible constructor through a reference with static type A.\n");
	}

    @Test
	public void testNoInaccessibleConstructorReferenceProblem_withPrivateConstructor_inMethodOfSameClass_andAllowPrivateConstructorsEnabled()
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
            "class A {",
			"private function A() {",
			"}",
            "public function method():Object {",
            "return new A()",
            "}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
	}

    @Test
	public void testNoInaccessibleConstructorReferenceProblem_withMetadata_inMethodOfSameClass_andAllowPrivateConstructorsEnabled()
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
            "[RoyalePrivateConstructor]",
            "class A {",
			"private function A() {",
			"}",
            "public function method():Object {",
            "return new A()",
            "}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
	}

    @Test
	public void testNoInaccessibleConstructorReferenceProblem_withPrivateConstructor_inStaticMethodOfSameClass_andAllowPrivateConstructorsEnabled()
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
            "class A {",
			"private function A() {",
			"}",
            "public static function method():Object {",
            "return new A()",
            "}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
	}

    @Test
	public void testNoInaccessibleConstructorReferenceProblem_withMetadata_inStaticMethodOfSameClass_andAllowPrivateConstructorsEnabled()
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
            "[RoyalePrivateConstructor]",
            "class A {",
			"private function A() {",
			"}",
            "public static function method():Object {",
            "return new A()",
            "}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
	}

    @Test
	public void testNoInaccessibleConstructorReferenceProblem_withPrivateConstructor_inGetterOfSameClass_andAllowPrivateConstructorsEnabled()
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
            "class A {",
			"private function A() {",
			"}",
            "public function get getter():Object {",
            "return new A()",
            "}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
	}

    @Test
	public void testNoInaccessibleConstructorReferenceProblem_withMetadata_inGetterOfSameClass_andAllowPrivateConstructorsEnabled()
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
            "[RoyalePrivateConstructor]",
            "class A {",
			"private function A() {",
			"}",
            "public function get getter():Object {",
            "return new A()",
            "}",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);
        String[] options = new String[]
        {
            "-allow-private-constructors=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false, false, false, options, true);
        Assert.assertEquals("", result);
	}
}