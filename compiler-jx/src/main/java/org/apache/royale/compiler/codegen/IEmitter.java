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

package org.apache.royale.compiler.codegen;

import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public interface IEmitter
{

    /**
     * Pushes an indent into the emitter so after newlines are emitted, the
     * output is correctly formatted.
     */
    void indentPush();

    /**
     * Pops an indent from the emitter so after newlines are emitted, the output
     * is correctly formatted.
     */
    void indentPop();

    /**
     * Writes a string to the writer.
     * 
     * @param value The string to write to the output buffer.
     */
    void write(IEmitterTokens value);

    void write(String value);

    /**
     * Writes newline character(s)
     */
    void writeNewline();

    /**
     * Writes the <code>value</code> and then a newline which will automatically
     * have the indent applied after the \n character.
     * 
     * @param value The String value to write before the \n is appended.
     */
    void writeNewline(IEmitterTokens value);

    void writeNewline(String value);

    /**
     * Writes the <code>value</code> after a push or pop of the indent.
     * <p>
     * This method effectively lets you write a value and then indent our
     * outdent. The method can be useful in the following where your cursor
     * writer is at <code>[0]</code>, you write
     * <code>writeNewline("if (foo) {", true);</code> and the cursor after the
     * call will end up at <code>[1]</code>.
     * 
     * <pre>
     * [0]if (foo) {
     *     [1]this.something;
     * }
     * </pre>
     * 
     * @param value The String value to write before the \n is appended.
     * @param pushIndent Whether to push indent <code>true</code> or pop indent
     * <code>false</code>.
     */
    void writeNewline(IEmitterTokens value, boolean pushIndent);

    void writeNewline(String value, boolean pushIndent);

    /**
     * Writes a {@link ASEmitterTokens} character to the buffer and appends a
     * space after automatically.
     * 
     * @param value The {@link ASEmitterTokens} value.
     */
    void writeToken(IEmitterTokens value);

    void writeToken(String value);

    /**
     * Takes the node argument and created a String representation if it using
     * the buffer temporarily.
     * <p>
     * Note; This method is still beta, it need more logic if an emitter is
     * actually using the buffer!
     * 
     * @param node The node walk and create a String for.
     * @return The node's output.
     */
    String stringifyNode(IASNode node);
    
}
