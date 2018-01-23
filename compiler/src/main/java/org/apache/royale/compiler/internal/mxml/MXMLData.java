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

package org.apache.royale.compiler.internal.mxml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.common.MutablePrefixMap;
import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.parsing.mxml.BalancingMXMLProcessor;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLTokenizer;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLUnitDataIterator;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.SyntaxProblem;
import org.apache.royale.utils.FastStack;
import org.apache.royale.utils.FastStack.IFastStackDecorator;

/**
 * Encapsulation of an MXML file, with individual units for each open tag, close
 * tag, and block of text.
 */
public class MXMLData implements IMXMLData
{

    private final class TokenizerPayload
    {
        private List<MXMLToken> tokens;

        private PrefixMap prefixMap;

        public TokenizerPayload(List<MXMLToken> tokens, PrefixMap prefixMap)
        {
            this.tokens = tokens;
            this.prefixMap = prefixMap;
        }

        public List<MXMLToken> getMXMLTokens()
        {
            return tokens;
        }

        public PrefixMap getPrefixMap()
        {
            return prefixMap;
        }
    }

    /**
     * A cursor represents the offset into the document and the index of the
     * MXML unit that contains that offset
     */
    public class Cursor
    {
        /**
         * Offset into the document
         */
        private int offset;
        /**
         * Index of the unit that contains that offset
         */
        private int unitIndex;

        /**
         * Constructor (points to beginning of the MXMLData)
         */
        public Cursor()
        {
            reset();
        }

        /**
         * Reset cursor to the beginning of the MXMLData
         */
        public void reset()
        {
            offset = 0;
            unitIndex = 0;
        }

        /**
         * Set the cursor to a particular offset/unit
         * 
         * @param offset offset into the document
         * @param unitIndex index of the unit containing that offset
         */
        public void setCursor(int offset, int unitIndex)
        {
            this.offset = offset;
            this.unitIndex = unitIndex;
        }

        /**
         * Get the offset the cursor is pointing to
         * 
         * @return current document offset
         */
        public int getOffset()
        {
            return offset;
        }

        /**
         * Get the index of the unit the cursor is pointing to
         * 
         * @return current unit index
         */
        public int getUnitIndex()
        {
            return unitIndex;
        }
    }

    private String path = null;

    /**
     * Individual units for each open tag, close tag, and block of text
     */
    private IMXMLUnitData[] units;

    /**
     * Flag indicating that the tokens underlying this structure were fixed
     */
    private boolean wasRepaired;

    private boolean shouldRepair = true;

    /**
     * This maps {@link IMXMLTagData} objects to their explicit
     * {@link PrefixMap} if it exist
     */
    private HashMap<IMXMLTagData, PrefixMap> nsMap;

    /**
     * The cursor holds the result of last offset lookup into the MXMLData (see
     * findNearestUnit, below). When an edit happens that causes the code
     * coloring engine to recompute everything from the edit position to the end
     * of the document, it will request MXMLOffsetInformations in order. Having
     * the cursor speeds this up tremendously.
     */
    private Cursor cursor;

    /**
     * The dialect of MXML being used.
     */
    private MXMLDialect mxmlDialect;

    private Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>(2);

    /**
     * Tells us if we are currently processing full MXML, or a fragment
     */
    private boolean fullContent = true;

    /**
     * Constructor
     * 
     * @param tokens MXML tokens to build the MXMLData object from
     * @param map the {@link PrefixMap} for the document, containing the
     * namespace/prefix mappings
     */
    public MXMLData(List<MXMLToken> tokens, PrefixMap map, IFileSpecification fileSpec)
    {
        path = fileSpec.getPath();
        init(tokens, map);
    }

    public MXMLData(List<MXMLToken> tokens, PrefixMap map, IFileSpecification fileSpec, boolean shouldRepair)
    {
        path = fileSpec.getPath();
        this.shouldRepair = false;
        init(tokens, map);
    }

