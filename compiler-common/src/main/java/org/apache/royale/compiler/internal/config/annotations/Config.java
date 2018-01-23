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

package org.apache.royale.compiler.internal.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration annotation.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Config
{
    /**
     * @return True if this option is an advanced option.
     */
    boolean advanced() default false;

    /**
     * @return True if this option does not show in the help message.
     */
    boolean hidden() default false;

    /**
     * If {@code allowMultiple} is false, a variable can only be set once in a
     * given file or command-line. Setting a variable that doesn't allow
     * "multiple" twice will cause
     * {@code ConfigurationException.IllegalMultipleSet}.
     * <p>
     * Set this to true if we want to accumulate values on the option. It's
     * usually useful for collection-based options like
     * {@code -compiler.external-library-path}.
     * 
     * @return True if the setter can be called multiple times
     */
    boolean allowMultiple() default false;

    /**
     * @return True if the values are treated as paths.
     */
    boolean isPath() default false;

    /**
     * @return True if the option is displayed.
     */
    boolean displayed() default true;
    
    /**
     * @return True if the option is only visible to {@code compc}.
     */
    boolean compcOnly() default false;
    
    /**
     * @return True indicates that the option is no longer 
     * supported and will not have any affect.
     */
    boolean removed() default false;
    
    /**
     * @return True if the commandline parser is in "greedy" mode.
     */
    boolean greedy() default false;
    
    /**
     * @return true if the option must be set.
     */
    boolean isRequired() default false;
}
