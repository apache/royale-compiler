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

package org.apache.royale.compiler.internal.fxg.swf;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.internal.embedding.transcoders.JPEGTranscoder;
import org.apache.royale.compiler.internal.fxg.dom.AbstractFXGNode;
import org.apache.royale.compiler.internal.fxg.dom.BitmapGraphicNode;
import org.apache.royale.compiler.internal.fxg.dom.IFillNode;
import org.apache.royale.compiler.internal.fxg.dom.fills.BitmapFillNode;
import org.apache.royale.compiler.internal.fxg.dom.types.FillMode;

import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.tags.DefineBitsJPEG2Tag;
import org.apache.royale.swf.tags.DefineBitsLossless2Tag;
import org.apache.royale.swf.tags.DefineBitsLosslessTag;
import org.apache.royale.swf.tags.DefineShape4Tag;
import org.apache.royale.swf.tags.DefineShapeTag;
import org.apache.royale.swf.types.FillStyle;
import org.apache.royale.swf.types.FillStyleArray;
import org.apache.royale.swf.types.LineStyleArray;
import org.apache.royale.swf.types.Matrix;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeRecord;
import org.apache.royale.swf.types.ShapeWithStyle;
import org.apache.royale.swf.types.StraightEdgeRecord;
import org.apache.royale.swf.types.StyleChangeRecord;
import org.apache.royale.swf.types.Styles;
import org.apache.royale.utils.FileUtils;
import org.apache.royale.utils.Trace;

/**
 * Utilities to help create SWF DefineBits and DefineBitsLossess image tags.
 */
public class ImageHelper
{
    public static final String MIME_GIF = "image/gif";
    public static final String MIME_JPEG = "image/jpeg";
    public static final String MIME_JPG = "image/jpg";
    public static final String MIME_PNG = "image/png";

    /**
     * Creates a rectangle for the given width and height as a DefineShape. The
     * shape is painted with a bitmap FillStyle with the given DefineBits
     * tag. 
     * 
     * @param image The DefineBits tag encoding the image.
     * @param node The BitmapGraphicNode.
      * @return A rectangle of given width and height as a DefineShape with a
     * bitmap fill. 
     */
    public static DefineShapeTag createShapeForImage(DefineImage image, BitmapGraphicNode node)
    {
    	double width = node.width;
    	double height = node.height;
    	boolean repeat = node.repeat;
    	FillMode fillMode = node.fillMode;
    	FXGVersion fileVersion = node.getFileVersion();

        // Use default width/height information if none specified
        if (Double.isNaN(width))
            width = image.getWidth();

        if (Double.isNaN(height))
            height = image.getHeight();

        // Create Fill Style
        Matrix matrix = new Matrix();
        double twx = (ISWFConstants.TWIPS_PER_PIXEL);
        matrix.setScale(twx, twx);
        
        FillStyle fs = null;
        if (fileVersion.equalTo(FXGVersion.v1_0))
        {
        	if (repeat)
        		fs = new FillStyle(FillStyle.REPEATING_BITMAP_FILL, matrix, image.getTag());
        	else
        		fs = new FillStyle(FillStyle.CLIPPED_BITMAP_FILL, matrix, image.getTag());
        }
        else
        {
        	if (fillMode.equals(FillMode.REPEAT))
        	{
        		fs = new FillStyle(FillStyle.REPEATING_BITMAP_FILL, matrix, image.getTag());
        	}
        	else if (fillMode.equals(FillMode.CLIP))
        	{
        		fs = new FillStyle(FillStyle.CLIPPED_BITMAP_FILL, matrix, image.getTag());
        	}
        	else if (fillMode.equals(FillMode.SCALE))
        	{
        		//override the scale for matrix
        	    double fwidth = (width*ISWFConstants.TWIPS_PER_PIXEL)/(double)image.getWidth();
        	    double fheight = (height*ISWFConstants.TWIPS_PER_PIXEL)/(double)image.getHeight();
                
        	    //For consistency with the 4.5.1 snapshot of the flex compiler.
        	    fwidth = ((double)StrictMath.rint(0x10000 * fwidth))/((double)0x10000);
        	    fheight = ((double)StrictMath.rint(0x10000 * fheight))/((double)0x10000);
        	    
        	    matrix.setScale(fwidth, fheight);
        		
        		//fill style does not matter much since the entire area is filled with bitmap
        		fs = new FillStyle(FillStyle.CLIPPED_BITMAP_FILL, matrix, image.getTag());
        	}
        }

        // Apply Fill Styles
        FillStyleArray styleArray = new FillStyleArray();
        styleArray.add(fs);
        LineStyleArray lineStyleArray = new LineStyleArray();
        
        Styles styles = new Styles(styleArray, lineStyleArray);

        ShapeWithStyle sws = new ShapeWithStyle(styles);
        
        // Build Raw SWF Shape
        List<ShapeRecord> shapeRecords = ShapeHelper.rectangle(width, height);
        ShapeHelper.setStyles(shapeRecords, 0, 1, 0, styles);
        sws.addShapeRecords(shapeRecords);
        

        // Wrap up into a SWF DefineShape Tag
        DefineShape4Tag defineShape = new DefineShape4Tag();
        defineShape.setShapeBounds(TypeHelper.rect(width, height));
        defineShape.setEdgeBounds(defineShape.getShapeBounds());
        defineShape.setShapes(sws);

        return defineShape;
    }
    
