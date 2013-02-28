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

package org.apache.flex.compiler.mxml.codegen;

import java.io.Writer;

import org.apache.flex.compiler.common.codegen.IEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.internal.mxml.codegen.MXMLBlockWalker;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.flex.compiler.tree.mxml.IMXMLLiteralNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.visitor.IASNodeStrategy;
import org.apache.flex.compiler.visitor.IMXMLBlockWalker;

/**
 * The {@link IMXMLEmitter} interface allows abstraction between the
 * {@link IASNodeStrategy} and the current output buffer {@link Writer}.
 * 
 * @author Michael Schmalle
 */
public interface IMXMLEmitter extends IEmitter
{

    IMXMLBlockWalker getMXMLWalker();

    void setMXMLWalker(MXMLBlockWalker mxmlBlockWalker);

    //--------------------------------------------------------------------------

    void emitDocumentHeader(IClassDefinition definition);
    void emitDocumentFooter(IClassDefinition definition);

    //--------------------------------------------------------------------------

    void emitClass(IMXMLClassDefinitionNode node);

    //--------------------------------------------------------------------------

    void emitInstance(IMXMLInstanceNode node);
    void emitPropertySpecifier(IMXMLPropertySpecifierNode node);

    //--------------------------------------------------------------------------

    void emitString(IMXMLStringNode node);

    //--------------------------------------------------------------------------

    void emitLiteral(IMXMLLiteralNode node);
    
}
