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

package codegraph.golden
{
    /** Dispatched when graph work completes. */
    [Event(name="complete", type="codegraph.golden.GraphEvent")]
    [DefaultProperty("label")]
    /**
    * "Root" graph description.
     *
     * @see codegraph.golden.GraphBase
     * @copy codegraph.golden.GraphBase#label
     * @see codegraph.golden.IGraphContract
     */
    public class GraphRoot extends GraphBase implements IGraphContract
    {
        public static const VERSION:String = "1";

        public function GraphRoot(value:String)
        {
        }

        public function execute(required:String, optional:Number = 2, ...rest):Boolean
        {
            return packageFunction(required);
        }

        override public function inheritedMethod():void
        {
        }

        private function hidden():void
        {
        }

        /**
         * @private
         */
        public function documentedPrivate():void
        {
        }
    }
}