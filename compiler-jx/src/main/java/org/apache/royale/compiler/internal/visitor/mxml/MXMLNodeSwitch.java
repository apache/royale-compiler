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

package org.apache.royale.compiler.internal.visitor.mxml;

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLComponentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLImplementsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLIntNode;
import org.apache.royale.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.royale.compiler.tree.mxml.IMXMLMetadataNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.royale.compiler.tree.mxml.IMXMLObjectNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectMethodNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;
import org.apache.royale.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceOperationNode;
import org.apache.royale.compiler.visitor.IASNodeStrategy;
import org.apache.royale.compiler.visitor.IBlockVisitor;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockVisitor;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class MXMLNodeSwitch implements IASNodeStrategy
{
    private final IMXMLBlockVisitor visitor;

    public MXMLNodeSwitch(IBlockVisitor visitor)
    {
        this.visitor = (IMXMLBlockVisitor) visitor;
    }

    @Override
    public void handle(IASNode node)
    {
        switch (node.getNodeID())
        {
        case MXMLArrayID:
            visitor.visitArray((IMXMLArrayNode) node);
            break;
        case MXMLBooleanID:
            visitor.visitBoolean((IMXMLBooleanNode) node);
            break;
        case MXMLDeclarationsID:
            visitor.visitDeclarations((IMXMLDeclarationsNode) node);
            break;
        case MXMLDeferredInstanceID:
            visitor.visitDeferredInstance((IMXMLDeferredInstanceNode) node);
            break;
        case MXMLDocumentID:
            visitor.visitDocument((IMXMLDocumentNode) node);
            break;
        case MXMLEventSpecifierID:
            visitor.visitEventSpecifier((IMXMLEventSpecifierNode) node);
            break;
        case MXMLFileID:
            visitor.visitFile((IMXMLFileNode) node);
            break;
        case MXMLIntID:
            visitor.visitInt((IMXMLIntNode) node);
            break;
        case MXMLInstanceID:
            visitor.visitInstance((IMXMLInstanceNode) node);
            break;
        case MXMLLiteralID:
            visitor.visitLiteral((IMXMLLiteralNode) node);
            break;
        case MXMLNumberID:
            visitor.visitNumber((IMXMLNumberNode) node);
            break;
        case MXMLEffectSpecifierID:
        case MXMLPropertySpecifierID:
            visitor.visitPropertySpecifier((IMXMLPropertySpecifierNode) node);
            break;
        case MXMLScriptID:
            visitor.visitScript((IMXMLScriptNode) node);
            break;
        case MXMLStringID:
            visitor.visitString((IMXMLStringNode) node);
            break;
        case MXMLStyleSpecifierID:
            visitor.visitStyleSpecifier((IMXMLStyleSpecifierNode) node);
            break;
        case MXMLUintID:
            visitor.visitUint((IMXMLUintNode) node);
            break;
        case MXMLClassID:
            visitor.visitMXMLClass((IMXMLClassNode) node);
            break;
        case MXMLStyleID:
            visitor.visitStyleBlock((IMXMLStyleNode)node);
            break;
        case MXMLStateID:
            visitor.visitInstance((IMXMLInstanceNode) node);
            break;
        case MXMLFactoryID:
            visitor.visitFactory((IMXMLFactoryNode) node);
            break;
        case MXMLComponentID:
            visitor.visitComponent((IMXMLComponentNode) node);
            break;
        case MXMLMetadataID:
            visitor.visitMetadata((IMXMLMetadataNode) node);
            break;
        case MXMLEmbedID:
            visitor.visitEmbed((IMXMLEmbedNode) node);
            break;
        case MXMLImplementsID:
            visitor.visitImplements((IMXMLImplementsNode) node);
            break;
        case MXMLVectorID:
            visitor.visitVector((IMXMLVectorNode) node);
            break;
        case MXMLDataBindingID:
            visitor.visitDatabinding((IMXMLDataBindingNode) node);
            break;
        case MXMLBindingID:
            visitor.visitBinding((IMXMLBindingNode) node);
        	break;
        case MXMLObjectID:
            visitor.visitObject((IMXMLObjectNode) node);
        	break;
        case MXMLHTTPServiceID:
        case MXMLXMLID:
        case MXMLXMLListID:
        case MXMLFunctionID:
            visitor.visitInstance((IMXMLInstanceNode) node);
            break;
        case MXMLRemoteObjectID:
            visitor.visitRemoteObject((IMXMLRemoteObjectNode) node);
            break;
        case MXMLRemoteObjectMethodID:
            visitor.visitRemoteObjectMethod((IMXMLRemoteObjectMethodNode) node);
            break;
        case MXMLWebServiceID:
            visitor.visitWebService((IMXMLWebServiceNode) node);
            break;
        case MXMLWebServiceOperationID:
            visitor.visitWebServiceMethod((IMXMLWebServiceOperationNode) node);
            break;
        case MXMLApplicationID:
        case MXMLBindingAttributeID:
        case MXMLClassDefinitionID:
        case MXMLClearID:
        case MXMLConcatenatedDataBindingID:
        case MXMLDateID:
        case MXMLDefinitionID:
        case MXMLDesignLayerID:
        case MXMLHTTPServiceRequestID:
        case MXMLLibraryID:
        case MXMLModelID:
        case MXMLModelPropertyID:
        case MXMLModelRootID:
        case MXMLPrivateID:
        case MXMLRegExpID:
        case MXMLReparentID:
        //case MXMLRepeaterID:
        case MXMLResourceID:
        default:
            throw new IllegalArgumentException(
                    "No handler specified for nodes of type '"
                            + node.getNodeID().getParaphrase() + "'");
        }
    }

}
