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

package org.apache.royale.abc.semantics;

import static org.apache.royale.abc.ABCConstants.*;

import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;

/**
 * A representation of a <a href="http://learn.adobe.com/wiki/display/AVM2/4.3+Constant+pool">name</a>.
 * <p>
 * All names are stored as multinames in the ABC, but this view of a name differentiates
 * regular multinames, runtime multinames, and type names.
 */
public class Name
{
    /**
     * The default package namespace for an unqualified name.
     */
    public static final Namespace packageNs = new Namespace(CONSTANT_PackageNs);

    /**
     * A prime number unlikely to be a divisor of the hash table size, used to
     * generate composite hash keys.
     * 
     * @warn if you copy this, pick a new prime to generate distinct hash keys
     * in different classes.
     */
    private static final long PRIME_MULTIPLIER = 8887;

    /**
     * Construct the simplest kind of Name: one whose kind is CONSTANT_Qname and
     * whose namespace set contains the single package namespace with name "".
     * 
     * @param baseName The unqualified name.
     */
    public Name(String baseName)
    {
        this(Name.packageNs, baseName);
    }

    /**
     * Construct a name whose kind is CONSTANT_Qname, with the namespace and
     * base name specified.
     * 
     * @param ns - the Namespace.
     * @param baseName The unqualified name.
     */
    public Name(Namespace ns, String baseName)
    {
        this(CONSTANT_Qname, new Nsset(ns), baseName, null, null);
    }

    /**
     * Construct a Name whose kind is CONSTANT_Multiname.
     * 
     * @param multiname_qualifiers The namespace set for the Name.
     * @param baseName The base name for the Name.
     */
    public Name(Nsset multiname_qualifiers, String baseName)
    {
        this(CONSTANT_Multiname, multiname_qualifiers, baseName, null, null);
    }

    /**
     * Construct a Name of any kind except CONSTANT_TypeName.
     * 
     * @param kind The kind of the Name.
     * @param qualifiers The namespace set for the name.
     * @param baseName The base name for the Name.
     */
    public Name(int kind, Nsset qualifiers, String baseName)
    {
        this(kind, qualifiers, baseName, null, null);
        assert kind != CONSTANT_TypeName : "Use the Name(Name, Name) constructor to construct a Name with kind CONSTANT_TypeName";
    }

    /**
     * Construct a Name of kind CONSTANT_TypeName, which represents a
     * parameterized type such as C.&lt;T&gt;. getBaseName() and getQualifiers()
     * will return null, but getTypeNameBase() returns the Name for type C, and
     * getTypeNameParameter() returns the Name for type T.
     * 
     * @param typename_base The Name for type C.
     * @param typename_parameter The Name for type T.
     */
    public Name(Name typename_base, Name typename_parameter)
    {
        this(CONSTANT_TypeName, null, null, typename_base, typename_parameter);
    }

    /**
     * Private constructor delegated to by the public constructors accepts all
     * variations of state and sets them.
     * 
     * @param kind The kind of the Name.
     * @param qualifiers The namespace set for the name.
     * @param baseName The base name for the Name.
     * @param typename_base The Name for type C.
     * @param typename_parameter The Name for type T.
     */
    private Name(int kind, Nsset qualifiers, String baseName, Name typename_base, Name typename_parameter)
    {
        this.kind = kind;
        this.baseName = baseName;
        this.qualifiers = qualifiers;
        this.typeNameBase = typename_base;
        this.typeNameParameter = typename_parameter;
    }
    
    /**
     * The allowed name kinds are the following ABCConstants: CONSTANT_Qname,
     * CONSTANT_QnameA, CONSTANT_RTQname, CONSTANT_RTQnameA, CONSTANT_RTQnameL,
     * CONSTANT_RTQnameLA, and CONSTANT_Multiname, CONSTANT_MultinameA,
     * CONSTANT_MultinameL, CONSTANT_MultinameLA, CONSTANT_TypeName. The kind
     * determines which of the other fields are relevant, as follows:
     * 
     * <pre>
     * CONSTANT_Qname, CONSTANT_QnameA:
     *   baseName
     *   qualifiers
     * CONSTANT_RTQname, CONSTANT_RTQnameA:
     *   baseName
     * CONSTANT_RTQnameL, CONSTANT_RTQnameLA:
     *   <none>
     * CONSTANT_Multiname, CONSTANT_MultinameA:
     *   baseName
     *   qualifiers
     * CONSTANT_MultinameL, CONSTANT_MultinameLA:
     *   qualifiers
     * CONSTANT_TypeName:
     *   typeNameBase
     *   typeNameParameter
     * </pre>
     */
    private final int kind;

