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

import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * A ConfigConstNode is a conditional compilation const that only exists at
 * compile time. It's value is computed while parsing is taking place and it is
 * replaced with the literal value it signifies.
 */
public class ConfigConstNode extends VariableNode
{
    /**
     * Object representing an unknown value from our type resolution when
     * determining the value of this const
     */
    public static final Object UNKNOWN_VALUE = new Object();

    /**
     * Constructor.
     * 
     * @param nameNode The identifier node specifying th ename of the config const.
     */
    public ConfigConstNode(IdentifierNode nameNode)
    {
        super(nameNode);
    }
    
    //
    // BaseVariableNode overrides
    //

    @Override
    VariableDefinition buildDefinition()
    {
        ConfigDefinition definition = new ConfigDefinition(nameNode.computeSimpleReference());
        definition.setNode(this);

        fillInNamespaceAndModifiers(definition);
        setDefinition(definition);
        definition.setInitializer(this.getAssignedValueNode());

        return (VariableDefinition)definition;
    }

    @Override
    public DefinitionBase getDefinition()
    {
        if (definition == null)
            return buildDefinition();
        
        return (DefinitionBase)definition;
    }
    
    //
    // Other methods
    //

    /**
     * Resets any information contained within this node
     */
    public void reset()
    {
        definition = null;
        setParent(null);
    }
    
    //
    // Inner types
    //

    /**
     * Internal implementation of ConstantDefinition that caches its value.
     */
    public static class ConfigDefinition extends ConstantDefinition
    {
        /**
         * Constructor.
         */
        public ConfigDefinition(String name)
        {
            super(name);
        }

        private Object resolvedValue = null;

        @Override
        public Object resolveValue(ICompilerProject project)
        {
            if (resolvedValue == UNKNOWN_VALUE)
            {
                return null;
            }
            if (resolvedValue == null)
            {
                Object resolveValue = super.resolveValue(project);
                if (resolveValue == null)
                {
                    resolvedValue = UNKNOWN_VALUE;
                }
                else
                {
                    resolvedValue = resolveValue;
                }

            }
            return resolvedValue;
        }
    }
}
