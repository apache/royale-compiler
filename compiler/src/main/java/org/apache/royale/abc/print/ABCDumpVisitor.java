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

package org.apache.royale.abc.print;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCParser;
import org.apache.royale.abc.PoolingABCVisitor;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static org.apache.royale.abc.ABCConstants.CONSTANT_Multiname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_MultinameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_MultinameL;
import static org.apache.royale.abc.ABCConstants.CONSTANT_Namespace;
import static org.apache.royale.abc.ABCConstants.CONSTANT_PackageInternalNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_PackageNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_PrivateNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_ProtectedNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_Qname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_QnameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQname;
import static org.apache.royale.abc.ABCConstants.CONSTANT_RTQnameA;
import static org.apache.royale.abc.ABCConstants.CONSTANT_StaticProtectedNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_TypeName;
import static org.apache.royale.abc.ABCConstants.OP_debugfile;
import static org.apache.royale.abc.ABCConstants.OP_lookupswitch;
import static org.apache.royale.abc.ABCConstants.OP_pushstring;
import static org.apache.royale.abc.ABCConstants.TRAIT_Class;
import static org.apache.royale.abc.ABCConstants.TRAIT_Const;
import static org.apache.royale.abc.ABCConstants.TRAIT_Function;
import static org.apache.royale.abc.ABCConstants.TRAIT_Getter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Method;
import static org.apache.royale.abc.ABCConstants.TRAIT_Setter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Var;

/**
 * ABC Visitor implementation that can take an ABC and
 * dump out a textual representation of it.  Similar to
 * abcdump/abcdis in the tamarin project.
 * <p/>
 * To use, construct one of these and pass it ABCParser,
 * or whatever is driving the ABC events.
 */
public class ABCDumpVisitor extends PoolingABCVisitor
{
    /**
     * Constructor
     *
     * @param p The PrintWriter to write the textual represention of the ABC to
     */
    public ABCDumpVisitor (PrintWriter p, boolean sortOption)
    {
        super();
        printer = new IndentingPrinter(p, 0, 2);
        dumpedMethods = new HashSet<MethodInfo>();
        this.sortOption = sortOption;
    }

    private boolean sortOption;
    private IndentingPrinter printer;
    private Set<MethodInfo> dumpedMethods;

    /**
     * Turn a namespace, name pair into a user readable String
     */
    private String qnameToString (Namespace ns, String n)
    {
        if (ns == null)
            return n;

        if ((ns.getKind() == CONSTANT_PackageNs) && (ns.getName().length() > 0))
            return ns.getName() + "::" + n;

        String qual = nsQualifierForNamespace(ns);
        if (qual.length() > 0)
            return n;

        if (ns.getName().length() == 0)
            return n;

        return ns.getName() + "::" + n;
    }

    /**
     * Turn a namespace set into a user readable String
     */
    private String nssetToString (Nsset nsSet)
    {
        String s = "";
        for (Namespace ns : nsSet)
        {
            if (ns.getKind() != CONSTANT_PrivateNs)
                s += (ns.getName() + ", ");
            else
                s += "private, ";
        }
        return "{" + s + "}";
    }

    /**
     * Turn a Name into a user readable String
     */
    private String nameToString (Name n)
    {
        if (n == null || n.couldBeAnyType())
            return "*";

        Nsset nsset;

        switch (n.getKind())
        {
            case CONSTANT_Qname:
            case CONSTANT_QnameA:
                return qnameToString(n.getSingleQualifier(), n.getBaseName());
            case CONSTANT_Multiname:
            case CONSTANT_MultinameA:
                nsset = n.getQualifiers();
                if (nsset.length() == 1)
                    return qnameToString(nsset.iterator().next(), n.getBaseName());
                else
                    return (nssetToString(nsset) + "::") + n.getBaseName();
            case CONSTANT_RTQname:
            case CONSTANT_RTQnameA:
                return "<error> " + n.toString();
            case CONSTANT_MultinameL:
                return "<error> " + n.toString();
            case CONSTANT_TypeName:
                Name typeName = n.getTypeNameParameter();
                return nameToString(n.getTypeNameBase()) + ".<" + nameToString(typeName) + ">";
        }
        return "<error> " + n.toString();
    }

