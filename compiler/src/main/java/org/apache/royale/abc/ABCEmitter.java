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

package org.apache.royale.abc;

import static org.apache.royale.abc.ABCConstants.CONSTANT_Multiname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_MultinameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_MultinameL;
import static org.apache.royale.abc.ABCConstants.CONSTANT_MultinameLA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_Qname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_QnameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQnameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQnameL;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQnameLA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_TypeName;
import static org.apache.royale.abc.ABCConstants.OP_applytype;
import static org.apache.royale.abc.ABCConstants.OP_astype;
import static org.apache.royale.abc.ABCConstants.OP_bkptline;
import static org.apache.royale.abc.ABCConstants.OP_call;
import static org.apache.royale.abc.ABCConstants.OP_callproperty;
import static org.apache.royale.abc.ABCConstants.OP_callproplex;
import static org.apache.royale.abc.ABCConstants.OP_callpropvoid;
import static org.apache.royale.abc.ABCConstants.OP_callstatic;
import static org.apache.royale.abc.ABCConstants.OP_callsuper;
import static org.apache.royale.abc.ABCConstants.OP_callsupervoid;
import static org.apache.royale.abc.ABCConstants.OP_coerce;
import static org.apache.royale.abc.ABCConstants.OP_construct;
import static org.apache.royale.abc.ABCConstants.OP_constructprop;
import static org.apache.royale.abc.ABCConstants.OP_constructsuper;
import static org.apache.royale.abc.ABCConstants.OP_debug;
import static org.apache.royale.abc.ABCConstants.OP_debugfile;
import static org.apache.royale.abc.ABCConstants.OP_debugline;
import static org.apache.royale.abc.ABCConstants.OP_declocal;
import static org.apache.royale.abc.ABCConstants.OP_declocal_i;
import static org.apache.royale.abc.ABCConstants.OP_deleteproperty;
import static org.apache.royale.abc.ABCConstants.OP_dxns;
import static org.apache.royale.abc.ABCConstants.OP_finddef;
import static org.apache.royale.abc.ABCConstants.OP_findproperty;
import static org.apache.royale.abc.ABCConstants.OP_findpropstrict;
import static org.apache.royale.abc.ABCConstants.OP_getdescendants;
import static org.apache.royale.abc.ABCConstants.OP_getglobalslot;
import static org.apache.royale.abc.ABCConstants.OP_getlex;
import static org.apache.royale.abc.ABCConstants.OP_getlocal;
import static org.apache.royale.abc.ABCConstants.OP_getlocal0;
import static org.apache.royale.abc.ABCConstants.OP_getproperty;
import static org.apache.royale.abc.ABCConstants.OP_getscopeobject;
import static org.apache.royale.abc.ABCConstants.OP_getslot;
import static org.apache.royale.abc.ABCConstants.OP_getsuper;
import static org.apache.royale.abc.ABCConstants.OP_hasnext2;
import static org.apache.royale.abc.ABCConstants.OP_inclocal;
import static org.apache.royale.abc.ABCConstants.OP_inclocal_i;
import static org.apache.royale.abc.ABCConstants.OP_initproperty;
import static org.apache.royale.abc.ABCConstants.OP_istype;
import static org.apache.royale.abc.ABCConstants.OP_kill;
import static org.apache.royale.abc.ABCConstants.OP_lookupswitch;
import static org.apache.royale.abc.ABCConstants.OP_newarray;
import static org.apache.royale.abc.ABCConstants.OP_newcatch;
import static org.apache.royale.abc.ABCConstants.OP_newclass;
import static org.apache.royale.abc.ABCConstants.OP_newfunction;
import static org.apache.royale.abc.ABCConstants.OP_newobject;
import static org.apache.royale.abc.ABCConstants.OP_pushbyte;
import static org.apache.royale.abc.ABCConstants.OP_pushdouble;
import static org.apache.royale.abc.ABCConstants.OP_pushint;
import static org.apache.royale.abc.ABCConstants.OP_pushnamespace;
import static org.apache.royale.abc.ABCConstants.OP_pushscope;
import static org.apache.royale.abc.ABCConstants.OP_pushshort;
import static org.apache.royale.abc.ABCConstants.OP_pushstring;
import static org.apache.royale.abc.ABCConstants.OP_pushuint;
import static org.apache.royale.abc.ABCConstants.OP_returnvoid;
import static org.apache.royale.abc.ABCConstants.OP_setglobalslot;
import static org.apache.royale.abc.ABCConstants.OP_setlocal;
import static org.apache.royale.abc.ABCConstants.OP_setproperty;
import static org.apache.royale.abc.ABCConstants.OP_setslot;
import static org.apache.royale.abc.ABCConstants.OP_setsuper;
import static org.apache.royale.abc.ABCConstants.TRAIT_Class;
import static org.apache.royale.abc.ABCConstants.TRAIT_Const;
import static org.apache.royale.abc.ABCConstants.TRAIT_Function;
import static org.apache.royale.abc.ABCConstants.TRAIT_Getter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Method;
import static org.apache.royale.abc.ABCConstants.TRAIT_Setter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Var;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.ExceptionInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.semantics.ScriptInfo;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.semantics.Traits;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;
import org.apache.royale.abc.visitors.IMetadataVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.abc.visitors.NilVisitors;

/**
 * The ABCEmitter is an IABCVisitor that collects information about
 * the ABC and emits it as a byte array.
 */
public final class ABCEmitter implements IABCVisitor
{
    private static final int VERSION_NONE = -1;

    //  Size of a S24 branch offset.
    private static final int SIZEOF_S24 = 3;
    
   /**
    *  Construct a new ABCEmitter, using the default
    *  diagnostics interface which ignores diagnostics.
    */
   public ABCEmitter()
    {
        this(NilVisitors.NIL_DIAGNOSTICS_VISITOR);
    }

    /**
     * Construct a new ABCEmitter using the specified diagnostics interface.
     * @param diagnosticsVisitor - a sink for diagnostics.
     */
    public ABCEmitter(IDiagnosticsVisitor diagnosticsVisitor)
    {
        this.w = new ABCWriter();
        this.lock = new ReentrantLock();
        this.visitEndCalled = false;
        this.diagnosticsVisitor = diagnosticsVisitor;
    }

    private ABCWriter w;

    /**
     * Name pool, has default zero entry.
     */
    final Pool<Name> namePool = new Pool<Name>(Pool.DefaultType.HasDefaultZero);
    
    public Pool<Name> getNamePool()
    {
    	return namePool;
    }
    
    /**
     * String pool, has default zero entry.
     */
    final Pool<String> stringPool = new Pool<String>(Pool.DefaultType.HasDefaultZero);
    
    public Pool<String> getStringPool()
    {
    	return stringPool;
    }
    
    /**
     * int pool, has default zero entry.
     */
    final Pool<Integer> intPool = new Pool<Integer>(Pool.DefaultType.HasDefaultZero);
    
