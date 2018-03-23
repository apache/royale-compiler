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
 * This is a tree grammar for advanced CSS in Flex. It walks the AST generated 
 * by the CSS parser and builds CSS DOM objects.
 */
tree grammar CSSTree;

options 
{
    language = Java;
    tokenVocab = CSS;
    ASTLabelType = CommonTree;
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
import org.apache.royale.compiler.css.*;
import org.apache.royale.compiler.problems.CSSParserProblem;

}

@members 
{

/**
 * CSS DOM object.
 */
protected CSSDocument model;

/**
 * Every definition object needs the token stream to compute source location.
 */
private final TokenStream tokenStream = getTreeNodeStream().getTokenStream();

/**
 * Tree walker problems.
 */
protected List<CSSParserProblem> problems = new ArrayList<CSSParserProblem>();

/**
 * Used for building up attribute selector strings until we implement a data
 * structure for it.
 */
protected String curAttribute;


/**
 * Collect problems.
 */
@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e)
{
    problems.add(CSSParserProblem.create(this, tokenNames, e));
}
}

stylesheet
scope 
{
    // namespace declarations are buffered in this map
    List<CSSNamespaceDefinition> namespaces;
    // ruleset definitions are buffered in this list
    List<CSSRule> rules;
    // font-face declarations are buffered in this list
    List<CSSFontFace> fontFaces;
    // keyframe declarations are buffered in this list
    List<CSSKeyFrames> keyFrames;
}
@init 
{
    $stylesheet::rules = new ArrayList<CSSRule>();
    $stylesheet::namespaces = new ArrayList<CSSNamespaceDefinition>();
    $stylesheet::fontFaces = new ArrayList<CSSFontFace>();
    $stylesheet::keyFrames = new ArrayList<CSSKeyFrames>();
}
@after 
{
    model = new CSSDocument($stylesheet::rules, 
                            $stylesheet::namespaces, 
                            $stylesheet::fontFaces,
                            $start,
                            tokenStream);
}
    :   ( namespaceStatement | fontFace | keyframes | mediaQuery | ruleset )*
    ;

namespaceStatement
@after        
{ 
    final CSSNamespaceDefinition ns = new CSSNamespaceDefinition(
            $id.text, $uri.text, $start, tokenStream);
    $stylesheet::namespaces.add(ns); 
}
    :   ^(AT_NAMESPACE id=ID? uri=STRING)
    ;
  
keyframes
    :   ^(AT_KEYFRAMES id=ID ruleset*)
         { $stylesheet::keyFrames.add(new CSSKeyFrames($id.text, CSSModelTreeType.KEYFRAMES, $start, tokenStream)); }
    |   ^(AT_WEBKIT_KEYFRAMES id=ID ruleset*)
         { $stylesheet::keyFrames.add(new CSSKeyFrames($id.text, CSSModelTreeType.KEYFRAMES_WEBKIT, $start, tokenStream)); }
    ;
  

mediaQuery
scope 
{ 
    // media query condition clauses are buffered in this list
    List<CSSMediaQueryCondition> conditions 
}
@init 
{ 
    $mediaQuery::conditions = new ArrayList<CSSMediaQueryCondition>(); 
}
    :   ^(AT_MEDIA medium ruleset*)
    ;
  
medium 
    :   ^(I_MEDIUM_CONDITIONS mediumCondition*)
    ;
  
mediumCondition
    :   ID 
    { 
        $mediaQuery::conditions.add(new CSSMediaQueryCondition($start, tokenStream)); 
    } 
    | ONLY id=ID 
    { 
        $mediaQuery::conditions.add(new CSSMediaQueryCondition($start, tokenStream)); 
        $mediaQuery::conditions.add(new CSSMediaQueryCondition($id, tokenStream)); 
    } 
    | ARGUMENTS
    { 
        $mediaQuery::conditions.add(new CSSMediaQueryCondition($start, tokenStream)); 
    } 
    | COMMA
    { 
        $mediaQuery::conditions.add(new CSSMediaQueryCondition($start, tokenStream)); 
    } 
    ;
    
fontFace
@after
{
    final CSSFontFace fontFace = new CSSFontFace($d.properties, $start, tokenStream);
    $stylesheet::fontFaces.add(fontFace);
}
    :   ^(AT_FONT_FACE d=declarationsBlock)
    ;
  
ruleset
scope 
{
    // list of subject selectors
    List<CSSSelector> subjects
}
@init 
{
    $ruleset::subjects = new ArrayList<CSSSelector>();
}
@after 
{
    final List<CSSMediaQueryCondition> mediaQueryConditions;
    if ($mediaQuery.isEmpty())
        mediaQueryConditions = null;
    else
        mediaQueryConditions = $mediaQuery::conditions;
    
    final CSSRule cssRule = new CSSRule(
            mediaQueryConditions,
            $ruleset::subjects,
            $d.properties, 
            $start, 
            tokenStream);
    $stylesheet::rules.add(cssRule);
}
    :   ^(I_RULE selectorGroup d=declarationsBlock)
    ;

