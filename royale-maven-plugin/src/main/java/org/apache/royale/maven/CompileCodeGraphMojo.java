/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.royale.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Goal which exports the public API graph for the project.
 */
@Mojo(name="compile-codegraph", defaultPhase = LifecyclePhase.SITE)
public class CompileCodeGraphMojo
    extends BaseMojo
{

    @Parameter(defaultValue = "codegraph")
    private String codeGraphDirectoryName;

    @Parameter(defaultValue = "${project.artifactId}.json")
    private String outputFileName;

    @Parameter(property = "royale.skipCodeGraph", defaultValue = "false")
    private boolean skipCodeGraph;

    @Parameter(defaultValue = "false")
    private boolean skipAS;

    private ThreadLocal<Type> type = new ThreadLocal<Type>();

    @Override
    protected String getToolGroupName() {
        return "Royale";
    }

    @Override
    protected String getFlexTool() {
        return "codegraph";
    }

    @Override
    protected String getConfigFileName() throws MojoExecutionException {
        if(type.get() == null) {
            throw new MojoExecutionException("type not set");
        }
        switch (type.get()) {
            case SWF:
                return "compile-codegraph-swf-config.xml";
            case JS:
                return "compile-codegraph-js-config.xml";
        }
        return null;
    }

    @Override
    protected File getOutput() throws MojoExecutionException {
        if(type.get() == null) {
            throw new MojoExecutionException("type not set");
        }
        return new File(new File(new File(outputDirectory, codeGraphDirectoryName),
                type.get() == Type.SWF ? "swf" : "js"), outputFileName);
    }

    @Override
    protected List<String> getCompilerArgs(File configFile) throws MojoExecutionException {
        List<String> args = super.getCompilerArgs(configFile);
        args.removeIf(arg -> arg.startsWith("-js-compiler-define="));
        return args;
    }

    @Override
    protected boolean skip() {
        return skipCodeGraph || "pom".equals(project.getPackaging());
    }

    @Override
    protected List<Namespace> getNamespaces() {
        List<Namespace> namespaces = new LinkedList<Namespace>();
        for(Namespace namespace : super.getNamespaces()) {
            boolean matches = type.get() == Type.JS
                    ? namespace.getType().equals(Namespace.TYPE_DEFAULT)
                            || namespace.getType().equals(Namespace.TYPE_JS)
                    : namespace.getType().equals(Namespace.TYPE_DEFAULT)
                            || namespace.getType().equals(Namespace.TYPE_AS);
            if(matches) {
                namespaces.add(namespace);
            }
        }
        return namespaces;
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            if (!skipAS)
            {
                getLog().info("Generating SWF code graph");
                type.set(Type.SWF);
                super.execute();
                getLog().info("Finished");
            }

            getLog().info("Generating JS code graph");
            type.set(Type.JS);
            super.execute();
            getLog().info("Finished");
        } finally {
            type.remove();
        }
    }

    @Override
    protected List<Define> getDefines() throws MojoExecutionException {
        List<Define> defines = super.getDefines();
        if(type.get() == null) {
            throw new MojoExecutionException("type not set");
        }
        for(Define define : defines) {
            if("GOOG::DEBUG".equals(define.getName())) {
                define.setValue(type.get() == Type.SWF ? "true" : "goog.DEBUG");
            } else if("ROYALE::DISPLAYOBJECT".equals(define.getName())) {
                define.setValue(type.get() == Type.SWF ? "DisplayObject" : "IUIComponent");
            } else if("ROYALE::PROXYVISIBILITY".equals(define.getName())) {
                define.setValue(type.get() == Type.SWF ? "flash_proxy" : "public");
            }
        }
        switch (type.get()) {
            case SWF:
                defines.add(new Define("COMPILE::JS", "false"));
                defines.add(new Define("COMPILE::SWF", "true"));
                break;
            case JS:
                defines.add(new Define("COMPILE::JS", "true"));
                defines.add(new Define("COMPILE::SWF", "false"));
                break;
        }
        return defines;
    }

    @Override
    protected boolean includeLibrary(Artifact library) {
        String scope = library.getScope();
        if ("test".equalsIgnoreCase(scope)) {
            return false;
        }
        switch (type.get()) {
            case SWF: {
                String classifier = library.getClassifier();
                return "swf".equalsIgnoreCase(classifier) ||
                    ((classifier == null) && "runtime".equalsIgnoreCase(scope));
            }
            case JS: {
                String classifier = library.getClassifier();
                return "typedefs".equalsIgnoreCase(classifier) ||
                    "js".equalsIgnoreCase(classifier);
            }
        }
        return false;
    }

    @Override
    protected boolean includeLibraryJS(Artifact library) {
        return includeLibrary(library);
    }

    @Override
    protected boolean includeLibrarySWF(Artifact library) {
        return includeLibrary(library);
    }

    private enum Type {
        SWF,
        JS
    }

}