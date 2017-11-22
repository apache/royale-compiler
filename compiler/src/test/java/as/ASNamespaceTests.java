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
        String[] imports = new String[]
        {
            "import chrome.app",
        };
        String[] testCode = new String[]
        {
        	"var foo:app = new app();",
        	"var bar:chrome.app = new chrome.app();",
        	"var b1:Boolean = bar is app;",
        	"var b2:Boolean = foo is chrome.app;",
            "assertEqual('package qualifiers', b1, true);",
            "assertEqual('package qualifiers', b2, true);",
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
            "import custom.custom_namespace;",
        };
        String[] declarations = new String[]
        {
	       	"custom_namespace var foo:Namespace = new Namespace('foo', 'bar');",
        };
        String[] testCode = new String[]
        {
        	"var qname:QName = new QName(custom_namespace, 'foo');",
            "assertEqual('qualified names', custom_namespace::foo, this[qname]);",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }

    @Test
    public void ASNamespace_QNameDefinition()
    {
    	// all tests can assume that flash.display.Sprite
    	// flash.system.System and flash.events.Event have been imported
        String[] imports = new String[]
        {
            "import custom.custom_namespace;",
        };
        String[] declarations = new String[]
        {
	       	"custom_namespace var foo:Namespace = new Namespace('foo', 'bar');",
        };
        String[] testCode = new String[]
        {
        	"var ns:Namespace = new Namespace('baz', 'bar');",
        	"var qname:QName = new QName(ns, 'foo');",
        };
        String source = getAS(imports, declarations, testCode, new String[0]);
        compileAndRun(source);
    }
}
