package org.apache.flex.compiler.internal.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.debugging.sourcemap.FilePosition;
import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;

public class SourceMapTestBase extends ASTestBase
{
    protected IJSEmitter jsEmitter;
    
    @Override
    public void setUp()
    {
        super.setUp();

        jsEmitter = (IJSEmitter) asEmitter;
    }

    protected void assertMapping(int sourceStartLine, int sourceStartColumn)
    {
        boolean foundMapping = false;
        List<IJSEmitter.SourceMapMapping> mappings = jsEmitter.getSourceMapMappings();
        for (IJSEmitter.SourceMapMapping mapping : mappings)
        {
            FilePosition position = mapping.sourceStartPosition;
            if(position.getLine() == sourceStartLine
                    && position.getColumn() == sourceStartColumn)
            {
                foundMapping = true;
            }
        }
        assertTrue(foundMapping);
    }
    
}
