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

import java.io.IOException;

import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IConstantDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IInterfaceDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.units.ICompilationUnit;

/**
 * @author Michael Schmalle
 */
public interface IASWalker
{
    // --------------------------------------------------------------------------
    //
    // Methods
    //
    // --------------------------------------------------------------------------

    /**
     * Walks an <code>IASProject</code> and recurses through all nodes in the
     * AST tree.
     * 
     * @param element The <code>IASProject</code>.
     * @throws IOException 
     */
    void walkProject(IASProject element) throws IOException;

    /**
     * Walks an <code>IASCompilationUnit</code> of the <code>IASProject</code>.
     * 
     * @param element The <code>IASCompilationUnit</code> of the
     * <code>IASProject</code>.
     * @throws IOException 
     */
    void walkCompilationUnit(ICompilationUnit element) throws IOException;

    /**
     * Walks an <code>IASPackage</code> of the <code>IASCompilationUnit</code>.
     * 
     * @param element The <code>IASPackage</code> of the
     * <code>IASCompilationUnit</code>.
     */
    void walkPackage(IPackageDefinition element);

    void walkClass(IClassDefinition element);

    void walkInterface(IInterfaceDefinition element);

    void walkNamespace(INamespaceDefinition element);

    void walkFunction(IFunctionDefinition element);

    void walkAccessor(IAccessorDefinition definition);

    void walkVariable(IVariableDefinition element);
    
    void walkConstant(IConstantDefinition element);
}
