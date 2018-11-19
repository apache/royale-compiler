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

package org.apache.royale.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;

public class ArgumentUtil {

    // workaround for Royale bug.
    // Input files with relative paths confuse the algorithm that extracts the root class name.
    public static String[] fixArgs(final String[] args) {
        String[] newArgs = args;
        if (args.length > 1) {
            String targetPath = args[args.length - 1];
            if (targetPath.startsWith(".")) {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_UTILS) == CompilerDiagnosticsConstants.FILE_UTILS)
            		System.out.println("ArgumentUtil waiting for lock in getTheRealPathBecauseCanonicalizeDoesNotFixCase");
                targetPath = FileUtils.getTheRealPathBecauseCanonicalizeDoesNotFixCase(new File(targetPath));
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_UTILS) == CompilerDiagnosticsConstants.FILE_UTILS)
            		System.out.println("ArgumentUtil waiting for lock in getTheRealPathBecauseCanonicalizeDoesNotFixCase");
                newArgs = new String[args.length];
                System.arraycopy(args, 0, newArgs, 0, args.length - 1);
                newArgs[args.length - 1] = targetPath;
            }
        }
        return newArgs;
    }

    public static String[] removeEachElement(String[] args, String element)
    {
        String[] newArgs = args.clone();

        while (getValue(newArgs, element) != null)
        {
            newArgs = removeElement(args, element);
        }

        return newArgs;
    }

    public static String[] removeElement(String[] args, String element) {

        int length = Array.getLength(args);
        int index = -1;

        for (int i = 0; i < length; i++) {
            final boolean plusEqual = args[i].contains("+=");
            String[] kvp = args[i].split(plusEqual ? "\\+=" : "=");
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

    public static String[] removeElementWithValue(String[] args, String element, String value) {

        int length = Array.getLength(args);
        int index = -1;

        for (int i = 0; i < length; i++) {
            final boolean plusEqual = args[i].contains("+=");
            String[] kvp = args[i].split(plusEqual ? "\\+=" : "=");
            if (element.equals(kvp[0]) && (kvp.length == 1 || (value != null && value.equals(kvp[1])))) {
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
            final boolean plusEqual = s.contains("+=");
            kvp = s.split(plusEqual ? "\\+=" : "=");

            if (kvp[0].equals(element)) {
                found = true;
                break;
            }
        }

        return found ? kvp.length == 2 ? kvp[1] : null : null;
    }

    public static Collection<String> getValues(String[] args, String element) {

        String[] kvp;
        final Multimap<String, String> argsMap = ArrayListMultimap.create();

        for (String s : args) {
            final boolean plusEqual = s.contains("+=");
            kvp = s.split(plusEqual ? "\\+=" : "=");

            if (plusEqual || !argsMap.containsKey(kvp[0])) {
                argsMap.put(kvp[0], kvp.length == 2 ? kvp[1] : null);
            }
            else {
                ArrayList<String> replacement = null;
                if (kvp.length > 1) {
                    replacement = new ArrayList<String>();
                    replacement.add(kvp[1]);
                    argsMap.replaceValues(kvp[0], replacement);
                }
            }
        }

        return argsMap.get(element);
    }

    public static void setValue(String[] args, String element, String value) {
        String[] kvp;

        for (int i = 0, argsLength = args.length; i < argsLength; i++) {
            final boolean plusEqual = args[i].contains("+=");
            kvp = args[i].split(plusEqual ? "\\+=" : "=");

            if (kvp[0].equals(element)) {
                String affectationSign = plusEqual ? "+=" : "=";
                args[i] = kvp[0] + affectationSign + value;
                break;
            }
        }
    }

    public static String[] addValue(String[] args, String element) {
        return  addValue(args, element, null, args.length - 1, false);
    }

    public static String[] addValue(String[] args, String element, String value) {
        return  addValue(args, element, value, args.length - 1, false);
    }

    public static String[] addValue(String[] args, String element, String value, boolean plusEqual) {
        return  addValue(args, element, value, args.length - 1, plusEqual);
    }

    public static String[] addValue(String[] args, String element, String value, int index) {
        return  addValue(args, element, value, index, false);
    }

    public static String[] addValue(String[] args, String element, String value, int index, boolean plusEqual) {

        int length = Array.getLength(args);

        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }

        String[] newArgs = new String[args.length + 1];

        for (int i = 0; i < newArgs.length; i++) {
            if (i < index) {
                newArgs[i] = args[i];
            } else if (i == index) {
                String affectationSign = plusEqual ? "+=" : "=";
                newArgs[i] = value != null ? element + affectationSign + value : element;
            } else {
                newArgs[i] = args[i - 1];
            }

        }

        return newArgs;
    }
}
