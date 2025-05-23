package tech.ebp.oqm.core.api.exception;

public class DataExportException extends RuntimeException {
	
	public DataExportException() {
	}
	
	public DataExportException(String message) {
		super(message);
	}
	
	public DataExportException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DataExportException(Throwable cause) {
		super(cause);
	}
	
	public DataExportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
