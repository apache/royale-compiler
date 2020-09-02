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
 * MainClass
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('MainClass');
goog.provide('MainClass.InternalClass');

goog.require('OtherClass');



/**
 * @constructor
 */
MainClass = function() {
};


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
MainClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'MainClass', qName: 'MainClass', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
MainClass.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    methods: function () {
      return {
        'MainClass': { type: '', declaredBy: 'MainClass'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
MainClass.prototype.ROYALE_COMPILE_FLAGS = 9;



/**
 * @constructor
 */
MainClass.InternalClass = function() {
  this.foo = new OtherClass();
};


/**
 * @type {OtherClass}
 */
MainClass.InternalClass.prototype.foo = null;


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
MainClass.InternalClass.prototype.ROYALE_CLASS_INFO = { names: [{ name: 'InternalClass', qName: 'MainClass.InternalClass', kind: 'class' }] };



/**
 * Reflection
 *
 * @return {Object.<string, Function>}
 */
MainClass.InternalClass.prototype.ROYALE_REFLECTION_INFO = function () {
  return {
    variables: function () {
      return {
        'foo': { type: 'OtherClass', get_set: function (/** MainClass.InternalClass */ inst, /** * */ v) {return v !== undefined ? inst.foo = v : inst.foo;}}
      };
    },
    methods: function () {
      return {
        'InternalClass': { type: '', declaredBy: 'MainClass.InternalClass'}
      };
    }
  };
};
/**
 * @const
 * @type {number}
 */
MainClass.InternalClass.prototype.ROYALE_COMPILE_FLAGS = 9;
