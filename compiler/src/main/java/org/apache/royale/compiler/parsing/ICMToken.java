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

package org.apache.royale.compiler.parsing;

import org.apache.royale.compiler.tree.as.IASNode;

/**
 * Range that represents a generic token within code model
 */
public interface ICMToken
{
	/**
	 * Returns the type of this token
	 * @return a token id
	 */
	int getType();
	
	/**
	 * Returns the start offset of this token
	 * @return the start offset, or -1
	 */
	int getStart();
	
	/**
	 * Returns the end offset of this token
	 * @return the end offset, or -1
	 */
	int getEnd();
	
	/**
	 * Returns the line of this token..
	 * Line numbers start at 0, not 1.
	 * @return the line, or -1
	 */
	int getLine();
	
    /**
     * Returns the column of this token.
     * Column numbers staart at 0, not 1.
     * @return the column, or -1
     */
    int getColumn();
	
	/**
	 * Returns the text contained within this token
	 * @return the text, or null
	 */
	String getText();
	
	/**
	 * @return True if this token does not actually exist
	 */
	boolean isImplicit();
	
	/**
	 * Changes the type of the token, and returns a new copy with the desired type.  This is non-destructive
	 * @param type the type of token to create
	 * @return a new {@link ICMToken} that is a copy of the original, with a different type
	 */
	ICMToken changeType(int type);
	
    /**
     * Get the source file path of the node. When the token is built from an
     * included source, this method returns the file path of the included
     * source. The return value can be null if the source file is not an
     * included file, but not every non-included file has a null source path. 
     * <p>
     * The source path will be copied to {@link IASNode#getSourcePath()} during
     * tree construction.
     * 
     * @return source file path or null
     */
    String getSourcePath();
}
