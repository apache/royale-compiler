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

/**
 *  ABCConstants contains manifest constants for the various codes and flags in an ABC file.
 */
public abstract class ABCConstants
{
    // method flags
    public static final int NEED_ARGUMENTS = 0x01;
    public static final int NEED_ACTIVATION = 0x02;
    public static final int NEED_REST = 0x04;
    public static final int HAS_OPTIONAL = 0x08;
    public static final int IGNORE_REST = 0x10;
    public static final int NATIVE = 0x20;
    public static final int SETS_DXNS = 0x40;
    public static final int HAS_PARAM_NAMES = 0x80;

    public static final int CONSTANT_Undefined = 0x00;
    public static final int CONSTANT_Utf8 = 0x01;
    public static final int CONSTANT_Int = 0x03;
    public static final int CONSTANT_UInt = 0x04;
    public static final int CONSTANT_PrivateNs = 0x05; // non-shared namespace
    public static final int CONSTANT_Double = 0x06;
    public static final int CONSTANT_Qname = 0x07; // o.ns::name, ct ns, ct name
    public static final int CONSTANT_Namespace = 0x08;
    public static final int CONSTANT_Multiname = 0x09; // o.name, ct nsset, ct name
    public static final int CONSTANT_False = 0x0A;
    public static final int CONSTANT_True = 0x0B;
    public static final int CONSTANT_Null = 0x0C;
    public static final int CONSTANT_QnameA = 0x0D; // o.@ns::name, ct ns, ct attr-name
    public static final int CONSTANT_MultinameA = 0x0E; // o.@name, ct attr-name
    public static final int CONSTANT_RTQname = 0x0F; // o.ns::name, rt ns, ct name
    public static final int CONSTANT_RTQnameA = 0x10; // o.@ns::name, rt ns, ct attr-name
    public static final int CONSTANT_RTQnameL = 0x11; // o.ns::[name], rt ns, rt name
    public static final int CONSTANT_RTQnameLA = 0x12; // o.@ns::[name], rt ns, rt attr-name
    public static final int CONSTANT_NameL = 0x13; // o.[], ns=public implied, rt name
    public static final int CONSTANT_NameLA = 0x14; // o.@[], ns=public implied, rt attr-name
    public static final int CONSTANT_NamespaceSet = 0x15;
    public static final int CONSTANT_PackageNs = 0x16;
    public static final int CONSTANT_PackageInternalNs = 0x17;
    public static final int CONSTANT_ProtectedNs = 0x18;
    public static final int CONSTANT_ExplicitNamespace = 0x19;
    public static final int CONSTANT_StaticProtectedNs = 0x1a;
    public static final int CONSTANT_MultinameL = 0x1B;
    public static final int CONSTANT_MultinameLA = 0x1C;
    public static final int CONSTANT_TypeName = 0x1D;

    public static final String[] constantKinds = {
            "0",
            "utf8",
            "",
            "int",
            "uint",
            "private",
            "double",
            "qname",
            "namespace",
            "multiname",
            "false",
            "true",
            "null",
            "@qname",
            "@multiname",
            "rtqname",
            "@rtqname",
            "[qname]",
            "@[qname]",
            "[name]",
            "@[name]",
            "nsset",
            "package",
            "internal",
            "protected",
            "explicit",
            "staticprotected",
            "[multiname]",
            "@[multiname]",
            "tyepname",
            ""
    };

    public static final int TRAIT_Var = 0x00;
    public static final int TRAIT_Method = 0x01;
    public static final int TRAIT_Getter = 0x02;
    public static final int TRAIT_Setter = 0x03;
    public static final int TRAIT_Class = 0x04;
    public static final int TRAIT_Function = 0x05;
    public static final int TRAIT_Const = 0x06;

    public static final int TRAIT_FLAG_final = 0x01;
    public static final int TRAIT_FLAG_override = 0x02;
    public static final int TRAIT_FLAG_metadata = 0x04;

    public static final String[] traitKinds =
    {
        "var", "function", "function get", "function set", "class", "function", "    final"
    };

