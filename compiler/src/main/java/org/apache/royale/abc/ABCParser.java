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

import java.io.*;
import java.util.*;

import org.apache.royale.abc.semantics.*;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IMetadataVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.abc.visitors.NilVisitors;

import static org.apache.royale.abc.ABCConstants.*;

/**
 * ABCParser reads an ABC from a byte array or an input stream
 * and translates it into a sequence of AET operations.
 */
public class ABCParser
{
	public boolean verbose = false;
	public PrintWriter output;
	
    /**
     * The ABC as a byte array.
     */
    byte[] abc;

    /*
     * Local copies of the pool data are maintained because the core's pools may
     * contain data from several ABCs, and so the constant pool indices
     * contained in this ABC won't necessarily match the indices in the core
     * pools.
     */

    /**
     * Names defined by this ABC.
     */
    private Name[] names;
    
    /**
     * Strings defined by this ABC.
     */
    private String[] strings;
    
    /**
     * Namespaces defined by this ABC.
     */
    private Namespace[] namespaces;
    
    /**
     * Namespace sets defined by this ABC.
     */
    private Nsset[] namespace_sets;
    
    /** 
     * Integer values defined by this ABC.
     */
    private int[] ints;
    
    /**
     * Unsigned integer values defined by this ABC.
     */
    private long[] uints;
    
    /**
     * Floating-point values defined by this ABC.
     */
    private double[] doubles;

    /**
     * Metdata defined by this ABC.
     */
    private Metadata[] metadata;

    /**
     * MethodInfos by id
     */
    private MethodInfo[] methodInfos;
    
    /**
     * And associated visitors.
     */
    private IMethodVisitor[] methodVisitors;

    /**
     * ClassInfos by id
     */
    private ClassInfo[] classInfos;

    /**
     * InstanceInfos by id
     */
    private InstanceInfo[] instanceInfos;

    /**
     * Construct a new ABC parser from a byte array.
     * 
     * @param abc - the ABC in byte array form.
     */
    public ABCParser(byte[] abc)
    {
        this.abc = abc;
    }

    /**
     *  Construct a new ABC parser from an input stream.
     *  @param input - the InputStream.  Clients may want
     *    to buffer their input for best performance.
     */
    public ABCParser(InputStream input)
    throws IOException
    {
        ByteArrayOutputStream bufferedABC = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int n = input.read(buffer);
        while (n != -1)
        {
            bufferedABC.write(buffer, 0, n);
            n = input.read(buffer);
        }

        this.abc = bufferedABC.toByteArray();
    }

