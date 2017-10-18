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

package org.apache.royale.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** A set of functions for managing Arrays of Objects in a memory efficient way.
 * 
 * This class is a memory-efficient alternative to using ArrayList to manage an array of objects. 
 * It is applicable when you have a List that is initially populated with some items, 
 * and then lives for a long time without any further changes. 
 * 
 * Usage: Instead of this ArrayList-based code:
 * 
 * 		// Populate the array
 * 		List array = new ArrayList(n);
 * 		array.add(item1);
 * 		array.add(item2);
 * 
 * 		// Use the data
 * 		s = array.size();
 * 		item = array.get(i);
 * 
 * you can use this CheapArray-based code
 * 	
 * 		// Populate the array
 * 		Object array = CheapArray.create(n);
 * 		CheapArray.add(item1, array);
 * 		CheapArray.add(item2, array);
 * 		array = CheapArray.optimize(Object, new MyItemType[0]);
 * 
 * 		// Use the data
 * 		s = CheapArray.size(array);
 * 		item = CheapArray.get(i, array);
 * 
 * The code for List is cleaner, but there is a benefit to using CheapArray.
 * The difference between CheapArray and ArrayList is this: each time you use ArrayList, you get two 
 * java objects: the ArrayList, and the Object[] that it contains. Each ArrayList object occupies 
 * 24 bytes of memory. A CheapArray, on the other hand, is typed as Object, and is represented by
 * an ArrayList while the array is being populated, and as an Object[] thereafter. The 24 byte
 * overhead is only incurred transiently, while the array is being populated;
 * the long-term memory cost is for the Object[] only. 
 * 
 * For convenience, we will use the term "cheapArray" to refer to Objects that are returned 
 * by create() and optimize(), even though they are not technically instances of CheapArray.
 * 
 * Not keeping the ArrayList object around gives a significant memory usage improvement. 
 * As of May 2006, the codemodel tree for a medium size test application (doradoSmall), plus the 
 * codemodel tree for the Flex framework (frameworks.swc) had almost 8 megabytes of ArrayList 
 * objects. By converting a few key data structures (in NodeBase and its subclasses) to use
 * CheapArray instead of ArrayList, most of that 8 mb of usage has been eliminated.  
 */
@SuppressWarnings("rawtypes")
public class CheapArray
{
	/** You cannot create instances of CheapArray. Use create(), and the other static
	 * functions.
	 */
	private CheapArray() { /*do nothing*/}

	/** Create a new cheapArray. Use instead of "new ArrayList(initialCapacity)".
	 * @param initialCapacity the initial capacity of the cheapArray.
	 * @return a 'cheapArray' Object that you can use as the "array" parameter in
	 * calls to other CheapArray functions.  
	 */
	public static Object create(int initialCapacity) {
		return new ArrayList(initialCapacity);
	}

	/** Fetch the contents of a cheapArray. Use instead of List.toArray().
	 * @param array a cheapArray
	 * @param emptyArray a zero-length array. The array returned by this function
	 * will be the same runtime type as this parameter.  
	 * @return an array containing all the items of the cheapArray.
	 */
    @SuppressWarnings("unchecked")
    public static Object[] toArray(Object array, Object[] emptyArray)
	{
		assert(emptyArray.length == 0);
		if(array instanceof List)
			return ((List)array).toArray( emptyArray );
		
		return (Object[])array;
	}
	
	/** Get the number of items of a cheapArray. Use instead of List.size().
	 * @param array a cheapArray
	 * @return		the number of items
	 */
	public static int size(Object array)
	{
		if (array == null)
			return 0;
		
		if(array instanceof List) {
			return ((List)array).size();
		} 
		return ((Object[])array).length;
	}
	
	/**
	 * Get a particular item from a cheapArray. Use instead of List.get().
	 * @param i		the item's index
	 * @param array a cheapArray
	 * @return		the specified item
	 */
	public static Object get(int i, Object array)
	{
		if(array instanceof List)
			return ((List)array).get(i);
		
		return ((Object[])array)[i];
	}
	
