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

package org.apache.royale.compiler.internal.mxml;

import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;

/**
 * This class stores a MXML-namespace-URI-to-manifest-file mapping.
 */
public class MXMLNamespaceMapping implements IMXMLNamespaceMapping
{
    public MXMLNamespaceMapping(String uri, String manifestFileName)
    {
        assert uri != null;
        assert manifestFileName != null;
        this.uri = uri;
        this.manifestFileName = manifestFileName;
    }

    private String uri;
    private String manifestFileName;

    @Override
    public String getURI()
    {
        return uri;
    }

    @Override
    public String getManifestFileName()
    {
        return manifestFileName;
    }

    /**
     * For debugging.
     * 
     * @return Debug string representation of this object.
     */
    @Override
    public String toString()
    {
        return uri + "->" + manifestFileName;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof MXMLNamespaceMapping))
            return false;
        MXMLNamespaceMapping other = (MXMLNamespaceMapping)obj;
        if (!(uri.equals(other.uri)))
            return false;
        return manifestFileName.equals(other.manifestFileName);
    }

}
