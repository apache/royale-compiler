////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package  
{

import custom.MouseEvent;
import custom.TestEvent;
import custom.TestImplementation;

public class Example extends TestImplementation
{
	private static const BYEBYE:String = "Bye Bye";
	private static const HELLOWORLD:String = "Hello World";
	
	private static var counter:int = 100;

	public function Example() 
	{
		init();
	}
	
	private var _btn1:TestEvent;
	private var _btn2:TestEvent;
	private var _btn3:TestEvent;
	private var _lbl1:MouseEvent;
	private var _lbl2:MouseEvent;
	private var _et1:TestImplementation;
	private var _et2:TestImplementation;
	private var _et3:TestImplementation;
	
	public function init():void
	{
		_et1 = new TestImplementation();
		_et2 = new TestImplementation();
		_et3 = new TestImplementation();
		
		_lbl1 = new MouseEvent();
		_lbl1.localX = 100;
		_lbl1.localY = 25;
		_lbl1.type = Example.HELLOWORLD;
		
		dispatchEvent(_lbl1);
		
		_lbl2 = new MouseEvent();
		_lbl2.localX = 200;
		_lbl2.localY = 25;
		_lbl2.type = Example.counter + "";
		
		dispatchEvent(_lbl2);
		
		_btn1 = new TestEvent();
		_btn1.type = "Click me";
		_et1.addEventListener(MouseEvent.CLICK, btn1clickHandler);
		
		_et1.dispatchEvent(_btn1);

		_btn2 = new TestEvent();
		_btn2.type = "Add it";
		_et2.addEventListener(MouseEvent.CLICK, btn2clickHandler);
		
		_et2.dispatchEvent(_btn2);
		
		_btn3 = new TestEvent();
		_btn3.type = "Move it";
		_et3.addEventListener(MouseEvent.CLICK, btn3clickHandler);
		
		_et3.dispatchEvent(_btn3);
	}
	
	protected function btn1clickHandler(event:MouseEvent):void
	{
		if (_lbl1.type == Example.HELLOWORLD)
			_lbl1.type = Example.BYEBYE;
		else
			_lbl1.type = Example.HELLOWORLD;
	}
	
	protected function btn2clickHandler(event:MouseEvent):void
	{
		_lbl2.type = --Example.counter + "";
	}
	
	protected function btn3clickHandler(event:MouseEvent):void
	{
		_lbl2.clientX += 10;
	}
	
}
}
