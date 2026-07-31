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

package org.apache.royale.compiler.internal.codegen.graph;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class TestCodeGraphIdFactory
{
    @Test
    public void testDefinition()
    {
        assertEquals("as3://org/apache/royale/core/UIBase",
                CodeGraphIdFactory.definition("org.apache.royale.core.UIBase"));
    }

    @Test
    public void testMember()
    {
        assertEquals("as3://org/apache/royale/core/UIBase#typeNames",
                CodeGraphIdFactory.member("org.apache.royale.core.UIBase", "typeNames"));
    }

    @Test
    public void testAccessors()
    {
        assertEquals("as3://org/apache/royale/core/UIBase#width:get",
                CodeGraphIdFactory.accessor("org.apache.royale.core.UIBase", "width", true));
        assertEquals("as3://org/apache/royale/core/UIBase#width:set",
                CodeGraphIdFactory.accessor("org.apache.royale.core.UIBase", "width", false));
    }

    @Test
    public void testCallable()
    {
        assertEquals("as3://org/apache/royale/core/UIBase#setWidth(Number,Boolean)",
                CodeGraphIdFactory.callable("org.apache.royale.core.UIBase", "setWidth",
                        Arrays.asList("Number", "Boolean")));
    }

    @Test
    public void testConstructor()
    {
        assertEquals("as3://org/apache/royale/core/UIBase#constructor()",
                CodeGraphIdFactory.constructor("org.apache.royale.core.UIBase", Collections.<String>emptyList()));
    }
}