selectorGroup
    :  ^(I_SELECTOR_GROUP compoundSelector+)
    ;    

compoundSelector
@init
{
    final Stack<CSSSelector> simpleSelectorStack = new Stack<CSSSelector>();
}
@after
{
    $ruleset::subjects.add(simpleSelectorStack.peek());
}
    :   ^(I_SELECTOR firstSelector[simpleSelectorStack] moreSelectors[simpleSelectorStack]*)   
    ;
    
moreSelectors [Stack<CSSSelector> simpleSelectorStack]
    :   ^(I_CHILD_SELECTOR simpleSelector[simpleSelectorStack, CombinatorType.CHILD])
    |   ^(I_PRECEDED_SELECTOR simpleSelector[simpleSelectorStack, CombinatorType.PRECEDED])
    |   ^(I_SIBLING_SELECTOR simpleSelector[simpleSelectorStack, CombinatorType.SIBLING])
    |   ^(I_SIMPLE_SELECTOR simpleSelector[simpleSelectorStack, CombinatorType.DESCENDANT]) 
    ;

firstSelector [Stack<CSSSelector> simpleSelectorStack]
    :   ^(I_SIMPLE_SELECTOR simpleSelector[simpleSelectorStack, CombinatorType.DESCENDANT])
    ;

simpleSelector [Stack<CSSSelector> simpleSelectorStack, CombinatorType combinatorType]
scope
{
    String namespace;
    String element;
    List<CSSSelectorCondition> conditions;
}
@init
{
    $simpleSelector::conditions = new ArrayList<CSSSelectorCondition>();
    final CSSCombinator combinator ;
    if (simpleSelectorStack.isEmpty())
        combinator = null;
    else                    
        combinator = new CSSCombinator(simpleSelectorStack.peek(), combinatorType, $start, tokenStream);
}
@after
{
    final CSSSelector simpleSelector = new CSSSelector(
        combinator,
        $simpleSelector::element,
        $simpleSelector::namespace,
        $simpleSelector::conditions, 
        $start, 
        tokenStream);
    simpleSelectorStack.push(simpleSelector);
}
    :   simpleSelectorFraction+
    ;    
   
    
simpleSelectorFraction
    :   elementSelector
    |   conditionSelector 
    ;
   
conditionSelector
@init
{
    ConditionType type = null;
    String name = null;
}
@after
{
    $simpleSelector::conditions.add(
        new CSSSelectorCondition(name, type, $start, tokenStream));
}
    :   ^(DOT c=ID)   { type = ConditionType.CLASS; name = $c.text; }  
    |   HASH_WORD   { type = ConditionType.ID; name = $HASH_WORD.text.substring(1); }
    |   ^(COLON NOT arg=ARGUMENTS) { type = ConditionType.NOT; name = $arg.text; }
    |   ^(COLON s=ID) { type = ConditionType.PSEUDO; name = $s.text; } 
    |   ^(DOUBLE_COLON dc=ID) { type = ConditionType.PSEUDO_ELEMENT; name = $dc.text; } 
    |   attributeSelector { type = ConditionType.ATTRIBUTE; name = curAttribute.substring(1); }
    ;
  
elementSelector
    :   ^(PIPE ns=ID e1=ID)  
        { $simpleSelector::element = $e1.text; 
          $simpleSelector::namespace = $ns.text; }
    |   e2=ID             
        { $simpleSelector::element = $e2.text; }
    |   np=NUMBER_WITH_PERCENT             
        { $simpleSelector::element = $np.text; }
    |   STAR           
        { $simpleSelector::element = $STAR.text; }
    ;
    
attributeSelector
    :   open = SQUARE_OPEN attributeName attributeOperator* attributeValue* close = SQUARE_END
	{ curAttribute = $open.text + curAttribute + $close.text; }
    ;
    
attributeName
    :    n1 = ID
         { curAttribute = $n1.text; }
    ;
    
attributeOperator
    :    o1 = BEGINS_WITH
         { curAttribute += $o1.text; }
    |    o2 = ENDS_WITH
         { curAttribute += $o2.text; }
    |    o3 = CONTAINS
         { curAttribute += $o3.text; }
    |    o4 = LIST_MATCH
         { curAttribute += $o4.text; }
    |    o5 = HREFLANG_MATCH
         { curAttribute += $o5.text; }
    |    o6 = EQUALS
         { curAttribute += $o6.text; }
    ;
    
attributeValue
    :    s = STRING
         { curAttribute += $s.text; }
    ;
    	

