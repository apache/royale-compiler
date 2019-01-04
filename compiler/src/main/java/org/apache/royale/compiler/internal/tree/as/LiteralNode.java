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

import antlr.Token;

import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.ILiteralNode;

/**
 * ActionScript parse tree node representing a literal (e.g. "blah" or true or
 * 12)
 */
public class LiteralNode extends ExpressionNodeBase implements ILiteralNode
{
    /**
     * Constructor.
     * 
     * @param type The type of the literal.
     * @param value The value of the literal.
     */
    public LiteralNode(LiteralType type, String value)
    {
        super();
        
        this.type = type;
        this.value = value;
    }

    /**
     * Constructor.
     * 
     * @param type type of the literal
     * @param t token
     */
    public LiteralNode(LiteralType type, Token t)
    {
        this(type, ((IASToken)t).getText());
        
        span(t);
    }

    /**
     * Constructor.
     * 
     * @param type type of the literal
     * @param value value of the literal
     * @param sourceLocation object that stores location information of the
     * literal
     */
    public LiteralNode(LiteralType type, String value, SourceLocation sourceLocation)
    {
        this(type, value);
        
        setSourceLocation(sourceLocation);
    }

    /**
     * Constructor.
     * 
     * @param t Token with text and source location.
     * @param type Literal type
     */
    public LiteralNode(ASToken t, LiteralType type)
    {
        this(type, t.getText());
        
        value = t.getText();
        span((Token)t);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected LiteralNode(LiteralNode other)
    {
        super(other);
        
        this.type = other.type;
        this.value = other.value;
        this.synthetic = other.synthetic;
    }
    
    /**
     * Type of the literal (e.g. Boolean or String)
     */
    protected LiteralType type;

    protected String value;

    private boolean synthetic;

    //
    // NodeBase overrides
    //

    @SuppressWarnings("incomplete-switch")
	@Override
    public ASTNodeID getNodeID()
    {
        switch (type)
        {
            case ARRAY:
                return ASTNodeID.LiteralArrayID;

            case BOOLEAN:
                return ASTNodeID.LiteralBooleanID;

            case NULL:
                return ASTNodeID.LiteralNullID;

            case NUMBER:
                return ASTNodeID.LiteralNumberID;

            case OBJECT:
                return ASTNodeID.LiteralObjectID;

            case REGEXP:
                return ASTNodeID.LiteralRegexID;

            case STRING:
                return ASTNodeID.LiteralStringID;

            case VOID:
                return ASTNodeID.LiteralVoidID;

            case XML:
                return ASTNodeID.LiteralXMLID;
        }

        return ASTNodeID.LiteralID;
    }
    
    @Override
    public void normalize(boolean fillInOffsets)
    {
        // do nothing
    }

    /*
     * For debugging only.
     * Builds a string such as <code>String "Hello"</code>
     * from the literal's type and value.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        String type = getLiteralType().getType().getName();
        boolean isString = type.equals(IASLanguageConstants.String);

        sb.append(type);
        sb.append(' ');
        if (isString)
            sb.append('"');
        sb.append(getValue());
        if (isString)
            sb.append('"');

        return true;
    }
    
    //
    // FixedChildrenNode overrides
    //
    
    @Override
    public boolean isTerminal()
    {
        return true;
    }
    
    //
    // ExpressionNodeBase overrides
    //

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        LiteralType l = getLiteralType();
        return project.getBuiltinType(l.getType());
    }

    @Override
    protected LiteralNode copy()
    {
        return new LiteralNode(this);
    }
    
    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return false;
    }

    //
    // ILiteralNode implementations
    //

    @Override
    public LiteralType getLiteralType()
    {
        return type;
    }
    
    @Override
    public String getValue()
    {
        return this.getValue(false);
    }

    @Override
    public String getValue(boolean rawValue)
    {
        String retVal = value;

        if (type == LiteralType.STRING)
        {
            // Note: MXML can create LiteralNodes of type STRING
            // where value is null, to represent a null string.
            // This happens in a case like <fx:String id="s"/>,
            // which sets s to null in MXML 2009.
            if (rawValue || retVal == null || retVal.length() == 0)
                return retVal;

            // Assuming the intent here was to be robust in the face of malformed strings

            switch (retVal.charAt(0))
            {
                case '"':
                case '\'':
                {
                    retVal = retVal.substring(1);
                }
            }
            if (retVal.length() >= 1)
            {
                switch (retVal.charAt(retVal.length() - 1))
                {
                    case '"':
                    case '\'':
                    {
                        retVal = retVal.substring(0, retVal.length() - 1);
                    }
                }
            }
            else
            {
                return "";
            }
        }
        
        return retVal;
    }

    //
    // Other methods
    //

    /**
     * Returns whether this node is synthetic. A synthetic node is one that is
     * generated by the parser during parsing, and does not exist in source.
     * 
     * @return true if synthetic
     */
    public boolean isSynthetic()
    {
        return synthetic;
    }

    /**
     * Sets whether this node is synthetic. A synthetic node is one that is
     * generated by the parser during parsing, and does not exist in source. A
     * common synthetic literal node would be the result of the evaluation of a
     * config expression
     * 
     * @param synthetic true if this node is synthetic
     */
    public void setSynthetic(boolean synthetic)
    {
        this.synthetic = synthetic;
    }
}
