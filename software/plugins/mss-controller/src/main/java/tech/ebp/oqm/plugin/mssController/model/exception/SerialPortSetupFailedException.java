package tech.ebp.oqm.plugin.mssController.model.exception;

public class SerialPortSetupFailedException extends ModuleSetupFailedException {

	public SerialPortSetupFailedException() {
	}

	public SerialPortSetupFailedException(String message) {
		super(message);
	}

	public SerialPortSetupFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerialPortSetupFailedException(Throwable cause) {
		super(cause);
	}

	public SerialPortSetupFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
