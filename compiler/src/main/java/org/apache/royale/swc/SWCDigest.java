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

package org.apache.royale.swc;

/**
 * Implementation of a SWC digest.
 */
public class SWCDigest implements ISWCDigest
{
    /**
     * Supported hash-types
     */
    public static final String SHA_256 = "SHA-256";
    
    private boolean signed;
    private String type;
    private String value;

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public boolean isSigned()
    {
        return signed;
    }

    @Override
    public String getValue()
    {
        return value;
    }

    /**
     * @param signed the signed to set
     */
    public void setSigned(boolean signed)
    {
        this.signed = signed;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * Set the value from a digest in the form
     * of an array of bytes. This method will
     * convert the bytes to a hex string.
     * 
     * @param value array of bytes. May not be null.
     * 
     * @throws NullPointerException if value is null.
     */
    public void setValue(byte[] value)
    {
        if (value == null)
            throw new NullPointerException("value may not be null.");
       
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < value.length; i++)
        {
            String s = Integer.toHexString((value[i] & 0xff));
            if (s.length() == 1)
            {
                buf.append("0");
            }

            buf.append(s);
       }
       
       setValue(buf.toString());       
    }
}
