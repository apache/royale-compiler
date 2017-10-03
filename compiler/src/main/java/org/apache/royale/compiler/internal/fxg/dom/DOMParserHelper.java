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

package org.apache.royale.compiler.internal.fxg.dom;

import static org.apache.royale.compiler.fxg.FXGConstants.*;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.royale.compiler.fxg.FXGVersion;
import org.apache.royale.compiler.fxg.dom.IFXGNode;
import org.apache.royale.compiler.fxg.logging.FXGLog;
import org.apache.royale.compiler.fxg.logging.IFXGLogger;
import org.apache.royale.compiler.internal.fxg.dom.types.FillMode;
import org.apache.royale.compiler.internal.fxg.dom.types.InterpolationMethod;
import org.apache.royale.compiler.internal.fxg.dom.types.MaskType;
import org.apache.royale.compiler.internal.fxg.dom.types.SpreadMethod;
import org.apache.royale.compiler.problems.FXGInvalidBooleanValueProblem;
import org.apache.royale.compiler.problems.FXGInvalidColorValueProblem;
import org.apache.royale.compiler.problems.FXGInvalidDoubleValueProblem;
import org.apache.royale.compiler.problems.FXGInvalidFloatValueProblem;
import org.apache.royale.compiler.problems.FXGInvalidIntegerValueProblem;
import org.apache.royale.compiler.problems.FXGInvalidPercentValueProblem;
import org.apache.royale.compiler.problems.FXGOutOfRangeValueProblem;
import org.apache.royale.compiler.problems.FXGUnknownAttributeValueProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Utilities to help parsing FXG.
 */
public class DOMParserHelper
{
    public static Pattern idPattern = Pattern.compile ("[a-zA-Z_][a-zA-Z_0-9]*");
    public static Pattern rgbPattern = Pattern.compile ("#[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]");

    /**
     * Convert an FXG String value to a boolean.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return true for the String 'true' (case insensitive), otherwise false. 
     */
    public static boolean parseBoolean(IFXGNode node, String value, String name, boolean defaultValue, Collection<ICompilerProblem> problems)
    {
        if (value.equals("true"))
            return true;
        else if (value.equals("false"))
            return false;
        
        problems.add(new FXGInvalidBooleanValueProblem(node.getDocumentPath(), node.getStartLine(), 
                node.getStartColumn(), name, value));
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG hexadecimal color String to an int. The
     * format must be a '#' character followed by six hexadecimal characters,
     * i.e. '#RRGGBB'.
     * 
     * @param node - the FXG node.
     * @param value - an FXG a hexadecimal color String.
     * @param name - the FXG attribute name.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return an RGB color represented as an int.
     */
    public static int parseRGB(IFXGNode node, String value, String name, int defaultValue, Collection<ICompilerProblem> problems)
    {
        Matcher m;

        m = rgbPattern.matcher(value);
        if (!m.matches ())
        {
            //Invalid color format: {0}
            problems.add(new FXGInvalidColorValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));
            return defaultValue;
        }    

        value = value.substring(1);

        int a = 255;
        int r = Integer.parseInt(value.substring(0, 2), 16) & 0xFF;
        int g = Integer.parseInt(value.substring(2, 4), 16) & 0xFF;
        int b = Integer.parseInt(value.substring(4, 6), 16) & 0xFF;

        return  (a << 24) | (r << 16) | (g << 8) | b;        
    }

