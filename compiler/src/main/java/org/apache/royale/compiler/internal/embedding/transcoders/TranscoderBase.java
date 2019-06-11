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

package org.apache.royale.compiler.internal.embedding.transcoders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.embedding.transcoders.ITranscoder;
import org.apache.royale.compiler.filespecs.IBinaryFileSpecification;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.embedding.EmbedMIMEType;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedUnableToReadSourceProblem;
import org.apache.royale.compiler.problems.EmbedUnsupportedAttributeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swf.tags.DefineBinaryDataTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;

/**
 * Base class for all embed transcoders
 */
public abstract class TranscoderBase implements ITranscoder
{
    public static final String CORE_PACKAGE = "mx.core";
    protected static final String UTILS_PACKAGE = "mx.utils";
    protected static final String byteArrayNamePostfix = "ByteArray";

    /**
     * @param data
     * @param workspace
     */
    protected TranscoderBase(EmbedData data, Workspace workspace)
    {
        this.data = data;
        this.workspace = workspace;
        this.swcSource = data.getSWCSource();
    }

    protected final EmbedData data;
    protected final Workspace workspace;
    protected final ISWCFileEntry swcSource;
    protected String baseClassQName;
    protected String source;
    private EmbedMIMEType mimeType;
    public String hashCodeSourceName;

