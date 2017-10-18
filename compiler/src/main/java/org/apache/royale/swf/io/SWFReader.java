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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.problems.FileIOProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.SWFCSMTextSettingsWrongReferenceTypeProblem;
import org.apache.royale.compiler.problems.SWFCharacterIDNotFoundProblem;
import org.apache.royale.compiler.problems.SWFDefineFontAlignZonesLinkToIncorrectFontProblem;
import org.apache.royale.compiler.problems.SWFInvalidSignatureProblem;
import org.apache.royale.compiler.problems.SWFFrameCountMismatchProblem;
import org.apache.royale.compiler.problems.SWFTagLengthTooLongProblem;
import org.apache.royale.compiler.problems.SWFUnableToReadTagBodyProblem;
import org.apache.royale.compiler.problems.SWFUnexpectedEndOfFileProblem;
import org.apache.royale.compiler.problems.SWFUnknownFillStyleProblem;
import org.apache.royale.swf.Header;
import org.apache.royale.swf.Header.Compression;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.ITagContainer;
import org.apache.royale.swf.SWF;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.TagType;
import org.apache.royale.swf.tags.*;
import org.apache.royale.swf.types.BevelFilter;
import org.apache.royale.swf.types.BlurFilter;
import org.apache.royale.swf.types.ButtonRecord;
import org.apache.royale.swf.types.CXForm;
import org.apache.royale.swf.types.CXFormWithAlpha;
import org.apache.royale.swf.types.ClipActions;
import org.apache.royale.swf.types.ConvolutionFilter;
import org.apache.royale.swf.types.CurvedEdgeRecord;
import org.apache.royale.swf.types.DropShadowFilter;
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
import org.apache.royale.swf.types.ZoneData;
import org.apache.royale.swf.types.ZoneRecord;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Implementation of {@link ISWFReader}. This is a recursive-descent decoder of
 * a SWF file. Error handling for malformed SWFs: 1. Catch RuntimeExceptions
 * thrown by InputBitStream and report problems. 2. Handle errors in SWF tag
 * bodies by logging problems and throwing MalformedTagExceptions. 3. Recover
 * from #1 and #2 by throwing out the current tag and reading up to the start of
 * the next tag.
 */
public class SWFReader implements ISWFReader, ITagContainer
{
    /**
     * There is an error in the tag body that prevents the tag from being
     * completely and correctly read.
     */
    private static class MalformedTagException extends Exception
    {
        /**
         * 
         */
        private static final long serialVersionUID = -8030549610732167171L;

    }

    /**
     * A made-up tag to substitute for a tag with an invalid character id.
     */
    private static class InvalidTag extends CharacterTag implements ICharacterTag
    {
        /**
         * Some SWFs contained bad character id references.
         */
        public static final int BAD_CHARACTER_ID = 65535;

        public InvalidTag()
        {
            super(TagType.End);

            // Set a bogus character id that matches the bogus input value.
            // This lets us round trip reading/writing a SWF.
            setCharacterID(BAD_CHARACTER_ID);
        }
    }

    public static final InvalidTag INVALID_TAG = new InvalidTag();

    /**
     * Wrapper class for "type" and "length" field in a SWF tag header.
     */
    protected static class TagHeader
    {
        TagHeader(TagType type, int length)
        {
            this.type = type;
            this.length = length;
        }

        final TagType type;
        final int length;
    }

    /**
     * Mask on the TagCodeAndLength field to get the lower 6 bits of tag length.
     */
    protected static final int MASK_TAG_LENGTH = 0x3F;

    /**
     * The lower 6 bits in the TagCodeAndLength field in the SWF tag header is
     * the tag length.
     */
    protected static final int BITS_TAG_LENGTH = 6;

    // 2 bytes for UI16
    private static final int UI16_LENGTH = 2;
    // 4 bytes for SI32
    private static final int SI32_LENGTH = 4;

    /**
     * SWF input bit stream.
     */
    protected InputBitStream bitStream;

    /**
     * Model of the SWF file.
     */
    protected SWF swf;

    private String swfPath; // path associated with bitStream

    // Dictionary for resolving character ID to tag. 
    private final Map<Integer, ICharacterTag> dictionary;

    // Flag for whether buildFramesFromTags() needs to be called.
    private final boolean buildFrames;

    /**
     * All the tags in the SWF file. The frame building process is based on
     * these tags.
     */
    protected final List<ITag> tags;

    protected final Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

    /**
     * Create a SWFReader and initialize field members.
     */
    public SWFReader()
    {
        this(true);
    }

    /**
     * Create a SWFReader and initialize field members.
     * 
     * @param isBuildFrames if true, the reader will build SWF frames from tags
     * read
     */
    public SWFReader(boolean isBuildFrames)
    {
        this.buildFrames = isBuildFrames;
        tags = new ArrayList<ITag>();
        dictionary = new HashMap<Integer, ICharacterTag>();
        swf = new SWF();
    }

    @Override
    public ISWF readFrom(InputStream input, String path)
    {
        assert input != null && path != null;

        swfPath = FilenameNormalization.normalize(path);
        bitStream = new InputBitStream(input);
        try
        {
            if (readHeader())
                readTags();
        }
        catch (IOException e)
        {
            problems.add(new FileIOProblem(e));
        }
        
        if (buildFrames)
        {
            int expectedFrames = swf.getFrameCount();
            int foundFrames = swf.getFrames().size();
            if (expectedFrames != foundFrames)
            {
                problems.add(new SWFFrameCountMismatchProblem(
                        expectedFrames, foundFrames, swfPath));                
            }
        }
        return swf;
    }

