/*
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

package org.apache.royale.compiler.tools.unknowntreehandler;

import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ext.DefaultHandler2;

/**
 *  UnknownTreePatternInputOutput is a support class that reads
 *  an XML file with template data and populates a map of finding templates,
 *  and may also write this map out as Java code to be copied into
 *  the UnknownTreeHandler as hard-coded patterns.
 */
class UnknownTreePatternInputOutput extends DefaultHandler2
{
    /**
     *  The map of templates by node ID to read into.
     */
    Map<String, ArrayList<Template>> destination;

    /**
     *  Package name to use.  Hard-coded for now.
     */
    String packageName = "org.apache.royale.compiler.internal.as.codegen";

    /**
     *  Class name to use.  Also hard-coded.
     */
    String className = "UnknownTreeHandlerPatterns";

    /**
     *  Emitter to use.  Hard-coded.
     */
    String emitterName = "org.apache.royale.compiler.internal.as.codegen.CmcEmitter";

    /**
     *  Load a map of templates from an XML file.
     *  @param pattern_file - the path of the XML pattern file.
     *  @param dest - the destination map.
     */
    boolean load(String pattern_file, Map<String, ArrayList<Template>> dest)
    {
        this.destination = dest;

        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();
            parser.parse( new java.io.FileInputStream(pattern_file), this);
            return true;
        }
        catch ( Throwable load_failed )
        {
            System.err.println("Load of " + pattern_file + " failed!");
            load_failed.printStackTrace();
            return false;
        }
    }

    /**
     *  This stack tracks UnknownTreeFindingTemplate objects from 
     *  their startElement event to their endElement event; 
     *  it's used to create the pattern/subpattern hierarchy.
     */
    Stack<Template> nodeStack = new Stack<Template>();

    /**
     *  Create new UnknownTreeFindingTemplate objects in response to Pattern
     *  elements, and decode the Pattern's attributes.
     */
    @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
        {   
            if ( ! (localName.equals("Pattern")) )
            {
                if ( ! (localName.equals("SEW")) )
                    System.err.println("Unknown element " + localName);
                return;
            }

            Template template = new Template();

            for ( int index = 0; index < attributes.getLength(); index++)
            {
                String attr_name = attributes.getLocalName(index);
                String attr_value = attributes.getValue(index);

                if ( "ID".equals(attr_name) )
                {
                    template.id = attr_value;
                }
                else if ( "cantHaveState".equals(attr_name) )
                {
                    String state_id = "__" + attr_value + "_NT";
                    template.cantHaveState = state_id;
                }
                else if ( "mustHaveState".equals(attr_name) )
                {
                    String state_id = "__" + attr_value + "_NT";
                    template.mustHaveState = state_id;
                }
                else if ( "problem".equals(attr_name) )
                {
                    String class_name;
                    if ( attr_value.startsWith("org.apache.") )
                        class_name = attr_value;
                    else
                        class_name = "org.apache.royale.compiler.problems." + attr_value;
                    template.problemClass = class_name;
                }
                else if ( "nodeClass".equals(attr_name) )
                {
                    String class_name;
                    if ( attr_value.startsWith("org.apache.") )
                        class_name = attr_value;
                    else
                        class_name = "org.apache.royale.compiler.internal.tree.as." + attr_value;
                    template.nodeClass = class_name;
                }
                else if ( "provisional".equals(attr_name) )
                {
                    template.provisional = Boolean.valueOf(attr_value);
                }
                else
                {
                    System.err.println("** Unknown attr name:" + attr_name);
                }
            }


            //  Top-level templates go into the map by node id;
            //  subpatterns get linked to their immediate parent.
            if ( nodeStack.isEmpty() )
            {
                assert template.problemClass != null : "Top-level template " + template + " must have a problem class.";

                if ( ! (destination.containsKey(template.id)) )
                    destination.put(template.id, new ArrayList<Template>());

                destination.get(template.id).add(template);
            }
            else
            {
                Template base = nodeStack.peek();
                if ( base.requiredSubtree == null )
                    base.requiredSubtree = template;
                else
                    System.err.println("already has subtree: " + base);
            }

            this.nodeStack.push(template);
        }

    /**
     *  Maintain the UnknownTreeFindingTemplate stack.
     */
    @Override
    public void endElement(String uri,  String localName,  String qName)
    {
        if ( localName.equals("Pattern") )
            this.nodeStack.pop();
    }

    /**
     *  Load an XML file containing patterns and dump equivalent Java code to System.out.
     */
    public static void main(String[] argv)
    throws Exception
    {
        if ( argv.length < 2 )
        {
            System.err.println("Usage: java org.apache.royale.compiler.tools.unknowntreehandler.UnknownTreePatternInputOutput <xml pattern file> <destination java file>");
            System.exit(1);
        }

        new UnknownTreePatternInputOutput().dumpTemplateData(argv[0], argv[1]);
    }
    
    PrintWriter output;

    /**
     *  Read an XML file of patterns and dump the equivalent Java code to the target file.
     *  @param src_file_name - the path of the XML file.
     *  @param dest_file_name - the path of the output Java file.
     */
    void dumpTemplateData(String src_file_name, String dest_file_name)
    throws Exception
    {
        if ( !load(src_file_name, new HashMap<String, ArrayList<Template>>()) )
            return;

        output = new PrintWriter(dest_file_name);

        output.println("package " + this.packageName + ";");
        output.println("import java.util.ArrayList;");
        output.println("import java.util.HashMap;");
        output.println("import java.util.Map;");
        output.println("import org.apache.royale.compiler.tree.ASTNodeID;");
        output.println("import static org.apache.royale.compiler.tree.ASTNodeID.*;");
        output.println("import " + this.emitterName + ";");
        output.println();
        output.println("public class " + this.className);
        output.println("{");
        output.println();
        String src = src_file_name.replaceAll("\\\\", "/");
        int c = src.indexOf("compiler/src");
        src = src.substring(c);
        output.println("    //  Patterns generated from " + src);
        output.println("    public static Map<ASTNodeID, ArrayList<UnknownTreeFinding.Template> > allTemplates = new HashMap<ASTNodeID, ArrayList<UnknownTreeFinding.Template>>();");

        output.println("    static");
        output.println("    {");

        for ( String id: destination.keySet() )
        {
            String templates_name = "templates_for_" + id;
            output.printf("        ArrayList<UnknownTreeFinding.Template> %s = allTemplates.get(%s);%n", templates_name, id);
            output.printf("        if ( %s == null ) {%n", templates_name);
            output.printf("            %s = new ArrayList<UnknownTreeFinding.Template>();%n", templates_name);
            output.printf("            allTemplates.put(%s, %s);%n", id, templates_name);
            output.printf("        }%n");

            for ( Template templ: destination.get(id) )
            {
                output.printf("        {%n");
                dumpTemplate(templ, "current_template");
                output.printf("            %s.add(current_template);%n", templates_name);
                output.printf("        }%n");
            }
        }
        output.println("    }");
        output.println("}");
        output.close();
    }

    /**
     *  Recursively dump a template.
     *  @param templ - the template to dump.
     *  @param var_name - the name by which this template will be known
     *    in the output Java code.  As this routine recurses, it builds
     *    successively longer variable names.
     */
    void dumpTemplate(Template templ, String var_name)
    {
        output.printf("            UnknownTreeFinding.Template %s = new UnknownTreeFinding.Template();%n", var_name);
        output.printf("            %s.id = %s;%n", var_name, templ.id);
        if ( templ.problemClass != null )
            output.printf("            %s.problemClass = %s.class;%n", var_name, templ.problemClass);
        if ( templ.nodeClass != null )
            output.printf("            %s.nodeClass = %s.class;%n", var_name, templ.nodeClass);
        if ( templ.mustHaveState != null )
            output.printf("            %s.mustHaveState = CmcEmitter.%s;%n", var_name, templ.mustHaveState);
        if ( templ.cantHaveState != null )
            output.printf("            %s.cantHaveState = CmcEmitter.%s;%n", var_name, templ.cantHaveState);
        output.printf("            %s.provisional = %s;%n", var_name, templ.provisional);

        if ( templ.requiredSubtree != null )
        {
            String subtemp_name = var_name + "_subtempl";
            dumpTemplate(templ.requiredSubtree, subtemp_name);
            output.printf("            %s.requiredSubtree = %s;%n", var_name, subtemp_name);
        }
    }

    /**
     *  Build-time representation of a UnknownTreeFinding.Template object.
     */
    private static class Template
    {
        String id = "UnknownID";

        String problemClass;
        String nodeClass;
        String mustHaveState;
        String cantHaveState;
        Boolean provisional = Boolean.FALSE;

        Template requiredSubtree;
    }
}
