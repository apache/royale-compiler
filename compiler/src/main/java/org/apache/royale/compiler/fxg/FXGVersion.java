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

package org.apache.royale.compiler.fxg;

import java.lang.Long;

/**
 * FXGVersion is an enumeration of the different versions of FXG Specification.
 */
public final class FXGVersion
{
    private static final double k_1_0 = 1.0D;
    private static final double k_2_0 = 2.0D;

    // Versions publicly supported by this compiler
    public static final FXGVersion v1_0 = new FXGVersion(k_1_0);
    public static final FXGVersion v2_0 = new FXGVersion(k_2_0);

    // Internal representation
    private final double version;

    private FXGVersion(double version) throws FXGVersionException
    {
        if (Double.isNaN(version) || version < 1.0D)
            throw new FXGVersionException(String.valueOf(version));
        
        this.version = version;
    }

    /**
     * Returns a FXGVersion object representing 'version' or null if 'version'
     * is not a properly formatted version string. This routine is intended to
     * be called internally and is suitable for use with version strings
     * extracted from FXG files.
     * 
     * @param version - the version double value.
     * @throws FXGVersionException if the version is invalid.
     */
    public static FXGVersion newInstance(double version) throws FXGVersionException
    {
        return new FXGVersion(version);
    }

    /**
     * Returns the version represented by this FXGVersion instance as a String
     * value (e.g., "1.0", "1.2", etc.).
     * 
     * @return String
     */
    public String asString()
    {
        return String.valueOf(version);
    }

    /**
     * Compares whether this FXGVersion's value is equal to the value of the
     * version parameter.
     * 
     * @param version The version of FXG.
     * @return true if this object's value is equal to the 'version' object's
     * value; false otherwise
     */
    public boolean equalTo(FXGVersion version)
    {
        return (compareTo(version) == 0);
    }

    /**
     * Compares whether this FXGVersion's value is equal to the version value of
     * the object. If object is not a FXGVersion, then false is returned. This
     * method exists to provide compatibility with the common usage case of
     * using equals(obj) to test object instance equality. It's use is
     * discouraged because it is not a strongly typed method. The method
     * equalTo(FXGVersion) is the preferred method to use for equality testing.
     * 
     * @param object The object.
     * @return true if this object's value is equal to the 'version' object's
     * value; false otherwise
     */
    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof FXGVersion))
            return false;
        return equalTo((FXGVersion)object);
    }

    /**
     * Returns the hashCode
     */
    @Override
    public int hashCode()
    {
        return (Long.valueOf(Double.doubleToLongBits(version))).hashCode();
    }
 
    /**
     * Compares whether this FXGVersion's value is greater than the value of the
     * version parameter.
     * 
     * @param version A version of FXG.
     * @return true if this object's value is greater than the 'version'
     * object's value; false otherwise
     */
    public boolean greaterThan(FXGVersion version)
    {
        return (compareTo(version) > 0);
    }


    /**
     * @param version
     * @return -1, 0, or 1 depending on whether this object's value is less
     * than, equal to, or greater than to specified version, respectively.
     */
    private int compareTo(FXGVersion version)
    {
        if (version == null)
            throw new RuntimeException("Cannot compare FXGVersion to a null value.");

        if (version == this)
            return 0;

        double thisVersion = this.asDouble();
        double compareVersion = version.asDouble();
        if (thisVersion < compareVersion)
            return -1;
        else if (thisVersion > compareVersion)
            return 1;
        else
            return 0;
    }

    /**
     * @return the version double value.
     */
    public double asDouble()
    {
        return this.version;
    }
    
    /**
     * @return the major version.
     */
    public long getMajorVersion()
    {
        return (long)this.version;
    }
    
    public class FXGVersionException extends RuntimeException 
    {
        private static final long serialVersionUID = 9034248618973261847L;
        
        private final String version;
        
        public FXGVersionException(String version)
        {
            super();
            this.version = version;
        }
        
        public String getVersion()
        {
            return version;
        }
    }
    
}
