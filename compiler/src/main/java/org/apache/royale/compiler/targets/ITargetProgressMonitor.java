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

package org.apache.royale.compiler.targets;

/**
 * Interface that clients of Royale can implement to receive progress
 * information as an ITarget compiles.
 * <p>
 * The same ITargetProgressMonitor instance may be added to any number of
 * ITargets.
 */
public interface ITargetProgressMonitor
{
    /**
     * Notifies that the compile operation is done; that is, either the task is
     * completed or the user canceled it.
     */
    void done(ITarget target);
    
    /**
     * Returns whether cancellation of current compile operation for the 
     * specified target has been requested.
     * Long-running operations should poll to see if cancellation
     * has been requested.
     *
     * @param target the target to check whether cancellation has been requested.
     * @return <code>true</code> if cancellation has been requested for 
     * the specified target, and <code>false</code> otherwise
     */
    boolean isCanceled(ITarget target);

    /**
     * Reports the percentage of the main work that has been completed so far.
     * This is called by ITarget implementations during compilation process.
     * This method guarantees that the value reported by this method will be
     * more or equal than the previous reported value for the specified target.
     * The frequency of this call might differ for each compilation process.
     * However, the specified target guarantees to notify the clients using this
     * method every time it determines a newly completed work. (Such as after
     * each compilation unit gets compiled successfully.)
     * <p>
     * This method will be called from the same call stack as the call into one
     * of the methods of ITarget or one of its sub-interfaces, so it would be
     * <b>unwise</b> to call any methods of ITarget or its sub-interfaces from
     * the implementation of this method.
     * 
     * @param target the target for which progress information is being updated.
     * @param percent the percentage of the main task completed so far.
     */
    void percentCompleted(ITarget target, int percent);
}
