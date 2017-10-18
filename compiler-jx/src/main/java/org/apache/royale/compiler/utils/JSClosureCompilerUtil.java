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

package org.apache.royale.compiler.utils;

import java.security.Permission;

import com.google.javascript.jscomp.CommandLineRunner;

public class JSClosureCompilerUtil
{

    public static void run(String[] options)
    {
        forbidSystemExitCall();
        try
        {
            JSClosureCommandLineRunner.main(options);
        }
        catch (ExitTrappedException e)
        {
            // (erikdebruin) the CLR issued an System.Exit(), but we have 
            //               denied the request ;-)
        }
        finally
        {
            enableSystemExitCall();
        }
    }

    private static class JSClosureCommandLineRunner extends CommandLineRunner
    {
        JSClosureCommandLineRunner(String[] args)
        {
            super(args);
        }

        public static void main(String[] args)
        {
            JSClosureCommandLineRunner runner = new JSClosureCommandLineRunner(
                    args);

            if (runner.shouldRunCompiler())
            {
                runner.run();
            }
            else
            {
                System.exit(-1);
            }
        }
    }

    private static class ExitTrappedException extends SecurityException
    {
        private static final long serialVersionUID = 666;
    }

    private static void forbidSystemExitCall()
    {
        final SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkPermission(Permission permission)
            {
            }

            @Override
            public void checkExit(int status)
            {
                throw new ExitTrappedException();
            }
        };

        System.setSecurityManager(securityManager);
    }

    private static void enableSystemExitCall()
    {
        System.setSecurityManager(null);
    }

}
