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
/**
 * org.apache.royale.A
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('org.apache.royale.A');

goog.require('custom.TestImplementation');
goog.require('custom.TestInterface');



/**
 * @constructor
 * @extends {custom.TestImplementation}
 * @implements {custom.TestInterface}
 */
org.apache.royale.A = function() {
  org.apache.royale.A.base(this, 'constructor');
  
  this._a = new ArgumentError();
  org.apache.royale.utils.Language.trace(typeof("a"));
};
goog.inherits(org.apache.royale.A, custom.TestImplementation);


/**
 * @nocollapse
 * @const
 * @type {string}
 */
org.apache.royale.A.MY_CLASS_CONST = "myClassConst";


/**
 * @private
 * @type {ArgumentError}
 */
org.apache.royale.A.prototype._a = null;


/**
 * @const
 * @type {string}
 */
org.apache.royale.A.prototype.MY_INSTANCE_CONST = "myInstanceConst";