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

package org.apache.flex.compiler.internal.codegen.externals.reference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.flex.compiler.internal.codegen.externals.pass.AddMemberPass;
import org.apache.flex.compiler.internal.codegen.externals.pass.CollectTypesPass;
import org.apache.flex.utils.FilenameNormalization;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.CustomPassExecutionTime;
import com.google.javascript.jscomp.JXCompilerOptions;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;

public class ReferenceModel
{
    private static final List<SourceFile> EMPTY_EXTERNS = ImmutableList.of(SourceFile.fromCode(
            "externs", ""));

    private File asRoot;
    private File asFunctionRoot;
    private File asConstantRoot;

    private List<ExcludedMemeber> excludesClass = new ArrayList<ExcludedMemeber>();
    private List<ExcludedMemeber> excludesField = new ArrayList<ExcludedMemeber>();
    private List<ExcludedMemeber> excludes = new ArrayList<ExcludedMemeber>();
    private List<ExternalFile> externals = new ArrayList<ExternalFile>();

    private HashMap<String, ClassReference> classes = new HashMap<String, ClassReference>();
    private HashMap<String, FunctionReference> functions = new HashMap<String, FunctionReference>();
    private HashMap<String, ConstantReference> constants = new HashMap<String, ConstantReference>();

    private com.google.javascript.jscomp.Compiler compiler;

    public void setASRoot(File file)
    {
        this.asRoot = file;

        asFunctionRoot = new File(asRoot.getParent(), "as_functions");
        asConstantRoot = new File(asRoot.getParent(), "as_constants");
    }

    public com.google.javascript.jscomp.Compiler getCompiler()
    {
        return compiler;
    }

    public ReferenceModel()
    {
        //walker = new JSBlockWalker(this);
    }

    public void addExclude(String className, String name)
    {
        excludes.add(new ExcludedMemeber(className, name));
    }

    public void addExclude(String className, String name, String description)
    {
        excludes.add(new ExcludedMemeber(className, name, description));
    }

    public void addFieldExclude(String className, String fieldName)
    {
        excludesField.add(new ExcludedMemeber(className, fieldName, ""));
    }

    public void addClassExclude(String className)
    {
        excludesClass.add(new ExcludedMemeber(className, null, ""));
    }

    public void addExternal(File file) throws IOException
    {
        if (!file.exists())
            throw new IOException(file.getAbsolutePath() + " does not exist.");
        externals.add(new ExternalFile(file));
    }

    public void addExternal(String externalFile) throws IOException
    {
        addExternal(new File(FilenameNormalization.normalize(externalFile)));
    }

    //    public void addExternal(String name)
    //    {
    //        File file = new File(jsRoot, name + ".js");
    //        externals.add(new ExternalFile(file));
    //    }

    public ClassReference getClassReference(String qualifiedName)
    {
        return classes.get(qualifiedName);
    }

