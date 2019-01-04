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
package org.apache.royale.compiler.internal.codegen.js.goog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.codegen.js.IJSPublisher;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.internal.codegen.js.JSPublisher;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.utils.JSClosureCompilerUtil;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.deps.DepsGenerator;
import com.google.javascript.jscomp.deps.DepsGenerator.InclusionStrategy;
import com.google.javascript.jscomp.deps.ModuleLoader;

public class JSGoogPublisher extends JSPublisher implements IJSPublisher
{

    public static final String GOOG_INTERMEDIATE_DIR_NAME = "js-intermediate";
    public static final String GOOG_RELEASE_DIR_NAME = "js-release";

    protected JSGoogConfiguration googConfiguration;

    public JSGoogPublisher(RoyaleJSProject project, Configuration config)
    {
        super(project, config);
        googConfiguration = (JSGoogConfiguration) config;
    }

    @Override
    public File getOutputFolder()
    {
        outputParentFolder = new File(configuration.getTargetFileDirectory()).getParentFile();
        outputFolder = new File(outputParentFolder, JSGoogPublisher.GOOG_INTERMEDIATE_DIR_NAME);

        setupOutputFolder();

        return outputFolder;
    }

    @Override
    public boolean publish(ProblemQuery problems) throws IOException
    {
        final String intermediateDirPath = getOutputFolder().getPath();

        final String projectName = FilenameUtils.getBaseName(configuration.getTargetFile());
        final String outputFileName = projectName + "." + project.getBackend().getOutputExtension();

        File releaseDir = new File(
                new File(intermediateDirPath).getParentFile(),
                GOOG_RELEASE_DIR_NAME);
        final String releaseDirPath = releaseDir.getPath();
        if (releaseDir.exists()) {
            FileUtils.deleteQuietly(releaseDir);
        }
        releaseDir.mkdir();

        final File closureLibDir = new File(googConfiguration.getClosureLib());
        final File closureGoogSrcLibDir = new File(closureLibDir, "/closure/goog/");
        final File closureGoogTgtLibDir = new File(intermediateDirPath, "/library/closure/goog");
        final File closureTPSrcLibDir = new File(closureLibDir, "/third_party/closure/goog/");
        final File closureTPTgtLibDir = new File(intermediateDirPath, "/library/third_party/closure/goog");

        final File depsSrcFile = new File(intermediateDirPath, "/library/closure/goog/deps.js");
        final File depsTgtFile = new File(intermediateDirPath, "/deps.js");
        final File projectIntermediateJSFile = new File(intermediateDirPath, outputFileName);
        final File projectReleaseJSFile = new File(releaseDirPath, outputFileName);

        appendExportSymbol(projectIntermediateJSFile, projectName);

        List<SourceFile> inputs = new ArrayList<SourceFile>();
        Collection<File> files = FileUtils.listFiles(
                new File(intermediateDirPath),
                new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            inputs.add(SourceFile.fromFile(file.getAbsolutePath()));
        }

        copyFile(closureGoogSrcLibDir, closureGoogTgtLibDir);
        copyFile(closureTPSrcLibDir, closureTPTgtLibDir);

        final List<SourceFile> deps = new ArrayList<SourceFile>();
        deps.add(SourceFile.fromFile(depsSrcFile.getAbsolutePath()));

        ErrorManager errorManager = new JSGoogErrorManager();
        DepsGenerator depsGenerator = new DepsGenerator(deps, inputs,
                InclusionStrategy.ALWAYS, closureGoogTgtLibDir.getCanonicalPath(),
                errorManager, ModuleLoader.EMPTY);
        writeFile(depsTgtFile, depsGenerator.computeDependencyCalls(),false);

        FileUtils.deleteQuietly(depsSrcFile);
        FileUtils.moveFile(depsTgtFile, depsSrcFile);

        // XXX (mschmalle) until we figure out what is going on with this configuration, just skip
        // HTML generation for JSC output type
        //String outputType = googConfiguration.getJSOutputType();
        //if (!outputType.equals(JSOutputType.JSC.getText()))
        //{
            writeHTML("intermediate", projectName, intermediateDirPath);
            writeHTML("release", projectName, releaseDirPath);
        //}

        ArrayList<String> optionList = new ArrayList<String>();

        files = FileUtils.listFiles(new File(
                intermediateDirPath), new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            optionList.add("--js=" + file.getCanonicalPath());
        }

        optionList.add("--closure_entry_point=" + projectName);
        optionList.add("--only_closure_dependencies");
        optionList.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
        optionList.add("--js_output_file=" + projectReleaseJSFile);
        optionList.add("--output_manifest=" + releaseDirPath + File.separator
                + "manifest.txt");
        optionList.add("--create_source_map=" + projectReleaseJSFile
                + ".map");
        optionList.add("--source_map_format=" + SourceMap.Format.V3);

        String[] options = optionList.toArray(new String[0]);

        JSClosureCompilerUtil.run(options);

        appendSourceMapLocation(projectReleaseJSFile, projectName);

        System.out.println("The project '" + projectName
                + "' has been successfully compiled and optimized.");

        return true;
    }

