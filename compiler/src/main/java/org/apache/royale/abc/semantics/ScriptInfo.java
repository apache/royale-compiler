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

import org.apache.royale.abc.semantics.Trait;
import org.apache.royale.abc.semantics.Traits;

/**
 * A representation of a <a href="http://learn.adobe.com/wiki/display/AVM2/4.10+Script">script info</a>.
 */
public class ScriptInfo
{
    /**
     * The method that initializes this script.
     */
    private MethodInfo init = null;

    /**
     * The script's traits.  Trait entries
     * in this Traits store become properties 
     * of the script's global object.
     */
    private Traits traits = new Traits();

    /**
     * Set the script's initialization method.
     * @param i the initialization method's {@link MethodInfo method info}.
     */
    public void setInit(MethodInfo i)
    {
        init = i;
    }

    /**
     * Get the script's initialization method.
     * @return the initialization method's {@link MethodInfo method info}. 
     */
    public MethodInfo getInit()
    {
        return this.init;
    }

    /**
     * Add a {@link Trait trait} to the script.
     * @param t the {@link Trait trait}.
     */
    public void addTrait(Trait t)
    {
        traits.add(t);
    }

    /**
     * Get this script's {@link Traits}.
     * @return this script's {@link Traits}
     */
    public Traits getTraits()
    {
        return traits;
    }
}