    /**
     * Get a String that can be used as the  qualifier for a given Name
     */
    private String nsQualifierForName (Name n)
    {
        Nsset nsset;
        switch (n.getKind())
        {
            case CONSTANT_Qname:
            case CONSTANT_QnameA:
                return nsQualifierForNamespace(n.getSingleQualifier());
            case CONSTANT_Multiname:
            case CONSTANT_MultinameA:
                nsset = n.getQualifiers();
                if (nsset.length() == 1)
                    return nsQualifierForNamespace(nsset.iterator().next());
                break;
            case CONSTANT_RTQname:
            case CONSTANT_RTQnameA:
                break;
            case CONSTANT_MultinameL:
                break;
            case CONSTANT_TypeName:
                break;
        }
        return "";
    }

    /**
     * Get a String representing the access modifier(public, private, etc) based on a namespace value
     */
    private String nsQualifierForNamespace (Namespace ns)
    {
        switch (ns.getKind())
        {
            case CONSTANT_PackageNs:
                return "public ";
            case CONSTANT_ProtectedNs:
            case CONSTANT_StaticProtectedNs:
                //case CONSTANT_StaticProtectedNs2:
                return "protected ";
            case CONSTANT_PackageInternalNs:
                return "internal ";
            case CONSTANT_PrivateNs:
                return "private ";
        }

        if (ns.getKind() == CONSTANT_Namespace)
        {
            if (ns.getName().equals("http://adobe.com/AS3/2006/builtin"))
                return "AS3 ";
        }
        return ns.getName() + " ";
    }

    /**
     * Escape a string for displaying better
     */
    private String stringToEscapedString (String s)
    {
        String charsToEscape = "\b\t\n\f\r\"\'\\";
        String escapeChars = "btnfr\"\'\\";
        int escapeIndex;
        char currChar;
        String result = "";
        for (int i = 0; i < s.length(); ++i)
        {
            currChar = s.charAt(i);
            escapeIndex = charsToEscape.indexOf(currChar);
            if (escapeIndex != -1)
                result += "\\" + escapeChars.charAt(escapeIndex);
            else
                result += currChar;
        }
        return result;
    }

    /**
     * Print the ABC
     */
    public void write ()
    {
        traverse();

        writeIterable(getMethodInfos(), new IWriteFunction()
        {
            public void write (Object v, int index)
            {
                writeAnonMethodInfo((MethodInfo) v, index);
            }
        }
        );
    }

    /**
     * Walk over the elements of the ABC
     * Starts with the Scripts and walks down from there
     */
    public void traverse ()
    {
        int nScripts = getScriptInfos().size();
        ScriptInfo si;
        if (sortOption)
        {
            HashMap<String, ScriptInfo> scripts = new HashMap<String, ScriptInfo>();
            for (int i = 0; i < nScripts; ++i)
            {
                si = getScriptInfos().get(i);
                Iterator<Trait> traits = si.getTraits().iterator();
                Name name = traits.hasNext() ? traits.next().getName() : null;
                String scriptName = name != null ? name.getSingleQualifier().getName() + "." + name.getBaseName() : "";
                scripts.put(scriptName, si);
            }
            ArrayList<String> nameList = new ArrayList<String>();
            nameList.addAll(scripts.keySet());
            Collections.sort(nameList);
            for (int i = 0; i < nScripts; ++i)
            {
                si = scripts.get(nameList.get(i));
                traverseScript(i, si);
            }            
        }
        else
        {
            for (int i = 0; i < nScripts; ++i)
            {
                si = getScriptInfos().get(i);
                traverseScript(i, si);
            }
        }
    }

    /**
     * Traverse a Script, and its traits
     */
    protected void traverseScript (int id, ScriptInfo scriptInfo)
    {
        printer.println("// script " + id);
        traverseScriptTraits(scriptInfo.getTraits(), scriptInfo);
        MethodInfo initMethodInfo = scriptInfo.getInit();
        traverseScriptInit(initMethodInfo, scriptInfo, id);
        printer.println("");
    }

