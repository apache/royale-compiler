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
 * This is a grammar for advanced CSS in Flex. It parses the CSS document and
 * generate DOM objects.
 */
grammar CSS;

options 
{
    language = Java;
//    output = AST;
//    k = 2;
}

tokens
{
    I_RULES, I_MEDIUM_CONDITIONS, I_DECL, I_RULE, I_SELECTOR_GROUP, I_SELECTOR,
    I_SIMPLE_SELECTOR, I_ARRAY
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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.flex.compiler.problems.CSSParserProblem;
}

/*@lexer::header
{
package org.apache.flex.compiler.internal.css;

import org.apache.flex.compiler.problems.CSSParserProblem;
}

@lexer::members
{

 * Lexer problems.
protected List<CSSParserProblem> problems = new ArrayList<CSSParserProblem>();

 * Collect lexer problems.
@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e)
{
    problems.add(CSSParserProblem.create(this, tokenNames, e));
}
}*/

@members 
{

/**
 * Parser problems.
 */
/*protected List<CSSParserProblem> problems = new ArrayList<CSSParserProblem>();*/

/**
 * Collect parser problems.
 */
/*@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e)
{
    problems.add(CSSParserProblem.create(this, tokenNames, e));
}*/

/**
 * Check if the cursor is at the end of a simple selector.
 */
/*private final boolean endOfSimpleSelector()
{
    final CommonToken nextToken = (CommonToken) getTokenStream().LT(1);
    if (nextToken == null)
        return true;
    
    // Check if there's white space between the previous token and the next token.
    final CommonToken lastToken = (CommonToken) getTokenStream().LT(-1);
    if (lastToken != null && nextToken != null)
    {
        final int lastStop = lastToken.getStopIndex();
        final int nextStart = nextToken.getStartIndex();
        if (lastStop + 1 < nextStart)
        {
            return true;
        }
    }
    
    // If the next token is "{" or ",", it's also end of a selector.
    final int nextType = nextToken.getType();
    if (nextType == BLOCK_OPEN || nextType == COMMA)
    {
        return true;
    }
    
    return false;
}*/

}

/**
 * Root rule for a CSS file.
 */
stylesheet
    :   ( namespaceStatement | fontFace | mediaQuery | ruleset )*
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
//    	->	^(AT_NAMESPACE ID? STRING)
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
//    	->	^(AT_MEDIA medium ruleset*)
    ;
  
/**
 * This rule matches the actual Flex media query conditions.
 * For example: (application-dpi: 240) and (os-platform: "Android")
 */
medium 
    :   mediumCondition    
        (  
            // Flex only support "and" at the momement. (Shaoting)
            'and' 
            mediumCondition 
        )*
//        ->	^(I_MEDIUM_CONDITIONS mediumCondition*)
    ;
  
/**
 * This rule matches a media query condition expression. It is either a 
 * word (like "all", "screen") or an expression like "(....)".
 */
mediumCondition
    :   ID 			
    |   ARGUMENTS
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
    :   AT_FONT_FACE declarationsBlock
//        -> ^(AT_FONT_FACE declarationsBlock)
    ;
  
/**
 * This rule matches a CSS Ruleset.
 */
ruleset
    :   selectorGroup declarationsBlock
//        -> ^(I_RULE selectorGroup declarationsBlock)
    ;

/**
 * This rule matches a group of comma-separated selectors.
 */
selectorGroup
	:	compoundSelector ( COMMA compoundSelector )*  
//        ->	^(I_SELECTOR_GROUP compoundSelector+)
    ;    

/**
 * Compound selector is a chain of simple selectors connected by combinators.
 * Each compound selector has one "subject", which is the element name of the
 * right most simple selector in the chain.
 * 
 * Currently, only descendant combinator (space) is supported.
 * If we need to support combinators, add the terminals in this production.
 * 
 * For example:
 * s|VBox s|HBox.content s|Button
 */
compoundSelector
@init 
{
	final List<Object> simpleSelectorNodeList = new ArrayList<Object>();
	//Object currentSimpleSelectorNode = adaptor.create(I_SIMPLE_SELECTOR, "I_SIMPLE_SELECTOR");
	Token simpleSelectorStartToken = null;
    Token simpleSelectorStopToken = null;
}
/*@after
{
	for(final Object simpleSelectorNode : simpleSelectorNodeList)
		adaptor.addChild($compoundSelector.tree, simpleSelectorNode);
}*/
    :   (   l=simpleSelectorFraction 
/*			{
			    // expand token range of the current simple selector
				if (simpleSelectorStartToken == null)
				    simpleSelectorStartToken = $l.start;
				simpleSelectorStopToken = $l.stop;
				
				adaptor.addChild(currentSimpleSelectorNode, $l.tree);
				
			    if (endOfSimpleSelector()) 
			    {
			        adaptor.setTokenBoundaries(
                        currentSimpleSelectorNode,
                        simpleSelectorStartToken,
                        simpleSelectorStopToken);
			    	simpleSelectorNodeList.add(currentSimpleSelectorNode);
                        
                    simpleSelectorStartToken = null;
                    simpleSelectorStopToken = null;
                    
			    	currentSimpleSelectorNode = adaptor.create(I_SIMPLE_SELECTOR, "I_SIMPLE_SELECTOR");			   
			    }
			}*/
        )+
