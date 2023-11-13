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
 * comps.A
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('comps.A');

goog.require('comps.B');



/**
 * @constructor
 */
comps.A = function() {
  var /** @type {comps.B} */ foo = new comps.B();
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
comps.A.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'A', qName: 'comps.A', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
comps.A.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'A': { type: '', declaredBy: 'comps.A'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
comps.A.prototype.ROYALE_COMPILE_FLAGS = 9;