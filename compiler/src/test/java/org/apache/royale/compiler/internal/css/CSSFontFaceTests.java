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

package org.apache.royale.compiler.internal.css;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.royale.compiler.css.FontFaceSourceType;
import org.apache.royale.compiler.css.ICSSFontFace;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSFontFace}.
 * 
 * @author Gordon Smith
 */
public class CSSFontFaceTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";
	
	protected String getPrefix()
	{
		return "@font-face {" + EOL;
	}
			
    protected String getPostfix()
    {
    	return EOL + "}";
    }
	
	protected List<ICSSFontFace> getCSSFontFace(String code) {
		return getCSSNodeBase(getPrefix() + code + getPostfix()).getFontFaces();
	}
	
	@Test
	public void CSSFontFaceTests_default_properties()
	{
		String code = 
				" src: url(\"font.ttf\");" + EOL +
				" fontFamily: font";
		
		List<ICSSFontFace> fontfaces = getCSSFontFace(code);
		assertThat("fontfaces.size()" , fontfaces.size(), is(1) );	
		
		CSSFontFace fontface = (CSSFontFace) fontfaces.get(0);
		assertThat("fontface.getOperator()" , fontface.getOperator(), is( CSSModelTreeType.FONT_FACE ) );
		assertThat("fontface.getFontFamily()" , fontface.getFontFamily(), is( "font" ) );
		assertThat("fontface.getFontStyle()" , fontface.getFontStyle(), is( "normal" ) );
		assertThat("fontface.getFontWeight()" , fontface.getFontWeight(), is( "normal" ) );
		assertThat("fontface.getAdvancedAntiAliasing()" , fontface.getAdvancedAntiAliasing(), is( true ) );
		assertThat("fontface.getSourceType()" , fontface.getSourceType(), is( FontFaceSourceType.URL ) );
		assertThat("fontface.getSourceValue()" , fontface.getSourceValue(), is( "font.ttf" ) );
		assertThat("fontface.getEmbedAsCFF()" , fontface.getEmbedAsCFF(), is( true ) );
	}
	
	
	@Test
	public void CSSFontFaceTests_properties()
	{
		String code = 
				" src: url(\"font.ttf\");" + EOL +
				" fontFamily: font;" + EOL +
				" fontStyle: italic;" + EOL +
				" fontWeight: bold;" + EOL +
				" embedAsCFF: true;" + EOL +
				" advancedAntiAliasing: true;";			
		
		List<ICSSFontFace> fontfaces = getCSSFontFace(code);
		assertThat("fontfaces.size()" , fontfaces.size(), is(1) );	
		
		CSSFontFace fontface = (CSSFontFace) fontfaces.get(0);
		assertThat("fontface.getOperator()" , fontface.getOperator(), is( CSSModelTreeType.FONT_FACE ) );
		assertThat("fontface.getFontFamily()" , fontface.getFontFamily(), is( "font" ) );
		assertThat("fontface.getFontStyle()" , fontface.getFontStyle(), is( "italic" ) );
		assertThat("fontface.getFontWeight()" , fontface.getFontWeight(), is( "bold" ) );
		assertThat("fontface.getAdvancedAntiAliasing()" , fontface.getAdvancedAntiAliasing(), is( true ) );
		assertThat("fontface.getSourceType()" , fontface.getSourceType(), is( FontFaceSourceType.URL ) );
		assertThat("fontface.getSourceValue()" , fontface.getSourceValue(), is( "font.ttf" ) );
		assertThat("fontface.getEmbedAsCFF()" , fontface.getEmbedAsCFF(), is( true ) );
	}
	
	@Test
	public void CSSFontFaceTests_properties2()
	{
		String code = 
				" src: url(\"font.ttf\");" + EOL +
				" fontFamily: font;" + EOL +
				" fontStyle: oblique;" + EOL +
				" fontWeight: heavy;" + EOL +
				" embedAsCFF: true;" + EOL +
				" advancedAntiAliasing: true;";			
		
		List<ICSSFontFace> fontfaces = getCSSFontFace(code);
		assertThat("fontfaces.size()" , fontfaces.size(), is(1) );	
		
		CSSFontFace fontface = (CSSFontFace) fontfaces.get(0);
		assertThat("fontface.getOperator()" , fontface.getOperator(), is( CSSModelTreeType.FONT_FACE ) );
		assertThat("fontface.getFontFamily()" , fontface.getFontFamily(), is( "font" ) );
		assertThat("fontface.getFontStyle()" , fontface.getFontStyle(), is( "oblique" ) );
		assertThat("fontface.getFontWeight()" , fontface.getFontWeight(), is( "heavy" ) );
		assertThat("fontface.getAdvancedAntiAliasing()" , fontface.getAdvancedAntiAliasing(), is( true ) );
		assertThat("fontface.getSourceType()" , fontface.getSourceType(), is( FontFaceSourceType.URL ) );
		assertThat("fontface.getSourceValue()" , fontface.getSourceValue(), is( "font.ttf" ) );
		assertThat("fontface.getEmbedAsCFF()" , fontface.getEmbedAsCFF(), is( true ) );
	}
	
	
	@Test
	public void CSSFontFaceTests_src_local()
	{
		String code = 
				" src: local(\"Myriad Web Pro\");" + EOL +
				" fontFamily: font";
		
		List<ICSSFontFace> fontfaces = getCSSFontFace(code);
		assertThat("fontfaces.size()" , fontfaces.size(), is(1) );	
		
		CSSFontFace fontface = (CSSFontFace) fontfaces.get(0);
		assertThat("fontface.getOperator()" , fontface.getOperator(), is( CSSModelTreeType.FONT_FACE ) );
		assertThat("fontface.getFontFamily()" , fontface.getFontFamily(), is( "font" ) );
		assertThat("fontface.getFontStyle()" , fontface.getFontStyle(), is( "normal" ) );
		assertThat("fontface.getFontWeight()" , fontface.getFontWeight(), is( "normal" ) );
		assertThat("fontface.getAdvancedAntiAliasing()" , fontface.getAdvancedAntiAliasing(), is( true ) );
		assertThat("fontface.getSourceType()" , fontface.getSourceType(), is( FontFaceSourceType.LOCAL ) );
		assertThat("fontface.getSourceValue()" , fontface.getSourceValue(), is( "Myriad Web Pro" ) );
		assertThat("fontface.getEmbedAsCFF()" , fontface.getEmbedAsCFF(), is( true ) );
	}
	
	@Test
	public void CSSFontFaceTests_src_url()
	{
		String code = 
				" src: url(\"assets/font.ttf\");" + EOL +
				" fontFamily: font";
		
		List<ICSSFontFace> fontfaces = getCSSFontFace(code);
		assertThat("fontfaces.size()" , fontfaces.size(), is(1) );	
		
		CSSFontFace fontface = (CSSFontFace) fontfaces.get(0);
		assertThat("fontface.getOperator()" , fontface.getOperator(), is( CSSModelTreeType.FONT_FACE ) );
		assertThat("fontface.getFontFamily()" , fontface.getFontFamily(), is( "font" ) );
		assertThat("fontface.getFontStyle()" , fontface.getFontStyle(), is( "normal" ) );
		assertThat("fontface.getFontWeight()" , fontface.getFontWeight(), is( "normal" ) );
		assertThat("fontface.getAdvancedAntiAliasing()" , fontface.getAdvancedAntiAliasing(), is( true ) );
		assertThat("fontface.getSourceType()" , fontface.getSourceType(), is( FontFaceSourceType.URL ) );
		assertThat("fontface.getSourceValue()" , fontface.getSourceValue(), is( "assets/font.ttf" ) );
		assertThat("fontface.getEmbedAsCFF()" , fontface.getEmbedAsCFF(), is( true ) );
	}
	
	@Test
	public void CSSFontFaceTests_embedAsCFF()
	{
		String code = 
				" src: url(\"assets/font.ttf\");" + EOL +
				" fontFamily: font;" + EOL +
				" embedAsCFF: false;";
		
		List<ICSSFontFace> fontfaces = getCSSFontFace(code);
		assertThat("fontfaces.size()" , fontfaces.size(), is(1) );	
		
		CSSFontFace fontface = (CSSFontFace) fontfaces.get(0);
		assertThat("fontface.getOperator()" , fontface.getOperator(), is( CSSModelTreeType.FONT_FACE ) );
		assertThat("fontface.getFontFamily()" , fontface.getFontFamily(), is( "font" ) );
		assertThat("fontface.getFontStyle()" , fontface.getFontStyle(), is( "normal" ) );
		assertThat("fontface.getFontWeight()" , fontface.getFontWeight(), is( "normal" ) );
		assertThat("fontface.getAdvancedAntiAliasing()" , fontface.getAdvancedAntiAliasing(), is( true ) );
		assertThat("fontface.getSourceType()" , fontface.getSourceType(), is( FontFaceSourceType.URL ) );
		assertThat("fontface.getSourceValue()" , fontface.getSourceValue(), is( "assets/font.ttf" ) );
		assertThat("fontface.getEmbedAsCFF()" , fontface.getEmbedAsCFF(), is( false ) );
	}
	
	@Test
	public void CSSFontFaceTests_advancedAntiAliasing()
	{
		String code = 
				" src: url(\"assets/font.ttf\");" + EOL +
				" fontFamily: font;" + EOL +
				" advancedAntiAliasing: false;";
		
		List<ICSSFontFace> fontfaces = getCSSFontFace(code);
		assertThat("fontfaces.size()" , fontfaces.size(), is(1) );	
		
		CSSFontFace fontface = (CSSFontFace) fontfaces.get(0);
		assertThat("fontface.getOperator()" , fontface.getOperator(), is( CSSModelTreeType.FONT_FACE ) );
		assertThat("fontface.getFontFamily()" , fontface.getFontFamily(), is( "font" ) );
		assertThat("fontface.getFontStyle()" , fontface.getFontStyle(), is( "normal" ) );
		assertThat("fontface.getFontWeight()" , fontface.getFontWeight(), is( "normal" ) );
		assertThat("fontface.getAdvancedAntiAliasing()" , fontface.getAdvancedAntiAliasing(), is( false ) );
		assertThat("fontface.getSourceType()" , fontface.getSourceType(), is( FontFaceSourceType.URL ) );
		assertThat("fontface.getSourceValue()" , fontface.getSourceValue(), is( "assets/font.ttf" ) );
		assertThat("fontface.getEmbedAsCFF()" , fontface.getEmbedAsCFF(), is( true ) );
	}

}
