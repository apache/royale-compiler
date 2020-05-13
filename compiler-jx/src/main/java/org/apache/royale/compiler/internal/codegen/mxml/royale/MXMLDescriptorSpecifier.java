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

import java.util.ArrayList;

import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;

/**
 * @author Erik de Bruin
 */
public class MXMLDescriptorSpecifier extends MXMLNodeSpecifier
{

    //--------------------------------------------------------------------------
    //
    //    Constructor
    //
    //--------------------------------------------------------------------------

    public MXMLDescriptorSpecifier()
    {
        super();

        eventSpecifiers = new ArrayList<MXMLEventSpecifier>();
        propertySpecifiers = new ArrayList<MXMLDescriptorSpecifier>();

        valueNeedsQuotes = false;
    }

    //--------------------------------------------------------------------------
    //
    //    Properties
    //
    //--------------------------------------------------------------------------

    //---------------------------------
    //    children
    //---------------------------------

    public MXMLDescriptorSpecifier childrenSpecifier;

    //---------------------------------
    //    properties
    //---------------------------------

    public ArrayList<MXMLDescriptorSpecifier> propertySpecifiers;

    //---------------------------------
    //    events
    //---------------------------------

    public ArrayList<MXMLEventSpecifier> eventSpecifiers;

    //---------------------------------
    //    hasArray
    //---------------------------------

    public boolean hasArray;

    //---------------------------------
    //    hasObject
    //---------------------------------

    public boolean hasObject;

    //---------------------------------
    //    id
    //---------------------------------

    public String id;

    //---------------------------------
    //    hasLocalId
    //---------------------------------

    public boolean hasLocalId;

    //---------------------------------
    //    effectiveId
    //---------------------------------

    public String effectiveId;

    //---------------------------------
    //    isTopNode
    //---------------------------------

    public boolean isTopNode;

    //---------------------------------
    //    isProperty
    //---------------------------------

    public boolean isProperty;

    //---------------------------------
    //    parent
    //---------------------------------

    public MXMLDescriptorSpecifier parent;

    //---------------------------------
    //    currentIndent
    //---------------------------------
 
    private int currentIndent = 0;

    //--------------------------------------------------------------------------
    //
    //    Methods
    //
    //--------------------------------------------------------------------------

    //---------------------------------
    //    indent
    //---------------------------------

