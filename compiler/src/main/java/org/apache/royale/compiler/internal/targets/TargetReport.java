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

package org.apache.royale.compiler.internal.targets;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITargetReport;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swf.types.RGB;
import com.google.common.collect.ImmutableSet;

/**
 * Implementation of the {@link ITargetReport} interface.
 */
public class TargetReport implements ITargetReport
{
    TargetReport(ICompilerProject project, 
            ImmutableSet<ICompilationUnit> reachableCompilationUnits, 
            List<RSLSettings> requiredRSLs,
            RGB bgColor,
            ITargetSettings targetSettings, 
            ITargetAttributes targetAttributes,
            LinkageChecker linkageChecker) throws InterruptedException
    {
        this.project = project;
        this.bgColor = bgColor;
        this.targetSettings = targetSettings;
        this.linkageChecker = linkageChecker;
        definitionNames = new HashSet<String>();
        assetCompilationUnits = new HashSet<String>();
        sourceCompilationUnits = new HashSet<String>();
        libraryCompilationUnits = new HashSet<String>();
        this.requiredRSLs = requiredRSLs;
        
        initReportFromReachableCompilationUnits(reachableCompilationUnits);

        Float attrWidth = targetAttributes.getWidth();
        if (attrWidth != null)
            width = attrWidth.intValue();
        else
            width = 0;

        Float attrHeight = targetAttributes.getHeight();
        if (attrHeight != null)
            height = attrHeight.intValue();
        else
            height = 0;

        Double attrWidthPercent = targetAttributes.getWidthPercentage();
        if (attrWidthPercent != null)
            widthPercent = attrWidthPercent.doubleValue();
        else
            widthPercent = 0.0;

        Double attrHeightPercent = targetAttributes.getHeightPercentage();
        if (attrHeightPercent != null)
            heightPercent = attrHeightPercent.doubleValue();
        else
            heightPercent = 0.0;

        pageTitle = targetAttributes.getPageTitle();
    }

    @SuppressWarnings("unused")
    private final ICompilerProject project;
    @SuppressWarnings("unused")
    private final ITargetSettings targetSettings;
    private final Set<String> definitionNames;
    private final Set<String> assetCompilationUnits;
    private final Set<String> sourceCompilationUnits;
    private final Set<String> libraryCompilationUnits;
    private final List<RSLSettings> requiredRSLs;
    private final RGB bgColor;
    private final int width;
    private final int height;
    private final double widthPercent;
    private final double heightPercent;
    private final String pageTitle;
    private final LinkageChecker linkageChecker;
    
    @Override
    public Set<String> getAssetNames()
    {
        return assetCompilationUnits;
    }

    @Override
    public Set<String> getSourceNames()
    {
        return sourceCompilationUnits;
    }

    @Override
    public Set<String> getLibraryNames()
    {
        return libraryCompilationUnits;
    }

    @Override
    public Set<String> getDefinitionNames()
    {
        return definitionNames;
    }

    @Override
    public List<RSLSettings> getRequiredRSLs()
    {
        return requiredRSLs != null ? requiredRSLs : Collections.<RSLSettings>emptyList();
    }
    
    @Override
    public RGB getBackgroundColor()
    {
        return bgColor;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public double getWidthPercent()
    {
        return widthPercent;
    }

    @Override
    public double getHeightPercent()
    {
        return heightPercent;
    }

    @Override
    public String getPageTitle()
    {
        return pageTitle;
    }

    private void initReportFromReachableCompilationUnits(ImmutableSet<ICompilationUnit> reachableCompilationUnits) throws InterruptedException
    {
        for (ICompilationUnit compilationUnit : reachableCompilationUnits)
        {
            if (!linkageChecker.isExternal(compilationUnit))
                definitionNames.addAll(compilationUnit.getQualifiedNames());

            switch (compilationUnit.getCompilationUnitType())
            {
                case FXG_UNIT:
                case EMBED_UNIT:
                    assetCompilationUnits.add(compilationUnit.getAbsoluteFilename());
                    break;
                case AS_UNIT:
                case ABC_UNIT:
                case CSS_UNIT:
                case MXML_UNIT:
                    sourceCompilationUnits.add(compilationUnit.getAbsoluteFilename());
                    sourceCompilationUnits.addAll(compilationUnit.getSyntaxTreeRequest().get().getIncludedFiles());
                    break;
                case SWC_UNIT:
                    libraryCompilationUnits.add(compilationUnit.getAbsoluteFilename());
                    break;
                case RESOURCE_UNIT:
                    // ignore
                    break;
            }
        }
    }
}
