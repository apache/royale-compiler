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
 * Created by Greg on 27/09/2016.
 */
public class ASInheritanceTests  extends ASFeatureTestsBase{



    @Test
    public void ImplicitConstructorErrorCheck()
    {
        // all tests can assume that flash.display.Sprite
        // flash.system.System and flash.events.Event have been imported
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
                        "public function A(nonOptional:String) {}",
                        "}",
                        "class B extends A {}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        compileAndExpectErrors(source, false,false,false, null,"No default constructor found in base class A.\n");
    }

    @Test
    public void ImplicitSuperCallErrorCheck()
    {
        // all tests can assume that flash.display.Sprite
        // flash.system.System and flash.events.Event have been imported
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
                        "public function A(nonOptional:String) {}",
                        "}",
                        "class B extends A {",
                        "public function B() {}",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        compileAndExpectErrors(source, false,false,false, null,"No default constructor found in base class A.\n");
    }
    
    @Test
    public void InterfaceOverrideError()
    {
        // all tests can assume that flash.display.Sprite
        // flash.system.System and flash.events.Event have been imported
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
                        "function get text():String;",
                        "}",
                        "interface B extends A {",
                        "function get text():String;",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        compileAndExpectErrors(source, false,false,false, null,"Cannot override an interface method.  Method text conflicts with a method in base interface A.\n");
    }

    @Test
    public void InterfaceOverrideOK()
    {
        // all tests can assume that flash.display.Sprite
        // flash.system.System and flash.events.Event have been imported
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
                        "function get text():String;",
                        "}",
                        "interface B extends A {",
                        "function set text(value:String):void;",
                        "}"
                };
        String source = getAS(imports, declarations, testCode, extra);

        compileAndRun(source);;
    }

}
