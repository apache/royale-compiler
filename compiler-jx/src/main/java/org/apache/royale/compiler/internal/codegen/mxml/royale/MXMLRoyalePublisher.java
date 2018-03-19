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
package org.apache.royale.compiler.internal.codegen.mxml.royale;

import com.google.javascript.jscomp.SourceFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.codegen.js.IJSPublisher;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSPropertyValue;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogPublisher;
import org.apache.royale.compiler.internal.codegen.js.goog.JarSourceFile;
import org.apache.royale.compiler.internal.css.CSSArrayPropertyValue;
import org.apache.royale.compiler.internal.css.CSSFontFace;
import org.apache.royale.compiler.internal.css.CSSFunctionCallPropertyValue;
import org.apache.royale.compiler.internal.driver.js.royale.JSCSSCompilationSession;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.graph.GoogDepsWriter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.targets.ITargetAttributes;
import org.apache.royale.compiler.utils.JSClosureCompilerWrapper;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.ISWCManager;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class MXMLRoyalePublisher extends JSGoogPublisher implements IJSPublisher
{

    public static final String ROYALE_OUTPUT_DIR_NAME = "bin";
    public static final String ROYALE_INTERMEDIATE_DIR_NAME = "js-debug";
    public static final String ROYALE_RELEASE_DIR_NAME = "js-release";

    private static final String ROYALE_EXTERNS = "externs";
    private static final String ROYALE_THEME_ASSETS = "assets/";

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

    public MXMLRoyalePublisher(RoyaleJSProject project, Configuration config)
    {
        super(project, config);
        this.isMarmotinniRun = googConfiguration.getMarmotinni() != null;
        this.outputPathParameter = configuration.getOutput();
        this.useStrictPublishing = googConfiguration.getStrictPublish();

        this.project = project;
    }

    protected RoyaleJSProject project;

    private boolean isMarmotinniRun;
    private String outputPathParameter;
    private boolean useStrictPublishing;

    private GoogDepsWriter getGoogDepsWriter(File intermediateDir, 
    										String mainClassQName, 
    										JSGoogConfiguration googConfiguration, 
    										List<ISWC> swcs)
    {
    	return new GoogDepsWriter(intermediateDir, mainClassQName, googConfiguration, swcs);
    }

    @Override
    public File getOutputFolder()
    {
        // Marmotinni is our test-framework. In case of a Marmotinni build
        // we need to output the code to a different location.
        // FIXME: I think this is a bad idea ... we should remove this.
        // (erikdebruin) - If there is a -marmotinni switch, we want
        // the output redirected to the directory it specifies.
        // - If there is an -output switch, use that path as the
        // output parent folder.
        if (isMarmotinniRun)
        {
            outputParentFolder = new File(googConfiguration.getMarmotinni());
        }
        // If the output path is specified using the config-xml or the commandline.
        else if (outputPathParameter != null)
        {
            // FB usually specified -output <project-path>/bin-release/app.swf
            if (outputPathParameter.contains(".swf")) {
            	if (outputParentFolder == null)
            		outputParentFolder = new File(outputPathParameter);
                outputParentFolder = outputParentFolder.getParentFile().getParentFile();
            } else {
                outputParentFolder = new File(outputPathParameter);
            }
        }
        else
        {
            String mainClassFolder = configuration.getTargetFileDirectory();
            if (mainClassFolder.endsWith("src"))
                outputParentFolder = new File(configuration.getTargetFileDirectory()).getParentFile();
            else if (mainClassFolder.endsWith("src/main/royale") || mainClassFolder.endsWith("src\\main\\royale"))
                outputParentFolder = new File(configuration.getTargetFileDirectory()).getParentFile().getParentFile().getParentFile();
            else
                outputParentFolder = new File(configuration.getTargetFileDirectory());
        }

        outputParentFolder = new File(outputParentFolder, ROYALE_OUTPUT_DIR_NAME);

        outputFolder = new File(outputParentFolder, File.separator + ROYALE_INTERMEDIATE_DIR_NAME);

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
        // The "intermediate" is the "js-debug" output.
        final File intermediateDir = outputFolder;

        // The source directory is the source path entry containing the Main class.
        List<File> sourcePaths = project.getSourcePath();
        String targetFile = configuration.getTargetFile().toLowerCase();
    	System.out.println("find project folder...");
        File imageSrcDir = null;
        for (File sp : sourcePaths)
        {
        	System.out.println("checking source path " + sp.getAbsolutePath());
        	String lowercasePath = sp.getAbsolutePath().toLowerCase();
        	if (targetFile.startsWith(lowercasePath))
        		imageSrcDir = sp;
        }
        if (imageSrcDir == null)
        {
        	imageSrcDir = new File(configuration.getTargetFile()).getAbsoluteFile().getParentFile();
        	System.out.println("not found on source path, using parent file " + imageSrcDir.getAbsolutePath());
        }
        final String projectName = FilenameUtils.getBaseName(configuration.getTargetFile());
        String qName = null;
        try {
			qName = project.mainCU.getQualifiedNames().get(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String mainClassQName = qName;
        final String outputFileName = projectName + "." + project.getBackend().getOutputExtension();

        // The "release" is the "js-release" directory.
        File releaseDir = new File(outputParentFolder, ROYALE_RELEASE_DIR_NAME);

        /////////////////////////////////////////////////////////////////////////////////
        // Copy static resources to the intermediate (and release) directory.
        /////////////////////////////////////////////////////////////////////////////////

        IOFileFilter pngSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".png"));
        IOFileFilter gifSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".gif"));
        IOFileFilter jpgSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".jpg"));
        IOFileFilter jpegSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".jpeg"));
        IOFileFilter svgSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".svg"));
        IOFileFilter jsonSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".json"));
        IOFileFilter assetFiles = FileFilterUtils.or(pngSuffixFilter, jpgSuffixFilter, jpegSuffixFilter, svgSuffixFilter, gifSuffixFilter,
                jsonSuffixFilter);
        IOFileFilter resourceFilter = FileFilterUtils.or(DirectoryFileFilter.DIRECTORY, assetFiles);
        // FIXME: All images need to be located relative to the Main class ... for Maven this is a problem.
        FileUtils.copyDirectory(imageSrcDir, intermediateDir, resourceFilter);
        // Iterate over all themes SWCs and add the contents of any included files in
        // an assets folder to an assets folder in the destination folder.
        final ISWCManager swcManager = project.getWorkspace().getSWCManager();
        List<ISWC> themeSWCs = new ArrayList<ISWC>();
        List<IFileSpecification> themes = project.getThemeFiles();
        for (final IFileSpecification themeFile : themes)
        {
            final String extension = FilenameUtils.getExtension(themeFile.getPath());
            if ("swc".equalsIgnoreCase(extension))
            {
                final ISWC swc = swcManager.get(new File(themeFile.getPath()));
                themeSWCs.add(swc);
	            Map<String, ISWCFileEntry> files = swc.getFiles();
	            for (String key : files.keySet())
	            {
	                if (key.startsWith(ROYALE_THEME_ASSETS))
	                {
	                    ISWCFileEntry fileEntry = swc.getFile(key);
	                    if (fileEntry != null)
	                    {
	                        InputStream is = fileEntry.createInputStream();
	                        int n = is.available();
	                        int total = 0;
                        	byte[] data = new byte[n];
	                        while (total < n)
	                        {
	                        	total += is.read(data, total, n - total);
	                        }
	                        FileUtils.writeByteArrayToFile(new File(intermediateDir, key), data);
	                    }
	                }
	            }
	        }
        }
        
        // If we are doing a release build, we need to copy them to the release dir too.
        if (configuration.release()) {
            FileUtils.copyDirectory(imageSrcDir, releaseDir, resourceFilter);
            // The copy-directory contains a lot of empty directories ... clean them up.
            clearEmptyDirectoryTrees(releaseDir);
        }

        /////////////////////////////////////////////////////////////////////////////////
        // Copy / Dump the closure files into the intermediate directory.
        /////////////////////////////////////////////////////////////////////////////////

        // List of source files we need to pass into the closure compiler. As we have to
        // read the content in order to dump it to the intermediate, we can just keep it
        // and eventually use it in case of a release build.
        List<SourceFile> closureSourceFiles = null;

        // If the closure lib dir is explicitly set, use that directory. If it
        // is not set, check if its content is available in the classpath. If
        // it is found in the classpath, use that as closure lib dir.
        if (!googConfiguration.isClosureLibSet())
        {
            // Check if the "goog/deps.js" is available in the classpath.
            File closureLibraryJar = getJarThatContainsClasspathResources("goog/deps.js");
            if (closureLibraryJar != null)
            {
                // We don't want to add all files to the classpath, so we only output the
                // resources contained in 'closure-whitelist.properites' to the output.
                Properties whiteList = new Properties();
                whiteList.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "royale/closure-whitelist.properites"));

                // Add the closure files from classpath.
                closureSourceFiles = getClasspathResources(closureLibraryJar, whiteList);
            }
        }
        if (closureSourceFiles == null)
        {
            File closureLibDir = new File(googConfiguration.getClosureLib());
            if (!closureLibDir.exists() || !closureLibDir.isDirectory())
            {
                //only throw this error if closure-lib is set because it
                //wouldn't make sense with the default fallback path
                if (googConfiguration.isClosureLibSet())
                {
                    throw new RuntimeException("Parameter 'closure-lib' doesn't point to a valid directory.");
                }
            }
            else
            {
                closureSourceFiles = getDirectoryResources(new File(closureLibDir, "closure"));
            }
        }
        if (closureSourceFiles == null || closureSourceFiles.size() == 0)
        {
            throw new RuntimeException(
                    "Parameter 'closure-lib' not specified and closure resources not available in classpath.");
        }
        // Dump a copy of the closure lib files to the intermediate directory. Without this
        // the application will not be able to run.
        for(SourceFile closureSourceFile : closureSourceFiles) {
            FileUtils.write(new File(new File(intermediateDir, "library/closure"),
                    closureSourceFile.getName()), closureSourceFile.getCode());
        }
        closureSourceFiles = closureFilesInOrder(intermediateDir + "/library/closure/", closureSourceFiles, "goog.events.EventTarget");


        /////////////////////////////////////////////////////////////////////////////////
        // Prepare the closure compilation.
        /////////////////////////////////////////////////////////////////////////////////

        JSClosureCompilerWrapper compilerWrapper = new JSClosureCompilerWrapper(googConfiguration.getJSCompilerOptions());


        /////////////////////////////////////////////////////////////////////////////////
        // Add all the closure lib files to the compilation unit.
        /////////////////////////////////////////////////////////////////////////////////

        for (SourceFile closureSourceFile : closureSourceFiles) {
            compilerWrapper.addJSSourceFile(closureSourceFile);
        }

        writeExportedNames(compilerWrapper);

        /////////////////////////////////////////////////////////////////////////////////
        // Add all the externs to the compilation
        /////////////////////////////////////////////////////////////////////////////////

        // Iterate over all swc dependencies and add all the externs they contain.
        // (Externs are located in a "externs" directory in the root of the SWC)
        List<ISWC> swcs = project.getLibraries();
        List<ISWC> allswcs = new ArrayList<ISWC>();
        allswcs.addAll(swcs);
        allswcs.addAll(themeSWCs);
        for (ISWC swc : allswcs)
        {
            Map<String, ISWCFileEntry> files = swc.getFiles();
            for (String key : files.keySet())
            {
                if (key.startsWith(ROYALE_EXTERNS))
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

                        // Write the extern into the filesystem.
                        // FIXME: I don't know why we need to do this.
                        //FileUtils.write(new File(intermediateDir, key), externFile.getCode());
                    }
                }
            }
        }


        /////////////////////////////////////////////////////////////////////////////////
        // Add all files generated by the compiler to the compilation unit.
        /////////////////////////////////////////////////////////////////////////////////

        GoogDepsWriter gdw = getGoogDepsWriter(intermediateDir, mainClassQName, googConfiguration, allswcs);
        // This list contains all files generated by the compiler, this is both the
        // compiled js files created by the sources of the current project plus the
        // js files of used dependencies.
        ArrayList<String> fileList = gdw.getListOfFiles(project, problems);
        for (String file : fileList) {
            compilerWrapper.addJSSourceFile(file);
        }


        /////////////////////////////////////////////////////////////////////////////////
        // Generate the index.html for loading the application.
        /////////////////////////////////////////////////////////////////////////////////

        // The application needs to import all dependencies the application needs, this
        // is generated here so it can be used for outputting the html templates.
        String depsFileData = gdw.generateDeps(project, problems);

        if (project.isModule(mainClassQName))
        {
        	// need better test someday
        	depsFileData += "\ngoog.require('" + mainClassQName + "');\n";
            writeFile(new File(intermediateDir, projectName + "__deps.js"), depsFileData, false);
            Set<String> provideds = computeProvideds(depsFileData);
            compilerWrapper.setProvideds(provideds);
            gdw.needCSS = true;
        }
        else
        {
	        File template = ((JSGoogConfiguration)configuration).getHtmlTemplate();
	        // Create the index.html for the debug-js version.
	        if (!((JSGoogConfiguration)configuration).getSkipTranspile()) {
	            if (template != null) {
	                writeTemplate(template, "intermediate", projectName, mainClassQName, intermediateDir, depsFileData, gdw.additionalHTML);
	            } else {
	                writeHTML("intermediate", projectName, mainClassQName, intermediateDir, depsFileData, gdw.additionalHTML);
	            }
	        }
	        // Create the index.html for the release-js version.
	        if (configuration.release()) {
	            if (template != null) {
	                writeTemplate(template, "release", projectName, mainClassQName, releaseDir, depsFileData, gdw.additionalHTML);
	            } else {
	                writeHTML("release", projectName, mainClassQName, releaseDir, null, gdw.additionalHTML);
	            }
	        }
        }        

        /////////////////////////////////////////////////////////////////////////////////
        // Generate or copy the main CSS resources.
        /////////////////////////////////////////////////////////////////////////////////

        project.needCSS = gdw.needCSS;
        if (project.needCSS || googConfiguration.getSkipTranspile()) {
            if (!googConfiguration.getSkipTranspile()) {
                writeCSS(projectName, intermediateDir);
            }
            if (project.needCSS && configuration.release()) {
                FileUtils.copyFile(new File(intermediateDir, projectName + ".css"),
                        new File(releaseDir, projectName + ".css"));
            }
        }


        /////////////////////////////////////////////////////////////////////////////////
        // If we are doing a release build, let the closure compiler do it's job.
        /////////////////////////////////////////////////////////////////////////////////

        if (configuration.release()) {
            final File projectReleaseMainFile = new File(releaseDir, outputFileName);
            compilerWrapper.setOptions(projectReleaseMainFile.getCanonicalPath(), useStrictPublishing, !googConfiguration.getRemoveCirculars(), projectName);
            compilerWrapper.targetFilePath = projectReleaseMainFile.getCanonicalPath();

            compilerWrapper.compile();

            appendSourceMapLocation(projectReleaseMainFile, projectName);
        }

        // if (ok)
        System.out.println("The project '" + projectName + "' has been successfully compiled and optimized.");

        return true;
    }

    private Set<String> computeProvideds(String data)
    {
    	HashSet<String> set = new HashSet<String>();
    	String[] lines = data.split("\n");
    	for (String line : lines)
    	{
    		int c = line.indexOf("['");
    		if (c != -1)
    		{
	    		int c2 = line.indexOf("'", c + 2);
	    		String name = line.substring(c + 2, c2);
	    		set.add(name);
    		}
    	}
    	return set;
    }
    
    protected List<SourceFile> closureFilesInOrder(String path, List<SourceFile> files, String entryPoint)
    {
    	ArrayList<String> sortedFiles = new ArrayList<String>();
    	HashMap<String, SourceFile> fileMap = new HashMap<String, SourceFile>();
    	SourceFile depsFile = null;
    	
    	for (SourceFile sourceFile : files)
    	{
    		if ((sourceFile.getOriginalPath().endsWith("goog/deps.js") || sourceFile.getOriginalPath().endsWith("goog\\deps.js")) &&
        		!(sourceFile.getOriginalPath().endsWith("third_party/goog/deps.js") || sourceFile.getOriginalPath().endsWith("third_party\\goog\\deps.js")))
    			depsFile = sourceFile;
    		System.out.println("originalPath: " + sourceFile.getOriginalPath());
    		fileMap.put(sourceFile.getOriginalPath(), sourceFile);
    	}
    	
		ArrayList<String> deps = new ArrayList<String>();
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path + depsFile.getOriginalPath()), "UTF8"));

            while (true)
            {
	            String line = in.readLine();
	            if (line.startsWith("//") || line.trim().length() == 0)
	            	continue;
	            deps.add(line);
            }
        }
        catch (Exception e)
        {
            // nothing to see, move along...
        }

        sortClosureFile(deps, entryPoint, sortedFiles);
        
        ArrayList<SourceFile> list = new ArrayList<SourceFile>();
        ArrayList<String> seen = new ArrayList<String>();
        sortedFiles.add("deps.js");
        sortedFiles.add("base.js");
        // in dual branch, add this to node publisher
        sortedFiles.add("bootstrap/nodejs.js");
        int n = sortedFiles.size();
        for (int i = n - 1; i >= 0; i--)
        {
        	String fileName = sortedFiles.get(i);
        	System.out.println("sorted filename: " + fileName);
        	if (seen.contains(fileName)) 
        		continue;
        	seen.add(fileName);
        	
        	SourceFile sf = fileMap.get("goog/" + fileName);
        	if (sf == null)
        		System.out.println("got null for " + fileName);
        	list.add(sf);
        }
        return list;
    }
    
    private void sortClosureFile(List<String> deps, String entryPoint, List<String> sortedFiles)
    {
    	String provided = getProvidedFile(deps, entryPoint);
        sortedFiles.add(provided);
        List<String> reqs = getRequires(deps, entryPoint);
        if (reqs == null) return;
        for (String req : reqs)
        {
        	sortClosureFile(deps, req, sortedFiles);
        }
    }
    
    private String getProvidedFile(List<String> deps, String name)
    {
    	for (String dep : deps)
    	{
    		int open = dep.indexOf("[");
    		int close = dep.indexOf("]");
			String list = dep.substring(open + 1, close);
			String[] parts = list.split(",");
			ArrayList<String> provideds = new ArrayList<String>();
			for (String part : parts)
			{
				part = part.trim();
				if (part.startsWith("'"))
					part = part.substring(1, part.length() - 1);
				provideds.add(part);    				
			}
    		if (provideds.contains(name))
    		{
    			open = dep.indexOf("'");
    			close = dep.indexOf("'", open + 1);
    			return dep.substring(open + 1, close);    			
    		}
    	}
    	return null;
    }
    
    private List<String> getRequires(List<String> deps, String name)
    {
    	for (String dep : deps)
    	{
    		int open = dep.indexOf("[");
    		int close = dep.indexOf("]");
			String list = dep.substring(open + 1, close);
			String[] parts = list.split(",");
			ArrayList<String> provideds = new ArrayList<String>();
			for (String part : parts)
			{
				part = part.trim();
				if (part.startsWith("'"))
					part = part.substring(1, part.length() - 1);
				provideds.add(part);    				
			}
    		if (provideds.contains(name))
    		{
    			open = dep.indexOf("[", close + 1);
    			close = dep.indexOf("]", open + 1);
    			if (open + 1 == close)
    				return null;
    			String list2 = dep.substring(open + 1, close);
    			String[] parts2 = list2.split(",");
    			ArrayList<String> reqs = new ArrayList<String>();
    			for (String part : parts2)
    			{
    				part = part.trim();
    				if (part.startsWith("'"))
    					part = part.substring(1, part.length() - 1);
    				reqs.add(part);    				
    			}
    			return reqs;
    		}
    	}
    	return null;
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

    protected void writeTemplate(File template, String type, String projectName, String mainClassQName, File targetDir, String deps, List<String> additionalHTML)
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
        String result = input.replaceAll("\\$\\{application\\}", mainClassQName);
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
		addHTML.append(getTemplateDependencies(type, projectName, mainClassQName, deps));
        result = result.replaceAll("\\$\\{head\\}", addHTML.toString());

        String templateBody = getTemplateBody("release".equals(type) ? projectName : mainClassQName);
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

    protected String getTemplateDependencies(String type, String projectName, String mainClassQName, String deps)
    {
        StringBuilder depsHTML = new StringBuilder();
        if ("intermediate".equals(type))
        {
            depsHTML.append("\t<script type=\"text/javascript\" src=\"./library/closure/goog/base.js\"></script>\n");
            depsHTML.append("\t<script type=\"text/javascript\">\n");
            depsHTML.append(deps);
            depsHTML.append("\t\tgoog.require(\"");
            depsHTML.append(mainClassQName);
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

	protected String getTemplateBody(String mainClassQName)
    {
        StringBuilder bodyHTML = new StringBuilder();
        bodyHTML.append("\t<script type=\"text/javascript\">\n");
        bodyHTML.append("\t\tnew ");
        bodyHTML.append(mainClassQName);
        bodyHTML.append("()");
        bodyHTML.append(".start();\n");
        bodyHTML.append("\t</script>\n");
        return bodyHTML.toString();
    }

    protected void writeHTML(String type, String projectName, String mainClassQName, File targetDir, String deps, List<String> additionalHTML)
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
        htmlFile.append(getTemplateDependencies(type, projectName, mainClassQName, deps));

        htmlFile.append("</head>\n");
        htmlFile.append("<body>\n");

        htmlFile.append(getTemplateBody(mainClassQName));

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
                        copyFontFile((CSSFunctionCallPropertyValue) value, dir, targetDir);
        			}
        		}
        		else
        		{
        	        if (prop instanceof CSSFunctionCallPropertyValue)
        	        {
                        copyFontFile((CSSFunctionCallPropertyValue) prop, dir, targetDir);
        	        }
        		}
        	}
        }
    }

    protected void copyFontFile(CSSFunctionCallPropertyValue fn, File sourceDir, File targetDir) throws IOException {
        String fontPath = fn.rawArguments;
        if (fontPath.startsWith("'")) {
            fontPath = fontPath.substring(1, fontPath.length() - 1);
        }
        if (fontPath.startsWith("\"")) {
            fontPath = fontPath.substring(1, fontPath.length() - 1);
        }
        int c = fontPath.indexOf("?");
        if (c != -1) {
            fontPath = fontPath.substring(0, c);
        }
        File fontFile = new File(sourceDir, fontPath);
        File destFile = new File(targetDir, fontPath);
        if (fontFile.exists())
        {
            if (!destFile.exists()) {
                FileUtils.copyFile(fontFile, destFile);
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
                    resourceJarPath = resourceJarPath.substring(resourceJarPath.indexOf(":") + 1);
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

    /**
     * In case of release builds, we also need the 'js-release' directory created.
     */
    @Override
    protected void setupOutputFolder() {
        super.setupOutputFolder();

        // Only create a release directory for release builds.
        if (configuration.release()) {
            File releaseDir = new File(outputParentFolder, ROYALE_RELEASE_DIR_NAME);
            if (!releaseDir.exists() && !releaseDir.mkdirs()) {
                throw new RuntimeException("Unable to create release directory at " + releaseDir.getAbsolutePath());
            }
        }
    }

    protected void clearEmptyDirectoryTrees(File baseDirectory) {
        File[] files = baseDirectory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    clearEmptyDirectoryTrees(file);
                    if (isEmptyDirectory(file)) {
                        file.delete();
                    }
                }
            }
        }
    }

    protected boolean isEmptyDirectory(File directory) {
        File[] files = directory.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void writeExportedNames(JSClosureCompilerWrapper compilerWrapper)
    {
    	if (!googConfiguration.getExportPublicSymbols())
    	{
    		// if not generating exports for every public symbol
    		// generate an externs file that blocks renaming
    		// of properties used by MXML and dataBinding.
	        Set<String> exportedNames = project.getExportedNames();
	        if (exportedNames.size() > 0)
	        {
	        	StringBuilder sb = new StringBuilder();
	        	sb.append("/**\n");
	        	sb.append(" * generated by Apache Royale compiler\n");
	        	sb.append(" * @externs\n");
	        	sb.append(" */\n");
	        	for (String name : exportedNames)
	        	{
	        		sb.append("\n\n");
		        	sb.append("/**\n");
		        	sb.append(" * @export\n");
		        	sb.append(" */\n");
		        	sb.append("Object.prototype." + name + ";\n");
	        	}
	        	File exportsFile = new File(outputFolder, "dontrename.js");
	        	try {
					writeFile(exportsFile, sb.toString(), false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	compilerWrapper.addJSExternsFile(exportsFile.getAbsolutePath());
	        }
    	}
    }

}
