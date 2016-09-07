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

package org.apache.flex.compiler.internal.embedding.transcoders;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.internal.embedding.EmbedData;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.swf.tags.DefineBinaryDataTag;
import org.apache.flex.swf.tags.ICharacterTag;
import org.apache.flex.swf.tags.ITag;

/**
 * Handle the embedding of data using a ByteArray
 */
public class DataTranscoder extends TranscoderBase
{
    /**
     * Constructor.
     * 
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public DataTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
    }

    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);
        baseClassQName = CORE_PACKAGE + ".ByteArrayAsset";
        return result;
    }

    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        return doTranscode(data.getQName(), tags, problems);
    }

    protected Map<String, ICharacterTag> doTranscode(String qName, Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        DefineBinaryDataTag assetTag = buildBinaryDataTag(problems);
        if (assetTag == null)
            return null;

        return Collections.singletonMap(qName, (ICharacterTag)assetTag);
    }
}
