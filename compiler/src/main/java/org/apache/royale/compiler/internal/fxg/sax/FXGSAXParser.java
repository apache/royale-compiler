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

package org.apache.royale.compiler.internal.fxg.sax;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.royale.compiler.fxg.IFXGParser;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.problems.FXGParserProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * FXGSAXParser implements a SAX parser for an input stream that represents a
 * FXG document
 */
public class FXGSAXParser implements IFXGParser
{
    private FXGSAXScanner scanner = null;

    /**
     * Constructs a new FXGSAXParser.
     * 
     * The scanner is initialized so that nodes and skipped elements can be
     * registered
     */
    public FXGSAXParser()
    {
        
    }

    /**
     * Parses an FXG document InputStream to produce an IFXGNode based DOM.
     * 
     * @param reader - input to be parsed
     * @param problems problem collection used to collect problems occurred within this method
     * @return the root IFXGNode of the DOM
     */
    @Override
    public IFXGNode parse(Reader reader, Collection<ICompilerProblem> problems)
    {
        return parse(reader, null, problems);
    }

    /**
     * Parses an FXG document InputStream to produce an IFXGNode based DOM.
     * 
     * @param reader - input to be parsed
     * @param documentPath - the path of the FXG document which can be useful
     * for error reporting.
     * @param problems problem collection used to collect problems occurred within this method
     * @return the root IFXGNode of the DOM
     */
    @Override
    public IFXGNode parse(Reader reader, String documentPath, Collection<ICompilerProblem> problems)
    {
        try
        {
            scanner = new FXGSAXScanner(problems);
        	scanner.setDocumentPath(documentPath);
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            saxFactory.setValidating(false);
            saxFactory.setNamespaceAware(true);
            SAXParser parser = saxFactory.newSAXParser();
            InputSource is = new InputSource(reader);
            parser.parse(is, scanner);

            IFXGNode node = scanner.getRootNode();
            return node;
        }
        catch (IOException ex)
        {
            problems.add(new FXGParserProblem(documentPath, scanner.getStartLine(), 
                    scanner.getStartColumn(), ex.getLocalizedMessage()));
        }
        catch (ParserConfigurationException ex)
        {
            problems.add(new FXGParserProblem(documentPath, scanner.getStartLine(), 
                    scanner.getStartColumn(), ex.getLocalizedMessage()));
        }
        catch (SAXException ex)
        {
            problems.add(new FXGParserProblem(documentPath, scanner.getStartLine(), 
                    scanner.getStartColumn(), ex.getLocalizedMessage()));
        }
        finally
        {
            try 
            {
                reader.close();
            } 
            catch(Exception e)
            {
                //do nothing
            }
        }
        
        return null;
    }

}