    /**
     * Transcode the embedded asset
     * @param tags The collection of transcoded asset tags
     * @param problems The collection of compiler problems to which this method will add problems.
     * @return map of symbol name to character asset tags.  null if error
     */
    protected abstract Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems);

    /**
     * The relevant SWF tags
     * @param tags The transcoded asset tags
     * @param problems The collection of compiler problems to which this method will add problems.
     * @return map of symbol name to character asset tags.  null if error.  the
     * returned map may not be modified.
     */
    public Map<String, ICharacterTag> getTags(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        return doTranscode(tags, problems);
    }

    public String getBaseClassQName()
    {
        return baseClassQName;
    }

    /**
     * @return The name of the base class of the generated class
     */
    public String getBaseClassName()
    {
        return baseClassQName.substring(baseClassQName.lastIndexOf(".") + 1);
    }

    /**
     * Analyze the attributes
     * @param location Source location from where the embed came from
     * @param problems Any problems discovered in the EmbedNode
     * @return false if analyze failed
     */
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = true;
        for (EmbedAttribute attribute : data.getAttributes())
        {
            if (!setAttribute(attribute))
            {
                problems.add(new EmbedUnsupportedAttributeProblem(location, attribute, (EmbedMIMEType)data.getAttribute(EmbedAttribute.MIME_TYPE)));
                result = false;
            }
        }

        if (!checkAttributeValues(location, problems))
            result = false;

        return result;
    }

    /**
     * Stores the supported attribute of the transcoder
     * @param attribute
     * @return false if a non-supported attribute
     */
    protected boolean setAttribute(EmbedAttribute attribute)
    {
        boolean isSupported = true;
        switch (attribute)
        {
            case SOURCE:
                source = (String)data.getAttribute(EmbedAttribute.SOURCE);
                break;
            case MIME_TYPE:
                mimeType = (EmbedMIMEType)data.getAttribute(EmbedAttribute.MIME_TYPE);
                break;
            default:
                isSupported = false;
        }

        return isSupported;
    }

    /**
     * Verify the attributes are valid for the specific transcoder
     * @param location
     * @param problems
     * @return false if contains invalid attributes
     */
    protected boolean checkAttributeValues(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        return true;
    }

    protected DefineBinaryDataTag buildBinaryDataTag(Collection<ICompilerProblem> problems)
    {
        byte[] bytes = getDataBytes(problems);
        if (bytes == null)
            return null;

        return new DefineBinaryDataTag(bytes);
    }

    /**
     * Get the input stream of the embedded asset
     * 
     * @param problems The collection of compiler problems to which this method will add problems.
     * @return resultant stream.  null on error
     */
    protected InputStream getDataStream(Collection<ICompilerProblem> problems)
    {
        InputStream inStrm = null;
        if (swcSource != null)
        {
            inStrm = getDataStream(swcSource, problems);
        }
        else
        {
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
        		System.out.println("TranscodeBase waiting for lock in getLatestBinaryFileSpecification");
            IBinaryFileSpecification fileSpec = workspace.getLatestBinaryFileSpecification(source);
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
        		System.out.println("TranscodeBase done with lock in getLatestBinaryFileSpecification");
            inStrm = getDataStream(fileSpec, problems);
        }

        return inStrm;
    }

    private InputStream getDataStream(ISWCFileEntry swcSource, Collection<ICompilerProblem> problems)
    {
        InputStream inStrm = null;
        try
        {
            inStrm = swcSource.createInputStream();
        }
        catch (IOException e)
        {
            problems.add(new EmbedUnableToReadSourceProblem(e, swcSource.getPath()));
        }

        return inStrm;
    }

    private InputStream getDataStream(IBinaryFileSpecification fileSpec, Collection<ICompilerProblem> problems)
    {
        InputStream inStrm = null;
        try
        {
            inStrm = fileSpec.createInputStream();
        }
        catch (IOException e)
        {
            problems.add(new EmbedUnableToReadSourceProblem(e, fileSpec.getPath()));
        }

        return inStrm;
    }

    /**
     * Read the original bytes of the embedded asset
     * 
     * @param problems
     * @return resultant bytes
     */
    protected byte[] getDataBytes(Collection<ICompilerProblem> problems)
    {
        InputStream inStrm = getDataStream(problems);
        if (inStrm == null)
            return null;

        byte[] bytes = null;
        try
        {
            bytes = IOUtils.toByteArray(inStrm);
        }
        catch (IOException e)
        {
            problems.add(new EmbedUnableToReadSourceProblem(e, source));
        }
        finally
        {
            if (inStrm != null)
            {
                try
                {
                    inStrm.close();
                }
                catch (IOException e)
                {
                    // don't care.  error case anyway...
                }
            }
        }

        return bytes;
    }
    
    /**
     * Build an AST to represent the embedded asset class
     * 
     * @param problems The collection of compiler problems to which this method will add problems.
     * @param filename The path to the file being embedded.
     * @return generated class AST
     */
    public FileNode buildAST(Collection<ICompilerProblem> problems, String filename)
    {
        FileNode fileNode = new FileNode(workspace, filename);
        PackageNode packageNode = new PackageNode(new IdentifierNode(""), null);
        fileNode.addItem(packageNode);

        ScopedBlockNode contents = packageNode.getScopedNode();
        ImportNode importNode = ImportNode.buildImportNode(getBaseClassQName());
        contents.addItem(importNode);

        ClassNode classNode = new ClassNode(new IdentifierNode(data.getQName()));
        classNode.setBaseClass(new IdentifierNode(getBaseClassName()));
        classNode.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        contents.addItem(classNode);

        fileNode.runPostProcess(EnumSet.of(PostProcessStep.POPULATE_SCOPE));

        return fileNode;
    }

    /**
     * Build ABC to represent the embedded asset class
     * 
     * @param project The compiler project.
     * @param problems The collecton of compiler problems to which this method will add problems.
     * @return generated class ABC
     */
    public byte[] buildABC(ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        assert false : "unimplemented buildABC called";
        return null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof TranscoderBase))
            return false;

        TranscoderBase t = (TranscoderBase)o;
        if (!source.equals(t.source) ||
            !baseClassQName.equals(t.baseClassQName) ||
            mimeType != t.mimeType)
        {
            return false;
        }

        if ((swcSource == null) && (t.swcSource != null) ||
            (swcSource != null) && (t.swcSource == null))
        {
            return false;
        }

        if (swcSource != null)
        {
            if (!swcSource.getContainingSWCPath().equals(t.swcSource.getContainingSWCPath()) ||
                !swcSource.getPath().equals(t.swcSource.getPath()))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 0;
        hashCode = hashCodeSourceName.hashCode();
        hashCode ^= baseClassQName.hashCode();
        hashCode ^= mimeType.toString().hashCode(); // the toString() gives us a reproducible hash code

        // if there is a swcSource, just use the two paths, and
        // ignore mod time, as we don't take that into account
        // for regular files
        if (swcSource != null)
        {
            hashCode ^= swcSource.getContainingSWCPath().hashCode();
            hashCode ^= swcSource.getPath().hashCode();
        }

        return hashCode;
    }
}
