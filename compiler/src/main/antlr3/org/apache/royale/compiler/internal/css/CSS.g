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

/**
 * This is a grammar for advanced CSS in Royale. It parses the CSS document and
 * generate DOM objects.
 */
grammar CSS;

options 
{
    language = Java;
    output = AST;
    k = 2;
}

tokens
{ 
    I_RULES; I_MEDIUM_CONDITIONS; I_DECL; I_RULE; I_SELECTOR_GROUP; I_SELECTOR; 
    I_SIMPLE_SELECTOR; I_CHILD_SELECTOR; I_PRECEDED_SELECTOR; I_SIBLING_SELECTOR; I_ARRAY; I_MULTIVALUE; 
}

@header 
{
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

package org.apache.royale.compiler.internal.css;

import java.util.Map;
import java.util.HashMap;
import org.apache.royale.compiler.problems.CSSParserProblem;
}

@lexer::header 
{
package org.apache.royale.compiler.internal.css;

import org.apache.royale.compiler.problems.CSSParserProblem;
} 

@lexer::members
{

/**
 * Lexer problems.
 */
protected List<CSSParserProblem> problems = new ArrayList<CSSParserProblem>();

/**
 * Collect lexer problems.
 */
@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e)
{
    problems.add(CSSParserProblem.create(this, tokenNames, e));
}
}

@members 
{

/**
 * Parser problems.
 */
protected List<CSSParserProblem> problems = new ArrayList<CSSParserProblem>();

/**
 * Collect parser problems.
 */
@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e)
{
    problems.add(CSSParserProblem.create(this, tokenNames, e));
}

/**
 * Check if the cursor is at the end of a simple selector.
 * @return -1 if not end
 * @return 0 if end
 * @return 1 if ended by Child combinator
 * @return 2 if ended by Preceded combinator
 * @return 3 if ended by Sibling combinator
 */
private final int endOfSimpleSelector()
{
    final CommonToken nextToken = (CommonToken) getTokenStream().LT(1);
    if (nextToken == null)
        return 0;
    
    final int nextType = nextToken.getType();

    if (nextType == CHILD) 
    { 
        getTokenStream().consume();
        return 1;
    }
    if (nextType == PRECEDED)
    {
        getTokenStream().consume();
        return 2;
    }
    if (nextType == TILDE)
    {
        getTokenStream().consume();
        return 3;
    }

    // Check if there's white space between the previous token and the next token.
    final CommonToken lastToken = (CommonToken) getTokenStream().LT(-1);
    if (lastToken != null)
    {
        final int lastType = lastToken.getType();
        if (lastType == CHILD)
        {
            return 1;
        }
        if (lastType == PRECEDED)
        {
            return 2;
        }
        if (lastType == TILDE)
        {
            return 3;
        }
        if (nextToken != null)
        {
            final int lastStop = lastToken.getStopIndex();
            final int nextStart = nextToken.getStartIndex();
            if (lastStop + 1 < nextStart)
            {
                return 0;
            }
        }
    }

    
    // If the next token is "{" or ",", it's also end of a selector.
    if (nextType == BLOCK_OPEN || nextType == COMMA)
    {
        return 0;
    }
    
    return -1;
}

}

/**
 * Root rule for a CSS file.
 */
stylesheet
    :   ( namespaceStatement | fontFace | keyframes | mediaQuery | ruleset )*
    ;

/**
 * This rule matches an "@namespace" statement.
 *  
 * For example, declaring a namespace prefix:
 * @namespace mx "library://ns.adobe.com/flex/mx";
 *
 * Declaring the default namespace:
 * @namespace "library://ns.adobe.com/flex/spark";
 */
namespaceStatement
    :   AT_NAMESPACE ID? STRING SEMI_COLONS    
    	->	^(AT_NAMESPACE ID? STRING)
    ;
  
/**
 * This rule matches a media query block.
 * 
 * For example:
 * @media all { ... }
 *
 * A media query block can have one or many rulesets inside.
 */
