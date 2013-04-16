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

package org.apache.flex.compiler.mxml;

/**
 * Represents a blob of MXML.
 * <p>
 * An MXML blob is a large chunk of MXML data that was passed over during
 * tokenization. A blob, for example could be the contents of an
 * <code>&lt;fx:Private&gt; tag.
 */
public interface IMXMLTagBlobData extends IMXMLUnitData
{
    /**
     * Gets the content of the blob.
     * 
     * @return The blob's content as a <code>String</code>.
     */
    String getName();
}
