package tech.ebp.oqm.core.api.model.object.storage.items.exception;

public class StoredNotFoundException extends IllegalArgumentException {
	
	public StoredNotFoundException() {
	}
	
	public StoredNotFoundException(String s) {
		super(s);
	}
	
	public StoredNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public StoredNotFoundException(Throwable cause) {
		super(cause);
	}
}
