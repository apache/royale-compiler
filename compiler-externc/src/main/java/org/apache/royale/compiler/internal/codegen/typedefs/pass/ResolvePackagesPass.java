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

import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.FieldReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.Node;

public class ResolvePackagesPass extends AbstractCompilerPass
{
    public ResolvePackagesPass(ReferenceModel model, AbstractCompiler compiler)
    {
        super(model, compiler);
    }

    @SuppressWarnings("unused")
    @Override
    public void process(Node externs, Node root)
    {
        log(">>>-----------------------------");
        for (ClassReference reference : model.getClasses())
        {
            if (reference.isQualifiedName())
            {
                reference.setIsNamespace(true);
            }
            log(reference.getQualifiedName());
            List<ClassReference> children = getFirstChildren(reference);

        }
        log("<<<-----------------------------");
    }

    @SuppressWarnings("unused")
    private List<ClassReference> getFirstChildren(ClassReference reference)
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        String qualifiedName = reference.getQualifiedName();
        String[] split = qualifiedName.split("\\.");

        for (ClassReference child : model.getClasses())
        {
            String baseName = child.getBaseName();
            String testName = qualifiedName + "." + baseName;
            if (testName.equals(child.getQualifiedName()))
            {
                FieldReference field;
                if (!reference.isQualifiedName())
                {
                    log("   Add field: public static var " + baseName);
                    field = reference.addField(child.getNode(), baseName, child.getNode().getJSDocInfo(), true);
                }
                else
                {
                    log("   Add field: public var " + baseName);
                    field = reference.addField(child.getNode(), baseName, child.getNode().getJSDocInfo(), false);
                }

                field.setOverrideStringType(child.getQualifiedName());
            }
        }

        return result;
    }

    @Override
    public boolean shouldTraverse(NodeTraversal arg0, Node arg1, Node arg2)
    {
        return false;
    }

    @Override
    public void visit(NodeTraversal arg0, Node arg1, Node arg2)
    {
    }
}
