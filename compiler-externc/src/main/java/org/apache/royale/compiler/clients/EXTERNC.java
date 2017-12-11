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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.config.ExterncConfigurator;
import org.apache.royale.compiler.internal.codegen.typedefs.emit.ReferenceEmitter;
import org.apache.royale.compiler.internal.codegen.typedefs.pass.ReferenceCompiler;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.flex.tools.FlexTool;

import com.google.javascript.jscomp.Result;

/**
 * @author Michael Schmalle
 */
public class EXTERNC implements FlexTool
{
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

    public ProblemQuery problems;
    protected Configurator projectConfigurator;
    private ExternCConfiguration configuration;
    private ReferenceModel model;
    private ReferenceCompiler compiler;
    private ReferenceEmitter emitter;

    public ReferenceModel getModel()
    {
        return model;
    }

    public ReferenceCompiler getCompiler()
    {
        return compiler;
    }

    public ReferenceEmitter getEmitter()
    {
        return emitter;
    }

    public EXTERNC()
    {
    }

    public EXTERNC(ExternCConfiguration configuration)
    {
        configure(configuration);
    }

    public boolean configure(String[] args)
    {
        projectConfigurator = createConfigurator();
        projectConfigurator.setConfiguration(args, "typedefs", false);
        projectConfigurator.getTargetSettings(TargetType.SWC);
        configure((ExternCConfiguration) projectConfigurator.getConfiguration());
        problems = new ProblemQuery(
                projectConfigurator.getCompilerProblemSettings());
        problems.addAll(projectConfigurator.getConfigurationProblems());
        if (problems.hasErrors())
        {
            return false;
        }
        return true;
    }

    public void configure(ExternCConfiguration configuration)
    {
        this.configuration = configuration;

        model = new ReferenceModel(configuration);
        compiler = new ReferenceCompiler(model);
        emitter = new ReferenceEmitter(model);
    }

    /**
     * Create a new Configurator. This method may be overridden to allow
     * Configurator subclasses to be created that have custom configurations.
     * 
     * @return a new instance or subclass of {@link Configurator}.
     */
    protected Configurator createConfigurator()
    {
        return new ExterncConfigurator(ExternCConfiguration.class);
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
     * Entry point for the <code>externc</code>.
     *
     * @param args Command line arguments.
     * @return An exit code.
     */
    public static int staticMainNoExit(final String[] args)
    {
        long startTime = System.nanoTime();

        final EXTERNC compiler = new EXTERNC();
        final int exitCode = compiler.mainNoExit(args, System.err);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    public int mainNoExit(final String[] args, OutputStream stderr)
    {
        int exitCode = -1;
        try
        {
            exitCode = _mainNoExit(args);
        }
        catch (Exception e)
        {
            PrintWriter writer = new PrintWriter(stderr);
            writer.println(e.toString());
        }
        finally
        {
            final ProblemFormatter formatter = new ProblemFormatter();
            final ProblemPrinter printer = new ProblemPrinter(formatter, stderr);
            printer.printProblems(problems.getFilteredProblems());
        }
        return exitCode;
    }

    public int _mainNoExit(final String[] args)
    {
        ExitCode exitCode = ExitCode.SUCCESS;

        try
        {
            final boolean continueCompilation = configure(args);
            if (continueCompilation)
            {
                model.problems = problems;
                cleanOutput();
                compile();
                emit();
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
            if (problems == null)
            {
                System.err.println(e.getMessage());
            }
            else
            {
                final ICompilerProblem unexpectedExceptionProblem = new UnexpectedExceptionProblem(
                        e);
                problems.add(unexpectedExceptionProblem);
            }
            exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
        }

        return exitCode.code;
    }

    public void cleanOutput() throws IOException
    {
        FileUtils.deleteDirectory(configuration.getAsRoot());
    }

    public void emit() throws IOException
    {
        emitter.emit();
    }

    public Result compile() throws IOException
    {
        return compiler.compile();
    }

    @Override
    public String getName() {
        // TODO: Change this to a flex-tool-api constant ...
        return "EXTERNC";
    }

    @Override
    public int execute(String[] args) {
        EXTERNC generator = new EXTERNC();
        generator.configure(args);
        try {
            generator.cleanOutput();
            /*Result result =*/ generator.compile();
            // We ignore errors for now ... they seem to be normal.
            /*if(result.errors.length > 0) {
                return 1;
            }*/
            generator.emit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
