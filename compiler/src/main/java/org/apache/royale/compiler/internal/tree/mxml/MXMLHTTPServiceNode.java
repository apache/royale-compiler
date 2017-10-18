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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLHTTPServiceNode;

/**
 * AST node for {@code <s:HTTPService>} tags. An {@code HTTPService} tag has a
 * special child tag {@code <s:request>} which defines an object literal but not
 * using the object literal MXML convention.
 * <p>
 * For example:
 * 
 * <pre>
 * &lt;s:request xmlns=""&gt;
 *     &lt;username&gt;John&lt;/username&gt;
 *     &lt;password&gt;1234&lt;/password&gt;
 * &lt;/s:request&gt;
 * </pre>
 * 
 * This "request" tag defines an object literal:
 * 
 * <pre>
 * httpService.request = { username:"John", password:"1234" };
 * </pre>
 * 
 * The purpose of this node is solely to special-case the
 * {@code MXMLPropertySpecifierNode} creation logic, so that a
 * {@code MXMLHTTPServiceRequestPropertyNode} is created for a
 * {@code <s:request>} tag.
 */
class MXMLHTTPServiceNode extends MXMLInstanceNode implements IMXMLHTTPServiceNode
{

    private static final String TAG_REQUEST = "request";

    /**
     * Create an AST node for {@code <s:HTTPService>} tag.
     * 
     * @param parent Parent node.
     */
    MXMLHTTPServiceNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLHTTPServiceID;
    }

    /**
     * Create special {@link MXMLHTTPServiceRequestPropertyNode} for
     * {@code <s:request>} tag. Otherwise, fall-back to the normal creation
     * logic.
     */
    @Override
    protected final MXMLSpecifierNodeBase createSpecifierNode(MXMLTreeBuilder builder, String specifierName)
    {
        if (TAG_REQUEST.equals(specifierName))
        {
            final RoyaleProject project = builder.getProject();
            final IClassDefinition classHTTPService = getClassReference(project);
            final IDefinition definitionRequest = project.resolveSpecifier(classHTTPService, TAG_REQUEST);
            if (definitionRequest != null)
            {
                final MXMLHTTPServiceRequestPropertyNode requestPropertyNode = new MXMLHTTPServiceRequestPropertyNode(this);
                requestPropertyNode.setDefinition(definitionRequest);
                return requestPropertyNode;
            }
        }
        return super.createSpecifierNode(builder, specifierName);
    }
}