    /**
     * Parse the ABC and send its information to the visitor.
     * 
     * @param vabc - the top-level visitor.
     */
    public void parseABC(IABCVisitor vabc)
    {
        int pool_size;

        ABCReader p = new ABCReader(0, abc);
        int minor = p.readU16();
        int major = p.readU16();

        vabc.visit(major, minor);

        if (verbose)
        	output.println("parsing int pool");
        	
        pool_size = p.readU30();
        ints = new int[pool_size];
        for (int i = 1; i < pool_size; i++)
        {
            ints[i] = p.readU30();
            vabc.visitPooledInt(ints[i]);
        }

        if (verbose)
        	output.println("parsing uint pool");
        
        pool_size = p.readU30();
        uints = new long[pool_size];
        for (int i = 1; i < pool_size; i++)
        {
            uints[i] = 0xffffffffL & p.readU30();
            vabc.visitPooledUInt(uints[i]);
        }

        if (verbose)
        	output.println("parsing float pool");
        
        pool_size = p.readU30();
        doubles = new double[pool_size];
        for (int i = 1; i < pool_size; i++)
        {
            doubles[i] = p.readDouble();
            vabc.visitPooledDouble(doubles[i]);
        }

        if (verbose)
        	output.println("parsing string pool");

        pool_size = p.readU30();
        strings = new String[pool_size];
        for (int i = 1; i < pool_size; i++)
        {
            int len = p.readU30();
            try
            {
                strings[i] = new String(abc, p.pos, len, "UTF-8");
            }
            catch (UnsupportedEncodingException badEncoding)
            {
                //  A well-formed ABC can't throw this exception.
                throw new IllegalStateException(badEncoding);
            }
            vabc.visitPooledString(strings[i]);
            p.pos += len;
        }

        if (verbose)
        	output.println("parsing namespace pool");
        
        pool_size = p.readU30();
        namespaces = new Namespace[pool_size];
        for (int i = 1; i < pool_size; i++)
        {
            int ns_kind = p.readU8();
            int name_idx = p.readU30();
            String ns_name = readPool(strings, name_idx, "string");
            namespaces[i] = new Namespace(ns_kind, ns_name);
            vabc.visitPooledNamespace(namespaces[i]);
        }

        if (verbose)
        	output.println("parsing namespace_set pool");
        
        pool_size = p.readU30();
        namespace_sets = new Nsset[pool_size];
        for (int i = 1; i < pool_size; i++)
        {
            int nsset_size = p.readU30();
            Vector<Namespace> nsset_contents = new Vector<Namespace>(nsset_size);

            for (int j = 0, m = nsset_size; j < m; j++)
                nsset_contents.add(readPool(namespaces, p.readU30(), "namespace"));

            namespace_sets[i] = new Nsset(nsset_contents);
            vabc.visitPooledNsSet(namespace_sets[i]);
        }

        pool_size = p.readU30();

        if (verbose)
        	output.println("parsing name pool");
        
        names = new Name[pool_size];
        List<NameAndPos> forward_ref_names = null;
        for (int i = 1; i < pool_size; i++)
        {
            Name name;
            int name_pos = p.pos;
            names[i] = name = readName(p);
            if (name.isTypeName() && 
                    usesForwardReference(name))
            {
                // If this typename refers to names later in the table, we need to reprocess them later
                // after the entire table has been read in
                if (forward_ref_names == null)
                    forward_ref_names = new ArrayList<NameAndPos>();
                forward_ref_names.add(new NameAndPos(i, name_pos));
            }
            else
            {
                // No forward ref, just visit the name now
                vabc.visitPooledName(name);
            }
        }
        
        if (forward_ref_names != null)
        {
            // save the current location
            int orig_pos = p.pos;
            for (NameAndPos nap : forward_ref_names)
            {
                p.pos = nap.pos;
                Name newName = readName(p);
                names[nap.nameIndex].initTypeName(newName.getTypeNameBase(), newName.getTypeNameParameter());
                // visit the name now that it's been filled in correctly
                vabc.visitPooledName(names[nap.nameIndex]);
            }
            // restore after we're done
            p.pos = orig_pos;
        }

        if (verbose)
        	output.println("parsing method pool");
        
        int n_methods = p.readU30();
        this.methodInfos = new MethodInfo[n_methods];
        this.methodVisitors = new IMethodVisitor[n_methods];
        for (int i = 0, n = n_methods; i < n; i++)
        {
            this.methodInfos[i] = readMethodInfo(p);
            this.methodVisitors[i] = vabc.visitMethod(this.methodInfos[i]);
            if (this.methodVisitors[i] != null)
                this.methodVisitors[i].visit();
        }

        if (verbose)
        	output.println("parsing metadata pool");
        
        pool_size = p.readU30();
        metadata = new Metadata[pool_size];
        for (int i = 0; i < pool_size; i++)
        {
            metadata[i] = readMetadata(p);
            vabc.visitPooledMetadata(metadata[i]);
        }

        if (verbose)
        	output.println("parsing instances pool");
        
        //  InstanceInfos and ClassInfos are stored in
        //  homogenous arrays in the ABC, but their
        //  IClassVisitor needs both in its constructor; so
        //  we read the arrays, remembering the position of
        //  the class and instance traits, and re-read the
        //  traits with the IClassVisitor's traits visitors.
        int n_instances = p.readU30();
        this.instanceInfos = new InstanceInfo[n_instances];
        this.classInfos = new ClassInfo[n_instances];

        for (int i = 0, n = n_instances; i < n; i++)
        {
            this.instanceInfos[i] = readInstanceInfo(p);
        }

        for (int i = 0, n = n_instances; i < n; i++)
        {
            this.classInfos[i] = readClassInfo(p);
        }

        for (int i = 0, n = n_instances; i < n; i++)
        {
            IClassVisitor cv = vabc.visitClass(this.instanceInfos[i], this.classInfos[i]);

            if (cv != null)
            {
                ITraitsVisitor tv = cv.visitClassTraits();
                readTraits(p, tv, this.classInfoToTraits.get(this.classInfos[i]));
                tv.visitEnd();

                tv = cv.visitInstanceTraits();
                readTraits(p, tv, this.instanceInfoToTraits.get(this.instanceInfos[i]));
                tv.visitEnd();
                cv.visitEnd();
            }
        }

        if (verbose)
        	output.println("parsing scripts pool");
        
        int n_scripts = p.readU30();
        for (int i = 0; i < n_scripts; i++)
        {
            IScriptVisitor sv = vabc.visitScript();

            if (sv != null)
            {
                readScript(p, sv);
            }
            else
            {
                p.readU30();
                readTraits(p, NilVisitors.NIL_TRAITS_VISITOR);
            }
        }

        if (verbose)
        	output.println("parsing method bodies pool");
        
        int n_method_bodies = p.readU30();
        for (int i = 0; i < n_method_bodies; i++)
        {
            readBody(vabc, p);
        }

        for (int i = 0; i < n_methods; ++i)
        {
            if (this.methodVisitors[i] != null)
                this.methodVisitors[i].visitEnd();
        }

        vabc.visitEnd();
    }

