package tech.ebp.oqm.core.api.exception.db;

/**
 * TODO:: main object specific info?
 */
public class AlreadyCheckedInException extends IllegalArgumentException {
	public AlreadyCheckedInException() {
	}
	
	public AlreadyCheckedInException(String s) {
		super(s);
	}
	
	public AlreadyCheckedInException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public AlreadyCheckedInException(Throwable cause) {
		super(cause);
	}
}
