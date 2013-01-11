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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.abc.semantics.Nsset;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IMemberedDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.internal.driver.IBackend;
import org.apache.flex.compiler.internal.legacy.MemberedDefinitionUtils;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.AccessValue;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.ClassificationValue;
import org.apache.flex.compiler.internal.legacy.ASDefinitionFilter.SearchScopeValue;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.swf.SWFFrame;

import static org.apache.flex.abc.ABCConstants.OP_nop;

/**
 * JSSharedData contains information that is being shared across
 * JSCompilationUnit, JSGenerator, and JSEmitter instances.
 * JSSharedData.COMPILER_VERSION holds FalconJS's version, which uses the Falcon
 * change list number as the major revision number. The minor revision number
 * represents FalconJS's internal version within the Falcon change list number
 * it is based on. This implementation is part of FalconJS. For more details on
 * FalconJS see org.apache.flex.compiler.JSDriver
 */
public class JSSharedData
{
    public static String COMPILER_NAME = "MXMLJSC";
    public final static String COMPILER_VERSION = "329449.1";

    public final static JSSharedData instance = new JSSharedData();
    public static IBackend backend = null;
    public static Workspace workspace = null;

    public final static String JS_FRAMEWORK_NAME = "org.apache.flex.FlexGlobal";
    public final static String ROOT_NAME = ""; // JS_FRAMEWORK_NAME + ".root.";
    public static String FRAMEWORK_CLASS = "browser.JQueryFramework";
    public static String BUILT_IN = "builtin.abc";
    public static String MAIN = null;
    public static Boolean NO_EXPORTS = true;
    public static Boolean OUTPUT_TIMESTAMPS = false;
    public static Boolean OUTPUT_ISOLATED = true;
    public static Boolean GENERATE_TEST_CASE = false;
    public static Boolean WARN_PERFORMANCE_LOSS = false;
    public static Boolean WARN_RUNTIME_NAME_LOOKUP = false; // JSWarnRuntimeNameLookupProblem
    public static Boolean WARN_CLASS_INIT = false; // JSWarnClassInitProblem
    public static Boolean DEBUG = false;
    public static Boolean OPTIMIZE = false;
    public static Boolean EXTEND_DOM = true;
    public static Boolean KEEP_GENERATED_AS = false;

    public static PrintStream STDOUT = System.out;
    public static PrintStream STDERR = System.err;
    public static String OUTPUT_FOLDER = ".";
    public static String OUTPUT_EXTENSION = "js";
    public static String SDK_PATH = null;

    public static String CLOSURE_compilation_level = "SIMPLE_OPTIMIZATIONS";
    public static List<String> CLOSURE_externs = null;
    public static List<String> CLOSURE_js = null;
    public static String CLOSURE_create_source_map = null;
    public static String CLOSURE_formatting = null;

    // public final static String FRAMEWORK_CLASS = "browser.ClosureFramework";
    public final static String m_superCalledMarker = "___SUPER_HAS_BEEN_CALLED___ ";
    public final static String THIS = "this";
    public final static String SUPER = "super";
    public final static String _SUPER = "_super";
    public final static String THIS_SUPER = THIS + "." + SUPER;
    public final static String JS_THIS = "this";
    public final static Boolean m_useClosureLib = false;
    public final static Boolean m_useSelfParameter = false;
    public final static int OP_JS = OP_nop;
    public final static String DEFAULT_PARAM_PREFIX = "_default_";
    public final static String GETTER_PREFIX = "get_";
    public final static String SETTER_PREFIX = "set_";
    public final static String CTOR_NAME = "init";
    public final static String EXTERN_INTERFACE_NAME = "IExtern";
    public final static String SYMBOL_INTERFACE_NAME = "ISymbol";
    public final static String FRAMEWORK_INTERFACE_NAME = "IFramework";
    public final static String CLASS_NAME = "_CLASS";
    public final static String SYMBOL_GET_LINKAGE = "getLinkage"; // see browser.ISymbol 
    public final static String SYMBOL_INIT = "init"; // see browser.ISymbol 
    public final static String SYMBOL_INSTANCE = "instance"; // see browser.ISymbol 
    public final static String GENERATE_CLASSINFO = "GenerateClassInfo"; // see browser.ISymbol 
    public final static String CONVERT_LOCAL_VARS_TO_MEMBERS = "ConvertLocalVarsToMembers";
    // public final static String JS_STATIC_INITS = JS_FRAMEWORK_NAME + ".m_staticInits";
    public final static String JS_RUN_STATIC_INITS = JS_FRAMEWORK_NAME + ".runStaticInits";
    public final static String JS_SYMBOLS = JS_FRAMEWORK_NAME + ".m_symbols";
    public final static String JS_CLASSES = JS_FRAMEWORK_NAME + ".classes";
    public final static String JS_TESTCASES = JS_FRAMEWORK_NAME + ".testCases";
    public final static String JS_INT_CLASS = JS_FRAMEWORK_NAME + ".intClass";
    public final static String JS_UINT_CLASS = JS_FRAMEWORK_NAME + ".uintClass";
    public final static String JS_EMPTY_FUNCTION = JS_FRAMEWORK_NAME + ".emptyFunction";
    public final static String REQUIRED_TAG_MARKER = "__REQUIRED__";
    public final static String EXTERN_METATAG = "Extern";
    public final static String DATACLASS_METATAG = "DataClass";
    public final static String TESTCASE_METATAG = "TestCase";
    public final static String AS3XML = "browser.AS3XML";
    public final static String AS3XMLList = "browser.AS3XMLList";
    public final static String STATIC_INIT = "__static_init";

