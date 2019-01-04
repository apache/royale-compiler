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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;

import org.apache.royale.abc.ABCParser;
import org.apache.royale.abc.PoolingABCVisitor;
import org.apache.royale.abc.print.ABCDumpVisitor;
import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.Header;
import org.apache.royale.swf.SWF;
import org.apache.royale.swf.SWFFrame;
import org.apache.royale.swf.TagType;
import org.apache.royale.swf.tags.*;
import org.apache.royale.swf.types.ButtonRecord;
import org.apache.royale.swf.types.CurvedEdgeRecord;
import org.apache.royale.swf.types.EdgeRecord;
import org.apache.royale.swf.types.FillStyle;
import org.apache.royale.swf.types.FillStyleArray;
import org.apache.royale.swf.types.Filter;
import org.apache.royale.swf.types.FocalGradient;
import org.apache.royale.swf.types.GlyphEntry;
import org.apache.royale.swf.types.GradRecord;
import org.apache.royale.swf.types.IFillStyle;
import org.apache.royale.swf.types.ILineStyle;
import org.apache.royale.swf.types.KerningRecord;
import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.LineStyle2;
import org.apache.royale.swf.types.LineStyleArray;
import org.apache.royale.swf.types.MorphFillStyle;
import org.apache.royale.swf.types.MorphGradRecord;
import org.apache.royale.swf.types.MorphLineStyle;
import org.apache.royale.swf.types.RGB;
import org.apache.royale.swf.types.RGBA;
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
import org.apache.royale.utils.Base64;
import org.apache.royale.utils.Trace;

import com.google.common.collect.ImmutableList;

/**
 * Dump a SWF model to XML. Usage: swfdump [-abc] file1.swf
 */
public final class SWFDump
{
    /**
     * Dump a SWF at a given URL.
     * 
     * @param url URL of the SWF to dump.
     * @throws IOException Any IO error ;-)
     */
    public void dump(URL url) throws IOException
    {
        final SWFReader swfReader = new SWFReader();
        final String path = url.getPath();
        try
        {
            swf = (SWF)swfReader.readFrom(
                    new BufferedInputStream(url.openStream()),
                    path);

            ProblemQuery problemQuery = new ProblemQuery();
            problemQuery.addAll(swfReader.getProblems());
            if (!problemQuery.hasErrors())
            {
                dumpHeader(swf.getHeader());
                final ImmutableList<ITag> tags = ImmutableList.copyOf(swfReader.iterator());
                currentFrameIndex = 0;
                if (swf.getFrameCount() > 0)
                    currentFrame = swf.getFrameAt(0);

                HashMap<String, ITag> abcTags = new HashMap<String, ITag>();
                for (ITag tag : tags)
                {
                    if (sortOption)
                    {
                        if (tag.getTagType() == TagType.DoABC)
                        {
                            DoABCTag abcTag = (DoABCTag)tag;
                            abcTags.put(abcTag.getName(), tag);
                        }
                        else if (tag.getTagType() == TagType.ShowFrame)
                        {
                            if (abcTags.size() > 0)
                            {
                                ArrayList<String> nameList = new ArrayList<String>();
                                nameList.addAll(abcTags.keySet());
                                Collections.sort(nameList);
                                for (String name : nameList)
                                {
                                    ITag abcTag = abcTags.get(name);
                                    dumpTag(abcTag);
                                }
                            }
                            dumpTag(tag);
                            abcTags = new HashMap<String, ITag>();
                        }
                        else
                            dumpTag(tag);
                    }
                    else
                    {
                        dumpTag(tag);
                    }
                    if (tag.getTagType() == TagType.ShowFrame)
                    {
                        currentFrameIndex++;
                        if (currentFrameIndex < swf.getFrameCount())
                            currentFrame = swf.getFrameAt(currentFrameIndex);
                    }
                }

                finish();
            }

            printProblems(swfReader.getProblems());
        }
        finally
        {
            IOUtils.closeQuietly(swfReader);
        }
    }

    /**
     * Print out problems to standard output.
     * 
     * @param problems
     */
    private void printProblems(Collection<ICompilerProblem> problems)
    {
        CompilerProblemCategorizer categorizer = new CompilerProblemCategorizer();
        ProblemFormatter formatter = new WorkspaceProblemFormatter(new Workspace(),
                categorizer);
        ProblemPrinter printer = new ProblemPrinter(formatter, System.err);

        printer.printProblems(problems);
    }

    /**
     * Dump a tag.
     * 
     * @param tag the tag to dump.
     */
    private void dumpTag(ITag tag)
    {
        TagType type = tag.getTagType();

        switch (type)
        {
            case CSMTextSettings:
                dumpCSMTextSettings((CSMTextSettingsTag)tag);
                break;
            case DoABC:
                dumpDoABC((DoABCTag)tag);
                break;
            case DefineBinaryData:
                dumpDefineBinaryData((DefineBinaryDataTag)tag);
                break;
            case DefineBits:
                dumpDefineBits((DefineBitsTag)tag);
                break;
            case DefineBitsJPEG2:
                dumpDefineBitsJPEG2((DefineBitsTag)tag);
                break;
            case DefineBitsJPEG3:
                dumpDefineBitsJPEG3((DefineBitsJPEG3Tag)tag);
                break;
            case DefineBitsLossless:
                dumpDefineBitsLossless((DefineBitsLosslessTag)tag);
                break;
            case DefineBitsLossless2:
                dumpDefineBitsLossless2((DefineBitsLossless2Tag)tag);
                break;
            case DefineScalingGrid:
                dumpDefineScalingGrid((DefineScalingGridTag)tag);
                break;
            case DefineShape:
                dumpDefineShape((DefineShapeTag)tag);
                break;
            case DefineShape2:
                dumpDefineShape2((DefineShapeTag)tag);
                break;
            case DefineShape3:
                dumpDefineShape3((DefineShape3Tag)tag);
                break;
            case DefineShape4:
                dumpDefineShape4((DefineShape4Tag)tag);
                break;
            case DefineSprite:
                dumpDefineSprite((DefineSpriteTag)tag);
                break;
            case DefineSound:
                dumpDefineSound((DefineSoundTag)tag);
                break;
            case StartSound:
                dumpStartSound((StartSoundTag)tag);
                break;
            case StartSound2:
                // TODO StartSound2
                //              dumpStartSound2(); 
                break;
            case SoundStreamHead:
                dumpSoundStreamHead((SoundStreamHeadTag)tag);
                break;
            case SoundStreamHead2:
                dumpSoundStreamHead2((SoundStreamHeadTag)tag);
                break;
            case SoundStreamBlock:
                dumpSoundStreamBlock((SoundStreamBlockTag)tag);
                break;
            case DefineMorphShape:
                dumpDefineMorphShape((DefineMorphShapeTag)tag);
                break;
            case DefineMorphShape2:
                dumpDefineMorphShape2((DefineMorphShapeTag)tag);
                break;
            case DefineSceneAndFrameLabelData:
                // TODO: no dump routine for this tag.
                //dumpDefineSceneAndFrameLabelData();
                break;
            case DefineFont:
                dumpDefineFont((DefineFontTag)tag);
                break;
            case DefineFontInfo:
                dumpDefineFontInfo((DefineFontInfoTag)tag);
                break;
            case DefineFont2:
                dumpDefineFont2((DefineFont2Tag)tag);
                break;
            case DefineFont3:
                dumpDefineFont3((DefineFont3Tag)tag);
                break;
            case DefineFont4:
                dumpDefineFont4((DefineFont4Tag)tag);
                break;
            case DefineFontAlignZones:
                dumpDefineFontAlignZones((DefineFontAlignZonesTag)tag);
                break;
            case DefineFontName:
                dumpDefineFontName((DefineFontNameTag)tag);
                break;
            case DefineText:
                dumpDefineText((DefineTextTag)tag);
                break;
            case DefineText2:
                dumpDefineText((DefineTextTag)tag);
                break;
            case DefineEditText:
                dumpDefineEditText((DefineEditTextTag)tag);
                break;
            case DefineButton:
                dumpDefineButton((DefineButtonTag)tag);
                break;
            case DefineButton2:
                dumpDefineButton2((DefineButton2Tag)tag);
                break;
            case DefineButtonSound:
                dumpDefineButtonSound((DefineButtonSoundTag)tag);
                break;
            case DefineVideoStream:
                dumpDefineVideoStream((DefineVideoStreamTag)tag);
                break;
            case VideoFrame:
                dumpVideoFrame((VideoFrameTag)tag);
                break;
            case End:
                break;
            case EnableDebugger2:
                dumpEnableDebugger2((EnableDebugger2Tag)tag);
                break;
            case ExportAssets:
                dumpExportAssets((ExportAssetsTag)tag);
                break;
            case FileAttributes:
                dumpFileAttributes((FileAttributesTag)tag);
                break;
            case FrameLabel:
                dumpFrameLabel((FrameLabelTag)tag);
                break;
            case JPEGTables:
                // TODO: handle this tag.
                //dumpJPEGTables();
                break;
            case Metadata:
                dumpMetadata((MetadataTag)tag);
                break;
            case ProductInfo:
                dumpProductInfo((ProductInfoTag)tag);
                break;
            case PlaceObject:
                dumpPlaceObject((PlaceObjectTag)tag);
                break;
            case PlaceObject2:
                dumpPlaceObject2((PlaceObjectTag)tag);
                break;
            case PlaceObject3:
                dumpPlaceObject3((PlaceObjectTag)tag);
                break;
            case RemoveObject:
                dumpRemoveObject((RemoveObjectTag)tag);
                break;
            case RemoveObject2:
                dumpRemoveObject2((RemoveObject2Tag)tag);
                break;
            case ScriptLimits:
                dumpScriptLimits((ScriptLimitsTag)tag);
                break;
            case SetBackgroundColor:
                dumpSetBackgroundColor((SetBackgroundColorTag)tag);
                break;
            case SetTabIndex:
                dumpSetTabIndex((SetTabIndexTag)tag);
                break;
            case ShowFrame:
                dumpShowFrame((ShowFrameTag)tag);
                break;
            case SymbolClass:
                dumpSymbolClass((SymbolClassTag)tag);
                break;
            case EnableTelemetry:
                dumpEnableTelemetry((EnableTelemetryTag) tag);
                break;
            default:
                assert (tag instanceof RawTag);
                if (tag instanceof RawTag) {
                    dumpRawTag((RawTag) tag);
                }
                break;
        }

    }

