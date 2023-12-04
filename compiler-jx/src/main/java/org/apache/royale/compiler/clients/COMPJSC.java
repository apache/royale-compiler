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

import java.util.*;

import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogCompcConfiguration;
import org.apache.royale.compiler.internal.parsing.as.RoyaleASDocDelegate;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.targets.ITarget.TargetType;
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
        FAILED_WITH_ERRORS(2),
        FAILED_WITH_EXCEPTIONS(3),
        FAILED_WITH_CONFIG_PROBLEMS(4),
        WATCHING(1000);

        ExitCode(int code)
        {
            this.code = code;
        }

        final int code;
        
        int getCode()
        {
        	return code;
        }
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
        if (exitCode != ExitCode.WATCHING.getCode())
        {
            System.exit(exitCode);
        }
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
                        //passing true to RoyaleASDocDelegate constructor, to make it behave (outwardly) the same
                        //as NilASDocDelegate, which would be the default ASDocDelegate, if the following
                        //setASDocDelegate assignment was not made. This allows for 'processing' of ASDocComments in
                        //the 'afterDefinition' call.
                        compc.workspace.setASDocDelegate(new RoyaleASDocDelegate(true));
	                    compc.configurationClass = JSGoogCompcConfiguration.class;
	                    result = compc.mainNoExit(removeJSArgs(args));
	                    if (result != COMPC.ExitCode.SUCCESS.getCode() && result != COMPC.ExitCode.WATCHING.getCode())
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
	                    if (result != COMPJSCRoyale.ExitCode.SUCCESS.getCode() && result != COMPJSCRoyale.ExitCode.WATCHING.getCode())
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                case JS_NATIVE:
                    case JS_NODE:
	                	COMPJSCNative jsc = new COMPJSCNative();
	                	lastCompiler = jsc;
	                    result = jsc.mainNoExit(removeASArgs(args), problems.getProblems(), false);
	                    if (result != COMPJSCNative.ExitCode.SUCCESS.getCode() && result != COMPJSCNative.ExitCode.WATCHING.getCode())
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                // if you add a new target here, don't forget to also add it
	                // to flex2.tools.MxmlJSC in flex-compiler-oem for IDE support
	                }
            	}
                if (problems.hasFilteredProblems())
                {
                    if (problems.hasErrors())
                        exitCode = ExitCode.FAILED_WITH_ERRORS;
                    // no exit code for warnings because anything except 0 is
                    // detected as a failure by various build tools
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
            if (!config.getWatch() || !ExitCode.SUCCESS.equals(exitCode))
            {
                waitAndClose();
            }

            if (outProblems != null && problems.hasFilteredProblems())
            {
                for (ICompilerProblem problem : problems.getFilteredProblems())
                {
                    outProblems.add(problem);
                }
            }
        }
        if (config.getWatch() && ExitCode.SUCCESS.equals(exitCode))
        {
            exitCode = ExitCode.WATCHING;
        }
        return exitCode.getCode();
    }

    public COMPJSC()
    {
        super();
    }

    @Override
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
