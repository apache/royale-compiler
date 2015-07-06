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

goog.require('classes.A');
goog.require('interfaces.IA');
goog.require('interfaces.IC');
goog.require('interfaces.IE');
goog.require('org_apache_flex_utils_Language');



/**
 * @constructor
 * @extends {classes.A}
 * @implements {interfaces.IA}
 * @implements {interfaces.IE}
 */
Test = function() {
  Test.base(this, 'constructor');
  var /** @type {interfaces.IA} */ ia = org_apache_flex_utils_Language.as(this.doSomething(interfaces.IC), interfaces.IA);
};
goog.inherits(Test, classes.A);


/**
 * @export
 * @param {interfaces.IC} ic
 * @return {interfaces.IC}
 */
Test.prototype.doSomething = function(ic) {
  for (var /** @type {number} */ i = 0; i < 3; i++) {
    var /** @type {classes.A} */ a = null;
  }
  this.superClass_.doStuff.call(this);
  return ic;
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Test.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'Test', qName: 'Test'}], interfaces: [interfaces.IA, interfaces.IE] };