    /**
     * Constructor
     * 
     * @param mxmlText input build our {@link MXMLData} from
     */
    public MXMLData(int startOffset, Reader mxmlText, IFileSpecification fileSpec)
    {
        TokenizerPayload payload = getTokens(startOffset, mxmlText);
        path = fileSpec.getPath();
        init(payload.getMXMLTokens(), payload.getPrefixMap());
    }

    /**
     * Constructor
     * 
     * @param mxmlText input build our {@link MXMLData} from
     */
    public MXMLData(int startOffset, Reader mxmlText, String path, boolean fullContent)
    {
        this.fullContent = fullContent;
        TokenizerPayload payload = getTokens(startOffset, mxmlText);
        this.path = path;
        init(payload.getMXMLTokens(), payload.getPrefixMap());
    }

    public MXMLData(Reader mxmlText, IFileSpecification fileSpec)
    {
        this(0, mxmlText, fileSpec);
    }

    public MXMLData(Reader mxmlText, String path)
    {
        // Some clients of MXML data pass in a null IPath for the source path.
        // Specifically this seems to happen in the code that builds a new
        // mxml document from a template.
        this(0, mxmlText, path != null ? path : null, true);
    }

    /**
     * Constructor
     * 
     * @param specification input build our {@link MXMLData} from
     */
    public MXMLData(IFileSpecification specification)
    {
        TokenizerPayload payload;
        try
        {
            path = specification.getPath();
            payload = getTokens(specification.createReader());
            init(payload.getMXMLTokens(), payload.getPrefixMap());
        }
        catch (FileNotFoundException e)
        {
            //do nothing
        }
    }

    private void init(List<MXMLToken> tokens, PrefixMap map)
    {
        mxmlDialect = MXMLDialect.getMXMLDialect(map);
        initializeFromTokens(tokens);
    }

    /**
     * Update the version of the mxml data
     * 
     * @param map the prefix map
     */
    public void updateMXMLVersion(PrefixMap map)
    {
        mxmlDialect = MXMLDialect.getMXMLDialect(map);
    }

    /**
     * Get the namespaces defined on the root tag
     * 
     * @param reader The reader which would provide the root tag information
     * @return {@link PrefixMap} for the root tag
     * @throws IOException error
     */
    public static PrefixMap getRootNamespaces(Reader reader) throws IOException
    {
        MXMLTokenizer tokenizer = new MXMLTokenizer();
        try
        {
            tokenizer.setReader(reader);
            return tokenizer.getRootTagPrefixMap().clone();
        }
        finally
        {
            tokenizer.close();
        }
    }

    protected TokenizerPayload getTokens(Reader reader)
    {
        return getTokens(0, reader);
    }

    protected TokenizerPayload getTokens(int startOffset, Reader reader)
    {
        List<MXMLToken> tokens = null;
        MXMLTokenizer tokenizer = new MXMLTokenizer(startOffset);
        try
        {
            tokenizer.setIsRepairing(true);
            tokens = tokenizer.parseTokens(reader);
            wasRepaired = tokenizer.tokensWereRepaired();
            return new TokenizerPayload(tokens, tokenizer.getPrefixMap());
        }
        finally
        {
            IOUtils.closeQuietly(tokenizer);
        }
    }

    @Override
    public MXMLDialect getMXMLDialect()
    {
        return mxmlDialect;
    }

    @Override
    public PrefixMap getPrefixMapForData(IMXMLTagData data)
    {
        PrefixMap result = nsMap.get(data);
        if (result != null)
            return result;
        if (data.isCloseTag())
        {
            IMXMLTagData openTagData = data.getParent().findTagOrSurroundingTagContainingOffset(data.getAbsoluteStart());
            if (openTagData != null)
                return nsMap.get(openTagData);
        }
        return null;
    }

    void removePrefixMappingForTag(IMXMLTagData data)
    {
        nsMap.remove(data);
    }

    void clearPrefixMappings()
    {
        nsMap.clear();
    }

    public void setPrefixMappings(HashMap<IMXMLTagData, PrefixMap> map)
    {
        if (nsMap != null)
            nsMap.clear();
        nsMap = map;
    }

