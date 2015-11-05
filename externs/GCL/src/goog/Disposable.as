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

package goog {

import goog.disposable.IDisposable;

/**
 * Class that provides the basic implementation for disposable objects. If your
 * class holds one or more references to COM objects, DOM nodes, or other
 * disposable objects, it should extend this class or implement the disposable
 * interface (defined in goog.disposable.IDisposable).
 *
 * @see [disposable]
 */
public class Disposable implements goog.disposable.IDisposable {

    /**
     * Class that provides the basic implementation for disposable objects. If your
     * class holds one or more references to COM objects, DOM nodes, or other
     * disposable objects, it should extend this class or implement the disposable
     * interface (defined in goog.disposable.IDisposable).
     *
     * @see [disposable]
     */
    public function Disposable() {
        super();
    }

    /**
     * If monitoring the goog.Disposable instances is enabled, stores the creation
     * stack trace of the Disposable instance.
     *
     * @see JSType - [string] 
     * @see [disposable]
     */
    public var creationStack:String;

    /**
     * Callbacks to invoke when this object is disposed.
     *
     * @see JSType - [(Array<Function>|null)] 
     * @see [disposable]
     */
    public var onDisposeCallbacks_:Array;

    /**
     * Returns True if we can verify the object is disposed.
     * Calls {@code isDisposed} on the argument if it supports it.  If obj
     * is not an object with an isDisposed() method, return false.
     *
     * @param obj [*] The object to investigate.
     * @see [disposable]
     * @returns {boolean} True if we can verify the object is disposed.
     */
    public static function isDisposed(obj:*):Boolean {  return null; }

    /**
     * Associates a disposable object with this object so that they will be disposed
     * together.
     *
     * @param disposable [(goog.disposable.IDisposable|null)] that will be disposed when this object is disposed.
     * @see [disposable]
     */
    public function registerDisposable(disposable:goog.disposable.IDisposable):void {  }

    /**
     * Disposes of the object. If the object hasn't already been disposed of, calls
     * {@link #disposeInternal}. Classes that extend {@code goog.Disposable} should
     * override {@link #disposeInternal} in order to delete references to COM
     * objects, DOM nodes, and other disposable objects. Reentrant.
     *
     * @see [disposable]
     * @returns {undefined} Nothing.
     */
    public function dispose():void /* undefined */ { }

    /**
     * Deletes or nulls out any references to COM objects, DOM nodes, or other
     * disposable objects. Classes that extend {@code goog.Disposable} should
     * override this method.
     * Not reentrant. To avoid calling it twice, it must only be called from the
     * subclass' {@code disposeInternal} method. Everywhere else the public
     * {@code dispose} method must be used.
     * For example:
     * <pre>
     * mypackage.MyClass = function() {
     * mypackage.MyClass.base(this, 'constructor');
     * // Constructor logic specific to MyClass.
     * ...
     * };
     * goog.inherits(mypackage.MyClass, goog.Disposable);
     * 
     * mypackage.MyClass.prototype.disposeInternal = function() {
     * // Dispose logic specific to MyClass.
     * ...
     * // Call superclass's disposeInternal at the end of the subclass's, like
     * // in C++, to avoid hard-to-catch issues.
     * mypackage.MyClass.base(this, 'disposeInternal');
     * };
     * </pre>
     *
     * @see [disposable]
     */
    public function disposeInternal():void {  }

    /**
     * Clears the registry of undisposed objects but doesn't dispose of them.
     *
     * @see [disposable]
     */
    public static function clearUndisposedObjects():void {  }

    /**
     * Invokes a callback function when this object is disposed. Callbacks are
     * invoked in the order in which they were added. If a callback is added to
     * an already disposed Disposable, it will be called immediately.
     *
     * @param callback [function (this:T): ?] The callback function.
     * @param opt_scope [(T|null|undefined)] An optional scope to call the callback in.
     * @see [disposable]
     */
    public function addOnDisposeCallback(callback:Object, opt_scope:Object = null):Object {  return null; }

    /**
     * @see [disposable]
     * @returns {Array<goog.Disposable>} All {@code goog.Disposable} objects that haven't been disposed of.
     */
    public static function getUndisposedObjects():Array {  return null; }

    public function isDisposed():Boolean {
        return false;
    }
}
}
