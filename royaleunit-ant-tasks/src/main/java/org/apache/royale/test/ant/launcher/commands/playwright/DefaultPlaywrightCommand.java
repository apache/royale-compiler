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
package org.apache.royale.test.ant.launcher.commands.playwright;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.royale.test.ant.LoggingUtil;
import org.apache.tools.ant.Project;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Playwright.CreateOptions;
import com.microsoft.playwright.impl.PlaywrightImpl;

public class DefaultPlaywrightCommand implements PlaywrightCommand
{
    private Project project;
    private String url;
    private File swf;
    private String browser;
    private String[] environment;
    private Playwright playwright;

    public void setBrowser(String browser)
    {
        this.browser = browser;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }
    
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public File getSwf()
    {
        return swf;
    }

    public void setSwf(File swf)
    {
        this.swf = swf;
    }
    
    public void prepare()
    {
        CreateOptions createOptions = new CreateOptions();
        createOptions.setEnv(getEnvironmentMap());
        playwright = PlaywrightImpl.create(createOptions);
    }
    
    public Playwright launch() throws IOException
    {
        LoggingUtil.log("Executing Playwright with " + browser);
        BrowserType browserType = null;
        switch (browser)
        {
            case "html":
            case "chromium":
            {
                browserType = playwright.chromium();
                break;
            }
            case "webkit":
            {
                browserType = playwright.webkit();
                break;
            }
            case "firefox":
            {
                browserType = playwright.firefox();
                break;
            }
            default: 
                throw new IOException("Unknown browser: " + browser);
        }
        Browser browserInstance = browserType.launch();
        Page page = browserInstance.newPage();
        page.onConsoleMessage(new Consumer<ConsoleMessage>()
        {
            @Override
            public void accept(ConsoleMessage t)
            {
                switch (t.type())
                {
                    case "error":
                        LoggingUtil.error(t.text());
                        break;
                    default:
                        LoggingUtil.log(t.text());
                }
            } 
        });
        
        if (getUrl() != null)
        {
            page.navigate(getUrl());
        }
        else
        {
            page.navigate(swf.toURI().toString());
        }
        return playwright;
    }

    public void setEnvironment(String[] variables)
    {
        environment = variables;
    }

    @SuppressWarnings("unchecked")
    private Map<String,String> getEnvironmentMap()
    {
        Map<String, String> result = new HashMap<String, String>();
        if (environment != null)
        {
            for (String envVar : environment)
            {
                String[] parts = envVar.split("=");
                result.put(parts[0].trim(), parts.length > 1 ? parts[1].trim() : "");
            }
        }
        return result;
    }
}
