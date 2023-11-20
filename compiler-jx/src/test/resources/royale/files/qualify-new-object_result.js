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



/**
 * @constructor
 * @extends {custom.TestImplementation}
 */
org.apache.royale.A = function() {
  org.apache.royale.A.base(this, 'constructor');
  this.init();
};
goog.inherits(org.apache.royale.A, custom.TestImplementation);


/**
 * @private
 * @type {custom.TestImplementation}
 */
org.apache.royale.A.prototype._privateVar = null;


/**
 */
org.apache.royale.A.prototype.init = function() {
  var self = this;
  var /** @type {custom.TestImplementation} */ btn = new custom.TestImplementation();
  this._privateVar = new custom.TestImplementation();
  this.addEventListener("click", function() {
  });
};


/**
 */
org.apache.royale.A.prototype.start = function() {
  var /** @type {string} */ localVar = org.apache.royale.utils.Language.string(this._privateVar.label);
  this.init();
  doIt();
};