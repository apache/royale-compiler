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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.codegen.js.IJSWriter;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.driver.js.IJSApplication;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.IOError;
import org.apache.royale.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogCompcConfiguration;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.targets.RoyaleSWCTarget;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.LibraryNotFoundProblem;
import org.apache.royale.compiler.problems.UnableToBuildSWFProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.io.SWCReader;
import org.apache.royale.utils.ArgumentUtil;

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

        final COMPJSC mxmlc = new COMPJSC();
        mxmlc.configurationClass = JSGoogCompcConfiguration.class;
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    @Override
    public int mainNoExit(final String[] args, List<ICompilerProblem> problems,
            Boolean printProblems)
    {
        int exitCode = -1;
        try
        {
            exitCode = _mainNoExit(ArgumentUtil.fixArgs(args), problems);
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
        finally
        {
            if (problems != null && !problems.isEmpty())
            {
                if (printProblems)
                {
                    final WorkspaceProblemFormatter formatter = new WorkspaceProblemFormatter(
                            workspace);
                    final ProblemPrinter printer = new ProblemPrinter(formatter);
                    printer.printProblems(problems);
                }
            }
        }
        return exitCode;
    }

    /**
     * Entry point that doesn't call <code>System.exit()</code>. This is for
     * unit testing.
     * 
     * @param args command line arguments
     * @return exit code
     */
    @SuppressWarnings("incomplete-switch")
	private int _mainNoExit(final String[] args,
            List<ICompilerProblem> outProblems)
    {
    	System.out.println("args:");
    	for (String arg : args)
    		System.out.println(arg);
        ExitCode exitCode = ExitCode.SUCCESS;
        try
        {
            final boolean continueCompilation = configure(args);
        	CompilerDiagnosticsConstants.diagnostics = config.getDiagnosticsLevel();

/*            if (outProblems != null && !config.isVerbose())
                JSSharedData.STDOUT = JSSharedData.STDERR = null;*/

            if (continueCompilation)
            {
                List<String> targets = config.getCompilerTargets();
                for (String target : targets)
                    System.out.println("target:" + target);
            	targetloop:
            	for (String target : config.getCompilerTargets())
            	{
            		int result = 0;
            		switch (JSTargetType.fromString(target))
	                {
	                case SWF:
                        System.out.println("COMPC");
	                    COMPC compc = new COMPC();
	                    mxmlc = compc;
	                    compc.configurationClass = JSGoogCompcConfiguration.class;
	                    result = compc.mainNoExit(removeJSArgs(args));
	                    if (result != 0)
	                    {
	                    	problems.addAll(compc.problems.getProblems());
	                    	break targetloop;
	                    }
	                    break;
	                case JS_ROYALE:
                        System.out.println("COMPCJSCRoyale");
	                	COMPJSCRoyale royale = new COMPJSCRoyale();
	                	lastCompiler = royale;
	                    result = royale.mainNoExit(removeASArgs(args), problems.getProblems(), false);
	                    if (result != 0)
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                case JS_NATIVE:
                    case JS_NODE:
	                	COMPJSCNative jsc = new COMPJSCNative();
	                	lastCompiler = jsc;
	                    result = jsc.mainNoExit(removeASArgs(args), problems.getProblems(), false);
	                    if (result != 0)
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                // if you add a new js-output-type here, don't forget to also add it
	                // to flex2.tools.MxmlJSC in flex-compiler-oem for IDE support
	                }
            	}
                if (problems.hasFilteredProblems())
                {
                    if (problems.hasErrors())
                        exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
                    else
                        exitCode = ExitCode.FAILED_WITH_PROBLEMS;
                }
            }
            else if (problems.hasFilteredProblems())
            {
                exitCode = ExitCode.FAILED_WITH_CONFIG_PROBLEMS;
            }
            else
            {
                exitCode = ExitCode.PRINT_HELP;
            }
        }
        catch (Exception e)
        {
            if (outProblems == null) {
                System.err.println(e.getMessage());
            } else
            {
                final ICompilerProblem unexpectedExceptionProblem = new UnexpectedExceptionProblem(
                        e);
                problems.add(unexpectedExceptionProblem);
            }
            exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
        }
        finally
        {
            waitAndClose();

            if (outProblems != null && problems.hasFilteredProblems())
            {
                for (ICompilerProblem problem : problems.getFilteredProblems())
                {
                    outProblems.add(problem);
                }
            }
        }
        return exitCode.code;
    }

    public COMPJSC()
    {
        super();
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
                        	ze.setTime(entry.getTime());
                        	ze.setSize(entry.getSize());
                        	ze.setCompressedSize(entry.getCompressedSize());
                        	ze.setCrc(entry.getCrc());
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
	                                    (List<ICompilerProblem>) errors, unit,
	                                    false);
	                        }
	                        else
	                        {
	                            writer = (IJSWriter) project.getBackend().createMXMLWriter(
	                                    project, (List<ICompilerProblem>) errors,
	                                    unit, false);
	                        }
	                        problems.addAll(errors);

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
	                                    (List<ICompilerProblem>) errors, unit,
	                                    false);
	                        }
	                        else
	                        {
	                            writer = (IJSWriter) project.getBackend().createMXMLWriter(
	                                    project, (List<ICompilerProblem>) errors,
	                                    unit, false);
	                        }
	                        problems.addAll(errors);
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
	                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                        temp.writeTo(baos);
                            writeFileToZip(zipOutputStream, outputClassFile, baos, fileList);
                            if(sourceMapTemp != null)
                            {
                                String sourceMapFile = getOutputSourceMapFile(
                                    cu.getQualifiedNames().get(0),
                                    isExterns ? externsOut : jsOut,
                                    false).getPath();
                                sourceMapFile = sourceMapFile.replace('\\', '/');
                                if (config.isVerbose())
                                {
                                    System.out.println("Writing file: " + sourceMapFile);
                                }
    	                        baos = new ByteArrayOutputStream();
                                sourceMapTemp.writeTo(baos);
                                writeFileToZip(zipOutputStream, sourceMapFile, baos, fileList);
                            }
                            writer.close();
                        }
                    }
                    else if (cuType == ICompilationUnit.UnitType.SWC_UNIT)
                    {
                    	String symbol = cu.getQualifiedNames().get(0);
                    	if (externs.contains(symbol)) continue;
                    	if (project.isExternalLinkage(cu)) continue;
                    	if (!packingSWC)
                    	{
                            // we probably shouldn't skip this -JT
                            continue;
                        }

                        // if another .swc file is on our library-path, we must
                        // include the .js (and .js.map) files because the
                        // bytecode will also be included. if we have the
                        // bytecode, but not the .js files, the compiler won't
                        // know where to find the .js files. that's really bad.

                        // if the bytecode and .js files should not be included,
                        // then the developer is expected to use
                        // external-library-path instead of library-path.

                        SWCCompilationUnit swcCU = (SWCCompilationUnit) cu;
                        String outputClassFile = getOutputClassFile(
                                cu.getQualifiedNames().get(0),
                                jsOut,
                                false).getPath();
                        outputClassFile = outputClassFile.replace('\\', '/');
                        ISWCFileEntry fileEntry = swcCU.getSWC().getFile(outputClassFile);
                        if (fileEntry == null)
                        {
                            continue;
                        }
                        if (config.isVerbose())
                        {
                            System.out.println("Writing file: " + outputClassFile + " from SWC: " + swcCU.getAbsoluteFilename());
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        InputStream fileStream = fileEntry.createInputStream();
                        IOUtils.copy(fileStream, baos);
                        fileStream.close();
                        writeFileToZip(zipOutputStream, outputClassFile, baos, fileList);

                        String outputMapFile = outputClassFile + ".map";
                        fileEntry = swcCU.getSWC().getFile(outputMapFile);
                        if (fileEntry == null)
                        {
                            continue;
                        }
                        if (config.isVerbose())
                        {
                            System.out.println("Writing file: " + outputMapFile + " from SWC: " + swcCU.getAbsoluteFilename());
                        }
                        baos = new ByteArrayOutputStream();
                        fileStream = fileEntry.createInputStream();
                        IOUtils.copy(fileStream, baos);
                        fileStream.close();
                        writeFileToZip(zipOutputStream, outputMapFile, baos, fileList);
                    }
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
                			Date d = sdf.parse(metadataDate);
                			Calendar cal = new GregorianCalendar();
                			cal.setTime(d);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                			d = sdf.parse(metadataDate);
                			fileDate = d.getTime();
                			ZonedDateTime zdt = ZonedDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 
                									cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 0, ZoneId.systemDefault());
                			zipFileDate = zdt.toInstant().toEpochMilli();
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
                			Date d = sdf.parse(metadataDate);
                			// zip file wants dates in local time
                			Calendar cal = new GregorianCalendar();
                			cal.setTime(d);
                            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                			d = sdf.parse(metadataDate);
                			// other timestamps are in UTC
                			fileDate = d.getTime();
                			ZonedDateTime zdt = ZonedDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 
                									cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 0, ZoneId.systemDefault());
                			zipFileDate = zdt.toInstant().toEpochMilli();
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

    private void writeFileToZip(ZipOutputStream zipOutputStream, String entryFilePath, ByteArrayOutputStream baos, StringBuilder fileList) throws IOException
    {
        long fileDate = System.currentTimeMillis();
        long zipFileDate = fileDate;
        String metadataDate = targetSettings.getSWFMetadataDate();
        if (metadataDate != null)
        {
            String metadataFormat = targetSettings.getSWFMetadataDateFormat();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                Date d = sdf.parse(metadataDate);
                Calendar cal = new GregorianCalendar();
                cal.setTime(d);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                d = sdf.parse(metadataDate);
                fileDate = d.getTime();
                ZonedDateTime zdt = ZonedDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 
                                        cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), 0, ZoneId.systemDefault());
                zipFileDate = zdt.toInstant().toEpochMilli();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();
            }
        }
        ZipEntry ze = new ZipEntry(entryFilePath);
        ze.setTime(zipFileDate);
        ze.setMethod(ZipEntry.STORED);
        
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
        fileList.append("        <file path=\"" + entryFilePath + "\" mod=\"" + fileDate + "\"/>\n");
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
     * is created, if specified.
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
}
