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

package org.apache.royale.swf.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.io.output.CountingOutputStream;

import org.apache.royale.swf.Header;
import org.apache.royale.swf.Header.Compression;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.SWF;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.TagType;
import org.apache.royale.swf.io.SWFReader.CurrentStyles;
import org.apache.royale.swf.tags.*;
import org.apache.royale.swf.types.ARGB;
import org.apache.royale.swf.types.BevelFilter;
import org.apache.royale.swf.types.BlurFilter;
import org.apache.royale.swf.types.ButtonRecord;
import org.apache.royale.swf.types.CXForm;
import org.apache.royale.swf.types.CXFormWithAlpha;
import org.apache.royale.swf.types.ConvolutionFilter;
import org.apache.royale.swf.types.CurvedEdgeRecord;
import org.apache.royale.swf.types.DropShadowFilter;
import org.apache.royale.swf.types.EndShapeRecord;
import org.apache.royale.swf.types.FillStyle;
import org.apache.royale.swf.types.FillStyleArray;
import org.apache.royale.swf.types.Filter;
import org.apache.royale.swf.types.FocalGradient;
import org.apache.royale.swf.types.GlowFilter;
import org.apache.royale.swf.types.GlyphEntry;
import org.apache.royale.swf.types.GradRecord;
import org.apache.royale.swf.types.Gradient;
import org.apache.royale.swf.types.GradientBevelFilter;
import org.apache.royale.swf.types.GradientGlowFilter;
import org.apache.royale.swf.types.IFillStyle;
import org.apache.royale.swf.types.ILineStyle;
import org.apache.royale.swf.types.KerningRecord;
import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.LineStyle2;
import org.apache.royale.swf.types.LineStyleArray;
import org.apache.royale.swf.types.Matrix;
import org.apache.royale.swf.types.MorphFillStyle;
import org.apache.royale.swf.types.MorphGradRecord;
import org.apache.royale.swf.types.MorphGradient;
import org.apache.royale.swf.types.MorphLineStyle;
import org.apache.royale.swf.types.MorphLineStyle2;
import org.apache.royale.swf.types.RGB;
import org.apache.royale.swf.types.RGBA;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.Shape;
import org.apache.royale.swf.types.ShapeRecord;
import org.apache.royale.swf.types.ShapeWithStyle;
import org.apache.royale.swf.types.SoundEnvelope;
import org.apache.royale.swf.types.SoundInfo;
import org.apache.royale.swf.types.StraightEdgeRecord;
import org.apache.royale.swf.types.StyleChangeRecord;
import org.apache.royale.swf.types.Styles;
import org.apache.royale.swf.types.TextRecord;
import org.apache.royale.swf.types.ZoneRecord;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

/**
 * The implementation of SWF tag, type encoding logic. The SWF file body are
 * buffered in memory using {@code IOutputBitStream}. ZLIB compression is
 * optional. If enabled, compression is on-the-fly via a filtered output stream.
 */
public class SWFWriter implements ISWFWriter
{
    /**
     * Default SWF writer factory.
     */
    private static class SWFWriterFactory implements ISWFWriterFactory
    {

        @Override
        public ISWFWriter createSWFWriter(ISWF swf, Compression useCompression,
                boolean enableDebug, boolean enableTelemetry)
        {
            return new SWFWriter(swf, useCompression, enableDebug, enableTelemetry);
        }

    }

    /**
     * A factory for default SWF writers. These SWF writers just write SWFs
     * without any additional features.
     */
    public static final ISWFWriterFactory DEFAULT_SWF_WRITER_FACTORY = new SWFWriterFactory();

    private static final int RESERVED = 0;
    private static final int SHORT_TAG_MAX_LENGTH = 62;

    /**
     * Compares the absolute values of 4 signed integers and returns the
     * unsigned magnitude of the number with the greatest absolute value.
     */
    public static int maxNum(int a, int b, int c, int d)
    {
        // take the absolute values of the given numbers
        a = Math.abs(a);
        b = Math.abs(b);
        c = Math.abs(c);
        d = Math.abs(d);

        // compare the numbers and return the unsigned value of the one with the greatest magnitude
        return Ints.max(a, b, c, d);
    }

    /**
     * Compares the absolute values of 4 signed doubles and returns the unsigned
     * magnitude of the number with the greatest absolute value.
     */
    public static double maxNum(double a, double b, double c, double d)
    {
        // take the absolute values of the given numbers
        a = Math.abs(a);
        b = Math.abs(b);
        c = Math.abs(c);
        d = Math.abs(d);

        // compare the numbers and return the unsigned value of the one with the greatest magnitude
        return Doubles.max(a, b, c, d);
    }

    /**
     * Calculate number of bits required to represent the given value in double
     * bit value.
     * 
     * @param value signed integer
     * @return number of bits required for SB[]
     */
    public static int requireFBCount(double value)
    {
        return requireSBCount((int)(value * 0x10000));
    }

    /**
     * Calculate number of bits required to represent the given value in signed
     * bit values.
     * 
     * @param value signed integer
     * @return number of bits required for SB[]
     */
    public static int requireSBCount(int value)
    {
        return minBits(Math.abs(value), 1);
    }

    public static int requireSBCount(int... values)
    {
        final int array[] = new int[values.length];
        for (int i = 0; i < values.length; i++)
            array[i] = requireSBCount(values[i]);
        Arrays.sort(array); // ascending order: last one is the biggest
        return array[array.length - 1];
    }

    /**
     * Calculate number of bits required to represent the given value in
     * unsigned bit values.
     * 
     * @param value signed integer
     * @return number of bits required for SB[]
     */
    public static int requireUBCount(int value)
    {
        assert (value >= 0) : "requireUBCount called with negative number";
        return minBits(value, 0);
    }

    /**
     * Calculates the minimum number of bits necessary to represent the given
     * number. The number should be given in its unsigned form with the starting
     * bits equal to 1 if it is signed. Repeatedly compares number to another
     * unsigned int called x. x is initialized to 1. The value of x is shifted
     * left i times until x is greater than number. Now i is equal to the number
     * of bits the UNSIGNED value of number needs. The signed value will need
     * one more bit for the sign so i+1 is returned if the number is signed, and
     * i is returned if the number is unsigned.
     * 
     * @param number the number to compute the size of
     * @param bits 1 if number is signed, 0 if unsigned
     */
    private static int minBits(int number, int bits)
    {
        int val = 1;
        for (int x = 1; val <= number && !(bits > 32); x <<= 1)
        {
            val = val | x;
            bits++;
        }

        assert (bits <= 32) : ("minBits " + bits + " must not exceed 32");

        return bits;
    }

    private void writeLengthString(String name)
    {
        try
        {
            assert (tagBuffer.getBitPos() == 8);
            byte[] b = swf.getVersion() >= 6 ? name.getBytes("UTF8") : name.getBytes();

            // [paul] Flash Authoring and the player expect the String
            // to be null terminated.
            tagBuffer.writeUI8(b.length + 1);
            tagBuffer.write(b);
            tagBuffer.writeUI8(0);
        }
        catch (UnsupportedEncodingException e)
        {
            assert false;
        }
    }

    // Tag buffer
    protected IOutputBitStream tagBuffer;

    // SWF model
    private final ISWF swf;

    // This buffer contains the SWF data after FileLength field. 
    protected final IOutputBitStream outputBuffer;

    // True if the encoded SWF file is compressed.
    private final Header.Compression useCompression;

    // True if debugging of the SWF is enabled.
    private final boolean enableDebug;

    // True if telemetry features of the SWF are enabled.
    private final boolean enableTelemetry;

    // Current frame index. Updated in writeFrames().
    private int currentFrameIndex;

    // Prevent writing out the same tag twice.
    private Set<ITag> writtenTags;

    /**
     * Create a SWF writer.
     * 
     * @param swf the SWF model to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public SWFWriter(ISWF swf, Header.Compression useCompression)
    {
        this(swf, useCompression, false, false);
    }

    /**
     * Create a SWF writer.
     * 
     * @param swf the SWF model to be encoded
     * @param useCompression use ZLIB compression if true
     * @param enableDebug enable debugging of the SWF if true
     * @param enableTelemetry enable telemetry
     */
    public SWFWriter(ISWF swf, Header.Compression useCompression, boolean enableDebug, boolean enableTelemetry)
    {
        this.swf = swf;
        this.useCompression = useCompression;
        this.enableDebug = enableDebug;
        this.enableTelemetry = enableTelemetry;
        this.outputBuffer = new OutputBitStream(false);
        this.tagBuffer = new OutputBitStream(false);

        computeCharacterID();
    }

    /**
     * Compute the character ID for all the {@code ICharacterTag}s.
     */
    private void computeCharacterID()
    {
        int id = 1; // need to start at 1, as index 0 has special meaning
        for (int frameIndex = 0; frameIndex < swf.getFrameCount(); frameIndex++)
        {
            final SWFFrame frame = swf.getFrameAt(frameIndex);
            for (final ITag tag : frame)
            {
                if (tag instanceof CharacterTag)
                {
                    final CharacterTag characterTag = (CharacterTag)tag;
                    characterTag.setCharacterID(id);
                    id++;
                }
            }
        }
    }

    /**
     * Compute the tag length for the tag header, then write the header and the
     * buffered tag body to target output stream.
     * 
     * @param tag tag object
     * @param tagData serialized tag body
     * @param out target output stream
     */
    protected void finishTag(
            final ITag tag,
            final IOutputBitStream tagData,
            final IOutputBitStream out)
    {
        tagData.flush();
        final int tagLength = tagData.size();

        // write tag header
        if (tag instanceof IAlwaysLongTag || tagLength > SHORT_TAG_MAX_LENGTH)
        {
            // use long tag header
            out.writeUI16((tag.getTagType().getValue() << 6) | 0x3F);
            out.writeSI32(tagLength);
        }
        else
        {
            // use short tag header
            out.writeUI16((tag.getTagType().getValue() << 6) | tagLength);
        }
        out.write(tagData.getBytes(), 0, tagLength);
    }

    public void writeARGB(ARGB argb)
    {
        tagBuffer.writeUI8(argb.getAlpha());
        tagBuffer.writeUI8(argb.getRed());
        tagBuffer.writeUI8(argb.getGreen());
        tagBuffer.writeUI8(argb.getBlue());
    }

    protected void writeCompressibleHeader()
    {
        // Frame size
        final Rect rect = swf.getFrameSize();
        tagBuffer.reset();
        writeRect(rect);
        outputBuffer.write(tagBuffer.getBytes(), 0, tagBuffer.size());

        // Frame rate
        outputBuffer.writeFIXED8(swf.getFrameRate());

        // Frame count
        outputBuffer.writeUI16(swf.getFrameCount());
    }

    /**
     * @see SWFReader#readCurvedEdgeRecord
     */
    private void writeCurvedEdgeRecord(CurvedEdgeRecord shape)
    {
        tagBuffer.writeBit(true); // This is an edge. Always 1.
        tagBuffer.writeBit(false); // StraightFlag is always false.
        int numBits = shape.getNumBits();
        tagBuffer.writeUB(numBits, 4);
        tagBuffer.writeSB(shape.getControlDeltaX(), numBits + 2);
        tagBuffer.writeSB(shape.getControlDeltaY(), numBits + 2);
        tagBuffer.writeSB(shape.getAnchorDeltaX(), numBits + 2);
        tagBuffer.writeSB(shape.getAnchorDeltaY(), numBits + 2);
    }

    private void writeDefineBinaryData(DefineBinaryDataTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUI32(0); // Reserved, always zero.
        tagBuffer.write(tag.getData());
    }

