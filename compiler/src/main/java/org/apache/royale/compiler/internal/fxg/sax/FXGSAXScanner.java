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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.royale.compiler.fxg.FXGConstants;
import org.apache.royale.compiler.fxg.dom.IFXGNode;

import org.apache.royale.compiler.internal.fxg.dom.CDATANode;
import org.apache.royale.compiler.internal.fxg.dom.GraphicNode;
import org.apache.royale.compiler.internal.fxg.dom.DefinitionNode;
import org.apache.royale.compiler.internal.fxg.dom.DelegateNode;
import org.apache.royale.compiler.internal.fxg.dom.IPreserveWhiteSpaceNode;
import org.apache.royale.compiler.problems.FXGInvalidRootNodeProblem;
import org.apache.royale.compiler.problems.FXGInvalidVersionProblem;
import org.apache.royale.compiler.problems.FXGMissingAttributeProblem;
import org.apache.royale.compiler.problems.FXGMultipleElementProblem;
import org.apache.royale.compiler.problems.FXGPrivateElementNotChildOfGraphicProblem;
import org.apache.royale.compiler.problems.FXGPrivateElementNotLastProblem;
import org.apache.royale.compiler.problems.FXGScanningProblem;
import org.apache.royale.compiler.problems.FXGUnknownElementInVersionProblem;
import org.apache.royale.compiler.problems.FXGVersionHandlerNotRegisteredProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

/**
 * This SAX2 based scanner converts an FXG document (an XML based description of
 * a graphical asset) to a simple object graph to serve as an intermediate
 * representation. The document must be in the FXG 1.0 namespace and the root
 * element must be a &lt;Graphic&gt; tag.
 */
public class FXGSAXScanner extends DefaultHandler
{
    private static boolean REJECT_MAJOR_VERSION_MISMATCH = false;
    
    // A special case needed to short circuit GroupNode creation inside a
    // Definition as such Groups are not the same as those in the graphics
    // tree.
    private static final String FXG_GROUP_DEFINITION_ELEMENT = "[GroupDefinition]";
        
    private GraphicNode root;
    private Stack<IFXGNode> stack;
    private int skippedElementCount;
    private boolean seenPrivateElement = false;
    private boolean inMaskAfterPrivateElement = false;
    private Locator locator;
    private int startLine = 0;
    private int startColumn = 0;
    private String documentPath = null;
    private String unknownElement = null;
    
    private Collection<ICompilerProblem> problems;
    
    // FXG version handler to handle different fxg versions 
    // depending on input file version at runtime. 
    private IFXGVersionHandler versionHandler = null;
    
    /**
     * Construct a new FXGSAXScanner
     */
    public FXGSAXScanner(Collection<ICompilerProblem> problems)
    {
        super();
        this.problems = problems;
        versionHandler = FXGVersionHandlerRegistry.getDefaultHandler();
        if (versionHandler == null)
            problems.add(new FXGVersionHandlerNotRegisteredProblem(FXGVersionHandlerRegistry.defaultVersion.asDouble()));
    }

    /**
     * Provides access to the root IFXGNode of the FXG document AFTER parsing.
     * 
     * @return the root IFXGNode of the DOM.
     */
    public IFXGNode getRootNode()
    {
        return root;
    }

    //--------------------------------------------------------------------------
    //
    // SAX DefaultHandler Implementation
    //
    //--------------------------------------------------------------------------

    @Override
    public void setDocumentLocator(Locator locator)
    {
        this.locator = locator;
    }
    
    /**
     * Get document path used for logging.
     */
    public String getDocumentPath()
    {
        return documentPath;
    }

    /**
     * Set document path used for logging.
     */
    public void setDocumentPath(String documentPath)
    {
        this.documentPath = documentPath;
    }


