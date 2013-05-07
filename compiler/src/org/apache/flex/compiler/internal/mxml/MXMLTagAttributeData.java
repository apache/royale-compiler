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

package org.apache.flex.compiler.internal.mxml;

import java.util.Collection;
import java.util.ListIterator;

import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.common.PrefixMap;
import org.apache.flex.compiler.common.SourceLocation;
import org.apache.flex.compiler.common.XMLName;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.parsing.ISourceFragment;
import org.apache.flex.compiler.internal.parsing.mxml.MXMLToken;
import org.apache.flex.compiler.mxml.IMXMLTagAttributeData;
import org.apache.flex.compiler.mxml.IMXMLTagData;
import org.apache.flex.compiler.parsing.MXMLTokenTypes;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.SyntaxProblem;

/**
 * Encapsulation of a tag attribute in MXML
 */
public class MXMLTagAttributeData extends SourceLocation implements
        IMXMLTagAttributeData
{
    /**
     * Constructor.
     */
    MXMLTagAttributeData(MXMLToken nameToken, ListIterator<MXMLToken> tokenIterator, MXMLDialect mxmlDialect, IFileSpecification spec, Collection<ICompilerProblem> problems)
    {
        setStart(nameToken.getStart());
        setLine(nameToken.getLine());
        setColumn(nameToken.getColumn());
        setEnd(nameToken.getEnd());

        // Deal with name if it is of the form name.state
        MXMLStateSplitter splitState = new MXMLStateSplitter(nameToken, mxmlDialect, problems, spec);
        attributeName = splitState.baseName;
        if (splitState.stateName != null)
        {
            stateName = splitState.stateName;
            stateStart = nameToken.getStart() + splitState.stateNameOffset;
        }

        MXMLToken token = null;

        // Look for "=" token
        if (tokenIterator.hasNext())
        {
            token = tokenIterator.next();

            if (token.getType() != MXMLTokenTypes.TOKEN_EQUALS)
            {
                problems.add(new SyntaxProblem(token));
                // need to restore the token position in the error
                // case to handle error recovery otherwise the any
                // trees after this won't be created
                tokenIterator.previous();
                return;
            }

            valueStart = token.getEnd() + 1; //set the attributes start to right after the equals until we have a value
            valueLine = token.getLine();
            valueColumn = token.getColumn();
        }

        // Look for value token
        while (tokenIterator.hasNext())
        {
            token = tokenIterator.next();
            if (token.getType() == MXMLTokenTypes.TOKEN_STRING)
            {
                valueIncludingDelimiters = token.getText();
            }
            else
            {
                if (!MXMLToken.isTagEnd(token.getType()) && token.getType() != MXMLTokenTypes.TOKEN_NAME)
                {
                    // if we error out early, push back token - it may be start of next tag
                    // this is "pre-falcon" repair that was lost
                    tokenIterator.previous();
                    problems.add(new SyntaxProblem(token));
                }
                else
                {
                    tokenIterator.previous();
                }
                break;
            }
        }
    }

    /**
     * The MXML tag that contains this attribute
     */
    protected IMXMLTagData parent;

    /**
     * The URI specified by this attribute's prefix.
     */
    protected String uri;

    /**
     * The name of this attribute.
     */
    protected String attributeName;

    /**
     * The offset at which the optional state starts.
     */
    protected int stateStart;
    
    /**
     * The attribute value, including any delimiters.
     */
    protected String valueIncludingDelimiters;

    /**
     * The offset at which the attribute value starts
     */
    protected int valueStart;

    /**
     * The line on which the attribute value starts
     */
    protected int valueLine;

    /**
     * The column at which the attribute value starts
     */
    protected int valueColumn;

    /**
     * The name of this state, if it exists
     */
    protected String stateName;

    //
    // Object overrides.
    //

    // For debugging only. This format is nice in the Eclipse debugger.
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        buildAttributeString(false);
        return sb.toString();
    }

    /**
     * For unit tests only.
     * 
     * @return name value and offsets in string form
     */
    public String buildAttributeString(boolean skipSrcPath)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append('=');
        sb.append('"');
        sb.append(getRawValue());
        sb.append('"');

        sb.append(' ');

        // Display line, column, start, and end as "17:5 160-188".
        if (skipSrcPath)
            sb.append(getOffsetsString());
        else
            sb.append(super.toString());
        return sb.toString();
    }

    //
    // Other methods
    //

    @Override
    public IMXMLTagData getParent()
    {
        return parent;
    }

    /**
     * Sets this attribute's tag.
     * 
     * @param parent MXML tag containing this attribute
     */
    public void setParent(IMXMLTagData parent)
    {
        this.parent = parent;
        setSourcePath(parent.getSourcePath());
    }

    /**
     * Adjust all associated offsets by the adjustment amount
     * 
     * @param offsetAdjustment amount to add to offsets
     */
    public void adjustOffsets(int offsetAdjustment)
    {
        if (attributeName != null)
        {
            setStart(getAbsoluteStart() + offsetAdjustment);
            setEnd(getAbsoluteEnd() + offsetAdjustment);
        }

        if (hasValue())
            valueStart += offsetAdjustment;

        if (stateName != null)
            stateStart += offsetAdjustment;
    }

    @Override
    public String getName()
    {
        return attributeName;
    }

    @Override
    public String getStateName()
    {
        return stateName != null ? stateName : "";
    }

    /**
     * Checks whether this attribute is associated with a state.
     * 
     * @return True if a state association exists.
     */
    public boolean hasState()
    {
        return stateName != null;
    }

    @Override
    public boolean hasValue()
    {
        return getRawValue() != null;
    }

    /**
     * Get the attribute value as a String (with quotes)
     * 
     * @return attribute value (with quotes)
     */
    public String getValueWithQuotes()
    {
        return valueIncludingDelimiters;
    }

    @Override
    public String getRawValue()
    {
        String value = getValueWithQuotes();

        if (value != null && value.length() > 0)
        {
            // length can be one in case of invalid data and then the substring() call fails
            // so, handle it here
            if (value.charAt(0) == value.charAt(value.length() - 1) && value.length() != 1)
                value = value.substring(1, value.length() - 1);
            else
                value = value.substring(1);
        }

        return value;
    }

    @Override
    public ISourceFragment[] getValueFragments(Collection<ICompilerProblem> problems)
    {
        String value = getRawValue();
        ISourceLocation location = getValueLocation();
        MXMLDialect mxmlDialect = getMXMLDialect();

        return EntityProcessor.parse(value, location, mxmlDialect, problems);
    }

    public IFileSpecification getSource()
    {
        return getParent().getSource();
    }

    /**
     * Get this unit's line number.
     * 
     * @return end offset
     */
    public final int getNameLine()
    {
        return getLine();
    }

    /**
     * Get this unit's column number.
     * 
     * @return end offset
     */
    public final int getNameColumn()
    {
        return getColumn();
    }

    public int getNameStart()
    {
        return getAbsoluteStart();
    }

    /**
     * Get this attribute's name's end offset
     * 
     * @return name end offset
     */
    public int getNameEnd()
    {
        return getAbsoluteStart() + attributeName.length();
    }

    @Override
    public int getValueStart()
    {
        return hasValue() ? valueStart + 1 : 0;
    }

    @Override
    public int getValueEnd()
    {
        if (hasValue())
              return getValueStart() + getRawValue().length();

        // If there is no valid "end", then we must return -1. Callers depend on this.
        // See MXMLTagData.findArttributeContainingOffset for an example
        return -1;
    }

    @Override
    public int getValueLine()
    {
        return hasValue() ? valueLine : 0;
    }

    @Override
    public int getValueColumn()
    {
        return hasValue() ? valueColumn + 1 : 0;
    }

    @Override
    public SourceLocation getValueLocation()
    {
        return new SourceLocation(getSourcePath(), getValueStart(), getValueEnd(), getValueLine(), getValueColumn());
    }

    /**
     * Get this attribute's state start offset if a state token is present other
     * wise zero.
     * 
     * @return state start offset or zero
     */
    public int getStateStart()
    {
        return stateName != null ? stateStart : 0;
    }

    /**
     * Get this attribute's state tokens end offset if a state token is present
     * other wise zero.
     * 
     * @return state start offset or zero
     */
    public int getStateEnd()
    {
        return stateName != null ? stateStart + stateName.length() : 0;
    }

    /**
     * Does this value have a closing quote character?
     * 
     * @return true if this value has a closing quote character
     */
    protected boolean valueIsWellFormed()
    {
        // If there is a value, it came from a string token.  We know (from the
        // RawTagTokenizer) that this means it starts with a quote character.  If
        // it ends with the same quote character, it's well formed.
        if (hasValue())
        {
            char firstChar = valueIncludingDelimiters.charAt(0);
            char lastChar = valueIncludingDelimiters.charAt(valueIncludingDelimiters.length() - 1);
            return (firstChar == '"' || firstChar == '\'') && firstChar == lastChar;
        }

        return false;
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
        return parent.getCompositePrefixMap();
    }

    /**
     * Does the offset fall inside the bounds of the attribute name?
     * 
     * @param offset test offset
     * @return true if the offset falls within the attribute name
     */
    public boolean isInsideName(int offset)
    {
        if (attributeName != null)
            return MXMLData.contains(getNameStart(), getNameEnd(), offset);

        return false;
    }

    public boolean isInsideStateName(int offset)
    {
        if (stateName != null)
            return MXMLData.contains(getStateStart(), getStateEnd(), offset);

        return false;
    }

    /**
     * Does the offset fall inside the bounds of the attribute value?
     * 
     * @param offset test offset
     * @return true if the offset falls within the attribute value
     */
    public boolean isInsideValue(int offset)
    {
        if (hasValue())
            return MXMLData.contains(getValueStart() - 1, getValueEnd(), offset);

        return false;
    }

    @Override
    public String getPrefix()
    {
        String name = getName();
        int i = name.indexOf(':');
        return i != -1 ? name.substring(0, i) : null;
    }

    @Override
    public String getShortName()
    {
        String name = getName();
        int i = name.indexOf(':');
        return i != -1 ? name.substring(i + 1) : name;
    }

    @Override
    public XMLName getXMLName()
    {
        return new XMLName(getURI(), getShortName());
    }

    @Override
    public String getURI()
    {
        if (uri == null)
        {
            //walk up our chain to find the correct uri for our namespace.  first one wins
            String prefix = getPrefix();
            if (prefix == null)
                return null;

            IMXMLTagData lookingAt = parent;

            // For attributes with prefix, parent's parent can be null if
            // parent is the root tag 
            while (lookingAt != null && lookingAt.getParent() != null)
            {
                PrefixMap depth = lookingAt.getParent().getPrefixMapForData(lookingAt);
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

    void invalidateURI()
    {
        uri = null;
    }

    @Override
    public MXMLDialect getMXMLDialect()
    {
        return getParent().getParent().getMXMLDialect();
    }

    @Override
    public boolean isSpecialAttribute(String name)
    {
        String languageURI = getMXMLDialect().getLanguageNamespace();

        return getName().equals(name) && (getPrefix() == null || getURI() == languageURI);
    }

    /**
     * Verifies that this attrobite has its source location information set.
     * <p>
     * This is used only in asserts.
     */
    public boolean verify()
    {
        // Verify the source location.
        assert getSourcePath() != null : "Attribute has null source path: " + toString();
        assert getStart() != UNKNOWN : "Attribute has unknown start: " + toString();
        assert getEnd() != UNKNOWN : "Attribute has unknown end: " + toString();
        assert getLine() != UNKNOWN : "Attribute has unknown line: " + toString();
        assert getColumn() != UNKNOWN : "Attribute has unknown column: " + toString();

        return true;
    }
}
