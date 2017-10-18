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
 * Represents a four-part version number, such as <code>4.5.12.12345</code>.
 * <p>
 * The second, third, and fourth parts, representing a minor version, 
 * a bug-fix version, and a build version, are optional.
 * When comparing versions, a missing part is considered to be 0.
 */
public class Version implements Comparable<Version>
{
    /**
     * Compares two possibly-null Integer values,
     * considering a null Integer to be 0.
     */
    private static int cmp(Integer i1, Integer i2)
    {
        // For comparison purposes, consider a null Integer to be 0.
        int j1 = i1 != null ? i1 : 0;
        int j2 = i2 != null ? i2 : 0;
        
        if (j1 > j2)
            return 1;
        else if (j1 < j2)
            return -1;
        
        return 0;
    }
    
    /**
     * Constructs a new <code>Version</code> instance with all four parts.
     * 
     * @param major The major version number.
     * @param minor The minor version number.
     * @param bugFix The bug-fix version number.
     * @param build The build version number or <code>null</code>.
     */
    public Version(Integer major, Integer minor, Integer bugFix, Integer build)
    {
        assert major >= 0 : "major version number cannot be negative";
        assert minor == null || minor >= 0 : "minor version number cannot be negative";
        assert bugFix == null || bugFix >= 0 : "bugFix version number cannot be negative";
        assert build == null || build >= 0 : "build version number cannot be negative";
        
        this.major = major;
        this.minor = minor;
        this.bugFix = bugFix;
        this.build = build;        
    }
    
    /**
     * Constructs a new <code>Version</code> instance with three parts.
     * 
     * @param major The major version number.
     * @param minor The minor version number or <code>null</code>.
     * @param bugFix The bug-fix version number or <code>null</code>.
     */
    public Version(int major, int minor, int bugFix)
    {
        this(major, minor, bugFix, null);
    }
    
    /**
     * Constructs a new <code>Version</code> instance with two parts.
     * 
     * @param major The major version number.
     * @param minor The minor version number or <code>null</code>.
     */
    public Version(int major, int minor)
    {
        this(major, minor, null, null);
    }
    
    /**
     * Constructs a new <code>Version</code> instance with one part.
     * 
     * @param major The major version number.
     */
    public Version(int major)
    {
        this(major, null, null, null);
    }
    
    /**
     * Constructs a new <code>Version</code> instance from a
     * one-, two-, three-, or four-part dotted version string.
     */
    public Version(String versionString)
    {
        this(versionString.split("\\."));
    }
    
    private Version(String[] parts)
    {
        int n = parts.length;
        assert 1 <= n && n <= 4 : "version string should have 1-4 parts";
        
        this.major = Integer.parseInt(parts[0]);
        this.minor = n >= 2 ? Integer.parseInt(parts[1]) : null;
        this.bugFix = n >= 3 ? Integer.parseInt(parts[2]) : null;
        this.build = n == 4 ? Integer.parseInt(parts[3]) : null;
    }
    
    private final Integer major;
    private final Integer minor;
    private final Integer bugFix;
    private final Integer build;
    
    @Override
    public String toString()
    {
        return getDisplayString();
    }

    @Override
    public int compareTo(Version other)
    {
        int compareBugFix = compareBugFixVersionTo(other);
        if (compareBugFix != 0)
            return compareBugFix;
        
        return cmp(build, other.build);
    }
    
    /**
     * Gets the major version number.
     * 
     * @return An <code>Integer</code> for the major version.
     */
    public Integer getMajor()
    {
        return major;
    }
    
    /**
     * Gets the minor version number number.
     * 
     * @return An <code>Integer</code> for the minor version or <code>null</code>.
     */
    public Integer getMinor()
    {
        return minor;
    }
    
    /**
     * Gets the bug-fix version number.
     * 
     * @return An <code>Integer</code> for the bug-fix version or <code>null</code>.
     */
    public Integer getBugFix()
    {
        return bugFix;
    }
    
    /**
     * Gets the build version number.
     * 
     * @return An <code>Integer</code> for the build version or <code>null</code>.
     */
    public Integer getBuild()
    {
        return build;
    }
    
    /**
     * Converts this <code>Version<code> to a string for display.
     * <p>
     * Null parts of the version are not displayed.
     * 
     * @return A String such as <code>"1.2"</code>.
     */
    public String getDisplayString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append(major);
        
        if (minor != null)
        {
            sb.append('.');
            sb.append(minor);
        }
        
        if (bugFix != null)
        {
            sb.append('.');
            sb.append(bugFix);
        }
        
        if (build != null)
        {
            sb.append('.');
            sb.append(build);
        }
        
        return sb.toString();
    }
 
    /**
     * Compares this <code>Version</code> to another,
     * considering only the major part.
     * 
     * @param other Another <code>Version</code> object.
     * @return 1 if greater, -1 if lesser, 0 if equal
     */
    public int compareMajorVersionTo(Version other)
    {
        return cmp(major, other.major);
    }
    
    /**
     * Compares this <code>Version</code> to another,
     * considering only the major and minor parts.
     * 
     * @param other Another <code>Version</code> object.
     * @return 1 if greater, -1 if lesser, 0 if equal
     */
    public int compareMinorVersionTo(Version other)
    {
        int compareMajor = compareMajorVersionTo(other);
        if (compareMajor != 0)
            return compareMajor;
        
        return cmp(minor, other.minor);
    }
    
    /**
     * Compares this <code>Version</code> to another,
     * considering only the major, minor, and bug-fix parts.
     * 
     * @param other Another <code>Version</code> object.
     * @return 1 if greater, -1 if lesser, 0 if equal
     */
    public int compareBugFixVersionTo(Version other)
    {
        int compareMinor = compareMinorVersionTo(other);
        if (compareMinor != 0)
            return compareMinor;
        
        return cmp(bugFix, other.bugFix);
    }
}
