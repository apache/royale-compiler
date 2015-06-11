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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.flex.compiler.internal.codegen.externals.utils.JSTypeUtils;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;

public class ClassReference extends BaseReference
{

    private boolean isFinal;

    private MethodReference constructor;
    private Map<String, FieldReference> fields = new HashMap<String, FieldReference>();
    private Map<String, MethodReference> methods = new HashMap<String, MethodReference>();

    private Node nameNode;

    private Node functionNode;

    @SuppressWarnings("unused")
    private Node paramListNode;

    public MethodReference getConstructor()
    {
        return constructor;
    }

    public Map<String, FieldReference> getFields()
    {
        return fields;
    }

    public Map<String, MethodReference> getMethods()
    {
        return methods;
    }

    public MethodReference getMethod(String name)
    {
        return methods.get(name);
    }

    public boolean isFinal()
    {
        return isFinal;
    }

    public void setFinal(boolean isFinal)
    {
        this.isFinal = isFinal;
    }

    public final boolean isInterface()
    {
        return getComment().isInterface();
    }

    /**
     * 
     * @param model
     * @param node (FUNCTION [NAME, PARAM_LIST, BLOCK]), or (ASSIGN [FUNCTION
     *        [NAME, PARAM_LIST, BLOCK]])
     * @param qualfiedName
     * @param comment
     */
    public ClassReference(ReferenceModel model, Node node, String qualfiedName)
    {
        super(model, node, qualfiedName, node.getJSDocInfo());

        nameNode = null;
        functionNode = null;
        paramListNode = null;

        if (comment.getTypedefType() != null)
        {
            //System.out.println(node.toStringTree());
            /*
             VAR 727 [jsdoc_info: JSDocInfo] [source_file: [w3c_rtc]] [length: 21]
                NAME MediaConstraints 727 [source_file: [w3c_rtc]] [length: 16]
             */
        }
        else if (node.isFunction())
        {
            /*
             FUNCTION FooVarArgs 43 [jsdoc_info: JSDocInfo]
                NAME FooVarArgs
                PARAM_LIST
                    NAME arg1
                    NAME var_args
                BLOCK 43
             */
            nameNode = node.getChildAtIndex(0);
            functionNode = node;
            paramListNode = functionNode.getChildAtIndex(1);
        }
        else if (node.isVar())
        {
            /*
            VAR 67 [jsdoc_info: JSDocInfo]
                NAME VarAssignFooNoArgs
                    FUNCTION 
                        NAME 
                        PARAM_LIST
                        BLOCK
             */
            nameNode = node.getChildAtIndex(0);
            functionNode = nameNode.getChildAtIndex(0);
            try
            {
                paramListNode = functionNode.getChildAtIndex(1);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else if (node.isAssign())
        {
            /*
             ASSIGN 60 [jsdoc_info: JSDocInfo]
                NAME AssignFooNoArgs
                FUNCTION
                    NAME
                    PARAM_LIST
                    BLOCK
             */
            nameNode = node.getFirstChild();
            functionNode = node.getLastChild();
            // this is an anonymous function assignment, no name
            //functionNameNode = functionNode.getChildAtIndex(0);
            paramListNode = functionNode.getChildAtIndex(1);
        }

        if (functionNode != null)
        {
            constructor = new MethodReference(model, this, functionNode,
                    getBaseName(), comment, false);
        }

    }

    public FieldReference addField(Node node, String fieldName,
            JSDocInfo comment, boolean isStatic)
    {
        if (hasField(fieldName))
        {
            // XXX Warning
            return null;
        }

        FieldReference field = new FieldReference(getModel(), this, node,
                fieldName, comment, isStatic);

        fields.put(fieldName, field);
        return field;
    }

    public boolean hasSuperField(String fieldName)
    {
        List<ClassReference> list = getSuperClasses();
        for (ClassReference reference : list)
        {
            if (reference.hasField(fieldName))
                return true;
        }
        return false;
    }

    public boolean hasSuperMethod(String methodName)
    {
        List<ClassReference> list = getSuperClasses();
        for (ClassReference reference : list)
        {
            if (reference.hasMethod(methodName))
                return true;
        }
        return false;
    }

    public List<ClassReference> getSuperClasses()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        ClassReference superClass = getSuperClass();
        while (superClass != null)
        {
            result.add(superClass);
            superClass = superClass.getSuperClass();
        }
        return result;
    }

    public boolean hasField(String fieldName)
    {
        return fields.containsKey(fieldName);
    }

    public boolean hasMethod(String methodName)
    {
        return methods.containsKey(methodName);
    }

    public MethodReference addMethod(Node node, String functionName,
            JSDocInfo comment, boolean isStatic)
    {
        MethodReference method = new MethodReference(getModel(), this, node,
                functionName, comment, isStatic);
        methods.put(functionName, method);
        return method;
    }

    @Override
    public void emit(StringBuilder sb)
    {
        String packageName = "";

        sb.append("package ");
        sb.append(packageName + " ");
        sb.append("{\n");
        sb.append("\n");

        printImports();

        boolean isInterface = isInterface();

        if (isInterface)
        {
            printInterface(sb);
        }
        else
        {
            printClass(sb);
        }

        sb.append("{\n");
        sb.append("\n");

        if (!isInterface)
        {
            printConstructor(sb);
            sb.append("\n");
        }

        //        for (Entry<String, FieldReference> fieldSet : getStaticFields().entrySet())
        //        {
        //            fieldSet.getValue().emit(sb);
        //            sb.append("\n");
        //        }

        for (Entry<String, FieldReference> fieldSet : getFields().entrySet())
        {
            fieldSet.getValue().emit(sb);
            sb.append("\n");
        }

        for (Entry<String, MethodReference> methodSet : getMethods().entrySet())
        {
            MethodReference method = methodSet.getValue();
            //if (!method.isOverride())
            //{
            method.emit(sb);
            sb.append("\n");
            //}
        }

        sb.append("}\n");
        sb.append("}\n"); // package

        //System.out.println(sb.toString());
    }

    private void printClass(StringBuilder sb)
    {
        boolean isDynamic = false;

        sb.append("public ");
        if (isDynamic)
        {
            sb.append("dynamic ");
        }

        if (isFinal)
        {
            sb.append("final ");
        }

        sb.append("class ");
        sb.append(getQualifiedName() + " ");

        if (getComment().hasBaseType())
        {
            printSuperClass(sb);
            sb.append(" ");
        }
        //
        //        if (TagUtils.hasTags(this, "implements"))
        //        {
        //            printImplements(sb);
        //            sb.append(" ");
        //        }
    }

    private void printInterface(StringBuilder sb)
    {
        sb.append("public interface ");

        sb.append(getQualifiedName() + " ");

        //        if (TagUtils.hasTags(this, "extends"))
        //        {
        //            printSuperClass(sb);
        //            sb.append(" ");
        //        }
    }

    private void printSuperClass(StringBuilder sb)
    {
        sb.append("extends ");
        String value = JSTypeUtils.toTypeJsType(getModel().getCompiler(),
                getComment().getBaseType()).toString();
        sb.append(value);
    }

    @SuppressWarnings("unused")
    private void printImplements(StringBuilder sb)
    {
        //        if (TagUtils.hasTags(this, "implements"))
        //        {
        //            sb.append("implements ");
        //            List<DocletTag> impls = TagUtils.getTags(this, "implements");
        //            int len = impls.size();
        //            for (int i = 0; i < len; i++)
        //            {
        //                String value = impls.get(0).getValue();
        //                sb.append(value.substring(1, value.length() - 1));
        //                if (i < len - 1)
        //                    sb.append(", ");
        //            }
        //        }
    }

    private void printConstructor(StringBuilder sb)
    {
        if (constructor != null)
        {
            constructor.emit(sb);
        }
    }

    private void printImports()
    {
        // TODO Auto-generated method stub

    }

    public File getFile(File asSourceRoot)
    {
        String packageName = "";

        return new File(asSourceRoot, packageName + File.separator
                + getQualifiedName() + ".as");
    }

    public boolean isMethodOverrideFromInterface(MethodReference reference)
    {
        //        if (!hasImplementations())
        //            return false;
        //
        //        List<DocletTag> impls = TagUtils.getTags(this, "implements");
        //        for (DocletTag docletTag : impls)
        //        {
        //            String interfaceName = docletTag.getValue().trim();
        //            interfaceName = interfaceName.substring(1,
        //                    interfaceName.length() - 1);
        //            ClassReference2 classReference = model.getClassReference(interfaceName);
        //            return classReference.hasMethod(reference.getName());
        //        }

        return false;
    }

    public MethodReference getMethodOverrideFromInterface(
            MethodReference reference)
    {
        //        if (!hasImplementations())
        //            return null;
        //
        //        List<DocletTag> impls = TagUtils.getTags(this, "implements");
        //        for (DocletTag docletTag : impls)
        //        {
        //            String interfaceName = docletTag.getValue().trim();
        //            interfaceName = interfaceName.substring(1,
        //                    interfaceName.length() - 1);
        //            ClassReference2 classReference = model.getClassReference(interfaceName);
        //            return classReference.getMethods().get(reference.getName());
        //        }

        return null;
    }

    @SuppressWarnings("unused")
    private boolean hasImplementations()
    {
        return getComment().getImplementedInterfaceCount() > 0;
    }

    public boolean hasImplements(String interfaceName)
    {
        //        boolean hasImplements = TagUtils.hasTags(this, "implements");
        //        if (hasImplements)
        //        {
        //            List<DocletTag> impls = TagUtils.getTags(this, "implements");
        //            for (DocletTag tag : impls)
        //            {
        //                String value = tag.getValue();
        //                value = value.substring(1, value.indexOf("}"));
        //                return value.equals(interfaceName);
        //            }
        //        }
        return false;
    }

    public ClassReference getSuperClass()
    {
        JSTypeExpression baseType = getComment().getBaseType();
        if (baseType != null)
        {
            JSType jsType = baseType.evaluate(null,
                    getModel().getCompiler().getTypeRegistry());
            if (jsType != null)
                return getModel().getClassReference(jsType.getDisplayName());
        }
        return null;
    }

    public boolean hasSuperFieldConflict(FieldReference reference)
    {
        //        ClassReference2 superClass = getSuperClass();
        //        if (superClass != null)
        //            return superClass.getInstanceFields().containsKey(
        //                    reference.getName());
        return false;
    }

    public boolean isPropertyInterfaceImplementation(FieldReference reference)
    {
        //        List<ClassReference2> superInterfaces = getSuperInterfaces();
        //        for (ClassReference2 interfaceRef : superInterfaces)
        //        {
        //            if (interfaceRef == null)
        //            {
        //                System.err.println("isPropertyInterfaceImplementation() null");
        //                continue;
        //            }
        //            if (interfaceRef.hasFieldConflict(reference))
        //                return true;
        //        }
        return false;
    }

    @SuppressWarnings("unused")
    private List<ClassReference> getSuperInterfaces()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        //        if (!TagUtils.hasTags(this, "implements"))
        //            return result;
        //
        //        List<DocletTag> impls = TagUtils.getTags(this, "implements");
        //        for (DocletTag tag : impls)
        //        {
        //            String type = TagUtils.getType(tag);
        //            result.add(model.getClassReference(type));
        //        }
        return result;
    }

    public boolean hasLocalMethodConflict(String functionName)
    {
        return methods.containsKey(functionName);
    }

    public boolean hasFieldConflict(FieldReference reference)
    {
        return getFields().containsKey(reference.getQualifiedName());
    }

}
