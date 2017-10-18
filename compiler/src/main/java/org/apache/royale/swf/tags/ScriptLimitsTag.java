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

package org.apache.royale.swf.tags;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>ScriptLimits</code> tag in a SWF file.
 * <p>
 * The ScriptLimits tag includes two fields that can be used to override the
 * default settings for maximum recursion depth and ActionScript time-out.
 */
public class ScriptLimitsTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     * 
     * @param maxRecursionDepth max recursion depth
     * @param scriptTimeoutSeconds script timeout seconds
     */
    public ScriptLimitsTag(int maxRecursionDepth, int scriptTimeoutSeconds)
    {
        super(TagType.ScriptLimits);
        
        this.maxRecursionDepth = maxRecursionDepth;
        this.scriptTimeoutSeconds = scriptTimeoutSeconds;
    }
    
    private final int maxRecursionDepth;
    private final int scriptTimeoutSeconds;

    /**
     * The MaxRecursionDepth field sets the ActionScript maximum recursion
     * limit. The default setting is 256 at the time of this writing. This
     * default can be changed to any value greater than zero (0).
     * 
     * @return max recursion depth
     */
    public int getMaxRecursionDepth()
    {
        return maxRecursionDepth;
    }

    /**
     * The ScriptTimeoutSeconds field sets the maximum number of seconds the
     * player should process ActionScript before displaying a dialog box asking
     * if the script should be stopped. The default value for
     * ScriptTimeoutSeconds varies by platform and is between 15 to 20 seconds.
     * This default value is subject to change.
     * 
     * @return script timeout seconds
     */
    public int getScriptTimeoutSeconds()
    {
        return scriptTimeoutSeconds;
    }

}
