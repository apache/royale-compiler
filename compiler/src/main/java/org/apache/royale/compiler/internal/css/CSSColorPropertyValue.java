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

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import com.google.common.collect.ImmutableMap;

/**
 * Implementation for CSS color property values.
 */
public class CSSColorPropertyValue extends CSSPropertyValue
{
    /**
     * Map color constant names to 24-bit RGB integer values.
     * 
     * @see <a href="http://www.w3schools.com/css/css_colorsfull.asp">SCC colors</a>
     */
    protected static final ImmutableMap<String, Integer> COLOR_MAP =
            new ImmutableMap.Builder<String, Integer>()
                    .put("black", 0x000000)
                    .put("navy", 0x000080)
                    .put("darkblue", 0x00008b)
                    .put("mediumblue", 0x0000cd)
                    .put("blue", 0x0000ff)
                    .put("darkgreen", 0x006400)
                    .put("green", 0x008000)
                    .put("teal", 0x008080)
                    .put("darkcyan", 0x008b8b)
                    .put("deepskyblue", 0x00bfff)
                    .put("darkturquoise", 0x00ced1)
                    .put("mediumspringgreen", 0x00fa9a)
                    .put("lime", 0x00ff00)
                    .put("springgreen", 0x00ff7f)
                    .put("aqua", 0x00ffff)
                    .put("cyan", 0x00ffff)
                    .put("midnightblue", 0x191970)
                    .put("dodgerblue", 0x1e90ff)
                    .put("lightseagreen", 0x20b2aa)
                    .put("forestgreen", 0x228b22)
                    .put("seagreen", 0x2e8b57)
                    .put("darkslategray", 0x2f4f4f)
                    .put("darkslategrey", 0x2f4f4f)
                    .put("limegreen", 0x32cd32)
                    .put("mediumseagreen", 0x3cb371)
                    .put("turquoise", 0x40e0d0)
                    .put("royalblue", 0x4169e1)
                    .put("steelblue", 0x4682b4)
                    .put("darkslateblue", 0x483d8b)
                    .put("mediumturquoise", 0x48d1cc)
                    .put("indigo ", 0x4b0082)
                    .put("darkolivegreen", 0x556b2f)
                    .put("cadetblue", 0x5f9ea0)
                    .put("cornflowerblue", 0x6495ed)
                    .put("mediumaquamarine", 0x66cdaa)
                    .put("dimgray", 0x696969)
                    .put("dimgrey", 0x696969)
                    .put("slateblue", 0x6a5acd)
                    .put("olivedrab", 0x6b8e23)
                    .put("slategray", 0x708090)
                    .put("slategrey", 0x708090)
                    .put("lightslategray", 0x778899)
                    .put("lightslategrey", 0x778899)
                    .put("mediumslateblue", 0x7b68ee)
                    .put("lawngreen", 0x7cfc00)
                    .put("chartreuse", 0x7fff00)
                    .put("aquamarine", 0x7fffd4)
                    .put("maroon", 0x800000)
                    .put("purple", 0x800080)
                    .put("olive", 0x808000)
                    .put("gray", 0x808080)
                    .put("grey", 0x808080)
                    .put("skyblue", 0x87ceeb)
                    .put("lightskyblue", 0x87cefa)
                    .put("blueviolet", 0x8a2be2)
                    .put("darkred", 0x8b0000)
                    .put("darkmagenta", 0x8b008b)
                    .put("saddlebrown", 0x8b4513)
                    .put("darkseagreen", 0x8fbc8f)
                    .put("lightgreen", 0x90ee90)
                    .put("mediumpurple", 0x9370d8)
                    .put("darkviolet", 0x9400d3)
                    .put("palegreen", 0x98fb98)
                    .put("darkorchid", 0x9932cc)
                    .put("yellowgreen", 0x9acd32)
                    .put("sienna", 0xa0522d)
                    .put("brown", 0xa52a2a)
                    .put("darkgray", 0xa9a9a9)
                    .put("darkgrey", 0xa9a9a9)
                    .put("lightblue", 0xadd8e6)
                    .put("greenyellow", 0xadff2f)
                    .put("paleturquoise", 0xafeeee)
                    .put("lightsteelblue", 0xb0c4de)
                    .put("powderblue", 0xb0e0e6)
                    .put("firebrick", 0xb22222)
                    .put("darkgoldenrod", 0xb8860b)
                    .put("mediumorchid", 0xba55d3)
                    .put("rosybrown", 0xbc8f8f)
                    .put("darkkhaki", 0xbdb76b)
                    .put("silver", 0xc0c0c0)
                    .put("mediumvioletred", 0xc71585)
                    .put("indianred ", 0xcd5c5c)
                    .put("peru", 0xcd853f)
                    .put("chocolate", 0xd2691e)
                    .put("tan", 0xd2b48c)
                    .put("lightgray", 0xd3d3d3)
                    .put("lightgrey", 0xd3d3d3)
                    .put("palevioletred", 0xd87093)
                    .put("thistle", 0xd8bfd8)
                    .put("orchid", 0xda70d6)
                    .put("goldenrod", 0xdaa520)
                    .put("crimson", 0xdc143c)
                    .put("gainsboro", 0xdcdcdc)
                    .put("plum", 0xdda0dd)
                    .put("burlywood", 0xdeb887)
                    .put("lightcyan", 0xe0ffff)
                    .put("lavender", 0xe6e6fa)
                    .put("darksalmon", 0xe9967a)
                    .put("violet", 0xee82ee)
                    .put("palegoldenrod", 0xeee8aa)
                    .put("lightcoral", 0xf08080)
                    .put("khaki", 0xf0e68c)
                    .put("aliceblue", 0xf0f8ff)
                    .put("honeydew", 0xf0fff0)
                    .put("azure", 0xf0ffff)
                    .put("sandybrown", 0xf4a460)
                    .put("wheat", 0xf5deb3)
                    .put("beige", 0xf5f5dc)
                    .put("whitesmoke", 0xf5f5f5)
                    .put("mintcream", 0xf5fffa)
                    .put("ghostwhite", 0xf8f8ff)
                    .put("salmon", 0xfa8072)
                    .put("antiquewhite", 0xfaebd7)
                    .put("linen", 0xfaf0e6)
                    .put("lightgoldenrodyellow", 0xfafad2)
                    .put("oldlace", 0xfdf5e6)
                    .put("red", 0xff0000)
                    .put("fuchsia", 0xff00ff)
                    .put("magenta", 0xff00ff)
                    .put("deeppink", 0xff1493)
                    .put("orangered", 0xff4500)
                    .put("tomato", 0xff6347)
                    .put("hotpink", 0xff69b4)
                    .put("coral", 0xff7f50)
                    .put("darkorange", 0xff8c00)
                    .put("lightsalmon", 0xffa07a)
                    .put("orange", 0xffa500)
                    .put("lightpink", 0xffb6c1)
                    .put("pink", 0xffc0cb)
                    .put("gold", 0xffd700)
                    .put("peachpuff", 0xffdab9)
                    .put("navajowhite", 0xffdead)
                    .put("moccasin", 0xffe4b5)
                    .put("bisque", 0xffe4c4)
                    .put("mistyrose", 0xffe4e1)
                    .put("blanchedalmond", 0xffebcd)
                    .put("papayawhip", 0xffefd5)
                    .put("lavenderblush", 0xfff0f5)
                    .put("seashell", 0xfff5ee)
                    .put("cornsilk", 0xfff8dc)
                    .put("lemonchiffon", 0xfffacd)
                    .put("floralwhite", 0xfffaf0)
                    .put("snow", 0xfffafa)
                    .put("yellow", 0xffff00)
                    .put("lightyellow", 0xffffe0)
                    .put("ivory", 0xfffff0)
                    .put("white", 0xffffff)
                    .build();

