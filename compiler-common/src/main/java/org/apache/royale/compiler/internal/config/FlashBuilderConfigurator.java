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

package org.apache.royale.compiler.internal.config;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.filespecs.FileSpecification;

/**
 * A utility class, which is used to parse an XML file of configuration options
 * and populate a ConfigurationBuffer. A counterpart of CommandLineConfigurator
 * and SystemPropertyConfigurator.
 * 
 * @see <a href="http://help.adobe.com/en_US/flex/using/WS2db454920e96a9e51e63e3d11c0bf67670-7ff2.html">Configuration file syntax</a>
 */
public class FlashBuilderConfigurator
{

    public static class SAXConfigurationException extends SAXParseException
    {
        private static final long serialVersionUID = -3388781933743434302L;

        SAXConfigurationException(ConfigurationException e, Locator locator)
        {
            super(null, locator); // ?
            this.innerException = e;
        }

        public ConfigurationException innerException;
    }

    /**
     * Take the command line args which should contain the -use-flashbuilder-project-files
     * option and an FB project folder, and return a new array of args
     * based on the FB project files.
     * @param args original set of args
     * @return new array of args
     */
    public static String[] computeFlashBuilderArgs(String[] args, String suffix)
    {
        String fbFolder = args[args.length - 1];
        
        ArrayList<String> fbArgs = new ArrayList<String>();
        boolean isDebug = false;
        String sdkdir = null;
        for (String arg : args)
        {
            if (arg.equals("-debug") || arg.equals("-debug=true"))
                isDebug = true;
            if (arg.contains("+royalelib"))
                sdkdir = arg.substring(9);
        }

        ActionScriptPropertiesReader aspr = new ActionScriptPropertiesReader();
        try
        {
            aspr.read(fbArgs, fbFolder, isDebug, suffix, sdkdir);
        }
        catch (ConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RoyaleLibPropertiesReader flpr = new RoyaleLibPropertiesReader();
        if (suffix.equals(".swc"))
        {
            try
            {
                flpr.read(fbArgs, fbFolder);
            }
            catch (ConfigurationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        for (String arg : args)
        {
        	if (arg.startsWith("-output="))
        	{
        		// allow command-line -output to override project -output
        		int n = fbArgs.size();
        		for (int i = 0; i < n; i++)
        		{
        			String fbArg = fbArgs.get(i);
        			if (fbArg.startsWith("-output"))
        			{
        				fbArgs.remove(i);
        				break;
        			}
        		}
        	}
            fbArgs.add(arg);
        }
        // remove last original arg which should have been project folder
        fbArgs.remove(fbArgs.size() - 1);
        if (suffix.equals(".swc"))
        {
            //fbArgs.add(flpr.classes);
        }
        else
        {
            // add new last arg which is path to app
            fbArgs.add(aspr.applicationPath);
        }
        
        System.out.println("using FlashBuilder Project Files");
        System.out.println("FlashBuilder settings:");
        for (String arg : fbArgs)
            System.out.println("    " + arg);
        return fbArgs.toArray(new String[fbArgs.size()]);
    }

    private static class ActionScriptPropertiesReader
    {
        public String applicationPath;
        
        public void read(ArrayList<String> fbArgs, String fbFolder, boolean isDebug, String suffix, String sdkdir) throws ConfigurationException
        {
            String path = fbFolder + "/" + ".actionScriptProperties";
            final ASPropertiesHandler h = new ASPropertiesHandler(fbArgs, path, fbFolder, "actionScriptProperties", isDebug, suffix, sdkdir);
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            Reader reader = null;
            try
            {
                FileSpecification fs = new FileSpecification(path);
                reader = fs.createReader();
                final SAXParser parser = factory.newSAXParser();
                final InputSource source = new InputSource(reader);
                parser.parse(source, h);
                applicationPath = h.applicationPath;
                if (h.additionalCompilerArguments != null)
                {
                    String[] parts = h.additionalCompilerArguments.split("\\s+");
                    for (String part : parts)
                    {
                        if (part.contains("-load-config") && part.contains(".."))
                        {
                            String[] pieces = part.split("=");
                            part = pieces[0] + "=" + h.sourcePath + "/" + pieces[1];
                        }
                        fbArgs.add(part);
                    }
                }
            }
            catch (SAXConfigurationException e)
            {
                throw e.innerException;
            }
            catch (SAXParseException e)
            {
                throw new ConfigurationException.OtherThrowable(e, null, path, e.getLineNumber());
            }
            catch (Exception e)
            {
                throw new ConfigurationException.OtherThrowable(e, null, path, -1);
            }
            finally
            {
                IOUtils.closeQuietly(reader);
            }
        }
    }
    

    /**
     * SAX handler for .actionScriptProperties XML.
     */
    private static class ASPropertiesHandler extends DefaultHandler
    {
        public ASPropertiesHandler(ArrayList<String> fbArgs,
                       String source,
                       String contextPath,
                       String rootElement,
                       boolean isDebug,
                       String suffix,
                       String sdkdir)
        {
            this.isDebug = isDebug;
            this.fbArgs = fbArgs;
            this.source = source;
            this.contextPath = contextPath;
            this.rootElement = rootElement;
            this.suffix = suffix;
            this.sdkdir = sdkdir;
        }
    
        private Stack<String> contextStack = new Stack<String>();
        private ArrayList<String> fbArgs;
        private final String source;
        private final String contextPath;
        private final String rootElement;
        private final String suffix;
        private Locator locator;
        private boolean isDebug;
        private String sdkdir;
    
        public String applicationPath;
        private String outputFileName;
        private boolean inExclude;
        private String workspacePath;
        public String additionalCompilerArguments;
        public String sourcePath;
        
        @Override
        public void startElement(final String uri, final String localName, final String qname, final Attributes attributes) throws SAXException
        {
            // Verify and initialize the context stack at root element.
            if (contextStack.size() == 0)
            {
                if (!qname.equals(rootElement))
                {
                    throw new SAXConfigurationException(
                            new ConfigurationException.IncorrectElement(rootElement, qname, this.source, locator.getLineNumber()),
                            locator);
                }                
                contextStack.push(qname);
                applicationPath = attributes.getValue("mainApplicationPath");
                outputFileName = applicationPath.substring(0, applicationPath.lastIndexOf('.'));
                return;
            }
            else
            {
                if (qname.equals("compiler"))
                {
                    String extras = attributes.getValue("additionalCompilerArguments");
                    if (extras != null && extras.length() > 0)
                    {
                        additionalCompilerArguments = extras;
                    }
                    String srcPath = attributes.getValue("sourceFolderPath");
                    if (srcPath != null && srcPath.length() > 0)
                    {
                        applicationPath = contextPath + "/" + srcPath + "/" + applicationPath;
                        sourcePath = contextPath + "/" + srcPath;
                        fbArgs.add("-source-path+=" + contextPath + "/" + srcPath);
                    }
                    String isApollo = attributes.getValue("useApolloConfig");
                    if (isApollo != null && isApollo.length() > 0)
                    {
                        if (isApollo.equals("true"))
                        {
                            fbArgs.add("+configname=air");
                        }
                    }
                    String isAccessible = attributes.getValue("generateAccessible");
                    if (isAccessible != null && isAccessible.length() > 0)
                    {
                        if (isAccessible.equals("true"))
                        {
                            fbArgs.add("-compiler.accessible=true");
                        }
                    }
                    String outputFolder = attributes.getValue("outputFolderPath");
                    if (outputFolder != null && outputFolder.length() > 0)
                    {
                        // swap bin-debug for bin-release if not debug swf
                        if (!isDebug)
                            outputFolder = outputFolder.replace("debug", "release");
                            
                        String outFile = "-output=";
                        if (!outputFolder.startsWith("/"))
                            outFile += contextPath + "/";
                        fbArgs.add(outFile + outputFolder +  "/" + outputFileName + suffix);
                    }
                }
                else if (qname.equals("sourcePathEntry"))
                {
                    String kind = attributes.getValue("kind");
                    if (kind.equals("1"))
                    {
                        String path = attributes.getValue("path");
                        fbArgs.add("-source-path+=" + contextPath + "/" + path);
                    }
                }
                else if (qname.equals("libraryPathEntry"))
                {
                    if (!inExclude)
                    {
                        String kind = attributes.getValue("kind");
                        String linkType = attributes.getValue("linkType");
                        String path = attributes.getValue("path");
                        if (!kind.equals("4")) // not an sdk reference
                        {
                            if (path.contains("${DOCUMENTS}"))
                            {
                                if (workspacePath == null)
                                {
                                    workspacePath = source;
                                    File workspaceFile = new File(source);
                                    workspacePath = workspaceFile.getParent();
                                    while (workspacePath != null && workspacePath.length() > 0)
                                    {
                                        workspaceFile = new File(workspacePath + "/.metadata");
                                        if (workspaceFile.exists())
                                            break;
                                        workspacePath = workspaceFile.getParentFile().getParent();
                                    }
                                }
                                path = path.replace("${DOCUMENTS}", workspacePath);
                                fbArgs.add(linkType.equals("2") ? "-external-library-path+=" + path : "-library-path+=" + path);
                            }
                            else if (path.contains("${PROJECT_FRAMEWORKS}"))
                            {
                                path = path.replace("${PROJECT_FRAMEWORKS}", sdkdir);
                                fbArgs.add(linkType.equals("2") ? "-external-library-path+=" + path : "-library-path+=" + path);
                            }
                            else
                            {
                                if (path.startsWith("/"))
                                {
                                    File f = new File(contextPath);
                                    // FB puts a "/" in front of references to other projects
                                    String parentPath = f.getParent();
                                    f = new File(parentPath + path);
                                    if (f.exists())
                                        fbArgs.add(linkType.equals("2") ? "-external-library-path+=" + parentPath + path : "-library-path+=" + parentPath + path);
                                    else
                                        fbArgs.add(linkType.equals("2") ? "-external-library-path+=" + path : "-library-path+=" + path);

                                }
                                else
                                    fbArgs.add(linkType.equals("2") ? "-external-library-path+=" + contextPath + "/" + path : "-library-path+=" + contextPath + "/" + path);
                            }
                        }
                    }
                }
                else if (qname.equals("excludedEntries"))
                    inExclude = true;
            }
            
        }
    
        @Override
        public void endElement(String uri, String localName, String qname) throws SAXException
        {
            if (qname.equals("excludedEntries"))
                inExclude = false;
        }
    
        @Override
        public void characters(char ch[], int start, int length)
        {
        }
    
        @Override
        public void setDocumentLocator(Locator locator)
        {
            this.locator = locator;
        }
    }

    private static class RoyaleLibPropertiesReader
    {        
        public void read(ArrayList<String> fbArgs, String fbFolder) throws ConfigurationException
        {
            String path = fbFolder + "/" + ".royaleLibProperties";
            final RoyaleLibPropertiesHandler h = new RoyaleLibPropertiesHandler(fbArgs, path, fbFolder, "royaleLibProperties");
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            Reader reader = null;
            try
            {
                FileSpecification fs = new FileSpecification(path);
                reader = fs.createReader();
                final SAXParser parser = factory.newSAXParser();
                final InputSource source = new InputSource(reader);
                parser.parse(source, h);
                if (h.allClasses)
                {
                    // may need to find and list all classes
                    fbArgs.add("-include-sources=" + fbFolder + "/src");
                }
                else
                {
                    StringBuilder classList = new StringBuilder();
                    boolean first = true;
                    for (String cname : h.includedClasses)
                    {
                        if (!first)
                            classList.append(", ");
                        first = false;
                        classList.append(cname);
                    }
                    fbArgs.add("-include-classes=" + classList.toString());
                }
            }
            catch (SAXConfigurationException e)
            {
                throw e.innerException;
            }
            catch (SAXParseException e)
            {
                throw new ConfigurationException.OtherThrowable(e, null, path, e.getLineNumber());
            }
            catch (Exception e)
            {
                throw new ConfigurationException.OtherThrowable(e, null, path, -1);
            }
            finally
            {
                IOUtils.closeQuietly(reader);
            }
        }
    }
    


    /**
     * SAX handler for .royaleLibProperties XML.
     */
    private static class RoyaleLibPropertiesHandler extends DefaultHandler
    {
        public RoyaleLibPropertiesHandler(ArrayList<String> fbArgs,
                       String source,
                       String contextPath,
                       String rootElement)
        {
            this.fbArgs = fbArgs;
            this.source = source;
            this.contextPath = contextPath;
            this.rootElement = rootElement;
        }
    
        private Stack<String> contextStack = new Stack<String>();
        private ArrayList<String> fbArgs;
        private final String source;
        private final String contextPath;
        private final String rootElement;
        private Locator locator;
        public boolean allClasses;
    
        public ArrayList<String> includedClasses = new ArrayList<String>();
        
        @Override
        public void startElement(final String uri, final String localName, final String qname, final Attributes attributes) throws SAXException
        {
            // Verify and initialize the context stack at root element.
            if (contextStack.size() == 0)
            {
                if (!qname.equals(rootElement))
                {
                    throw new SAXConfigurationException(
                            new ConfigurationException.IncorrectElement(rootElement, qname, this.source, locator.getLineNumber()),
                            locator);
                }                
                String all = attributes.getValue("includeAllClasses");
                if ("true".equals(all))
                    allClasses = true;
                contextStack.push(qname);
                return;
            }
            else
            {
                if (qname.equals("classEntry"))
                {
                    String path = attributes.getValue("path");
                    includedClasses.add(path);
                }
                else if (qname.equals("namespaceManifestEntry"))
                {
                    String manifest = attributes.getValue("manifest");
                    String namespace = attributes.getValue("namespace");
                    fbArgs.add("-namespace");
                    fbArgs.add(namespace);
                    String mf = contextPath + "/" + manifest;
                    File f = new File(mf);
                    if (!f.exists())
                    {
                        mf = contextPath + "/src/" + manifest;
                    }
                    fbArgs.add(mf);
                    fbArgs.add("-include-namespaces");
                    fbArgs.add(namespace);                    
                }
            }
        }
    
        @Override
        public void endElement(String uri, String localName, String qname) throws SAXException
        {
        }
    
        @Override
        public void characters(char ch[], int start, int length)
        {
        }
    
        @Override
        public void setDocumentLocator(Locator locator)
        {
            this.locator = locator;
        }
    }
}
