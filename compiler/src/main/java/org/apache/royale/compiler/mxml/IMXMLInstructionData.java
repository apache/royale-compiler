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

package org.apache.royale.compiler.mxml;

/**
 * Represents an XML processing instruction.
 */
public interface IMXMLInstructionData extends IMXMLUnitData
{
    /**
     * Returns the raw processing instruction, starting with the {@code <?} and ending with the {@code ?>}.
     */
    String getInstructionText();
    
    /**
     * Returns the target of the processing instruction.
     * <p>
     * The target is the identifier that follows the {@code <?}.
     * For example, for {@code <?foo bar baz?>} the target is {@code "foo"}.
     */
    String getTarget();
    
    /**
     * Returns the content of the processing instruction.
     * <p>
     * The content is everything that follows the whitespace after the target, up to the {@code ?>}.
     * For example, for {@code <?foo bar baz?>} the content is {@code "bar baz"}.
     */
    String getContent();
}