    /**
     * This method treats the bytes after the color table as a binary blob so
     * both the lossless and lossless2 tags can be written using this method.
     * 
     * @param tag
     */
    private void writeDefineBitsLossless(DefineBitsLosslessTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUI8(tag.getBitmapFormat());
        tagBuffer.writeUI16(tag.getBitmapWidth());
        tagBuffer.writeUI16(tag.getBitmapHeight());
        if (DefineBitsLossless2Tag.BF_8BIT_COLORMAPPED_IMAGE == tag.getBitmapFormat())
        {
            tagBuffer.writeUI8(tag.getBitmapColorTableSize() - 1);
        }
        tagBuffer.write(tag.getZlibBitmapData());
    }

    /**
     * @see SWFReader#readDefineShape
     */
    private void writeDefineShape(DefineShapeTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        writeRect(tag.getShapeBounds());
        writeShapeWithStyle(tag.getShapes(), tag.getTagType());
    }

    /**
     * @see SWFReader#readDefineShape2
     */
    private void writeDefineShape2(DefineShape2Tag tag)
    {
        writeDefineShape(tag);
    }

    /**
     * @see SWFReader#readDefineShape3
     */
    private void writeDefineShape3(DefineShape3Tag tag)
    {
        writeDefineShape2(tag);
    }

    /**
     * @see SWFReader#readDefineShape4
     */
    private void writeDefineShape4(DefineShape4Tag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        writeRect(tag.getShapeBounds());
        writeRect(tag.getEdgeBounds());
        tagBuffer.byteAlign();
        tagBuffer.writeUB(0, 5); // Reserved. Must be 0.
        tagBuffer.writeBit(tag.isUsesFillWindingRule());
        tagBuffer.writeBit(tag.isUsesNonScalingStrokes());
        tagBuffer.writeBit(tag.isUsesScalingStrokes());
        tagBuffer.byteAlign();
        writeShapeWithStyle(tag.getShapes(), tag.getTagType());
    }

    /**
     * @see SWFReader#readDefineMorphShape2
     * @see SWFWriter#writeDefineMorphShape
     */
    private void writeDefineMorphShape2(DefineMorphShape2Tag tag)
    {
        writeDefineMorphShape(tag);
    }

    /**
     * @see SWFReader#readDefineMorphShape
     */
    private void writeDefineMorphShape(DefineMorphShapeTag tag)
    {
        // Write to another buffer to calculate offset to EndEdges field.
        final IOutputBitStream originalTagBuffer = tagBuffer;
        tagBuffer = new OutputBitStream();

        // fields before "offset"
        tagBuffer.writeUI16(tag.getCharacterID());
        writeRect(tag.getStartBounds());
        writeRect(tag.getEndBounds());
        if (TagType.DefineMorphShape2 == tag.getTagType())
        {
            final DefineMorphShape2Tag tag2 = (DefineMorphShape2Tag)tag;
            writeRect(tag2.getStartEdgeBounds());
            writeRect(tag2.getEndEdgeBounds());
            tagBuffer.writeUB(0, 6);
            tagBuffer.writeBit(tag2.isUsesNonScalingStrokes());
            tagBuffer.writeBit(tag2.isUsesScalingStrokes());
            tagBuffer.byteAlign();
        }
        final int sizeBeforeOffset = tagBuffer.size();

        // fields after "offset"

        Shape startEdges = tag.getStartEdges();
        writeShapeWithStyle((ShapeWithStyle)startEdges, tag.getTagType());

        final int sizeAfterOffsetToEnd = tagBuffer.size() - sizeBeforeOffset;

        // put together
        originalTagBuffer.write(tagBuffer.getBytes(), 0, sizeBeforeOffset);
        originalTagBuffer.writeUI32(sizeAfterOffsetToEnd);
        originalTagBuffer.write(tagBuffer.getBytes(), sizeBeforeOffset, sizeAfterOffsetToEnd);

        // recover current tag buffer
        tagBuffer = originalTagBuffer;

        writeShape(tag.getEndEdges(), tag.getTagType(), 0, 0);
    }

    /**
     * @see SWFReader#readShape
     */
    private void writeShape(Shape shape, TagType tagType, int fillStyleCount, int lineStyleCount)
    {
        CurrentStyles currentStyles = new CurrentStyles();
        currentStyles.numFillBits = requireUBCount(fillStyleCount);
        currentStyles.numLineBits = requireUBCount(lineStyleCount);
        writeShape(shape, tagType, currentStyles);
    }

    /**
     * @see SWFReader#readShape
     */
    private void writeShape(Shape shape, TagType tagType, CurrentStyles currentStyles)
    {
        tagBuffer.writeUB(currentStyles.numFillBits, 4);
        tagBuffer.writeUB(currentStyles.numLineBits, 4);
        for (final ShapeRecord shapeRecord : shape.getShapeRecords())
        {
            writeShapeRecord(shapeRecord, tagType, currentStyles);
        }

        writeShapeRecord(new EndShapeRecord(), tagType, currentStyles);
        tagBuffer.byteAlign();
    }

    /**
     * @see SWFReader#readMorphLineStyleArray
     */
    //    private void writeMorphLineStyleArray(MorphLineStyleArray lineStyles)
    //    {
    //        writeExtensibleCount(lineStyles.size());
    //        for (MorphLineStyle lineStyle : lineStyles)
    //        {
    //            writeMorphLineStyle(lineStyle);
    //        }
    //    }

    /**
     * @see SWFReader#readMorphLineStyle
     */
    private void writeMorphLineStyle(MorphLineStyle lineStyle)
    {
        tagBuffer.writeUI16(lineStyle.getStartWidth());
        tagBuffer.writeUI16(lineStyle.getEndWidth());
        writeRGBA(lineStyle.getStartColor());
        writeRGBA(lineStyle.getEndColor());
    }

    private void writeMorphLineStyle2(MorphLineStyle2 lineStyle, TagType tagType)
    {
        // widths
        tagBuffer.writeUI16(lineStyle.getStartWidth());
        tagBuffer.writeUI16(lineStyle.getEndWidth());

        // misc fields byte
        tagBuffer.writeUB(lineStyle.getStartCapStyle(), 2);
        tagBuffer.writeUB(lineStyle.getJoinStyle(), 2);
        tagBuffer.writeBit(lineStyle.isHasFillFlag());
        tagBuffer.writeBit(lineStyle.isNoHScaleFlag());
        tagBuffer.writeBit(lineStyle.isNoVScaleFlag());
        tagBuffer.writeBit(lineStyle.isPixelHintingFlag());

        // next mixed byte
        tagBuffer.writeUB(0, 5); // reserved
        tagBuffer.writeBit(lineStyle.isNoClose());
        tagBuffer.writeUB(lineStyle.getEndCapStyle(), 2);

        //
        if (lineStyle.getJoinStyle() == LineStyle2.JS_MITER_JOIN)
        {
            tagBuffer.writeUI16(lineStyle.getMiterLimitFactor());
        }

        if (!lineStyle.isHasFillFlag())
        {
            writeRGBA(lineStyle.getStartColor());
            writeRGBA(lineStyle.getEndColor());
        }
        else
        {
            writeMorphFillStyle(lineStyle.getFillType(), tagType);
        }
    }

    /**
     * @see SWFReader#readMorphFillStyle
     */
    private void writeMorphFillStyle(MorphFillStyle fillStyle, TagType tagType)
    {
        int fillStyleType = fillStyle.getFillStyleType();
        tagBuffer.writeUI8(fillStyleType);
        switch (fillStyle.getFillStyleType())
        {
            case FillStyle.SOLID_FILL:
                writeRGBA(fillStyle.getStartColor());
                writeRGBA(fillStyle.getEndColor());
                break;
            case FillStyle.LINEAR_GRADIENT_FILL:
            case FillStyle.RADIAL_GRADIENT_FILL:
            case FillStyle.FOCAL_RADIAL_GRADIENT_FILL:
                writeMatrix(fillStyle.getStartGradientMatrix());
                writeMatrix(fillStyle.getEndGradientMatrix());
                writeMorphGradient(fillStyle.getGradient());
                if (fillStyleType == FillStyle.FOCAL_RADIAL_GRADIENT_FILL &&
                        tagType.getValue() == TagType.DefineMorphShape2.getValue())
                {
                    tagBuffer.writeSI16(fillStyle.getRatio1());
                    tagBuffer.writeSI16(fillStyle.getRatio2());
                }
                break;
            case FillStyle.REPEATING_BITMAP_FILL:
            case FillStyle.CLIPPED_BITMAP_FILL:
            case FillStyle.NON_SMOOTHED_CLIPPED_BITMAP:
            case FillStyle.NON_SMOOTHED_REPEATING_BITMAP:
                tagBuffer.writeUI16(fillStyle.getBitmap().getCharacterID());
                writeMatrix(fillStyle.getStartBitmapMatrix());
                writeMatrix(fillStyle.getEndBitmapMatrix());
                break;
        }
    }

    /**
     * @see SWFReader#readMorphGradient
     */
    private void writeMorphGradient(MorphGradient gradient)
    {
        tagBuffer.writeUI8(gradient.size());
        for (MorphGradRecord morphGradRecord : gradient)
        {
            writeMorphGradRecord(morphGradRecord);
        }
    }

    /**
     * @see SWFReader#readMorphGradRecord
     */
    private void writeMorphGradRecord(MorphGradRecord morphGradRecord)
    {
        tagBuffer.writeUI8(morphGradRecord.getStartRatio());
        writeRGBA(morphGradRecord.getStartColor());
        tagBuffer.writeUI8(morphGradRecord.getEndRatio());
        writeRGBA(morphGradRecord.getEndColor());
    }

    /**
     * @see SWFReader#readExtensibleCount
     * @param count
     */
    private void writeExtensibleCount(int count)
    {
        if (count < 0xFF)
        {
            tagBuffer.writeUI8(count);
        }
        else
        {
            tagBuffer.writeUI8(0xFF);
            tagBuffer.writeUI16(count);
        }
    }

    public void writeDoABC(DoABCTag tag)
    {
        assert swf.getUseAS3() : "DoABC tag requires FileAttributes.Actionscript3=true.";
        tagBuffer.writeUI32(tag.getFlags());
        tagBuffer.writeString(tag.getName());
        tagBuffer.write(tag.getABCData());
    }

    private void writeEnableDebugger2(EnableDebugger2Tag tag)
    {
        tagBuffer.writeUI16(0); // reserved always zero
        tagBuffer.writeString(tag.getPassword());
    }

    private void writeEnableTelemetry(EnableTelemetryTag tag)
    {
        // Tag Code (Upper 10 bits = tag type, lower 16 bit = tag length)
        tagBuffer.writeUI16(0); // reserved always zero
        // PasswordHash: Optional SHA-256 hash of the UTF-8 representation of the password.
        // If not present telemetry clients can connect without using a password, if set they
        // have to authenticate.
        if(tag.getPassword() != null) {
            tagBuffer.writeString(tag.getPassword());
        }
    }

    private void writeEnd()
    {
        // End tag has no tag body.
    }

    private void writeEndShapeRecord(EndShapeRecord shape)
    {
        tagBuffer.writeBit(shape.getTypeFlag());
        tagBuffer.writeUB(0, 5); // EndOfShape always 0.
    }

