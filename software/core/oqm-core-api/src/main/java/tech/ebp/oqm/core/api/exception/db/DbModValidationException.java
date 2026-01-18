package tech.ebp.oqm.core.api.exception.db;

/**
 * TODO:: main object specific info?
 */
public class DbModValidationException extends IllegalArgumentException {
	public DbModValidationException() {
	}
	
	public DbModValidationException(String s) {
		super(s);
	}
	
	public DbModValidationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DbModValidationException(Throwable cause) {
		super(cause);
	}
}
