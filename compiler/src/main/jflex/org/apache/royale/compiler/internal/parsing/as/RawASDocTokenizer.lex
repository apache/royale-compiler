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

package org.apache.royale.compiler.internal.parsing.as;

/*
 * This file is generated from RawASDocTokenizer.lex.
 * DO NOT MAKE EDITS DIRECTLY TO THIS FILE. 
 * THEY WILL BE LOST WHEN THE FILE IS GENERATED AGAIN.
 */

import antlr.Token;
import antlr.CommonToken;

@SuppressWarnings("all")

%%

%{

private boolean fromMXML = false;

public RawASDocTokenizer()
{
	//no-arg constructor
}

public final int getOffset()
{
	return yychar;
}

/**
 * Gets the current line number of the tokenizer.
 * Line numbers start at 0, not 1.
 */
public final int getLine()
{
	return yyline;
}

/**
 * Gets the current column number of the tokenizer.
 * Column numbers start at 0, not 1.
 */
public final int getColumn()
{
	return yycolumn;
}

protected final ASDocToken newToken(int type, int start, int end, int line, int column, CharSequence text)
{
	return new ASDocToken(type, start, end, line, column, text);
}

@Override
protected ASDocToken[] initTokenPool()
{
    return new ASDocToken[10];
}

/**
 * Retry the parse.  Call this immediately after nextToken() to discard the first
 * character in the token and start parsing again just after that.
 */
public void retry()
{
	zzMarkedPos = zzStartRead + 1;
}

protected final void unget()
{
	zzMarkedPos--;
}

protected final void startOrContinueAggregate()
{
	if (aggregateContents == null)
		startAggregate();
	else
		aggregateContents.append(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}

protected final void startOrContinueAggregate(String text)
{
	if (aggregateContents == null)
		startAggregate();
	aggregateContents.append(text);
}

public void setFromMXML(boolean fromMXML)
{
	this.fromMXML = fromMXML;
}

protected final void fillBuffer(StringBuilder builder)
{
	builder.append(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
}
	
%}

%char
%line
%state TAG, STRING1, STRING2
%class RawASDocTokenizer
%function nextToken
%type ASDocToken
%implements ASTokenTypes
%extends BaseRawTokenizer<ASDocToken>
%unicode
%ignorecase

LETTER=[a-zA-Z]
DIGIT=[0-9]
LETTER_DIGIT = ({LETTER}|{DIGIT})

WHITE_SPACE_CHAR=[\r\n\ \t\b\012]
NS_WHITE_SPACE_CHAR=[\r\n\t\b\012]

%%

<YYINITIAL> {NS_WHITE_SPACE_CHAR}+
{
	startOrContinueAggregate();
}

<YYINITIAL> "<!---"
{
	if (!fromMXML)
		startOrContinueAggregate();
}
<YYINITIAL> "-->"
{
	if (!fromMXML)
		startOrContinueAggregate();
}

<YYINITIAL> "/**"
{
}

<YYINITIAL> "* "
{
}

<YYINITIAL> "~~"
{
	startOrContinueAggregate("*");
}

<YYINITIAL> "*/"
{
	return buildAggregateToken(TOKEN_ASDOC_TEXT);
}

<YYINITIAL> "@"
{
	unget();
	yybegin(TAG);
	if (hasAggregateContents())
		return buildAggregateToken(TOKEN_ASDOC_TEXT);
} 

<YYINITIAL> "\""
{
	startOrContinueAggregate();
	yybegin(STRING1);
}

<STRING1> ([^\\\"])+
{
	startOrContinueAggregate();
}

<STRING1> ([\\](.|"\n"))+
{
	startOrContinueAggregate();
}

<STRING1> {WHITE_SPACE_CHAR}+
{
	startOrContinueAggregate();
}

<STRING1> <<EOF>>
{
	return buildAggregateToken(TOKEN_ASDOC_TEXT);
}

<STRING1> "\""
{
	yybegin(YYINITIAL);
	startOrContinueAggregate();
}

<YYINITIAL> <<EOF>>
{
	return buildAggregateToken(TOKEN_ASDOC_TEXT);
}

<YYINITIAL> .
{
	startOrContinueAggregate();
}

<TAG>  "@"{LETTER_DIGIT}+
{
	yybegin(YYINITIAL);
	return buildToken(TOKEN_ASDOC_TAG);
	}

<YYINITIAL, TAG, STRING1> .|"\n"
{
	// just ignore anything that we don't recognize
	// System.out.println(getContext(yyline));
	// System.out.println("Illegal character: " + <" + yytext() + ">");
}
