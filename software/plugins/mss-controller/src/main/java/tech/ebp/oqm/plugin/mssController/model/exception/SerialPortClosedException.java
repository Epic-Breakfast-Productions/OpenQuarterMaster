package tech.ebp.oqm.plugin.mssController.model.exception;

public class SerialPortClosedException extends Exception {

	public SerialPortClosedException() {
	}

	public SerialPortClosedException(String message) {
		super(message);
	}

	public SerialPortClosedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerialPortClosedException(Throwable cause) {
		super(cause);
	}

	public SerialPortClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
