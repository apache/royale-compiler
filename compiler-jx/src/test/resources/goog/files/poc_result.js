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
goog.provide('Example');

goog.require('custom.MouseEvent');
goog.require('custom.TestEvent');
goog.require('custom.TestImplementation');

/**
 * @constructor
 * @extends {custom.TestImplementation}
 */
Example = function() {
	var self = this;
	Example.base(this, 'constructor');
	self.init();
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
Example.prototype._btn1;

/**
 * @private
 * @type {custom.TestEvent}
 */
Example.prototype._btn2;

/**
 * @private
 * @type {custom.TestEvent}
 */
Example.prototype._btn3;

/**
 * @private
 * @type {custom.MouseEvent}
 */
Example.prototype._lbl1;

/**
 * @private
 * @type {custom.MouseEvent}
 */
Example.prototype._lbl2;

/**
 * @private
 * @type {custom.TestImplementation}
 */
Example.prototype._et1;

/**
 * @private
 * @type {custom.TestImplementation}
 */
Example.prototype._et2;

/**
 * @private
 * @type {custom.TestImplementation}
 */
Example.prototype._et3;

Example.prototype.init = function() {
	var self = this;
	self._et1 = new custom.TestImplementation();
	self._et2 = new custom.TestImplementation();
	self._et3 = new custom.TestImplementation();
	self._lbl1 = new custom.MouseEvent();
	self._lbl1.localX = 100;
	self._lbl1.localY = 25;
	self._lbl1.type = Example.HELLOWORLD;
	self.dispatchEvent(self._lbl1);
	self._lbl2 = new custom.MouseEvent();
	self._lbl2.localX = 200;
	self._lbl2.localY = 25;
	self._lbl2.type = Example.counter + "";
	self.dispatchEvent(self._lbl2);
	self._btn1 = new custom.TestEvent();
	self._btn1.type = "Click me";
	self._et1.addEventListener(custom.MouseEvent.CLICK, self.btn1clickHandler);
	self._et1.dispatchEvent(self._btn1);
	self._btn2 = new custom.TestEvent();
	self._btn2.type = "Add it";
	self._et2.addEventListener(custom.MouseEvent.CLICK, self.btn2clickHandler);
	self._et2.dispatchEvent(self._btn2);
	self._btn3 = new custom.TestEvent();
	self._btn3.type = "Move it";
	self._et3.addEventListener(custom.MouseEvent.CLICK, self.btn3clickHandler);
	self._et3.dispatchEvent(self._btn3);
};

/**
 * @param {custom.MouseEvent} event
 */
Example.prototype.btn1clickHandler = function(event) {
	var self = this;
	if (self._lbl1.type == Example.HELLOWORLD)
		self._lbl1.type = Example.BYEBYE;
	else
		self._lbl1.type = Example.HELLOWORLD;
};

/**
 * @param {custom.MouseEvent} event
 */
Example.prototype.btn2clickHandler = function(event) {
	var self = this;
	self._lbl2.type = --Example.counter + "";
};

/**
 * @param {custom.MouseEvent} event
 */
Example.prototype.btn3clickHandler = function(event) {
	var self = this;
	self._lbl2.clientX += 10;
};
