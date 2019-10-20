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

package org.apache.royale.compiler.internal.codegen.as;

import java.io.FilterWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.IEmitter;
import org.apache.royale.compiler.codegen.IEmitterTokens;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.royale.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.as.IContainerNode.ContainerType;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.utils.ASNodeUtils;
import org.apache.royale.compiler.visitor.IBlockWalker;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;

/**
 * The base implementation for an ActionScript emitter.
 * 
 * @author Michael Schmalle
 */
public class ASEmitter implements IASEmitter, IEmitter
{
    private final FilterWriter out;

    private IEmitter parentEmitter;

    public IEmitter getParentEmitter()
    {
        return parentEmitter;
    }

    public void setParentEmitter(IEmitter value)
    {
        parentEmitter = value;
    }

    private boolean bufferWrite;

    protected boolean isBufferWrite()
    {
        return bufferWrite;
    }

    public void setBufferWrite(boolean value)
    {
        bufferWrite = value;
    }

    private StringBuilder builder;

    public StringBuilder getBuilder()
    {
        return builder;
    }

    public void setBuilder(StringBuilder sb)
    {
        builder = sb;
    }
    
    protected void flushBuilder()
    {
        setBufferWrite(false);
        write(builder.toString());
        builder.setLength(0);
    }

    // (mschmalle) think about how this should be implemented, we can add our
    // own problems to this, they don't just have to be parse problems
    public List<ICompilerProblem> getProblems()
    {
        return walker.getErrors();
    }

    private int currentIndent = 0;

    protected int getCurrentIndent()
    {
        return currentIndent;
    }

    protected void writeIndent()
    {
        write(ASEmitterTokens.INDENT);
    }

    private IASBlockWalker walker;

    @Override
    public IBlockWalker getWalker()
    {
        return walker;
    }

    @Override
    public void setWalker(IBlockWalker value)
    {
        walker = (IASBlockWalker) value;
    }

    @Override
    public IDocEmitter getDocEmitter()
    {
        return null;
    }

    @Override
    public void setDocEmitter(IDocEmitter value)
    {
    }
    
    private int currentLine = 0;

    protected int getCurrentLine()
    {
        return currentLine;
    }

    private int currentColumn = 0;

    protected int getCurrentColumn()
    {
        return currentColumn;
    }

    public ASEmitter(FilterWriter out)
    {
        this.out = out;
        builder = new StringBuilder();
    }

    @Override
    public String postProcess(String output)
    {
    	return output;
    }
    
    @Override
    public void write(IEmitterTokens value)
    {
        write(value.getToken());
    }

