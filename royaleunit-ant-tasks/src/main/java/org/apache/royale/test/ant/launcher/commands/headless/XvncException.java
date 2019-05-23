/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.royale.test.ant.launcher.commands.headless;

public class XvncException extends Exception
{
    private static final long serialVersionUID = -879079265370069307L;
    
    public XvncException()
    {
        super("Could not find the vncserver command.");
    }

    public XvncException(int baseDisplayNumber, int finalDisplayNumber)
    {
        super("Could not start xvnc using displays " 
                + String.valueOf(baseDisplayNumber) 
                + "-" 
                + String.valueOf(finalDisplayNumber) 
                + "; Consider adding to your launch script: killall Xvnc Xrealvnc; rm -fv /tmp/.X*-lock /tmp/.X11-unix/X*");
    }
}
