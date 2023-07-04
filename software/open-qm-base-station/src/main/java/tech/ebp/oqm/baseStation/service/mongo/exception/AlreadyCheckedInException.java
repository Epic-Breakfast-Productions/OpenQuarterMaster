package tech.ebp.oqm.baseStation.service.mongo.exception;

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
