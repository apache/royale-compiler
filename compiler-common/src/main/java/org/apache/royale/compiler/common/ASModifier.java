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

package org.apache.royale.compiler.common;

import java.util.HashMap;

import org.apache.royale.compiler.constants.IASKeywordConstants;

/**
 * Modifiers found within the AS3 language.
 * TODO: Make this class an enum.
 */
public class ASModifier
{
    private static HashMap<String, ASModifier> LOOKUP = new HashMap<String, ASModifier>(6);

	/**
	 * Represents the <code>dynamic</code> modifier.
	 */
	public static final ASModifier DYNAMIC = new ASModifier(IASKeywordConstants.DYNAMIC, 1 << 1);
	
    /**
     * Represents the <code>final</code> modifier.
     */
	public static final ASModifier FINAL = new ASModifier(IASKeywordConstants.FINAL, 1 << 2);
	
    /**
     * Represents the <code>native</code> modifier.
     */
	public static final ASModifier NATIVE = new ASModifier(IASKeywordConstants.NATIVE, 1 << 3);
	
    /**
     * Represents the <code>override</code> modifier.
     */
	public static final ASModifier OVERRIDE = new ASModifier(IASKeywordConstants.OVERRIDE, 1 << 4);

    /**
     * Represents the <code>static</code> modifier.
     */
	public static final ASModifier STATIC = new ASModifier(IASKeywordConstants.STATIC, 1 << 5);

	/**
     * Represents the <code>virtual</code> modifier.
	 */
	public static final ASModifier VIRTUAL = new ASModifier(IASKeywordConstants.VIRTUAL, 1 << 6);

	/**
     * Represents the <code>abstract</code> modifier.
	 */
	public static final ASModifier ABSTRACT = new ASModifier(IASKeywordConstants.ABSTRACT, 1 << 7);
	
	/**
	 * A list of all the modifiers that exist within AS3
	 */
	public static final ASModifier[] MODIFIERS = new ASModifier[]
	{
		DYNAMIC,
		FINAL,
		NATIVE,
		OVERRIDE,
		STATIC,
		VIRTUAL,
		ABSTRACT
	};
	
    /**
	 * Returns the {@link ASModifier} for the given string literal
	 * @param text a literal representing a modifier
	 * @return an {@link ASModifier} or null
	 */
	public static ASModifier getASModifier(String text)
	{
		return LOOKUP.get(text);
	}
	
	/**
	 * Private constructor.
	 * 
     * @param text The text of the modifier.
	 * @param type An integer identifying the modifier
	 * that can be used as a bitmask value.
	 */
	private ASModifier(String text, int maskValue)
	{
        this.text = text;
	    this.maskValue = maskValue;
	    LOOKUP.put(text, this);
	}
	    
    private String text;

    private int maskValue;
    
	@Override
	public String toString()
	{
	    return text;
	}
	    
	public int getMaskValue()
	{
	    return maskValue;
	}
}
