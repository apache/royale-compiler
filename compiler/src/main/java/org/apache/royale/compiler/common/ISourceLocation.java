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

/**
 * This interface provides information about where something 
 * (a token, a tag, an attribute, a node, a definition, etc.)
 * appears in source code.
 * <p>
 * Its primary purpose is for problem reporting.
 * The start and end offsets are used to highlight
 * a soruce range in the Flash Builder editor.
 * The line and column numbers are used by command-line
 * tools to report problems.
 */
public interface ISourceLocation
{
    /**
     * Indicates an unknown or implicit starting offset, ending offset,
     * line number, or column number.
     */
    static final int UNKNOWN = -1;
    
    /**
     * Gets the normalized source path.
     */
    String getSourcePath();
    
    /**
     * Gets the local starting offset. It is zero-based.
     */
    int getStart();

    /**
     * Gets the local ending offset. It is zero-based.
     */
    int getEnd();
    
    /**
     * Gets the local line number. It is zero-based.
     */
    int getLine();
    
    /**
     * Gets the local column number. It is zero-based.
     */
    int getColumn();
    
    /**
     * Gets the local line number at the end. It is zero-based.
     */
    int getEndLine();

    /**
     * Gets the local column number at the end. It is zero-based.
     */
    int getEndColumn();
    
    /**
     * Gets the absolute starting offset. It is zero-based.
     */
    int getAbsoluteStart();
    
    /**
     * Gets the absolute starting offset. It is zero-based.
     */
    int getAbsoluteEnd();
}