    public static final int OP_bkpt = 0x01;
    public static final int OP_nop = 0x02;
    public static final int OP_throw = 0x03;
    public static final int OP_getsuper = 0x04;
    public static final int OP_setsuper = 0x05;
    public static final int OP_dxns = 0x06;
    public static final int OP_dxnslate = 0x07;
    public static final int OP_kill = 0x08;
    public static final int OP_label = 0x09;
    public static final int OP_ifnlt = 0x0C;
    public static final int OP_ifnle = 0x0D;
    public static final int OP_ifngt = 0x0E;
    public static final int OP_ifnge = 0x0F;
    public static final int OP_jump = 0x10;
    public static final int OP_iftrue = 0x11;
    public static final int OP_iffalse = 0x12;
    public static final int OP_ifeq = 0x13;
    public static final int OP_ifne = 0x14;
    public static final int OP_iflt = 0x15;
    public static final int OP_ifle = 0x16;
    public static final int OP_ifgt = 0x17;
    public static final int OP_ifge = 0x18;
    public static final int OP_ifstricteq = 0x19;
    public static final int OP_ifstrictne = 0x1A;
    public static final int OP_lookupswitch = 0x1B;
    public static final int OP_pushwith = 0x1C;
    public static final int OP_popscope = 0x1D;
    public static final int OP_nextname = 0x1E;
    public static final int OP_hasnext = 0x1F;
    public static final int OP_pushnull = 0x20;
    public static final int OP_pushundefined = 0x21;
    public static final int OP_nextvalue = 0x23;
    public static final int OP_pushbyte = 0x24;
    public static final int OP_pushshort = 0x25;
    public static final int OP_pushtrue = 0x26;
    public static final int OP_pushfalse = 0x27;
    public static final int OP_pushnan = 0x28;
    public static final int OP_pop = 0x29;
    public static final int OP_dup = 0x2A;
    public static final int OP_swap = 0x2B;
    public static final int OP_pushstring = 0x2C;
    public static final int OP_pushint = 0x2D;
    public static final int OP_pushuint = 0x2E;
    public static final int OP_pushdouble = 0x2F;
    public static final int OP_pushscope = 0x30;
    public static final int OP_pushnamespace = 0x31;
    public static final int OP_hasnext2 = 0x32;
    public static final int OP_li8 = 0x35;
    public static final int OP_li16 = 0x36;
    public static final int OP_li32 = 0x37;
    public static final int OP_lf32 = 0x38;
    public static final int OP_lf64 = 0x39;
    public static final int OP_si8 = 0x3A;
    public static final int OP_si16 = 0x3B;
    public static final int OP_si32 = 0x3C;
    public static final int OP_sf32 = 0x3D;
    public static final int OP_sf64 = 0x3E;
    public static final int OP_newfunction = 0x40;
    public static final int OP_call = 0x41;
    public static final int OP_construct = 0x42;
    public static final int OP_callmethod = 0x43;
    public static final int OP_callstatic = 0x44;
    public static final int OP_callsuper = 0x45;
    public static final int OP_callproperty = 0x46;
    public static final int OP_returnvoid = 0x47;
    public static final int OP_returnvalue = 0x48;
    public static final int OP_constructsuper = 0x49;
    public static final int OP_constructprop = 0x4A;
    public static final int OP_callsuperid = 0x4B;
    public static final int OP_callproplex = 0x4C;
    public static final int OP_callinterface = 0x4D;
    public static final int OP_callsupervoid = 0x4E;
    public static final int OP_callpropvoid = 0x4F;
    public static final int OP_sxi1 = 0x50;
    public static final int OP_sxi8 = 0x51;
    public static final int OP_sxi16 = 0x52;
    public static final int OP_applytype = 0x53;
    public static final int OP_newobject = 0x55;
    public static final int OP_newarray = 0x56;
    public static final int OP_newactivation = 0x57;
    public static final int OP_newclass = 0x58;
    public static final int OP_getdescendants = 0x59;
    public static final int OP_newcatch = 0x5A;
    public static final int OP_findpropstrict = 0x5D;
    public static final int OP_findproperty = 0x5E;
    public static final int OP_finddef = 0x5F;
    public static final int OP_getlex = 0x60;
    public static final int OP_setproperty = 0x61;
    public static final int OP_getlocal = 0x62;
    public static final int OP_setlocal = 0x63;
    public static final int OP_getglobalscope = 0x64;
    public static final int OP_getscopeobject = 0x65;
    public static final int OP_getproperty = 0x66;
    public static final int OP_getouterscope = 0x67;
    public static final int OP_initproperty = 0x68;
    public static final int OP_setpropertylate = 0x69;
    public static final int OP_deleteproperty = 0x6A;
    public static final int OP_deletepropertylate = 0x6B;
    public static final int OP_getslot = 0x6C;
    public static final int OP_setslot = 0x6D;
    public static final int OP_getglobalslot = 0x6E;
    public static final int OP_setglobalslot = 0x6F;
    public static final int OP_convert_s = 0x70;
    public static final int OP_esc_xelem = 0x71;
    public static final int OP_esc_xattr = 0x72;
    public static final int OP_convert_i = 0x73;
    public static final int OP_convert_u = 0x74;
    public static final int OP_convert_d = 0x75;
    public static final int OP_convert_b = 0x76;
    public static final int OP_convert_o = 0x77;
    public static final int OP_checkfilter = 0x78;
    public static final int OP_unplus = 0x7A;
    public static final int OP_coerce = 0x80;
    public static final int OP_coerce_b = 0x81;
    public static final int OP_coerce_a = 0x82;
    public static final int OP_coerce_i = 0x83;
    public static final int OP_coerce_d = 0x84;
    public static final int OP_coerce_s = 0x85;
    public static final int OP_astype = 0x86;
    public static final int OP_astypelate = 0x87;
    public static final int OP_coerce_u = 0x88;
    public static final int OP_coerce_o = 0x89;
    public static final int OP_negate = 0x90;
    public static final int OP_increment = 0x91;
    public static final int OP_inclocal = 0x92;
    public static final int OP_decrement = 0x93;
    public static final int OP_declocal = 0x94;
    public static final int OP_typeof = 0x95;
    public static final int OP_not = 0x96;
    public static final int OP_bitnot = 0x97;
    public static final int OP_add_d = 0x9B;
    public static final int OP_add = 0xA0;
    public static final int OP_subtract = 0xA1;
    public static final int OP_multiply = 0xA2;
    public static final int OP_divide = 0xA3;
    public static final int OP_modulo = 0xA4;
    public static final int OP_lshift = 0xA5;
    public static final int OP_rshift = 0xA6;
    public static final int OP_urshift = 0xA7;
    public static final int OP_bitand = 0xA8;
    public static final int OP_bitor = 0xA9;
    public static final int OP_bitxor = 0xAA;
    public static final int OP_equals = 0xAB;
    public static final int OP_strictequals = 0xAC;
    public static final int OP_lessthan = 0xAD;
    public static final int OP_lessequals = 0xAE;
    public static final int OP_greaterthan = 0xAF;
    public static final int OP_greaterequals = 0xB0;
    public static final int OP_instanceof = 0xB1;
    public static final int OP_istype = 0xB2;
    public static final int OP_istypelate = 0xB3;
    public static final int OP_in = 0xB4;
    public static final int OP_increment_i = 0xC0;
    public static final int OP_decrement_i = 0xC1;
    public static final int OP_inclocal_i = 0xC2;
    public static final int OP_declocal_i = 0xC3;
    public static final int OP_negate_i = 0xC4;
    public static final int OP_add_i = 0xC5;
    public static final int OP_subtract_i = 0xC6;
    public static final int OP_multiply_i = 0xC7;
    public static final int OP_getlocal0 = 0xD0;
    public static final int OP_getlocal1 = 0xD1;
    public static final int OP_getlocal2 = 0xD2;
    public static final int OP_getlocal3 = 0xD3;
    public static final int OP_setlocal0 = 0xD4;
    public static final int OP_setlocal1 = 0xD5;
    public static final int OP_setlocal2 = 0xD6;
    public static final int OP_setlocal3 = 0xD7;
    public static final int OP_debug = 0xEF;
    public static final int OP_debugline = 0xF0;
    public static final int OP_debugfile = 0xF1;
    public static final int OP_bkptline = 0xF2;
    public static final int OP_timestamp = 0xF3;

