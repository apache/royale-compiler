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

package org.apache.royale.compiler.codegen.mxml;

import java.io.Writer;

import org.apache.royale.compiler.codegen.IEmitter;
import org.apache.royale.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLComponentNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
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
import org.apache.royale.compiler.tree.mxml.IMXMLStyleSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;
import org.apache.royale.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceOperationNode;
import org.apache.royale.compiler.visitor.IASNodeStrategy;
import org.apache.royale.compiler.visitor.IBlockWalker;

/**
 * The {@link IMXMLEmitter} interface allows abstraction between the
 * {@link IASNodeStrategy} and the current output buffer {@link Writer}.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public interface IMXMLEmitter extends IEmitter
{

    IBlockWalker getMXMLWalker();

    String postProcess(String output);
    
    void setMXMLWalker(IBlockWalker mxmlBlockWalker);

    //--------------------------------------------------------------------------

    void emitDocumentHeader(IMXMLFileNode node);

    void emitDocumentFooter(IMXMLFileNode node);

    //--------------------------------------------------------------------------

    void emitClass(IMXMLClassDefinitionNode node);

    //--------------------------------------------------------------------------

    void emitEventSpecifier(IMXMLEventSpecifierNode node);

    void emitInstance(IMXMLInstanceNode node);

    void emitPropertySpecifier(IMXMLPropertySpecifierNode node);

    void emitScript(IMXMLScriptNode node);

    void emitStyleSpecifier(IMXMLStyleSpecifierNode node);

    //--------------------------------------------------------------------------

    void emitArray(IMXMLArrayNode node);

    void emitBoolean(IMXMLBooleanNode node);

    void emitInt(IMXMLIntNode node);

    void emitNumber(IMXMLNumberNode node);

    void emitObject(IMXMLObjectNode node);

    void emitString(IMXMLStringNode node);

    void emitUint(IMXMLUintNode node);

    void emitMXMLClass(IMXMLClassNode node);

    //--------------------------------------------------------------------------

    void emitLiteral(IMXMLLiteralNode node);

    //--------------------------------------------------------------------------

    void emitPropertySpecifiers(IMXMLPropertySpecifierNode[] nodes,
            boolean emitAttributes);

    void emitFactory(IMXMLFactoryNode node);

    void emitComponent(IMXMLComponentNode node);

    void emitDeclarations(IMXMLDeclarationsNode node);

    //--------------------------------------------------------------------------

    void emitMetadata(IMXMLMetadataNode node);

    //--------------------------------------------------------------------------

    void emitEmbed(IMXMLEmbedNode node);
    
    //--------------------------------------------------------------------------
    
    void emitImplements(IMXMLImplementsNode node);
    
    //--------------------------------------------------------------------------
    
    void emitVector(IMXMLVectorNode node);
    
    //--------------------------------------------------------------------------
    
    void emitDatabinding(IMXMLDataBindingNode node);

    //--------------------------------------------------------------------------

    void emitRemoteObjectMethod(IMXMLRemoteObjectMethodNode node);

	void emitRemoteObject(IMXMLRemoteObjectNode node);

    //--------------------------------------------------------------------------

    void emitWebServiceMethod(IMXMLWebServiceOperationNode node);

	void emitWebService(IMXMLWebServiceNode node);
}
