package tech.ebp.oqm.lib.core.rest.auth.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * The response object from a user login request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceLoginResponse {
	
	private String token;
	private Instant expires;
}
