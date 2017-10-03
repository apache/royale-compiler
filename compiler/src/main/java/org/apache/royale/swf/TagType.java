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

package org.apache.royale.swf;

/**
 * Type code of a SWF tag.
 * 
 * @see SWF File Format Specification Version 10.
 */
public enum TagType
{
    End(0),
    ShowFrame(1),
    DefineShape(2),
    PlaceObject(4),
    RemoveObject(5),
    DefineBits(6),
    DefineButton(7),
    JPEGTables(8),
    SetBackgroundColor(9),
    DefineFont(10),
    DefineText(11),
    DoAction(12),
    DefineFontInfo(13),
    DefineSound(14),
    StartSound(15),
    DefineButtonSound(17),
    SoundStreamHead(18),
    SoundStreamBlock(19),
    DefineBitsLossless(20),
    DefineBitsJPEG2(21),
    DefineShape2(22),
    DefineButtonCxform(23),
    Protect(24),
    PlaceObject2(26),
    RemoveObject2(28),
    DefineShape3(32),
    DefineText2(33),
    DefineButton2(34),
    DefineBitsJPEG3(35),
    DefineBitsLossless2(36),
    DefineEditText(37),
    DefineSprite(39),
    ProductInfo(41), // TODO not in spec, verify this is necessary
    FrameLabel(43),
    SoundStreamHead2(45),
    DefineMorphShape(46),
    DefineFont2(48),
    ExportAssets(56),
    ImportAssets(57),
    EnableDebugger(58),
    DoInitAction(59),
    DefineVideoStream(60),
    VideoFrame(61),
    DefineFontInfo2(62),
    DebugID(63), // TODO not documented in the spec
    EnableDebugger2(64),
    ScriptLimits(65),
    SetTabIndex(66),
    FileAttributes(69),
    PlaceObject3(70),
    ImportAssets2(71),
    DefineFontAlignZones(73),
    CSMTextSettings(74),
    DefineFont3(75),
    SymbolClass(76),
    Metadata(77),
    DefineScalingGrid(78),
    DoABC_OLD(72), // TODO resolve this with DoABC from spec
    DoABC(82),
    DefineShape4(83),
    DefineMorphShape2(84),
    DefineSceneAndFrameLabelData(86),
    DefineBinaryData(87),
    DefineFontName(88),
    StartSound2(89),
    DefineBitsJPEG4(90),
    DefineFont4(91),
    EnableTelemetry(93),
    Undefined(-1);

    private final int value;

    /**
     * Bind the integer tag value with a field.
     * 
     * @param value tag type code
     */
    private TagType(int value)
    {
        this.value = value;
    }

    /**
     * Get the integer tag type code.
     * 
     * @return tag type code
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Reverse lookup TagType by its type code value.
     * 
     * @param value type code
     * @return TagType enum value
     */
    public static TagType getTagType(int value)
    {
        for (TagType tagType : TagType.values())
        {
            if (tagType.value == value)
                return tagType;
        }

        return Undefined;
    }
}
