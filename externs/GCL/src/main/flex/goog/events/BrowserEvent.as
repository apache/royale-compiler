// Copyright 2005 The Closure Library Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS-IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package goog.events {

import goog.events;
import goog.events.BrowserEvent.MouseButton;

/**
 * Accepts a browser event object and creates a patched, cross browser event
 * object.
 * The content of this object will not be initialized if no event object is
 * provided. If this is the case, init() needs to be invoked separately.
 *
 * @see [browserevent]
 */
public class BrowserEvent extends goog.events.Event {

    public var altKey:Boolean;
    public var button:uint;
    public var charCode:uint;
    public var clientX:Number;
    public var clientY:Number;
    public var offsetX:Number;
    public var offsetY:Number;
    public var screenX:Number;
    public var screenY:Number;
    public var ctrlKey:Boolean;
    public var metaKey:Boolean;
    public var shiftKey:Boolean;
    public var keyCode:uint;
    public var relatedTarget:Object;
    public var state:Object;
    public var timestamp:Date;

    /**
     * Accepts a browser event object and creates a patched, cross browser event
     * object.
     * The content of this object will not be initialized if no event object is
     * provided. If this is the case, init() needs to be invoked separately.
     *
     * @param opt_e [(Event|null|undefined)] Browser event object.
     * @param opt_currentTarget [(EventTarget|null|undefined)] Current target for event.
     * @see [browserevent]
     */
    public function BrowserEvent(opt_e:goog.events.Event = null, opt_currentTarget:EventTarget = null) {
        super(null, null);
    }

    /**
     * Tests to see which button was pressed during the event. This is really only
     * useful in IE and Gecko browsers. And in IE, it's only useful for
     * mousedown/mouseup events, because click only fires for the left mouse button.
     * 
     * Safari 2 only reports the left button being clicked, and uses the value '1'
     * instead of 0. Opera only reports a mousedown event for the middle button, and
     * no mouse events for the right button. Opera has default behavior for left and
     * middle click that can only be overridden via a configuration setting.
     * 
     * There's a nice table of this mess at http://www.unixpapa.com/js/mouse.html.
     *
     * @param button [(goog.events.BrowserEvent.MouseButton|null)] The button to test for.
     * @see [browserevent]
     * @returns {boolean} True if button was pressed.
     */
    public function isButton(button:goog.events.BrowserEvent.MouseButton):Boolean {  return null; }

    /**
     * Whether this has an "action"-producing mouse button.
     * 
     * By definition, this includes left-click on windows/linux, and left-click
     * without the ctrl key on Macs.
     *
     * @see [browserevent]
     * @returns {boolean} The result.
     */
    public function isMouseActionButton():Boolean {  return null; }


    /**
     * Accepts a browser event object and creates a patched, cross browser event
     * object.
     *
     * @param e [(Event|null)] Browser event object.
     * @param opt_currentTarget [(EventTarget|null|undefined)] Current target for event.
     * @see [browserevent]
     */
    public function init(e:Event, opt_currentTarget:EventTarget = null):void {  }


    /**
     * @see [browserevent]
     * @returns {(Event|null)} The underlying browser event object.
     */
    public function getBrowserEvent():Object {  return null; }

}
}
