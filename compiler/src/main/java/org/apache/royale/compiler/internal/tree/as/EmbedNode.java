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

package org.apache.royale.compiler.internal.tree.as;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.embedding.EmbedMIMEType;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnitFactory;
import org.apache.royale.compiler.problems.EmbedUnableToReadSourceProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IEmbedNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.utils.FilenameNormalization;

/**
 * AS parse tree node representing Embed meta data.
 */
public class EmbedNode extends ExpressionNodeBase implements IEmbedNode, IEmbedResolver
{
    /**
     * Constructor.
     * 
     * @param containingFilePath The path to the file containing the Embed metadata.
     * @param metaData The node representing the Embed metadata.
     * @param fileNodeAccumulator An object that keeps track of all Embed metadata.
     */
    public EmbedNode(String containingFilePath, IMetaTagNode metaData, IFileNodeAccumulator fileNodeAccumulator)
    {
        this.containingSourceFilename = FilenameNormalization.normalize(containingFilePath);
        this.metaData = metaData;
        assert metaData != null && metaData.getAllAttributes() != null; //can be empty array

        if (fileNodeAccumulator != null)
            fileNodeAccumulator.addEmbedNode(this);
    }
    
    private final String containingSourceFilename;

    private final IMetaTagNode metaData;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.EmbedID;
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // TODO: Should this be null, or type Class, or the
        // base type of the embed class, or the embed class?
        return null;
    }
    
    @Override
    protected EmbedNode copy()
    {
        // This should never be used as a constant initializer,
        return null;
    }
    
    //
    // IEmbedNode implementations
    //

    @Override
    public IMetaTagAttribute[] getAttributes()
    {
        return metaData.getAllAttributes();
    }

    //
    // IEmbedResolver implementations
    //

    @Override
    public EmbedCompilationUnit resolveCompilationUnit(ICompilerProject project,
                                                       Collection<ICompilerProblem> problems)
        throws InterruptedException
    {
        assert (project instanceof CompilerProject);
        
        return EmbedCompilationUnitFactory.getCompilationUnit(
            (CompilerProject)project, containingSourceFilename,
            metaData, metaData.getAllAttributes(), problems);
    }

    @Override
    public EmbedCompilationUnit resolveCompilationUnit(ICompilerProject project)
        throws InterruptedException
    {
        Collection<ICompilerProblem> ignoredProblems = new ArrayList<ICompilerProblem>();
        return resolveCompilationUnit(project, ignoredProblems);
    }

    //
    // Other methods
    //

    public String getName(ICompilerProject project, Collection<ICompilerProblem> problems)
        throws InterruptedException
    {
    	// if the embed is text/plain, return the actual text from the file.
    	// this assumes the variable being initialized is of type String.
        EmbedData data = new EmbedData(containingSourceFilename, null);
        boolean hadError = false;
        for (IMetaTagAttribute attribute : getAttributes())
        {
            String key = attribute.getKey();
            String value = attribute.getValue();
            if (data.addAttribute((CompilerProject) project, this, key, value, problems))
            {
                hadError = true;
            }
        }
        if (hadError)
        	return new String();
    	String source = (String) data.getAttribute(EmbedAttribute.SOURCE);
    	EmbedMIMEType mimeType = (EmbedMIMEType) data.getAttribute(EmbedAttribute.MIME_TYPE);
        if (mimeType != null && mimeType.toString().equals(EmbedMIMEType.TEXT.toString()) && source != null)
        {
            File file = new File(FilenameNormalization.normalize(source));
    		try {
				String string = FileUtils.readFileToString(file);
				return string;
			} catch (IOException e) {
	            problems.add(new EmbedUnableToReadSourceProblem(e, file.getPath()));
			}

        }
        EmbedCompilationUnit cu = resolveCompilationUnit(project, problems);
        // If there was an error resolving the compilation unit, just return an empty string.
        if (cu == null)
            return new String();

        return cu.getName();
    }
}
