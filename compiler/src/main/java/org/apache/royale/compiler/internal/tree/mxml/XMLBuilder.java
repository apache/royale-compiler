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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.internal.as.codegen.InstructionListNode;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLTextData.TextType;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSingleDataBindingNode;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Helper class for MXMLXMLNode and MXMLXMLListNode to process their subtrees.
 * Will turn the children sub-tree into a String, and a list of DataBinding
 * expressions.
 */
class XMLBuilder
{
    public XMLBuilder(MXMLInstanceNode parent, IMXMLTagData rootTag, PrefixMap externalPrefixes, MXMLTreeBuilder builder)
    {
        this.parent = parent;
        this.rootTag = rootTag;
        this.externalPrefixes = externalPrefixes;

        this.builder = builder;
    }

    private MXMLInstanceNode parent;
    private MXMLTreeBuilder builder;

    private IMXMLTagData rootTag;

    /**
     * PrefixMap of the prefix'es defined outside of the contents of the XML
     * tag.
     */
    PrefixMap externalPrefixes;

    /**
     * External prefixes which are referenced from inside the XML tag, each
     * these must be added to the root tag as xmlns.
     */
    Set<String> referencedPrefixes = new HashSet<String>();

    private List<IMXMLBindingNode> databindings = new ArrayList<IMXMLBindingNode>();

    /**
     * Process an MXMLTagData - this will write a String representation of the
     * tag into the StringWriter passed in. This will strip out any databinding
     * expressions from the String, TODO: databinding - add the databinding
     * expressions as children of the MXMLXMLNode, and also record what the
     * TODO: target expressions for those are (these are the expressions to set
     * the value in the XML object when the TODO: PropertyChange event fires).
     */
    void processNode(IMXMLTagData tag,
                     StringWriter sw)
    {
        sw.write('<');
        if (tag.isCloseTag())
            sw.write('/');
        sw.write(tag.getName());
        String tagPrefix = tag.getPrefix();

        // lookup the prefix in case it's defined elsewhere in the document outside the XML tag
        if (tagPrefix != null)
            lookupPrefix(tagPrefix, tag);

        List<IMXMLTagAttributeData> attrs = getAttributes(tag);
        for (IMXMLTagAttributeData attr : attrs)
        {
            sw.write(' ');
            sw.write(attr.getName());
            sw.write('=');
            sw.write('"');
            sw.write(attr.getRawValue());
            sw.write('"');

            // lookup the prefix in case it's defined outside the XML tag
            String prefix = attr.getPrefix();
            if (prefix != null)
                lookupPrefix(prefix, tag);

        }

        StringWriter childrenSW = new StringWriter();
        for (IMXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            processNode(unit, childrenSW);
        }

        if (tag == rootTag)
        {
            // If we're the root tag, then add an xmlns for each prefix that was referenced by one of our
            // children, but was defined elsewhere in the document (like in the Application tag).
            for (String prefix : referencedPrefixes)
            {
                String uri = externalPrefixes.getNamespaceForPrefix(prefix);
                if (uri != null)
                {
                    sw.write(" xmlns");
                    if (!prefix.isEmpty())
                        sw.write(":");
                    sw.write(prefix);
                    sw.write("=\"");
                    sw.write(uri);
                    sw.write('\"');
                }
            }
        }
        if (tag.isEmptyTag())
        {
            sw.write("/>");
        }
        else
        {
            sw.write('>');
        }
        sw.write(childrenSW.toString());

        IMXMLTagData endTag = tag.findMatchingEndTag();
        if (endTag != null)
        {
            processNode(endTag, sw);
        }
    }

    /**
     * Do a lookup of a prefix, starting with the specified tag. This method
     * will record if the prefix is defined outside of the <fx:XML> tag. If it
     * is defined outside of the tag, then the prefix will need to be added to
     * the root XML tag as an 'xmlns' so that it will still be accessible.
     * 
     * @param prefix the prefix to look for
     * @param tag the tag to start looking in
     */
    void lookupPrefix(String prefix, IMXMLTagData tag)
    {
        while (tag != null)
        {
            PrefixMap pm = tag.getPrefixMap();
            if (pm != null && pm.containsPrefix(prefix))
                // we found the prefix, and haven't gone past the root tag, so don't need to do anything special
                return;
            else if (tag == rootTag)
                // don't look past the root tag
                tag = null;
            else
                tag = tag.getParentTag();
        }
        // We will only get here if we have traversed past the root XML tag, and haven't found the prefix yet
        // in that case, record the prefix if its defined somewhere else in the MXML document, so we can
        // make sure it ends up in the resulting XML literal.
        if (externalPrefixes.containsPrefix(prefix))
            referencedPrefixes.add(prefix);

    }

