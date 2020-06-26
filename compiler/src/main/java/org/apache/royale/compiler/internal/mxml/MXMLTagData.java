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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.MutablePrefixMap;
import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.common.PrefixedXMLName;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLNamespaceAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.compiler.mxml.IMXMLTextData.TextType;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDuplicateAttributeProblem;
import org.apache.royale.compiler.problems.MXMLUnclosedTagProblem;
import org.apache.royale.compiler.problems.SyntaxProblem;
import org.apache.royale.utils.FastStack;

/**
 * Encapsulation of an open tag, a close tag, or an empty tag in MXML.
 */
public class MXMLTagData extends MXMLUnitData implements IMXMLTagData
{
    private static final IMXMLTagAttributeData[] NO_ATTRIBUTES = new IMXMLTagAttributeData[0];

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
        this.setEndLine(other.getEndLine());
        this.setEndColumn(other.getEndColumn());
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
    protected Map<String, IMXMLTagAttributeData> attributeMap;

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

    /**
     * The problems list in case we find a problem later
     */
    Collection<ICompilerProblem> problems;
    
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

    protected void setProblems(Collection<ICompilerProblem> problems)
    {
        this.problems = problems;
    }

    @SuppressWarnings("fallthrough")
    MutablePrefixMap init(IMXMLData mxmlData, MXMLToken nameToken, ListIterator<MXMLToken> tokenIterator, MXMLDialect dialect, IFileSpecification spec, Collection<ICompilerProblem> problems)
    {
    	this.problems = problems;
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
        tagName = splitState.getBaseName();
        if (splitState.getStateName() != null)
        {
            stateName = splitState.getStateName();
            stateStart = nameToken.getStart() + splitState.getStateNameOffset();
        }

        nameType = nameToken.getType();

        int nameEnd = nameStart + tagName.length();
        int contentEnd = nameEnd;
        setTagOffsets(startOffset, nameEnd, nameStart, contentEnd);
        setColumn(nameToken.getColumn());
        setLine(nameToken.getLine());
        setEndColumn(nameToken.getEndColumn());
        setEndLine(nameToken.getEndLine());
        attributesStart = getNameEnd();
        ArrayList<IMXMLTagAttributeData> attrs = new ArrayList<IMXMLTagAttributeData>();
        attributeMap = new LinkedHashMap<String, IMXMLTagAttributeData>(); //preserve order of attrs
        boolean foundTagEnd = false;
        boolean putTokenBack = false; // This is a pre-royale algorithm that helped recover from tag nesting errors
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
                        map.add(((IMXMLNamespaceAttributeData)attribute).getNamespacePrefix(), ((IMXMLNamespaceAttributeData)attribute).getNamespace());
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
                            setTagOffsets(getAbsoluteStart() == -1 ? next.getStart() : getAbsoluteStart(), getAbsoluteEnd() == -1 ? next.getStart() : getAbsoluteEnd(), nameStart == -1 ? next.getStart() : nameStart, contentEnd == -1 ? next.getStart() : contentEnd);
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
        assert (start <= contentStart) && ((contentStart <= contentEnd) || contentEnd == -1) && ((contentEnd <= end) || (end == -1));
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

    @Override
    public boolean isTag()
    {
        return true;
    }

    @Override
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
                    IMXMLUnitData unit = getParent().getUnit(index);
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

    @Override
    public boolean isOpenAndNotEmptyTag()
    {
        return (isOpenTag() && !isEmptyTag());
    }

    @Override
    public boolean isCloseTag()
    {
        return nameType == MXMLTokenTypes.TOKEN_CLOSE_TAG_START;
    }

    @Override
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

    @Override
    public XMLName getXMLName()
    {
        return new XMLName(getURI(), getShortName());
    }

    @Override
    public PrefixMap getPrefixMap()
    {
        return getParent().getPrefixMapForData(this);
    }

    @Override
    public PrefixMap getCompositePrefixMap()
    {
        MutablePrefixMap compMap = new MutablePrefixMap();
        IMXMLTagData lookingAt = this;
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

    @Override
    public String getPrefix()
    {
        String name = getName();
        int i = name.indexOf(':');
        return i != -1 ? name.substring(0, i) : "";
    }

    @Override
    public String getShortName()
    {
        String name = getName();
        int i = name.indexOf(':');
        return i != -1 ? name.substring(i + 1) : name;
    }

    @Override
    public String getURI()
    {
        if (uri == null)
        {
            //walk up our chain to find the correct uri for our namespace.  first one wins
            String prefix = getPrefix();
            IMXMLTagData lookingAt = this;
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

    @Override
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

    @Override
    public String getRawAttributeValue(String attributeName)
    {
        IMXMLTagAttributeData attributeData = attributeMap.get(attributeName);
        if (attributeData != null)
            return attributeData.getRawValue();
        return null;
    }

    @Override
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

    @Override
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
        return MXMLData.contains(getContentStart(), getContentEnd(), // was getContentsEnd (plural)
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

    @Override
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
            IMXMLUnitData[] list = getParent().getUnits();
            int index = getIndex() + 1;
            for (int i = index; i < list.length; i++)
            {
                IMXMLUnitData next = list[i];
                if (next instanceof IMXMLTextData && ((IMXMLTextData)next).getTextType() == TextType.WHITESPACE)
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

    @Override
    public String getCompilableText()
    {
        StringBuilder sb = new StringBuilder();

        for (IMXMLUnitData unit = getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit.isText())
                sb.append(((IMXMLTextData)unit).getCompilableText());
        }

        return sb.toString();
    }

    @Override
    public IMXMLTagData findMatchingEndTag()
    {
        return findMatchingEndTag(false);
    }

    /**
     * Finds the close tag that matches this tag.
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
    public IMXMLTagData findMatchingEndTag(boolean includeImplicit)
    {
        if (isCloseTag() || isEmptyTag())
            return null;
        // Back up to the first surrounding tag that has a different name, and ensure
        // that *it* is balanced, saving our expected return value along the way.
        IMXMLTagData startTag = this;
        while (true)
        {
            IMXMLTagData parentTag = startTag.getContainingTag(startTag.getAbsoluteStart());
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
        IMXMLUnitData[] list = getParent().getUnits();
        FastStack<IMXMLTagData> tagStack = new FastStack<IMXMLTagData>();
        IMXMLTagData result = null;
        for (int i = startTag.getIndex(); i < list.length; i++)
        {
            IMXMLUnitData curUnit = list[i];
            if (curUnit.isTag())
            {
                IMXMLTagData curTag = (IMXMLTagData)curUnit;
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
                    IMXMLTagData pop = tagStack.pop();

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
                        if (result != null && result.isImplicit() && !includeImplicit)
                            return null;
                        return result;
                    }
                }
            }
        }
        if (!tagStack.isEmpty())
        {
            IMXMLTagData pop = tagStack.pop();
            problems.add(new MXMLUnclosedTagProblem(pop, pop.getName()));
        }
        return null;
    }

    @Override
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
        IMXMLTagData tagData = findMatchingEndTag();
        return tagData != null && !tagData.isImplicit();
    }

    @Override
    public IMXMLUnitData getFirstChildUnit()
    {
        // If this tag is <foo/> then it has no child units.
        if (!isOpenAndNotEmptyTag())
            return null;

        IMXMLUnitData next = getNext();

        // If this tag is followed immediately by its end tag,
        // as in <foo></foo>, then it has no child units.
        if (next == findMatchingEndTag())
            return null;

        // Otherwise, the first child unit is the unit after the tag.
        return next;
    }

    @Override
    public IMXMLTagData getFirstChild(boolean includeEmptyTags)
    {
        IMXMLTagData nextTag = null;
        if (isEmptyTag())
            return null;
        if (isOpenTag())
        {
            nextTag = getNextTag();
        }
        else
        {
            // This is a close tag.  Start at the corresponding open tag.
            IMXMLTagData openTag = getContainingTag(getAbsoluteStart());
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

    @Override
    public IMXMLTagData getNextSibling(boolean includeEmptyTags)
    {
        IMXMLTagData nextTag = null;
        // Be sure we're starting at the close tag, then get the next tag.
        if (isCloseTag() || isEmptyTag())
        {
            nextTag = getNextTag();
        }
        else
        {
            IMXMLTagData endTag = findMatchingEndTag();
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
     * @param includeEmptyTags <code>true</code> if empty tags should be
     * included.
     * @return Array of children.
     */
    public IMXMLTagData[] getChildren(boolean includeEmptyTags)
    {
        ArrayList<IMXMLTagData> children = new ArrayList<IMXMLTagData>();
        IMXMLTagData child = getFirstChild(includeEmptyTags);
        while (child != null)
        {
            children.add(child);
            child = child.getNextSibling(includeEmptyTags);
        }
        return children.toArray(new IMXMLTagData[0]);
    }

    /**
     * Return the parent tag of this tag. If the document is not balanced before
     * this tag, returns null.
     */
    public IMXMLTagData getParentTag()
    {
        IMXMLUnitData data = getParentUnitData();
        if (data instanceof IMXMLTagData)
            return (IMXMLTagData)data;
        return null;
    }

    @Override
    public ISourceLocation getLocationOfChildUnits()
    {
        String sourcePath = getSourcePath();
        int start = getStart();
        int end = getEnd();
        int line = getLine();
        int column = getColumn();
        int endLine = getEndLine();
        int endColumn = getEndColumn();

        boolean foundFirstChild = false;

        for (IMXMLUnitData unit = getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (!foundFirstChild)
            {
                sourcePath = unit.getSourcePath();
                start = unit.getStart();
                line = unit.getLine();
                column = unit.getColumn();
                endLine = unit.getEndLine();
                endColumn = unit.getEndColumn();

                foundFirstChild = true;
            }

            end = unit.getEnd();
        }

        return new SourceLocation(sourcePath, start, end, line, column, endLine, endColumn);
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
    public String buildDumpString(boolean skipSrcPath)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(super.buildDumpString(skipSrcPath));

        sb.append('\t');

        sb.append('|');
        sb.append(getName());
        sb.append('|');

        return sb.toString();
    }
    
    public String stringify()
    {
    	StringBuilder sb = new StringBuilder();
        sb.append('<');
        if (isCloseTag())
            sb.append('/');
        sb.append(getName());

        // Verify the attributes.
        for (IMXMLTagAttributeData attribute : getAttributeDatas())
        {
        	sb.append(" ");
            sb.append(attribute.getName());
            sb.append("=\"");
            sb.append(attribute.getRawValue());
            sb.append("\"");
        }
        if (isEmptyTag())
            sb.append('/');
        sb.append('>');
        for (IMXMLUnitData unit = getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
        {
            if (unit.isText())
                sb.append(((IMXMLTextData)unit).getCompilableText());
            else if (unit instanceof MXMLTagData)
            	sb.append(((MXMLTagData)unit).stringify());
        }
        if (!isEmptyTag())
        {
            sb.append('<');
            sb.append('/');
            sb.append(getName());
            sb.append('>');
        }
        return sb.toString();
    }
}
