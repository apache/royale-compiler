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

package org.apache.royale.abc.semantics;

import java.util.HashMap;
import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.Name;

import static org.apache.royale.abc.ABCConstants.*;

/**
 * A representation of an individual <a href="http://learn.adobe.com/wiki/display/AVM2/4.8+Trait">trait</a>.
 */
public class Trait
{
    public static final String TRAIT_CLASS = "class_id";
    public static final String TRAIT_DISP = "disp_id";
    public static final String TRAIT_FINAL = "final";
    public static final String TRAIT_METHOD = "method_id";
    public static final String TRAIT_NAME = "name";
    public static final String TRAIT_OVERRIDE = "override";
    public static final String TRAIT_PUBLIC = "public";
    public static final String TRAIT_SLOT = "slot_id";
    public static final String TRAIT_TYPE = "type";
    public static final String SLOT_VALUE = "value";

    /**
     * Construct a Trait.
     * 
     * @param kind - the Trait's kind nibble.
     */
    public Trait(int kind, Name name)
    {
        this.kind_byte = (byte)(kind & 0x0F);
        this.name = name;
        addAttr(TRAIT_NAME, name);
    }

    /**
     * This trait's kind byte. Flags are set in the high nibble of the kind
     * byte, but only the lower nibble is stored here.
     * 
     * @see #getFullKindByte()
     */
    private byte kind_byte;

    /**
     * This trait's name. This is also denormalized into the trait's attributes
     * as TRAIT_NAME.
     */
    private final Name name;

    /**
     * Attributes of this trait. TODO: This shows its origins in abcasm, should
     * be converted to a more type-safe system and an adapter layer added to
     * abcasm to translate attribute names to their corresponding trait
     * attributes.
     */
    HashMap<String, Object> attrs = new HashMap<String, Object>();

    /**
     * Metadata attached to this trait.
     */
    private Vector<Metadata> metaData;

    /**
     * Add a trait attribute.
     * 
     * @param key - the attribute's name.
     * @param value - the attribute's value.
     * <pre> the attribute must not be present.</pre>
     */
    public void addAttr(String key, Object value)
    {
        if (attrs.containsKey(key))
        {
            throw new IllegalArgumentException("Trait attribute " + key + " cannot be specified twice.");
        }

        attrs.put(key, value);
    }

    /**
     * @return the low nibble of the kind byte (the actual trait kind).
     */
    public byte getKind()
    {
        return (byte)(kind_byte & 0x0F);
    }

    /**
     * @return the kind byte with its flags set in the high nibble.
     */
    public byte getFullKindByte()
    {
        int result = getKind();
        
        if (hasAttr(TRAIT_FINAL) && getBooleanAttr(TRAIT_FINAL))
            result = result | (ABCConstants.ATTR_final << 4);
        
        if (hasAttr(TRAIT_OVERRIDE) && getBooleanAttr(TRAIT_OVERRIDE))
            result = result | (ABCConstants.ATTR_override << 4);
        
        if (this.hasMetadata())
            result = result | (TRAIT_FLAG_metadata << 4);
        
        return (byte)result;
    }

    /**
     * @return the Trait's name.
     */
    public Name getName()
    {
        return this.name;
    }

    /**
     * Set a trait attribute.
     * 
     * @param attr_name - the attribute's name.
     * @param attr_value - the attribute's value.
     */
    public void setAttr(String attr_name, Object attr_value)
    {
        attrs.put(attr_name, attr_value);
    }

    /**
     * Determine whether an attribute is present.
     * 
     * @param attr_name - the attribute's name.
     * @return true if the attribute is present.
     */
    public boolean hasAttr(String attr_name)
    {
        return attrs.containsKey(attr_name);
    }

    /**
     * Get an attribute's value.
     * 
     * @param attr_name - the attribute's name.
     * @return the value of the specified attribute.
     * @pre the attribute must be present.
     */
    public Object getAttr(String attr_name)
    {
        verifyContains(attr_name, null);
        return attrs.get(attr_name);
    }

