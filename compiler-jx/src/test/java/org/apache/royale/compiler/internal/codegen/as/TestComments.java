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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.internal.test.ASTestBase;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestComments extends ASTestBase
{
    // (mschmalle) comments aren't preserved, no need for them in release 
    //             output...

    @Test
    public void testComment_SingleLine()
    {
//        IFunctionNode node = getMethod("function a():void {// single line comment};");
//        visitor.visitFunction(node);
//        assertOut("function a():void {\n\t// single line comment\n}");
    }

    @Test
    public void testComment_SingleLine_After()
    {
//        IFunctionNode node = getMethod("function a():void {var a:String = ''; // single line comment};");
//        visitor.visitFunction(node);
//        assertOut("function a():void {\n\tvar a:String = ''; // single line comment\n}");
    }

    @Test
    public void testComment_MultiLine()
    {
//        IFunctionNode node = getMethod("function a():void {/*first line comment\nsecond line comment*/};");
//        visitor.visitFunction(node);
//        assertOut("function a():void {\n\t/*first line comment\n\tsecond line comment*/\n}");
    }

    @Test
    public void testComment_InLine()
    {
//        IFunctionNode node = getMethod("function a():void {var a:String /* inline comment */ = 'Hello world';};");
//        visitor.visitFunction(node);
//        assertOut("function a():void {\n\tvar a:String /* inline comment */ = 'Hello world';\n}");
    }

    @Test
    public void testComment_ASDoc()
    {
//        IFunctionNode node = getMethod("function a():void {/**\n * line comment\n */};");
//        visitor.visitFunction(node);
//        assertOut("function a():void {\n\t/**\n\t * line comment\n\t */};");
    }

}
