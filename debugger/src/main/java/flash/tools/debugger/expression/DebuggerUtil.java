/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flash.tools.debugger.expression;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * 
 * @author ggv
 */
public class DebuggerUtil
{

    /**
     * 
     * @param code
     * @param problems
     * @return
     */
    public static IASNode parseExpression(String code, List<ICompilerProblem> problems)
    {
    	IWorkspace workspace = new Workspace();
        DebuggerUtil.InMemoryFileSpecification imfs = new DebuggerUtil.InMemoryFileSpecification(code);
        EnumSet<PostProcessStep> empty = EnumSet.noneOf(PostProcessStep.class);
        IASNode exprAST = ASParser.parseFile(imfs, workspace, empty, null, false, false, false, new ArrayList<String>(), null, null, null);

        // Have to create a fake ScopedBlockNode so the expression can do things
        // like resolve, which means it has to be able to find a scope.
        // For parsing an expression in a file, one would hook up the expression
        // AST to whatever the real scope was.
        ScopedBlockNode scopedNode = new ScopedBlockNode();
        scopedNode.addChild((NodeBase)exprAST);
        scopedNode.setScope(new ASFileScope(workspace, "fake"));
        scopedNode.runPostProcess(EnumSet.of(PostProcessStep.CALCULATE_OFFSETS));

        // return the first (and only child).  This is essentially unwrapping the
        // FileNode that was wrapped around the expression being parsed
        return exprAST.getChild(0);
    }

    public static class InMemoryFileSpecification implements IFileSpecification
    {
    	public InMemoryFileSpecification(String s)
    	{
    		this.s = s;
    	}
    	
    	private String s;
    	
    	public String getPath()
    	{
    		return "flash.tools.debugger";
    	}
    	
    	public Reader createReader() throws FileNotFoundException
    	{
    		return new StringReader(s);
    	}
    	
    	public long getLastModified()
    	{
    		return 0;
    	}
    	
        @Override
        public void setLastModified(long fileDate) {
            // TODO Auto-generated method stub
        }

    	public boolean isOpenDocument()
    	{
    		return false;
    	}
    }

}
