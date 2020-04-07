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
 * classes.C
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('classes.C');

goog.require('classes.B');



/**
 * @constructor
 * @extends {classes.B}
 */
classes.C = function() {
  classes.C.base(this, 'constructor');
};
goog.inherits(classes.C, classes.B);


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
classes.C.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'C', qName: 'classes.C', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
classes.C.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'C': { type: '', declaredBy: 'classes.C'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
classes.C.prototype.ROYALE_COMPILE_FLAGS = 9;
