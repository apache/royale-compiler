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

package org.apache.royale.compiler;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.royale.compiler.clients.problems.ProblemFormatter;

/**
 * Class to look up messages for the compiler client classes. This class looks
 * up strings from a well known bundle. The locale can be modified by the client
 * to display messages in a non-default locale. See the -tools-locale option.
 */
public final class Messages
{
    private static final String BUNDLE_NAME = "org.apache.royale.compiler.messages";
    
    private static ResourceBundle resourceBundle;
    private static Locale locale;
    
    
    /**
     * Get a localized string for the message id.
     * 
     * @param id the id of the message.
     * @return localized string.
     */
    public static String getString(String id)
    {
        String message = null;
        
        try
        {
            message = getBundle().getString(id);
        }
        catch (MissingResourceException e)
        {
            // ignore exception
        }
        
        return message; 
    }

    /**
     * Get a localized string for a parameterized message.
     *  
     * @param id the id of the message.
     * @param parameters map that contains key/value pairs used to resolve the
     * tokens in the specified message.
     * @return resolved, token-free message
     */
    public static String getString(String id, Map<String, Object> parameters)
    {
        return ProblemFormatter.substitute(getString(id), parameters);
    }
    
    public static void setLocale(Locale locale)
    {
        Messages.locale = locale;
    }
    
    protected static ResourceBundle getBundle()
    {
        try 
        {
            if (resourceBundle == null)
            {
                Locale locale = Messages.locale;
                if (locale == null)
                    locale = Locale.getDefault();
                
                resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            }
        }
        catch (MissingResourceException e)
        {
            // fallback to english.
            resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
        }
        
        return resourceBundle;
    }
    
}
