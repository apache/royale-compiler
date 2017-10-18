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
 * This file is generated from RawASTokenizer.lex.
 * DO NOT MAKE EDITS DIRECTLY TO THIS FILE. 
 * THEY WILL BE LOST WHEN THE FILE IS GENERATED AGAIN.
 */

import antlr.Token;
import antlr.CommonToken;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASKeywordConstants;

@SuppressWarnings("all")

%%

%{

/*
 * The order of rules does not matter since JFlex is a longest-match system,
 * so groupings can be changed accordingly when dealing with
 * situations where we have smaller rules.
 */

/**
 * tells us the type of string we are dealing with for when we end them
 */
private enum StringKind
{
	CHAR, STRING
}

private StringKind stringKind;

/**
 * in E4X, <code>&lt;</code> is allowed in char and string literals. 
 */
private boolean allowLT;

public RawASTokenizer()
{
	allowLT = true;
}

protected final int getOffset()
{
	return yychar;
}

/**
 * Gets the current line number of the tokenizer.
 * Line numbers start at 0, not 1.
 */
protected final int getLine()
{
	return yyline;
}

/**
 * Gets the current column number of the tokenizer.
 * Column numbers start at 0, not 1.
 */
protected final int getColumn()
{
	return yycolumn;
}

protected void setAllowLTInE4XStringLiterals(boolean allow)
{
	allowLT = allow;
}

%}

//make sure that allowLT is set by all the constructors that are generated
%init{ 
 	allowLT = true;
%init}

%char
%line
%column
%state COMMENT, ASDOC_COMMENT, TYPED_COLLECTION, TYPED_COLLECTION_LITERAL, STRINGLITERAL, ESCAPE_SEQUENCE, E4X, DIRECTIVE, STATE, ENTITY_RETURN, MARKUP_IGNORE, STRINGLITERAL_IGNORE, CHARLITERAL_IGNORE, CDATA, E4XCOMMENT, MARKUP, E4XSTRINGLITERAL, E4XCHARLITERAL, E4XTEXTVALUE
%class RawASTokenizer
%function nextToken
%type ASToken
%implements ASTokenTypes
%extends BaseRawASTokenizer
%public
%final
%unicode
%pack

// Naming of the patterns are based on AS3 Language Spec - Syntax (version Feb 15, 2011).
DECIMAL_DIGITS = [0-9]+
EXPONENT_PART = ("E"|"e") ("+"|"-")? {DECIMAL_DIGITS}
DECIMAL_LITERAL_1 = {DECIMAL_DIGITS} "." {DECIMAL_DIGITS}? {EXPONENT_PART}?
DECIMAL_LITERAL_1 = {DECIMAL_DIGITS} "." {DECIMAL_DIGITS}? {EXPONENT_PART}?
DECIMAL_LITERAL_2 = "." {DECIMAL_DIGITS} {EXPONENT_PART}?
DECIMAL_LITERAL_3 = {DECIMAL_DIGITS} {EXPONENT_PART}?
DECIMAL_LITERAL = ({DECIMAL_LITERAL_1}|{DECIMAL_LITERAL_2}|{DECIMAL_LITERAL_3})

HEX_DIGIT=[a-fA-F0-9]
HEX_NUMBER = ("0X"|"0x"){HEX_DIGIT}+

UNICODE_ESCAPE = "u"{HEX_DIGIT}{4}

ID_FIRST=[:jletter:]|"_"|"$"|"\\"{UNICODE_ESCAPE}
ID_FOLLOW=[:jletter:]|[:jletterdigit:]|"_"|"$"|"\\"{UNICODE_ESCAPE}
E4X_ID_FIRST=[:jletter:]|":"|"_"
E4X_ID_FOLLOW=[:jletter:]|[:jletterdigit:]|":"|"_"|"-"

// http://www.unicode.org/versions/Unicode6.0.0/ch06.pdf
// Page 186, Table 6-2 Unicode Space Characters
UNICODE_ZS_CATEGORY=[\u0020\u00A0\u1680\u180E\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000]
WHITE_SPACE_CHAR={LINE_TERMINATOR} | [ \t\b\012\f] | {UNICODE_ZS_CATEGORY}
HEX_ENTITY="&#x"{HEX_DIGIT}+";"
DECIMAL_ENTITY="&#\\"DECIMAL_DIGITS";"
ENTITY=("&amp;")|("&apos;")|("&gt;")|("&lt;")|("&quot;")

