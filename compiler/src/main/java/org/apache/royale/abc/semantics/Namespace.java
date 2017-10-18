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

package org.apache.royale.abc.semantics;

import org.apache.royale.abc.ABCConstants;

/**
 * A representation of a <a href="http://learn.adobe.com/wiki/display/AVM2/4.3+Constant+pool">namespace</a>.
 */
public class Namespace
{
    /**
     * A prime number unlikely to be a divisor of the hash table size, used to
     * generate composite hash keys.
     * 
     * @warn if you copy this, pick a new prime to generate distinct hash keys
     * in different classes.
     */
    private static final long PRIME_MULTIPLIER = 9929;

    /**
     * Whether or not private namespaces should be merged.
     * 
     * This changes the semantics of private namespaces which is
     * officially Not A Good Thing (tm), but is the only way inlining
     * can be made to work until we can do it at the ABC link time, but
     * that requires better static analysis which we don't have yet.
     */
    private boolean mergePrivateNamespaces = false;

    public Namespace(int kind)
    {
        this(kind, "");
    }

    public Namespace(int kind, String name)
    {
        this.kind = kind;
        this.apiVersion = extractApiVersion(name);

        this.name = this.apiVersion != ABCConstants.NO_API_VERSION ?
                stripApiVersion(name) :
                name;
    }

    /**
     * The Namespace's kind.
     * 
     * @see ABCConstants
     */
    private final int kind;

    /**
     * The Namespace's name, stripped of any API version markers.
     */
    private final String name;

    /**
     * The Namespace's API version, or ABCConstants.NO_API_VERSION.
     */
    private final int apiVersion;

    /**
     * Use name-oriented hashing unless the kind is private, for private
     * namespaces use identity semantics.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if (kind != ABCConstants.CONSTANT_PrivateNs || mergePrivateNamespaces)
        {
            long result = this.name.hashCode();
            result = result * PRIME_MULTIPLIER + this.kind;
            if (this.apiVersion != ABCConstants.NO_API_VERSION)
                result = result * PRIME_MULTIPLIER + this.apiVersion;

            return (int)result;
        }
        else
        {
            //  Always use identity comparision for private namespaces.
            return super.hashCode();
        }
    }

    /**
     * Check name-oriented equality unless the kind is private, for private
     * namespaces use identity semantics.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj instanceof Namespace && this.kind != ABCConstants.CONSTANT_PrivateNs || mergePrivateNamespaces)
        {
            Namespace that = (Namespace) obj;
            if(that != null) {
                return that.kind == this.kind &&
                        that.apiVersion == this.apiVersion &&
                        that.name.equals(this.name);
            }
            return false;
        }

        //  Other object is not a Namespace.
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("%s:\"%s\"", getKindString(), this.name);
    }

    /**
     * @return the Namespace's kind.
     */
    public int getKind()
    {
        return kind;
    }

    /**
     * Helper method used by toString().
     * 
     * @return A readable string for the namespace kind.
     */
    private String getKindString()
    {
        switch (kind)
        {
            case ABCConstants.CONSTANT_Namespace:
                return "Ns";
                
            case ABCConstants.CONSTANT_PackageNs:
                return "PackageNs";
                
            case ABCConstants.CONSTANT_PackageInternalNs:
                return "PackageInternalNs";
                
            case ABCConstants.CONSTANT_ProtectedNs:
                return "ProtectedNs";
                
            case ABCConstants.CONSTANT_ExplicitNamespace:
                return "ExplicitNs";
                
            case ABCConstants.CONSTANT_StaticProtectedNs:
                return "StaticProtectedNs";
                
            case ABCConstants.CONSTANT_PrivateNs:
                return "PrivateNs";
        }

        return "Unknown(0x" + Integer.toHexString(kind) + ")";
    }

    /**
     * @return the namespace's name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the namespace's name and API version, encoded into the ABC-format
     * URI.
     */
    public String getVersionedName()
    {
        if (getApiVersion() == ABCConstants.NO_API_VERSION)
            return this.name;
        else
            return this.name + new String(new int[] {getApiVersion() + ABCConstants.MIN_API_MARK}, 0, 1);
    }

    /**
     * @return the namespace's API version.
     */
    public int getApiVersion()
    {
        return this.apiVersion;
    }

    /**
     * Get the API version from a Namespace's name.
     * 
     * @return the Namespace's version number, or NO_API_VERSION if not
     * specified.
     */
    public static int extractApiVersion(String uri)
    {
        if (uri == null || uri.length() == 0)
            return ABCConstants.NO_API_VERSION;

        int last = uri.codePointAt(uri.length() - 1);

        if (last >= ABCConstants.MIN_API_MARK && last <= ABCConstants.MAX_API_MARK)
            return last - ABCConstants.MIN_API_MARK;

        return ABCConstants.NO_API_VERSION;
    }

    /**
     * Get a Namespace's name, minus any API versioning information.
     */
    static String stripApiVersion(String uri)
    {
        return extractApiVersion(uri) > 0 ?
               uri.substring(0, uri.length() - 1) :
               uri;
    }

    /**
     * Set whether or not private namespaces should be merged
     * 
     * @param mergePrivateNamespaces <code>true</code if they should be merged.
     */
    public void setMergePrivateNamespaces(boolean mergePrivateNamespaces)
    {
        assert ((mergePrivateNamespaces == true && kind == ABCConstants.CONSTANT_PrivateNs) ? !name.isEmpty() : true) :
            "private namespaces must have a name when merging enabled";

        this.mergePrivateNamespaces = mergePrivateNamespaces;
    }
}
