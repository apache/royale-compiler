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

package org.apache.flex.compiler.internal.codegen.js.jsc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSPublisher;
import org.apache.flex.compiler.internal.projects.FlexJSProject;

public class JSCPublisher extends MXMLFlexJSPublisher
{
    public JSCPublisher(Configuration config, FlexJSProject project)
    {
        super(config, project);
    }

    @Override
    protected void writeHTML(String type, String projectName, String dirPath,
                             String deps, List<String> additionalHTML) throws IOException
    {
        if ("intermediate".equals(type))
        {
            StringBuilder depsFile = new StringBuilder();
            depsFile.append(deps);
            depsFile.append("goog.require(\"");
            depsFile.append(projectName);
            depsFile.append("\");\n");
            writeFile(dirPath + File.separator + projectName + "-dependencies.js", depsFile.toString(), false);
        }
        //don't call super.writeHTML() because asjsc defaults to no HTML
    }
}
