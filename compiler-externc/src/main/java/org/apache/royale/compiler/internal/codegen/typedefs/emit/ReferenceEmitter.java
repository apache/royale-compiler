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

package org.apache.royale.compiler.internal.codegen.typedefs.emit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.BaseReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ConstantReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.FunctionReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.jstype.JSType;

public class ReferenceEmitter
{
    private ReferenceModel model;

    public ReferenceEmitter(ReferenceModel model)
    {
        this.model = model;
    }

    public void emit() throws IOException
    {
        final File asRoot = model.getConfiguration().getAsRoot();
        if (!asRoot.exists())
            asRoot.mkdirs();

        emitClasses();
        emitInterfaces();
        emitTypedefs();
        emitFunctions();
        emitConstants();
    }

    protected void emitInterfaces() throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        for (ClassReference reference : model.getClasses())
        {
            if (model.isExcludedClass(reference) != null)
                continue;

            if (!reference.isInterface())
                continue;

            if (model.getConfiguration().isExternalTypedef(reference))
                continue;

            emit(reference, sb);

            File sourceFile = reference.getFile(model.getConfiguration().getAsInterfaceRoot());
            if(sourceFile.exists())
            {
                sourceFile = reference.getFile(model.getConfiguration().getAsDuplicatesRoot());
            }
            FileUtils.write(sourceFile, sb.toString());

            sb.setLength(0);
        }
    }

    protected void emitClasses() throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        for (ClassReference reference : model.getClasses())
        {
            if (model.isExcludedClass(reference) != null)
                continue;

            if (reference.isInterface())
                continue;

            if (model.getConfiguration().isExternalTypedef(reference))
                continue;
            
            emit(reference, sb);

            File sourceFile = reference.getFile(model.getConfiguration().getAsClassRoot());
            if(sourceFile.exists())
            {
                sourceFile = reference.getFile(model.getConfiguration().getAsDuplicatesRoot());
            }
            FileUtils.write(sourceFile, sb.toString());

            sb.setLength(0);
        }
    }

    protected void emitTypedefs() throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        // TODO figure out how to resolve/emit @typedef
        for (ClassReference reference : model.getTypedefs())
        {
            if (model.isExcludedClass(reference) != null)
                continue;

            if (model.getConfiguration().isExternalTypedef(reference))
                continue;

            JSDocInfo comment = reference.getComment();
            if (comment != null)
            {
                JSType typeDefJSType = model.evaluate(comment.getTypedefType());
                if (typeDefJSType.isFunctionType())
                {
                    // typedefs for functions don't need to be emitted because the
                    // ActionScript Function type doesn't have a way to specify a
                    // function signature.
                    continue;
                }
            }

            emit(reference, sb);

            File sourceFile = reference.getFile(model.getConfiguration().getAsTypeDefRoot());
            if(sourceFile.exists())
            {
                sourceFile = reference.getFile(model.getConfiguration().getAsDuplicatesRoot());
            }
            FileUtils.write(sourceFile, sb.toString());

            sb.setLength(0);
        }
    }

    protected void emitFunctions() throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        for (FunctionReference reference : model.getFunctions())
        {
            if (model.isExcludedClass(reference) != null)
                continue;
            
            if (model.getConfiguration().isExternalTypedef(reference))
                continue;
            
            emit(reference, sb);

            File sourceFile = reference.getFile(model.getConfiguration().getAsFunctionRoot());
            if(sourceFile.exists())
            {
                sourceFile = reference.getFile(model.getConfiguration().getAsDuplicatesRoot());
            }
            FileUtils.write(sourceFile, sb.toString());

            sb.setLength(0);
        }
    }

    protected void emitConstants() throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        for (ConstantReference reference : model.getConstants())
        {
            if (model.getConfiguration().isExternalTypedef(reference))
                continue;
            
            emit(reference, sb);

            File sourceFile = reference.getFile(model.getConfiguration().getAsConstantRoot());
            if(sourceFile.exists())
            {
                sourceFile = reference.getFile(model.getConfiguration().getAsDuplicatesRoot());
            }
            FileUtils.write(sourceFile, sb.toString());

            sb.setLength(0);
        }
    }

    public void emit(BaseReference reference, StringBuilder sb)
    {
        reference.emit(sb);
    }

    public String emit(BaseReference reference)
    {
        final StringBuilder sb = new StringBuilder();
        reference.emit(sb);
        return sb.toString();
    }
}
