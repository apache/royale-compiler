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
import org.apache.royale.compiler.tree.ASTNodeID;

/**
 * Special block node for a block gated with a configuration condition. For
 * example:
 * 
 * <pre>
 * CONFIG::DEBUG 
 * {
 *     import my.debug.classes.*;
 * }
 * </pre>
 */
public final class ConfigConditionBlockNode extends BlockNode
{
    private boolean enabled;

    /**
     * Create a enabled or disabled configuration config block.
     * 
     * @param enabled True if the configuration condition evaluates to true.
     */
    public ConfigConditionBlockNode(boolean enabled)
    {
        super();
        this.enabled = enabled;
        this.setContainerType(ContainerType.CONFIG_BLOCK);
    }

    /**
     * Disabled configuration condition block doesn't have children.
     */
    @Override
    public int getChildCount()
    {
        if (enabled)
            return super.getChildCount();
        else
            return 0;
    }

    @Override
    protected void fillInOffsets()
    {
        boolean oldEnabled = enabled;
        enabled = true;
        super.fillInOffsets();
        enabled = oldEnabled;
    }

    @Override
    public void normalize(boolean fillInOffsets)
    {
        boolean oldEnabled = enabled;
        enabled = true;
        super.normalize(fillInOffsets);
        enabled = oldEnabled;
    }
    
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append(enabled ? "(enabled) " : "(disabled) ");
        return super.buildInnerString(sb);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.ConfigBlockID;
    }
}
