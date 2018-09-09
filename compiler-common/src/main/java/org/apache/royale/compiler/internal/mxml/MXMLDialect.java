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

package org.apache.royale.compiler.internal.mxml;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;

/**
 * This is the abstract base class for classes representing dialects of MXML,
 * such as MXML 2006 or MXML 2009.
 * <p>
 * Each MXML dialect has a unique language namespace, such as
 * <code>"http://ns.adobe.com/mxml/2009"</code> for MXML 2009.
 * <p>
 * You can use an MXML dialect to get the XML name for various core tags of the
 * language, such as <code>("http://ns.adobe.com/mxml/2009", "Library")</code>
 * for the {@code <Library>} tag of MXML 2009. The name will be
 * <code>null</code> if the tag is not supported in the dialect. For example,
 * the {@code <Library>} tag does not exist in MXML 2006.
 * <p>
 * You can also use an MXML dialect to perform core MXML parsing, such as
 * parsing ActionScript values of type <code>Boolean</code>, <code>int</code>,
 * etc. from attribute values and character data.
 * <p>
 * Static methods on this class allow you to determine whether a URI is language
 * namespace for a dialect of MXML, and to get the dialect object for that
 * namespace.
 */
public abstract class MXMLDialect
{
    /**
     * The <code>MXMLDialect</code> representing MXML 2012 (experimental).
     */
    public static final MXMLDialect MXML_2012 = MXMLDialect2012.getInstance();

    /**
     * The <code>MXMLDialect</code> representing MXML 2009.
     */
    public static final MXMLDialect MXML_2009 = MXMLDialect2009.getInstance();

    /**
     * The <code>MXMLDialect</code> representing MXML 2006.
     */
    public static final MXMLDialect MXML_2006 = MXMLDialect2006.getInstance();

    /**
     * The <code>MXMLDialect</code> representing the default dialect of MXML. It
     * is used when the dialect is not explicitly expressed.
     */
    public static final MXMLDialect DEFAULT = MXML_2009;

    /*
     * A map of language namespace to dialect, such as
     * "http://ns.adobe.com/mxml/2009" -> MXMLDialect.MXML_2009.
     */
    private static final Map<String, MXMLDialect> DIALECT_MAP =
            new ImmutableMap.Builder<String, MXMLDialect>()
                    .put(MXML_2006.getLanguageNamespace(), MXML_2006)
                    .put(MXML_2009.getLanguageNamespace(), MXML_2009)
                    .put(MXML_2012.getLanguageNamespace(), MXML_2012)
                    .build();

    /**
     * A map of entities to characters, such as "lt" -> '<'.
     * TODO HTML 4 supports about 250 named characters
     * HTML 5 supports about 2500. How many should MXML support
     * beyond these required 5? What did Xerces/mxmlc support?
     */
    private static final Map<String, Character> NAMED_ENTITY_MAP =
            new ImmutableMap.Builder<String, Character>()
                    .put("amp", '&')
                    .put("apos", '\'')
                    .put("gt", '>')
                    .put("lt", '<')
                    .put("quot", '"')
                    .put("nbsp", ' ')
                    .build();

    /**
     * Determines whether a specified URI is the language namespace for a
     * supported dialect of MXML.
     * <p>
     * The supported URIs are:
     * <ul>
     * <li><code>"http://www.adobe.com/2006/mxml"</code> for MXML 2006</li>
     * <li><code>"http://ns.adobe.com/mxml/2009"</code> for MXML 2009</li>
     * <li><code>"http://ns.adobe.com/mxml/2012"</code> for MXML 2012</li>
     * </ul>
     * 
     * @param uri A URI specifying a language namespace.
     * @return <code>true</code> if the URI is a supported language namespace.
     */
    public static boolean isLanguageNamespace(String uri)
    {
        return DIALECT_MAP.containsKey(uri);
    }

    /**
     * Gets the <code>MXMLDialect</code> object corresponding to a specified
     * language namespace.
     * 
     * @param uri A URI string specifying a langauge namespace.
     * @return An <code>MXMLDialect</code> or <code>null</code>.
     */
    public static MXMLDialect getDialectForLanguageNamespace(String uri)
    {
        return DIALECT_MAP.get(uri);
    }

