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

package org.apache.flex.compiler.mxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.flex.compiler.common.MutablePrefixMap;
import org.apache.flex.compiler.common.PrefixMap;
import org.apache.flex.compiler.common.PrefixedXMLName;
import org.apache.flex.compiler.common.SourceLocation;
import org.apache.flex.compiler.common.XMLName;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.mxml.MXMLDialect;
import org.apache.flex.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.flex.compiler.mxml.IMXMLTextData.TextType;
import org.apache.flex.compiler.parsing.IMXMLToken;
import org.apache.flex.compiler.parsing.MXMLTokenTypes;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.MXMLDuplicateAttributeProblem;
import org.apache.flex.compiler.problems.SyntaxProblem;
import org.apache.flex.utils.FastStack;

/**
 * Encapsulation of an open tag, a close tag, or an empty tag in MXML.
 */
public class MXMLTagData extends MXMLUnitData
{
    private static final MXMLTagAttributeData[] NO_ATTRIBUTES = new MXMLTagAttributeData[0];

    /**
     * Constructor.
     */
    public MXMLTagData()
    {
        // Although we don't normally rely on the default ctor to make a completely valid object,
        // in some cases we don't end up calling init, and we end up with a partially construced object.
        // So let's init enough stuff so that the object is well behaved.
        this.attributeMap = Collections.emptyMap();
        this.attributes = NO_ATTRIBUTES;
    }

    /**
     * Copy constructor.
     */
    protected MXMLTagData(MXMLTagData other)
    {
        super(other);
        this.nameStart = other.nameStart;
        this.contentEnd = other.contentEnd;
        this.tagName = other.tagName;
        this.nameType = other.nameType;
        this.commentToken = other.commentToken;
        this.stateStart = other.stateStart;
        this.stateName = other.stateName;
        this.attributesStart = other.attributesStart;
        this.emptyTag = other.emptyTag;
        this.explicitCloseToken = other.explicitCloseToken;
        this.attributeMap = other.attributeMap;
        this.attributes = other.attributes;
        this.uri = other.uri;

        this.setOffsets(other.getStart(), other.getEnd());
        this.setLine(other.getLine());
        this.setColumn(other.getColumn());
    }

    protected String tagName;

    /**
     * The URI specified by this tag's prefix.
     */
    protected String uri;
    
    protected String stateName;
    
    /**
     * In-order list of MXML attributes
     */
    protected IMXMLTagAttributeData[] attributes;

    /**
     * Map of attribute name to MXML attribute
     */
    protected Map<String, MXMLTagAttributeData> attributeMap;
    
    /*
     * offset where the tag name starts Note that we also use the for
     * offsetContains(), as it is the first "real" character in the tag
     */
    protected int nameStart;

    /*
     * this is the offset of the end of the "stuff inside the punctuation
     */
    protected int contentEnd;

    protected int nameType;

    /**
     * MXML Comment
     */
    protected IMXMLToken commentToken;

    protected int stateStart;
    
    /**
     * Start of the attribute list (after the tag name + whitespace)
     */
    protected int attributesStart;

    /**
     * Is this an empty tag (ending with "/>")?
     */
    protected boolean emptyTag;

    /**
     * Indicates if we have an explicit close tag
     */
    protected boolean explicitCloseToken;

    /*
     * unlike most MXML units, our "content" bounds are not the same as the
     * {@link SourceLocation#getStart()} For tags, we don't count the outer
     * punctuation as "content", although we do count spaces. Example:
     * 0123456789 <foo /> start ==0 contentStart==1 contentEnd== 6 end == 8
     */
    @Override
    public int getContentStart()
    {
        return nameStart;
    }

    @Override
    public int getContentEnd()
    {
        return contentEnd;
    }

