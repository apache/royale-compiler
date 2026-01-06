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

package org.apache.royale.swf.builders;

import org.apache.royale.compiler.fonts.FontFace;
import org.apache.royale.swf.types.ZoneRecord;
import org.apache.royale.utils.Trace;

/**
 * A simple class to decouple FlashType ZoneRecord construction from
 * FontBuilder.
 */
public class ZoneRecordBuilder
{
    private static final String DEFAULT_BUILDER = "flash.fonts.flashtype.FlashTypeZoneRecordBuilder";
    
    protected String fontAlias;
    protected FontBuilder fontBuilder;
    protected FontFace fontFace;

    protected ZoneRecordBuilder()
    {
    }

    public void setFontAlias(String alias)
    {
        fontAlias = alias;
    }

    public void setFontBuilder(FontBuilder builder)
    {
        fontBuilder = builder;
    }

    public void setFontFace(FontFace face)
    {
        fontFace = face;
    }

    /**
     * This no-op method returns an empty ZoneRecord. Subclasses should
     * override this method. 
     */
    public ZoneRecord build(int character)
    {
        // Return an empty Zone Record...
        ZoneRecord zoneRecord = new ZoneRecord();
        return zoneRecord;
    }

    public static ZoneRecordBuilder createInstance()
    {
        try
        {
            Class c = Class.forName(DEFAULT_BUILDER);
            ZoneRecordBuilder builder = (ZoneRecordBuilder)c.newInstance();
            return builder;
        }
        catch (Throwable t)
        {
            if (Trace.error)
                Trace.trace("ZoneRecordBuilder implementation not found '" + DEFAULT_BUILDER + "'");
        }

        return null;
    }
}
