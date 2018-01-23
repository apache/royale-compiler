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

package org.apache.royale.swc;

/**
 * Each {@code ISWCComponent} object maps to a {@code <component>} entry in the
 * catalog file.
 */
public interface ISWCComponent
{
    /**
     * Display name of a component.
     * @return name
     */
    String getName();

    /**
     * Qualified name of a component. For example:
     * {@code spark.layouts:VerticalAlign}.
     * @return qname
     */
    String getQName();

    /**
     * Namespace URI.
     * @return URI
     */
    String getURI();

    /**
     * Preview attribute value.
     * @return preview
     */
    String getPreview();

    /**
     * Relative path of the icon image file.
     * @return icon path
     */
    String getIcon();
    
    /**
     * Get the the script where the component is defined.
     * @return script
     */
    ISWCScript getScript();
}
