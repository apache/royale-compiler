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

public class ASNullishCoalescingOperatorTests extends ASFeatureTestsBase
{
    @Test
    public void testNonNull()
    {
        String[] testCode = new String[]
        {
            "var s:String = 'foo';",
			"var result:Object = s ?? 'bar';",
			"assertEqual('non-null nullish coalescing', result, 'foo');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNull()
    {
        String[] testCode = new String[]
        {
            "var s:String = null;",
			"var result:Object = s ?? 'bar';",
			"assertEqual('non-null nullish coalescing', result, 'bar');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNonNullMemberAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {field: 'foo'};",
			"var result:Object = o.field ?? 'bar';",
			"assertEqual('non-null nullish coalescing', result, 'foo');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }

	@Test
    public void testNullMemberAccess()
    {
        String[] testCode = new String[]
        {
            "var o:Object = {field: null};",
			"var result:Object = o.field ?? 'bar';",
			"assertEqual('non-null nullish coalescing', result, 'bar');",
        };
        String source = getAS(new String[0], new String[0], testCode, new String[0]);

        compileAndRun(source);
    }
}
