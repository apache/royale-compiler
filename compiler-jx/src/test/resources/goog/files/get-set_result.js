/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
goog.provide('org.apache.royale.A');

/**
 * @constructor
 */
org.apache.royale.A = function() {
};

/**
 * @private
 * @type {number}
 */
org.apache.royale.A.prototype._a = -1;

/**
 * @type {number}
 */
org.apache.royale.A.prototype.a;

Object.defineProperty(
	org.apache.royale.A.prototype, 
	'a', 
	{get:function() {
		var self = this;
		return -1;
	}, configurable:true}
);

Object.defineProperty(
	org.apache.royale.A.prototype, 
	'a', 
	{set:function(value) {
		var self = this;
		self._a = value;
	}, configurable:true}
);
