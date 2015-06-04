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
import java.lang.reflect.Array;

public class ArgumentUtil {

    // workaround for Falcon bug.
    // Input files with relative paths confuse the algorithm that extracts the root class name.
    public static String[] fixArgs(final String[] args) {
        String[] newArgs = args;
        if (args.length > 1) {
            String targetPath = args[args.length - 1];
            if (targetPath.startsWith(".")) {
                targetPath = FileUtils.getTheRealPathBecauseCanonicalizeDoesNotFixCase(new File(targetPath));
                newArgs = new String[args.length];
                System.arraycopy(args, 0, newArgs, 0, args.length - 1);
                newArgs[args.length - 1] = targetPath;
            }
        }
        return newArgs;
    }

    public static String[] removeElement(String[] args, String element) {

        int length = Array.getLength(args);
        int index = -1;

        for (int i = 0; i < length; i++) {
            final String[] kvp = args[i].split("=");
            if (element.equals(kvp[0])) {
                index = i;
                break;
            }
        }

        String[] newArgs = new String[length - 1];

        if (index < 0 || index >= length) {
            System.arraycopy(args, 0, newArgs, 0, length - 1);
        } else  {
            System.arraycopy(args, 0, newArgs, 0, index);
            if (index < length - 1) {
                System.arraycopy(args, index + 1, newArgs, index, length - index - 1);
            }
        }

        return newArgs;
    }

    public static String getValue(String[] args, String element) {

        boolean found = false;
        String[] kvp = new String[0];

        for (String s : args) {
            kvp = s.split("=");

            if (kvp[0].equals(element)) {
                found = true;
                break;
            }
        }

        return found ? kvp[1] : null;
    }

    public static void setValue(String[] args, String element, String value) {
        String[] kvp;

        for (int i = 0, argsLength = args.length; i < argsLength; i++) {
            kvp = args[i].split("=");

            if (kvp[0].equals(element)) {
                args[i] = kvp[0] + "=" + value;
                break;
            }
        }
    }

    public static String[] addValueAt(String[] args, String elemrnt, String value, int index) {

        int length = Array.getLength(args);

        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }

        String[] newArgs = new String[args.length + 1];

        for (int i = 0; i < newArgs.length; i++) {
            if (i < index) {
                newArgs[i] = args[i];
            } else if (i == index) {
                newArgs[i] = elemrnt + "=" + value;
            } else {
                newArgs[i] = args[i - 1];
            }

        }

        return newArgs;
    }
}
