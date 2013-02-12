package org.apache.flex
{

import flash.events.IEventDispatcher;

import spark.components.Button;

public dynamic class A extends Button implements IEventDispatcher
{
	public function A(z:String)
	{
		super(z);
	}
	
	public function hasSuperCall(a:String, b:Number):String
	{
		super.hasSuperCall(a, b, 100);
		
		var result:String = myRegularFunctionCall(-1);
		
		return result;
	}
}
}