keyframes
    :   AT_KEYFRAMES ID BLOCK_OPEN ruleset* BLOCK_END
    	->	^(AT_KEYFRAMES ID ruleset*)
    |   AT_WEBKIT_KEYFRAMES ID BLOCK_OPEN ruleset* BLOCK_END
    	->	^(AT_WEBKIT_KEYFRAMES ID ruleset*)
    ;
  
/**
 * This rule matches a media query block.
 * 
 * For example:
 * @media all { ... }
 *
 * A media query block can have one or many rulesets inside.
 */
mediaQuery
    :   AT_MEDIA medium BLOCK_OPEN ruleset* BLOCK_END
    	->	^(AT_MEDIA medium ruleset*)
    ;
  
/**
 * This rule matches the actual Royale media query conditions.
 * For example: (application-dpi: 240) and (os-platform: "Android")
 */
medium 
    :   mediumCondition    
        (  
            // Royale only support "and" at the moment.
            'and' 
            mediumCondition
            | mediumCondition
        )*
        ->	^(I_MEDIUM_CONDITIONS mediumCondition*)
    ;
  
/**
 * This rule matches a media query condition expression. It is either a 
 * word (like "all", "screen") or an expression like "(....)".
 */
mediumCondition
    :   ID
    |   ONLY ID 			
    |   ARGUMENTS
    |   COMMA
    ;
    
/**
 * For example:
 *
 * @font-face {
 *     src: url("../assets/MyriadWebPro.ttf");
 *     fontFamily: myFontFamily;
 *     advancedAntiAliasing: true;
 * }
 *
 * @see http://livedocs.adobe.com/flex/3/html/help.html?content=fonts_04.html
 */    
fontFace
    :   AT_FONT_FACE declarationsBlock	-> ^(AT_FONT_FACE declarationsBlock)
    ;
  
/**
 * This rule matches a CSS Ruleset.
 */
ruleset
    :   selectorGroup declarationsBlock
        -> ^(I_RULE selectorGroup declarationsBlock)
    ;

/**
 * This rule matches a group of comma-separated selectors.
 */
selectorGroup
	:	compoundSelector ( COMMA compoundSelector )*  
        ->	^(I_SELECTOR_GROUP compoundSelector+)
    ;    

/**
 * Compound selector is a chain of simple selectors connected by combinators.
 * Each compound selector has one "subject", which is the element name of the
 * right most simple selector in the chain.
 * 
 * For example:
 * s|VBox s|HBox.content s|Button
 */
compoundSelector
@init 
{
	final List<Object> simpleSelectorNodeList = new ArrayList<Object>();
	Object currentSimpleSelectorNode = adaptor.create(I_SIMPLE_SELECTOR, "I_SIMPLE_SELECTOR");
	Token simpleSelectorStartToken = null;
    Token simpleSelectorStopToken = null;
}
@after 
{
	for(final Object simpleSelectorNode : simpleSelectorNodeList)
		adaptor.addChild($compoundSelector.tree, simpleSelectorNode);
}
    :   (   l=simpleSelectorFraction ( '+' | '>'  )?
			{ 
			    // expand token range of the current simple selector
				if (simpleSelectorStartToken == null)
				    simpleSelectorStartToken = $l.start;
				simpleSelectorStopToken = $l.stop;
				
				adaptor.addChild(currentSimpleSelectorNode, $l.tree);
			    
                            int end = endOfSimpleSelector();
			    if (end != -1) 
			    {
			        adaptor.setTokenBoundaries(
                        currentSimpleSelectorNode,
                        simpleSelectorStartToken,
                        simpleSelectorStopToken);
			    	simpleSelectorNodeList.add(currentSimpleSelectorNode);
                        
                    simpleSelectorStartToken = null;
                    simpleSelectorStopToken = null;
                    
                                if (end == 1)
                                  currentSimpleSelectorNode =  adaptor.create(I_CHILD_SELECTOR, "I_CHILD_SELECTOR");
                                else if (end == 2)		   
			    	  currentSimpleSelectorNode = adaptor.create(I_PRECEDED_SELECTOR, "I_PRECEDED_SELECTOR");			   
                                else if (end == 3)			   
			    	  currentSimpleSelectorNode = adaptor.create(I_SIBLING_SELECTOR, "I_SIBLING_SELECTOR");
                                else
                              	  currentSimpleSelectorNode = adaptor.create(I_SIMPLE_SELECTOR, "I_SIMPLE_SELECTOR");			   
			    }
			}
        )+
        ->	^(I_SELECTOR)   
        // Tree nodes for simple selectors are added in the @after block.
    ;
    