    @Override
    public void startDocument() throws SAXException
    {
        stack = new Stack<IFXGNode>();
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException
    {
        // First check if we're currently skipping elements
        if (isSkippedElement(uri, localName, true))
            skippedElementCount++;
        if (inSkippedElement())
            return;
        
        // Check if we're currently skipping unknown elements
        if (unknownElement != null)
        	return;

        // Record starting position
        startLine = locator.getLineNumber();
        startColumn = locator.getColumnNumber();

        // Check the current parent
        IFXGNode parent = null;
        if (stack.size() > 0)
        {
            parent = stack.peek();
            if(parent == null)
            {
                //If the parent is invalid, then there is no need to look into the children
                return;
            }
        }

        // Switch to special GroupDefinitionNode for Definition child
        if (isFXGNamespace(uri))
        {
            if (parent instanceof DefinitionNode && FXG_GROUP_ELEMENT.equals(localName))
                localName = FXG_GROUP_DEFINITION_ELEMENT;
        }
        
        // Create a node for this element
        IFXGNode node = createNode(uri, localName);
        
        try 
        {   
            if (node == null)
            {
                if (root != null)
                {
                    if (root.isVersionGreaterThanCompiler())
                    {
                        // Warning: Minor version of this FXG file is greater than minor
                        // version supported by this compiler. Log a warning for an
                        // unknown element.

                        //TODO FXGLOG                    
                        //FXGLog.getLogger().log(IFXGLogger.WARN, "UnknownElement", null, documentName, startLine, startColumn);
                        unknownElement = localName;
                    }
                    else
                    {
                        problems.add(new FXGUnknownElementInVersionProblem(documentPath, startLine, startColumn, localName, root.getFileVersion().asDouble()));                   
                    }
                }
                else
                {
                    problems.add(new FXGInvalidRootNodeProblem(documentPath, startLine, startColumn));
                }

                return;
            }

            // Provide access to the root document node used for querying version 
            // for non-root elements
            if (root != null)
            {
                node.setDocumentNode(root);
            }

            // Set node name if it is a delegate node. This allows proper error 
            // message to be reported.
            if (node instanceof DelegateNode)
            {
                DelegateNode propertyNode = (DelegateNode)node;
                propertyNode.setName(localName);
            }

            // Set attributes on the current node
            for (int i = 0; i < attributes.getLength(); i++)
            {
                String attributeURI = attributes.getURI(i);
                if (attributeURI == null || attributeURI == "" || isFXGNamespace(attributeURI))
                {
                    String attributeName = attributes.getLocalName(i);
                    String attributeValue = attributes.getValue(i);
                    node.setAttribute(attributeName, attributeValue, problems);
                }
            }

            // Associate child with parent node (and handle any special
            // relationships)
            if (parent != null)
            {
                if (node instanceof DelegateNode)
                {
                    DelegateNode propertyNode = (DelegateNode)node;
                    propertyNode.setDelegate(parent, problems);
                }
                else
                {
                    parent.addChild(node, problems);
                }
            }
            else if (node instanceof GraphicNode)
            {
                root = (GraphicNode)node;
                // Provide access to the root document node
                node.setDocumentNode(root);
                if (root.getVersion() == null)
                {
                    for(ICompilerProblem problem : problems)
                    {
                        if(problem instanceof FXGInvalidVersionProblem)
                        {
                            // There was a version attribute but it was invalid.
                            return;
                        }
                    }

                    //<Graphic> doesn't have the required attribute "version".
                    problems.add(new FXGMissingAttributeProblem(documentPath, startLine, startColumn, FXG_VERSION_ATTRIBUTE, root.getNodeName()));
                    return;
                }
                else
                {
                    if (!isMajorVersionMatch(root))
                    {
                        IFXGVersionHandler newVHandler = FXGVersionHandlerRegistry.getVersionHandler(root.getVersion());

                        if (newVHandler == null) 
                        {
                            if  (REJECT_MAJOR_VERSION_MISMATCH)
                            {
                                // Major version of this FXG file is greater than
                                // major version supported by this compiler. Cannot process
                                // the file.
                                problems.add(new FXGInvalidVersionProblem(documentPath, startLine, startColumn, root.getVersion().asString()));
                                return;
                            }
                            else
                            {
                                // Warning: Major version of this FXG file is greater than
                                // major version supported by this compiler.

                                //TODO FXGLOG
                                //FXGLog.getLogger().log(IFXGLogger.WARN, "MajorVersionMismatch", null, getDocumentName(), startLine, startColumn);

                                //use the latest version handler
                                versionHandler = FXGVersionHandlerRegistry.getLatestVersionHandler();
                                if (versionHandler == null)
                                {   
                                    problems.add(new FXGVersionHandlerNotRegisteredProblem(root.getVersion().asDouble()));   
                                    return;
                                }                           
                            }
                        }
                        else
                        {
                            versionHandler = newVHandler;
                        }
                    }
                }
                // Provide reference to the handler for querying version of the
                // current document processed.
                root.setDocumentPath(documentPath);
                root.setVersionGreaterThanCompiler(root.getVersion().greaterThan(versionHandler.getVersion()));
                root.setReservedNodes(versionHandler.getElementNodes(uri));
            }
            else if(root == null)
            {
                // Exception:<Graphic> must be the root node of an FXG document.
                problems.add(new FXGInvalidRootNodeProblem(documentPath, startLine, startColumn));
            }
        }
        finally 
        {
            stack.push(node);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException
    {
        if (stack != null && stack.size() > 0 && stack.peek() != null && 
                !inSkippedElement() && (unknownElement == null))
        {
            IFXGNode node = stack.peek();
            String content = new String(ch, start, length);

            if (!(node instanceof IPreserveWhiteSpaceNode))
            {
                content = content.trim();
            }
            
            if (content.length() > 0)
            {
                CDATANode cdata = new CDATANode();
                cdata.content = content;
                assignNodeLocation(cdata);
                node.addChild(cdata, problems);
            }
        }

        // Reset starting position
        startLine = locator.getLineNumber();
        startColumn = locator.getColumnNumber();
    }


    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException
    {
        if (isSkippedElement(uri, localName, false))
        {
            skippedElementCount--;
        }
        else if (unknownElement != null)
        {
            if (unknownElement.equals(localName))
            {
                unknownElement = null;
            }
        }
        else if (!inSkippedElement() && stack.peek() != null)
        {
            stack.pop();
        }

        // Reset starting position
        startLine = locator.getLineNumber();
        startColumn = locator.getColumnNumber();
    }


    //--------------------------------------------------------------------------
    //
    // Other Methods
    //
    //--------------------------------------------------------------------------

    /**
     * @return the last processed line number
     */
    public int getStartLine()
    {
        return startLine;
    }

    /**
     * @return the last processed column number
     */
    public int getStartColumn()
    {
        return startColumn;
    }

    /**
     * @param uri - the namespace URI to check
     * @return whether the given namespace URI is considered an FXG namespace. 
     */
    protected boolean isFXGNamespace(String uri)
    {
        return FXG_NAMESPACE.equals(uri);
    }

    /**
     * Determines whether an element should be skipped.
     * 
     * @param uri - the namespace URI of the element
     * @param localName - the name of the element
     * @return true if the element has been marked as skipped, otherwise false.
     */
    protected boolean isSkippedElement(String uri, String localName, boolean startElement)
    {
        Set<String> skippedElements = versionHandler.getSkippedElements(uri);
        if (skippedElements != null)
        {
            if (skippedElements.contains(FXGConstants.FXG_PRIVATE_ELEMENT)) 
            {
                validatePrivateElement(localName, startElement); 
            }
            if (skippedElements.contains(localName))
            {    
                return true;
            }
        }

        return false;
    }
    
    
    /**
     * Attempts to construct an instance of IFXGNode for the given element.
     * 
     * @param uri - the namespace URI of the element
     * @param localName - the name of the element
     * @return IFXGNode instance if
     */
    protected IFXGNode createNode(String uri, String localName)
    {
        IFXGNode node = null;

        try
        {
            Map<String, Class<? extends IFXGNode>> elementNodes = getElementNodes(uri);
            if (elementNodes != null)
            {
                Class<? extends IFXGNode> nodeClass = elementNodes.get(localName);
                if (nodeClass != null)
                {
                    node = (IFXGNode)nodeClass.newInstance();
                }
                else if (root != null)
                {
                    node = root.getDefinitionInstance(localName);
                }
            }
        }
        catch (Exception e)
        {
            problems.add(new FXGScanningProblem(documentPath, startLine, startColumn, e.getLocalizedMessage()));
        }

        if (node != null)
        {
            assignNodeLocation(node);
        }

        return node;
    }

    /**
     * @return if currently in a skipped element.
     */
    private boolean inSkippedElement()
    {
        return skippedElementCount > 0;
    }

    /**
     * Record the start and end line and column information for this node.
     * @param node - the current node 
     */
    private void assignNodeLocation(IFXGNode node)
    {
        if (node != null)
        {
            node.setStartLine(startLine);
            node.setStartColumn(startColumn);
            node.setEndLine(locator.getLineNumber());
            node.setEndColumn(locator.getColumnNumber());
        }
    }

    /**
     * @param uri - the namespace URI of the registered FXG elements.
     * @return a Map of the IFXGNode Classes registered for elements in the
     * given namespace URI.
     */
    private Map<String, Class<? extends IFXGNode>> getElementNodes(String uri)
    {
        return versionHandler.getElementNodes(uri);
    }

    /**
     * validates restrictions on PRIVATE element
     * @param localName
     */
    private void validatePrivateElement(String localName, boolean startElement)
    {
        if (!startElement)
        {
            if (inMaskAfterPrivateElement && localName.equals(FXGConstants.FXG_MASK_ELEMENT))
                inMaskAfterPrivateElement = false;
            return;
        }

        if (localName.equals(FXGConstants.FXG_PRIVATE_ELEMENT))
        {
            if (seenPrivateElement)
            {
                problems.add(new FXGMultipleElementProblem(documentPath, startLine, startColumn, localName));
            }
            else
            {
                if ((!inSkippedElement()) && stack.size() == 1)
                    seenPrivateElement = true;
                else
                    problems.add(new FXGPrivateElementNotChildOfGraphicProblem(documentPath, startLine, startColumn));
            }
        }
        else
        {
            if (seenPrivateElement && (!inSkippedElement()))
            {
                if ((!inMaskAfterPrivateElement) && (localName.equals(FXGConstants.FXG_MASK_ELEMENT)))
                {
                    inMaskAfterPrivateElement = true;
                }
                else
                {
                    if (!inMaskAfterPrivateElement)
                        problems.add(new FXGPrivateElementNotLastProblem(documentPath, startLine, startColumn));
                }
            }
        }
    }
    
    /**
     * @return - true if major version of the FXG file matches the compiler's
     * major version. false otherwise.
     */
    private boolean isMajorVersionMatch(GraphicNode root)
    {
        long majorVersion = root.getVersion().getMajorVersion();
        long compilerMajorVersion = versionHandler.getVersion().getMajorVersion();
        if (majorVersion == compilerMajorVersion)
            return true;
        else
            return false;
    }
    
}
