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

package org.apache.royale.compiler.tree.metadata;

/**
 * Represents an Inspectable metadata tag
 */
public interface IInspectableTagNode extends ITypedTagNode
{
    /**
     * @return the values of the enumeration attribute, or an empty array
     */
    String[] getAllowedValues();

    /**
     * @return the value of the environment attribute, or null
     */
    String getEnvironment();

    /**
     * @return the value of the format attribute, or null
     */
    String getFormat();

    /**
     * @return the value of the name attribute, or null
     */
    String getName();

    /**
     * @return the value of the variable attribute, or null
     */
    String getVariable();

    /**
     * @return the value of the verbose attribute, or null
     */
    String getVerbose();

    /**
     * @return the value of the arrayType attribute, or null
     */
    String getArrayType();
}
