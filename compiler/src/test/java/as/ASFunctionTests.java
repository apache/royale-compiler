package as;

import org.junit.Test;

public class ASFunctionTests extends ASFeatureTestsBase {
	
    @Test
    public void testFunctionWithoutBody()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo():void"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "Function does not have a body.\n");
    }
	
    @Test
    public void testParameterHasNoTypeDeclaration()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(bar):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "parameter 'bar' for function 'foo' has no type declaration.\n");
    }
	
    @Test
    public void testRequiredParameterAfterOptional()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(bar:Number = 123.4, baz:String):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "Required parameters are not permitted after optional parameters.\n");
    }
	
    @Test
    public void testRestParameterMustBeLast()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(...rest, bar:String):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "Rest parameters must be last.\n");
    }
	
    @Test
    public void testDuplicateParameterNames()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"function foo(bar:String, bar:Number):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "More than one argument named 'bar' specified for function 'foo'. References to that argument will always resolve to the last one.\n");
    }

    @Test
    public void testDuplicateParameterNames2()
    {
        String[] imports = new String[]
        {
        };
        String[] declarations = new String[]
        {
        };
        String[] testCode = new String[]
        {
        };
        String[] extra = new String[]
        {
			"var foo:Function = function(bar:String, bar:Number):void {}"
        };
        String source = getAS(imports, declarations, testCode, extra);

        String[] options = new String[]
        {
        };
        compileAndExpectErrors(source, false, false, false, options, "More than one argument named 'bar' specified for function ''. References to that argument will always resolve to the last one.\n");
    }
}
