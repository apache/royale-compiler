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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.flex.tools.FlexTool;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

@Mojo(name="compile-tests",defaultPhase = LifecyclePhase.PROCESS_TEST_SOURCES)
public class CompileTestsMojo extends BaseMojo {
	private static final Pattern suiteMetadataPattern = Pattern.compile("\\[Suite\\]\\s*((public|dynamic|final)\\s+)+class\\s+\\w+");
	private static final Pattern testMetadataPattern = Pattern.compile("\\[Test\\]\\s*((public|override|final)\\s+)+function\\s+\\w+\\(");

    @Parameter
    private String testsMainClass;

	@Parameter(defaultValue = "${basedir}/src/test/royale", required = true, readonly = true)
	private File testSrcDirectory;

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/royaleunit", required = true, readonly = true)
	private File testGeneratedSrcDirectory;

    @Parameter(defaultValue = "${project.artifactId}-tests-${project.version}.swf")
    private String flashTestsOutputFileName;

    @Parameter(defaultValue = "javascript-tests")
    private String javascriptTestsOutputDirectoryName;

    @Parameter
    protected String targets = "SWF,JSRoyale";

    /**
     * Allows providing of a custom htmlTemplate which overrides the built-in one.
     */
    @Parameter
    protected String htmlTemplate;
    
    @Parameter(defaultValue = "true")
    protected boolean removeCirculars;

	@Parameter(property = "maven.test.skip", readonly = true)
	private boolean skip;

	public CompileTestsMojo() {
		super();
	}

    @Override
    protected String getToolGroupName() {
        return "Royale";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_MXMLC;
    }

    @Override
    protected String getConfigFileName() throws MojoExecutionException {
        return "compile-app-config.xml";
    }

    @Override
    protected File getOutput() throws MojoExecutionException {
        return new File(outputDirectory, flashTestsOutputFileName);
    }
    
    protected boolean isForceSwcExternalLibraryPath() {
        return false;
    }

    @Override
    protected List<String> getCompilerArgs(File configFile) throws MojoExecutionException {
        if(testsMainClass == null) {
            throw new MojoExecutionException("The mainClass has to be declared for SWF type modules.");
        }
        String mainClassPath = getSourcePath(testsMainClass);
        if(mainClassPath == null) {
            throw new MojoExecutionException("Could not find main class");
        }
        List<String> args = super.getCompilerArgs(configFile);
        File jsOutput = new File(outputDirectory, javascriptTestsOutputDirectoryName);
        args.add("-js-output=" + jsOutput.getAbsolutePath());
        args.add("-compiler.targets=" + targets);
        args.add(mainClassPath);
        return args;
    }

