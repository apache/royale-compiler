package
{
	import Super;

  public class Base extends Super
  {
    public function Base() 
    {
      super();
    }; 

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
  }
}