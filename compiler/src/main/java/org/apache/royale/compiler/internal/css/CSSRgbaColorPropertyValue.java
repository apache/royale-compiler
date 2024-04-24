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

package org.apache.royale.compiler.internal.css;

import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

/**
 * Implementation for CSS rgb (0,0,0) property values.
 */
public class CSSRgbaColorPropertyValue extends CSSPropertyValue
{

    /**
     * Create a color property value from AST with rgb color value such as
     * {@code rgb(100%, 0%, 0%) }.
     * 
     * @param rawRgb
     * @param tree AST  
     * @param tokenStream token stream
     */
    protected CSSRgbaColorPropertyValue(final String rawRgb,
            final CommonTree tree, final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.PROPERTY_VALUE);
        this.token = tree.token;
        this.rawRgb = rawRgb; 
        this.colorInt = getIntValue( this.rawRgb ) ;
    }
    

    /**
     * Computes from the given rgb definition a int value. 
     * 
     * @param rgba definition - rgba(100, 0, 0)
     * @return int value bit color.
     */
    protected static long getIntValue(String rgb)
    {
        rgb = rgb.substring(5, rgb.length() - 1);
        
        StringBuffer sb = new StringBuffer();
        Iterator<Object> iterator = null;
        if (rgb.indexOf(",") != -1)
        { 
            // separated by commas is considered a legacy mode
            StringTokenizer st = new StringTokenizer(rgb, ",");
            iterator = st.asIterator();
        }
        else
        {
            // separated by whitespace (with / before alpha) is considered modern
            String[] elements = rgb.split("(\\s*\\/\\s*)|(\\s+)");
            iterator = Arrays.<Object>stream(elements).iterator();
        }
        int tokenIndex = 0;
        while (iterator.hasNext())
        {
            int digit;
            String t = (String) iterator.next();
            t = t.trim();
            if (t.contains("%"))
            {
                t = t.replaceAll("%", "");
                digit = ( Float.valueOf(t).intValue() * 255) / 100;
                sb.append(Character.forDigit((digit >> 4) & 15, 16));
                sb.append(Character.forDigit(digit & 15, 16));
            }
            else
            {
                if (tokenIndex < 3)
                {
                    digit = Float.valueOf(t).intValue();
                    sb.append(Character.forDigit((digit >> 4) & 15, 16));
                    sb.append(Character.forDigit(digit & 15, 16));
                }
                else // alpha
                {
                    Float alpha = Float.valueOf(t);
                    alpha *= 255;
                    digit = alpha.intValue();
                    sb.insert(0, Character.forDigit((digit >> 4) & 15, 16));
                    sb.insert(1, Character.forDigit(digit & 15, 16));
                }
            }
            tokenIndex++;       
        }
        return Long.parseLong( sb.substring(0, 8), 16 );
    }

    private final Token token;
    private final long colorInt;
    private final String rawRgb;
 
    /**
     * @return Integer value bit color.
     */
    public long getColorAsLong()
    {
        return colorInt;
    }

    /**
     * Get the original rgb definition in the CSS document.
     * <p>
     * For example: {@code 255,0,0}, {@code 100%,0,0}, {@code 255,0,100%}.
     * 
     * @return Original rgb property value text in the CSS document.
     */
    public String getRawRgb()
    {
        return rawRgb;
    }

    @Override
    public String toString()
    {
        return token.getText();
    }
}
