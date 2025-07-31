package tech.ebp.oqm.core.api.exception;

public class DataImportException extends RuntimeException {

	public DataImportException() {
	}

	public DataImportException(String message) {
		super(message);
	}

	public DataImportException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataImportException(Throwable cause) {
		super(cause);
	}

	public DataImportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
