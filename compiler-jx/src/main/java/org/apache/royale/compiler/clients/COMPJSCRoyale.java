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

package org.apache.royale.compiler.clients;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.codegen.js.IJSWriter;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.driver.js.IJSApplication;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.IOError;
import org.apache.royale.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.royale.compiler.internal.driver.mxml.royale.MXMLRoyaleSWCBackend;
import org.apache.royale.compiler.internal.parsing.as.RoyaleASDocDelegate;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.targets.RoyaleSWCTarget;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.LibraryNotFoundProblem;
import org.apache.royale.compiler.problems.UnableToBuildSWFProblem;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.swc.io.SWCReader;

/**
 * @author Erik de Bruin
 * @author Michael Schmalle
 */
public class COMPJSCRoyale extends MXMLJSCRoyale
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

        final COMPJSCRoyale mxmlc = new COMPJSCRoyale();
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    public COMPJSCRoyale()
    {
        IBackend backend = new MXMLRoyaleSWCBackend();

        workspace = new Workspace();
        workspace.setASDocDelegate(new RoyaleASDocDelegate());
        project = new RoyaleJSProject(workspace, backend);
        problems = new ProblemQuery(); // this gets replaced in configure().  Do we need it here?
        asFileHandler = backend.getSourceFileHandlerInstance();
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
            	File externsOut = new File("externs");
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
                            if (config.isVerbose())
                            {
                                System.out.println("Copy " + entry.getName());
                            }
                        	InputStream input = zipFile.getInputStream(entry);
                        	ZipEntry ze = new ZipEntry(entry.getName());
                        	ze.setMethod(ZipEntry.STORED);
                        	ze.setSize(entry.getSize());
                        	ze.setCompressedSize(entry.getCompressedSize());
                        	ze.setCrc(entry.getCrc());
	                        long fileDate = System.currentTimeMillis();
	                        long zipFileDate = fileDate;
	                    	String metadataDate = targetSettings.getSWFMetadataDate();
	                    	if (metadataDate != null)
	                    	{
	                    		String metadataFormat = targetSettings.getSWFMetadataDateFormat();
	                    		try {
	                    			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	                    			fileDate = sdf.parse(metadataDate).getTime();
	                    		} catch (ParseException e) {
	                				// TODO Auto-generated catch block
	                				e.printStackTrace();
	                			} catch (IllegalArgumentException e1) {
	                				e1.printStackTrace();
	                			}
	                    		// TODO: Fix this the same way that the compiler normalized the date
	                    		// strip off timezone.  Zip format doesn't store timezone
	                    		// and the goal is to have the same date and time regardless
	                    		// of which timezone the build machine is using.
	                    		int c = metadataDate.lastIndexOf(' ');
	                    		if(c != -1) {
                                    metadataDate = metadataDate.substring(0, c);
                                }
	                    		c = metadataFormat.lastIndexOf(' ');
	                    		if(c != -1) {
                                    metadataFormat = metadataFormat.substring(0, c);
                                }
	                    		try {
	                    			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    zipFileDate = sdf.parse(metadataDate).getTime();
	                    		} catch (ParseException e) {
	                				// TODO Auto-generated catch block
	                				e.printStackTrace();
	                			} catch (IllegalArgumentException e1) {
	                				e1.printStackTrace();
	                			}
	                    	}
	                    	ze.setTime(zipFileDate);
                        	zipOutputStream.putNextEntry(ze);
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
                Collection<ICompilationUnit> roots = ((RoyaleSWCTarget)target).getReachableCompilationUnits(errors);
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
	                                cu.getQualifiedNames().get(0), outputFolder, true);
	
                            if (config.isVerbose())
                            {
                                System.out.println("Compiling file: " + outputClassFile);
                            }
	
	                        ICompilationUnit unit = cu;
	
	                        IJSWriter writer;
	                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
	                        {
	                            writer = (IJSWriter) project.getBackend().createWriter(project,
	                                    (List<ICompilerProblem>) problems.getProblems(), unit,
	                                    false);
	                        }
	                        else
	                        {
	                            writer = (IJSWriter) project.getBackend().createMXMLWriter(
	                                    project, (List<ICompilerProblem>) problems.getProblems(),
	                                    unit, false);
	                        }

                            BufferedOutputStream out = new BufferedOutputStream(
	                                new FileOutputStream(outputClassFile));
                            BufferedOutputStream sourceMapOut = null;
	                        File outputSourceMapFile = null;
                            if (project.config.getSourceMap())
                            {
	                            outputSourceMapFile = getOutputSourceMapFile(
                                        cu.getQualifiedNames().get(0), outputFolder, true);
                                sourceMapOut = new BufferedOutputStream(
                                    new FileOutputStream(outputSourceMapFile));
                            }
	                        writer.writeTo(out, sourceMapOut, outputSourceMapFile);
                            out.flush();
	                        out.close();
                            if (sourceMapOut != null)
                            {
                                sourceMapOut.flush();
                                sourceMapOut.close();
                            }
                            writer.close();
                    	}
                    	else
                    	{
	                        if (config.isVerbose())
                            {
                                System.out.println("Compiling file: " + cu.getQualifiedNames().get(0));
                            }
	                    	
	                        ICompilationUnit unit = cu;
	
	                        IJSWriter writer;
	                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
	                        {
	                            writer = (IJSWriter) project.getBackend().createWriter(project,
	                                    (List<ICompilerProblem>) problems.getProblems(), unit,
	                                    false);
	                        }
	                        else
	                        {
	                            writer = (IJSWriter) project.getBackend().createMXMLWriter(
	                                    project, (List<ICompilerProblem>) problems.getProblems(),
	                                    unit, false);
	                        }

                            ByteArrayOutputStream temp = new ByteArrayOutputStream();
                            ByteArrayOutputStream sourceMapTemp = null;
                            
                            boolean isExterns = false;
                            if(cu.getDefinitionPromises().size() > 0)
                            {
                                isExterns = project.isExterns(cu.getDefinitionPromises().get(0).getQualifiedName());
                            }
	                        
                            // if the file is @externs DON'T create source map file
                            if (project.config.getSourceMap() && !isExterns)
	                        {
                                sourceMapTemp = new ByteArrayOutputStream();
	                        }
                            writer.writeTo(temp, sourceMapTemp, null);

                    		String outputClassFile = getOutputClassFile(
                                    cu.getQualifiedNames().get(0),
                                    isExterns ? externsOut : jsOut,
                                    false).getPath();
                    		outputClassFile = outputClassFile.replace('\\', '/');
	                        if (config.isVerbose())
                            {
                                System.out.println("Writing file: " + outputClassFile);     	
                            }
	                        long fileDate = System.currentTimeMillis();
	                        long zipFileDate = fileDate;
	                    	String metadataDate = targetSettings.getSWFMetadataDate();
	                    	if (metadataDate != null)
	                    	{
	                    		String metadataFormat = targetSettings.getSWFMetadataDateFormat();
	                    		try {
	                    			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    fileDate = sdf.parse(metadataDate).getTime();
	                    		} catch (ParseException e) {
	                				// TODO Auto-generated catch block
	                				e.printStackTrace();
	                			} catch (IllegalArgumentException e1) {
	                				e1.printStackTrace();
	                			}
                                // TODO: Fix this the same way that the compiler normalized the date
                                // strip off timezone.  Zip format doesn't store timezone
                                // and the goal is to have the same date and time regardless
                                // of which timezone the build machine is using.
                                int c = metadataDate.lastIndexOf(' ');
                                if(c != -1) {
                                    metadataDate = metadataDate.substring(0, c);
                                }
                                c = metadataFormat.lastIndexOf(' ');
                                if(c != -1) {
                                    metadataFormat = metadataFormat.substring(0, c);
                                }
	                    		try {
	                    			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    zipFileDate = sdf.parse(metadataDate).getTime();
	                    		} catch (ParseException e) {
	                				// TODO Auto-generated catch block
	                				e.printStackTrace();
	                			} catch (IllegalArgumentException e1) {
	                				e1.printStackTrace();
	                			}
	                    	}
	                    	ZipEntry ze = new ZipEntry(outputClassFile);
	                    	ze.setTime(zipFileDate);
	                    	ze.setMethod(ZipEntry.STORED);
	                    	
	                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                        temp.writeTo(baos);
	                        ze.setSize(baos.size());
	                        ze.setCompressedSize(baos.size());
	                        CRC32 crc = new CRC32();
	                        crc.reset();
	                        crc.update(baos.toByteArray());
	                        ze.setCrc(crc.getValue());

	                        zipOutputStream.putNextEntry(ze);
	                        baos.writeTo(zipOutputStream);
                            zipOutputStream.flush();
                            zipOutputStream.closeEntry();
                            fileList.append("        <file path=\"" + outputClassFile + "\" mod=\"" + fileDate + "\"/>\n");
                            
                            if(sourceMapTemp != null)
                            {
                                String sourceMapFile = getOutputSourceMapFile(
                                                                                cu.getQualifiedNames().get(0),
                                                                                isExterns ? externsOut : jsOut,
                                                                                false).getPath();
                                if (config.isVerbose())
                                {
                                    System.out.println("Writing file: " + sourceMapFile);
                                }
                                ze = new ZipEntry(sourceMapFile);
                                ze.setTime(zipFileDate);
                                ze.setMethod(ZipEntry.STORED);
                                
                                baos = new ByteArrayOutputStream();
                                sourceMapTemp.writeTo(baos);
                                ze.setSize(baos.size());
                                ze.setCompressedSize(baos.size());
                                crc = new CRC32();
                                crc.reset();
                                crc.update(baos.toByteArray());
                                ze.setCrc(crc.getValue());
                                
                                zipOutputStream.putNextEntry(ze);
                                baos.writeTo(zipOutputStream);
                                zipOutputStream.flush();
                                zipOutputStream.closeEntry();
                                fileList.append("        <file path=\"" + sourceMapFile + "\" mod=\"" + fileDate + "\"/>\n");
                            }
	                        writer.close();
                    	}
                    }
                }
                if (!config.getCreateTargetWithErrors())
                {
                	errors.clear();
                	warnings.clear();
                    problems.getErrorsAndWarnings(errors, warnings);
                    if (errors.size() > 0)
                        return false;
                }
                if (packingSWC)
                {
                	zipFile.close();
                    long fileDate = System.currentTimeMillis();
                    long zipFileDate = fileDate;
                	String metadataDate = targetSettings.getSWFMetadataDate();
                	if (metadataDate != null)
                	{
                		String metadataFormat = targetSettings.getSWFMetadataDateFormat();
                		try {
                			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            fileDate = sdf.parse(metadataDate).getTime();
                		} catch (ParseException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			} catch (IllegalArgumentException e1) {
            				e1.printStackTrace();
            			}
                        // TODO: Fix this the same way that the compiler normalized the date
                		// strip off timezone.  Zip format doesn't store timezone
                		// and the goal is to have the same date and time regardless
                		// of which timezone the build machine is using.
                		int c = metadataDate.lastIndexOf(' ');
                        if(c != -1) {
                            metadataDate = metadataDate.substring(0, c);
                        }
                        c = metadataFormat.lastIndexOf(' ');
                        if(c != -1) {
                            metadataFormat = metadataFormat.substring(0, c);
                        }
                		try {
                			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                            zipFileDate = sdf.parse(metadataDate).getTime();
                		} catch (ParseException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			} catch (IllegalArgumentException e1) {
            				e1.printStackTrace();
            			}
                	}
                	int libraryIndex = catalog.indexOf("</libraries>");
                	catalog = catalog.substring(0, libraryIndex + 13) +
                		"    <files>\n" + fileList.toString() + "    </files>" + 
                		catalog.substring(libraryIndex + 13);
                	ZipEntry ze = new ZipEntry(SWCReader.CATALOG_XML);
                	ze.setTime(zipFileDate);
                	ze.setMethod(ZipEntry.STORED);
                	
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                	baos.write(catalog.getBytes());
                    ze.setSize(baos.size());
                    ze.setCompressedSize(baos.size());
                    CRC32 crc = new CRC32();
                    crc.reset();
                    crc.update(baos.toByteArray());
                    ze.setCrc(crc.getValue());
                	
                    zipOutputStream.putNextEntry(ze);
                    baos.writeTo(zipOutputStream);
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
     * Replaces RoyaleApplicationProject::buildSWF()
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
     * is created if specified.
     * 
     * @author Erik de Bruin
     * @param qname
     * @param outputFolder
     * @param createDirs
     * @return output class file path
     */
    private File getOutputClassFile(String qname, File outputFolder, boolean createDirs)
    {
        String[] cname = qname.split("\\.");
        String sdirPath = outputFolder + File.separator;
        if (cname.length > 0)
        {
            for (int i = 0, n = cname.length - 1; i < n; i++)
            {
                sdirPath += cname[i] + File.separator;
            }

            if (createDirs)
            {
                File sdir = new File(sdirPath);
                if (!sdir.exists())
                    sdir.mkdirs();
            }

            qname = cname[cname.length - 1];
        }

        return new File(sdirPath + qname + "." + project.getBackend().getOutputExtension());
    }

    /**
     * Similar to getOutputClassFile, but for the source map file.
     * 
     * @param qname
     * @param outputFolder
     * @param createDirs
     * @return output source map file path
     */
    private File getOutputSourceMapFile(String qname, File outputFolder, boolean createDirs)
    {
        String[] cname = qname.split("\\.");
        String sdirPath = outputFolder + File.separator;
        if (cname.length > 0)
        {
            for (int i = 0, n = cname.length - 1; i < n; i++)
            {
                sdirPath += cname[i] + File.separator;
            }

            if (createDirs)
            {
                File sdir = new File(sdirPath);
                if (!sdir.exists())
                    sdir.mkdirs();
            }

            qname = cname[cname.length - 1];
        }

        return new File(sdirPath + qname + "." + project.getBackend().getOutputExtension() + ".map");
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

    @Override
    public List<String> getSourceList()
    {
        ArrayList<String> list = new ArrayList<String>();
        try
        {
            Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
            Collection<ICompilationUnit> roots = ((RoyaleSWCTarget)target).getReachableCompilationUnits(errors);
            Collection<ICompilationUnit> units = project.getReachableCompilationUnitsInSWFOrder(roots);
            for (ICompilationUnit unit : units)
            {
                UnitType ut = unit.getCompilationUnitType();
                if (ut == UnitType.AS_UNIT || ut == UnitType.MXML_UNIT)
                {
                    list.add(unit.getAbsoluteFilename());
                }
            }
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return list;
    }

    @Override
    public String getMainSource()
    {
        return null;
    }

}
