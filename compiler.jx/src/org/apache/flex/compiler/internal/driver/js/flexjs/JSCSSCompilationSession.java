package org.apache.flex.compiler.internal.driver.js.flexjs;

import org.apache.flex.compiler.css.ICSSDocument;
import org.apache.flex.compiler.css.ICSSRule;
import org.apache.flex.compiler.internal.css.codegen.CSSCompilationSession;

import com.google.common.collect.ImmutableList;

public class JSCSSCompilationSession extends CSSCompilationSession
{

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
}