    public void writeFileAttributes(FileAttributesTag tag)
    {
        tagBuffer.writeBit(false);
        tagBuffer.writeBit(tag.isUseDirectBlit());
        tagBuffer.writeBit(tag.isUseGPU());
        tagBuffer.writeBit(tag.isHasMetadata());
        tagBuffer.writeBit(tag.isAS3());
        tagBuffer.writeUB(RESERVED, 2);
        tagBuffer.writeBit(tag.isUseNetwork());
        tagBuffer.writeUB(RESERVED, 24);
        tagBuffer.byteAlign();
    }

    private void writeFillStyle(IFillStyle fillStyle, TagType tagType)
    {
        if (fillStyle instanceof FillStyle)
            writeFillStyle((FillStyle)fillStyle, tagType);
        else if (fillStyle instanceof MorphFillStyle)
            writeMorphFillStyle((MorphFillStyle)fillStyle, tagType);
        else
            assert false;
    }

    private void writeFillStyle(FillStyle fillStyle, TagType tagType)
    {
        assert fillStyle != null;

        final int fillStyleType = fillStyle.getFillStyleType();
        tagBuffer.writeUI8(fillStyleType);

        switch (fillStyleType)
        {
            case FillStyle.SOLID_FILL:
                switch (tagType)
                {
                    case DefineShape3:
                    case DefineShape4:
                        writeRGBA((RGBA)fillStyle.getColor());
                        break;
                    case DefineShape:
                    case DefineShape2:
                        writeRGB(fillStyle.getColor());
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid tag: " + tagType);
                }
                break;
            case FillStyle.LINEAR_GRADIENT_FILL:
            case FillStyle.RADIAL_GRADIENT_FILL:
                writeMatrix(fillStyle.getGradientMatrix());
                writeGradient(fillStyle.getGradient(), tagType);
                break;
            case FillStyle.FOCAL_RADIAL_GRADIENT_FILL:
                writeMatrix(fillStyle.getGradientMatrix());
                writeFocalGradient((FocalGradient)fillStyle.getGradient(), tagType);
                break;
            case FillStyle.REPEATING_BITMAP_FILL:
            case FillStyle.CLIPPED_BITMAP_FILL:
            case FillStyle.NON_SMOOTHED_REPEATING_BITMAP:
            case FillStyle.NON_SMOOTHED_CLIPPED_BITMAP:
                tagBuffer.writeUI16(
                        fillStyle.getBitmapCharacter().getCharacterID());
                writeMatrix(fillStyle.getBitmapMatrix());
                break;
            default:
                throw new IllegalArgumentException(
                        "Invalid FillStyleType: " + fillStyleType);
        }
    }

    private void writeFillStyles(FillStyleArray fillStyles, TagType tagType)
    {
        assert fillStyles != null;

        final int fillStyleCount = fillStyles.size();
        writeExtensibleCount(fillStyleCount);
        for (final IFillStyle fillStyle : fillStyles)
        {
            writeFillStyle(fillStyle, tagType);
        }
    }

    private void writeFocalGradient(FocalGradient gradient, TagType tagType)
    {
        assert TagType.DefineShape4 == tagType;
        writeGradient(gradient, tagType);
        tagBuffer.writeFIXED8(gradient.getFocalPoint());
    }

    private void writeFrames()
    {
        for (currentFrameIndex = 0; currentFrameIndex < swf.getFrameCount(); currentFrameIndex++)
        {
            final SWFFrame frame = swf.getFrameAt(currentFrameIndex);

            // If the SWF has a top level class name, the first frame must contain a SymbolClass tag.
            if (0 == currentFrameIndex && null != swf.getTopLevelClass())
            {
                SWFFrame.forceSymbolClassTag(frame);
            }

            for (final ITag tag : frame)
            {
                writeTag(tag);
            }
        }
    }

    private void writeGradient(Gradient gradient, TagType tagType)
    {
        assert gradient != null;
        assert gradient.getGradientRecords() != null;

        tagBuffer.writeUB(gradient.getSpreadMode(), 2);
        tagBuffer.writeUB(gradient.getInterpolationMode(), 2);
        tagBuffer.writeUB(gradient.getGradientRecords().size(), 4);
        tagBuffer.byteAlign();

        for (final GradRecord gradRecord : gradient.getGradientRecords())
        {
            writeGradRecord(gradRecord, tagType);
        }
    }

    private void writeGradRecord(GradRecord gradRecord, TagType tagType)
    {
        assert gradRecord != null;

        tagBuffer.writeUI8(gradRecord.getRatio());

        switch (tagType)
        {
            case DefineShape:
            case DefineShape2:
                writeRGB(gradRecord.getColor());
                break;
            case DefineShape3:
            case DefineShape4:
                writeRGBA((RGBA)gradRecord.getColor());
                break;
            default:
                throw new IllegalArgumentException("Invalid tag: " + tagType);
        }
    }

    private void writeLineStyle(LineStyle lineStyle, TagType tagType)
    {
        assert lineStyle != null;

        tagBuffer.writeUI16(lineStyle.getWidth());

        switch (tagType)
        {
            case DefineShape:
            case DefineShape2:
                writeRGB(lineStyle.getColor());
                break;
            case DefineShape3:
                writeRGBA((RGBA)lineStyle.getColor());
                break;
            default:
                throw new IllegalArgumentException("Invalid tag: " + tagType);
        }
    }

    private void writeLineStyle2(LineStyle2 lineStyle, TagType tagType)
    {
        assert lineStyle != null;

        tagBuffer.writeUI16(lineStyle.getWidth());
        tagBuffer.writeUB(lineStyle.getStartCapStyle(), 2);
        tagBuffer.writeUB(lineStyle.getJoinStyle(), 2);
        tagBuffer.writeBit(lineStyle.isHasFillFlag());
        tagBuffer.writeBit(lineStyle.isNoHScaleFlag());
        tagBuffer.writeBit(lineStyle.isNoVScaleFlag());
        tagBuffer.writeBit(lineStyle.isPixelHintingFlag());
        tagBuffer.writeUB(0, 5); // Reserved
        tagBuffer.writeBit(lineStyle.isNoClose());
        tagBuffer.writeUB(lineStyle.getEndCapStyle(), 2);
        tagBuffer.byteAlign();

        if (LineStyle2.JS_MITER_JOIN == lineStyle.getJoinStyle())
        {
            tagBuffer.writeFIXED8(lineStyle.getMiterLimitFactor());
        }

        if (lineStyle.isHasFillFlag())
        {
            writeFillStyle(lineStyle.getFillType(), tagType);
        }
        else
        {
            writeRGBA((RGBA)lineStyle.getColor());
        }

    }

    private void writeLineStyles(LineStyleArray lineStyles, TagType tagType)
    {
        assert lineStyles != null;
        final int lineStyleCount = lineStyles.size();
        writeExtensibleCount(lineStyleCount);
        for (final ILineStyle lineStyle : lineStyles)
        {
            switch (tagType)
            {
                case DefineShape:
                case DefineShape2:
                case DefineShape3:
                    writeLineStyle((LineStyle)lineStyle, tagType);
                    break;
                case DefineShape4:
                    writeLineStyle2((LineStyle2)lineStyle, tagType);
                    break;
                case DefineMorphShape2:
                    writeMorphLineStyle2((MorphLineStyle2)lineStyle, tagType);
                    break;
                case DefineMorphShape:
                    writeMorphLineStyle((MorphLineStyle)lineStyle);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid tag: " + tagType);
            }
        }
    }

    /**
     * @param matrix
     */
    private void writeMatrix(Matrix matrix)
    {
        // scale (optional)
        tagBuffer.writeBit(matrix.hasScale());
        if (matrix.hasScale())
        {
            final double scaleX = matrix.getScaleX();
            final double scaleY = matrix.getScaleY();
            final int nScaleBits = requireFBCount(maxNum(scaleX, scaleY, 0, 0));
            tagBuffer.writeUB(nScaleBits, 5);
            tagBuffer.writeFB(scaleX, nScaleBits);
            tagBuffer.writeFB(scaleY, nScaleBits);
        }

        // rotate-skew (optional)
        tagBuffer.writeBit(matrix.hasRotate());
        if (matrix.hasRotate())
        {
            final double rotateSkew0 = matrix.getRotateSkew0();
            final double rotateSkew1 = matrix.getRotateSkew1();
            final int nRotateBits = requireFBCount(maxNum(rotateSkew0, rotateSkew1, 0, 0));
            tagBuffer.writeUB(nRotateBits, 5);
            tagBuffer.writeFB(rotateSkew0, nRotateBits);
            tagBuffer.writeFB(rotateSkew1, nRotateBits);
        }

        // translate (always)
        final int translateX = matrix.getTranslateX();
        final int translateY = matrix.getTranslateY();
        final int nTranslateBits = requireSBCount(maxNum(translateX, translateY, 0, 0));
        tagBuffer.writeUB(nTranslateBits, 5);
        tagBuffer.writeSB(translateX, nTranslateBits);
        tagBuffer.writeSB(translateY, nTranslateBits);

        tagBuffer.byteAlign();
    }

    /* Tag Encoders */

    private void writeMetadata(MetadataTag tag)
    {
        tagBuffer.writeString(tag.getMetadata());
    }

    private void writeProductInfo(ProductInfoTag tag)
    {
        tagBuffer.writeUI32(tag.getProduct().getCode());
        tagBuffer.writeUI32(tag.getEdition().getCode());
        tagBuffer.writeUI8(tag.getMajorVersion());
        tagBuffer.writeUI8(tag.getMinorVersion());
        tagBuffer.writeSI64(tag.getBuild());
        tagBuffer.writeSI64(tag.getCompileDate());
    }

    public void writeRect(Rect rect)
    {
        int maxRectNum = maxNum(rect.xMin(), rect.xMax(), rect.yMin(), rect.yMax());
        final int nbits = requireSBCount(maxRectNum);
        tagBuffer.writeUB(nbits, 5);
        tagBuffer.writeSB(rect.xMin(), nbits);
        tagBuffer.writeSB(rect.xMax(), nbits);
        tagBuffer.writeSB(rect.yMin(), nbits);
        tagBuffer.writeSB(rect.yMax(), nbits);
        tagBuffer.byteAlign();
    }

    public void writeRGB(RGB rgb)
    {
        tagBuffer.writeUI8(rgb.getRed());
        tagBuffer.writeUI8(rgb.getGreen());
        tagBuffer.writeUI8(rgb.getBlue());
    }

    public void writeRGBA(RGBA rgba)
    {
        tagBuffer.writeUI8(rgba.getRed());
        tagBuffer.writeUI8(rgba.getGreen());
        tagBuffer.writeUI8(rgba.getBlue());
        tagBuffer.writeUI8(rgba.getAlpha());
    }

    private void writeScriptLimits(ScriptLimitsTag tag)
    {
        tagBuffer.writeUI16(tag.getMaxRecursionDepth());
        tagBuffer.writeUI16(tag.getScriptTimeoutSeconds());
    }

    private void writeSetBackgroundColor(SetBackgroundColorTag tag)
    {
        writeRGB(tag.getColor());
    }

    private void writeShapeRecord(
            final ShapeRecord shape,
            final TagType tagType,
            final CurrentStyles currentStyles)
    {
        switch (shape.getShapeRecordType())
        {
            case END_SHAPE:
                writeEndShapeRecord((EndShapeRecord)shape);
                break;
            case CURVED_EDGE:
                writeCurvedEdgeRecord((CurvedEdgeRecord)shape);
                break;
            case STRAIGHT_EDGE:
                writeStraightEdgeRecord((StraightEdgeRecord)shape);
                break;
            case STYLE_CHANGE:
                writeStyleChangeRecord(
                        (StyleChangeRecord)shape,
                        tagType,
                        currentStyles);
                break;
        }
    }

