package tech.ebp.oqm.lib.core.api.java.utils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ConvenienceData {
	
	
	private String oqmDbIdOrName;
	
	/**
	 * Convenience structure to hold account credentials.
	 */
	@Getter(AccessLevel.PRIVATE)
	private Map<String, OqmCredentials> credentialsMap = new HashMap<>();
}
