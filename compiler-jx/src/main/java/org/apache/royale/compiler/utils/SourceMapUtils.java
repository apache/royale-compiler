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
package org.apache.royale.compiler.utils;

import java.io.IOException;

import com.google.debugging.sourcemap.FilePosition;
import com.google.debugging.sourcemap.SourceMapConsumerV3;
import com.google.debugging.sourcemap.SourceMapGeneratorV3;
import com.google.debugging.sourcemap.SourceMapParseException;

public class SourceMapUtils
{
	public static String sourceMapConsumerToString(SourceMapConsumerV3 consumer, String file)
	{
		SourceMapGeneratorV3 generator = sourceMapConsumerToGenerator(consumer);
		StringBuilder builder = new StringBuilder();
		try
		{
			generator.appendTo(builder, file);
		}
		catch(IOException e)
		{
			return "";
		}
		return builder.toString();
	}

	public static SourceMapGeneratorV3 sourceMapConsumerToGenerator(SourceMapConsumerV3 consumer)
	{
		final SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
		final SourceMapEntryCounter counter = new SourceMapEntryCounter();
		generator.setSourceRoot(consumer.getSourceRoot());
		consumer.visitMappings(counter);
		consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor()
		{
			private int index = 0;

			@Override
			public void visit(String sourceName,
				String symbolName,
				FilePosition sourceStartPosition,
				FilePosition startPosition,
				FilePosition endPosition) {
				generator.addMapping(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				index++;
				if(index == counter.count)
				{
					//add an extra mapping because there seems to be a bug in
					//SourceMapGeneratorV3's appendTo() that omits the last
					//entry, for some reason
					appendExtraMappingToGenerator(generator, sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				}
			}
		});
		return generator;
	}

	public static SourceMapGeneratorV3 sourceMapConsumerToGeneratorWithRemappedSourceRoot(SourceMapConsumerV3 consumer, String sourceRoot, String className)
	{
		final SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
		final SourceMapEntryCounter counter = new SourceMapEntryCounter();
		final String startPath = "/" + className.replace(".", "/") + ".";
		generator.setSourceRoot(sourceRoot);
		consumer.visitMappings(counter);
		consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor()
		{
			private int index = 0;

			@Override
			public void visit(String sourceName,
					String symbolName,
					FilePosition sourceStartPosition,
					FilePosition startPosition,
					FilePosition endPosition) {
				String newSourceName = sourceName;
				int startPathIndex = newSourceName.indexOf(startPath);
				if(startPathIndex != -1)
				{
					newSourceName = newSourceName.substring(startPathIndex + 1);
				}
				generator.addMapping(newSourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				index++;
				if(index == counter.count)
				{
					//add an extra mapping because there seems to be a bug in
					//SourceMapGeneratorV3's appendTo() that omits the last
					//entry, for some reason
					appendExtraMappingToGenerator(generator, newSourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				}
			}
		});
		return generator;
	}

	public static SourceMapConsumerV3 addLineToSourceMap(SourceMapConsumerV3 consumer, String sourceFileName, final int lineToAdd)
	{
		if (consumer == null)
		{
			return null;
		}
		final SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
		final SourceMapEntryCounter counter = new SourceMapEntryCounter();
		generator.setSourceRoot(consumer.getSourceRoot());
		consumer.visitMappings(counter);
		consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor()
		{
			private int index = 0;

			@Override
			public void visit(String sourceName,
				String symbolName,
				FilePosition sourceStartPosition,
				FilePosition startPosition,
				FilePosition endPosition) {
				if(startPosition.getLine() >= lineToAdd)
				{
					startPosition = new FilePosition(startPosition.getLine() + 1, startPosition.getColumn());
					endPosition = new FilePosition(endPosition.getLine() + 1, endPosition.getColumn());
				}
				generator.addMapping(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				index++;
				if(index == counter.count)
				{
					//add an extra mapping because there seems to be a bug in
					//SourceMapGeneratorV3's appendTo() that omits the last
					//entry, for some reason
					appendExtraMappingToGenerator(generator, sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				}
			}
		});
		return sourceMapGeneratorToConsumer(generator, sourceFileName);
	}

	public static SourceMapConsumerV3 removeLineFromSourceMap(SourceMapConsumerV3 consumer, String sourceFileName, final int lineToRemove)
	{
		if (consumer == null)
		{
			return null;
		}
		final SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
		final SourceMapEntryCounter counter = new SourceMapEntryCounter();
		generator.setSourceRoot(consumer.getSourceRoot());
		consumer.visitMappings(counter);
		consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor()
		{
			private int index = 0;

			@Override
			public void visit(String sourceName,
				String symbolName,
				FilePosition sourceStartPosition,
				FilePosition startPosition,
				FilePosition endPosition) {
				if(startPosition.getLine() == lineToRemove)
				{
					return;
				}
				if(startPosition.getLine() > lineToRemove)
				{
					startPosition = new FilePosition(startPosition.getLine() - 1, startPosition.getColumn());
				}
				if(endPosition.getLine() > lineToRemove)
				{
					endPosition = new FilePosition(endPosition.getLine() - 1, endPosition.getColumn());
				}
				generator.addMapping(sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				index++;
				if(index == counter.count)
				{
					//add an extra mapping because there seems to be a bug in
					//SourceMapGeneratorV3's appendTo() that omits the last
					//entry, for some reason
					appendExtraMappingToGenerator(generator, sourceName, symbolName, sourceStartPosition, startPosition, endPosition);
				}
			}
		});
		return sourceMapGeneratorToConsumer(generator, sourceFileName);
	}

	private static void appendExtraMappingToGenerator(SourceMapGeneratorV3 generator,
		String sourceName,
		String symbolName,
		FilePosition sourceStartPosition,
		FilePosition startPosition,
		FilePosition endPosition)
	{
		//add an extra mapping because there seems to be a bug in
		//SourceMapGeneratorV3's appendTo() that omits the last
		//entry, for some reason
		FilePosition newEndPosition = new FilePosition(endPosition.getLine(), endPosition.getColumn() + 1);
		generator.addMapping(sourceName, symbolName, sourceStartPosition, endPosition, newEndPosition);
	}

	private static class SourceMapEntryCounter implements SourceMapConsumerV3.EntryVisitor
	{
		private int count = 0;

		@Override
		public void visit(String sourceName,
			String symbolName,
			FilePosition sourceStartPosition,
			FilePosition startPosition,
			FilePosition endPosition) {
			count++;
		}
	}

	public static String sourceMapGeneratorToString(SourceMapGeneratorV3 generator, String fileName)
	{
		StringBuilder builder = new StringBuilder();
		try
		{
			generator.appendTo(builder, fileName);
		}
		catch(IOException e)
		{
			return null;
		}
		return builder.toString();
	}

	public static SourceMapConsumerV3 sourceMapGeneratorToConsumer(SourceMapGeneratorV3 generator, String fileName)
	{
		String generatorString = sourceMapGeneratorToString(generator, fileName);
		if(generatorString == null)
		{
			return null;
		}
		SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
		try
		{
			consumer.parse(generatorString);
		}
		catch(SourceMapParseException e)
		{
			return null;
		}
		return consumer;
	}
}