    /**
     * uint pool, has default zero entry.
     */
    final Pool<Long> uintPool = new Pool<Long>(Pool.DefaultType.HasDefaultZero);
    
    /**
     * double pool, has default zero entry.
     */
    final Pool<Double> doublePool = new Pool<Double>(Pool.DefaultType.HasDefaultZero);
    
    /**
     * namespace pool, has default zero entry.
     */
    final Pool<Namespace> nsPool = new Pool<Namespace>(Pool.DefaultType.HasDefaultZero);
    
    public Pool<Namespace> getNamespacePool()
    {
    	return nsPool;
    }

    /**
     * namespace set pool, has default zero entry.
     */
    final Pool<Nsset> nssetPool = new Pool<Nsset>(Pool.DefaultType.HasDefaultZero);
    
    /**
     * metadata pool, does not have default zero entry.
     */
    final Pool<Metadata> metadataPool = new Pool<Metadata>(Pool.DefaultType.NoDefaultZero);

    /**
     * Visitors of classes defined in this ABC.
     */
    private Collection<EmitterClassVisitor> definedClasses = new ArrayList<EmitterClassVisitor>();
    
    public Collection<EmitterClassVisitor> getDefinedClasses()
    {
    	return definedClasses;
    }
    /**
     * MethodInfos defined in this ABC.  These are stored in a dual-indexed store
     * so the method info number of a MethodInfo can be quickly retrieved.
     */
    private final EntryOrderedStore<MethodInfo> methodInfos = new EntryOrderedStore<MethodInfo>();
    
    public EntryOrderedStore<MethodInfo> getMethodInfos()
    {
    	return methodInfos;
    }
    
    /**
     * Lock used to protect the method info pool stored in the
     * {@link #methodInfos} field.
     */
    private final ReadWriteLock methodInfosLock = new ReentrantReadWriteLock();

    /**
     * Method bodies defined in this ABC.
     */
    private final Vector<MethodBodyInfo> methodBodies = new Vector<MethodBodyInfo>();
    
    /**
     * Scripts defined in this ABC.  There is normally one.
     */
    private final Vector<ScriptInfo> scriptInfos = new Vector<ScriptInfo>();

    private int versionABCMajor = VERSION_NONE;
    
    private int versionABCMinor = VERSION_NONE;

    /**
     * determines whether the ABCEmitter should throw exceptions when it
     * encounters jumps to nowhere, or just try and repair and keep going.
     */
    private boolean allowBadJumps = false;

    /**
     * Should the emitter serialize method bodies to ABC as soon as possible, or
     * serialize them as it emits the final ABC?
     */
    private boolean eagerlyEmitMethodBodies = true;

    /**
     * Lock used to enforce concurrency rules of this class.
     * <p>
     * This lock is used to ensure that vistEnd calls are only made on the same
     * thread that called {@link #visit(int, int)}.
     * <p>
     * When running with asserts enabled this lock is used to cause an assertion
     * failure when the currency rules of this class are violated.
     * <p>
     * When running with asserts disabled this lock is used to cause a deadlock
     * when the currency rules of this class are violated.
     * 
     * @see #assertLockHeld()
     */
    private final ReentrantLock lock;

    /**
     * Flag set in visitEnd(), checked in verifyEmitterStatus()
     */
    private boolean visitEndCalled;

    private final IDiagnosticsVisitor diagnosticsVisitor;

    /**
     * Emit bytecode.
     * @return the ABC bytecode corresponding to the ABC structures
     * visited by this emitter or its subsidiary visitors.
     * @throws Exception if the emitter is unable to generate valid bytecode.
     */
    public byte[] emit() throws Exception
    {
        if (getMajorVersion() == VERSION_NONE || getMinorVersion() == VERSION_NONE)
            throw new IllegalStateException("No abc version specified");

        //  First sort the classes into dependency order.
        this.definedClasses = ClassDependencySort.getSorted(this.definedClasses);

        w.writeU16(getMinorVersion());
        w.writeU16(getMajorVersion());

        w.writeU30(this.intPool.getNominalSize());
        for (int x : this.intPool.getValues())
        {
            w.writeU30(x);
        }

        w.writeU30(this.uintPool.getNominalSize());
        for (long x : this.uintPool.getValues())
        {
            w.writeU30((int)x);
        }

        w.writeU30(this.doublePool.getNominalSize());
        for (double x : this.doublePool.getValues())
        {
            w.write64(Double.doubleToLongBits(x));
        }

        w.writeU30(this.stringPool.getNominalSize());
        for (String s : this.stringPool.getValues())
        {
            byte[] stringBytes = s.getBytes("UTF-8");
            w.writeU30(stringBytes.length);
            w.write(stringBytes);
        }

        w.writeU30(this.nsPool.getNominalSize());
        for (Namespace ns : this.nsPool.getValues())
        {
            emitNamespace(ns);
        }

        w.writeU30(this.nssetPool.getNominalSize());
        for (Nsset nsset : this.nssetPool.getValues())
        {
            w.writeU30(nsset.length());
            for (Namespace ns : nsset)
            {
                w.writeU30(this.nsPool.id(ns));
            }
        }

        w.writeU30(this.namePool.getNominalSize());
        for (Name n : this.namePool.getValues())
        {
            w.write(n.getKind());
            switch (n.getKind())
            {
                case CONSTANT_Qname:
                case CONSTANT_QnameA:
                {
                    w.writeU30(this.nsPool.id(n.getSingleQualifier()));
                    w.writeU30(this.stringPool.id(n.getBaseName()));
                    break;
                }
                case CONSTANT_Multiname:
                case CONSTANT_MultinameA:
                {
                    w.writeU30(this.stringPool.id(n.getBaseName()));
                    w.writeU30(this.nssetPool.id(n.getQualifiers()));
                    break;
                }
                case CONSTANT_RTQname:
                case CONSTANT_RTQnameA:
                {
                    w.writeU30(this.stringPool.id(n.getBaseName()));
                    break;
                }
                case CONSTANT_MultinameL:
                case CONSTANT_MultinameLA:
                {
                    w.writeU30(this.nssetPool.id(n.getQualifiers()));
                    break;
                }
                case CONSTANT_RTQnameL:
                case CONSTANT_RTQnameLA:
                {
                    break;
                }
                case CONSTANT_TypeName:
                {
                    w.writeU30(this.namePool.id(n.getTypeNameBase()));
                    w.writeU30(1); // only 1 type parameter is currently supposed in AVM2
                    w.writeU30(this.namePool.id(n.getTypeNameParameter()));
                    break;
                }
                default:
                {
                    assert (false) : "Unimplemented name kind " + n.getKind();
                    throw new IllegalArgumentException("Not implemented.");
                }
            }
        }

        // See the comment in EmitterMethodInfoVisitor.visit
        // to understand why we have this lock here.
        // The short answer is because multiple threads
        // need to simultaneously add method infos to the method
        // info pool.
        final Lock methodInfosReadLock = methodInfosLock.readLock();
        methodInfosReadLock.lock();
        try
        {
            w.writeU30(this.methodInfos.size());

            for (MethodInfo mi : this.methodInfos)
                emitMethodInfo(mi);
        }
        finally
        {
            methodInfosReadLock.unlock();
        }

        w.writeU30(this.metadataPool.getNominalSize());
        for (Metadata md : this.metadataPool.values)
        {
            // name
            w.writeU30(this.stringPool.id(md.getName()));

            // items count
            assert md.getKeys().length == md.getValues().length;
            w.writeU30(md.getKeys().length);

            // metadata keys
            for (final String key : md.getKeys())
            {
                final int string_index = stringPool.id(key);
                w.writeU30(string_index);
            }

            // metadata values
            for (final String value : md.getValues())
            {
                final int string_index = stringPool.id(value);
                w.writeU30(string_index);
            }
        }

        w.writeU30(this.definedClasses.size());
        for (EmitterClassVisitor clz : this.definedClasses)
        {
            InstanceInfo ii = clz.instanceInfo;

            w.writeU30(namePool.id(ii.name));
            w.writeU30(namePool.id(ii.superName));
            w.write(ii.flags);

            if (ii.hasProtectedNs())
                w.writeU30(this.nsPool.id(ii.protectedNs));
            w.writeU30(ii.interfaceNames.length);

            for (Name i : ii.interfaceNames)
                w.writeU30(this.namePool.id(i));
            w.writeU30(getMethodId(ii.iInit));

            emitTraits(clz.instanceTraits);
        }

        for (EmitterClassVisitor clz : this.definedClasses)
        {
            w.writeU30(getMethodId(clz.classInfo.cInit));
            emitTraits(clz.classTraits);
        }

        w.writeU30(this.scriptInfos.size());
        for (ScriptInfo s : this.scriptInfos)
        {
            emitScriptInfo(s);
        }

        w.writeU30(this.methodBodies.size());
        for (MethodBodyInfo mb : this.methodBodies)
        {
            emitMethodBody(mb);
        }

        return w.getDirectByteArray();
    }

