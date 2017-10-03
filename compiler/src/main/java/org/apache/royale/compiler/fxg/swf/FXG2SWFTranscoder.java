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

package org.apache.royale.compiler.fxg.swf;

import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.fxg.FXGConstants;
import org.apache.royale.compiler.fxg.IFXGTranscoder;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.fxg.flex.FXGSymbolClass;
import org.apache.royale.compiler.fxg.resources.IFXGResourceResolver;
import org.apache.royale.compiler.internal.fxg.dom.AbstractShapeNode;
import org.apache.royale.compiler.internal.fxg.dom.BitmapGraphicNode;
import org.apache.royale.compiler.internal.fxg.dom.DefinitionNode;
import org.apache.royale.compiler.internal.fxg.dom.EllipseNode;
import org.apache.royale.compiler.internal.fxg.dom.IFillNode;
import org.apache.royale.compiler.internal.fxg.dom.IFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.GradientEntryNode;
import org.apache.royale.compiler.internal.fxg.dom.GraphicContentNode;
import org.apache.royale.compiler.internal.fxg.dom.GraphicContext;
import org.apache.royale.compiler.internal.fxg.dom.GraphicNode;
import org.apache.royale.compiler.internal.fxg.dom.GroupDefinitionNode;
import org.apache.royale.compiler.internal.fxg.dom.GroupNode;
import org.apache.royale.compiler.internal.fxg.dom.LineNode;
import org.apache.royale.compiler.internal.fxg.dom.IMaskableNode;
import org.apache.royale.compiler.internal.fxg.dom.IMaskingNode;
import org.apache.royale.compiler.internal.fxg.dom.PathNode;
import org.apache.royale.compiler.internal.fxg.dom.PlaceObjectNode;
import org.apache.royale.compiler.internal.fxg.dom.RectNode;
import org.apache.royale.compiler.internal.fxg.dom.RichTextNode;
import org.apache.royale.compiler.internal.fxg.dom.IStrokeNode;
import org.apache.royale.compiler.internal.fxg.dom.TextGraphicNode;
import org.apache.royale.compiler.internal.fxg.dom.fills.BitmapFillNode;
import org.apache.royale.compiler.internal.fxg.dom.fills.LinearGradientFillNode;
import org.apache.royale.compiler.internal.fxg.dom.fills.RadialGradientFillNode;
import org.apache.royale.compiler.internal.fxg.dom.fills.SolidColorFillNode;
import org.apache.royale.compiler.internal.fxg.dom.filters.BevelFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.filters.BlurFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.filters.ColorMatrixFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.filters.DropShadowFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.filters.GlowFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.filters.GradientBevelFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.filters.GradientGlowFilterNode;
import org.apache.royale.compiler.internal.fxg.dom.strokes.AbstractStrokeNode;
import org.apache.royale.compiler.internal.fxg.dom.strokes.LinearGradientStrokeNode;
import org.apache.royale.compiler.internal.fxg.dom.strokes.RadialGradientStrokeNode;
import org.apache.royale.compiler.internal.fxg.dom.strokes.SolidColorStrokeNode;
import org.apache.royale.compiler.internal.fxg.dom.transforms.ColorTransformNode;
import org.apache.royale.compiler.internal.fxg.dom.transforms.MatrixNode;
import org.apache.royale.compiler.internal.fxg.dom.types.BevelType;
import org.apache.royale.compiler.internal.fxg.dom.types.BlendMode;
import org.apache.royale.compiler.internal.fxg.dom.types.Caps;
import org.apache.royale.compiler.internal.fxg.dom.types.InterpolationMethod;
import org.apache.royale.compiler.internal.fxg.dom.types.Joints;
import org.apache.royale.compiler.internal.fxg.dom.types.MaskType;
import org.apache.royale.compiler.internal.fxg.dom.types.ScaleMode;
import org.apache.royale.compiler.internal.fxg.dom.types.ScalingGrid;
import org.apache.royale.compiler.internal.fxg.dom.types.SpreadMethod;
import org.apache.royale.compiler.internal.fxg.dom.types.Winding;
import org.apache.royale.compiler.internal.fxg.swf.DefineImage;
import org.apache.royale.compiler.internal.fxg.swf.ImageHelper;
import org.apache.royale.compiler.internal.fxg.swf.ShapeHelper;
import org.apache.royale.compiler.internal.fxg.swf.TypeHelper;
import org.apache.royale.compiler.internal.fxg.types.FXGMatrix;
import org.apache.royale.compiler.problems.FXGErrorEmbeddingImageProblem;
import org.apache.royale.compiler.problems.FXGMissingAttributeProblem;
import org.apache.royale.compiler.problems.FXGMissingGroupChildNodeProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.builders.IShapeIterator;
import org.apache.royale.swf.builders.ShapeBuilder;
import org.apache.royale.swf.tags.DefineScalingGridTag;
import org.apache.royale.swf.tags.DefineShape4Tag;
import org.apache.royale.swf.tags.DefineShapeTag;
import org.apache.royale.swf.tags.DefineSpriteTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.PlaceObject3Tag;
import org.apache.royale.swf.types.BevelFilter;
import org.apache.royale.swf.types.BlurFilter;
import org.apache.royale.swf.types.CXFormWithAlpha;
import org.apache.royale.swf.types.DropShadowFilter;
import org.apache.royale.swf.types.FillStyle;
import org.apache.royale.swf.types.FillStyleArray;
import org.apache.royale.swf.types.Filter;
import org.apache.royale.swf.types.FocalGradient;
import org.apache.royale.swf.types.GlowFilter;
import org.apache.royale.swf.types.GradRecord;
import org.apache.royale.swf.types.Gradient;
import org.apache.royale.swf.types.GradientBevelFilter;
import org.apache.royale.swf.types.GradientGlowFilter;
import org.apache.royale.swf.types.LineStyle;
import org.apache.royale.swf.types.LineStyle2;
import org.apache.royale.swf.types.LineStyleArray;
import org.apache.royale.swf.types.Matrix;
import org.apache.royale.swf.types.RGB;
import org.apache.royale.swf.types.RGBA;
import org.apache.royale.swf.types.Rect;
import org.apache.royale.swf.types.Shape;
import org.apache.royale.swf.types.ShapeRecord;
import org.apache.royale.swf.types.ShapeWithStyle;
import org.apache.royale.swf.types.Styles;

/**
 * Transcodes an FXG DOM into a tree of SWF DefineSpriteTags which use SWF graphics
 * primitives to draw the document.
 * Note that in this implementation, since FTE based text
 * has no equivalent in SWF tags, text nodes are ignored.
 */
public class FXG2SWFTranscoder implements IFXGTranscoder
{
    protected FXGSymbolClass graphicClass;
    protected HashMap<String, DefineSpriteTag> definitions;
    protected Stack<DefineSpriteTag> spriteStack;
    protected Map<DefineSpriteTag, Integer> depthMap;
    protected IFXGResourceResolver resourceResolver;
    protected Map<ITag, ITag> extraTags;
    protected Map<String, DefineImage> imageMap;
    protected Collection<ICompilerProblem> problems;
    
    public FXG2SWFTranscoder newInstance()
    {
        FXG2SWFTranscoder transcoder = new FXG2SWFTranscoder();;
        transcoder.extraTags = extraTags;
        transcoder.imageMap = imageMap;
        transcoder.depthMap = depthMap;
        return transcoder;
    }
    
    @Override
    public void setResourceResolver(IFXGResourceResolver resolver)
    {
        resourceResolver = resolver;
    }

    protected int getSpriteDepth(DefineSpriteTag sprite)
    {
        if(depthMap.containsKey(sprite))
            return depthMap.get(sprite);
        else
            depthMap.put(sprite, 0);
            return 0;
    }
    
    private void setSpriteDepth(DefineSpriteTag sprite, Integer depth)
    {
        depthMap.put(sprite, depth);
    }
    
