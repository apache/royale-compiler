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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.output.CountingOutputStream;

import org.apache.royale.swf.Header.Compression;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.tags.CharacterTag;
import org.apache.royale.swf.tags.DefineBinaryDataTag;
import org.apache.royale.swf.tags.DefineFont2Tag;
import org.apache.royale.swf.tags.DefineFont4Tag;
import org.apache.royale.swf.tags.DefineFontNameTag;
import org.apache.royale.swf.tags.DefineFontTag;
import org.apache.royale.swf.tags.DefineShapeTag;
import org.apache.royale.swf.tags.DefineSoundTag;
import org.apache.royale.swf.tags.DefineSpriteTag;
import org.apache.royale.swf.tags.DefineVideoStreamTag;
import org.apache.royale.swf.tags.DoABCTag;
import org.apache.royale.swf.tags.FileAttributesTag;
import org.apache.royale.swf.tags.FrameLabelTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.SymbolClassTag;

/**
 * A class that writes a SWF and a size report. The path of the size report is
 * passed to the constructor. When the SWF is written to disk, the size report
 * is also written.
 */
public class SizeReportWritingSWFWriter extends SWFWriter
{
    /**
     * SWF writer factory for SWF writers that also create size reports.
     */
    private static class SWFWriterFactory implements ISWFWriterFactory
    {

        SWFWriterFactory(File sizeReport)
        {
            this.sizeReport = sizeReport;
        }

        private final File sizeReport;

        @Override
        public ISWFWriter createSWFWriter(ISWF swf, Compression useCompression,
                boolean enableDebug, boolean enableTelemetry)
        {
            return new SizeReportWritingSWFWriter(swf, useCompression, enableDebug,
                    enableTelemetry, sizeReport);
        }

    }

    /**
     * Get a SWF Writer factory that can create an instance of a class that can
     * write a SWF with the optional capability of creating a size report.
     * 
     * @param sizeReport if non-null, return a factory for an object that can
     * create a size report in addition to a writing a SWF. Otherwise the
     * factory is for an object that just writes a SWF.
     * @return a SWF writer factory.
     */
    public static ISWFWriterFactory getSWFWriterFactory(File sizeReport)
    {
        if (sizeReport != null)
            return new SWFWriterFactory(sizeReport);

        return SWFWriter.DEFAULT_SWF_WRITER_FACTORY;
    }

    /**
     * Create a SWF writer with a size reporter.
     * 
     * @param swf the SWF model to be encoded
     * @param useCompression use ZLIB compression if true
     * @param enableDebug enable debugging of the SWF if true
     * @param sizeReport the file the size report is written to.
     */
    public SizeReportWritingSWFWriter(ISWF swf, Compression useCompression,
                                      boolean enableDebug, boolean enableTelemetry, File sizeReport)
    {
        super(swf, useCompression, enableDebug, enableTelemetry);

        assert sizeReport != null;

        this.sizeReportFile = sizeReport;
        this.report = new SizeReport();
    }

    private final File sizeReportFile;
    private final SizeReport report;
    private boolean definingSprite = false;

    @Override
    public int writeTo(File outputFile) throws FileNotFoundException, IOException
    {
        final int bytes = super.writeTo(outputFile);
        report.setCompressedSize(bytes);

        return bytes;
    }

    @Override
    public void writeTo(OutputStream output)
    {
        CountingOutputStream countingOutput = new CountingOutputStream(output);
        super.writeTo(countingOutput);

        report.setCompressedSize(countingOutput.getCount());

        writeSizeReport();
    }