/**
 * A compound selector consists of one or many simple selectors.
 * This rule matches a fraction of a simple selector.
 *
 *   s|HBox s|Button.confirm #activeLabel,  <-- compound selector
 *   s|HBox               <-- simple selector
 *   s|Button.confirm     <-- simple selector
 *   #activeLabel         <-- simple selector
 *
 * This rule will match each of the following 4 fractions:
 *
 *   [s|HBox] [s|Button] [.confirm] [#activeLabel]
 *
 * As you can see, the parse tree isn't aware of the start and end of a simple
 * selector. This is because white spaces have been ignored so that the parser 
 * can't distinguish between the following input:
 *
 *   s|Button.rounded .labelText   <-- 2 simple selectors
 *   s|Button.rounded.labelText    <-- 1 simple selector
 * 
 * As a result, this rule could match only part of a simple selector, because
 * the conditions were broken into multiple tokens.
 */  
simpleSelectorFraction
    :   condition 
    |   element
    ;
   
/**
 * This rule matches a condition selector.
 * For example: ".styleName", "#loginButton", ":up"
 */
condition
    :   ( DOT^ ID
        | HASH_WORD 
        | COLON^ NOT ARGUMENTS 
        | COLON^ ID 
        | DOUBLE_COLON^ ID 
        | attributeSelector
        ) 
    ;
  
/** 
 * This rule matches an element name with or without namespace prefix. It also
 * matches the special "any" element "*".
 * For example: "s|Panel", "Label", "*"
 */
element
    :   ID PIPE^ ID
    |   NUMBER_WITH_PERCENT            
    |   ID          
    |   STAR
    ;
    
/**
 * This rule matches a declarations block.
 * For example:
 * { font-size:12; font-family: "sans"; }
 */    
declarationsBlock
    :   BLOCK_OPEN 
        (   
            SEMI_COLONS?   // multiple semi-colons are legal
            declaration 
            ( 
                SEMI_COLONS 
                declaration
            )* 
            SEMI_COLONS? 
        )?
        BLOCK_END
        ->	^(I_DECL declaration*)
    ;

/**
 * This rule matches property declaration. The declaration is a key value pair.
 * For example:
 * font-size:12px
 */  
declaration
    :   ID COLON value	->	^(COLON ID value)
    ;
    
/**
 * This rule matches an comma-separated array of property values or a single value.
 * If it matches an array, the output is an I_ARRAY tree of element values.
 * If it matches an single value, the output is a "singleValue" tree.
 * 
 * Array example:
 *     2.0, 2.0, 3.0, 3.0
 *     "tl", "tr", "bl", "br"
 *     #FFFFFF, #FFFFFF, #FFFFFF, #FFFFFF
 *     Verdana, Times, Sans-Serif
 *     20px 20px, 40px 40px
 *
 */    
value  
@init { int count = 1; }
    :   multiValue ( COMMA multiValue { count++; } )*
        -> {count > 1}? ^(I_ARRAY multiValue+)
        ->              multiValue
    ;  

/**
 * This rule matches an space-separated array of property values or a single value.
 * If it matches an array, the output is an I_MULTIVALUE tree of element values.
 * If it matches an single value, the output is a "singleValue" tree.
 * 
 * multiValue example:
 *     solid 1px #666666
 *
 */    
multiValue
@init { int count = 1; }
    :   singleValue ( singleValue { count++; } )*
        -> {count > 1}? ^(I_MULTIVALUE singleValue+)
        ->              singleValue
    ;

/**
 * This rule matches one property value.
 *
 * For example:
 *     12, 12px, 100%.
 *     #FF3322
 *     rgb(100%, 100%, 100%)
 *     ClassReference("mx.controls.Button")
 *     PropertyReference("size")
 *     Embed(source="assets/logo.png", mimeType="images/png")
 *     url("../fonts/myfont.ttf")
 *     local("My Font")
 *     "Times New Roman"
 *     italic
 */