    /**
     * Determines whether the bitmap image should be clipped. 
     * 
     * @param defImage The  tag encoding the image.
     * @param node The BitmapGraphicNode.
      * @return boolean if bitmap should be clipped. 
     */    
    public static boolean bitmapImageNeedsClipping(DefineImage defImage, BitmapGraphicNode node)
    {
    	if (((node.getFileVersion().equalTo(FXGVersion.v1_0)) && !node.repeat) ||
    			(node.fillMode.equals(FillMode.CLIP)))
    	{
    		if ((defImage.getWidth() < node.width) || (defImage.getHeight() < node.height))
    			return true;
    	}
    	
    	return false;
    	
    }
    
    /**
     * Determines whether the bitmap fill mode is repeat. 
     * 
     * @param node The BitmapFillNode.
     * @return boolean if bitmap should repeat. 
     */    
    public static boolean bitmapFillModeIsRepeat(BitmapFillNode node)
    {
    	if (((node.getFileVersion().equalTo(FXGVersion.v1_0)) && node.repeat) ||
    			(node.fillMode.equals(FillMode.REPEAT)))
    	{
    		return true;
    	}
    	
    	return false;
    	
    }
 
    public static boolean isBitmapFillWithClip(IFillNode fill)
    {
        if (fill == null) 
    		return false;
    	
    	if (fill instanceof BitmapFillNode)
    	{
    		BitmapFillNode bFill = (BitmapFillNode) fill;
    		if (ImageHelper.bitmapFillModeIsRepeat(bFill))
    		{
    			return false;
    		}
    		else
    		{
    			if ((bFill.getFileVersion().equalTo(FXGVersion.v2_0)) && (bFill.fillMode == FillMode.SCALE))
    			{
    				if (Double.isNaN(bFill.scaleX) && Double.isNaN(bFill.scaleY) && 
    						Double.isNaN(bFill.x) && Double.isNaN(bFill.y) &&
    						(Double.isNaN(bFill.rotation) || Math.abs(bFill.rotation) < AbstractFXGNode.EPSILON) &&
    						bFill.matrix == null)
    					return false;
    				else
    					return true;
    			}
    			else
    			{
    				return true;
    			}
    		}    		
    		
    	}
    	return false;
    	
    }

