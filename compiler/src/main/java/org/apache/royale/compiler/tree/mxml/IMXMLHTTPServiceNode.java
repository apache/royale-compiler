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
 * This AST node represents an {@code <HTTPService>} tag.
 *
 * The {@code <HTTPService>} tag is an ordinary instance tag from the compiler's
 * point of view, except for the fact that an {@code <HTTPService>} tag can have
 * special child {@code <request>} tags mixed in with its other child tags
 * for properties and events.
 * A {@code <request>} tag is a special kind of property tag,
 * corresponding to the <code>request</code> property.
 * This property has type <code>Object</code>, but you don't have to write
 * an MXML {@code <Object>} tag to set its value; instead you just
 * write the name/value pairs.
 * <p>
 * Each {@code <request>} tag is represented by a child {@code IMXMLHTTPServiceRequestPropertyNode}.
 */
public interface IMXMLHTTPServiceNode extends IMXMLInstanceNode
{
}