    /**
     * Get the SWF tied to this reader. Note that the returned SWF may or not be
     * initialized depending on whether readFrom() has been called or not
     * 
     * @return swf
     */
    public ISWF getSWF()
    {
        return swf;
    }

    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }

    /**
     * Read the header and body of the next SWF tag.
     * 
     * @return SWF tag model, may be null if the tag is invalid.
     * @throws IOException error
     */
    private ITag nextTag() throws IOException
    {
        final TagHeader header = nextTagHeader();
        return readTag(header);
    }

    /**
     * Read SWF tags and add each tag to the tag list. Stop at the End tag.
     * 
     * @throws IOException error
     */
    protected void readTags() throws IOException
    {
        SWFFrame currentFrame = buildFrames ? new SWFFrame() : null;
        ITag tag;
        do
        {
            tag = nextTag();

            if (tag == null)
                continue;

            // deposit character tag to dictionary
            if (tag instanceof ICharacterTag)
            {
                addToDictionary((ICharacterTag)tag);
            }

            // save to tags list
            tags.add(tag);

            if (buildFrames)
                currentFrame = buildFramesFromTags(currentFrame, tag);

        }
        while (tag == null || tag.getTagType() != TagType.End);
    }

    /**
     * Read the next tag's header field and get the tag length and type.
     * 
     * @return next tag header
     */
    protected TagHeader nextTagHeader()
    {
        try
        {
            bitStream.setReadBoundary(bitStream.getOffset() + UI16_LENGTH);
            // get tag code and length
            final int tagCodeAndLength = bitStream.readUI16();
            final TagType tagType = TagType.getTagType(tagCodeAndLength >>> BITS_TAG_LENGTH);
            int tagLength = tagCodeAndLength & MASK_TAG_LENGTH;
            if (tagLength == MASK_TAG_LENGTH)
            {
                bitStream.setReadBoundary(bitStream.getOffset() + SI32_LENGTH);
                // long tag header uses an SI32 field for tag length
                tagLength = bitStream.readSI32();
            }
            return new TagHeader(tagType, tagLength);
        }
        catch (Exception e)
        {
            // Unexpected end of file.
            // Log a problem and return an end tag so the
            // outer loop will terminate normally.
            problems.add(new SWFUnexpectedEndOfFileProblem(swfPath));
            return new TagHeader(TagType.End, 0);
        }
    }

    /**
     * Read a tag body. A "read boundary" is marked to the length of the tag to
     * prevent invalid tag or incorrect decoding logic from contaminating the
     * following tags or having left-over bytes after decoding a tag.
     * 
     * @param header tag header
     * @return tag model or null if the tag is invalid.
     * @throws IOException error
     */
    protected ITag readTag(TagHeader header) throws IOException
    {
        bitStream.setReadBoundary(bitStream.getOffset() + header.length);
        ITag tag = null;

        try
        {
            tag = readTagBody(header.type);
        }
        catch (RuntimeException e)
        {
            problems.add(new SWFUnableToReadTagBodyProblem(header.type.getValue(),
                    header.length, swfPath, bitStream.getOffset()));

            // recover by reading the rest of the tag.            
        }
        catch (MalformedTagException e)
        {
            // We have already logged problems for these.
            // recover by reading the rest of the tag.
        }

        // If the read-boundary was not reached, consume the additional bytes
        // assuming an incorrectly formatted SWF tag was encountered. 
        if (bitStream.getOffset() < bitStream.getReadBoundary())
        {
            try
            {
                // The tag is too long but there is no reason to assume
                // the data we read is invalid. We'll treat the data as 
                // valid. Only report a problem if any of the remaining
                // bytes are non-zero.
                boolean nonZeroBytes = false;
                long oldOffset = bitStream.getOffset();
                while (bitStream.getOffset() < bitStream.getReadBoundary())
                {
                    if (bitStream.readByte() != 0)
                        nonZeroBytes = true;
                }

                if (nonZeroBytes)
                {
                    problems.add(new SWFTagLengthTooLongProblem(header.type.getValue(),
                            swfPath, oldOffset, bitStream.getReadBoundary()));
                }
            }
            catch (Exception e)
            {
                // Unable to skip to the end of the tag.
                return null;
            }
        }

        return tag;
    }

    /**
     * Add an {@code ICharacterTag} to the character dictionary.
     * 
     * @param tag character tag
     */
    private void addToDictionary(ICharacterTag tag)
    {
        dictionary.put(tag.getCharacterID(), tag);
    }

    /**
     * Build {@code SWFFrame} model from a series of tags as they are
     * encountered in the SWF.
     * 
     * @param currentFrame The current frame to add the tag to.
     * @param tag The current tag.
     * @return The current frame. A new frame will be returned when a ShowFrame
     * tag is encountered. Otherwise the currentFrame parameter will be
     * returned.
     */
    private SWFFrame buildFramesFromTags(SWFFrame currentFrame, ITag tag)
    {
        if (tag instanceof IManagedTag)
        {
            // managed tags    
            switch (tag.getTagType())
            {
                case ShowFrame:
                    swf.addFrame(currentFrame);
                    currentFrame = new SWFFrame();
                    break;
                case FrameLabel:
                    final FrameLabelTag frameLabel = (FrameLabelTag)tag;
                    currentFrame.setName(frameLabel.getName(), frameLabel.isNamedAnchorTag());
                    break;
                case Metadata:
                    swf.setMetadata(((MetadataTag)tag).getMetadata());
                    break;
                case FileAttributes:
                    final FileAttributesTag fileAttributes = (FileAttributesTag)tag;
                    swf.setUseAS3(fileAttributes.isAS3());
                    swf.setUseDirectBlit(fileAttributes.isUseDirectBlit());
                    swf.setUseGPU(fileAttributes.isUseGPU());
                    swf.setUseNetwork(fileAttributes.isUseNetwork());
                    break;
                case SetBackgroundColor:
                    swf.setBackgroundColor(((SetBackgroundColorTag)tag).getColor());
                    break;
                case SymbolClass:
                    final SymbolClassTag symbolClass = (SymbolClassTag)tag;
                    for (final String name : symbolClass.getSymbolNames())
                    {
                        final ICharacterTag exportedCharacter = symbolClass.getSymbol(name);
                        currentFrame.defineSymbol(exportedCharacter, name, dictionary);
                    }
                    break;
                case EnableDebugger2:
                    swf.setEnableDebugger2((EnableDebugger2Tag)tag);
                    break;
                case ProductInfo:
                    swf.setProductInfo((ProductInfoTag)tag);
                    break;
                case DefineSceneAndFrameLabelData:
                case ScriptLimits:
                case ExportAssets:
                case ImportAssets:
                case End:
                    // TODO: store on ISWF instance
                    break;
                default:
                    assert false : "Unhandled managed tag: " + tag;
            }
        }
        else
        {
            currentFrame.addTag(tag);
        }

        return currentFrame;
    }

    /**
     * Close the reader an the underlying input stream.
     */
    @Override
    public void close() throws IOException
    {
        if (bitStream != null)
            bitStream.close();
    }

    private ICharacterTag getTagById(int id, TagType tagType) throws MalformedTagException
    {
        if (dictionary.containsKey(id))
        {
            return dictionary.get(id);
        }
        else
        {
            // [tpr 7/6/04] work around authoring tool bug of bogus 65535 ids
            if (id != InvalidTag.BAD_CHARACTER_ID)
            {
                problems.add(new SWFCharacterIDNotFoundProblem(id,
                        tagType.getValue(), swfPath, bitStream.getOffset()));
                throw new MalformedTagException();
            }
            else
            {
                return INVALID_TAG;
            }
        }
    }

    /**
     * Get all the tags in this SWF file.
     */
    @Override
    public Iterator<ITag> iterator()
    {
        return tags.iterator();
    }

    private CXFormWithAlpha readColorTransformWithAlpha()
    {
        bitStream.byteAlign();
        final CXFormWithAlpha cxFormWithAlpha = new CXFormWithAlpha();
        final boolean hasAddTerms = bitStream.readBit();
        final boolean hasMultTerms = bitStream.readBit();
        final int nbits = bitStream.readUB(4);

        if (hasMultTerms)
        {
            cxFormWithAlpha.setMultTerm(
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits));
        }

        if (hasAddTerms)
        {
            cxFormWithAlpha.setAddTerm(
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits));
        }

        return cxFormWithAlpha;
    }

    private CurvedEdgeRecord readCurvedEdgeRecord() throws IOException
    {
        final CurvedEdgeRecord curvedEdgeRecord = new CurvedEdgeRecord();
        final int nbits = 2 + bitStream.readUB(4);
        curvedEdgeRecord.setControlDeltaX(bitStream.readSB(nbits));
        curvedEdgeRecord.setControlDeltaY(bitStream.readSB(nbits));
        curvedEdgeRecord.setAnchorDeltaX(bitStream.readSB(nbits));
        curvedEdgeRecord.setAnchorDeltaY(bitStream.readSB(nbits));
        return curvedEdgeRecord;
    }

    private DefineBinaryDataTag readDefineBinaryData() throws IOException
    {
        final int characterId = bitStream.readUI16();
        bitStream.readUI32(); // Skip reserved UI32.
        final byte[] data = bitStream.readToBoundary();
        final DefineBinaryDataTag result = new DefineBinaryDataTag(data);
        result.setCharacterID(characterId);
        return result;
    }

    // The following are decoding methods for SWF tags and types.

    private DefineBitsLosslessTag readDefineBitsLossless() throws IOException
    {
        return readDefineBitsLossless(new DefineBitsLosslessTag());
    }

    private DefineBitsLossless2Tag readDefineBitsLossless2() throws IOException
    {
        return (DefineBitsLossless2Tag)readDefineBitsLossless(new DefineBitsLossless2Tag());
    }

    /**
     * This method treats the bytes after the color table as a binary blob so
     * both the lossless and lossless2 tags can be read using this method.
     * 
     * @param tag
     * @return reference to tag parameter.
     * @throws IOException
     */
    private DefineBitsLosslessTag readDefineBitsLossless(DefineBitsLosslessTag tag) throws IOException
    {
        tag.setCharacterID(bitStream.readUI16());
        tag.setBitmapFormat(bitStream.readUI8());
        tag.setBitmapWidth(bitStream.readUI16());
        tag.setBitmapHeight(bitStream.readUI16());
        if (tag.getBitmapFormat() == DefineBitsLosslessTag.BF_8BIT_COLORMAPPED_IMAGE)
        {
            tag.setBitmapColorTableSize(bitStream.readUI8() + 1);
        }
        tag.setZlibBitmapData(bitStream.readToBoundary());
        addToDictionary(tag);
        return tag;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeDefineScalingGrid
     */
    private DefineScalingGridTag readDefineScalingGrid() throws MalformedTagException
    {
        final int characterId = bitStream.readUI16();
        final ICharacterTag character = getTagById(characterId,
                TagType.DefineScalingGrid);
        final Rect splitter = readRect();

        return new DefineScalingGridTag(character, splitter);
    }

    private ITag readDefineSceneAndFrameLabelData()
    {
        final DefineSceneAndFrameLabelDataTag tag = new DefineSceneAndFrameLabelDataTag();

        final long sceneCount = bitStream.readEncodedU32();
        for (long i = 0; i < sceneCount; i++)
        {
            final long offset = bitStream.readEncodedU32();
            final String name = bitStream.readString();
            tag.addScene(name, offset);
        }

        final long frameLabelCount = bitStream.readEncodedU32();
        for (long i = 0; i < frameLabelCount; i++)
        {
            final long frameNum = bitStream.readEncodedU32();
            final String frameLabel = bitStream.readString();
            tag.addFrame(frameLabel, frameNum);
        }

        return tag;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeDefineShape
     */
    private DefineShapeTag readDefineShape() throws IOException, MalformedTagException
    {
        final DefineShapeTag tag = new DefineShapeTag();
        tag.setCharacterID(bitStream.readUI16());
        tag.setShapeBounds(readRect());
        final ShapeWithStyle shapeWithStyle = readShapeWithStyle(TagType.DefineShape);
        tag.setShapes(shapeWithStyle);
        return tag;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeDefineShape2
     */
    private DefineShape2Tag readDefineShape2() throws IOException, MalformedTagException
    {
        final DefineShape2Tag tag = new DefineShape2Tag();
        tag.setCharacterID(bitStream.readUI16());
        tag.setShapeBounds(readRect());
        final ShapeWithStyle shapeWithStyle = readShapeWithStyle(TagType.DefineShape2);
        tag.setShapes(shapeWithStyle);
        return tag;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeDefineShape3
     */
    private DefineShape3Tag readDefineShape3() throws IOException, MalformedTagException
    {
        final DefineShape3Tag tag = new DefineShape3Tag();
        tag.setCharacterID(bitStream.readUI16());
        tag.setShapeBounds(readRect());
        final ShapeWithStyle shapeWithStyle = readShapeWithStyle(TagType.DefineShape3);
        tag.setShapes(shapeWithStyle);
        return tag;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeDefineShape4
     */
    private DefineShape4Tag readDefineShape4() throws IOException, MalformedTagException
    {
        final DefineShape4Tag tag = new DefineShape4Tag();
        tag.setCharacterID(bitStream.readUI16());
        tag.setShapeBounds(readRect());
        tag.setEdgeBounds(readRect());
        bitStream.readUB(5); // skip reserved UB[5]
        tag.setUsesFillWindingRule(bitStream.readBit());
        tag.setUsesNonScalingStrokes(bitStream.readBit());
        tag.setUsesScalingStrokes(bitStream.readBit());
        // 8 bits. No need to align.
        final ShapeWithStyle shapeWithStyle = readShapeWithStyle(TagType.DefineShape4);
        tag.setShapes(shapeWithStyle);
        return tag;
    }

    /**
     * @see SWFWriter#writeDefineSprite
     */
    private DefineSpriteTag readDefineSprite() throws IOException
    {
        final long boundary = bitStream.getReadBoundary();
        final int spriteId = bitStream.readUI16();
        final int frameCount = bitStream.readUI16();

        final List<ITag> spriteTags = new ArrayList<ITag>();
        ITag spriteTag;
        do
        {
            spriteTag = nextTag();

            if (spriteTag != null && spriteTag.getTagType() != TagType.End)
                spriteTags.add(spriteTag);
        }
        while (spriteTag == null || spriteTag.getTagType() != TagType.End);

        bitStream.setReadBoundary(boundary);
        DefineSpriteTag sprite = new DefineSpriteTag(frameCount, spriteTags);
        sprite.setCharacterID(spriteId);
        return sprite;
    }

    protected DoABCTag readDoABC() throws IOException
    {
        final long flag = bitStream.readUI32();
        final String name = bitStream.readString();
        final byte[] abcData = bitStream.readToBoundary();
        return new DoABCTag(flag, name, abcData);
    }

    private EnableDebugger2Tag readEnableDebugger2()
    {
        bitStream.readUI16();
        return new EnableDebugger2Tag(bitStream.readString());
    }

    private EnableTelemetryTag readEnableTelemetry()
    {
        // Read the reserved 2 bytes
        bitStream.readUI16();
        String password = bitStream.readString();
        return new EnableTelemetryTag(password);
    }

    private EndTag readEnd()
    {
        return new EndTag();
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeExportAssets
     */
    private ExportAssetsTag readExportAssets() throws MalformedTagException
    {
        final ExportAssetsTag tag = new ExportAssetsTag();
        final int count = bitStream.readUI16();
        for (int i = 0; i < count; i++)
        {
            final int id = bitStream.readUI16();
            final String name = bitStream.readString();
            tag.addExport(getTagById(id, tag.getTagType()), name);
        }
        return tag;
    }

    private FileAttributesTag readFileAttributes()
    {
        final FileAttributesTag tag = new FileAttributesTag();
        bitStream.readUB(1);
        tag.setUseDirectBlit(bitStream.readBit());
        tag.setUseGPU(bitStream.readBit());
        tag.setHasMetadata(bitStream.readBit());
        tag.setAS3(bitStream.readBit());
        bitStream.readUB(2);
        tag.setUseNetwork(bitStream.readBit());
        bitStream.readUB(24);
        return tag;
    }

    /**
     * Reads in appropriate type of IFillStyle, as determined by tagType
     * 
     * @return valid FillStyle.
     * @throws MalformedTagException
     */
    private IFillStyle readFillStyle(TagType tagType) throws MalformedTagException
    {
        switch (tagType)
        {
            case DefineMorphShape:
            case DefineMorphShape2:
                return readMorphFillStyle(tagType);
            default:
                return readStandardFillStyle(tagType);
        }
    }

    /**
     * Reads the non-morph fill styles
     * 
     * @return A {@link FillStyle}.
     * @throws MalformedTagException
     * @throws RuntimeException if the FillStyle is invalid.
     */
    private FillStyle readStandardFillStyle(TagType tagType) throws MalformedTagException
    {
        final FillStyle s = new FillStyle();
        final int type = bitStream.readUI8();
        s.setFillStyleType(type);

        switch (type)
        {
            case FillStyle.SOLID_FILL:
                switch (tagType)
                {
                    case DefineShape3:
                    case DefineShape4:
                        s.setColor(readRGBA());
                        break;
                    case DefineShape2:
                    case DefineShape:
                        s.setColor(readRGB());
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid tag: " + tagType);
                }
                break;
            case FillStyle.LINEAR_GRADIENT_FILL:
            case FillStyle.RADIAL_GRADIENT_FILL:
                s.setGradientMatrix(readMatrix());
                s.setGradient(readGradient(tagType));
                break;
            case FillStyle.FOCAL_RADIAL_GRADIENT_FILL:
                s.setGradientMatrix(readMatrix());
                s.setGradient(readFocalGradient(tagType));
                break;
            case FillStyle.REPEATING_BITMAP_FILL: // 0x40 tiled bitmap fill
            case FillStyle.CLIPPED_BITMAP_FILL: // 0x41 clipped bitmap fill
            case FillStyle.NON_SMOOTHED_REPEATING_BITMAP: // 0x42 tiled non-smoothed fill
            case FillStyle.NON_SMOOTHED_CLIPPED_BITMAP: // 0x43 clipped non-smoothed fill
                final int idref = bitStream.readUI16();
                s.setBitmapCharacter(getTagById(idref, tagType));
                s.setBitmapMatrix(readMatrix());
                break;
            default:
                problems.add(new SWFUnknownFillStyleProblem(type, false, swfPath, bitStream.getOffset()));
                throw new MalformedTagException();
        }

        return s;
    }

    private FillStyleArray readFillStyleArray(TagType tagType) throws MalformedTagException
    {
        final FillStyleArray fillStyleArray = new FillStyleArray();
        final int count = readExtensibleCount();
        for (int i = 0; i < count; i++)
        {
            final IFillStyle fillStyle = readFillStyle(tagType);
            fillStyleArray.add(fillStyle);
        }
        return fillStyleArray;
    }

    /**
     * @param tagType
     * @return a FocalGradient record
     */
    private FocalGradient readFocalGradient(TagType tagType)
    {
        bitStream.byteAlign();
        final FocalGradient gradient = new FocalGradient();
        gradient.setSpreadMode(bitStream.readUB(2));
        gradient.setInterpolationMode(bitStream.readUB(2));
        final int numGradients = bitStream.readUB(4);
        for (int i = 0; i < numGradients; i++)
        {
            gradient.getGradientRecords().add(readGradRecord(tagType));
        }
        gradient.setFocalPoint(bitStream.readFIXED8());
        return gradient;
    }

    private FrameLabelTag readFrameLabel() throws IOException
    {
        final String name = bitStream.readString();
        final FrameLabelTag tag = new FrameLabelTag(name);
        if (bitStream.getOffset() < bitStream.getReadBoundary())
        {
            final int flag = bitStream.readUI8();
            assert flag == 1 : "FrameLabel::NamedAnchorFlag must be 1.";
            tag.setNamedAnchorTag(true);
        }
        return tag;
    }

    private Gradient readGradient(TagType tagType)
    {
        bitStream.byteAlign();
        final Gradient gradient = new Gradient();
        gradient.setSpreadMode(bitStream.readUB(2));
        gradient.setInterpolationMode(bitStream.readUB(2));
        final int numGradients = bitStream.readUB(4);
        for (int i = 0; i < numGradients; i++)
        {
            gradient.getGradientRecords().add(readGradRecord(tagType));
        }
        return gradient;
    }

    /**
     * @param tagType
     * @return A gradient record.
     */
    private GradRecord readGradRecord(TagType tagType)
    {
        final int ratio = bitStream.readUI8();
        RGB color = null;
        if (TagType.DefineShape == tagType || TagType.DefineShape2 == tagType)
        {
            color = readRGB();
        }
        else if (TagType.DefineShape3 == tagType || TagType.DefineShape4 == tagType)
        {
            color = readRGBA();
        }
        else
        {
            throw new IllegalArgumentException("Invalid tag: " + tagType);
        }

        return new GradRecord(ratio, color);
    }

    /**
     * Read SWF header.
     * 
     * @return true if successful, false if there is an error in the header.
     * @throws IOException
     */
    protected boolean readHeader() throws IOException
    {
        Header header = swf.getHeader();
        try
        {
            bitStream.setReadBoundary(8); // 4 x UI8 and 1 x UI32
            final char[] signature = new char[] {
                (char)bitStream.readUI8(),
                (char)bitStream.readUI8(),
                (char)bitStream.readUI8()};

            if (!header.isSignatureValid(signature))
            {
                problems.add(new SWFInvalidSignatureProblem(swfPath));
                return false;
            }
            
            header.setSignature(signature);
            header.setVersion((byte)bitStream.readUI8());
            header.setLength(bitStream.readUI32());

            if (header.getCompression() == Compression.LZMA)
            {
                bitStream.setReadBoundary(bitStream.getOffset() + 4);
                long compressedSize = bitStream.readUI32(); // read the 4 bytes compressedLen;     
                header.setCompressedLength(compressedSize);
            }

            bitStream.setCompress(header.getCompression());

            // Max length of a Rect is 17 bytes 
            bitStream.setReadBoundary(bitStream.getOffset() + 17);
            header.setFrameSize(readRect());

            bitStream.setReadBoundary(bitStream.getOffset() + 4);
            header.setFrameRate(bitStream.readFIXED8());
            header.setFrameCount(bitStream.readUI16());
        }
        catch (RuntimeException e)
        {
            problems.add(new SWFUnexpectedEndOfFileProblem(swfPath));
            return false;
        }
        
        return true;
    }

    private ILineStyle readLineStyle(TagType tagType) throws MalformedTagException
    {
        ILineStyle result = null;
        if (tagType == TagType.DefineShape4)
        {
            final LineStyle2 s = new LineStyle2();
            s.setWidth(bitStream.readUI16());
            s.setStartCapStyle(bitStream.readUB(2));
            s.setJoinStyle(bitStream.readUB(2));
            s.setHasFillFlag(bitStream.readBit());
            s.setNoHScaleFlag(bitStream.readBit());
            s.setNoVScaleFlag(bitStream.readBit());
            s.setPixelHintingFlag(bitStream.readBit());
            bitStream.readUB(5);
            s.setNoClose(bitStream.readBit());
            s.setEndCapStyle(bitStream.readUB(2));

            if (s.getJoinStyle() == LineStyle2.JS_MITER_JOIN)
            {
                s.setMiterLimitFactor(bitStream.readUI16()); // 8.8 fixed point
            }

            if (s.isHasFillFlag())
            {
                IFillStyle fillStyle = readFillStyle(tagType);
                s.setFillType((FillStyle)fillStyle);
                // Default to #00000000 when there's no color,
                // to match behavior of old SWF reader
                s.setColor(new RGBA(0, 0, 0, 0));
            }
            else
            {
                s.setColor(readRGBA());
            }
            result = s;
        }
        else if (tagType == TagType.DefineMorphShape)
        {
            result = readMorphLineStyle();
        }
        else if (tagType == TagType.DefineMorphShape2)
        {
            result = readMorphLineStyle2(tagType);
        }
        else if (tagType == TagType.DefineShape3)
        {
            LineStyle ls = new LineStyle();
            result = ls;
            ls.setWidth(bitStream.readUI16());
            ls.setColor(readRGBA());
        }
        else
        {
            LineStyle ls = new LineStyle();
            result = ls;
            ls.setWidth(bitStream.readUI16());
            ls.setColor(readRGB());
        }
        return result;
    }

    private LineStyleArray readLineStyleArray(TagType tagType) throws MalformedTagException
    {
        final LineStyleArray lineStyleArray = new LineStyleArray();
        final int count = readExtensibleCount();

        for (int i = 0; i < count; i++)
        {
            lineStyleArray.add(readLineStyle(tagType));
        }
        return lineStyleArray;
    }

    protected Matrix readMatrix()
    {
        bitStream.byteAlign();

        final Matrix matrix = new Matrix();
        if (bitStream.readBit())
        {
            final int nScaleBits = bitStream.readUB(5);
            matrix.setScale(bitStream.readFB(nScaleBits),
                            bitStream.readFB(nScaleBits));
        }

        if (bitStream.readBit())
        {
            final int nRotateBits = bitStream.readUB(5);
            matrix.setRotate(bitStream.readFB(nRotateBits), bitStream.readFB(nRotateBits));
        }

        final int nTranslateBits = bitStream.readUB(5);
        matrix.setTranslate(bitStream.readSB(nTranslateBits), bitStream.readSB(nTranslateBits));

        bitStream.byteAlign();
        return matrix;
    }

    private MetadataTag readMetadata()
    {
        return new MetadataTag(bitStream.readString());
    }

    private PlaceObject2Tag readPlaceObject2() throws IOException, MalformedTagException
    {
        final PlaceObject2Tag tag = new PlaceObject2Tag();
        tag.setHasClipActions(bitStream.readBit());
        tag.setHasClipDepth(bitStream.readBit());
        tag.setHasName(bitStream.readBit());
        tag.setHasRatio(bitStream.readBit());
        tag.setHasColorTransform(bitStream.readBit());
        tag.setHasMatrix(bitStream.readBit());
        tag.setHasCharacter(bitStream.readBit());
        tag.setMove(bitStream.readBit());

        tag.setDepth(bitStream.readUI16());
        if (tag.isHasCharacter())
            tag.setCharacter(getTagById(bitStream.readUI16(), tag.getTagType()));
        if (tag.isHasMatrix())
            tag.setMatrix(readMatrix());
        if (tag.isHasColorTransform())
            tag.setColorTransform(readColorTransformWithAlpha());
        if (tag.isHasRatio())
            tag.setRatio(bitStream.readUI16());
        if (tag.isHasName())
            tag.setName(bitStream.readString());
        if (tag.isHasClipDepth())
            tag.setClipDepth(bitStream.readUI16());

        ClipActions clipActions = new ClipActions();
        clipActions.data = bitStream.readToBoundary();
        tag.setClipActions(clipActions);

        return tag;
    }

    private ProductInfoTag readProductInfo()
    {
        final ProductInfoTag.Product product = ProductInfoTag.Product.fromCode(
                bitStream.readSI32());
        final ProductInfoTag.Edition edition = ProductInfoTag.Edition.fromCode(
                bitStream.readSI32());
        final byte majorVersion = bitStream.readSI8();
        final byte minorVersion = bitStream.readSI8();
        final long build = bitStream.readSI64();
        final long compileDate = bitStream.readSI64();
        return new ProductInfoTag(
                product,
                edition,
                majorVersion,
                minorVersion,
                build,
                compileDate);
    }

    private RawTag readRawTag(TagType type) throws IOException
    {
        final RawTag rawTag = new RawTag(type);
        rawTag.setTagBody(bitStream.readToBoundary());
        return rawTag;
    }

    private Rect readRect()
    {
        bitStream.byteAlign();
        final int nbits = bitStream.readUB(5);
        final Rect rect = new Rect(
                bitStream.readSB(nbits),
                bitStream.readSB(nbits),
                bitStream.readSB(nbits),
                bitStream.readSB(nbits));
        bitStream.byteAlign();
        return rect;
    }

    private RGB readRGB()
    {
        return new RGB(
                bitStream.readUI8(),
                bitStream.readUI8(),
                bitStream.readUI8());
    }

    private RGBA readRGBA()
    {
        return new RGBA(
                bitStream.readUI8(),
                bitStream.readUI8(),
                bitStream.readUI8(),
                bitStream.readUI8());
    }

    private ScriptLimitsTag readScriptLimits()
    {
        return new ScriptLimitsTag(bitStream.readUI16(), bitStream.readUI16());
    }

    private SetBackgroundColorTag readSetBackgroundColor()
    {
        return new SetBackgroundColorTag(
                bitStream.readUI8(),
                bitStream.readUI8(),
                bitStream.readUI8());
    }

    private List<ShapeRecord> readShapeRecords(
            final TagType tagType,
            final Shape shape,
            final CurrentStyles currentStyles) throws IOException, MalformedTagException
    {
        final ArrayList<ShapeRecord> list = new ArrayList<ShapeRecord>();
        boolean endShapeRecord = false;
        do
        {
            final boolean isEdge = bitStream.readBit();
            if (isEdge)
            {
                final boolean isStraight = bitStream.readBit();
                if (isStraight)
                {
                    final StraightEdgeRecord straightEdge = readStraightEdgeRecord();
                    list.add(straightEdge);
                }
                else
                {
                    final CurvedEdgeRecord curvedEdge = readCurvedEdgeRecord();
                    list.add(curvedEdge);
                }
            }
            else
            {
                final boolean stateNewStyles = bitStream.readBit();
                final boolean stateLineStyle = bitStream.readBit();
                final boolean stateFillStyle1 = bitStream.readBit();
                final boolean stateFillStyle0 = bitStream.readBit();
                final boolean stateMoveTo = bitStream.readBit();

                if (stateNewStyles ||
                    stateLineStyle ||
                    stateFillStyle1 ||
                    stateFillStyle0 ||
                    stateMoveTo)
                {
                    final StyleChangeRecord styleChange = readStyleChangeRecord(
                            stateNewStyles,
                            stateLineStyle,
                            stateFillStyle1,
                            stateFillStyle0,
                            stateMoveTo,
                            tagType,
                            shape,
                            currentStyles);
                    list.add(styleChange);
                }
                else
                {
                    endShapeRecord = true;
                }
            }
        }
        while (!endShapeRecord);

        return list;

    }

    private ShapeWithStyle readShapeWithStyle(TagType tagType) throws IOException, MalformedTagException
    {
        // Read styles from SWF.
        final FillStyleArray fillStyles = readFillStyleArray(tagType);
        final LineStyleArray lineStyles = readLineStyleArray(tagType);
        bitStream.byteAlign();
        final int numFillBits = bitStream.readUB(4);
        final int numLineBits = bitStream.readUB(4);
        final Styles styles = new Styles(fillStyles, lineStyles);

        // Create styles context.
        final CurrentStyles currentStyles = new CurrentStyles();
        currentStyles.styles = styles;
        currentStyles.numFillBits = numFillBits;
        currentStyles.numLineBits = numLineBits;

        // Create ShapeWithStyle tag.
        final ShapeWithStyle shapes = new ShapeWithStyle(styles);
        shapes.setNumFillBits(numFillBits);
        shapes.setNumLineBits(numLineBits);

        // Read ShapeRecords and passing in the style context.
        final List<ShapeRecord> shapeRecords = readShapeRecords(tagType, shapes, currentStyles);
        shapes.addShapeRecords(shapeRecords);
        return shapes;
    }

    private Shape readShape(TagType tagType) throws IOException, MalformedTagException
    {
        bitStream.byteAlign();

        // Read styles from SWF.
        final int numFillBits = bitStream.readUB(4);
        final int numLineBits = bitStream.readUB(4);

        // Create styles context.
        final CurrentStyles currentStyles = new CurrentStyles();
        currentStyles.styles = null; // No initial style set.
        currentStyles.numFillBits = numFillBits;
        currentStyles.numLineBits = numLineBits;

        // Create ShapeWithStyle tag.
        final Shape shapes = new Shape();
        shapes.setNumFillBits(numFillBits);
        shapes.setNumLineBits(numLineBits);

        // Read ShapeRecords and passing in the style context.
        final List<ShapeRecord> shapeRecords = readShapeRecords(tagType, shapes, currentStyles);
        shapes.addShapeRecords(shapeRecords);
        return shapes;
    }

    /**
     * @see SWFWriter#writeMorphGradRecord
     */
    private MorphGradRecord readMorphGradRecord()
    {
        final int startRatio = bitStream.readUI8();
        final RGBA startColor = readRGBA();
        final int endRatio = bitStream.readUI8();
        final RGBA endColor = readRGBA();

        final MorphGradRecord result = new MorphGradRecord();
        result.setStartRatio(startRatio);
        result.setStartColor(startColor);
        result.setEndRatio(endRatio);
        result.setEndColor(endColor);
        return result;
    }

    /**
     * @see SWFWriter#writeMorphGradient
     */
    private MorphGradient readMorphGradient()
    {
        final MorphGradient result = new MorphGradient();

        final int numGradients = bitStream.readUI8();

        for (int idx = 0; idx < numGradients; idx++)
        {
            final MorphGradRecord gradientRecord = readMorphGradRecord();
            result.add(gradientRecord);
        }

        return result;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeMorphFillStyle
     */
    private MorphFillStyle readMorphFillStyle(TagType tagType) throws MalformedTagException
    {
        final MorphFillStyle result = new MorphFillStyle();
        final int fillStyleType = bitStream.readUI8();
        result.setFillStyleType(fillStyleType);
        switch (fillStyleType)
        {
            case FillStyle.SOLID_FILL:
                final RGBA startColor = readRGBA();
                final RGBA endColor = readRGBA();
                result.setStartColor(startColor);
                result.setEndColor(endColor);
                break;
            case FillStyle.LINEAR_GRADIENT_FILL:
            case FillStyle.RADIAL_GRADIENT_FILL:
            case FillStyle.FOCAL_RADIAL_GRADIENT_FILL:
                final Matrix startGradientMatrix = readMatrix();
                final Matrix endGradientMatrix = readMatrix();
                final MorphGradient gradient = readMorphGradient();
                result.setStartGradientMatrix(startGradientMatrix);
                result.setEndGradientMatrix(endGradientMatrix);
                result.setGradient(gradient);
                if (fillStyleType == FillStyle.FOCAL_RADIAL_GRADIENT_FILL &&
                    tagType.getValue() == TagType.DefineMorphShape2.getValue())
                {
                    result.setRatio1(bitStream.readSI16());
                    result.setRatio2(bitStream.readSI16());
                }
                break;
            case FillStyle.REPEATING_BITMAP_FILL:
            case FillStyle.CLIPPED_BITMAP_FILL:
            case FillStyle.NON_SMOOTHED_REPEATING_BITMAP:
            case FillStyle.NON_SMOOTHED_CLIPPED_BITMAP:
                final int bitmapId = bitStream.readUI16();
                final ICharacterTag bitmap = getTagById(bitmapId, tagType);
                final Matrix startBitmapMatrix = readMatrix();
                final Matrix endBitmapMatrix = readMatrix();
                result.setBitmap(bitmap);
                result.setStartBitmapMatrix(startBitmapMatrix);
                result.setEndBitmapMatrix(endBitmapMatrix);
                break;
            default:
                problems.add(new SWFUnknownFillStyleProblem(fillStyleType, true,
                        swfPath, bitStream.getOffset()));
                throw new MalformedTagException();
        }
        return result;
    }

    /**
     * @see SWFWriter#writeMorphLineStyle
     */
    private MorphLineStyle readMorphLineStyle()
    {
        final int startWidth = bitStream.readUI16();
        final int endWidth = bitStream.readUI16();
        final RGBA startColor = readRGBA();
        final RGBA endColor = readRGBA();

        final MorphLineStyle result = new MorphLineStyle();
        result.setStartWidth(startWidth);
        result.setEndWidth(endWidth);
        result.setStartColor(startColor);
        result.setEndColor(endColor);
        return result;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeMorphLineStyle2
     */
    private MorphLineStyle2 readMorphLineStyle2(TagType tagType) throws MalformedTagException
    {
        final MorphLineStyle2 result = new MorphLineStyle2();
        result.setStartWidth(bitStream.readUI16());
        result.setEndWidth(bitStream.readUI16());
        result.setStartCapStyle(bitStream.readUB(2));
        result.setJoinStyle(bitStream.readUB(2));
        result.setHasFillFlag(bitStream.readBit());
        result.setNoHScaleFlag(bitStream.readBit());
        result.setNoVScaleFlag(bitStream.readBit());
        result.setPixelHintingFlag(bitStream.readBit());
        bitStream.readUB(5); // Reserved
        result.setNoClose(bitStream.readBit());
        result.setEndCapStyle(bitStream.readUB(2));
        bitStream.byteAlign();

        if (LineStyle2.JS_MITER_JOIN == result.getJoinStyle())
        {
            result.setMiterLimitFactor(bitStream.readUI16());
        }

        if (!result.isHasFillFlag())
        {
            result.setStartColor(readRGBA());
            result.setEndColor(readRGBA());
        }
        else
        {
            result.setFillType(readMorphFillStyle(tagType));
        }

        return result;
    }

    /**
     * @see SWFWriter#writeDefineMorphShape
     */
    public DefineMorphShapeTag readDefineMorphShape() throws IOException, MalformedTagException
    {

        final int characterId = bitStream.readUI16();
        final Rect startBounds = readRect();
        final Rect endBounds = readRect();
        final long offset = bitStream.readUI32();

        final Shape startEdges = readShapeWithStyle(TagType.DefineMorphShape);
        final Shape endEdges = readShape(TagType.DefineMorphShape);

        final DefineMorphShapeTag tag = new DefineMorphShapeTag();
        tag.setCharacterID(characterId);
        tag.setStartBounds(startBounds);
        tag.setEndBounds(endBounds);
        tag.setOffset(offset);
        tag.setStartEdges(startEdges);
        tag.setEndEdges(endEdges);

        return tag;
    }

    /**
     * @see SWFWriter#writeDefineMorphShape2
     */
    public DefineMorphShape2Tag readDefineMorphShape2() throws IOException, MalformedTagException
    {
        final int characterId = bitStream.readUI16();
        final Rect startBounds = readRect();
        final Rect endBounds = readRect();
        final Rect startEdgeBounds = readRect();
        final Rect endEdgeBounds = readRect();
        bitStream.readUB(6); // Reserved
        final boolean usesNonScalingStrokes = bitStream.readBit();
        final boolean usesScalingStrokes = bitStream.readBit();
        // 8 bits already. No need to align.
        final long offset = bitStream.readUI32();

        final Shape startEdges = readShapeWithStyle(TagType.DefineMorphShape2);
        final Shape endEdges = readShape(TagType.DefineMorphShape2);

        final DefineMorphShape2Tag tag = new DefineMorphShape2Tag();
        tag.setCharacterID(characterId);
        tag.setStartBounds(startBounds);
        tag.setEndBounds(endBounds);
        tag.setOffset(offset);
        tag.setStartEdges(startEdges);
        tag.setEndEdges(endEdges);
        // new fields in MorphShape2
        tag.setStartEdgeBounds(startEdgeBounds);
        tag.setEndEdgeBounds(endEdgeBounds);
        tag.setUsesNonScalingStrokes(usesNonScalingStrokes);
        tag.setUsesScalingStrokes(usesScalingStrokes);

        return tag;
    }

    /**
     * Extensible count is common in SWF types. They share a pattern of: <br>
     * count : UI8 <br>
     * countExtended: UI16 if count=0xFF <br>
     * 
     * @return count value
     * @see SWFWriter#writeExtensibleCount
     */
    private int readExtensibleCount()
    {
        final int count = bitStream.readUI8();
        if (count == 0xFF)
        {
            final int countExtended = bitStream.readUI16();
            return countExtended;
        }
        else
        {
            return count;
        }
    }

    private ShowFrameTag readShowFrame()
    {
        return new ShowFrameTag();
    }

    private StraightEdgeRecord readStraightEdgeRecord() throws IOException
    {
        StraightEdgeRecord straightEdgeRecord = null;
        final int nbits = 2 + bitStream.readUB(4);
        final boolean isGeneralLine = bitStream.readBit();
        if (isGeneralLine)
        {
            final int dx = bitStream.readSB(nbits);
            final int dy = bitStream.readSB(nbits);
            straightEdgeRecord = new StraightEdgeRecord(dx, dy);
        }
        else
        {
            final boolean isVertLine = bitStream.readBit();
            if (isVertLine)
            {
                final int dy = bitStream.readSB(nbits);
                straightEdgeRecord = new StraightEdgeRecord(0, dy);
            }
            else
            {
                final int dx = bitStream.readSB(nbits);
                straightEdgeRecord = new StraightEdgeRecord(dx, 0);
            }
        }
        return straightEdgeRecord;
    }

    /**
     * A wrapper for a reference to a {@code Style} object.
     */
    static class CurrentStyles
    {
        Styles styles;
        int numFillBits;
        int numLineBits;
    }

    private StyleChangeRecord readStyleChangeRecord(
            boolean stateNewStyles,
            boolean stateLineStyle,
            boolean stateFillStyle1,
            boolean stateFillStyle0,
            boolean stateMoveTo,
            TagType tagType,
            Shape shape,
            CurrentStyles currentStyles) throws IOException, MalformedTagException
    {
        assert tagType != null;
        assert currentStyles != null;

        final StyleChangeRecord styleChange = new StyleChangeRecord();

        // move draw point
        if (stateMoveTo)
        {
            final int moveBits = bitStream.readUB(5);
            final int moveDeltaX = bitStream.readSB(moveBits);
            final int moveDeltaY = bitStream.readSB(moveBits);
            styleChange.setMove(moveDeltaX, moveDeltaY);
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

        // select a style
        final int indexFillStyle0 = stateFillStyle0 ? bitStream.readUB(currentStyles.numFillBits) : 0;
        final int indexFillStyle1 = stateFillStyle1 ? bitStream.readUB(currentStyles.numFillBits) : 0;
        final int indexLineStyle = stateLineStyle ? bitStream.readUB(currentStyles.numLineBits) : 0;
        final IFillStyle fillStyle0;
        if (indexFillStyle0 > 0 && !ignoreStyle)
            fillStyle0 = currentStyles.styles.getFillStyles().get(indexFillStyle0 - 1);
        else
            fillStyle0 = null;

        final IFillStyle fillStyle1;
        if (indexFillStyle1 > 0 && !ignoreStyle)
            fillStyle1 = currentStyles.styles.getFillStyles().get(indexFillStyle1 - 1);
        else
            fillStyle1 = null;

        final ILineStyle lineStyle;
        if (indexLineStyle > 0 && !ignoreStyle)
            lineStyle = currentStyles.styles.getLineStyles().get(indexLineStyle - 1);
        else
            lineStyle = null;

        styleChange.setDefinedStyles(fillStyle0, fillStyle1, lineStyle,
                stateFillStyle0, stateFillStyle1, stateLineStyle, currentStyles.styles);

        // "StateNewStyles" field is only used by DefineShape 2, 3 and 4 tags.
        final boolean isDefineShape234 = tagType == TagType.DefineShape2 ||
                                         tagType == TagType.DefineShape3 ||
                                         tagType == TagType.DefineShape4;

        // replace styles
        if (stateNewStyles && isDefineShape234)
        {
            // read from SWF
            final FillStyleArray fillStyles = readFillStyleArray(tagType);
            final LineStyleArray lineStyles = readLineStyleArray(tagType);
            bitStream.byteAlign();
            final int numFillBits = bitStream.readUB(4);
            final int numLineBits = bitStream.readUB(4);

            // update StyleChangeRecord
            final Styles newStyles = new Styles(fillStyles, lineStyles);
            styleChange.setNumFillBits(numFillBits);
            styleChange.setNumLineBits(numLineBits);
            styleChange.setNewStyles(newStyles);

            // update style context variable
            currentStyles.styles = newStyles;
            currentStyles.numFillBits = numFillBits;
            currentStyles.numLineBits = numLineBits;
        }

        return styleChange;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeSymbolClass
     */
    private SymbolClassTag readSymbolClass() throws MalformedTagException
    {
        final SymbolClassTag symbolClass = new SymbolClassTag();
        final int numSymbols = bitStream.readUI16();
        for (int i = 0; i < numSymbols; i++)
        {
            final int id = bitStream.readUI16();
            final String name = bitStream.readString();
            if (id == 0)
            {
                if (swf.getTopLevelClass() == null)
                    swf.setTopLevelClass(name);
            }
            else
            {
                symbolClass.addSymbol(getTagById(id,
                        symbolClass.getTagType()), name);
            }
        }

        return symbolClass;
    }

    /**
     * Select the tag decoding function by its type.
     * 
     * @param type tag type
     * @return tag model
     */
    protected ITag readTagBody(TagType type) throws IOException, MalformedTagException
    {
        // Sort "case" conditions alphabetically.

        switch (type)
        {
            case CSMTextSettings:
                return readCSMTextSettings();
            case DoABC:
                return readDoABC();
            case DefineBinaryData:
                return readDefineBinaryData();
            case DefineBits:
                return readDefineBits();
            case DefineBitsJPEG2:
                return readDefineBitsJPEG2();
            case DefineBitsJPEG3:
                return readDefineBitsJPEG3();
            case DefineBitsLossless:
                return readDefineBitsLossless();
            case DefineBitsLossless2:
                return readDefineBitsLossless2();
            case DefineScalingGrid:
                return readDefineScalingGrid();
            case DefineShape:
                return readDefineShape();
            case DefineShape2:
                return readDefineShape2();
            case DefineShape3:
                return readDefineShape3();
            case DefineShape4:
                return readDefineShape4();
            case DefineSprite:
                return readDefineSprite();
            case DefineSound:
                return readDefineSound();
            case StartSound:
                return readStartSound();
            case StartSound2:
                return readStartSound2();
            case SoundStreamHead:
                return readSoundStreamHead(type);
            case SoundStreamHead2:
                return readSoundStreamHead(type);
            case SoundStreamBlock:
                return readSoundStreamBlock();
            case DefineMorphShape:
                return readDefineMorphShape();
            case DefineMorphShape2:
                return readDefineMorphShape2();
            case DefineSceneAndFrameLabelData:
                return readDefineSceneAndFrameLabelData();
            case DefineFont:
                return readDefineFont();
            case DefineFontInfo:
                return readDefineFontInfo(type);
            case DefineFont2:
                return readDefineFont2();
            case DefineFont3:
                return readDefineFont3();
            case DefineFont4:
                return readDefineFont4();
            case DefineFontAlignZones:
                return readDefineFontAlignZones();
            case DefineFontName:
                return readFontName();
            case DefineText:
                return readDefineText(type);
            case DefineText2:
                return readDefineText(type);
            case DefineEditText:
                return readDefineEditText();
            case DefineButton:
                return readDefineButton();
            case DefineButton2:
                return readDefineButton2();
            case DefineButtonSound:
                return readDefineButtonSound();
            case DefineVideoStream:
                return readDefineVideoStream();
            case VideoFrame:
                return readVideoFrame();
            case End:
                return readEnd();
            case EnableDebugger2:
                return readEnableDebugger2();
            case ExportAssets:
                return readExportAssets();
            case FileAttributes:
                return readFileAttributes();
            case FrameLabel:
                return readFrameLabel();
            case JPEGTables:
                return readJPEGTables();
            case Metadata:
                return readMetadata();
            case ProductInfo:
                return readProductInfo();
            case PlaceObject:
                return readPlaceObject();
            case PlaceObject2:
                return readPlaceObject2();
            case PlaceObject3:
                return readPlaceObject3();
            case RemoveObject:
                return readRemoveObject();
            case RemoveObject2:
                return readRemoveObject2();
            case ScriptLimits:
                return readScriptLimits();
            case SetBackgroundColor:
                return readSetBackgroundColor();
            case SetTabIndex:
                return readSetTabIndex();
            case ShowFrame:
                return readShowFrame();
            case SymbolClass:
                return readSymbolClass();
            case EnableTelemetry:
                return readEnableTelemetry();
            default:
                return readRawTag(type);
        }
    }

    private ITag readSetTabIndex()
    {
        final SetTabIndexTag tag = new SetTabIndexTag();
        tag.setDepth(bitStream.readUI16());
        tag.setTabIndex(bitStream.readUI16());
        return tag;
    }

    private RemoveObject2Tag readRemoveObject2()
    {
        final RemoveObject2Tag tag = new RemoveObject2Tag();
        tag.setDepth(bitStream.readUI16());
        return tag;
    }

    private RemoveObjectTag readRemoveObject() throws MalformedTagException
    {
        final RemoveObjectTag tag = new RemoveObjectTag();
        tag.setCharacter(getTagById(bitStream.readUI16(), tag.getTagType()));
        tag.setDepth(bitStream.readUI16());
        return tag;
    }

    private PlaceObject3Tag readPlaceObject3() throws IOException, MalformedTagException
    {
        final PlaceObject3Tag tag = new PlaceObject3Tag();
        tag.setHasClipActions(bitStream.readBit());
        tag.setHasClipDepth(bitStream.readBit());
        tag.setHasName(bitStream.readBit());
        tag.setHasRatio(bitStream.readBit());
        tag.setHasColorTransform(bitStream.readBit());
        tag.setHasMatrix(bitStream.readBit());
        tag.setHasCharacter(bitStream.readBit());
        tag.setMove(bitStream.readBit());

        bitStream.readUB(3); // reserved;
        tag.setHasImage(bitStream.readBit());
        tag.setHasClassName(bitStream.readBit());
        tag.setHasCacheAsBitmap(bitStream.readBit());
        tag.setHasBlendMode(bitStream.readBit());
        tag.setHasFilterList(bitStream.readBit());

        tag.setDepth(bitStream.readUI16());
        if (tag.isHasClassName())
            tag.setClassName(bitStream.readString());
        if (tag.isHasCharacter())
            tag.setCharacter(getTagById(bitStream.readUI16(), tag.getTagType()));
        if (tag.isHasMatrix())
            tag.setMatrix(readMatrix());
        if (tag.isHasColorTransform())
            tag.setColorTransform(readColorTransformWithAlpha());
        if (tag.isHasRatio())
            tag.setRatio(bitStream.readUI16());
        if (tag.isHasName())
            tag.setName(bitStream.readString());
        if (tag.isHasClipDepth())
            tag.setClipDepth(bitStream.readUI16());
        if (tag.isHasFilterList())
        {
            final int count = bitStream.readUI8();
            final Filter[] filterList = new Filter[count];
            for (int i = 0; i < count; i++)
                filterList[i] = readFilter();
            tag.setSurfaceFilterList(filterList);
        }
        if (tag.isHasBlendMode())
            tag.setBlendMode(bitStream.readUI8());
        if (tag.isHasCacheAsBitmap())
            tag.setBitmapCache(bitStream.readUI8());

        ClipActions clipActions = new ClipActions();
        clipActions.data = bitStream.readToBoundary();
        tag.setClipActions(clipActions);
        return tag;
    }

    private PlaceObjectTag readPlaceObject() throws IOException, MalformedTagException
    {
        final PlaceObjectTag tag = new PlaceObjectTag();
        tag.setCharacter(getTagById(bitStream.readUI16(), tag.getTagType()));
        tag.setDepth(bitStream.readUI16());
        tag.setMatrix(readMatrix());
        if (bitStream.available() > 0)
            tag.setColorTransform(readColorTransform());
        return tag;
    }

    private CXForm readColorTransform()
    {
        bitStream.byteAlign();
        final CXForm cx = new CXForm();
        final boolean hasAddTerms = bitStream.readBit();
        final boolean hasMultTerms = bitStream.readBit();
        final int nbits = bitStream.readUB(4);

        if (hasAddTerms)
        {
            cx.setAddTerm(
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits));
        }

        if (hasMultTerms)
        {
            cx.setMultTerm(
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits),
                    bitStream.readSB(nbits));
        }

        return cx;
    }

    private ITag readVideoFrame() throws IOException, MalformedTagException
    {
        final int id = bitStream.readUI16();
        final ICharacterTag streamTag = getTagById(id, TagType.VideoFrame);
        assert streamTag.getTagType() == TagType.DefineVideoStream;
        final int frameNum = bitStream.readUI16();
        final byte[] videoData = bitStream.readToBoundary();

        final VideoFrameTag tag = new VideoFrameTag();
        tag.setStreamTag((DefineVideoStreamTag)streamTag);
        tag.setFrameNum(frameNum);
        tag.setVideoData(videoData);
        return tag;
    }

    private ITag readDefineVideoStream()
    {
        final int characterID = bitStream.readUI16();
        final int numFrames = bitStream.readUI16();
        final int width = bitStream.readUI16();
        final int height = bitStream.readUI16();
        bitStream.byteAlign();
        bitStream.readUB(4); // reserved
        final int deblocking = bitStream.readUB(3);
        final boolean smoothing = bitStream.readBit();
        final int codecID = bitStream.readUI8();

        final DefineVideoStreamTag tag = new DefineVideoStreamTag();
        tag.setCharacterID(characterID);
        tag.setNumFrames(numFrames);
        tag.setWidth(width);
        tag.setHeight(height);
        tag.setDeblocking(deblocking);
        tag.setSmoothing(smoothing);
        tag.setCodecID(codecID);
        return tag;
    }

    private DefineButtonSoundTag readDefineButtonSound() throws MalformedTagException
    {
        final int buttonID = bitStream.readUI16();
        final DefineButtonSoundTag tag = new DefineButtonSoundTag();
        tag.setButtonTag(getTagById(buttonID, tag.getTagType()));
        for (int i = 0; i < DefineButtonSoundTag.TOTAL_SOUND_STYLE; i++)
        {
            final int soundID = bitStream.readUI16();
            if (soundID == 0)
                continue;
            final ICharacterTag soundTag = getTagById(soundID, tag.getTagType());
            assert soundTag instanceof DefineSoundTag;
            tag.getSoundChar()[i] = (DefineSoundTag)soundTag;
            tag.getSoundInfo()[i] = readSoundInfo();
        }
        return tag;
    }

    private DefineButton2Tag readDefineButton2() throws IOException
    {
        final int buttonID = bitStream.readUI16();
        bitStream.byteAlign();
        bitStream.readUB(7); // reserved;
        final boolean trackAsMenu = bitStream.readBit();
        final int actionOffset = bitStream.readUI16();
        final ButtonRecord[] characters = readButtonRecords(TagType.DefineButton2);
        final byte[] actions = bitStream.readToBoundary();

        final DefineButton2Tag tag = new DefineButton2Tag();
        tag.setTrackAsMenu(trackAsMenu);
        tag.setActionOffset(actionOffset);
        tag.setCharacterID(buttonID);
        tag.setCharacters(characters);
        tag.setActions(actions);
        return tag;
    }

    private DefineButtonTag readDefineButton() throws IOException
    {
        final int buttonID = bitStream.readUI16();
        final ButtonRecord[] characters = readButtonRecords(TagType.DefineButton);

        final byte[] actionsWithEndFlag = bitStream.readToBoundary();
        final int actionSize = actionsWithEndFlag.length - 1;
        final byte[] actions = new byte[actionSize];
        System.arraycopy(actionsWithEndFlag, 0, actions, 0, actionSize);

        final DefineButtonTag tag = new DefineButtonTag();
        tag.setCharacterID(buttonID);
        tag.setCharacters(characters);
        tag.setActions(actions);
        return tag;
    }

    private ButtonRecord[] readButtonRecords(final TagType type)
    {
        final ArrayList<ButtonRecord> characters = new ArrayList<ButtonRecord>(6);
        // loop until CharacterEndFlag (0x00) is read
        while (true)
        {
            final int firstByte = bitStream.readUI8();
            if (firstByte == 0)
                break;
            final ButtonRecord record = new ButtonRecord();
            record.setHasBlendMode((firstByte & 0x20) > 0);
            record.setHasFilterList((firstByte & 0x10) > 0);
            record.setStateHitTest((firstByte & 0x08) > 0);
            record.setStateDown((firstByte & 0x04) > 0);
            record.setStateOver((firstByte & 0x02) > 0);
            record.setStateUp((firstByte & 0x01) > 0);
            record.setCharacterID(bitStream.readUI16());
            record.setPlaceDepth(bitStream.readUI16());
            record.setPlaceMatrix(readMatrix());
            if (type == TagType.DefineButton2)
            {
                record.setColorTransform(readColorTransformWithAlpha());

                if (record.isHasFilterList())
                {
                    final int count = bitStream.readUI8();
                    final Filter[] filterList = new Filter[count];
                    for (int i = 0; i < count; i++)
                        filterList[i] = readFilter();
                    record.setFilterList(filterList);
                }

                if (record.isHasBlendMode())
                    record.setBlendMode(bitStream.readUI8());
            }
            characters.add(record);
        }
        return characters.toArray(new ButtonRecord[characters.size()]);
    }

    private Filter readFilter()
    {
        final Filter filter = new Filter();
        final int type = bitStream.readUI8();
        filter.setFilterID(type);

        switch (type)
        {
            case Filter.DROP_SHADOW:
                filter.setDropShadowFilter(readDropShadowFilter());
                break;
            case Filter.BLUR:
                filter.setBlurFilter(readBlurFilter());
                break;
            case Filter.GLOW:
                filter.setGlowFilter(readGlowFilter());
                break;
            case Filter.BEVEL:
                filter.setBevelFilter(readBevelFilter());
                break;
            case Filter.GRADIENT_GLOW:
                filter.setGradientGlowFilter(readGradientGlowFilter());
                break;
            case Filter.CONVOLUTION:
                filter.setConvolutionFilter(readConvolutionFilter());
                break;
            case Filter.COLOR_MATRIX:
                filter.setColorMatrixFilter(readColorMatrixFilter());
                break;
            case Filter.GRADIENT_BEVEL:
                filter.setGradientBevelFilter(readGradientBevelFilter());
                break;
        }

        return filter;
    }

    private GradientBevelFilter readGradientBevelFilter()
    {
        final GradientBevelFilter filter = new GradientBevelFilter();
        final short numColors = bitStream.readUI8();

        final RGBA[] gradientColors = new RGBA[numColors];
        final int[] gradientRatio = new int[numColors];
        for (short i = 0; i < numColors; i++)
        {
            gradientColors[i] = readRGBA();
            gradientRatio[i] = bitStream.readUI8();
        }

        filter.setNumColors(numColors);
        filter.setGradientColors(gradientColors);
        filter.setGradientRatio(gradientRatio);
        filter.setBlurX(bitStream.readFIXED());
        filter.setBlurY(bitStream.readFIXED());
        filter.setAngle(bitStream.readFIXED());
        filter.setDistance(bitStream.readFIXED());
        filter.setStrength(bitStream.readFIXED8());
        filter.setInnerShadow(bitStream.readBit());
        filter.setKnockout(bitStream.readBit());
        filter.setCompositeSource(bitStream.readBit());
        filter.setPasses(bitStream.readUB(4));
        return filter;
    }

    private float[] readColorMatrixFilter()
    {
        final float[] result = new float[20];
        for (int i = 0; i < 20; i++)
            result[i] = bitStream.readFLOAT();
        return result;
    }

    private ConvolutionFilter readConvolutionFilter()
    {
        final ConvolutionFilter filter = new ConvolutionFilter();
        filter.setMatrixX(bitStream.readUI8());
        filter.setMatrixY(bitStream.readUI8());
        filter.setDivisor(bitStream.readFLOAT());
        filter.setBias(bitStream.readFLOAT());

        int length = filter.getMatrixX() * filter.getMatrixY();
        final float[] matrix = new float[length];
        for (int i = 0; i < length; i++)
            matrix[i] = bitStream.readFLOAT();
        filter.setMatrix(matrix);

        filter.setDefaultColor(readRGBA());
        bitStream.byteAlign();
        bitStream.readUB(6); // reserved
        filter.setClamp(bitStream.readBit());
        filter.setPreserveAlpha(bitStream.readBit());
        return filter;
    }

    private GradientGlowFilter readGradientGlowFilter()
    {
        final GradientGlowFilter filter = new GradientGlowFilter();
        final short numColors = bitStream.readUI8();

        final RGBA[] gradientColors = new RGBA[numColors];
        final int[] gradientRatio = new int[numColors];
        for (short i = 0; i < numColors; i++)
        {
            gradientColors[i] = readRGBA();
            gradientRatio[i] = bitStream.readUI8();
        }

        filter.setNumColors(numColors);
        filter.setGradientColors(gradientColors);
        filter.setGradientRatio(gradientRatio);
        filter.setBlurX(bitStream.readFIXED());
        filter.setBlurY(bitStream.readFIXED());
        filter.setAngle(bitStream.readFIXED());
        filter.setDistance(bitStream.readFIXED());
        filter.setStrength(bitStream.readFIXED8());
        filter.setInnerGlow(bitStream.readBit());
        filter.setKnockout(bitStream.readBit());
        filter.setCompositeSource(bitStream.readBit());
        filter.setPasses(bitStream.readUB(4));
        return filter;
    }

    private BevelFilter readBevelFilter()
    {
        final BevelFilter filter = new BevelFilter();
        filter.setShadowColor(readRGBA());
        filter.setHighlightColor(readRGBA());
        filter.setBlurX(bitStream.readFIXED());
        filter.setBlurY(bitStream.readFIXED());
        filter.setAngle(bitStream.readFIXED());
        filter.setDistance(bitStream.readFIXED());
        filter.setStrength(bitStream.readFIXED8());
        filter.setInnerShadow(bitStream.readBit());
        filter.setKnockout(bitStream.readBit());
        filter.setCompositeSource(bitStream.readBit());
        filter.setOnTop(bitStream.readBit());
        filter.setPasses(bitStream.readUB(4));
        return filter;
    }

    private GlowFilter readGlowFilter()
    {
        final GlowFilter filter = new GlowFilter();
        filter.setGlowColor(readRGBA());
        filter.setBlurX(bitStream.readFIXED());
        filter.setBlurY(bitStream.readFIXED());
        filter.setStrength(bitStream.readFIXED8());
        filter.setInnerGlow(bitStream.readBit());
        filter.setKnockout(bitStream.readBit());
        filter.setCompositeSource(bitStream.readBit());
        filter.setPasses(bitStream.readUB(5));
        return filter;
    }

    private BlurFilter readBlurFilter()
    {
        final BlurFilter filter = new BlurFilter();
        filter.setBlurX(bitStream.readFIXED());
        filter.setBlurY(bitStream.readFIXED());
        filter.setPasses(bitStream.readUB(5));
        bitStream.readUB(3); // reserved
        return filter;
    }

    private DropShadowFilter readDropShadowFilter()
    {
        final DropShadowFilter filter = new DropShadowFilter();
        filter.setDropShadowColor(readRGBA());
        filter.setBlurX(bitStream.readFIXED());
        filter.setBlurY(bitStream.readFIXED());
        filter.setAngle(bitStream.readFIXED());
        filter.setDistance(bitStream.readFIXED());
        filter.setStrength(bitStream.readFIXED8());
        filter.setInnerShadow(bitStream.readBit());
        filter.setKnockout(bitStream.readBit());
        filter.setCompositeSource(bitStream.readBit());
        filter.setPasses(bitStream.readUB(5));
        return filter;
    }

    private SoundStreamBlockTag readSoundStreamBlock() throws IOException
    {
        final byte streamSoundData[] = bitStream.readToBoundary();

        final SoundStreamBlockTag tag = new SoundStreamBlockTag();
        tag.setStreamSoundData(streamSoundData);
        return tag;
    }

    private SoundStreamHeadTag readSoundStreamHead(final TagType tagType)
    {
        bitStream.byteAlign();
        bitStream.readUB(4); // reserved
        final int playbackSoundRate = bitStream.readUB(2);
        final int playbackSoundSize = bitStream.readUB(1);
        final int playbackSoundType = bitStream.readUB(1);
        final int streamSoundCompression = bitStream.readUB(4);
        final int streamSoundRate = bitStream.readUB(2);
        final int streamSoundSize = bitStream.readUB(1);
        final int streamSoundType = bitStream.readUB(1);
        final int streamSoundSampleCount = bitStream.readUI16();
        final int latencySeek = streamSoundCompression == SoundStreamHeadTag.SSC_MP3 ? bitStream.readSI16() : 0;

        final SoundStreamHeadTag tag =
                (tagType == TagType.SoundStreamHead) ?
                        new SoundStreamHeadTag() :
                        new SoundStreamHead2Tag();
        tag.setPlaybackSoundRate(playbackSoundRate);
        tag.setPlaybackSoundSize(playbackSoundSize);
        tag.setPlaybackSoundType(playbackSoundType);
        tag.setStreamSoundCompression(streamSoundCompression);
        tag.setStreamSoundRate(streamSoundRate);
        tag.setStreamSoundSize(streamSoundSize);
        tag.setStreamSoundType(streamSoundType);
        tag.setStreamSoundSampleCount(streamSoundSampleCount);
        tag.setLatencySeek(latencySeek);
        return tag;
    }

    private StartSoundTag readStartSound() throws MalformedTagException
    {
        final int soundId = bitStream.readUI16();
        final SoundInfo soundInfo = readSoundInfo();

        final StartSoundTag tag = new StartSoundTag();
        tag.setSoundTag(getTagById(soundId, tag.getTagType()));
        tag.setSoundInfo(soundInfo);
        return tag;
    }

    private StartSound2Tag readStartSound2()
    {
        final String soundClassName = bitStream.readString();
        final SoundInfo soundInfo = readSoundInfo();

        final StartSound2Tag tag = new StartSound2Tag();
        tag.setSoundClassName(soundClassName);
        tag.setSoundInfo(soundInfo);
        return tag;
    }

    private SoundInfo readSoundInfo()
    {
        bitStream.byteAlign();
        bitStream.readUB(2); // reserved
        final boolean syncStop = bitStream.readBit();
        final boolean syncNoMultiple = bitStream.readBit();
        final boolean hasEnvelope = bitStream.readBit();
        final boolean hasLoops = bitStream.readBit();
        final boolean hasOutPoint = bitStream.readBit();
        final boolean hasInPoint = bitStream.readBit();
        final long inPoint = hasInPoint ? bitStream.readUI32() : 0;
        final long outPoint = hasOutPoint ? bitStream.readUI32() : 0;
        final int loopCount = hasLoops ? bitStream.readUI16() : 0;
        final int envPoints = hasEnvelope ? bitStream.readUI8() : 0;
        final SoundEnvelope envelopeRecords[] = new SoundEnvelope[envPoints];
        for (int i = 0; i < envPoints; i++)
        {
            envelopeRecords[i] = new SoundEnvelope();
            envelopeRecords[i].setPos44(bitStream.readUI32());
            envelopeRecords[i].setLeftLevel(bitStream.readUI16());
            envelopeRecords[i].setRightLevel(bitStream.readUI16());
        }

        final SoundInfo soundInfo = new SoundInfo();
        soundInfo.setSyncStop(syncStop);
        soundInfo.setSyncNoMultiple(syncNoMultiple);
        soundInfo.setHasEnvelope(hasEnvelope);
        soundInfo.setHasLoops(hasLoops);
        soundInfo.setHasOutPoint(hasOutPoint);
        soundInfo.setHasInPoint(hasInPoint);
        soundInfo.setInPoint(inPoint);
        soundInfo.setOutPoint(outPoint);
        soundInfo.setLoopCount(loopCount);
        soundInfo.setEnvPoints(envPoints);
        soundInfo.setEnvelopeRecords(envelopeRecords);
        return soundInfo;
    }

    private ITag readDefineSound() throws IOException
    {
        bitStream.byteAlign();
        final int soundId = bitStream.readUI16();
        final int soundFormat = bitStream.readUB(4);
        final int soundRate = bitStream.readUB(2);
        final int soundSize = bitStream.readUB(1);
        final int soundType = bitStream.readUB(1);
        final long soundSampleCount = bitStream.readUI32();
        final byte soundData[] = bitStream.readToBoundary();

        final DefineSoundTag tag = new DefineSoundTag();
        tag.setCharacterID(soundId);
        tag.setSoundFormat(soundFormat);
        tag.setSoundRate(soundRate);
        tag.setSoundSize(soundSize);
        tag.setSoundType(soundType);
        tag.setSoundSampleCount(soundSampleCount);
        tag.setSoundData(soundData);
        return tag;
    }

    private DefineFont4Tag readDefineFont4() throws IOException
    {
        final DefineFont4Tag tag = new DefineFont4Tag();
        tag.setCharacterID(bitStream.readUI16());

        bitStream.byteAlign();
        bitStream.readUB(5); // reserved
        tag.setFontFlagsHasFontData(bitStream.readBit());
        tag.setFontFlagsItalic(bitStream.readBit());
        tag.setFontFlagsBold(bitStream.readBit());
        // 8 bits - no need to align

        tag.setFontName(bitStream.readString());
        tag.setFontData(bitStream.readToBoundary());

        return tag;
    }

    private CSMTextSettingsTag readCSMTextSettings() throws MalformedTagException
    {
        final int id = bitStream.readUI16();
        final CSMTextSettingsTag tag = new CSMTextSettingsTag();
        final ICharacterTag textTag = getTagById(id, tag.getTagType());

        tag.setTextTag(textTag);
        bitStream.byteAlign();
        tag.setUseFlashType(bitStream.readUB(2));
        tag.setGridFit(bitStream.readUB(3));
        bitStream.readUB(3); // reserved
        // 8 bits - no need to align
        tag.setThickness(bitStream.readFLOAT());
        tag.setSharpness(bitStream.readFLOAT());
        bitStream.readUI8(); // reserved

        if (textTag instanceof DefineTextTag)
            ((DefineTextTag)textTag).setCSMTextSettings(tag);
        else if (textTag instanceof DefineEditTextTag)
            ((DefineEditTextTag)textTag).setCSMTextSettings(tag);
        else
            problems.add(new SWFCSMTextSettingsWrongReferenceTypeProblem(swfPath, id));

        return tag;
    }

    private DefineEditTextTag readDefineEditText() throws MalformedTagException
    {
        final DefineEditTextTag tag = new DefineEditTextTag();
        tag.setCharacterID(bitStream.readUI16());
        tag.setBounds(readRect());

        bitStream.byteAlign();
        tag.setHasText(bitStream.readBit());
        tag.setWordWrap(bitStream.readBit());
        tag.setMultiline(bitStream.readBit());
        tag.setPassword(bitStream.readBit());
        tag.setReadOnly(bitStream.readBit());
        tag.setHasTextColor(bitStream.readBit());
        tag.setHasMaxLength(bitStream.readBit());
        tag.setHasFont(bitStream.readBit());
        tag.setHasFontclass(bitStream.readBit());
        tag.setAutoSize(bitStream.readBit());
        tag.setHasLayout(bitStream.readBit());
        tag.setNoSelect(bitStream.readBit());
        tag.setBorder(bitStream.readBit());
        tag.setWasStatic(bitStream.readBit());
        tag.setHtml(bitStream.readBit());
        tag.setUseOutlines(bitStream.readBit());

        // HasFont and HasFontClass is exclusive, but we tolerate the situation where both 
        // are set.
        if (tag.isHasFont())
        {
            final int id = bitStream.readUI16();
            final ICharacterTag fontTag = getTagById(id, tag.getTagType());
            tag.setFontTag(fontTag);
            tag.setFontHeight(bitStream.readUI16());
        }

        if (tag.isHasFontClass())
        {
            tag.setFontClass(bitStream.readString());
            // HasFontClass needs a Height field as well.
            tag.setFontHeight(bitStream.readUI16());
        }

        if (tag.isHasTextColor())
        {
            tag.setTextColor(readRGBA());
        }

        if (tag.isHasMaxLength())
        {
            tag.setMaxLength(bitStream.readUI16());
        }

        if (tag.isHasLayout())
        {
            tag.setAlign(bitStream.readUI8());
            tag.setLeftMargin(bitStream.readUI16());
            tag.setRightMargin(bitStream.readUI16());
            tag.setIndent(bitStream.readUI16());
            tag.setLeading(bitStream.readSI16());
        }

        tag.setVariableName(bitStream.readString());

        if (tag.isHasText())
        {
            tag.setInitialText(bitStream.readString());
        }

        return tag;
    }

    private DefineTextTag readDefineText(TagType tagType) throws IOException, MalformedTagException
    {
        assert tagType == TagType.DefineText || tagType == TagType.DefineText2;

        final int characterId = bitStream.readUI16();
        final Rect textBounds = readRect();
        final Matrix textMatrix = readMatrix();
        final int glyphBits = bitStream.readUI8();
        final int advanceBits = bitStream.readUI8();
        final ArrayList<TextRecord> textRecords = new ArrayList<TextRecord>();

        while (true)
        {
            final TextRecord textRecord = readTextRecord(tagType, glyphBits, advanceBits);
            if (textRecord == null)
                break;
            textRecords.add(textRecord);
        }

        final DefineTextTag tag = new DefineTextTag();
        tag.setCharacterID(characterId);
        tag.setTextBounds(textBounds);
        tag.setTextMatrix(textMatrix);
        tag.setGlyphBits(glyphBits);
        tag.setAdvanceBits(advanceBits);
        tag.setTextRecords(textRecords.toArray(new TextRecord[0]));
        return tag;
    }

    private TextRecord readTextRecord(TagType type, int glyphBits, int advanceBits) throws MalformedTagException
    {
        bitStream.byteAlign();
        final boolean textRecordType = bitStream.readBit();
        if (!textRecordType)
            return null;

        bitStream.readUB(3); // reserved

        final TextRecord textRecord = new TextRecord();
        textRecord.setStyleFlagsHasFont(bitStream.readBit());
        textRecord.setStyleFlagsHasColor(bitStream.readBit());
        textRecord.setStyleFlagsHasYOffset(bitStream.readBit());
        textRecord.setStyleFlagsHasXOffset(bitStream.readBit());

        if (textRecord.isStyleFlagsHasFont())
        {
            final int fontId = bitStream.readUI16();
            final ICharacterTag fontTag = getTagById(fontId, type);
            textRecord.setFontTag(fontTag);
        }

        if (textRecord.isStyleFlagsHasColor())
        {
            if (type == TagType.DefineText2)
            {
                textRecord.setTextColor(readRGBA());
            }
            else
            {
                textRecord.setTextColor(readRGB());
            }
        }

        if (textRecord.isStyleFlagsHasXOffset())
        {
            textRecord.setxOffset(bitStream.readSI16());
        }

        if (textRecord.isStyleFlagsHasYOffset())
        {
            textRecord.setyOffset(bitStream.readSI16());
        }

        if (textRecord.isStyleFlagsHasFont())
        {
            textRecord.setTextHeight(bitStream.readUI16());
        }

        textRecord.setGlyphCount(bitStream.readUI8());

        final GlyphEntry[] glyphEntries = new GlyphEntry[textRecord.getGlyphCount()];
        for (int i = 0; i < textRecord.getGlyphCount(); i++)
        {
            glyphEntries[i] = readGlyphEntry(glyphBits, advanceBits);
        }
        textRecord.setGlyphEntries(glyphEntries);

        return textRecord;
    }

    private GlyphEntry readGlyphEntry(int glyphBits, int advanceBits)
    {
        final GlyphEntry entry = new GlyphEntry();
        entry.setGlyphIndex(bitStream.readUB(glyphBits));
        entry.setGlyphAdvance(bitStream.readSB(advanceBits));
        return entry;
    }

    private DefineFontNameTag readFontName() throws MalformedTagException
    {
        final int fontId = bitStream.readUI16();
        final DefineFontNameTag tag = new DefineFontNameTag();
        final ICharacterTag character = getTagById(fontId, tag.getTagType());
        final String fontName = bitStream.readString();
        final String fontCopyright = bitStream.readString();

        tag.setFontTag(character);
        tag.setFontName(fontName);
        tag.setFontCopyright(fontCopyright);

        ((IDefineFontTag)character).setLicense(tag);

        return tag;
    }

    /**
     * @return a valid tag.
     * @throws MalformedTagException
     * @throws RuntimeException if the record is invalid.
     */
    private DefineFontAlignZonesTag readDefineFontAlignZones() throws MalformedTagException
    {
        final int fontId = bitStream.readUI16();
        final DefineFontAlignZonesTag tag = new DefineFontAlignZonesTag();
        final ICharacterTag character = getTagById(fontId, tag.getTagType());
        if (character instanceof DefineFont3Tag)
        {
            final DefineFont3Tag fontTag = (DefineFont3Tag)character;
            bitStream.byteAlign();
            final int csmTableHint = bitStream.readUB(2);
            bitStream.byteAlign(); // skip reserved

            final ZoneRecord[] zoneTable = new ZoneRecord[fontTag.getNumGlyphs()];
            for (int i = 0; i < fontTag.getNumGlyphs(); i++)
            {
                zoneTable[i] = readZoneRecord();
            }

            tag.setFontTag(fontTag);
            tag.setCsmTableHint(csmTableHint);
            tag.setZoneTable(zoneTable);

            fontTag.setZones(tag);

            return tag;
        }
        else
        {
            problems.add(new SWFDefineFontAlignZonesLinkToIncorrectFontProblem(fontId,
                    swfPath, bitStream.getOffset()));
            throw new MalformedTagException();
        }
    }

    /**
     * @return
     */
    private ZoneRecord readZoneRecord()
    {
        final int numZoneData = bitStream.readUI8();
        assert 2 == numZoneData;

        final ZoneData zoneData0 = readZoneData();
        final ZoneData zoneData1 = readZoneData();

        bitStream.byteAlign();
        bitStream.readUB(6); // reserved
        final boolean zoneMaskY = bitStream.readBit();
        final boolean zoneMaskX = bitStream.readBit();

        final ZoneRecord zoneRecord = new ZoneRecord();
        zoneRecord.setZoneData0(zoneData0);
        zoneRecord.setZoneData1(zoneData1);
        zoneRecord.setZoneMaskY(zoneMaskY);
        zoneRecord.setZoneMaskX(zoneMaskX);
        return zoneRecord;
    }

    private ZoneData readZoneData()
    {
        final ZoneData zoneData = new ZoneData();
        zoneData.setData(bitStream.readUI32());
        return zoneData;
    }

    private DefineFont3Tag readDefineFont3() throws IOException, MalformedTagException
    {
        final DefineFont3Tag tag = new DefineFont3Tag();
        readDefineFont2And3(tag);
        return tag;
    }

    /**
     * @throws MalformedTagException
     * @see SWFWriter#writeDefineFont2
     */
    private DefineFont2Tag readDefineFont2() throws IOException, MalformedTagException
    {
        final DefineFont2Tag tag = new DefineFont2Tag();
        readDefineFont2And3(tag);
        return tag;
    }

    private void readDefineFont2And3(DefineFont2Tag tag) throws IOException, MalformedTagException
    {
        // reading
        final int fontId = bitStream.readUI16();
        bitStream.byteAlign();
        final boolean fontFlagsHasLayout = bitStream.readBit();
        final boolean fontFlagsShiftJIS = bitStream.readBit();
        final boolean fontFlagsSmallText = bitStream.readBit();
        final boolean fontFlagsANSI = bitStream.readBit();
        final boolean fontFlagsWideOffsets = bitStream.readBit();
        final boolean fontFlagsWideCodes = bitStream.readBit();
        final boolean fontFlagsItalic = bitStream.readBit();
        final boolean fontFlagsBold = bitStream.readBit();
        final int languageCode = bitStream.readUI8();
        final String fontName = readLengthString();
        final int numGlyphs = bitStream.readUI16();

        // read offset table
        final long[] offsetTable = new long[numGlyphs];
        for (int i = 0; i < numGlyphs; i++)
        {
            offsetTable[i] = fontFlagsWideOffsets ? bitStream.readUI32()
                                                  : bitStream.readUI16();
        }

        // Only read the CodeTableOffset if numGlyphs > 0
        long codeTableOffset = 0;
        if (numGlyphs > 0)
        {
            codeTableOffset = fontFlagsWideOffsets ? bitStream.readUI32()
                                                   : bitStream.readUI16();
        }

        final Shape[] glyphShapeTable = new Shape[numGlyphs];
        for (int i = 0; i < numGlyphs; i++)
        {
            glyphShapeTable[i] = readShape(tag.getTagType());
        }

        // read code table
        final int[] codeTable = new int[numGlyphs];
        for (int i = 0; i < numGlyphs; i++)
        {
            codeTable[i] = fontFlagsWideCodes ? bitStream.readUI16()
                                              : bitStream.readUI8();
        }
        int fontAscent = 0;
        int fontDescent = 0;
        int fontLeading = 0;
        int[] fontAdvanceTable = null;
        Rect[] fontBoundsTable = null;
        int kerningCount = 0;
        KerningRecord[] fontKerningTable = null;

        if (fontFlagsHasLayout)
        {
            fontAscent = bitStream.readSI16();
            fontDescent = bitStream.readSI16();
            fontLeading = bitStream.readSI16();

            fontAdvanceTable = new int[numGlyphs];
            for (int i = 0; i < numGlyphs; i++)
            {
                fontAdvanceTable[i] = bitStream.readSI16();
            }

            fontBoundsTable = new Rect[numGlyphs];
            for (int i = 0; i < numGlyphs; i++)
            {
                fontBoundsTable[i] = readRect();
            }

            kerningCount = bitStream.readUI16();
            fontKerningTable = new KerningRecord[kerningCount];
            for (int i = 0; i < kerningCount; i++)
            {
                fontKerningTable[i] = readKerningRecord(fontFlagsWideCodes);
            }
        }

        // construct tag
        tag.setCharacterID(fontId);
        tag.setFontFlagsHasLayout(fontFlagsHasLayout);
        tag.setFontFlagsShiftJIS(fontFlagsShiftJIS);
        tag.setFontFlagsSmallText(fontFlagsSmallText);
        tag.setFontFlagsANSI(fontFlagsANSI);
        tag.setFontFlagsWideOffsets(fontFlagsWideOffsets);
        tag.setFontFlagsWideCodes(fontFlagsWideCodes);
        tag.setFontFlagsItalic(fontFlagsItalic);
        tag.setFontFlagsBold(fontFlagsBold);
        tag.setLanguageCode(languageCode);
        tag.setFontName(fontName);
        tag.setNumGlyphs(numGlyphs);
        tag.setOffsetTable(offsetTable);
        tag.setCodeTableOffset(codeTableOffset);
        tag.setGlyphShapeTable(glyphShapeTable);
        tag.setCodeTable(codeTable);
        tag.setFontAscent(fontAscent);
        tag.setFontDescent(fontDescent);
        tag.setFontLeading(fontLeading);
        tag.setFontAdvanceTable(fontAdvanceTable);
        tag.setFontBoundsTable(fontBoundsTable);
        tag.setKerningCount(kerningCount);
        tag.setFontKerningTable(fontKerningTable);
    }

    /**
     * @see SWFWriter#writeKerningRecord
     */
    private KerningRecord readKerningRecord(boolean fontFlagsWideCodes)
    {
        final int code1 = fontFlagsWideCodes ? bitStream.readUI16() : bitStream.readUI8();
        final int code2 = fontFlagsWideCodes ? bitStream.readUI16() : bitStream.readUI8();
        final int adjustment = bitStream.readSI16();
        final KerningRecord rec = new KerningRecord();
        rec.setCode1(code1);
        rec.setCode2(code2);
        rec.setAdjustment(adjustment);
        return rec;
    }

    private ITag readDefineFontInfo(TagType type) throws IOException, MalformedTagException
    {
        assert type == TagType.DefineFontInfo || type == TagType.DefineFontInfo2 : "unknown tag type in readDefineFontInfo";

        final int fontId = bitStream.readUI16();
        final ICharacterTag fontTag = getTagById(fontId, type);
        final String fontName = readLengthString();
        final int reserved = bitStream.readUB(2);
        final boolean smallText = bitStream.readBit();
        final boolean shiftJIS = bitStream.readBit();
        final boolean ansi = bitStream.readBit();
        final boolean italic = bitStream.readBit();
        final boolean bold = bitStream.readBit();
        final boolean wideCodes = bitStream.readBit();
        int langCode = 0;
        if (type == TagType.DefineFontInfo2)
            langCode = bitStream.readUI8();

        final byte[] codeTableRaw = bitStream.readToBoundary();
        final int numGlyphs = codeTableRaw.length / (wideCodes ? 2 : 1);
        final int[] codeTable = new int[numGlyphs];
        final IInputBitStream codeTableStream = new InputBitStream(codeTableRaw);
        codeTableStream.setReadBoundary(codeTableRaw.length);
        for (int i = 0; i < numGlyphs; i++)
        {
            codeTable[i] = wideCodes ? codeTableStream.readUI16()
                                     : codeTableStream.readUI8();
        }
        codeTableStream.close();

        DefineFontInfoTag tag = null;
        if (type == TagType.DefineFontInfo)
            tag = new DefineFontInfoTag();
        else
            tag = new DefineFontInfo2Tag();

        tag.setFontTag(fontTag);
        tag.setFontName(fontName);
        tag.setFontFlagsReserved(reserved);
        tag.setFontFlagsSmallText(smallText);
        tag.setFontFlagsShiftJIS(shiftJIS);
        tag.setFontFlagsANSI(ansi);
        tag.setFontFlagsItalic(italic);
        tag.setFontFlagsBold(bold);
        tag.setFontFlagsWideCodes(wideCodes);
        if (type == TagType.DefineFontInfo2)
            ((DefineFontInfo2Tag)tag).setLanguageCode(langCode);
        tag.setCodeTable(codeTable);

        return tag;
    }

    /**
     * The OffsetTable and GlyphShapeTable are used together. These tables have
     * the same number of entries, and there is a one-to-one ordering match
     * between the order of the offsets and the order of the shapes. The
     * OffsetTable points to locations in the GlyphShapeTable. Each offset entry
     * stores the difference (in bytes) between the start of the offset table
     * and the location of the corresponding shape. Because the GlyphShapeTable
     * immediately follows the OffsetTable, the number of entries in each table
     * (the number of glyphs in the font) can be inferred by dividing the first
     * entry in the OffsetTable by two.
     * 
     * @throws MalformedTagException
     * @see SWFWriter#writeDefineFont
     */
    private ITag readDefineFont() throws IOException, MalformedTagException
    {
        final int id = bitStream.readUI16();
        final int firstGlyphShapeOffset = bitStream.readUI16();
        final int numGlyphs = firstGlyphShapeOffset / 2;
        final DefineFontTag tag = new DefineFontTag();
        tag.setCharacterID(id);

        final long[] offsetTable = new long[numGlyphs];
        offsetTable[0] = firstGlyphShapeOffset;
        for (int i = 1; i < numGlyphs; i++)
        {
            offsetTable[i] = bitStream.readUI16();
        }
        tag.setOffsetTable(offsetTable);

        final Shape[] glyphShapeTable = new Shape[numGlyphs];
        for (int i = 0; i < numGlyphs; i++)
        {
            glyphShapeTable[i] = readShape(tag.getTagType());
        }
        tag.setGlyphShapeTable(glyphShapeTable);

        return tag;
    }

    /**
     * @see SWFWriter#writeDefineBitsJPEG3
     */
    private ITag readDefineBitsJPEG3() throws IOException
    {
        final int id = bitStream.readUI16();
        final int alphaDataOffset = (int)bitStream.readUI32();
        final byte[] imageData = bitStream.read(alphaDataOffset);
        final byte[] bitmapAlphaData = bitStream.readToBoundary();

        final DefineBitsJPEG3Tag tag = new DefineBitsJPEG3Tag();
        tag.setCharacterID(id);
        tag.setAlphaDataOffset(alphaDataOffset);
        tag.setImageData(imageData);
        tag.setBitmapAlphaData(bitmapAlphaData);
        return tag;
    }

    /**
     * @see SWFWriter#writeDefineBitsJPEG2
     */
    private ITag readDefineBitsJPEG2() throws IOException
    {
        final DefineBitsJPEG2Tag tag = new DefineBitsJPEG2Tag();
        tag.setCharacterID(bitStream.readUI16());
        tag.setImageData(bitStream.readToBoundary());
        return tag;
    }

    /**
     * @see SWFWriter#writeJPEGTables
     */
    private ITag readJPEGTables() throws IOException
    {
        final JPEGTablesTag tag = new JPEGTablesTag();
        tag.setJpegData(bitStream.readToBoundary());
        return tag;
    }

    /**
     * @see SWFWriter#writeDefineBits
     */
    private ITag readDefineBits() throws IOException
    {
        final DefineBitsTag tag = new DefineBitsTag();
        tag.setCharacterID(bitStream.readUI16());
        tag.setImageData(bitStream.readToBoundary());
        return tag;
    }

    private String readLengthString() throws IOException
    {
        int length = bitStream.readUI8();
        byte[] b = new byte[length];
        bitStream.read(b);

        // [paul] Flash Authoring and the player null terminate the
        // string, so ignore the last byte when constructing the String.
        if (swf.getVersion() >= 6)
        {
            return new String(b, 0, length - 1, "UTF8");
        }
        else
        {
            // use platform encoding
            return new String(b, 0, length - 1);
        }
    }

}
