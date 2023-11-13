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
 * Case
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('Case');

goog.require('comps.A');



/**
 * @constructor
 */
Case = function() {
  var /** @type {comps.A} */ bar = new comps.A();
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
Case.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'Case', qName: 'Case', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
Case.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'Case': { type: '', declaredBy: 'Case'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
Case.prototype.ROYALE_COMPILE_FLAGS = 9;