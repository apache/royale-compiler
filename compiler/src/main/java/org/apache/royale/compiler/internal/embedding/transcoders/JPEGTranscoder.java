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

package org.apache.royale.compiler.internal.embedding.transcoders;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedExceptionWhileTranscodingProblem;
import org.apache.royale.compiler.problems.EmbedQualityRequiresCompressionProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.tags.DefineBitsJPEG3Tag;
import org.apache.royale.swf.tags.DefineBitsTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.utils.DAByteArrayOutputStream;

/**
 * Handle the embedding of images which need to be transcoded
 */
public class JPEGTranscoder extends ImageTranscoder
{
    public JPEGTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
        this.compression = false;
    }

    private boolean compression;
    private Float quality;

    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);
        baseClassQName = CORE_PACKAGE + ".BitmapAsset";
        return result;
    }

    @Override
    protected boolean checkAttributeValues(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.checkAttributeValues(location, problems);
        if (!result)
            return false;

        // quality doesn't make sense without compression
        if (!compression && (quality != null))
        {
            problems.add(new EmbedQualityRequiresCompressionProblem(location));
            result = false;
        }

        return result;
    }

    @Override
    protected boolean setAttribute(EmbedAttribute attribute)
    {
        boolean isSupported = true;
        switch (attribute)
        {
            case COMPRESSION:
                compression = (Boolean)data.getAttribute(EmbedAttribute.COMPRESSION);
                break;
            case QUALITY:
                quality = (Float)data.getAttribute(EmbedAttribute.QUALITY);
                break;
            default:
                isSupported = super.setAttribute(attribute);
        }

        return isSupported;
    }

    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        byte[] bytes = getDataBytes(problems);
        if (bytes == null)
            return null;

        byte[] jpegBytes;
        byte[] compressedAlphaData;
        try
        {
            ImageInfo imageInfo = getImageInfo(bytes, problems);

            int imageSize = imageInfo.width * imageInfo.height;
            byte[] alphaData = new byte[imageSize];
            int[] pixels = (int[])imageInfo.pixelGrabber.getPixels();
            for (int i = 0; i < imageSize; ++i)
            {
                alphaData[i] = (byte)((pixels[i] >> 24) & 0xff);
            }
            // need to compress the alpha data
            compressedAlphaData = deflate(alphaData);

            jpegBytes = bufferedImageToJPEG(imageInfo, pixels);
        }
        catch (Exception e)
        {
            problems.add(new EmbedExceptionWhileTranscodingProblem(e));
            return null;
        }

        DefineBitsTag image = buildImage(jpegBytes, compressedAlphaData);
        if (image == null)
            return null;

        Map<String, ICharacterTag> symbolTags = Collections.singletonMap(data.getQName(), (ICharacterTag)image);
        return symbolTags;
    }

    private byte[] bufferedImageToJPEG(ImageInfo imageInfo, int[] pixels) throws Exception
    {
        BufferedImage bufferedImage = new BufferedImage(imageInfo.width, imageInfo.height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, imageInfo.width, imageInfo.height, pixels, 0, imageInfo.width);

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ColorModel colorModel = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
        ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(colorModel, colorModel.createCompatibleSampleModel(1, 1));
        writeParam.setDestinationType(imageTypeSpecifier);
        writeParam.setSourceBands(new int[] {0, 1, 2});
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

        float q = 1.0f;
        if (quality != null)
            q = quality.floatValue();
        writeParam.setCompressionQuality(q);

        DAByteArrayOutputStream buffer = new DAByteArrayOutputStream();
        writer.setOutput(new MemoryCacheImageOutputStream(buffer));

        IIOImage ioImage = new IIOImage(bufferedImage, null, null);

        writer.write(null, ioImage, writeParam);
        writer.dispose();

    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("JPEGTranscoder waiting for lock in bufferedImageToJPEG");
        byte[] b =  buffer.getDirectByteArray();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("JPEGTranscoder waiting for lock in bufferedImageToJPEG");
    	return b;
    }

    private DefineBitsTag buildImage(byte[] imageBytes, byte[] alphaBytes)
    {
        if (imageBytes == null || alphaBytes == null)
            return null;

        DefineBitsJPEG3Tag tag = new DefineBitsJPEG3Tag();
        tag.setImageData(imageBytes);
        tag.setBitmapAlphaData(alphaBytes);
        tag.setAlphaDataOffset(imageBytes.length);
        return tag;
    }

    public static byte[] deflate(byte[] buf) throws IOException
    {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        DAByteArrayOutputStream out = new DAByteArrayOutputStream();
        DeflaterOutputStream deflaterStream = new DeflaterOutputStream(out, deflater);
        try
        {
            deflaterStream.write(buf, 0, buf.length);
            deflaterStream.finish();
            deflater.end();
        }
        finally
        {
            IOUtils.closeQuietly(deflaterStream);
        }
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("JPEGTranscoder waiting for lock in deflate");
        byte[] b = out.getDirectByteArray();
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
    		System.out.println("JPEGTranscoder waiting for lock in deflate");
    	return b;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
            return false;

        if (!(o instanceof JPEGTranscoder))
            return false;

        JPEGTranscoder t = (JPEGTranscoder)o;
        if ((compression != t.compression) ||
            !quality.equals(t.quality))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode += (compression ? 1 : 0);

        if (quality != null)
            hashCode ^= quality.hashCode();

        return hashCode;
    }
}
