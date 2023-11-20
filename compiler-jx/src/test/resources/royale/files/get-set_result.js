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
 * @nocollapse
 * @export
 * @type {number}
 */
org.apache.royale.A.prototype.a;


org.apache.royale.A.prototype.get__a = function() {
  return -1;
};


org.apache.royale.A.prototype.set__a = function(value) {
  this._a = value;
};


Object.defineProperties(org.apache.royale.A.prototype, /** @lends {org.apache.royale.A.prototype} */ {
/**
 * @type {number}
 */
a: {
get: org.apache.royale.A.prototype.get__a,
set: org.apache.royale.A.prototype.set__a}}
);