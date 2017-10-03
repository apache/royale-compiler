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

package org.apache.royale.compiler.internal.as.codegen;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;

/**
 * A node to use to inject an InstructionList in places that an Expression is expected.
 *
 * This is for use when the compiler can easily synthesize an instruction list for some
 * boiler-plate code gen.  This lets us avoid having to construct a Synthesized AST that would
 * then be run through the code generator to produce the InstructionList.
 *
 * This also means that no semantic checking of the code in the InstructionList will be done.
 * If you are using this, then it is assumed you know what you are doing, and that you are putting
 * correct code into the InstructionList
 */
public class InstructionListNode extends NodeBase implements IExpressionNode
{
    public InstructionListNode(InstructionList il )
    {
        this.il = il;
    }

    private InstructionList il;

    public InstructionList getInstructions()
    {
        return il;
    }

    public InstructionListNode copyForInitializer (IScopedNode scopedNode)
    {
        return new InstructionListNode(il);
    }

    // Implement IExpressionNode methods below

    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return false;
    }

    /**
     * Determines whether this expression is surrounded by parenthesis.  Nested parenthesis are reduced to the smallest number needed
     * to maintain code meaning
     *
     * @return true if surrounded by parenthesis
     */
    @Override
    public boolean hasParenthesis()
    {
        return false;
    }

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        // TODO: allow this to be parameterized by the ctor
        return null;
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        // TODO: allow this to be parameterized by the ctor
        return null;
    }

    /**
     * Get the opcode of this node
     *
     * @return the opcode - this is one of the constants defined in ASTConstants
     */
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.InstructionListID;
    }

    /**
     * Get a particular child of this node
     *
     * @param i the child's index
     * @return the specified child
     */
    @Override
    public IASNode getChild(int i)
    {
        // Has no children
        return null;
    }

    /**
     * Get the number of children
     *
     * @return the number of children
     */
    @Override
    public int getChildCount()
    {
        // Has no children
        return 0;
    }
}
