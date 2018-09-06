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
package org.apache.royale.compiler.internal.driver.js.royale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSMediaQueryCondition;
import org.apache.royale.compiler.css.ICSSProperty;
import org.apache.royale.compiler.css.ICSSPropertyValue;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.css.ICSSSelector;
import org.apache.royale.compiler.css.ICSSSelectorCondition;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.royale.compiler.internal.css.*;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class JSCSSCompilationSession extends CSSCompilationSession
{
	private List<String> otherCSSFunctions = Arrays.asList(
			 "-moz-linear-gradient",
	         "-webkit-linear-gradient",
	         "linear-gradient",
	         "progid:DXImageTransform.Microsoft.gradient",
	         "translateX",
	         "translateY",
	         "translate",
             "blur",
             "brightness",
             "contrast",
             "drop-shadow",
             "hue-rotate",
             "invert",
             "saturate",
             "sepia"
    );
    //  this two conflicts with other css functions with the same name - comment for now
    //  "grayscale",
    //  "opacity",

    private ArrayList<String> requires;
    
    public String getEncodedCSS()
    {
        final ICSSDocument css = synthesisNormalizedCSS(false);
        StringBuilder sb = new StringBuilder();
        requires = new ArrayList<String>();
        encodeCSS(css, sb);
        if (sb.length() == 0)
        	return null;
        sb.append("];\n");
        for (String r : requires)
        {
            sb.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken() + "('" + formatQualifiedName(r) + "');\n");
        }

        return sb.toString();        
    }
    
    public String emitCSS()
    {
        final ICSSDocument css = synthesisNormalizedCSS(false);
        StringBuilder sb = new StringBuilder();
        sb.append("/* Generated by Apache Royale Compiler */\n");
        walkCSS(css, sb);
        return sb.toString();
    }

    /**
     * used to minify the CSS for release mode
     */
    public static String minifyCSSString(String cssString)
    {
        //Remove empty selectors
        cssString = cssString.replaceAll("\\S+\\s?\\{[\\s\\n]+}", "");
        // Remove comments
        cssString = cssString.replaceAll("/\\*[\\d\\D]*?\\*/", "");
        cssString = cssString.replace(";}", "}");
        cssString = cssString.replaceAll("[\\n\\r]+\\s*", "");
        cssString = cssString.replaceAll("\\s+", " ");
        cssString = cssString.replaceAll("\\s?([:,;{}])\\s?", "$1");
        cssString = cssString.replaceAll("([\\s:]0)(px|pt|%|em)", "$1");

        return cssString;
    }
    
    private String fontFaceToString(CSSFontFace fontFace)
    {
        final StringBuilder result = new StringBuilder();
        result.append("@font-face {\n");
        result.append("    ");
        result.append("font-family: ");
        result.append(fontFace.getFontFamily() + ";\n");
        result.append("    ");
        result.append("font-style: ");
        result.append(fontFace.getFontStyle() + ";\n");
        result.append("    ");
        result.append("font-weight: ");
        result.append(fontFace.getFontStyle() + ";\n");
        result.append("    ");
        ArrayList<ICSSPropertyValue> sources = fontFace.getSources();
        for (ICSSPropertyValue src : sources)
        {
        	result.append("src: ");
        	result.append(src.toString() + ";\n");
        }
       	result.append("}\n");
        return result.toString();
    }
    
    private String cssRuleToString(ICSSRule rule)
    {
        final StringBuilder result = new StringBuilder();

        ImmutableList<ICSSMediaQueryCondition> mqList = rule.getMediaQueryConditions();
        boolean hasMediaQuery = !mqList.isEmpty();
        if (hasMediaQuery)
        {
            result.append("@media ");
            result.append(Joiner.on(" and ").join(rule.getMediaQueryConditions()));
            result.append(" {\n");
            result.append("    ");
        }

        ImmutableList<ICSSSelector> selectors = rule.getSelectorGroup();
        boolean firstOne = true;
        for (ICSSSelector selector : selectors)
        {
        	String s = selector.toString();
	        // add "." to type selectors that don't map cleanly
	        // to CSS type selectors to convert them to class
	    	// selectors.
	        if (!s.startsWith(".") && !s.startsWith("*") && !s.startsWith("#") && !s.startsWith("::"))
	        {
	        	String condition = null;
        		int colon = s.indexOf(":");
	        	if (colon != -1)
	        	{
	        		condition = s.substring(colon);
	        		s = s.substring(0, colon);
	        	}
	        	else
	        	{
	        		int brace = s.indexOf("[");
	        		if (brace != -1)
	        		{
		        		condition = s.substring(brace);
		        		s = s.substring(0, brace);	        			
	        		}
	        		else
	        		{
	        			int child = s.indexOf(">");
	        			if (child != -1)
	        			{
			        		condition = s.substring(child);
			        		s = s.substring(0, child);	        			
	        			}
	        			else
	        			{
	        				int preceded = s.indexOf("+");
		        			if (preceded != -1)
		        			{
				        		condition = s.substring(preceded);
				        		s = s.substring(0, preceded);	        			
		        			}
	        			}
	        		}
	        	}
	        	if (!htmlElementNames.contains(s.toLowerCase()))
	        	{
	        		if (s.indexOf(" ") > 0)
	        		{
	        			String parts[] = s.split(" ");
	        			int n = parts.length;
	        			s = "";
	        			for (int i = 0; i < n; i++)
	        			{
	        				if (i != 0)
	        					s += " ";
	        				String part = parts[i];
	        				if (!part.startsWith(".") && !part.startsWith("*") && !part.startsWith("#") && !part.startsWith("::"))
	        				{
	        					int pipe = part.indexOf("|");
				        		if (pipe != -1)
				        			part = part.substring(pipe + 1);
				        		part = "." + part;
	        				}
			        		s += part;
	        			}
	        		}
	        		else
	        		{
		        		int pipe = s.indexOf("|");
		        		if (pipe != -1)
		        			s = s.substring(pipe + 1);
		        		s = "." + s;
	        		}
	        	}
	        	if (condition != null)
	        		s = s + condition;
	        }
	        if (!firstOne)
	        	result.append(",\n");
	        result.append(s);
        }

        result.append(" {\n");
        for (final ICSSProperty prop : rule.getProperties())
        {
            if (!hasMediaQuery)
                result.append("    ");

            String propString = ((CSSProperty)prop).toCSSString();
            // skip class references since the won't work in CSS
            if (propString.contains("ClassReference"))
            	continue;
            result.append("    ").append(propString).append("\n");
        }
        if (hasMediaQuery)
            result.append("    }\n");

        result.append("}\n");

        return result.toString();
    }
    
    private void walkCSS(ICSSDocument css, StringBuilder sb)
    {
    	for (CSSFontFace fontFace : fontFaces)
    	{
    		sb.append(fontFaceToString(fontFace));
    	}
    	if (fontFaces.size() > 0)
    		sb.append("\n\n");
        ImmutableList<ICSSRule> rules = css.getRules();
        for (ICSSRule rule : rules)
        {
        	String s = cssRuleToString(rule);
        	if (s.startsWith("@media -royale-swf"))
        		continue;
            sb.append(s);
            sb.append("\n\n");
        }
    }
    
    private void encodeCSS(ICSSDocument css, StringBuilder sb)
    {
        ImmutableList<ICSSRule> rules = css.getRules();
        boolean skipcomma = true;
        for (ICSSRule rule : rules)
        {
            String s = encodeRule(rule);
            if (s != null)
            {
                if (skipcomma)
                    skipcomma = false;
                else
                    sb.append(",\n");
                sb.append(s);
            }
        }
    }
    
    List<String> htmlElementNames = Arrays.asList(
        "a",
        "aside",
        "b",
        "br",
        "body",
        "button",
        "caption",
        "code",
        "col",
        "colgroup",
        "dialog",
        "div",
        "em",
        "embed",
        "font",
        "form",
        "h1",
        "h2",
        "h3",
        "h4",
        "h5",
        "h6",
        "header",
        "hr",
        "html",
        "i",
        "img",
        "input",
        "label",
        "li",
        "main",
        /*"menu", not cross-browser, and we want to use it in Royale */
        "nav",
        "ol",
        "option",
        "p",
        "pre",
        "s",
        "select",
        "small",
        "span",
        "strong",
        "table",
        "tbody",
        "td",
        "textarea",
        "tfoot",
        "th",
        "thead",
        "tr",
        "u",
        "ul"
    );
    
    private String escapeDoubleQuotes(String s)
    {
    	if (s.contains("\""))
    		s = s.replace("\"", "\\\"");
    	return s;
    }
    
    private String encodeRule(ICSSRule rule)
    {
        final StringBuilder result = new StringBuilder();

        ImmutableList<ICSSMediaQueryCondition> mqlist = rule.getMediaQueryConditions();
        int n = mqlist.size();
        if (n > 0)
        {
            if (mqlist.get(0).toString().equals("-royale-swf"))
                return null;
            
            result.append(n);
            
            for (ICSSMediaQueryCondition mqcond : mqlist)
            {
                result.append(",\n");
                result.append("\"" + mqcond.toString() + "\"");
            }
        }
        else
            result.append(n);

        result.append(",\n");

        ImmutableList<ICSSSelector> slist = rule.getSelectorGroup();
        result.append(slist.size());

        for (ICSSSelector sel : slist)
        {
            result.append(",\n");
            String selName = this.resolvedSelectors.get(sel);
            if (selName == null || selName.equals("null"))
                result.append("\"" + sel.toString() + "\"");
            else
            {
            	selName = formatQualifiedName(selName);
                ImmutableList<ICSSSelectorCondition> conds = sel.getConditions();
                for (ICSSSelectorCondition cond : conds)
                {
                	String condString = escapeDoubleQuotes(cond.toString());
                    selName += condString;
                }
                result.append("\"" + selName + "\"");
            }
        }
        result.append(",\n");
        result.append("function() {");
        
        ImmutableList<ICSSProperty> plist = rule.getProperties();
        
        boolean firstProp = true;
        for (final ICSSProperty prop : plist)
        {
        	if (!firstProp)
        		result.append(";\n");
        	firstProp = false;
            result.append("this[\"" + prop.getName() + "\"] = ");
            ICSSPropertyValue value = prop.getValue();
            if (value instanceof CSSArrayPropertyValue)
            {
                ImmutableList<? extends ICSSPropertyValue> values = ((CSSArrayPropertyValue)value).getElements();
                result.append("[");
                boolean firstone = true;
                for (ICSSPropertyValue val : values)
                {
                    if (firstone)
                        firstone = false;
                    else
                        result.append(", ");
                    if (val instanceof CSSStringPropertyValue)
                    {
                        result.append("\"" + escapeDoubleQuotes(((CSSStringPropertyValue)val).getValue()) + "\"");
                    }
                    else if (val instanceof CSSColorPropertyValue)
                    {
                        result.append(new Integer(((CSSColorPropertyValue)val).getColorAsInt()));
                    }
                    else if (val instanceof CSSRgbColorPropertyValue)
                    {
                        result.append(new Integer(((CSSRgbColorPropertyValue)val).getColorAsInt()));
                    }
                    else if (value instanceof CSSRgbaColorPropertyValue)
                    {
                        //todo: handle alpha in the RGBA ?
                        result.append(new Long(((CSSRgbaColorPropertyValue)value).getColorAsLong()));
                    }
                    else if (val instanceof CSSKeywordPropertyValue)
                    {
                        CSSKeywordPropertyValue keywordValue = (CSSKeywordPropertyValue)val;
                        String keywordString = keywordValue.getKeyword();
                        if (IASLanguageConstants.TRUE.equals(keywordString))
                            result.append("true");
                        else if (IASLanguageConstants.FALSE.equals(keywordString))
                            result.append("false");
                        else
                            result.append("\"" + ((CSSKeywordPropertyValue)val).getKeyword() + "\"");
                    }
                    else if (val instanceof CSSNumberPropertyValue)
                    {
                        result.append(new Double(((CSSNumberPropertyValue)val).getNumber().doubleValue()));
                    }
                    else if (val instanceof CSSURLAndFormatPropertyValue)
                    {
                        result.append("\"" + escapeDoubleQuotes(((CSSURLAndFormatPropertyValue)val).toString()) + "\"");
                    }
                    else if (val instanceof CSSMultiValuePropertyValue)
                    {
                        result.append("\"" + ((CSSMultiValuePropertyValue)val).toString() + "\"");
                    }
                    else
                    {
                        result.append("unexpected value type: " + val.toString());
                    }
                }
                result.append("]");
            }
            else if (value instanceof CSSMultiValuePropertyValue)
            {
                ImmutableList<? extends ICSSPropertyValue> values = ((CSSMultiValuePropertyValue)value).getElements();
                result.append("[");
                boolean firstone = true;
                for (ICSSPropertyValue val : values)
                {
                    if (firstone)
                        firstone = false;
                    else
                        result.append(", ");
                    if (val instanceof CSSStringPropertyValue)
                    {
                        result.append("\"" + escapeDoubleQuotes(((CSSStringPropertyValue)val).getValue()) + "\"");
                    }
                    else if (val instanceof CSSColorPropertyValue)
                    {
                        result.append(new Integer(((CSSColorPropertyValue)val).getColorAsInt()));
                    }
                    else if (val instanceof CSSRgbColorPropertyValue)
                    {
                        result.append(new Integer(((CSSRgbColorPropertyValue)val).getColorAsInt()));
                    }
                    else if (val instanceof CSSRgbaColorPropertyValue)
                    {
                        //todo: handle alpha in the RGBA ?
                        result.append(new Long(((CSSRgbaColorPropertyValue)val).getColorAsLong()));
                    }
                    else if (val instanceof CSSKeywordPropertyValue)
                    {
                        CSSKeywordPropertyValue keywordValue = (CSSKeywordPropertyValue)val;
                        String keywordString = keywordValue.getKeyword();
                        if (IASLanguageConstants.TRUE.equals(keywordString))
                            result.append("true");
                        else if (IASLanguageConstants.FALSE.equals(keywordString))
                            result.append("false");
                        else
                            result.append("\"" + ((CSSKeywordPropertyValue)val).getKeyword() + "\"");
                    }
                    else if (val instanceof CSSNumberPropertyValue)
                    {
                        result.append(new Double(((CSSNumberPropertyValue)val).getNumber().doubleValue()));
                    }
                    else if (val instanceof CSSURLAndFormatPropertyValue)
                    {
                        result.append("\"" + escapeDoubleQuotes(((CSSURLAndFormatPropertyValue)val).toString()) + "\"");
                    }
                    else if (val instanceof CSSMultiValuePropertyValue)
                    {
                        result.append("\"" + ((CSSMultiValuePropertyValue)val).toString() + "\"");
                    }
                    else
                    {
                        result.append("unexpected value type: " + val.toString());
                    }
                }
                result.append("]");
            }
            else if (value instanceof CSSStringPropertyValue)
            {
                result.append("\"" + ((CSSStringPropertyValue)value).getValue() + "\"");
            }
            else if (value instanceof CSSColorPropertyValue)
            {
                result.append(new Integer(((CSSColorPropertyValue)value).getColorAsInt()));
            }
            else if (value instanceof CSSRgbColorPropertyValue)
            {
                result.append(new Integer(((CSSRgbColorPropertyValue)value).getColorAsInt()));
            }
            else if (value instanceof CSSRgbaColorPropertyValue)
            {
                //todo: handle alpha in the RGBA ?
                result.append(new Long(((CSSRgbaColorPropertyValue)value).getColorAsLong()));
            }
            else if (value instanceof CSSKeywordPropertyValue)
            {
                CSSKeywordPropertyValue keywordValue = (CSSKeywordPropertyValue)value;
                String keywordString = keywordValue.getKeyword();
                if (IASLanguageConstants.TRUE.equals(keywordString))
                    result.append("true");
                else if (IASLanguageConstants.FALSE.equals(keywordString))
                    result.append("false");
                else
                    result.append("\"" + ((CSSKeywordPropertyValue)value).getKeyword() + "\"");
            }
            else if (value instanceof CSSNumberPropertyValue)
            {
                result.append(new Double(((CSSNumberPropertyValue)value).getNumber().doubleValue()));
            }
            else if (value instanceof CSSFunctionCallPropertyValue)
            {
                final CSSFunctionCallPropertyValue functionCall = (CSSFunctionCallPropertyValue)value;
                if ("ClassReference".equals(functionCall.name))
                {
                    final String className = CSSFunctionCallPropertyValue.getSingleArgumentFromRaw(functionCall.rawArguments);
                    if ("null".equals(className))
                    {
                        // ClassReference(null) resets the property's class reference.
                        result.append("null");
                    }
                    else
                    {
                        result.append(formatQualifiedName(className));
                        requires.add(className);
                    }
                }
                else if ("url".equals(functionCall.name))
                {
                    final String urlString = CSSFunctionCallPropertyValue.getSingleArgumentFromRaw(functionCall.rawArguments);
                    result.append("\"" + urlString + "\"");
                }
                else if ("PropertyReference".equals(functionCall.name))
                {
                    // TODO: implement me
                }
                else if ("calc".equals(functionCall.name))
                {
                    // TODO: implement me
                	result.append("null");
                }
                else if ("Embed".equals(functionCall.name))
                {
                    // TODO: implement me
                    /*
                    final ICompilerProblem e = new CSSCodeGenProblem(
                            new IllegalStateException("Unable to find compilation unit for " + functionCall));
                    problems.add(e);
                    */
                }
                else if (otherCSSFunctions.contains(functionCall.name))
                {
                	// ignore for now?
                	result.append("null");
                }
                else
                {
                    assert false : "CSS parser bug: unexpected function call property value: " + functionCall;
                    throw new IllegalStateException("Unexpected function call property value: " + functionCall);
                }
            }
        }
        result.append("}");

        return result.toString();

    }
    
    @Override
    protected boolean keepRule(ICSSRule newRule)
    {
    	if (super.keepRule(newRule))
    		return true;
    	    	
    	// might need to loop over all selectors in selector group
    	if (newRule.getSelectorGroup().size() > 0)
    	{
    		ICSSSelector selector = newRule.getSelectorGroup().get(0);
	    	String elementName = selector.getElementName();
	    	if (elementName != null)
	    	{
	    		if (htmlElementNames.contains(elementName))
	    			return true;
	    	}
	    	else
	    	{
	    		return true;
	    	}
    	}
        return false;
    }

    private String formatQualifiedName(String name)
    {
    	/*
    	if (name.contains("goog.") || name.startsWith("Vector."))
    		return name;
    	if (name.startsWith("."))
    	{
    		return "." + name.substring(1).replaceAll("\\.", "_");
    	}
    	name = name.replaceAll("\\.", "_");
    	*/
    	return name;
    }

}
