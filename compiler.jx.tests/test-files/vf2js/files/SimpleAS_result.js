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
 * org.apache.flex.A
 *
 * @fileoverview
 *
 * @suppress {checkTypes}
 */

goog.provide('org.apache.flex.A');



/**
 * @constructor
 * @extends {vf2js_s.components.Button}
 * @implements {flash.events.IEventDispatcher}
 */
org.apache.flex.A = function() {
  goog.base(this);
  org.apache.flex.utils.Language.trace(typeof("a"));
};
goog.inherits(org.apache.flex.A, vf2js_s.components.Button);


/**
 * @const
 * @type {string}
 */
org.apache.flex.A.MY_CLASS_CONST = "myClassConst";


/**
 * @private
 * @type {ArgumentError}
 */
org.apache.flex.A.prototype._a = new ArgumentError();


/**
 * @private
 * @type {vf2js_mx.components.Button}
 */
org.apache.flex.A.prototype._mxButton = new vf2js_mx.components.Button();


/**
 * @const
 * @type {string}
 */
org.apache.flex.A.prototype.MY_INSTANCE_CONST = "myInstanceConst";


/**
 * Metadata
 *
 * @type {Object.<string, Array.<Object>>}
 */
org.apache.flex.A.prototype.FLEXJS_CLASS_INFO = { names: [{ name: 'A', qName: 'org.apache.flex.A'}], interfaces: [flash.events.IEventDispatcher] };

