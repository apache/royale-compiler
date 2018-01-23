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

package org.apache.royale.utils;


/**
 * This is all that is left of a rather large collection of utilties.
 * 
 * The documentation for these utilities used to say:
 *      String utilities which exist in JDK 1.4 but are unavailable in JDK 1.3
 *      The jakarta oro package is used for regular expressions support.
 */
public class StringUtils
{
	/**
	 * character escaping.  For example, "\u0041-\u0043" returns "\\u0041-\\u0043".
	 *
	 * @param s The string to be formatted.
	 * @return a formatted string
	 */
	public static String formatString(String s)
	{
		StringBuilder result = new StringBuilder(s.length() + 2);

		result.append('"');
		for (int i = 0; i < s.length(); i++)
		{
			switch (s.charAt(i))
			{
			case '\\':
                // Leave unicode characters as is.
                if ((i + 1 < s.length()) && (s.charAt(i + 1) == 'u'))
                {
                    result.append("\\");
                }
                else
                {
                    result.append("\\\\");
                }
				break;
			case '"':
				result.append("\\\"");
				break;
			case '\b':
				result.append("\\b");
				break;
			case '\t':
				result.append("\\t");
				break;
			case '\f':
				result.append("\\f");
				break;
			case '\r':
				result.append("\\r");
				break;
			case '\n':
				result.append("\\n");
				break;
			default:
				if (s.charAt(i) < ' ')
				{
					result.append("\\x").append((int) s.charAt(i)).append("X");
				}
				else
				{
					result.append(s.charAt(i));
				}
			}
		}
		result.append('"');
		return result.toString();
	}
	
	public static String join(String[] a, String c)
	{
	    StringBuilder sb = new StringBuilder();
	    
	    int n = a.length;
	    for (int i = 0; i < n; i++)
	    {
	        sb.append(a[i]);
	        if (i < n - 1)
	            sb.append(c);
	    }
	    
	    return sb.toString();
	}
}
