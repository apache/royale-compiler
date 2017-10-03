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

package org.apache.royale.compiler.internal.tree.properties;

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.properties.IResourceBundleFileNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

public class ResourceBundleFileNode extends FileNode implements IResourceBundleFileNode
{
    private static final long serialVersionUID = -3996874882327215266L;

    /**
     * Constructor.
     */
    public ResourceBundleFileNode(IWorkspace workspace, String pathName, String locale)
    {
        super(workspace, pathName);
        this.locale = locale;

        setSourcePath(pathName);
        setStart(0);
        // TODO How do we get the size of a resource file?
        // We can't ask the file system because the file could be being edited.
        setEnd(Integer.MAX_VALUE);
        setLine(0);
        setColumn(0);
    }

    private String locale;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.PropertiesFileID;
    }

    @Override
    public String getBundleName()
    {
        return FilenameUtils.getBaseName(getSourcePath());
    }

    @Override
    public String getLocale()
    {
        return locale;
    }
}
