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
 * Represents a <code>EnableTelemetry</code> tag in a SWF file.
 * <p>
 * The EnableTelemetry tag instructs the flash runtime to provide advanced telemetry options.
 */
public class EnableTelemetryTag extends Tag {

    /**
     * Constructor.
     */
    public EnableTelemetryTag() {
        super(TagType.EnableTelemetry);
    }

    /**
     * Constructor with initialization.
     */
    public EnableTelemetryTag(String password) {
        this();
        this.password = password;
    }


    private String password;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
