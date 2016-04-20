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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.ConfigurationValue;
import org.apache.flex.compiler.exceptions.ConfigurationException.CannotOpen;
import org.apache.flex.compiler.exceptions.ConfigurationException.IncorrectArgumentCount;
import org.apache.flex.compiler.internal.codegen.externals.pass.ReferenceCompiler.ExternalFile;
import org.apache.flex.compiler.internal.codegen.externals.reference.BaseReference;
import org.apache.flex.compiler.internal.codegen.externals.reference.ClassReference;
import org.apache.flex.compiler.internal.codegen.externals.reference.FieldReference;
import org.apache.flex.compiler.internal.codegen.externals.reference.MemberReference;
import org.apache.flex.compiler.internal.config.annotations.Arguments;
import org.apache.flex.compiler.internal.config.annotations.Config;
import org.apache.flex.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.flex.compiler.internal.config.annotations.Mapping;
import org.apache.flex.utils.FilenameNormalization;

public class ExternCConfiguration extends Configuration
{
    private File jsRoot;

    private File asRoot;
    
    private File asClassRoot;
    private File asInterfaceRoot;
    private File asFunctionRoot;
    private File asConstantRoot;
    private File asTypeDefRoot;
    private File asDuplicatesRoot;

    private List<ExternalFile> externals = new ArrayList<ExternalFile>();
    private List<ExternalFile> externalExterns = new ArrayList<ExternalFile>();

    private List<String> classToFunctions = new ArrayList<String>();
    private List<ExcludedMember> excludesClass = new ArrayList<ExcludedMember>();
    private List<ExcludedMember> excludesField = new ArrayList<ExcludedMember>();
    private List<ExcludedMember> excludes = new ArrayList<ExcludedMember>();

    public ExternCConfiguration()
    {
    }

    public File getAsRoot()
    {
        return asRoot;
    }

    @Config
    @Mapping("as-root")
    public void setASRoot(ConfigurationValue cfgval, String filename) throws CannotOpen
    {
    	setASRoot(new File(FilenameNormalization.normalize(getOutputPath(cfgval, filename))));
    }
    
    public void setASRoot(File file)
    {
        this.asRoot = file;

        asClassRoot = new File(asRoot, "classes");
        asInterfaceRoot = new File(asRoot, "interfaces");
        asFunctionRoot = new File(asRoot, "functions");
        asConstantRoot = new File(asRoot, "constants");
        asTypeDefRoot = new File(asRoot, "typedefs");
        asDuplicatesRoot = new File(asRoot, "duplicates");
    }

    public File getAsClassRoot()
    {
        return asClassRoot;
    }

    public File getAsInterfaceRoot()
    {
        return asInterfaceRoot;
    }

    public File getAsFunctionRoot()
    {
        return asFunctionRoot;
    }

    public File getAsConstantRoot()
    {
        return asConstantRoot;
    }

    public File getAsTypeDefRoot()
    {
        return asTypeDefRoot;
    }

    public File getAsDuplicatesRoot()
    {
        return asDuplicatesRoot;
    }

    public Collection<ExternalFile> getExternals()
    {
        return externals;
    }

    public Collection<ExternalFile> getExternalExterns()
    {
        return externalExterns;
    }

    public boolean isClassToFunctions(String className)
    {
        return classToFunctions.contains(className);
    }

