package org.apache.flex.compiler.internal.js.driver.goog;

import com.google.javascript.jscomp.CommandLineRunner;

public class JSGoogClosureCLR extends CommandLineRunner
{
    JSGoogClosureCLR(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        JSGoogClosureCLR runner = new JSGoogClosureCLR(args);

        if (runner.shouldRunCompiler())
        {
            runner.run();
        }
        else
        {
            System.exit(-1);
        }
    }
}