    public final static byte CONSTANT_Namespace = 1;
    public final static byte CONSTANT_PackageNs = 2;
    public final static byte CONSTANT_PackageInternalNs = 3;
    public final static byte CONSTANT_ProtectedNs = 4;
    public final static byte CONSTANT_ExplicitNamespace = 5;
    public final static byte CONSTANT_StaticProtectedNs = 6;
    public final static byte CONSTANT_PrivateNs = 7;
    public final static byte CONSTANT_ClassPrivateNS = 8;

    private Set<String> m_packages = new TreeSet<String>();
    private final ReadWriteLock m_packagesLock = new ReentrantReadWriteLock();

    private Map<Name, Name> m_classes = new HashMap<Name, Name>();
    private final ReadWriteLock m_classesLock = new ReentrantReadWriteLock();

    private Map<Name, Set<Name>> m_interfaces = new HashMap<Name, Set<Name>>();
    private final ReadWriteLock m_interfacesLock = new ReentrantReadWriteLock();

    private Map<String, String> m_externs = new HashMap<String, String>();
    private final ReadWriteLock m_externsLock = new ReentrantReadWriteLock();

    private Map<String, String> m_varTypes = new HashMap<String, String>();
    private final ReadWriteLock m_varTypesLock = new ReentrantReadWriteLock();

    private Boolean m_verbose = false;
    private final ReadWriteLock m_verboseLock = new ReentrantReadWriteLock();

    private Object m_codeGenMonitor = new Object();
    private long m_codeGenCounter = 0;
    private final ReadWriteLock m_codeGenCounterLock = new ReentrantReadWriteLock();

    private Set<Name> m_emittedClasses = new HashSet<Name>();
    private final ReadWriteLock m_emittedClassesLock = new ReentrantReadWriteLock();

    private Map<String, INamespaceDefinition> m_uriToNamespace = new HashMap<String, INamespaceDefinition>();
    private Map<String, INamespaceDefinition> m_qnameToNamespace = new HashMap<String, INamespaceDefinition>();
    private final ReadWriteLock m_uriToNamespaceLock = new ReentrantReadWriteLock();

    private Map<String, String> m_classToSymbol = new HashMap<String, String>();
    private final ReadWriteLock m_classToSymbolLock = new ReentrantReadWriteLock();

    // private Map<IDefinition,Set<IDefinition>> m_dependencies = new HashMap<IDefinition,Set<IDefinition>>();
    // private final ReadWriteLock m_dependenciesLock = new ReentrantReadWriteLock();

    private Map<String, String> m_compilationUnitToJS = new HashMap<String, String>();
    private final ReadWriteLock m_compilationUnitToJSLock = new ReentrantReadWriteLock();

    private Map<SWFFrame, ICompilationUnit> m_swfFrameToCompilationUnit = new HashMap<SWFFrame, ICompilationUnit>();
    private final ReadWriteLock m_swfFrameToCompilationUnitLock = new ReentrantReadWriteLock();

