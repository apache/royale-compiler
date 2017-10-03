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

package org.apache.royale.abc.visitors;

/**
 * Base interface for all visitors reachable from an {@link IABCVisitor}.
 * <p>
 * Currently this interface exists so that clients can keep a collection of
 * visitor's whose {@link #visitEnd()} methods need to be called at some point
 * in the future.
 */
public interface IVisitor
{
    /**
     * Indicates that no further method calls will be made on this visitor
     * instance. Interfaces that derive from this interface add more specific
     * meaning to this method.
     */
    void visitEnd();
}
