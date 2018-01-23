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

package org.apache.royale.compiler.constants;

/**
 * Collection of core constants for CSS handling within FLex
 */
public interface ICSSCoreConstants
{
	// CSS strings
	static final String ClassReference = "ClassReference";
	static final String Embed = "Embed";
	static final String fontFace = "@font-face";
	static final String namespace = "@namespace";
	static final String at_import = "@import";
	static final String at_media = "@media";
	static final String source = "source";
	static final String symbol = "symbol";
	static final String skinClass = "skinClass";
	static final String mediaType_all = "all";
	static final String mediaType_screen = "screen";
	static final String mediaFeature_application_dpi = "application-dpi";
	static final String mediaFeature_os_platform = "os-platform";
	static final String platform_Android = "\"Android\"";
	static final String platform_IOS = "\"IOS\"";
	static final String platform_Macintosh = "\"Macintosh\"";
	static final String platform_Windows = "\"Windows\"";
	static final String platform_Linux = "\"Linux\"";
	static final String platform_QNX = "\"QNX\"";
	static final String dpi_160 = "160";
	static final String dpi_240 = "240";
	static final String dpi_320 = "320";
	
	/**
	 * This is needed because hypenated-to-camel converters cannot predict
	 * that a property will have an acronym in it -- the compiler requires the
	 * acronym to be cased correctly.
	 */
	static final String embedAsCFF_TRUE_CAMEL = "embedAsCff";
	static final String embedAsCFF = "embedAsCFF";
	
	/**
	 * All the available media types
	 */
	static String[] mediaTypes =
	{
		mediaType_all,
		mediaType_screen
	};
	
	/**
	 * All the available media feature names
	 */
	static String[] mediaFeatureNames =
	{
		mediaFeature_application_dpi,
		mediaFeature_os_platform
	};
	
	/**
	 * All the available media feature platforms
	 */
	static String[] featurePlatforms =
	{
		platform_Android,
		platform_IOS,
		platform_Linux,
		platform_Macintosh,
		platform_QNX,
		platform_Windows
	};
	
	/**
	 * All the available media feature dpis
	 */
	static String[] featuresDpis =
	{
		dpi_160,
		dpi_240,
		dpi_320
	};
}