    public static DefineImage createDefineBits(InputStream in, String mimeType) throws IOException
    {
        // TODO: Investigate faster mechanisms of getting image info and pixels
        byte[] bytes = FileUtils.toByteArray(in);
        Image image = getImage(bytes);
        if (mimeType == null)
        {
            throw new IOException("Unsupported MIME type");
        }

        PixelGrabber pixelGrabber = null;
        try
        {
            pixelGrabber = getPixelGrabber(image, null);
        }
        catch (Exception e)
        {
            throw new IOException("Error reading image");
        }

        int width = pixelGrabber.getWidth();
        int height = pixelGrabber.getHeight();

        // JPEG
        if (MIME_JPG.equals(mimeType) || MIME_JPEG.equals(mimeType))
        {
            DefineBitsJPEG2Tag imageTag = new DefineBitsJPEG2Tag();
            imageTag.setImageData(bytes);
            return new DefineImage(imageTag, width, height);
        }
        // PNG or GIF
        else if (MIME_PNG.equals(mimeType) || MIME_GIF.equals(mimeType))
        {
            int[] pixels = (int[])pixelGrabber.getPixels();
            DefineImage defimage = createDefineBitsLossless(pixels, width, height);
            return defimage;
        }
        else
        {
            throw new IOException("Unsupported MIME type: " + mimeType);
        }
    }

    public static DefineImage createDefineBitsLossless(int[] pixels, int width, int height) throws IOException
    {
        DefineBitsLossless2Tag defineBitsLossless = new DefineBitsLossless2Tag();
        defineBitsLossless.setBitmapFormat(DefineBitsLosslessTag.BF_24BIT_RGB_IMAGE);
        defineBitsLossless.setBitmapWidth(width);
        defineBitsLossless.setBitmapHeight(height);

        byte data[] = new byte[pixels.length * 4];

        for (int i = 0; i < pixels.length; i++)
        {
            int offset = i * 4;
            int alpha = (pixels[i] >> 24) & 0xFF;
            data[offset] = (byte)alpha;

            // Premultiply the alpha channel
            if (data[offset] != 0)
            {
                int red = (pixels[i] >> 16) & 0xFF;
                data[offset + 1] = (byte)((red * alpha) / 255);
                int green = (pixels[i] >> 8) & 0xFF;
                data[offset + 2] = (byte)((green * alpha) / 255);
                int blue = pixels[i] & 0xFF;
                data[offset + 3] = (byte)((blue * alpha) / 255);
            }
        }
        defineBitsLossless.setZlibBitmapData(JPEGTranscoder.deflate(data));
        return new DefineImage(defineBitsLossless, width, height);
    }