	/** Adds an item to a cheapArray. This is only allowed before the array is optimized.
	 * Use instead of List.add().
	 * @param item the item to add
	 * @param array a cheapArray
	 */
	@SuppressWarnings("unchecked")
    public static void add(Object item, Object array) {
		assert(array instanceof List);
		((List)array).add(item);
	}
	
	/**
	 * Sorts the given array based on a comparator
	 * @param array the array to sort
	 * @param comparator a comparator
	 */
	@SuppressWarnings("unchecked")
	public static void sort(Object array, Comparator comparator) {
		if(array instanceof List) {
			Collections.sort((List)array, comparator);
		} else {
			Arrays.sort((Object[]) array, comparator);
		}
	}

	/** Adds an item to a cheapArray. This can be used either before or after the array is optimized.
	 * Use instead of List.add().
	 * @param item the item to add
	 * @param array a cheapArray
	 * @return a new 'cheapArray' Object that replaces the old instance
	 */
	@SuppressWarnings("unchecked")
	public static Object add(Object item, Object array, Object[] emptyArray) {
		if(array instanceof List) {
			((List)array).add(item);
			return array;
		}
		int oldSize = ((Object[])array).length;
		Object[] newArray = (Object[])java.lang.reflect.Array.newInstance(
				emptyArray.getClass().getComponentType(), oldSize + 1);
		System.arraycopy(array, 0, newArray, 0, oldSize);
		newArray[oldSize] = item;
		return newArray;
	}

	/** Adds an item to a cheapArray at a specific position. 
	 * This is only allowed before the array is optimized. Use instead of List.add().
	 * @param index the position at which to add the item
	 * @param item the item to add
	 * @param array a cheapArray
	 */
	@SuppressWarnings("unchecked")
    public static void add(int index, Object item, Object array) {
		assert(array instanceof List);
		
		((List)array).add(index, item);
	}
	
	/**
	 * Replaces the item at the given index in the passed in array
	 * @param index the index to replace at
	 * @param item the item to use as a replacement
	 * @param array the array to operate on
	 */
	@SuppressWarnings("unchecked")
    public static void replace(int index, Object item, Object array) {
		if(array instanceof List) {
			((List)array).remove(index);
			((List)array).add(index, item);
		} else {
			((Object[])array)[index] = item;
		}
	}
	
	/** Remove an item from a cheapArray.
	 * This is only allowed before the array is optimized. Use instead of List.remove().
	 * @param item the item to remove
	 * @param array a cheapArray
	 */
	public static void remove(Object item, Object array) {
		assert(array instanceof List);
			
		((List)array).remove(item);
	}
	
	/** Remove an item from a cheapArray.
	 * This can be used either before or after the array is optimized. Use instead of List.remove().
	 * @param item the item to remove
	 * @param array a cheapArray
	 */
	public static Object remove(Object item, Object array, Object[] emptyArray) {
		if(array instanceof List) {
			((List)array).remove(item);
			return array;
		} 
		int oldSize = ((Object[])array).length;
		for(int i = 0; i < oldSize; i++) {
			if(((Object[])array)[i].equals(item)) {
				Object[] newArray = (Object[])java.lang.reflect.Array.newInstance(
						emptyArray.getClass().getComponentType(), oldSize - 1);
				System.arraycopy(array, 0, newArray, 0, i);
				System.arraycopy(array, i + 1, newArray, i, oldSize - i - 1);
				return newArray;
			}
		}
		return array;
		
	}
	
	/** Optimize the cheapArray so it uses the minimum possible memory. Use this after
	 * the array has been populated with all of its data. After this call,
	 * you cannot use add() or remove() on the cheapArray. Use instead of 
	 * ArrayList.trimToSize().
	 * @param array a cheapArray
	 * @param emptyArray a zero-length array. The array returned by this function
	 * will be the same runtime type as this parameter.  
	 * @return a 'cheapArray' Object that you can use as the "array" parameter in
	 * calls to CheapArray functions.  
	 */
    @SuppressWarnings("unchecked")
    public static Object optimize(Object array, Object[] emptyArray) {
        assert (emptyArray.length == 0);
        if (array instanceof List)
        {
            if (((List)array).size() == 0)
                return emptyArray;

            return ((List)array).toArray(emptyArray);
        }
		return array;
		
	}
}
