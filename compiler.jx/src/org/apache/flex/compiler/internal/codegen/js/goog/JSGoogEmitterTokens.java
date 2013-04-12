package org.apache.flex.compiler.internal.codegen.js.goog;

import org.apache.flex.compiler.codegen.IEmitterTokens;

public enum JSGoogEmitterTokens implements IEmitterTokens
{
    AS3("__AS3__"),
    GOOG_ARRAY_FOREACH("goog.array.forEach"),
    GOOG_BASE("goog.base"),
    GOOG_BIND("goog.bind"),
    GOOG_INHERITS("goog.inherits"),
    GOOG_PROVIDE("goog.provide"),
    GOOG_REQUIRE("goog.require"),
    OBJECT("Object"),
    ARRAY("Array"),
    ERROR("Error"),
    SELF("self");

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
