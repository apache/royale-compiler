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

package org.apache.royale.compiler.internal.tree.as.parts;

import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.KeywordNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.tree.as.IContainerNode.ContainerType;

/**
 * Base function contents part
 */
public class FunctionContentsPart implements IFunctionContentsPart
{
    /**
     * The "function" keyword
     */
    protected KeywordNode functionKeywordNode;

    /**
     * The arguments of this function (ContainerNode full of ArgumentNodes)
     */
    protected ContainerNode argumentsNode;

    /**
     * The contents of this function
     */
    protected ScopedBlockNode contentsNode;

    public FunctionContentsPart()
    {
        super();
        argumentsNode = new ContainerNode(2);
        argumentsNode.setContainerType(ContainerType.PARENTHESIS);
        contentsNode = new ScopedBlockNode(false);
        // Set the contents to implicit - the parser will set to BRACES if it
        // sees a function body
        contentsNode.setContainerType(ContainerType.SYNTHESIZED);
    }

    @Override
    public void setArgumentsNode(ContainerNode args)
    {
        argumentsNode = args;
    }

    @Override
    public void setContentsNode(ScopedBlockNode contents)
    {
        contentsNode = contents;
    }

    @Override
    public void setKeywordNode(KeywordNode node)
    {
        functionKeywordNode = node;
    }

    @Override
    public ContainerNode getParametersNode()
    {
        return argumentsNode;
    }

    @Override
    public ScopedBlockNode getContents()
    {
        return contentsNode;
    }

    @Override
    public KeywordNode getFunctionKeywordNode()
    {
        return functionKeywordNode;
    }

    @Override
    public void optimize()
    {
        //do nothing
    }
}
