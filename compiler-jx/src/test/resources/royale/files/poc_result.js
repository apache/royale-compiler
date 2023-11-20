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
 * Example
 *
 * @fileoverview
 *
 * @suppress {checkTypes|accessControls}
 */

goog.provide('Example');

goog.require('custom.MouseEvent');
goog.require('custom.TestEvent');
goog.require('custom.TestImplementation');



/**
 * @constructor
 * @extends {custom.TestImplementation}
 */
Example = function() {
  Example.base(this, 'constructor');
  this.init();
};
goog.inherits(Example, custom.TestImplementation);


/**
 * @private
 * @const
 * @type {string}
 */
Example.BYEBYE = "Bye Bye";


/**
 * @private
 * @const
 * @type {string}
 */
Example.HELLOWORLD = "Hello World";


/**
 * @private
 * @type {number}
 */
Example.counter = 100;


/**
 * @private
 * @type {custom.TestEvent}
 */
Example.prototype._btn1 = null;


/**
 * @private
 * @type {custom.TestEvent}
 */
Example.prototype._btn2 = null;


/**
 * @private
 * @type {custom.TestEvent}
 */
Example.prototype._btn3 = null;


/**
 * @private
 * @type {custom.MouseEvent}
 */
Example.prototype._lbl1 = null;


/**
 * @private
 * @type {custom.MouseEvent}
 */
Example.prototype._lbl2 = null;


/**
 * @private
 * @type {custom.TestImplementation}
 */
Example.prototype._et1 = null;


/**
 * @private
 * @type {custom.TestImplementation}
 */
Example.prototype._et2 = null;


/**
 * @private
 * @type {custom.TestImplementation}
 */
Example.prototype._et3 = null;


/**
 */
Example.prototype.init = function() {
  this._et1 = new custom.TestImplementation();
  this._et2 = new custom.TestImplementation();
  this._et3 = new custom.TestImplementation();
  this._lbl1 = new custom.MouseEvent();
  this._lbl1.localX = 100;
  this._lbl1.localY = 25;
  this._lbl1.type = Example.HELLOWORLD;
  this.dispatchEvent(this._lbl1);
  this._lbl2 = new custom.MouseEvent();
  this._lbl2.localX = 200;
  this._lbl2.localY = 25;
  this._lbl2.type = Example.counter + "";
  this.dispatchEvent(this._lbl2);
  this._btn1 = new custom.TestEvent();
  this._btn1.type = "Click me";
  this._et1.addEventListener(custom.MouseEvent.CLICK, org.apache.royale.utils.Language.closure(this.btn1clickHandler, this, 'btn1clickHandler'));
  this._et1.dispatchEvent(this._btn1);
  this._btn2 = new custom.TestEvent();
  this._btn2.type = "Add it";
  this._et2.addEventListener(custom.MouseEvent.CLICK, org.apache.royale.utils.Language.closure(this.btn2clickHandler, this, 'btn2clickHandler'));
  this._et2.dispatchEvent(this._btn2);
  this._btn3 = new custom.TestEvent();
  this._btn3.type = "Move it";
  this._et3.addEventListener(custom.MouseEvent.CLICK, org.apache.royale.utils.Language.closure(this.btn3clickHandler, this, 'btn3clickHandler'));
  this._et3.dispatchEvent(this._btn3);
};


/**
 * @protected
 * @param {custom.MouseEvent} event
 */
Example.prototype.btn1clickHandler = function(event) {
  if (this._lbl1.type == Example.HELLOWORLD)
    this._lbl1.type = Example.BYEBYE;
  else
    this._lbl1.type = Example.HELLOWORLD;
};


/**
 * @protected
 * @param {custom.MouseEvent} event
 */
Example.prototype.btn2clickHandler = function(event) {
  this._lbl2.type = --Example.counter + "";
};


/**
 * @protected
 * @param {custom.MouseEvent} event
 */
Example.prototype.btn3clickHandler = function(event) {
  this._lbl2.clientX += 10;
};