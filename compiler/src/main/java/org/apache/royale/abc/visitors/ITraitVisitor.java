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
 * An ITraitVisitor generates a IMetadataVisitor to define a trait's metadata, and
 * records name=value trait attributes.
 */
public interface ITraitVisitor extends IVisitor
{
    /**
     * Start visiting trait.
     */
    void visitStart();

    /**
     * Visit the trait's metadata.
     * 
     * @param count number of metadata objects
     * @return the IMetadataVisitor that will define the metadata.
     */
    IMetadataVisitor visitMetadata(int count);

    /**
     * Record a name=value trait attribute, e.g.,
     * <xmp>trait_visitor.visitAttribute(Trait.TRAIT_FINAL, Boolean.TRUE);</xmp>
     * 
     * @param attrName - the attribute's name.
     * @param attrValue - the attribute's value.
     * @note many attributes are enabled by their presence, value may not be
     * checked. Refer to the AVM spec for information on a particular attribute.
     */
    void visitAttribute(String attrName, Object attrValue);
}
