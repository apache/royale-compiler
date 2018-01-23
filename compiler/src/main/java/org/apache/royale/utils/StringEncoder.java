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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for encoding String
 */
public class StringEncoder
{
    /**
     * utility method to get the hash code of a specified string as a string.
     * 
     * @param input String to hash
     * @return resulting hash as a String
     */
    public static String stringToHashCodeString(String input)
    {
        int hashCode = input.hashCode();
        StringBuffer hashCodeBuffer = new StringBuffer();
        hashCodeBuffer.append(toHex((byte)(hashCode >> 24)));
        hashCodeBuffer.append(toHex((byte)((hashCode >> 16) & 0xFF)));
        hashCodeBuffer.append(toHex((byte)((hashCode >> 8) & 0xFF)));
        hashCodeBuffer.append(toHex((byte)(hashCode & 0xFF)));
        return hashCodeBuffer.toString();
    }
    
    /**
     * utility method to md5 a String
     * 
     * @param input String to md5
     * @return resulting md5 as a String
     */
    public static String stringToMD5String(String input)
    {
        String md5String = "";
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = digest.digest(input.getBytes("UTF8"));
            StringBuffer md5Buffer = new StringBuffer();
            for (byte md5Byte : md5Bytes)
                md5Buffer.append(toHex(md5Byte));

            md5String = md5Buffer.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            assert false;
        }
        catch (UnsupportedEncodingException e)
        {
            assert false;
        }

        return md5String;
    }

    /**
     * Simple function to return a two character string with the hex digits
     * that represent the specified byte.
     * 
     * @param b The byte to convert to a two character hex string.
     * @return a two character string with the hex digits that represent the
     * specified byte
     */
    private static String toHex(byte b)
    {
        int highNibble = (b >> 4) & 0xF;
        int lowNibble = b & 0xF;
        return Integer.toHexString(highNibble) + Integer.toHexString(lowNibble);
    }
}
