package tech.ebp.oqm.lib.core.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * The response object from when errors occur
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ErrorMessage {
	
	@lombok.Builder.Default
	private String displayMessage = "";
	@lombok.Builder.Default
	private Throwable cause = null;
	
	public ErrorMessage(String errorMessage) {
		this(errorMessage, null);
	}
	
	public ErrorMessage(Throwable cause) {
		this(cause.getMessage(), cause);
	}
	
	//TODO:: builders for types of exceptions?
}
