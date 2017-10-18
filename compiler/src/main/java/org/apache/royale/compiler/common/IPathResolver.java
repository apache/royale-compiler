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

package org.apache.royale.compiler.common;

import java.io.File;

/**
 * Interface for resolving path names to files.
 */
public interface IPathResolver
{
    /**
     * Resolve a path to a file. The path may be absolute or my need to be
     * resolved either relative to the resolver's root directory. The instance
     * of the resolver determines what the root directory is. The path in the
     * returned file has been normalized by calling
     * FilenameNormalization.normalize() or some equivalent.
     * 
     * @param path the path to resolve.
     * @return a {@link File} of the resolved, and normalized path. The file
     * is not guaranteed to exist.
     */
    File resolve(String path);
}