    /**
     * Traverse the traits of a script
     */
    protected void traverseScriptTraits (Traits traits, ScriptInfo si)
    {
        for (Trait t : traits)
        {
            switch (t.getKind())
            {
                case TRAIT_Const:
                    traverseScriptConstTrait(t, si);
                    break;
                case TRAIT_Var:
                    traverseScriptSlotTrait(t, si);
                    break;
                case TRAIT_Method:
                    traverseScriptMethodTrait(t, si);
                    break;
                case TRAIT_Getter:
                    traverseScriptGetterTrait(t, si);
                    break;
                case TRAIT_Setter:
                    traverseScriptSetterTrait(t, si);
                    break;
                case TRAIT_Function:
                    traverseScriptFunctionTrait(t, si);
                    break;
                case TRAIT_Class:
                    traverseScriptClassTrait(t, si);
                    break;
            }
        }
    }

    /**
     * traverse the traits of an Instance Info
     */
    protected void traverseInstanceTraits (Traits traits)
    {
        for (Trait t : traits)
        {
            switch (t.getKind())
            {
                case TRAIT_Const:
                    traverseInstanceConstTrait(t);
                    break;
                case TRAIT_Var:
                    traverseInstanceSlotTrait(t);
                    break;
                case TRAIT_Method:
                    traverseInstanceMethodTrait(t);
                    break;
                case TRAIT_Getter:
                    traverseInstanceGetterTrait(t);
                    break;
                case TRAIT_Setter:
                    traverseInstanceSetterTrait(t);
                    break;
                case TRAIT_Function:
                    traverseInstanceFunctionTrait(t);
                    break;
            }
        }
    }

    /**
     * Traverse the traits of a Class Info
     */
    protected void traverseClassTraits (Traits traits)
    {
        for (Trait t : traits)
        {
            switch (t.getKind())
            {
                case TRAIT_Const:
                    traverseClassConstTrait(t);
                    break;
                case TRAIT_Var:
                    traverseClassSlotTrait(t);
                    break;
                case TRAIT_Method:
                    traverseClassMethodTrait(t);
                    break;
                case TRAIT_Getter:
                    traverseClassGetterTrait(t);
                    break;
                case TRAIT_Setter:
                    traverseClassSetterTrait(t);
                    break;
                case TRAIT_Function:
                    traverseClassFunctionTrait(t);
                    break;
            }
        }
    }

    /**
     * Traverse a slot trait of a script
     */
    protected void traverseScriptSlotTrait (Trait trait, ScriptInfo scriptInfo)
    {
        writeSlotTrait("var", trait, false);
    }

    /**
     * Traverse a const trait of a script
     */
    protected void traverseScriptConstTrait (Trait trait, ScriptInfo scriptInfo)
    {
        writeSlotTrait("const", trait, false);
    }

    /**
     * Traverse a method trait of a script
     */
    protected void traverseScriptMethodTrait (Trait trait, ScriptInfo scriptInfo)
    {
        writeMethodTrait("function", trait, false);
    }

    /**
     * Traverse a getter trait of a script
     */
    protected void traverseScriptGetterTrait (Trait trait, ScriptInfo scriptInfo)
    {
        writeMethodTrait("function get", trait, false);
    }

    /**
     * Traverse a setter trait of a script
     */
    protected void traverseScriptSetterTrait (Trait trait, ScriptInfo scriptInfo)
    {
        writeMethodTrait("function set", trait, false);
    }

    /**
     * Traverse a function trait of a script
     */
    protected void traverseScriptFunctionTrait (Trait trait, ScriptInfo scriptInfo)
    {
        writeMethodTrait("function", trait, false);
    }

    /**
     * Traverse a class trait of a script
     */
    protected void traverseScriptClassTrait (Trait trait, ScriptInfo scriptInfo)
    {
        ClassInfo ci = (ClassInfo) trait.getAttr(Trait.TRAIT_CLASS);
        int classIndex = getClassId(ci);
        ClassVisitor cv = getDefinedClasses().get(classIndex);
        InstanceInfo iinfo = cv.getInstanceInfo();

        traverseScriptClassTrait(classIndex, iinfo, ci, trait, scriptInfo);
    }

