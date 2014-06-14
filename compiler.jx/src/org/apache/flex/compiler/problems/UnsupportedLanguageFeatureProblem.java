package org.apache.flex.compiler.problems;

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.internal.parsing.as.ASToken;
import org.apache.flex.compiler.problems.annotations.ProblemClassification;

@ProblemClassification(CompilerProblemClassification.SYNTAX_ERROR)
public class UnsupportedLanguageFeatureProblem extends CodegenProblem
{

    public static final String DESCRIPTION =
        "'${tokenText}' cannot be cross-compiled.";
    
    public UnsupportedLanguageFeatureProblem(ISourceLocation site, String text)
    {
        super(site);
        tokenText = text;
    }
    
    public UnsupportedLanguageFeatureProblem(ASToken site, String text) 
    {
        super(site);
        tokenText = text;
    }
    
    public UnsupportedLanguageFeatureProblem(ASToken token)
    {
        this(token, token.getText());
    }
    
    public final String tokenText;
}