singleValue
    :   NUMBER_WITH_PERCENT
    |   NUMBER_WITH_UNIT
    |   HASH_WORD
    |   CLASS_REFERENCE ARGUMENTS
    								-> ^(CLASS_REFERENCE ARGUMENTS)
    |   PROPERTY_REFERENCE ARGUMENTS
    								-> ^(PROPERTY_REFERENCE ARGUMENTS)
    |   EMBED ARGUMENTS
    								-> ^(EMBED ARGUMENTS)
    |   URL ARGUMENTS formatOption*   -> ^(URL ARGUMENTS formatOption*)
    |   LOCAL ARGUMENTS		        -> ^(LOCAL ARGUMENTS)
    |   CALC ARGUMENTS		        -> ^(CALC ARGUMENTS)
    |   FUNCTIONS ARGUMENTS		        -> ^(FUNCTIONS ARGUMENTS)
    |   ALPHA_VALUE
    |   SCALE_VALUE
    |   RECT_VALUE
    |   ROTATE_VALUE
    |   TRANSLATE3D_VALUE
    |   RGB
    |   RGBA
    |   STRING						
    |   ID
    |   OPERATOR
    |   IMPORTANT 
    ;

formatOption
    :   FORMAT ARGUMENTS	-> ^(FORMAT ARGUMENTS)
	;
    
attributeSelector
    :   SQUARE_OPEN attributeName attributeOperator* attributeValue* SQUARE_END
    ;
    
attributeName
    :    ID
    ;
    
attributeOperator
    :    BEGINS_WITH
    |    ENDS_WITH
    |    CONTAINS
    |    LIST_MATCH
    |    HREFLANG_MATCH
    |    EQUALS
    ;
    
attributeValue
    :    STRING
    ;
    	
/* Lexer Rules */
  
BEGINS_WITH : '^=' ;
ENDS_WITH : '$=' ;
CONTAINS : '*=' ;
LIST_MATCH : '~=' ;
HREFLANG_MATCH : '|=' ;
BLOCK_OPEN : '{' ;
BLOCK_END :  '}' ;
SQUARE_OPEN : '[' ;
SQUARE_END :  ']' ;
COMMA : ',' ;
PERCENT : '%' ;
PIPE : '|' ; 
STAR : '*' ;
TILDE : '~' ;
DOT : '.' ;
EQUALS: '='; 
AT_NAMESPACE : '@namespace' ;
AT_MEDIA : '@media' ;
AT_KEYFRAMES : '@keyframes' ;
AT_WEBKIT_KEYFRAMES : '@-webkit-keyframes' ;
DOUBLE_COLON : '::' ;
COLON : ':' ;
AT_FONT_FACE : '@font-face' ;
CLASS_REFERENCE : 'ClassReference' ;
PROPERTY_REFERENCE : 'PropertyReference' ;
IMPORTANT : '!important' ;
EMBED : 'Embed' ;
URL : 'url' ;
FORMAT : 'format' ;
LOCAL : 'local' ;
CALC : 'calc' ;
SCALE : 'scale' ;
NULL : 'null' ;
ONLY : 'only' ;
CHILD : '>' ;
PRECEDED : '+' ;
FUNCTIONS : '-moz-linear-gradient'
          | '-webkit-linear-gradient'
          | 'linear-gradient'
          | 'progid:DXImageTransform.Microsoft.gradient'
          | 'translateX'
          | 'translateY'
          | 'translate'
          | 'blur'
          | 'brightness'
          | 'contrast'
          | 'drop-shadow'
          | 'hue-rotate'
          | 'invert'
          | 'saturate'
          | 'sepia'
          ;
/**
 * Removed for now this two since conflicts with same keywords in old fucntion
 * This will be fixed later  
 *        | 'grayscale'
 *        | 'opacity'
 */
NOT
    :  'not'
    ;

/** 
 * Matches an alpha filter - alpha(opacity=70)
 */
ALPHA_VALUE : 	'alpha(' ( options {greedy=false;}: . )* ')' ; 

/** 
 * Matches an alpha filter - alpha(opacity=70)
 */
