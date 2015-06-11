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

package org.apache.flex.compiler.internal.codegen.externals;

import java.io.File;
import java.io.IOException;

import org.apache.flex.compiler.clients.EXTERNC;
import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.google.javascript.jscomp.Result;

public abstract class ExternalsTestBase
{
    private static File unitTestBaseDir = new File(
            FilenameNormalization.normalize("test-files/externals_unit_tests"));

    // Only used for testing, all configuration must happen in configure()
    protected ExternCConfiguration config;
    protected EXTERNC client;
    protected ReferenceModel model;

    @Before
    public void setUp() throws IOException
    {
        config = new ExternCConfiguration();
        configure(config);
        client = new EXTERNC(config);
        model = client.getModel();
    }

    protected abstract void configure(ExternCConfiguration config)
            throws IOException;

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
        config.addExternal(file);
        Result result = client.compile();
        Assert.assertTrue(result.success);
        return result;
    }

}
