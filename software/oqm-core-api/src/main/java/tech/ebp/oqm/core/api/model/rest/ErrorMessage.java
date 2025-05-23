package tech.ebp.oqm.core.api.model.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
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
	private String displayMessage = "Unspecified error.";
	
	@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		include = JsonTypeInfo.As.WRAPPER_OBJECT,
		property = "causeType"
	)
	
	@lombok.Builder.Default
	private Object cause = null;
	
	public ErrorMessage(String errorMessage) {
		this(errorMessage, new Object());
	}
	
	public ErrorMessage(Throwable cause) {
		this(cause.getMessage(), cause);
	}
	
}
