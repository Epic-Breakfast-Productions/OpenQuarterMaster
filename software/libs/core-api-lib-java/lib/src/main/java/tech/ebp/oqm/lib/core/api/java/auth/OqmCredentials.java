package tech.ebp.oqm.lib.core.api.java.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Class to define a credentials carrying object.
 */
@Data
@AllArgsConstructor
//@NoArgsConstructor
@SuperBuilder
public abstract class OqmCredentials {
	
	public abstract String getAccessHeaderContent();
}
