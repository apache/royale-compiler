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

goog.require('custom.TestInterface');
goog.require('custom.TestImplementation');

/**
 * @constructor
 * @extends {custom.TestImplementation}
 * @implements {custom.TestInterface}
 */
org.apache.royale.A = function() {
	var self = this;
	org.apache.royale.A.base(this, 'constructor');
	self.trace(typeof("a"));
};
goog.inherits(org.apache.royale.A, custom.TestImplementation);

/**
 * @const
 * @type {string}
 */
org.apache.royale.A.MY_CLASS_CONST = "myClassConst";

/**
 * @private
 * @type {ArgumentError}
 */
org.apache.royale.A.prototype._a = new ArgumentError();

/**
 * @const
 * @type {string}
 */
org.apache.royale.A.prototype.MY_INSTANCE_CONST = "myInstanceConst";
