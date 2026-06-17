package tech.ebp.oqm.plugin.mssController.model.exception;

public class ModuleSetupFailedException extends Exception {

	public ModuleSetupFailedException() {
	}

	public ModuleSetupFailedException(String message) {
		super(message);
	}

	public ModuleSetupFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModuleSetupFailedException(Throwable cause) {
		super(cause);
	}

	public ModuleSetupFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
