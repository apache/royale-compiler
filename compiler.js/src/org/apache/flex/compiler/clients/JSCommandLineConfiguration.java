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

package org.apache.flex.compiler.clients;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.ConfigurationValue;
import org.apache.flex.compiler.exceptions.ConfigurationException.CannotOpen;
import org.apache.flex.compiler.internal.as.codegen.JSSharedData;
import org.apache.flex.compiler.internal.config.annotations.Arguments;
import org.apache.flex.compiler.internal.config.annotations.Config;
import org.apache.flex.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.flex.compiler.internal.config.annotations.Mapping;

import com.google.common.collect.ImmutableList;

public class JSCommandLineConfiguration extends Configuration
{
    //
    // js-builtin
    //

    public String getJsBuiltin()
    {
        return JSSharedData.BUILT_IN;
    }

    @Config(advanced = true)
    @Mapping("js-builtin")
    @Arguments("name")
    public void setJsBuiltin(ConfigurationValue cv, String name)
    {
        JSSharedData.BUILT_IN = name;
    }

    //
    // js-framework
    //

    public String getJsFramework()
    {
        return JSSharedData.FRAMEWORK_CLASS;
    }

    @Config(advanced = true)
    @Mapping("js-framework")
    @Arguments("name")
    public void setJsFramework(ConfigurationValue cv, String name)
    {
        if (name.equals("jquery"))
        {
            JSSharedData.FRAMEWORK_CLASS = "browser.JQueryFramework";
        }
        else if (name.equals("goog") || name.equals("closure") || name.equals("google"))
        {
            JSSharedData.FRAMEWORK_CLASS = "browser.ClosureFramework";
        }
        else if (name.equals("none"))
        {
            JSSharedData.FRAMEWORK_CLASS = "browser.NoFramework";
        }
        else
        {
            JSSharedData.FRAMEWORK_CLASS = name;
        }
    }

    //
    // js-main
    //

    public String getJsMain()
    {
        return JSSharedData.MAIN;
    }

    @Config(advanced = true)
    @Mapping("js-main")
    @Arguments("name")
    public void setJsMain(ConfigurationValue cv, String name)
    {
        JSSharedData.MAIN = name;
    }

    //
    // js-no-exports
    //