    public static final String[] opNames =
    {
        "OP_0x00       ",
        "bkpt          ",
        "nop           ",
        "throw         ",
        "getsuper      ",
        "setsuper      ",
        "dxns          ",
        "dxnslate      ",
        "kill          ",
        "label         ",
        "OP_0x0A       ",
        "OP_0x0B       ",
        "ifnlt         ",
        "ifnle         ",
        "ifngt         ",
        "ifnge         ",
        "jump          ",
        "iftrue        ",
        "iffalse       ",
        "ifeq          ",
        "ifne          ",
        "iflt          ",
        "ifle          ",
        "ifgt          ",
        "ifge          ",
        "ifstricteq    ",
        "ifstrictne    ",
        "lookupswitch  ",
        "pushwith      ",
        "popscope      ",
        "nextname      ",
        "hasnext       ",
        "pushnull      ",
        "pushundefined ",
        "OP_0x22       ",
        "nextvalue     ",
        "pushbyte      ",
        "pushshort     ",
        "pushtrue      ",
        "pushfalse     ",
        "pushnan       ",
        "pop           ",
        "dup           ",
        "swap          ",
        "pushstring    ",
        "pushint       ",
        "pushuint      ",
        "pushdouble    ",
        "pushscope     ",
        "pushnamespace ",
        "hasnext2      ",
        "OP_0x33       ", // lix8 (internal)
        "OP_0x34       ", // lix16 (internal)
        "li8           ",
        "li16          ",
        "li32          ",
        "lf32          ",
        "lf64          ",
        "si8           ",
        "si16          ",
        "si32          ",
        "sf32          ",
        "sf64          ",
        "OP_0x3F       ",
        "newfunction   ",
        "call          ",
        "construct     ",
        "callmethod    ",
        "callstatic    ",
        "callsuper     ",
        "callproperty  ",
        "returnvoid    ",
        "returnvalue   ",
        "constructsuper",
        "constructprop ",
        "callsuperid   ",
        "callproplex   ",
        "callinterface ",
        "callsupervoid ",
        "callpropvoid  ",
        "sxi1          ",
        "sxi8          ",
        "sxi16         ",
        "applytype     ",
        "OP_0x54    ",
        "newobject     ",
        "newarray      ",
        "newactivation ",
        "newclass      ",
        "getdescendants",
        "newcatch      ",
        "OP_0x5B       ", // findpropglobalstrict (internal)
        "OP_0x5C       ", // findpropglobal (internal)
        "findpropstrict",
        "findproperty  ",
        "finddef       ",
        "getlex        ",
        "setproperty   ",
        "getlocal      ",
        "setlocal      ",
        "getglobalscope",
        "getscopeobject",
        "getproperty   ",
        "getouterscope ",
        "initproperty  ",
        "OP_0x69       ",
        "deleteproperty",
        "OP_0x6B       ",
        "getslot       ",
        "setslot       ",
        "getglobalslot ",
        "setglobalslot ",
        "convert_s     ",
        "esc_xelem     ",
        "esc_xattr     ",
        "convert_i     ",
        "convert_u     ",
        "convert_d     ",
        "convert_b     ",
        "convert_o     ",
        "checkfilter   ",
        "OP_0x79       ",
        "unplus        ",
        "OP_0x7B       ",
        "OP_0x7C       ",
        "OP_0x7D       ",
        "OP_0x7E       ",
        "OP_0x7F       ",
        "coerce        ",
        "coerce_b      ",
        "coerce_a      ",
        "coerce_i      ",
        "coerce_d      ",
        "coerce_s      ",
        "astype        ",
        "astypelate    ",
        "coerce_u      ",
        "coerce_o      ",
        "OP_0x8A       ",
        "OP_0x8B       ",
        "OP_0x8C       ",
        "OP_0x8D       ",
        "OP_0x8E       ",
        "OP_0x8F       ",
        "negate        ",
        "increment     ",
        "inclocal      ",
        "decrement     ",
        "declocal      ",
        "typeof        ",
        "not           ",
        "bitnot        ",
        "OP_0x98       ",
        "OP_0x99       ",
        "OP_0x9A       ",
        "add_d         ",
        "OP_0x9C       ",
        "OP_0x9D       ",
        "OP_0x9E       ",
        "OP_0x9F       ",
        "add           ",
        "subtract      ",
        "multiply      ",
        "divide        ",
        "modulo        ",
        "lshift        ",
        "rshift        ",
        "urshift       ",
        "bitand        ",
        "bitor         ",
        "bitxor        ",
        "equals        ",
        "strictequals  ",
        "lessthan      ",
        "lessequals    ",
        "greaterthan   ",
        "greaterequals ",
        "instanceof    ",
        "istype        ",
        "istypelate    ",
        "in            ",
        "OP_0xB5       ",
        "OP_0xB6       ",
        "OP_0xB7       ",
        "OP_0xB8       ",
        "OP_0xB9       ",
        "OP_0xBA       ",
        "OP_0xBB       ",
        "OP_0xBC       ",
        "OP_0xBD       ",
        "OP_0xBE       ",
        "OP_0xBF       ",
        "increment_i   ",
        "decrement_i   ",
        "inclocal_i    ",
        "declocal_i    ",
        "negate_i      ",
        "add_i         ",
        "subtract_i    ",
        "multiply_i    ",
        "OP_0xC8       ",
        "OP_0xC9       ",
        "OP_0xCA       ",
        "OP_0xCB       ",
        "OP_0xCC       ",
        "OP_0xCD       ",
        "OP_0xCE       ",
        "OP_0xCF       ",
        "getlocal0     ",
        "getlocal1     ",
        "getlocal2     ",
        "getlocal3     ",
        "setlocal0     ",
        "setlocal1     ",
        "setlocal2     ",
        "setlocal3     ",
        "OP_0xD8       ",
        "OP_0xD9       ",
        "OP_0xDA       ",
        "OP_0xDB       ",
        "OP_0xDC       ",
        "OP_0xDD       ",
        "OP_0xDE       ",
        "OP_0xDF       ",
        "OP_0xE0       ",
        "OP_0xE1       ",
        "OP_0xE2       ",
        "OP_0xE3       ",
        "OP_0xE4       ",
        "OP_0xE5       ",
        "OP_0xE6       ",
        "OP_0xE7       ",
        "OP_0xE8       ",
        "OP_0xE9       ",
        "OP_0xEA       ",
        "OP_0xEB       ",
        "OP_0xEC       ",
        "OP_0xED       ",
        "OP_0xEE       ",
        "debug         ",
        "debugline     ",
        "debugfile     ",
        "bkptline      ",
        "timestamp     ",
        "OP_0xF4       ",
        "OP_0xF5       ",
        "OP_0xF6       ",
        "OP_0xF7       ",
        "OP_0xF8       ",
        "OP_0xF9       ",
        "OP_0xFA       ",
        "OP_0xFB       ",
        "OP_0xFC       ",
        "OP_0xFD       ",
        "OP_0xFE       ",
        "OP_0xFF       "
    };

