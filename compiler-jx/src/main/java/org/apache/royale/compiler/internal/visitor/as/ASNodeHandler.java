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

package org.apache.royale.compiler.internal.visitor.as;

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.visitor.IASNodeStrategy;

/**
 * A concrete implementation of the {@link IASNodeStrategy} that allows a
 * subclass to either implement the {@link IASNode} handling directly or pass a
 * child {@link IASNodeStrategy} that this class will delegate it's
 * {@link #handle(IASNode)} method to.
 * 
 * @author Michael Schmalle
 * 
 * @see BeforeAfterStrategy
 */
public class ASNodeHandler implements IASNodeStrategy
{
    private IASNodeStrategy handler;

    /**
     * Returns the {@link IASNodeStrategy} currently being used to handle
     * {@link IASNode} AST.
     * 
     * @return The current strategy.
     */
    public IASNodeStrategy getHandler()
    {
        return handler;
    }

    /**
     * Sets the {@link IASNode} handler strategy.
     * 
     * @param handler The {@link IASNodeStrategy} to handle the specific
     * {@link IASNode}.
     */
    public void setHandler(IASNodeStrategy handler)
    {
        this.handler = handler;
    }

    /**
     * Constructor, used when this handler directly implements
     * {@link #handle(IASNode)} and does not composite a child
     * {@link IASNodeStrategy}.
     */
    public ASNodeHandler()
    {
    }

    /**
     * Constructor, creates a node strategy that composites a child
     * {@link IASNodeStrategy} implemented in the {@link #handle(IASNode)}
     * method.
     * 
     * @param handler The {@link IASNode} handler to be used in this strategy.
     */
    public ASNodeHandler(IASNodeStrategy handler)
    {
        this.handler = handler;
    }

    @Override
    public void handle(IASNode node)
    {
        handler.handle(node);
    }
}