    private void appendExportSymbol(File targetFile, String projectName)
            throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString.append("\n\n// Ensures the symbol will be visible after compiler renaming.\n");
        appendString.append("goog.exportSymbol('");
        appendString.append(projectName);
        appendString.append("', ");
        appendString.append(projectName);
        appendString.append(");\n");
        writeFile(targetFile, appendString.toString(), true);
    }

    protected void appendSourceMapLocation(File path, String projectName)
            throws IOException
    {
        if (!googConfiguration.getSourceMap())
        {
            return;
        }
        StringBuilder appendString = new StringBuilder();
        appendString.append("\n//# sourceMappingURL=./" + projectName
                + ".js.map");
        writeFile(path, appendString.toString(), true);
    }

    protected void copyFile(File source, File target) throws IOException
    {
        if (source.isDirectory()) {
            FileUtils.copyDirectory(source, target);
        } else {
            FileUtils.copyFile(source, target);
        }
    }

    protected void writeHTML(String type, String projectName, String dirPath)
            throws IOException
    {
        StringBuilder htmlFile = new StringBuilder();
        htmlFile.append("<!DOCTYPE html>\n");
        htmlFile.append("<html>\n");
        htmlFile.append("<head>\n");
        htmlFile.append("\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n");
        htmlFile.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");

        if ("intermediate".equals(type))
        {
            htmlFile.append("\t<script type=\"text/javascript\" src=\"./library/closure/goog/base.js\"></script>\n");
            htmlFile.append("\t<script type=\"text/javascript\">\n");
            htmlFile.append("\t\tgoog.require(\"");
            htmlFile.append(projectName);
            htmlFile.append("\");\n");
            htmlFile.append("\t</script>\n");
        }
        else
        {
            htmlFile.append("\t<script type=\"text/javascript\" src=\"./");
            htmlFile.append(projectName);
            htmlFile.append(".js\"></script>\n");
        }

        htmlFile.append("</head>\n");
        htmlFile.append("<body>\n");
        htmlFile.append("\t<script type=\"text/javascript\">\n");
        htmlFile.append("\t\tnew ");
        htmlFile.append(projectName);
        htmlFile.append("();\n");
        htmlFile.append("\t</script>\n");
        htmlFile.append("</body>\n");
        htmlFile.append("</html>");

        writeFile(new File(dirPath, "index.html"), htmlFile.toString(),false);
    }

    protected void writeFile(File target, String content, boolean append)
            throws IOException
    {
        if (!target.exists()) {
            target.createNewFile();
        }

        FileWriter fw = new FileWriter(target, append);
        fw.write(content);
        fw.close();
    }

    protected List<SourceFile> getClasspathResources(File jarFile) throws IOException {
        return getClasspathResources(jarFile, null);
    }

    protected List<SourceFile> getClasspathResources(File jarFile, Properties whiteList) throws IOException {
        List<SourceFile> sourceFiles = new LinkedList<SourceFile>();

        JarFile jar = null;
        try {
            jar = new JarFile(jarFile);
            for (Enumeration<JarEntry> jarEntries = jar.entries(); jarEntries.hasMoreElements(); ) {
                JarEntry jarEntry = jarEntries.nextElement();
                String fileName = jarEntry.getName();
                // Add only JS files and if a white-list is specified, only files on that white-list.
                if (fileName.endsWith(".js") && ((whiteList == null) || (whiteList.containsKey(fileName)))) {
                    // Dump the file.
                    InputStream is = jar.getInputStream(jarEntry);
                    String code = IOUtils.toString(is, "UTF-8");
                    SourceFile sourceFile = new JarSourceFile(jarEntry.getName(), code, false);
                    is.close();
                    sourceFiles.add(sourceFile);
                }
            }
        } finally {
            if(jar != null) {
                jar.close();
            }
        }

        return sourceFiles;
    }

    protected List<SourceFile> getDirectoryResources(File directory) throws IOException {
        List<SourceFile> sourceFiles = new LinkedList<SourceFile>();

        Collection<File> files = FileUtils.listFiles(directory,
                new RegexFileFilter("^.*(\\.js)"), DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            String relative = directory.toURI().relativize(file.toURI()).getPath();
            String code = FileUtils.readFileToString(file, "UTF-8");
            SourceFile sourceFile = new JarSourceFile(relative, code, false);
            sourceFiles.add(sourceFile);
        }

        return sourceFiles;
    }

    protected void dumpJar(File jarFile, File outputDir) throws IOException
    {
        // TODO (mschmalle) for some reason ide thinks this has not been closed
        JarFile jar = new JarFile(jarFile);

        for (Enumeration<JarEntry> jarEntries = jar.entries(); jarEntries.hasMoreElements();)
        {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.getName().endsWith("/"))
            {
                File file = new File(outputDir, jarEntry.getName());

                // Check if the parent directory exists. If not -> create it.
                File dir = file.getParentFile();
                if (!dir.exists())
                {
                    if (!dir.mkdirs())
                    {
                        jar.close();
                        throw new IOException("Unable to create directory "
                                + dir.getAbsolutePath());
                    }
                }

                // Dump the file.
                InputStream is = jar.getInputStream(jarEntry);
                FileOutputStream fos = new FileOutputStream(file);
                while (is.available() > 0)
                {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        }

        jar.close();
    }

    public class JSGoogErrorManager implements ErrorManager
    {
        @Override
        public void setTypedPercent(double arg0)
        {
        }

        @Override
        public void report(CheckLevel arg0, JSError arg1)
        {
        }

        @Override
        public JSError[] getWarnings()
        {
            return null;
        }

        @Override
        public int getWarningCount()
        {
            return 0;
        }

        @Override
        public double getTypedPercent()
        {
            return 0;
        }

        @Override
        public JSError[] getErrors()
        {
            return null;
        }

        @Override
        public int getErrorCount()
        {
            return 0;
        }

        @Override
        public void generateReport()
        {
        }
    }
}
