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

package org.apache.royale.compiler.internal.units;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;

/**
 * Factory class to create EmbedCompilationUnits from EmbedData.  This factory
 * will handle reusing EmbedCompilationUnits when the attributes are equivalent.
 */
public class EmbedCompilationUnitFactory
{
    /**
     * Return an EmbedCompilationUnit based on information from the EmbedData.
     * The EmbedCompilationUnit will be reused if the EmbedData's are equivalent.
     * 
     * @param project Project to resolve against
     * @param containingSourceFilename Filename from which the embed meta data comes from
     * @param location Location from which the embed meta data comes from
     * @param attributes Array of IMetaTagAttribute
     * @param problems Any problems with the attributes
     * @return an EmbedCompilationUnit
     */
    public static EmbedCompilationUnit getCompilationUnit(CompilerProject project, String containingSourceFilename, ISourceLocation location, IMetaTagAttribute[] attributes, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        Workspace workspace = project.getWorkspace();
        workspace.embedLock.writeLock().lock();
        try
        {
            // there was a problem parsing the embed meta data, so just bomb out
            // rather than creating a compilation unit
            EmbedData data = getEmbedData(project, null, containingSourceFilename, location, attributes, problems);
            if (data == null)
                return null;

            // Check to see if there is an existing compilation unit in this project
            // which can be re-used.
            EmbedCompilationUnit embedCompilationUnit = project.getCompilationUnit(data);
            if (embedCompilationUnit != null)
            {
                return embedCompilationUnit;
            }

            // ensure that the EmbedData that's been passed in has already been
            // canonicalized, if not there's an EmbedData reuse bug somewhere.
            assert (data == project.getWorkspace().getCanonicalEmbedData(data)) : "EmbedData has not been canonicalized";

            embedCompilationUnit = new EmbedCompilationUnit(project, data);
            project.addEmbedCompilationUnit(embedCompilationUnit);
            return embedCompilationUnit;
        }
        finally
        {
            workspace.embedLock.writeLock().unlock();
        }
    }

    /**
     * Return an EmbedData based on information from the meta data attributes.
     * The EmbedData will be reused if the attributes are equivalent.
     * 
     * @param project Project to resolve against
     * @param specifiedQName a user defined qname for the generated class.  may be null.
     * @param containingSourceFilename Filename from which the embed meta data comes from
     * @param location Location from which the embed meta data comes from
     * @param attributes Array of IMetaTagAttribute
     * @param problems Any problems with the attributes
     * @return an EmbedData
     */
    public static EmbedData getEmbedData(CompilerProject project, String specifiedQName, String containingSourceFilename, ISourceLocation location, IMetaTagAttribute[] attributes, Collection<ICompilerProblem> problems)
    {
        EmbedData data = new EmbedData(containingSourceFilename, specifiedQName);

        boolean hadError = false;
        for (IMetaTagAttribute attribute : attributes)
        {
            String key = attribute.getKey();
            String value = attribute.getValue();
            if (data.addAttribute(project, location, key, value, problems))
            {
                hadError = true;
            }
        }

        if (hadError || !data.createTranscoder(project, location, problems))
            return null;

        // get the canonical EmbedData, as there may already be an equivalent
        // embed data sitting in the workspace
        return project.getWorkspace().getCanonicalEmbedData(data);
    }

    /**
     * Collect any embed datas in the file
     * 
     * @param project Project to resolve against
     * @param fileNode FileNode to find embed nodes in
     * @param embeds Set of EmbedDatas to populate
     * @param problems Any problems with the attributes
     */
    public static void collectEmbedDatas(ICompilerProject project, IFileNodeAccumulator fileNode, Set<EmbedData> embeds, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        List<IEmbedResolver> embedNodes = fileNode.getEmbedNodes();
        for (IEmbedResolver embedNode : embedNodes)
        {
            EmbedCompilationUnit cu = (EmbedCompilationUnit)embedNode.resolveCompilationUnit(project);
            if (cu != null)
            {
                embeds.add(cu.getEmbedData());
            }
        }
    }
}
