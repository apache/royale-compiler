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

package org.apache.royale.compiler.internal.css.codegen;

import org.apache.royale.abc.instructionlist.InstructionList;

/**
 * Defines all the runtime constants in the CSS framework. The constants are of
 * type {@code Integer} so that they can be directly used in
 * {@link InstructionList#addInstruction(int, Object)}.
 */
public interface ICSSRuntimeConstants
{
    // From CSSClass
    static final Integer SELECTOR = 0;
    static final Integer CONDITION = 1;
    static final Integer STYLE_DECLARATION = 2;
    static final Integer MEDIA_QUERY = 3;
    
    // From CSSFactory
    static final Integer DEFAULT_FACTORY = 0;
    static final Integer FACTORY = 1;

    // From CSSDataType
    static final Integer NATIVE = 0;
    static final Integer DEFINITION = 1;
}
