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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.io.input.BOMInputStream;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.ModifierNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedSourceAttributeCouldNotBeReadProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;

/**
 * Handle the embedding of XML files
 */
public class XMLTranscoder extends TranscoderBase
{
    /**
     * Constructor.
     * 
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public XMLTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
        this.encoding = "";
    }

    private String encoding;

    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);
        baseClassQName = "";
        return result;
    }

    @Override
    protected boolean setAttribute(EmbedAttribute attribute)
    {
        boolean isSupported = true;
        switch (attribute)
        {
            case ENCODING:
                encoding = (String)data.getAttribute(EmbedAttribute.ENCODING);
                break;
            default:
                isSupported = super.setAttribute(attribute);
        }

        return isSupported;
    }

    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        // no tags related to XML, as all it's all inlined in the generated class
        return Collections.emptyMap();
    }

    @Override
    public FileNode buildAST(Collection<ICompilerProblem> problems, String filename)
    {
        FileNode fileNode = new FileNode(workspace, filename);
        PackageNode packageNode = new PackageNode(new IdentifierNode(""), null);
        fileNode.addItem(packageNode);

        ScopedBlockNode contents = packageNode.getScopedNode();

        ClassNode classNode = new ClassNode(new IdentifierNode(data.getQName()));
        classNode.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        contents.addItem(classNode);

        // generate: public static var data:XML = ${XML_DATA};
        VariableNode variableNodeData = new VariableNode(new IdentifierNode("data"));
        variableNodeData.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        variableNodeData.addModifier(new ModifierNode(IASKeywordConstants.STATIC));
        variableNodeData.setType(null, new IdentifierNode("XML"));
        ASToken assignToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT, -1, -1, -1, -1, "=");
        String xmlContents = getXMLString(problems);
        LiteralNode xmlData = new LiteralNode(LiteralType.STRING, xmlContents);
        variableNodeData.setAssignedValue(assignToken, xmlData);
        classNode.getScopedNode().addItem(variableNodeData);

        fileNode.runPostProcess(EnumSet.of(PostProcessStep.POPULATE_SCOPE));

        return fileNode;
    }

    private String getXMLString(Collection<ICompilerProblem> problems)
    {
        InputStream strm = getDataStream(problems);
        if (strm == null)
            return "";

        Reader reader = null;
        BOMInputStream bomStream = null;
        StringBuilder str = new StringBuilder();
        try
        {
            bomStream = new BOMInputStream(strm);
            String bomCharsetName = bomStream.getBOMCharsetName();
            if (bomCharsetName == null)
            {
                if (encoding == null || encoding.length() == 0)
                {
                    bomCharsetName = System.getProperty("file.encoding");
                }
                else
                {
                    bomCharsetName = encoding;
                }
            }

            reader = new InputStreamReader(bomStream, bomCharsetName);
            char[] line = new char[2048];
            int count = 0;
            while ((count = reader.read(line, 0, line.length)) >= 0)
            {
                str.append(line, 0, count);
            }
        }
        catch (IOException e)
        {
            problems.add(new EmbedSourceAttributeCouldNotBeReadProblem(source));
        }
        finally
        {
            if (bomStream != null)
            {
                try
                {
                    bomStream.close();
                }
                catch (IOException e)
                {
                }
            }

            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                }                
            }
        }

        return str.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
            return false;

        if (!(o instanceof XMLTranscoder))
            return false;

        XMLTranscoder t = (XMLTranscoder)o;
        if (!encoding.equals(t.encoding))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode += encoding.hashCode();

        return hashCode;
    }
}