    private boolean usesForwardReference(Name name)
    {
        Name nameBase = name.getTypeNameBase();
        Name nameParam = name.getTypeNameParameter();
        if (nameBase == null || nameParam == null)
            return true;
        
        if (nameBase.isTypeName() && usesForwardReference(nameBase))
            return true;
        
        if (nameParam.isTypeName() && usesForwardReference(nameParam))
            return true;
        
        return false;
    }
    
    /**
     * Simple struct to keep track of Names, and where in the abc they come from
     */
    private static class NameAndPos
    {
        // the index of the name in the name pool
        public final int nameIndex;
        
        // the pos of the name in the ABC
        public final int pos;

        public NameAndPos(int nameIndex, int pos)
        {
            this.nameIndex = nameIndex;
            this.pos = pos;
        }
    }

    /**
     * Read MetadataInfo structures.
     * <p>
     * <b>Note:</b> Metadata entries can be "keyless". A "keyless" entry's key
     * string index is "0". Although ABC file format defines string_pool[0] to
     * be empty string "", this implementation use {@code null} to represent an
     * "empty key" or "empty value". AET's implementation doesn't guarantee an
     * empty string to have pool index of "0".
     */
    Metadata readMetadata(ABCReader p)
    {
        final String metadata_name = readPool(strings, p.readU30(), "string");
        final int value_count = p.readU30();

        final String[] keys = new String[value_count];
        for (int i = 0; i < value_count; i++)
        {
            final int key_index = p.readU30();
            keys[i] = readPool(strings, key_index, "string");
        }

        final String[] values = new String[value_count];
        for (int i = 0; i < value_count; i++)
        {
            final int value_index = p.readU30();
            values[i] = readPool(strings, value_index, "string");
        }

        final Metadata metadata = new Metadata(metadata_name, keys, values);
        return metadata;
    }

    /**
     * Note that ABCParser does not insert a this pointer. The consumer of the
     * ABC visitor should handle it.
     */
    MethodInfo readMethodInfo(ABCReader p)
    {
        MethodInfo m = new MethodInfo();

        final int param_count = p.readU30();

        final int return_type_index = p.readU30();
        final Name return_type = readPool(names, return_type_index, "name");
        m.setReturnType(return_type);

        final Vector<Name> param_types = new Vector<Name>(param_count);

        for (int j = 0; j < param_count; j++)
        {
            final int param_index = p.readU30();
            final Name param_type = readPool(names, param_index, "name");
            param_types.add(param_type);
        }
        m.setParamTypes(param_types);

        final String methodName = readPool(strings, p.readU30(), "string");
        m.setMethodName(methodName);
        m.setFlags((byte)p.readU8());

        if (m.hasOptional())
        {
            int optional_count = p.readU30();
            assert (optional_count > 0);

            for (int j = 0; j < optional_count; j++)
            {
                m.addDefaultValue(readArgDefault(p));
            }
        }

        if (m.hasParamNames())
        {
            for (int j = 0; j < param_count; j++)
            {
                final int param_name_index = p.readU30();
                final String param_name = readPoolWithDefault(strings, param_name_index, MethodInfo.UNKNOWN_PARAM_NAME);
                m.getParamNames().add(param_name);
            }
        }
        return m;
    }

    Name readName(ABCReader p)
    {
        int kind = p.readU8();
        switch (kind)
        {
            default:
            {
                throw new RuntimeException(String.format("Unknown name kind 0x%x", kind));
            }
            case CONSTANT_TypeName:
            {
                int index = p.readU30(); // Index to the Multiname type, i.e. Vector
                int count = p.readU30(); // number of type parameter names
                assert (count == 1); // all we support for now
                int typeparm = p.readU30(); // Multinames for the type parameters, i.e. String for a Vector.<String>
                Name mn = this.readPool(names, index, "name");
                Name type_param = this.readPool(names, typeparm, "name");
                return new Name(mn, type_param);
            }
            case CONSTANT_Qname:
            case CONSTANT_QnameA:
            {
                int ns_idx = p.readU30();
                Nsset nss = ns_idx != 0 ? new Nsset(readPool(namespaces, ns_idx, "namespace")) : null;
                return new Name(kind, nss, readPool(strings, p.readU30(), "string"));
            }
            case CONSTANT_Multiname:
            case CONSTANT_MultinameA:
            {
                int local_ix = p.readU30();
                int nss_ix = p.readU30();
                return new Name(kind, readPool(namespace_sets, nss_ix, "namespace_set"), readPool(strings, local_ix, "string"));
            }
            case CONSTANT_RTQname:
            case CONSTANT_RTQnameA:
            {
                return new Name(kind, null, readPool(strings, p.readU30(), "string"));
            }
            case CONSTANT_MultinameL:
            case CONSTANT_MultinameLA:
            {
                return new Name(kind, readPool(namespace_sets, p.readU30(), "namespace_set"), null);
            }
            case CONSTANT_RTQnameL:
            case CONSTANT_RTQnameLA:
            {
                return new Name(kind, null, null);
            }
        }
    }

