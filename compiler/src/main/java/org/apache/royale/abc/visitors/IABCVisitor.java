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

import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.Metadata;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;

/**
 * The IABCVisitor is the ABC program-level data sink and visitor generator. A
 * program using the AET to construct an ABC program first instantiates an
 * IABCVisitor implementation instance, usually
 * org.apache.royale.abc.util.ABCEmitter, and get visitors for the classes,
 * scripts, and other data elements in the ABC by calling the factory methods
 * declared here.
 */
public interface IABCVisitor extends IVisitor
{
    void visit(int majorVersion, int minorVersion);

    /**
     * Visit a script.
     * 
     * @return the script's visitor. May be null if the script is not to be
     * processed.
     */
    IScriptVisitor visitScript();

    /**
     * Visit a class.
     * 
     * @param iinfo - the class' instance info.
     * @param cinfo - the class' class info.
     * @return the class' visitor. May be null if the class is not to be
     * processed.
     */
    IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo);

    /**
     * Visit a method.
     * 
     * @param minfo - the Method's method info.
     * @return the method body visitor. May be null if the method is not to be
     * processed.
     */
    IMethodVisitor visitMethod(MethodInfo minfo);

    /**
     * Visit a pooled integer value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledInt(Integer i);

    /**
     * Visit a pooled unsigned integer value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledUInt(Long l);

    /**
     * Visit a pooled double value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledDouble(Double d);

    /**
     * Visit a pooled string value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledString(String s);

    /**
     * Visit a pooled namespace value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledNamespace(Namespace ns);

    /**
     * Visit a pooled namespace set value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledNsSet(Nsset nss);

    /**
     * Visit a pooled name value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledName(Name n);

    /**
     * Visit a pooled metadata value.
     * 
     * @note values introduced as operands of instructions need not use this
     * method.
     */
    void visitPooledMetadata(Metadata md);
}
