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

import org.apache.royale.compiler.css.ICSSDocument;
import org.apache.royale.compiler.css.ICSSProperty;
import org.junit.Test;

/**
 * JUnit tests for {@link CSSProperty}.
 * 
 * @author Gordon Smith
 */
public class CSSPropertyTests extends CSSBaseTests {
	
	private static final String EOL = "\n\t\t";
	
	protected String getPrefix()
	{
		return "custom|Label {" + EOL;
	}
			
    protected String getPostfix()
    {
    	return EOL +
		       "}";
    }

	@Override
	public ICSSDocument getCSSNodeBase(String code) {
		return super.getCSSNodeBase(getPrefix()  + code + getPostfix());
	}
	
	
	@Test
	public void CSSPropertyTests_property()
	{
		String code = "	color: red; ";
		
		List<ICSSProperty> properties = getCSSProperties(code);
		assertThat("properties.size()" , properties.size(), is(1) );	
		
		CSSProperty property = (CSSProperty) properties.get(0);
		assertThat("property.getOperator()" , property.getOperator(), is( CSSModelTreeType.PROPERTY ) );
		assertThat("property.getValue()" , property.getName(), is( "color" ) );

	}
	
	@Test
	public void CSSPropertyTests_property2()
	{
		String code = "	font-family: red; ";
		
		List<ICSSProperty> properties = getCSSProperties(code);
		assertThat("properties.size()" , properties.size(), is(1) );	
		
		CSSProperty property = (CSSProperty) properties.get(0);
		assertThat("property.getOperator()" , property.getOperator(), is( CSSModelTreeType.PROPERTY ) );
		assertThat("property.getValue()" , property.getName(), is( "fontFamily" ) );

	}
	
	@Test
	public void CSSPropertyTests_static_normalize()
	{
		assertThat("normalize()" , CSSProperty.normalize("font-family") , is( "fontFamily" ) );
		assertThat("normalize()" , CSSProperty.normalize("fontfamily") , is( "fontfamily" ) );
		assertThat("normalize()" , CSSProperty.normalize("-font-family") , is( "FontFamily" ) );
		assertThat("normalize()" , CSSProperty.normalize("-fontfamily") , is( "Fontfamily" ) );
		assertThat("normalize()" , CSSProperty.normalize("fontfamily-") , is( "fontfamily" ) );
	}

}
