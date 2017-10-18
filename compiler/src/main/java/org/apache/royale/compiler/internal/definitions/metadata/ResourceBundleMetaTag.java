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

package org.apache.royale.compiler.internal.definitions.metadata;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;

import java.util.Collection;

/**
 * Represents an ResourceBundle metadata tag, of the form
 * [ResourceBundle("bundlename")]. This is the definition version, that is
 * created by the ResourceBundleTagNode so that we don't need the AST.
 */
public class ResourceBundleMetaTag extends MetaTag
{
    private String bundleName;

    public ResourceBundleMetaTag(IDefinition decoratedDefinition, String tagName, IMetaTagAttribute[] attributes, String bundleName)
    {
        super(decoratedDefinition, tagName, attributes);
        this.bundleName = bundleName;
    }

    public void resolveDependencies(Collection<ICompilerProblem> errors, ICompilerProject project) throws InterruptedException
    {
        if (project instanceof RoyaleProject)
        {
            //compilation unit of the file that has the metadata
            final ICompilationUnit refCompUnit = ((RoyaleProject)project).getScope().getCompilationUnitForScope(getDecoratedDefinition().getContainingScope());
            assert refCompUnit != null;

            ResourceBundleUtils.resolveDependencies(bundleName, refCompUnit, project, this, errors);
        }
    }

}
