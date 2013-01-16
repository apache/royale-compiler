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

package org.apache.flex.compiler.internal.as.codegen;

import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.as.codegen.IASEmitter;
import org.apache.flex.compiler.as.codegen.IDocEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ModifiersSet;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.internal.tree.as.ChainedVariableNode;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.FunctionObjectNode;
import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IBlockNode;
import org.apache.flex.compiler.tree.as.ICatchNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IConditionalNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IIfNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.IIterationFlowNode;
import org.apache.flex.compiler.tree.as.IKeywordNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.INamespaceNode;
import org.apache.flex.compiler.tree.as.INumericLiteralNode;
import org.apache.flex.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.IScopedNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.IStatementNode;
import org.apache.flex.compiler.tree.as.ISwitchNode;
import org.apache.flex.compiler.tree.as.ITerminalNode;
import org.apache.flex.compiler.tree.as.ITernaryOperatorNode;
import org.apache.flex.compiler.tree.as.IThrowNode;
import org.apache.flex.compiler.tree.as.ITryNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.as.IContainerNode.ContainerType;
import org.apache.flex.compiler.tree.as.IWhileLoopNode;
import org.apache.flex.compiler.tree.as.IWithNode;
import org.apache.flex.compiler.visitor.IASBlockWalker;

/**
 * The base implementation for an ActionScript emitter.
 * 
 * @author Michael Schmalle
 */
public class ASEmitter implements IASEmitter
{
    private static final String SPACE = " ";

    protected static final String NL = "\n";

    protected static final String INDENT_STRING = "\t";

    private final FilterWriter out;

    List<ICompilerProblem> problems;

    // (mschmalle) think about how this should be implemented, we can add our
    // own problems to this, they don't just have to be parse problems
    public List<ICompilerProblem> getProblems()
    {
        return problems;
    }

    private IDocEmitter docEmitter;

    @Override
    public IDocEmitter getDocEmitter()
    {
        return docEmitter;
    }

    @Override
    public void setDocEmitter(IDocEmitter value)
    {
        docEmitter = value;
    }

    private int currentIndent = 0;

    protected int getCurrentIndent()
    {
        return currentIndent;
    }

    private IASBlockWalker walker;

    @Override
    public IASBlockWalker getWalker()
    {
        return walker;
    }

    @Override
    public void setWalker(IASBlockWalker value)
    {
        walker = value;
    }

    public ASEmitter(FilterWriter out)
    {
        this.out = out;
        problems = new ArrayList<ICompilerProblem>();
    }