//        ->	^(I_SELECTOR)
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
    :   element
    |   condition 
    ;
   
/**
 * This rule matches a condition selector.
 * For example: ".styleName", "#loginButton", ":up"
 */
condition
    :   ( DOT ID
        | HASH_WORD 
        | COLON ID
        ) 
    ;
  
/** 
 * This rule matches an element name with or without namespace prefix. It also
 * matches the special "any" element "*".
 * For example: "s|Panel", "Label", "*"
 */
element
    :   ID PIPE ID
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
//        ->	^(I_DECL declaration*)
    ;

/**
 * This rule matches property declaration. The declaration is a key value pair.
 * For example:
 * font-size:12px
 */  
declaration
    :   ID COLON value
//    	->	^(COLON ID value)
    ;
    
/**
 * This rule matches an array of property values or a single value.
 * If it matches an array, the output is an I_ARRAY tree of element values.
 * If it matches an single value, the output is a "singleValue" tree.
 * 
 * Array example:
 *     2.0, 2.0, 3.0, 3.0
 *     "tl", "tr", "bl", "br"
 *     #FFFFFF, #FFFFFF, #FFFFFF, #FFFFFF
 *     Verdana, Times, Sans-Serif
 *     solid 1px #666666
 *
 */    
value  
@init { int count = 1; }
    :   singleValue ( COMMA? singleValue { count++; } )*
//        -> {count > 1}? ^(I_ARRAY singleValue+)
//        ->              singleValue
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
    :   NUMBER_WITH_UNIT
    |   HASH_WORD
    |   CLASS_REFERENCE ARGUMENTS
//    								-> ^(CLASS_REFERENCE ARGUMENTS)
    |   PROPERTY_REFERENCE ARGUMENTS
//    								-> ^(PROPERTY_REFERENCE ARGUMENTS)
    |   EMBED ARGUMENTS
//    								-> ^(EMBED ARGUMENTS)
    |   URL ARGUMENTS
//    			    -> ^(URL ARGUMENTS)
    |   LOCAL ARGUMENTS
//    	        -> ^(LOCAL ARGUMENTS)
    |   RGB
    |   STRING						
    |   ID 
    ;

/* Lexer Rules */
  
BLOCK_OPEN : '{' ;
BLOCK_END :  '}' ;
COMMA : ',' ;
PERCENT : '%' ;
PIPE : '|' ; 
STAR : '*' ;
DOT : '.' ;
EQUALS: '='; 
AT_NAMESPACE : '@namespace' ;
AT_MEDIA : '@media' ;
COLON : ':' ;
AT_FONT_FACE : '@font-face' ;
CLASS_REFERENCE : 'ClassReference' ;
PROPERTY_REFERENCE : 'PropertyReference' ;
EMBED : 'Embed' ;
URL : 'url' ;
LOCAL : 'local' ;
NULL : 'null' ;

/** 
 * Matches a rgb definition - rgb(100%,100%,100%)
 */
RGB : 	'rgb(' 	( WS* NUMBER ( PERCENT | ) WS* ) ',' 
				( WS* NUMBER ( PERCENT | ) WS* ) ',' 
				( WS* NUMBER ( PERCENT | ) WS* ) 
		')' ; 

/** Arguments of a function call property value. */
ARGUMENTS
    :   '(' .*? ')'
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
  
ID  :   ( '-' | '_'  )? LETTER ( LETTER | DIGIT | '-' | '_'  )*
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
 * Matches a number with optinal unit string.
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
NUMBER_WITH_UNIT
	:	NUMBER (ID|PERCENT)?
	;
	
fragment
DIGIT
    :   '0'..'9' 
    ;
  
COMMENT
    :   '/*' .*? '*/'
    ;

WS  :   ( ' ' | '\t' | '\r' | '\n' )
    ;

STRING
    :   ['"''\']
        ( ~( '\\' | ['"''\'] ) )*
        ['"''\']
    ;

