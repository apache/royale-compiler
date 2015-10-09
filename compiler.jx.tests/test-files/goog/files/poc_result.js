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

goog.require('flash.events.MouseEvent');
goog.require('goog.events.BrowserEvent');
goog.require('goog.events.Event');
goog.require('goog.events.EventTarget');

/**
 * @constructor
 * @extends {goog.events.EventTarget}
 */
Example = function() {
	var self = this;
	Example.base(this, 'constructor');
	self.init();
};
goog.inherits(Example, goog.events.EventTarget);

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
 * @type {goog.events.Event}
 */
Example.prototype._btn1;

/**
 * @private
 * @type {goog.events.Event}
 */
Example.prototype._btn2;

/**
 * @private
 * @type {goog.events.Event}
 */
Example.prototype._btn3;

/**
 * @private
 * @type {goog.events.BrowserEvent}
 */
Example.prototype._lbl1;

/**
 * @private
 * @type {goog.events.BrowserEvent}
 */
Example.prototype._lbl2;

/**
 * @private
 * @type {goog.events.EventTarget}
 */
Example.prototype._et1;

/**
 * @private
 * @type {goog.events.EventTarget}
 */
Example.prototype._et2;

/**
 * @private
 * @type {goog.events.EventTarget}
 */
Example.prototype._et3;

Example.prototype.init = function() {
	var self = this;
	self._et1 = new goog.events.EventTarget();
	self._et2 = new goog.events.EventTarget();
	self._et3 = new goog.events.EventTarget();
	self._lbl1 = new goog.events.BrowserEvent();
	self._lbl1.clientX = 100;
	self._lbl1.clientY = 25;
	self._lbl1.type = Example.HELLOWORLD;
	self.dispatchEvent(self._lbl1);
	self._lbl2 = new goog.events.BrowserEvent();
	self._lbl2.clientX = 200;
	self._lbl2.clientY = 25;
	self._lbl2.type = Example.counter + "";
	self.dispatchEvent(self._lbl2);
	self._btn1 = new goog.events.Event();
	self._btn1.type = "Click me";
	self._et1.addEventListener(flash.events.MouseEvent.CLICK, self.btn1clickHandler);
	self._et1.dispatchEvent(self._btn1);
	self._btn2 = new goog.events.Event();
	self._btn2.type = "Add it";
	self._et2.addEventListener(flash.events.MouseEvent.CLICK, self.btn2clickHandler);
	self._et2.dispatchEvent(self._btn2);
	self._btn3 = new goog.events.Event();
	self._btn3.type = "Move it";
	self._et3.addEventListener(flash.events.MouseEvent.CLICK, self.btn3clickHandler);
	self._et3.dispatchEvent(self._btn3);
};

/**
 * @param {flash.events.MouseEvent} event
 */
Example.prototype.btn1clickHandler = function(event) {
	var self = this;
	if (self._lbl1.type == Example.HELLOWORLD)
		self._lbl1.type = Example.BYEBYE;
	else
		self._lbl1.type = Example.HELLOWORLD;
};

/**
 * @param {flash.events.MouseEvent} event
 */
Example.prototype.btn2clickHandler = function(event) {
	var self = this;
	self._lbl2.type = --Example.counter + "";
};

/**
 * @param {flash.events.MouseEvent} event
 */
Example.prototype.btn3clickHandler = function(event) {
	var self = this;
	self._lbl2.clientX += 10;
};