    @Override
    public void write(String value)
    {
        try
        {
            if (!bufferWrite)
            {
                if (parentEmitter != null)
                {
                    parentEmitter.write(value);
                }
                else
                {
                    int newLineCount = value.length() - value.replace("\n", "").length();
                    currentLine += newLineCount;
                    if (newLineCount > 0)
                    {
                        currentColumn = value.length() - value.lastIndexOf("\n") - 1;
                    }
                    else
                    {
                        currentColumn += value.length();
                    }
                    out.write(value);
                }
            }
            else
            {
                builder.append(value);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(ASEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    @Override
    public void indentPush()
    {
        currentIndent++;
    }

    @Override
    public void indentPop()
    {
        currentIndent--;
    }

    @Override
    public void writeNewline()
    {
        write(ASEmitterTokens.NEW_LINE);
        write(getIndent(currentIndent));
    }

    @Override
    public void writeNewline(IEmitterTokens value)
    {
        writeNewline(value.getToken());
    }

    @Override
    public void writeNewline(String value)
    {
        write(value);
        writeNewline();
    }

    @Override
    public void writeNewline(IEmitterTokens value, boolean pushIndent)
    {
        writeNewline(value.getToken(), pushIndent);
    }

    @Override
    public void writeNewline(String value, boolean pushIndent)
    {
        if (pushIndent)
            indentPush();
        else
            indentPop();
        write(value);
        writeNewline();
    }

    public void writeSymbol(String value)
    {
        write(value);
    }

    @Override
    public void writeToken(IEmitterTokens value)
    {
        writeToken(value.getToken());
    }

    @Override
    public void writeToken(String value)
    {
        write(value);
        write(ASEmitterTokens.SPACE);
    }

    //--------------------------------------------------------------------------
    // IPackageNode
    //--------------------------------------------------------------------------

    @Override
    public void emitImport(IImportNode node)
    {
        IImportTarget target = node.getImportTarget();
        writeToken(ASEmitterTokens.IMPORT);
        write(target.toString());
    }

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        write(ASEmitterTokens.PACKAGE);

        IPackageNode node = definition.getNode();
        String name = node.getQualifiedName();
        if (name != null && !name.equals(""))
        {
            write(ASEmitterTokens.SPACE);
            getWalker().walk(node.getNameExpressionNode());
        }

        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.BLOCK_OPEN);
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
    }

    @Override
    public void emitPackageContents(IPackageDefinition definition)
    {
        IPackageNode node = definition.getNode();
        ITypeNode tnode = EmitterUtils.findTypeNode(node);
        if (tnode != null)
        {
            indentPush();
            writeNewline();
            getWalker().walk(tnode); // IClassNode | IInterfaceNode
        }
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        indentPop();
        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        writeToken(node.getNamespace());

        if (node.hasModifier(ASModifier.FINAL))
        {
            writeToken(ASEmitterTokens.FINAL);
        }
        else if (node.hasModifier(ASModifier.DYNAMIC))
        {
            writeToken(ASEmitterTokens.DYNAMIC);
        }

        writeToken(ASEmitterTokens.CLASS);
        getWalker().walk(node.getNameExpressionNode());
        write(ASEmitterTokens.SPACE);

        IExpressionNode bnode = node.getBaseClassExpressionNode();
        if (bnode != null)
        {
            writeToken(ASEmitterTokens.EXTENDS);
            getWalker().walk(bnode);
            write(ASEmitterTokens.SPACE);
        }

        IExpressionNode[] inodes = node.getImplementedInterfaceNodes();
        final int ilen = inodes.length;
        if (ilen != 0)
        {
            writeToken(ASEmitterTokens.IMPLEMENTS);
            for (int i = 0; i < ilen; i++)
            {
                getWalker().walk(inodes[i]);
                if (i < ilen - 1)
                {
                    writeToken(ASEmitterTokens.COMMA);
                }
            }
            write(ASEmitterTokens.SPACE);
        }

        write(ASEmitterTokens.BLOCK_OPEN);

        // fields, methods, namespaces
        final IDefinitionNode[] members = node.getAllMemberNodes();
        if (members.length > 0)
        {
            indentPush();
            writeNewline();

            final int len = members.length;
            int i = 0;
            for (IDefinitionNode mnode : members)
            {
                getWalker().walk(mnode);
                if (mnode.getNodeID() == ASTNodeID.VariableID)
                {
                    write(ASEmitterTokens.SEMICOLON);
                    if (i < len - 1)
                        writeNewline();
                }
                else if (mnode.getNodeID() == ASTNodeID.FunctionID)
                {
                    if (i < len - 1)
                        writeNewline();
                }
                else if (mnode.getNodeID() == ASTNodeID.GetterID
                        || mnode.getNodeID() == ASTNodeID.SetterID)
                {
                    if (i < len - 1)
                        writeNewline();
                }
                i++;
            }

            indentPop();
        }

        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        writeToken(node.getNamespace());

        writeToken(ASEmitterTokens.INTERFACE);
        getWalker().walk(node.getNameExpressionNode());
        write(ASEmitterTokens.SPACE);

        IExpressionNode[] inodes = node.getExtendedInterfaceNodes();
        final int ilen = inodes.length;
        if (ilen != 0)
        {
            writeToken(ASEmitterTokens.EXTENDS);
            for (int i = 0; i < ilen; i++)
            {
                getWalker().walk(inodes[i]);
                if (i < ilen - 1)
                {
                    writeToken(ASEmitterTokens.COMMA);
                }
            }
            write(ASEmitterTokens.SPACE);
        }

        write(ASEmitterTokens.BLOCK_OPEN);

        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        if (members.length > 0)
        {
            indentPush();
            writeNewline();

            final int len = members.length;
            int i = 0;
            for (IDefinitionNode mnode : members)
            {
                getWalker().walk(mnode);
                write(ASEmitterTokens.SEMICOLON);
                if (i < len - 1)
                    writeNewline();
                i++;
            }

            indentPop();
        }

        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitVarDeclaration(IVariableNode node)
    {
        if (!(node instanceof ChainedVariableNode))
        {
            emitMemberKeyword(node);
        }

        emitDeclarationName(node);
        emitType(node.getVariableTypeNode());
        
        IExpressionNode avnode = node.getAssignedValueNode();
        if (avnode != null)
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            emitAssignedValue(avnode);
        }

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    writeToken(ASEmitterTokens.COMMA);
                    emitVarDeclaration((IVariableNode) child);
                }
            }
        }

