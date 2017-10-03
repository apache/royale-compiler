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

package org.apache.royale.compiler.internal.as.codegen;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.InstructionFactory;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.visitors.ITraitsVisitor;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  A Binding collects information about a name that
 *  is relevant to the code generator:
 *  <ul>
 *    <li>Semantic definition
 *    <li>Local register access instructions --
 *        cached here so they can be shared and
 *        assigned concrete registers at function wrapup.
 *  </ul>
 *  Future enhancements:
 *  <ul>
 *    <li>Cache more binding information for better codegen.
 *    <li>Enforce const-ness of constant locals.
 *  </ul>
 */

public class Binding
{

    /**
     *  The IASNode that contributed the Name,
     *  usually an IdentifierNode.  May be null
     *  if the Binding is synthetic.
     */
    private final IASNode node;

    /**
     * Data structure to hold information that can be shared between many bindings.
     *
     * These are things like the local register, the name, etc.
     */
    private final SharedBindingInfo sharedBindingInfo;

    /**
     *  Construct an empty Binding.  Used for Bindings
     *  that refer to internal constructs, e.g., this.
     */
    public Binding()
    {
        this(null, null, null);
    }

    /**
     *  Construct a Binding.
     *  @param name - the Binding's name.
     *  @param def - the Binding's definition, if known.
     */
    public Binding(IASNode node, Name name, IDefinition def)
    {
        this.node = node;
        this.sharedBindingInfo = new SharedBindingInfo(name, def);
    }

    /**
     * Construct a Binding with the given node, but that otherwise has the same
     * properties as the other binding passed in.
     * @param node  the node that generated this binding
     * @param other the binding with the shared binding info to use
     */
    public Binding(IASNode node, Binding other)
    {
        this.node = node;
        this.sharedBindingInfo = other.sharedBindingInfo;
    }

    /**
     * Get the multiname for this binding - used to generate access by name
     * @return  The name for the binding
     */
    public Name getName()
    {
        return sharedBindingInfo.getName();
    }

    /**
     *  Get the AST node that generated this binding, used for error reporting.
     *  @return the IASNode, or null if not present.
     */
    public IASNode getNode()
    {
        return node;
    }

    /*
     *  Get the definition for this binding.
     *  @return the definition associated with the binding.
     */
    public IDefinition getDefinition()
    {
        return sharedBindingInfo.getDefinition();
    }

    /**
     *  @return true if this Binding has been
     *    flagged as resident in a local register.
     */
    public boolean isLocal()
    {
        return sharedBindingInfo.isLocal();
    }

    /**
     *  Flag this Binding as resident in a
     *  local register (or unset the flag).
     *  @param is_local - the new value of the 
     *    "binding lives in a register" flag.
     */
    public void setIsLocal(boolean is_local)
    {
        sharedBindingInfo.setIsLocal(is_local);
    }

    /**
     *  @return true if this Binding's local number
     *    has already been set.  Useful if a group of
     *    Bindings have subgroups that may get their
     *    local numbers set in distinct phases (e.g.,
     *    the set of Bindings in registers has a subset
     *    of parameter Bindings that have preallocated
     *    register numbers).
     */
    public boolean localNumberIsSet()
    {
        return sharedBindingInfo.localNumberIsSet();
    }


    /**
     *  @return this Binding's local register number.
     */
    public int getLocalRegister()
    {
        return sharedBindingInfo.getLocalRegister();
    }

    /**
     *  Set this binding's local register number.
     *  @param local_num - the binding's local register.
     */
    void setLocalRegister(int local_num)
    {

        sharedBindingInfo.setLocalRegister(local_num);
    }

    /**
     *  Fetch this Binding's getlocal instruction;
     *  the Binding manages a single instruction 
     *  so that its register number can be assigned
     *  at function wrap-up time.
     *  @return the Binding's getlocal instruction.
     *    The instruction will be created if not 
     *    already present.
     */
    public Instruction getlocal()
    {
        return sharedBindingInfo.getlocal();
    }


    /**
     *  Fetch this Binding's setlocal instruction;
     *  the Binding manages a single instruction 
     *  so that its register number can be assigned
     *  at function wrap-up time.
     *  @return the Binding's setlocal instruction.
     *    The instruction will be created if not 
     *    already present.
     */
    public Instruction setlocal()
    {
        return sharedBindingInfo.setlocal();
    }

    /**
     *  Fetch this Binding's inclocal instruction;
     *  the Binding manages a single instruction 
     *  so that its register number can be assigned
     *  at function wrap-up time.
     *  @return the Binding's inclocal instruction.
     *    The instruction will be created if not 
     *    already present.
     */
    public Instruction inclocal()
    {
        return sharedBindingInfo.inclocal();
    }

