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

package org.apache.royale.compiler.internal.tree.mxml;

import java.util.List;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLObjectNode;

/**
 * This AST node represents an MXML &lt;Object&gt; tag. Although
 * {@code MXMLObjectNode} has the same API as {@code MXMLInstanceNode}, it gets
 * codegen'd differently in {@code MXMLDocumentDirectoveProcessor}, to use the
 * <code>newobject</code> opcode.
 */
class MXMLObjectNode extends MXMLInstanceNode implements IMXMLObjectNode
{
    MXMLObjectNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLObjectID;
    }

    @Override
    public String getName()
    {
        return IASLanguageConstants.Object;
    }
    
    public void initialize(MXMLTreeBuilder builder, IMXMLTagData parentTag, List<IMXMLUnitData> contentUnits, MXMLNodeInfo info)
	{
    	RoyaleProject project = builder.getProject();

    	// Set the location of the implicit array node
    	// to span the tags that specify the default property value.
    	setLocation(builder, contentUnits);

    	setClassReference(project, IASLanguageConstants.Object);

    	for (IMXMLUnitData unit : contentUnits)
    	{
    		if (unit instanceof IMXMLTagData)
    		{
    			IMXMLTagData tag = (IMXMLTagData)unit;
    			processChildTag(builder, parentTag, tag, info);
    		}
    	}
    	initializationComplete(builder, parentTag, info);
	}

}
