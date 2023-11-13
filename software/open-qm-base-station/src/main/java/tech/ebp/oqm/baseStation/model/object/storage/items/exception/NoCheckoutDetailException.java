package tech.ebp.oqm.baseStation.model.object.storage.items.exception;

public class NoCheckoutDetailException extends IllegalArgumentException {
	
	public NoCheckoutDetailException() {
	}
	
	public NoCheckoutDetailException(String s) {
		super(s);
	}
	
	public NoCheckoutDetailException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NoCheckoutDetailException(Throwable cause) {
		super(cause);
	}
}
