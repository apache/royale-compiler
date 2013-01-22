package org.apache.flex
{

import flash.events.IEventDispatcher;

import spark.components.Button;

public dynamic class A extends spark.components.Button implements IEventDispatcher
{
	public static const MY_CLASS_CONST:String = "myClassConst";
	
	public function A()
	{
		trace(typeof "a");
	}
	
	private var _a:ArgumentError = new ArgumentError();

	public const MY_INSTANCE_CONST:String = "myInstanceConst";
}
}