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

package org.apache.royale.compiler.codegen.js.goog;

import org.apache.royale.compiler.codegen.js.IJSDocEmitter;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

/**
 * The {@link IJSGoogDocEmitter} interface allows the abstraction of JavaScript
 * document comments to be emitted per tag.
 * <p>
 * The purpose of the API is to clamp emitted output to JavaScript doc tags. The
 * output can be multiline but is specific to one tag. This allows a full
 * comment to be created without worrying about how to assemble the tags.
 * <p>
 * The current tags were found at
 * https://developers.google.com/closure/compiler/docs/js-for-compiler#types
 * <p>
 * TODO (mschmalle) Might make a comment API and tag API so comments are not
 * dependent on tag creation IE IJSDocEmitter and IJSDocTagEmitter
 * 
 * @author Michael Schmalle
 */
public interface IJSGoogDocEmitter extends IJSDocEmitter
{

    void emitInterfaceDoc(IInterfaceNode node, ICompilerProject project);

    void emitInterfaceMemberDoc(IDefinitionNode node, ICompilerProject project);
    
    void emitFieldDoc(IVariableNode node, IDefinition def, ICompilerProject project);

    void emitMethodDoc(IFunctionNode node, ICompilerProject project);

    void emitVarDoc(IVariableNode node, IDefinition def, ICompilerProject project);

    /*
     * https://developers.google.com/closure/compiler/docs/js-for-compiler#types
     *- @const - Marks a variable as read-only. The compiler can inline @const variables
     *
     *- @define - Indicates a constant that can be overridden by the compiler at compile-time.
     *
     * @deprecated - Warns against using the marked function, method, or property should not be used.
     * 
     *- @enum - Specifies the type of an enum. An enum is an object whose properties constitute a 
     *        set of related constants. The @enum tag must be followed by a type expression. 
     *        
     * @export - Declares an exported property. Exported properties will have an alias set up so
     *        they can be accessed via [] syntax.
     *         
     *- @extends - Marks a class or interface as inheriting from another class. A class marked 
     *           with @extends must also be marked with either @constructor or @interface. 
     *           
     *- @implements - Used with @constructor to indicate that a class implements an interface. 
     *
     *- @inheritDoc - tag implies the @override tag.  has exactly the same documentation.
     *
     * @interface - Marks a function as an interface.
     * 
     * @lends
     * 
     * @license|@preserve - Tells the compiler to insert the associated comment before the compiled
     *                      code for the marked file.
     *                      
     * @nosideeffects - Indicates that a call to the declared function has no side effects
     * 
     *- @override - Indicates that a method or property of a subclass intentionally hides a method or 
     *              property of the superclass.
     *              
     * @param - Used with method, function and constructor definitions to specify the types of function 
     *          arguments. 
     *          
     * @private - Marks a member as private. Only code in the same file can access global variables and 
     *            functions marked @private. Constructors marked @private can only be instantiated by code 
     *            in the same file and by their static and instance members. 
     *            
     * @protected - Indicates that a member or property is protected.
     * 
     * @return - Specifies the return types of method and function definitions. The @return tag must be 
     *           followed by a type expression. 
     *           
     * @this - Specifies the type of the object to which the keyword this refers within a function. 
     *         The @this tag must be followed by a type expression. 
     *         
     * @type - Identifies the type of a variable, property, or expression. The @type tag must be 
     *         followed by a type expression. 
     *         
     * @typedef - Declares an alias for a more complex type. 
     */

    void emitConst(IVariableNode node);

    void emitExtends(IClassDefinition superDefinition, String packageName);

    void emitImplements(ITypeDefinition definition, String packageName);

    void emitOverride(IFunctionNode node);

    void emitParam(IParameterNode node, String packageName);

    void emitPublic(IASNode node);

    void emitPrivate(IASNode node);

    void emitProtected(IASNode node);

    void emitInternal(IASNode node);

    void emitReturn(IFunctionNode node, String packageName);

    void emitThis(ITypeDefinition node, String packageName);

    void emitType(IASNode node, String packageName, ICompilerProject project);

	void emitType(String type, String packageName);
}
