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
 * This AST node represents an MXML {@code <operation>} tag inside a
 * {@code <WebService>} tag.
 * <p>
 * The {@code <operation>} tag is special in MXML. It doesn't correspond to a
 * property definition in the {@code WebService} class. Instead, it codegens an
 * {@code Operation} in the {@code operations} property. Each
 * {@code <operation>} tag codegens one {@code Operation}.
 * <p>
 * A similar scheme applies to {@code <RemoteObject>} and {@code <method>}.
 */
public interface IMXMLWebServiceOperationNode extends IMXMLInstanceNode
{
    /**
     * @return The value of the {@code name} property of an {@code <operation>}.
     */
    String getOperationName();
}
