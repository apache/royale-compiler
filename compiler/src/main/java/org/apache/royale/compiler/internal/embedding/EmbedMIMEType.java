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

package org.apache.royale.compiler.internal.embedding;

/**
 * Map MIME types to file extensions
 */
public enum EmbedMIMEType
{
    JPEG("image/jpeg", new String[] {".jpg", ".jpeg"}),
    JPG("image/jpg", new String[] {".jpg", ".jpeg"}),
    PNG("image/png", ".png"),
    GIF("image/gif", ".gif"),
    MP3("audio/mpeg", ".mp3"),
    FLASH("application/x-shockwave-flash", ".swf"),
    TEXT("text/plain", ".txt"),
    PROPERTIES("text/plain", ".properties"),
    XML("text/xml", ".xml"),
    PBJ("application/x-pbj", ".pbj"),
    OCT_STRM("application/octet-stream"),
    SKIN("skin"),
    TTF("application/x-font-truetype", ".ttf"),
    TTC("application/x-font-truetype-collection", ".ttc"),
    OTF("application/x-font-opentype", ".otf"),
    FONT("application/x-font"),
    DFONT("application/x-dfont", ".dfont");

    private EmbedMIMEType(String name)
    {
        this.name = name;
        this.extensions = new String[0];
    }

    private EmbedMIMEType(String name, String extension)
    {
        this.name = name;
        this.extensions = new String[] {extension};
    }

    private EmbedMIMEType(String name, String[] extensions)
    {
        this.name = name;
        this.extensions = extensions;
    }

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * Convert a mime type string to the corresponding enum
     * @param mimeName A mime type string
     * @return MimeType enum value
     */
    public static EmbedMIMEType getMimeTypeFromMimeString(String mimeName)
    {
        if (mimeName == null || mimeName.isEmpty())
            return null;

        String lcMimeName = mimeName.toLowerCase();
        for (EmbedMIMEType mimeType : EmbedMIMEType.values())
        {
            if (mimeType.name.equals(lcMimeName))
            {
                return mimeType;
            }
        }

        return null;
    }

    /**
     * Look at the filename passed in and try and guess the MIME type
     * based on file extension.
     * @param filename The path to the file being embedded.
     * @return mimeType or null if could not be determined
     */
    public static EmbedMIMEType getMimeTypeFromFilename(String filename)
    {
        if (filename == null || filename.isEmpty())
            return null;

        String lcFilename = filename.toLowerCase();
        for (EmbedMIMEType mimeType : EmbedMIMEType.values())
        {
            for (String extension : mimeType.extensions)
            {
                int nlen = lcFilename.length();
                int elen = extension.length();
                if (nlen > elen && lcFilename.regionMatches(false, nlen - elen, extension, 0, elen))
                {
                    return mimeType;
                }
            }
        }

        return null;
    }

    private final String name;
    private final String[] extensions;
}