    protected String getIndent()
    {
        return getIndent(currentIndent);
    }

    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        if (parent != null)
        {
            sb.append(parent.getIndent());
        }
        for (int i = 0; i < numIndent; i++)
            sb.append(JSRoyaleEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    public void indentPush()
    {
        currentIndent++;
    }

    public void indentPop()
    {
        if (currentIndent > 0)
        {
            currentIndent--;
        }
    }

    @Override
    protected void writeNewline()
    {
        write(ASEmitterTokens.NEW_LINE);
        write(getIndent(currentIndent));
    }

    //---------------------------------
    //    outputEventSpecifier
    //---------------------------------

    private void outputEventSpecifier(boolean writeNewline)
    {
        // number of events
        int count = 0;
        for (MXMLEventSpecifier me : eventSpecifiers)
        {
            if (me.name != null)
                count++;
        }
        write(count + "");

        for (MXMLEventSpecifier me : eventSpecifiers)
        {
            writeDelimiter(writeNewline);
            write(me.output(writeNewline));
        }
    }

    //---------------------------------
    //    outputPropertySpecifier
    //---------------------------------

    private String outputPropertySpecifier(boolean writeNewline)
    {
        write((isProperty) ? ASEmitterTokens.SINGLE_QUOTE.getToken() : "");
        write(name);
        write((isProperty) ? ASEmitterTokens.SINGLE_QUOTE.getToken() : "");
        writeDelimiter(writeNewline);

        if (isProperty)
        {
            if (value != null)
            {
                write(ASEmitterTokens.TRUE);
                writeDelimiter(writeNewline);
                // need to do other escaping here?
                // restrict="0-9.\"
                if (value.endsWith("\\'") && !value.endsWith("\\\\'"))
                {
                	value = value.substring(0, value.length() - 1) + "\\'";
                }
                write(value);
            }
            else
            {
                write((hasArray) ? ASEmitterTokens.NULL : ASEmitterTokens.FALSE);
                writeDelimiter(writeNewline);
                write(ASEmitterTokens.SQUARE_OPEN);
                indentPush();
                writeNewline();
                output(writeNewline);
                indentPop();
                writeNewline();
                write(ASEmitterTokens.SQUARE_CLOSE);
            }

            if (parent != null)
                writeDelimiter(writeNewline);
        }
        else
        {
            for (MXMLDescriptorSpecifier md : propertySpecifiers)
            {
                if (md.name != null && md.name.equals("mxmlContent"))
                {
                    childrenSpecifier = md;
                    propertySpecifiers.remove(md);
                    break;
                }
            }

            if (id != null || effectiveId != null)
            {
                write(propertySpecifiers.size() + 1 + "");
                writeDelimiter(writeNewline);
                String idPropName = (effectiveId != null) ? "_id"
                        : "id";
                writeSimpleDescriptor(idPropName, ASEmitterTokens.TRUE.getToken(),
                        ASEmitterTokens.SINGLE_QUOTE.getToken()
                                + ((id != null) ? id : effectiveId) + ASEmitterTokens.SINGLE_QUOTE.getToken(),
                        writeNewline);

                writeDelimiter(writeNewline);
            }
            else
            {
                write(propertySpecifiers.size() + "");
                writeDelimiter(writeNewline);
            }

            output(writeNewline);
        }

        return sb.toString();
    }

    //---------------------------------
    //    outputStyleSpecifier
    //---------------------------------

    private void outputStyleSpecifier(boolean writeNewline)
    {
        // TODO (erikdebruin) not yet implemented in Royale

        write("0");
        writeDelimiter(writeNewline);
    }

    //---------------------------------
    //    output
    //---------------------------------

    @Override
    public String output(boolean writeNewline)
    {
        if (isTopNode)
        {
            int count = 0;
            for (MXMLDescriptorSpecifier md : propertySpecifiers)
            {
                if (md.name != null)
                    count++;
            }

            write(count + "");
            writeNewline(ASEmitterTokens.COMMA);
        }

        MXMLDescriptorSpecifier model = null; // model goes first
        MXMLDescriptorSpecifier beads = null; // beads go last

        for (MXMLDescriptorSpecifier md : propertySpecifiers)
        {
            if (md.name != null && md.name.equals("model"))
            {
                model = md;

                break;
            }
        }

        if (model != null)
        {
            write(model.outputPropertySpecifier(writeNewline));
        }

        for (MXMLDescriptorSpecifier md : propertySpecifiers)
        {
            if (md.name != null)
            {
                if (!md.name.equals("model") && !md.name.equals("beads"))
                    write(md.outputPropertySpecifier(writeNewline));
                else if (md.name.equals("beads"))
                    beads = md;
            }
        }

        if (beads != null)
        {
            write(beads.outputPropertySpecifier(writeNewline));
        }

        if (!isProperty)
        {
            outputStyleSpecifier(writeNewline);

            // TODO (erikdebruin) not yet implemented in Royale
            //outputEffectSpecifier(writeNewline);

            outputEventSpecifier(writeNewline);

            if (!isTopNode)
            {
                writeDelimiter(writeNewline);

                if (childrenSpecifier == null)
                    write(ASEmitterTokens.NULL);
                else
                    outputChildren(childrenSpecifier, writeNewline);
            }

            boolean isLastChild = parent != null
                    && parent.propertySpecifiers.indexOf(this) == parent.propertySpecifiers
                            .size() - 1;

            if (!isLastChild && !isTopNode)
                writeDelimiter(writeNewline);
        }

        return sb.toString();
    }

    private void outputChildren(MXMLDescriptorSpecifier children, boolean writeNewline)
    {
        write(ASEmitterTokens.SQUARE_OPEN.getToken());
        if(writeNewline)
        {
            indentPush();
            writeNewline();
        }
        write(children.output(writeNewline));
        if(writeNewline)
        {
            indentPop();
            writeNewline();
        }
        write(ASEmitterTokens.SQUARE_CLOSE.getToken());
    }

    public String outputStateDescriptors(boolean writeNewLine)
    {
        for (MXMLDescriptorSpecifier md : propertySpecifiers)
        {
            write(ASEmitterTokens.SQUARE_OPEN);
            write(md.output(writeNewLine));
            write(ASEmitterTokens.SQUARE_CLOSE);
            writeNewline(ASEmitterTokens.COMMA);
        }
        return sb.toString();
    }
}