    private Set<String> m_referencedDefinitions = new HashSet<String>();
    private final ReadWriteLock m_referencedDefinitionsLock = new ReentrantReadWriteLock();

    private Map<String, IDefinition> m_definitions = new HashMap<String, IDefinition>();
    private final ReadWriteLock m_definitionsLock = new ReentrantReadWriteLock();

    private Set<String> m_usedExterns = new HashSet<String>();
    private final ReadWriteLock m_usedExternsLock = new ReentrantReadWriteLock();

    private Set<String> m_scriptInfos = new HashSet<String>();
    private final ReadWriteLock m_scriptInfosLock = new ReentrantReadWriteLock();

    private Map<String, Integer> m_propertyNames = new HashMap<String, Integer>();
    private final ReadWriteLock m_propertyNamesLock = new ReentrantReadWriteLock();

    private Set<String> m_accessedPropertyNames = new HashSet<String>();
    private final ReadWriteLock m_accessedPropertyNamesLock = new ReentrantReadWriteLock();

    private Set<String> m_classInit = new HashSet<String>();
    private final ReadWriteLock m_classInitLock = new ReentrantReadWriteLock();

    private Map<String, String> m_pathToUUID = new HashMap<String, String>();
    private final ReadWriteLock m_pathToUUIDLock = new ReentrantReadWriteLock();

    private Set<String> m_encryptedJS = new HashSet<String>();
    private final ReadWriteLock m_encryptedJSLock = new ReentrantReadWriteLock();

    private Map<String, Set<String>> m_methods = new HashMap<String, Set<String>>();
    private final ReadWriteLock m_methodsLock = new ReentrantReadWriteLock();

    public void reset()
    {
        m_packages.clear();
        m_classes.clear();
        m_interfaces.clear();
        m_externs.clear();
        m_varTypes.clear();
        m_verbose = false;
        // m_codeGenMonitor
        m_emittedClasses.clear();
        m_uriToNamespace.clear();
        m_qnameToNamespace.clear();
        m_classToSymbol.clear();
        // m_dependencies.clear();
        m_compilationUnitToJS.clear();
        m_swfFrameToCompilationUnit.clear();
        m_referencedDefinitions.clear();
        m_definitions.clear();
        m_usedExterns.clear();
        m_scriptInfos.clear();
        m_propertyNames.clear();
        m_accessedPropertyNames.clear();
        m_classInit.clear();
        // m_pathToUUID.clear();
        m_encryptedJS.clear();
        m_methods.clear();
    }

    /**
     * @param m_packages the m_packages to set
     */
    public void registerPackage(String packageName)
    {
        // TODO: generalize, avoid hard-coded "goog"
        if (packageName != null && !packageName.isEmpty() && !packageName.startsWith("goog"))
        {
            m_packagesLock.writeLock().lock();
            this.m_packages.add(packageName);
            m_packagesLock.writeLock().unlock();
        }
    }

    /**
     * @return the m_packages
     */
    public Boolean hasPackage(String packageName)
    {
        m_packagesLock.readLock().lock();
        final Boolean val = m_packages.contains(packageName);
        m_packagesLock.readLock().unlock();
        return val;
    }

    /**
     * @return the m_packages
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPackages()
    {
        Set<String> packages = null;
        m_packagesLock.readLock().lock();
        Object clone = ((TreeSet<String>)m_packages).clone();
        if (clone != null && clone instanceof Set<?>)
            packages = (Set<String>)(clone);
        m_packagesLock.readLock().unlock();
        return packages;
    }

    /**
     * @param m_varTypes the m_varTypes to set
     */
    public void registerVarType(String varName, String varType)
    {
        m_varTypesLock.writeLock().lock();
        this.m_varTypes.put(varName, varType);
        m_varTypesLock.writeLock().unlock();
    }

    /**
     * @return the m_varTypes
     */
    public String getVarType(String varName)
    {
        m_varTypesLock.readLock().lock();
        final String val = m_varTypes.get(varName);
        m_varTypesLock.readLock().unlock();
        return val;
    }

    public Boolean hasVarName(String varName)
    {
        m_varTypesLock.readLock().lock();
        final Boolean val = m_varTypes.containsKey(varName);
        m_varTypesLock.readLock().unlock();
        return val;
    }

