package org.apache.flex
{

import flash.events.EventDispatcher;

import spark.components.Button;

public class A extends EventDispatcher
{
	public function A() {}
	
	public function init()
	{
		var btn:Button = new Button();
		
		addEventListener("click", function () {});
	}
}
}