// AS3 Syntax Specification, Chapter 3.2 Whitespaces and Line Terminators
LINE_TERMINATOR = (\r|\n|\r\n|\u2028|\u2029)
LINE_COMMENT = "//" [^\r\n]* {LINE_TERMINATOR}?

REGEX_START_CHAR=[^\n\r\*\[/\\]
REGEX_CHAR=[^\n\r\\/\[]
REGEX_ESCAPE=("\\"[^])|("\\u"{HEX_DIGIT}{4})
REGEX_CLASS="[" ({REGEX_ESCAPE}|[^\n\r\]\\])* "]"

%%

//
// Whitespace
//

<YYINITIAL> {WHITE_SPACE_CHAR}+
{
}

//
// Comments
//

// C style comment
<YYINITIAL> "/*" [^*]*
{
    if (collectComments)
        startAggregate();
    yybegin(COMMENT);
}

<COMMENT> [^]
{
	continueAggregate();
}

<COMMENT> ~"*/"
{
    yybegin(YYINITIAL);
    if (collectComments)
    {
        continueAggregate();
        return buildAggregateToken(HIDDEN_TOKEN_MULTI_LINE_COMMENT);
    }
}

<COMMENT><<EOF>>
{
    yybegin(YYINITIAL);
    if (collectComments)
    {
        continueAggregate();
        return buildAggregateToken(HIDDEN_TOKEN_MULTI_LINE_COMMENT);
    }
}

// AS DOC comment style
<YYINITIAL> "/**"
{
    startAggregate();
    yybegin(ASDOC_COMMENT);
}

<ASDOC_COMMENT> [^]
{
	continueAggregate();
}

<ASDOC_COMMENT> ~"*/"
{
    yybegin(YYINITIAL);
    continueAggregate();
    return buildAggregateToken(TOKEN_ASDOC_COMMENT);
}

<ASDOC_COMMENT><<EOF>>
{
    yybegin(YYINITIAL);
    reportUnclosedASDoc();
    continueAggregate();
    return buildAggregateToken(TOKEN_ASDOC_COMMENT);
}

// Flash Builder thinks that /**/ is an asdoc comment, so match it's behavior
<YYINITIAL> "/*" "*"+ "/"
{
    return buildToken(TOKEN_ASDOC_COMMENT);
}

<YYINITIAL> {LINE_COMMENT}
{
	if (collectComments)
		return buildToken(HIDDEN_TOKEN_SINGLE_LINE_COMMENT);
}

// Regex
<YYINITIAL> [/] ({REGEX_START_CHAR}|{REGEX_ESCAPE}|{REGEX_CLASS}) ({REGEX_CHAR}|{REGEX_ESCAPE}|{REGEX_CLASS})* [/] [:jletterdigit:]* 
{
	if (ASToken.canPreceedRegex(getLastTokenType())) 
	{
		return buildToken(TOKEN_LITERAL_REGEXP, yytext());
	}
	else if (yylength() > 1 && yycharat(1) == '=')
	{
        final ASToken token = buildToken(TOKEN_OPERATOR_DIVISION_ASSIGNMENT, "/=");
        zzMarkedPos = zzStartRead + 2; // back track then skip over '/='
        return token;
	}
    else
    {
        final ASToken token = buildToken(TOKEN_OPERATOR_DIVISION, "/");
        zzMarkedPos = zzStartRead + 1; // back track then skip over '/'
        return token;
    }
}

//
// Numeric literals
//

<YYINITIAL> {DECIMAL_LITERAL}
{
	return buildToken(TOKEN_LITERAL_NUMBER);
}

<YYINITIAL> {HEX_NUMBER}
{
	return buildToken(TOKEN_LITERAL_HEX_NUMBER);
}

//
// Three keywords needed to help determine if something is a regex
//

<YYINITIAL> "return"
{
	return buildToken(TOKEN_KEYWORD_RETURN, IASKeywordConstants.RETURN);
}

<YYINITIAL> "throw"
{
	return buildToken(TOKEN_KEYWORD_THROW, IASKeywordConstants.THROW);
}

<YYINITIAL> "new"
{
	return buildToken(TOKEN_KEYWORD_NEW, IASKeywordConstants.NEW);
}

// Identifier
<YYINITIAL> {ID_FIRST}({ID_FOLLOW})*
{
	return buildToken(TOKEN_IDENTIFIER, yytext());
	}

//
// Strings
//

<YYINITIAL> "\""
{
	startAggregate();
	stringKind = StringKind.STRING;
	yybegin(STRINGLITERAL);

}

<YYINITIAL> "'"
{
	startAggregate();
	stringKind = StringKind.CHAR;
	yybegin(STRINGLITERAL);
}

// String handling for both double and single quoted strings

<STRINGLITERAL> "\""
{
	continueAggregate();
	if (stringKind == StringKind.STRING)
	{
		yybegin(YYINITIAL);
		return buildAggregateToken(TOKEN_LITERAL_STRING);
	}
}

<STRINGLITERAL> "'"
{
	continueAggregate(); 
	if (stringKind == StringKind.CHAR)
	{
		yybegin(YYINITIAL);
		return buildAggregateToken(TOKEN_LITERAL_STRING);
	}
} 

<STRINGLITERAL> "\\"
{
	yybegin(ESCAPE_SEQUENCE);
}

<STRINGLITERAL> {LINE_TERMINATOR}+
{
	continueAggregate(); 
    reportInvalidLineTerminatorInStringLiteral(); 
	yybegin(YYINITIAL);
	return buildAggregateToken(TOKEN_LITERAL_STRING);
}

<STRINGLITERAL> [^\\]
{
	continueAggregate();
}

<STRINGLITERAL, ESCAPE_SEQUENCE> <<EOF>>
{
    yybegin(YYINITIAL);
    continueAggregate();
    reportUnclosedStringLiteral();
    return buildAggregateToken(TOKEN_LITERAL_STRING);
}

<ESCAPE_SEQUENCE> "b"
{
	continueAggregate('\b');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "f"
{
	continueAggregate('\f');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "n"
{
	continueAggregate('\n');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "r"
{
	continueAggregate('\r');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "t"
{
	continueAggregate('\t');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "v"
{
	continueAggregate((char)0xb);
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "'"
{
	continueAggregate('\'');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "\""
{
	continueAggregate('"');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "\\"
{
	continueAggregate('\\');
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> "x"{HEX_DIGIT}{2}
{
	aggregateEscapedUnicodeChar("\\" + yytext());
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> {UNICODE_ESCAPE}
{
	aggregateEscapedUnicodeChar("\\" + yytext());
	yybegin(STRINGLITERAL);
}

<ESCAPE_SEQUENCE> {LINE_TERMINATOR}
{
	continueAggregate(new char[0]);
	yybegin(STRINGLITERAL);
}
<ESCAPE_SEQUENCE> [^\"\\]
{
	continueAggregate(yycharat(0));
	yybegin(STRINGLITERAL);
}

// handle declaration ambiguity on, e.g. "var x:*=1;"
<YYINITIAL> "*="
{
	return buildToken(getLastTokenType() == TOKEN_COLON ?
	                  HIDDEN_TOKEN_STAR_ASSIGNMENT :
	                  TOKEN_OPERATOR_MULTIPLICATION_ASSIGNMENT);
}

// Vector
<YYINITIAL> ".<"
{
	typedDepth++;
	yybegin(TYPED_COLLECTION);
	return buildToken(TOKEN_TYPED_COLLECTION_OPEN, ".<");
}

//
// Other operators
//

<YYINITIAL> "/"
{
	return buildToken(TOKEN_OPERATOR_DIVISION, "/");
}	

<YYINITIAL> "%"
{
	return buildToken(TOKEN_OPERATOR_MODULO, "%");
}	

<YYINITIAL> "<<"
{
	return buildToken(TOKEN_OPERATOR_BITWISE_LEFT_SHIFT, "<<");
}	

<YYINITIAL> ">>"
{
	return buildToken(TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT,">>");
}	

<YYINITIAL> ">>>"
{
	return buildToken(TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT, ">>>");
}	

<YYINITIAL> "<="
{
	return buildToken(TOKEN_OPERATOR_LESS_THAN_EQUALS, "<=");
}	

<YYINITIAL> ">="
{
	return buildToken(TOKEN_OPERATOR_GREATER_THAN_EQUALS, ">=");
}	

<YYINITIAL> "=="
{
	return buildToken(TOKEN_OPERATOR_EQUAL, "==");
}	

<YYINITIAL> "!="
{
	return buildToken(TOKEN_OPERATOR_NOT_EQUAL, "!=");
}	

<YYINITIAL> "==="
{
	return buildToken(TOKEN_OPERATOR_STRICT_EQUAL, "===");
}	

<YYINITIAL> "!=="
{
	return buildToken(TOKEN_OPERATOR_STRICT_NOT_EQUAL, "!==");
}	

<YYINITIAL> "&"
{
	return buildToken(TOKEN_OPERATOR_BITWISE_AND, "&");
}	

<YYINITIAL> "^"
{
	return buildToken(TOKEN_OPERATOR_BITWISE_XOR, "^");
}	

<YYINITIAL> "|"
{
	return buildToken(TOKEN_OPERATOR_BITWISE_OR, "|");
}	

<YYINITIAL> "&&"
{
	return buildToken(TOKEN_OPERATOR_LOGICAL_AND, "&&");
}	

<YYINITIAL> "||"
{
	return buildToken(TOKEN_OPERATOR_LOGICAL_OR, "||");
}	

<YYINITIAL> "="
{
	return buildToken(TOKEN_OPERATOR_ASSIGNMENT, "=");
}

<YYINITIAL> "::"
{
	return buildToken(TOKEN_OPERATOR_NS_QUALIFIER, "::");
}

<YYINITIAL> "."
{
	return buildToken(TOKEN_OPERATOR_MEMBER_ACCESS, ".");
}

<YYINITIAL> "*"
{
	return buildToken(TOKEN_OPERATOR_STAR, "*");
}

<YYINITIAL> "~"
{
	return buildToken(TOKEN_OPERATOR_BITWISE_NOT, "~");
}

<YYINITIAL> "!"
{
	return buildToken(TOKEN_OPERATOR_LOGICAL_NOT, "!");
}

<YYINITIAL> "++"
{
	return buildToken(TOKEN_OPERATOR_INCREMENT, "++");
}

<YYINITIAL> "--"
{
	return buildToken(TOKEN_OPERATOR_DECREMENT, "--");
}

<YYINITIAL> "?"
{
	return buildToken(TOKEN_OPERATOR_TERNARY, "?");
}

<YYINITIAL> "+"
{
	return buildToken(TOKEN_OPERATOR_PLUS, "+");
}

<YYINITIAL> "-"
{
	return buildToken(TOKEN_OPERATOR_MINUS, "-");
}

<YYINITIAL> "@"
{
	return buildToken(TOKEN_OPERATOR_ATSIGN, "@");
}

<YYINITIAL> "..."
{
	return buildToken(TOKEN_ELLIPSIS, "...");
}

<YYINITIAL> ".."
{
	return buildToken(TOKEN_OPERATOR_DESCENDANT_ACCESS, "..");
}

<YYINITIAL> ","
{
	return buildToken(TOKEN_COMMA, ",");
}

<YYINITIAL> ":"
{
	return buildToken(TOKEN_COLON, ":");
}

<YYINITIAL> "("
{
	return buildToken(TOKEN_PAREN_OPEN, "(");
}

<YYINITIAL> ")"
{
	return buildToken(TOKEN_PAREN_CLOSE, ")");
}

<YYINITIAL> ";"
{
	return buildToken(TOKEN_SEMICOLON, ";");
}

<YYINITIAL> "["
{
	return buildToken(TOKEN_SQUARE_OPEN, "[");
}

<YYINITIAL> "]"
{
	return buildToken(TOKEN_SQUARE_CLOSE, "]");
}

<YYINITIAL> "&&="
{
	return buildToken(TOKEN_OPERATOR_LOGICAL_AND_ASSIGNMENT, "&&=");
}

<YYINITIAL> "||="
{
	return buildToken(TOKEN_OPERATOR_LOGICAL_OR_ASSIGNMENT, "||=");
}

<YYINITIAL> "/="
{
	return buildToken(TOKEN_OPERATOR_DIVISION_ASSIGNMENT, "/=");
}

<YYINITIAL> "%="
{
	return buildToken(TOKEN_OPERATOR_MODULO_ASSIGNMENT, "%=");
}

<YYINITIAL> "+="
{
	return buildToken(TOKEN_OPERATOR_PLUS_ASSIGNMENT, "+=");
}

<YYINITIAL> "-="
{
	return buildToken(TOKEN_OPERATOR_MINUS_ASSIGNMENT, "-=");
}

<YYINITIAL> "<<="
{
	return buildToken(TOKEN_OPERATOR_BITWISE_LEFT_SHIFT_ASSIGNMENT, "<<=");
}

<YYINITIAL> ">>="
{
	return buildToken(TOKEN_OPERATOR_BITWISE_RIGHT_SHIFT_ASSIGNMENT, ">>=");
}

<YYINITIAL> ">>>=" 
{
	return buildToken(TOKEN_OPERATOR_BITWISE_UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, ">>>=");
}

<YYINITIAL> "&="
{
	return buildToken(TOKEN_OPERATOR_BITWISE_AND_ASSIGNMENT, "&=");
}

<YYINITIAL> "^="
{
	return buildToken(TOKEN_OPERATOR_BITWISE_XOR_ASSIGNMENT, "^=");
}

<YYINITIAL> "|="
{
	return buildToken(TOKEN_OPERATOR_BITWISE_OR_ASSIGNMENT, "|=");
}

//
// E4X
//

<YYINITIAL> "{"
{  
	if (e4xTagDepth == 0)
	{
		return buildToken(TOKEN_BLOCK_OPEN, "{");
	}
	else
	{ 
		e4xBraceBalance++; 
		return buildToken(TOKEN_E4X_BINDING_OPEN, "{"); 
	} 
}

<YYINITIAL> "}"
{ 
	if (e4xTagDepth == 0)
	{
		return buildToken(TOKEN_BLOCK_CLOSE, "}"); 
	}
	else
	{ 
		e4xBraceBalance--;
		if (e4xBraceBalance == 0) 
			yybegin(e4xReturnState); 
		e4xReturnState = E4X; 
	}
	return buildToken(TOKEN_E4X_BINDING_CLOSE, "}");   
}

<YYINITIAL> ("<"{WHITE_SPACE_CHAR}*">") 
{
	// Only allow a literal if we are not inside databindings or E4X already
	if (e4xTagDepth == 0 && e4xBraceBalance == 0 && ASToken.canPreceedE4X(getLastTokenType()))
	{
		yybegin(E4XTEXTVALUE); //jump to text value because this is the equivalent of MARKUP(">") which jumps to text values
		e4xTagDepth++;
		return buildToken(TOKEN_LITERAL_XMLLIST);
	}
		
	return buildToken(TOKEN_OPERATOR_LESS_THAN,"<");
}

<YYINITIAL> ("<")
{
	// If we have tag depth, we can't be in a databinding expression
	if (e4xTagDepth != 0 && e4xBraceBalance == 0)
	{ 
		yypushback(1);
		yybegin(E4X);
	}
	else
	{
		if(ASToken.canPreceedE4X(getLastTokenType()))
		{
			yypushback(1);
			yybegin(E4X);
			break;
		}
		else if(getLastTokenType() == TOKEN_KEYWORD_NEW)
		{
			typedDepth++;
			yybegin(TYPED_COLLECTION_LITERAL);
			return buildToken(TOKEN_TYPED_LITERAL_OPEN);
		}
		return buildToken(TOKEN_OPERATOR_LESS_THAN, "<");
	}
}

<YYINITIAL> (">")
{
	// If we have tag depth, we can't be in a databinding expression
	if (e4xTagDepth != 0 && e4xBraceBalance == 0)
	{
		yypushback(1);
		yybegin(MARKUP);
	}
	else
	{
		if (typedDepth > 0)
		{
			typedDepth--;
			return buildToken(TOKEN_TYPED_COLLECTION_CLOSE,">");
		}
		return buildToken(TOKEN_OPERATOR_GREATER_THAN,">");
	}
}

//
// Vectors and vector literals
//

<TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> {ID_FIRST}{ID_FOLLOW}*
{
	return buildToken(TOKEN_IDENTIFIER);
}

<TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> "."
{
	return buildToken(TOKEN_OPERATOR_MEMBER_ACCESS);
}

<TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> "*"
{
	if (typedDepth > 0)
	{
		return buildToken(TOKEN_OPERATOR_STAR,"*");
	}
	else
	{
		yypushback(1);
		typedDepth = 0;
		yybegin(YYINITIAL);
	}
}

<TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> ".<"
{
	typedDepth++;
	return buildToken(TOKEN_TYPED_COLLECTION_OPEN);
}

<TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> ">"
{ 
	typedDepth--; 
	int tType = TOKEN_TYPED_COLLECTION_CLOSE;
	if (typedDepth == 0)
	{
		// If we entered the literal state, last token out should be a literal close.
		if (yystate() == TYPED_COLLECTION_LITERAL)
			tType = TOKEN_TYPED_LITERAL_CLOSE;
		yybegin(YYINITIAL);
	} 
	return buildToken(tType); 
}

<TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> {WHITE_SPACE_CHAR}+
{
}

<TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> .|"\n"
{
	yypushback(1);
	typedDepth = 0;
	yybegin(YYINITIAL);
}

//
// E4X
//

<E4X> {WHITE_SPACE_CHAR}+
{
	return buildToken(TOKEN_E4X_WHITESPACE);
}

<E4X> ("<!DOCTYPE"|"<!ENTITY"|"<!ELEMENT"|"<!ATTLIST"|"<!NOTATION")
{
	yybegin(MARKUP_IGNORE);
	docTypeLevel = 1;
}

<E4X> "<![CDATA["
{
	startAggregate();
	yybegin(CDATA);
}

<E4X> "<!--"
{
	startAggregate();
	yybegin(E4XCOMMENT);
}

<E4X> "<?"
{
	startAggregate();
	yybegin(DIRECTIVE);
}

<E4X> ("<"{E4X_ID_FIRST}{E4X_ID_FOLLOW}*)
{
	e4xTagDepth++;
	isInCloseTag = false;
	yybegin(MARKUP);
	return buildToken(TOKEN_E4X_OPEN_TAG_START);
}

<E4X> ("</"{E4X_ID_FIRST}{E4X_ID_FOLLOW}*)
{
	isInCloseTag = true;
	yybegin(MARKUP);
	return buildToken(TOKEN_E4X_CLOSE_TAG_START);
}

<E4X> ("<")
{
	yybegin(MARKUP);
	e4xTagDepth++;
	return buildToken(HIDDEN_TOKEN_E4X);
}

<E4X> ("</")
{
	isInCloseTag = true;
	yybegin(MARKUP);
	return buildToken(TOKEN_E4X_CLOSE_TAG_START);
}

<E4X> ("</"{WHITE_SPACE_CHAR}*">")
{
	e4xTagDepth--;
	return buildToken(TOKEN_E4X_XMLLIST_CLOSE);
}

<E4X> ("/>")
{
	e4xTagDepth--;
	return buildToken(TOKEN_E4X_EMPTY_TAG_END);
}

<E4X> ("=")
{
	yybegin(MARKUP);
	return buildToken(TOKEN_E4X_EQUALS);
}

<E4X> .|"\n"
{
	yypushback(1);
	yybegin(e4xTagDepth > 0 ? E4XTEXTVALUE : YYINITIAL);
} //if we are inside tags, treat the text as a text value

<MARKUP_IGNORE> ([^<>\"']*)
{
}

<MARKUP_IGNORE> [<]
{
	docTypeLevel++;
}

<MARKUP_IGNORE> [>]
{
	docTypeLevel--;
	if (docTypeLevel == 0)
		yybegin(E4X);
}

<MARKUP_IGNORE> [\"]
{
	yybegin(STRINGLITERAL_IGNORE);
}

<MARKUP_IGNORE> [']
{
	yybegin(CHARLITERAL_IGNORE);
}

<STRINGLITERAL_IGNORE> ([^\"<])*
{
}

<STRINGLITERAL_IGNORE> [\"]
{
	yybegin(MARKUP_IGNORE);
}

<STRINGLITERAL_IGNORE, CHARLITERAL_IGNORE> [<]
{
	yybegin(MARKUP_IGNORE);
	yypushback(1);
}

<CHARLITERAL_IGNORE> ([^'<])*
{
}

<CHARLITERAL_IGNORE> [']
{
	yybegin(MARKUP_IGNORE);
}

<CDATA> [^]
{
	continueAggregate();
}

<CDATA> ~("]""]"+">")
{
	continueAggregate();
	yybegin(E4X);
	return buildAggregateToken(TOKEN_E4X_CDATA);
}

<CDATA><<EOF>>
{
	reportUnclosedCDATA();
	return null;
}

<E4XCOMMENT> [^]
{
	continueAggregate();
}

<E4XCOMMENT> ~("-""-"+">")
{
	continueAggregate();
	yybegin(E4X);
	return buildAggregateToken(TOKEN_E4X_COMMENT);
}

<E4XCOMMENT><<EOF>>
{
        reportUnclosedComment();
	return null;
}

<DIRECTIVE> ([^?])*
{
	continueAggregate();
}

<DIRECTIVE> ("?"+[^?>])
{
	continueAggregate();
}

<DIRECTIVE> .
{
	continueAggregate();
}

<DIRECTIVE> ("?"+">")
{
	continueAggregate();
	yybegin(E4X);
	return buildAggregateToken(TOKEN_E4X_PROCESSING_INSTRUCTION);
}

<DIRECTIVE><<EOF>>
{
	continueAggregate();
	yybegin(E4X);
	return buildAggregateToken(TOKEN_E4X_PROCESSING_INSTRUCTION);
}

<MARKUP> "{"
{
	e4xBraceBalance++;
	e4xReturnState = MARKUP;
	yybegin(YYINITIAL);
	return buildToken(TOKEN_E4X_BINDING_OPEN);
}

<MARKUP> "}"
{
	e4xBraceBalance--;
	yybegin(MARKUP);
	return buildToken(TOKEN_E4X_BINDING_CLOSE, "}");
}

<MARKUP> (">")
{
	if (isInCloseTag)
	{
		isInCloseTag = false;
		e4xTagDepth--;
		yybegin(E4X);
	}
	else
	{
		yybegin(E4XTEXTVALUE);
	}
	return buildToken(TOKEN_E4X_TAG_END);
}

<MARKUP> ("/>")
{
	yybegin(E4X);
	e4xTagDepth--;
	return buildToken(TOKEN_E4X_EMPTY_TAG_END);
}

<MARKUP> {WHITE_SPACE_CHAR}+
{
	return buildToken(TOKEN_E4X_WHITESPACE);
}

<MARKUP> "."
{
	yybegin(STATE);
	return buildToken(TOKEN_E4X_NAME_DOT);
}

<MARKUP> "xmlns"?(":"{E4X_ID_FOLLOW}*)?
{
	return buildToken(TOKEN_E4X_XMLNS);
}

<MARKUP> ({E4X_ID_FIRST}{E4X_ID_FOLLOW}*)
{
	return buildToken(TOKEN_E4X_NAME);
}

<MARKUP> "="
{
	return buildToken(TOKEN_E4X_EQUALS);
}

<MARKUP> "<"
{
	yypushback(1); yybegin(E4X);
}

<MARKUP> [\"]
{
	startAggregate();
	yybegin(E4XSTRINGLITERAL);
}

<MARKUP> [']
{
	startAggregate();
	yybegin(E4XCHARLITERAL);
}

<STATE> {ID_FOLLOW}*
{ 
	yybegin(MARKUP);
	return buildToken(TOKEN_E4X_DOTTED_NAME_PART);
}

// We "buffer" a token here because JFlex can't return two tokens at the same time,
// or keep a pool of tokens.  In this case, we want to return the string content,
// then the entity. Clients have been updated to look for a buffered token. And,
// because it's longest match first and we are finding entities and databindings,
// we have to find this char by char.  We do a fast aggregate because it doesn't
// cause stringbuilder growth until we build the token, or until right before the
// underlying lexer buffer refills.

<E4XTEXTVALUE> {WHITE_SPACE_CHAR}+
{ 
	final ASToken tok = buildToken(TOKEN_E4X_WHITESPACE); 
	if (hasAggregateContents())
	{ 
	    bufferToken = tok; 
	    
	    // Put the text token before the whitespace in the buffer.
	    // Adjust the text token's end offset.
		final ASToken textToken = buildAggregateToken(TOKEN_E4X_TEXT); 
		textToken.setEnd(textToken.getEnd() - yytext().length());
		return textToken;
	}
	else
	{ 
		aggregateContents = null; 
		return tok; 
	}
}
<E4XTEXTVALUE> "<"
{
	yypushback(1);
	yybegin(E4X);
	if (hasAggregateContents())
	return buildAggregateToken(TOKEN_E4X_TEXT);
}

<E4XTEXTVALUE> [^<\{\}]
{
	if (!hasAggregateContents())
		startAggregate();
	else
		continueAggregate();
}

<E4XTEXTVALUE> "{"
{ 
	e4xBraceBalance++;
	e4xReturnState = yystate();
	yybegin(YYINITIAL); 
	ASToken tok = buildToken(TOKEN_E4X_BINDING_OPEN); 
	if (hasAggregateContents())
	{ 
		bufferToken = tok; 
		return buildAggregateToken(TOKEN_E4X_STRING); 
	}
	else
	{ 
		aggregateContents = null;
		return tok; 
	}
}

<E4XTEXTVALUE> "}"
{ 
	e4xBraceBalance--; 
	yybegin(e4xReturnState); 
	return buildToken(TOKEN_E4X_BINDING_CLOSE); 
}

<E4XSTRINGLITERAL, E4XCHARLITERAL, E4XTEXTVALUE> {ENTITY}
{
	return buildE4XTextToken(TOKEN_E4X_ENTITY);
}

<E4XSTRINGLITERAL, E4XCHARLITERAL, E4XTEXTVALUE> {HEX_ENTITY}
{
	return buildE4XTextToken(TOKEN_E4X_HEX_ENTITY);
}

<E4XSTRINGLITERAL, E4XCHARLITERAL, E4XTEXTVALUE> {DECIMAL_ENTITY}
{
	return buildE4XTextToken(TOKEN_E4X_DECIMAL_ENTITY);
}

<E4XSTRINGLITERAL, E4XCHARLITERAL, E4XTEXTVALUE> ("\\x"{HEX_DIGIT}{2})|("\\u"{HEX_DIGIT}{4})
{ 
    continueAggregate(escapedUnicodeToHtmlEntity(yytext()).toCharArray()); 
}

<E4XSTRINGLITERAL> ([^\"<])
{
	continueAggregate();
}

<E4XSTRINGLITERAL, E4XCHARLITERAL> {WHITE_SPACE_CHAR}+
{
	continueAggregate();
}

<E4XSTRINGLITERAL> [\"]
{ 
	yybegin(MARKUP); 
	continueAggregate(); 
	return buildAggregateToken(TOKEN_E4X_STRING); 
}

<E4XSTRINGLITERAL, E4XCHARLITERAL> [<]
{ 
	if (allowLT)
	{
		continueAggregate(); 
	}
	else
	{
	 	addBadCharacterProblem(yytext()); //char not allowed
		yybegin(MARKUP); 
		yypushback(1); 
		return buildAggregateToken(TOKEN_E4X_STRING); 
	}
}

<E4XCHARLITERAL> ([^'<])
{
	continueAggregate();
}

<E4XCHARLITERAL> [']
{ 
	yybegin(MARKUP); 
	continueAggregate(); 
	return buildAggregateToken(TOKEN_E4X_STRING); 
}

<MARKUP, E4XSTRINGLITERAL, DIRECTIVE, E4XCHARLITERAL, E4XCOMMENT, CDATA, E4XTEXTVALUE, STATE, MARKUP_IGNORE, STRINGLITERAL_IGNORE, CHARLITERAL_IGNORE> .
{
	e4xBraceBalance = 0;
	e4xTagDepth = 0;
	yypushback(1);
	yybegin(YYINITIAL);
}

<STATE> "\n"
{
	e4xBraceBalance = 0;
	e4xTagDepth = 0;
	yypushback(1);
	yybegin(YYINITIAL);
}

<YYINITIAL, STRINGLITERAL, ESCAPE_SEQUENCE, TYPED_COLLECTION, TYPED_COLLECTION_LITERAL> .|"\n"
{
	addBadCharacterProblem(yytext());
}
