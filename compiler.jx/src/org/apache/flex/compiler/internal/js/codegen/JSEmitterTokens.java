package org.apache.flex.compiler.internal.js.codegen;

import org.apache.flex.compiler.as.codegen.IEmitterTokens;

public enum JSEmitterTokens implements IEmitterTokens
{
    ARGUMENTS("arguments"), 
    CALL("call"),
    CONFIGURABLE("configurable"),
    CONSTRUCTOR("constructor"),
    DEFINE_PROPERTY("defineProperty"),
    INTERFACE("interface"),
    PROTOTYPE("prototype"),
    SLICE("slice");

    private String token;

    private JSEmitterTokens(String value)
    {
        token = value;
    }

    public String getToken()
    {
        return token;
    }
}
