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
 * Test
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('Test');

goog.require('classes_A');
goog.require('interfaces_IA');
goog.require('interfaces_IC');
goog.require('interfaces_IE');
goog.require('org_apache_flex_utils_Language');



/**
 * @constructor
 * @extends {classes_A}
 * @implements {interfaces_IA}
 * @implements {interfaces_IE}
 */
Test = function() {
  Test.base(this, 'constructor');
  var /** @type {interfaces_IA} */ ia = org_apache_flex_utils_Language.as(this.doSomething(interfaces_IC), interfaces_IA);
};
goog.inherits(Test, classes_A);


/**
 * @expose
 * @param {interfaces_IC} ic
 * @return {interfaces_IC}
 */
Test.prototype.doSomething = function(ic) {
  for (var /** @type {number} */ i = 0; i < 3; i++) {
    var /** @type {classes_A} */ a = null;
  }
  return ic;
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Test.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'Test', qName: 'Test'}], interfaces: [interfaces_IA, interfaces_IE] };
