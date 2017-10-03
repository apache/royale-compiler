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

import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A utility class for looking up localized text.
 */
public class LocalizationManager
{
    private Locale locale = Locale.getDefault();
    private List<ILocalizer> localizers = new LinkedList<ILocalizer>();
    
    private static LocalizationManager instance = new LocalizationManager();
    public static LocalizationManager get()
    {
        return instance;
    }

    private LocalizationManager()
    {
    }

    public void addLocalizer( ILocalizer localizer )
    {
        localizers.add( localizer );
    }

    private ILocalizedText getLocalizedTextInner( Locale locale, String id )
    {
        for (Iterator<ILocalizer> it = localizers.iterator(); it.hasNext(); )
        {
            ILocalizer localizer = it.next();

            ILocalizedText text = localizer.getLocalizedText( locale, id );

            if (text != null)
            {
                return text;
            }
        }

        return null;
    }

    private ILocalizedText getLocalizedText( Locale locale, String id )
    {
        ILocalizedText t = getLocalizedTextInner( locale, id );

        if ((t == null) && (locale.getCountry().length() > 0) && (locale.getVariant().length() > 0))
        {
            t = getLocalizedTextInner( new Locale( locale.getLanguage(), locale.getCountry() ), id );
        }

        if ((t == null) && (locale.getCountry().length() > 0))
        {
            t = getLocalizedTextInner( new Locale( locale.getLanguage() ), id );
        }

        return t;
    }

    protected static String replaceInlineReferences( String text, Map<String, Object> parameters )
    {
        if (parameters == null)
            return text;

        int depth = 100;
        while (depth-- > 0)
        {
            int o = text.indexOf( "${" );
            if (o == -1)
                break;
            if ((o >= 1) && (text.charAt( o-1 ) == '$'))
            {
                o = text.indexOf( "${", o+2 );
                if (o == -1)
                    break;
            }

            int c = text.indexOf( "}", o );

            if (c == -1)
            {
                return null;
            }
            String name = text.substring( o + 2, c );
            String value = null;
            if (parameters.containsKey( name ) && (parameters.get( name ) != null))
            {
                value = parameters.get( name ).toString();
            }

            if (value == null)
            {
                value = "";
            }
            text = text.substring( 0, o ) + value + text.substring( c + 1 );
        }
        return text.replaceAll( "[$][$][{]", "\\${" );
    }

	public String getLocalizedTextString( String id )
	{
		return getLocalizedTextString( id, Collections.<String, Object>emptyMap());
	}

	public String getLocalizedTextString( String id, Map<String, Object> parameters )
	{
		return getLocalizedTextString(locale, id, parameters );
	}

    public String getLocalizedTextString( Locale locale, String id, Map<String, Object> parameters )
    {
        ILocalizedText t = getLocalizedText( locale, id );

        if ((t == null) && !locale.equals(locale))
        {
            t = getLocalizedText(locale, id );
        }
        if ((t == null) && !locale.getLanguage().equals( "en" ))
        {
            t = getLocalizedText( new Locale( "en" ), id );
        }

        return (t == null)? null : t.format( parameters );
    }

	public String getLocalizedTextString( Object object )
	{
		String s = getLocalizedTextString(locale, object );

        return s;
    }

    // todo - this is a pretty specialized helper function, hoist up to client code?
    public String getLocalizedTextString( Locale locale, Object object )
    {
        String id = object.getClass().getName().replaceAll( "\\$", "." );

        Map<String, Object> parameters = new HashMap<String, Object>();
        Class<?> c = object.getClass();

        while (c != Object.class)
        {
            Field[] fields = c.getDeclaredFields();

            for (int i = 0; i < fields.length; ++i)
            {
                Field f = fields[i];

                if (!Modifier.isPublic( f.getModifiers() ))
                {
                    continue;
                }
                if (Modifier.isStatic( f.getModifiers() ))
                {
                    continue;
                }

                try
                {
                    parameters.put( f.getName(), f.get( object ) );
                }
                catch (Exception e)
                {
                }
            }
            c = c.getSuperclass();
        }

        String s = null;
        if ((parameters.containsKey( "id" ) && parameters.get( "id" ) != null ))
        {
            String subid = parameters.get( "id" ).toString();
            if (subid.length() > 0)
            {
                s = getLocalizedTextString( locale, id + "." + subid, parameters );
            }
        }
        if (s == null)
        {
            s = getLocalizedTextString( locale, id, parameters );
        }

        if (s == null)
        {
            s = id;

            if (parameters != null)
            {
                s += "[";
                for (Iterator<Map.Entry<String, Object>> it = parameters.entrySet().iterator(); it.hasNext(); )
                {
                    Map.Entry<String, Object> e = it.next();
                    s += e.getKey();
                    if (e.getValue() != null)
                        s += "='" + e.getValue() + "'";
                    if (it.hasNext())
                        s += ", ";
                }
                s += "]";
            }
            return s;
        }

        return s;
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
}
