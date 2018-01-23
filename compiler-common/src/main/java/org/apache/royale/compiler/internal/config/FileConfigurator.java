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

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.royale.compiler.config.ConfigurationBuffer;
import org.apache.royale.compiler.config.ConfigurationInfo;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import com.google.common.collect.ImmutableSet;

/**
 * A utility class, which is used to parse an XML file of configuration options
 * and populate a ConfigurationBuffer. A counterpart of CommandLineConfigurator
 * and SystemPropertyConfigurator.
 * 
 * @see <a href="http://help.adobe.com/en_US/flex/using/WS2db454920e96a9e51e63e3d11c0bf67670-7ff2.html">Configuration file syntax</a>
 */
public class FileConfigurator
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
     * Load configuration XML file into a {@link ConfigurationBuffer} object.
     * 
     * @param buffer result {@link ConfigurationBuffer} object.
     * @param fileSpec configuration XML file.
     * @param context path context used for resolving relative paths in the
     * configuration options.
     * @param rootElement expected root element of the XML DOM tree.
     * @param ignoreUnknownItems if false, unknown option will cause exception.
     * @throws ConfigurationException error.
     */
    public static void load(
            final ConfigurationBuffer buffer,
            final IFileSpecification fileSpec,
            final String context,
            final String rootElement,
            boolean ignoreUnknownItems)
            throws ConfigurationException
    {
        final String path = fileSpec.getPath();
        final Handler h = new Handler(buffer, path, context, rootElement, ignoreUnknownItems);
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        Reader reader = null;
        try
        {
            reader = fileSpec.createReader();
            final SAXParser parser = factory.newSAXParser();
            final InputSource source = new InputSource(reader);
            parser.parse(source, h);
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

    /**
     * SAX handler for configuration XML.
     */
    private static class Handler extends DefaultHandler
    {
        private static final String ATTRIBUTE_APPEND = "append";

        public Handler(ConfigurationBuffer buffer,
                       String source,
                       String contextPath,
                       String rootElement,
                       boolean ignoreUnknownItems)
        {
            this.cfgbuf = buffer;
            this.source = source;
            this.contextPath = contextPath;
            this.rootElement = rootElement;
            this.ignoreUnknownItems = ignoreUnknownItems;
        }

        private final Stack<ParseContext> contextStack = new Stack<ParseContext>();
        private final ConfigurationBuffer cfgbuf;
        private final String source;
        private final String contextPath;
        private final String rootElement;
        private final boolean ignoreUnknownItems;
        private final StringBuilder text = new StringBuilder();
        private Locator locator;

        @Override
        public void startElement(final String uri, final String localName, final String qname, final Attributes attributes) throws SAXException
        {
            // Verify and initialize the context stack at root element.
            if (contextStack.size() == 0)
            {
                if (!qname.equals(rootElement))
                {
                    if (!qname.equals("flex-config"))
                    {
                        throw new SAXConfigurationException(
                            new ConfigurationException.IncorrectElement(rootElement, qname, this.source, locator.getLineNumber()),
                            locator);
                    }
                }
                final ParseContext ctx = new ParseContext();
                contextStack.push(ctx);
                return;
            }

            final ParseContext ctx = contextStack.peek();

            if (ctx.ignore)
            {
                // ignore starting new elements
                return;
            }

            if (text.length() > 0)
            {
                // Only leave nodes can have CDATA as option values.
                throw new SAXConfigurationException(
                        new ConfigurationException.UnexpectedCDATA(this.source, locator.getLineNumber()),
                        locator);
            }

            final String fullname = ConfigurationBuffer.varname(qname, ctx.base);

            if (ctx.item != null)
            {
                throw new SAXConfigurationException(
                        new ConfigurationException.UnexpectedElement(qname, contextPath, locator.getLineNumber()),
                        locator);
            }
            else if (ctx.var != null)
            {
                // we're setting values for a variable

                if (ctx.varArgCount == 1)
                {
                    // oops, we weren't expecting more than one value!

                    throw new SAXConfigurationException(
                            new ConfigurationException.UnexpectedElement(qname, source, locator.getLineNumber()),
                            locator);
                }
                ctx.item = qname;
            }
            else if (cfgbuf.isValidVar(fullname))
            {
                ctx.var = fullname;
                ctx.varArgCount = cfgbuf.getVarArgCount(ctx.var);
                ctx.append = false;
                final String append = attributes.getValue(ATTRIBUTE_APPEND);
                if (append != null)
                {
                    if (append.equalsIgnoreCase("true") || append.equalsIgnoreCase("false"))
                        ctx.append = Boolean.valueOf(append).booleanValue();
                    else
                        throw new SAXConfigurationException(
                                new ConfigurationException.BadAppendValue(
                                        ctx.var,
                                        source,
                                        locator.getLineNumber()),
                                locator);
                }
            }
            else if (isSubTree(fullname))
            {
                final ParseContext newctx = new ParseContext();
                newctx.base = fullname;
                contextStack.push(newctx);
            }
            else
            {
                if (ignoreUnknownItems)
                {
                    // push a new context and ignore everything until we get the end 
                    // of this element.
                    ParseContext newctx = new ParseContext();
                    newctx.item = qname;
                    newctx.ignore = true;
                    contextStack.push(newctx);
                    return;
                }
                System.err.println("Unknown tag:" + fullname);
                throw new SAXConfigurationException(
                        new ConfigurationException.UnknownVariable(
                                                    fullname, source, locator.getLineNumber()),
                        locator);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qname) throws SAXException
        {
            final ParseContext ctx = contextStack.peek();

            if (ctx.ignore)
            {
                // if found the matching end element, then pop the context and stop ignoring input
                if (ctx.item.equals(qname))
                {
                    contextStack.pop();
                    text.setLength(0); // ignore any text read
                }

                return;
            }

            // There are four possible states here;
            // 1. localname==rootElement -> end of file, pop, we're done
            // 2. localname==itemElement -> finished gathering text, push onto arglist
            // 2. var is set -> set the var to the argList, pop
            // 3. var is null -> we're finishing a child config, pop

            if (qname.equals(rootElement))
            {
                // Finished with the file!
            }
            else if (ctx.item != null)
            {
                // Finished with the current item.
                final ParseValue v = new ParseValue();
                v.name = qname;
                v.value = text.toString();
                v.line = locator.getLineNumber();
                ctx.argList.add(v);
                text.setLength(0);
                ctx.item = null;
            }
            else if (ctx.var != null)
            {
                if ((ctx.varArgCount > 1) && (ctx.argList.size() == 0))
                {
                    throw new SAXConfigurationException(
                            new ConfigurationException.IncorrectArgumentCount(ctx.varArgCount, 0,
                                                                               ctx.var, source, locator.getLineNumber()),
                            locator);
                }
                if (ctx.varArgCount == 1)
                {
                    ParseValue v = new ParseValue();
                    v.name = null;
                    v.value = text.toString();
                    v.line = locator.getLineNumber();
                    ctx.argList.add(v);
                    text.setLength(0);
                }
                else
                {
                    if (text.length() > 0)
                    {
                        // "unexpected CDATA encountered, " + ctx.var + " requires named arguments.", locator );
                        throw new SAXConfigurationException(
                                new ConfigurationException.UnexpectedCDATA(source, locator.getLineNumber()),
                                locator);

                    }
                }
                // Finished with the current var, save the current list
                try
                {
                    setVar(ctx.var, ctx.argList, locator.getLineNumber(), ctx.append);
                    ctx.var = null;
                    ctx.argList.clear();
                    ctx.item = null;
                    ctx.append = false;
                }
                catch (ConfigurationException e)
                {
                    throw new SAXConfigurationException(e, locator);
                }
            }
            else
            {
                // done with a child config
                contextStack.pop();
            }
        }

        public void setVar(String var, List<ParseValue> argList, int line, boolean append) throws ConfigurationException
        {
            int varArgCount = cfgbuf.getVarArgCount(var);

            Map<String, String> items = new HashMap<String, String>();

            boolean byName = (varArgCount > 1);

            if (byName)
            {
                for (Iterator<ParseValue> it = argList.iterator(); it.hasNext();)
                {
                    ParseValue v = it.next();

                    if (items.containsKey(v.name))
                    {
                        byName = false; // can't support byName, duplicate item name!
                        break;
                    }
                    else
                    {
                        items.put(v.name, v.value);
                    }
                }
            }
            List<String> args = new LinkedList<String>();

            if (byName)
            {
                int argc = 0;

                while (args.size() < items.size())
                {
                    String name = cfgbuf.getVarArgName(var, argc++);
                    String val = items.get(name);
                    if (val == null)
                    {
                        throw new ConfigurationException.MissingArgument(name, var, source, line);
                    }
                    args.add(val);
                }
            }
            else
            {
                Iterator<ParseValue> it = argList.iterator();
                int argc = 0;
                while (it.hasNext())
                {
                    ParseValue v = it.next();
                    String name = cfgbuf.getVarArgName(var, argc++);
                    if ((v.name != null) && !name.equals(v.name))
                    {
                        throw new ConfigurationException.UnexpectedArgument(name, v.name, var, source, v.line);
                    }
                    args.add(v.value);
                }
            }
            cfgbuf.setVar(var, args, source, line, contextPath, append);
        }

        @Override
        public void characters(char ch[], int start, int length)
        {
            String chars = new String(ch, start, length).trim();
            text.append(chars);
        }

        @Override
        public void setDocumentLocator(Locator locator)
        {
            this.locator = locator;
        }
    }

    private static class ParseContext
    {
        ParseContext()
        {
            this.base = null;
            this.var = null;
            this.varArgCount = -2;
            this.argList = new LinkedList<ParseValue>();
            this.append = false;
            this.ignore = false;
        }

        public String var;
        public String base;
        public String item;
        public int varArgCount;
        public boolean append;
        public List<ParseValue> argList;
        public boolean ignore; // ignore this variable, do not put in config buffer
    }

    private static class ParseValue
    {
        public String name;
        public String value;
        public int line;
    }

    private static class FormatNode
    {
        public String fullname;
        public String shortname;
        public ConfigurationInfo info;
        public List<ConfigurationValue> values;

        public TreeMap<String, FormatNode> children; // only for configs
    }

    static final String pad = "   ";

    /**
     * These XML nodes can have subtrees of configurations.
     */
    protected static final ImmutableSet<String> VALID_SUBTREE_TAG = ImmutableSet.of(
            "compiler",
            "compiler.namespaces",
            "compiler.js-namespaces",
            "compiler.fonts",
            "compiler.fonts.languages",
            "compiler.mxml",
            "compiler.mxml.imports",
            "metadata",
            "licenses",
            "frames",
            "runtime-shared-library-settings");

    /**
     * @param fullname
     * @return
     */
    private static boolean isSubTree(String fullname)
    {
        return VALID_SUBTREE_TAG.contains(fullname);
    }

    private static String classToArgName(Class<?> c)
    {
        // we only support builtin classnames!

        String className = c.getName();
        if (className.startsWith("java.lang."))
            className = className.substring("java.lang.".length());

        return className.toLowerCase();
    }

    private static String formatBuffer1(ConfigurationBuffer cfgbuf,
                                         FormatNode node,
                                         String indent,
                                         LocalizationManager lmgr,
                                         String prefix)
    {
        StringBuilder buf = new StringBuilder(1024);

        buf.append(indent + "<" + node.shortname + ">\n");
        if (node.children != null)
        {
            for (final String key : node.children.keySet())
            {
                final FormatNode child = node.children.get(key);

                if (child.children != null) // its a config
                {
                    buf.append(formatBuffer1(cfgbuf, child, indent + pad, lmgr, prefix));
                }
                else
                {
                    String description = lmgr.getLocalizedTextString(prefix + "." + child.fullname);

                    if (description != null)
                        buf.append(indent + pad + "<!-- " + child.fullname + ": " + description + "-->\n");

                    if ((child.values == null) || !child.info.isDisplayed())
                    {
                        boolean newline = false;
                        buf.append(indent + pad + "<!-- " + child.fullname + " usage:\n");
                        buf.append(indent + pad + "<" + child.shortname + ">");

                        int i = 0;
                        while (true)
                        {
                            if (child.info.getArgCount() == 1)
                            {
                                buf.append(child.info.getArgName(i));
                                break;
                            }
                            else
                            {
                                buf.append("\n" + indent + pad + pad + "<" + child.info.getArgName(i) + ">" + classToArgName(child.info.getArgType(i)) + "</" + child.info.getArgName(i) + ">");
                                newline = true;
                            }
                            if (child.info.getArgCount() == -1)
                            {
                                if (i > 0)
                                {
                                    // stop iterating thru arguments when an arg name
                                    // matches a previously used arg name.
                                    boolean found = false; // true if found argName in the arg list
                                    String argName = child.info.getArgName(i + 1);
                                    for (int j = i; j >= 0; j--)
                                    {
                                        if (child.info.getArgName(j).equals(argName))
                                        {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (found)
                                    {
                                        break;
                                    }
                                }
                            }
                            else if (i >= child.info.getArgCount())
                            {
                                break;
                            }
                            ++i;
                        }
                        if (newline)
                            buf.append("\n" + indent + pad);

                        buf.append("</" + child.shortname + ">\n");
                        buf.append(indent + pad + "-->\n");
                    }
                    else
                    {
                        // var may be set multiple times...
                        boolean newline = false;
                        for (final ConfigurationValue cv : child.values)
                        {
                            buf.append(indent + pad + "<" + child.shortname + ">");

                            int argCount = child.info.getArgCount();
                            // var may have multiple values...
                            int argc = 0;
                            for (final String arg : cv.getArgs())
                            {
                                if (argCount == 1)
                                {
                                    buf.append(arg);
                                    break;
                                }
                                else
                                {
                                    String argname = child.info.getArgName(argc++);
                                    newline = true;
                                    buf.append("\n" + indent + pad + pad + "<" + argname + ">" + arg + "</" + argname + ">");
                                }
                            }
                            if (newline)
                                buf.append("\n" + indent + pad);
                            buf.append("</" + child.shortname + ">\n");
                        }
                    }
                }
            }
        }
        buf.append(indent + "</" + node.shortname + ">\n");

        return buf.toString();
    }

    private static void addNode(ConfigurationBuffer cfgbuf, String var, FormatNode root)
    {
        String name = null;
        StringTokenizer t = new StringTokenizer(var, ".");

        FormatNode current = root;

        while (t.hasMoreTokens())
        {
            String token = t.nextToken();

            if (name == null)
                name = token;
            else
                name += "." + token;

            if (current.children == null)
                current.children = new TreeMap<String, FormatNode>();

            if (isSubTree(name))
            {
                if (!current.children.containsKey(token))
                {
                    FormatNode node = new FormatNode();
                    node.fullname = name;
                    node.shortname = token;
                    node.children = new TreeMap<String, FormatNode>();
                    current.children.put(token, node);
                    current = node;
                }
                else
                {
                    current = current.children.get(token);
                }
            }
            else if (cfgbuf.isValidVar(name))
            {
                FormatNode node = new FormatNode();
                node.fullname = name;
                node.shortname = token;
                node.info = cfgbuf.getInfo(name);
                node.values = cfgbuf.getVar(name);
                current.children.put(token, node);
            }
        }
    }

    public static String formatBuffer(ConfigurationBuffer cfgbuf,
                                       String rootElement,
                                       LocalizationManager lmgr,
                                       String prefix)
    {
        FormatNode root = new FormatNode();
        root.shortname = rootElement;
        for (final String var : cfgbuf.getVars())
        {
            // if var is a 'hidden' or a 'removed' parameter, don't dump.
            ConfigurationInfo info = cfgbuf.getInfo(var);
            if (info != null && (info.isHidden() || info.isRemoved() || !info.isDisplayed()))
            {
                continue;
            }
            addNode(cfgbuf, var, root);
        }

        return formatBuffer1(cfgbuf, root, "", lmgr, prefix);
    }

    public static String formatBuffer(ConfigurationBuffer cfgbuf, String rootElement)
    {
        return formatBuffer(cfgbuf, rootElement, null, null);
    }
}
