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

/**
 * A base class for event objects, so that they can support preventDefault and
 * stopPropagation.
 *
 * @see [event]
 */
public class Event {

    /**
     * A base class for event objects, so that they can support preventDefault and
     * stopPropagation.
     *
     * @param type [(goog.events.EventId|string)] Event Type.
     * @param opt_target [(Object|null|undefined)] Reference to the object that is the target of this event. It has to implement the {@code EventTarget} interface declared at {@link http://developer.mozilla.org/en/DOM/EventTarget}.
     * @see [event]
     */
    public function Event(type:Object, opt_target:Object = null) {
        super();
    }

    public var type:String;
    public var target:Object;
    public var currentTarget:Object;
    public var defaultPrevented:Boolean;

    public function stopPropagation():void {}
    public function preventDefault():void {}

    /**
     * Stops the propagation of the event. It is equivalent to
     * {@code e.stopPropagation()}, but can be used as the callback argument of
     * {@link goog.events.listen} without declaring another function.
     *
     * @param e [goog.events.Event] An event.
     * @see [event]
     */
    public static function stopPropagation(e:goog.events.Event):void {  }

    /**
     * Prevents the default action. It is equivalent to
     * {@code e.preventDefault()}, but can be used as the callback argument of
     * {@link goog.events.listen} without declaring another function.
     *
     * @param e [goog.events.Event] An event.
     * @see [event]
     */
    public static function preventDefault(e:goog.events.Event):void {  }

}
}