    private int getMajorVersion()
    {
        return versionABCMajor;
    }

    private int getMinorVersion()
    {
        return versionABCMinor;
    }

    /**
     * Emit ABC bytes for a Traits entry.
     * 
     * @param traits - the Traits to emit.
     * @see #poolTraitValues(Traits) which puts these values into the pools.
     */
    private void emitTraits(Traits traits)
    {
        w.writeU30(traits.getTraitCount());

        for (Trait t : traits)
        {
            w.writeU30(namePool.id(t.getNameAttr("name")));
            
            //  Get the kind byte with its flags set in the high nibble.
            w.write(t.getFullKindByte());

            switch (t.getKind())
            {
                case TRAIT_Var:
                case TRAIT_Const:
                {
                    w.writeU30(t.getIntAttr(Trait.TRAIT_SLOT));
                    w.writeU30(namePool.id(t.getNameAttr(Trait.TRAIT_TYPE)));

                    //  Emit the Trait's initial value, if it has one.
                    Object trait_value = t.getAttr(Trait.SLOT_VALUE);
                    if (trait_value != null)
                    {
                        if (trait_value instanceof String)
                        {
                            w.writeU30(stringPool.id((String)trait_value));
                            w.write(ABCConstants.CONSTANT_Utf8);
                        }
                        else if (trait_value instanceof Namespace)
                        {
                            w.writeU30(nsPool.id((Namespace)trait_value));
                            w.write(ABCConstants.CONSTANT_Namespace);
                        }
                        else if (trait_value instanceof Double)
                        {
                            w.writeU30(doublePool.id((Double)trait_value));
                            w.write(ABCConstants.CONSTANT_Double);
                        }
                        else if (trait_value instanceof Integer)
                        {
                            w.writeU30(intPool.id((Integer)trait_value));
                            w.write(ABCConstants.CONSTANT_Int);
                        }
                        else if (trait_value instanceof Long)
                        {
                            w.writeU30(uintPool.id((Long)trait_value));
                            w.write(ABCConstants.CONSTANT_UInt);
                        }
                        else if (trait_value.equals(Boolean.TRUE))
                        {
                            w.writeU30(ABCConstants.CONSTANT_True);
                            w.write(ABCConstants.CONSTANT_True);
                        }
                        else if (trait_value.equals(Boolean.FALSE))
                        {
                            w.writeU30(ABCConstants.CONSTANT_False);
                            w.write(ABCConstants.CONSTANT_False);
                        }
                        else if (trait_value == ABCConstants.NULL_VALUE)
                        {
                            w.writeU30(ABCConstants.CONSTANT_Null);
                            w.write(ABCConstants.CONSTANT_Null);
                        }
                        else if (trait_value == ABCConstants.UNDEFINED_VALUE)
                        {
                            //  Undefined is a special case; it has no kind byte.
                            w.writeU30(0);
                        }
                        else
                        {
                            throw new IllegalStateException("Unrecognized initializer type: " + trait_value.getClass().toString());
                        }
                    }
                    else
                    {
                        w.writeU30(0);
                    }

                    break;
                }
                case TRAIT_Method:
                case TRAIT_Function:
                case TRAIT_Getter:
                case TRAIT_Setter:
                {
                    if (t.hasAttr(Trait.TRAIT_DISP))
                        w.writeU30(t.getIntAttr(Trait.TRAIT_DISP));
                    else
                        w.writeU30(0);

                    w.writeU30(this.getMethodId((MethodInfo)t.getAttr(Trait.TRAIT_METHOD)));
                    break;
                }
                case TRAIT_Class:
                {
                    if (t.hasAttr(Trait.TRAIT_SLOT))
                        w.writeU30(t.getIntAttr(Trait.TRAIT_SLOT));
                    else
                        w.writeU30(0);
                    w.writeU30(this.getClassId(((ClassInfo)t.getAttr(Trait.TRAIT_CLASS))));
                    break;
                }
                default:
                {
                    throw new IllegalArgumentException("Unknown trait kind " + t.getKind());
                }
            }

            if (t.hasMetadata())
            {
                Vector<Metadata> metadata = t.getMetadata();
                w.writeU30(metadata.size());
                for (Metadata m : metadata)
                {
                    w.writeU30(metadataPool.id(m));
                }
            }
        }
    }

    private int getMethodId(MethodInfo info)
    {
        Lock methodInfosReadLock = methodInfosLock.readLock();
        methodInfosReadLock.lock();
        try
        {
            return methodInfos.getId(info);
        }
        finally
        {
            methodInfosReadLock.unlock();
        }
    }

