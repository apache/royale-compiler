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

package org.apache.royale.swf.tags;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>FileAttributes</code> tag in a SWF file.
 * <p>
 * The FileAttributes tag defines characteristics of the SWF file. This tag is
 * required for SWF 8 and later and must be the first tag in the SWF file.
 * Additionally, the FileAttributes tag can optionally be included in all SWF
 * file versions. The HasMetadata flag identifies whether the SWF file contains
 * the Metadata tag. Flash Player does not care about this bit field or the
 * related tag but it is useful for search engines. The UseNetwork flag
 * signifies whether Flash Player should grant the SWF file local or network
 * file access if the SWF file is loaded locally. The default behavior is to
 * allow local SWF files to interact with local files only, and not with the
 * network. However, by setting the UseNetwork flag, the local SWF can forfeit
 * its local file system access in exchange for access to the network. Any
 * version of SWF can use the UseNetwork flag to set the file access for locally
 * loaded SWF files that are running in Flash Player 8 or later.
 */
public class FileAttributesTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     */
    public FileAttributesTag()
    {
        super(TagType.FileAttributes);
    }
    
    private boolean useDirectBlit;
    private boolean useGPU;
    private boolean hasMetadata;
    private boolean as3;
    private boolean useNetwork;

    // not in spec
    private boolean suppressCrossDomainCaching;

    // not in spec
    private boolean swfRelativeURLs;

    public boolean isAS3()
    {
        return as3;
    }

    public boolean isHasMetadata()
    {
        return hasMetadata;
    }

    public boolean isSuppressCrossDomainCaching()
    {
        return suppressCrossDomainCaching;
    }

    public boolean isSWFRelativeURLs()
    {
        return swfRelativeURLs;
    }

    public boolean isUseDirectBlit()
    {
        return useDirectBlit;
    }

    public boolean isUseGPU()
    {
        return useGPU;
    }

    public boolean isUseNetwork()
    {
        return useNetwork;
    }

    public void setAS3(boolean value)
    {
        as3 = value;
    }

    public void setHasMetadata(boolean value)
    {
        hasMetadata = value;
    }

    public void setSuppressCrossDomainCaching(boolean value)
    {
        suppressCrossDomainCaching = value;
    }

    public void setSWFRelativeURLs(boolean value)
    {
        swfRelativeURLs = value;
    }

    public void setUseDirectBlit(boolean value)
    {
        useDirectBlit = value;
    }

    public void setUseGPU(boolean value)
    {
        useGPU = value;
    }

    public void setUseNetwork(boolean value)
    {
        useNetwork = value;
    }
}
