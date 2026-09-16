package tech.ebp.oqm.core.api.model.object.storage.items.exception;

public class AlreadyStoredException extends IllegalArgumentException {
	
	public AlreadyStoredException() {
	}
	
	public AlreadyStoredException(String s) {
		super(s);
	}
	
	public AlreadyStoredException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public AlreadyStoredException(Throwable cause) {
		super(cause);
	}
}
