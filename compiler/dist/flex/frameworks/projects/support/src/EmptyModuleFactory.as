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
import flash.display.LoaderInfo;
import flash.display.Sprite;
import flash.utils.Dictionary;

import flashx.textLayout.compose.ISWFContext;
import mx.core.IFlexModuleFactory;
import mx.core.RSLData;

[ExcludeClass]

/**
 *  An empty module factory. This is used by the Falcon compiler as the 
 *  base class of a SWC's library.swf root class.
 */
public class EmptyModuleFactory extends Sprite implements IFlexModuleFactory,ISWFContext
{
    public function EmptyModuleFactory()
    {
    }
    
    public function get allowDomainsInNewRSLs():Boolean
    {
        return false;
    }
    
    public function set allowDomainsInNewRSLs(value:Boolean):void
    {
    }
    
    public function get allowInsecureDomainsInNewRSLs():Boolean
    {
        return false;
    }
    
    public function set allowInsecureDomainsInNewRSLs(value:Boolean):void
    {
    }
    
    public function get preloadedRSLs():Dictionary
    {
        return null;
    }
    
    public function addPreloadedRSL(loaderInfo:LoaderInfo, rsl:Vector.<RSLData>):void
    {
    }
    
    public function allowDomain(...parameters):void
    {
    }
    
    public function allowInsecureDomain(...parameters):void
    {
    }
    
    public function callInContext(fn:Function, thisArg:Object, argArray:Array, returns:Boolean=true):*
    {
        return null;
    }
    
    public function create(...parameters):Object
    {
        return null;
    }
    
    public function getImplementation(interfaceName:String):Object
    {
        return null;
    }
    
    public function info():Object
    {
        return {};
    }
    
    public function registerImplementation(interfaceName:String, impl:Object):void
    {
    }
}
}