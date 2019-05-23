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
package org.apache.royale.test.ant.launcher.commands.player;

import java.io.File;
import java.io.IOException;

import org.apache.royale.test.ant.launcher.commands.Command;
import org.apache.royale.test.ant.launcher.platforms.PlatformDefaults;

public abstract class DefaultPlayerCommand extends Command implements PlayerCommand
{
    private String url;
    private File swf;
    private PlatformDefaults defaults;
    
    public DefaultPlayerCommand()
    {
        super();
    }

    public void setSwf(File swf)
    {
        this.swf = swf;
    }

    public File getSwf()
    {
        return swf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }   

    public void setDefaults(PlatformDefaults defaults)
    {
        this.defaults = defaults;
    }

    public PlatformDefaults getDefaults()
    {
        return defaults;
    }
    
    public abstract File getFileToExecute();
    
    public abstract void prepare();
    
    @Override
    public Process launch() throws IOException
    {
        return super.launch();
    }
}
