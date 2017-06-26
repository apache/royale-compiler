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

package flex2.compiler;

import java.io.File;

import flex2.compiler.io.VirtualFile;

/**
 * This class represents the information associated with a single file
 * while it's being compiled.  This information includes the
 * <code>pathRoot</code>, the <code>relativePath</code>, the
 * <code>shortName</code>, which is often the class name, the owner,
 * which specifies where the Source came from, and whether the
 * <code>Source</code> is internal, root, and debuggable.
 *
 * @author Clement Wong
 */
public final class Source implements Comparable<Source>
{
	// used by flex2.compiler.i18n.I18nCompiler, InterfaceCompiler and ImplementationCompiler
	public Source(VirtualFile file, Source original)
	{
		this(file, original.pathRoot, original.relativePath, original.shortName, original.owner, original.isInternal, original.isRoot, original.isDebuggable);
		//this.delegate = original;
	}

	// used by InterfaceCompiler.createInlineComponentUnit().  Note the owner will be set
	// later by the ResourceContainer when this is passed into addResource() by
	// CompilerAPI.addGeneratedSources().
	public Source(VirtualFile file, Source original, String shortName, boolean isInternal, boolean isRoot)
	{
		this(file, original.pathRoot, original.relativePath, shortName, null, isInternal, isRoot, true);
		//this.delegate = original;
	}

	// used by FileSpec
	public Source(VirtualFile file, String relativePath, String shortName, Object owner, boolean isInternal, boolean isRoot)
	{
		this(file, null, relativePath, shortName, owner, isInternal, isRoot, true);
	}

	// used by SourceList and SourcePath
	public Source(VirtualFile file, VirtualFile pathRoot, String relativePath, String shortName, Object owner, boolean isInternal, boolean isRoot)
	{
		this(file, pathRoot, relativePath, shortName, owner, isInternal, isRoot, true);
	}

	// used by StylesContainer, CompilerSwcContext, EmbedEvaluator, DataBindingExtension and PreLink
	public Source(VirtualFile file, String relativePath, String shortName, Object owner, boolean isInternal, boolean isRoot, boolean isDebuggable)
	{
		this(file, null, relativePath, shortName, owner, isInternal, isRoot, isDebuggable);
	}

	Source(VirtualFile file, VirtualFile pathRoot, String relativePath, String shortName, Object owner, boolean isInternal, boolean isRoot, boolean isDebuggable)
	{
		this.file = file;
		this.pathRoot = pathRoot;
		this.relativePath = relativePath;
		this.shortName = shortName;
		this.owner = owner;
		this.isInternal = isInternal;
		this.isRoot = isRoot;
		this.isDebuggable = isDebuggable;

		if (file != null)
		{
			//fileTime = file.getLastModified();
		}

		//fileIncludeTimes = new HashMap<VirtualFile, Long>(4);
	}

	private VirtualFile file;
	private VirtualFile pathRoot;
	private String relativePath, shortName;
	private Object owner;
	private boolean isInternal;
	private boolean isRoot;
	private boolean isDebuggable;
	//private boolean isPreprocessed;

	//private long fileTime;
	//private Map<VirtualFile, Long> fileIncludeTimes;

	// 1. path resolution
	// 2. backing file
	// 3. source fragments
	//private Source delegate;

    public int compareTo(Source source)
    {
        return getName().compareTo(source.getName());
    }

    public String getName()
    {
        return file.getName();
    }

    public String getNameForReporting()
    {
        return file.getNameForReporting();
    }
    
    public String getSourceFileName()
    {
    	String s = getName();
    	if (relativePath.length() > 0)
    		s += relativePath;
    	s += File.separator;
    	s += shortName;
        return s;
    }
    
    public boolean exists()
    {
        return file.getLastModified() > 0;
    }

}
