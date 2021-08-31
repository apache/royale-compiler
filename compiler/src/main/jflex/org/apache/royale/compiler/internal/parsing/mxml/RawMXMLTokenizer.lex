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

package org.apache.royale.compiler.internal.parsing.mxml;

/*
 * RawMXMLTokenizer.java is generated from RawMXMLTokenizer.lex.
 * DO NOT MAKE EDITS DIRECTLY TO RawMXMLTokenizer.java.
 * THEY WILL BE LOST WHEN THE FILE IS GENERATED AGAIN.
 */

import antlr.Token;
import antlr.CommonToken;
import org.apache.royale.compiler.internal.parsing.mxml.BaseRawMXMLTokenizer;
import static org.apache.royale.compiler.parsing.MXMLTokenTypes.*;

@SuppressWarnings("all")

%%

%{

/**
 * Nested '<' bracket level (for <!DOCTYPE et al)
 */
protected int bracketLevel;

/**
 * Get the current column of the tokenizer.
 */
public final int getColumn()
{
	return yycolumn;
}

/**
 * Unget one character
 */
protected final void unget()
{
	zzMarkedPos--;
}

public final int getLine()
{
	return yyline;
}

/**
 * Setter for the start offset from where the token's offsets will start
 */
public final void setOffset(int offset)
{
    yychar = offset;
}

public final int getOffset()
{
	return yychar;
}

public RawMXMLTokenizer()
{
}

protected final Token buildToken(int type, int start, int end, int line, int column, String text)
{
	MXMLToken token = new MXMLToken(type, start, end, line, column, text);
	token.setSourcePath(sourcePath);
	return token;
}

public void reset()
{
	super.reset();
	bracketLevel = 0;
}

protected final void fillBuffer(StringBuilder builder)
{
	builder.append(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
}

%}

%char
%line
%column
%state CDATA, COMMENT, ASDOC_COMMENT, DIRECTIVE, MARKUP, STRING1, STRING2, MARKUP_IGNORE, STRING1_IGNORE, STRING2_IGNORE, WHITESPACE
%class RawMXMLTokenizer
%function nextToken
%type Token
%extends BaseRawMXMLTokenizer
%unicode
%ignorecase

LETTER=[a-zA-Z]
DIGIT=[0-9]
ID_FIRST=({LETTER}|":"|"_")
ID_FOLLOW=({LETTER}|{DIGIT}|":"|"_"|".")
ID={ID_FIRST}{ID_FOLLOW}*

WHITE_SPACE_CHAR=[\r\n\ \t\b\012]

%%

//
// Whitespace has semantic significance only in certain states.
// In the following states, we simply ignore it.
//

<DIRECTIVE, MARKUP_IGNORE, STRING1_IGNORE, STRING2_IGNORE, MARKUP> {WHITE_SPACE_CHAR}+
{
}

//
// The YYINITIAL state is the starting state of the lexer, and the state to which
// other states eventually return in a well-formed document.
// In this state, it looks for processing instructions, whitespace, comments, DTDs, tags,
// text, and CDATA blocks; it then transitions to other states to handle each of these
// (except for text, which doesn't need a separate state).
// It builds TOKEN_OPEN_TAG_START and TOKEN_CLOSE_TAG_START to represent the beginning
// of tags such as
//   <s:Button
// and
//   </s:Button
// and also builds TOKEN_TEXT tokens to represent text.
// All other tokens are built by other states.
//

<YYINITIAL> "<?"
{
	startAggregate();
	yybegin(DIRECTIVE);
}

<YYINITIAL> {WHITE_SPACE_CHAR}+
{
	startAggregate();
	yybegin(WHITESPACE);
}

<YYINITIAL> "<!--"
{
	startAggregate();
	yybegin(COMMENT);
}

<YYINITIAL> "<!---"
{
	startAggregate();
	yybegin(ASDOC_COMMENT);
}

<YYINITIAL> ("<!DOCTYPE"|"<!ENTITY"|"<!ELEMENT"|"<!ATTLIST"|"<!NOTATION")
{
	yybegin(MARKUP_IGNORE);
	bracketLevel = 1;
}

<YYINITIAL> ("<"{ID})
{ 
	Token token = buildToken(TOKEN_OPEN_TAG_START); 
    yybegin(MARKUP); 
	return token;
}
	
<YYINITIAL> ("</"{ID})
{
	yybegin(MARKUP);
	return buildToken(TOKEN_CLOSE_TAG_START);
}

<YYINITIAL> [^<]+
{
	return buildToken(TOKEN_TEXT);
}

<YYINITIAL> "<![CDATA["
{
	startAggregate();
	yybegin(CDATA);
}

//
// The DIRECTIVE state lexes an XML processing instruction such as
//   <?xml version="1.0" encoding="utf-8"?>
// It builds a single TOKEN_PROCESSING_INSTRUCTION token
// containing the text of the entire instruction before
// returning to the initial state.
//

<DIRECTIVE> ([^?])*
{
	continueAggregate();
}

<DIRECTIVE> ("?"+[^?>])
{
	continueAggregate();
}

<DIRECTIVE> ("?"+">")
{
	continueAggregate();
	yybegin(YYINITIAL);
	return buildAggregateToken(TOKEN_PROCESSING_INSTRUCTION);
}

<DIRECTIVE> .
{
	continueAggregate();
}

<DIRECTIVE><<EOF>>
{
	continueAggregate();
	yybegin(YYINITIAL);
	return buildAggregateToken(TOKEN_PROCESSING_INSTRUCTION);
}

//
// The WHITESPACE state lexes whitespace that has semantic significance.
// It builds a single TOKEN_WHITESPACE token before returning to the initial state.
//

<WHITESPACE> [<]
{
	yybegin(YYINITIAL);
	unget();
	return buildAggregateToken(TOKEN_WHITESPACE);
}

<WHITESPACE><<EOF>>
{
	yybegin(YYINITIAL);
	return buildAggregateToken(TOKEN_WHITESPACE); 
}

//
// The COMMENT and ASDOC_COMMENT states lex comments such as
// <!-- This is a normal comment -->
// and
// <!--- This is an ASDoc comment -->
// They build  a single TOKEN_COMMENT and TOKEN_ASDOC_COMMENT token
// before returning to the initial state.
//

<COMMENT,ASDOC_COMMENT> [^]
{
	continueAggregate();
}

<COMMENT> ~("-""-"+">")
{
	continueAggregate();
	yybegin(YYINITIAL);
	return buildAggregateToken(TOKEN_COMMENT);
}

<ASDOC_COMMENT> ~("-""-"+">")
{
	continueAggregate();
	yybegin(YYINITIAL);
	return buildAggregateToken(TOKEN_ASDOC_COMMENT);
}

<COMMENT><<EOF>>
{
	MXMLToken token = (MXMLToken)buildAggregateToken(TOKEN_COMMENT);
	if (token != null)
            reportUnclosedComment(token);
	return token;
}

<ASDOC_COMMENT><<EOF>>
{
	MXMLToken token = (MXMLToken)buildAggregateToken(TOKEN_ASDOC_COMMENT);
        if (token != null)
	    reportUnclosedASDocComment(token);
	return token;
}

//
// The MARKUP_IGNORE, STRING1_IGNORE, and STRING2_IGNORE states
// lex the DTD by ignoring it. They build no tokens.
//

<MARKUP_IGNORE> ([^<>\"']*)
{
}

<MARKUP_IGNORE> [<]
{
	bracketLevel++;
}

<MARKUP_IGNORE> [>]
{
	bracketLevel--;
	if (bracketLevel == 0)
		yybegin(YYINITIAL);
}

<MARKUP_IGNORE> [\"]
{
	yybegin(STRING1_IGNORE);
}

<MARKUP_IGNORE> [']
{
	yybegin(STRING2_IGNORE);
}

<STRING1_IGNORE> ([^\"<])*
{
}

<STRING1_IGNORE> [\"]
{
	yybegin(MARKUP_IGNORE);
}

<STRING1_IGNORE> [<]
{
	yybegin(MARKUP_IGNORE);
	unget();
}

<STRING2_IGNORE> ([^'<])*
{
}

<STRING2_IGNORE> [']
{
	yybegin(MARKUP_IGNORE);
}

<STRING2_IGNORE> [<]
{
	yybegin(MARKUP_IGNORE);
	unget();
}

//
// The MARKUP state lexes the parts of normal tags such as
//   <s:Button xmlns:s="library://ns.adobe.com/flex/spark" id="b">
// or
//   </s:Button>
// or
//  <s:Button id="b"/>
// that follow the tag name.
// 
// It builds a TOKEN_XMLNS or TOKEN_NAME token for each attribute name
// (depending on whether it is a namespace attribute),
// and a TOKEN_EQUALS token for each equals sign in an attribute.
// The attribute values are handled by transitioning to the
// STRING1 and STRING2 states, depending on whether the attribute
// value is delimited by double quotes or single quotes.
// At the end of the tag, it builds a TOKEN_TAG_END token (for >)
// or a TOKEN_EMPTY_TAG_END token (for />) before returning to the
// initial state.

<MARKUP> "xmlns"(":"{ID_FOLLOW}*)?
{
	return buildToken(TOKEN_XMLNS);
}

<MARKUP> ({ID})
{
	return buildToken(TOKEN_NAME);
}

<MARKUP> "="
{
	return buildToken(TOKEN_EQUALS);
}

<MARKUP> (">")
{
	yybegin(YYINITIAL);
	return buildToken(TOKEN_TAG_END);
}

<MARKUP> ("/>")
{
	yybegin(YYINITIAL);
	return buildToken(TOKEN_EMPTY_TAG_END);
}

<MARKUP> "<"
{
	unget();
	yybegin(YYINITIAL);
}

<MARKUP> [\"]
{
	startAggregate();
	yybegin(STRING1);
}

<MARKUP> [']
{
	startAggregate();
	yybegin(STRING2);
}

//
// The STRING1 state handles attribute values within double-quotes
// such as the OK in
//  label="OK"
// It builds a single TOKEN_STRING token.
//

<STRING1> ([^\"<])*
{
	continueAggregate();
}

<STRING1> {WHITE_SPACE_CHAR}+
{
	continueAggregate();
}

<STRING1> [\"]
{
	yybegin(MARKUP);
	continueAggregate();
	return buildAggregateToken(TOKEN_STRING);
}

<STRING1> [<]
{
	yybegin(MARKUP);
	unget();
	return buildAggregateToken(TOKEN_STRING);
}

<STRING1><<EOF>>
{
	yybegin(MARKUP);
	continueAggregate();
	return buildAggregateToken(TOKEN_STRING);
}

//
// The STRING2 state handles attribute values within single-quotes
// such as the OK in
//  label='OK'
// It builds a single TOKEN_STRING token.
//

<STRING2> ([^'<])*
{
	continueAggregate();
}

<STRING2> {WHITE_SPACE_CHAR}+
{
	continueAggregate();
}

<STRING2> [']
{
	yybegin(MARKUP);
	continueAggregate();
	return buildAggregateToken(TOKEN_STRING);
}

<STRING2> [<]
{
	yybegin(MARKUP);
	unget();
	return buildAggregateToken(TOKEN_STRING);
}

<STRING2><<EOF>>
{
	yybegin(MARKUP);
	continueAggregate();
	return buildAggregateToken(TOKEN_STRING);
}

//
// The CATA state handles a CDATA block such as
//    <![CDATA[File > New]]>
// It builds a single TOKEN_CDATA token
// before returning to the initial state.
//

<CDATA> [^]
{
	continueAggregate();
}


<CDATA> ~("]""]"+">")
{
	continueAggregate();
	yybegin(YYINITIAL);
	return buildAggregateToken(TOKEN_CDATA);
}

<CDATA><<EOF>>
{
	MXMLToken token = (MXMLToken)buildAggregateToken(TOKEN_CDATA);
        if (token != null)
	    reportUnclosedCDATA(token);
	return token;
}

//
// Just ignore anything we don't recognize
//

<YYINITIAL, MARKUP, STRING1, STRING2, MARKUP_IGNORE, STRING1_IGNORE, STRING2_IGNORE> .
{
	reportBadCharacterProblem(yytext());
}
