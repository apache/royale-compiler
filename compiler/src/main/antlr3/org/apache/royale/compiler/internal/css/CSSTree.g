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

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.css.*;
import org.apache.royale.compiler.problems.CSSParserProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.CSSStrictFlexSyntaxProblem;
import org.apache.royale.compiler.problems.CSSUnknownFunctionProblem;
import org.apache.royale.compiler.problems.CSSUnknownPseudoClassProblem;

}

@members 
{
protected static final List<String> STRICT_FUNCTIONS = Arrays.asList("ClassReference", "PropertyReference", "Embed", "url", "local");

protected static final List<String> KNOWN_FUNCTIONS = Arrays.asList(
    // special Flex functions
    "ClassReference",
    "PropertyReference",
    "Embed",

    // vendor-prefixed browser functions
    "-moz-linear-gradient",
    "-webkit-linear-gradient",

    // regular browser functions
    "acos",
    "asin",
    "atan",
    "atan2",
    "blur",
    "brightness",
    "calc",
    "circle",
    "clamp",
    "color",
    "color-mix",
    "conic-gradient",
    "contrast",
    "cos",
    "counter",
    "counters",
    "cubic-bezier",
    "drop-shadow",
    "ellipse",
    "env",
    "exp",
    "grayscale",
    "hsl",
    "hue-rotate",
    "hwb",
    "hypot",
    "image-set",
    "inset",
    "invert",
    "lab",
    "layer",
    "lch",
    "light-dark",
    "linear",
    "linear-gradient",
    "local",
    "log",
    "matrix",
    "matrix3d",
    "max",
    "min",
    "minmax",
    "mod",
    "oklab",
    "oklch",
    "opacity",
    "path",
    "perspective",
    "polygon",
    "pow",
    "radial-gradient",
    "ray",
    "rect",
    "rem",
    "repeat",
    "repeating-conic-gradient",
    "repeating-linear-gradient",
    "repeating-radial-gradient",
    "rgb",
    "rgba",
    "rotate",
    "rotate3d",
    "rotateX",
    "rotateY",
    "rotateZ",
    "round",
    "saturate",
    "scale",
    "scale3d",
    "scaleX",
    "scaleY",
    "scaleZ",
    "sepia",
    "sign",
    "skew",
    "skewX",
    "skewY",
    "sqrt",
    "steps",
    "tan",
    "translate",
    "translate3d",
    "translateX",
    "translateY",
    "translateZ",
    "url",
    "var",
    "xywh"
);

protected static final List<String> KNOWN_PSEUDO_CLASS_FUNCTIONS = Arrays.asList(
    "dir",
    "has",
    "host",
    "is",
    "lang",
    "not",
    "nth-child",
    "nth-last-child",
    "nth-last-of-type",
    "nth-of-type",
    "state",
    "where"
);

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
protected List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

/**
 * Used for building up attribute selector strings until we implement a data
 * structure for it.
 */
protected String curAttribute;

/**
 * Determines if problems should be reported for CSS syntax that would not be
 * recognized by the Flex SDK compiler.
 */
protected boolean strictFlexCSS = false;

public boolean getStrictFlexCSS()
{
    return strictFlexCSS;
}

public void setStrictFlexCSS(boolean value)
{
    strictFlexCSS = value;
}


/**
 * Collect problems.
 */
@Override
public void displayRecognitionError(String[] tokenNames, RecognitionException e)
{
    problems.add(CSSParserProblem.create(this, tokenNames, e));
}


/**
 * Collect strict Flex CSS problems.
 */
public void displayStrictFlexSyntaxError(String syntax, CommonTree tree)
{
    final ISourceLocation location = new SourceLocation(
        getSourceName(),
        -1, -1, // TODO Need start and end info from CSS
        tree.getLine(), tree.getCharPositionInLine());
    problems.add(new CSSStrictFlexSyntaxProblem(location, syntax));
}

public void displayUnknownPseudoClassError(String pseudoClassName, CommonTree tree)
{
    final ISourceLocation location = new SourceLocation(
        getSourceName(),
        -1, -1, // TODO Need start and end info from CSS
        tree.getLine(), tree.getCharPositionInLine());
    problems.add(new CSSUnknownPseudoClassProblem(location, pseudoClassName));
}

public void displayUnknownFunctionError(String functionName, CommonTree tree)
{
    final ISourceLocation location = new SourceLocation(
        getSourceName(),
        -1, -1, // TODO Need start and end info from CSS
        tree.getLine(), tree.getCharPositionInLine());
    problems.add(new CSSUnknownFunctionProblem(location, functionName));
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
    if (ns.getProblems().size() == 0)
    {
        $stylesheet::namespaces.add(ns); 
    }
    else
    {
        problems.addAll(ns.getProblems());
    }
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
    List<CSSProperty> properties = $d.properties;
    if (properties == null)
    {
        properties = new ArrayList<CSSProperty>();
    }
    final CSSFontFace fontFace = new CSSFontFace(properties, $start, tokenStream);
    if (fontFace.getProblems().size() == 0)
    {
        $stylesheet::fontFaces.add(fontFace);
    }
    else
    {
        problems.addAll(fontFace.getProblems());
    }
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
    if (strictFlexCSS && !CombinatorType.DESCENDANT.equals(combinatorType))
    {
        // Flex supported only the DESCENDANT combinator type
        problems.add(new CSSStrictFlexSyntaxProblem(combinator, combinatorType.text));
    }
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
    String arguments = null;
}
@after
{
    $simpleSelector::conditions.add(
        new CSSSelectorCondition(name, type, arguments, $start, tokenStream));
}
    :   ^(DOT c=ID)   { type = ConditionType.CLASS; name = $c.text; }  
    |   HASH_WORD   { type = ConditionType.ID; name = $HASH_WORD.text.substring(1); }
    |   ^(COLON s=ID arg=ARGUMENTS)
        {
            if (strictFlexCSS)
            {
                // Flex didn't support the CSS pseudo-class functions
                displayStrictFlexSyntaxError($COLON.text + $s.text, $s);
            }
            if (!KNOWN_PSEUDO_CLASS_FUNCTIONS.contains($s.text))
            {
                displayUnknownPseudoClassError($s.text, $s);
            }
            type = ConditionType.PSEUDO;
            name = $s.text;
            arguments = $arg.text;
        }
    |   ^(COLON s=ID) { type = ConditionType.PSEUDO; name = $s.text; } 
    |   ^(DOUBLE_COLON dc=ID)
        {
            if (strictFlexCSS)
            {
                // Flex didn't support CSS pseudo elements (but did support non-function pseudo-classes)
                displayStrictFlexSyntaxError($DOUBLE_COLON.text, $DOUBLE_COLON);
            }
            type = ConditionType.PSEUDO_ELEMENT;
            name = $dc.text;
        } 
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

/**
 * Matches an attribute selector.
 *
 * Must starts with an opening square bracket and end with a closing square
 * bracket. Inside the brackets, it must start with attribute name. An operator
 * and value may optionally follow, but if either exists, both must exist.
 */
attributeSelector
    :   open = SQUARE_OPEN attributeName (attributeOperator attributeValue)? close = SQUARE_END
        {
            if (strictFlexCSS)
            {
                // Flex didn't support CSS attributes
                displayStrictFlexSyntaxError($SQUARE_OPEN.text, $SQUARE_OPEN);
            }
            curAttribute = $open.text + curAttribute + $close.text;
        }
    ;

/**
 * Matches an attribute name within an attribute selector.
 */
attributeName
    :    n1 = ID
         { curAttribute = $n1.text; }
    ;

/**
 * Matches an operator within an attribute selector.
 *
 * Immediately follows the attribute name.
 */
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


/**
 * Matches a value within an attribute selector.
 *
 * Immediately follows the attribute operator.
 */ 
attributeValue
    :    s = STRING
         { curAttribute += $s.text; }
    |    s1 = ID
         { curAttribute = $s1.text; }
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
    if (strictFlexCSS && $id.text.startsWith("--"))
    {
        // Flex didn't support CSS custom properties (CSS variables)
        displayStrictFlexSyntaxError($id.text, $id);
    }
    if ($id.text != null && $v.propertyValue != null)
        $property = new CSSProperty($id.text, $v.propertyValue, $start, tokenStream);  
}
    :   ^(COLON id=ID v=value)
    |   ^(COLON id=DASHED_ID v=value)
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
		{
            CSSNumberPropertyValue numWithPercentValue = new CSSNumberPropertyValue($NUMBER_WITH_PERCENT.text, $start, tokenStream);
            problems.addAll(numWithPercentValue.getProblems());
            $propertyValue = numWithPercentValue;
        }
    |   NUMBER_WITH_UNIT         
		{
            CSSNumberPropertyValue numWithUnitValue = new CSSNumberPropertyValue($NUMBER_WITH_UNIT.text, $start, tokenStream);
            problems.addAll(numWithUnitValue.getProblems());
            $propertyValue = numWithUnitValue;
        }
    |   HASH_WORD         
        {
            CSSColorPropertyValue colorValue = new CSSColorPropertyValue($start, tokenStream);
            problems.addAll(colorValue.getProblems());
            $propertyValue = colorValue;
        }
    |   ALPHA_VALUE
        {
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($ALPHA_VALUE.text, $ALPHA_VALUE);
            }
            $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream);
        }
    |   RECT_VALUE
        { $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream); }
    |   ROTATE_VALUE
        {
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($ROTATE_VALUE.text, $ROTATE_VALUE);
            }
            $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream);
        }
    |   SCALE_VALUE
        {
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($SCALE_VALUE.text, $SCALE_VALUE);
            }
            $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream);
        }
    |   TRANSLATE3D_VALUE
        {
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($TRANSLATE3D_VALUE.text, $TRANSLATE3D_VALUE);
            }
            $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream);
        }
    |   MATRIX_VALUE
        {
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($MATRIX_VALUE.text, $MATRIX_VALUE);
            }
            $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream);
        }
    |   MATRIX3D_VALUE
        {
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($MATRIX3D_VALUE.text, $MATRIX3D_VALUE);
            }
            $propertyValue = CSSKeywordPropertyValue.create($start, tokenStream);
        }
    |   RGB
    	{ $propertyValue = new CSSRgbColorPropertyValue($RGB.text, $start, tokenStream); }
    |   RGBA
    	{
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($RGBA.text, $RGBA);
            }
            $propertyValue = new CSSRgbaColorPropertyValue($RGBA.text, $start, tokenStream);
        }
    |   ^(URL url=ARGUMENTS format=formatOption*)
        { $propertyValue = new CSSURLAndFormatPropertyValue($URL.text, $url.text, $format.text, $start, tokenStream); }
    |   ^(id=ID args=ARGUMENTS)
        {
            if (strictFlexCSS && !STRICT_FUNCTIONS.contains($id.text))
            {
                displayStrictFlexSyntaxError($id.text, $id);
            }
            else if (!KNOWN_FUNCTIONS.contains($id.text))
            {
                displayUnknownFunctionError($id.text, $id);
            }
            $propertyValue = new CSSFunctionCallPropertyValue($id.text, $args.text, $start, tokenStream);
        }
    |   ^(FUNCTIONS l=ARGUMENTS)
        {
            if (strictFlexCSS)
            {
                displayStrictFlexSyntaxError($FUNCTIONS.text, $FUNCTIONS);
            }
            $propertyValue = new CSSFunctionCallPropertyValue($FUNCTIONS.text, $l.text, $start, tokenStream);
        }
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
