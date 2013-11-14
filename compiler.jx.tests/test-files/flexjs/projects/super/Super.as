package
{
  public class Super
  {
    public function Super() {}; 

    private var _text:String = '';
  
    public function get text():String 
    {
      return _text;
    };
  
    public function set text(value:String):void 
    {
      if (value != _text)
      {
        _text = value;
      }
    };
  }
}