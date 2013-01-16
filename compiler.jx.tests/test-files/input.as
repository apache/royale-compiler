package org.apache.flex
{

import flash.events.IEventDispatcher;

import spark.components.Button;

public dynamic class A extends spark.components.Button implements IEventDispatcher
{
	public function A()
	{
		trace(typeof "a");
	}
	
	private var _a:ArgumentError = new ArgumentError();
}
}