    /**
     * this value should get set after the header is parsed
     */
    @SuppressWarnings("unused")
    private Integer swfVersion = null;

    private boolean abc = false;
    private boolean verbose = false;
    private boolean showActions = true;
    private boolean showOffset = false;
    @SuppressWarnings("unused")
    private boolean showByteCode = false;
    @SuppressWarnings("unused")
    private boolean showDebugSource = false;
    private boolean glyphs = true;
    private boolean external = false;
    private String externalPrefix = null;
    private String externalDirectory = null;
    @SuppressWarnings("unused")
    private boolean decompile;
    @SuppressWarnings("unused")
    private boolean defunc;
    private int indent = 0;
    private boolean tabbedGlyphs = true;
    private SWF swf;

    /**
     * Constructor.
     * 
     * @param out The output stream.
     */
    public SWFDump(PrintWriter out)
    {
        this.out = out;
    }

    // TODO: We currently don't decode actions.
    //    private void printActions(ActionList list)
    //    {
    //        if (decompile)
    //        {
    //            /*
    //             AsNode node;
    //             try
    //             {
    //             node = new Decompiler(defunc).decompile(list);
    //             new PrettyPrinter(out, indent).list(node);
    //             return;
    //             }
    //             catch (Exception e)
    //             {
    //             indent();
    //             out.println("// error while decompiling.  falling back to disassembler");
    //             }
    //             */
    //        }
    //        
    //        Disassembler disassembler = new Disassembler(out, showOffset, indent);
    //        if (showDebugSource)
    //        {
    //            disassembler.setShowDebugSource(showDebugSource);
    //            disassembler.setComment("// ");
    //        }
    //        list.visitAll(disassembler);
    //    }

    @SuppressWarnings("unused")
    private void setExternal(boolean b, String path)
    {
        external = b;

        if (external)
        {
            if (path != null)
            {
                externalPrefix = baseName(path);
                externalDirectory = dirName(path);
            }

            if (externalPrefix == null)
                externalPrefix = "";
            else
                externalPrefix += "-";
            if (externalDirectory == null)
                externalDirectory = "";
        }
    }

    private void indent()
    {
        for (int i = 0; i < indent; i++)
        {
            out.print("  ");
        }
    }

    public void dumpHeader(Header h)
    {
        swfVersion = h.getVersion();
        out.println("<swf xmlns=\"http://macromedia/2003/swfx\"" +
                    " version=\"" + h.getVersion() + "\"" +
                    " framerate=\"" + h.getFrameRate() + "\"" +
                    " size=\"" + h.getFrameSize().getWidth() + "x" +
                    h.getFrameSize().getHeight() + "\"" +
                    " compressed=\"" + (h.getCompression() != Header.Compression.NONE) +
                    "\"" + " >");
        indent++;
        indent();
        if (sortOption)
            out.println("<!-- framecount=" + h.getFrameCount() + " length might be different because of debugfile paths -->");
        else
            out.println("<!-- framecount=" + h.getFrameCount() + " length=" + h.getLength() + " -->");
    }

    public void dumpProductInfo(ProductInfoTag productInfo)
    {
        open(productInfo);
        out.print(" product=\"" + productInfo.getProduct() + "\"");
        out.print(" edition=\"" + productInfo.getEdition() + "\"");
        out.print(" version=\"" + productInfo.getMajorVersion() + "." +
                  productInfo.getMinorVersion() + "\"");
        out.print(" build=\"" + productInfo.getBuild() + "\"");
        out.print(" compileDate=\"" + DateFormat.getInstance().format(new Date(productInfo.getCompileDate())) + "\"");
        close();
    }

    public void dumpMetadata(MetadataTag tag)
    {
        open(tag);
        end();
        indent();
        openCDATA();
        String metaData = tag.getMetadata();
        if (sortOption)
        {
            metaData = metaData.replaceAll("build=\".*\"", "");
        }
        out.println(metaData);
        closeCDATA();
        close(tag);
    }

    public void dumpFileAttributes(FileAttributesTag tag)
    {
        open(tag);
        out.print(" useDirectBlit=\"" + tag.isUseDirectBlit() + "\"");
        out.print(" useGPU=\"" + tag.isUseGPU() + "\"");
        out.print(" hasMetadata=\"" + tag.isHasMetadata() + "\"");
        out.print(" actionScript3=\"" + tag.isAS3() + "\"");
        out.print(" suppressCrossDomainCaching=\"" + tag.isSuppressCrossDomainCaching() + "\"");
        out.print(" swfRelativeUrls=\"" + tag.isSWFRelativeURLs() + "\"");
        out.print(" useNetwork=\"" + tag.isUseNetwork() + "\"");
        close();
    }

    private final PrintWriter out;

    private SWFFrame currentFrame;
    private int currentFrameIndex;

    /**
     * Get the symbol name of a class.
     * 
     * @param tag tag to look up.
     * @return Name of tag in the symbol class table, null if not found.
     */
    public String id(ICharacterTag tag)
    {
        return String.valueOf(tag.getCharacterID());
    }

    public void setOffsetAndSize(int offset, int size)
    {
        // Note: 'size' includes the size of the tag's header
        // so it is either length + 2 or length + 6.

        if (showOffset)
        {
            indent();
            out.println("<!--" +
                        " offset=" + offset +
                        " size=" + size +
                        " -->");
        }
    }

    /**
     * Output the start of XML output for a SWF tag.
     * 
     * @param tag the tag to output.
     */
    private void open(ITag tag)
    {
        indent();
        out.print("<" + tag.getTagType().toString());
    }

    /**
     * Output the end of XML output for a SWF tag.
     * 
     * @param tag the tag to output.
     */
    private void end()
    {
        out.println(">");
        indent++;
    }

    private void openCDATA()
    {
        indent();
        out.print("<![CDATA[");
    }

    private void closeCDATA()
    {
        out.println("]]>");
    }

    private void close()
    {
        out.println("/>");
    }

    private void close(Tag tag)
    {
        indent--;
        indent();
        out.println("</" + tag.getTagType().toString() + ">");
    }

    public void error(String s)
    {
        indent();
        out.println("<!-- error: " + s + " -->");
    }

    public void dumpRawTag(RawTag tag)
    {
        indent();
        out.println("<!-- unknown tag=" + tag.getTagType().getValue() + " length=" +
                    (tag.getTagBody() != null ? tag.getTagBody().length : 0) + " -->");
    }

    public void dumpShowFrame(ShowFrameTag tag)
    {
        open(tag);
        close();
    }

    public void dumpDefineShape(DefineShapeTag tag)
    {
        printDefineShape(tag);
    }

    private void printDefineShape(DefineShapeTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        out.print(" bounds=\"" + tag.getShapeBounds() + "\"");
        if (tag.getTagType() == TagType.DefineShape4)
        {
            DefineShape4Tag shape4Tag = (DefineShape4Tag)tag;
            out.print(" edgebounds=\"" + shape4Tag.getEdgeBounds() + "\"");
            out.print(" usesNonScalingStrokes=\"" + shape4Tag.isUsesNonScalingStrokes() + "\"");
            out.print(" usesScalingStrokes=\"" + shape4Tag.isUsesScalingStrokes() + "\"");
        }

        end();

        printShapeWithStyles(tag.getShapes());

        close(tag);
    }

    static final char[] digits = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * @param color to print.
     * @return string formatted as #RRGGBB
     */
    public String printColor(RGB color)
    {
        StringBuilder b = new StringBuilder();
        b.append('#');
        int red = color.getRed();
        b.append(digits[(red >> 4) & 15]);
        b.append(digits[red & 15]);
        int green = color.getGreen();
        b.append(digits[(green >> 4) & 15]);
        b.append(digits[green & 15]);
        int blue = color.getBlue();
        b.append(digits[(blue >> 4) & 15]);
        b.append(digits[blue & 15]);

        if (color instanceof RGBA)
        {
            int alpha = ((RGBA)color).getAlpha();
            b.append(digits[(alpha >> 4) & 15]);
            b.append(digits[alpha & 15]);
        }

        return b.toString();
    }

    public void dumpPlaceObject(PlaceObjectTag tag)
    {
        open(tag);
        out.print(" idref=\"" + idRef(tag.getCharacter()) + "\"");
        out.print(" depth=\"" + tag.getDepth() + "\"");
        out.print(" matrix=\"" + tag.getMatrix() + "\"");
        if (tag.getColorTransform() != null)
            out.print(" colorXform=\"" + tag.getColorTransform() + "\"");
        close();
    }

    public void dumpRemoveObject(RemoveObjectTag tag)
    {
        open(tag);
        out.print(" idref=\"" + idRef(tag.getCharacter()) + "\"");
        close();
    }

    public void outputBase64(byte[] data)
    {
        Base64.Encoder e = new Base64.Encoder(1024);

        indent();
        int remain = data.length;
        while (remain > 0)
        {
            int block = 1024;
            if (block > remain)
                block = remain;
            e.encode(data, data.length - remain, block);
            out.print(e.drain());
            remain -= block;
        }
        out.println(e.flush());
    }

