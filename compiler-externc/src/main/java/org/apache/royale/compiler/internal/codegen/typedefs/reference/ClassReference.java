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

package org.apache.royale.compiler.internal.codegen.typedefs.reference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.internal.codegen.typedefs.pass.AbstractCompilerPass;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.DebugLogUtils;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.JSTypeUtils;
import org.apache.royale.compiler.internal.codegen.typedefs.DummyNode;
import org.apache.royale.compiler.problems.UnresolvedClassReferenceProblem;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.TemplatizedType;

public class ClassReference extends BaseReference
{
    private boolean isFinal;
    private boolean isDynamic;
    private String moduleName;
    private int enumConstantCounter = 0;

    private Set<String> imports = new HashSet<String>();
    private MethodReference constructor;
    private Map<String, FieldReference> instanceFields = new HashMap<String, FieldReference>();
    private Map<String, FieldReference> staticFields = new HashMap<String, FieldReference>();
    private Map<String, MethodReference> instanceMethods = new HashMap<String, MethodReference>();
    private Map<String, MethodReference> staticMethods = new HashMap<String, MethodReference>();

    private Node nameNode;

    private Node functionNode;

    @SuppressWarnings("unused")
    private Node paramListNode;

    private boolean isNamespace;

    public final int getEnumConstant()
    {
        return enumConstantCounter;
    }

    public final void nextEnumConstant()
    {
        enumConstantCounter++;
    }

    public void setIsNamespace(boolean isNamespace)
    {
        this.isNamespace = isNamespace;
    }

    public boolean isNamespace()
    {
        return isNamespace;
    }

    public MethodReference getConstructor()
    {
        return constructor;
    }

    public ArrayList<FieldReference> getAllFields()
    {
        ArrayList<FieldReference> allMethods = new ArrayList<FieldReference>();
        if (!isInterface())
            allMethods.addAll(staticFields.values());
        allMethods.addAll(instanceFields.values());
        Collections.sort(allMethods);
        return allMethods;
    }

    public ArrayList<MethodReference> getAllMethods()
    {
        ArrayList<MethodReference> allMethods = new ArrayList<MethodReference>();
        if (!isInterface())
            allMethods.addAll(staticMethods.values());
        allMethods.addAll(instanceMethods.values());
        Collections.sort(allMethods);
        return allMethods;
    }

    public FieldReference getStaticField(String name)
    {
        return staticFields.get(name);
    }

    public FieldReference getInstanceField(String name)
    {
        return instanceFields.get(name);
    }

    public MethodReference getStaticMethod(String name)
    {
        return staticMethods.get(name);
    }

    public MethodReference getInstanceMethod(String name)
    {
        return instanceMethods.get(name);
    }

    public boolean isDynamic()
    {
        return isDynamic;
    }

    public void setDynamic(boolean isDynamic)
    {
        this.isDynamic = isDynamic;
    }

    public boolean isFinal()
    {
        return isFinal;
    }

    public void setFinal(boolean isFinal)
    {
        this.isFinal = isFinal;
    }

    public String getModuleName()
    {
        return moduleName;
    }

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    public final boolean isInterface()
    {
        return getComment().isInterface();
    }

