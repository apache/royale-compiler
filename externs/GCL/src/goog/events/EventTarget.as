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

import goog.Disposable;

/**
 * An implementation of {@code goog.events.Listenable} with full W3C
 * EventTarget-like support (capture/bubble mechanism, stopping event
 * propagation, preventing default actions).
 * 
 * You may subclass this class to turn your class into a Listenable.
 * 
 * Unless propagation is stopped, an event dispatched by an
 * EventTarget will bubble to the parent returned by
 * {@code getParentEventTarget}. To set the parent, call
 * {@code setParentEventTarget}. Subclasses that don't support
 * changing the parent can override the setter to throw an error.
 * 
 * Example usage:
 * <pre>
 * var source = new goog.events.EventTarget();
 * function handleEvent(e) {
 * alert('Type: ' + e.type + '; Target: ' + e.target);
 * }
 * source.listen('foo', handleEvent);
 * // Or: goog.events.listen(source, 'foo', handleEvent);
 * ...
 * source.dispatchEvent('foo');  // will call handleEvent
 * ...
 * source.unlisten('foo', handleEvent);
 * // Or: goog.events.unlisten(source, 'foo', handleEvent);
 * </pre>
 *
 * @see [eventtarget]
 */
public class EventTarget extends Disposable implements Listenable {


    /**
     * Marks a given class (constructor) as an implementation of
     * Listenable, do that we can query that fact at runtime. The class
     * must have already implemented the interface.
     *
     * @param cls [Function] The class constructor. The corresponding class must have already implemented the interface.
     * @see [listenable]
     */
    public static function addImplementation(cls:Function):void {}

    /**
     * @param obj [(Object|null)] The object to check.
     * @see [listenable]
     * @returns {boolean} Whether a given instance implements Listenable. The class/superclass of the instance must call addImplementation.
     */
    public static function isImplementedBy(obj:Object):Boolean {return false}

    /**
     * An implementation of {@code goog.events.Listenable} with full W3C
     * EventTarget-like support (capture/bubble mechanism, stopping event
     * propagation, preventing default actions).
     * 
     * You may subclass this class to turn your class into a Listenable.
     * 
     * Unless propagation is stopped, an event dispatched by an
     * EventTarget will bubble to the parent returned by
     * {@code getParentEventTarget}. To set the parent, call
     * {@code setParentEventTarget}. Subclasses that don't support
     * changing the parent can override the setter to throw an error.
     * 
     * Example usage:
     * <pre>
     * var source = new goog.events.EventTarget();
     * function handleEvent(e) {
     * alert('Type: ' + e.type + '; Target: ' + e.target);
     * }
     * source.listen('foo', handleEvent);
     * // Or: goog.events.listen(source, 'foo', handleEvent);
     * ...
     * source.dispatchEvent('foo');  // will call handleEvent
     * ...
     * source.unlisten('foo', handleEvent);
     * // Or: goog.events.unlisten(source, 'foo', handleEvent);
     * </pre>
     *
     * @see [eventtarget]
     */
    public function EventTarget() {
        super();
    }

    /**
     * @see [eventtarget]
     */
    public function removeAllListeners(type:String = null):Number { return 0 }

    /**
     * Sets the target to be used for {@code event.target} when firing
     * event. Mainly used for testing. For example, see
     * {@code goog.testing.events.mixinListenable}.
     *
     * @param target [Object] The target.
     * @see [eventtarget]
     */
    public function setTargetForTesting(target:Object):void {  }

    /**
     * Adds an event listener to the event target. The same handler can only be
     * added once per the type. Even if you add the same handler multiple times
     * using the same type then it will only be called once when the event is
     * dispatched.
     *
     * @param type [string] The type of the event to listen for.
     * @param handler [(function (?): ?|null|{handleEvent: function (?): ?})] The function to handle the event. The handler can also be an object that implements the handleEvent method which takes the event object as argument.
     * @param opt_capture [(boolean|undefined)] In DOM-compliant browsers, this determines whether the listener is fired during the capture or bubble phase of the event.
     * @param opt_handlerScope [(Object|null|undefined)] Object in whose scope to call the listener.
     * @see [eventtarget]
     */
    public function addEventListener(type:String, handler:Function, opt_capture:Boolean = false, opt_handlerScope:Object = null):void {  }


    /**
     * Returns the parent of this event target to use for bubbling.
     *
     * @see [eventtarget]
     * @returns {(goog.events.EventTarget|null)} The parent EventTarget or null if there is no parent.
     */
    public function getParentEventTarget():EventTarget {  return null; }

    /**
     * Removes an event listener from the event target. The handler must be the
     * same object as the one added. If the handler has not been added then
     * nothing is done.
     *
     * @param type [string] The type of the event to listen for.
     * @param handler [(function (?): ?|null|{handleEvent: function (?): ?})] The function to handle the event. The handler can also be an object that implements the handleEvent method which takes the event object as argument.
     * @param opt_capture [(boolean|undefined)] In DOM-compliant browsers, this determines whether the listener is fired during the capture or bubble phase of the event.
     * @param opt_handlerScope [(Object|null|undefined)] Object in whose scope to call the listener.
     * @see [eventtarget]
     */
    public function removeEventListener(type:String, handler:Function, opt_capture:Boolean = false, opt_handlerScope:Object = null):void {  }

    /**
     * Asserts that the event target instance is initialized properly.
     *
     * @see [eventtarget]
     */
    public function assertInitialized_():void {  }

    /**
     * @see [eventtarget]
     */
    public function unlisten(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):void {  }

    /**
     * @see [eventtarget]
     */
    public function fireListeners(type:Object, capture:Boolean, eventObject:Object):Boolean { return false }

    /**
     * @see [eventtarget]
     */
    public function listenOnce(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):ListenableKey { return null }

    /**
     * @see [eventtarget]
     */
    public function unlistenByKey(key:ListenableKey):Boolean { return false }

    /**
     * @see [eventtarget]
     */
    public function dispatchEvent(e:Object):Boolean { return false }

    /**
     * @see [eventtarget]
     */
    public function hasListener(opt_type:Object = null, opt_capture:Boolean = false):Boolean { return false }

    /**
     * @see [eventtarget]
     */
    public function getListener(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):goog.events.ListenableKey { return null }

    /**
     * @see [eventtarget]
     */
    public function listen(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):goog.events.ListenableKey { return null }

    /**
     * Dispatches the given event on the ancestorsTree.
     *
     * @param target [Object] The target to dispatch on.
     * @param e [(Object|goog.events.Event|null|string)] The event object.
     * @param opt_ancestorsTree [(Array<(goog.events.Listenable|null)>|null|undefined)] The ancestors tree of the target, in reverse order from the closest ancestor to the root event target. May be null if the target has no ancestor.
     * @see [eventtarget]
     * @returns {boolean} If anyone called preventDefault on the event object (or if any of the listeners returns false) this will also return false.
     */
    public static function dispatchEventInternal_(target:Object, e:Object, opt_ancestorsTree:Array = null):Boolean {  return false }

    /**
     * @see [eventtarget]
     */
    public function getListeners(type:Object, capture:Boolean):Array { return null }

    /**
     * Sets the parent of this event target to use for capture/bubble
     * mechanism.
     *
     * @param parent [(goog.events.EventTarget|null)] Parent listenable (null if none).
     * @see [eventtarget]
     */
    public function setParentEventTarget(parent:EventTarget):void {  }

}
}
