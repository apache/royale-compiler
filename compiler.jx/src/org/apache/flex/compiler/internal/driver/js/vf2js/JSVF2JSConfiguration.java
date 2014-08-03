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

package org.apache.flex.compiler.internal.driver.js.vf2js;

import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;

/**
 * The {@link JSVF2JSConfiguration} class holds all compiler arguments needed for
 * compiling ActionScript to JavaScript the 'goog' way.
 * <p>
 * Specific flags are implemented here for the configuration to be loaded by the
 * configure() method of {@link MXMLJSC}.
 * <p>
 * This class inherits all compiler arguments from the MXMLC compiler.
 * 
 * @author Erik de Bruin
 */
public class JSVF2JSConfiguration extends JSGoogConfiguration
{
    public JSVF2JSConfiguration()
    {
    }

    //
    // 'closure-lib'
    //

    @Override
    public String getClosureLib()
    {
        try
        {
            if (closureLib.equals(""))
            {
                closureLib = getAbsolutePathFromPathRelativeToMXMLC(
                        "../lib/google/closure-library");
            }
        }
        catch (Exception e) { /* better to try and fail... */ }
        
        return closureLib;
    }

}
