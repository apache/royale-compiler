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
import org.apache.royale.abc.semantics.ScriptInfo;
import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.semantics.Traits;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IMetadataVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements an IABCVisitor which will simply collect
 * all of the structures of an ABC block, such as constant pools, method
 * infos, class infos, etc into various pools so they can be processed later
 * in a different order than the IABCVisitor events are received in.
 */
public class PoolingABCVisitor implements IABCVisitor
{
    public PoolingABCVisitor ()
    {
    }

    public void visit (int majorVersion, int minorVersion)
    {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public IScriptVisitor visitScript ()
    {
        return new ScriptVisitor();
    }

    public IClassVisitor visitClass (InstanceInfo iinfo, ClassInfo cinfo)
    {
        return new ClassVisitor(iinfo, cinfo);
    }

    public IMethodVisitor visitMethod (MethodInfo minfo)
    {
        return new MethodVisitor(minfo);
    }

    @Override
    public void visitPooledDouble (Double d)
    {
        this.doublePool.add(d);
    }

    @Override
    public void visitPooledInt (Integer i)
    {
        this.intPool.add(i);
    }

    @Override
    public void visitPooledMetadata (Metadata md)
    {
        this.metadataPool.add(md);
    }

    @Override
    public void visitPooledName (Name n)
    {
        this.namePool.add(n);
    }

    @Override
    public void visitPooledNamespace (Namespace ns)
    {
        this.nsPool.add(ns);

        if (ns != null)
            visitPooledString(ns.getVersionedName());
    }

    @Override
    public void visitPooledNsSet (Nsset nss)
    {
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
    public void visitPooledString (String s)
    {
        this.stringPool.add(s);
    }

    @Override
    public void visitPooledUInt (Long l)
    {
        this.uintPool.add(l);
    }

    private int majorVersion;

    private int minorVersion;


    private final Pool<Name> namePool = new Pool<Name>(Pool.DefaultType.HasDefaultZero);

    private final Pool<String> stringPool = new Pool<String>(Pool.DefaultType.HasDefaultZero);

    private final Pool<Integer> intPool = new Pool<Integer>(Pool.DefaultType.HasDefaultZero);

    private final Pool<Long> uintPool = new Pool<Long>(Pool.DefaultType.HasDefaultZero);

    private final Pool<Double> doublePool = new Pool<Double>(Pool.DefaultType.HasDefaultZero);

    private final Pool<Namespace> nsPool = new Pool<Namespace>(Pool.DefaultType.HasDefaultZero);

    private final Pool<Nsset> nssetPool = new Pool<Nsset>(Pool.DefaultType.HasDefaultZero);

    private final Pool<Metadata> metadataPool = new Pool<Metadata>(Pool.DefaultType.NoDefaultZero);

    private List<ScriptInfo> scriptInfos = new ArrayList<ScriptInfo>();
    private List<MethodBodyInfo> methodBodies = new ArrayList<MethodBodyInfo>();
    private EntryOrderedStore<MethodInfo> methodInfos = new EntryOrderedStore<MethodInfo>();
    private List<ClassVisitor> definedClasses = new ArrayList<ClassVisitor>();
    private Map<MethodInfo, MethodBodyInfo> methodMap = new HashMap<MethodInfo, MethodBodyInfo>();

    /**
     * Major version of the ABC
     */
    public int getMajorVersion ()
    {
        return majorVersion;
    }

    /**
     * Minor version of the ABC
     */
    public int getMinorVersion ()
    {
        return minorVersion;
    }

    /**
     * Name pool, has default zero entry.
     */
    public Pool<Name> getNamePool ()
    {
        return namePool;
    }

    /**
     * String pool, has default zero entry.
     */
    public Pool<String> getStringPool ()
    {
        return stringPool;
    }

    /**
     * int pool, has default zero entry.
     */
    public Pool<Integer> getIntPool ()
    {
        return intPool;
    }

    /**
     * uint pool, has default zero entry.
     */
    public Pool<Long> getUintPool ()
    {
        return uintPool;
    }

    /**
     * double pool, has default zero entry.
     */
    public Pool<Double> getDoublePool ()
    {
        return doublePool;
    }

    /**
     * namespace pool, has default zero entry.
     */
    public Pool<Namespace> getNsPool ()
    {
        return nsPool;
    }

    /**
     * namespace set pool, has default zero entry.
     */
    public Pool<Nsset> getNssetPool ()
    {
        return nssetPool;
    }

    /**
     * metadata pool, does not have default zero entry.
     */
    public Pool<Metadata> getMetadataPool ()
    {
        return metadataPool;
    }

    /**
     * Get a list of the ScriptInfos
     */
    public List<ScriptInfo> getScriptInfos ()
    {
        return scriptInfos;
    }

    /**
     * Get a list of the method bodies
     */
    public List<MethodBodyInfo> getMethodBodies ()
    {
        return methodBodies;
    }

    /**
     * Get a list of the method infos
     */
    public EntryOrderedStore<MethodInfo> getMethodInfos ()
    {
        return methodInfos;
    }

    /**
     * Get a list of the defined classes
     */
    public List<ClassVisitor> getDefinedClasses ()
    {
        return definedClasses;
    }

    /**
     * Script visitor that adds its ScriptInfo to the
     * list of ScriptInfos
     */
    private class ScriptVisitor implements IScriptVisitor
    {
        ScriptVisitor ()
        {
            this.si = new ScriptInfo();
        }

        final ScriptInfo si;

        @Override
        public void visit ()
        {
        }

        @Override
        public void visitEnd ()
        {
            scriptInfos.add(this.si);
        }

        @Override
        public void visitInit (MethodInfo init_method)
        {
            si.setInit(init_method);
        }

        @Override
        public ITraitsVisitor visitTraits ()
        {
            return new TraitsVisitor(si.getTraits());
        }
    }

    /**
     * Class Visitor that adds itself to the list
     * of defined classes
     */
    protected class ClassVisitor implements IClassVisitor
    {
        ClassVisitor (InstanceInfo iinfo, ClassInfo cinfo)
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
        Traits classTraits;
        InstanceInfo instanceInfo;
        Traits instanceTraits;

        @Override
        public void visit ()
        {
        }

        @Override
        public ITraitsVisitor visitClassTraits ()
        {
            return new TraitsVisitor(this.classTraits);
        }

        @Override
        public ITraitsVisitor visitInstanceTraits ()
        {
            return new TraitsVisitor(this.instanceTraits);
        }

        @Override
        public void visitEnd ()
        {
            definedClasses.add(this);
        }

        public InstanceInfo getInstanceInfo ()
        {
            return this.instanceInfo;
        }
    }

    /**
     * Traits Visitor that populates a Traits object
     */
    private class TraitsVisitor implements ITraitsVisitor
    {
        TraitsVisitor (Traits traits)
        {
            this.traits = traits;
        }

        Traits traits;

        @Override
        public ITraitVisitor visitClassTrait (int kind, Name name, int slot_id, ClassInfo clazz)
        {
            Trait t = createTrait(kind, name);
            if (slot_id != 0)
                t.addAttr(Trait.TRAIT_SLOT, slot_id);
            t.addAttr(Trait.TRAIT_CLASS, clazz);

            return new TraitVisitor(t);
        }

        @Override
        public ITraitVisitor visitMethodTrait (int kind, Name name, int dispId, MethodInfo method)
        {
            Trait t = createTrait(kind, name);
            t.addAttr(Trait.TRAIT_METHOD, method);
            if (dispId != 0)
                t.addAttr(Trait.TRAIT_DISP, dispId);
            return new TraitVisitor(t);
        }

        @Override
        public ITraitVisitor visitSlotTrait (int kind, Name name, int slotId, Name slotType, Object slotValue)
        {
            Trait t = createTrait(kind, name);
            t.addAttr(Trait.TRAIT_SLOT, slotId);
            t.addAttr(Trait.TRAIT_TYPE, slotType);
            t.addAttr(Trait.SLOT_VALUE, slotValue);
            if (slotType != null)
                visitPooledName(slotType);
            return new TraitVisitor(t);
        }

        @Override
        public void visit ()
        {
        }

        @Override
        public void visitEnd ()
        {
        }

        @Override
        public Traits getTraits ()
        {
            return this.traits;
        }

        private Trait createTrait (int kind, Name name)
        {
            Trait t = new Trait(kind, name);
            traits.add(t);
            return t;
        }
    }

    /**
     * Trait Visitor that creates a Trait object
     */
    private class TraitVisitor implements ITraitVisitor
    {
        TraitVisitor (Trait t)
        {
            this.t = t;
        }

        Trait t;

        @Override
        public IMetadataVisitor visitMetadata (int count)
        {
            return new IMetadataVisitor()
            {

                @Override
                public void visit (Metadata md)
                {
                    t.addMetadata(md);
                }
            };
        }

        @Override
        public void visitAttribute (String attr_name, Object attr_value)
        {
            this.t.addAttr(attr_name, attr_value);
        }

        @Override
        public void visitStart ()
        {
        }

        @Override
        public void visitEnd ()
        {
        }
    }

    /**
     * Method Body Visitor that adds its MethodBodyInfo to the list of method bodies
     */
    private class MethodBodyVisitor implements IMethodBodyVisitor
    {
        MethodBodyVisitor (org.apache.royale.abc.semantics.MethodBodyInfo mbinfo)
        {
            this.mbi = mbinfo;
        }

        MethodBodyInfo mbi;

        @Override
        public void visit ()
        {
        }

        @Override
        public void visitEnd ()
        {
            methodBodies.add(mbi);
            methodMap.put(mbi.getMethodInfo(), mbi);
        }

        @Override
        public void visitInstruction (int opcode)
        {
            this.mbi.insn(opcode);
        }

        @Override
        public void visitInstruction (int opcode, int immediate_operand)
        {
            this.mbi.insn(opcode, immediate_operand);
        }

        @Override
        public void visitInstruction (int opcode, Object single_operand)
        {
            this.mbi.insn(opcode, single_operand);
        }

        @Override
        public void visitInstruction (int opcode, Object[] operands)
        {
            this.mbi.insn(opcode, operands);
        }

        @Override
        public void visitInstruction (Instruction insn)
        {
            this.mbi.insn(insn);
        }

        @Override
        public ITraitsVisitor visitTraits ()
        {
            return new TraitsVisitor(this.mbi.getTraits());
        }

        @Override
        public int visitException (Label from, Label to, Label target, Name ex_type, Name ex_var)
        {
            return mbi.addExceptionInfo(new ExceptionInfo(from, to, target, ex_type, ex_var));
        }

        @Override
        public void visitInstructionList (InstructionList new_list)
        {
            mbi.setInstructionList(new_list);
        }

        @Override
        public void labelCurrent (Label l)
        {
            mbi.labelCurrent(l);
        }

        @Override
        public void labelNext (Label l)
        {
            mbi.labelNext(l);
        }
    }

    /**
     * Method Visitor that adds its MethodInfo to the list of MethodInfos
     */
    private class MethodVisitor implements IMethodVisitor
    {
        MethodVisitor (MethodInfo mi)
        {
            assert (mi != null);
            this.mi = mi;
        }

        final MethodInfo mi;

        @Override
        public void visit ()
        {
            methodInfos.add(mi);
        }

        @Override
        public IMethodBodyVisitor visitBody (MethodBodyInfo mbi)
        {
            return new MethodBodyVisitor(mbi);
        }

        @Override
        public void visitEnd ()
        {
        }
    }

    /**
     * @return the class ID of the given ClassInfo.
     * @throws IllegalArgumentException if the class is not found.
     */
    protected int getClassId (ClassInfo info)
    {
        int id_index = 0;
        for (ClassVisitor candidate : this.definedClasses)
        {
            if (candidate.classInfo == info)
                return id_index;
            else
                id_index++;
        }

        throw new IllegalArgumentException("Unable to find ClassInfo index for " + info);
    }

    /**
     * Get the corresponding MethodBodyInfo for a given MethodInfo
     * @param mi    the MethodInfo you want the body for
     * @return      the MethodBodyInfo which is the body of the given MethodInfo
     */
    protected MethodBodyInfo getMethodBodyForMethodInfo (MethodInfo mi)
    {
        return methodMap.get(mi);
    }

    @Override
    public void visitEnd ()
    {
    }
}
