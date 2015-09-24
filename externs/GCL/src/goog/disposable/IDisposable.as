package goog.disposable {

/**
 * Interface for a disposable object.  If a instance requires cleanup
 * (references COM objects, DOM notes, or other disposable objects), it should
 * implement this interface (it may subclass goog.Disposable).
 *
 * @see [idisposable]
 */
public interface IDisposable {
	function dispose():void;
	function isDisposed():Boolean;
}
}
