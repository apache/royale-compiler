package org.apache.flex.compiler.internal.driver.js.flexjs;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.css.ICSSMediaQueryCondition;
import org.apache.flex.compiler.css.ICSSProperty;
import org.apache.flex.compiler.css.ICSSPropertyValue;
import org.apache.flex.compiler.css.ICSSRule;
import org.apache.flex.compiler.css.ICSSSelector;
import org.apache.flex.compiler.css.ICSSSelectorCondition;
import org.apache.flex.compiler.internal.css.CSSArrayPropertyValue;
import org.apache.flex.compiler.internal.css.CSSColorPropertyValue;
import org.apache.flex.compiler.internal.css.CSSFunctionCallPropertyValue;
import org.apache.flex.compiler.internal.css.CSSKeywordPropertyValue;
import org.apache.flex.compiler.internal.css.CSSNumberPropertyValue;
import org.apache.flex.compiler.internal.css.CSSRgbColorPropertyValue;
import org.apache.flex.compiler.internal.css.CSSStringPropertyValue;
import org.apache.flex.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.flex.compiler.problems.CSSCodeGenProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.ICompilerProject;

import com.google.common.collect.ImmutableList;

public class JSCSSCompilationSession extends CSSCompilationSession
{

    private ArrayList<String> requires;
    
    public String getEncodedCSS(ICompilerProject project, final Collection<ICompilerProblem> problems)
    {
        final ICSSDocument css = synthesisNormalizedCSS();
        StringBuilder sb = new StringBuilder();
        requires = new ArrayList<String>();
        encodeCSS(css, sb, project, problems);
        sb.append("];\n");
        for (String r : requires)
        {
            sb.append("goog.require('" + r + "');\n");
        }

        return sb.toString();        
    }
    
    public String emitCSS()
    {
        final ICSSDocument css = synthesisNormalizedCSS();
        StringBuilder sb = new StringBuilder();
        walkCSS(css, sb);
        return sb.toString();
    }
    
    private void walkCSS(ICSSDocument css, StringBuilder sb)
    {
        ImmutableList<ICSSRule> rules = css.getRules();
        for (ICSSRule rule : rules)
        {
            sb.append(rule.toString());
            sb.append("\n\n");
        }
    }
    
    private void encodeCSS(ICSSDocument css, StringBuilder sb,
            ICompilerProject project, final Collection<ICompilerProblem> problems)
    {
        ImmutableList<ICSSRule> rules = css.getRules();
        boolean skipcomma = true;
        for (ICSSRule rule : rules)
        {
            String s = encodeRule(rule, project, problems);
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
    
    private String encodeRule(ICSSRule rule,
            ICompilerProject project, final Collection<ICompilerProblem> problems)
    {
        final StringBuilder result = new StringBuilder();

        ImmutableList<ICSSMediaQueryCondition> mqlist = rule.getMediaQueryConditions();
        int n = mqlist.size();
        if (n > 0)
        {
            if (mqlist.get(0).toString().equals("-flex-flash"))
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
                ImmutableList<ICSSSelectorCondition> conds = sel.getConditions();
                for (ICSSSelectorCondition cond : conds)
                    selName += cond.toString();
                result.append("\"" + selName + "\"");
            }
        }
        result.append(",\n");
        
        ImmutableList<ICSSProperty> plist = rule.getProperties();
        result.append(plist.size());
        
        for (final ICSSProperty prop : plist)
        {
            result.append(",\n");
            result.append("\"" + prop.getName() + "\"");
            result.append(",\n");
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
                        result.append("\"" + ((CSSStringPropertyValue)val).getValue() + "\"");
                    }
                    else if (val instanceof CSSColorPropertyValue)
                    {
                        result.append(new Integer(((CSSColorPropertyValue)val).getColorAsInt()));
                    }
                    else if (val instanceof CSSRgbColorPropertyValue)
                    {
                        result.append(new Integer(((CSSRgbColorPropertyValue)val).getColorAsInt()));
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
                        result.append(className);
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
                else if ("Embed".equals(functionCall.name))
                {
                    final ICompilerProblem e = new CSSCodeGenProblem(
                            new IllegalStateException("Unable to find compilation unit for " + functionCall));
                    problems.add(e);
                }
                else
                {
                    assert false : "CSS parser bug: unexpected function call property value: " + functionCall;
                    throw new IllegalStateException("Unexpected function call property value: " + functionCall);
                }
            }
        }

        return result.toString();

    }
}
