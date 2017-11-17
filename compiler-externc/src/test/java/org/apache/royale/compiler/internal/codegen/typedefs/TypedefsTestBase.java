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

package org.apache.royale.compiler.internal.codegen.typedefs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.royale.compiler.clients.EXTERNC;
import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.MethodReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.jstype.JSType;

public abstract class TypedefsTestBase
{
    private static File unitTestBaseDir =
            new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(), "typedefs/unit_tests");

    // Only used for testing, all configuration must happen in configure()
    protected ExternCConfiguration config;
    protected EXTERNC client;
    protected ReferenceModel model;

    @Before
    public void setUp() throws IOException
    {
        config = new ExternCConfiguration();
        configure(config);
        assertFalse(config.getConfigurationProblems().size() > 0);
        client = new EXTERNC(config);
        model = client.getModel();
        model.problems = new ProblemQuery();
    }

    protected abstract void configure(ExternCConfiguration config) throws IOException;

    @After
    public void tearDown()
    {
        model = null;
    }

    protected Result compile(String fileName) throws IOException
    {
        return compile(new File(unitTestBaseDir, fileName));
    }

    protected Result compile(File file) throws IOException
    {
        config.addTypedef(file);
        return compile();
    }

    protected Result compile() throws IOException
    {
        Result result = client.compile();
        Assert.assertTrue(result.success);
        return result;
    }

    protected JSType evaluateParam(MethodReference method, String paramName)
    {
        JSType jsType = method.getComment().getParameterType(paramName).evaluate(null,
                client.getCompiler().getJSCompiler().getTypeRegistry());
        return jsType;
    }

    /**
     * Clean, compile a js file based on the test method name.
     * 
     * @param relativeTestDir unitTestBaseDir relative base test directory.
     * @throws IOException
     */
    protected void assertCompileTestFileSuccess(String relativeTestDir) throws IOException
    {
        if (config.getAsRoot() != null)
        {
            client.cleanOutput();
        }
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        final String methodName = ste[2].getMethodName();
        Result result = compile(relativeTestDir + methodName + ".js");
        assertTrue(result.success);
    }
}
