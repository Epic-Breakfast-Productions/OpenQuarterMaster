package tech.ebp.oqm.plugin.mssController.model.exception;

import lombok.Getter;
import tech.ebp.oqm.plugin.mssController.config.ModuleConfig;

public class ModuleSetupFailedException extends Exception {

	@Getter
	private final Object moduleConfig;

	public ModuleSetupFailedException(Object moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

	public ModuleSetupFailedException(Object moduleConfig, String message) {
		this.moduleConfig = moduleConfig;
		super(message);
	}

	public ModuleSetupFailedException(Object moduleConfig, String message, Throwable cause) {
		this.moduleConfig = moduleConfig;
		super(message, cause);
	}

	public ModuleSetupFailedException(Object moduleConfig, Throwable cause) {
		this.moduleConfig = moduleConfig;
		super(cause);
	}

}