    void readTraits(ABCReader p, ITraitsVisitor traits_visitor)
    {
        assert traits_visitor != null : "Instead of passing null, pass NilVisitors.NIL_TRAITS_VISITOR";

        traits_visitor.visit();

        final int n_traits = p.readU30();
        for (int i = 0; i < n_traits; i++)
        {
            final Name trait_name = readPool(names, p.readU30(), "name");
            final int tag = p.readU8();
            final int kind = tag & ABCConstants.TRAIT_KIND_MASK;

            boolean is_method_getter_setter = false;
            ITraitVisitor trait_visitor;
            switch (kind)
            {
                case TRAIT_Var:
                case TRAIT_Const:
                {
                    int slot_id = p.readU30();
                    Name slot_type = readPool(names, p.readU30(), "name");
                    Object slot_value = readSlotDefault(p);
                    trait_visitor = traits_visitor.visitSlotTrait(kind, trait_name, slot_id, slot_type, slot_value);
                    break;
                }
                case TRAIT_Class:
                {
                    trait_visitor = traits_visitor.visitClassTrait(
                        kind, trait_name, p.readU30(), this.readPool(classInfos, p.readU30(), "classInfo"));
                    break;
                }
                case TRAIT_Method:
                case TRAIT_Getter:
                case TRAIT_Setter:
                {
                    is_method_getter_setter = true;
                }
                case TRAIT_Function:
                {
                    trait_visitor = traits_visitor.visitMethodTrait(
                        kind, trait_name, p.readU30(), this.readPool(methodInfos, p.readU30(), "methodInfo"));
                    break;
                }
                default:
                {
                    throw new IllegalArgumentException(String.format("illegal trait kind 0x%h at offset %d", kind, p.pos));
                }
            }

            // provide a nil-visitor when trait visitor is null
            if (trait_visitor == null)
                trait_visitor = NilVisitors.NIL_TRAIT_VISITOR;

            trait_visitor.visitStart();

            // method, getter, setter has attributes in the high 4 bits of the kind byte
            // 0x01: (1=final,0=virtual), 0x02: (1=override,0=new)
            if (is_method_getter_setter)
            {
                trait_visitor.visitAttribute(Trait.TRAIT_FINAL, functionIsFinal(tag));
                trait_visitor.visitAttribute(Trait.TRAIT_OVERRIDE, functionIsOverride(tag));
            }

            if (traitHasMetadata(tag))
            {
                final int n_entries = p.readU30();
                final IMetadataVisitor mv = trait_visitor.visitMetadata(n_entries);
                for (int j = 0; j < n_entries; j++)
                {
                    final Metadata md = readPool(metadata, p.readU30(), "metadata");
                    if (mv != null)
                        mv.visit(md);
                }
            }

            trait_visitor.visitEnd();
        }
        
        traits_visitor.visitEnd();
    }

    /**
     * High 4 bits of the kind byte: 0x04: (1=has metadata,0=no metadata)
     */
    private boolean traitHasMetadata(int kind)
    {
        return ((kind >> ABCConstants.TRAIT_KIND_SHIFT) & TRAIT_FLAG_metadata) != 0;
    }

    /**
     * High 4 bits of the kind byte: 0x01: (1=final,0=virtual)
     */
    private boolean functionIsFinal(int kind)
    {
        return ((kind >> ABCConstants.TRAIT_KIND_SHIFT) & ABCConstants.TRAIT_FLAG_final) != 0;
    }

    /**
     * High 4 bits of the kind byte: 0x02: (1=override,0=new)
     */
    private boolean functionIsOverride(int kind)
    {
        return ((kind >> ABCConstants.TRAIT_KIND_SHIFT) & ABCConstants.TRAIT_FLAG_override) != 0;
    }

