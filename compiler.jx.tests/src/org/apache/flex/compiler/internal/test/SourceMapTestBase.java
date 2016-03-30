package org.apache.flex.compiler.internal.test;

import java.util.List;

import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.tree.as.IASNode;

import com.google.debugging.sourcemap.FilePosition;
import static org.junit.Assert.assertTrue;

public class SourceMapTestBase extends ASTestBase
{
    protected IJSEmitter jsEmitter;
    
    @Override
    public void setUp()
    {
        super.setUp();

        jsEmitter = (IJSEmitter) asEmitter;
    }

    protected void assertMapping(IASNode node, int nodeStartLine, int nodeStartColumn,
        int outStartLine, int outStartColumn, int outEndLine, int outEndColumn)
    {
        int sourceStartLine = nodeStartLine + node.getLine();
        int sourceStartColumn = nodeStartColumn + node.getColumn();
        boolean foundMapping = false;
        List<IJSEmitter.SourceMapMapping> mappings = jsEmitter.getSourceMapMappings();
        for (IJSEmitter.SourceMapMapping mapping : mappings)
        {
            FilePosition sourcePosition = mapping.sourceStartPosition;
            FilePosition startPosition = mapping.destStartPosition;
            FilePosition endPosition = mapping.destEndPosition;
            if (sourcePosition.getLine() == sourceStartLine
                    && sourcePosition.getColumn() == sourceStartColumn
                    && startPosition.getLine() == outStartLine
                    && startPosition.getColumn() == outStartColumn
                    && endPosition.getLine() == outEndLine
                    && endPosition.getColumn() == outEndColumn)
            {
                foundMapping = true;
            }
        }
        assertTrue("Mapping not found for node " + node.toString(), foundMapping);
    }
    
}