    /**
     * Traverse a class trait of a script
     */
    protected void traverseScriptClassTrait (int classId, InstanceInfo instanceInfo, ClassInfo classInfo, Trait trait, ScriptInfo scriptInfo)
    {
        printer.println("");

        int slotId = 0;
        if( trait.hasAttr(Trait.TRAIT_SLOT ))
            slotId = trait.getIntAttr(Trait.TRAIT_SLOT);

        printer.println("// class_id=" + classId + " slot_id=" + String.valueOf(slotId));
        String def;
        if (instanceInfo.isInterface())
        {
            def = "interface";
        }
        else
        {
            def = "class";
            if (!instanceInfo.isSealed())
                def = "dynamic " + def;
            if (instanceInfo.isFinal())
                def = "final " + def;
        }
        writeMetaData(trait);
        printer.println(nsQualifierForName(trait.getName()) + def + " " + nameToString(trait.getName()) + " extends " + nameToString(instanceInfo.superName));
        if (instanceInfo.interfaceNames.length > 0)
        {
            printer.indent();
            List<String> interfaceNames = new ArrayList<String>();
            for (Name interfaceName : instanceInfo.interfaceNames)
            {
                interfaceNames.add(nameToString(interfaceName));
            }
            printer.println(joinOn(",",interfaceNames));
            printer.unindent();
        }

        printer.println("{");
        printer.indent();

        traverseInstanceInit(instanceInfo.iInit, instanceInfo, trait, scriptInfo);
        traverseInstanceTraits(instanceInfo.traits);
        traverseClassInit(classInfo.cInit, classInfo, trait, scriptInfo);
        traverseClassTraits(classInfo.classTraits);

        printer.unindent();
        printer.println("}");

    }

    /**
     * Traverse the Script init method
     */
    protected void traverseScriptInit (MethodInfo init, ScriptInfo scriptInfo, int scriptId)
    {
        printer.println("");
        writeMethodInfo("", "script" + scriptId + "$init", "function", init, false, false, false);
    }

    /**
     * Traverse an instance init method
     */
    protected void traverseInstanceInit (MethodInfo init, InstanceInfo instanceInfo, Trait classTrait, ScriptInfo scriptInfo)
    {
        printer.println("");
        printer.println("// method_id=" + getMethodInfos().getId(instanceInfo.iInit));
        writeMethodInfo("public ", nameToString(classTrait.getName()), "function", init, false, false, false);
    }

    /**
     * Traverse a slot trait of an instance info
     */
    protected void traverseInstanceSlotTrait (Trait trait)
    {
        writeSlotTrait("var", trait, false);
    }

    /**
     * Traverse a const trait of an instance info
     */
    protected void traverseInstanceConstTrait (Trait trait)
    {
        writeSlotTrait("const", trait, false);
    }

    /**
     * Traverse a method trait of an instance info
     */
    protected void traverseInstanceMethodTrait (Trait trait)
    {
        writeMethodTrait("function", trait, false);
    }

    /**
     * Traverse a getter trait of an instance info
     */
    protected void traverseInstanceGetterTrait (Trait trait)
    {
        writeMethodTrait("function get", trait, false);
    }

    /**
     * Traverse a setter trait of an instance info
     */
    protected void traverseInstanceSetterTrait (Trait trait)
    {
        writeMethodTrait("function set", trait, false);
    }

    /**
     * Traverse a function trait of an instance info
     */
    protected void traverseInstanceFunctionTrait (Trait trait)
    {
        writeMethodTrait("function", trait, false);
    }

    /**
     * Traverse a class init method
     */
    protected void traverseClassInit (MethodInfo init, ClassInfo classInfo, Trait classTrait, ScriptInfo scriptInfo)
    {
        printer.println("");
        //printer.println("// method_id=" + classInfo.init_index)
        writeMethodInfo("public ", nameToString(classTrait.getName()) + "$", "function", init, true, false, false);
    }

    /**
     * Traverse a slot trait of a class info
     */
    protected void traverseClassSlotTrait (Trait trait)
    {
        writeSlotTrait("var", trait, true);
    }

