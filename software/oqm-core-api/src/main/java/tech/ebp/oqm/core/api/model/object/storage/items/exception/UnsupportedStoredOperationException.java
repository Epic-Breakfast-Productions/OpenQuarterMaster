package tech.ebp.oqm.core.api.model.object.storage.items.exception;

public class UnsupportedStoredOperationException extends UnsupportedOperationException {
	
	public UnsupportedStoredOperationException() {
	}
	
	public UnsupportedStoredOperationException(String s) {
		super(s);
	}
	
	public UnsupportedStoredOperationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnsupportedStoredOperationException(Throwable cause) {
		super(cause);
	}
}