    /**
     * Get a integer attribute's value.
     * 
     * @param attr_name - the attribute's name.
     * @return the value of the specified attribute.
     * @pre the attribute must be present and must be of Integer type.
     */
    public int getIntAttr(String attr_name)
    {
        verifyContains(attr_name, Integer.class);
        return (Integer)attrs.get(attr_name);
    }

    /**
     * Get a Name attribute's value.
     * 
     * @param attr_name - the attribute's name.
     * @return the value of the specified attribute.
     * @pre the attribute must be present and must be of Name type.
     */
    public Name getNameAttr(String attr_name)
    {
        verifyContains(attr_name, Name.class);
        return (Name)attrs.get(attr_name);
    }

    /**
     * Get a boolean attribute's value.
     * 
     * @param attr_name - the attribute's name.
     * @return the value of the specified attribute.
     * @pre the attribute must be present and must be of Boolean type.
     */
    public boolean getBooleanAttr(String attr_name)
    {
        verifyContains(attr_name, Boolean.class);
        return (Boolean)attrs.get(attr_name);
    }

    /**
     * Ensure an attribute is present and of the specified type.
     * 
     * @param attr_name - the attribute's name.
     * @param clazz - the required class.
     */
    void verifyContains(String attr_name, Class<? extends Object> clazz)
    {
        if (!attrs.containsKey(attr_name))
            throw new IllegalArgumentException("Required attribute " + attr_name + " not found.");

        if (!(null == clazz || null == attrs.get(attr_name) || attrs.get(attr_name).getClass().equals(clazz)))
            throw new IllegalArgumentException("Attribute " + attr_name + " must be type " + clazz.getSimpleName());
        }

    /**
     * @param kind_byte the kind_byte to set
     */
    public void setKind(int kind_byte)
    {
        this.kind_byte = (byte)(kind_byte & 0x0F);
    }

    /**
     * @return true if the trait is final.
     */
    public boolean isFinal()
    {
        return ((kind_byte >> 4) & TRAIT_FLAG_final) != 0;
    }

    /**
     * @return true if the trait is an override.
     */
    public boolean isOverride()
    {
        return ((kind_byte >> 4) & TRAIT_FLAG_override) != 0;
    }

    /**
     * @return true if the trait has metadata.
     */
    public boolean hasMetadata()
    {
        //  getFullKindByte() ensures the TRAIT_metatdata flag is set.
        return this.metaData != null;
    }

    /**
     * @return true if the trait is constant.
     */
    public boolean isConst()
    {
        return getKind() == TRAIT_Const || getKind() == TRAIT_Getter;
    }

    /**
     * @return true if the trait is a class trait.
     */
    public boolean isClass()
    {
        return getKind() == TRAIT_Class;
    }

    /**
     * @return true if the trait is a method trait.
     */
    public boolean isMethod()
    {
        return getKind() == TRAIT_Method;
    }

    /**
     * @return true if the trait is a getter trait.
     */
    public boolean isGetter()
    {
        return getKind() == TRAIT_Getter;
    }

    /**
     * @return true if the trait is a setter trait.
     */
    public boolean isSetter()
    {
        return getKind() == TRAIT_Setter;
    }

    /**
     * @return true if the trait is some type of slot trait.
     */
    public boolean isSlot()
    {
        int tk = getKind();
        return tk == TRAIT_Var || tk == TRAIT_Const || tk == TRAIT_Class;
    }

    /**
     * Add a metadata entry to this trait.
     * 
     * @param md - the metadata to add.
     */
    public void addMetadata(Metadata md)
    {
        if (null == this.metaData)
            this.metaData = new Vector<Metadata>();
        
        this.metaData.add(md);
    }

    /**
     * Common entry for empty metadata.
     */
    private static final Vector<Metadata> emptyMetadata = new Vector<Metadata>();

    /**
     * @return this trait's metadata.
     */
    public final Vector<Metadata> getMetadata()
    {
        if (this.metaData != null)
            return this.metaData;
        else
            return emptyMetadata;
    }
}
