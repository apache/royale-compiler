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

package org.apache.royale.compiler.internal.fxg.dom.types;

/**
 * The VerticalAlign class.
 * 
 * <pre>
 *   0 = top
 *   1 = middle
 *   2 = bottom
 *   3 = justify
 *   4 = inherit
 * </pre>
 */
public enum VerticalAlign
{
    /**
     * The enum representing an 'top' VerticalAlign.
     */
    TOP,

    /**
     * The enum representing an 'middle' VerticalAlign.
     */    
    MIDDLE,

    /**
     * The enum representing an 'bottom' VerticalAlign.
     */    
    BOTTOM,
    
    /**
     * The enum representing an 'justify' VerticalAlign.
     */    
    JUSTIFY,
    
    /**
     * The enum representing an 'inherit' VerticalAlign.
     */    
    INHERIT;
}
