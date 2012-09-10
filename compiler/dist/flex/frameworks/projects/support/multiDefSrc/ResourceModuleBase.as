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
	
	import flash.utils.getDefinitionByName; 
	import mx.modules.ModuleBase; 
	import mx.resources.IResourceModule; 
	import mx.resources.ResourceBundle; 
	
	[ExcludeClass]  
	public class ResourceModuleBase extends ModuleBase implements IResourceModule 
	{ 
		private var resourceBundleClassNames : Array;
		private var _resourceBundles:Array;  
		
		public function ResourceModuleBase(resourceBundleClassNames : Array) 
		{ 
			super();
			this.resourceBundleClassNames = resourceBundleClassNames;
		}
		
		public function get resourceBundles():Array 
		{ 
			if (!_resourceBundles) 
			{ 
				_resourceBundles = []; 
				var n:int = resourceBundleClassNames.length; 
				for (var i:int = 0; i < n; i++) 
				{ 
					var resourceBundleClass:Class = 
						Class(getDefinitionByName(resourceBundleClassNames[i])); 
					var resourceBundle:ResourceBundle = new resourceBundleClass(); 
					_resourceBundles.push(resourceBundle); 
				} 
			}  
			
			return _resourceBundles; 
		} 
	}
}