    /**
     *  Fetch this Binding's inclocal_i instruction;
     *  the Binding manages a single instruction
     *  so that its register number can be assigned
     *  at function wrap-up time.
     *  @return the Binding's inclocal_i instruction.
     *    The instruction will be created if not
     *    already present.
     */
    public Instruction inclocal_i()
    {
        return sharedBindingInfo.inclocal_i();
    }

    /**
     *  Fetch this Binding's declocal instruction;
     *  the Binding manages a single instruction 
     *  so that its register number can be assigned
     *  at function wrap-up time.
     *  @return the Binding's declocal instruction.
     *    The instruction will be created if not 
     *    already present.
     */
    public Instruction declocal()
    {
        return sharedBindingInfo.declocal();
    }

    /**
     *  Fetch this Binding's declocal_i instruction;
     *  the Binding manages a single instruction
     *  so that its register number can be assigned
     *  at function wrap-up time.
     *  @return the Binding's declocal_i instruction.
     *    The instruction will be created if not
     *    already present.
     */
    public Instruction declocal_i()
    {
        return sharedBindingInfo.declocal_i();
    }

    /**
     * Get the Binding's slot id.  If this is 0, it
     * is an AVM assigned id.
     * 
     * @return slot id.
     */
    public int getSlotId()
    {
        return sharedBindingInfo.getSlotId();
    }

    /**
     * @return true if this Binding's slot id number
     *         has been set, rather than relying
     *         on a runtime slot.
     */
    public boolean slotIdIsSet()
    {
        return sharedBindingInfo.slotIdIsSet();
    }

    /**
     * Set the Binding's slot id.  If this is 0, it
     * is an AVM assigned id.
     */
    public void setSlotId(int slotId)
    {
        sharedBindingInfo.setSlotId(slotId);
    }

    /**
     *  Fetch this Binding's kill instruction;
     *  the Binding manages a single instruction 
     *  so that its register number can be assigned
     *  at function wrap-up time.
     *  @return the Binding's kill instruction.
     *    The instruction will be created if not 
     *    already present.
     */
    public Instruction kill()
    {
        return sharedBindingInfo.kill();
    }

    @Override
    public String toString()
    {
        return sharedBindingInfo.toString();
    }

    /**
     *  Indicate that this Binding had a "super" qualifier.
     *  @param super_qualified - set true if the name that generated
     *    this Binding had an explicit "super" qualifier.
     */
    public void setSuperQualified(final boolean super_qualified)
    {
        sharedBindingInfo.setSuperQualified(super_qualified);
    }

    /**
     *  Was this Binding explicitly qualified with "super"?
     *  @return true if the name that generated
     *    this Binding had an explicit "super" qualifier.
     */
    public boolean isSuperQualified()
    {
        return sharedBindingInfo.isSuperQualified();
    }

    /**
     * Data structure to hold information that can be shared between many bindings.
     *
     * These are things like the local register, the name, etc.
     */
    public static class SharedBindingInfo
    {
        /**
         * The multiname for this binding - used to generate lookups by name
         */
        private final Name name;
        /**
         * The Binding's definition.
         */
        private final IDefinition definition;
        /**
         * Use register-based instructions to manipulate
         * this Binding's entity when this is set.
         */
        private boolean isLocalDef;
        /**
         * The Binding's local register number.
         * This is set at function wrap time.
         */
        private Integer localNumber;
        /**
         * The Binding's slot id.
         * This is zero by default to make it a
         * AVM assigned slot.
         */
        private int slotId = ITraitsVisitor.RUNTIME_SLOT;
        /**
         * The Binding's getlocal instruction.
         * The Binding manages a single getlocal
         * to simplify setting its immediate value,
         * i.e., the register number.
         */
        private Instruction getlocalIns = null;
        /**
         * The Binding's setlocal instruction.
         * The Binding manages a single setlocal
         * to simplify setting its immediate value,
         * i.e., the register number.
         */
        private Instruction setlocalIns = null;
        /**
         * The Binding's inclocal instruction.
         * The Binding manages a single inclocal
         * to simplify setting its immediate value,
         * i.e., the register number.
         */
        private Instruction inclocalIns = null;
        /**
         * The Binding's inclocal_i instruction.
         * The Binding manages a single inclocal_i
         * to simplify setting its immediate value,
         * i.e., the register number.
         */
        private Instruction inclocaliIns = null;
        /**
         * The Binding's declocal instruction.
         * The Binding manages a single declocal
         * to simplify setting its immediate value,
         * i.e., the register number.
         */
        private Instruction declocalIns = null;
        /**
         * The Binding's declocal_i instruction.
         * The Binding manages a single declocal_
         * to simplify setting its immediate value,
         * i.e., the register number.
         */
        private Instruction declocaliIns = null;
        /**
         * The Binding's kill instruction.
         * The Binding manages a single kill
         * to simplify setting its immediate value,
         * i.e., the register number.
         */
        private Instruction killIns = null;
        /**
         * Set when the Binding's name was
         * explicitly qualified with "super."
         */
        private boolean superQualified = false;
    
