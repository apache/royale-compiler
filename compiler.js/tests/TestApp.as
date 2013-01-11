////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////
package
{
	import flash.display.Sprite;

	public class TestApp extends Sprite
	{
		private var linker:MainCode = new MainCode();
		
		public var publicVar:int = 99;
		
		private var privateVar:int = 44;
		
		public function TestApp()
		{
			
		}
		
		private function foo():int
		{
			return publicVar;
		}
		
		private function get bar():int
		{
			return publicVar;
		}
		
		private function set bar(value:int):void
		{
			return publicVar;
		}

		private function baz():int
		{
			return publicVar;
		}
		
		private function get boo():int
		{
			return publicVar;
		}
		
		private function set boo(value:int):void
		{
			return publicVar;
		}
	}
	
}