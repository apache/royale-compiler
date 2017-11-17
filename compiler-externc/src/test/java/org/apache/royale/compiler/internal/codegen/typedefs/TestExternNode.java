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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.junit.Test;

import com.google.javascript.jscomp.Result;

public class TestExternNode extends TypedefsTestBase
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
    	TypedefsTestUtils.init();
        config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);

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

        String coreRoot = TypedefsTestUtils.EXTERNAL_NODE_DIR.getAbsolutePath();
        config.addTypedef(coreRoot + "/assert.js");
        config.addTypedef(coreRoot + "/buffer.js");
        config.addTypedef(coreRoot + "/child_process.js");
        config.addTypedef(coreRoot + "/cluster.js");
        config.addTypedef(coreRoot + "/crypto.js");
        config.addTypedef(coreRoot + "/dgram.js");
        config.addTypedef(coreRoot + "/dns.js");
        config.addTypedef(coreRoot + "/domain.js");
        config.addTypedef(coreRoot + "/events.js");
        config.addTypedef(coreRoot + "/fs.js");
        config.addTypedef(coreRoot + "/globals.js");
        config.addTypedef(coreRoot + "/http.js");
        config.addTypedef(coreRoot + "/https.js");
        config.addTypedef(coreRoot + "/net.js");
        config.addTypedef(coreRoot + "/os.js");
        config.addTypedef(coreRoot + "/path.js");
        config.addTypedef(coreRoot + "/punycode.js");
        config.addTypedef(coreRoot + "/querystring.js");
        config.addTypedef(coreRoot + "/readline.js");
        config.addTypedef(coreRoot + "/repl.js");
        config.addTypedef(coreRoot + "/stream.js");
        config.addTypedef(coreRoot + "/string_decoder.js");
        config.addTypedef(coreRoot + "/tls.js");
        config.addTypedef(coreRoot + "/tty.js");
        config.addTypedef(coreRoot + "/url.js");
        config.addTypedef(coreRoot + "/util.js");
        config.addTypedef(coreRoot + "/vm.js");
        config.addTypedef(coreRoot + "/zlib.js");
    }

}
