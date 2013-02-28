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

package org.apache.flex.compiler.visitor;

import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;

/**
 * @author Michael Schmalle
 */
public interface IMXMLBlockVisitor
{
    void visitFile(IMXMLFileNode node);

    void visitDocument(IMXMLDocumentNode node);

    void visitClassDefinition(IMXMLClassDefinitionNode node);

    void visitDeclarations(IMXMLDeclarationsNode node);
    
    //--------------------------------------------------------------------------

    void visitPropertySpecifier(IMXMLPropertySpecifierNode node);

    void visitEventSpecifier(IMXMLEventSpecifierNode node);

    void visitStyleSpecifier(IMXMLStyleSpecifierNode node);

    void visitInstance(IMXMLInstanceNode node);

    void visitScript(IMXMLScriptNode node);

    //--------------------------------------------------------------------------

    void visitString(IMXMLStringNode node);

    //--------------------------------------------------------------------------

    void visitLiteral(IMXMLLiteralNode node);
    
}
