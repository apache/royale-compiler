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
 * An interface that describes a single registered listener.
 *
 * @see [listenable]
 */
public interface ListenableKey {

    /**
     * Whether the listener works on capture phase.
     *
     * @see JSType - [boolean] 
     * @see [listenable]
     */
    function get capture():Boolean;
    function set capture(value:Boolean):void;

    /**
     * The listener function.
     *
     * @see JSType - [(function (?): ?|null|{handleEvent: function (?): ?})] 
     * @see [listenable]
     */
    function get listener():Object;
    function set listener(value:Object):void;

    /**
     * The source event target.
     *
     * @see JSType - [(Object|goog.events.EventTarget|goog.events.Listenable)] 
     * @see [listenable]
     */
    function get src():Object;
    function set src(value:Object):void;

    /**
     * The event type the listener is listening to.
     *
     * @see JSType - [string] 
     * @see [listenable]
     */
    function get type():String;
    function set type(value:String):void;

    /**
     * A globally unique number to identify the key.
     *
     * @see JSType - [number] 
     * @see [listenable]
     */
    function get key():Number;
    function set key(value:Number):void;

    /**
     * The 'this' object for the listener function's scope.
     *
     * @see JSType - [(Object|null)] 
     * @see [listenable]
     */
    function get handler():Object;
    function set handler(value:Object):void;

    /**
     * Reserves a key to be used for ListenableKey#key field.
     *
     * @see [listenable]
     * @returns {number} A number to be used to fill ListenableKey#key field.
     */
    function reserveKey():Number

}
}