    void readTraits(ABCReader p, ITraitsVisitor tv, Integer pos)
    {
        //  Save current position, reposition as requested
        int saved_pos = p.pos;
        p.pos = pos;

        readTraits(p, tv);

        //  Restore saved position
        p.pos = saved_pos;
    }

    Object readSlotDefault(ABCReader p)
    {
        int i = p.readU30();
        if (i != 0)
        {
            int kind = p.readU8();
            return defaultValue(kind, i);
        }

        // changed from returning null to fix CMP-357.  Note that this assumes
        // that a 0 will be dumped by the ABCEmitter when it hits a
        // ABCConstants.UNDEFINED_VALUE to match behavior
        return ABCConstants.UNDEFINED_VALUE;
    }

    private PooledValue readArgDefault(ABCReader p)
    {
        int i = p.readU30();
        int kind = p.readU8();
        Object v = defaultValue(kind, i);
        PooledValue result = new PooledValue(kind, v);
        return result;
    }

    Object defaultValue(int kind, int i)
    {
        if (i == ABCConstants.ZERO_INDEX)
            return ABCConstants.NULL_VALUE;
        
        switch (kind)
        {
            case CONSTANT_False:
            {
                return Boolean.FALSE;
            }
            case CONSTANT_True:
            {
                return Boolean.TRUE;
            }
            case CONSTANT_Null:
            {
                return ABCConstants.NULL_VALUE;
            }
            case CONSTANT_Utf8:
            {
                return readPool(strings, i, "string");
            }
            case CONSTANT_Int:
            {
                return readIntPool(i);
            }
            case CONSTANT_UInt:
            {
                return readUintPool(i);
            }
            case CONSTANT_Double:
            {
                return readDoublePool(i);
            }
            case CONSTANT_Namespace:
            {
                return readPool(namespaces, i, "namespace");
            }
            case CONSTANT_PackageNs:
            case CONSTANT_ProtectedNs:
            case CONSTANT_PackageInternalNs:
            case CONSTANT_StaticProtectedNs:
            case CONSTANT_PrivateNs:
            {
                return readPool(namespaces, i, "namespace");
            }
        }
        
        assert false : "Unknown value kind " + Integer.toString(i);
        return null;
    }

    void readScript(ABCReader p, IScriptVisitor sv)
    {
        sv.visit();
        sv.visitInit(this.readPool(methodInfos, p.readU30(), "methodInfo"));
        ITraitsVisitor scriptTraitsVisitor = sv.visitTraits();
        readTraits(p, scriptTraitsVisitor);
        scriptTraitsVisitor.visitEnd();
        sv.visitEnd();
    }

    Map<ClassInfo, Integer> classInfoToTraits = new HashMap<ClassInfo, Integer>();

    ClassInfo readClassInfo(ABCReader p)
    {
        ClassInfo cinfo = new ClassInfo();
        cinfo.cInit = this.readPool(methodInfos, p.readU30(), "methodInfo");

        //  Record the position of these traits, then skip past them.
        //  They'll be re-read when the class' IClassVisitor is initialized.
        classInfoToTraits.put(cinfo, p.pos);
        readTraits(p, NilVisitors.NIL_TRAITS_VISITOR);

        return cinfo;
    }

    Map<InstanceInfo, Integer> instanceInfoToTraits = new HashMap<InstanceInfo, Integer>();

    InstanceInfo readInstanceInfo(ABCReader p)
    {
        InstanceInfo iinfo = new InstanceInfo();

        iinfo.name = readPool(names, p.readU30(), "name");
        iinfo.superName = readPool(names, p.readU30(), "name");

        iinfo.flags = p.readU8();
        if (iinfo.hasProtectedNs())
            iinfo.protectedNs = readPool(namespaces, p.readU30(), "namespace");
        iinfo.interfaceNames = new Name[p.readU30()];
        for (int j = 0, n = iinfo.interfaceNames.length; j < n; j++)
        {
            iinfo.interfaceNames[j] = readPool(names, p.readU30(), "name");
        }

        iinfo.iInit = this.readPool(methodInfos, p.readU30(), "methodInfo");

        //  Save the position of the instance traits
        //  for the IClassVisitor, which needs the 
        //  InstanceInfo in its constructor, and  read 
        //  the traits with a nil visitor to skip them.
        instanceInfoToTraits.put(iinfo, p.pos);
        readTraits(p, NilVisitors.NIL_TRAITS_VISITOR);

        return iinfo;
    }

