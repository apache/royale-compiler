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

package org.apache.royale.compiler.internal.codegen.typedefs.pass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.parsing.Config;

public class ReferenceCompiler
{
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
    
    String[] asdocTags = new String[] {"chainable", 
    		"readOnly", "uses", "main"};

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
        //options.setIdeMode(true);
        options.setParseJsDocDocumentation(Config.JsDocParsing.INCLUDE_DESCRIPTIONS_NO_WHITESPACE);
        options.setExternExports(false);
        options.setExtraAnnotationNames(Arrays.asList(asdocTags));
        options.setLanguageIn(LanguageMode.ECMASCRIPT_2015);
        options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS, new NamespaceResolutionPass(model,
                jscompiler));
        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS, new ResolvePackagesPass(model, jscompiler));

        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS, new CollectTypesPass(model, jscompiler));
        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS, new AddMemberPass(model, jscompiler));

        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS, new CollectImportsPass(model, jscompiler));

        //compiler.setErrorManager(testErrorManager);
        jscompiler.initOptions(options);

        // don't need custom error manager with es6->es5 language options
        //jscompiler.setErrorManager(wrapErrorManager(jscompiler.getErrorManager()));
        model.setJSCompiler(jscompiler);
    }

    public ErrorManager wrapErrorManager(ErrorManager em)
    {
    	return new ReferenceErrorManager(em);
    }
    
    public Result compile() throws IOException
    {
        List<SourceFile> externs = new ArrayList<SourceFile>();
        for (TypedefFile externalFile : model.getConfiguration().getTypedefs())
        {
            String name = externalFile.getName();
            String source = FileUtils.readFileToString(externalFile.getFile());
            externs.add(SourceFile.fromCode("[" + name + "]", source));
        }
        for (TypedefFile externalFile : model.getConfiguration().getTypedefTypedefs())
        {
            String name = externalFile.getName();
            String source = FileUtils.readFileToString(externalFile.getFile());
            externs.add(SourceFile.fromCode("[" + name + "]", source));
        }

        //should be passed in as externs rather than source files because they
        //may contain annotations that are only allowed in externs -JT
        Result result = jscompiler.compile(externs, new ArrayList<SourceFile>(), options);
        if (!result.success)
        {

        }

        return result;
    }

    public static class TypedefFile
    {
        private File file;

        public File getFile()
        {
            return file;
        }

        public TypedefFile(File file)
        {
            this.file = file;
        }

        public String getName()
        {
            return FilenameUtils.getBaseName(getFile().getAbsolutePath());
        }
        
        public String toString()
        {
        	return getFile().getName();
        }
    }
    
    public static class ReferenceErrorManager implements ErrorManager
    {
    	public ReferenceErrorManager(ErrorManager em)
    	{
    		this.em = em;
    	}
    	
    	private ErrorManager em;

		@Override
		public void generateReport() {
			em.generateReport();
		}

		@Override
		public int getErrorCount() {
			int num = em.getErrorCount();
			if (num > 0)
			{
				num = 0;
			}
			return num;
		}

		@Override
		public JSError[] getErrors() {
			return em.getErrors();
		}

		@Override
		public double getTypedPercent() {
			return em.getTypedPercent();
		}

		@Override
		public int getWarningCount() {
			return em.getWarningCount();
		}

		@Override
		public JSError[] getWarnings() {
			return em.getWarnings();
		}

		@Override
		public void report(CheckLevel arg0, JSError arg1) {
			if (arg1.description.equals("Parse error. identifier is a reserved word"))
			{
				if (arg1.sourceName.equals("[missing]"))
				{
					if (arg1.lineNumber == 101 ||
							arg1.lineNumber == 107 ||
							arg1.lineNumber == 232 ||
							arg1.lineNumber == 239)
					{
						return;
					}
				}
			}
			em.report(arg0, arg1);
		}

		@Override
		public void setTypedPercent(double arg0) {
			em.setTypedPercent(arg0);
		}
    	
    }
}