    /**
     * Traverse a const trait of a class info
     */
    protected void traverseClassConstTrait (Trait trait)
    {
        writeSlotTrait("const", trait, true);
    }

    /**
     * Traverse a method trait of a class info
     */
    protected void traverseClassMethodTrait (Trait trait)
    {
        writeMethodTrait("function", trait, true);
    }

    /**
     * Traverse a getter trait of a class info
     */
    protected void traverseClassGetterTrait (Trait trait)
    {
        writeMethodTrait("function get", trait, true);
    }

    /**
     * Traverse a setter trait of a class info
     */
    protected void traverseClassSetterTrait (Trait trait)
    {
        writeMethodTrait("function set", trait, true);
    }

    /**
     * Traverse a function trait of a class info
     */
    protected void traverseClassFunctionTrait (Trait trait)
    {
        writeMethodTrait("function", trait, true);
    }

    /**
     * Write out the metadata for a given Trait
     */
    private void writeMetaData (Trait t)
    {
        if (!t.hasMetadata())
            return;
        for (Metadata mid : t.getMetadata())
        {
            List<String> entries = new Vector<String>();
            String[] keys = mid.getKeys();
            for (int i = 0; i < keys.length; ++i)
            {
                String key = keys[i];
                String value = mid.getValues()[i];
                if (key == null || key.length() == 0)
                    entries.add("\"" + value + "\"");
                else
                    entries.add(key + "=\"" + value + "\"");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < entries.size(); ++i)
            {
                sb.append(entries.get(i));
                if (i < entries.size() - 1)
                    sb.append(", ");
            }
            if (sortOption && mid.getName().contains("_definition_help"))
                printer.println("[" + mid.getName() + "(xxxx)]");
            else
                printer.println("[" + mid.getName() + "(" + sb.toString() + ")]");
        }
    }

    /**
     * Write out the method info for an anonymous method
     */
    private void writeAnonMethodInfo (MethodInfo mi, int id)
    {
        if (dumpedMethods.contains(mi))
            return;
        printer.println("");
        printer.println("// " + id + " " + mi.getMethodName());
        writeMethodInfo("", "", "function ", mi, false, false, false);
    }

    /**
     * Write out a slot trait
     *
     * @param kindStr  the kind of trait (var or const)
     * @param t        the trait
     * @param isStatic whether the trait is static or not
     */
    private void writeSlotTrait (String kindStr, Trait t, boolean isStatic)
    {
        printer.println("");
        String qual = nsQualifierForName(t.getName());
        String nameStr = nameToString(t.getName());

        Object value = null;
        if( t.hasAttr(Trait.SLOT_VALUE) )
            value = t.getAttr(Trait.SLOT_VALUE);

        String valueStr = "";
        if (value instanceof String)
            valueStr = " = \"" + value + "\"";
        else if (value instanceof Namespace)
            valueStr = " = " + ((Namespace) value).getName();
        else if (value == ABCConstants.NULL_VALUE)
            valueStr = " = null";
        else if (value == ABCConstants.UNDEFINED_VALUE)
            valueStr = "";
        else if (value != null)
            valueStr = " = " + value.toString();

        String staticStr = isStatic ? "static " : "";
        writeMetaData(t);
        //printer.println("// name_id=" + t.name_index + " slot_id=" + t.slot_id)
        printer.println(qual + staticStr + kindStr + " " + nameStr + ":" + nameToString((Name) t.getAttr(Trait.TRAIT_TYPE)) + valueStr);
    }

    /**
     * Generate a string for displaying a lookupswitch instruction
     *
     * @param inst       the lookupswitch instruction
     * @param mb         the method body
     * @param blockNames a map of IBasicBlock to display name of block
     * @param cfg        the control flow graph for the current method
     * @return a string to use to display the lookupswitch istruction
     */
    private String stringForLookupSwitch (Instruction inst, MethodBodyInfo mb, Map<IBasicBlock, String> blockNames, IFlowgraph cfg)
    {
        int case_size = inst.getOperandCount() - 1;

        // Last label is the default
        String defaultStr = "default: " + blockNames.get(cfg.getBlock((Label) inst.getOperand(case_size)));

        String maxCaseStr = "maxcase: " + case_size;
        List<String> result = new Vector<String>();
        result.add(defaultStr);
        result.add(maxCaseStr);
        for (int i = 0; i < case_size; ++i)
        {
            result.add(blockNames.get(cfg.getBlock((Label) inst.getOperand(i))));
        }
        return joinOn(" ", result);
    }