    /**
     * Create a color property value from AST with hex color value such as
     * {@code #FFEEDD}.
     * 
     * @param tree AST
     * @param tokenStream token stream
     */
    protected CSSColorPropertyValue(final CommonTree tree,
                                    final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.PROPERTY_VALUE);
        String tokenText = tree.token.getText();
        assert tokenText.startsWith("#") : "Invalid color:" + tokenText;

        this.token = tree.token;
        if (tokenText.length() == 4)
        {
            StringBuilder six = new StringBuilder();
            six.append("#");
            six.append(tokenText.charAt(1));
            six.append(tokenText.charAt(1));
            six.append(tokenText.charAt(2));
            six.append(tokenText.charAt(2));
            six.append(tokenText.charAt(3));
            six.append(tokenText.charAt(3));
            tokenText = six.toString();
        }
        this.colorInt = Integer.parseInt(tokenText.substring(1), 16);
    }

    /**
     * Create a color property value from color name keyword such as "red" ,
     * "blue".
     * 
     * @param colorInt 24-bit RGB integer value
     * @param tree AST
     * @param tokenStream token stream
     */
    protected CSSColorPropertyValue(final int colorInt,
                                    final CommonTree tree,
                                    final TokenStream tokenStream)
    {
        super(tree, tokenStream, CSSModelTreeType.PROPERTY_VALUE);
        this.token = tree.token;
        this.colorInt = colorInt;
    }

    private final Token token;
    private final int colorInt;

    /**
     * @return Integer value for the 24-bit color.
     */
    public int getColorAsInt()
    {
        return colorInt;
    }

    /**
     * Get the original color property value text in the CSS document.
     * <p>
     * For example: {@code #FF0022}, {@code red}, {@code blue}.
     * 
     * @return Original color property value text in the CSS document.
     */
    public String getText()
    {
        return token.getText();
    }

    @Override
    public String toString()
    {
        return token.getText();
    }
}
