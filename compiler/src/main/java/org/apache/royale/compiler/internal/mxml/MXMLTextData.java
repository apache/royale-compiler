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

package org.apache.royale.compiler.internal.mxml;

import static org.apache.royale.compiler.constants.IMXMLCoreConstants.*;

import java.util.Collection;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Encapsulation of a block of text in MXML
 */
public class MXMLTextData extends MXMLUnitData implements IMXMLTextData
{
    /**
     * Constructor.
     */
    public MXMLTextData(MXMLToken textToken)
    {
        text = textToken.getText();

        setOffsets(textToken.getStart(), textToken.getEnd());
        setLine(textToken.getLine());
        setColumn(textToken.getColumn());
        setEndLine(textToken.getEndLine());
        setEndColumn(textToken.getEndColumn());

        switch (textToken.getType())
        {
            case MXMLTokenTypes.TOKEN_TEXT:
            {
                type = TextType.TEXT;
                break;
            }
            case MXMLTokenTypes.TOKEN_WHITESPACE:
            {
                type = TextType.WHITESPACE;
                break;
            }
            case MXMLTokenTypes.TOKEN_CDATA:
            {
                type = TextType.CDATA;
                break;
            }
            case MXMLTokenTypes.TOKEN_COMMENT:
            {
                type = TextType.COMMENT;
                break;
            }
            case MXMLTokenTypes.TOKEN_ASDOC_COMMENT:
            {
                type = TextType.ASDOC;
                break;
            }
        }
    }

    /**
     * The represented text
     */
    private String text;

    private TextType type;

    //
    // Object overrides
    //

    // For debugging only. This format is nice in the Eclipse debugger.
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        // Display TEXT, WHITESPACE, etc.
        sb.append(getTextType());

        sb.append(' ');

        // Display the text characters.
        sb.append('"');
        sb.append(getEscapedContent());
        sb.append('"');

        sb.append(' ');

        // Display line, column, start, and end as "17:5 160-188".
        sb.append(super.toString());

        return sb.toString();
    }

    //
    // MXMLUnitData overrides
    //

    @Override
    public boolean isText()
    {
        return true;
    }

    // For debugging only.
    @Override
    public String getTypeString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(super.getTypeString());
        sb.append(":");
        sb.append(getTextType());

        return sb.toString();
    }

    // For debugging only. This format is nice in a text file.
    @Override
    public String toDumpString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toDumpString());

        sb.append('\t');

        sb.append('|');
        sb.append(getEscapedContent());
        sb.append('|');

        return sb.toString();
    }

    //
    // IMXMLTextData implementations
    //

    @Override
    public String getContent()
    {
        return text;
    }

    @Override
    public TextType getTextType()
    {
        return type;
    }

    //
    // Other methods
    //

    // For debugging only.
    private String getEscapedContent()
    {
        String s = getContent();

        s = s.replaceAll("\n", "\\\\n");
        s = s.replaceAll("\r", "\\\\r");
        s = s.replaceAll("\t", "\\\\t");

        return s;
    }

    @Override
    public String getCompilableText()
    {
        switch (type)
        {
            case TEXT:
            case CDATA:
            case WHITESPACE:
            {
                return getContents();
            }

            case ASDOC:
            case COMMENT:
            {
                return "";
            }
        }

        assert false;
        return null;
    }

    @Override
    public int getCompilableTextStart()
    {
        return getContentsStart();
    }

    @Override
    public int getCompilableTextEnd()
    {
        return getContentsEnd();
    }

    @Override
    public int getCompilableTextLine()
    {
        return getLine();
    }

    @Override
    public int getCompilableTextColumn()
    {
        return (getContentsStart() - getAbsoluteStart()) + getColumn();
    }

    /**
     * Return the text contained within this unit, without any opening or
     * closing delimiters
     * 
     * @return a normalized string
     */
    public String getContents()
    {
        String tokenString = text;

        switch (type)
        {
            case CDATA:
            {
                if (tokenString.endsWith(cDataEnd))
                {
                    tokenString = tokenString.substring(cDataStart.length(), tokenString.length() - cDataEnd.length());
                }
                else
                {
                    tokenString = tokenString.substring(cDataStart.length());
                }
                return tokenString;
            }
            case ASDOC:
            {
                if (tokenString.endsWith(asDocEnd))
                {
                    tokenString = tokenString.substring(asDocStart.length(), tokenString.length() - asDocEnd.length());
                }
                else
                {
                    tokenString = tokenString.substring(asDocStart.length());
                }
                return tokenString;
            }
            case COMMENT:
            {
                if (tokenString.endsWith(commentEnd))
                {
                    tokenString = tokenString.substring(commentStart.length(), tokenString.length() - commentEnd.length());
                }
                else
                {
                    tokenString = tokenString.substring(commentStart.length());
                }
                return tokenString;
            }
            default:
            {
                break;
            }
        }

        return text;
    }

    /**
     * @return The start offset of actual content
     */
    @SuppressWarnings("incomplete-switch")
	public int getContentsStart()
    {
        switch (type)
        {
            case CDATA:
                return getAbsoluteStart() + cDataStart.length();

            case ASDOC:
                return getAbsoluteStart() + asDocStart.length();

            case COMMENT:
                return getAbsoluteStart() + commentStart.length();
        }

        return getAbsoluteStart();
    }

    /**
     * @return The end offset of content
     */
    @SuppressWarnings("incomplete-switch")
	public int getContentsEnd()
    {
        switch (type)
        {
            case CDATA:
                return text.endsWith(cDataEnd) ? getAbsoluteEnd() - cDataEnd.length() : getAbsoluteEnd();

            case ASDOC:
                return text.endsWith(asDocEnd) ? getAbsoluteEnd() - asDocEnd.length() : getAbsoluteEnd();

            case COMMENT:
                return text.endsWith(commentEnd) ? getAbsoluteEnd() - commentEnd.length() : getAbsoluteEnd();
        }

        return getAbsoluteEnd();
    }

    public int getContentsLine()
    {
        return getLine();
    }

    @SuppressWarnings("incomplete-switch")
	public int getContentsColumn()
    {
        switch (type)
        {
            case CDATA:
                return getColumn() + cDataStart.length();

            case ASDOC:
                return getColumn() + asDocStart.length();

            case COMMENT:
                return getColumn() + commentStart.length();
        }

        return getColumn();
    }

    @Override
    public ISourceFragment[] getFragments(Collection<ICompilerProblem> problems)
    {
        ISourceLocation location = this;

        switch (type)
        {
            case TEXT:
            {
                // TEXT might contain one or more entities,
                // in which case we will return multiple fragments.
                MXMLDialect mxmlDialect = getMXMLDialect();
                return EntityProcessor.parse(text, location, mxmlDialect, problems);
            }

            case WHITESPACE:
            {
                // WHITESPACE creates only a single fragment.
                ISourceFragment fragment = new SourceFragment(text, location);
                return new ISourceFragment[] {fragment};
            }

            case CDATA:
            {
                // CDATA creates only a single fragment.
                ISourceFragment fragment = new SourceFragment(text, getContents(), getContentsStart(), getContentsLine(), getContentsColumn());
                return new ISourceFragment[] {fragment};
            }

            default:
            {
                assert false : "Unexpected type of MXMLTextData";
            }
        }

        return null;
    }
}
