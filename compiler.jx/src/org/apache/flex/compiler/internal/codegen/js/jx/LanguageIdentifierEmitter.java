package org.apache.flex.compiler.internal.codegen.js.jx;

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;

public class LanguageIdentifierEmitter extends JSSubEmitter implements
        ISubEmitter<ILanguageIdentifierNode>
{
    public LanguageIdentifierEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ILanguageIdentifierNode node)
    {
        startMapping((ISourceLocation) node);
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
        endMapping((ISourceLocation) node);
    }
}