    public void addClassToFunction(String className)
    {
        classToFunctions.add(className);
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

    public void addExternalExtern(File file) throws IOException
    {
        if (!file.exists())
            throw new IOException(file.getAbsolutePath() + " does not exist.");
        externalExterns.add(new ExternalFile(file));
    }

    public void addExternalExtern(String externalFile) throws IOException
    {
        addExternalExtern(new File(FilenameNormalization.normalize(externalFile)));
    }

    @Config(allowMultiple = true)
    @Mapping("class-to-function")
    @Arguments(Arguments.CLASS)
    public void setClassToFunctions(ConfigurationValue cfgval, List<String> values) throws IncorrectArgumentCount
    {
        addClassToFunction(values.get(0));
    }

    @Config(allowMultiple = true, isPath = true)
    @Mapping("external")
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setExternal(ConfigurationValue cfgval, String[] vals) throws IOException, CannotOpen
    {
    	for (String val : vals)
    		addExternal(resolvePathStrict(val, cfgval));
    }

    @Config(allowMultiple = true, isPath = true)
    @Mapping("external-externs")
    @Arguments(Arguments.PATH_ELEMENT)
    @InfiniteArguments
    public void setExternalExterns(ConfigurationValue cfgval, String[] vals) throws IOException, CannotOpen
    {
        for (String val : vals)
            addExternalExtern(resolvePathStrict(val, cfgval));
    }
    
    public boolean isExternalExtern(BaseReference reference)
    {
        String sourceFileName = reference.getNode().getSourceFileName();
        for (ExternalFile file : externalExterns)
        {
            if (sourceFileName.equals("[" + file.getName() + "]"))
            {
                return true;
            }
        }
        return false;
    }
    
    public ExcludedMember isExcludedClass(ClassReference classReference)
    {
        for (ExcludedMember memeber : excludesClass)
        {
            if (memeber.isExcluded(classReference, null))
                return memeber;
        }
        return null;
    }

    public ExcludedMember isExcludedMember(ClassReference classReference,
            MemberReference memberReference)
    {
        if (memberReference instanceof FieldReference)
        {
            for (ExcludedMember memeber : excludesField)
            {
                if (memeber.isExcluded(classReference, memberReference))
                    return memeber;
            }
        }
        for (ExcludedMember memeber : excludes)
        {
            if (memeber.isExcluded(classReference, memberReference))
                return memeber;
        }
        return null;
    }

    @Config(allowMultiple = true)
    @Mapping("exclude")
    @Arguments({"class", "name"})
    public void setExcludes(ConfigurationValue cfgval, List<String> values) throws IncorrectArgumentCount
    {
        final int size = values.size();
        if (size % 2 != 0)
            throw new IncorrectArgumentCount(size + 1, size, cfgval.getVar(), cfgval.getSource(), cfgval.getLine());

        for (int nameIndex = 0; nameIndex < size - 1; nameIndex += 2)
        {
            final String className = values.get(nameIndex);
            final String name = values.get(nameIndex + 1);
        	addExclude(className, name);
        }
    }
    
    public void addExclude(String className, String name)
    {
        excludes.add(new ExcludedMember(className, name));
    }

    public void addExclude(String className, String name, String description)
    {
        excludes.add(new ExcludedMember(className, name, description));
    }

    @Config(allowMultiple = true)
    @Mapping("field-exclude")
    @Arguments({"class", "field"})
    public void setFieldExcludes(ConfigurationValue cfgval, List<String> values) throws IncorrectArgumentCount
    {
        final int size = values.size();
        if (size % 2 != 0)
            throw new IncorrectArgumentCount(size + 1, size, cfgval.getVar(), cfgval.getSource(), cfgval.getLine());

        for (int nameIndex = 0; nameIndex < size - 1; nameIndex += 2)
        {
            final String className = values.get(nameIndex);
            final String fieldName = values.get(nameIndex + 1);
        	addFieldExclude(className, fieldName);
        }
    }
    
    public void addFieldExclude(String className, String fieldName)
    {
        excludesField.add(new ExcludedMember(className, fieldName, ""));
    }

    @Config(allowMultiple = true)
    @Mapping("class-exclude")
    @Arguments("class")
    public void setClassExcludes(ConfigurationValue cfgval, List<String> values)
    {
    	for (String className : values)
    		addClassExclude(className);
    }
    public void addClassExclude(String className)
    {
        excludesClass.add(new ExcludedMember(className, null, ""));
    }

    public File getJsRoot()
    {
        return jsRoot;
    }

    @Config
    @Mapping("js-root")
    public void setJSRoot(ConfigurationValue cfgval, String filename) throws CannotOpen
    {
        this.jsRoot = new File(filename);
    }


    public static class ExcludedMember
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

        public ExcludedMember(String className, String name)
        {
            this.className = className;
            this.name = name;
        }

        public ExcludedMember(String className, String name, String description)
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

}
