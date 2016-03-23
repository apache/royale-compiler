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

package org.apache.flex.compiler.internal.codegen.js;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.debugging.sourcemap.FilePosition;
import org.apache.flex.compiler.clients.JSConfiguration;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;
import org.apache.flex.compiler.tree.as.IKeywordNode;
import org.apache.flex.compiler.tree.as.INumericLiteralNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.visitor.IBlockWalker;

/**
 * @author Michael Schmalle
 */
public class JSEmitter extends ASEmitter implements IJSEmitter
{
    private JSSessionModel model;
    
    @Override
    public JSSessionModel getModel()
    {
        return model;
    }
    
    private SourceMapMapping lastMapping;
    
    private List<SourceMapMapping> sourceMapMappings;
    
    public List<SourceMapMapping> getSourceMapMappings()
    {
        return sourceMapMappings;
    }

    public JSEmitter(FilterWriter out)
    {
        super(out);
        
        model = new JSSessionModel();
        sourceMapMappings = new ArrayList<SourceMapMapping>();
    }

    @Override
    public String formatQualifiedName(String name)
    {
        return name;
    }
    
    @Override
    public void emitLocalNamedFunction(IFunctionNode node)
    {
        startMapping(node);
        FunctionNode fnode = (FunctionNode)node;
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.SPACE);
        write(fnode.getName());
        endMapping(node);
        emitParameters(fnode.getParameterNodes());
        emitFunctionScope(fnode.getScopedNode());
    }
    
    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
        startMapping(node);
        FunctionNode fnode = node.getFunctionNode();
        write(ASEmitterTokens.FUNCTION);
        endMapping(node);
        emitParameters(fnode.getParameterNodes());
        emitFunctionScope(fnode.getScopedNode());
    }

    public void emitClosureStart()
    {
    	
    }

    public void emitClosureEnd(IASNode node)
    {
    	
    }
    
    public void emitSourceMapDirective(ITypeNode node)
    {
        boolean sourceMap = false;
        
        IBlockWalker walker = getWalker();
        FlexJSProject project = (FlexJSProject) walker.getProject();
        if (project != null)
        {
            JSConfiguration config = project.config;
            if (config != null)
            {
                sourceMap = config.getSourceMap();
            }
        }
        
        if (sourceMap)
        {
            writeNewline();
            write("//# sourceMappingURL=./" + node.getName() + ".js.map");
        }
    }

    public void emitParameters(IParameterNode[] nodes)
    {
        write(ASEmitterTokens.PAREN_OPEN);
        int len = nodes.length;
        for (int i = 0; i < len; i++)
        {
            IParameterNode node = nodes[i];
            getWalker().walk(node); //emitParameter
            if (i < len - 1)
            {
                writeToken(ASEmitterTokens.COMMA);
            }
        }
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        startMapping(node);
        super.emitParameter(node);
        endMapping(node);
    }

    @Override
    public void emitNumericLiteral(INumericLiteralNode node)
    {
        startMapping((ISourceLocation) node);
        super.emitNumericLiteral(node);
        endMapping((ISourceLocation) node);
    }

    @Override
    public void emitReturn(IReturnNode node)
    {
        startMapping(node);
        write(ASEmitterTokens.RETURN);
        endMapping(node);
        IExpressionNode rnode = node.getReturnValueNode();
        if (rnode != null && rnode.getNodeID() != ASTNodeID.NilID)
        {
            write(ASEmitterTokens.SPACE);
            getWalker().walk(rnode);
        }
    }

    @Override
    public void emitMemberKeyword(IDefinitionNode node)
    {
        IKeywordNode keywordNode = null;
        for(int i = 0; i < node.getChildCount(); i++)
        {
            IASNode childNode = node.getChild(i);
            if (childNode instanceof IKeywordNode)
            {
                keywordNode = (IKeywordNode) childNode;
                break; 
            }
        }
        if (keywordNode != null)
        {
            startMapping(keywordNode);
        }
        if (node instanceof IFunctionNode)
        {
            writeToken(ASEmitterTokens.FUNCTION);
        }
        else if (node instanceof IVariableNode)
        {
            writeToken(ASEmitterTokens.VAR);
        }
        if (keywordNode != null)
        {
            endMapping(keywordNode);
        }
    }
    
    public void startMapping(ISourceLocation node)
    {
        if (lastMapping != null)
        {
            FilePosition sourceStartPosition = lastMapping.sourceStartPosition;
            throw new IllegalStateException("Cannot start new mapping when another mapping is already started. "
                    + "Previous mapping at Line " + sourceStartPosition.getLine()
                    + " and Column " + sourceStartPosition.getColumn()
                    + " in file " + lastMapping.sourcePath);
        }
        
        String sourcePath = node.getSourcePath();
        if (sourcePath == null)
        {
            //if the source path is null, this node may have been generated by
            //the compiler automatically. for example, an untyped variable will
            //have a node for the * type.
            if (node instanceof IASNode)
            {
                IASNode parentNode = ((IASNode) node).getParent();
                if (parentNode != null)
                {
                    //try the parent node
                    startMapping(parentNode);
                }
            }
            return;
        }
        
        String nodeName = null;
        if (node instanceof IDefinitionNode)
        {
            IDefinitionNode definitionNode = (IDefinitionNode) node; 
            nodeName = definitionNode.getQualifiedName();

            ITypeDefinition typeDef = EmitterUtils.getTypeDefinition(definitionNode);
            if (typeDef != null)
            {
                boolean isConstructor = node instanceof IFunctionNode &&
                        ((IFunctionNode) node).isConstructor();
                boolean isStatic = definitionNode.hasModifier(ASModifier.STATIC);
                if (isConstructor)
                {
                    nodeName = typeDef.getQualifiedName() + ".constructor";
                }
                else if (isStatic)
                {
                    nodeName = typeDef.getQualifiedName() + "." + nodeName;
                }
                else
                {
                    nodeName = typeDef.getQualifiedName() + ".prototype." + nodeName;
                }
            }
        }
        SourceMapMapping mapping = new SourceMapMapping();
        mapping.sourcePath = sourcePath;
        mapping.name = nodeName;
        mapping.sourceStartPosition = new FilePosition(node.getLine(), node.getColumn());
        mapping.destStartPosition = new FilePosition(getCurrentLine(), getCurrentColumn());
        lastMapping = mapping;
    }

    public void endMapping(ISourceLocation node)
    {
        if (lastMapping == null)
        {
            throw new IllegalStateException("Cannot end mapping when a mapping has not been started");
        }
        
        lastMapping.destEndPosition = new FilePosition(getCurrentLine(), getCurrentColumn());
        sourceMapMappings.add(lastMapping);
        lastMapping = null;
    }

}
