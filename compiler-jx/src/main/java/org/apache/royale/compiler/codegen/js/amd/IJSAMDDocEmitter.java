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

package org.apache.royale.compiler.codegen.js.amd;

import org.apache.royale.compiler.codegen.js.IJSDocEmitter;

/**
 * The {@link IJSAMDDocEmitter} interface allows the abstraction of JavaScript
 * document comments to be emitted per tag.
 * <p>
 * The purpose of the API is to clamp emitted output to JavaScript doc tags. The
 * output can be multiline but is specific to one tag. This allows a full
 * comment to be created without worrying about how to assemble the tags.
 * <p>
 * TODO (mschmalle) Might make a comment API and tag API so comments are not
 * dependent on tag creation IE IJSDocEmitter and IJSDocTagEmitter
 * 
 * @author Michael Schmalle
 */
public interface IJSAMDDocEmitter extends IJSDocEmitter
{

}
