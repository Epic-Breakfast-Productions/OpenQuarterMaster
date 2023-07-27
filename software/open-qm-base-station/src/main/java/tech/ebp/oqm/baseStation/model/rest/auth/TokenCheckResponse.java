package tech.ebp.oqm.baseStation.model.rest.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * The response object from a token check request
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenCheckResponse {
	
	private boolean hadToken = false;
	private boolean tokenSecure = false;
	private boolean expired = true;
	private Date expirationDate = null;
	
	//TODO:: add entity id, type
}
