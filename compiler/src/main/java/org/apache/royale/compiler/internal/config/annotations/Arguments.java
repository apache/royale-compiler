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
 * Display names for the arguments of the annotated configuration option. This
 * annotation also implies the number of expected arguments the annotated option
 * expects. Use @{@link InfiniteArguments} to override the implication.
 * <p>
 * For example:
 * 
 * <pre>
 * -default-script-limits [max-recursion-depth] [max-execution-time]
 * </pre>
 * 
 * is defined as
 * 
 * <pre>
 * &#64;Config()
 * &#64;Arguments({"max-recursion-depth", "max-execution-time"})
 * public void setDefaultScriptLimits(...);
 * </pre>
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Arguments
{
    /**
     * {@code path-element} is a commonly used argument name for options taking
     * a file path.
     */
    public static final String PATH_ELEMENT = "path-element";

    /**
     * {@code path-element} is a commonly used argument name for options taking
     * a file path.
     */
    public static final String CLASS = "class";

    /**
     * @return an array of argument names
     */
    String[] value() default "";
}
