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
