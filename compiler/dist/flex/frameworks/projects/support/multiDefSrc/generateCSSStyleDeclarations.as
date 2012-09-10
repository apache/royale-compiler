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

package flex.compiler.support
{
    import mx.core.FlexVersion;
    import mx.core.IFlexModuleFactory;
    import mx.core.mx_internal;
    import mx.styles.CSSCondition;
    import mx.styles.CSSConditionKind;
    import mx.styles.CSSSelector;
    import mx.styles.CSSStyleDeclaration;
    import mx.styles.IStyleManager2;
    import mx.utils.ObjectUtil;
    
    public function generateCSSStyleDeclarationsForComponents(styleManager:IStyleManager2, factoryFunctions:Object, data:Array, isCompatibilityVersion3:Boolean = false):void
    {
        if(isCompatibilityVersion3)
            FlexVersion.compatibilityVersionString = "3.0.0";
        
        var arr:Array = data;
        
        var conditions:Array = null;
        var condition:CSSCondition = null;
        var selector:CSSSelector = null;
        var style:CSSStyleDeclaration;
        var declarationName:String = "";
        var segmentName:String = "";
        var effects:Array;
        
        var mergedStyle:CSSStyleDeclaration;
        
        var conditionCombiners:Object = {};
        conditionCombiners[CSSConditionKind.CLASS] = ".";
        conditionCombiners[CSSConditionKind.ID] = "#";
        conditionCombiners[CSSConditionKind.PSEUDO] = ':';    
        var n:int = arr.length;
        for (var i:int = 0; i < n; i++)
        {
            var className:int = arr[i];
            if (className == CSSClass.CSSSelector)
            {
                var selectorName:String = arr[++i];
                selector = new CSSSelector(selectorName, conditions, selector);
                segmentName = selectorName + segmentName;
                if (declarationName != "")
                    declarationName += " ";
                declarationName += segmentName;
                segmentName = "";
                conditions = null;
            }
            else if (className == CSSClass.CSSCondition)
            {
                if (!conditions)
                    conditions = [];
                var conditionType:String = arr[++i];
                var conditionName:String = arr[++i];
                condition = new CSSCondition(conditionType, conditionName);
                conditions.push(condition);
                segmentName = segmentName + conditionCombiners[conditionType] + conditionName;
            }
            else if (className == CSSClass.CSSStyleDeclaration)
            {
                var factoryName:int = arr[++i]; // defaultFactory or factory
                var defaultFactory:Boolean = factoryName == CSSFactory.DefaultFactory;
                if (isCompatibilityVersion3)
                {
                    style = styleManager.getStyleDeclaration(declarationName);
                    if (!style)
                    {
                        style = new CSSStyleDeclaration(null, styleManager);
                        styleManager.setStyleDeclaration(declarationName, style, false);
                    }
                    
                    if (defaultFactory)
                    {
                        if (style.defaultFactory == null)
                            style.defaultFactory = factoryFunctions[declarationName];
                    }
                    else
                    {
                        if (style.factory == null)
                            style.factory = factoryFunctions[declarationName];
                    }
                }
                else
                {
                    if (defaultFactory)
                    {
                        mergedStyle = styleManager.getMergedStyleDeclaration(declarationName);
                        style = new CSSStyleDeclaration(selector, styleManager, mergedStyle == null);
                    }
                    else
                    {
                        style = styleManager.getStyleDeclaration(declarationName);
                        if (!style)
                            style = new CSSStyleDeclaration(selector, styleManager, mergedStyle == null);
                    }
                    if (defaultFactory)
                    {
                        if (style.defaultFactory == null)
                            style.defaultFactory = factoryFunctions[declarationName];
                    }
                    else
                    {
                        if (style.factory == null)
                            style.factory = factoryFunctions[declarationName];
                    }
                    if (defaultFactory && mergedStyle != null && 
                        (mergedStyle.defaultFactory == null ||
                            ObjectUtil.compare(new style.defaultFactory(), new mergedStyle.defaultFactory())))
                    {
                        styleManager.setStyleDeclaration(style.mx_internal::selectorString, style, false);
                    }
                }
                selector = null;
                conditions = null;
                declarationName = "";
                mergedStyle = null;
            }
        }
    }
    
    public function generateCSSStyleDeclarations(fbs:IFlexModuleFactory, factoryFunctions:Object, data:Array, isCompatibilityVersion3:Boolean = false):IStyleManager2
    {
        var styleManager : IStyleManager2 = fbs.getImplementation("mx.styles::IStyleManager2") as IStyleManager2;
        generateCSSStyleDeclarationsForComponents(styleManager, factoryFunctions, data, isCompatibilityVersion3);
        return styleManager;
    }
}

class CSSClass
{
    public static const CSSSelector:int = 0;
    public static const CSSCondition:int = 1;
    public static const CSSStyleDeclaration:int = 2;
}

class CSSFactory
{
    public static const DefaultFactory:int = 0;
    public static const Factory:int = 1;
}

class CSSDataType
{
    public static const Native:int = 0;
    public static const Definition:int = 1;
}