    /**
     * @return the class ID of the given ClassInfo.
     * @throws IllegalArgumentException if the class is not found.
     */
    private int getClassId(ClassInfo info)
    {
        int id_index = 0;
        for (EmitterClassVisitor candidate : this.definedClasses)
        {
            if (candidate.classInfo == info)
                return id_index;
            else
                id_index++;
        }

        throw new IllegalArgumentException("Unable to find ClassInfo index for " + info);
    }

    private void emitScriptInfo(ScriptInfo info)
    {
        final MethodInfo scriptInit = info.getInit();
        final int nRequiredArguments =
            scriptInit.getParamTypes().size() - scriptInit.getDefaultValues().size();
        if (nRequiredArguments > 0)
            diagnosticsVisitor.scriptInitWithRequiredArguments(info, scriptInit);
        w.writeU30(getMethodId(scriptInit));
        emitTraits(info.getTraits());
    }

    private void emitMethodInfo(MethodInfo info)
    {
        final Collection<Name> paramTypes = info.getParamTypes();
        final int nParamTypes = paramTypes.size();
        w.writeU30(nParamTypes);
        w.writeU30(namePool.id(info.getReturnType()));

        for (Name n : paramTypes)
        {
            w.writeU30(namePool.id(n));
        }

        w.writeU30(this.stringPool.id(info.getMethodName()));
        w.write(info.getFlags());

        if (info.hasOptional())
        {
            Collection<PooledValue> defaults = info.getDefaultValues();
            final int nDefaults = defaults.size();

            if (nDefaults > nParamTypes)
                this.diagnosticsVisitor.tooManyDefaultParameters(info);

            w.writeU30(nDefaults);
            for (PooledValue v : defaults)
            {
                w.writeU30(v.getPoolIndex());
                w.write(v.getKind());
            }
        }

        if (info.hasParamNames())
        {
            List<String> paramNames = info.getParamNames();
            final int nParamNames = paramNames.size();
            if (nParamTypes != nParamNames)
                this.diagnosticsVisitor.incorrectNumberOfParameterNames(info);

            for (String param_name : info.getParamNames())
                w.writeU30(stringPool.id(param_name));
        }
    }

    private void emitMethodBody(MethodBodyInfo f) throws Exception
    {
        MethodInfo signature = f.getMethodInfo();

        w.writeU30(getMethodId(signature));
        //  Note: computeFrameCounts() called at MethodBodyInfo.visitEnd().
        w.writeU30(f.getMaxStack());

        int max_local = f.getLocalCount();
        if (signature.getParamCount() > max_local)
            max_local = signature.getParamCount();

        w.writeU30(max_local);

        w.writeU30(f.getInitScopeDepth());
        w.writeU30(f.getMaxScopeDepth());

        if (!f.hasBytecode())
            emitCode(f);

        w.write(f.getBytecode());

        emitTraits(f.getTraits());
    }

    private void emitCode(MethodBodyInfo f) throws Exception
    {
        ABCWriter result = new ABCWriter();
        Map<IBasicBlock, ABCWriter> writers = new HashMap<IBasicBlock, ABCWriter>();

        // generate linear code sequences within the basic blocks,
        // and compute the offset of each block from the method start.
        Map<IBasicBlock, Integer> block_offsets = new HashMap<IBasicBlock, Integer>();
        int code_len = 0;

        //  First, generate code for each block.
        //  Keep a running tally of the code length,
        //  which corresponds to the starting position
        //  of each block.
        for (IBasicBlock b : f.getCfg().getBlocksInEntryOrder())
        {
            block_offsets.put(b, code_len);
            ABCWriter blockWriter = new ABCWriter();
            writers.put(b, blockWriter);

            emitBlock(b, blockWriter);

            code_len += blockWriter.size();

            //  Blocks with no instructions are
            //  valid assembly constructs.
            if (b.size() == 0)
                continue;

            //  If the last instruction in the block
            //  is a jump, leave room for the instruction,
            //  but don't emit it yet.
            Instruction last = b.get(b.size() - 1);

            if (last.isBranch())
            {
                if (last.getOpcode() == OP_lookupswitch)
                {
                    //  Switch table contains a U30 case count and S24 offsets.
                    int switch_size = 1 + sizeOfU30(last.getOperandCount()) + SIZEOF_S24 * last.getOperandCount();
                    code_len += switch_size;
                }
                else
                {
                    assert (null != last.getTarget());
                    //  Reserve space for a branch instruction.
                    code_len += 4;
                }
            }
        }

        //  Note: Can't compute code_start until we've seen
        //  how big this U30 is.
        result.writeU30(code_len);
        int code_start = result.size();

        //  Now we can resolve labels to their code offsets.
        //  Copy the linear code sequences into the main ABCWriter,
        //  and emit the branch and lookupswitch instructions.
        for (IBasicBlock b : f.getCfg().getBlocksInEntryOrder())
        {
            writers.get(b).writeTo(result);
            if (b.size() > 0)
            {
                Instruction last = b.get(b.size() - 1);

                if (last.isBranch())
                {
                    if (OP_lookupswitch == last.getOpcode())
                    {
                        emitLookupswitch(result, code_start, f, last, block_offsets);
                    }
                    else
                    {
                        assert (last.getTarget() != null);

                        emitBranch(result, last.getOpcode(), f.getBlock(last.getTarget(), !allowBadJumps), code_start, block_offsets, code_len);
                    }
                }
            }
        }

        emitExceptionInfo(f, result, block_offsets);

        f.setBytecode(result.getDirectByteArray());
    }

    private void emitBranch(ABCWriter writer, int opcode, IBasicBlock target, int code_start, Map<IBasicBlock, Integer> block_offsets, int code_len)
    {
        writer.write(opcode);
        
        //  Branch offset computed from the instruction following the branch.
        int from = writer.size() + SIZEOF_S24;
        
        //  Convert the target offset relative to the start of the ABC, as
        //  the "from" address is expressed in this fashion.
        //  if we can't determine the target then jump past the end of the method - this is to allow
        //  malformed ABCs to be successfully processed by AET.
        int to = code_start + (target != null ? block_offsets.get(target) : code_len + 1);
        
        writer.writeS24(to - from);
    }