    @Override
    public void write(String value)
    {
        try
        {
            out.write(value);

            final StringBuilder sb = new StringBuilder();
            if (value.indexOf(NL) != -1)
            {
                for (int i = 0; i < currentIndent; i++)
                    sb.append(INDENT_STRING);

                out.write(sb.toString());
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
            sb.append(INDENT_STRING);
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

    // (erikdebruin) I needed a way to add a semi-colon after the closing curly
    //               bracket of a block in the 'goog'-ified output. Instead of 
    // 				 subclassing 'ASAfterNodeStrategy' and  copying
    //               the entire function body, I thought I might use this little
    //               utility method and override that. Am I doing it right?
    public void writeBlockClose()
    {
        write("}");
    }

    public void writeIndent()
    {
        String indent = "";
        for (int i = 0; i < currentIndent; i++)
            indent += INDENT_STRING;
        write(indent);
    }

    public void writeNewline()
    {
        write(NL);
    }

    public void writeToken(String value)
    {
        write(value);
    }

    public void writeSymbol(String value)
    {
        write(value);
    }

    //--------------------------------------------------------------------------
    // IPackageNode
    //--------------------------------------------------------------------------

    @Override
    public void emitPackageHeader(IPackageNode node)
    {
        writeToken(IASKeywordConstants.PACKAGE);

        String name = node.getQualifiedName();
        if (name != null && !name.equals(""))
        {
            write(SPACE);
            getWalker().walk(node.getNameExpressionNode());
        }

        write(SPACE);
        write("{");
    }

    @Override
    public void emitPackageHeaderContents(IPackageNode node)
    {
    }

    @Override
    public void emitPackageContents(IPackageNode node)
    {
        ITypeNode tnode = findTypeNode(node);
        if (tnode != null)
        {
            indentPush();
            write(NL);
            getWalker().walk(tnode); // IClassNode | IInterfaceNode
        }
    }

    @Override
    public void emitPackageFooter(IPackageNode node)
    {
        indentPop();
        write(NL);
        write("}");
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitClass(IClassNode node)
    {
        write(node.getNamespace());
        write(SPACE);

        if (node.hasModifier(ASModifier.FINAL))
        {
            writeToken(IASKeywordConstants.FINAL);
            write(SPACE);
        }
        else if (node.hasModifier(ASModifier.DYNAMIC))
        {
            writeToken(IASKeywordConstants.DYNAMIC);
            write(SPACE);
        }

        writeToken(IASKeywordConstants.CLASS);
        write(SPACE);
        getWalker().walk(node.getNameExpressionNode());
        write(SPACE);

        IExpressionNode bnode = node.getBaseClassExpressionNode();
        if (bnode != null)
        {
            writeToken(IASKeywordConstants.EXTENDS);
            write(SPACE);
            getWalker().walk(bnode);
            write(SPACE);
        }

        IExpressionNode[] inodes = node.getImplementedInterfaceNodes();
        final int ilen = inodes.length;
        if (ilen != 0)
        {
            writeToken(IASKeywordConstants.IMPLEMENTS);
            write(SPACE);
            for (int i = 0; i < ilen; i++)
            {
                getWalker().walk(inodes[i]);
                if (i < ilen - 1)
                {
                    write(",");
                    write(SPACE);
                }
            }
            write(SPACE);
        }

        write("{");

        // fields, methods, namespaces
        final IDefinitionNode[] members = node.getAllMemberNodes();
        if (members.length > 0)
        {
            indentPush();
            write(NL);

            final int len = members.length;
            int i = 0;
            for (IDefinitionNode mnode : members)
            {
                getWalker().walk(mnode);
                if (mnode.getNodeID() == ASTNodeID.VariableID)
                {
                    write(";");
                    if (i < len - 1)
                        write(NL);
                }
                else if (mnode.getNodeID() == ASTNodeID.FunctionID)
                {
                    if (i < len - 1)
                        write(NL);
                }
                else if (mnode.getNodeID() == ASTNodeID.GetterID
                        || mnode.getNodeID() == ASTNodeID.SetterID)
                {
                    if (i < len - 1)
                        write(NL);
                }
                i++;
            }

            indentPop();
        }

        write(NL);
        write("}");
    }

    @Override
    public void emitInterface(IInterfaceNode node)
    {
        write(node.getNamespace());
        write(SPACE);

        writeToken(IASKeywordConstants.INTERFACE);
        write(SPACE);
        getWalker().walk(node.getNameExpressionNode());
        write(SPACE);

        IExpressionNode[] inodes = node.getExtendedInterfaceNodes();
        final int ilen = inodes.length;
        if (ilen != 0)
        {
            writeToken(IASKeywordConstants.EXTENDS);
            write(SPACE);
            for (int i = 0; i < ilen; i++)
            {
                getWalker().walk(inodes[i]);
                if (i < ilen - 1)
                {
                    write(",");
                    write(SPACE);
                }
            }
            write(SPACE);
        }

        write("{");

        final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
        if (members.length > 0)
        {
            indentPush();
            write(NL);

            final int len = members.length;
            int i = 0;
            for (IDefinitionNode mnode : members)
            {
                getWalker().walk(mnode);
                write(";");
                if (i < len - 1)
                    write(NL);
                i++;
            }

            indentPop();
        }

        write(NL);
        write("}");
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
        emitAssignedValue(node.getAssignedValueNode());

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    write(",");
                    write(SPACE);
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
        emitAssignedValue(node.getAssignedValueNode());

        if (!(node instanceof ChainedVariableNode))
        {
            // check for chained variables
            int len = node.getChildCount();
            for (int i = 0; i < len; i++)
            {
                IASNode child = node.getChild(i);
                if (child instanceof ChainedVariableNode)
                {
                    write(",");
                    write(SPACE);
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
        fn.parseFunctionBody(problems);

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
        emitParamters(node.getParameterNodes());
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
    public void emitFunctionObject(IExpressionNode node)
    {
        FunctionObjectNode f = (FunctionObjectNode) node;
        FunctionNode fnode = f.getFunctionNode();
        writeToken(IASKeywordConstants.FUNCTION);
        emitParamters(fnode.getParameterNodes());
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
        writeToken(IASKeywordConstants.NAMESPACE);
        write(SPACE);
        emitMemberName(node);
        write(SPACE);
        write("=");
        write(SPACE);
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
            write(namespace);
            write(SPACE);
        }
    }

    protected void emitModifiers(IDefinition definition)
    {
        ModifiersSet modifierSet = definition.getModifiers();
        if (modifierSet.hasModifiers())
        {
            for (ASModifier modifier : modifierSet.getAllModifiers())
            {
                write(modifier.toString());
                write(SPACE);
            }
        }
    }

    protected void emitMemberKeyword(IDefinitionNode node)
    {
        if (node instanceof IFunctionNode)
        {
            writeToken(IASKeywordConstants.FUNCTION);
            write(SPACE);
        }
        else if (node instanceof IVariableNode)
        {
            write(((IVariableNode) node).isConst() ? "const" : "var");
            write(SPACE);
        }
    }

    protected void emitMemberName(IDefinitionNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    protected void emitDeclarationName(IDefinitionNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    protected void emitParamters(IParameterNode[] nodes)
    {
        write("(");
        int len = nodes.length;
        for (int i = 0; i < len; i++)
        {
            IParameterNode node = nodes[i];
            // this will call emitParameter(node)
            getWalker().walk(node);
            if (i < len - 1)
                write(", ");
        }
        write(")");
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
        write(":");
        getWalker().walk(node.getVariableTypeNode());
        IExpressionNode anode = node.getAssignedValueNode();
        if (anode != null)
        {
            write(" = ");
            getWalker().walk(anode);
        }
    }

    protected void emitType(IExpressionNode node)
    {
        // TODO (mschmalle) node.getVariableTypeNode() will return "*" if undefined, what to use?
        // or node.getReturnTypeNode()
        if (node != null)
        {
            write(":");
            getWalker().walk(node);
        }
    }

    protected void emitAssignedValue(IExpressionNode node)
    {
        if (node != null)
        {
            write(SPACE);
            write("=");
            write(SPACE);
            getWalker().walk(node);
        }
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        // nothing to do in AS
    }

    protected void emitMethodScope(IScopedNode node)
    {
        write(SPACE);
        getWalker().walk(node);
    }

    protected void emitAccessorKeyword(IKeywordNode node)
    {
        getWalker().walk(node);
        write(SPACE);
    }

    protected void emitFunctionScope(IScopedNode node)
    {
        // TODO (mschmalle) FunctionObjectNode; does this need specific treatment?
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
                && !(node instanceof IStatementNode))
        {
            write(";");
        }

        if (!isLastStatement(node))
            write("\n");
    }

    @Override
    public void emitIf(IIfNode node)
    {
        IConditionalNode conditional = (IConditionalNode) node.getChild(0);

        IContainerNode xnode = (IContainerNode) conditional
                .getStatementContentsNode();

        write("if");
        write(" ");
        write("(");
        getWalker().walk(conditional.getChild(0)); // conditional expression
        write(")");
        if (!isImplicit(xnode))
            write(" ");

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
                    write("\n");
                else
                    write(" ");

                write("else if");
                write(" ");
                write("(");
                getWalker().walk(enode.getChild(0));
                write(")");
                if (!isImplicit)
                    write(" ");

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
                write("\n");
            else
                write(" ");
            write("else");
            if (!isImplicit)
                write(" ");

            getWalker().walk(elseNode); // TerminalNode
        }
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);
        write("for");
        write(" ");
        write("each");
        write(" ");
        write("(");

        IContainerNode cnode = node.getConditionalsContainerNode();
        getWalker().walk(cnode.getChild(0));

        write(")");
        if (!isImplicit(xnode))
            write(" ");

        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitForLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);

        write("for");
        write(" ");
        write("(");

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

        write(")");
        if (!isImplicit(xnode))
            write(" ");

        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitSwitch(ISwitchNode node)
    {
        write("switch");
        write(" ");
        write("(");
        getWalker().walk(node.getChild(0));
        write(")");
        write(" {");
        indentPush();
        write("\n");

        IConditionalNode[] cnodes = getCaseNodes(node);
        ITerminalNode dnode = getDefaultNode(node);

        for (int i = 0; i < cnodes.length; i++)
        {
            IConditionalNode casen = cnodes[i];
            IContainerNode cnode = (IContainerNode) casen.getChild(1);
            write("case");
            write(" ");
            getWalker().walk(casen.getConditionalExpressionNode());
            write(":");
            if (!isImplicit(cnode))
                write(" ");
            getWalker().walk(casen.getStatementContentsNode());
            if (i == cnodes.length - 1 && dnode == null)
            {
                indentPop();
                write("\n");
            }
            else
                write("\n");
        }
        if (dnode != null)
        {
            IContainerNode cnode = (IContainerNode) dnode.getChild(0);
            write("default");
            write(":");
            if (!isImplicit(cnode))
                write(" ");
            getWalker().walk(dnode);
            indentPop();
            write("\n");
        }
        write("}");
    }

    @Override
    public void emitWhileLoop(IWhileLoopNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(1);
        write("while");
        write(" ");
        write("(");
        getWalker().walk(node.getConditionalExpressionNode());
        write(")");
        if (!isImplicit(cnode))
            write(" ");
        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitDoLoop(IWhileLoopNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(0);
        write("do");
        if (!isImplicit(cnode))
            write(" ");
        getWalker().walk(node.getStatementContentsNode());
        if (!isImplicit(cnode))
            write(" ");
        else
            write("\n"); // TODO (mschmalle) there is something wrong here, block should NL
        write("while");
        write(" ");
        write("(");
        getWalker().walk(node.getConditionalExpressionNode());
        write(");");
    }

    @Override
    public void emitWith(IWithNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(1);
        write("with");
        write(" ");
        write("(");
        getWalker().walk(node.getTargetNode());
        write(")");
        if (!isImplicit(cnode))
            write(" ");
        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitThrow(IThrowNode node)
    {
        write("throw");
        write(" ");
        getWalker().walk(node.getThrownExpressionNode());
    }

    @Override
    public void emitTry(ITryNode node)
    {
        write("try");
        write(" ");
        getWalker().walk(node.getStatementContentsNode());
        for (int i = 0; i < node.getCatchNodeCount(); i++)
        {
            getWalker().walk(node.getCatchNode(i));
        }
        ITerminalNode fnode = node.getFinallyNode();
        if (fnode != null)
        {
            write(" ");
            write("finally");
            write(" ");
            getWalker().walk(fnode);
        }
    }

    @Override
    public void emitCatch(ICatchNode node)
    {
        write(" ");
        write("catch");
        write(" ");
        write("(");
        getWalker().walk(node.getCatchParameterNode());
        write(")");
        write(" ");
        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitReturn(IReturnNode node)
    {
        write("return");
        IExpressionNode rnode = node.getReturnValueNode();
        if (rnode != null && rnode.getNodeID() != ASTNodeID.NilID)
        {
            write(" ");
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
            write("new");
            write(" ");
        }

        getWalker().walk(node.getNameNode());

        write("(");
        walkArguments(node.getArgumentNodes());
        write(")");
    }

    //--------------------------------------------------------------------------
    // Operators
    //--------------------------------------------------------------------------

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        if (node.getNodeID() != ASTNodeID.Op_CommaID)
            write(SPACE);
        write(node.getOperator().getOperatorText());
        write(SPACE);
        getWalker().walk(node.getRightOperandNode());
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

    private void walkArguments(IExpressionNode[] nodes)
    {
        int len = nodes.length;
        for (int i = 0; i < len; i++)
        {
            IExpressionNode node = nodes[i];
            getWalker().walk(node);
            if (i < len - 1)
                write(", ");
        }
    }

    //--------------------------------------------------------------------------
    // Static Utility
    //--------------------------------------------------------------------------

    private static String toPrefix(ContainerType type)
    {
        if (type == ContainerType.BRACES)
            return "{";
        else if (type == ContainerType.BRACKETS)
            return "[";
        else if (type == ContainerType.IMPLICIT)
            return "";
        else if (type == ContainerType.PARENTHESIS)
            return "(";
        return null;
    }

    private static String toPostfix(ContainerType type)
    {
        if (type == ContainerType.BRACES)
            return "}";
        else if (type == ContainerType.BRACKETS)
            return "]";
        else if (type == ContainerType.IMPLICIT)
            return "";
        else if (type == ContainerType.PARENTHESIS)
            return ")";
        return null;
    }

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

    private static boolean isLastStatement(IASNode node)
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

    private static final boolean isImplicit(IContainerNode node)
    {
        return node.getContainerType() == ContainerType.IMPLICIT
                || node.getContainerType() == ContainerType.SYNTHESIZED;
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
            write(";");
            if (node1.getNodeID() != ASTNodeID.NilID)
                write(" ");
        }
        // condition or target
        if (node1 != null)
        {
            getWalker().walk(node1);
            write(";");
            if (node2.getNodeID() != ASTNodeID.NilID)
                write(" ");
        }
        // iterator
        if (node2 != null)
        {
            getWalker().walk(node2);
        }
    }

    //--------------------------------------------------------------------------
    // Temp: These need JIRA tickets
    //--------------------------------------------------------------------------

    // there seems to be a bug in the ISwitchNode.getCaseNodes(), need to file a bug
    public IConditionalNode[] getCaseNodes(ISwitchNode node)
    {
        IBlockNode block = (IBlockNode) node.getChild(1);
        int childCount = block.getChildCount();
        ArrayList<IConditionalNode> retVal = new ArrayList<IConditionalNode>(
                childCount);

        for (int i = 0; i < childCount; i++)
        {
            IASNode child = block.getChild(i);
            if (child instanceof IConditionalNode)
                retVal.add((IConditionalNode) child);
        }

        return retVal.toArray(new IConditionalNode[0]);
    }

    // there seems to be a bug in the ISwitchNode.getDefaultNode(), need to file a bug
    public ITerminalNode getDefaultNode(ISwitchNode node)
    {
        IBlockNode block = (IBlockNode) node.getChild(1);
        int childCount = block.getChildCount();
        for (int i = childCount - 1; i >= 0; i--)
        {
            IASNode child = block.getChild(i);
            if (child instanceof ITerminalNode)
                return (ITerminalNode) child;
        }

        return null;
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        write(node.getValue(true));
    }

    @Override
    public void emitLiteralContainer(IContainerNode node)
    {
        write(toPrefix(node.getContainerType()));
        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = node.getChild(i);
            getWalker().walk(child);
            if (i < len - 1)
                write(",");
        }
        write(toPostfix(node.getContainerType()));
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
            write(" ");
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
        write("[");
        getWalker().walk(node.getRightOperandNode());
        write("]");
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        getWalker().walk(node.getCollectionNode());
        write(".<");
        getWalker().walk(node.getTypeNode());
        write(">");
    }

    @Override
    public void emitTernaryOperator(ITernaryOperatorNode node)
    {
        getWalker().walk(node.getConditionalNode());
        write(" ");
        write("?");
        write(" ");
        getWalker().walk(node.getLeftOperandNode());
        write(" ");
        write(":");
        write(" ");
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitObjectLiteralValuePair(IObjectLiteralValuePairNode node)
    {
        getWalker().walk(node.getNameNode());
        write(":");
        getWalker().walk(node.getValueNode());
    }

    @Override
    public void emitLabelStatement(LabeledStatementNode node)
    {
        write(node.getLabel());
        write(" ");
        write(":");
        write(" ");
        getWalker().walk(node.getLabeledStatement());
    }

    @Override
    public void emitNamespaceAccessExpression(NamespaceAccessExpressionNode node)
    {
        getWalker().walk(node.getLeftOperandNode());
        write(node.getOperator().getOperatorText());
        getWalker().walk(node.getRightOperandNode());
    }

    @Override
    public void emitUnaryOperator(IUnaryOperatorNode node)
    {
        if (node.getNodeID() == ASTNodeID.Op_PreIncrID
                || node.getNodeID() == ASTNodeID.Op_PreDecrID
                || node.getNodeID() == ASTNodeID.Op_BitwiseNotID
                || node.getNodeID() == ASTNodeID.Op_LogicalNotID
                || node.getNodeID() == ASTNodeID.Op_SubtractID
                || node.getNodeID() == ASTNodeID.Op_AddID)
        {
            write(node.getOperator().getOperatorText());
            getWalker().walk(node.getOperandNode());
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
            write(node.getOperator().getOperatorText());
            write(" ");
            getWalker().walk(node.getOperandNode());
        }
        else if (node.getNodeID() == ASTNodeID.Op_TypeOfID)
        {
            write(node.getOperator().getOperatorText());
            write("(");
            getWalker().walk(node.getOperandNode());
            write(")");
        }
    }

    @Override
    public void emitLanguageIdentifier(ILanguageIdentifierNode node)
    {
        if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.ANY_TYPE)
        {
            write("*");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.REST)
        {
            write("...");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.SUPER)
        {
            write("super");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.THIS)
        {
            write("this");
        }
        else if (node.getKind() == ILanguageIdentifierNode.LanguageIdentifierKind.VOID)
        {
            write("void");
        }
    }
}