    public Map<IMXMLTagData, PrefixMap> getTagToPrefixMap()
    {
        return nsMap;
    }

    /**
     * Returns a collection of prefix->namespaces mappings found within this
     * document. This map DOES NOT maintain order, and for more fine-grained
     * information, the getPrefixMap call on individual {@link IMXMLTagData} and
     * {@link IMXMLTagAttributeData} objects should be called
     * 
     * @return a prefix map
     */
    public PrefixMap getDocumentPrefixMap()
    {
        MutablePrefixMap map = new MutablePrefixMap();
        for (PrefixMap tagMap : nsMap.values())
        {
            assert tagMap != null;
            map.addAll(tagMap);
        }
        return map.toImmutable();
    }

    /**
     * Returns the PrefixMap for the root tag of this {@link MXMLData} object
     * 
     * @return a {@link PrefixMap} or null
     */
    public PrefixMap getRootTagPrefixMap()
    {
        IMXMLTagData rootTag = getRootTag();
        if (rootTag != null)
        {
            return nsMap.get(rootTag);
        }
        return null;
    }

    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }

    public boolean hasProblems()
    {
        return problems.size() > 0;
    }

    /**
     * Determines if this data was built from a source that was repaired
     * 
     * @return true if the underlying source was repaired
     */
    public boolean isDataRepaired()
    {
        return wasRepaired;
    }

    public void setWasRepaired(boolean wasRepaired)
    {
        this.wasRepaired = wasRepaired;
    }

    @Override
    public IFileSpecification getFileSpecification()
    {
        return new FileSpecification(path);
    }

    @Override
    public String getPath()
    {
        return path;
    }

    /**
     * API to change the path after MXMLData creation. An MXMLDocument makes an
     * MXMLData before the actual location is known.
     * 
     * @param path is the absolute path to the backing source file
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Common code from the two constructors
     * 
     * @param tokens A list of MXML tokens.
     */
    protected void initializeFromTokens(List<MXMLToken> tokens)
    {
        cursor = new Cursor();
        parse(this, tokens, mxmlDialect, problems);
        cursor.reset();
    }

    /**
     * Use the MXML tokens to build MXMLUnitData.
     * 
     * @param data the {@link MXMLData} object
     * @param tokens the list of tokens to build this data from
     * @param dialect the {@link MXMLDialect} we are working against
     * @param incremental true if this data is being built incrementally. All
     * location updates will need to be done outside this element
     */
    private void parse(MXMLData data, List<MXMLToken> tokens, MXMLDialect dialect, Collection<ICompilerProblem> problems)
    {
        ArrayList<MXMLUnitData> units = new ArrayList<MXMLUnitData>(tokens.size() / 6);
        nsMap = new HashMap<IMXMLTagData, PrefixMap>();
        MXMLUnitData unit = null;
        MXMLToken currentComment = null;
        FastStack<Integer> depth = new FastStack<Integer>(tokens.size() / 8);
        IFileSpecification spec = new FileSpecification(data.getPath() != null ? data.getPath() : "");
        depth.setStackDecorator(new IFastStackDecorator<Integer>() {
            @Override
            public Integer decorate(Integer e)
            {
                if (e == null)
                    return -1;
                return e;
            }

        });
        int index = -1;
        int balancingIndex = 0;
        depth.push(index);
        ListIterator<MXMLToken> tokenIterator = tokens.listIterator();
        BalancingMXMLProcessor processor = new BalancingMXMLProcessor(getFileSpecification(), problems);
        while (tokenIterator.hasNext())
        {
            MXMLToken token = tokenIterator.next();
            switch (token.getType())
            {
                case MXMLTokenTypes.TOKEN_ASDOC_COMMENT:
                {
                    currentComment = token;
                    //treat this like text.
                    unit = new MXMLTextData(token);
                    units.add(unit);
                    index++;
                    if (fullContent)
                    {
                        unit.setParentUnitDataIndex(depth.peek());
                        unit.setLocation(data, index);
                    }
                    break;
                }
                
                case MXMLTokenTypes.TOKEN_COMMENT:
                case MXMLTokenTypes.TOKEN_CDATA:
                case MXMLTokenTypes.TOKEN_WHITESPACE:
                {
                    //treat this like text.
                    unit = new MXMLTextData(token);
                    units.add(unit);
                    index++;
                    if (fullContent)
                    {
                        unit.setParentUnitDataIndex(depth.peek());
                        unit.setLocation(data, index);
                    }
                    break;
                }
                
                case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
                {
                    unit = new MXMLTagData();
                    MutablePrefixMap map = ((MXMLTagData)unit).init(this, token, tokenIterator, dialect, spec, problems);
                    ((MXMLTagData)unit).setCommentToken(currentComment);
                    currentComment = null;
                    units.add(unit);
                    index++;
                    if (fullContent)
                    {
                        unit.setParentUnitDataIndex(depth.peek());
                        unit.setLocation(data, index);
                        if (!((MXMLTagData)unit).isEmptyTag())
                            processor.addOpenTag((MXMLTagData)unit, balancingIndex);
                    }
                    if (map != null)
                        nsMap.put((MXMLTagData)unit, map.toImmutable());
                    if (!((MXMLTagData)unit).isEmptyTag())
                    {
                        depth.push(index);
                        balancingIndex++;
                    }
                    break;
                }
                
                case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
                {
                    unit = new MXMLTagData();
                    ((MXMLTagData)unit).init(this, token, tokenIterator, dialect, spec, problems);
                    ((MXMLTagData)unit).setCommentToken(currentComment);
                    if (!((MXMLTagData)unit).isEmptyTag())
                    {
                        depth.pop();
                        balancingIndex--;
                    }
                    index++;
                    if (fullContent)
                    {
                        unit.setLocation(data, index);
                        unit.setParentUnitDataIndex(depth.peek());
                        processor.addCloseTag((MXMLTagData)unit, balancingIndex);
                    }
                    currentComment = null;
                    units.add(unit);
                    break;
                }
                
                case MXMLTokenTypes.TOKEN_TEXT:
                {
                    unit = new MXMLTextData(token);
                    units.add(unit);
                    index++;
                    if (fullContent)
                    {
                        unit.setParentUnitDataIndex(depth.peek());
                        unit.setLocation(data, index);
                    }
                    break;
                }
                
                case MXMLTokenTypes.TOKEN_PROCESSING_INSTRUCTION:
                {
                    unit = new MXMLInstructionData(token);
                    units.add(unit);
                    index++;
                    if (fullContent)
                    {
                        unit.setParentUnitDataIndex(depth.peek());
                        unit.setLocation(data, index);
                    }
                    break;
                }
                
                default:
                {
                    problems.add(new SyntaxProblem(token));
                    break;
                }
            }
        }
        this.units = units.toArray(new IMXMLUnitData[0]);
        if (fullContent && shouldRepair)
        {
            this.units = processor.balance(this.units, this, nsMap);
            if (processor.wasRepaired())
            { //repaired, so let's rebuild our prefix maps and tag depths
                refreshPositionData();
            }
        }
    }

    /**
     * Used to rebuild the structures inside of the MXMLData, refreshing prefix
     * maps, depth and position data. Should only be used after calls that
     * rebuild the internal structure are called
     */
    public void refreshPositionData()
    {
        FastStack<Integer> depth = new FastStack<Integer>(units.length / 2);
        depth.setStackDecorator(new IFastStackDecorator<Integer>() {
            @Override
            public Integer decorate(Integer e)
            {
                if (e == null)
                    return -1;
                return e;
            }

        });
        depth.clear();
        depth.push(-1);
        for (int i = 0; i < units.length; i++)
        {
            if (units[i] instanceof IMXMLTagData)
            {
                IMXMLTagData currentTag = (IMXMLTagData)units[i];
                if (currentTag.isCloseTag())
                {
                    if (!currentTag.isEmptyTag())
                        depth.pop();
                }
                ((MXMLTagData)currentTag).setParentUnitDataIndex(depth.peek());
                ((MXMLTagData)currentTag).setLocation(this, i);
                if (currentTag.isOpenTag())
                {
                    if (!currentTag.isEmptyTag())
                    {
                        depth.push(i);
                    }
                }
            }
            else
            {
                ((MXMLUnitData)units[i]).setParentUnitDataIndex(depth.peek());
                ((MXMLUnitData)units[i]).setLocation(this, i);
            }
        }
    }

    @Override
    public IMXMLUnitData[] getUnits()
    {
        return units;
    }

    public Iterator<IMXMLUnitData> getUnitIterator()
    {
        return new MXMLUnitDataIterator(units);
    }

    /**
     * Replace the list of units in this MXMLData.
     * 
     * @param units units to add
     */
    public void setUnits(IMXMLUnitData[] units)
    {
        this.units = units;
    }

    @Override
    public IMXMLUnitData getUnit(int i)
    {
        if (i < 0 || i >= units.length)
            return null;
        return units[i];
    }

    @Override
    public int getNumUnits()
    {
        return units.length;
    }

    /**
     * If the offset is contained within an MXML unit, get that unit. If it's
     * not, then get the first unit that follows the offset.
     * 
     * @param offset test offset
     * @return unit that contains (or immediately follows) the offset A few
     * subtleties: In Royale we have endeavored to preserve the existing
     * definition of "nearest", so that for a given MXML file, royale will find
     * the same "nearest" unit.
     */

    protected IMXMLUnitData findNearestUnit(int offset)
    {

        // Use the cursor as a fast search hint. But only if the cursor is at or before the
        // are of interest.
        // TODO: do we care that this is not thread safe?
        int startOffset = 0;
        if (cursor.getOffset() <= offset)
            startOffset = cursor.getUnitIndex();

        // Sanity check
        if (startOffset < 0 || startOffset >= units.length)
            startOffset = 0;

        // Now iterate though the units and find the first one that is acceptable
        IMXMLUnitData ret = null;
        for (int i = startOffset; (i < units.length) && (ret == null); i++)
        {
            IMXMLUnitData unit = units[i];

            // unit is a match if it "contains" the offset.
            // We are using a somewhat bizarre form of "contains" here, in that we are
            // using getStart() and getConentEnd(). This asymmetric mismatch is for several reasons:
            //      * it's the only way to match the existing (non-royale) behavior
            //      * If our cursor is before the <, we want to match the tag.
            //              example:     |<foo   >  will find "foo" as the nearest tag.
            //      So we need to use start here (not content start)
            //      * If our cursor is between two tags, we want to match the NEXT one, not the previous one
            //              example:   <bar >|<foo>  should match foo, not bar

            if (MXMLData.contains(unit.getAbsoluteStart(), unit.getContentEnd(), offset))
            {
                ret = unit;
            }
            // if we find a unit that starts after the offset, then it must
            // be the "first one after", so return it
            else if (unit.getAbsoluteStart() >= offset)
            {
                ret = unit;
            }
        }

        // If we found something, update the cursor for the next search
        if (ret != null)
            cursor.setCursor(offset, ret.getIndex());
        return ret;
    }

    /**
     * Get the unit that should be referenced when looking at what tags contain
     * this offset (i.e. tags that are opened and not closed before this offset)
     * 
     * @param offset test offset
     * @return reference unit for containment searches
     */
    public IMXMLUnitData findContainmentReferenceUnit(int offset)
    {
        return findNearestUnit(offset);
    }

    /**
     * Get the unit that contains this offset
     * 
     * @param offset test offset
     * @return the containing unit (or null if no unit contains this offset)
     */
    public IMXMLUnitData findUnitContainingOffset(int offset)
    {
        IMXMLUnitData unit = findNearestUnit(offset);
        if (unit != null && unit.containsOffset(offset))
            return unit;

        return null;
    }

    /**
     * Get the open, close, or empty tag that contains this offset. Note that if
     * offset is inside a text node, this returns null. If you want the
     * surrounding tag in that case, use
     * findTagOrSurroundingTagContainingOffset.
     * 
     * @param offset test offset
     * @return the containing tag (or null, if no tag contains this offset)
     */
    public IMXMLTagData findTagContainingOffset(int offset)
    {
        IMXMLUnitData unit = findUnitContainingOffset(offset);
        if (unit != null && unit.isTag())
            return (IMXMLTagData)unit;

        return null;
    }

    @Override
    public IMXMLTagData findTagOrSurroundingTagContainingOffset(int offset)
    {
        IMXMLUnitData unit = findUnitContainingOffset(offset);
        if (unit != null)
        {
            if (unit.isTag())
            {
                return (IMXMLTagData)unit;
            }
            else if (unit.isText())
            {
                IMXMLTagData containingTag = unit.getContainingTag(unit.getAbsoluteStart());
                return containingTag;
            }
        }

        return null;
    }

    /**
     * Get the open or empty tag whose attribute list contains this offset. A
     * tag's attribute list extends from after the tag name + first whitespace
     * until before the closing ">" or "/>".
     * 
     * @param offset test offset
     * @return tag whose attribute list contains this offset (or null, if the
     * offset isn't in any attribute lists
     */
    public IMXMLTagData findAttributeListContainingOffset(int offset)
    {
        IMXMLTagData tag = findTagContainingOffset(offset);
        if (tag != null && tag.isOpenTag())
            return tag.isOffsetInAttributeList(offset) ? tag : null;

        return null;
    }

    /**
     * Test whether the offset is contained within the range from start to end.
     * This test excludes the start position and includes the end position,
     * which is how you want things to work for code hints.
     * 
     * @param start start of the range (excluded)
     * @param end end of the range (included)
     * @param offset test offset
     * @return true iff the offset is contained in the range
     */
    public static boolean contains(int start, int end, int offset)
    {
        return start < offset && end >= offset;
    }

    @Override
    public IMXMLTagData getRootTag()
    {
        int n = units.length;
        for (int i = 0; i < n; i++)
        {
            IMXMLUnitData unit = units[i];
            if (unit instanceof IMXMLTagData)
                return (IMXMLTagData)unit;
        }
        return null;
    }

    @Override
    public int getEnd()
    {
        final int n = getNumUnits();
        return n > 0 ? getUnit(n - 1).getAbsoluteEnd() : 0;
    }

    public Cursor getCursor()
    {
        return cursor;
    }

    /**
     * Verifies that all units (plus all attributes on units that are tags) have
     * their source location information set.
     * <p>
     * This is used only in asserts.
     */
    public boolean verify()
    {
        for (IMXMLUnitData unit : getUnits())
        {
            ((MXMLUnitData)unit).verify();
        }

        return true;
    }

    // For debugging only.
    void dumpUnits()
    {
        for (IMXMLUnitData unit : getUnits())
        {
            System.out.println(((MXMLUnitData)unit).toDumpString());
        }
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        IMXMLUnitData[] units = getUnits();
        int n = units.length;
        for (int i = 0; i < n; i++)
        {
            // Display the unit's index as, for example, [3].
            sb.append('[');
            sb.append(i);
            sb.append(']');

            sb.append(' ');

            // Display the unit's information.
            sb.append(units[i].toString());

            sb.append('\n');
        }

        return sb.toString();
    }

    public static void main(String[] args)
    {
        final FileSpecification fileSpec = new FileSpecification(args[0]);
        final MXMLTokenizer tokenizer = new MXMLTokenizer(fileSpec);
        List<MXMLToken> tokens = null;
        try
        {
            tokens = tokenizer.parseTokens(fileSpec.createReader());

            // Build tags and attributes from the tokens.
            final MXMLData mxmlData = new MXMLData(tokens, tokenizer.getPrefixMap(), fileSpec);

            mxmlData.dumpUnits();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(tokenizer);
        }
    }
}
