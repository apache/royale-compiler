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

package org.apache.royale.compiler.config;

/**
 *  The ApplicationDomainTarget enum defines the possible values of the 
 *  application domain an RSL can be loaded into at runtime.
 *  The application domain target specifies a relative application 
 *  domain that is resolved at runtime.
 */
public enum ApplicationDomainTarget
{
    /**
     *  The default behavior for RSL loading is to load an RSL as high in 
     *  the parent module factory chain as possible. In order to load an RSL
     *  into a parent module factory, that module factory must have been 
     *  compiled with that RSL specified in the compiler options. If no parent module 
     *  factories were compiled with that RSL , then the RSL will be loaded in
     *  the application domain of the module factory loading the RSL.
     */
    DEFAULT("default"),
    
    /**
     *  The application domain of the current module factory.
     */
    CURRENT("current"),
    
    /**
     *  The application domain of the parent module factory.
     */
    PARENT("parent"),
    
    /**
     *  The application domain of the top-level module factory.
     */
    TOP_LEVEL("top-level");
    
    
    private String applicationDomainValue;

    /**
     * Not for public consumption.
     * 
     * @param applicationDomainValue
     */
    private ApplicationDomainTarget( String applicationDomainValue )
    {
        this.applicationDomainValue = applicationDomainValue;
    }

    /**
     * The value the Flex runtime understands.
     * 
     * @return The {@link String} value of the enum.
     */
    public String getApplicationDomainValue()
    {
        return applicationDomainValue;
    }
    

}