    public static DefineShapeTag create9SlicedShape(DefineImage bitmap, Rect r, double width, double height)
    {
        // Use default width/height information if none specified
        if (Double.isNaN(width))
            width = bitmap.getWidth();

        if (Double.isNaN(height))
            height = bitmap.getHeight();

        int slt = r.xMin();
        int srt = r.xMax();
        int stt = r.yMin();
        int sbt = r.yMax();

        FillStyleArray fillStyleArray = new FillStyleArray(9);
        LineStyleArray lineStyleArray = new LineStyleArray();
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>(50);

        // Apply runtime scale of 20x for twips
        Matrix matrix = new Matrix();
        
        double twx = (ISWFConstants.TWIPS_PER_PIXEL);
        matrix.setScale(twx, twx);
        
        // Create 9 identical fillstyles as a work around
        for (int i = 0; i < 9; i++)
        {
            FillStyle fs = new FillStyle(FillStyle.NON_SMOOTHED_REPEATING_BITMAP, matrix, bitmap.getTag());
            fillStyleArray.add(fs);
        }
        Styles styles = new Styles(fillStyleArray, lineStyleArray);
        
        int dxa = slt;
        int dxb = srt - slt;
        int dxc = (int)(bitmap.getWidth() * ISWFConstants.TWIPS_PER_PIXEL) - srt;

        int dya = stt;
        int dyb = sbt - stt;
        int dyc = (int)(bitmap.getHeight() * ISWFConstants.TWIPS_PER_PIXEL) - sbt;

        // border
        StyleChangeRecord scr = new StyleChangeRecord();
        scr.setMove(0, dya);
        scr.setDefinedStyles(-1, 1, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, -dya));
        shapeRecords.add(new StraightEdgeRecord(dxa, 0));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(-1, 2, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(dxb, 0));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(-1, 3, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(dxc, 0));
        shapeRecords.add(new StraightEdgeRecord(0, dya));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(-1, 6, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dyb));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(-1, 9, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dyc));
        shapeRecords.add(new StraightEdgeRecord(-dxc, 0));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(-1, 8, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(-dxb, 0));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(-1, 7, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(-dxa, 0));
        shapeRecords.add(new StraightEdgeRecord(0, -dyc));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(-1, 4, -1, styles);
        shapeRecords.add(scr);

        shapeRecords.add(new StraightEdgeRecord(0, -dyb));

        // down 1
        
        scr = new StyleChangeRecord();
        scr.setMove(dxa, 0);
        scr.setDefinedStyles(2, 1, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dya));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(5, 4, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dyb));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(8, 7, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dyc));

        // down 2
        scr = new StyleChangeRecord();
        scr.setMove(dxa + dxb, 0);
        scr.setDefinedStyles(3, 2, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dya));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(6, 5, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dyb));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(9, 8, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(0, dyc));

        // right 1
        scr = new StyleChangeRecord();
        scr.setMove(0, dya);
        scr.setDefinedStyles(1, 4, -1, styles);
        shapeRecords.add(scr);
        
        shapeRecords.add(new StraightEdgeRecord(dxa, 0));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(2, 5, -1, styles);
        shapeRecords.add(scr);

        shapeRecords.add(new StraightEdgeRecord(dxb, 0));
        
        scr = new StyleChangeRecord();
        scr.setDefinedStyles(3, 6, -1, styles);
        shapeRecords.add(scr);

        shapeRecords.add(new StraightEdgeRecord(dxc, 0));

        // right 2        
        scr = new StyleChangeRecord();
        scr.setMove(0, dya + dyb);
        scr.setDefinedStyles(4, 7, -1, styles);
        shapeRecords.add(scr);
        shapeRecords.add(new StraightEdgeRecord(dxa, 0));

        scr = new StyleChangeRecord();
        scr.setDefinedStyles(5, 8, -1, styles);
        shapeRecords.add(scr);

        shapeRecords.add(new StraightEdgeRecord(dxb, 0));

        scr = new StyleChangeRecord();
        scr.setDefinedStyles(6, 9, -1, styles);
        shapeRecords.add(scr);

        shapeRecords.add(new StraightEdgeRecord(dxc, 0));

        ShapeWithStyle sws = new ShapeWithStyle(styles);
        sws.addShapeRecords(shapeRecords);
        
        DefineShape4Tag shape = new DefineShape4Tag();
        shape.setShapeBounds(TypeHelper.rect(width, height));
        shape.setEdgeBounds(shape.getShapeBounds());
        shape.setShapes(sws);
        return shape;
    }

    public static String guessMimeType(String path)
    {
        if (path != null)
        {
            path = path.toLowerCase();
            if (path.endsWith(".png"))
                return MIME_PNG;
            if (path.endsWith(".gif"))
                return MIME_GIF;
            if (path.endsWith(".jpg"))
                return MIME_JPG;
            if (path.endsWith(".jpeg"))
                return MIME_JPEG;
        }

        return null;
    }

    private static Image getImage(byte[] bytes)
    {
        Image image;
        try
    	{
            image = Toolkit.getDefaultToolkit().createImage(bytes);
        }
        catch (InternalError ie)
        {
            if (Trace.error)
            {
                ie.printStackTrace();
            }
            throw new InternalError("An error occurred because there is no graphics environment available.  Please set the headless-server setting in the Flex configuration file to true.");
        }
        catch (NoClassDefFoundError ce)
        {
            if (Trace.error)
            {
                ce.printStackTrace();
            }
            throw new InternalError("An error occurred because there is no graphics environment available.  Please set the headless-server setting in the Flex configuration file to true.");
        }
        return image;
    }

    private static PixelGrabber getPixelGrabber(Image image, String location)
    {
        PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, -1, -1, true);
    
        try
        {
            pixelGrabber.grabPixels();
        }
        catch (InterruptedException interruptedException)
        {
            if (Trace.error)
            {
    	        interruptedException.printStackTrace();
            }
            throw new RuntimeException("Failed to grab pixels for image " + location);
        }
    
        if (((pixelGrabber.getStatus() & ImageObserver.WIDTH) == 0) ||
    		((pixelGrabber.getStatus() & ImageObserver.HEIGHT) == 0))
        {
    	    throw new RuntimeException("Failed to grab pixels for image " + location);
        }
    
        return pixelGrabber;
    }
}
