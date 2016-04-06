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

package org.apache.flex.compiler.internal.as.codegen;

import static org.apache.flex.abc.ABCConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.flex.abc.ABCConstants;
import org.apache.flex.abc.visitors.IABCVisitor;
import org.apache.flex.abc.visitors.IClassVisitor;
import org.apache.flex.abc.visitors.IMetadataVisitor;
import org.apache.flex.abc.visitors.IMethodBodyVisitor;
import org.apache.flex.abc.visitors.IMethodVisitor;
import org.apache.flex.abc.visitors.IScriptVisitor;
import org.apache.flex.abc.visitors.ITraitVisitor;
import org.apache.flex.abc.visitors.ITraitsVisitor;
import org.apache.flex.abc.graph.IBasicBlock;
import org.apache.flex.abc.semantics.ClassInfo;
import org.apache.flex.abc.semantics.ExceptionInfo;
import org.apache.flex.abc.semantics.InstanceInfo;
import org.apache.flex.abc.semantics.Instruction;
import org.apache.flex.abc.semantics.Label;
import org.apache.flex.abc.semantics.Metadata;
import org.apache.flex.abc.semantics.MethodBodyInfo;
import org.apache.flex.abc.semantics.MethodInfo;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.abc.semantics.Nsset;
import org.apache.flex.abc.semantics.PooledValue;
import org.apache.flex.abc.semantics.ScriptInfo;
import org.apache.flex.abc.semantics.Trait;
import org.apache.flex.abc.semantics.Traits;
import org.apache.flex.abc.instructionlist.InstructionList;
import org.apache.flex.compiler.constants.IMetaAttributeConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.internal.definitions.AmbiguousDefinition;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.FunctionDefinition;
import org.apache.flex.compiler.internal.definitions.PackageDefinition;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.AccessValue;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.ClassificationValue;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.SearchScopeValue;
import org.apache.flex.compiler.internal.legacy.ASScopeUtils;
import org.apache.flex.compiler.internal.legacy.MemberedDefinitionUtils;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IASScope;
import org.apache.flex.compiler.tree.as.IImportNode.ImportKind;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.abc.ABCEmitter;

/**
 * JSEmitter is used in two different phases. During compilation it acts as a
 * visitor that gets called for each layer in the AST (see Emitter*IVisitor
 * classes). The second phase is triggered by JSCompilationUnit's
 * handleABCBytesRequest() and uses emitCode() for building the JavaScript code
 * (see emit* methods). This implementation is part of FalconJS. For more
 * details on FalconJS see org.apache.flex.compiler.JSDriver.
 */
@SuppressWarnings("nls")
public class JSEmitter implements IABCVisitor
{
    protected JSOutputStream w = new JSOutputStream();
    protected Set<String> m_importNames = new TreeSet<String>();
    protected Set<String> m_useNames = new TreeSet<String>();

    protected Vector<EmitterClassVisitor> definedClasses = new Vector<EmitterClassVisitor>();
    private Vector<MethodInfo> methodInfos = new Vector<MethodInfo>();
    private Vector<MethodBodyInfo> methodBodies = new Vector<MethodBodyInfo>();
    private Vector<ScriptInfo> scriptInfos = new Vector<ScriptInfo>();
    protected final Boolean emitExterns = JSSharedData.m_useClosureLib && false; // only set emitExterns to true if you want to generate extern files, i.e. svg.js.

    private JSSharedData m_sharedData;
    private EmitterClassVisitor m_currentClassVisitor;
    private String m_methodPrologue = "";
    private String m_methodPostlogue = "";
    private static String EXTERN_PREFIX = "// EXTERN ";
    // private ICompilationUnit.Operation m_buildPhase;
    private IABCVisitor m_visitor = null;
    protected ICompilerProject m_project = null;
    protected JSGenerator m_generator = null;
    private IASScope m_currentScope = null;
    protected Map<MethodInfo, FunctionDefinition> m_methodInfoToDefinition = new HashMap<MethodInfo, FunctionDefinition>();
    protected Map<FunctionDefinition, MethodInfo> m_definitionToMethodInfo = new HashMap<FunctionDefinition, MethodInfo>();
    protected String m_packageName = null;

    public JSEmitter(JSSharedData sharedData,
                     ICompilationUnit.Operation buildPhase,
                     ICompilerProject project,
                     JSGenerator generator)
    {
        this.w = new JSOutputStream();
        m_sharedData = sharedData;
        m_currentClassVisitor = null;
        // m_buildPhase = buildPhase;
        m_project = project;
        m_generator = generator;
    }

    protected void writeString(String str)
    {
        w.writeString(str);
    }

    public void setVisitor(IABCVisitor visitor)
    {
        m_visitor = visitor;
    }

    public byte[] emit()
            throws Exception
    {
        // Metadata
        /*
         * for (Metadata md : metadataPool.getValues()) { // name final String
         * name = md.getName(); // items count assert md.getKeys().length ==
         * md.getValues().length; // metadata keys for (final String key :
         * md.getKeys()) { writeString( "// META[" + name + "]: key=" + key +
         * "\n" ); } // metadata values for (final String value :
         * md.getValues()) { writeString( "// META[" + name + "]: value=" +
         * value + "\n" ); } }
         */
        m_currentClassVisitor = null;

        // first write out the framework class
        EmitterClassVisitor frameWorkClassVisitor = null;
        for (EmitterClassVisitor clz : this.definedClasses)
        {
            InstanceInfo ii = clz.instanceInfo;
            if (!ii.isInterface() && JSGeneratingReducer.getBasenameFromName(ii.name).equals(JSSharedData.JS_FRAMEWORK_NAME))
            {
                m_currentClassVisitor = clz;
                final IDefinition def = getDefinition(ii.name);
                final String packageName = def.getPackageName();
                if (packageName.isEmpty())
                {
                    frameWorkClassVisitor = clz;
                    emitClass(clz);
                }
            }
        }

        // write out all the other classes
        for (EmitterClassVisitor clz : this.definedClasses)
        {
            if (clz != frameWorkClassVisitor)
            {
                m_currentClassVisitor = clz;

                if (clz.instanceInfo.isInterface())
                    emitInterface(clz);
                else
                    emitClass(clz);
            }
        }

        // static initializers?
        /*
         * for (EmitterClassVisitor clz : this.definedClasses) {
         * m_currentClassVisitor = clz;
         * w.writeU30(getMethodId(clz.classInfo.cInit));
         * emitTraits(clz.classTraits); }
         */

        m_currentClassVisitor = null;

        // write out free floating code: package functions and package variables. 
        for (ScriptInfo si : this.scriptInfos)
        {
            emitScriptInfo(si);
        }

        return w.toByteArray();
    }

    protected MethodBodyInfo findMethodBodyInfo(MethodInfo mi)
    {
        for (int i = 0; i < this.methodBodies.size(); i++)
        {
            MethodBodyInfo mbi = methodBodies.elementAt(i);
            if (mbi.getMethodInfo() == mi)
                return mbi;
        }

        // Don't throw, because interfaces don't have  MethodBodyInfo:
        // throw new IllegalArgumentException("Unable to find MethodBodyInfo for " + mi);
        return null;
    }

    protected String emitParameters(MethodInfo mi) throws Exception
    {
        final FunctionDefinition fdef = this.m_methodInfoToDefinition.get(mi);
        String[] chunk = JSGeneratingReducer.emitParameters(m_project, fdef, mi);
        // String[] chunk = JSGeneratingReducer.emitParameters(mi);

        final String a_priori_insns = chunk[0];
        final String code = chunk[1];

        writeString(code);
        return a_priori_insns;
    }

    protected void emitInstanceTraits(Traits traits, MethodInfo ctor, String packageName, String className, String superClass,
                                        Boolean isExtern, Boolean isInterface, Boolean isPackageFunction) throws Exception
    {
        // private void emitTraits(Traits traits, Boolean isInstanceTraits, MethodInfo ctor, String packageName, String className, String methodPrefix, String assignmentOp, String separator, String indent ) throws Exception

        if (JSSharedData.m_useClosureLib)
        {
            if (isExtern)
                emitTraits(traits, true, isExtern, isInterface, isPackageFunction, ctor, packageName, className, superClass, className + ".prototype.", "=", ";", EXTERN_PREFIX);
            else
            {
                final String fullName = createFullName(packageName, className);
                emitTraits(traits, true, isExtern, isInterface, isPackageFunction, ctor, packageName, className, superClass, fullName + ".prototype.", "=", ";", "");
            }
        }
        else
            emitTraits(traits, true, isExtern, isInterface, isPackageFunction, ctor, packageName, className, superClass, "this.", "", ",", isExtern ? EXTERN_PREFIX : "    ");
    }

    protected Boolean isExtern(Name name)
    {
        if (name != null)
        {
            final IDefinition def = JSGeneratingReducer.getDefinitionForName(m_project, name);
            if (def == null)
            {
                JSGeneratingReducer.getDefinitionForName(m_project, name);
                throw new IllegalArgumentException("isExtern: can't find definition for name " + name.toString());
            }

            if (def.getMetaTagByName(JSSharedData.EXTERN_METATAG) != null)
                return true;
        }
        return false;
    }

    /** packageName can be "" */
    protected IDefinition getDefinition(Name name)
    {
        if (name == null)
            return null;

        final IDefinition def = JSGeneratingReducer.getDefinitionForNameByScope(m_project, m_project.getScope(), name);

        if (def == null)
        {
            throw new IllegalArgumentException("getDefinition: can't find definition for name " + name.toString());
        }

        return def;
    }

    /** packageName can be "" */
    private ClassDefinition getClassDefinition(String shortClassName, String packageName)
    {
        if (packageName.isEmpty())
            return getClassDefinition(shortClassName);
        else
            return getClassDefinition(packageName + "." + shortClassName);
    }

