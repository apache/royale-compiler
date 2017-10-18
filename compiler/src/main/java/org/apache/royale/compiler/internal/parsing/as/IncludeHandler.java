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

package org.apache.royale.compiler.internal.parsing.as;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import org.apache.royale.compiler.common.IFileSpecificationGetter;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLScopeBuilder;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Handler for include processing. Each source file has one
 * {@code IncludeHandler}.
 * <p>
 * {@code IncludeHandler} responsibilities:
 * <ul>
 * <li>Produce a {@link IFileSpecification} from an include statement.</li>
 * <li>Track include chain to prevent cyclic include.</li>
 * <li>Track and store a tree of include relationships.</li>
 * </ul>
 */
public class IncludeHandler
{
    /**
     * Tree node type for storing include relationships.
     */
    protected static final class Tree
    {
        private Tree(String filename, Tree parent)
        {
            this.children = new ArrayList<Tree>();
            this.filename = filename;
            this.parent = parent;
            this.tokenEnd = 0;
        }

        protected final List<Tree> children;
        protected final String filename;
        private final Tree parent;
        private int tokenEnd;

        private Tree addChild(String filename)
        {
            final Tree child = new Tree(filename, this);
            children.add(child);
            return child;
        }

        @Override
        public String toString()
        {
            return MoreObjects
                    .toStringHelper(this)
                    .add("end", tokenEnd)
                    .add("file", new File(filename).getName())
                    .add("children", children.size())
                    .toString();
        }

        private static void dfs(final Tree tree, final Collection<String> result)
        {
            for (final Tree child : tree.children)
            {
                dfs(child, result);
            }
            result.add(tree.filename);
        }

        private static Tree getRoot(final Tree tree)
        {
            if (tree == null)
                return null;
            Tree result = tree;
            while (result.parent != null)
            {
                result = result.parent;
            }
            return result;
        }
    }

    /**
     * Create an {@code IncludeHandler}. The {@code workspace} is used to find a
     * file specification from the workspace file specification pool. If the
     * workspace is null,
     * {@code getFileSpecificationForInclude(String, String)} will always
     * create a new {@link IFileSpecification} result.
     * 
     * @param fileSpecGetter {@link IFileSpecificationGetter} that should be
     * used by the include hander to open included files.
     */
    public IncludeHandler(final IFileSpecificationGetter fileSpecGetter)
    {
        this.fileSpecGetter = fileSpecGetter;
        this.currentNode = null;
        this.absoluteOffset = 0;
        this.offsetCueListBuilder = new ImmutableList.Builder<OffsetCue>();
        this.timeStamp = 0l;
    }

    /**
     * Create an {@code IncludeHandler} with a given starting absolute offset.
     * This is useful for creating MXML AST nodes, because MXML node
     * construction routine doesn't track absolute offsets. Instead, it uses the
     * {@link OffsetLookup} created by {@link MXMLScopeBuilder} to recover the
     * absolute offset, and create a "one-time" {@code IncludeHandler} in order
     * to get the nodes with the same absolute offsets and the definitions.
     * 
     * @param fileSpecGetter {@link IFileSpecificationGetter} that should be
     * used by the include hander to open included files.
     * @param startAbsoluteOffset starting absolute offset
     * @return IncludeHandler
     */
    public static IncludeHandler create(
            final IFileSpecificationGetter fileSpecGetter,
            final int startAbsoluteOffset)
    {
        final IncludeHandler handler = new IncludeHandler(fileSpecGetter);
        handler.absoluteOffset = startAbsoluteOffset;
        return handler;
    }

    /**
     * Create an {@link IncludeHandler} that does not interception of requests
     * to open included files.
     * 
     * @return An {@link IncludeHandler} that does not interception of requests
     * to open included files.
     */
    public static IncludeHandler creatDefaultIncludeHandler()
    {
        return new IncludeHandler(null);
    }

