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
package org.apache.royale.compiler.internal.test;

import java.util.List;

import org.apache.royale.compiler.codegen.js.IMappingEmitter;
import org.apache.royale.compiler.tree.as.IASNode;

import com.google.debugging.sourcemap.FilePosition;
import static org.junit.Assert.assertTrue;

public class RoyaleSourceMapTestBase extends RoyaleTestBase
{
    protected void assertMapping(IASNode node, int nodeStartLine, int nodeStartColumn,
                                 int outStartLine, int outStartColumn, int outEndLine, int outEndColumn)
    {
        int sourceStartLine = nodeStartLine + node.getLine();
        int sourceStartColumn = nodeStartColumn;
        if (nodeStartLine == 0)
        {
            sourceStartColumn += node.getColumn();
        }
        boolean foundMapping = false;
        IMappingEmitter emitter = (IMappingEmitter) mxmlEmitter;
        List<IMappingEmitter.SourceMapMapping> mappings = emitter.getSourceMapMappings();
        for (IMappingEmitter.SourceMapMapping mapping : mappings)
        {
            FilePosition sourcePosition = mapping.sourceStartPosition;
            FilePosition startPosition = mapping.destStartPosition;
            FilePosition endPosition = mapping.destEndPosition;
            if (sourcePosition.getLine() == sourceStartLine
                    && sourcePosition.getColumn() == sourceStartColumn
                    && startPosition.getLine() == outStartLine
                    && startPosition.getColumn() == outStartColumn
                    && endPosition.getLine() == outEndLine
                    && endPosition.getColumn() == outEndColumn)
            {
                foundMapping = true;
                break;
            }
        }
        assertTrue("Mapping not found for node " + node.getNodeID() + ". Expected "
                        + "source: (" + nodeStartLine + ", " + nodeStartColumn + "), dest: (" + outStartLine + ", " + outStartColumn + ") to (" + outEndLine + ", " + outEndColumn + ")",
                foundMapping);
    }
}
