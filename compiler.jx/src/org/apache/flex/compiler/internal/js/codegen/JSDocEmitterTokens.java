package org.apache.flex.compiler.internal.js.codegen;

import org.apache.flex.compiler.common.codegen.IEmitterTokens;

public enum JSDocEmitterTokens implements IEmitterTokens
{
    JSDOC_CLOSE("*/"), JSDOC_OPEN("/**");

    private String token;

    private JSDocEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