    /**
     * Convert an FXG String value to a double.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the double precision floating point value represented by the
     * String. 
     */
    public static double parseDouble(IFXGNode node, String value, String name, double defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch(NumberFormatException e)
        {
            problems.add(new FXGInvalidDoubleValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));
            return defaultValue;
        }        
    }

    /**
     * Convert an FXG String value to a double after taking care of the % sign.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the double precision floating point value represented by the
     * String.
     */
    public static double parsePercent(IFXGNode node, String value, String name, double defaultValue, Collection<ICompilerProblem> problems)
    {
        if (value.length() != 0 && value.charAt(value.length()-1) == '%')
        {
            try 
            {
                String doubleValue = value.substring(0, value.length()-1);
                return parseDouble(node, doubleValue, Double.MIN_VALUE, Double.MAX_VALUE);
            } 
            catch(Exception e)
            {
                problems.add(new FXGInvalidPercentValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), name, value));
                
                return defaultValue;
            } 
        }

        return parseDouble(node, value, name, defaultValue, problems); 
    }
    
    /**
     * Convert an FXG String value to a double after taking care of the % sign.
     * If the value is double, it is checked against the specified range 
     * (inclusive).
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method.
     * @return the double precision floating point value represented by the
     * String.
     */
    public static double parseNumberPercent(IFXGNode node, String value, String name, double min, double max, double defaultValue, Collection<ICompilerProblem> problems)
    {
        if (value.length() != 0 && value.charAt(value.length()-1) == '%')
        {                       
            try 
            {
                String doubleValue = value.substring(0, value.length()-1);
                return parseDouble(node, doubleValue, min, max);
            } 
            catch(Exception e)
            {
                problems.add(new FXGInvalidPercentValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), name, value));
                
                return defaultValue;
            }
        }
        
        return parseDouble(node, value, name, min, max, defaultValue, problems);
    }
    
    /**
     * Convert an FXG String value to a double after taking care of the % sign.
     * If the value is double, it is checked against the specified range 
     * (inclusive).
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @return the double precision floating point value represented by the
     * String.
     * @throws NumberFormatException if the number is not in an acceptable format 
     * or out of bounds defined by min and max.
     */
    public static double parseNumberPercent(IFXGNode node, String value, double min, double max) throws NumberFormatException
    {
        if (value.length() != 0 && value.charAt(value.length()-1) == '%')
        {                       
            String doubleValue = value.substring(0, value.length()-1);
            return parseDouble(node, doubleValue, min, max);
        }
        
        return parseDouble(node, value, min, max);
    }
    
    /**
     * Convert an FXG String value to a double after taking care of the % sign.
     * If the value is double, it is checked against the specified range 
     * (inclusive). There are separate ranges for percent and number.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param minNumber - the smallest double value that the result must be greater
     * or equal to.
     * @param maxNumber - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method.
     * @return the double precision floating point value represented by the
     * String.
     */
    public static double parseNumberPercentWithSeparateRange(IFXGNode node, String value, String name, double minNumber, 
            double maxNumber, double minPercent, double maxPercent, 
            double defaultValue, Collection<ICompilerProblem> problems)
    {
        if (value.length() != 0 && value.charAt(value.length()-1) == '%')
        {
            try 
            {
                String doubleValue = value.substring(0, value.length()-1);
                return parseDouble(node, doubleValue, minPercent, maxPercent);
            } 
            catch(Exception e)
            {
                problems.add(new FXGInvalidPercentValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), name, value));
                
                return defaultValue;
            }
        }
        
        return parseDouble(node, value, name, minNumber, maxNumber, defaultValue, problems);
    }
    
    /**
     * Convert an FXG String value to a double and check that the result is
     * within the specified range (inclusive).
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @return the double precision floating point value represented by the
     * String.
     * @throws NumberFormatException if the number is not in an acceptable format 
     * or out of bounds defined by min and max.
     */
    public static double parseDouble(IFXGNode node, String value, double min, double max) throws NumberFormatException
    {
        double d = Double.parseDouble(value);
        if (d >= min && d <= max)
        {
            return d;
        }
        
        throw new NumberFormatException("Double is out of bounds");
    }
    
    /**
     * Convert an FXG String value to a double and check that the result is
     * within the specified range (inclusive).
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest double value that the result must be greater
     * or equal to.
     * @param max - the largest double value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default double value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method.
     * @return the double precision floating point value represented by the
     * String.
     */
    public static double parseDouble(IFXGNode node, String value, String name, double min, double max, double defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
            double d = Double.parseDouble(value);
            if (d >= min && d <= max)
            {
                return d;
            }
        }
        catch(NumberFormatException e)
        {
            problems.add(new FXGInvalidDoubleValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));
            return defaultValue;
        }

        if (((AbstractFXGNode)node).isVersionGreaterThanCompiler())
        {
            // Warning: Minor version of this FXG file is greater than minor
            // version supported by this compiler. Use default value if an
            // attribute value is out of range.
            FXGLog.getLogger().log(IFXGLogger.WARN, "DefaultAttributeValue", null, ((AbstractFXGNode)node).getDocumentPath(), node.getStartLine(), node.getStartColumn());
        }
        else
        {
            // Numeric value {0} must be greater than or equal to {1}
            // and less than or equal to {2}.
            problems.add(new FXGOutOfRangeValueProblem(node.getDocumentPath(), node.getStartLine(), node.getStartColumn(), name, value, min, max));
        }
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to a float.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the floating point value represented by the String.
     */
    public static float parseFloat(IFXGNode node, String name, String value, float defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
            return Float.parseFloat(value);            
        }
        catch(NumberFormatException e)
        {
            problems.add(new FXGInvalidFloatValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));
            return defaultValue;
        }       
    }
    
    /**
     * Convert an FXG String value to a float.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @return the floating point value represented by the String.
     * @throws NumberFormatException if the number is not in an acceptable format.
     */
    public static float parseFloat(IFXGNode node, String value) throws NumberFormatException
    {
        return Float.parseFloat(value); 
    }
    
    /**
     * Convert an FXG String value to an int and check that the result is
     * within the specified range (inclusive).
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param name - the FXG attribute name.
     * @param min - the smallest int value that the result must be greater
     * or equal to.
     * @param max - the largest int value that the result must be smaller
     * than or equal to.
     * @param defaultValue - the default int value; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method.
     * @return the integer value represented by the String.
     */
    public static int parseInt(IFXGNode node, String value, String name, int min, int max, int defaultValue, Collection<ICompilerProblem> problems)
    {
        try
        {
            int i = Integer.parseInt(value);
            
            if (i >= min && i <= max)
            {
                return i;
            }
        }
        catch(NumberFormatException e)
        {
            problems.add(new FXGInvalidIntegerValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), name, value));
            
            return defaultValue;
        }

        if (((AbstractFXGNode)node).isVersionGreaterThanCompiler())
        {
            // Warning: Minor version of this FXG file is greater than minor
            // version supported by this compiler. Use default value if an
            // attribute value is out of range.
            FXGLog.getLogger().log(IFXGLogger.WARN, "DefaultAttributeValue", null, ((AbstractFXGNode)node).getDocumentPath(), node.getStartLine(), node.getStartColumn());
        }
        else
        {
            // Numeric value {0} must be greater than or equal to {1}
            // and less than or equal to {2}.
            problems.add(new FXGOutOfRangeValueProblem(node.getDocumentPath(), node.getStartLine(), node.getStartColumn(), name, value, min, max));
        }
        
        return defaultValue;
    }
    
    /**
     * Convert an FXG String value to an int and check that the result is
     * within the specified range (inclusive).
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param min - the smallest int value that the result must be greater
     * or equal to.
     * @param max - the largest int value that the result must be smaller
     * than or equal to.
     * @return the integer value represented by the String.
     * @throws NumberFormatException if the number is not in an acceptable format 
     * or out of bounds defined by min and max.
     */
    public static int parseInt(IFXGNode node, String value, int min, int max) throws NumberFormatException
    {
        int i = Integer.parseInt(value);

        if (i >= min && i <= max)
        {
            return i;
        }

        throw new NumberFormatException("Integer is out of bounds");
    }

    /**
     * Convert an FXG String value to an InterpolationMethod enumeration.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value
     * @param defaultValue - the FXG InterpolationMethod default value
     * @param problems problem collection used to collect problems occurred within this method.
     * @return the matching InterpolationMethod; if the encountered minor 
     * version is later than the supported minor version and the attribute value
     *  is out-of-range, the default value is returned.
     */
    public static InterpolationMethod parseInterpolationMethod(IFXGNode node, String value, InterpolationMethod defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_INTERPOLATION_RGB_VALUE.equals(value))
        {
            return InterpolationMethod.RGB;
        }
        else if (FXG_INTERPOLATION_LINEARRGB_VALUE.equals(value))
        {
            return InterpolationMethod.LINEAR_RGB;
        }
        else
        {
            if (((AbstractFXGNode)node).isVersionGreaterThanCompiler())
            {
                // Warning: Minor version of this FXG file is greater than minor
                // version supported by this compiler. Use default value if an
                // attribute value is out of range.
                FXGLog.getLogger().log(IFXGLogger.WARN, "DefaultAttributeValue", null, ((AbstractFXGNode)node).getDocumentPath(), node.getStartLine(), node.getStartColumn());
            }
            else
            {
                //Unknown interpolation method.
                problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), FXG_INTERPOLATIONMETHOD_ATTRIBUTE, value));
            }
            
            return defaultValue;
        }
    }

    /**
     * Convert an FXG String value to a MaskType enumeration.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value
     * @param defaultValue - the FXG MaskType default value; if the encountered 
     * minor version is later than the supported minor version and the attribute
     *  value is out-of-range, the default value is returned.
     * @param problems problem collection used to collect problems occurred within this method.
     * @return the matching MaskType
     */
    public static MaskType parseMaskType(IFXGNode node, String value, MaskType defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_MASK_CLIP_VALUE.equals(value))
        {
            return MaskType.CLIP;
        }
        else if (FXG_MASK_ALPHA_VALUE.equals(value))
        {
            return MaskType.ALPHA;
        }
        else if (((AbstractFXGNode)node).getFileVersion().equalTo(FXGVersion.v1_0))
        {
            // FXG 1.0 does not support any more maskTypes
            // Unknown maskType {0}.
            problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                    node.getStartColumn(), FXG_MASKTYPE_ATTRIBUTE, value)); 
            return defaultValue;
        }
        else if (FXG_MASK_LUMINOSITY_VALUE.equals(value))
        {
            return MaskType.LUMINOSITY;
        }
        else
        {
            if (((AbstractFXGNode)node).isVersionGreaterThanCompiler())
            {
                // Warning: Minor version of this FXG file is greater than minor
                // version supported by this compiler. Use default value if an
                // attribute value is out of range.
                FXGLog.getLogger().log(IFXGLogger.WARN, "DefaultAttributeValue", null, ((AbstractFXGNode)node).getDocumentPath(), node.getStartLine(), node.getStartColumn());
            }
            else
            {
                // Unknown maskType {0}.
                problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), FXG_MASKTYPE_ATTRIBUTE, value)); 
            }
            
            return defaultValue;
        }
    }

    /**
     * Convert an FXG String value to a fillMode enumeration.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value.
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     * @return the matching fillMode value.
     */
    public static FillMode parseFillMode(IFXGNode node, String value, FillMode defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_FILLMODE_CLIP_VALUE.equals(value))
        {
            return FillMode.CLIP;
        }
        else if (FXG_FILLMODE_REPEAT_VALUE.equals(value))
        {
            return FillMode.REPEAT;
        }
        else if (FXG_FILLMODE_SCALE_VALUE.equals(value))
        {
            return FillMode.SCALE;
        }
        else
        {
            if (((AbstractFXGNode)node).isVersionGreaterThanCompiler())
            {
                // Warning: Minor version of this FXG file is greater than minor
                // version supported by this compiler. Use default value if an
                // attribute value is out of range.
                FXGLog.getLogger().log(IFXGLogger.WARN, "DefaultAttributeValue", null, 
                        ((AbstractFXGNode)node).getDocumentPath(), node.getStartLine(), node.getStartColumn());
            }
            else
            {
                // Unknown fill mode.
                problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), FXG_FILLMODE_ATTRIBUTE, value)); 
            }
            
            return defaultValue;
        }
            
    }
    
    /**
     * Convert an FXG String value to a SpreadMethod enumeration.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value
     * @param defaultValue - the FXG SpreadMethod default value
     * @param problems problem collection used to collect problems occurred within this method.
     * @return the matching SpreadMethod; if the encountered minor version is 
     * later than the supported minor version and the attribute value is 
     * out-of-range, the default value is returned.
     */
    public static SpreadMethod parseSpreadMethod(IFXGNode node, String value, SpreadMethod defaultValue, Collection<ICompilerProblem> problems)
    {
        if (FXG_SPREADMETHOD_PAD_VALUE.equals(value))
        {
            return SpreadMethod.PAD;
        }
        else if (FXG_SPREADMETHOD_REFLECT_VALUE.equals(value))
        {
            return SpreadMethod.REFLECT;
        }
        else if (FXG_SPREADMETHOD_REPEAT_VALUE.equals(value))
        {
            return SpreadMethod.REPEAT;
        }
        else
        {
            if (((AbstractFXGNode)node).isVersionGreaterThanCompiler())
            {
                // Warning: Minor version of this FXG file is greater than minor
                // version supported by this compiler. Use default value if an
                // attribute value is out of range.
                FXGLog.getLogger().log(IFXGLogger.WARN, "DefaultAttributeValue", null, ((AbstractFXGNode)node).getDocumentPath(), node.getStartLine(), node.getStartColumn());
            }
            else
            {
                // Unknown spreadMethod.
                problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), FXG_SPREADMETHOD_ATTRIBUTE, value)); 
            }
            
            return defaultValue;
        }
    }
    
    /**
     * Convert an FXG String value to a Identifier matching pattern 
     * [a-zA-Z_][a-zA-Z_0-9]*.
     * 
     * @param node - the FXG node.
     * @param value - the FXG String value
     * @param defaultValue default value to use in case of any problem
     * @param problems problem collection used to collect problems occurred within this method
     */
    public static String parseIdentifier(IFXGNode node, String name, String value, String defaultValue, Collection<ICompilerProblem> problems)
    {
        Matcher m;

        m = idPattern.matcher(value);
        if (m.matches ())
        {
            return value; 
        }
        else
        {
            if (((AbstractFXGNode)node).isVersionGreaterThanCompiler())
            {
                // Warning: Minor version of this FXG file is greater than minor
                // version supported by this compiler. Use default value if an
                // attribute value is out of range.
                FXGLog.getLogger().log(IFXGLogger.WARN, "DefaultAttributeValue", null, ((AbstractFXGNode)node).getDocumentPath(), node.getStartLine(), node.getStartColumn());
            }
            else
            {
                // Invalid identifier format
                problems.add(new FXGUnknownAttributeValueProblem(node.getDocumentPath(), node.getStartLine(), 
                        node.getStartColumn(), name, value)); 
            }
            
            return defaultValue;
        }
    }
}