    public void addClass(Node node, String qualfiedName)
    {
        if (classes.containsKey(qualfiedName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addClass(" + qualfiedName + ")");

        ClassReference reference = new ClassReference(this, node, qualfiedName,
                node.getJSDocInfo());
        classes.put(qualfiedName, reference);
    }

    public void addInterface(Node node, String qualfiedName)
    {
        if (classes.containsKey(qualfiedName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addInterface(" + qualfiedName + ")");

        ClassReference reference = new ClassReference(this, node, qualfiedName,
                node.getJSDocInfo());
        classes.put(qualfiedName, reference);
    }

    public void addFinalClass(Node node, String qualfiedName)
    {
        if (classes.containsKey(qualfiedName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addFinalClass(" + qualfiedName + ")");

        ClassReference reference = new ClassReference(this, node, qualfiedName,
                node.getJSDocInfo());
        reference.setFinal(true);
        classes.put(qualfiedName, reference);
    }

    public void addFunction(Node node, String qualfiedName)
    {
        if (functions.containsKey(qualfiedName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addFunction(" + qualfiedName + ")");
        //System.err.println(node.toStringTree());
        FunctionReference reference = new FunctionReference(this, node,
                qualfiedName, node.getJSDocInfo());
        functions.put(qualfiedName, reference);
    }

    public void addConstant(Node node, String qualfiedName)
    {
        if (constants.containsKey(qualfiedName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addConstant(" + qualfiedName + ")");

        ConstantReference reference = new ConstantReference(this, node,
                qualfiedName, node.getJSDocInfo());
        constants.put(qualfiedName, reference);
    }

    public void addField(Node node, String className, String qualfiedName)
    {
        ClassReference classReference = getClassReference(className);
        classReference.addField(node, qualfiedName, node.getJSDocInfo(), false);
    }

    public void addStaticField(Node node, String className, String qualfiedName)
    {
        ClassReference classReference = getClassReference(className);
        // XXX this is here because for now, the doc might be on the parent ASSIGN node
        // if it's a static property with a value
        JSDocInfo comment = NodeUtil.getBestJSDocInfo(node);
        if (classReference != null)
        {
            classReference.addField(node, qualfiedName, comment, true);
        }
        else
        {
            System.err.println(">>>> {ReferenceModel} Class [" + className
                    + "] not found in " + node.getSourceFileName());
        }
    }

    //----------------------------------------------------

    public void cleanOutput() throws IOException
    {
        FileUtils.deleteDirectory(asRoot);
    }

    public void emit() throws IOException
    {
        asRoot.mkdirs();

        for (Entry<String, ClassReference> set : classes.entrySet())
        {
            StringBuilder sb = new StringBuilder();

            ClassReference reference = set.getValue();
            if (isExcludedClass(reference) != null)
                continue;

            if (!reference.isInterface())
                continue;

            emit(reference, sb);

            File sourceFile = reference.getFile(asRoot);
            FileUtils.write(sourceFile, sb.toString());
        }

        for (Entry<String, ClassReference> set : classes.entrySet())
        {
            ClassReference reference = set.getValue();
            if (isExcludedClass(reference) != null)
                continue;

            if (reference.isInterface())
                continue;

            StringBuilder sb = new StringBuilder();

            emit(reference, sb);

            File sourceFile = reference.getFile(asRoot);
            FileUtils.write(sourceFile, sb.toString());
        }

        for (Entry<String, FunctionReference> set : functions.entrySet())
        {
            StringBuilder sb = new StringBuilder();

            FunctionReference reference = set.getValue();
            emit(reference, sb);

            File sourceFile = reference.getFile(asFunctionRoot);
            FileUtils.write(sourceFile, sb.toString());
        }

        for (Entry<String, ConstantReference> set : constants.entrySet())
        {
            StringBuilder sb = new StringBuilder();

            ConstantReference reference = set.getValue();
            emit(reference, sb);

            File sourceFile = reference.getFile(asConstantRoot);
            FileUtils.write(sourceFile, sb.toString());
        }

        //        StringBuilder sb = new StringBuilder();
        //        sb.append("package {\n");
        //        for (Entry<String, ConstantReference2> set : constants.entrySet())
        //        {
        //            ConstantReference2 reference = set.getValue();
        //            emit(reference, sb);
        //        }
        //        sb.append("\n}");
        //        File sourceFile = new File(asRoot, "constants.as");
        //        FileUtils.write(sourceFile, sb.toString());
    }

    public void emit(BaseReference reference, StringBuilder sb)
    {
        reference.emit(sb);
    }

    public String emit(BaseReference reference)
    {
        StringBuilder sb = new StringBuilder();
        reference.emit(sb);
        return sb.toString();
    }

    public void compile() throws IOException
    {
        JXCompilerOptions options = new JXCompilerOptions();
        //options.setLanguageIn(LanguageMode.ECMASCRIPT6_TYPED);
        //options.setLanguageOut(LanguageMode.ECMASCRIPT6_TYPED);
        options.setPreserveTypeAnnotations(true);
        options.setPrettyPrint(true);
        options.setLineLengthThreshold(80);
        options.setPreferSingleQuotes(true);
        options.setIdeMode(true);
        options.setParseJsDocDocumentation(true);
        options.setExternExports(false);

        compiler = new com.google.javascript.jscomp.Compiler();

        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
                new CollectTypesPass(this, compiler));
        options.addCustomPass(CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
                new AddMemberPass(this, compiler));

        //compiler.setErrorManager(testErrorManager);
        compiler.initOptions(options);

        //Node script = compiler.parse(SourceFile.fromCode("[test]", source));

        List<SourceFile> sources = new ArrayList<SourceFile>();
        for (ExternalFile externalFile : externals)
        {
            String name = externalFile.getName();
            String source = FileUtils.readFileToString(externalFile.getFile());
            sources.add(SourceFile.fromCode("[" + name + "]", source));
        }

        Result compile = compiler.compile(EMPTY_EXTERNS, sources, options);
        if (!compile.success)
        {

        }
    }

    public ExcludedMemeber isExcludedClass(ClassReference classReference)
    {
        for (ExcludedMemeber memeber : excludesClass)
        {
            if (memeber.isExcluded(classReference, null))
                return memeber;
        }
        return null;
    }

    public ExcludedMemeber isExcludedMember(ClassReference classReference,
            MemberReference memberReference)
    {
        if (memberReference instanceof FieldReference)
        {
            for (ExcludedMemeber memeber : excludesField)
            {
                if (memeber.isExcluded(classReference, memberReference))
                    return memeber;
            }
        }
        for (ExcludedMemeber memeber : excludes)
        {
            if (memeber.isExcluded(classReference, memberReference))
                return memeber;
        }
        return null;
    }

    public static class ExcludedMemeber
    {
        private String className;
        private String name;
        private String description;

        public String getClassName()
        {
            return className;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public ExcludedMemeber(String className, String name)
        {
            this.className = className;
            this.name = name;
        }

        public ExcludedMemeber(String className, String name, String description)
        {
            this.className = className;
            this.name = name;
            this.description = description;
        }

        public boolean isExcluded(ClassReference classReference,
                MemberReference memberReference)
        {
            if (memberReference == null)
            {
                return classReference.getQualifiedName().equals(className);
            }
            return classReference.getQualifiedName().equals(className)
                    && memberReference.getQualifiedName().equals(name);
        }

        public void print(StringBuilder sb)
        {
            if (description != null)
                sb.append("// " + description + "\n");
            sb.append("//");
        }
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
