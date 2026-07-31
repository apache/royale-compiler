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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.codegen.graph.CodeGraphExporter;
import org.apache.royale.compiler.internal.codegen.graph.CodeGraphModel;
import org.apache.royale.compiler.internal.codegen.graph.CodeGraphWriter;
import org.apache.royale.compiler.internal.driver.mxml.royale.MXMLRoyaleSWCBackend;
import org.apache.royale.compiler.internal.targets.RoyaleSWCTarget;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;

public class CODEGRAPH extends MXMLJSCRoyale
{
    public CODEGRAPH()
    {
        super(new MXMLRoyaleSWCBackend());
    }

    @Override
    public String getName()
    {
        return "codegraph";
    }

    @Override
    public int execute(String[] args)
    {
        return staticMainNoExit(args);
    }

    public static void main(final String[] args)
    {
        System.exit(staticMainNoExit(args));
    }

    public static int staticMainNoExit(final String[] args)
    {
        final CODEGRAPH codeGraph = new CODEGRAPH();
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        return codeGraph.mainNoExit(args, problems, true);
    }

    @Override
    protected boolean compile()
    {
        try
        {
            project.getSourceCompilationUnitFactory().addHandler(asFileHandler);
            if (!setupTargetFile())
                return false;
            buildArtifact();
            if (jsTarget == null)
                return false;
            if (!config.getCreateTargetWithErrors())
            {
                Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
                Collection<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();
                problems.getErrorsAndWarnings(errors, warnings);
                if (!errors.isEmpty())
                    return false;
            }

            Collection<IDefinition> definitions = new ArrayList<IDefinition>();
            for (ICompilationUnit compilationUnit : getReachableCompilationUnits())
            {
                definitions.addAll(compilationUnit.getFileScopeRequest().get().getExternallyVisibleDefinitions());
            }

            CodeGraphModel model = new CodeGraphExporter(project).export(definitions, getGraphTarget(), null);
            File outputFile = getGraphOutputFile();
            File parent = outputFile.getParentFile();
            if (parent != null && !parent.exists())
                parent.mkdirs();
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            try
            {
                new CodeGraphWriter().write(model, writer);
            }
            finally
            {
                writer.close();
            }
            return true;
        }
        catch (Exception exception)
        {
            problems.add(new InternalCompilerProblem(exception));
            return false;
        }
    }

    private Collection<ICompilationUnit> getReachableCompilationUnits() throws InterruptedException
    {
        Set<String> externs = config.getExterns();
        Collection<ICompilationUnit> roots = ((RoyaleSWCTarget)target).getReachableCompilationUnits(problems.getProblems());
        Collection<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(roots);
        Collection<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
        for (ICompilationUnit compilationUnit : reachableCompilationUnits)
        {
            if (compilationUnit.isInvisible())
                continue;
            ICompilationUnit.UnitType unitType = compilationUnit.getCompilationUnitType();
            if (unitType != ICompilationUnit.UnitType.AS_UNIT
                    && unitType != ICompilationUnit.UnitType.MXML_UNIT)
                continue;
            if (!isProjectSource(compilationUnit))
                continue;
            if (externs.contains(compilationUnit.getQualifiedNames().get(0)))
                continue;
            if (project.isExternalLinkage(compilationUnit))
                continue;
            result.add(compilationUnit);
        }
        return result;
    }

    private boolean isProjectSource(ICompilationUnit compilationUnit)
    {
        File sourceFile = new File(compilationUnit.getAbsoluteFilename()).getAbsoluteFile();
        if (project.isFileOnSourcePath(sourceFile))
            return true;
        if (sourceFile.equals(new File(config.getTargetFile()).getAbsoluteFile()))
            return true;
        for (String includeSource : config.getIncludeSources())
        {
            if (sourceFile.equals(new File(includeSource).getAbsoluteFile()))
                return true;
        }
        return false;
    }

    @Override
    protected boolean setupTargetFile() throws InterruptedException
    {
        ITargetSettings settings = getCodeGraphTargetSettings();
        if (settings == null)
            return false;
        project.setTargetSettings(settings);
        target = project.getBackend().createTarget(project, settings, null);
        return true;
    }

    private ITargetSettings getCodeGraphTargetSettings()
    {
        if (targetSettings == null)
        {
            Collection<File> includeSources = new ArrayList<File>();
            for (String includeSource : config.getIncludeSources())
                includeSources.add(new File(includeSource));
            File targetFile = new File(config.getTargetFile());
            if (!includeSources.contains(targetFile))
                includeSources.add(targetFile);
            projectConfigurator.setIncludeSources(includeSources);
            targetSettings = projectConfigurator.getTargetSettings(getTargetType());
        }
        if (targetSettings == null)
            problems.addAll(projectConfigurator.getConfigurationProblems());
        return targetSettings;
    }

    @Override
    protected void validateTargetFile()
    {
    }

    @Override
    protected TargetType getTargetType()
    {
        return TargetType.SWC;
    }

    private String getGraphTarget()
    {
        Map<String, String> definitions = config.getCompilerDefine();
        return definitions != null && "true".equals(definitions.get("COMPILE::SWF")) ? "swf" : "js";
    }

    private File getGraphOutputFile()
    {
        if (config.getOutput() != null)
            return new File(config.getOutput());
        return new File(FilenameUtils.removeExtension(config.getTargetFile()) + ".codegraph.json");
    }

}