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

package
{
import flash.system.ApplicationDomain;
import flash.system.Security;
import flash.text.Font;
import flash.utils.getQualifiedClassName;
import mx.core.mx_internal;
import mx.modules.ModuleBase;
import mx.styles.CSSCondition;
import mx.styles.CSSSelector;
import mx.styles.CSSStyleDeclaration;
import mx.styles.StyleManager;
import mx.styles.IStyleManager2;
import mx.styles.IStyleModule;
import mx.core.IFlexModule;
import mx.core.IFlexModuleFactory;

[Frame(factoryClass="mx.core.FlexModuleFactory")]

public class StyleModuleBase extends ModuleBase implements IStyleModule, IFlexModule
{
    // This declaration is for adding dependency to CSSModule2_StyleData in the old compiler.
    // Falcon's codegen doesn't need this.
    // public static var dataclass:CSSModule2_StyleData;
    
    private var _moduleFactory:IFlexModuleFactory;
    
    public function get moduleFactory():IFlexModuleFactory
    {
        return _moduleFactory;
    }
    public function set moduleFactory(value:IFlexModuleFactory):void
    {
        _moduleFactory = value;
    }
    
    public function setStyleDeclarations(styleManager:IStyleManager2):void
    {
        this.styleManager = styleManager;
   
        // remember if the global selector exists
        var hadGlobal:Boolean = styleManager.getStyleDeclaration("global") == null;
        
        // don't force-link StyleManagerImpl.  Otherwise, compiler will set up
        // styles for it within this module, which is unnecessary
        // The following is a bit inefficient, but you took the time to load
        // a module already
        var styleManagerClassName:String = getQualifiedClassName(styleManager);
        var currentDomain:ApplicationDomain = moduleFactory.info().currentDomain;
        var styleManagerImpl:Object = currentDomain.getDefinition(styleManagerClassName);
        
        var styleDataClass:Object = currentDomain.getDefinition(
                                            moduleFactory.info()["styleDataClassName"]);
        
        var inheritingStyles:String = styleDataClass["inheritingStyles"];
        var styleNames:Array = inheritingStyles.split(",");
        for each (var s:String in styleNames)
        {
            styleManager.registerInheritingStyle(s);
        }
        
        // and we assume generateCSSStyleDeclarations is a static on the Class
        styleManagerImpl.generateCSSStyleDeclarations(styleManager, 
                                                        styleDataClass["factoryFunctions"], 
                                                        styleDataClass["data"], 
                                                        selectors, 
                                                        overrideMap);
        
        // There is more work to do when unloading if this module introduced
        // the global selector.
        if (!hadGlobal && styleManager.getStyleDeclaration("global") != null)
            unloadGlobal = true;
    }
    
    /**
     * @private
     */
    public var selectors:Array = [];
    
    /**
     * @private
     */
    public var overrideMap:Object = {};
    
    /**
     * @private
     */
    private var effectMap:Object = {};
    
    /**
     * @private
     */
    private var unloadGlobal:Boolean;
    
    /**
     * @private
     */
    public var styleManager:IStyleManager2;
    
    /**
     * @private
     */
    private static var domainsAllowed:Boolean = allowDomains();

    /**
     * @private
     */
    private static function allowDomains():Boolean
    {
		// allowDomain not allowed in AIR
		if (Security.sandboxType != "application")
			Security.allowDomain("*");
        return true;
    }
    

    public function StyleModuleBase()
    {
        super();
    }
    

    public function unload():void
    {
        unloadOverrides();
        unloadStyleDeclarations();

        if (unloadGlobal)
        {
            styleManager.stylesRoot = null;
            styleManager.initProtoChainRoots();
        }
    }

    /**
     * @private
     */
    private function unloadOverrides():void
    {
        for (var selector:String in overrideMap)
        {
            var style:CSSStyleDeclaration = styleManager.getStyleDeclaration(selector);

            if (style != null)
            {
                var keys:Array = overrideMap[selector];
                var numKeys:int;
                var i:uint;

                if (keys != null)
                {
                    numKeys = keys.length;

                    for (i = 0; i < numKeys; i++)
                    {
                        style.mx_internal::clearOverride(keys[i]);
                    }
                }

                keys = effectMap[selector];

                if (keys != null)
                {
                    numKeys = keys.length;
                    var index:uint;
                    var effects:Array = style.mx_internal::effects;

                    for (i = 0; i < numKeys; i++)
                    {
                        index = effects.indexOf(numKeys[i]);
                        if (index >= 0)
                        {
                            effects.splice(index, 1);
                        }
                    }                    
                }
            }
        }

        overrideMap = null;
        effectMap = null;
    }

    /**
     * @private
     */
    private function unloadStyleDeclarations():void
    {
        var numSelectors:int = selectors.length;

        for (var i:int = 0; i < numSelectors; i++)
        {
            var selector:String = selectors[i];
            styleManager.clearStyleDeclaration(selector, false);
        }

        selectors = null;
    }
}

}