    /**
     * Process an IMXMLTextData - this will write a String representation of the
     * tag into the StringWriter passed in. This will strip out any databinding
     * expressions from the String. This will write out only CDATA and TEXT
     * TextDatas Other kinds of text data are not output into the resulting XML
     * object. TODO: databinding - add the databinding expressions as children
     * of the MXMLXMLNode, and also record what the TODO: target expressions for
     * those are (these are the expressions to set the value in the XML object
     * when the TODO: PropertyChange event fires).
     */
    @SuppressWarnings("incomplete-switch")
	void processNode(IMXMLTextData tag,
                     StringWriter sw)
    {
        switch (tag.getTextType())
        {

            case CDATA:
                // For CDATA, just write out the text
                sw.write(tag.getContent());
                break;
            case TEXT:
            {
                IMXMLSingleDataBindingNode db = null;
                if ((db = parseBindingExpression(tag)) != null)
                {
                    //   do databinding stuff:
                    //      1.  Walk up parent chain to compute target expression
                    //      2.  Parse databinding expression
                    //      3.  Save off both those pieces of data for use during codegen

                    databindings.add(generateBindingNode(tag, db));
                    if (!isOnlyTextChild(tag))
                    {
                        // Write out an empty CDATA section, so the tag will have the right
                        // number of text children for the binding to target.
                        sw.write("<![CDATA[]]>");
                    }
                }
                else
                {
                    sw.write(replaceBindingEscapes(tag.getContent()));
                }
            }
                break;
            // Everything else gets stripped out
        }
    }

    /**
     * Generate an MXMLBindingNode to represent the binding expression in the
     * IMXMLTextData passed in.
     * 
     * @param tag the TextData that is the destination of the binding expression
     * @param dbnode the DataBinding Node that contains the source expression
     * @return An MXMLBindingNode with expressions for the source and
     * destination
     */
    private MXMLBindingNode generateBindingNode(IMXMLTextData tag, IMXMLSingleDataBindingNode dbnode)
    {
        return generateBindingNode(tag, null, dbnode);
    }

    /**
     * Generate an MXMLBindingNode to represent the binding expression in the
     * MXMLTagAttributeData passed in.
     * 
     * @param attr the Attribute that is the destination of the binding
     * expression
     * @param dbnode the DataBinding Node that contains the source expression
     * @return An MXMLBindingNode with expressions for the source and
     * destination
     */
    private MXMLBindingNode generateBindingNode(IMXMLTagAttributeData attr, IMXMLSingleDataBindingNode dbnode)
    {
        return generateBindingNode(attr.getParent(), attr, dbnode);
    }

    /**
     * Generate the destination expression, and build the MXMLBindingNode
     * 
     * @param tag The unit data that is the target of the databinding
     * expression, if an attribute is passed in this should be the TagData
     * containing the attribute
     * @param attr The attribute that is the target of the databinding
     * expression, or null if we are not targeting an attribute
     * @param dbnode The DataBindingNode that contains the source expression
     * @return An MXMLBindingNode with the source and destination expressions
     */
    private MXMLBindingNode generateBindingNode(IMXMLUnitData tag, IMXMLTagAttributeData attr, IMXMLSingleDataBindingNode dbnode)
    {
        // Build the destination expression
        InstructionListNode destExpr = getTargetExprNode(tag, attr);

        MXMLBindingNode bindingNode = new MXMLBindingNode(parent);

        MXMLBindingAttributeNode target = new MXMLBindingAttributeNode(bindingNode, destExpr);
        target.setLocation(attr);
        target.setName(attr.getName());
        destExpr.setParent(target);
        MXMLBindingAttributeNode source = new MXMLBindingAttributeNode(bindingNode, dbnode.getExpressionNode());

        bindingNode.setDestinationAttributeNode(target);
        bindingNode.setSourceAttributeNode(source);

        return bindingNode;
    }

    /**
     * Attempt to parse a binding expression from the passed in text data
     * 
     * @param text The Text Data to parse
     * @return An IMXMLDataBindingNode that was parsed from text, or null if no
     * databinding expression was found
     */
    private IMXMLSingleDataBindingNode parseBindingExpression(IMXMLTextData text)
    {
        Object o = MXMLDataBindingParser.parse(parent, text, text.getFragments(builder.getProblems()), builder.getProblems(), 
                                                builder.getWorkspace(), builder.getMXMLDialect(),
                                                builder.getProject());
        if (o instanceof IMXMLSingleDataBindingNode)
        {
            return ((IMXMLSingleDataBindingNode)o);
        }

        return null;
    }

