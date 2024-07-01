package tech.ebp.oqm.core.api.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class UpgradeFailedException extends RuntimeException {


	public UpgradeFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UpgradeFailedException(Throwable cause) {
		super(cause);
	}

	public UpgradeFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpgradeFailedException(String message) {
		super(message);
	}

	public UpgradeFailedException(JsonProcessingException e, Class<?> clazz){
		super(
			"Failed to parse resulting json to " + clazz.getCanonicalName() + ". Error: " + e.getMessage(),
			e
		);
	}
}
