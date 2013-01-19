package org.apache.flex
{

import spark.components.Button;

public dynamic class A extends spark.components.Button
{
	public function A()
	{
		super();
	}
	
	public function hasSuperCall(a:String, b:Number)
	{
		super.hasSuperCall(a, b, 100);
		
		var result:String = myRegularFunctionCall(-1);
	}
}
}