    @Override
    public FXGSymbolClass transcode(IFXGNode fxgNode, String packageName, String className, Map<ITag, ITag> extraTags, Collection<ICompilerProblem> problems)
    {
        this.problems = problems;
        this.extraTags = extraTags;
        graphicClass = new FXGSymbolClass();
        graphicClass.setPackageName(packageName);
        graphicClass.setClassName(className);
        
        GraphicNode node = (GraphicNode)fxgNode;
        DefineSpriteTag sprite = createDefineSpriteTag("Graphic");
        spriteStack.push(sprite);
        
        // Process mask (if present)
        if (node.mask != null)
            mask(node, sprite);

        // Handle 'scale 9' grid definition
        if (node.definesScaleGrid())
        {
            DefineScalingGridTag grid = createDefineScalingGridTag(node.getScalingGrid());
            grid.setCharacter(sprite);
            extraTags.put(sprite, grid);
            //sprite.scalingGrid = grid;
        }

        // Process child nodes
        if (node.children != null)
            graphicContentNodes(node.children);

        spriteStack.pop();
        
        graphicClass.setSymbol(sprite);
        return graphicClass;
    }

    public FXG2SWFTranscoder()
    {
        spriteStack = new Stack<DefineSpriteTag>();
        depthMap = new HashMap<DefineSpriteTag, Integer>();
        imageMap = new HashMap<String, DefineImage>();
    }
    

    private PlaceObject3Tag bitmapWithClip(DefineImage defImage, BitmapGraphicNode node)
    {
        GraphicContext context = node.createGraphicContext();

        //process the filters later to avoid masking
        List<IFilterNode> filters = null;
        if (context.filters != null)       
        {   
            filters = context.filters;
            context.filters = null;
            DefineSpriteTag filterSprite = createDefineSpriteTag("MaskFilter");
            spriteStack.push(filterSprite);
        }
        
        DefineSpriteTag imageSprite = createDefineSpriteTag("BitmapGraphic");
        spriteStack.push(imageSprite);
        
        // First, generate the clipping mask
        DefineSpriteTag clipSprite = createDefineSpriteTag("BitmapGraphic_Clip");
        spriteStack.push(clipSprite);
        
        double width = (defImage.getWidth() < node.width) ? defImage.getWidth() : node.width;
        double height = (defImage.getHeight() < node.height) ? defImage.getHeight() : node.height;
        List<ShapeRecord> shapeRecords = ShapeHelper.rectangle(0.0, 0.0, width, height);
        DefineShapeTag clipShape = createDefineShapeTag(null, shapeRecords, new SolidColorFillNode(), null, context.getTransform());
        PlaceObject3Tag(clipShape, new GraphicContext());
        spriteStack.pop();
        
        //place the clipping mask in the imageSprite
        PlaceObject3Tag po3clip = PlaceObject3Tag(clipSprite, context);
        po3clip.setClipDepth(po3clip.getDepth()+1); 
        po3clip.setHasClipDepth(true);
        // Then, process the image
        DefineShapeTag imageShape = ImageHelper.createShapeForImage(defImage, node);
        PlaceObject3Tag(imageShape, context);        
        spriteStack.pop();
        
        PlaceObject3Tag po3 = PlaceObject3Tag(imageSprite, new GraphicContext());
        
        // If filters were not processed, place the topmost sprite in display list and apply filters 
        // This is done to force processing of masks before filters
        if (filters != null)
        {
            DefineSpriteTag sprite = spriteStack.pop();
            GraphicContext gc = new GraphicContext();
            gc.filters = filters;
            PlaceObject3Tag poFilter = PlaceObject3Tag(sprite, gc);
            return poFilter;            
        }
        return po3;
    }
    