    void readBody(IABCVisitor vabc, ABCReader p)
    {
        MethodBodyInfo mb = new MethodBodyInfo();

        int method_id = p.readU30();

        mb.max_stack = p.readU30();
        mb.max_local = p.readU30();
        mb.initial_scope = p.readU30();
        mb.max_scope = p.readU30();
        mb.code_len = p.readU30();
        mb.setMethodInfo(this.readPool(methodInfos, method_id, "methodInfo"));

        IMethodBodyVisitor mv = null;
        ITraitsVisitor tv = NilVisitors.NIL_TRAITS_VISITOR;

        if (this.readPool(methodVisitors, method_id, "methodVisitor") != null)
            mv = this.readPool(methodVisitors, method_id, "methodVisitor").visitBody(mb);

        if (mv != null)
        {
            mv.visit();
            readCode(mb, mv, p);
            tv = mv.visitTraits();
            mv.visitEnd();
            this.readPool(methodVisitors, method_id, "methodVisitor").visitEnd();
            this.methodVisitors[method_id] = null;
        }
        else
        {
            p.pos += mb.code_len;
            skipExceptions(p);
        }

        readTraits(p, tv);
        tv.visitEnd();
    }

    void readCode(MethodBodyInfo mb, IMethodBodyVisitor m, ABCReader p)
    {
        int end_pos = p.pos + mb.code_len;

        //  Read the exception information to get label information 
        //  that can't be deduced from the instructions themselves.
        Map<Integer, Label> labels_by_pos = readExceptions(p, m, end_pos);

        while (p.pos < end_pos)
        {
            int insn_pos = p.pos;
            int op = p.readU8();

            switch (op)
            {
                //  Instructions with no operands.
                case OP_add:
                case OP_add_i:
                case OP_astypelate:
                case OP_bitand:
                case OP_bitnot:
                case OP_bitor:
                case OP_bitxor:
                case OP_checkfilter:
                case OP_coerce_a:
                case OP_coerce_b:
                case OP_coerce_d:
                case OP_coerce_i:
                case OP_coerce_s:
                case OP_convert_b:
                case OP_convert_i:
                case OP_convert_d:
                case OP_convert_o:
                case OP_convert_u:
                case OP_convert_s:
                case OP_decrement:
                case OP_decrement_i:
                case OP_divide:
                case OP_dup:
                case OP_dxnslate:
                case OP_equals:
                case OP_esc_xattr:
                case OP_esc_xelem:
                case OP_getglobalscope:
                case OP_getlocal0:
                case OP_getlocal1:
                case OP_getlocal2:
                case OP_getlocal3:
                case OP_greaterequals:
                case OP_greaterthan:
                case OP_hasnext:
                case OP_in:
                case OP_increment:
                case OP_increment_i:
                case OP_instanceof:
                case OP_istypelate:
                case OP_lessequals:
                case OP_lessthan:
                case OP_lshift:
                case OP_modulo:
                case OP_multiply:
                case OP_multiply_i:
                case OP_negate:
                case OP_negate_i:
                case OP_newactivation:
                case OP_nextname:
                case OP_nextvalue:
                case OP_nop:
                case OP_not:
                case OP_pop:
                case OP_popscope:
                case OP_pushfalse:
                case OP_pushtrue:
                case OP_pushnan:
                case OP_pushnull:
                case OP_pushscope:
                case OP_pushundefined:
                case OP_pushwith:
                case OP_returnvalue:
                case OP_returnvoid:
                case OP_rshift:
                case OP_setlocal0:
                case OP_setlocal1:
                case OP_setlocal2:
                case OP_setlocal3:
                case OP_strictequals:
                case OP_subtract:
                case OP_subtract_i:
                case OP_swap:
                case OP_throw:
                case OP_typeof:
                case OP_unplus:
                case OP_urshift:
                case OP_bkpt:
                case OP_timestamp:
                case OP_coerce_o:
                case OP_li8:
                case OP_li16:
                case OP_li32:
                case OP_lf32:
                case OP_lf64:
                case OP_si8:
                case OP_si16:
                case OP_si32:
                case OP_sf32:
                case OP_sf64:
                case OP_sxi1:
                case OP_sxi8:
                case OP_sxi16:
                {
                    m.visitInstruction(op);
                    break;
                }

                // Opcodes with two uint operands.
                case OP_hasnext2:
                {
                    m.visitInstruction(op, new Object[] {Integer.valueOf(p.readU30()), Integer.valueOf(p.readU30())});
                    break;
                }

                // Opcodes with a MethodInfo and an integer operand.
                case OP_callstatic:
                {
                    m.visitInstruction(op, new Object[] {this.readPool(methodInfos, p.readU30(), "methodInfo"), Integer.valueOf(p.readU30())});
                    break;
                }

                // Opcodes with one name operand.
                case OP_findproperty:
                case OP_findpropstrict:
                case OP_getlex:
                case OP_getsuper:
                case OP_setsuper:
                case OP_getproperty:
                case OP_setproperty:
                case OP_deleteproperty:
                case OP_getdescendants:
                case OP_initproperty:
                case OP_istype:
                case OP_coerce:
                case OP_astype:
                case OP_finddef:
                {
                    m.visitInstruction(op, readPool(names, p.readU30(), "name"));
                    break;
                }
                
                // Opcodes with a name and an integer operand.
                case OP_callproperty:
                case OP_callproplex:
                case OP_callpropvoid:
                case OP_callsuper:
                case OP_callsupervoid:
                case OP_constructprop:
                {
                    m.visitInstruction(op, new Object[] {readPool(names, p.readU30(), "name"), p.readU30()});
                    break;
                }

                // Opcodes with an unsigned immediate operand.
                case OP_constructsuper:
                case OP_call:
                case OP_construct:
                case OP_newarray:
                case OP_newobject:
                {
                    m.visitInstruction(op, p.readU30());
                    break;
                }

                // Opcodes with a branch operand (a signed immediate operand
                // that designates a jump target).
                case OP_ifnlt:
                case OP_ifnle:
                case OP_ifngt:
                case OP_ifnge:
                case OP_iftrue:
                case OP_iffalse:
                case OP_ifeq:
                case OP_ifne:
                case OP_iflt:
                case OP_ifle:
                case OP_ifgt:
                case OP_ifge:
                case OP_ifstricteq:
                case OP_ifstrictne:
                case OP_jump:
                {
                    //  Jump offset computed from the
                    //  instruction following the branch.
                    int jump_target = p.readS24() + p.pos;

                    if (jump_target < insn_pos)
                    {
                        assert (labels_by_pos.containsKey(jump_target)) : "Unmapped backwards branch target " + jump_target + ", insn_pos " + insn_pos + ": " + labels_by_pos;
                    }

                    m.visitInstruction(op, addLabel(jump_target, labels_by_pos));
                    break;
                }

                // Lookupswitch, with a table of labels.
                case OP_lookupswitch:
                {
                    int default_case = p.readS24() + insn_pos;
                    Label default_label = addLabel(default_case, labels_by_pos);

                    int case_count = p.readU30() + 1;
                    Label[] switch_labels = new Label[case_count + 1];
                    switch_labels[case_count] = default_label;

                    for (int i = 0; i < case_count; i++)
                    {
                        int current_case = p.readS24() + insn_pos;
                        switch_labels[i] = addLabel(current_case, labels_by_pos);
                    }

                    m.visitInstruction(op, switch_labels);
                    break;
                }

                // OP_label, no operands generates a label as a side effect.
                case OP_label:
                {
                    addLabel(insn_pos, labels_by_pos);
                    m.visitInstruction(op);
                    break;
                }

                // Opcodes with an immediate integer operand.
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
                case OP_pushshort:
                case OP_debugline:
                case OP_bkptline:
                {
                    m.visitInstruction(op, p.readU30());
                    break;
                }

                // Opcodes with an instance operand.
                case OP_newclass:
                {
                    m.visitInstruction(op, this.readPool(classInfos, p.readU30(), "classInfo"));
                    break;
                }

                // Opcodes with a byte operand.
                case OP_pushbyte:
                case OP_getscopeobject:
                {
                    m.visitInstruction(op, p.readU8());
                    break;
                }

                // Opcodes with a string operand.
                case OP_pushstring:
                case OP_dxns:
                case OP_debugfile:
                {
                    m.visitInstruction(op, this.readPool(strings, p.readU30(), "string"));
                    break;
                }

                // Opcodes with a namespace operand.
                case OP_pushnamespace:
                {
                    m.visitInstruction(op, this.readPool(namespaces, p.readU30(), "namespace"));
                    break;
                }

                // Opcodes with a pooled operand.
                case OP_pushint:
                {
                    m.visitInstruction(op, Integer.valueOf(this.readIntPool(p.readU30())));
                    break;
                }
                case OP_pushuint:
                {
                    m.visitInstruction(op, Long.valueOf(this.readUintPool(p.readU30())));
                    break;
                }
                case OP_pushdouble:
                {
                    m.visitInstruction(op, Double.valueOf(this.readDoublePool(p.readU30())));
                    break;
                }

                // Opcodes with a function operand.
                case OP_newfunction:
                {
                    m.visitInstruction(op, this.readPool(methodInfos, p.readU30(), "methodInfo"));
                    break;
                }
                
                // Opcodes with a grab-bag of operands.
                case OP_debug:
                {
                    m.visitInstruction(op, new Object[] {p.readU8(), this.readPool(strings, p.readU30(), "string"), p.readU8(), p.readU30()});
                    break;
                }
                
                default:
                {
                    throw new IllegalArgumentException(String.format("Unknown ABC bytecode 0x%x", op));
                }
            }

            Label insn_label = labels_by_pos.get(insn_pos);
            if (insn_label != null)
                m.labelCurrent(insn_label);
        }

        //  Skip over the exception table, it's already been read.
        skipExceptions(p);
    }

