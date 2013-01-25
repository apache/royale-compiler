package org.apache.flex
{

import flash.events.EventDispatcher;

import spark.components.Button;

public class A extends EventDispatcher
{
	public function A() 
	{
		init();
	}
	
	private var _privateVar:Button;
	
	public function init():void
	{
		var btn:Button = new Button();
		
		_privateVar = new Button();
		
		addEventListener("click", function () {});
	}
	
	public function start():void
	{
		var localVar:String = _privateVar.label;
		init();
		doIt();
	}
}
}