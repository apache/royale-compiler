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

package org.apache.royale.compiler.codegen.js;

import java.util.List;

import org.apache.royale.compiler.common.ISourceLocation;

import com.google.debugging.sourcemap.FilePosition;

/**
 * Tracks mappings to generate source maps while it emits code.
 */
public interface IMappingEmitter
{
    List<SourceMapMapping> getSourceMapMappings();

    /**
     * Adds a node to the source map.
     */
    void startMapping(ISourceLocation node);

    /**
     * Adds a node to the source map using custom line and column values,
     * instead of the node's own line and column. Useful for starting a mapping
     * in the middle of the node.
     */
    void startMapping(ISourceLocation node, int line, int column);

    /**
     * Adds a node to the source map after a particular node instead using the
     * node's own line and column.
     */
    void startMapping(ISourceLocation node, ISourceLocation afterNode);

    /**
     * Commits a mapping to the source map.
     */
    void endMapping(ISourceLocation node);

    class SourceMapMapping
    {
        public String sourcePath;
        public String name;
        public FilePosition sourceStartPosition;
        public FilePosition destStartPosition;
        public FilePosition destEndPosition;
    }
}