    public String getJsNoExports()
    {
        return JSSharedData.NO_EXPORTS ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-no-exports")
    @Arguments("value")
    public void setJsNoExports(ConfigurationValue cv, String value)
    {
        JSSharedData.NO_EXPORTS = value.equals("true") ? true : false;
    }

    //
    // js-no-timestamps 
    //

    public String getJsNoTimestamps()
    {
        return JSSharedData.OUTPUT_TIMESTAMPS ? "false" : "true";
    }

    @Config(advanced = true)
    @Mapping("js-no-timestamps")
    @Arguments("value")
    public void setJsNoTimestamps(ConfigurationValue cv, String value)
    {
        JSSharedData.OUTPUT_TIMESTAMPS = value.equals("false") ? true : false;
    }

    //
    // js-isolated 
    //

    public String getJsIsolated()
    {
        return JSSharedData.OUTPUT_ISOLATED ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-isolated")
    @Arguments("value")
    public void setJsIsolated(ConfigurationValue cv, String value)
    {
        JSSharedData.OUTPUT_ISOLATED = value.equals("true") ? true : false;
    }

    //
    // js-generate-test-case 
    //

    public String getJsGenerateTestCase()
    {
        return JSSharedData.GENERATE_TEST_CASE ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-generate-test-case")
    @Arguments("value")
    public void setJsGenerateTestCase(ConfigurationValue cv, String value)
    {
        JSSharedData.GENERATE_TEST_CASE = value.equals("true") ? true : false;
    }

    //
    // js-warn-performance-loss
    //

    public String getJsWarnPerformanceLoss()
    {
        return JSSharedData.WARN_PERFORMANCE_LOSS ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-warn-performance-loss")
    @Arguments("value")
    public void setJsWarnPerformanceLoss(ConfigurationValue cv, String value)
    {
        JSSharedData.WARN_PERFORMANCE_LOSS = value.equals("true") ? true : false;
    }

    //
    // js-warn-runtime-name-lookup
    //

    public String getJsRuntimeNameLookup()
    {
        return JSSharedData.WARN_RUNTIME_NAME_LOOKUP ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-warn-runtime-name-lookup")
    @Arguments("value")
    public void setJsRuntimeNameLookup(ConfigurationValue cv, String value)
    {
        JSSharedData.WARN_RUNTIME_NAME_LOOKUP = value.equals("true") ? true : false;
    }

    //
    // js-warn-class-init
    //

    public String getJsWarnClassInit()
    {
        return JSSharedData.WARN_CLASS_INIT ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-warn-class-init")
    @Arguments("value")
    public void setJsWarnClassInit(ConfigurationValue cv, String value)
    {
        JSSharedData.WARN_CLASS_INIT = value.equals("true") ? true : false;
    }

    //
    // js-extend-dom
    //

    public String getJsExtendDom()
    {
        return JSSharedData.EXTEND_DOM ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-extend-dom")
    @Arguments("value")
    public void setJsExtendDom(ConfigurationValue cv, String value)
    {
        JSSharedData.EXTEND_DOM = value.equals("true") ? true : false;
    }

    //
    // js-closure-compilation-level 
    //
    public String getJsClosureCompilationLevel()
    {
        return JSSharedData.CLOSURE_compilation_level;
    }

    @Config(advanced = true)
    @Mapping("js-closure-compilation-level")
    @Arguments("value")
    public void setJsClosureCompilationLevel(ConfigurationValue cv, String value)
    {
        JSSharedData.CLOSURE_compilation_level = value;
    }

    //
    // js-closure-externs 
    //
    public List<String> getJsClosureExterns()
    {
        return JSSharedData.CLOSURE_externs;
    }

    @Config(allowMultiple = true, isPath = true)
    @Mapping({"js-closure-externs"})
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setJsClosureExterns(ConfigurationValue cv, String[] pathlist) throws CannotOpen
    {
        final ImmutableList<String> resolvedPaths = expandTokens(
                Arrays.asList(pathlist),
                getCompilerLocales(),
                cv);
        if (JSSharedData.CLOSURE_externs == null)
            JSSharedData.CLOSURE_externs = new ArrayList<String>();
        JSSharedData.CLOSURE_externs.addAll(resolvedPaths);
    }

    //
    // js-closure-js 
    //
    public List<String> getJsClosureJs()
    {
        return JSSharedData.CLOSURE_js;
    }

    @Config(allowMultiple = true, isPath = true)
    @Mapping({"js-closure-js"})
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setJsClosureJs(ConfigurationValue cv, String[] pathlist) throws CannotOpen
    {
        final ImmutableList<String> resolvedPaths = expandTokens(
                Arrays.asList(pathlist),
                getCompilerLocales(),
                cv);
        if (JSSharedData.CLOSURE_js == null)
            JSSharedData.CLOSURE_js = new ArrayList<String>();
        JSSharedData.CLOSURE_js.addAll(resolvedPaths);
    }

    //
    // js-closure-create-source-map 
    //
    public String getJsClosureCreateSourceMap()
    {
        return JSSharedData.CLOSURE_create_source_map;
    }

    @Config(advanced = true)
    @Mapping("js-closure-create-source-map")
    @Arguments("value")
    public void setJsClosureCreateSourceMap(ConfigurationValue cv, String value)
    {
        JSSharedData.CLOSURE_create_source_map = value;
    }

    //
    // js-closure-formatting 
    //
    public String getJsClosureFormatting()
    {
        return JSSharedData.CLOSURE_formatting;
    }

    @Config(advanced = true)
    @Mapping("js-closure-formatting")
    @Arguments("value")
    public void setJsClosureFormatting(ConfigurationValue cv, String value)
    {
        JSSharedData.CLOSURE_formatting = value;
    }

    //
    // keep-generated-actionscript 
    //

    public String getJsKeepGeneratedJavaScript()
    {
        return JSSharedData.KEEP_GENERATED_AS ? "true" : "false";
    }

    @Config(advanced = true)
    @Mapping("js-keep-generated-javascript")
    @Arguments("value")
    public void setJsKeepGeneratedJavaScript(ConfigurationValue cv, String value)
    {
        JSSharedData.KEEP_GENERATED_AS = value.equals("true") ? true : false;
    }

    public Boolean keepGeneratedJavaScript()
    {
        return JSSharedData.KEEP_GENERATED_AS;
    }
}
