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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedExceptionWhileTranscodingProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.tags.DefineBitsJPEG2Tag;
import org.apache.royale.swf.tags.DefineBitsTag;
import org.apache.royale.swf.tags.DefineScalingGridTag;
import org.apache.royale.swf.tags.DefineShape4Tag;
import org.apache.royale.swf.tags.DefineShapeTag;
import org.apache.royale.swf.tags.DefineSpriteTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.PlaceObject2Tag;
import org.apache.royale.swf.types.FillStyle;
import org.apache.royale.swf.types.FillStyleArray;
import org.apache.royale.swf.types.IFillStyle;
import org.apache.royale.swf.types.LineStyleArray;
import org.apache.royale.swf.types.Matrix;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.ShapeWithStyle;
import org.apache.royale.swf.types.StraightEdgeRecord;
import org.apache.royale.swf.types.StyleChangeRecord;
import org.apache.royale.swf.types.Styles;

/**
 * Handle the embedding of images which don't need to be transcoded
 */
public class ImageTranscoder extends ScalableTranscoder
{
    protected static class ImageInfo
    {
        public ImageInfo(PixelGrabber pixelGrabber)
        {
            this.pixelGrabber = pixelGrabber;
            this.width = pixelGrabber.getWidth();
            this.height = pixelGrabber.getHeight();
            this.widthInTwips = this.width * ISWFConstants.TWIPS_PER_PIXEL;
            this.heightInTwips = this.height * ISWFConstants.TWIPS_PER_PIXEL;
        }

        public final PixelGrabber pixelGrabber;
        public final int width;
        public final int height;
        public final int widthInTwips;
        public final int heightInTwips;
    }

