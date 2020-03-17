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
 * interfaces.IE
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('interfaces.IE');



/**
 * @interface
 */
interfaces.IE = function() {
};
interfaces.IE.prototype.myMethod = function() {
};
/**  * @type {string}
 */interfaces.IE.prototype.myProp;


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
interfaces.IE.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'IE', qName: 'interfaces.IE', kind: 'interface' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
interfaces.IE.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    accessors: function () {
      return {
        'myProp': { type: 'String', access: 'readwrite', declaredBy: 'interfaces.IE'}
      };
    },
    methods: function () {
      return {
        'myMethod': { type: 'void', declaredBy: 'interfaces.IE'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
interfaces.IE.prototype.ROYALE_COMPILE_FLAGS = 9;