    public void dumpDefineBits(DefineBitsTag tag)
    {
        if (tag.getData() == null)
        {
            out.println("<!-- warning: no JPEG table tag found. -->");
        }

        open(tag);
        out.print(" id=\"" + id(tag) + "\"");

        //        if (external)
        //        {
        //            String path = externalDirectory
        //            + externalPrefix
        //            + "image"
        //            + dictionary.getId(tag)
        //            + ".jpg";
        //            
        //            out.println(" src=\"" + path + "\" />");
        //            try
        //            {
        //                FileOutputStream image = new FileOutputStream(path, false);
        //                SwfImageUtils.JPEG jpeg = new SwfImageUtils.JPEG(tag.jpegTables.data, tag.data);
        //                jpeg.write(image);
        //                image.close();
        //            }
        //            catch (IOException e)
        //            {
        //                out.println("<!-- error: unable to write external asset file " + path + "-->");
        //            }
        //        }
        //        else
        {
            out.print(" encoding=\"base64\"");
            end();
            outputBase64(tag.getData());
            close(tag);
        }
    }

    public void dumpDefineButton(DefineButtonTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        end();
        if (showActions)
        {
            openCDATA();
            // todo print button records
            outputBase64(tag.getActions());
            closeCDATA();
        }
        else
        {
            // TODO: dump out raw action bytes
            //            out.println("<!-- " + tag.condActions[0].actionList.size() + " action(s) elided -->");
        }
        close(tag);
    }

    public void dumpSetBackgroundColor(SetBackgroundColorTag tag)
    {
        open(tag);
        out.print(" color=\"" + printColor(tag.getColor()) + "\"");
        close();
    }

    public void dumpDefineFont(DefineFontTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        end();

        if (glyphs)
        {
            for (int i = 0; i < tag.getGlyphShapeTable().length; i++)
            {
                indent();
                out.println("<glyph>");

                Shape shape = tag.getGlyphShapeTable()[i];
                indent++;
                printShapeWithTabs(shape);
                indent--;

                indent();
                out.println("</glyph>");
            }
        }
        close(tag);
    }

    public void dumpDefineText(DefineTextTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        out.print(" bounds=\"" + tag.getTextBounds() + "\"");
        out.print(" matrix=\"" + tag.getTextMatrix() + "\"");

        end();

        for (TextRecord tr : tag.getTextRecords())
        {
            printTextRecord(tr, tag.getTagType().getValue());
        }

        close(tag);
    }

    // TODO: decode actions
    //    public void doAction(DoAction tag)
    //    {
    //        open(tag);
    //        end();
    //        
    //        if (showActions)
    //        {
    //            openCDATA();
    //            printActions(tag.actionList);
    //            closeCDATA();
    //        }
    //        else
    //        {
    //            out.println("<!-- " + tag.actionList.size() + " action(s) elided -->");
    //        }
    //        close(tag);
    //    }

    public void dumpDefineFontInfo(DefineFontInfoTag tag)
    {
        open(tag);
        out.print(" idref=\"" + idRef(tag.getFontTag()) + "\"");
        out.print(" ansi=\"" + tag.isFontFlagsANSI() + "\"");
        out.print(" italic=\"" + tag.isFontFlagsItalic() + "\"");
        out.print(" bold=\"" + tag.isFontFlagsBold() + "\"");
        out.print(" wideCodes=\"" + tag.isFontFlagsWideCodes() + "\"");
        out.print(" smallText=\"" + tag.isFontFlagsSmallText() + "\"");
        out.print(" name=\"" + escape(tag.getFontName()) + "\"");
        out.print(" shiftJIS=\"" + tag.isFontFlagsShiftJIS() + "\"");
        end();
        indent();
        int[] codeTable = tag.getCodeTable();
        for (int i = 0; i < codeTable.length; i++)
        {
            out.print((int)codeTable[i]);
            if ((i + 1) % 16 == 0)
            {
                out.println();
                indent();
            }
            else
            {
                out.print(' ');
            }
        }
        if (codeTable.length % 16 != 0)
        {
            out.println();
            indent();
        }
        close(tag);
    }

    public void dumpDefineSound(DefineSoundTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        out.print(" format=\"" + tag.getSoundFormat() + "\"");
        out.print(" rate=\"" + tag.getSoundRate() + "\"");
        out.print(" size=\"" + tag.getSoundSize() + "\"");
        out.print(" type=\"" + tag.getSoundType() + "\"");
        out.print(" sampleCount=\"" + tag.getSoundSampleCount() + "\"");
        out.print(" soundDataSize=\"" + tag.getSoundData().length + "\"");
        end();
        openCDATA();
        outputBase64(tag.getSoundData());
        closeCDATA();
        close(tag);
    }

    public void dumpStartSound(StartSoundTag tag)
    {
        open(tag);
        out.print(" soundid=\"" + idRef(tag.getSoundTag()) + "\"");
        printSoundInfo(tag.getSoundInfo());
        close(tag);
    }

    private void printSoundInfo(SoundInfo info)
    {
        out.print(" syncStop=\"" + info.isSyncStop() + "\"");
        out.print(" syncNoMultiple=\"" + info.isSyncNoMultiple() + "\"");
        if (info.getInPoint() != 0)
        {
            out.print(" inPoint=\"" + info.getInPoint() + "\"");
        }
        if (info.getOutPoint() != 0)
        {
            out.print(" outPoint=\"" + info.getOutPoint() + "\"");
        }
        if (info.getLoopCount() != 0)
        {
            out.print(" loopCount=\"" + info.getLoopCount() + "\"");
        }
        end();

        SoundEnvelope[] envelopes = info.getEnvelopeRecords();
        if (envelopes != null && envelopes.length > 0)
        {
            openCDATA();
            for (int i = 0; i < envelopes.length; i++)
            {
                out.println("pos44      =\"" + envelopes[i].getPos44() + "\"");
                out.println("left level =\"" + envelopes[i].getLeftLevel() + "\"");
                out.println("right level=\"" + envelopes[i].getRightLevel() + "\"");
            }
            closeCDATA();
        }
    }

    public void dumpDefineButtonSound(DefineButtonSoundTag tag)
    {
        open(tag);
        out.print(" buttonId=\"" + idRef(tag.getButtonTag()) + "\"");
        close();
    }

    public void dumpSoundStreamHead(SoundStreamHeadTag tag)
    {
        open(tag);
        close();
    }

    public void dumpSoundStreamBlock(SoundStreamBlockTag tag)
    {
        open(tag);
        close();
    }

    public void dumpDefineBinaryData(DefineBinaryDataTag tag)
    {
        open(tag);
        out.println(" id=\"" + id(tag) + "\" length=\"" + tag.getData().length + "\" />");
    }

    public void dumpDefineBitsLossless(DefineBitsLosslessTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\" width=\"" + tag.getBitmapWidth() +
                  "\" height=\"" + tag.getBitmapHeight() + "\"");