    // --------------------------------------------------------------------------
    //
    // Graphic Content Nodes
    //
    // --------------------------------------------------------------------------
    protected PlaceObject3Tag bitmap(BitmapGraphicNode node)
    {
        GraphicContext context = node.createGraphicContext();
        String source = parseSource(node.source);

        if (source == null)
        {
            // Missing source attribute in <BitmapGraphic> or <BitmapFill>.
            problems.add(new FXGMissingAttributeProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), FXGConstants.FXG_SOURCE_ATTRIBUTE, node.getNodeName()));
            return null;
       }
        
        DefineImage imageTag = createDefineBitsTag(node, source);
        if(imageTag == null)
            return null;
        
        if ((node.visible) && (!node.isPartofClipMask))
        {
       
            DefineShapeTag imageShape;
            ScalingGrid scalingGrid = context.scalingGrid;
            if (scalingGrid != null)
            {
                Rect grid = TypeHelper.rect(scalingGrid.scaleGridLeft, scalingGrid.scaleGridTop, scalingGrid.scaleGridRight, scalingGrid.scaleGridBottom);
                imageShape = ImageHelper.create9SlicedShape(imageTag, grid, Double.NaN, Double.NaN);
                PlaceObject3Tag po3 = PlaceObject3Tag(imageShape, context);
                return po3;
            }
            else
            {
            	if (ImageHelper.bitmapImageNeedsClipping(imageTag, node))
            	{
            		PlaceObject3Tag p03 = bitmapWithClip(imageTag, node);
            		return p03;
            	}
            	else
            	{
            		imageShape = ImageHelper.createShapeForImage(imageTag, node);
            		PlaceObject3Tag po3 = PlaceObject3Tag(imageShape, context);
            		return po3;
            	}
            }            
        }
        else
        {
        	if (!ImageHelper.bitmapImageNeedsClipping(imageTag, node))
        	{       		
        	     double width = (Double.isNaN(node.width)) ? imageTag.getWidth() : node.width;
                 double height = (Double.isNaN(node.height)) ? imageTag.getHeight() : node.height;
        		 List<ShapeRecord>  shapeRecords = ShapeHelper.rectangle(0.0, 0.0, width, height);        
        	     DefineShapeTag shape = createDefineShapeTag(null, shapeRecords, new SolidColorFillNode(), null, context.getTransform());
        		 PlaceObject3Tag po3 = PlaceObject3Tag(shape, context);
        		 return po3;
        	}
        	else
        	{
                double width = ((imageTag.getWidth() < node.width) || Double.isNaN(node.width)) ? imageTag.getWidth() : node.width;
                double height = ((imageTag.getHeight() < node.height) || (Double.isNaN(node.height))) ? imageTag.getHeight() : node.height;
       		 	List<ShapeRecord>  shapeRecords = ShapeHelper.rectangle(0.0, 0.0, width, height);        
       	        DefineShapeTag shape = createDefineShapeTag(null, shapeRecords, new SolidColorFillNode(), null, context.getTransform());
       		 	PlaceObject3Tag po3 = PlaceObject3Tag(shape, context);
       		 	return po3;
        	}
        }
    }

    protected void graphicContentNodes(List<GraphicContentNode> nodes)
    {
        if (nodes == null) return;
        Iterator<GraphicContentNode> iterator = nodes.iterator();
        while (iterator.hasNext())
        {
            GraphicContentNode node = iterator.next();
            graphicContentNode(node);
        }
    }

    protected PlaceObject3Tag graphicContentNode(GraphicContentNode node)
    {
        PlaceObject3Tag po3 = null;

    	if (!node.visible)
        {
            ColorTransformNode ct = new ColorTransformNode();
            ct.alphaMultiplier = 0;
            ct.alphaOffset = 0;
            ct.blueMultiplier = 1;
            ct.blueOffset = 0;
            ct.greenMultiplier = 1;
            ct.greenOffset = 0;
            ct.redMultiplier = 1;
            ct.redOffset = 0;
            node.colorTransform = ct;
            
            if (node instanceof AbstractShapeNode) 
            {
                AbstractShapeNode shapeNode = (AbstractShapeNode)node;
                shapeNode.fill = null;
                shapeNode.stroke = null;
            }
            
        }

        if (node instanceof GroupNode)
        {
            group((GroupNode)node);
        }
        else
        {
            if (node.blendMode == BlendMode.AUTO)
                node.blendMode = BlendMode.NORMAL;
            
            // For non-group nodes, we process mask to clip only this shape
            // node. Process the mask first to ensure the depth is correct.
            List<IFilterNode> filters = null;
            if (node.mask != null)
            {
                // Remove the filters from context and process them later to force Flash Player to process the masks first
                if (node.filters != null)
                {   
                    filters = node.filters;
                    node.filters = null;
                    DefineSpriteTag filterSprite = createDefineSpriteTag("MaskFilter");
                    spriteStack.push(filterSprite);
                }
                DefineSpriteTag parentSprite = spriteStack.peek();
                mask(node, parentSprite);
            }
            
            if (node instanceof EllipseNode)
                po3 = ellipse((EllipseNode)node);
            else if (node instanceof LineNode)
                po3 = line((LineNode)node);
            else if (node instanceof PathNode)
                po3 = path((PathNode)node);
            else if (node instanceof RectNode)
                po3 = rect((RectNode)node);
            else if (node instanceof PlaceObjectNode)
                po3 = PlaceObject3TagInstance((PlaceObjectNode)node);
            else if (node instanceof BitmapGraphicNode)
                po3 = bitmap((BitmapGraphicNode)node);
            else if (node instanceof TextGraphicNode)
                po3 = text((TextGraphicNode)node);
            else if (node instanceof RichTextNode)
                po3 = richtext((RichTextNode)node);
            
            // If filters were not processed, place the topmost sprite in display list and apply filters 
            // This is done to force processing of masks before filters
            if (filters != null)
            {
                DefineSpriteTag sprite = spriteStack.pop();
                GraphicContext gc = new GraphicContext();
                gc.filters = filters;
                PlaceObject3Tag poFilter = PlaceObject3Tag(sprite, gc);
                return poFilter;            
            }
        }

        return po3;
    }

    protected PlaceObject3Tag ellipse(EllipseNode node)
    {
        // Note that we will apply node.x and node.y as a translation operation
        // in the PlaceObject3Tag3 Matrix and instead start the shape from the
        // origin (0.0, 0.0).
        Ellipse2D.Double ellipse = new Ellipse2D.Double(0.0, 0.0, node.width, node.height);
        
        ShapeBuilder builder = new ShapeBuilder();
        IShapeIterator iterator = new PathIteratorWrapper(ellipse.getPathIterator(null));
        builder.processShape(iterator);
        Shape shape = builder.build();
 
        return placeDefineShapeTag(node, shape.getShapeRecords(), node.fill, node.stroke, node.createGraphicContext());
    }

    protected PlaceObject3Tag group(GroupNode node)
    {
    	//handle blendMode "auto"
        if (node.blendMode == BlendMode.AUTO)
        {
        	if ((node.alpha == 0) || (node.alpha == 1))
        		node.blendMode = BlendMode.NORMAL;
        	else
        		node.blendMode = BlendMode.LAYER;
        }
        
        DefineSpriteTag groupSprite = createDefineSpriteTag("Group");
        GraphicContext context = node.createGraphicContext();
        
        // Handle 'scale 9' grid definition
        if (node.definesScaleGrid())
        {
            DefineScalingGridTag grid = createDefineScalingGridTag(context.scalingGrid);
            grid.setCharacter(groupSprite);
            extraTags.put(groupSprite,grid);
            //groupSprite.scalingGrid = grid;
        }

        PlaceObject3Tag po3 = PlaceObject3Tag(groupSprite, context);
        spriteStack.push(groupSprite);
        
        // First, process mask (if present)
        List <IFilterNode> filters = null;
        if (node.mask != null)
        {    
            // Remove the filters from context and process them later to force Flash Player to process the masks first
            filters = node.filters;
            if (filters == null)
            {
                List<GraphicContentNode> children = node.children;
                if (children != null)
                {
                    GraphicContentNode gcNode0 = (GraphicContentNode) children.get(0);
                    filters = gcNode0.filters;
                    if (filters != null)
                    {
                        //check if all the nodes share the same filter
                        for (int i = 1; ((i < children.size()) && filters!= null); i++)
                        {
                            GraphicContentNode gcNodeI = (GraphicContentNode) children.get(i);
                            if (gcNodeI.filters != filters)
                                filters = null;
                        }
                    }

                    if (filters != null)
                    {
                        for (int i = 0; (i < children.size()) ; i++)
                        {
                            GraphicContentNode gcNodeI = (GraphicContentNode) children.get(i);
                            gcNodeI.filters = null;
                        }                        
                    }
                    
                }
            }
            else
            {
                node.filters = null;
            }
 
            if (filters != null)       
            {    
                DefineSpriteTag filterSprite = createDefineSpriteTag("MaskFilter");
                spriteStack.push(filterSprite);
            }
            DefineSpriteTag sprite = spriteStack.peek();
            mask(node, sprite);
        }

        // Then process child nodes.
        if (node.children != null)
            graphicContentNodes(node.children);
        
        // If filters were not processed, place the topmost sprite in display list and apply filters 
        // This is done to force processing of masks before filters
        if (filters != null)
        {
            DefineSpriteTag sprite = spriteStack.pop();
            GraphicContext gc = new GraphicContext();
            gc.filters = filters;
            PlaceObject3Tag poFilter = PlaceObject3Tag(sprite, gc);
            return poFilter;            
        }
        spriteStack.pop();
        return po3;
    }

    protected PlaceObject3Tag line(LineNode node)
    {
        List<ShapeRecord> shapeRecords = ShapeHelper.line(node.xFrom, node.yFrom, node.xTo, node.yTo);
        GraphicContext context = node.createGraphicContext();
        PlaceObject3Tag po3 = placeDefineShapeTag(node, shapeRecords, node.fill, node.stroke, context);
        return po3;
    }

    protected PlaceObject3Tag mask(IMaskableNode node, DefineSpriteTag parentSprite)
    {
        PlaceObject3Tag po3 = null;

        IMaskingNode mask = node.getMask();
        if (mask instanceof GroupNode)
        {
            // According to FXG Spec.: The masking element inherits the target 
            // group's coordinate space, as though it were a direct child 
            // element. In the case when mask is inside a shape, it doesn't 
            // automatically inherit the coordinates from the shape node 
            // but inherits from its parent node which is also parent of 
            // the shape node. To fix it, specifically concatenating the 
            // shape node matrix to the masking node matrix.
            if (!(node instanceof GroupNode || node instanceof GraphicNode))
            {
                FXGMatrix nodeMatrix = null;
                MatrixNode matrixNodeShape = ((GraphicContentNode)node).matrix;
                if (matrixNodeShape == null)
                    // Convert shape node's discreet transform attributes to 
                    // matrix.
                    nodeMatrix = FXGMatrix.convertToMatrix(((GraphicContentNode)node).scaleX, ((GraphicContentNode)node).scaleY, ((GraphicContentNode)node).rotation, ((GraphicContentNode)node).x, ((GraphicContentNode)node).y);
                else
                    nodeMatrix = new FXGMatrix(matrixNodeShape);
                // Get masking node matrix.
                MatrixNode matrixNodeMasking = ((GraphicContentNode)mask).matrix;
                // Create a new MatrixNode if the masking node doesn't have one.
                if (matrixNodeMasking == null)
                {
                    // Convert masking node's transform attributes to matrix
                    // so we can concatenate the shape node's matrix to it.
                    ((GraphicContentNode)mask).convertTransformAttrToMatrix(problems);
                    matrixNodeMasking = ((GraphicContentNode)mask).matrix;
                }
                FXGMatrix maskMatrix = new FXGMatrix(matrixNodeMasking);
                // Concatenate the shape node's matrix to the masking node's 
                // matrix.
                maskMatrix.concat(nodeMatrix);
                // Set the masking node's matrix with the concatenated values.
                maskMatrix.setMatrixNodeValue(matrixNodeMasking);
            }
            
            markLeafNodesAsMask(node, (GroupNode) mask);
            po3 = group((GroupNode)mask);
        }
        else if (mask instanceof PlaceObjectNode)
        {
            po3 = PlaceObject3TagInstance((PlaceObjectNode)mask);
        }

        if (po3 != null)
        {
            int clipDepth = 1;
            // If we had a graphic or group, clip the depths for all children.
            if (node instanceof GroupNode)
            {
                GroupNode group = (GroupNode)node;
                if (group.children != null)
                    clipDepth = getSpriteDepth(parentSprite) + group.children.size();
            }
            else if (node instanceof GraphicNode)
            {
                GraphicNode graphic = (GraphicNode)node;
                if (graphic.children != null)
                    clipDepth = getSpriteDepth(parentSprite) + graphic.children.size();
            }
            // ... otherwise, just clip the shape itself.
            else
            {//TODO
                clipDepth = po3.getDepth() + 1;
            }

            po3.setClipDepth(clipDepth);
            po3.setHasClipDepth(true);
            if (node.getMaskType() == MaskType.ALPHA)
            {
                po3.setHasCacheAsBitmap(true);
            }
        }

        return po3;
    }

    protected PlaceObject3Tag path(PathNode node)
    {
        List<ShapeRecord> shapeRecords = ShapeHelper.path(node, (node.fill != null), problems);
        GraphicContext context = node.createGraphicContext();
        Winding winding[] = new Winding[1];
        winding[0] = node.winding;
        PlaceObject3Tag po3 = placeDefineShapeTag(node, shapeRecords, node.fill, node.stroke, context, winding);
        return po3;
    }

    protected void setPixelBenderBlendMode(PlaceObject3Tag po, BlendMode blendMode)
    {
        
    }
    
    protected void setAlphaMask(PlaceObject3Tag po)
    {
        po.setHasCacheAsBitmap(true);
    }
    
    protected void setLuminosityMask(PlaceObject3Tag po)
    {
       
    }
    
    protected PlaceObject3Tag PlaceObject3Tag(ICharacterTag symbol, GraphicContext context)
    {
        DefineSpriteTag sprite = spriteStack.peek();

        PlaceObject3Tag po3 = new PlaceObject3Tag();
        // po3.setName(name);
        po3.setCharacter(symbol);
        assert symbol != sprite;
        po3.setHasCharacter(true);
        Integer depthCount = getSpriteDepth(sprite) + 1;
        
        setSpriteDepth(sprite, depthCount);
        
        po3.setDepth(depthCount);
        if (context.blendMode != null)
        {
            if (!context.blendMode.needsPixelBenderSupport())
            {
                int blendMode = createBlendMode(context.blendMode);
                po3.setBlendMode(blendMode);
                po3.setHasBlendMode(true);
            }
            else
            {
                setPixelBenderBlendMode(po3, context.blendMode);
            }
        }
        
        if (context.filters != null)
        {
            List<Filter> filters = createFilters(context.filters);
            Filter filterArray[] = {};
            po3.setSurfaceFilterList(filters.toArray(filterArray));
            po3.setHasFilterList(true);
        }

        // FXG angles are always clockwise.
        Matrix matrix = context.getTransform().toSWFMatrix();
        po3.setMatrix(matrix);
        po3.setHasMatrix(true);
        if (context.colorTransform != null)
        {
            ColorTransformNode t = context.colorTransform;
            CXFormWithAlpha cx = TypeHelper.cxFormWithAlpha(t.alphaMultiplier, t.redMultiplier, t.greenMultiplier, t.blueMultiplier, t.alphaOffset, t.redOffset, t.greenOffset, t.blueOffset);
            po3.setColorTransform(cx);
            po3.setHasColorTransform(true);
        }

        
        if (context.maskType == MaskType.ALPHA)
        {
            setAlphaMask(po3);
        }
        else if (context.maskType == MaskType.LUMINOSITY)
        {
            setLuminosityMask(po3);
        }
        sprite.getControlTags().add(po3);
        return po3;
    }


    protected PlaceObject3Tag rect(RectNode node)
    {
        // Note that we will apply node.x and node.y as a translation operation
        // in the PlaceObject3Tag3 Matrix and instead start the shape from the
        // origin (0.0, 0.0).
        GraphicContext context = node.createGraphicContext();
        List<ShapeRecord> shapeRecords;
        if (node.radiusX != 0.0 || node.radiusY != 0.0 
        		|| !Double.isNaN(node.topLeftRadiusX) || !Double.isNaN(node.topLeftRadiusY)
        		|| !Double.isNaN(node.topRightRadiusX) || !Double.isNaN(node.topRightRadiusY)
        		|| !Double.isNaN(node.bottomLeftRadiusX) || !Double.isNaN(node.bottomLeftRadiusY)
        		|| !Double.isNaN(node.bottomRightRadiusX) || !Double.isNaN(node.bottomRightRadiusY))
        {
             shapeRecords  = ShapeHelper.rectangle(0.0, 0.0, node.width, node.height, 
            		 node.radiusX, node.radiusY, node.topLeftRadiusX, node.topLeftRadiusY,
            		 node.topRightRadiusX, node.topRightRadiusY, node.bottomLeftRadiusX, node.bottomLeftRadiusY,
            		 node.bottomRightRadiusX, node.bottomRightRadiusY);
        }
        else
        {
             shapeRecords = ShapeHelper.rectangle(0.0, 0.0, node.width, node.height);
        }
        
        PlaceObject3Tag po3 = placeDefineShapeTag(node, shapeRecords, node.fill, node.stroke, context);
        return po3;
    }

    protected PlaceObject3Tag text(TextGraphicNode node)
    {
        // No operation - text is ignored in this implementation.
        return null;
    }

    protected PlaceObject3Tag richtext(RichTextNode node)
    {
        // No operation - richtext is ignored in this implementation.
        return null;
    }

    // --------------------------------------------------------------------------
    //
    // FXG Library Definitions
    //
    // --------------------------------------------------------------------------

    protected PlaceObject3Tag PlaceObject3TagInstance(PlaceObjectNode node)
    {
        String definitionName = node.getNodeName();

        if (definitions == null)
            definitions = new HashMap<String, DefineSpriteTag>();

        DefineSpriteTag definitionSprite = definitions.get(definitionName);
        if (definitionSprite == null)
        {
            definitionSprite = createDefineSpriteTag("Definition");
            FXG2SWFTranscoder graphics = newInstance();
            graphics.setResourceResolver(resourceResolver);
            definitions.put(definitionName, definitionSprite);
            graphics.definitions = definitions;
            graphics.problems = problems;
            graphics.definition(node.definition, definitionSprite);
        }

        PlaceObject3Tag po3 = PlaceObject3Tag(definitionSprite, node.createGraphicContext());
        return po3;
    }

    protected void definition(DefinitionNode node, DefineSpriteTag definitionSprite)
    {
        GroupDefinitionNode groupDefinition = node.groupDefinition;
        
        if (groupDefinition == null) 
        {
            // Definitions must define a single Group child node.
            problems.add(new FXGMissingGroupChildNodeProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn()));
            return;
        }
        spriteStack.push(definitionSprite);
        
        if (groupDefinition.definesScaleGrid())
        {
            DefineScalingGridTag scalingGrid = createDefineScalingGridTag(groupDefinition.getScalingGrid());
            scalingGrid.setCharacter(definitionSprite);
            extraTags.put(definitionSprite,scalingGrid);
            //definitionSprite.scalingGrid = scalingGrid;
            
        }

        graphicContentNodes(groupDefinition.children);

        spriteStack.pop();
    }

    // --------------------------------------------------------------------------
    //
    // SWF Tags and Types Helper Methods
    //
    // --------------------------------------------------------------------------

    protected DefineImage createDefineBitsTag(IFXGNode node, String source)
    {
        DefineImage imageTag = imageMap.get(source);
        if (imageTag == null)
        {
            try
            {                
                InputStream stream = resourceResolver.openStream(source);
                imageTag = ImageHelper.createDefineBits(stream, ImageHelper.guessMimeType(source));
                imageMap.put(source, imageTag);
            }
            catch (IOException ioe)
            {
                // Error {0} occurred while embedding image {1}.
                problems.add(new FXGErrorEmbeddingImageProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), ioe.getMessage(), source));
                return null;
            }
        }
        return imageTag;
    }

    protected DefineScalingGridTag createDefineScalingGridTag(ScalingGrid grid)
    {
        DefineScalingGridTag scalingGrid = new DefineScalingGridTag();
        scalingGrid.setSplitter(TypeHelper.rect(grid.scaleGridLeft, grid.scaleGridTop, grid.scaleGridRight, grid.scaleGridBottom));
        return scalingGrid;
    }

    protected DefineSpriteTag createDefineSpriteTag(String name)
    {
        DefineSpriteTag sprite = new DefineSpriteTag(0, new ArrayList<ITag>());
        if (name == null) 
            name = "";
        return sprite;
    }
 
    protected DefineShapeTag createDefineShapeTag(AbstractShapeNode node, List<ShapeRecord> shapeRecords, IFillNode fill,
            IStrokeNode stroke, FXGMatrix transform, Winding... windings)
    {
        // Calculate the bounds of the shape outline (without strokes) - edgeBounds
        Rect edgeBounds = (node == null) ? ShapeHelper.getBounds(shapeRecords, null, (AbstractStrokeNode)stroke) : node.getBounds(shapeRecords, null);
        
        
        Rect shapeBounds;

        
        int lineStyleIndex = stroke == null ? 0 : 1;
        int fillStyle0Index = fill == null ? 0 : 1;
        int fillStyle1Index = 0;
        
        FillStyleArray fillStyles = new FillStyleArray(1);
        LineStyleArray lineStyles = new LineStyleArray();
        if (fill != null)
        {
            FillStyle fillStyle = createFillStyle(fill, edgeBounds);
            if(fillStyle != null)
                fillStyles.add(fillStyle);
        }

        if (stroke != null)
        {
        	//find the shapeBounds with stroke
        	LineStyle ls = createGenericLineStyle((AbstractStrokeNode)stroke);
            shapeBounds = (node == null) ? ShapeHelper.getBounds(shapeRecords, ls, (AbstractStrokeNode)stroke) : node.getBounds(shapeRecords, ls);        	

            LineStyle lineStyle = createLineStyle(stroke, shapeBounds);
            lineStyles.add(lineStyle);            
        }
        else
        {
        	shapeBounds = edgeBounds;
        }


        Styles styles = new Styles(fillStyles, lineStyles);


        if (windings.length > 0)
            ShapeHelper.setPathStyles(shapeRecords, lineStyleIndex, fillStyle0Index, fillStyle1Index, styles);
        else
            ShapeHelper.setStyles(shapeRecords, lineStyleIndex, fillStyle0Index, fillStyle1Index, styles);

        
        ShapeWithStyle sws = new ShapeWithStyle(styles);
        sws.addShapeRecords(shapeRecords);
        
        DefineShape4Tag DefineShapeTag4 = new DefineShape4Tag();
        DefineShapeTag4.setShapes(sws);
        DefineShapeTag4.setShapeBounds(shapeBounds);
        DefineShapeTag4.setEdgeBounds(edgeBounds);
        if ((fill != null) &&( windings.length > 0))
        {
        	Winding windingValue = windings[0];
            DefineShapeTag4.setUsesFillWindingRule(windingValue == Winding.NON_ZERO);
        }
        
        return DefineShapeTag4;
    }
    
    
    protected PlaceObject3Tag placeDefineShapeTag(AbstractShapeNode node, List<ShapeRecord> shapeRecords, 
            IFillNode fill, IStrokeNode stroke, GraphicContext context, Winding... windings )
    {
 
        if (node != null && fill!= null && !node.isPartofClipMask && ImageHelper.isBitmapFillWithClip(fill)) 
        {
            /* Support of fillMode=clip/scale is complicated since SWF does not 
             * support proper clipping of bitmaps. For fillMode=clip/scale, FXG defines 
             * the area outside of the bitmap fill area to be transparent.
             * In SWF, the bitmap bleeds to fill the rest of the path/shape 
             * if bitmap is specified to be a clipping bitmap.
             * 
             * In order to get the effect that FXG wants with SWF tags, the
             * the path/shape is split into two ShapeRecords for a path 
             * with a stroke & fill. A clipping mask is applied to the fill 
             * but not the stroke.
             */
            
            BitmapFillNode fillNode = (BitmapFillNode) fill;

            // Calculate the bounds of the shape outline (without strokes)
            Rect edgeBounds = node.getBounds(shapeRecords, null);           
            
            String source = parseSource(fillNode.source);
            if (source == null)
            {
                // Missing source attribute in <BitmapGraphic> or <BitmapFill>.
                problems.add(new FXGMissingAttributeProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), FXGConstants.FXG_SOURCE_ATTRIBUTE, node.getNodeName()));
                return null;
            }
            DefineImage defImage = createDefineBitsTag(fill, source);
            if(defImage == null)
                return null;

            //process the filters later to avoid masking
            List<IFilterNode> filters = null;
            if (context.filters != null)       
            {   
                filters = context.filters;
                context.filters = null;
                DefineSpriteTag filterSprite = createDefineSpriteTag("MaskFilter");
                spriteStack.push(filterSprite);
            }

            DefineSpriteTag imageSprite = createDefineSpriteTag("BitmapFill");
            spriteStack.push(imageSprite);
            
            // First, generate the clipping mask
            DefineSpriteTag clipSprite = createDefineSpriteTag("BitmapFill_Clip");
            spriteStack.push(clipSprite);
            
            List<ShapeRecord> clipRectRecords = ShapeHelper.rectangle(0.0, 0.0, defImage.getWidth(), defImage.getHeight());
            DefineShapeTag clipShape = createDefineShapeTag(null, clipRectRecords, new SolidColorFillNode(), null, context.getTransform());
            FXGMatrix bitmapMatrix = TypeHelper.bitmapFillMatrix(fillNode, defImage, edgeBounds);
            FXGMatrix clipMatrix = new FXGMatrix(bitmapMatrix.a, bitmapMatrix.b, bitmapMatrix.c, bitmapMatrix.d, 0, 0);
            clipMatrix.scale(1.0/ISWFConstants.TWIPS_PER_PIXEL, 1.0/ISWFConstants.TWIPS_PER_PIXEL);
            clipMatrix.translate(bitmapMatrix.tx, bitmapMatrix.ty);
            GraphicContext clipContext = new GraphicContext();
            clipContext.setTransform(clipMatrix);
            PlaceObject3Tag(clipShape, clipContext); 
            spriteStack.pop();
            
            // Set the depth of the mask to that of the bitmap image fill
            clipContext.setTransform(context.getTransform());
            PlaceObject3Tag po3clip = PlaceObject3Tag(clipSprite, clipContext); 
            po3clip.setClipDepth(po3clip.getDepth() + 1);
            po3clip.setHasClipDepth(true);
            
            Styles styles = new Styles(new FillStyleArray(), new LineStyleArray());
            // Then, process the bitmap image fill
            ShapeWithStyle sws = new ShapeWithStyle(styles);
            
            int lineStyleIndex = 0;
            int fillStyle0Index = 1;
            int fillStyle1Index = 0;

            FillStyle fillStyle = createFillStyle(fill, edgeBounds);
            if(fillStyle != null)
                sws.getFillStyles().add(fillStyle);

            if (windings.length > 0)
                ShapeHelper.setPathStyles(shapeRecords, lineStyleIndex, fillStyle0Index, fillStyle1Index, styles);
            else
                ShapeHelper.setStyles(shapeRecords, lineStyleIndex, fillStyle0Index, fillStyle1Index, styles);
            
            sws.addShapeRecords(shapeRecords);
            
            DefineShape4Tag imageShape = new DefineShape4Tag();
            imageShape.setShapes(sws);
            imageShape.setShapeBounds(edgeBounds);
            imageShape.setEdgeBounds(edgeBounds);
            if ((fill != null) &&( windings.length > 0))
            {
                Winding windingValue = windings[0];
                imageShape.setUsesFillWindingRule(windingValue == Winding.NON_ZERO);
            }
            PlaceObject3Tag po3 = PlaceObject3Tag(imageShape, context);        
             
            if (stroke != null)
            {
                //make a copy of ShapeRecord for strokes
                ArrayList<ShapeRecord> shapeRecords2 = new ArrayList<ShapeRecord>(shapeRecords);
                Collections.copy(shapeRecords2, shapeRecords);

                Styles strokeStyles = new Styles(new FillStyleArray(), new LineStyleArray());

                //generate the define sprite for the stroke object with no clipping
                ShapeWithStyle swsStroke = new ShapeWithStyle(strokeStyles);

                // Consider linestyle stroke widths with bounds calculation               
                AbstractStrokeNode strokeNode = (AbstractStrokeNode) stroke;
                LineStyle ls = createGenericLineStyle(strokeNode);
                Rect shapeBounds =  node.getBounds(shapeRecords2, ls);     
                
                LineStyle lineStyle = createLineStyle(stroke, shapeBounds);
                swsStroke.getLineStyles().add(lineStyle);
                
                lineStyleIndex = 1;
                fillStyle0Index = 0;
                fillStyle1Index = 0;
                ShapeHelper.replaceStyles(shapeRecords2, lineStyleIndex, fillStyle0Index, fillStyle1Index, strokeStyles);
         
                swsStroke.addShapeRecords(shapeRecords2);

                DefineShape4Tag strokeShape = new DefineShape4Tag();
                strokeShape.setShapes(swsStroke);
                strokeShape.setShapeBounds(shapeBounds);
                strokeShape.setEdgeBounds(edgeBounds);
                po3 = PlaceObject3Tag(strokeShape, context);    
            }             
            spriteStack.pop();
            
            po3 = PlaceObject3Tag(imageSprite, new GraphicContext());
            
            // If filters were not processed, place the topmost sprite in display list and apply filters 
            // This is done to force processing of masks before filters
            if (filters != null)
            {
                DefineSpriteTag sprite = spriteStack.pop();
                
                GraphicContext gc = new GraphicContext();
                gc.filters = filters;
                PlaceObject3Tag poFilter = PlaceObject3Tag(sprite, gc);
                return poFilter;            
            }
            
            return po3;
            
        }
        else
        {
        	DefineShapeTag shape = createDefineShapeTag(node, shapeRecords, fill, stroke, context.getTransform(), windings);
        	PlaceObject3Tag po3 = PlaceObject3Tag(shape, context);
        	return po3;
        } 
       
    }
    
    protected FillStyle createFillStyle(IFillNode fill, Rect bounds)
     {
        if (fill instanceof SolidColorFillNode)
            return createFillStyle((SolidColorFillNode)fill);
        else if (fill instanceof LinearGradientFillNode)
            return createFillStyle((LinearGradientFillNode)fill, bounds);
        else if (fill instanceof RadialGradientFillNode)
            return createFillStyle((RadialGradientFillNode)fill, bounds);
        else if (fill instanceof BitmapFillNode)
            return createFillStyle((BitmapFillNode)fill, bounds);
        else
            return null;
    }

    protected FillStyle createFillStyle(SolidColorFillNode fill)
    {
        FillStyle fs = new FillStyle();
        fs.setColor(TypeHelper.splitColor(TypeHelper.colorARGB(fill.color, fill.alpha)));
        fs.setFillStyleType(FillStyle.SOLID_FILL);
        return fs;
    }

    protected FillStyle createFillStyle(BitmapFillNode fill, Rect bounds)
    {
        FillStyle fs = new FillStyle();
        
        if (ImageHelper.bitmapFillModeIsRepeat(fill))
            fs.setFillStyleType(FillStyle.REPEATING_BITMAP_FILL);
        else
            fs.setFillStyleType(FillStyle.CLIPPED_BITMAP_FILL);

        String sourceFormatted = parseSource(fill.source);
        
        if (sourceFormatted == null)
        {
            // Source is required after FXG 1.0
            // Missing source attribute in <BitmapGraphic> or <BitmapFill>.
            problems.add(new FXGMissingAttributeProblem(fill.getDocumentPath(), fill.getStartLine(), 
                    fill.getStartColumn(), FXGConstants.FXG_SOURCE_ATTRIBUTE, fill.getNodeName()));
            return null;
        }

        DefineImage img = createDefineBitsTag(fill, sourceFormatted); 
        if(img != null)
        {
            fs.setBitmapCharacter(img.getTag());
        
            fs.setBitmapMatrix(TypeHelper.bitmapFillMatrix(fill, img, bounds).toSWFMatrix());
        }
        return fs;
    }

    protected FillStyle createFillStyle(LinearGradientFillNode node, Rect bounds)
    {
        FillStyle fs = new FillStyle();
        fs.setFillStyleType(FillStyle.LINEAR_GRADIENT_FILL);
        fs.setGradientMatrix(TypeHelper.linearGradientMatrix(node, bounds));
        Gradient gradient = new Gradient();
        populateGradient(gradient, node.entries, node.interpolationMethod, node.spreadMethod);
        fs.setGradient(gradient);

        return fs;
    }

    protected FillStyle createFillStyle(LinearGradientStrokeNode node, Rect bounds)
    {
        FillStyle fs = new FillStyle();
        fs.setFillStyleType(FillStyle.LINEAR_GRADIENT_FILL);
        fs.setGradientMatrix(TypeHelper.linearGradientMatrix(node, bounds));
        Gradient gradient = new Gradient();
        populateGradient(gradient, node.entries, node.interpolationMethod, node.spreadMethod);
        fs.setGradient(gradient);

        return fs;
    }

    protected FillStyle createFillStyle(RadialGradientFillNode node, Rect bounds)
    {
        FillStyle fs = new FillStyle();
        fs.setFillStyleType(FillStyle.FOCAL_RADIAL_GRADIENT_FILL);
        fs.setGradientMatrix(TypeHelper.radialGradientMatrix(node, bounds));
        FocalGradient gradient = new FocalGradient();
        populateGradient(gradient, node.entries, node.interpolationMethod, node.spreadMethod);
        gradient.setFocalPoint((float)node.focalPointRatio);
        fs.setGradient(gradient);

        return fs;
    }

    protected FillStyle createFillStyle(RadialGradientStrokeNode node, Rect bounds)
    {
        FillStyle fs = new FillStyle();
        fs.setFillStyleType(FillStyle.FOCAL_RADIAL_GRADIENT_FILL);
        fs.setGradientMatrix(TypeHelper.radialGradientMatrix(node, bounds));
        FocalGradient gradient = new FocalGradient();
        populateGradient(gradient, node.entries, node.interpolationMethod, node.spreadMethod);
        gradient.setFocalPoint((float)node.focalPointRatio);
        fs.setGradient(gradient);

        return fs;
    }
    
    protected LineStyle createLineStyle(IStrokeNode stroke, Rect bounds)
    {
        if (stroke instanceof SolidColorStrokeNode)
            return createLineStyle((SolidColorStrokeNode)stroke);
        else if (stroke instanceof LinearGradientStrokeNode)
            return createLineStyle((LinearGradientStrokeNode)stroke, bounds);
        else if (stroke instanceof RadialGradientStrokeNode)
            return createLineStyle((RadialGradientStrokeNode)stroke, bounds);
        else
            return null;
    }

    private LineStyle2 createGenericLineStyle(AbstractStrokeNode stroke)
    {
        LineStyle2 ls = new LineStyle2();
        ls.setWidth((int)StrictMath.rint(stroke.getWeight() * ISWFConstants.TWIPS_PER_PIXEL));

        int startCapStyle = createCaps(stroke.caps);
        int endCapStyle = startCapStyle;
        int jointStyle = createJoints(stroke.joints);
        
        boolean noHScaleFlag = stroke.scaleMode == ScaleMode.VERTICAL || stroke.scaleMode == ScaleMode.NONE;
        boolean noVScaleFlag = stroke.scaleMode == ScaleMode.HORIZONTAL || stroke.scaleMode == ScaleMode.NONE;
        
        // The 4.5.1 Flex Compiler switches these two flags.                   
        // A bug has been logged in JIRA against the old compiler for this issue
        // http://bugs.adobe.com/jira/browse/SDK-31114
        ls.setNoHScaleFlag(noHScaleFlag);
        ls.setNoVScaleFlag(noVScaleFlag);
        
        ls.setJoinStyle(jointStyle);
        ls.setStartCapStyle(startCapStyle);
        ls.setEndCapStyle(endCapStyle);
        ls.setPixelHintingFlag(stroke.pixelHinting);
        
        if (jointStyle == 2)
        {
            // Encoded in SWF as an 8.8 fixed point value
            ls.setMiterLimitFactor((float)(stroke.miterLimit));
        }
        
        return ls;
    }
    protected LineStyle createLineStyle(SolidColorStrokeNode stroke)
    {
        LineStyle ls = createGenericLineStyle(stroke);
        ls.setColor(TypeHelper.splitColor(TypeHelper.colorARGB(stroke.color, stroke.alpha)));
        return ls;
    }
    
    protected LineStyle2 createLineStyle(LinearGradientStrokeNode stroke, Rect bounds)
    {
        LineStyle2 ls = createGenericLineStyle(stroke);
        ls.setFillType(createFillStyle(stroke, bounds));
        ls.setHasFillFlag(true);
        return ls;
    }

    protected LineStyle2 createLineStyle(RadialGradientStrokeNode stroke, Rect edgeBounds)
    {
        LineStyle2 ls = createGenericLineStyle(stroke);
        ls.setFillType(createFillStyle(stroke, edgeBounds));
        ls.setHasFillFlag(true);
        return ls;
    }

    protected int createCaps(Caps value)
    {
        if (value != null)
            return value.ordinal();
        else
            return Caps.NONE.ordinal();
    }

    protected int createJoints(Joints value)
    {
        if (value != null)
            return value.ordinal();
        else
            return Joints.ROUND.ordinal();
    }

    protected int createSpreadMode(SpreadMethod value)
    {
        return value.ordinal();
    }

    protected int createBlendMode(BlendMode value)
    {
        return value.ordinal();
    }

    protected int createInterpolationMode(InterpolationMethod value)
    {
        return value.ordinal();
    }

    protected List<Filter> createFilters(List<IFilterNode> list)
    {
        List<Filter> filters = new ArrayList<Filter>(list.size());
        Iterator<IFilterNode> iterator = list.iterator();
        while (iterator.hasNext())
        {
            IFilterNode f = iterator.next();
            if (f instanceof BevelFilterNode)
            {
                BevelFilterNode node = (BevelFilterNode)f;
                BevelFilter filter = createBevelFilter(node);
                Filter rec = new Filter();
                rec.setFilterID(Filter.BEVEL);
                rec.setBevelFilter(filter);
                filters.add(rec);
            }
            else if (f instanceof BlurFilterNode)
            {
                BlurFilterNode node = (BlurFilterNode)f;
                BlurFilter filter = createBlurFilter(node);
                Filter rec = new Filter();
                rec.setFilterID(Filter.BLUR);
                rec.setBlurFilter(filter);
                filters.add(rec);
            }
            else if (f instanceof ColorMatrixFilterNode)
            {
                ColorMatrixFilterNode node = (ColorMatrixFilterNode)f;
                Filter rec = new Filter();
                rec.setFilterID(Filter.COLOR_MATRIX);
                rec.setColorMatrixFilter(node.matrix);
                filters.add(rec);
            }
            else if (f instanceof DropShadowFilterNode)
            {
                DropShadowFilterNode node = (DropShadowFilterNode)f;
                DropShadowFilter filter = createDropShadowFilter(node);
                Filter rec = new Filter();
                rec.setFilterID(Filter.DROP_SHADOW);
                rec.setDropShadowFilter(filter);
                filters.add(rec);
            }
            else if (f instanceof GlowFilterNode)
            {
                GlowFilterNode node = (GlowFilterNode)f;
                GlowFilter filter = createGlowFilter(node);
                Filter rec = new Filter();
                rec.setFilterID(Filter.GLOW);
                rec.setGlowFilter(filter);
                filters.add(rec);
            }
            else if (f instanceof GradientBevelFilterNode)
            {
                GradientBevelFilterNode node = (GradientBevelFilterNode)f;
                GradientBevelFilter filter = createGradientBevelFilter(node);
                Filter rec = new Filter();
                rec.setFilterID(Filter.GRADIENT_BEVEL);
                rec.setGradientBevelFilter(filter);
                filters.add(rec);
            }
            else if (f instanceof GradientGlowFilterNode)
            {
                GradientGlowFilterNode node = (GradientGlowFilterNode)f;
                GradientGlowFilter filter = createGradientGlowFilter(node);
                Filter rec = new Filter();
                rec.setFilterID(Filter.GRADIENT_GLOW);
                rec.setGradientGlowFilter(filter);
                filters.add(rec);
            }
        }
        return filters;
    }

    protected BevelFilter createBevelFilter(BevelFilterNode node)
    {
        BevelFilter filter = new BevelFilter();
        filter.setAngle((float)(node.angle*Math.PI/180.0));
        filter.setBlurX((float)node.blurX);
        filter.setBlurY((float)node.blurY);
        filter.setDistance((float)node.distance);
        filter.setStrength((float)node.strength);
        filter.setShadowColor(TypeHelper.splitColor(TypeHelper.colorARGB(node.shadowColor, node.shadowAlpha)));
        filter.setHighlightColor(TypeHelper.splitColor(TypeHelper.colorARGB(node.highlightColor, node.highlightAlpha)));
        
        
        filter.setOnTop(node.type == BevelType.FULL);
        
        filter.setInnerShadow(node.type == BevelType.INNER);
        filter.setPasses(node.quality);
        filter.setKnockout(node.knockout);
        filter.setCompositeSource(true);
        return filter;
    }

    protected BlurFilter createBlurFilter(BlurFilterNode node)
    {
        BlurFilter filter = new BlurFilter();
        filter.setBlurX((float)(node.blurX));
        filter.setBlurY((float)(node.blurY));
        filter.setPasses(node.quality);
        return filter;
    }

    protected DropShadowFilter createDropShadowFilter(DropShadowFilterNode node)
    {
        DropShadowFilter filter = new DropShadowFilter();
        filter.setDropShadowColor(TypeHelper.splitColor(TypeHelper.colorARGB(node.color, node.alpha)));
        filter.setAngle((float)(node.angle*Math.PI/180.0));
        filter.setBlurX((float)(node.blurX));
        filter.setBlurY((float)(node.blurY));
        filter.setDistance((float)(node.distance));
        filter.setStrength((float)(node.strength));
        filter.setPasses(node.quality);
        filter.setCompositeSource(!node.hideObject);
        filter.setKnockout(node.knockout);
        filter.setInnerShadow(node.inner);
        return filter;
    }

    protected GlowFilter createGlowFilter(GlowFilterNode node)
    {
        GlowFilter filter = new GlowFilter();
        filter.setGlowColor(TypeHelper.splitColor(TypeHelper.colorARGB(node.color, node.alpha)));
        filter.setBlurX((float)(node.blurX));
        filter.setBlurY((float)(node.blurY));
        filter.setStrength((float)(node.strength));
        filter.setKnockout(node.knockout);
        filter.setInnerGlow(node.inner);
        filter.setCompositeSource(true);
        filter.setPasses(node.quality);
        
        return filter;
    }

    protected GradientBevelFilter createGradientBevelFilter(
            GradientBevelFilterNode node)
    {
        GradientBevelFilter filter = new GradientBevelFilter();
        if (node.entries != null)
        {
            byte count = (byte)node.entries.size();
            filter.setNumColors(count);
            RGBA gradientColors[] = new RGBA[count];
            int gradientRatios[] = new int[count];
            filter.setGradientColors(gradientColors);
            filter.setGradientRatio(gradientRatios);

            GradRecord[] records = createGradRecords(node.entries);
            for (int i = 0; i < records.length; i++)
            {
                GradRecord record = records[i];
                RGB color = record.getColor();
                if (color instanceof RGBA)
                    gradientColors[i] = (RGBA)color;
                else
                    gradientColors[i] = new RGBA(color.getRed(), color.getGreen(), color.getBlue(), 0xFF);
                gradientRatios[i] = record.getRatio();
            }
        }

        filter.setAngle((float)(node.angle*Math.PI/180.0));
        filter.setBlurX((float)(node.blurX));
        filter.setBlurY((float)(node.blurY));
        filter.setDistance((float)(node.distance));
        filter.setStrength((float)(node.strength));
        filter.setKnockout(node.knockout);
        filter.setPasses(node.quality);
        filter.setCompositeSource(true);
        filter.setInnerShadow(node.type == BevelType.INNER);
        filter.setOnTop(node.type == BevelType.FULL);

        return filter;
    }

    protected GradientGlowFilter createGradientGlowFilter(
            GradientGlowFilterNode node)
    {
        GradientGlowFilter filter = new GradientGlowFilter();

        if (node.entries != null)
        {
            byte count = (byte)node.entries.size();
            filter.setNumColors(count);
            RGBA gradientColors[] = new RGBA[count];
            int gradientRatio[] = new int[count];

            GradRecord[] records = createGradRecords(node.entries);
            for (int i = 0; i < records.length; i++)
            {
                GradRecord record = records[i];
                RGB color = record.getColor();
                if (color instanceof RGBA)
                    gradientColors[i] = (RGBA)color;
                else
                    gradientColors[i] = new RGBA(color.getRed(), color.getGreen(), color.getBlue(), 0xFF);
                gradientRatio[i] = record.getRatio();
            }
            filter.setGradientColors(gradientColors);
            filter.setGradientRatio(gradientRatio);
        }

        filter.setAngle((float)(node.angle*Math.PI/180.0));
        filter.setBlurX((float)node.blurX);
        filter.setBlurY((float)node.blurY);
        filter.setDistance((float)node.distance);
        filter.setStrength((float)node.strength);
        
        filter.setPasses(node.quality);
        filter.setKnockout(node.knockout);
        filter.setInnerGlow(node.inner);
        filter.setCompositeSource(true);

        return filter;
    }

    protected void populateGradient(Gradient gradient,
            List<GradientEntryNode> entries, InterpolationMethod interpolation,
            SpreadMethod spread)
    {
        gradient.setGradientRecords(Arrays.asList(createGradRecords(entries)));

        if (interpolation != null)
            gradient.setInterpolationMode(createInterpolationMode(interpolation));

        if (spread != null)
            gradient.setSpreadMode(createSpreadMode(spread));
    }

    protected GradRecord[] createGradRecords(List<GradientEntryNode> entries)
    {
        int count = entries.size();
        GradRecord[] records = new GradRecord[count];
        double previousRatio = 0.0;
        for (int currentIndex = 0; currentIndex < count; currentIndex++)
        {
            GradientEntryNode entry = entries.get(currentIndex);
            double thisRatio = entry.ratio;

            // Auto-calculate gradient ratio if omitted from an entry.
            if (Double.isNaN(thisRatio))
            {
                // The first ratio is assumed to be 0.0.
                if (currentIndex == 0)
                {
                    thisRatio = 0.0;
                }
                // The last ratio is assumed to be 1.0.
                else if (currentIndex == count - 1)
                {
                    thisRatio = 1.0;
                }
                else
                {
                    // Other omitted ratios are divided evenly between the last
                    // ratio and the next specified ratio (or 1.0 if none).
                    double nextRatio = 1.0;
                    int nextIndex = count - 1;
                    for (int i = currentIndex; i < count; i++)
                    {
                        GradientEntryNode nextEntry = entries.get(i);
                        if (!Double.isNaN(nextEntry.ratio))
                        {
                            nextRatio = nextEntry.ratio;
                            nextIndex = i;
                            break;
                        }
                    }

                    int entryGap = nextIndex - (currentIndex - 1);
                    if (entryGap > 0)
                    {
                        thisRatio = previousRatio + ((nextRatio - previousRatio) / (entryGap));
                    }
                    else
                    {
                        thisRatio = previousRatio;
                    }
                }
            }

            GradRecord record = new GradRecord(TypeHelper.gradientRatio(thisRatio), TypeHelper.splitColor(TypeHelper.colorARGB(entry.color, entry.alpha)));
            records[currentIndex] = record;

            // Remember this ratio as the last one specified
            previousRatio = thisRatio;
        }

        return records;
    }

    protected String parseSource(String source)
    {
        // TODO: Create a standard @Embed() parser.
        if (source != null)
        {
            source = source.trim();

            if (source.startsWith("@Embed("))
            {
                source = source.substring(7).trim();

                if (source.endsWith(")"))
                {
                    source = source.substring(0, source.length() - 1).trim();
                }

                if (source.charAt(0) == '\'' && source.charAt(source.length() - 1) == '\'')
                {
                    source = source.substring(1, source.length() - 1).trim();
                }
            }
        }

        return source;
    }
    
    private void  markLeafNodesAsMask(IMaskableNode maskableNode, GroupNode mask)
    {
        if ((mask == null) || (mask.children == null))
            return;
    	Iterator<GraphicContentNode> iter = mask.children.iterator();
    	while (iter.hasNext()) 
    	{
    		GraphicContentNode gcNode = iter.next();
    		if (gcNode instanceof GroupNode)
    		{
    			markLeafNodesAsMask(maskableNode, (GroupNode) gcNode);
    		}
    		else
    		{
     		    if (maskableNode.getMaskType() == MaskType.CLIP)
    		        gcNode.isPartofClipMask = true; 
    		}
    	}
    }

    @Override
    public ITypeDefinition[] getDependencies()
    {
        return new ITypeDefinition[0];
    }
    
    /**
     * This class wraps a PathIterator and adds a IShapeIterator
     * implemenation.
     */
    private static class PathIteratorWrapper implements IShapeIterator
    {
        private PathIterator pi;

        public PathIteratorWrapper(PathIterator pi)
        {
            this.pi = pi;
        }

        @Override
        public short currentSegment(double[] coords)
        {
            int code = pi.currentSegment(coords);
            return (short) code;
        }

        @Override
        public boolean isDone()
        {
            return pi.isDone();
        }

        @Override
        public void next()
        {
            pi.next();
        }
    }
}
