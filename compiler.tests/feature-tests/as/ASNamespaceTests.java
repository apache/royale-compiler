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
 * Feature tests for AS Namespaces.
 */
public class ASNamespaceTests extends ASFeatureTestsBase
{
    @Test
    public void ASNamespace_package()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
            "import flash.utils.getQualifiedClassName;",
        };
        String[] testCode = new String[]
        {
        	"var foo:Event = new Event('foo');",
        	"var bar:flash.events.Event = new flash.events.Event('bar');",
            "assertEqual('qualified names', getQualifiedClassName(foo), getQualifiedClassName(bar));",
        };
        String source = getAS(imports, new String[0], testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASNamespace_custom()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
            "import flash.utils.getQualifiedClassName;",
            "import mx.core.mx_internal;",
        };
        String[] declarations = new String[]
        {
	       	"mx_internal var foo:Event = new Event('foo');",
        };
        String[] testCode = new String[]
        {
        	"var qname:QName = new QName(mx_internal, 'foo');",
            "assertEqual('qualified names', getQualifiedClassName(mx_internal::foo), getQualifiedClassName(this[qname]));",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source, true, false, false, null);
    }

    /*
     * The PackageConflict tests 
     */
    @Test
    public void ASNamespace_packageConflict()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
            "import flash.utils.getQualifiedClassName;",
        };
        String[] declarations = new String[]
        {
	       	"private var foo:Event = new Event('foo');",
	       	"private var bar:flash.events.Event = new flash.events.Event('foo');",
        };
        String[] testCode = new String[]
        {
            "assertEqual('qualified names', getQualifiedClassName(foo), 'Event');",
            "assertEqual('qualified names', getQualifiedClassName(bar), 'flash.events:Event');",
        };
        String[] extraCode = new String[]
        {
        	"class Event",
            "{",
            "    public function Event()",
            "    {",
            "    }",
            "}",
        };
        String source = getAS(imports, declarations, testCode, extraCode);
        compileAndRun(source, false, false, false, null);
    }

}
