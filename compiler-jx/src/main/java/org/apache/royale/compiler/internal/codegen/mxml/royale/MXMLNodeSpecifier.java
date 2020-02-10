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

package org.apache.royale.compiler.internal.codegen.mxml.royale;

import org.apache.royale.compiler.codegen.IEmitterTokens;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;

/**
 * @author Erik de Bruin
 */
public class MXMLNodeSpecifier
{

    //--------------------------------------------------------------------------
    //
    //    Constructor
    //
    //--------------------------------------------------------------------------

    public MXMLNodeSpecifier()
    {
        sb = new StringBuilder();
    }

    //--------------------------------------------------------------------------
    //
    //    Variables
    //
    //--------------------------------------------------------------------------

    //---------------------------------
    //    sb
    //---------------------------------

    protected StringBuilder sb;
    
    //--------------------------------------------------------------------------
    //
    //    Properties
    //
    //--------------------------------------------------------------------------

    //---------------------------------
    //    name
    //---------------------------------

    public String name;

    //---------------------------------
    //    value
    //---------------------------------

    public String value;

    //---------------------------------
    //    valueNeedsQuotes
    //---------------------------------

    public boolean valueNeedsQuotes;

    //--------------------------------------------------------------------------
    //
    //    Methods
    //
    //--------------------------------------------------------------------------

    //---------------------------------
    //    output
    //---------------------------------

    public String output(boolean writeNewline)
    {
        return "";
    }

    //---------------------------------
    //    write
    //---------------------------------

    protected void write(IEmitterTokens value)
    {
        write(value.getToken());
    }

    protected void write(String value)
    {
        sb.append(value);
    }

    //---------------------------------
    //    writeDelimiter
    //---------------------------------

    protected void writeDelimiter(boolean writeNewline)
    {
        if (writeNewline)
            writeNewline(ASEmitterTokens.COMMA);
        else
            writeToken(ASEmitterTokens.COMMA);
    }

    //---------------------------------
    //    writeNewline
    //---------------------------------

    protected void writeNewline(IEmitterTokens value)
    {
        writeNewline(value.getToken());
    }

    protected void writeNewline(String value)
    {
        write(value);
        writeNewline();
    }

    protected void writeNewline()
    {
        write(ASEmitterTokens.NEW_LINE);
    }

    //---------------------------------
    //    writeSimpleDescriptor
    //---------------------------------

    protected void writeSimpleDescriptor(String name, String type, String value,
            boolean writeNewline)
    {
        write(ASEmitterTokens.SINGLE_QUOTE);
        write(name);
        write(ASEmitterTokens.SINGLE_QUOTE);
        writeDelimiter(writeNewline);

        if (type != null)
        {
            write(type);
            writeDelimiter(writeNewline);
        }

        write(value);
    }

    //---------------------------------
    //    writeToken
    //---------------------------------

    protected void writeToken(IEmitterTokens value)
    {
        writeToken(value.getToken());
    }

    protected void writeToken(String value)
    {
        write(value);
        write(ASEmitterTokens.SPACE);
    }

}
