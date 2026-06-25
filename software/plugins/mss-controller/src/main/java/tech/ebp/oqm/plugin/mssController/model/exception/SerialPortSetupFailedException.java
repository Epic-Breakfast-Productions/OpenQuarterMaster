package tech.ebp.oqm.plugin.mssController.model.exception;

public class SerialPortSetupFailedException extends ModuleSetupFailedException {

	public SerialPortSetupFailedException(Object moduleConfig) {
		super(moduleConfig);
	}

	public SerialPortSetupFailedException(Object moduleConfig, String message) {
		super(moduleConfig, message);
	}

	public SerialPortSetupFailedException(Object moduleConfig, String message, Throwable cause) {
		super(moduleConfig, message, cause);
	}

	public SerialPortSetupFailedException(Object moduleConfig, Throwable cause) {
		super(moduleConfig, cause);
	}
}
