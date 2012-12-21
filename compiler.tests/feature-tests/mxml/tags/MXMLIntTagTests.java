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
 * Feature tests for the MXML {@code <int>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLIntTagTests extends MXMLInstanceTagTestsBase
{
    @Test
    public void MXMLIntTag_basic()
    {
        String[] declarations = new String[]
        {
            "<fx:int id='i1'>0</fx:int>",
            "<fx:int id='i2'>2147483647</fx:int>",
            "<fx:int id='i3'>-2147483648</fx:int>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('i1', i1, 0);",
            "assertEqual('i2', i2, 2147483647);",
            "assertEqual('i3', i3, -2147483648);"
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