    /**
     * Attempt to parse a binding expression from the passed in attribute
     * 
     * @param attr The Tag Attribute Data to parse
     * @return An IMXMLDataBindingNode that was parsed from attr, or null if no
     * databinding expression was found
     */
    private IMXMLSingleDataBindingNode parseBindingExpression(IMXMLTagAttributeData attr)
    {
        Object o = MXMLDataBindingParser.parse(parent, attr, attr.getValueFragments(builder.getProblems()), builder.getProblems(), 
                                                builder.getWorkspace(), builder.getMXMLDialect(),
                                                builder.getProject());
        if (o instanceof IMXMLSingleDataBindingNode)
        {
            return ((IMXMLSingleDataBindingNode)o);
        }

        return null;
    }

    /**
     * Helper to build a InstructionListNode
     * 
     * @param data The unit data to target
     * @param attr The attr to target, or null if we aren't targeting an attr
     * @return An InstructionListNode that can be used as the destination
     * expression for an MXMLBindingNode
     */
    private InstructionListNode getTargetExprNode(IMXMLUnitData data, IMXMLTagAttributeData attr)
    {
        InstructionListNode expr = null;
        InstructionList il = getTargetInstructions(data, attr);
        if (il != null)
        {
            expr = new InstructionListNode(il);
        }

        return expr;
    }

    /**
     * Generate the instructions to set the target expression based on the Data
     * and Attribute passed in. This will walk up the parent chain to the root
     * tag, and then proceed to emit get instructions for all parents from the
     * root tag down. For the actual target, instructions to set the value will
     * be generated. If an attribute is passed in then the set instructions will
     * target the attribute instead
     * 
     * @param data The UnitData to target
     * @param attr The attribute to target, or null if there is no attribute
     * @return An InstructionList that contains the instructions to set the
     * target expression
     */
    private InstructionList getTargetInstructions(IMXMLUnitData data, IMXMLTagAttributeData attr)
    {
        IMXMLUnitData d = data;
        Stack<IMXMLUnitData> parentStack = new Stack<IMXMLUnitData>();

        if (isOnlyTextChild(d))
        {
            // If we are the only text child of a TagData, then start with the TagData,
            // as that is what we'll be setting
            d = d.getParentUnitData();
        }
        IMXMLUnitData target = d;

        // push parents onto a stack, so we can walk down from the parent later
        while (d != null)
        {
            parentStack.add(d);
            d = d == rootTag ? null : d.getParentUnitData();
        }

        InstructionList il = new InstructionList();
        // we're always going to start with "this"
        il.addInstruction(ABCConstants.OP_getlocal0);

        // Walk down the parent stack, and emit get instructions for each tag
        // except for the last one, which is the one we're targeting
        while (parentStack.size() > 1)
        {
            IMXMLUnitData unitData = parentStack.pop();
            if (unitData instanceof IMXMLTagData)
            {
                generateGetInstructions(il, (IMXMLTagData)unitData);
            }
        }

        if (target instanceof IMXMLTagData)
        {
            // Targeting a Tag
            if (attr == null)
            {
                // Just setting the tag value
                generateSetInstructions(il, (IMXMLTagData)target);
            }
            else
            {
                // We have an attr, do a get for the tag, and a set
                // for the attr
                generateGetInstructions(il, (IMXMLTagData)target);
                generateSetInstructions(il, attr);
            }
        }
        else if (target instanceof IMXMLTextData)
        {
            // We're targeting a TextData
            generateSetInstructions(il, (IMXMLTextData)target);
        }

        return il;
    }

    /**
     * Generate the instructions to place the tag on the stack. This assumes
     * that the base expr is already in the instruction list
     * <a><b><c></c></b></a> so if called on the C node above, it would assume
     * the instructions to place b on the stack were already generated - this
     * method will only compute the instructions to get c from b
     */
    private void generateGetInstructions(InstructionList il, IMXMLTagData tag)
    {
        if (tag == rootTag)
        {
            il.addInstruction(ABCConstants.OP_getproperty, getNameForTag(tag));
        }
        else
        {
        	String rootTagName = rootTag.getName();
        	if (!rootTagName.contentEquals("fx:XMLList"))
        		il.addInstruction(ABCConstants.OP_getproperty, getNameForTag(tag));
            int index = getIndexOfTag(tag);
            il.addInstruction(ABCConstants.OP_getproperty, new Name(String.valueOf(index)));
        }
    }