    /**
     * @param m_classes the m_classes to set
     */
    public void registerClass(Name className, Name superName)
    {
        m_classesLock.writeLock().lock();
        this.m_classes.put(className, superName);
        m_classesLock.writeLock().unlock();
    }

    /**
     * @return the m_classes
     */
    public Name getSuperClass(Name className)
    {
        m_classesLock.readLock().lock();
        final Name val = m_classes.get(className);
        m_classesLock.readLock().unlock();
        return val;
    }

    public Boolean hasClass(Name className)
    {
        m_classesLock.readLock().lock();
        final Boolean val = m_classes.containsKey(className);
        m_classesLock.readLock().unlock();
        return val;
    }

    public Map<String, String> getClassInfo()
    {
        // copying the whole m_classes is the only thing you can do 
        // if you want to stay thread safe.
        Map<String, String> info = new HashMap<String, String>();

        m_classesLock.readLock().lock();
        Iterator<Map.Entry<Name, Name>> it = m_classes.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Name, Name> pair = it.next();
            final Name _class = pair.getKey();
            final Name _super = pair.getValue();
            if (_super != null)
                info.put(_class.getBaseName(), _super.getBaseName());
            else
                info.put(_class.getBaseName(), "Object");
        }
        m_classesLock.readLock().unlock();

