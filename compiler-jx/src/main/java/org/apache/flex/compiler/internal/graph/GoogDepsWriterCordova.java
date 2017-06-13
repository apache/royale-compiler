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
package org.apache.flex.compiler.internal.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.internal.codegen.js.flexjs.JSFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.problems.FileNotFoundProblem;
import org.apache.flex.swc.ISWC;
import org.apache.flex.swc.ISWCFileEntry;

import com.google.common.io.Files;

public class GoogDepsWriterCordova extends GoogDepsWriter {

    public GoogDepsWriterCordova(File outputFolder, String mainClassName, JSGoogConfiguration config, 
    							List<ISWC> swcs, List<String> cordovaPlugins)
	{
		super(outputFolder, mainClassName, config, swcs);
		this.cordovaPlugins = cordovaPlugins;
	}
	
    private List<String> cordovaPlugins;

    @Override
	protected void otherScanning(String s)
	{	
    	int c = s.indexOf(JSFlexJSEmitterTokens.CORDOVA_PLUGIN.getToken());
    	if (c > -1)
    	{
    		cordovaPlugins.add(s.substring(c + JSFlexJSEmitterTokens.CORDOVA_PLUGIN.getToken().length()).trim());
    	}
	}
}
