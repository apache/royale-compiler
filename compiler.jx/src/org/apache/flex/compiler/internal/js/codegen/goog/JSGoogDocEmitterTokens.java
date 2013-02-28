package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.common.codegen.IEmitterTokens;

public enum JSGoogDocEmitterTokens implements IEmitterTokens
{
    PARAM("param"), STAR("*"), TYPE("type");

    private String token;

    private JSGoogDocEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
