package tech.ebp.oqm.core.api.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class UpgradeFailedException extends RuntimeException {
	public UpgradeFailedException(JsonProcessingException e, Class<?> clazz){
		super(
			"Failed to parse resulting json to " + clazz.getCanonicalName() + ". Error: " + e.getMessage(),
			e
		);
	}
}
