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

package org.apache.royale.compiler.internal.codegen.js.jsc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.codegen.mxml.royale.MXMLRoyalePublisher;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;

public class JSCPublisher extends MXMLRoyalePublisher
{
    public JSCPublisher(RoyaleJSProject project, Configuration config)
    {
        super(project, config);
        this.project = project;
    }

    private RoyaleJSProject project;

    @Override
    protected String getTemplateBody(String mainClassQName)
    {
        IDefinition def = project.resolveQNameToDefinition(mainClassQName);
        IDefinitionNode node = def.getNode();
        if (node instanceof IMXMLDocumentNode)
        {
            //we should probably customize MXML too, but for now, pass it to the
            //default implementation -JT
            return super.getTemplateBody(mainClassQName);
        }
        //for ActionScript classes, simply call the constructor by default
        StringBuilder bodyHTML = new StringBuilder();
        bodyHTML.append("\t<script type=\"text/javascript\">\n");
        bodyHTML.append("\t\tnew ");
        bodyHTML.append(mainClassQName);
        bodyHTML.append("();\n");
        bodyHTML.append("\t</script>\n");
        return bodyHTML.toString();
    }

    @Override
    protected void writeHTML(String type, String projectName, String mainClassQName, File targetDir,
                             String deps, List<String> additionalHTML) throws IOException
    {
        if ("intermediate".equals(type))
        {
            StringBuilder depsFile = new StringBuilder();
            depsFile.append(deps);
            depsFile.append("goog.require(\"");
            depsFile.append(projectName);
            depsFile.append("\");\n");
            writeFile(new File(targetDir, projectName + "-dependencies.js"), depsFile.toString(), false);
        }
        //don't call super.writeHTML() because asjsc defaults to no HTML
    }
}
