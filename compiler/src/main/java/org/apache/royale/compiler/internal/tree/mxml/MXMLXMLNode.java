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

import java.io.StringWriter;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDualContentProblem;
import org.apache.royale.compiler.problems.MXMLUnknownXMLFormatProblem;
import org.apache.royale.compiler.problems.MXMLXMLOnlyOneRootTagProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLXMLNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * Implementation of the {@code IMXMLXMLNode} interface.
 */
class MXMLXMLNode extends MXMLInstanceNode implements IMXMLXMLNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLXMLNode(NodeBase parent)
    {
        super(parent);
    }

    private IMXMLTagData rootTag;

    private XML_TYPE xmlType = XML_TYPE.E4X;

    // did we see more than one child tag?
    boolean multipleTags = false;

    private String xmlString;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLXMLID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.XML;
    }

    /**
     * What type of xml object should this node create.
     */
    @Override
    public XML_TYPE getXMLType()
    {
        return xmlType;
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_SOURCE))
        {
            // Resolve the attribute value to a normalized path.
            // Doing so makes this compilation unit dependent on that file.
            String sourcePath = resolveSourceAttributePath(builder, attribute, info);
            if (sourcePath != null)
                xmlString = builder.readExternalFile(attribute, sourcePath);
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_FORMAT))
        {
            String attrValue = attribute.getRawValue().toLowerCase();

            if (attrValue.equals(FORMAT_E4X))
            {
                xmlType = XML_TYPE.E4X;
            }
            else if (attrValue.equals(FORMAT_XML))
            {
                xmlType = XML_TYPE.OLDXML;
            }
            else
            {
                // Unlike Flex 4.5, we report a problem if the format
                // is anything other than "e4x" or "xml".                
                ICompilerProblem problem = new MXMLUnknownXMLFormatProblem(attribute);
                builder.addProblem(problem);

                // But like Flex 4.5, we default to "xml"
                // if neither is specified.
                xmlType = XML_TYPE.OLDXML;
            }
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected void processChildTag(MXMLTreeBuilder builder, IMXMLTagData tag,
                                   IMXMLTagData childTag, MXMLNodeInfo info)
    {
        info.hasDualContent = true;

        if (rootTag == null)
            rootTag = childTag;
        else
            multipleTags = true;
    }

    @Override
    public IClassDefinition getClassReference(ICompilerProject project)
    {
        if (xmlType == XML_TYPE.OLDXML)
        {
            ASProjectScope projectScope = (ASProjectScope)project.getScope();
            return (IClassDefinition)projectScope.findDefinitionByName(XML_NODE_NAME);
        }

        return super.getClassReference(project);
    }

    /**
     * This method gives subclasses a chance to do final processing after
     * considering each attribute and content unit.
     * <p>
     * The base class version calls <code>adjustOffset</code> to translate the
     * node start and end offset from local to absolute offsets.
     */
    @Override
    protected void initializationComplete(MXMLTreeBuilder builder,
                                          IMXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        if (info.hasSourceAttribute && info.hasDualContent)
        {
            ICompilerProblem problem = new MXMLDualContentProblem(tag, tag.getShortName());
            builder.addProblem(problem);
            return;
        }

        if (multipleTags)
            builder.addProblem(new MXMLXMLOnlyOneRootTagProblem(tag));

        //if (rootTag == null && !tag.isEmptyTag())
        //    builder.addProblem(new MXMLXMLRequireContentProblem(tag));

        analyzeXML(builder);

        // don't pin the MXMLTagDatas
        rootTag = null;

        // An old-style <XML> tag introduces a dependency on mx.utils.XMLUtils.
        if (xmlType == XML_TYPE.OLDXML)
        {
            RoyaleProject project = builder.getProject();
            builder.addExpressionDependency(project.getXMLUtilClass());
        }
    }

    /**
     * Gets the XML that this node represents as a string. This will trim out
     * all the bindable parts, as those parts will be set programmatically when
     * their bindings fire.
     * 
     * @return A String representation of the XML object
     */
    @Override
    public String getXMLString()
    {
        return xmlString;
    }

    /**
     * Walk the XML children of this node, and grab all the goodies we need. 1.
     * Grabs a String representation of the XML. 2. Will record all databinding
     * expressions, and the target expressions.
     */
    private void analyzeXML(MXMLTreeBuilder builder)
    {
        if (rootTag != null)
        {
            StringWriter writer = new StringWriter();
            XMLBuilder xmlBuilder = new XMLBuilder(
                    this, rootTag, rootTag.getCompositePrefixMap(), builder);
            xmlBuilder.processNode(rootTag, writer);

            setChildren(xmlBuilder.getDatabindings().toArray(new IMXMLNode[] {}));

            xmlString = writer.toString();
        }
    }
}
