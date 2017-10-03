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

package org.apache.royale.compiler.internal.codegen.js.amd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.definitions.ITypeDefinition;

/**
 * @author Michael Schmalle
 */
public class ExportWriter
{
    private static final String CLASSES_TYPES = "__CLASSES$TYPES__";

    private static final String CLASSES_STRINGS = "__CLASSES$STRINGS__";

    private static final String RUNTIME_TYPES = "__RUNTIME$TYPES__";

    private static final String RUNTIME_STRINGS = "__RUNTIME$STRINGS__";

    private final JSAMDEmitter emitter;

    private List<Dependency> runtime = new ArrayList<Dependency>();

    private List<Dependency> types = new ArrayList<Dependency>();

    public ExportWriter(JSAMDEmitter emitter)
    {
        this.emitter = emitter;
    }

    public void queueExports(ITypeDefinition type, boolean outputString)
    {
        if (outputString)
        {
            emitter.write("[");
            emitter.write("\"exports\"");
        }

        emitter.write(", ");

        if (outputString)
            emitter.write(RUNTIME_STRINGS);
        else
            emitter.write(RUNTIME_TYPES);

        //emitter.write(", ");

        if (outputString)
            emitter.write(CLASSES_STRINGS);
        else
            emitter.write(CLASSES_TYPES);

        if (outputString)
        {
            emitter.write("]");
        }
    }

    public void writeExports(ITypeDefinition type, boolean outputString)
    {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        int len = runtime.size();

        for (Dependency dependency : runtime)
        {
            sb.append(dependency.output(outputString, "runtime", outputString));

            if (i < len - 1)
                sb.append(", ");
            i++;
        }

        if (outputString)
        {
            int start = emitter.builder().indexOf(RUNTIME_STRINGS);
            int end = start + RUNTIME_STRINGS.length();
            emitter.builder().replace(start, end, sb.toString());
        }
        else
        {
            int start = emitter.builder().indexOf(RUNTIME_TYPES);
            int end = start + RUNTIME_TYPES.length();
            emitter.builder().replace(start, end, sb.toString());
        }

        sb = new StringBuilder();

        i = 0;
        len = types.size();
        if (len > 0)
            sb.append(", "); // trailing comma

        for (Dependency dependency : types)
        {
            sb.append(dependency.output(outputString, "classes", outputString));

            if (i < len - 1)
                sb.append(", ");
            i++;
        }

        if (outputString)
        {
            int start = emitter.builder().indexOf(CLASSES_STRINGS);
            int end = start + CLASSES_STRINGS.length();
            emitter.builder().replace(start, end, sb.toString());
        }
        else
        {
            int start = emitter.builder().indexOf(CLASSES_TYPES);
            int end = start + CLASSES_TYPES.length();
            emitter.builder().replace(start, end, sb.toString());
        }
    }

    void addImports(ITypeDefinition type)
    {
        Collection<String> imports = new ArrayList<String>();
        type.getContainedScope().getScopeNode().getAllImports(imports);
        for (String imp : imports)
        {
            String name = toBaseName(imp);
            if (!isExcludedImport(imp))
                addDependency(name, imp, false, false);
        }
    }

    void addFrameworkDependencies()
    {
        runtime.add(new Dependency("AS3", "AS3", false, false));
    }

    protected boolean isExcludedImport(String imp)
    {
        return imp.startsWith("__AS3__");
    }

    public void addDependency(String baseName, String qualifiedName,
            boolean isNative, boolean isPlugin)
    {
        types.add(new Dependency(baseName, qualifiedName, isNative, isPlugin));
    }

    static String toBaseName(String name)
    {
        if (!name.contains("."))
            return name;
        final String basename = name.substring(name.lastIndexOf(".") + 1);
        return basename;
    }

    static class Dependency
    {
        private final String baseName;

        private final String qualifiedName;

        public String getQualifiedName()
        {
            return qualifiedName;
        }

        private final boolean isNative;

        public boolean isNative()
        {
            return isNative;
        }

        @SuppressWarnings("unused")
        private final boolean isPlugin;

        public Dependency(String baseName, String qualifiedName,
                boolean isNative, boolean isPlugin)
        {
            this.baseName = baseName;
            this.qualifiedName = qualifiedName;
            this.isNative = isNative;
            this.isPlugin = isPlugin;
        }

        @Override
        public String toString()
        {
            return qualifiedName; // TODO (mschmalle|AMD) native
        }

        public String output(boolean outputString, String base,
                boolean qualified)
        {
            StringBuilder sb = new StringBuilder();
            if (outputString)
            {
                sb.append("\"" + base + "/"
                        + qualifiedName.replaceAll("\\.", "/") + "\"");
            }
            else
            {
                if (qualified)
                    sb.append(qualifiedName);
                else
                    sb.append(baseName);
            }
            return sb.toString();
        }
    }
}
