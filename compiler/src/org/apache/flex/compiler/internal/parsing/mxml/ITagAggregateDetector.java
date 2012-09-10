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

package org.apache.flex.compiler.internal.parsing.mxml;

import antlr.Token;

/**
 * Interface used by the tokenizer to detect tokens that begin and end a tag
 * (such as {@code <Private>} in MXML 2009) whose contents should be aggregated
 * by the tokenizer into a single <code>TOKEN_MXML_BLOB</code> token.
 */
public interface ITagAggregateDetector
{
    /**
     * Called on an <code>TOKEN_OPEN_TAG_START</code> token
     * to determine whether an aggregated tag is starting.
     */
	boolean shouldStartAggregate(Token token);
	
	/**
	 * Called on an <code>TOKEN_CLOSE_TAG_START</code> token
	 * to determine whether an aggregated tag is ending.
	 */
	boolean shouldEndAggregate(Token token);
}
