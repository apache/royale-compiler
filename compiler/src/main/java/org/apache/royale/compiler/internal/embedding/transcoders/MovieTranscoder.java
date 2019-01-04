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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.internal.caches.CacheStoreKeyBase;
import org.apache.royale.compiler.internal.caches.SWFCache;
import org.apache.royale.compiler.embedding.EmbedAttribute;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.targets.RoyaleFontInfo;
import org.apache.royale.compiler.internal.targets.TagSorter;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorNodeBase;
import org.apache.royale.compiler.internal.tree.as.BlockNode;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.ConditionalNode;
import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.GetterNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.IfNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.tree.as.LanguageIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.ModifierNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceIdentifierNode;
import org.apache.royale.compiler.internal.tree.as.NumericLiteralNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.ReturnNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedAS2TagsModifiedProblem;
import org.apache.royale.compiler.problems.EmbedBadScalingGridTargetProblem;
import org.apache.royale.compiler.problems.EmbedMissingSymbolProblem;
import org.apache.royale.compiler.problems.EmbedMovieScalingNoSymbolProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCManager;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.ISWFConstants;
import org.apache.royale.swf.io.SWFReader;
import org.apache.royale.swf.tags.DefineBitsLosslessTag;
import org.apache.royale.swf.tags.DefineBitsTag;
import org.apache.royale.swf.tags.DefineButtonTag;
import org.apache.royale.swf.tags.DefineFont2Tag;
import org.apache.royale.swf.tags.DefineFont4Tag;
import org.apache.royale.swf.tags.DefineFontTag;
import org.apache.royale.swf.tags.DefineScalingGridTag;
import org.apache.royale.swf.tags.DefineSoundTag;
import org.apache.royale.swf.tags.DefineSpriteTag;
import org.apache.royale.swf.tags.DefineTextTag;
import org.apache.royale.swf.tags.ExportAssetsTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.swf.tags.PlaceObject2Tag;
import org.apache.royale.swf.tags.SymbolClassTag;
import org.apache.royale.swf.types.Rect;

