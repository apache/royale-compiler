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

package org.apache.royale.swc.catalog;

import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Add indentation to XML.
 */
public class XMLFormatter implements XMLStreamWriter
{
    private static final Integer STATE_START = 0;
    private static final Integer STATE_IN_ELEMENT = 1;
    private static final Integer STATE_IN_DATA = 2;
    
    private static final String INDENT_STRING = "    ";
    
    // We use platform-independent line endings so that files
    // such as a SWC's catalog.xml in don't depend on whether
    // the SWC was compiled on Mac or Win.
    private static final String LINE_ENDING_STRING = "\n";

    public XMLFormatter(XMLStreamWriter writer)
    {
        this.writer = writer;
        this.states = new Stack<Integer>();
    }
    
    private final XMLStreamWriter writer;
    
    private int depth = 0;

    private final Stack<Integer> states;

    private Integer currentState = STATE_START;

    @Override
    public void close() throws XMLStreamException
    {
        writer.close();
    }

    @Override
    public void flush() throws XMLStreamException
    {
        writer.flush();
    }

    @Override
    public NamespaceContext getNamespaceContext()
    {
        return writer.getNamespaceContext();
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException
    {
        return writer.getPrefix(uri);
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException
    {
        return writer.getProperty(name);
    }

    private void onEmptyElement() throws XMLStreamException
    {
        currentState = STATE_IN_ELEMENT;
        if (depth > 0)
        {
            writeLineEnding();
        }
        writeIndentString();
    }

    private void onEndElement() throws XMLStreamException
    {
        depth--;
        if (currentState == STATE_IN_ELEMENT)
        {
            writeLineEnding();
            writeIndentString();
        }
        currentState = states.pop();
    }

    private void onStartElement() throws XMLStreamException
    {
        states.push(STATE_IN_ELEMENT);
        currentState = STATE_START;
        if (depth > 0)
        {
            writeLineEnding();
        }
        writeIndentString();
        depth++;
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException
    {
        writer.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException
    {
        writer.setNamespaceContext(context);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException
    {
        writer.setPrefix(prefix, uri);
    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException
    {
        writer.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException
    {
        writer.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException
    {
        writer.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException
    {
        currentState = STATE_IN_DATA;
        writer.writeCData(data);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException
    {
        currentState = STATE_IN_DATA;
        writer.writeCharacters(text, start, len);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException
    {
        currentState = STATE_IN_DATA;
        writer.writeCharacters(text);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException
    {
        writer.writeComment(data);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException
    {
        writer.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException
    {
        writer.writeDTD(dtd);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException
    {
        onEmptyElement();
        writer.writeEmptyElement(localName);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException
    {
        onEmptyElement();
        writer.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
    {
        onEmptyElement();
        writer.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEndDocument() throws XMLStreamException
    {
        writer.writeEndDocument();
    }

    @Override
    public void writeEndElement() throws XMLStreamException
    {
        onEndElement();
        writer.writeEndElement();
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException
    {
        writer.writeEntityRef(name);
    }

    private void writeIndentString() throws XMLStreamException
    {
        if (depth > 0)
        {
            for (int i = 0; i < depth; i++)
                writer.writeCharacters(INDENT_STRING);
        }
    }
    
    private void writeLineEnding() throws XMLStreamException
    {
        writer.writeCharacters(LINE_ENDING_STRING);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException
    {
        writer.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException
    {
        writer.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException
    {
        writer.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException
    {
        writer.writeStartDocument();
        writeLineEnding();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException
    {
        writer.writeStartDocument(version);
        writeLineEnding();
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException
    {
        writer.writeStartDocument(encoding, version);
        writeLineEnding();;
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException
    {
        onStartElement();
        writer.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException
    {
        onStartElement();
        writer.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
    {
        onStartElement();
        writer.writeStartElement(prefix, localName, namespaceURI);
    }

}