    /**
     * The base name for this Name.
     */
    private final String baseName;

    /**
     * The namespace set for this Name.
     */
    private final Nsset qualifiers;

    /**
     * The Name representing the base type of a parameterized type Name. (e.g.,
     * C for C.<T>).
     */
    private Name typeNameBase;

    /**
     * The Name representing the type parameter for a parameterized type Name.
     * (e.g., T for C.<T>). Only one type parameter is currently supported, to
     * avoid creating unnecessary arrays of length 1.
     * 
     * @warn this field may be null if the parameter is *.
     */
    private Name typeNameParameter;

    /**
     * @return a more readable description of this name's kind.
     */
    private String getKindString()
    {
        switch (kind)
        {
            case CONSTANT_Qname:
                return "Qname";
                
            case CONSTANT_QnameA:
                return "QnameA";
                
            case CONSTANT_Multiname:
                return "Multiname";
                
            case CONSTANT_MultinameA:
                return "MultinameA";
                
            case CONSTANT_MultinameL:
                return "MultinameL";
                
            case CONSTANT_MultinameLA:
                return "MultinameLA";
                
            case CONSTANT_TypeName:
                return "TypeName";
                
            case CONSTANT_RTQname:
                return "RTQname";
                
            case CONSTANT_RTQnameA:
                return "RTQnameA";
                
            case CONSTANT_RTQnameL:
                return "RTQnameL";
                
            case CONSTANT_RTQnameLA:
                return "RTQnameLA";
        }
        return "<Unknown kind>";
    }

