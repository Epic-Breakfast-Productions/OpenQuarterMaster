package tech.ebp.oqm.plugin.mssController.model.exception.command;

public class MssCommandError extends Exception {

	public MssCommandError() {
	}

	public MssCommandError(String message) {
		super(message);
	}

	public MssCommandError(String message, Throwable cause) {
		super(message, cause);
	}

	public MssCommandError(Throwable cause) {
		super(cause);
	}

	public MssCommandError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
