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

package org.apache.royale.compiler.units.requests;

/**
 * Interface to all request objects obtained from methods on ICompilationUnit.
 * 
 * @param <T> The type of the result of the request.
 * @param <U> The type of the object on which the request is being made.
 * @see org.apache.royale.compiler.units.ICompilationUnit
 */
public interface IRequest<T extends IRequestResult, U>
{
    /**
     * Blocks until requested operation is complete.
     * 
     * @return The result of the request.
     * @throws InterruptedException 
     */
    T get() throws InterruptedException;
    
    /**
     * Returns true if the requested operation is complete.
     * @return true if the requested operation is complete, false otherwise.
     */
    boolean isDone();

    /**
     * Returns the time at which the request was first created. Consumers of the
     * result of this request can use this time to determine if their cached
     * results are still valid.
     * <p>
     * If the result of the request has been cached to disk, then the time
     * returned from this method should be the time the request whose data was
     * cached to disk was created. In other words, data cached to disk should
     * contain the time stamp.
     * <p>
     * This method should return before the results of the request are
     * available.
     * <p>
     * This method is used to propagate invalidations when using on disk caches
     * containing request results from a previously run java process.
     * 
     * @return A long value representing the time the request was created,
     * measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     */
    long getTimeStamp();
    
    /**
     * Get the object on which this request is being made.
     * @return The object on which this request is being made.
     */
    U getRequestee();
}
