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

package org.apache.royale.compiler.internal.codegen.typedefs.utils;

import com.google.javascript.rhino.Node;

public final class DebugLogUtils
{
    private static boolean logEnabled = false;
    private static boolean errEnabled = false;

    public static void log(Node n)
    {
        log("StringTree -------------------------------------");
        log(n.toStringTree());
    }

    public static void log(String message)
    {
        if (logEnabled)
            System.out.println(message);
    }

    public static void err(String message)
    {
        if (errEnabled)
            System.err.println(message);
    }

    public static void err(Node n)
    {
        err("StringTree -------------------------------------");
        err(n.toStringTree());
    }
}