    /**
     * Generate the instructions to set the value of a tag. This assumes that
     * the base expr is already in the instruction list <a><b><c></c></b></a> so
     * if called on the C node above, it would assume the instructions to place
     * b on the stack were already generated - this method will only compute the
     * instructions to set c in b Assumes that the new value is stored in local
     * 1 (will use OP_getlocal1 to get it). This code will be in a function that
     * has 1 argument, which is the new value, so we know it's passed in as the
     * first local.
     */
    private void generateSetInstructions(InstructionList il, IMXMLTagData tag)
    {
        if (tag == rootTag)
        {
            il.addInstruction(ABCConstants.OP_getlocal1);
            il.addInstruction(ABCConstants.OP_setproperty, getNameForTag(tag));
        }
        else
        {
            il.addInstruction(ABCConstants.OP_getproperty, getNameForTag(tag));
            il.addInstruction(ABCConstants.OP_getlocal1);
            int index = getIndexOfTag(tag);
            il.addInstruction(ABCConstants.OP_setproperty, new Name(String.valueOf(index)));
        }
    }

    /**
     * Generate the instructions to set the value of a tag. This assumes that
     * the base expr is already in the instruction list <a><b><c
     * f="{blah}"></c></b></a> so if called on the f node above, it would assume
     * the instructions to place c on the stack were already generated - this
     * method will only compute the instructions to set f in c Assumes that the
     * new value is stored in local 1 (will use OP_getlocal1 to get it). This
     * code will be in a function that has 1 argument, which is the new value,
     * so we know it's passed in as the first local.
     */
    private void generateSetInstructions(InstructionList il, IMXMLTagAttributeData attr)
    {
        il.addInstruction(ABCConstants.OP_getlocal1);
        il.addInstruction(ABCConstants.OP_setproperty, getNameForAttr(attr));
    }

    /**
     * Generate the instructions to set the value of a text data. This assumes
     * that the base expr is already in the instruction list
     * <a><b><c>...<![CDATA[]]></![CDATA[]]></c></b></a> so if called on the
     * CDATA node above, it would assume the instructions to place c on the
     * stack were already generated - this method will only compute the
     * instructions to set the CDATA in c This will compute the index of the
     * cdata, and then emit code like: .text()[index] = value Assumes that the
     * new value is stored in local 1 (will use OP_getlocal1 to get it). This
     * code will be in a function that has 1 argument, which is the new value,
     * so we know it's passed in as the first local.
     */
    private void generateSetInstructions(InstructionList il, IMXMLTextData text)
    {
        il.addInstruction(ABCConstants.OP_callproperty, new Object[] {new Name("text"), 0});
        il.addInstruction(ABCConstants.OP_getlocal1);
        int index = getIndexOfText(text);
        il.addInstruction(ABCConstants.OP_setproperty, new Name(String.valueOf(index)));
    }

    /**
     * Get the index of a tag. Grabs the parent tag, and iterates it's children
     * to find out what the index of the tag passed in should be
     */
    private int getIndexOfTag(IMXMLTagData tag)
    {
        IMXMLTagData parent = tag.getParentTag();

        int index = 0;
        for (IMXMLTagData d = parent.getFirstChild(true); d != null; d = d.getNextSibling(true))
        {
            if (d == tag)
                break;
            else if (d.getName().equals(tag.getName()))
                ++index;

        }
        return index;
    }

    /**
     * Get the index of a text data. Grabs the parent, and iterates it's
     * children to find out what the index of the text data passed in should be
     */
    private int getIndexOfText(IMXMLTextData text)
    {
        IMXMLUnitData parent = text.getParentUnitData();

        IMXMLTagData parentTag = parent instanceof IMXMLTagData ? (IMXMLTagData)parent : null;
        int index = 0;

        if (parentTag != null)
        {
            for (IMXMLUnitData d = parentTag.getFirstChildUnit(); d != null; d = d.getNextSiblingUnit())
            {
                if (d == text)
                    break;
                else if (d instanceof IMXMLTextData && ((IMXMLTextData)d).getTextType() == TextType.CDATA)
                    ++index;
            }
        }
        return index;
    }