    private void writeShapeWithStyle(ShapeWithStyle shape, TagType tagType)
    {
        writeFillStyles(shape.getFillStyles(), tagType);
        writeLineStyles(shape.getLineStyles(), tagType);
        CurrentStyles currentStyles = new CurrentStyles();
        currentStyles.styles = new Styles(shape.getFillStyles(), shape.getLineStyles());
        currentStyles.numFillBits = requireUBCount(shape.getFillStyles().size());
        currentStyles.numLineBits = requireUBCount(shape.getLineStyles().size());
        writeShape(shape, tagType, currentStyles);
    }

    private void writeShowFrame()
    {
        // ShowFrame tag has no tag body.
    }

    /**
     * @see SWFReader#readStraightEdgeRecord
     */
    private void writeStraightEdgeRecord(StraightEdgeRecord shape)
    {
        tagBuffer.writeBit(true); // This is an edge. Always 1.
        tagBuffer.writeBit(true); // StraightFlag is always true.
        int numBits = shape.getNumBits();
        tagBuffer.writeUB(numBits, 4);
        switch (shape.getLineType())
        {
            case GENERAL:
                tagBuffer.writeBit(true);
                tagBuffer.writeSB(shape.getDeltaX(), numBits + 2);
                tagBuffer.writeSB(shape.getDeltaY(), numBits + 2);
                break;
            case VERTICAL:
                tagBuffer.writeBit(false);
                tagBuffer.writeBit(true);
                tagBuffer.writeSB(shape.getDeltaY(), numBits + 2);
                break;
            case HORIZONTAL:
                tagBuffer.writeBit(false);
                tagBuffer.writeBit(false);
                tagBuffer.writeSB(shape.getDeltaX(), numBits + 2);
                break;
        }
    }

    /**
     * @see SWFReader#readStyleChangeRecord
     */
    private void writeStyleChangeRecord(
            StyleChangeRecord shape,
            TagType tagType,
            CurrentStyles currentStyles)
    {
        tagBuffer.writeBit(shape.getTypeFlag());
        tagBuffer.writeBit(shape.isStateNewStyles());
        tagBuffer.writeBit(shape.isStateLineStyle());
        tagBuffer.writeBit(shape.isStateFillStyle1());
        tagBuffer.writeBit(shape.isStateFillStyle0());
        tagBuffer.writeBit(shape.isStateMoveTo());

        if (shape.isStateMoveTo())
        {
            final int moveBits = requireSBCount(maxNum(shape.getMoveDeltaX(), shape.getMoveDeltaY(), 0, 0));
            tagBuffer.writeUB(moveBits, 5);
            tagBuffer.writeSB(shape.getMoveDeltaX(), moveBits);
            tagBuffer.writeSB(shape.getMoveDeltaY(), moveBits);
        }

        // there shouldn't be any styles on a shape for fonts, as the
        // tag is a Shape, not ShapeWithStyle, but the fillStyle0 can be 1 because
        // of the following from the SWF spec:
        // "The first STYLECHANGERECORD of each SHAPE in the GlyphShapeTable does not use
        // the LineStyle and LineStyles fields. In addition, the first STYLECHANGERECORD of each
        // shape must have both fields StateFillStyle0 and FillStyle0 set to 1."
        boolean ignoreStyle = tagType == TagType.DefineFont ||
                              tagType == TagType.DefineFont2 ||
                              tagType == TagType.DefineFont3;

        if (shape.isStateFillStyle0())
        {
            int fs0;
            if (ignoreStyle)
                fs0 = 1;
            else
                fs0 = currentStyles.styles.getFillStyles().indexOf(shape.getFillstyle0()) + 1;

            tagBuffer.writeUB(fs0, currentStyles.numFillBits);
        }

        if (shape.isStateFillStyle1())
        {
            int fs1;
            if (ignoreStyle)
                fs1 = 1;
            else
                fs1 = currentStyles.styles.getFillStyles().indexOf(shape.getFillstyle1()) + 1;

            tagBuffer.writeUB(fs1, currentStyles.numFillBits);
        }

        if (shape.isStateLineStyle())
        {
            int ls;
            if (ignoreStyle)
                ls = 0;
            else
                ls = currentStyles.styles.getLineStyles().indexOf(shape.getLinestyle()) + 1;

            tagBuffer.writeUB(ls, currentStyles.numLineBits);
        }

        if (shape.isStateNewStyles())
        {
            tagBuffer.byteAlign();

            // encode
            writeFillStyles(shape.getStyles().getFillStyles(), tagType);
            writeLineStyles(shape.getStyles().getLineStyles(), tagType);
            final int nFillBits = shape.getNumFillBits();
            final int nLineBits = shape.getNumLineBits();
            tagBuffer.writeUB(nFillBits, 4);
            tagBuffer.writeUB(nLineBits, 4);

            // update context
            currentStyles.styles = shape.getStyles();
            currentStyles.numFillBits = nFillBits;
            currentStyles.numLineBits = nLineBits;
        }
    }

    /**
     * @see SWFReader#readSymbolClass
     */
    private void writeSymbolClass(SymbolClassTag tag)
    {
        final boolean writeRootClass = currentFrameIndex == 0 && swf.getTopLevelClass() != null;

        // number of symbols
        final int count = writeRootClass ? tag.size() + 1 : tag.size();
        tagBuffer.writeUI16(count);

        // export symbols
        for (String symbolName : tag.getSymbolNames())
        {
            final ICharacterTag characterTag = tag.getSymbol(symbolName);
            tagBuffer.writeUI16(characterTag.getCharacterID());
            tagBuffer.writeString(symbolName);
        }

        // root class name
        if (writeRootClass)
        {
            tagBuffer.writeUI16(0);
            tagBuffer.writeString(swf.getTopLevelClass());
        }

    }

    /**
     * This is the entry-point for encoding a SWF tag. In order to calculate the
     * size of a tag, each tag data is buffered on a OutputBitStream object.
     * This method initialize the buffer, select encoding method according to
     * the tag type, encode the tag body and then write the tag header and tag
     * body onto the target output stream.
     * 
     * @param tag tag to encode
     */

    private void writeTag(ITag tag)
    {
        if (!writtenTags.contains(tag))
        {
            tagBuffer.reset();
            writeTag(tag, tagBuffer, outputBuffer);

            writtenTags.add(tag);
        }
    }

    /**
     * Encode {@code tag}'s body onto buffer {@code tagData}. Then compute the
     * tag header and length. Finally, write the tag header and tag body to
     * {@code out}.
     * <p>
     * This method assumes that {@code tagData} is a clean, initialized
     * {@code IOutputBitStream} object.
     * 
     * @param tag tag object
     * @param tagData tag buffer
     * @param out target output
     */
    private void writeTag(ITag tag, IOutputBitStream tagData, IOutputBitStream out)
    {
        assert tag != null;
        assert tagData != null;
        assert out != null;
        assert tagData != out;

        if (tag == SWFReader.INVALID_TAG)
            return;

        // redirect tag buffer to "tagData"
        IOutputBitStream swfTagBuffer = null;
        if (tagData != this.tagBuffer)
        {
            swfTagBuffer = this.tagBuffer;
            this.tagBuffer = tagData;
        }

        boolean skipRawTag = false;
        Collection<ITag> extraTags = new LinkedList<ITag>();
        if (tag instanceof RawTag)
        {
            skipRawTag = writeRawTag((RawTag)tag);
        }
        else
        {
            switch (tag.getTagType())
            {
                case DoABC:
                    writeDoABC((DoABCTag)tag);
                    break;
                case FileAttributes:
                    writeFileAttributes((FileAttributesTag) tag);
                    break;
                case SymbolClass:
                    writeSymbolClass((SymbolClassTag) tag);
                    break;
                case ShowFrame:
                    writeShowFrame();
                    break;
                case SetBackgroundColor:
                    writeSetBackgroundColor((SetBackgroundColorTag) tag);
                    break;
                case EnableDebugger2:
                    writeEnableDebugger2((EnableDebugger2Tag) tag);
                    break;
                case EnableTelemetry:
                    writeEnableTelemetry((EnableTelemetryTag) tag);
                    break;
                case ScriptLimits:
                    writeScriptLimits((ScriptLimitsTag)tag);
                    break;
                case ProductInfo:
                    writeProductInfo((ProductInfoTag)tag);
                    break;
                case Metadata:
                    writeMetadata((MetadataTag)tag);
                    break;
                case DefineBits:
                    writeDefineBits((DefineBitsTag)tag);
                    break;
                case DefineBitsJPEG2:
                    writeDefineBitsJPEG2((DefineBitsJPEG2Tag)tag);
                    break;
                case DefineBitsJPEG3:
                    writeDefineBitsJPEG3((DefineBitsJPEG3Tag)tag);
                    break;
                case DefineBitsLossless:
                case DefineBitsLossless2:
                    writeDefineBitsLossless((DefineBitsLosslessTag)tag);
                    break;
                case DefineBinaryData:
                    writeDefineBinaryData((DefineBinaryDataTag)tag);
                    break;
                case DefineShape:
                    writeDefineShape((DefineShapeTag)tag);
                    break;
                case DefineShape2:
                    writeDefineShape2((DefineShape2Tag)tag);
                    break;
                case DefineShape3:
                    writeDefineShape3((DefineShape3Tag)tag);
                    break;
                case DefineShape4:
                    writeDefineShape4((DefineShape4Tag)tag);
                    break;
                case DefineSprite:
                    writeDefineSprite((DefineSpriteTag)tag);
                    break;
                case ExportAssets:
                    writeExportAssets((ExportAssetsTag)tag);
                    break;
                case DefineScalingGrid:
                    writeDefineScalingGrid((DefineScalingGridTag)tag);
                    break;
                case DefineFont:
                    writeDefineFont((DefineFontTag)tag, extraTags);
                    break;
                case DefineFont2:
                    writeDefineFont2((DefineFont2Tag)tag, extraTags);
                    break;
                case DefineFont3:
                    writeDefineFont3((DefineFont3Tag)tag, extraTags);
                    break;
                case DefineFont4:
                    writeDefineFont4((DefineFont4Tag)tag, extraTags);
                    break;
                case DefineFontInfo:
                    writeDefineFontInfo((IFontInfo)tag);
                    break;
                case DefineFontInfo2:
                    writeDefineFontInfo2((DefineFontInfo2Tag)tag);
                    break;
                case DefineFontAlignZones:
                    writeDefineFontAlignZones((DefineFontAlignZonesTag)tag);
                    break;
                case DefineFontName:
                    writeDefineFontName((DefineFontNameTag)tag);
                    break;
                case DefineText:
                    writeDefineText((DefineTextTag)tag, extraTags);
                    break;
                case DefineText2:
                    writeDefineText2((DefineText2Tag)tag, extraTags);
                    break;
                case DefineEditText:
                    writeDefineEditText((DefineEditTextTag)tag, extraTags);
                    break;
                case DefineSound:
                    writeDefineSound((DefineSoundTag)tag);
                    break;
                case DefineVideoStream:
                    writeDefineVideoStream((DefineVideoStreamTag)tag);
                    break;
                case VideoFrame:
                    writeVideoFrame((VideoFrameTag)tag);
                    break;
                case StartSound:
                    writeStartSound((StartSoundTag)tag);
                    break;
                case StartSound2:
                    writeStartSound2((StartSound2Tag)tag);
                    break;
                case SoundStreamHead:
                    writeSoundStreamHead((SoundStreamHeadTag)tag);
                    break;
                case SoundStreamHead2:
                    writeSoundStreamHead((SoundStreamHead2Tag)tag);
                    break;
                case SoundStreamBlock:
                    writeSoundStreamBlock((SoundStreamBlockTag)tag);
                    break;
                case DefineButton:
                    writeDefineButton((DefineButtonTag)tag);
                    break;
                case DefineButton2:
                    writeDefineButton2((DefineButton2Tag)tag);
                    break;
                case DefineButtonSound:
                    writeDefineButtonSound((DefineButtonSoundTag)tag);
                    break;
                case CSMTextSettings:
                    writeCSMTextSettings((CSMTextSettingsTag)tag);
                    break;
                case End:
                    writeEnd();
                    break;
                case JPEGTables:
                    writeJPEGTables(((JPEGTablesTag)tag));
                    break;
                case DefineMorphShape:
                    writeDefineMorphShape((DefineMorphShapeTag)tag);
                    break;
                case DefineMorphShape2:
                    writeDefineMorphShape2((DefineMorphShape2Tag)tag);
                    break;
                case PlaceObject:
                    writePlaceObject((PlaceObjectTag)tag);
                    break;
                case PlaceObject2:
                    writePlaceObject2((PlaceObject2Tag)tag);
                    break;
                case PlaceObject3:
                    writePlaceObject3((PlaceObject3Tag)tag);
                    break;
                case RemoveObject:
                    writeRemoveObject((RemoveObjectTag)tag);
                    break;
                case RemoveObject2:
                    writeRemoveObject2((RemoveObject2Tag)tag);
                    break;
                case SetTabIndex:
                    writeSetTabIndex((SetTabIndexTag)tag);
                    break;
                case FrameLabel:
                    writeFrameLabel((FrameLabelTag)tag);
                    break;
                default:
                    throw new RuntimeException("Tag not supported: " + tag);
            }
        }

        // reset tag buffer
        if (swfTagBuffer != null)
        {
            this.tagBuffer = swfTagBuffer;
        }

        if (!skipRawTag)
            finishTag(tag, tagData, out);

        for (ITag extraTag : extraTags)
            writeTag(extraTag);
    }