declarationsBlock returns [List<CSSProperty> properties]
@init 
{
    $properties = new ArrayList<CSSProperty>();
}
    :   ^(I_DECL (declaration 
         { 
             if ($declaration.property != null)
                 $properties.add($declaration.property); 
         }
         )*)
    ;

declaration returns [CSSProperty property]
@after
{
    if ($id.text != null && $v.propertyValue != null)
        $property = new CSSProperty($id.text, $v.propertyValue, $start, tokenStream);  
}
    :   ^(COLON id=ID v=value)
    ;
    
value returns [CSSPropertyValue propertyValue]
    :   ^( I_ARRAY 
                              { final List<CSSPropertyValue> array = new ArrayList<CSSPropertyValue>(); }
           ( s1=multiValue    { array.add($s1.propertyValue); } )+
        )                     { $propertyValue = new CSSArrayPropertyValue(array, $start, tokenStream); }
    |   s2=multiValue         { $propertyValue = $s2.propertyValue; }
    ;    

multiValue returns [CSSPropertyValue propertyValue]
    :   ^( I_MULTIVALUE 
                              { final List<CSSPropertyValue> array = new ArrayList<CSSPropertyValue>(); }
           ( s1=singleValue   { array.add($s1.propertyValue); } )+
        )                     { $propertyValue = new CSSMultiValuePropertyValue(array, $start, tokenStream); }
    |   s2=singleValue        { $propertyValue = $s2.propertyValue; }
    ;
  
singleValue returns [CSSPropertyValue propertyValue]
    :   NUMBER_WITH_PERCENT         
		{ $propertyValue = new CSSNumberPropertyValue($NUMBER_WITH_PERCENT.text, $start, tokenStream); }
    |   NUMBER_WITH_UNIT         
		{ $propertyValue = new CSSNumberPropertyValue($NUMBER_WITH_UNIT.text, $start, tokenStream); }
    |   HASH_WORD         
        { $propertyValue = new CSSColorPropertyValue($start, tokenStream); }
    |   ALPHA_VALUE
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); }
    |   RECT_VALUE
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); }
    |   ROTATE_VALUE
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); }
    |   SCALE_VALUE
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); }
    |   TRANSLATE3D_VALUE
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); }
    |   RGB
    	{ $propertyValue = new CSSRgbColorPropertyValue($RGB.text, $start, tokenStream); }
    |   RGBA
    	{ $propertyValue = new CSSRgbaColorPropertyValue($RGBA.text, $start, tokenStream); }
    |   ^(CLASS_REFERENCE cr=ARGUMENTS)
        { $propertyValue = new CSSFunctionCallPropertyValue($CLASS_REFERENCE.text, $cr.text, $start, tokenStream); }
    |   ^(PROPERTY_REFERENCE pr=ARGUMENTS)
        { $propertyValue = new CSSFunctionCallPropertyValue($PROPERTY_REFERENCE.text, $pr.text, $start, tokenStream); }
    |   ^(EMBED es=ARGUMENTS)
        { $propertyValue = new CSSFunctionCallPropertyValue($EMBED.text, $es.text, $start, tokenStream); }
    |   ^(URL url=ARGUMENTS format=formatOption*)
        { $propertyValue = new CSSURLAndFormatPropertyValue($URL.text, $url.text, $format.text, $start, tokenStream); }
    |   ^(LOCAL l=ARGUMENTS)
        { $propertyValue = new CSSFunctionCallPropertyValue($LOCAL.text, $l.text, $start, tokenStream); }
    |   ^(CALC l=ARGUMENTS)
        { $propertyValue = new CSSFunctionCallPropertyValue($CALC.text, $l.text, $start, tokenStream); }
    |   ^(FUNCTIONS l=ARGUMENTS)
        { $propertyValue = new CSSFunctionCallPropertyValue($FUNCTIONS.text, $l.text, $start, tokenStream); }
    |   s=STRING   
        { $propertyValue = new CSSStringPropertyValue($s.text, $start, tokenStream); }                   
    |   ID
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); } 
    |   OPERATOR
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); } 
    |   IMPORTANT
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); } 
    ;
    
formatOption returns [CSSPropertyValue propertyValue]
    :   ^(FORMAT format=ARGUMENTS)
        { $propertyValue = new CSSFunctionCallPropertyValue($FORMAT.text, $format.text, $start, tokenStream); } 
    ;

argumentList returns [List<String> labels, List<String> values]
@init 
{
    $labels = new ArrayList<String>(3);
    $values = new ArrayList<String>(3);
}
    :   argument[$labels, $values]+
    ;
    
argument [List<String> labels, List<String> values]
@after
{
    // Use null for argument without label.
    $labels.add($l.text);
    $values.add($v.text); 
}
    :   ^(EQUALS l=ID? v=STRING)
    ;
