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
package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.royale.compiler.codegen.IEmitterTokens;

public enum JSGoogEmitterTokens implements IEmitterTokens
{
    AS3("__AS3__"),
    GOOG_ARRAY_FOREACH("goog.array.forEach"),
    GOOG_BASE("base"),
    GOOG_CALL("call"),
    GOOG_BIND("goog.bind"),
    GOOG_CONSTRUCTOR("constructor"),
    GOOG_GOOG("goog"),
    GOOG_INHERITS("goog.inherits"),
    GOOG_PROVIDE("goog.provide"),
    GOOG_REQUIRE("goog.require"),
    ROYALE_DEPENDENCY_LIST("/* Royale Dependency List: "),
    ROYALE_STATIC_DEPENDENCY_LIST("/* Royale Static Dependency List: "),
    OBJECT("Object"),
    ARRAY("Array"),
    ERROR("Error"),
    SELF("self"),
    SUPERCLASS("superClass_");

    private String token;

    private JSGoogEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
