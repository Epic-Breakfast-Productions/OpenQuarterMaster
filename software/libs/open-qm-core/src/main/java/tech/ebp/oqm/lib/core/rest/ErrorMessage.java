package tech.ebp.oqm.lib.core.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The response object from when errors occur
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage {
	
	private String error;
	private String detail = "";
	
	public ErrorMessage(String error) {
		this.error = error;
	}
}
