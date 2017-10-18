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

package org.apache.royale.compiler.internal.fxg.dom.types;

/**
 * The BlendMode enumeration determines which blend mode to use when
 * compositing with the background. 
 * 
 * The ordinal for the enumeration is significant for the following BlendModes supported in FXGVersion.v1_0 
 * as it matches the SWF specification for the BlendMode property of the PlaceObject3 tag.
 * 
 * <pre>
 *   0 = normal
 *   1 = normal
 *   2 = layer
 *   3 = multiply
 *   4 = screen
 *   5 = lighten
 *   6 = darken
 *   7 = difference
 *   8 = add
 *   9 = subtract
 *  10 = invert
 *  11 = alpha
 *  12 = erase
 *  13 = overlay
 *  14 = hardlight
 * </pre>
 * 
 * The following Blendmodes were introduced in FXGVersion 2.0. Their implementation needs PixelBurst support.
 * 
 * <pre>
 *    colordodge
 *    colorburn
 *    exclusion
 *    softlight
 *    hue
 *    saturation
 *    color
 *    luminosity
 * </pre>
 * 
 * The following was introduced in FXG 2.0 which acts like blendMode="layer" except when alpha is 0 or 1, in which case it acts like blendMode="normal"
 * <pre>
 * 		auto
 * </pre>
 */
public enum BlendMode
{
    /**
     * The enum representing the default (unspecified) blend mode.
     */
    NORMAL0(),

    /**
     * The enum representing a 'normal' blend mode.
     */
    NORMAL(),

    /**
     * The enum representing a 'layer' blend mode.
     */
    LAYER(),

    /**
     * The enum representing a 'multiply' blend mode.
     */
    MULTIPLY(),

    /**
     * The enum representing a 'screen' blend mode.
     */
    SCREEN(),

    /**
     * The enum representing a 'lighten' blend mode.
     */
    LIGHTEN(),

    /**
     * The enum representing a 'darken' blend mode.
     */
    DARKEN(),

    /**
     * The enum representing an 'difference' blend mode.
     */
    DIFFERENCE(),

    /**
     * The enum representing a 'add' blend mode.
     */
    ADD(),

    /**
     * The enum representing a 'subtract' blend mode.
     */
    SUBTRACT(),

    /**
     * The enum representing an 'invert' blend mode.
     */
    INVERT(),

    /**
     * The enum representing an 'alpha' blend mode.
     */
    ALPHA(),

    /**
     * The enum representing an 'erase' blend mode.
     */
    ERASE(),

    /**
     * The enum representing a 'overlay' blend mode.
     */
    OVERLAY(),

    /**
     * The enum representing a 'hardlight' blend mode.
     */
    HARDLIGHT(),
    
    /**
     * The enum representing an 'auto' blend mode.
     */
    AUTO(),
    
    /**
     * The enum representing a 'colordodge' blend mode.
     */
    COLORDODGE("mx.graphics.shaderClasses.ColorDodgeShader"),
    
    /**
     * The enum representing a 'colorburn' blend mode.
     */
    COLORBURN("mx.graphics.shaderClasses.ColorBurnShader"),
    
    /**
     * The enum representing a 'exclusion' blend mode.
     */
    EXCLUSION("mx.graphics.shaderClasses.ExclusionShader"), 
    
    /**
     * The enum representing a 'softlight' blend mode.
     */
    SOFTLIGHT("mx.graphics.shaderClasses.SoftLightShader"), 
    
    /**
     * The enum representing a 'hue' blend mode.
     */
    HUE("mx.graphics.shaderClasses.HueShader"),
    
    /**
     * The enum representing a 'saturation' blend mode.
     */
    SATURATION("mx.graphics.shaderClasses.SaturationShader"),
    
    /**
     * The enum representing a 'color' blend mode.
     */
    COLOR("mx.graphics.shaderClasses.ColorShader"),
    
    /**
     * The enum representing a 'luminosity' blend mode.
     */
    LUMINOSITY("mx.graphics.shaderClasses.LuminosityShader");
    
    /**
     * Qualified name of the class required for this blend mode. Only the blend 
     * modes that need pixel bender support relies on a class. <code>null</code> 
     * if this blend mode doesn't require pixel bender support.
     */
    private String className;
    
    
    BlendMode()
    {
        this.className = null;
    }
    
    BlendMode(String className)
    {
        this.className =  className;
    }
    
    public boolean needsPixelBenderSupport()
    {
        return className != null;
    }
    
    /**
     * Returns the qualified name of the class that this blend mode
     * requires. <code>null</code> if this blend mode doesn't require pixel
     * bender support.
     */
    public String getClassName() {
        return className;
    }

}
