package tech.ebp.oqm.lib.core.rest.auth.externalService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * The response object from an external service login request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExternalServiceLoginResponse {
	
	private String token;
	private Instant expires;
}