    /**
     * Write out a method info, and it's corresponding body
     */
    private void writeMethodInfo (String qualStr, String nameStr, String kindStr, MethodInfo methodInfo, boolean isStatic, boolean isOverride, boolean isFinal)
    {
        dumpedMethods.add(methodInfo);
        List<String> paramTypeStrings = new Vector<String>();
        for (Name paramTypeName : methodInfo.getParamTypes())
            paramTypeStrings.add(nameToString(paramTypeName));

        String staticStr = isStatic ? "static " : "";
        String overrideStr = isOverride ? "override " : "";
        String nativeStr = methodInfo.isNative() ? "native " : "";
        String finalStr = isFinal ? "final " : "";
        printer.println(qualStr + staticStr + nativeStr + finalStr + overrideStr + kindStr + " " + nameStr + "(" + joinOn(",", paramTypeStrings) + "):" + nameToString(methodInfo.getReturnType()));
        MethodBodyInfo mb = getMethodBodyForMethodInfo(methodInfo);
        if (mb != null)
        {
            printer.println("{");
            printer.indent();
            TablePrinter tablePrinter = new TablePrinter(3, 2);
            tablePrinter.addRow(new String[]{"//", "derivedName", methodInfo.getMethodName()});
            tablePrinter.addRow(new String[]{"//", "method_info", String.valueOf(getMethodInfos().getId(mb.getMethodInfo()))});
            tablePrinter.addRow(new String[]{"//", "max_stack", String.valueOf(mb.max_stack)});
            tablePrinter.addRow(new String[]{"//", "max_regs", String.valueOf(mb.max_local)});
            tablePrinter.addRow(new String[]{"//", "scope_depth", String.valueOf(mb.initial_scope)});
            tablePrinter.addRow(new String[]{"//", "max_scope", String.valueOf(mb.max_scope)});
            tablePrinter.addRow(new String[]{"//", "code_length", String.valueOf(mb.code_len)});
            //tablePrinter.addRow(["//", "code_offset", mb.code_offset]);
            tablePrinter.print(printer);
            if (mb.getTraits() != null && mb.getTraits().getTraitCount() > 0)
            {
                printer.println("activation_traits {");
                printer.indent();

                for (Trait trait : mb.getTraits())
                {
                    //var kindStr : String;
                    switch (trait.getKind())
                    {
                        case TRAIT_Var:
                            kindStr = "var";
                            break;
                        case TRAIT_Const:
                            kindStr = "const";
                            break;
                        default:
                            throw new Error("Illegal activation trait in " + methodInfo.getMethodName());
                    }
                    writeSlotTrait(kindStr, trait, false);
                }

                printer.unindent();
                printer.println("}");
            }
            IFlowgraph cfg = mb.getCfg();
            Map<IBasicBlock, String> blockNames = new HashMap<IBasicBlock, String>();
            int i = 0;
            for (IBasicBlock block : cfg.getBlocksInEntryOrder())
            {
                blockNames.put(block, "bb" + i++);
            }
            int offset = 0;
            for (IBasicBlock block : cfg.getBlocksInEntryOrder())
            {
                printer.println(blockNames.get(block));
                printer.indent();
                // TODO: preds
                //printer.println("preds=[" + block.getPredeccessor()mb.blocks[i].getPredIds(mb.blocks).join(", ") + "]");
                Collection<? extends IBasicBlock> succs = block.getSuccessors();
                List<String> succNames = new ArrayList<String>();
                for (IBasicBlock s : succs)
                    succNames.add(blockNames.get(s));

                if (!sortOption)
                    printer.println("succs=[" + joinOn(",", succNames) + "]");
                /*
                // TODO: implement this with FrameModelEncoder
                if(mb.blocks[i].state != null) {
                    printer.println("verification = " + (mb.blocks[i].state.verifyError == null ? "ok" : "failed: " + mb.blocks[i].state.verifyError));
                }
                */
                tablePrinter = new TablePrinter(4, 2);
                for (int j = 0; j < block.size(); j++)
                {
                    Instruction inst = block.get(j);
                    String constantStr = "";

                    if (inst.hasOperands() && inst.getOperand(0) instanceof Name)
                    {
                        constantStr = nameToString((Name) inst.getOperand(0));
                    }
                    else if (inst.isBranch() && inst.getOpcode() != OP_lookupswitch)
                    {
                        constantStr = blockNames.get(cfg.getBlock((Label) inst.getOperand(0)));
                    }
                    else
                    {
                        switch (inst.getOpcode())
                        {
                            case OP_debugfile:
                                if (sortOption)
                                {
                                    String fileName = (String) inst.getOperand(0);
                                    fileName = fileName.substring(fileName.indexOf(";"));
                                    fileName = fileName.replace("\\", "/");
                                    constantStr = "\"" + stringToEscapedString(fileName) + "\"";
                                }
                                else
                                    constantStr = "\"" + stringToEscapedString((String) inst.getOperand(0)) + "\"";
                                break;
                            case OP_pushstring:
                                constantStr = "\"" + stringToEscapedString((String) inst.getOperand(0)) + "\"";
                                break;
                            case OP_lookupswitch:
                                constantStr = stringForLookupSwitch(inst, mb, blockNames, cfg);
                                break;
                        }
                    }
                    tablePrinter.addRow(new String[]{offset + "    ",
                            Instruction.decodeOp(inst.getOpcode()),
                            constantStr,
                            inst.isImmediate() ? String.valueOf(inst.getImmediate()) : ""
                            // TODO : Use FrameModelEncoder to keep track
                            // TODO: of stack/local values
                            //(inst.getStackDepth() == null ? "" : "// stack: " + inst.getStackDepth()),
                            //(inst.getState() == null ? "" : "// stack["+inst.getState().stackDepth+"]: " + inst.getState().stackTypeString()),
                            //(inst.getState() == null ? "" : "// locals: " + inst.getState().localsTypeString()),
                            //inst.getVerifyError()
                    });
                    offset++;
                }
                tablePrinter.print(printer);
                printer.unindent();
            }

            printer.unindent();
            printer.println("}");
            if (mb.getExceptions().size() > 0)
            {
                tablePrinter = new TablePrinter(7, 2);
                tablePrinter.addRow(new String[]{"//", "exception", "start", "end", "target", "type string", "name string"});
                for (i = 0; i < mb.getExceptions().size(); i++)
                {
                    ExceptionInfo exception = mb.getExceptions().get(i);
                    tablePrinter.addRow(new String[]{"//", String.valueOf(i), String.valueOf(exception.getFrom().getPosition()),
                            String.valueOf(exception.getTo().getPosition()),
                            String.valueOf(exception.getTarget().getPosition()),
                            nameToString(exception.getExceptionType()),
                            nameToString(exception.getCatchVar())});
                }
                tablePrinter.print(printer);
                printer.println("");
            }
        }
    }