    @SuppressWarnings("fallthrough")
    MutablePrefixMap init(MXMLData mxmlData, MXMLToken nameToken, ListIterator<MXMLToken> tokenIterator, MXMLDialect dialect, IFileSpecification spec, Collection<ICompilerProblem> problems)
    {
        setSourcePath(mxmlData.getPath());
        MutablePrefixMap map = null;
        emptyTag = false;
        explicitCloseToken = false;

        // the start offset will by where '<' is. We strip that text off, but need to remember correct offset first
        int startOffset = nameToken.getStart();
        if (nameToken.getType() == MXMLTokenTypes.TOKEN_OPEN_TAG_START)
            nameToken.truncate(1, 0);
        else if (nameToken.getType() == MXMLTokenTypes.TOKEN_CLOSE_TAG_START)
            nameToken.truncate(2, 0);
        else
        {
            problems.add(new SyntaxProblem(nameToken));
            return map;
        }

        // Deal with name if it is of the form name.state
        int nameStart = nameToken.getStart();
        MXMLStateSplitter splitState = new MXMLStateSplitter(nameToken, dialect, problems, spec);
        tagName = splitState.baseName;
        if (splitState.stateName != null)
        {
            stateName = splitState.stateName;
            stateStart = nameToken.getStart() + splitState.stateNameOffset;
        }

        nameType = nameToken.getType();

        int nameEnd = nameStart + tagName.length();
        int contentEnd = nameEnd;
        setTagOffsets(startOffset, nameEnd, nameStart, contentEnd);
        setColumn(nameToken.getColumn());
        setLine(nameToken.getLine());
        attributesStart = getNameEnd();
        ArrayList<MXMLTagAttributeData> attrs = new ArrayList<MXMLTagAttributeData>();
        attributeMap = new LinkedHashMap<String, MXMLTagAttributeData>(); //preserve order of attrs
        boolean foundTagEnd = false;
        boolean putTokenBack = false; // This is a pre-falcon algorithm that helped recover from tag nesting errors
                                      // I am bringing it back to life
        while (tokenIterator.hasNext() && !foundTagEnd)
        {
            MXMLToken token = tokenIterator.next();
            MXMLTagAttributeData attribute = null;
            switch (token.getType())
            {
                case MXMLTokenTypes.TOKEN_NAME:
                    if (nameType == MXMLTokenTypes.TOKEN_CLOSE_TAG_START)
                    {
                        problems.add(new SyntaxProblem(token));
                        //burn forward until the end tag
                        //TODO do we want to mark each token as an error, or just the first?
                        while (tokenIterator.hasNext() && !foundTagEnd)
                        {
                            token = tokenIterator.next();
                            switch (token.getType())
                            {
                                case MXMLTokenTypes.TOKEN_TAG_END:
                                case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
                                    foundTagEnd = true;
                                    break;
                            }
                        }
                        break;
                    }
                    if (token.getText().startsWith("xmlns"))
                    {
                        attribute = new MXMLNamespaceAttributeData(token, tokenIterator, dialect, spec, problems);
                        if (map == null)
                            map = new MutablePrefixMap();
                        map.add(((MXMLNamespaceAttributeData)attribute).getNamespacePrefix(), ((MXMLNamespaceAttributeData)attribute).getNamespace());
                    }
                    else
                    {
                        attribute = new MXMLTagAttributeData(token, tokenIterator, dialect, spec, problems);
                    }
                    attribute.setParent(this);
                    // add the attribute to the attributes list even if it is duplicate
                    // otherwise code-hinting will not work properly
                    if (attributeMap.containsKey(token.getText()))
                    {
                        MXMLDuplicateAttributeProblem problem = new MXMLDuplicateAttributeProblem(attribute);
                        problems.add(problem);
                    }
                    attrs.add(attribute);
                    attributeMap.put(token.getText(), attribute);

                    // Now advance the offsets to include the newly parsed attributes
                    contentEnd = attribute.getAbsoluteEnd();
                    setTagOffsets(startOffset, contentEnd, nameStart, contentEnd);
                    break;
                case MXMLTokenTypes.TOKEN_TAG_END:
                    foundTagEnd = true;
                    explicitCloseToken = !token.isImplicit();
                    break;
                case MXMLTokenTypes.TOKEN_EMPTY_TAG_END:
                    emptyTag = true;
                    explicitCloseToken = !token.isImplicit();
                    foundTagEnd = true;
                    break;
                case MXMLTokenTypes.TOKEN_OPEN_TAG_START:
                case MXMLTokenTypes.TOKEN_CLOSE_TAG_START:
                    problems.add(new SyntaxProblem(token));
                    foundTagEnd = true; // Don't keep going - bail from malformed tag
                    putTokenBack = true;

                    // if we added 	this.fEmptyTag = true; then we could repair the damage here, 
                    // but it's better to let the balancer repair it (in case there is a matching close lurking somewhere)
                    break;
                default:
                    problems.add(new SyntaxProblem(token));
                    break;
            }
            if (foundTagEnd)
            {
                if (token.isImplicit() && token.getStart() == -1)
                {
                    explicitCloseToken = false;
                    //let's try to end at the start of the next token if one exists
                    if (tokenIterator.hasNext())
                    {
                        MXMLToken next = tokenIterator.next();
                        if (next != null)
                        {
                            // extend the end, but not the content end
                            setTagOffsets(getAbsoluteStart() == -1 ? next.getStart() : getAbsoluteStart(),
                                    getAbsoluteEnd() == -1 ? next.getStart() : getAbsoluteEnd(),
                                    nameStart == -1 ? next.getStart() : nameStart,
                                    contentEnd == -1 ? next.getStart() : contentEnd);
                            tokenIterator.previous();
                        }
                    }
                    else
                    {
                        // TODO: if we hit this case do we need to call setTagOffset.
                        // and is getNameEnd() correct in any case?
                        setOffsets(startOffset, getNameEnd());
                    }
                }
                else
                {
                    // A Tag's content extends all the way to the end token,
                    // so use the token to set content end
                    contentEnd = token.getStart();
                    if (!putTokenBack)
                    {
                        // if we are terminating on a "real" close tag, then the "end"
                        // of our tag will be the end of the TOKEN_TAG_END
                        setTagOffsets(startOffset, token.getEnd(), nameStart, contentEnd);
                    }
                    else
                    {
                        // ... conversely, if we are terminating on some other kind of token
                        // and are going to push the token back, we definietly don't
                        // want to adjust our bounds based on the end of THAT token.
                        //
                        // So.. use the token start to set the conent end (above) and the end
                        setTagOffsets(startOffset, contentEnd, nameStart, contentEnd);
                        tokenIterator.previous();
                    }
                }
            }
            else if (getAbsoluteEnd() < token.getEnd())
            {
                contentEnd = token.getEnd();
                setTagOffsets(startOffset, contentEnd, nameStart, contentEnd);
            }
        }
        attributes = attrs.toArray(new MXMLTagAttributeData[0]);
        return map;
    }

