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

package org.apache.flex.compiler.clients;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.flex.compiler.codegen.as.IASWriter;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.driver.js.IJSApplication;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.IOError;
import org.apache.flex.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.flex.compiler.internal.driver.as.ASBackend;
import org.apache.flex.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.flex.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSSWCBackend;
import org.apache.flex.compiler.internal.driver.mxml.jsc.MXMLJSCJSSWCBackend;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.targets.FlexJSSWCTarget;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.InternalCompilerProblem;
import org.apache.flex.compiler.problems.LibraryNotFoundProblem;
import org.apache.flex.compiler.problems.UnableToBuildSWFProblem;
import org.apache.flex.compiler.targets.ITarget.TargetType;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.swc.io.SWCReader;

/**
 * @author Erik de Bruin
 * @author Michael Schmalle
 */
public class COMPJSC extends MXMLJSC
{
    /*
     * Exit code enumerations.
     */
    static enum ExitCode
    {
        SUCCESS(0),
        PRINT_HELP(1),
        FAILED_WITH_PROBLEMS(2),
        FAILED_WITH_EXCEPTIONS(3),
        FAILED_WITH_CONFIG_PROBLEMS(4);

        ExitCode(int code)
        {
            this.code = code;
        }

        final int code;
    }

    @Override
    public String getName()
    {
        return FLEX_TOOL_COMPC;
    }

    @Override
    public int execute(String[] args)
    {
        return staticMainNoExit(args);
    }

