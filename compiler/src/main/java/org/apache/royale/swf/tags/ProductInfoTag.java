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

import java.util.Date;

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>ProductInfo</code> tag in a SWF file.
 * <p>
 * This represents a ProductInfo SWF tag. It is used to embed information about
 * the product, which was used to construct the SWF, including it's edition,
 * major version, minor version, and build number, and the date the SWF was
 * constructed.
 */
public class ProductInfoTag extends Tag implements IManagedTag
{
    /**
     * Constructor.
     * 
     * @param product product type
     * @param edition edition type
     * @param majorVersion major version of the SDK
     * @param minorVersion minor version of the SDK
     * @param build build number of the SDK
     * @param compileDate date the file is compiled
     */
    public ProductInfoTag(Product product, Edition edition,
                          byte majorVersion, byte minorVersion, long build,
                          long compileDate)
    {
        super(TagType.ProductInfo);
        
        this.product = product;
        this.edition = edition;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.build = build;
        this.compileDate = compileDate;
    }

    private Edition edition;
    private Product product;
    private byte majorVersion;
    private byte minorVersion;
    private long build;
    private long compileDate;

    public Edition getEdition()
    {
        return edition;
    }

    public void setEdition(Edition edition)
    {
        this.edition = edition;
    }

    public Product getProduct()
    {
        return product;
    }

    public void setProduct(Product product)
    {
        this.product = product;
    }

    public byte getMajorVersion()
    {
        return majorVersion;
    }

    public void setMajorVersion(byte majorVersion)
    {
        this.majorVersion = majorVersion;
    }

    public byte getMinorVersion()
    {
        return minorVersion;
    }

    public void setMinorVersion(byte minorVersion)
    {
        this.minorVersion = minorVersion;
    }

    public long getBuild()
    {
        return build;
    }

    public void setBuild(long build)
    {
        this.build = build;
    }

    public long getCompileDate()
    {
        return compileDate;
    }

    public void setCompileDate(long compileDate)
    {
        this.compileDate = compileDate;
    }

    @Override
    public String description()
    {
        return String.format(
                "product=%s, edition=%s, version=%d.%d build %d, compiled on %s",
                product, edition, majorVersion, minorVersion, build, new Date(compileDate));
    }
    
    /**
     * Product Types
     * */
    public static enum Product
    {
        UNKNOWN(0, "unknown"),
        J2EE(1, "Macromedia Flex for J2EE"),
        DOTNET(2, "Macromedia Flex for .NET"),
        ROYALE(3, "Apache Royale");

        public static Product fromCode(int code)
        {
            for (final Product product : Product.values())
            {
                if (product.code == code)
                    return product;
            }
            return null;
        }

        Product(int code, String name)
        {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public int getCode()
        {
            return code;
        }

        public String getName()
        {
            return name;
        }
    }

    /**
     * Edition Types
     */
    public static enum Edition
    {
        DEVELOPER(0, "Developer Edition"),
        FULL_COMMERCIAL(1, "Full Commercial Edition"),
        NON_COMMERCIAL(2, "Non-Commercial Edition"),
        EDUCATIONAL(3, "Educational Edition"),
        NFR(4, "NFR Edition"),
        TRIAL(5, "Trial Edition"),
        NONE(6, "");

        public static Edition fromCode(int code)
        {
            for (final Edition edition : Edition.values())
            {
                if (edition.code == code)
                    return edition;
            }
            return null;
        }

        Edition(int code, String name)
        {
            this.code = code;
            this.name = name;
        }

        private final int code;
        private final String name;

        public int getCode()
        {
            return code;
        }

        public String getName()
        {
            return name;
        }
    }
}
