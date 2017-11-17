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

package org.apache.royale.compiler.internal.codegen.typedefs.reference;

import com.google.javascript.rhino.Node;

/**
 * @author: Frederic Thomas Date: 05/07/2015 Time: 19:34
 */
public class ParameterReference extends BaseReference
{

    private String name;

    public ParameterReference(final ReferenceModel model, final Node node, final String qualifiedName)
    {
        super(model, node, qualifiedName, null);
        name = node.getString();
    }

    public ParameterReference(final ReferenceModel model, final String name, final String qualifiedName)
    {
        super(model, null, qualifiedName, null);
        this.name = name;
    }

    public ParameterReference(final ReferenceModel model, final Node parameterNode)
    {
        this(model, parameterNode, "Object");
    }

    @Override
    public void emit(final StringBuilder sb)
    {
        // Emitted by the Method / Function reference.
    }

    public String getName()
    {
        return name;
    }
}