    /**
     *
     * @param model
     * @param node (FUNCTION [NAME, PARAM_LIST, BLOCK]), or (ASSIGN [FUNCTION [NAME, PARAM_LIST, BLOCK]])
     * @param qualifiedName
     */
    public ClassReference(ReferenceModel model, Node node, String qualifiedName)
    {
        super(model, node, qualifiedName, node.getJSDocInfo());

        indent = "";

        nameNode = null;
        functionNode = null;
        paramListNode = null;

        if (comment.hasEnumParameterType())
        {
            /*
            var Foo = { ...

            VAR 35 [jsdoc_info: JSDocInfo]
                NAME FontFaceSetLoadStatus
                    OBJECTLIT
                        STRING_KEY LOADED
                            STRING loaded
                        STRING_KEY LOADING
                            STRING loading

             Or..

             foo.bar.baz.QualifiedEnum = { ...

             ASSIGN 50 [jsdoc_info: JSDocInfo]
                GETPROP
                    GETPROP
                        ...
                    STRING QualifiedEnum
                OBJECTLIT 50
             */

            String overrideStringType = JSTypeUtils.toEnumTypeString(this);

            Node objLit = null;
            if (node.isVar())
            {
                objLit = node.getFirstChild().getFirstChild();
            }
            else if (node.isAssign())
            {
                objLit = node.getLastChild();
            }

            if (objLit != null)
            {
                for (Node stringKey : objLit.children())
                {
                    if (stringKey.isStringKey())
                    {
                        Node valueNode = stringKey.getFirstChild();

                        JSDocInfoBuilder b = new JSDocInfoBuilder(true);
                        JSDocInfo fieldComment = b.build();
                        String fieldName = stringKey.getString();
                        FieldReference field = addField(stringKey, fieldName, fieldComment, true);
                        field.setConst(true);
                        field.setOverrideStringType(overrideStringType);
                        field.setConstantValueNode(valueNode);
                    }
                }
            }
        }
        else if (comment.getTypedefType() != null)
        {
            JSTypeExpression typeDefType = comment.getTypedefType();
            JSType typeDefJSType = model.evaluate(typeDefType);
            if (typeDefJSType != null)
            {
                ObjectType typeDefObjectType = typeDefJSType.toObjectType();
                if (typeDefObjectType != null)
                {
                    Map<String,JSType> properties = typeDefObjectType.getPropertyTypeMap();
                    for (Map.Entry<String, JSType> property : properties.entrySet())
                    {
                        JSDocInfoBuilder b = new JSDocInfoBuilder(true);
                        b.recordBlockDescription("Generated doc for missing field JSDoc.");
                        JSDocInfo fieldComment = b.build();
                        addField(node, property.getKey(), fieldComment, false);
                    }
                }
            }
            //System.out.println(node.toStringTree());
            /*
             VAR 727 [jsdoc_info: JSDocInfo] [source_file: [w3c_rtc]] [length: 21]
                NAME MediaConstraints 727 [source_file: [w3c_rtc]] [length: 16]
             */
        }
        else if (comment.isConstant())
        {
            /*
             VAR 882 [jsdoc_info: JSDocInfo]
                NAME Math
                    OBJECTLIT
             */
            constructor = new NullConstructorReference(model, this, node, getBaseName(), comment);
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
        else if (node.isAssign() && node.getChildAtIndex(1).isFunction())
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
            constructor = new MethodReference(model, this, functionNode, getBaseName(), comment, false);
        }

        moduleName = model.getConfiguration().isNamedModule(this);

    }

    private static List<String> definedPackages = new ArrayList<String>();

