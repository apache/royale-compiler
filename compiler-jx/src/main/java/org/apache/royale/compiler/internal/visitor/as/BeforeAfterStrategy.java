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
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.visitor.IASNodeStrategy;

/**
 * The {@link BeforeAfterStrategy} implements a before and after {@link IASNode}
 * handler.
 * <p>
 * When {@link #handle(IASNode)} is called on an instance of this class, the
 * default {@link #handle(IASNode)} method will first call the
 * {@link #getBefore()} handle() method , the will call the supers handle()
 * implementation which is to call the {@link #getHandler()} handle() method.
 * Finally, the {@link #getAfter()} handler's handle() method will be called.
 * <p>
 * Currently, this strategy is used for indenting and {@link IBlockNode} pre and
 * post symbol management.
 * 
 * @author Michael Schmalle
 */
public class BeforeAfterStrategy extends ASNodeHandler
{
    private IASNodeStrategy before;

    /**
     * Returns the {@link IASNodeStrategy} called before the
     * {@link #getHandler()}'s handle() method.
     * 
     * @return The before handler.
     */
    public IASNodeStrategy getBefore()
    {
        return before;
    }

    /**
     * Sets the before handler.
     * 
     * @param before The before handler.
     */
    public void setBefore(IASNodeStrategy value)
    {
        this.before = value;
    }

    private IASNodeStrategy after;

    /**
     * Returns the {@link IASNodeStrategy} called after the
     * {@link #getHandler()}'s handle() method.
     * 
     * @return The after handler.
     */
    public IASNodeStrategy getAfter()
    {
        return after;
    }

    /**
     * Sets the after handler.
     * 
     * @param after The after handler.
     */
    public void setAfter(IASNodeStrategy value)
    {
        this.after = value;
    }

    /**
     * Constructor, creates a strategy that implements a before and after
     * {@link IASNodeStrategy}.
     * 
     * @param handler The handler that will be called between the before and
     * after {@link #handle(IASNode)} method.
     * @param before The before handler.
     * @param after The after handler.
     */
    public BeforeAfterStrategy(IASNodeStrategy handler, IASNodeStrategy before,
            IASNodeStrategy after)
    {
        super(handler);
        this.before = before;
        this.after = after;
    }

    @Override
    public void handle(IASNode node)
    {
        before(node);
        super.handle(node);
        after(node);
    }

    /**
     * Called before the {@link #handle(IASNode)} method.
     * <p>
     * If the {@link #getAfter()} strategy is <code>null</code>, this method
     * does nothing.
     * 
     * @param node The current {@link IASNode} being handled by the strategy.
     */
    protected void after(IASNode node)
    {
        if (after != null)
            after.handle(node);
    }

    /**
     * Called after the {@link #handle(IASNode)} method.
     * <p>
     * If the {@link #getBefore()} strategy is <code>null</code>, this method
     * does nothing.
     * 
     * @param node The current {@link IASNode} being handled by the strategy.
     */
    protected void before(IASNode node)
    {
        if (before != null)
            before.handle(node);
    }
}