        public SharedBindingInfo (Name n, IDefinition d)
        {
            this.name = n;
            this.definition =d;
        }
    
        /**
         * Get the multiname for this binding - used to generate access by name
         *
         * @return The name for the binding
         */
        private Name getName ()
        {
            return name;
        }/*
         *  Get the definition for this binding.
         *  @return the definition associated with the binding.
         */
    
        private IDefinition getDefinition ()
        {
            return definition;
        }
    
        /**
         * @return true if this Binding has been
         *         flagged as resident in a local register.
         */
        private boolean isLocal ()
        {
            return isLocalDef;
        }
    
        /**
         * Flag this Binding as resident in a
         * local register (or unset the flag).
         *
         * @param is_local - the new value of the
         *                 "binding lives in a register" flag.
         */
        private void setIsLocal (boolean is_local)
        {
            this.isLocalDef = is_local;
        }
    
        /**
         * @return true if this Binding's local number
         *         has already been set.  Useful if a group of
         *         Bindings have subgroups that may get their
         *         local numbers set in distinct phases (e.g.,
         *         the set of Bindings in registers has a subset
         *         of parameter Bindings that have preallocated
         *         register numbers).
         */
        private boolean localNumberIsSet ()
        {
            return this.localNumber != null;
        }
    
        /**
         * @return this Binding's local register number.
         */
        private int getLocalRegister ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            assert (this.localNumber != null) : "local register number no set:" + this;
            return this.localNumber;
        }
    
        /**
         * Set this binding's local register number.
         *
         * @param local_num - the binding's local register.
         */
        private void setLocalRegister (int local_num)
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            assert (this.localNumber == null) : "local already set:" + this;
    
            this.localNumber = local_num;
    
