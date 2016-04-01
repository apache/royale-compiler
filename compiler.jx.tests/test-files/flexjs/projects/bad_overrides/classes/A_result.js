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
 * classes.A
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('classes.A');

goog.require('interfaces.IA');



/**
 * @constructor
 * @implements {interfaces.IA}
 */
classes.A = function() {
};


/**
 * @export
 * @return {classes.B}
 */
classes.A.prototype.someFunction = function() {
  return null;
};


/**
 * @export
 * @return {interfaces.IB}
 */
classes.A.prototype.someOtherFunction = function() {
  return null;
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
classes.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'classes.A'}], interfaces: [interfaces.IA] };


/**
 * Prevent renaming of class. Needed for reflection.
 */
goog.exportSymbol('classes.A', classes.A);



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
classes.A.prototype.FLEXJS_REFLECTION_INFO = function () {
  return {
    variables: function () {
      return {
      };
    },
    accessors: function () {
      return {
      };
    },
    methods: function () {
      return {
        'A': { type: '', declaredBy: 'classes.A'},
        'someFunction': { type: 'B', declaredBy: 'classes.A'},
        'someOtherFunction': { type: 'IB', declaredBy: 'classes.A'}
      };
    }
  };
};