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
 * The IClassVisitor generates traits visitors for an ABC class' static and
 * instance traits.
 * 
 * @see org.apache.royale.abc.semantics.ClassInfo which holds the rest of the
 * class' information.
 */
public interface IClassVisitor extends IVisitor
{
    /**
     * Begin visiting a class.
     */
    void visit();

    /**
     * Visit the class' static (class) traits.
     * 
     * @return a ITraitsVisitor used to define the class' static traits.
     */
    ITraitsVisitor visitClassTraits();

    /**
     * Visit the class' instance traits.
     * 
     * @return a ITraitsVisitor used to define the class' instance traits.
     */
    ITraitsVisitor visitInstanceTraits();
}
