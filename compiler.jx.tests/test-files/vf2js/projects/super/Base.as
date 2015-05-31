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

import Super;

public class Base extends Super
{
	public static var myClassConst:String = new Number();
	
	public function Base() 
	{
		super();
	}; 

	private var number:Number = this.getNumber(); 
	
	private var newText:String = this.text; 
	
	private var newTextAgain:String = text; 
	
	override public function get text():String 
	{
		return "A" + super.text;
	};

	override public function set text(value:String):void 
	{
		if (value != super.text)
		{
			super.text = "B" + value;
		}
	};
	
	public function getNumber():void
	{
		alert(super.doStuff());
		
		var x:Number = super.x;
	}
	
	override public function doStuff():Number 
	{
		throw new Error("No way!");
	};

}
}