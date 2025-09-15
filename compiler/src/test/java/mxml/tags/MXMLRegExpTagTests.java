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

package mxml.tags;

import org.junit.Test;

/**
 * Feature tests for the MXML {@code <RegExp>} tag.
 */
public class MXMLRegExpTagTests extends MXMLInstanceTagTestsBase
{
    @Test
    public void MXMLRegExpTag_basic()
    {
        String[] declarations = new String[]
        {
            "<fx:RegExp id='r1'>/abc123/</fx:RegExp>",
            "<fx:RegExp id='r2'>/xyz987/sxgim</fx:RegExp>",
            "<fx:RegExp id='r3'>/[a-z]{3,} \\d+(oz|g)/ig</fx:RegExp>",
        };
        String[] asserts = new String[]
        {
            "assertEqual('r1', \"abc123\", r1.source);",
            "assertEqual('r1', false, r1.dotall);",
            "assertEqual('r1', false, r1.extended);",
            "assertEqual('r1', false, r1.global);",
            "assertEqual('r1', false, r1.ignoreCase);",
            "assertEqual('r1', false, r1.multiline);",

            "assertEqual('r2', \"xyz987\", r2.source);",
            "assertEqual('r2', true, r2.dotall);",
            "assertEqual('r2', true, r2.extended);",
            "assertEqual('r2', true, r2.global);",
            "assertEqual('r2', true, r2.ignoreCase);",
            "assertEqual('r2', true, r2.multiline);",

            "assertEqual('r3', \"[a-z]{3,} \\\\d+(oz|g)\", r3.source);",
            "assertEqual('r3', false, r3.dotall);",
            "assertEqual('r3', false, r3.extended);",
            "assertEqual('r3', true, r3.global);",
            "assertEqual('r3', true, r3.ignoreCase);",
            "assertEqual('r3', false, r3.multiline);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
