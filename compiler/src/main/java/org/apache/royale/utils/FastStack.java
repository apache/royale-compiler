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
import java.util.Collection;

/**
 * Stack implementation based on ArrayList instead of a Vector.  Improves performance when threading is not needed.
 * Also allows for decoration of values coming back from the stack
 *
 * @param <E> The type of the stack elements.
 */
public class FastStack<E> extends ArrayList<E> {
	
	/**
	 * Decorator used to change values as they come off this stack
	 *
	 * @param <E> The type of the stack elemetns.
	 */
	public interface IFastStackDecorator<E> {
		/**
		 * Decorates the value as it is popped or peeked from the stack
		 * @param e the value to decorate.  Value can be null
		 * @return the decorated value
		 */
		public E decorate(E e);
	}
	
	
	private static final long serialVersionUID = 7491447816791648090L;
	private IFastStackDecorator<E> decorator;

	public FastStack() {
		super(3);
	}
	
	public FastStack(int size) {
		super(size);
	}
	
	public FastStack(Collection<? extends E> c) {
		super(c);
	}
	
	public void setStackDecorator(IFastStackDecorator<E> decorator) {
		this.decorator = decorator;
	}
	
	/**
	 * Pushes the entry onto the end of the stack
	 * @param entry the entry to add
	 */
	public void push(E entry) {
		add(entry);
	}
	
	/**
	 * Pops the entry from the end of the stack
	 * @return the entry
	 */
	public E pop() {
		E retVal = null;
		int size = size();
		if(size > 0)
			retVal = remove(size - 1);
		if(decorator != null) 
			return decorator.decorate(retVal);
		return retVal;
	}
	
	/**
	 * Gets the entry at the head of the stack
	 * @return head of the stack
	 */
	public E peek() {
		E retVal = null;
		int size = size();
		if(size > 0)
			retVal = get(size - 1);
		if(decorator != null) 
			return decorator.decorate(retVal);
		return retVal;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		FastStack<E> st = new FastStack<E>((ArrayList<E>)super.clone());
		return st;
	}
}
