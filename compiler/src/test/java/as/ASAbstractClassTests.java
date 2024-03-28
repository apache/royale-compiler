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

public class ASAbstractClassTests extends ASFeatureTestsBase
{
    @Test
    public void testAbstractNotAllowedOnClassError_withAllowAbstractClassesDisabled()
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
            //error because abstract classes have not been enabled
            "abstract class A {",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
    }
    
    @Test
    public void testAbstractNotAllowedOnInterfaceError_withAllowAbstractClassesDisabled()
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
            //error because abstract classes have not been enabled
            "abstract interface A {",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
    }

    @Test
    public void testAbstractNotAllowedOnClassMethodError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public abstract function a():void",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceMethodError_withAllowAbstractClassesDisabled()
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
					"interface A {",
					//error because abstract classes have not been enabled
					"abstract function a():void",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceStaticMethodError_withAllowAbstractClassesDisabled()
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
					"interface A {",
					//error because abstract classes have not been enabled
					"static abstract function a():void",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nThe static attribute may be used only on definitions inside a class.\n");
	}

	@Test
    public void testAbstractNotAllowedOnStaticMethodError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public static abstract function a():void",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractNotAllowedOnGetterError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\nFunction does not return a value.\n");
	}

	@Test
    public void testAbstractNotAllowedOnSetterError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractNotAllowedOnStaticGetterError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public static abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\nFunction does not return a value.\n");
	}

	@Test
    public void testAbstractNotAllowedOnStaticSetterError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public static abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractNotAllowedOnVariableError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

	@Test
    public void testAbstractNotAllowedOnStaticVariableError_withAllowAbstractClassesDisabled()
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
					//error because abstract classes have not been enabled
					"public static abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceGetterError_withAllowAbstractClassesDisabled()
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
					"interface A {",
					//error because abstract classes have not been enabled
					"abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceSetterError_withAllowAbstractClassesDisabled()
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
					"interface A {",
					//error because abstract classes have not been enabled
					"abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceStaticGetterError_withAllowAbstractClassesDisabled()
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
					"interface A {",
					//error because abstract classes have not been enabled
					"static abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nThe static attribute may be used only on definitions inside a class.\n");
	}

    @Test
    public void testAbstractNotAllowedOnInterfaceStaticSetterError_withAllowAbstractClassesDisabled()
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
					"interface A {",
					//error because abstract classes have not been enabled
					"static abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=false"
        };
        compileAndExpectErrors(source, false,false,false, options,"'abstract' is not allowed here\nThe static attribute may be used only on definitions inside a class.\n");
	}

	@Test
    public void testAbstractClassNoErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false,false,false, options, true);
        Assert.assertEquals("", result);
    }

	@Test
    public void testAbstractMethodOnClassNoErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractMethodWithParametersAndReturnOnClassNoErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a(arg0:String, arg1:Number):Boolean;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractMethodOnInterfaceError_withAllowAbstractClassesEnabled()
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
					"interface A {",
					//error because an abstract method may only be defined on an
					//abstract class (not an interface)
					"abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractGetterOnInterfaceError_withAllowAbstractClassesEnabled()
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
					"interface A {",
					//error because an abstract method may only be defined on an
					//abstract class (not an interface)
					"abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractSetterOnInterfaceError_withAllowAbstractClassesEnabled()
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
					"interface A {",
					//error because an abstract method may only be defined on an
					//abstract class (not an interface)
					"abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractStaticMethodOnInterfaceError_withAllowAbstractClassesEnabled()
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
					"interface A {",
					//error because an abstract method may only be defined on an
					//abstract class (not an interface)
					"static abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The static attribute may be used only on definitions inside a class.\nThe abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractStaticGetterOnInterfaceError_withAllowAbstractClassesEnabled()
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
					"interface A {",
					//error because an abstract method may only be defined on an
					//abstract class (not an interface)
					"static abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The static attribute may be used only on definitions inside a class.\nThe abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractStaticSetterOnInterfaceError_withAllowAbstractClassesEnabled()
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
					"interface A {",
					//error because an abstract method may only be defined on an
					//abstract class (not an interface)
					"static abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The static attribute may be used only on definitions inside a class.\nThe abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractProtectedMethodNoErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"protected abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractInternalMethodNoErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"internal abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractPrivateMethodError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because an abstract method cannot be private
					"private abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Methods that are abstract cannot be declared private.\n");
	}

	@Test
    public void testAbstractFinalClassError_withAllowAbstractClassesEnabled()
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
                    //error because an abstract class cannot be final
					"abstract final class A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractFinalMethodError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because an abstract method cannot be final
					"public final abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractMethodNotInAbstractClassError_withAllowAbstractClassesEnabled()
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
					//error because an abstract method may only be defined on an
					//abstract class
					"public abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractStaticMethodNotInAbstractClassError_withAllowAbstractClassesEnabled()
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
					//error because an abstract method may only be defined on an
					//abstract class
					"public static abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractGetterNotInAbstractClassError_withAllowAbstractClassesEnabled()
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
					//error because an abstract getter may only be defined on an
					//abstract class
					"public abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\nFunction does not return a value.\n");
	}

	@Test
    public void testAbstractSetterNotInAbstractClassError_withAllowAbstractClassesEnabled()
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
					//error because an abstract setter may only be defined on an
					//abstract class
					"public abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractStaticGetterNotInAbstractClassError_withAllowAbstractClassesEnabled()
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
					//error because an abstract getter may only be defined on an
					//abstract class
					"public static abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\nFunction does not return a value.\n");
	}

	@Test
    public void testAbstractStaticSetterNotInAbstractClassError_withAllowAbstractClassesEnabled()
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
					//error because an abstract setter may only be defined on an
					//abstract class
					"public static abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractStaticMethodError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because a static method cannot be abstract
					"public static abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractGetterNoError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false,false,false, options, true);
        Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractStaticGetterError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because a static getter cannot be abstract
					"public static abstract function get a():String;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\nFunction does not return a value.\n");
	}

	@Test
    public void testAbstractSetterNoError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
        String result = compile(tempASFile, source, false,false,false, options, true);
        Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractStaticSetterError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because a static setter cannot be abstract
					"public static abstract function set a(value:String):void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\nFunction does not have a body.\n");
	}

	@Test
    public void testAbstractVariableError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because a variable cannot be abstract
					"public abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractStaticVariableError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because a static variable cannot be abstract
					"public static abstract var a:Object;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
	}

	@Test
    public void testAbstractMethodBodyError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because an abstract method has a body
					"public abstract function a():void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method marked abstract must not have a body.\n");
	}

	@Test
    public void testAbstractGetterBodyError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because an abstract getter has a body
					"public abstract function get a():String {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method marked abstract must not have a body.\n");
	}

	@Test
    public void testAbstractSetterBodyError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					//error because an abstract getter has a body
					"public abstract function set a(value:String):void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method marked abstract must not have a body.\n");
	}

	@Test
    public void testAbstractClassNewOperatorError_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
					//error because the class is abstract and cannot be
					//instantiated with new
					"var obj:A = new A();",
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Abstract classes cannot be instantiated with the new operator.\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractNewOperatorNoErrors_withAllowAbstractClassesEnabled()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
					"var obj:A = new B();",
        };
        String[] extra = new String[]
        {
					"abstract class A {",
					"}",
					"class B extends A {",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractClassSuperMethodError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"class B extends A {",
					"override public function a():void {",
					//error because the super method is abstract
					"super.a();",
					"}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Call to a possibly undefined method a.\n");
	}

	@Test
    public void testAbstractMethodOverride_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					//error because the method already exists and is already abstract
					"override public abstract function a():void;",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Functions cannot be both abstract and override.\n");
	}

	@Test
    public void testAbstractClassSuperGetterError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function get a():String;",
					"}",
					"class B extends A {",
					"override public function get a():String {",
					//error because the super getter is abstract
					"return super.a;",
					"}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Access of possibly undefined property a.\n");
	}

	@Test
    public void testAbstractClassSuperSetterError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function set a(value:String):void;",
					"}",
					"class B extends A {",
					"override public function set a(value:String):void {",
					//error because the super setter is abstract
					"super.a = value;",
					"}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Access of possibly undefined property a.\n");
	}

	@Test
    public void testAbstractClassMethodNotImplementedError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"class B extends A {",
					//error because we did not implement the abstract method in
					//a concrete subclass
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method a in abstract class A not implemented by class B\n");
	}

	@Test
    public void testAbstractClassGetterNotImplementedError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function get a():String;",
					"}",
					"class B extends A {",
					//error because we did not implement the abstract getter in
					//a concrete subclass
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method get a in abstract class A not implemented by class B\n");
	}

	@Test
    public void testAbstractClassSetterNotImplementedError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function set a(value:String):void;",
					"}",
					"class B extends A {",
					//error because we did not implement the abstract setter in
					//a concrete subclass
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method set a in abstract class A not implemented by class B\n");
	}

	@Test
    public void testAbstractClassNotImplementedErrorOneTimeForTwoConcreteSubclasses_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
                    "class B extends A {",
					//error because we did not implement the abstract method in
					//a concrete subclass
                    "}",
                    "class C extends B {",
                    //no duplicate error here!
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Method a in abstract class A not implemented by class B\n");
	}

	@Test
    public void testAbstractMethodMissingOverrideError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"class B extends A {",
					//error because we did not use the override keyword
					"public function a():void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Overriding a function that is not marked for override\n");
	}

	@Test
    public void testAbstractGetterMissingOverrideError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function get a():String;",
					"}",
					"class B extends A {",
					//error because we did not use the override keyword
					"public function get a():String {return null;}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Overriding a function that is not marked for override\n");
	}

	@Test
    public void testAbstractSetterMissingOverrideError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function set a(value:String):void;",
					"}",
					"class B extends A {",
					//error because we did not use the override keyword
					"public function set a(value:String):void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		compileAndExpectErrors(source, false,false,false, options, "Overriding a function that is not marked for override\n");
	}

	@Test
    public void testAbstractClassExtendsAbstractWithoutImplementingNoError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"public abstract function get b():String;",
					"public abstract function set b(value:String):void;",
					"}",
					"abstract class B extends A {",
					//we don't want an error for unimplemented methods because
					//this class is also abstract
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractMethodNotImplementedError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					//we don't want an error for unimplemented methods because
					//this class is also abstract
					"}",
					"class C extends B {",
					//but we do want an error here for unimplemented methods
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractGetterNotImplementedError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function get a():String;",
					"}",
					"abstract class B extends A {",
					//we don't want an error for unimplemented methods because
					//this class is also abstract
					"}",
					"class C extends B {",
					//but we do want an error here for unimplemented methods
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method get a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractSetterNotImplementedError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function set a(value:String):void;",
					"}",
					"abstract class B extends A {",
					//we don't want an error for unimplemented methods because
					//this class is also abstract
					"}",
					"class C extends B {",
					//but we do want an error here for unimplemented methods
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method set a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractMultipleMethodNotImplementedErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					"public abstract function b():void;",
					"}",
					"class C extends B {",
					//multiple errors because more than one method is not implemented
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method b in abstract class B not implemented by class C\nMethod a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractMultipleGetterNotImplementedErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function get a():String;",
					"}",
					"abstract class B extends A {",
					"public abstract function get b():String;",
					"}",
					"class C extends B {",
					//multiple errors because more than one method is not implemented
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method get b in abstract class B not implemented by class C\nMethod get a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testConcreteClassExtendsAbstractExtendsAbstractMultipleSetterNotImplementedErrors_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function set a(value:String):void;",
					"}",
					"abstract class B extends A {",
					"public abstract function set b(value:String):void;",
					"}",
					"class C extends B {",
					//multiple errors because more than one method is not implemented
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "Method set b in abstract class B not implemented by class C\nMethod set a in abstract class A not implemented by class C\n");
	}

	@Test
    public void testAbstractClassExtendsAbstractAndImplementsMethodNoError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"}",
					"abstract class B extends A {",
					//it's okay for an abstract subclass to implement any
					//abstract methods from the superclass
					"override public function a():void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractClassExtendsAbstractAndImplementsGetterNoError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function get a():String;",
					"}",
					"abstract class B extends A {",
					//it's okay for an abstract subclass to implement any
					//abstract getters from the superclass
					"override public function get a():String {return null;}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testAbstractClassExtendsAbstractAndImplementsSetterNoError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function set a(value:String):void;",
					"}",
					"abstract class B extends A {",
					//it's okay for an abstract subclass to implement any
					//abstract setters from the superclass
					"override public function set a(value:String):void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}

	@Test
    public void testConcreteClassExtendsAbstractNoError_withAllowAbstractClassesEnabled()
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
					"abstract class A {",
					"public abstract function a():void;",
					"public abstract function get b():String;",
					"public abstract function set b(value:String):void;",
					"}",
					"class B extends A {",
					//no errors because we've implemented all of the methods
					//that are abstract
					"override public function a():void {}",
					"override public function get b():String {return null;}",
					"override public function set b(value:String):void {}",
					"}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
					"-allow-abstract-classes=true"
        };
		File tempASFile = generateTempFile(source);
		String result = compile(tempASFile, source, false,false,false, options, true);
		Assert.assertEquals("", result);
	}
    
    @Test
    public void testAbstractNotAllowedOnInterfaceError_withAllowAbstractClassesEnabled()
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
            //error because abstract cannot be used with interfaces (only classes)
            "abstract interface A {",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
    }
    
    @Test
    public void testAbstractNotAllowedOnFunctionNotMethodError_withAllowAbstractClassesEnabled()
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
            //error because abstract cannot be used with standalone functions
            //that are not class methods
            "abstract function a():void {",
            "}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
            "-allow-abstract-classes=true"
        };
        compileAndExpectErrors(source, false,false,false, options, "The abstract attribute can only be used on a class definition or a non-static, non-final method defined on an abstract class.\n");
    }
}