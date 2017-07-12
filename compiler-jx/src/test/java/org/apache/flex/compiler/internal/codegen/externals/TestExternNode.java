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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.apache.flex.compiler.internal.codegen.externals.reference.ClassReference;
import org.junit.Test;

import com.google.javascript.jscomp.Result;

public class TestExternNode extends ExternalsTestBase
{
    @Test
    public void test_classes() throws IOException
    {
        Result result = compile();
        assertTrue("Compile Node.js externs not successful", result.success);

        ClassReference child_process_ChildProcess = model.getClassReference("child_process.ChildProcess");
        assertNotNull("child_process.ChildProcess not found in Node.js externs", child_process_ChildProcess);

        StringBuilder sb = new StringBuilder();
        child_process_ChildProcess.emit(sb);
        String r = sb.toString();
        assertTrue("child_process.ChildProcess must contain [JSModule(name=\"child_process\")] named module metadata",
                r.contains("[JSModule(name=\"child_process\")]"));
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    	ExternalsTestUtils.init();
        config.setASRoot(ExternalsTestUtils.AS_ROOT_DIR);

        config.addNamedModule("assert");
        config.addNamedModule("buffer");
        config.addNamedModule("child_process");
        config.addNamedModule("cluster");
        config.addNamedModule("crypto");
        config.addNamedModule("dgram");
        config.addNamedModule("dns");
        config.addNamedModule("domain");
        config.addNamedModule("events");
        config.addNamedModule("fs");
        config.addNamedModule("globals");
        config.addNamedModule("http");
        config.addNamedModule("https");
        config.addNamedModule("net");
        config.addNamedModule("os");
        config.addNamedModule("path");
        config.addNamedModule("punycode");
        config.addNamedModule("querystring");
        config.addNamedModule("readline");
        config.addNamedModule("repl");
        config.addNamedModule("stream");
        config.addNamedModule("string_decoder");
        config.addNamedModule("tls");
        config.addNamedModule("tty");
        config.addNamedModule("url");
        config.addNamedModule("util");
        config.addNamedModule("vm");
        config.addNamedModule("zlib");

        config.addExclude("Buffer", "toJSON");
        config.addExclude("osNetworkInterfacesInfo", "internal");

        String coreRoot = ExternalsTestUtils.EXTERNAL_NODE_DIR.getAbsolutePath();
        config.addExternal(coreRoot + "/assert.js");
        config.addExternal(coreRoot + "/buffer.js");
        config.addExternal(coreRoot + "/child_process.js");
        config.addExternal(coreRoot + "/cluster.js");
        config.addExternal(coreRoot + "/crypto.js");
        config.addExternal(coreRoot + "/dgram.js");
        config.addExternal(coreRoot + "/dns.js");
        config.addExternal(coreRoot + "/domain.js");
        config.addExternal(coreRoot + "/events.js");
        config.addExternal(coreRoot + "/fs.js");
        config.addExternal(coreRoot + "/globals.js");
        config.addExternal(coreRoot + "/http.js");
        config.addExternal(coreRoot + "/https.js");
        config.addExternal(coreRoot + "/net.js");
        config.addExternal(coreRoot + "/os.js");
        config.addExternal(coreRoot + "/path.js");
        config.addExternal(coreRoot + "/punycode.js");
        config.addExternal(coreRoot + "/querystring.js");
        config.addExternal(coreRoot + "/readline.js");
        config.addExternal(coreRoot + "/repl.js");
        config.addExternal(coreRoot + "/stream.js");
        config.addExternal(coreRoot + "/string_decoder.js");
        config.addExternal(coreRoot + "/tls.js");
        config.addExternal(coreRoot + "/tty.js");
        config.addExternal(coreRoot + "/url.js");
        config.addExternal(coreRoot + "/util.js");
        config.addExternal(coreRoot + "/vm.js");
        config.addExternal(coreRoot + "/zlib.js");
    }

}