public class MovieTranscoder extends ScalableTranscoder
{
    /**
     * Constructor.
     * 
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public MovieTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
        this.symbol = null;
        this.symbolTag = null;
        this.royaleFontInfo = null;
        this.swfWidth = 0;
        this.swfHeight = 0;
    }

    private String symbol;
    private ICharacterTag symbolTag;
    private RoyaleFontInfo royaleFontInfo;
    private int swfWidth;
    private int swfHeight;

    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);
        if (!result)
            return false;

        SWFReader swfReader = getSWFReader(problems);
        if (swfReader == null)
            return false;

        ISWF swf = swfReader.getSWF();
        if (swf == null)
            return false;

        if (symbol == null)
        {
            baseClassQName = CORE_PACKAGE + ".MovieClipLoaderAsset";

            if (scaling)
            {
                problems.add(new EmbedMovieScalingNoSymbolProblem(location));
            }
        }
        else
        {
            symbolTag = getSWFTag(swfReader, symbol, problems);
            if (symbolTag == null)
            {
                problems.add(new EmbedMissingSymbolProblem(location, source, symbol));
                return false;
            }

            if (scaling)
            {
                if (!(symbolTag instanceof DefineSpriteTag))
                {
                    problems.add(new EmbedBadScalingGridTargetProblem(location, symbol));
                    return false;
                }
            }

            if (swf.getFrameCount() > 1)
                baseClassQName = CORE_PACKAGE + ".MovieClipLoaderAsset";
            else
                baseClassQName = getAssociatedClass(symbolTag);

            if (symbolTag instanceof DefineFont2Tag)
            {
                royaleFontInfo = new RoyaleFontInfo(((DefineFont2Tag)symbolTag).isFontFlagsBold(), ((DefineFont2Tag)symbolTag).isFontFlagsItalic());
            }
            else if (symbolTag instanceof DefineFont4Tag)
            {
                royaleFontInfo = new RoyaleFontInfo(((DefineFont4Tag)symbolTag).isFontFlagsBold(), ((DefineFont4Tag)symbolTag).isFontFlagsItalic());                
            }
        }

        Rect swfSize = swf.getFrameSize();
        if (swfSize != null)
        {
            swfWidth =  swfSize.getWidth() / ISWFConstants.TWIPS_PER_PIXEL;
            swfHeight = swfSize.getHeight() / ISWFConstants.TWIPS_PER_PIXEL;
        }
        else
        {
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
            case SYMBOL:
                symbol = (String)data.getAttribute(EmbedAttribute.SYMBOL);
                break;
            default:
                isSupported = super.setAttribute(attribute);
        }

        return isSupported;
    }

    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        ICharacterTag assetTag = null;
        String symbolName;
        if (symbolTag != null)
        {
            symbolName = data.getQName();
            Set<ITag> sortedTags = new LinkedHashSet<ITag>();
            sortedTags.addAll(TagSorter.sortFullGraph(Collections.<ITag>singletonList(symbolTag)));

            if (scaling)
            {
                tags.addAll(sortedTags);
                assetTag = scaleExistingSprite((DefineSpriteTag)symbolTag, tags, problems);
            }
            else
            {
                sortedTags.remove(symbolTag);
                tags.addAll(sortedTags);
                assetTag = symbolTag;
            }
        }
        else
        {
            symbolName = data.getQName() + byteArrayNamePostfix;

            assetTag = buildBinaryDataTag(problems);
            if (assetTag == null)
                return null;
        }

        Map<String, ICharacterTag> symbolTags = Collections.singletonMap(symbolName, assetTag);
        return symbolTags;
    }

    @Override
    public FileNode buildAST(Collection<ICompilerProblem> problems, String filename)
    {
        // when a symbol has been specified, no need to generate the wrapper class
        if (symbolTag != null)
        {
            return super.buildAST(problems, filename);
        }

        FileNode fileNode = new FileNode(workspace, filename);
        PackageNode packageNode = new PackageNode(new IdentifierNode(""), null);
        fileNode.addItem(packageNode);

        ScopedBlockNode packageContents = packageNode.getScopedNode();
        ImportNode importNode = ImportNode.buildImportNode(getBaseClassQName());
        packageContents.addItem(importNode);
        importNode = ImportNode.buildImportNode("mx.core.ByteArrayAsset");
        packageContents.addItem(importNode);
        importNode = ImportNode.buildImportNode("flash.utils.ByteArray");
        packageContents.addItem(importNode);

        // generate the byte array class
        String byteArrayClassName = data.getQName() + byteArrayNamePostfix;
        ClassNode classNodeByteArray = new ClassNode(new IdentifierNode(byteArrayClassName));
        classNodeByteArray.setBaseClass(new IdentifierNode("ByteArrayAsset"));
        classNodeByteArray.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        packageContents.addItem(classNodeByteArray);

        // generate the movie class
        String movieClassName = data.getQName();
        ClassNode classNodeMovie = new ClassNode(new IdentifierNode(movieClassName));
        classNodeMovie.setBaseClass(new IdentifierNode(getBaseClassName()));
        classNodeMovie.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        packageContents.addItem(classNodeMovie);
        ScopedBlockNode classNodeMovieContents = classNodeMovie.getScopedNode();

        // generate: private static var bytes:ByteArray = null;
        VariableNode variableNodeBytes = new VariableNode(new IdentifierNode("bytes"));
        variableNodeBytes.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.private_));
        variableNodeBytes.addModifier(new ModifierNode(IASKeywordConstants.STATIC));
        variableNodeBytes.setType(null, new IdentifierNode("ByteArray"));
        ASToken assignToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_ASSIGNMENT, -1, -1, -1, -1, "=");
        ASToken nullToken = new ASToken(ASTokenTypes.TOKEN_KEYWORD_NULL, -1, -1, -1, -1, IASKeywordConstants.NULL);
        LiteralNode nullNode = new LiteralNode(LiteralType.NULL, nullToken);
        variableNodeBytes.setAssignedValue(assignToken, nullNode);
        classNodeMovieContents.addItem(variableNodeBytes);

        // build the constructor
        IdentifierNode constructorNameNode = new IdentifierNode(movieClassName);
        constructorNameNode.setReferenceValue(classNodeMovie.getDefinition());
        FunctionNode constructorNode = new FunctionNode(null, constructorNameNode);
        constructorNode.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        ScopedBlockNode constructorContents = constructorNode.getScopedNode();

        // generate: super();
        FunctionCallNode superCall = new FunctionCallNode(LanguageIdentifierNode.buildSuper());
        constructorContents.addItem(superCall);

        // generate: initialWidth = $swfWidth;
        LiteralNode widthNode = new NumericLiteralNode(Integer.toString(swfWidth));
        BinaryOperatorNodeBase assignmentWidth = BinaryOperatorNodeBase.create(assignToken, new IdentifierNode("initialWidth"), widthNode);
        constructorContents.addItem(assignmentWidth);

        // generate: initialHeight = $swfHeight;
        LiteralNode heightNode = new NumericLiteralNode(Integer.toString(swfHeight));
        BinaryOperatorNodeBase assignmentHeight = BinaryOperatorNodeBase.create(assignToken, new IdentifierNode("initialHeight"), heightNode);
        constructorContents.addItem(assignmentHeight);

        classNodeMovieContents.addItem(constructorNode);

        // build the movieClipData() getter
        GetterNode movieClipDataGetterNode = new GetterNode(null, null, new IdentifierNode("movieClipData"));
        movieClipDataGetterNode.addModifier(new ModifierNode(IASKeywordConstants.OVERRIDE));
        movieClipDataGetterNode.setNamespace(new NamespaceIdentifierNode(INamespaceConstants.public_));
        movieClipDataGetterNode.setType(null, new IdentifierNode("ByteArray"));
        ScopedBlockNode movieClipDataContents = movieClipDataGetterNode.getScopedNode();

        // generate: if (bytes == null)
        ASToken compareToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_EQUAL, -1, -1, -1, -1, "==");
        BinaryOperatorNodeBase nullCheck = BinaryOperatorNodeBase.create(compareToken, new IdentifierNode("bytes"), new LiteralNode(LiteralType.NULL, nullToken));
        IfNode ifStmt = new IfNode(null);
        ConditionalNode cNode = new ConditionalNode(null);
        cNode.setConditionalExpression(nullCheck);
        ifStmt.addBranch(cNode);
        movieClipDataContents.addItem(ifStmt);
        BlockNode ifContents = cNode.getContentsNode();

        // generate: bytes = ByteArray(new $assetByteArray());
        ASToken newToken = new ASToken(ASTokenTypes.TOKEN_KEYWORD_NEW, -1, -1, -1, -1, IASKeywordConstants.NEW);
        FunctionCallNode newBytes = new FunctionCallNode(newToken, new IdentifierNode(byteArrayClassName));
        FunctionCallNode byteArrayCall = new FunctionCallNode(new IdentifierNode("ByteArray"));
        ContainerNode args = byteArrayCall.getArgumentsNode();
        args.addItem(newBytes);
        BinaryOperatorNodeBase assignmentBytes = BinaryOperatorNodeBase.create(assignToken, new IdentifierNode("bytes"), byteArrayCall);
        ifContents.addItem(assignmentBytes);

        // generate: return bytes;
        ReturnNode returnStmt = new ReturnNode(null);
        returnStmt.setStatementExpression(new IdentifierNode("bytes"));
        movieClipDataContents.addItem(returnStmt);

        classNodeMovieContents.addItem(movieClipDataGetterNode);

        fileNode.runPostProcess(EnumSet.of(PostProcessStep.POPULATE_SCOPE));

        return fileNode;
    }

    /**
     * Get the symbol name of the embedded SWF, or null if the entire SWF is
     * being embedded.
     * 
     * @return the symbol name, or null if none specified.
     */
    public String getSymbol()
    {
        return symbol;
    }