ROTATE_VALUE : 	'rotate(' ( options {greedy=false;}: . )* ')' ; 

/** 
 * Matches an scale value - scale(1.001)
 */
SCALE_VALUE : 	'scale(' ( options {greedy=false;}: . )* ')' ; 

/** 
 * Matches a translate3d value - translate3d(1.001)
 */
TRANSLATE3D_VALUE : 	'translate3d(' ( options {greedy=false;}: . )* ')' ; 

/** 
 * Matches an rect value - rect(x,y,w,h)
 */
RECT_VALUE : 	'rect(' ( options {greedy=false;}: . )* ')' ; 

/** 
 * Matches an rgba definition - rgba(100%,100%,100%,100%)
 */
RGBA : 	'rgba(' 	( WS* NUMBER ( PERCENT | ) WS* ) ',' 
				( WS* NUMBER ( PERCENT | ) WS* ) ',' 
				( WS* NUMBER ( PERCENT | ) WS* ) ',' 
				( WS* NUMBER ( PERCENT | ) WS* ) 
		')' ; 

/** 
 * Matches a rgb definition - rgb(100%,100%,100%)
 */
RGB : 	'rgb(' 	( WS* NUMBER ( PERCENT | ) WS* ) ',' 
				( WS* NUMBER ( PERCENT | ) WS* ) ',' 
				( WS* NUMBER ( PERCENT | ) WS* ) 
		')' ; 


/** Arguments of a function call property value. */
ARGUMENTS
    :   '(' ( options {greedy=false;}: ARGUMENTS | . )* ')'
    ;

/**
 * Match multiple semi-colons in Lexer level so that the parser can have a 
 * finite number of look ahead, instead of LL(*);
 */
SEMI_COLONS : ';'+ ;

/**
 * Matches either ID selector or color value. Unfortunately, these two types of
 * tokens are ambiguous in ANTLR world.
 */
HASH_WORD
    :   '#' ( LETTER | DIGIT | '-' | '_' )+
    ;

ID  :   ( '-' | '_' | '__' | '___' )? LETTER ( LETTER | DIGIT | '-' | '_'  )*
    ;
    
/**
 * Matches: + - * /
 */
OPERATOR
	:   ('+'|'-'|'*'|'/')
    ;    
    

fragment
LETTER
    :   'a'..'z' 
    |   'A'..'Z'
    ;

/**
 * Matches: 100, -100, +3.14, .25, -0.25, +.25
 */
fragment
NUMBER
	:   ('+'|'-')? 
		(	DIGIT+ (DOT DIGIT+)? 
		|   DOT DIGIT+
		)
    ;    


/**
 * Matches a number with optional unit string.
 * For example:
 *   2.5
 *   2.5em
 *   100%
 *
 * The grammar doesn't allow spaces between the number and the unit.
 * For example: 35 em is a "NUMBER" and an "ID".
 * Otherwise such input will be ambigiuous:
 *    font-style: bold 35 pt 25em
 * "pt" could either be another keyword or the unit of "35".
 */
NUMBER_WITH_PERCENT
    :   NUMBER PERCENT
    ;
    
NUMBER_WITH_UNIT
	:	NUMBER (ID)?
	;
	
fragment
DIGIT
    :   '0'..'9' 
    ;
  
COMMENT
    :   '/*' ( options {greedy=false;}: . )* '*/' 
        { $channel = HIDDEN; }
    ;

WS  :   ( ' ' | '\t' | '\r' | '\n' )  { $channel = HIDDEN; }
    ;

STRING
    :   STRING_QUOTE 
        ( ~( '\\' | STRING_QUOTE ) | ESCAPED_HEX )* 
        STRING_QUOTE
    ;
  
fragment
STRING_QUOTE
    :   '"'
    |   '\''
    ;

fragment
HEX_DIGIT
    :    DIGIT
    |    ('a'..'f'|'A'..'F')
    ;
    
fragment
ESCAPED_HEX
    :    '\\' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    |    '\\' HEX_DIGIT HEX_DIGIT HEX_DIGIT
    |    '\\' HEX_DIGIT HEX_DIGIT
    |    '\\' HEX_DIGIT
    ;
