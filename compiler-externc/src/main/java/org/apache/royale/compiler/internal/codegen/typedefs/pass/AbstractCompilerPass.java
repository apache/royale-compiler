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

package org.apache.royale.compiler.internal.codegen.typedefs.pass;

import java.util.Collection;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.pass.ReferenceCompiler.TypedefFile;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ReferenceModel;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.DebugLogUtils;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.StaticSourceFile;

public abstract class AbstractCompilerPass implements CompilerPass, Callback
{
    protected ReferenceModel model;
    protected AbstractCompiler compiler;

    protected boolean logEnabled;
    protected boolean errEnabled;

    public AbstractCompilerPass(ReferenceModel model, AbstractCompiler compiler)
    {
        this.model = model;
        this.compiler = compiler;
    }

    @Override
    public void process(Node externs, Node root)
    {
        //NodeTraversal.traverse(compiler, root, this);
        NodeTraversal.traverseRoots(compiler, this, externs, root);
    }

    protected void log(Node n)
    {
        DebugLogUtils.err(n);
    }

    protected void err(Node n)
    {
        DebugLogUtils.err(n);
    }

    protected void log(String message)
    {
        DebugLogUtils.log(message);
    }

    protected void err(String message)
    {
        DebugLogUtils.err(message);
    }
    
    protected String getSourceCode(StaticSourceFile file, int line)
    {
    	if (file instanceof SourceFile)
    	{
    		String code = ((SourceFile)file).getLine(line);
    		return code;
    	}
    	return "no source line found";
    }

    public static String getSourceFileName(String externName, ReferenceModel model)
    {
    	if (externName.contains("["))
    	{
    		externName = externName.replace("[", "");
    		externName = externName.replace("]", "");
        	ExternCConfiguration config = model.getConfiguration();
        	Collection<TypedefFile> externs = config.getTypedefs();
        	for (TypedefFile f : externs)
        	{
        		String fn = f.getName();
        		if (fn.equals(externName))
        		{
        			externName = f.getFile().getAbsolutePath();
        			break;
        		}
        	}
    	}
    	return externName;
    }
}