    private Map<Integer, Label> readExceptions(ABCReader p, IMethodBodyVisitor mbv, int end_pos)
    {
        Map<Integer, Label> result = new HashMap<Integer, Label>();

        int start_pos = p.pos;
        p.pos = end_pos;

        int n_exceptions = p.readU30();

        for (int i = 0; i < n_exceptions; i++)
        {
            int from_pos = p.readU30() + start_pos;
            int to_pos = p.readU30() + start_pos;
            int catch_pos = p.readU30() + start_pos;

            if (!result.containsKey(from_pos))
                result.put(from_pos, new Label("try/from", Label.LabelKind.ANY_INSTRUCTION));
           
            if (!result.containsKey(to_pos))
                result.put(to_pos, new Label("try/to", Label.LabelKind.ANY_INSTRUCTION));
            
            if (!result.containsKey(catch_pos))
                result.put(catch_pos, new Label("catch"));

            Name catch_type = readPool(names, p.readU30(), "name");
            int catch_idx = p.readU30();
            Name catch_var = readPool(names, catch_idx, "name");

            if (mbv != null)
                mbv.visitException(result.get(from_pos), result.get(to_pos), result.get(catch_pos), catch_type, catch_var);
        }

        p.pos = start_pos;
        return result;
    }

    private void skipExceptions(ABCReader p)
    {
        int n_exceptions = p.readU30();
        for (int i = 0; i < n_exceptions * 5; i++)
        {
            p.readU30();
        }
    }

