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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Name;

/**
 * A representation of a method's <a href="http://learn.adobe.com/wiki/display/AVM2/4.5+Method+signature">method signature</a>.
 */
public class MethodInfo
{
    // Unknown parameter name.
    public static final String UNKNOWN_PARAM_NAME = "";

    private int flags = 0;

    private String methodName;

    private Name returnType;
    private Vector<Name> paramTypes = new Vector<Name>();
    private Vector<PooledValue> defaultValues = new Vector<PooledValue>();
    private List<String> paramNames;

    /**
     * @return the number of parameters present.
     */
    public int getParamCount()
    {
        return paramTypes.size() + 1;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    /**
     * @return the methodName
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * @param paramTypes the paramTypes to set
     */
    public void setParamTypes(Vector<Name> paramTypes)
    {
        this.paramTypes = paramTypes;
    }

    /**
     * @return the paramTypes
     */
    public Vector<Name> getParamTypes()
    {
        return paramTypes;
    }

    /**
     * @param returnType the returnType to set
     */
    public void setReturnType(Name returnType)
    {
        this.returnType = returnType;
    }

    /**
     * @return the returnType
     */
    public Name getReturnType()
    {
        return returnType;
    }

    /**
     * @param flags the method's flags
     */
    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    /**
     * @return method flags
     */
    public int getFlags()
    {
        return flags;
    }

    /**
     * @return true if the method has optional args
     */
    public boolean hasOptional()
    {
        return (flags & ABCConstants.HAS_OPTIONAL) > 0;
    }

    /**
     * @return true if this method has parameter names.
     */
    public boolean hasParamNames()
    {
        return (flags & ABCConstants.HAS_PARAM_NAMES) > 0;
    }

    public boolean needsRest()
    {
        return (flags & ABCConstants.NEED_REST) > 0;
    }

    /**
     * @return true if this is a native method.
     */
    public boolean isNative()
    {
        return (flags & ABCConstants.NATIVE) > 0;
    }

    @Override
    public String toString()
    {
        if (this.methodName != null)
            return this.methodName;
        else
            return super.toString();
    }

    /**
     * Add a default parameter value.
     * 
     * @post No attempt made to verify consistency of the defaults and the
     * corresponding parameters.
     */
    public void addDefaultValue(PooledValue value)
    {
        if (this.defaultValues == null)
            this.defaultValues = new Vector<PooledValue>();

        this.defaultValues.add(value);
        this.flags |= ABCConstants.HAS_OPTIONAL;
    }

    /**
     * @return this method's default values.
     * @see #hasOptional()
     */
    public Vector<PooledValue> getDefaultValues()
    {
        assert (this.defaultValues != null) : "No default values";
        return this.defaultValues;
    }

    /**
     * @return This method's parameter names, or an empty list if its param
     * names are not set.
     */
    public final List<String> getParamNames()
    {
        if (paramNames == null)
            paramNames = new ArrayList<String>();
        
        return paramNames;
    }

    /**
     * Set the method's parameter names, and update its flags accordingly.
     * 
     * @param param_names - parameter names. Caller should pass null if
     * parameter names are not provided.
     * @post method flags' HAS_PARAM_NAMES field set if param_names not null,
     * reset otherwise.
     */
    public void setParamNames(List<String> param_names)
    {
        this.paramNames = param_names;

        if (this.paramNames != null)
            this.flags |= ABCConstants.HAS_PARAM_NAMES;
        else
            this.flags = this.flags & ~ABCConstants.HAS_PARAM_NAMES;
    }
}
