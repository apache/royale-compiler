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
 * Feature tests for the MXML {@code <String>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLStringTagTests extends MXMLInstanceTagTestsBase
{
    @Test
    public void MXMLStringTag_basic()
    {
        String[] declarations = new String[]
        {
            "<fx:String id='s1'>abc</fx:String>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('s1', s1, 'abc');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