    void emitBlock(IBasicBlock b, ABCWriter blockWriter)
    {
        for (int i = 0; i < b.size() && !b.get(i).isBranch(); i++)
        {
            Instruction insn = b.get(i);

            blockWriter.write(insn.getOpcode());
            switch (insn.getOpcode())
            {
                case OP_hasnext2:
                {
                    blockWriter.writeU30((Integer)insn.getOperand(0));
                    blockWriter.writeU30((Integer)insn.getOperand(1));
                    break;
                }
                case OP_findproperty:
                case OP_findpropstrict:
                case OP_getlex:
                case OP_getsuper:
                case OP_setsuper:
                case OP_deleteproperty:
                case OP_getdescendants:
                case OP_getproperty:
                case OP_setproperty:
                case OP_initproperty:
                case OP_istype:
                case OP_coerce:
                case OP_astype:
                case OP_finddef:
                {
                    blockWriter.writeU30(namePool.id((Name)insn.getOperand(0)));
                    break;
                }
                case OP_callproperty:
                case OP_callproplex:
                case OP_callpropvoid:
                case OP_callsuper:
                case OP_callsupervoid:
                case OP_constructprop:
                {
                    blockWriter.writeU30(namePool.id((Name)insn.getOperand(0)));
                    blockWriter.writeU30((Integer)insn.getOperand(1));
                    break;
                }
                case OP_constructsuper:
                case OP_call:
                case OP_construct:
                case OP_newarray:
                case OP_newobject:
                case OP_getlocal:
                case OP_setlocal:
                case OP_getslot:
                case OP_setslot:
                case OP_kill:
                case OP_inclocal:
                case OP_declocal:
                case OP_inclocal_i:
                case OP_declocal_i:
                case OP_newcatch:
                case OP_getglobalslot:
                case OP_setglobalslot:
                case OP_applytype:
                case OP_getscopeobject:
                case OP_pushshort:
                {
                    blockWriter.writeU30(insn.getImmediate());
                    break;
                }
                case OP_pushbyte:
                {
                    blockWriter.write(insn.getImmediate());
                    break;
                }
                case OP_newclass:
                {
                    blockWriter.writeU30(getClassId((ClassInfo)insn.getOperand(0)));
                    break;
                }
                case OP_newfunction:
                {
                    blockWriter.writeU30(getMethodId((MethodInfo)insn.getOperand(0)));
                    break;
                }
                case OP_callstatic:
                {
                    blockWriter.writeU30(getMethodId((MethodInfo)insn.getOperand(0)));
                    blockWriter.writeU30((Integer)(insn.getOperand(1)));
                    break;
                }
                case OP_pushstring:
                case OP_dxns:
                case OP_debugfile:
                {
                    blockWriter.writeU30(stringPool.id(insn.getOperand(0).toString()));
                    break;
                }
                case OP_pushnamespace:
                {
                    blockWriter.writeU30(nsPool.id((Namespace)insn.getOperand(0)));
                    break;
                }
                case OP_pushint:
                {
                    blockWriter.writeU30(intPool.id((Integer)insn.getOperand(0)));
                    break;
                }
                case OP_pushuint:
                {
                    blockWriter.writeU30(uintPool.id((Long)insn.getOperand(0)));
                    break;
                }
                case OP_pushdouble:
                {
                    blockWriter.writeU30(doublePool.id((Double)insn.getOperand(0)));
                    break;
                }
                case OP_debugline:
                case OP_bkptline:
                {
                    blockWriter.writeU30(insn.getImmediate());
                    break;
                }
                case OP_debug:
                {
                    blockWriter.write((Integer)(insn.getOperand(0)));
                    blockWriter.writeU30(stringPool.id(insn.getOperand(1).toString()));
                    blockWriter.write((Integer)insn.getOperand(2));
                    blockWriter.writeU30(0);
                    break;
                }
            }
        }
    }

    private void emitNamespace(Namespace ns)
    {
        w.write(ns.getKind());
        w.writeU30(stringPool.id(ns.getVersionedName()));
    }

    private void emitExceptionInfo(MethodBodyInfo f, ABCWriter w, Map<IBasicBlock, Integer> pos)
    {
        w.writeU30(f.getExceptions().size());

        for (ExceptionInfo ex : f.getExceptions())
        {
            if ( ex.isLive() )
            {
                w.writeU30(pos.get(f.getBlock(ex.getFrom())));
                w.writeU30(pos.get(f.getBlock(ex.getTo())));
                w.writeU30(pos.get(f.getBlock(ex.getTarget())));
                w.writeU30(namePool.id(ex.getExceptionType()));
                w.writeU30(namePool.id(ex.getCatchVar()));
            }
        }
    }

    void emitLookupswitch(ABCWriter out, int code_start, MethodBodyInfo f,
                          Instruction switch_insn, Map<IBasicBlock, Integer> block_offsets)
    {
        int case_size = switch_insn.getOperandCount() - 1; //  Last Label reserved for the default

        //  "The base location is the address of the lookupswitch instruction itself." - AVM2 
        int base_loc = out.size() - code_start;

        out.write(OP_lookupswitch);

        //  The last label is the default case.
        Label default_case = (Label)switch_insn.getOperand(case_size);
        int default_offset = (block_offsets.get(f.getBlock(default_case)) - base_loc);
        out.writeS24(default_offset);

        out.writeU30(case_size - 1);

        for (int i = 0; i < case_size; i++)
        {
            int branch_offset = (block_offsets.get(f.getBlock((Label)switch_insn.getOperand(i))) - base_loc);
            out.writeS24(branch_offset);
        }
    }

    static class ABCWriter extends ByteArrayOutputStream
    {
        void rewind(int n)
        {
            super.count -= n;
        }

        void writeU16(int i)
        {
            write(i);
            write(i >> 8);
        }

        void writeS24(int i)
        {
            writeU16(i);
            write(i >> 16);
        }

        void write64(long i)
        {
            writeS24((int)i);
            writeS24((int)(i >> 24));
            writeU16((int)(i >> 48));
        }

        void writeU30(int v)
        {
            if (v < 128 && v >= 0)
            {
                write(v);
            }
            else if (v < 16384 && v >= 0)
            {
                write(v & 0x7F | 0x80);
                write(v >> 7);
            }
            else if (v < 2097152 && v >= 0)
            {
                write(v & 0x7F | 0x80);
                write(v >> 7 | 0x80);
                write(v >> 14);
            }
            else if (v < 268435456 && v >= 0)
            {
                write(v & 0x7F | 0x80);
                write(v >> 7 | 0x80);
                write(v >> 14 | 0x80);
                write(v >> 21);
            }
            else
            {
                write(v & 0x7F | 0x80);
                write(v >> 7 | 0x80);
                write(v >> 14 | 0x80);
                write(v >> 21 | 0x80);
                write(v >>> 28);
            }
        }

        int sizeOfU30(int v)
        {
            if (v < 128 && v >= 0)
                return 1;

            else if (v < 16384 && v >= 0)
                return 2;

            else if (v < 2097152 && v >= 0)
                 return 3;

            else if (v < 268435456 && v >= 0)
                return 4;

            else
                return 5;
        }


        /**
         * Get the byte array contained within this stream.  Note: flush may need to be called
         * @return the byte array
         */
        public byte[] getDirectByteArray()
        {
            // only return the buffer directly if the length of the buffer
            // is the same as the count, as the buffer can be bigger than the
            // count, and in this case, we return an array which is larger than
            // expected, so we need to truncate it with a copy.
            if (buf.length == count)
                return buf;
            else
                return super.toByteArray();
        }

