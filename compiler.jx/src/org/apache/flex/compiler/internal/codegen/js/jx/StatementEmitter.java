package org.apache.flex.compiler.internal.codegen.js.jx;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IStatementNode;

public class StatementEmitter extends JSSubEmitter implements
        ISubEmitter<IASNode>
{
    public StatementEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IASNode node)
    {
        getWalker().walk(node);

        // XXX (mschmalle) this should be in the after handler?
        if (node.getParent().getNodeID() != ASTNodeID.LabledStatementID
                && node.getNodeID() != ASTNodeID.ConfigBlockID
                && !(node instanceof IStatementNode))
        {
            startMapping(node, node);
            write(ASEmitterTokens.SEMICOLON);
            endMapping(node);
        }

        if (!isLastStatement(node))
            writeNewline();
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
}
