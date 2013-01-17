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

package org.apache.flex.compiler.internal.js.codegen.amd;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.constants.IASKeywordConstants;
import org.apache.flex.compiler.constants.IASLanguageConstants;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.js.codegen.JSEmitter;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.js.codegen.amd.IJSAMDDocEmitter;
import org.apache.flex.compiler.js.codegen.amd.IJSAMDEmitter;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.ITypeNode;

/**
 * Concrete implementation of the 'AMD' JavaScript production.
 * 
 * @author Michael Schmalle
 */
public class JSAMDEmitter extends JSEmitter implements IJSAMDEmitter
{
    IJSAMDDocEmitter getDoc()
    {
        return (IJSAMDDocEmitter) getDocEmitter();
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    @Override
    public void emitMethod(IFunctionNode node)
    {
        if (node.isConstructor())
        {
            IClassDefinition definition = getClassDefinition(node);

            FunctionNode fn = (FunctionNode) node;
            fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

            String qname = definition.getQualifiedName();
            write(qname);
            write(SPACE);
            write(EQUALS);
            write(SPACE);
            write(FUNCTION);
            emitParamters(node.getParameterNodes());
            emitMethodScope(node.getScopedNode());

            return;
        }

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(new ArrayList<ICompilerProblem>());

        String qname = getTypeDefinition(node).getQualifiedName();
        if (qname != null && !qname.equals(""))
        {
            write(qname);
            write(PERIOD);
            if (!fn.hasModifier(ASModifier.STATIC))
            {
                write(PROTOTYPE);
                write(PERIOD);
            }
        }

        emitMemberName(node);
        write(SPACE);
        write(EQUALS);
        write(SPACE);
        write(FUNCTION);
        emitParamters(node.getParameterNodes());
        emitMethodScope(node.getScopedNode());
    }

    @Override
    public void emitFunctionBlockHeader(IFunctionNode node)
    {
        emitDefaultParameterCodeBlock(node);
    }

    private void emitDefaultParameterCodeBlock(IFunctionNode node)
    {
        // TODO (mschmalle) test for ... rest 
        // if default parameters exist, produce the init code
        IParameterNode[] pnodes = node.getParameterNodes();
        Map<Integer, IParameterNode> defaults = getDefaults(pnodes);
        if (pnodes.length == 0)
            return;

        if (defaults != null)
        {
        	boolean hasBody = node.getScopedNode().getChildCount() > 0;
            
            if (!hasBody)
            {
                indentPush();
                write(INDENT);
            }

        	final StringBuilder code = new StringBuilder();

            List<IParameterNode> parameters = new ArrayList<IParameterNode>(
                    defaults.values());
            Collections.reverse(parameters);

            int len = defaults.size();
            // make the header in reverse order
            for (IParameterNode pnode : parameters)
            {
                if (pnode != null)
                {
                    code.setLength(0);
                    
                    code.append(IASKeywordConstants.IF);
                    code.append(SPACE);
                    code.append(PARENTHESES_OPEN);
                    code.append(IASLanguageConstants.arguments);
                    code.append(PERIOD);
                    code.append(LENGTH);
                    code.append(SPACE);
                    code.append(LESS_THEN);
                    code.append(SPACE);
                    code.append(len);
                    code.append(PARENTHESES_CLOSE);
                    code.append(SPACE);
                    code.append(CURLYBRACE_OPEN);
                    
                    write(code.toString());
                    
                    indentPush();
                    writeNewline();
                }
                len--;
            }

            Collections.reverse(parameters);
            for (int i = 0, n = parameters.size(); i < n; i++)
            {
            	IParameterNode pnode = parameters.get(i);
            	
                if (pnode != null)
                {
                    code.setLength(0);
                    
                    code.append(pnode.getName());
                    code.append(SPACE);
                    code.append(EQUALS);
                    code.append(SPACE);
                    code.append(pnode.getDefaultValue());
                    code.append(SEMICOLON);
                    write(code.toString());

                    indentPop();
                    writeNewline();
                    
                    write(CURLYBRACE_CLOSE);

                    if (i == n - 1 && !hasBody)
                    	indentPop();
                    
                    writeNewline();
                }
            }
        }
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        getWalker().walk(node.getNameExpressionNode());
    }

    public JSAMDEmitter(FilterWriter out)
    {
        super(out);
    }

    private Map<Integer, IParameterNode> getDefaults(IParameterNode[] nodes)
    {
        Map<Integer, IParameterNode> result = new HashMap<Integer, IParameterNode>();
        int i = 0;
        boolean hasDefaults = false;
        for (IParameterNode node : nodes)
        {
            if (node.hasDefaultValue())
            {
                hasDefaults = true;
                result.put(i, node);
            }
            else
            {
                result.put(i, null);
            }
            i++;
        }

        if (!hasDefaults)
            return null;

        return result;
    }

    private static ITypeDefinition getTypeDefinition(IDefinitionNode node)
    {
        ITypeNode tnode = (ITypeNode) node.getAncestorOfType(ITypeNode.class);
        return (ITypeDefinition) tnode.getDefinition();
    }

    private static IClassDefinition getClassDefinition(IDefinitionNode node)
    {
        IClassNode tnode = (IClassNode) node
                .getAncestorOfType(IClassNode.class);
        return tnode.getDefinition();
    }

}