    /**
     * @return true if the Name refers to an attribute.
     */
    public boolean isAttributeName()
    {
        switch (kind)
        {
            case CONSTANT_QnameA:
            case CONSTANT_MultinameA:
            case CONSTANT_MultinameLA:
            case CONSTANT_RTQnameA:
            case CONSTANT_RTQnameLA:
            {
                return true;
            }
            default:
            {
                return false;
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append(getKindString());

        switch (kind)
        {
            case CONSTANT_Qname:
            case CONSTANT_QnameA:
            case CONSTANT_Multiname:
            case CONSTANT_MultinameA:
            {
                result.append(": ");
                // Display the base name first since the qualifiers can be very long
                // and you don't want to have to scroll to see the base name.
                result.append(getBaseName());
                result.append("::");
                if (qualifiers != null)
                {
                    result.append(qualifiers.toString());
                }
                break;
            }
            case CONSTANT_RTQname:
            case CONSTANT_RTQnameA:
            {
                result.append(": ");
                result.append(getBaseName());
                break;
            }
            case CONSTANT_RTQnameL:
            case CONSTANT_RTQnameLA:
            {
                break;
            }
            case CONSTANT_MultinameL:
            case CONSTANT_MultinameLA:
            {
                result.append(": ");
                if (qualifiers != null)
                {
                    result.append(qualifiers.toString());
                }
                break;
            }
            case CONSTANT_TypeName:
            {
                result.append(": ");
                result.append(typeNameBase.toString());
                result.append(".<");
                if (typeNameParameter != null)
                    result.append(typeNameParameter.toString());
                else
                    result.append("*");
                result.append(">");
                break;
            }
        }

        return result.toString();
    }

    /**
     * Compare two Names for equality.
     * 
     * @param other - the Name to compare to.
     * @return true if the components of these Names are equal.
     */
    private boolean isEqualTo(Name other)
    {
        //  Checking kind has already been done (via the hashCode())
        //  when called from equals().
        boolean result = true; /* this.kind == other.kind; */

        if (kind != CONSTANT_TypeName)
        {
            //  Check qualifiers, then base.
            if (this.qualifiers == other.qualifiers)
            {
                // result = true;
            }
            else if (this.qualifiers != null && other.qualifiers != null)
            {
                result = this.qualifiers.equals(other.qualifiers);
            }
            else
            {
                //  One qualifier is null, but not both
                result = false;
            }

            if (result)
            {
                String this_base = this.getBaseName();
                String other_base = other.getBaseName();

                if (this_base != null && other_base != null)
                {
                    result = this_base.equals(other_base);
                }
                else
                {
                    result = this_base == null && other_base == null;
                }
            }
        }
        else
        {
            // When comparing C1.<T1> to C2.<T2>,
            // first compare C1 to C2; if they're equal, compare T1 to T2.

            result = this.typeNameBase.equals(other.typeNameBase);

            if (result && this.typeNameParameter != null)
            {
                if (other.typeNameParameter != null)
                    result = this.typeNameParameter.equals(other.typeNameParameter);
                else
                    result = false;
            }
        }

        return result;
    }

    /**
     * @return this Name's one and only Namespace qualifier.
     * @pre caller needs to know if this Name does, in fact, only have one
     * qualifier.
     */
    public Namespace getSingleQualifier()
    {
        return this.qualifiers != null ? this.qualifiers.getSingleQualifier() : null;
    }

    /**
     * @return the Name's kind byte.
     * @see "values in ABCConstants"
     */
    public int getKind()
    {
        return kind;
    }

    /**
     * @return the baseName
     */
    public String getBaseName()
    {
        return baseName;
    }

    /**
     * @return the qualifiers
     */
    public Nsset getQualifiers()
    {
        return qualifiers;
    }

    /**
     * @return true if this name is a parameterized type name.
     */
    public boolean isTypeName()
    {
        return kind == CONSTANT_TypeName;
    }

    /**
     * @return true if this name could be the Any type *
     */
    public boolean couldBeAnyType()
    {
        return (kind == CONSTANT_Qname || kind == CONSTANT_Multiname)
               &&
               (baseName == null || baseName.equals("*"));
    }

    /**
     * If this Name is for a parameterized type C.<T>, then this method returns
     * the Name for type C.
     * 
     * @return the Name for the base type of a parameterized type
     */
    public Name getTypeNameBase()
    {
        return typeNameBase;
    }

    /**
     * If this Name is for a parameterized type C.<T>, then this method returns
     * the Name for type T.
     * 
     * @return the Name for the type parameter of a parameterized type
     */
    public Name getTypeNameParameter()
    {
        return typeNameParameter;
    }

    /**
     * Cache the hash code since it's fairly expensive to compute.
     */
    private Integer cachedHashCode = null;

    /**
     * Generate a composite hash code using the Name's fields' hashes.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if (cachedHashCode == null)
        {
            int result = kind;

            if (kind != CONSTANT_TypeName)
            {
                result = (int)(PRIME_MULTIPLIER * result + (baseName != null ? baseName.hashCode() : 0));
                result = (int)(PRIME_MULTIPLIER * result + (qualifiers != null ? qualifiers.hashCode() : 0));
            }
            else
            {
                result = (int)(PRIME_MULTIPLIER * result) + typeNameBase.hashCode();
                if (typeNameParameter != null)
                    result = (int)(PRIME_MULTIPLIER * result) + typeNameParameter.hashCode();
            }

            cachedHashCode = result;
        }

        return cachedHashCode;
    }

    /**
     * Determine equality by checking Name objects' corresponding fields.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof Name) || this.hashCode() != o.hashCode())
            return false;
        else
            return this.isEqualTo((Name)o);
    }

    /**
     * Method to init a type name with a base name and parameter name. This
     * method is to facilitate reading in a Name table from an ABC - the type
     * name may refer to names which have not been read yet, so the values
     * cannot be filled in until later. This will assert if this name is not a
     * type name, or if it has already been initialized with values that are
     * different than those passed in.
     * 
     * @param base the base name this type name should use
     * @param parameter the parameter name this type name should use
     */
    public void initTypeName(Name base, Name parameter)
    {
        assert this.isTypeName() : "cannot init a Name that is not a type name";

        if (typeNameBase == null)
            typeNameBase = base;
        
        if (typeNameParameter == null)
            typeNameParameter = parameter;

        assert base == typeNameBase && parameter == typeNameParameter : "cannot init a typename that has already been initialized";
    }

    /**
     * Is this some type of runtime name?
     * 
     * @return true if this is a runtime name.
     */
    public boolean isRuntimeName()
    {
        switch (this.kind)
        {
            case CONSTANT_RTQname:
            case CONSTANT_RTQnameA:
            case CONSTANT_RTQnameL:
            case CONSTANT_RTQnameLA:
            {
                return true;
            }
            default:
            {
                return false;
            }
        }
    }

    /**
     * Compute the number of value stack elements this Name will need at
     * evaluation.
     * 
     * @return the number of value stack elements evaluating this Name requires.
     */
    public int runtimeNameAllowance()
    {
        switch (this.kind)
        {
            case CONSTANT_MultinameL:
            case CONSTANT_RTQname:
            {
                return 1;
            }
            case CONSTANT_RTQnameL:
            {
                return 2;
            }
            default:
            {
                return 0;
            }
        }
    }
}
