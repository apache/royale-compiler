package
{
	
	public class LocalFunction
	{
		public function LocalFunction() {}
		
		private var myMemberProperty:String = "got it: ";
		
		private function myMemberMethod(value:int):void
		{
			function myLocalFunction(value:int):String
			{
				return myMemberProperty + value;
			}
			
			trace("WOW! :: " + myLocalFunction(value + 42));
		}
		
		public function doIt():void
		{
			myMemberMethod(624);
		}
	}
	
}