    @Override
    public void emit(StringBuilder sb)
    {
        enumConstantCounter = 0;

        String packageName = getPackageName();

        if (outputJS)
        {
            sb.append("/** @fileoverview Auto-generated Externs files\n * @externs\n */\n");
            if (!packageName.isEmpty())
            {
                if (!definedPackages.contains(packageName))
                {
                    definedPackages.add(packageName);
                    String[] pieces = packageName.split("\\.");
                    String chain = "";
                    int n = pieces.length;
                    for (int i = 0; i < n; i++)
                    {
                        String piece = pieces[i];
                        sb.append("\n");
                        sb.append("\n");
                        sb.append("/**\n * @const\n * @suppress {duplicate|const} */\n");
                        if (chain.isEmpty())
                            sb.append("var " + piece + " = {};\n\n\n");
                        else
                            sb.append(chain + "." + piece + " = {}\n\n\n");
                        chain = chain + "." + piece;
                    }
                }
            }
        }
        else
        {
            sb.append("package ");
            if (!packageName.equals(""))
                sb.append(packageName).append(" ");
            sb.append("{\n");
            sb.append("\n");

            emitImports(sb);
        }

        if (moduleName != null)
        {
            sb.append("[JSModule");
            if (packageName.length() > 0 || !getBaseName().equals(moduleName))
            {
                sb.append("(");
                sb.append("name=\"");
                sb.append(moduleName);
                sb.append("\"");
                sb.append(")");
            }
            sb.append("]");
            sb.append("\n");
        }

        emitComment(sb);

        boolean isInterface = isInterface();

        if (isInterface)
        {
            emitInterface(sb);
        }
        else
        {
            emitClass(sb);
        }

        if (!outputJS)
        {
            sb.append("{\n");
            sb.append("\n");
        }

        if (!isInterface)
        {
            emitConstructor(sb);
            sb.append("\n");
        }

        emitFields(sb);
        emitMethods(sb);

        if (!outputJS)
        {
            sb.append("}\n");
            sb.append("}\n"); // package
        }
    }

    public boolean hasSuperField(String fieldName)
    {
        List<ClassReference> list = getSuperClasses();
        for (ClassReference reference : list)
        {
            if (reference.hasInstanceField(fieldName))
                return true;
        }
        return false;
    }

    public boolean hasSuperMethod(String methodName)
    {
        List<ClassReference> list = getSuperClasses();
        for (ClassReference reference : list)
        {
            if (reference.hasInstanceMethod(methodName))
                return true;
        }
        return false;
    }

    public MethodReference getSuperMethod(String methodName)
    {
        List<ClassReference> list = getSuperClasses();
        for (ClassReference reference : list)
        {
            if (reference.hasInstanceMethod(methodName))
                return reference.getInstanceMethod(methodName);
        }

        list = getAllImplInterfaces(); // return all our interfaces and all superclass
        for (ClassReference reference : list)
        {
            if (reference.hasInstanceMethod(methodName))
                return reference.getInstanceMethod(methodName);
        }

        return null;
    }