        // the client such as IASBlockWalker is responsible for the 
        // semi-colon and newline handling
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitFieldDocumentation(IVariableNode node)
    {
    }

    @Override
    public void emitField(IVariableNode node)
    {
        emitFieldDocumentation(node);

        IVariableDefinition definition = (IVariableDefinition) node
                .getDefinition();

        if (!(node instanceof ChainedVariableNode))
        {
            emitNamespaceIdentifier(node);
            emitModifiers(definition);
            emitMemberKeyword(node);
        }

        emitMemberName(node);
        emitType(node.getVariableTypeNode());

        IExpressionNode avnode = node.getAssignedValueNode();
        if (avnode != null)
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.EQUAL);
            emitAssignedValue(avnode);
        }

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    writeToken(ASEmitterTokens.COMMA);
                    emitField((IVariableNode) child);
                }
            }
        }

        // the client such as IASBlockWalker is responsible for the 
        // semi-colon and newline handling
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitMethodDocumentation(IFunctionNode node)
    {
    }

    @Override
    public void emitMethod(IFunctionNode node)
    {
        // see below, this is temp, I don't want a bunch of duplicated code
        // at them moment, subclasses can refine anyways, we are generalizing
        if (node instanceof IGetterNode)
        {
            emitGetAccessorDocumentation((IGetterNode) node);
        }
        else if (node instanceof ISetterNode)
        {
            emitSetAccessorDocumentation((ISetterNode) node);
        }
        else
        {
            emitMethodDocumentation(node);
        }

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        IFunctionDefinition definition = node.getDefinition();

        emitNamespaceIdentifier(node);
        emitModifiers(definition);
        emitMemberKeyword(node);

        // I'm cheating right here, I haven't "seen" the light
        // on how to properly and efficiently deal with accessors since they are SO alike
        // I don't want to lump them in with methods because implementations in the
        // future need to know the difference without loopholes
        if (node instanceof IAccessorNode)
        {
            emitAccessorKeyword(((IAccessorNode) node).getAccessorKeywordNode());
        }

        emitMemberName(node);
        emitParameters(node.getParametersContainerNode());
        emitType(node.getReturnTypeNode());
        if (node.getParent().getParent().getNodeID() == ASTNodeID.ClassID)
        {
            emitMethodScope(node.getScopedNode());
        }

        // the client such as IASBlockWalker is responsible for the 
        // semi-colon and newline handling
    }

    @Override
    public void emitGetAccessorDocumentation(IGetterNode node)
    {
    }

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
        // just cheat for now, IGetterNode is a IFunctionNode
        emitMethod(node);
    }

    @Override
    public void emitSetAccessorDocumentation(ISetterNode node)
    {
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        // just cheat for now, ISetterNode is a IFunctionNode
        emitMethod(node);
    }

    @Override
    public void emitLocalNamedFunction(IFunctionNode node)
    {
        FunctionNode fnode = (FunctionNode) node;
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.SPACE);
        write(fnode.getName());
        emitParameters(fnode.getParametersContainerNode());
        emitType(fnode.getTypeNode());
        emitFunctionScope(fnode.getScopedNode());
    }

    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
        FunctionNode fnode = node.getFunctionNode();
        write(ASEmitterTokens.FUNCTION);
        emitParameters(fnode.getParametersContainerNode());
        emitType(fnode.getTypeNode());
        emitFunctionScope(fnode.getScopedNode());
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitNamespace(INamespaceNode node)
    {
        emitNamespaceIdentifier(node);
        writeToken(ASEmitterTokens.NAMESPACE);
        emitMemberName(node);
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        getWalker().walk(node.getNamespaceURINode());
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    protected void emitNamespaceIdentifier(IDefinitionNode node)
    {
        String namespace = node.getNamespace();
        if (namespace != null
                && !namespace.equals(IASKeywordConstants.INTERNAL))
        {
            writeToken(namespace);
        }
    }

    protected void emitModifiers(IDefinition definition)
    {
        ModifiersSet modifierSet = definition.getModifiers();
        if (modifierSet.hasModifiers())
        {
            for (ASModifier modifier : modifierSet.getAllModifiers())
            {
                writeToken(modifier.toString());
            }
        }
    }

    public void emitMemberKeyword(IDefinitionNode node)
    {
        if (node instanceof IFunctionNode)
        {
            writeToken(ASEmitterTokens.FUNCTION);
        }
        else if (node instanceof IVariableNode)
        {
            writeToken(((IVariableNode) node).isConst() ? ASEmitterTokens.CONST
                    : ASEmitterTokens.VAR);
        }
    }

    protected void emitMemberName(IDefinitionNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    public void emitDeclarationName(IDefinitionNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    public void emitParameters(IContainerNode node)
    {
        write(ASEmitterTokens.PAREN_OPEN);
        int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IParameterNode parameterNode = (IParameterNode) node.getChild(i);
            getWalker().walk(parameterNode); //emitParameter
            if (i < len - 1)
            {
                writeToken(ASEmitterTokens.COMMA);
            }
        }
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        if (node.isRest())
        {
            write(ASEmitterTokens.ELLIPSIS);
            write(node.getName());
        }
        else
        {
            getWalker().walk(node.getNameExpressionNode());
            write(ASEmitterTokens.COLON);
            getWalker().walk(node.getVariableTypeNode());
            IExpressionNode anode = node.getAssignedValueNode();
            if (anode != null)
            {
                write(ASEmitterTokens.SPACE);
                writeToken(ASEmitterTokens.EQUAL);
                getWalker().walk(anode);
            }
        }
    }

    protected void emitType(IExpressionNode node)
    {
        // TODO (mschmalle) node.getVariableTypeNode() will return "*" if undefined, what to use?
        // or node.getReturnTypeNode()
        if (node != null)
        {
            write(ASEmitterTokens.COLON);
            getWalker().walk(node);
        }
    }

    protected void emitAssignedValue(IExpressionNode node)
    {
        if (node == null)
        {
            return;
        }
        getWalker().walk(node);
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        // nothing to do in AS
    }

    public void emitMethodScope(IScopedNode node)
    {
        write(ASEmitterTokens.SPACE);
        if (node instanceof IContainerNode)
        {
            IContainerNode container = (IContainerNode) node;
            //native or abstract methods may have a synthesized scope block
            if (container.getContainerType().equals(ContainerType.SYNTHESIZED))
            {
                write(ASEmitterTokens.BLOCK_OPEN);
                writeNewline();
                write(ASEmitterTokens.BLOCK_CLOSE);
                return;
            }
        }
        getWalker().walk(node);
    }

    protected void emitAccessorKeyword(IKeywordNode node)
    {
        getWalker().walk(node);
        write(ASEmitterTokens.SPACE);
    }

    protected void emitFunctionScope(IScopedNode node)
    {
        emitMethodScope(node);
    }

    //--------------------------------------------------------------------------
    // Statements
    //--------------------------------------------------------------------------

    @Override
    public void emitStatement(IASNode node)
    {
        getWalker().walk(node);
        // XXX (mschmalle) this should be in the after handler?
        if (node.getParent().getNodeID() != ASTNodeID.LabledStatementID
        		&& node.getNodeID() != ASTNodeID.ConfigBlockID
                && !(node instanceof IStatementNode))
        {
            write(ASEmitterTokens.SEMICOLON);
        }

        if (!isLastStatement(node))
            writeNewline();
    }

    @Override
    public void emitIf(IIfNode node)
    {
        IConditionalNode conditional = (IConditionalNode) node.getChild(0);

        IContainerNode xnode = (IContainerNode) conditional
                .getStatementContentsNode();

        writeToken(ASEmitterTokens.IF);
        //write(SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(conditional.getChild(0)); // conditional expression
        write(ASEmitterTokens.PAREN_CLOSE);
        if (!isImplicit(xnode))
            write(ASEmitterTokens.SPACE);

        getWalker().walk(conditional.getChild(1)); // BlockNode
        IConditionalNode[] nodes = node.getElseIfNodes();
        if (nodes.length > 0)
        {
            for (int i = 0; i < nodes.length; i++)
            {
                IConditionalNode enode = nodes[i];
                IContainerNode snode = (IContainerNode) enode
                        .getStatementContentsNode();

                final boolean isImplicit = isImplicit(snode);
                if (isImplicit)
                    writeNewline();
                else
                    write(ASEmitterTokens.SPACE);

                writeToken(ASEmitterTokens.ELSE);
                writeToken(ASEmitterTokens.IF);
                write(ASEmitterTokens.PAREN_OPEN);
                getWalker().walk(enode.getChild(0));
                write(ASEmitterTokens.PAREN_CLOSE);
                if (!isImplicit)
                    write(ASEmitterTokens.SPACE);

                getWalker().walk(enode.getChild(1)); // ConditionalNode
            }
        }

        ITerminalNode elseNode = node.getElseNode();
        if (elseNode != null)
        {
            IContainerNode cnode = (IContainerNode) elseNode.getChild(0);
            // if an implicit if, add a newline with no space
            final boolean isImplicit = isImplicit(cnode);
            if (isImplicit)
                writeNewline();
            else
                write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.ELSE);
            if (!isImplicit)
                write(ASEmitterTokens.SPACE);

            getWalker().walk(elseNode); // TerminalNode
        }
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);
        writeToken(ASEmitterTokens.FOR);
        writeToken(ASEmitterTokens.EACH);
        write(ASEmitterTokens.PAREN_OPEN);

        IContainerNode cnode = node.getConditionalsContainerNode();
        getWalker().walk(cnode.getChild(0));

        write(ASEmitterTokens.PAREN_CLOSE);
        if (!isImplicit(xnode))
            write(ASEmitterTokens.SPACE);

        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitForLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);

        writeToken(ASEmitterTokens.FOR);
        write(ASEmitterTokens.PAREN_OPEN);

        IContainerNode cnode = node.getConditionalsContainerNode();
        final IASNode node0 = cnode.getChild(0);
        if (node0.getNodeID() == ASTNodeID.Op_InID)
        {
            getWalker().walk(cnode.getChild(0));
        }
        else
        {
            visitForBody(cnode);
        }

        write(ASEmitterTokens.PAREN_CLOSE);
        if (!isImplicit(xnode))
            write(ASEmitterTokens.SPACE);

        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitSwitch(ISwitchNode node)
    {
        writeToken(ASEmitterTokens.SWITCH);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getChild(0));
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        writeNewline(ASEmitterTokens.BLOCK_OPEN, true);

        IConditionalNode[] cnodes = ASNodeUtils.getCaseNodes(node);
        ITerminalNode dnode = ASNodeUtils.getDefaultNode(node);

        for (int i = 0; i < cnodes.length; i++)
        {
            IConditionalNode casen = cnodes[i];
            IContainerNode cnode = (IContainerNode) casen.getChild(1);
            writeToken(ASEmitterTokens.CASE);
            getWalker().walk(casen.getConditionalExpressionNode());
            write(ASEmitterTokens.COLON);
            if (!isImplicit(cnode))
                write(ASEmitterTokens.SPACE);
            getWalker().walk(casen.getStatementContentsNode());
            if (i == cnodes.length - 1 && dnode == null)
            {
                indentPop();
                writeNewline();
            }
            else
                writeNewline();
        }
        if (dnode != null)
        {
            IContainerNode cnode = (IContainerNode) dnode.getChild(0);
            write(ASEmitterTokens.DEFAULT);
            write(ASEmitterTokens.COLON);
            if (!isImplicit(cnode))
                write(ASEmitterTokens.SPACE);
            getWalker().walk(dnode);
            indentPop();
            writeNewline();
        }
        write(ASEmitterTokens.BLOCK_CLOSE);
    }

    @Override
    public void emitWhileLoop(IWhileLoopNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(1);
        writeToken(ASEmitterTokens.WHILE);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getConditionalExpressionNode());
        write(ASEmitterTokens.PAREN_CLOSE);
        if (!isImplicit(cnode))
            write(ASEmitterTokens.SPACE);
        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitDoLoop(IWhileLoopNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(0);
        write(ASEmitterTokens.DO);
        if (!isImplicit(cnode))
            write(ASEmitterTokens.SPACE);
        getWalker().walk(node.getStatementContentsNode());
        if (!isImplicit(cnode))
            write(ASEmitterTokens.SPACE);
        else
            writeNewline(); // TODO (mschmalle) there is something wrong here, block should NL
        write(ASEmitterTokens.WHILE);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getConditionalExpressionNode());
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
    }

    @Override
    public void emitWith(IWithNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(1);
        writeToken(ASEmitterTokens.WITH);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getTargetNode());
        write(ASEmitterTokens.PAREN_CLOSE);
        if (!isImplicit(cnode))
            write(ASEmitterTokens.SPACE);
        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitThrow(IThrowNode node)
    {
        writeToken(ASEmitterTokens.THROW);
        getWalker().walk(node.getThrownExpressionNode());
    }

    @Override
    public void emitTry(ITryNode node)
    {
        writeToken(ASEmitterTokens.TRY);
        getWalker().walk(node.getStatementContentsNode());
        for (int i = 0; i < node.getCatchNodeCount(); i++)
        {
            getWalker().walk(node.getCatchNode(i));
        }
        ITerminalNode fnode = node.getFinallyNode();
        if (fnode != null)
        {
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.FINALLY);
            getWalker().walk(fnode);
        }
    }

    @Override
    public void emitCatch(ICatchNode node)
    {
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.CATCH);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getCatchParameterNode());
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitReturn(IReturnNode node)
    {
        write(ASEmitterTokens.RETURN);
        IExpressionNode rnode = node.getReturnValueNode();
        if (rnode != null && rnode.getNodeID() != ASTNodeID.NilID)
        {
            write(ASEmitterTokens.SPACE);
            getWalker().walk(rnode);
        }
    }

    //--------------------------------------------------------------------------
    // Expressions
    //--------------------------------------------------------------------------

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        if (node.isNewExpression())
        {
            writeToken(ASEmitterTokens.NEW);
        }

        getWalker().walk(node.getNameNode());
        
        emitArguments(node.getArgumentsNode());
    }

    @Override
    public void emitArguments(IContainerNode node)
    {
        write(ASEmitterTokens.PAREN_OPEN);
        int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IExpressionNode argumentNode = (IExpressionNode) node.getChild(i);
            getWalker().walk(argumentNode);
            if (i < len - 1)
            {
                writeToken(ASEmitterTokens.COMMA);
            }
        }
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    //--------------------------------------------------------------------------
    // Operators
    //--------------------------------------------------------------------------

    @Override
    public void emitAsOperator(IBinaryOperatorNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(ASEmitterTokens.SPACE);
        writeToken(node.getOperator().getOperatorText());
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitIsOperator(IBinaryOperatorNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(ASEmitterTokens.SPACE);
        writeToken(node.getOperator().getOperatorText());
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getLeftOperandNode());
        if (node.getNodeID() != ASTNodeID.Op_CommaID)
            write(ASEmitterTokens.SPACE);
        writeToken(node.getOperator().getOperatorText());
        getWalker().walk(node.getRightOperandNode());
        if (ASNodeUtils.hasParenClose(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

    //--------------------------------------------------------------------------
    // Utility
    //--------------------------------------------------------------------------

    protected ITypeNode findTypeNode(IPackageNode node)
    {
        IScopedNode scope = node.getScopedNode();
        for (int i = 0; i < scope.getChildCount(); i++)
        {
            IASNode child = scope.getChild(i);
            if (child instanceof ITypeNode)
                return (ITypeNode) child;
        }
        return null;
    }

    protected ITypeDefinition findType(Collection<IDefinition> definitions)
    {
        for (IDefinition definition : definitions)
        {
            if (definition instanceof ITypeDefinition)
                return (ITypeDefinition) definition;
        }
        return null;
    }

    //--------------------------------------------------------------------------
    // Static Utility
    //--------------------------------------------------------------------------

    protected static IFunctionNode getConstructor(IDefinitionNode[] members)
    {
        for (IDefinitionNode node : members)
        {
            if (node instanceof IFunctionNode)
            {
                IFunctionNode fnode = (IFunctionNode) node;
                if (fnode.isConstructor())
                    return fnode;
            }
        }
        return null;
    }

    protected static boolean isLastStatement(IASNode node)
    {
        return getChildIndex(node.getParent(), node) == node.getParent()
                .getChildCount() - 1;
    }

    // this is not fair that we have to do this if (i < len - 1)
    private static int getChildIndex(IASNode parent, IASNode node)
    {
        final int len = parent.getChildCount();
        for (int i = 0; i < len; i++)
        {
            if (parent.getChild(i) == node)
                return i;
        }
        return -1;
    }

    protected static final boolean isImplicit(IContainerNode node)
    {
        return EmitterUtils.isImplicit(node);
    }

    protected void visitForBody(IContainerNode node)
    {
        final IASNode node0 = node.getChild(0);
        final IASNode node1 = node.getChild(1);
        final IASNode node2 = node.getChild(2);

        // initializer
        if (node0 != null)
        {
            getWalker().walk(node0);
            write(ASEmitterTokens.SEMICOLON);
            if (node1.getNodeID() != ASTNodeID.NilID)
                write(ASEmitterTokens.SPACE);
        }
        // condition or target
        if (node1 != null)
        {
            getWalker().walk(node1);
            write(ASEmitterTokens.SEMICOLON);
            if (node2.getNodeID() != ASTNodeID.NilID)
                write(ASEmitterTokens.SPACE);
        }
        // iterator
        if (node2 != null)
        {
            getWalker().walk(node2);
        }
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        write(node.getValue(true));
    }

    @Override
    public void emitLiteralContainer(ILiteralContainerNode node)
    {
        final ContainerNode cnode = node.getContentsNode();
        final ContainerType type = cnode.getContainerType();
        String postFix = "";

        if (type == ContainerType.BRACES)
        {
            write(ASEmitterTokens.BLOCK_OPEN);
            postFix = ASEmitterTokens.BLOCK_CLOSE.getToken();
        }
        else if (type == ContainerType.BRACKETS)
        {
            write(ASEmitterTokens.SQUARE_OPEN);
            postFix = ASEmitterTokens.SQUARE_CLOSE.getToken();
        }
        else if (type == ContainerType.IMPLICIT)
        {
            // nothing to write, move along
        }
        else if (type == ContainerType.PARENTHESIS)
        {
            write(ASEmitterTokens.PAREN_OPEN);
            postFix = ASEmitterTokens.PAREN_CLOSE.getToken();
        }

        final int len = cnode.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = cnode.getChild(i);
            getWalker().walk(child);
            if (i < len - 1)
                writeToken(ASEmitterTokens.COMMA);
        }

        if (postFix != "")
            write(postFix);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        write(node.getName());
    }

    @Override
    public void emitNumericLiteral(INumericLiteralNode node)
    {
        write(node.getNumericValue().toString());
    }

    @Override
    public void emitKeyword(IKeywordNode node)
    {
        write(node.getNodeID().getParaphrase());
    }

    @Override
    public void emitIterationFlow(IIterationFlowNode node)
    {
        write(node.getKind().toString().toLowerCase());
        IIdentifierNode lnode = node.getLabelNode();
        if (lnode != null)
        {
            write(ASEmitterTokens.SPACE);
            getWalker().walk(lnode);
        }
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(node.getOperator().getOperatorText());
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitDynamicAccess(IDynamicAccessNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(ASEmitterTokens.SQUARE_OPEN);
        getWalker().walk(node.getRightOperandNode());
        write(ASEmitterTokens.SQUARE_CLOSE);
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        getWalker().walk(node.getCollectionNode());
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(ASEmitterTokens.LESS_THAN);
        getWalker().walk(node.getTypeNode());
        write(ASEmitterTokens.GREATER_THAN);
    }

    @Override
    public void emitVariableExpression(IVariableExpressionNode node)
    {
        getWalker().walk(node.getTargetVariable());
    }

    @Override
    public void emitTernaryOperator(ITernaryOperatorNode node)
    {
    	if (ASNodeUtils.hasParenOpen((IOperatorNode) node))
    		write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(node.getConditionalNode());
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.TERNARY);
        getWalker().walk(node.getLeftOperandNode());
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.COLON);
        getWalker().walk(node.getRightOperandNode());
        if (ASNodeUtils.hasParenClose((IOperatorNode) node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitObjectLiteralValuePair(IObjectLiteralValuePairNode node)
    {
        getWalker().walk(node.getNameNode());
        write(ASEmitterTokens.COLON);
        getWalker().walk(node.getValueNode());
    }

    @Override
    public void emitLabelStatement(LabeledStatementNode node)
    {
        writeToken(node.getLabel());
        writeToken(ASEmitterTokens.COLON);
        getWalker().walk(node.getLabeledStatement());
    }

    @Override
    public void emitNamespaceAccessExpression(
            INamespaceAccessExpressionNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(node.getOperator().getOperatorText());
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitUnaryOperator(IUnaryOperatorNode node)
    {
        if (ASNodeUtils.hasParenOpen(node))
            write(ASEmitterTokens.PAREN_OPEN);

        if (node.getNodeID() == ASTNodeID.Op_PreIncrID
                || node.getNodeID() == ASTNodeID.Op_PreDecrID
                || node.getNodeID() == ASTNodeID.Op_BitwiseNotID
                || node.getNodeID() == ASTNodeID.Op_LogicalNotID
                || node.getNodeID() == ASTNodeID.Op_SubtractID
                || node.getNodeID() == ASTNodeID.Op_AddID)
        {
            write(node.getOperator().getOperatorText());
            IExpressionNode opNode = node.getOperandNode();
            getWalker().walk(opNode);
        }

        else if (node.getNodeID() == ASTNodeID.Op_PostIncrID
                || node.getNodeID() == ASTNodeID.Op_PostDecrID)
        {
            getWalker().walk(node.getOperandNode());
            write(node.getOperator().getOperatorText());
        }
        else if (node.getNodeID() == ASTNodeID.Op_DeleteID
                || node.getNodeID() == ASTNodeID.Op_VoidID)
        {
            writeToken(node.getOperator().getOperatorText());
            getWalker().walk(node.getOperandNode());
        }
        else if (node.getNodeID() == ASTNodeID.Op_TypeOfID)
        {
            write(node.getOperator().getOperatorText());
            write(ASEmitterTokens.PAREN_OPEN);
            getWalker().walk(node.getOperandNode());
            write(ASEmitterTokens.PAREN_CLOSE);
        }

        if (ASNodeUtils.hasParenClose(node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitLanguageIdentifier(ILanguageIdentifierNode node)
    {
        if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.ANY_TYPE)
        {
            write(ASEmitterTokens.ANY_TYPE);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.REST)
        {
            write(ASEmitterTokens.ELLIPSIS);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.SUPER)
        {
            write(ASEmitterTokens.SUPER);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS)
        {
            write(ASEmitterTokens.THIS);
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.VOID)
        {
            write(ASEmitterTokens.VOID);
        }
    }

    @Override
    public void emitMetaTag(IMetaTagNode node)
    {
    }

    @Override
    public void emitEmbed(IEmbedNode node)
    {
    }

    @Override
    public void emitContainer(IContainerNode node)
    {
    }

    @Override
    public void emitE4XFilter(IMemberAccessExpressionNode node)
    {
        // ToDo (erikdebruin)
    }
    
    @Override
    public void emitE4XDefaultNamespaceDirective(IDefaultXMLNamespaceNode node)
    {
    }

    @Override
    public void emitUseNamespace(IUseNamespaceNode node)
    {
        // ToDo (erikdebruin)
    }

    @Override
    public void emitBlockOpen(IContainerNode node)
    {
        write(ASEmitterTokens.BLOCK_OPEN);
    }

    @Override
    public void emitBlockClose(IContainerNode node)
    {
        write(ASEmitterTokens.BLOCK_CLOSE);
    }

    @Override
    public String stringifyNode(IASNode node)
    {
        boolean oldBufferWrite = isBufferWrite();
        StringBuilder oldBuilder = this.builder;
        this.builder = new StringBuilder();
        setBufferWrite(true);
        getWalker().walk(node);
        String result = getBuilder().toString();
        getBuilder().setLength(0);
        this.builder = oldBuilder;
        setBufferWrite(oldBufferWrite);
        return result;
    }
}