    /**
     * Create an {@code IncludeHandler} with a given starting absolute offset.
     * This is useful for building MXML AST nodes, because MXML node
     * construction routine doesn't track absolute offsets. Instead, it uses the
     * {@link OffsetLookup} in {@link MXMLScopeBuilder} to recover the absolute
     * offset, and mock a "one-time" {@code IncludeHandler} in order to get the
     * nodes with the same absolute offsets and the definitions.
     * 
     * @param sourcePath enter this file
     * @param localOffset starting local offset
     * @param absoluteOffset starting absolute offset
     * @return IncludeHandler IncludeHander
     */
    public static IncludeHandler createForASTBuilding(
            final IFileSpecificationGetter fileSpecGetter,
            final String sourcePath,
            final int localOffset,
            final int absoluteOffset)
    {
        final IncludeHandler handler = new IncludeHandler(fileSpecGetter);
        handler.enterFile(sourcePath);
        handler.currentNode.tokenEnd = localOffset;
        handler.absoluteOffset = absoluteOffset;
        return handler;
    }

    private final IFileSpecificationGetter fileSpecGetter;

    /**
     * A tree of previously included file (including the first main file). This
     * helps detecting cyclic includes.
     * <p>
     * There can't be duplicated filename on the trail from the current node to
     * the tree root.
     */
    private Tree currentNode;

    /**
     * Track the absolute offset of the current lexer session.
     */
    private int absoluteOffset;

    /**
     * An {@code OffsetCue} is created every time {@link #enterFile(String)} or
     * {@link #leaveFile()} is called.
     */
    private final ImmutableList.Builder<OffsetCue> offsetCueListBuilder;

    /**
     * A last modified timestamp on the tree of include files of this current
     * source
     */
    private long timeStamp;

    /**
     * Containing compiler project. This field is null-able.
     */
    private IASProject project;

    private ICompilationUnit compilationUnit;

    /**
     * True if this {@code IncludeHandler} tracks not only {@link ASToken} but
     * also {@link IMXMLUnitData}. This flag tells {@link #onNextToken(ASToken)}
     * not to check whether {@code currentNode.tokenEnd} increases
     * monotonically. Before CMP-1490 is fixed, this is a workaround for
     * CMP-1368.
     */
    private boolean hasMXMLUnits = false;

    /**
     * Helper function to get an {@link IFileSpecification} from a filename
     * 
     * @param path Normalized file path.
     * @return A {@link IFileSpecification} either provided by the workspace or
     * created if none exists. This method never returns null.
     */
    private final IFileSpecification getFileSpec(final String path)
    {
        IFileSpecification results = null;
        if (fileSpecGetter != null)
            results = fileSpecGetter.getFileSpecification(path);

        if (results == null)
            results = new FileSpecification(path);

        return results;
    }

    /**
     * Given an the canonical path of a file that contains an include directive
     * with the specified string, return an {@link IFileSpecification} for the
     * file the include directive references.
     * <p>
     * <ol>
     * <li>If {@code includeString} is an absolute path, a
     * {@code IFileSpecification} for that location will be returned.</li>
     * <li>Otherwise, if this object has a reference to {@code IASProject},
     * the included file path will be resolved firstly in the current directory
     * of the including file, then in each of the source folders on the project
     * until the first existing file is found.</li>
     * <li>If no on-disk file is found for the included file, a
     * {@code IFileSpecification} pointing to the intended default location will
     * be returned.</li>
     * </ol>
     * 
     * @param includer The canonical path of a file that contains the include
     * directive.
     * @param includeString Unquoted string that refers to a file to be
     * included.
     * @return An {@link IFileSpecification} for the included file, or null if
     * can't resolve.
     */
    protected IFileSpecification getFileSpecificationForInclude(String includer, String includeString)
    {
        if (Strings.isNullOrEmpty(includer) || Strings.isNullOrEmpty(includeString))
            return null;

        if (new File(includeString).isAbsolute())
        {
            // If included file path is already absolute, do not try to resolve
            // in any other folder contexts.
            return getFileSpec(FilenameNormalization.normalize(includeString));
        }
        else
        {
            final String includingFolder = FilenameNormalization.normalize(FilenameUtils.getFullPath(includer));
            final File includedFileInDefaultFolder = new File(includingFolder, includeString);
            if (includedFileInDefaultFolder.isFile())
            {
                // Always resolve to the including file's containing source folder first.
                return getFileSpec(FilenameNormalization.normalizeFileToPath(includedFileInDefaultFolder));
            }
            else if (project != null)
            {
                // Try other source folders if "project" was given.
                final String sourceFileFromSourcePath = project.getSourceFileFromSourcePath(includeString);
                if (sourceFileFromSourcePath != null && new File(sourceFileFromSourcePath).isFile())
                    return getFileSpec(sourceFileFromSourcePath);
            }

            /**
             * Can't resolve included file name in all source folders. Return
             * the IFileSpecification intended to resolve in the including
             * file's parent folder. This helps generate reasonable error
             * message in the IDE.
             */
            return getFileSpec(FilenameNormalization.normalizeFileToPath(includedFileInDefaultFolder));
        }
    }

