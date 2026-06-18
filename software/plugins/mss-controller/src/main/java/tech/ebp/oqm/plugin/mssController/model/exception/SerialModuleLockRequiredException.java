package tech.ebp.oqm.plugin.mssController.model.exception;

public class SerialModuleLockRequiredException extends RuntimeException {

	public SerialModuleLockRequiredException() {
	}

	public SerialModuleLockRequiredException(String message) {
		super(message);
	}

	public SerialModuleLockRequiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerialModuleLockRequiredException(Throwable cause) {
		super(cause);
	}

	public SerialModuleLockRequiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
