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

package org.apache.royale.compiler.css;

/**
 * Namespace definition maps a namespace URI to a prefix name. For example:
 * 
 * <pre>
 * &#64namespace s "library://ns.adobe.com/flex/spark";
 * </pre>
 * 
 * The {@code @namespace} directive maps "library://ns.adobe.com/flex/spark" to
 * prefix "s". The selector can be gated with "s" to style "spark" components,
 * like {@code s|Button} .
 * <p>
 * Every CSS document can have a default namespace definition without specifying
 * a prefix.
 * 
 * <pre>
 * &#64namespace "library://ns.adobe.com/flex/mx";
 * </pre>
 * 
 * Then all the type selectors without a namespace prefix defaults to components
 * in the default namespace.
 */
public interface ICSSNamespaceDefinition extends ICSSNode
{
    /**
     * Get the prefix defined in the @namespace directive. The result is null if
     * no prefix is specified.
     * 
     * @return Prefix string or null.
     */
    String getPrefix();

    /**
     * Get the URI of the namespace.
     * 
     * @return Namespace URI.
     */
    String getURI();
}