    /**
     * Helper to determine if a give MXMLUnitData is the only Text child of an
     * MXMLTagData This implies special, different processing from normal Text
     * Datas.
     */
    private boolean isOnlyTextChild(IMXMLUnitData child)
    {
        if (child instanceof IMXMLTextData && ((IMXMLTextData)child).getTextType() == TextType.TEXT)
        {
            IMXMLUnitData p = child.getParentUnitData();
            IMXMLTagData parent = p instanceof IMXMLTagData ? (IMXMLTagData)p : null;
            if (parent != null)
            {
                return parent.getFirstChildUnit() == child && child.getNextSiblingUnit() == null;
            }
        }
        return false;
    }

    /**
     * Generate an AET Name that corresponds to the tag passed in
     */
    private Name getNameForTag(IMXMLTagData tag)
    {
        if (tag == rootTag)
        {
            return new Name(parent.getEffectiveID());
        }
        else
        {
            String uri = tag.getURI();
            if (uri != null)
            {
                return new Name(new Namespace(ABCConstants.CONSTANT_Namespace, uri), tag.getShortName());
            }
            else
            {
                return new Name(tag.getShortName());
            }
        }
    }

    /**
     * Generate an AET Name for the attr passed in
     */
    private Name getNameForAttr(IMXMLTagAttributeData attr)
    {
        String uri = attr.getURI();
        if (uri != null)
        {
            return new Name(ABCConstants.CONSTANT_QnameA, new Nsset(new Namespace(ABCConstants.CONSTANT_Namespace, uri)), attr.getShortName());
        }
        else
        {
            return new Name(ABCConstants.CONSTANT_QnameA, new Nsset(new Namespace(ABCConstants.CONSTANT_Namespace, "")), attr.getShortName());
        }
    }

    /**
     * replace backslashes for curly braces and @ with &#7d; &#7b;
     * 
     * @param toClean the string to clean
     * @return the cleaned string
     */
    public static String replaceBindingEscapes(String toClean)
    {
        toClean = cleanupEscapedCharForXML('{', toClean);
        toClean = cleanupEscapedCharForXML('}', toClean);
        toClean = cleanupEscapedCharForXML('@', toClean);
        return toClean;
    }
    
    /**
     * Get rid of backslashes that were escaping the specified character
     * @param toClean
     * @return the cleaned string
     */
    private static String cleanupEscapedCharForXML(char escapedChar, String toClean)
    {
        //if there's no char to begin with or no escape character we can just return the orig string
        if (toClean == null || toClean.indexOf(escapedChar) == -1 || toClean.indexOf('\\') == -1)
        {
            return toClean;
        }
        StringBuilder buf = new StringBuilder(toClean.length());
        char[] chars = toClean.toCharArray();
        int i;
        for (i = 0; i < chars.length - 1; ++i)
        {
            if (chars[i] != '\\' || chars[i+1] != escapedChar)
            {
                buf.append(chars[i]);
            } else {
                buf.append("&#x" + Integer.toString((chars[i+1]), 16) + ";");
                i++;
            }
        }
        if (i == chars.length - 1) {
            buf.append(chars[chars.length - 1]);
        }
        
        return buf.toString();
    }

    /**
     * Get a list of attributes, filtering out the attributes that have data
     * binding values.
     * 
     * @param tag The
     * @return
     */
    List<IMXMLTagAttributeData> getAttributes(IMXMLTagData tag)
    {
        IMXMLTagAttributeData[] rawAttrs = tag.getAttributeDatas();
        if (rawAttrs != null)
        {
            ArrayList<IMXMLTagAttributeData> attrs = new ArrayList<IMXMLTagAttributeData>(rawAttrs.length);

            for (IMXMLTagAttributeData attr : rawAttrs)
            {
                IMXMLSingleDataBindingNode db = null;
                if ((db = parseBindingExpression(attr)) != null)
                {
                    //   do databinding stuff:
                    //      1.  Walk up parent chain to compute target expression
                    //      2.  Parse databinding expression
                    //      3.  Save off both those pieces of data for use during codegen

                    databindings.add(generateBindingNode(attr, db));
                }
                else
                {
                    attrs.add(attr);
                }
            }
            return attrs;
        }
        return Collections.emptyList();
    }

    void processNode(IMXMLUnitData node, StringWriter sw)
    {
        if (node instanceof IMXMLTagData)
            processNode((IMXMLTagData)node, sw);
        else if (node instanceof IMXMLTextData)
            processNode((IMXMLTextData)node, sw);
    }

    public List<IMXMLBindingNode> getDatabindings()
    {
        return databindings;
    }
}
