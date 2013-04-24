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

package org.apache.flex.compiler.visitor.mxml;

import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDeferredInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDocumentNode;
import org.apache.flex.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLUintNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IBlockVisitor;

/**
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public interface IMXMLBlockVisitor extends IBlockVisitor
{

    //--------------------------------------------------------------------------

    void visitCompilationUnit(ICompilationUnit unit);

    void visitFile(IMXMLFileNode node);

    void visitDocument(IMXMLDocumentNode node);

    void visitClassDefinition(IMXMLClassDefinitionNode node);

    void visitDeclarations(IMXMLDeclarationsNode node);

    //--------------------------------------------------------------------------

    void visitDeferredInstance(IMXMLDeferredInstanceNode node);

    //--------------------------------------------------------------------------

    void visitEventSpecifier(IMXMLEventSpecifierNode node);

    void visitInstance(IMXMLInstanceNode node);

    void visitPropertySpecifier(IMXMLPropertySpecifierNode node);

    void visitScript(IMXMLScriptNode node);

    void visitStyleBlock(IMXMLStyleNode node);

    void visitStyleSpecifier(IMXMLStyleSpecifierNode node);

    //--------------------------------------------------------------------------

    void visitArray(IMXMLArrayNode node);

    void visitBoolean(IMXMLBooleanNode node);

    void visitInt(IMXMLIntNode node);

    void visitNumber(IMXMLNumberNode node);

    void visitString(IMXMLStringNode node);

    void visitUint(IMXMLUintNode node);

    //--------------------------------------------------------------------------

    void visitLiteral(IMXMLLiteralNode node);
    
}
