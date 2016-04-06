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
 * A listenable interface. A listenable is an object with the ability
 * to dispatch/broadcast events to "event listeners" registered via
 * listen/listenOnce.
 * 
 * The interface allows for an event propagation mechanism similar
 * to one offered by native browser event targets, such as
 * capture/bubble mechanism, stopping propagation, and preventing
 * default actions. Capture/bubble mechanism depends on the ancestor
 * tree constructed via {@code #getParentEventTarget}; this tree
 * must be directed acyclic graph. The meaning of default action(s)
 * in preventDefault is specific to a particular use case.
 * 
 * Implementations that do not support capture/bubble or can not have
 * a parent listenable can simply not implement any ability to set the
 * parent listenable (and have {@code #getParentEventTarget} return
 * null).
 * 
 * Implementation of this class can be used with or independently from
 * goog.events.
 * 
 * Implementation must call {@code #addImplementation(implClass)}.
 *
 * @see goog.events 
 * @see http://www.w3.org/TR/DOM-Level-2-Events/events.html 
 * @see [listenable]
 */
public interface Listenable {

    /**
     * Removes all listeners from this listenable. If type is specified,
     * it will only remove listeners of the particular type. otherwise all
     * registered listeners will be removed.
     *
     * @see [listenable]
     */
    function removeAllListeners(type:String = null):Number;

    /**
     * Removes an event listener which was added with listen() by the key
     * returned by listen().
     *
     * @see [listenable]
     */
    function unlistenByKey(key:ListenableKey):Boolean;

    /**
     * Dispatches an event (or event like object) and calls all listeners
     * listening for events of this type. The type of the event is decided by the
     * type property on the event object.
     * 
     * If any of the listeners returns false OR calls preventDefault then this
     * function will return false.  If one of the capture listeners calls
     * stopPropagation, then the bubble listeners won't fire.
     *
     * @see [listenable]
     */
    function dispatchEvent(e:Object):Boolean;

    /**
     * Returns the parent of this event target to use for capture/bubble
     * mechanism.
     * 
     * NOTE(chrishenry): The name reflects the original implementation of
     * custom event target ({@code goog.events.EventTarget}). We decided
     * that changing the name is not worth it.
     *
     * @see [listenable]
     */
    function getParentEventTarget():EventTarget;

    /**
     * Fires all registered listeners in this listenable for the given
     * type and capture mode, passing them the given eventObject. This
     * does not perform actual capture/bubble. Only implementors of the
     * interface should be using this.
     *
     * @see [listenable]
     */
    function fireListeners(type:Object, capture:Boolean, eventObject:Object):Boolean;

    /**
     * Removes an event listener which was added with listen() or listenOnce().
     *
     * @see [listenable]
     */
    function unlisten(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):void;

    /**
     * Whether there is any active listeners matching the specified
     * signature. If either the type or capture parameters are
     * unspecified, the function will match on the remaining criteria.
     *
     * @see [listenable]
     */
    function hasListener(opt_type:Object = null, opt_capture:Boolean = false):Boolean;

    /**
     * Adds an event listener that is removed automatically after the
     * listener fired once.
     * 
     * If an existing listener already exists, listenOnce will do
     * nothing. In particular, if the listener was previously registered
     * via listen(), listenOnce() will not turn the listener into a
     * one-off listener. Similarly, if there is already an existing
     * one-off listener, listenOnce does not modify the listeners (it is
     * still a once listener).
     *
     * @see [listenable]
     */
    function listenOnce(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):ListenableKey;

    /**
     * Gets the goog.events.ListenableKey for the event or null if no such
     * listener is in use.
     *
     * @see [listenable]
     */
    function getListener(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):ListenableKey;

    /**
     * Adds an event listener. A listener can only be added once to an
     * object and if it is added again the key for the listener is
     * returned. Note that if the existing listener is a one-off listener
     * (registered via listenOnce), it will no longer be a one-off
     * listener after a call to listen().
     *
     * @see [listenable]
     */
    function listen(type:Object, listener:Function, opt_useCapture:Boolean = false, opt_listenerScope:Object = null):ListenableKey;

    /**
     * Gets all listeners in this listenable for the given type and
     * capture mode.
     *
     * @see [listenable]
     */
    function getListeners(type:Object, capture:Boolean):Array;

}
}
