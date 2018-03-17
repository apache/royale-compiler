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

package org.apache.royale.compiler.internal.codegen.js.node;

import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.internal.codegen.js.jsc.JSCPublisher;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NodePublisher extends JSCPublisher
{
    public boolean exportModule = false;

    public NodePublisher(Configuration config, RoyaleJSProject project)
    {
        super(project, config);
    }

    @Override
    protected String getTemplateDependencies(String type, String projectName, String mainClassQName, String deps)
    {
        StringBuilder depsJS = new StringBuilder();
        if ("intermediate".equals(type))
        {
            depsJS.append("require(\"./library/closure/goog/bootstrap/nodejs\");\n");
            depsJS.append(deps);
            depsJS.append("goog.require(\"");
            depsJS.append(projectName);
            depsJS.append("\");\n");
        }
        else
        {
            depsJS.append("var ");
            depsJS.append(projectName);
            depsJS.append(" = require(\"./");
            depsJS.append(projectName);
            depsJS.append("\").");
            depsJS.append(projectName);
            depsJS.append(";\n");
        }
        return depsJS.toString();
    }

    @Override
    protected String getTemplateBody(String mainClassQName)
    {
        StringBuilder bodyJS = new StringBuilder();
        if (exportModule)
        {
            bodyJS.append("module.exports = ");
            bodyJS.append(mainClassQName);
            bodyJS.append(";");
        }
        else
        {
            bodyJS.append("new ");
            bodyJS.append(mainClassQName);
            bodyJS.append("();");
        }
        return bodyJS.toString();
    }

    @Override
    protected void writeHTML(String type, String projectName, String mainClassQName, File targetDir,
                             String deps, List<String> additionalHTML) throws IOException
    {
        StringBuilder contents = new StringBuilder();
        contents.append(getTemplateDependencies(type, projectName, mainClassQName, deps));
        contents.append(getTemplateBody(mainClassQName));
        writeFile(new File(targetDir, "index.js"), contents.toString(), false);
    }
}
