package
{
	import classes.A;
	import interfaces.IA;
	import interfaces.IC;
	import interfaces.IE;

  public class Test extends A implements IA, IE
  {
    public function Test()
    {
      super();
      
      var ia:IA = doSomething(IC) as IA
    }
    
    public function doSomething(ic:IC):IC
    {
      return ic;
    }
  }
}