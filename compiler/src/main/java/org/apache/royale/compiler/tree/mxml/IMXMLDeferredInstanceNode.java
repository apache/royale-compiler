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

package org.apache.royale.compiler.tree.mxml;

/**
 * This AST node represents the instance of
 * <code>mx.core.DeferredInstanceFromClass</code> or
 * <code>mx.core.DeferredInstanceFromFunction</code> that the compiler
 * implicitly creates as the value for a property or style of type
 * <code>mx.core.IDeferredInstance</code> or
 * <code>mx.core.ITransientDeferredInstance</code>.
 * <p>
 * An {@link IMXMLDeferredInstanceNode} has exactly one child, which can be
 * either an {@link IMXMLClassNode} (in the case of an
 * <code>DeferredInstanceFromClass</code>) or an {@link IMXMLInstanceNode} (in
 * the case of an <code>DeferredInstanceFromFunction</code)). Gordon Smith
 */
public interface IMXMLDeferredInstanceNode extends IMXMLInstanceNode
{
    /**
     * If the sole child is an {@link IMXMLClassNode}, this method gets it and
     * {@link #getInstanceNode()} returns <code>null</code>
     */
    IMXMLClassNode getClassNode();

    /**
     * If the sole child is not a {@link IMXMLClassNode}, this method gets it
     * and {@link #getClassNode()} returns <code>null</code>
     */
    IMXMLInstanceNode getInstanceNode();
}
