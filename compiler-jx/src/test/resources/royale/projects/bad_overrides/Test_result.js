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
 * @suppress {checkTypes|accessControls}
 */

goog.provide('Test');

goog.require('classes.A');



/**
 * @constructor
 * @extends {classes.A}
 */
Test = function() {
  Test.base(this, 'constructor');
};
goog.inherits(Test, classes.A);


/**
 * @export
 * @override
 */
Test.prototype.someFunction = function() {
  return null;
};


/**
 * @export
 * @override
 */
Test.prototype.someOtherFunction = function() {
  return null;
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Test.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'Test', qName: 'Test', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
Test.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'Test': { type: '', declaredBy: 'Test'},
        'someFunction': { type: 'C', declaredBy: 'Test'},
        'someOtherFunction': { type: 'IC', declaredBy: 'Test'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
Test.prototype.ROYALE_COMPILE_FLAGS = 8;
