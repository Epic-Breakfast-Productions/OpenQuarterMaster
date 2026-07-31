package tech.ebp.oqm.plugin.mssController.model.exception.command;

public class MssCommandTimeoutException extends MssCommandError {

	public MssCommandTimeoutException() {
	}

	public MssCommandTimeoutException(String message) {
		super(message);
	}

	public MssCommandTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public MssCommandTimeoutException(Throwable cause) {
		super(cause);
	}

	public MssCommandTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
