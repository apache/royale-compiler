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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.apache.flex.compiler.clients.ExternCConfiguration.ExcludedMemeber;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

public class ReferenceModel
{
    private ExternCConfiguration configuration;
    private Compiler compiler;

    private List<String> namespaces = new ArrayList<String>();
    private HashMap<String, ClassReference> typedefs = new HashMap<String, ClassReference>();
    private HashMap<String, ClassReference> classes = new HashMap<String, ClassReference>();
    private HashMap<String, FunctionReference> functions = new HashMap<String, FunctionReference>();
    private HashMap<String, ConstantReference> constants = new HashMap<String, ConstantReference>();

    public Compiler getCompiler()
    {
        return compiler;
    }

    public void setCompiler(Compiler compiler)
    {
        this.compiler = compiler;
    }

    public ExternCConfiguration getConfiguration()
    {
        return configuration;
    }

    public Collection<String> getNamespaces()
    {
        return namespaces;
    }

    public Collection<ClassReference> getTypedefs()
    {
        return typedefs.values();
    }

    public Collection<ClassReference> getClasses()
    {
        return classes.values();
    }

    public Collection<FunctionReference> getFunctions()
    {
        return functions.values();
    }

    public Collection<ConstantReference> getConstants()
    {
        return constants.values();
    }

    public ReferenceModel(ExternCConfiguration config)
    {
        this.configuration = config;
    }

    public ClassReference getClassReference(String qName)
    {
        return classes.get(qName);
    }

    public void addNamespace(Node node, String qName)
    {
        if (namespaces.contains(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addNamespace(" + qName + ")");

        namespaces.add(qName);
    }

    public void addClass(Node node, String qName)
    {
        if (classes.containsKey(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addClass(" + qName + ")");

        ClassReference reference = new ClassReference(this, node, qName,
                node.getJSDocInfo());
        classes.put(qName, reference);
    }

    public void addTypeDef(Node node, String qName)
    {
        if (typedefs.containsKey(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addTypeDef(" + qName + ")");

        ClassReference reference = new ClassReference(this, node, qName,
                node.getJSDocInfo());
        typedefs.put(qName, reference);
    }

    public void addInterface(Node node, String qName)
    {
        if (classes.containsKey(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addInterface(" + qName + ")");

        ClassReference reference = new ClassReference(this, node, qName,
                node.getJSDocInfo());
        classes.put(qName, reference);
    }

    public void addFinalClass(Node node, String qName)
    {
        if (classes.containsKey(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addFinalClass(" + qName + ")");

        ClassReference reference = new ClassReference(this, node, qName,
                node.getJSDocInfo());
        reference.setFinal(true);
        classes.put(qName, reference);
    }

    public void addFunction(Node node, String qName)
    {
        if (functions.containsKey(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addFunction(" + qName + ")");
        //System.err.println(node.toStringTree());
        FunctionReference reference = new FunctionReference(this, node, qName,
                node.getJSDocInfo());
        functions.put(qName, reference);
    }

    public boolean hasConstant(String qName)
    {
        return constants.containsKey(qName);
    }

    public void addConstant(Node node, String qName)
    {
        if (constants.containsKey(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addConstant(" + qName + ")");

        ConstantReference reference = new ConstantReference(this, node, qName,
                node.getJSDocInfo());
        constants.put(qName, reference);
    }

    public void addConstantType(Node node, String qName, JSType type)
    {
        if (constants.containsKey(qName))
        {
            // XXX Record warning;
            return;
        }

        System.out.println("Model.addConstantType(" + qName + ")");

        ConstantReference reference = new ConstantReference(this, node, qName,
                node.getJSDocInfo(), type);
        constants.put(qName, reference);
    }

    public void addField(Node node, String className, String qualfiedName)
    {
        ClassReference classReference = getClassReference(className);
        if (classReference != null)
            classReference.addField(node, qualfiedName, node.getJSDocInfo(),
                    false);
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

    public ExcludedMemeber isExcludedClass(ClassReference classReference)
    {
        return getConfiguration().isExcludedClass(classReference);
    }

    public ExcludedMemeber isExcludedMember(ClassReference classReference,
            MemberReference memberReference)
    {
        return getConfiguration().isExcludedMember(classReference,
                memberReference);
    }

}
