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

package org.apache.flex.compiler.internal.codegen.externals.pass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.flex.compiler.internal.codegen.externals.reference.ReferenceModel;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CustomPassExecutionTime;
import com.google.javascript.jscomp.JXCompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

public class ReferenceCompiler
{
    private static final List<SourceFile> EMPTY_EXTERNS = ImmutableList.of(SourceFile.fromCode(
            "externs", ""));

    private ReferenceModel model;

    private Compiler jscompiler;
    private JXCompilerOptions options;

    public Compiler getJSCompiler()
    {
        return jscompiler;
    }

    public ReferenceCompiler(ReferenceModel model)
    {
        this.model = model;

        initializeCompiler();
    }

    private void initializeCompiler()
    {
        jscompiler = new Compiler();

        options = new JXCompilerOptions();
        //options.setLanguageIn(LanguageMode.ECMASCRIPT6_TYPED);
        //options.setLanguageOut(LanguageMode.ECMASCRIPT6_TYPED);
        options.setPreserveTypeAnnotations(true);
        options.setPrettyPrint(true);
        options.setLineLengthThreshold(80);
        options.setPreferSingleQuotes(true);
        options.setIdeMode(true);
        options.setParseJsDocDocumentation(true);
        options.setExternExports(false);

        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
                new NamespaceResolutionPass(model, jscompiler));
        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
                new ResolvePackagesPass(model, jscompiler));

        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
                new CollectTypesPass(model, jscompiler));
        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
                new AddMemberPass(model, jscompiler));

        //compiler.setErrorManager(testErrorManager);
        jscompiler.initOptions(options);

        model.setJSCompiler(jscompiler);
    }

    public Result compile() throws IOException
    {
        List<SourceFile> sources = new ArrayList<SourceFile>();
        for (ExternalFile externalFile : model.getConfiguration().getExternals())
        {
            String name = externalFile.getName();
            String source = FileUtils.readFileToString(externalFile.getFile());
            sources.add(SourceFile.fromCode("[" + name + "]", source));
        }
        for (ExternalFile externalFile : model.getConfiguration().getExternalExterns())
        {
            String name = externalFile.getName();
            String source = FileUtils.readFileToString(externalFile.getFile());
            sources.add(SourceFile.fromCode("[" + name + "]", source));
        }

        Result result = jscompiler.compile(EMPTY_EXTERNS, sources, options);
        if (!result.success)
        {

        }

        return result;
    }

    public static class ExternalFile
    {
        private File file;

        public File getFile()
        {
            return file;
        }

        public ExternalFile(File file)
        {
            this.file = file;
        }

        public String getName()
        {
            return FilenameUtils.getBaseName(getFile().getAbsolutePath());
        }
    }
}
