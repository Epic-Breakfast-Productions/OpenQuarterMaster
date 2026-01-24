package tech.ebp.oqm.core.characteristics.exception;

public class FailedReadingCharacteristicsException extends RuntimeException {
	
	public FailedReadingCharacteristicsException() {
	}
	
	public FailedReadingCharacteristicsException(String message) {
		super(message);
	}
	
	public FailedReadingCharacteristicsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FailedReadingCharacteristicsException(Throwable cause) {
		super(cause);
	}
	
	public FailedReadingCharacteristicsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