    /**
     * Get the font info if the SWF being embedded is a font, or null if not
     * a font.
     * 
     * @return RoyaleFontInfo for the font, or null.
     */
    public RoyaleFontInfo getRoyaleFontInfo()
    {
        return royaleFontInfo;
    }

    private SWFReader getSWFReader(Collection<ICompilerProblem> problems)
    {
        
        CacheStoreKeyBase cacheKey;
        ISWCManager swcManager = workspace.getSWCManager();
        if (super.swcSource == null)
            cacheKey = SWFCache.createKey(source);
        else
        {
            final ISWC swc = swcManager.get(new File(super.swcSource.getContainingSWCPath()));
            cacheKey = SWFCache.createKey(swc, source);
        }
        SWFCache swfCache = (SWFCache)workspace.getSWCManager().getSWFCache();
        SWFReader swfReader = (SWFReader)swfCache.get(cacheKey);
        return swfReader;
    }

    private ICharacterTag getSWFTag(SWFReader swfReader, String tagName, Collection<ICompilerProblem> problems)
    {
        ICharacterTag characterTag = null;
        for (ITag tag : swfReader)
        {
            if (tag instanceof SymbolClassTag)
            {
                SymbolClassTag symbolClassTag = (SymbolClassTag)tag;
                characterTag = symbolClassTag.getSymbol(tagName);
                if (characterTag != null)
                    break;
            }
            else if (tag instanceof ExportAssetsTag)
            {
                ExportAssetsTag exportAssetsTag = (ExportAssetsTag)tag;
                characterTag = exportAssetsTag.getCharacterTagByName(tagName);
                if (characterTag != null)
                    break;
            }
        }

        if (characterTag != null)
        {
            boolean tagsModified = modifyTagsForEmbedding(characterTag);
            if (tagsModified)
            {
                problems.add(new EmbedAS2TagsModifiedProblem(symbol));                
            }
        }

        return characterTag;
    }