    /**
     * @return Set of all files included by this file.
     */
    public ImmutableSet<String> getIncludedFiles()
    {
        if (currentNode == null)
            return ImmutableSet.of();

        final Tree root = Tree.getRoot(currentNode);
        final List<String> result = new ArrayList<String>();
        Tree.dfs(root, result);

        // Exclude the root including file.
        result.remove(result.size() - 1);

        return ImmutableSet.copyOf(result);
    }

    /**
     * Reset the state of the include handler by clearing the include chain and
     * collection of included files.
     */
    public void clear()
    {
        this.currentNode = null;
    }

    /**
     * Get the file on top of the include chain stack.
     * 
     * @return The file on top of the include chain stack.
     */
    protected String getIncludeStackTop()
    {
        if (currentNode == null)
            return null;
        else
            return currentNode.filename;
    }

    /**
     * Enter an included file. Push the filename to the include chain.
     * 
     * @param filename Included file name.
     */
    public void enterFile(final String filename)
    {
        assert filename != null : "Filename can't be null.";

        if (currentNode == null)
            currentNode = new Tree(filename, null);
        else
            currentNode = currentNode.addChild(filename);

        IFileSpecification filespec = getFileSpec(filename);

        long newTimeStamp = filespec.getLastModified();
        timeStamp = Math.max(timeStamp, newTimeStamp);
        addOffsetCue();
    }

    /**
     * propagates the lastModified timestamp from a {@link IncludeHandler} from a
     * child node to this one.
     * 
     * @param childIncludeHandler The <code>include</code>-handler whose
     * timestamp will be used to set the time stap of this <code>include</code>-handler.
     */
    public void propagateLastModified(IncludeHandler childIncludeHandler)
    {
        timeStamp = Math.max(timeStamp, childIncludeHandler.getLastModified());
    }

    /**
     * This timestamp will be the latest modification time of all the root
     * source and all included files
     * 
     * @return The timestamp of the root include file
     */
    public long getLastModified()
    {
        return timeStamp;
    }

    /**
     * Make an {@code OffsetCue} object and add it to the buffer.
     */
    private void addOffsetCue()
    {
        final int adjustment = absoluteOffset - currentNode.tokenEnd;
        final OffsetCue cue = new OffsetCue(currentNode.filename, absoluteOffset, adjustment);
        offsetCueListBuilder.add(cue);
    }

    /**
     * Leave the included file.
     */
    public void leaveFile()
    {
        currentNode = currentNode.parent;
        if (currentNode != null)
            addOffsetCue();
    }

    /**
     * Extend the end offset of the current file and leave the file.
     * 
     * @param endOffset Local end offset of the current file, including all
     * trailing white spaces.
     */
    public void leaveFile(final int endOffset)
    {
        final int advance = endOffset - currentNode.tokenEnd;
        if (advance > 0)
            absoluteOffset += advance;
        leaveFile();
    }

    /**
     * Check if the file is already on the include chain. If true, including it
     * again will cause cyclic include problem.
     * 
     * @param filename file name
     * @return True if including the file causes cyclic include problem.
     */
    protected boolean isCyclicInclude(final String filename)
    {
        assert filename != null : "Filename can't be null.";

        Tree cursor = currentNode;
        while (cursor != null)
        {
            if (cursor.filename.equals(filename))
                return true;
            cursor = cursor.parent;
        }
        return false;
    }

