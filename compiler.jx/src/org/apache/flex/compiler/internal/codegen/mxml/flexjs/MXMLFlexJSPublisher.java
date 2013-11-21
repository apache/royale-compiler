package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.flex.compiler.codegen.js.IJSPublisher;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.internal.codegen.js.JSSharedData;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogPublisher;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.graph.GoogDepsWriter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.utils.JSClosureCompilerUtil;

import com.google.javascript.jscomp.SourceMap;

public class MXMLFlexJSPublisher extends JSGoogPublisher implements
        IJSPublisher
{

    public static final String FLEXJS_OUTPUT_DIR_NAME = "bin";
    public static final String FLEXJS_INTERMEDIATE_DIR_NAME = "js-debug";
    public static final String FLEXJS_RELEASE_DIR_NAME = "js-release";

    public MXMLFlexJSPublisher(Configuration config, FlexJSProject project)
    {
        super(config);

        this.isMarmotinniRun = ((JSGoogConfiguration) configuration)
                .getMarmotinni() != null;

        this.useStrictPublishing = ((JSGoogConfiguration) configuration)
                .getStrictPublish();

        this.project = project;
    }

    private FlexJSProject project;

    private boolean isMarmotinniRun;
    private boolean useStrictPublishing;

    @Override
    public File getOutputFolder()
    {
        // (erikdebruin) If there is a -marmotinni switch, we want
        //               the output redirected to the directory it specifies.
        if (isMarmotinniRun)
        {
            outputParentFolder = new File(
                    ((JSGoogConfiguration) configuration).getMarmotinni());
        }
        else
        {
            outputParentFolder = new File(
                    configuration.getTargetFileDirectory()).getParentFile();
        }

        outputParentFolder = new File(outputParentFolder,
                FLEXJS_OUTPUT_DIR_NAME);

        outputFolder = new File(outputParentFolder, File.separator
                + FLEXJS_INTERMEDIATE_DIR_NAME);

        // (erikdebruin) Marmotinni handles file management, so we 
        //               bypass the setup.
        if (!isMarmotinniRun)
            setupOutputFolder();

        return outputFolder;
    }

    @Override
    public void publish() throws IOException
    {
        final String intermediateDirPath = outputFolder.getPath();
        final File intermediateDir = new File(intermediateDirPath);
        File srcDir = new File(configuration.getTargetFile());
        srcDir = srcDir.getParentFile();

        final String projectName = FilenameUtils.getBaseName(configuration
                .getTargetFile());
        final String outputFileName = projectName
                + "." + JSSharedData.OUTPUT_EXTENSION;

        File releaseDir = new File(outputParentFolder, FLEXJS_RELEASE_DIR_NAME);
        final String releaseDirPath = releaseDir.getPath();

        if (!isMarmotinniRun)
        {
            if (releaseDir.exists())
                org.apache.commons.io.FileUtils.deleteQuietly(releaseDir);

            releaseDir.mkdirs();
        }

        final String closureLibDirPath = ((JSGoogConfiguration) configuration)
                .getClosureLib();
        final String closureGoogSrcLibDirPath = closureLibDirPath
                + "/closure/goog/";
        final String closureGoogTgtLibDirPath = intermediateDirPath
                + "/library/closure/goog";
        final String depsSrcFilePath = intermediateDirPath
                + "/library/closure/goog/deps.js";
        final String depsTgtFilePath = intermediateDirPath + "/deps.js";
        final String projectIntermediateJSFilePath = intermediateDirPath
                + File.separator + outputFileName;
        final String projectReleaseJSFilePath = releaseDirPath
                + File.separator + outputFileName;

        appendExportSymbol(projectIntermediateJSFilePath, projectName);
        appendEncodedCSS(projectIntermediateJSFilePath, projectName);

        // (erikdebruin) We need to leave the 'goog' files and dependencies well
        //               enough alone. We copy the entire library over so the 
        //               'goog' dependencies will resolve without our help.
        FileUtils.copyDirectory(new File(closureGoogSrcLibDirPath), new File(closureGoogTgtLibDirPath));

        GoogDepsWriter gdw = new GoogDepsWriter(intermediateDir, projectName, (JSGoogConfiguration) configuration);
        try
        {
            String depsFileData = gdw.generateDeps();
            writeFile(depsTgtFilePath, depsFileData, false);        
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        IOFileFilter pngSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".png"));
        IOFileFilter gifSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".gif"));
        IOFileFilter jpgSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".jpg"));
        IOFileFilter assetFiles = FileFilterUtils.or(pngSuffixFilter,
                jpgSuffixFilter, gifSuffixFilter);

        FileUtils.copyDirectory(srcDir, intermediateDir, assetFiles);
        FileUtils.copyDirectory(srcDir, releaseDir, assetFiles);

        File srcDeps = new File(depsSrcFilePath);

        writeHTML("intermediate", projectName, intermediateDirPath);
        writeHTML("release", projectName, releaseDirPath);
        writeCSS(projectName, intermediateDirPath);
        writeCSS(projectName, releaseDirPath);

        ArrayList<String> optionList = new ArrayList<String>();

        // (erikdebruin) add 'goog' files
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(new File(
                closureGoogTgtLibDirPath), new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            optionList.add("--js=" + file.getCanonicalPath());
        }

        // (erikdebruin) add project files
        for (String filePath : gdw.filePathsInOrder)
        {
            optionList.add("--js=" + new File(filePath).getCanonicalPath());
        }
        
        if (useStrictPublishing)
        {
            // (erikdebruin) set compiler flags to 'strictest' to allow maximum
            //               code optimization
            optionList.add("--define='goog.DEBUG=false'");
            optionList.add("--language_in=ECMASCRIPT5_STRICT");
            optionList.add("--warning_level=VERBOSE");
            optionList.add("--jscomp_warning=accessControls");
            optionList.add("--jscomp_warning=const");
            optionList.add("--jscomp_warning=constantProperty");
            optionList.add("--jscomp_warning=strictModuleDepCheck");
            optionList.add("--jscomp_warning=visibility");
            optionList.add("--jscomp_off=deprecated");
        }
        
        // (erikdebruin) Include the 'goog' deps to allow the compiler to resolve
        //               dependencies.
        optionList.add("--js=" + closureGoogSrcLibDirPath + File.separator + "deps.js");

        optionList.add("--closure_entry_point=" + projectName);
        optionList.add("--only_closure_dependencies");
        optionList.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
        optionList.add("--js_output_file=" + projectReleaseJSFilePath);
        optionList.add("--output_manifest="
                + releaseDirPath + File.separator + "manifest.txt");
        optionList.add("--create_source_map="
                + projectReleaseJSFilePath + ".map");
        optionList.add("--source_map_format=" + SourceMap.Format.V3);
        
        List<String> externs = ((JSGoogConfiguration)configuration).getExternalJSLib();
        for (String extern : externs)
            optionList.add("--externs=" + extern);

        String[] options = (String[]) optionList.toArray(new String[0]);

        JSClosureCompilerUtil.run(options);

        appendSourceMapLocation(projectReleaseJSFilePath, projectName);

        if (!isMarmotinniRun)
        {
            String allDeps = "";
            allDeps += FileUtils.readFileToString(srcDeps);
            allDeps += FileUtils.readFileToString(new File(depsTgtFilePath));
            
            FileUtils.writeStringToFile(srcDeps, allDeps);
            
            org.apache.commons.io.FileUtils.deleteQuietly(new File(depsTgtFilePath));
        }

        System.out.println("The project '"
                + projectName
                + "' has been successfully compiled and optimized.");
    }

    private void appendExportSymbol(String path, String projectName)
            throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString
                .append("\n\n// Ensures the symbol will be visible after compiler renaming.\n");
        appendString.append("goog.exportSymbol('");
        appendString.append(projectName);
        appendString.append("', ");
        appendString.append(projectName);
        appendString.append(");\n");
        writeFile(path, appendString.toString(), true);
    }

    private void appendEncodedCSS(String path, String projectName)
            throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString.append("\n\n");
        appendString.append(projectName);
        appendString.append(".prototype.cssData = [");
        String s = project.cssEncoding;
        int reqidx = s.indexOf("goog.require");
        if (reqidx != -1)
        {
            String reqs = s.substring(reqidx);
            s = s.substring(0, reqidx - 1);
            String fileData = readCode(new File(path));
            reqidx = fileData.indexOf("goog.require");
            String after = fileData.substring(reqidx);
            String before = fileData.substring(0, reqidx - 1);
            s = before + reqs + after + appendString.toString() + s;
            writeFile(path, s, false);
        }
        else
        {
            appendString.append(s);
            writeFile(path, appendString.toString(), true);
        }
    }
        
    protected String readCode(File file)
    {
        String code = "";
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF8"));

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

    private void writeHTML(String type, String projectName, String dirPath)
            throws IOException
    {
        StringBuilder htmlFile = new StringBuilder();
        htmlFile.append("<!DOCTYPE html>\n");
        htmlFile.append("<html>\n");
        htmlFile.append("<head>\n");
        htmlFile.append("\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n");
        htmlFile.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        htmlFile.append("\t<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + projectName + ".css\">\n");

        if (type == "intermediate")
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
        htmlFile.append("()");
        htmlFile.append(".start();\n");
        htmlFile.append("\t</script>\n");
        htmlFile.append("</body>\n");
        htmlFile.append("</html>");

        writeFile(dirPath + File.separator + "index.html", htmlFile.toString(),
                false);
    }

    private void writeCSS(String projectName, String dirPath)
            throws IOException
    {
        StringBuilder cssFile = new StringBuilder();
        cssFile.append(project.cssDocument);

        writeFile(dirPath + File.separator + projectName + ".css",
                cssFile.toString(), false);
    }
}