    @SuppressWarnings("incomplete-switch")
	private boolean writeRawTag(RawTag tag)
    {
        boolean skipTag = false;
        // if writing out an AS3 swf, there are a number of
        // tags which need to be ignored as they're not valid
        // in AS3.  These can get in when embedding tags from an
        // old SWF into a AS3 type SWF.
        if (swf.getUseAS3())
        {
            switch (tag.getTagType())
            {
                case DoAction:
                case DoInitAction:
                    skipTag = true;
                    break;
            }
        }

        if (!skipTag)
        {
            tagBuffer.write(tag.getTagBody());
        }

        return skipTag;
    }

    private void writeSetTabIndex(SetTabIndexTag tag)
    {
        tagBuffer.writeUI16(tag.getDepth());
        tagBuffer.writeUI16(tag.getTabIndex());
    }

    private void writeRemoveObject2(RemoveObject2Tag tag)
    {
        tagBuffer.writeUI16(tag.getDepth());
    }

    private void writeRemoveObject(RemoveObjectTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacter().getCharacterID());
        tagBuffer.writeUI16(tag.getDepth());
    }

    private void writePlaceObject(PlaceObjectTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacter().getCharacterID());
        tagBuffer.writeUI16(tag.getDepth());
        writeMatrix(tag.getMatrix());
        final CXForm colorTransform = tag.getColorTransform();
        if (colorTransform != null)
            writeColorTransform(colorTransform);
    }

    private void writeColorTransform(CXForm cx)
    {
        final int nbits = requireSBCount(
                cx.getRedMultTerm(),
                cx.getGreenMultTerm(),
                cx.getBlueMultTerm(),
                cx.getRedAddTerm(),
                cx.getGreenAddTerm(),
                cx.getBlueAddTerm());

        tagBuffer.writeBit(cx.hasAdd());
        tagBuffer.writeBit(cx.hasMult());
        tagBuffer.writeUB(nbits, 4);

        if (cx.hasMult())
        {
            tagBuffer.writeSB(cx.getRedMultTerm(), nbits);
            tagBuffer.writeSB(cx.getGreenMultTerm(), nbits);
            tagBuffer.writeSB(cx.getBlueMultTerm(), nbits);
        }

        if (cx.hasAdd())
        {
            tagBuffer.writeSB(cx.getRedAddTerm(), nbits);
            tagBuffer.writeSB(cx.getGreenAddTerm(), nbits);
            tagBuffer.writeSB(cx.getBlueAddTerm(), nbits);
        }
        tagBuffer.byteAlign();
    }

    private void writePlaceObject2(PlaceObject2Tag tag)
    {
        tagBuffer.writeBit(tag.isHasClipActions());
        tagBuffer.writeBit(tag.isHasClipDepth());
        tagBuffer.writeBit(tag.isHasName());
        tagBuffer.writeBit(tag.isHasRatio());
        tagBuffer.writeBit(tag.isHasColorTransform());
        tagBuffer.writeBit(tag.isHasMatrix());
        tagBuffer.writeBit(tag.isHasCharacter());
        tagBuffer.writeBit(tag.isMove());

        tagBuffer.writeUI16(tag.getDepth());

        if (tag.isHasCharacter())
            tagBuffer.writeUI16(tag.getCharacter().getCharacterID());
        if (tag.isHasMatrix())
            writeMatrix(tag.getMatrix());
        if (tag.isHasColorTransform())
            writeColorTransformWithAlpha(tag.getColorTransform());
        if (tag.isHasRatio())
            tagBuffer.writeUI16(tag.getRatio());
        if (tag.isHasName())
            tagBuffer.writeString(tag.getName());
        if (tag.isHasClipDepth())
            tagBuffer.writeUI16(tag.getClipDepth());
        if (tag.isHasClipActions())
            tagBuffer.write(tag.getClipActions().data);
    }

    private void writePlaceObject3(PlaceObject3Tag tag)
    {
        tagBuffer.writeBit(tag.isHasClipActions());
        tagBuffer.writeBit(tag.isHasClipDepth());
        tagBuffer.writeBit(tag.isHasName());
        tagBuffer.writeBit(tag.isHasRatio());
        tagBuffer.writeBit(tag.isHasColorTransform());
        tagBuffer.writeBit(tag.isHasMatrix());
        tagBuffer.writeBit(tag.isHasCharacter());
        tagBuffer.writeBit(tag.isMove());

        tagBuffer.writeUB(0, 3); // reserved
        tagBuffer.writeBit(tag.isHasImage());
        tagBuffer.writeBit(tag.isHasClassName());
        tagBuffer.writeBit(tag.isHasCacheAsBitmap());
        tagBuffer.writeBit(tag.isHasBlendMode());
        tagBuffer.writeBit(tag.isHasFilterList());

        tagBuffer.writeUI16(tag.getDepth());

        if (tag.isHasClassName())
            tagBuffer.writeString(tag.getClassName());
        if (tag.isHasCharacter())
            tagBuffer.writeUI16(tag.getCharacter().getCharacterID());
        if (tag.isHasMatrix())
            writeMatrix(tag.getMatrix());
        if (tag.isHasColorTransform())
            writeColorTransformWithAlpha(tag.getColorTransform());
        if (tag.isHasRatio())
            tagBuffer.writeUI16(tag.getRatio());
        if (tag.isHasName())
            tagBuffer.writeString(tag.getName());
        if (tag.isHasClipDepth())
            tagBuffer.writeUI16(tag.getClipDepth());
        if (tag.isHasFilterList())
        {
            tagBuffer.writeUI8(tag.getSurfaceFilterList().length);
            for (final Filter filter : tag.getSurfaceFilterList())
                writeFilter(filter);
        }
        if (tag.isHasBlendMode())
            tagBuffer.writeUI8(tag.getBlendMode());

        if (tag.isHasCacheAsBitmap())
            tagBuffer.writeUI8(tag.getBitmapCache());

        if (tag.isHasClipActions())
            tagBuffer.write(tag.getClipActions().data);
    }

    private void writeVideoFrame(VideoFrameTag tag)
    {
        tagBuffer.writeUI16(tag.getStreamTag().getCharacterID());
        tagBuffer.writeUI16(tag.getFrameNum());
        tagBuffer.write(tag.getVideoData());
    }

    private void writeDefineVideoStream(DefineVideoStreamTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUI16(tag.getNumFrames());
        tagBuffer.writeUI16(tag.getWidth());
        tagBuffer.writeUI16(tag.getHeight());
        tagBuffer.writeUB(0, 4); // reserved
        tagBuffer.writeUB(tag.getDeblocking(), 3);
        tagBuffer.writeBit(tag.isSmoothing());
        tagBuffer.writeUI8(tag.getCodecID());
    }

    private void writeDefineButtonSound(DefineButtonSoundTag tag)
    {
        tagBuffer.writeUI16(tag.getButtonTag().getCharacterID());
        for (int i = 0; i < DefineButtonSoundTag.TOTAL_SOUND_STYLE; i++)
        {
            if (tag.getSoundChar()[i] == null)
            {
                // write out a zero sound id if there is no sound info.
                tagBuffer.writeUI16(0);
                continue;
            }

            assert tag.getSoundChar()[i].getTagType() == TagType.DefineSound;

            tagBuffer.writeUI16(tag.getSoundChar()[i].getCharacterID());
            writeSoundInfo(tag.getSoundInfo()[i]);
        }
    }

    private void writeDefineButton2(DefineButton2Tag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUB(0, 7); // reserved
        tagBuffer.writeBit(tag.isTrackAsMenu());
        tagBuffer.writeUI16(tag.getActionOffset()); // TODO: need calculation
        for (final ButtonRecord r : tag.getCharacters())
            writeButtonRecord(r, tag.getTagType());
        tagBuffer.writeUI8(0); // end of character tag
        tagBuffer.write(tag.getActions());
    }

    private void writeDefineButton(DefineButtonTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        for (final ButtonRecord record : tag.getCharacters())
        {
            writeButtonRecord(record, tag.getTagType());
        }
        tagBuffer.writeUI8(0); // end of characters
        tagBuffer.write(tag.getActions());
        tagBuffer.writeUI8(0); // end of actions
    }

    private void writeButtonRecord(ButtonRecord record, TagType tagType)
    {
        tagBuffer.writeUB(0, 2); // reserved
        tagBuffer.writeBit(record.isHasBlendMode());
        tagBuffer.writeBit(record.isHasFilterList());
        tagBuffer.writeBit(record.isStateHitTest());
        tagBuffer.writeBit(record.isStateDown());
        tagBuffer.writeBit(record.isStateOver());
        tagBuffer.writeBit(record.isStateUp());
        tagBuffer.writeUI16(record.getCharacterID());
        tagBuffer.writeUI16(record.getPlaceDepth());
        writeMatrix(record.getPlaceMatrix());
        if (tagType == TagType.DefineButton2)
        {
            writeColorTransformWithAlpha(record.getColorTransform());

            if (record.isHasFilterList())
            {
                tagBuffer.writeUI8(record.getFilterList().length);
                for (final Filter filter : record.getFilterList())
                    writeFilter(filter);
            }

            if (record.isHasBlendMode())
                tagBuffer.writeUI8(record.getBlendMode());
        }
    }

    private void writeFilter(Filter filter)
    {
        tagBuffer.writeUI8(filter.getFilterID());
        switch (filter.getFilterID())
        {
            case Filter.DROP_SHADOW:
                writeDropShadowFilter(filter.getDropShadowFilter());
                break;
            case Filter.BLUR:
                writeBlurFilter(filter.getBlurFilter());
                break;
            case Filter.GLOW:
                writeGlowFilter(filter.getGlowFilter());
                break;
            case Filter.BEVEL:
                writeBevelFilter(filter.getBevelFilter());
                break;
            case Filter.GRADIENT_GLOW:
                writeGradientGlowFilter(filter.getGradientGlowFilter());
                break;
            case Filter.CONVOLUTION:
                writeConvolutionFilter(filter.getConvolutionFilter());
                break;
            case Filter.COLOR_MATRIX:
                writeColorMatrixFilter(filter.getColorMatrixFilter());
                break;
            case Filter.GRADIENT_BEVEL:
                writeGradientBevelFilter(filter.getGradientBevelFilter());
                break;
        }
    }

    private void writeGradientBevelFilter(GradientBevelFilter filter)
    {
        assert filter.getNumColors() == filter.getGradientColors().length;
        assert filter.getNumColors() == filter.getGradientRatio().length;

        tagBuffer.writeUI8(filter.getNumColors());
        for (RGBA color : filter.getGradientColors())
            writeRGBA(color);
        for (int ratio : filter.getGradientRatio())
            tagBuffer.writeUI8(ratio);

        tagBuffer.writeFIXED(filter.getBlurX());
        tagBuffer.writeFIXED(filter.getBlurY());
        tagBuffer.writeFIXED(filter.getAngle());
        tagBuffer.writeFIXED(filter.getDistance());
        tagBuffer.writeFIXED8(filter.getStrength());
        tagBuffer.writeBit(filter.isInnerShadow());
        tagBuffer.writeBit(filter.isKnockout());
        tagBuffer.writeBit(filter.isCompositeSource());
        tagBuffer.writeBit(filter.isOnTop());
        tagBuffer.writeUB(filter.getPasses(), 4);
    }

    private void writeGradientGlowFilter(GradientGlowFilter filter)
    {
        assert filter.getNumColors() == filter.getGradientColors().length;
        assert filter.getNumColors() == filter.getGradientRatio().length;

        tagBuffer.writeUI8(filter.getNumColors());
        for (RGBA color : filter.getGradientColors())
            writeRGBA(color);
        for (int ratio : filter.getGradientRatio())
            tagBuffer.writeUI8(ratio);

        tagBuffer.writeFIXED(filter.getBlurX());
        tagBuffer.writeFIXED(filter.getBlurY());
        tagBuffer.writeFIXED(filter.getAngle());
        tagBuffer.writeFIXED(filter.getDistance());
        tagBuffer.writeFIXED8(filter.getStrength());
        tagBuffer.writeBit(filter.isInnerGlow());
        tagBuffer.writeBit(filter.isKnockout());
        tagBuffer.writeBit(filter.isCompositeSource());
        tagBuffer.writeBit(filter.isOnTop());
        tagBuffer.writeUB(filter.getPasses(), 4);
    }

    private void writeBevelFilter(BevelFilter filter)
    {
        //Note: The SWF File Format Specifications Version 10 switches these two colors (it writes ShadowColor before HighlightColor).
        //A bug has been logged in JIRA against the specs for this issue
        writeRGBA(filter.getHighlightColor());
        writeRGBA(filter.getShadowColor());
        tagBuffer.writeFIXED(filter.getBlurX());
        tagBuffer.writeFIXED(filter.getBlurY());
        tagBuffer.writeFIXED(filter.getAngle());
        tagBuffer.writeFIXED(filter.getDistance());
        tagBuffer.writeFIXED8(filter.getStrength());
        tagBuffer.writeBit(filter.isInnerShadow());
        tagBuffer.writeBit(filter.isKnockout());
        tagBuffer.writeBit(filter.isCompositeSource());
        tagBuffer.writeBit(filter.isOnTop());
        tagBuffer.writeUB(filter.getPasses(), 4);
    }

    private void writeGlowFilter(GlowFilter filter)
    {
        writeRGBA(filter.getGlowColor());
        tagBuffer.writeFIXED(filter.getBlurX());
        tagBuffer.writeFIXED(filter.getBlurY());
        tagBuffer.writeFIXED8(filter.getStrength());
        tagBuffer.writeBit(filter.isInnerGlow());
        tagBuffer.writeBit(filter.isKnockout());
        tagBuffer.writeBit(filter.isCompositeSource());
        tagBuffer.writeUB(filter.getPasses(), 5);
    }

    private void writeDropShadowFilter(DropShadowFilter filter)
    {
        writeRGBA(filter.getDropShadowColor());
        tagBuffer.writeFIXED(filter.getBlurX());
        tagBuffer.writeFIXED(filter.getBlurY());
        tagBuffer.writeFIXED(filter.getAngle());
        tagBuffer.writeFIXED(filter.getDistance());
        tagBuffer.writeFIXED8(filter.getStrength());
        tagBuffer.writeBit(filter.isInnerShadow());
        tagBuffer.writeBit(filter.isKnockout());
        tagBuffer.writeBit(filter.isCompositeSource());
        tagBuffer.writeUB(filter.getPasses(), 5);
    }

    private void writeBlurFilter(BlurFilter filter)
    {
        tagBuffer.writeFIXED(filter.getBlurX());
        tagBuffer.writeFIXED(filter.getBlurY());
        tagBuffer.writeUB(filter.getPasses(), 5);
        tagBuffer.writeUB(0, 3); // reserved
    }

    private void writeConvolutionFilter(ConvolutionFilter filter)
    {
        assert filter.getMatrixX() * filter.getMatrixY() == filter.getMatrix().length;

        tagBuffer.writeUI8(filter.getMatrixX());
        tagBuffer.writeUI8(filter.getMatrixY());
        tagBuffer.writeFLOAT(filter.getDivisor());
        tagBuffer.writeFLOAT(filter.getBias());
        for (final float f : filter.getMatrix())
            tagBuffer.writeFLOAT(f);
        writeRGBA(filter.getDefaultColor());
        tagBuffer.writeUB(0, 6); // reserved
        tagBuffer.writeBit(filter.isClamp());
        tagBuffer.writeBit(filter.isPreserveAlpha());
        tagBuffer.byteAlign();
    }

    private void writeColorMatrixFilter(float[] filter)
    {
        assert filter.length == 20;
        for (float f : filter)
            tagBuffer.writeFLOAT(f);
    }

    private void writeColorTransformWithAlpha(CXFormWithAlpha cx)
    {
        final int nbits = requireSBCount(
                cx.getRedMultTerm(),
                cx.getGreenMultTerm(),
                cx.getBlueMultTerm(),
                cx.getAlphaMultTerm(),
                cx.getRedAddTerm(),
                cx.getGreenAddTerm(),
                cx.getBlueAddTerm(),
                cx.getAlphaAddTerm());

        tagBuffer.writeBit(cx.hasAdd());
        tagBuffer.writeBit(cx.hasMult());
        tagBuffer.writeUB(nbits, 4);

        if (cx.hasMult())
        {
            tagBuffer.writeSB(cx.getRedMultTerm(), nbits);
            tagBuffer.writeSB(cx.getGreenMultTerm(), nbits);
            tagBuffer.writeSB(cx.getBlueMultTerm(), nbits);
            tagBuffer.writeSB(cx.getAlphaMultTerm(), nbits);
        }

        if (cx.hasAdd())
        {
            tagBuffer.writeSB(cx.getRedAddTerm(), nbits);
            tagBuffer.writeSB(cx.getGreenAddTerm(), nbits);
            tagBuffer.writeSB(cx.getBlueAddTerm(), nbits);
            tagBuffer.writeSB(cx.getAlphaAddTerm(), nbits);
        }
        tagBuffer.byteAlign();
    }

    private void writeSoundStreamBlock(SoundStreamBlockTag tag)
    {
        tagBuffer.write(tag.getStreamSoundData());
    }

    private void writeSoundStreamHead(SoundStreamHeadTag tag)
    {
        tagBuffer.byteAlign();
        tagBuffer.writeUB(0, 4); // reserved
        tagBuffer.writeUB(tag.getPlaybackSoundRate(), 2);
        tagBuffer.writeUB(tag.getPlaybackSoundSize(), 1);
        tagBuffer.writeUB(tag.getPlaybackSoundType(), 1);
        tagBuffer.writeUB(tag.getStreamSoundCompression(), 4);
        tagBuffer.writeUB(tag.getStreamSoundRate(), 2);
        tagBuffer.writeUB(tag.getStreamSoundSize(), 1);
        tagBuffer.writeUB(tag.getStreamSoundType(), 1);
        tagBuffer.writeUI16(tag.getStreamSoundSampleCount());
        if (tag.getStreamSoundCompression() == SoundStreamHeadTag.SSC_MP3)
            tagBuffer.writeSI16(tag.getLatencySeek());

    }

    private void writeStartSound2(StartSound2Tag tag)
    {
        tagBuffer.writeString(tag.getSoundClassName());
        writeSoundInfo(tag.getSoundInfo());
    }

    private void writeStartSound(StartSoundTag tag)
    {
        tagBuffer.writeUI16(tag.getSoundTag().getCharacterID());
        writeSoundInfo(tag.getSoundInfo());
    }

    private void writeSoundInfo(SoundInfo soundInfo)
    {
        tagBuffer.byteAlign();
        tagBuffer.writeUB(0, 2); // reserved
        tagBuffer.writeBit(soundInfo.isSyncStop());
        tagBuffer.writeBit(soundInfo.isSyncNoMultiple());
        tagBuffer.writeBit(soundInfo.isHasEnvelope());
        tagBuffer.writeBit(soundInfo.isHasLoops());
        tagBuffer.writeBit(soundInfo.isHasOutPoint());
        tagBuffer.writeBit(soundInfo.isHasInPoint());
        if (soundInfo.isHasInPoint())
            tagBuffer.writeUI32(soundInfo.getInPoint());
        if (soundInfo.isHasOutPoint())
            tagBuffer.writeUI32(soundInfo.getOutPoint());
        if (soundInfo.isHasLoops())
            tagBuffer.writeUI16(soundInfo.getLoopCount());
        if (soundInfo.isHasEnvelope())
        {
            tagBuffer.writeUI8(soundInfo.getEnvPoints());
            for (final SoundEnvelope env : soundInfo.getEnvelopeRecords())
            {
                tagBuffer.writeUI32(env.getPos44());
                tagBuffer.writeUI16(env.getLeftLevel());
                tagBuffer.writeUI16(env.getRightLevel());
            }
        }
    }

    private void writeDefineSound(DefineSoundTag tag)
    {
        tagBuffer.byteAlign();
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUB(tag.getSoundFormat(), 4);
        tagBuffer.writeUB(tag.getSoundRate(), 2);
        tagBuffer.writeUB(tag.getSoundSize(), 1);
        tagBuffer.writeUB(tag.getSoundType(), 1);
        tagBuffer.writeUI32(tag.getSoundSampleCount());
        tagBuffer.write(tag.getSoundData());
    }

    private void writeDefineFont4(DefineFont4Tag tag, Collection<ITag> extraTags)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUB(0, 5); // reserved
        tagBuffer.writeBit(tag.isFontFlagsHasFontData());
        tagBuffer.writeBit(tag.isFontFlagsItalic());
        tagBuffer.writeBit(tag.isFontFlagsBold());
        // 8 bits - no need to align

        tagBuffer.writeString(tag.getFontName());
        tagBuffer.write(tag.getFontData());

        DefineFontNameTag license = tag.getLicense();
        if (license != null)
            extraTags.add(license);
    }

    private void writeCSMTextSettings(CSMTextSettingsTag tag)
    {
        tagBuffer.writeUI16(tag.getTextTag().getCharacterID());
        tagBuffer.writeUB(tag.getUseFlashType(), 2);
        tagBuffer.writeUB(tag.getGridFit(), 3);
        tagBuffer.writeUB(0, 3);
        // 8 bits - no need to align

        tagBuffer.writeFLOAT(tag.getThickness());
        tagBuffer.writeFLOAT(tag.getSharpness());
        tagBuffer.writeUI8(0); // reserved
    }

    private void writeDefineEditText(DefineEditTextTag tag, Collection<ITag> extraTags)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        writeRect(tag.getBounds());

        tagBuffer.writeBit(tag.isHasText());
        tagBuffer.writeBit(tag.isWordWrap());
        tagBuffer.writeBit(tag.isMultiline());
        tagBuffer.writeBit(tag.isPassword());
        tagBuffer.writeBit(tag.isReadOnly());
        tagBuffer.writeBit(tag.isHasTextColor());
        tagBuffer.writeBit(tag.isHasMaxLength());
        tagBuffer.writeBit(tag.isHasFont());
        tagBuffer.writeBit(tag.isHasFontClass());
        tagBuffer.writeBit(tag.isAutoSize());
        tagBuffer.writeBit(tag.isHasLayout());
        tagBuffer.writeBit(tag.isNoSelect());
        tagBuffer.writeBit(tag.isBorder());
        tagBuffer.writeBit(tag.isWasStatic());
        tagBuffer.writeBit(tag.isHtml());
        tagBuffer.writeBit(tag.isUseOutlines());

        // Both HasFont and HasFontClass requires a Height field.
        if (tag.isHasFont())
        {
            tagBuffer.writeUI16(tag.getFontTag().getCharacterID());
            tagBuffer.writeUI16(tag.getFontHeight());
        }
        else if (tag.isHasFontClass())
        {
            tagBuffer.writeString(tag.getFontClass());
            tagBuffer.writeUI16(tag.getFontHeight());
        }

        if (tag.isHasTextColor())
            writeRGBA(tag.getTextColor());

        if (tag.isHasMaxLength())
            tagBuffer.writeUI16(tag.getMaxLength());

        if (tag.isHasLayout())
        {
            tagBuffer.writeUI8(tag.getAlign());
            tagBuffer.writeUI16(tag.getLeftMargin());
            tagBuffer.writeUI16(tag.getRightMargin());
            tagBuffer.writeUI16(tag.getIndent());
            tagBuffer.writeSI16(tag.getLeading());
        }

        tagBuffer.writeString(tag.getVariableName());

        if (tag.isHasText())
            tagBuffer.writeString(tag.getInitialText());

        CSMTextSettingsTag textSettings = tag.getCSMTextSettings();
        if (textSettings != null)
            extraTags.add(textSettings);
    }

    private void writeDefineText2(DefineText2Tag tag, Collection<ITag> extraTags)
    {
        writeDefineText(tag, extraTags);
    }

    private void writeDefineText(DefineTextTag tag, Collection<ITag> extraTags)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        writeRect(tag.getTextBounds());
        writeMatrix(tag.getTextMatrix());
        tagBuffer.writeUI8(tag.getGlyphBits());
        tagBuffer.writeUI8(tag.getAdvanceBits());
        for (TextRecord textRecord : tag.getTextRecords())
        {
            writeTextRecord(textRecord, tag);
        }
        tagBuffer.byteAlign();
        tagBuffer.writeUI8(0); // end of records

        CSMTextSettingsTag textSettings = tag.getCSMTextSettings();
        if (textSettings != null)
            extraTags.add(textSettings);
    }

    private void writeTextRecord(TextRecord textRecord, DefineTextTag tag)
    {
        tagBuffer.byteAlign();
        tagBuffer.writeBit(true); // TextRecordType always 1.
        tagBuffer.writeUB(0, 3); // reserved
        tagBuffer.writeBit(textRecord.isStyleFlagsHasFont());
        tagBuffer.writeBit(textRecord.isStyleFlagsHasColor());
        tagBuffer.writeBit(textRecord.isStyleFlagsHasYOffset());
        tagBuffer.writeBit(textRecord.isStyleFlagsHasXOffset());
        // 8 bits - no need to align

        if (textRecord.isStyleFlagsHasFont())
        {
            tagBuffer.writeUI16(textRecord.getFontTag().getCharacterID());
        }

        if (textRecord.isStyleFlagsHasColor())
        {
            if (tag.getTagType() == TagType.DefineText2)
            {
                assert textRecord.getTextColor() instanceof RGBA;
                writeRGBA((RGBA)textRecord.getTextColor());
            }
            else
            {
                writeRGB(textRecord.getTextColor());
            }
        }

        if (textRecord.isStyleFlagsHasXOffset())
        {
            tagBuffer.writeSI16(textRecord.getxOffset());
        }

        if (textRecord.isStyleFlagsHasYOffset())
        {
            tagBuffer.writeSI16(textRecord.getyOffset());
        }

        if (textRecord.isStyleFlagsHasFont())
        {
            tagBuffer.writeUI16(textRecord.getTextHeight());
        }

        tagBuffer.writeUI8(textRecord.getGlyphCount());

        assert textRecord.getGlyphCount() == textRecord.getGlyphEntries().length;

        for (final GlyphEntry entry : textRecord.getGlyphEntries())
        {
            writeGlyphEntry(entry, tag);
        }
    }

    /**
     * @param entry
     * @param tag
     */
    private void writeGlyphEntry(GlyphEntry entry, DefineTextTag tag)
    {
        tagBuffer.writeUB(entry.getGlyphIndex(), tag.getGlyphBits());
        tagBuffer.writeSB(entry.getGlyphAdvance(), tag.getAdvanceBits());
    }

    private void writeDefineFontName(DefineFontNameTag tag)
    {
        tagBuffer.writeUI16(tag.getFontTag().getCharacterID());
        tagBuffer.writeString(tag.getFontName());
        tagBuffer.writeString(tag.getFontCopyright());
    }

    private void writeDefineFontAlignZones(DefineFontAlignZonesTag tag)
    {
        tagBuffer.writeUI16(tag.getFontTag().getCharacterID());
        tagBuffer.writeUB(tag.getCsmTableHint(), 2);
        tagBuffer.writeUB(0, 6); // reserved
        tagBuffer.byteAlign();
        for (final ZoneRecord zoneRecord : tag.getZoneTable())
        {
            writeZoneRecord(zoneRecord);
        }
    }

    /**
     * @param zoneRecord
     */
    private void writeZoneRecord(ZoneRecord zoneRecord)
    {
        assert zoneRecord.getNumZoneData() == 2;
        tagBuffer.writeUI8(2); // always 2
        tagBuffer.writeUI32(zoneRecord.getZoneData0().getData());
        tagBuffer.writeUI32(zoneRecord.getZoneData1().getData());
        tagBuffer.writeUB(0, 6); // reserved
        tagBuffer.writeBit(zoneRecord.isZoneMaskY());
        tagBuffer.writeBit(zoneRecord.isZoneMaskX());

    }

    private void writeDefineFont3(DefineFont3Tag tag, Collection<ITag> extraTags)
    {
        DefineFontAlignZonesTag zones = tag.getZones();
        if (zones != null)
            extraTags.add(zones);

        writeDefineFont2(tag, extraTags);
    }

    /**
     * @see SWFReader#readDefineFont2
     */
    private void writeDefineFont2(DefineFont2Tag tag, Collection<ITag> extraTags)
    {
        // need to write the glyphTable to a buffer first, so as to work out
        // size size of the table, so we know whether wide offsets are needed
        final int numGlyphs = tag.getNumGlyphs();
        int[] shapeSizes = new int[numGlyphs];
        IOutputBitStream shapeBuffer = writeGlyphTableToBuffer(numGlyphs, tag, shapeSizes);

        // if the shape table is bigger that 65535 bytes, we need to use
        // wide offsets if we're not already
        if (!tag.isFontFlagsWideOffsets() && shapeBuffer.size() > 65535)
        {
            tag.setFontFlagsWideOffsets(true);
        }

        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeBit(tag.isFontFlagsHasLayout());
        tagBuffer.writeBit(tag.isFontFlagsShiftJIS());
        tagBuffer.writeBit(tag.isFontFlagsSmallText());
        tagBuffer.writeBit(tag.isFontFlagsANSI());
        tagBuffer.writeBit(tag.isFontFlagsWideOffsets());
        tagBuffer.writeBit(tag.isFontFlagsWideCodes());
        tagBuffer.writeBit(tag.isFontFlagsItalic());
        tagBuffer.writeBit(tag.isFontFlagsBold());
        // 8bits - no need to align
        tagBuffer.writeUI8(tag.getLanguageCode());
        writeLengthString(tag.getFontName());
        tagBuffer.writeUI16(numGlyphs);

        writeFontOffsetAndGlyphTable(shapeBuffer, shapeSizes, numGlyphs, tag.getTagType(), tag.isFontFlagsWideOffsets());

        assert tag.getCodeTable().length == tag.getNumGlyphs();
        for (int code : tag.getCodeTable())
        {
            if (tag.isFontFlagsWideCodes())
            {
                tagBuffer.writeUI16(code);
            }
            else
            {
                tagBuffer.writeUI8(code);
            }
        }

        if (tag.isFontFlagsHasLayout())
        {
            assert tag.getFontAdvanceTable().length == tag.getNumGlyphs();

            tagBuffer.writeSI16(tag.getFontAscent());
            tagBuffer.writeSI16(tag.getFontDescent());
            tagBuffer.writeSI16(tag.getFontLeading());

            for (int fontAdvance : tag.getFontAdvanceTable())
            {
                tagBuffer.writeSI16(fontAdvance);
            }

            assert tag.getFontBoundsTable().length == tag.getNumGlyphs();
            for (Rect bound : tag.getFontBoundsTable())
            {
                writeRect(bound);
            }

            tagBuffer.writeUI16(tag.getKerningCount());

            assert tag.getKerningCount() == tag.getFontKerningTable().length;
            for (KerningRecord kerning : tag.getFontKerningTable())
            {
                writeKerningRecord(kerning, tag.isFontFlagsWideCodes());
            }
        }

        DefineFontNameTag license = tag.getLicense();
        if (license != null)
            extraTags.add(license);
    }

    /**
     * @param kerning
     * @param fontFlagsWideCodes
     */
    private void writeKerningRecord(KerningRecord kerning, boolean fontFlagsWideCodes)
    {
        if (fontFlagsWideCodes)
        {
            tagBuffer.writeUI16(kerning.getCode1());
            tagBuffer.writeUI16(kerning.getCode2());
        }
        else
        {
            tagBuffer.writeUI32(kerning.getCode1());
            tagBuffer.writeUI32(kerning.getCode2());
        }
        tagBuffer.writeSI16(kerning.getAdjustment());
    }

    private void writeDefineFontInfo2(DefineFontInfo2Tag tag)
    {
        tagBuffer.writeUI16(tag.getFontTag().getCharacterID());
        writeLengthString(tag.getFontName());
        tagBuffer.writeUB(tag.getFontFlagsReserved(), 2);
        tagBuffer.writeBit(tag.isFontFlagsSmallText());
        tagBuffer.writeBit(tag.isFontFlagsShiftJIS());
        tagBuffer.writeBit(tag.isFontFlagsANSI());
        tagBuffer.writeBit(tag.isFontFlagsItalic());
        tagBuffer.writeBit(tag.isFontFlagsBold());
        tagBuffer.writeBit(tag.isFontFlagsWideCodes());
        // 8 bits - no need to align
        tagBuffer.writeUI8(tag.getLanguageCode());
        for (final int code : tag.getCodeTable())
        {
            if (tag.isFontFlagsWideCodes())
            {
                tagBuffer.writeUI16(code);
            }
            else
            {
                tagBuffer.writeUI8(code);
            }
        }
    }

    /**
     * @see SWFReader#readDefineFontInfo
     */
    private void writeDefineFontInfo(IFontInfo tag)
    {
        tagBuffer.writeUI16(tag.getFontTag().getCharacterID());
        writeLengthString(tag.getFontName());
        tagBuffer.writeUB(tag.getFontFlagsReserved(), 2);
        tagBuffer.writeBit(tag.isFontFlagsSmallText());
        tagBuffer.writeBit(tag.isFontFlagsShiftJIS());
        tagBuffer.writeBit(tag.isFontFlagsANSI());
        tagBuffer.writeBit(tag.isFontFlagsItalic());
        tagBuffer.writeBit(tag.isFontFlagsBold());
        tagBuffer.writeBit(tag.isFontFlagsWideCodes());
        // 8 bits - no need to align
        for (final int code : tag.getCodeTable())
        {
            if (tag.isFontFlagsWideCodes())
            {
                tagBuffer.writeUI16(code);
            }
            else
            {
                tagBuffer.writeUI8(code);
            }
        }
    }

    private IOutputBitStream writeGlyphTableToBuffer(int numGlyphs, DefineFontTag tag, int[] shapeSizes)
    {
        // create a separate buffer for the glyph table to calculate offsets
        // and then write it out at the end
        final IOutputBitStream currentTagBuffer = tagBuffer;
        final IOutputBitStream shapeBuffer = new OutputBitStream();
        tagBuffer = shapeBuffer;

        int currentOffset = 0;
        int previousOffset = 0;
        Shape[] shapes = tag.getGlyphShapeTable();
        for (int i = 0; i < numGlyphs; i++)
        {
            /**
             * The first STYLECHANGERECORD of each SHAPE in the GlyphShapeTable
             * does not use the LineStyle and LineStyles fields. In addition,
             * the first STYLECHANGERECORD of each shape must have both fields
             * StateFillStyle0 and FillStyle0 set to 1.
             */
            writeShape(shapes[i], tag.getTagType(), 1, 0);
            currentOffset = shapeBuffer.size();
            shapeSizes[i] = currentOffset - previousOffset;
            previousOffset = currentOffset;
        }

        // restore the original tag buffer;
        tagBuffer = currentTagBuffer;

        return shapeBuffer;
    }

    private void writeFontOffsetAndGlyphTable(IOutputBitStream shapeBuffer, int[] shapeSizes, int numGlyphs, TagType tagType, boolean wideOffsets)
    {
        int offsetTableElementSize = wideOffsets ? 4 : 2;

        int baseOffset = numGlyphs * offsetTableElementSize;
        if (tagType != TagType.DefineFont)
        {
            // baseOffset is now at the end of the GlyphShapeTable,
            // so add space for the CodeTableOffset value (2 or 4 bytes)
            // and that gets us to the start of the CodeTable
            if (wideOffsets)
                baseOffset += 4;
            else
                baseOffset += 2;
        }

        // Write offset table
        int currentOffset = baseOffset;
        for (int i = 0; i < numGlyphs; i++)
        {
            if (wideOffsets)
                tagBuffer.writeUI32(currentOffset);
            else
                tagBuffer.writeUI16(currentOffset);

            currentOffset += shapeSizes[i];
        }

        // Only write the CodeTableOffset if numGlyphs is > 0
        if (tagType != TagType.DefineFont && numGlyphs > 0)
        {
            assert (currentOffset == (baseOffset + shapeBuffer.size())) : "offset mismatch writing font glyph table";

            if (wideOffsets)
                tagBuffer.writeUI32(currentOffset);
            else
                tagBuffer.writeUI16(currentOffset);
        }

        // Write GlyphShapeTable from the already created buffer
        tagBuffer.write(shapeBuffer.getBytes(), 0, shapeBuffer.size());
        try
        {
            shapeBuffer.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see SWFReader#readDefineFont
     */
    private void writeDefineFont(DefineFontTag tag, Collection<ITag> extraTags)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        final int numGlyphs = tag.getGlyphShapeTable().length;
        int[] shapeSizes = new int[numGlyphs];
        IOutputBitStream shapeBuffer = writeGlyphTableToBuffer(numGlyphs, tag, shapeSizes);
        writeFontOffsetAndGlyphTable(shapeBuffer, shapeSizes, numGlyphs, tag.getTagType(), false);

        DefineFontNameTag license = tag.getLicense();
        if (license != null)
            extraTags.add(license);
    }

    /**
     * @see SWFReader#readDefineBitsJPEG3
     */
    private void writeDefineBitsJPEG3(DefineBitsJPEG3Tag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUI32(tag.getAlphaDataOffset());
        tagBuffer.write(tag.getImageData());
        tagBuffer.write(tag.getBitmapAlphaData());
    }

    /**
     * @see SWFReader#readDefineBitsJPEG2
     */
    private void writeDefineBitsJPEG2(DefineBitsJPEG2Tag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.write(tag.getImageData());
    }

    /**
     * @see SWFReader#readJPEGTables
     */
    private void writeJPEGTables(JPEGTablesTag tag)
    {
        tagBuffer.write(tag.getJpegData());
    }

    /**
     * @see SWFReader#readDefineBits
     */
    private void writeDefineBits(DefineBitsTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.write(tag.getImageData());
    }

    /**
     * @see SWFReader#readDefineScalingGrid
     */
    private void writeDefineScalingGrid(DefineScalingGridTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacter().getCharacterID());
        writeRect(tag.getSplitter());
    }

    /**
     * @see SWFReader#readExportAssets
     */
    private void writeExportAssets(ExportAssetsTag tag)
    {
        tagBuffer.writeUI16(tag.size());
        for (final String name : tag.getCharacterNames())
        {
            final ICharacterTag characterTag = tag.getCharacterTagByName(name);
            tagBuffer.writeUI16(characterTag.getCharacterID());
            tagBuffer.writeString(name);
        }
    }

    /**
     * @see SWFReader#readDefineSprite
     */
    protected void writeDefineSprite(DefineSpriteTag tag)
    {
        tagBuffer.writeUI16(tag.getCharacterID());
        tagBuffer.writeUI16(tag.getFrameCount());

        // Tag buffer for embedded control tags.
        final IOutputBitStream controlTagBuffer = new OutputBitStream();
        for (final ITag controlTag : tag.getControlTags())
        {
            controlTagBuffer.reset();
            // DefineSprite's tagBuffer is the target output for the embedded
            // tags.
            writeTag(controlTag, controlTagBuffer, tagBuffer);
        }

        // write end marker
        tagBuffer.writeUI16(0);
    }

    /**
     * This method does not close the {@code output} stream.
     */
    @Override
    public void writeTo(OutputStream output)
    {
        assert output != null;

        writtenTags = new HashSet<ITag>();

        // The SWF data after the first 8 bytes can be compressed. At this
        // moment, we only encode the "compressible" part.
        writeCompressibleHeader();

        // FileAttributes must be the first tag.
        writeTag(SWF.getFileAttributes(swf));

        // Raw Metadata
        String metadata = swf.getMetadata();

        if (metadata != null) {
           writeTag(new MetadataTag(metadata));
        }

        // SetBackgroundColor tag
        final RGB backgroundColor = swf.getBackgroundColor();
        if (backgroundColor != null) {
            writeTag(new SetBackgroundColorTag(backgroundColor));
        }

        // EnableDebugger2 tag        
        if (enableDebug) {
            writeTag(new EnableDebugger2Tag("NO-PASSWORD"));
        }

        // EnableTelemetry tag
        if (enableTelemetry) {
           writeTag(new EnableTelemetryTag());
        }

        // ProductInfo tag for Flex compatibility
        ProductInfoTag productInfo = swf.getProductInfo();
        if (productInfo != null) {
            writeTag(productInfo);
        }

        // ScriptLimits tag
        final ScriptLimitsTag scriptLimitsTag = swf.getScriptLimits();
        if (scriptLimitsTag != null) {
            writeTag(scriptLimitsTag);
        }

        // Frames and enclosed tags.
        writeFrames();

        // End of SWF
        writeTag(new EndTag());

        writtenTags = null;

        // Compute the size of the SWF file.
        long length = outputBuffer.size() + 8;
        try
        {
            // write the first 8 bytes
            switch (useCompression)
            {
                case LZMA:
                    output.write('Z');
                    break;
                case ZLIB:
                    output.write('C');
                    break;
                case NONE:
                    output.write('F');
                    break;
                default:
                    assert false;
            }

            output.write('W');
            output.write('S');
            output.write(swf.getVersion());

            writeInt(output, (int)length);

            // write the "compressible" part
            switch (useCompression)
            {
                case LZMA:
                {
                    LZMACompressor compressor = new LZMACompressor();
                    compressor.compress(outputBuffer);
                    // now write the compressed length
                    final long compressedLength = compressor.getLengthOfCompressedPayload();
                    assert compressedLength <= 0xffffffffl;

                    writeInt(output, (int)compressedLength);

                    // now write the LZMA props
                    compressor.writeLZMAProperties(output);

                    // Normally LZMA (7zip) would write an 8 byte length here, but we don't, because the
                    // SWF header already has this info

                    // now write the n bytes of LZMA data, followed by the 6 byte EOF
                    compressor.writeDataAndEnd(output);
                    output.flush();
                }
                    break;
                case ZLIB:
                {
                    int compressionLevel = enableDebug ? Deflater.BEST_SPEED : Deflater.BEST_COMPRESSION;
                    Deflater deflater = new Deflater(compressionLevel);
                    DeflaterOutputStream deflaterStream = new DeflaterOutputStream(output, deflater);
                    deflaterStream.write(outputBuffer.getBytes(), 0, outputBuffer.size());
                    deflaterStream.finish();
                    deflater.end();
                    deflaterStream.flush();
                    break;
                }
                case NONE:
                {
                    output.write(outputBuffer.getBytes(), 0, outputBuffer.size());
                    output.flush();
                    break;
                }
                default:
                    assert false;
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * write a 32 bit integer into an output stream, in SWF byte ordering, which
     * is little-endian.
     */
    private void writeInt(OutputStream output, int theInt) throws IOException
    {
        output.write(theInt);
        output.write((theInt >> 8));
        output.write((theInt >> 16));
        output.write((theInt >> 24));
    }

    @Override
    public int writeTo(File outputFile) throws FileNotFoundException, IOException
    {
        // Ensure that the directory for the SWF exists.
        final File outputDirectory = new File(outputFile.getAbsoluteFile().getParent());
        outputDirectory.mkdirs();

        // Write out the SWF, counting how many bytes were written.
        final CountingOutputStream output =
                new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        writeTo(output);
        output.flush();
        output.close();
        close();

        final int swfSize = output.getCount();
        return swfSize;
    }

    private void writeFrameLabel(FrameLabelTag tag)
    {
        tagBuffer.writeString(tag.getName());
    }

    /**
     * Close the internal output buffer that stores the encoded SWF tags and
     * part of the SWF header. It does not close the {@link OutputStream}
     * argument in {@link #writeTo(OutputStream)}.
     */
    @Override
    public void close() throws IOException
    {
        outputBuffer.close();
    }
}