            if (this.getlocalIns != null)
                this.getlocalIns.setImmediate(local_num);
            if (this.setlocalIns != null)
                this.setlocalIns.setImmediate(local_num);
            if (this.inclocalIns != null)
                this.inclocalIns.setImmediate(local_num);
            if (this.inclocaliIns != null)
                this.inclocaliIns.setImmediate(local_num);
            if (this.declocalIns != null)
                this.declocalIns.setImmediate(local_num);
            if (this.declocaliIns != null)
                this.declocaliIns.setImmediate(local_num);
            if (this.killIns != null)
                this.killIns.setImmediate(local_num);
        }
    
        /**
         * Fetch this Binding's getlocal instruction;
         * the Binding manages a single instruction
         * so that its register number can be assigned
         * at function wrap-up time.
         *
         * @return the Binding's getlocal instruction.
         *         The instruction will be created if not
         *         already present.
         */
        private Instruction getlocal ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            if (this.getlocalIns == null)
            {
                this.getlocalIns = InstructionFactory.getDeferredImmediateInstruction(ABCConstants.OP_getlocal);
                if (this.localNumber != null)
                    this.getlocalIns.setImmediate(this.localNumber);
            }
            return this.getlocalIns;
        }
    
        /**
         * Fetch this Binding's setlocal instruction;
         * the Binding manages a single instruction
         * so that its register number can be assigned
         * at function wrap-up time.
         *
         * @return the Binding's setlocal instruction.
         *         The instruction will be created if not
         *         already present.
         */
        private Instruction setlocal ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            if (this.setlocalIns == null)
            {
                this.setlocalIns = InstructionFactory.getDeferredImmediateInstruction(ABCConstants.OP_setlocal);
                if (this.localNumber != null)
                    this.setlocalIns.setImmediate(this.localNumber);
            }
            return this.setlocalIns;
        }
    
        /**
         * Fetch this Binding's inclocal instruction;
         * the Binding manages a single instruction
         * so that its register number can be assigned
         * at function wrap-up time.
         *
         * @return the Binding's inclocal instruction.
         *         The instruction will be created if not
         *         already present.
         */
        private Instruction inclocal ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            if (this.inclocalIns == null)
            {
                this.inclocalIns = InstructionFactory.getDeferredImmediateInstruction(ABCConstants.OP_inclocal);
                if (this.localNumber != null)
                    this.inclocalIns.setImmediate(this.localNumber);
            }
            return this.inclocalIns;
        }
    
        /**
         * Fetch this Binding's inclocal_i instruction;
         * the Binding manages a single instruction
         * so that its register number can be assigned
         * at function wrap-up time.
         *
         * @return the Binding's inclocal_i instruction.
         *         The instruction will be created if not
         *         already present.
         */
        private Instruction inclocal_i ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            if (this.inclocaliIns == null)
            {
                this.inclocaliIns = InstructionFactory.getDeferredImmediateInstruction(ABCConstants.OP_inclocal_i);
                if (this.localNumber != null)
                    this.inclocaliIns.setImmediate(this.localNumber);
            }
            return this.inclocaliIns;
        }
    
        /**
         * Fetch this Binding's declocal instruction;
         * the Binding manages a single instruction
         * so that its register number can be assigned
         * at function wrap-up time.
         *
         * @return the Binding's declocal instruction.
         *         The instruction will be created if not
         *         already present.
         */
        private Instruction declocal ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            if (this.declocalIns == null)
            {
                this.declocalIns = InstructionFactory.getDeferredImmediateInstruction(ABCConstants.OP_declocal);
                if (this.localNumber != null)
                    this.declocalIns.setImmediate(this.localNumber);
            }
            return this.declocalIns;
        }
    
        /**
         * Fetch this Binding's declocal_i instruction;
         * the Binding manages a single instruction
         * so that its register number can be assigned
         * at function wrap-up time.
         *
         * @return the Binding's declocal_i instruction.
         *         The instruction will be created if not
         *         already present.
         */
        private Instruction declocal_i ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            if (this.declocaliIns == null)
            {
                this.declocaliIns = InstructionFactory.getDeferredImmediateInstruction(ABCConstants.OP_declocal_i);
                if (this.localNumber != null)
                    this.declocaliIns.setImmediate(this.localNumber);
            }
            return this.declocaliIns;
        }

        /**
         * Get the Binding's slot id.  If this is 0, it
         * is an AVM assigned id.
         * 
         * @return slot id.
         */
        private int getSlotId()
        {
            return this.slotId;
        }

        /**
         * Set the Binding's slot id.  If this is 0, it
         * is an AVM assigned id.
         */
        private void setSlotId(int slotId)
        {
            assert (slotId != ITraitsVisitor.RUNTIME_SLOT) : "setSlotId should only be used manually allocated slot ids";
            this.slotId = slotId;
        }

        /**
         * @return true if this Binding's slot id number
         *         has been set, rather than relying
         *         on a runtime slot.
         */
        private boolean slotIdIsSet()
        {
            return this.slotId != ITraitsVisitor.RUNTIME_SLOT;
        }

        /**
         * Fetch this Binding's kill instruction;
         * the Binding manages a single instruction
         * so that its register number can be assigned
         * at function wrap-up time.
         *
         * @return the Binding's kill instruction.
         *         The instruction will be created if not
         *         already present.
         */
        private Instruction kill ()
        {
            assert (this.isLocalDef) : "not a local Binding:" + this;
            if (this.killIns == null)
            {
                this.killIns = InstructionFactory.getDeferredImmediateInstruction(ABCConstants.OP_kill);
                if (this.localNumber != null)
                    this.killIns.setImmediate(this.localNumber);
            }
            return this.killIns;
        }
    
        /**
         * Indicate that this Binding had a "super" qualifier.
         *
         * @param super_qualified - set true if the name that generated
         *                        this Binding had an explicit "super" qualifier.
         */
        private void setSuperQualified (final boolean super_qualified)
        {
            this.superQualified = super_qualified;
        }
    
        /**
         * Was this Binding explicitly qualified with "super"?
         *
         * @return true if the name that generated
         *         this Binding had an explicit "super" qualifier.
         */
        private boolean isSuperQualified ()
        {
            return this.superQualified;
        }

        @Override
        public String toString()
        {
            StringBuilder result = new StringBuilder();
            result.append("Binding { name:");
            result.append(getName() != null? getName() :"-null-");
            result.append(" def:");
            result.append(getDefinition() != null? getDefinition() :"-null-");
            result.append(" local:");
            result.append(localNumber != null? localNumber :"--");
            result.append("}");

            return result.toString();
        }
    }

}

