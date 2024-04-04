package tech.ebp.oqm.core.api.model.object.storage.items.exception;

public class NotEnoughStoredException extends IllegalArgumentException {
	
	public NotEnoughStoredException() {
	}
	
	public NotEnoughStoredException(String s) {
		super(s);
	}
	
	public NotEnoughStoredException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NotEnoughStoredException(Throwable cause) {
		super(cause);
	}
}