    /**
     * Java program entry point.
     * 
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        int exitCode = staticMainNoExit(args);
        System.exit(exitCode);
    }

    /**
     * Entry point for the {@code <compc>} Ant task.
     *
     * @param args Command line arguments.
     * @return An exit code.
     */
    public static int staticMainNoExit(final String[] args)
    {
        long startTime = System.nanoTime();

        IBackend backend = new ASBackend();
        for (String s : args)
        {
            if (s.contains("-js-output-type"))
            {
                jsOutputType = JSOutputType.fromString(s.split("=")[1]);

                switch (jsOutputType)
                {
                case AMD:
                    backend = new AMDBackend();
                    break;

                case JSC:
                    backend = new MXMLJSCJSSWCBackend();
                    break;

                case FLEXJS:
                case FLEXJS_DUAL:
                    backend = new MXMLFlexJSSWCBackend();
                    break;

                case GOOG:
                    backend = new GoogBackend();
                    break;

                default:
                    throw new UnsupportedOperationException();
                }
            }
        }

        final COMPJSC mxmlc = new COMPJSC(backend);
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    public COMPJSC(IBackend backend)
    {
        super(backend);
    }

    /**
     * Main body of this program. This method is called from the public static
     * method's for this program.
     * 
     * @return true if compiler succeeds
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected boolean compile()
    {
        boolean compilationSuccess = false;

        try
        {
            project.getSourceCompilationUnitFactory().addHandler(asFileHandler);

            if (setupTargetFile())
                buildArtifact();

            if (jsTarget != null)
            {
                Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
                Collection<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();

                if (!config.getCreateTargetWithErrors())
                {
                    problems.getErrorsAndWarnings(errors, warnings);
                    if (errors.size() > 0)
                        return false;
                }

                boolean packingSWC = false;
                String outputFolderName = getOutputFilePath();
            	File swcFile = new File(outputFolderName);
            	File jsOut = new File("js/out");
                ZipFile zipFile = null;
            	ZipOutputStream zipOutputStream = null;
            	String catalog = null;
            	StringBuilder fileList = new StringBuilder();
                if (outputFolderName.endsWith(".swc"))
                {
                	packingSWC = true;
                	if (!swcFile.exists())
                	{
                		problems.add(new LibraryNotFoundProblem(outputFolderName));
                		return false;
                	}
                    zipFile = new ZipFile(swcFile, ZipFile.OPEN_READ);
                    final InputStream catalogInputStream = SWCReader.getInputStream(zipFile, SWCReader.CATALOG_XML);
                    
                    catalog = IOUtils.toString(catalogInputStream);
                    catalogInputStream.close();
                    zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFolderName + ".new")));
                    zipOutputStream.setLevel(Deflater.NO_COMPRESSION);
                    for (final Enumeration<? extends ZipEntry> entryEnum = zipFile.entries(); entryEnum.hasMoreElements();)
                    {
                        final ZipEntry entry = entryEnum.nextElement();
                        if (!entry.getName().contains("js/out") &&
                        	!entry.getName().contains(SWCReader.CATALOG_XML))
                        {
                            System.out.println("Copy " + entry.getName());
                        	InputStream input = zipFile.getInputStream(entry);
                        	zipOutputStream.putNextEntry(new ZipEntry(entry.getName()));
                        	IOUtils.copy(input, zipOutputStream);
                            zipOutputStream.flush();
                        	zipOutputStream.closeEntry();
                        }
                    }
                    int filesIndex = catalog.indexOf("<files>");
                    if (filesIndex != -1)
                    {
                    	int filesIndex2 = catalog.indexOf("</files>");
                    	String files = catalog.substring(filesIndex, filesIndex2);
                    	int fileIndex = files.indexOf("<file", 6);
                    	int pathIndex = files.indexOf("path=");
                    	while (pathIndex != -1)
                    	{
                    		int pathIndex2 = files.indexOf("\"", pathIndex + 6);
                    		int fileIndex2 = files.indexOf("/>", fileIndex);
                    		String path = files.substring(pathIndex + 6, pathIndex2);
                    		if (!path.startsWith("js/out"))
                    		{
                    			fileList.append(files.substring(fileIndex - 8, fileIndex2 + 3));
                    		}
                    		pathIndex = files.indexOf("path=", pathIndex2);
                    		fileIndex = files.indexOf("<file", fileIndex2);
                    	}
                        catalog = catalog.substring(0, filesIndex) + catalog.substring(filesIndex2 + 8);
                    }
                }

                File outputFolder = null;
                if (!packingSWC) 
                	outputFolder = new File(outputFolderName);

                Set<String> externs = config.getExterns();
                Collection<ICompilationUnit> roots = ((FlexJSSWCTarget)target).getReachableCompilationUnits(errors);
                Collection<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(roots);
                for (final ICompilationUnit cu : reachableCompilationUnits)
                {
                    ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();

                    if (cuType == ICompilationUnit.UnitType.AS_UNIT
                            || cuType == ICompilationUnit.UnitType.MXML_UNIT)
                    {
                    	String symbol = cu.getQualifiedNames().get(0);
                    	if (externs.contains(symbol)) continue;
                    	
                    	if (project.isExternalLinkage(cu)) continue;
                    	
                    	if (!packingSWC)
                    	{
	                        final File outputClassFile = getOutputClassFile(
	                                cu.getQualifiedNames().get(0), outputFolder);
	
	                        System.out.println("Compiling file: " + outputClassFile);
	
	                        ICompilationUnit unit = cu;
	
	                        IASWriter writer;
	                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
	                        {
	                            writer = project.getBackend().createWriter(project,
	                                    (List<ICompilerProblem>) errors, unit,
	                                    false);
	                        }
	                        else
	                        {
	                            writer = project.getBackend().createMXMLWriter(
	                                    project, (List<ICompilerProblem>) errors,
	                                    unit, false);
	                        }
	                        problems.addAll(errors);
	                        BufferedOutputStream out = new BufferedOutputStream(
	                                new FileOutputStream(outputClassFile));
	                        writer.writeTo(out);
	                        out.flush();
	                        out.close();
	                        writer.close();
                    	}
                    	else
                    	{
                    		String outputClassFile = getOutputClassFile(
	                                cu.getQualifiedNames().get(0), jsOut).getPath();
	                        System.out.println("Compiling file: " + outputClassFile);
	                    	
	                        ICompilationUnit unit = cu;
	
	                        IASWriter writer;
	                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
	                        {
	                            writer = project.getBackend().createWriter(project,
	                                    (List<ICompilerProblem>) errors, unit,
	                                    false);
	                        }
	                        else
	                        {
	                            writer = project.getBackend().createMXMLWriter(
	                                    project, (List<ICompilerProblem>) errors,
	                                    unit, false);
	                        }
	                        problems.addAll(errors);
	                        zipOutputStream.putNextEntry(new ZipEntry(outputClassFile));
	                        writer.writeTo(zipOutputStream);
                            zipOutputStream.flush();
	                        zipOutputStream.closeEntry();
	                        writer.close();
	                        fileList.append("        <file path=\"" + outputClassFile + "\" mod=\"" + System.currentTimeMillis() + "\"/>\n");
                    	}
                    }
                }
                if (packingSWC)
                {
                	zipFile.close();
                	int libraryIndex = catalog.indexOf("</libraries>");
                	catalog = catalog.substring(0, libraryIndex + 13) +
                		"    <files>\n" + fileList.toString() + "    </files>" + 
                		catalog.substring(libraryIndex + 13);
                    zipOutputStream.putNextEntry(new ZipEntry(SWCReader.CATALOG_XML));
                	zipOutputStream.write(catalog.getBytes());
                    zipOutputStream.flush();
                    zipOutputStream.closeEntry();
                    zipOutputStream.flush();
                	zipOutputStream.close();
                	swcFile.delete();
                	File newSWCFile = new File(outputFolderName + ".new");
                	newSWCFile.renameTo(swcFile);
                }
                compilationSuccess = true;
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
            final ICompilerProblem problem = new InternalCompilerProblem(e);
            problems.add(problem);
        }

        return compilationSuccess;
    }

    /**
     * Build target artifact.
     * 
     * @throws InterruptedException threading error
     * @throws IOException IO error
     * @throws ConfigurationException
     */
    @Override
    protected void buildArtifact() throws InterruptedException, IOException,
            ConfigurationException
    {
        jsTarget = buildJSTarget();
    }

    private IJSApplication buildJSTarget() throws InterruptedException,
            FileNotFoundException, ConfigurationException
    {
        final List<ICompilerProblem> problemsBuildingSWF = new ArrayList<ICompilerProblem>();

        final IJSApplication app = buildApplication(project,
                config.getMainDefinition(), null, problemsBuildingSWF);
        problems.addAll(problemsBuildingSWF);
        if (app == null)
        {
            ICompilerProblem problem = new UnableToBuildSWFProblem(
                    getOutputFilePath());
            problems.add(problem);
        }

        return app;
    }

    /**
     * Replaces FlexApplicationProject::buildSWF()
     * 
     * @param applicationProject
     * @param rootClassName
     * @param problems
     * @return
     * @throws InterruptedException
     */

    private IJSApplication buildApplication(CompilerProject applicationProject,
            String rootClassName, ICompilationUnit mainCU,
            Collection<ICompilerProblem> problems) throws InterruptedException,
            ConfigurationException, FileNotFoundException
    {
        Collection<ICompilerProblem> fatalProblems = applicationProject.getFatalProblems();
        if (!fatalProblems.isEmpty())
        {
            problems.addAll(fatalProblems);
            return null;
        }

        return ((JSTarget) target).build(mainCU, problems);
    }

    /**
     * Get the output file path. If {@code -output} is specified, use its value;
     * otherwise, use the same base name as the target file.
     * 
     * @return output file path
     */
    private String getOutputFilePath()
    {
        if (config.getOutput() == null)
        {
            final String extension = "." + project.getBackend().getOutputExtension();
            return FilenameUtils.removeExtension(config.getTargetFile()).concat(
                    extension);
        }
        else
        {
            String outputFolderName = config.getOutput();
            return outputFolderName;
        }
    }

    /**
     * Get the output class file. This includes the (sub)directory in which the
     * original class file lives. If the directory structure doesn't exist, it
     * is created.
     * 
     * @author Erik de Bruin
     * @param qname
     * @param outputFolder
     * @return output class file path
     */
    private File getOutputClassFile(String qname, File outputFolder)
    {
        String[] cname = qname.split("\\.");
        String sdirPath = outputFolder + File.separator;
        if (cname.length > 0)
        {
            for (int i = 0, n = cname.length - 1; i < n; i++)
            {
                sdirPath += cname[i] + File.separator;
            }

            File sdir = new File(sdirPath);
            if (!sdir.exists())
                sdir.mkdirs();

            qname = cname[cname.length - 1];
        }

        return new File(sdirPath + qname + "." + project.getBackend().getOutputExtension());
    }

    /**
     * Mxmlc uses target file as the main compilation unit and derive the output
     * SWF file name from this file.
     * 
     * @return true if successful, false otherwise.
     * @throws InterruptedException
     */
    @Override
    protected boolean setupTargetFile() throws InterruptedException
    {
        config.getTargetFile();

        ITargetSettings settings = getTargetSettings();
        if (settings != null)
            project.setTargetSettings(settings);
        else
            return false;

        target = project.getBackend().createTarget(project,
                getTargetSettings(), null);

        return true;
    }

    private ITargetSettings getTargetSettings()
    {
        if (targetSettings == null)
            targetSettings = projectConfigurator.getTargetSettings(getTargetType());

        if (targetSettings == null)
            problems.addAll(projectConfigurator.getConfigurationProblems());

        return targetSettings;
    }

    /**
     * Validate target file.
     * 
     * @throws MustSpecifyTarget
     * @throws IOError
     */
    @Override
    protected void validateTargetFile() throws ConfigurationException
    {

    }

    protected String getProgramName()
    {
        return "compc";
    }

    protected boolean isCompc()
    {
        return true;
    }

    @Override
    protected TargetType getTargetType()
    {
        return TargetType.SWC;
    }
}