    /** expects className to be fully-qualified if it's in a package */
    private ClassDefinition getClassDefinition(String className)
    {
        IDefinition definitionContext = null;
        ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES, SearchScopeValue.ALL_SCOPES, AccessValue.ALL, definitionContext);
        IDefinition def = ASScopeUtils.findDefinitionByName(m_project.getScope(), m_project, className, filter);
        if (def instanceof ClassDefinition)
        {
            final ClassDefinition classDef = (ClassDefinition)def;
            return classDef;
        }
        return null;
    }

    // Symbol support
    private Boolean isSymbolClass(String className)
    {
        final ClassDefinition classDef = getClassDefinition(className);
        if (classDef != null)
        {
            for (String implementedInterface : classDef.getImplementedInterfacesAsDisplayStrings())
            {
                if (implementedInterface.equals(JSSharedData.SYMBOL_INTERFACE_NAME))
                    return true;
            }
        }
        return false;
    }

    private void emitClassInfo(String packageName, String className, String superClass, Boolean isDynamic, Boolean isFinal)
    {
        final String fullClassNameWithoutDot = createFullName(packageName, className);
        String fullClassName = fullClassNameWithoutDot;
        if (!fullClassName.isEmpty())
            fullClassName += ".";

        if (superClass.isEmpty())
            superClass = "Object";

        // Namespace support
        final IDefinition def = JSSharedData.instance.getDefinition(fullClassNameWithoutDot);
        if (def != null)
        {
            def.getQualifiedName();
        }
    }

    private void emitClassTraits(Traits traits, MethodInfo staticInitMI,
                                String packageName, String className, String superClass,
                                Boolean isExtern, Boolean isPackageFunction,
                                Boolean isDynamicClass, Boolean isFinalClass,
                                Boolean emitClassPrologue) throws Exception
    {
        final Boolean isInterface = false;
        writeString("\n\n");

        // TODO: generalize
        if (isExtern || packageName.startsWith("goog"))
            return;

        if (emitClassPrologue && !className.isEmpty())
        {
            emitClassInfo(packageName, className, superClass, isDynamicClass, isFinalClass);
        }

        String fullClassName = createFullName(packageName, className);
        if (!fullClassName.isEmpty())
            fullClassName += ".";

        emitTraits(traits, false, isExtern, isInterface, isPackageFunction, staticInitMI, packageName.replaceFirst(JSSharedData.ROOT_NAME, ""), className, superClass, fullClassName, "=", ";", "");

        /*
         * if( isExtern ) emitTraits( traits, false, isExtern, staticInitMI,
         * packageName, className, superClass, fullClassName, "=", ";", "" );
         * else emitTraits( traits, false, isExtern, staticInitMI,
         * packageName.replaceFirst( JSSharedData.ROOT_NAME, ""), className,
         * superClass, fullClassName, "=", ";", "" );
         */
    }

    private String nameToString(Name name)
    {
        String baseName = JSGeneratingReducer.getBasenameFromName(name);
        if (baseName == null || baseName.isEmpty())
        {
            String packageName = "";
            final Nsset nsset = name.getQualifiers();
            if (nsset != null && nsset.length() > 0)
            {
                packageName = nsset.iterator().next().getName();
                if (!packageName.isEmpty())
                    baseName = "{" + packageName + "}::" + baseName;
            }
        }
        else
        {
            final IDefinition def = getDefinition(name);
            if (name != null && !name.isTypeName())
            {
                final String packageName = def.getPackageName();
                if (packageName != null && !packageName.isEmpty())
                {
                    final String fullName = "{" + packageName + "}::" + baseName;
                    return fullName;
                }
            }
        }
        return baseName;

    }

    // see Namespace::getKindString
    /*
     * private String getKindString(Namespace ns) { switch(ns.getKind()) { case
     * ABCConstants.CONSTANT_Namespace: return "Ns"; case
     * ABCConstants.CONSTANT_PackageNs: return "PackageNs"; case
     * ABCConstants.CONSTANT_PackageInternalNs: return "PackageInternalNs"; case
     * ABCConstants.CONSTANT_ProtectedNs: return "ProtectedNs"; case
     * ABCConstants.CONSTANT_ExplicitNamespace: return "ExplicitNs"; case
     * ABCConstants.CONSTANT_StaticProtectedNs: return "StaticProtectedNs"; case
     * ABCConstants.CONSTANT_PrivateNs: return "PrivateNs"; } return "UnknownNs"
     * ; }
     */

    private String mangleName(String baseName, Namespace ns)
    {
        String name = baseName + "::";
        String nsStr = ns.toString();

        // workaround for Falcon bug.
        // ns.toString() is now returning:
        //      ProtectedNs:"tests:Test"
        // instead of
        //      ProtectedNs:"tests.Test"
        if (ns.getName().contains(":") /*
                                        * && ns.getKind() !=
                                        * ABCConstants.CONSTANT_Namespace
                                        */)
        {
            String tmp = ns.getName();

            // support for "use namespace"
            /*
             * final INamespaceDefinition ins =
             * JSSharedData.instance.getNamespace(tmp); if( ins != null ) { tmp
             * = ins.getQualifiedName(); }
             */
            tmp = tmp.replaceAll(":", ".");

            // another workaround for a Falcon bug.
            // WRONG:       PrivateNs:"ClassPrivateNS:tests:Test"
            // CORRECT:     PrivateNs:ClassPrivateNS:"tests:Test"
            if (tmp.startsWith("ClassPrivateNS."))
                tmp = tmp.replace("ClassPrivateNS.", "ClassPrivateNS:");
            nsStr = nsStr.substring(0, nsStr.length() - ns.getName().length() - 2) + tmp;
        }
        nsStr = nsStr.replaceAll("\\\"", "");

        Boolean colon = false;
        for (String part : nsStr.split(":"))
        {
            if (colon)
                name += ":";
            else
                colon = true;
            part = part.replaceAll("\\\"", "");

            if (part.equals("Ns"))
                name += JSSharedData.CONSTANT_Namespace;
            else if (part.equals("PackageNs"))
                name += JSSharedData.CONSTANT_PackageNs;
            else if (part.equals("PackageInternalNs"))
                name += JSSharedData.CONSTANT_PackageInternalNs;
            else if (part.equals("ProtectedNs"))
                name += JSSharedData.CONSTANT_ProtectedNs;
            else if (part.equals("ExplicitNs"))
                name += JSSharedData.CONSTANT_ExplicitNamespace;
            else if (part.equals("StaticProtectedNs"))
                name += JSSharedData.CONSTANT_StaticProtectedNs;
            else if (part.equals("PrivateNs"))
                name += JSSharedData.CONSTANT_PrivateNs;
            else if (part.equals("ClassPrivateNS"))
                name += JSSharedData.CONSTANT_ClassPrivateNS;
            else
                name += part;
        }

        return name;
    }

    private void emitNamespaceInfo(EmitterClassVisitor clz,
            String packageName, String className, String superClass) throws Exception
    {
        // check whether this class is tagged with [ClassInfo]
        if (!className.isEmpty())
        {
            final Traits instanceTraits = clz.instanceTraits;
            final String fullName = createFullName(packageName, className);
            boolean comma = false;
            String names = "";
            for (Trait t : instanceTraits)
            {
                final Name name = t.getNameAttr("name");
                final Nsset nsset = name.getQualifiers();
                if (nsset != null && nsset.length() > 0)
                {
                    if (comma)
                        names += ",\n";
                    else
                        comma = true;
                    for (Namespace ns : nsset)
                    {
                        names += "    \"" + mangleName(name.getBaseName(), ns) + "\" : true";
                    }
                }
            }

            // add _NAMESPACES member to class 
            writeString("\n");
            writeString("/**\n");
            writeString(" * Member: " + fullName + "._NAMESPACES\n");
            writeString(" * @const\n");
            writeString(" * @type {Object}\n");
            writeString(" */\n");

            if (names.isEmpty())
                writeString(fullName + "._NAMESPACES = {};\n");
            else
            {
                writeString(fullName + "._NAMESPACES = {\n");
                writeString(names + "\n");
                writeString("}\n");
            }

            // add _USES member to class 
            names = "";
            comma = false;
            for (String uses : m_useNames)
            {
                final INamespaceDefinition ns = JSSharedData.instance.getNamespaceForQName(uses);
                if (ns != null)
                {
                    if (comma)
                        names += ",\n";
                    else
                        comma = true;
                    names += ("    \"" + ns.getURI().replaceAll(":", ".") + "\" : \"" + uses + "\"");
                }
            }

            if (!names.isEmpty())
            {
                writeString("\n");
                writeString("/**\n");
                writeString(" * Member: " + fullName + "._USES\n");
                writeString(" * @const\n");
                writeString(" * @type {Object}\n");
                writeString(" */\n");

                writeString(fullName + "._USES = {\n");
                writeString(names + "\n");
                writeString("}\n");
            }
        }
    }

    private Boolean needsClassInfo(Traits classTraits)
    {
        for (Trait t : classTraits)
        {
            final Vector<Metadata> metaData = t.getMetadata();
            for (Metadata md : metaData)
            {
                if (md.getName().equals(JSSharedData.GENERATE_CLASSINFO))
                    return true;
            }
        }
        return false;
    }

    /*
     * tests.Test._CLASSINFO = { name : "", isDynamic : true, isFinal : true,
     * isStatic : true, bases : [], traits : { bases : {}, interfaces : {},
     * constructor : { {type: "", optional:false}, ... }, variables : { name:
     * "", type: "", access: "readwrite", uri: "", metadata: { name : "", value
     * : { {key:"", value:""}, ... } } } accessors : { name: "", type: "",
     * access: "readwrite", declaredBy: "", uri: "", metadata: { name : "",
     * value : { {key:"", value:""}, ... } } }, methods : { name: "",
     * returnType: "", declaredBy: "", parameters: { }, uri: "", metadata: {
     * name : "", value : { {key:"", value:""}, ... } } }, metadata: { name :
     * "", value : { {key:"", value:""}, ... } } } }
     */
    private void emitJSONInfo(EmitterClassVisitor clz,
            String packageName, String className, String superClass) throws Exception
    {
        final Traits classTraits = clz.classTraits;
        // check whether this class is tagged with [ClassInfo]
        if (!className.isEmpty() && needsClassInfo(classTraits))
        {
            final Traits instanceTraits = clz.instanceTraits;
            final String fullName = createFullName(packageName, className);

            // add _CLASSINFO member to class 
            writeString("\n");
            writeString("/**\n");
            writeString(" * Member: " + fullName + "._CLASSINFO\n");
            writeString(" * @const\n");
            writeString(" * @type {Object}\n");
            writeString(" */\n");
            writeString(fullName + "._CLASSINFO =\n");
            writeString("{\n");

            final String indent = "    ";
            final String indent2 = "        ";
            final String indent3 = "            ";
            final String indent4 = "                ";
            final String indent5 = "                    ";

            // add name, bases, isDynamic, isFinal, isStatic
            final InstanceInfo iinfo = clz.instanceInfo;
            final Boolean isDynamic = !iinfo.isSealed();
            final Boolean isFinal = iinfo.isFinal();
            final Boolean isStatic = true;
            if (packageName.isEmpty())
                writeString(indent + "name : \"" + className + "\",\n");
            else
                writeString(indent + "name : \"{" + packageName + "}::" + className + "\",\n");
            writeString(indent + "bases : [\"Class\"],\n");
            writeString(indent + "isDynamic : " + (isDynamic ? "true" : "false") + ",\n");
            writeString(indent + "isFinal : " + (isFinal ? "true" : "false") + ",\n");
            writeString(indent + "isStatic : " + (isStatic ? "true" : "false") + ",\n");

            // traits
            writeString(indent + "traits:\n");
            writeString(indent + "{\n");

            // add traits.bases 
            writeString(indent2 + "bases:\n");
            writeString(indent2 + "{\n");

            ClassDefinition classDef = getClassDefinition(className, packageName);

            IClassDefinition superclassDef = classDef.resolveBaseClass(m_project);
            while (superclassDef != null)
            {
                final String superPackageName = superclassDef.getPackageName();
                if (superPackageName.isEmpty())
                    writeString(indent3 + superclassDef.getBaseName() + ": \"" + superclassDef.getBaseName() + "\",\n");
                else
                    writeString(indent3 + superclassDef.getBaseName() + ": \"{" + superPackageName + "}::" + superclassDef.getBaseName() + "\",\n");

                superclassDef = superclassDef.resolveBaseClass(m_project);
            }
            writeString(indent2 + "},\n");

            // add traits.interfaces
            writeString(indent2 + "interfaces:\n");
            writeString(indent2 + "{\n");
            for (Name iname : iinfo.interfaceNames)
            {
                writeString(indent3 + "\"" + nameToString(iname) + "\",\n");
            }
            writeString(indent2 + "},\n");

            // add traits.constructor 
            writeString(indent2 + "constructor:\n");
            writeString(indent2 + "{\n");
            final MethodInfo ctor = clz.instanceInfo.iInit;
            writeString(getParameterInfo(ctor, indent3));
            writeString(indent2 + "},\n");

            // add variables, accessors, methods
            List<String> variables = new ArrayList<String>();
            List<String> methods = new ArrayList<String>();
            Map<String, String> getters = new HashMap<String, String>();
            Map<String, String> setters = new HashMap<String, String>();

            for (Trait t : instanceTraits)
            {
                String str = new String();
                final Name name = t.getNameAttr("name");
                String propName = JSGeneratingReducer.getBasenameFromName(name);
                String propType = "";
                switch (t.getKind())
                {
                    // traits.variables
                    case TRAIT_Var:
                    case TRAIT_Const:
                    {
                        final Boolean isConst = t.getKind() == TRAIT_Const;
                        final Name type = t.getNameAttr(Trait.TRAIT_TYPE);
                        if (type == null)
                            propType = "Object";
                        else
                            propType = nameToString(type);// JSGeneratingReducer.getBasenameFromName(type);
                        str += indent3 + propName + ":\n";
                        str += indent3 + "{\n";
                        str += indent4 + "name : " + "\"" + propName + "\",\n";
                        str += indent4 + "type : " + "\"" + propType + "\",\n";
                        str += indent4 + "access : " + "\"" + (isConst ? "readonly" : "readwrite") + "\",\n";
                        // str += indent4 + "uri : " + "\"" + ??? + "\"\n";
                        // str += getMetaDataInfo( findMetadata(classTraits, propName, t.getKind()), indent4 );
                        str += getMetaDataInfo(t.getMetadata(), indent4);
                        str += indent3 + "}";
                        variables.add(str);
                        break;
                    }
                        // traits.accessors
                    case TRAIT_Getter:
                    case TRAIT_Setter:
                    {
                        final String declaredBy = "(unknown)"; // TODO
                        final String access = t.getKind() == TRAIT_Getter ? "readonly" : "readwrite";
                        str += indent3 + propName + ":\n";
                        str += indent3 + "{\n";
                        str += indent4 + "name : " + "\"" + propName + "\",\n";
                        str += indent4 + "type : " + "\"" + propType + "\",\n";
                        str += indent4 + "access : " + "\"" + access + "\",\n";
                        str += indent4 + "declaredBy : " + "\"" + declaredBy + "\",\n";
                        // str += indent4 + "uri : " + "\"" + ??? + "\"\n";
                        // str += getMetaDataInfo( findMetadata(classTraits, propName, t.getKind()), indent4 );
                        str += getMetaDataInfo(t.getMetadata(), indent4);
                        str += indent3 + "}";

                        if (t.getKind() == TRAIT_Getter)
                            getters.put(propName, str);
                        else
                            setters.put(propName, str);
                        break;
                    }
                        // traits.methods
                    case TRAIT_Method:
                    case TRAIT_Function:
                    {
                        final String declaredBy = "(unknown)"; // TODO
                        final MethodInfo mi = (MethodInfo)t.getAttr("method_id");
                        str += indent3 + propName + ":\n";
                        str += indent3 + "{\n";
                        str += indent4 + "name : " + "\"" + propName + "\",\n";
                        str += indent4 + "returnType : " + "\"" + nameToString(mi.getReturnType()) + "\",\n";
                        str += indent4 + "declaredBy : " + "\"" + declaredBy + "\",\n";
                        // str += indent4 + "uri : " + "\"" + ??? + "\"\n";
                        str += indent4 + "parameters:\n";
                        str += indent4 + "{\n";
                        str += getParameterInfo(mi, indent5);
                        str += indent4 + "},\n";
                        // str += getMetaDataInfo( findMetadata(classTraits, propName, t.getKind()), indent4 );
                        str += getMetaDataInfo(t.getMetadata(), indent4);
                        str += indent3 + "}";
                        methods.add(str);

                        break;
                    }
                }
            }

            // add variables
            writeString(indent2 + "variables:\n");
            writeString(indent2 + "{\n");
            for (String var : variables)
            {
                writeString(var + ",\n");
            }
            writeString(indent2 + "},\n");

            // add accessors
            writeString(indent2 + "accessors:\n");
            writeString(indent2 + "{\n");
            for (Map.Entry<String, String> entry : getters.entrySet())
            {
                final String s = setters.get(entry.getKey());
                if (s != null)
                    writeString(s + ",\n");
                else
                    writeString(entry.getValue() + ",\n");
            }
            writeString(indent2 + "},\n");

            // add methods
            writeString(indent2 + "methods:\n");
            writeString(indent2 + "{\n");
            for (String met : methods)
            {
                writeString(met + ",\n");
            }
            writeString("\n");
            writeString(indent2 + "},\n");

            // closing traits
            writeString(indent + "},\n");

            // add class metadata
            for (Trait t : classTraits)
            {
                if (t.getKind() == TRAIT_Class)
                    writeString(getMetaDataInfo(t.getMetadata(), indent));
            }

            writeString("}\n");
        }
    }

    /*
     * private Vector<Metadata> findMetadata( Traits classTraits, String name,
     * int traitKind ) { for( Trait t : classTraits ) { if( t.getKind() ==
     * traitKind ) { final Name n = t.getNameAttr("name"); if(
     * JSGeneratingReducer.getBasenameFromName(n).equals(name) ) return
     * t.getMetadata(); } } return new Vector<Metadata>(); }
     */

    private String getMetaDataInfo(Vector<Metadata> metaData, String indent)
    {
        String s = "";
        if (!metaData.isEmpty())
        {
            s += indent + "metadata:\n";
            s += indent + "{\n";
            for (Metadata md : metaData)
            {
                s += indent + "    " + md.getName() + ":\n";
                s += indent + "    {\n";
                s += indent + "        " + "name : \"" + md.getName() + "\",\n";
                final String[] keys = md.getKeys();
                if (keys.length > 0)
                {
                    final String[] values = md.getValues();

                    s += indent + "        " + "value:\n";
                    s += indent + "        " + "{\n";
                    for (int i = 0; i < keys.length; ++i)
                    {
                        s += indent + "            " + keys[i] + ": {key: \"" + keys[i] + "\", value: \"" + values[i] + "\"},\n";
                    }
                    s += indent + "        " + "}\n";
                }
                s += indent + "    },\n";
            }
            s += indent + "}\n";
        }
        return s;
    }

    private String getParameterInfo(MethodInfo mi, String indent)
    {
        String s = "";
        if (mi != null)
        {
            Vector<PooledValue> defaultValues = mi.getDefaultValues();
            Vector<Name> paramTypes = mi.getParamTypes();
            final int defaultsStartAt = paramTypes.size() - defaultValues.size();
            int current = 0;
            for (Name type : paramTypes)
            {
                final String optional = current >= defaultsStartAt ? "true" : "false";
                s += indent + "p" + current + ": {type: \"" + nameToString(type) + "\", optional: " + optional + "},\n";
                current++;
            }
        }
        return s;
    }

    /*
     * private void emitPackageNames() { writeString( "// Packages \n" ); // get
     * all package names from the shared data object Set<String> packages =
     * JSSharedData.instance.getPackages(); // "expand" the package names, i.e.
     * "com.adobe" expands to "com", "com.adobe". Set<String> expanded = new
     * TreeSet<String>(); for (String packageName : packages) { String path =
     * ""; String[] parts = packageName.split("\\."); for( String part: parts )
     * { if( path.length() == 0 ) path += part; else path += "." + part; if(
     * path.length() > 0 ) expanded.add( path ); } } // create an entry for each
     * expanded package name Iterator<String> it = expanded.iterator(); if(
     * it.hasNext() ) { while (it.hasNext()) { writeString(
     * JSSharedData.ROOT_NAME + it.next().toString() + " = {};\n" ); } }
     * writeString( "\n" ); }
     */

    private String createFullName(String packageName, String className)
    {
        String fullName = "";

        // root name
        if (!JSSharedData.ROOT_NAME.isEmpty() && !className.startsWith(JSSharedData.ROOT_NAME))
            fullName += JSSharedData.ROOT_NAME;

        // package name
        if (!packageName.isEmpty())
        {
            if (!fullName.isEmpty())
                fullName += ".";
            fullName += packageName;
        }

        // class name
        if (!className.isEmpty())
        {
            if (!fullName.isEmpty())
                fullName += ".";
            fullName += className;
        }

        return fullName;
    }

    /*
     * private int typeNameToAbcConstant( Name typeName ) { if( typeName != null
     * ) { final String type =
     * JSGeneratingReducer.getBasenameFromName(typeName); if( type.equals("int")
     * ) return ABCConstants.CONSTANT_Int; if( type.equals("uint") ) return
     * ABCConstants.CONSTANT_UInt; if( type.equals("Number") ) return
     * ABCConstants.CONSTANT_Double; if( type.equals("String") ) return
     * ABCConstants.CONSTANT_Utf8; if( type.equals("Boolean") ) return
     * ABCConstants.CONSTANT_False; if( type.equals("null") ) return
     * ABCConstants.CONSTANT_Null; } return ABCConstants.CONSTANT_Undefined; }
     */
    private Boolean isDataClassDefinition(IDefinition def)
    {
        if (def != null && def instanceof IClassDefinition)
        {
            IClassDefinition classDef = (IClassDefinition)def;
            Iterator<IClassDefinition> superClassIterator = classDef.classIterator(m_project, true);

            while (superClassIterator.hasNext())
            {
                final IClassDefinition superClass = superClassIterator.next();
                if (superClass.getMetaTagByName("DataClass") != null)
                    return true;
            }
        }
        return false;
    }

    protected void emitCtor(MethodInfo ctor, Boolean isExtern, Boolean isInterface, String packageName, String className, String superClassName,
            String methodPrefix, String assignmentOp, String separator, String indent) throws Exception
    {
        final Boolean isFramework = className.equals(JSSharedData.JS_FRAMEWORK_NAME);

        // first check whether there is any code...
        final String body = getCodeForConstructor(ctor);
        final String fullName = createFullName(packageName, className);

        if (!body.isEmpty() || JSSharedData.instance.hasClassInit(fullName))
        {
            // writeString("\n" + indent + "// Constructor\n");

            m_methodPrologue = "";

            final Boolean isPackageFunction = false;
            if (JSSharedData.m_useClosureLib)
            {
                final String pkg = packageName.isEmpty() ? "" : (createFullName(packageName, "") + ".");
                emitMethod(null, ctor, isFramework, isExtern, isInterface, isPackageFunction, fullName, className, "", pkg, assignmentOp, separator, indent);
            }
            else
            {
                MethodBodyInfo mbi = findMethodBodyInfo(ctor);
                emitMethodBody(mbi);
            }
        }
        else
        {
            if (JSSharedData.m_useClosureLib)
                writeString(fullName + " = function(){}\n");
        }

        // inherits
        if (JSSharedData.m_useClosureLib && (!JSGeneratingReducer.isDataType(superClassName) || superClassName.equals("Error")))
        {
            writeString("goog.inherits("
                            + fullName + ", "
                            + superClassName + ");\n");
        }

        // don't add _CLASS to DataClass instances.
        final IDefinition def = JSSharedData.instance.getDefinition(fullName);
        if (JSSharedData.m_useClosureLib && (def == null || !isDataClassDefinition(def)))
        {
            // add _CLASS member to instance 
            writeString("\n");
            writeString("/**\n");
            writeString(" * Member: " + fullName + ".prototype._CLASS\n");
            writeString(" * @const\n");
            writeString(" * @type {" + fullName + "}\n");
            writeString(" */\n");
            writeString(fullName + ".prototype._CLASS = " + fullName + ";\n");
        }
    }

    protected String emitSlotValue(Trait t)
    {
        Object trait_value = t.getAttr(Trait.SLOT_VALUE);
        if (trait_value != null)
        {
            //  See also poolTraitValues(Traits), which
            //  puts these values into the pools.
            if (trait_value instanceof String)
            {
                return ((String)trait_value);
            }
            else if (trait_value instanceof Namespace)
            {
                return (((Namespace)trait_value).getName());
            }
            else if (trait_value instanceof Double)
            {
                return (((Double)trait_value).toString());
            }
            else if (trait_value instanceof Integer)
            {
                return (((Integer)trait_value).toString());
            }
            else if (trait_value instanceof Long)
            {
                return (((Long)trait_value).toString());
            }
            else if (trait_value instanceof Float)
            {
                return (((Float)trait_value).toString());
            }
            else if (trait_value.equals(Boolean.TRUE))
            {
                return ("true");
            }
            else if (trait_value.equals(Boolean.FALSE))
            {
                return ("false");
            }
            else if (trait_value == ABCConstants.NULL_VALUE)
            {
                return ("null");
            }
            else if (trait_value == ABCConstants.UNDEFINED_VALUE)
            {
                //  Undefined is a special case; it has no kind byte.
                return ("undefined");
            }
            else
            {
                throw new IllegalStateException("Unrecognized initializer type: " + trait_value.getClass().toString());
            }
        }
        else
        {
            final Name type = t.getNameAttr(Trait.TRAIT_TYPE);

            // In AS, uninitialized variables have a type-specific default value (not always undefined).
            // The local-var version of this fix is in JSGeneratingReducer.reduce_typedVariableDecl()
            return (getDefaultInitializerForVariable(type));

            /*
             * // Flash assumes that uninitialized numbers default to "0" and
             * not "undefined". String defaultVal = "undefined"; switch(
             * typeNameToAbcConstant(type) ) { case ABCConstants.CONSTANT_Int :
             * defaultVal = "0"; break; case ABCConstants.CONSTANT_UInt :
             * defaultVal = "0"; break; case ABCConstants.CONSTANT_Double :
             * defaultVal = "0.0"; break; case ABCConstants.CONSTANT_Utf8 :
             * defaultVal = "null"; break; case ABCConstants.CONSTANT_False :
             * defaultVal = "false"; break; case ABCConstants.CONSTANT_True :
             * defaultVal = "true"; break; case ABCConstants.CONSTANT_Undefined
             * : defaultVal = "undefined"; break; case
             * ABCConstants.CONSTANT_Null : defaultVal = "null"; break; default
             * : defaultVal = "undefined"; break; } return( defaultVal );
             */
        }
    }

    protected Boolean emitVariable(Trait t, String baseName,
            String packageName, String className, String superClassName,
            String methodPrefix, String assignment, String separator, String indent) throws Exception
    {
        Boolean needsSkinPartProcessing = false;
        final Name name = t.getNameAttr("name");
        final Name type = t.getNameAttr(Trait.TRAIT_TYPE);

        // JSDoc 
        List<String> jsdoc = new ArrayList<String>();
        // jsdoc.add("Member: " + createFullName(packageName, className + "." + baseName));

        if (name != null)
        {
            Namespace ns = name.getSingleQualifier();
            if (ns.getKind() == CONSTANT_PrivateNs)
                jsdoc.add("@private");
            else if (ns.getKind() == CONSTANT_ProtectedNs)
                jsdoc.add("@protected");
            else
                jsdoc.add("@expose");
        }
        if (t.getKind() == TRAIT_Const)
            jsdoc.add("@const");
        if (type != null)
        {
            final StringBuilder sb = new StringBuilder();
            JSGeneratingReducer.nameToJSDocType(m_project, type, sb);
            jsdoc.add("@type {" + sb.toString() + "}");
        }

        if (jsdoc.size() > 0)
        {
            writeString("\n");
            if (jsdoc.size() == 1)
                writeString(indent + "/** " + jsdoc.get(0) + " */\n");
            else
            {
                writeString(indent + "/**\n");
                for (String decl : jsdoc)
                {
                    writeString(indent + " * " + decl + "\n");
                }
                writeString(indent + " */\n");
            }
        }

        writeString(indent + methodPrefix + baseName + assignment + ";\n\n");

        // Examine var/const metadata
        final Vector<Metadata> metaData = t.getMetadata();
        for (Metadata md : metaData)
        {
            if (md.getName().equals(IMetaAttributeConstants.ATTRIBUTE_SKIN_PART))
            {
                needsSkinPartProcessing = true;
                break;
            }
        }

        return needsSkinPartProcessing;
    }

    protected void emitTraits(Traits traits,
                            Boolean isInstanceTraits, Boolean isExtern, Boolean isInterface, Boolean isPackageFunction,
                            MethodInfo ctor, String packageName, String className, String superClassName,
                            String methodPrefix, String assignmentOp, String separator, String indent) throws Exception
    {
        final Boolean isFramework = className.equals(JSSharedData.JS_FRAMEWORK_NAME);
        Boolean emitComma = false;

        // cache some data used by all the warnIfPrivateNameCollision() calls below
        IClassDefinition classDef = getClassDefinition(className, packageName);
        IClassDefinition baseClass = null;
        ASDefinitionFilter filter = null;
        if (classDef != null)
        {
            baseClass = classDef.resolveBaseClass(m_project);
            filter = new ASDefinitionFilter(ClassificationValue.ALL,
                                            ASDefinitionFilter.SearchScopeValue.INHERITED_MEMBERS,
                                            ASDefinitionFilter.AccessValue.ALL,
                                            baseClass);
        }

        // 1. emit constructor 
        if (isInstanceTraits && ctor != null)
        {
            emitCtor(ctor, isExtern, isInterface, packageName, className, superClassName,
                      methodPrefix, assignmentOp, separator, indent);
        }

        // Avoid emitting duplicate Traits, which starts happening after adding 
        // this line to JSClassDirectiveProcessor::declareVariable():
        // this.classScope.traitsVisitor = (is_static)? ctraits: itraits;
        final Set<String> visitedTraits = new HashSet<String>();

        // 2. emit all variables before any methods
        boolean needsSkinPartProcessing = false;

        for (Trait t : traits)
        {
            //  Get the kind byte with its flags set in the high nibble.
            switch (t.getKind())
            {
                case TRAIT_Var:
                case TRAIT_Const:

                    final Name name = t.getNameAttr("name");
                    Namespace ns = name.getSingleQualifier();
                    if (ns.getKind() != CONSTANT_PrivateNs && isInstanceTraits)
                    	break;
                    final String baseName = JSGeneratingReducer.getBasenameFromName(name);

                    if (!visitedTraits.contains(baseName))
                    {
                        visitedTraits.add(baseName);

                        // see JSGlobalDirectiveProcessor::declareFunction.
                        // Functions at the global scope create a var of type '*'
                        Boolean emitVar = true;
                        if (isPackageFunction)
                        {
                            final Name type = t.getNameAttr("type");
                            if (type == null || type.equals("Function"))
                            {
                                for (MethodInfo mi : methodInfos)
                                {
                                    if (mi.getMethodName() != null && mi.getMethodName().equals(baseName))
                                    {
                                        emitMethod(t, mi, isFramework, isExtern, isInterface, isPackageFunction, baseName, baseName, "", "var ", assignmentOp, separator, indent);
                                        emitVar = false;
                                        break;
                                    }
                                }
                            }
                        }

                        if (emitVar)
                        {
                        	writeString("\n");
                        	String slotValue = (String)t.getAttr(Trait.SLOT_VALUE);
                        	if (slotValue == null)
                        		slotValue = "";
                        	else
                        	{
                        		slotValue = " = " + slotValue;
                        	}                            
                        	boolean methodNeedsSkinPartProcessing = emitVariable(t, baseName,
                                                                    packageName, className, superClassName,
                                                                    methodPrefix, slotValue, separator, indent);

                        	needsSkinPartProcessing = needsSkinPartProcessing || methodNeedsSkinPartProcessing;
                            // print warning in cases where FJS-24 is being hit
                            String fullName = createFullName(packageName, className);
                            if (!fullName.isEmpty())
                                fullName += ".";
                            fullName += baseName;

                            warnIfPrivateNameCollision(baseClass, filter, baseName, fullName, "Field");
                        }
                    }
                    break;
                case TRAIT_Method:
                case TRAIT_Function:
                case TRAIT_Getter:
                case TRAIT_Setter:
                {
                    // methods will be processed below.
                }
                    break;
                case TRAIT_Class:
                    // TODO: non-zero slot id
                    // writeString( "\n    // ClassInfo: " + ((ClassInfo)t.getAttr(Trait.TRAIT_CLASS)).toString() + "\n" );
                    break;
                default:
                    throw new IllegalArgumentException("Unknown trait kind " + t.getKind());
            }
        }
        if (isInstanceTraits && ctor != null)
        {
        	writeString("};\n"); // end of constructor
            final String cName = createFullName(packageName, className);
            writeString("goog.inherits(" + cName + ", " + superClassName + ");\n");
        }
        
        // 3. emit public vars
        for (Trait t : traits)
        {
            //  Get the kind byte with its flags set in the high nibble.
            switch (t.getKind())
            {
                case TRAIT_Var:
                case TRAIT_Const:

                    final Name name = t.getNameAttr("name");
                    Namespace ns = name.getSingleQualifier();
                    if (ns.getKind() == CONSTANT_PrivateNs)
                    	break;
                    final String baseName = JSGeneratingReducer.getBasenameFromName(name);

                    if (!visitedTraits.contains(baseName))
                    {
                        visitedTraits.add(baseName);

                        // see JSGlobalDirectiveProcessor::declareFunction.
                        // Functions at the global scope create a var of type '*'
                        Boolean emitVar = true;
                        if (isPackageFunction)
                        {
                            final Name type = t.getNameAttr("type");
                            if (type == null || type.equals("Function"))
                            {
                                for (MethodInfo mi : methodInfos)
                                {
                                    if (mi.getMethodName() != null && mi.getMethodName().equals(baseName))
                                    {
                                        emitMethod(t, mi, isFramework, isExtern, isInterface, isPackageFunction, baseName, baseName, "", "var ", assignmentOp, separator, indent);
                                        emitVar = false;
                                        break;
                                    }
                                }
                            }
                        }

                        if (emitVar)
                        {
                        	String slotValue = (String)t.getAttr(Trait.SLOT_VALUE);
                        	if (slotValue == null)
                        		slotValue = "";
                        	else
                        	{
                        		slotValue = " = " + slotValue;
                        	}
                        	writeString("\n");
                            
                        	boolean methodNeedsSkinPartProcessing = emitVariable(t, baseName,
                                                                    packageName, className, superClassName,
                                                                    (packageName == "") ? className + ".prototype." :
                                                                    packageName + "." + className + ".prototype.", slotValue, "", "");

                        	needsSkinPartProcessing = needsSkinPartProcessing || methodNeedsSkinPartProcessing;
                            // print warning in cases where FJS-24 is being hit
                            String fullName = createFullName(packageName, className);
                            if (!fullName.isEmpty())
                                fullName += ".";
                            fullName += baseName;

                            warnIfPrivateNameCollision(baseClass, filter, baseName, fullName, "Field");
                        }
                    }
                    break;
                case TRAIT_Method:
                case TRAIT_Function:
                case TRAIT_Getter:
                case TRAIT_Setter:
                {
                    // methods will be processed below.
                }
                    break;
                case TRAIT_Class:
                    // TODO: non-zero slot id
                    // writeString( "\n    // ClassInfo: " + ((ClassInfo)t.getAttr(Trait.TRAIT_CLASS)).toString() + "\n" );
                    break;
                default:
                    throw new IllegalArgumentException("Unknown trait kind " + t.getKind());
            }
        }
        
        String proto = isInstanceTraits ? ".prototype." : ".";
        // 4. emit all other methods (ctor already emitted)
        for (Trait t : traits)
        {
            //  Get the kind byte with its flags set in the high nibble.
            switch (t.getKind())
            {
                case TRAIT_Method:
                case TRAIT_Function:
                case TRAIT_Getter:
                case TRAIT_Setter:
                {
                    // we can't emit getter and setter for extern declarations.
                    if (!isExtern || t.getKind() != TRAIT_Setter)
                    {

                        // write out full function name with package.
                        Name name = t.getNameAttr("name");
                        MethodInfo mi = (MethodInfo)t.getAttr("method_id");
                        final String baseName = JSGeneratingReducer.getBasenameFromName(name);

                        // write out comment with the full class name
                        String fullName = createFullName(packageName, className);
                        String xetter = "";

                        // cleaning up static inits.
                        // Static initializer is generated but not called for classes without explicit constructor
                        //if ((!isInstanceTraits || ctor == null) && JSSharedData.instance.hasClassInit(fullName))
                        //    m_methodPrologue += "        " + fullName + "." + JSSharedData.STATIC_INIT + "();\n";

                        if (!isExtern)
                        {
                            if (t.getKind() == TRAIT_Getter)
                                xetter = JSSharedData.GETTER_PREFIX;
                            else if (t.getKind() == TRAIT_Setter)
                                xetter = JSSharedData.SETTER_PREFIX;
                        }

                        if (!fullName.isEmpty())
                            fullName += ".";
                        fullName += xetter + baseName;

                        // don't emit extern package functions
                        if (!isExtern && isPackageFunction)
                            isExtern = isExtern(name);

                        // actually emit the JS code
                        emitMethod(t, mi, isFramework, isExtern, isInterface, isPackageFunction, fullName, baseName, xetter, 
                                (packageName == "") ? className + proto :
                                    packageName + "." + className + proto , "=", "", "");

                        // print warning in cases where FJS-24 is being hit
                        warnIfPrivateNameCollision(baseClass, filter, baseName, fullName, "Method");
                    }
                }
                    break;
            }
        }

        // 5. custom methods generated from metadata
        if (needsSkinPartProcessing)
        {
            if (emitComma)
                writeString(separator + "\n");
            else
                emitComma = separator.equals(",");

            emitGeneratedSkinPartsMethod(className, packageName, methodPrefix, assignmentOp);
        }

        // 5. class constructor
        if (ctor != null && !isInstanceTraits)
        {
            // first check whether there is any code...
            final String body = getCodeForConstructor(ctor);
            if (!body.isEmpty())
            {
                if (emitComma)
                    writeString(separator + "\n");
                else
                    emitComma = separator.equals(",");

                final String fullName = createFullName(packageName, className);

                // writeString( "\n    // Static inits:\n" );
                emitMethod(null, ctor, isFramework, isExtern, isInterface, isPackageFunction, fullName, null, "", methodPrefix, assignmentOp, separator, indent);
            }
        }
    }

    /**
     * Emits the skinParts getter definition that is implied by the use of
     * [SkinPart] metadata on a Flex class. TODO: once Falcon starts handling
     * [SkinPart] this code should probbly go away
     */
    private void emitGeneratedSkinPartsMethod(String className, String packageName, String methodPrefix, String assignmentOp)
    {
        IClassDefinition def = getClassDefinition(className, packageName);
        String jsonResult = JSFlexUtils.generateGetSkinPartsJSON(def, m_project);

        // Emit JSDoc -- based on emitJSDocForMethod()
        String methodName = "get_skinParts";
        String methodFullName = packageName + "." + className + "." + methodName;
        String indent = "";
        writeString(indent + "\n\n");
        writeString(indent + "/**\n");
        writeString(indent + " * Method: " + methodFullName + "()\n");
        writeString(indent + " * @this {" + methodFullName.substring(0, methodFullName.lastIndexOf(".")) + "}\n");
        writeString(indent + " * @protected\n");
        writeString(indent + " * @override\n");
        writeString(indent + " * @return {Object}\n");
        writeString(indent + " */\n");

        // Emit method definition itself -- based on emitMethod()
        writeString(indent + methodPrefix + methodName);
        writeString(" " + assignmentOp + " ");

        writeString("function() /* : Object */\n");
        writeString("{\n");

        indent = "        ";
        writeString(indent + "return " + jsonResult + ";\n");

        writeString("}");
    }

    /**
     * Given a var or const trait, return the default value it should be
     * initialized to if there was no initializer in the AS code
     */
    public static String getDefaultInitializerForVariable(Name varType)
    {
        if (varType != null)
        {
            String typeName = JSGeneratingReducer.getBasenameFromName(varType);

            // int/uint default to 0 in AS; this matters since, e.g.: 0++ = 1; undefined++ = NaN
            if (typeName.equals("int") || typeName.equals("uint"))
                return "0";

            // Number defaults to naN
            if (typeName.equals("Number"))
                return "NaN";

            // Boolean defaults to false in AS; this matters when comparing two bools: undefined != false
            if (typeName.equals("Boolean"))
                return "false";
        }
        return "undefined";
    }

    /**
     * Given a class member and the class's base class, try to detect whether it
     * will hit FJS-24 - and if so print a warning. Intended to be called from
     * emitTraits().
     */
    public void warnIfPrivateNameCollision(IClassDefinition baseClass, ASDefinitionFilter baseClassAllFilter, String memberName, String memberFullName, String memberDescription)
    {
        // If baseClass is null, we're probably looking at a global function like trace() or setTimeout()
        if (baseClass != null)
        {
            IDefinition conflict = MemberedDefinitionUtils.getMemberByName(baseClass, m_project, memberName, baseClassAllFilter);
            if (conflict != null)
            {
                // If member is non-private, it's *expected* to be overridden (note: we could also check isOverride() on classDef's member)
                // If member is static, FJS-24 doesn't apply and all is well
                if (conflict.isPrivate() && !conflict.isStatic())
                {
                    m_sharedData.stderr("Warning: " + memberDescription + " " + memberFullName + " will unexpectedly override a private member of superclass " + conflict.getParent().getBaseName());
                }
            }
        }
    }

    /*
     * private String getCodeForMethodInfo( MethodInfo mi ) { String body = "";
     * MethodBodyInfo mbi = findMethodBodyInfo( mi ); if( mbi != null ) { for
     * (Block b : mbi.getBlocks()) { for (int i = 0; i < b.instructions.size()
     * && !b.instructions.get(i).isBranch(); i++) { final Instruction insn =
     * b.instructions.get(i); if( insn.getOpcode() == JSSharedData.OP_JS ) {
     * final String str = (String)insn.getOperand(0); body +=
     * JSGeneratingReducer.indentBlock(str,1); } } } } return body; }
     */

    protected String getCodeForConstructor(MethodInfo mi)
    {
        String body = "";
        MethodBodyInfo mbi = findMethodBodyInfo(mi);

        if (mbi != null)
        {
            for (IBasicBlock b : mbi.getCfg().getBlocksInEntryOrder())
            {
                for (int i = 0; i < b.size() && !b.get(i).isBranch(); i++)
                {
                    final Instruction insn = b.get(i);
                    if (insn.getOpcode() == JSSharedData.OP_JS)
                    {
                        final String str = (String)insn.getOperand(0);

                        // we are only interested in the instruction that initialize variables with a value.
                        // if( str.contains("=") )
                        body += JSGeneratingReducer.indentBlock(str, 1);
                    }
                }
            }
        }
        return body;
    }

    private void emitJSDocForMethod(Trait t, MethodInfo mi, MethodBodyInfo mbi, String fullName, String indent)
    {
        final Boolean isCtor = t == null;
        final Boolean isInterface = mbi == null;

        writeString(indent + "\n\n");
        writeString(indent + "/**\n");
        /*
        if (isCtor)
        {
            writeString(indent + " * Constructor: " + fullName + "()\n");
            writeString(indent + " * @constructor\n");
        }
        else if (isInterface)
        {
            writeString(indent + " * Interface: " + fullName + "()\n");
        }
        else
        {
            writeString(indent + " * Method: " + fullName + "()\n");
        }
		*/
        if (!isInterface)
        {
            if (fullName.contains("."))
            {
                if (fullName.substring(0, fullName.lastIndexOf(".")).equals(JSSharedData.JS_FRAMEWORK_NAME))
                    writeString(indent + " * @this {" + JSSharedData.JS_FRAMEWORK_NAME + "}\n");
                else
                    writeString(indent + " * @this {" + fullName.substring(0, fullName.lastIndexOf(".")) + "}\n");
            }

            if (t != null)
            {
                final Name name = t.getNameAttr("name");
                if (name != null)
                {
                    Namespace ns = name.getSingleQualifier();
                    if (ns.getKind() == CONSTANT_PrivateNs)
                        writeString(indent + " * @private\n");
                    else if (ns.getKind() == CONSTANT_ProtectedNs)
                        writeString(indent + " * @protected\n");
                    else
                        writeString(indent + " * @expose\n");
                }
                if (t.isOverride() ||
                    (t.hasAttr("override") && (Boolean)(t.getAttr("override")) == true))
                {
                    writeString(indent + " * @override\n");
                }
            }
        }

        emitJSDocForParams(mi, indent);
        
        if (mi.getReturnType() != null && !JSGeneratingReducer.getBasenameFromName(mi.getReturnType()).equals("void"))
        {
            final StringBuilder sb = new StringBuilder();
            JSGeneratingReducer.nameToJSDocType(m_project, mi.getReturnType(), sb);

            writeString(indent + " * @return {" + sb.toString() + "}\n");
        }

        writeString(indent + " */\n");
    }

    private void emitJSDocForParams(MethodInfo mi, String indent)
    {
        Vector<PooledValue> defaultValues = mi.getDefaultValues();
        Vector<Name> paramTypes = mi.getParamTypes();
        List<String> paramNames = mi.getParamNames();

        int nthParam = 0;
        final int defaultsStartAt = paramTypes.size() - defaultValues.size();
        final Boolean needsRest = (mi.getFlags() & ABCConstants.NEED_REST) > 0;
        final int restStartsAt = needsRest ? paramNames.size() - 1 : paramNames.size();

        for (String argName : paramNames)
        {
            if (nthParam == restStartsAt)
            {
                // param is rest argument
                writeString(indent + " * @param {...} " + argName + "\n");
            }
            else
            {
                Name nextParam = paramTypes.elementAt(nthParam);
                final StringBuilder sb = new StringBuilder();
                JSGeneratingReducer.nameToJSDocType(m_project, nextParam, sb);
                final String argType = sb.toString();
                if (nthParam < defaultsStartAt)
                {
                    // param without default value
                    writeString(indent + " * @param {" + argType + "} " + argName + "\n");
                }
                else
                {
                    // param with default value
                    writeString(indent + " * @param {" + argType + "} " + JSSharedData.DEFAULT_PARAM_PREFIX + argName);

                    String defaultVal = "undefined";
                    PooledValue val = defaultValues.elementAt(nthParam - defaultsStartAt);
                    if (val != null)
                    {
                        switch (val.getKind())
                        {
                            case ABCConstants.CONSTANT_Int:
                                defaultVal = val.getIntegerValue().toString();
                                break;
                            case ABCConstants.CONSTANT_UInt:
                                defaultVal = val.getLongValue().toString();
                                break;
                            case ABCConstants.CONSTANT_Double:
                                defaultVal = val.getDoubleValue().toString();
                                break;
                            case ABCConstants.CONSTANT_Utf8:
                                defaultVal = val.getStringValue();
                                break;
                            case ABCConstants.CONSTANT_True:
                                defaultVal = "true";
                                break;
                            case ABCConstants.CONSTANT_False:
                                defaultVal = "false";
                                break;
                            case ABCConstants.CONSTANT_Undefined:
                                defaultVal = "undefined";
                                break;
                            case ABCConstants.CONSTANT_Null:
                                defaultVal = "null";
                                break;
                            default:
                            {
                                final Namespace ns = val.getNamespaceValue();
                                if (ns != null)
                                    defaultVal = ns.getName() + " /* (namespace) */";
                            }
                                break;
                        }
                    }
                    writeString(" Defaults to " + defaultVal + ".\n");
                }
            }

            nthParam++;
        }
    }
    
    protected Boolean isPackageFunction(MethodInfo mi)
    {
        final FunctionDefinition fdef = m_methodInfoToDefinition.get(mi);
        if (fdef != null && fdef.getParent() instanceof PackageDefinition)
            return true;

        return false;
    }

    protected void emitFrameworkInit()
    {
        writeString("\n");
        writeString("// Overrides \n");

        writeString("adobe.globals = __global;\n");

        // auto-register classes
        writeString("" + JSSharedData.JS_CLASSES + " = {};\n");
        writeString("" + JSSharedData.JS_INT_CLASS + " = IntClass;\n");
        writeString("" + JSSharedData.JS_UINT_CLASS + " = UIntClass;\n");

        // override settings for adobe.USE_SELF and adobe.USE_STATIC
        final Boolean useClosure = JSSharedData.m_useClosureLib;
        final Boolean useSelfParameter = JSSharedData.m_useSelfParameter;
        final Boolean convertToStatic = useSelfParameter && false; // see JSSharedData.m_convertToStatic
        writeString("\n");
        writeString("// Settings \n");
        writeString("adobe.USE_CLOSURE = " + (useClosure ? "true" : "false") + ";\n");
        writeString("adobe.USE_SELF = " + (useSelfParameter ? "true" : "false") + ";\n");
        writeString("adobe.USE_STATIC = " + (convertToStatic ? "true" : "false") + ";\n");
        writeString("adobe.ROOT_NAME = \"" + JSSharedData.ROOT_NAME + "\";\n");
        writeString("adobe.NO_EXPORTS = " + JSSharedData.NO_EXPORTS + ";\n");
        writeString("adobe.root = " + JSSharedData.JS_SYMBOLS + ";\n");
    }

    protected void emitMethod(Trait t, MethodInfo mi, Boolean isFramework, Boolean isExtern, Boolean isInterface, Boolean isPackageFunction, String fullName, String name, String xetter, String methodPrefix, String assignmentOp, String separator, String indent) throws Exception
    {
        // don't emit extern package functions
        if (isExtern && isPackageFunction)
            return;

        MethodBodyInfo mbi = findMethodBodyInfo(mi);

        if (name != null)
        {
            // Sometimes mi.name is empty
            if (mi.getMethodName() == null || mi.getMethodName().isEmpty())
                mi.setMethodName(name);

            // set m_currentScope to classScope
            if (m_currentScope != null)
            {
                IASScope methodScope = null;
                IASScope scope = m_currentScope;
                ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.MEMBERS_AND_TYPES,
                        SearchScopeValue.ALL_SCOPES,
                        AccessValue.ALL,
                        scope.getDefinition());
                IDefinition def = ASScopeUtils.findDefinitionByName(scope, m_project, name, filter);
                if (def != null)
                {
                    //FunctionDefinition 
                    // methodScope = new ASScope((ASScope)def.getContainingScope());
                    methodScope = def.getContainingScope();
                    m_currentScope = methodScope;
                }
            }

            emitJSDocForMethod(t, mi, mbi, fullName, indent);

            final Boolean isGetter = t != null && t.getKind() == TRAIT_Getter;
            // final Boolean isPackageFunction = methodPrefix.isEmpty() && isPackageFunction(mi);

            // regular constructor
            writeString(indent + methodPrefix + xetter + name);
            if (isInterface && isGetter)
            {
                writeString(";\n");
                return;
            }

            writeString(" " + assignmentOp + " ");

            writeString("function(");
            String a_priori_insns = "";
            // a_priori_insns = emitParameters( m_methodInfoToDefinition.get(mi) );
            a_priori_insns = emitParameters(mi);
            writeString(")");

            // return type 
            // if (mi.getReturnType() != null)
            //    writeString(" /* : " + JSGeneratingReducer.getBasenameFromName(mi.getReturnType()) + " */");

            if (isInterface)
            {
                writeString(" {};\n");
            }
            else
            {
                writeString("\n");
                writeString(indent + "{");
                writeString("\n");
                if (!a_priori_insns.isEmpty())
                {
                    writeString(a_priori_insns);
                }
                emitMethodBody(mbi);
                writeString(indent + "};");
            }

        }
        else
        {
            // all __static_inits need to be registered.
            if (!JSSharedData.instance.hasClassInit(fullName))
                JSSharedData.instance.registerClassInit(fullName);

            final String staticInitName = methodPrefix + JSSharedData.STATIC_INIT;

            writeString("\n\n");
            writeString("/**\n");
            writeString(" * Method: " + indent + staticInitName + "()\n");
            writeString(" */\n");

            writeString(indent + staticInitName + " = ");
            writeString("function() /* : void */\n");
            writeString(indent + "{\n");
            writeString(indent + "    " + staticInitName + " = " + JSSharedData.JS_EMPTY_FUNCTION + ";\n");

            // static init
            emitMethodBody(mbi);

            writeString(indent + "}");
        }
    }

    private void emitScriptInfo(ScriptInfo info) throws Exception
    {
        // w.writeU30(getScriptId(info.getInitId()));
        // emitTraits(info.getTraits(), "", "    " );

        final Object init_id = info.getInit();
        ;
        if (init_id instanceof MethodInfo)
        {
            final MethodInfo mi = (MethodInfo)init_id;
            final MethodBodyInfo mbi = this.findMethodBodyInfo(mi);
            if (mbi != null)
            {
                final String str = extractCodeFromMethodBodyInfo(mbi);
                if (!str.isEmpty())
                {
                    final StringBuilder scriptInfos = new StringBuilder();

                    scriptInfos.append("// ScriptInfo \n");
                    scriptInfos.append("{\n");
                    scriptInfos.append("    var " + JSSharedData.THIS + " = ");
                    if (m_packageName != null && !m_packageName.isEmpty())
                    {
                        scriptInfos.append(m_packageName + ";\n");
                    }
                    else
                    {
                        scriptInfos.append(JSSharedData.JS_FRAMEWORK_NAME + ".globals;\n");
                    }

                    // This is just crazy...
                    // It looks like very variable declared in a script info block becomes a global variable.
                    // See testUnicodeRangeHelper() in Tamarin's ecma3/Unicode/unicodeUtil.as, which for some
                    // reasons uses "this.array" instead of just "array".
                    final StringBuilder sb = new StringBuilder();
                    for (String line : str.split("\n"))
                    {
                        if (line.startsWith("var "))
                            sb.append(line.replaceFirst("var ", JSSharedData.THIS + "."));
                        else
                            sb.append(line);
                        sb.append("\n");
                    }

                    // the meat of  emitCode(mbi);
                    scriptInfos.append(JSGeneratingReducer.indentBlock(sb.toString(), 1));

                    scriptInfos.append("}\n");

                    JSSharedData.instance.registerScriptInfo(scriptInfos.toString());
                }
            }
        }
    }

    protected void emitMethodBody(MethodBodyInfo f)
            throws Exception
    {
        if (f == null)
            return;

        /*
         * MethodInfo signature = f.getMethodInfo();
         * w.writeU30(getMethodId(signature)); f.computeFrameCounts();
         * w.writeU30(f.getMaxStack()); int max_local = f.getLocalCount(); if
         * (signature.getParamCount() > max_local) max_local =
         * signature.getParamCount(); w.writeU30(max_local);
         * w.writeU30(f.getInitScopeDepth()); w.writeU30(f.getMaxScopeDepth());
         */

        if (!m_methodPrologue.isEmpty())
        {
            writeString(m_methodPrologue);
            m_methodPrologue = "";
        }

        emitCode(f);

        if (!m_methodPostlogue.isEmpty())
        {
            writeString(m_methodPostlogue);
            m_methodPostlogue = "";
        }
    }

    private void emitCode(MethodBodyInfo f)
            throws Exception
    {
        String str = extractCodeFromMethodBodyInfo(f);

        /*
         * Experimental: ABC decompilation if( str.isEmpty() ) { final
         * ByteArrayOutputStream out = new ByteArrayOutputStream(); final
         * JSConverter converter = new JSConverter(m_project, m_currentScope,
         * out ); final MethodInfo mi = f.getMethodInfo(); final IMethodVisitor
         * mv = converter.visitMethod(mi); mv.visit(); final IMethodBodyVisitor
         * mbv = mv.visitBody(f); mbv.visit(); mbv.visitEnd(); mv.visitEnd();
         * str = out.toString(); }
         */

        if (!str.isEmpty())
        {
            writeString(JSGeneratingReducer.indentBlock(str, 1));
        }
    }

    /*
     * private String indentBlock( String block, int indentBy ) { Boolean
     * firstPart = true; String s = ""; String[] parts = block.split( "\n" );
     * for( String part : parts ) { if( firstPart ) firstPart = false; else s +=
     * "\n"; for( int i = 0; i < indentBy; ++i ) s += "    "; s += part; } return
     * s; } private void emitNamespace(Namespace ns) { w.write(ns.getKind());
     * w.writeU30(stringPool.id(ns.getName())); }
     */

    private String extractCodeFromBlock(IBasicBlock b)
    {
        String str = "";
        for (int i = 0; i < b.size() && !b.get(i).isBranch(); i++)
        {
            final Instruction insn = b.get(i);
            if (insn.getOpcode() == JSSharedData.OP_JS)
                str += (String)insn.getOperand(0);
        }
        return str;
    }

    private String extractCodeFromMethodBodyInfo(MethodBodyInfo mbi)
    {
        String str = "";
        for (IBasicBlock b : mbi.getCfg().getBlocksInEntryOrder())
        {
            str += extractCodeFromBlock(b);
        }
        return str;
    }

    class JSOutputStream extends ByteArrayOutputStream
    {
        void rewind(int n)
        {
            super.count -= n;
        }

        void writeString(String str)
        {
            try
            {
                write(str.getBytes());
            }
            catch (IOException e)
            {

            }
        }

        public void write(int i)
        {
            // super.write(i);
        }

        void writeU16(int i)
        {
            //write(i);
            //write(i >> 8);
        }

        void writeS24(int i)
        {
            //writeU16(i);
            //write(i >> 16);
        }

        void write64(long i)
        {
            //writeS24((int)i);
            //writeS24((int)(i >> 24));
            //writeU16((int)(i >> 48));
        }

        void writeU30(int v)
        {
            /*
             * if (v < 128 && v >= 0) { write(v); } else if (v < 16384 && v >=
             * 0) { write(v & 0x7F | 0x80); write(v >> 7); } else if (v <
             * 2097152 && v >= 0) { write(v & 0x7F | 0x80); write(v >> 7 |
             * 0x80); write(v >> 14); } else if (v < 268435456 && v >= 0) {
             * write(v & 0x7F | 0x80); write(v >> 7 | 0x80); write(v >> 14 |
             * 0x80); write(v >> 21); } else { write(v & 0x7F | 0x80); write(v
             * >> 7 | 0x80); write(v >> 14 | 0x80); write(v >> 21 | 0x80);
             * write(v >> 28); }
             */
        }

        int sizeOfU30(int v)
        {
            if (v < 128 && v >= 0)
            {
                return 1;
            }
            else if (v < 16384 && v >= 0)
            {
                return 2;
            }
            else if (v < 2097152 && v >= 0)
            {
                return 3;
            }
            else if (v < 268435456 && v >= 0)
            {
                return 4;
            }
            else
            {
                return 5;
            }
        }
    }

    /**
     * The nominal name given to a MultinameL.
     * 
     * @see http://bugs.adobe.com/jira/browse/CMP-393, this may not be
     * necessary, and if under some circumstances it is necessary this is
     * certainly not the name required.
     */
    private static Name indexAccessName;
    /**
     * The above Name packaged up into operand form.
     */
    private static Object[] indexAccessOperands;
    /*
     * Initialization code for indexAccessname and indexAccessOperands.
     */
    static
    {
        Vector<Namespace> usual_suspects = new Vector<Namespace>(2);
        usual_suspects.add(new Namespace(CONSTANT_PrivateNs));
        usual_suspects.add(new Namespace(CONSTANT_PackageNs));
        indexAccessName = new Name(ABCConstants.CONSTANT_MultinameL, new Nsset(usual_suspects), null);
        indexAccessOperands = new Object[] {indexAccessName};
    }

    /**
     * Find all the operands in a method body and make sure they find their way
     * into the appropriate pool.
     * 
     * @param mi - the method body.
     * @post any runtime multinames have a dummy Name operand.
     */
    void _poolOperands(MethodBodyInfo mbi)
    {
        for (IBasicBlock b : mbi.getCfg().getBlocksInEntryOrder())
            for (Instruction insn : b.getInstructions())
                switch (insn.getOpcode())
                {
                    case OP_findproperty:
                    case OP_findpropstrict:
                    case OP_getlex:
                    case OP_getsuper:
                    case OP_setsuper:
                    case OP_getdescendants:
                    case OP_initproperty:
                    case OP_istype:
                    case OP_coerce:
                    case OP_astype:
                    case OP_finddef:
                        visitPooledName((Name)insn.getOperand(0));
                        break;
                    case OP_deleteproperty:
                    case OP_getproperty:
                    case OP_setproperty:
                        if (insn.getOperandCount() > 0 && insn.getOperand(0) != null)
                        {
                            visitPooledName((Name)insn.getOperand(0));
                        }
                        else
                        {
                            //  get/set/deleteproperty with no operands => runtime multiname.
                            visitPooledName(indexAccessName);
                            insn.setOperands(indexAccessOperands);
                        }
                        break;
                    case OP_callproperty:
                    case OP_callproplex:
                    case OP_callpropvoid:
                    case OP_callsuper:
                    case OP_callsupervoid:
                    case OP_constructprop:
                        visitPooledName((Name)insn.getOperand(0));
                        break;
                    case OP_pushstring:
                    case OP_dxns:
                    case OP_debugfile:
                        break;
                    case OP_pushnamespace:
                        visitPooledNamespace((Namespace)insn.getOperand(0));
                        break;
                    case OP_pushint:
                        break;
                    case OP_pushuint:
                        break;
                    case OP_pushdouble:
                        break;
                    case OP_debug:
                        break;
                }
    }

    /**
     * Inspect a set of Traits and ensure that constant initializer values have
     * been properly registered in the constant pools.
     */
    private void poolTraitValues(Traits ts)
    {
        for (Trait t : ts)
        {
            if (t.hasAttr(Trait.SLOT_VALUE))
            {
                Object trait_value = t.getAttr(Trait.SLOT_VALUE);
                if (trait_value == null)
                {
                    //  No action required; the pool ID resolution logic
                    //  handles a null pointer by returning the nil pool value.
                }
                else if (trait_value instanceof String)
                {
                    visitPooledString((String)trait_value);
                }
                else if (trait_value instanceof Namespace)
                {
                    visitPooledNamespace((Namespace)trait_value);
                }
                else if (trait_value instanceof Double)
                {
                    visitPooledDouble((Double)trait_value);
                }
                else if (trait_value instanceof Integer)
                {
                    visitPooledInt((Integer)trait_value);
                }
                else if (trait_value instanceof Long)
                {
                    visitPooledUInt((Long)trait_value);
                }
                else if (trait_value instanceof Float)
                {
                    visitPooledFloat((Float)trait_value);
                }
                else if (trait_value.equals(ABCConstants.UNDEFINED_VALUE)
                         || trait_value.equals(ABCConstants.NULL_VALUE)
                         || trait_value.equals(Boolean.TRUE)
                         || trait_value.equals(Boolean.FALSE))
                {
                    // Nothing to do, predefined value.
                }
                else
                {
                    throw new IllegalStateException("Unrecognized initializer type: " + trait_value.getClass().toString());
                }
            }
        }
    }

    /**
     * Disambiguate input from multiple ABCs by numbering them.
     */
    // private int abcNum = 0;

    public void visit(int majorVersion, int minorVersion)
    {
        if (m_visitor != null)
            m_visitor.visit(majorVersion, minorVersion);
        // abcNum++;
    }

    public void visitEnd()
    {
        if (m_visitor != null)
            m_visitor.visitEnd();
    }

    public IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo)
    {
        IClassVisitor visitor = null;
        if (m_visitor != null)
            visitor = m_visitor.visitClass(iinfo, cinfo);
        this.definedClasses.add(new EmitterClassVisitor(iinfo, cinfo, visitor));
        IClassVisitor result = this.definedClasses.lastElement();
        result.visit();
        return result;
    }

    public IScriptVisitor visitScript()
    {
        IScriptVisitor visitor = null;
        if (m_visitor != null)
            visitor = m_visitor.visitScript();
        this.scriptInfos.add(new ScriptInfo());
        return new EmitterScriptInfo(this.scriptInfos.lastElement(), visitor);
    }

    public IMethodVisitor visitMethod(MethodInfo minfo)
    {
        IMethodVisitor visitor = null;
        if (m_visitor != null)
            visitor = m_visitor.visitMethod(minfo);

        methodInfos.add(minfo);
        for (Name param_type_name : minfo.getParamTypes())
            visitPooledName(param_type_name);

        return new EmitterMethodInfoVisitor(minfo, visitor);
    }

    public void visitPooledDouble(Double d)
    {
    }

    public void visitPooledInt(Integer i)
    {
    }

    public void visitPooledMetadata(Metadata md)
    {
        visitPooledString(md.getName());

        for (String key : md.getKeys())
            visitPooledString(key);

        for (String value : md.getValues())
            visitPooledString(value);
    }

    public void visitPooledName(Name n)
    {
        if (null == n)
            return;

        if (n.getKind() != ABCConstants.CONSTANT_TypeName)
        {
            visitPooledString(n.getBaseName());
            visitPooledNsSet(n.getQualifiers());
        }
        else
        {
            visitPooledName(n.getTypeNameBase());
            visitPooledName(n.getTypeNameParameter());
        }
    }

    public void visitPooledNamespace(Namespace ns)
    {
        if (ns != null)
            visitPooledString(ns.getName());
    }

    public void visitPooledNsSet(Nsset nss)
    {
        if (nss != null)
        {
            for (Namespace ns : nss)
            {
                visitPooledNamespace(ns);
            }
        }
    }

    public void visitPooledString(String s)
    {
    }

    public void visitPooledUInt(Long l)
    {
    }

    public void visitPooledFloat(Float f)
    {
        throw new IllegalStateException("not implemented.");
    }

    public static int sizeOfU30(int v)
    {
        if (v < 128 && v >= 0)
        {
            return 1;
        }
        else if (v < 16384 && v >= 0)
        {
            return 2;
        }
        else if (v < 2097152 && v >= 0)
        {
            return 3;
        }
        else if (v < 268435456 && v >= 0)
        {
            return 4;
        }
        else
        {
            return 5;
        }
    }

    protected class EmitterClassVisitor implements IClassVisitor
    {
        ClassInfo classInfo;
        Traits classTraits;
        InstanceInfo instanceInfo;
        Traits instanceTraits;
        IClassVisitor m_visitor;

        /**
         * Disambiguate classes with the same name from different ABCs.
         */
        // TODO: public int abcNum = JSEmitter.this.abcNum;

        EmitterClassVisitor(InstanceInfo iinfo, ClassInfo cinfo, IClassVisitor visitor)
        {
            this.m_visitor = visitor;

            this.classInfo = cinfo;
            if (null == cinfo.classTraits)
                cinfo.classTraits = new Traits();
            this.classTraits = cinfo.classTraits;

            this.instanceInfo = iinfo;

            if (null == iinfo.traits)
                iinfo.traits = new Traits();
            this.instanceTraits = iinfo.traits;
            if (null == iinfo.interfaceNames)
                iinfo.interfaceNames = new Name[0];
        }

        public void visit()
        {
            m_currentClassVisitor = this;

            // register as extern 
            final Name name = instanceInfo.name;
            final IDefinition def = getDefinition(name);
            final String packageName = def == null ? m_packageName : def.getPackageName();
            final Boolean isExtern = isExtern(instanceInfo);
            if (isExtern)
            {
                m_sharedData.registerExtern(name);
                // name = JSGeneratingReducer.makeName(JSGeneratingReducer.getBasenameFromName(name));
            }

            // extract and register package 
            m_sharedData.registerPackage(packageName);

            // register class with super class
            if (!instanceInfo.isInterface() && !isExtern)
                m_sharedData.registerClass(name, instanceInfo.superName);

            if (!isExtern && instanceInfo.interfaceNames != null)
            {
                for (Name iname : instanceInfo.interfaceNames)
                {
                    // Skipping classes that are "marked" as IExtern.
                    if (!JSGeneratingReducer.getBasenameFromName(iname).equals(JSSharedData.EXTERN_INTERFACE_NAME))
                    {
                        /*
                         * if( isExtern(iname) ) iname =
                         * JSGeneratingReducer.makeName
                         * (JSGeneratingReducer.getBasenameFromName(iname));
                         */
                        m_sharedData.registerInterface(name, iname);
                    }
                }
            }
        }

        public ITraitsVisitor visitClassTraits()
        {
            ITraitsVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitClassTraits();
            return new EmitterTraitsVisitor(this.classTraits, visitor);
        }

        public ITraitsVisitor visitInstanceTraits()
        {
            ITraitsVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitInstanceTraits();
            return new EmitterTraitsVisitor(this.instanceTraits, visitor);
        }

        public void visitEnd()
        {
            if (null == classInfo.cInit)
            {
                classInfo.cInit = new MethodInfo();
                MethodBodyInfo m_cinit = new MethodBodyInfo();
                m_cinit.setMethodInfo(classInfo.cInit);

                IMethodVisitor mv = visitMethod(classInfo.cInit);
                mv.visit();
                IMethodBodyVisitor mbv = mv.visitBody(m_cinit);
                mbv.visit();
                mbv.visitInstruction(OP_returnvoid);
                mbv.visitEnd();
                mv.visitEnd();
            }

            visitPooledName(instanceInfo.name);
            visitPooledName(instanceInfo.superName);

            if (instanceInfo.hasProtectedNs())
                visitPooledNamespace(instanceInfo.protectedNs);

            if (null == instanceInfo.iInit)
            {
                instanceInfo.iInit = new MethodInfo();
                MethodBodyInfo iinit = new MethodBodyInfo();
                iinit.setMethodInfo(instanceInfo.iInit);

                IMethodVisitor mv = visitMethod(instanceInfo.iInit);
                mv.visit();

                //  Interfaces need an instance init method, 
                //  but it doesn't have a body.
                if (0 == (instanceInfo.flags & ABCConstants.CLASS_FLAG_interface))
                {
                    IMethodBodyVisitor mbv = mv.visitBody(iinit);
                    mbv.visit();
                    /*
                     * mbv.visitInstruction(OP_getlocal0);
                     * mbv.visitInstruction(OP_pushscope);
                     * mbv.visitInstruction(OP_getlocal0);
                     * mbv.visitInstruction(ABCConstants.OP_constructsuper, 0);
                     * mbv.visitInstruction(OP_returnvoid);
                     */
                    mbv.visitEnd();
                }
                mv.visitEnd();
            }

            if (instanceInfo.interfaceNames != null)
            {
                for (Name interface_name : instanceInfo.interfaceNames)
                {
                    visitPooledName(interface_name);
                }
            }

            poolTraitValues(classInfo.classTraits);
            poolTraitValues(instanceInfo.traits);

            // m_currentClassVisitor = null;
        }
    }

    private class EmitterTraitsVisitor implements ITraitsVisitor
    {

        Traits traits;
        ITraitsVisitor m_visitor;

        EmitterTraitsVisitor(Traits traits, ITraitsVisitor visitor)
        {
            this.traits = traits;
            this.m_visitor = visitor;
        }

        private String getFullClassName()
        {
            if (m_currentClassVisitor != null)
            {
                final IDefinition def = getDefinition(m_currentClassVisitor.instanceInfo.name);
                if (def != null)
                    return def.getQualifiedName();
            }
            return "";
        }

        private Boolean hasClassTraits()
        {
            return m_currentClassVisitor != null && m_currentClassVisitor.classInfo.classTraits == this.traits;
        }

        private Boolean hasMethodTraits()
        {
            return m_currentClassVisitor != null && m_currentClassVisitor.instanceInfo.traits == this.traits;
        }

        public ITraitVisitor visitClassTrait(int kind, Name name, int slot_id, ClassInfo clazz)
        {
            ITraitVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitClassTrait(kind, name, slot_id, clazz);

            Trait t = createTrait(kind, name);
            if (slot_id != 0)
                t.addAttr(Trait.TRAIT_SLOT, slot_id);
            t.addAttr(Trait.TRAIT_CLASS, clazz);

            // this is missing in ABCEmitter.
            clazz.classTraits.add(t);

            return new EmitterTraitVisitor(t, visitor);
        }

        public ITraitVisitor visitMethodTrait(int kind, Name name, int dispId,
                MethodInfo method)
        {
            ITraitVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitMethodTrait(kind, name, dispId, method);

            String fullName = getFullClassName();
            if (fullName.isEmpty() && m_packageName != null)
                fullName = m_packageName;
            if (!fullName.isEmpty())
                fullName += ".";

            final String baseName = JSGeneratingReducer.getBasenameFromName(name);
            String type = "Function";
            switch (kind)
            {
                case TRAIT_Method:
                case TRAIT_Function:
                {
                    type = "Function";
                    fullName += baseName;
                }
                    break;
                case TRAIT_Getter:
                {
                    type = "Getter";
                    fullName += JSSharedData.GETTER_PREFIX + baseName;
                    // m_sharedData.registerGetter(fullName + baseName));
                }
                    break;
                case TRAIT_Setter:
                {
                    type = "Setter";
                    fullName += JSSharedData.SETTER_PREFIX + baseName;
                    // m_sharedData.registerSetter(fullName + baseName);
                }
                    break;
            }

            m_sharedData.registerVarType(fullName, type);
            // if( hasClassTraits() )
            //    m_sharedData.registerStaticMember(fullName);

            Trait t = createTrait(kind, name);
            t.addAttr(Trait.TRAIT_METHOD, method);
            return new EmitterTraitVisitor(t, visitor);
        }

        public ITraitVisitor visitSlotTrait(int kind, Name name, int slotId,
                Name slotType, Object slotValue)
        {
            final String baseName = JSGeneratingReducer.getBasenameFromName(name);

            ITraitVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitSlotTrait(kind, name, slotId, slotType, slotValue);

            if (slotType != null && kind == TRAIT_Var && (hasClassTraits() || hasMethodTraits()))
            {

                String fullName = getFullClassName();
                if (!fullName.isEmpty())
                    fullName += ".";
                fullName += baseName;
                final IDefinition def = getDefinition(slotType);
                if (!(def instanceof AmbiguousDefinition))
                    m_sharedData.registerVarType(fullName, def.getQualifiedName());
            }

            Trait t = createTrait(kind, name);
            t.addAttr(Trait.TRAIT_SLOT, slotId);
            t.addAttr(Trait.TRAIT_TYPE, slotType);
            t.addAttr(Trait.SLOT_VALUE, slotValue);
            if (slotType != null)
                visitPooledName(slotType);
            return new EmitterTraitVisitor(t, visitor);
        }

        public void visit()
        {
            if (m_visitor != null)
                m_visitor.visit();
        }

        public void visitEnd()
        {
            if (m_visitor != null)
                m_visitor.visitEnd();
        }

        public Traits getTraits()
        {
            return this.traits;
        }

        private Trait createTrait(int kind, Name name)
        {
            Trait t = new Trait(kind, name);
            traits.add(t);
            visitPooledName(name);
            return t;
        }
    }

    private class EmitterMetadataVisitor implements IMetadataVisitor
    {
        Trait t;
        IMetadataVisitor m_visitor;

        EmitterMetadataVisitor(Trait t, IMetadataVisitor visitor)
        {
            this.t = t;
            this.m_visitor = visitor;
        }

        public void visit(Metadata md)
        {
            if (m_visitor != null)
                m_visitor.visit(md);

            visitPooledMetadata(md);
            t.addMetadata(md);
        }
    }

    public class EmitterTraitVisitor implements ITraitVisitor
    {
        Trait t;
        ITraitVisitor m_visitor;

        EmitterTraitVisitor(Trait t, ITraitVisitor visitor)
        {
            this.t = t;
            this.m_visitor = visitor;
        }

        public IMetadataVisitor visitMetadata(int count)
        {
            IMetadataVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitMetadata(count);

            return new EmitterMetadataVisitor(t, visitor);
        }

        public void visitAttribute(String attr_name, Object attr_value)
        {
            if (m_visitor != null)
                m_visitor.visitAttribute(attr_name, attr_value);
            this.t.addAttr(attr_name, attr_value);
        }

        public void visitStart()
        {
            if (m_visitor != null)
                m_visitor.visitStart();
        }

        public void visitEnd()
        {
            if (m_visitor != null)
                m_visitor.visitEnd();
        }
    }

    private class EmitterMethodBodyInfo implements IMethodBodyVisitor
    {
        private MethodBodyInfo mbi;
        public IMethodBodyVisitor m_visitor;

        EmitterMethodBodyInfo(MethodBodyInfo mbinfo, IMethodBodyVisitor visitor)
        {
            this.mbi = mbinfo;
            this.m_visitor = visitor;
        }

        public void visit()
        {
            if (m_visitor != null)
                m_visitor.visit();
        }

        public void visitEnd()
        {
            if (m_visitor != null)
                m_visitor.visitEnd();
        }

        public void visitInstruction(int opcode)
        {
            if (m_visitor != null)
                m_visitor.visitInstruction(opcode);
            this.mbi.insn(opcode);
        }

        public void visitInstruction(int opcode, int immediate_operand)
        {
            if (m_visitor != null)
                m_visitor.visitInstruction(opcode, immediate_operand);
            this.mbi.insn(opcode, immediate_operand);
        }

        public void visitInstruction(int opcode, Object single_operand)
        {
            if (m_visitor != null)
                m_visitor.visitInstruction(opcode, single_operand);
            this.mbi.insn(opcode, single_operand);
        }

        public void visitInstruction(int opcode, Object[] operands)
        {
            if (m_visitor != null)
                m_visitor.visitInstruction(opcode, operands);
            this.mbi.insn(opcode, operands);
        }

        public void visitInstruction(Instruction i)
        {
            if (m_visitor != null)
                m_visitor.visitInstruction(i);
            this.mbi.insn(i);
        }

        public ITraitsVisitor visitTraits()
        {
            ITraitsVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitTraits();
            return new EmitterTraitsVisitor(this.mbi.getTraits(), visitor);
        }

        public int visitException(Label from, Label to, Label target, Name ex_type, Name ex_var)
        {
            if (m_visitor != null)
                m_visitor.visitException(from, to, target, ex_type, ex_var);
            visitPooledName(ex_type);
            visitPooledName(ex_var);
            return mbi.addExceptionInfo(new ExceptionInfo(from, to, target, ex_type, ex_var));
        }

        public void visitInstructionList(InstructionList new_list)
        {
            if (m_visitor != null)
                m_visitor.visitInstructionList(new_list);
            mbi.setInstructionList(new_list);
        }

        public void labelCurrent(Label l)
        {
            if (m_visitor != null)
                m_visitor.labelCurrent(l);
            mbi.labelCurrent(l);
        }

        public void labelNext(Label l)
        {
            if (m_visitor != null)
                m_visitor.labelNext(l);
            mbi.labelNext(l);
        }

    }

    private class EmitterScriptInfo implements IScriptVisitor
    {

        ScriptInfo si;
        IScriptVisitor m_visitor;

        EmitterScriptInfo(ScriptInfo s, IScriptVisitor visitor)
        {
            this.si = s;
            this.m_visitor = visitor;
        }

        public void visit()
        {
        }

        public void visitEnd()
        {
            poolTraitValues(si.getTraits());
        }

        public void visitInit(MethodInfo init_method)
        {
            si.setInit(init_method);
        }

        public ITraitsVisitor visitTraits()
        {
            ITraitsVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitTraits();

            return new EmitterTraitsVisitor(si.getTraits(), visitor);
        }

    }

    private class EmitterMethodInfoVisitor implements IMethodVisitor
    {
        MethodInfo mi;
        MethodBodyInfo mbi;
        IMethodVisitor m_visitor;

        EmitterMethodInfoVisitor(MethodInfo mi, IMethodVisitor visitor)
        {
            this.mi = mi;
            this.m_visitor = visitor;
        }

        public void visit()
        {
            if (m_visitor != null)
                m_visitor.visit();

            if (m_currentClassVisitor != null)
            {

            }
        }

        public IMethodBodyVisitor visitBody(MethodBodyInfo mbi)
        {
            IMethodBodyVisitor visitor = null;
            if (m_visitor != null)
                visitor = m_visitor.visitBody(mbi);

            this.mbi = mbi;
            return new EmitterMethodBodyInfo(mbi, visitor);
        }

        public void visitEnd()
        {
            if (m_visitor != null)
                m_visitor.visitEnd();

            if (mbi != null)
            {
                // a native method should never have a method body
                assert (!mi.isNative());

                _poolOperands(mbi);
                poolTraitValues(mbi.getTraits());
                methodBodies.add(mbi);
            }

            visitPooledString(mi.getMethodName());
            visitPooledName(mi.getReturnType());

            if (mi.hasParamNames())
            {
                for (String param_name : mi.getParamNames())
                {
                    visitPooledString(param_name);
                }
            }
        }
    }

    protected String buildSuperClassName(String className, String _super)
    {
        String superClass = "";

        if (_super == null)
        {
            superClass = "Object";
        }
        else if (_super.indexOf("jQuery.jQuery") == 0 || _super.indexOf("jQuery.event") == 0)
        {
            superClass = "jQuery";
        }
        else if (_super.indexOf("jQuery.event") == 0)
        {
            superClass = "jQuery.Event";
        }
        else if (_super.equals("browser.JSError"))
        {
            // JSError will get mapped to the browser's Error class.                 
            superClass = "Error";
        }
        else if (JSGeneratingReducer.isDataType(_super))
        {
            superClass = _super;
        }
        else if (className.equals(JSSharedData.JS_FRAMEWORK_NAME) ||
                 _super.indexOf("browser.") == 0 ||
                 _super.indexOf("com.jquery.") == 0)
        {
            superClass = "Object";
        }
        else
        {
            superClass = createFullName("", _super);
        }

        return superClass;
    }

    protected Boolean isExtern(InstanceInfo ii)
    {
        if (JSGeneratingReducer.getBasenameFromName(ii.name).equals(JSSharedData.EXTERN_INTERFACE_NAME))
            return true;

        for (Name iname : ii.interfaceNames)
        {
            if (JSGeneratingReducer.getBasenameFromName(iname).equals(JSSharedData.EXTERN_INTERFACE_NAME))
            {
                return true;
            }
        }
        return false;
    }

    /*
     * private void emitPackageNames( String fullName ) { String packageName =
     * ""; String[] parts = fullName.split("\\."); for( String part: parts ) {
     * if( !packageName.isEmpty() ) packageName += "."; packageName += part; if(
     * !m_sharedData.hasPackage(packageName) &&
     * packageName.equals(JSSharedData.JS_FRAMEWORK_NAME) ) {
     * m_sharedData.registerPackage(packageName);
     * writeString("/ * * @typedef { Object }  * /\n"); if(
     * !packageName.contains(".") ) writeString( "var " ); writeString(
     * packageName + " = {};\n\n" ); } } }
     */

    protected void emitInterface(EmitterClassVisitor clz) throws Exception
    {

        // only set emitExterns to true if you want to generate extern files, i.e. svg.js.
        // if( !emitExterns )
        //    return;

        InstanceInfo ii = clz.instanceInfo;
        final String className = JSGeneratingReducer.getBasenameFromName(ii.name);
        if (className.equals(JSSharedData.EXTERN_INTERFACE_NAME))
            return;

        final Boolean isExtern = isExtern(ii);
        final Boolean isInterface = ii.isInterface();
        final IDefinition def = getDefinition(ii.name);
        final String packageName = def.getPackageName();

        // register class with super class
        final Name superClassName = ii.interfaceNames.length == 0 ? ii.superName : ii.interfaceNames[0];
        IDefinition superClassDef = null;
        if (superClassName != null)
        {
            superClassDef = getDefinition(superClassName);
            JSSharedData.instance.registerClass(ii.name, superClassName);
        }

        final String superClass = superClassDef == null || superClassDef instanceof AmbiguousDefinition ? "Object" : superClassDef.getQualifiedName();

        /*
         * final Name name = ii.interfaceNames[0]; nsset.add(ns); Multiname
         * mname = Multiname.create(name.getQualifiers()., name.getBaseName() );
         * IDefinition def = null; // final ASDefinitionFilter filter = new
         * ASDefinitionFilter(ClassificationValue.CLASSES_AND_INTERFACES,
         * SearchScopeValue.ALL_SCOPES, AccessValue.ALL, def); def =
         * m_project.getScope().findDefinitionByName(ii.interfaceNames[0],
         * filter, new ASDefinitionCache(m_project) ); // Name name =
         * ii.interfaceNames[0]; String name =
         * JSGeneratingReducer.getFullNameFromName( ii.interfaceNames[0], false
         * ); // def =
         * m_project.getScope().findDefinitionByName(ii.interfaceNames[0],true);
         * if( def != null ) superClass =
         * JSGeneratingReducer.definitionToString(def); }
         */

        if (isExtern && emitExterns)
        {
            // IFilter out only the SVG interfaces.
            if (!packageName.startsWith("org.w3c.dom.svg"))
                return;

            // For now we are only interested in classes that are marked as IExtern

            String labelStr = "Extern";

            // register class with super class

            writeString(EXTERN_PREFIX + "\n");
            writeString(EXTERN_PREFIX + "/**\n");
            writeString(EXTERN_PREFIX + " * " + JSGeneratingReducer.getTimeStampString());
            writeString(EXTERN_PREFIX + " *\n");
            writeString(EXTERN_PREFIX + " * " + labelStr + ": " + def.getQualifiedName() + "\n");
            writeString(EXTERN_PREFIX + " *\n");
            writeString(EXTERN_PREFIX + " * @typedef {" + className + "}\n");
            writeString(EXTERN_PREFIX + " */\n");
            writeString(EXTERN_PREFIX + "var " + className + ";\n\n");

            final Boolean isPackageFunction = ii.name == null;
            emitInstanceTraits(ii.traits, null, packageName, className, superClass, isExtern, isInterface, isPackageFunction);

            return;
        }

        final String fullName = def.getQualifiedName();

        // JSDoc
        writeString("\n");
        writeString("/**\n");
        writeString(" * " + JSGeneratingReducer.getTimeStampString());
        writeString(" *\n");
        writeString(" * Interface: " + fullName + "\n");
        writeString(" *\n");
        writeString(" */\n");

        // provide()
        JSSharedData.instance.registerPackage(fullName);
        // emitPackageNames( fullName );
        // writeString( "goog.provide(\"" + fullName  + "\");\n\n" );

        if (isExtern)
        {
            // TODO:
            // writeString("/** @typedef {" + className + "} */\n");
            // writeString( fullName  + " = typeof(" + className + ") == \"undefined\" ? function() {} : " + className + ";\n" );
        }
        else
        {
            writeString("/** @typedef {" + fullName + "} */\n");
            writeString(fullName + " = function() {};\n");

            final Boolean isDynamicClass = def != null && def.isDynamic();
            final Boolean isFinalClass = def != null && def.isFinal();
            emitClassInfo(packageName, className, superClass, isDynamicClass, isFinalClass);
        }

        // finish with an extra line feed
        writeString("\n");
    }

    /*
     * protected IDefinition getDefinition(String packageName, String
     * classOrInterfaceName) { IDefinition def = null; if( m_project != null ) {
     * IASScope scope = m_project.getScope(); ASDefinitionFilter filter = new
     * ASDefinitionFilter( ClassificationValue.MEMBERS_AND_TYPES,
     * SearchScopeValue.ALL_SCOPES, AccessValue.ALL, scope.getDefinition());
     * ASDefinitionCache cache = new ASDefinitionCache(m_project); if(
     * packageName.isEmpty() ) def =
     * scope.findDefinitionByName(classOrInterfaceName, filter, cache); else def
     * = scope.findDefinitionByName(packageName + "." + classOrInterfaceName,
     * filter, cache); } return def; }
     */
    protected void emitClass(EmitterClassVisitor clz) throws Exception
    {
        InstanceInfo ii = clz.instanceInfo;

        // Skipping classes that are "marked" as IExtern.
        final Boolean isExtern = isExtern(ii);
        if (isExtern)
            return;

        final Boolean isInterface = ii.isInterface();
        final Boolean isPackageFunction = ii.name == null;

        String packageName;
        String className;
        if (ii.name != null)
        {
            final IDefinition def = getDefinition(ii.name);
            packageName = def.getPackageName();
            className = JSGeneratingReducer.getBasenameFromName(ii.name);
        }
        else
        {
            packageName = m_packageName;
            className = "";
        }

        ClassDefinition classDef = null;

        // set m_currentScope to classScope
        if (m_project != null)
        {
            m_currentScope = null;
            IASScope scope = m_project.getScope();
            IASScope classScope = null;
            ASDefinitionFilter filter = new ASDefinitionFilter(ClassificationValue.MEMBERS_AND_TYPES,
                    SearchScopeValue.ALL_SCOPES,
                    AccessValue.ALL,
                    scope.getDefinition());
            IDefinition def;
            if (packageName.isEmpty())
                def = ASScopeUtils.findDefinitionByName(scope, m_project, className, filter);
            else
                def = ASScopeUtils.findDefinitionByName(scope, m_project, packageName + "." + className, filter);

            if (def != null && def instanceof ClassDefinition)
            {
                classDef = (ClassDefinition)def;
                classScope = classDef.getContainedScope();
            }

            /*
             * for( int i = 0; i < m_scopes.length && classScope == null; ++i )
             * { IASScope scope = m_scopes[i]; ASDefinitionFilter filter = new
             * ASDefinitionFilter( ClassificationValue.MEMBERS_AND_TYPES,
             * SearchScopeValue.ALL_SCOPES, AccessValue.ALL,
             * scope.getDefinition()); ASDefinitionCache cache = new
             * ASDefinitionCache(m_project); IDefinition def =
             * scope.findDefinitionByName(className, filter, cache); if( def !=
             * null && def instanceof ClassDefinition ) { final ClassDefinition
             * classDef = (ClassDefinition)def; classScope =
             * classDef.getContainedScope(); } }
             */
            m_currentScope = classScope;
        }

        final JSSharedData sharedData = JSSharedData.instance;
        String provideName = null;

        // handle package functions, i.e. goog.events.listen.
        if (ii.iInit == null)
        {
            if (ii.superName == null &&
                ii.traits.getTraitCount() == 0 &&
                clz.classInfo.cInit == null &&
                clz.classTraits.getTraitCount() == 1)
            {
                final Trait t = clz.classTraits.iterator().next();

                if (t.getKind() == TRAIT_Method ||
                    t.getKind() == TRAIT_Function ||
                    t.getKind() == TRAIT_Getter ||
                    t.getKind() == TRAIT_Setter)
                {
                    if (t.getBooleanAttr(Trait.TRAIT_FINAL))
                    {
                        final Name name = t.getName();
                        /*
                         * provideName =
                         * JSGeneratingReducer.getPackageNameFromName
                         * (name,false) + "."
                         * +JSGeneratingReducer.getBasenameFromName(name); if(
                         * !JSSharedData.ROOT_NAME.isEmpty() &&
                         * !provideName.startsWith(JSSharedData.ROOT_NAME))
                         * provideName = JSSharedData.ROOT_NAME + provideName;
                         */

                        final IDefinition def = getDefinition(name);
                        provideName = def.getQualifiedName();
                    }
                }
            }
            else
            {
                return;
            }
        }

        if (provideName == null)
        {
            final Boolean emitClassPrologue = !sharedData.hasClassBeenEmitted(ii.name);
            /*
             * This is WRONG: if( emitClassPrologue && m_buildPhase ==
             * Operation.GET_ABC_BYTES ) because classes don't get registered if
             * needsSecondPass is false. JSCompilationUnit returns the generated
             * JS from the first pass if needsSecondPass is false and
             * registerEmittedClass() will never be called for this class.
             */
            if (emitClassPrologue)
            {
                // Register regardless of phase, since ABC phase might not happen (if JSGeneratingReducer.needsSecondPass == false)
                // And registerEmittedClass() properly handles duplicate registrations, if we do happen to do both passes.
                sharedData.registerEmittedClass(ii.name);
            }

            provideName = createFullName(packageName, className);
        }

        // register class with super class
        final IDefinition superClassDef = getDefinition(ii.superName);
        final String superClassName = superClassDef == null ? "Object" : superClassDef.getQualifiedName();
        sharedData.registerClass(ii.name, ii.superName);

        // TODO: Special case: className.equals( "Package" )

        if (className.isEmpty());
        else if (classDef != null)
        {
        	String classQName = classDef.getQualifiedName();
            writeString("goog.provide('" + classQName + "');\n\n");
            FlexJSProject project = (FlexJSProject)m_project;
            ArrayList<String> deps = project.getRequires(m_generator.m_compilationUnit);
            emitRequires(deps, classQName);
        }
        else
            writeString("goog.provide('" + className + "');\n");

        final MethodInfo ctor = clz.instanceInfo.name == null ? null : clz.instanceInfo.iInit;
        // first check whether there is any code...
        final String body = getCodeForConstructor(ctor);

    	writeString("/**\n");
        if (!JSGeneratingReducer.isDataType(superClassName) || !body.isEmpty())
        {
            writeString(" * @constructor\n");
            writeString(" * @extends {" + superClassName + "}\n");
            emitJSDocForParams(ctor, "");
        }

        writeString(" */\n");

        if (ii.hasProtectedNs())
            writeString("// protected: " + ii.protectedNs + "\n");

        for (Name i : ii.interfaceNames)
            writeString("// interface: " + i.toString() + "\n");

        // every class extends another class or Object except for the framework class.
        if (!className.equals(JSSharedData.JS_FRAMEWORK_NAME))
        {
            if (JSSharedData.m_useClosureLib)
            {
                /*
                 * TODO: redundant? if( provideName != null &&
                 * !className.isEmpty() ) { emitPackageNames( provideName );
                 * writeString( "goog.provide(\"" + provideName + "\");\n" ); }
                 */

                emitInstanceTraits(clz.instanceTraits, ctor, packageName, className, superClassName, isExtern, isInterface, isPackageFunction);
            }
            else
            {
                // provide()
                /*
                 * TODO: redundant? if( provideName != null ) {
                 * emitPackageNames( provideName ); writeString(
                 * JSSharedData.JS_FRAMEWORK_NAME + ".provide(\"" + provideName
                 * + "\");\n" ); }
                 */

                final String fullName = createFullName(packageName, className);
                writeString(fullName + " = function(");
                String a_priori_insns = emitParameters(ctor);
                writeString(") {\n");
                writeString("    " + superClassName + ".call(this);\n\n");
                if (!a_priori_insns.isEmpty())
                {
                    writeString(a_priori_insns);
                }
                emitInstanceTraits(clz.instanceTraits, ctor, packageName, className, superClassName, isExtern, isInterface, isPackageFunction);
            }
        }

        final Boolean isDynamicClass = classDef != null && classDef.isDynamic();
        final Boolean isFinalClass = classDef != null && classDef.isFinal();
        emitClassTraits(clz.classTraits, clz.classInfo.cInit,
                        packageName, className, superClassName,
                        isExtern, isPackageFunction, isDynamicClass, isFinalClass,
                        provideName != null);

        // constructor, ii.iInit is a MethodInfo
        // w.writeU30(getMethodId(ii.iInit));

        // finish with an extra line feed
        writeString("\n");
    }

    protected void emitRequires(ArrayList<String> deps, String classQName)
    {
    	HashMap<String, String> already = new HashMap<String, String>();
    	for (String imp : deps)
    	{
            if (imp.indexOf("__AS3__") != -1)
                continue;
            if (imp.equals(classQName))
            	continue;
            if (imp.equals("Array"))
            	continue;
            if (imp.equals("Boolean"))
            	continue;
            if (imp.equals("decodeURI"))
            	continue;
            if (imp.equals("decodeURIComponent"))
            	continue;
            if (imp.equals("encodeURI"))
            	continue;
            if (imp.equals("encodeURIComponent"))
            	continue;
            if (imp.equals("Error"))
            	continue;
            if (imp.equals("Function"))
            	continue;
            if (imp.equals("JSON"))
            	continue;
            if (imp.equals("Number"))
            	continue;
            if (imp.equals("int"))
            	continue;
            if (imp.equals("Object"))
            	continue;
            if (imp.equals("RegExp"))
            	continue;
            if (imp.equals("String"))
            	continue;
            if (imp.equals("uint"))
            	continue;
            if (!already.containsKey(imp))
            	writeString("goog.require('" + imp + "');\n");
            already.put(imp, imp);
    	}
        writeString("\n");    	
    }
    
    private Boolean hasTrait(Traits traits, String baseName, int kind)
    {
        for (Trait t : traits)
        {
            if (t.getKind() == kind)
            {
                Name name = t.getNameAttr("name");
                if (JSGeneratingReducer.getBasenameFromName(name).equals(baseName))
                    return true;
            }
        }
        return false;
    }

    public Boolean hasSetter(IDefinition def, String baseName)
    {
        for (EmitterClassVisitor clz : definedClasses)
        {
            if (hasTrait(clz.instanceInfo.traits, baseName, TRAIT_Setter))
                return true;
            if (hasTrait(clz.classInfo.classTraits, baseName, TRAIT_Setter))
                return true;
        }
        return false;
    }

    public String getCurrentClassName()
    {
        if (m_currentClassVisitor != null)
        {
            return JSGeneratingReducer.getBasenameFromName(m_currentClassVisitor.instanceInfo.name);
        }
        return null;
    }

    public String getCurrentSuperClassName()
    {
        if (m_currentClassVisitor != null)
        {
            return JSGeneratingReducer.getBasenameFromName(m_currentClassVisitor.instanceInfo.superName);
        }
        return null;
    }

    public String getCurrentSuperClassFullName()
    {
        if (m_currentClassVisitor != null)
        {
            final IDefinition def = getDefinition(m_currentClassVisitor.instanceInfo.superName);
            return def.getQualifiedName();
        }
        return null;
    }

    public String getCurrentPackageName()
    {
        if (m_currentClassVisitor != null)
        {
            final IDefinition def = getDefinition(m_currentClassVisitor.instanceInfo.name);
            if (def != null)
                return def.getPackageName();
            return m_packageName;
        }
        return null;
    }

    public IClassVisitor getCurrentClassVisitor()
    {
        return m_currentClassVisitor;
    }

    public void visitImport(String importName, ImportKind importKind)
    {
        // we can skip implicit nodes.
        if (importKind != ImportKind.IMPLICIT_IMPORT)
        {
            m_importNames.add(importName);
        }
    }

    public void visitUseNamespace(String namespaceName)
    {
        m_useNames.add(namespaceName);
    }

    public void visitFunctionDefinition(MethodInfo mi, FunctionDefinition funcDef)
    {
        if (!m_methodInfoToDefinition.containsKey(mi))
        {
            m_methodInfoToDefinition.put(mi, funcDef);
            m_definitionToMethodInfo.put(funcDef, mi);
        }
    }

    public IClassVisitor visitPackage(String packageName)
    {
        if (packageName == null)
            packageName = "";

        m_packageName = packageName;

        InstanceInfo iinfo = new InstanceInfo();
        // iinfo.name = JSGeneratingReducer.makeName(packageName);
        final ClassInfo ci = new ClassInfo();
        // ci.cInit = mi;
        IClassVisitor cv = visitClass(iinfo, ci);
        return cv;
    }
}
