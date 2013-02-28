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

package org.apache.flex.compiler.internal.mxml.visitor;

import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.visitor.IASNodeStrategy;
import org.apache.flex.compiler.visitor.IMXMLBlockVisitor;

/**
 * @author Michael Schmalle
 */
public class MXMLNodeSwitch implements IASNodeStrategy
{
    private final IMXMLBlockVisitor visitor;

    public MXMLNodeSwitch(IMXMLBlockVisitor visitor)
    {
        this.visitor = visitor;
    }

    @Override
    public void handle(IASNode node)
    {
        switch (node.getNodeID())
        {
        case MXMLClassDefinitionID:
            visitor.visitClassDefinition((IMXMLClassDefinitionNode) node);
            break;
        case MXMLDeclarationsID:
            visitor.visitDeclarations((IMXMLDeclarationsNode) node);
            break;
        case MXMLDocumentID:
            visitor.visitDocument((IMXMLDocumentNode) node);
            break;
        case MXMLInstanceID:
            visitor.visitInstance((IMXMLInstanceNode) node);
            break;
        case MXMLLiteralID:
            visitor.visitLiteral((IMXMLLiteralNode) node);
            break;
        case MXMLPropertySpecifierID:
            visitor.visitPropertySpecifier((IMXMLPropertySpecifierNode) node);
            break;
        case MXMLStringID:
            visitor.visitString((IMXMLStringNode) node);
            break;
            
        default:
            throw new IllegalArgumentException("No handler specified for nodes of type '" + node.getNodeID().getParaphrase() + "'");
        }
    }

}
