package tech.ebp.oqm.lib.core.api.java.utils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;

import java.util.HashMap;
import java.util.Map;

/**
 * This object provides convenient places to store commonly used data alongside the client.
 */
@Data
@NoArgsConstructor
public class ConvenienceData {
	
	/**
	 * The database this client's implementation is focused on.
	 */
	private String oqmDbIdOrName;
	
	/**
	 * Convenience structure to hold account credentials.
	 */
	@Getter(AccessLevel.PRIVATE)
	private Map<String, OqmCredentials> credentialsMap = new HashMap<>();
}
