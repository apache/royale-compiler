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

package org.apache.royale.compiler.internal.codegen.js.jx;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IObjectLiteralValuePairNode;

public class ObjectLiteralValuePairEmitter extends JSSubEmitter implements
        ISubEmitter<IObjectLiteralValuePairNode>
{
    public ObjectLiteralValuePairEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(IObjectLiteralValuePairNode node)
    {
        ISourceLocation location = (ISourceLocation) node;
        
        boolean dynamicAccessUnknownMembers = false;
        ICompilerProject project = getProject();
        if(project instanceof RoyaleJSProject)
        {
            RoyaleJSProject fjsProject = (RoyaleJSProject) project;
            if(fjsProject.config != null)
            {
                dynamicAccessUnknownMembers = fjsProject.config.getJsDynamicAccessUnknownMembers();
            }
        }

        IExpressionNode nameNode = node.getNameNode();
        if(dynamicAccessUnknownMembers && nameNode instanceof IIdentifierNode)
        {
            IIdentifierNode identifierNode = (IIdentifierNode) nameNode;
            startMapping(location, nameNode);
            write(ASEmitterTokens.DOUBLE_QUOTE);
            write(identifierNode.getName());
            write(ASEmitterTokens.DOUBLE_QUOTE);
            endMapping(location);
        }
        else
        {
            getWalker().walk(nameNode);
        }

        startMapping(location, nameNode);
        write(ASEmitterTokens.COLON);
        endMapping(location);

        IExpressionNode valueNode = node.getValueNode();
        getWalker().walk(valueNode);
    }
}