    public static final int ATTR_final = 0x01; // 1=final, 0=virtual
    public static final int ATTR_override = 0x02; // 1=override, 0=new
    public static final int ATTR_metadata = 0x04; // 1=has metadata, 0=no metadata
    public static final int ATTR_public = 0x08; // 1=add public namespace

    public static final int CLASS_FLAG_sealed = 0x01;
    public static final int CLASS_FLAG_final = 0x02;
    public static final int CLASS_FLAG_interface = 0x04;
    public static final int CLASS_FLAG_protected = 0x08;

    public static final int METHOD_Arguments = 0x1;
    public static final int METHOD_Activation = 0x2;
    public static final int METHOD_Needrest = 0x4;
    public static final int METHOD_HasOptional = 0x8;
    public static final int METHOD_IgnoreRest = 0x10;
    public static final int METHOD_Native = 0x20;
    public static final int METHOD_Setsdxns = 0x40;
    public static final int METHOD_HasParamNames = 0x80;

    /**
     * The class is sealed: properties can not be dynamically added to instances
     * of the class.
     */
    public static final int CONSTANT_ClassSealed = 0x01;
    
    /**
     * The class is final: it cannot be a base class for any other class.
     */
    public static final int CONSTANT_ClassFinal = 0x02;
    
    /**
     * The class is an interface.
     */
    public static final int CONSTANT_ClassInterface = 0x04;
    