    /**
     * Constructor.
     * 
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public ImageTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
        this.smoothing = false;
    }

    private boolean smoothing;

    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);

        if (scaling || smoothing)
            baseClassQName = CORE_PACKAGE + ".SpriteAsset";
        else
            baseClassQName = CORE_PACKAGE + ".BitmapAsset";

        return result;
    }

    @Override
    protected boolean setAttribute(EmbedAttribute attribute)
    {
        boolean isSupported = true;
        switch (attribute)
        {
            case SMOOTHING:
                smoothing = (Boolean)data.getAttribute(EmbedAttribute.SMOOTHING);
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

        ImageInfo imageInfo = null;
        if (scaling || smoothing)
        {
            try
            {
                imageInfo = getImageInfo(bytes, problems);
            }
            catch (Exception e)
            {
                problems.add(new EmbedExceptionWhileTranscodingProblem(e));
                return null;
            }
        }

        DefineBitsTag image = buildImage(bytes, problems);
        if (image == null)
            return null;

        ICharacterTag assetTag;
        if (scaling)
        {
            assetTag = buildSlicedSprite(image, imageInfo.widthInTwips, imageInfo.heightInTwips, tags, problems);
        }
        else if (smoothing)
        {
            assetTag = buildSmoothingSprite(image, imageInfo.widthInTwips, imageInfo.heightInTwips, tags, problems);
        }
        else
        {
            assetTag = image;
        }

        if (assetTag == null)
            return null;

        Map<String, ICharacterTag> symbolTags = Collections.singletonMap(data.getQName(), (ICharacterTag)assetTag);
        return symbolTags;
    }

    protected ImageInfo getImageInfo(byte[] bytes, Collection<ICompilerProblem> problems) throws Exception
    {
        // TODO This gets the image and width of the image.
        // Need to remove this and come up with a way to get the dimensions
        // without reading the whole file/using AWT.
        // http://bugs.adobe.com/jira/browse/CMP-542
        Image image = Toolkit.getDefaultToolkit().createImage(bytes);
        PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, -1, -1, true);
        pixelGrabber.grabPixels();

        ImageInfo imageInfo = new ImageInfo(pixelGrabber);
        return imageInfo;
    }

    private DefineSpriteTag buildSlicedSprite(DefineBitsTag image, int width, int height, Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        DefineScalingGridTag scalingGrid = buildScalingGrid();
        DefineShapeTag shape = buildSlicedShape(image, scalingGrid.getSplitter(), width, height);
        return buildSprite(image, shape, scalingGrid, tags);
    }

    private DefineSpriteTag buildSmoothingSprite(DefineBitsTag image, int width, int height, Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        DefineShapeTag shape = buildSmoothingShape(image, width, height);
        return buildSprite(image, shape, null, tags);
    }

    private DefineBitsTag buildImage(byte[] bytes, Collection<ICompilerProblem> problems)
    {
        DefineBitsJPEG2Tag tag = new DefineBitsJPEG2Tag();
        tag.setImageData(bytes);
        return tag;
    }

    private DefineSpriteTag buildSprite(DefineBitsTag image, DefineShapeTag shape, DefineScalingGridTag scalingGrid, Collection<ITag> tags)
    {
        tags.add(image);

        Matrix tm = new Matrix();
        tm.setScale(1, 1);

        PlaceObject2Tag po = new PlaceObject2Tag();
        po.setDepth(10);
        po.setCharacter(shape);
        po.setHasCharacter(true);
        po.setMatrix(tm);
        po.setHasMatrix(true);

        List<ITag> spriteTags = new ArrayList<ITag>(1);
        spriteTags.add(po);
        int frameCount = 0;
        DefineSpriteTag sprite = buildSprite(spriteTags, frameCount, scalingGrid, tags);

        tags.add(shape);

        return sprite;   
    }

    private DefineShapeTag buildSlicedShape(DefineBitsTag image, Rect r, int width, int height)
    {
        DefineShape4Tag shape = new DefineShape4Tag();

        Rect bounds = new Rect(0, width, 0, height);
        shape.setShapeBounds(bounds);
        shape.setEdgeBounds(bounds);

        FillStyleArray fillStyles = new FillStyleArray();
        LineStyleArray lineStyles = new LineStyleArray();
        Styles styles = new Styles(fillStyles, lineStyles);
        ShapeWithStyle shapeWithStyle = new ShapeWithStyle(styles);
        shape.setShapes(shapeWithStyle);

        // translate into source bitmap
        Matrix tsm = new Matrix();
        // unity in twips
        tsm.setScale(ISWFConstants.TWIPS_PER_PIXEL, ISWFConstants.TWIPS_PER_PIXEL);

        // 9 identical fillstyles to fool things
        // not sure why this is needed, and why we're indexing into
        // the into, as the values are identical.  Was ported over from hero.
        for (int i = 0; i < 9; ++i)
        {
            FillStyle fs = new FillStyle();
            fs.setBitmapCharacter(image);
            fs.setBitmapMatrix(tsm);
            fs.setFillStyleType(FillStyle.NON_SMOOTHED_REPEATING_BITMAP);
            fillStyles.add(fs);
        }

        int slt = r.xMin();
        int srt = r.xMax();
        int stt = r.yMin();
        int sbt = r.yMax();
        int dxa = slt;
        int dxb = srt - slt;
        int dxc = width - srt;

        int dya = stt;
        int dyb = sbt - stt;
        int dyc = height - sbt;

        StyleChangeRecord startStyle = new StyleChangeRecord();
        startStyle.setMove(0, dya);
        shapeWithStyle.addShapeRecord(startStyle);

        // border
        addEdgesWithFill(styles, shapeWithStyle, new int[][] { {0, -dya}, {dxa, 0}}, 0, 1);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{dxb, 0}}, 0, 2);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] { {dxc, 0}, {0, dya}}, 0, 3);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, dyb}}, 0, 6);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] { {0, dyc}, {-dxc, 0}}, 0, 9);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{-dxb, 0}}, 0, 8);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] { {-dxa, 0}, {0, -dyc}}, 0, 7);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, -dyb}}, 0, 4);

        // down 1
        StyleChangeRecord down1Style = new StyleChangeRecord();
        down1Style.setMove(dxa, 0);
        shapeWithStyle.addShapeRecord(down1Style);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, dya}}, 2, 1);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, dyb}}, 5, 4);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, dyc}}, 8, 7);

        // down 2
        StyleChangeRecord down2Style = new StyleChangeRecord();
        down2Style.setMove(dxa + dxb, 0);
        shapeWithStyle.addShapeRecord(down2Style);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, dya}}, 3, 2);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, dyb}}, 6, 5);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{0, dyc}}, 9, 8);

        // right 1
        StyleChangeRecord right1Style = new StyleChangeRecord();
        right1Style.setMove(0, dya);
        shapeWithStyle.addShapeRecord(right1Style);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{dxa, 0}}, 1, 4);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{dxb, 0}}, 2, 5);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{dxc, 0}}, 3, 6);

        // right 2
        StyleChangeRecord right2Style = new StyleChangeRecord();
        right2Style.setMove(0, dya + dyb);
        shapeWithStyle.addShapeRecord(right2Style);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{dxa, 0}}, 4, 7);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{dxb, 0}}, 5, 8);
        addEdgesWithFill(styles, shapeWithStyle, new int[][] {{dxc, 0}}, 6, 9);

        return shape;
    }

    private DefineShapeTag buildSmoothingShape(DefineBitsTag image, int width, int height)
    {
        DefineShapeTag shape = new DefineShapeTag();

        Rect bounds = new Rect(0, width, 0, height);
        shape.setShapeBounds(bounds);

        final FillStyleArray fillStyles = new FillStyleArray();
        final LineStyleArray lineStyles = new LineStyleArray();
        Styles styles = new Styles(fillStyles, lineStyles);
        ShapeWithStyle shapeWithStyle = new ShapeWithStyle(styles);
        shape.setShapes(shapeWithStyle);

        // translate into source bitmap
        Matrix tsm = new Matrix();
        // unity in twips
        tsm.setScale(ISWFConstants.TWIPS_PER_PIXEL, ISWFConstants.TWIPS_PER_PIXEL);

        FillStyle fs = new FillStyle();
        fs.setBitmapCharacter(image);
        fs.setBitmapMatrix(tsm);
        fs.setFillStyleType(FillStyle.CLIPPED_BITMAP_FILL);
        fillStyles.add(fs);

        StyleChangeRecord startStyle = new StyleChangeRecord();
        // We use fillstyle1, because it matches what FlashAuthoring generates.
        startStyle.setDefinedStyles(null, fs, null, styles);
        startStyle.setMove(width, height);
        shapeWithStyle.addShapeRecord(startStyle);

        // border
        shapeWithStyle.addShapeRecord(new StraightEdgeRecord(-1 * width, 0));
        shapeWithStyle.addShapeRecord(new StraightEdgeRecord(0, -1 * height));
        shapeWithStyle.addShapeRecord(new StraightEdgeRecord(width, 0));
        shapeWithStyle.addShapeRecord(new StraightEdgeRecord(0, height));

        return shape;
    }

    private void addEdgesWithFill(Styles styles, ShapeWithStyle shapeWithStyle, int[][] coords, int left, int right)
    {
        StyleChangeRecord scr = new StyleChangeRecord();
        if ((left != 0) || (right != 0))
        {
            IFillStyle fillStyle0 = null;
            if (left > 0)
            {
                fillStyle0 = styles.getFillStyles().get(left - 1);
            }

            IFillStyle fillStyle1 = null;
            if (right > 0)
            {
                fillStyle1 = styles.getFillStyles().get(right - 1);
            }

            scr.setDefinedStyles(fillStyle0, fillStyle1, null, styles);
        }
        shapeWithStyle.addShapeRecord(scr);

        for (int i = 0; i < coords.length; ++i)
        {
            shapeWithStyle.addShapeRecord(new StraightEdgeRecord(coords[i][0], coords[i][1]));
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
            return false;

        if (!(o instanceof ImageTranscoder))
            return false;

        ImageTranscoder t = (ImageTranscoder)o;
        if (smoothing != t.smoothing)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        hashCode += (smoothing ? 1 : 0);

        return hashCode;
    }
}
