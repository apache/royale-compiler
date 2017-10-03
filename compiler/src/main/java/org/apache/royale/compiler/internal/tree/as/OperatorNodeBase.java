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

import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.as.IOperatorNode;

/**
 * ActionScript parse tree node representing a binary operator expression (e.g.
 * x + 2 or var1 == var2)
 */
public abstract class OperatorNodeBase extends ExpressionNodeBase implements IOperatorNode
{
    /**
     * Constructor.
     * 
     * @param operator ASToken holding the operator itself
     */
    public OperatorNodeBase(IASToken operator)
    {
        if (operator != null)
        {
            operatorStart = operator.getStart();
            startBefore(operator);
            endAfter(operator);
            setSourcePath(operator.getSourcePath());
        }
    }
    
    /**
     * Copy constructor.
     * 
     * @param other The node to copy.
     */
    protected OperatorNodeBase(OperatorNodeBase other)
    {
        super(other);
        
        this.operatorStart = other.operatorStart;
    }

    /**
     * Offset where the operator starts
     */
    protected int operatorStart = UNKNOWN;
    
    //
    // NodeBase overrides
    //
    
    @Override
    public boolean isTerminal()
    {
        return false;
    }
    
    /*
     * For debugging only. Builds a string such as <code>"+"</code> from the
     * operator.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getOperatorText());
        sb.append('"');

        return true;
    }

    //
    // IOperatorNode implementations
    //
    
    @Override
    public int getOperatorStart()
    {
        final OffsetLookup offsetLookup = tryGetOffsetLookup();
        return offsetLookup != null ?
               offsetLookup.getLocalOffset(operatorStart) :
               operatorStart;
    }

    @Override
    public int getOperatorEnd()
    {
        int operatorStart = getOperatorStart();
        return operatorStart != -1 ? operatorStart + getOperatorText().length() : operatorStart;
    }

    @Override
    public int getOperatorAbsoluteStart()
    {
        return operatorStart;
    }

    @Override
    public int getOperatorAbsoluteEnd()
    {
        return operatorStart != -1 ? operatorStart + getOperatorText().length() : operatorStart;
    }

    //
    // Other methods
    //

    public String getOperatorText()
    {
        OperatorType operator = getOperator();
        return operator != null ? operator.getOperatorText() : "";
    }
}
