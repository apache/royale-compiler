package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.flex.compiler.codegen.js.IJSPublisher;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.internal.codegen.js.JSSharedData;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogPublisher;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.utils.JSClosureCompilerUtil;

import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.deps.DepsGenerator;
import com.google.javascript.jscomp.deps.DepsGenerator.InclusionStrategy;

public class MXMLFlexJSPublisher extends JSGoogPublisher implements
        IJSPublisher
{

    public static final String FLEXJS_INTERMEDIATE_DIR_NAME = "bin/js-debug";
    public static final String FLEXJS_RELEASE_DIR_NAME = "bin/js-release";

    private File outputFolder;

    public MXMLFlexJSPublisher(Configuration config)
    {
        super(config);

        this.outputFolder = new File(configuration.getTargetFileDirectory())
                .getParentFile();
    }

    public File getOutputFolder()
    {
        return new File(outputFolder, FLEXJS_INTERMEDIATE_DIR_NAME);
    }

    public void publish() throws IOException
    {
        final String intermediateDirPath = getOutputFolder().getPath();

        final String projectName = FilenameUtils.getBaseName(configuration
                .getTargetFile());
        final String outputFileName = projectName
                + "." + JSSharedData.OUTPUT_EXTENSION;

        File releaseDir = new File(outputFolder, FLEXJS_RELEASE_DIR_NAME);
        final String releaseDirPath = releaseDir.getPath();
        if (releaseDir.exists())
            org.apache.commons.io.FileUtils.deleteQuietly(releaseDir);
        releaseDir.mkdirs();

        final String closureLibDirPath = ((JSGoogConfiguration) configuration)
                .getClosureLib();
        final String closureGoogSrcLibDirPath = closureLibDirPath
                + "/closure/goog/";
        final String closureGoogTgtLibDirPath = intermediateDirPath
                + "/library/closure/goog";
        final String closureTPSrcLibDirPath = closureLibDirPath
                + "/third_party/closure/goog/";
        final String closureTPTgtLibDirPath = intermediateDirPath
                + "/library/third_party/closure/goog";
        final String sdkJSLibSrcDirPath = ((JSGoogConfiguration) configuration)
                .getSDKJSLib();
        final String sdkJSLibTgtDirPath = intermediateDirPath;

        final String depsSrcFilePath = intermediateDirPath
                + "/library/closure/goog/deps.js";
        final String depsTgtFilePath = intermediateDirPath + "/deps.js";
        final String projectIntermediateJSFilePath = intermediateDirPath
                + File.separator + outputFileName;
        final String projectReleaseJSFilePath = releaseDirPath
                + File.separator + outputFileName;

        appendExportSymbol(projectIntermediateJSFilePath, projectName);

        copyFile(sdkJSLibSrcDirPath, sdkJSLibTgtDirPath);

        List<SourceFile> inputs = new ArrayList<SourceFile>();
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                new File(intermediateDirPath),
                new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            inputs.add(SourceFile.fromFile(file));
        }

        copyFile(closureGoogSrcLibDirPath, closureGoogTgtLibDirPath);
        copyFile(closureTPSrcLibDirPath, closureTPTgtLibDirPath);

        File srcDeps = new File(depsSrcFilePath);

        final List<SourceFile> deps = new ArrayList<SourceFile>();
        deps.add(SourceFile.fromFile(srcDeps));

        ErrorManager errorManager = new JSGoogErrorManager();
        DepsGenerator depsGenerator = new DepsGenerator(deps, inputs,
                InclusionStrategy.ALWAYS, closureGoogTgtLibDirPath,
                errorManager);
        writeFile(depsTgtFilePath, depsGenerator.computeDependencyCalls(),
                false);

        org.apache.commons.io.FileUtils.deleteQuietly(srcDeps);
        org.apache.commons.io.FileUtils.moveFile(new File(depsTgtFilePath),
                srcDeps);

        writeHTML("intermediate", projectName, intermediateDirPath);
        writeHTML("release", projectName, releaseDirPath);

        ArrayList<String> optionList = new ArrayList<String>();

        files = org.apache.commons.io.FileUtils.listFiles(new File(
                intermediateDirPath), new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            optionList.add("--js=" + file.getCanonicalPath());
        }

        optionList.add("--closure_entry_point=" + projectName);
        optionList.add("--only_closure_dependencies");
        optionList.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
        optionList.add("--js_output_file=" + projectReleaseJSFilePath);
        optionList.add("--output_manifest="
                + releaseDirPath + File.separator + "manifest.txt");
        optionList.add("--create_source_map="
                + projectReleaseJSFilePath + ".map");
        optionList.add("--source_map_format=" + SourceMap.Format.V3);

        String[] options = (String[]) optionList.toArray(new String[0]);

        JSClosureCompilerUtil.run(options);

        appendSourceMapLocation(projectReleaseJSFilePath, projectName);

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

    private void writeHTML(String type, String projectName, String dirPath)
            throws IOException
    {
        StringBuilder htmlFile = new StringBuilder();
        htmlFile.append("<!DOCTYPE html>\n");
        htmlFile.append("<html>\n");
        htmlFile.append("<head>\n");
        htmlFile.append("\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n");
        htmlFile.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");

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

        // TODO (erikdebruin) the utility methods should have their own place...
        htmlFile.append("\t\tfunction is(object, type) {return object;};\n");
        htmlFile.append("\t\t\n");
        
        htmlFile.append("\t\tnew ");
        htmlFile.append(projectName);
        htmlFile.append("()");
        htmlFile.append(".start()\n");
        htmlFile.append("\t</script>\n");
        htmlFile.append("</body>\n");
        htmlFile.append("</html>");

        writeFile(dirPath + File.separator + "index.html", htmlFile.toString(),
                false);
    }
}