    /**
     * Write out a method trait
     */
    private void writeMethodTrait (String kindStr, Trait t, boolean isStatic)
    {
        String qual = nsQualifierForName(t.getName());
        String nameStr = nameToString(t.getName());
        MethodInfo methodInfo = (MethodInfo) t.getAttr(Trait.TRAIT_METHOD);
        printer.println("");
        writeMetaData(t);
        //printer.println("// name_id=" + t.name_index + " method_id=" + t.method_info + " disp_id=" + t.disp_id)
        writeMethodInfo(qual, nameStr, kindStr, methodInfo, isStatic, t.getBooleanAttr(Trait.TRAIT_OVERRIDE), t.getBooleanAttr(Trait.TRAIT_FINAL));
    }

    /**
     * Helper interface to pass around writers
     */
    interface IWriteFunction
    {
        void write (Object v, int index);
    }

    /**
     * Write an array using the given IWriteFunction
     */
    private <T> void writeIterable (Iterable<T> p, IWriteFunction writefunc)
    {
        int i = 0;
        for (Object v : p)
        {
            writefunc.write(v, i++);
        }
    }

    /**
     * Helper class to manage indenting
     */
    private static class IndentingPrinter
    {
        private PrintWriter delegate;
        private String currentIndent;
        private int indentIncrement;

