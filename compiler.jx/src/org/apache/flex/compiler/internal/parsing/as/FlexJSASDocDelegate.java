package org.apache.flex.compiler.internal.parsing.as;




import antlr.Token;

import org.apache.flex.compiler.asdoc.IASDocComment;
import org.apache.flex.compiler.asdoc.IASDocDelegate;
import org.apache.flex.compiler.asdoc.IASParserASDocDelegate;
import org.apache.flex.compiler.asdoc.IMetadataParserASDocDelegate;
import org.apache.flex.compiler.asdoc.IPackageDITAParser;
import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.definitions.IDocumentableDefinition;
import org.apache.flex.compiler.tree.as.IDocumentableDefinitionNode;

/**
 * Default implementation of {@link IASDocDelegate} that does not have any code
 * model or eclipse dependencies.
 */
public final class FlexJSASDocDelegate implements IASDocDelegate
{
    private static final FlexJSASDocDelegate INSTANCE = new FlexJSASDocDelegate();

    /**
     * Gets the single instance of this delegate.
     * 
     * @return The single instance of this delegate.
     */
    public static IASDocDelegate get()
    {
        return INSTANCE;
    }

    public FlexJSASDocDelegate()
    {
    }

    @Override
    public IASParserASDocDelegate getASParserASDocDelegate()
    {
        return ASDelegate.INSTANCE;
    }

    @Override
    public IASDocComment createASDocComment(ISourceLocation location, IDocumentableDefinition definition)
    {
        return null;
    }

    @Override
    public IPackageDITAParser getPackageDitaParser()
    {
        return IPackageDITAParser.NIL_PARSER;
    }

    private static final class ASDelegate implements IASParserASDocDelegate
    {
        static final ASDelegate INSTANCE = new ASDelegate();

        @Override
        public void beforeVariable()
        {
        }

        @Override
        public void afterVariable()
        {
        }

        Token currentToken;
        
        @Override
        public void setCurrentASDocToken(Token asDocToken)
        {
            currentToken = asDocToken;
        }

        @Override
        public IASDocComment afterDefinition(IDocumentableDefinitionNode definitionNode)
        {
            if (currentToken == null)
                return null;
            
            ASDocComment comment = new ASDocComment(currentToken);
            currentToken = null;
            return comment;
        }

        @Override
        public IMetadataParserASDocDelegate getMetadataParserASDocDelegate()
        {
            return MetadataDelegate.INSTANCE;
        }

    }

    private static final class MetadataDelegate implements IMetadataParserASDocDelegate
    {
        static final MetadataDelegate INSTANCE = new MetadataDelegate();

        @Override
        public void setCurrentASDocToken(Token asDocToken)
        {
        }

        @Override
        public IASDocComment afterDefinition(IDocumentableDefinitionNode definitionNode)
        {
            return null;
        }

        @Override
        public void clearMetadataComment(String metaDataTagName)
        {
        }

        @Override
        public void afterMetadata(int metaDataEndOffset)
        {
        }
    }
}
