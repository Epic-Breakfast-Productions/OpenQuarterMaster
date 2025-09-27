package tech.ebp.oqm.lib.core.api.java.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * Class to define a credentials carrying object.
 *
 * All relevant public facing methods should be threadsafe.
 */
@Data
@AllArgsConstructor
//@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class OqmCredentials {
	
	/**
	 * Gets the credential string for the "Authorization" header.
	 *
	 * Must be a threadsafe operation
	 * @return
	 */
	public abstract String getAccessHeaderContent();
}
