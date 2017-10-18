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

package flex2.tools;

import org.apache.royale.compiler.clients.COMPC;

import java.lang.reflect.InvocationTargetException;

/**
 * Entry-point for compc, the command-line tool for compiling components.
 */
public class Compc extends Tool {

    /**
     * The entry-point for Mxmlc.
     * Note that if you change anything in this method, make sure to check Compc, Shell, and
     * the server's CompileFilter to see if the same change needs to be made there.  You
     * should also inform the Zorn team of the change.
     *
     * @param args
     */
    public static void main(String[] args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        System.exit(compcNoExit(args));
    }

    public static int compcNoExit(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        JS_COMPILER = CompJSC.class;

        return compile(args);
    }

    public static void compc(String[] args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        JS_COMPILER = CompJSC.class;

        compile(args);
    }
}
