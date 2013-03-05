package org.apache.flex.compiler.internal.codegen.js;

import org.apache.flex.compiler.codegen.IEmitterTokens;

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
