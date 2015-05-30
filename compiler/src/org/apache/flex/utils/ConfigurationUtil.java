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

package org.apache.flex.utils;

import java.io.File;

public class ConfigurationUtil {

    // workaround for Falcon bug.
    // Input files with relative paths confuse the algorithm that extracts the root class name.
    public static String[] fixArgs(final String[] args) {
        String[] newArgs = args;
        if (args.length > 1) {
            String targetPath = args[args.length - 1];
            if (targetPath.startsWith(".")) {
                targetPath = FileUtils.getTheRealPathBecauseCanonicalizeDoesNotFixCase(new File(targetPath));
                newArgs = new String[args.length];
                for (int i = 0; i < args.length - 1; ++i)
                    newArgs[i] = args[i];
                newArgs[args.length - 1] = targetPath;
            }
        }
        return newArgs;
    }
}
