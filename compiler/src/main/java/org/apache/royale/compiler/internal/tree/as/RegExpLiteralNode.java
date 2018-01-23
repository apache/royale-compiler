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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.IProblemReporter;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.SyntaxProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IRegExpLiteralNode;

public class RegExpLiteralNode extends LiteralNode implements IRegExpLiteralNode
{
    private static final Set<RegExpFlag> EMPTY_SET = Collections.emptySet();

    /**
     * Constructor.
     * 
     * @param t The token representing the RegExp literal.
     * @param reporter The object used for reporting compiler problems.
     */
    public RegExpLiteralNode(ASToken t, IProblemReporter reporter)
    {
        super(t, LiteralType.REGEXP);

        // Process for flags.
        ArrayList<RegExpFlag> flags = new ArrayList<RegExpFlag>(5);
        boolean cont = true;
        for (int i = value.length() - 1; i >= 0 && cont; i--)
        {
            char charAt = value.charAt(i);
            switch (charAt)
            {
                case 'm':
                {
                    flags.add(RegExpFlag.MULTILINE);
                    break;
                }
                case 's':
                {
                    flags.add(RegExpFlag.DOTALL);
                    break;
                }
                case 'g':
                {
                    flags.add(RegExpFlag.GLOBAL);
                    break;
                }
                case 'i':
                {
                    flags.add(RegExpFlag.IGNORECASE);
                    break;
                }
                case 'x':
                {
                    flags.add(RegExpFlag.EXTENDED);
                    break;
                }
                case '/':
                {
                    cont = false;
                    break;
                }
                default:
                {
                    // Add an error if the flag is not valid.
                    ISourceLocation location = new SourceLocation(getSourcePath(),
                            getStart() + i, getStart() + i + 1, getLine(), getColumn());
                    ICompilerProblem problem = new SyntaxProblem(location, Character.toString(charAt));
                    reporter.addProblem(problem);
                    break;
                }
            }
        }
        if (flags.size() > 0)
            this.flags = EnumSet.copyOf(flags);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected RegExpLiteralNode(RegExpLiteralNode other)
    {
        super(other);
        
        this.flags = other.flags;
    }

    /**
     * Flags collected on this regular expression
     */
    private EnumSet<RegExpFlag> flags;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.LiteralRegexID;
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    protected RegExpLiteralNode copy()
    {
        return new RegExpLiteralNode(this);
    }

    //
    // LiteralNode overrides
    //
    
    @Override
    public String getValue()
    {
        return getValue(false);
    }

    @Override
    public String getValue(boolean rawValue)
    {
        String retVal = value;
        if (rawValue || retVal == null)
            return retVal;

        return retVal.substring(1, retVal.lastIndexOf("/"));
    }

    //
    // IRegExpLiteralNode implementations
    //

    @Override
    public Set<RegExpFlag> getFlags()
    {
        return flags != null ? flags : EMPTY_SET;
    }

    @Override
    public String getFlagString()
    {
        if (flags == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (RegExpFlag f : flags)
        {
            sb.append(f.getCode());
        }
        return sb.toString();
    }

    @Override
    public boolean hasFlag(RegExpFlag flag)
    {
        return flags != null && flags.contains(flag);
    }
}