    /**
     * Get a tree of include relationship.
     * 
     * @return A tree of include relationship.
     */
    protected Tree getIncludeTree()
    {
        return Tree.getRoot(currentNode);
    }

    /**
     * Set the absolute offset on an {@link ASToken}. The absolute offset is the
     * token's "local" start offset translated into the absolute space.
     * <p>
     * Advance absolute offset recorded in the handler by one token.
     * 
     * @param token The token to update.
     */
    protected void onNextToken(ASToken token)
    {
        assert token != null : "ASToken can't be null.";

        if (currentNode != null)
        {
            // "hasMXMLUnits" shortcuts the token offset check. See Javadoc of "hasMXMLUnits" for details.
            assert hasMXMLUnits || (token.getEnd() >= currentNode.tokenEnd) : String.format(
                        "Token [%s] (line:%d, col:%d) end at '%d', but last token end at '%d': %s",
                        token.getText(), token.getLine(), token.getColumn(), token.getEnd(), currentNode.tokenEnd, currentNode.filename);

            // "current_absolute += y"
            final int advance = token.getEnd() - currentNode.tokenEnd;
            absoluteOffset += advance;

            // Advance local end offset stored on the tree node.
            currentNode.tokenEnd = token.getEnd();

            // [LAST_TOKEN]   [THIS_TOKEN]
            // ..............(x)........(y)
            //
            // "token.absolute = current_absolute + x"
            // From last token's end offset to this token's start offset.
            final int delta = token.getStart() - currentNode.tokenEnd;
            final int tokenAbsoluteStart = delta + absoluteOffset;

            // Override token offset with absolute offsets
            int tokenLength = token.getEnd() - token.getStart();
            if (tokenLength < 0)
                tokenLength = 0;
            token.setStart(tokenAbsoluteStart);
            token.setEnd(tokenAbsoluteStart + tokenLength);
        }
    }

    /**
     * Update the {@code IncludeHandler}'s current offset counter with the next
     * {@code IMXMLUnitData}
     * 
     * @param unitData Next {@code IMXMLUnitData} object.
     */
    public void onNextMXMLUnitData(final IMXMLUnitData unitData)
    {
        assert unitData != null : "IMXMLUnitData can't be null.";
        hasMXMLUnits = true;

        if (currentNode != null)
        {
            final int contentEnd = unitData.getContentEnd();

            // This assert is turned off because of CMP-1490.
            // assert contentEnd >= currentNode.tokenEnd : "MXMLUnitData end can't be smaller than last unit end.";

            final int advance = contentEnd - currentNode.tokenEnd;
            absoluteOffset += advance;

            // Advance local end offset stored on the tree node.
            currentNode.tokenEnd = contentEnd;
        }
    }

    /**
     * @return Absolute offset.
     */
    protected int getAbsoluteOffset()
    {
        return absoluteOffset;
    }

    /**
     * Get all the {@link OffsetCue}'s.
     * 
     * @return All the {@code OffsetCue} objects.
     */
    public ImmutableList<OffsetCue> getOffsetCueList()
    {
        return offsetCueListBuilder.build();
    }

    @Override
    public String toString()
    {
        final StringBuilder result = new StringBuilder();
        Tree walker = currentNode;
        while (walker != null)
        {
            result.append("    ").append(walker).append("\n");
            walker = walker.parent;
        }
        return result.toString();
    }

    /**
     * Set project reference. The project reference is used to resolve included
     * files in other source folders in the project.
     * 
     * @param project Current project.
     */
    public final void setProjectAndCompilationUnit(IASProject project, ICompilationUnit compilationUnit)
    {
        this.project = project;
        this.compilationUnit = compilationUnit;
    }

    /**
     * When a client catches a file not found, they should call us back and let
     * us know. Only important in an incremental compilation workflow (like
     * Flash Buildler). NOt needed for command line compiles any most unit
     * tests.
     * 
     * @param fileSpec A file specification.
     */
    public void handleFileNotFound(IFileSpecification fileSpec)
    {
        if (this.project != null && this.compilationUnit != null)
        {
            ((CompilerProject)this.project).addUnfoundReferencedSourceFileDependency(fileSpec.getPath(), compilationUnit);
        }
    }
}
