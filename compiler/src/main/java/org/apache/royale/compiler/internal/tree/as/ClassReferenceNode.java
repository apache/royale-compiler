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

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.tree.ASTNodeID;

/**
 * Node representing a ClassReference("...") expression. Currently, this is only
 * used for the ClassReference expressions in .properties files but this class
 * is not limited to this case.
 */
// TODO Add IClassReferenceNode with getName().
public class ClassReferenceNode extends ExpressionNodeBase
{
    /**
     * Constructor.
     */
    public ClassReferenceNode(String value, SourceLocation sourceLocation)
    {
        name = value;
        setSourceLocation(sourceLocation);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected ClassReferenceNode(ClassReferenceNode other)
    {
        super(other);
        
        this.name = other.name;
    }
    
    private String name;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ClassReferenceID;
    }
    
    //
    // ExpressionNodeBase overrides
    //

    // TODO Does this class need to override resolveType()?

    @Override
    protected ClassReferenceNode copy()
    {
        return new ClassReferenceNode(this);
    }
    
    //
    // Other methods
    //

    /**
     * Returns the qualified name used in the ClassReference expression.
     * 
     * @return qualified name used in the expression
     */
    public String getName()
    {
        return name;
    }
}