    /**
     * For tags, we "contain" an offset if our content contains the offset. This
     * means that we return false for the outside "<", ">", "</", "/>"
     */
    @Override
    public boolean containsOffset(int offset)
    {
        boolean ret = offset >= nameStart && offset <= contentEnd;
        return ret;
    }

    private void setTagOffsets(int start, int end, int contentStart, int contentEnd)
    {
        assert (start <= contentStart) &&
               ((contentStart <= contentEnd) || contentEnd == -1)
               && ((contentEnd <= end) || (end == -1));
        setOffsets(start, end);
        nameStart = contentStart;

        if (contentEnd != -1)
            this.contentEnd = contentEnd;
    }

    public void setCommentToken(IMXMLToken commentToken)
    {
        this.commentToken = commentToken;
    }

    public IMXMLToken getCommentToken()
    {
        return commentToken;
    }

    @Override
    public void setParentUnitDataIndex(int parentIndex)
    {
        //when we fixup tokens, we don't have enough context to determine if we are a root tag.  When we're a root tag, we cannot be
        //an emty 
        if (emptyTag && !explicitCloseToken)
        {
            if (parentIndex == -1)
            {
                emptyTag = false;
            }
        }
        super.setParentUnitDataIndex(parentIndex);
    }

    /**
     * Adjust all associated offsets by the adjustment amount
     * 
     * @param offsetAdjustment amount to add to offsets
     */
    @Override
    public void adjustOffsets(int offsetAdjustment)
    {
        super.adjustOffsets(offsetAdjustment);
        nameStart += offsetAdjustment;
        contentEnd += offsetAdjustment;
        if (stateName != null)
        {
            stateStart += offsetAdjustment;
        }
        attributesStart += offsetAdjustment;
        for (int i = 0; i < attributes.length; i++)
        {
            IMXMLTagAttributeData attribute = attributes[i];
            ((MXMLTagAttributeData)attribute).adjustOffsets(offsetAdjustment);
        }
    }

    /**
     * Is this MXML unit a tag?
     * 
     * @return true if the unit is a tag
     */
    @Override
    public boolean isTag()
    {
        return true;
    }

