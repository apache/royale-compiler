package org.apache.flex.compiler.internal.codegen.js.node;

import org.apache.flex.compiler.codegen.IEmitterTokens;

public enum NodeEmitterTokens implements IEmitterTokens
{
    REQUIRE("require"),
    EXPORTS("exports");

    private String token;

    private NodeEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
