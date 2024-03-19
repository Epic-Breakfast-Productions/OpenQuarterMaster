package tech.ebp.oqm.baseStation.model.object.storage.items.exception;

public class NoStorageBlockException extends IllegalArgumentException {
	
	public NoStorageBlockException() {
	}
	
	public NoStorageBlockException(String s) {
		super(s);
	}
	
	public NoStorageBlockException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NoStorageBlockException(Throwable cause) {
		super(cause);
	}
}