	@Override
	public void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("Not building test sources");
			return;
		}

		if (testsMainClass == null)
		{
			try {
				boolean hasTests = generateTestSources();
				if (!hasTests) {
					project.getProperties().setProperty("maven.test.skip", "true");
					return;
				}
			} catch (Exception e) {
				throw new MojoExecutionException("Failed to generate RoyaleUnit tests sources");
			}
		}

		super.execute();
	}

    @Override
    protected List<Define> getDefines() throws MojoExecutionException {
        List<Define> defines = super.getDefines();
        defines.add(new Define("COMPILE::JS", "AUTO"));
        defines.add(new Define("COMPILE::SWF", "AUTO"));
        return defines;
    }

	@Override
    protected VelocityContext getVelocityContext() throws MojoExecutionException {
		VelocityContext context = super.getVelocityContext();
        context.put("removeCirculars", removeCirculars);
        context.put("htmlTemplate", htmlTemplate);
		// these compiler options are available to library projects only
		// if a library has tests, they should be skipped because the tests
		// are compiled as an app instead of a library
        context.put("includeClasses", null);
        context.put("includeSources", false);
		return context;
	}

    protected VelocityContext getTestsApplicationVelocityContext(List<String> testQnames) throws MojoExecutionException {
        VelocityContext context = new VelocityContext();
        context.put("testQnames", testQnames);
        return context;
    }

	protected String getRoyaleUnitApplicationTemplateFileName() {
		return "RoyaleUnitApplication.mxml";
	}

	protected String getRoyaleUnitApplicationOutputFileName() {
		return "$__ROYALE_MAVEN_PLUGIN__RoyaleUnitApplication.mxml";
	}

	@Override
	protected List<String> getSourcePaths() {
		List<String> sourcePaths = super.getSourcePaths();
		sourcePaths.add(testSrcDirectory.getAbsolutePath());
		sourcePaths.add(testGeneratedSrcDirectory.getAbsolutePath());
		return sourcePaths;
	}

	protected boolean generateTestSources() throws MojoExecutionException, IOException {
		if (!testSrcDirectory.exists()) {
			return false;
		}
		Path testSrcDirectoryPath = testSrcDirectory.toPath();
		List<String> testQualifiedNames = Files.walk(testSrcDirectoryPath).map(Path::toFile).filter((File file) -> {
			if (!file.getName().endsWith(".as")) {
				return false;
			}

			String fileText;
			try {
				fileText = FileUtils.readFileToString(file, Charset.forName("utf-8"));
			} catch (IOException e) {
				return false;
			}

			Matcher testMetadataMatcher = testMetadataPattern.matcher(fileText);
			boolean hasTestMetadata = testMetadataMatcher.find();
			Matcher suiteMetadataMatcher = suiteMetadataPattern.matcher(fileText);
			boolean hasSuiteMetadata = suiteMetadataMatcher.find();

			if (!hasTestMetadata && !hasSuiteMetadata) {
				return false;
			}

			return true;
		}).map(File::toPath).map(path -> testSrcDirectoryPath.relativize(path).toString().replace("/", ".")
				.replace("\\", ".").replace(".as", "")).collect(Collectors.toList());

		if (testQualifiedNames.size() == 0) {
			return false;
		}

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        velocityEngine.setProperty(RuntimeConstants.SPACE_GOBBLING, "bc");
        velocityEngine.setProperty(RuntimeConstants.CHECK_EMPTY_OBJECTS, false);
        velocityEngine.setProperty("runtime.conversion.handler", "none");
        velocityEngine.init();
		VelocityContext context = getTestsApplicationVelocityContext(testQualifiedNames);
        File royaleUnitAppFile = new File(testGeneratedSrcDirectory, getRoyaleUnitApplicationOutputFileName());
        Template royaleUnitAppTemplate = velocityEngine.getTemplate("royale/" + getRoyaleUnitApplicationTemplateFileName());
        if(!royaleUnitAppFile.getParentFile().exists()) {
            if(!royaleUnitAppFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Could not create output directory: " + royaleUnitAppFile.getParent());
            }
        }
        FileWriter royaleUnitAppWriter = null;
        try {
            royaleUnitAppWriter = new FileWriter(royaleUnitAppFile);
            royaleUnitAppTemplate.merge(context, royaleUnitAppWriter);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating RoyaleUnit application file at " + royaleUnitAppFile.getPath());
        } finally {
            if(royaleUnitAppWriter != null) {
                try {
                    royaleUnitAppWriter.close();
                } catch (IOException e) {
                    throw new MojoExecutionException("Error creating RoyaleUnit application file at " + royaleUnitAppFile.getPath());
                }
            }
        }
		testsMainClass = getRoyaleUnitApplicationOutputFileName();
		return true;
	}

    @Override
    protected boolean includeLibrary(Artifact library) {
        String classifier = library.getClassifier();
        return (classifier == null) && !("runtime".equalsIgnoreCase(library.getScope()));
    }

    @Override
    protected boolean includeLibraryJS(Artifact library) {
        String classifier = library.getClassifier();
        // Strip out all externs except if the dependency was declared inside the pom itself.
        return "typedefs".equalsIgnoreCase(classifier) ||
                "js".equalsIgnoreCase(classifier);
    }

    @Override
    protected boolean includeLibrarySWF(Artifact library) {
        String classifier = library.getClassifier();
        return "swf".equalsIgnoreCase(classifier) ||
            ((classifier == null) && "runtime".equalsIgnoreCase(library.getScope()));
    }
}
