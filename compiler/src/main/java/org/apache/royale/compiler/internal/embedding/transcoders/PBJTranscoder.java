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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorNodeBase;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.LanguageIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;

/**
 * Handle the embedding of PBJ files
 */
public class PBJTranscoder extends DataTranscoder
{
    /**
     * Constructor.
     * 
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public PBJTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
    }

    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        // set the symbol name to be the ByteArray embed asset, not the wrapper qname
        String symbolName = data.getQName() + byteArrayNamePostfix;
        return doTranscode(symbolName, tags, problems);
    }

    @Override
    public FileNode buildAST(Collection<ICompilerProblem> problems, String filename)
    {
        FileNode fileNode = new FileNode(workspace, filename);
        PackageNode packageNode = new PackageNode(new IdentifierNode(""), null);
        fileNode.addItem(packageNode);

        ScopedBlockNode packageContents = packageNode.getScopedNode();
        ImportNode importNode = ImportNode.buildImportNode(getBaseClassQName());
        packageContents.addItem(importNode);
        importNode = ImportNode.buildImportNode("mx.core.IFlexAsset");
        packageContents.addItem(importNode);
        importNode = ImportNode.buildImportNode("flash.utils.ByteArray");
        packageContents.addItem(importNode);
        importNode = ImportNode.buildImportNode("flash.display.Shader");
        packageContents.addItem(importNode);

        // generate the byte array class name
        String byteArrayClassName = data.getQName() + byteArrayNamePostfix;
        ClassNode classNodeByteArray = new ClassNode(new IdentifierNode(byteArrayClassName));
        classNodeByteArray.setBaseClass(new IdentifierNode(getBaseClassName()));
        classNodeByteArray.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        packageContents.addItem(classNodeByteArray);

        // generate the pbj class name
        String pbjClassName = data.getQName();
        ClassNode classNodePbj = new ClassNode(new IdentifierNode(pbjClassName));
        classNodePbj.setBaseClass(new IdentifierNode("Shader"));
        classNodePbj.addInterface(new IdentifierNode("IFlexAsset"));
        classNodePbj.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        packageContents.addItem(classNodePbj);

        // build the constructor
        IdentifierNode constructorNameNode = new IdentifierNode(pbjClassName);
        constructorNameNode.setReferenceValue(classNodePbj.getDefinition());
        FunctionNode constructorNode = new FunctionNode(null, constructorNameNode);
        constructorNode.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        ScopedBlockNode constructorContents = constructorNode.getScopedNode();

        // generate: super();
        FunctionCallNode superCall = new FunctionCallNode(LanguageIdentifierNode.buildSuper());
        constructorContents.addItem(superCall);

        // generate: byteCode = new EmbedTest_sphereClassByteArray();
        ASToken newToken = new ASToken(ASTokenTypes.TOKEN_KEYWORD_NEW, -1, -1, -1, -1, IASKeywordConstants.NEW);
        FunctionCallNode newCall = new FunctionCallNode(newToken, new IdentifierNode(byteArrayClassName));
        ASToken assignToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT, -1, -1, -1, -1, "=");
        BinaryOperatorNodeBase assignment = BinaryOperatorNodeBase.create(assignToken, new IdentifierNode("byteCode"), newCall);
        constructorContents.addItem(assignment);

        classNodePbj.getScopedNode().addItem(constructorNode);

        fileNode.runPostProcess(EnumSet.of(PostProcessStep.POPULATE_SCOPE));

        return fileNode;
    }
}