    private Label addLabel(int key, Map<Integer, Label> labels)
    {
        if (!labels.containsKey(key))
            labels.put(key, new Label());
        
        return labels.get(key);
    }

    /**
     * Read a required value from a pool.
     * 
     * @param pool - the pool of interest.
     * @param idx - the proposed pool index.
     * @param poolName - the name of the pool, used for diagnostics.
     * @return the pool's value at the specified index.
     * @throws IllegalStateException if the index is out of range.
     */
    private <T> T readPool(T[] pool, final int idx, final String poolName)
    {
        if (idx < 0 || idx >= pool.length)
            throw new IllegalStateException(String.format("%d is not a valid %s pool index.", idx, poolName));

        return pool[idx];
    }

    /**
     * Read an optional value from a pool.
     * 
     * @param pool - the pool of interest.
     * @param idx - the proposed pool index.
     * @param defaultValue - the default value to use if the pool entry's not
     * present.
     * @return the pool's value at the specified index, or null if the index is
     * out of range.
     */
    private <T> T readPoolWithDefault(T[] pool, final int idx, final T defaultValue)
    {
        if (idx < 0)
            throw new IllegalStateException(String.format("%d is not a valid pool index", idx));

        else if (idx >= pool.length)
            return defaultValue;

        return pool[idx];
    }

    /**
     * Read a required value from the int pool.
     * 
     * @param idx - the proposed pool index.
     * @return the pool's value at the specified index.
     * @throws IllegalStateException if the index is out of range.
     */
    private int readIntPool(final int idx)
    {
        if (idx < 0 || idx >= this.ints.length)
            throw new IllegalStateException(String.format("%d is not a valid int pool index.", idx));

        return this.ints[idx];
    }

    /**
     * Read a required value from the uint pool.
     * 
     * @param idx - the proposed pool index.
     * @return the pool's value at the specified index.
     * @throws IllegalStateException if the index is out of range.
     */
    private long readUintPool(final int idx)
    {
        if (idx < 0 || idx >= this.uints.length)
            throw new IllegalStateException(String.format("%d is not a valid uint pool index.", idx));

        return this.uints[idx];
    }

    /**
     * Read a required value from the double pool.
     * 
     * @param idx - the proposed pool index.
     * @return the pool's value at the specified index.
     * @throws IllegalStateException if the index is out of range.
     */
    private double readDoublePool(final int idx)
    {
        if (idx < 0 || idx >= this.doubles.length)
            throw new IllegalStateException(String.format("%d is not a valid double pool index.", idx));

        return this.doubles[idx];
    }

}