        if (external)
        {
            String path = externalDirectory
                          + externalPrefix
                          + "image"
                          + id(tag)
                          + ".bitmap";

            out.println(" src=\"" + path + "\" />");
            try
            {
                FileOutputStream image = new FileOutputStream(path, false);
                image.write(tag.getData());
                image.close();
            }
            catch (IOException e)
            {
                out.println("<!-- error: unable to write external asset file " + path + "-->");
            }
        }
        else
        {
            out.print(" encoding=\"base64\"");
            end();
            outputBase64(tag.getData());
            close(tag);
        }
    }

    public void dumpDefineBitsJPEG2(DefineBitsTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");

        if (external)
        {
            String path = externalDirectory
                          + externalPrefix
                          + "image"
                          + id(tag)
                          + ".jpg";

            out.println(" src=\"" + path + "\" />");
            try
            {
                FileOutputStream image = new FileOutputStream(path, false);
                image.write(tag.getData());
                image.close();
            }
            catch (IOException e)
            {
                out.println("<!-- error: unable to write external asset file " + path + "-->");
            }
        }
        else
        {
            out.print(" encoding=\"base64\"");
            end();
            outputBase64(tag.getData());
            close(tag);
        }
    }

    public void dumpDefineShape2(DefineShapeTag tag)
    {
        printDefineShape(tag);
    }

    public void dumpDefineButtonCxform(DefineButtonCxformTag tag)
    {
        open(tag);
        out.print(" buttonId=\"" + idRef(tag.getButtonTag()) + "\"");
        close();
    }

    // TODO: Handle the Protect Tag
    //    public void protect(GenericTag tag)
    //    {
    //        open(tag);
    //        if (tag.data != null)
    //            out.print(" password=\"" + hexify(tag.data) + "\"");
    //        close();
    //    }

    public void dumpPlaceObject2(PlaceObjectTag tag)
    {
        dumpPlaceObject23(tag);
    }

    public void dumpPlaceObject3(PlaceObjectTag tag)
    {
        dumpPlaceObject23(tag);
    }

    public void dumpPlaceObject23(PlaceObjectTag tag)
    {
        PlaceObject2Tag tag2 = tag instanceof PlaceObject2Tag ? (PlaceObject2Tag)tag : null;
        PlaceObject3Tag tag3 = tag instanceof PlaceObject3Tag ? (PlaceObject3Tag)tag : null;

        if (tag != null || (tag2 != null && tag2.isHasCharacter()))
        {
            if (tag.getCharacter() != null && currentFrame != null &&
                currentFrame.getSymbolName(tag.getCharacter()) != null)
            {
                indent();
                out.println("<!-- instance of " + idRef(tag.getCharacter()) + " -->");
            }
        }

        open(tag);
        if (tag3 != null && tag3.isHasClassName())
            out.print(" className=\"" + tag3.getClassName() + "\"");
        if (tag3 != null && tag3.isHasImage())
            out.print(" hasImage=\"true\" ");
        if (tag != null || (tag2 != null && tag2.isHasCharacter()))
            out.print(" idref=\"" + idRef(tag.getCharacter()) + "\"");
        if (tag2 != null && tag2.isHasName())
            out.print(" name=\"" + tag2.getName() + "\"");
        out.print(" depth=\"" + tag.getDepth() + "\"");
        if (tag2 != null && tag2.isHasClipDepth())
            out.print(" clipDepth=\"" + tag2.getClipDepth() + "\"");
        if (tag3 != null && tag3.isHasCacheAsBitmap())
            out.print(" cacheAsBitmap=\"true\"");
        if (tag2 != null && tag2.isHasRatio())
            out.print(" ratio=\"" + tag2.getRatio() + "\"");
        if (tag2 != null && tag2.isHasColorTransform())
            out.print(" cxform=\"" + tag2.getColorTransform() + "\"");
        else if (tag.getColorTransform() != null)
            out.print(" cxform=\"" + tag.getColorTransform() + "\"");
        if (tag.getMatrix() != null || (tag2 != null && tag2.isHasMatrix()))
            out.print(" matrix=\"" + tag.getMatrix() + "\"");
        if (tag3 != null && tag3.isHasBlendMode())
            out.print(" blendmode=\"" + tag3.getBlendMode() + "\"");
        if (tag3 != null && tag3.isHasFilterList())
        {
            // todo - pretty print this once we actually care
            out.print(" filters=\"");
            for (Filter filter : tag3.getSurfaceFilterList())
            {
                out.print(filter.getFilterID() + " ");
            }
            out.print("\"");
        }

        // TODO: decode clip actions
        //        if (tag3 != null && tag3.isHasClipActions())
        //        {
        //            end();
        //            Iterator it = tag.clipActions.clipActionRecords.iterator();
        //            
        //            openCDATA();
        //            for (ClipActions )
        //            {
        //                ClipActionRecord record = (ClipActionRecord)it.next();
        //                indent();
        //                out.println("onClipEvent(" + printClipEventFlags(record.eventFlags) +
        //                            (record.hasKeyPress() ? "<" + record.keyCode + ">" : "") +
        //                            ") {");
        //                indent++;
        //                if (showActions)
        //                {
        //                    printActions(record.actionList);
        //                }
        //                else
        //                {
        //                    indent();
        //                    out.println("// " + record.actionList.size() + " action(s) elided");
        //                }
        //                indent--;
        //                indent();
        //                out.println("}");
        //            }
        //            closeCDATA();
        //            close(tag);
        //        }
        //        else
        {
            close();
        }
    }

    public void dumpRemoveObject2(RemoveObject2Tag tag)
    {
        open(tag);
        out.print(" depth=\"" + tag.getDepth() + "\"");
        close();
    }

    public void dumpDefineShape3(DefineShape3Tag tag)
    {
        printDefineShape(tag);
    }

    public void dumpDefineShape4(DefineShape4Tag tag)
    {
        printDefineShape(tag);
    }

    private void printShapeWithStyles(ShapeWithStyle shapes)
    {
        printFillStyles(shapes.getFillStyles());
        printLineStyles(shapes.getLineStyles());
        printShape(shapes);
    }

    @SuppressWarnings("unused")
    private void printMorphLineStyles(MorphLineStyle[] lineStyles)
    {
        for (int i = 0; i < lineStyles.length; i++)
        {
            MorphLineStyle lineStyle = lineStyles[i];
            indent();
            out.print("<linestyle ");
            out.print("startColor=\"" + printColor(lineStyle.getStartColor()) + "\" ");
            out.print("endColor=\"" + printColor(lineStyle.getStartColor()) + "\" ");
            out.print("startWidth=\"" + lineStyle.getStartWidth() + "\" ");
            out.print("endWidth=\"" + lineStyle.getEndWidth() + "\" ");
            out.println("/>");
        }
    }

    private void printLineStyles(LineStyleArray linestyles)
    {
        for (ILineStyle lineStyle : linestyles)
        {
            indent();
            out.print("<linestyle ");
            if (lineStyle instanceof LineStyle)
            {
                LineStyle ls = (LineStyle)lineStyle;
                String color = printColor(ls.getColor());
                out.print("color=\"" + color + "\" ");
                out.print("width=\"" + ls.getWidth() + "\" ");
            }

            if (lineStyle instanceof LineStyle2)
            {
                LineStyle2 lineStyle2 = (LineStyle2)lineStyle;

                if (lineStyle2.getJoinStyle() == LineStyle2.JS_MITER_JOIN)
                {
                    out.print("miterLimit=\"" + lineStyle2.getMiterLimitFactor() + "\" ");
                }

                if (lineStyle2.isHasFillFlag())
                {
                    out.println(">");
                    indent();
                    FillStyleArray fillStyles = new FillStyleArray(1);
                    fillStyles.add(lineStyle2.getFillType());
                    printFillStyles(fillStyles);
                    indent();
                    out.println("</linestyle>");
                }
                else
                {
                    out.println("/>");
                }
            }
            else
            {
                out.println("/>");
            }
        }
    }

    private void printFillStyles(FillStyleArray fillstyles)
    {
        for (IFillStyle iFillStyle : fillstyles)
        {
            indent();
            out.print("<fillstyle");

            int fillStyleType;
            FillStyle fillStyle = (FillStyle)iFillStyle;
            fillStyleType = fillStyle.getFillStyleType();

            out.print(" type=\"" + fillStyleType + "\"");
            if (fillStyleType == FillStyle.SOLID_FILL)
            {
                out.print(" color=\"" + printColor(fillStyle.getColor()) + "\"");
            }
            if ((fillStyleType & FillStyle.LINEAR_GRADIENT_FILL) != 0)
            {
                if (fillStyleType == FillStyle.RADIAL_GRADIENT_FILL)
                    out.print(" typeName=\"radial\"");
                else if (fillStyleType == FillStyle.FOCAL_RADIAL_GRADIENT_FILL)
                    out.print(" typeName=\"focal\" focalPoint=\"" +
                              ((FocalGradient)fillStyle.getGradient()).getFocalPoint() + "\"");
                // todo print linear or radial or focal
                out.print(" gradient=\"" + formatGradient(fillStyle.getGradient().getGradientRecords()) + "\"");
                out.print(" matrix=\"" + fillStyle.getGradientMatrix() + "\"");
            }
            if ((fillStyleType & FillStyle.REPEATING_BITMAP_FILL) != 0)
            {
                // todo print tiled or clipped
                out.print(" idref=\"" + idRef(fillStyle.getBitmapCharacter()) + "\"");
                out.print(" matrix=\"" + fillStyle.getBitmapMatrix() + "\"");
            }
            out.println(" />");
        }
    }

    @SuppressWarnings("unused")
    private void printMorphFillStyles(MorphFillStyle[] fillStyles)
    {
        for (int i = 0; i < fillStyles.length; i++)
        {
            MorphFillStyle fillStyle = fillStyles[i];
            indent();
            out.print("<fillstyle");
            out.print(" type=\"" + fillStyle.getFillStyleType() + "\"");
            if (fillStyle.getFillStyleType() == FillStyle.SOLID_FILL)
            {
                out.print(" startColor=\"" + printColor(fillStyle.getStartColor()) + "\"");
                out.print(" endColor=\"" + printColor(fillStyle.getEndColor()) + "\"");
            }

            if ((fillStyle.getFillStyleType() & FillStyle.LINEAR_GRADIENT_FILL) != 0)
            {
                // todo print linear or radial
                out.print(" gradient=\"" + formatMorphGradient(
                          fillStyle.getGradient().toArray(new MorphGradRecord[0])) + "\"");
                out.print(" startMatrix=\"" + fillStyle.getStartGradientMatrix() + "\"");
                out.print(" endMatrix=\"" + fillStyle.getEndGradientMatrix() + "\"");
                if (fillStyle.getFillStyleType() == FillStyle.FOCAL_RADIAL_GRADIENT_FILL)
                {
                    out.print(" ratio1 =\"" + fillStyle.getRatio1());
                    out.print(" ratio2 =\"" + fillStyle.getRatio2());
                }
            }

            if ((fillStyle.getFillStyleType() & FillStyle.REPEATING_BITMAP_FILL) != 0)
            {
                // todo print tiled or clipped
                out.print(" idref=\"" + idRef(fillStyle.getBitmap()) + "\"");
                out.print(" startMatrix=\"" + fillStyle.getStartBitmapMatrix() + "\"");
                out.print(" endMatrix=\"" + fillStyle.getEndBitmapMatrix() + "\"");
            }
            out.println(" />");
        }
    }

    private String formatGradient(List<GradRecord> records)
    {
        StringBuilder b = new StringBuilder();
        int i = 0;
        for (GradRecord record : records)
        {
            b.append(record.getRatio());
            b.append(' ');
            b.append(printColor(record.getColor()));
            if (i + 1 < records.size())
                b.append(' ');
            i++;
        }
        return b.toString();
    }

    private String formatMorphGradient(MorphGradRecord[] records)
    {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < records.length; i++)
        {
            b.append(records[i].getStartRatio());
            b.append(',');
            b.append(records[i].getEndRatio());
            b.append(' ');
            b.append(printColor(records[i].getStartColor()));
            b.append(',');
            b.append(printColor(records[i].getEndColor()));
            if (i + 1 < records.length)
                b.append(' ');
        }
        return b.toString();
    }

    private void printShape(Shape shapes)
    {
        if (shapes == null)
            return;

        Styles styles = null;

        if (shapes instanceof ShapeWithStyle)
        {
            styles = new Styles(((ShapeWithStyle)shapes).getFillStyles(),
                                ((ShapeWithStyle)shapes).getLineStyles());
        }

        for (ShapeRecord shape : shapes.getShapeRecords())
        {
            indent();
            if (shape instanceof StyleChangeRecord)
            {
                StyleChangeRecord styleChange = (StyleChangeRecord)shape;
                out.print("<styleChange ");
                if (styleChange.isStateMoveTo())
                {
                    out.print("dx=\"" + styleChange.getMoveDeltaX() + "\" dy=\"" +
                              styleChange.getMoveDeltaY() + "\" ");
                }
                if (styleChange.isStateFillStyle0())
                {
                    out.print("fillStyle0=\"" +
                              (styles.getFillStyles().indexOf(styleChange.getFillstyle0()) + 1) + "\" ");
                }
                if (styleChange.isStateFillStyle1())
                {
                    out.print("fillStyle1=\"" +
                              (styles.getFillStyles().indexOf(styleChange.getFillstyle1()) + 1) + "\" ");
                }
                if (styleChange.isStateLineStyle())
                {
                    out.print("lineStyle=\"" +
                              (styles.getLineStyles().indexOf(styleChange.getLinestyle()) + 1) + "\" ");
                }
                if (styleChange.isStateNewStyles())
                {
                    out.println(">");
                    indent++;
                    printFillStyles(styleChange.getStyles().getFillStyles());
                    printLineStyles(styleChange.getStyles().getLineStyles());
                    indent--;
                    indent();
                    out.println("</styleChange>");
                    styles = styleChange.getStyles();
                }
                else
                {
                    out.println("/>");
                }
            }
            else
            {
                EdgeRecord edge = (EdgeRecord)shape;
                if (edge instanceof StraightEdgeRecord)
                {
                    StraightEdgeRecord straightEdge = (StraightEdgeRecord)edge;
                    out.println("<line dx=\"" + straightEdge.getDeltaX() + "\" dy=\"" +
                                straightEdge.getDeltaY() + "\" />");
                }
                else
                {
                    CurvedEdgeRecord curvedEdge = (CurvedEdgeRecord)edge;
                    out.print("<curve ");
                    out.print("cdx=\"" + curvedEdge.getControlDeltaX() + "\" cdy=\"" +
                              curvedEdge.getControlDeltaY() + "\" ");
                    out.print("dx=\"" + curvedEdge.getAnchorDeltaX() + "\" dy=\"" +
                              curvedEdge.getAnchorDeltaY() + "\" ");
                    out.println("/>");
                }
            }
        }
    }

    private void printShapeWithTabs(Shape shapes)
    {
        if (shapes == null)
            return;

        int startX = 0;
        int startY = 0;

        int x = 0;
        int y = 0;

        for (ShapeRecord shape : shapes.getShapeRecords())
        {
            indent();
            if (shape instanceof StyleChangeRecord)
            {
                StyleChangeRecord styleChange = (StyleChangeRecord)shape;

                // No longer print out number of bits in MoveDeltaX and 
                // MoveDeltaY. The StyleChangeRecord record does not preserve
                // this information.
                out.print("SCR\t");
                if (styleChange.isStateMoveTo())
                {
                    out.print(styleChange.getMoveDeltaX() + "\t" + styleChange.getMoveDeltaY());

                    if (startX == 0 && startY == 0)
                    {
                        startX = styleChange.getMoveDeltaX();
                        startY = styleChange.getMoveDeltaY();
                    }

                    x = styleChange.getMoveDeltaX();
                    y = styleChange.getMoveDeltaY();

                    out.print("\t\t");
                }
            }
            else
            {
                EdgeRecord edge = (EdgeRecord)shape;
                if (edge instanceof StraightEdgeRecord)
                {
                    StraightEdgeRecord straightEdge = (StraightEdgeRecord)edge;
                    out.print("SER" + "\t");
                    out.print(straightEdge.getDeltaX() + "\t" + straightEdge.getDeltaY());
                    x += straightEdge.getDeltaX();
                    y += straightEdge.getDeltaY();
                    out.print("\t\t");
                }
                else
                {
                    CurvedEdgeRecord curvedEdge = (CurvedEdgeRecord)edge;
                    out.print("CER" + "\t");
                    out.print(curvedEdge.getControlDeltaX() + "\t" + curvedEdge.getControlDeltaY() + "\t");
                    out.print(curvedEdge.getAnchorDeltaX() + "\t" + curvedEdge.getAnchorDeltaY());
                    x += (curvedEdge.getControlDeltaX() + curvedEdge.getAnchorDeltaX());
                    y += (curvedEdge.getControlDeltaY() + curvedEdge.getAnchorDeltaY());
                }
            }

            out.println("\t\t" + x + "\t" + y);
        }
    }

    // TODO: decode ClipActionRecords
    //    private String printClipEventFlags(int flags)
    //    {
    //        StringBuilder b = new StringBuilder();
    //        
    //        if ((flags & ClipActionRecord.unused31) != 0) b.append("res31,");
    //        if ((flags & ClipActionRecord.unused30) != 0) b.append("res30,");
    //        if ((flags & ClipActionRecord.unused29) != 0) b.append("res29,");
    //        if ((flags & ClipActionRecord.unused28) != 0) b.append("res28,");
    //        if ((flags & ClipActionRecord.unused27) != 0) b.append("res27,");
    //        if ((flags & ClipActionRecord.unused26) != 0) b.append("res26,");
    //        if ((flags & ClipActionRecord.unused25) != 0) b.append("res25,");
    //        if ((flags & ClipActionRecord.unused24) != 0) b.append("res24,");
    //        
    //        if ((flags & ClipActionRecord.unused23) != 0) b.append("res23,");
    //        if ((flags & ClipActionRecord.unused22) != 0) b.append("res22,");
    //        if ((flags & ClipActionRecord.unused21) != 0) b.append("res21,");
    //        if ((flags & ClipActionRecord.unused20) != 0) b.append("res20,");
    //        if ((flags & ClipActionRecord.unused19) != 0) b.append("res19,");
    //        if ((flags & ClipActionRecord.construct) != 0) b.append("construct,");
    //        if ((flags & ClipActionRecord.keyPress) != 0) b.append("keyPress,");
    //        if ((flags & ClipActionRecord.dragOut) != 0) b.append("dragOut,");
    //        
    //        if ((flags & ClipActionRecord.dragOver) != 0) b.append("dragOver,");
    //        if ((flags & ClipActionRecord.rollOut) != 0) b.append("rollOut,");
    //        if ((flags & ClipActionRecord.rollOver) != 0) b.append("rollOver,");
    //        if ((flags & ClipActionRecord.releaseOutside) != 0) b.append("releaseOutside,");
    //        if ((flags & ClipActionRecord.release) != 0) b.append("release,");
    //        if ((flags & ClipActionRecord.press) != 0) b.append("press,");
    //        if ((flags & ClipActionRecord.initialize) != 0) b.append("initialize,");
    //        if ((flags & ClipActionRecord.data) != 0) b.append("data,");
    //        
    //        if ((flags & ClipActionRecord.keyUp) != 0) b.append("keyUp,");
    //        if ((flags & ClipActionRecord.keyDown) != 0) b.append("keyDown,");
    //        if ((flags & ClipActionRecord.mouseUp) != 0) b.append("mouseUp,");
    //        if ((flags & ClipActionRecord.mouseDown) != 0) b.append("mouseDown,");
    //        if ((flags & ClipActionRecord.mouseMove) != 0) b.append("moseMove,");
    //        if ((flags & ClipActionRecord.unload) != 0) b.append("unload,");
    //        if ((flags & ClipActionRecord.enterFrame) != 0) b.append("enterFrame,");
    //        if ((flags & ClipActionRecord.load) != 0) b.append("load,");
    //        if (b.length() > 1)
    //        {
    //            b.setLength(b.length() - 1);
    //        }
    //        return b.toString();
    //    }

    public void defineText2(DefineTextTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        end();

        for (TextRecord tr : tag.getTextRecords())
        {
            printTextRecord(tr, tag.getTagType().getValue());
        }

        close(tag);
    }

    public void printTextRecord(TextRecord tr, int tagCode)
    {
        indent();
        out.print("<textRecord ");
        if (tr.isStyleFlagsHasFont())
        {
            out.print(" font=\"" + id(tr.getFontTag()) + "\"");
            out.print(" height=\"" + tr.getTextHeight() + "\"");
        }

        if (tr.isStyleFlagsHasXOffset())
            out.print(" xOffset=\"" + tr.getxOffset() + "\"");

        if (tr.isStyleFlagsHasYOffset())
            out.print(" yOffset=\"" + tr.getyOffset() + "\"");

        if (tr.isStyleFlagsHasColor())
            out.print(" color=\"" + printColor(tr.getTextColor()) + "\"");
        out.println(">");

        indent++;
        printGlyphEntries(tr);
        indent--;
        indent();
        out.println("</textRecord>");

    }

    private void printGlyphEntries(TextRecord tr)
    {
        indent();
        GlyphEntry[] entries = tr.getGlyphEntries();
        for (int i = 0; i < entries.length; i++)
        {
            GlyphEntry ge = entries[i];
            out.print(ge.getGlyphIndex());
            if (ge.getGlyphAdvance() >= 0)
                out.print('+');
            out.print(ge.getGlyphAdvance());
            out.print(' ');
            if ((i + 1) % 10 == 0)
            {
                out.println();
                indent();
            }
        }
        if (entries.length % 10 != 0)
            out.println();
    }

    public void dumpDefineButton2(DefineButton2Tag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        out.print(" trackAsMenu=\"" + tag.isTrackAsMenu() + "\"");
        end();

        for (int i = 0; i < tag.getCharacters().length; i++)
        {
            ButtonRecord record = tag.getCharacters()[i];
            indent();
            out.println("<buttonRecord " +
                        "idref=\"" + record.getCharacterID() + "\" " + //TODO: print symbol name
                        "depth=\"" + record.getPlaceDepth() + "\" " +
                        "matrix=\"" + record.getPlaceMatrix() + "\" " +
                        "stateHitTest=\"" + record.isStateHitTest() + "\" " +
                        "stateDown=\"" + record.isStateDown() + "\" " +
                        "stateOver=\"" + record.isStateOver() + "\" " +
                        "stateUp=\"" + record.isStateUp() + "\" " +
                        "/>");
            // todo print optional cxforma
        }

        // print conditional actions
        // TODO: print actions
        //        if (tag.condActions.length > 0 && showActions)
        //        {
        //            indent();
        //            out.println("<buttonCondAction>");
        //            openCDATA();
        //            for (int i = 0; i < tag.condActions.length; i++)
        //            {
        //                ButtonCondAction cond = tag.condActions[i];
        //                indent();
        //                out.println("on(" + cond + ") {");
        //                indent++;
        //                printActions(cond.actionList);
        //                indent--;
        //                indent();
        //                out.println("}");
        //            }
        //            closeCDATA();
        //            indent();
        //            out.println("</buttonCondAction>");
        //        }

        close(tag);
    }

    public void dumpDefineBitsJPEG3(DefineBitsJPEG3Tag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");

        // TODO: We don't have a JEB encoder
        //        if (external)
        //        {
        //            String path = externalDirectory
        //                + externalPrefix
        //                + "image"
        //                + id(tag)
        //                + ".jpg";
        //    
        //            out.println(" src=\"" + path + "\" />");
        //
        //            try
        //            {
        //                FileOutputStream image = new FileOutputStream(path, false);
        //                SwfImageUtils.JPEG jpeg = null;
        //
        //                if (tag.jpegTables != null)
        //                {
        //                    jpeg = new SwfImageUtils.JPEG(tag.jpegTables.data, tag.data);
        //                }
        //                else
        //                {
        //                    jpeg = new SwfImageUtils.JPEG(tag.data, true);
        //                }
        //
        //                jpeg.write(image);
        //                image.close();
        //            }
        //            catch (IOException e)
        //            {
        //                out.println("<!-- error: unable to write external asset file " + path + "-->");
        //            }
        //        }
        //        else
        {
            out.print(" encoding=\"base64\"");
            end();
            outputBase64(tag.getData());
            close(tag);
        }
    }

    public void dumpDefineBitsLossless2(DefineBitsLossless2Tag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");

        if (external)
        {
            String path = externalDirectory
                          + externalPrefix
                          + "image"
                          + id(tag)
                          + ".bitmap";

            out.println(" src=\"" + path + "\" />");
            try
            {
                FileOutputStream image = new FileOutputStream(path, false);
                image.write(tag.getData());
                image.close();
            }
            catch (IOException e)
            {
                out.println("<!-- error: unable to write external asset file " + path + "-->");
            }
        }
        else
        {
            out.print(" encoding=\"base64\"");
            end();
            outputBase64(tag.getData());
            close(tag);
        }
    }

    String escape(String s)
    {
        if (s == null)
            return null;

        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            switch (c)
            {
                case '<':
                    b.append("&lt;");
                    break;
                case '>':
                    b.append("&gt;");
                    break;
                case '&':
                    b.append("&amp;");
                    break;
                case '"':
                    b.append("&quot;");
                    break;
                default:
                {
                    // Non-printable low ASCII characters are invalid in XML
                    if ((c >=  0 && c <=  8)
                     || (c >= 11 && c <= 12)
                     || (c >= 14 && c <= 31))
                    {
                        b.append("?");
                    }
                    else
                    {
                        b.append(c);
                    }
                }
            }
        }

        return b.toString();
    }

    public void dumpDefineEditText(DefineEditTextTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");

        if (tag.isHasFont())
        {
            out.print(" fontId=\"" + id(tag.getFontTag()) + "\"");
            out.print(" fontName=\"" + escape(idRef(tag.getFontTag())) + "\"");
            out.print(" fontHeight=\"" + tag.getFontHeight() + "\"");
        }
        else if (tag.isHasFontClass())
        {
            out.print(" fontClass=\"" + tag.getFontClass() + "\"");
            out.print(" fontHeight=\"" + tag.getFontHeight() + "\"");
        }

        out.print(" bounds=\"" + tag.getBounds() + "\"");

        if (tag.isHasTextColor())
            out.print(" color=\"" + printColor(tag.getTextColor()) + "\"");

        out.print(" html=\"" + tag.isHtml() + "\"");
        out.print(" autoSize=\"" + tag.isAutoSize() + "\"");
        out.print(" border=\"" + tag.isBorder() + "\"");

        if (tag.isHasMaxLength())
            out.print(" maxLength=\"" + tag.getMaxLength() + "\"");

        out.print(" multiline=\"" + tag.isMultiline() + "\"");
        out.print(" noSelect=\"" + tag.isNoSelect() + "\"");
        out.print(" password=\"" + tag.isPassword() + "\"");
        out.print(" readOnly=\"" + tag.isReadOnly() + "\"");
        out.print(" useOutlines=\"" + tag.isUseOutlines() + "\"");
        out.print(" varName=\"" + escape(tag.getVariableName()) + "\"");
        out.print(" wordWrap=\"" + tag.isWordWrap() + "\"");

        if (tag.isHasLayout())
        {
            out.print(" align=\"" + tag.getAlign() + "\"");
            out.print(" indent=\"" + tag.getIndent() + "\"");
            out.print(" leading=\"" + tag.getLeading() + "\"");
            out.print(" leftMargin=\"" + tag.getLeftMargin() + "\"");
            out.print(" rightMargin=\"" + tag.getRightMargin() + "\"");
        }
        end();
        if (tag.isHasText())
        {
            indent();
            out.println("<text>");
            openCDATA();
            out.print(tag.getInitialText());
            closeCDATA();
            indent();
            out.println("</text>");
        }
        close(tag);
    }

    public void dumpDefineSprite(DefineSpriteTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        end();
        indent();
        out.println("<!-- sprite framecount=" + tag.getFrameCount() + " -->");

        // Dump list of nested tags in the sprite.
        for (ITag controlTag : tag.getControlTags())
            dumpTag(controlTag);

        close(tag);
    }

    public void finish()
    {
        --indent;
        indent();
        out.println("</swf>");
    }

    public void dumpFrameLabel(FrameLabelTag tag)
    {
        open(tag);
        out.print(" label=\"" + tag.getName() + "\"");
        if (tag.isNamedAnchorTag())
            out.print(" anchor=\"" + "true" + "\"");
        close();
    }

    public void dumpSoundStreamHead2(SoundStreamHeadTag tag)
    {
        open(tag);
        out.print(" playbackRate=\"" + tag.getPlaybackSoundRate() + "\"");
        out.print(" playbackSize=\"" + tag.getPlaybackSoundSize() + "\"");
        out.print(" playbackType=\"" + tag.getPlaybackSoundType() + "\"");
        out.print(" compression=\"" + tag.getStreamSoundCompression() + "\"");
        out.print(" streamRate=\"" + tag.getStreamSoundRate() + "\"");
        out.print(" streamSize=\"" + tag.getStreamSoundSize() + "\"");
        out.print(" streamType=\"" + tag.getStreamSoundRate() + "\"");
        out.print(" streamSampleCount=\"" + tag.getStreamSoundSampleCount() + "\"");

        if (tag.getStreamSoundCompression() == 2)
        {
            out.print(" latencySeek=\"" + tag.getLatencySeek() + "\"");
        }
        close();
    }

    public void dumpDefineScalingGrid(DefineScalingGridTag tag)
    {
        open(tag);
        out.print(" idref=\"" + id(tag.getCharacter()) + "\"");
        out.print(" grid=\"" + tag.getSplitter() + "\"");
        close();
    }

    public void dumpDefineMorphShape(DefineMorphShapeTag tag)
    {
        dumpDefineMorphShape2(tag);
    }

    public void dumpDefineMorphShape2(DefineMorphShapeTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        out.print(" startBounds=\"" + tag.getStartBounds() + "\"");
        out.print(" endBounds=\"" + tag.getEndBounds() + "\"");
        if (tag.getTagType() == TagType.DefineMorphShape2)
        {
            DefineMorphShape2Tag tag2 = (DefineMorphShape2Tag)tag;

            out.print(" startEdgeBounds=\"" + tag2.getStartEdgeBounds() + "\"");
            out.print(" endEdgeBounds=\"" + tag2.getEndEdgeBounds() + "\"");
            out.print(" usesNonScalingStrokes=\"" + tag2.isUsesNonScalingStrokes() + "\"");
            out.print(" usesScalingStrokes=\"" + tag2.isUsesScalingStrokes() + "\"");
        }
        end();

        // TODO: dump line styles and fill styles
        //        printMorphLineStyles(tag.lineStyles);
        //        printMorphFillStyles(tag.fillStyles);

        indent();
        out.println("<start>");
        indent++;
        printShape(tag.getStartEdges());
        indent--;
        indent();
        out.println("</start>");

        indent();
        out.println("<end>");
        indent++;
        printShape(tag.getEndEdges());
        indent--;
        indent();
        out.println("</end>");

        close(tag);
    }

    public void dumpDefineFont2(DefineFont2Tag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        out.print(" font=\"" + escape(tag.getFontName()) + "\"");
        out.print(" numGlyphs=\"" + tag.getNumGlyphs() + "\"");
        out.print(" italic=\"" + tag.isFontFlagsItalic() + "\"");
        out.print(" bold=\"" + tag.isFontFlagsBold() + "\"");
        out.print(" ansi=\"" + tag.isFontFlagsANSI() + "\"");
        out.print(" wideOffsets=\"" + tag.isFontFlagsWideCodes() + "\"");
        out.print(" wideCodes=\"" + tag.isFontFlagsWideCodes() + "\"");
        out.print(" shiftJIS=\"" + tag.isFontFlagsShiftJIS() + "\"");
        out.print(" langCode=\"" + tag.getLanguageCode() + "\"");
        out.print(" hasLayout=\"" + tag.isFontFlagsHasLayout() + "\"");
        out.print(" ascent=\"" + tag.getFontAscent() + "\"");
        out.print(" descent=\"" + tag.getFontDescent() + "\"");
        out.print(" leading=\"" + tag.getFontLeading() + "\"");
        out.print(" kerningCount=\"" + tag.getKerningCount() + "\"");

        out.print(" codepointCount=\"" + tag.getCodeTable().length + "\"");

        if (tag.isFontFlagsHasLayout())
        {
            out.print(" advanceCount=\"" + tag.getFontAdvanceTable().length + "\"");
            out.print(" boundsCount=\"" + tag.getFontBoundsTable().length + "\"");
        }
        end();

        if (glyphs && tag.isFontFlagsHasLayout())
        {
            for (int i = 0; i < tag.getFontKerningTable().length; i++)
            {
                KerningRecord rec = tag.getFontKerningTable()[i];
                indent();
                out.println("<kerningRecord adjustment=\"" + rec.getAdjustment() +
                            "\" code1=\"" + rec.getCode1() + "\" code2=\"" + rec.getCode2() + "\" />");
            }

            for (int i = 0; i < tag.getGlyphShapeTable().length; i++)
            {
                indent();
                out.print("<glyph");
                out.print(" codepoint=\"" + (tag.getCodeTable()[i]) +
                          (isPrintable((char)tag.getCodeTable()[i]) ? escape(("(" + (char)tag.getCodeTable()[i] + ")")) : "(?)") + "\"");
                if (tag.isFontFlagsHasLayout())
                {
                    out.print(" advance=\"" + tag.getFontAdvanceTable()[i] + "\"");
                    out.print(" bounds=\"" + tag.getFontBoundsTable()[i] + "\"");
                }
                out.println(">");

                Shape shape = tag.getGlyphShapeTable()[i];
                indent++;
                if (tabbedGlyphs)
                    printShapeWithTabs(shape);
                else
                    printShape(shape);
                indent--;
                indent();
                out.println("</glyph>");
            }
        }

        close(tag);
    }

    public void dumpDefineFont3(DefineFont3Tag tag)
    {
        dumpDefineFont2(tag);
    }

    public void dumpDefineFont4(DefineFont4Tag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        out.print(" font=\"" + escape(tag.getFontName()) + "\"");
        out.print(" hasFontData=\"" + tag.isFontFlagsHasFontData() + "\"");
        out.print(" italic=\"" + tag.isFontFlagsItalic() + "\"");
        out.print(" bold=\"" + tag.isFontFlagsBold() + "\"");
        end();

        if (glyphs && tag.isFontFlagsHasFontData())
        {
            outputBase64(tag.getFontData());
        }

        close(tag);
    }

    public void dumpDefineFontAlignZones(DefineFontAlignZonesTag tag)
    {
        open(tag);
        out.print(" fontID=\"" + id(tag.getFontTag()) + "\"");
        out.print(" CSMTableHint=\"" + tag.getCsmTableHint() + "\"");
        out.println(">");
        indent++;
        indent();
        out.println("<ZoneTable length=\"" + tag.getZoneTable().length + "\">");
        indent++;
        if (glyphs)
        {
            for (int i = 0; i < tag.getZoneTable().length; i++)
            {
                ZoneRecord record = tag.getZoneTable()[i];
                indent();
                out.print("<ZoneRecord num=\"" + record.getNumZoneData() +
                          "\" maskX=\"" + record.isZoneMaskX() +
                          "\" maskY=\"" + record.isZoneMaskY() +
                          "\">");
                out.print(record.getZoneData0().getData() + " " +
                          record.getZoneData1().getData() + " ");
                out.println("</ZoneRecord>");
            }
        }
        indent--;
        indent();
        out.println("</ZoneTable>");
        close(tag);
    }

    public void dumpCSMTextSettings(CSMTextSettingsTag tag)
    {
        open(tag);
        String textID = tag.getTextTag() == null ? "0" : id(tag.getTextTag());
        out.print(" textID=\"" + textID + "\"");
        out.print(" useFlashType=\"" + tag.getUseFlashType() + "\"");
        out.print(" gridFitType=\"" + tag.getGridFit() + "\"");
        out.print(" thickness=\"" + tag.getThickness() + "\"");
        out.print(" sharpness=\"" + tag.getSharpness() + "\"");
        close();
    }

    public void dumpDefineFontName(DefineFontNameTag tag)
    {
        open(tag);
        out.print(" fontID=\"" + id(tag.getFontTag()) + "\"");
        if (tag.getFontName() != null)
        {
            out.print(" name=\"" + tag.getFontName() + "\"");
        }
        if (tag.getFontCopyright() != null)
        {
            out.print(" copyright=\"" + escape(tag.getFontCopyright()) + "\"");
        }

        close();
    }

    private boolean isPrintable(char c)
    {
        int i = c & 0xFFFF;
        if (i < ' ' || i == '<' || i == '&' || i == '\'')
            return false;
        else
            return true;
    }

    public void dumpExportAssets(ExportAssetsTag tag)
    {
        open(tag);
        end();

        for (String name : tag.getCharacterNames())
        {
            indent();
            out.println("<Export idref=\"" + tag.getCharacterTagByName(name).getCharacterID() +
                        "\" name=\"" + name + "\" />");
        }

        close(tag);
    }

    public void dumpSymbolClass(SymbolClassTag tag)
    {
        open(tag);
        end();

        for (String symbolName : tag.getSymbolNames())
        {
            indent();
            out.println("<Symbol idref=\"" + tag.getSymbol(symbolName).getCharacterID() +
                        "\" className=\"" + symbolName + "\" />");
        }

        if (currentFrameIndex == 0 && swf.getTopLevelClass() != null)
        {
            indent();
            out.println("<Symbol idref=\"0\" className=\"" + swf.getTopLevelClass() + "\" />");
        }

        close(tag);
    }

    // TODO: handle ImportAssets tag.
    //    public void dumpImportAssets(ImportAssets tag)
    //    {
    //        open(tag);
    //        out.print(" url=\"" + tag.url + "\"");
    //        end();
    //        
    //        Iterator it = tag.importRecords.iterator();
    //        while (it.hasNext())
    //        {
    //            ImportRecord record = (ImportRecord)it.next();
    //            indent();
    //            out.println("<Import name=\"" + record.name + "\" id=\"" + dictionary.getId(record) + "\" />");
    //        }
    //        
    //        close(tag);
    //    }
    //    
    //    public void dumpImportAssets2(ImportAssets tag)
    //    {
    //        // TODO: add support for tag.downloadNow and SHA1...
    //        importAssets(tag);
    //    }

    public void dumpEnableDebugger(EnableDebugger2Tag tag)
    {
        open(tag);
        out.print(" password=\"" + tag.getPassword() + "\"");
        close();
    }

    // TODO Decode actions

    //    public void dumpDoInitAction(DoInitAction tag)
    //    {
    //        if (tag.sprite != null && tag.sprite.name != null)
    //        {
    //            indent();
    //            out.println("<!-- init " + tag.sprite.name + " " + dictionary.getId(tag.sprite) + " -->");
    //        }
    //        
    //        open(tag);
    //        if (tag.sprite != null)
    //            out.print(" idref=\"" + idRef(tag.sprite) + "\"");
    //        end();
    //        
    //        if (showActions)
    //        {
    //            openCDATA();
    //            printActions(tag.actionList);
    //            closeCDATA();
    //        }
    //        else
    //        {
    //            indent();
    //            out.println("<!-- " + tag.actionList.size() + " action(s) elided -->");
    //        }
    //        close(tag);
    //    }

    public void dumpEnableTelemetry(EnableTelemetryTag tag)
    {
        open(tag);
        out.print(" password=\"" + tag.getPassword() + "\"");
        close();
    }

    private String idRef(ICharacterTag tag)
    {
        if (tag == null)
        {
            // if tag is null then it isn't in the dict -- the SWF is invalid.
            // lets be lax and print something; Matador generates invalid SWF sometimes.
            return "-1";
        }
        else if (currentFrame == null || currentFrame.getSymbolName(tag) == null)
        {
            // just print the character id since no name was exported
            return String.valueOf(id(tag));
        }
        else
        {
            return currentFrame.getSymbolName(tag);
        }
    }

    public void dumpDefineVideoStream(DefineVideoStreamTag tag)
    {
        open(tag);
        out.print(" id=\"" + id(tag) + "\"");
        close();
    }

    public void dumpVideoFrame(VideoFrameTag tag)
    {
        open(tag);
        out.print(" streamId=\"" + idRef(tag.getStreamTag()) + "\"");
        out.print(" frame=\"" + tag.getFrameNum() + "\"");
        close();
    }

    public void dumpDefineFontInfo2(DefineFontInfoTag tag)
    {
        dumpDefineFontInfo(tag);
    }

    public void dumpEnableDebugger2(EnableDebugger2Tag tag)
    {
        open(tag);
        out.print(" password=\"" + tag.getPassword() + "\"");
        out.print(" reserved=\"0x" + Integer.toHexString(EnableDebugger2Tag.RESERVED_FIELD_VALUE) + "\"");
        close();
    }

    // TODO: handle DebugID tag
    //    public void dumpDebugID(DebugID tag)
    //    {
    //        open(tag);
    //        out.print(" uuid=\"" + tag.uuid + "\"");
    //        close();
    //    }

    public void dumpScriptLimits(ScriptLimitsTag tag)
    {
        open(tag);
        out.print(" scriptRecursionLimit=\"" + tag.getMaxRecursionDepth() + "\"" +
                  " scriptTimeLimit=\"" + tag.getScriptTimeoutSeconds() + "\"");
        close();
    }

    public void dumpSetTabIndex(SetTabIndexTag tag)
    {
        open(tag);
        out.print(" depth=\"" + tag.getDepth() + "\"");
        out.print(" index=\"" + tag.getTabIndex() + "\"");
        close();
    }

    public void dumpDoABC(DoABCTag tag)
    {
        if (abc)
        {
            open(tag);
            end();
            ABCParser parser = new ABCParser(tag.getABCData());
            parser.verbose = verbose;
            parser.output = out;
            PoolingABCVisitor printer = new ABCDumpVisitor(out, sortOption);
            if (verbose)
            	out.println("doABC for " + tag.getName());
            parser.parseABC(printer);
            close(tag);
        }
        else
        {
            open(tag);
            if (tag.getTagType() == TagType.DoABC)
                out.print(" name=\"" + tag.getName() + "\"");
            close();
        }
    }

    @SuppressWarnings("unused")
    private String hexify(byte[] id)
    {
        StringBuilder b = new StringBuilder(id.length * 2);
        for (int i = 0; i < id.length; i++)
        {
            b.append(Character.forDigit((id[i] >> 4) & 15, 16));
            b.append(Character.forDigit(id[i] & 15, 16));
        }
        return b.toString().toUpperCase();
    }

    public static String baseName(String path)
    {
        int start = path.lastIndexOf(File.separatorChar);

        if (File.separatorChar != '/')
        {
            // some of us are grouchy about unix paths not being
            // parsed since they are totally legit at the system
            // level of win32.
            int altstart = path.lastIndexOf('/');
            if ((start == -1) || (altstart > start))
                start = altstart;
        }

        if (start == -1)
            start = 0;
        else
            ++start;

        int end = path.lastIndexOf('.');

        if (end == -1)
            end = path.length();

        if (start > end)
            end = path.length();

        return path.substring(start, end);

    }

    public static String dirName(String path)
    {
        int end = path.lastIndexOf(File.pathSeparatorChar);

        if (File.pathSeparatorChar != '/')
        {
            // some of us are grouchy about unix paths not being
            // parsed since they are totally legit at the system
            // level of win32.
            int altend = path.lastIndexOf('/');
            if ((end == -1) || (altend < end))
                end = altend;
        }

        if (end == -1)
            return "";
        else
            ++end;

        return path.substring(0, end);
    }

    // options
    public static boolean abcOption = false;
    public static boolean verboseOption = false;
    static boolean encodeOption = false;
    static boolean showActionsOption = true;
    static boolean showOffsetOption = false;
    static boolean showByteCodeOption = false;
    static boolean showDebugSourceOption = false;
    static boolean glyphsOption = true;
    static boolean externalOption = false;
    static boolean decompileOption = true;
    static boolean defuncOption = true;
    static boolean saveOption = false;
    static boolean sortOption = false;
    static boolean tabbedGlyphsOption = true;
    static boolean uncompressOption = false;

    /**
     * SWFDump will dump a SWF file as XML.
     * 
     * Usage: swfdump [-abc] file1.swf
     */
    public static void main(String[] args) throws IOException
    {
        // This message should not be localized.
        System.err.println("Apache Royale SWF Dump Utility");
        System.err.println(VersionInfo.buildMessage());
        System.err.println("");

        if (args.length == 0)
        {
            // TODO: decide which options to implement.
            //            System.err.println("Usage: swfdump [-encode] [-asm] [-abc] [-showbytecode] [-showdebugsource] [-showoffset] [-noglyphs] [-save file.swf] [-nofunctions] [-out file.swfx] file1.swf ...");
            System.err.println("Usage: swfdump [-abc] file1.swf");
            System.exit(1);
        }

        int index = 0;
        PrintWriter out = null;
        String outfile = null;

        while ((index < args.length) && (args[index].startsWith("-")))
        {
            if (args[index].equals("-encode"))
            {
                encodeOption = true;
                ++index;
            }
            else if (args[index].equals("-verbose"))
            {
                ++index;
                verboseOption = true;
            }
            else if (args[index].equals("-save"))
            {
                ++index;
                saveOption = true;
                outfile = args[index++];
            }
            else if (args[index].equals("-sort"))
            {
                // Try to sort output by alpha-order of identifiers
                // so compare of two dumps compare better.
                // There is some randomness in the order of output
                // of some scripts
                ++index;
                sortOption = true;
            }
            else if (args[index].equals("-uncompress"))
            {
                ++index;
                uncompressOption = true;
                outfile = args[index++];
            }
            else if (args[index].equals("-decompile"))
            {
                decompileOption = true;
                ++index;
            }
            else if (args[index].equals("-nofunctions"))
            {
                defuncOption = false;
                ++index;
            }
            else if (args[index].equals("-asm"))
            {
                decompileOption = false;
                ++index;
            }
            else if (args[index].equals("-abc"))
            {
                abcOption = true;
                ++index;
            }
            else if (args[index].equals("-noactions"))
            {
                showActionsOption = false;
                ++index;
            }
            else if (args[index].equals("-showoffset"))
            {
                showOffsetOption = true;
                ++index;
            }
            else if (args[index].equals("-showbytecode"))
            {
                showByteCodeOption = true;
                ++index;
            }
            else if (args[index].equals("-showdebugsource"))
            {
                showDebugSourceOption = true;
                ++index;
            }
            else if (args[index].equals("-noglyphs"))
            {
                glyphsOption = false;
                ++index;
            }
            else if (args[index].equals("-out"))
            {
                if (index + 1 == args.length)
                {
                    System.err.println("-out requires a filename or - for stdout");
                    System.exit(1);
                }
                if (!args[index + 1].equals("-"))
                {

                    outfile = args[index + 1];
                    out = new PrintWriter(outfile, "UTF-8");
                }
                index += 2;
            }
            else if (args[index].equals("-external"))
            {
                externalOption = true;
                ++index;
            }
            else if (args[index].equalsIgnoreCase("-tabbedGlyphs"))
            {
                tabbedGlyphsOption = true;
                ++index;
            }
            else
            {
                System.err.println("unknown argument " + args[index]);
                ++index;
            }
        }

        if (out == null)
            out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true);

        File f = new File(args[index]);
        URL[] urls = new URL[0];
        File currentFile = f;
        try
        {
            if (!f.exists())
            {
                urls = new URL[] {new URL(args[index])};
            }
            else
            {
                if (f.isDirectory())
                {
                    File[] list = listFiles(f);
                    urls = new URL[list.length];
                    for (int i = 0; i < list.length; i++)
                    {
                        currentFile = list[i];
                        urls[i] = toURL(list[i]);
                    }
                }
                else
                {
                    urls = new URL[] {toURL(f)};
                }
            }
        }
        catch (MalformedURLException e)
        {
            System.err.println("Unable to open " + currentFile);
        }

        for (int i = 0; i < urls.length; i++)
        {
            try
            {
                URL url = urls[i];
                if (saveOption)
                {
                    InputStream in = new BufferedInputStream(url.openStream());
                    try
                    {
                        OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outfile));
                        try
                        {
                            int c;
                            while ((c = in.read()) != -1)
                            {
                                fileOut.write(c);
                            }
                        }
                        finally
                        {
                            fileOut.close();
                        }
                    }
                    finally
                    {
                        in.close();
                    }
                }
                if (uncompressOption)
                {
                    final SWFReader swfReader = new SWFReader();
                    final String path = url.getPath();
                    try
                    {
                        SWF swf = (SWF)swfReader.readFrom(
                                new BufferedInputStream(url.openStream()),
                                path);

                        ProblemQuery problemQuery = new ProblemQuery();
                        problemQuery.addAll(swfReader.getProblems());
                        if (!problemQuery.hasErrors())
                        {
                            OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(outfile));
                            SWFWriter swfWriter = new SWFWriter(swf, Header.Compression.NONE);
                            swfWriter.writeTo(fileOut);
                            swfWriter.close();
                        }
                    }
                    finally
                    {
                        IOUtils.closeQuietly(swfReader);
                    }

                }

                if (!uncompressOption)
                    dumpSwf(out, url, outfile);

                out.flush();
            }
            catch (Error e)
            {
                if (Trace.error)
                    e.printStackTrace();

                System.err.println("");
                System.err.println("An unrecoverable error occurred.  The given file " + urls[i] + " may not be");
                System.err.println("a valid swf.");
            }
            catch (FileNotFoundException e)
            {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    public static void dumpSwf(PrintWriter out, URL url, String outfile)
            throws IOException
    {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        if (!sortOption)
            out.println("<!-- Parsing swf " + url + " -->");
        SWFDump swfDump = new SWFDump(out);

        // TODO: Disable options
        //        swfDump.showActions = showActionsOption;
        //        swfDump.showOffset = showOffsetOption;
        //        swfDump.showByteCode = showByteCodeOption;
        //        swfDump.showDebugSource = showDebugSourceOption;
        //        swfDump.glyphs = glyphsOption;
        //        swfDump.setExternal(externalOption, outfile);
        //        swfDump.decompile = decompileOption;
        swfDump.abc = abcOption;
        swfDump.verbose = verboseOption;
        //        swfDump.defunc = defuncOption;
        //        swfDump.tabbedGlyphs = tabbedGlyphsOption;

        swfDump.dump(url);
    }

    public static URL toURL(File f) throws MalformedURLException
    {
        String s = f.getAbsolutePath();
        if (File.separatorChar != '/')
        {
            s = s.replace(File.separatorChar, '/');
        }
        if (!s.startsWith("/"))
        {
            s = "/" + s;
        }
        if (!s.endsWith("/") && f.isDirectory())
        {
            s = s + "/";
        }
        return new URL("file", "", s);
    }

    public static File[] listFiles(File dir)
    {
        String[] fileNames = dir.list();

        if (fileNames == null)
        {
            return null;
        }

        File[] fileList = new File[fileNames.length];
        for (int i = 0; i < fileNames.length; i++)
        {
            fileList[i] = new File(dir.getPath(), fileNames[i]);
        }
        return fileList;
    }

}