    public List<ClassReference> getSuperClasses()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        if (isInterface())
        {
        	return getExtendedInterfaces();
        }
        ClassReference superClass = getSuperClass();
        while (superClass != null)
        {
            result.add(superClass);
            superClass = superClass.getSuperClass();
        }
        return result;
    }

    public List<ClassReference> getAllImplInterfaces()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        for (JSTypeExpression jsTypeExpression : getComment().getImplementedInterfaces())
        {
            String interfaceName = getModel().evaluate(jsTypeExpression).getDisplayName();
            ClassReference classReference = getModel().getClassReference(interfaceName);
            if (classReference != null)
                result.add(classReference);
        }
        Collections.sort(result);
        return result;
    }

    public List<ClassReference> getImplementedInterfaces()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        for (JSTypeExpression jsTypeExpression : getComment().getImplementedInterfaces())
        {
            String interfaceName = getModel().evaluate(jsTypeExpression).getDisplayName();
            ClassReference reference = getModel().getClassReference(interfaceName);
            if (reference != null)
                result.add(reference);
        }
        Collections.sort(result);
        return result;
    }

    public List<ClassReference> getExtendedInterfaces()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        for (JSTypeExpression jsTypeExpression : getComment().getExtendedInterfaces())
        {
            String interfaceName = getModel().evaluate(jsTypeExpression).getDisplayName();
            ClassReference reference = getModel().getClassReference(interfaceName);
            if (reference != null)
                result.add(reference);
        }
        Collections.sort(result);
        return result;
    }

    public List<ClassReference> getInterfaces()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        List<JSTypeExpression> implementedInterfaces = getComment().getImplementedInterfaces();
        for (JSTypeExpression jsTypeExpression : implementedInterfaces)
        {
            JSType jsType = getModel().evaluate(jsTypeExpression);
            if (jsType.isTemplatizedType())
            {
            	jsType = ((TemplatizedType)jsType).getReferencedType();
            }
            String interfaceName = jsType.getDisplayName();
            ClassReference interfaceReference = getModel().getClassReference(interfaceName);
            if (interfaceReference != null)
            	result.add(interfaceReference);
            else
            {
            	DummyNode node = new DummyNode();
            	String externName = AbstractCompilerPass.getSourceFileName(this.getNode().getStaticSourceFile().getName(), getModel());
            	node.setSourcePath(externName);
            	node.setLine(this.getNode().getLineno());
            	UnresolvedClassReferenceProblem problem = new UnresolvedClassReferenceProblem(node, interfaceName);
            	getModel().problems.add(problem);
            }
        }
        Collections.sort(result);
        return result;
    }

    public List<ClassReference> getSuperInterfaces()
    {
        ArrayList<ClassReference> result = new ArrayList<ClassReference>();
        result.addAll(getInterfaces());

        ClassReference superClass = getSuperClass();
        while (superClass != null)
        {
            result.addAll(superClass.getInterfaces());
            superClass = superClass.getSuperClass();
        }
        Collections.sort(result);
        return result;
    }

    public boolean hasInstanceField(String fieldName)
    {
        return instanceFields.containsKey(fieldName);
    }

    public boolean hasStaticField(String fieldName)
    {
        return staticFields.containsKey(fieldName);
    }

    public boolean hasInstanceMethod(String fieldName)
    {
        return instanceMethods.containsKey(fieldName);
    }

    public boolean hasStaticMethod(String fieldName)
    {
        return staticMethods.containsKey(fieldName);
    }

    public FieldReference addField(Node node, String fieldName, JSDocInfo comment, boolean isStatic)
    {
        if (isStatic ? hasStaticField(fieldName) : hasInstanceField(fieldName))
        {
            // XXX Warning
            return null;
        }

        /* AJH This doesn't make sense to me
        if (isNamespace)
            isStatic = false;
        */

        if (comment == null)
        {
            DebugLogUtils.err("Field comment null for; " + node.getQualifiedName());
            //DebugLogUtils.err(node);
            JSDocInfoBuilder b = new JSDocInfoBuilder(true);
            b.recordBlockDescription("Generated doc for missing field JSDoc.");
            comment = b.build();
        }

        FieldReference field = new FieldReference(getModel(), this, node, fieldName, comment, isStatic);

        if (isStatic)
            staticFields.put(fieldName, field);
        else
            instanceFields.put(fieldName, field);
        return field;
    }

    public MethodReference addMethod(Node node, String functionName, JSDocInfo comment, boolean isStatic)
    {
        /* AJH This doesn't make sense to me
        if (isNamespace)
            isStatic = false;
		*/

        if (comment == null)
        {
            DebugLogUtils.err("Method comment null for; " + node.getQualifiedName());
            //DebugLogUtils.err(node);
            JSDocInfoBuilder b = new JSDocInfoBuilder(true);
            b.recordBlockDescription("Generated doc for missing method JSDoc.");
            comment = b.build();
        }

        MethodReference method = new MethodReference(getModel(), this, node, functionName, comment, isStatic);

        if (isStatic)
        {
            staticMethods.put(functionName, method);
        }
        else if (getQualifiedName().equals("Object") && functionName.equals("toString"))
        {
            // skipping Object.prototype.toString() allows toString(opt_radix) for Number, int and uint
        }
        else if (getQualifiedName().equals("Object") && functionName.equals("toJSON"))
        {
            // skipping Object.prototype.toJSON().  Doesn't seem to be in the spec and excluding
        	// in the config seems to also block Date.toJSON
        }
        else
        {
            instanceMethods.put(functionName, method);
        }
        return method;
    }

    public boolean isMethodOverrideFromInterface(MethodReference reference)
    {
        boolean isMethodOverrideFromInterface = false;

        if (!hasImplementations())
        {
            List<JSTypeExpression> implementedInterfaces = getComment().getImplementedInterfaces();
            for (JSTypeExpression jsTypeExpression : implementedInterfaces)
            {
                String interfaceName = getModel().evaluate(jsTypeExpression).getDisplayName();
                ClassReference classReference = getModel().getClassReference(interfaceName);
                if (classReference.hasSuperMethod(reference.getQualifiedName()))
                {
                    isMethodOverrideFromInterface = true;
                    break;
                }
            }
        }

        return isMethodOverrideFromInterface;
    }

    public MethodReference getMethodOverrideFromInterface(MethodReference reference)
    {
        // get all super classes, reverse and search top down
        List<ClassReference> superClasses = getSuperClasses();
        superClasses.add(0, this);
        Collections.reverse(superClasses);

        // for each superclass, get all implemented interfaces
        for (ClassReference classReference : superClasses)
        {
            List<ClassReference> interfaces = classReference.getImplementedInterfaces();
            for (ClassReference interfaceReference : interfaces)
            {
                // check for the method on the interface
                MethodReference method = interfaceReference.getInstanceMethod(reference.getBaseName());
                if (method != null)
                    return method;
            }
        }

        return null;
    }

    public ClassReference getSuperClass()
    {
        if (getBaseName().equals("Object"))
            return null;

        JSTypeExpression baseType = getComment().getBaseType();
        if (baseType != null)
        {
            JSType jsType = getModel().evaluate(baseType);
            if (jsType != null)
                return getModel().getClassReference(jsType.getDisplayName());
        }
        else
        {
            return getModel().getObjectReference();
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

    public boolean isPropertyInterfaceImplementation(String fieldName)
    {
        List<ClassReference> superInterfaces = getSuperInterfaces();
        for (ClassReference interfaceRef : superInterfaces)
        {
            if (interfaceRef == null)
            {
                System.err.println("isPropertyInterfaceImplementation() null");
                continue;
            }
            if (interfaceRef.hasInstanceField(fieldName))
                return true;
        }
        return false;
    }

    public boolean hasLocalMethodConflict(String functionName)
    {
        return instanceMethods.containsKey(functionName) || staticMethods.containsKey(functionName);
    }

    public void addImport(ClassReference reference)
    {
        if (reference != null)
        {
            imports.add(reference.getQualifiedName());
        }
    }

    public boolean hasImport(String qualifiedName)
    {
        return imports.contains(qualifiedName);
    }

    private boolean hasImplementations()
    {
        return getComment().getImplementedInterfaceCount() > 0;
    }

    private void emitImports(StringBuilder sb)
    {
        if (imports.size() > 0)
        {
            for (String anImport : imports)
            {
                sb.append("import ").append(anImport).append(";\n");
            }
            sb.append("\n");
        }
    }

    @Override
    protected void emitCommentBody(StringBuilder sb)
    {
        super.emitCommentBody(sb);
        super.emitParams(sb);
        if (isInterface())
            sb.append(" * @interface\n");
        else
            sb.append(" * @constructor\n");
        if (getComment().hasBaseType())
        {
        	sb.append(" * @");
            emitSuperClass(sb);
            sb.append("\n");
        }
        if (!isInterface())
        {
        	if (!outputJS)
        		sb.append(" * @");
            emitImplements(sb);
            sb.append("\n");
            List<JSTypeExpression> implementedInterfaces = getComment().getImplementedInterfaces();
            int len = implementedInterfaces.size();
            if (len != 0)
            {
                for (int i = 0; i < len; i++)
                {
                    String value = getModel().evaluate(implementedInterfaces.get(i)).getDisplayName();

                    if (value.equals("IArrayLike"))
                    {
                    	String comment = getComment().getOriginalCommentString();
                    	int c = comment.indexOf("IArrayLike");
                    	int c1 = comment.indexOf('<', c);
                    	if (c1 == c + 10)
                    	{
                    		int c2 =  comment.indexOf('>', c1);
                    		if (c2 != -1)
                    		{
                    			String type = comment.substring(c1 + 1, c2);
                    			int insert = sb.toString().indexOf("\n");
                    			sb.insert(insert + 1, "\n[ArrayElementType(\"" + JSTypeUtils.transformType(type) + "\")]\n");
                    		}
                    	}
                    	break;
                    }
                }           	
            }
        }
    }

    private void emitClass(StringBuilder sb)
    {
        if (outputJS)
            return;

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
        sb.append(getBaseName()).append(" ");

        if (getComment().hasBaseType())
        {
            emitSuperClass(sb);
            sb.append(" ");
        }
        else
        {
            // XXX JSObject extends
            //sb.append("extends JSObject ");
        }

        if (!isInterface())
        {
            emitImplements(sb);
        }
    }

    private void emitInterface(StringBuilder sb)
    {
        sb.append("public interface ");

        sb.append(getBaseName()).append(" ");

        List<JSTypeExpression> extendedInterfaces = getComment().getExtendedInterfaces();
        int len = extendedInterfaces.size();
        if (len > 0)
        {
            sb.append("extends ");
            for (JSTypeExpression jsTypeExpression : extendedInterfaces)
            {
                String value = getModel().evaluate(jsTypeExpression).toString();
                sb.append(value);
                if (--len > 0)
                    sb.append(", ");
            }
            sb.append(" ");
        }
    }

    private void emitSuperClass(StringBuilder sb)
    {
        if (outputJS)
        {
            sb.append("extends ");
            String value = JSTypeUtils.toClassTypeString(this);
            sb.append(value);
            sb.append("\n");
        }
        else
        {
            sb.append("extends ");
            String value = JSTypeUtils.toClassTypeString(this);
            sb.append(value);
        }
    }

    private void emitImplements(StringBuilder sb)
    {
        List<JSTypeExpression> implementedInterfaces = getComment().getImplementedInterfaces();
        if (implementedInterfaces.size() == 0)
            return;

        if (outputJS)
            sb.append(" * @implements ");
        else
            sb.append("implements ");

        int len = implementedInterfaces.size();
        for (int i = 0; i < len; i++)
        {
            String value = getModel().evaluate(implementedInterfaces.get(i)).getDisplayName();

            sb.append(value);
            if (outputJS)
            {
            	if (i < len - 1)
            		sb.append("\n * @implements ");
            	else
            		sb.append("\n");
            }
            else
            {
                if (i < len - 1)
                    sb.append(", ");
            }
        }

        sb.append(" ");
    }

    private void emitConstructor(StringBuilder sb)
    {
        if (constructor != null)
        {
            constructor.emit(sb);
        }
    }

    private void emitFields(StringBuilder sb)
    {
        for (FieldReference field : getAllFields())
        {
            field.emit(sb);
            sb.append("\n");
            nextEnumConstant();
        }
    }

    private void emitMethods(StringBuilder sb)
    {
        for (MethodReference method : getAllMethods())
        {
            method.emit(sb);
            sb.append("\n");
        }
    }

    public File getFile(File asSourceRoot)
    {
        File jsRoot = getModel().getConfiguration().getJsRoot();
        if (jsRoot == null)
        {
            String packagePath = toPackagePath();
            return new File(asSourceRoot, packagePath + File.separator + getBaseName() + ".as");
        }

        return new File(jsRoot, getBaseName() + ".js");
    }

    private String toPackagePath()
    {
        String packageName = getPackageName();

        String[] cname = packageName.split("\\.");
        String sdirPath = "";
        if (cname.length > 0)
        {
            for (final String aCname : cname)
            {
                sdirPath += aCname + File.separator;
            }

            return sdirPath;
        }

        return "";
    }
}
