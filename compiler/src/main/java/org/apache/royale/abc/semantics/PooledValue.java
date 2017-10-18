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

package org.apache.royale.abc.semantics;

import org.apache.royale.abc.ABCConstants;

/**
 * A representation of a value in a <a href="http://learn.adobe.com/wiki/display/AVM2/4.3+Constant+pool">constant pool</a>
 * that is not a name, namespace, or namespace set.
 */
public class PooledValue
{
    /**
     * Construct a PooledValue, kind determined by the value object's type.
     * 
     * @param value - the PooledValue's value. Must be one of a restricted set
     * of Java types.
     */
    public PooledValue(Object value)
    {
        //  Deduce the kind from the value.
        if (value instanceof Integer) {
            this.kind = ABCConstants.CONSTANT_Int;
        }
        else if (value instanceof Long) {
            this.kind = ABCConstants.CONSTANT_UInt;
        }
        else if (value instanceof Double) {
            this.kind = ABCConstants.CONSTANT_Double;
        }
        else if (value instanceof String) {
            this.kind = ABCConstants.CONSTANT_Utf8;
        }
        else if (value instanceof Boolean) {
            if (Boolean.TRUE.equals(value)) {
                this.kind = ABCConstants.CONSTANT_True;
            } else {
                this.kind = ABCConstants.CONSTANT_False;
            }
        }
        else if (ABCConstants.UNDEFINED_VALUE == value) {
            this.kind = ABCConstants.CONSTANT_Undefined;
        }
        else if (value == ABCConstants.NULL_VALUE) {
            this.kind = ABCConstants.CONSTANT_Null;
        }
        else if (value instanceof Namespace) {
            this.kind = ((Namespace) value).getKind();
        }
        else {
            throw new IllegalArgumentException("Unknown value class " + value == null ? "null" : value.getClass().toString());
        }

        this.value = value;
    }

    /**
     * Construct a PooledValue, kind taken directly from the caller.
     */
    public PooledValue(int kind, Object value)
    {
        this.kind = kind;
        this.value = value;
    }

    /**
     * The value's ABC kind designator.
     */
    private int kind;
    
    /**
     * The value's pool index. Set when the containing MethodInfo or other
     * container type structure undergoes its endVisit() event.
     */
    private int poolIndex = -1;

    /**
     * The value, if there is one. Many value kinds don't have a value or don't
     * consider it relevant.
     */
    private final Object value;

    /**
     * Set the value's pool index. This is done when the object that owns this
     * PooledValue, e.g., a MethodInfo, gets to its visitEnd() event and
     * registers all its constant information with the ABCEmitter's constant
     * pools.
     */
    public void setPoolIndex(int index)
    {
        assert (this.poolIndex == -1);
        this.poolIndex = index;
    }

    /**
     * @return the value's pool index.
     */
    public int getPoolIndex()
    {
        assert (this.poolIndex != -1);
        return this.poolIndex;
    }

    /**
     * @return the value's kind.
     * @see "the kind values in ABCConstants."
     */
    public int getKind()
    {
        return this.kind;
    }

    /**
     * @return this value's value object.
     */
    public Object getValue()
    {
        return this.value;
    }

    /**
     * @return this value's value object, cast to Integer.
     */
    public Integer getIntegerValue()
    {
        return (Integer)this.value;
    }

    /**
     * @return this value's value object, cast to Long (uint).
     */
    public Long getLongValue()
    {
        return (Long)this.value;
    }

    /**
     * @return this value's value object, cast to Double (Number).
     */
    public Double getDoubleValue()
    {
        return (Double)this.value;
    }

    /**
     * @return this value's value object, cast to String.
     */
    public String getStringValue()
    {
        return (String)this.value;
    }

    /**
     * @return this value's value object, cast to Namespace
     */
    public Namespace getNamespaceValue()
    {
        return (Namespace)this.value;
    }

}
