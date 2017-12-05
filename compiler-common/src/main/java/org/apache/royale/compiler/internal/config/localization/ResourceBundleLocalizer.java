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

package org.apache.royale.compiler.internal.config.localization;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.royale.compiler.Messages;

/**
 * ILocalizer implementation, which supports looking up text in
 * resource bundles.
 */
public class ResourceBundleLocalizer implements ILocalizer
{
    @Override
    public ILocalizedText getLocalizedText( Locale locale, String id )
    {
        String prefix = id;

        while (true)
        {
            int dot = prefix.lastIndexOf( '.' );
            if (dot == -1)
            {
                break;
            }
            prefix = prefix.substring( 0, dot );
            String suffix = id.substring( dot + 1 );
            try
            {
                String message = Messages.getString(suffix);
                if (message != null)
                {
                    return new ResourceBundleText(message);
                }
            }
            catch (MissingResourceException e)
            {
            }
        }

        return null;
    }

    private class ResourceBundleText implements ILocalizedText
    {
        public ResourceBundleText( String text )
        {
            this.text = text;
        }
        @Override
        public String format( Map<String, Object> parameters )
        {
            return LocalizationManager.replaceInlineReferences( text, parameters );
        }
        private String text;
    }
}
