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

package org.apache.royale.compiler.internal.codegen;

import java.io.FilterWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.codegen.IEmitter;
import org.apache.royale.compiler.codegen.IEmitterTokens;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;

/**
 * The base implementation for an ActionScript emitter.
 * 
 * @author Michael Schmalle
 */
public class Emitter implements IEmitter
{
    private final FilterWriter out;

    private boolean bufferWrite;

    protected boolean isBufferWrite()
    {
        return bufferWrite;
    }

    protected void setBufferWrite(boolean value)
    {
        bufferWrite = value;
    }

    private StringBuilder builder;

    protected StringBuilder getBuilder()
    {
        return builder;
    }

    protected void flushBuilder()
    {
        setBufferWrite(false);
        write(builder.toString());
        builder.setLength(0);
    }

    protected List<ICompilerProblem> problems;

    // (mschmalle) think about how this should be implemented, we can add our
    // own problems to this, they don't just have to be parse problems
    public List<ICompilerProblem> getProblems()
    {
        return problems;
    }

    //    private IDocEmitter docEmitter;
    //
    //    @Override
    //    public IDocEmitter getDocEmitter()
    //    {
    //        return docEmitter;
    //    }
    //
    //    @Override
    //    public void setDocEmitter(IDocEmitter value)
    //    {
    //        docEmitter = value;
    //    }

    private int currentIndent = 0;

    protected int getCurrentIndent()
    {
        return currentIndent;
    }

    private IASBlockWalker walker;

    public IASBlockWalker getWalker()
    {
        return walker;
    }

    public void setWalker(IASBlockWalker value)
    {
        walker = value;
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

    public Emitter(FilterWriter out)
    {
        this.out = out;
        builder = new StringBuilder();
        problems = new ArrayList<ICompilerProblem>();
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
        if (currentIndent > 0)
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

    /**
     * Takes the node argument and created a String representation if it using
     * the buffer temporarily.
     * <p>
     * Note; This method is still beta, it need more logic if an emitter is
     * actually using the buffer!
     * 
     * @param node The node walk and create a String for.
     * @return The node's output.
     */
    public String stringifyNode(IASNode node)
    {
        setBufferWrite(true);
        getWalker().walk(node);
        String result = getBuilder().toString();
        getBuilder().setLength(0);
        setBufferWrite(false);
        return result;
    }
}
