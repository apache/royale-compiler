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

package org.apache.royale.compiler.clients.problems;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.royale.compiler.Messages;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Abstract class that provide useful functionality for
 * formatting and retrieving the localized message of {@link CompilerProblem}'s.
 */
public class ProblemFormatter
{       
    public static ProblemFormatter DEFAULT_FORMATTER = new ProblemFormatter();
    
    /**
     * Returns a readable description of the problem, by substituting field
     * values for named placeholders such as ${name} in the message specified 
     * for this compiler problem.
     * 
     * @param problem problem whose message to find 
     * @return A localized readable description of the problem.
     */
    public String format(ICompilerProblem problem)
    {
        Class<?> c = problem.getClass();
        
        String problemMessage = getMessage(c.getSimpleName());
        
        if(problemMessage == null)
        {          
            // Use reflection to look up the value of the static field
            // named 'description' on the problem subclass.
            // For example, if this problem is a SyntaxError, this will be
            // "Syntax error: '${tokenText}' is not allowed here"
            try
            {
                Field descriptionField = c.getDeclaredField("DESCRIPTION");
                problemMessage = (String)descriptionField.get(null);
            }
            catch (Exception e)
            {
                //No message found for this problem so just returned 
                //the name of the class which will also be a bit helpful.
                return '!' + c.getSimpleName() + '!';
            }
        }
               
        // Use reflection on this class and its superclasses
        // to build a name/value map from the public non-static fields.
        final Map<String, Object> map = new HashMap<String, Object>();   
        while (c != Object.class)
        {
            for (final Field field : c.getDeclaredFields())
            {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) &&
                    !Modifier.isStatic(modifiers))
                {
                    try
                    {
                        // Normally there should not be null values in a problem field, but
                        // It has happened once or twice... Better to not attempt the substitution,
                        // since it will just NPE and no problems we be correctly reported.
                        Object value = field.get(problem);
                        if (value != null)
                            map.put(field.getName(), value);
                    }
                    catch (Exception e)
                    {
                        // ignore
                    }
                }
            }
            c = c.getSuperclass();
        }
        
        //Replace all the tokens in the description using this key/value map
        return substitute(problemMessage, map);
    }
    
    /**
     * Resolves the tokens in the text using the key/value pairs in the
     * specified map. Each token in the message is searched in the map's key
     * list and if found, the token replaced with the value of found key.
     * 
     * @param text compiler message to resolve
     * @param parameters map that contains key/value pairs used to resolve the
     * tokens in the specified message.
     * @return resolved, token-free compiler message
     */
    public static String substitute(String text, final Map<String, Object> parameters)
    {
        for (String key : parameters.keySet())
        {
            String value = parameters.get(key).toString();
            if(value != null)
            {
                // Need to escape the value in case it contains special characters, like '$'
                value = Matcher.quoteReplacement(value);
                text = text.replaceAll("[$][{]" + key + "[}]", value);
            }
        }
        return text;
    }
    
    /**
     * Returns message for the specified key using the resource 
     * bundle generated for the specified locale.
     * 
     * @param key key to find (should be the class name of the problem class)
     * @return the message associated with the specified key
     */
    protected String getMessage(String key)
    {
        return Messages.getString(key);
    }
}
