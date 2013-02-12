package  
{

import flash.events.MouseEvent;

import spark.components.Button;
import spark.components.Group;
import spark.components.Label;

public class Example extends Group
{
	private static const BYEBYE:String = "Bye Bye";
	private static const HELLOWORLD:String = "Hello World";
	
	private static var counter:int = 100;

	public function Example() 
	{
		init();
	}
	
	private var _btn1:Button;
	private var _btn2:Button;
	private var _btn3:Button;
	private var _lbl1:Label;
	private var _lbl2:Label;
	
	public function init():void
	{
		_lbl1 = new Label();
		_lbl1.x = 100;
		_lbl1.y = 25;
		_lbl1.text = Example.HELLOWORLD;
		
		addElement(_lbl1);
		
		_lbl2 = new Label();
		_lbl2.x = 200;
		_lbl2.y = 25;
		_lbl2.text = Example.counter + "";
		
		addElement(_lbl2);
		
		_btn1 = new Button();
		_btn1.x = 100;
		_btn1.y = 50;
		_btn1.label = "Click me";
		_btn1.addEventListener(MouseEvent.CLICK, btn1clickHandler);
		
		addElement(_btn1);

		_btn2 = new Button();
		_btn2.x = 200;
		_btn2.y = 50;
		_btn2.label = "Add it";
		_btn2.addEventListener(MouseEvent.CLICK, btn2clickHandler);
		
		addElement(_btn2);
		
		_btn3 = new Button();
		_btn3.x = 300;
		_btn3.y = 50;
		_btn3.label = "Move it";
		_btn3.addEventListener(MouseEvent.CLICK, btn3clickHandler);
		
		addElement(_btn3);
	}
	
	protected function btn1clickHandler(event:MouseEvent):void
	{
		if (_lbl1.text == Example.HELLOWORLD)
			_lbl1.text = Example.BYEBYE;
		else
			_lbl1.text = Example.HELLOWORLD;
	}
	
	protected function btn2clickHandler(event:MouseEvent):void
	{
		_lbl2.text = --Example.counter + "";
	}
	
	protected function btn3clickHandler(event:MouseEvent):void
	{
		_btn3.x += 10;
	}
	
}
}