    /**
     * Certain AS2 tags and actions shouldn't be copied over into
     * AS3 tags, so filter out any unvalid tags/actions here.
     * 
     * @param ICharacterTag tag
     * @return true if tag has been modified
     */
    private boolean modifyTagsForEmbedding(ICharacterTag tag)
    {
        boolean tagsModified = false;
        if (tag instanceof DefineSpriteTag)
        {
            DefineSpriteTag spriteTag = (DefineSpriteTag)tag;
            for (ITag controlTag : spriteTag.getControlTags())
            {
                if (controlTag instanceof PlaceObject2Tag)
                {
                    PlaceObject2Tag placeObject = (PlaceObject2Tag)controlTag;
                    if (placeObject.isHasClipActions())
                    {
                        placeObject.setCharacter(null);
                        tagsModified = true;
                    }
                    if (placeObject.isHasCharacter())
                    {
                        tagsModified |= modifyTagsForEmbedding(placeObject.getCharacter());
                    }
                }
            }
        }
        else if (tag instanceof DefineButtonTag)
        {
            DefineButtonTag buttonTag = (DefineButtonTag)tag;
            byte[] actions = buttonTag.getActions();
            if (actions != null && actions.length > 0)
            {
                buttonTag.setActions(null);
                tagsModified = true;
            }
        }

        return tagsModified;
    }

    private String getAssociatedClass(ICharacterTag tag)
    {
        // default to SpriteAsset
        String associatedClass = CORE_PACKAGE + ".SpriteAsset";

        if (tag instanceof DefineButtonTag)
            associatedClass = CORE_PACKAGE + ".ButtonAsset";
        else if (tag instanceof DefineFontTag || tag instanceof DefineFont4Tag)
            associatedClass = CORE_PACKAGE + ".FontAsset";
        else if (tag instanceof DefineTextTag)
            associatedClass = CORE_PACKAGE + ".TextFieldAsset";
        else if (tag instanceof DefineSoundTag)
            associatedClass = CORE_PACKAGE + ".SoundAsset";
        else if (tag instanceof DefineBitsTag || tag instanceof DefineBitsLosslessTag)
            associatedClass = CORE_PACKAGE + ".BitmapAsset";
        else if (tag instanceof DefineSpriteTag)
            associatedClass = CORE_PACKAGE + ".SpriteAsset";

        return associatedClass;
    }

    private DefineSpriteTag scaleExistingSprite(DefineSpriteTag sprite, Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        DefineScalingGridTag scalingGrid = buildScalingGrid();
        return buildSprite(sprite.getControlTags(), sprite.getFrameCount(), scalingGrid, tags);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!super.equals(o))
            return false;

        if (!(o instanceof MovieTranscoder))
            return false;

        MovieTranscoder t = (MovieTranscoder)o;
        if (!(symbol == null ? t.symbol == null : symbol.equals(t.symbol)))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int hashCode = super.hashCode();

        if (symbol != null)
            hashCode ^= symbol.hashCode();

        return hashCode;
    }
}