    @SuppressWarnings("incomplete-switch")
	@Override
    protected void finishTag(ITag tag, IOutputBitStream tagData, IOutputBitStream out)
    {
        int startPos = out.size();
        super.finishTag(tag, tagData, out);

        int recordLength = out.size() - startPos;

        switch (tag.getTagType())
        {
            case DoABC:
                reportDoABC((DoABCTag)tag, recordLength);
                break;
            case FileAttributes:
                reportFileAttributes((FileAttributesTag)tag, recordLength);
                break;
            case SymbolClass:
                reportSymbolClass((SymbolClassTag)tag, recordLength);
                break;
            case ShowFrame:
                reportShowFrame(recordLength);
                break;
            case SetBackgroundColor:
                reportSetBackgroundColor(recordLength);
                break;
            case EnableDebugger2:
                reportEnableDebugger2(recordLength);
                break;
            case ScriptLimits:
                reportScriptLimits(recordLength);
                break;
            //            case ProductInfo:
            //                reportProductInfo((ProductInfoTag)tag, recordLength);
            //                break;
            case Metadata:
                reportMetadata(recordLength);
                break;
            case DefineBits:
            case DefineBitsJPEG2:
            case DefineBitsJPEG3:
            case DefineBitsLossless2:
                reportBitmap((CharacterTag)tag, recordLength);
                break;
            case DefineBinaryData:
                reportDefineBinaryData((DefineBinaryDataTag)tag, recordLength);
                break;
            case DefineShape:
            case DefineShape2:
            case DefineShape3:
            case DefineShape4:
                reportDefineShape((DefineShapeTag)tag, recordLength);
                break;
            case DefineSprite:
                reportDefineSprite((DefineSpriteTag)tag, recordLength);
                break;
            case ExportAssets:
                reportExportAssets(recordLength);
                break;
            //            case DefineScalingGrid:
            //                reportDefineScalingGrid((DefineScalingGridTag)tag, recordLength);
            //                break;
            case DefineFont:
                reportDefineFont((DefineFontTag)tag, recordLength);
                break;
            case DefineFont2:
            case DefineFont3:
                reportDefineFont2or3((DefineFont2Tag)tag, recordLength);
                break;
            case DefineFont4:
                reportDefineFont4((DefineFont4Tag)tag, recordLength);
                break;
            //            case DefineFontInfo:
            //                reportDefineFontInfo((IFontInfo)tag, recordLength);
            //                break;
            //            case DefineFontInfo2:
            //                reportDefineFontInfo2((DefineFontInfo2Tag)tag, recordLength);
            //                break;
            //            case DefineFontAlignZones:
            //                reportDefineFontAlignZones((DefineFontAlignZonesTag)tag);
            //                break;
            //            case DefineFontName:
            //                reportDefineFontName((DefineFontNameTag)tag);
            //                break;
            //            case DefineText:
            //                reportDefineText((DefineTextTag)tag, extraTags);
            //                break;
            //            case DefineText2:
            //                reportDefineText2((DefineText2Tag)tag, extraTags);
            //                break;
            //            case DefineEditText:
            //                reportDefineEditText((DefineEditTextTag)tag, extraTags);
            //                break;
            case DefineSound:
                reportDefineSound((DefineSoundTag)tag, recordLength);
                break;
            case DefineVideoStream:
                reportDefineVideoStream((DefineVideoStreamTag)tag, recordLength);
                break;
            //            case VideoFrame:
            //                reportVideoFrame((VideoFrameTag)tag);
            //                break;
            //            case StartSound:
            //                reportStartSound((StartSoundTag)tag);
            //                break;
            //            case StartSound2:
            //                reportStartSound2((StartSound2Tag)tag);
            //                break;
            //            case SoundStreamHead:
            //                reportSoundStreamHead((SoundStreamHeadTag)tag);
            //                break;
            //            case SoundStreamHead2:
            //                reportSoundStreamHead((SoundStreamHead2Tag)tag);
            //                break;
            //            case SoundStreamBlock:
            //                reportSoundStreamBlock((SoundStreamBlockTag)tag);
            //                break;
            //            case DefineButton:
            //                reportDefineButton((DefineButtonTag)tag);
            //                break;
            //            case DefineButton2:
            //                reportDefineButton2((DefineButton2Tag)tag);
            //                break;
            //            case DefineButtonSound:
            //                reportDefineButtonSound((DefineButtonSoundTag)tag);
            //                break;
            //            case CSMTextSettings:
            //                reportCSMTextSettings((CSMTextSettingsTag)tag);
            //                break;
            case End:
                reportEnd();
                break;
            //            case JPEGTables:
            //                reportJPEGTables(((JPEGTablesTag)tag));
            //                break;
            //            case DefineMorphShape:
            //                reportDefineMorphShape((DefineMorphShapeTag)tag);
            //                break;
            //            case DefineMorphShape2:
            //                reportDefineMorphShape2((DefineMorphShape2Tag)tag);
            //                break;
            //            case PlaceObject:
            //                reportPlaceObject((PlaceObjectTag)tag);
            //                break;
            //            case PlaceObject2:
            //                reportPlaceObject2((PlaceObject2Tag)tag);
            //                break;
            //            case PlaceObject3:
            //                reportPlaceObject3((PlaceObject3Tag)tag);
            //                break;
            //            case RemoveObject:
            //                reportRemoveObject((RemoveObjectTag)tag);
            //                break;
            //            case RemoveObject2:
            //                reportRemoveObject2((RemoveObject2Tag)tag);
            //                break;
            //            case SetTabIndex:
            //                reportSetTabIndex((SetTabIndexTag)tag);
            //                break;
            case FrameLabel:
                reportFrameLabel((FrameLabelTag)tag, recordLength);
                break;
        }

    }

    @Override
    protected void writeCompressibleHeader()
    {
        super.writeCompressibleHeader();

        tagBuffer.flush();
        report.startEntry(SizeReport.HEADER_DATA, 0, -1, "swfHeader");

        // Here we are recording the size of the full header, not just
        // the compressible part. The full header is 12 bytes plus the 
        // size needed to encode a RECT record. The 'tagBuffer' contains
        // the size of the RECT so we just add 12 to it to get the full size.
        report.endEntry(SizeReport.HEADER_DATA, tagBuffer.size() + 12);
    }