        return info;
    }

    public Set<Name> getClasses()
    {
        m_classesLock.readLock().lock();

        Set<Name> info = new HashSet<Name>();
        info.addAll(m_classes.keySet());
        m_classesLock.readLock().unlock();

        return info;
    }

    public void setVerbose(Boolean value)
    {
        m_verboseLock.writeLock().lock();
        this.m_verbose = value;
        m_verboseLock.writeLock().unlock();
    }

    public Boolean isVerbose()
    {
        m_verboseLock.readLock().lock();
        final Boolean val = m_verbose;
        m_verboseLock.readLock().unlock();
        return val;
    }

    public void stdout(String s)
    {
        if (STDOUT != null)
        {
            m_verboseLock.writeLock().lock();
            STDOUT.println(s);
            m_verboseLock.writeLock().unlock();
        }
    }

    public void stderr(String s)
    {
        if (STDERR != null)
        {
            m_verboseLock.writeLock().lock();
            STDERR.println(s);
            m_verboseLock.writeLock().unlock();
        }
    }

    public void verboseMessage(String s)
    {
        if (isVerbose() && STDOUT != null)
        {
            m_verboseLock.writeLock().lock();
            STDOUT.println(s);
            m_verboseLock.writeLock().unlock();
        }
    }

    public void beginCodeGen()
    {
        m_codeGenCounterLock.writeLock().lock();
        m_codeGenCounter++;
        m_codeGenCounterLock.writeLock().unlock();
    }

    public void endCodeGen()
    {
        m_codeGenCounterLock.writeLock().lock();
        final long currentCounter = --m_codeGenCounter;
        m_codeGenCounterLock.writeLock().unlock();

        if (currentCounter == 0)
        {
            synchronized (m_codeGenMonitor)
            {
                m_codeGenMonitor.notifyAll();
            }
        }
    }

    /*
     * private long getCodeGenCounter() {
     * m_codeGenCounterLock.readLock().lock(); final long val =
     * m_codeGenCounter; m_codeGenCounterLock.readLock().unlock(); return val; }
     * private void waitUntilCodeGenHasFinished() throws InterruptedException {
     * if( getCodeGenCounter() == 0 ) { synchronized (m_codeGenMonitor) {
     * m_codeGenMonitor.wait(); } } }
     */

    public void registerInterface(Name className, Name interfaceName)
    {
        m_interfacesLock.writeLock().lock();
        Set<Name> interfaces = m_interfaces.get(className);
        if (interfaces == null)
        {
            interfaces = new HashSet<Name>();
            this.m_interfaces.put(className, interfaces);
        }
        interfaces.add(interfaceName);
        m_interfacesLock.writeLock().unlock();
    }

    @SuppressWarnings("unchecked")
    public Map<Name, Set<Name>> getAllInterfaces()
    {
        // copying the whole m_classes is the only thing you can do 
        // if you want to stay thread safe.
        Map<Name, Set<Name>> info = new HashMap<Name, Set<Name>>();

        m_interfacesLock.readLock().lock();
        Iterator<Map.Entry<Name, Set<Name>>> it = m_interfaces.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Name, Set<Name>> pair = it.next();
            final Name _class = pair.getKey();
            final HashSet<Name> interfaces = (HashSet<Name>)pair.getValue();
            info.put(_class, (Set<Name>)(interfaces.clone()));
        }
        m_interfacesLock.readLock().unlock();

        return info;
    }

    public void registerExtern(Name name)
    {
        final IDefinition def = getDefinition(name);
        if (def == null)
            throw backend.createException("registerExtern: can't find definition for " + name.toString());
        registerExtern(def);
    }

    public void registerExtern(IDefinition def)
    {
        final IMetaTag extern = def.getMetaTagByName(EXTERN_METATAG);
        if (extern != null)
        {
            String externName = extern.getAttributeValue("name");
            if (externName == null)
                externName = def.getBaseName();

            m_externsLock.writeLock().lock();
            m_externs.put(def.getQualifiedName(), externName);
            m_externsLock.writeLock().unlock();
        }
    }

    public Boolean isExtern(String name)
    {
        m_externsLock.readLock().lock();
        final Boolean val = m_externs.containsKey(name);
        m_externsLock.readLock().unlock();
        return val;
    }

    public String getExternName(String name)
    {
        m_externsLock.readLock().lock();
        final String val = m_externs.get(name);
        m_externsLock.readLock().unlock();
        return val;
    }

    public Set<String> getExterns()
    {
        final Set<String> externs = new HashSet<String>();
        m_externsLock.readLock().lock();
        externs.addAll(m_externs.keySet());
        m_externsLock.readLock().unlock();
        return externs;
    }

    public void registerEmittedClass(Name classOrInterfaceName)
    {
        m_emittedClassesLock.writeLock().lock();
        this.m_emittedClasses.add(classOrInterfaceName);
        m_emittedClassesLock.writeLock().unlock();
    }

    public Boolean hasClassBeenEmitted(Name classOrInterfaceName)
    {
        m_emittedClassesLock.readLock().lock();
        final Boolean val = m_emittedClasses.contains(classOrInterfaceName);
        m_emittedClassesLock.readLock().unlock();
        return val;
    }

    public void registerSymbol(String className, String symbolName)
    {
        m_classToSymbolLock.writeLock().lock();
        this.m_classToSymbol.put(className, symbolName);
        m_classToSymbolLock.writeLock().unlock();
    }

    public String getSymbol(String className)
    {
        m_classToSymbolLock.writeLock().lock();
        final String symbol = this.m_classToSymbol.get(className);
        m_classToSymbolLock.writeLock().unlock();
        return symbol;
    }

    public Boolean hasSymbols()
    {
        m_classToSymbolLock.writeLock().lock();
        final Boolean hasSymbols = !this.m_classToSymbol.isEmpty();
        m_classToSymbolLock.writeLock().unlock();
        return hasSymbols;
    }

    public List<String> getSymbols()
    {
        // copying the whole m_classes is the only thing you can do 
        // if you want to stay thread safe.
        List<String> info = new ArrayList<String>();
        m_classToSymbolLock.readLock().lock();
        info.addAll(m_classToSymbol.values());
        m_classToSymbolLock.readLock().unlock();

        return info;
    }

    public void registerNamespace(String uri, INamespaceDefinition ns)
    {
        m_uriToNamespaceLock.writeLock().lock();
        this.m_uriToNamespace.put(uri, ns);
        this.m_qnameToNamespace.put(ns.getQualifiedName(), ns);
        m_uriToNamespaceLock.writeLock().unlock();
    }

    public Map<String, INamespaceDefinition> getNamespaces()
    {
        // Copy the whole m_uriToNamespace for thread-safety
        Map<String, INamespaceDefinition> namespaces = new HashMap<String, INamespaceDefinition>();
        m_uriToNamespaceLock.writeLock().lock();
        namespaces.putAll(m_uriToNamespace);
        m_uriToNamespaceLock.writeLock().unlock();
        return namespaces;
    }

    public INamespaceDefinition getNamespace(String uri)
    {
        m_uriToNamespaceLock.writeLock().lock();
        final INamespaceDefinition ns = this.m_uriToNamespace.get(uri);
        m_uriToNamespaceLock.writeLock().unlock();
        return ns;
    }

    public INamespaceDefinition getNamespaceForQName(String qname)
    {
        m_uriToNamespaceLock.writeLock().lock();
        final INamespaceDefinition ns = this.m_qnameToNamespace.get(qname);
        m_uriToNamespaceLock.writeLock().unlock();
        return ns;
    }

    /*
     * // returns true if def1 depends on def2 private Boolean dependsOn(
     * IDefinition def1, IDefinition def2 ) { Set<IDefinition> defs =
     * m_dependencies.get(def2); if( defs.contains(def1) ) return true; for(
     * IDefinition next: defs ) { if( dependsOn(def1, next) ) return true; }
     * return false; } // workaround for Falcon bugs // Falcon's DependencyGraph
     * does not work correctly for Definitions // implemented by SWCs. // def1
     * depends on def2 public void addDependency( IDefinition def1, IDefinition
     * def2 ) { if( def1 != def2 ) { m_dependenciesLock.writeLock().lock();
     * Set<IDefinition> defs = m_dependencies.get(def1); if( defs == null ) {
     * defs = new HashSet<IDefinition>(); m_dependencies.put( def1, defs ); }
     * else { if( !defs.contains(def2) && dependsOn(def2, def1) ) throw new
     * Error( "addDependency: circular dependencies!" ); } defs.add(def2);
     * m_dependenciesLock.writeLock().unlock(); } } public Set<IDefinition>
     * getDependencies( IDefinition def ) {
     * m_dependenciesLock.writeLock().lock(); final Set<IDefinition> defs =
     * m_dependencies.get(def); m_dependenciesLock.writeLock().unlock(); return
     * defs; }
     */

    public void registerJavaScript(String className, String jsCode)
    {
        m_compilationUnitToJSLock.writeLock().lock();
        m_compilationUnitToJS.put(className, jsCode);
        m_compilationUnitToJSLock.writeLock().unlock();
    }

    public void registerJavaScript(Map<String, String> classNameToJS)
    {
        m_compilationUnitToJSLock.writeLock().lock();
        m_compilationUnitToJS.putAll(classNameToJS);
        m_compilationUnitToJSLock.writeLock().unlock();
    }

    public Boolean hasJavaScript(String className)
    {
        m_compilationUnitToJSLock.writeLock().lock();
        final Boolean found = m_compilationUnitToJS.containsKey(className);
        m_compilationUnitToJSLock.writeLock().unlock();
        return found;
    }

    public String getJavaScript(String className)
    {
        m_compilationUnitToJSLock.writeLock().lock();
        final String jsCode = m_compilationUnitToJS.get(className);
        m_compilationUnitToJSLock.writeLock().unlock();
        return jsCode;
    }

    public void registerSWFFrame(SWFFrame frame, ICompilationUnit cu)
    {
        m_swfFrameToCompilationUnitLock.writeLock().lock();
        m_swfFrameToCompilationUnit.put(frame, cu);
        m_swfFrameToCompilationUnitLock.writeLock().unlock();
    }

    public ICompilationUnit getCompilationUnit(SWFFrame frame)
    {
        m_swfFrameToCompilationUnitLock.writeLock().lock();
        final ICompilationUnit cu = m_swfFrameToCompilationUnit.get(frame);
        m_swfFrameToCompilationUnitLock.writeLock().unlock();
        return cu;
    }

    public void registerReferencedDefinition(String name)
    {
        m_referencedDefinitionsLock.writeLock().lock();
        m_referencedDefinitions.add(name);
        m_referencedDefinitionsLock.writeLock().unlock();
    }

    public Boolean isReferencedDefinition(String name)
    {
        m_referencedDefinitionsLock.readLock().lock();
        final Boolean val = m_referencedDefinitions.contains(name);
        m_referencedDefinitionsLock.readLock().unlock();
        return val;
    }

    public void registerDefinition(IDefinition def)
    {
        m_definitionsLock.writeLock().lock();
        final String qName = def.getQualifiedName();
        this.m_definitions.put(qName, def);
        m_definitionsLock.writeLock().unlock();

        // register this definition as an extern if necessary.
        final IMetaTag extern = def.getMetaTagByName(JSSharedData.EXTERN_METATAG);
        if (extern != null)
            registerExtern(def);
    }

    public IDefinition getDefinition(String qualifiedName)
    {
        m_definitionsLock.readLock().lock();
        final IDefinition val = m_definitions.get(qualifiedName);
        m_definitionsLock.readLock().unlock();
        return val;
    }

    public IDefinition getDefinition(Name name)
    {
        m_definitionsLock.readLock().lock();

        IDefinition val = null;
        final String baseName = name.getBaseName();
        if (baseName != null)
        {
            final Nsset nsset = name.getQualifiers();
            if (nsset != null)
            {
                for (Namespace ns : nsset)
                {
                    String defName = ns.getName();
                    if (!defName.isEmpty())
                        defName += ".";
                    defName += name.getBaseName();
                    val = m_definitions.get(defName);
                    if (val != null)
                        break;
                }
            }
        }

        if (val == null)
            val = m_definitions.get(baseName);

        m_definitionsLock.readLock().unlock();
        return val;
    }

    public String getQualifiedName(Name name)
    {
        final IDefinition def = getDefinition(name);
        if (def != null)
            return def.getQualifiedName();
        return null;
    }

    public void registerUsedExtern(String name)
    {
        m_usedExternsLock.writeLock().lock();
        m_usedExterns.add(name);
        m_usedExternsLock.writeLock().unlock();
    }

    public Boolean isUsedExtern(String name)
    {
        m_usedExternsLock.readLock().lock();
        final Boolean val = m_usedExterns.contains(name);
        m_usedExternsLock.readLock().unlock();
        return val;
    }

    public Set<String> getUsedExterns()
    {
        final Set<String> externs = new HashSet<String>();
        m_scriptInfosLock.readLock().lock();
        externs.addAll(m_scriptInfos);
        m_scriptInfosLock.readLock().unlock();
        return externs;
    }

    public void registerScriptInfo(String name)
    {
        m_scriptInfosLock.writeLock().lock();
        m_scriptInfos.add(name);
        m_scriptInfosLock.writeLock().unlock();
    }

    public Set<String> getScriptInfos()
    {
        final Set<String> scriptInfos = new HashSet<String>();
        m_scriptInfosLock.readLock().lock();
        scriptInfos.addAll(m_scriptInfos);
        m_scriptInfosLock.readLock().unlock();
        return scriptInfos;
    }

    public void registerPropertyName(String name)
    {
        m_propertyNamesLock.writeLock().lock();

        if (m_propertyNames.containsKey(name))
        {
            final Integer uses = m_propertyNames.get(name);
            m_propertyNames.put(name, uses + 1);
        }
        else
        {
            m_propertyNames.put(name, 1);
        }
        m_propertyNamesLock.writeLock().unlock();
    }

    public Boolean isUniquePropertyName(String name)
    {
        Boolean isUnique = true;
        m_propertyNamesLock.readLock().lock();
        if (m_propertyNames.containsKey(name))
        {
            final Integer uses = m_propertyNames.get(name);
            if (uses > 1)
                isUnique = false;
        }
        m_propertyNamesLock.readLock().unlock();
        return isUnique;
    }

    // called whenever we have to emit adobe.get/set/call property.
    public void registerAccessedPropertyName(String name)
    {
        m_accessedPropertyNamesLock.writeLock().lock();
        m_accessedPropertyNames.add(name);
        m_accessedPropertyNamesLock.writeLock().unlock();
    }

    public Boolean isAccessedPropertyName(String name)
    {
        m_accessedPropertyNamesLock.readLock().lock();
        final Boolean val = m_accessedPropertyNames.contains(name);
        m_accessedPropertyNamesLock.readLock().unlock();
        return val;
    }

    public static Boolean usingAdvancedOptimizations()
    {
        return CLOSURE_compilation_level.equals("ADVANCED_OPTIMIZATIONS");
    }

    // called whenever we have to emit adobe.get/set/call property.
    public void registerClassInit(String fullClassName)
    {
        if (fullClassName != null && !fullClassName.isEmpty())
        {
            m_classInitLock.writeLock().lock();
            m_classInit.add(fullClassName);
            m_classInitLock.writeLock().unlock();
        }
    }

    public Boolean hasClassInit(String fullClassName)
    {
        m_classInitLock.readLock().lock();
        final Boolean val = m_classInit.contains(fullClassName);
        m_classInitLock.readLock().unlock();
        return val;
    }

    public Boolean hasAnyClassInit()
    {
        m_classInitLock.readLock().lock();
        final Boolean val = m_classInit.isEmpty();
        m_classInitLock.readLock().unlock();
        return !val;
    }

    public void registerUUID(String path, String uuid)
    {
        m_pathToUUIDLock.writeLock().lock();
        m_pathToUUID.put(path, uuid);
        m_pathToUUIDLock.writeLock().unlock();
    }

    public String getUUID(String path)
    {
        m_pathToUUIDLock.readLock().lock();
        final String val = m_pathToUUID.get(path);
        m_pathToUUIDLock.readLock().unlock();
        return val;
    }

    public void registerEncryptedJS(String name)
    {
        m_encryptedJSLock.writeLock().lock();
        m_encryptedJS.add(name);
        m_encryptedJSLock.writeLock().unlock();
    }

    public Boolean hasEncryptedJS(String name)
    {
        m_encryptedJSLock.readLock().lock();
        final Boolean val = m_encryptedJS.contains(name);
        m_encryptedJSLock.readLock().unlock();
        return val;
    }

    public Boolean hasAnyEncryptedJS()
    {
        m_encryptedJSLock.readLock().lock();
        final Boolean val = !m_encryptedJS.isEmpty();
        m_encryptedJSLock.readLock().unlock();
        return val;
    }

    public void registerMethod(String methodName, String className)
    {
        m_methodsLock.writeLock().lock();
        Set<String> classes = m_methods.get(methodName);
        if (classes == null)
        {
            classes = new HashSet<String>();
            m_methods.put(methodName, classes);
        }
        classes.add(className);
        m_methodsLock.writeLock().unlock();
    }

    // poor man's type inference.
    public void registerMethods(ICompilerProject project, IMemberedDefinition memberedDef)
    {
        final String className = memberedDef.getQualifiedName();
        final ASDefinitionFilter memberFilter = new ASDefinitionFilter(
                ClassificationValue.FUNCTIONS, SearchScopeValue.INHERITED_MEMBERS,
                AccessValue.ALL, memberedDef);

        for (IDefinition member : MemberedDefinitionUtils.getAllMembers(memberedDef, project, memberFilter))
        {
            /*
             * class method registered as eventListener gets global when class
             * instance is in an array
             * http://watsonexp.corp.adobe.com/#bug=3040108 We need register
             * only methods with unique signatures. In other words overridden
             * methods must be excluded.
             */
            if (member instanceof IFunctionDefinition && !member.isOverride())
            {
                final INamespaceDefinition ns = member.resolveNamespace(project);
                if (ns != null && ns.isPublicOrInternalNamespace())
                    registerMethod(member.getBaseName(), className);
            }
        }
    }

    // poor man's type inference.
    public Set<IFunctionDefinition> getMethods(ICompilerProject project, String methodName)
    {
        Set<IFunctionDefinition> defs = new HashSet<IFunctionDefinition>();
        m_methodsLock.readLock().lock();
        final Set<String> classes = m_methods.get(methodName);
        if (classes != null)
        {
            for (String className : classes)
            {
                final IDefinition def = getDefinition(className);
                if (def != null && def instanceof IMemberedDefinition)
                {
                    final ASDefinitionFilter memberFilter = new ASDefinitionFilter(
                            ClassificationValue.FUNCTIONS, SearchScopeValue.INHERITED_MEMBERS,
                            AccessValue.ALL, def);
                    final IMemberedDefinition memberedDef = (IMemberedDefinition)def;
                    final IDefinition mdef = MemberedDefinitionUtils.getMemberByName(
                            memberedDef, project, methodName, memberFilter);
                    if (mdef instanceof IFunctionDefinition)
                    {
                        final IFunctionDefinition fdef = (IFunctionDefinition)mdef;
                        defs.add(fdef);
                    }
                }
            }
        }
        m_methodsLock.readLock().unlock();
        return defs;
    }

}
