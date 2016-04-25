////////////////////////////////////////////////////////////////////////////////
//
//	Licensed to the Apache Software Foundation (ASF) under one or more
//	contributor license agreements.	See the NOTICE file distributed with
//	this work for additional information regarding copyright ownership.
//	The ASF licenses this file to You under the Apache License, Version 2.0
//	(the "License"); you may not use this file except in compliance with
//	the License.	You may obtain a copy of the License at
//
//			http://www.apache.org/licenses/LICENSE-2.0
//
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package
{

import mx.core.mx_internal;

import bases.HelperBaseClass;

use namespace mx_internal;

public class SomeSDKClass
{
	public function SomeSDKClass() {}; 

	private var number:Number = 'Got it: ' + this.getString(); 

	public function getString():String
	{
		return Helper.helperFunction();
	}

	public function someFunction():String
	{
		helperBaseClass.doSomething();
	}

	mx_internal var helperBaseClass:HelperBaseClass = new HelperBaseClass();
}

}

import bases.HelperBaseClass;

class Helper extends HelperBaseClass
{

	public static function helperFunction():String {
		return "Hello world";
	}
	
	public function Helper(url:String) {
	  url_ = url;
	}
	
	private var url_:String;
	
	public function get url():String {
		return url_;
	}

}