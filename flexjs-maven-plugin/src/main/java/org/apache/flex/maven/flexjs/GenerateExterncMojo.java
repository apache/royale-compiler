package org.apache.flex.maven.flexjs;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.shared.utils.StringUtils;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by christoferdutz on 30.04.16.
 */
@Mojo(name="generate-extern",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class GenerateExterncMojo
        extends BaseMojo
{

    @Parameter
    private FileSet[] externcInput;

    @Parameter(defaultValue = "generated-sources/externc")
    private String outputDirectoryName;

    @Parameter
    private ExterncConfig externcConfig;

    @Override
    protected String getToolGroupName() {
        return "FlexJS";
    }

    @Override
    protected String getFlexTool() {
        return "EXTERNC";
    }

    @Override
    protected String getConfigFileName() {
        return "generate-externc-config.xml";
    }

    @Override
    protected boolean skip() {
        return externcInput == null;
    }

    @Override
    protected File getOutput() {
        return new File(outputDirectory, outputDirectoryName);
    }

    @Override
    protected VelocityContext getVelocityContext() throws MojoExecutionException {
        VelocityContext context = super.getVelocityContext();

        List<File> includedFiles = new LinkedList<File>();
        FileSetManager fileSetManager = new FileSetManager();
        for(FileSet fileSet : externcInput) {
            String[] fileSetIncludes = fileSetManager.getIncludedFiles(fileSet);
            if((fileSetIncludes != null) && (fileSetIncludes.length > 0)) {
                for(String include : fileSetIncludes) {
                    includedFiles.add(new File(fileSet.getDirectory(), include));
                }
            }
        }
        context.put("sourcePath", includedFiles);
        if(externcConfig != null) {
            context.put("classExcludes", externcConfig.classExcludes);
            context.put("fieldExcludes", externcConfig.fieldExcludes);
            context.put("excludes", externcConfig.excludes);
        }
        
        return context;
    }

    @Override
    public void execute() throws MojoExecutionException {
        File outputDirectory = getOutput();
        if(!outputDirectory.exists()) {
            if(!outputDirectory.mkdirs()) {
                throw new MojoExecutionException("Could not create output directory " + outputDirectory.getPath());
            }
        }

        super.execute();

        // Add eventually generated source paths to the project.
        if(outputDirectory.exists()) {
            File[] typeDirectories = outputDirectory.listFiles();
            if(typeDirectories != null) {
                for (File typeDirectory : typeDirectories) {
                    project.addCompileSourceRoot(typeDirectory.getPath());
                }
            }
        }
    }
}