    /**
     * Given a <code>PrefixMap</code> representing the <code>xmlns</code>
     * attributes on the root tag, determines which dialect of MXML is being
     * used.
     * 
     * @param rootPrefixMap A {@link PrefixMap}.
     * @return An {@link MXMLDialect}.
     */
    public static MXMLDialect getMXMLDialect(PrefixMap rootPrefixMap)
    {
        for (String prefix : rootPrefixMap.getAllPrefixes())
        {
            String ns = rootPrefixMap.getNamespaceForPrefix(prefix);
            if (isLanguageNamespace(ns))
                return getDialectForLanguageNamespace(ns);
        }

        // When we can't find anything, use the default dialect.
        return MXMLDialect.DEFAULT;
    }

    /**
     * Constructor.
     * 
     * @param languageNamespace The language namespace URI that identifies this
     * dialect of MXML, such as <code>"http://ns.adobe.com/mxml/2009"</code> for
     * MXML 2009.
     * @param year The numeric value (such as <code>2009</code>) used for
     * comparing different dialects of MXML.
     */
    protected MXMLDialect(String languageNamespace, int year)
    {
        this.languageNamespace = languageNamespace;
        this.year = year;

        // Names of language tags for builtin types.
        arrayXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.ARRAY);
        booleanXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.BOOLEAN);
        classXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.CLASS);
        dateXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.DATE);
        functionXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.FUNCTION);
        intXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.INT);
        numberXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.NUMBER);
        objectXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.OBJECT);
        stringXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.STRING);
        uintXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.UINT);
        xmlXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.XML);
        xmlListXMLName = new XMLName(languageNamespace, IMXMLLanguageConstants.XML_LIST);
    }

    private final String languageNamespace;

    private final int year;

    // Names of language tags that represent builtin ActionScript types.
    private final XMLName arrayXMLName;
    private final XMLName booleanXMLName;
    private final XMLName classXMLName;
    private final XMLName dateXMLName;
    private final XMLName functionXMLName;
    private final XMLName intXMLName;
    private final XMLName numberXMLName;
    private final XMLName objectXMLName;
    private final XMLName stringXMLName;
    private final XMLName uintXMLName;
    private final XMLName xmlXMLName;
    private final XMLName xmlListXMLName;

    // Names of special language tags that don't represent builtiln ActionScript types.
    protected XMLName bindingXMLName;
    protected XMLName componentXMLName;
    protected XMLName declarationsXMLName;
    protected XMLName definitionXMLName;
    protected XMLName libraryXMLName;
    protected XMLName metadataXMLName;
    protected XMLName modelXMLName;
    protected XMLName privateXMLName;
    protected XMLName reparentXMLName;
    protected XMLName scriptXMLName;
    protected XMLName styleXMLName;

    /**
     * Gets the language namespace for this dialect of MXML.
     * <p>
     * For MXML 2009, for example, this is
     * <code>"http://ns.adobe.com/mxml/2009"</code>.
     * 
     * @return The language namespace as a String.
     */
    public String getLanguageNamespace()
    {
        return languageNamespace;
    }

    /**
     * Determines whether this dialect is equal to, or later than, another
     * dialect.
     * 
     * @param other Another <code>MXMLDialect</code>.
     * @return <code>true</code if it.
     */
    public boolean isEqualToOrAfter(MXMLDialect other)
    {
        return year >= other.year;
    }

    /**
     * Determines whether this dialect is equal to, or earlier than, another
     * dialect.
     * 
     * @param other Another <code>MXMLDialect</code>.
     * @return <code>true</code if it.
     */
    public boolean isEqualToOrBefore(MXMLDialect other)
    {
        return year <= other.year;
    }

    /**
     * Gets the XML name of the {@code <Array>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveArray()
    {
        return arrayXMLName;
    }

    /**
     * Gets the XML name of the {@code <Binding>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveBinding()
    {
        return bindingXMLName;
    }

    /**
     * Gets the XML name of the {@code <Boolean>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveBoolean()
    {
        return booleanXMLName;
    }

    /**
     * Gets the XML name of the {@code <Class>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveClass()
    {
        return classXMLName;
    }

    /**
     * Gets the XML name of the {@code <Component>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveComponent()
    {
        return componentXMLName;
    }

    /**
     * Gets the XML name of the {@code <De3clarations>} tag in this dialect of
     * MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveDeclarations()
    {
        return declarationsXMLName;
    }

    /**
     * Gets the XML name of the {@code <Date>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveDate()
    {
        return dateXMLName;
    }

    /**
     * Gets the XML name of the {@code <Definition>} tag in this dialect of
     * MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveDefinition()
    {
        return definitionXMLName;
    }

    /**
     * Gets the XML name of the {@code <Function>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveFunction()
    {
        return functionXMLName;
    }

    /**
     * Gets the XML name of the {@code <int>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveInt()
    {
        return intXMLName;
    }

    /**
     * Gets the XML name of the {@code <Library>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveLibrary()
    {
        return libraryXMLName;
    }

    /**
     * Gets the XML name of the {@code <Metadata>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveMetadata()
    {
        return metadataXMLName;
    }

    /**
     * Gets the XML name of the {@code <Model>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveModel()
    {
        return modelXMLName;
    }

    /**
     * Gets the XML name of the {@code <Number>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveNumber()
    {
        return numberXMLName;
    }

    /**
     * Gets the XML name of the {@code <Object>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveObject()
    {
        return objectXMLName;
    }

    /**
     * Gets the XML name of the {@code <Private>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolvePrivate()
    {
        return privateXMLName;
    }

    /**
     * Gets the XML name of the {@code <Reparent>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveReparent()
    {
        return reparentXMLName;
    }

    /**
     * Gets the XML name of the {@code <Script>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveScript()
    {
        return scriptXMLName;
    }

    /**
     * Gets the XML name of the {@code <String>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveString()
    {
        return stringXMLName;
    }

    /**
     * Gets the XML name of the {@code <Style>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveStyle()
    {
        return styleXMLName;
    }

    /**
     * Gets the XML name of the {@code <uint>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveUint()
    {
        return uintXMLName;
    }

    /**
     * Gets the XML name of the {@code <XML>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveXML()
    {
        return xmlXMLName;
    }

    /**
     * Gets the XML name of the {@code <XMLList>} tag in this dialect of MXML.
     * 
     * @return An {@link XMLName} or <code>null</code> if this tag is not
     * supported.
     */
    public XMLName resolveXMLList()
    {
        return xmlListXMLName;
    }

    /**
     * Gets the character corresponding to an entity name in this dialect of
     * MXML.
     * <p>
     * For example, if you pass the entity name <code>"lt"</code>, this returns
     * the character <code>'>'</code>.
     * 
     * @return A Character, or <code>null</code> if the entity name is invalid
     * in this dialect of MXML.
     */
    public Character getNamedEntity(String entityName)
    {
        return NAMED_ENTITY_MAP.get(entityName);
    }

    /**
     * Determines whether a character is considered whitespace in this dialect
     * of MXML.
     * 
     * @param c The character.
     * @return <code>true</code> if the character is whitespace.
     */
    public abstract boolean isWhitespace(char c);

    /**
     * Determines whether a text String contains only characters that are
     * considered whitespace in this dialect of MXML.
     * 
     * @param s The string.
     * @return true if the string is all-whitespace, or empty.
     */
    public abstract boolean isWhitespace(String s);

    /**
     * Removes whitespace from the input string and returns a string that
     * contains at most 1 'replacementChar' character between each word.
     * <p>
     * This method can be used to strip newlines, tabs, multiple spaces, etc.
     * between words and replace them with a single space.
     * 
     * @param s The input String.
     * @param replacementChar The character that replaces whitespace between
     * words.
     * @return The output String.
     */
    public abstract String collapseWhitespace(String s, char replacementChar);

    /**
     * Removes any leading and trailing characters from a String that are
     * considered whitespace in this version of MXML.
     * 
     * @param s A String.
     * @return A new String with the leading and trailing whitespace removed.
     */
    public abstract String trim(String s);

    /**
     * Splits a string at commas and then trims whitespace from each part.
     * 
     * @param s The input string.
     * @return An array of trimmed strings split from the input string.
     */
    public abstract String[] splitAndTrim(String s);

    /**
     * Parses an ActionScript <code>Boolean</code> value from a string.
     * 
     * @param project The {@link ICompilerProject} within which the MXML is being
     * parsed.
     * @param s The string to be parsed.
     * @param flags A set of flags controlling the text parsing.
     * @return A Java <code>Boolean</code> representing the ActionScript
     * <code>Boolean</code>, or <code>null</code>.
     */
    public abstract Boolean parseBoolean(ICompilerProject project, String s,
                                         EnumSet<TextParsingFlags> flags);

    /**
     * Parses an ActionScript <code>int</code> value from a string.
     * 
     * @param project The {@link ICompilerProject} within which the MXML is being
     * parsed.
     * @param s The string to be parsed.
     * @param flags A set of flags controlling the text parsing.
     * @return A Java <code>Integer</code> representing the ActionScript
     * <code>int</code>, or <code>null</code>.
     */
    public abstract Integer parseInt(ICompilerProject project, String s,
                                     EnumSet<TextParsingFlags> flags);

    /**
     * Parses an ActionScript <code>uint</code> value from a string.
     * 
     * @param project The {@link ICompilerProject} within which the MXML is being
     * parsed.
     * @param s The string to be parsed.
     * @param flags A set of flags controlling the text parsing.
     * @return A Java <code>Long</code> representing the ActionScript
     * <code>uint</code>, or <code>null</code>.
     */
    public abstract Long parseUint(ICompilerProject project, String s,
                                   EnumSet<TextParsingFlags> flags);

    /**
     * Parses an ActionScript <code>Number</code> value from a string.
     * 
     * @param project The {@link ICompilerProject} within which the MXML is being
     * parsed.
     * @param s The string to be parsed.
     * @param flags A set of flags controlling the text parsing.
     * @return A Java <code>Number</code> representing the ActionScript
     * <code>Number</code>, or <code>null</code>.
     */
    public abstract Number parseNumber(ICompilerProject project, String s,
                                       EnumSet<TextParsingFlags> flags);

    /**
     * Parses an ActionScript <code>String</code> value from a string.
     * 
     * @param project The {@link ICompilerProject} within which the MXML is being
     * parsed.
     * @param s The string to be parsed.
     * @param flags A set of flags controlling the text parsing.
     * @return A Java <code>String</code> representing the ActionScript
     * <code>String</code>, or <code>null</code>.
     */
    public abstract String parseString(ICompilerProject project, String s,
                                       EnumSet<TextParsingFlags> flags);

    /**
     * Parses an ActionScript <code>Array</code> value from a string.
     * 
     * @param project The {@link ICompilerProject} within which the MXML is being
     * parsed.
     * @param s The string to be parsed.
     * @param flags A set of flags controlling the text parsing.
     * @return A <code>List</code> of Java <code>Object</code> instances
     * representing the elements of the ActionScript <code>Array</code>, or
     * <code>null</code>.
     */
    public abstract List<Object> parseArray(ICompilerProject project, String s,
                                            EnumSet<TextParsingFlags> flags);

    /**
     * Parses an ActionScript value from a string.
     * 
     * @param project The {@link ICompilerProject} within which the MXML is being
     * parsed.
     * @param s The string to be parsed.
     * @param flags A set of flags controlling the text parsing.
     * @return A Java <code>Object</code> representing the ActionScript value,
     * or <code>null</code>.
     */
    public abstract Object parseObject(ICompilerProject project, String s,
                                       EnumSet<TextParsingFlags> flags);

    /**
     * Flags that affect how the parsing methods work.
     */
    public static enum TextParsingFlags
    {
        /**
         * Recognize array literals, such as <code>"[ 1, 2 ]"</code>.
         */
        ALLOW_ARRAY,
        
        /**
         * Recognize databinding expressions, such as <code>"{name.first}"</code>.
         */
        ALLOW_BINDING,
        
        /**
         * Recognize named colors, such as <code>"red"</code>.
         */
        ALLOW_COLOR_NAME,
        
        /**
         * Recognize compiler directives, such as <code>"@Embed('flag.jpg')"</code>.
         */
        ALLOW_COMPILER_DIRECTIVE,
        
        /**
         * Allow escaping of compiler directives, so that <code>"\@Embed('flag.jpg')"</code>
         * means the text <code>"@Embed('flag.jpg')"</code> and not a directive.
         */
        ALLOW_ESCAPED_COMPILER_DIRECTIVE,
        
        /**
         * Recognize percent values, such as <code>"100%"</code>.
         */
        ALLOW_PERCENT,
        
        /**
         * Collapse whitespace into a single space character.
         */
        COLLAPSE_WHITE_SPACE,
        
        /**
         * Parse as rich text content.
         */
        RICH_TEXT_CONTENT
    }
}