        public IndentingPrinter (PrintWriter delegate, int initialIndent, int indentIncrement)
        {
            this.delegate = delegate;
            this.currentIndent = makeIndentStr(initialIndent);
            this.indentIncrement = indentIncrement;
        }

        private static String makeIndentStr (int indent)
        {
            String result;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; ++i)
                sb.append(" ");
            result = sb.toString();
            return result;
        }

        public void println (String s)
        {
            if (s.length() > 0)
                s = currentIndent + s;
            delegate.println(s);
        }

        public void indent ()
        {
            int newIndent = currentIndent.length() + indentIncrement;
            currentIndent = makeIndentStr(newIndent);
        }

        public void unindent ()
        {
            int newIndent = currentIndent.length() - indentIncrement;
            currentIndent = makeIndentStr(newIndent);
        }

        public void flush ()
        {
            delegate.flush();
        }
    }

    /**
     * Helper class to display nicely formatted tables of data
     */
    public static class TablePrinter
    {
        public TablePrinter (int nCols, int minPadding)
        {
            cols = nCols;
            this.minPadding = minPadding;
            m_rows = new Vector<Row>();
        }

        public void addRow (String[] r)
        {
            if (r.length != cols)
                throw new Error("Invalid row");
            m_rows.add(new Row(r));
        }

        public void print (IndentingPrinter p)
        {
            int[] colWidths = new int[cols];
            int i;
            for (i = 0; i < cols; ++i)
                colWidths[i] = 0;

            for (Row r : m_rows)
                r.measure(colWidths, minPadding);

            for (Row r : m_rows)
                r.print(p, colWidths);
        }

        private int cols;
        private int minPadding;
        private Vector<Row> m_rows;

        private class Row
        {
            private String[] cells;

            public Row (String[] cells)
            {
                this.cells = cells;
            }

            public void measure (int[] colWidths, int minPadding)
            {
                for (int i = 0; i < cells.length; ++i)
                    colWidths[i] = Math.max(colWidths[i], getRowItemStr(i).length() + minPadding);
            }

            public void print (IndentingPrinter p, int[] colWidths)
            {
                String rowStr = "";
                for (int i = 0; i < cells.length; ++i)
                    rowStr += padString(getRowItemStr(i), colWidths[i]);
                p.println(rowStr);
            }

            private String getRowItemStr (int i)
            {
                if (cells[i] == null)
                    return "null";

                if (i < cells.length)
                    return cells[i];

                return "error - out of range " + i;
            }

            private String padString (String s, int minLength)
            {
                while (s.length() < minLength)
                    s += " ";
                return s;
            }
        }
    }

    /**
     * Entry point for testing
     * <p/>
     * Spits out the dump to System.out
     */
    public static void main (String[] args) throws Exception
    {
        for (String arg : args)
        {
            File f = new File(arg);
            if (f.exists())
            {
                ABCParser parser = new ABCParser(new BufferedInputStream(new FileInputStream(f)));
                PoolingABCVisitor printer = new ABCDumpVisitor(new PrintWriter(System.out), false);
                parser.parseABC(printer);
            }
        }
    }

    /**
     * This implementation will dump out a text representation of the ABC to
     * the PrintWriter that was passed in on construction.
     */
    public void visitEnd ()
    {
        write();
        printer.flush();
    }

    /**
     * Stringify a collection of items.
     * @param separator the separator string.
     * @param items the items to stringify.
     * @return the items, listed out with a separator in between.
     */
    private static String joinOn(String separator, Collection<? extends Object> items)
    {
        StringBuilder result = new StringBuilder();

        for ( Object item: items )
        {
            if ( result.length() > 0 )
                result.append(separator);
            result.append(item);
        }

        return result.toString();
    }
}