        /**
         * Ensure toByteArray() is never called.
         */
        @Override
        public byte[] toByteArray()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Find all the operands in a method body and make sure they find their way
     * into the appropriate pool.
     * 
     * @param mi - the method body.
     * @post any runtime multinames have a dummy Name operand.
     */
    void poolOperands(MethodBodyInfo mbi)
    {
        for (IBasicBlock b : mbi.getCfg().getBlocksInEntryOrder())
        {
            for (Instruction insn : b.getInstructions())
            {
                switch (insn.getOpcode())
                {
                    case OP_findproperty:
                    case OP_findpropstrict:
                    case OP_getlex:
                    case OP_getdescendants:
                    case OP_initproperty:
                    case OP_istype:
                    case OP_coerce:
                    case OP_astype:
                    case OP_finddef:
                    case OP_deleteproperty:
                    case OP_getproperty:
                    case OP_setproperty:
                    case OP_getsuper:
                    case OP_setsuper:
                    {
                        visitPooledName((Name)insn.getOperand(0));
                        break;
                    }
                    case OP_callproperty:
                    case OP_callproplex:
                    case OP_callpropvoid:
                    case OP_callsuper:
                    case OP_callsupervoid:
                    case OP_constructprop:
                    {
                        visitPooledName((Name)insn.getOperand(0));
                        break;
                    }
                    case OP_pushstring:
                    case OP_dxns:
                    case OP_debugfile:
                    {
                        stringPool.add(insn.getOperand(0).toString());
                        break;
                    }
                    case OP_pushnamespace:
                    {
                        visitPooledNamespace((Namespace)insn.getOperand(0));
                        break;
                    }
                    case OP_pushint:
                    {
                        intPool.add((Integer)insn.getOperand(0));
                        break;
                    }
                    case OP_pushuint:
                    {
                        uintPool.add((Long)insn.getOperand(0));
                        break;
                    }
                    case OP_pushdouble:
                    {
                        doublePool.add((Double)insn.getOperand(0));
                        break;
                    }
                    case OP_debug:
                    {
                        stringPool.add(insn.getOperand(1).toString());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Pool all the objects referenced by a Traits that need to be added to the
     * various constant pools. These objects include:
     * <ul>
     * <li>The {@link Name} object for the qualified name of each {@link Trait}.
     * </li>
     * <li>The {@link Name} object that refers to the class or interface of each
     * {@link Trait} with a type annotation.</li>
     * <li>The {@link Metadata} objects that decorate each {@link Trait}.</li>
     * <li>The {@link String}, {@link Namespace}, {@link Double},
     * {@link Integer}, or {@link Long} objects that specify the
     * initial value of each {@link Trait}.</li>
     * </ul>
     */
    private void poolTraitsConstants(Traits ts)
    {
        for (Trait t : ts)
        {
            Name traitName = t.getNameAttr(Trait.TRAIT_NAME);
            visitPooledName(traitName);

            if (t.hasAttr(Trait.TRAIT_TYPE))
                visitPooledName(t.getNameAttr(Trait.TRAIT_TYPE));

            for (Metadata md : t.getMetadata())
                visitPooledMetadata(md);

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

    @Override
    public void visit(int majorVersion, int minorVersion)
    {
        verifyEmitterStatus();

        this.lock.lock();
        
        assert this.lock.getHoldCount() == 1 : "The hold count should be 1, beacuse this method should only be called once!";
        
        if (versionABCMajor == VERSION_NONE)
        {
            versionABCMajor = majorVersion;
            versionABCMinor = minorVersion;
        }
        else if (versionABCMajor != majorVersion || versionABCMinor != minorVersion)
        {
            throw new IllegalArgumentException("abc versions do not match");
        }
    }

    @Override
    public void visitEnd()
    {
        verifyEmitterStatus();
        assertLockHeld();

        this.visitEndCalled = true;
    }

    @Override
    public IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo)
    {
        verifyEmitterStatus();

        EmitterClassVisitor result = new EmitterClassVisitor(iinfo, cinfo);
        result.visit();
        return result;
    }

    @Override
    public IScriptVisitor visitScript()
    {
        verifyEmitterStatus();

        return new EmitterScriptInfo();
    }

    @Override
    public IMethodVisitor visitMethod(MethodInfo minfo)
    {
        verifyEmitterStatus();

        return new EmitterMethodInfoVisitor(minfo);
    }

    @Override
    public void visitPooledDouble(Double d)
    {
        verifyEmitterStatus();

        this.doublePool.add(d);
    }

    @Override
    public void visitPooledInt(Integer i)
    {
        verifyEmitterStatus();

        this.intPool.add(i);
    }

    @Override
    public void visitPooledMetadata(Metadata md)
    {
        verifyEmitterStatus();

        this.metadataPool.add(md);

        visitPooledString(md.getName());

        for (String key : md.getKeys())
            visitPooledString(key);

        for (String value : md.getValues())
            visitPooledString(value);
    }

    @Override
    public void visitPooledName(Name n)
    {
        verifyEmitterStatus();

        if (null == n)
            return;

        final int kind = n.getKind();

        if (kind != ABCConstants.CONSTANT_TypeName)
        {
            this.namePool.add(n);

            visitPooledString(n.getBaseName());
            if ((kind == ABCConstants.CONSTANT_Qname) || (kind == ABCConstants.CONSTANT_QnameA))
                visitPooledNamespace(n.getSingleQualifier());
            else
                visitPooledNsSet(n.getQualifiers());
        }
        else
        {
            visitPooledName(n.getTypeNameBase());
            visitPooledName(n.getTypeNameParameter());
            this.namePool.add(n);
        }
    }

    @Override
    public void visitPooledNamespace(Namespace ns)
    {
        verifyEmitterStatus();

        this.nsPool.add(ns);

        if (ns != null)
            visitPooledString(ns.getVersionedName());
    }

    @Override
    public void visitPooledNsSet(Nsset nss)
    {
        verifyEmitterStatus();

        this.nssetPool.add(nss);

        if (nss != null)
        {
            for (Namespace ns : nss)
            {
                visitPooledNamespace(ns);
            }
        }
    }

    @Override
    public void visitPooledString(String s)
    {
        verifyEmitterStatus();

        this.stringPool.add(s);
    }

    @Override
    public void visitPooledUInt(Long l)
    {
        verifyEmitterStatus();

        this.uintPool.add(l);
    }

    public static int sizeOfU30(int v)
    {
        if (v < 128 && v >= 0)
            return 1;

        else if (v < 16384 && v >= 0)
            return 2;

        else if (v < 2097152 && v >= 0)
            return 3;

        else if (v < 268435456 && v >= 0)
            return 4;

        else
            return 5;

    }

    public class EmitterClassVisitor implements IClassVisitor, ClassDependencySort.IInstanceInfoProvider
    {
        EmitterClassVisitor(InstanceInfo iinfo, ClassInfo cinfo)
        {
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

        ClassInfo classInfo;
        public Traits classTraits;
        InstanceInfo instanceInfo;
        public Traits instanceTraits;

        @Override
        public void visit()
        {
            verifyEmitterStatus();
        }

        @Override
        public ITraitsVisitor visitClassTraits()
        {
            verifyEmitterStatus();
            return new EmitterTraitsVisitor(this.classTraits);
        }

        @Override
        public ITraitsVisitor visitInstanceTraits()
        {
            verifyEmitterStatus();
            return new EmitterTraitsVisitor(this.instanceTraits);
        }

        @Override
        public void visitEnd()
        {
            verifyEmitterStatus();

            assertLockHeld();
            definedClasses.add(this);
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
                    mbv.visitInstruction(OP_getlocal0);
                    mbv.visitInstruction(OP_pushscope);
                    mbv.visitInstruction(OP_getlocal0);
                    mbv.visitInstruction(ABCConstants.OP_constructsuper, 0);
                    mbv.visitInstruction(OP_returnvoid);
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
        }

        @Override
        public InstanceInfo getInstanceInfo()
        {
            return this.instanceInfo;
        }
    }

    private class EmitterTraitsVisitor implements ITraitsVisitor
    {
        EmitterTraitsVisitor(Traits traits)
        {
            this.traits = traits;
        }

        Traits traits;

        @Override
        public ITraitVisitor visitClassTrait(int kind, Name name, int slot_id, ClassInfo clazz)
        {
            verifyEmitterStatus();

            Trait t = createTrait(kind, name);
            if (slot_id != 0)
                t.addAttr(Trait.TRAIT_SLOT, slot_id);
            t.addAttr(Trait.TRAIT_CLASS, clazz);

            return new EmitterTraitVisitor(t);
        }

        @Override
        public ITraitVisitor visitMethodTrait(int kind, Name name, int dispId, MethodInfo method)
        {
            verifyEmitterStatus();

            Trait t = createTrait(kind, name);
            t.addAttr(Trait.TRAIT_METHOD, method);
            if (dispId != 0)
                t.addAttr(Trait.TRAIT_DISP, dispId);
            return new EmitterTraitVisitor(t);
        }

        @Override
        public ITraitVisitor visitSlotTrait(int kind, Name name, int slotId, Name slotType, Object slotValue)
        {
            verifyEmitterStatus();

            Trait t = createTrait(kind, name);
            t.addAttr(Trait.TRAIT_SLOT, slotId);
            t.addAttr(Trait.TRAIT_TYPE, slotType);
            t.addAttr(Trait.SLOT_VALUE, slotValue);
            if (slotType != null)
                visitPooledName(slotType);
            return new EmitterTraitVisitor(t);
        }

        @Override
        public void visit()
        {
            verifyEmitterStatus();
        }

        @Override
        public void visitEnd()
        {
            verifyEmitterStatus();

            assertLockHeld();
            poolTraitsConstants(traits);
        }

        @Override
        public Traits getTraits()
        {
            return this.traits;
        }

        private Trait createTrait(int kind, Name name)
        {
            verifyEmitterStatus();

            Trait t = new Trait(kind, name);
            traits.add(t);
            return t;
        }
    }

    private class EmitterTraitVisitor implements ITraitVisitor
    {
        EmitterTraitVisitor(Trait t)
        {
            this.t = t;
        }

        Trait t;

        @Override
        public IMetadataVisitor visitMetadata(int count)
        {
            verifyEmitterStatus();

            return new IMetadataVisitor()
            {

                @Override
                public void visit(Metadata md)
                {
                    verifyEmitterStatus();

                    t.addMetadata(md);
                }
            };
        }

        @Override
        public void visitAttribute(String attr_name, Object attr_value)
        {
            verifyEmitterStatus();

            this.t.addAttr(attr_name, attr_value);
        }

        @Override
        public void visitStart()
        {
            verifyEmitterStatus();

        }

        @Override
        public void visitEnd()
        {
            verifyEmitterStatus();

            // We do not need to assert that the lock is held, because this method is empty,
            // but if this method ever started doing anything we would not want clients to
            // be broken.
            assertLockHeld();
        }
    }

    private class EmitterMethodBodyInfo implements IMethodBodyVisitor
    {
        EmitterMethodBodyInfo(MethodBodyInfo mbinfo)
        {
            this.mbi = mbinfo;
        }

        MethodBodyInfo mbi;

        @Override
        public void visit()
        {
            verifyEmitterStatus();

        }

        @Override
        public void visitEnd()
        {
            verifyEmitterStatus();

            assertLockHeld();
            poolOperands(mbi);
            methodBodies.add(mbi);

            for (ExceptionInfo exceptionInfo : mbi.getExceptions())
            {
                visitPooledName(exceptionInfo.getExceptionType());
                visitPooledName(exceptionInfo.getCatchVar());
            }
        }

        @Override
        public void visitInstruction(int opcode)
        {
            verifyEmitterStatus();

            this.mbi.insn(opcode);
        }

        @Override
        public void visitInstruction(int opcode, int immediate_operand)
        {
            verifyEmitterStatus();

            this.mbi.insn(opcode, immediate_operand);
        }

        @Override
        public void visitInstruction(int opcode, Object single_operand)
        {
            verifyEmitterStatus();

            this.mbi.insn(opcode, single_operand);
        }

        @Override
        public void visitInstruction(int opcode, Object[] operands)
        {
            verifyEmitterStatus();

            this.mbi.insn(opcode, operands);
        }

        @Override
        public void visitInstruction(Instruction insn)
        {
            verifyEmitterStatus();

            this.mbi.insn(insn);
        }

        @Override
        public ITraitsVisitor visitTraits()
        {
            verifyEmitterStatus();

            return new EmitterTraitsVisitor(this.mbi.getTraits());
        }

        @Override
        public int visitException(Label from, Label to, Label target, Name ex_type, Name ex_var)
        {
            verifyEmitterStatus();

            return mbi.addExceptionInfo(new ExceptionInfo(from, to, target, ex_type, ex_var));
        }

        @Override
        public void visitInstructionList(InstructionList new_list)
        {
            verifyEmitterStatus();

            mbi.setInstructionList(new_list);
        }

        @Override
        public void labelCurrent(Label l)
        {
            mbi.labelCurrent(l);
        }

        @Override
        public void labelNext(Label l)
        {
            mbi.labelNext(l);
        }
    }

    private class EmitterScriptInfo implements IScriptVisitor
    {
        EmitterScriptInfo()
        {
            this.si = new ScriptInfo();
        }

        final ScriptInfo si;

        @Override
        public void visit()
        {
            verifyEmitterStatus();
        }

        @Override
        public void visitEnd()
        {
            verifyEmitterStatus();
            assertLockHeld();
            scriptInfos.add(this.si);
        }

        @Override
        public void visitInit(MethodInfo init_method)
        {
            verifyEmitterStatus();

            si.setInit(init_method);
        }

        @Override
        public ITraitsVisitor visitTraits()
        {
            verifyEmitterStatus();

            return new EmitterTraitsVisitor(si.getTraits());
        }
    }

    private class EmitterMethodInfoVisitor implements IMethodVisitor
    {
        EmitterMethodInfoVisitor(MethodInfo mi)
        {
            assert (mi != null);
            this.mi = mi;
        }

        final MethodInfo mi;
        
        MethodBodyInfo mbi;

        @Override
        public void visit()
        {
            verifyEmitterStatus();

            // We need to grab a lock before adding the method
            // info to the method pool because this method ( the visit method )
            // can be called from any number of threads at the same time for the
            // same emitter.  This is the only place where we mutate emitter
            // state from a method other than visitEnd.
            //
            // We need to add the method info to the method pool here
            // so that the EmitterMethodBodyInfo.visitEnd call for other
            // methods can compute a method id for the method info
            // associated with this EmitterMethodInfoVisitor before
            // visitEnd has been called on this EmitterMethodInfoVisitor.
            final Lock methodInfosWriteLock = methodInfosLock.writeLock();
            methodInfosWriteLock.lock();
            try
            {
                methodInfos.add(mi);
            }
            finally
            {
                methodInfosWriteLock.unlock();
            }
        }

        @Override
        public IMethodBodyVisitor visitBody(MethodBodyInfo mbi)
        {
            verifyEmitterStatus();

            this.mbi = mbi;
            return new EmitterMethodBodyInfo(mbi);
        }

        @Override
        public void visitEnd()
        {
            verifyEmitterStatus();

            assertLockHeld();

            for (Name param_type_name : mi.getParamTypes())
                visitPooledName(param_type_name);

            if ((mbi != null) && (mi.isNative()))
                diagnosticsVisitor.nativeMethodWithMethodBody(mi, mbi);

            visitPooledString(mi.getMethodName());
            visitPooledName(mi.getReturnType());

            for (Name ptype : mi.getParamTypes())
            {
                visitPooledName(ptype);
            }

            if (mi.hasOptional())
            {
                for (PooledValue v : mi.getDefaultValues())
                {
                    v.setPoolIndex(visitPooledValue(v));
                }
            }

            if (mi.hasParamNames())
            {
                for (String param_name : mi.getParamNames())
                {
                    visitPooledString(param_name);
                }
            }

            //  Save the method body as compressed ABC bytecode,
            //  if so indicated.  EmitterMethodBodyInfo.visitEnd()
            //  does not do this because the method body needs the
            //  type names and default values pooled in this method.
            if (this.mbi != null)
            {
                this.mbi.computeFrameCounts(ABCEmitter.this.diagnosticsVisitor);

                //  Don't eagerly emit (to ABC blobs) method bodies that
                //  contain newclass instructions.  The class declarations
                //  are the one ABC header structure that may be sorted even
                //  without optimization (to reorder the declarations in
                //  dependency order).  If that happens then the indices
                //  in the newclass instructions would be invalid.
                if (eagerlyEmitMethodBodies && !this.mbi.hasNewclassInstruction())
                {
                    try
                    {
                        emitCode(this.mbi);
                    }
                    catch (RuntimeException uncheckedSNAFU)
                    {
                        throw uncheckedSNAFU;
                    }
                    catch (Exception checkedSNAFU)
                    {
                        //  TODO: The AET needs an error/warnings interface.
                        //  For now, "report" by rethrowing wrapped in an
                        //  unchecked exception.
                        throw new IllegalStateException(checkedSNAFU);
                    }
                }
            }
        }
    }

    /**
     * Record a PooledValue's value in the appropriate constant pool.
     * 
     * @return the pool index.
     * @see #PooledValue.setPoolIndex() which consumes the index.
     */
    private int visitPooledValue(PooledValue value)
    {
        switch (value.getKind())
        {
            case ABCConstants.CONSTANT_Int:
            {
                visitPooledInt(value.getIntegerValue());
                return this.intPool.id(value.getIntegerValue());
            }
            case ABCConstants.CONSTANT_UInt:
            {
                visitPooledUInt(value.getLongValue());
                return this.uintPool.id(value.getLongValue());
            }
            case ABCConstants.CONSTANT_Double:
            {
                visitPooledDouble(value.getDoubleValue());
                return this.doublePool.id(value.getDoubleValue());
            }
            case ABCConstants.CONSTANT_Utf8:
            {
                visitPooledString(value.getStringValue());
                return this.stringPool.id(value.getStringValue());
            }
            // The kind and index for a manifest CONSTANT are identical.
            case ABCConstants.CONSTANT_True:
            {
                return ABCConstants.CONSTANT_True;
            }
            case ABCConstants.CONSTANT_False:
            {
                return ABCConstants.CONSTANT_False;
            }
            case ABCConstants.CONSTANT_Undefined:
            {
                return ABCConstants.CONSTANT_Undefined;
            }
            case ABCConstants.CONSTANT_Null:
            {
                return ABCConstants.CONSTANT_Null;
            }
            //  All these variants of namespace are stored in the same pool.
            case ABCConstants.CONSTANT_Namespace:
            case ABCConstants.CONSTANT_PackageNs:
            case ABCConstants.CONSTANT_PackageInternalNs:
            case ABCConstants.CONSTANT_ProtectedNs:
            case ABCConstants.CONSTANT_ExplicitNamespace:
            case ABCConstants.CONSTANT_StaticProtectedNs:
            case ABCConstants.CONSTANT_PrivateNs:
            {
                visitPooledNamespace(value.getNamespaceValue());
                return this.nsPool.id(value.getNamespaceValue());
            }
            default:
            {
                throw new IllegalStateException("Unrecognized initializer type: " + value.getKind());
            }
        }
    }

    /**
     * Allow invalid jump instructions for legacy ABCs.
     * @param b - true if the caller wishes to allow bad jumps.
     */
    public void setAllowBadJumps(boolean b)
    {
        this.allowBadJumps = b;
    }

    /**
     * Lock used to enforce concurrency rules of this class.
     * <p>
     * This method is used to ensure that vistEnd calls are only made on the
     * same thread that called {@link #visit(int, int)}.
     * <p>
     * When running with asserts enabled this method will cause an assertion
     * failure when the currency rules of this class are violated.
     * <p>
     * When running with asserts disabled this method will cause a deadlock when
     * the currency rules of this class are violated.
     * 
     * @see #lock()
     */
    private void assertLockHeld()
    {
        assert this.lock.isHeldByCurrentThread() : "A visitEnd method was called from a thread other than the thread that called IABCVisitor.visit!";
        // lock immediately followed by unlock should be a NOP, unless
        // some other thread holds the lock ( which the assert above guards against when
        // running with asserts enabled ).
        lock.lock();
        lock.unlock();
    }

    /**
     * verifyEmitterStatus() verifies that the emitter has not seen a visitEnd()
     * call.
     */
    private void verifyEmitterStatus()
    {
        if (this.visitEndCalled)
            throw new IllegalStateException("An ABCEmitter can only emit once visitEnd has been called.");
    }
}
