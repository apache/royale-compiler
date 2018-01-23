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

package org.apache.royale.compiler.definitions.metadata;

/**
 * An IMetaTagAttribute represents one attribute inside a metadata annotation,
 * such as <code>name="click"</code> inside [Event(name="click",
 * type="flash.events.MouseEvent")]</code>.
 * <p>
 * Attributes are typically key/value pairs of Strings, but keyless String
 * values are also allowed. In this case, <code>getValue()</code> returns null.
 */
public interface IMetaTagAttribute
{
    /**
     * Gets the key of the attribute, or <code>null</code> if this attribute is
     * a keyless value.
     * 
     * @return A String for the attribute key.
     */
    String getKey();

    /**
     * Does this attribute have a key?
     * 
     * @return <code>true</code> if there is a key.
     */
    boolean hasKey();

    /**
     * Gets the value of the attribute.
     * 
     * @return A String for the attribute value.
     */
    String getValue();
}