    @Override
    protected void writeDefineSprite(DefineSpriteTag tag)
    {
        definingSprite = true;
        super.writeDefineSprite(tag);
        definingSprite = false;
    }

    private void reportShowFrame(int recordLength)
    {
        if (!definingSprite)
        {
            report.addEntry(SizeReport.FRAME_DATA, -1, 2, "showFrame");
            report.endEntry(SizeReport.FRAME, 0);
        }
    }

    private void reportDefineBinaryData(DefineBinaryDataTag tag, int recordLength)
    {
        report.addEntry(SizeReport.BINARY, tag.getCharacterID(), recordLength);
    }

    private void reportDefineShape(DefineShapeTag tag, int recordLength)
    {
        report.addEntry(SizeReport.SHAPE, tag.getCharacterID(), recordLength);
    }

    public void reportDoABC(DoABCTag tag, int recordLength)
    {
        report.addEntry(SizeReport.SCRIPT, -1, recordLength, tag.getName());
    }

    private void reportEnableDebugger2(int recordLength)
    {
        report.addEntry(SizeReport.HEADER_DATA, -1, recordLength, "enableDebugger");
    }

    public void reportFileAttributes(FileAttributesTag tag, int recordLength)
    {
        report.addEntry(SizeReport.HEADER_DATA, -1, recordLength, "fileAttributes");
    }

    private void reportMetadata(int recordLength)
    {
        report.addEntry(SizeReport.HEADER_DATA, -1, recordLength, "metaData");
    }

    private void reportScriptLimits(int recordLength)
    {
        report.addEntry(SizeReport.HEADER_DATA, -1, recordLength, "scriptLimits");
    }

    private void reportSetBackgroundColor(int recordLength)
    {
        report.addEntry(SizeReport.HEADER_DATA, -1, recordLength, "backgroundColor");
    }

    private void reportEnd()
    {
        report.addEntry(SizeReport.HEADER_DATA, -1, 2, "endMarker");
        report.setSize(outputBuffer.size());
    }

    private void reportSymbolClass(SymbolClassTag tag, int recordLength)
    {
        for (String symbol : tag.getSymbolNames())
            report.addSymbol(symbol, tag.getSymbol(symbol).getCharacterID());

        report.addEntry(SizeReport.FRAME_DATA, -1, recordLength, "symbolClass");
    }

    private void reportDefineVideoStream(DefineVideoStreamTag tag, int recordLength)
    {
        report.addEntry(SizeReport.VIDEO, tag.getCharacterID(), recordLength);
    }

    private void reportDefineSound(DefineSoundTag tag, int recordLength)
    {
        report.addEntry(SizeReport.SOUND, tag.getCharacterID(), recordLength);
    }

    private void reportDefineFont(DefineFontTag tag, int recordLength)
    {
        String fontName = null;
        DefineFontNameTag license = tag.getLicense();
        if (license != null)
            fontName = license.getFontName();

        report.addEntry(SizeReport.FONT, tag.getCharacterID(), recordLength,
                fontName);
    }

    private void reportDefineFont2or3(DefineFont2Tag tag, int recordLength)
    {
        report.addEntry(SizeReport.FONT, tag.getCharacterID(), recordLength,
                tag.getFontName());
    }

    private void reportDefineFont4(DefineFont4Tag tag, int recordLength)
    {
        report.addEntry(SizeReport.FONT, tag.getCharacterID(), recordLength,
                tag.getFontName());
    }

    private void reportBitmap(CharacterTag tag, int recordLength)
    {
        report.addEntry(SizeReport.BITMAP, tag.getCharacterID(), recordLength);
    }

    private void reportExportAssets(int recordLength)
    {
        report.addEntry(SizeReport.FRAME_DATA, -1, recordLength, "exportAssets");
    }

    private void reportDefineSprite(DefineSpriteTag tag, int recordLength)
    {
        report.addEntry(SizeReport.SPRITE, tag.getCharacterID(), recordLength);
    }

    private void reportFrameLabel(FrameLabelTag tag, int recordLength)
    {
        report.startEntry(SizeReport.FRAME, 0, -1, tag.getName());
        report.addEntry(SizeReport.FRAME_DATA, -1, recordLength, "frameLabel");
    }

    /**
     * Write the size report to the specified file.
     */
    private void writeSizeReport()
    {
        Writer reportOut = null;
        try
        {
            reportOut = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(sizeReportFile), "UTF8"));
            reportOut.write(report.generate());
            reportOut.flush();
        }
        catch (Exception e)
        {
            // TODO: report a problem
            throw new RuntimeException(e);
        }
        finally
        {
            if (reportOut != null)
                try
                {
                    reportOut.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
        }
    }
}
