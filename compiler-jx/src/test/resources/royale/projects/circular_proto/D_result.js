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
 * Generated by Apache Royale Compiler from D.as
 * D
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('D');

goog.require('C');



/**
 * @constructor
 * @extends {C}
 */
D = function() {
  D.base(this, 'constructor');
};
goog.inherits(D, C);


/**
 * @export
 * @param {boolean} b
 * @return {number}
 */
D.a = function(b) {
  return 0;
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
D.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'D', qName: 'D', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
D.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'D': { type: '', declaredBy: 'D'},
        '|a': { type: 'int', declaredBy: 'D', parameters: function () { return [ { index: 1, type: 'Boolean', optional: false } ]; }}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
D.prototype.ROYALE_COMPILE_FLAGS = 8;
