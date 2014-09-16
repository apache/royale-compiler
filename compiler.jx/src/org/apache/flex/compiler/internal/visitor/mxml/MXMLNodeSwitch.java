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

package org.apache.flex.compiler.internal.visitor.mxml;

import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLComponentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFactoryNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLImplementsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLMetadataNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLUintNode;
import org.apache.flex.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.flex.compiler.visitor.IASNodeStrategy;
import org.apache.flex.compiler.visitor.IBlockVisitor;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockVisitor;

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
            
        case MXMLApplicationID:
        case MXMLBindingID:
        case MXMLBindingAttributeID:
        case MXMLClassID:
        case MXMLClassDefinitionID:
        case MXMLClearID:
        case MXMLConcatenatedDataBindingID:
        case MXMLDateID:
        case MXMLDefinitionID:
        case MXMLDesignLayerID:
        case MXMLEffectSpecifierID:
        case MXMLFunctionID:
        case MXMLHTTPServiceID:
        case MXMLHTTPServiceRequestID:
        case MXMLLibraryID:
        case MXMLModelID:
        case MXMLModelPropertyID:
        case MXMLModelRootID:
        case MXMLObjectID:
        case MXMLPrivateID:
        case MXMLRegExpID:
        case MXMLRemoteObjectID:
        case MXMLRemoteObjectMethodID:
        case MXMLReparentID:
        //case MXMLRepeaterID:
        case MXMLResourceID:
        case MXMLWebServiceID:
        case MXMLWebServiceOperationID:
        case MXMLXMLID:
        case MXMLXMLListID:
        default:
            throw new IllegalArgumentException(
                    "No handler specified for nodes of type '"
                            + node.getNodeID().getParaphrase() + "'");
        }
    }

}
