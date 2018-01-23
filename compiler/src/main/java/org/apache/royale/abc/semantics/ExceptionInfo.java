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

package org.apache.royale.abc.semantics;

import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.Name;

/**
 * ExceptionInfo represents an exception-handling annotation in a MethodBodyInfo
 * structure.
 */
public class ExceptionInfo
{
    /**
     * Constructor.
     * 
     * @param from - the start of the "try" block.
     * @param to - the end of the "try" block.
     * @param target - the "catch" block to transfer to in the event of an
     * exception in the [from, to) region.
     * @param type - the type of exceptions to handle.
     * @param catchVar - the name of the catch variable.
     */
    public ExceptionInfo(Label from, Label to, Label target, Name type, Name catchVar)
    {
        this.from = from;
        this.to = to;
        this.target = target;
        this.exceptionType = type;
        this.catchVar = catchVar;
        this.live = true;
    }

    /**
     * The start of the covered region.
     */
    
    private Label from;
    /**
     * The end of the covered region.
     */
    private Label to;
    
    /**
     * The "catch block" to transfer to in the event of an exception in the
     * [from, to) region.
     */
    private Label target;

    /**
     * The type of exceptions to handle.
     */
    private Name exceptionType;

    /**
     * The name of the catch variable.
     */
    private Name catchVar;

    /**
     *  Dead exception-handler?
     */
    private boolean live;

    /**
     * @return the label at the start of the "try" block.
     */
    public Label getFrom()
    {
        return from;
    }

    /**
     * @return the label at the end of the "try" block.
     */
    public Label getTo()
    {
        return to;
    }

    /**
     * @return the label of the "catch" block.
     */
    public Label getTarget()
    {
        return target;
    }

    /**
     * @return the type of exception to be handled.
     */
    public Name getExceptionType()
    {
        return exceptionType;
    }

    /**
     * @return the name of the "catch" variable.
     */
    public Name getCatchVar()
    {
        return catchVar;
    }

    /**
     *  Reset an ExceptionHandler's live state.
     *  @param live - the new value for liveness.
     */
    public void setLive(boolean live)
    {
        this.live = live;
    }

    /**
     *  Is this ExceptionHandler live?
     *  @return the ExceptionHandler's live state.
     */
    public boolean isLive()
    {
        return this.live;
    }
}