    /**
     * Is this MXML unit an empty tag? (An "empty tag" is, I believe, a tag that
     * opens and closes, i.e. &lt;foo/&gt; -- note that this is also considered
     * an "open tag". -gse)
     * 
     * @return true if the unit is an empty tag
     */
    public boolean isEmptyTag()
    {
        return emptyTag;
    }

    /**
     * accessor for repair. This lets the balancer mark a tag as empty.
     */
    public void setEmptyTag()
    {
        emptyTag = true;
    }

    /**
     * True if this MXMLTagData object has an actual close token, and was not
     * closed as a post-process step of MXML repair
     * 
     * @return if we have an explicit close tag
     */
    public boolean hasExplicitCloseTag()
    {
        return explicitCloseToken;
    }

    /**
     * Returns true if this tag is the root tag of the containing MXML document
     * 
     * @return true if we are the root tag
     */
    public boolean isDocumentRoot()
    {
        if (isOpenTag())
        {
            if (getParentUnitDataIndex() == -1)
            {
                int index = getIndex();
                if (index == 0)
                    return true;
                //if we are not zero, scan backwards to see if there is a tag before us
                index--;
                while (index >= 0)
                {
                    MXMLUnitData unit = getParent().getUnit(index);
                    if (unit == null || unit.isTag())
                        return false;
                    index--;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Is this MXML unit an open tag? (i.e. &lt;foo&gt; OR &lt;foo/&gt;, note
     * that the latter is also an empty tag)
     * 
     * @return true if the unit is an open tag
     */
    @Override
    public boolean isOpenTag()
    {
        return nameType == MXMLTokenTypes.TOKEN_OPEN_TAG_START;
    }

    /**
     * Is this MXML unit an open tag and not an empty tag (i.e. only
     * &lt;foo&gt;, not &ltfoo;/&gt;)?
     * 
     * @return true if the unit is an open tag, and not an empty tag
     */
    @Override
    public boolean isOpenAndNotEmptyTag()
    {
        return (isOpenTag() && !isEmptyTag());
    }

    /**
     * Is this MXML unit a close tag? (i.e. &lt;/foo&gt;)
     * 
     * @return true if the unit is a close tag
     */
    @Override
    public boolean isCloseTag()
    {
        return nameType == MXMLTokenTypes.TOKEN_CLOSE_TAG_START;
    }

    /**
     * Get the tag name as a string
     * 
     * @return the tag name (as a string)
     */
    public String getName()
    {
        return tagName;
    }

    /**
     * Get the tag name as an {@link PrefixedXMLName}
     * 
     * @return the tag name as an {@link PrefixedXMLName}
     */
    public PrefixedXMLName getPrefixedXMLName()
    {
        return new PrefixedXMLName(getName(), getURI());
    }

    /**
     * Get the tag name as an {@code XMLName}.
     * 
     * @return The tag name as an {@code XMLName}.
     */
    public XMLName getXMLName()
    {
        return new XMLName(getURI(), getShortName());
    }

    /**
     * Returns the {@link PrefixMap} that is explicitly set on this tag. If no
     * uri->namespace mappings are set, this will return null
     * 
     * @return a {@link PrefixMap} or null
     */
    public PrefixMap getPrefixMap()
    {
        return getParent().getPrefixMapForData(this);
    }

    /**
     * Returns the {@link PrefixMap} that represents all prefix->namespace
     * mappings are in play on this tag. For example, if a parent tag defines
     * <code>xmlns:m="falcon"</code> and this tag defines
     * <code>xmlns:m="eagle"</code> then in this prefix map, m will equal
     * "eagle"
     * 
     * @return a {@link PrefixMap} or null
     */
    public PrefixMap getCompositePrefixMap()
    {
        MutablePrefixMap compMap = new MutablePrefixMap();
        MXMLTagData lookingAt = this;
        while (lookingAt != null)
        {
            PrefixMap depth = getParent().getPrefixMapForData(lookingAt);
            if (depth != null)
            {
                compMap.addAll(depth, true);
            }
            lookingAt = lookingAt.getParentTag();
        }
        return compMap;
    }

    /**
     * Gets the prefix of this tag.
     * <p>
     * If the tag does not have a prefix, this method returns <code>null</code>.
     * 
     * @return The prefix as a String, or <code>null</code>.
     */
    public String getPrefix()
    {
        String name = getName();
        int i = name.indexOf(':');
        return i != -1 ? name.substring(0, i) : "";
    }

    public String getShortName()
    {
        String name = getName();
        int i = name.indexOf(':');
        return i != -1 ? name.substring(i + 1) : name;
    }

    /**
     * Gets the URI of this tag.
     * <p>
     * If the tag does not have a prefix, this method returns <code>null</code>
     * 
     * @return The URI as a String, or <code>null</code>.
     */
    public String getURI()
    {
        if (uri == null)
        {
            //walk up our chain to find the correct uri for our namespace.  first one wins
            String prefix = getPrefix();
            MXMLTagData lookingAt = this;
            while (lookingAt != null)
            {
                PrefixMap depth = getParent().getPrefixMapForData(lookingAt);
                if (depth != null && depth.containsPrefix(prefix))
                {
                    uri = depth.getNamespaceForPrefix(prefix);
                    break;
                }
                lookingAt = lookingAt.getParentTag();
            }
        }
        return uri;
    }

    public void invalidateURI()
    {
        uri = null;
        int length = attributes.length;
        for (int i = 0; i < length; i++)
        {
            ((MXMLTagAttributeData)attributes[i]).invalidateURI();
        }
    }

    /**
     * Get the state name as a string
     * 
     * @return the tag name (as a string)
     */
    public String getStateName()
    {
        return stateName != null ? stateName : "";
    }

    /**
     * Find out if this tag contains the specified attribute.
     * 
     * @param attributeName name of the attribute
     * @return true if the attribute is present
     */
    public boolean hasAttribute(String attributeName)
    {
        return attributeMap.containsKey(attributeName);
    }

    public boolean hasState()
    {
        return stateName != null;
    }

    public int getStateStart()
    {
        return stateStart;
    }

    public int getStateEnd()
    {
        return stateName != null ? stateName.length() + stateStart : 0;
    }

    /**
     * Get the raw attribute value for the named attribute. This value does not
     * include the quotes.
     * 
     * @param attributeName name of the attribute
     * @return value of the attribute (or null, if no such attribute exists)
     */
    // TODO Rename to getAttributevalue()
    public String getRawAttributeValue(String attributeName)
    {
        IMXMLTagAttributeData attributeData = attributeMap.get(attributeName);
        if (attributeData != null)
            return attributeData.getRawValue();
        return null;
    }

    /**
     * Gets the attr data associated for the given name
     * 
     * @param attributeName name of the attribute
     * @return the {@link MXMLTagAttributeData} (or null, if no such attribute
     * exists)
     */
    public IMXMLTagAttributeData getTagAttributeData(String attributeName)
    {
        return attributeMap.get(attributeName);
    }

    /**
     * Get the start position of the tag name
     * 
     * @return the start position of the tag name
     */
    public int getNameStart()
    {
        return nameStart;
    }

    /**
     * Get the end position of the tag name
     * 
     * @return the end position of the tag name
     */
    public int getNameEnd()
    {
        return nameStart + tagName.length();
    }

    /**
     * Get the start position of the state name
     * 
     * @return the start position of the tag name
     */
    public int getStateNameStart()
    {
        return stateName != null ? stateStart : -1;
    }

    /**
     * Get the end position of the state name
     * 
     * @return the end position of the tag name
     */
    public int getStateNameEnd()
    {
        return getStateEnd();
    }

    /**
     * Get the start position of the attribute list (after the first whitespace
     * after the tag name).
     * 
     * @return the start position of the attribute list
     */
    public int getAttributesStart()
    {
        return attributesStart;
    }

    /**
     * Get the end position of the attribute list (before the ">" or "/>" or the
     * start of the next tag).
     * 
     * @return the end position of the attribute list
     */
    public int getAttributesEnd()
    {
        return getAbsoluteEnd(); //attr end is just the end of this tag unit
    }

    /**
     * Does the offset fall inside this tag's attribute list?
     * 
     * @param offset test offset
     * @return true iff the offset falls inside this tag's attribute list
     */
    public boolean isOffsetInAttributeList(int offset)
    {
        return MXMLData.contains(attributesStart, getAbsoluteEnd(), offset);
    }

    public boolean isInsideStateName(int offset)
    {
        if (stateName != null)
            return MXMLData.contains(getStateStart(), getStateEnd(), offset);
        return false;
    }

    /**
     * Does the offset fall inside this tag's contents?
     * 
     * @param offset test offset
     * @return true iff the offset falls inside this tag's contents
     */
    public boolean isOffsetInsideContents(int offset)
    {
        return MXMLData.contains(getContentStart(),
                getContentEnd(), // was getContentsEnd (plural)
                offset);
    }

    /**
     * Get all of the attribute names in this tag
     * 
     * @return all of the attribute names (as a String [])
     */
    public String[] getAttributeNames()
    {
        String[] attributeNames = new String[attributes.length];
        for (int i = 0; i < attributeNames.length; i++)
        {
            attributeNames[i] = attributes[i].getName();
        }
        return attributeNames;
    }

    /**
     * Get all of the attributes in this tag
     * 
     * @return all of the attributes (as a MXMLTagAttributeData [])
     */
    public IMXMLTagAttributeData[] getAttributeDatas()
    {
        return attributes;
    }

    /**
     * Find the attribute that contains the offset
     * 
     * @param offset test offset
     * @return the attribute (or null, if no attribute contains the offset)
     */
    public IMXMLTagAttributeData findAttributeContainingOffset(int offset)
    {
        // Find the last attribute that starts to the left of offset
        IMXMLTagAttributeData lastAttribute = null;
        IMXMLTagAttributeData attribute = null;
        for (int i = 0; i < attributes.length; i++)
        {
            attribute = attributes[i];
            if (attribute.getAbsoluteStart() < offset)
                lastAttribute = attribute;
            else
                break;
        }
        // That last attribute is good if it's unfinished or if it contains the offset in question
        if (lastAttribute != null && (lastAttribute.getAbsoluteEnd() == -1 || lastAttribute.getAbsoluteEnd() >= offset))
            return lastAttribute;
        return null;
    }

    /**
     * Collect the text contents of this tag (like <mx:Script> or <mx:Style>)
     * 
     * @return the text contents of this tag
     */
    public int[] getTextContentOffsets()
    {
        int startOffset = -1;
        int endOffset = -1;
        if (!isEmptyTag() && isOpenTag())
        {
            MXMLUnitData[] list = getParent().getUnits();
            int index = getIndex() + 1;
            for (int i = index; i < list.length; i++)
            {
                MXMLUnitData next = list[i];
                if (next instanceof MXMLTextData && ((MXMLTextData)next).getTextType() == TextType.WHITESPACE)
                    continue;
                if (next.isText())
                {
                    startOffset = startOffset == -1 ? next.getAbsoluteStart() : startOffset;
                    endOffset = next.getAbsoluteEnd();
                }
                else
                {
                    if (startOffset == -1 && endOffset == -1)
                    {
                        startOffset = getAbsoluteEnd();
                        endOffset = next.getAbsoluteStart();
                    }
                    break;
                }
            }
        }
        return new int[] {startOffset, endOffset};
    }

    /**
     * Gets this compilable text inside this tag. The compilable text may appear
     * as multiple child text units, and there may also be comments (which are
     * ignored). If any child units are tags rather than text, they are simply
     * ignored.
     * 
     * @return The compilable text inside this tag.
     */
    public String getCompilableText()
    {
        StringBuilder sb = new StringBuilder();

        for (MXMLUnitData unit = getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit.isText())
                sb.append(((MXMLTextData)unit).getCompilableText());
        }

        return sb.toString();
    }

    /**
     * Return the close tag that matches this tag.
     * <p>
     * Returns null if this tag is a close or empty tag.
     * <p>
     * Returns null if a surrounding tag is unbalanced; this is determined by
     * backing up to the innermost parent tag with a different tag.
     * <p>
     * {@code <a> <b> <b> <-- find matching for this one will return null
     * </b> </a> * }
     */
    public MXMLTagData findMatchingEndTag()
    {
        return findMatchingEndTag(false);
    }

    /**
     * Return the close tag that matches this tag.
     * <p>
     * Returns null if this tag is a close or empty tag.
     * <p>
     * Returns null if a surrounding tag is unbalanced ONLY if includeImplicit
     * is false; this is determined by backing up to the innermost parent tag
     * with a different tag.
     * <p>
     * {@code <a> <b> <b> <-- find matching for this one will return null
     * </b> </a> * }
     */
    public MXMLTagData findMatchingEndTag(boolean includeImplicit)
    {
        if (isCloseTag() || isEmptyTag())
            return null;
        // Back up to the first surrounding tag that has a different name, and ensure
        // that *it* is balanced, saving our expected return value along the way.
        MXMLTagData startTag = this;
        while (true)
        {
            MXMLTagData parentTag = startTag.getContainingTag(startTag.getAbsoluteStart());
            if (parentTag == null)
                break;
            startTag = parentTag;
            if (!parentTag.getName().equals(this.getName()))
            {
                break;
            }
        }
        // Now walk through the tags starting at startTag.  Once we pop ourselves
        // off the tagStack, we've found our candidate result -- but keep going
        // until the stack is null, to ensure that we're balanced out to the
        // surrounding tag.
        MXMLUnitData[] list = getParent().getUnits();
        FastStack<MXMLTagData> tagStack = new FastStack<MXMLTagData>();
        MXMLTagData result = null;
        for (int i = startTag.getIndex(); i < list.length; i++)
        {
            MXMLUnitData curUnit = list[i];
            if (curUnit.isTag())
            {
                MXMLTagData curTag = (MXMLTagData)curUnit;
                if (curTag.isEmptyTag())
                {
                    // do nothing for empty tags.
                }
                else if (curTag.isOpenTag())
                {
                    tagStack.push(curTag);
                }
                else if (curTag.isCloseTag())
                {
                    if (tagStack.isEmpty())
                    {
                        // document is unbalanced.
                        return null;
                    }
                    MXMLTagData pop = tagStack.pop();

                    //check the short name in case the namespace is not spelled properly
                    if (!pop.getName().equals(curTag.getName()) && !pop.getShortName().equals(curTag.getShortName()))
                    {
                        // document is unbalanced.
                        return null;
                    }
                    if (pop == this)
                    {
                        // This is our result -- remember it.
                        result = curTag;
                    }
                    if (tagStack.isEmpty())
                    {
                        if (result.isImplicit() && !includeImplicit)
                            return null;
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns true if this tag does not actually exist within the MXML Document
     * that is its source
     * 
     * @return true if we are implicit
     */
    public boolean isImplicit()
    {
        return false;
    }

    /**
     * determines if the current tag has an end tag. Will return true if this
     * tag is a close tag or an end tag. If the document is not balanced, we
     * will return false. If the end tag was implicit, we will return false
     * 
     * @return true if this tag has an end tag
     */
    public boolean hasEndTag()
    {
        if (isCloseTag() || isEmptyTag())
            return explicitCloseToken;
        MXMLTagData tagData = findMatchingEndTag();
        return tagData != null && !tagData.isImplicit();
    }

    /**
     * Gets the first child unit inside this tag. The child unit may be a tag or
     * text. If there is no child unit, this method returns <code>null</code>.
     * 
     * @return The first child unit inside this tag.
     */
    public MXMLUnitData getFirstChildUnit()
    {
        // If this tag is <foo/> then it has no child units.
        if (!isOpenAndNotEmptyTag())
            return null;

        MXMLUnitData next = getNext();

        // If this tag is followed immediately by its end tag,
        // as in <foo></foo>, then it has no child units.
        if (next == findMatchingEndTag())
            return null;

        // Otherwise, the first child unit is the unit after the tag.
        return next;
    }

    /**
     * Get the first child open tag of this tag. "First Child" is defined as the
     * first open (or maybe empty) tag found before a close tag. If this is a
     * close tag, starts looking after the corresponding start tag.
     * 
     * @return Child tag, or null if none.
     */
    public MXMLTagData getFirstChild(boolean includeEmptyTags)
    {
        MXMLTagData nextTag = null;
        if (isEmptyTag())
            return null;
        if (isOpenTag())
        {
            nextTag = getNextTag();
        }
        else
        {
            // This is a close tag.  Start at the corresponding open tag.
            MXMLTagData openTag = getContainingTag(getAbsoluteStart());
            nextTag = openTag.getNextTag();
        }
        // Skip any text blocks to find the next actual tag.  If it's an open tag,
        // that is our first child.  Otherwise it's a close tag, return null.
        while (true)
        {
            if (nextTag == null || nextTag.isCloseTag())
                return null;
            if (nextTag.isOpenAndNotEmptyTag() || (nextTag.isEmptyTag() && includeEmptyTags))
                return nextTag;
            nextTag = nextTag.getNextTag();
        }
    }

    /**
     * Get the next sibling open tag of this tag. "Sibling" is defined as the
     * first open (or maybe empty) tag after this tag's close tag. If this is a
     * close tag, starts looking after the corresponding start tag.
     * 
     * @return Sibling, or null if none.
     */
    public MXMLTagData getNextSibling(boolean includeEmptyTags)
    {
        MXMLTagData nextTag = null;
        // Be sure we're starting at the close tag, then get the next tag.
        if (isCloseTag() || isEmptyTag())
        {
            nextTag = getNextTag();
        }
        else
        {
            MXMLTagData endTag = findMatchingEndTag();
            if (endTag == null)
                return null;
            nextTag = endTag.getNextTag();
        }
        while (true)
        {
            if (nextTag == null || nextTag.isCloseTag())
                return null;
            if (nextTag.isOpenAndNotEmptyTag() || (nextTag.isEmptyTag() && includeEmptyTags))
                return nextTag;
            nextTag = nextTag.getNextTag();
        }
    }

    /**
     * Get the start tags for all children of this tag.
     * 
     * @param includeEmptyTags <code>true</code> if empty tags should be included.
     * @return Array of children.
     */
    public MXMLTagData[] getChildren(boolean includeEmptyTags)
    {
        ArrayList<MXMLTagData> children = new ArrayList<MXMLTagData>();
        MXMLTagData child = getFirstChild(includeEmptyTags);
        while (child != null)
        {
            children.add(child);
            child = child.getNextSibling(includeEmptyTags);
        }
        return children.toArray(new MXMLTagData[0]);
    }

    /**
     * Return the parent tag of this tag. If the document is not balanced before
     * this tag, returns null.
     */
    public MXMLTagData getParentTag()
    {
        MXMLUnitData data = getParentUnitData();
        if (data instanceof MXMLTagData)
            return (MXMLTagData)data;
        return null;
    }

    public SourceLocation getLocationOfChildUnits()
    {
        String sourcePath = getSourcePath();
        int start = getStart();
        int end = getEnd();
        int line = getLine();
        int column = getColumn();

        boolean foundFirstChild = false;

        for (MXMLUnitData unit = getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (!foundFirstChild)
            {
                sourcePath = unit.getSourcePath();
                start = unit.getStart();
                line = unit.getLine();
                column = unit.getColumn();

                foundFirstChild = true;
            }

            end = unit.getEnd();
        }

        return new SourceLocation(sourcePath, start, end, line, column);
    }

    /**
     * Verifies that this tag and its attributes have their source location
     * information set.
     * <p>
     * This is used only in asserts.
     */
    @Override
    public boolean verify()
    {
        // Verify the source location.
        super.verify();

        // Verify the attributes.
        for (IMXMLTagAttributeData attribute : getAttributeDatas())
        {
            ((MXMLTagAttributeData)attribute).verify();
        }

        return true;
    }

    /**
     * For debugging only. This format is nice in the Eclipse debugger.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append('<');
        if (isCloseTag())
            sb.append('/');
        sb.append(getName());
        if (isEmptyTag())
            sb.append('/');
        sb.append('>');

        sb.append(' ');

        // Display line, column, start, end as "17:5 160-188"
        sb.append(super.toString());

        // add content range as "(161-187)"
        sb.append(' ');
        sb.append('(');
        sb.append(nameStart);
        sb.append('-');
        sb.append(contentEnd);
        sb.append(')');

        return sb.toString();
    }

    /**
     * For debugging only. This format is nice in a text file.
     */
    @Override
    public String toDumpString()
    {
        return buildDumpString(false);
    }
    
    @Override
    public String buildDumpString(boolean skipSrcPath) {
        StringBuilder sb = new StringBuilder();

        sb.append(super.buildDumpString(skipSrcPath));

        sb.append('\t');

        sb.append('|');
        sb.append(getName());
        sb.append('|');

        return sb.toString();
    }
}