    /**
     * The class uses its protected namespace and the protectedNs field is
     * present in the interface_info.
     */
    public static final int CONSTANT_ClassProtectedNs = 0x08;

    public static final int VERSION_ABC_MAJOR_FP10 = 46;
    public static final int VERSION_ABC_MINOR_FP10 = 16;

    public static final int TRAIT_KIND_MASK = 0x0F;
    public static final int ZERO_INDEX = 0;

    // The higher 4 bits in trait kind is the flag.
    public static final int TRAIT_KIND_SHIFT = 4;

    // Traits Kind

    public static final int KIND_SLOT = 0;
    public static final int KIND_METHOD = 1;
    public static final int KIND_GETTER = 2;
    public static final int KIND_SETTER = 3;
    public static final int KIND_CLASS = 4;
    public static final int KIND_FUNCTION = 5;
    public static final int KIND_CONST = 6;

    /**
     * This Object represents the undefined constant value.
     */
    public static final Object UNDEFINED_VALUE = new Object();

    /**
     * This Object represents the null constant value.
     */
    public static final Object NULL_VALUE = new Object();

    /**
     * Constant used as the first arg to the debug op when adding debug
     * information for formals and locals
     */
    public static final int DI_LOCAL = 1;

    /**
     * Minimum API version number; API version numbers are optionally encoded
     * into the last character of a Namespace's name, modulo this number.
     */
    public static final int MIN_API_MARK = 0xE000;

    /**
     * Maximum allowed API version number, modulo MIN_API_MARK.
     */
    public static final int MAX_API_MARK = 0xF8FF;

    /**
     * (Invalid) API version number that means "no API version."
     */
    public static final int NO_API_VERSION = -1;
}
