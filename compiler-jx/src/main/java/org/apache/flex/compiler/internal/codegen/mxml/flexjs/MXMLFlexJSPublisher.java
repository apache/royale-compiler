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
package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import com.google.javascript.jscomp.SourceFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.codegen.js.IJSPublisher;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.css.ICSSPropertyValue;
import org.apache.flex.compiler.internal.codegen.js.JSSharedData;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogPublisher;
import org.apache.flex.compiler.internal.codegen.js.goog.JarSourceFile;
import org.apache.flex.compiler.internal.css.CSSArrayPropertyValue;
import org.apache.flex.compiler.internal.css.CSSFontFace;
import org.apache.flex.compiler.internal.css.CSSFunctionCallPropertyValue;
import org.apache.flex.compiler.internal.driver.js.flexjs.JSCSSCompilationSession;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.graph.GoogDepsWriter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.targets.ITargetAttributes;
import org.apache.flex.compiler.utils.JSClosureCompilerWrapper;
import org.apache.flex.swc.ISWC;
import org.apache.flex.swc.ISWCFileEntry;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class MXMLFlexJSPublisher extends JSGoogPublisher implements IJSPublisher
{

    public static final String FLEXJS_OUTPUT_DIR_NAME = "bin";
    public static final String FLEXJS_INTERMEDIATE_DIR_NAME = "js-debug";
    public static final String FLEXJS_RELEASE_DIR_NAME = "js-release";

    private static final String FLEXJS_EXTERNS = "externs";

    class DependencyRecord
    {
        String path;
        String deps;
        String line;
        int lineNumber;
    }

    class DependencyLineComparator implements Comparator<DependencyRecord>
    {
        @Override
        public int compare(DependencyRecord o1, DependencyRecord o2)
        {
            return new Integer(o1.lineNumber).compareTo(o2.lineNumber);
        }
    }

    public MXMLFlexJSPublisher(Configuration config, FlexJSProject project)
    {
        super(config);
        this.isMarmotinniRun = googConfiguration.getMarmotinni() != null;
        this.outputPathParameter = configuration.getOutput();
        this.useStrictPublishing = googConfiguration.getStrictPublish();

        this.project = project;
    }

    private FlexJSProject project;

    private boolean isMarmotinniRun;
    private String outputPathParameter;
    private boolean useStrictPublishing;
    private String closureLibDirPath;

    @Override
    public File getOutputFolder()
    {
        // (erikdebruin) - If there is a -marmotinni switch, we want
        // the output redirected to the directory it specifies.
        // - If there is an -output switch, use that path as the
        // output parent folder.
        // FIXME: What is marmotinni?
        if (isMarmotinniRun)
        {
            outputParentFolder = new File(googConfiguration.getMarmotinni());
        }
        else if (outputPathParameter != null)
        {
            outputParentFolder = new File(outputPathParameter);
            // FB usually specified -output <project-path>/bin-release/app.swf
            if (outputPathParameter.contains(".swf"))
                outputParentFolder = outputParentFolder.getParentFile().getParentFile();
        }
        else
        {
            outputParentFolder = new File(configuration.getTargetFileDirectory()).getParentFile();
        }

        outputParentFolder = new File(outputParentFolder, FLEXJS_OUTPUT_DIR_NAME);

        outputFolder = new File(outputParentFolder, File.separator + FLEXJS_INTERMEDIATE_DIR_NAME);

        // (erikdebruin) Marmotinni handles file management, so we
        // bypass the setup.
        if (!isMarmotinniRun && !googConfiguration.getSkipTranspile()) {
            setupOutputFolder();
        }

        return outputFolder;
    }

    @Override
    public boolean publish(ProblemQuery problems) throws IOException
    {
        final File intermediateDir = outputFolder;
        File mainSourceFile = new File(configuration.getTargetFile());
        File srcDir = mainSourceFile.getAbsoluteFile().getParentFile();

        final String projectName = FilenameUtils.getBaseName(configuration.getTargetFile());
        final String outputFileName = projectName + "." + JSSharedData.OUTPUT_EXTENSION;

        File releaseDir = new File(outputParentFolder, FLEXJS_RELEASE_DIR_NAME);
        //final String releaseDirPath = releaseDir.getPath();

        // Ensure the release-dir is available and clean.
        if (!isMarmotinniRun)
        {
            if (releaseDir.exists())
            {
                FileUtils.deleteQuietly(releaseDir);
            }

	        if (configuration.release())
	        {
	            if (!releaseDir.mkdirs())
	            {
	                throw new IOException("Unable to create release directory at " + releaseDir.getAbsolutePath());
	            }
	        }
        }

        JSClosureCompilerWrapper compilerWrapper = new JSClosureCompilerWrapper(googConfiguration.getJSCompilerOptions());

        // If the closure lib dir is explicitly set, use that directory. If it
        // is not set, check if its content is available in the classpath. If
        // it is found in the classpath, dump it's content to the filesystem and
        // pass the files in to the compiler directly.
        if (googConfiguration.isClosureLibSet())
        {
            closureLibDirPath = googConfiguration.getClosureLib();
        }
        else
        {
            // Check if the "goog/deps.js" is available in the classpath.
            File closureLibraryJar = getJarThatContainsClasspathResources("goog/deps.js");
            if (closureLibraryJar != null)
            {
                // We don't want to add all files to the classpath, so we only output the
                // resources contained in 'closure-whitelist.properites' to the output.
                Properties whiteList = new Properties();
                whiteList.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "flexjs/closure-whitelist.properites"));

                // Add the closure files from classpath.
                for(SourceFile sourceFile : getClasspathResources(closureLibraryJar, whiteList)) {
                    compilerWrapper.addJSSourceFile(sourceFile);
                    // And dump a copy to the output directory (we will need them to execute the application)
                    FileUtils.write(new File(new File(intermediateDir, "library/closure"),
                            sourceFile.getName()), sourceFile.getCode());
                }

                // We won't be using the closure-lib dir in this case.
                closureLibDirPath = new File(intermediateDir, "library").getCanonicalPath();
            }
            // Fallback to the default.
            else
            {
                closureLibDirPath = googConfiguration.getClosureLib();
            }
        }

        final File closureGoogSrcLibDir = new File(closureLibDirPath, "closure/goog/");

        final File projectIntermediateJSFile = new File(intermediateDir, outputFileName);
        final File projectReleaseJSFile = new File(releaseDir, outputFileName);
	
        if (!googConfiguration.getSkipTranspile())
        {
	        appendEncodedCSS(projectIntermediateJSFile, projectName);
	
	        // if (!subsetGoog)
	        // {
	        // (erikdebruin) We need to leave the 'goog' files and dependencies well
	        // enough alone. We copy the entire library over so the
	        // 'goog' dependencies will resolve without our help.
// TODO: Check if this is needed.
//	        FileUtils.copyDirectory(new File(closureGoogSrcLibDirPath), new File(closureGoogTgtLibDirPath));
	        // }
        }

        // Iterate over all swc dependencies and add all the externs they contain.
        // (Externs are located in a "externs" directory in the root of the SWC)
        List<ISWC> swcs = project.getLibraries();
        for (ISWC swc : swcs)
        {
        	Map<String, ISWCFileEntry> files = swc.getFiles();
        	for (String key : files.keySet())
        	{
        		if (key.startsWith(FLEXJS_EXTERNS))
        		{
                    ISWCFileEntry fileEntry = swc.getFile(key);
                    if (fileEntry != null)
                    {
                        InputStream is = fileEntry.createInputStream();
                        String code = IOUtils.toString(is, "UTF-8");
                        is.close();
                        JarSourceFile externFile = new JarSourceFile(key, code,true);
                        System.out.println("using extern: " + key);
                        compilerWrapper.addJSExternsFile(externFile);
                    }
        		}
        	}
        }

        GoogDepsWriter gdw = new GoogDepsWriter(intermediateDir, projectName, googConfiguration, swcs);

        // Add all the js-files generated by the compiler to to config.
        ArrayList<String> fileList = gdw.getListOfFiles(problems);
        for (String file : fileList) {
            compilerWrapper.addJSSourceFile(file);
        }

        // Generate the content for the deps-file
        String depsFileData = gdw.generateDeps(problems);

        project.needCSS = gdw.needCSS;

        // Copy static resources to the intermediate (and release) directory.
        IOFileFilter pngSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".png"));
        IOFileFilter gifSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".gif"));
        IOFileFilter jpgSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".jpg"));
        IOFileFilter jsonSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".json"));
        IOFileFilter assetFiles = FileFilterUtils.or(pngSuffixFilter, jpgSuffixFilter, gifSuffixFilter,
                jsonSuffixFilter);
        IOFileFilter subdirs = FileFilterUtils.or(DirectoryFileFilter.DIRECTORY, assetFiles);
        FileUtils.copyDirectory(srcDir, intermediateDir, subdirs);
        if (configuration.release()) {
            FileUtils.copyDirectory(srcDir, releaseDir, subdirs);
        }

    	File template = ((JSGoogConfiguration)configuration).getHtmlTemplate();
        if (!((JSGoogConfiguration)configuration).getSkipTranspile())
        {
        	if (template != null) {
                writeTemplate(template, "intermediate", projectName, intermediateDir, depsFileData, gdw.additionalHTML);
            } else {
                writeHTML("intermediate", projectName, intermediateDir, depsFileData, gdw.additionalHTML);
            }
        }
        if (configuration.release())
        {
        	if (template != null) {
                writeTemplate(template, "release", projectName, releaseDir, depsFileData, gdw.additionalHTML);
            } else {
                writeHTML("release", projectName, releaseDir, null, gdw.additionalHTML);
            }
        }
        if (project.needCSS || ((JSGoogConfiguration)configuration).getSkipTranspile())
        {
            if (!((JSGoogConfiguration)configuration).getSkipTranspile()) {
                writeCSS(projectName, intermediateDir);
            }
	        if (configuration.release()) {
                FileUtils.copyFile(new File(intermediateDir, projectName + ".css"),
                        new File(releaseDir, projectName + ".css"));
            }
        }
        
        if (configuration.release())
        {

            List<SourceFile> sourceFiles = getDirectoryResources(closureGoogSrcLibDir);

            // Add all SourceFiles to the compiler.
            for(SourceFile sourceFile : sourceFiles) {
                compilerWrapper.addJSSourceFile(sourceFile);
            }

	        compilerWrapper.setOptions(projectReleaseJSFile.getCanonicalPath(), useStrictPublishing, projectName);
	
	        /*
	         * // (erikdebruin) Include the 'goog' deps to allow the compiler to
	         * resolve // dependencies. compilerWrapper.addJSSourceFile(
	         * closureGoogSrcLibDirPath + File.separator + "deps.js");
	         */
	        List<String> externs = googConfiguration.getExternalJSLib();
	        for (String extern : externs)
	        {
	            compilerWrapper.addJSExternsFile(extern);
	        }
	
	        compilerWrapper.targetFilePath = projectReleaseJSFile.getCanonicalPath();
	        compilerWrapper.compile();
	
	        appendSourceMapLocation(projectReleaseJSFile, projectName);
        }

        // if (ok)
        System.out.println("The project '" + projectName + "' has been successfully compiled and optimized.");

        return true;
    }

    private void appendEncodedCSS(File targetFile, String projectName) throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString.append("\n\n");
        appendString.append(projectName);
        appendString.append(".prototype.cssData = [");
        JSCSSCompilationSession cssSession = (JSCSSCompilationSession) project.getCSSCompilationSession();
        String s = cssSession.getEncodedCSS();
        if (s != null)
        {
	        int reqidx = s.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	        if (reqidx != -1)
	        {
	            String reqs = s.substring(reqidx);
	            s = s.substring(0, reqidx - 1);
	            String fileData = readCode(targetFile);
	            reqidx = fileData.indexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	            String after = fileData.substring(reqidx);
	            String before = fileData.substring(0, reqidx - 1);
	            s = before + reqs + after + appendString.toString() + s;
	            writeFile(targetFile, s, false);
	        }
	        else
	        {
	            appendString.append(s);
	            writeFile(targetFile, appendString.toString(), true);
	        }
        }
    }

    protected String readCode(File file)
    {
        String code = "";
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));

            String line = in.readLine();

            while (line != null)
            {
                code += line + "\n";
                line = in.readLine();
            }
            code = code.substring(0, code.length() - 1);

            in.close();
        }
        catch (Exception e)
        {
            // nothing to see, move along...
        }

        return code;
    }

    protected void writeTemplate(File template, String type, String projectName, File targetDir, String deps, List<String> additionalHTML)
    		throws IOException
	{
	    // Check if the template exists.
	    if(!template.exists()) {
	        throw new IOException("Template specified by 'html-template' does not exist: " + template.getPath());
        }

        String input = readCode(template);
        ITargetAttributes ta = project.computeTargetAttributes();
        Float width = null;
        Float height = null;
        String bgcolor = null;
        String pageTitle = null;
        if(ta != null)
        {
            width = ta.getWidth();
            height = ta.getHeight();
            bgcolor = ta.getBackgroundColor();
            pageTitle = ta.getPageTitle();
        }
        String result = input.replaceAll("\\$\\{application\\}", projectName);
        if (bgcolor != null)
            result = result.replaceAll("\\$\\{bgcolor\\}", bgcolor);
        //result = result.replaceAll("\\$\\{expressInstallSwf\\}", expressInstallSwf);
        if (height != null)
        	result = result.replaceAll("\\$\\{height\\}", height.toString());
        if (pageTitle != null)
            result = result.replaceAll("\\$\\{title\\}", pageTitle);
        //result = result.replaceAll("\\$\\{version_major\\}", versionMajor);
        //result = result.replaceAll("\\$\\{version_minor\\}", versionMinor);
        //result = result.replaceAll("\\$\\{version_revision\\}", versionRevision);
        if (width != null)
        	result = result.replaceAll("\\$\\{width\\}", width.toString());
        //result = result.replaceAll("\\$\\{useBrowserHistory\\}", useBrowserHistory);

        StringBuilder addHTML = new StringBuilder();
        addHTML.append(getTemplateAdditionalHTML(additionalHTML));
		addHTML.append(getTemplateDependencies(type, projectName, deps));
        result = result.replaceAll("\\$\\{head\\}", addHTML.toString());

        String templateBody = getTemplateBody(projectName);
        result = result.replaceAll("\\$\\{body\\}", templateBody);

		writeFile(new File(targetDir, googConfiguration.getHtmlOutputFileName()), result, false);
	}

    protected String getTemplateAdditionalHTML(List<String> additionalHTML)
    {
        StringBuilder htmlFile = new StringBuilder();
        for (String s : additionalHTML)
        {
            htmlFile.append(s).append("\n");
        }
        return htmlFile.toString();
    }

    protected String getTemplateDependencies(String type, String projectName, String deps)
    {
        StringBuilder depsHTML = new StringBuilder();
        if ("intermediate".equals(type))
        {
            depsHTML.append("\t<script type=\"text/javascript\" src=\"./library/closure/goog/base.js\"></script>\n");
            depsHTML.append("\t<script type=\"text/javascript\">\n");
            depsHTML.append(deps);
            depsHTML.append("\t\tgoog.require(\"");
            depsHTML.append(projectName);
            depsHTML.append("\");\n");
            depsHTML.append("\t</script>\n");
        }
        else
        {
            depsHTML.append("\t<script type=\"text/javascript\" src=\"./");
            depsHTML.append(projectName);
            depsHTML.append(".js\"></script>\n");
        }
        return depsHTML.toString();
    }

	protected String getTemplateBody(String projectName)
    {
        StringBuilder bodyHTML = new StringBuilder();
        bodyHTML.append("\t<script type=\"text/javascript\">\n");
        bodyHTML.append("\t\tnew ");
        bodyHTML.append(projectName);
        bodyHTML.append("()");
        bodyHTML.append(".start();\n");
        bodyHTML.append("\t</script>\n");
        return bodyHTML.toString();
    }

    protected void writeHTML(String type, String projectName, File targetDir, String deps, List<String> additionalHTML)
            throws IOException
    {
        StringBuilder htmlFile = new StringBuilder();
        htmlFile.append("<!DOCTYPE html>\n");
        htmlFile.append("<html>\n");
        htmlFile.append("<head>\n");
        htmlFile.append("\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n");
        htmlFile.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        htmlFile.append("\t<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(projectName).append(".css\">\n");

        htmlFile.append(getTemplateAdditionalHTML(additionalHTML));
        htmlFile.append(getTemplateDependencies(type, projectName, deps));

        htmlFile.append("</head>\n");
        htmlFile.append("<body>\n");

        htmlFile.append(getTemplateBody(projectName));

        htmlFile.append("</body>\n");
        htmlFile.append("</html>");

        writeFile(new File(targetDir, googConfiguration.getHtmlOutputFileName()), htmlFile.toString(), false);
    }

    private void writeCSS(String projectName, File targetDir) throws IOException
    {
        JSCSSCompilationSession cssSession = (JSCSSCompilationSession) project.getCSSCompilationSession();
        writeFile(new File(targetDir, projectName + ".css"), cssSession.emitCSS(), false);
        for (CSSFontFace fontFace : cssSession.fontFaces)
        {
        	// check frameworks/fonts folder
        	String configdir = configuration.getLoadConfig();
        	File dir = new File(configdir);
        	dir = dir.getParentFile();
        	for (ICSSPropertyValue prop : fontFace.getSources())
        	{
        		if (prop instanceof CSSArrayPropertyValue)
        		{
        			for (ICSSPropertyValue value : ((CSSArrayPropertyValue)prop).getElements())
        			{
        				CSSFunctionCallPropertyValue fn = (CSSFunctionCallPropertyValue)value;
        				String fontPath = fn.rawArguments;
        				if (fontPath.startsWith("'"))
        					fontPath = fontPath.substring(1, fontPath.length() - 1);
        				if (fontPath.startsWith("\""))
        					fontPath = fontPath.substring(1, fontPath.length() - 1);
        				int c = fontPath.indexOf("?");
        				if (c != -1)
        					fontPath = fontPath.substring(0, c);
        				File fontFile = new File(dir, fontPath);
        				File destFile = new File(targetDir, fontPath);
        				if (fontFile.exists())
        				{
        					if (!destFile.exists())
        						FileUtils.copyFile(fontFile, destFile);
        				}
        			}
        		}
        		else
        		{
        	        if (prop instanceof CSSFunctionCallPropertyValue)
        	        {
        				CSSFunctionCallPropertyValue fn = (CSSFunctionCallPropertyValue)prop;
        				String fontPath = fn.rawArguments;
        				if (fontPath.startsWith("'"))
        					fontPath = fontPath.substring(1, fontPath.length() - 1);
        				if (fontPath.startsWith("\""))
        					fontPath = fontPath.substring(1, fontPath.length() - 1);
        				int c = fontPath.indexOf("?");
        				if (c != -1)
        					fontPath = fontPath.substring(0, c);
        				File fontFile = new File(dir, fontPath);
        				File destFile = new File(targetDir, fontPath);
        				if (fontFile.exists())
        				{
        					if (!destFile.exists())
        						FileUtils.copyFile(fontFile, destFile);
        				}
        	        }
        		}
        	}
        }
    }

    protected File getJarThatContainsClasspathResources(String resourcePath) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (resource != null) {
            // Strip the url of the parts we don't need.
            // Unless we are not using some insanely complex setup
            // the resource will always be on the same machine.
            String resourceJarPath = resource.getFile();
            try {
                resourceJarPath = URLDecoder.decode(resourceJarPath, "UTF-8");
                if (resourceJarPath.contains(":")) {
                    resourceJarPath = resourceJarPath.substring(resourceJarPath.lastIndexOf(":") + 1);
                }
                if (resourceJarPath.contains("!")) {
                    resourceJarPath = resourceJarPath.substring(0, resourceJarPath.indexOf("!"));
                }
                return new File(resourceJarPath);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
